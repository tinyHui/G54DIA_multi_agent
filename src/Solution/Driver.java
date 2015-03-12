package Solution;

import Solution.Planner.PlanGenerator;
import Solution.Planner.TaskPair;
import uk.ac.nott.cs.g54dia.multilibrary.Action;
import uk.ac.nott.cs.g54dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g54dia.multilibrary.Task;

import java.util.HashMap;
import java.util.Queue;

/**
 * Created by JasonChen on 2/15/15.
 */
public class Driver {
    MemPoint current_point = (MemPoint) MemPoint.FUEL_PUMP.clone();
    MemMap map;
    TaskSys ts;
    int prev_task_list_size = 0;

    public Driver(MemMap map, TaskSys ts) {
        this.map = map;
        this.ts = ts;
    }

    public int getDirection(MemPoint target) {
        if (target == null) {
            return -1;
        }
        int dx = target.x - current_point.x;
        int dy = target.y - current_point.y;

        if (dx > 0 && dy > 0) {
            return MoveAction.NORTHEAST;
        } else if (dx > 0 && dy < 0) {
            return MoveAction.SOUTHEAST;
        } else if (dx > 0 && dy == 0) {
            return MoveAction.EAST;
        } else if (dx < 0 && dy > 0) {
            return MoveAction.NORTHWEST;
        } else if (dx < 0 && dy < 0) {
            return MoveAction.SOUTHWEST;
        } else if (dx < 0 && dy == 0) {
            return MoveAction.WEST;
        } else if (dx == 0 && dy > 0) {
            return MoveAction.NORTH;
        } else if (dx == 0 && dy < 0) {
            return MoveAction.SOUTH;
        } else {
            return -1;
        }
    }

    public Action driveTo(MemPoint target) {
        int direction = this.getDirection(target);
        this.current_point.moveTo(direction);
        return new MoveAction(direction);
    }

    public MemPoint getCurrentPoint() {
        return current_point;
    }

    public boolean plan(Queue<TaskPair> plan_list, Status status) {
        boolean new_plan = false;
        HashMap<Task, MemPoint> task_list = this.ts.scanTaskList();
        if (this.prev_task_list_size != task_list.size()) {
            // more task append, re-plan
            PlanGenerator generator = new PlanGenerator(this.map, this.current_point, status);
            generator.start(plan_list, task_list);
            new_plan = true;
        }
        this.prev_task_list_size = task_list.size();
        return new_plan;
    }
}
