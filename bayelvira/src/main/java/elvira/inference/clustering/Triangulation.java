/* Triangulation.java */

package elvira.inference.clustering;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.ContinuousProbabilityTree;

/**
 * Class <code>Triangulation</code>.
 * Implements the variable elimination method of triangulation.
 *
 * @since 22/8/2001
 */

public class Triangulation extends Propagation {
    
/**
 * Contains the relations of the triangulated net.
 */
private RelationList triangulatedRelations;
   
/**
 * Contains the triangulation secuence.
 */
private NodeList triangulatedNodes;  
   
/**
 * Contains the net to Triangulate.
 */
private Bnet netToTriangulate;
   
   
/**
 * Creates an empty <code>Triangulation</code> object.
 */

public Triangulation() {

  triangulatedRelations = new RelationList();
  triangulatedNodes = new NodeList();
  netToTriangulate = new Bnet();      
}


/**
 * Constructor 2. Creates a <code>Triangulation</code> object from
 * a network passed as parameter.
 * 
 * @param b the <code>Bnet</code> that will cotain the object created.
 */

public Triangulation(Bnet b) {
  
  triangulatedRelations = new RelationList();
  triangulatedNodes = new NodeList();
  netToTriangulate = b;         		
}


/**
 * Sets a deletion sequence in <code>triangulatedNodes</code>.
 * @param sigma The deletion sequence
 */

public void setTriangulatedNodes(NodeList sigma) {
  
  triangulatedNodes=sigma;
}


/**
 * Gets the triangulated relations.
 * @return <code>triangulatedRelations</code>.
 */

public RelationList getTriangulatedRelations() {
  
  return triangulatedRelations;
}       

/**
 * Computes the initial relations present in the network.
 * @param net contains the net where we obtain the initial relations
 * @return a list of the relations in <code>net</code>.
 */

public RelationList continuousGetInitialRelations(Bnet bayesNet) {
  
  Relation r, newRelation;
  RelationList initialRelations;
  int i;
  PotentialContinuousPT pot,potActual;
  int nTerms, nSplits;
  
  initialRelations = new RelationList();
  
  for (i=0 ; i<bayesNet.getRelationList().size() ; i++) {
    r = (Relation)bayesNet.getRelationList().elementAt(i);
    potActual = (PotentialContinuousPT)r.getValues();
    //potActual.setNumTerms(3);
    //potActual.setNumSplits(2);
    newRelation = new Relation();
    newRelation.setKind(Relation.CONDITIONAL_PROB);
    newRelation.setVariables(r.getVariables().copy());
    nTerms = ((PotentialContinuousPT)(r.getValues())).getNumTerms();
    //System.out.println("El potential tiene "+nTerms+" terminos exponenciales");
    nSplits = ((PotentialContinuousPT)(r.getValues())).getNumSplits();
    pot = new PotentialContinuousPT();
    pot.setTree(new ContinuousProbabilityTree(1));
    pot.setNumTerms(nTerms);
    pot.setNumSplits(nSplits);
    newRelation.setValues(pot);
    initialRelations.insertRelation(newRelation);
  }
  
  //initialRelations.print();
  return initialRelations;
}


/**
 * Computes the initial relations present in the network.
 * @param net contains the net where we obtain the initial relations
 * @return a list of the relations in <code>net</code>.
 */

public RelationList getInitialRelations(Bnet bayesNet) {
  
  Relation r, newRelation;
  RelationList initialRelations;
  int i;
  
  initialRelations = new RelationList();
  
  for (i=0 ; i<bayesNet.getRelationList().size() ; i++) {
    r = (Relation)bayesNet.getRelationList().elementAt(i);
    newRelation = new Relation();
    newRelation.setKind(Relation.CONDITIONAL_PROB);
    newRelation.setVariables(r.getVariables().copy());            
    initialRelations.insertRelation(newRelation);
  }
  
  return initialRelations;
}


/**
 * Gets the triangulation and the deletion secuence of the net that
 * is store in <code>netToTriangulate</code>.
 */

public void getTriangulation() {
  
  NodeList nl=new NodeList();
  getTriangulation(nl);
}


/**
 * Gets the triangulation and the deletion secuence of the net that
 * is store in <code>netToTriangulate</code>. If the argument list is not
 * empty, gets a constrained deletion sequence with the nodes of that list
 * at the end of the sequence.
 *
 * @param set a list of nodes (could be empty)
 */

public void getTriangulation (NodeList set) {
  
  NodeList notRemoved;
  Node x;
  Relation r, newRelation, newRelationClone, tmp;
  RelationList currentRelations, auxRelationList;  
  PairTable pairTable;
  int i, j, k, p, pos, s;
  boolean getOut;
  
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  s = netToTriangulate.getNodeList().size();
  
  for (i=0 ; i<s ; i++) {
    x = (FiniteStates)netToTriangulate.getNodeList().elementAt(i);         
    notRemoved.insertNode(x);
    pairTable.addElement(x);         
  }
  
  currentRelations = getInitialRelations(netToTriangulate); 
  
  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));
  
  
  for (i=notRemoved.size() ; i>0 ; i--) {
    if (set.size()>0)
      x = pairTable.nextToRemove(set);
    else
      x = pairTable.nextToRemove();
    
    auxRelationList = currentRelations.getRelationsOfAndRemove(x);
    notRemoved.removeNode(x);
    pairTable.removeVariable(x);
    
    newRelation = auxRelationList.elementAt(0);                   
    pairTable.removeRelation(newRelation);
    
    for (j=1 ; j<auxRelationList.size() ; j++) {
      r = auxRelationList.elementAt(j);
      combine (newRelation, r);
      pairTable.removeRelation(r);      
    }
    
    this.triangulatedNodes.insertNode(x);              
    this.triangulatedRelations.insertRelation(newRelation);
    
    
    /* Check that the relation inserted is not contained in another one and
       that the relation inserted is not contained in another one too. */
    
    j = 0;
    getOut = false;      
    
    while ((j<this.triangulatedRelations.size()-1) && (!getOut)) {
      tmp = this.triangulatedRelations.elementAt(j);
      
      if (tmp.getVariables().size() > newRelation.getVariables().size()) {           
	if (tmp.isContained(newRelation)) {
	  this.triangulatedRelations.removeRelation(newRelation);
	  getOut = true;
	}
      }
      else
	if (tmp.getVariables().size() < newRelation.getVariables().size()) {           
	if (newRelation.isContained(this.triangulatedRelations.elementAt(j))) {
	  this.triangulatedRelations.removeRelation(tmp);
	  getOut = true;
	}
      }
      j++;
    }
    
    
    /* Copy the object newRelation because we need remove a 
       element of the Relation. Any modification in newRelation will
       modify the Relation introduced in triangulatedRelations */
    
    newRelationClone = newRelation.copy();
    newRelationClone.getVariables().removeNode(x);
    
    currentRelations.getRelations().addElement(newRelationClone);
    pairTable.addRelation(newRelationClone);  
  }
  
}


/**
 * Gets the triangulation and the deletion secuence of the net that
 * is store in <code>netToTriangulate</code> using the heuristic specified by
 * the argument criterion. If the argument list is not empty, gets a
 * constrained deletion sequence with the nodes of that list at the end
 * of the sequence.
 *
 * @param set a list of nodes (could be empty).
 * @param criterion the heuristic to be used.
 */

public void getTriangulation(NodeList set, String criterion) {
  
  NodeList notRemoved;
  FiniteStates x;
  Relation r, newRelation, newRelationClone, tmp;
  RelationList currentRelations, auxRelationList;  
  PairTable pairTable;
  int i, j, k, p, pos, s;
  boolean getOut;
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  s = netToTriangulate.getNodeList().size();
  
  for (i=0 ; i<s ; i++) {
    x = (FiniteStates)netToTriangulate.getNodeList().elementAt(i);         
    notRemoved.insertNode(x);
    pairTable.addElement(x);         
  }
  
  currentRelations = getInitialRelations(netToTriangulate); 
  
  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));
  
  
  for (i=notRemoved.size() ; i>0 ; i--) {
    if (set.size()>0)
      x = pairTable.nextToRemove(set,criterion);
    else
      x = pairTable.nextToRemove(criterion);
    
    auxRelationList = currentRelations.getRelationsOfAndRemove(x);
    notRemoved.removeNode(x);
    pairTable.removeVariable(x);
    
    newRelation = auxRelationList.elementAt(0);                   
    pairTable.removeRelation(newRelation);
    
    for (j=1 ; j<auxRelationList.size() ; j++) {
      r = auxRelationList.elementAt(j);
      combine(newRelation, r);
      pairTable.removeRelation(r);      
    }
    
    this.triangulatedNodes.insertNode(x);              
    this.triangulatedRelations.insertRelation(newRelation);
    
    
    /* Check that the relation inserted is not contained in another one and
       that the relation inserted is not contained in another one too. */
    
    j = 0;
    getOut = false;      
    
    while ((j<this.triangulatedRelations.size()-1) && (!getOut)) {
      tmp = this.triangulatedRelations.elementAt(j);
      
      if (tmp.getVariables().size() > newRelation.getVariables().size()) {           
	if (tmp.isContained(newRelation)) {
	  this.triangulatedRelations.removeRelation(newRelation);
	  getOut = true;
	}
      }
      else
	if (tmp.getVariables().size() < newRelation.getVariables().size()) {           
	if (newRelation.isContained(this.triangulatedRelations.elementAt(j))) {
	  this.triangulatedRelations.removeRelation(tmp);
	  getOut = true;
	}
      }
      j++;
    }
    
    
    /* Copy the object newRelation because we need remove a 
       element of the Relation. Any modification in newRelation will
       modify the Relation introduced in triangulatedRelations */
            
    newRelationClone = newRelation.copy();
    newRelationClone.getVariables().removeNode(x);
    
    currentRelations.getRelations().addElement(newRelationClone);
    pairTable.addRelation(newRelationClone);
  }  
}

/**
 * Gets the triangulation and the deletion secuence of the RelationList 
 * passed as parameter using the heuristic specified by
 * the argument criterion. If the argument list is not empty, gets a
 * constrained deletion sequence with the nodes of that list at the end
 * of the sequence.
 *
 *
 * !! Important: relations it is supposed to proceed from 
 * restrictToObservations method
 *
 * @param relations the <code>RelationList</code> which induces
 *                  the moral graph to be triangulated
 * @param set a list of nodes (could be empty).
 * @param criterion the heuristic to be used.
 */

public void getTriangulation(RelationList relations){
  getTriangulation(relations,new NodeList(),"CanoMoral");
}

public void getTriangulation(RelationList relations,
				NodeList set, String criterion) {
  
  NodeList notRemoved;
  FiniteStates x;
  Relation r, newRelation, newRelationClone, tmp;
  RelationList currentRelations, auxRelationList;  
  PairTable pairTable;
  int i, j, k, p, pos, s;
  boolean getOut;
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  // preparing the nodes to be triangulated and counting them
  
  for (i=0,s=0; i<relations.size() ; i++) {
    r = relations.elementAt(i);
    if (r.getKind() == Relation.CONDITIONAL_PROB){
      x = (FiniteStates) r.getVariables().elementAt(0);
      notRemoved.insertNode(x);
      pairTable.addElement(x);
      s++;   
    }      
  }
  
  // copying the relations to be triangulated
  currentRelations = relations.copy();
  
  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));
  
  
  for (i=notRemoved.size() ; i>0 ; i--) {
    if (set.size()>0)
      x = pairTable.nextToRemove(set,criterion);
    else
      x = pairTable.nextToRemove(criterion);
    
    auxRelationList = currentRelations.getRelationsOfAndRemove(x);
    notRemoved.removeNode(x);
    pairTable.removeVariable(x);
    
    newRelation = auxRelationList.elementAt(0);                   
    pairTable.removeRelation(newRelation);
    
    for (j=1 ; j<auxRelationList.size() ; j++) {
      r = auxRelationList.elementAt(j);
      combine(newRelation, r);
      pairTable.removeRelation(r);      
    }
    
    this.triangulatedNodes.insertNode(x);              
    this.triangulatedRelations.insertRelation(newRelation);
    
    
    /* Check that the relation inserted is not contained in another one and
       that the relation inserted is not contained in another one too. */
    
    j = 0;
    getOut = false;      
    
    while ((j<this.triangulatedRelations.size()-1) && (!getOut)) {
      tmp = this.triangulatedRelations.elementAt(j);
      
      if (tmp.getVariables().size() > newRelation.getVariables().size()) {           
	if (tmp.isContained(newRelation)) {
	  this.triangulatedRelations.removeRelation(newRelation);
	  getOut = true;
	}
      }
      else
	if (tmp.getVariables().size() < newRelation.getVariables().size()) {           
	if (newRelation.isContained(this.triangulatedRelations.elementAt(j))) {
	  this.triangulatedRelations.removeRelation(tmp);
	  getOut = true;
	}
      }
      j++;
    }
    
    
    /* Copy the object newRelation because we need remove a 
       element of the Relation. Any modification in newRelation will
       modify the Relation introduced in triangulatedRelations */
            
    newRelationClone = newRelation.copy();
    newRelationClone.getVariables().removeNode(x);
    
    currentRelations.getRelations().addElement(newRelationClone);
    pairTable.addRelation(newRelationClone);
  }  
}


/**
 * Performs the triangulation of the network according to the
 * deletion sequence stored in <code>triangulatedNodes</code>
 */

public void triangulate() {
 
  NodeList notRemoved;
  FiniteStates x;
  Relation r, newRelation, newRelationClone, tmp;
  RelationList currentRelations, auxRelationList;  
  PairTable pairTable;
  int i, j, k, p, pos, s, s2;
  boolean getOut;
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  s = netToTriangulate.getNodeList().size();
  
  for (i=0 ; i<s ; i++) {
    x = (FiniteStates)netToTriangulate.getNodeList().elementAt(i);         
    notRemoved.insertNode(x);
    pairTable.addElement(x);         
  }
  
  currentRelations = getInitialRelations(netToTriangulate); 
  
  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));
  
  s2 = notRemoved.size();
  for (i=notRemoved.size() ; i>0 ; i--) {
    x = (FiniteStates)triangulatedNodes.elementAt(s-i); // using the sequence
    
    auxRelationList = currentRelations.getRelationsOfAndRemove(x);
    notRemoved.removeNode(x);
    pairTable.removeVariable(x);
    
    newRelation = auxRelationList.elementAt(0);                   
    pairTable.removeRelation(newRelation);
    
    for (j=1 ; j<auxRelationList.size() ; j++) {
      r = auxRelationList.elementAt(j);
      combine (newRelation, r);
      pairTable.removeRelation(r);      
    }
    
    this.triangulatedRelations.insertRelation(newRelation);
    
    
    /* Check that the relation inserted is not contained in another one and
       that the relation inserted is not contained in another one too. */
    
    j = 0;
    getOut = false;      
    
    while ((j<this.triangulatedRelations.size()-1) && (!getOut)) {
      tmp = this.triangulatedRelations.elementAt(j);
      
      if (tmp.getVariables().size() > newRelation.getVariables().size()) {           
	if (tmp.isContained(newRelation)) {
	  this.triangulatedRelations.removeRelation(newRelation);
	  getOut = true;
	}
      }
      else
	if (tmp.getVariables().size() < newRelation.getVariables().size()) {           
	if (newRelation.isContained(this.triangulatedRelations.elementAt(j))) {
	  this.triangulatedRelations.removeRelation(tmp);
	  getOut = true;
	}
      }
      j++;
    }
    
    
    /* Copy the object newRelation because we need remove a 
       element of the Relation. Any modification in newRelation will
       modify the Relation introduced in triangulatedRelations */
    
    newRelationClone = newRelation.copy();
    newRelationClone.getVariables().removeNode(x);
    
    currentRelations.getRelations().addElement(newRelationClone);
    pairTable.addRelation(newRelationClone);
  }
}


/**
 * Obtains a list of the cliques in the network. The heuristic used
 * is the one-step look ahead minimum size criterium, where
 * variables contained in only one relation are eliminated first.
 * @return a <code>RelationList</code> corresponding to the cliques.
 */

public RelationList getCliques() {

  NodeList notRemoved;
  Node x;
  RelationList currentRelations, cliques;  
  PairTable pairTable;
  int i, s;
  
  cliques = new RelationList();
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  s = netToTriangulate.getNodeList().size();
  
  for (i=0 ; i<s ; i++) {
    x = (Node)netToTriangulate.getNodeList().elementAt(i);         
    notRemoved.insertNode(x);
    pairTable.addElement(x);         
  }
  
  currentRelations = getInitialRelations(netToTriangulate); 
  
  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));
  
  for (i=notRemoved.size() ; i>0 ; i--) {
    x = pairTable.nextToRemove();
    removeNode(x,notRemoved,currentRelations,cliques,pairTable);  
  }
  
  return cliques;
}

/**
 * Obtains a list of the cliques in the moral subgraph of the network 
 * induced by the relation list passed as parameter (in fact, only
 * those relation of type conditional_prob are considered.
 * The heuristic used
 * is the one-step look ahead minimum size criterium, where
 * variables contained in only one relation are eliminated first.
 *
 * @param relations a <code>RelationList</code> which generally
 * comes from a restricToObservations process 
 *
 * @return a <code>RelationList</code> corresponding to the cliques.
 */

public RelationList getCliques(RelationList relations) {

  NodeList notRemoved;
  Node x;
  RelationList currentRelations, cliques;  
  PairTable pairTable;
  int i, s;
  Relation r;
  
  cliques = new RelationList();
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  // preparing the nodes to be triangulated and counting them
  
  for (i=0,s=0; i<relations.size() ; i++) {
    r = relations.elementAt(i);
    if (r.getKind() == Relation.CONDITIONAL_PROB){
      x = (FiniteStates) r.getVariables().elementAt(0);
      notRemoved.insertNode(x);
      pairTable.addElement(x);
      s++;   
    }      
  }

  currentRelations = relations.copy();

  
  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));
  
  for (i=notRemoved.size() ; i>0 ; i--) {
    x = pairTable.nextToRemove();
    removeNode(x,notRemoved,currentRelations,cliques,pairTable);  
  }
  
  return cliques;
}


/**
 * Obtains a list of the cliques in the network. The heuristic used
 * is the one-step look ahead minimum size criterium, where
 * variables contained in only one relation are eliminated first.
 * Variables in the given evidence are removed from the initial
 * relations in the network, and they are not considered for
 * elimination. It means that the triangulation depends on the
 * observations.
 * @param ev a set of observations.
 * @return a <code>RelationList</code> corresponding to the cliques.
 */

public RelationList getCliques(Evidence ev) {

  NodeList notRemoved;
  Node x;
  RelationList currentRelations, cliques;  
  PairTable pairTable;
  int i, s;
  
  cliques = new RelationList();
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  s = netToTriangulate.getNodeList().size();
  
  for (i=0 ; i<s ; i++) {
    x = (FiniteStates)netToTriangulate.getNodeList().elementAt(i);
    if (!ev.isObserved(x)) {
      notRemoved.insertNode(x);
      pairTable.addElement(x);
    }
  }
  
  currentRelations = getInitialRelations(netToTriangulate); 
  currentRelations.restrictToObservations(ev);
  
  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));
  
  for (i=notRemoved.size() ; i>0 ; i--) {
    x = pairTable.nextToRemove();
    removeNode(x,notRemoved,currentRelations,cliques,pairTable);  
  }
  
  return cliques;
}

/**
 * Obtains a list of the cliques in the network. The heuristic used
 * is the one-step look ahead minimum size criterium, where
 * variables contained in only one relation are eliminated first.
 * @return a <code>RelationList</code> corresponding to the cliques.
 */

public RelationList continuousGetCliques() {

  NodeList notRemoved;
  Node x;
  RelationList currentRelations, cliques;  
  PairTable pairTable;
  int i, s;
  
  cliques = new RelationList();
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  s = netToTriangulate.getNodeList().size();
  
  for (i=0 ; i<s ; i++) {
    x = (Node)netToTriangulate.getNodeList().elementAt(i);  
    notRemoved.insertNode(x);
    pairTable.addElement(x);
    
    
  }
  
  currentRelations = continuousGetInitialRelations(netToTriangulate); 
  
  for (i=0 ; i<currentRelations.size() ; i++){
    //System.out.println("Estamos en Triangulation, en continuousGetCliques, este el el currentRelations:");
    //currentRelations.elementAt(i).print();
    pairTable.addRelation(currentRelations.elementAt(i));
    
  }
  for (i=notRemoved.size() ; i>0 ; i--) {
      //System.out.println("Estamos formando el clique "+i);
      x = (Node)pairTable.nextToRemoveCont();
      
      //System.out.println("Estoy en ContinuousTriangulation. Este es el siguiente a eliminar:");
      //x.print();
      removeNodeCont(x,notRemoved,currentRelations,cliques,pairTable);  
  }
  
  return cliques;
}


/**
 * Obtains a list of the cliques in the network adapted to
 * the initial evidence of the network. The heuristic used
 * is the one-step look ahead minimum size criterium, where
 * variables contained in only one relation are eliminated first.
 * Variables in the given evidence are removed from the initial
 * relations in the network. The elimination sequence starts
 * from the leaves, eliminating nodes without observed
 * predecessors. When a variable is removen from a conditional
 * distribution, the relation is not considered again.
 * @param ev a set of observations.
 * @param ev is the initial evidence.
 * @return a <code>RelationList</code> corresponding to the cliques.
 */

public RelationList getCliquesConditional(Evidence ev) {
  
  NodeList notRemoved;
  Node x;
  RelationList auxRelationList, auxRelationList2, currentRelations, cliques;  
  PairTable pairTable;
  int i, j, s;
  boolean introInPT;
  Relation newRelation, newRelationClone, r;
     
  cliques = new RelationList();
  
  notRemoved = new NodeList();
  pairTable = new PairTable();
  
  s = netToTriangulate.getNodeList().size();
  
  for (i=0 ; i<s ; i++) {
    //x = (FiniteStates)netToTriangulate.getNodeList().elementAt(i);         
    x = (Node)netToTriangulate.getNodeList().elementAt(i); 
    if (!ev.isObserved(x)) {
      notRemoved.insertNode(x);
      pairTable.addElement(x);  
    }
  }
  
  currentRelations = getInitialRelations(netToTriangulate);
  
  // DELETING OBSERVATIONS
  // Initially, make all the relations active(conditionals) 
  // (by default they are)
  // In those relations in which the observed variable (the one being
  // removed) is the conditioned one (the first one), the resulting
  // relation will be inactive.
  // In the conditional (active) relations where we remove other variable
  // different of the first one, the result is active.
  // All the relations will be stored in the <code>PairTable</code>
  currentRelations.restrictToObservations(ev);
  
  // INITIALIZING THE PAIRTABLE
  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));  
  
  // DELETING VARIABLES     
  for (i=notRemoved.size() ; i>0 ; i--) {
    
    x = pairTable.nextToRemove(); 
    auxRelationList = currentRelations.getRelationsOfAndRemove(x);
    auxRelationList2 = new RelationList();
    auxRelationList2.setRelations(pairTable.getRelations(x));
    notRemoved.removeNode(x);
    pairTable.removeVariable(x);
    
    if (auxRelationList2.size() > 0) {
      newRelation = auxRelationList2.elementAt(0); 
      
      if (auxRelationList2.size() > 1) {
	// When we delete a variable that is in more than one
	// relation, the result will be a POTENTIAL and will be
	// stored in the PairTable.
	newRelation.setKind(Relation.POTENTIAL);
	introInPT = true;
	
      }
      else if((newRelation.getKind()==Relation.CONDITIONAL_PROB) &&
	      (newRelation.getVariables().getId(x)==0)) {
	// When in a relation CONDITIONAL_PROB we delete the first
	// variable, and this one is just in this relation, the
	// result will be a POTENTIAL, and will not be stored in
	// the PairTable. 
	newRelation.setKind(Relation.POTENTIAL);
	introInPT = false;
	
      }
      else {
	// When in a relation CONDITIONAL_PROB we delete a variable,
	// and this one is just in this relation, the result will
	// be CONDITIONAL_PROB, and will be stored in the PairTable. 
	newRelation.setKind(Relation.CONDITIONAL_PROB);
	introInPT = true;	
      }
      
      pairTable.removeRelation(newRelation);
      
      for (j=1 ; j<auxRelationList2.size() ; j++) {
	//r = auxRelationList.elementAt(j);
	r = auxRelationList2.elementAt(j);
	combine (newRelation, r);
	pairTable.removeRelation(r);      
      }
      
      newRelationClone = newRelation.copy();
      newRelationClone.getVariables().removeNode(x);
      
      if (introInPT)
	pairTable.addRelation(newRelationClone); 
    }
    
    if (auxRelationList.size() > 0) {
      newRelation = auxRelationList.elementAt(0); 
      
      for (j=1 ; j<auxRelationList.size() ; j++) {
	r = auxRelationList.elementAt(j);
	combine(newRelation, r);     
      }
      
      
      cliques.insertRelation(newRelation);
      
      
      
      newRelationClone = newRelation.copy();
      newRelationClone.getVariables().removeNode(x);
      
      currentRelations.getRelations().addElement(newRelationClone);
      
       
    }
  }
  
  return cliques;
}


/**
 * Puts all the variables of two relations in one of them.
 * @param rMain one of the relations. It will contain the 
 * relation with the final combination.
 * @param r the other relation to combine.
 */

public void combine (Relation rMain, Relation r) {
  
  int i;      
  
  for (i=0 ; i<r.getVariables().size() ; i++) {
    if (!rMain.isInRelation(r.getVariables().elementAt(i))) 
      rMain.getVariables().insertNode(r.getVariables().elementAt(i));
  }
}         


/**
 * Carries out the operations needed to delete a node when we
 * are getting the cliques from a <code>Bnet</code>. This method is used
 * by getCliques().
 * @see getCliques()
 * @param x is the node to be deleted
 * @param notRemoved is the list of not removed nodes. After this
 * method, node <code>x</code> will be deleted of this list.
 * @param currentRelations is the list of current relations that 
 * contains only non deleted nodes. After this method the 
 * relations containing node <code>x</code> will be replaced
 * by a relation with all the variables of such relations.
 * @param pairTable allows to recover the relations in which is every non 
 * deleted node.
 */

private void removeNode(Node x, NodeList notRemoved,
			RelationList currentRelations,
			RelationList cliques, PairTable pairTable) {
  
  int j;
  
  RelationList auxRelationList;
  Relation newRelation, newRelationClone, r;
  
  auxRelationList = currentRelations.getRelationsOfAndRemove(x);
  notRemoved.removeNode(x);
  pairTable.removeVariable(x);
  
  newRelation = auxRelationList.elementAt(0);                   
  pairTable.removeRelation(newRelation);
  
  for (j=1 ; j<auxRelationList.size() ; j++) {
    r = auxRelationList.elementAt(j);
    combine (newRelation, r);
    pairTable.removeRelation(r);      
  }
     
  cliques.insertRelation(newRelation);
  
  /* Copy the object newRelation because we need remove a 
     element of the Relation. Any modification in newRelation will
     modify the Relation introduced in cliques */
  
  newRelationClone = newRelation.copy();
  newRelationClone.getVariables().removeNode(x);
  
  currentRelations.getRelations().addElement(newRelationClone);
  pairTable.addRelation(newRelationClone);   
}


/**
 * Restricts a list of relations to the observations.
 * @param relationList the list of relations to restrict.
 */

public void restrictToObservations(RelationList relationList) {

  Relation r;
  int i, s;
  
  if (observations.size() > 0) {
    s = relationList.size();
    
    for (i=0 ; i<s ; i++) {
      r = relationList.elementAt(i);
      r.setValues (((PotentialTree)r.getValues()).restrictVariable(observations));
      r.getVariables().setNodes (r.getValues().getVariables());
    }
  }
}


/**
 * Obtains the numeration by maximum cardinality for the list of nodes
 * contained in <code>triangulatedNodes</code>.
 * @return The list of ordered nodes.
 * @see Triangulation#maximumNeighbours
 */

public NodeList maximumCardinality() {
  
  NodeList visited,    // Containsthe list of visited nodes.
           tmp;        // A copy of the ordered variables.
  int i, j, k = 0, numberOfNodes;
   
  numberOfNodes = this.triangulatedNodes.size();  
  tmp = new NodeList();
  tmp.setNodes ((Vector) this.triangulatedNodes.getNodes().clone());      
  
  visited = new NodeList();
  visited.insertNode (tmp.elementAt(0));
  tmp.removeNode(tmp.elementAt(0));
  
  for (i=1 ; i<numberOfNodes ; i++) {
    visited.insertNode (this.maximumNeighbours (tmp,visited));                    
  }
  
  return visited;
}


/**
 * Obtains the numeration for the list of nodes contained in
 * <code>triangulatedNodes</code> by maximum cardinality search.
 * The following restrictions are applied:
 *     - the first node is a node belonging to the argument list.
 *     - if two nodes are tied, we break the tie in favour of the node
 *       belongin to the argument list (if possible)
 *  @param set a list of nodes containing a set of relevants nodes,
 *  @return The list of ordered nodes.
 */

public NodeList maximumCardinalitySearch(NodeList set) {
  
  NodeList visited,    // the list to be returned  
           neighbours; // the neighbours for a node
  Node node;        
  int i, j, k, numberOfNodes, next;
  int numNeighbours[]; // contains in position i the number of 
                       // numbered neighbours for the node
                       // triangulatedNodes.elementAt(i)
                       // if the value is -1 the node not will be 
                       // considered
  
  numberOfNodes = this.triangulatedNodes.size();  
  numNeighbours = new int[numberOfNodes];
  for (i=0 ; i<numberOfNodes ; i++) 
    numNeighbours[i]=0;
  
  visited = new NodeList();
  
  // we choose a node beloging to set as the first node. We begin by
  // the end of triangulated nodes because if the triangulation has been
  // obtained respecto to set, then the last node of triangulatednodes 
  // will be a node of set
  
  next = 0; // initialization of next
  if (set.size() > 0) {
    for (i=numberOfNodes-1 ; i>=0 ; i--) {
      node = this.triangulatedNodes.elementAt(i);
      if (set.getId(node) != -1) {
	next = i;
	break;
      }
    }
  }
  
  // begining the numeration process
  
  for (i=0 ; i<numberOfNodes ; i++) {
    // inserting the node and setting its value to -1
    visited.insertNode(triangulatedNodes.elementAt(next));
    numNeighbours[next] = -1;
    // getting a nodelist with all the nodes that appear with next in 
    // some relation
    neighbours = this.neighbourhood(next);
    // for all node in neighbours do numberofneigbours++
    for (j=0 ; j<neighbours.size() ; j++){
      node = neighbours.elementAt(j); 
      k = triangulatedNodes.getId(node);
      if (k != -1) {
	if (numNeighbours[k] != -1)
	  numNeighbours[k]++;
      }
      else {
	System.out.println("We have missed a node!!!!");}
    }
    
    // getting the position of the next node to be numbered
    next = 0;
    for (j=1 ; j<numberOfNodes ; j++) {
      if (numNeighbours[j]>numNeighbours[next])
	next = j;
      else if (set.size() > 0) {
	if (numNeighbours[j] == numNeighbours[next])
	  if (set.getId(this.triangulatedNodes.elementAt(j)) != -1)
	    next = j;
      }
    }
    
  }        
  
  return visited;
}


/**
 * Obtains the numeration for the list of nodes contained in
 * <code>triangulatedNodes</code> by maximum cardinality search.
 * Calls the previous method with an empty list.
 *
 *  @return The list of ordered nodes.
 */

public NodeList maximumCardinalitySearch() {
  
  NodeList nl = new NodeList();
  
  return maximumCardinalitySearch(nl);
}


/**
 * This method returns the node with most neighbours visited previously       
 * @param order the list of the nodes not visited yet
 * @param visited the list of the nodes already visited
 * @return The node with the highest number of neighbours
 * @see Triangulation#neighbourhood
 */

public Node maximumNeighbours (NodeList order, NodeList visited)  {
  
  int neighbour = 0, higher = 0, index = 0, i, j, k;
  NodeList neighbours = new NodeList();
  Node element;
  
  /* Looking for the node with most neighbours numerated */
  
  for (i=0 ; i<order.size() ; i++) {
    neighbours = neighbourhood (order.elementAt(i));
    neighbour = 0;
    for (k=0 ; k<neighbours.size() ; k++) {
      if (visited.getId(neighbours.elementAt(k).getName()) != -1) 
	neighbour++;
    }
    
    /* Find a node with a higher number of neighbours visited */
    
    if (neighbour > higher) {
      index = i;
      higher = neighbour;
    }
  }
  
  element = order.elementAt(index);
  order.removeNode(index);
  
  return element;
}


/**
 * This method returns the list of the neighbours of a node 
 * @param n the node whose neighbours are being searched. 
 * @return a list of neighbours of <code>n</code>
 */

public NodeList neighbourhood(Node n) {
  
  int numberOfNodes, i, j, p, q;
  NodeList neighbours = new NodeList();
  Relation r;
  
  numberOfNodes = this.triangulatedRelations.size();
  
  for (i=0 ; i<numberOfNodes ; i++) {
    r = (this.triangulatedRelations.elementAt(i)).copy();
    p = r.getVariables().getId(n);
    
    if (p != -1) {                 
      r.getVariables().removeNode(n);
      
      for (j=0 ; j<r.getVariables().size() ; j++) {
	q = neighbours.getId(r.getVariables().elementAt(j));
	if (q == -1)
	  neighbours.insertNode(r.getVariables().elementAt(j));
      }
      
    }
    
  }
  
  return neighbours;    
}


/**
 * This method returns the list of neighbours for the node
 * whose index in <code>triangulatedNodes<code> is passed as parameter.
 * @param pos the position of the node whose neighbours are wanted. 
 * @return the list of neighbours of the node of interest.
 */

public NodeList neighbourhood(int pos) {
  
  NodeList neighbours = new NodeList();
  int i, numberOfNodes;
  Node node;
  Relation r;
  
  numberOfNodes = this.triangulatedRelations.size();
  node = this.triangulatedNodes.elementAt(pos);
  
  for (i=0 ; i<numberOfNodes ; i++) {
    r = this.triangulatedRelations.elementAt(i);
    if (r.getVariables().getId(node) != -1){
      neighbours.join(r.getVariables());
    }
  }
  
  return neighbours;
}


/**
 * Compares two relations by the order of the variables.
 * @param r1 the first relation to compare.
 * @param r2 the second relation to compare.
 * @param nodeOrder the list of nodes indicating the order of
 * the variables.
 * @param pos the position from which <code>nodeOrder</code> is considered
 * @return <code>true</code> if <code>Relation r1</code> is lower
 * than <code>Relation r2</code>, with respect to the order stablished
 * by <code>nodeOrder</code>, from position <code>pos</code> on.
 */

public boolean lower(Relation r1, Relation r2, NodeList nodeOrder,
		     int pos) {
  
  int i, p1, p2;
  Node node;
  
  for (i=pos+1 ; i<nodeOrder.size() ; i++) {
    node = nodeOrder.elementAt(i);
    p1 = r1.getVariables().getId(node.getName());
    p2 = r2.getVariables().getId(node.getName());
    
    if ((p1!=-1) && (p2==-1))
      return true;
    if ((p1==-1) && (p2!=-1))
      return false;
  }
  
  System.out.println("Triangulation.lower: error ...");
  return false; 
}


/**
 * This method numerates the cliques according to the node of lower order.
 * @param nodeOrder the list of nodes in order.
 * @return A list of cliques (relations) in order according the numeration
 * obtained.
 */

public RelationList numerateCliques (NodeList nodeOrder) {

  RelationList numeration, relationCopy, tmp;
  FiniteStates x;
  int i, j, k;
  
  relationCopy = new RelationList();
  tmp = new RelationList();
  numeration = new RelationList();
  x = new FiniteStates();
  Relation aux;      
  
  relationCopy.setRelations((Vector)
			    this.triangulatedRelations.getRelations().clone());
  
  for (i=0 ; i<nodeOrder.size() ; i++) {
    x = (FiniteStates) nodeOrder.elementAt(i);
    tmp = relationCopy.getRelationsOfAndRemove(x);
    
    // if tmp contains more than one relation, the list 
    // has to be sorted considering the other nodes
    
    if (tmp.size() > 1) { // sorting by selection
      
      for (j=0 ; j < tmp.size()-1 ; j++) {
	int posLower;
	Relation relLower;
	
	posLower = j;
	relLower = tmp.elementAt(j);
	
	for (k=j+1 ; k<tmp.size() ; k++)
	  if (lower(tmp.elementAt(k),relLower,nodeOrder,i)) {
	  posLower = k;
	  relLower = tmp.elementAt(k);
	}
	
	tmp.setElementAt(tmp.elementAt(j),posLower);
	tmp.setElementAt(relLower,j);
      }
      
    }
    
    // introducing the relations in numeration
    for (j=0 ; j<tmp.size() ; j++) 
      numeration.insertRelation(tmp.elementAt(j));         
  }
  
  return numeration;
}


/**
 * Obtains the separators of a list of ordered cliques (relations).
 * @param orderedCliques a list of cliques ordered according to
 * its numeration
 * @return a list of Separators 
 * @see Triangulation#union
 * @see Triangulation#intersection
 */

public RelationList getSeparators(RelationList orderedCliques) {
  
  int i;
  RelationList separators = new RelationList();
  Relation r;
  
  separators.insertRelation (new Relation());
  r = orderedCliques.elementAt(0).copy();
  
  for (i=1 ; i<orderedCliques.size() ; i++) {
    union (r, orderedCliques.elementAt(i-1));      
    separators.insertRelation (intersection (r, orderedCliques.elementAt(i)));
  }
  
  return separators;  
}   


/**
 * This method obtains the union between two relations. 
 * @param r1 The first relation. When the method finishes, this relation
 * contains the union with the second relation.
 * @param r2 The second relation.
 */

public void union(Relation r1, Relation r2) {
  
  int j, q;
  for (j=0 ; j<r2.getVariables().size() ; j++) {
    q = r1.getVariables().getId(r2.getVariables().elementAt(j));
    if (q == -1)
      r1.getVariables().insertNode(r2.getVariables().elementAt(j));
  }  
}


/**
 * Calculates the intersection between two relations.
 * @param r1 The first relation.
 * @param r2 The other relation.
 * @return The <code>relation</code> with the intersection of
 * <code>r1</code> and  <code>r2</code>.
 */

public Relation intersection (Relation r1, Relation r2) {

  Relation intersection = new Relation();
  int i = 0, j, sizel1, sizel2;
  boolean getOut;
    
  sizel1 = r1.getVariables().size();
  sizel2 = r2.getVariables().size();
  
  /* The first while controls the nodes of the first relation */
  
  while (i < sizel1) {
    getOut = false;
    j = 0;
    
    /* Checks whether the current node of the first list is in the second
       with the next while */
    
    while ((!getOut) && (j<sizel2)) {
      if (r1.getVariables().elementAt(i).getName().equals(r2.getVariables().elementAt(j).getName())){
	intersection.getVariables().insertNode(r1.getVariables().elementAt(i));
	getOut = true;
      }
      j++;
    }
    i++;    
  }
  return intersection;
}

/**
 * Puts all the variables of two relations in one of them.
 * @param rMain one of the relations. It will contain the 
 * relation with the final combination, and the product of splits and numterms
 * @param r the other relation to combine.
 */

public void combineCont (Relation rMain, Relation r) {
  
  int i;      
  PotentialContinuousPT potMain, pot;
  
  for (i=0 ; i<r.getVariables().size() ; i++) {
    if (!rMain.isInRelation(r.getVariables().elementAt(i))) 
      rMain.getVariables().insertNode(r.getVariables().elementAt(i));
  }
  potMain = (PotentialContinuousPT)rMain.getValues();
  pot = (PotentialContinuousPT)r.getValues();
  
  potMain.setNumTerms(potMain.getNumTerms()*pot.getNumTerms());
  potMain.setNumSplits(potMain.getNumSplits()*pot.getNumSplits());
  
}         

/**
 * Carries out the operations needed to delete a node when we
 * are getting the cliques from a <code>Bnet</code>. This method is used
 * by getCliques().
 * @see getCliques()
 * @param x is the node to be deleted
 * @param notRemoved is the list of not removed nodes. After this
 * method, node <code>x</code> will be deleted of this list.
 * @param currentRelations is the list of current relations that 
 * contains only non deleted nodes. After this method the 
 * relations containing node <code>x</code> will be replaced
 * by a relation with all the variables of such relations.
 * @param pairTable allows to recover the relations in which is every non 
 * deleted node.
 */

private void removeNodeCont(Node x, NodeList notRemoved,
			RelationList currentRelations,
			RelationList cliques, PairTable pairTable) {
  
  int j;
  
  RelationList auxRelationList;
  Relation newRelation, newRelationClone, r;
  
  auxRelationList = currentRelations.getRelationsOfAndRemove(x);
  notRemoved.removeNode(x);
  pairTable.removeVariable(x);
  
  newRelation = auxRelationList.elementAt(0);                   
  pairTable.removeRelation(newRelation);
  
  for (j=1 ; j<auxRelationList.size() ; j++) {
    r = auxRelationList.elementAt(j);
    combineCont (newRelation, r);
    pairTable.removeRelation(r);      
  }
     
  cliques.insertRelation(newRelation);
  
  /* Copy the object newRelation because we need remove a 
     element of the Relation. Any modification in newRelation will
     modify the Relation introduced in cliques */
  
  newRelationClone = newRelation.copy();
  newRelationClone.getVariables().removeNode(x);
  
  currentRelations.getRelations().addElement(newRelationClone);
  pairTable.addRelation(newRelationClone);   
}


}  // end of class
