/* Graph.java */

package elvira;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import elvira.*;

import elvira.parser.BayesNetParse;


/**
 * A graph is a set of nodes and a set of links among them.
 * We allow at most one link for each pair of nodes.
 *
 * @version 0.1
 * @since 19/06/2007
 */

public class Graph implements Cloneable, ConditionalIndependence, Serializable {

static final long serialVersionUID = 138760915463606263L;
/**
 * Name of the graph.
 */
private String name = "";   
   
/* KINDS OF GRAPH */
   
/**
 * Constrained property name indicating that the graph is directed.
 */
public static final int DIRECTED = 0;

/**
 * Constrained property name indicating that the graph is undirected.
 */   
public static final int UNDIRECTED = 1;

/**
 * Constrained property name indicating that the graph is mixed.
 */
public static final int MIXED = 2;

/**
 * Kind of this graph (see possible kinds above).
 */
private int kindOfGraph;

/**
 * An array of strings that contains the possible kinds of graph.
 * The position of the strings in the array are the same that the
 * integers assigned to instance variable <code>kindOfGraph</code>.
 */
private static final String kindNames[]= {"directed","undirected","mixed"};

/**
 * List of nodes in the graph.
 */
protected NodeList nodeList;

/**
 * List of links in the graph.
 */
protected LinkList linkList;


/**
 * Creates a new empty graph whose kind is given as parameter.
 * @param i the kind of graph.
 */

public Graph (int kind) {
  
  nodeList = new NodeList();
  kindOfGraph = kind;
  linkList = new LinkList();
}


/**
 * Creates a new empty and directed graph.
 */

public Graph () {
  
  this(DIRECTED);
}


/**
 * Builds a graph with the given lists of nodes and links.
 * The kind of the graph is given as parameter too.
 * @param nodes the list of nodes of the new graph.
 * @param links the list of links of the new graph.
 * @param kind the kind of the new graph.
 */

public Graph (NodeList nodes, LinkList links, int kind) {
  
  kindOfGraph = kind;
  nodeList = nodes;
  linkList = new LinkList();
  
  if (links.size() == 0)
    linkList = new LinkList();
  else {
    try{
      Link link;
      Enumeration linklist = links.elements();
      while (linklist.hasMoreElements()) {
	link = (Link) linklist.nextElement();
	Node tail=nodeList.getNode(link.getTail().getName());
	Node head=nodeList.getNode(link.getHead().getName());	
	createLink(tail, head, link.getDirected());
      }
    }
    catch (InvalidEditException iee){
      System.out.println("The graph can't be constructed");
    }
  }
}



/**
 * Builds a graph with the given lists of nodes and links.
 * The kind of the graph is given as parameter too.
 * It contains a parameter that it is to false no check
 * of cycles is carried out
 * @param nodes the list of nodes of the new graph.
 * @param links the list of links of the new graph.
 * @param kind the kind of the new graph.
 * @param test boolean: to false non test is carrried out
 */

public Graph (NodeList nodes, LinkList links, int kind, boolean test) {
  
  kindOfGraph = kind;
  nodeList = nodes;
  linkList = new LinkList();
  
  
  if (links.size() == 0)
    linkList = new LinkList();
  else {
    try{
      Link link;
      Enumeration linklist = links.elements();
      while (linklist.hasMoreElements()) {
	link = (Link) linklist.nextElement();
      if(test) {	createLink(link.getTail(), link.getHead(), link.getDirected());}
      else {	createLinkNonTest(link.getTail(), link.getHead(), link.getDirected());}
      }
    }
    catch (InvalidEditException iee){
      System.out.println("The graph can't be constructed");
    }
  }
}

/**
 * Copy constructor. Creates a completely new graph
 * from another that has been previously defined.
 * @param g a previously created graph.
 */

public Graph (Graph g) {

  name=g.name;
  kindOfGraph = g.kindOfGraph;
  nodeList = g.getNodeList().duplicate();
  linkList = new LinkList();
  
  if (g.linkList.size() != 0) {
    try {
      int i, post, posh;
      Link l, laux;
      LinkList ll = new LinkList();
      
      for (i=0 ; i<g.linkList.size() ; i++) {
	laux = g.linkList.elementAt(i);
	post = nodeList.getId(laux.getTail().getName());
	posh = nodeList.getId(laux.getHead().getName());
	l = new Link(nodeList.elementAt(post),nodeList.elementAt(posh),laux.getDirected());
	ll.insertLink(l);
      }
      
      Enumeration linkList = ll.elements();
      while (linkList.hasMoreElements()) {
	l = (Link) linkList.nextElement();
	createLink(l.getTail(),l.getHead(),l.getDirected());
      }
    }
    catch (InvalidEditException e) {
      System.out.println("The graph can not be created");
    }
  }
}


/**
 * Creates a new random graph with a given number of nodes.
 * @param generator the random numbers generator.
 * @param numberOfNodes the number of nodes.
 * @param nParents average of parents for each node.
 * @param con <code>true</code> if we want a connected graph.
 */

public Graph(Random generator, int numberOfNodes, double nParents,
	     boolean con) {
  
  Node node, nodep, nodeh;
  Link link; 
  NodeList pa, nodesCon, total;
  Vector connected = new Vector(), others;
  SetVectorOperations s = new SetVectorOperations();
  int i, j;
  
  try {
    kindOfGraph = DIRECTED;             /* by default directed */
    nodeList = new NodeList();
    linkList = new LinkList();
    for (i=0 ; i<numberOfNodes ; i++) {
      node = new FiniteStates(2);
      node.setName("x"+i);
      nodeList.insertNode(node);
      
      // Now the random parents of <code>node</code> are generated.

      pa = randomParents(generator,node,nParents);
      for (j=0 ; j < pa.size() ; j++) {
	nodep = (Node)pa.elementAt(j);
	
	// Now, to attach the nodes with the links and update fields
	// parents, children y siblings.
	
	createLink(nodep,node,true);
      }
    }
    
    // Now we seek the connected components of the graph and store them
    // in <code>Vector connected</code>.
    if (con) {
      total = new NodeList();
      nodesCon = new NodeList();
      undirectedAccessibles((Node)nodeList.elementAt(0),nodesCon);
      total.join(nodesCon);
      while (nodesCon != null) {
      	connected.addElement(nodesCon);
        others = s.notIn(nodeList.toVector(),total.toVector());
        if (others.size() > 0) {
      	  node = (Node)others.elementAt(0);
      	  nodesCon = new NodeList();
      	  undirectedAccessibles(node,nodesCon);
      	  total.join(nodesCon);
        }
      	else
          nodesCon = null;
      }
      
      // Now join the connected components of the graph.
      
      for (i=0 ; i< connected.size()-1 ; i++) {
	nodep = (Node)((NodeList)connected.elementAt(i)).elementAt(0);
	nodeh = (Node)((NodeList)connected.elementAt(i+1)).elementAt(0);

	// Now, attach the nodes with the links and update fields
	// parents, children y siblings.
	
	createLink(nodep,nodeh,true);
      }
    }
  }
  catch (InvalidEditException iee){
    System.out.println("The graph can't be generated randomly");
  }

}
   

/* ********************* Accesing methods ******************** */

/**
 * Gets the name of the graph.
 * @return the name.
 */

public String getName() {

	return name;
}

/**
 * Determines whether the graph is empty or not.
 * @return <code>true</code> if the graph has no nodes, <code>false</code>
 * otherwise.
 */

public boolean isEmpty() {
  
  return (nodeList.size() == 0);
}


/**
 * Gets the kind in the graph.
 * @return the kind in the graph.
 */

public int getKindOfGraph() {
  
  return kindOfGraph;
}

/**
 * Gets the kind of the graph as a String
 * @return the kind in of graph as a String
 */

public String getKindOfGraphAsString() {
  
  return kindNames[kindOfGraph];
}


/**
 * Gets a list with the nodes in the graph.
 * @return a list with the nodes in the graph.
 */

public NodeList getNodeList() {
  
  return nodeList;
}


/**
 * Gets the links in the graph.
 * @return a <code>LinkList</code> with all the links in the graph.
 */

public LinkList getLinkList() {

  return linkList;
}


/* ********************* Methods for read/save graphs ******************** */

/**
 * Saves the list of nodes in the text output stream given as parameter.
 * @param p the <code>PrintWriter</code> where the list will be saved.
 */

public void saveNodeList(PrintWriter p) {

  p.print("// Variables \n\n");

  for (int i=0 ; i<getNodeList().size() ; i++) {
    ((Node) getNodeList().elementAt(i)).save(p);
  }
}


/**
 * Saves the links of the graph in the variable given as argument.
 * @param p text output stream used to saved the links of the graph.
 */

public void saveLinkList(PrintWriter p) {

  p.print("// Links of the associated graph:\n\n");

  for (int i=0 ; i<getLinkList().size() ; i++) {
    ((Link) getLinkList().elementAt(i)).save(p);
  }
}



/**
 * Saves all the variables of the graph using the text output
 * stream given as parameter. The graph is saved with the Elvira
 * format.
 * @param p <code>PrintWriter</code> where the network is saved.
 * @see saveNodeList
 * @see saveLinkList
 */

public void save(PrintWriter p) throws IOException {

  saveHead(p);

  p.print("// Graph Properties\n\n");
  p.print("kindofgraph = \""+getKindOfGraphAsString()+"\";\n");

  saveNodeList(p);
  saveLinkList(p);
  p.print("}\n");
}


/**
 * Saves the graph to a file.
 * @param nameOfFile the name of the file.
 */

public void save(String nameOfFile) throws IOException {

  FileWriter f;
  PrintWriter p;

  f = new FileWriter(nameOfFile);
  p = new PrintWriter(f);
  save(p);
  f.close();
}


/**
 * Saves the header of the Graph.
 * @param p <code>PrintWriter</code> where the graph is saved.
 */

public void saveHead(PrintWriter p) throws IOException {

  p.print("// Graph\n");
  p.print("// Elvira format \n\n");
  
  if ( this.getName().equals("") )
      p.print("graph noName { \n\n");
  else
      p.print("graph "+this.getName()+" { \n\n");
}


/**
 * Reads a graph from a file.
 * @param nameOfFile the name of the file.
 * @return the read graph.
 */

public static Graph readGraph(String nameOfFile) throws elvira.parser.ParseException ,IOException {

  FileInputStream f;
  Graph graph = null;

  f = new FileInputStream(nameOfFile);
  BayesNetParse parser = new BayesNetParse(f);
  parser.initialize();

  parser.CompilationUnit();
  if (parser.Type.equals("graph")) {
      if (parser.KindOfGraph.equalsIgnoreCase( new String("mixed") )) 
	  graph = new Graph(MIXED);
      else if (parser.KindOfGraph.equalsIgnoreCase( new String("directed") )) 
	  graph = new Graph(DIRECTED);
      else if (parser.KindOfGraph.equalsIgnoreCase( new String("undirected") ))
	  graph = new Graph(UNDIRECTED);
      else 
	  graph = new Graph();
  } else {
      System.out.println("Error in Graph.readGraph(String nameOfFile): This isn't a Graph type");
      System.exit(1);
  }
 graph.translate(parser);
  f.close();
  return graph;
}

/**
 * Gets the value for all the instance variables of the graph
 * from the parser.
 * @param parser contains all the information about the
 * network (read from a file).
 */

public void translate(BayesNetParse parser) {
  //Set name
  setName(parser.Name);

  //Set Kind of graph
  if (parser.KindOfGraph != null) {
      if (parser.KindOfGraph.equalsIgnoreCase( new String("mixed") )) 
	  setKindOfGraph(MIXED);
      else if (parser.KindOfGraph.equalsIgnoreCase( new String("directed") )) 
	  setKindOfGraph(DIRECTED);
      else if (parser.KindOfGraph.equalsIgnoreCase( new String("undirected") ))
	  setKindOfGraph(UNDIRECTED);
  }

  //Set NodeList and LinkList
  try{
    nodeList = parser.Nodes;

    /**
     * The parser contains the list of links and the list of nodes but not
     * the list of links of each node that acts as parent, child or
     * sibling. Therefore the graph that represents the network with all
     * this information must be built now. It is done creating a new link for
     * each one of the list of links in the parser and inserting it
     * in the graph calling to the method createLink defined in class Graph.
    */
    Link link;
    Enumeration links = parser.Links.elements();
    while (links.hasMoreElements()) {
      link = (Link) links.nextElement();
      createLink(link.getTail(), link.getHead(), link.getDirected());
    }
  }
  catch (InvalidEditException iee){
    System.out.println("The Graph can't be translated");
  }

}


/* ********************* Modifiers ******************** */

/**
 * Sets the name of the graph.
 * @param s the new name.
 */

public void setName(String s) {

    if (s.equals("")){
	name = "noname";
    } else if (s.substring(0,1).equals("\"")) {
	name = new String(s.substring(1,s.length()-1));
    } else {
	name = new String(s);
    }
}


/**
 * Method for setting the list of nodes in the graph.
 * @param nodes the new list of nodes.
 */
 
public void setNodeList (NodeList nodes) {
  
  nodeList = nodes;
}


/**
 * Method for setting the list of the links in the graph.
 * @param links the new list of links.
 */

public void setLinkList (LinkList links) {
  
  linkList = links;
}


/**
 * Sets the kind of the graph.
 * @param i the new kind.
 */

public void setKindOfGraph(int i) {
  
  if ((i<3) && (i>=0))
    kindOfGraph = i;
}



/**
 * Sets if all the nodes are visited or not
 * @param visited if it's true, all the nodes was 
 *                visited, else if it's false, all the nodes wasn't visited
 */
public void setVisitedAll(boolean visited) {
    for (int i=0; i < this.nodeList.size(); i++) {
	this.nodeList.elementAt(i).setVisited(visited);
	
	NodeList cnl=this.nodeList.elementAt(i).getChildrenNodes();
	NodeList pnl=this.nodeList.elementAt(i).getParentNodes();
	NodeList snl=this.nodeList.elementAt(i).getSiblingsNodes();
	
	for (int j=0;j < cnl.size(); j++)
	    cnl.elementAt(j).setVisited(visited);
	
	for (int j=0;j < pnl.size(); j++)
	    pnl.elementAt(j).setVisited(visited);
	
	for (int j=0;j < snl.size(); j++)
	    snl.elementAt(j).setVisited(visited);
    }//end for i
    
}//end setVisitedAll method


/* ************* Add / Remove nodes and arcs ************ */

/**
 * Inserts a node in the graph if it doesn't exist in the graph.
 * @param n the node to insert.
 */

public void addNode(Node n) throws InvalidEditException{
  
  if (nodeList.getId(n.getName()) != -1)
    throw new InvalidEditException(0);
  else nodeList.insertNode(n);
}


/**
 * Dettach a node and its adjacent links from the graph
 * without destroying the node.
 * @param n the node to remove.
 */

public void removeNode(Node node) throws InvalidEditException {
  
  node = nodeList.getNode(node.getName());
  if (node != null){
    
    Link l;
    LinkList linksl;
    
    // remove links to children
    linksl = node.getChildren();
    int posl=0;
    while (posl < linksl.size()) {
      l = linksl.elementAt(posl);
      l.getHead().getParents().removeLink(l.getHead().getParents().indexOf(l));
      linksl.removeLink(l);
      linkList.removeLink(l);
    }
    // remove links to parents
    linksl = node.getParents();
    while (posl < linksl.size()) {
      l = linksl.elementAt(posl);
      l.getTail().getChildren().removeLink(l.getTail().getChildren().indexOf(l));
      linksl.removeLink(l);
      linkList.removeLink(l);
    }
    
    // remove links to siblings
    linksl = node.getSiblings();
    Node sibling;
    
    while (posl < linksl.size()) {
      l = linksl.elementAt(posl);
      sibling = l.getTail();
      if (sibling.equals(node))
	sibling = l.getHead();
      sibling.getSiblings().removeLink(sibling.getSiblings().indexOf(l));
      linksl.removeLink(l);
      linkList.removeLink(l);
    }
    
    //remove node from the list of nodes
    nodeList.removeNode(node);
  }
  else throw new InvalidEditException(2);
}


/**
 * Dettach a node and its adjacent links from the graph
 * without destroying the node.
 * @param position the position of the node to be removed.
 * @see#removeNode(Node)
 */

public void removeNode(int position) throws InvalidEditException{
  
  removeNode(nodeList.elementAt(position));
}


/**
 * Add a new link, between tail and head, in the link
 * list. The kind of the link is given as parameter.
 * @param head second node of the link.
 * @param tail first node of the link.
 * @param directed kind of node.
 */

     public void createLink (Node tail, Node head, boolean directed) throws InvalidEditException{
      Node t,h;
      Link l;
      boolean insert;
      int pos,posSibT,posSibH,posChT,posPaH;
      int post=nodeList.getId(tail);
      int posh=nodeList.getId(head);
      if ((post!=-1)&&(posh!=-1)){
          if ((((kindOfGraph==DIRECTED) && (directed)) || ((kindOfGraph==UNDIRECTED) && (!directed))) || (kindOfGraph==MIXED))
              insert=true;
              else insert=false;
      }
      else insert=false;
      if (insert){
	 pos = getLinkList().getID(tail.getName(),head.getName()); 
	 if(pos == -1){
           Vector acc = new Vector();
           acc = directedDescendants(head);
           if(((directed) && (acc.indexOf(tail) == -1)   )   || 
              ((kindOfGraph == UNDIRECTED)&&(!directed))||
              ((kindOfGraph == MIXED))){
	     l=new Link (tail, head, directed);
             linkList.insertLink(l);
             if (!directed){
		posSibT = tail.getSiblings().getID(tail.getName(),
						   head.getName());
		posSibH = head.getSiblings().getID(tail.getName(),
						   head.getName());
		if((posSibT == -1) && (posSibH == -1)){
		    tail.getSiblings().insertLink(l);                
		    head.getSiblings().insertLink(l);
		}
             }
             if (directed){
		 posChT = tail.getChildren().getID(tail.getName(),
						   head.getName());
		 posPaH = head.getParents().getID(tail.getName(),
						  head.getName());
		 if((posChT==-1) && (posPaH == -1)){
		     tail.getChildren().insertLink(l);                 
		     head.getParents().insertLink(l);
		 }
              }
           } else throw new InvalidEditException(5); 
         }
         else throw new InvalidEditException(1);
      }
      else throw new InvalidEditException(3);
   }//end of method createlink

/**
 * Add a new link, between tail and head, in the link
 * list. The kind of the link is given as parameter.
 * @param head second node of the link.
 * @param tail first node of the link.
 * @param directed kind of node.
 */

     public void createLinkNonTest (Node tail, Node head, boolean directed) throws InvalidEditException{
      Node t,h;
      Link l;
      boolean insert;
      int pos,posSibT,posSibH,posChT,posPaH;
      int post=nodeList.getId(tail);
      int posh=nodeList.getId(head);
      if ((post!=-1)&&(posh!=-1)){
          if ((((kindOfGraph==DIRECTED) && (directed)) || ((kindOfGraph==UNDIRECTED) && (!directed))) || (kindOfGraph==MIXED))
              insert=true;
              else insert=false;
      }
      else insert=false;
      if (insert){
	 pos = getLinkList().getID(tail.getName(),head.getName()); 
	 if(pos == -1){
           Vector acc = new Vector();
           acc = directedDescendants(head);
           if(((directed))   || 
              ((kindOfGraph == UNDIRECTED)&&(!directed))||
              ((kindOfGraph == MIXED))){
	     l=new Link (tail, head, directed);
             linkList.insertLink(l);
             if (!directed){
		posSibT = tail.getSiblings().getID(tail.getName(),
						   head.getName());
		posSibH = head.getSiblings().getID(tail.getName(),
						   head.getName());
		if((posSibT == -1) && (posSibH == -1)){
		    tail.getSiblings().insertLink(l);                
		    head.getSiblings().insertLink(l);
		}
             }
             if (directed){
		 posChT = tail.getChildren().getID(tail.getName(),
						   head.getName());
		 posPaH = head.getParents().getID(tail.getName(),
						  head.getName());
		 if((posChT==-1) && (posPaH == -1)){
		     tail.getChildren().insertLink(l);                 
		     head.getParents().insertLink(l);
		 }
              }
           } else throw new InvalidEditException(5); 
         }
         else throw new InvalidEditException(1);
      }
      else throw new InvalidEditException(3);
   }//end of method createlink



/**
 * Add a new link, between head and tail, in the link
 * list. By default the link is directed.
 */

public void createLink (Node tail, Node head) throws InvalidEditException {
  
  createLink(tail,head,true);
}


/**
 * Dettach a link the graph without destroying it.
 * @param l link to remove.
 */

public void removeLink (Link l) throws InvalidEditException {
  
  Node h, t;
  int pos, posl = linkList.indexOf(l);
  
  
  if (posl != -1) {
    h = l.getHead();
    t = l.getTail();
    t.getChildren().removeLink(l);
    h.getParents().removeLink(l);
    if ((kindOfGraph==MIXED) || (kindOfGraph==UNDIRECTED)) {
      h.getSiblings().removeLink(l);
      t.getSiblings().removeLink(l);
    }
    linkList.removeLink (l);
  }
  else throw new InvalidEditException(4);
}

/**
 * Delete the orientation of all the outward arcs of a node. A recursive
 * procedure that undoes the
 * the directed way from a node through its children. 
 */

    public void unorientLink(Node node){
	Link link;
	Node nodeh; // node head, the child
	NodeList nodes = children(node);
	for (int i=0; i< nodes.size(); i++){
	    nodeh =  nodes.elementAt(i);
	    if (parents(nodeh).size()==1){ // it has only one parent, node
		link = getLink(node,nodeh);
		try{
		    removeLink(link);
		    createLink(nodeh,node,false);
		} catch (InvalidEditException iee) { };
		unorientLink(nodeh);
	    }
	}     
    }

/**
 * Removes a specific connection from a dag and restores the rpdag as a 
 * well formed rpdag.
 * A connection may be any kind of link directed or undirected.
 * @param link <code>Link</code>.
 */
    public void removeLinkrepairRPDAG(Link link){

	Node nodeY;
	NodeList plY; // parent list of Y

	try {
	     removeLink(link);
	} catch (InvalidEditException iee) { };
	nodeY=link.getHead();

	int npar= parents(nodeY).size();
	if (link.getDirected())
	    if (npar ==1) { // a h2h has been undone
		plY=parents(nodeY); // only one parent
		if (parents(plY.elementAt(0)).size()==0){
		    //getLink(plY.elementAt(0),nodeY).setDirected(false);

		    try{ 
			  removeLink(getLink(plY.elementAt(0),nodeY));
			  createLink(plY.elementAt(0),nodeY,false);
                    }catch(InvalidEditException iee){};
		    unorientLink(nodeY);
		}
	    }
	    else if (npar==0)
		    unorientLink(nodeY);
	   
       // the cases x--y && ( x->y && npar >=3) nothing to do additionaly
    }

    
/**
 * Dettach a link without destroying it.
 * @param p position of the link to remove
 */

public void removeLink (int p) throws InvalidEditException {
  
  removeLink ((Link)linkList.elementAt(p));
}


/**
 * Dettach a link of the graph without destroying it.
 * @param tail node at the tail of the link to remove.
 * @param head node at the head of the link to remove
 */

public void removeLink (Node tail, Node head) throws InvalidEditException {
 // System.out.println(linkList.toString());
  int pos = linkList.getID(tail.getName(), head.getName());
  
  if (pos != -1)
    removeLink(pos);
  else throw new InvalidEditException(5);
}


/* ****************** Other methods *********************** */



/**
 * Duplicates a graph.
 * @return a new graph with the same nodes and the same links than this one.
 */

public Graph duplicate() {
  
  Graph co = new Graph(getKindOfGraph());
  Node newNode, n;
  
  try{
    for (int posn=0 ; posn<nodeList.size() ; posn++) {
      n = (Node)nodeList.elementAt(posn);
      newNode = n.copy();
      co.addNode(newNode);
    }
    Link l;
    Node h, t;
    for (int posl=0 ; posl<linkList.size() ; posl++) {
      l = (Link)linkList.elementAt(posl);
      t = l.getTail();
      h = l.getHead();
      t = co.getNodeList().getNode(t.getName());
      h = co.getNodeList().getNode(h.getName());
      co.createLink(t,h,l.getDirected());
    }
    return co;
  }
  catch (InvalidEditException iee){
    System.out.println("The graph can't be duplicated");
    return null;
  }
}


/**
 * Takes the subgraph that contains the corresponding NodeList
 * @return a new graph with the subset of nodes and links that this graph had
 */
public Graph projectGraph(NodeList nl) {

  Graph co = new Graph(getKindOfGraph());
  Node newNode, n;
  NodeList nextToIt;

  try{
    for (int posn=0 ; posn<nodeList.size() ; posn++) {
      n = (Node)nodeList.elementAt(posn);
      if (nl.getId(n)!=-1){
       newNode = n.copy();
       co.addNode(newNode);
      }
    }
    Link l;
    Node h, t;
    for (int posl=0 ; posl<linkList.size() ; posl++) {
      l = (Link)linkList.elementAt(posl);
      t = l.getTail();
      h = l.getHead();
      if ((nl.getId(t)!=-1)&&(nl.getId(h)!=-1)){
        t = co.getNodeList().getNode(t.getName());
        h = co.getNodeList().getNode(h.getName());
        co.createLink(t,h,l.getDirected());
        //Here in createLink function it adds parents/children (directed)
        //or siblings (undirected)
      }
    }
    return co;
  }
  catch (InvalidEditException iee){
    System.out.println("We can't obtain the subgraph associated to this list of nodes");
    return null;
  }
} 

/**
 * This method is used to obtain the link that contains a given node.
 * @param n the node to find in one of the links.
 * @return an integer with the position of the link
 * or -1 if this link is not in the list.
 */

public int getNodePosition (Node n) {
  
  int position;
  Link link;
  
  for (position=0 ; position < linkList.size() ; position++) {
    link = (Link) linkList.elementAt(position);
    if (n==link.getTail() || n==link.getHead())
      return position;
  }
  
  return (-1);
}


/**
 * Obtain the link between two nodes.
 * @return a link between <code>node1</code> and <code>node2</code>, 
 *         or null if the link does not exist.
 */

public Link getLink(Node node1, Node node2) {
  
  int position;
  Link link;
  
  for (position=0 ; position<linkList.size() ; position++) {
    link = linkList.elementAt(position);
    
    if (node1.equals(link.getTail()) && node2.equals(link.getHead()))
      return (linkList.elementAt(position));
    
    /* If the link from node1 to node2 does not exist
       and the link is not directed, it is necessary to check whether
       a link from node2 to node1 exists */
    
    if (!link.getDirected())
      if (node1.equals(link.getHead()) && (node2.equals(link.getTail())))
	return (linkList.elementAt(position));
  }
  return null;
}


/**
 * Returns the list of parents of a node.
 * @param node the node whose parents are returned.
 * @return a <code>NodeList</code> with the parents of the node.
 */

public NodeList parents(Node node) {
  
  NodeList parents = new NodeList();
  Link link;
  
  if (node.getParents() == null) 
    return null;
  
  for (int i=0 ; i<node.getParents().size() ; i++) {
    link = node.getParents().elementAt(i);
    parents.insertNode (link.getTail());
  }
  
  return parents;  
}


/**
 * Returns the list of parents of a node, on the list passed 
 * as parameter.
 * @param node the node whose parents are returned.
 * @param nodeList the list where the parents are inserted.
 */

public void parents(Node node, NodeList nodeList) {
  
  Link link;
  
  if (node.getParents() == null) 
    return;
  
  for (int i=0 ; i<node.getParents().size() ; i++) {
    link = node.getParents().elementAt(i);
    nodeList.insertNode (link.getTail());
  }
}


/**
 * Returns the list of the children of the given node.
 * @param node node whose children are returned.
 * @return a <code>NodeList</code> with the children of <code>node</code>.
 */

public NodeList children(Node node) {
  
  NodeList children = new NodeList();
  Link link;
  
  for (int i=0 ; i<node.getChildren().size() ; i++) {
    link = node.getChildren().elementAt(i);
    children.insertNode (link.getHead());
  }
  
  return children;
}


/**
 * Returns the list of the siblings of the given node.
 * @param node node whose siblings are returned.
 * @return a <code>NodeList</code> with the siblings of <code>node</code>.
 */

public NodeList siblings(Node node) {
  
  NodeList siblings = new NodeList();
  Link link;
  
  for (int i=0 ; i<node.getSiblings().size() ; i++) {
    link = node.getSiblings().elementAt(i);
    if (node.equals(link.getHead()))
      siblings.insertNode (link.getTail());
    else
      siblings.insertNode (link.getHead());
  }
  
  return siblings;
}


/**
 * Calculates the neighbour nodes of the given node
 * (includes parents, children and siblings).
 * @param node node whose neighbours are returned.
 * @return a <code>NodeList</code> with the neighbours of <code>node</code>.
 */

public NodeList neighbours(Node node) {
  
  NodeList neighborlist = new NodeList();
  Link link;
  Enumeration e = linkList.elements();
  
  while (e.hasMoreElements()) {
    link = (Link) e.nextElement();
    if (node.equals(link.getHead()))
      neighborlist.insertNode (link.getTail());
    if (node.equals(link.getTail()))
      neighborlist.insertNode (link.getHead());
  }
  
  return neighborlist;
}

/**
 * Returns the List of the ascendant nodes of the one given as parameter.
 * To do it, a call to method <code>depthFirst</code> from the node is done.
 * @param node node whose descendants will be obtained.
 * @return a <code>Vector</code> with the descendants of <code>node</code>.
 */

public Vector ascendants(Node node) {
  
  NodeList nodelist=new NodeList();
  Vector resul=new Vector();
  Node nod;
  
 // System.out.println("Computing ascendants of " + node.toString() );
  parents(node, nodelist);
 //  System.out.println("parent size " + nodelist.size());
  if (nodelist.size()>0){
    resul=nodelist.toVector();
    for (int n=0; n<nodelist.size(); n++){
        nod = nodelist.elementAt(n);
       // System.out.println("A parent: " + nod.toString() );
        Vector asc=ascendants(nod);
        for (int a=0; a<asc.size(); a++)
            if (!resul.contains(asc.elementAt(a)))
                    resul.addElement(asc.elementAt(a));
    }
  }
  return resul;
}



/**
 * Returns the List of the ascendant nodes of a node list of nodes (the union
 * of all the ascendants including the list given as parameter.
 * To do it, a call to method <code>depthFirst</code> from the node is done.
 * @param variable node whose descendants will be obtained.
 * @return a <code>Vector</code> with the descendants of <code>node</code>.
 */

public Vector ascendants(Vector list) {
  Node node;
  Vector resul=new Vector();
  int j;  
  
  
  
  
  if (list.size()>0){    
  for (j=0;j<list.size();j++){
      node = (Node) list.elementAt(j);
      if (!resul.contains(node))
      {
          resul.addElement(node);
          // System.out.println("Adding " + node.toString() +  " to ascendants ");
      }
      Vector asc=ascendants(node);
       // System.out.println("ascendants " + asc.size());
      for (int a=0; a<asc.size(); a++)
            if (!resul.contains(asc.elementAt(a)))
            {   resul.addElement(asc.elementAt(a));
                    
               //  System.out.println("Adding " + asc.elementAt(a).toString() +  " to ascendants ");
            }
  }
  }
  return resul;
}


/**
 * This method decides whether a given node is a descendant of all the
 * nodes of a list.
 * @ param a the node whose ascendants are studied.
 * @ param list nodes that are going to be studied in order to know
 * whether they are ascendants of <code>a</code> or not.
 * @ return <code>true</code> if all the nodes of <code>list</code> are
 * ascendants of the node <code>a</code>, <code>false</code> otherwise.
 */

public boolean areAscendantsOf(Node a, NodeList list) {
  Node n;
  boolean areAscendants=true;
  
  for (int i=0 ;(i<list.size())&&areAscendants ; i++) {
  	n=list.elementAt(i);
  	if (descendantOf(n,a)==false){
  		areAscendants=false;
  	}
  }
  return areAscendants;
}

/**
 * Returns the List of the descendant nodes of the one given as parameter.
 * To do it, a call to method <code>depthFirst</code> from the node is done.
 * @param node node whose descendants will be obtained.
 * @return a <code>Vector</code> with the descendants of <code>node</code>.
 */

public Vector descendants(Node node) {
  
  Node n;
  
  for (int i=0 ; i<nodeList.size() ; i++) {
    n = (Node) nodeList.elementAt(i);
    n.setVisited(false);
  }
  node.setVisited(true);
  Vector desc = new Vector();
  depthFirst(node, desc);
  return desc;
}



/**
 * Returns the list of the directed descendant nodes of the one given 
 * as parameter.
 * To do it,  a call to method <code>directedDepthFirst</code> from
 * the node is done.
 * @param node node whose descendants will be obtained.
 * @return a <code>Vector</code> with the descendants of <code>node</code>.
 */

public Vector directedDescendants(Node node) {
  
  Node n;
  
  for (int i=0 ; i<nodeList.size() ; i++) {
    n = (Node) nodeList.elementAt(i);
    n.setVisited(false);
  }
  node.setVisited(true);
  Vector desc = new Vector();
  directedDepthFirst(node, desc);
  return desc;
}


/**
 * Returns the vertices that can be accessed from the node given as parameter.
 * @param node the node from which we departure.
 * @param accessible the vector in which the nodes are going to be stored
 * during the traversal.
 * @return a <code>Vector</code> that contains all the vertices that can
 * be accessed from <code>node</code>.
 */

public Vector depthFirst(Node node, Vector accessible) {
  
  Enumeration e = children(node).elements();
  Node child, sibling;
  
  while (e.hasMoreElements()) {
    child = (Node) e.nextElement();
    if (!child.getVisited()) {
      accessible.addElement(child);
      child.setVisited(true);
      depthFirst(child, accessible);
    }
  }
  
  e = siblings(node).elements();
  while (e.hasMoreElements()) {
    sibling = (Node) e.nextElement();
    if (!sibling.getVisited()) {
      accessible.addElement(sibling);
      sibling.setVisited(true);
      depthFirst(sibling, accessible);
    }
  }
  return accessible;
}


/**
 * Returns the vertices that can be accessed by a directed path from
 * the node given as parameter.
 * @param node the node from which we departure.
 * @param accessible the vector in which the nodes are going to be stored
 * during the traversal.
 * @return a <code>Vector</code> that contains all the vertices that can
 * be accessed from <code>node</code>.
 */

public Vector directedDepthFirst(Node node, Vector accessible) {
  
  Enumeration e = children(node).elements();
  Node child;
  
  while (e.hasMoreElements()) {
    child = (Node) e.nextElement();
    if (!child.getVisited()) {
      accessible.addElement(child);
      child.setVisited(true);
      directedDepthFirst(child, accessible);
    }
  }
  
  return accessible;
}


/**
 * This method decides whether a given node is a descendant of another node.
 * Initially, marks all the nodes in the graph as not visited.
 * @ param a the node for wich its descendants are looked for.
 * @ param b the node that is going to be studied in order to know whether it
 * is descendant of <code>a</code> or not.
 * @ return <code>true</code> if node <code>b</code> is a descendant of
 * node <code>a</code>, <code>false</code> otherwise.
 */

public boolean descendantOf(Node a, Node b) {
  
  Node n;
  
  for (int i=0 ; i<nodeList.size() ; i++) {
    n = (Node) nodeList.elementAt(i);
    n.setVisited(false);
  }
  a.setVisited(true);
  return isThereMixedPath(a,b);
}


/**
 * This method decides whether a given node is a directed descendant of another node.
 * During the traversal, marks the studied nodes as visited.
 * @param a the node for wich its directed descendants are looked for.
 * @param b the node that is going to be studied in order to know whether it
 * is directed descendant of <code>a</code> or not.
 * @return <code>true</code> if node <code>b</code> is a directed descendant of
 * node <code>a</code>, <code>false</code> otherwise.
 */

public boolean isThereDirectedPath(Node a, Node b) {
  
 Node n;
  
  if (a.equals(b))
    return true;
  else { 
      
   
  
  for (int i=0 ; i<nodeList.size() ; i++) {
    n = (Node) nodeList.elementAt(i);
    n.setVisited(false);
  }
  
  return( RecisThereDirectedPath(a,b));
  
  }
}


private boolean RecisThereDirectedPath(Node a, Node b) {
  
    Enumeration e = children(a).elements();
    Node ady;
    boolean found = false;
  
    if (a.equals(b))
    return true;
    
    while ((e.hasMoreElements()) && (! found)) {
      ady = (Node) e.nextElement();
      if (ady.equals(b))
	found = true;
      else if (!ady.getVisited()) {
	ady.setVisited(true);
	found = RecisThereDirectedPath(ady,b);
      }
    }
    return found;
  } //else
//end isTherePath

/**
 * This method decides whether a given node is a descendant of another node.
 * During the traversal, marks the studied nodes as visited.
 * @param a the node for wich its descendants are looked for.
 * @param b the node that is going to be studied in order to know whether it
 * is descendant of <code>a</code> or not.
 * @return <code>true</code> if node <code>b</code> is a descendant of
 * node <code>a</code>, <code>false</code> otherwise.
 */


public boolean isThereMixedPath(Node a, Node b) {
  
  Node n;  
 
  if (a.equals(b))
    return true;
  
  else { 
      
   
  
  for (int i=0 ; i<nodeList.size() ; i++) {
    n = (Node) nodeList.elementAt(i);
    n.setVisited(false);
  }
  
  return( RecisThereMixedPath(a,b));
  
  }  
}

private boolean RecisThereMixedPath(Node a, Node b) {
  
 if (a.equals(b))
    return true;
  
  else{
    Enumeration e = children(a).elements();
    Node ady;
    boolean found = false;
    
    while ((e.hasMoreElements()) && (! found)) {
      ady = (Node) e.nextElement();
      if (ady.equals(b))
	found = true;
      else if (!ady.getVisited()) {
	ady.setVisited(true);
	found = RecisThereMixedPath(ady,b);
      }
    }

    e = siblings(a).elements();
    while ((e.hasMoreElements()) && (!found)) {
	ady = (Node) e.nextElement();
	if (ady.equals(b))
	    found = true;
	else if (!ady.getVisited()) {
	    ady.setVisited(true);
	    found = RecisThereMixedPath(ady,b);
	}
    }
    return found;
  } //else
} //end isThereMixedPath



/**
 * Returns the list of accessible nodes of the node given as parameter.
 * @param variable the node whose accessible nodes will be obtained.
 * @return a <code>NodeList</code> with the descendants of <code>node</code>.
 */

public NodeList descendantsList(Node n) {
  
  Vector v = descendants(n);
  NodeList l = new NodeList(v);
  
  return l;
}


/**
 * Calculates the moral graph using the graph that receives
 * this message. If the graph is undirected returns the same graph.
 * @return the moral graph.
 */

public Graph moral() {
  
  try{
    Graph moralGraph = (Graph) duplicate();
    moralGraph.kindOfGraph = UNDIRECTED;
    
    switch (getKindOfGraph()) {
      
    case UNDIRECTED:
      return moralGraph;
      
        case MIXED:
      return moralGraph;
      
    case DIRECTED:
      
      int j, k;
      Link l;
      NodeList nodeParents;
      Node n, n1, n2;
      Enumeration e1 = moralGraph.nodeList.elements();
      
      while (e1.hasMoreElements()) {
	n = (Node) e1.nextElement();
	nodeParents = parents(n);
	
	for (j=0 ; j<nodeParents.size()-1 ; j++) {
	  n1 = (Node) nodeParents.elementAt(j);
	  
	  for (k=j+1 ; k<nodeParents.size() ; k++) {
	    n2 = (Node) nodeParents.elementAt(k);
	    
	    if ((moralGraph.getLink(n1,n2)==null) &&
		(moralGraph.getLink(n2,n1)==null))
	      
	      moralGraph.createLink(n1,n2,false);
	  }  
	}
      }

      // marking all the links in the graph as undirected

      LinkList ll = moralGraph.getLinkList();
      for(j=0;j<ll.size();j++)
        ((Link)ll.elementAt(j)).setDirected(false);

      return moralGraph;
    }
    // case MIXED: not implemented
    return null;
  }
  catch (InvalidEditException e){
    System.out.println("The graph can't be moralized because it can't be duplicated");
    return null;
  }
}


/**
 * This method is used to know whether a given graph is a dag.
 * @return <code>true</code> if the graph is a dag,
 * <code>false</code> in other case.
 */

public boolean isADag() {
  
  boolean change = false;
  Node node;
  int i, position, nodeParents, nodeChildren;
  
  Graph aux = (Graph) duplicate();

  //If the graph can't be duplicated, return false
  if (aux == null) return false;
  
  try{
    do {
      change = false;
      
      for (i=0 ; i<aux.nodeList.size() ; i++) {
	node = (Node) aux.nodeList.elementAt(i);
	nodeParents = ((NodeList) aux.parents(node)).size();
	nodeChildren = ((NodeList) aux.children(node)).size();
	
	if ((nodeParents==0) || (nodeChildren==0)) {
	  change = true;
	  position = aux.getNodePosition(node);
	  while (position != -1) {
	    aux.removeLink(position);            // remove their link�s
	    position = aux.getNodePosition(node);
	  }
	  aux.removeNode(i);     // remove the node
	}
      }
    } while (change);
    
    if (aux.nodeList.size() >= 2)
      return false;
    else
      return true;
  }
  catch (InvalidEditException iee){return true;}
}

/**
 * This method is used to know there is a directed cycle for the nodes a,b of the 
 * same link.The nodes will must set to not visited or they
 * will not be studied. 
 * @param a the tail of the link
 * @param b the head of the link
 * @param path a path with a mixed cycle. Initialy it must have the tail a.
 * @return true if there is a directed cycle, false in other case
 */
    public boolean hasDirectedCycle(Node a, Node b, Vector path) {
        if (a.equals(b)) {
            return true;
	} else {
	    //get the childs of b
	    Enumeration c=children(b).elements();
	    Node child;
	    boolean found=false;

	    //look the path that follows the childrens of b
	    while ((!found) && (c.hasMoreElements())){
		child = (Node) c.nextElement();
		
		if ( path.contains(child) ) 
		    return true;
		
		if (!child.getVisited()) { //check if the child has visited
		    child.setVisited(true);
		    path.add(b);
		    found=hasDirectedCycle(a,child,path);
		    if (!found) path.remove(b);
		}
	    }//end while

	    return found;
	}//end else
    }//end hasDirectedCyle method

/**
 * This method is used to know there is a undirected cycle for the nodes a,b of the 
 * same link.The nodes will must set to not visited or they
 * will not be studied. 
 * @param a an extrem, 
 * @param b the other extrem
 * @return true if there is a undirected cycle, false in other case
 */
    public boolean hasUndirectedCycle(Node a, Node b) {
        if (a.equals(b)) {
            return true;
	} else {
	    //get the siblings of b
	    Enumeration e=siblings(b).elements();
	    Node ady;
	    boolean found=false;

	    //look the path that follows the siblings of b
	    while ((!found) && (e.hasMoreElements())){
		ady = (Node) e.nextElement();
		if (!ady.equals(b)) // node where it comes from
		    if (!ady.getVisited()) { //check if the sibling has visited
			ady.setVisited(true);
			found=hasUndirectedCycle(a,ady);
		    }
	    }//end while

	    return found;
	}//end else
    }//end hasUndirectedCyle method

/**
 * This method is used to know there is a mixed cycle (with directed or undirected links)
 * for the nodes a,b of the same link. The nodes will must set to not visited or they
 * will not be studied.
 * @param a a extrem/tail of the link
 * @param b a extrem/head of the link
 * @param path a path with a mixed cycle. Initialy it must have the tail a.
 * @return true if there is a mixed cycle, false in other case
 */
    public boolean hasMixedCycle(Node a, Node b, Vector path) {
        if (a.equals(b)) {
            return true;
	} else {
	    //get the childs of b
	    Enumeration c=children(b).elements();
	    Node child;
	    boolean found=false;

	    //We study where the childrens of b ends
	    while ((!found) && (c.hasMoreElements())){
		child = (Node) c.nextElement();
		//look if the child is in the path
		if ( path.contains(child) ) return true;
		//only check childs that hasn't visited
		if (!child.getVisited()) { 
		    child.setVisited(true);
		    path.add(b);//child);
		    found=hasMixedCycle(a,child,path);
		    if (!found) path.remove(b);
		}
	    }//end while childs

	    //get the siblings of b
	    Enumeration e=siblings(b).elements();
	    Node ady;

	    //We study where the siblings of b ends
	    while ((!found) && (e.hasMoreElements())){
		ady = (Node) e.nextElement();
		if (!ady.equals((Node)path.elementAt(path.size()-1)) ) {// ignore the node from the siblings where it comes from
		    //look if the next sibling is in the path
		    if ( path.contains(ady) ) return true;
		    
		    //only check childs that hasn't visited 
		    if (!ady.getVisited()) { //check if the sibling has visited
			//ady.setVisited(true);
			path.add(b);
			found=hasMixedCycle(a,ady,path);
			if (!found) path.remove(b);
		    }
		}
	    }//end while siblings

	    return found;
	}//end else
    }//end hasMixedCycle Link method

/**
 * This method is used to know if there is a mixed cycle (with directed or undirected links)
 * for the Graph
 * @return true if there is a mixed cycle, false in other case
 */
    public boolean hasMixedCycles() {
	Graph aux = (Graph) duplicate();
	aux.setVisitedAll(false);
	boolean found=false;
	//If the graph cant't be duplicate return false
	if (aux == null) return false;


	//we look for cycles at every not visited link of the Graph
	for (int i=0 ; ( i<aux.linkList.size() ) && (!found) ; i++) {
	    Link link = (Link) aux.linkList.elementAt(i);
	    Node tail=link.getTail();	
	    Node head=link.getHead();
	    if ( (!head.getVisited()) || (!tail.getVisited()) ) {
		Vector path=new Vector();
		path.add(tail);
		found=aux.hasMixedCycle(tail,head,path);
	    }
	}//end for

	return found;
    }//end hasMixedCycles Graph method

/**
 * This method is used to know if there is a directed cycle (only with directed links)
 * for the Graph
 * @return true if there is a directed cycle, false in other case
 */
    public boolean hasDirectedCycles() {
	Graph aux = (Graph) duplicate();
	aux.setVisitedAll(false);
	boolean found=false;
	//If the graph cant't be duplicate return false
	if (aux == null) return false;


	//we look for cycles at every not visited link of the Graph
	for (int i=0 ; ( i<aux.linkList.size() ) && (!found) ; i++) {
	    Link link = (Link) aux.linkList.elementAt(i);

	    if (!link.getDirected()) continue;
	    Node tail=link.getTail();	
	    Node head=link.getHead();
	    if ( (!head.getVisited()) || (!tail.getVisited()) ) {
		Vector path=new Vector();
		path.add(tail);
		found=aux.hasDirectedCycle(tail,head,path);
	    }
	}//end for

	return found;
    }//end hasDirectedCycles Graph method

/**
 * This method is used to know whether a given graph is a rpdag.
 * @return <code>true</code> if the graph is a rpdag,
 * <code>false</code> in other case.
 */

public boolean isARpdag() {
  
  boolean change = false;
  Node node;
  Link link;
  int i, position, nodeParents, nodeParentParents, nodeChildren, nodeSiblings;
  Graph aux = (Graph) duplicate();
  aux.setVisitedAll(false);
  NodeList nodes;
  LinkList links;

  //Test if there is structures like  x-->y--z, for this we check the 
  //siblings and the  parents of a node. If the siblings list and the 
  //parents list has elements it isn't a rpdag
  for (i=0 ; i<aux.nodeList.size() ; i++) {
      //for each node, get the number of parents and the number of siblings
      node = (Node) aux.nodeList.elementAt(i);
      nodeParents = ((NodeList) aux.parents(node)).size();
      nodeSiblings = ((NodeList) aux.siblings(node)).size();
      
      //test if both,number of siblings and parents, are greater tha zero
      if ( (nodeParents>0) && (nodeSiblings>0) ) return false;
  }//end for i

  
  //Test that for each directed link (x->y), the tail (x) have parents or the head(y) have another parent
  for (i=0 ; i<aux.linkList.size() ; i++) {
	link = (Link) aux.linkList.elementAt(i);
	if ( link.getDirected() ) {
	    //Check that y has more parents
	    node=link.getHead();
	    nodeParents = ((NodeList) aux.parents(node)).size();
	    if (nodeParents >1) continue;
	    
	    //If only have one parent, check y x has a parent
	    node=link.getTail();
	    nodeParentParents = ((NodeList) aux.parents(node)).size();
	    if (nodeParentParents==0)  return false;
	}
  }//end for i


  //Test if there is directed cycles or undirected cyles
  for (i=0 ; i<aux.linkList.size() ; i++) {
	link = (Link) aux.linkList.elementAt(i);
	if ( !link.getDirected() ) {
	    if ( hasUndirectedCycle(link.getTail(),link.getHead()) )
		return false;
	} else {
	    Vector path=new Vector();
	    path.add(link.getTail());
	    if ( hasDirectedCycle(link.getTail(),link.getHead(),path) )
		return false;
	}
  }//end for i


  //If it hasn't cycles (directed or undirected), or structures like x->y--z, and 
  //for each x->y , x has parents or y have another parent; then it's a rpdag
  return true;
}


/**
 * This method is used to know whether a given graph is a tree.
 * @return <code>true</code> if the graph is a tree,
 * <code>false</code> in other case.
 */

public boolean isATree() {
  
  Node node;
  Enumeration e = nodeList.elements();
  
  while (e.hasMoreElements()) {
    node = (Node) e.nextElement();
    if (((NodeList)parents(node)).size() > 1)
      return false;
  }
  
  return true;
}


/**
 * Used to know whether a given graph is a polytree.
 * @return <code>true</code> if the graph is a polytree,
 * <code>false</code> in other case.
 */

public boolean isAPolytree() {
  
  boolean change = false;
  Node node;
  int i, position;
  
  try{
    Graph aux = (Graph) duplicate();
    
    do {
      for (i=0 ; i<aux.nodeList.size() ; i++) {
	node = (Node) aux.nodeList.elementAt(i);
	
	if ((aux.neighbours(node)).size() == 1) {
	  change = true;
	  
	  while ((position=aux.getNodePosition(node)) != -1)
	    aux.removeLink(position);            // remove their links
	  aux.removeNode(i);     // remove the node
	}
      }
    } while (change);
    
    if (aux.nodeList.size() >= 2)
      return false;
    else
      return true;
  }
  catch (InvalidEditException e){
    System.out.println("The graph can't be duplicated");
    return false;
  }
}


/**
 * Used to know whether a graph is a simple graph.
 * @return <code>true</code> if the graph is simple,
 * <code>false</code> in other case.
 */

public boolean isASimpleGraph() {
  
  boolean change = false;
  Node node;
  int i, position;
  
  try{
    Graph aux = (Graph) duplicate();
    
    // first we remove the nodes out of the loops
    do {
      for (i=0 ; i<aux.nodeList.size() ; i++) {
	node = (Node) aux.nodeList.elementAt(i);
	
	if ((aux.neighbours(node)).size() == 1){
	  change = true;
	  while ((position=aux.getNodePosition(node)) != -1)
	    aux.removeLink(position);            // remove their links
	  aux.removeNode(i);     // remove the node
	}
      }
    } while (change);
    
    // now we test whether the resultant lattice is a DAG, and if the answer
    // is true, we test whether the simple graph condition holds for the
    // remaining nodes.
    
    if (aux.isADag()) {
      int nChildren, j;
      Node child;
      Vector global=new Vector();
      Vector accessible;
      SetVectorOperations s = new SetVectorOperations();
      
      for (i=0 ; i<aux.nodeList.size() ; i++) {
	node = (Node) aux.nodeList.elementAt(i);
	nChildren = ((NodeList) aux.children(node)).size();
	
	if (nChildren >= 2) {
	  global.removeAllElements();  // set global to an empty list
	  
	  for (j=0 ; j<nChildren ; j++) {
	    child = (Node) ((NodeList) aux.children(node)).elementAt(j);
	    accessible = descendants(child);
	    if (((Vector)s.intersection(global,accessible)).size() != 0)
	      return false;
	    else
	      global = (Vector)s.union(global, accessible);
	  }
	}
      }
      return true;
    }
    else
      return false;
  }
  catch (InvalidEditException e){
    System.out.println("The graph can't be duplicated");
    return false;
  }
}


/**
 * This method is used to know whether exists a undirected path among
 * two nodes.
 * @param source one of the nodes.
 * @param destination the other node.
 * @param tested a <code>Vector</code> that must be created empty before
 * calling this method.
 * This vector is used for testing the nodes visited.
 * @return <code>true</code> if exists a undirected path,
 * <code>false</code> in other case.
 */

public boolean undirectedPath (Node source, Node destination, Vector tested) {
  
  boolean aux = false;
  NodeList neighb;
  Enumeration e;
  Node node;
  
  neighb = neighbours(source);
  tested.addElement(source);
  
  if ( neighb.getId(destination) != -1 ) {
    aux = true;
  }
  else {
    for (e=neighb.elements() ; e.hasMoreElements() ; ) {
      node = (Node) e.nextElement();
      if (tested.indexOf(node) == -1) {
	aux = undirectedPath(node,destination,tested);
      }
      if(aux)
	break;               
    }
  }
  return aux;
}

/**
 * This method computes the ancestral graph for a node.
 * @param x the node for which the ancestral graph will be obtained..
 * @return the ancestral graph of <code>x</code>.
 */

public Graph reachable(Node x) {
  
  Graph aux;
  NodeList parents, nodes, siblings;
  LinkList links, links2;
  Link link;
  int i, pos, j;
  Node nodep, n;
  
  
  
  nodes = new NodeList();
  links = new LinkList();
  nodes.insertNode(x);
  x.setVisited(true);
  
  parents = parents(x);
  
  for (i=0 ; i< parents.size() ; i++) {
    nodep = (Node)parents.elementAt(i);

    if (!nodep.getVisited()){
    nodep.setVisited(true);
    pos = getLinkList().getID(nodep.getName(),x.getName());
    if (pos != -1) {
      links.insertLink(getLinkList().elementAt(pos));
    }
    else
      System.out.println("Something wrong in method ancestral");
   
    aux = reachable(nodep);
      for (j=0 ; j<aux.getNodeList().size() ; j++) {
       n = (Node)aux.getNodeList().elementAt(j);
      if (nodes.getId(n.getName()) == -1)
	nodes.insertNode(n);
    }
     links.join(aux.getLinkList());
    } 
  }
  
  
  siblings = siblings(x); 
 
  
   for (i=0 ; i< siblings.size() ; i++) {
        
    nodep = (Node) siblings.elementAt(i);
      pos = getLinkList().getID(nodep.getName(),x.getName());
    if (pos != -1) {
      links.insertLink(getLinkList().elementAt(pos));
    }
    else
    {pos = getLinkList().getID(x.getName(),nodep.getName());
      if (pos != -1) {
      links.insertLink(getLinkList().elementAt(pos));}
      else {
      System.out.println("Something wrong in method ancestral");
      }
    }
    
    if (!nodep.getVisited()){
    nodep.setVisited(true);
  
   
    aux = reachable(nodep);
      for (j=0 ; j<aux.getNodeList().size() ; j++) {
       n = (Node)aux.getNodeList().elementAt(j);
      if (nodes.getId(n.getName()) == -1)
	nodes.insertNode(n);
    }
     links.join(aux.getLinkList());
    } 
  }
  
  
  nodes = nodes.duplicate();
  links2 = new LinkList();
  for (j=0 ; j<links.size() ; j++) {
    Link l = (Link)links.elementAt(j);
    int posT = nodes.getId(l.getTail().getName());
    int posH = nodes.getId(l.getHead().getName());
    if ((posT!=-1) && (posH!=-1)) {
      Link lap = new Link(nodes.elementAt(posT),
			  nodes.elementAt(posH),l.getDirected());
      links2.insertLink(lap);
    }
  }
  
  
  aux = new Graph(nodes,links2, getKindOfGraph());
  
  
  return aux;
}



/**
 * This method computes the ancestral graph for a node.
 * @param x the node for which the ancestral graph will be obtained..
 * @return the ancestral graph of <code>x</code>.
 */

public Graph ancestral(Node x) {
  
  Graph aux;
  NodeList parents, nodes, sibbilings;
  LinkList links, links2;
  Link link;
  int i, pos, j;
  Node nodep;
  
  nodes = new NodeList();
  links = new LinkList();
  
  parents = parents(x);
  
  for (i=0 ; i< parents.size() ; i++) {
    nodep = (Node)parents.elementAt(i); 
    pos = getLinkList().getID(nodep.getName(),x.getName());
    if (pos != -1) {
      links.insertLink(getLinkList().elementAt(pos));
    }
    else
      System.out.println("Something wrong in method ancestral");
    
    aux = ancestral(nodep);
    
    for (j=0 ; j<aux.getNodeList().size() ; j++) {
      Node n = (Node)aux.getNodeList().elementAt(j);
      if (nodes.getId(n.getName()) == -1)
	nodes.insertNode(n);
    }
    
    links.join(aux.getLinkList());
  }
  nodes.insertNode(x);
  nodes = nodes.duplicate();
  links2 = new LinkList();
  for (j=0 ; j<links.size() ; j++) {
    Link l = (Link)links.elementAt(j);
    int posT = nodes.getId(l.getTail().getName());
    int posH = nodes.getId(l.getHead().getName());
    if ((posT!=-1) && (posH!=-1)) {
      Link lap = new Link(nodes.elementAt(posT),
			  nodes.elementAt(posH),l.getDirected());
      links2.insertLink(lap);
    }
  }
  aux = new Graph(nodes,links2, DIRECTED);
  
  return aux;
}


/**
 * This method computes the ancestral graph for a list of nodes.
 * @param vars the list of nodes for which the ancestral graph will be
 * obtained.
 * @return the ancestral graph  of <code>vars</code>.
 */

public Graph ancestral(NodeList vars) {
  
  int i, j,k;
  Graph aux;
  NodeList nodes;
  LinkList links, links2;
  Node node;
  
  nodes = new NodeList();
  links = new LinkList();
  
  for (i=0 ; i< vars.size() ; i++) {
    node = (Node)vars.elementAt(i);
  if (getKindOfGraph() == DIRECTED) { aux = ancestral(node);}
  else { 
  for (k=0 ; k<nodeList.size() ; k++) {
      Node n = (Node)  nodeList.elementAt(k);
    n.setVisited(false);
  }
  aux = reachable(node);}
    
    for (j=0 ; j<aux.getNodeList().size() ; j++) {
      Node n = (Node)aux.getNodeList().elementAt(j);
      if(nodes.getId(n.getName()) == -1)
	nodes.insertNode(n);
    }
    
    links.join(aux.getLinkList());
  }
  nodes = nodes.duplicate();
  
  links2 = new LinkList();
  
  for (j=0 ; j<links.size() ; j++) {
    Link l = (Link)links.elementAt(j);
    int posT = nodes.getId(l.getTail().getName());
    int posH = nodes.getId(l.getHead().getName());
    if ((posT!=-1) && (posH!=-1)) {
      Link lap = new Link(nodes.elementAt(posT),
			  nodes.elementAt(posH),l.getDirected());
      links2.insertLink(lap);
    }
  }
  aux = new Graph(nodes,links2, getKindOfGraph());
  return aux;
}


/**
 * This method computes the ancestral list of nodes for a given node.
 * @param x the node for which the ancestral list will be obtained.
 * @param list the list where the ancestral list will be stored.
 */

public void ancestralList(Node x, NodeList list) {
  
  int i;
  NodeList listOfParents;
  
  listOfParents = parents(x);
  
  if (listOfParents != (NodeList)null)
    list.merge(listOfParents);
  
  for (i=0 ; i < listOfParents.size() ; i++) {
    ancestralList(listOfParents.elementAt(i),list);                 
  }
}


/**
 * This method determines whether two nodes are d-separated given a list of
 * nodes.
 * @param x one of the nodes.
 * @param y the other node.
 * @param z the list of nodes.
 * @return <code>true</code> if <code>x</code> is d-separated from
 * <code>y</code> by <code>z</code>.
 */

public boolean independents(Node x, Node y, NodeList z) {
  
  Graph moral;
  Enumeration e;
  Node node, xAux, yAux;
  NodeList nodes, zAux;
  Vector tested;
  int pos;
  
  nodes = new NodeList();
  nodes.insertNode(getNodeList().getNode(x.getName()));
  nodes.insertNode(getNodeList().getNode(y.getName()));
  zAux = getNodeList().intersectionNames(z);
  nodes.join(zAux);
  moral = ancestral(nodes);
  moral = moral.moral();
  xAux = moral.getNodeList().getNode(x.getName());
  yAux = moral.getNodeList().getNode(y.getName());
  zAux = moral.getNodeList().intersectionNames(z);
  
  for (e=zAux.elements() ; e.hasMoreElements() ; ) {
    node = (Node)e.nextElement();
    try{
      moral.removeNode(node);
    }catch(InvalidEditException iee){};
  }
  tested = new Vector();
  if (moral.undirectedPath(xAux, yAux, tested))
    return false;
  else
    return true;
}


/**
 * This method determines whether two nodes are d-separated given a list of
 * nodes.
 * @param x one of the nodes.
 * @param y the other node.
 * @param z the list of nodes.
 * @param degree here, it's not used.
 * @return <code>true</code> if <code>x</code> is d-separated from
 * <code>y</code> by <code>z</code>.
 */

public boolean independents(Node x, Node y, NodeList z, int degree) {
  
  return (independents(x,y,z));
}
public boolean independents(Node x, Node y, NodeList z, double degree){
      return(independents(x,y,z));
   }

public double getDep(Node x, Node y, NodeList z){
    if (independents(x,y,z)) { return (-1.0);}
    else { return (1.0);}
    
    
    
}



/**
 * This method computes a topological ancestral order of the nodes in the
 * graph.
 * @return a list with the nodes, topologically sorted.
 */

public NodeList topologicalOrder() {
  
  NodeList index;
  Node node;
  int i;
  
  index = new NodeList();

  for (i=0 ; i< getNodeList().size() ; i++) {
    node = (Node)getNodeList().elementAt(i);
    if (index.getId(node) == -1)
      topological(node,index);
  }
  
  return index;
}


/**
 * This method is private. It is a auxiliar method for
 * <code>topologicalOrder</code>. Inserts the given node in the
 * given list, topologically ordered.
 * @param node the node to insert.
 * @param index the list of nodes.
 */

private void topological(Node node, NodeList index) {
  
  int i;
  NodeList pa;
  Node aux;
  
  pa = parents(node);
  for (i=0 ; i< pa.size() ; i++) {
    aux = (Node)pa.elementAt(i);
    if (index.getId(aux) == -1)
      topological(aux,index);
  }

  index.insertNode(node);
}


/**
 * This method computes the maximal number of adjacencies in the graph.
 * @return the maximal number of adjacencies in the graph.
 */

public int maxOfAdyacencies() {
  
  int max, i;
  NodeList nb;
  Node node;
  
  max = 0;
  for (i=0 ; i<getNodeList().size() ; i++) {
    node = (Node)getNodeList().elementAt(i);
    nb = neighbours(node);
    
    if (nb.size() > max)
      max = nb.size();
  }

  return max;
}


/**
 * This method computes the Markov Blanket of a node.
 * @param node the node for which the Markov Blanket will be computed.
 * @return the list of nodes in the Markov Blanket of <code>node</code>.
*/

public NodeList markovBlanket(Node node) {
  
  int i;
  NodeList mb, pa, ch;
  Node aux;

  mb = new NodeList();
  pa = parents(node);
  mb.join(pa);
  ch = children(node);
  mb.join(ch);
  
  for (i=0 ; i< ch.size() ; i++) {
    aux = (Node)ch.elementAt(i);
    pa = parents(aux);
    pa.removeNode(node);
    mb.join(pa);
  }
  
  return mb;
}


/**
 * This method computes the min d-separating set among two nodes
 * in this graph.
 * When called with a undirected graph will compute the minimum
 * size cut-set. 
 *
 * @param x one node.
 * @param y the other node.
 * @return a list of nodes with the minumun d-separating set.
 */

public NodeList minimunDSeparatingSet(Node x, Node y) {
  
  int i, j;
  NodeList minimun, nodes;
  Graph auxGraph;
  Hashtable labelNodePlus, labelNodeMinus;
  NodeList labeledNodes, scannedNodes, ch, pa, xMinusCut, queue;
  Node source, sink, Xi, Xj, z, node, nodeLabel;
  boolean found = false;
  LinkList flow;
  Link link;
  
  flow = new LinkList();
  auxGraph = initializeMinDSep(x,y);
  
  source = (Node)auxGraph.getNodeList().getNode(x.getName()+"-");
  sink   = (Node)auxGraph.getNodeList().getNode(y.getName()+"+");
  
  do {
    labeledNodes = new NodeList();
    queue = new NodeList();
    scannedNodes = new NodeList();
    labelNodePlus = new Hashtable();
    labelNodeMinus = new Hashtable();
    labeledNodes.insertNode(source);
    queue.insertNode(source);
    labelNodePlus.put(source,source);
    
    // Now it tries to find a non saturated path.

    while ((labeledNodes.getId(sink)==-1) && (queue.size()>0)) {
      Xi = (Node)queue.elementAt(0);
      if (scannedNodes.getId(Xi) == -1) { // If it is not yet tested
	// now test children
	ch = auxGraph.children(Xi);
	for (i=0 ; i< ch.size() ; i++) {
	  Xj = (Node)ch.elementAt(i);
	  link = (Link)auxGraph.getLinkList().
	                        getLinks(Xi.getName(),Xj.getName());
	  if ((labeledNodes.getId(Xj) == -1) &&
	      (flow.indexOf(link) == -1)) {
	    labelNodePlus.put(Xj,Xi);
	    labeledNodes.insertNode(Xj);
	    queue.insertNode(Xj);
	  }
	}
	
	// Now test parents.

	pa = auxGraph.parents(Xi);
	for (i=0 ; i< pa.size() ; i++) {
	  Xj = (Node)pa.elementAt(i);
	  link = (Link)auxGraph.getLinkList().
	                        getLinks(Xj.getName(),Xi.getName());
	  if ((labeledNodes.getId(Xj) == -1) &&
	      (flow.indexOf(link)!= -1)) {
	    labelNodeMinus.put(Xj,Xi);
	    labeledNodes.insertNode(Xj);
	    queue.insertNode(Xj);
	  }
	}
      }
      scannedNodes.insertNode(Xi);
      queue.removeNode(Xi);
    }
    
    // If such path has been found, retrieve and mark it.
    
    if (labeledNodes.getId(sink) != -1) {
      Xj = sink;
      do {
	if ((z=(Node)labelNodePlus.get(Xj)) != null) {
	  link = (Link)auxGraph.getLinkList().
	                        getLinks(z.getName(),Xj.getName());
	
          flow.insertLink(link);
	}
	else {
	  z = (Node)labelNodeMinus.get(Xj);
	  link = (Link)auxGraph.getLinkList().
	  		        getLinks(Xj.getName(),z.getName());
	  flow.removeLink(link);
	}
	
	Xj = z;
      } while (z != source);      
    }
    if (queue.size() == 0)
      found = true;
    
  } while (!found); // Until no other path can be found.
  
  xMinusCut = new NodeList();
  xMinusCut = auxGraph.getNodeList();
  
  for (i=0 ; i<labeledNodes.size() ; i++) {
    node = (Node)labeledNodes.elementAt(i);
    if ((nodeLabel=(Node)xMinusCut.getNode(node.getName())) != null)
      xMinusCut.removeNode(node);
  }
  
  minimun = new NodeList();

  for(i=0; i<auxGraph.getLinkList().size(); i++) {
  link = (Link) auxGraph.getLinkList().elementAt(i);
  
  }
  // The minimum separator will be those not within the labeled.
  // Now look for nodes corresponding to arcs in the minimum separator.
  
  for (i=0 ; i< labeledNodes.size() ; i++)
    for (j=0 ; j<xMinusCut.size() ; j++) {
    Xi = (Node)labeledNodes.elementAt(i);
   
    Xj = (Node)xMinusCut.elementAt(j);
   
    link = (Link)auxGraph.getLinkList().
		          getLinks(Xi.getName(),Xj.getName());
    if (link != null) {
      Xj.setName(Xj.getName().replace('+',' ').trim());
      Xj.setName(Xj.getName().replace('-',' ').trim());
      Xj = (Node)getNodeList().getNode(Xj.getName());
      minimun.insertNode(Xj);
  //    System.out.println("Inserto nodo " + Xj.getName());
      
    }
  }

  return minimun;
}


/**
 * Private method to initialize the look up of the minimun d-separator.
 * @param x one node.
 * @param y the other node.
 * @return a graph.
 */

private Graph initializeMinDSep(Node x , Node y) {
  
  NodeList nodes, nodesPlus, nodesMinus, auxNodes;
  Graph auxGraph;
  Link link, linkUminusVplus, linkVminusUplus;
  LinkList auxLinks;
  Node nodePlus, nodeMinus, nodeUplus, nodeUminus, nodeVplus, nodeVminus;
  int i;
  
  nodes = new NodeList();
  nodes.insertNode(x);
  nodes.insertNode(y);
  
  
  auxGraph = ancestral(nodes);
  
  auxGraph.setKindOfGraph(UNDIRECTED); 
  auxGraph = auxGraph.moral();
   
  
  nodesPlus = (NodeList)auxGraph.getNodeList().duplicate();
  nodesMinus = (NodeList)auxGraph.getNodeList().duplicate();
  auxLinks = new LinkList();
  
  
  for (i=0 ; i< nodesPlus.size() ; i++) {
    nodePlus  = (Node)nodesPlus.elementAt(i);
    nodeMinus = (Node)nodesMinus.elementAt(i);
    nodePlus.setName(nodePlus.getName()+"+");
    nodeMinus.setName(nodeMinus.getName()+"-");
    link = new Link(nodePlus,nodeMinus);
    auxLinks.insertLink(link);
  }
  
  auxNodes = new NodeList();
  auxNodes.join(nodesPlus);
  auxNodes.join(nodesMinus);
  
  for (i=0 ; i<auxGraph.getLinkList().size() ; i++) {
    link = (Link)auxGraph.getLinkList().elementAt(i);
    nodeUplus =	nodesPlus.getNode(((Node)link.getTail()).getName()+"+");
    nodeUminus = nodesMinus.getNode(((Node)link.getTail()).getName()+"-");
    nodeVplus =	nodesPlus.getNode(((Node)link.getHead()).getName()+"+");
    nodeVminus = nodesMinus.getNode(((Node)link.getHead()).getName()+"-");
    linkUminusVplus = new Link(nodeUminus,nodeVplus);
    linkVminusUplus = new Link(nodeVminus,nodeUplus);
    auxLinks.insertLink(linkUminusVplus);
    auxLinks.insertLink(linkVminusUplus);
  }

  auxGraph = new Graph(auxNodes,auxLinks,DIRECTED,false);
  
  return auxGraph;
}


   /**
    * Converts all the siblings of a node to children
    *
    * @param node Node whose siblings become children 
    * 
    */

   public void siblingsToChildren(Node node) {
      Link link;
      Node nodeh;

      for (int i=0; siblings(node) != null; ){

	  nodeh = (Node) siblings(node).elementAt(i);
	
	  try{
	      removeLink(node,nodeh);
	      createLink(node,nodeh,true);
	  }
	  catch (InvalidEditException iee){
	      System.out.println("siblings can't be turn to children");
	  }
      }
   }

  /**
    * Orient the siblings in cascade starting from node 
    *
    * @param node Node whose siblings become children 
    * 
    */


   public void  orientInCascade(Node node){
     Link link;
     Node nodeh;

     while (siblings(node).size()>0) { 
	  nodeh = (Node) siblings(node).elementAt(0);
	  try{
	      link=getLink(node,nodeh);
	      removeLink(link);
	      createLink(node,nodeh,true);
	  }
	  catch (InvalidEditException iee){
	  System.out.println("siblings can't be turn to children");
	  }
	  orientInCascade(nodeh);
     }
   }

  /**
    * Orient a link from the first to the second node,
    * but if this creates a cycle, then it does it
    * with the reverse orientation.
    *
    * @param link the link that it is going to be oriented
    * @param tail The tail node
    * @param heaad The head node 
    * 
    */


   public void  orientLinkDag(Link link, Node tail, Node head){
   
     
	   try{ 
              removeLink(link);
	      if (!isThereDirectedPath(head,tail))
	      createLink(tail,head,true);
              else { if (!isThereDirectedPath(tail,head))
                  
              { createLink(head,tail,true);}
              
              else {System.out.println("Impossible orientation");} 
              }}
	   catch (InvalidEditException iee){
	 
	  if (!isADag()) {System.out.println("Warning Directed cycles");}
          }
     }
   
    /**
     * Obtains a dag from a rpdag, an extension. Changing every unoriented link
     * into arc. It is suposed that every uncompeled arc can be oriented in any
     * direction without creating new h2h nor cycle. So it does not test this
     * condition
     */

    public void  extendRPDAG(){

	Node node;
	for(int i=0; i< getNodeList().size(); i++){
	    node=getNodeList().elementAt(i);
	    while  (siblings(node).size() >0)
		orientInCascade(siblings(node).elementAt(0));
   
	}
    }

/**
 * This method generates a random list of parents from a Poisson distribution
 * for a given node.
 * @param generator a random numbers generator.
 * @param node the node for which the list of parents will be generated.
 * @param average the mean of the Poisson distribution.
 * @return the list of parents.
 */

private NodeList randomParents(Random generator, Node node, double average) {
  
  int i, r2, nParents, pos;
  NodeList nodes = new NodeList();
  
  nParents = poisson(generator,average);
  pos = getNodeList().size() - 1;
  
  if (pos <= nParents) {
    for (i=0 ; i < pos ; i++)
      nodes.insertNode((Node)getNodeList().elementAt(i));
  }
  else {
    for (i=0 ; i < nParents ; i++) {
      r2 = (int)(generator.nextDouble() * pos);
      if (nodes.getId((Node)getNodeList().elementAt(r2)) == -1) {
	nodes.insertNode((Node)getNodeList().elementAt(r2));
      }
      else
	i--;
    }
  }

  return nodes;
}


/**
 * This method generates an integer number by simulating from the
 * Poisson distribution.
 * @param generator a random numbers generator.
 * @param average the mean of the Poisson distribution.
 * @return the simulated value.
 */

static public int poisson(Random generator, double average) {
  
  int x, i;
  double Xi, sum, tem, TR, den, div;
  
  sum = 0.0;
  tem = (double) (-1.0/average);
  den = generator.nextDouble();
  Xi = tem * Math.log(den);
  sum = sum + Xi;
  
  for (x=1 ; sum<1 ; x++) {
    den = generator.nextDouble();
    Xi = tem * Math.log(den);
    sum = sum + Xi;
  }
  
  return (x-1);
}


/**
 * This method computes the connected component of a graph from a node.
 * @param node the node from which the connected component will be
 * searched.
 * @param nodes list of nodes where the method stores the nodes in the
 * connected component.
 */

public void undirectedAccessibles(Node node, NodeList nodes) {
  
  Enumeration e;
  Node nb;

  if (nodes.getId(node) == -1)
        nodes.insertNode(node);

  for (e=neighbours(node).elements() ; e.hasMoreElements() ; ) {
    nb = (Node) e.nextElement();
    if (nodes.getId(nb) == -1) {
      nodes.insertNode(nb);
      undirectedAccessibles(nb,nodes);
    }
  }
}

/**
 * Method for removing utility nodes from the graph
 */
public void removeUtilityNodes(){
  NodeList nodesInGraph,nodesToRemove;
  Node node;
  
  	// Look for utility nodes 
    nodesInGraph=getNodeList();
    nodesToRemove=new NodeList();
		for(int i=0; i < nodesInGraph.size(); i++){
      node=nodesInGraph.elementAt(i);

      // If it is a utility node, remove it
      if (node.getKindOfNode() == Node.UTILITY){
        nodesToRemove.insertNode(node);
      }
    }

    // Finally, remove the utility nodes
    for(int i=0; i < nodesToRemove.size(); i++){
      node=nodesToRemove.elementAt(i);
      try{
          removeNode(node);
      }
      catch(InvalidEditException e){
          System.out.println("Error removing node from graph");
          System.out.println("Method removeUtilityNodes");
          System.out.println("Class Graph");
          System.exit(0);
      }
    }
}

/* ******************* Graph Fusion Methods ******************* */


/**
 * Computes the union of two Graphs, it takes into account the children, parents and
 * siblings list of the nodes.
 * @param Graph. Graph which is going to be joined with this.
 * @return the resulting union.
 */

public Graph union (Graph graph) {
  
  if (getKindOfGraph() != graph.getKindOfGraph())
    return null;
  
  NodeList nodes = new NodeList();
  nodes.merge(graph.getNodeList());
  nodes.merge(nodeList);

  Graph g = new Graph(nodes, new LinkList(), getKindOfGraph());
  
  LinkList links = new LinkList();
  links.join(graph.getLinkList());
  links.join(linkList);
  
  try {
      for (int i=0; i < links.size(); i++) {
	  Link l=links.elementAt(i);
	  Node tail=g.getNodeList().getNode(l.getTail().getName());
	  Node head=g.getNodeList().getNode(l.getHead().getName());
	  g.createLink(tail,head,l.getDirected());
      }
  } catch (InvalidEditException iee) {}
  
  return g;
}


/**
 * Computes the intersection of two Graphs.
 * @param  Graph which is going to be intersected with this.
 * @return the resulting intersection.
 */

public Graph intersection (Graph graph) {
  
  if (getKindOfGraph() != graph.getKindOfGraph())
    return null;
  
  NodeList nodes = new NodeList();
  nodes.merge(nodeList);
  nodes.intersectionNames(graph.getNodeList());
  
  LinkList links = new LinkList();
  links.join(linkList);
  links = links.intersection(graph.getLinkList());
  
  return (new Graph (nodes, links, getKindOfGraph()));
}


/**
 * Extends the sets of links obtained after the intersection of
 * two directed Graphs.
 * @param graph. Graph which is going to be intersected with this.
 * @return the resulting intersection.
 */

public Graph intersectionExtended (Graph graph) {
  
  int i;
  LinkList ll, llaux;
  NodeList nl;
  Link link;
  
  if ((getKindOfGraph()!=DIRECTED) || (getKindOfGraph()!=graph.getKindOfGraph()))
    return null;
  
  nl = getNodeList();
  ll = getLinkList();
  llaux = graph.getLinkList();
  ll = ll.intersection(llaux);
  
  for (i=0 ; i<llaux.size() ; i++) {
    link = llaux.elementAt(i);
    if ((nl.getId(link.getTail())==-1) || (nl.getId(link.getHead())==-1))
      ll.insertLink(link);
  }
  nl = graph.getNodeList();
  llaux = getLinkList();
  for (i=0 ; i<llaux.size() ; i++) {
    link = llaux.elementAt(i);
    if ((nl.getId(link.getTail())==-1) || (nl.getId(link.getHead())==-1))
      ll.insertLink(link);
  }
  nl = new NodeList();
  nl.merge(graph.getNodeList());
  nl.merge(getNodeList());
  
  return (new Graph(nl,ll,DIRECTED));
}


/**
 * Marginalizes this graph to a given <code>NodeList</code>.
 * @param nodes a <code>NodeList</code> to which the graph will be
 * marginalized.
 * @return a new Graph restricted to the variables in <code>nodes</code>.
 */

public Graph marginalization (NodeList nodes) throws InvalidEditException {
  
  int i, j, k;
  Node aux;
  Link l;
  NodeList e, to, pa, hi;
  Graph marginalGraph = new Graph(this);
  
  to = marginalGraph.topologicalOrder();
  e = to.differenceNames(nodes);
  
  for (i=0 ; i<e.size() ; i++) {
    aux = e.elementAt(i);
    pa = marginalGraph.parents(aux);
    hi = marginalGraph.children(aux);
    marginalGraph.removeNode(aux);
    
    // Add links from parents to children
    for (j=0 ; j<pa.size() ; j++) {
      aux = pa.elementAt(j);
      for (k=0 ; k<hi.size() ; k++) 
	if (marginalGraph.linkList.getID(aux.getName(), hi.elementAt(k).getName()) == -1) {
	marginalGraph.createLink(aux, hi.elementAt(k));
      }
    }
    
    // Add links from child to child following the topological order
    for (j=0 ; j<hi.size() ; j++) {
      aux = hi.elementAt(j);
      for (k=0 ; k<hi.size() ; k++)
	if ((k!=j) && (to.getId(aux)<to.getId(hi.elementAt(k))) && (marginalGraph.linkList.getID(aux.getName(), hi.elementAt(k).getName())==-1)) {
	marginalGraph.createLink(aux, hi.elementAt(k),true);
      }
    }
  }
  
  return marginalGraph;  
} 


/**
 * Obtains a maximal directed acyclic graph of two given graphs.
 * If it can not be obtained, returns null.
 * @param graph a graph which is going to be maximally combined with this
 * graph.
 * @return a maximal directed acyclic graph, or <code>null</code> if
 * it can not be obtained.
 */

public Graph maximal (Graph graph) throws InvalidEditException {
  
  NodeList common = graph.getNodeList().copy();
  common = common.intersection(getNodeList());
  
  Graph aux1 = graph.marginalization(common);
  
  Graph aux2 = duplicate();
  aux2.linkList = aux2.linkList.difference(aux1.linkList);
  aux2 = aux2.union(graph);
  
  aux1 = marginalization(common);
  Graph aux3 = graph.duplicate();
  aux3.linkList = aux3.linkList.difference(aux1.linkList);
  aux3 = aux3.union(this);
  
  aux2 = aux2.intersection(aux3);
  if (aux2.isADag())
    return aux2;
  else
    return null;
}


/**
 * Sorts the links of a directed acyclic graph using an ancestral
 * order of its nodes (Chickering's sort algorithm).
 * @param nodesOrdered an ancestrally ordered <code>NodeList</code>.
 * @return an ordered <code>Vector</code> with the graph links.
 */

public Vector sortLinks (NodeList nodesOrdered) {
  
  int i, j, k, topVal, maxTopVal;
  Node y;
  LinkList withoutSort = getLinkList().copy();
  Vector sorted = new Vector();
  LinkList selected;
  
  i = 0;
  while (withoutSort.size() != 0) {
    y = nodesOrdered.elementAt(i);
    selected = new LinkList();
    for (j=0 ; j<withoutSort.size() ; j++)
	if ((withoutSort.elementAt(j).getHead().compareTo(y) == 0))
	    selected.insertLink(withoutSort.elementAt(j));
    withoutSort = withoutSort.difference(selected);
	    
    while (selected.size() != 0) {
      maxTopVal = 0;
      k = 0;
      for (j=0 ; j<selected.size() ; j++) {
	topVal = nodesOrdered.getId(selected.elementAt(j).getTail().getName());
	if (topVal > maxTopVal) {
	  maxTopVal = topVal;
	  k = j;
	}
      }
      sorted.addElement(selected.elementAt(k));
      selected.removeLink(k);
    }
    i++;
  }
  
  return sorted;
}



/**
 * Classifies the links of a directed aciclyc graph using an ancestral
 * order of its nodes (Chickering's classification algorithm).
 * @param nodesOrdered an ancestral ordered <code>NodeList</code>.
 * @return a list of reversible links.
 */

public LinkList reversibleLinks (NodeList nodesOrdered) {
  int i, lim, pos;
  Node x, y, w, z;
  boolean parentY, existsWX, existsZY;
  Link axy, awx, a_y, awy;
  NodeList yParents;

  /*System.out.println("Paso 0. NodesOrdered=");
    print(nodesOrdered);*/
  Vector/*LinkList*/ unsettled = sortLinks(nodesOrdered);
  /*System.out.println("Paso 1. unsettled=");
    printLL(unsettled);*/

  LinkList allthelinks = getLinkList();
  Vector/*LinkList*/ irreversible = new Vector();//LinkList();
  LinkList reversible = new LinkList();
  
  int j=0;
  while (unsettled.size() != 0) {
    axy = (Link)unsettled.elementAt(0);
    unsettled.remove(0);//removeLink(0);
    x = axy.getTail();
    y = axy.getHead();
    /*System.out.println("Paso 2. iteracion="+(j++)+" unsettled=");
      printLL(unsettled);*/


    existsWX = false;
    parentY = true;
    yParents = parents(y);

    /*System.out.println("Paso 2.0. iteracion="+j+" parents=");
    print(yParents);
    System.out.println("");    */

    for (i=0 ; i<irreversible.size() ; i++) {
      awx = (Link)irreversible.elementAt(i);
      /*System.out.println("Paso 2.1. in="+i+" awx="+awx);*/
      if (x.compareTo(awx.getHead()) == 0) {
	existsWX = true;
	if (yParents.getId(awx.getTail().getName()) == -1) {
	   parentY = false;
	   /*System.out.println("Paso 2.2. in="+i+" awx="+awx);*/
	} else {
	    //awy = unsettled.getLinks(awx.getTail().getName(),y.getName());
	    awy=awx;//for "variable awy might not have been initialized" warning
	    String t=awx.getTail().getName();
	    String h=y.getName();
	    boolean cont=true;
	    pos=0;
	    while (cont) {
		Link l = (Link) unsettled.elementAt(pos);
		Node he = (Node) l.getHead();
		Node ta = (Node) l.getTail();
		if (l.getDirected()) {
		    //directed
		    if ((t.equals(ta.getName())) && (h.equals(he.getName()))) {
			awy=l;
			cont=false;
		    }
		} else
		    //no directed
		    if (((t.equals(ta.getName())) && (h.equals(he.getName()))) ||
			((h.equals(ta.getName())) && (t.equals(he.getName())))) {
			awy=l;
			cont=false;
		    }
		
		if (cont) {
		    pos++;

		    if (pos>=unsettled.size()) {
			System.out.println("Warning: Link not found in reversibleLinks");
			cont=false;
			String nombre1="BUG1.elv";
			try {
			    save(nombre1);		    
			}catch (IOException excp) {
			    System.out.println("Fallo de IO. Vaya ruina.");
			}
		    }
		}
	    }//end while

	    /*pos = unsettled.indexOf(awy);*/
	    /*System.out.println("Paso 2.3. in="+i+" awx="+awy+" pos="+pos);*/
	    irreversible.addElement(awy);//insertLink(awy);
	    unsettled.remove(pos);//removeLink(pos);
	    /*System.out.println("Paso 2.4. i="+i+" unsettled=");
	    printLL(unsettled);*/
	}//end else
      }
    }
    /*System.out.println("Paso 3. iteracion="+j+" unsettled=");
      printLL(unsettled);*/

    
    if ((existsWX) && (!parentY)) {
	irreversible.addElement(axy);//insertLink(axy);
      lim = unsettled.size();
      pos = 0;
      for (i=0 ; i<lim ; i++) {
	a_y = (Link)unsettled.elementAt(pos);
	if (y.compareTo(a_y.getHead()) == 0) {
	    irreversible.addElement(a_y);//insertLink(a_y);
	  unsettled.remove(pos);//removeLink(pos);
	}
	else
	  pos++;
      }
    }
    else {
      yParents = parents(x);
      existsZY = false;
      for (i=0 ; (i<allthelinks.size()) && (!existsZY) ; i++) {
	a_y = allthelinks.elementAt(i);
	if ((x.compareTo(a_y.getTail())!=0) && (y.compareTo(a_y.getHead())==0)) {
	  if (yParents.getId(a_y.getTail().getName()) == -1)
	    existsZY = true;
	}
      }
      
      if (existsZY) {
	  irreversible.addElement(axy);//insertLink(axy);
	lim = unsettled.size();
	pos = 0;
	for (i=0 ; i<lim ; i++) {
	  a_y = (Link)unsettled.elementAt(pos);
	  if (y.compareTo(a_y.getHead()) == 0) {
	      irreversible.addElement(a_y);//insertLink(a_y);
	    unsettled.remove(pos);//removeLink(pos);
	  }
	  else
	    pos++;
	}
      }
      else {
	reversible.insertLink(axy);
	lim = unsettled.size();
	pos = 0;
	for (i=0 ; i<lim ; i++) {
	  a_y = (Link)unsettled.elementAt(pos);
	  if (y.compareTo(a_y.getHead()) == 0) {
	    reversible.insertLink(a_y);
	    unsettled.remove(pos);//removeLink(pos);
	  }
	  else
	    pos++;
	}
      }
    } 
  }
  
  return reversible;
}



/**
 * This method is used to know whether a given graph is complete
 * taking into account only the nodes indicated in the NodeList
 * passed as parameter. 
 *
 * @param nl a <code>NodeList</code>
 * @return <code>true</code> if the subgraph containing the nodes
 * of the <code>NodeList</code> nl, <code>false</code> in other case.
 */

public boolean isComplete(NodeList nl) {
  int i,j;
  Node nodeI,nodeJ;
 
  if (nl.size()<=1) return true;
   
  for(i=0;i<nl.size();i++){
    nodeI = nodeList.getNode(nl.elementAt(i).getName());  
    for(j=i+1;j<nl.size();j++){
      nodeJ = nodeList.getNode(nl.elementAt(j).getName());  
      if (!(nodeI.isNeighbour(nodeJ))) return false;
    }
  }
   
  return true;      
}

  /**
   * This method is used to know how many connected components has the graph
   *
   * @return the number of connected components of the graph
   */
  public int numberOfConnectedComponents() {
    Vector connected      = new Vector();
    Vector others         = new Vector();
    NodeList total        = new NodeList();
    NodeList nodesCon     = new NodeList();
    SetVectorOperations s = new SetVectorOperations();

    this.undirectedAccessibles((Node)this.nodeList.elementAt(0),nodesCon);
    total.join(nodesCon);
    while (nodesCon != null) {
      connected.addElement(nodesCon);
      others      = s.notIn(nodeList.toVector(),total.toVector());
      if (others.size() > 0) {
        Node node = (Node)others.elementAt(0);
      	nodesCon  = new NodeList();
      	this.undirectedAccessibles(node,nodesCon);
      	total.join(nodesCon);
      }
      else
        nodesCon = null;
    }
    return(connected.size());
  }

  /**
   * All the nodes without children are returned
   * @return all the nodes without children
   */
  public Vector getLeafs() {
    Vector leafs = new Vector();
    for(int i= 0; i<this.nodeList.size(); i++)
      if (this.nodeList.elementAt(i).getChildrenNodes().size() == 0) 
        leafs.addElement(this.nodeList.elementAt(i));
    return(leafs);
  }

  /**
   * This method return the connected components of the graph
   *
   * @return the connected components
   */
  public Vector connectedComponents() {
    Vector connected      = new Vector();
    Vector others         = new Vector();
    NodeList total        = new NodeList();
    NodeList nodesCon     = new NodeList();
    SetVectorOperations s = new SetVectorOperations();

    this.undirectedAccessibles((Node)this.nodeList.elementAt(0),nodesCon);
    total.join(nodesCon);
    while (nodesCon != null) {
      connected.addElement(nodesCon);
      others      = s.notIn(nodeList.toVector(),total.toVector());
      if (others.size() > 0) {
        Node node = (Node)others.elementAt(0);
      	nodesCon  = new NodeList();
      	this.undirectedAccessibles(node,nodesCon);
        total.join(nodesCon);
      }
      else
        nodesCon = null;
    }
    return(connected);
  }

/**
 * This method is used to know how many links contains the subgraph
 * induced by the variables in the nodeList passed as parameter
 *
 * @return the number of links in the induced subgraph
 */

public int numberOfLinks(NodeList nl) {
  int i,j;
  Node nodeI,nodeJ;
  int nLinks=0;
 
   
  for(i=0;i<nl.size();i++){
    nodeI = nodeList.getNode(nl.elementAt(i).getName());  
    for(j=i+1;j<nl.size();j++){
      nodeJ = nodeList.getNode(nl.elementAt(j).getName());  
      if ( nodeI.isNeighbour(nodeJ) ) nLinks++;
    }
  }
   
  return nLinks;      
}


/**
 * @param depth
 * @return The set of nodes in the graph that are depth 'depth'
 */
public ArrayList<Node> nodesAtDepth(int depth) {
	// TODO Auto-generated method stub
	NodeList nodes;
	Node auxNode;
	ArrayList<Node> level;
	nodes = this.getNodeList();
	level = new ArrayList<Node>();
	for (int i=0;i<nodes.size();i++){
		auxNode = nodes.elementAt(i);
		if (getDepth(auxNode)==depth){
			level.add(auxNode);
		}
		
	}
	return level;
}




/**
 * @param auxNode
 * @return The depth of the node in the graph: length of the maximum path finishing in this node
 */
private int getDepth(Node auxNode) {
	// TODO Auto-generated method stub
	Vector ancestors;
	int depth;
	
	
	ancestors = this.ascendants(auxNode);
	if (ancestors.size()==0){
		depth=0;
	}
	else{
		int maxDepthFather = 0;
		for (int i=0;i<ancestors.size();i++){
			int depthFather = getDepth(((Node)ancestors.elementAt(i)));
			if (depthFather>maxDepthFather){
				maxDepthFather = depthFather;
			}
		}
		depth = maxDepthFather+1;
	}
	return depth;

}

/**
 * This method returns the node (or one of the nodes) that has
 * the most connections, children and parents (and undirected neighbours if they exist).
 * 
 * @return the maximally connected node in this graph.
 */
public Node getMaximallyConnectedNode(){
	int[] counts = new int[nodeList.size()];
	Node n;
	LinkList ll = this.linkList;
	for(int i=0; i<this.linkList.size();i++){
		Link l = ll.elementAt(i);
		counts[this.nodeList.getId(l.getHead())]++;
		counts[this.nodeList.getId(l.getTail())]++;
	}
	int maxConnectionsIndex = 0;
	for(int i = 0; i<counts.length;i++){
		maxConnectionsIndex = (counts[maxConnectionsIndex] < counts[i] ? i : maxConnectionsIndex);
	}
	return nodeList.elementAt(maxConnectionsIndex);
}

/**
 * This method computes and returns a Vector of connected components that 
 * will be in the graph with the same structure as this graph, but where one 
 * node is completely removed from the graph. A connected component is represented
 * as a Vector<Node> object, and hence the method returns a Vector<Vector<Node>> object.
 * 
 * Two <b>important</b> points :
 * <OL>
 * <LI> The method uses exclusively the LinkList of this Graph. That is, we do not
 * neither considder nor change the state of the parents, children or siblings of
 * any node. In any case, it should be considered a safe method as it does not alter 
 * the Graph object on which it is called.
 * <LI> The method was originally written to get the connected components of a 
 * FAN structured Bnet, where the class node is then naturally the argument.
 * </OL>
 * @param nodeToLeaveOut - the node that is not considered in the structure
 * @return
 */
public Vector<Vector<Node>> getConnectedComponentsWithoutNode(Node nodeToLeaveOut){
	Vector<Vector<Node>> cc = new Vector<Vector<Node>>();
	Vector<Node> pendingNodes = new Vector<Node>(this.nodeList.getNodes());
	Vector<Link> myLinkList = new Vector<Link>(this.linkList.getLinks());
	pendingNodes.remove(nodeToLeaveOut);
	Node currentNode;
	
	// the Vector<Node> object pendingNodes contains the 
	// nodes that we have not yet assigned to a connected
	// component
	while(!pendingNodes.isEmpty()){
		Vector<Node> currentCC = new Vector<Node>();
		Stack<Node> pendingCurrentCC = new Stack<Node>();
		pendingCurrentCC.push(pendingNodes.firstElement());
		
		// the Stack<Node> object pendingCurrentCC contains the nodes
		// contained in the current connectedcomponent, but has not yet
		// been traversed.
		while(!pendingCurrentCC.isEmpty()){
			currentNode = pendingCurrentCC.pop(); pendingNodes.remove(currentNode);
			if(!currentCC.contains(currentNode)) currentCC.add(currentNode);
			Vector<Link> traversed = new Vector<Link>();
			for(Link l : myLinkList){
				Node nextNode = null;
				if(l.getHead() == currentNode){ nextNode = l.getTail();}
				else if(l.getTail() == currentNode){ nextNode = l.getHead();}
				if(nextNode != null && nextNode != nodeToLeaveOut){
					pendingCurrentCC.push(nextNode);
					pendingNodes.remove(nextNode);
					traversed.add(l);
				}
			}
			myLinkList.removeAll(traversed);
			
		}
		cc.add(currentCC);
	}
	return cc;
} 

} // End of class

