package elvira.potential;


import java.util.Vector;
import java.util.Hashtable;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.FocalSet;


/**
 * This class implements a semi-lattice representation (Slr) of
 * a series of focal sets of a Dempster-Shafer belief
 * fucntion and the real value assigned to each one of them.
 * The semi-lattice representation is described in tech. rep.
 * "Hugin architecture for propagating belief functions"
 * by A. Salmeron and F.V. Jensen (1997), available at
 * www.ual.es/personal/asalmero
 *
 * The focal sets are ordered with respect to set inclusion.
 *
 * @author Antonio.Salmeron@ual.es
 * @since 22/5/2002
 */

public class Slr {
  
/**
 * The list of nodes in the Slr. The elements in this vector
 * are  objects of class <code>NodeSlr</code>.
 */
Vector nodes;

/**
 * The list of root nodes of the Slr. This is a subset of
 * <code>nodes</code>.
 */
Vector roots;

/**
 * The number of nodes in the Slr. This is equal to the size of
 * vector <code>nodes</code>.
 */
int size;


/**
 * Creates an empty object.
 */

public Slr() {

  size = 0;
  nodes = new Vector();
  roots = new Vector();
}


/**
 * Inserts a node in the Slr. The object is modified.
 *
 * @param node the node to insert.
 */

public void insert(NodeSlr node) {
 
  Vector temp, h;
  NodeSlr n, child;
  FocalSet focal;
  int i, j, s;
  boolean finish = false, delete;
  
  temp = new Vector();
  for (i=0 ; i<roots.size() ; i++) {
    n = (NodeSlr)roots.elementAt(i);
    focal = n.getFocal();
    if (node.getFocal().isSubset(focal)) {
      temp.addElement(n);
      if (n.getSize() == node.getSize()) {
	// If the node is already contained, just increase
	// its mass and return.
	n.setValue(n.getValue()+node.getValue());
	return;
      }
    }
  }
  
  // Now we check whether or not node is a root.
  if (temp.size() == 0) {
    roots.addElement(node);
    nodes.addElement(node);
    size++;
    return;
  }
  
  // After this step, <code>temp</code> contains the
  // root nodes that are subsets of the node to insert.
  
  do {
    h = new Vector();
    s = temp.size();
    for (i=(s-1) ; i>=0 ; i--) {
      n = (NodeSlr)temp.elementAt(i);
      delete = false;
      for (j=0 ; j<n.getNumberOfChildren() ; j++) {
	child = n.getChild(j);
	focal = child.getFocal();
	if (node.getFocal().isSubset(focal)) {
	  if (node.getSize() == child.getSize()) {
	    // If the node is already contained, just increase
	    // its mass and return.
	    child.setValue(child.getValue()+node.getValue());
	    return;
	  }
	  h.addElement(child);
	  delete = true;
	}
      }
      
      if (delete)
	temp.removeElementAt(i);
    }
    
    if (h.size() > 0) {
      for (i=0 ; i<h.size() ; i++) {
	temp.addElement(h.elementAt(i));
      }
    }
    else {
      finish = true;
    }
  } while (!finish);
  
  // Now, add the node to the Slr and connect it appropriately.
  
  nodes.addElement(node);
  size++;
  
  for (i=0 ; i<temp.size() ; i++) {
    n = (NodeSlr)temp.elementAt(i);
    s = n.getNumberOfChildren();
    for (j=(s-1) ; j>=0 ; j--) {
      child = n.getChild(j);
      if (child.getFocal().isSubset(node.getFocal())) {
	n.removeChildAt(j);
	node.addChild(child);
      }
    }
  }  
}


/**
 * Gets the number of nodes in this Slr.
 *
 * @return the size of this slr.
 */

public int getSize() {
  
  return (size);
}


/**
 * Retrieves an element from the slr.
 *
 * @param pos the position of the element to retrieve.
 * @return the element at position <code>pos</code>.
 */

public NodeSlr elementAt(int pos) {
  
  if (pos > (nodes.size()-1)) {
    System.out.println("Error in Slr.elementAt(): index out of bounds\n");
    System.exit(1);
  }
  
  return ((NodeSlr)nodes.elementAt(pos));
}


/**
 * Combines two slrs. The combination is carried out
 * according to Dempster's rule, and assuming that the
 * slrs represent mass functions.
 *
 * @param factor the slr to combine with this.
 * @return a new slr with the result of combining this object
 * with <code>factor</code>
 */

public Slr combine(Slr factor) {
 
  int i, j;
  NodeSlr n1, n2, newNode;
  FocalSet f1, f2, intersect;
  Slr combination = new Slr();
  
  for (i=0 ; i<getSize() ; i++) {
    n1 = elementAt(i);
    for (j=0 ; j<factor.getSize() ; j++) {
      n2 = factor.elementAt(j);
      intersect = n1.getFocal().intersection(n2.getFocal());
      newNode = new NodeSlr(intersect,n1.getValue()*n2.getValue());
      combination.insert(newNode);
    }
  }
  
  return (combination);
}


/**
 * Marginalises this Slr over a given set of variables.
 *
 * @param var a set of variables over which the marginal
 * will be computed.
 * @return the marginalised Slr.
 */

public Slr marginal(Vector var) {
  
  int i;
  NodeSlr n, newNode;
  Slr marg = new Slr();
  
  for (i=0 ; i<getSize() ; i++) {
    n = elementAt(i);
    newNode = new NodeSlr(n.getFocal().projection(var),n.getValue());
    marg.insert(newNode);
  } 

  return (marg);
}


} // End of class Slr