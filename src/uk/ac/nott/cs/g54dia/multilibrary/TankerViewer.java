package uk.ac.nott.cs.g54dia.multilibrary;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

import javax.swing.*;

/**
 * A simple user interface for watching an individual Tanker.
 * 
 * @author Neil Madden.
 */
/*
 * Copyright (c) 2003 Stuart Reeves Copyright (c) 2003-2005 Neil Madden
 * (nem@cs.nott.ac.uk). Copyright (c) 2011 Julian Zappala (jxz@cs.nott.ac.uk).
 * See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class TankerViewer extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2810783821678793885L;
	TankerViewerIconFactory iconfactory;
	JLabel[][] cells;
	JLabel[][] tankers;
	JLabel tstep, fuel, pos, water, completed, delivered, score;
	Tanker tank;
	Fleet fleet;
	final static int SIZE = Tanker.VIEW_RANGE * 2 + 1;
	JLayeredPane lp;
	JComboBox tankerList;
	JPanel infop;

	public TankerViewer(Tanker Tanker) {
		this(Tanker, new DefaultTankerViewerIconFactory());
	}

	public TankerViewer(Tanker Tanker, TankerViewerIconFactory fac) {
		this.tank = Tanker;
		this.iconfactory = fac;
		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		// Create the cell viewer
		cells = new JLabel[SIZE][SIZE];
		tankers = new JLabel[SIZE][SIZE];
		lp = new JLayeredPane();
		JPanel pCells = new JPanel(new GridLayout(SIZE, SIZE));
		JPanel pTankers = new JPanel(new GridLayout(SIZE, SIZE));
		pCells.setBackground(Color.GREEN);
		pTankers.setOpaque(false);

		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				cells[x][y] = new JLabel();
				tankers[x][y] = new JLabel();
				tankers[x][y].setIcon(iconfactory.getIconForTanker(Tanker));
				tankers[x][y].setVisible(false);
				pCells.add(cells[x][y]);
				pTankers.add(tankers[x][y]);
			}
		}

		lp.add(pTankers, new Integer(1));
		
		
		lp.add(pCells, new Integer(0));
		
		pCells.setBounds(0, 0, 600, 600);
		pTankers.setBounds(0, 0, 600, 600);

		c.add(lp, BorderLayout.CENTER);

		// Create some labels to show info about the Tanker and environment
		infop = new JPanel(new GridLayout(4, 4));
		infop.add(new JLabel("Timestep:"));
		tstep = new JLabel("0");
		infop.add(tstep);
		infop.add(new JLabel("Fuel:"));
		fuel = new JLabel("200");
		infop.add(fuel);
		infop.add(new JLabel("Position:"));
		pos = new JLabel("(0,0)");
		infop.add(pos);
		infop.add(new JLabel("Water:"));
		water = new JLabel("0");
		infop.add(water);

		infop.add(new JLabel("Completed:"));
		completed = new JLabel("0");
		infop.add(completed);

		infop.add(new JLabel("Delivered:"));
		delivered = new JLabel("0");
		infop.add(delivered);

		infop.add(new JLabel("Overall Score:"));
		score = new JLabel("0");
		infop.add(score);

		c.add(infop, BorderLayout.SOUTH);
		// infop.setPreferredSize(new Dimension(200,100));

		setSize(600, 725);
		setTitle("Tanker Viewer");
		setVisible(true);

	}

	public TankerViewer(Fleet fleet) {
		this(fleet.get(0));
		this.fleet = fleet;

		String[] tankerNames = new String[fleet.size()];

		for (int i = 0; i < fleet.size(); i++) {
			tankerNames[i] = "Tanker " + i;
		}

		//A drop down list to select which tanker to view
		tankerList = new JComboBox(tankerNames);
		infop.add(tankerList);

		//Event handler for drop down list
		tankerList.addActionListener(this);

	}

	public void setTanker(Tanker t) {
		this.tank = t;
	}

    static public String customFormat(String pattern, double value ) {
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        String output = myFormatter.format(value);
        return output;
    }

	public void tick(Environment env) {
		Cell[][] view = env.getView(tank.getPosition(), Tanker.VIEW_RANGE);
		pos.setText(tank.getPosition().toString());
		tstep.setText(new String("" + env.getTimestep()));
		water.setText(new String("" + tank.waterLevel));
        fuel.setText(new String("" + tank.getFuelLevel()));
		completed.setText(new String("" + tank.getCompletedCount()));
		delivered.setText("" + tank.waterDelivered);
		score.setText(customFormat("###,###,###,###,###,###", tank.getScore()));
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				Icon cur = iconfactory.getIconForCell(view[x][y]);
				cells[x][y].setIcon(cur);
				tankers[x][y].setVisible(false);

				// Now Draw Tankers
				for (Tanker t : fleet) {
					if (view[x][y].getPoint().equals(t.getPosition())) {
						tankers[x][y].setVisible(true);
					}

				}
			}
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		tank = fleet.get(tankerList.getSelectedIndex());

	}
}
