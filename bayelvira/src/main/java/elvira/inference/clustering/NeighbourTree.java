/* NeighbourTree.java */

package elvira.inference.clustering;

import elvira.*;

/**
 * Class <code>NeighbourTree</code>. Implements the structure that will
 * contain the information about one neighbour of a node in the Join Tree.
 * To work with objects of this class it is recommended to use
 * a <code>Vector</code>.
 *
 * For instance, if two nodes X and Y are neighbours, node X will have
 * associated an object of class <code>NeighbourTree</code>, containing
 * node Y and the message from X to Y.
 *
 * @since 12/9/2000
 */

public class NeighbourTree {
   
/**
 * The neighbour of the node to whom this object belongs. 
 */
NodeJoinTree neighbour;  
  
/**
 * The message from the node to whom this object belongs, to the node
 * stored in <code>neighbour</code>.
 */
Relation message;

/**
 * A reference to the message (NeighbourTree) in the opposite sense.
 */
private NeighbourTree oppositeMessage;

/**
 * Constructor. Creates an empty object of this class.
 */

public NeighbourTree() {
  
  neighbour = new NodeJoinTree();
  message = new Relation();
}
   
   
/**
 * This function is used to set the neighbour node.
 * 
 * @param n The node to set as neighbour.
 */

public void setNeighbour (NodeJoinTree n) {
  
  neighbour = n;
}
   
   
/**
 * This function is used to set the message.
 * 
 * @param r The message (it is a <code>Relation</code>).
 */

public void setMessage (Relation r) {
  
  message = r;
}
 
/**
 * Get the oppositeMessage reference (pointer to the message in the opposite sense)
 * @return A reference to the message in the opposite sense
 */
public NeighbourTree getOppositeMessage(){
  return oppositeMessage;
}

/** 
 * Set the oppositeMessage reference (pointer to the message in the opposite sense)
 * with the value nt
 * @param nt the new value for the oppositeMessage reference
 */
public void setOppositeMessage(NeighbourTree nt){
  oppositeMessage=nt; 
}

/**
 * This function is used to get the neighbour node.
 * 
 * @return The neighbour.
 */

public NodeJoinTree getNeighbour() {
  
  return neighbour;
}
   
   
/**
 * This function is used to get the message.
 * 
 * @return The message.
 */
   
public Relation getMessage() {
  
  return message;
}

public void print(){
  System.out.println("NeighbourTree.print()");
  System.out.println("=====================");
  System.out.print("Message to nodeJoinTree with label="+neighbour.getLabel());
  message.print();
}

} // end of class    