package uk.ac.nott.cs.g54dia.multidemo;
import uk.ac.nott.cs.g54dia.multilibrary.*;
/**
 * A simple example tanker that chooses actions.
 * 
 * @author Julian Zappala
 */
/*
 * 
 * Copyright (c) 2011 Julian Zappala
 * 
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class DemoTanker extends Tanker {
	
    public DemoTanker() { }


    
   
    

    /*
     * The following is a very simple demonstration of how to write a tanker. The
     * code below is very stupid and pretty much randomly picks actions to
     * perform. You can use this file as a template for writing your own tanker,
     * but DO NOT USE THIS CODE. That would be plagiarism.
     */
    public Action senseAndAct(Cell[][] view, long timestep) {
   
      	
    	// If fuel tank is low and not at the fuel pump then move towards the fuel pump
        if ((getFuelLevel() < (MAX_FUEL/2)+5) && !(getCurrentCell(view) instanceof FuelPump)) {
            return new MoveTowardsAction(FUEL_PUMP_LOCATION);
        }
        else {
            //Random Search
            return new MoveAction((int)(Math.random()*8));       	
        }

     
                
        
    }


}
