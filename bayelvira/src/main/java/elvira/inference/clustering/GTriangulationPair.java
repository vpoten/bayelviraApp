/*
 * GTriangulationPair.java
 *
 */

package elvira.inference.clustering;

import java.io.*;
import elvira.Node;


/**
 * Class <code>GTriangulationPair</code> 
 * Represents a pair of a node and the value after removing it
 * in the triangulation.
 *
 * @author Julia Flores
 * @author Jose A. Gámez
 * @since 22/06/2003
 */

public class GTriangulationPair {
    
/**
 * The name of this node
 */
private Node node; 
  
/**
 *  The value associated to it by the used heuristic
 */
private double value; 


//
// ---- constructors -----
//

/** 
 * Creates an empty object
 *
public GTriangulationPair() {
}


/**
 * Creates an object with the name and value passed as parameters
 * @param n a <code>Node</code>
 * @param d the value associated to it
 */
    
public GTriangulationPair(Node n,double d){
  node = n;
  value = d;
}

/***** Access methods *****/
    
/**
 * This method sets the node
 * @param n a <code>Node</code> 
 */

public void setNode(Node n){
  node = n;
}
    
    
/**
 * This method sets the value associated to the node
 * @param d a double     
 */

public void setValue(double d){
  value = d;
}

/**
 * This method return the node
 * @return a <code>Node</code>
 */
    
public Node getNode( ){ 
  return node;
}

/**
 * This method returns the value associated to this node
 * @return a double    
 */
 
public double getValue( ){
  return value;
}

} // end of class
