package elvira.decisionTrees;

import elvira.Configuration;
import elvira.Node;
import elvira.potential.Potential;

/**
 * Implements the structure and methods of an utility node, basically
 * it calculates what value given a configuration and a potential for
 * the variable in the influence diagram 
 * 
 * It plays the 'ConcreteComponent' role in the 'Composite' design pattern
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public class UtilityNode extends AbstractNode {
	/**
	 * Calculted utility for this leaf node
	 */
	private double utility;
	
	/**
	 * Assigned potential of the variable in this node
	 */
	protected Potential potential;
	
	/**
	 * @param node DI's node associated to this DT's node
	 * @param potential potential of the DI's node
	 * @param configuration configuration of the DI's node for the DT's node
	 */
	public UtilityNode(Node node, Potential potential, Configuration configuration) {
		super(node,configuration);
		this.potential= potential;
		setUtility( potential.getValue(configuration) );
	}

	/**
	 * @param utility stores the given utility in this decision tree node
	 */
	public void setUtility(double utility) {
		this.utility= utility;
	}
	
	/*
	 * Retrieve the utility of this decision tree node 
	 * @see elvira.pfc.AbstractNode#getUtility()
	 */
	public double getUtility() {
		return utility;
	}
}
