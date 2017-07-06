/**
 * Class <code>VariableEliminationSV</code>. Implements the solution of
 * an IDWithSVNodes (Influence Diagram With Super-Value Nodes).
 * @author Manuel Luque
 * @since 8/3/2004
 */

package elvira.inference.super_value;

import elvira.inference.elimination.VariableElimination;
import elvira.*;
import elvira.tools.Crono;

import java.util.ArrayList;
import java.util.Vector;
import java.io.*;
import elvira.parser.*;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;

/**
 * @author Manuel Luque
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class VariableEliminationSV extends VariableElimination {

	/**
	 * Max number real numbers to represent each Potential
	 */
	int limitSize;

	/**
	 * The relations available in a given moment.
	 */

	RelationList currentRelations;

	/**
	 * Maximum and minimum values reached during IDs evaluation
	 */

	private double maximum;
	private double minimum;

	/**
	 * Crono, to measure the computation times
	 */

	Crono crono;
	
	/**
	 * Tables of decisions that let to obtain the politics for each decision
	 * The attribute 'results' will have the table of utilities for
	 * the parents of the decision
	 */
	Vector<Potential> resultsForPolicies;
	
	
	

	/**
	 * Constructs a new propagation for a given IDWithSVNodes
	 *
	  * @param diag a <code>IDWithSVNodes</code>.
	 * @param e the evidence.
	 */

	public VariableEliminationSV(IDWithSVNodes diag) {
	  
	  super(diag);
	  
	  crono=new Crono();
	  
	  resultsForPolicies=new Vector();

	  
	  // Modifies the IDiagram, to add non forgetting arcs,
	  // to eliminate redundancy and to transform the set
	  // of initial relations
  
		diag.addNonForgettingArcs();
		diag.removeBarrenNodes();
	//	diag.eliminateRedundancy();
		currentRelations=getInitialRelations();
		maximum=0;
		minimum=0;
	  
	}
	
/**
	 * Constructs a new propagation for a given Bayesian network and
	 * some evidence.
	 *
	 * @param diag a <code>IDWithSVNodes</code>.
	 * @param e the evidence.
	 */

	public VariableEliminationSV(IDWithSVNodes diag, Evidence e) {
	  
	  super(diag,e);

	  observations = e;
	  network = diag;
	  crono=new Crono();
	  
	  resultsForPolicies=new Vector();
  
	  // Modifies the IDiagram, to add non forgetting arcs,
	  // to eliminate redundancy and to transform the set
	  // of initial relations
  
		diag.addNonForgettingArcs();
		diag.removeBarrenNodes();       
	//	diag.eliminateRedundancy();
		currentRelations=getInitialRelations();
		maximum=0;
		minimum=0;
	}
 	
 	
 	public Vector<Potential> getResultsForPolicies(){
 		return resultsForPolicies;
 	}
 
 	
 	/**
	 * Evaluate the ID with SV nodes by applying VariableEliminationSV	 *
	 * @param algorithm
	 * @param parameteres:
	 * 0 -> Boolean value for applying subset rule.
	 */

 	public void propagate(int algorithm, Vector parameters){
 		boolean computeUtilities;
 		boolean applySubsetRule = false;
 		ArrayList<String> orderOfElim=null;
 		boolean groupNoDependents=false;
 		int numParams;
 		/**
 		 * It indicates the preprocessing of the tree of potentials to eliminate a decision D:
 		 * true --> Nodes sum and product are converted into unforked.
 		 * false --> All the branches depending on D are joined. 
 		 */
 		//boolean eliminateDecByUnforking=true;
 				
 		
 		//Parameter of the algorithm that specifies whether the posibility of applying
 		//the subset rule exists. Default value = false.
 		if (parameters==null){
 			applySubsetRule = false;
 			orderOfElim = null;
 			groupNoDependents = false;
 		}
 		else{//All the parameters must be provided
 			numParams = parameters.size();
 			if (numParams==3){
 				applySubsetRule = ((Boolean)(parameters.elementAt(0))).booleanValue();
 				orderOfElim = (ArrayList<String>)(parameters.elementAt(1));
 				groupNoDependents = (Boolean) (parameters.elementAt(2));
 			}
 			else if (numParams==0){
 				applySubsetRule = false;
 				orderOfElim = null;
 				groupNoDependents = false;
 			}
 			else{
 				System.out.println("Error when invoking method 'propagate' of class VariableEliminationSV");
 				System.exit(-1);
 			}
 		}
 		
 		((IDiagram)network).areDecisionsOrdered();
 		
 		switch (algorithm){
 			case 0: computeUtilities = true; 
 					propagateWithDivisions(computeUtilities,applySubsetRule,orderOfElim,groupNoDependents);
 					break;
 			case 1: computeUtilities = false; 
 					propagateWithDivisions(computeUtilities,applySubsetRule,orderOfElim,groupNoDependents);
 					break;
 			case 2: propagateWithoutDivisions(applySubsetRule,orderOfElim,groupNoDependents);
 					break;
 		}	
 
 	}
 	
 	
/**  
 * Evaluates the Influence Diagram associated with the class
 * using VariableElimination method and produce the partial
 * order between the variables
 */

public ArrayList<String> getTotalOrder() {
	NodeList decisions = ((IDiagram) network).getDecisionList();
	NodeList phase, newPhase;
	Node decisionConsidered;
	int i, j;
	ArrayList<String> orderOfElimination=new ArrayList();

	// Now, as first step, get all nodes without decisions
	// as sucessors

	phase = ((IDWithSVNodes) network).getChanceNodesWithoutDecisionsAsSucessors();

	// Now, add all of these nodes to the vector giving the
	// order of evaluation

	for (i = 0; i < phase.size(); i++) {
		orderOfElimination.add(phase.elementAt(i).getName());
	}

	// Now, go on the list of decisions

	for (i = decisions.size() - 1; i >= 0; i--) {

		// Get the decision considered in this iteration

		decisionConsidered = decisions.elementAt(i);

		// Add this decision to orderOfElimination

		orderOfElimination.add(decisionConsidered.getName());

		// Add it to phase

		phase.insertNode(decisionConsidered);

		// Now, get all chance nodes with sucessors in phase

		newPhase = ((IDiagram) network).getChanceNodesWithSucessorsInSet(phase);

		// Add all nodes in newPhase to orderOfElimination

		for (j = 0; j < newPhase.size(); j++) {
			orderOfElimination.add(newPhase.elementAt(j).getName());
			phase.insertNode(newPhase.elementAt(j));
		}
	}
	return orderOfElimination;
}
 
 
 /**
  * Propagation without divisions of potentials
  * 'resultsForPolicies' will have the utilites that determine the policies
  * 'results' will be equal than 'resultsForPolicies'
 * @param applySubsetRule Option for the evaluation
 * @param orderOfElim 
 * @param eliminateDecByUnforking 
  */
 	public void propagateWithoutDivisions(boolean applySubsetRule, ArrayList<String> orderOfElim, boolean groupNoDependents){
 		
	
		 Node x;
		 int i;
		 ArrayList<String> orderOfElimination;
		 TreePotentialsSV tree;
		 Potential policyTable;
		 
 		
 		
 		//Initializes the crono
 		
 		crono.start();
 		
		// First at all, remove all results stored in results vector

 		results.removeAllElements();
 
 	//Now, I suppose there aren't any observations
 	//	notRemoved=getNotObservedNodes();
 		
// 		We check if an elimination order is given
 	   if (orderOfElim == null){
 		   // Find out the order of elimination
 		   orderOfElimination=getTotalOrder();
 	   }
 	   else{
 		   orderOfElimination = orderOfElim;
 	   }


   		tree = new TreePotentialsSV(network);

//		Remove consecutive operators
		tree.compactTree();
		

		//Statistics about the size of the problem
		double size = tree.getSize();
		statistics.addSize(size);
		System.out.println("The size of the potentials is: "+size);
		
		


 // Loop to eliminate the variables

  for (i=0; i<orderOfElimination.size() ; i++) {
   	
  if (applySubsetRule){
  	while (tree.applySubsetRule()==true){
  		
  	}
  }
  			
  			
  //Select next variable to remove
   x=network.getNode(orderOfElimination.get(i));

  // Store the operation
   
  statistics.addOperation("Variable elimination: "+x.getName());
  
  System.out.println("Removing variable: "+x.getName());
  
  
  policyTable=tree.eliminateWithoutDivisions(x,statistics,groupNoDependents);
  
  if (x.getKindOfNode()==Node.DECISION){
   		 resultsForPolicies.add(policyTable);
   }


   //Statistics about the size of the problem
  size = tree.getSize();
   statistics.addSize(size);
   System.out.println("The size of the potentials is: "+size);

  
  } // end for

 //Reduce the tree into an only leaf
 tree.getRoot().reduce();
 
 // Finally set the final utility about the evaluation
 statistics.setFinalExpectedUtility(tree.getRoot().getRelation().getValues());
 
 //results is irrelevant. However, we do it's equal than 'resultsForPolicies'
 results=resultsForPolicies;
  
// Set the time needed to the evaluation
System.out.println("Evaluation without division of potentials, only policies");
statistics.setTime(crono.getTime());

// Shows the time needed to complete the evaluation
crono.viewTime();
 }

 /**
   * Propagation with divisions of potentials
   * 'results' will have the global utilities for each decision
   * 'resultsForPolicies' will have the utilites that determine the policies
   * for each decision 
 * @param applySubsetRule
 * @param orderOfElim 
 * @param eliminateDecByUnforking2 
   */
public void propagateWithDivisions(boolean computeUtilitiesTable, boolean applySubsetRule, ArrayList<String> orderOfElim, boolean groupNoDependents){
		
		Node x;
	
		int i;
		ArrayList<String> orderOfElimination;
		TreePotentialsSV tree;
		PotentialTable policyTable;
		PotentialTable utilitiesTable;
		TreePotentialsSV probTree;
		TreePotentialsSV utilTree;

		 
 		
 		
	   //Initializes the crono
 		
	   crono.start();
 		
	   // First at all, remove all results stored in results vector

	   results.removeAllElements();
 
   //Now, I suppose there aren't any observations
   //	notRemoved=getNotObservedNodes();
 		
//We check if an elimination order is given
	   if (orderOfElim == null){
		   // Find out the order of elimination
		   orderOfElimination=getTotalOrder();
	   }
	   else{
		   orderOfElimination = orderOfElim;
	   }

	   tree = new TreePotentialsSV(network);
	   
	   utilTree=tree.quitUtilitiesTree();
	   probTree=tree;
	   
	   //Remove consecutive operators
	   utilTree.compactTree();
	   

	//Statistics about the size of the problem
		//Statistics about the size of the problem
		double size = probTree.getSize()+utilTree.getSize();
		statistics.addSize(size);
		System.out.println("The size of the potentials is: "+size);
	

// Loop to eliminate the variables

 for (i=0; i<orderOfElimination.size() ; i++) {
 	
 	if (applySubsetRule){
 		while (utilTree.applySubsetRule()==true){
  		
 		}
 	}
   
  // Select next variable to remove

  x=network.getNode(orderOfElimination.get(i));

 // Store the operation
   
 statistics.addOperation("Variable elimination: "+x.getName());
 
 System.out.println("Removing variable: "+x.getName());

  switch (x.getKindOfNode()){
 	case Node.CHANCE:
		utilTree.eliminateWithDivisions(probTree,x,statistics,false);
		break;
	case Node.DECISION:
	//	The result of the elimination of a decision is a vector 
   //that contains two tables: one of utilities and other of policies

	if (computeUtilitiesTable){
	//Table with the utilities
		  utilitiesTable=utilTree.getUtilitiesEliminationWithD(x);
		  results.add(utilitiesTable);
	}
	
	//Table with the utilities to obtain the policy
	policyTable=utilTree.eliminateWithDivisions(probTree,x, statistics, groupNoDependents);
	resultsForPolicies.add(policyTable);
 }
 

 
  } // end for


//Reduce the tree into an only leaf
utilTree.getRoot().reduce();
 
// Finally set the final utility about the evaluation
Potential meu = utilTree.getRoot().getRelation().getValues();
statistics.setFinalExpectedUtility(meu);
System.out.println("MEU: "+meu.totalPotential());
if (computeUtilitiesTable==false){
	//results is irrelevant. However, we do it's equal than 'resultsForPolicies'
  	results=resultsForPolicies;
}


// Set the time needed to the evaluation

statistics.setTime(crono.getTime());

/*if (computeUtilitiesTable){
	System.out.println("Evaluation with division of potentials, utilities and policies");
}
else{
	System.out.println("Evaluation with division of potentials, only policies");
}*/

// Shows the time needed to complete the evaluation

//crono.viewTime();

 
  
  	
}


/**  
 * Eliminate the variable 'x' in the tree of potentials 'tree'
 */
/*private void eliminate(NodeTreeSV tree, Node x) {
	Relation rel;
	int kindNodeTreeSV;
	int operator;
	
	if (tree.getVariables().getId(x)==-1){ 
		//x isn't a variable of the potential of 'tree'
		return; 
	}
	else{
			if (tree.isLeaf()){
			//TO DO
				  return;
			}
			else {
				kindNodeTreeSV = tree.getKindOfNodeTreeSV();
				operator = tree.getOperator();
				switch (x.getKindOfNode()) {
					case Node.CHANCE :
						switch (kindNodeTreeSV) {
							case NodeTreeSV.OPERATOR :
								switch (operator) {
									case NodeTreeSV.SUM :
										eliminateInChild(tree,x);
										break;
									/*case NodeTreeSV.PRODUCT :
										break;*/
								/*}

/*								break;
							/*case NodeTreeSV.OPERAND :
								break;*/
/*						}
						break;
					/*case Node.DECISION :
						kindNodeTreeSV = tree.getKindOfNodeTreeSV();
						switch (kindNodeTreeSV) {
							case NodeTreeSV.OPERATOR :
								switch (operator) {
									case NodeTreeSV.SUM :
										break;
									case NodeTreeSV.PRODUCT :
										break;
								}
								break;
							case NodeTreeSV.OPERAND :
								break;
						}
						break;*/
/*				}//switch
			}//if..else..

		
	}
	
	
}*/

/**  
 * Eliminate the variable 'x' in all the children of the root of 'tree'
 */
/*private void eliminateInChild(TreeNodeSV tree, Node x) {
	int i;
	Vector child;
	
	child=tree.getChild();
	
	for (i=0;i<child.size();i++){
		eliminate(tree.getChild(i),x);
	}
}*/
	

public static void main(String args[]) throws ParseException, IOException {
	String argsElv[];
	argsElv= new String[1];
	argsElv[0]="C:\\bayelvira2\\Redes_para_probar_VariableEliminationSV\\ej1.elv";
 	Elvira.main(argsElv);	
}
}
