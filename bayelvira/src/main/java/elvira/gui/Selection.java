/* Selection.java */

package elvira.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.Vector;
import elvira.*;
import elvira.Elvira;
import elvira.gui.explication.*;


/**
 * Selection provides a simple mechanism to have the
 * nodes, links and relations that are selected into the
 * graphical editor
 *
 * @author ..., fjdiez, ratienza, ...
 * @since 3/11/99
 * @version 0.1
 */

public class Selection implements Cloneable, Transferable{
   private Vector nodes;
   private Vector links;
   private Vector relations;

   /**
    * Stores the higest (X,Y) position of the nodes selected. This will be
    * used by the EditorPanel to set its size. The X and Y values can be
    * of different nodes.
    */
   private Point maxPosition;

   /**
    * Stores the lowest (X,Y) position of the nodes selected. This will be
    * used by the EditorPanel to set its size. The X and Y values can be
    * of different nodes.
    */
   private Point minPosition;


   /**
    * Creates a new empty Selection object with all its variables
    * sets to empty too.
    */

   public Selection() {
      nodes = new Vector();
      links = new Vector();
      relations = new Vector();
   }


   /**
    * Returns an array of DataFlavor objects indicating the
    * flavors the data can be provided in. The array should
    * be ordered according to preference for providing the
    * data (from most richly descriptive to least descriptive).
    *
    * @return an array of data flavors in which this data can be transferred
    */

   public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] elviraDataFlavors = new DataFlavor[2];

      try {
         elviraDataFlavors[0] = new DataFlavor (
                  Class.forName("elvira.gui.Selection"),"Selection");
         elviraDataFlavors[1] = new DataFlavor (
                  Class.forName("String"),"String");
      } catch (ClassNotFoundException e) {
         System.err.println("Exception: "+e.getMessage());
      }

      return elviraDataFlavors;

   }


   /**
    * Returns whether or not the specified data flavor is supported
    * for this object.
    * @param theFlavor The requested flavor for the data
    * @return boolean indicating wjether or not the data flavor is supported
    */

   public boolean isDataFlavorSupported (DataFlavor theFlavor) {
      boolean result = false;
      try {
         if (theFlavor.equals( new DataFlavor(
               Class.forName("elvira.gui.Selection"), "Selection")) ||
             theFlavor.equals( new DataFlavor(
               Class.forName("String"), "String")))
                  result = true;
      } catch (ClassNotFoundException e) {
         System.err.println("Exception: "+e.getMessage());
      }

      return result;

   }


   /**
    * Returns an object which represents the data to be
    * transferred. The class of the object returned is defined
    * by the representation class of the flavor.
    *
    * @param theFlavor the requested flavor for the data
    */

   public Object getTransferData (DataFlavor theFlavor)
      throws UnsupportedFlavorException, IOException {

      Object result = null;
      try {
         if (theFlavor.equals(new DataFlavor(
            Class.forName("elvira.gui.Selection"),"Selection")))
               result = this;
         else
            result = this.toString();
      } catch (ClassNotFoundException e) {
         System.err.println("Exception: "+e.getMessage());
      }

      return result;
   }


   /**
    * Returns the node at the specified position
    */

   public Node getNode (int position) {
      return ((Node) nodes.elementAt(position));
   }


   public Node getNode (String name) {
      Node n;

      for (int i=0; i<nodes.size(); i++) {
         n = (Node) nodes.elementAt(i);
         if (name.equals(n.getName()))
            return n;
      }

      return null;
   }


   public Vector getNodes() {
	return nodes;
   }


   /**
    * Returns the link at the specified position
    */

   public Link getLink (int position) {
      return ((Link) links.elementAt(position));
   }


   /**
    * Returns the relation at the specified position
    */

   public Relation getRelation (int position) {
      return ((Relation) relations.elementAt(position));
   }


   /**
    * Returns the higest (X,Y) position of the nodes selected
    */

   public Point getMaxPosition() {
      return maxPosition;
   }


   /**
    * Returns the lowest (X,Y) position of the nodes selected
    */

   public Point getMinPosition() {
      return minPosition;
   }


   /**
    * Set the new higest Position adding the distance moved
    * given as parameter.
    *
    * @param n Node that could modified the minimum position
    * @param zoom The new minimum position must be set according
    * to the zoom of the Editor
    * @param isInference The value returned depends on the mode
    * currently active, so this parameter is used to determine the
    * the correct values to set the maximum position
    */

   private void setMaxPosition (Node n,
                  double zoom, boolean isInference) {
      int posX, posY;
      if (isInference) {
         posX = (int) ((n.getPosX()+VisualNode.getWidth(n)/2)*zoom);
         posY = (int) ((n.getPosY()+VisualNode.getHeight(n)/2)*zoom);
      }
      else {
         posX = (int) ((n.getPosX()+n.getHigherAxis()/2)*zoom);
         posY = (int) ((n.getPosY()+n.getLowerAxis()/2)*zoom);
      }

      if (posX > maxPosition.x)
         maxPosition.x = posX;
      if (posY > maxPosition.y)
         maxPosition.y = posY;
   }


   /**
    * Set the new higest Position adding the distance moved
    * given as parameter.
    *
    * @param n Node that could modified the minimum position
    * @param zoom The new minimum position must be set according
    * to the zoom of the Editor
    */

   private void setMinPosition (Node n,
                  double zoom, boolean isInference) {
      int posX, posY;
      if (isInference) {
         posX = (int) ((n.getPosX()-VisualNode.getWidth(n)/2)*zoom);
         posY = (int) ((n.getPosY()-VisualNode.getHeight(n)/2)*zoom);
      }
      else {
         posX = (int) ((n.getPosX()-n.getHigherAxis()/2)*zoom);
         posY = (int) ((n.getPosY()-n.getLowerAxis()/2)*zoom);
      }

      if (posX < minPosition.x)
         minPosition.x = posX;
      if (posY < minPosition.y)
         minPosition.y = posY;
   }


   /**
    * Sets the new maximum and minimum position of the
    * selection.
    *
    * @param n Node used to set the new positions
    * @param zoom Cotains the zoom actually used in the EditorPanel.
    */

   public void recalculatePositions (Node n,
                  double zoom, boolean isInference) {
      setMaxPosition (n, zoom, isInference);
      setMinPosition (n, zoom, isInference);
   }


   /**
    * Reset the maximum and minimum positions. This function is
    * used when no nodes are selected
    */

   public void resetPositions () {
      maxPosition = new Point (0,0);
      minPosition = new Point (1000,1000);
   }


   /**
    * Add a node to the end of nodes Vector and set the maxPosition
    * variable according to this new node
    */

   public void addNode (Node n, double zoom) {
      int x = (int) (n.getPosX()*zoom),
          y = (int) (n.getPosY()*zoom);

      nodes.addElement(n);
      if (nodes.size()==1) {
         maxPosition = new Point(x,y);
         minPosition = new Point(x,y);
      }
      else {
         setMaxPosition(n,zoom,false);
         setMinPosition(n,zoom,false);
      }
   }


   /**
    * Add a link to the end of links Vector
    */

   public void addLink (Link l) {
      links.addElement(l);
   }


   /**
    * Add a link to the end of relations Vector
    */

   public void addRelation (Relation r) {
      relations.addElement(r);
   }


   /**
    * Removes the first ocurrence of the node n in the
    * nodes vector. If the node cannot be found do nothing
    */

   public void removeNode (Node n) {
      nodes.removeElement(n);
   }


   /**
    * Removes the first ocurrence of the node n in the
    * links vector. If the node cannot be found do nothing
    */

   public void removeLink (Link l) {
      links.removeElement(l);
   }


   /**
    * Removes the first ocurrence of the node n in the
    * relations vector. If the node cannot be found do nothing
    */

   public void removeRelation (Relation r) {
      relations.removeElement(r);
   }


   /**
    * Returns the number of nodes of the Selection object
    */

   public int numberOfNodes() {
      return nodes.size();
   }


   /**
    * Returns the number of links of the Selection object
    */

   public int numberOfLinks() {
      return links.size();
   }


   /**
    * Search the first ocurrence of the given node into the
    * nodes vector
    */

   public int getNodePosition (Node n) {
      return nodes.indexOf(n);
   }


   /**
    * Returns a clone of this selection object
    */

   public Object clone () {
      Selection selectionClone = new Selection();
      for (int i=0; i<nodes.size(); i++) {
         FiniteStates fn = ((FiniteStates) nodes.elementAt(i)).copy(false);
         selectionClone.nodes.addElement(fn);
      }

      for (int i=0; i<links.size(); i++) {
         Link l = (Link) links.elementAt(i);
         if (selectionClone.links.indexOf(l)==-1) {
            Node head = selectionClone.getNode(l.getHead().getName());
            Node tail = selectionClone.getNode(l.getTail().getName());
            selectionClone.links.addElement(new Link (head,tail));
         }
      }

      selectionClone.relations = (Vector) relations.clone();
      return selectionClone;
   }


   /**
    * Copy the selection that receives this message into a
    * new one that contains a clone of all its nodes and links
    * and all the links that contains any of the links cloned
    *
    * @param bayesNet Network that contains the selection to
    * be copied
    */

   public Selection copySelectionToPaste() {
      Selection selectionClone = new Selection();
      for (int i=0; i<nodes.size(); i++) {
         FiniteStates old = (FiniteStates) nodes.elementAt(i);
         selectionClone.nodes.addElement(old.copy(false));

         // the parents and the children must be added because when
         // a node is cut or copied the links that had with others
         // nodes must be included
         //addLinks(old.getParents());
         //addLinks(old.getChildren());
      }

      for (int i=0; i<links.size(); i++) {
         Link l = (Link) links.elementAt(i);
         if (selectionClone.links.indexOf(l)==-1) {
            Node head = selectionClone.getNode(l.getHead().getName());
            if (head == null)
               head = l.getHead();
            Node tail = selectionClone.getNode(l.getTail().getName());
            if (tail == null)
               tail = l.getTail();
            selectionClone.links.addElement(new Link(tail,head));
         }
      }

      selectionClone.relations = (Vector) relations.clone();
      /*for (int i=0; i<relations.size(); i++) {
        selectionClone.relations.addElement(((Relation) relations.elementAt(i)).copy());
      }*/
      return selectionClone;
   }


   /**
    * Add the links contained in the LinkList given as parameter
    * to the list of links of the Selection object
    */

   public void addLinks(LinkList linkList) {
      for (int i=0; i<linkList.size(); i++) {
         Link l = (Link) linkList.elementAt(i);
         if (this.links.indexOf(l)==-1)
            links.addElement(new Link(l.getTail(), l.getHead()));
      }
   }

   public void changeAllNames(Bnet bayesNet) {
      for (int i=0; i<nodes.size(); i++) {
         FiniteStates fn = (FiniteStates) nodes.elementAt(i);
         changeName(fn,bayesNet);
      }
   }

   /**
    * Change the name of node given as parameter.
    * This method will be used when the paste action
    * is done and the nodes added contains names that
    * is this moment are displayed in the Frame. </P>
    * <P> If it is necessary a new, this will contain the
    * next structure: <old_name>+'_'+<integer>.
    * This method introduces the new names in the links too.
    *
    * @return True if the name has been changed
    */

   public boolean changeName (Node n, Bnet bayesNet) {

      String name = n.getName();
      int counter=1;
      while (bayesNet.checkName(n.getName())==null) {
         n.setName(name+'_'+counter);
         counter++;
      }
  FontMetrics fm=Elvira.getElviraFrame().getCurrentEditorPanel().getFontMetrics(
          ElviraPanel.getFont(n.getFont()));
      VisualNode.setAxis(n,n.getName(),fm);
      return !name.equals(n.getName());

   }

}
