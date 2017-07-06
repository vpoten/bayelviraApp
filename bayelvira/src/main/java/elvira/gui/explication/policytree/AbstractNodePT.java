package elvira.gui.explication.policytree;

import elvira.Node;
import elvira.Configuration;
import elvira.decisionTrees.AbstractNode;

/**
 * This class provides the basic methods to access the variable
 * and/or the configuration of a node of the policy tree
 * 
 * It implements the common structure to every kind of a decision
 * tree node (chance, decision or leaf)
 *
 * 
 * It plays the 'AbstractComponent' rol in the 'Composite' design pattern
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 *
 */
public abstract class AbstractNodePT extends AbstractNode {
	public AbstractNodePT(Node variable, Configuration configuration) {
		super(variable, configuration);
		// TODO Auto-generated constructor stub
	}
}