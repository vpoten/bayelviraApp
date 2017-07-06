/*
 * NeighbourTreeDouble.java
 *
 * Created on 25 de junio de 2004, 12:52
 */

package elvira.inference.clustering.multiplemessaging;

import elvira.inference.clustering.NeighbourTree;
import elvira.*;

/**
 *
 * @author  Andrés Cano Utrera (acu@decsai.ugr.es)
 */
public class NeighbourTreeMultiple extends NeighbourTree{
  /**
   * A list of Relations (RelationList) with additional Relations in the message. It
   * can be used in some special Propagation methods where we need several Relations to
   * be send from one clique to another one.
   */
  private RelationList additionalMessages;
    
  
  /** Creates a new instance of NeighbourTree. The list of additional messages
   * is initialized with one additional message (Relation)
   */
  public NeighbourTreeMultiple() {
    additionalMessages = new RelationList();
    additionalMessages.insertRelation(new Relation());
  }
  
  /**
   * Gets an additional message (number n)
   * @param n the number of message to be returned
   * @return a Relation representing the additional message number n
   */
  public Relation getAdditionalMessage(int n){
    return additionalMessages.elementAt(n);
  }
  
}
