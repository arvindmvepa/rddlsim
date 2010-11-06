/**
 * RDDL: A simple graphics display for the Sidewalk domain. 
 * 
 * @author Scott Sanner (ssanner@gmail.com)
 * @version 10/10/10
 *
 **/

package rddl.viz;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;

import rddl.EvalException;
import rddl.State;
import rddl.RDDL.LCONST;
import rddl.RDDL.PVARIABLE_DEF;
import rddl.RDDL.PVAR_NAME;
import rddl.RDDL.TYPE_NAME;

public class TrafficDisplay extends StateViz {

	public TrafficDisplay() {
		_nTimeDelay = 200; // in milliseconds
	}

	public TrafficDisplay(int time_delay_per_frame) {
		_nTimeDelay = time_delay_per_frame; // in milliseconds
	}
	
	public boolean _bSuppressNonFluents = true;
	public BlockDisplay _bd = null;
	public int _nTimeDelay = 0;
	
	public void display(State s, int time) {
		System.out.println("TIME = " + time + ": " + getStateDescription(s));
	}

	//////////////////////////////////////////////////////////////////////

	public String getStateDescription(State s) {
		StringBuilder sb = new StringBuilder();

		TYPE_NAME cell_type = new TYPE_NAME("cell");
		ArrayList<LCONST> cells = s._hmObject2Consts.get(cell_type);

		PVAR_NAME occupied = new PVAR_NAME("occupied");
		PVARIABLE_DEF occupied_def = s._hmPVariables.get(occupied);

		if (_bd == null) {
			int max_row = -1;
			int max_col = -1;
			for (LCONST cell : cells) {
				String[] split = cell.toString().split("_");
				int row = new Integer(split[1]);
				int col = new Integer(split[2]);
				if (row > max_row) max_row = row;
				if (col > max_col) max_col = col;
			}
			_bd= new BlockDisplay("RDDL Traffic Simulation", "RDDL Traffic Simulation", max_row + 2, max_col + 2);	
		}
		
		// Set up an arity-1 parameter list
		ArrayList<LCONST> params = new ArrayList<LCONST>(1);
		params.add(null);

		_bd.clearAllCells();
		_bd.clearAllLines();
		for (LCONST cell : cells) {
			
			// Get person info and select a color
			params.set(0, cell);
			
			// Get state values
			boolean occ = (Boolean)s.getPVariableAssign(occupied, params);
			//System.out.println("'" + cell + "'");
			String[] split = cell.toString().split("_");
			int row = new Integer(split[1]);
			int col = new Integer(split[2]);
			Color c = _bd._colors[row % _bd._colors.length];
			System.out.println(occupied + " (" + row + "," + col + ") = " + occ);

			_bd.setCell(row, col, c, occ ? null : ".");
		}
		_bd.repaint();
		
		// Go through all variable types (state, interm, observ, action, nonfluent)
		for (Map.Entry<String,ArrayList<PVAR_NAME>> e : s._hmTypeMap.entrySet()) {
			
			if (_bSuppressNonFluents && e.getKey().equals("nonfluent"))
				continue;
			
			// Go through all variable names p for a variable type
			for (PVAR_NAME p : e.getValue()) 
				try {
					// Go through all term groundings for variable p
					ArrayList<ArrayList<LCONST>> gfluents = s.generateAtoms(p);										
					for (ArrayList<LCONST> gfluent : gfluents)
						sb.append("- " + e.getKey() + ": " + p + 
								(gfluent.size() > 0 ? gfluent : "") + " := " + 
								s.getPVariableAssign(p, gfluent) + "\n");
						
				} catch (EvalException ex) {
					sb.append("- could not retrieve assignment" + s + " for " + p + "\n");
				}
		}
		
		// Sleep so the animation can be viewed at a frame rate of 1000/_nTimeDelay per second
	    try {
			Thread.currentThread().sleep(_nTimeDelay);
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		}
				
		return sb.toString();
	}
	
	public void close() {
		_bd.close();
	}
}

