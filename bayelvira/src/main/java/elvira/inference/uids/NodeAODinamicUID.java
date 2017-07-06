package elvira.inference.uids;

import java.util.ArrayList;
import java.util.Vector;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.InvalidEditException;
import elvira.LinkList;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.uids.NodeAOUID.TypeOfNodeAOUID;
import elvira.potential.Potential;
import elvira.tools.statistics.analysis.Stat;

/**  
 * Class <code>NodeAOUIDDP</code>. Implements the node for obtaining the solution of 
 * an UID with a combination of AO* algorithm and dinamic programming.
 * @author Manuel Luque
 * @since 13/10/2005
 */
public class NodeAODinamicUID extends NodeAOUID {
	
	boolean hasObsoleteF=false;

	@Override
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
			
			//newNodeAOUID.setF(getF());
			graphUID.addNode(newNodeAOUID);
			//graphUID.open.add(newNodeAOUID);
			newNodeAOUID.setOpen(true);
			sucessors.add(newNodeAOUID);
			graphUID.setNumberOfNodes(graphUID.getNumberOfNodes()+1);
		}
		
		calculateFInSucessorsOfBranch(sucessors);
		return sucessors;
	}

	/**
	 * It calculates the heuristic in the sucessors of the branch saving time because
	 * it takes into account that some computation does not have to be repeated for several
	 * sucessors
	 * @param sucessors
	 */
	private void calculateFInSucessorsOfBranch(ArrayList<NodeAOUID> sucessors) {
		// TODO Auto-generated method stub
		ArrayList<NodeGSDAG>[] descendantsOfChild;
		NodeAOUID auxSuc;
		ArrayList<NodeGSDAG> allDescendants;
		NodeGSDAG auxNodeGSDAG;
		RelationList auxInstantUtilRels;
		RelationList auxInstantProbRels;
		double[] heurOfAllDescendants;
		ArrayList<NodeGSDAG> auxDescs;
		double [][]heurOfDescendantsOfChild;
		int indexOfAuxNodeGSDAG;
		double []finalHeuristic;
		
		descendantsOfChild = new ArrayList[sucessors.size()];
		//Calculate the descendants with potentials for each nodeGSDAG sucessor of the branch
		for (int i=0;i<sucessors.size();i++){
			auxSuc = sucessors.get(i);
			descendantsOfChild[i] = auxSuc.nodeGSDAG.obtainMinimalSetOfNearestDescendantsWithSomeVariablesEliminated();
		}
		
		//We join all the descendants
		allDescendants = (ArrayList<NodeGSDAG>) descendantsOfChild[0].clone();
		for (int i=1;i<descendantsOfChild.length;i++){
			allDescendants.addAll(descendantsOfChild[i]);
		}
	
	
		heurOfAllDescendants = new double[allDescendants.size()];
	//Computation of the heuristic for each descendant
	for (int i=0;i<allDescendants.size();i++){
		auxNodeGSDAG = allDescendants.get(i);
		//We instantiate the utility potentials
		auxInstantUtilRels = instantiateRelations(auxNodeGSDAG.getCurrentUtilityRelations());
		auxInstantProbRels = instantiateRelations(auxNodeGSDAG.getCurrentProbabilityRelations());
		
		//We calculate the value of the heuristic for the utility potentials instantiated
		heurOfAllDescendants[i] = heuristic(auxInstantProbRels,auxInstantUtilRels);
	}
	
	heurOfDescendantsOfChild = new double[sucessors.size()][];
	//We maximize for each sucessor over the values of the heuristics for its descendants
	for (int i=0;i<sucessors.size();i++){
		auxSuc = sucessors.get(i);
		auxDescs = descendantsOfChild[i];
		heurOfDescendantsOfChild[i]=new double[auxDescs.size()];
		//We copy the values of the heuristics corresponding to the descendants
		for (int j=0;j<auxDescs.size();j++){
			auxNodeGSDAG = auxDescs.get(j);
			indexOfAuxNodeGSDAG = allDescendants.indexOf(auxNodeGSDAG);
			heurOfDescendantsOfChild[i][j]=heurOfAllDescendants[indexOfAuxNodeGSDAG];
		}
	}
	
	finalHeuristic = new double[sucessors.size()];
	//Computation of the maximum for each sucessor of the branch and set F for each one.
	for (int i=0;i<sucessors.size();i++){
		sucessors.get(i).setF(Stat.max(heurOfDescendantsOfChild[i]));
	}
	
	
	
	
	}

	/**
	 * @param uid2
	 * @param gsdag
	 * @param graphUIDDP
	 */
	public NodeAODinamicUID(UID uid2, GSDAG gsdag, GraphAODinamicUID graphUIDDP) {
		// TODO Auto-generated constructor stub
		// TODO Auto-generated constructor stub
			
		//Keep a pointer to the UID
		uid = uid2;
		
		//Keep a pointer to the graphUID
		setGraphUID(graphUIDDP); 
		
		nodeGSDAG = gsdag.root;
		if (nodeGSDAG.type!=NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
			nameOfVariable = gsdag.root.getVariables().get(0);
		}
		type = getTypeFromGSDAG(nodeGSDAG.type);
		
	
		instantiations = new Configuration();
		
		calculateValueOfHeuristic(null);
		

	}

	public NodeAODinamicUID() {
		// TODO Auto-generated constructor stub
	}

	//It takes into account the possible descendants to compute the heuristic
	protected void calculateValueOfHeuristic(NodeAOUID father) {
		// TODO Auto-generated method stub
		ArrayList<NodeGSDAG> nearestDescsEvaluated;
		RelationList auxInstantUtilRels;
		RelationList auxInstantProbRels;
		double heurOfDescs[];
		NodeGSDAG auxNodeGSDAG;
		
		//Calculate the nodeGSDAGs descendants with potentials for the computation of the heuristic
		nearestDescsEvaluated = nodeGSDAG.obtainMinimalSetOfNearestDescendantsWithSomeVariablesEliminated();
		
		heurOfDescs = new double[nearestDescsEvaluated.size()];
		
		//Computation of the heuristic for each descendant
		for (int i=0;i<nearestDescsEvaluated.size();i++){
			auxNodeGSDAG = nearestDescsEvaluated.get(i);
			//We instantiate the utility potentials
			auxInstantUtilRels = instantiateRelations(auxNodeGSDAG.getCurrentUtilityRelations());
			auxInstantProbRels = instantiateRelations(auxNodeGSDAG.getCurrentProbabilityRelations());
			//We calculate the value of the heuristic for the utility potentials insantiated
			heurOfDescs[i] = heuristic(auxInstantProbRels,auxInstantUtilRels);
			if (graphUID.applyDynamicWeighting){
				heurOfDescs[i]=modifyHeuristicWithDynamicWeighting(heurOfDescs[i],auxNodeGSDAG);
			}
		}
		
		//When there are several descendants that give us a value of heuristic,
		//we have to use the maximum
		f = Stat.max(heurOfDescs);
		System.out.println("The value of the heuristic of the node "+getInstantiations().toString()+" is calculated as: "+f);
		
			
	}
	

	
	/*public void auxObtainCandidatesToExpand(ArrayList<NodeAODinamicUID> auxNodes){
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NodeList children;
		
		if (isInFrontierWithDinamicProgramming()){
			//Include it as candidate and stop in this branch
			
		}
		else{
			if (it is in open){
				//include it as candidate
			}
			//continue the search in the children
		}
		
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
		
	}*/
	
	/*public boolean isInFrontierWithDinamicProgramming(){
		ArrayList<NodeAOUID> oldOpen;
		
		oldOpen = open;
		
		open = new ArrayList();
		
		for (NodeAOUID node:oldOpen){
			if (node.is)
		}
		return ;
		
	}*/

	@Override
	public double getEUOfCurrentStrategyForLeaves() {
		ArrayList<NodeGSDAG> nearestDescsEvaluated;
		NodeGSDAG auxNodeGSDAG;
		RelationList auxInstantUtilRels;
		RelationList auxInstantProbRels;
		double euOfDescs[];
		double probSelectDesc[];
		double globalEU;
		// TODO Auto-generated method stub
		nearestDescsEvaluated = nodeGSDAG.obtainNearestDescendantsWithSomeVariablesEliminated();
		
		
		globalEU = 0.0;
		euOfDescs = new double[nearestDescsEvaluated.size()];
		probSelectDesc = new double[nearestDescsEvaluated.size()];
		
		for (int i=0;i<nearestDescsEvaluated.size();i++){
			auxNodeGSDAG = nearestDescsEvaluated.get(i);
			//We instantiate the utility potentials
			auxInstantUtilRels = instantiateRelations(auxNodeGSDAG.getCurrentUtilityRelations());
			auxInstantProbRels = instantiateRelations(auxNodeGSDAG.getCurrentProbabilityRelations());
			//We calculate the value of the EU for the utility potentials insantiated
			euOfDescs[i] = getEU(auxInstantProbRels,auxInstantUtilRels);
			probSelectDesc[i]=nodeGSDAG.obtainProbabilityOfSelect(auxNodeGSDAG);
			globalEU = globalEU+euOfDescs[i]*probSelectDesc[i];
		}
		return globalEU;
		
		
	}

	/**
	 * It prunes all the part of the tree that have been exceeded by the evaluation
	 * of the dinamic programming.
	 * It also computes the new list of open.
	 * The value of the heuristic is not changed because subsequent expansion of the
	 * open nodes will calculate the value of the heuristic using the new tables that we have
	 * after the dinamic programming
	 * @param newOpen
	 */
	public void pruneAfterDP(ArrayList<NodeAOUID> newOpen) {
		// TODO Auto-generated method stub
		NodeList children;
		LinkList links;
		
		children = this.getChildrenNodes();
 
		if (isCoveredByDP()){//The node is in the limit of the zone calculated by the DP
			if (hasDescendantsInOpen()){
				//We add it to newOpen if one of its descendants was in open
				newOpen.add(this);
			}
			links = this.getChildren();
			//We prune the links with all the children 
			while (links.size()>0){
				links.removeLink(0);
			}
			
		}
		else{//The node has not been evaluated by the DP, so it's included in newOpen iff it
			//was in open. Also, we proecess the children
			if (this.graphUID.isOpen(this)){
				newOpen.add(this);
			}
		
			for (int i=0;i<children.size();i++){
				((NodeAODinamicUID)children.elementAt(i)).pruneAfterDP(newOpen);
			}
			
		}
		
	}

	private boolean hasDescendantsInOpen() {
		boolean hasDescInOpen=false;
		NodeList children;
		
		if (this.graphUID.isOpen(this)){
			hasDescInOpen = true;
		}
		else{
			children = getChildrenNodes();
			for (int i=0;i<children.size();i++){
				if (((NodeAODinamicUID)children.elementAt(i)).hasDescendantsInOpen()){
					hasDescInOpen = true;
				}
			}
		}
		// TODO Auto-generated method stub
		return hasDescInOpen;
	}

	
	/**
	 * It returns 'true' iff the NodeAODinamicUID don't have to be explored through
	 * the tree because it was exceeded by the frontier of the evaluation of the dinamic programming
	 * @return
	 */
	boolean isCoveredByDP() {
		// TODO Auto-generated method stub
		boolean isCovered;
		ArrayList<String> vars;
		String lastVar;
		
		isCovered = false;
		//If the nodeGSDAG has been evaluated (all its variables have been eliminated)
		//then this node is covered by the dinamic programming
		if (nodeGSDAG.isCompletelyEvaluated()){
			isCovered = true;
		}
		else{
			lastVar = nodeGSDAG.getLastEliminatedVariable();
			vars = nodeGSDAG.getVariables();
			//We look out if nameOfVariable was eliminated previously. If it was then
			//this node is covered by the dinamic programming
			if ((lastVar!="")&&(vars.indexOf(nameOfVariable)>=vars.indexOf(lastVar))){
				isCovered = true;
			}
			else{
				isCovered = false;
			}
			
		}
		return isCovered;
	}

	public NodeAODinamicUID copy(){
		NodeAODinamicUID auxNode;
		
		auxNode = new NodeAODinamicUID();
		auxNode.uid = uid;
		auxNode.graphUID = graphUID;
		auxNode.instantiations = instantiations.duplicate();
		auxNode.f = f;
		auxNode.type = type;
		auxNode.nameOfVariable = nameOfVariable;
		auxNode.nodeGSDAG = nodeGSDAG;
	
		return auxNode;
	}

/*	@Override
	protected void instantiateRelations(FiniteStates nodeUID) {
		// TODO Auto-generated method stub
		//We do nothing. It's only for compatibility with a method of NodeAOUID
	}*/

	@Override
	protected Potential calculateConditionedProbabilities() {
		FiniteStates varNode;
		ArrayList<NodeGSDAG> nearestDescsEvaluated;
		RelationList instantiatedProbRels;
		
		varNode = (FiniteStates) uid.getNode(nameOfVariable);
		
		nearestDescsEvaluated = nodeGSDAG.obtainNearestDescendantsWithSomeVariablesEliminated();
		
		instantiatedProbRels = instantiateRelations(nearestDescsEvaluated.get(0).getCurrentProbabilityRelations());
		
		return NodeAOUID.calculateConditionedProbabilities(varNode,instantiatedProbRels);
	}



	@Override
	protected void setFInChildOfBranch(NodeAOUID branch) {
		// TODO Auto-generated method stub
		calculateValueOfHeuristic(null);
	}

	public boolean hasObsoleteHeuristic() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isHasObsoleteF() {
		return hasObsoleteF;
	}

	public void setHasObsoleteF(boolean hasObsoleteF) {
		this.hasObsoleteF = hasObsoleteF;
	}
	
	
	
	
	
	
}
