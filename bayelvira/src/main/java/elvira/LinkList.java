/* LinkList.java */

package elvira;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import elvira.Link;
import java.io.*;
import java.util.Random;
import java.util.Date;

/**
 * This class implements the list of links of a graph and all the needful
 * methods for using it.
 *
 * @since 21/9/2000
 */

public class LinkList implements Serializable {

static final long serialVersionUID = -70098651046265100L;
//static final long serialVersionUID = 2784756110651313707L;

/**
 * A list with the links of the network.
 */
private Vector links;


/**
 * Constructor. Creates a vector of links.
 */

public LinkList() {

  links = new Vector();
}


/**
 * Gets the links in the list.
 * @return a <code>Vector</code> with the links.
 */

public Vector getLinks() {

  return links;
}


/**
 * Sets the links in the list.
 * @param l the new vector of links.
 */

public void setLinks(Vector l) {
  setLinks(l,true);
}


public Iterator iterator(){
  return links.iterator();
}

public void setLinks(Vector l, boolean sort) {
  links = l;
  this.sort();
}


/**
 * Adds a link at the end of the list.
 * @param l the link to add.
 */

public void insertLink(Link l) {
  Link link;

  //System.out.print("Antes de insertar " + l.toString() + "en posicion ");

  if (links.size()==0) links.addElement(l);
  else{
    int pos = getOrderedInsertionPoint(l);
    //System.out.println(pos + "\n" + this.toString());
    if (pos == links.size()) link = (Link)links.elementAt(pos-1);
    else link = (Link)links.elementAt(pos);
    if (!(l.equals(link))) links.insertElementAt(l,pos); 
  }

  //System.out.println("despues de insertar\n" + this.toString());

}


/**
 * Deletes the link given as argument. If the link doesn't exists,
 * des not do anything.
 * @param l the link to remove.
 */

public void removeLink(Link l) {
  Link l2;
  int position;

//  System.out.println(this.toString());
//  System.out.println("\nLink: ("+l.getTail().getName() + " , " +
//			l.getHead().getName() + ")");
//  System.out.println("\n size es " + links.size()); 
  if (l.getDirected()) position = locateLinkByBinSearch(l);
  else{//undirected link
    position = locateLinkByBinSearch(l);
    if (position == -1){
      l2 = new Link(l.getHead(),l.getTail(),false);
      position = locateLinkByBinSearch(l2);
    }
  }
//  System.out.println(".. y posicion es " + position);
  if (position != -1) links.removeElementAt(position);

}


/**
 * Saves the link in the variable given as argument.
 * @param p the object where the link is saved.
 */

public void save(PrintWriter p) {

  int i, j;

  p.print("// links of the associated graph:\n\n");
  j = links.size();

  for (i=0 ; i<j ; i++)
    ((Link) links.elementAt(i)).save(p);
}


/**
 * This method returns the position that the link, given as
 * parameter, occupies in the list.
 * @param l the link to find.
 * @return an integer with the position of the <code>l</code>
 * or -1 if the link isn't in the list.
 */

public int indexOf(Link l) {

  return links.indexOf(l);
}


/**
 * Deletes the link in a given position.
 * @param p position of the link to remove.
 */

public void removeLink(int p) {

  links.removeElementAt(p);
}


/**
 * This method is used to obtain the link between the two variables
 * whose names are given as parameters.
 * @param t first of the two nodes.
 * @param h second node.
 * @return an integer with the position of the link between <code>t</code>
 * and <code>h</code>, or -1 if the link doesn't exists.
 */

public int getID(String t, String h) {

  int pos;

  pos = locateLinkByBinSearch(t,h);
  if (pos != -1) return pos;
  else{
    pos = locateLinkByBinSearch(h,t);
    if (pos==-1) return pos;
    else{
      if (!((Link)links.elementAt(pos)).getDirected()) return pos;
      else return -1;
    }
  }  

}

public int getIDsequential(String t, String h) {

  int pos;
  Node he, ta;
  Link l;

  for (pos=0 ; pos<links.size() ; pos++) {
    l = (Link) links.elementAt(pos);
    he = (Node) l.getHead();
    ta = (Node) l.getTail();
    if (l.getDirected()) {
      if ((t.equals(ta.getName())) && (h.equals(he.getName())))
	return pos;
    }
    else
      if (((t.equals(ta.getName())) && (h.equals(he.getName()))) ||
	  ((h.equals(ta.getName())) && (t.equals(he.getName()))))
	return pos;
  }

  return (-1);
}


/**
 * This method is used to obtain the link that contains a given node.
 * @param n the node to find in one of the links.
 * @return an integer with the position of the first link in the list
 * containing <code>n</code> or -1 if this link isn't in the list.
 */

public int getID(Node n) {

  int pos;
  Node he, ta;
  Link l;

  for (pos=0 ; pos<links.size() ; pos++) {
    l = (Link) links.elementAt(pos);
    he = (Node) l.getHead();
    ta = (Node) l.getTail();
    if (n.getName().equals(ta.getName()) || n.getName().equals(he.getName()))
      return pos;
  }

  return (-1);
}


/**
 * This method is used to obtain the link between two nodes.
 * @param t the first node.
 * @param h the second node.
 * @return the link between <code>h</code> and <code>t</code> or
 * <code>null</code> if the link can't be found.
 */

public Link getLinks(String t, String h) {

  int id = getID(t,h);

  if (id == -1)
    return null;
  else
    return ((Link)links.elementAt(id));
}


/**
 * Used to know how many links are in the list.
 * @return nn integer with the number of links.
 */

public int size() {

  return ((int)links.size());
}


/**
 * Used to get the link in a given position.
 * @param p an integer with the position of the link to retrieve.
 * @return the link at position <code>p</code>.
 */

public Link elementAt(int p) {

  return ((Link)links.elementAt(p));
}


/**
 * Gets the enumeration of the <code>LinkList</code>.
 * @return an <code>Enumeration</code> with the <code>LinkList</code>.
 */

public Enumeration elements() {

  return ((Enumeration)links.elements());
}


/**
 * This method is used to know whether a node is a parent of another one.
 * @param t a node.
 * @param h another node.
 * @return <code>true</code> if there is a link from <code>t</code> to
 * </code>h</code> (<code>t</code> is a parent of <code>h</code>), or
 * <code>false</code> in other case.
 */

public boolean parent(Node t, Node h) {
// perhaps we should compare if the link retrieved is directed
  Link l = new Link(t,h);
  int pos = locateLinkByBinSearch(l);
  if (pos == -1) return false;
  else return true;

}

/**
 * This method computes the parents of the argument node by inspecting all directed 
 * links in this LinkList object. The Node objects that are tail of any directed Link object
 * with the Node child as head in this LinkList object are stored in a Vector<Node> object and returned.
 * 
 * The reason for implementing this method was to avoid using the parents 
 * field in object Node.
 * 
 * @param child - the node for which we want to get the parents.
 * @return the parents of child in a Vector<Node> object
 */
public Vector<Node> getParentsInList(Node child){
	Vector<Node> parents = new Vector<Node>();
	for(int i=0;i<links.size();i++){
		Link l = (Link)links.elementAt(i);
		if(l.getDirected() && l.getHead() == child){
			parents.add(l.getTail());
		}
	}
	return parents;
}


/**
 * This method computes the children of the argument node by inspecting all directed 
 * links in this LinkList object. The Node objects that are head of any directed Link object
 * with Node parent as tail in this LinkList object are stored in a Vector<Node> object and returned.
 * 
 * The reason for implementing this method was to avoid using the parents and children 
 * field in object Node.
 * 
 * @param parent - the node for which we want to get the parents.
 * @return the children of Node parent in a Vector<Node> object
 */
public Vector<Node> getChildrenInList(Node parent){
	Vector<Node> children = new Vector<Node>();
	for(int i=0;i<links.size();i++){
		Link l = (Link)links.elementAt(i);
		if(l.getDirected() && l.getTail() == parent){
			children.add(l.getHead());
		}
	}
	return children;	
}

/**
 * Joins this list with the argument one.
 * The current list is modified, and will contain the result.
 * @param list a list to join with this one.
 */

public void join(LinkList list) {

  int i,j,c;
  Vector v = new Vector(Math.max(this.size(),list.size()));

  for(i=0,j=0; ((i<this.size()) && (j<list.size())); ){
    c = compareLinksByName((Link)links.elementAt(i),list.elementAt(j));
    if (c<0) v.addElement(links.elementAt(i++));
    else if (c>0) v.addElement(list.elementAt(j++));
    else {v.addElement(links.elementAt(i)); i++; j++;}
  }

  for( ; i<this.size(); i++) v.addElement(links.elementAt(i));
  for( ; j<list.size(); j++) v.addElement(list.elementAt(j));

  links=v;
}


/**
 * Creates a copy of the list that receives the message. The copy
 * is done cloning the vector.
 * @return a copy of this list.
 */

public LinkList copy() {

  LinkList list;

  list = new LinkList();

  list.links = (Vector)links.clone();

  return list;
}


/**
 * Generates a new list with its elements equal to those in this one,
 * but not the same because they are duplicated.
 * @return a duplicate of this list.
 */

public LinkList duplicate() {

  LinkList dlist = new LinkList();
  Link link;

  for (int posl=0 ; posl<links.size() ; posl++) {
    link = (Link)links.elementAt(posl);
    dlist.links.addElement(link.duplicate());
  }
  return dlist;
}


/**
 * This method converts a list of links to a <code>String</code>.
 * @ return a string with the names of the links in the list.
 */

public String toString() {

  StringBuffer string = new StringBuffer("");
  Link l;

  for (int i=0 ; i<links.size() ; i++) {
    l = (Link)links.elementAt(i);
    if (l.getDirected())
      string.append("("+(l.getTail()).getName()+"->"+(l.getHead()).getName()+")");
    else
      string.append("("+(l.getTail()).getName()+"--"+(l.getHead()).getName()+")");
    string.append("\n");
  }
  return (string.toString());
}


/**
 * Intersects this list with the argument one.
 * @param list a <code>LinkList</code>.
 * @return a new list with the common links between this
 * and <code>list</code>.
 */

public LinkList intersection (LinkList list) {
  LinkList ll = new LinkList();
  int i,j,c;
  Vector v = new Vector();

  for(i=0,j=0; ((i<links.size()) && (j<list.size())); ){
    c = compareLinksByName((Link)links.elementAt(i),list.elementAt(j));
    if (c < 0) i++;
    else if (c > 0) j++;
    else if (c == 0) {v.addElement(links.elementAt(i));i++;j++;}
  }

  ll.setLinks(v,false);
  return ll;
}


/**
 * Obtains the difference of two lists.
 * @return a new list with the links in the list which receives
 * the message minus the nodes in <code>list</code>.
 * @param list the resulting list.
 */

public LinkList difference(LinkList list) {

  int i,j,c;
  LinkList ll = new LinkList();
  Vector v = new Vector(); 
 
  for(i=0,j=0; ((i<links.size()) && (j<list.size())); ){
    c = compareLinksByName((Link)links.elementAt(i),list.elementAt(j));
    if (c < 0) v.addElement(links.elementAt(i++));
    else if (c > 0) j++;
    else if (c == 0) {i++;j++;}
  }
  for( ; i<links.size(); i++) v.addElement(links.elementAt(i));

 
  ll.setLinks(v,false);
  return ll;
}


// **********************************************************************
// insertion and location methods based on bin search
// **********************************************************************

/**
 * return the position in which the link has to be inserted in
 * order to maintain the list sorted
 * 
 * @param link the <code>Link</code> to be inserted in the list
 * @return the position in which n has to be inserted 
 */
 
public int getOrderedInsertionPoint(Link link){

  int min,max,mid=-1;
  int s;
  Object middle;
  int c;

  s = links.size();
  if (s==0) mid=0;
  else if (compareLinksByName((Link)links.elementAt(s-1),link) < 0) mid=s;
  else{
    for(min=0, max=s-1, mid=(int)(min+max)/2;
        min < max;
        mid = (int) (min+max)/2){
  
      middle = links.elementAt(mid);
      c = compareLinksByName(link,(Link)middle);  

      if (c < 0) max = mid;
      else if (c > 0) min = mid + 1;
      else break; // mid is the position
    }
  }

  return mid;
}

/**
 * locateLinkByBinSearch: look for a link using binary search
 *
 * @param link the <code>Link</code> to locate in the list
 * @return the position in which the link is place or -1 if it is not
 *		included in the linklist
 */


public int locateLinkByBinSearch(Link link){
  int low=0,high=links.size()-1,mid=0;
  boolean found=false;
  Link linkM=null;
  int c;

  while ( (low <= high) && !found) {
    mid = (int) (low+high)/2;
    linkM = (Link)links.elementAt(mid);
    c = compareLinksByName(link,linkM);
    if (c < 0) high = mid-1;
    else if (c > 0) low = mid+1;
    else if (c == 0) found = true;
  }

  if (found) return mid;
  else return -1;
}

public int locateLinkByBinSearch(String tail, String head){
  int low=0,high=links.size()-1,mid=0;
  boolean found=false;
  Link linkM=null;
  int c;

  while ( (low <= high) && !found) {
    mid = (int) (low+high)/2;
    linkM = (Link)links.elementAt(mid);

    c = compareLinksByName(tail,head,linkM);
    //c = tail.compareTo(linkM.getTail().getName());
    //if (c == 0) c = head.compareTo(linkM.getHead().getName());

    if (c < 0) high = mid-1;
    else if (c > 0) low = mid+1;
    else if (c == 0) found = true;
  }

  if (found) return mid;
  else return -1;
}

/**
 * Sort the Links in the list by using lexicographic order
 */

public void sort(){
  qsort(0,links.size()-1);
}

/**
 * quick sort.
 */

  void qsort(int lo0, int hi0)   {
      int lo = lo0;
      int hi = hi0;
      Link mid;
 
      if ( hi0 > lo0)
	{
	  mid = (Link) links.elementAt( ( lo0 + hi0 ) / 2 );
	  while( lo <= hi )
	    {    
	      while( ( lo < hi0 ) && (compareLinksByName( (Link)links.elementAt(lo) , mid) < 0) )
		++lo;
	      while( ( hi > lo0 ) && (compareLinksByName( (Link)links.elementAt(hi) , mid) > 0) )
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
    Object obj;
    obj = links.elementAt(i);
    links.setElementAt(links.elementAt(j),i);
    links.setElementAt(obj,j);
  }


/**
 * isSorted: 
 *
 * @return true if the links are sorted lexicographically
 */

public boolean isSorted(){
  boolean s = false;

  for(int i=0;i<links.size()-1;i++)
    if (compareLinksByName((Link)links.elementAt(i),(Link)links.elementAt(i+1)) > 0) 
      return false;

  return true;
}


/**
 * compareLinks(link l1,link l2) 
 * returns -1, 0 or 1 as most compareTo methods do
 */

public int compareLinksByName(Link l1, Link l2){
    int c = l1.getTail().getName().compareTo(l2.getTail().getName());
    if (c == 0) c = l1.getHead().getName().compareTo(l2.getHead().getName());
    return c;
}  

public int compareLinksByName(String tail, String head, Link l2){
    int c = tail.compareTo(l2.getTail().getName());
    if (c == 0) c = head.compareTo(l2.getHead().getName());
    return c;
}  



//***********************************************************************
//        private class LinkComparatorByName
//***********************************************************************

// main

public static void main(String args[]){
  LinkList ll;
  FiniteStates nodeT,nodeH;
  Link link;
  boolean directed;
  String[] estates = {"yes","no"};  
  int s,i,j,k;
  Random gen = new Random();
  String st;
  Date d;
  double timeBin,timeSeq;
  

  System.out.println("tama�o \t timeSeq \t timeBin");

  for(s=1;s<10;s++){
    //creamos el linklist de tamanyo s
    ll = new LinkList();
    for(i=0;i<s;i++){
      st = "N"+gen.nextInt(20);
      nodeT = new FiniteStates(new String(st),estates); 
      st = "N"+gen.nextInt(20);
      nodeH = new FiniteStates(new String(st),estates); 
      if (gen.nextInt(2)==0) directed=true;
      else directed=false;
      link = new Link(nodeT,nodeH,directed);
      ll.insertLink(link);
    }
    //System.out.println(ll.toString());
   
    // vale, ahora vamos a buscar ....
    // creamos cinco links y los buscamos

    Link[] links = new Link[20];

    for(j=0;j<20;j++){
      //crear link
      st = "N"+gen.nextInt(20);
      nodeT = new FiniteStates(new String(st),estates); 
      st = "N"+gen.nextInt(20);
      nodeH = new FiniteStates(new String(st),estates); 
      if (gen.nextInt(2)==0) directed=true;
      else directed=false;
      link = new Link(nodeT,nodeH,directed);
      //link creado
      links[j] = link;
    }


    //buscar con metodo viejo

    d = new Date();
    timeSeq = (double)d.getTime();

    for(j=0;j<1000;j++)
      for(k=0;k<20;k++) 
        ll.getIDsequential(links[k].getTail().getName(),links[k].getHead().getName());

    d = new Date();
    timeSeq = ((double)d.getTime()-timeSeq) / 1000;

    //buscar con metodo nuevo

    d = new Date();
    timeBin = (double)d.getTime();

    for(j=0;j<1000;j++)
      for(k=0;k<5;k++) 
        ll.getID(links[k].getTail().getName(),links[k].getHead().getName());

    d = new Date();
    timeBin = ((double)d.getTime()-timeBin) / 1000;

    // informando

    System.out.println(s + "\t" + timeSeq + "\t" + timeBin);

  }

} // end main



} // End of class LinkList.
