/* GTriangulation.java */

package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ArrayList;
import elvira.parser.ParseException;
import elvira.tools.JoinTreeStatistics;
import elvira.tools.TriangulationData;
import elvira.*;
import java.util.Random;
import java.lang.Math;
import elvira.tools.Crono; 


/**
 * Class <code>GTriangulation</code>.
 * Implements the graph's triangulation. The main difference with
 * Triangulation.class (also in this package) is that Triangulation 
 * uses the relations in the network to obtain the elimination 
 * sequence, while GTriangulation used the graph
 *
 * 
 * <li>
 * <li>EXAMPLES:
 * <li>
 * <li>1) Obtaining a join tree
 *
 * <code>
 * <li>Bnet b;
 * <li>Graph g,gMoral;  
 * <li>Random generator = new Random();
 * <li>JoinTree jt;
 * <li>
 * <li>.....
 * <li>
 * <li>jt = new JoinTree();
 * <li>g = (Graph)b; // the bnet b has to be initialised
 * <li>gMoral = g.moral();
 * <li>jt.treeOfCliquesByGTriangulation(gMoral,
 * <li>      "CanoMoral", // replace by another heuristic if you want
 * <li>      "yes",       // set to "no" for random tie-breaking    
 * <li>      generator,
 * <li>      true         // set to false if no minimal triangulation is required
 * <li>);
 * </code>
 *
 *
 * @author Julia Flores
 * @author Jose A. Gámez 
 *
 * @since 4/7/2003
 */

public class GTriangulation {

/**
 * Contains the fill-in links that have been added during triangulation.
 */

private LinkList addedFillIns;

/**
 * Each elements of this array will be a <code>LinkList</code> 
 * containing the fillins created during the deletion of a node
 */

private ArrayList groupedAddedFillIns; 
   
/**
 * A <code>NodeList</code> containing the triangulation/deletion
 * sequence 
 */

private NodeList triangulatedNodes;  
   
/**
 * Contains the <code>Graph</code> to be triangulated. (The initial one)
 */

private Graph graphToTriangulate;

/**
 * Contains the list of cliques identified by when deleting
 * simplicial nodes (clique = NodeJoinTree)
 */

private ArrayList simplicialCliques;

/**
 * Creates an empty <code>Gtriangulation</code> object.
 */

public GTriangulation() {
  addedFillIns = new LinkList();
  groupedAddedFillIns = new ArrayList();
  triangulatedNodes = new NodeList();
  simplicialCliques = new ArrayList();
}
    
/** 
 * Creates new <code>Gtriangulation</code> indicating the graph
 * @param g a <code>Graph</code>
 */

public GTriangulation(Graph g) {
  addedFillIns = new LinkList();
  groupedAddedFillIns = new ArrayList();
  triangulatedNodes = new NodeList();
  simplicialCliques = new ArrayList(g.getNodeList().size()/2); // an estimation
  graphToTriangulate = g;
  g.getNodeList().sortByNames(); 
}

//
// ----------------- Access metthods -----------------
//

/**
 * This method is used for accessing to the list of added links
 *
 * @return a <code>LinkList</code> containing the links (fill-ins)
 * added during the triangulation
 */

public LinkList getAddedLinks(){
  return addedFillIns;
}


/**
 * This method is used for accessing to the list of added links created
 * when deleting each node
 *
 * @return an <code>ArrayList</code> which in its position contains 
 * the links added when deleting that node
 */

public ArrayList getGroupedAddedLinks(){
  return groupedAddedFillIns;
}


/**
 * This method is used for accessing the list of simplicial cliques
 *
 * @return an <code>ArrayList</code> containing the simplicial cliques
 */

public ArrayList getSimplicialCliques(){
  return simplicialCliques;
}


//
// ------- methods for triangulating a graph ------
//


/**
 * This is an auxiliar method for reduceGraph.
 * @param g a <code>Graph</code>
 * @return the number of variables in the largest possible clique
 */

private int maxVarsInClique(Graph g){
  int i;
  int max = 0;
  FiniteStates node;
  NodeList nl;
  
  nl = g.getNodeList();
  
  for(i=0;i<nl.size();i++){
    node = (FiniteStates) nl.elementAt(i);    
    if ((node.getNumNeighbours()+1)>max) max=node.getNumNeighbours()+1;
  }
  return max;
}


/**
 * Given a <code>Graph</code> and the name of one of its nodes,
 * this method returns the number of links to add in the
 * case that this node would be eliminated in a triangulation
 * process.
 * @param nameOfNode the name of the corresponding node
 * @param g the <code>Graph</code> we are working with
 * @return the number of links to add
 */

public int linksToAdd(Node node){
    int j,k,numNeighbours;
    Node nodeJ,nodeK;
    int num=0;
    NodeList listOfNodes;
    
    numNeighbours = node.getNumNeighbours(); 
    
    /* We basically compute the number of neighbours
     * that this node has and if they are not connected 
     * we must accumulate one link to add in case if elimination*/
    
    for(j=0;j<numNeighbours-1;j++){
        nodeJ = node.getNeighbourAt(j);
        for(k=j+1;k<numNeighbours;k++){
            nodeK = node.getNeighbourAt(k);
            if (!(nodeK.isNeighbour(nodeJ))) num++;
        }
    }
    
    return num;    
}



/**
 * Reduce a graph by removing simplicial nodes.
 * IMPORTANT: the graph is modified.
 * @param g is the <code>Graph</code> to be reduced
 * @return an <code>ArrayList</code> containing the sequence of 
 * removed nodes
 */

public TriangulationData reduceGraph(Graph g) throws elvira.InvalidEditException{
  boolean deleted;
  int i,t,pos;
  FiniteStates n;
  NodeList nl,vars;
  String name;
  NodeJoinTree clique;
  Relation rel;
  TriangulationData td = new TriangulationData();
  boolean maximal;
  double cliqueSize;
  NodeList delNodes;  

  delNodes = new NodeList(g.getNodeList().size()); // a rough estimation 

  nl = g.getNodeList();
  
  for(deleted=true; deleted==true; ){
    deleted = false;
    for(t=maxVarsInClique(g); t>1; t--)
      for(i=0;i<nl.size();i++){
        n = (FiniteStates)nl.elementAt(i);
        name = n.getName();

        //If the maximum number of variables in a
        //clique is 2 and the number of neighbours is different
        //from 1 we see the next node

        if ((t==2) && (n.getNumNeighbours()!=1)) continue;

        //here we see the number of node of clique that would appear
        //from the elimination of the node n. If this is equal to the
        //maximum number of variables in the clique, we will
        //check if it is a simplicial node, and if so we add his name
        //to the deletedNodes

        if ((n.getNumNeighbours()+1)==t)
          if (linksToAdd(n)==0){
            delNodes.insertNode(n);
            //creating clique
            vars = new NodeList();
            vars.insertNode(n);
            vars.join(n.getChildrenNodes());
            vars.join(n.getParentNodes());
            vars.join(n.getSiblingsNodes());
            rel = new Relation();
            rel.setVariables(vars);
            clique = new NodeJoinTree(rel);
            clique.setIsSimplicial(true);
            maximal = false;
            if (!isContainedIn(clique,simplicialCliques)){
              simplicialCliques.add(clique);
              maximal=true;
            }
            cliqueSize = calculateSize(clique);
            //deleting node
            g.removeNode(n); 
            deleted = true;
            //updating data
            td.update(cliqueSize,maximal,clique.getVariables().size());
          }
      }

  }
  td.setSequence(delNodes);  

  return td;
}


/**
 * Build a nodelist with the elements contained in <code>ArrayList</code>
 * v, that contains the name of the nodes in the <code>Graph</code> g 
 * from which we will obtain the NodeList
 *
 * @param v an <code>ArrayList</code>
 * @param g a <code>Graph</code>
 * @return a <code>NodeList</code> containing the nodes in v
 */

public NodeList buildNodeList(ArrayList v, Graph g){
  NodeList nl = new NodeList();
  int i,pos;
  NodeList listOfNodes;
  FiniteStates node;
  String name;
  
  listOfNodes = g.getNodeList();
  
  for(i=0;i<v.size();i++){
    name = (String)v.get(i);
    node = (FiniteStates) listOfNodes.getNode(name);
    nl.insertNode(node);
  }

  return nl;
}



/**
 * getTriangulation. Looks for a deletion sequence for triangulation
 * applying a certain heuristic method:
 * 	- minSize
 *      - CanoMoral
 *      - minFill      
 * The data returned is the deletion sequence, the fillins, the size 
 * associated to that deletion sequence, ...
 *
 * @param criterion a <code>String</code> indicating the mode (minSize,CanoMoral,minFill)
 * @param arbitrary a <code>String</code> which value "yes" for arbitrary
 * decision and "no" for breaking ties randomly
 * @param generator a random number generator
 * @param g the undirected (and previously moralized) <code>Graph</code> to
 * be triangulated
 * @return a <code>TriangulationData</code> object containing 
 * diverse data about the deletion sequence
 */

public TriangulationData getDeletionSequence(String criterion,
		String arbitrary, Random generator, Graph g) 
					throws elvira.InvalidEditException{
  int i=0; //We could choose it randomly
  NodeList listOfNodes = g.getNodeList();
  int numNodes = listOfNodes.size();
  ArrayList cliqueList;
  GTriangulationRankedPairList pl;
  TriangulationData td = new TriangulationData();
  double cliqueSize;
  boolean maximal;
  NodeList delSequence = new NodeList(g.getNodeList().size());
  Node candidateNode;
  NodeJoinTree clique;
  NodeList vars;

  cliqueList = new ArrayList(g.getNodeList().size());

  pl = new GTriangulationRankedPairList(g,criterion);   
  for(i=0;i<numNodes;i++){
    candidateNode = pl.getNodeToEliminate(arbitrary,generator);
    clique = this.eliminateNode(candidateNode,g,true); 
    cliqueSize = calculateSize(clique);
    maximal = false; 
    if (!isContainedIn(clique,cliqueList))
      if (!isContainedIn(clique,simplicialCliques)){
      maximal=true;
      cliqueList.add(clique);
      } 
    td.update(cliqueSize,maximal,clique.getVariables().size());
    pl.updateAllExceptIndex(clique.getVariables(),candidateNode,g,criterion);
    pl.removeElementAt(pl.getIndexOfNodeInList(candidateNode));
    delSequence.insertNode(candidateNode);
  }
  td.setSequence(delSequence);
  return td;
}

/**
 * Given a graph and the name of one of its nodes
 * this method returns the size of the clique produced
 * in case that this node would be eliminated in a triangulation
 * processs.
 * @param nameOfNode the name of the corresponding node
 * @param g the Graph we are working with
 * @return the state space size of the produced clique
 */

public double sizeOfClique(Node node){
  int j;
  FiniteStates nodeJ;
  double SIZE;
    
  SIZE = (double) ((FiniteStates)node).getNumStates();
 
  /* We multiply the size of this node by the sizes of
   * its different neihbours */ 
  for(j=0;j<node.getNumNeighbours();j++){
    nodeJ = (FiniteStates) node.getNeighbourAt(j);
    SIZE *= (double) nodeJ.getNumStates();
  }
    
  return SIZE;
}

/**
 * Given a graph and the name of one of its nodes
 * this method returns the number of variables in the
 * clique produced in the case that this node would 
 * be eliminated in a triangulation processs.
 * @param nameOfNode the name of the corresponding node
 * @param g the Graph we are working with
 * @return the number of variables of the produced clique
 */
public int numberOfVars(Node node){

  /* We return the number of neighbours (they will form the clique)
   * and also adding this node */
  
  return (node.getNumNeighbours()+1);
}



/**
 * Given a graph and the name of one of its nodes
 * this method returns the sum of the sizes of the cliques
 * in case that this node would be eliminated in a triangulation
 * processs.
 * @param nameOfNode the name of the corresponding node
 * @param g the Graph we are working with
 * @return the sum of the sizes of the cliques
 */

public double sizeOfSumCliques(Node node){
  int i,j,k;
  FiniteStates nodeI,nodeJ,nodeK;
  double SIZE, sizeNode, subSize;
  int numNeighbours;
  int active[]; //1 if the clique is enabled; 0 is the clique is disabled
  Vector cliques = new Vector();
  boolean contained;
  
  numNeighbours = node.getNumNeighbours();
  active = new int[numNeighbours];
  
  for(i=0;i<numNeighbours;i++){
      active[i] = 1;
      nodeI = (FiniteStates) node.getNeighbourAt(i);
      //Better using Relation??
      Vector cli = new Vector();
      cli.addElement(nodeI);
      cliques.addElement(cli);
  }
  
  //We initialise cli again
  Vector cli = new Vector();
  
  for(i=0;i<numNeighbours;i++){
      nodeI = (FiniteStates) node.getNeighbourAt(i);
      for(j=0;j<i;j++){
          if (active[j]==1){
              cli = (Vector) cliques.elementAt(j);
              contained = true;
              for(k=0;k<cli.size();k++){
                  nodeK = (FiniteStates)cli.elementAt(k);
                  if (!nodeK.isNeighbour(nodeI)) {
                      contained = false;
                      break;
                  }
              }
              if (contained){
                  active[i]=0;
                  cli.addElement(nodeI);
                  break;
              }
          }
      } //end for j
  }//end for i
  
  SIZE=0.0; 
  sizeNode = (double) ((FiniteStates)node).getNumStates();
  for(i=0;i<numNeighbours;i++){
      if (active[i]==1){
          cli = (Vector) cliques.elementAt(i);
          subSize = sizeNode;
          for(j=0;j<cli.size();j++){
              nodeJ = (FiniteStates) cli.elementAt(j);
              subSize *= (double) nodeJ.getNumStates();
          }//end for j
          SIZE +=subSize;
      }//end if
  }//end for i
  
  return SIZE;
}


/**
 * Performs the elimination process of the node with name n.
 * We will save in addedFillins the added links not used yet.
 * 
 * @param n the <code>Node</code> to be eliminated
 * @param g the <code>Graph</code> being triangulated
 * @param triangulate a <code>boolean</code>  indicating if triangulation
 * has to be carried out
 * @return an <code>NodeJoinTree</code> containing the clique 
 *         obtained when deleting node n
 */

public NodeJoinTree eliminateNode(Node n, Graph g, boolean triangulate) 
					throws elvira.InvalidEditException{
  NodeJoinTree clique;
  Relation rel;
  int i,j,k;
  FiniteStates nodeJ,nodeK;
  int nodePos;
  LinkList fillIns = new LinkList();  
  int it = 0,numNeighbours;
  NodeList vars;
  Node node,node2;

  node = g.getNodeList().getNode(n.getName());

  // creating the clique to be returned
  numNeighbours=node.getNumNeighbours();
  vars = new NodeList(numNeighbours+1,true);
  vars.insertNode(node);
  for(i=0;i<numNeighbours;i++){
    node2 = node.getNeighbourAt(i); 
    vars.insertNode(node2);
  }

  // filling the graph
  for(j=0;j<(numNeighbours-1);j++){
    nodeJ = (FiniteStates) node.getNeighbourAt(j);
    for(k=j+1;k<numNeighbours;k++){
      nodeK = (FiniteStates) node.getNeighbourAt(k);
      if (!(nodeK.isNeighbour(nodeJ))){ // adding the link
        nodeJ.addNeighbour(nodeK);
        it++;
        Link newLink = new Link(nodeJ,nodeK,new String("fill-in_" +it));
        newLink.setDirected(false);
        try{
          g.createLink(nodeJ,nodeK,false);
          if (triangulate) { //Adding the link 
            newLink = new Link(nodeJ,nodeK,new String("fill-in_" +it),false);
            addedFillIns.insertLink(newLink);
            fillIns.insertLink(newLink);
          }
        }catch(InvalidEditException iee){
          if (iee.getCode() != 1){ // duplicate link
            if (triangulate) { //Adding the link 
              newLink = new Link(nodeJ,nodeK,new String("fill-in_" +it),false);
              addedFillIns.insertLink(newLink);
              fillIns.insertLink(newLink);
            }
            throw iee; // sending the exception
          } 
          
        }
        
      } 
    }
  }

  if (fillIns.size()>0) groupedAddedFillIns.add(fillIns); 
   
  //deleting the node from the graph
  try{
    g.removeNode(node);
  }catch(InvalidEditException iee){
    System.out.println("Exception about inexistence of node to be deleted "
			+ "has been captured. Code = " + iee.getCode());
  }

  // returning the clique
  rel = new Relation();
  rel.setVariables(vars);
  clique = new NodeJoinTree(rel);

  
  return clique;
}


/**
 * computes the state space size of the clique passed as parameter
 * @param clique a <code>NodeJoinTree</code> 
 * @return a <code>double</code> which represent the state space size
 * 	of the clique passed as parameter
 */
public double calculateSize(NodeJoinTree clique){
  double cliqueSize = 1.0;
  NodeList nl = clique.getVariables();

  for(int j=0;j<nl.size();j++){
    cliqueSize *= ((FiniteStates)nl.elementAt(j)).getNumStates();
  }
  return cliqueSize;
}



/**
 * @param sigma a <code>NodeList</code> with the deletion sequence
 * @param g the <code>Graph</code> graph to be triangulated
 * @return the state space size of the jointree built using this 
 * elimination sequence.
 * 
 */

public TriangulationData triangulateNetwork(NodeList sigma, Graph g) 
					throws elvira.InvalidEditException{
  return triangulateNetwork(sigma,g,Double.MAX_VALUE);
}

public TriangulationData triangulateNetwork(NodeList sigma, Graph g,
					double maxSizeAllowed) 
					throws elvira.InvalidEditException{

  int i;
  ArrayList cliqueList;
  NodeJoinTree clique;
  double cliqueSize;
  FiniteStates node;
  TriangulationData td = new TriangulationData();
  boolean maximal = false; 
 
  cliqueList = new ArrayList(g.getNodeList().size());  
  
  for(i=0;i<sigma.size();i++){
    node = (FiniteStates) sigma.elementAt(i);
    clique = this.eliminateNode(node,g,true); 
    maximal = false;
    if (!isContainedIn(clique,cliqueList))
      if (!isContainedIn(clique,simplicialCliques)){
        maximal = true; 
        cliqueList.add(clique);
      }
    cliqueSize = calculateSize(clique); //pepe
    td.update(cliqueSize,maximal,clique.getVariables().size());
    if (td.getCliqueTreeSize() > maxSizeAllowed) break;
  }

  return td;
}


/**
 * @param clique a <code>NodeJoinTree</code>  
 * @param cliqueList an <code>ArrayList</code> of <code>NodeJoinTree</code>
 * representing cliques 
 *
 * @return true if the clique passed as argument is
 * a sub-clique of some other clique in cliqueList 
 * Return false in other case.
 */

public boolean isContainedIn(NodeJoinTree clique,ArrayList cliqueList){
  int i,j,k;
  ArrayList v;
  String J,K;   
  boolean found;
  Object object;
  NodeJoinTree node;


  for(i=0;i<cliqueList.size();i++){
    node = (NodeJoinTree) cliqueList.get(i);
    if (clique.getVariables().isIncluded(node.getVariables())){
      return true;
    }
  }
  
  return false;
}


/**
 *
 * It implements the method RecursiveThinning (PhD Thesis Kjaerulff). 
 * Do not destroy the graph passed as parameter
 *
 * @param T a <code>LinkList</code> with the added fill-ins
 * @param g the <code>Graph</code> the moral graph being triangulated
 * @param R an auxiliary <code>LinkList</code>. Set R=T in the initial
 * 	call
 * @return the list of links that represents a minimal triangulation 
 */                 

public LinkList MINT(LinkList T, Graph g, LinkList R) {
   LinkList Rprime,Tprime;
   int i,j,k,comun;
   Link l,l2;
   Node nodeH,nodeT,node1,node2;
   NodeList nlH,nlT,listOfNodes,nl;
   int complete;

   listOfNodes = g.getNodeList();

   // computing Rprime
   // Rprime = edges of T s.t. they have non-empty intersection 
   //             with some other edge of R
       
   Rprime = new LinkList();
   for(i=0;i<T.size();i++){
     l = (Link) T.elementAt(i);
     comun=0;
     for(j=0;j<R.size();j++){
       l2 = (Link) R.elementAt(j);
       if (l.getTail().getName().equals(l2.getTail().getName())) comun=1;
       else if (l.getTail().getName().equals(l2.getHead().getName())) comun=1;
       else if (l.getHead().getName().equals(l2.getTail().getName())) comun=1;
       else if (l.getHead().getName().equals(l2.getHead().getName())) comun=1;
       if (comun == 1) break; 
     }
     if (comun == 1) Rprime.insertLink(l);
   }

   // Computing Tprime
   // Tprime = edges (X,Y) from Rprime s.t. adj(X) \cap adj(Y) is
   //             a complete graph in G.

   Tprime = new LinkList();     
   for(i=0;i<Rprime.size();i++){
     l = (Link) Rprime.elementAt(i);
     nl = new NodeList();
     // adjacents in G
     nodeH = (FiniteStates)listOfNodes.getNode(l.getHead().getName());
     nodeT = (FiniteStates)listOfNodes.getNode(l.getTail().getName());
     nlH = g.neighbours(nodeH);
     nlT = g.neighbours(nodeT);
     // adjacents because T
     for(j=0;j<T.size();j++){
       l2 = T.elementAt(j);
       if (l2.getHead().getName().equals(nodeH.getName())) 
         nlH.insertNode(l2.getTail());
       if (l2.getTail().getName().equals(nodeH.getName())) 
         nlH.insertNode(l2.getHead());
       if (l2.getHead().getName().equals(nodeT.getName())) 
         nlT.insertNode(l2.getTail());
       if (l2.getTail().getName().equals(nodeT.getName())) 
         nlT.insertNode(l2.getHead());
     }

     // intersection
     nl = nlH.intersection(nlT);
     nl = listOfNodes.intersection(nl);

     // computing if nl induces a complete subgraph in G \cup links(T)
     if (isComplete(nl,g,T)) 
     { Tprime.insertLink(l);
       T.removeLink(l);
     }

   }

   // finishing

   if (Tprime.size() > 0) {
     //LinkList aux = T.difference(Tprime);
     //return MINT(aux,g,Tprime);
     return MINT(T,g,Tprime);
   }

   return T;
} 


/**
 * Verify if the subgraph induced by the list of nodes (nl) is
 * complete in G, but taking also into account the links passed as parameter
 * 
 * @param nl a <code>NodeList</code> with the set of nodes whose induced
 *	subgraph has to be verified
 * @param g a <code>Graph</code>
 * @param ll a <code>LinkList</code> used as aditional links for the graph,
 *	when verifying completeness 
 * @return true if the induced subgraph is complete and false in other case
 */

public boolean isComplete(NodeList nl, Graph g, LinkList ll){
  Node nodeI,nodeJ;
  int i,j;
  
  for(i=0;i<nl.size();i++){ 
    nodeI = nl.elementAt(i);
    for(j=i+1;j<nl.size();j++){
      nodeJ = nl.elementAt(j);
      if (!(nodeI.isNeighbour(nodeJ))){
	if (ll.getID(nodeI.getName(),nodeJ.getName()) == -1)
          return false;
      }
    }
  }

  return true;
}


/**
 * Fill a graph with a set of provided links.
 * @param g is the <code>Graph</code> we are going to triangulate adding links
 * @param links is a <code>LinkList</code> where we specify the fill-ins to add
 */
public void fillGraph(Graph g, LinkList links) throws InvalidEditException{
  Link l;
  int i;
  FiniteStates H,T,nodeH,nodeT;
  NodeList listOfNodes = g.getNodeList();
  
  for(i=0;i<links.size();i++){
    l = (Link) links.elementAt(i);
    H = (FiniteStates) l.getHead();
    T = (FiniteStates) l.getTail();
    g.createLink(listOfNodes.getNode(H.getName()),
		listOfNodes.getNode(T.getName()),false); //not directed
  }

}


/**
 * Obtains the numbering for the list of nodes in the <code>Graph</code>
 * g by applying the well known algorithm: maximum cardinality search.
 * The following restrictions are applied:
 *     - if first is equal to -1, then the first node in nodelist is
 *       selected
 *  @param initial the first node for numbering
 *  @param g the graph whose nodes are to be numbered
 *  @return an array with the list of ordered nodes.
 */

public int[] maximumCardinalitySearch(int initial,Graph g) {
  
  int[] visited; // the numbering to be returned
  NodeList neighbours,nodes; // the neighbours for a node
  Node node,nodeNext;        
  int i, j, k, numberOfNodes, next;
  int numNeighbours[]; // contains in position i the number of 
                       // numbered neighbours for the node
                       // triangulatedNodes.elementAt(i)
                       // if the value is -1 the node not will be 
                       // considered
  
  nodes = g.getNodeList();

  numberOfNodes = nodes.size();  
  numNeighbours = new int[numberOfNodes];
  for (i=0 ; i<numberOfNodes ; i++) 
    numNeighbours[i]=0;
  
  visited = new int[numberOfNodes];

  //initialising next  
  if (initial >= numberOfNodes) 
    System.out.print("Incorrect parameter for MCS\n");	
  if (initial==-1) next=0;
  else next=initial;


  
  // begining the numeration process
  
  for (i=0 ; i<numberOfNodes ; i++) {
    nodeNext = nodes.elementAt(next);
    // inserting the node and setting its value to -1
    visited[i]=next;
    numNeighbours[next] = -1;
    // getting a nodelist with all the neighbours of next
    neighbours = g.neighbours(nodeNext);
    // for all node in neighbours do numberofneigbours++
    for (j=0 ; j<neighbours.size() ; j++){
      node = neighbours.elementAt(j); 
      k = nodes.getId(node);
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
    }
    
  }        
  
  return visited;
}


/**
 * This function says if the value id is already in
 * the array v. It returns -1 if it doesn't exist in v 
 * and the position otherwise.
 * 
 * @param v an array of ints
 * @param id an <code>int</code> containing the value we are looking for
 * @param max the maximum number of positions in v actually filled
 * @return the position in wich id is contained in v, or -1 in other case
 */
private int inArray(int v[],int id,int max)
{
    int i;
    for(i=0;i<max;i++)
    {
        if (v[i]==id)
        return i;
    }
    return -1;    
}



/**
 * Function that gets the cliques in a tree
 * It follows the corresponding algorithm, taking the nodes in
 * the inverse order to nubering it forms cliques taking all the 
 * adjacent nodes with a lower number (this is possible
 * thanks to one property of the MCS)
 * @param numbering is an array whose index represents the numbering order and 
 * they contain the node associated with this order
 * @param g is the "triangulated" <code>Graph</code> over which we 
 *	search the cliques
 * @return an <code>ArrayList</code> of cliques, with the names of the nodes
 */

public ArrayList identifyCliques(int numbering[], Graph g)
{ 
  int my_index;
  int i,j,k,indexK;
  ArrayList myCliqueList;
  NodeJoinTree clique;
  FiniteStates NodeI,NodeK;
  NodeList listOfNodes = g.getNodeList();
  int numNodes = listOfNodes.size();
  int numNeighbours;
  NodeList vars;
  Relation rel;

  myCliqueList = new ArrayList(g.getNodeList().size());


  for(i=numNodes-1;i>=0;i--)
  {	  
    my_index = numbering[i]; 
    //We take the variables or nodes in the inverse order to the numbering
    NodeI = (FiniteStates) listOfNodes.elementAt(my_index);
    numNeighbours = NodeI.getNumNeighbours();
    vars = new NodeList(numNeighbours); //an estimation
    vars.insertNode(NodeI);
    for(k=0;k<numNeighbours;k++)
    {
      //We are only interested in those neighbours with a lower number.
      //So, their position in numbering must be between 0 and i-1	  
      NodeK = (FiniteStates) NodeI.getNeighbourAt(k);
      indexK = listOfNodes.getId(NodeK);
      if ( inArray(numbering,indexK,numNodes) < i) 
          vars.insertNode(NodeK);
    }//end for k

    rel = new Relation();
    rel.setVariables(vars);
    clique = new NodeJoinTree(rel); 

    //We are only interested in maximal clusters
    if (!isContainedIn(clique,myCliqueList)) 
      if (!isContainedIn(clique,simplicialCliques))
        myCliqueList.add(clique); 
  }//end for i

  return myCliqueList;
}

/**
 * A tree is build with the obtained cliques. The resulting structure
 * verify the junction tree property.
 * @param cliques is an <code>ArrayList</code> of elements (also
 * <code>ArrayList</code> of Strings (they will be the names of 
 * the contained nodes)
 * @param g is the "non-triangulated" <code>Graph</code> from which we 
 * build the tree.
 * @return a <code>Vector</code> with the cliques (<code>JoinTreeNodes</code>) in the tree 
 */
public Vector buildTree(ArrayList cliques,Graph g)
{ 
  RelationList relations;
  Relation rel;
  NodeList vars;
  int i,j;
  NodeJoinTree NodeJT,NodeJT2;
  Vector listOfNodeJoinTree = new Vector();
  Vector listOfNodeJoinTree2 = new Vector();
  int maxIntersect,sumIntersectJ=0;
  int indexNeigh=-1;
        
  for(i=0;i<cliques.size();i++)
  {
    NodeJT = (NodeJoinTree)cliques.get(i);    
    listOfNodeJoinTree.addElement(NodeJT);
  }

  //adding simplicialCliques (if any)
  if (simplicialCliques != null){
    for(i=simplicialCliques.size()-1 ; i>=0; i--) {
      NodeJT = (NodeJoinTree) simplicialCliques.get(i);
      listOfNodeJoinTree.insertElementAt(NodeJT,0);
    }
  }


  // end of adding

  int sizeList = listOfNodeJoinTree.size();
    
  for(i=sizeList-1;i>=0;i--) 
  {   
    NodeJT = (NodeJoinTree)listOfNodeJoinTree.elementAt(i);
    NodeJT.setLabel(sizeList-1-i);
    maxIntersect = 0;
    indexNeigh = -1;
    for(j=sizeList-1;j>i;j--){   
      NodeJT2 = (NodeJoinTree)listOfNodeJoinTree.elementAt(j);
      //We need to know the number of variables in the intersection
      sumIntersectJ = (NodeJT.getVariables().intersection(
					NodeJT2.getVariables())).size();
      if (sumIntersectJ>maxIntersect){
        maxIntersect = sumIntersectJ;
        indexNeigh = j;
      }
    }
    //We must make NodeJT be neighbour of the nodeJoinTree in indexNeigh
    if (indexNeigh!=-1){ //If that doesn't happen is because this Clique is disconnected
      NodeJT2 = (NodeJoinTree) listOfNodeJoinTree.elementAt(indexNeigh);
      //Insert neighbour will be charged of construction the message (separator)
      NodeJT.insertNeighbour(NodeJT2);
      NodeJT2.insertNeighbour(NodeJT);
    }
    else{
      //the first one is never disconnected, if the second one of any of
      //the next nodes is we will connect it to the first one by an empty separator
      if (i!=sizeList-1){
        NodeJT2 = (NodeJoinTree) listOfNodeJoinTree.elementAt(sizeList-1);
        NodeJT.insertNeighbour(NodeJT2);
        NodeJT2.insertNeighbour(NodeJT);            
      }       
    }
        
    listOfNodeJoinTree2.addElement(NodeJT);
  }   
  return listOfNodeJoinTree2;
}



//
// ----------- MAIN ---------------
//


/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * </ol>
 */

public static void main(String args[]) throws ParseException, IOException,
			elvira.InvalidEditException {

  Bnet b;
  FileInputStream networkFile;
  Graph g,gMoral,g2;  
  GTriangulation gt;
  ArrayList list;
  Family family;
  NodeList reducedNodes,sigma;
  int i;
  Random generator = new Random();
  JoinTree jt;
  JoinTreeStatistics jts;
  Crono crono=new Crono();

  if (args.length < 1) {
    System.out.print("Too few arguments. Arguments are: ElviraFile");
    System.out.println();
  }
  else {
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);

    jt = new JoinTree();
    g = (Graph)b;
    gMoral = g.moral();
    jt.treeOfCliquesByGTriangulation(gMoral,
          "CanoMoral", // replace by another heuristic if you want
	  "yes",       // set to "no" for random tie-breaking    
	  generator,
          true         // set to false if no minimal triangulation is required
    );
    jt.calculateStatistics();
    jts = jt.getStatistics();

    System.out.println("\nThe state space size of the jt is: " + 
				jts.getJTSize() + "\n\n");


    crono.start();
    //jt.initTables2(b);
    //System.out.println("Con initTables2 " + crono.getTime() + " seconds");
    //crono.toCero();
    //jt.initTables(b);
    //System.out.println("Con initTables " + crono.getTime() + " seconds");    
    //crono.stop();

    //jt.display3();
    JoinTree jt2 = jt.duplicate(false);
    System.out.println("\n" + crono.getTime() + " segundos en hacer la copia\n");
    crono.toCero();
    //System.out.println("Imprimiendo la copia ....");
    //jt2.display();
    jt2.toMPST(gMoral,1.0,false);
    System.out.println("\n" + crono.getTime() + " segundos en obtener el MPST\n");
    list = jt2.getDecomposition();
    System.out.println("\nThe MPSD has " + list.size() + " subgraphs\n");

/*
    for(i=0;i<jt.getJoinTreeNodes().size();i++){
      NodeJoinTree node = jt.elementAt(i);
      System.out.println(node.getVariables().toString2());
      list = node.getFamilies();
      for(int j=0;j<list.size();j++){
        family = (Family)list.get(j);
        System.out.println("\t"+family.getNode().getName());
      }
    }
*/
    //jt.display();

 /*
    g = new Graph(b.getNodeList(),b.getLinkList(),0);
 
    gt = new GTriangulation(g);
    gt.pPrint(g);      
    gMoral = g.duplicate();
    gMoral = gMoral.moral();
    gt.pPrint(gMoral);
    g2 = gMoral.duplicate();

    list = gt.reduceGraph(gMoral);
    reducedNodes = gt.buildNodeList(list,g);
    gt.pPrint(gMoral);
    System.out.println("\n\nSequence of eliminated nodes in reduction process:\n");
    for(i=0;i<reducedNodes.size();i++)
      System.out.println((reducedNodes.elementAt(i)).getName());

    list2 = gt.getDeletionSequence("minSize","yes",generator,gMoral);
    sigma = gt.buildNodeList(list2,g);
    System.out.println("\n\nSequence of eliminated nodes after reduction process:\n");
    for(i=0;i<sigma.size();i++)
      System.out.println((sigma.elementAt(i)).getName());
    
    reducedNodes.join(sigma);
    System.out.println("\nThe state space size of the triangulated graph is:"+
       ((JoinTreeStatistics)gt.triangulateNetwork(reducedNodes,g2)).getJTSize());
  
*/
    

  }

} // end of main


//
// -------------- methods for debugging by printing structure
// -------------- on the screen. They will be deleted when
// -------------- finishing the class
// 


 
/** 
 * print a graph in a pretty way, indicating the nodes and the links.   
 */
public void pPrint(Graph g){
  int i,j;
  FiniteStates n,n2;
  int numLinks = 0;
  NodeList listOfNodes;
  
  listOfNodes = g.getNodeList();

  System.out.println("The graph has " + listOfNodes.size() + " nodes. The links are: \n\n");

  for(i=0;i<listOfNodes.size();i++){
    n = (FiniteStates) listOfNodes.elementAt(i);
    for(j=0;j<n.getNumNeighbours();j++){
      n2 = (FiniteStates) n.getNeighbourAt(j); 
      if (((String)n.getName()).compareTo((String)n2.getName())<0){ 
        System.out.println("("+ n.getName() +
               "," + n2.getName() +")");
        numLinks++;
      }
    }
  }
  System.out.println("\nThe graph has " + numLinks + " links\n");
}



/** 
 * print a graph in a pretty way indicating the nodes and the number
 * of links that it contains.   
 */

public void printData(Graph g){
  int i,j;
  FiniteStates n,n2;
  int numLinks = 0;
  NodeList listOfNodes;

  listOfNodes = g.getNodeList();
  
  System.out.print("\n\nThe graph has " + listOfNodes.size() + " nodes and ");

  for(i=0;i<listOfNodes.size();i++){
    n = (FiniteStates) listOfNodes.elementAt(i);
    for(j=0;j<n.getNumNeighbours();j++){
      n2 = (FiniteStates) n.getNeighbourAt(j);
      if (((String)n.getName()).compareTo((String)n2.getName())<0){ 
        System.out.println("("+ n.getName() +
               "," + n2.getName() + ")");
        numLinks++;
      }

  }
  System.out.println(numLinks + " links\n");
}
}


/** 
 * print a graph indicating the links.   
 */

public void print(Graph g){
  int i,j;
  FiniteStates n,n2;
  NodeList listOfNodes;
  
  listOfNodes = g.getNodeList();

  System.out.println("The links of the graph are: \n\n");
  for(i=0;i<listOfNodes.size();i++){
    n = (FiniteStates) listOfNodes.elementAt(i);
    for(j=0;j<n.getNumNeighbours();j++){
      n2 = (FiniteStates) n.getNeighbourAt(j);
      if (((String)n.getName()).compareTo((String)n2.getName())<0) //No se si al cambiar de índices a nombre vale,creo que si
        System.out.println("("+n.getName()+","+n2.getName()+")");
    }
  }
  System.out.println();
}



}// end of class
