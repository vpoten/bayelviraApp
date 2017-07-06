/* LearningPanel.java */

package elvira.gui;

import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;

import elvira.*;
import elvira.Elvira;
import elvira.gui.explication.*;

/**
 * This class implements the panel where the result of
 * executing a learning method will be displayed. The
 * objects of this class will store the learning method
 * and its parameters.
 *
 * @author Roberto Atienza
 */

public class LearningPanel extends ElviraPanel {
    
   private int learningMethod = 0;
   private int parameterMethod = 0;
   private int metric = 0;
   
   private Vector parameters;

      
   /**
    * Default constructor for LearningPanel object.
    */
  
   public LearningPanel() {            
      super();
      learningMethod = 0;
      parameterMethod = 0;
      parameters = new Vector();
   }


   /**
    * Return the index of the current learning method
    *
    * @return an int with the index of the learing method
    */
   
   public int getLearningMethod() {
      return learningMethod;
   }
   
   public int getParameterMethod() {
      return parameterMethod;
   }
   
   public int getMetric() {
      return metric;
   }
   
   /**
    * Return a vector that contains the parameters of the 
    * current learning method
    */
   
   public Vector getParameters() {
      return parameters;
   }
   
   
   /**
    * Select the learning method that will be applied 
    */
   
   public void setLearningMethod (int i) {
      learningMethod = i;
   }
   
   
   public void setParameterMethod (int i) {
      parameterMethod = i;
   }
   
   public void setMetric (int i) {
      metric = i;
   }
   
   
   /**
    * Set the parameters of the current learning method
    */
   
   public void setParameters (Vector v){
      parameters = v;
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
      arcHeadNode = null;
      arcBottomNode = null;  
      
      int xPosition = (int) (evt.getX()/zoom), 
          yPosition = (int) (evt.getY()/zoom);
      
      if (Elvira.getElviraFrame().unselectAllComponents ()) {
         evt.consume();
         return;
      }
            
      currentNode = nodeHit(xPosition, yPosition);      
      
      if (SwingUtilities.isRightMouseButton(evt)) {
         evt.consume();
         return;
      }            
      
      startDragPosition = new Point (xPosition, yPosition);

      if (currentNode != null) {
                           
         if (!evt.isShiftDown() && !currentNode.isSelected())
             unSelectAll();
                  
         if (!currentNode.isSelected()) {
             currentNode.setSelected(true);
             selection.addNode(currentNode,zoom);
             oldNodePositions.addElement(new Point (currentNode.getPosX(), currentNode.getPosY()));                 
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
         
         refreshElviraPanel(selection.getMaxPosition(), selection.getMinPosition());
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
	   
	   if (selection.numberOfNodes() > 0) {	
	      // Get the distance that the selection has moved
	      int xMoved = x-startDragPosition.x, 
	          yMoved = y-startDragPosition.y;
	          
	      if (xMoved > 2 || yMoved > 2) {
	         Point distanceMoved = new Point (xMoved,
	                                          yMoved);
	         selection.resetPositions();
	                                       
	         for (int i=0; i<selection.numberOfNodes(); i++) {
	            Node n = selection.getNode(i);
	            Point p = (Point) oldNodePositions.elementAt(i);
	            n.setPosX(p.x);
	            n.setPosY(p.y);
               selection.recalculatePositions(n,zoom,false);
	         }
	         refreshElviraPanel(selection.getMaxPosition(), selection.getMinPosition());
	         moveSelection (distanceMoved);
	         setModifiedNetwork(true);	
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
      
      //Node node, parent;
	   Node node;
	   Link link;    // añadido <----------- jgamez
      Enumeration e, ee;      
            
      if (bayesNet == null) return;

      g2.setColor(ARC_COLOR);
    	// draw a new arc upto current mouse position
    	
	   g2.scale(zoom, zoom);
	   
    	// draw all arcs
	   for (e=bayesNet.getLinkList().elements(); e.hasMoreElements(); ) {
	      link = (Link) e.nextElement();
         VisualLink.drawArc(link,g2,dragging,selection,false);  
	   }		
	   
    	// draw the nodes
    	g2.setFont(HELVETICA);
    	
    	for (e = bayesNet.getNodeList().elements(); e.hasMoreElements(); ) {
   	   node = (Node)e.nextElement();       	      

    	   if (node.getPosX() >= 0) 
    	      VisualNode.drawNode(node,g2, NODE_COLOR, NODE_NAME_COLOR, byTitle, dragging); 	    	      
	       	    	        	    	
      }
      
   }
          
                					      
}  // end of LearningPanel.java
