/* Link.java */

package elvira;

import java.io.*;
import java.awt.*;

/*import java.awt.geom.*;
import elvira.gui.EditorPanel;
import elvira.gui.Selection;*/


/**
 * This class implements the structure necessary to store all 
 * the information refered to a link in a graph.
 *
 * since 21/9/2000
 */
  
public class Link implements Serializable {
  
/* GRAPHICAL CONSTANTS */
/*private final double ARROW_SIZE = 6.0;
private final double ARROW_HALF_SIZE = 3.0;
private final int NODE_RADIUS = 13; 

private final static BasicStroke stroke = new BasicStroke(1.0f);
private final static BasicStroke wideStroke = new BasicStroke(4.0f);*/

/**
 * Contains the tail of the link.
 */
private Node tail;		

/**
 * Contains the head of the link.
 */
private Node head;		

/**
 * Contains the comment of the link.
 */
private String comment;		

/**
 * Contains the type of the link:
 * <ul>
 * <li> <code>true</code> - if the link is directed.
 * <li> <code>false</code> - if the link is not directed.
 * </ul>
 */
private boolean directed;	

// Graphical property

/**
 * <ul>
 * <li> <code>true</code> - if the link is selected.
 * <li> <code>false</code> - if the link is not selected.
 * </ul>
 */
private boolean selected;

private boolean UPDOWN=true;

private Color colorlink=Color.black;

private String kindofrelation="";

private double width=0.2;


/* ******* Constructors ******** */


/**
 * Constructor. Creates an empty link.
 */

public Link() {
  
  setComment("");
  //colorlink=ElviraPanel.ARC_COLOR;
}


/**
 * Constructor. Creates an empty link. Its class depends on
 * the parameter.
 * @param d <code>true</code> if it is directed and <code>false</code>
 * if it is undirected.
 */

public Link(boolean d) {
  
  setComment("");
  setDirected(d);
  //colorlink=ElviraPanel.ARC_COLOR;  
}


/**
 * Constructor. Creates a link between two nodes. By default  
 * the link is directed.
 * @param t tail node.
 * @param h head node.
 */

public Link (Node t, Node h) {
  
  setTail(t);
  setHead(h);
  setComment("");
  setDirected(true);
  //colorlink=ElviraPanel.ARC_COLOR;  
}


/**
 * Constructor. Creates a link between  two nodes. By default  
 * the link is directed.
 * @param t tail node.
 * @param h head node.
 * @param c a comment about the link.
 */

public Link (Node t, Node h, String c) {
  
  setTail(t);
  setHead(h);
  setComment(c);
  setDirected(true);
  //colorlink=ElviraPanel.ARC_COLOR;  
}


/**
 * Constructor. Creates a link between  two nodes.
 * @param t tail node.
 * @param h head node.
 * @param d <code>true</code> if it is directed and <code>false</code>
 * if it is undirected.
 */

public Link (Node t, Node h, boolean d) {
  
  setTail(t);
  setHead(h);
  setComment("");
  setDirected(d);
  //colorlink=ElviraPanel.ARC_COLOR;
}


/**
 * Constructor. Creates a link between  two nodes.
 * @param t tail node.
 * @param h head node.
 * @param c a comment about the link.
 * @param d <code>true</code> if it is directed and <code>false</code>
 * if it is undirected.
 */

public Link (Node t, Node h, String c, boolean d) {
  
  setTail(t);
  setHead(h);
  setComment(c);
  setDirected(d);
  //colorlink=ElviraPanel.ARC_COLOR;
}


/* ******** Access methods ********* */

/**
 * Used for accessing variable <code>head</code>.
 * @return a node containing the head of the link.
 */

public Node getHead() {
  
  return head;
}


/**
 * Used for accessing variable <code>tail</code>.
 * @return a node with the tail of the link.
 */

public Node getTail() {
  
  return tail;
}


/**
 * Used for accessing variable <code>comment</code>.
 * @return a string with the comment.
 */

public String getComment() {
  
  return comment;
}


/**
 * Gets the type of link.
 * @return <ul>
 * <li> <code>true</code> - if the link is directed.
 * <li> <code>false</code> - if the link is not directed.
 * </ul>
 */

public boolean getDirected() {
  
  return directed;
}


public boolean getUPDOWN(){
    return UPDOWN;
}

public void setUPDOWN(boolean ud){
    UPDOWN=ud;
}
/**
 * Decides whether the link is selected or not.
 * @return <ul>
 * <li> <code>true</code> - if the link is selected.
 * <li> <code>false</code> - if the link is not selected.
 * </ul>
 */

public boolean isSelected() {
  
  return selected;
}

public Color getColorLink(){
    return colorlink;
}

public String getKindofRelation(){
    return kindofrelation;
}


public double getWidth(){
	return width;
}

/* ******** Modifiers ******** */


/**
 * Sets the head of the link.
 * @param h a node with the head of the link.
 */

public void setHead(Node h) {
  
  head = h;
}


/**
 * Sets the tail of the link.
 * @param t a node with the tail of the link.
 */

public void setTail(Node t) {
  
  tail = t;
} 


/**
 * Sets the comment of the link.
 * @param s a string that contains a comment about the link.
 */

public void setComment(String s) {
  
  comment = new String(s);
}


/**
 * Marks the link as directed or not.
 * @param b <code>true</code> if the link will be directed,
 * <code>false</code> if not.
 */

public void setDirected(boolean b) {
  
  directed = b;
}
  

/**
 * Marks the link as selected or not.
 * @param b <code>true</code> if the link will be selected,
 * <code>false</code> if not.
 */
  
public void setSelected (boolean b) {
  
  selected = b;
}


public void setColorLink(Color c){
    colorlink=c;
}

public void setKindofRelation(String k){
    kindofrelation=k;
}

public void setWidth(double w){
	width=w;
}
/** 
 * Decides whether two links are equal, i.e, if the names of their respective
 * heads and tails are the same and both are directed or both are undirected.
 * @param l a link to compare with this.
 * @return <code>true</code> if link <code>l</code> is equal to this.
 */

public boolean equals(Object l) {
  
  Link lo;
  if ((l!=null) && (l instanceof Link)) {
    lo = (Link) l;
    if ((this.getHead().equals(lo.getHead())) && (this.getTail().equals(lo.getTail())))
      return (getDirected()==lo.getDirected());
    else
      return false;
  }
  else
    return false;
}


/**
 * Saves the link to a file.
 * @param p the file.
 */

public void save(PrintWriter p) {
  
  p.print("link ");
  p.print(tail.getName()+" "+head.getName());
  
  if (comment.equals("") && directed)
    p.print(";\n\n");
  else {
    p.print("{\n");    
    if (!directed) {
      p.print(" directed = false;\n");
    }
    
    if (!comment.equals("")) {
      p.print(" comment =\""+comment+"\";\n");
    }
    
    p.print("}\n\n");
  }
}           


/**
 * Generates a new link whose tail and head are equal to the tail and head
 * of this link, but not the same. Therefore a duplication of the tail
 * and head of this link must be done.
 * @return a duplicate of this link.
 */

public Link duplicate() {
  
  return new Link(getTail().copy(),getHead().copy(),getDirected());
}


/**
 * Generates a new link with the same tail and head than this has.
 * So, the tail and head are shared by both the original link (this)
 * and its copy.
 * @return a shallow copy of the link.
 */

public Link copy() {
  
  return new Link(getTail(),getHead());
}


/**
 * Creates a string with some information about the link: the names of
 * the tail and head.
 * @return a String with the names of the tail and head of the link.
 */

public String toString() {

  StringBuffer string = new StringBuffer();

  string.append("("+getTail().getName());
  if (directed)
    string.append("->");
  else
    string.append("--");
  string.append(getHead().getName()+")"+"\n");
  return (string.toString());
}


public boolean isARevelationArc() {
	// TODO Auto-generated method stub
	return false;
}


} // End of class

