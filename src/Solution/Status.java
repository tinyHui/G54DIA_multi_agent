package Solution;

import uk.ac.nott.cs.g54dia.multilibrary.Cell;

/**
 * Created by JasonChen on 2/27/15.
 */
public class Status {
    public static int DURATION = 10 * 10000;

    public int completed_count = 0;
    public int delivered_water = 0;
    public long time_remain = DURATION;

    public Cell current_cell;
    public MemPoint current_point;
    public boolean obeying = false;
}
