package Solution;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.multilibrary.MoveAction;

/**
 * Created by JasonChen on 2/15/15.
 */
public class MemPoint implements Cloneable {
    public final static MemPoint FUEL_PUMP = new MemPoint(0, 0);

    volatile int x, y;

    public MemPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object o) {
        MemPoint p = (MemPoint)o;
        if (p==null) return false;
        return (p.x == this.x) && (p.y == this.y);
    }

    public Object clone() {
        return new MemPoint(x,y);
    }

    public int calcDistance(MemPoint p2) {
        if (p2 == null) {
            return 101;
        }
        int dx = Math.abs(this.x - p2.x);
        int dy = Math.abs(this.y - p2.y);
        // coordinate start from 0
        return dx > dy ? dx : dy;
    }

    public int calcDistanceToFuel() {
        return this.calcDistance(FUEL_PUMP);
    }

    public void moveTo(int direction) {
        switch (direction) {
            case MoveAction.NORTHEAST:
                this.x++;
                this.y++;
                break;
            case MoveAction.SOUTHEAST:
                this.x++;
                this.y--;
                break;
            case MoveAction.EAST:
                this.x++;
                break;
            case MoveAction.NORTHWEST:
                this.x--;
                this.y++;
                break;
            case MoveAction.SOUTHWEST:
                this.x--;
                this.y--;
                break;
            case MoveAction.WEST:
                this.x--;
                break;
            case MoveAction.NORTH:
                this.y++;
                break;
            case MoveAction.SOUTH:
                this.y--;
                break;
            default:
                throw new ValueException("Already there");
        }
    }
}
