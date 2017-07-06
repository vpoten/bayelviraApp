package elvira.potential;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import elvira.Configuration;
import elvira.Link;
import elvira.Node;

/**
 * Implements the constraint associated to a revelation arc
 * @author Manuel Luque Gallego
 * @since 17/3/2008
 * 
 *
 */
public class RevelationArc extends ConstraintArc {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -75518751195316426L;

		
	//Indexes of the relevant states of the node'tail' in 'arc'
	ArrayList<Integer> relevantStates;
	
	
	/**
	 * @return the variable that reveals the values of the other variable
	 */
	public Node getRevealerVariable(){
		return arc.getTail();
	}
	
	
	/**
	 * @return the variable whose values are revealed by the arc
	 */
	public Node getRevealedVariable(){
		return arc.getHead();
	}


	@Override
	public Potential addVariable(Node var) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Potential combine(Potential pot) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public double entropyPotential() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double entropyPotential(Configuration conf) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public long getSize() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getValue(Configuration conf) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Potential marginalizePotential(Vector vars) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void normalize() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Potential restrictVariable(Configuration conf) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void saveResult(PrintWriter p) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setValue(Configuration conf, double val) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public double totalPotential() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double totalPotential(Configuration conf) {
		// TODO Auto-generated method stub
		return 0;
	}
	

}
