package elvira.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import java.awt.*;
import java.util.*;
import java.text.*;
import java.io.*;

import elvira.*;
import elvira.potential.*;
import elvira.inference.super_value.ArcReversalSV;
import elvira.inference.super_value.VariableEliminationSV;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ShowDecisionTable extends JDialog {

  protected Vector potentVectorForPolicies;
	javax.swing.JTabbedPane decisionTabbedPane = new javax.swing.JTabbedPane();
  javax.swing.JRadioButton decisionRadioButton = new javax.swing.JRadioButton();
  javax.swing.JRadioButton utilityRadioButton = new javax.swing.JRadioButton();
  javax.swing.JPanel decvsutlPanel = new javax.swing.JPanel();
  javax.swing.JPanel decisionPanel = new javax.swing.JPanel();
  javax.swing.JPanel buttonsPanel = new javax.swing.JPanel();
  javax.swing.JButton okButton = new javax.swing.JButton();
  javax.swing.JPanel tablePanel = new javax.swing.JPanel();
  javax.swing.JScrollPane relationScrollPane = new javax.swing.JScrollPane();
  javax.swing.JTable relationTable = new javax.swing.JTable();
  NodeTableModel relationModel = new NodeTableModel();
  JTable headerTable = new JTable(relationModel);
  GridBagConstraints c = new GridBagConstraints();
  GridBagLayout gridbag = new GridBagLayout();
  ButtonGroup bGroup = new ButtonGroup();
  TableColumnModel tcm;
  TableColumn firstColumn;
  Vector potentVector;
  boolean utilityWasSelected = true;
  Node theNode;
  Potential thePot;
  Potential thePotPolicy;

  // Bundles for internacionalization.
  ResourceBundle menuBundle, dialogBundle;

  String editingValue;

  int inferenceMethod;	
  /* CONSTRUCTORS */

  //Order of the variables in the diagram
  ArrayList<String> order;
  

 public ShowDecisionTable(){
	 
 }
  
  
 public ShowDecisionTable(Node node, Bnet bnet, int indexMethod) {
 	//See compile(...) in class IDWithSVNodes, about indexMethod
	Vector potVector;
	Vector potVectorForPolicies;
	VariableEliminationSV vesv;
	
	

    inferenceMethod=indexMethod;
    if ((indexMethod>=5)&&(indexMethod<11)){
    	//Variable elimination or arc reversal
    	potVector=bnet.getCompiledPotentialList();
    	potVectorForPolicies=potVector;
    }
    else{
		//Variable elimination for ID with SV nodes or
		//Tatman and Shachter's algorithm
    	potVector=bnet.getCompiledPotentialList();
    	if ((indexMethod >= 0)&&(indexMethod <= 2)){
    		potVectorForPolicies=((VariableEliminationSV)((IDiagram)bnet).getPropagation()).getResultsForPolicies();
    	}
    	else{//Tatman and Shachter
			potVectorForPolicies=((ArcReversalSV)((IDiagram)bnet).getPropagation()).getResultsForPolicies();
    	}
    }


    if ((node==null) || (potVector==null)) {
      System.out.println("null parameters in ShowDecisionTable!!!!!!!");
      System.exit(1);
    }
    
    
    vesv = new VariableEliminationSV((IDWithSVNodes)bnet);
    order = vesv.getTotalOrder();
    
    
    //for (int i=0;i<potVector.size();i++){
    	//potVector.set(i,((Potential)(potVector.elementAt(i))).placeVariablesAccordingToOrder(order));
    	//potVectorForPolicies.set(i,((Potential)(potVectorForPolicies.elementAt(i))).placeVariablesAccordingToOrder(order));
    //}
    
    theNode = node;
    potentVector = potVector;
    potentVectorForPolicies = potVectorForPolicies;
   
 
    initializeGUI(node);


    Potential pot=null;
    Potential potPolicy=null;

    
    potPolicy = obtainThePolicyFrom(potVectorForPolicies,node);

	
	
	//Set pot, that contains the potential of utilities when
	//the decision was eliminated
	//Do if the the algorithm isn't VE for ID with SV nodes without divisions
	if (hasInferenceMethodTableUtilities()) {
		pot = obtainTheUtilitiesOfThePolicyFrom(potVector, node);
	} else { //In VE without divisions we haven't any potentials of utilities
		pot = null;
	}
		
	
  
    thePot = pot;
    thePotPolicy= potPolicy;
    
    if (hasInferenceMethodTableUtilities()){
		utilityRadioButton.setEnabled(true);
		utilityRadioButton.setSelected(true);
		generateDecisionTableUtilities(theNode,thePot);
    }
    else{
		utilityRadioButton.setEnabled(false);
		decisionRadioButton.setSelected(true);
		generateDecisionTableDecisions(theNode,thePotPolicy);
    
    }
  	
    	
	   // generateDecisionTable(theNode, thePot, thePotPolicy);
    
  }
  
  private void generateDecisionTableUtilitiesFollowingTotalOrder(Node theNode2,
		Potential thePot2) {
	// TODO Auto-generated method stub
	
}


private Potential obtainTheUtilitiesOfThePolicyFrom(Vector potVector,Node node) {
	// TODO Auto-generated method stub
	  Potential pot=null;
	  for (int i = 0; i < potVector.size(); i++) {
			if (potVector.elementAt(i) != null) {
				if (((Node) ((Potential) potVector.elementAt(i))
					.getVariables()
					.elementAt(
						((Potential) potVector.elementAt(i))
							.getVariables()
							.size()
							- 1))
					.getName()
					.equals(node.getName())) {
					pot = (Potential) potVector.elementAt(i);
					break;
				}
			}
		}
	  return pot;
}

private Potential obtainThePolicyFrom(Vector potVectorForPolicies, Node node) {
	// TODO Auto-generated method stub
	//Set potPolicy, that determines the optimal decision
	  Potential potPolicy=null;
		for (int i=0; i<potVectorForPolicies.size(); i++) {
			  if (potVectorForPolicies.elementAt(i) != null) {
			if (((Node) ((Potential) potVectorForPolicies.elementAt(i)).getVariables().elementAt(((Potential) potVectorForPolicies.elementAt(i)).getVariables().size()-1)).getName().equals(node.getName())) {
			  potPolicy = (Potential) potVectorForPolicies.elementAt(i);
			  break;
			}
		  }
		}
		
		return potPolicy;
}

protected void initializeGUI(Node node) {
	// TODO Auto-generated method stub
	    setModal(false);
	    getContentPane().setLayout(new BorderLayout(0,0));
	    setSize(578,376);
	    setVisible(false);
	    setResizable(true);
	    setLocationRelativeTo(this.getParent());
	    		setTitle("Decision: "+node.getName());

	    dialogBundle = Elvira.getElviraFrame().getDialogBundle();
	    
	    buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
	    getContentPane().add(BorderLayout.SOUTH,buttonsPanel);
	    buttonsPanel.setBounds(0,341,578,35);
	    okButton.setText(localize(dialogBundle,"OK.label"));
	    okButton.setActionCommand("OK");
	    buttonsPanel.add(okButton);
	    okButton.setBounds(245,5,73,25);
	    
	    SymAction lSymAction = new SymAction();
	    okButton.addActionListener(lSymAction);
	    okButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
	    decisionTabbedPane.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    tablePanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    buttonsPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    relationScrollPane.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    decisionPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    relationTable.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    utilityRadioButton.addActionListener(lSymAction);
	    decisionRadioButton.addActionListener(lSymAction);

	    getContentPane().add(BorderLayout.CENTER,decisionTabbedPane);
	    decisionTabbedPane.setBounds(0,0,578,341);
	    decisionPanel.setLayout(gridbag);
	    decisionTabbedPane.add(decisionPanel);
	    decisionTabbedPane.setTitleAt(0,"Decision");
	    decisionTabbedPane.setSelectedComponent(decisionPanel);
	    decisionPanel.setBounds(2,27,573,311);
	    decisionPanel.setVisible(false);
	    decvsutlPanel.setLayout(null);
	    c = setGridBagConstraints (0,0,2,1,0.7,0.15);
	    gridbag.setConstraints(decvsutlPanel, c);
	    decisionPanel.add(decvsutlPanel);
	    utilityRadioButton.setBounds(14,6,99,23);
	    utilityRadioButton.setText(localize(dialogBundle,"ShowDecision.Utility.label"));
	    utilityRadioButton.setSelected(true);
	    decvsutlPanel.add(utilityRadioButton);
	    decisionRadioButton.setBounds(120,6,95,23);
	    decisionRadioButton.setText(localize(dialogBundle,"ShowDecision.Decision.label"));
	    decvsutlPanel.add(decisionRadioButton);
	    tablePanel.setLayout(new BorderLayout(0,0));
	    c = setGridBagConstraints(0,2,4,3,1.5,1.0);
	    gridbag.setConstraints(tablePanel, c);
	    decisionPanel.add(tablePanel);
	    tablePanel.setBounds(1,90,571,220);
	    relationScrollPane.setOpaque(true);
	    tablePanel.add(BorderLayout.CENTER,relationScrollPane);
	    relationScrollPane.setBounds(0,0,570,219);
	    relationTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
	    relationScrollPane.getViewport().add(relationTable);
	    relationTable.setBounds(0,0,0,0);
	    relationTable.setCellSelectionEnabled(false);
	    relationTable.setRowSelectionAllowed(false);
	    relationTable.setColumnSelectionAllowed(false);
	    
	    bGroup.add(utilityRadioButton);
	    bGroup.add(decisionRadioButton);
	
}

private boolean hasInferenceMethodTableUtilities(){
  	if ((inferenceMethod == 1)||(inferenceMethod == 2)||(inferenceMethod == 4)){
  		return false;
  	}
  	else{
  		return true;
  	}
  }

  /* LISTENERS */

  class SymAction implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent event) {
	Object object = event.getSource();
	if (object == okButton) {
	  okButton_actionPerformed(event);
	}
        else if (object == decisionTabbedPane) {
          okButton_actionPerformed(event);
        }
        else if (object == tablePanel) {
          okButton_actionPerformed(event);
        }
        else if (object == buttonsPanel) {
          okButton_actionPerformed(event);
        }
        else if (object == relationScrollPane) {
          okButton_actionPerformed(event);
        }
        else if (object == decisionPanel) {
          okButton_actionPerformed(event);
        }
        else if (object == relationTable) {
          okButton_actionPerformed(event);   
        }
        else if (object == utilityRadioButton) {
          utilityRadioButton_actionPerformed(event);
        }
        else if (object == decisionRadioButton) {
          decisionRadioButton_actionPerformed(event);
        }
    }
  }

  void okButton_actionPerformed(java.awt.event.ActionEvent event){
    dispose();
  }
  
  void utilityRadioButton_actionPerformed(java.awt.event.ActionEvent event) {
    if (utilityWasSelected) {
        return;
    }
    else {
        /* TO DO */
        utilityWasSelected = true;
        generateDecisionTableUtilities(theNode, thePot);
    }
  }
  
  void decisionRadioButton_actionPerformed(java.awt.event.ActionEvent event) {
    if (!utilityWasSelected) {
        return;
    }
    else {
        /* TO DO */
        utilityWasSelected = false;
        generateDecisionTableDecisions(theNode, thePotPolicy);
    }
  }

  /* METHODS */

  /**
   * Using with the bundles. Find the string given as parameter in
   * the bundle specified
   */

  private String localize (ResourceBundle bundle, String name) {
    return Elvira.getElviraFrame().localize(bundle, name);
  }

  /**
   * Generates the decision table
   */

  protected void generateDecisionTableUtilities(Node node, Potential pot) {

    Vector leftColumn = new Vector();
    Vector states;
    NodeList headerNodes;
    int rowLines, rows;
    
    
    

    if (pot == null) {
      /*System.out.println("Potential in generateDecisionTable is null!!!!!");
      System.exit(1);*/
        /* Modified by mluque: If the table of the decision is null, Elvira informs the user
     that the decision node hasn`t influence on the expected utility of the utility node. */
       ShowMessages.showMessageDialog(ShowMessages.DECISION_TABLE_NULL,
	     JOptionPane.INFORMATION_MESSAGE);
	    return;
              
    }

    pot = pot.placeVariablesAccordingToOrder(order);
    
    clearRelationTable();

    states = new Vector();
    rowLines = ((FiniteStates) node).getStates().size();
    for (int i=0; i<rowLines; i++) {
      states.addElement(withoutQm(((FiniteStates) node).getState(i)));
    }

    Vector nodes = new Vector();

    for (int i=0; i<pot.getVariables().size(); i++) {
      if (!((Node) pot.getVariables().elementAt(i)).getName().equals(node.getName())) {
	nodes.addElement((FiniteStates) pot.getVariables().elementAt(i));
      }
    }

    Vector arrangedNodes = new Vector();
    
    for (int i=0; i<nodes.size(); i++) {
        arrangedNodes.add(nodes.elementAt(i));
    }

    boolean moreThanTheParents = false;
    
    for (int i=0; i<nodes.size(); i++) {
        if ((node.getParents().getID((FiniteStates) nodes.elementAt(i)) == -1) ||
            (node.getParents().getID((FiniteStates) nodes.elementAt(i)) > nodes.size()-1)) {
           moreThanTheParents = true;
           break;
        }
        else {
           arrangedNodes.setElementAt(nodes.elementAt(i),node.getParents().getID((FiniteStates) nodes.elementAt(i)));
        }
    }
    
    if (moreThanTheParents) {
        arrangedNodes = nodes;
    }
    
    headerNodes = new NodeList(arrangedNodes);
    
	  //Change the order of the nodes in header
	  headerNodes = new NodeList(arrangedNodes);
	  NodeList reversed=new NodeList();
	  for (int i=headerNodes.size()-1;i>=0;i--){
		  reversed.insertNode(headerNodes.elementAt(i));
	  }
	  headerNodes = reversed;

    for (int i=0; i<headerNodes.size(); i++) {
       FiniteStates father=(FiniteStates) headerNodes.elementAt(i);
       leftColumn.addElement(father.getNodeString(true));
    }

    int positions[]= new int[headerNodes.size()];

        rows = positions.length+states.size();
        for (int i=0; i<states.size(); i++) {
            leftColumn.addElement(states.elementAt(i));
        }

    // Set the number of rows of the table's header
    relationModel.setNumRows(rows);
    relationModel.addColumn("",leftColumn);

    // Set the columns with its headers
    Configuration config = new Configuration(headerNodes);
    int ncolumns = (int) pot.getSize()/states.size();

    for (int i=0; i<ncolumns; i++) {
      relationModel.addColumn("",setColumn(config.getVariables(),config.getValues()));
      config.nextConfiguration();
    }

    tcm = relationTable.getColumnModel();

    for (int i=0; i<relationTable.getColumnCount(); i++) {
      TableColumn column = tcm.getColumn(i);
      column.setCellRenderer(new RowRenderer(false,headerNodes));
    }

    setRelationTableProperties(headerNodes);
    //fillValues(headerNodes, node, pot, ncolumns);
    fillValuesUtilities(headerNodes,node,pot,ncolumns);
        	
    relationTable.revalidate();

    /*relationTable.getModel().addTableModelListener(
     new RelationTableListener());

    relationTable.getSelectionModel().addListSelectionListener(
            new RelationSelectionListener());*/

    relationTable.setVisible(true);
  }



  protected void generateDecisionTableDecisions(Node node, Potential pot) {

	  Vector leftColumn = new Vector();
	  Vector states;
	  NodeList headerNodes;
	  int rowLines, rows;

	  if (pot == null) {
		/*System.out.println("Potential in generateDecisionTable is null!!!!!");
		System.exit(1);*/
		  /* Modified by mluque: If the table of the decision is null, Elvira informs the user
	   that the decision node hasn`t influence on the expected utility of the utility node. */
		 ShowMessages.showMessageDialog(ShowMessages.DECISION_TABLE_NULL,
		   JOptionPane.INFORMATION_MESSAGE);
		  return;
              
	  }

	  pot = pot.placeVariablesAccordingToOrder(order);
	  
	  clearRelationTable();

	  states = new Vector();
	  rowLines = ((FiniteStates) node).getStates().size();
	  for (int i=0; i<rowLines; i++) {
		states.addElement(withoutQm(((FiniteStates) node).getState(i)));
	  }

	  Vector nodes = new Vector();

	  for (int i=0; i<pot.getVariables().size(); i++) {
		if (!((Node) pot.getVariables().elementAt(i)).getName().equals(node.getName())) {
	  nodes.addElement((FiniteStates) pot.getVariables().elementAt(i));
		}
	  }

	  Vector arrangedNodes = new Vector();
    
	  for (int i=0; i<nodes.size(); i++) {
		  arrangedNodes.add(nodes.elementAt(i));
	  }

	  boolean moreThanTheParents = false;
    
	  for (int i=0; i<nodes.size(); i++) {
		  if ((node.getParents().getID((FiniteStates) nodes.elementAt(i)) == -1) ||
			  (node.getParents().getID((FiniteStates) nodes.elementAt(i)) > nodes.size()-1)) {
			 moreThanTheParents = true;
			 break;
    }
    else {
			 arrangedNodes.setElementAt(nodes.elementAt(i),node.getParents().getID((FiniteStates) nodes.elementAt(i)));
		  }
	  }
    
	  if (moreThanTheParents) {
		  arrangedNodes = nodes;
	  }
    
	  //Change the order of the nodes in header
	  headerNodes = new NodeList(arrangedNodes);
	  NodeList reversed=new NodeList();
	  for (int i=headerNodes.size()-1;i>=0;i--){
		  reversed.insertNode(headerNodes.elementAt(i));
	  }
	  headerNodes = reversed;
	  
	  

	  for (int i=0; i<headerNodes.size(); i++) {
		 FiniteStates father=(FiniteStates) headerNodes.elementAt(i);
		 leftColumn.addElement(father.getNodeString(true));
    }

	  int positions[]= new int[headerNodes.size()];

	  rows = positions.length+1;
	  leftColumn.addElement(node.getName());


    // Set the number of rows of the table's header
    relationModel.setNumRows(rows);
    relationModel.addColumn("",leftColumn);

    // Set the columns with its headers
    Configuration config = new Configuration(headerNodes);
    int ncolumns = (int) pot.getSize()/states.size();

    for (int i=0; i<ncolumns; i++) {
      relationModel.addColumn("",setColumn(config.getVariables(),config.getValues()));
      config.nextConfiguration();
    }

    tcm = relationTable.getColumnModel();

    for (int i=0; i<relationTable.getColumnCount(); i++) {
      TableColumn column = tcm.getColumn(i);
      column.setCellRenderer(new RowRenderer(false,headerNodes));
    }

    setRelationTableProperties(headerNodes);
	  //fillValues(headerNodes, node, pot, ncolumns);
	  fillValuesDecisions(headerNodes,node,pot,ncolumns);
	 
    	
    relationTable.revalidate();

    /*relationTable.getModel().addTableModelListener(
     new RelationTableListener());

    relationTable.getSelectionModel().addListSelectionListener(
            new RelationSelectionListener());*/

    relationTable.setVisible(true);
  }


    private String withoutQm(String s)
    {
            if (s.substring(0,1).equals("\""))
            {
                return (s.substring(1,s.length()-1));
            }
            else {
                return s;
            }
    }
  
  private void fillValuesUtilities(NodeList headerNodes, Node node, Potential pot, int columns) {

    int tableColumns, tableRows, headerRows, valuesColumns;
    int numStates;

    int maxFinal = 0;
    double maxVal = 0.0;
  /*  Configuration confMax = new Configuration(((Potential) potentVector.elementAt(potentVector.size()-1)).getVariables());

    for (int i=0; i<((Potential) potentVector.elementAt(potentVector.size()-1)).getSize(); i++) {
      if (((Potential) potentVector.elementAt(potentVector.size()-1)).getValue(confMax) > maxVal) {
         maxVal = ((Potential) potentVector.elementAt(potentVector.size()-1)).getValue(confMax);
         maxFinal = i;
      }

      confMax.nextConfiguration();
    }*/

    //System.out.println("maxFinal: "+maxFinal);

    numStates = ((FiniteStates) node).getNumStates();
    tableColumns = columns;
    valuesColumns = columns;
    tableRows = numStates;
    headerRows = headerNodes.size();

    Configuration configHD = new Configuration(headerNodes);
    Configuration configT;
    double theMaxRow[] = new double[tableColumns];
    int theMaxIndx[] = new int[tableColumns];
    for (int j=0; j<tableColumns; j++) {
      theMaxRow[j]=Float.NEGATIVE_INFINITY;
      theMaxIndx[j]=-1;
    }

  
        for (int i=0; i<tableRows; i++) {
            configT = new Configuration(pot.getVariables(),configHD,false);    
            configT.putValue(((FiniteStates) node).getName(),i);
            for (int j=0; j<tableColumns; j++) {
                if (theMaxRow[j] < pot.getValue(configT)) {
                    theMaxRow[j]=pot.getValue(configT);
                    //if (configT.getValue(((FiniteStates) ((Potential) potentVector.elementAt(potentVector.size()-1)).getVariables().elementAt(0)).getName()) == maxFinal) {
                        /* This is surely wrong: to CHANGE */
                        theMaxIndx[j]=i+headerNodes.size();
                    //}
                }
                DecimalFormatSymbols dfs = new DecimalFormatSymbols();
                dfs.setDecimalSeparator('.');
                NumberFormat nf = new DecimalFormat(Elvira.getElviraFrame().getCurrentEditorPanel().getBayesNet().getVisualPrecision(),dfs);
                double m = Double.parseDouble(String.valueOf(nf.format(new Double(pot.getValue(configT))).toString()));
                relationTable.setValueAt(new Double(m).toString(),i+headerNodes.size(),j);
                configHD.nextConfiguration();
                for (int k=0; k<configHD.getVariables().size(); k++) {
                    configT.putValue(((FiniteStates) configHD.getVariables().elementAt(k)).getName(),configHD.getValue(((FiniteStates) configHD.getVariables().elementAt(k)).getName()));
                }
            }

            tcm = relationTable.getColumnModel();

            for (int j=0; j<relationTable.getColumnCount(); j++) {
                TableColumn column = tcm.getColumn(j);
                column.setCellRenderer(new RowRenderer(false,headerNodes,theMaxIndx[j]));
            }

            configHD = new Configuration(headerNodes);
        }
      
    }


  private void fillValuesDecisions(NodeList headerNodes, Node node, Potential pot, int columns) {

	  int tableColumns, tableRows, headerRows, valuesColumns;
	  int numStates;

	  int maxFinal = 0;
	  double maxVal = 0.0;
	  
	  //ESTO LO HE PUESTO ENTRE COMENTARIOS (VER SI HARÍA FALTA DEJARLO SIN COMENTAR)
	 /* Configuration confMax = new Configuration(((Potential) potentVectorForPolicies.elementAt(potentVectorForPolicies.size()-1)).getVariables());

	  for (int i=0; i<((Potential) potentVectorForPolicies.elementAt(potentVectorForPolicies.size()-1)).getSize(); i++) {
		if (((Potential) potentVectorForPolicies.elementAt(potentVectorForPolicies.size()-1)).getValue(confMax) > maxVal) {
		   maxVal = ((Potential) potentVectorForPolicies.elementAt(potentVectorForPolicies.size()-1)).getValue(confMax);
		   maxFinal = i;
		}

		confMax.nextConfiguration();
	  }*/

	  

	  numStates = ((FiniteStates) node).getNumStates();
	  tableColumns = columns;
	  valuesColumns = columns;
	  tableRows = numStates;
	  headerRows = headerNodes.size();

	  Configuration configHD = new Configuration(headerNodes);
	  Configuration configT;
	  double theMaxRow[] = new double[tableColumns];
	  int theMaxIndx[] = new int[tableColumns];
	  for (int j=0; j<tableColumns; j++) {
		theMaxRow[j]=Float.NEGATIVE_INFINITY;
		theMaxIndx[j]=-1;
	  }

	
        for (int i=0; i<tableRows; i++) {
            configT = new Configuration(pot.getVariables(),configHD,false);
            configT.putValue(((FiniteStates) node).getName(),i);
            for (int j=0; j<tableColumns; j++) {
                if (theMaxRow[j] < pot.getValue(configT)) {
                    theMaxRow[j]=pot.getValue(configT);
                        theMaxIndx[j]=i;
	
                }
                configHD.nextConfiguration();
                for (int k=0; k<configHD.getVariables().size(); k++) {
                    configT.putValue(((FiniteStates) configHD.getVariables().elementAt(k)).getName(),configHD.getValue(((FiniteStates) configHD.getVariables().elementAt(k)).getName()));
                }
            }

            configHD = new Configuration(headerNodes);
        }
        
        for (int j=0; j<tableColumns; j++) { 
           String theVal = "-";
           if (theMaxIndx[j] >= 0) {
              theVal = withoutQm(((FiniteStates) node).getState(theMaxIndx[j]));
           }   
           relationTable.setValueAt(theVal,headerNodes.size(),j);
        }    
	
    }


  private void clearRelationTable() {
     relationModel = new NodeTableModel();

     relationTable = new JTable(relationModel);
	  relationTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
     relationScrollPane.getViewport().add(relationTable);
	  relationTable.setBounds(0,0,347,253);

     headerTable = new JTable(relationModel);
     headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
     headerTable.setAutoscrolls(false);
  }

  public void setRelationTableProperties(NodeList headerNodes) {

     firstColumn = tcm.getColumn(0);

     headerTable.getTableHeader().setReorderingAllowed(false);
     headerTable.setPreferredScrollableViewportSize(
	   new Dimension (
	      firstColumn.getWidth()+
	      headerTable.getColumnModel().getColumnMargin(),
	      0));

     tcm.removeColumn(firstColumn);
     relationScrollPane.setRowHeaderView(headerTable);
     relationScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
				   headerTable.getTableHeader());
     TableColumn tc = headerTable.getColumnModel().getColumn(0);
     tc.setCellRenderer(new RowRenderer(true, headerNodes));

     relationTable.setCellSelectionEnabled(false);
     relationTable.setRowSelectionAllowed(false);
     relationTable.setColumnSelectionAllowed(false);
  }

  private Vector setColumn (Vector headerNodes, Vector numstates) {

     Vector column = new Vector();

     for (int i=0; i<headerNodes.size(); i++) {
	column.addElement(withoutQm(((FiniteStates)headerNodes.elementAt(i)).getState(((Integer)numstates.elementAt(i)).intValue())));
     }

     return column;
  }

  private GridBagConstraints setGridBagConstraints (int x, int y, int width,
			       int height, double wx, double wy) {
	GridBagConstraints c = new GridBagConstraints();
	c.gridx=x;
	c.gridy=y;
	c.gridwidth=width;
	c.gridheight=height;
	c.weightx=wx;
	c.weighty=wy;
	c.anchor=GridBagConstraints.CENTER;
	c.fill=GridBagConstraints.BOTH;
	return c;
  }

  /* OTHER CLASSES */

  public class NodeTableModel extends DefaultTableModel {

    /**
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */

    public Class getColumnClass(int c) {
      if(getValueAt(0,c)!=null)
	return getValueAt(0, c).getClass();
      else
	return super.getColumnClass(c);
    }

    public boolean isCellEditable(int row, int col) {
      return false;
    }
  }

  /**
   * This class is used for painting and colouring the table and
   * the headers
   */

  class RowRenderer extends DefaultTableCellRenderer {
     // True if it is rendering the first column
     private boolean first;
     private NodeList headerNodes;
     private int theRow;

     RowRenderer (boolean b, NodeList hNodes) {
        first = b;
        headerNodes = hNodes;
        theRow = -1;
     }

     RowRenderer (boolean b, NodeList hNodes, int row) {
	first = b;
        theRow = row;
	headerNodes = hNodes;
     }

     public Component getTableCellRendererComponent (JTable table,
			  Object value, boolean isSelected,
			  boolean hasFocus,
			  int row, int column) {

      if (theRow == row) {
        setBackground(Color.white);
        setForeground(Color.red);
      }
      else {
	if (row < headerNodes.size()){
	      setBackground(Color.lightGray);
	      if (row%2==0)
		 setForeground(Color.red);
	      else
		 setForeground(Color.blue);
	}
	else {
	      if (first) {
		 setBackground(Color.lightGray);
		 setForeground(Color.black);
	      }
	      else {
		 setBackground(Color.white);
		 setForeground(Color.black);
	      }
        }
      }

      return super.getTableCellRendererComponent(table,
		       value, isSelected, hasFocus,
		       row, column);
     }
  }

  /**
   * Set the selection method that is allowed in the table
   */

  public class RelationSelectionListener implements ListSelectionListener {

    public void valueChanged (ListSelectionEvent e) {
      int column = relationTable.getSelectedColumn(),
	row = relationTable.getSelectedRow();

      editingValue = (String) relationTable.getValueAt(row, column);
    }
  }

  public class RelationTableListener implements TableModelListener {

    public void tableChanged (TableModelEvent e) {
      //int row = e.getFirstRow();
      int row = e.getLastRow(); /* TO TEST */
      int column = e.getColumn();

      String data = (String) relationModel.getValueAt(row, column);

      if (data==null) {
	TableCellEditor rowcolCellEditor =
	    (TableCellEditor) relationTable.getCellEditor(row,column-1); /* Why column-1? */
	rowcolCellEditor.stopCellEditing(); /* This was the action lacking */
	data = (String) rowcolCellEditor.getCellEditorValue();
	//relationModel.setValueAt(data, row, column);
	//System.out.println("Hemos pasado por aquí");
      }

      try {
	 Double value = new Double(data);
	}
	catch (NumberFormatException ex) {
	  ShowMessages.showMessageDialog(ShowMessages.WRONG_CELL_VALUE,
		  JOptionPane.ERROR_MESSAGE);
	  relationModel.setValueAt(editingValue, row, column); /*¿?*/
	}
      }
  }

}
