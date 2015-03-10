package uk.ac.nott.cs.g54dia.multilibrary;

import java.util.*;

public class Fleet extends ArrayList<Tanker>{

	private static final long serialVersionUID = 8031611383212571139L;

	public long getScore() {
		int delivered = 0;
		int completed = 0;
		
		for (Tanker t:this) {
			delivered += t.waterDelivered;
			completed += t.completedCount;
		}
		
		return (long)delivered * (long)completed;
	}
}
