package elvira.inference.super_value;


	import elvira.inference.elimination.ids.ArcReversal;

	import elvira.*;
	import elvira.tools.Crono;
	import elvira.tools.PropagationStatisticsID;

	import java.util.ArrayList;
	import java.util.Vector;
	import java.io.*;


import elvira.potential.DeterministicPotentialTable;
	import elvira.potential.Function;
	import elvira.potential.Potential;
	import elvira.potential.PotentialTable;
	import elvira.potential.SumFunction;
	import elvira.potential.ProductFunction;
import elvira.potential.UtilityPotential;

	/**
	 * @author Manuel Luque
	 * Class created by using copy and paste from ArcReversalSV, but implements an interface for the evaluation of an influence diagram
	 *
	 */
	public class ArcReversalSV2 extends IDPropagation {

		
		
		protected boolean canApplySubsetRule;


		private IDWithSVNodes diag;
		

		
		public IDWithSVNodes getDiag() {
			return diag;
		}


		public void setDiag(IDWithSVNodes diag) {
			this.diag = diag;
		}


		/**
		 * The influence diagram over which the class operates.
		 */
		//IDWithSVNodes diagsv;
		/**
		 * Creates a new propagation.
		 */

		public ArcReversalSV2() { 
			
			optimalStrategy = new Strategy(diag);
			
			stochasticStrategy = new StochasticStrategy(diag);
	 
			boolean evaluable = initialConditions();
			  
		}


		/**
		 * Creates a new propagation for a given diagram.
		 * @param id the influence diagram to evaluate.
		 */

		public ArcReversalSV2(IDWithSVNodes id) {

		  diag = id;
		 
		  optimalStrategy = new Strategy(diag);
			
		  stochasticStrategy = new StochasticStrategy(diag);
		  
		  boolean evaluable = initialConditions();
		}
		
		/**  
		 * Check whether the diagram is suitable to be evaluated:  
		 * - only directed links  
		 * - only one terminal value node  
		 * - no cycles  
		 * - directed path between decisions and value node  
		 *  
		 * @return <code>true</code> if OK. <code>false</code> in other case.
		 */  

		public boolean initialConditions() {
	  
		  boolean evaluable;  
	  
	 
		  evaluable = diag.directedLinks();
	  
		  if (evaluable == false) {  
			System.out.print("Influence Diagram with no directed links\n\n");  
			return(false);  
		  }
	 
		  evaluable = ((((IDWithSVNodes)diag).hasOnlyOneTerminalSVNode())
		  				||(((IDWithSVNodes)diag).hasOnlyOneValueNode()));
		    

		  if (evaluable == false) {  
			System.out.print("Influence Diagram with 0 or more than 1 terminal super value nodes\n or hasn't got an only utility node\n");  
			return(false);  
		  }
	  
		  evaluable = diag.hasCycles();  
		  if (evaluable == true) {  
			System.out.print("Influence Diagram with cycles\n\n");  
			return(false);  
		  }
	  
		  diag.addNonForgettingArcs();  

		  evaluable = diag.pathBetweenDecisions();  
		  if (evaluable == false) {  
			System.out.print("Influence Diagram with non ordered decisions\n\n");  
		  return(false);  
		  }
		  
		  evaluable = ((IDWithSVNodes)diag).isTreeStructureSV();  
			if (evaluable == false) {  
			  System.out.print("Influence Diagram whose structure of value nodes isn't a tree\n\n");  
			return(false);  
			}

		  // Remove barren nodes

		  //diag.eliminateRedundancy();
			diag.removeBarrenNodes();
	  
	 

		  // Return true if OK
	 
		  return(true);  
		}  
		
		/**
		 * To know whether the chance node is ready to be removed.
		 * @param the chance node to be removed.
		 * @return the result of the operation.
		 */
		protected boolean isRemovableChance(Node node){
			boolean removable;
			
			if (node.getKindOfNode()==Node.CHANCE){
				removable=precedeOnlyUtilities(node);
			}
			else{
				removable=false;			
			}
			return removable;
		}
		
		/**
		 * To know whether the decision node can be removed
		 * @param the decision node to be removed.
		 * @return the result of the operation.
		 */
		protected boolean isRemovableDecision(Node dec){
			boolean removable;
			
			if (dec.getKindOfNode()==Node.DECISION){
				if (precedeOnlyUtilities(dec)){
					Node candToReduce;
					candToReduce=getCandidateValueNodeToReduce(dec);
					//See whether the decision can be removed in 'canToReduce'
					//whitout the extraction of a node from this
					if (isRemovableDecInValueNode(dec,candToReduce)){
						removable=true;
					}
					else{
						//See whether the decision can be removed in a node extracted
						//from 'candToReduce'
						removable=isRemovableDecInReachableParents(dec,candToReduce);
					}
				}
				else{
					removable=false;
				}
				
			}
			else{
				removable=false;			
			}
			return removable;
		}
		
		/**
		 * To know if all the children of a node are utilities.
		 * It must have some children.
		 */
		private boolean precedeOnlyUtilities(Node node) {
			NodeList children;
			boolean precedeU;

			children = diag.children(node);

			//It can´t be a barren node, i.e. it must have some children
			if (children.size() > 0) {
				precedeU = true;
				//All its children must be utilities
				for (int i = 0;(i < children.size()) && precedeU; i++) {
					if (children.elementAt(i).getKindOfNode() != Node.UTILITY) {
						precedeU = false;
					}
				}
			}
			else precedeU = false;
			return precedeU;
		}

		
		/**
		 * To know whether the decision can be removed in a value node.
		 * The conditional predecessors of the value node must be informational
		 * predecessors of the decision
		 * Precondition: All the sucessors of the decision are utilitie nodes
		 * @param the decision node to be removed.
		 * @param a value node.
		 * @return the result of the operation.
		 */
		private boolean isRemovableDecInValueNode(Node decision,Node valueNode){
			NodeList functionalPredOfSV = null;
			NodeList diff;
			NodeList predOfDec;

			
			//Informational predecessors of the decision
			predOfDec=decision.getParentNodes();
			predOfDec.insertNode(decision);
			
			//Conditional predecessors of the value node must be informational
			//predecessors of the decision
			functionalPredOfSV=((IDWithSVNodes)diag).getChanceAndDecisionPredecessors(valueNode);
			diff=functionalPredOfSV.difference(decision.getParentNodes());
			diff.removeNode(decision);
			return (diff.size()==0);
					
		}
		
			/**
			 * To know whether the decision can be removed in the parents of the value node
			 * that are reachable from the decision.
			 * The conditional predecessors of the value node must be informational
			 * predecessors of the decision
			 * Precondition: All the sucessors of the decision are utilitie nodes
			 * @param the decision node to be removed.
			 * @param a value node.
			 * @return the result of the operation.
			 */
			private boolean isRemovableDecInReachableParents(Node decision,Node valueNode){
				NodeList reachableParents;
				boolean removable=true;
			
				if (valueNode.getKindOfNode()==Node.UTILITY){
					//Utility nodes hasn't got any children, so the decision isn't removable
					//in its parents.
					removable=false;
				}
				else{
						//It must be able to remove the decision in all the reachable parents.
						reachableParents=getReachableParents(decision,valueNode);
						removable=true;
						for (int i=0;(i<reachableParents.size())&&removable;i++){
							removable=(isRemovableDecInValueNode(decision,reachableParents.elementAt(i)));
						}
				}
			
				return removable;
			}
		
			
		/**  
		 * Evaluates the Influence Diagram associated with the class
		 * using Tatman and Shachter's Algorithm.  
		 */

			public void evaluateDiagram(boolean computeUtilitiesTable,Vector parameters) {
				
				
			Node value;
			Relation rel;
			PotentialTable finalUtility;
			
			
			
	 	 		
	 		
			if (parameters==null){
				canApplySubsetRule = false;
	 		}
	 		else{
		 		if (parameters.size()==0){
		 			canApplySubsetRule = true;
		 		}
		 		if (parameters.size()>=1){
		 			canApplySubsetRule = ((Boolean)(parameters.elementAt(0))).booleanValue();
		 		}
		 		if (parameters.size()>=2){
		 			ArrayList<String> orderOfElimToFollow = (ArrayList<String>)(parameters.elementAt(1));
		 			//By the moment this variable is not used. We assume it has the same value that the
		 			//order determined by ArcReversalSV2, so we give this freedom to choose the node to eliminate 
		 		}
	 		}
	 		
	 		
			// Now, begin with the evaluation

			value = ((IDWithSVNodes) diag).getTerminalValueNode();
			rel = diag.getRelation(value);

				// Transform the initial relations

				getInitialRelations();

			// Main loop: while value node has parents  

			while (value.hasParentNodes() == true) {

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
			
			finalUtility = (PotentialTable)(((IDWithSVNodes)diag).getPotentialOfGlobalUtility());
			
			optimalStrategy.setOptimal(true);
			stochasticStrategy.setOptimal(true);
			
			
		}

			

		

			
			 /**
			   * Returns all the initial relations present
			   * in the network.
			   * @return A copy of the list of the Relations of the network
			   */
			  public RelationList getInitialRelations() {
			    Relation r, rNew;
			    RelationList ir;
			    int i;
			    
			    ir = new RelationList();
			    
			    for (i=0 ; i<diag.getRelationList().size() ; i++) {
			      r = (Relation)((Relation)(diag.getRelationList().elementAt(i))).copy();
			      rNew = r;
			      ir.insertRelation(rNew);
			    }
			    
			    return ir;
			  }
			 

		/**
		 * To apply the subset rule once at a pair of value ancestors of this super
		 * value node
		 * @param value
		 * @return
		 */
			protected boolean applySubsetRule(Node value) {
		
			int type;
			int sizeParents;
			boolean isAppliedRule=false;
			Node auxI;
			Node auxJ;
			int i;
			NodeList auxNodeList=new NodeList();
			Node auxNode;
			
			//If the subset rule is forbidden then the methods finishes
			if (canApplySubsetRule == false) return false;
			else {
			
			type = value.getKindOfNode();
			
			if (type == Node.UTILITY){
				return false;
			}
			else{//(type == Node.SUPER_VALUE)
				NodeList parents = diag.parents(value);
				sizeParents=parents.size();
				
				//See if subset rule can be applied to some ancestors			
				for (i=0;(i<sizeParents)&&(isAppliedRule==false);i++){
					isAppliedRule=applySubsetRule(parents.elementAt(i));
				}
				 
				if (isAppliedRule==false){
					//See if subset rule can be applied to two utility parents
					for (i=0;(i<sizeParents-1)&&(isAppliedRule==false);i++){
						auxI=parents.elementAt(i);
						if (auxI.getKindOfNode()==Node.UTILITY){
						for (int j=i+1;(j<sizeParents)&&(isAppliedRule==false);j++){
							auxJ=parents.elementAt(j);
							if (auxJ.getKindOfNode()==Node.UTILITY){
							if (verifySubsetRule(auxI,auxJ)){
								String operation =
									"Apply subset rule to: " + auxI.getName()+ " and "+ auxJ.getName();
									
								
								System.out.println(operation);
								
								auxNodeList.insertNode(auxI);
								auxNodeList.insertNode(auxJ);
								auxNode=introduceSVNode(auxNodeList,value);
								System.out.println("Nodo introducido por la subset rule anterior: "+auxNode.getName());
								ReductionAndEvalID.reduceNode((IDWithSVNodes)diag,auxNode);
								System.out.println("Reducción del nodo: "+auxNode.getName());
								
							
									
								isAppliedRule=true;
								
							}
						}
					}
					}
					}

				}
			}
					
			return isAppliedRule;
			}
		}
		

		/*	private boolean applySubsetRule(Node value) {
		
			int type;
			int sizeParents;
			boolean isAppliedRule=false;
			Node auxI;
			Node auxJ;
			int i;
			NodeList auxNodeList=new NodeList();
			Node auxNode;
			
			type = value.getKindOfNode();
			
			//Subset rule is only applied to sv nodes
			if (type == Node.SUPER_VALUE){
				NodeList parents = diag.parents(value);
				sizeParents=parents.size();
				if (sizeParents>0){
					//Subset rule is applied to sv nodes whose parents are utility
					//nodes
					if (parents.elementAt(0).getKindOfNode()==Node.UTILITY){
						//Check if there are two parents of value to apply the
						//subset rule
						for (i=0;(i<sizeParents-1)&&(isAppliedRule==false);i++){
							auxI=parents.elementAt(i);
							for (int j=i+1;(j<sizeParents)&&(isAppliedRule==false);j++){
								auxJ=parents.elementAt(j);
								if (verifySubsetRule(auxI,auxJ)){
									String operation =
										"Apply subset rule to: " + auxI.getName()+ " and "+ auxJ.getName();
										
									statistics.addOperation(operation);
									System.out.println(operation);
									
									auxNodeList.insertNode(auxI);
									auxNodeList.insertNode(auxJ);
									auxNode=introduceSVNode(auxNodeList,value);
									ReductionAndEvalID.reduceNode((IDWithSVNodes)diag,auxNode);
									
									statistics.addSize(diag.calculateSizeOfPotentials());
									statistics.addTime(crono.getTime());
										
									isAppliedRule=true;
									
								}
							}
						}
					}
					else{
						//Invoke applySubsetRule until it's applied to some
						//ancestor
						for (i=0;(i<sizeParents)&&(isAppliedRule==false);i++){
							isAppliedRule=applySubsetRule(parents.elementAt(i));
						}
					}
				}
				else{//Error:The sv node hasn't any parents
					System.out.println("Error in function applySubsetRule in class ArcReversalSV2\n");  
					System.out.println("SV node without parents\n\n");
					isAppliedRule=false;
				}
			}
			/*else{
				System.out.println("Error in function applySubsetRule in class ArcReversalSV2\n");  
				System.out.println("Subset rule must be invoked on sv nodes\n\n");
				isAppliedRule=false; 
			}*/
	/*		return isAppliedRule;	
		}
	*/	




		/**
		 * To know if two nodes verify the subset rule, i.e. the parents of a node
		 * are parents of the other one.
		 * @param auxI
		 * @param auxJ
		 * @return
		 */
		protected static boolean verifySubsetRule(Node auxI, Node auxJ) {
			NodeList predI;
			NodeList predJ;
			
			predI=auxI.getParentNodes();
			predJ=auxJ.getParentNodes();
			return ((predI.kindOfInclusion(predJ).equals("subset"))||
			(predJ.kindOfInclusion(predI).equals("subset")));
								
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
			
		
				

			// Obtain the value node  

			//sv = ((IDWithSVNodes) diag).getTerminalValueNode();
			
			//diag.save("debug-mediastinet.elv");

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
						candidateToReduce = getCandidateValueNodeToReduce(nodeToRemove);
						
						//valueNodeToReduce =	obtainValueNodeToReduce(reachableParents);
						valueNodeToReduce =	obtainValueNodeToReduce(nodeToRemove,candidateToReduce);
						ReductionAndEvalID.reduceNode(
							(IDWithSVNodes) diag,
							valueNodeToReduce);
						nodeUtil=valueNodeToReduce;
						operation =	"Reduce: " + nodeUtil.getName() +" to eliminate: "+nodeToRemove.getName();
						System.out.println(operation);
						
					}
					else{
						//The chance has only one child (utility)
						nodeUtil=children.elementAt(0);
						
					}


					String auxName = candidateToRemove.getName();
					operation =	"Chance node removal: " + auxName;
					System.out.println(operation);
				
					
					// The relation of the utility node is modified. In this 
					// case the parents of the node to remove will be parents 
					// of the utility node 

					modifyUtilityRelation(nodeUtil, nodeToRemove, true);
					
					// Calculate the new expected utility  
					getExpectedUtility(nodeUtil, nodeToRemove);

					// The node is deleted  

					diag.removeNodeOnly(nodeToRemove);
					
//					Store the size of the diagram
			
					try {
						diag.save("kk2.elv");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// Set removed 
					removed = true;

				}
			}//for
			return removed;
		}
		

		/**
		 * To get the expected utility after a node deletion
		 */

		public void getExpectedUtility(Node util, Node toRemove) {
		  
		  Potential expected, func, dist, mixedPotential; 
		  FiniteStates fict; 
		  Configuration conf; 
		  double value; 
		  int total, i; 
		  Potential postDist;
		  Potential postUtils;
		  Potential postUtilsVarAtEnd;
		  
		  // Get utility potentialTable 

		  func = (Potential)diag.getRelation(util).getValues(); 
		  
		  // Get the probability distribution 
		  dist = (Potential)diag.getRelation(toRemove).getValues();
		  postDist=dist.sendVarToEnd(toRemove);
		  
				  
		  // Get the utilities for the chance node
		  postUtils=(Potential)diag.getPotentialOfGlobalUtility().sendVarToEnd(toRemove);
		  
				  
		   // Combine these potentials 
		  
		  expected = func.combine(dist); 
		  
		  // Marginalize over toRemove
		  
		  expected = expected.addVariable((FiniteStates)toRemove); 

		
		  diag.getRelation(util).setValues(expected); 
		} 


		/**  
		 * To remove a decision node  
		 * @return the result of the operation,  as a <code>boolean</code>.  
		 */  
	  
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
			ArrayList<String> barrens;
			PotentialTable maximum=null;
			DeterministicPotentialTable optimalPolicy=null;
			PotentialTable stochastic = null;
			

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
						candidateToReduce = getCandidateValueNodeToReduce(decisionToRemove);
						//reachableParents = getReachableParents(nodeToRemove,candidateToReduce);
						valueNodeToReduce =	obtainValueNodeToReduce(decisionToRemove,candidateToReduce);
						ReductionAndEvalID.reduceNode(
							(IDWithSVNodes) diag,
							valueNodeToReduce);
						nodeUtil=valueNodeToReduce;
						operation =	"Reduce: " + nodeUtil.getName() +" to eliminate: "+decisionToRemove.getName();
						
						System.out.println(operation);
					}
					else{
						//The chance has only one child (utility)
						nodeUtil=children.elementAt(0);
					}
					
					String auxName = candidateToRemove.getName();
					operation =	"Decision node removal: " + auxName;
					
					System.out.println(operation);
		
			
		
						  // The relation of the utility node is modified. In this 
						  // case the parents of the node to remove wont be parents 
						  // of the utility node 
						  modifyUtilityRelation(nodeUtil,decisionToRemove,false);  
		
						  
						  // Maximize the utility 
						  //optimalPolicy = maximizeUtility(nodeUtil,decisionToRemove,null);
						  Relation relUtil;
						  
						  relUtil = diag.getRelation(nodeUtil);
						  obtainDeterministicPotentialAndMaximum((PotentialTable) relUtil.getValues(),decisionToRemove,optimalPolicy,stochastic,maximum);
						  optimalStrategy.setPolicy((FiniteStates) decisionToRemove, optimalPolicy);
						  stochasticStrategy.setPolicy((FiniteStates) decisionToRemove, stochastic);
						  diag.getRelation(nodeUtil).setValues(maximum);
						  
						  
	  	
						  // The node is deleted  
						 diag.removeNodeOnly(decisionToRemove);  
						 
						 //Remove barren nodes
						 barrens = ((IDWithSVNodes)diag).removeBarrenNodesAndReturnThem();
											   
						 
						// Set removed 
							removed = true;
					
					//}//if
				}//if
			}//for
			try {
				diag.save("kk2.elv");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return removed;
						
		}  //removeDecisionNode(...)
	 
		
			
		
		
		/**
		 * @param pot (for example, the table of utilities)
		 * @param node (for example, a decision)
		 * @param policy A deterministic potential table (for example, a policy)
		 * @param maximum The maximum of the utilities
		 * @param maximum2 
		 */
		public void obtainDeterministicPotentialAndMaximum(PotentialTable pot,Node node,DeterministicPotentialTable policy,PotentialTable stochastic, PotentialTable maximum){
			NodeList nl;
			PotentialTable newPot;
			Configuration auxConf;
			Configuration maxConf;
			int numberOfValues;
			int numberOfValuesMax;
			int numberOfValuesStoc;
			
			//Policy
			policy = new DeterministicPotentialTable();
			policy.setVariable((FiniteStates) node);
			Vector variables = (Vector)(pot.getVariables().clone());
			variables.remove(node);
			policy.setVariables(variables);
			numberOfValues = (int)FiniteStates.getSize(variables);
			int[] values = new int[numberOfValues];
			policy.setValues(values);
			
			//Stochastic
			//Initalize the new potential to 0
			stochastic = new PotentialTable();
			Vector variablesStoc =  (Vector)(pot.getVariables().clone());
			variablesStoc.remove(node);
			variablesStoc.insertElementAt(node,0);
			stochastic.setVariables(variablesStoc);
			numberOfValuesStoc = (int)FiniteStates.getSize(variablesStoc);
			double[] valuesStoc = new double[numberOfValuesStoc];
			stochastic.setValues(valuesStoc);
			stochastic.setValue(0.0);
					
			
			//Maximum
			maximum = new PotentialTable();
			Vector variablesMax = (Vector)(pot.getVariables().clone());
			variablesMax.remove(node);
			maximum.setVariables(variablesMax);
			numberOfValuesMax = (int)FiniteStates.getSize(variablesMax);
			double[] valuesMax = new double[numberOfValuesMax];
			maximum.setValues(valuesMax);
			
			//Rest of variables of the potential
			nl = new NodeList(variables);
					
			//Compute the maximum configuration for each combination of the variables
			auxConf = new Configuration(nl);
			for (int i=0;i<nl.getSize();i++){
				//Compute the maximum configuration
				maxConf = pot.getMaxConfiguration(auxConf);
				
				//Set the state of the maximum configuration
				policy.setValue(auxConf,maxConf.getValue((FiniteStates)node));
				stochastic.setValue(maxConf,1.0);
				maximum.setValue(pot.getValue(auxConf));
				
				//Next configuration
				auxConf.nextConfiguration();
			}
			
			
		}
		
		

		/**  
		 * To reverse an arc  
		 * @return the result of the operation, as a <code>boolean</code>.
		 */

		protected boolean reverseArc() {
	  
		  Node terminalValue; 
		  Node candidate;  
		  Node dest; 
		  NodeList functionalPredTV;  
		  NodeList candidateChildren; 
		  String operation;
		  int i,j;  
	 
		  // we obtain the utility node 
	  
		  terminalValue = ((IDWithSVNodes)diag).getTerminalValueNode();
		   
		  functionalPredTV = ((IDWithSVNodes)diag).getChanceAndDecisionPredecessors(terminalValue); 
	  
		  for (i=0; i < functionalPredTV.size(); i++) {
	    
			candidate = functionalPredTV.elementAt(i); 
	    
			if (candidate.getKindOfNode() == Node.CHANCE) {
			  // We look for a node: 
			  //   - parent of the utility node 
			  //   - is not parent of a decision nodo
			  //   - parent of another chance node 
			  //         - only one path between these two nodes 
	      
			  candidateChildren = diag.children(candidate);
			  if (candidate.hasDirectDecisionChild() == false && 
				  candidate.isUtilityParent() == true && candidateChildren.size() > 1) {

				 // Consider the childrens of the candidate. 
				 // Revert the arc if there is only one 
				 // path between them 
		
				 for (j=0; j < candidateChildren.size(); j++) {
					dest = candidateChildren.elementAt(j); 

				   // Act if is a chance node

				   if (dest.getKindOfNode() == Node.CHANCE) {

					  // Now we see if there is another link between them 

					 if (candidate.moreThanAPath(dest) == false) {
						// The arc can be reversed : save operation and size

					   operation="Arc reversal: "+candidate.getName() + "-> "+dest.getName(); 
						
							
							System.out.println(operation);

						// We modify the relations of the nodes 
		    
						modifyRelations(candidate,dest); 
		    
						// We get the posterior distributions 
		    
						getPosteriorDistributions(candidate,dest); 

					   // Once the operation is done, notes down the size

					   
										 
						/*try {
							diag.save("kk2.elv");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
		    
						// Return true 
		    
						return(true); 
					}
				  }
				} 
			  } 
			} 
		  } 
		  return(false);  
		}

		
		

		/**
		 * Modify the relations of the nodes for reverse arc
		 * operations.
		 */

		public void modifyRelations(Node origin, Node dest) { 
		  
		  Relation originRel, destRel; 
		  NodeList originParents, destParents,
			   finalDestVariables = new NodeList(),
			   finalOriginVariables=new NodeList(); 
		  Link link; 
		  int i; 
		  
		  // Get the parents of the origin 
		  
		  originParents = diag.parents(origin); 
		  destParents = diag.parents(dest); 
		  
		  // Get the relations of both nodes 
		  
		  originRel = diag.getRelation(origin); 
		  destRel = diag.getRelation(dest); 
		  
		  // Add links for the dest node 
		  
		  diag.addLinks(originParents,dest); 
		  
		  // Remove the link between origin and dest 
		  
		  link = diag.getLink(origin,dest); 
		  diag.removeLinkOnly(link); 
		  
		  // The origin inherites the parents of the dest, and we have 
		  // to insert the dest as parent 
		  
		  diag.addLinks(destParents,origin); 
		  try{ 
		    diag.createLink(dest,origin,true); 
		  } catch (InvalidEditException iee){;} 
		  
		  // Modify the variables of the relations 
		  // First, eliminate origin as parent of dest 
		  
		  destParents.removeNode(origin); 
		  
		  // a) for dest node 
		  
		  finalDestVariables.insertNode(dest); 
		  finalDestVariables.merge(destParents); 
		  finalDestVariables.merge(originParents); 
		  destRel.setVariables(finalDestVariables); 
		  
		  // b) for origin node 
		  
		  finalOriginVariables.insertNode(origin); 
		  finalOriginVariables.merge(originParents); 
		  finalOriginVariables.merge(destParents); 
		  finalOriginVariables.insertNode(dest); 
		  originRel.setVariables(finalOriginVariables); 
		} 

		/** 
		 * To get the posterior distributions as result of an arc reversal.
		 * @param origin the origin node.
		 * @param dest the destination node.
		 */

		public void getPosteriorDistributions(Node origin, Node dest) {
		  
		  Potential initialOrigin, initialDest, finalOrigin, finalDest; 
		  
		  // To get the initial distributions 
		  
		  initialOrigin = (Potential)diag.getRelation(origin).getValues(); 
		  initialDest = (Potential)diag.getRelation(dest).getValues(); 
		  
		  // First, get the final distribution for dest 
		  
		  finalDest = initialDest.combine(initialOrigin); 
		  finalDest = finalDest.addVariable((FiniteStates)origin); 
		  
		  // Now, the final distribution for origin 
		  
		  finalOrigin = initialOrigin.combine(initialDest); 
		  finalOrigin = finalOrigin.divide(finalDest); 

		  
		  // Set the final distributions, once transformed
		  
		  diag.getRelation(origin).setValues(finalOrigin); 
		  diag.getRelation(dest).setValues(finalDest); 
		} 

		/**  
		 * It returns the value node which is descendent of 'node' in all
		 * the paths from 'node', so it can be reduced by Tatman and
		 * Shachter's algorithm in order to eliminate a chance or decision variable. No optimizations are applied in this
		 * method.
		 */


	public Node getCandidateValueNodeToReduce(Node node){
		Node commonDesc;
		boolean foundMinimum=false;
		NodeList parents;
		Node auxValueNode;
			
		commonDesc=((IDWithSVNodes)diag).getTerminalValueNode();
		//If commonDesc is UTILITY it's candidate to reduce.
		//Else, it's necessary the search.
		if (commonDesc.getKindOfNode()==Node.SUPER_VALUE){
		//The search of the minimum common descendent begins
			  //at the terminal super value node. His parents will be studied
			  while (foundMinimum==false){
				  parents=commonDesc.getParentNodes();
				  foundMinimum=true;
				  //Check if some parent is common descendent of 'utils'
				  for (int i=0;i<parents.size();i++){
					  auxValueNode=parents.elementAt(i);
					  if (this.isJunctionValueNodeForDecision(node,auxValueNode)){	
						  foundMinimum=false;
						  commonDesc=auxValueNode;
					  }
				  }
			  }
		}
		
		return commonDesc;
	}


		
		/**
		 * To know whether a value node is a junction of all the paths from 
		 * a chance or decision node. 
		 * Precondition: All the sucessors of the decision are utilitie nodes
		 * @param a decision node
		 * @param a value node.
		 * @return the result of the operation.
		 */

		private boolean isJunctionValueNodeForDecision(Node chanceOrDecNode, Node valueNode){
			NodeList influentialUtils;
			NodeList negativeUtilities;
			
			influentialUtils=chanceOrDecNode.getChildrenNodes();
			negativeUtilities = obtainNegativeUtilityNodes();
			influentialUtils.merge(negativeUtilities);
			return diag.areAscendantsOf(valueNode,influentialUtils);
		}
			

		private NodeList obtainNegativeUtilityNodes() {
			// TODO Auto-generated method stub
			NodeList negativeUtils;
			NodeList utils;
			
			utils = diag.getNodesOfKind(Node.UTILITY);
			negativeUtils = new NodeList();
			for (int i=0;i<utils.size();i++){
				Node util = utils.elementAt(i);
				double min = diag.getRelation(util).getValues().getMinimum(null);
				if (min<0){
					negativeUtils.insertNode(util);
				}
			}
			return negativeUtils;
			
		}


		/**  
		 * It returns the value node which is descendent of 'node' in all
		 * the paths from 'node', so it can be reduced by Tatman and
		 * Shachter's algorithm in order to eliminate a chance or decision variable. No optimizations are applied in this
		 * method. See method ...
		 */


	public NodeList getReachableParents(Node chanceOrDec,Node nodeToReduce){
		
		NodeList parentsOfValue;
		NodeList reachableFromChanceOrDec;
		
		
		
		if (nodeToReduce.getKindOfNode()==Node.UTILITY){
			reachableFromChanceOrDec=null;
		}
		else{
			//List of reachable nodes from the node to remove
			reachableFromChanceOrDec=diag.descendantsList(chanceOrDec);
		
			  //Parents of the node that we were going to reduce
			 parentsOfValue=nodeToReduce.getParentNodes();
		
			  //Compute the list of reachable parents from the nodes of 'utils'
			reachableFromChanceOrDec=parentsOfValue.intersection(reachableFromChanceOrDec);

		}
		
			
		return reachableFromChanceOrDec;
	}

	/**
	 * To know whether a value node is a junction of all the paths from 
	 * a chance or decision node. 
	 * Precondition: All the sucessors of the decision are utilitie nodes
	 * @param a chance node
	 * @param a value node.
	 * @return the result of the operation.
	 */

	private boolean isJunctionValueNode(Node chanceOrDecNode, Node valueNode){
		NodeList childrenUtils;
		
		childrenUtils=chanceOrDecNode.getChildrenNodes();
		return diag.areAscendantsOf(valueNode,childrenUtils);
	}
		



	/** 
	 * To change the relation of the utility node.
	 * @param toRemove the node to be removed.
	 * @param inherit <ul>
	 * <li>1 (the parents of the node to remove will be inherited).
	 * <li>0 (the parents of the node to remove will not be inherited).
	 * </ul>
	 */

	public void modifyUtilityRelation(Node util, Node toRemove, boolean inherit) {

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
	 * It returns the value node that must be reduced in order to the conditions
	 * of Shachter's algorithm can be applied to eliminate a chance or decision node.
	 * This method creates (if it's necessary) a new node which is made
	 * children of the parents of 'sv' that are reachable from 'candToReduce'
	 * @ param chanceOrDec chance or decision node which will be eliminated
	 * @ param candToReduce value node that is "always" reachable from chanceOrDec
	 * @ return the node that must be reduced
	 */
	public Node obtainValueNodeToReduce(Node chanceOrDec,Node candToReduce){	

		NodeList reachableParents;
		Node nodeToReduce;

		reachableParents = getReachableParents(chanceOrDec,candToReduce);
		
		nodeToReduce=introduceSVNode(reachableParents,candToReduce);
		System.out.println("Nodo introducido: "+nodeToReduce.getName());
		
		
		return nodeToReduce;
	}
		



	/**
	 * Introduce a value node of the same kind of 'sv' that joins 'nodesToJoin'.
	 * It's set as child of 'nodesToJoin' and parent of 'sv'.  
	 * @param nodesToJoin
	 * @param sv
	 * @return
	 */
	protected Node introduceSVNode(NodeList nodesToJoin, Node sv) {
		Node introducedNode;
		NodeList parents;
		Node auxNode;
		Function functionSV;
		UtilityPotential potSV;
		Function functionNewNode;

		parents = sv.getParentNodes();

		if (nodesToJoin.size() == parents.size()) {
			//It isn't necessary to introduce a new super value node
			introducedNode = sv;
		} else {
			String nameOfNewNode;
			Node newNode;

			//Create a node to join the 'nodesToJoin'
			nameOfNewNode = nodesToJoin.elementAt(0).getName() + nodesToJoin.elementAt(1).getName();
			newNode = new Continuous();
			newNode.setName(nameOfNewNode);
			newNode.setKindOfNode(Node.UTILITY);

			//Add the new node to the diagram
			try {
				diag.addNode(newNode);
			} catch (InvalidEditException iee) {
			};
			diag.addRelation(newNode);

			//Removal of the links among the nodes 'nodesToJoin'
			//and the node 'sv' and redirect the 'nodesToJoin'
			//to the new super value node
			for (int i = 0; i < nodesToJoin.size(); i++) {
				auxNode = nodesToJoin.elementAt(i);

				//Remove the link to the node 'sv'
				try {
					diag.removeLink(auxNode, sv);
				} catch (InvalidEditException iee) {
					;
				}

				//Direct the link from 'auxNode' to the new super value node
				try {
					diag.createLink(auxNode, newNode);
				} catch (InvalidEditException iee) {
					;
				}

			}

			//Set the potentials as arguments in newNode
			for (int i = 0; i < nodesToJoin.size(); i++) {
				auxNode = nodesToJoin.elementAt(i);
				ReductionAndEvalID.updateArgumentChild(
					(IDWithSVNodes) diag,
					auxNode,
					newNode);
			}

			//Create a link between 'newNode' and 'sv'
			try {
				diag.createLink(newNode, sv);
			} catch (InvalidEditException iee) {
				;
			}

			//Set the appropiate kind of super value node
			potSV=((UtilityPotential) diag.getRelation(sv).getValues());
			functionSV=potSV.getFunction();
			if (functionSV.getClass()==SumFunction.class){
				functionNewNode=new SumFunction();
			}
			else{
				functionNewNode=new ProductFunction();
			}
			((UtilityPotential)(diag.getRelation(newNode).getValues())).setFunction(functionNewNode);
			//Set the potential of newNode as arguments in sv
			ReductionAndEvalID.updateArgumentChild(
				(IDWithSVNodes) diag,
				newNode,
				sv);

			introducedNode=newNode;
		}
		return introducedNode;
	}


	



	}//end class


