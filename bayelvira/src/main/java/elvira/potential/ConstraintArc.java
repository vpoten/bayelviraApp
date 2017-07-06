package elvira.potential;

import elvira.Link;
import elvira.Node;

public abstract class ConstraintArc extends Potential {
	Link arc;
	
	/**
	 * @return the variable that constraints the values of the other variable
	 */
	public Node getConstraintingVariable(){
		return arc.getTail();
	}
	
	/**
	 * @return the variable whose variables are constrained
	 */
	public Node getConstrainedVariable(){
		return arc.getHead();
	}
}
