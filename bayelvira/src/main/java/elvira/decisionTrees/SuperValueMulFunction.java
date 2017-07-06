package elvira.decisionTrees;

import java.util.Collection;

/**
 * Implements the multiplication operation as a group function
 * It can be associated to a supervalue node
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 * 
 * @see elvira.decisionTrees.SuperValueNode
 */
public class SuperValueMulFunction implements ISuperValueFunction {

	/**
	 * Given the nodes 'N1', 'N2', ... 'Nn', it returns the product 'Utility(N1)*Utility(N2)*...*Utility(Nn)'
	 * 
	 * @return the product of its children utilities 
	 * @throws DTEvaluatingException 
	 * 
	 * @see elvira.decisionTrees.ISuperValueFunction#apply(java.util.Collection)
	 */
	public double apply(Collection<AbstractNode> collection) throws DTEvaluatingException {
		double resultado= 1.0;
		
		// Iterates across the collection, doing the product of its utilities
		for( AbstractNode nodeDT : collection ) {
			resultado *= nodeDT.getUtility();			
		}		

		return resultado;
	}

	/**
	 * Symbol associated to the multiplication of utilities
	 * 
	 * @see elvira.decisionTrees.ISuperValueFunction#getSymbol()
	 */
	public String getSymbol() {
		// Meter en el bundle
		return "*";
	}	
}
