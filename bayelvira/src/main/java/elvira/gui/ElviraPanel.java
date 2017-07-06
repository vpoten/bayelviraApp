/* ElviraPanel.java */

package elvira.gui;


import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.undo.*;
import java.util.*;
import elvira.*;
import elvira.gui.explication.*;


/**
 * Contains a standard work area with the basic methods
 * for changing the network.
 *
 * @author ..., fjdiez, ratienza, ...
 * @since 14/2/00
 * @version 0.1
 */

public class ElviraPanel extends JPanel {

   /**  Store the mode for events in the panel */
   protected Bnet bayesNet;
   protected Node currentNode;
   protected Link currentLink;
   protected boolean modifiedNetwork = false;
   protected ResourceBundle menuBundle;
   protected ResourceBundle dialogBundle;

   protected Dimension editorSize;

   /**
    * Contains the list of nodes and links that are currently
    * selected in the EditorPanel
    */
   protected Selection selection;

   /**
    * If its value is True the titles of the nodes will be
    * displayed. If the value is False the names of the nodes
    * will be displayed
    */
   protected boolean byTitle;

   protected boolean dragging = false;
   protected Node nodeToMove = null;
   protected Vector oldNodePositions = new Vector();
   protected Point startDragPosition = null;
   protected Node arcBottomNode = null;
   protected Node arcHeadNode = null;
   protected boolean propertiesEditable;

   // constants for drawing entities
   protected final int NODE_SIZE  = 26;
   protected final int NODE_RADIUS = 13;
   protected final double DISTANCE_HIT_ARC = 200.0;
   protected final int SQUARE_SIDE = 7;

   public static final String EXPLAIN_NODE = "Explain Node";
   public static final String EXPLAIN_LINK = "Explain Link";
   public static final String EXPLAIN_NETWORK = "Explain Network";
   // color constants for various graphical elements
   protected static final Color NODE_COLOR = new Color(255,255,200);
   protected static final Color RED = Color.red;
   protected static final Color WHITE = Color.white;
   protected static final Color OBSERVED_NODE_COLOR = Color.lightGray;
   protected static final Color MORE_EXPLANATION_NODE_COLOR = new Color(255,102,102);
   protected static final Color LESS_EXPLANATION_NODE_COLOR = new Color(102,153,255);
   protected static final Color UNKOWN_EXPLANATION_NODE_COLOR = new Color(204,102,255);
   protected static final Color EQUALS_EXPLANATION_NODE_COLOR = NODE_COLOR;
   protected static final Color NODE_BORDER_COLOR = Color.black;
   protected static final Color NODE_NAME_COLOR = Color.black;
   protected static final Color OBSERVED_NAME_COLOR = Color.white;
   public static final Color ARC_COLOR 	= Color.darkGray;
   public static final Color GREATER_ARC_COLOR 	= new Color(255,102,102);//rojo
   public static final Color LESS_ARC_COLOR 	= new Color(102,153,255);//azul
   public static final Color EQUALS_ARC_COLOR 	= new Color(204,102,255);//violeta
   protected static final Color BACKGROUND_COLOR = Color.white;

   // Zoom variables
   protected double zoom = 1.0;

   // Differents shapes for lines
   public final static BasicStroke stroke = new BasicStroke(1.0f);
   public final static BasicStroke wideStroke = new BasicStroke(4.0f);
   public final static float dash1[] = {10.0f};
   public final static BasicStroke dashed = new BasicStroke(1.0f,
                                                   BasicStroke.CAP_BUTT,
                                                   BasicStroke.JOIN_MITER,
                                                   10.0f, dash1, 0.0f);

   // fonts
   public final static Font ROMAN	= new Font("TimesRoman", Font.BOLD, 12);
   public final static Font HELVETICA = new Font("Helvetica", Font.BOLD, 15);
   public final FontMetrics FMETRICS = getFontMetrics(HELVETICA);
   protected int HEIGHT = (int)FMETRICS.getHeight()/3;

   // for double buffering
   public Image offScreenImage;
   public Graphics2D offScreenGraphics;
   public Dimension offScreenSize;


   /* For undoing actions */
   protected UndoManager undoManager;



   /**
    * Default constructor for NetworkPanel object.
    *
    * @param frame ElviraFrame
    */

   ElviraPanel() {
     menuBundle = Elvira.getElviraFrame().getMenuBundle();
     dialogBundle = Elvira.getElviraFrame().getDialogBundle();
     bayesNet = new Bnet();
     Elvira.getElviraFrame().enableUndo(false);
     editorSize = new Dimension (0, 0);

     // set color for background
     setBackground(BACKGROUND_COLOR);
     setVisible(true);

     //{{INIT_CONTROLS
     setAutoscrolls(true);
     setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
     setBackground(java.awt.Color.white);
     setSize(0,0);
     setOpaque(false);
     setByTitle(true);
   }


   /**
    * For accessing to the bayesNet loaded in the frame
    *
    * @return A Bnet object with the bayessian network of the
    * current frame
    */

   public Bnet getBayesNet () {
     return bayesNet;
   }


   /**
    * Obtain the zoom of the Network displayed in the panel
    */

   public double getZoom() {
      return zoom;
   }


   /**
    * To know if the nodes will be displayed by name or by title
    */

   public boolean getByTitle() {
      return byTitle;
   }


   /**
    * Set the nodes and links that must appear in the Panel
    */

   public void setSelection (Selection s) {
      selection = s;
   }


   /**
    * Get the nodes and links selected in the Panel
    */

   public Selection getSelection () {
     return selection;
   }


   /**
    * Set the modified variable.
    *
    * @param mod Contain the value to set
    * @param m
    */

   public void setModifiedNetwork (boolean m) {
     modifiedNetwork = m;
   }


   public UndoManager getUndoManager() {
      return undoManager;
   }


   public String localize (ResourceBundle bundle, String name) {
      return ElviraFrame.localize(bundle, name);
   }


   /**
    * Set the Bayessian Network to be displayed in the editor
    */

    public void setBayesNet (Bnet bn) {
      bayesNet = bn;
    }


   /**
    * Set a new zoom for the panel
    */

    public void setZoom (double x) {
      zoom = x;
    }


    /**
     * Set the value of the byTitle variable. This value will
     * be True if the titles of the nodes will be displayed in
     * the editor and False if the names will be displayed.
     */

    public void setByTitle (boolean b) {
      byTitle = b;
      setStrings();
    }


    public void setUndoManager (UndoManager um) {
      undoManager = um;
    }


    public JMenuItem getUndoItem() {
      return Elvira.getElviraFrame().getUndoItem();
    }


    public JMenuItem getRedoItem() {
      return Elvira.getElviraFrame().getRedoItem();
    }


    protected void updateUndoRedo() {
      if (undoManager.canUndo()) {
        Elvira.getElviraFrame().enableUndo(true);
        getUndoItem().setText(undoManager.getUndoPresentationName());
      }
      else {
        Elvira.getElviraFrame().enableUndo(false);
        getUndoItem().setText(localize(menuBundle, "Edit.Undo.label"));
      }

      if (undoManager.canRedo()) {
        Elvira.getElviraFrame().enableRedo(true);
        getRedoItem().setText(undoManager.getRedoPresentationName());
      }
      else {
        Elvira.getElviraFrame().enableRedo(false);
        getRedoItem().setText(localize(menuBundle, "Edit.Redo.label"));
      }
    }


    /**
     * <P>This method is used when a new node is created or a node
     * is moved. </P>
     * <P>Ensures that the coordinates of the node are higher that
     * that the node radius.
     * @return The validate point
     */

    public Point validateCoordinates (int x, int y) {
      if (x<NODE_RADIUS+2) x = NODE_RADIUS+2;
      if (y<NODE_RADIUS+10) y = NODE_RADIUS+10;
      return (new Point(x,y));
    }


    /**
     * Determine whether a node was hit by a mouse click.
     *
     * @param x X Position of the mouse click
     * @param y Y Position of the mouse click
     * @return The node hitted
     */

    protected Node nodeHit(int x, int y) {
      Node node;
      int a, b,x1,y1;

      for (int i=bayesNet.getNodeList().size()-1; i>=0; i--) {
        node = (Node) bayesNet.getNodeList().elementAt(i);
        int kindOfNode = node.getKindOfNode();
		if ((kindOfNode==Node.CHANCE)||(kindOfNode==Node.DECISION)||(kindOfNode==Node.OBSERVED)) {
          if (node.getExpanded() && getClass()==InferencePanel.class) {
            RoundRectangle2D rect = new RoundRectangle2D.Double (
                node.getPosX()-VisualNode.HALF_EXPANDED_WIDTH,
                node.getPosY()-VisualNode.getHalfHeight(node),
                VisualNode.CHANCE_NODE_EXPANDED_WIDTH,
                VisualNode.getHeight(node),30, 30);
            if (rect.contains (x, y))
              return node;
          }
          else {
            Ellipse2D ellipse = new Ellipse2D.Double(
                node.getPosX()-node.getHigherAxis()/2,
                node.getPosY()-node.getLowerAxis()/2,
                node.getHigherAxis(),
                node.getLowerAxis());

            if (ellipse.contains(x, y))
              return node;
          }
        }
        else if ((kindOfNode==Node.UTILITY)||(kindOfNode==Node.SUPER_VALUE)) {
          int halfXAxis, halfYAxis;
          int x2,x3,x4,y2;

          if (node.getExpanded() && getClass()==InferencePanel.class) {
            halfXAxis = VisualNode.UTILITY_HALF_EXPANDED_WIDTH;
            halfYAxis = VisualNode.UTILITY_HALF_EXPANDED_HEIGHT;
            x1 = node.getPosX()-halfXAxis-6;
            y1 = node.getPosY()-halfYAxis;

            x2 = node.getPosX()-halfXAxis+6;
            x3 = node.getPosX()+halfXAxis-6;
            x4 = node.getPosX()+halfXAxis+6;
            y2 = node.getPosY()+halfYAxis;
          }
          else {
            halfXAxis = node.getHigherAxis()/2;
            halfYAxis = node.getLowerAxis()/2;

            x1 = node.getPosX()-halfXAxis-3;
            y1 = node.getPosY()-halfYAxis-3;

            x2 = node.getPosX()-halfXAxis+3;
            x3 = node.getPosX()+halfXAxis-3;
            x4 = node.getPosX()+halfXAxis+3;
            y2 = node.getPosY()+halfYAxis-6;
          }

          int x1Points[] = {x1, x2, x3, x4, x3, x2, x1};
          int y1Points[] = {node.getPosY()-4, y1, y1,
            node.getPosY()-4, y2, y2,
            node.getPosY()-4};

          GeneralPath polygon = new
                                GeneralPath(GeneralPath.WIND_EVEN_ODD,
                                x1Points.length);

          polygon.moveTo(x1Points[0], y1Points[0]);

          for (int index = 1; index < x1Points.length; index++) {
            polygon.lineTo(x1Points[index],
            y1Points[index]);
          }
          polygon.closePath();

          if (polygon.contains (x, y))
            return node;
        }
        else if (kindOfNode==Node.DECISION) {
          Rectangle2D rect;
          if (node.getExpanded() && getClass()==InferencePanel.class)
            rect = new Rectangle2D.Double (
                node.getPosX()-VisualNode.DECISION_HALF_EXPANDED_WIDTH,
                node.getPosY()-VisualNode.DECISION_HALF_EXPANDED_HEIGHT,
                VisualNode.DECISION_NODE_EXPANDED_WIDTH,
                VisualNode.DECISION_NODE_EXPANDED_HEIGHT);
          else
            rect = new Rectangle2D.Double(
                node.getPosX()-node.getHigherAxis()/2,
                node.getPosY()-node.getLowerAxis()/2,
                node.getHigherAxis(),
                node.getLowerAxis());

          if (rect.contains(x, y))
            return node;
        }
      }

      return(null);
    }


   /**
    * Determine whether an arc was hit by a mouse click.
    *
    * @param x X-position of the mouse click
    * @param y Y-position of the mouse click
    */

   protected void arcHit(int x, int y) {
     Node hnode, pnode;
     double sdpa;
     NodeList parents; // <--- jgamez
     Graph gr;         // <--- jgamez

     for (Enumeration e = bayesNet.getNodeList().elements(); e.hasMoreElements(); ) {
       hnode = (Node)(e.nextElement());
       gr = new Graph(bayesNet.getNodeList(),bayesNet.getLinkList(),Graph.DIRECTED);
       parents = (NodeList) gr.parents(hnode);
       for (Enumeration ee = parents.elements(); ee.hasMoreElements(); ) {
         pnode = (Node)(ee.nextElement());
         sdpa = squareDistancePointArc(hnode, pnode, x, y);
         if ((sdpa >= 0.0) && (sdpa <= DISTANCE_HIT_ARC)) {
           arcHeadNode = hnode;
           arcBottomNode = pnode;
         }
       }
     }
   }


   /**
     * Determine whether a point is close to the segment
     * between two nodes (hnode and pnode); if the point
     * does not lie over or above the segment, return -1.0
     *
     * @param hnode Head node
     * @param pnode Tail node
     * @param x3 X-position of the point
     * @param y3 Y-position of the point
     */

   protected double squareDistancePointArc(Node hnode,Node pnode, int x3, int y3) {
      int x1, y1, x2, y2;
       double area, squareBase, squareHeight, squareHyp;

       x1 = hnode.getPosX();  // las siguientes 4 descomentadas por jgamez
       y1 = hnode.getPosY();
       x2 = pnode.getPosX();
       y2 = pnode.getPosY();

       // Area of the triangle defined by the three points
       area = (double)(x1 * y2 + y1 * x3 + x2 * y3 -
                       x3 * y2 - y3 * x1 - x2 * y1);
       // Base of the triangle
       squareBase = (double)( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );

       // Height of the triangle
       squareHeight = 4.0 * (area*area) / squareBase;

       // Maximum possible distance from point to extreme points
       squareHyp = squareBase + squareHeight;

       // Check first extreme point
       if (squareHyp < ((double)( (x3-x1)*(x3-x1) + (y3-y1)*(y3-y1)) ))
         return(-1.0);
       // Check second extreme point
       if (squareHyp < ((double)( (x3-x2)*(x3-x2) + (y3-y2)*(y3-y2)) ))
         return(-1.0);

         // Requested distance is the height of the triangle
         return(squareHeight );
    }


   /**
    * Update the screen with the network.
    *
    * @param g Graphics interface
    */

   public synchronized void update(Graphics g) {
     Graphics2D g2 = (Graphics2D) g;
     // prepare new image offscreen

     Dimension d=getSize();
     MediaTracker tracker;
     if ( (offScreenImage == null) ) {
       offScreenImage =createImage(getPreferredSize().width,
                                   getPreferredSize().height);
       tracker = new MediaTracker(this);
       try { // wait to image to be constructed
         tracker.addImage(offScreenImage, 0);
         tracker.waitForID(0,0);
       }
       catch (InterruptedException e) {
       }
       offScreenSize=getPreferredSize();
       offScreenGraphics = (Graphics2D) offScreenImage.getGraphics();
     }
     offScreenGraphics.setColor(BACKGROUND_COLOR);
     //	   offScreenGraphics.fillRect(0, 0, preferredSize.width, preferredSize.height);
     paint(offScreenGraphics);
     g2.drawImage(offScreenImage, 0, 0, null);
   }


   /**
    * Move the node n to the position given as parameter. It is supposed
    * that the n variable is not null - this must be done before calling
    * this method
    */

   public void moveNode(Point p, Node n) {
     if (p.x < NODE_RADIUS)
       p.x = NODE_RADIUS;
     else
       if (p.x+NODE_RADIUS > getSize().width)
         p.x = getSize().width-NODE_RADIUS;

     if (p.y < NODE_RADIUS)
       p.y = NODE_RADIUS;
     else
       if (p.y+NODE_RADIUS > getSize().height)
         p.y = getSize().height-NODE_RADIUS;

     if (n!= null) {
       n.setPosX(p.x);
       n.setPosY(p.y);
     }
   }


   /**
    * Moved the nodes contained in the Selection variable the
    * (X,Y) distance contained in the point given as parameter
    */

   public void moveSelection(Point distanceMoved) {
     oldNodePositions = new Vector();

     for (int i=0; i<selection.numberOfNodes() ;i++) {
       Node n = selection.getNode(i);
       Point p = new Point (n.getPosX()+distanceMoved.x,n.getPosY()+distanceMoved.y);
       moveNode (p,n);

       // The node position could be different of p, so p is set

       p = new Point (n.getPosX(),n.getPosY());
       oldNodePositions.addElement(p);
     }
   }



/* ************** FUNCTIONS USED FOR SELECT-UNSELECT ************** */



   /**
    * Set the selected variable of all nodes and links to
    * false, and clear the oldNodePositions and selection
    * variables
    */

   public void unSelectAll () {
     int i;
     for (i=0; i<bayesNet.getNodeList().size(); i++) {
       bayesNet.getNodeList().elementAt(i).setSelected(false);
     }

     for (i=0; i<bayesNet.getLinkList().size(); i++) {
       bayesNet.getLinkList().elementAt(i).setSelected(false);
     }

     oldNodePositions = new Vector();
     selection = new Selection();
   }


   /**
    * Calculates the new size of the editorPanel using the
    * highest x-position and y-position given as parameter
    *
    * @see refreshScrollPane
    */

   public void refreshElviraPanel (Point max, Point min) {
      boolean changed_width=false, changed_height=false;
      int this_width = max.x + 10;

      //if (this_width > editorSize.width)
      {
         editorSize.width = this_width;
         changed_width=true;
      }
      int this_height = max.y + 10;
      //if (this_height > editorSize.height)
      {
         editorSize.height = this_height;
         changed_height=true;
      }
      if (changed_height || changed_width) {
         //Update client's preferred size because
         //the area taken up by the graphics has
         //gotten larger or smaller (if cleared).
         setPreferredSize(editorSize);
         //Let the scroll pane know to update itself
         //and its scrollbars.
         revalidate();
      }
      NetworkFrame n = (NetworkFrame)Elvira.getElviraFrame().
                       getCurrentNetworkFrame();
      n.refreshScrollPane(max,min);
   }


   /**
    * Calculates the new size of the editorPanel using the
    * highest x-position and y-position of the nodes displayed
    * in it.
    */


   public void refreshElviraPanel(double zoom) {
     Point max = getMaximumPosition(),
     min = new Point(0,0);
     max.x = (int) (max.x*zoom);
     max.y = (int) (max.y*zoom);
     refreshElviraPanel (max, min);
   }


   /**
    * @return A Point with the maximum position that must be
    * displayed in the EditorPanel.
    */

   public Point getMaximumPosition() {
     Point p = new Point (0,0);
     for (int i=0; i<bayesNet.getNodeList().size(); i++) {
       Node n = (Node) bayesNet.getNodeList().elementAt(i);
       //if (n.getPosX()+n.getWidth()>p.x)
       if (n.getPosX()+VisualNode.getWidth(n)>p.x)
         //   p.x = n.getPosX()+n.getWidth();
         p.x = n.getPosX()+VisualNode.getWidth(n);
       // if (n.getPosY()+n.getHeight()>p.y)
       if (n.getPosY()+VisualNode.getHeight(n)>p.y)
         //    p.y = n.getPosY()+n.getHeight();
         p.y = n.getPosY()+VisualNode.getHeight(n);
     }
     return p;
   }


   /**
    * Get all the nodes in the editor panel whose position
    * is in the rectangle definied by the two points given
    * as parameter. </P>
    * <P>Note: The x,y coordinates of the end point must be
    * higher than the x,y coordinates of the begin point
    */

   public void areaSelected(Point begin, Point end) {
     Rectangle2D area = new Rectangle2D.Double (begin.x, begin.y, end.x-begin.x, end.y-begin.y);
     oldNodePositions = new Vector();
     // Get the nodes contained in the area

     for (int i=0; i<bayesNet.getNodeList().size(); i++) {
       Node n = (Node) bayesNet.getNodeList().elementAt(i);
       if (area.contains(n.getPosX(), n.getPosY())) {
         n.setSelected(true);
         selection.addNode(n, zoom);
         oldNodePositions.addElement(
             new Point (n.getPosX(), n.getPosY()));
       }
     }

     /* Now, get the links. This operation is necessary
     because if we don't do this the links will not
     appear as selected */

     for (int i=0; i<bayesNet.getLinkList().size(); i++) {
       Link l = (Link) bayesNet.getLinkList().elementAt(i);
       if (area.contains(l.getHead().getPosX(),l.getHead().getPosY()) && // || /* MODIFIED */
           area.contains(l.getTail().getPosX(),l.getTail().getPosY())) {
         l.setSelected(true);
         selection.addLink(l);
       }
     }
   }


   protected void explainMenuItem_actionPerformed(java.awt.event.ActionEvent event)
   {
     if (event.getActionCommand().equals(EXPLAIN_NODE)) {
       if (selection.numberOfNodes()>0) {
         Node n = selection.getNode(0);
         ExplainNodeDialog d = new ExplainNodeDialog ((FiniteStates)n,n.getName());
         d.setVisible(true);
       }
     }
     else if (event.getActionCommand().equals(EXPLAIN_LINK)) {
       if (selection.numberOfLinks()>0) {
         Link l = selection.getLink(0);
         ExplainLinkDialog d = new ExplainLinkDialog(l);
         d.setVisible(true);
       }
     }
     else if (event.getActionCommand().equals(EXPLAIN_NETWORK)){
       Bnet bn=Elvira.getElviraFrame().getNetworkFrame().getInferencePanel().getBayesNet();
       System.out.println(bn.getTitle());
       ExplainNetDialog d=new ExplainNetDialog(bn);
       d.setVisible(true);
     }
   }


   /**
    * Controles the action that must be produced when the
    * edit choice of the popup menu of the EditorPanel is clicked.¡
    * Depending of the object where the popup menu is diplayed it
    * must: </P>
    * <LI> Create and show an EditVariableDialog </LI>
    * <LI> Create and show an EditLinkDialog </LI>
    * <LI> Create and show the NetworkPropertiesDialog </LI>
    */

   void editMenuItem_actionPerformed(java.awt.event.ActionEvent event)
   {
     if (currentNode!=null && event.getActionCommand().equals("Edit Node")) {
       EditVariableDialog d = new EditVariableDialog(currentNode,
           byTitle,localize(dialogBundle, "EditVariable.Node.label")+": "+currentNode.getNodeString(byTitle), propertiesEditable);
       d.show();
       setModifiedNetwork(true);
       repaint();
     }
     else if (event.getActionCommand().equals("Edit Link")) {
       LinkPropertiesDialog d = new LinkPropertiesDialog(currentLink, propertiesEditable);
       d.show();
       setModifiedNetwork(true);
     }
     else {
       EditorPanel e = ((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel();
       NetworkPropertiesDialog d = new NetworkPropertiesDialog(
           null, false, e.getBayesNet(), propertiesEditable);
       d.show();
       d.dispose();
       setModifiedNetwork(true);
     }
   }


   /**
    * This class will be used for the UndoManager for undo/redo
    * the move action
    */

   class MoveEdit extends AbstractUndoableEdit {
     Selection moved;
     Point distanceMoved;

     public MoveEdit (Selection s, Point p) {
       super();
       moved = (Selection) s;
       distanceMoved = p;
     }

     public void undo() throws CannotUndoException {
       super.undo();
       selection = moved;
       moveSelection (new Point (0-distanceMoved.x,0-distanceMoved.y));
       updateUndoRedo();
     }

     public void redo() throws CannotRedoException {
       super.redo();
       selection = moved;
       moveSelection (distanceMoved);
       updateUndoRedo();
     }

     public String getUndoPresentationName() {
       return localize (menuBundle, "Edit.Undo.label")+" "+
           localize(menuBundle, "Action.move.label");
     }

     public String getRedoPresentationName() {
       return localize (menuBundle, "Edit.Redo.label")+" "+
           localize(menuBundle, "Action.move.label");
     }
   }


   public static Font getFont(String fname){
     if(fname.equals("Helvetica")){
       return HELVETICA;
     }
     else if(fname.equals("Roman")){
       return ROMAN;
     }
     else{
       System.out.println("Font name "+fname+" not kown");
       System.exit(1);
     }
     return HELVETICA;
   }

   /**
    * Sets all the nodes with the correct axis values.
    */

   public void setStrings() {
     FontMetrics fm;
     for (int i=0 ; i<bayesNet.getNodeList().size() ; i++) {
       Node n = (Node) bayesNet.getNodeList().elementAt(i);
       fm=getFontMetrics(getFont(n.getFont()));
       VisualNode.setAxis (n,n.getNodeString(byTitle),fm);
     }
   }

} // end of ElviraPanel class
