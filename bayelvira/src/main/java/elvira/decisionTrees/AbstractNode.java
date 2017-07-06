package elvira.decisionTrees;

import elvira.Node;
import elvira.Configuration;

/**
 * This class provides the basic methods to access the variable
 * and/or the configuration of a node of the decision tree
 * 
 * Implements the common structure to every kind of a decision
 * tree node (chance, decision, supervalue or utility)
 *
 * Implements the interface for the recursive calculation of
 * the decision tree thru the abstract definition of the
 * method <code>getUtility</code>
 * 
 * It plays the 'AbstractComponent' rol in the 'Composite' design pattern
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 *
 */
public abstract class AbstractNode {
	/**
	 * Variable from the Influence Diagram that creates this node
	 * 
	 * @see elvira.Node
	 */
	private Node variable;
	
	/**
	 * Configuration of variables associated to this node and (probably)
	 * its parents in the decision tree created
	 * 
	 * @see elvira.Configuration
	 */
	private Configuration configuration;
	
	/**
	 * Link with its parent node in the decision tree
	 * 
	 * NOTA: si finalmente se implanta algún tipo de coalescencia
	 * tendrá que cambiarse este enlace y la forma de acceder de los
	 * nodos hacia sus padres
	 */
	private AbstractNode parent;
	
	/**
	 * @param variable variable/node from the influence diagram
	 * @param configuration potential configuration of this node
	 * 
	 * @see elvira.Node
	 * @see elvira.Configuration
	 */
	public AbstractNode(Node variable, Configuration configuration) {
		setVariable(variable);
		setConfiguration(configuration);
	}

	/**
	 * @return utility of the node
	 * @throws DTEvaluatingException 
	 */
	public abstract double getUtility() throws DTEvaluatingException;
	
	/**
	 * Setter method for the 'variable' attribute
	 * @param variable variable from the influence diagram
	 * 
	 * @see elvira.Node
	 */
	public void setVariable(Node variable) {
		this.variable= variable;
	}
	
	/**
	 * Getter method for the 'variable' attribute
	 * 
	 * @return variable from the influence diagram that creates this node
	 * 
	 * @see elvira.Node
	 */
	public Node getVariable() {
		return variable;
	}
	
	/**
	 * @param nodeDT Update the parent of this node in the decision tree
	 */
	public void setParent(AbstractNode nodeDT) {
		parent= nodeDT;
	}

	/**
	 * @param configuration
	 * 
	 * @see elvira.Configuration
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration= configuration;
	}
	
	/**
	 * @return
	 * 
	 * @see elvira.Configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
	
	/**
	 * @return
	 */
	public AbstractNode getParent() {
		return parent;
	}
}
