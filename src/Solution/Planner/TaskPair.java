package Solution.Planner;

import Solution.MemPoint;
import uk.ac.nott.cs.g54dia.multilibrary.Task;

/**
 * Created by JasonChen on 2/25/15.
 */
public class TaskPair implements Cloneable {
    public MemPoint p;
    public Task t;

    public TaskPair() {
    }

    public TaskPair(MemPoint p, Task t) {
        this.p = (MemPoint) p.clone();
        this.t = t;
    }

    public boolean isNull() {
        return this.p == null && this.t == null;
    }

    public Object clone() {
        return new TaskPair(p,t);
    }
}
