package elvira.learning.policies;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.InvalidEditException;
import elvira.Node;
import elvira.NodeList;
import elvira.RelationList;
import elvira.inference.elimination.VariableElimination;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.inference.super_value.PolicyNetwork;
import elvira.inference.uids.NodeAOUID;
import elvira.potential.DeterministicPotentialTable;
import elvira.potential.Potential;

public class RuleNode {
	//Values of the variables instantiated from the root of the tree until this state
		//public Configuration instantiations;
		
		//Node that is instantiated
		FiniteStates variable;
		
		//Integer value for node (only in leaves)
		Integer value;

		//For each state of the 'node' we have a tree of rules
		//Hashtable<Integer,RuleNode> children;
		ArrayList<RuleNode> children;
		
		RuleNode parent;
		
		Configuration configuration;

		public void constructRuleNode(FiniteStates decision, DeterministicPotentialTable auxDetPot, IDWithSVNodes diagram, ArrayList<String> auxElimOrder, Integer varToEliminate,Configuration instant, Integer indexStateParent, boolean pruneNullConfigurationsPT, PolicyNetwork pn) {
			// TODO Auto-generated method stub
			FiniteStates nodeToEliminate;
			Configuration newConf;
			RuleNode auxRuleNode;
			Potential conditionalProbs;
			
			Integer nextVarToEliminate;
			
			
			
			configuration = instant;
			//if (auxDetPot.isConstantPotential(instant)){//Leaf of the tree of rules
			if ((auxDetPot.getVariables().size()==0)||(varToEliminate==-1)){
				RuleNode child;
				variable = decision;
				//children = new Hashtable<Integer,RuleNode>();
				children = new ArrayList<RuleNode>();
				//children = null;
				value = auxDetPot.getValueInConstantPotential(instant);
				newConf = instant.duplicate();
				newConf.putValue(decision,value);
				child = new RuleNode(decision,newConf);
				//children.put(value,child);
				children.add(child);
				child.setParent(this);
				
			}
			else{
				//OJO: ESTO HAY QUE SACARLO DE AQUÍ PUES SI NO SE PONE A CALCULAR LA PN
				//CADA VEZ QUE ENTRE AQUÍ
				//PolicyNetwork pn = new PolicyNetwork(diagram);
				
				nodeToEliminate = (FiniteStates) diagram.getNode(auxElimOrder.get(varToEliminate));
				variable = nodeToEliminate;
				//children = new Hashtable<Integer,RuleNode>();
				children = new ArrayList<RuleNode>();
				//value = indexStateParent;
				value = null;
				Vector states = nodeToEliminate.getStates();
				//Select the next variable to eliminate
				nextVarToEliminate = selectNextVariableToEliminate(diagram,auxDetPot,auxElimOrder,varToEliminate);
				conditionalProbs = getConditionalProbabilitiesOfChildren(pn,instant,nodeToEliminate);
				for (int iState=0;iState<states.size();iState++){
					
						newConf = instant.duplicate();
						newConf.putValue(nodeToEliminate,iState);
					if (hasNonZeroProbability(conditionalProbs,newConf)){
						auxRuleNode = new RuleNode();
						//	if (variable.getKindOfNode()==Node.CHANCE){
						auxRuleNode.constructRuleNode(decision,auxDetPot,diagram,auxElimOrder,nextVarToEliminate,newConf,iState, pruneNullConfigurationsPT,pn);
						//children.put(iState,auxRuleNode);
						children.add(auxRuleNode);
						auxRuleNode.setParent(this);
					}
					
				}
				//children.
			}
			
			
		}

		private boolean hasNonZeroProbability(Potential conditionalProbs, Configuration instant) {
			// TODO Auto-generated method stub
			double epsilon=0.00000000000001;
			return (conditionalProbs.getValue(instant)>epsilon);
		}

		/*private Potential getConditionalProbabilitiesOfChildren(
				PolicyNetwork pn, Configuration instant,
				FiniteStates nodeToEliminate) {
			// TODO Auto-generated method stub
			RelationList probRelations;
			RelationList minimalSetOfProbRels;
			
			probRelations = pn.getInitialRelations().restrict(instant);
			
			minimalSetOfProbRels = NodeAOUID.obtainMinimalSetOfProbabilityRelations(nodeToEliminate,probRelations);
			
			return NodeAOUID.calculateConditionedProbabilitiesFromMinimalRelations(nodeToEliminate,minimalSetOfProbRels);
			
		}*/
		
		
	/*	private Potential getConditionalProbabilitiesOfChildren(
				PolicyNetwork pn, Configuration instant,
				FiniteStates nodeToEliminate) {
			// TODO Auto-generated method stub
			NodeList nodes;
			VariableElimination ve = new VariableElimination(pn);
			
			nodes = new NodeList();
			nodes.insertNode(nodeToEliminate);
			try {
				ve.getPosteriorDistributionOf(nodes,instant);
			} catch (InvalidEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			RelationList probRelations;
			RelationList minimalSetOfProbRels;
			
			probRelations = pn.getInitialRelations().restrict(instant);
			
			minimalSetOfProbRels = NodeAOUID.obtainMinimalSetOfProbabilityRelations(nodeToEliminate,probRelations);
			
			return NodeAOUID.calculateConditionedProbabilitiesFromMinimalRelations(nodeToEliminate,minimalSetOfProbRels);
			
		}*/
		
		
		private Potential getConditionalProbabilitiesOfChildren(
				PolicyNetwork pn, Configuration instant,
				FiniteStates nodeToEliminate) {
			// TODO Auto-generated method stub
			NodeList nodes;
			VariableElimination ve = new VariableElimination(pn);
			
			nodes = new NodeList();
			nodes.insertNode(nodeToEliminate);
			try {
				ve.getPosteriorDistributionOf(nodes,instant);
			} catch (InvalidEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			RelationList probRelations;
			RelationList minimalSetOfProbRels;
			
			probRelations = pn.getInitialRelations().restrict(instant);
			
			minimalSetOfProbRels = NodeAOUID.obtainMinimalSetOfProbabilityRelations(nodeToEliminate,probRelations);
			
			return NodeAOUID.calculateConditionedProbabilitiesFromMinimalRelations(nodeToEliminate,minimalSetOfProbRels);
			
		}

		public RuleNode(Node node, Configuration configuration) {
			// TODO Auto-generated constructor stub
			variable = (FiniteStates) node;
			this.configuration = configuration;
		}
		
		public RuleNode(){
			
		}

		/**
		 * @param diagram
		 * @param auxDetPot
		 * @param auxElimOrder
		 * @param varToEliminate
		 * @return The next variable to eliminate that is in the variables of the potential
		 */
		private Integer selectNextVariableToEliminate(
				IDWithSVNodes diagram, DeterministicPotentialTable auxDetPot, ArrayList<String> auxElimOrder, Integer varToEliminate) {
			// TODO Auto-generated method stub
			boolean selected;
			Node auxNode;
			String auxName;
			Integer next;
			
			Vector varsPot = auxDetPot.getVariables();
			
			selected = false;
			next = -1;
			for (int i=varToEliminate-1;(i>=0)&&(!selected);i--){
				auxName = auxElimOrder.get(i);
				auxNode = diagram.getNode(auxName);
				if (varsPot.indexOf(auxNode)!=-1){
					selected = true;
					next = i;
				}
			}
			return next;
		}

		public void print(int level) {
			// TODO Auto-generated method stub
			String auxName;
			Vector states;
			String indentation;
			
			indentation = getIndentation(level);
			if (isLeaf()){
				System.out.println(indentation+getVariable().getName()+"="+variable.getState(value));
			}
			else{
				auxName = variable.getName();
				states = variable.getStates();
				//for (int i=0;i<states.size();i++){
				for (int i=0;i<children.size();i++){
					if (children.get(i)!=null){
					String prefix = (i==0)?"":"ELSE ";
						if (!isRuleForDecisionOfPolicy()){
							System.out.println(indentation+prefix+"IF "+auxName+"=="+states.get(children.get(i).getConfiguration().getValue(variable))+" THEN ");
							children.get(i).print(level+1);
						}
						else{
							//Enumeration<RuleNode> auxChildren = children.elements();
							RuleNode auxChild;
							//auxChild = auxChildren.nextElement();
							auxChild = children.get(0);
							System.out.println(indentation+prefix+auxName+"="+auxChild.getNameOfValueAssignedToVariableOfParent());
						
					}
					}
				}
				if (!isRuleForDecisionOfPolicy()){
				System.out.println(indentation+"ENDIF");
				}
			}
		}

		/**
		 * @return true if the node is the rule that assigns a value to the decision of the policy. This node will have an only child for the corresponding value
		 */
		public boolean isRuleForDecisionOfPolicy() {
			// TODO Auto-generated method stub
			//Enumeration<RuleNode> auxChildren = children.elements();
			RuleNode auxChild;
			//auxChild = auxChildren.nextElement();
			auxChild = children.get(0);
			
			return ((auxChild.children == null)||(auxChild.children.size()==0));
		}

		private boolean isLeaf() {
			// TODO Auto-generated method stub
			return ((children==null)||(children.size()==0));
		}

		private String getIndentation(int level) {
			// TODO Auto-generated method stub
			String ind;
			ind = "";
			for (int i=0;i<level;i++){
				ind = ind+"\t";
			}
			return ind;
		}

		public RuleNode getParent() {
			// TODO Auto-generated method stub
			return parent;
		}

		public FiniteStates getVariable() {
			return variable;
		}

		public void setVariable(FiniteStates variable) {
			this.variable = variable;
		}

		public Configuration getConfiguration() {
			// TODO Auto-generated method stub
			return configuration;
		}

		/**
	 * Get the cardinality of this node
	 * 
	 * @return the number of children of this node
	 */
	public int getSize() {
		int size;
		
		if (children!=null){
			size = children.size();
		}
		else{
			size = 0;
		}
		return size;
	}

		public RuleNode getChild(int index) {
			// TODO Auto-generated method stub
			return children.get(index);
		}

		public void setParent(RuleNode parent) {
			this.parent = parent;
		}

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		public void setConfiguration(Configuration configuration) {
			this.configuration = configuration;
		}
		
		
		public Integer getValueAssignedToVariableOfParent(){
			
			return configuration.getValue(parent.getVariable());
			
		}
		
		
		public RuleNode getChildWhoseValueAssignedToVariableInConfigurations(int val){
			
			RuleNode child;
			boolean found;
			
			child = null;
			found = false;
			
			for (int i=0;(i<children.size())&&!found;i++){
				RuleNode auxChild;
				auxChild = children.get(i);
			
				if (val==auxChild.getValueAssignedToVariableOfParent()){
					found = true;
					child = auxChild;
				}
				
				
			}
			return child;
		}
		
		public String getNameOfValueAssignedToVariableOfParent(){
			FiniteStates father;
			
			father = parent.getVariable();
			
			
			return father.getState(configuration.getValue(father));
			
		}

		/*public Hashtable<Integer, RuleNode> getChildren() {
			return children;
		}

		public void setChildren(Hashtable<Integer, RuleNode> children) {
			this.children = children;
		}*/
		
		public ArrayList<RuleNode> getChildren() {
			return children;
		}

		public void setChildren(ArrayList<RuleNode> children) {
			this.children = children;
		}

		
	

		public void obtainRuleNodeAfterEliminatingRedundancy(FiniteStates decision,RuleNode oldRuleNode){
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			FiniteStates nodeToEliminate;
			Configuration newConf;
			
			Potential conditionalProbs;
			
			Integer nextVarToEliminate;
			
			
			
			
			//if (auxDetPot.isConstantPotential(instant)){//Leaf of the tree of rules
			if (oldRuleNode.getVariable()==decision){
				//We copy the oldNodeRule
				RuleNode child;
				variable = decision;
				//children = new Hashtable<Integer,RuleNode>();
				children = new ArrayList<RuleNode>();
				//children = null;
				value = oldRuleNode.getValue();
				configuration =  new Configuration(oldRuleNode.getConfiguration(),this.getVariablesInPathToTheRooth());
				newConf = configuration.duplicate();
				newConf.putValue(decision,value);
				child = new RuleNode(decision,newConf);
				//children.put(value,child);
				children.add(child);
				child.setParent(this);
				
			}
			else{
				
				boolean conditionIf = (oldRuleNode.getVariable().getKindOfNode()==Node.DECISION)||(!oldRuleNode.isRootAChanceRedundantVariable(decision));
				//conditionIf = true;
				conditionIf = (!oldRuleNode.isRootAChanceRedundantVariable(decision));
				
				if (conditionIf){
					configuration =new Configuration(oldRuleNode.getConfiguration(),this.getVariablesInPathToTheRooth());
					variable = oldRuleNode.getVariable();
					children = new ArrayList<RuleNode>();
					value = null;
					
					for (RuleNode auxChildOldRuleNode:oldRuleNode.getChildren()){
						RuleNode auxRuleNode;
						
						auxRuleNode = new RuleNode();
						//	if (variable.getKindOfNode()==Node.CHANCE){
						children.add(auxRuleNode);
						auxRuleNode.setParent(this);
						auxRuleNode.obtainRuleNodeAfterEliminatingRedundancy(decision,auxChildOldRuleNode);
						//children.put(iState,auxRuleNode);
						
					}
					
					
				}
				else{
					obtainRuleNodeAfterEliminatingRedundancy(decision,oldRuleNode.getChild(0));
							
							
				}
				
		
			}
		}

		private boolean isRootAChanceRedundantVariable(FiniteStates decision) {
			// TODO Auto-generated method stub
			boolean isRedundant;
			
			
			
			if ((children!=null)&&(children.size()>1)){
				
				isRedundant = areIdenticalTreesSelectigDecision(decision,children);
			}
			else{
				isRedundant = true;
				
			}
			
			return isRedundant;
		}

		/**
		 * @param decision 
		 * @param listOfTrees
		 * @return truee iff the trees of listOfTrees are identical when selecting the decision
		 */
		private boolean areIdenticalTreesSelectigDecision(
				FiniteStates decision, ArrayList<RuleNode> listOfTrees) {
			
			NodeList unionOfSetOfNodesOfTrees;
			boolean areIdentical;
			
			unionOfSetOfNodesOfTrees = listOfTrees.get(0).getVariablesInThePathsToTheLeaves(decision);
			for (int i=1;i<listOfTrees.size();i++){
				unionOfSetOfNodesOfTrees.merge(listOfTrees.get(0).getVariablesInThePathsToTheLeaves(decision));
			}
			
			//Evaluate all the configurations with each tree
			
			
			 int numConfigurations = (int)FiniteStates.getSize(unionOfSetOfNodesOfTrees);

			  Configuration auxConf = new Configuration(unionOfSetOfNodesOfTrees);
			  areIdentical = true;
			  for (int i=0 ; (i<numConfigurations)&&areIdentical ; i++) {
				  if (isConsistentConfigurationInAllTheTrees(decision,listOfTrees,auxConf)){
				  		  areIdentical = areIdenticalTreesSelectigDecision(decision,listOfTrees,auxConf);
				  }
				  if (areIdentical){
					  auxConf.nextConfiguration();
				  }
			  }
			  return areIdentical;
			// TODO Auto-generated method stub
			
		}

		private boolean isConsistentConfigurationInAllTheTrees(
				FiniteStates decision, ArrayList<RuleNode> listOfTrees, Configuration auxConf) {
			// TODO Auto-generated method stub
			boolean isInAll;
			
			isInAll = true;
			for (int i=0;(i<listOfTrees.size())&&isInAll;i++){
				isInAll = listOfTrees.get(i).isConsistentConfiguration(decision,auxConf);
				
			}
			
			return isInAll;
		}

		/**
		 * @param decision 
		 * @param auxConf
		 * @return true iff the configuration is consistent with the tree
		 */
		private boolean isConsistentConfiguration(FiniteStates decision, Configuration auxConf) {
			// TODO Auto-generated method stub
			boolean isConsistent;
			if (variable==decision){
				isConsistent = true;
			}
			else{
				int valInConf = auxConf.getValue(variable);
				RuleNode auxChild = this.getChildWhoseValueAssignedToVariableInConfigurations(valInConf);
				if (auxChild==null){
					isConsistent = false;
				}
				else{
					isConsistent = auxChild.isConsistentConfiguration(decision,auxConf);
				}
			}
			
			
			return isConsistent;
		}

		private boolean areIdenticalTreesSelectigDecision(
				FiniteStates decision, ArrayList<RuleNode> listOfTrees,
				Configuration auxConf) {
			// TODO Auto-generated method stub
			int selectedOption;
			int auxSelectedOption;
			boolean areIdentical;
			
			selectedOption = listOfTrees.get(0).getSelectedOption(decision,auxConf);
			areIdentical = true;
			for (int i=1;(i<listOfTrees.size())&&areIdentical;i++){
				auxSelectedOption = listOfTrees.get(i).getSelectedOption(decision,auxConf);
				areIdentical = (selectedOption==auxSelectedOption);
			}
			
			return areIdentical;
		}

		private int getSelectedOption(FiniteStates decision,
				Configuration auxConf) {
			// TODO Auto-generated method stub
			int option;
			int valInConf;
			if (variable==decision){
				option = value;
			}
			else{
				valInConf = auxConf.getValue(variable);
				RuleNode auxChild = this.getChildWhoseValueAssignedToVariableInConfigurations(valInConf);
				option = auxChild.getSelectedOption(decision, auxConf);
			}
			return option;
		}

		private NodeList getVariablesInThePathsToTheLeaves(FiniteStates decision) {
			// TODO Auto-generated method stub
			NodeList nodes;
			
			nodes = new NodeList();
			if (variable!=decision){
				if (nodes.getId(variable)==-1){
					nodes.insertNode(variable);
				}
				for (int i=0;i<children.size();i++){
					nodes.merge(getChild(i).getVariablesInThePathsToTheLeaves(decision));
				}
			}
			return nodes;
		}

		/**
		 * @return the variables contained ih the path from the root until this node (but not including the current node)
		 */
		private NodeList getVariablesInPathToTheRooth() {
			// TODO Auto-generated method stub
			NodeList path;
			Vector pathVector;
			
			path = new NodeList();
			if (parent!=null){
				pathVector = parent.getConfiguration().getVariables();
				
				
				for (int i=0;i<pathVector.size();i++){
					path.insertNode((Node) pathVector.elementAt(i));
				}
				path.insertNode(parent.getVariable());
		
				
			}
			
			return path;
		}
	
	
		
}
