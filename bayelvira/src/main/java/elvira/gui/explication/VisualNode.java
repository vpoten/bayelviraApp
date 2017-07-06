package elvira.gui.explication;

import java.awt.*;
import java.awt.geom.*;
import java.util.Vector;
import java.text.*;

import elvira.gui.EditorPanel;
import elvira.gui.NetworkFrame;
import elvira.*;
import elvira.potential.*;


public class VisualNode{

/* GRAPHICAL VARIABLES FOR NODE HEIGHT AND WIDTH */
public int chance_node_expanded_width;
public int chance_node_expanded_height;
public int half_expanded_width;
public int half_expanded_height;

/* GRAPHICAL CONSTANTS */
private static final int YSPACE_DRAW_NODE_NAME = 20;
private static final int XSPACE_DRAW_NODE_NAME = 12;

public static final int CHANCE_NODE_EXPANDED_WIDTH = 192;
public static final int HALF_EXPANDED_WIDTH = 96;

public static final int DECISION_NODE_EXPANDED_WIDTH = 110;
public static final int DECISION_NODE_EXPANDED_HEIGHT = 80;
public static final int DECISION_HALF_EXPANDED_WIDTH = 55;
public static final int DECISION_HALF_EXPANDED_HEIGHT = 40;

public static final int UTILITY_NODE_EXPANDED_WIDTH = 110;
public static final int UTILITY_NODE_EXPANDED_HEIGHT = 60;
public static final int UTILITY_HALF_EXPANDED_WIDTH = 55;
public static final int UTILITY_HALF_EXPANDED_HEIGHT = 30;


/*
 * Created by andrew
 */
public VisualNode(Node node, Node n){
 
        
}

public VisualNode(Continuous node, ExplanationContinuous n){
    int numdistributions=2;


    chance_node_expanded_height=120;
    half_expanded_height=chance_node_expanded_height/2;


    chance_node_expanded_width=230;
    half_expanded_width = chance_node_expanded_width/2;
}


public VisualNode(FiniteStates node, ExplanationFStates n){
    int numdistributions=2;
    if ((n!= null) && (n.getCasesList().getNumStoredCases()!=0))
        numdistributions=n.getCasesList().getNumStoredCases();

    chance_node_expanded_height=15+(((FiniteStates) node).getNumStates())*(VisualFStates.distestados+(VisualFStatesDistribution.height*(numdistributions))+10);
    half_expanded_height=chance_node_expanded_height/2;

	// --> Jorge-PFC 05/01/2006
	NetworkFrame frame= Elvira.getElviraFrame().getNetworkToPaint();
	int precisionLength= frame.getEditorPanel().getBayesNet().getVisualPrecision().length();
	
	chance_node_expanded_width = CHANCE_NODE_EXPANDED_WIDTH
		+ (new VisualExplanationFStates((ExplanationFStates) null).getNumFont().getSize()-4)*(precisionLength-4);
	// <-- Jorge-PFC 05/01/2006
	
    half_expanded_width = chance_node_expanded_width/2;
}


public VisualNode(Continuous node, ExplanationValueNode n){
    int numdistributions=2;
    if ((n!= null) && (n.getCasesList().getNumStoredCases()!=0))
        numdistributions=n.getCasesList().getNumStoredCases();

    //chance_node_expanded_height=15+VisualFStates.distestados+(VisualFStatesDistribution.height*numdistributions+10);
    chance_node_expanded_height=46+VisualFStates.distestados+(VisualFStatesDistribution.height*numdistributions+10);
    half_expanded_height=chance_node_expanded_height/2;

	// --> Jorge-PFC 05/01/2006
	NetworkFrame frame= Elvira.getElviraFrame().getNetworkToPaint();
	int precisionLength= frame.getEditorPanel().getBayesNet().getVisualPrecision().length();
	
	chance_node_expanded_width = CHANCE_NODE_EXPANDED_WIDTH
		+ (new VisualExplanationFStates((ExplanationFStates) null).getNumFont().getSize()-4)*(precisionLength-4);
	// <-- Jorge-PFC 05/01/2006

	half_expanded_width = chance_node_expanded_width/2;
}
/**
 * This function draws a node with a width according to
 * the length of its name and its kind.
 * @param g Object where the node is drawn.
 * @param nodeColor Color of the node.
 * @param nodeNameColor Color of the string contained into the name.
 * @param byTitle <code>true</code> if the string displayed into the node
 *        will be the title of the node. <code>false</code> if the string
 *        displayed will be the name of the node.
 * @param dragged <code>true</code> if the node must be drawn in the
 *        dragged mode.
 */

public static void drawNode(Node node,Graphics2D g, Color nodeColor,
        Color nodeNameColor,boolean byTitle, boolean dragged) {
   g.setStroke (EditorPanel.stroke);
  int kindOfNode = node.getKindOfNode();
if (kindOfNode == Node.CHANCE){
	if (!node.getPurpose().equalsIgnoreCase("Observable")){
		drawChanceNode(node,g, nodeColor, nodeNameColor, byTitle, dragged);
	}
	else{
		drawObservableNode(node,g, nodeColor, nodeNameColor, byTitle, dragged);
	}
	
}
else if (kindOfNode == Node.OBSERVED){
	if (nodeColor!=Color.black){
		nodeColor = Color.GREEN;
	}
	drawChanceNode(node,g, nodeColor, nodeNameColor, byTitle, dragged);
	}

  else if ((kindOfNode == Node.UTILITY)||(kindOfNode==Node.SUPER_VALUE)) {
    if (nodeColor != Color.black)
      nodeColor = new Color (200,255,200);
    	
    drawUtilityNode (node,g, nodeColor, nodeNameColor, byTitle, dragged);
  }
  else if (kindOfNode == Node.DECISION) {
    if (nodeColor != Color.black)
      nodeColor = new Color (200,255,255);
    drawDecisionNode (node,g, nodeColor, nodeNameColor, byTitle, dragged);
  }
  
  
  
}


/**
 *
 */

public static void drawChanceNode(Node node,Graphics2D g, Color nodeColor,
       Color nodeNameColor,boolean byTitle, boolean dragged) {

  int x = node.getPosX()-node.getHigherAxis()/2,
      y = node.getPosY()-node.getLowerAxis()/2;

/*  GradientPaint colortowhite = new GradientPaint(x, y, nodeColor,
						 x+node.getHigherAxis(),
						 y, Color.white);
*/
     g.setStroke (EditorPanel.stroke);

     g.setPaint(nodeColor/*colortowhite*/);
  if (!dragged || !(node.isSelected()))
    g.fill (new Ellipse2D.Double(x, y,  node.getHigherAxis(), node.getLowerAxis()));

  g.setPaint(Color.black);
  if (node.isSelected())
    if (dragged)
      g.setStroke(EditorPanel.dashed);
  else
    g.setStroke(EditorPanel.wideStroke);

  g.draw (new Ellipse2D.Double(x, y, node.getHigherAxis(), node.getLowerAxis()));

  if (!dragged || !(node.isSelected())) {
    g.setColor(nodeNameColor);
    g.drawString (node.getNodeString(byTitle),
		  x + XSPACE_DRAW_NODE_NAME,
		  y + YSPACE_DRAW_NODE_NAME);
  }

  g.setStroke (EditorPanel.stroke);
}



/**
*
*/

public static void drawObservableNode(Node node,Graphics2D g, Color nodeColor,
      Color nodeNameColor,boolean byTitle, boolean dragged) {
	double ratioRing=0.8;

 int higherAxis = node.getHigherAxis();
int lowerAxis = node.getLowerAxis();
int innerHigherAxis = (int)(ratioRing*higherAxis);
int innerLowerAxis = (int)(ratioRing*lowerAxis);
int x = node.getPosX()-higherAxis/2,
     y = node.getPosY()-lowerAxis/2;
int innerX;
int innerY;
innerX =   x+(int)(Math.round((1.0-ratioRing)*(higherAxis/2)));
innerY = y+(int)(Math.round((1.0-ratioRing)*(lowerAxis/2)));

/*  GradientPaint colortowhite = new GradientPaint(x, y, nodeColor,
						 x+node.getHigherAxis(),
						 y, Color.white);
*/
    g.setStroke (EditorPanel.stroke);

    g.setPaint(nodeColor/*colortowhite*/);
 if (!dragged || !(node.isSelected())){
	 g.fill (new Ellipse2D.Double(x, y,  higherAxis, lowerAxis));
	 g.fill (new Ellipse2D.Double(innerX, innerY,  innerHigherAxis, innerLowerAxis));
	 
 }
   

 g.setPaint(Color.black);
 if (node.isSelected())
   if (dragged)
     g.setStroke(EditorPanel.dashed);
 else
   g.setStroke(EditorPanel.wideStroke);

 g.draw (new Ellipse2D.Double(x, y, higherAxis, lowerAxis));
 g.draw (new Ellipse2D.Double(innerX, innerY, innerHigherAxis, innerLowerAxis));

 if (!dragged || !(node.isSelected())) {
   g.setColor(nodeNameColor);
   g.drawString (node.getNodeString(byTitle),
		  x + XSPACE_DRAW_NODE_NAME,
		  y + YSPACE_DRAW_NODE_NAME);
 }

 g.setStroke (EditorPanel.stroke);
}


/**
 *
 */

public static void drawUtilityNode (Node node,Graphics2D g, Color nodeColor,
			     Color nodeNameColor, boolean byTitle,
			     boolean dragged) {

  int halfXAxis = node.getHigherAxis()/2,
      halfYAxis = node.getLowerAxis()/2;

  int x1 = node.getPosX()-halfXAxis-3,
      x2 = node.getPosX()-halfXAxis+3,
      x3 = node.getPosX()+halfXAxis-3,
      x4 = node.getPosX()+halfXAxis+3,
      y1 = node.getPosY()-halfYAxis,
      y2 = node.getPosY()+halfYAxis;

  int x1Points[] = {x1, x2, x3, x4, x3, x2, x1};
  int y1Points[] = {node.getPosY()-1, y1, y1, node.getPosY()-1, y2, y2, node.getPosY()-1};

  GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
					x1Points.length);

  /*GradientPaint colortowhite = new GradientPaint(x1, y1, nodeColor,
						 x4, y2, Color.white);
 */
  g.setPaint(nodeColor/*colortowhite*/);
  polygon.moveTo(x1Points[0], y1Points[0]);

  for (int index = 1 ; index < x1Points.length ; index++) {
    polygon.lineTo(x1Points[index], y1Points[index]);
  }
  polygon.closePath();
  if (!dragged || !node.isSelected())
    g.fill(polygon);

  g.setPaint(Color.black);

  if (node.isSelected())
    if (dragged)
      g.setStroke(EditorPanel.dashed);
  else
    g.setStroke(EditorPanel.wideStroke);

  g.draw (polygon);

  if (!dragged || !node.isSelected()) {
    g.setColor(nodeNameColor);
    g.drawString (node.getNodeString(byTitle), node.getPosX()-halfXAxis+12, node.getPosY()+4);
  }

  g.setStroke (EditorPanel.stroke);
}


/**
 *
 */

public static void drawDecisionNode (Node node,Graphics2D g, Color nodeColor,
			      Color nodeNameColor, boolean byTitle,
			      boolean dragged) {

  int x = node.getPosX()-node.getHigherAxis()/2,
      y = node.getPosY()-node.getLowerAxis()/2;

/*  GradientPaint colortowhite = new GradientPaint(x, y, nodeColor,
						 x+node.getHigherAxis(), y,
						 Color.white);
 */
  g.setPaint(nodeColor/*colortowhite*/);

  if (!dragged || !node.isSelected())
    g.fill(new Rectangle2D.Double(x, y, node.getHigherAxis(), node.getLowerAxis()));

  g.setPaint(Color.black);

  if (node.isSelected())
    if (dragged)
      g.setStroke(EditorPanel.dashed);
  else
    g.setStroke(EditorPanel.wideStroke);

  g.draw (new Rectangle2D.Double(x, y, node.getHigherAxis(), node.getLowerAxis()));

  if (!dragged || !(node.isSelected())) {
    g.setColor(nodeNameColor);
    g.drawString (node.getNodeString(byTitle),
		  x + XSPACE_DRAW_NODE_NAME,
		  y + YSPACE_DRAW_NODE_NAME);
  }

  g.setStroke (EditorPanel.stroke);
}


/**
 * This function draws the node shape when it is
 * moved.
 * @param the object where the node is drawn.
 */

public static void drawNodeDragged (Node node,Graphics2D g) {

  int x = node.getPosX(),
      y = node.getPosY();

  g.setPaint(Color.white);
  g.fill (new Ellipse2D.Double(x, y, node.getHigherAxis(), node.getLowerAxis()));

  g.setStroke (EditorPanel.dashed);
  g.setPaint(Color.black);
  g.draw (new Ellipse2D.Double(x, y, node.getHigherAxis(), node.getLowerAxis()));
  g.setStroke (EditorPanel.stroke);
}


/**
 * Draws the node when the inference mode is enabled
 * and the user wants to see the probability distributions
 * into the net. It uses a Node n, as a ExplanationFStates or ExplanationContinuous  variable
 * to draw the propagation result
 * @param g Object where the node is drawn.
 * @param nodeColor Color of the node.
 * @param nodeNameColor Color of the string contained into the name.
 * @param n Variable used to display inference info into the node.
 */

public static void drawExpandedNode (Node node,Graphics g, Color nodeColor,
			      Color nodeNameColor, Node n,
			      boolean byTitle, boolean dragged,
			      FontMetrics fm) {
		int kindOfNode;

      if (node.getClass()==Continuous.class){
      	kindOfNode = node.getKindOfNode();
      	if ((kindOfNode ==Node.UTILITY)||(kindOfNode==Node.SUPER_VALUE)){
      		//mluque
      		drawExpandedValueNode((Continuous)node,g,nodeColor,nodeNameColor,(ExplanationValueNode)n,byTitle,dragged,fm);
      	}
      	else{
      		//andrew
          drawExpandedContinuousNode((Continuous)node,g,nodeColor,nodeNameColor,(ExplanationContinuous)n,byTitle,dragged,fm);
      	}
      }
      else{ //clacave
          drawExpandedNode(node,g,nodeColor,nodeNameColor,(ExplanationFStates)n,byTitle,dragged,fm);
      }
}


/**
 * @param continuous
 * @param g
 * @param nodeColor
 * @param nodeNameColor
 * @param node
 * @param byTitle
 * @param dragged
 * @param fm
 */
private static void drawExpandedValueNode(Continuous node, Graphics g, Color nodeColor, Color nodeNameColor, ExplanationValueNode n, boolean byTitle, boolean dragged, FontMetrics fm) {

	 int x = node.getPosX()-new VisualNode(node, n).half_expanded_width,
     y = node.getPosY()-new VisualNode(node, n).half_expanded_height;
 Graphics2D g2 = (Graphics2D) g;
 
 int halfXAxis = (new VisualNode(node,n).chance_node_expanded_width)/2;
 int halfYAxis = (new VisualNode(node,n).chance_node_expanded_height)/2;

int x1 = node.getPosX()-halfXAxis-5,
 x2 = node.getPosX()-halfXAxis+5,
 x3 = node.getPosX()+halfXAxis-5,
 x4 = node.getPosX()+halfXAxis+5,
 y1 = node.getPosY()-halfYAxis,
 y2 = node.getPosY()+halfYAxis;

int x1Points[] = {x1, x2, x3, x4, x3, x2, x1};
int y1Points[] = {node.getPosY()-1, y1, y1, node.getPosY()-1, y2, y2, node.getPosY()-1};

GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				x1Points.length);

polygon.moveTo(x1Points[0], y1Points[0]);

for (int index = 1 ; index < x1Points.length ; index++) {
  polygon.lineTo(x1Points[index], y1Points[index]);
}

 if (!dragged || !(node.isSelected())) {
 /*  GradientPaint colortowhite = new GradientPaint(x, y, nodeColor,
				     x+CHANCE_NODE_EXPANDED_WIDTH, y,
				     Color.white);
*/
  g2.setStroke (EditorPanel.stroke);
  nodeColor = new Color (200,255,200);
  g2.setPaint(nodeColor/*colortowhite*/);

   
  	g2.fill(polygon);
 }
 
 

 g2.setPaint(Color.black);

 if ((node.isSelected()))
   if (dragged)
     g2.setStroke(EditorPanel.dashed);
 else
   g2.setStroke(EditorPanel.wideStroke);

  g2.draw(polygon);
 if (node.isSelected())
   g2.setStroke(EditorPanel.stroke);

 if (!dragged || !(node.isSelected())) {
   g2.setPaint(nodeNameColor);
   drawCenteredName (node,g2, node.getNodeString(byTitle), new VisualNode(node, n).half_expanded_height,
		      new VisualNode(node, n).chance_node_expanded_width,fm);
   g2.setPaint(Color.white);
   g2.fill(new Rectangle2D.Double (x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30, new VisualNode(node,n).chance_node_expanded_height-30));
   g2.setPaint(Color.black);
   g2.draw(new Rectangle2D.Double (x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30, new VisualNode(node,n).chance_node_expanded_height-30));
   Rectangle clip = g2.getClipBounds();
	
	/* Jorge-PFC 27/12/2005, el código anterior permitía q se pintara fuera del 'internalframe' */
   Rectangle rt= clip.intersection(new Rectangle(x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30+1, new VisualNode(node,n).chance_node_expanded_height-30+1));
   g2.setClip(rt);
	
   VisualExplanationValue visualExplanationNode = new VisualExplanationValue(n);
   visualExplanationNode.paintExplanation(g2,x+19, y+30);
   g2.setClip(clip);
   g2.setFont(EditorPanel.HELVETICA);
}
}
 


/**
 * Draws the node when the inference mode is enabled
 * and the user wants to see the probability distributions
 * into the net. It uses a <code>VisualExplanationFStates</code> variable
 * to draw the probability bars.
 * @param g Object where the node is drawn.
 * @param nodeColor Color of the node.
 * @param nodeNameColor Color of the string contained into the name.
 * @param n Variable used to display the bars into the node.
 */

public static void drawExpandedNode (Node node,Graphics g, Color nodeColor,
			      Color nodeNameColor, ExplanationFStates n,
			      boolean byTitle, boolean dragged,
			      FontMetrics fm) {

  if (node.getKindOfNode() == Node.CHANCE)
    drawExpandedFiniteStatesNode ((FiniteStates)node,g, nodeColor, nodeNameColor,
                            n, byTitle, dragged, fm);
 
  else if (node.getKindOfNode() == Node.DECISION) {
    if (nodeColor != Color.black)

    drawExpandedDecisionNode ((FiniteStates)node,g, nodeColor, nodeNameColor,
                              n, byTitle, dragged,fm);
  }
 

}


/**
 *
 */

public static void drawExpandedFiniteStatesNode (FiniteStates node,Graphics g,
                                    Color nodeColor,
				    Color nameColor, ExplanationFStates n,
				    boolean byTitle, boolean dragged,
				    FontMetrics fm) {

  int x = node.getPosX()-new VisualNode(node, n).half_expanded_width,
      y = node.getPosY()-new VisualNode(node, n).half_expanded_height;
  Graphics2D g2 = (Graphics2D) g;

  if (!dragged || !(node.isSelected())) {
  /*  GradientPaint colortowhite = new GradientPaint(x, y, nodeColor,
				     x+CHANCE_NODE_EXPANDED_WIDTH, y,
				     Color.white);
*/
   g2.setStroke (EditorPanel.stroke);
 
   g2.setPaint(nodeColor/*colortowhite*/);

    g2.fill(rectangle(node.getKindOfNode(),x, y,
					new VisualNode(node,n).chance_node_expanded_width,
					new VisualNode(node,n).chance_node_expanded_height));
  }

  g2.setPaint(Color.black);

  if ((node.isSelected()))
    if (dragged)
      g2.setStroke(EditorPanel.dashed);
  else
    g2.setStroke(EditorPanel.wideStroke);

  g2.draw(rectangle(node.getKindOfNode(),x, y,
				      new VisualNode(node,n).chance_node_expanded_width,
				      new VisualNode(node,n).chance_node_expanded_height));
  if (node.isSelected())
    g2.setStroke(EditorPanel.stroke);

  if (!dragged || !(node.isSelected())) {
    g2.setPaint(nameColor);
    drawCenteredName (node,g2, node.getNodeString(byTitle), new VisualNode(node, n).half_expanded_height,
		      new VisualNode(node, n).chance_node_expanded_width,fm);
    g2.setPaint(Color.white);
    g2.fill(new Rectangle2D.Double (x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30, new VisualNode(node,n).chance_node_expanded_height-30));
    g2.setPaint(Color.black);
    g2.draw(new Rectangle2D.Double (x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30, new VisualNode(node,n).chance_node_expanded_height-30));
    Rectangle clip = g2.getClipBounds();
    
	/* Jorge-PFC 27/12/2005, el código anterior permitía q se pintara fuera del 'internalframe' */
	Rectangle rt= clip.intersection(new Rectangle(x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30, new VisualNode(node,n).chance_node_expanded_height-30));
	g2.setClip(rt);
	
    VisualExplanationFStates visualExplanationNode = new VisualExplanationFStates(n);
    visualExplanationNode.paintExplanation(g2,x+19, y+30);
    g2.setClip(clip);
    g2.setFont(EditorPanel.HELVETICA);
  }
}


/**
 * @param kindOfNode Kind of the node to draw
 */
private static Shape rectangle(int kindOfNode, int x, int y, int width, int height) {
	// TODO Auto-generated method stub
	if (kindOfNode == Node.CHANCE){
		return new RoundRectangle2D.Double(x,y,width,height,30,30);
	}
	else{//Node.DECISION
		return new Rectangle2D.Double(x,y,width,height);
		
	}
}

/**
 * Draws the node when the inference mode is enabled
 * and the user wants to see the probability distributions
 * into the net. It uses a <code>VisualExplanationContinuous</code> variable
 * to draw the probability bars.
 * @param g Object where the node is drawn.
 * @param nodeColor Color of the node.
 * @param nodeNameColor Color of the string contained into the name.
 * @param n Variable used to display inference info into the node.
 */

public static void drawExpandedContinuousNode (Continuous node,Graphics g, Color nodeColor,
			      Color nameColor, ExplanationContinuous n,
			      boolean byTitle, boolean dragged,
			      FontMetrics fm) 
{

    int x = node.getPosX()-new VisualNode(node, n).half_expanded_width,
      y = node.getPosY()-new VisualNode(node, n).half_expanded_height;
  Graphics2D g2 = (Graphics2D) g;

  if (!dragged || !(node.isSelected())) {
  /*  GradientPaint colortowhite = new GradientPaint(x, y, nodeColor,
				     x+CHANCE_NODE_EXPANDED_WIDTH, y,
				     Color.white);
*/
   g2.setStroke (EditorPanel.stroke);
   g2.setPaint(nodeColor/*colortowhite*/);

    g2.fill(new RoundRectangle2D.Double(x, y,
					new VisualNode(node,n).chance_node_expanded_width,
					new VisualNode(node,n).chance_node_expanded_height,
					30, 30));
  }

  g2.setPaint(Color.black);

  if ((node.isSelected()))
    if (dragged)
      g2.setStroke(EditorPanel.dashed);
  else
    g2.setStroke(EditorPanel.wideStroke);

  g2.draw(new RoundRectangle2D.Double(x, y,
				      new VisualNode(node,n).chance_node_expanded_width,
				      new VisualNode(node,n).chance_node_expanded_height,
				      30, 30));
  if (node.isSelected())
    g2.setStroke(EditorPanel.stroke);

  if (!dragged || !(node.isSelected())) {
    g2.setPaint(nameColor);
    drawCenteredName (node,g2, node.getNodeString(byTitle), new VisualNode(node, n).half_expanded_height,
		      new VisualNode(node, n).chance_node_expanded_width,fm);
    g2.setPaint(Color.white);
    g2.fill(new Rectangle2D.Double (x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30, new VisualNode(node,n).chance_node_expanded_height-30));
    g2.setPaint(Color.black);
    g2.draw(new Rectangle2D.Double (x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30, new VisualNode(node,n).chance_node_expanded_height-30));
    Rectangle clip = g2.getClipBounds();
    
	/* Jorge-PFC 27/12/2005, el código anterior permitía q se pintara fuera del 'internalframe' */
	Rectangle rt= clip.intersection(new Rectangle(x+14, y+20, new VisualNode(node,n).chance_node_expanded_width-30, new VisualNode(node,n).chance_node_expanded_height-30));
	g2.setClip(rt);
	
    VisualExplanationContinuous visualExplanationNode = new VisualExplanationContinuous(n);
    visualExplanationNode.paintFunction(g2,x+15, y+22, new VisualNode(node,n).chance_node_expanded_width-40, new VisualNode(node,n).chance_node_expanded_height-25);
    g2.setClip(clip);
    g2.setFont(EditorPanel.HELVETICA);
    
    n.setGraph(visualExplanationNode.getGraph());
    
  }
    
    
                                  

                                  
                                  
                                  
}
/**
 *
 */

public static void drawExpandedUtilityNode (Node node,Graphics g,
     Color nodeColor,
     Color nameColor, ExplanationFStates n,
     boolean byTitle, boolean dragged,
     FontMetrics fm)
{

  int halfXAxis = UTILITY_HALF_EXPANDED_WIDTH,
  halfYAxis = UTILITY_HALF_EXPANDED_HEIGHT;

  Graphics2D g2 = (Graphics2D) g;

  int x1 = node.getPosX()-halfXAxis-6,
      x2 = node.getPosX()-halfXAxis+6,
      x3 = node.getPosX()+halfXAxis-6,
      x4 = node.getPosX()+halfXAxis+6,
      y1 = node.getPosY()-halfYAxis,
      y2 = node.getPosY()+halfYAxis;

  int x1Points[] = {x1, x2, x3, x4, x3, x2, x1};
  int y1Points[] = {node.getPosY()-1, y1, y1, node.getPosY()-1, y2, y2, node.getPosY()-1};

  GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
					x1Points.length);
  if (!dragged || !(node.isSelected())) {
   /* GradientPaint colortowhite = new GradientPaint(x1, y1, nodeColor,
							   x4, y2, Color.white);
*/
    g2.setPaint(nodeColor/*colortowhite*/);
  }

  polygon.moveTo(x1Points[0], y1Points[0]);

  for (int index = 1 ; index < x1Points.length ; index++) {
    polygon.lineTo(x1Points[index], y1Points[index]);
  }

  if (!dragged || !(node.isSelected()))
    g2.fill(polygon);

  g2.setPaint(Color.black);

  if ((node.isSelected()))
    if (dragged)
      g2.setStroke(EditorPanel.dashed);
  else
    g2.setStroke(EditorPanel.wideStroke);

  g2.draw (polygon);

  if ((node.isSelected()))
    g2.setStroke(EditorPanel.stroke);

  if (!dragged || !(node.isSelected())) {
    polygon.closePath();
    g2.setPaint(nameColor);
    drawCenteredName (node,g2, node.getNodeString(byTitle),
		      UTILITY_HALF_EXPANDED_HEIGHT,
		      UTILITY_NODE_EXPANDED_WIDTH,fm);
    g2.setPaint(Color.white);
    g2.fill(new Rectangle2D.Double (x1+14, y1+20, 94, 26));
    g2.setPaint(Color.black);
    g2.draw(new Rectangle2D.Double (x1+14, y1+20, 94, 26));
    /*double[] maxValues = new double[(int)((Potential) n.getBayesNet().getCompiledPotentialList().elementAt(n.getBayesNet().getCompiledPotentialList().size()-1)).getSize()];
    Configuration config = new Configuration(((Potential) n.getBayesNet().getCompiledPotentialList().elementAt(n.getBayesNet().getCompiledPotentialList().size()-1)).getVariables());
    for (int i=0; i<((Potential) n.getBayesNet().getCompiledPotentialList().elementAt(n.getBayesNet().getCompiledPotentialList().size()-1)).getSize(); i++) {
      maxValues[i]=((Potential) n.getBayesNet().getCompiledPotentialList().elementAt(n.getBayesNet().getCompiledPotentialList().size()-1)).getValue(config);
      config.nextConfiguration();
    }
    double maxValue=0;
    for (int i=0; i<maxValues.length; i++) {
      if (maxValue < maxValues[i]) {
        maxValue = maxValues[i];
      }
    }*/
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator('.');
    NumberFormat nf = new DecimalFormat(n.getBayesNet().getVisualPrecision(),dfs);
    Potential pot = ((IDiagram) n.getBayesNet()).getPropagation().statistics.getFinalExpectedUtility();
    double finalValue = -1;
    if (pot != null) {
        finalValue = pot.getValue(new Configuration(pot.getVariables()));
    }
    double m = Double.parseDouble(String.valueOf(nf.format(new Double(finalValue)).toString()));
    g2.setFont(new Font("Helvetica", Font.PLAIN, 9));
    //g2.drawString((new Double(maxValue)).toString(),x1+20,y1+30);
    g2.drawString((new Double(m)).toString(),x1+20,y1+30);

    /* Jorge-PFC 27/12/2005, este código no tiene ningún efecto
    Rectangle clip = g2.getClipBounds();
    g2.setClip(x1+14, y1+20, 94, 26);
    g2.setClip(clip);
    */
    
    g2.setFont(EditorPanel.HELVETICA);

  }

}


/**
 *
 */

public static void drawExpandedDecisionNode (FiniteStates node,Graphics g,
      Color nodeColor,
      Color nameColor, ExplanationFStates n,
      boolean byTitle, boolean dragged,
      FontMetrics fm)
{
	
	  //Decisions not observed are drawn with blue color
	   if ((node.getKindOfNode()==Node.DECISION)&&(node.getObserved()==false)){
	   		nodeColor = new Color (200,255,255);
	   }
	
	drawExpandedFiniteStatesNode(node,g,nodeColor,nameColor,n,byTitle,dragged,fm);
	

}


/**
 *
 */

public static void drawCenteredName (Node node,Graphics2D g, String name,
                              int halfHeight,int width,FontMetrics fm) {
  String stringName = name;
  //int namePixels = nodeFontMetrics.stringWidth(stringName);
  //FontMetrics fm=EditorPanel.getFontMetrics(node.getFontMetrics());
  int namePixels = fm.stringWidth(stringName);

  if (namePixels > width+20) {
    stringName = stringName.substring(0,10)+"...";
    //namePixels = nodeFontMetrics.stringWidth(stringName);
    namePixels = fm.stringWidth(stringName);
  }
  int x = node.getPosX()-(namePixels/2),
      y = node.getPosY()-halfHeight+15;
  g.drawString(stringName, x, y);
}

public static int getLowerAxis (Node node,String nodeString,FontMetrics fm) {
  return 30;
}

public static int getHigherAxis (Node node,String nodeString,FontMetrics fm) {
  //FontMetrics fm=EditorPanel.getFontMetrics(node.getFontMetrics());
  int namePixels = fm.stringWidth(nodeString);

  if (namePixels < 12)
    return(30);
  else
    return(namePixels+24);
}

public static void setAxis (Node node,String nodeString,FontMetrics fm) {
  node.setAxis(getLowerAxis(node,nodeString,fm),getHigherAxis(node,nodeString,fm));
}

/*public static void setAxis (Node node,boolean byTitle,FontMetrics fm) {
  setAxis(node,node.getNodeString(byTitle),fm);
}*/

/**
 *
 */

public static int getWidth(Node node) {

	// Jorge-PFC 05/01/2006
	NetworkFrame n = Elvira.getElviraFrame().getNetworkToPaint();
	
  int width = 0;

  if (n.getMode() == NetworkFrame.INFERENCE_ACTIVE && node.getExpanded())
    switch (node.getKindOfNode()) {
      case Node.CHANCE: 
          if (node.getClass()==Continuous.class)
              width = new VisualNode((Continuous)node, (ExplanationContinuous)null).chance_node_expanded_width; 
          else
              width = new VisualNode((FiniteStates)node, null).chance_node_expanded_width; 
          break;
      case Node.DECISION: //width = DECISION_NODE_EXPANDED_WIDTH; break;
      		width = new VisualNode((FiniteStates)node, null).chance_node_expanded_width;
      		break;
      case Node.UTILITY:// width = UTILITY_NODE_EXPANDED_WIDTH; break;
      case Node.SUPER_VALUE:// width = UTILITY_NODE_EXPANDED_WIDTH; break;
      		width = new VisualNode((Continuous)node, (ExplanationValueNode)null).chance_node_expanded_width;
      		break;
    }
  else
    width = node.getHigherAxis();

  return width;
}


/**
 *
 */

public static int getHeight(Node node) {
	// Jorge-PFC 05/01/2006
	NetworkFrame n = Elvira.getElviraFrame().getNetworkToPaint();
	
  int height = 0;

  if (n.getMode() == NetworkFrame.INFERENCE_ACTIVE  && node.getExpanded()){
    switch (node.getKindOfNode()) {
      case Node.CHANCE:
        
        if (node.getClass()==FiniteStates.class){  
            ExplanationFStates expfs=new ExplanationFStates(n.getInferencePanel(),
                         n.getInferencePanel().getBayesNet(),
                         (FiniteStates)node);
            height = new VisualNode((FiniteStates)node,expfs).chance_node_expanded_height;
        }else{
            
            height = new VisualNode((Continuous)node,(ExplanationContinuous)null).chance_node_expanded_height;
        }
        break;
      case Node.DECISION: //height = DECISION_NODE_EXPANDED_HEIGHT; break;
      	ExplanationFStates expfs=new ExplanationFStates(n.getInferencePanel(),
                n.getInferencePanel().getBayesNet(),
                (FiniteStates)node);
      	height = new VisualNode((FiniteStates)node,expfs).chance_node_expanded_height;
      	break;
      case Node.UTILITY: //height = UTILITY_NODE_EXPANDED_HEIGHT; break;
      case Node.SUPER_VALUE: //height = UTILITY_NODE_EXPANDED_HEIGHT; break;
      	height = new VisualNode((Continuous)node, (ExplanationValueNode)null).chance_node_expanded_height;
		break;
    }
    
  }else{
    height = node.getLowerAxis();
  }

  

  return height;
}

public static int getHalfHeight(Node node) {

  return getHeight(node)/2;
}


} // End of class

