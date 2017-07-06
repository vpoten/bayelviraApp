package elvira.potential;

import java.io.PrintWriter;
import java.util.Vector;

import elvira.Configuration;
import elvira.Node;

public class ConstraintTable extends ConstraintArc {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6656900814351433345L;
	PotentialTable values;
	
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
