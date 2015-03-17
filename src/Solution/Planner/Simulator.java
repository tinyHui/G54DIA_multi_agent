package Solution.Planner;

import Solution.MemMap;
import Solution.MemPoint;
import Solution.SmartTanker;
import Solution.Status;
import uk.ac.nott.cs.g54dia.multilibrary.Tanker;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by JasonChen on 2/26/15.
 */
public class Simulator {
    private final static TaskPair FUEL_PAIR = new TaskPair(MemPoint.FUEL_PUMP, null);

    private ArrayList<TaskPair> visit_list = new ArrayList<TaskPair>();
    private ArrayList<TaskPair> visit_list_plan = new ArrayList<TaskPair>();

    private Random rand = new Random(System.nanoTime());
    private MemMap map;
    private MemPoint current_point;
    private int water_level = -1;
    private int completed_count = 0;
    private int delivered_water = 0;
    private int fuel_level;
    private long time_remain = 0;
    private long time_spend = 0;

    public Simulator(ArrayList<TaskPair> visit_list, SmartTanker tanker, MemMap map) {
        Status status = tanker.getStatus();
        this.visit_list = (ArrayList<TaskPair>) visit_list.clone();
        this.current_point = status.getCurrentPointCopy();
        this.water_level = tanker.getWaterLevel();
        this.fuel_level = tanker.getFuelLevel();
        this.completed_count = status.completed_count;
        this.delivered_water = status.delivered_water;
        this.time_remain = status.time_remain;
        this.map = map;
    }

    private MemPoint checkFuel(MemPoint target_point) {
        int distance_c_t = this.current_point.calcDistance(target_point);
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

    public void generate() {
        for (TaskPair current_pair : this.visit_list) {
            MemPoint target_point;

            if (current_pair.t.getRequired() > this.water_level) {
                // not enough water
                MemPoint well = this.map.getMidWell(this.current_point, current_pair.p);
                if (well == null) {
                    continue;
                }
                // go well
                this.visit_list_plan.add(new TaskPair(well, null));
                // target p is well
                target_point = well;
                // check fuel and update current p
                this.current_point = checkFuel(target_point);
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
            this.current_point = checkFuel(target_point);
            // update score
            this.completed_count++;
            this.delivered_water += current_pair.t.getRequired();
        }
    }

    public int getCompleteNum() {
        return this.completed_count;
    }

    public long getDeliverWaterNum() {
        return this.delivered_water;
    }

    public long getCost() {
        return time_spend;
    }

    public ArrayList<TaskPair> getVisitList() {
        return this.visit_list_plan;
    }
}