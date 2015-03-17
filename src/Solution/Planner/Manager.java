package Solution.Planner;

import Solution.*;
import uk.ac.nott.cs.g54dia.multilibrary.Tanker;
import uk.ac.nott.cs.g54dia.multilibrary.Task;

import java.util.*;

/**
 * Created by JasonChen on 3/12/15.
 */
public class Manager {
    final static int RETRY_MAX = 50;
    MemMap map;
    TaskSys task_sys;
    int tanker_num;
    ArrayList<SmartTanker> tanker_list;
    ArrayList<Queue<TaskPair>> plan_lists;
    ArrayList<TaskPair> station_list = new ArrayList<TaskPair>();
    Random rand = new Random(System.nanoTime());

    public Manager(MemMap map, TaskSys task_sys, int tanker_num) {
        this.map = map;
        this.task_sys = task_sys;
        this.tanker_num = tanker_num;
        this.tanker_list = new ArrayList<SmartTanker>(this.tanker_num);
        this.plan_lists = new ArrayList<Queue<TaskPair>>(this.tanker_num);
    }

    public void appendTanker(Tanker tanker){
        SmartTanker smart_tanker = (SmartTanker) tanker;
        this.tanker_list.add(smart_tanker);
        this.plan_lists.add(new LinkedList<TaskPair>());
    }

    public void updatePlan() {
        if (this.task_sys.haveNewTask()) {
            long best_score = 0;
            ArrayList<ArrayList<TaskPair>> station_visit_lists;
            ArrayList<ArrayList<TaskPair>> best_visit_lists = new ArrayList<ArrayList<TaskPair>>(this.tanker_num);

            // convert unfinished tasks from hash map to array list
            this.station_list.clear();
            for (Map.Entry<Task, MemPoint> pairs : this.task_sys.scanTaskList().entrySet()) {
                Task t = pairs.getKey();
                MemPoint p = pairs.getValue();
                this.station_list.add(new TaskPair(p, t));
            }

            station_visit_lists = this.stationInOrder();
            for (int i = 0; i < this.tanker_num; i++) {
                best_visit_lists.add(new ArrayList<TaskPair>());
            }


            // start plan
            for (int retry_time = 0; retry_time < RETRY_MAX; retry_time++) {
                ArrayList<ArrayList<TaskPair>> visit_lists = new ArrayList<ArrayList<TaskPair>>();
                int completed_num = 0;
                long deliver_water_num = 0;
                long cost = 0;
                long total_cost = 0;
                long total_score = 0;

                for (int tanker_id = 0; tanker_id < this.tanker_num; tanker_id++) {
                    SmartTanker tanker = this.tanker_list.get(tanker_id);
                    ArrayList<TaskPair> visit_list = (ArrayList <TaskPair>) (station_visit_lists.get(tanker_id)).clone();
                    Collections.shuffle(visit_list);

                    Simulator s = new Simulator(visit_list, tanker, this.map);
                    s.generate();
                    visit_lists.add(s.getVisitList());
                    completed_num += s.getCompleteNum();
                    deliver_water_num += s.getDeliverWaterNum();
                    cost = s.getCost();
                    if (cost > total_cost) {
                        total_cost = cost;
                    }
                }

                total_score = completed_num * deliver_water_num - total_cost;

                if (total_score > best_score) {
                    best_score = total_score;
                    best_visit_lists = visit_lists;
                }
            }

            // convert array list to queue
            for (int tanker_id = 0; tanker_id < this.tanker_num; tanker_id++) {
                Queue<TaskPair> plan_list = this.plan_lists.get(tanker_id);
                plan_list.clear();
                for (TaskPair t : best_visit_lists.get(tanker_id)) {
                    plan_list.add(t);
                }
                this.tanker_list.get(tanker_id).updatePlanList(plan_list);
//                System.out.println("Give tanker " + tanker_id + ", tasks " + plan_list.size());
            }

            this.task_sys.resetNewTask();
        }
    }

    private ArrayList<ArrayList<TaskPair>> stationInOrder() {
        // make station evenly assigned to tankers
        int tanker_station_limit = (int) Math.ceil(this.station_list.size() / (float) this.tanker_num);
        int tanker_station_count[] = new int[this.tanker_num];
        ArrayList<ArrayList<TaskPair>> station_visit_lists = new ArrayList<ArrayList<TaskPair>>(this.tanker_num);

        // initial list
        for (int tanker_id = 0; tanker_id < this.tanker_num; tanker_id++) {
            station_visit_lists.add(new ArrayList<TaskPair>());
        }

        for (TaskPair current_pair : this.station_list) {
            int distance = 0;
            int min_distance = 100;
            int min_id = 0;
            for (int tanker_id = 0; tanker_id < this.tanker_num; tanker_id++) {
                if (tanker_station_count[tanker_id] > tanker_station_limit ||
                        tanker_list.get(tanker_id).getStatus().busy)
                    continue;
                distance = current_pair.p.calcDistance(this.tanker_list.get(tanker_id).getStatus().getCurrentPointCopy());
                if (distance < min_distance) {
                    min_distance = distance;
                    min_id = tanker_id;
                }
            }
            tanker_station_count[min_id] += 1;
            station_visit_lists.get(min_id).add(current_pair);
        }

        return station_visit_lists;
    }

}
