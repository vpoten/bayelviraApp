/* NeighbourTree.java */

package elvira.inference.clustering;

import elvira.*;
import java.util.Vector;
import java.io.*;

/**
 * This class implements the basic structure that a node uses to 
 * access to his neighbours. The neighbours of a node in the join tree
 * will be stored as a list of objects of class <code>NeighbourTree</code>.
 * 
 * @since 12/9/2000
 */

public class NeighbourTreeList {
  
/**
 * Contains the list of neighbours of thes node that has associated
 * this list.
 */

Vector neighbourList;


/**
 * Constructor. Creates an empty object of this class.
 */

NeighbourTreeList() {
  
  neighbourList = new Vector();      
}        
   
   
/**
 * This method is used to set the list of neighbours of a node.
 * 
 * @param v The list (<code>Vector</code>) of nodes
 * (<code>NeighbourTree</code>) that will be put asa neighbours
 * of the owner node.
 */

public void setNeighbourList(Vector v) {
  
  neighbourList = v;
}
 
  
/**
 * This method is used for accessing to the list of neighbours.
 * 
 * @return The list of neighbours of this node, as a (<code>Vector</code>)
 * of  (<code>NeighbourTree</code>).
 */
   
public Vector getNeighbourList() {
  
  return neighbourList;
} 
   
   
/**
 * Returns the neighbour stored at a given position.
 * 
 * @param p The position of the neighbour to retrieve.
 * @return The neighbour at position <code>p</code>.
 */

public NeighbourTree elementAt(int p) {
  
  return ( (NeighbourTree) neighbourList.elementAt(p));
}
        
        
/**
 * Inserts a neighbour at the end of the list of neighbours
 * 
 * @param n The neighbour to insert.
 */

public void insertNeighbour(NeighbourTree n) {
  
  neighbourList.addElement(n);
}
   
   
/**
 * Removes a neighbour from the list.
 * 
 * @param n the neighbour to remove.
 */

public void removeNeighbour (NeighbourTree n) {
  
  neighbourList.removeElement(n);
}
   
   
/**
 * This method returns the position of a neighbour in the list.
 * 
 * @param n the neighbour to locate.
 * @return The position of <code>n</code> in the list or -1 if
 * <code>n</code> is not in it.
 */

public int indexOf(NeighbourTree n) {
  
  return neighbourList.indexOf(n);
}


/**
 * Returns the position of a node in the list of neighbours.
 * @param n a <code>NodeJoinTree</code>.
 * @return the position of node <code>n</code> in the list of neighbours.
 */

public int indexOf(NodeJoinTree n) {

  int i, s;
  NeighbourTree nt;
  
  s = neighbourList.size();
  
  for (i=0 ; i<s ; i++) {
    nt = elementAt(i);
    if (nt.getNeighbour() == n)
      return i;
  }
  return (-1);
}


/**
 * Returns the position of a node in the list of neighbours,
 * where nodes are identified by labels.
 * @param n the label of the node  to search.
 * @return the position of node <code>n</code> in the list of neighbours.
 */

public int indexOf(int n) {

  int i, s;
  NeighbourTree nt;
  
  s = neighbourList.size();
  
  for (i=0 ; i<s ; i++) {
    nt = elementAt(i);
    if (nt.getNeighbour().getLabel() == n)
      return i;
  }
  return (-1);
}


/**
 * This method calculates the number of neighbours of the node
 * 
 * @return The number of neighbours the list.
 */

public int size() {
  
  return neighbourList.size();
}


/**
 * Removes the element at a given position.
 * 
 * @param p The position of the element to remove.
 */

public void removeElementAt(int p) {
  
  neighbourList.removeElementAt(p);
}


/**
 * Removes a node from the list of neighbours.
 * 
 * @param nodetoRemove The node to remove.
 */

public void removeNeighbour(NodeJoinTree nodetoRemove) {
  
  int i = 0;
  boolean found = false;
  NeighbourTree neighbourNode;
      
  while ((i<this.size()) && (!found)) {
    neighbourNode = (NeighbourTree) this.neighbourList.elementAt(i);
    if (neighbourNode.neighbour.nodeRelation.isTheSame(nodetoRemove.nodeRelation)) {
      this.removeNeighbour(neighbourNode);
      found = true;
    }
    i++;
  }
}




/**
 * Removes a node from the list of neighbours.
 * 
 * @param labelToRemove The label of the node to remove.
 */

public void removeNeighbour(int labelToRemove) {
  
  int i = 0;
  boolean found = false;
  NeighbourTree neighbourNode;
  NodeJoinTree node;
      
  while ((i<this.size()) && (!found)) {
    neighbourNode = (NeighbourTree) this.neighbourList.elementAt(i);
    if (neighbourNode.neighbour.getLabel()==labelToRemove) {
      this.removeNeighbour(neighbourNode);
      found = true;
    }
    i++;
  }
}
   

/**
 * Gets the message towards a given node.
 * @param n the node towards which the message is searched.
 * @return the message.
 */

public Relation getMessage(NodeJoinTree n) {
 
  int pos;
  
  pos = indexOf(n.getLabel());
  
  return elementAt(pos).getMessage();
}
  
  
} // end of class