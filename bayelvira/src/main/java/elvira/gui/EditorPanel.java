/* EditorPanel.java */

package elvira.gui;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.undo.*;
import java.util.*;
import java.lang.Math;
import java.awt.event.*;
import elvira.gui.explication.*;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.*;

/**
 * Contains the work area for make changes
 * in the net topology
 *
 * @author ..., fjdiez, ratienza, ...
 * @since 2/11/99
 * @version 0.1
 */

public class EditorPanel extends ElviraPanel implements MouseListener, MouseMotionListener {

   /**  Store the mode for events in the panel */
   private static int mode;
   //   private Timer timer;

   /* Variables that store various quantities that must
      be shared among event handling functions */
   private boolean newArc = false;
   private Point newArcHead = null;

   private Point selectBegin, selectEnd;
   
   private boolean hasBeenCompiled = false;
   

   private int chanceNodes = 0,
               utilityNodes = 0,
               decisionNodes = 0,
               observedNodes = 0
               ;

   /* to show graphically the influences of the links*/
   private boolean showInfluences=false;

   // network editing modes
   private static final int CREATE_NODE_MODE  = 1;
   private static final int CREATE_LINK_MODE = 2;
   private static final int MOVE_MODE    = 3;
   private static final int DELETE_MODE  = 4;
   private static final int OBSERVE_MODE = 5;
   private static final int QUERY_MODE   = 6;
   private static final int EDIT_VARIABLE_MODE = 7;
   private static final int EDIT_FUNCTION_MODE = 8;
   private static final int EDIT_NETWORK_MODE = 9;
   private static final int SELECTING = 10;

   // network type modes
   public static final String CREATE_NODE = "Create Node";
   public static final String CREATE_LINK = "Create Link";
   public static final String MOVE = "Move";
   public static final String DELETE = "Delete";
   public static final String EDIT_VARIABLE = "Edit Variable";
   public static final String EDIT_FUNCTION = "Edit Function";


   public static final String[] functionsNode={"Treatment", "Riskfactor", "Symptom", "Sign", "Test", "Disease", "Aux","Observable", "Other", "Defined", ""};


   /**
    * Contain the type of the node that will be
    * created if the CREATE_NODE_MODE is active
    */

   private int nodeType = Node.CHANCE;

	/* PopupMenu and its items */
	public JPopupMenu editorPopupMenu = new JPopupMenu();
	public JMenuItem editMenuItem = new JMenuItem(),
	                 explainMenuItem = new JMenuItem(),
                         nSCMenuItem = new JMenuItem(),
                         nSCToLinkMenuItem = new JMenuItem(),
                         testDecisionMenuItem = new JMenuItem(),
	                 deleteMenuItem = new JMenuItem(),
	                 cutMenuItem = new JMenuItem(),
	                 copyMenuItem = new JMenuItem(),
	                 pasteMenuItem = new JMenuItem();

        /**
         * Default constructor for NetworkPanel object.
         *
         * @param frame ElviraFrame
         */

        EditorPanel() {

        super();

    	modifiedNetwork = false;

        // Set mode
        mode = MOVE_MODE;
        selection = new Selection();
	   propertiesEditable = true;

	   addMouseListener(this);
	   addMouseMotionListener(this);

		editorPopupMenu.add(editMenuItem);

		editorPopupMenu.add(new JSeparator());

                insertMenuItem (cutMenuItem, Elvira.getElviraFrame().cutIcon,
                                editorPopupMenu, "Edit.Cut", "Cut");
                insertMenuItem (copyMenuItem, Elvira.getElviraFrame().copyIcon,
                                editorPopupMenu, "Edit.Copy", "Copy");
                insertMenuItem (pasteMenuItem, Elvira.getElviraFrame().pasteIcon,
                                editorPopupMenu, "Edit.Paste", "Paste");
                pasteMenuItem.setEnabled(false);
		editorPopupMenu.add(deleteMenuItem);

		//{{REGISTER_LISTENERS
		EditorPopupAction action = new EditorPopupAction();
		editMenuItem.addActionListener(action);
                nSCMenuItem.addActionListener(action);
                nSCToLinkMenuItem.addActionListener(action);
                testDecisionMenuItem.addActionListener(action);
		explainMenuItem.addActionListener(action);
		deleteMenuItem.addActionListener(action);
		cutMenuItem.addActionListener(action);
		copyMenuItem.addActionListener(action);
		pasteMenuItem.addActionListener(action);
		//}}
	}


        private void setMenuItem (JMenuItem item, javax.swing.Icon icon,
                                  JPopupMenu menu, String name, String actionCommand) {
          item.setIcon(icon);
          item.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
          item.setText(localize(menuBundle,name+".label"));
          item.setActionCommand(actionCommand);
          item.setMnemonic((int) localize(menuBundle,name+".mnemonic").charAt(0));
        }


        public void insertMenuItem (JMenuItem item, javax.swing.Icon icon,
                                    JPopupMenu menu, String name, String actionCommand) {
          setMenuItem (item, icon, menu, name, actionCommand);
          menu.add(item);
        }


	public void insertMenuItemAt (JMenuItem item, javax.swing.Icon icon,
                                      JPopupMenu menu, String name, String actionCommand,
                                      int position) {
          setMenuItem (item, icon, menu, name, actionCommand);
          menu.insert(item, position);
        }

        /* ************** Accesing methods ************* */

   /**
    * This method is used to get the mode of the EditorPanel
    *
    * @return An integer with the actual mode of working
    */

   public int getMode () {
      return mode;
   }

   public Selection getSelection() {
      return selection;
   }

   public void setHasBeenCompiled(boolean b) {
      hasBeenCompiled = b;
   }

   public boolean getInfluences(){
   	return showInfluences;
   }

   /**
    * Used to see if the Elvira object has been modified.
    *
    * @return True if the Elvira object has been modified
    * False in other case
    */

   public boolean isModifiedNetwork () {
      return modifiedNetwork;
   }


   public void undo() {
      try {
	      undoManager.undo();
	   } catch (CannotUndoException e) {
	      Elvira.println ("Unable to undo: "+e);
	      e.printStackTrace();
	   }
   }


   public void redo() {
      try {
         undoManager.redo();
      } catch (CannotUndoException e) {
         Elvira.println("Unable to redo: "+e);
         e.printStackTrace();
      }
   }


   /* ******************* Modifiers ********************* */


   public void setInfluences(boolean b){
   	showInfluences=b;
   }

   public void setMode(int m) {
      mode = m;
   }


   /**
    * Set the mode for the NetworkPanel.
    *
    * @param label This string contains the name of the mode to be set
    */

   public void setMode(String label) {

	   if (label.equals(CREATE_NODE ))
	      mode = CREATE_NODE_MODE;
	   else if (label.equals(CREATE_LINK))
	      mode = CREATE_LINK_MODE;
	   else if (label.equals(MOVE))
	      mode = MOVE_MODE;
	   else if (label.equals(DELETE))
	      mode = DELETE_MODE;
	   else if (label.equals(EDIT_VARIABLE))
	      mode = EDIT_VARIABLE_MODE;
	   else if (label.equals(EDIT_FUNCTION))
	      mode = EDIT_FUNCTION_MODE;
	   /*else if (label.equals(EXPLAIN_NODE ))
	      mode = EXPLAIN_NODE_MODE;
	   else if (label.equals(EXPLAIN_LINK))
	      mode = EXPLAIN_LINK_MODE;	      */
	   else // default mode;
	      mode = MOVE_MODE;
   }


   public void setNodeType (int type) {
      nodeType = type;
   }

    /**
     * Clear the selection variable. This method will be used when the
     * user want to do a new Selection
     */

    public void resetSelection () {
      selection = new Selection();
    }


   /**
    * Store the QuasiBayesNet object to be displayed in
    * the NetworkPanel.
    *
    * @param bn Bnet to be stored
    */

   public void load(Bnet bn) {
	   bayesNet = bn;
	   update(getGraphics());	// forced update to clear old network
	   repaint();
   }


   /**
    * Clear the NetworkPanel
    */
   void clear() {
      bayesNet = new Bnet();
      repaint();
   }


   /**
    * Enables or disables the menu items according
    * to the value of the parameter b
    */

   public void elementSelected (boolean b, String deleteName,
                                String deleteAction) {
      JMenuItem lastItem = (JMenuItem) editorPopupMenu.getComponentAtIndex(
               editorPopupMenu.getComponentCount()-1);

      if (b)
         if (lastItem == deleteMenuItem) {
            deleteMenuItem.setText(deleteName);
            deleteMenuItem.setActionCommand(deleteAction);
         }
         else
            insertMenuItem (deleteMenuItem, null, editorPopupMenu,
	           deleteName, deleteAction);
	   else
	      if (lastItem == deleteMenuItem)
	         editorPopupMenu.remove(deleteMenuItem);

      cutMenuItem.setEnabled(b);
      copyMenuItem.setEnabled(b);
   }
   
   
   public void processMouseEvent (MouseEvent e) {

      currentNode = nodeHit((int) (e.getX()/zoom), (int) (e.getY()/zoom));

      // if the first and the second elements are JMenuItem, the
      // explainMenuItem is in the editorPopupMenu.
      boolean existsEditorItem =
            (editorPopupMenu.getComponentAtIndex(1).getClass() ==
             editorPopupMenu.getComponentAtIndex(0).getClass());

      if (hasBeenCompiled)
         explainMenuItem.setEnabled(true);
      else
         explainMenuItem.setEnabled(false);

      // Checks if the popup menu must been showed

      if (e.isPopupTrigger()) {
         unSelectAll();

         // The popup menu is shown over a node
         if (currentNode != null) {
            if (currentNode.getKindOfNode()==Node.CHANCE)
               explainMenuItem.setEnabled(true);
            else
               explainMenuItem.setEnabled(false);
            currentNode.setSelected(true);
            selection.addNode(currentNode,zoom);
            editMenuItem.setText(localize(menuBundle,"Popup.NodeProperties.label"));
            editMenuItem.setActionCommand("Edit Node");

            for (int i=0; i<editorPopupMenu.getComponents().length; i++) {
              if (editorPopupMenu.getComponent(i) == nSCToLinkMenuItem) {
                editorPopupMenu.remove(i);
              }
            }

            if ((bayesNet.getClass() == IDiagram.class) && (currentNode.getKindOfNode() == Node.CHANCE)) {
              insertMenuItemAt(testDecisionMenuItem, null,
                                editorPopupMenu, localize(menuBundle,"Popup.addTestDecision.label"),
                                "Add Test Decision", 1);
              testDecisionMenuItem.setEnabled(true);
              testDecisionMenuItem.setText(localize(menuBundle,"Popup.addTestDecision.label"));
              testDecisionMenuItem.setActionCommand("Add Test Decision");
            }
            else {
              testDecisionMenuItem.setEnabled(false);
              for (int i=0; i<editorPopupMenu.getComponents().length; i++) {
                if (editorPopupMenu.getComponent(i) == testDecisionMenuItem) {
                  editorPopupMenu.remove(i);
                }
              }
            }

            if (currentNode.getKindOfNode() == Node.DECISION) {
              insertMenuItemAt(nSCMenuItem, null,
                                editorPopupMenu, localize(menuBundle,"Popup.NonSenseConstraints.label"),
                                "Non Sense State", 1);
              nSCMenuItem.setEnabled(true);
              nSCMenuItem.setText(localize(menuBundle,"Popup.NonSenseConstraints.label"));
              nSCMenuItem.setActionCommand("Non Sense Constraint");
            }
            else {
              nSCMenuItem.setEnabled(false);
              for (int i=0; i<editorPopupMenu.getComponents().length; i++) {
                if (editorPopupMenu.getComponent(i) == nSCMenuItem) {
                  editorPopupMenu.remove(i);
                }
              }
            }

            if (existsEditorItem) {
               explainMenuItem.setText(localize(menuBundle,"Popup.ExplainNode.label"));
               explainMenuItem.setActionCommand("Explain Node");
            }
            else
              insertMenuItemAt (explainMenuItem, null,
                                editorPopupMenu, localize(menuBundle,"Popup.ExplainNode.label"),
                                "Explain Node", 1);

            elementSelected(true, localize(menuBundle,"Popup.DeleteNode.label"),"Delete Node");
         }
         else {
            arcHit((int) (e.getX()/zoom), (int) (e.getY()/zoom));

            for (int i=0; i<editorPopupMenu.getComponents().length; i++) {
              if (editorPopupMenu.getComponent(i) == nSCToLinkMenuItem) {
                editorPopupMenu.remove(i);
              }
            }

            nSCMenuItem.setEnabled(false);
            testDecisionMenuItem.setEnabled(false);
            for (int i=0; i<editorPopupMenu.getComponents().length; i++) {
              if (editorPopupMenu.getComponent(i) == nSCMenuItem) {
                editorPopupMenu.remove(i);
              }
              else if (editorPopupMenu.getComponent(i) == testDecisionMenuItem) {
                editorPopupMenu.remove(i);
              }
            }

            if ((arcHeadNode != null) && (arcBottomNode != null))
               currentLink = bayesNet.getLinkList().getLinks(arcBottomNode.getName(),
                   arcHeadNode.getName());
            if (currentLink != null) {
              if ((currentLink.getHead().getKindOfNode() == Node.CHANCE) && (currentLink.getTail().getKindOfNode() == Node.DECISION)) {
                insertMenuItemAt(nSCToLinkMenuItem, null,
                                  editorPopupMenu, localize(menuBundle,"Popup.NonSenseConstraints.label"),
                                  "Non Sense State", 1);
                nSCToLinkMenuItem.setEnabled(true);
                nSCToLinkMenuItem.setText(localize(menuBundle,"Popup.NonSenseConstraints.label"));
                nSCToLinkMenuItem.setActionCommand("Non Sense Constraint");
              }
              else {
                for (int i=0; i<editorPopupMenu.getComponents().length; i++) {
                  if (editorPopupMenu.getComponent(i) == nSCToLinkMenuItem) {
                    editorPopupMenu.remove(i);
                  }
                }
              }
               currentLink.setSelected(true);
               selection.addLink(currentLink);
               editMenuItem.setText(localize(
                     menuBundle,"Popup.LinkProperties.label"));
               editMenuItem.setActionCommand("Edit Link");

               if (existsEditorItem) {
                  explainMenuItem.setText(localize(menuBundle,"Popup.ExplainLink.label"));
                  explainMenuItem.setActionCommand("Explain Link");
               }
               else
                 insertMenuItemAt(explainMenuItem, null,
                                  editorPopupMenu, localize(menuBundle,"Popup.ExplainLink.label"),
                                  "Explain Link", 1);

               elementSelected(true, localize(menuBundle,"Popup.DeleteLink.label"),"Delete Link");
            }
            else {
              editMenuItem.setText(localize(menuBundle,"Popup.NetProperties.label"));
              editMenuItem.setActionCommand("Edit Network");

              if (existsEditorItem)
                editorPopupMenu.remove(1);

              elementSelected(false, null, null);
            }
         }

         repaint();
         editorPopupMenu.show(this, e.getX(), e.getY());

      }
      else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
         mousePressed(e);
      }
      else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
         mouseDragged(e);
      }
      else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
         mouseReleased(e);
      }

   }

   public void mouseClicked(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
   }

  /**
   * Process mouse down events.
   *
   * @param evt Event produced
   */

   public void mousePressed(MouseEvent evt) {
      Node node;

      arcHeadNode = null;
      arcBottomNode = null;

      int xPosition = (int) (evt.getX()/zoom),
          yPosition = (int) (evt.getY()/zoom);

      if (Elvira.getElviraFrame().unselectAllComponents ()) {
         evt.consume();
         return;
      }

      if (editorPopupMenu.isVisible()) {
         editorPopupMenu.setVisible(false);
         nodeToMove=null;
         unSelectAll();
         evt.consume();
         return;
      }

      currentNode = nodeHit(xPosition, yPosition);

      if (SwingUtilities.isRightMouseButton(evt)) {
         evt.consume();
         return;
      }


      /* If a double click has been produced */

      if (evt.getClickCount()==2) {
         ActionEvent event;
         EditorPopupAction action = new EditorPopupAction();
         newArc = false;
         Elvira.getElviraFrame().selectAction (new ActionEvent(this,0,""));

         if (currentNode != null) {
            event = new ActionEvent (editMenuItem,0,"Edit Node");
            action.actionPerformed(event);
         }
         else {
            arcHit (xPosition, yPosition);
            if ((arcHeadNode != null) && (arcBottomNode != null)) {
               event = new ActionEvent (editMenuItem,0,"Edit Link");
               action.actionPerformed(event);
            }
            else {
               Point p = validateCoordinates (xPosition, yPosition);

               bayesNet.createNode(p.x,p.y,"Helvetica",
                        bayesNet.generateName(chanceNodes++),nodeType);
               Node n = bayesNet.getNodeList().lastElement();
               FontMetrics fm=getFontMetrics(getFont(n.getFont()));
               VisualNode.setAxis(n,n.getNodeString(byTitle),fm);

               unSelectAll();
               repaint();
               currentNode = bayesNet.getNodeList().lastElement();

               EditVariableDialog d = new EditVariableDialog(currentNode,
                   byTitle, "New Node", true);

               /*timer = new Timer (1000, new ActionListener() {
               public void actionPerformed (ActionEvent e) {
		         timer.stop();
               }});

               timer.setRepeats(false);
               timer.start();

               while (timer.isRunning()) {
               }*/

               if (d.showDialog()) // The cancel button has been hit
                 try {
                 bayesNet.removeNode(bayesNet.getNodeList().size()-1);
                 chanceNodes--;
                 repaint();
                 } catch (InvalidEditException iee) {}

                 else {

                   /* The node has been added correctly, so the network
                   has been modified and the undo action must been added */

                   setModifiedNetwork(true);
                   AddNodeEdit addNodeAction = new AddNodeEdit((Node) nodeHit(xPosition, yPosition));
                   getUndoItem().setText(addNodeAction.getUndoPresentationName());
                   getRedoItem().setText(addNodeAction.getRedoPresentationName());
                   undoManager.addEdit(addNodeAction);
                   Elvira.getElviraFrame().enableUndo(true);
                 }

                 setModifiedNetwork(true);

            }
         }
         return;
      }

      startDragPosition = new Point (xPosition, yPosition);

      if (currentNode == null) { // If no node was clicked on.

         if (mode == DELETE_MODE) { // Delete arc
            arcHit(xPosition, yPosition);

            // Affects arcHeadNode and arcBottomNode
            if ((arcHeadNode != null) && (arcBottomNode != null)) {
                delete();


                arcHeadNode = null;
                arcBottomNode = null;
            }
            //else
                //ElviraHelpMessages.show(frame, ElviraHelpMessages.notnode);
            mode = MOVE_MODE;
        }
        else if (mode == CREATE_NODE_MODE) { // Create a node
           Point aux = new Point (xPosition, yPosition);
           createNode(aux.x, aux.y);
           //refreshElviraPanel(aux,aux);
           unSelectAll();
        }
        else if (mode == MOVE_MODE) {

            if (!evt.isShiftDown())
               unSelectAll();

            arcHit(xPosition, yPosition);
            if ((arcHeadNode != null) && (arcBottomNode != null)) {
               currentLink = bayesNet.getLinkList().getLinks(arcBottomNode.getName(),
                   arcHeadNode.getName());
               if (currentLink!=null)
                  if (!currentLink.isSelected()) {
                     currentLink.setSelected(true);
                     selection.addLink(currentLink);
                  }
                  else {
                     currentLink.setSelected(false);
                     selection.removeLink(currentLink);
                  }
            }
            else {
               unSelectAll();
               currentLink = null;
               if (mode == MOVE_MODE) {
                  mode = SELECTING;
                  selectBegin = new Point (xPosition, yPosition);
                  selectEnd = selectBegin;
               }
            }

            checkSelected();
        }
      }
      else {   // If a node was clicked on.

         /* If the control key is down, it must be created a link
            from all nodes selected to the node clicked */

         if (evt.isControlDown()) {
            newArc = true;
            mouseReleased (new MouseEvent(this,0,0,0,currentNode.getPosX(),currentNode.getPosY(),1,false));
         }

	      if (mode == MOVE_MODE) { // Move node
              if (!evt.isShiftDown() && !currentNode.isSelected())
                 unSelectAll();

              if (!currentNode.isSelected()) {
                 currentNode.setSelected(true);
                 selection.addNode(currentNode,zoom);
                 oldNodePositions.addElement(new Point (currentNode.getPosX(), currentNode.getPosY()));
              }
              checkSelected();
         }
         else if (mode == DELETE_MODE) { // Delete node
             if ((arcHeadNode != null) && (arcBottomNode != null)) {
              delete();
              mode = MOVE_MODE;
             }
         }
         else if (mode == CREATE_LINK_MODE) { // Create arc
             /* INSERTADO POR MANOLO */
           //if (currentNode.getKindOfNode()!=Node.UTILITY) { /* To remove in case of valid utility nodes as children of utility nodes */
             setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
             newArc = true;
             arcBottomNode = currentNode;
             newArcHead = new Point(xPosition, yPosition);
             unSelectAll();
           
           //}
         }
      }

      repaint();
   }


   /**
    * Process mouse drag events.
    *
    * @param evt Event produced
    */

   public void mouseDragged(MouseEvent evt) {

      int xPosition = (int) (evt.getX()/zoom),
          yPosition = (int) (evt.getY()/zoom);

      if (SwingUtilities.isRightMouseButton(evt)) {
         evt.consume();
         return;
      }

      if (selection.numberOfNodes()>0) {
         Point moved = new Point (xPosition-startDragPosition.x,
                                  yPosition-startDragPosition.y);
         selection.resetPositions();
         dragging = true;

         for (int i=0; i<selection.numberOfNodes(); i++) {
            Node n = selection.getNode(i);
            Point p = (Point) oldNodePositions.elementAt(i);
            n.setPosX(p.x+moved.x);
            n.setPosY(p.y+moved.y);
            selection.recalculatePositions(n,zoom,false);
         }

         //refreshElviraPanel(selection.getMaxPosition(),selection.getMinPosition());
      }
      else if (newArc == true) {
         newArcHead = new Point(xPosition, yPosition);
      }
      else if (mode == SELECTING) {
         selectEnd = new Point (xPosition, yPosition);
      }
      repaint();
   }

   public void mouseMoved(MouseEvent evt) {
   }

 

  /**
   * Process mouse up events.
   *
   * @param evt Event produced
   */

public void mouseReleased(MouseEvent evt) {
    int x = (int) (evt.getX()/zoom),
    y = (int) (evt.getY()/zoom);
    
    if (SwingUtilities.isRightMouseButton(evt)) {
        evt.consume();
        return;
    }
    
    if (selection.numberOfNodes() > 0 && newArc==false) {
        // Get the distance that the selection has moved
        int xMoved = x-startDragPosition.x,
        yMoved = y-startDragPosition.y;
        
        if (xMoved > 2 || yMoved > 2) {
            Point distanceMoved = new Point(xMoved,
            yMoved);
            selection.resetPositions();
            
            for (int i=0; i<selection.numberOfNodes(); i++) {
                Node n = selection.getNode(i);
                Point p = (Point) oldNodePositions.elementAt(i);
                n.setPosX(p.x);
                n.setPosY(p.y);
                selection.recalculatePositions(n,zoom,false);
            }
            //refreshElviraPanel(selection.getMaxPosition(),selection.getMinPosition());
            move(distanceMoved);
            setModifiedNetwork(true);
        }
    }
    else
        // Creates a new arc
        if (newArc == true) {
            arcHeadNode = nodeHit(x, y);
            /* If the mouse has been released on a node the link
           is created. But if the mouse hasn't been released
           on a node the line drawing must be erased*/
            if (arcHeadNode!=null) {
                if (bayesNet.getClass() == IDiagram.class){
                    if (arcBottomNode!=null) {
                        if (IDiagram.isCompatibleLink(arcBottomNode,arcHeadNode)){
                            newArc=true;
                            if ((arcHeadNode.getKindOfNode()==Node.UTILITY)
                            &&(((IDiagram) bayesNet).isThereAGlobalUtilityNode() == null)) {
                                //There wasn't global utility, so we set one.
                                 ((IDiagram) bayesNet).setGlobalUtility(arcHeadNode);
                            }
                      }
                        else{
                            newArc = false;
                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            repaint();
                            return;
                        }
                    }
                }
                if (bayesNet.getClass() == IDWithSVNodes.class){
                    if (arcBottomNode!=null) {
                        if (IDWithSVNodes.isCompatibleLink(arcBottomNode,arcHeadNode))
                            newArc=true;
                        else{
                            newArc = false;
                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            repaint();
                            return;
                        }
                    }
                }
                Node arcHead=bayesNet.getNodeList().getNode(arcHeadNode.getName());
                createLink();
                Vector rlist=bayesNet.getRelationList();
                boolean enc=false;
                for (int i=0; i<rlist.size() && !enc ; i++) {
                    Relation r = (Relation) rlist.elementAt(i);
                    if ((((Node)r.getVariables().elementAt(0)).getName()).equals(arcHead.getName())) {
                        enc=true;
                    }
                }
                if (enc) {
                    LinkList linkl=arcHead.getParents();
                    for (Enumeration e=linkl.elements(); e.hasMoreElements(); ) {
                        Link link = (Link) e.nextElement();
                        link.setColorLink(ARC_COLOR);
                    }
                }
                
            }//end if (arcHeadNode!=NULL)
            else
                newArc = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        else {
            if (mode == SELECTING) {
                areaSelected(selectBegin, selectEnd);
                checkSelected();
                mode = MOVE_MODE;
            }
        }
    
    dragging = false;
    repaint();
}
   
   

 



                    
                    


/**
* Paint the network.
*
* @param g Graphics interface
*/

public void paint(Graphics g) {

  Graphics2D g2 = (Graphics2D) g;
  Node node;
  Link link;    // añadido <----------- jgamez
  Enumeration e, ee;
  Class<? extends Bnet> bayesNetClass;

  if (bayesNet == null)
    return;
    	// draw a new arc upto current mouse position
    g2.setColor(ARC_COLOR);
    g2.scale(zoom, zoom);

	   // if the link it is now drawn, a dashed line is painted
  if (newArc) {
    g2.setStroke(dashed);
    g2.drawLine(arcBottomNode.getPosX(), arcBottomNode.getPosY(),
                newArcHead.x, newArcHead.y);
    g2.setStroke(stroke);
  }

  bayesNetClass = bayesNet.getClass();

  
  // draw all arcs
  //System.out.println("Show"+showInfluences);
  for (e=bayesNet.getLinkList().elements(); e.hasMoreElements(); ) {
    link = (Link) e.nextElement();
  	double wide=0.2;
    Color arcColor=ARC_COLOR;//negro por defecto
//    if (link.getColorLink()==null){
        //if(bayesNet.getClass()==Bnet.class && showInfluences){
  
		if (((bayesNetClass==Bnet.class)||(bayesNetClass==IDiagram.class)||(bayesNetClass==IDWithSVNodes.class)||(bayesNetClass==Dan.class))
    			&&showInfluences){
          	if (InferencePanel.isIncomingToSVNode(link)==false){
            double[][][] dist=macroExplanation.greaterdist(bayesNet, link.getHead(), link.getTail());
//          System.out.println("Distribuciones de "+link.getHead().getTitle()+"|"+link.getTail().getTitle());
//          macroExplanation.print(dist);
            int res=macroExplanation.compare(dist);

          	switch (res){
            	case 0:arcColor=ElviraPanel.GREATER_ARC_COLOR; //rojo
                       break;
            	case 1:arcColor=ElviraPanel.LESS_ARC_COLOR; //azul
                       break;
            	case 3:arcColor=ElviraPanel.EQUALS_ARC_COLOR; //violeta
                       break;
          	}
        	if (res==0 || res==1)
        		wide=macroExplanation.influence(dist);
        	
    	}
    	else{//Arc incoming to SV nodes are always drawn in red
    		arcColor=ElviraPanel.GREATER_ARC_COLOR;
    		wide=0.2;
    	}
    	}
        link.setColorLink(arcColor);
    	link.setWidth(wide);
    	//System.out.println(wide);
    	if (!link.isARevelationArc()){
    		VisualLink.drawArc(link, g2,dragging,selection, link.getColorLink(),false);
    	}
    	else{
    		//VisualLink.drawRevelationArc(link,g2,dragging,selection,link.getColorLink(),false);
    	}
  }
  // draw the nodes
  g2.setFont(HELVETICA);
  for (e = bayesNet.getNodeList().elements(); e.hasMoreElements(); ) {
    node = (Node)e.nextElement();
    if (node.getPosX() >= 0)
      VisualNode.drawNode (node,g2, NODE_COLOR, NODE_NAME_COLOR,
                           byTitle, dragging);
  }

  if (mode==SELECTING) {
    g2.setPaint(Color.black);
    if (selectBegin!=null && selectEnd!=null)
      g2.draw(new Rectangle2D.Double (selectBegin.x,selectBegin.y,
                                         selectEnd.x-selectBegin.x,
                                         selectEnd.y-selectBegin.y));
  }
}



   private boolean askWhetherAddATerminalValueNodeInfluences(IDWithSVNodes auxID) {
	// TODO Auto-generated method stub
		boolean propagate;
    	String msg;
    	
		
    	msg = ShowMessages.ADD_TERMINAL_VALUE_NODE_INFLUENCES;
    	
    	int reply;
    	
    	Object[] options = {localize(dialogBundle,"Yes.label"),localize(dialogBundle,"No.label")};
    	
    	reply = ShowMessages.showOptionDialog(msg,JOptionPane.QUESTION_MESSAGE,options,0);
    	
    	if (reply==0){ //The answer is YES
    		Node newNode;
    		propagate = true;
    		newNode =auxID.addATerminalSuperValueNode();
    		//Visual properties for the new SV node
    		FontMetrics fm=getFontMetrics(ElviraPanel.getFont(newNode.getFont()));
  	      	VisualNode.setAxis(newNode,newNode.getNodeString(byTitle),fm);
    		
    		setModifiedNetwork(true);
    		
    	}
    	else{//The answer is NO
    		propagate = false;
    	}
    	
    	return propagate;
}


/**
    * Create a node and create de necessary action for undo/redo it
    *
    * @param x X-position where the node is going to be created
    * @param y Y-position where the node is going to be created
    * @see Bnet#createNode
    */

   public void createNode(int x, int y) {
      String nodeName = new String();
      switch (nodeType) {
         case Node.CHANCE: nodeName = bayesNet.generateName(chanceNodes);
                 chanceNodes++;
                 break;
         case Node.DECISION: nodeName = bayesNet.generateSpecialName("D",decisionNodes);
                 decisionNodes++;
                 break;
         case Node.UTILITY: nodeName = bayesNet.generateSpecialName("U",utilityNodes);
                 utilityNodes++;
                 break;
                  
      }

      bayesNet.createNode (x, y, "Helvetica",
                           nodeName, nodeType);
      Node n = bayesNet.getNodeList().lastElement();
      FontMetrics fm=getFontMetrics(getFont(n.getFont()));
      VisualNode.setAxis(n,n.getNodeString(byTitle),fm);

	   setModifiedNetwork(true);

	   // Actions for the undo manager
	   AddNodeEdit addNodeAction = new AddNodeEdit(n);
	   getUndoItem().setText(addNodeAction.getUndoPresentationName());
	   getRedoItem().setText(addNodeAction.getRedoPresentationName());
	   undoManager.addEdit(addNodeAction);
	   Elvira.getElviraFrame().enableUndo(true);
	   Elvira.getElviraFrame().enableRedo(false);
   }


   /**
    * <P>Insert a set a link/links into the EditorPanel's network.
    * The variables used in this method are:</P>
    * <LI> arcHeadNode, is the node where the mouse has been released
    * <LI> selection, contains the current selection. In this case only can
    * have nodes. If have any link the methods finishes.
    * <LI> arcBottomNode, contains the node where the moused was pressed
    * before been dragged. This variable is necessary, because when a simple
    * node is going to be created the selection can be null, and we need this
    * variable to create the link.
    * <LI> addedLinks, is an internal variable that contains the links that must
    * be added. This variable will be used if the addition of all the links are
    * correct.
    * <LI> addLinksAction, added to the undoManager when the addition of the links
    * are finished. This operation will let undo/redo this operation
    */

   public void createLink() {
       LinkList addedLinks = new LinkList();
       int i = 0;
       boolean fail=false;
       
       if (selection.numberOfLinks()!=0)
           return;
       
       if ((arcBottomNode != null) && (selection.numberOfNodes() == 0))
           selection.addNode(arcBottomNode,zoom);
       
       while (selection.numberOfNodes()>i && !fail) {
           arcBottomNode = selection.getNode(i);
           arcBottomNode.setSelected(false);
           
           if (arcHeadNode == arcBottomNode) {
               //ElviraHelpMessages.show(frame, ElviraHelpMessages.selfarc);
               fail=true;
           }
           else{
               
               if (bayesNet.hasCycle(arcBottomNode, arcHeadNode)) {
                   Object[] names = {arcBottomNode.getName(), arcHeadNode.getName()};
                   ShowMessages.showMessageDialogPlus(
                   ShowMessages.CYCLE,
                   JOptionPane.ERROR_MESSAGE, names);
                   //ElviraHelpMessages.show(frame, ElviraHelpMessages.circular);
                   fail=true;
               }
               else {
                   if (bayesNet.getClass()==IDiagram.class){
                       fail = !IDiagram.isCompatibleLink(arcBottomNode,arcHeadNode);
                   }
                   else if (bayesNet.getClass()==IDWithSVNodes.class){
                       fail = !IDWithSVNodes.isCompatibleLink(arcBottomNode,arcHeadNode);
                   }
                   if (!fail){
                       addedLinks.insertLink(new Link(arcBottomNode, arcHeadNode));
                       i++;
                   }
               }
           }
       }
     arcHeadNode = null;
     arcBottomNode = null;
     newArcHead = null;
     newArc = false;

     if (!fail) {  // There is no problem with the links added
       for (Enumeration e = addedLinks.elements() ; e.hasMoreElements() ;) {
         Link l = (Link) e.nextElement();
         try {
           bayesNet.createLink(l.getTail(), l.getHead());  // <-- jgamez
         }
         catch (InvalidEditException iee) {};
       }

       setModifiedNetwork(true);

       /* Set the undo/redo action corresponding to
       the adition of links */

       AddLinksEdit addLinksAction = new AddLinksEdit(addedLinks);
       getUndoItem().setText(addLinksAction.getUndoPresentationName());
       getRedoItem().setText(addLinksAction.getRedoPresentationName());
       undoManager.addEdit(addLinksAction);
       Elvira.getElviraFrame().enableUndo(true);
       Elvira.getElviraFrame().enableRedo(false);

       selection = new Selection();

     }
   }

   public void deleteSelection() {

      for (int i=0; i<selection.numberOfLinks(); i++) {
          try {
              if ((arcHeadNode != null) && (arcBottomNode != null)) {
                  Node arcHead=bayesNet.getNodeList().getNode(arcHeadNode.getName());
                  bayesNet.removeLink(selection.getLink(i).getTail(),
                  selection.getLink(i).getHead());
                  Vector rlist=bayesNet.getRelationList();
                  boolean enc=false;
                  for (int j=0; j<rlist.size() && !enc ; j++) {
                      Relation r = (Relation) rlist.elementAt(j);
                      if ((((Node)r.getVariables().elementAt(0)).getName()).equals(arcHead.getName())){
                          enc=true;
                      }
                  }
                  if (enc){
                      LinkList linkl=arcHead.getParents();
                      for (Enumeration e=linkl.elements(); e.hasMoreElements(); ) {
                          Link link = (Link) e.nextElement();
                          link.setColorLink(ARC_COLOR);
                      }
                  }
              }
          }
         catch (InvalidEditException iee) {};
      }

      for (int i=0; i<selection.numberOfNodes(); i++) {
         Node n = selection.getNode(i);
         int j;

/*         while ((j=bayesNet.getLinkList().getID(n))!=-1) {
            Link l = bayesNet.getLinkList().elementAt(j);
            try {
               bayesNet.removeLink(j);
               selection.addLink(l);
            }
            catch (InvalidEditException iee) {};
         }
*/
         removeNodeFromNetwork(n);
      }
   }


   public void delete() {
      deleteSelection();

	   DeleteEdit deleteAction = new DeleteEdit(selection);
	   getUndoItem().setText(deleteAction.getUndoPresentationName());
	   getRedoItem().setText(deleteAction.getRedoPresentationName());
	   undoManager.addEdit(deleteAction);
	   Elvira.getElviraFrame().enableUndo(true);
	   Elvira.getElviraFrame().enableRedo(false);
   }


   public void move(Point p) {

      moveSelection(p);

	   MoveEdit moveAction = new MoveEdit(selection,p);
	   getUndoItem().setText(moveAction.getUndoPresentationName());
	   getRedoItem().setText(moveAction.getRedoPresentationName());
	   undoManager.addEdit(moveAction);
	   Elvira.getElviraFrame().enableUndo(true);
	   Elvira.getElviraFrame().enableRedo(false);
	}

   /* The next method are called when an undo operation is done */

   public void createNode(Node n) {
      try {
         bayesNet.addNode(n);
         bayesNet.addRelation(n);
         switch (n.getKindOfNode()) {
            case 0: chanceNodes++;
            case 1: decisionNodes++;
            case 2: utilityNodes++;
         }
      }
      catch (InvalidEditException iee) {};
   }

   public void createLink (Node arcBottomNode, Node arcHeadNode) {
      try {
         bayesNet.createLink(arcBottomNode, arcHeadNode);
      }
      catch (InvalidEditException iee) {};
   }

   public void removeNodeFromNetwork(Node n) {
      bayesNet.removeNode(n);
      switch (n.getKindOfNode()) {
         case 0: chanceNodes--;
         case 1: decisionNodes--;
         case 2: utilityNodes--;

      }
   }

   public void removeLink(Node arcBottomNode, Node arcHeadNode) {
      try {
         bayesNet.removeLink(arcBottomNode, arcHeadNode);
      }
      catch (InvalidEditException iee) {};
   }


   public void checkSelected() {
      if (selection.numberOfNodes()==0 && selection.numberOfLinks()==0)
         Elvira.getElviraFrame().enableCutCopy(false);
      else
         Elvira.getElviraFrame().enableCutCopy(true);
   }


   /* ******************************************* */
   /* **** Auxiliary Methods for undo / redo **** */
   /* ******************************************* */


   /**
    * Add the Selection contained in the variables to the
    * Network displayed in the EditorPanel
    */

   public void addSelection(Selection s) {
      for (int i=0; i<s.numberOfNodes(); i++) {
         Node n = s.getNode(i);
         createNode(n);
      }
      for (int i=0; i<s.numberOfLinks(); i++) {
         Link l = s.getLink(i);
         try {
            bayesNet.createLink(l.getTail(), l.getHead());
         }
         catch (InvalidEditException iee) {};
      }

   }


   /**
    * Manage all the actions related with the UndoManager that must
    * be done when the Cut action is produced. Normally this method
    * will be called from the ElviraFrame object because it is here where
    * the PasteAction is produced.
    */

   public void undoCutAction() {
  	   CutEdit cutAction = new CutEdit(selection);
	   getUndoItem().setText(cutAction.getUndoPresentationName());
	   getRedoItem().setText(cutAction.getRedoPresentationName());
	   undoManager.addEdit(cutAction);
	   Elvira.getElviraFrame().enableUndo(true);

   }


   /**
    * Manage all the actions related with the UndoManager that must
    * be done when the Paste action is produced. Normally this method
    * will be called from the ElviraFrame object because it is here where
    * the PasteAction is produced.
    *
    * @param s The Selection object that has been pasted
    */

   public void undoPasteAction(Selection s) {
  	   PasteEdit PasteAction = new PasteEdit(s);
	   getUndoItem().setText(PasteAction.getUndoPresentationName());
	   getRedoItem().setText(PasteAction.getRedoPresentationName());
	   undoManager.addEdit(PasteAction);
	   Elvira.getElviraFrame().enableUndo(true);

   }


   public void selectAll () {
      selection = new Selection();
      for (int i=0; i<bayesNet.getNodeList().size(); i++) {
         Node n = (Node) bayesNet.getNodeList().elementAt(i);
         selection.addNode(n,zoom);
         n.setSelected(true);
      }

      for (int i=0; i<bayesNet.getLinkList().size(); i++) {
         Link l = (Link) bayesNet.getLinkList().elementAt(i);
         selection.addLink(l);
         l.setSelected(true);
      }

      mode = MOVE_MODE;
      Elvira.getElviraFrame().activeSelect();
      repaint();
   }


	/**
	 * Manage the actions produced in the ElviraPopupMenu object
	 */

	class EditorPopupAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == editMenuItem)
			  editMenuItem_actionPerformed(event);
			else if (object == explainMenuItem)
                          explainMenuItem_actionPerformed(event);
                        else if (object == nSCMenuItem)
                          nSCMenuItem_actionPerformed(event);
                        else if (object == nSCToLinkMenuItem)
                          nSCToLinkMenuItem_actionPerformed(event);
                        else if (object == testDecisionMenuItem)
                          testDecisionMenuItem_actionPerformed(event);
			else if (object == deleteMenuItem)
                          deleteMenuItem_actionPerformed(event);
                        else if (object == cutMenuItem)
                          Elvira.getElviraFrame().cutAction(event);
                        else if (object == copyMenuItem)
                          Elvira.getElviraFrame().copyAction(event);
                        else if (object == pasteMenuItem)
                          Elvira.getElviraFrame().pasteAction(event);
		}
	}


	void deleteMenuItem_actionPerformed(java.awt.event.ActionEvent event)
	{
            //if ((arcHeadNode != null) && (arcBottomNode != null)) {
                deleteSelection();
                repaint();
            //}
	}

        void nSCMenuItem_actionPerformed(java.awt.event.ActionEvent event)
        {
          EditNonSenseConstraints eNSC = new EditNonSenseConstraints((IDiagram) bayesNet,(FiniteStates) currentNode,(FiniteStates) null);
          eNSC.show();
        }

        void nSCToLinkMenuItem_actionPerformed(java.awt.event.ActionEvent event)
        {
          EditNonSenseConstraints eNSC = new EditNonSenseConstraints((IDiagram) bayesNet,(FiniteStates) currentLink.getTail(),(FiniteStates) currentLink.getHead());
          eNSC.isAccepted();
          eNSC.dispose();
        }

        void testDecisionMenuItem_actionPerformed(java.awt.event.ActionEvent event)
        {
          /* TO DO */
          if (currentNode == null) {
            return;
          }
          String name = new String("Dec:_"+currentNode.getName());
          bayesNet.createNode(currentNode.getPosX()-120,currentNode.getPosY(),"Helvetica",
                              name,Node.DECISION);
          Node n = bayesNet.getNodeList().lastElement();
          FontMetrics fm=getFontMetrics(getFont(n.getFont()));
          VisualNode.setAxis(n,n.getNodeString(byTitle),fm);

          try {
            bayesNet.createLink(bayesNet.getNode(name),currentNode);
          }
          catch (InvalidEditException iee) {};

          boolean withUtility = false;
          Node theNode = null;
          for (int i=0; i<bayesNet.getNodeList().size(); i++) {
            if (((Node) bayesNet.getNodeList().getNodes().elementAt(i)).getKindOfNode() == Node.UTILITY) {
              withUtility = true;
              theNode = (Node) bayesNet.getNodeList().getNodes().elementAt(i);
            }
          }

          withUtility = false;

          if (withUtility) {
            try {
              bayesNet.createLink(currentNode,theNode);
            }
            catch (InvalidEditException iee) {};
          }
          else {
            String uName = new String(bayesNet.generateSpecialName("Coste:_"+currentNode.getName(),utilityNodes++));
            bayesNet.createNode(currentNode.getPosX()-100, currentNode.getPosY()+100,"Helvetica",
                                uName,Node.UTILITY);
            n = bayesNet.getNodeList().lastElement();
            fm=getFontMetrics(getFont(n.getFont()));
            VisualNode.setAxis(n,n.getNodeString(byTitle),fm);
            try {
              bayesNet.createLink(bayesNet.getNode(name),bayesNet.getNode(uName));
            }
            catch (InvalidEditException iee) {};
          }

          EditNonSenseConstraints eNSC = new EditNonSenseConstraints((IDiagram) bayesNet,(FiniteStates) bayesNet.getNode(name),(FiniteStates) currentNode);
          eNSC.isAccepted();
          eNSC.dispose();

          currentNode.setPurpose(localize(dialogBundle, "Test"));
          
          unSelectAll();
          repaint();
        }

        /* THE NEXT CLASSES ARE USED FOR THE UNDOMANAGER TO STORES AND
         * RECOVER THE ACTIONS THAT TAKE PLACE IN THE EDITOR PANEL */


	/**
	 * This class will be used for the UndoManager for undo/redo
	 * the add node action
	 */

	class AddNodeEdit extends AbstractUndoableEdit {
	   Node addedNode;

	   public AddNodeEdit (Node n){
	      super();
	      addedNode = n;
	   }

	   public void undo() throws CannotUndoException {
	      super.undo();
              removeNodeFromNetwork(addedNode);
              updateUndoRedo();
	   }

	   public void redo() throws CannotRedoException {
	      super.redo();
	      createNode(addedNode);
	      updateUndoRedo();
	   }

	   public String getUndoPresentationName() {
	      return localize (menuBundle, "Edit.Undo.label")+" "+
	             localize(menuBundle, "Action.addNode.label");
	   }

	   public String getRedoPresentationName() {
	      return localize (menuBundle, "Edit.Redo.label")+" "+
	             localize(menuBundle, "Action.addNode.label");
	   }

	}


	/**
	 * This class will be used for the UndoManager for undo/redo
	 * the add link action
	 */

	class AddLinksEdit extends AbstractUndoableEdit {
	   LinkList addedLinks;

	   public AddLinksEdit (LinkList list) {
	      super();
	      addedLinks = list;
	   }

	   public void undo() throws CannotUndoException {
	      super.undo();
              for (Enumeration e = addedLinks.elements() ; e.hasMoreElements() ;) {
                Link l = (Link) e.nextElement();
                try {
                  bayesNet.removeLink(l.getTail(),l.getHead());
                }
                catch (InvalidEditException iee) {};
              }
              updateUndoRedo();
	   }

	   public void redo() throws CannotRedoException {
	      super.redo();
              for (Enumeration e = addedLinks.elements() ; e.hasMoreElements() ;) {
                Link l = (Link) e.nextElement();
                try {
                  bayesNet.createLink(l.getTail(),l.getHead());
                }
                catch (InvalidEditException iee) {};
              }
              updateUndoRedo();
           }

	   public String getUndoPresentationName() {
	      return localize (menuBundle, "Edit.Undo.label")+" "+
	             localize(menuBundle, "Action.addLink.label");
	   }

	   public String getRedoPresentationName() {
	      return localize (menuBundle, "Edit.Redo.label")+" "+
	             localize(menuBundle, "Action.addLink.label");
	   }

	}


	/**
	 * This class will be used for the UndoManager for undo/redo
	 * a delete action
	 */

	class DeleteEdit extends AbstractUndoableEdit {
	   Selection deleted;

	   public DeleteEdit (Selection s){
	      super();
	      deleted = s;
	   }

	   public void undo() throws CannotUndoException {
             FontMetrics fm;
	      super.undo();
              for (int i=0; i<deleted.numberOfNodes(); i++) {
                Node n = deleted.getNode(i);
                n.setSelected(false);
                fm=getFontMetrics(getFont(n.getFont()));
                VisualNode.setAxis(n,n.getName(),fm);

                createNode (n);
              }
              for (int i=0; i<selection.numberOfLinks(); i++) {
                Link l = deleted.getLink(i);
                try {
                  bayesNet.createLink(l.getTail(), l.getHead());
                }
                catch (InvalidEditException iee) {};
              }
              updateUndoRedo();
	   }

	   public void redo() throws CannotRedoException {
	      super.redo();
	      selection = deleted;
              if ((arcHeadNode != null) && (arcBottomNode != null)) {
                deleteSelection();
              }
	      updateUndoRedo();
	   }

	   public String getUndoPresentationName() {
	      return localize (menuBundle, "Edit.Undo.label")+" "+
	             localize(menuBundle, "Action.delete.label");
	   }

	   public String getRedoPresentationName() {
	      return localize (menuBundle, "Edit.Redo.label")+" "+
	             localize(menuBundle, "Action.delete.label");
	   }

	}


	/**
	 * This class will be used for the UndoManager for undo/redo
	 * the cut action
	 */

	class CutEdit extends AbstractUndoableEdit {
	   Selection cut;
           FontMetrics fm;

	   public CutEdit (Selection s){
	      super();
	      cut = s;
	   }

	   public void undo() throws CannotUndoException {
	      super.undo();
              for (int i=0; i<cut.numberOfNodes(); i++) {
                Node n = cut.getNode(i);
                n.setSelected(false);
                fm=getFontMetrics(getFont(n.getFont()));
                VisualNode.setAxis(n,n.getName(),fm);
                createNode (n);
              }
              for (int i=0; i<selection.numberOfLinks(); i++) {
                Link l = cut.getLink(i);
                try {
                  bayesNet.createLink(l.getTail(), l.getHead());
                }
                catch (InvalidEditException iee) {};
              }
              updateUndoRedo();
           }

	   public void redo() throws CannotRedoException {
	      super.redo();
	      selection = cut;
              if ((arcHeadNode != null) && (arcBottomNode != null)) {
                deleteSelection();
              }
	      updateUndoRedo();
	   }

	   public String getUndoPresentationName() {
	      return localize (menuBundle, "Edit.Undo.label")+" "+
	             localize(menuBundle, "Action.cut.label");
	   }

	   public String getRedoPresentationName() {
	      return localize (menuBundle, "Edit.Redo.label")+" "+
	             localize(menuBundle, "Action.cut.label");
	   }

	}


	/**
	 * This class will be used for the UndoManager for undo/redo
	 * the paste action
	 */

	class PasteEdit extends AbstractUndoableEdit {
	   Selection pasted;
           FontMetrics fm;

	   public PasteEdit (Selection s){
	      super();
	      pasted = s;
	   }

	   public void undo() throws CannotUndoException {
	      super.undo();
	      selection = pasted;
              if ((arcHeadNode != null) && (arcBottomNode != null)) {
                deleteSelection();
              }
              updateUndoRedo();
	   }

	   public void redo() throws CannotRedoException {
	      super.redo();
              for (int i=0; i<pasted.numberOfNodes(); i++) {
                Node n = pasted.getNode(i);
                n.setSelected(false);
                fm=getFontMetrics(getFont(n.getFont()));
                VisualNode.setAxis(n,n.getName(),fm);
                createNode (n);
              }
              for (int i=0; i<pasted.numberOfLinks(); i++) {
                Link l = pasted.getLink(i);
                try {
                  bayesNet.createLink(l.getTail(), l.getHead());
                }
                catch (InvalidEditException iee) {};
              }
              updateUndoRedo();
           }

           public String getUndoPresentationName() {
             return localize (menuBundle, "Edit.Undo.label")+" "+
                 localize(menuBundle, "Action.paste.label");
           }

	   public String getRedoPresentationName() {
	      return localize (menuBundle, "Edit.Redo.label")+" "+
	             localize(menuBundle, "Action.paste.label");
	   }

	}
           
        
        
}  // end of EditorPanel class

