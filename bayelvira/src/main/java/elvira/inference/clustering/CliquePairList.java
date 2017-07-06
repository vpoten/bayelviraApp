/* CliquePairList.java */

package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import elvira.*;

/**
 * Implements a list of objects of class <code>CliquePair</code>
 * in which the elements are ranked in ascending order,
 * attending to one of the following parameters:
 * size, restricted size or ratio.
 *
 * @since 12/9/2000
 */

public class CliquePairList {

/**
 * The list of objects of class <code>CliquePair</code>
 */

private Vector list;


/**
 * Constructor: creates a list from a join tree, a list of variables and 
 * the criterion used to rank the elements.
 * 
 * IMPORTANT: the nodes of the join tree must be labeled.
 *
 * @param jt the <code>JoinTree</code>
 * @param set the <code>NodeList</code>
 * @param criterion a <code>String</code> with the criterion used
 * to rank the elements.
 */

public CliquePairList(JoinTree jt, NodeList set, String criterion) {

  int i, j, s;
  NodeJoinTree nodeHead, nodeTail;
  NeighbourTreeList ntl;
  NeighbourTree nt;
  CliquePair cp;
  NodeList nl, nl2;

  list = new Vector();
  s = jt.size();
  
  for (i=0 ; i<s ; i++) {
    nodeHead = jt.elementAt(i);
    ntl = nodeHead.getNeighbourList();
    
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      nodeTail = nt.getNeighbour();
      
      // the following comparison is to avoid the introduction of the
      // same link two times
      if (nodeHead.getLabel() < nodeTail.getLabel()) {
        // Now we have to test if the separator contains variables not in set
        nl = (nodeHead.getVariables()).intersection(nodeTail.getVariables());
        nl2 = nl.intersection(set);
        if (nl.size() != nl2.size()){ // some variables in nl are not in set
          cp = new CliquePair(nodeHead,nodeTail,set);
          addElement(cp,criterion);
        }
        else // one of the nodes is a subset of the other 
          if ( nl.equals(nodeHead.getVariables()) ||
                      nl.equals(nodeTail.getVariables()) ) {
            cp = new CliquePair(nodeHead,nodeTail,set);
            addElement(cp,criterion);
	}
      }        
    }
  }
}


/**
 * Gets an element from a given position in the list.
 * @param i the index of the element to retrieve. 
 * @returns the <code>CliquePair</code> at position <code>i</code>.
 */

public CliquePair elementAt(int i) {

  return (CliquePair)list.elementAt(i);
}


/**
 * Gets the element in the first position and remove it from the list.
 * @return the first <code>CliquePair</code> in the list.
 */
 
public CliquePair getFirstAndRemove() {
  
  CliquePair cp;
  
  cp = (CliquePair) list.elementAt(0);
  list.removeElementAt(0);   
  return cp;
}


/**
 * Gets the size of the list.
 * @returns the size of list (<code>int</code>).
 */

public int size() {
  
  return list.size();
}


/**
 * Adds a new element to the list, mantaining the ranking.
 *
 * @param e the <code>CliquePair</code> to add.
 * @param criterion a <code>String</code> with the criterion
 * used for ranking.
 */

public void addElement(CliquePair e, String criterion) {

  int min, max, middle;
  int s;
  double eValue, middleValue;

  eValue = e.getValue(criterion);
  s = list.size();
  
  if (s == 0) {
    middle = 0;
  }
  else if ( ((CliquePair)list.elementAt(s-1)).getValue(criterion) < eValue) {
    middle = s;
  }
  else {
    for (min = 0, max = s-1, middle = (int) (min+max)/2; 
	 min < max; 
	 middle = (int) (min+max)/2) {
      middleValue = ((CliquePair)list.elementAt(middle)).getValue(criterion);
      if (middleValue > eValue) {
        max = middle;
      }
      else if (middleValue < eValue) {
        min = middle + 1;
      }
      else break; // middle is the possition 
    }
  }
  // Insert the element in middle
  list.insertElementAt(e,middle);
}


/**
 * Removes all the elements (links) in the list which contain the clique
 * passed as argument.
 *
 * @param clique a <code>NodeJoinTree</code>.
 */

public void removeAllElements(NodeJoinTree clique) {

  int i, s;
  CliquePair cp;

  s = list.size();
  for (i=0 ; i<s ; ) {
    cp = elementAt(i);
    if ( (clique == cp.getHead()) || (clique == cp.getTail()) ) {
      list.removeElementAt(i);
    }
    else i++;
  }
}


/**
 * Obtains a list containing all the elements (links) in this list that
 * contain the clique passed as argument. The elements will be removed also.
 *
 * @param clique a <code>NodeJoinTree</code>.
 * @return a <code>Vector</code> with the list of objects of class
 * <code>CliquePair</code> obtained.
 */

public Vector getListAndRemoveElements(NodeJoinTree clique) {

  int i;
  CliquePair cp;
  Vector v = new Vector();

  for (i=0 ; i<list.size() ; ) {
    cp = elementAt(i);
    if ( (clique == cp.getHead()) || (clique == cp.getTail()) ) {
      v.addElement(cp);
      list.removeElementAt(i);
    }
    else i++;
  }
  
  return v;
}
 

/**
 * Detect if a cliquePair (H,T) or (T,H) is included in the list
 *
 * @param H a NodeJoinTree
 * @param T a NodeJoinTree
 *
 * @return a boolean
 */

public boolean isIncluded(NodeJoinTree H,NodeJoinTree T){

  int i;
  CliquePair cp; 

  for(i=0;i<this.size();i++){
    cp = this.elementAt(i);
    if ( ( (H==cp.getHead()) && (T==cp.getTail()) ) || 
         ( (H==cp.getTail()) && (T==cp.getHead()) ) )
      return true;
  } 

  return false;
}

/**
 * Prints the object to the standard output.
 */

public void print() {
 
  int i;

  System.out.println("Printing CliquePairList");
  for (i=0 ; i<this.size() ; i++) {
    System.out.println("Element at " + i);
    this.elementAt(i).print();
  }
}

} // end of class