package elvira.inference.super_value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import elvira.Continuous;
import elvira.IDWithSVNodes;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.potential.Function;
import elvira.potential.PotentialTable;
import elvira.potential.ProductFunction;
import elvira.potential.SumFunction;
import elvira.potential.UtilityPotential;

/**  
 * Class <code>QualitativeArcReversalSV</code>. Implements the solution of 
 * an influence diagram with super-value nodes. No updates of utilities nor probabilities
 * are made. In this moment, the only objective is to obtain the order of elimination.
 * In the future, other objectives will be to get measures about the evaluation process and
 * determine the variables relevant for decision tables.
 * @author Manuel Luque Gallego 
 * @since 09/06/2006
 */
public class QualitativeArcReversalSV extends ArcReversalSV {
		
	/**
	 * Array to store the order of elimination. The first element in the arraylist is the 
	 * first variable to be eliminated in the evaluation.
	 */
	ArrayList<String> orderOfElimination;
	
	public QualitativeArcReversalSV(IDWithSVNodes id){
		super(id.qualitativeCopyWithRelationsSV());
		orderOfElimination = new ArrayList();
	}

	public ArrayList<String> getOrderOfElimination() {
		return orderOfElimination;
	}
	
	
	/**  
	 * Evaluates the Influence Diagram associated with the class
	 * using Tatman and Shachter's Algorithm.  
	 */

		public void evaluateDiagram(boolean computeUtilitiesTable,Vector parameters) {
			
			
		Node value;
		Relation rel;
		
		
 		//Parameter of the algorithm that specifies whether the posibility of applying
 		//the subset rule exists. Default value = false.
 		if (parameters==null){
 			canApplySubsetRule = false;
 		}
 		else{
	 		if (parameters.size()==0){
	 			canApplySubsetRule = true;
	 		}
	 		else{
	 			canApplySubsetRule = ((Boolean)(parameters.elementAt(0))).booleanValue();
	 		}
 		}

		crono.start();

		// First at all put in the vector of operations and sizes
		// the initial situation of the IDiagram

		statistics.addOperation("Start of evaluation: ");
		statistics.addSize(diag.getProblemSize());
		statistics.addTime(crono.getTime());

		// Now, begin with the evaluation

		value = ((IDWithSVNodes) diag).getTerminalValueNode();
		rel = diag.getRelation(value);

			// Transform the initial relations

			getInitialRelations();

		// Main loop: while value node has parents  

		while (value.hasParentNodes() == true) {

			//EN PRUEBAS (MLUQUE)
			//Try applying the subset rule
			if (applySubsetRule(value) == false) {

				//First, try it with a decision node  
				if (removeDecisionNode(computeUtilitiesTable) == false) {

					// Second, try to eliminate a chance node  
					if (removeChanceNode() == false) {

						// Try an arc inversion  
						if (reverseArc() == false) {
							System.out.print("Error in evaluation algorithm\n");
							value.print();
							return;
						}
					}
				}
			}
		}
		
	if (computeUtilitiesTable==false){
	//results is irrelevant. However, we do it's equal than 'resultsForPolicies'
	   results=resultsForPolicies;
	}

		// Now store the final value for the utility

		statistics.setFinalExpectedUtility(diag.getRelation(value).getValues());

		// Set the time needed to the evaluation

		statistics.setTime(crono.getTime());

		/*if (computeUtilitiesTable){
			System.out.println("Tatman and Shachter, utilities and policies");
		}
		else{
			System.out.println("Tatman and Shachter, only policies");
		}*/
		
		// Shows the time needed to complete the evaluation

		//crono.viewTime();

		// Finally, generates the file with the statistics

		try {
			statistics.printOperationsAndSizes();
		} catch (IOException e) {
		};
	}

	
		/**  
		 * To remove a chance node  
		 * @return the result of the operation,  as a <code>boolean</code>.  
		 */ 
		protected boolean removeChanceNode() {

			Node candidateToReduce;
			Node candidateToRemove;
			Node nodeToRemove;
			String operation;
			int i;
			NodeList children;
			Node valueNodeToReduce;
			
			NodeList chancesID;
			boolean removed = false;
			Node nodeUtil=null;
			String auxName;
			
		
				

			// Obtain the value node  

			//sv = ((IDWithSVNodes) diag).getTerminalValueNode();
			

			//List of chance nodes in the diagram
			chancesID = diag.getNodesOfKind(Node.CHANCE);
			
			for (i = 0;(i < chancesID.size()) && removed == false; i++) {

				candidateToRemove = chancesID.elementAt(i);
							
				//Check if the candidaToRemove can be removed
				if (isRemovableChance(candidateToRemove)){

					nodeToRemove = candidateToRemove;
					children=nodeToRemove.getChildrenNodes();
					//Reduce value nodes if it's necessary
								
					if (children.size() > 1) {
						//We have to reduce
						candidateToReduce = getCandidateValueNodeToReduceForChanceNode(nodeToRemove);
						
						//valueNodeToReduce =	obtainValueNodeToReduce(reachableParents);
						valueNodeToReduce =	obtainValueNodeToReduce(nodeToRemove,candidateToReduce);
						ReductionAndEvalID.reduceNodeQualitatively(
							(IDWithSVNodes) diag,
							valueNodeToReduce);
						nodeUtil=valueNodeToReduce;
						operation =	"Reduce: " + nodeUtil.getName() +" to eliminate: "+nodeToRemove.getName();
						System.out.println(operation);
						statistics.addOperation(operation);
					}
					else{
						//The chance has only one child (utility)
						nodeUtil=children.elementAt(0);
						
					}

					auxName = candidateToRemove.getName();
					operation =	"Chance node removal: " + auxName;
					System.out.println(operation);
					statistics.addOperation(operation);
					
					//Add the name of the node to 'orderOfElimination'
					orderOfElimination.add(auxName);
					
					// The relation of the utility node is modified. In this 
					// case the parents of the node to remove will be parents 
					// of the utility node 

					modifyUtilityLinks(nodeUtil, nodeToRemove, true);
					
//					 To store the size of the diagram just before of sum-marginalize the utiility node
					
					statistics.addSize(diag.calculateSizeOfPotentials());

					// The node is deleted  

					diag.removeNodeOnly(nodeToRemove);

					statistics.addTime(crono.getTime());

					// Set removed 
					removed = true;

				}
			}//for
			return removed;
		}
		
		
		/** 
		 * To change the relation of the utility node.
		 * @param toRemove the node to be removed.
		 * @param inherit <ul>
		 * <li>1 (the parents of the node to remove will be inherited).
		 * <li>0 (the parents of the node to remove will not be inherited).
		 * </ul>
		 */

		public void modifyUtilityLinks(Node util, Node toRemove, boolean inherit) {

			Link link;
			NodeList finalVariables, parentNodes, inherited;
			

			finalVariables = new NodeList();
			finalVariables.insertNode(util);
			parentNodes = diag.parents(util);
			inherited = diag.parents(toRemove);

			finalVariables.merge(parentNodes);

			if (inherit == true) {
				finalVariables.merge(inherited);

				// Add new links 

				diag.addLinks(inherited, util);
			}

			// Remove link between toRemove and util 

			link = diag.getLink(toRemove, util);
			try {
				diag.removeLink(link);
			} catch (InvalidEditException iee) {
				;
			}

			// Modify the relation of the value node 

			finalVariables.removeNode(toRemove);
			diag.getRelation(util).setVariables(finalVariables);
		}


		
		/**  
		 * To remove a decision node  
		 * @return the result of the operation,  as a <code>boolean</code>.  
		 */  
	  
		protected boolean removeDecisionNode(boolean computeUtilitiesTable) {
	  

			Node candidateToReduce;
			Node candidateToRemove;
			Node decisionToRemove;
			String operation;
			int i;
			NodeList children;
			Node valueNodeToReduce=null;
			NodeList decisionsID;
			boolean removed = false;
			Node nodeUtil=null;

	
			
			//List of chance nodes in the diagram
			decisionsID = diag.getNodesOfKind(Node.DECISION);
			
			for (i = 0;(i < decisionsID.size()) && removed == false; i++) {

				candidateToRemove = decisionsID.elementAt(i);
				
				if (isRemovableDecision(candidateToRemove)){	
					decisionToRemove= candidateToRemove;
					
					
					children=decisionToRemove.getChildrenNodes();
					//Reduce value nodes if it's necessary
				
					if (children.size() > 1) {
						//We have to reduce
						candidateToReduce = getCandidateValueNodeToReduceForDecisionNode(decisionToRemove);
						//reachableParents = getReachableParents(nodeToRemove,candidateToReduce);
						valueNodeToReduce =	obtainValueNodeToReduce(decisionToRemove,candidateToReduce);
						ReductionAndEvalID.reduceNodeQualitatively(
							(IDWithSVNodes) diag,
							valueNodeToReduce);
						nodeUtil=valueNodeToReduce;
						operation =	"Reduce: " + nodeUtil.getName() +" to eliminate: "+decisionToRemove.getName();
						statistics.addOperation(operation);
						System.out.println(operation);
					}
					else{
						//The chance has only one child (utility)
						nodeUtil=children.elementAt(0);
					}
					
					String auxName = candidateToRemove.getName();
					operation =	"Decision node removal: " + auxName;
					statistics.addOperation(operation);
					System.out.println(operation);
					
//					Add the name of the node to 'orderOfElimination'
					orderOfElimination.add(auxName);
				

					
		
						  // The relation of the utility node is modified. In this 
						  // case the parents of the node to remove wont be parents 
						  // of the utility node 
						  modifyUtilityLinks(nodeUtil,decisionToRemove,false);  
		
//						 To store the size before the maximization
						  statistics.addSize(diag.calculateSizeOfPotentials());
						  
						  // The node is deleted  
						 diag.removeNodeOnly(decisionToRemove);  
						 
						 //Remove barren nodes
						 diag.removeBarrenNodes();

					   
						
					   statistics.addTime(crono.getTime());
		
						// Set removed 
							removed = true;
					
					//}//if
				}//if
			}//for
			return removed;
						
		}  //removeDecisionNode(...)
	 

		

	
			
		
}
