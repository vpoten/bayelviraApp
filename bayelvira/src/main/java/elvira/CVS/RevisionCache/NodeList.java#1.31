/* NodeList.java */

package elvira;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.Collections;
import java.io.*;
import java.util.Random;
import elvira.parser.*;


/**
 * Implements the list of nodes in a graph.
 *
 * @since 21/9/2000
 */

public class NodeList implements Cloneable, Serializable {

static final long serialVersionUID = -4330825935889988611L;
    
    
/**
 * Contains the list of nodes.
 */
private Vector<Node> nodes;

private boolean sorted = false;

/**
 * Constructor. Creates an empty list of nodes.
 */

public NodeList() {

  nodes = new Vector<Node>();
}


/**
 * Constructor. Creates an empty list of nodes but with
 * an initial capacity for the vector.
 */

public NodeList(int cap) {

  nodes = new Vector<Node>(cap);
}

/**
 * Constructor. Creates an empty list of nodes but with
 * an initial capacity for the vector. The NodeList can
 * be defined as sorted.
 */

public NodeList(int cap, boolean s) {

  nodes = new Vector<Node>(cap);
  sorted = s;
}


/**
 * Constructs a <code>NodeList</code> from a vector of nodes.
 * @param varList is the vector of nodes.
 */

public NodeList(Vector<Node> varList) {

  int i;

  nodes = new Vector<Node>();
  for (i=0 ; i<varList.size() ; i++) {
    nodes.addElement(varList.elementAt(i));
  }
}


/**
 * Constructs a new list reading from a file, given a list
 * of all possible variables.
 * @param f the input file.
 * @param list the list of all possible variables.
 */

public NodeList(FileInputStream f, NodeList list) throws ParseException ,IOException {

  VariableListParse parser = new VariableListParse(f);

  parser.initialize(list);

  parser.CompilationUnit();
  nodes = parser.outputNodes.toVector();
}



/**
 * Constructs a new list with a random ordering, given a list
 * of all possible variables saving it in a file.
 * @param f the output file.
 * @param list the list of all possible variables.
 */

public NodeList(FileWriter f,NodeList variables) throws IOException {

    PrintWriter fsal = new PrintWriter(f);
    Random generator = new Random();
    int[] tmpIndex = new int[variables.size()];
    int nvariables = variables.size();
    int i,j,k,AddedInts;
    int[] Canonic;

    Canonic = new int[nvariables];

    for (i=0;i<nvariables;i++)
	Canonic[i]=i;

    AddedInts = nvariables;
    while (AddedInts>1) {
	j = (int)(generator.nextDouble()*AddedInts);
	tmpIndex[nvariables-AddedInts] = Canonic[j];
	AddedInts--;
	for (k=j;k<AddedInts;k++)
	    Canonic[k]=Canonic[k+1];
    }
    tmpIndex[nvariables-1] = Canonic[0];

    fsal.print("variables ALEATORIAS { \n\n");

    nodes = new Vector();
    for(i=0 ; i< tmpIndex.length ; i++){
	Node node = variables.elementAt(tmpIndex[i]);
	nodes.addElement(node);
	fsal.print("\n"+node.getName()+",");
    }
    fsal.print("\n}");
    fsal.close();
}



/**
 * Constructs a new list with a random ordering, given a list
 * of all possible variables.
 * @param list the list of all possible variables.
 */

public NodeList(Random generator, NodeList variables){

    int[] tmpIndex = new int[variables.size()];
    int nvariables = variables.size();
    int i,j,k,AddedInts;
    int[] Canonic;

    Canonic = new int[nvariables];

    for (i=0;i<nvariables;i++)
	Canonic[i]=i;

    AddedInts = nvariables;
    while (AddedInts>1) {
	j = (int)(generator.nextDouble()*AddedInts);
	tmpIndex[nvariables-AddedInts] = Canonic[j];
	AddedInts--;
	for (k=j;k<AddedInts;k++)
	    Canonic[k]=Canonic[k+1];
    }
    tmpIndex[nvariables-1] = Canonic[0];

    nodes = new Vector();
    for(i=0 ; i< tmpIndex.length ; i++){
	Node node = variables.elementAt(tmpIndex[i]);
	nodes.addElement(node);
    }    
}



public Vector<Node> getNodes() {

  return nodes;
}


public boolean getSorted(){
  return sorted;
}

public void setSorted(boolean s){
  sorted = s;
}

/**
 * Sets the nodes of the list.
 * @param nl the new nodes.
 */

public void setNodes(Vector nl) {

  nodes = nl;
  // if the list was defined as sorted, we test if nl is sorted
  if (sorted) 
    if (!isSorted()) sorted=false;
}

/**
 * Inserts a node at the end of the list.
 * @param n the node to insert.
 */

public void insertNode(Node n) {

  if (!(sorted)) {
    nodes.addElement(n);
    //System.out.print("n");
  }
  else{
    int pos = getOrderedInsertionPoint(n);
    nodes.insertElementAt(n, pos);
    //System.out.print("s");
  }
}

/**
 * Removes a node from the list.
 * @param n the node to remove.
 */

public void removeNode(Node n) {

  int position = -1;

  if (!sorted){
    for (int i=0; i<nodes.size(); i++) {
      if (((Node) nodes.elementAt(i)).getName().equals(n.getName())) {
        position = i;
      }
    }
  }
  else position = locateNodeByBinSearch(n);

  if (position != -1) {
    nodes.removeElementAt(position);
  }
}


/**
 * Removes a node from the list.
 * @param position the position of the node to be removed.
 */

public void removeNode(int position) {

  nodes.removeElementAt(position);
}

/**
 * Remove nodes of the kind passed as argument from the list
 * @param kind
 */
public void removeNodes(int kind){
  Node node;
  for(int i=0; i < nodes.size(); i++){
    if (((Node)nodes.elementAt(i)).getKindOfNode() == kind){
       nodes.removeElementAt(i);
       i=0;
    }
  }
}


/**
 * Gets the position of a node in the list.
 * @param name the name of the node to search.
 * @return the position of node <code>name</code>, or -1 if it is not found.
 */

public int getId(String name) {

  int position;

  if (!sorted){
    for (position=0 ; position<nodes.size() ; position++)
      if (name.trim().compareTo(((Node)nodes.elementAt(position)).getName().trim())==0)
        return position;
  }
  else return locateNodeByBinSearch(name);

  return (-1);
}


/**
 * Gets the position of a node in the list.
 * @param node the node to search.
 * @return the position of <code>node</code>, or -1 if it is not found.
 */

public int getId(Node node) {

  int position = -1;

  if (!sorted){   
    for (int i=0; i<nodes.size(); i++) {
      if (((Node) nodes.elementAt(i)).getName().equals(node.getName())) {
        position = i;
        return position;
      }
    }
  } 
  else return locateNodeByBinSearch(node);

  return position;
}


/**
 * Gets the node with the name given as parameter.
 * @param name The name of a node.
 * @return The node with the name given or error if this name
 * can't be found in the list.
 */

public Node getNode(String name) {

  return ((Node)nodes.elementAt(getId(name)));
}


/**
 * Gets the node with the name or title given as parameter.
 * @param name The name or the title of a node.
 * @return The node with the name or title  given or error if this name
 * can't be found in the list.
 */

public Node getNodeString(String name, boolean byTitle) {
// important ---- no taking advantage if sorted
    int n = 0;
    boolean found = false;

    while (n<nodes.size() && !found) {
      Node nf = (Node)nodes.elementAt(n);
      if (nf.getTitle().equals(name) && byTitle) {
	found = true;
	return nf;
      }
      else
	if (byTitle && nf.getTitle().equals("") && nf.getName().equals(name)) {
	  found = true;
	  return nf;
        }
        else
	  if (!byTitle && nf.getName().equals(name)) {
	    found = true;
	    return nf;
	  }
	  else n++;
    }
    return null;
}


/**
 * Calculates the size of the list.
 * @return the number of nodes in the list.
 */

public int size() {

  return ((int)nodes.size());
}


/**
 * Calculates the mutiplication of the sizes of the nodes in the list
 * (the dimension of the joint distribution) for those nodes.
 * @return the resulting size.
 */

public double getSize() {

  double size;
  int i, type, kind;
  Node node;
  NodeList parents;

  size = 1;
  for (i=0 ; i < size() ; i++) {
    node = elementAt(i);
    type = node.getTypeOfVariable();
    kind = node.getKindOfNode();

    if (type != node.CONTINUOUS && type != node.MIXED)
      size *= ((FiniteStates) node).getNumStates();
    else {
      if (kind == node.CHANCE) {
	/*
         * The size depends on the parents
         */

	parents = node.getParentNodes();
	size *= parents.getSize();
      }
    }
  }
  return size;
}


/**
 * Finds the element at the given position.
 * @param position an integer lower than the size of
 * the list - 1. Otherwise, an error is obtained.
 * @return the node at position <code>position</code>.
 */

public Node elementAt(int position) {

  return ((Node)nodes.elementAt(position));
}


/**
 * Returns the first node in the list.
 * @return the first node in the list.
 */

public Node firstElement() {
   return (Node)nodes.firstElement();
}

/**
 * Returns the last node in the list.
 * @return the last node in the list.
 */

public Node lastElement() {

   return (Node)nodes.lastElement();
}


/**
 * Creates an enumeration with the elements of the list.
 * @return The enumeration created.
 */

public Enumeration elements() {

  return((Enumeration)nodes.elements());
}


/**
 * Saves the list.
 * @param p the <code>PrintWriter</code> where the list will be saved.
 */

public void save(PrintWriter p) {

  Enumeration enumerator;

  enumerator = nodes.elements();

  p.print("// Network Variables \n\n");

  while (enumerator.hasMoreElements())
    ((Node)enumerator.nextElement()).save(p);
}



/**
 * Converts the list to a <code>String</code>. All the information
 * about the nodes in the list is inserted in the string.
 * @return the string generated.
 */

public String toString() {

  StringBuffer salida = new StringBuffer("");
  Node n;

  for (int i=0 ; i<nodes.size() ; i++) {
    n = (Node)nodes.elementAt(i);
    salida.append(n.toString());
    salida.append("\n");
  }
  return (salida.toString());
}


/**
 * Converts the list to a <code>String</code>. This string will contain the
 * names of the nodes in the list separated by commas.
 * @return the string generated.
 */

public String toString2() {

  StringBuffer salida = new StringBuffer("");
  Node n;
  for (int i=0 ; i<nodes.size()-1 ; i++) {
    n = (Node)nodes.elementAt(i);
    salida.append(n.getName());
    salida.append(", ");
  }

  if (nodes.size() > 0)
    salida.append(((Node)nodes.lastElement()).getName());
  return (salida.toString());
}


/**
 * Prints the list to the standard output.
 */

public void print() {

  Enumeration enumerator;

  enumerator = nodes.elements();

  System.out.println("// Network Variables \n\n ");

  while(enumerator.hasMoreElements())
    ((Node)enumerator.nextElement()).print();
}


/**
 * Prints the names of the variables in list.
 */

public void printNames() {
  System.out.println(this.toString2());
}


/**
 * Merges this list with the argument one.
 * The current list is modified, and will contain the result of the
 * operation.
 * @param list a <code>NodeList</code> to merge with this one.
 */

public void merge(NodeList list) {

  int i,j,c;
  Node node;

  if ( !(sorted && list.getSorted()) ){
    for (i=0 ; i<list.size() ; i++) {
      node = list.elementAt(i);
      if (getId(node.getName()) == -1)
        insertNode(node);
    }
  }
  else{//both are sorted
    Vector v = new Vector(Math.max(this.size(),list.size()));

    for(i=0,j=0; ((i<this.size()) && (j<list.size())); ){
      c = compareNodesByName((Node)nodes.elementAt(i),list.elementAt(j));
      if (c<0) v.addElement(nodes.elementAt(i++));
      else if (c>0) v.addElement(list.elementAt(j++));
      else {v.addElement(nodes.elementAt(i)); i++; j++;} 
    }

    for( ; i<this.size(); i++) v.addElement(nodes.elementAt(i));
    for( ; j<list.size(); j++) v.addElement(list.elementAt(j));

    nodes=v;
  }

}


/**
 * Joins this list with the argument.
 * The current list is modified, and will contain the result of the
 * operation.
 * @param list a <code>NodeList</code> to join with this one.
 */

public void join(NodeList list) {

 int i;
 Node node;

 if (!(sorted && list.getSorted())){  
   for (i=0 ; i<list.size() ; i++) {
     node = list.elementAt(i);
     if (getId(node) == -1)
       insertNode(node);
   }
 }
 else{  // both are sorted
   this.merge(list);
 }
}


/**
 * Computes the difference between this list and the argument one.
 * @param list a <code>NodeList</code>.
 * @return a <code>NodeList</code> with the nodes in this list
 * that are not contained in <code>list</code>.
 */

public NodeList difference(NodeList list) {

  int i,j,c;
  Node node;
  NodeList nl = new NodeList();
  Vector v = new Vector();

  if (!(sorted && list.getSorted()) ){
    for (i=0 ; i<this.size() ; i++) {
      node = this.elementAt(i);
      if (list.getId(node) == -1)
        nl.insertNode(node);
    }
  }
  else{ // we can do better 
    nl.setSorted(true);
    for(i=0,j=0; ((i<nodes.size()) && (j<list.size())); ){
      c = compareNodesByName((Node)nodes.elementAt(i),list.elementAt(j));
      if (c < 0) v.addElement(nodes.elementAt(i++));
      else if (c > 0) j++;
      else if (c == 0) {i++;j++;} 
    } 
    for( ; i<nodes.size(); i++) v.addElement(nodes.elementAt(i));
    nl.setNodes(v);
  }

  //nl.setNodes(v);
  return nl;
}



/**
 * Computes the difference between this list and the argumant one.
 * Nodes are compared by their names.
 * @param list a <code>NodeList</code>.
 * @return a <code>NodeList</code> with the nodes in this list
 * that are not contained in <code>list</code>.
 */

public NodeList differenceNames(NodeList list) {

  int i;
  Node node;
  NodeList nl = new NodeList();

  if (!(sorted && list.getSorted()) ){
    for (i=0 ; i<this.size() ; i++) {
      node = this.elementAt(i);
      if (list.getId(node.getName()) == -1)
        nl.insertNode(node);
    }
  }
  else return this.difference(list);

  return nl;
}


/**
 * Computes the intersection between this list and the argument one.
 * @param list a <code>NodeList</code>.
 * @return a <code>NodeList</code> with the nodes in this list
 * that are also contained in <code>list</code>.
 */

public NodeList intersection(NodeList list) {

  int i,j,c;
  Node node;
  NodeList nl = new NodeList();
  NodeList l1,l2;
  Vector v = new Vector();

  // if only one of the two list is sorted it is better to use it to search
  // else we look in the smaller one

  if (sorted) {l1=this;l2=list;}
  else if (list.getSorted()) {l1=list;l2=this;}
  else if (this.size()<=list.size()) {l1=this;l2=list;}
  else {l1=list;l2=this;}

  // doing intersection

  if ( !(sorted && list.getSorted()) ){
    for (i=0 ; i<l2.size() ; i++) {
      node = l2.elementAt(i);
      if (l1.getId(node) != -1)
        nl.insertNode(node);
    }
  }
  else{//we can do it better
    nl.setSorted(true);
    for(i=0,j=0; ((i<nodes.size()) && (j<list.size())); ){
      c = compareNodesByName((Node)nodes.elementAt(i),list.elementAt(j));
      if (c < 0) i++;
      else if (c > 0) j++;
      else if (c == 0) {v.addElement(nodes.elementAt(i));i++;j++;}
    } 
    nl.setNodes(v);
  }

  //nl.setNodes(v);
  return nl;
}


/**
 * Computes the intersection between this list and the argumant one.
 * Nodes are compared by their names.
 * @param list a <code>NodeList</code>.
 * @return a <code>NodeList</code> with the nodes in this list
 * that are also contained in <code>list</code>.
 */

public NodeList intersectionNames(NodeList list) {

  int i;
  Node node;
  NodeList nl = new NodeList();

  if (!(sorted && list.getSorted())){
    for (i=0 ; i<this.size() ; i++) {
      node = this.elementAt(i);
      if (list.getId(node.getName()) != -1)
        nl.insertNode(node);
    }
  }
  else return this.intersection(list);
  return nl;
}


/**
 * Return the list of variables as a vector.
 * @return a <code>Vector</code> with the variables.
 */

public Vector toVector() {

  return nodes;
}


/**
 * Computes all the lists of size <code>n</code> that can be obtained from
 * this list.
 * @param n size of the subsets.
 * @return a <code>Vector</code> with the <code>NodeList</code> subsets of
 * this, of size <code>n</code>.
 */

public Vector subSetsOfSize(int n) {

  Vector aux = new Vector();
  NodeList subSet = new NodeList();
  int indexSubSet[], i, j, k;
  boolean found = true;

  indexSubSet = new int[n];

  if ((n > 0) & (n <= nodes.size())) {
    for (i=0 ; i<n ; i++) {
      indexSubSet[i] = i;
      subSet.insertNode((Node)nodes.elementAt(i));
    }
    aux.addElement(subSet);

    if (n < nodes.size()) {
      while (found) {
	found = false;

	for (i=n-1 ; i>=0 ; i--)
	  if (indexSubSet[i] < (nodes.size()+(i-n))) {
	  indexSubSet[i] = indexSubSet[i]+1;

	  if (i < (n-1)) {
	    for (j=i+1 ; j<n ; j++)
	      indexSubSet[j] = indexSubSet[j-1] + 1;
	  }

	  found = true;
	  break;
	}

	if (found) {
	  subSet = new NodeList();
	  for (k=0 ; k<n ; k++)
	    subSet.insertNode((Node)nodes.elementAt(indexSubSet[k]));

	  aux.addElement(subSet);
	}
      }
    }
  }

  return aux;
}




/**
 * Decides whether two lists are the same.
 * @param nl a <code>NodeList</code> to compare to this one.
 * @return <code>true</code> if both lists are equal, <code>false</code>
 * otherwise.
 */

public boolean equals(NodeList nl) {

  int i;

  if (size() != nl.size())
    return false;
  
  // if only one of the two list is sorted it is better to use it for searching
  NodeList l1=null,l2=null;

  if (sorted) {l1=this;l2=nl;}
  else {l1=nl;l2=this;}

  // comparing equality

  if ( !(sorted && nl.getSorted()) ){
    for (i=0 ; i<l2.size() ; i++) {
      if (l1.getId(l2.elementAt(i)) == -1)
      return false;
    }
  }
  else{ //we can do better
    for(i=0; i<size(); i++)
      if (compareNodesByName((Node)nodes.elementAt(i),nl.elementAt(i)) != 0) 
        return false;
  }

  return true;
}

/**
 * Method to check if two nodelists contains the same variables
 * (taking into account their names)
 * @param other
 * @return boolean
 */
public boolean equalsByName(NodeList other){
  // Check if both of them has the same number of nodes
  if (size() != other.size())
    return false;

  // If both nodelists contain the same number of nodes,
  // compare them one by one
  Node thisNode, otherNode;
  for(int i=0; i < size(); i++){
    thisNode=(Node)nodes.elementAt(i);
    if (other.getId(thisNode.getName()) == -1){
      return false;
    }
  }

  // If this point is reached, return true
  return true;
}

/**
 * @param nl a <code>NodeList</code> 
 * @return true if the object is included in the nodelist passed as
 * parameter. Comparisons are carried out by name
 */
public boolean isIncluded(NodeList nl){

  int i,j,c;

  if (size() > nl.size())
    return false;


  if (!(sorted && nl.getSorted())){
    for (i=0 ; i<size() ; i++) {
      if (nl.getId(this.elementAt(i).getName()) == -1)
        return false;
    }
  }
  else{// we can do it better
    for(i=0,j=0; ((i<nodes.size()) && (j<nl.size())); ){
      c = compareNodesByName((Node)nodes.elementAt(i),nl.elementAt(j));
      if (c < 0) return false;
      else if (c > 0) j++;
      else if (c == 0) {i++;j++;}
    } 
    if (i<this.size()) return false;
  }

  return true;
}


/**
 * Duplicates this list.
 * @return a duplicate of this <code>NodeList</code>.
 */

public NodeList duplicate() {

  NodeList dlist = new NodeList(this.size());
  Node node;

  if (sorted) dlist.setSorted(true);
  dlist.nodes.setSize(nodes.size());

  for (int posN=0 ; posN < nodes.size() ; posN++) {
    node = (Node)nodes.elementAt(posN);
    dlist.nodes.setElementAt(node.copy(),posN);
  }
  return dlist;
}


/**
 * Creates a copy of this list.
 * @return the list created.
 */

public NodeList copy() {

  NodeList dlist = new NodeList(this.size());
  Node node;

  dlist.nodes.setSize(nodes.size());
  if (sorted) dlist.setSorted(true);

  for (int posN=0 ; posN<nodes.size() ; posN++) {
    node = (Node)nodes.elementAt(posN);
    dlist.nodes.setElementAt(node, posN);
  }
  return dlist;
}


/**
 * Sort the variables in the list according to the order
 * of these variables in the list given as argument (pattern).
 * NOTE: the variables of this <code>NodeList</code> not included in the
 * pattern will be situated in the fist positions of the new list.
 * @param pattern a list of nodes.
 */

public void sort(NodeList pattern) {

  int i, k, posMinor, indexMinor;
  Node minor, aux;

  for (i=0 ; i<(nodes.size()-1) ; i++) {

    posMinor = i;
    minor = (Node) nodes.elementAt(i);
    indexMinor = pattern.getId(minor);

    for (k=i+1 ; k<nodes.size() ; k++) {
      aux = (Node)nodes.elementAt(k);
      if ( pattern.getId(aux) < indexMinor ){
        posMinor = k;
        minor = (Node)nodes.elementAt(k);
        indexMinor = pattern.getId(minor);
      }
    }

    nodes.setElementAt((Node)nodes.elementAt(i),posMinor);
    nodes.setElementAt(minor,i);
  }

  setSorted(pattern.getSorted());
}


/* sort the variables in the node list according to the order
 * of these variables in the node list given as argument (pattern).
 *
 * NOTE: the variables of the nodeList not included in pattern will be
 * situated in the fist positions of the new list.
 *
 * @param pattern a superset of this
 * @return NodeList a list of nodes of this sorted
 */

public NodeList sortNames(NodeList pattern) {

  int i,k,posMenor,indexMenor;
  Node menor,aux;
  NodeList nodesReturn = copy();

  for(i=0; i<(nodesReturn.size()-1) ;i++){

    posMenor = i;
    menor = (Node) nodesReturn.elementAt(i);
    indexMenor = pattern.getId(menor.getName());

    for(k=i+1;k<nodesReturn.size();k++){
      aux = (Node)nodesReturn.elementAt(k);
      if ( pattern.getId(aux.getName()) < indexMenor ){
        posMenor = k;
        menor = (Node)nodesReturn.elementAt(k);
        indexMenor = pattern.getId(menor.getName());
      }
    }

    nodesReturn.nodes.setElementAt((Node)nodesReturn.elementAt(i),posMenor);
    nodesReturn.nodes.setElementAt(menor,i);

  }

  nodesReturn.setSorted(pattern.getSorted());
  return nodesReturn;
}


/**
 * Determines the kind of relation between this list and the argument one.
 * @param set a <code>NodeList</code>.
 * @return a string with the kind of inclusion of this list and
 * <code>set</code>. The possible outcomes are: <p>
 * <ul>
 * <li> "subset": this list is contained in <code>set</code>.
 * <li> "empty" : the intersection between both lists is empty.
 * <li> "not empty": the intersection between both lists is not empty,
 * but there are nodes in this list not belonging to <code>set</code>.
 * </ul>
 */

public String kindOfInclusion(NodeList set) {

  int i, s, n=0, j, r;
  String c;

  s = this.size();

  if (!(sorted && set.getSorted())){
    for (i=0 ; i<s ; i++)
      if (set.getId(this.elementAt(i)) != -1)
        n++;
  }
  else{ //both lists are sorted
    for(i=0,j=0; ((i<nodes.size()) && (j<set.size())); ){
      r = compareNodesByName((Node)nodes.elementAt(i),set.elementAt(j));
      if (r < 0) i++;
      else if (r > 0) j++;
      else if (r == 0) {n++;i++;j++;}
    } 
  }


  if (n == 0)
    c = new String("empty");
  else {
    if (n == this.size())
      c = new String("subset");
    else
      c = new String("not empty");
  }

  return c;
}


public void setElementAt(Node x , int pos){

   nodes.setElementAt(x,pos);

   //testing if the list is still sorted
   if (sorted){
     if (!(x.getName().compareTo(elementAt(pos-1).getName())<=0)) 
       sorted=false;
     else if (!(x.getName().compareTo(elementAt(pos+1).getName())>=0)) 
       sorted=false; 
   }

}

/**
 * Sort the variables in the list according to a random order
 * @return a list of nodes.
 */



public NodeList randomOrder(){
    Random generator = new Random();
    return (new NodeList(generator,this));    
}

/**
 * returns the next sequence (1 move from left to right, by interchanging) from current 
 * to goal. For example, (15342) = (12345).next(15324). Returns null 
 * if both sequences are the same.
 *
 * IMPORTANT: both sequences MUST have the same length and the same set of
 * nodes, otherwise the program exits given an error message. 
 *
 * @param goal a <code>NodeList</code> which represent the goal sequence
 * @return a <code>NodeList</code> (null if both sequences are the same)
 */

 public NodeList next(NodeList goal){
   NodeList nl = new NodeList(size(),false);
   boolean error = false, changed=false;
   int i,posNode=-1;
   Node node=null,nodeDest;

   if (this.size() != goal.size()) error=true;

   for(i=0;((i<this.size()) && !error);i++){
     node = this.elementAt(i);
     nodeDest = goal.elementAt(i);
     if (node.getName().equals(nodeDest.getName()) )
       nl.insertNode(node);
     else{
       nl.insertNode(nodeDest);
       posNode = this.getId(nodeDest.getName());
       if (posNode==-1) error=true;
       changed = true;
       break; 
     } 
   }

   if (changed){
     for(i=i+1; i<this.size(); i++)
       nl.insertNode(this.elementAt(i));
     nl.setElementAt(node,posNode);
   }

   if (error){
     System.out.println("*** ERROR in TriangulateHeur.next: not compatible sequences, exiting ...");
     System.exit(0);
   }

   if (!changed) return null;
   else return nl;
 }


/**
 * returns the next sequence (1 move from left to right by insertion) from current 
 * to goal. For example, (15342) = (12345).next(15324). Returns null 
 * if both sequences are the same.
 *
 * IMPORTANT: both sequences MUST have the same length and the same set of
 * nodes, otherwise the program exits given an error message. 
 *
 * @param goal a <code>NodeList</code> which represent the goal sequence
 * @return a <code>NodeList</code> (null if both sequences are the same)
 */

 public NodeList next2(NodeList goal){
   NodeList nl = new NodeList(size(),false);
   boolean error = false, changed=false;
   int i,posNode=-1;
   Node node=null,nodeDest;

   if (this.size() != goal.size()) error=true;

   for(i=0;((i<this.size()) && !error);i++){
     node = this.elementAt(i);
     nodeDest = goal.elementAt(i);
     if (node.getName().equals(nodeDest.getName()) )
       nl.insertNode(node);
     else{       
       nl.insertNode(nodeDest);
       posNode = this.getId(nodeDest.getName());
       if (posNode==-1) error=true;
       changed = true;
       break; 
     } 
   }

   if (changed){
     for( ; i<this.size(); i++)
       if (i!=posNode) nl.insertNode(this.elementAt(i));
   }

   if (error){
     System.out.println("*** ERROR in TriangulateHeur.next: not compatible sequences, exiting ...");
     System.exit(0);
   }

   if (!changed) return null;
   else return nl;
 }

/**
 * interchanges the nodes in position p1 and p2
 *
 * @param p1 an int representing the first position
 * @param p2 an int representing the second position
 *
 * IMPORTANT: the nodelist is modified
 *
 * @return a <code>NodeList</code> 
 */

public NodeList interchange(int p1, int p2){
  Node temp;

  temp = (Node) nodes.elementAt(p1);
  nodes.setElementAt(nodes.elementAt(p2),p1);
  nodes.setElementAt(temp,p2);
 
  //checking if the list is still sorted
  if (sorted) 
    if (elementAt(p1).getName().compareTo(elementAt(p2).getName())!=0)
      sorted = false;

  return this;
}

// pepe

/**
 * Sort the nodes in the list by using lexicographic order
 */

public void sortByNames(){
  qsort(0,nodes.size()-1);
}

/**
 * quick sort.
 */

  void qsort(int lo0, int hi0)   {
      int lo = lo0;
      int hi = hi0;
      Node mid;
 
      if ( hi0 > lo0)
	{
	  mid = (Node) nodes.elementAt( ( lo0 + hi0 ) / 2 );
	  while( lo <= hi )
	    {    
	      while( ( lo < hi0 ) && (compareNodesByName( (Node)nodes.elementAt(lo) , mid) < 0) )
		++lo;
	      while( ( hi > lo0 ) && (compareNodesByName( (Node)nodes.elementAt(hi) , mid) > 0) )
		--hi;
	      if( lo <= hi ) 
		{
		  swap(lo, hi);
		  ++lo;
		  --hi;
		}
	    }
	  if( lo0 < hi )
            qsort( lo0, hi );
	  if( lo < hi0 )
            qsort( lo, hi0 );
	}
  }

  private void swap(int i, int j){
    Node obj;
    obj = nodes.elementAt(i);
    nodes.setElementAt(nodes.elementAt(j),i);
    nodes.setElementAt(obj,j);
  }


/**
 * isSorted: 
 *
 * @return true if the nodes are sorted lexicographically
 */

public boolean isSorted(){
  boolean s = false;

  for(int i=0;i<nodes.size()-1;i++)
    if (compareNodesByName((Node)nodes.elementAt(i),(Node)nodes.elementAt(i+1)) > 0) 
      return false;

  sorted=true;
  return true;
}


/**
 * return the position in which the node has to be inserted in
 * order to maintain the list sorted by name
 * 
 * @param n the <code>Node</code> to be inserted in the list
 * @return the position in which n has to be inserted
 */
 
public int getOrderedInsertionPoint(Node n){

  int min,max,mid=-1;
  int s;
  Object middle;
  int c;

  s = nodes.size();
  if (s==0) mid=0;
  else if (compareNodesByName((Node)nodes.elementAt(s-1),n) < 0) mid=s;
  else{
    for(min=0, max=s-1, mid=(int)(min+max)/2;
        min < max;
        mid = (int) (min+max)/2){
  
      middle = nodes.elementAt(mid);
      c = compareNodesByName(n,(Node)middle);
      if (c < 0) max = mid;
      else if (c > 0) min = mid + 1;
      else break; // mid is the position
    }
  }

  return mid;
}

/**
 * locateNodeByBinSearch: look for a node by name using binary search
 *
 * @param n the <code>Node</code> to locate in the list
 * @return the position in which the node is place or -1 if there is no
 *		node with the same name in the list
 */


public int locateNodeByBinSearch(Node n){
  return locateNodeByBinSearch(n.getName());
}

public int locateNodeByBinSearch(String name){
  int low=0,high=nodes.size()-1,mid=0;
  boolean found=false;
  Node nodeM=null;
  int c;

  while ( (low <= high) && !found) {
    mid = (int) (low+high)/2;
    nodeM = (Node)nodes.elementAt(mid);
    c = name.compareTo(nodeM.getName());   
    if (c < 0) high = mid-1;
    else if (c > 0) low = mid+1;
    else // c==0
      found = true;
  }

  if (found) return mid;
  else return -1;
}


// main

public static void main(String args[]){
  NodeList nl,nl2;
  FiniteStates fs;
  String[] estates = {"yes","no"};  

  nl = new NodeList(6);
  fs = new FiniteStates("F1",estates); nl.insertNode(fs);
  fs = new FiniteStates("E1",estates); nl.insertNode(fs);
  fs = new FiniteStates("D1",estates); nl.insertNode(fs);
  fs = new FiniteStates("C1",estates); nl.insertNode(fs);
  fs = new FiniteStates("B1",estates); nl.insertNode(fs);
  fs = new FiniteStates("A1",estates); nl.insertNode(fs);
  nl.printNames(); System.out.println();
  if (nl.isSorted()) System.out.println("-- ordenada --");
  else System.out.println("-- desordenada --");
  nl.sortByNames();
  nl.printNames(); System.out.println();
  if (nl.isSorted()) System.out.println("-- ordenada --");
  else System.out.println("-- desordenada --");
  nl.setSorted(true);

  nl2 = new NodeList(6,true);
  fs = new FiniteStates("F2",estates); nl2.insertNode(fs);
  fs = new FiniteStates("A2",estates); nl2.insertNode(fs);
  fs = new FiniteStates("D2",estates); nl2.insertNode(fs);
  fs = new FiniteStates("C2",estates); nl2.insertNode(fs);
  fs = new FiniteStates("B2",estates); nl2.insertNode(fs);
  fs = new FiniteStates("E2",estates); nl2.insertNode(fs);
  System.out.println(nl2.toString2());
  if (nl2.isSorted()) System.out.println("-- ordenada --");
  else System.out.println("-- desordenada --");

  if (nl.equals(nl2)) System.out.println("\n iguales ");
  NodeList nl3 = nl.intersection(nl2);
  nl3.printNames();

  nl.join(nl2);
  if (nl2.isIncluded(nl)) System.out.println("\n nl2 incluido en nl ");
  else System.out.println("\n no");
  if (nl.isIncluded(nl2)) System.out.println("\n nl incluido en nl2 ");
  else System.out.println("\n no no");


  System.out.println(nl.toString2());

  nl3 = nl.intersection(nl2);
  nl3.printNames();

  if (nl3.isSorted()) System.out.println("-- ordenada --");
  else System.out.println("-- desordenada --");

  if (nl3.equals(nl2)) System.out.println("\n iguales ");

}

/**
 * compareNodesByName
 * returns -1, 0, or 1 as most compare methods do
 */

private int compareNodesByName(Node n1, Node n2){
  return n1.getName().compareTo(n2.getName());
}


/**
 * Class: NodeComparator
 * 
 * Protected class to order a NodeList by using node.getName
 * It implements the interface Comparator
 */

/*protected class NodeComparatorByName implements Comparator {

  public int compare(Object o1, Object o2){
    Node n1 = (Node)o1;
    Node n2 = (Node)o2;

    return n1.getName().compareTo(n2.getName());
  }
}*/ // End protected class NodeComparatorByName

/**
 * 
 * This method calculates a weight vector for the nodelist
 *
 * @return weights - vector that stores weights
 * 
 */
public double[] getWeights(){
    
    double[] weights = new double[this.size()];
    int size = this.size();
    weights[size-1]=1;
    for(int i=size-2; i>=0;i--){
        weights[i]=weights[i+1]*(((FiniteStates) this.elementAt(i+1)).getNumStates());
    }
    return weights;
} 

/**
 * This method calculates an array with the index of some variables
 * 
 * @param indexOfVars array of indexes
 * @param vars variables to be searched
 * 
 */

public void getIndexOfVars(int[] indexOfVars, NodeList vars){
    int i, pos;
    int nv=vars.size();
    for (i = 0; i < nv; i++) {
            pos = getId(vars.elementAt(i));
            indexOfVars[i] = pos;
        }    
    
}


} // End of class node 
