package Solution.Planner;

import Solution.MemMap;
import Solution.MemPoint;
import Solution.Status;
import uk.ac.nott.cs.g54dia.multilibrary.Tanker;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by JasonChen on 2/26/15.
 */
public class Simulator {
    private final static Double ESCAPE_RATE = 0.5;
    private final static Double DIST_TO_PUMP_RATE = 0.6;
    private final static TaskPair FUEL_PAIR = new TaskPair(MemPoint.FUEL_PUMP, null);

    private ArrayList<TaskPair> visit_list = new ArrayList<TaskPair>();
    private ArrayList<TaskPair> visit_list_plan = new ArrayList<TaskPair>();

    private Random rand = new Random(System.currentTimeMillis());
    private MemMap map;
    private int water_level = -1;
    private int completed_count = 0;
    private int delivered_water = 0;
    private int fuel_level;
    private long time_remain = 0;
    private long time_spend = 0;
    private long score = 0;

    public Simulator(ArrayList<TaskPair> visit_list, MemMap map, Status status) {
        this.visit_list = (ArrayList<TaskPair>) visit_list.clone();
        this.water_level = status.water_level;
        this.completed_count = status.completed_count;
        this.delivered_water = status.delivered_water;
        this.fuel_level = status.fuel_level;
        this.time_remain = status.time_remain;
        this.map = map;
    }

    private MemPoint checkFuel(MemPoint current_point, MemPoint target_point) {
        int distance_c_t = current_point.calcDistance(target_point);
        int distance_f_t = MemPoint.FUEL_PUMP.calcDistance(target_point);
        int cost = distance_c_t + target_point.calcDistanceToFuel();

        if (cost > this.fuel_level) {
            // not enough fuel to go and back, go back fuel pump first
            int size = this.visit_list_plan.size();
            if (size == 0) {
                // none of the point been added, but not enough fuel
                this.visit_list_plan.add(FUEL_PAIR);
            } else {
                this.visit_list_plan.add(size - 1, FUEL_PAIR);
            }
            // fuel level max
            this.fuel_level = Tanker.MAX_FUEL - distance_f_t;
            // update time spend
            this.time_spend += cost + 1;
        } else {
            this.fuel_level -= distance_c_t;
            // update time spend
            this.time_spend += distance_c_t;
        }
        this.time_spend++;
        this.time_remain -= this.time_spend;

        return (MemPoint) target_point.clone();
    }

    public void generate(MemPoint current_point) {
        TaskPair current_pair = new TaskPair();
        while (visit_list.size() > 0) {
            MemPoint target_point;

            if (rand.nextDouble() > ESCAPE_RATE) {
                // not mutate, find the nearest one
                int min_cost = 10000000;
                int min_index = 0;
                int index = 0;
                for (TaskPair tp : visit_list) {
                    int cost;
                    int distance = (int) Math.floor((tp.p.calcDistanceToFuel() -
                            current_point.calcDistanceToFuel()) * DIST_TO_PUMP_RATE);
                    // add well distance if not enough water
                    if (tp.t.getRequired() > this.water_level) {
                        MemPoint well = this.map.getMidWell(current_point, tp.p);
                        distance += current_point.calcDistance(well) + well.calcDistance(tp.p);
                    } else {
                        distance = current_point.calcDistance(tp.p);
                    }
                    cost = distance - tp.t.getRequired();

                    if (cost < min_cost) {
                        current_pair = tp;
                        min_cost = cost;
                        min_index = index;
                    }
                    index++;
                }
                visit_list.remove(min_index);
            } else {
                // mutate, random find one
                int index = this.rand.nextInt(visit_list.size());
                current_pair = visit_list.get(index);
                visit_list.remove(index);
            }

            if (current_pair.t.getRequired() > this.water_level) {
                // not enough water
                MemPoint well = this.map.getMidWell(current_point, current_pair.p);
                // go well
                this.visit_list_plan.add(new TaskPair(well, null));
                // target p is well
                target_point = well;
                // check fuel and update current p
                current_point = checkFuel(current_point, target_point);
                // water level max
                this.water_level = Tanker.MAX_WATER;
            }

            // finish task
            this.visit_list_plan.add(new TaskPair(current_pair.p, current_pair.t));
            // update water level
            this.water_level -= current_pair.t.getRequired();
            // target p is station
            target_point = (MemPoint) current_pair.p.clone();
            // check fuel
            current_point = checkFuel(current_point, target_point);
            // update score
            this.completed_count++;
            this.delivered_water += current_pair.t.getRequired();
            this.score = (long)this.completed_count * (long)this.delivered_water;
        }
    }

    public long getScore() {
        return this.score - this.time_spend;
    }

    public ArrayList<TaskPair> getVisitList() {
        return this.visit_list_plan;
    }
}
