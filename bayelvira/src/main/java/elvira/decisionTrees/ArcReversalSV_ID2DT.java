package elvira.decisionTrees;

import java.io.IOException;
import java.util.Vector;
import elvira.IDWithSVNodes;
import elvira.Node;
import elvira.NodeList;
import elvira.inference.super_value.ArcReversalSV;
import elvira.inference.super_value.ReductionAndEvalID;
import elvira.potential.PotentialTable;
import elvira.RelationList;

/**
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.3
 * 
 * @see elvira.inference.super_value.ArcReversalSV
 * 
 * version 0.1: class didn't extends ArcReversalSV class
 * version 0.2: class extends ArcReversalSV
 * version 0.3: Nested class 'FakePropagationStatistics' created to allow use of this
 * 	algorithm in applets without security issues
 */
public class ArcReversalSV_ID2DT extends ArcReversalSV {
	/**
	 * Order of elimination in ArcReversal_ID2DT == Left to Right Order in equivalent Decision Tree
	 */
	private Vector<Node> eliminatedVariables;
	
	/**
	 * 
	 */
	private RelationList decisionTreeRelations;

	/**
	 * 
	 */
	private IDWithSVNodes originalID;
	
	/**
	 * @return
	 */
	public Vector<Node> getEliminatedVariables() {
		return eliminatedVariables;
	}

	/**
	 * @return
	 */
	public RelationList getDecisionTreeRelations() {
		return decisionTreeRelations;
	}
	
	/**
	 * @return
	 */
	public IDWithSVNodes getOriginalID() {
		return originalID;
	}
	
	/**
	 * @return
	 */
	public IDWithSVNodes getID() {
		return (IDWithSVNodes) diag;
	}

	/**
	 * @param id
	 * 
	 * TODO: verificar que esta asignación es compatible con el funcionamiento de Elvira
	 * a la hora de utilizar los menús de la aplicación
	 */
	public ArcReversalSV_ID2DT(IDWithSVNodes id) {
		super(id.copyIDWSV());
		
		// TODO: quizá esta copia no sea necesaria
		originalID= id.copyIDWSV();
	}
	
	/**  
	 * Evaluates the Influence Diagram associated with the class
	 * using Tatman and Shachter's Algorithm.
	 *   
	 * @see elvira.inference.super_value.ArcReversalSV#evaluateDiagram(boolean, java.util.Vector)
	 */
	public void evaluateDiagram(boolean computeUtilitiesTable,Vector parameters) {
		eliminatedVariables= new Vector<Node>();
		decisionTreeRelations= new RelationList();

 		// Parameter of the algorithm that specifies whether the posibility of fake statistics
 		// to avoid security issues when run from an applet
		//
		
		diag = ((IDWithSVNodes)diag).obtainACopyWithAnOnlyValueNode();
		
		boolean fakePrintStatistics= true;		
 		if( parameters!=null && parameters.size()>=2) {
 			fakePrintStatistics = ((Boolean)(parameters.elementAt(1))).booleanValue();
 		}

 		if( fakePrintStatistics ) {
 			statistics= new FakePropagationStatistics();
 		}
 		
		super.evaluateDiagram(computeUtilitiesTable,parameters);
	}
	
	/**  
	 * To remove a chance node. Code structure based in ArcRevesalSV class
	 *  
	 * @return the result of the operation, as a <code>boolean</code>.  
	 */ 
	protected boolean removeChanceNode() {
		//List of chance nodes in the diagram
		NodeList chancesID= diag.getNodesOfKind(Node.CHANCE);
		boolean removed = false;
		Node nodeUtil=null;
		String operation;
		NodeList children;
		
		for (int i = 0;(i < chancesID.size()) && removed == false; i++) {
			Node candidateToRemove = chancesID.elementAt(i);
			
			//Check if the candidaToRemove can be removed
			if (isRemovableChance(candidateToRemove)){
				Node nodeToRemove = candidateToRemove;
				children=nodeToRemove.getChildrenNodes();

				//Reduce value nodes if it's necessary
				if (children.size() > 1) {
					//We have to reduce
					Node candidateToReduce = getCandidateValueNodeToReduceForChanceNode(nodeToRemove);
					Node valueNodeToReduce =	obtainValueNodeToReduce(nodeToRemove,candidateToReduce);
					ReductionAndEvalID.reduceNode((IDWithSVNodes) diag, valueNodeToReduce);
					nodeUtil=valueNodeToReduce;
					operation =	"Reduce: " + nodeUtil.getName() +" to eliminate: "+nodeToRemove.getName();
					System.out.println(operation);
					statistics.addOperation(operation);
				} else {
					//The chance has only one child (utility)
					nodeUtil=children.elementAt(0);
				}

				operation =	"Chance node removal: " + candidateToRemove.getName();
				System.out.println(operation);
				statistics.addOperation(operation);
				
				/*--Jorge */
				eliminatedVariables.add(0,candidateToRemove);
				
				// The relation of the utility node is modified. In this 
				// case the parents of the node to remove will be parents 
				// of the utility node 
				modifyUtilityRelation(nodeUtil, nodeToRemove, true);
				
				/*--Jorge */
				decisionTreeRelations.insertRelation( diag.getRelation(candidateToRemove) );
				
				// To store the size of the diagram just before of sum-marginalize the utiility node
				statistics.addSize(diag.calculateSizeOfPotentials());
				
				// Calculate the new expected utility  
				getExpectedUtility(nodeUtil, nodeToRemove);
				
				// The node is deleted  
				diag.removeNodeOnly(nodeToRemove);
				statistics.addTime(crono.getTime());
				
				// Set removed 
				removed = true;
			}
		}
		
		return removed;
	}

	/**  
	 * To remove a decision node  
	 * @return the result of the operation,  as a <code>boolean</code>.
	 * 
	 *  SuppressWarnings hint added: this warning is caused by a non-generic Vector
	 *  that this class inherited from Propagation base class
	 */  
	@SuppressWarnings({"unchecked"})
	protected boolean removeDecisionNode(boolean computeUtilitiesTable) {
		
		Node terminalValueNode;
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
		PotentialTable utilitiesTable;
		PotentialTable policyTable;
		
		// Obtain the value node  
		
		terminalValueNode = ((IDWithSVNodes) diag).getTerminalValueNode();
		
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
					ReductionAndEvalID.reduceNode(
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
				
				operation =	"Decision node removal: " + candidateToRemove.getName();
				statistics.addOperation(operation);
				System.out.println(operation);
				
				/*--Jorge */
				eliminatedVariables.add(0,decisionToRemove);
				
				
				if (computeUtilitiesTable){
					//Save the potential with the decision function
					//Table with the utilities
					utilitiesTable = (PotentialTable)((IDWithSVNodes)diag).getTotalUtility(terminalValueNode).sendVarToEnd(decisionToRemove);
					results.add(utilitiesTable);
					// Store the explanation with the importance of each variable
					// of the decision table
					statistics.setExplanation(decisionToRemove.getName(),utilitiesTable);
				}
				
				
				
				//Table with the utilities to obtain the policy
				policyTable=(PotentialTable)(diag.getRelation(nodeUtil).getValues().sendVarToEnd(decisionToRemove));
				resultsForPolicies.add(policyTable);
				
				
				
				// The relation of the utility node is modified. In this 
				// case the parents of the node to remove wont be parents 
				// of the utility node 
				modifyUtilityRelation(nodeUtil,decisionToRemove,false);  
				
				// To store the size before the maximization
				statistics.addSize(diag.calculateSizeOfPotentials());
				
				// Maximize the utility 
				maximizeUtility(nodeUtil,decisionToRemove,null);  
				
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
	
	/** This class is used to avoid security issues in applets: the base class ArcReversalSV use
	 * this class to write the statistics to a file/stream
	 *  
	 * @author Jorge Fern&aacute;ndez Su&aacute;rez
	 * @version 0.1
	 */
	class FakePropagationStatistics extends elvira.tools.PropagationStatistics {
		
		/* (non-Javadoc)
		 * @see elvira.tools.PropagationStatistics#printOperationsAndSizes()
		 */
		public void printOperationsAndSizes() throws IOException {
			// Para que el Applet no dé problemas de seguridad...
		}
	}	
}