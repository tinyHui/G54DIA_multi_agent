package Solution;

import Solution.Planner.Manager;
import uk.ac.nott.cs.g54dia.multilibrary.*;

/**
 * An example of how to simulate execution of a tanker agent in the sample (task) environment.
 * <p>
 * Creates a default {@link uk.ac.nott.cs.g54dia.multilibrary.Environment}, a {@link uk.ac.nott.cs.g54dia.multidemo.DemoTanker} and a GUI window
 * (a {@link uk.ac.nott.cs.g54dia.multilibrary.TankerViewer}) and executes the Tanker for DURATION days in the environment.
 *
 * @author Julian Zappala
 */

/*
 * Copyright (c) 2005 Neil Madden.
 * Copyright (c) 2011 Julian Zappala (jxz@cs.nott.ac.uk)
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */

public class Simulator {

    /**
     * Time for which execution pauses so that GUI can update.
     * Reducing this value causes the simulation to run faster.
     */
    private static int DELAY = 0;


    private static int FLEET_SIZE = 4;


    /**
     * Number of timesteps to execute
     */
    private static int DURATION = 10 * 10000;

    public static void main(String[] args) {
        //For trapping fatal errors
        Boolean fatalError = false;

        // Create an environment
        Environment env = new Environment((Tanker.MAX_FUEL/2)-5);

        //Create a fleet
        Fleet fleet = new Fleet();
        MemMap map = new MemMap();
        TaskSys ts = new TaskSys();
        Manager manager = new Manager(map);
        Tanker t;

        for (int i=0; i<FLEET_SIZE; i++) {
            t = new SmartTanker(map, ts, i);
            fleet.add(t);
        }

        // Create a GUI window to show our tanker
        TankerViewer tv = new TankerViewer(fleet);
        tv.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        // Start executing the Tanker
        while (env.getTimestep()<(DURATION)) {
            // Advance the environment timestep
            env.tick();
            // Update the GUI
            tv.tick(env);

            for (Tanker tank:fleet) {
                // Get the current view of the tanker
                Cell[][] view = env.getView(tank.getPosition(), Tanker.VIEW_RANGE);
                // Let the tanker choose an action
                Action a = tank.senseAndAct(view, env.getTimestep());
                // Try to execute the action
                try {
                    a.execute(env, tank);
                } catch (OutOfFuelException dte) {
                    System.out.println("Tanker out of fuel!");
                    fatalError = true;
                    break;
                } catch (ActionFailedException afe) {
                    System.err.println("Failed: " + afe.getMessage());
                }
            }

            if (fatalError) {
                break;
            }

            try { Thread.sleep(DELAY);} catch (Exception e) { }


        }
    }


}

