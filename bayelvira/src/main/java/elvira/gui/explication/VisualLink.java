/* VisualLink.java */

package elvira.gui.explication;

import elvira.Link;
import elvira.Node;
import java.awt.*;
import java.awt.geom.*;
import elvira.gui.EditorPanel;
import elvira.gui.Selection;

public class VisualLink {


/* GRAPHICAL CONSTANTS */

private final static double ARROW_SIZE = 6.0;
private final static double ARROW_HALF_SIZE = 3.0;
private final static int NODE_RADIUS = 13; 

private final static BasicStroke stroke = new BasicStroke(1.0f);
private final static BasicStroke wideStroke = new BasicStroke(4.0f);


/**
 * Decides whether this link is in a selected list of nodes.
 * @param s a list of nodes.
 * @return <code>true</code> if the head of this link or its tail is 
 * contained in the list of nodes given as parameter.
 */

public static boolean isInASelectedNode(Link link, Selection s) {
  
  for (int i=0 ; i<s.numberOfNodes() ; i++) {
    Node n = (Node) s.getNode(i);
    if (link.getHead()==n || link.getTail()==n)
      return true;
  }
  return false;
}


/**
 * Auxiliary function that draws an arc.
 * @param g Graphics interface.
 * @param dragging If the link is being dragged it must
 * be drawn in a different form.
 * @param s If the link is not selected but the node that
 *        contains it is, the link must be drawn as if it were
 *        seleted. The method uses this parameter to know if the
 *        link must appear when it is dragged dashed.
 * @param drawDiscontinuously 
 */

public static void drawArc(Link link, Graphics2D g, boolean dragging, Selection s, boolean alwaysDiscontinuously) {
  
  Node node = link.getHead(), parent = link.getTail();      
  int nodeX, nodeY, parentX, parentY,
      nodeA, nodeB, parentA, parentB,
      x1, x2, x3, y1, y2, y3, xPos, yPos;
  double dirX, dirY, distance,
         headX, headY, headXCenter, headYCenter, bottomX, bottomY;
         
  if (link.getUPDOWN()){ 
  // calculate archead
  nodeA = VisualNode.getWidth(node)/2;
  nodeB = VisualNode.getHeight(node)/2;      
  parentA = VisualNode.getWidth(parent)/2;
  parentB = VisualNode.getHeight(parent)/2;
  
  nodeX = node.getPosX();
  nodeY = node.getPosY();
  parentX = parent.getPosX();
  parentY = parent.getPosY();
  
  dirX = (double)(nodeX - parentX);
  dirY = (double)(nodeY - parentY);      
  
  // Calculates the (x,y) position of the arrow head
  

  if (!node.getExpanded()) {
	  //MLuque
	  //if (dirY != 0.0){
    yPos = (int) Math.round(Math.sqrt(1/(Math.pow(dirX/(nodeA*dirY),2)+(1/Math.pow(nodeB,2)))));
	  //}
	  //else{//MLuque
		//  yPos = (int) Math.round((1/Math.pow(nodeB,2)));
	  //}
  /*  if (yPos == 0)
      yPos=1;
    xPos = (int) Math.round((dirX*yPos)/dirY); */   
    
    
    if (yPos == 0){
        yPos=1;
        xPos = (int) Math.round(dirX);
    }
    else{
      xPos = (int) Math.round((dirX*yPos)/dirY);
    }
  }
  else {
    yPos = (int) Math.round((dirY*nodeA)/dirX);
    if (yPos > nodeB) {
      yPos = nodeB;
      xPos = (int) Math.round((dirX*nodeB)/dirY);
    }
    else if (yPos<-nodeB) {
            yPos = -nodeB;
            xPos = (int) Math.round((dirX*nodeB)/dirY);
         }
    else
      xPos = nodeA;
  }
  
  distance = Math.sqrt(dirX * dirX + dirY * dirY);
  
  dirX /= distance;
  dirY /= distance;
  
  double nodeRadius = Math.sqrt(xPos * xPos + yPos * yPos);
  

    if (!node.getExpanded()) 
    if (nodeRadius > nodeA)
      nodeRadius = nodeA;
  else if (nodeRadius < nodeB)
         nodeRadius = nodeB;
  
  headX = nodeX - (nodeRadius + ARROW_SIZE) * dirX;
  headY = nodeY - (nodeRadius + ARROW_SIZE) * dirY;
  
  bottomX = parentX + NODE_RADIUS * dirX;
  bottomY = parentY + NODE_RADIUS * dirY;
  
  x1 = (int)(headX - ARROW_HALF_SIZE*dirX + ARROW_SIZE*dirY);
  x2 = (int)(headX - ARROW_HALF_SIZE*dirX - ARROW_SIZE*dirY);
  x3 = (int)(headX + ARROW_SIZE*dirX);
  
  y1 = (int)(headY - ARROW_HALF_SIZE*dirY - ARROW_SIZE*dirX);
  y2 = (int)(headY - ARROW_HALF_SIZE*dirY + ARROW_SIZE*dirX);
  y3 = (int)(headY + ARROW_SIZE*dirY);
  
  if (((dragging)&&(isInASelectedNode(link,s)))||(alwaysDiscontinuously)) {
      g.setStroke (EditorPanel.dashed);
  }
  else 
    if (link.isSelected()) {
       if (dirX > 0) {
	 x1 = x1 - 3;
	 x2 = x2 - 3;
       } 
       else {
	 x1 = x1 + 3;
	 x2 = x2 + 3;
       }
       if (dirY > 0) {
	 y1 = y1 - 3;
	 y2 = y2 - 3;
       }
       else {
	 y1 = y1 + 3;
	 y2 = y2 + 3;
       }
       g.setStroke(new BasicStroke((new Double(link.getWidth())).floatValue()*5));           
//       g.setStroke(wideStroke);
  }
  
  // draw archead
    g.draw(new Line2D.Double((int)bottomX,(int)bottomY,(int)headX,(int)headY));
  
    int archeadX[] = { x1, x2, x3, x1 };
    int archeadY[] = { y1, y2, y3, y1 };
  
    g.fillPolygon(archeadX, archeadY, 4);
  
    if (link.isSelected() || dragging || alwaysDiscontinuously )
        g.setStroke(stroke);
  }
  else {
    //(new Link(link.getHead(), link.getTail())).drawArc(g, dragging, s);
      drawArc((new Link(link.getHead(), link.getTail())),g, dragging, s,alwaysDiscontinuously);
  }
}

public static void drawArc(Link link, Graphics2D g, boolean dragging, Selection s, Color c, boolean alwaysDiscontinuously) {
    g.setStroke(new BasicStroke((new Double(link.getWidth())).floatValue()*5));    
    g.setColor(c);
    drawArc(link,g, dragging, s, alwaysDiscontinuously);
}

public static void drawArc(Link link, Graphics2D g, boolean dragging, Selection s, Color c, double d,boolean drawDiscontinuously) {
    g.setStroke(new BasicStroke((new Double(d)).floatValue()));    
    g.setColor(c);
    drawArc(link,g, dragging, s,drawDiscontinuously);
}


} // End of class
