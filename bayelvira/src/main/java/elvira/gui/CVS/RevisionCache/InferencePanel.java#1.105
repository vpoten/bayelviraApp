/* InferencePanel.java */

package elvira.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import java.io.*;
import elvira.*;
import elvira.parser.*;
import elvira.gui.explication.*;
import elvira.gui.KmpesDialog;
import elvira.inference.*;
import elvira.inference.clustering.*;
import elvira.inference.abduction.*;
import elvira.potential.*;
import elvira.inference.elimination.*;
import elvira.inference.elimination.ids.*;
import elvira.inference.super_value.ArcReversalSV;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.NodeGSDAG;
import elvira.inference.approximate.*;



/**
 * Contains the work area for make inference
 * in the net that has been edited
 *
 * @since 20/11/00
 * @version 0.1
 */

public class InferencePanel extends ElviraPanel implements MouseListener, MouseMotionListener{

   //private Evidence evidence

   private CasesList cases;

   private int compcase=0;

   // network inference modes
   public static final int DISABLED_MODE = 0;
   public static final int EXPLAIN_NODE_MODE = 1;
   public static final int EXPLAIN_LINK_MODE = 2;


   public static final String EXPLAIN_NODE = "Explain Node";
   public static final String EXPLAIN_LINK = "Explain Link";
   //public static final String EXPLAIN_LINK = "Explain Link";

   public static final int EXPANDED = 0;
   public static final int CONTRACTED = 1;

   public boolean AUTOPROPAGATION=true;

   public boolean MACROEXPLANATION=false;

   public boolean AUTOEXPLANATION=false;

   public boolean PATHS=false;
   
   public boolean UPDOWN=true;

   public static int POSTERIORI=0;
   public static int MOSTPROBEXPL=1;
   public static int KMOSTPROBEXPL=2;

   public int INFERENCEAIM=POSTERIORI;

   public boolean TOTALABDUCTION=true;

   private int numExplanations=1;
   private NodeList expSet=new NodeList();

   public static int CASOP=0;
   public static int CASOANT=1;
   public static int CASOK=2;

   public int COMPARINGCASE=CASOP;

   private double Theta=0.001;

   int nodesNumber = 0;
   private double expansionThreshold = 7.00;
   private String[] functionThreshold = new String[EditorPanel.functionsNode.length-1];
   //private int inferenceMethod = 0;
   //The new method of propagation by default for bayesian networks is Variable Elimination
   private int inferenceMethod = 3;
   private Vector parameters;
   private Vector auxiliaryFilesNames;
   
   //Auxiliary bayesian network used in the inference mode for influence diagrams
  // private CooperPolicyNetwork cpn;

   /**
    * Contains the policy to be applied when the
    * Expand/Contract button is clicked
    */
   public int expandMode = EXPANDED;

   private boolean purposeMode = false;

   /* to show graphically the influences of the links*/
   private boolean showInfluences;

   
   private Propagation propag=null;
   
   private Vector propagResults=new Vector();
   
   public JPopupMenu inferencePopupMenu = new JPopupMenu();
   public JMenuItem explainMenuItem = new JMenuItem();
   public JMenuItem expandMenuItem = new JMenuItem();
   public JMenuItem propertiesMenuItem = new JMenuItem();
   public JMenuItem explainFunctionMenuItem = new JMenuItem();
   public JMenuItem showDecisionTableMenuItem = new JMenuItem();
   public JMenuItem showDecisionPolicyTreeMenuItem = new JMenuItem();
   public JMenuItem showPosteriorDistributionsTableMenuItem = new JMenuItem();
   public JMenuItem showUtilitiesTableMenuItem = new JMenuItem();


   {
	   inferencePopupMenu.add(propertiesMenuItem);
   }

   public InferencePanel() {
         
        super();
    	setAutoscrolls(true);
	setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
	setBackground(java.awt.Color.white);

        //System.out.println("InferencePanel");
        
	propertiesEditable = false;

	addMouseListener(this);
	addMouseMotionListener(this);

//	for (int f=0; f<functionThreshold.length; f++)
//	  functionThreshold[f]=EditorPanel.functionsNode[f];

	expandMenuItem.setText("Expand Node");
	expandMenuItem.setActionCommand("Expand Node");
	propertiesMenuItem.setText("Edit Node Properties...");

	//{{REGISTER_LISTENERS
	InferencePopupAction action = new InferencePopupAction();
	explainMenuItem.addActionListener(action);
	propertiesMenuItem.addActionListener(action);
	expandMenuItem.addActionListener(action);
    explainFunctionMenuItem.addActionListener(action);
    showDecisionTableMenuItem.addActionListener(action);
    //showDecisionPolicyTreeMenuItem.addActionListener(Elvira.getElviraFrame().getElviraGUIMediatorPT());
    showDecisionPolicyTreeMenuItem.addActionListener(action);
    showPosteriorDistributionsTableMenuItem.addActionListener(action);
    showUtilitiesTableMenuItem.addActionListener(action);

    
    showDecisionPolicyTreeMenuItem.setActionCommand("Create Policy Tree");
        //}}

    }


   public Vector getPotentialList(){

        if (propagResults.size()>0){
           if (cases.getNumCurrentCase()==0)
               return null;
           else if ((cases.getNumCurrentCase()-1)>=propagResults.size())
               return null;
           else
               return ((Propagation)propagResults.elementAt(cases.getNumCurrentCase()-1)).results;
        }else
            return null;
   }

   public Vector getResultsList(){
       
        return propagResults;
   }
   /**
    * Gets the evidence of the net displayed in the editor
    */

   public Evidence getEvidence () {
      return cases.getCurrentCase().getEvidence();
      //return evidence;
   }

   public CasesList getCasesList () {
      return cases;
   }


   /**
    * Gets the expansion threshold of the network displayed
    */

   public double getExpansionThreshold () {
      return expansionThreshold;
   }

   public String[] getFunctionThreshold () {
      return functionThreshold;
   }

   public int getCasetoCompare (){
      return compcase;
   }

   public int getInferenceMethod() {
      return inferenceMethod;
   }

   public Vector getParameters() {
      return parameters;
   }

   public boolean getPurposeMode(){
   	return purposeMode;
   }

   public Vector getAuxiliaryFilesNames() {
      return auxiliaryFilesNames;
   }

   public int getNumExplanations() {
      return numExplanations;
   }

   public double getTheta(){
   	return Theta;
   }

   public boolean getInfluences(){
   	return showInfluences;
   }

   /**
    * Returns a Vector with significant data for abductive inference
    * elementAt(0): a boolean (true if TOTALABDUCTION)
    * elementAt(1): a nodelist containing the explanation set
    * elementAt(2): an integer with the number of explanations (K)
    */

   public Vector getAbductiveValues() {
     Vector v = new Vector();

     v.addElement(new Boolean(TOTALABDUCTION));
     v.addElement(expSet);
     v.addElement(new Integer(numExplanations));

     return v;
   }

   /**
    * Set the evidence of the Net displayed in the editor
    */

    /*public void setEvidence (Evidence e) {
      evidence = e;
    }*/

   public void setCasesList(CasesList c) {
      cases = c;
   }

    /**
     * Set the expansion threshold value
     */
   public void setExpansionThreshold (double d) {
      expansionThreshold = d;
   }

   public void setFunctionThreshold (String[] s) {
      functionThreshold = s;
   }

   public void setCasetoCompare (int i){
      compcase = i;
   }

   public void setInferenceMethod (int i) {
      inferenceMethod = i;
   }

   public void setParameters (Vector v){
      parameters = v;
   }

   public void setAuxiliaryFilesNames (Vector v) {
      auxiliaryFilesNames = v;
   }


   public void setPurposeMode(boolean b){
      purposeMode=b;
   }

   public void setNumExplanations(int n){
      numExplanations=n;
   }

  public void setExplanationSet(NodeList nl){
     expSet=nl;
  }

  public void setTheta(double t){
  	Theta=t;
  }

  public void setInfluences(boolean b){
  	showInfluences=b;
  }

   public void processMouseEvent (MouseEvent e) {


       currentNode = nodeHit((int) (e.getX()/zoom), (int) (e.getY()/zoom));

      if (e.isPopupTrigger()) {
         JMenuItem lastItem = (JMenuItem) inferencePopupMenu.getComponentAtIndex(
               inferencePopupMenu.getComponentCount()-1);
         JMenuItem firstItem = (JMenuItem) inferencePopupMenu.getComponentAtIndex(0);


         if (currentNode != null) {

             explainMenuItem.setText(localize(
                  menuBundle,"Popup.ExplainNode.label"));
            explainMenuItem.setActionCommand("Explain Node");
		      propertiesMenuItem.setText(localize(
                  menuBundle,"Popup.NodeProperties.label"));
              propertiesMenuItem.setActionCommand("Edit Node");
           if (currentNode.getClass()==Continuous.class){
                explainFunctionMenuItem.setText(localize(
                    menuBundle,"Popup.ExplainFunctionNode.label"));
                explainFunctionMenuItem.setActionCommand("Explain Function Node");
                    inferencePopupMenu.insert(explainFunctionMenuItem,3);
                

           }
           
          	
           	
            if (expandMode == CONTRACTED)
               expandMenuItem.setText(localize(
                  menuBundle,"Popup.ContractNode.label"));
            else
               expandMenuItem.setText(localize(
                  menuBundle,"Popup.ExpandNode.label"));

            
            if (firstItem == propertiesMenuItem)
		        inferencePopupMenu.insert(explainMenuItem,0);
            
            Class<? extends Bnet> bnetClass = this.getBayesNet().getClass();
			if (bnetClass==IDWithSVNodes.class){
            	ArrayList postDistrib;
            	ArrayList postUtilities;
            	boolean hasForcedPolicy;
            	
            	//For decision nodes
            	showDecisionTableMenuItem.setText(localize(menuBundle,"Popup.ShowDecisionTables.label"));
            	showDecisionPolicyTreeMenuItem.setText(localize(menuBundle,"Popup.ShowDecisionPolicyTree.label"));
           		inferencePopupMenu.insert(showDecisionTableMenuItem,0);
           		boolean enableShowPolicy = (currentNode.getKindOfNode()==Node.DECISION)&&(!((IDiagram)getBayesNet()).hasForcedPolicy(currentNode));
				showDecisionTableMenuItem.setEnabled(enableShowPolicy);
           		
           		showDecisionPolicyTreeMenuItem.setText(localize(menuBundle,"Popup.ShowDecisionPolicyTree.label"));
           		inferencePopupMenu.insert(showDecisionPolicyTreeMenuItem,1);
           		showDecisionPolicyTreeMenuItem.setEnabled(enableShowPolicy);
           		
           		//For chance nodes
           		showPosteriorDistributionsTableMenuItem.setText(localize(menuBundle,"Popup.ShowPosteriorDistributionsTables.label"));
           		inferencePopupMenu.insert(showPosteriorDistributionsTableMenuItem,2);
           		showUtilitiesTableMenuItem.setText(localize(menuBundle,"Popup.ShowUtilitiesTables.label"));
           		inferencePopupMenu.insert(showUtilitiesTableMenuItem,3);
           		postDistrib = ((IDiagram)bayesNet).getPosteriorDistributions();
           		postUtilities = ((IDiagram)bayesNet).getPosteriorUtilities();
           		//showPosteriorDistributionsTableMenuItem.setEnabled((currentNode.getKindOfNode()==Node.CHANCE)&&(postDistrib!=null)&&(postDistrib.size()>0));
           		//showUtilitiesTableMenuItem.setEnabled((currentNode.getKindOfNode()==Node.CHANCE)&&(postUtilities!=null)&&(postUtilities.size()>0));
           		showPosteriorDistributionsTableMenuItem.setEnabled(currentNode.getKindOfNode()==Node.CHANCE);
           		showUtilitiesTableMenuItem.setEnabled(currentNode.getKindOfNode()==Node.CHANCE);

            }
            else if (bnetClass==GSDAG.class){
            	boolean enableShowPolicy;
            	
               	showDecisionTableMenuItem.setText(localize(menuBundle,"Popup.ShowDecisionTables.label"));
           		inferencePopupMenu.insert(showDecisionTableMenuItem,0);
           		enableShowPolicy = (currentNode.getKindOfNode()==Node.DECISION);
           		showDecisionTableMenuItem.setEnabled(enableShowPolicy);
           		showDecisionPolicyTreeMenuItem.setEnabled(false);
            	explainMenuItem.setEnabled(false);
            	propertiesMenuItem.setEnabled(false);
            	expandMenuItem.setEnabled(false);
            }
            
          
           		
           
            
	    if (lastItem == propertiesMenuItem)
                        inferencePopupMenu.add(expandMenuItem);
         }

         else if (currentLink != null) {
         	
         	inferencePopupMenu.remove(showDecisionTableMenuItem);
         	inferencePopupMenu.remove(showDecisionPolicyTreeMenuItem);
             explainMenuItem.setText(localize(
                  menuBundle,"Popup.ExplainLink.label"));
            explainMenuItem.setActionCommand("Explain Link");
		      propertiesMenuItem.setText(localize(
                  menuBundle,"Popup.LinkProperties.label"));
		      propertiesMenuItem.setActionCommand("Edit Link");
  		      if ((firstItem == propertiesMenuItem) &&
  		          (getBayesNet().getClass()!=IDiagram.class))
		         inferencePopupMenu.insert(explainMenuItem,0);
	         if (lastItem != propertiesMenuItem)
                  inferencePopupMenu.remove(expandMenuItem);
	}

	      else {

	      	inferencePopupMenu.remove(showDecisionTableMenuItem);
	      	inferencePopupMenu.remove(showDecisionPolicyTreeMenuItem);
                 if (firstItem != propertiesMenuItem)
		         inferencePopupMenu.remove(explainMenuItem);
	         if (lastItem != propertiesMenuItem)
                  inferencePopupMenu.remove(expandMenuItem);

		      propertiesMenuItem.setText(localize(
                  menuBundle,"Popup.NetProperties.label"));
		      propertiesMenuItem.setActionCommand("Edit Network");

              explainMenuItem.setText(localize(
                  menuBundle,"Popup.ExplainNetwork.label"));
              explainMenuItem.setActionCommand("Explain Network");
              inferencePopupMenu.insert(explainMenuItem,0);
           
	      }
         inferencePopupMenu.show(this, e.getX(), e.getY());
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
      arcHeadNode = null;
      arcBottomNode = null;

      //System.out.println("MousePressed");
      int xPosition = (int) (evt.getX()/zoom),
          yPosition = (int) (evt.getY()/zoom);

      if (Elvira.getElviraFrame().unselectAllComponents ()) {
         evt.consume();
         return;
      }

      if (inferencePopupMenu.isVisible()) {
         inferencePopupMenu.setVisible(false);
         nodeToMove=null;
         unSelectAll();
         evt.consume();
         return;
      }

      currentNode = nodeHit(xPosition, yPosition);
      startDragPosition = new Point (xPosition, yPosition);

      if (currentNode == null) {

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
	   }

      }

      else {   // If a node was clicked on.

         if ((evt.getClickCount()==2) &&
             (currentNode.getExpanded()) &&
             (currentNode.getClass() == FiniteStates.class)){
            if ((Math.abs(currentNode.getPosX()-xPosition)<=81) && (Math.abs(currentNode.getPosY()-yPosition)<=(VisualNode.getHeight(currentNode)-30)/2)) {
                int i=0, posg=VisualFStates.distestados;
               int states = ((FiniteStates)currentNode).getStates().size();
               int stateheight = VisualFStates.distestados+(VisualFStatesDistribution.height*cases.getNumStoredCases());
               boolean exit = false;
               while ((i<states) && (!exit)) {
                   posg=posg+stateheight;
                  if ((Math.abs(yPosition-currentNode.getPosY()+(VisualNode.getHeight(currentNode)-30)/2))<posg) {
                     if ((cases.getNumCurrentCase()==0) && (cases.getNumActiveCase()==1) &&
                        (!cases.getActiveCase().getPropagated())){
                        cases.setCurrentCase(1);
                     }
                     if (cases.getNumCurrentCase()!=0){
                                propagate ((FiniteStates)currentNode, i);
                                exit = true;
                     }
                  }
                  i++;
               }
            }
            else {
               cases.getCurrentCase().unsetAsFinding(currentNode);
               propagate ((FiniteStates)currentNode, -1);
            }
          } else if ((evt.getClickCount()==2) &&
             (currentNode.getExpanded()) &&
             (currentNode.getClass() == Continuous.class) && currentNode.getKindOfNode()==Node.CHANCE){
            

            if ((Math.abs(currentNode.getPosX()-xPosition)<=81) && (Math.abs(currentNode.getPosY()-yPosition)<=(VisualNode.getHeight(currentNode)-30)/2)) {

            }
            else {
               cases.getCurrentCase().unsetAsFinding(currentNode);
               propagate ((Continuous)currentNode, -1.0);
            }
          }

          else //si est� seleccionado pero no expandido
            if ((evt.getClickCount()==2) && (! currentNode.getExpanded()) &&
                     (currentNode.getKindOfNode() == Node.CHANCE)) {
                    //if (!cases.getCurrentCase().getIsObserved(currentNode)){
                        FindingDialog df=new FindingDialog(Elvira.getElviraFrame().getNetworkFrame(), bayesNet, currentNode);
                        df.show();
/*                    }
                    else {
                          cases.getCurrentCase().unsetAsFinding(currentNode);
                          propagate (currentNode, -1);
                    }
*/          }
	    else {
               if (!evt.isShiftDown() && !currentNode.isSelected()) {
                 unSelectAll();
		         currentLink = null;
	       }

               if (!currentNode.isSelected()) {
                currentNode.setSelected(true);
                selection.addNode(currentNode,zoom);
                oldNodePositions.addElement(new Point (currentNode.getPosX(), currentNode.getPosY()));
               }
           }
      }


      checkSelected();
      repaint();
   }


   /**
    * Process mouse drag events.
    *
    * @param evt Event produced
    */

   public void mouseDragged(MouseEvent evt) {

      //System.out.println("MouseDragged");
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
            selection.recalculatePositions(n,zoom,true);
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
               selection.recalculatePositions(n,zoom,true);
	         }
	         move (distanceMoved);
	         setModifiedNetwork(true);
	         refreshElviraPanel(getZoom()/*selection.getMaxPosition(), selection.getMinPosition()*/);
	      }

	   }
      dragging = false;

	   repaint();
   }


   public void propagate(FiniteStates n, int value)
   {
   
       //System.out.println("Propagate2");
       if (value != -1) {
           Finding f=new Finding(n,value);
           cases.getCurrentCase().setAsFinding(n, value);
           System.out.println("Evidencia");
           cases.getCurrentCase().getEvidence().pPrint();
   	       cases.addCurrentCase( n, value);
   	       if (AUTOPROPAGATION){
   	           if (!propagate(cases.getCurrentCase())){
   	           		if (n.getKindOfNode()==Node.CHANCE){
   	           		ShowMessages.showMessageDialog(ShowMessages.IMPOSIBLE_FINDING,
                            JOptionPane.ERROR_MESSAGE);
   	           		}
   	           		else{//Node.DECISION
   	           		ShowMessages.showMessageDialog(ShowMessages.IMPOSIBLE_DECISION,
                            JOptionPane.ERROR_MESSAGE);
   	           			
   	           		}
                    
   	                cases.getCurrentCase().unsetAsFinding((FiniteStates) n);
   	                propagate(cases.getCurrentCase());
   	            }
   	       }
   	       else notpropagate(cases.getCurrentCase());
   	  }
   	  else {
   	        cases.getCurrentCase().unsetAsFinding(n);
   	        if (AUTOPROPAGATION) propagate(cases.getCurrentCase());
   	  }
  }

   public void propagate(Continuous n, double value)
   {
   
       //propag=null;
       System.out.println("Propagate2:"+value+","+cases.getCurrentCase().getEvidence().getName());
       if (value != -1.0) {
           Finding f=new Finding(n,value);
           cases.getCurrentCase().setAsFinding(n, value);
           System.out.println("Evidencia");
//           cases.getCurrentCase().getEvidence().pPrint();
           //cases.addCurrentCase(n,value);
   	       if (AUTOPROPAGATION){
   	           if (!propagate(cases.getCurrentCase())){
                    ShowMessages.showMessageDialog(ShowMessages.IMPOSIBLE_FINDING,
                                                    JOptionPane.ERROR_MESSAGE);
   	                cases.getCurrentCase().unsetAsFinding(n);
   	                propagate(cases.getCurrentCase());
   	            }
   	       }
   	       else notpropagate(cases.getCurrentCase());
       }
       else {
            cases.getCurrentCase().unsetAsFinding(n);
            if (AUTOPROPAGATION) propagate(cases.getCurrentCase());
       }
  }

   
   public int getElement(Vector parameters, int i) {
      Integer element = (Integer) parameters.elementAt(i);
      return element.intValue();
   }

   public double getFloatElement(Vector parameters, int i) {
      Double element = (Double) parameters.elementAt(i);
      return element.doubleValue();
   }

   private void showResults(Propagation p) {
     for (int i=0 ; i<p.results.size() ; i++) {
       System.out.print("The Results: \n");
       ((Potential)p.results.elementAt(i)).print();
       System.out.println();
     }
   }

   public Propagation propagateMethod (Bnet bnetToPropagate,Case c, int index, Vector parameters) {
      Evidence e=c.getEvidence();
      System.out.println("Evidencia al hacer la propagaci�n. �ndice "+index);
      //e.pPrint();
      Propagation pres=new Propagation();


      
      if ((bnetToPropagate.getClass() != IDiagram.class)&&(bnetToPropagate.getClass()!=IDWithSVNodes.class)) {
         //System.out.println("Var elimination "+index);
         switch (index) {
         case 0: HuginPropagation p = new HuginPropagation(bnetToPropagate,e,"tables");
                 p.obtainInterest();
                 p.propagate(p.getJoinTree().elementAt(0),"si");
                 try {p.saveResults("potential.pot");}
                 catch (IOException ioe){System.out.println("No se han grabado los potenciales");}
                 pres = p;
                 break;
         case 1: HuginPropagation tp = new HuginPropagation(bnetToPropagate,e,"trees");
                 tp.obtainInterest();
                 tp.propagate(tp.getJoinTree().elementAt(0),"si");
                 try {tp.saveResults("potential.pot");}
                 catch (IOException ioe){System.out.println("No se han grabado los potenciales");}
                 pres = tp;
                 break;
         case 2: ApproximateHuginPropagation ap = new
                       ApproximateHuginPropagation(bnetToPropagate,e,
                            ((Double)parameters.elementAt(1)).doubleValue(),
                            ((Integer)parameters.elementAt(0)).intValue());
                 ap.obtainInterest();
                 ap.propagate(ap.getJoinTree().elementAt(0),"si");
                 try {ap.saveResults("potential.pot");}
                 catch (IOException ioe){System.out.println("No se han grabado los potenciales");}
                 pres = ap;
                 break;

         case 3: VariableElimination ve = new VariableElimination(bnetToPropagate,e);
                 ve.obtainInterest();
                 ve.propagate();
                 pres = ve;
                 break;
         case 4: VEWithPotentialTree vew = new VEWithPotentialTree(bnetToPropagate,e);
                 vew.obtainInterest();
                 vew.propagate();
                 pres = vew;
                 break;
         case 5: ImportanceSamplingTable istb =
                     new ImportanceSamplingTable(bnetToPropagate,e,
                         getElement(parameters, 0),
                         getElement(parameters, 1), 1);
                 istb.obtainInterest();
                 istb.propagate();
                 pres = istb;
                 break;
         case 6: ImportanceSamplingTree ist =
                     new ImportanceSamplingTree(bnetToPropagate,e,
                         getFloatElement(parameters, 2),
                         getElement(parameters, 0),
                         getElement(parameters, 1), 1);
                 ist.obtainInterest();
                 ist.propagate();
                 pres = ist;
                 break;
         case 7: ImportanceSamplingFunctionTree isft =
                     new ImportanceSamplingFunctionTree(bnetToPropagate,e,
                         getFloatElement(parameters, 2),
                         getElement(parameters, 0),
                         getElement(parameters, 1), 1);
                 isft.obtainInterest();
                 isft.propagate();
                 pres = isft;
                 break;
         case 8: ImportanceSamplingTreeAV istav =
                     new ImportanceSamplingTreeAV(bnetToPropagate,e,
                         getFloatElement(parameters, 2),
                         getElement(parameters, 0),
                         getElement(parameters, 1), 1);
                 istav.obtainInterest();
                 istav.propagate();
                 pres = istav;
                 break;

         case 12: AbductiveInferenceNilsson ain= new AbductiveInferenceNilsson(bnetToPropagate,e,"tables");
                  if (!TOTALABDUCTION){
                     ain.setPartial(true);
                     if (parameters==null)
                        ain.setPropComment("size");
                        else ain.setPropComment((String)parameters.elementAt(0));
                     ain.setExplanationSet(expSet);
                  }
                  else {ain.setPartial(false);
                        ain.setExplanationSet(ain.getExplanationSet());
                  }
                  if (INFERENCEAIM==InferencePanel.MOSTPROBEXPL)
                      ain.setNExplanations(1);
                      else ain.setNExplanations(numExplanations);
                  ain.propagate("maxprobexpot.pot");
                  ain.results=((Explanation)ain.getKBest().elementAt(0)).toPosteriorProbability(bnetToPropagate.getNodeList(),e);
                  pres=ain;

                  if (INFERENCEAIM == InferencePanel.KMOSTPROBEXPL){
                    KmpesDialog kmpeDialog = new KmpesDialog(
                                                    ain.getKBest());
                  }

                  try{
                      ain.saveResults("maxprobexpot.pot");
                  }
                  catch (IOException ioe){System.out.println("No se han grabado ");}

                  break;

         case 13: AbductiveInferenceNilsson aint= new AbductiveInferenceNilsson(bnetToPropagate,e,"trees");
                  if (!TOTALABDUCTION){
                     aint.setPartial(true);
                     if (parameters==null)
                        aint.setPropComment("size");
                        else aint.setPropComment((String)parameters.elementAt(0));
                     aint.setExplanationSet(expSet);
                  }
                  else {aint.setPartial(false);
                       aint.obtainInterest();
                        aint.setExplanationSet(aint.interest);
                  }
                  if (INFERENCEAIM==InferencePanel.MOSTPROBEXPL)
                      aint.setNExplanations(1);
                      else aint.setNExplanations(numExplanations);
                  aint.propagate("maxprobexpot.pot");
                  aint.results=((Explanation)aint.getKBest().elementAt(0)).toPosteriorProbability(bnetToPropagate.getNodeList(),e);
                  pres=aint;

                  if (INFERENCEAIM == InferencePanel.KMOSTPROBEXPL){
                    KmpesDialog kmpeDialog = new KmpesDialog(
                                                    aint.getKBest());
                  }


                  try{
                      aint.saveResults("maxprobexpot.pot");
                  }
                  catch (IOException ioe){System.out.println("No se han grabado ");}
                  break;
         case 14: System.out.println("Par�metros "+parameters.toString());
                    ApproximateAbductiveInferenceNilsson aain= new ApproximateAbductiveInferenceNilsson(bnetToPropagate,e,((Double)parameters.elementAt(1)).doubleValue(),((Integer)parameters.elementAt(0)).intValue());
                  if (!TOTALABDUCTION){
                     aain.setPartial(true);
                     if (parameters==null)
                        aain.setPropComment("size");
                        else aain.setPropComment((String)parameters.elementAt(2));
                     aain.setExplanationSet(expSet);
                  }
                  else {aain.setPartial(false);
                       aain.obtainInterest();
                        aain.setExplanationSet(aain.interest);
                  }
                  if (INFERENCEAIM==InferencePanel.MOSTPROBEXPL)
                      aain.setNExplanations(1);
                      else aain.setNExplanations(numExplanations);
                  aain.propagate("maxprobexpot.pot");
                  aain.results=((Explanation)aain.getKBest().elementAt(0)).toPosteriorProbability(bnetToPropagate.getNodeList(),e);
                  pres=aain;

                  if (INFERENCEAIM == InferencePanel.KMOSTPROBEXPL){
                    KmpesDialog kmpeDialog = new KmpesDialog(
                                                    aain.getKBest());
                  }


                  try{
                      aain.saveResults("maxprobexpot.pot");
                  }
                  catch (IOException ioe){System.out.println("No se han grabado ");}
                  break;

	case 15:  LikelihoodWeighting lw = new LikelihoodWeighting(bnetToPropagate,e);
		  lw.obtainInterest();
		  Integer simStep = new Integer(getElement(parameters, 0));
		  try {
		    lw.propagate(simStep.toString(),"potential.pot");
	          } catch(ParseException pe) {}
		    catch(IOException ioe) {System.out.println("No se han grabado ");}
                  pres = lw;
                  break;

        }
      }
      else {
        /*Vector reList = ((IDiagram) getBayesNet()).getRelationList();
        for (int j=0; j<reList.size(); j++) {
          if (((Relation) reList.elementAt(j)).getKind() == Relation.CONSTRAINT) {
            if ((index != 2) && (index != 5)) {
              setInferenceMethod(2);
              Vector v = new Vector();
              Double d = new Double(0.0);
              v.addElement(d);
              setParameters(v);
            }
          }
        }*/
        //System.out.println("Var elimination2 "+index);
        switch (index) {

          /* Variable Elimination */

          case 0: Network bVE=((IDiagram)bnetToPropagate).copy(); // Need to copy: barren nodes are removed
                  VariableElimination ve;

                  ve = new VariableElimination((Bnet)bVE,e);

                  // There are not interest variables
                  //ve.obtainInterest();

                  // Propagate

                  ve.propagate();

                  // To present the results

                  
                  pres = ve;
                  showResults(pres);

                  break;

          /* Variable Elimination, Potential Trees */

          case 1: Network bVEPT=((IDiagram) bnetToPropagate).copy(); // Need to copy: barren nodes removed
                  VEWithPotentialTree vePT;

                  vePT = new VEWithPotentialTree((Bnet)bVEPT,e);

                  // There are not interest variables
                  //vePT.obtainInterest();

                  // Set the threshold for prunning

                  vePT.setThresholdForPrunning(getFloatElement(parameters, 0)); /* TO SET */
                  //vePT.setThresholdForPrunning(0.0);

                  // Propagate

                  vePT.propagate();

                  // Present the results

                  pres = vePT;
                  showResults(pres);

                  break;

          /* Variable Elimination, Potential Trees and Constraints */

          case 2: Network bVEPTC=((IDiagram) bnetToPropagate).copy(); // Need to copy: barren nodes removed
                  IDVEWPTAndConstraints vePTC;

                  vePTC = new IDVEWPTAndConstraints((Bnet)bVEPTC,e);

                  // No interest variables
                  // vePTC.obtainInterest();

                  // Store the thresholdForPrunning

                  vePTC.setThresholdForPrunning(getFloatElement(parameters, 0));/* TO SET */
                  //vePTC.setThresholdForPrunning(0.0);

                  // Propagate

                  vePTC.propagate();

                  // Present the results

                  pres = vePTC;
                  showResults(pres);

                  break;

          /* Arc Reversal */

          case 3: ArcReversal eval;
                  boolean evaluable;
                  IDiagram id = ((IDiagram) bnetToPropagate).copy();
                  eval = new ArcReversal(id);

                  // It's necessary to copy, otherwise the real nodes are erased

                  // Initial tests about the node.

                  evaluable = eval.initialConditions();

                  // If the diagram is evaluable, do it.

                  if (evaluable == true) {
                    eval.evaluateDiagram();

                    // Make the results be accessible

                    pres = eval;
                    showResults(pres);
                  }

                  break;

          /* Arc Reversal, Potential Tress */

          case 4: ARWithPotentialTree evalPT;
                  boolean evaluablePT;
                  IDiagram idPT = ((IDiagram) bnetToPropagate).copy();
                  evalPT = new ARWithPotentialTree(idPT);

                  // It's necessary to copy, otherwise the real nodes are erased


                  // initial chekout about the node

                  evaluablePT = evalPT.initialConditions();

                  // Set the threshold for prunning operations

                  evalPT.setThresholdForPrunning(getFloatElement(parameters,0));/* TO SET */
                  //evalPT.setThresholdForPrunning(0.0);

                  // If the diagram is suitable to be evaluated, then do it.

                  if (evaluablePT == true) {
                    evalPT.evaluateDiagram();

                    // Let results accessible

                    pres = evalPT;
                    showResults(pres);
                  }

                  break;

          /* Arc Reversal, Potential Trees and Constraints */

          case 5: ARWPTAndConstraints evalPTC;
                  boolean evaluablePTC;
                  IDiagram idPTC = ((IDiagram) bnetToPropagate).copy();
                  evalPTC = new ARWPTAndConstraints(idPTC);

                  // It's necessary to copy, otherwise the real nodes are erased


                  // initial chekout about the node.

                  evaluablePTC = evalPTC.initialConditions();

                  // Set the threshold for prunning operations

                  evalPTC.setThresholdForPrunning(getFloatElement(parameters,0));/*TO SET*/
                  //evalPTC.setThresholdForPrunning(0.0);

                  // If the diagram is suitable to be evaluated, then do it.

                  if (evaluablePTC == true) {
                    evalPTC.evaluateDiagram();

                    // Let results be accessible

                    pres = evalPTC;
                    showResults(pres);
                  }

                  break;
        }
      }

      return pres;
   }



    public boolean propagate(Case c)
    {
    	CooperPolicyNetwork cpn=null;
    	//propagamos
    	if (getBayesNet().getClass()==Bnet.class){
       propag = propagateMethod(bayesNet,c,getInferenceMethod(),
                                              getParameters());
    	}
    	else{
    		
    		
    		cpn = ((IDiagram)bayesNet).getCpn();
    		
    		//POR HACER: MLUQUE
    		//propag = propagateMethod(cpn,new Case(cpn,c),getInferenceMethod(),getParameters());
    		//Compile the CPN with Hugin method
    		propag = propagateMethod(cpn,new Case(cpn,c),0,getParameters());
    	}
       Vector cpots=propag.results;

       if (propagResults.size()==(cases.getNumCurrentCase()-1))
           propagResults.addElement(propag);
       else
           propagResults.set(cases.getNumCurrentCase()-1,propag);
       
       showResults(propag);
       
       //System.out.println("CPT"+cpots);
       //obtenemos las probabilidades para cada nodo no observado
       boolean[] observed=c.getObserved();


       boolean correct=true;
       for (int j=0; j<bayesNet.getNodeList().size() && correct; j++){
       		Node auxNode;
       		
       		auxNode = bayesNet.getNodeList().elementAt(j);
          if (!observed[j]){
          	
          	if (auxNode.getClass() == FiniteStates.class){
          
             FiniteStates fs=(FiniteStates)auxNode;

             ((Node)bayesNet.getNodeList().elementAt(j)).setObserved(false);

             double[] postprob=new double[fs.getNumStates()];
             boolean found = false;
	         int  ip= 0;
	         while (!found && ip<cpots.size()) {
	      	        Potential p = (Potential) cpots.elementAt(ip);
	      	        //p.showResult();
                //if (fs.equals(((FiniteStates)p.getVariables().elementAt(0)))){
/*andrew*/      if (fs.getName().equals(((Node)p.getVariables().elementAt(0)).getName())){                        
                        found=true;
                        if (p.getClass()==PotentialContinuousPT.class){
                            PotentialTree aux2=new PotentialTree((PotentialContinuousPT)p);
                            p=new PotentialTable(aux2);
                        }
                        
                        postprob = ((PotentialTable) p).getValues();

                    }
                    else ip++;
	        }
            int ps=0;
            Double nan=new Double(Double.NaN);
            while (correct && ps<postprob.length){
                  Double d=new Double(postprob[ps]);
                  if (d.equals(nan))
                      correct=false;
                      else ps++;
            }
	        if (correct){
                c.setProbOfNode(fs, postprob);
            }
          }
          else{
          	int kindOfNode = auxNode.getKindOfNode();
          	if ((kindOfNode == Node.UTILITY)
					|| (kindOfNode == Node.SUPER_VALUE)){ /* mluque */
								
				boolean found=false;
				 double[] postprob=new double[1];

				
				for (int l = 0; ((l < cpots.size()) && (found == false)); l++) {
							PotentialTable p = (PotentialTable) cpots
									.elementAt(l);
							PotentialTable newPot;
							if (auxNode.getName().equals(
									((Node) p.getVariables().elementAt(0))
											.getName())) {
								found = true;

								newPot = p.convertProbabilityIntoUtility(cpn
										.getMinimumUtility(auxNode), cpn
										.getMaximumUtility(auxNode));
								double[] theValues = new double[(int) newPot
										.getSize()];
								Configuration config = new Configuration(newPot
										.getVariables());
								for (int k = 0; k < newPot.getSize(); k++) {
									theValues[k] = newPot.getValue(config);
									config.nextConfiguration();
								}
								postprob = theValues;
							}

				}
				
		        //Check if the evidence is not impossible
				Double nan=new Double(Double.NaN);
			    for (int i=0;(i<postprob.length)&&correct;i++){
			    	Double d=new Double(postprob[i]);
			    	if (d.equals(nan)){
			    		correct = false;
			    	}
			    }
			    if (correct){
			    	c.setProbOfNode(auxNode,postprob);
			    }
				
          }
					}
          }//if !observed...
          else{
              ((Node)bayesNet.getNodeList().elementAt(j)).setObserved(true);
          }
       }

       Elvira.getElviraFrame().setNodeName(c.getIdentifier());
       Elvira.getElviraFrame().setColorNodeName(c.getColor());
       c.setPropagated(true);
       c.setIsShown(true);
       if (cases.posCase(c)>cases.getLastShown())
           cases.setLastShown(cases.posCase(c));
       if (cases.posCase(c)<cases.getFirstShown())
           cases.setFirstShown(cases.posCase(c));

       repaint();
       return correct;
   }

    public void notpropagate(Case c){
       Elvira.getElviraFrame().setNodeName(c.getIdentifier());
       Elvira.getElviraFrame().setColorNodeName(c.getColor());
       c.setPropagated(false);
       c.setIsShown(true);
       if (cases.posCase(c)>cases.getLastShown())
           cases.setLastShown(cases.posCase(c));
       if (cases.posCase(c)<cases.getFirstShown())
           cases.setFirstShown(cases.posCase(c));
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
      Link link;    // a�adido <----------- jgamez
      Enumeration e, ee;
      LinkList linksToDraw;
      Bnet bn;
      boolean drawDiscontinuously = false;
      
      /*if (bayesNet.getClass()==Bnet.class) bn = bayesNet;
      else bn = ((IDiagram)bayesNet).getCpn();*/
      

      if (bayesNet == null) return;
      //if (bayesNet.getClass()!=Bnet.class) return; /* TEST */

      // draw a new arc upto current mouse position
      g2.scale(zoom, zoom);

      linksToDraw = linksToDraw();
      
      //    draw all arcs
     for (int i=0;i<linksToDraw.size();i++) {
		link = linksToDraw.elementAt(i);
		Color arcColor;
		//Existing links in original network are drawn of black,
		//but additional links are drawn are green
		boolean existsLink = (bayesNet.getLinkList().indexOf(link)!=-1);
		arcColor = Color.black;
		drawDiscontinuously = (existsLink==false);
				
		double wide=0.2;
		if ((showInfluences || (AUTOEXPLANATION || MACROEXPLANATION))&&(canInfluencesBeShown(link))){
		 if (isIncomingToSVNode(link)==false){
			double[][][] dist=macroExplanation.greaterdist(bayesNet, link.getHead(), link.getTail());
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
		 else{
			 wide = 0.2;
			 arcColor = ElviraPanel.GREATER_ARC_COLOR;
		 }
		
		
		
		}
       link.setColorLink(arcColor);
       link.setWidth(wide);
	   if (!MACROEXPLANATION)
	  	  VisualLink.drawArc(link,g2,dragging,selection,link.getColorLink(),drawDiscontinuously);
	   else if (MACROEXPLANATION && (link.getHead().getMarked() && link.getTail().getMarked()))
	  			VisualLink.drawArc(link,g2,dragging,selection,link.getColorLink(),drawDiscontinuously);
    }//end drawing arcs
     
    

      // draw the nodes
    g2.setFont(HELVETICA);
	Case comparingcase = null;
	//if (bayesNet.getClass() != IDiagram.class) { /* MODIFIED */
    if (bayesNet.getIsCompiled()) {
	  	System.out.println("Compiled");
                if (COMPARINGCASE==CASOP)
		  comparingcase=cases.getCaseNum(0);
	  	else if (COMPARINGCASE==CASOANT)
	 		 if (cases.getNumCurrentCase()==0)
		    	     comparingcase=cases.getCaseNum(0);
		      	 else comparingcase=cases.getCaseNum(cases.getNumCurrentCase()-1);
		     else comparingcase=cases.getCaseNum(compcase);
    }else 
            comparingcase=null;
    
        
        
     for (e = bayesNet.getNodeList().elements(); e.hasMoreElements(); ) {
   	  	node = (Node)e.nextElement();
		if (node.getPosX() >= 0) {
	  	  boolean enter = true;
	  	Font fontNode = getFont(node.getFont());
	  	FontMetrics fm;
		if (fontNode==null){
			fontNode = HELVETICA;
                  
		}
		fm=getFontMetrics(fontNode);
        Color colornode = NODE_COLOR;
                  


/*mluque*/	Class<? extends Node> nodeClass = node.getClass();
if ((comparingcase!=null) && (nodeClass!=Continuous.class) && (nodeClass!=NodeGSDAG.class)){
			  	 double[] distcompcase=macroExplanation.greaterdist(comparingcase, node);
			  	 double[] distcurrcase=macroExplanation.greaterdist(cases.getCurrentCase(), node);
			  	 double maxinf=macroExplanation.influences(distcompcase, distcurrcase);
			     int max=(int) ((maxinf*10000)/100);
			     if (macroExplanation.influencesTheta(maxinf,getTheta()))
              	    switch (macroExplanation.compare(distcompcase,distcurrcase)){
					  case 0: Color colorn=MORE_EXPLANATION_NODE_COLOR;
					  		  int alpha=colorn.getRed()-2*max-50;
							  colornode=new Color(colorn.getRed(), alpha, alpha);
		  					  break;
					  case 1: colorn=LESS_EXPLANATION_NODE_COLOR;
							  alpha=colorn.getBlue()-2*max-50;
							  colornode=new Color(alpha, alpha, colorn.getBlue());
		  					  break;
					  case 2: colornode=EQUALS_EXPLANATION_NODE_COLOR;
		  					  break;
					  case 3: colornode=UNKOWN_EXPLANATION_NODE_COLOR;
		  					  break;
				   }//end switch
			     else colornode=EQUALS_EXPLANATION_NODE_COLOR;
			 }
             if ((node.getExpanded()) && enter) {
                Node expfs=null;

	    	if (nodeClass == FiniteStates.class) 
                   expfs = new ExplanationFStates(this, bayesNet, (FiniteStates) node);
			else {
				int kindOfNode = node.getKindOfNode();
				if (nodeClass == Continuous.class){
					if ((kindOfNode!=Node.UTILITY)&&(kindOfNode!=Node.SUPER_VALUE)){
					
				/*andrew*/         expfs = new ExplanationContinuous(this, bayesNet, (Continuous) node);
					}
					else{ 
				                    //expfs = new ExplanationFStates(this, bayesNet, new FiniteStates("Utility",new Vector()));
				                	expfs = new ExplanationValueNode(this, (IDiagram)bayesNet, (Continuous) node);
					}
				}
				else if (nodeClass == NodeGSDAG.class){
					//In the future we could consider having expanded nodes in GSDAG
				}
				else{
					System.out.println("Error in the type of node to be displayed by InferencePanel class");
				}
			}
                	

                //System.out.println("inference panel: "+ MACROEXPLANATION +", "+ AUTOEXPLANATION+"\nnode:"+node);
                if (cases.getNumStoredCases()==0)
                        VisualNode.drawExpandedNode (node,g, NODE_COLOR, NODE_NAME_COLOR, expfs, byTitle, dragging, fm);
                else if (cases.getCurrentCase().getIsObserved(node))
                        VisualNode.drawExpandedNode (node,g, OBSERVED_NODE_COLOR,OBSERVED_NAME_COLOR, expfs, byTitle, dragging, fm);
                else if (MACROEXPLANATION && node.getMarked())
                        VisualNode.drawExpandedNode(node,g, colornode, NODE_NAME_COLOR, expfs, byTitle, dragging, fm); 
                else if (!MACROEXPLANATION && AUTOEXPLANATION)/* se pintan todos los nodos dependiendo de la influencia */
                        VisualNode.drawExpandedNode(node,g, colornode, NODE_NAME_COLOR, expfs, byTitle, dragging, fm); 
                else if (!MACROEXPLANATION && !AUTOEXPLANATION){/* se pinta del color habitual: amarillo*/
                		VisualNode.drawExpandedNode (node,g, NODE_COLOR, NODE_NAME_COLOR, expfs, byTitle, dragging, fm);
                }
	      			
	      } //if node.getExpanded && enter
     	      else{ //if (!(node.getExpanded() && enter))
	    	    if (node.getPosX() >= 0)
	      			if ((cases!=null)&& (cases.getNumStoredCases()!=0 && cases.getCurrentCase().getIsObserved(node)))
					VisualNode.drawNode (node,g2, OBSERVED_NODE_COLOR, OBSERVED_NAME_COLOR, byTitle, dragging);
	    			else if ((MACROEXPLANATION && node.getMarked()))
	    				VisualNode.drawNode(node,g2, colornode, NODE_NAME_COLOR, byTitle, dragging); 
				 else if (!MACROEXPLANATION && AUTOEXPLANATION)
		      			VisualNode.drawNode (node,g2, colornode, NODE_NAME_COLOR, byTitle, dragging);
	 	      		 else if (!AUTOEXPLANATION && !MACROEXPLANATION ) 
                                         VisualNode.drawNode (node,g2, NODE_COLOR, NODE_NAME_COLOR, byTitle, dragging);
		
                  }
                }//end if (node.getPosX()>=0)
      }//end for
   }//end paint



public static boolean isIncomingToSVNode(Link link) {
	// TODO Auto-generated method stub
	return (link.getHead().getKindOfNode()==Node.SUPER_VALUE);
}


private boolean canInfluencesBeShown(Link link) {
	// TODO Auto-generated method stub
	//return ((link.getTail().getKindOfNode()==Node.CHANCE)&&(link.getHead().getKindOfNode()==Node.CHANCE));
	return true;
}


/**
 * This method draws the arcs that don't appear in the influence diagram in edition mode, but
 * they exist in the inference mode, because they are informational arcs incoming to decisions.  
 */
private LinkList linksToDraw() {
	// TODO Auto-generated method stub
	NodeList decisions;
	Node decInCPN;
	Node decInID;
	Link linkInCPN;
	Link linkInID;
	LinkList links;
	LinkList auxLinksToDraw;
	Node auxTail;
	
	
	//All the links of the original network must be drawn
	auxLinksToDraw = bayesNet.getLinkList().copy();
	
	if ((bayesNet.getClass()==IDiagram.class)||(bayesNet.getClass()==IDWithSVNodes.class)){
		
	decisions = this.bayesNet.getNodesOfKind(Node.DECISION);
	
	//Additional arcs: Informational arcs incoming to decisions
	for (int i=0;i<decisions.size();i++){
		decInID = decisions.elementAt(i);
		decInCPN = ((IDiagram)bayesNet).getCpn().getNode(decInID.getName());
		
		//Incoming links to the decision in the CPN
		links = decInCPN.getParents();
		for (int j=0;j<links.size();j++){
			//For each link of the CPN, we see if it exists in the ID
			linkInCPN = links.elementAt(j);
			auxTail = bayesNet.getNode(linkInCPN.getTail().getName());
			linkInID = bayesNet.getLink(auxTail,decInID);
			if (linkInID == null){ //The link of the CPN doesn't exist in the ID, so we must draw it
				auxLinksToDraw.insertLink(new Link(auxTail,decInID));
				
			}
			
		}
	}
	}
	return auxLinksToDraw;
}


/**
    * Move the nodes and links selected the distance
    * contained in the p parameter
    */

   public void move(Point p) {
      moveSelection(p);

	   MoveEdit moveAction = new MoveEdit(selection,p);
	   getUndoItem().setText(moveAction.getUndoPresentationName());
	   getRedoItem().setText(moveAction.getRedoPresentationName());
	   undoManager.addEdit(moveAction);
	   Elvira.getElviraFrame().enableUndo(true);
	   Elvira.getElviraFrame().enableRedo(false);
	}


   /**
    * Check if the the options of the inferencePanel that
    * must be enabled when a node or a link is selected
    * and enables or disables it
    */

   public void checkSelected () {

      if (selection==null)
         return;

      int links = selection.numberOfLinks(),
          nodes = selection.numberOfNodes(),
          mode;

      /* The explain option only will be active if
         there is only one link or node selected */

      if (links==0 && nodes==1)
         mode = EXPLAIN_NODE_MODE;
      else if (links==1 && nodes==0)
         mode = EXPLAIN_LINK_MODE;
      else
         mode = DISABLED_MODE;

      /*if ((getBayesNet().getClass()!=IDiagram.class)&&(getBayesNet().getClass()!=IDWithSVNodes.class))
         Elvira.getElviraFrame().setExplainName(mode);*/
      //Also for IDiagram and IDWithSVNodes
      Elvira.getElviraFrame().setExplainName(mode);

      if (nodes==0)
         Elvira.getElviraFrame().enableExpand(false);
      else {
         int i=0;
         boolean find = false;
         while (i<selection.numberOfNodes() && !find) {
            Node n = selection.getNode(i);
            if (!n.getExpanded()) {
               expandMode = EXPANDED;
               find = true;
            }
            i++;
         }
         if (!find) expandMode = CONTRACTED;

         Elvira.getElviraFrame().setExpandName(expandMode);
         Elvira.getElviraFrame().enableExpand(true);
      }

   }

   public void expandNodes(){
   	  expandNodes(getExpansionThreshold(), getFunctionThreshold());
   }

   /**
    * Expand all the nodes whose relevancia are higher than
    * the value given as parameter. If there is an expanded
    * node whose value are lower than this value
    */

   public void expandNodes(double threshold, String[] functions) {
       for (Enumeration e = bayesNet.getNodeList().elements(); e.hasMoreElements(); ) {
           Node node = (Node) e.nextElement();
           /*if (((bayesNet.getClass() == IDiagram.class) || (bayesNet.getClass() == IDWithSVNodes.class))&&
           ((node.getKindOfNode() == Node.CHANCE) || (node.getKindOfNode() == Node.DECISION)))
                MODIFIED 
               node.setExpanded(false);
           else{*/
               boolean toExpand=true;
               if (purposeMode) {
                   
                   toExpand=false;
                   
                   for (int f=0; f<functions.length && !toExpand; f++)
                       if (functions[f]!=null){
                           //                  	System.out.print((String)functions[f]+" ");
                           if (EditVariableDialog.isPredefined(node.getPurpose())){
                               if (node.getPurpose().equals((String) functions[f]))
                                   toExpand=true;
                           }
                           else if (functions[f].equals("Defined"))
                               toExpand=true;
                       }
               }
               
              //Super value nodes and utility nodes are also expanded
               /*if (((node.getKindOfNode()==Node.UTILITY)||(node.getKindOfNode()==Node.SUPER_VALUE))&&(node.getChildrenNodes().size()>0))
                   toExpand=false;*/
               
               
               if (node.getRelevance()>=threshold && toExpand)
                   node.setExpanded(true);
               else node.setExpanded(false);
           //}
       }
       repaint();
       
   }


	/**
	 * Manage the actions produced in the ElviraPopupMenu object
	 */

	class InferencePopupAction implements java.awt.event.ActionListener{
		public void actionPerformed(java.awt.event.ActionEvent event){
		  Object object = event.getSource();
          if (object == explainMenuItem)
			  explainMenuItem_actionPerformed(event);
          else if (object == expandMenuItem)
			   	   expandMenuItem_actionPerformed(event);
          else if (object == propertiesMenuItem)
		      			editMenuItem_actionPerformed(event);
          else if (object == explainFunctionMenuItem)
		      			explainFunctionMenuItem_actionPerformed(event);
          else if (object == showDecisionTableMenuItem)
          				showDecisionTableMenuItem_actionPerformed(event);
          else if (object == showDecisionPolicyTreeMenuItem){
        	  			showDecisionPolicyTreeMenuItem_actionPerformed(event);
          }
          else if (object == showPosteriorDistributionsTableMenuItem)
				showPosteriorDistributionsTableMenuItem_actionPerformed(event);
          else if (object == showUtilitiesTableMenuItem)
			showUtilitiesTableMenuItem_actionPerformed(event);
                  
          }

		private void showDecisionPolicyTreeMenuItem_actionPerformed(
				ActionEvent event) {
			// TODO Auto-generated method stub
			Elvira.getElviraFrame().getElviraGUIMediatorPT().policyTreeAction(event, (FiniteStates) currentNode);
			
		}

		/**
		 * @param event
		 */
		private void showUtilitiesTableMenuItem_actionPerformed(ActionEvent event) {
			// TODO Auto-generated method stub
			showConditionalTableMenuItem(ShowConditionalTable.UTILITIES);
			
		}

		/**
		 * @param event
		 */
		private void showPosteriorDistributionsTableMenuItem_actionPerformed(ActionEvent event) {
			
			// TODO Auto-generated method stub
			
			showConditionalTableMenuItem(ShowConditionalTable.PROBABILITIES);
		}
		
		/**
		 * @param typeOfValues
		 */
		private void showConditionalTableMenuItem(int typeOfValues) {
			// TODO Auto-generated method stub
			String msg;
			Propagation prop;
			IDWithSVNodes id;
			
			if (typeOfValues==ShowConditionalTable.PROBABILITIES){
				msg = ShowMessages.RECOMPILE_ID_PROBABILITIES;
			}
			else{//typeOfValues==ShowConditionalTable.UTILITIES)
				msg = ShowMessages.RECOMPILE_ID_UTILITIES;
			}
			
			
			id = (IDWithSVNodes)(getBayesNet());
			
			prop = id.getPropagation();
			
			
		 if ((prop.getClass()==ArcReversal.class)||(prop.getClass()==ArcReversalSV.class)){
		 	ShowConditionalTable spd = new ShowConditionalTable(currentNode,id,typeOfValues);
			spd.show();
		 }
		 else{
		 	//User can choose the influence diagram with Tatman and Shachter's method
		 	//so that posterior distributions and utilities can be computed.
		 	int reply;
		 	Object[] options = { localize(dialogBundle,"Yes.label"), localize(dialogBundle,"No.label")};
		 	
		 	reply = ShowMessages.showOptionDialog(msg,JOptionPane.QUESTION_MESSAGE,options, 0);
		 	
		 	if (reply == 0){ //The answer is YES
		 			//Compile the id with Tatman and Shachter's method
		 			setInferenceMethod(3);
		 			//Elvira.getElviraFrame().getNetworkFrame().activeInferencePanel();
		 			id.compile(getInferenceMethod(),getParameters());
		 			//Necessary to access to posterior probabilities and utilities
		 			id.showResults(id.getPropagation());
		 			
		 			ShowConditionalTable spd = new ShowConditionalTable(currentNode,id,typeOfValues);
					spd.show();

		 	}
		 }	
		}
			
		

	

		/**
		 * @param event
		 */
		private void showDecisionTableMenuItem_actionPerformed(ActionEvent event) {
			// TODO Auto-generated method stub
			
			if (getBayesNet().getClass()!=GSDAG.class){
			ShowDecisionTable sdd = new ShowDecisionTable(currentNode,getBayesNet(),inferenceMethod);
			//sdd.show();
			sdd.setVisible(true);
			}
			else{//UID
				ShowDecisionTable sdd = new ShowDecisionTableUID((NodeGSDAG) currentNode,(GSDAG) getBayesNet());
				//sdd.show();
				sdd.setVisible(true);
			}
		}
                
                
	}

        /*andrew*/
        void explainFunctionMenuItem_actionPerformed(java.awt.event.ActionEvent event){
                
            
      	    if (currentNode!=null && currentNode.getClass() == Continuous.class) {
                ExplanationContinuous expfs = new ExplanationContinuous(this, bayesNet, (Continuous) currentNode);
                ExplanationDensity ef = new ExplanationDensity(expfs);
            }
        }
	
        
        
        void expandMenuItem_actionPerformed(java.awt.event.ActionEvent event){
	   boolean value;
	   if (expandMode==EXPANDED)
	      value = true;
	   else
	      value = false;

	   if (selection!=null)
	      for (int i=0; i<selection.numberOfNodes(); i++) {
	         Node n = (Node) selection.getNode(i);
            if (((bayesNet.getClass() == IDiagram.class)||(bayesNet.getClass() == IDWithSVNodes.class))
               &&((n.getKindOfNode() == Node.CHANCE) || (n.getKindOfNode() == Node.DECISION)))
 /* MODIFIED */
               n.setExpanded(false);
            else
	            n.setExpanded(value);
	      }

	   repaint();
	   checkSelected();
	}


   void storeCase_actionPerformed (java.awt.event.ActionEvent event) {
      //almacena el caso activo en la lista de casos y genera uno nuevo
      if (cases.getNumCurrentCase()!=cases.getNumActiveCase())
           ShowMessages.showMessageDialog(ShowMessages.CASE_ALREADY_STORED,
                     JOptionPane.ERROR_MESSAGE);

          else if (cases.getMaxNumStoredCases()==cases.getNumStoredCases())
                     ShowMessages.showMessageDialog(ShowMessages.FULL_CASESLIST,
                     JOptionPane.ERROR_MESSAGE);

               else {
                     cases.storeCase(cases.getActiveCase());
                     Elvira.getElviraFrame().setNodeName(cases.getCurrentCase().getIdentifier());
                     Elvira.getElviraFrame().setColorNodeName(cases.getCurrentCase().getColor());
                     repaint();
              }
   }

   void firstCaseItem_actionPerformed(java.awt.event.ActionEvent event){
     if (cases.getNumCurrentCase()==cases.getFirstShown())
        ShowMessages.showMessageDialog(ShowMessages.FIRST_CASE, JOptionPane.ERROR_MESSAGE);
     else {
           Case c=cases.getCaseNum(cases.getFirstShown());
           Elvira.getElviraFrame().setNodeName(c.getIdentifier());
           Elvira.getElviraFrame().setColorNodeName(c.getColor());
           cases.setCurrentCase(cases.getFirstShown());

           //recorremos todos los nodos para marcarlos como no observados pues es la probabilidad a priori
           for (int n=0; n<bayesNet.getNodeList().size(); n++)
                ((Node)bayesNet.getNodeList().elementAt(n)).setObserved(false);
           repaint();
     }
   }

   void nextCaseItem_actionPerformed(java.awt.event.ActionEvent event){
      if (cases.getNumCurrentCase()==(cases.getLastShown()) )
           ShowMessages.showMessageDialog(ShowMessages.NEXT_CASE,
                     JOptionPane.ERROR_MESSAGE);
      else {
            int casenum=cases.getNumCurrentCase()+1;
            while (!cases.getCaseNum(casenum).getIsShown() && casenum<cases.getLastShown())
                   casenum++;
            if (casenum>cases.getLastShown())
                ShowMessages.showMessageDialog(ShowMessages.NEXT_CASE, JOptionPane.ERROR_MESSAGE);
            else {
                  Case c=cases.getCaseNum(casenum);
                  cases.setCurrentCase(casenum);
                  Elvira.getElviraFrame().setNodeName(c.getIdentifier());
                  Elvira.getElviraFrame().setColorNodeName(c.getColor());
                  boolean[] o=c.getObserved();
                  for (int n=0; n<bayesNet.getNodeList().size(); n++)
                       if (o[n])
                          ((Node)bayesNet.getNodeList().elementAt(n)).setObserved(true);
                       else ((Node)bayesNet.getNodeList().elementAt(n)).setObserved(false);
            }
            repaint();
      }
   }

   void previousCaseItem_actionPerformed(java.awt.event.ActionEvent event){
      if (cases.getNumCurrentCase()==cases.getFirstShown())
           ShowMessages.showMessageDialog(ShowMessages.PREVIOUS_CASE,
                     JOptionPane.ERROR_MESSAGE);

         else {
               int casenum=cases.getNumCurrentCase()-1;
               while (!cases.getCaseNum(casenum).getIsShown() && casenum>=cases.getFirstShown())
                      casenum--;
               if (casenum<cases.getFirstShown())
                   ShowMessages.showMessageDialog(ShowMessages.NEXT_CASE, JOptionPane.ERROR_MESSAGE);
               else {
                     Case c=cases.getCaseNum(casenum);
                     Elvira.getElviraFrame().setNodeName(c.getIdentifier());
                     Elvira.getElviraFrame().setColorNodeName(c.getColor());
                     cases.setCurrentCase(casenum);
                     boolean[] o=c.getObserved();
                     for (int n=0; n<bayesNet.getNodeList().size(); n++)
                         if (o[n])
                            ((Node)bayesNet.getNodeList().elementAt(n)).setObserved(true);
                         else ((Node)bayesNet.getNodeList().elementAt(n)).setObserved(false);
                     repaint();
               }
         }
   }

   void lastCaseItem_actionPerformed(java.awt.event.ActionEvent event){
      if (cases.getNumCurrentCase()==(cases.getLastShown()))
           ShowMessages.showMessageDialog(ShowMessages.LAST_CASE,
                     JOptionPane.ERROR_MESSAGE);

      else {
            Case c=cases.getCaseNum(cases.getLastShown());
            cases.setCurrentCase(cases.getLastShown());
            Elvira.getElviraFrame().setNodeName(cases.getCurrentCase().getIdentifier());
            Elvira.getElviraFrame().setColorNodeName(cases.getCurrentCase().getColor());
            boolean[] o=cases.getCurrentCase().getObserved();
            for (int n=0; n<bayesNet.getNodeList().size(); n++)
                 if (o[n])
                    ((Node)bayesNet.getNodeList().elementAt(n)).setObserved(true);
                 else ((Node)bayesNet.getNodeList().elementAt(n)).setObserved(false);
            repaint();
      }
   }


/**
 * @param cpn The cpn to set.
 *//*
public void setCpn(CooperPolicyNetwork cpn) {
	this.cpn = cpn;
}*/
}  // end of InferencePanel class

