package elvira.decisionTrees;

import java.util.Vector;
import elvira.Configuration;
import elvira.Node;

/**
 * This abstract class provides the basic methods to operate
 * with nodes that must have children in the decision tree
 * (chance, decision or supervalue nodes) 
 * <p>
 * It plays the 'AbstractComposite' role of the 'Composite' design pattern
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1, 02/10/2005
 */
public abstract class AbstractCompositeNode extends AbstractNode {
	/**
	 * Container of the node's children
	 */
    protected Vector<AbstractNode> children= new Vector<AbstractNode>();
	
	/**
	 * Initialize the class: unnecessary at the moment
	 * (only calls its ancestor to initialize)
	 * 
	 * @param n Node of the influence diagram
	 * @param ccc Node's configuration in the potential 
	 */
	public AbstractCompositeNode(Node node, Configuration configuration) {
		super(node,configuration);
	}        

	/**
	 * Add a new child to this node
	 * 
	 * @param nodeDT child to add
	 * @throws DTBuildingException 
	 */
	public void add(AbstractNode nodeDT) throws DTBuildingException {
		// 07/01/2006 control del problema con ¿asimetrias? 
		if(nodeDT==null) {
			throw new DTBuildingException("No se puede añadir un hijo null");
		}
		else if(nodeDT instanceof AbstractCompositeNode) {
			if( ((AbstractCompositeNode)nodeDT).children.isEmpty() ) {
				throw new DTBuildingException("No se puede añadir un composite sin hijos");				
			}
		}
		
		children.add(nodeDT);
		
		// NOTA: review this if coalescence is finally implemented
		nodeDT.setParent(this);
	}
	
	/**
	 * Get the n-th child of this composite node
	 * No range check of the index is done
	 *  
	 * @param k child index
	 * @return the child of this node at that index
	 */
	public AbstractNode getChild(int index) {
		return children.elementAt(index);
	}
	
	/**
	 * Por si en un futuro interesa incluir la posibilidad de invalidar el valor calculado
	 * para forzar el recálculo del árbol ante un cambio de la estructura de nodos 'aguas abajo'
	 */
	public abstract void reset();

	/**
	 * Get the cardinality of this node
	 * 
	 * @return the number of children of this node
	 */
	public int getSize() {
		return children.size();
	}	
}
