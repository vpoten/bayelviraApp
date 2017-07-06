/* AIPartitionElement.java */

package elvira.inference.abduction;

import java.io.*;
import java.util.Vector;
import elvira.*;
import elvira.inference.clustering.NodeJoinTree;

/**
 * Class <code>AIPartitionElement</code> - 
 * (short name for AbductiveInferencePartitionElement)
 * Contains the information necessary to indentify an element
 * of the partition built by Nilsson's algorithm.
 *
 * @since 14/9/2000
 */

public class AIPartitionElement {

/**
 * The <code>NodeJoinTree</code> for which the Partition Element was built.
 */

private NodeJoinTree clique;

/**
 * The index of the explanation for which the Partition Element was built.
 */

private int expIndex;

/**
 * The configuration whose values must be included in all the elements
 * of this Partition Element.
 */

private Configuration sameValues;

/**
 * A vector with the configurations impossible to be considered.
 */

private Vector distincts;

/**
 * The maximum subconfiguration found.
 */

private Configuration maxSubConf;

/**
 * The probability P(maxSubConf,observations)
 */

private double prob;


/**
 * Constructor.
 *
 * @param n the clique.
 * @param e the explanation index.
 * @param i the configuration to initialize <code>sameValues</code>.
 */

AIPartitionElement(NodeJoinTree n, int e, Configuration i) {

  clique = n;
  expIndex = e;
  sameValues = i;
  distincts = new Vector();
  maxSubConf = new Configuration(n.getVariables());
  prob = 0.0;
}                                             


/**
 * Gets the clique stored in this object.
 * @return the clique.
 */

public NodeJoinTree getClique() {
  
  return clique;
}


/**
 * Gets the explanation index.
 * @return the explanation index.
 */

public int getExpIndex() {
  
  return expIndex;
}


/** 
 * Gets the configuration in <code>sameValues</code>.
 * @return the configuration <code>sameValues</code>.
 */

public Configuration getSameValues() {
  
  return sameValues;
}


/**
 * Gets the not allowed configurations.
 * @return the vector of configurations <code>distincts</code>.
 */

public Vector getDistincts() {
  
  return distincts;
}


/**
 * Gets the max configuration.
 * @return the max configuration.
 */ 

public Configuration getMaxSubConf() {
  
  return maxSubConf;
}


/**
 * Gets the probability value stored in <code>prob</code>.
 * @return the probability.
 */

public double getProb() {
  
  return prob;
}


/**
 * Sets the clique.
 * @param n the <code>NodeJoinTree</code> to store in <code>clique</code>.
 */

public void setClique(NodeJoinTree n) {
  
  clique = n;
}


/**
 * Sets the expIndex.
 * @param i the new index.
 */

public void setExpIndex(int i) {
  
  expIndex = i;
}


/** 
 * Sets the configuration in  <code>sameValues</code>.
 * @param c the new configuration.
 */

public void setSameValues(Configuration c) {
  
  sameValues = c;
}


/**
 * Sets the vector of configurations not allowed.
 * @param v the vector with such configurations.
 */

public void setDistincts(Vector v) {

  int i;
  
  distincts = new Vector();
  for (i=0 ; i<v.size() ; i++)
    distincts.addElement(v.elementAt(i));
}


/**
 * Sets the max configuration.
 * @param c the configuration.
 */ 

public void setMaxSubConf(Configuration c) {

  maxSubConf = c;
}


/**
 * Sets the probability.
 * @param p the probability.
 */

public void setProb(double p) {

  prob = p;
}


/**
 * Adds a new distinct configuration.
 *
 * @param c the configuration to be added.
 */

public void addDistinct(Configuration c) {

  distincts.addElement(c);
}


/**
 * Prints this partition element to the standard output.
 */
 
public void print () {

  int i, j;
  Configuration conf;

  System.out.println("");
  System.out.println("Clique: " + clique.getLabel());
  System.out.println("Explanation: " + expIndex);
  System.out.print("Same subconfiguration: ");
  for (i=0 ; i<sameValues.size() ; i++) {
    System.out.print(((Node)sameValues.getVariables().elementAt(i)).getName()
                         + " ");
  }
  sameValues.print();
  System.out.println();

  System.out.println("Differents subconfigurations: ");
  for (i=0 ; i<distincts.size() ; i++) {
    conf = (Configuration)distincts.elementAt(i);
    for (j=0 ; j<conf.size() ; j++) {
      System.out.print(((Node)conf.getVariables().elementAt(j)).getName()
                         + " ");
    }
    conf.print();
    System.out.println();
  }

  System.out.print("Max subconfiguration found: ");
  for (i=0 ; i<maxSubConf.size() ; i++) {
    System.out.print(((Node)maxSubConf.getVariables().elementAt(i)).getName()
                         + " ");
  }
  maxSubConf.print();
  System.out.println();

  System.out.println("with probability " + prob);    
}

} // end of class