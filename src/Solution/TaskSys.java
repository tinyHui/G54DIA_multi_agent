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

    public HashMap<Task, MemPoint> scanTaskList() {
        Iterator it = this.task_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Task t_r = (Task) pairs.getKey();
            if (t_r.isComplete()) {
                it.remove();
            }
        }
        return (HashMap<Task, MemPoint>) task_list.clone();
    }

    public void appendTask(MemPoint p, Cell cell) {
        Task t = ((Station) cell).getTask();
        if (t != null) {
            this.task_list.put(t, (MemPoint) p.clone());
        }
    }
}
