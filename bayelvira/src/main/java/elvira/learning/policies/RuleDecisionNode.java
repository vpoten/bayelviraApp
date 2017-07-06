package elvira.learning.policies;

import java.util.ArrayList;
import java.util.Iterator;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.Node;
import elvira.potential.DeterministicPotentialTable;

/**
 * Implements the methods for operating decision nodes in a decision tree
 * 
 * It plays the 'CompositeConcrete' role of the 'Composite' design pattern
 *  
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 *
 */
public class RuleDecisionNode extends RuleNode {
	/**
	 * This attribute references the branch with the best decision
	 */
	//private AbstractNode bestDecision=null;
	
	/**
	 * @param node
	 * @param configuration
	 */
	public RuleDecisionNode(Node node, Configuration configuration) {
		super(node, configuration);
	}

	
	public RuleDecisionNode() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Getter for 'bestDecision' attribute
	 * 
	 * @return the node in the best branch
	 * Will return null if the node is unevaluated
	 */
	/*public AbstractNode getBestDecision() {
		return bestDecision;
	}*/
	
	/**
	 * Por si en un futuro interesa incluir la posibilidad de invalidar el valor calculado
	 * para forzar el recálculo del árbol ante un cambio de la estructura de nodos 'aguas abajo'
	 */
	/*public void reset() {
		bestDecision= null;
	}*/
	
	/* (non-Javadoc)
	 * @see elvira.pfc.AbstractNode#getUtility()
	 */
	/*public double getUtility() throws PTEvaluatingException {
		if( children.isEmpty()) {
			throw new PTEvaluatingException("Nodo de utilidad sin sucesores");
		}
		
		// Si ya está precalculada la utilidad de la mejor decision, tomarla 
		if( bestDecision!=null ) {
			return bestDecision.getUtility();
		}
		
		// Se recorre cada rama que parte de este nodo buscando la mejor decision
		Iterator<AbstractNode> it= children.iterator();
		bestDecision= it.next();
		
		while (it.hasNext()) {
			AbstractNode n= it.next();
			if( n.getUtility() > bestDecision.getUtility() ) {
				bestDecision= n;
			}
		}
		
		return bestDecision.getUtility();
	}*/
}
