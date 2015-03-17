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
    Random rand = new Random(System.nanoTime());

    public Manager(MemMap map, TaskSys task_sys, int tanker_num) {
        this.map = map;
        this.task_sys = task_sys;
        this.tanker_num = tanker_num;
        this.tanker_list = new ArrayList<SmartTanker>(this.tanker_num);
        this.plan_lists = new ArrayList<Queue<TaskPair>>(this.tanker_num);
    }

    public void appendTanker(Tanker tanker){
        this.tanker_list.add((SmartTanker) tanker);
        this.plan_lists.add(new LinkedList<TaskPair>());
    }

    public void updatePlan() {
        if (this.task_sys.haveNewTask()) {
            long best_score = 0;
            ArrayList<TaskPair> visit_list = new ArrayList<TaskPair>();
            ArrayList<ArrayList<TaskPair>> best_visit_list_pair = new ArrayList<ArrayList<TaskPair>>(this.tanker_num);
            for (int i = 0; i < this.tanker_num; i++) {
                best_visit_list_pair.add(new ArrayList<TaskPair>());
            }

            // convert unfinished tasks from hash map to array list
            for (Map.Entry<Task, MemPoint> pairs : this.task_sys.scanTaskList().entrySet()) {
                Task t = pairs.getKey();
                MemPoint p = pairs.getValue();
                visit_list.add(new TaskPair(p, t));
            }

            // start plan
            for (int retry_time = 0; retry_time < RETRY_MAX; retry_time++) {
                int completed_num = 0;
                long deliver_water_num = 0;
                long cost = 0;
                long total_cost = 0;
                long total_score = 0;
                Collections.shuffle(visit_list, this.rand);
                ArrayList<ArrayList<TaskPair>> visit_list_pair = new ArrayList<ArrayList<TaskPair>>(this.tanker_num);
                for (int i = 0; i < this.tanker_num; i++) {
                    visit_list_pair.add(new ArrayList<TaskPair>());
                }

                for (TaskPair current_pair : visit_list) {
                    int tanker_id = this.rand.nextInt(this.tanker_num);
                    visit_list_pair.get(tanker_id).add(current_pair);
                }

                for (int tanker_id = 0; tanker_id < this.tanker_num; tanker_id++) {
                    SmartTanker tanker = this.tanker_list.get(tanker_id);
                    Simulator s = new Simulator(visit_list_pair.get(tanker_id), tanker, this.map);
                    s.generate();
                    visit_list_pair.set(tanker_id, s.getVisitList());
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
                    best_visit_list_pair = (ArrayList<ArrayList<TaskPair>>) visit_list_pair.clone();
                }
            }

            // convert array list to queue
            for (int tanker_id = 0; tanker_id < this.tanker_num; tanker_id++) {
                Queue<TaskPair> plan_list = this.plan_lists.get(tanker_id);
                plan_list.clear();
                for (TaskPair t : best_visit_list_pair.get(tanker_id)) {
                    plan_list.add(t);
                }
                this.tanker_list.get(tanker_id).updatePlanList(plan_list);
//                System.out.println("Give tanker " + tanker_id + ", tasks " + plan_list.size());
            }

            this.task_sys.resetNewTask();
        }
    }

}
