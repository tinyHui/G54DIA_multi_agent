package Solution;

import Solution.Planner.Manager;
import Solution.Planner.TaskPair;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.multilibrary.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by JasonChen on 2/12/15.
 */

public class SmartTanker extends Tanker {
    static int MAX_RANGE = MAX_FUEL / 2;
    final static int
            // action mode
            REFUEL = 0,
            LOAD_WATER = 1,
            DELIVER_WATER = 2,
            // Driving mode
            EXPLORE = 3,
            DRIVE_TO_PUMP = 4,
            DRIVE_TO_FACILITY = 5;

    int id;
    int mode = EXPLORE;

    Status status = new Status();
    MemMap map;
    TaskSys task_sys;
    Manager manager;
    Driver driver;

    MemPoint[] explore_target_point_list = {new MemPoint(19, 25),
                                            new MemPoint(38, 0),
                                            new MemPoint(19, -25),
                                            new MemPoint(-19, -25),
                                            new MemPoint(-38, 0),
                                            new MemPoint(-19, 25),
                                            new MemPoint(-25, 19),
                                            new MemPoint(0, 38),
                                            new MemPoint(25, 19),
                                            new MemPoint(25, -19),
                                            new MemPoint(0, -38),
                                            new MemPoint(-25, -19),
                                            new MemPoint(38,38),
                                            new MemPoint(0, 0),
                                            new MemPoint(-38,-38),
                                            new MemPoint(0, 0),
                                            new MemPoint(38,-38),
                                            new MemPoint(0, 0),
                                            new MemPoint(-38,38),
                                            new MemPoint(0, 0)};
    Task current_task;
    MemPoint target_point;
    MemPoint explore_target_point;
    int explore_count;
    TaskPair current_task_pair = new TaskPair();
    Queue<TaskPair> plan_list = new LinkedList<TaskPair>();

    public SmartTanker(int id, MemMap map, TaskSys task_sys, Manager manager) {
        this.id = id;
        this.map = map;
        this.task_sys = task_sys;
        this.manager = manager;
        this.driver = new Driver(map, task_sys);
        this.explore_count = id * 3;
    }

    @Override
    public Action senseAndAct(Cell[][] view, long time_step) {
        Action act;
        updateState(view, time_step);
        recordMap(view);

        this.mode = arbitrator();

        switch (this.mode) {
            case EXPLORE:
            case DRIVE_TO_FACILITY:
                act = this.driver.driveTo(this.target_point);
                break;
            case DRIVE_TO_PUMP:
                act = this.driver.driveTo(MemPoint.FUEL_PUMP);
                break;
            case REFUEL:
                act = new RefuelAction();
                break;
            case LOAD_WATER:
                act = new LoadWaterAction();
                break;
            case DELIVER_WATER:
                act = new DeliverWaterAction(this.current_task);
                break;
            default:
                throw new ValueException("Unrecognised mode");
        }
        return act;
    }

    private void updateState(Cell[][] view, long time_step) {
        this.status.current_point = this.driver.getCurrentPoint();
        this.status.current_cell = this.getCurrentCell(view);
        this.status.completed_count = this.getCompletedCount();
        this.status.time_remain = this.status.DURATION - time_step;
    }

    private boolean enoughFuel() {
        int cost = this.status.current_point.calcDistance(this.target_point) +
                this.target_point.calcDistanceToFuel();

        return !(cost > this.getFuelLevel() &&
                !(this.status.current_cell instanceof FuelPump));
    }

    private void recordMap(Cell[][] view) {
        for (int y=-VIEW_RANGE; y < VIEW_RANGE; y++) {
            for (int x=-VIEW_RANGE; x < VIEW_RANGE; x++) {
                int real_x = this.status.current_point.x + x;
                int real_y = this.status.current_point.y - y;
                MemPoint point = new MemPoint(real_x, real_y);
                Cell cell = view[VIEW_RANGE + x][VIEW_RANGE + y];
                if (point.calcDistanceToFuel() <= MAX_RANGE) {
                    if (cell instanceof Station) {
                        this.task_sys.appendTask(point, cell);
                        this.map.appendStation(point, (Station) cell);
                    } else if (cell instanceof Well) {
                        this.map.appendWell(point, (Well) cell);
                    }
                }
            }
        }
    }

    private MemPoint exploreWorld() {
        if (this.status.current_point.equals(this.explore_target_point)) {
            this.explore_count++;
        }

        if (this.explore_count >= this.explore_target_point_list.length) {
            this.explore_count = 0;
        }
        this.explore_target_point = (MemPoint) this.explore_target_point_list[this.explore_count].clone();


        if (this.mode != EXPLORE &&
                this.mode != REFUEL &&
                this.mode != LOAD_WATER &&
                this.status.current_point.calcDistance(this.explore_target_point) < VIEW_RANGE) {
            this.explore_count++;
            if (this.explore_count >= this.explore_target_point_list.length) {
                this.explore_count = 0;
            }
            this.explore_target_point = (MemPoint) this.explore_target_point_list[this.explore_count].clone();
        }

        return (MemPoint) this.explore_target_point.clone();
    }

    private int arbitrator() {
        int command = EXPLORE;

        if (!this.plan_list.isEmpty() ||
                !this.current_task_pair.isNull()) {
            // have plan list
            this.status.obeying = true;
            if (this.current_task_pair.isNull()) {
                // no plan occupied, try to read a new one
                this.current_task_pair = this.plan_list.poll();
            }

            if (!this.current_task_pair.isNull()) {
                // have plan occupied
                if (!this.status.current_point.equals(this.current_task_pair.p)) {
                    // not at task point
                    this.target_point = (MemPoint) this.current_task_pair.p.clone();
                    command = DRIVE_TO_FACILITY;
                } else {
                    // at plan point
                    if (this.current_task_pair.t == null) {
                        // not a task
                        this.current_task_pair = new TaskPair();
                        if (this.status.current_cell instanceof FuelPump) {
                            return REFUEL;
                        } else if(this.status.current_cell instanceof Well) {
                            return LOAD_WATER;
                        }
                    } else {
                        // is a task
                        this.current_task = this.current_task_pair.t;
                        this.current_task_pair = new TaskPair();
                        this.status.delivered_water += Math.min(this.current_task.getRequired(), this.getWaterLevel());
                        return DELIVER_WATER;
                    }
                }
            }
        } else {
            // empty plan list
            this.status.obeying = false;
            if (this.status.current_cell instanceof FuelPump &&
                    this.getFuelLevel() < MAX_FUEL) {
                // at fuel pump, gas not max
                return REFUEL;
            } else if (this.status.current_cell instanceof Well &&
                    this.getWaterLevel() < MAX_WATER) {
                // at water well, water not max
                return LOAD_WATER;
            }

            this.target_point = exploreWorld();
            if (!enoughFuel()) {
                command = DRIVE_TO_PUMP;
            }
        }

        return command;
    }

    public Status getStatus() {
        return this.status;
    }

    public void updatePlanList(Queue<TaskPair> plan_list) {
        this.current_task_pair = new TaskPair();
        this.plan_list.clear();
        this.plan_list = new LinkedList<TaskPair>(plan_list);
    }
}
