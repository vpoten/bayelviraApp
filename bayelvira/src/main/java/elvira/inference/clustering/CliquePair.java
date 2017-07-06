/* CliquePair */

package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import elvira.*;

/** 
 * This class implements an object which contains two nodes
 * (<code>NodeJoinTree</code>) that are neighbours in a join tree.
 * The class will be used for the implementation of some heuristics used
 * in the process of the inner restriction of a join tree with respect to
 * a list of varibles. Therefore, some extra information
 * is stored for each link:
 *  - size: the size of the list of variables formed with the union
 *    of both cliques
 *  - restrictedSize: the size of the clique formed from this link, that is,
 *    removing from the whole set the variables not in set, except those necessary 
 *    to mantain a join tree (the join property holds after to 
 *    replace this link by the new clique)
 *  - ratio: the quotient restrictedSize/size.
 *
 * @since 12/9/2000
 */

public class CliquePair {

/**
 * The two nodes placed in the extremes of the link. Although the link in
 * a join tree is undirected, we use the directed terminology head and 
 * tail for the cliques in the link.
 */
  
private NodeJoinTree head;
private NodeJoinTree tail;

/**
 * The list of variables induced by the union of head and tail.
 */

private NodeList completeList;

/**
 * The restricted list of variables.
 */

private NodeList restrictedList;

/**
 * The numerical data described in the beginning of this class.
 */

private double size;
private double restrictedSize;
private double ratio;


/**
 * Constructor. Builds a <code>CliquePair</code> from two cliques and
 * the set of variables to which the join tree has to be restricted.
 * 
 * @param cliqueA a <code>NodeJoinTree</code> to store in this object.
 * @param cliqueB a <code>NodeJoinTree</code> to store in this object.
 * @param set the list of variables, as a <code>NodeList</code>
 */

public CliquePair(NodeJoinTree cliqueA, NodeJoinTree cliqueB, NodeList set) {
  
  NodeList varsSep;
  NeighbourTreeList ntl;
  NeighbourTree nt;
  NodeJoinTree node, node2, other;
  Vector headTail;
  Relation r;
  int i, j;
  double sizeH,sizeT;
  
  head = cliqueA;
  tail = cliqueB;
  
  completeList = new NodeList(); 
  completeList.join(head.getVariables()); 
  completeList.join(tail.getVariables());

  sizeH = FiniteStates.getSize(head.getVariables());
  sizeT = FiniteStates.getSize(tail.getVariables());  
  size = FiniteStates.getSize(completeList);
  if ((size == sizeH) || (size == sizeT)) 
    size = 0; // when absorption no new clique is created
  
  // now a list with the variables in all the links (separators) with head
  // or tail in an extreme (except for the link head-tail) is build, because
  // this variables has to be mantained in the new clique in order to ensure
  // the join property.
  
  varsSep = new NodeList();
  headTail = new Vector();   // as the process is the same for the two nodes,
  headTail.addElement(head); // we introduce both in a vector and use an
  headTail.addElement(tail); // interative procedure
  
  for (i=0 ; i<2 ; i++) {
    node = (NodeJoinTree)headTail.elementAt(i);
    other = (NodeJoinTree)headTail.elementAt((i+1)%2);
    ntl = node.getNeighbourList();
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      node2 = nt.getNeighbour();
      if (node2 != other) {
        r = nt.getMessage();
        varsSep.join(r.getVariables());
      }
    }
  }
  
  restrictedList = completeList.intersection(set);
  restrictedList.join(varsSep);
  
  restrictedSize = (int) FiniteStates.getSize(restrictedList);
  if (size == 0) { restrictedSize = 0; ratio = 0;}
  else {ratio = restrictedSize/size;}     
}

// Access methods

/**
 * Gets the head node.
 * @return the <code>NodeJoinTree</code> in head
 */
 
public NodeJoinTree getHead() {
  
  return head;
}


/**
 * Gets the tail node.
 * @return the <code>NodeJoinTree</code> in tail
 */
 
public NodeJoinTree getTail() {
  
  return tail;
}
 

/**
 * Gets the complete list of variables.
 * @return <code>completeList</code>
 */

public NodeList getCompleteList() {
  
  return completeList;
}


/**
 * Gets the restricted list of variables.
 * @return <code>restrictedList</code>
 */

public NodeList getRestrictedList() {

  return restrictedList;
}


/**
 * Gets the size of the list of variables formed with the union
 * of both cliques
 * @return <code>size</code>
 */
 
public double getSize() {
  
  return size;
}


/**
 * Gets the size of the clique formed from this link, that is,
 * removing from the whole set the variables not in set, except those
 * necessary to mantain a join tree (the join property holds after to 
 * replace this link by the new clique)
 * @return <code>restrictedSize</code>
 */
 
public double getRestrictedSize() {
  
  return restrictedSize;
}


/**
 * Gets the quotient <code>restrictedSize/size</code>.
 * @return <code>ratio</code>
 */
 
public double getRatio() {
  
  return ratio;
}
 

/**
 * Gets the size value of the link.
 * @param criterion a <code>String</code> indicating the size to be
 * returned.
 * @return the value of the size specified by <code>criterion</code>
 */ 
 
public double getValue(String criterion) {

  if (criterion.equals("size")) {
    return size;
  }
  else if (criterion.equals("restrictedSize")) {
    return restrictedSize;
  }
  else if (criterion.equals("ratio")) {
    return (restrictedSize + ratio);
  }
  else {
    System.out.println("CliquePair:getValue:ERROR --- unknown criterion");
    System.exit(0); 
  }       
  
  return -1; // the method never will arrive until this point
}


/**
 * Print a <code>CliquePair</code> to the standard output.
 */

public void print() {
  
  int i;
  NodeList nl;

  System.out.println("\tHead: " + this.getHead().getLabel());
  System.out.println("\tTail: " + this.getTail().getLabel());
  System.out.print("\tCompleteList: ");
  nl = this.getCompleteList();
  
  for (i=0 ; i<nl.size() ; i++)
    System.out.print(nl.elementAt(i).getName() + " ");
  
  System.out.println();
  System.out.print("\tRestrictedList: ");
  nl = this.getRestrictedList();
  
  for (i=0 ; i<nl.size() ; i++)
    System.out.print(nl.elementAt(i).getName() + " ");
  
  System.out.println();
  System.out.println("\tSize:           " + this.getSize());
  System.out.println("\tRestrictedSize: " + this.getRestrictedSize());
  System.out.println("\tRatio:          " + this.getRatio());
}

} // end of class