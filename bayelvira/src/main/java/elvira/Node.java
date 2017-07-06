package elvira;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Abstract class <code>Node</code> implements the basic structure of a
 * node in a probabilistic graphical model. A node represents a
 * variable in the probabilistic model.
 *
 * @since 18/9/2000
 */

public abstract class Node implements Serializable {
   
static final long serialVersionUID = -2139829456556276809L;
//static final long serialVersionUID = -6357035463653168324L;

/* GRAPHICAL CONSTANTS */
/*private final int YSPACE_DRAW_NODE_NAME = 20;
private final int XSPACE_DRAW_NODE_NAME = 12;

public static int CHANCE_NODE_EXPANDED_WIDTH = 192;
public static int CHANCE_NODE_EXPANDED_HEIGHT = 98;
public static int HALF_EXPANDED_WIDTH = 96;
public static int HALF_EXPANDED_HEIGHT = 49;

public static int DECISION_NODE_EXPANDED_WIDTH = 110;
public static int DECISION_NODE_EXPANDED_HEIGHT = 80;
public static int DECISION_HALF_EXPANDED_WIDTH = 55;
public static int DECISION_HALF_EXPANDED_HEIGHT = 40;   

public static int UTILITY_NODE_EXPANDED_WIDTH = 110;
public static int UTILITY_NODE_EXPANDED_HEIGHT = 60;
public static int UTILITY_HALF_EXPANDED_WIDTH = 55;
public static int UTILITY_HALF_EXPANDED_HEIGHT = 30;   */

/* These constants are used to set the type of the node */
public static final int CONTINUOUS = 0;
public static final int FINITE_STATES = 1;
public static final int INFINITE_DISCRETE = 2;
public static final int MIXED = 3;

/* These constants are used to set the kind of the node */
public static final int CHANCE = 0;
public static final int DECISION = 1;
public static final int UTILITY = 2;  
public static final int SUPER_VALUE = 3;
public static final int OBSERVED = 4;

/**
 * An array of strings that contains the names of the different types
 * of nodes. The positions in this array are the same that the integers
 * assigned to instance variable <code>typeOfVariable</code>.
 */

public static final String typeNames[]={"continuous","finite-states","infinite-discrete","mixed"};

/**
 * Contains the size of array <code>typeNames</code> (the number of types).
 */
static final int totalTypes = 4;

/**
 * An array of strings that contains the possible kinds of nodes.
 * The position of the strings in the array are the same that the
 * integers assigned to instance variable <code>kindOfNode</code>.
 */
public static final String kindNames[]= {"chance","decision","utility","super-value","observed"};

/**
 * This variable contains the number of possible kinds of nodes.
 */
static final int totalKinds = kindNames.length;

/**
 * Name of the node.
 */
private String name;

/**
 * A title.
 */
private String title;

/**
 * A free comment about the node.
 */
private String comment;

/**
 * The kind of node is used to know whether it is a chance, decision or
 * utility node.
 */
private int kindOfNode;

/**
 * To know whether it is a continuous, finite-states, 
 * infinite-discrete or mixed variable
 */
private int typeOfVariable;

/**
 * This variable contains the list of the links towards the children of
 * the node, i.e., a list with all the directed links of the form (this,x)
 * contained in the graph.
 */
protected LinkList children;

/**
 * This variable contains the list of the links comming from the parents of
 * the node, i.e., a list with all the directed links of the form (x, this)
 * present in the graph.
 */
protected LinkList parents;

/**
 * This variable contains the list of the links that contains the siblings of
 * the node, i.e., a list with all the undirected links of the form (this,x)
 * or (x, this) for every node x in the graph
 */
protected LinkList siblings;

/**
 * Property used to know whether the node has already been visited while
 * doing a traversal of the graph. By default, is <code>false</code>.
 */
private boolean visited = false;


/**
 * Property used to do macroexplanation. By default, is <code>false</code>.
 */
private boolean marked = false;
/**
 * Property used to know whether the node has been observed.
 * By default, is <code>false</code>.
 */
private boolean observed = false;
 
/**
 * Property used to determine whether a node will be displayed or not.
 * The graphical interface allows to represent only those nodes whose
 * relevance is higher than a given value.
 */
private double relevance = 7.0;

/** 
 * Property used to know the rol of the Node in the network. It is useful for doing explanation
 */
private String purpose = "";


private Hashtable propertyList;

/* GRAPHICAL PROPERTIES */

/**
 * X-position of the node in the graphical representation.
 */
private int posX = 0;

/**
 * Y-position of the node in the graphical representation.
 */
private int posY = 0;
   
/**
 * Contains the value of the higher axis of the graphical
 * representation of the node.
 */
private int higherAxis = 0;
   
/**
 * Contains the value of the lower axis of the graphical
 * representation of the node.
 */
private int lowerAxis = 0;


/**
 * String describing the font metrics attached to the node.
 */
private String nodeFont;
   
/** 
 * Graphic property used to know whether the node is selected
 * when the graphic mode is used.
 */
private boolean selected = false;  

/**
 * Determines whether the node is expanded or not (used in the inference
 * mode in the graphical interface).
 */
private boolean expanded = false;

/**
 * Used in EditVariableDialog to show full or independent parameters.
 */

private boolean showIndependent = false;

/**
 * Creates an instance of <code>Node</code>. By default is a chance node 
 * and it has finite states.
 */

public Node() {
  
  kindOfNode = CHANCE;      // default :  chance
  typeOfVariable = FINITE_STATES;       // default :  finite-states
  title = new String();
  comment = new String();
  name = new String();
  children = new LinkList();
  parents = new LinkList();
  siblings = new LinkList();  
  propertyList = new Hashtable();       
}


/**
 * Creates a new node and sets the <code>nodeFont</code> field.
 * @param fm a <code>String</code> describing the font metrics.
 */

public Node (String fm) {
  this();
  nodeFont = new String(fm);   
}


/**
 * Sets the <code>name</code> field.
 * @param s the name of the node.
 */

public void setName(String s) { 
  
  name = new String(s);
}

    
/**
 * Sets the <code>title</code> field.
 * @param s the title of the node.
 */

public void setTitle(String s) {
  
  title = new String(s);
}


/**
 * Sets the <code>comment</code> field.
 * @param s a comment about the node.
 */

public void setComment(String s) {
  
  comment = new String(s);
}

/**
 * Sets the showIndependent property. To be accessed from EditVariableDialog
 */

public void setIndependentParams(boolean indParam) {
 
  showIndependent = indParam;
}

/**
 * Sets the kind of the node.
 * @param i a int value representing the new kind.
 */

public void setKindOfNode(int i) {
  
  kindOfNode = i;
}

      
/**
 * Sets the kind of the node to a given string. This method is used
 * by <code>BayesNetParse</code> class.
 * @param s a <code>String</code> with the new kind.
 */

public void setKindOfNode(String s) {
  
  int i;
  
  // Looks for the number corresponding to s
  
  for (i=0 ; i<totalKinds ; i++) {
    if (s.equals(kindNames[i])) {
      kindOfNode = i;
      return;
    }
  }
} 


/**
 * Sets the type of variable to the given parameter.
 * @param i an integer value correponding to the new type.
 */

public void setTypeOfVariable(int i) {
  
  typeOfVariable = i;
}


/**
 * Sets the type of variable to s. This method is mainly used by
 * <code>BayesNetParse</code> class.
 * @param s a <code>String</code> with the new type.
 */

public void setTypeOfVariable(String s) {
  
  int i;
  
  for (i=0 ; i<totalTypes ; i++) {
    if (s.equals(typeNames[i])) {
      typeOfVariable = i;
      return;
    }
  }
} 



 /**
 * Sets Children of a Node
 * @param lch is the list of Children
 */
   public void setChildren(LinkList lch){
       children=lch;
   }


/**
 * Sets Parents of a Node
 * @param lp is the list of Parents
 */
   public void setParents(LinkList lp){
       parents=lp;
   }


/**
 * Sets Siblings of a Node
 * @param ls is the list of Siblings
 */
   public void setSiblings(LinkList ls){
       siblings=ls;
   }


/**
 * Marks the node as visited or not visited.
 * @param b <code>true</code> for visited, <code>false</code> for
 * not visited.
 */

public void setVisited(boolean b) {
  
  visited = b;
}

   
/**
 * Sets the node as marked or not marked.
 * @param b <code>true</code> for marked, <code>false</code> for
 * not marked.
 */

public void setMarked(boolean b) {
  
  marked = b;
}
   

/**
 * Marks the node as observed or not observed.
 * @param b <code>true</code> for observed, <code>false</code> for
 * not observed.
 */

public void setObserved(boolean b) {

  observed = b;
}


/**
 * Sets the relevance index.
 * @param value the relevance index.
 */

public void setRelevance (double value) {

  relevance = value;
}


/**
 * Sets the purpose of the Node.
 * @param value the name of the purpose.
 */

public void setPurpose(String value) {

  purpose= value;
}

/* Graphics methods */


/**
 * Sets the x position.
 * @param x the new position.
 */

public void setPosX(int x) {     
  
  posX = x;
}


/**
 * Sets the y position.
 * @param y the new position.
 */

public void setPosY(int y) {     

  posY = y;
}


/**
 * Sets the higher axis.
 * @param h the new value.
 */

public void setHigherAxis (int h) {

  higherAxis = h;
}


/**
 * Sets the lower axis.
 * @param l the new value.
 */

public void setLowerAxis (int l) {
  
  lowerAxis = l;
}
 

/**
 * Sets the font metrics.
 * @param fm the new value.
 */

public void setFont(String fm) {
  
  nodeFont = new String(fm);
}



/**
 * Sets the lower and higher axis.
 * @param l the new lower axis value.
 * @param h the new higher axis value.
 */

public void setAxis (int l, int h) {
  setLowerAxis (l);
  setHigherAxis (h);
}


/**
 *
 */

/*public void setAxis (String nodeString) {

  int namePixels = nodeFont.stringWidth(nodeString);
  
  lowerAxis = 30;
  if (namePixels < 12) 
    higherAxis = 30;
  else
    higherAxis = namePixels+24;  
}*/


/**
 *
 */

/*public void setAxis (boolean byTitle) { 
  
  String name = getNodeString(byTitle);
  setAxis(name);
} */  


/**
 * Marks the node as selected or not selected.
 * @param b <code>true</code> for selected, <code>false</code> for
 * not selected.
 */

public void setSelected (boolean b) {
  
  selected = b;
}   


/**
 * Marks the node as expanded or not expanded.
 * @param b <code>true</code> for expanded, <code>false</code> for
 * not expanded.
 */

public void setExpanded (boolean b) {
  
  expanded = b;
}   


/**
 * Gets the name of the node.
 * @return the name.
 */

public String getName() {
  
  return name;
}


/**
 * Gets the title of the node.
 * @return the title.
 */

public String getTitle() {
  
  return title;
}


/**
 * Gets the comment about the node.
 * @return the comment.
 */

public String getComment() {
  
  return comment;
}

/**
 * Gets the showIndependent property. To be used in EditVariableDialog
 */

public boolean toShowIndependent() {
 
  return showIndependent;
}

/**
 * Gets the kind of node.
 * @return the kind of node (see the kinds of node defined at the
 * beginning of this file).
 */

public int getKindOfNode() {
  
  return kindOfNode;
}


/**
 * Gets the kind of this node as a <code>String</code>.
 * @returns a <code>String</code> that contains the kind of the node.
 */

public String getKind () {
  
  return kindNames[kindOfNode];         
}


/**
 * Gets the type of variable attached to the node.
 * @return the type of variable (see the types defined at the
 * beginning of this file).
 */

public int getTypeOfVariable() {
  
  return typeOfVariable;
}


/**
 * Gets the type of variable attached to the node.
 * @returns s <code>String</code> that contains the type of the variable.
 */

public String getType () {
  
  return typeNames[typeOfVariable];
}


/**
 * Gets the links starting from this node.
 * @return the list of links starting from this node.
 */

public LinkList getChildren() {
  
  return children;
}


/**
 * Gets the links arriving at this node.
 * @return the list of links arriving at this node.
 */

public LinkList getParents() {
  
  return parents;
}


/**
 * Gets the relevance index.
 * @return the relevance index.
 */

public double getRelevance() {
  
  return relevance;
}

/**
 * Gets the function of the node.
 * @return the identifier ot the node's function.
 */

public String getPurpose() {
  
  return purpose;
}

/**
 * Puts a new property in the list of properties
 * @param nameProperty the name of the property
 * @param valueProperty the value for the property
 */
public void putProperty(String nameProperty,String valueProperty){
  propertyList.put(nameProperty,valueProperty);
}

/**
 * Gets the parents of this node.
 * @return a list with the parents of this node.
 */

public NodeList getParentNodes() {
  
  int i;
  Link link;
  LinkList listOfLinks;
  NodeList parentNodes;
 
  parentNodes = new NodeList();
  listOfLinks = getParents();
  
  for (i=0 ; i < listOfLinks.size() ; i++) {
    link = listOfLinks.elementAt(i);
    parentNodes.insertNode(link.getTail());
  }
  return parentNodes;
}

/**
 * Check if a node is parent of the node passed as argument
 * @node to test as child of this node
 * @return boolean value: the result of the test
 */
public boolean isParentOf(Node node){
   NodeList childs=getChildrenNodes();

   // Check if node is contained in parents
   if (childs.getId(node.getName()) != -1)
      return true;
   else
      return false;
}


/**
 * Obtains all the ancestors of this node. This is a recursive method.
 * @param allParents a <code>NodeList</code>, initially empty, where the
 * ancestors will be stored.
 */

public void getAllParentNodes(NodeList allParents) {
  
  int i, j;
  Link link;
  LinkList listOfLinks;
  
  listOfLinks = getParents();
 
  // Direct parents
 
  for (i=0 ; i < listOfLinks.size() ; i++) {
    link = listOfLinks.elementAt(i);
    if (allParents.getId(link.getTail()) == -1) {
      allParents.insertNode(link.getTail());
      (link.getTail()).getAllParentNodes(allParents);
    }
  }
}


/**
 * Gets the children of this node.
 * @return a list with the children of this node.
 */

public NodeList getChildrenNodes() {
  
  int i;
  Link link;
  LinkList listOfLinks;
  NodeList childrenNodes;
  
  childrenNodes = new NodeList();
  listOfLinks = getChildren();
  
  for (i=0 ; i < listOfLinks.size() ; i++) {
    link = listOfLinks.elementAt(i);
    childrenNodes.insertNode(link.getHead());
  }
  
  return childrenNodes;
}

/**
 * Gives the number of neighbours that a node has
 */

public int getNumNeighbours() {
    return (children.size() + parents.size() + siblings.size());
    
}

/**
 * adds Node n as a neighbour of this node (we use siblings)
 * necessary to triangulate in the class GTriangulation
 * @param n is the <code>Node</code> to add as a neighbour
 */
public void addNeighbour(Node n) {
    Link newLink = new Link(this,n,"fill-in");
    newLink.setDirected(false);
    siblings.insertLink(newLink);
    ((LinkList)n.getSiblings()).insertLink(newLink);
    
}

/**
 * Gives the neighbour ith of this node 
 * @param pos gives the position of the neighbour we are 
 * looking for
 */
public Node getNeighbourAtOld(int pos) {
    int c = children.size();
    int p = parents.size();
    int s = siblings.size();
    /* This will make a "route" along the possible
     * types of neighbours in this order: first
     * children, secind parents and finally siblings
     */
    if (pos < c)
        return getChildrenNodes().elementAt(pos);
    else if (pos < c+p)
        return getParentNodes().elementAt(pos-c);
    else return getSiblingsNodes().elementAt(pos-c-p);
        
}


/**
 * Gives the neighbour ith of this node 
 * @param pos gives the position of the neighbour we are 
 * looking for
 * New faster implementation (I hope!)
 */

public Node getNeighbourAt(int pos) {
    int c = children.size();
    int p = parents.size();
    int s = siblings.size();
    Link link;

    /* This will make a "route" along the possible
     * types of neighbours in this order: first
     * children, secind parents and finally siblings
     */

    if (pos < c)
        return children.elementAt(pos).getHead();
    else if (pos < c+p)
        return parents.elementAt(pos-c).getTail();
    else{
        link = siblings.elementAt(pos-c-p);
        if (this.compareTo(link.getHead())==0) return link.getTail();
        else return link.getHead();
    }      
}



/**
 * Says whether the node is neighbour of the current one
 * @param node is checked as a neighbour of this node
 * @return true if both nodes are neighbours, false otherwise
 */

public boolean isNeighbourOld(Node node) {
    int pos;
    pos = node.indexOf(getChildrenNodes().getNodes());
    
    if (node.indexOf(getChildrenNodes().getNodes())!=-1)
      return true;
       
    else if (node.indexOf(getParentNodes().getNodes())!=-1)
            return true;

         else if (node.indexOf(getSiblingsNodes().getNodes())!=-1)
                return true;
   return false;
}


/**
 * Says whether the node is neighbour of the current one
 *
 * A faster version (I hope!) of the previous method
 *
 * @param node is checked as a neighbour of this node
 * @return true if both nodes are neighbours, false otherwise
 */

public boolean isNeighbour(Node node) {
    
    if (children.getID(this.getName(),node.getName()) != -1) return true;     
    else if (parents.getID(node.getName(),this.getName()) != -1) return true;
    else if (siblings.getID(this.getName(),node.getName()) != -1) return true;

    return false;
}





/**
 * Determine if the node has only decision childs
 * @return <code>true</code> if that is true, <code>false</code> otherwise
 */

public boolean onlyHasDecisionChilds() {
   NodeList childs;
   Node node;
   int i;

    // Get the list of childs

    childs=getChildrenNodes();

    // Consider all of them

    for(i=0; i < childs.size(); i++) {

        node=childs.elementAt(i);

        if (node.getKindOfNode() != node.DECISION)
            return(false);
    }

    // Return true

    return(true);
}

/**
 * Calculates a hash code for this Node. The code is
 * computed as the sum of the hash codes of each of the variables
 * and their values.
 *
 * @return the hash code.
 */

public int hashCode() 
{
  return (this.name.hashCode()+1000);  
}

/**
 * Determines whether the node is a root (does not have parents)
 * or not (has parents).
 * @return <code>true</code> if the node has parents, <code>false</code>
 * otherwise.
 */

public boolean hasParentNodes() {
  
  LinkList listOfLinks;
  
  listOfLinks = getParents();
  if (listOfLinks.size() != 0)
    return true;
  else
    return false;
}

/**
 * Determines whether the node has got some children
 * @return <code>true</code> if the node has children, <code>false</code>
 * otherwise.
 */
public boolean hasChildrenNodes(){
    return (getChildren().size()>0);
}

/**
 * Gets the list of links to the siblings of this node.
 * @return the list of links to the siblings.
 */

public LinkList getSiblings() {
  
  return siblings;
}

/**
 * Gets the siblings of this node.
 * @return a list with the siblings of this node.
 */

public NodeList getSiblingsNodes() {
  
  int i;
  Link link;
  LinkList listOfLinks;
  NodeList siblingsNodes;
  Node head;
  
  siblingsNodes = new NodeList();
  listOfLinks = getSiblings();
  
  for (i=0 ; i < listOfLinks.size() ; i++) {
    link = listOfLinks.elementAt(i);
    head = link.getHead();
    if (this.compareTo(head)==0)
        siblingsNodes.insertNode(link.getTail());
    else siblingsNodes.insertNode(head);
  }
  
  return siblingsNodes;
}



/**
 * Determines whether the node has been visited or not.
 * @return <code>true</code> if visited, <code>false</code> otherwise.
 */

public boolean getVisited() {
  
  return visited;
}

/**
 * Determines whether the node has been marked or not.
 * @return <code>true</code> if visited, <code>false</code> otherwise.
 */

public boolean getMarked() {
  
  return marked;
}


/**
 * Determines whether the node has been observed or not.
 * @return <code>true</code> if observed, <code>false</code> otherwise.
 */

public boolean getObserved() {
  
  return observed;
}


/**
 * Gets the x position.
 * @return the x position.
 */

public int getPosX() {
  
  return posX;
}


/**
 * Gets the y position.
 * @return the y position.
 */

public int getPosY() {
  
  return posY;
}


/**
 * Gets the higher axis.
 * @return the higher axis.
 */

public int getHigherAxis() {
  
  return higherAxis;
}


/**
 * Gets the lower axis.
 * @return the lower axis.
 */

public int getLowerAxis() {
  
  return lowerAxis;
}


/**
 *
 */

/*public int getWidth() {
  
  NetworkFrame n = (NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame();
  int width = 0;
  
  if (n.getMode() == NetworkFrame.INFERENCE_ACTIVE && expanded)
    switch (kindOfNode) {
      case 0: width = CHANCE_NODE_EXPANDED_WIDTH; break;
      case 1: width = DECISION_NODE_EXPANDED_WIDTH; break;
      case 2: width = UTILITY_NODE_EXPANDED_WIDTH; break;
    }
  else
    width = higherAxis;
  
  return width;
}*/


/**
 *
 */

/*public int getHeight() {
  
  NetworkFrame n = (NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame();
  int height = 0;
  
  if (n.getMode() == NetworkFrame.INFERENCE_ACTIVE  && expanded)
    switch (kindOfNode) {
      case 0: height = CHANCE_NODE_EXPANDED_HEIGHT; break;
      case 1: height = DECISION_NODE_EXPANDED_HEIGHT; break;
      case 2: height = UTILITY_NODE_EXPANDED_HEIGHT; break;
    }
  else
    height = lowerAxis;      
  
  return height;
}*/


/**
 * Gets the font metrics.
 * @return the font metrics.
 */

public String getFont() {
  
  return nodeFont;
}


/**
 * This function is used by method <code> repaint()</code> for drawing
 * the name/title of the nodes in the editor. To do this, it checks
 * the argument variable.
 * @param byTitle <code>true</code> if the node will display the title,
 * <code>false</code> if instead the name will be displayed.
 */

public String getNodeString (boolean byTitle) {
  
  if (byTitle)
    if (getTitle().equals(""))
      return getName();
    else
      return getTitle();
  else
    return getName();
}


/* Graphics methods */


/**
 * Determines whether the node is selected or not.
 * @return <code>true</code> if the node is selected,
 * <code>false</code> otherwise.
 */

public boolean isSelected() {
  
  return selected;
}


/**
 * Determines whether the node is expanded or not.
 * @return <code>true</code> if the node is expanded,
 * <code>false</code> otherwise.
 */

public boolean getExpanded() {
  
  return expanded;
}

/**
 * Returns the value for the property nameProperty
 * @returns the value for the property nameProperty
 */
public String getProperty(String nameProperty){
  return (String)propertyList.get(nameProperty);
}



/**
 * Locates this node in a given vector of nodes.
 * @param v a <code>Vector</code> of <code>FiniteStates</code> nodes.
 * @return the position of this node in <code>v</code>, or -1 if the
 * node is not in the <code>v</code>.
 */

public int indexOf(Vector v) {
  
  int i;
  Node aux;
  
  for (i=0 ; i<v.size() ; i++) {
    aux = (Node)v.elementAt(i);
    if (compareTo(aux) == 0)
      return i;
  }
  
  return (-1);
}


/**
 * Saves the node using the text output stream given as parameter.
 * @param p a <code>PrintWriter</code> where the node will be printed.
 */

public void save(PrintWriter p) {
  
  if ((getTitle()!=null) && (!getTitle().equals(""))) 
    p.print("title = \""+ getTitle()+"\";\n");
  
  if ((getComment()!=null) && (!getComment().equals("")))
    p.print("comment = \""+ getComment()+"\";\n");
  
  p.print("kind-of-node = " +  getKind() + ";\n");
  p.print("type-of-variable = " + getType() + ";\n");
  
  if (posX != 0)
    p.print("pos_x ="+posX+";\n");
  
  if (posY != 0)
    p.print("pos_y ="+posY+";\n");
  
  p.print("relevance = " + getRelevance() + ";\n");
  
  p.print("purpose = \"" +getPurpose()+"\";\n");
  String key;
  for(Enumeration pKeys=propertyList.keys();pKeys.hasMoreElements();){
    key=(String)pKeys.nextElement();
    p.print(key + "=" + propertyList.get(key) + ";\n");
  }
}


/**
 * Prints the node to the standard output.
 */

public void print() {

  if ((getName()!=null) && (!getName().equals(""))) 
    System.out.print("name = \""+ getName()+"\";\n");
  
  if ((getTitle()!=null) && (!getTitle().equals(""))) 
    System.out.print("title = \""+ getTitle()+"\";\n");
  
  if ((getComment()!=null) && (!getComment().equals("")))
    System.out.print("comment = \""+ getComment()+"\";\n");
  
  System.out.print("kind-of-node = " +  getKind() + ";\n");
  System.out.print("type-of-variable = " + getType() + ";\n");
  
  if (posX != 0)
    System.out.print("pos_x ="+posX+";\n");
  
  if (posY != 0)
    System.out.print("pos_y ="+posY+";\n");
  String key;
  for(Enumeration pKeys=propertyList.keys();pKeys.hasMoreElements();){
    key=(String)pKeys.nextElement();
    System.out.print(key + "=" + propertyList.get(key) + ";\n");
  }
}  

/**
 * Prints the node to the standard output with n tabs.
 */

public void print(int n) {
  int i;
  for (i=0; i<n; i++)
      System.out.print("\t");

  if ((getName()!=null) && (!getName().equals(""))) 
    System.out.print("name = \""+ getName()+"\";\n");
  
  for (i=0; i<n; i++)
      System.out.print("\t");

  if ((getTitle()!=null) && (!getTitle().equals(""))) 
    System.out.print("title = \""+ getTitle()+"\";\n");
  
  for (i=0; i<n; i++)
      System.out.print("\t");

  if ((getComment()!=null) && (!getComment().equals("")))
    System.out.print("comment = \""+ getComment()+"\";\n");
  
  for (i=0; i<n; i++)
      System.out.print("\t");

  System.out.print("kind-of-node = " +  getKind() + ";\n");
  for (i=0; i<n; i++)
      System.out.print("\t");

  System.out.print("type-of-variable = " + getType() + ";\n");
  for (i=0; i<n; i++)
      System.out.print("\t");
  
  if (posX != 0){
      for (i=0; i<n; i++)
          System.out.print("\t");
    System.out.print("pos_x ="+posX+";\n");
  }  
  if (posY != 0){
     for (i=0; i<n; i++)
      System.out.print("\t");
      System.out.print("pos_y ="+posY+";\n");
  }
  String key;
  for(Enumeration pKeys=propertyList.keys();pKeys.hasMoreElements();){
    key=(String)pKeys.nextElement();
    for (i=0; i<n; i++)
      System.out.print("\t");

    System.out.print(key + "=" + propertyList.get(key) + ";\n");
  }
}  

/**
 * Compares the name of the node with the name of the argument node.
 * @param n the node to compare with this.
 * @return 0 if the name of the argument is the same as the name
 * of this; an int < 0 if it is lower and >0 if it is greater.
 */

public int compareTo(Node n) {
  
  return name.compareTo(n.name);
}


/**
 * Decides whether two nodes are equals, i.e. when their names are the same.
 * @param n the node to compare with this.
 * @return true if the title of the argument is the same as the title of
 * this one.
 */

public boolean equals(Object n) {
  
  if ((n!=null) && (n instanceof Node)) {
    Node no = (Node)n;
   /* if (!getTitle().equals(""))
        if (this.getTitle().equals(no.getTitle()))
            return true;
        else return false;
    else */if (this.getName().equals(no.getName()))
            return true;
         else return false;
  }
  else
    return false;
}


/**
 * Generates a <code>String</code> with some information about the node:
 * its name, parents, children and siblings.
 * @return a <code>String</code> that contains the information about the node.
 */

public String toString() {
  
  StringBuffer string = new StringBuffer();
  
  string.append(getTitle());
  
  /*string.append(getName()+"\n");
  if (children.size() == 0)
    string.append("The node "+getName()+" has no children"+"\n");
  else {
    string.append("The children of the node "+getName()+" are: "+"\n");
    string.append(children.toString());
  }
  if (parents.size() == 0)
    string.append("The node "+getName()+" has no parents"+"\n");
  else {
    string.append("The parents of the node "+getName()+" are: "+"\n");
    string.append(parents.toString());
  }
  if (siblings.size() == 0)
    string.append("The node "+getName()+" has no siblings");
  else {
    string.append("The siblings of the node "+getName()+" are: ");
    string.append(siblings.toString());
  } */
  return (string.toString());
}


/**
 * This method creates a new node equal to this but the lists of links
 * of its parents, children and siblings are empty.
 * @return the new node.
 */

public Node copy() {
  try{
    Node n = (Node) super.clone();
    n.parents = new LinkList();
    n.children = new LinkList();
    n.siblings = new LinkList();

    return n;
  }
  catch (CloneNotSupportedException e) {
    System.out.println("Node "+getName()+" can't be cloned");
    return null;
  }
}

     
/** 
 * To see whether the node has decisions as children.
 * @return <code>true</code> if the node has decisions as children,
 * <code>false</code> if not.
 */

public boolean hasDecisionChild() {
  
  NodeList childrenNodes;
  Node node;
  boolean has;
  int i;
  
  childrenNodes = getChildrenNodes();
  // En primer lugar buscamos entre los sucesores directos
 
  for (i=0 ; i < childrenNodes.size() ; i++) {
    node = childrenNodes.elementAt(i);
    if (node.getKindOfNode() == node.DECISION) {
      return true;
    }
  }

  // En caso de no haber encontrado aqui, continuamos la busqueda
  // a partir de ellos

  for(i=0; i < childrenNodes.size(); i++) {
     // Trabajamos sobre los hijos

     node = childrenNodes.elementAt(i);
     has=node.hasDecisionChild();

     // If any of the childrens has, consider it is OK

     if (has == true)
       return true;
  }
  
  return false;
}

/** 
 * To see whether the node has decisions as direct child.
 * @return <code>true</code> if the node has decisions as children,
 * <code>false</code> if not.
 */

public boolean hasDirectDecisionChild() {
  
  NodeList childrenNodes;
  Node node;
  int i;
  
  childrenNodes = getChildrenNodes();
  
  for (i=0 ; i < childrenNodes.size() ; i++) {
    node = childrenNodes.elementAt(i);
    if (node.getKindOfNode() == node.DECISION)
      return true;
  }
  
  return false;
}

/** 
 * To see whether the node has decisions as parents.
 * @return <code>true</code> if the node has decisions as parent,
 * <code>false</code> if not.
 */

public boolean hasDecisionParent() {
 
  boolean condition; 
  NodeList parentNodes;
  Node node;
  int i;
  
  parentNodes = getParentNodes();

  // First, consider direct relations
  
  for (i=0 ; i < parentNodes.size() ; i++) {
    node = parentNodes.elementAt(i);
    if (node.getKindOfNode() == node.DECISION) {
      return true;
    }
  }

  // We have to study indirect ancestors

  for(i=0; i < parentNodes.size(); i++) {
    node = parentNodes.elementAt(i);
    condition=node.hasDecisionParent();
    if (condition == true) {
      return(true);
    }
  }

  // If we are here, the node has not a decision as
  // parent
  
  return false;
}


/**
 * To see whether the node is parent of a Utility Node.
 * @return <code>true</code> if the node is parent of a Utility Node,
 * <code>false</code> if not.
 */

public boolean isUtilityParent() {
  
  NodeList childrenNodes;
  int i;
  Node node;
  
  childrenNodes = getChildrenNodes();
  
  for (i=0 ; i < childrenNodes.size() ; i ++) {
    node = childrenNodes.elementAt(i);
    if (node.getKindOfNode() == node.UTILITY) {
      return true;
    }
    else {
      if (node.isUtilityParent() == true)
	      return true;
    }
  }
  return false;
}

/**
 * Method to test if the node has direct or undirect
 * way to some value node
 */

public boolean withPathToValueNode(NodeList considered) {
  NodeList childrenNodes;
  NodeList parentNodes;
  Node node;
  boolean path;
  int i;

  // Check the same node, looking its childrens

  path=false;
  childrenNodes=getChildrenNodes();

  for(i=0; i < childrenNodes.size(); i++){
    node=childrenNodes.elementAt(i);

    if (node.getKindOfNode() == Node.UTILITY){
      return true;
    }
  }

  // For each one of the childrens, look if has path to the
  // value node

  for(i=0; i < childrenNodes.size(); i++){
      node=childrenNodes.elementAt(i);

      if (node.getKindOfNode() == Node.CHANCE){
        // Analyze the node, if it is not analyzed yet

        if (considered.getId(node) == -1){
          considered.insertNode(node);
          path=node.withPathToValueNode(considered);

          // If path == true, return directly

          if (path == true)
            return path;
         }
      }
  }

  // Check the parents

  parentNodes=getParentNodes();

  // For each one of them, look for paths to value nodes

  for(i=0; i < parentNodes.size(); i++){
      node=parentNodes.elementAt(i);

      if (node.getKindOfNode() == Node.CHANCE){

        if (considered.getId(node) == -1){
          considered.insertNode(node);
          path=node.withPathToValueNode(considered);

          if (path == true)
            return path;
        }
      }
  }

  // Return path

  return path;
}


/**
 * To return the minimal distance between two nodes
 * @param destiny the node to consider.
 * @return <code>distance</code> Distance betwwen the nodes 
 *         -1 if there is not a path between them
 */

public int minimalDistanceBetweenNodes(Node destiny) {
  Vector allChildrens;
  NodeList nodes;
  int i;

    // Retrieve the list of childrens

    allChildrens=getAllChildrens();

    // Now we have to find the minimal position where destiny
    // is inserted

    for(i=0; i < allChildrens.size(); i++) {
       // Get the nodes at distance i+1

       nodes=(NodeList)allChildrens.elementAt(i);

       // Look if destiny is in nodes

       if (nodes.getId(destiny) != -1) {
           // The destination node is at distance i+1

           return(i+1);
       }
    }

   // In this situatiuon there is not a path between this
   // and destiny and return -1

   return(-1);
}

/**
 * To return the maximal distance between two nodes
 * @param destiny the node to consider.
 * @return <code>distance</code> Distance betwwen the nodes 
 */

public int maximalDistanceBetweenNodes(Node destiny) {
  Vector allChildrens;
  NodeList nodes;
  int maxDistance=-1;
  int i;

    // Retrieve the list of childrens

    allChildrens=getAllChildrens();

    // Now we have to find the maximal position where destiny
    // is inserted

    for(i=0; i < allChildrens.size(); i++) {
       // Get the nodes at distance i+1

       nodes=(NodeList)allChildrens.elementAt(i);

       // Look if destiny is in nodes

       if (nodes.getId(destiny) != -1) {
           // The destination node is at distance i+1

           maxDistance=i+1;
       }
    }

    // Return maxDistance

   return(maxDistance);
}
/**
 * Inserted By Alberto Ruiz 2008
 * New Method to Obtain all the previous nodes, the difference is
 * the type value that return in this case an "ArrayList" of "String"
 * This method is used in one-way sensitivity analysis 
 */

public ArrayList<String> getAllParents2(){
	Vector visited= new Vector();
	ArrayList<String>visited2= new ArrayList<String>(); 
	NodeList parentsNodes;
	NodeList previousSet,completeSet;
	Node node;
	int prev,i,cont=0;
	boolean inserted=true;
	
	//Comezamos con los antecesores directos
	
	parentsNodes=getParentNodes();
	
	//Todos estos nodos son añadidos en la posicion 0
	   visited.addElement((Object)parentsNodes);
		for(int g=0;g<parentsNodes.size();g++){
			visited2.add(g, parentsNodes.elementAt(g).getNodeString(true));
			cont++;
		}
	 // Posicion donde recuperamos el ultimo visitado

       prev=0;
		

  // Repetimos hasta que ningun nodo sea añadido

  while(inserted) {

    // Consideramos los ultimos nodos visitados
  	
      previousSet=(NodeList)visited.elementAt(prev);
  	

    if (previousSet.size() != 0) {

        completeSet=new NodeList();

        // Para cada uno de ellos, obtener sus padres

        for(i=0; i < previousSet.size(); i++) {
           node=previousSet.elementAt(i);

           parentsNodes=node.getParentNodes();

           	for(int u=0;u<parentsNodes.size();u++){
           		visited2.add(cont, parentsNodes.elementAt(u).getNodeString(true));
      			cont++;
           	}
           // Este conjunto de nodos es almacenado en CompleteSet

           completeSet.merge(parentsNodes);
        }

        // Incrementar prev

        prev++;

        // Añadir un nuevo elemento a visitar

        visited.addElement((Object)completeSet);
    }
    else
     inserted=false;
  }

  // Return the vector

  return(visited2);
}	

//End Alberto Ruiz 2008 PFC

/**
 * Inserted By Alberto Ruiz 2008
 * New Method to Obtain all the previous nodes, the difference is
 * the type value that return in this case an "ArrayList" of "String"
 * This method is used in one-way sensitivity analysis 
 */
public ArrayList<String> getAllChildren2(){
	Vector visited= new Vector();
	ArrayList<String>visited2= new ArrayList<String>(); 
	NodeList childrenNodes;
	NodeList previousSet,completeSet;
	Node node;
	int prev,i,cont=0;
	boolean inserted=true;
	
	//Comezamos con los antecesores directos
	
	childrenNodes=getChildrenNodes();
	
	//Todos estos nodos son añadidos en la posicion 0
	   visited.addElement((Object)childrenNodes);
		for(int g=0;g<childrenNodes.size();g++){
			visited2.add(g, childrenNodes.elementAt(g).getNodeString(true));
			cont++;
		}
	 // Posicion donde recuperamos el ultimo visitado

       prev=0;
		

  // Repetimos hasta que ningun nodo sea añadido

  while(inserted) {

    // Consideramos los ultimos nodos visitados
  	
      previousSet=(NodeList)visited.elementAt(prev);
  	

    if (previousSet.size() != 0) {

        completeSet=new NodeList();

        // Para cada uno de ellos, obtener sus padres

        for(i=0; i < previousSet.size(); i++) {
           node=previousSet.elementAt(i);

           childrenNodes=node.getChildrenNodes();

           	for(int u=0;u<childrenNodes.size();u++){
           		visited2.add(cont, childrenNodes.elementAt(u).getNodeString(true));
      			cont++;
           	}
           // Este conjunto de nodos es almacenado en CompleteSet

           completeSet.merge(childrenNodes);
        }

        // Incrementar prev

        prev++;

        // Añadir un nuevo elemento a visitar

        visited.addElement((Object)completeSet);
    }
    else
     inserted=false;
  }

  // Return the vector

  return(visited2);
}	
/**
 * To get all paths from a node, with information
 * about the distances
 * @return a Vector of NodeList, with the nodes
 *         at distance 1, 2, etc....
 */

public Vector getAllChildrens() {
  Vector visited=new Vector();
  NodeList childrenNodes;
  NodeList previousSet, completeSet;
  Node node;
  int prev, i;
  boolean inserted=true;

    // We begin with direct sucessors

    childrenNodes=getChildrenNodes();

    // All of these nodes are added in position 0

    visited.addElement((Object)childrenNodes);

    // Position whre retrive the last visited

    prev=0;

    // Repeat until no nodes are added

    while(inserted) {

      // Consider the nodes visited in last iteration

      previousSet=(NodeList)visited.elementAt(prev);

      if (previousSet.size() != 0) {

          completeSet=new NodeList();

          // For each one of them, get their childrens

          for(i=0; i < previousSet.size(); i++) {
             node=previousSet.elementAt(i);

             childrenNodes=node.getChildrenNodes();

             // This set of nodes is stored in completeSet

             completeSet.merge(childrenNodes);
          }

          // Increment prev

          prev++;

          // Add a new element to visited

          visited.addElement((Object)completeSet);
      }
      else
       inserted=false;
    }

    // Return the vector

    return(visited);
}

/**
 * To detect if there is more than a direct
 * path between two nodes. This method suposses
 * dest is direct sucessor of this
 * @param dest.
 * @return <code>boolean</code> Result of the function 
 */

public boolean moreThanAPath(Node dest) {
 NodeList childrenNodes;
 Node node;
 int i;
 boolean reachable;

  // Consider the sucessors, except dest

  childrenNodes = getChildrenNodes();

  for (i=0 ; i < childrenNodes.size() ; i++) {
    node = childrenNodes.elementAt(i);
    if (node.getName() != dest.getName()) {
      reachable=node.isReachable(dest);
      if (reachable == true) {
          return(true);
      }
    }
  }
  return(false);
}


/**
 * To detect if we can reach a node from this node
 * @param origin.
 * @param dest.
 * @return <code>boolean</code> Result of the function 
 **/

public boolean isReachable(Node dest) {
 NodeList childrenNodes;
 Node node;
 int i;
 boolean reachable=false;

  childrenNodes = getChildrenNodes();
 
  // First at all, look at direct sucessors 
 
  for (i=0 ; i < childrenNodes.size() ; i++) {
    node = childrenNodes.elementAt(i);
    if (node.getName() == dest.getName()) {
        return(true);
    }
  }

  // Consider indirect sucessors

  for(i=0; i < childrenNodes.size(); i++) {
     node = childrenNodes.elementAt(i);

     if (node.getName() == dest.getName()) {
        reachable=true;
        break;
     }
     else {
       reachable=node.isReachable(dest);
       if (reachable == true)
           break;
     }
  }

  return(reachable);
}

/**
 * To detect if we can reach decisions from this node 
 * @return <code>NodeList</code> The decision node 
 **/

public NodeList decisionsReachable() {
 NodeList childrenNodes;
 NodeList decisions=new NodeList();
 Node node;
 int i;

  childrenNodes = getChildrenNodes();
 
  // First at all, look at direct sucessors 
 
  for (i=0 ; i < childrenNodes.size() ; i++) {
    node = childrenNodes.elementAt(i);
    if (node.getKindOfNode() == Node.DECISION) {
      if (decisions.getId(node.getName()) == -1)
        decisions.insertNode(node);
    }
  }

  // Consider indirect sucessors

  for(i=0; i < childrenNodes.size(); i++) {
     node = childrenNodes.elementAt(i);
     if (node.getKindOfNode() != Node.DECISION) 
       decisions.merge(node.decisionsReachable());    
  }

  return(decisions);
}

/**
 * Add a link <code>l</code> to the LinkList parents in this Node object.
 * 
 * @param l - the new parent link
 */
public void addParent(Link l){
	if(parents == null){
		parents = new LinkList();
	}
	if(l.getHead() != this){
		System.out.println("WARNING : tried to add link l to parents in class Node for which Node is not the head!");
	} else {
		parents.insertLink(l);
	}
}

/**
 * Add a link <code>l</code> to the LinkList children in this Node object.
 * 
 * @param l - the new parent link
 */
public void addChild(Link l){
	if(children == null){
		children = new LinkList();
	}
	if(l.getTail() != this){
		System.out.println("WARNING : tried to add link l to children in class Node for which Node is not the tail!");
	} else {
		children.insertLink(l);
	}
}

/**
 * Returns the value of an undef value for this Node
 */

abstract public double undefValue();

}  // End of class
