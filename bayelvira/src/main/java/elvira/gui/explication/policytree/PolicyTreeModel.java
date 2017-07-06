package elvira.gui.explication.policytree;

import javax.swing.tree.TreeModel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import elvira.learning.policies.Rule;
import elvira.learning.policies.RuleNode;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 * Esta clase crea la estructura de presentación que tendrá el árbol en el JTree.
 * Crea un modelo de presentación a partir de la estructura del DT sin alterar
 * este último
 *  
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public class PolicyTreeModel implements TreeModel {
	/** the root node of the decision tree */
	//protected AbstractCompositeNodePT decisionTree;
	protected Rule policyTree;
	
	/** Listeners */
	private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();
	
	/** Pool de asociaciones entre nodos y sus cuadros resumen */
	protected HashMap<RuleNode,ValueBox> cuadros= new HashMap<RuleNode,ValueBox>();
	
	/** Store the nodes of the decision tree saved by depth level */
	private LevelProxyPT levelProxy;
	
	/** Getter for levelProxy attribute
	 * 
	 * @return the object that stores all decision tree nodes by depth level
	 */
	public LevelProxyPT getLevelProxy() {
		return levelProxy;
	}
	
	/** Build the model with the info that will be displayed in the tree
	 * It decorates de original decision tree adding summary boxes to the nodes
	 * 
	 * @param decisionTree el arbol de decision base
	 */
	public PolicyTreeModel(Rule policyTree) {
		this.policyTree= policyTree;
		levelProxy= new LevelProxyPT(this);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	/*public Object getRoot() {
		RuleNode ruleRoot;
		
		ruleRoot = policyTree.getRoot();
		
		
		// La raiz del árbol será el cuadro de resumen del nodo raiz
		ValueBox cuadro= cuadros.get(ruleRoot);
		
		// Crear el cuadro resumen asociado si áun no existe
		if( cuadro == null ) {
			cuadro= new ValueBox(ruleRoot);
			cuadros.put(ruleRoot,cuadro);
		}
		
		return cuadro;
	}
	*/
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	public Object getRoot() {
		RuleNode ruleRoot;
		
		ruleRoot = policyTree.getRoot();
		
		return ruleRoot;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent) {
		if( isLeaf(parent) ) {
			return 0;
		}
		
		// Los cuadros resumen solo tienen 1 hijo: su nodo asociado 
		if( parent instanceof ValueBox ) {
			return 1;
		}
		
		// Sino, el nº de hijos que tenga en la estructura del DT
		return ((RuleNode)parent).getSize();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object node) {
		// Sólo los nodos de utilidad pueden ser hojas
		//return ((RuleNode)node).getSize()==0;
		boolean isLeaf;
		
		if (node instanceof ValueBox){
			isLeaf =(((ValueBox)node).getSource().getSize()==0); 
		}
		else if (node instanceof RuleNode){
			isLeaf = (((RuleNode)node).getSize()==0);
		}
		else{
			isLeaf = false; 
		}
		return isLeaf;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.addElement(l);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.removeElement(l);
	}
		
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index) {
		// Un cuadro resumen solo tiene un hijo, que es su nodo asociado
		if( parent instanceof ValueBox ) {
			// ... por lo tanto, si el indice no es cero hay un error 
			if (index !=0) {
				// DONE: ¿es esta la excepcion que se deberia lanzar?
			    throw new ArrayIndexOutOfBoundsException("node has no children");
			}
			
			return ((ValueBox) parent).getSource();
		}
		else{
			RuleNode auxParent = ((RuleNode) parent);
			RuleNode hijo;
			
			if (!(auxParent.isRuleForDecisionOfPolicy())){
				hijo= auxParent.getChild(index);
				
			
			}
			else{
				/*Enumeration<RuleNode> auxChildren = auxParent.getChildren().elements();
				
				hijo = auxChildren.nextElement();*/
				hijo = auxParent.getChild(0);
				
			}

						// Se coge el cuadro resumen asociado al hijo del nodo
			ValueBox cuadro= cuadros.get(hijo);
			if( cuadro == null ) {
				cuadro= new ValueBox(hijo);
				cuadros.put(hijo,cuadro);
			}
			return cuadro;
			
		}
		
				

	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child) {
		// casos triviales
		if( parent==null || child==null ) {
			return -1;
		}
		
		// Los Cuadros de resumen solo pueden tener 1 hijo q sera de tipo nodo
		if( parent instanceof ValueBox ) {
			return ((ValueBox)parent).getSource()==child ? 0 : -1;
		}
		
		// Se busca si existe la referencia entre los hijos del nodo
		for( int i=0; i< ((RuleNode)parent).getSize();i++ ) {
			RuleNode hijo= ((RuleNode) parent).getChild(i);
			
		
				// Si no es un nodo supervalor, sus hijos en el JTree tienen
				// que ser obligatoriamente cuadros de resumen
				ValueBox cuadro= cuadros.get(hijo);
	
				// ... 'child' sera el hijo de 'parent' si es el nodo asociado a dicho cuadro
				if( cuadro.getSource() == child ) {
					return i;
				}
		
		}
		
		return -1;
	}

	/** Este metodo avisa a todos los objetos registrados como listener de que
	 * el nodo indicado ha cambiado
	 * 
	 * @param node el nodo que ha cambiado
	 */
	public void fireNodesChanged(Object node) {
		// Vector con todos los caminos posibles del nodo a la raiz
        Vector<Object []> nodosModificados= getPathsToRoot(node);
        
              
        // Se recorre cada path modificado informado a cada uno de los listener inscritos
        for (Object []nodes : nodosModificados) {
            TreeModelEvent e= new TreeModelEvent(this, nodes);
            
            for (TreeModelListener listener : treeModelListeners) {
            	listener.treeNodesChanged(e);
            }
        }
	}	
	
	/** Este metodo avisa a todos los objetos registrados como listener de que todos los nodos han cambiado
	 * DONE: ¿no se actualizan los nodos de utilidad? ¿solo los cuadros resumen? 
	 */
	public void fireAllNodesChanged() {
		// Se recorren todos los nodos del arbol informando del cambio 
		for (HashSet<Object> nivel : levelProxy.objectsInLevel) {
			for (Object o : nivel) {
				fireNodesChanged(o);
			}
		}
	}
	
	/** Devuelve un vector con todos los path ascendentes que puede tener un nodo
	 * Normalmente solo tendra un path, pero de esta forma se deja abierta la posibilidad
	 * de manejar una futura implementación de coalescencia en las ramas del árbol
	 * 
	 * @param node nodo del que se quieren calcular sus paths
	 * @return vector con todos los paths existentes en el arbol
	 */
	protected Vector<Object []> getPathsToRoot(Object node) {
		Vector<Object []> paths= new Vector<Object []>();
		findPathsToRoot(node, new Vector<Object>(), paths);
		return paths;
	}
	
	/**
	 * @param node
	 * @param pathActual
	 * @param paths
	 *
	 * NOTA: si finalmente se implementa coalescencia, será necesario modificar
	 * este método para que sea recursivo y calcule todos los paths posibles
	 * del nodo hasta la raiz del arbol de decision
	 * 
	 * DONE: tratamiento de excepciones
	 */
	protected void findPathsToRoot(Object node, Vector<Object> pathActual, Vector<Object []> paths) {
		// Se añade el objeto al inicio del vector
		pathActual.add(0,node);
		
		// hasta que se llegue a la raiz del arbol
		while( node != getRoot() ) {
			if( node instanceof ValueBox ) {
				// Si es un cuadro resumen, es necesario saltar al padre del nodo resumido 
				node= ((ValueBox) node).getSource().getParent();
			}
			else {
				// Si no es un cuadro resumen, se coge el cuadro resumen asociado
				ValueBox r= cuadros.get(node);
				if( r!= null ) {
					node= r;
				}
				else if( node instanceof RuleNode ){
					/* Los padres de los nodos de utilidad son nodos supervalor, de ahi que se
					 * tenga que controlar que un nodo no tenga cuadro resumen asociado
					 */
					node= ((RuleNode) node).getParent();
				}
				else {
					throw new RuntimeException("Error buscando paths");
				}
			}
			
			// Se añade el padre del nodo al path actual
			pathActual.add(0,node);
		}
		
		// Se añade a la lista de paths el nuevo path encontrado
		paths.add(pathActual.toArray());
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 * 
	 * NOTA: ¿Puede ser de alguna utilidad implementar este metodo? hasta ahora no ha sido necesario
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException();
	}
}
