/*
 * NodeJoinTreeDouble.java
 *
 * Created on 25 de junio de 2004, 13:50
 */

package elvira.inference.clustering.multiplemessaging;

import elvira.inference.clustering.*;
import elvira.*;

/**
 * This class extends NodeJoinTree to allow maintaining more than one Potential
 * in the clique.
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 * @author Manuel Gómez Olmedo (mgomez@decsai.ugr.es)
 */
public class NodeJoinTreeMultiple extends NodeJoinTree {
    
  /**
   * The list of additional Relations with respect to its superclass NodeJoinTree
   */
    private RelationList additionalRelations;
    
    
   /**
     * Creates a new <code>NodeJoinTree</code> 
     */
    public NodeJoinTreeMultiple() {
      super();
      additionalRelations=new RelationList();
    }
    
    
    /**
     * Creates a new <code>NodeJoinTree</code> and attaches to it
     * a given relation.
     * @param r the <code>Relation</code> to store in the new node.
     */
    public NodeJoinTreeMultiple(Relation r) {
        super(r);
        additionalRelations=new RelationList();
    }
    
    /**
     * Gets an additional Relation (the number n)
     * @param n the number of additional Relation to be returned
     * @return a Relation representing the additional Relation number n
     */
    public Relation getAdditionalRelation(int n){
        return additionalRelations.elementAt(n);
    }
    
    /**
     * Gets the number of addtional Relations
     * @return the number of addtional Relations
     */
    public int getAdditionalRelationsSize(){
        return additionalRelations.size();
    }
    
    /**
     * Appends an additional Relation to this NodeJoinTree
     * @param rel the new Relation to be inserted
     */
    public void  putAdditionalRelation(Relation rel){
        additionalRelations.insertRelation(rel);
    }
    
    /**
     * Removes the Relation at position i from the list of additional Relations
     * @param i the position of the Relation to be removed
     */
    public void removeAdditionalRelation(int i){
        additionalRelations.removeElementAt(i);
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
