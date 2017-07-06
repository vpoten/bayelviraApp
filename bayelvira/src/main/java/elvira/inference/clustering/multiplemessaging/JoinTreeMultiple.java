/*
 * JoinTreeDouble.java
 *
 * Created on 25 de junio de 2004, 12:56
 */

package elvira.inference.clustering.multiplemessaging;

import elvira.inference.clustering.*;
import elvira.*;
import elvira.tools.JoinTreeStatistics;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

/**
 * Extends JoinTree to represents a Join Tree where the cliques are of class
 * NodeJoinTree and the separators are of the class NeighbourTreeMultiple.
 * This allows to have more than one Potential in the cliques and in the
 * separators
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 * @author Manuel Gómez Olmedo (mgomez@decsai.ugr.es)
 */
public class JoinTreeMultiple extends JoinTree {
  
  /**
   * Creates a new instance of JoinTreeMultiple
   * @param b The Bnet used to make the Join Tree
   */
  
  public JoinTreeMultiple(Bnet b) {
    super(b);
  }
  
  /**
   * Creates an instance of class NodeJoinTreeMultiple
   * Override this method in subclases to get other behaviour.
   * @return a new instance of NodeJoinTreeMultiple
   */
  protected NodeJoinTree makeNodeJoinTree(){
    return new NodeJoinTreeMultiple();
  }
  
  /**
   * Creates an instance of class NodeJoinTreeMultiple and attaches to it
   * a given relation.
   * Override this method in subclases to get other behaviour.
   * @param rel the Relation to be attached to the new NodeJoinTree
   * @return a new instance of NodeJoinTreeMultiple
   */
  protected NodeJoinTree makeNodeJoinTree(Relation rel){
    return new NodeJoinTreeMultiple(rel);
  }
  
  /**
   * Creates an instance of class NeighbourTreeMultiple
   * Override this method in subclases to get other behaviour.
   * @return a new instance of NeighbourTreeMultiple
   */
  protected NeighbourTree makeNeighbourTree(){
    return new NeighbourTreeMultiple();
  }
  
}
