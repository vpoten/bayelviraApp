package elvira.gui;

import java.util.Vector;

import javax.swing.JComboBox;

import elvira.Bnet;
import elvira.IDWithSVNodes;
import elvira.IDiagram;
import elvira.Node;
import elvira.inference.super_value.ArcReversalSV;
import elvira.inference.super_value.VariableEliminationSV;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.NodeGSDAG;
import elvira.inference.uids.NodeGSDAG.PotentialsForDecisionTable;
import elvira.inference.uids.NodeGSDAG.TypeOfNodeGSDAG;
import elvira.potential.Potential;

public class ShowDecisionTableUID extends ShowDecisionTable {
	
	/**
	 * It indicates the decision whose policy is shown
	 */
	private JComboBox decisionComboBox;

	public ShowDecisionTableUID(NodeGSDAG nodeGSDAG, GSDAG gsdag) {
		// TODO Auto-generated constructor stub Potential pot=null;
	    Potential utilities;
	    Potential policy;		
	    String nameVar = null;
	 	//See compile(...) in class IDWithSVNodes, about indexMethod
	/*	Vector potVector;
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
	    
	    
	   
	    theNode = node;
	    potentVector = potVector;
	    potentVectorForPolicies = potVectorForPolicies;
	   */
	 
	    initializeGUI(nodeGSDAG);
	    
	    TypeOfNodeGSDAG typeOfNodeGSDAG = nodeGSDAG.getTypeOfNodeGSDAG();
		if (typeOfNodeGSDAG==NodeGSDAG.TypeOfNodeGSDAG.DECISION){
	    	decisionComboBox = new JComboBox();
	    	for (String auxDec:nodeGSDAG.getVariables()){
	    		decisionComboBox.addItem(auxDec);
	    	}
	    	decisionComboBox.setVisible(true);
	    	decisionComboBox.setBounds(250,6,95,23);
	    	decisionComboBox.setSelectedIndex(0);
	    	decvsutlPanel.add(decisionComboBox);
	    	//decisionPanel.add(decisionComboBox);
	    }


	 
	    switch (typeOfNodeGSDAG){
	    case DECISION:
	    	nameVar = (String) decisionComboBox.getSelectedItem();
	    	break;
	    case BRANCH:
	    	nameVar = "Branch";
	    	break;
	    }
	    
	    PotentialsForDecisionTable potentialsOfVariable = nodeGSDAG.getPotentialsForDecisionTable().get(nameVar);
	    
	    policy = potentialsOfVariable.getPolicyDecisionTable();
		utilities = potentialsOfVariable.getUtilitiesDecisionTable();
		
		thePot = utilities;
	    thePotPolicy= policy;
		
		utilityRadioButton.setEnabled(true);
		utilityRadioButton.setSelected(true);
		decisionRadioButton.setEnabled(true);
		decisionRadioButton.setSelected(false);
		
		//Variable used for ordering the variables in the tables
		order=nodeGSDAG.getAnAdmissibleOrderOfThePast();
		
		if (typeOfNodeGSDAG==TypeOfNodeGSDAG.BRANCH){
			order.add("Branch");
		}
		
		theNode = (Node) thePot.getVariables().lastElement();
		//potentVector = thePot;
		//potentVectorForPolicies = thePotPolicy;
		generateDecisionTableUtilities(theNode,thePot);
	    //generateDecisionTableDecisions(theNode,thePotPolicy);
	    
	    
	}

}
