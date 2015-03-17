package Solution;

import uk.ac.nott.cs.g54dia.multilibrary.Cell;
import uk.ac.nott.cs.g54dia.multilibrary.Station;
import uk.ac.nott.cs.g54dia.multilibrary.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by JasonChen on 2/16/15.
 */
public class TaskSys {
    private HashMap<Task, MemPoint> task_list = new HashMap<Task, MemPoint>();
    private boolean new_task = false;
    private int prev_task_num = 0;

    public HashMap<Task, MemPoint> scanTaskList() {
        Iterator it = this.task_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Task t_r = (Task) pairs.getKey();
            if (t_r.isComplete()) {
                it.remove();
            }
        }
        return (HashMap<Task, MemPoint>) this.task_list.clone();
    }

    public boolean haveNewTask() {
        return this.new_task;
    }

    public void resetNewTask() {
        this.new_task = false;
        this.prev_task_num = this.task_list.size();
    }

    public void appendTask(MemPoint p, Cell cell) {
        Task t = ((Station) cell).getTask();
        if (t != null) {
            this.task_list.put(t, (MemPoint) p.clone());
            int task_num = this.task_list.size();
            this.new_task = task_num > this.prev_task_num;
        }
    }
}
