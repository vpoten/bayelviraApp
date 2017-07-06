package elvira.decisionTrees;

import elvira.Configuration;
import elvira.Node;

/**
 * Implements the basic methods to work with supervalue nodes in a decision tree
 * 
 * It plays the 'CompositeConcrete' role in a 'Composite' design pattern
 *  
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public class SuperValueNode extends AbstractCompositeNode {
	/**
	 * This attribute marks this node if was calculated previously to
	 * avoid recalcs in the nexts calls to 'getUtility' method
	 */
	private boolean calculated= false;
	
	/**
	 * calculated result of a previous call to getUtility() function: it has sense
	 * only if the attribute 'calculated' is true
	 */
	private double result= 0.0;
	
	/**
	 * Group function applicable to this supervalue node
	 * 
	 *  @see elvira.decisionTrees.SuperValueAddFunction
	 *  @see elvira.decisionTrees.SuperValueMulFunction
	 */
	private ISuperValueFunction function;

	/**
	 * @param nodeDI node of the associated ID for this DT node
	 * @param configuration Configuration of variables calculated for this DT node
	 * @param function supervalue function to apply to this node's children
	 */
	public SuperValueNode(Node nodeDI, Configuration configuration, ISuperValueFunction function) {
		super(nodeDI, configuration);
		this.function= function;
	}

	/** 
	 * Por si en un futuro interesa incluir la posibilidad de invalidar el valor calculado
	 * para forzar el recálculo del árbol ante un cambio de la estructura de nodos 'aguas abajo'
	 */
	public void reset() {
		calculated= false;
	}
	
	/**
	 * @return group function of this supervalue node
	 */
	public ISuperValueFunction getFunction() {
		return function;
	}
	
	/*
	 * Calculates the utility for this DT node
	 * 
	 * @see elvira.pfc.AbstractNode#getUtility()
	 */
	public double getUtility() throws DTEvaluatingException {
		if( children.isEmpty()) {
			throw new DTEvaluatingException("NodoSV sin sucesores");
		}
		
		// Use the previously calculated value if available
		if( calculated ) {
			return result;
		}
		
		// The group function is applied to this node's branch
		result= function.apply(children);
		calculated= true;
		
		return result;
	}

}
