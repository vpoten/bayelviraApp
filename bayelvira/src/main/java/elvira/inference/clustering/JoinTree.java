/* JoinTree.java */

package elvira.inference.clustering;

import elvira.*;
import elvira.tools.JoinTreeStatistics;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Random;
import java.io.*;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialTable;
import elvira.potential.ProbabilityTree;
import elvira.potential.PotentialMTree;
import elvira.potential.PotentialFunction;
import elvira.potential.CanonicalPotential;
import elvira.potential.ListPotential;
import java.lang.Math;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.ContinuousProbabilityTree;

import elvira.sensitivityAnalysis.GeneralizedPotentialTable;//Introducido por jruiz

/**
 * This class implements the structure necessary for creating
 * a Join Tree. Contains the methods necessary to use
 * this structure.
 *
 * 
 * @author Andres Cano
 * @author Antonio Salmeron
 * @author Jose A. Gamez
 * @author Julia Flores
 * 
 * @since 30/06/2003
 */

public class JoinTree {

/**
 * Contains the list of the nodes of the Joint Tree, of class
 * <code>NodeJoinTree</code>.
 */

protected Vector joinTreeNodes;

/**
 * Data about the size of the join tree.
 */

protected JoinTreeStatistics statistics;

/**
 * Values to be used in the prunning of the potentials included in the
 * join tree in methods that manipulate the graphic structure and the
 * probability values stored in the nodes (like outerRestriction, ...)
 */
private double limitForPotentialPrunning = 0;
private double lowLimitForPrunning = 0;
private int maximumSizeForPotentialPrunning = 2147483647; // maxint
private boolean applySortAndBound = false;

/**
 * A flag indicating if this join tree is in fact a MPST (maximal 
 * prime subgraph tree)
 * By default its value is false
 */

boolean isMPST=false;

/**
 * Constructor. Creates a empty join tree .
 */

public JoinTree() {

  joinTreeNodes = new Vector();
  statistics = new JoinTreeStatistics();
}




/**
 * Constructor. Creates a new join tree for a given Bayesian network.
 * @param b a <code>Bnet</code>.
 */

public JoinTree(Bnet b) {

  RelationList cliques;
  Relation currentRel, inter, variablesInserted;
  NodeJoinTree n1, n2;
  Triangulation t;
  boolean found;
  int i, j;

  joinTreeNodes = new Vector();
  statistics = new JoinTreeStatistics();

  t = new Triangulation(b);

  cliques = t.getCliques();

  currentRel = cliques.elementAt(cliques.size()-1);
  n1 = makeNodeJoinTree(currentRel);

  variablesInserted = new Relation(currentRel.getVariables().toVector());
  insertNodeJoinTree(n1);


  for (i=(cliques.size()-2) ; i>=0 ; i--) {
    currentRel = cliques.elementAt(i);
    inter = variablesInserted.intersection(currentRel);

    // Now look for one node containing the intersection.
    found = false;
    j = 0;

    while ((!found) && (j<joinTreeNodes.size())) {
      n1 = elementAt(j);
      if (n1.getNodeRelation().isContained(inter))
	found = true;
      else
	j++;
    }
    if (!found)
      System.out.println("ERROR");

    n2 = makeNodeJoinTree(currentRel);
    insertNodeJoinTree(n2);

    /*n2.insertNeighbour(n1);
    n1.insertNeighbour(n2);*/
    insertNeighbour(n1,n2);

    variablesInserted.union(currentRel);
  }
}


/**
 * Constructor. Creates a new join tree for a given Bayesian network,
 * but only taking into account the moral graph induced by the relation
 * list passed as parameter. It is supposed that this relationlist comes
 * from a restrictToObservation process, so only conditional_prob relations
 * will be considered
 * 
 *
 * @param b a <code>Bnet</code>.
 * @param relations a <code>RelationList</code>
 */

public JoinTree(Bnet b, RelationList relations) {

  RelationList cliques;
  Relation currentRel, inter, variablesInserted;
  NodeJoinTree n1, n2;
  Triangulation t;
  boolean found;
  int i, j;

  joinTreeNodes = new Vector();
  statistics = new JoinTreeStatistics();

  t = new Triangulation(b);

  cliques = t.getCliques(relations);

  currentRel = cliques.elementAt(cliques.size()-1);
  n1 = new NodeJoinTree(currentRel);

  variablesInserted = new Relation(currentRel.getVariables().toVector());
  insertNodeJoinTree(n1);


  for (i=(cliques.size()-2) ; i>=0 ; i--) {
    currentRel = cliques.elementAt(i);
    inter = variablesInserted.intersection(currentRel);

    // Now look for one node containing the intersection.
    found = false;
    j = 0;

    while ((!found) && (j<joinTreeNodes.size())) {
      n1 = elementAt(j);
      if (n1.getNodeRelation().isContained(inter))
	found = true;
      else
	j++;
    }
    if (!found)
      System.out.println("ERROR");

    n2 = new NodeJoinTree(currentRel);
    insertNodeJoinTree(n2);

    n2.insertNeighbour(n1);
    n1.insertNeighbour(n2);

    variablesInserted.union(currentRel);
  }
}


/**
 * Constructor. Creates a new join tree for a given Bayesian network.
 * @param b a <code>Bnet</code>.
 * @param fl a flag indicating if the network is an MTE network or not. (1 or 0)
 */

public JoinTree(Bnet b, int fl) {

  RelationList cliques;
  Relation currentRel, inter, variablesInserted;
  NodeJoinTree n1, n2;
  Triangulation t;
  boolean found;
  int i, j;
 
  joinTreeNodes = new Vector();
  statistics = new JoinTreeStatistics();

  t = new Triangulation(b);

  if (fl == 1 )
    cliques = t.continuousGetCliques();
  else 
    cliques = t.getCliques();
  
  currentRel = cliques.elementAt(cliques.size()-1);
  n1 = new NodeJoinTree(currentRel);

  variablesInserted = new Relation(currentRel.getVariables().toVector());
  insertNodeJoinTree(n1);


  for (i=(cliques.size()-2) ; i>=0 ; i--) {
    currentRel = cliques.elementAt(i);
    inter = variablesInserted.intersection(currentRel);

    // Now look for one node containing the intersection.
    found = false;
    j = 0;

    while ((!found) && (j<joinTreeNodes.size())) {
      n1 = elementAt(j);
      if (n1.getNodeRelation().isContained(inter))
	found = true;
      else
	j++;
    }
    if (!found)
      System.out.println("ERROR");

    n2 = new NodeJoinTree(currentRel);
    insertNodeJoinTree(n2);

    n2.insertNeighbour(n1);
    n1.insertNeighbour(n2);

    variablesInserted.union(currentRel);
  }
}

/**
 * Constructor. Creates a new join tree for a given Bayesian network.
 * by using set to constraint the deletion sequence
 * @param b a <code>Bnet</code>.
 * @param set a <code>NodeList</code>.
 */

public JoinTree(Bnet b, NodeList set) {

  RelationList cliques;
  Relation currentRel, inter, variablesInserted;
  NodeJoinTree n1, n2;
  Triangulation t;
  boolean found;
  int i, j;

  joinTreeNodes = new Vector();
  statistics = new JoinTreeStatistics();

  t = new Triangulation(b);
  t.getTriangulation(set);

  //cliques = t.getCliques();
  cliques = t.numerateCliques(t.maximumCardinalitySearch(set));

  currentRel = cliques.elementAt(cliques.size()-1);
  n1 = new NodeJoinTree(currentRel);

  variablesInserted = new Relation(currentRel.getVariables().toVector());
  insertNodeJoinTree(n1);


  for (i=(cliques.size()-2) ; i>=0 ; i--) {
    currentRel = cliques.elementAt(i);
    inter = variablesInserted.intersection(currentRel);

    // Now look for one node containing the intersection.
    found = false;
    j = 0;

    while ((!found) && (j<joinTreeNodes.size())) {
      n1 = elementAt(j);
      if (n1.getNodeRelation().isContained(inter))
	found = true;
      else
	j++;
    }
    if (!found)
      System.out.println("ERROR");

    n2 = new NodeJoinTree(currentRel);
    insertNodeJoinTree(n2);

    n2.insertNeighbour(n1);
    n1.insertNeighbour(n2);

    variablesInserted.union(currentRel);
  }
}


/**
 * Constructor. Creates a new Join Tree for a given network and a
 * some evidence.
 * @param b a <code>Bnet</code>.
 * @param ev the <code>Evidence</code>.
 */

public JoinTree(Bnet b, Evidence ev) {

  RelationList cliques;
  Relation currentRel, inter, variablesInserted;
  NodeJoinTree n1, n2;
  Triangulation t;
  boolean found;
  int i, j;

  joinTreeNodes = new Vector();
  statistics = new JoinTreeStatistics();

  t = new Triangulation(b);

  cliques = t.getCliquesConditional(ev);

  currentRel = cliques.elementAt(cliques.size()-1);
  n1 = new NodeJoinTree(currentRel);

  variablesInserted = new Relation(currentRel.getVariables().toVector());
  insertNodeJoinTree(n1);


  for (i=(cliques.size()-2) ; i>=0 ; i--) {
    currentRel = cliques.elementAt(i);
    inter = variablesInserted.intersection(currentRel);

    // Now look for one node containing the intersection.
    found = false;
    j = 0;

    while ((!found) && (j<joinTreeNodes.size())) {
      n1 = elementAt(j);
      if (n1.getNodeRelation().isContained(inter))
	found = true;
      else
	j++;
    }
    if (!found)
      System.out.println("ERROR");

    n2 = new NodeJoinTree(currentRel);
    insertNodeJoinTree(n2);

    n2.insertNeighbour(n1);
    n1.insertNeighbour(n2);

    variablesInserted.union(currentRel);
  }
}


/**
 * Constructor. Creates a new Join Tree for a given network and a
 * some evidence.
 * @param b a <code>Bnet</code>.
 * @param ev the <code>Evidence</code>.
 * @param triangMethod indicated the triangulation to carry out.
 * <ol>
 * <li> 0 is for considering evidence during the triangulation.
 * <li> 1 is for considering evidence and directly remove relations
 * that are conditional distributions when the conditioned variable
 * is removed.
 * </ol>
 */

public JoinTree(Bnet b, Evidence ev, int triangMethod) {

  RelationList cliques;
  Relation currentRel, inter, variablesInserted;
  NodeJoinTree n1, n2;
  Triangulation t;
  boolean found;
  int i, j;

  joinTreeNodes = new Vector();
  statistics = new JoinTreeStatistics();

  t = new Triangulation(b);

  if (triangMethod == 0)
    cliques = t.getCliques(ev);
  else
    cliques = t.getCliquesConditional(ev);

  currentRel = cliques.elementAt(cliques.size()-1);
  n1 = new NodeJoinTree(currentRel);

  variablesInserted = new Relation(currentRel.getVariables().toVector());
  insertNodeJoinTree(n1);


  for (i=(cliques.size()-2) ; i>=0 ; i--) {
    currentRel = cliques.elementAt(i);
    inter = variablesInserted.intersection(currentRel);

    // Now look for one node containing the intersection.
    found = false;
    j = 0;

    while ((!found) && (j<joinTreeNodes.size())) {
      n1 = elementAt(j);
      if (n1.getNodeRelation().isContained(inter))
	found = true;
      else
	j++;
    }
    if (!found)
      System.out.println("ERROR");

    n2 = new NodeJoinTree(currentRel);
    insertNodeJoinTree(n2);

    n2.insertNeighbour(n1);
    n1.insertNeighbour(n2);

    variablesInserted.union(currentRel);
  }
}


/**
 * Constructor. Creates a Join Tree where each node corresponds to one
 * of the relations given in the argument list.
 * @param r a <code>RelationList</code> with the relations used to
 * create the Join Tree.
 */

public JoinTree (RelationList r) {

  NodeJoinTree n;
  int i;

  //we need to initialize this vector to be able
  //to introduce new nodes afterwards  
  joinTreeNodes = new Vector();

  for (i=0 ; i<r.size() ; i++) {
    n = new NodeJoinTree(r.elementAt(i));
    insertNodeJoinTree(n);
  }

  statistics = new JoinTreeStatistics();
}


/**
 * Constructor. Creates a Join Tree where the list of NodeJoinTree
 * contained are given in the argument list.
 * @param JTnodes is the Vector of elements <code>NodeJoinTree</code> used to
 * create the Join Tree.
 */


public JoinTree (Vector JTnodes) {
  
  NodeJoinTree n;
  int i;
  
  joinTreeNodes = JTnodes;
     
  statistics = new JoinTreeStatistics();
}  



/**
 * Creates a tree of cliques, that is, all the clusters in
 * the join tree are maximal complete subgraph of the triangulated graph.
 *
 * @param b a <code>Bnet</code>.
 */

public void treeOfCliques (Bnet b) {

  Triangulation t;
  NodeList ordering;
  RelationList r, s;

  t = new Triangulation(b);
  t.getTriangulation();
  ordering = t.maximumCardinalitySearch();
  r = t.numerateCliques(ordering);
  s = t.getSeparators(r);
  this.createJoinTree(r,s);
}

/**
 * Creates a tree of cliques, that is, all the clusters in
 * the join tree are maximal complete subgraph of the triangulated graph.
 *
 * The join tree is created only for the moral graph induced by the
 * <code>RelationList</code> passed as parameter, which normally is 
 * a subgraph of the original one.
 *
 * @param b a <code>Bnet</code>.
 */

public void treeOfCliques (Bnet b, RelationList relations) {

  Triangulation t;
  NodeList ordering;
  RelationList r, s;

  t = new Triangulation(b);
  t.getTriangulation(relations);
  ordering = t.maximumCardinalitySearch();
  r = t.numerateCliques(ordering);
  s = t.getSeparators(r);
  this.createJoinTree(r,s);
}


/**
 * Creates a tree of cliques from a given list of nodel, in which the
 * variables of the list form a subtree of the entire join tree.
 *
 * @param b a <code>Bnet</code>.
 * @param set a list of variables (<code>NodeList</code>).
 */

public void treeOfCliques(Bnet b, NodeList set) {

  Triangulation t;
  NodeList ordering;
  RelationList r, s;

  t = new Triangulation(b);
  t.getTriangulation(set);
  ordering = t.maximumCardinalitySearch(set);
  r = t.numerateCliques(ordering);
  s = t.getSeparators(r);
  this.createJoinTree(r,s);
}


/**
 * Creates a tree of cliques in which the variables of the argument list
 * form a subtree of the whole join tree, using "criterion" as heuristics
 * during the triangulation process.
 *
 * @param b a <code>Bnet</code>
 * @param set a list of variables (<code>NodeList</code>).
 * @param criterion a <code>String</code> with the heuristic.
 */

public void treeOfCliques (Bnet b, NodeList set, String criterion) {

  Triangulation t;
  NodeList ordering;
  RelationList r, s;

  t = new Triangulation(b);
  t.getTriangulation(set,criterion);
  ordering = t.maximumCardinalitySearch(set);
  r = t.numerateCliques(ordering);
  s = t.getSeparators(r);
  this.createJoinTree(r,s);
}


/**
 * Creates a tree of cliques, that is, all the clusters in
 * the join tree are maximal complete subgraph of the triangulated graph.
 * The DIFFERENCE with the previous methods is the use of a previously
 * known deletion sequence, avoiding the process of obtaining a new one.
 *
 * @param b a <code>Bnet</code>.
 * @param sigma The deletion sequence to be used (<code>NodeList</code>).
 */

public void treeOfCliques (NodeList sigma, Bnet b) {

  Triangulation t;
  NodeList ordering;
  RelationList r, s;

  t = new Triangulation(b);
  t.setTriangulatedNodes(sigma);
  t.triangulate();
  ordering = t.maximumCardinalitySearch();
  r = t.numerateCliques(ordering);
  s = t.getSeparators(r);
  this.createJoinTree(r,s);
}

/**
 * Creates a tree of cliques, that is, all the clusters in 
 * the join tree are maximal complete subgraph of the triangulated graph,
 * but using the class <code>GTriangulation</code>. <br>
 *
 * @param g the <code>Graph</code> graph to be triangulated
 * @param crietrion a <code>String</code> identifying the heuristic to be
 *      used during triangulation
 * @param arbitrary a <code>String</code> of value "yes" if ties are 
 * 	breaks in arbitrary way. "no" for random tie-breaking 
 * @param generator a <code>Random</code> number generator
 * @param minimal a <code>boolean</code> indicating if triangulation
 * 	has to be minimal
 */

public void treeOfCliquesByGTriangulation (Graph g, String criterion, 
		String arbitrary, Random generator, boolean minimal) 
				throws InvalidEditException, IOException{
  
  GTriangulation gt = new GTriangulation(g);
  int numbering[];
  int j;
  ArrayList cliques;
  ArrayList v;
  Graph gMoralReduced,gInitialMoral,gCopy,gMoralReducedCopy;
  LinkList minLinks,addedLinks;
  ArrayList fillIns;
  

  gMoralReduced = g.duplicate(); //copying the original graph  
  //gInitialMoral = g.duplicate(); //extra copy

  gt.reduceGraph(gMoralReduced); 
  gMoralReducedCopy = gMoralReduced.duplicate();//add
  gt.getDeletionSequence(criterion,arbitrary,generator,gMoralReduced);
    

  fillIns = gt.getGroupedAddedLinks();
  addedLinks = gt.getAddedLinks();

  if (minimal){
    //gCopy = gInitialMoral;
    gCopy = gMoralReducedCopy;//add
    minLinks = gt.MINT(addedLinks,gCopy,addedLinks);  
  }
  else minLinks = addedLinks;

  //gt.fillGraph(gInitialMoral,minLinks);
  gt.fillGraph(gMoralReducedCopy,minLinks);//add

  //1.-NUMBERING
  //numbering = gt.maximumCardinalitySearch(-1,gInitialMoral);
  numbering = gt.maximumCardinalitySearch(-1,gMoralReducedCopy);//add  

  //2.-CLIQUE IDENTIFICATION
  //cliques = gt.identifyCliques(numbering,gInitialMoral);
  cliques = gt.identifyCliques(numbering,gMoralReducedCopy);//add

  //3.-CONSTRUCTION OF THE TREE 
  //joinTreeNodes = gt.buildTree(cliques,g);
  joinTreeNodes = gt.buildTree(cliques,gMoralReducedCopy);//add

}



/**
 * This method is used to set the value of the <code>joinTreeNodes</code>
 * instance variable.
 *
 * @param v The list of the nodes of the Join Tree, of class
 * <code>NodeJoinTree</code>.
 */

public void setJoinTreeNodes(Vector v) {

  joinTreeNodes = v;
}


/**
 * This method is used for accessing to the list of nodes.
 *
 * @return The list of nodes as a vector of elements of
 * class <code>NodeJoinTree</code>.
 */

public Vector getJoinTreeNodes() {

  return joinTreeNodes;
}


/**
 * @return if the join tree is being used as a MPSTree
 */

public boolean getIsMPST( ) {
  return isMPST;
}

/**
 * Sets the value of isMPST.
 * @param mpst a boolean: true if the tree is in fact a MPST
 */

public void setIsMPST(boolean mpst){
  isMPST = mpst;
}

/**
 * Sets the information limit for prunning the probability trees
 * associated with the potentials in the join tree.
 * @param the information limit.
 */

public void setLimitForPotentialPruning(double l) {

  limitForPotentialPrunning = l;
}


/**
 * Sets the information limit for lowLimitForPrunning
 * @param the information limit.
 */

public void setLowLimitForPruning(double l) {

  lowLimitForPrunning = l;
}


/**
 * Sets the maximum size for a potential in the join tree.
 * @param the maximum number of values.
 */

public void setMaximumSizeForPotentialPrunning(int m) {

  maximumSizeForPotentialPrunning = m;
}

/**
 * Sets the value of ApplySortAndBound
 */

public void setApplySortAndBound(boolean b){
  applySortAndBound = b;
}


/**
 * Set the statistics.
 * @param the <code>JoinTreeStatistics</code>.
 */

public void setStatistics(JoinTreeStatistics stat) {

  statistics = stat;
}


/**
 * Gets the statistics about the join tree.
 * @return the statistics about the join tree.
 */

public JoinTreeStatistics getStatistics() {

  return statistics;
}


/**
 * Returns the node in a given position in the list of nodes.
 *
 * @param p The position of the node to retrieve.
 * @return The <code>NodeJoinTree</code> at position <code>p</code>.
 */

public NodeJoinTree elementAt(int p) {

  return ((NodeJoinTree)joinTreeNodes.elementAt(p));
}


/**
 * Inserts a <code>NodeJoinTree</code> at the end of the list of nodes
 *
 * @param n The node to insert.
 */

public void insertNodeJoinTree (NodeJoinTree n) {

  joinTreeNodes.addElement(n);
}

/**
 * Create the messages (NeighbourTree) from n1 to n2 and viceversa. This method
 * sets a reference (oppositeMessage) at every message to point to the message in 
 * the opposite sense
 */
void insertNeighbour(NodeJoinTree n1, NodeJoinTree n2){
    NeighbourTree nt1,nt2;
    Relation msg;
    
    nt1 = makeNeighbourTree();
    nt2 = makeNeighbourTree();
    nt1.setNeighbour(n2);
    nt2.setNeighbour(n1);
    nt1.setOppositeMessage(nt2);
    nt2.setOppositeMessage(nt1);
    
   // Now create the Relations for the messages
    msg = n1.getNodeRelation().intersection(n2.getNodeRelation());
    nt1.setMessage(msg);
    msg = n1.getNodeRelation().intersection(n2.getNodeRelation());
    nt2.setMessage(msg);    
    
    n1.insertNeighbour(nt1);
    n2.insertNeighbour(nt2);
  }
  

/**
 * Removes a <code>NodeJoinTree</code> from the list of nodes.
 *
 * @param n the node to remove.
 */

public void removeNodeJoinTree(NodeJoinTree n) {

  joinTreeNodes.removeElement(n);
}


/**
 * This method is used to get the position of a Node in the list of Nodes
 * in the Join Tree.
 *
 * @param n the <code>NodeJoinTree</code> to locate.
 * @return The position of <code>n</code> in the list or -1 is
 * <code>n</code> is not in it.
 */

public int indexOf(NodeJoinTree n) {

  return joinTreeNodes.indexOf(n);
}

/**
 * This method is used to get the position in the join tree 
 * of a Node whose set of variables
 * is equals to the nodelist passed as parameter
 *
 * @param n the <code>NodeList</code> 
 * @return The position of <code>n</code> in the list or -1 if
 * <code>n</code> is not in it.
 */

public int indexOf(NodeList n) {
  String s;
  int i,pos;
  NodeJoinTree node;

  s = n.toString2();

  pos = -1;
  for(i=0;i<joinTreeNodes.size();i++){
    node = elementAt(i);
    if (node.getVariables().toString2().equals(s) ){
      pos=i;
      break;
    }
  }

  return pos;
}



/**
 * This method is used to calculate the number of nodes that contains the
 * join tree
 *
 * @return The number of nodes in the join tree.
 */

public int size() {

  return joinTreeNodes.size();
}


/**
 * Creates the Join Tree using a list of Cliques and a list of Separators.
 *
 * @param cliques the list of the relations that contains the Cliques of
 * the network.
 * @param separators the list of the messages that will be between the
 * nodes of the Join Tree.
 */

public void createJoinTree(RelationList cliques, RelationList separators) {

  int i, j;
  boolean find;
  NodeJoinTree node;
  NeighbourTree neighbour;

  node = new NodeJoinTree(cliques.elementAt(0));
  insertNodeJoinTree(node);

  for (i=1 ; i<cliques.size() ; i++) {
    j = 0;
    find = false;

    /* looking for a neighbour */

    while ((j<i) && (!find)) {
      if (cliques.elementAt(j).isContained(separators.elementAt(i))) {
	find = true;
	node = new NodeJoinTree(cliques.elementAt(i));

	neighbour = new NeighbourTree();
	neighbour.neighbour = this.elementAt(j);
	neighbour.setMessage(separators.elementAt(i));
	node.neighbourList.insertNeighbour(neighbour);
	insertNodeJoinTree(node);

	neighbour = new NeighbourTree();
	neighbour.neighbour = node;
	neighbour.setMessage(separators.elementAt(i));
	this.elementAt(j).neighbourList.insertNeighbour(neighbour);
      }
      else
	j++;
    }
  }
}


/**
 * Gets the list of messages (separators) in the Join Tree. It is used for
 * the construction of the binary tree.
 *
 * @return The list of the separators as a <code>RelationList</code>.
 * Contains no repeated messages.
 */

public RelationList getDifferentMessages() {

  int i, j;
  RelationList r = new RelationList();
  NodeJoinTree n = new NodeJoinTree();
  NeighbourTree neighbour = new NeighbourTree();

  for (i=0 ; i<this.joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree) this.joinTreeNodes.elementAt(i);
    for (j=0 ; j<n.neighbourList.size() ; j++) {
      neighbour = (NeighbourTree) n.neighbourList.elementAt(j);
      if (!r.contains(neighbour.getMessage())) {
	r.insertRelation(neighbour.getMessage());
      }
    }

  }

  return r;
}


/**
 * Gets the list of all messages. Repeated messages may appear.
 * @return the list of messages as a <code>RelationList</code>..
 */

public RelationList getMessages() {

  int i, j;
  RelationList r = new RelationList();
  NodeJoinTree n = new NodeJoinTree();
  NeighbourTree neighbour = new NeighbourTree();

  for (i=0 ; i<this.joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree) this.joinTreeNodes.elementAt(i);
    for (j=0 ; j<n.neighbourList.size() ; j++) {
      neighbour = (NeighbourTree) n.neighbourList.elementAt(j);
      r.insertRelation(neighbour.getMessage());
    }
  }

  return r;
}


/**
 * Converts this <code>JoinTree</code> in another <code>JoinTree</code>
 * where all the leaves contain one and only one original relation.
 * All the original relations are contained in leaves.
 *
 * @param relations A <code>RelationList</code> with the original relations.
 * @return a <code>Hashtable</code> with pairs
 * <code>Node</code>-<code>Relation</code>
 * usefull to obtain the <code>Relation</code> to get the marginal for
 * a variable.
 */

public Hashtable Leaves (RelationList relations) {

  int i, position;
  NodeJoinTree n, newNode;
  NeighbourTree element;
  Relation r, r2, rel;
  Triangulation t = new Triangulation();
  Hashtable marginalCliques;
  Potential pot;

  marginalCliques = new Hashtable();

  /* Make a copy of the object because we need to remove some elements */

  for (i=0 ; i<relations.size(); i++) {
    position = this.containRelation(relations.elementAt(i));

    if (position != -1) {

      /* n cotains the element neighbouring the new node */
      n = (NodeJoinTree) this.joinTreeNodes.elementAt(position);

      /* Creates the new node */
      rel = new Relation();
      newNode = makeNodeJoinTree();

      rel.setVariables(relations.elementAt(i).getVariables().copy());

      pot = relations.elementAt(i).getValues();

      rel.setValues(pot.copy());

      rel.setKind((int)(relations.elementAt(i).getKind()));

      newNode.nodeRelation = rel;
      if (rel.isConditional()) {
        marginalCliques.put(rel.getVariables().elementAt(0),newNode);
      }

      /* Got all the information, so insert the node */
      this.joinTreeNodes.addElement(newNode); 
      insertNeighbour(n,newNode);
    }
    else {System.out.println("A relation has not been included");}
  }
  return marginalCliques;
}

/**
 * Creates an instance of class NodeJoinTree
 * Override this method in subclases to get other behaviour.
 * @return a new instance of NodeJoinTree
 */
protected NodeJoinTree makeNodeJoinTree(){
  return new NodeJoinTree();
}

/**
 * Creates an instance of class NodeJoinTree and attaches to it
 * a given relation.
 * Override this method in subclases to get other behaviour.
 * @param rel the Relation to be attached to the new NodeJoinTree
 * @return a new instance of NodeJoinTree
 */
protected NodeJoinTree makeNodeJoinTree(Relation rel){
  return new NodeJoinTree(rel);
}

/**
 * Creates an instance of class NeighbourTree. 
 * Override this method in subclases to get other behaviour.
 * @return a new instance of NeighbourTree
 */
protected NeighbourTree makeNeighbourTree(){
  return new NeighbourTree();
}

/**
 * This method tells if any node in the join tree contains
 * a given relation.
 *
 * @param r The <code>Relation</code> to search in <code>joinTreeNodes</code>
 * @return the position of <code>r</code> in <code>joinTreeNodes</code>,
 * or -1 if it is not found.
 */

public int containRelation(Relation r) {

  NodeJoinTree n;
  int i = 0, position = -1;
  boolean found = false;

  while ((i < this.size()) && (!found)) {
    n = new NodeJoinTree();
    n = (NodeJoinTree) this.joinTreeNodes.elementAt(i);
    if (n.nodeRelation.isContained(r)) {
      found = true;
      position = i;
    }
    else
      i++;
  }

  return position;
}


/**
 * This method constructs a binary join tree from the join tree recorded in
 * a object of this class. The binary tree constructed is recorded in the same
 * object that call this method, so the original tree is removed
 *
 * @see joinNeighbours
 * @see divideNode
 */

public void binaryTree () {

  int i = 0, removed = 0;
  NodeJoinTree n;

  while (i + removed < this.joinTreeNodes.size()) {
    n = (NodeJoinTree) this.joinTreeNodes.elementAt(i);
    if (n.neighbourList.size() == 2) {
      this.joinNeighbours(n);
      this.joinTreeNodes.removeElementAt(i);
      removed ++;
    }
    else
      i++;
  }

  for (i=0 ; i<this.joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree) this.joinTreeNodes.elementAt(i);
    if (n.neighbourList.size() > 3) {
      this.divideNode(n);
    }
  }
}


/**
 * This method joins the neighbours of the node that is gives as parameter.
 * This method assumes that the node has only two neighbours.
 *
 * @param node The node whose neighbours are going to be joined.
 */

public void joinNeighbours(NodeJoinTree node) {

  /* contains the message between the new neighbours */
  Relation r = new Relation();

  /* used for calling intersection method */
  Triangulation t = new Triangulation();

  /* Contains the future neighbours */
  NodeJoinTree neighbour1 = new NodeJoinTree();
  NodeJoinTree neighbour2 = new NodeJoinTree();

  /* this is the structure to insert into the list of neighbours of each one */
  NeighbourTree variableToInsert = new NeighbourTree();
  NeighbourTree temp;

  temp = (NeighbourTree) node.neighbourList.elementAt(0);
  neighbour1 = temp.getNeighbour();

  temp = (NeighbourTree) node.neighbourList.elementAt(1);
  neighbour2 = temp.getNeighbour();

  neighbour1.neighbourList.removeNeighbour(node);
  neighbour2.neighbourList.removeNeighbour(node);

  r = t.intersection(neighbour1.nodeRelation, neighbour2.nodeRelation);

  variableToInsert.neighbour = neighbour2;
  variableToInsert.setMessage(r);
  neighbour1.neighbourList.insertNeighbour(variableToInsert);

  variableToInsert.neighbour = neighbour1;
  neighbour2.neighbourList.insertNeighbour(variableToInsert);
}


/**
 * Divides the node in two. The first node will contain three neighbours
 * (the two neighbours of the original node and the other new one),
 * and the second will contain the rest. If this second
 * node contains more than 3 neighbours this method is called recursively.
 *
 * @param n the node to divide.
 */

public void divideNode (NodeJoinTree n) {

  int i;
  NodeJoinTree node1, node2;
  NeighbourTree newNode;
  NeighbourTree element = new NeighbourTree();
  Relation r = new Relation();
  Triangulation t = new Triangulation();

  node1 = new NodeJoinTree();
  node2 = new NodeJoinTree();

  for (i=0 ; i<2 ; i++) {
    element = (NeighbourTree) n.neighbourList.elementAt(i);
    node1.neighbourList.insertNeighbour(element);
    element.neighbour.neighbourList.removeNeighbour(n);

    newNode = new NeighbourTree();
    t.union (node1.nodeRelation, element.getMessage());
    newNode.neighbour = node1;
    newNode.setMessage(element.getMessage());
    element.neighbour.neighbourList.insertNeighbour(newNode);
  }

  this.joinTreeNodes.addElement(node1);

  for (i=2 ; i<n.neighbourList.size() ; i++) {
    element = (NeighbourTree) n.neighbourList.elementAt(i);
    node2.neighbourList.insertNeighbour(element);
    element.neighbour.neighbourList.removeNeighbour(n);

    newNode = new NeighbourTree();
    t.union (node2.nodeRelation, element.getMessage());
    newNode.neighbour = node2;
    newNode.setMessage(element.getMessage());
    element.neighbour.neighbourList.insertNeighbour(newNode);
  }

  this.joinTreeNodes.addElement(node2);

  r = t.intersection(node1.nodeRelation, node2.nodeRelation);

  element.neighbour = node2;
  element.setMessage(r);
  node1.neighbourList.insertNeighbour(element);

  element.neighbour = node1;
  element.setMessage(r);
  node2.neighbourList.insertNeighbour(element);

  if (node2.neighbourList.size() > 3)
    divideNode (node2);
}


/**
 * Gets the leaves of the join tree, i.e., those nodes with
 * only zero or one neigbours.
 * @return a vector of <code>JoinTreeNode</code>s corresponding to
 * the leaves.
 */

public Vector getLeaves() {

  Vector leaves;
  NodeJoinTree node;
  int i;

  leaves = new Vector();

  for (i=0 ; i<joinTreeNodes.size() ; i++) {
    node = (NodeJoinTree)joinTreeNodes.elementAt(i);
    if (node.isLeaf())
      leaves.addElement(node);
  }

  return leaves;
}


/**
 * Assigns a label to each node, starting from 0.
 */

public void setLabels() {

  int i;
  NodeJoinTree n;

  for (i=0 ; i<joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree)joinTreeNodes.elementAt(i);
    n.setLabel(i);
  }
}

/**
 * Assigns a label to each node, starting from begin
 */

public void setLabels(int begin) {

  int i;
  NodeJoinTree n;

  for (i=0 ; i<joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree)joinTreeNodes.elementAt(i);
    n.setLabel(i+begin);
  }
} 


/**
 * Assigns a label to each node, starting from 0.
 * After this labelling, the numbering (and the cliques)
 * represent an ancestral order (parents have lower number
 * than their children) labelling for a tree whose
 * root is the one passed as parameter
 */

public void ancestralLabelling(NodeJoinTree root) {

  Vector newNodes = new Vector(this.size());
  Stack stackNodes = new Stack();
  Stack stackParents = new Stack();
  NeighbourTreeList ntl;
  int i;
  NodeJoinTree node,node2;
  Integer I;


  setLabels(); //preliminar labelling

  // setting the nodes in the correct position

  // initialising
  newNodes.addElement(root);
  ntl = root.getNeighbourList();
  for(i=0;i<ntl.size();i++){
    stackNodes.push(ntl.elementAt(i).getNeighbour());
    stackParents.push(new Integer(root.getLabel()));
  }
  // creating in-order vector
  for( ; !stackNodes.empty(); ){
    node = (NodeJoinTree)stackNodes.pop();
    I = (Integer)stackParents.pop();
    newNodes.addElement(node);

    ntl = node.getNeighbourList();
    for(i=0;i<ntl.size();i++){
      node2 = ntl.elementAt(i).getNeighbour();
      if (node2.getLabel()!=I.intValue()){
        stackNodes.push(node2);
        stackParents.push(new Integer(node.getLabel()));
      }
    }
  }

  this.setJoinTreeNodes(newNodes);
  setLabels(); //definitive labelling
}



/**
 * Displays in the standard output each node together
 * with its neighbours (labels).
 */

public void display() {

  int i, j;
  NodeJoinTree n;
  NeighbourTree nt;

  for (i=0 ; i<joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree)joinTreeNodes.elementAt(i);
    System.out.println("NodeJoinTree.class()="+n.getClass().getName());
    System.out.println("Node "+n.getLabel()+" has variables :");
    n.getNodeRelation().print();
    System.out.println("Node "+n.getLabel()+" has neighbours :");

    for (j=0 ; j<n.getNeighbourList().size() ; j++) {
      nt = n.getNeighbourList().elementAt(j);
      System.out.println("NeighbourTree.getClass()="+nt.getClass().getName());
      System.out.println("Label : "+nt.getNeighbour().getLabel());
      System.out.println("oppositeMessage="+nt.getOppositeMessage());

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
}

/**
 * Displays a MPST in the standard output each node together
 * with its neighbours (labels).
 */

public void displayMPST() {

  int i, j;
  NodeJoinTree n;
  NeighbourTree nt;

  System.out.println("Printing MPST ("+ joinTreeNodes.size() +
  						" subgraphs):\n");
  for (i=0 ; i<joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree)joinTreeNodes.elementAt(i);
    System.out.println("Node "+n.getLabel()+" has variables :");
    for(j=0 ; j<n.getVariables().size() ; j++)
      System.out.print(n.getVariables().elementAt(j).getName()+" ");
    System.out.println();
    System.out.print("Cliques: ");
    if (n.getCliques() != null)
      for(j=0;j<n.getCliques().size();j++)
        System.out.print( ((NodeJoinTree)n.getCliques().get(j)).getLabel() + " ");
    System.out.println();
    if (n.getFamilies() != null)
      for(j=0;j<n.getFamilies().size();j++)
        System.out.print(((Family)n.getFamilies().get(j)).getNode().getName() + " ");
    System.out.println();
     
    System.out.println("Node "+n.getLabel()+" has neighbours :");

    for (j=0 ; j<n.getNeighbourList().size() ; j++) {
      nt = n.getNeighbourList().elementAt(j);
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
}


/**
 * Displays in the standard output each node together
 * with its neighbours (labels).
 */

public void display3() {

  int i, j;
  NodeJoinTree n;
  NeighbourTree nt;

  for (i=0 ; i<joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree)joinTreeNodes.elementAt(i);
    System.out.println("Node "+n.getLabel()+" has variables :");
    n.getNodeRelation().print();
    if (n.getIsSimplicial()) System.out.println("Simplicial");
    if (n.getCorrespondingMPS()!=null)
      System.out.println("Corresponding MPS: "+ n.getCorrespondingMPS().getLabel());
    System.out.println("Node "+n.getLabel()+" has neighbours :");

    for (j=0 ; j<n.getNeighbourList().size() ; j++) {
      nt = n.getNeighbourList().elementAt(j);
      System.out.println("Label : "+nt.getNeighbour().getLabel());

      if (nt.getMessage() != null) {
	if (nt.getMessage().getValues() != null) {
	  System.out.println("OUTGOING MESSAGE");
	  (nt.getMessage().getValues()).print();
	}
        else{
          System.out.println("OUTGOING MESSAGE");
          nt.getMessage().getVariables().printNames();
          System.out.println();
        }
	if (nt.getMessage().getOtherValues() != null) {
	  System.out.println("INCOMING MESSAGE");
	  (nt.getMessage().getOtherValues()).print();
	}
        else{
          System.out.println("OUTGOING MESSAGE");
          nt.getMessage().getVariables().printNames();
          System.out.println();
        }
      }
    }
    System.out.println(" ");
  }
}

/**
 * Transforms this  join tree into a binary join tree.
 */
public void binTree() {
  NeighbourTreeList neighbours;
  NeighbourTree nt;
  NodeJoinTree node, newNode, tempNode;
  int i, n;

  node = elementAt(0);
  neighbours = node.getNeighbourList();
  n = neighbours.size();

  if (n > 3) {
    newNode = makeNodeJoinTree();
    newNode.getNodeRelation().setVariables(node.getVariables());
    insertNodeJoinTree(newNode);

    for (i=(n-1) ; i>1 ; i--) {
      nt = neighbours.elementAt(i);
      nt.getNeighbour().removeNeighbour(node);
      neighbours.removeElementAt(i);
      insertNeighbour(newNode,nt.getNeighbour());
    }
    insertNeighbour(node,newNode);
  }
  neighbours = node.getNeighbourList();
  n = neighbours.size();
  for (i=0 ; i<n ; i++) {
    tempNode = neighbours.elementAt(i).getNeighbour();
    binTreeAux(node,tempNode);
  }
}

/**
 * Auxiliar to binTree.
 * @param parent a <code>NodeJoinTree</code>
 * @param child a <code>NodeJointTree</code>
 */
public void binTreeAux(NodeJoinTree parent, NodeJoinTree child) {
  NeighbourTreeList neighbours;
  NeighbourTree nt;
  NodeJoinTree newNode, tempNode;
  int i, n, pos1, pos2;

  neighbours = child.getNeighbourList();
  n = neighbours.size();
  if (n > 3) { 
    newNode = makeNodeJoinTree();
    newNode.getNodeRelation().setVariables(child.getVariables());
    insertNodeJoinTree(newNode);

    pos1 = neighbours.indexOf(parent); // This is to maintain as neighbours the cliques in  pos1 and pos2,  that its the parent and another neighbour
    if (pos1 == 0)
      pos2 = 1;
    else
      pos2 = 0;

    for (i=(n-1) ; i>=0 ; i--) {
      if ((i!=pos1) && (i!=pos2)) {   
         nt = neighbours.elementAt(i);
         nt.getNeighbour().removeNeighbour(child);
         neighbours.removeElementAt(i);
         insertNeighbour(newNode,nt.getNeighbour());
      }
    }
    insertNeighbour(child,newNode);
  }
  neighbours = child.getNeighbourList();
  n = neighbours.size();
  for (i=0 ; i<n ; i++) {
    tempNode = neighbours.elementAt(i).getNeighbour();
    if (tempNode != parent)
      binTreeAux(child,tempNode);
  }
}


/**
 * Transforms a join tree into a binary join tree.
 */

public void binTree2() {

  NeighbourTreeList neighbours, temp;
  NeighbourTree nt;
  NodeJoinTree node, newNode, tempNode;
  Relation rel;
  int i, n;
  int pos1=0,pos2=1;

  node = elementAt(0);
  neighbours = node.getNeighbourList();
  n = neighbours.size();

  if (n > 3) {
    temp = new NeighbourTreeList();
    newNode = new NodeJoinTree();
    insertNodeJoinTree(newNode);
    rel = new Relation();
    //deciding wich cliques are being maintained as children of node
    pos1 = getChildrenToBeMaintained(node,-1);
    pos2 = getChildrenToBeMaintained(node,pos1);
    //computing the nodelist for newNode as the intersection of node.variables 
    // with the union of all its children minus those two to be maintained 
    NodeList nl = new NodeList();
    for(i=0;i<n;i++)
      if ((i!=pos1) && (i!=pos2))
        nl.join(neighbours.elementAt(i).getNeighbour().getVariables());
    nl = nl.intersection(node.getVariables());
    //setting the relation and the nodelist
    rel.setVariables(nl);
    newNode.setVariables(nl);
    newNode.setNodeRelation(rel);

    // processing neighbours
    for (i=(n-1) ; i>1 ; i--) {
      nt = neighbours.elementAt(i);
      nt.getNeighbour().removeNeighbour(node);
      nt.getNeighbour().insertNeighbour(newNode);
      temp.insertNeighbour(nt);
      neighbours.removeElementAt(i);
    }
    newNode.setNeighbourList(temp);
    newNode.insertNeighbourAsFirstElement(node);
    node.insertNeighbour(newNode);
  }

  neighbours = node.getNeighbourList();
  n = neighbours.size();

  for (i=0 ; i<n ; i++) {
    tempNode = neighbours.elementAt(i).getNeighbour();
    binTreeAux2(node,tempNode);
  }
}

/**
 * Auxiliar to binTree2
 * 
 * @param node the <code>NodeJoinTree</node> being splitted
 * @param pos0 the position in neighbours of node which have been decided
 *       yet to be maintained or -1 if no decision has been already taken
 *
 * @return the position in neighbours of the clique to be maintained
 */

private int getChildrenToBeMaintained(NodeJoinTree node,int pos0){
  int pos = 0;
  NeighbourTreeList neighbours;
  double tam0, removed;
  int i, j, n;
  Hashtable ht;
  NodeList nl;
  Integer I;

  NeighbourTree nt;
  //NodeJoinTree newNode, tempNode;
  //Relation rel;

  // initialising the hashtable with all the variables in node
  ht = new Hashtable(node.getVariables().size());
  for(i=0;i<node.getVariables().size();i++)
    ht.put(node.getVariables().elementAt(i),new Integer(0));

  // for each neighbour, for each variable in neighbour, if it is
  // in node we increase the counter 
  neighbours = node.getNeighbourList();
  n = neighbours.size();
  for(i=0;i<n;i++){
    if (i!=pos0){
      nt = neighbours.elementAt(i);
      nl = nt.getNeighbour().getVariables();
      for(j=0;j<nl.size();j++){
        I = (Integer) ht.get(nl.elementAt(j));
        if (I!=null) I = new Integer(I.intValue()+1);
      }
    }
  }

  // we prefer the position which yield the remaining set of smaller size
  pos = 0;
  tam0 = 0;
  for(i=0;i<n;i++){
    if (i!=pos0){
      removed = 1.0;
      nt = neighbours.elementAt(i);
      nl = nt.getNeighbour().getVariables();
      for(j=0;j<nl.size();j++){
        I = (Integer) ht.get(nl.elementAt(j));
        if ((I!=null) && (I.intValue()==0))
          removed *= ((FiniteStates)nl.elementAt(j)).getNumStates();
      }
      if (removed > tam0){
        tam0 = removed;
        pos = i;
      }
    }
  }

  return pos;
}

/**
 * Auxiliar to binTree2.
 * @param parent a <code>NodeJoinTree</code>
 * @param child a <code>NodeJointTree</code>
 */

public void binTreeAux2(NodeJoinTree parent, NodeJoinTree child) {

  NeighbourTreeList neighbours, temp;
  NeighbourTree nt;
  NodeJoinTree newNode, tempNode;
  Relation rel;
  int i, n, pos1, pos2;


  neighbours = child.getNeighbourList();
  n = neighbours.size();

  if (n > 3) {
    temp = new NeighbourTreeList();
    newNode = new NodeJoinTree();
    insertNodeJoinTree(newNode);
    rel = new Relation();

    //identifying position of neighbour to be retained
    pos1 = neighbours.indexOf(parent);
    pos2 = getChildrenToBeMaintained(child,pos1);

    //computing the nodelist for newNode as the union of all its children
    NodeList nl = new NodeList();
    for(i=0;i<n;i++){
      if ((i!=pos1) && (i!=pos2))
        nl.join(neighbours.elementAt(i).getNeighbour().getVariables());
    }
    nl = nl.intersection(child.getVariables()); 

    //setting the relation and the nodelist
    rel.setVariables(nl);
    newNode.setVariables(nl);
    newNode.setNodeRelation(rel);

    //processing neighbours
    for (i=(n-1) ; i>=0 ; i--) {
      if ((i!=pos1) && (i!=pos2)) {
	nt = neighbours.elementAt(i);
	nt.getNeighbour().removeNeighbour(child);
	nt.getNeighbour().insertNeighbour(newNode);
	temp.insertNeighbour(nt);
	neighbours.removeElementAt(i);
      }
    }
    newNode.setNeighbourList(temp);
    newNode.insertNeighbourAsFirstElement(child);
    child.insertNeighbour(newNode);
  }

  neighbours = child.getNeighbourList();
  n = neighbours.size();

  for (i=0 ; i<n ; i++) {
    tempNode = neighbours.elementAt(i).getNeighbour();
    if (tempNode != parent)
      binTreeAux2(child,tempNode);
  }
}


/**
 * Associates each family in the network to a clique <code>NodeJoinTree</code>
 * Notice that potentials are not initialized
 * The families/relations are taken from a given network.
 *
 * @param ir the <code>RelationList</code> which contains the initial relations.
 */

public void assignFamilies(RelationList ir) {

  //RelationList ir;
  NodeJoinTree node;
  Relation r, r2;
  int i, j, k;
  ArrayList families;
  Family family,aux;
  double size;
  Potential potential;

  // we get the relations in the network

  //ir = b.getInitialRelations();

  // treating relations of canonical potentials

  Vector v = new Vector();
  for (i=0; i<ir.size(); i++) {
    r = ir.elementAt(i);
    potential = (Potential)r.getValues();
    if (r.getValues().getClass() == CanonicalPotential.class) {
      for (k=0; k<((CanonicalPotential) potential).getArguments().size(); k++) {
	v.addElement(((CanonicalPotential) potential).getStrArgument(k));
      }
    }
  }

  for (i=0; i<v.size(); i++) {
    ir.removeRelation(ir.getRelation((String) v.elementAt(i)));
  }

  // processing families

  for (i=0 ; i<ir.size() ; i++) {
    r = ir.elementAt(i);
    // searching for a clique containing relation r
    for (j=0 ; j<getJoinTreeNodes().size() ; j++) {
      node = elementAt(j);
      r2 = node.getNodeRelation();
      if (r2.isContained(r)) { //assigning family
        //creating the family
        family = new Family(r.getVariables().elementAt(0),r);
        // adding the family to families
        // we insert the new family according to its state space size
        // this is for efficiency reasons when initializing the join tree
        families = node.getFamilies();
        if (families.size()==0) families.add(family);
        else{ 
          size = FiniteStates.getSize(r.getVariables());
          for(k=0;k<families.size();k++){
            aux = (Family)families.get(k);
            if (size <= FiniteStates.getSize(
			aux.getRelation().getVariables()) )
              break;            
          }
          families.add(k,family);
        }
        break;
      }
    }
  }

  

}

/**
 * Associates each family in the network to a clique <code>NodeJoinTree</code>
 * Notice that potentials are not initialized
 * The families/relations are taken from a given network.
 *
 * @param b the <code>Bnet</code> which contains the initial relations.
 */

public void assignFamilies(Bnet b) {

  RelationList ir;
  NodeJoinTree node;
  Relation r, r2;
  int i, j, k;
  ArrayList families;
  Family family,aux;
  double size;
  Potential potential;

  // we get the relations in the network

  ir = b.getInitialRelations();

  // treating relations of canonical potentials

  Vector v = new Vector();
  for (i=0; i<ir.size(); i++) {
    r = ir.elementAt(i);
    potential = (Potential)r.getValues();
    if (r.getValues().getClass() == CanonicalPotential.class) {
      for (k=0; k<((CanonicalPotential) potential).getArguments().size(); k++) {
    v.addElement(((CanonicalPotential) potential).getStrArgument(k));
      }
    }
  }

  for (i=0; i<v.size(); i++) {
    ir.removeRelation(ir.getRelation((String) v.elementAt(i)));
  }

  // processing families

  for (i=0 ; i<ir.size() ; i++) {
    r = ir.elementAt(i);
    // searching for a clique containing relation r
    for (j=0 ; j<getJoinTreeNodes().size() ; j++) {
      node = elementAt(j);
      r2 = node.getNodeRelation();
      if (r2.isContained(r)) { //assigning family
        //creating the family
        family = new Family(r.getVariables().elementAt(0),r);
        // adding the family to families
        // we insert the new family according to its state space size
        // this is for efficiency reasons when initializing the join tree
        families = node.getFamilies();
        if (families.size()==0) families.add(family);
        else{
          size = FiniteStates.getSize(r.getVariables());
          for(k=0;k<families.size();k++){
            aux = (Family)families.get(k);
            if (size <= FiniteStates.getSize(
            aux.getRelation().getVariables()) )
              break;
          }
          families.add(k,family);
        }
        break;
      }
    }
  }



}

/**
 * Associates each family in the network to a clique <code>NodeJoinTree</code>
 * Notice that potentials are not initialized
 * The families/relations are taken from a given network.
 *
 * @param b the <code>Bnet</code> which contains the initial relations.
 */

public void assignFamiliesRestrictedTo(NodeList nl,Bnet bigB) {

  RelationList ir;
  NodeJoinTree node;
  Relation r, r2;
  int i, j, k;
  ArrayList families;
  Family family,aux;
  double size;
  Potential potential;

  Bnet b = new Bnet(bigB.getNodeList().intersectionNames(nl));

  // we get the relations in the network

  ir = b.getInitialRelations();

  // treating relations of canonical potentials

  Vector v = new Vector();
  for (i=0; i<ir.size(); i++) {
    r = ir.elementAt(i);
    potential = (Potential)r.getValues();
    if (r.getValues().getClass() == CanonicalPotential.class) {
      for (k=0; k<((CanonicalPotential) potential).getArguments().size(); k++) {
    v.addElement(((CanonicalPotential) potential).getStrArgument(k));
      }
    }
  }

  for (i=0; i<v.size(); i++) {
    ir.removeRelation(ir.getRelation((String) v.elementAt(i)));
  }

  // processing families
  boolean already;
  for (i=0 ; i<ir.size() ; i++) {
    r = ir.elementAt(i);
    already =false;
    // searching for a clique containing relation r
    for (j=0 ; j<getJoinTreeNodes().size() ; j++) {
        node = elementAt(j);
        r2 = node.getNodeRelation();
        if (r2.isContained(r)) { //assigning family
         //creating the family
         family = new Family(r.getVariables().elementAt(0),r);
         // adding the family to families
         // we insert the new family according to its state space size
         // this is for efficiency reasons when initializing the join tree
        families = node.getFamilies();
        if (families.size()==0) families.add(family);
        else{
          size = FiniteStates.getSize(r.getVariables());
          for(k=0;k<families.size();k++){
            aux = (Family)families.get(k);
            if (family.getNode().getName().equals(aux.getNode().getName()))
                already = true;
            if (size <= FiniteStates.getSize(
            aux.getRelation().getVariables()) )
              break;
          }
          if (!already)
              families.add(k,family);
        }
        break;
       }
     }
  }
} 



/**
 * Initializes the potentials in a join tree as objects of
 * class <code>PotentialTable</code>. The potentials are taken from
 * a given network.
 *
 * @param b the <code>Bnet</code> which contains the initial relations.
 */

public void initTablesOld(Bnet b) {

  RelationList ir;
  PotentialTable potTable, pot2;
  PotentialTree potTree;
  Potential potential;
  NodeJoinTree node;
  Relation r, r2;
  int i, j;

  // First we create unitary potentials for all the cliques

  for (i=0 ; i<getJoinTreeNodes().size() ; i++) {
    node = elementAt(i);
    r = node.getNodeRelation();
    potTable = new PotentialTable(r.getVariables());
    potTable.setValue(1.0);
    r.setValues(potTable);
  }

  // Now, we initialize the potentials using the network relations

  ir = b.getInitialRelations();

  // Added in order to take into account the canonical models
  Vector v = new Vector();
  for (i=0; i<ir.size(); i++) {
    r = ir.elementAt(i);
    potential = (Potential)r.getValues();
    if (r.getValues().getClass() == CanonicalPotential.class) {
      for (int k=0; k<((CanonicalPotential) potential).getArguments().size(); k++) {
	v.addElement(((CanonicalPotential) potential).getStrArgument(k));
      }
    }
  }
  for (i=0; i<v.size(); i++) {
    ir.removeRelation(ir.getRelation((String) v.elementAt(i)));
  }

  for (i=0 ; i<ir.size() ; i++) {
    r = ir.elementAt(i);
    potTable = new PotentialTable();
    potential = (Potential)r.getValues();

    //Modificado por jruiz
    if (potential.getClassName().equals("PotentialTable") || 
        potential.getClassName().equals("GeneralizedPotentialTable")) {
       potTable = (PotentialTable)potential;
    }//Fin modificado por jruiz

    else if (potential.getClassName().equals("PotentialTree")) {
       potTable = new PotentialTable((PotentialTree)potential);
    }
    else if (potential.getClassName().equals("CanonicalPotential")) {
      potTable = ((CanonicalPotential) potential).getCPT();
    }
    else {
      System.out.println(potential.getClass().getName() +
			 " is not implemented in JoinTree.initTables");
      System.exit(0);
    }

    // searching for a clique containing relation r
    for (j=0 ; j<getJoinTreeNodes().size() ; j++) {
      node = elementAt(j);
      r2 = node.getNodeRelation();
      if (r2.isContained(r)) {
        pot2 = (PotentialTable)r2.getValues();
        pot2.combineWithSubset(potTable);
        break;
      }
    }
  }
}


/**
 * Initializes the potentials in a join tree as objects of
 * class <code>PotentialTable</code>. The potentials are taken from
 * a given network.
 *
 * @param b the <code>Bnet</code> which contains the initial relations.
 */

public void initTables(Bnet b) {

  PotentialTable potTable, potTable2;
  PotentialTree potTree;
  NodeJoinTree node;
  Relation r;
  int i, j;
  Family family;
  ArrayList families;

  // First we assign families to cliques
  assignFamilies(b.getInitialRelations());

  // Secondly we create unitary potentials for all the cliques

  for (i=0 ; i<getJoinTreeNodes().size() ; i++) {
    node = elementAt(i);
    r = node.getNodeRelation();
    potTable = new PotentialTable(r.getVariables());
    potTable.setValue(1.0);
    r.setValues(potTable);
  }

  // Now, we initialize the potentials using the families associated
  // to each clique

  for(i=0;i<getJoinTreeNodes().size();i++){
    node = elementAt(i);  
    families = node.getFamilies();
    if (families.size() == 0){ // unitary potential 
      r = node.getNodeRelation();
      potTable = new PotentialTable(r.getVariables());
      potTable.setValue(1.0);
      r.setValues(potTable);
    }
    else{ //getting the potential of the first family
      family = (Family)families.get(0);
      r = family.getRelation( );
      potTable = PotentialTable.convertToPotentialTable(r.getValues());
      // creating the potential by combination
      for(j=1;j<families.size();j++){
        family = (Family)families.get(j);
        r = family.getRelation();
        potTable = potTable.combine(
		PotentialTable.convertToPotentialTable(r.getValues()));
      }
      //assigning the potential
      r = node.getNodeRelation();
      potTable2 = new PotentialTable(r.getVariables(),potTable);
      r.setValues(potTable2);
    }
  }
}


/**
 * Initializes the potentials in a join tree as objects of
 * class <code>PotentialTree</code>. The potentials are taken from
 * a given network.
 *
 * @param b the <code>Bnet</code> which contains the initial relations.
 */

public void initTrees(Bnet b){
  initTrees(b,null);
}

public void initTrees(Bnet b,RelationList relations) {

  PotentialTree potTree;
  NodeJoinTree node;
  Relation r, r2;
  int i, j;
  Family family;
  ArrayList families;

  // First we assign families to cliques
  if (relations == null) assignFamilies(b.getInitialRelations());
  else assignFamilies(relations);

  // Secondly we create unitary potentials for all the cliques

  for (i=0 ; i<getJoinTreeNodes().size() ; i++) {
    node = elementAt(i);
    r = node.getNodeRelation();
    potTree = new PotentialTree(r.getVariables());
    potTree.setTree(new ProbabilityTree(1.0));
    potTree.updateSize();
    r.setValues(potTree);
  }

  // Now, we initialize the potentials using the families associated
  // to each clique

  for(i=0;i<getJoinTreeNodes().size();i++){
    node = elementAt(i);
    r2 = node.getNodeRelation();
    potTree = (PotentialTree) r2.getValues();
  
    families = node.getFamilies();
    if (families.size() != 0){ // not unitary potential 
      // creating the potential by combination
      for(j=0;j<families.size();j++){
        family = (Family)families.get(j);
        r = family.getRelation();
        potTree = (PotentialTree)potTree.combine(
		PotentialTree.convertToPotentialTree(r.getValues()));
      }
      //assigning the potential
      r2.setValues(potTree);
    }
  }

}


/**
 * Initializes the potentials in a join tree as objects of
 * class <code>PotentialTree</code>. The potentials are taken from
 * a given network.
 *
 * @param b the <code>Bnet</code> which contains the initial relations.
 */

public void initTreesOld(Bnet b) {

  RelationList ir;
  PotentialTree potTree, pot2;
  Potential potential;
  PotentialTable potTable;
  NodeJoinTree node;
  Relation r, r2;
  int i, j;

  // First we create unitary potentials for all the cliques

  for (i=0 ; i<getJoinTreeNodes().size() ; i++) {
    node = elementAt(i);
    r = node.getNodeRelation();
    potTree = new PotentialTree(r.getVariables());
    potTree.setTree(new ProbabilityTree(1.0));
    potTree.updateSize();
    r.setValues(potTree);
  }

  // Now, we initialize the potentials using the network relations

  ir = b.getInitialRelations( );

  // Added in order to take into account the canonical models
  Vector v = new Vector();
  for (i=0; i<ir.size(); i++) {
    r = ir.elementAt(i);
    potential = (Potential)r.getValues();
    if (r.getValues().getClass() == CanonicalPotential.class) {
      for (int k=0; k<((CanonicalPotential) potential).getArguments().size(); k++) {
	v.addElement(((CanonicalPotential) potential).getStrArgument(k));
      }
    }
  }
  for (i=0; i<v.size(); i++) {
    ir.removeRelation(ir.getRelation((String) v.elementAt(i)));
  }

  for (i=0 ; i<ir.size() ; i++) {
    r = ir.elementAt(i);
    potTree = new PotentialTree();
    potential = (Potential)r.getValues();

    if (potential.getClassName().equals("PotentialTree"))
       potTree = (PotentialTree)potential;
    else if (potential.getClassName().equals("PotentialTable"))
       potTree = ((PotentialTable)potential).toTree();
    else if (potential.getClassName().equals("CanonicalPotential")) {
      potTree = ((CanonicalPotential) potential).getCPT().toTree();
    }
    else {
      System.out.println(potential.getClass().getName() +
			 " is not implemented in JoinTree.initTrees");
      System.exit(0);
    }

    // searching for a clique containing relation r
    for (j=0 ; j<getJoinTreeNodes().size() ; j++) {
      node = elementAt(j);
      r2 = node.getNodeRelation();
      if (r2.isContained(r)) {
        pot2 = (PotentialTree)r2.getValues();
        pot2.combineWithSubset(potTree);
        r2.setValues(pot2);
        break;
      }
    }
  }
}


/**
 * Initializes the potentials in a join tree as objects of
 * class <code>PotentialMTree</code>. The potentials are taken from
 * a given network.
 *
 * @param b the <code>Bnet</code> which contains the initial relations.
 */

public void initMultipleTrees(Bnet b) {

    int j;
    Relation r;
    PotentialTree pot;
    PotentialMTree potM;
    NodeJoinTree node;

    initTrees(b);
    
    for (j=0 ; j<getJoinTreeNodes().size() ; j++) {
	node = elementAt(j);
	r = node.getNodeRelation();
	pot = (PotentialTree)r.getValues();
	potM = new PotentialMTree(pot);
	r.setValues(potM);
    }
}



/**
 * Initializes the potentials in a join tree as objects of
 * class <code>PotentialMTree</code>. The potentials are taken from
 * a given list of relations.
 *
 * @param ir the <code>RelationList</code> which contains the initial relations.
 */

public void initMultipleTrees(RelationList ir,boolean SaB,double lfp,
                              int maxSize) {

  PotentialMTree potMTree;
  PotentialTree potTree,pt2;
  Potential potential;
  PotentialTable potTable;
  Potential pot;
  NodeJoinTree node;
  Relation r, r2;
  int i, j;

  // First we create unitary potentials for all the cliques

  for (i=0 ; i<getJoinTreeNodes().size() ; i++) {
    node = elementAt(i);
    r = node.getNodeRelation();
    potTree = new PotentialTree(r.getVariables());
    potTree.setTree(new ProbabilityTree(1.0));
    potTree.updateSize();
    //potMTree = new PotentialMTree(potTree);
    r.setValues(potTree);
  }

  // Now, we initialize the potentials using the list of relations

  for (i=0 ; i<ir.size() ; i++) {
    r = ir.elementAt(i);
    potTree =  (PotentialTree) r.getValues();

    // searching for a clique containing relation r
    for (j=0 ; j<getJoinTreeNodes().size() ; j++) {
      node = elementAt(j);
      r2 = node.getNodeRelation();
      if (r2.isContained(r)) {
        pt2 = (PotentialTree)r2.getValues();
        pt2 = (PotentialTree)pt2.combine(potTree);
        // transforming potential
        if ( ((PotentialTree)pt2).getTree().getLabel() == 1)
          if (SaB){
            pot = ((PotentialTree)pt2).sortAndBound(maxSize);
            pt2 = (PotentialTree)pot;
          }
        if (lfp!=0) ((PotentialTree)pt2).limitBound(lfp);
        // end of transformation
        r2.setValues(pt2);
        break;
      }
    }
  }

  // Creating PotentialMTree from PotentialTree
  for (i=0 ; i<getJoinTreeNodes().size() ; i++) {
    node = elementAt(i);
    r = node.getNodeRelation();
    potTree = (PotentialTree)r.getValues();
    potMTree = new PotentialMTree(potTree);
    r.setValues(potMTree);
  }


}



/**
 * Initializes the potentials in a join tree as objects of
 * class <code>PotentialTree</code>. All the potentials are unitary.
 */

public void initUnitaryTrees() {

  PotentialTree potTree;
  NodeJoinTree node;
  Relation r;
  int i;

  // First we create unitary potentials for all the cliques

  for (i=0 ; i<getJoinTreeNodes().size() ; i++) {
    node = elementAt(i);
    r = node.getNodeRelation();
    potTree = new PotentialTree(r.getVariables());
    potTree.setTree(new ProbabilityTree(1.0));
    potTree.updateSize();
    r.setValues(potTree);
  }
}


/**
 * This method assign each relation of the list passed as
 * argument to a clique, but ensuring than each clique has
 * at most one relation assigned. If necessary, new cliques
 * are added to the join tree.
 *
 * !!! Important: Potentials are initialised as <code>PotentialTree</code>s
 *
 * @param relations a <code>RelationList</code>
 */

public void expandByAssigningRelations(RelationList relations) {

  PotentialTree potTree;
  NodeJoinTree node;
  Relation r, r2;
  int i, j;
  Family family;
  ArrayList families;
 
  // First we assign families to cliques
  assignFamilies(relations);

  // Now, we initialize the potentials using the families associated
  // to each clique
  int numNodes = this.size();
  for(i=0;i<numNodes;i++){
    node = elementAt(i);
    families = node.getFamilies();
    if (families.size() == 0){ //creating unitary potential
      r = node.getNodeRelation();
      potTree = new PotentialTree(r.getVariables());
      potTree.setTree(new ProbabilityTree(1.0));
      potTree.updateSize();
      r.setValues(potTree);
    }
    if (families.size() == 1){ // not unitary potential, just copying
      family = (Family)families.get(0);
      r = family.getRelation();
      node.getNodeRelation().setValues( r.getValues().copy());
    }
    if (families.size() > 1){ // creating new cliques and updating families
      // as families are stored in ascending complexity, we maintain the
      // original clique for the last one and create new cliques for the rest
      for(j=0;j<families.size()-1;j++){
        family = (Family)families.get(j);
        r = family.getRelation();
        //creating a new node, its relation and its conections
        NodeJoinTree newNode = new NodeJoinTree();
        Relation newRel = new Relation();
        newRel.setVariables(r.getVariables().copy());
        newRel.setValues(r.getValues().copy());
        newRel.setKind((int)(r.getKind()));
        newNode.setNodeRelation(newRel);

        // Calculates the message that will be between the nodes
        Triangulation t = new Triangulation(); 
        Relation message = t.intersection(node.nodeRelation, newNode.nodeRelation);
        Relation otherMessage = t.intersection(node.nodeRelation,newNode.nodeRelation);

        // element contains the structure used to record the information
        // about the neighbours
        NeighbourTree element = new NeighbourTree();
        element.setNeighbour(node);
        element.setMessage(message);

        // Insert this in the Neighbours list 
        newNode.neighbourList.insertNeighbour(element);
        this.insertNodeJoinTree(newNode);
        element = new NeighbourTree();
        element.setNeighbour(newNode);
        element.setMessage(otherMessage);
        node.neighbourList.insertNeighbour(element);
            
        // updating families
        Family family2 = new Family(newRel.getVariables().elementAt(0),newRel);
        ArrayList families2 = newNode.getFamilies();
        families2.add(family2);;
      }
      // working with the last one
      family = (Family)families.get(families.size()-1);
      r = family.getRelation();
      node.getNodeRelation().setValues(r.getValues().copy());
    }
  }

}

/**
 * Displays each node together with its neighbours (labels)
 * by the standard output.
 */

public void display2() throws IOException {

  int i, j;
  NodeJoinTree n;
  NeighbourTree nt;

  for (i=0 ; i<joinTreeNodes.size() ; i++) {
    n = (NodeJoinTree)joinTreeNodes.elementAt(i);
    System.out.println("Node "+n.getLabel()+" has variables :");
    System.out.println("Tamanno : " +
           FiniteStates.getSize(n.getNodeRelation().getVariables().toVector()));
    n.getNodeRelation().print();
    System.out.println("Node "+n.getLabel()+" has neighbours :");

    for (j=0 ; j<n.getNeighbourList().size() ; j++) {
      nt = n.getNeighbourList().elementAt(j);
      System.out.println("Label : "+nt.getNeighbour().getLabel());
      System.out.println("OUTGOING MESSAGE");
      nt.getMessage().print();
    }
    System.out.println(" ");
    System.out.println("Press any key.....");
    //System.in.read();
    //System.in.read();
  }
}


/**
 * Gets all the variables that appear in the join tree.
 * @return a <code>NodeList</code> containing all the variables
 * in the join tree.
 */

public NodeList getVariables() {

  NodeList nl = new NodeList(), cliqueList;
  NodeJoinTree node;
  Node variable;
  int i, j;

  for (i=0 ; i<this.joinTreeNodes.size() ; i++) {
    node = this.elementAt(i);
    cliqueList = node.getVariables();
    for (j=0 ; j<cliqueList.size() ; j++) {
      variable = cliqueList.elementAt(j);
      if (nl.getId(variable) == -1)
        nl.insertNode(variable);
    }
  }
  return nl;
}


/**
 * Sorts the list of variables contained in the relations (cliques and
 * messages) of the join tree according to the order or variables
 * specified in a given <code>NodeList</code> pattern.
 *
 * @param pattern the <code>NodeList</code> used to test the order.
 */

public void sortVariables(NodeList pattern) {

  int i, j;
  NodeJoinTree node;
  NodeList cliqueList;
  Relation r;
  NeighbourTreeList ntl;
  NeighbourTree nt;

  for (i=0 ; i<this.joinTreeNodes.size() ; i++) {
    node = this.elementAt(i);
    r = node.getNodeRelation();
    cliqueList = r.getVariables();
    cliqueList.sort(pattern);
    r.setVariables(cliqueList);

    // now the messages
    ntl = node.getNeighbourList();
    for (j=0 ; j < ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      r = nt.getMessage();
      cliqueList = r.getVariables();
      cliqueList.sort(pattern);
      r.setVariables(cliqueList);
    }
  }
}


/**
 * This method restricts the join tree by eliminating those variables that
 * are situated in the residual sets of the leaves cliques. So, the
 * restriction can be performed by passing sum-flows to the parent node, or
 * simply summing up in the node (if the residual set also contains variables
 * of the list passed as parameter)
 *
 * @param set the list of variables to be mantained in the jointree
 * @param divide if "yes" division is performed when a node is being removed
 * @param penniless if "yes" penniless approximation is used when computing
 *                     the message of the absorbed node to its parent
 */

public void outerRestriction(NodeList set, String divide, String Penniless) {

  boolean change = true;
  int i, j, s;
  NodeJoinTree node, parent;
  NodeList varsNode, varsSep, varsRes, nl;
  NeighbourTreeList ntl;
  NeighbourTree nt;
  String type;
  Vector leaves = new Vector();
  Relation rel;

  // First we begin the outer restriction, in a bottom-up way

  s = joinTreeNodes.size();
  for (i=0 ; i<s ; i++) {
    node = (NodeJoinTree) joinTreeNodes.elementAt(i);
    if (node.isLeaf())
      leaves.addElement(node);
  }



  for (i=0 ; i<leaves.size() ; ) {

    node = (NodeJoinTree) leaves.elementAt(i);

    // obtaining the variables in the residual
    varsNode = node.getVariables();
    if (joinTreeNodes.size() > 1) { // the tree is not a single clique
      ntl = node.getNeighbourList();
      nt = ntl.elementAt(0); // the only neighbour
      varsSep = nt.getMessage().getVariables();
      varsRes = varsNode.difference(varsSep);
    }
    else {
      varsRes = varsNode;
      varsSep = new NodeList();
      nt = new NeighbourTree(); // this is only to avoid a compilation
                                // error, because nt has to be initialized
    }


    // identifying the kind of residual respect to set and
    // performing the addecuate restriction

    type = varsRes.kindOfInclusion(set);
    if (type.equals("subset")) { // nothing to do
      i++;
    }
    else if (type.equals("not empty")) { // removing the variables that are
					 // in residual but not in set.
                                         // The node is not removed.
      nl = set.copy();
      nl.join(varsSep);
      (node.getNodeRelation()).restrictToVariables(nl);
      node.setVariables(  node.getVariables().intersectionNames(nl)  );

      i++;
    }
    else { // "empty": remove the node and repeat the process
           // for the parent of i
           // Absorbtion of node by parent(node)

      parent = nt.getNeighbour();
      if (Penniless.equals("no")) parent.absorbFromNode(node,divide);
      else parent.pennilessAbsorbtionFromNode(node,limitForPotentialPrunning,
                                              lowLimitForPrunning);
      rel = parent.getNodeRelation();
      rel = transformRelation(rel);

      // removing node and its link with parent (the only link, because
      // node is a leaf)
      this.removeNodeJoinTree(node);
      parent.removeNeighbour(node.getLabel());

      // If now parent is a leaf, reexecuting loop for parent. To avoid
      // a more complex treatment, I use the trick to replace in leaves[i]
      // node by parent
      if (parent.isLeaf())
	leaves.setElementAt(parent,i);
      else
	i++;
    }
  } // end for




  // now we are going to remove (for each node) the variables in residual
  // but not in set. The node is not removed.

  s = joinTreeNodes.size();
  for (i=0 ; i<s ; i++) {

    node = (NodeJoinTree) joinTreeNodes.elementAt(i);

    // obtaining the variables in the residual
    varsNode = node.getVariables();
    ntl = node.getNeighbourList();
    varsSep = new NodeList();
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      varsSep.join(nt.getMessage().getVariables());
    }
    varsRes = varsNode.difference(varsSep);

    // identifying the kind of residual respect to set and
    // performing the addecuate restriction

    if ((varsRes.difference(set)).size() > 0) {
      nl = set.copy();
      nl.join(varsSep);
      (node.getNodeRelation()).restrictToVariables(nl);
      node.setVariables(  node.getVariables().intersectionNames(nl)  );
    }
  }
}




/**
 * Fuses two nodes in a <code>CliquePair</code>.
 *
 * @param cp the <code>CliquePair</code> containig the nodes (head and tail)
 *        to be fused.
 * @param order the <code>NodeList</code> used to mantain the same order in
 *        the variables of the new <code>NodeJoinTree</code>.
 * @param divide indicates if division has to be performed during the
 *        fusion of head and tail
 * @return a <code>NodeJoinTree</code> from a <code>CliquePair</code>, this,
 * and a the network variables, that are used to mantain the order among
 * the variables
 */

private NodeJoinTree fuseTwoNodes(CliquePair cp, NodeList order,
				  String divide) {

  NodeJoinTree newNode;
  Potential pot, pot2;
  NodeList complete, restricted;
  Relation rel;
  NeighbourTreeList ntl;
  NeighbourTree nt;

  rel = new Relation();

  // building the new potential. The method is a bit complicated, but
  // it is necessary to mantain the order among the variables specified
  // by order

  complete = cp.getCompleteList();
  complete.sort(order);

  pot2 = cp.getTail().getNodeRelation().getValues();
  if (pot2.getClassName().equals("PotentialTable")) {
    pot = new PotentialTable(complete,cp.getHead().getNodeRelation());
  }
  else if (pot2.getClassName().equals("PotentialTree")) {
    pot = new PotentialTree(complete,cp.getHead().getNodeRelation());
  }
  else {
    System.out.println("Potential class " + pot2.getClass().getName() +
		       " not implemented for method fuseTwoNodes");
    System.exit(0);
    pot = pot2; // the method never arrives here
  }

  pot = pot.combine(cp.getTail().getNodeRelation().getValues());
  pot = transformPotentialAfterCombination(pot);

  // if divide is true, looking for the separator head-tail and performing
  // the division

  if (divide.equals("yes")) {
    ntl = cp.getHead().getNeighbourList();
    nt = ntl.elementAt(ntl.indexOf(cp.getTail()));
    pot = pot.divide(nt.getMessage().getValues());
  }

  // maginalizing to restricted if necessary

  restricted = cp.getRestrictedList();
  if (restricted.size() != complete.size()) {
    restricted.sort(order);
    pot = pot.marginalizePotential(restricted.toVector());
    pot = transformPotentialAfterAddition(pot);
    rel.setVariables(restricted.copy());
  }
  else
    rel.setVariables(complete.copy());

  rel.setValues(pot);
  newNode = new NodeJoinTree(rel);
  return newNode;
}


/**
 * This method is applied after the outer restriction. During the execution
 * the neighbour cliques with separator including variables not in the
 ^ argument list <code>set</code>, are fused using Xu's method,
 * but applying some heuristics guided by a given criterion.
 *
 * @param set the list of variables to be mantained in the join tree
 * @param divide if "yes" division is performed when a node is being removed
 * @param criterion the criterion used for the selection of the nodes to
 *        fuse in the inner restriction
 * @param order the <code>NodeList</code> representing the order to be
 *        mantained in the nodes of new creation
 * @return the sum of the cliques' size added in this process
 */

public double innerRestriction(NodeList set, String divide, String criterion,
			       NodeList order) {

  CliquePairList cpl;
  CliquePair cp, cp2, cp3;
  int i, j, pos;
  NodeJoinTree newNode, head, tail, node, other;
  NeighbourTreeList ntl;
  NeighbourTreeList ntlNode, ntlNeighbour;
  NeighbourTree nt, nt2, ntNew;
  Vector headTail, auxVector;
  Vector subList;
  double extraSize = 0.0;
  NodeList nl, nl2;

  cpl = new CliquePairList(this,set,criterion);

  while (cpl.size() != 0) {
    cp = cpl.getFirstAndRemove();
    extraSize += cp.getSize();
    head = cp.getHead();
    tail = cp.getTail();
    headTail = new Vector();
    headTail.addElement(head);
    headTail.addElement(tail);

    // creating the new nodeJoinTree
    newNode = fuseTwoNodes(cp,order,divide);
    auxVector = new Vector();

    // setting as neighbour of the new node all the neighbours of
    // head and tail
    for (j=0 ; j<2 ; j++) {
      node = (NodeJoinTree)headTail.elementAt(j);
      other = (NodeJoinTree)headTail.elementAt((j+1)%2);
      ntlNode = node.getNeighbourList();

      for (i=0 ; i<ntlNode.size() ; i++) {
        nt = ntlNode.elementAt(i);
        if (nt.getNeighbour().getLabel() != other.getLabel() ) {
          // setting new neighbour for newNode
          ntNew = new NeighbourTree();
          ntNew.setNeighbour(nt.getNeighbour());
          ntNew.setMessage(nt.getMessage());
          auxVector.addElement(ntNew);
          // setting newNode as neighbour
          ntlNeighbour = nt.getNeighbour().getNeighbourList();
          pos = ntlNeighbour.indexOf(node);
          nt2 = ntlNeighbour.elementAt(pos);
          nt2.setNeighbour(newNode);
        }
      }
    }

    // remove head and tail from the jointree and add newNode
    pos = joinTreeNodes.indexOf(head);
    newNode.setLabel(head.getLabel());
    ntl = new NeighbourTreeList();

    for (j=0 ; j<auxVector.size() ; j++)
      ntl.insertNeighbour((NeighbourTree)auxVector.elementAt(j));
    newNode.setNeighbourList(ntl);
    joinTreeNodes.setElementAt(newNode,pos); // this also remove head
    joinTreeNodes.removeElement(tail);

    // Removing from cliquePairList the elements containing head or tail,
    // and replacing them by new cliquePairList containing newNode
    for (j=0 ; j<2 ; j++) {
      node = (NodeJoinTree)headTail.elementAt(j);
      subList = cpl.getListAndRemoveElements(node);

      for (i=0 ; i<subList.size() ; i++) {
        cp2 = (CliquePair) subList.elementAt(i);
        if (cp2.getHead() == node) {
          //testing if the separator contains variables not in set
          nl = (newNode.getVariables()).intersection(cp2.getTail().getVariables());
          nl2 = nl.intersection(set);
          if (nl.size() != nl2.size()) {
            cp3 = new CliquePair(newNode,cp2.getTail(),set);
            cpl.addElement(cp3,criterion);
          }
          else if (nl.equals(newNode.getVariables()) ||
		   nl.equals(cp2.getTail().getVariables())) {
            cp3 = new CliquePair(newNode,cp2.getTail(),set);
            cpl.addElement(cp3,criterion);
          }
        }
        else {
          //testing if the separator contains variables not in set
          nl = (newNode.getVariables()).intersection(cp2.getHead().getVariables());
          nl2 = nl.intersection(set);
          if (nl.size() != nl2.size()) {
            cp3 = new CliquePair(cp2.getHead(),newNode,set);
            cpl.addElement(cp3,criterion);
          }
          else if (nl.equals(newNode.getVariables()) ||
		   nl.equals(cp2.getHead().getVariables())) {
            cp3 = new CliquePair(newNode,cp2.getHead(),set);
            cpl.addElement(cp3,criterion);
          }
        }
      }
    }

    // studying if new cliquepairs have to be added to the list, because
    // newClique can be a subset of one of its neighbours

    for(j=0; j<ntl.size(); j++){
      nt = ntl.elementAt(j);
      node = nt.getNeighbour();

      //testing if newNode is a subset of node
      nl = (newNode.getVariables()).intersection(node.getVariables());
      if (nl.equals(newNode.getVariables()) ||
                       nl.equals(node.getVariables()) ){
        // detecting if the clique pair is included yet
        if (!cpl.isIncluded(newNode,node)){
          cp = new CliquePair(newNode,node,set);
          cpl.addElement(cp,criterion);
        }
      }

    }


  }

  return extraSize;
}


/**
 * This method restricts the join tree by eliminating the variables not
 * included in the set of variables passed as parameter. The method of
 * modification is based in the paper of Xu - Artificial Intelligence, 74
 * (1995) and in the modifications of this method outlined by Nilsson in
 * his paper Nilsson - Statistics and Computing, (1998).
 *
 * @param set the variables to be mantained in the join tree
 * @param divide if "yes" division is performed when a node is being removed
 * @param criterion the criterion used for the selection of the nodes to
 *        fuse in the inner restriction
 * @param order the nodelist representing the order of variables that have
 *        to be mantained
 * @return the size of the cliques built during the inner restriction
 */

public double restrictToVariables(NodeList set, String divide,
				  String criterion, NodeList order) {

  double extraSize;

  this.outerRestriction(set,divide,"no"); // no penniless approximation
  this.setLabels();

  extraSize = this.innerRestriction(set,divide,criterion,order);
  this.setLabels();

  return extraSize;
}


/**
 * Prints some statistics about the join tree to the standard output.
 */

public void printStatistics() {

  statistics.print();
}


/**
 * Saves the statistics to a given output.
 *
 * @param p the <code>PrintWriter</code> where the statistics will
 * be written.
 */

public void saveStatistics(PrintWriter p) {

  statistics.save(p);
}


/**
 * Saves the statistics to a given file.
 *
 * @param s the name of the file where the statistics will
 * be written.
 */

public void saveStatistics(String s) throws IOException {

  statistics.save(s);
}


/**
 * Calculates the main data about the join tree.
 */

public void calculateStatistics() {

  int i;
  double min, max, mean, total, meanVars;
  int minVars, maxVars, totalVars;
  double val;
  int numVars;
  NodeJoinTree node;

  // initializing values with the clique at position 0
  node = this.elementAt(0);
  min = max = total = FiniteStates.getSize(
               node.getNodeRelation().getVariables().toVector());
  minVars = maxVars = totalVars = node.getNodeRelation().getVariables().size();

  for (i=1 ; i<this.joinTreeNodes.size() ; i++) {
    node = this.elementAt(i);
    val = FiniteStates.getSize(node.getNodeRelation().getVariables().toVector());
    total += val;
    if (val < min)
      min = val;
    if (val > max)
      max = val;

    numVars = node.getNodeRelation().getVariables().size();
    totalVars += numVars;
    if (numVars < minVars)
      minVars = numVars;
    if (numVars > maxVars)
      maxVars = numVars;
  }

  mean = total / (double)this.joinTreeNodes.size();
  meanVars = totalVars / (double)this.joinTreeNodes.size();

  statistics.setNumCliques(this.joinTreeNodes.size());
  statistics.setMinVarsInClique(minVars);
  statistics.setMaxVarsInClique(maxVars);
  statistics.setMeanVarsInClique(meanVars);
  statistics.setMinCliqueSize(min);
  statistics.setMaxCliqueSize(max);
  statistics.setMeanCliqueSize(mean);
  statistics.setJTSize(total);
}


/**
 * Stores the potentials of the join tree in a <code>Vector</code>.
 * @return a <code>Vector</code> with a copy of the potentials in the
 * cliques of the join tree.
 */

public Vector storePotentials() {

  Vector v = new Vector();
  int i, s;
  NodeJoinTree node;
  Potential pot;

  s = joinTreeNodes.size();
  for (i=0 ; i<s ; i++){
    node = (NodeJoinTree) joinTreeNodes.elementAt(i);
    pot = node.getNodeRelation().getValues();
    if (pot.getClassName().equals("PotentialTree")) {
      v.addElement(((PotentialTree)node.getNodeRelation().getValues()).copy());
    }
    else if (pot.getClassName().equals("PotentialMTree")) {
      v.addElement(((PotentialMTree)node.getNodeRelation().getValues()).copy());
    }
    else if (pot.getClassName().equals("PotentialTable")) {
      v.addElement(((PotentialTable)node.getNodeRelation().getValues()).copy());
    }
    else {
      System.out.println("Potential class: " + pot.getClass().getName() +
			 " is not implemented for the method storePotentials");
      System.exit(0);
    }
  }

  return v;
}


/**
 * This method restores the potentials in the join tree from a vector of
 * potentials.
 * @param v the <code>Vector</code> containing the potentials to enter in the
 * join tree.
 */

public void restorePotentials(Vector v) {

  int i, s;
  NodeJoinTree node;
  Potential pot;

  s = joinTreeNodes.size();

  for (i=0 ; i<s ; i++) {
    node = (NodeJoinTree) joinTreeNodes.elementAt(i);
    pot = (Potential) v.elementAt(i);
    node.getNodeRelation().setValues(pot);
  }
}


/**
 * Transforms a <code>PotentialTree</code> after a combination operator.
 * IMPORTANT: the potential passed as parameter is modified.
 * @param pot the <code>PotentialTree</code> to be transformed.
 * @return the transformed <code>PotentialTree</code>.
 */

public Potential transformPotentialAfterCombination(Potential pot) {

  if (pot.getClassName().equals("PotentialTree")) {
    if (maximumSizeForPotentialPrunning != 2147483647)
      if ( ((PotentialTree)pot).getTree().getLabel() == 1)
        if (applySortAndBound)
          pot = ((PotentialTree)pot).sortAndBound(
                            maximumSizeForPotentialPrunning);

    if (limitForPotentialPrunning != 0) 
      ((PotentialTree)pot).limitBound(limitForPotentialPrunning);
  }

  if (pot.getClassName().equals("PotentialMTree")) {
    if (maximumSizeForPotentialPrunning != 2147483647)
      if ( ((PotentialMTree)pot).getTree().getLabel() == 1)
        if (applySortAndBound)
          pot = ((PotentialMTree)pot).sortAndBound(
                            maximumSizeForPotentialPrunning);

    if (limitForPotentialPrunning != 0) 
      ((PotentialMTree)pot).limitBound(limitForPotentialPrunning);
  }
  return pot;
}


/**
 * Transforms a <code>PotentialTree</code> after a
 * marginalization-by-addition operation.
 * IMPORTANT: the potential passed as parameter is modified.
 * @param pot the <code>PotentialTree</code> to be transformed.
 * @return the transformed <code>PotentialTree</code>.
 */

public Potential transformPotentialAfterAddition(Potential pot) {

  if (pot.getClassName().equals("PotentialTree")) {
    if (applySortAndBound) pot =((PotentialTree)pot).sortAndBound(
                                      maximumSizeForPotentialPrunning);
    if (limitForPotentialPrunning != 0) 
      ((PotentialTree)pot).limitBound(limitForPotentialPrunning);
  }
  return pot;
}


/**
 * Transforms a Relation if its values are of class
 * <code>PotentialTree</code>.
 * The only thing to do is the pruning of
 * nodes whose children are equal, so we use a very small value
 * as limit for pruning.
 * This method can be overloaded for special requirements.
 * @param r the <code>Relation</code> to be transformed.
 * @return the transformed Relation
 */

public Relation transformRelation(Relation r) {

  Potential pot;

  pot = r.getValues();
  if (pot.getClassName().equals("PotentialTree")) {
    pot = transformPotentialAfterCombination((PotentialTree)pot);
    r.setValues((PotentialTree)pot);
  }
  if (pot.getClassName().equals("PotentialMTree")) {
    pot = transformPotentialAfterCombination((PotentialMTree)pot);
    r.setValues((PotentialMTree)pot);
  }
  return r;
}

/**
 * Duplicates the object which receives the call. 
 * @param probabilities a <code>boolean</code> indicating if the 
 * probabilistic information has also to be duplicated
 */

public JoinTree duplicate(boolean probabilities){
  JoinTree jt;
  int i;
  NodeJoinTree node;
  NeighbourTreeList ntl,ntl2;

  jt = new JoinTree();
 
  // duplicating cliques
  for(i=0;i<this.getJoinTreeNodes().size();i++){
    node = this.elementAt(i);
    jt.insertNodeJoinTree(node.duplicate(probabilities));    
  }

  // duplicating/creating the neighbourhood structure
  for(i=0;i<jt.getJoinTreeNodes().size();i++){
    ntl = this.elementAt(i).getNeighbourList();
    ntl2 = jt.duplicateNeighbours(ntl,probabilities);
    jt.elementAt(i).setNeighbourList(ntl2);
  }

  // rest of member variabls

  jt.isMPST = this.isMPST;

  // jointreestatistics
  jt.setStatistics(this.getStatistics().duplicate());

  return jt;
}

/**
 * creates a NeighbourTreeList  for a given node by copying
 * a given NeighbourTreeList. However, references to NodeJoinTrees
 * are updated to the cliques in the jt which receives the call.
 * 
 * @param list the <code>NeighbourTreeList</code> to be used as original
 * @param probabilites a <code>boolean</code> indicating if the potential
 * stored in messages have to be copied
 * @return a <code>NeighbourTreeList</code>
 */

private NeighbourTreeList duplicateNeighbours(NeighbourTreeList list,
					boolean probabilities){
  NeighbourTreeList ntl=new NeighbourTreeList();
  int i,p;
  NeighbourTree nt,ntNew;
  NodeJoinTree node,nodeNew=new NodeJoinTree();
  Relation rel,relNew;

  
  for(i=0;i<list.size();i++){
    nt = list.elementAt(i);
    node = nt.getNeighbour();
    rel = nt.getMessage();
    // locating the image of this node in jt
    p = this.indexOf(node.getVariables());
    if (p==-1){
      System.out.println("\n\n -- JoinTree.duplicateNeighbours: the nodejointree has not been located. Exiting ...\n"); 
      System.exit(0);
    }
    else nodeNew = this.elementAt(p);
    // copying the relation
    relNew = rel.copy(probabilities);
    // building the new neighbourtree
    ntNew = new NeighbourTree();
    ntNew.setNeighbour(nodeNew);
    ntNew.setMessage(relNew);
    // adding the new neighbourtree
    ntl.insertNeighbour(ntNew);
  } 

  return ntl;
}



/**
 * Transform a given join tree into a MPSTree.
 * The MPST is constructed by amalgating those cliques which do not
 * share a complete separator in the moral graph.
 * When two nodes Ci and Cj are fused, the probabilistic 
 * information (potential) is computed as psi(C_i \cup C_j)
 * = \psi(C_i)\times\psi(C_j), 
 *
 * IMPORTANT!!! the object which receives the call is modified
 * 
 * REQUIREMENT: the cliques MUST be ordered in joinTreeNodes, in such
 * a way that clique in position 0 is the root, and interpreting the
 * join tree as a rooted tree, then children appear later than its parent
 *
 * @param g the moral <code>Graph</code> from which the join tree
 * was obtained
 * @param probabilities a boolean indicating if probabilistic
 * information should be computed or not.
 */

public void toMPST(Graph gMoral, boolean probabilities){
  this.toMPST(gMoral,1.0,probabilities);
}

/**
 * Transform a given join tree into a MPSTree.
 * The MPST is constructed by amalgating those cliques which do not
 * share a complete separator in the moral graph.
 * In fact this happens when alpha=1.0. In other case you can
 * relax the condition by indicating an smaller alpha, but then
 * you are not getting a true MPST.
 * When two nodes Ci and Cj are fused, the probabilistic 
 * information (potential) is computed as psi(C_i \cup C_j)
 * = \psi(C_i)\times\psi(C_j), 
 *
 * IMPORTANT!!! the object which receives the call is modified
 * 
 * REQUIREMENT: the cliques MUST be ordered in joinTreeNodes, in such
 * a way that clique in position 0 is the root, and interpreting the
 * join tree as a rooted tree, then children appear later than its parent
 *
 * @param g the moral <code>Graph</code> from which the join tree
 * was obtained
 * @param alpha a double indicating the degree of relaxing when computing MPS
 * @param probabilities a boolean indicating if probabilistic
 * information should be computed or not.
 */

public void toMPST(Graph gMoral, double alpha, boolean probabilities){
  int i,j,s,n;
  NodeJoinTree node,parent; 
  NeighbourTreeList ntl;
  NeighbourTree nt;
  NodeList separator=new NodeList();

  // taking into account the constraint imposed about the order of cliques
  // in joinTreeNodes, we can easily procced in a bottom-up way
  
  setLabels();
  s = joinTreeNodes.size();
  for(i=s-1; i>0; i--) {
    node = elementAt(i);
    if (node.getIsSimplicial()) node.setIsMPS(true);
    else{
      ntl = node.getNeighbourList();
      for(j=0 ; j<ntl.size() ; j++){
        nt = ntl.elementAt(j);
        parent = nt.getNeighbour();
        if ( parent.getLabel() < node.getLabel() ){
          //parent has been identified
          separator = nt.getMessage().getVariables();
          if (alpha == 1.0){
            if ( !gMoral.isComplete(separator) ){//fuse them into a MPS
              amalgate(parent,node,probabilities);
            }
            else node.setIsMPS(true);
          }
          else{
            n = separator.size();
            if (n>1){ 
              int numLinksInComplete = (n*n - n)/2; 
              n = gMoral.numberOfLinks(separator);
              double d = (n/(double)numLinksInComplete);
              //if (d != 1.0)
              //  System.out.println("[" + numLinksInComplete + " - " + n + "] .... -> " + d);
              if (d < alpha) {//fuse them into a MPS
                amalgate(parent,node,probabilities);
              }
              else node.setIsMPS(true);
            }  
            else node.setIsMPS(true);
          }
          break;
        }
      }
    }
  }
  ((NodeJoinTree)elementAt(0)).setIsMPS(true);

} // end of toMPST


/**
 * Creates a MPSTree from a given join tree. The original join
 * tre is not modified, and a correspondence is stablished between
 * the two structures (cliques an MPSs)
 *
 * The MPST is constructed by amalgating those cliques which do not
 * share a complete separator in the moral graph.
 *
 * Probabilities are not taken into account, that is, the MPS does
 * not store any probabilistic information (potentials)
 *
 * REQUIREMENT: the cliques MUST be ordered in joinTreeNodes, in such
 * a way that clique in position 0 is the root, and interpreting the
 * join tree as a rooted tree, then children appear later than its parent
 *
 * @param g the moral <code>Graph</code> from which the join tree
 * was obtained
 * @return a join tree
 */

public JoinTree getMPST(Graph gMoral){
  int i,j,k,s,n,pos;
  NodeJoinTree node,parent,nodeImage,mps; 
  NeighbourTreeList ntl;
  NeighbourTree nt;
  NodeList separator=new NodeList();
  JoinTree mpst;

  this.setLabels();
  mpst = this.duplicate(false);

  // taking into account the constraint imposed about the order of cliques
  // in joinTreeNodes, we can easily procced in a bottom-up way
  
  s = mpst.getJoinTreeNodes().size();
  for(i=s-1; i>0; i--) {
    node = mpst.elementAt(i);

    if (node.getIsSimplicial()){
      //stablishing the correspondence
      if (!node.getIsMPS()){
        pos = this.indexOf(node.getVariables());
        if (pos==-1){
          System.out.println("*** ERROR ***: JoinTree.getMPST - the clique does not exists. Exiting ...\n");
          System.exit(0);
        }
        nodeImage = this.elementAt(pos);
        node.makeCorrespondenceWithClique(nodeImage);
      }
      //end of correspondence
      node.setIsMPS(true);
    }
    else{
      ntl = node.getNeighbourList();
      for(j=0 ; j<ntl.size() ; j++){
        nt = ntl.elementAt(j);
        parent = nt.getNeighbour();
        if ( parent.getLabel() < node.getLabel() ){
          //parent has been identified
          separator = nt.getMessage().getVariables();
          if ( !gMoral.isComplete(separator) ){//fuse them into a MPS
            //stablishing correspondence with parents
            if (!node.getIsMPS()){
              pos = this.indexOf(node.getVariables());
              if (pos==-1){
                System.out.println("+++ ERROR +++: JoinTree.getMPST - the clique does not exists. Exiting ...\n");
                System.exit(0);
              }
              nodeImage = this.elementAt(pos);
              node.makeCorrespondenceWithClique(nodeImage);
              nodeImage.setCorrespondingMPS(parent);  
            }
            else{
              for(k=0 ; k<this.joinTreeNodes.size() ; k++){
                mps = this.elementAt(k).getCorrespondingMPS();
                if (mps != null)
                  if (mps.getLabel() == node.getLabel())
                    this.elementAt(k).setCorrespondingMPS(parent);
              }
            }
            
            if (!parent.getIsMPS()){
              pos = this.indexOf(parent.getVariables());
              if (pos==-1){
                System.out.println("--- ERROR ---: JoinTree.getMPST - the clique does not exists. Exiting ...\n");
                System.exit(0);
              }
              nodeImage = this.elementAt(pos);
              parent.makeCorrespondenceWithClique(nodeImage);
            }

            //end of correspondence
            mpst.amalgate(parent,node,false);
            parent.setIsMPS(true);
          }
          else{
            //stablishing the correspondence
            if (!node.getIsMPS()){
              pos = this.indexOf(node.getVariables());
              if (pos==-1){
                System.out.println("<<< ERROR >>>: JoinTree.getMPST - the clique does not exists. Exiting ...\n");
                System.exit(0);
              }
              nodeImage = this.elementAt(pos);
              node.makeCorrespondenceWithClique(nodeImage);
            }
            //end of correspondence 
            node.setIsMPS(true);
          }
          break;
        }
      }
    }
  //this.display3();
  //mpst.displayMPST();
  }
  //stablishing correspondence with parents
  node = mpst.elementAt(0);
  if (!node.getIsMPS()){
    pos = this.indexOf(node.getVariables());
    if (pos==-1){
      System.out.println("-/- ERROR -\\-: JoinTree.getMPST - the clique does not exists. Exiting ...\n");
      System.exit(0);
    }
    nodeImage = this.elementAt(pos);
    node.makeCorrespondenceWithClique(nodeImage);
  }
  //end of correspondence
  ((NodeJoinTree)elementAt(0)).setIsMPS(true);

  mpst.setLabels();
  return mpst;
} // end of toMPST



/**
 * This method amalgates the two nodes (nodeA and nodeB) sharing
 * a link in the join tree into a single clique. 
 * The result is stored into nodeA and nodeB is deleted. All the
 * references to nodeB are repaced by nodeA.
 *
 * if probabilities is true the new potential is updated
 *
 * @param nodeA a <code>NodeJoinTree</code>
 * @param nodeB a <code>NodeJoinTree</code> 
 * @param probabilities a <code>boolean</code> indicating if the potential
 * has to be constructed
 */

public void amalgate(NodeJoinTree nodeA, NodeJoinTree nodeB, 
		     boolean probabilities){
  int i,j;
  ArrayList listA,listB;  
  Relation rel;
  Potential pot,pot2;
  NeighbourTreeList ntlA,ntlB,ntlC;
  NeighbourTree nt,nt2;
  NodeJoinTree nodeC;

  // updating families
  listA = nodeA.getFamilies();
  listB = nodeB.getFamilies();
  if (listA == null){
    if (listB != null) listA = listB;
  }
  else if (listB != null){
    listA.ensureCapacity(listB.size());
    for(i=0 ; i<listB.size() ; i++)
      listA.add(listB.get(i));
  }
 
  // updating cliques
  listA = nodeA.getCliques();
  listB = nodeB.getCliques();
  if (listA == null){
    if (listB != null) listA = listB;
  }
  else if (listB != null){
  listA.ensureCapacity(listB.size());
  for(i=0 ; i<listB.size() ; i++)
    listA.add(listB.get(i));
  }


  // other variables
  nodeA.setCorrespondingMPS(null);
  
  // updating variables   
  nodeA.getVariables().join(nodeB.getVariables());

  // updating relation

  if (probabilities){
    rel = new Relation();
    rel.setVariables(nodeA.getVariables());
    pot2 = nodeA.getNodeRelation().getValues();
    pot = pot2.combine(nodeB.getNodeRelation().getValues());
    rel.setValues(pot);
    nodeA.setNodeRelation(rel);
  }else{
    rel = nodeA.getNodeRelation();
    rel.setVariables(nodeA.getVariables());
    rel.setValues(null);
  }
    

  // updating tree structure
  ntlA = nodeA.getNeighbourList();
  ntlB = nodeB.getNeighbourList();
  for( i=0 ; i<ntlB.size() ; i++){
    nt = ntlB.elementAt(i);
    nodeC = nt.getNeighbour();
    if (nodeC.getLabel() != nodeA.getLabel() ){
      ntlA.insertNeighbour(nt);
      
      ntlC = nodeC.getNeighbourList();
      for(j=0 ; j<ntlC.size() ; j++){
        nt2 = ntlC.elementAt(j);
        if ( nt2.getNeighbour().getLabel() == nodeB.getLabel() ){
          nt2.setNeighbour(nodeA);
        }
      }
    }
  }
  // deleting nodeB 
  nodeA.removeNeighbour(nodeB); 
  removeNodeJoinTree(nodeB);

} // end of amalgate 

/**
 * This methods returns an ArrayList containing the decomposition of a
 * graph induced by a JoinTree (its main use is to apply it to a MPSTree)
 *
 * @return an <code>ArrayList</code> of <code>NodeList</code>
 */

public ArrayList getDecomposition( ){
  int i,s;
  ArrayList list;

  s = joinTreeNodes.size();
  list=new ArrayList(s);

  for(i=0 ; i<s; i++)
    list.add(elementAt(i).getVariables());    
  
  return list;
}

}   // end of class
