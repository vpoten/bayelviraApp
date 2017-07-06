/*
 * Family.java
 *
 */

package elvira.inference.clustering;

import java.io.*;
import elvira.*;


/**
 * Class <code>Family</code> 
 * Represents a family in a network. Is a pair containing the node
 * owning the family and a relation with the information about the
 * family
 *
 * @author Jose A. Gámez
 * @since 30/06/2003
 */

public class Family {
    
/**
 * The node
 */
private Node node;
  
/**
 *  The relation containing the family
 */
private Relation relation;


//
// ---- constructors -----
//

/** 
 * Creates a new object
 * @param n a <code>Node</code>
 * @param r a <code>Relation</code>
 */

public Family(Node n,Relation r) {
  node = n;
  relation = r;
}


/***** Access methods *****/
    
/**
 * This method sets the node
 * @param n an <code>Node</code> 
 */

public void setNode(Node n){
  node = n;
}
    
    
/**
 * This method sets the relation
 * @param r a <code>Relation</code>
 */

public void setRelation(Relation r){
  relation = r;
}

/**
 * This method returns the node
 * @return a <code>Node</code>
 */
    
public Node getNode( ){ 
  return node;
}

/**
 * This method returns the relation
 * @return a <code>Relation</code>
 */
 
public Relation getRelation( ){
  return relation;
}

} // end of class
