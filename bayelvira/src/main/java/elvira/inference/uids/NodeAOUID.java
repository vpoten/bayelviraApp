package elvira.inference.uids;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
//import java.util.TreeSet;
import java.util.Vector;

import elvira.Configuration;
import elvira.Evidence;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.clustering.LazyPenniless;
import elvira.inference.clustering.ShenoyShaferPropagation;
import elvira.inference.uids.NodeGSDAG.RelationsNodeGSDAG;
import elvira.inference.uids.NodeGSDAG.TypeOfNodeGSDAG;
import elvira.potential.Potential;
import elvira.potential.PotentialMTree;
import elvira.potential.PotentialTable;
import elvira.potential.PotentialTree;
import elvira.potential.SumFunction;
import elvira.tools.Jama.util.Maths;
import elvira.tools.statistics.analysis.Stat;

/**
 * @author Manolo Luque
 *
 */
public class NodeAOUID extends Node {
	//Unconstrained influence diagram
	protected UID uid;
	
	//Best child of the node in a partial solution
	//It's only necessary for BRANCH and DECISION nodes, because are nodes
	//of kind OR.
	//In CHANCE nodes, as they are AND nodes, all the children belongs to the partial solution
	protected NodeAOUID bestChildInPartialSolution;
	
	public enum TypeOfNodeAOUID {DECISION, CHANCE, BRANCH}
	
	
	//Values of the variables instantiated from the root of the tree until this state
	//It's used to check equivalent states
	public Configuration instantiations;
	
	//Value of the heuristic
	protected double f;
	
	//It indicates if the node is a leaf
	protected boolean solved;

	protected TypeOfNodeAOUID type;

	//Name of the variable of the state. Only appliable for CHANCE and DECISION NodeAOUIDs.
	protected String nameOfVariable;
	
	//NodeGSDAG associated to this NodeAOUID
	//A NodeGSDAG can have several variables, but each one is associated to it
	protected NodeGSDAG nodeGSDAG;
	
	//The probability and utility relations are instantiated in each NodeAOUID with
	//the current configuration
	//However, managing potentials with dinamic programming requires not instantiating the
	//the potentials in the generated children. So this attributes are not used
	//by the algorithm that uses dinamic programming. Instead of it, we use a global utility
	//and probability potentials stored in the AOGraphUIDDP
//	private RelationList probabilityRelations;
	
//	private RelationList utilityRelations;
	
	//Index of the variable of the node in the NodeGSDAG
	//It's usually 0, but sometimes we can have more variables in a same NodeGSDAG
	//int indexInNOdeGSDAG;
	
	//Only appliable if the type CHANCE. It contains the probability of the variable	//given the past
	//	double probability;
	protected Potential conditionalProbs;

	protected GraphAOUID graphUID;
	
	
	/**
	 * It indicates if the node is open, so it could be expanded.
	 */
	private boolean open;
	
	
	
	public NodeAOUID(UID uid2, GSDAG gsdag, GraphAOUID graphUID2) {
		// TODO Auto-generated constructor stub
		
		//Keep a pointer to the UID
		uid = uid2;
		
		//Keep a pointer to the graphUID
		graphUID =graphUID2; 
		
		nodeGSDAG = gsdag.root;
		if (nodeGSDAG.type!=NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
			nameOfVariable = gsdag.root.getVariables().get(0);
		}
		type = getTypeFromGSDAG(nodeGSDAG.type);
		
		instantiations = new Configuration();
		
		solved = false;
		
		calculateValueOfHeuristic(null);
	}

	
	public NodeAOUID() {
		// TODO Auto-generated constructor stub
	}


	/* (non-Javadoc)
	 * @see elvira.Node#copy()
	 * Copy the NodeAOUID in a new NodeAOUID.
	 */
	public NodeAOUID copy(){
		NodeAOUID auxNode;
		
		auxNode = new NodeAOUID();
		auxNode.uid = uid;
		auxNode.graphUID = graphUID;
		auxNode.instantiations = instantiations.duplicate();
		auxNode.f = f;
		auxNode.type = type;
		auxNode.nameOfVariable = nameOfVariable;
		auxNode.nodeGSDAG = nodeGSDAG;

		return auxNode;
	}
	
	protected TypeOfNodeAOUID getTypeFromGSDAG(TypeOfNodeGSDAG type2) {
		// TODO Auto-generated method stub
		TypeOfNodeAOUID auxType = null;
		switch (type2){
		case BRANCH:
			auxType=TypeOfNodeAOUID.BRANCH;
			break;
		case DECISION:
			auxType=TypeOfNodeAOUID.DECISION;
			break;
		case CHANCE:
			auxType=TypeOfNodeAOUID.CHANCE;
			break;
			
		}
		return auxType;
	}

	//Computes the value of the heuristic with the sum of the maximum for
	//the utility nodes 
	/**
	 * @param father It's the father of the node receiving the message, in the AO graph
	 * It can be used to save computation of the heuristic (for example, when it's implemented
	 * like a list of heuristics.
	 */
	protected void calculateValueOfHeuristic(NodeAOUID father) {
		NodeGSDAG last;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		double f1;
		double f2;
		
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();
		instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
		instantProbRels = instantiateRelations(last.getCurrentProbabilityRelations());
		
		f=heuristic(instantProbRels,instantUtilRels);
		
	//	calculateTheValueOfNonAdmissibleHeuristic();
				
			
	}
	
//	Computes the value of the heuristic with the sum of the maximum for
	//the utility nodes 
	protected void calculateTheValueOfNonAdmissibleHeuristic() {
		NodeGSDAG last;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		double f1;
		double f2;
		
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();
		instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
		//instantProbRels = instantiateRelations(last.probabilityRelations);
		
		nonAdmissibleHeuristic(null,instantUtilRels);
				
			
	}
	
		
	
	protected double heuristic(RelationList instantProbRels, RelationList instantUtilRels) {
		// TODO Auto-generated method stub
		double f1,f2;
		double h;
		
		//f1 = heuristicMaximumGlobalUtility(null,instantUtilRels);
		f1 = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
		if (graphUID.applyDynamicWeighting){
			f1 = modifyHeuristicWithDynamicWeighting(f1,graphUID.gsdag.getLastNodeGSDAG());
		}
		//System.out.println("The value of the admissible heuristic maximum of the global utilitiy is: "+f1+".");
		return f1;
		//f2 = heuristicSum(instantProbRels,instantUtilRels);
		//return f2;
		/*if (f1<f2){
			graphUID.selectedHeuristic[0]++;
			h=f1;
		}
		else{
			graphUID.selectedHeuristic[1]++;
			h=f2;
		}*/
		//return h;
	}
	
	protected double modifyHeuristicWithDynamicWeighting(double h){
		return modifyHeuristicWithDynamicWeighting(h,graphUID.gsdag.getLastNodeGSDAG());
		
	}

	protected double modifyHeuristicWithDynamicWeighting(double h,NodeGSDAG nodeOfDP){
		
			// TODO Auto-generated method stub
		double epsilon = 0.1;
		int distanceFromRootToNodeOfDP;
		int distanceFromRootToNodeGSDAG;
		
	
		distanceFromRootToNodeOfDP = nodeOfDP.distanceToRootNode();
		
		distanceFromRootToNodeGSDAG = nodeGSDAG.distanceToRootNode();
		
		//this is not completely correct, but it's a temporal solution
		if (distanceFromRootToNodeOfDP<distanceFromRootToNodeGSDAG){
			return h;
		}
		else{
			return h*(1-epsilon*(1-(distanceFromRootToNodeGSDAG/distanceFromRootToNodeOfDP)));
		}
		
	}


	protected static double nonAdmissibleHeuristic(RelationList instantProbRels, RelationList instantUtilRels) {
		// TODO Auto-generated method stub
		double f1,f2;
		double h;
		
		//f1 = heuristicMaximumGlobalUtility(null,instantUtilRels);
		f1 = heuristicMeanGlobalUtilityByDP(instantUtilRels);
		System.out.println("Non-admissible heuristic (mean of the global utility) is:"+f1+".");
	    return f1;
		//f2 = heuristicSum(instantProbRels,instantUtilRels);
		//return f2;
		/*if (f1<f2){
			graphUID.selectedHeuristic[0]++;
			h=f1;
		}
		else{
			graphUID.selectedHeuristic[1]++;
			h=f2;
		}*/
		//return h;
	}


	
	/**
	 * It eliminates the variable 'nodeToElim' by max and updates the set utility potentials 'instantUtilRels'
	 * @param nodeToElim
	 * @param instantUtilRels
	 */
	private static void eliminateVariableByDP(String function,Node nodeToElim, RelationList instantUtilRels) {
		// TODO Auto-generated method stub
		Vector vars;
	
		RelationList utilRelsOfElim;
//		PotentialTable newProbPot = null;
	
	//	Relation newProbRel;
		Relation newUtilRel;
		
	           
        //Elimination of the variable from the utility potentials
	
		//Combine the utility potentials
            utilRelsOfElim = instantUtilRels.getRelationsOfAndRemove(nodeToElim);
         
            if (utilRelsOfElim.size()>0){
            
    		PotentialTable utilPot = (PotentialTable)(utilRelsOfElim.elementAt(0).getValues());
			for(int j=1;j<utilRelsOfElim.size();j++){
				utilPot = utilPot.combine((PotentialTable) utilRelsOfElim.elementAt(j).getValues(),new SumFunction());
			}
	
			
			if (function=="Max"){
				
			//We prepare the maximization
			vars = new Vector(utilPot.getVariables());
			vars.removeElement(nodeToElim);
						
			//Maximize the utility potential over nodeToElim
			utilPot = (PotentialTable) utilPot.maxMarginalizePotential(vars);
			}
			else if (function=="Sum"){
//				Sum the utility potential over nodeToElim
				utilPot = (PotentialTable) utilPot.addVariable(nodeToElim);
			}
			else{
				System.out.println("Error in method eliminateVariableByDP of class NodeAOUID.");
				System.out.println("The argument 'function' must be Sum or Max");
			}
			
			//Create a new relation that is added to the utility relations
            newUtilRel = new Relation();
            // Set the kind for the final relation 
            newUtilRel.setKind(Relation.POTENTIAL);
            newUtilRel.getVariables().setNodes((Vector)utilPot.getVariables().clone());
            newUtilRel.setValues(utilPot);
      
            //Add the new relation to the remaining utility relations
            instantUtilRels.insertRelation(newUtilRel);
            }
	}


	/**
	 * It computes the heuristic through sum weighted over all the variables
	 * @param instantProbRels
	 * @param instantUtilRels
	 * @return
	 */
	public static double heuristicSum(RelationList instantProbRels, RelationList instantUtilRels) {
		// TODO Auto-generated method stub
		NodeList vars;
		int length;
		double finalValues[];
		
		vars = instantProbRels.getVariables();
		vars.join(instantUtilRels.getVariables());
		
		//We eliminate all variables through sum using probability and utility potentials
		for(int i=0;i<vars.size();i++){
			eliminateVariableBySum(vars.elementAt(i),instantProbRels,instantUtilRels);
		}
		
		length = instantUtilRels.size();
		finalValues = new double[length];
		
		//We sum the final values of the utility relations
		for (int i=0;i<length;i++){
			finalValues[i]=((PotentialTable)(instantUtilRels.elementAt(i).getValues())).maximumValue();
		}
		return Stat.sum(finalValues);
	
	}
	
	
	/**
	 * @param instantUtilRels
	 * @return The mean value of the global utility. It can be used like a non-admissible heuristic
	 */
	public static double heuristicMeanGlobalUtilityByDP(RelationList instantUtilRels){
		NodeList vars;
		int length;
		double finalValues[];
		double sizesOfUtilRels[];
		Relation globalUtilityRel;
		RelationList globalUtilityListRel;
		double finalSum;
		double sizeGlobalUtility;
		
		globalUtilityRel = DynamicUID.sumUtilityRelations(instantUtilRels);
		
		sizeGlobalUtility = ((PotentialTable)globalUtilityRel.getValues()).getSize();
		
		globalUtilityListRel = new RelationList();
		globalUtilityListRel.insertRelation(globalUtilityRel);
		
		sizesOfUtilRels = new double[instantUtilRels.size()];
		
		vars = globalUtilityListRel.getVariables();
			
		//We eliminate all variables through sum using probability and utility potentials
		for(int i=0;i<vars.size();i++){
			eliminateVariableByDP("Sum",vars.elementAt(i),globalUtilityListRel);
		}
		
		finalSum = ((PotentialTable)(globalUtilityListRel.elementAt(0).getValues())).maximumValue();
		
		return finalSum/sizeGlobalUtility;
	}
	
	/**
	 * It eliminates the variable 'nodeToElim' by sum and updates the sets of probability and utility
	 * potentials 'instantProbRels' and 'instantUtilRels'
	 * @param nodeToElim
	 * @param instantProbRels
	 * @param instantUtilRels
	 */
	private static void eliminateVariableBySum(Node nodeToElim, RelationList instantProbRels, RelationList instantUtilRels) {
		
		// TODO Auto-generated method stub
		
		RelationList probRelsOfElim;
		RelationList utilRelsOfElim;
//		PotentialTable newProbPot = null;
		PotentialTable probPot = null;
	//	Relation newProbRel;
		Relation newUtilRel;
		PotentialTable newProbPot = null;
		Relation newProbRel;
		boolean withProbPot;
		
		
		
		
		probRelsOfElim =	instantProbRels.getRelationsOfAndRemove(nodeToElim);
			
		//Elimination of the variable from the probability potentials	
		if (probRelsOfElim.size()>0){
			withProbPot = true;
			probPot = (PotentialTable) probRelsOfElim.elementAt(0).getValues();
			for(int j=1;j<probRelsOfElim.size();j++){
				probPot = (PotentialTable) probPot.combine(probRelsOfElim.elementAt(j).getValues());
			}
		
		}
		else{
			withProbPot = false;
		}
		
		if (withProbPot){
		//Sum over nodeToElim
		newProbPot = (PotentialTable) probPot.addVariable(nodeToElim);
		
		
        // Create a new relation that is added to probability relations
        newProbRel = new Relation();
        // Set the kind for the final relation 
        newProbRel.setKind(Relation.POTENTIAL);
        newProbRel.getVariables().setNodes((Vector)newProbPot.getVariables().clone());
        newProbRel.setValues(newProbPot);
  
        //Add the new relation to the remaining probability relations
        instantProbRels.insertRelation(newProbRel);
        
		}
            
        //Elimination of the variable from the utility potentials
	
		//Combine the utility potentials
            utilRelsOfElim = instantUtilRels.getRelationsOfAndRemove(nodeToElim);
         
            if (utilRelsOfElim.size()>0){
            
    		PotentialTable utilPot = (PotentialTable)(utilRelsOfElim.elementAt(0).getValues());
			for(int j=1;j<utilRelsOfElim.size();j++){
				utilPot = utilPot.combine((PotentialTable) utilRelsOfElim.elementAt(j).getValues(),new SumFunction());
			}
			if (withProbPot){
			//Multiply the probability and the utility potential
			utilPot = utilPot.combine(probPot);
			}
			//Sum the utility potential over nodeToElim
			utilPot = (PotentialTable) utilPot.addVariable(nodeToElim);
			
		//Division by the probability potential
			if (withProbPot){
			utilPot = (PotentialTable)(utilPot.divide(newProbPot));
			}
			
			//Create a new relation that is added to the utility relations
            newUtilRel = new Relation();
            // Set the kind for the final relation 
            newUtilRel.setKind(Relation.POTENTIAL);
            newUtilRel.getVariables().setNodes((Vector)utilPot.getVariables().clone());
            newUtilRel.setValues(utilPot);
      
            //Add the new relation to the remaining utility relations
            instantUtilRels.insertRelation(newUtilRel);
            
            }
     
            
   }



	//Computes the value of the heuristic with the sum of the maximum for
	//the utility nodes 
	public static double heuristicMaximumLocalUtility(RelationList probRels,RelationList utilRels) {
		double[] maximums;
		int length;
		
		length = utilRels.size();
		
		maximums = new double[length];
		// TODO Auto-generated method stub
		for (int i=0;i<length;i++){
			maximums[i]=((PotentialTable)(utilRels.elementAt(i).getValues())).maximumValue();
		}
		
		return Stat.sum(maximums);
		
		
	}
	
	
//	Computes the value of the heuristic with the maximum of the global utility function 
	public static double heuristicMaximumGlobalUtility(RelationList probRels,RelationList utilRels) {
		Relation globalUtilityRel;
		
		globalUtilityRel = DynamicUID.sumUtilityRelations(utilRels);
		
		return ((PotentialTable)(globalUtilityRel.getValues())).maximumValue();
	}
	@Override
	public double undefValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	//It returns in auxNodes the set of nodes of the partial solution under the graph rooted by this AOUIDNode
	public void auxObtainNodesOfPartialSolution(ArrayList<NodeAOUID> auxNodes) {
		// TODO Auto-generated method stub
		NodeList children;
		
		//Add the node (if it is not included) to the set of nodes
		auxNodes.add(this);
		
		switch(type){
		case BRANCH:
		case DECISION:
			if (bestChildInPartialSolution!=null){
				bestChildInPartialSolution.auxObtainNodesOfPartialSolution(auxNodes);
			}
			break;
		case CHANCE:
			children = getChildrenNodes();
			for(int i=0;i<children.size();i++){
				((NodeAOUID)children.elementAt(i)).auxObtainNodesOfPartialSolution(auxNodes);				
			}
			break;
		}
		
		
	}

//It returns in auxNodes the set of nodes of the partial solution under the graph rooted by this AOUIDNode
	public void auxObtainNodesOfPartialSolutionRandomly(ArrayList<NodeAOUID> auxNodes) {
		// TODO Auto-generated method stub
		NodeList children;
		NodeAOUID auxNode;
		
		//Add the node (if it is not included) to the set of nodes
		auxNodes.add(this);
		
		switch(type){
		case BRANCH:
		case DECISION:
			if (bestChildInPartialSolution!=null){
				if ((this.children!=null)&&(this.children.size()>0)){
				auxNode = selectOneChildRandomlyAccordingToF();
				auxNode.auxObtainNodesOfPartialSolutionRandomly(auxNodes);
				}
			}
			break;
		case CHANCE:
			children = getChildrenNodes();
			for(int i=0;i<children.size();i++){
				((NodeAOUID)children.elementAt(i)).auxObtainNodesOfPartialSolutionRandomly(auxNodes);				
			}
			break;
		}
		
		
	}

	private NodeAOUID selectOneChildRandomlyAccordingToF() {
	// TODO Auto-generated method stub
		double probs[];
		NodeList children;
		int length;
		double sum;
		Random r;
		double base;
		double auxProb;
		NodeAOUID randomChild;
		boolean found;
		double auxRandom;
		
		children = this.getChildrenNodes();
		length = children.size();
		
		probs = new double[length];
		
		//Compute the sum of F of the children
		sum = 0.0;
		for (int i=0;i<length;i++){
			sum = sum+((NodeAOUID)children.elementAt(i)).getF();  
		}
		
		//Compute the probabilities
		for (int i=0;i<length;i++){
			probs[i]=((NodeAOUID)children.elementAt(i)).getF()/sum;  
		}
		
		//Select randomly a child according to the probabilities probs.
		r = new Random();
		auxRandom = r.nextDouble();
		base = 0.0;
		randomChild = (NodeAOUID) children.elementAt(0);
		found = false;
		for (int i=0;(i<length)&&(found==false);i++){
			auxProb = probs[i];
			if ((auxRandom>=base)&&(auxRandom<(base+auxProb))){
				found = true;
				randomChild = (NodeAOUID)children.elementAt(i);
			}
			else{
				base = base + auxProb;
			}
		}
		return randomChild;
}


	//It generates the sucessors of the node to expand
	//Add the new nodes to the aouid and to the list open
	public ArrayList<NodeAOUID> generateSucessors() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> sucessors;
		NodeAOUID newNodeAOUID;
		NodeGSDAG newNodeGSDAG;
		NodeList childrenGSDAG;
		Configuration auxConf;
		FiniteStates nodeUID;
		String newNameOfVariable;
		TypeOfNodeAOUID newType;
		Potential pot=null;
		NodeGSDAG auxNodeGSDAG;
		NodeAOUID existingNodeAOUID;
		
		sucessors = new ArrayList();
		//if (isSolvedNodeAOUID()){
		if (isSolved()){
			
			if (isDebug()) System.out.println("Removing from open the node with instantiations "+instantiations.toString());
			return sucessors;
		}
		else if (isChildOfChanceNodeAOUID()&&hasZeroProbability()){
		
			if (isDebug()) System.out.println("We remove from open the node with instantiatons "+instantiations.toString()+" because it has probability 0");
			return sucessors;
		}
		else{
			
		if (isDebug()){
			System.out.print("* Expanding the node of kind "+type.toString()+", name "+nameOfVariable+" and instantiations ");
			instantiations.print();
			System.out.println("");
		}
		
		
		
		
		switch (type){
		case DECISION:
		case CHANCE:
				//Set the new name of variable and the nodeGSDAG
				newNameOfVariable = nodeGSDAG.nextVariable(nameOfVariable);
				newNodeGSDAG = nextNodeGSDAG();
				newType = getTypeFromGSDAG(newNodeGSDAG.getTypeOfNodeGSDAG());
				nodeUID = (FiniteStates) uid.getNode(nameOfVariable);
				if (type == TypeOfNodeAOUID.CHANCE){
				conditionalProbs = calculateConditionedProbabilities();
				//conditionalProbs = calculateConditionedProbabilitiesSSP();
				
				}
				//System.out.println("Children of the expanded node:");
				//Copy the parent in each child and set the new values for it
				for (int i=0;i<nodeUID.getNumStates();i++){
					existingNodeAOUID = graphUID.improvedGetNodeAOUID(instantiations,nodeUID,i);
					
					if (existingNodeAOUID == null){
//					Copy of the current NodeAOUID in the child
					newNodeAOUID = copy();
					newNodeAOUID.setNameOfVariable(newNameOfVariable);
					newNodeAOUID.setNodeGSDAG(newNodeGSDAG);
					newNodeAOUID.setType(newType);
					//Modify the instantiations adding the new variable with the corresponding value
					newNodeAOUID.instantiations.insert(nodeUID,i);
					
					newNodeAOUID.calculateValueOfHeuristic(this);
					if (isDebug()) System.out.println("Node "+newNodeAOUID.getInstantiations().toString()+ " F:"+newNodeAOUID.getF());
					
					if (type == TypeOfNodeAOUID.CHANCE){
						if (isDebug()) System.out.println("Probability: "+conditionalProbs.getValue(newNodeAOUID.getInstantiations()));
					}
					graphUID.addNode(newNodeAOUID);
					
					newNodeAOUID.setOpen(true);
					newNodeAOUID.setSolved(isSolvedNodeAOUID());
					sucessors.add(newNodeAOUID);
					graphUID.setNumberOfNodes(graphUID.getNumberOfNodes()+1);
					}
					else {
						if (isDebug()) System.out.println("State not added to the graph of search because it exists");
						sucessors.add(existingNodeAOUID);
					}
					
					
				}
				break;
		case BRANCH:
			sucessors = generateSucessorsOfBranch();
		
			break;
				
				
		}		
		return sucessors;
		}
	}
	
	public ArrayList<NodeAOUID> generateSucessorsOfBranch() {
		
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> sucessors;
		NodeAOUID newNodeAOUID;
		NodeGSDAG newNodeGSDAG;
		NodeList childrenGSDAG;
			TypeOfNodeAOUID newType;
	
		
		sucessors = new ArrayList();
		
		
		childrenGSDAG = nodeGSDAG.getChildrenNodes();
		for (int i=0;i<childrenGSDAG.size();i++){
			newNodeGSDAG = (NodeGSDAG) childrenGSDAG.elementAt(i);
			newNodeAOUID = copy();
			newNodeAOUID.setNameOfVariable(newNodeGSDAG.getVariables().get(0));
			newNodeAOUID.setNodeGSDAG(newNodeGSDAG);
			newType = getTypeFromGSDAG(newNodeGSDAG.getTypeOfNodeGSDAG());
			newNodeAOUID.setType(newType);
			//We copy the value of the heuristic of the current node
			newNodeAOUID.setFInChildOfBranch(this);
			//newNodeAOUID.setF(getF());
			graphUID.addNode(newNodeAOUID);
			//graphUID.open.add(newNodeAOUID);
			newNodeAOUID.setOpen(true);
			newNodeAOUID.setSolved(isSolvedNodeAOUID());
			sucessors.add(newNodeAOUID);
			graphUID.setNumberOfNodes(graphUID.getNumberOfNodes()+1);
		}
		
		return sucessors;
	}
	

	protected void setFInChildOfBranch(NodeAOUID branch) {
		// TODO Auto-generated method stub
		setF(branch.getF());
	}


	protected boolean isChildOfChanceNodeAOUID() {
		// TODO Auto-generated method stub
		NodeList parents;
		
		parents = getParentNodes();
		
		if ((parents==null)||(parents.size()==0)) return false;
		else return (((NodeAOUID)parents.elementAt(0)).type==TypeOfNodeAOUID.CHANCE);
	}


	//It returns true if the nodesAOUID is a terminal node in the tree, so it's solved
	//and can`t be expanded (it must be deleted from open)
	protected boolean isSolvedNodeAOUID() {
		// TODO Auto-generated method stub
		boolean completeConf=true;
		NodeList nodes;
		Node auxNode;
		int kind;
		int numChanceAndDecisions;
		NodeList chanceNodes;
		NodeList decisionNodes;
		
		chanceNodes = uid.getNodesOfKind(Node.CHANCE);
		decisionNodes = uid.getNodesOfKind(Node.DECISION);
		numChanceAndDecisions = chanceNodes.size()+decisionNodes.size();

		//If both list have the same number of elements is because all the variables are instantiated
		completeConf = (numChanceAndDecisions==instantiations.size());
		return completeConf;
	}

	/**
	 * It returns true iff all the paths that arrive to the node have probability zero.
	 * @return
	 */
	protected boolean hasZeroProbability(){
		NodeList parents;
		double maxProb = Double.NEGATIVE_INFINITY;
		double auxProb;
		
		parents = this.getParentNodes();
		//We calculate the maximum of the probabilities of the incoming links to the node
		for (int i=0;i<parents.size();i++){
			auxProb = ((NodeAOUID)parents.elementAt(i)).conditionalProbs.getValue(getInstantiations());
			if (auxProb>maxProb){
				maxProb = auxProb;
			}
		}
		
		return (maxProb==0.0);
			
	}

	/**
	 * @return
	 */
	protected Potential calculateConditionedProbabilities() {
		// TODO Auto-generated method stub
		FiniteStates varNode;
		RelationList instantiatedProbRels;
		NodeGSDAG last;
		RelationList minimalSetOfProbRels;
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();
		
		varNode = (FiniteStates) uid.getNode(nameOfVariable);
		
		minimalSetOfProbRels = obtainMinimalSetOfProbabilityRelations(varNode,last.getCurrentProbabilityRelations());
		
		instantiatedProbRels = instantiateRelations(minimalSetOfProbRels);
		
		//return NodeAOUID.calculateConditionedProbabilities(varNode,instantiatedProbRels);
		return NodeAOUID.calculateConditionedProbabilitiesFromMinimalRelations(varNode,instantiatedProbRels);
		//return NodeAOUID.calculateConditionedProbabilities(varNode,last.getProbabilityRelations());
		
	}
	
	
	protected Potential[] calculateConditionedProbabilitiesAndHeurEvenDistrib(
			boolean calculateConditionalProbs) {
		// TODO Auto-generated method stub
		
		
		// TODO Auto-generated method stub
		FiniteStates varNode;
		RelationList instantProbRels;
		RelationList instantUtilRels;
		
		NodeGSDAG last;
		RelationList minimalSetOfProbRels;
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();
		
		varNode = (FiniteStates) uid.getNode(nameOfVariable);
		
		instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
		instantProbRels = instantiateRelations(last.getCurrentProbabilityRelations());
		
		//return NodeAOUID.calculateConditionedProbabilities(varNode,instantiatedProbRels);
		return calculateConditionedProbabilitiesAndHeurEvenDistrib(varNode,instantUtilRels,instantProbRels,true);
		//return NodeAOUID.calculateConditionedProbabilities(varNode,last.getProbabilityRelations());
	}
	
	/**
	 * @param instantProbRels 
	 * @param instantUtilRels 
	 * @param varNode 
	 * @param conditionalProbs Output indicating the conditional probabilities
	 * @param calculateCondProbs 
	 */
	private Potential[] calculateConditionedProbabilitiesAndHeurEvenDistrib(
			FiniteStates varNode, RelationList instantUtilRels, RelationList instantProbRels, boolean calculateCondProbs) {
		// TODO Auto-generated method stub
		
		// TODO Auto-generated method stub
		NodeList vars;
		int length;
		double finalValues[];
		FiniteStates var;
		Relation newDecRel;
		PotentialTable newDecPot;
		NodeList auxNodes;
		Potential pot;
		Potential finalProbPot = null;
		Potential finalUtilPot;
		
		vars = instantProbRels.getVariables();
		vars.join(instantUtilRels.getVariables());
		
		//We eliminate all variables through sum using probability and utility potentials
		
		vars = removeNonFiniteStatesVariables(vars);
		
		vars.removeNode(varNode);
		
		while (vars.size()>0){
			
			//var = (FiniteStates)vars.elementAt(i);
			var = extractAVariableToBeEliminated(vars,instantProbRels,instantUtilRels);
			
			//We add a uniform distribution for the decision node
			if (var.getKindOfNode()==Node.DECISION){
				//We create a uniform distribution for the decision and we add it to the set of probability relations
				//Create a new relation that is added to the utility relations
	            newDecRel = new Relation();
	            // Set the kind for the final relation 
	            newDecRel.setKind(Relation.POTENTIAL);
	            auxNodes = new NodeList();
	            auxNodes.insertNode(var);
	            newDecRel.setVariables(auxNodes);
	            newDecPot = new PotentialTable(var);
	            newDecPot.setValue(1.0/var.getNumStates());
	            newDecRel.setValues(newDecPot);
	            instantProbRels.insertRelation(newDecRel);
			}
			
			//We sum-marginalize the variable
			eliminateVariableBySum(var,instantProbRels,instantUtilRels);
			
		}
		
		if (calculateCondProbs){
		//We combine the probability relations, which only depend on the varNode and we normalize them
		pot = instantProbRels.elementAt(0).getValues();
		for(int j=1;j<instantProbRels.size();j++){
			pot = pot.combine(instantProbRels.elementAt(j).getValues());
		}
		finalProbPot = pot;
		finalProbPot.normalize();
		}
		
		//We sum the final values of the utility relations
		length = instantUtilRels.size();
		PotentialTable utilPot = (PotentialTable)(instantUtilRels.elementAt(0).getValues());
		for(int j=1;j<length;j++){
			utilPot = utilPot.combine((PotentialTable) instantUtilRels.elementAt(j).getValues(),new SumFunction());
		}
		
		//Outputs
		Potential[] condProbsAndHeur = new Potential[2];
		if (calculateCondProbs){
			condProbsAndHeur[0] = finalProbPot;
		}
		else{
			condProbsAndHeur[0]=null;
		}
		condProbsAndHeur[1] = utilPot;
		
		return condProbsAndHeur;
	}


	/**
	 * @return
	 */
	protected Potential calculateConditionedProbabilitiesSSP() {
		// TODO Auto-generated method stub
		FiniteStates varNodeInBNOfJoinTree;
		ShenoyShaferPropagation ssp;
		RelationList instantiatedProbRels;
		NodeGSDAG last;
		RelationList minimalSetOfProbRels;
		NodeList interest;
		Evidence ev;
		
	//	last = getGraphUID().getGsdag().getLastNodeGSDAG();
		
			
		ssp = graphUID.getSSP();
		
		varNodeInBNOfJoinTree = (FiniteStates) ssp.network.getNode(nameOfVariable);
		

		
		//Clean the previous evidence of the join tree
		quitPreviousInstantiationsOutOf(ssp);
		
		//Incorporate evidence
		incorporateInstantiationsIn(ssp);
		
		interest = new NodeList();
		interest.insertNode(varNodeInBNOfJoinTree);
		
		ssp.setInterest(interest);
		
				
		//Define the interest variable and Propagate
	    ssp.iterativePropagation(ssp.getJoinTree().elementAt(0),false);
	    
	    try {
			ssp.saveResults("propMIDDLE.res");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    
	    //Obtain the marginal of the variable of interest
	    return (PotentialTree)(ssp.results.get((Integer) (ssp.getPositions().get(varNodeInBNOfJoinTree))));
		
		
	}
	/**
	 * It incorporates the instantiations of the NodeAOUID as evidence to the join tree
	 * @param ssp
	 */
	private void incorporateInstantiationsIn(ShenoyShaferPropagation ssp) {
		// TODO Auto-generated method stub
		
		for (int i=0;i<instantiations.size();i++){
			ssp.addEvidenceItem((FiniteStates) ssp.network.getNode(instantiations.getVariable(i).getName()),instantiations.getValue(i));
		}
	
	}


	/**
	 * It quits the existing evidence out of the join tree
	 * @param ssp
	 */
	private void quitPreviousInstantiationsOutOf(ShenoyShaferPropagation ssp) {
		// TODO Auto-generated method stub
		Evidence observations;
		FiniteStates auxNode;
		
		observations = ssp.observations;
		for (int i=0;i<observations.size();i++){
			auxNode = observations.getVariable(i);
			ssp.retractEvidenceItem(auxNode);
		}
	}


	/**
	 * It instantiate a list of relations with the new configuration
	 * It uses the atrribute 'instantiations'
	 * The relations of the parameter 'relations' are changed if 'nodeVar' appears in them
	 * @param relations A list of the relations after the instantiation
	 */
	protected RelationList instantiateRelations(RelationList relations) {
		// TODO Auto-generated method stub
	
		Relation auxNewRel;
		RelationList newRelations;
		Relation auxRel;
		
		ArrayList<Relation> auxRelations;
		
		newRelations = new RelationList();
		
		
			
		//Restrict the relations where the new variable appears
		//The relations where it doesn't appear don't change,
		//but the rest do.
		for (int i=0;i<relations.size();i++){
			auxRel = relations.elementAt(i);
				//Add the new relation restricted to the new configuration of variables
				auxNewRel = auxRel.copy();
				auxNewRel = auxNewRel.restrict(instantiations);
				newRelations.insertRelation(auxNewRel);
			
		}
		
		return newRelations;
		
		
	}

	
	/**
	 * It instantiate a list of relations with the new configuration
	 * The relations of the parameter 'relations' are changed if 'nodeVar' appears in them
	 * @param relations A list of the relations after the instantiation
	 */
	protected static RelationList instantiateRelations(RelationList relations,Configuration conf) {
		// TODO Auto-generated method stub
	
		Relation auxNewRel;
		RelationList newRelations;
		Relation auxRel;
		
		ArrayList<Relation> auxRelations;
		
		newRelations = new RelationList();
		
		
			
		//Restrict the relations where the new variable appears
		//The relations where it doesn't appear don't change,
		//but the rest do.
		for (int i=0;i<relations.size();i++){
			auxRel = relations.elementAt(i);
				//Add the new relation restricted to the new configuration of variables
				auxNewRel = auxRel.copy();
				auxNewRel = auxNewRel.restrict(conf);
				newRelations.insertRelation(auxNewRel);
			
		}
		
		return newRelations;
		
		
	}

	
	
	protected Relation instantiateRelation(Relation rel){
		Relation auxNewRel;
		
		auxNewRel = rel.copy();
		auxNewRel = auxNewRel.restrict(instantiations);
		
		return auxNewRel;
	}

	/**
	 * It calculates the conditioned probabilities with the variable elimination algorithm
	 * @param node
	 * @param probRels
	 * @return
	 */
	public static Potential calculateConditionedProbabilitiesVE(FiniteStates node,RelationList probRels){
		RelationList relsOfElim;
		NodeList varsToElim;
		RelationList auxRelsOfElim;
		Potential finalPot;
		Relation finalRel;
		Potential pot;
		
		//relsOfElim = probRels.getRelationsOf(node);
		//We need all the probabilistic relations to calculate the conditioned probabilities.
		relsOfElim = new RelationList();
		for (int i=0;i<probRels.size();i++){
			relsOfElim.insertRelation(probRels.elementAt(i));
		}
		
		
		
		
		//relsOfElim = probRels.copy();
		varsToElim = relsOfElim.getVariables();
		varsToElim.removeNode(node);
		
		//We eliminate the rest of variables of the relations where varNode apeears
		for(int i=0;i<varsToElim.size();i++){
			Node nodeToElim = varsToElim.elementAt(i); 
			auxRelsOfElim =	relsOfElim.getRelationsOfAndRemove(nodeToElim);
			
			//We eliminate the variable nodeToElim of the relations where it appears			
			pot = auxRelsOfElim.elementAt(0).getValues();
			for(int j=1;j<auxRelsOfElim.size();j++){
				pot = pot.combine(auxRelsOfElim.elementAt(j).getValues());
			}
			//Sum over nodeToElim
			pot = pot.addVariable(nodeToElim);
			
	        // Create a new relation to store the results of the elimination of nodeToElim
            Relation newRel = new Relation();
            // Set the kind for the final relation 
            newRel.setKind(Relation.POTENTIAL);
            newRel.getVariables().setNodes((Vector)pot.getVariables().clone());
            newRel.setValues(pot);
      
            //Add the new relation to the remaing relations
            relsOfElim.insertRelation(newRel);
		}
		
		//We combine the rest of relations, which only depend on node
		//and we normalize them		
		pot = relsOfElim.elementAt(0).getValues();
		for(int j=1;j<relsOfElim.size();j++){
			pot = pot.combine(relsOfElim.elementAt(j).getValues());
		}
		finalPot = pot;
		finalPot.normalize();
	
		return finalPot;
	}


	/**
	 * It calculates the joint probability distribution of a set of variables
	 * @param node
	 * @param probRels
	 * @return
	 */
	/*public static Potential calculateJointProbabilityDistributionVE(NodeList variables,RelationList probRels){
		RelationList relsOfElim;
		NodeList varsToElim;
		RelationList auxRelsOfElim;
		Potential finalPot;
		Relation finalRel;
		Potential pot;
		
		//relsOfElim = probRels.getRelationsOf(node);
		//We need all the probabilistic relations to calculate the conditioned probabilities.
		relsOfElim = new RelationList();
		for (int i=0;i<probRels.size();i++){
			relsOfElim.insertRelation(probRels.elementAt(i));
		}
		
		
		
		
		//relsOfElim = probRels.copy();
		varsToElim = relsOfElim.getVariables();
		varsToElim.removeNode(node);
		
		//We eliminate the rest of variables of the relations where varNode apeears
		for(int i=0;i<varsToElim.size();i++){
			Node nodeToElim = varsToElim.elementAt(i); 
			auxRelsOfElim =	relsOfElim.getRelationsOfAndRemove(nodeToElim);
			
			//We eliminate the variable nodeToElim of the relations where it appears			
			pot = auxRelsOfElim.elementAt(0).getValues();
			for(int j=1;j<auxRelsOfElim.size();j++){
				pot = pot.combine(auxRelsOfElim.elementAt(j).getValues());
			}
			//Sum over nodeToElim
			pot = pot.addVariable(nodeToElim);
			
	        // Create a new relation to store the results of the elimination of nodeToElim
            Relation newRel = new Relation();
            // Set the kind for the final relation 
            newRel.setKind(Relation.POTENTIAL);
            newRel.getVariables().setNodes((Vector)pot.getVariables().clone());
            newRel.setValues(pot);
      
            //Add the new relation to the remaing relations
            relsOfElim.insertRelation(newRel);
		}
		
		//We combine the rest of relations, which only depend on node
		//and we normalize them		
		pot = relsOfElim.elementAt(0).getValues();
		for(int j=1;j<relsOfElim.size();j++){
			pot = pot.combine(relsOfElim.elementAt(j).getValues());
		}
		finalPot = pot;
		finalPot.normalize();
	
		return finalPot;
	}
*/
	
	/**
	 * It calculates the conditioned probabilities with the variable elimination algorithm, but selecting only the probability potentials 
	 * that are necessary to calculate the conditional probabilities
	 * @param node
	 * @param probRels
	 * @return
	 */
	public static Potential calculateConditionedProbabilities(FiniteStates node,RelationList probRels){
	
		RelationList minimalSetOfProbRels;
			
		minimalSetOfProbRels = obtainMinimalSetOfProbabilityRelations(node,probRels);
		
		return NodeAOUID.calculateConditionedProbabilitiesVE(node,minimalSetOfProbRels);
		
	
	}
	
	/**
	 * It calculates the conditioned probabilities with the variable elimination algorithm, but selecting only the probability potentials 
	 * that are necessary to calculate the conditional probabilities
	 * @param node
	 * @param probRels
	 * @return
	 */
	public static Potential calculateConditionedProbabilitiesFromMinimalRelations(FiniteStates node,RelationList minimalSetOfProbRels){
	
			
		return NodeAOUID.calculateConditionedProbabilitiesVE(node,minimalSetOfProbRels);
		
	
	}
	
	
	public static RelationList obtainMinimalSetOfProbabilityRelations(FiniteStates node, RelationList probRels) {
		// TODO Auto-generated method stub
		RelationList minimalSetOfProbRels;
		RelationList auxProbRels;
		RelationList auxRelsOfElim;
		NodeList varsToElim;
		NodeList newVarsToElim;
		
		//Copy the relations in auxProbRels
		auxProbRels = new RelationList();
		for (int i=0;i<probRels.size();i++){
			auxProbRels.insertRelation(probRels.elementAt(i));
		}
		
		minimalSetOfProbRels = new RelationList();
		
		//We start taking the relations of node
		varsToElim = new NodeList();
		varsToElim.insertNode(node);
		
		while (varsToElim.size()>0){
			newVarsToElim = new NodeList();
			for (int i=0;i<varsToElim.size();i++){
				Node nodeToElim = varsToElim.elementAt(i); 
				auxRelsOfElim =	auxProbRels.getRelationsOfAndRemove(nodeToElim);
				if ((auxRelsOfElim!=null)&&(auxRelsOfElim.size()>0)){
				if (auxRelsOfElim.getVariables().size()>0){
				newVarsToElim.join(auxRelsOfElim.getVariables());
				}
				for (int j=0;j<auxRelsOfElim.size();j++){
					//We add the relations of the variables that appear in the relations already added
					minimalSetOfProbRels.insertRelation(auxRelsOfElim.elementAt(j));
				}
				}
			}
			varsToElim = newVarsToElim;
		}
		return minimalSetOfProbRels;
	}


	/*protected void instantiateRelations(FiniteStates nodeUID) {
		// TODO Auto-generated method stub
		probabilityRelations = instantiateRelations(probabilityRelations,nodeUID);
		utilityRelations = instantiateRelations(utilityRelations,nodeUID);
		
	}*/

	/**
	 * It instantiates a list of relations with the new configuration
	 * It uses the atrribute 'instantiations'
	 * The relations of the parameter 'relations' are changed if 'nodeVar' appears in them
	 * If nodeVar is null then all the relations are instantiated 
	 * @param relations A list of the relations after the instantiation
	 */
	protected RelationList instantiateRelations(RelationList relations,FiniteStates nodeVar) {
		// TODO Auto-generated method stub
	
		Relation auxNewRel;
		RelationList newRelations;
		Relation auxRel;
		
		ArrayList<Relation> auxRelations;
		
		newRelations = new RelationList();
		
		
			
		//Restrict the relations where the new variable appears
		//The relations where it doesn't appear don't change,
		//but the rest do.
		for (int i=0;i<relations.size();i++){
			auxRel = relations.elementAt(i);
			if (auxRel.isInRelation(nodeVar)){
				//Add the new relation restricted to the new configuration of variables
				auxNewRel = auxRel.copy();
				auxNewRel = auxNewRel.restrict(instantiations);
				newRelations.insertRelation(auxNewRel);
			}
			else{
				newRelations.insertRelation(auxRel);
			}
		}
		
		return newRelations;
		
		
	}

	public void setType(TypeOfNodeAOUID newType) {
		// TODO Auto-generated method stub
		type = newType;
		
	}

	public void setNodeGSDAG(NodeGSDAG newNodeGSDAG) {
		// TODO Auto-generated method stub
		nodeGSDAG = newNodeGSDAG;
		
	}

	/**
	 * It returns the next node in the GSDAG according to the current variable of NodeAOUID
	 * It may be the same NodeGSDAG. This method is invoked when the type of NodeAOUID
	 * is CHANCE or DECISION, which ensures that there's only a sucessor in the GSDAG
	 * @return
	 */
	protected NodeGSDAG nextNodeGSDAG() {
		// TODO Auto-generated method stub
		int indexOfVar;
		ArrayList<String> vars;
		int newIndex;
		NodeGSDAG nextNode;
		
		if (type == TypeOfNodeAOUID.BRANCH){//The method shouldn't be invoked for branches
				nextNode = null;
				System.out.println("Error. The method nextNodeGSDAG shouldn't be invoked for branches");
		}
		else{
			vars = nodeGSDAG.getVariables();
			indexOfVar = vars.indexOf(nameOfVariable);
			newIndex = indexOfVar+1;
			
			if (newIndex < vars.size()){//There's more variables to process in the same NodeGSDAG
				nextNode = nodeGSDAG;
			}
			else{//All variables in the NodeGSDAG have been processed, so we have to process the sucessor
				nextNode = (NodeGSDAG)nodeGSDAG.getChildrenNodes().elementAt(0);
				
			}
		}
		return nextNode;
		
	}

	

		


	/*public void setProbability(double probability) {
		this.probability = probability;
	}*/

	public double getF() {
		return f;
	}

	public void setF(double f) {
		this.f = f;
	}

	/**
	 * Method that update the necessary heuristics in node and its parents
	 * if it is necessary.
	 * The node can use an only heuristic for select the node and prune.
	 * Or it can use different heuristics.
	 * The class NodeAOUID uses an heuristic f.
	 * See how the class NodeAOUID_Anytime uses two.
	 */
	public void updateHeuristicInNode(){
		updateHeuristicFInNode();
	}
	
	//It updates the value of F of this node and of the ancestors if it's necessary
	//It is invoked when we use an only heuristic (admissible) for both tasks:
	// (a) Select the node to expand
	// (b) Prune the branches that do not lead to an optimal solution
	protected void updateHeuristicFInNode() {
		// TODO Auto-generated method stub
		
		double newF;
		NodeList childrenNodes;
		NodeList parentNodes;
		NodeAOUID auxChild;
		double auxFChild;
		ArrayList<NodeAOUID> newBestChild = null; 
		
		
		childrenNodes = this.getChildrenNodes();
		switch (type){
		case BRANCH:
		case DECISION:
			//Maximize over the children
			newF = Double.NEGATIVE_INFINITY;
			
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID)childrenNodes.elementAt(i);
				auxFChild = auxChild.getF();
				if (auxFChild>newF){//We improve newF
					newBestChild = new ArrayList();
					newBestChild.add(auxChild);
					newF = auxFChild;
				}
				else if (auxFChild==newF){//We have a tie in newF
					newBestChild.add(auxChild);
				}
			}
			//Update of bestChildInPartialSolution
			//No sé por qué falla la randomización
			
			bestChildInPartialSolution = (((newBestChild!=null)&&(newBestChild.size()>0))?((NodeAOUID) selectChildRandomlyWhenTie(newBestChild)):null);
			//bestChildInPartialSolution = (((newBestChild!=null)&&(newBestChild.size()>0))?(newBestChild.get(0)):null);
			//If f is updated we have to notify it to the parents
			if (newF!=f){
				f = newF;
				System.out.println("The value of F in the node "+this.getInstantiations().toString()+ " has changed to:"+this.f);
				//updateHeuristicInParents();
			}
			//We can update the heuristic in parents always to select other paths when we have tie
			updateHeuristicInParents();
			break;
		case CHANCE:
			//Weighted sum over the children
			newF = 0;
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID)childrenNodes.elementAt(i);
				newF = newF + auxChild.getF()*conditionalProbs.getValue(auxChild.getInstantiations());
			}
			//Update of F
			if (newF!=f){
				f = newF;
				System.out.println("The value of F in the node "+this.getInstantiations().toString()+ " has changed to:"+this.f);
				//updateHeuristicInParents();
			}
//			We can update the heuristic in parents always to select other paths when we have tie
			updateHeuristicInParents();
			break;
		}
		
		
	}

	/**
	 * @param newBestChild
	 * @return A nodeAOUID randomly selected from a list of nodeAUIDs
	 */
	protected static Node selectRandomlyANode(NodeList newBestChild) {
		// TODO Auto-generated method stub
		Random r=new Random();
		if (newBestChild.size()>1){
			if (isDebug()) System.out.println("Breaking randomly the tie between "+newBestChild.size()+" children in a BRANCH/DECISION");
		}
		return (GeneratorUIDs.chooseRandom(newBestChild,1,r).elementAt(0));
		
		
	}

	
	/**
	 * @param newBestChild
	 * @return A nodeAOUID randomly selected from a list of nodeAUIDs
	 */
	public static Node selectChildRandomlyWhenTie(ArrayList<NodeAOUID> newBestChild) {
		// TODO Auto-generated method stub
		NodeList nodes;
		
		nodes = new NodeList();
		
		for (NodeAOUID auxNode:newBestChild){
			nodes.insertNode(auxNode);
		}
		return selectRandomlyANode(nodes);		
		
	}

	//It sends the message updateHeuristicInNode to the parents in order
	//they update the value of the heuristic
	protected void updateHeuristicInParents() {
		// TODO Auto-generated method stub
		NodeList parentNodes;
		NodeAOUID auxParent;
		
		parentNodes = this.getParentNodes();
		for (int i=0;i<parentNodes.size();i++){
			auxParent = (NodeAOUID)parentNodes.elementAt(i);
			auxParent.updateHeuristicInNode();
		}
		
		
		
	}

	/*public double getProbability() {
		return probability;
	}*/


	public Configuration getInstantiations() {
		return instantiations;
	}


	public void setInstantiations(Configuration instantiations) {
		this.instantiations = instantiations;
	}


	public String getNameOfVariable() {
		return nameOfVariable;
	}


	public void setNameOfVariable(String nameOfVariable) {
		this.nameOfVariable = nameOfVariable;
	}


	@Override
	public boolean equals(Object n) {
		// TODO Auto-generated method stub
		return (this == n);
	}


	public GraphAOUID getGraphUID() {
		return graphUID;
	}


	public void setGraphUID(GraphAOUID graphUID) {
		this.graphUID = graphUID;
	}


	/**
	 * @return The depth of the subtree rooted by this nodeaouid
	 */
	public int getDepth() {
		int depth;
		int depthChildren[];
		// TODO Auto-generated method stub
		if ((children==null)||children.size()==0){
			depth = 1;
		}
		else{
			depthChildren = new int[children.size()];
			for (int i=0;i<children.size();i++){
				depthChildren[i]=((NodeAOUID)children.elementAt(i).getHead()).getDepth();
			}
			depth = Stat.max(depthChildren)+1;
		}
		return depth;
	}


	public int getNumNodesInTheTree() {
		// TODO Auto-generated method stub
		NodeList children;
		int numNodes;
		NodeAOUID auxChild;
		
		
		children = this.getChildrenNodes();
		numNodes = 1;
		for (int i=0;i<children.size();i++){
			auxChild = (NodeAOUID) children.elementAt(i);
			if (auxChild.getParentNodes().elementAt(0)==this){
					//Only the first parent of the nodesAOUID with several parents will
					//count the nodes of the subtree of the child
					numNodes = numNodes+auxChild.getNumNodesInTheTree();
				
			}
		}
		return numNodes;
	}


	public double getEUOfCurrentStrategy() {
		// TODO Auto-generated method stub
		
		NodeList childrenNodes;
		NodeAOUID auxChild;
		double eu = 0;
		
		
		childrenNodes = this.getChildrenNodes();
		if (childrenNodes.size()==0){
			eu = getEUOfCurrentStrategyForLeaves();
		}
		else{
		switch (type){
		case BRANCH:
		case DECISION:
			eu = bestChildInPartialSolution.getEUOfCurrentStrategy();
			break;
		case CHANCE:
			//Weighted sum over the children
			eu = 0;
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID)childrenNodes.elementAt(i);
				eu = eu + auxChild.getEUOfCurrentStrategy()*conditionalProbs.getValue(auxChild.getInstantiations());
			}
			break;
		}
		}
		return eu;
		
		
	}

	
	protected double getEUOfCurrentStrategyForLeaves() {
		// TODO Auto-generated method stub
		NodeGSDAG last;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		double eu;
		
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();
		instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
		instantProbRels = instantiateRelations(last.getCurrentUtilityRelations());
		
		eu=getEU(instantProbRels,instantUtilRels);
		
		return eu;
	}
	
	/**
	 * @param instantProbRels
	 * @param instantUtilRels
	 * @return The expected utility for the final part of the diagram replacing the decision
	 * nodes by chance nodes with uniform distribution
	 */
	protected static double getEU(RelationList instantProbRels, RelationList instantUtilRels) {
		return heuristicEvenDistributionForDecisions(instantProbRels,instantUtilRels);
		
		
		
		}


	public boolean isOpen() {
		return open;
	}


	public void setOpen(boolean open) {
		this.open = open;
	}


	/**
	 * @param fullConf
	 * @return A nodeAOUID whose configuration is equivalent to fullConf
	 */
	public NodeAOUID improvedGetNodeAOUID(Configuration fullConf) {
		// TODO Auto-generated method stub
		NodeAOUID auxNode;
		boolean found;
		NodeAOUID foundNode=null;
		int valueOfVariable;
		
		
		if (instantiations.size()==fullConf.size()){
//			If we reach this point is because 'instantatiations' and 'fullConf' are equivalent
			foundNode = this;
		}
		else{
			//The size of instantiations is lower than the size of fullConf.
			
			switch (type){
			case BRANCH:
				found = false;
				for (int i=0;(i<children.size())&&(found==false);i++){
					//We look for in all the children of the branch until we found
					//something different of null
					auxNode = ((NodeAOUID)(children.elementAt(i).getHead())).improvedGetNodeAOUID(fullConf);
					if (auxNode!=null){
						foundNode = auxNode;
						found = true;
					}
				}
				break;
			case CHANCE:
			case DECISION:
				if (children.size()>0){
					//We continue looking for if the node has children
					valueOfVariable=fullConf.getValue(nameOfVariable);
					if (valueOfVariable==-1){
						// The configuration doesn't contain the variable
						foundNode=null;
					}
					else{
						// We continue the search through the corresponding child
						foundNode=((NodeAOUID)(children.elementAt(valueOfVariable).getHead())).improvedGetNodeAOUID(fullConf);
					}
				}
				else{
					// If the node is a leaf then we haven't found the node that
					// we were
					// looking for
					foundNode=null;
				}
				break;
		}
		
	}
		return foundNode;
	}


	/*public NodeAOUID obtainAnOpenNodeOfPartialSolution() {
				
		// TODO Auto-generated method stub
		NodeList children;
		NodeAOUID foundNode=null;
		boolean found;
		NodeAOUID auxNode;
		
		if (this.isSolved() == false) {
			if (this.children.size() == 0) {
				if (this.isOpen()) {
					foundNode = this;
				} else {
					foundNode = null;
				}
			}
			else{
			switch (type) {
			case BRANCH:
			case DECISION:
				if (bestChildInPartialSolution != null) {
					foundNode = bestChildInPartialSolution
							.obtainAnOpenNodeOfPartialSolution();
				}
				break;
			case CHANCE:
				children = getChildrenNodes();
				found = false;
				for (int i = 0; (i < children.size()) && (found == false); i++) {
					auxNode = ((NodeAOUID) children.elementAt(i))
							.obtainAnOpenNodeOfPartialSolution();
					if (auxNode != null) {
						found = true;
						foundNode = auxNode;
					}
				}
				break;
			}
			}
		}
		
		return foundNode;
	}
*/

	public boolean isSolved() {
		return solved;
	}


	public void setSolved(boolean solved) {
		this.solved = solved;
	}


	public boolean hasAllChildrenSolved() {
		// TODO Auto-generated method stub
		boolean allSolved;
		
		allSolved = true;
		for (int i=0;(i<children.size())&&allSolved;i++){
			if (((NodeAOUID)children.elementAt(i).getHead()).isSolved()==false){
				allSolved=false;
			}
		}
		
		return allSolved;
	}


	/**
	 * @return The probability of a configuration in the current hypergraph solution
	 * Configurations not in the solution has probability 0
	 */
	public Double getProbability() {
		// TODO Auto-generated method stub
		NodeList parents;
		NodeAOUID auxParent;
		double prob;
		double auxProb=0.0;
		
		parents = this.getParentNodes();
		if ((parents==null)||(parents.size()==0)){
			prob= 1.0;
		}
		else{
			//CORREGIR ESTO: PUEDE QUE POR UN CAMINO LA PROBABILIDAD
			//SEA CERO PERO POR OTRO NO. HAY QUE SUMAR LA PROBABILIDAD
			//QUE SE OBTIENE POR TODOS LOS CAMINOS PARENTALES
			prob = 0.0;
			//Sum the probability through the different parents
			for (int i = 0; i < parents.size(); i++) {

				auxParent = (NodeAOUID) parents.elementAt(i);
				switch (auxParent.type) {
				case BRANCH:
				case DECISION:
					if (auxParent.bestChildInPartialSolution == this) {
						auxProb = auxParent.getProbability();
					} else {
						auxProb = 0.0;
					}
					break;
				case CHANCE:
					auxProb = auxParent.conditionalProbs
							.getValue(getInstantiations())
							* auxParent.getProbability();
					break;
				}
				prob = prob + auxProb;
			}
			
		}
		return prob;
	}


	public TypeOfNodeAOUID getTypeOfNodeAOUID() {
		return type;
	}


	public Potential getConditionalProbs() {
		return conditionalProbs;
	}


	public void setConditionalProbs(Potential conditionalProbs) {
		this.conditionalProbs = conditionalProbs;
	}


	public UID getUid() {
		return uid;
	}


	public void setUid(UID uid) {
		this.uid = uid;
	}


	public NodeAOUID getBestChildInPartialSolution() {
		return bestChildInPartialSolution;
	}


	public void setBestChildInPartialSolution(NodeAOUID bestChildInPartialSolution) {
		this.bestChildInPartialSolution = bestChildInPartialSolution;
	}


	public NodeGSDAG getNodeGSDAG() {
		return nodeGSDAG;
	}

	

/***** DIFFERENT HEURISTICS *******/
/***** All the heuristics here presented assume that the relations have been previously instantiated ***/
	
	/**
	 * @param instantUtilRels
	 * @return The value of the heuristic by maximazing the utilities
	 */
	public static double heuristicMaximumGlobalUtilityByDP(RelationList instantUtilRels){
		NodeList vars;
		int length;
		double finalValues[];
		
		vars = instantUtilRels.getVariables();
			
		//We eliminate all variables through sum using probability and utility potentials
		for(int i=0;i<vars.size();i++){
			eliminateVariableByDP("Max",vars.elementAt(i),instantUtilRels);
		}
		
		length = instantUtilRels.size();
		finalValues = new double[length];
		
		//We sum the final values of the utility relations
		for (int i=0;i<length;i++){
			finalValues[i]=((PotentialTable)(instantUtilRels.elementAt(i).getValues())).maximumValue();
		}
		return Stat.sum(finalValues);
	}

	
/*	*//**
	 * @param instantProbRels
	 * @param instantUtilRels
	 * @return The value of the heuristic computed replacing decisions by chance nodes with even distribution
	 *  
	 *//*
	protected static double heuristicEvenDistributionForDecisions(RelationList instantProbRels, RelationList instantUtilRels){
		// TODO Auto-generated method stub
		NodeList vars;
		int length;
		double finalValues[];
		FiniteStates var;
		Relation newDecRel;
		PotentialTable newDecPot;
		NodeList auxNodes;
		
		vars = instantProbRels.getVariables();
		vars.join(instantUtilRels.getVariables());
		
		//We eliminate all variables through sum using probability and utility potentials
		for(int i=0;i<vars.size();i++){
			if (vars.elementAt(i).getClass()==FiniteStates.class){
			var = (FiniteStates)vars.elementAt(i);
			//We add a uniform distribution for the decision node
			if (var.getKindOfNode()==Node.DECISION){
				//We create a uniform distribution for the decision and we add it to the set of probability relations
				//Create a new relation that is added to the utility relations
	            newDecRel = new Relation();
	            // Set the kind for the final relation 
	            newDecRel.setKind(Relation.POTENTIAL);
	            auxNodes = new NodeList();
	            auxNodes.insertNode(var);
	            newDecRel.setVariables(auxNodes);
	            newDecPot = new PotentialTable(var);
	            newDecPot.setValue(1.0/var.getNumStates());
	            newDecRel.setValues(newDecPot);
	            instantProbRels.insertRelation(newDecRel);
			}
			
			//We sum-marginalize the variable
			eliminateVariableBySum(vars.elementAt(i),instantProbRels,instantUtilRels);
			}
		}
		
		length = instantUtilRels.size();
		finalValues = new double[length];
		
		//We sum the final values of the utility relations
		for (int i=0;i<length;i++){
			finalValues[i]=((PotentialTable)(instantUtilRels.elementAt(i).getValues())).maximumValue();
		}
		return Stat.sum(finalValues);
		//return 0.0;
	}
*/
	
	
	/**
	 * @param instantProbRels
	 * @param instantUtilRels
	 * @return The value of the heuristic computed replacing decisions by chance nodes with even distribution
	 * The variable elimination sequence is improved using a minimum fillin heuristic
	 */
	protected static double heuristicEvenDistributionForDecisions(RelationList instantProbRels, RelationList instantUtilRels){
	// TODO Auto-generated method stub
		NodeList vars;
		int length;
		double finalValues[];
		FiniteStates var;
		Relation newDecRel;
		PotentialTable newDecPot;
		NodeList auxNodes;
		
		vars = instantProbRels.getVariables();
		vars.join(instantUtilRels.getVariables());
		
		//We eliminate all variables through sum using probability and utility potentials
		
		vars = removeNonFiniteStatesVariables(vars);
		
		while (vars.size()>0){
			
			//var = (FiniteStates)vars.elementAt(i);
			var = extractAVariableToBeEliminated(vars,instantProbRels,instantUtilRels);
			
			//We add a uniform distribution for the decision node
			if (var.getKindOfNode()==Node.DECISION){
				//We create a uniform distribution for the decision and we add it to the set of probability relations
				//Create a new relation that is added to the utility relations
	            newDecRel = new Relation();
	            // Set the kind for the final relation 
	            newDecRel.setKind(Relation.POTENTIAL);
	            auxNodes = new NodeList();
	            auxNodes.insertNode(var);
	            newDecRel.setVariables(auxNodes);
	            newDecPot = new PotentialTable(var);
	            newDecPot.setValue(1.0/var.getNumStates());
	            newDecRel.setValues(newDecPot);
	            instantProbRels.insertRelation(newDecRel);
			}
			
			//We sum-marginalize the variable
			eliminateVariableBySum(var,instantProbRels,instantUtilRels);
			
		}
		
		length = instantUtilRels.size();
		finalValues = new double[length];
		
		//We sum the final values of the utility relations
		for (int i=0;i<length;i++){
			finalValues[i]=((PotentialTable)(instantUtilRels.elementAt(i).getValues())).maximumValue();
		}
		return Stat.sum(finalValues);
		//return 0.0;
	}

	/**
	 * @param vars
	 * @param instantProbRels
	 * @param instantUtilRels
	 * @return a variable to be eliminated and remove it from the list 'vars'
	 */
	private static FiniteStates extractAVariableToBeEliminated(NodeList vars,
		RelationList instantProbRels, RelationList instantUtilRels) {
	// TODO Auto-generated method stub
	
		double minIncreaseSizes;
		double auxIncreaseSize;
		Node auxVar;
		Node minVar = null;
		int indexMinVar=-1;
		
		minIncreaseSizes = Double.MAX_VALUE;
		
		for (int i=0;i<vars.size();i++){
			auxVar = vars.elementAt(i);
			auxIncreaseSize = increaseInSizeElimination(auxVar,instantProbRels,instantUtilRels);
			if (auxIncreaseSize<minIncreaseSizes){
				minIncreaseSizes = auxIncreaseSize;
				minVar = auxVar;
				indexMinVar = i;
			}
		}
		//Remove minVar from vars
		vars.removeNode(indexMinVar);
		
		return (FiniteStates) minVar;
}


	private static double increaseInSizeElimination(Node auxVar,
			RelationList instantProbRels, RelationList instantUtilRels) {
		// TODO Auto-generated method stub
		RelationList probRelsVar;
		RelationList utilRelsVar;
		double increase;
		double sizeBefore;
		double sizeAfter;
		
		probRelsVar = instantProbRels.getRelationsOf(auxVar);
		utilRelsVar = instantUtilRels.getRelationsOf(auxVar);
		//Size before the elimination of the variable
		sizeBefore = probRelsVar.sumSizes()+utilRelsVar.sumSizes();
		//Size after the elimination of the variable
		sizeAfter = sizeEliminationVar(auxVar,probRelsVar,utilRelsVar);
		increase = sizeAfter - sizeBefore;
		return increase;
	}


	/**
	 * @param auxVar
	 * @param probRelsVar
	 * @param utilRelsVar
	 * @return The size of the new list of probability and utility potentials after combining them and eliminating a variable
	 */
	private static double sizeEliminationVar(Node auxVar,
			RelationList probRelsVar, RelationList utilRelsVar) {
		// TODO Auto-generated method stub
		NodeList varsProbRels;
		NodeList varsUtilRels;
		double newSizeProbRels;
		double newSizeUtilRels;
		int numStatesAuxVar;
		double totalSize;
		
		numStatesAuxVar = ((FiniteStates)auxVar).getNumStates();
		
		varsProbRels = probRelsVar.getVariables();
		//New size of the probability potentials
		newSizeProbRels = varsProbRels.getSize()/numStatesAuxVar;
		varsUtilRels = utilRelsVar.getVariables();
		varsUtilRels.join(varsProbRels);
		//New size of the utility potentials
		newSizeUtilRels = varsUtilRels.getSize()/numStatesAuxVar;
		//Total size
		totalSize = (newSizeProbRels+newSizeUtilRels);
		
		return totalSize;
	}


	private static NodeList removeNonFiniteStatesVariables(NodeList vars) {
	// TODO Auto-generated method stub
		NodeList newVars;
		Node auxVar;
		newVars = new NodeList();

		
		for(int i=0;i<vars.size();i++){
			auxVar = vars.elementAt(i);
			
			if (auxVar.getClass()==FiniteStates.class){
				newVars.insertNode(auxVar);
			}
		}
		return newVars;
		
		
}


	/**
	 * @param instantUtilRels
	 * @return The value of the heuristic maxixum modified with dynamic weighting
	 */
	public double heuristicMaximumModifiedWithDynamicWeighting(RelationList instantUtilRels){
		double f1;
		
		f1 = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
		
		f1 = modifyHeuristicWithDynamicWeighting(f1);
		
		return f1;
	}

	public int getIndexOfBestChild(){
		// TODO Auto-generated method stub
//		 TODO Auto-generated method stub
		int first=-1;
		NodeList children;
		boolean found=false;
		NodeAOUID auxChild;

		
		switch(type){
		case CHANCE:
			//We don't have to select any decision
			first = -1;
			break;
		case DECISION:
		case BRANCH:
		
			children = getChildrenNodes();
			//To look for the index of the best child
			for (int i=0;(i<children.size())&&(found==false);i++){
				auxChild = (NodeAOUID)children.elementAt(i);
				if (auxChild==bestChildInPartialSolution){
					first = i;
					found = true;
				}
			}
			break;
		}
		return first;
	}


	public void printEstimates() {
		// TODO Auto-generated method stub
		if (isDebug()) System.out.println("Partial optimal solution: f="+this.f);
	}


	protected static boolean isDebug() {
		// TODO Auto-generated method stub
		return (GraphAOUID.isDebug());
	}


	public int getNumberOfLevelsCompleted() {
		// TODO Auto-generated method stub
			int levels;
			int depthChildren[];
			// TODO Auto-generated method stub
			if ((children==null)||children.size()==0){
				levels = 1;
			}
			else{
				depthChildren = new int[children.size()];
				for (int i=0;i<children.size();i++){
					depthChildren[i]=((NodeAOUID)children.elementAt(i).getHead()).getDepth();
				}
				levels = Stat.min(depthChildren)+1;
			}
			return levels;
		}


	/**
	 * @return The EU of the strategy but using the value computed by the evaluation of the GSDAG in the leaves of the tree
	 */
	public double getEUOfCurrentStrategyDPGSDAG() {
		// TODO Auto-generated method stub
	// TODO Auto-generated method stub
		
		NodeList childrenNodes;
		NodeAOUID auxChild;
		double eu = 0;
		
		
		childrenNodes = this.getChildrenNodes();
		if (childrenNodes.size()==0){
			eu = getEUOfCurrentStrategyForLeavesDPGSDAG();
		}
		else{
		switch (type){
		case BRANCH:
		case DECISION:
			eu = bestChildInPartialSolution.getEUOfCurrentStrategyDPGSDAG();
			break;
		case CHANCE:
			//Weighted sum over the children
			eu = 0;
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID)childrenNodes.elementAt(i);
				eu = eu + auxChild.getEUOfCurrentStrategyDPGSDAG()*conditionalProbs.getValue(auxChild.getInstantiations());
			}
			break;
		}
		}
		return eu;
	}


	/**
	 * @return The EU of the leaf obtained by DP over the GSDAG
	 */
	public double getEUOfCurrentStrategyForLeavesDPGSDAG() {
		// TODO Auto-generated method stub
		GSDAG gsdag;
		RelationsNodeGSDAG rels;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		double eu;
		
		gsdag = this.graphUID.gsdag;
		
		rels = getRelationsOfNodeAOUIDInGSDAG();
		
		instantProbRels = instantiateRelations(rels.getProbabilityRelations());
		instantUtilRels = instantiateRelations(rels.getUtilityRelations());
		
		eu=getEU(instantProbRels,instantUtilRels);
		
		return eu;
	}


	private RelationsNodeGSDAG getRelationsOfNodeAOUIDInGSDAG() {
		// TODO Auto-generated method stub
		int indexOfRels;
		
		//indexOfRels = this.nodeGSDAG.getIndexOfRelationsBeforeEliminating(nameOfVariable);
		indexOfRels = this.nodeGSDAG.getIndexOfRelationsAfterEliminating(nameOfVariable);
		
		return this.nodeGSDAG.getListRelations().get(indexOfRels);
		
	}


	/**
	 * @param numDecisionsPlotMEU
	 * @return The EU where the first N decisions/branch points are given by AO and the rest by DP
	 */
	public double getEUOfCurrentStrategyNDecs(int numDecisionsPlotMEU) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
			
			NodeList childrenNodes;
		NodeAOUID auxChild;
		double eu = 0;
		int newNumDecisionsPlotMEU;

		if (numDecisionsPlotMEU > 0) {

			childrenNodes = this.getChildrenNodes();
			if (childrenNodes.size() == 0) {
				eu = getEUOfCurrentStrategyForLeavesNDecs(numDecisionsPlotMEU);
			} else {
				switch (type) {
				case BRANCH:
				case DECISION:
					newNumDecisionsPlotMEU = numDecisionsPlotMEU - 1;
					eu = bestChildInPartialSolution
							.getEUOfCurrentStrategyNDecs(newNumDecisionsPlotMEU);
					break;
				case CHANCE:
					// Weighted sum over the children
					eu = 0;
					for (int i = 0; i < childrenNodes.size(); i++) {
						auxChild = (NodeAOUID) childrenNodes.elementAt(i);
						eu = eu
								+ auxChild
										.getEUOfCurrentStrategyNDecs(numDecisionsPlotMEU)
								* conditionalProbs.getValue(auxChild
										.getInstantiations());
					}
					break;
				}
			}

		} else {// We take the value given by DP, taken from a previous evaluation of the complete GSDAG
			eu = getEUOfCurrentStrategyForLeavesDPGSDAG();
		}
		return eu;
	}


	/**
	 * @param levelDT The level of the decision tree (only considering branch points and decisions) where we are going to
	 * calculate the statistic
	 * @return The proportion of the decisions we are right
	 */
	public double getProportionDecisionsRight(int levelDT) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
			
			NodeList childrenNodes;
			NodeAOUID auxChild;
			double proportion = 0;
			int newLevelDT=0;
			int numChildren;
			
			if (levelDT > 0) {

			childrenNodes = this.getChildrenNodes();
			numChildren = childrenNodes.size();
			if (numChildren == 0) {
				proportion = getProportionDecisionsRightForLeavesOfDT(levelDT,
						nodeGSDAG);
			}
			else {

				if ((levelDT > 1) || (type == NodeAOUID.TypeOfNodeAOUID.CHANCE)) {
					switch (type) {
					case BRANCH:
					case DECISION:
						newLevelDT = levelDT - 1;
						break;
					case CHANCE:
						newLevelDT = levelDT;
						break;
					}

					for (int i = 0; i < numChildren; i++) {
						auxChild = (NodeAOUID) childrenNodes.elementAt(i);
						proportion = proportion
								+ (auxChild
										.getProportionDecisionsRight(newLevelDT) / numChildren);
					}
				}
				else {
					// Else we have to see if we are right in the decision

					proportion = getValueDecisionMade();
				}
			}
		}
		else {// We take the value given by DP
			proportion = 1.0;
		}
		return proportion;
	}



/**
 * @return 1 if the decision made is correct and 0 otherwise
 */
private double getValueDecisionMade() {
	// TODO Auto-generated method stub
	double maximumValue;
	double selectedValue;
	double epsilon = 0.00001;
	double valueDec;
	
	maximumValue = getEUOfCurrentStrategyForLeavesDPGSDAG();
	
	selectedValue = this.bestChildInPartialSolution.getEUOfCurrentStrategyForLeavesDPGSDAG();
	
	if (selectedValue+epsilon>maximumValue){
		valueDec = 1.0;
	}
	else{
		valueDec = 0.0;
	}
	
	return valueDec;
}
	
	/**
	 * This value is not exact. It is an approximation assuming all
	 * @param levelDT
	 * @return
	 */
	private double getProportionDecisionsRightForLeavesOfDT(int levelDT,NodeGSDAG node) {
		// TODO Auto-generated method stub
		
		double proportion = 0.0;
		NodeList auxChildren;
		int numChildren;
		int newLevelDT = 0;
		FiniteStates auxDec;
		
		
		
		if (levelDT==0){
			proportion = 1.0;
		}
		else{
			TypeOfNodeGSDAG typeNodeGSDAG = node.type;
			int numVarsInNode;
			ArrayList<String> varsInNode = node.getVariables();
			numVarsInNode = varsInNode.size();
			
			if ((typeNodeGSDAG==TypeOfNodeGSDAG.DECISION)&&(levelDT<=numVarsInNode)){
				auxDec = (FiniteStates) uid.getNode(varsInNode.get(levelDT-1));
				proportion = 1.0/(auxDec.getNumStates());
			}
			else if ((typeNodeGSDAG==TypeOfNodeGSDAG.BRANCH)&&(levelDT==1)){
				proportion = 1.0/node.getChildren().size();
				
			}
			else{
				switch (typeNodeGSDAG){
				case CHANCE:
					newLevelDT = levelDT;
					break;
				case DECISION:
					newLevelDT = levelDT - numVarsInNode;
					break;
				case BRANCH:
					newLevelDT = levelDT-1;
					break;
				}
				
				auxChildren = node.getChildrenNodes();
				numChildren = auxChildren.size();
				for (int i=0;i<numChildren;i++){
					NodeGSDAG auxChild = (NodeGSDAG) auxChildren.elementAt(i);
					proportion = proportion + getProportionDecisionsRightForLeavesOfDT(newLevelDT,auxChild)/numChildren;
				}	
			}
						
			
		}
		return proportion;
			
			
			
	}


	/**
	 * @param numDecisionsPlotMEU
	 * @return The EU for the leaves of the DT, assuming that 
	 */
	private double getEUOfCurrentStrategyForLeavesNDecs(int numDecisionsPlotMEU) {
		
		
		ArrayList<NodeGSDAGAndRelations> nearestDescsNDecs;
		NodeGSDAG auxNodeGSDAG;
		RelationList auxInstantUtilRels;
		RelationList auxInstantProbRels;
		double euOfDescs[];
		double probSelectDesc[];
		double globalEU;
		NodeGSDAGAndRelations auxNodeGSDAGAndRels;
		RelationsNodeGSDAG auxRels;
		
		nearestDescsNDecs = nodeGSDAG.obtainNearestDescendantsAfterDeciding(numDecisionsPlotMEU);
		
		
		globalEU = 0.0;
		euOfDescs = new double[nearestDescsNDecs.size()];
		probSelectDesc = new double[nearestDescsNDecs.size()];
		
		for (int i=0;i<nearestDescsNDecs.size();i++){
			auxNodeGSDAGAndRels = nearestDescsNDecs.get(i);
			auxRels = auxNodeGSDAGAndRels.getRelations();
			//We instantiate the utility potentials
			auxInstantUtilRels = instantiateRelations(auxRels.getUtilityRelations());
			auxInstantProbRels = instantiateRelations(auxRels.getProbabilityRelations());
			//We calculate the value of the EU for the utility potentials insantiated
			euOfDescs[i] = getEU(auxInstantProbRels,auxInstantUtilRels);
			auxNodeGSDAG = auxNodeGSDAGAndRels.getNodeGSDAG();
			probSelectDesc[i]=nodeGSDAG.obtainProbabilityOfSelectGSDAGReference(auxNodeGSDAG);
			globalEU = globalEU+euOfDescs[i]*probSelectDesc[i];
		}
		return globalEU;
		
		
	}


	


	}//end of class
