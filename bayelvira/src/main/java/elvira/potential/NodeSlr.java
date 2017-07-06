package elvira.potential;


import java.util.Vector;
import java.util.Hashtable;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.FocalSet;

/**
 * This class implements a node of a semi-lattice
 * representation (Slr) of a Dempster-Shafer belief
 * fucntion. Each node is a focal set and a real value.
 *
 * @author Antonio.Salmeron@ual.es
 * @since 20/5/2002
 */

public class NodeSlr {
  
/**
 * The focal set to which this node is referred.
 */
FocalSet focal;

/**
 * The value for the belief function (usually a b.p.a.)
 * for the focal set previously declared.
 */
double value;

/**
 * The set of children of this node.
 */
Vector children;


/**
 * Creates an empty object.
 */

public NodeSlr() {
 
  focal = new FocalSet();
  children = new Vector();
  value = 0.0;
}


/**
 * Creates a node with a given focal set and its value.
 *
 * @param f a focal set.
 * @param x a real number.
 */

public NodeSlr(FocalSet f, double x) {
 
  focal = f;
  value = x;
  children = new Vector();
}


/**
 * Sets the value of this node.
 * 
 * @param x a double value.
 */

public void setValue(double x) {
  
  value = x;
}


/**
 * Gets the value of this node.
 * 
 * @return a double value.
 */

public double getValue() {
  
  return (value);
}


/**
 * Gets the size of the focal set included in this node.
 *
 * @return the size of the focal set.
 */

public int getSize() {
  
  return focal.getSize();
}


/**
 * Sets the focal set of this node.
 * 
 * @param f a focal set.
 */

public void setFocal(FocalSet f) {
  
  focal = f;
}


/**
 * Gets the focal set of this node.
 * 
 * @return a focal set.
 */

public FocalSet getFocal() {
  
  return (focal);
}
 
 
/**
 * Adds a node to the list of children of this.
 *
 * @param node the node to add.
 */

public void addChild(NodeSlr n) {

  children.addElement(n);
}


/**
 * Removes the child at a given position. The object is modified.
 *
 * @param pos the position of the child to remove.
 */

public void removeChildAt(int pos) {
 
  children.removeElementAt(pos);
}


/**
 * Gets the node at a given position in the list of
 * children.
 * @param pos a position in the list of children.
 * @return the node at position <code>pos</code>
 */

public NodeSlr getChild(int pos) {
 
  if (pos > children.size()-1) {
    System.out.println("Error in NodeSlr: not so many children");
    System.exit(1);
  }
  
  return ((NodeSlr)children.elementAt(pos));
}


/**
 * Gets the number of children of this node.
 *
 * @return the number of children.
 */

public int getNumberOfChildren() {
  
  return children.size();
}

} // End of class NodeSlr