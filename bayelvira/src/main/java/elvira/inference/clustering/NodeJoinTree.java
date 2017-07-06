/* NodeJoinTree.java */

package elvira.inference.clustering;

import elvira.*;
import java.util.Vector;
import java.util.ArrayList;
import elvira.potential.*;

/**
 * This class represents a node of a join tree. A node of a joint tree
 * consists of a relation, a list of neighbours and a label.
 *
 * @author Andres Cano
 * @author Antonio Salmerón
 * @author Jose A. Gámez
 * @author Julia Flores
 * 
 * @since 4/07/2003
 */

public class NodeJoinTree {
   
/**
 * Contains the relation associated with the node.
 */
Relation nodeRelation;
   
/**
 * A list with the outgoing neighbours of the node.
 */
protected NeighbourTreeList neighbourList;
   
/**
 * A label.
 */  
int label;


/**
 * A list with the families in the network associated to this node,
 * when initialising the potentials. 
 * 
 */  
ArrayList families;
   
/**
 * is true if this node is in fact a MPS (maximal prime subgraph)
 */

boolean isMPS;

/**
 * Contains the Maximal Prime Subgraph to which this nodejointree
 * has been assigned. Only of interest if a Maximal Prime Subgraph Tree
 * is constructed.
 */

NodeJoinTree correspondingMPS;
    
/**
 * At the moment this field is used only when isMPS is true.
 * A <code>NodeList</code> with the set of variables included
 * in this MPS
 */

NodeList variables;

/**
 * Only useful if isMPS is true. 
 * A list with the NodeJoinTree it contains
 */

ArrayList cliques;

/**
 * isSimplicial: indicates if the clique was obtained by deleting
 * a simplicial node. Default is false.
 */

boolean isSimplicial=false;


/**
 * isMarked: indicates if the clique (or MPS) is marked during
 *  incremental compilation. Default is false.
 */

boolean isMarked=false;


/**
 * Constructor. Creates an empty object of this class.
 */


public NodeJoinTree() {
  
  nodeRelation = new Relation();
  neighbourList = new NeighbourTreeList();
  families = new ArrayList();
  isMPS = false;
  correspondingMPS = null;
  cliques = null;
  variables = null;
}
    
    
/**
 * Creates a new <code>NodeJoinTree</code> and attaches to it
 * a given relation.
 * @param r the <code>Relation</code> to store in the new node.
 */

public NodeJoinTree (Relation r) {
  
  neighbourList = new NeighbourTreeList();
  nodeRelation = r;
  families = new ArrayList();
  isMPS = false;
  correspondingMPS = null;
  cliques = null;
  variables = null;
}


/**
 * Inserts a relation in the node.
 * 
 * @param r the <code>Relation</code> to store in the node.
 */

public void setNodeRelation (Relation r) {
  
  nodeRelation = r;
}


/**
 * This method sets the list of neighbours.
 * 
 * @param list the list of the neighbours (<code>NeighbourTreeList</code>).
 */

public void setNeighbourList(NeighbourTreeList list) {
  
  neighbourList = list;
}


/**
 * Sets the label.
 * @param l the label.
 */

public void setLabel(int l) {
  
  label = l;
}


/**
 * Sets isSimplicial
 * @param b a boolean.
 */

public void setIsSimplicial(boolean b) {
  
  isSimplicial = b;
}

/**
 * Sets the isMPS
 * @param b the boolean.
 */

public void setIsMPS(boolean b) {
  
  isMPS = b;
}

/**
 * Sets the isMarked
 * @param b the boolean.
 */

public void setIsMarked(boolean b) {

  isMarked = b;
} 

/**
 * Sets the cliques
 * @param list an ArrayList
 */

public void setCliques(ArrayList list) {
  
  cliques = list;
}


/**
 * Gets the cliques
 * @return an <code>ArrayList</code>
 */

public ArrayList getCliques( ) {
  
  return cliques;
}


/**
 * Sets the corresponding MPS.
 * @param m the MPS
 */

public void setCorrespondingMPS(NodeJoinTree m) {
  correspondingMPS = m;
}


/**
 * Gets the corresponding MPS.
 * @return a <code>NodeJoinTree</code>
 */

public NodeJoinTree getCorrespondingMPS( ) {
  return correspondingMPS;
}


/**
 * Gets the label.
 * @return the label.
 */

public int getLabel() {
  
  return label;
}

/**
 * Gets isSimplicial
 * @return a boolean
 */

public boolean getIsSimplicial() {
  
  return isSimplicial;
}


/**
 * Gets isMPS
 * @return a boolean
 */

public boolean getIsMPS() {
  
  return isMPS;
}

/**
 * Gets isMarked
 * @return a boolean
 */

public boolean getIsMarked() {

    if (isMPS)
        return isMarked;
    else
        if (this.getCorrespondingMPS()!=null)
            return (this.getCorrespondingMPS().getIsMarked());
        else
        return false;
} 

/**
 * This method is used to get the relation in the node.
 * 
 * @return the <code>Relation</code> in the node.
 */

public Relation getNodeRelation() {
  
  return nodeRelation;
}


/**
 * Gets the list of variables in the node.
 * @return a <code>NodeList</code> with the variables in the node.
 */

public NodeList getVariables() {
  if (variables==null) variables = nodeRelation.getVariables();
  return variables;
}

/**
 * Sets the list of variables in the node.
 * @param nl a <code>NodeList</code> 
 */

public void setVariables(NodeList nl) {
  variables = nl;
}



/**
 * Gets the family list associated to this node
 * @return a <code>ArrayList</code> with the families associated to this node
 */

public ArrayList getFamilies() {
     
  return families;
}


/**
 * Get the list of outgoing neighbours (messages).
 * 
 * @return the neighbours of the node.
 */

public NeighbourTreeList getNeighbourList() {
  
  return neighbourList;
}

/**
 * Get a list of incoming messages
 * @return The list of the incoming messages
 */
public NeighbourTreeList getIncomingMessages() {
  NeighbourTreeList ntl = new NeighbourTreeList();
  for(int i=0;i<neighbourList.size();i++){
    ntl.insertNeighbour(neighbourList.elementAt(i).getOppositeMessage());
  }
  return ntl;
}

/**
 * Removes all the neighbours of a node except the one who contains the 
 * given relation as message.
 * 
 * @param r is the message that determines the neighbour that will not
 * be removed.
 */

public void removeOtherParents(Relation r) {
  
  int i;
  NeighbourTree element;
  Relation tmp;
  
  for (i=0 ; i<this.neighbourList.size() ; i++) {
    element = new NeighbourTree();
    element = (NeighbourTree) this.neighbourList.elementAt(i);
    tmp = (Relation) element.getMessage();
    if (!tmp.isTheSame(r))
      this.neighbourList.removeElementAt(i);
  }
  
}


/**
 * Determines whether a node is a leaf or not. A node is
 * considered a leaf if it has less than two neighbours.
 * @return <code>true</code> if the node is a leaf,
 * <code>false</code> otherwise.
 */

public boolean isLeaf() {
  
  if (neighbourList.size() < 2)
    return true;
  return false;
}


/**
 * Inserts a family associated to this Clique/MPS.
 * @param f The family to be inserted
 */

public void insertFamily(Family f) {

  families.add(f);
}


/**
 * Inserts a new message (neighbour) to the list of messages of this clique. 
 * The message (neighbour) is create whithin this method.
 * @param neighbour a <code>NodeJoinTree</code> that will be the neighbour
 * of this one.
 */

public void insertNeighbour(NodeJoinTree neighbour) {
  
  NeighbourTree nt;
  Relation msg;
  
  nt = makeNeighbourTree();
  nt.setNeighbour(neighbour);
  
  // Now compute the message
  msg = getNodeRelation().intersection(neighbour.getNodeRelation());
  nt.setMessage(msg);
  
  neighbourList.insertNeighbour(nt);
}

/**
 * Creates an instance of class NeighbourTree
 * Override this method in subclasses to get other behaviour.
 * @return a new instance of NeighbourTree
 */
protected NeighbourTree makeNeighbourTree(){
  return new NeighbourTree();
}

/**
 * Put the message nt as a new message in the list of messages of this clique
 * @param nt the message to be appended to the list of messages of this clique
 */ 
public void insertNeighbour(NeighbourTree nt){
  neighbourList.insertNeighbour(nt);
}

/**
 * Inserts a neighbour in the first postion of neighbour tree list.
 * @param neighbour a <code>NodeJoinTree</code> that will be a neighbour
 * of this.
 */

public void insertNeighbourAsFirstElement(NodeJoinTree neighbour) {
  
  NeighbourTree nt;
  Relation msg;
  Vector v;
  
  nt = new NeighbourTree();
  nt.setNeighbour(neighbour);
  
  // Now compute the message
  msg = getNodeRelation().intersection(neighbour.getNodeRelation());
  nt.setMessage(msg);
  
  neighbourList.getNeighbourList().add(0,nt);
}


/**
 * Removes a neighbour.
 * @param neighbour the neighbour to remove (a <code>NodeJoinTree</code>).
 */

public void removeNeighbour(NodeJoinTree neighbour) {
  
  neighbourList.removeNeighbour(neighbour);
}


/**
 * Removes a neighbour by searching its label
 * @param neighbourLabel the label of the neighbour to remove (an int).
 */

public void removeNeighbour(int neighbourLabel) {
  
  neighbourList.removeNeighbour(neighbourLabel);
}


/**
 * Absorbs from a node. That is, this requests a sum-message to the
 * argument node and the message is combined with the potential stored in
 * <code>this</code>. Messages are modified.
 *
 * @param node the <code>NodeJoinTree</code> from which <code>this</code>
 * absorbs.
 * @param divide "yes" if division is performed during the absortion .
 */

public void absorbFromNode(NodeJoinTree node, String divide) {
  
  Relation r, rNode, message;
  Potential pot, newSep; 
  NeighbourTreeList ntl;
  NeighbourTree nt;
  int i;
  
  ntl = node.getNeighbourList();
  i = ntl.indexOf(this);
  if (i == -1) {
    System.out.println("NodeJoinTree.absorbFromNode:error: no neighbours");
    System.exit(0);
  }
  rNode = node.getNodeRelation();
  pot = rNode.getValues();
  nt = ntl.elementAt(i);
  message = nt.getMessage();
  if (divide.equals("yes")) {
    nt.getMessage().setOtherValues(nt.getMessage().getValues());
  }
  newSep = pot.marginalizePotential(nt.getMessage().getVariables().toVector());
  
  nt.getMessage().setValues(newSep);
  
  r = this.getNodeRelation();
  if (divide.equals("yes")) {
    pot = (r.getValues()).combine(newSep.divide(nt.getMessage().getOtherValues()));  
  }
  else
    pot = (r.getValues()).combine(newSep);       
  r.setValues(pot);
}



/**
 * Absorbs from a node. That is, this requests a sum-message to the
 * argument node and the message is combined with the potential stored in
 * <code>this</code>. Messages are modified.
 *
 * Penniless approximation is used when computing the message from
 * the node to be absorbed
 *
 * @param node the <code>NodeJoinTree</code> from which <code>this</code>
 * absorbs.
 */

public void pennilessAbsorbtionFromNode(NodeJoinTree node,
					double limitForPruning,
					double lowLimitForPruning){
  
  Relation r, rNode, message;
  Potential pot, newSep; 
  NeighbourTreeList ntl;
  NeighbourTree nt;
  int i;
  
  ntl = node.getNeighbourList();
  i = ntl.indexOf(this);
  if (i == -1) {
    System.out.println("NodeJoinTree.absorbFromNode:error: no neighbours");
    System.exit(0);
  }
  rNode = node.getNodeRelation();
  pot = rNode.getValues();
  nt = ntl.elementAt(i);
  message = nt.getMessage(); // Messages between this and node
  
  // computing the message and approximating it by the reverse message
  newSep = pot.marginalizePotential(message.getVariables().toVector());
  newSep = (PotentialMTree)newSep.conditional((Potential)message.getOtherValues());
  ((PotentialMTree)newSep).conditionalLimitBound(MultipleTree.AVERAGEPRODCOND_APPROX,
						 limitForPruning,lowLimitForPruning,0.0,2);
  
  message.setValues(newSep);

  r = this.getNodeRelation();
  pot = (r.getValues()).combine(newSep);
  r.setValues(pot);
}


/**
 * Duplicates the object.
 * !!! IMPORTANT !!! the list of neighbours is not copied, because
 * it refers to external information
 * @param probabilities if true the potential are also duplicated 
 */

public NodeJoinTree duplicate(boolean probabilities){
  NodeJoinTree node;
  int i;

  node = new NodeJoinTree();
  node.label = this.label;
  node.isMPS = this.isMPS;
  node.correspondingMPS = this.correspondingMPS;
  node.isSimplicial = this.isSimplicial;
  node.nodeRelation = this.nodeRelation.copy(probabilities);

  if (this.cliques!=null){
    node.cliques = new ArrayList(this.cliques.size());
    for(i=0;i<this.cliques.size();i++)
      node.cliques.add(this.cliques.get(i));
  }

  if (this.variables != null){
    node.variables = new NodeList();
    for(i=0;i<this.variables.size();i++)
      node.variables.insertNode(this.variables.elementAt(i));
  }

  if (this.families != null){
    node.families = new ArrayList(this.families.size());
    for(i=0;i<this.families.size();i++)
      node.families.add(this.families.get(i));
  }

  node.neighbourList = new NeighbourTreeList();

  return node;
}

/**
 * Prints the node to the standard output.
 */

public void print() {
  
  NeighbourTree nt;
  int j;
  
  System.out.println("Node "+getLabel()+" has variables :");
  getNodeRelation().print();
  System.out.println("Node "+getLabel()+" has neighbours :");
  
  for (j=0 ; j<getNeighbourList().size() ; j++) {
    nt = getNeighbourList().elementAt(j);
    System.out.println("Label : "+nt.getNeighbour().getLabel());
    
    if (nt.getMessage() != null) {
      if (nt.getMessage().getValues() != null) {
	System.out.println("OUTGOING MESSAGE");
        (nt.getMessage().getValues()).print();
      }
      if (nt.getMessage().getOtherValues() != null) {
	System.out.println("INCOMING MESSAGE");
        (nt.getMessage().getOtherValues()).print();
      }
    }
  }
  System.out.println(" ");
}

/**
 * makeCorrespondeceWithClique: the node receiving the call is in fact
 * a MPS. The parameter is a clique (in a copied join tree) that has
 * been absorved by this MPS. The idea is to stablish a correspondence
 * between the two structures.
 */

public void makeCorrespondenceWithClique(NodeJoinTree clique){
  
  if (this.cliques == null) this.cliques = new ArrayList();
  this.cliques.add(clique);
  clique.setCorrespondingMPS(this);

}

  /**
   * This method deletes a variable
   * @param var The variable to delete 
   */
public boolean deleteVar(Node var){

    boolean res;
    NeighbourTree nt;
    NodeJoinTree vec;
    NodeList nlInter;
    
    if(isLeaf()){
	nt = getNeighbourList().elementAt(0);

	// Este es el vecino (como nodeJoinTree)
	vec = nt.getNeighbour();
	nlInter = getVariables().intersection(vec.getVariables());
	if(nlInter.getId(var) == -1)
	    res = true;
	else
	    res = false;
	

    }else{
	res = false;
    }
    return res;

}

} // end of class
