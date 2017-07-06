package elvira.decisionTrees;

import java.util.*;
import elvira.Configuration;
import elvira.Node;

/**
 * Implements the methods for operating chance nodes in a decision tree
 * 
 * It plays the 'CompositeConcrete' role of the 'Composite' design pattern
 *  
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public class ChanceNode extends AbstractCompositeNode {
	/**
	 * Store if this chance node was previosly calculated, to avoid
	 * replicate the evaluation of its children
	 */
	private boolean calculated= false;
	
	/**
	 * Result of previous evaluation. It has sense only if calculated attribute is true
	 */
	private double average= 0.0;

	/**
	 * Store the chance probability of every child of this node
	 */
	private HashMap<AbstractNode,Double> mapNodeChance= new HashMap<AbstractNode,Double>();
	
	/**
	 * @param node
	 * @param configuration
	 * 
	 * @see elvira.Node
	 * @see elvira.Configuration
	 */
	public ChanceNode(Node node, Configuration configuration) {
		super(node, configuration);
	}

	/**
	 * Por si en un futuro interesa incluir la posibilidad de invalidar el valor calculado
	 * para forzar el recálculo del árbol ante un cambio de la estructura de nodos 'aguas abajo'
	 */
	public void reset() {
		calculated= false;
	}
	
	/**
	 * @param n
	 * @return
	 * @throws DTBuildingException 
	 */
	public double getChance(AbstractNode nodeDT) throws DTEvaluatingException {
		if( !mapNodeChance.containsKey(nodeDT) ) {
			throw new DTEvaluatingException("No hay ninguna probabilidad asignada al nodo indicado");
		}
		
		return mapNodeChance.get(nodeDT).doubleValue();
	}
	
	/* (non-Javadoc)
	 * @see elvira.pfc.AbstractCompositeNode#add(elvira.pfc.AbstractNode)
	 */
	public void add(AbstractNode nodeDT) {
		add(nodeDT,0.0);
	}	
	
	/**
	 * @param nodeDT
	 * @param prob
	 */
	public void add(AbstractNode nodeDT, double prob) {
		children.add(nodeDT);
		mapNodeChance.put(nodeDT,new Double(prob));
		nodeDT.setParent(this);
	}
	
	/* (non-Javadoc)
	 * @see elvira.pfc.AbstractNode#getUtility()
	 */
	public double getUtility() throws DTEvaluatingException {
		if( children.isEmpty()) {
			throw new DTEvaluatingException("Nodo de azar sin sucesores");
		}
		
		if( calculated ) {
			return average;
		}
		
		average= 0.0;
		for (AbstractNode nodeDT : children) {
			average += nodeDT.getUtility() * mapNodeChance.get( nodeDT );		
		}
		
		calculated= true;
		return average;
	}
}
