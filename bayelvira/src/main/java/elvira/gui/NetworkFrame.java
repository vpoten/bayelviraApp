/* NetworkFrame.java */

package elvira.gui;

import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.util.Enumeration;
import java.util.Vector;
import elvira.*;
import elvira.gui.explication.*;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.inference.uids.DynamicUID;
import elvira.inference.uids.GSDAG;
import elvira.Elvira;
import elvira.learning.*;
import elvira.learning.constraints.*;
import elvira.database.*;
//import elvira.parser.*;
import java.io.*;


/**
 *	This class let us having more than one network open in a
 * ElviraFrame. Every network will be placed in one NetworkFrame
 * object
 *
 * @author ..., fjdiez, ratienza, ...
 * @version 1.53
 * @since 01/06/04
 * @see EditorPanel
 */

public class NetworkFrame extends javax.swing.JInternalFrame
{

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	private JSplitPane networkSplitPane = new JSplitPane();
	private JScrollPane networkPane = new JScrollPane();
	private EditorPanel networkEditorPanel = new EditorPanel();
	private InferencePanel networkInferencePanel = new InferencePanel();
	private LearningPanel networkLearningPanel = new LearningPanel();

        private GenerateDBCPanel GeneratePanel = new GenerateDBCPanel();
        private ConstraintKnowledgePanel CKPanel = new ConstraintKnowledgePanel();

        private ConstraintKnowledge CK= null; 
        private FileTree f;

        private NodeList Max=new NodeList();
        private NodeList Min=new NodeList();

	public static final int EDITOR_ACTIVE = 0;
	public static final int INFERENCE_ACTIVE = 1;
	public static final int LEARNING_ACTIVE = 2;

	public int mode = EDITOR_ACTIVE;

	private boolean isNew;
	private UndoManager undoManager;

	private int numvez=0;

        public String fileName;

	/**
	 * Creates a new empty NewtworkFrame
	 */

	public NetworkFrame()
	{
		setIconifiable(true);
		getContentPane().setLayout(new BorderLayout(0,0));
		getContentPane().setBackground(java.awt.Color.white);
		setSize(500,400);
		//networkSplitPane.setContinuousLayout(true);
		//getContentPane().add("Center",networkSplitPane);
		getContentPane().add("Center",networkPane);
		//networkSplitPane.setBounds(0,0,400,400);

		networkPane.setOpaque(true);
		networkPane.setBounds(0,0,400,400);
		networkPane.getViewport().add(networkEditorPanel);
		networkPane.setPreferredSize(new Dimension(400,400));
                
                undoManager = new UndoManager();
                networkEditorPanel.setUndoManager(undoManager);
		networkEditorPanel.setAutoscrolls(true);
		networkEditorPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		networkEditorPanel.setBackground(java.awt.Color.white);
		networkEditorPanel.setBounds(0,0,400,400);

		//networkSplitPane.setBottomComponent(networkPane);
		JViewport view = networkPane.getViewport();
		Dimension d = view.getViewSize();
		d.setSize(d.getHeight()*1.5,d.getWidth()*1.5);
		view.setViewSize(d);

                addVetoableChangeListener(new CloseListener());

		SymInternalFrame lSymInternalFrame = new SymInternalFrame();
		this.addInternalFrameListener(lSymInternalFrame);

		networkInferencePanel.setUndoManager(undoManager);
		networkInferencePanel.setAutoscrolls(true);
		networkInferencePanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		networkInferencePanel.setBackground(java.awt.Color.white);
		networkInferencePanel.setBounds(0,0,400,400);
		networkInferencePanel.setBayesNet(networkEditorPanel.getBayesNet());

	}


	/**
	 * Creates a new empty NetworkFrame a set its title
	 *
	 * @param sTitle The title of the frame
	 */

	public NetworkFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}


	/**
	 * Creates a new empty NetworkFrame and set its main
	 * properties
	 *
	 * @param sTitle The title of the frame
	 * @param resizable True if the NetworkFrame can be resized
	 * @param closable True if the NetworkFrame can be closed
	 * @param maximizable True if the NetworkFrame can be maximized
	 * @param iconifiable True if the NetworkFrame can be iconified
	 * @param b True if the network that will be placed in the
	          NetworkFrame is new
	 */

	public NetworkFrame (String sTitle, boolean resizable, boolean closable,
	                     boolean maximizable, boolean iconifiable, boolean b)
	{
	   this(sTitle);
	   setResizable(resizable);
	   setClosable(closable);
	   setMaximizable(maximizable);
	   setIconifiable(iconifiable);
	   isNew = b;
	}


        /**
         * Shows or hides the NetworkFrame component depending
         * on the value of parameter b.
         */

	/*public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}*/

	static public void main(String args[])
	{
		(new NetworkFrame()).setVisible(true);
	}


	/**
	 * Notifies this component that it has been added to a container
	 * and if a peer is required, it should be created. This method
	 * should be called by Container.add, and not by user code directly
	 */

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets and menu bar
		Insets insets = getInsets();
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
			menuBarHeight = menuBar.getPreferredSize().height;
		int offset = 0;
		Component comp[] = getComponents();
		for(int i = 0; i < comp.length; ++i) {
			if (comp[i] instanceof javax.swing.JRootPane)
				continue;
			offset += comp[i].getPreferredSize().height;
		}
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight + offset);
	}


	/**
	 * Method for accesing to the EditorPanel
	 */

	public EditorPanel getEditorPanel () {
	   return networkEditorPanel;
	}


	/**
	 * Method for accesing to the InferencePanel
	 */

	public InferencePanel getInferencePanel() {
	   return networkInferencePanel;
	}

        public GenerateDBCPanel getGenerateDBCPanel() {
	   return GeneratePanel;
	}

        public ConstraintKnowledgePanel getConstraintKnowledgePanel() {
	   return CKPanel;
	}


	public LearningPanel getLearningPanel() {
	   return networkLearningPanel;
	}

	public int getMode() {
	   return mode;
	}

	public boolean isNew() {
	   return isNew;
	}


	/**
	 * Active the EditorPanel into the networkPane
	 */

	public void activeEditorPanel () {
	  Bnet b=networkEditorPanel.getBayesNet();
	  b.setCompiled(false);
	  //siempre que se pasa a modo edición, se le asigna a la variable isModified el valor
	  //true para que luego al pasar a modo inferencia se genere la lista de casos vacía
	  b.setIsModified(true);
	  removePanel();
	  networkPane.getViewport().add(networkEditorPanel);
	  networkEditorPanel.setMode(EditorPanel.MOVE);
	  networkEditorPanel.unSelectAll();
	  Elvira.getElviraFrame().activeSelect();
	  mode = EDITOR_ACTIVE;
	  repaint();
	}


   public int getElement(Vector parameters, int i) {
      Integer element = (Integer) parameters.elementAt(i);
      return element.intValue();
   }

   public double getFloatElement(Vector parameters, int i) {
      Double element = (Double) parameters.elementAt(i);
      return element.doubleValue();
   }

   public String getStringElement(Vector parameters, int i) {
      String element = (String) parameters.elementAt(i);
      return element;
   }


 // Get and save a .dbc file
   public void GenerateDBC(int memory,Vector parameters)
   {
   try {

   Bnet red = (Bnet) networkEditorPanel.getBayesNet();

   int num_casos=GeneratePanel.getNumberCases();

   String fileName=GeneratePanel.getfileName();


  String path=fileName+".dbc";
  boolean active_memory;

  if (memory==0)
     {
     active_memory=true;
     }
  else
     {
     active_memory=false;
     }

  FileWriter f3 = new FileWriter(path);
  DataBaseCases dataBase = new DataBaseCases(red,f3,num_casos,active_memory);
  f3.close();

  }
           catch (IOException ioe){

            System.out.println(ioe.getMessage());
        }



   }

   public void activeGenerateDBCPanel () {

           int mem=GeneratePanel.getSelectionMemory();

           GenerateDBC(mem,networkLearningPanel.getParameters());

   }

    /**
     * Save the constraints of the ConstraintKnowledgePanel in the 
     * ConstraintKnowledge object
     */
    public void activeConstraintKnowledgePanel () {
       this.CK=this.CKPanel.getConstraints();
     }

    /**
     * Get the knowledge constraints.
     *  @return the knowledge constraints, if the constraints are empty will return null
     */
    public ConstraintKnowledge getConstraintKnowledge () {
      return this.CK;
    }





   public Bnet learn(int index, Vector parameters) throws InvalidEditException {
      DataBaseCases cases = (DataBaseCases) networkEditorPanel.getBayesNet();
      Bnet baprend = (Bnet) cases;
      int n=baprend.getNodeList().size();
	System.out.println("\nInitial net: ");
      System.out.println("Number of Nodes: "+n);

      LinkList l;
      l=new LinkList();
      l=baprend.getLinkList();

      //System.out.println("Nodos: "+n);
      System.out.println("Number of Links: "+l.size()+"\n");

      int method1;

      switch (index) {
         /*
          case 0: BBenedict bBenedict = new BBenedict(cases,0.01,0.01);
                 fileName=getStringElement(parameters,0);
                 bBenedict.learning();
                 //return (Bnet)bBenedict.getOutput();

                 method1=networkLearningPanel.getParameterMethod();

                 if (method1==1)
                 {
                 LPLearning lpLearning = new LPLearning(cases, (Bnet)bBenedict.getOutput());
                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
                 }
                 else //method1=0
                 {
                 DELearning deLearning = new DELearning(cases, (Bnet)bBenedict.getOutput());
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
                 }

           */


         /*
         case 1: BICLearning bicLearning = new BICLearning(cases);
                 fileName=getStringElement(parameters,0);
                 bicLearning.learning();
                 //return (Bnet) bicLearning.getOutput();

                 method1=networkLearningPanel.getParameterMethod();

                 if (method1==1)
                 {
                 LPLearning lpLearning = new LPLearning(cases, (Bnet) bicLearning.getOutput());
                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
                 }
                 else //method1=0
                 {
                 DELearning deLearning = new DELearning(cases, (Bnet) bicLearning.getOutput());
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
                 }



          *//*
         case 2: ClassifierGALearning outputNet1 = new ClassifierGALearning(cases,baprend);
                 fileName=getStringElement(parameters,3);
                 /*
                 return outputNet1.learn (getElement(parameters,0),
                               getElement(parameters,1),
                               getElement(parameters,2));

                  */
              /*
                 method1=networkLearningPanel.getParameterMethod();

                 if (method1==1)
                 {
                 LPLearning lpLearning = new LPLearning(cases, outputNet1.learn (getElement(parameters,0),
                               getElement(parameters,1),
                               getElement(parameters,2)));
                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
                 }
                 else //method1=0
                 {
                 DELearning deLearning = new DELearning(cases, outputNet1.learn (getElement(parameters,0),
                               getElement(parameters,1),
                               getElement(parameters,2)));
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
                 }


         /*
         case 3: DELearning deLearning = new DELearning(cases, baprend);
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
          */
         /*
         case 3: K2GALearning k2GALearning = new K2GALearning(cases,getElement(parameters,3));
                 fileName=getStringElement(parameters,4);
                 /*
                 return k2GALearning.learn(getElement(parameters,0),
                                   getElement(parameters,1),
                                   getElement(parameters,2));
                 *//*
                 method1=networkLearningPanel.getParameterMethod();

                 if (method1==1)
                 {
                 LPLearning lpLearning = new LPLearning(cases, k2GALearning.learn(getElement(parameters,0),
                                   getElement(parameters,1),
                                   getElement(parameters,2)));
                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
                 }
                 else //method1=0
                 {
                 DELearning deLearning = new DELearning(cases,k2GALearning.learn(getElement(parameters,0),
                                   getElement(parameters,1),
                                   getElement(parameters,2)));
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
                 }


         */
         case 0: K2Learning k2Learning = new K2Learning(cases,getElement(parameters,0));
                 fileName=getStringElement(parameters,1);
                 k2Learning.learning();
                 //return (Bnet)k2Learning.getOutput();

                 method1=networkLearningPanel.getParameterMethod();

		 //We use the constraints knowledge if we have it to repair the learned bnet
		 if ( (this.CK!=null) && (!CK.isEmpty()) ) {

		     System.out.println("Using Constraints");

		     if ( !CK.test(k2Learning.getOutput()) ) {
			 //Ask to repair or not
			 String question = ElviraFrame.localize(Elvira.getElviraFrame().getDialogBundle(),
									    "ConstraintKnowledgeDialog.repair");
			 int deleteselected = JOptionPane.showConfirmDialog(this, question, "Warning",
									    JOptionPane.YES_NO_OPTION);
			 //If the option is Yes, we repair it
			 if (deleteselected == JOptionPane.YES_OPTION) {
			     System.out.println("Repair Bnet");
			     k2Learning.setOutput( CK.repair(k2Learning.getOutput()) );
			 }
		     }
		 }


                 if (method1==1)
                 {
                 LPLearning lpLearning = new LPLearning(cases, (Bnet)k2Learning.getOutput());
                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
                 }
                 else //method1=0
                 {
                 DELearning deLearning = new DELearning(cases,(Bnet)k2Learning.getOutput());
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
                 }


         /*

         case 5: K2TSPLearning k2TSPLearning = new K2TSPLearning(cases,getElement(parameters,3));
                 k2TSPLearning.setInput (cases);
                 fileName=getStringElement(parameters,4);
                 k2TSPLearning.setMaxParents (getElement(parameters, 3));
                 /*
                 return k2TSPLearning.learnTSP(getElement(parameters,0),
                                   getElement(parameters,1),
                                   getElement(parameters,2));
                 *//*
                 method1=networkLearningPanel.getParameterMethod();

                 if (method1==1)
                 {
                 LPLearning lpLearning = new LPLearning(cases, k2TSPLearning.learnTSP(getElement(parameters,0),
                                   getElement(parameters,1),
                                   getElement(parameters,2)));
                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
                 }
                 else //method1=0
                 {
                 DELearning deLearning = new DELearning(cases, k2TSPLearning.learnTSP(getElement(parameters,0),
                                   getElement(parameters,1),
                                   getElement(parameters,2)));
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
                 }


         /*
         case 7: LPLearning lpLearning = new LPLearning(cases, baprend);
                 fileName=getStringElement(parameters,0);
                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
        */
         case 1: cases.setNumberOfCases(getElement(parameters,0));
                 fileName=getStringElement(parameters,2);
		 Learning pcLearning;
		 float confidence=(float)(getElement(parameters,1))/100;
		 if ( (this.CK!=null) && (!CK.isEmpty()) ) { 
		     System.out.println("Using Constraints");
		     PCLearningCK pclearningck = new PCLearningCK(CK,cases);
		     pclearningck.setLevelOfConfidence(confidence);
		     pclearningck.learning();
		     pcLearning=pclearningck;
		 } else {
		     PCLearning pclearning = new PCLearning(cases);
		     pclearning.setLevelOfConfidence(confidence);
		     pclearning.learning();
		     pcLearning=pclearning;
		 }

                 method1=networkLearningPanel.getParameterMethod();
                 if (method1==1) {
		     LPLearning lpLearning = new LPLearning(cases, (Bnet) pcLearning.getOutput());
		     lpLearning.learning();
		     return (Bnet) lpLearning.getOutput();
                 } else {
		     //method1=0
		     DELearning deLearning = new DELearning(cases, (Bnet) pcLearning.getOutput());
		     deLearning.learning();
		     return (Bnet) deLearning.getOutput();
                 }

         /*
         case 7: tanga t = new tanga(cases,baprend);
                 fileName=getStringElement(parameters,3);
                 /*return t.learn(getElement(parameters,0),
                                getElement(parameters,1),
                                getElement(parameters,2));
                 */ /*
                 method1=networkLearningPanel.getParameterMethod();

                 if (method1==1)
                 {
                 LPLearning lpLearning = new LPLearning(cases, t.learn(getElement(parameters,0),
                                getElement(parameters,1),
                                getElement(parameters,2)));

                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
                 }
                 else //method1=0
                 {
                 DELearning deLearning = new DELearning(cases,t.learn(getElement(parameters,0),
                                getElement(parameters,1),
                                getElement(parameters,2)));
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
                 }
                 */
         case 2: int metrica=networkLearningPanel.getMetric();

                 Metrics met=null;

                 if (metrica==0) met = (Metrics) new BICMetrics(cases);
                 if (metrica==1) met = (Metrics) new K2Metrics(cases);
                 if (metrica==2) met = (Metrics) new BDeMetrics(cases);

		 //We use the constraints knowledge if we have it
		 Learning dv;
		 if ( (this.CK!=null) && (!CK.isEmpty()) ) {
		     System.out.println("Using Constraints");
		     dv = new DVNSSTLearningCK(cases,getElement(parameters,0),getElement(parameters,2),
					       getElement(parameters,3),getElement(parameters,1),met,this.CK);
		     dv.learning();
		 } else {
		     dv = new DVNSSTLearning(cases,getElement(parameters,0),getElement(parameters,2),
					     getElement(parameters,3),getElement(parameters,1),met);
		     dv.learning();
		 }
	 
                 fileName=getStringElement(parameters,4);
                 method1=networkLearningPanel.getParameterMethod();
                 if (method1==1) {
		     LPLearning lpLearning = new LPLearning(cases, (Bnet) dv.getOutput());
		     lpLearning.learning();
		     return (Bnet) lpLearning.getOutput();
                 } else { //method1=0
		     DELearning deLearning = new DELearning(cases,(Bnet) dv.getOutput());
		     deLearning.learning();
		     return (Bnet) deLearning.getOutput();
                 }

          case 3:metrica=networkLearningPanel.getMetric();

                 
                 met = (Metrics) new BICMetrics(cases);                 
          
                 if (metrica==0) met = (Metrics) new BICMetrics(cases);
                 if (metrica==1) met = (Metrics) new K2Metrics(cases);
                 if (metrica==2) met = (Metrics) new BDeMetrics(cases);

                 

                 fileName=getStringElement(parameters,1);
                 int numberparents=getElement(parameters,0);

                 K2SNOPT k2s = new K2SNOPT(cases,numberparents,met,1,0.0,n);


                 method1=networkLearningPanel.getParameterMethod();
                 k2s.learning();

		 //We use the constraints knowledge if we have it to repair the learned bnet
		 if ( (this.CK!=null) && (!CK.isEmpty()) ) {

		     System.out.println("Using Constraints");

		     if ( !CK.test(k2s.getOutput()) ) {
			 //Ask to repair or not
			 String question = ElviraFrame.localize(Elvira.getElviraFrame().getDialogBundle(),
									    "ConstraintKnowledgeDialog.repair");
			 int deleteselected = JOptionPane.showConfirmDialog(this, question, "Warning",
									    JOptionPane.YES_NO_OPTION);
			 //If the option is Yes, we repair it
			 if (deleteselected == JOptionPane.YES_OPTION) {
			     System.out.println("Repair Bnet");
			     k2s.setOutput( CK.repair(k2s.getOutput()) );
			 }
		     }
		 }

                 if (method1==1)
                 {
                 LPLearning lpLearning = new LPLearning(cases, (Bnet) k2s.getOutput());
                 lpLearning.learning();
                 return (Bnet) lpLearning.getOutput();
                 }
                 else //method1=0
                 {
                 DELearning deLearning = new DELearning(cases,(Bnet) k2s.getOutput());
                 deLearning.learning();
                 return (Bnet) deLearning.getOutput();
                 }
	case 4:		//Structural MTE Learning
		StructuralMTELearning StrucMTELearning = new StructuralMTELearning(cases);
                //fileName=getStringElement(parameters,1);
		boolean noContinuousVar = true;
		NodeList vars = cases.getVariables();
	        for(int i=0 ; i< vars.size(); i++){
	            Node var = (Node) vars.elementAt(i);
		    if (var.getTypeOfVariable()==Node.CONTINUOUS)
		    	noContinuousVar = false;
	        }
		if (noContinuousVar){
			System.out.println("\nThere is not any continuous variable. Please, use one method for finite-states variables.");
			return null;
		}
		else{
	                StrucMTELearning.structuralLearning();
			return StrucMTELearning.getOutput();
		}

      }
      return null;
   }



	public void activeLearningPanel () throws InvalidEditException {
           try {
	   Bnet b;
           int method=networkLearningPanel.getLearningMethod();
           int method1=networkLearningPanel.getParameterMethod();

           System.out.print("Parameter: "+method1+"\n");

           DataBaseCases cases = (DataBaseCases) networkEditorPanel.getBayesNet();
           System.out.print("Nombre de la Red: "+networkEditorPanel.getBayesNet().getName());

           removePanel();
	   mode = LEARNING_ACTIVE;
	   networkPane.getViewport().add(networkLearningPanel);
           b = learn(networkLearningPanel.getLearningMethod(),
	                  networkLearningPanel.getParameters());
	   networkLearningPanel.setBayesNet(b);
	   b.setCompiled(false);
           repaint();

           //Save file in Ruta
           FileWriter fw=null;

           // Get parameter static -> path to save file
           String Path=FileTree.Ruta;
           if (Path.equals("")) {
               Path=".";
           }

           Path=Path+File.separator+fileName+".elv";
           System.out.println("Ruta: "+Path);
           fw = new FileWriter(Path);
           b.setName(fileName);
           b.saveBnet(fw);
           fw.close();

           }
           catch (IOException ioe){

            System.out.println(ioe.getMessage());
        }


	}



	/**
	 * Active the InferencePanel into the networkPane
	 */

	public void activeInferencePanel () {
		//ID with forced policies
		IDWithSVNodes idWithFP;
		
		
		
	    removePanel();
	    networkPane.getViewport().add(networkInferencePanel);
	    Bnet b=networkEditorPanel.getBayesNet();
	    CooperPolicyNetwork cooperPN=null;
	    Class<? extends Bnet> bClass = b.getClass();
	    
	    if (!b.isEmpty()) {
	      if (networkInferencePanel.AUTOPROPAGATION){
	         
	         if (bClass == IDiagram.class){
	        	 
	         	  
	            ((IDiagram) b).compile(networkInferencePanel.getInferenceMethod(),networkInferencePanel.getParameters());
		    b.setCompiled(true);
	         } else if (bClass == IDWithSVNodes.class) {
                	 idWithFP = ((IDWithSVNodes)b).constructAnIDUsingForcedPolicies();
                	 
                 	//cooperPN = CooperPolicyNetwork.constructCPNFrom(idWithFP,networkInferencePanel.getInferenceMethod(),networkInferencePanel.getParameters());
                	 //Compile with Tatman and Shachter's method
              	    cooperPN = CooperPolicyNetwork.constructCPNFrom(idWithFP,3,networkInferencePanel.getParameters());
              	    //cooperPN = CooperPolicyNetwork.constructCPNFrom(idWithFP,0,networkInferencePanel.getParameters());
                 	//The cooper policy network must also be compiled.
                 	cooperPN.compile(0,
                 			networkInferencePanel.getParameters(),
                 			networkInferencePanel.getAuxiliaryFilesNames(),
                            networkInferencePanel.getAbductiveValues());
                 	
                 	((IDiagram)b).setCpn(cooperPN);
                 	((IDiagram)b).showResults(idWithFP.getPropagation());
                           
                     b.setCompiled(true);
                     //return;
             }
	         else if (bClass == UID.class){
	        	 ((UID)b).compileByDefault();
	        	 b.setCompiled(true);
	        	  GSDAG gsdag = ((DynamicUID)((UID)b).getPropagation()).getGsdag();
		    	  gsdag.setCompiled(true);
		    	  gsdag.prepareGsdagToPaintIt();
	         }
	         else {
	              b.compile(networkInferencePanel.getInferenceMethod(),
	                  networkInferencePanel.getParameters(),
                      networkInferencePanel.getAuxiliaryFilesNames(),
                      networkInferencePanel.getAbductiveValues());
                  b.setCompiled(true);
		}
	      }
	      
	      if (bClass!=UID.class){
	    	  networkInferencePanel.setBayesNet(b);
	      }
	      else{
	    	  networkInferencePanel.setBayesNet(((DynamicUID)((UID)b).getPropagation()).getGsdag());
	      }
	    	  
	      networkInferencePanel.refreshElviraPanel(networkInferencePanel.getZoom());
	      networkInferencePanel.setBounds(getBounds());
	      if (bClass!=UID.class){
	    	  networkInferencePanel.expandNodes();
	      }
	      

	      //if (b.getClass() != IDiagram.class){
	      if (true) { /* MODIFIED */
		if (b.getIsCompiled()){
			//if (bClass!=UID.class){
				CasesList nueva = new CasesList(b);;
    	          
    	          networkInferencePanel.setCasesList(nueva);
    	          System.out.println("número de casos de la lista "+nueva.getNumStoredCases());
   	            Elvira.getElviraFrame().setNodeName(networkInferencePanel.getCasesList().getCurrentCase().getIdentifier());
   	            Elvira.getElviraFrame().setColorNodeName(networkInferencePanel.getCasesList().getCurrentCase().getColor());
    	System.out.println("Casos almacenados "+nueva.getNumStoredCases());
			//}
    			}
		else {
   	            networkInferencePanel.setCasesList(new CasesList());
   	            Elvira.getElviraFrame().setNodeName("Bnet not compiled");
   	            Elvira.getElviraFrame().setColorNodeName(Color.white);

		}
   	      }
   	      mode=INFERENCE_ACTIVE;
	      networkInferencePanel.unSelectAll();
	      repaint();
	   }
	}


	public void removePanel() {
	   switch (mode) {
	      case EDITOR_ACTIVE: networkPane.getViewport().remove(networkEditorPanel);
	      case INFERENCE_ACTIVE: networkPane.getViewport().remove(networkInferencePanel);
	      case LEARNING_ACTIVE: networkPane.getViewport().remove(networkLearningPanel);
	   }
	}


	/**
	 * Set the zoom variable in the panels
	 */

	public void setZoom (double zoom) {
	   networkInferencePanel.setZoom(zoom);
	   networkEditorPanel.setZoom(zoom);
	}


	/**
	 * Repaint the actve panel
	 */

	public void repaintPanel(double zoom) {
	   if (mode==EDITOR_ACTIVE) {
         networkEditorPanel.repaint();
         networkEditorPanel.refreshElviraPanel(zoom);
      }
      else {
         networkInferencePanel.repaint();
         networkInferencePanel.refreshElviraPanel(zoom);
      }

	}

	public void repaintPanel() {
	   if (mode==EDITOR_ACTIVE) {
         networkEditorPanel.repaint();
         networkEditorPanel.refreshElviraPanel(
               networkEditorPanel.getZoom());
      }
      else {
         networkInferencePanel.repaint();
         networkInferencePanel.refreshElviraPanel(
               networkInferencePanel.getZoom());
      }
	}


	/**
	 * Set the bounds of the scrollPane of the NetworkFrame
	 * using the points given as parameter
	 */

	public void refreshScrollPane(Point max, Point min) {

	   JScrollBar horizontalBar = networkPane.getHorizontalScrollBar(),
	              verticalBar = networkPane.getVerticalScrollBar();
	   Rectangle panelBounds;
	   Rectangle scrollBounds = networkPane.getBounds();

	   if (mode == EDITOR_ACTIVE)
	      panelBounds = networkEditorPanel.getBounds();
	   else
	      panelBounds = networkInferencePanel.getBounds();

	   if (min==null)
	      min=max;

	   // Set the x values

	   if (min.x < Math.abs(panelBounds.x) + networkEditorPanel.NODE_RADIUS) {
	      if (horizontalBar.getValue()<30)
	         horizontalBar.setValue(0);
	      else
	         horizontalBar.setValue(horizontalBar.getValue()-30);
	   }
	   else
	      if (max.x >
	         Math.abs(panelBounds.x)+scrollBounds.width) {

	         if (horizontalBar.getValue()+30>horizontalBar.getMaximum())
	            horizontalBar.setValue(horizontalBar.getMaximum());
	         else
	            horizontalBar.setValue(horizontalBar.getValue()+30);
	      }

	   // Set the y values

	   if (min.y < Math.abs(panelBounds.y)-networkEditorPanel.NODE_RADIUS) {
	      if (verticalBar.getValue()<30)
	         verticalBar.setValue(0);
	      else
	         verticalBar.setValue(verticalBar.getValue()-30);
	   }
      else
	      if (max.y >
	         Math.abs(panelBounds.y)+scrollBounds.height) {

	         if (verticalBar.getValue()+30>verticalBar.getMaximum())
	            verticalBar.setValue(verticalBar.getMaximum());
	         else
	            verticalBar.setValue(verticalBar.getValue()+30);
	      }
	   /*if (horizontal) {
	      maximum = networkPane.getHorizontalScrollBar().getMaximum();
	      networkPane.getHorizontalScrollBar().setValue(maximum);
	   }
	   if (vertical) {
	      maximum = networkPane.getVerticalScrollBar().getMaximum();
	      networkPane.getVerticalScrollBar().setValue(maximum);
	   }*/

	}


	/**
	 * Manage the events produced into the NetworkFrame
	 * We will use this for do some actions when the
	 * NetworkFrame is activated.
	 */

	class SymInternalFrame implements javax.swing.event.InternalFrameListener
	{
		public void internalFrameClosed(javax.swing.event.InternalFrameEvent event)
		{
		}

		public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent event)
		{
		}

		public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent event)
		{
		}

		public void internalFrameActivated(javax.swing.event.InternalFrameEvent event)
		{
			Object object = event.getSource();
			if (object == NetworkFrame.this)
				NetworkFrame_internalFrameActivated(event);
		}

		public void internalFrameIconified(javax.swing.event.InternalFrameEvent event)
		{
		}

		public void internalFrameClosing(javax.swing.event.InternalFrameEvent event)
		{
		}


		public void internalFrameOpened(javax.swing.event.InternalFrameEvent event)
		{
		}
	}

	void NetworkFrame_internalFrameActivated(javax.swing.event.InternalFrameEvent event)
	{
		NetworkFrame_internalFrameActivated_Interaction1(event);
	}


	/**
	 * Active the menuItem in the WindowMenu that correspond to
	 * this NetworkFrame when this is activated. This method set
	 * the Undo/Redo menu items according to the new frame active
	 * and modified the currentNetworkFrame variable of the ElviraFrame
	 * setting it to this NetworkFrame.
	 */

	void NetworkFrame_internalFrameActivated_Interaction1(javax.swing.event.InternalFrameEvent event)
	{
		   Enumeration windowMenu = Elvira.getElviraFrame().getWindowGroup().getElements();
		   boolean exit = false;
		   while (windowMenu.hasMoreElements() && !exit) {
		      JMenuItem windowItem = (JMenuItem) windowMenu.nextElement();
		      if (windowItem.getText().equals(this.getTitle())) {
		         if (!windowItem.isSelected())
		            windowItem.setSelected(true);
		         exit = true;
		      }
		   }

		   networkEditorPanel.updateUndoRedo();
                   Elvira.getElviraFrame().setZoom(networkEditorPanel.getZoom());
		   Elvira.getElviraFrame().setCurrentNetworkFrame(this);
		   Elvira.getElviraFrame().enableMenusOpenNetworks(true,
                    networkEditorPanel.getBayesNet());
                   Elvira.getElviraFrame().setThresholdComboValue (
	            networkInferencePanel.getExpansionThreshold());

	}

	/* (non-Javadoc)
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 * 
	 * Para evitar la excepcion de VisualNode (getHeight y getWidth)
	 * Jorge-PFC 05/01/2006
	 */
	public void paint(Graphics g) {
		Elvira.getElviraFrame().setNetworkToPaint(this);
		super.paint(g);
	}	
}

 


/**
 * An object of this class is added to a NetworkFrame when is created to
 * manage the closing action of this InternalFrames.
 */

class CloseListener implements VetoableChangeListener {

   /**
    * Obtains the name of the property from the property change event
    * and checks to see if the property being modified is the closed
    * property. If so, a reference obtained to the internal frame from
    * whence the event was fired
    */

	public void vetoableChange(PropertyChangeEvent e)
									throws PropertyVetoException {
		String name = e.getPropertyName();

		if(name.equals(JInternalFrame.IS_CLOSED_PROPERTY)) {
			Component internalFrame = (Component)e.getSource();
			Boolean oldValue = (Boolean)e.getOldValue(),
			  		newValue = (Boolean)e.getNewValue();

			if (oldValue == Boolean.FALSE &&
				 newValue == Boolean.TRUE) {
				   ElviraFrame f = Elvira.getElviraFrame();
				   f.getCurrentNetworkFrame();
				   f.closeAction(e);

				   switch (f.getDesktopPane().getAllFrames().length) {
				      case 0: f.enableMenusOpenFrames(false);
				              break;
				      case 1: f.enableMenusOpenNetworks(false, null);
				              break;
				   }

			}
		}
	}
        
        
}





