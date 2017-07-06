/* MarkovChainMonteCarlo.java */

package elvira.inference.approximate;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.potential.*;

/**
 * Class MarkovChainMonteCarlo.
 * Implements a genral framework for Markov Chain Monte Carlo
 * algorithms.
 *
 * @since 26/6/2000
 */


public class MarkovChainMonteCarlo extends SimulationProp {
  
/**
 * The sample obtained during the simulation.
 */
CaseListMem sample;

/**
 * The configuration being simulated in a precise moment.
 */
Configuration currentConf;

/**
 * Number of repetitions of the experiment.
 */
int numberOfRuns; 

/**
 * A Hash Table indexed by nodes that contains, for each node, the
 * potentials of its Markov Blanket. The potentials for a
 * specific node are stored as a <code>RelationList</code>.
 */
Hashtable distributions;


/**
 * Sets the number of runs.
 * @param n the number of runs.
 */

public void setNumberOfRuns(int n) {
  
  numberOfRuns = n;
}


/**
 * @return the initial relations present
 * in the network.
 */

public RelationList getInitialRelations() {
  
  Relation rel, newRel;
  RelationList list;
  int i;
 
  list = new RelationList();
  
  for (i=0 ; i<network.getRelationList().size() ; i++) {
    rel = (Relation)network.getRelationList().elementAt(i);
    newRel = new Relation();
    newRel.setVariables(rel.getVariables().copy());
    
    newRel.setValues(rel.getValues());
    list.insertRelation(newRel);
  }
  
  return list;
}


/**
 * Determines the distributions in the Markov Blanket of every
 * variable in the network.
 */

public void determineMarkovBlanket() {
 
  RelationList initialRelations, auxRelList;
  Node auxNode;
  NodeList variables, nodesInNetwork;
  Relation auxRelation;
  int i, j;
  
  initialRelations = getInitialRelations();
  
  distributions = new Hashtable(20);
  
  nodesInNetwork = network.getNodeList();
  
  for (i=0 ; i<nodesInNetwork.size() ; i++) {
   
    auxNode = nodesInNetwork.elementAt(i);
    auxRelList = new RelationList();
    distributions.put(auxNode,auxRelList);
  }
  
  for (i=0 ; i<initialRelations.size() ; i++) {
   
    auxRelation = initialRelations.elementAt(i);
    variables = auxRelation.getVariables();
  
    for (j=0 ; j<variables.size() ; j++) {
      
      auxNode = variables.elementAt(j);
      auxRelList = (RelationList)distributions.get(auxNode);
      auxRelList.insertRelation(auxRelation);
    }
  }
}

} // End of class