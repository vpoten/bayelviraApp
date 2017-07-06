package elvira.inference.uids;

import java.util.ArrayList;

import elvira.InvalidEditException;
import elvira.NodeList;
import elvira.UID;
import elvira.inference.uids.NodeGSDAG.TypeOfNodeGSDAG;
import elvira.potential.PotentialTable;

public class AODinamicUID extends DynamicUID {
	GraphAODinamicUID tree;
	//We declare the tree more general for compatibility of all the subclasses
	//GraphAOUID tree;

	public AODinamicUID(UID uid) {
		super(uid);
		// TODO Auto-generated constructor stub
	}

	public void propagate(){
		ArrayList<NodeAOUID> candidates;
		NodeAODinamicUID nodeToExpand;
		PotentialTable finalPot;
		
		
		((UID)network).createGSDAG();
		
		try {
			gsdag = new GSDAG(network);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		initializePotentialsInGSDAG();
		
		tree = new GraphAODinamicUID((UID)network,gsdag,false);
		
		//We apply dinamic programming at the end of the GSDAG
		
//		candidates = tree.obtainCandidatesToExpand();
		candidates = tree.obtainAnOnlyCandidateToExpand();
		
		while (candidates.size()>0){
			//By the moment we don't apply dinamic programming, so this evaluation
			//is equivalent to AOUID, except how the tables are stored
			//Expand a node of the tree
			nodeToExpand = selectCandidate(candidates);
			tree.expand(nodeToExpand);
//			candidates = tree.obtainCandidatesToExpand();
			candidates = tree.obtainAnOnlyCandidateToExpand();
		}
		
		  finalPot = new PotentialTable();
		  finalPot.setValue(tree.root.f);
		  statistics.setFinalExpectedUtility(finalPot);
		  
		System.out.println("Partial optimal solution: f="+tree.root.f);
		
		
	}
	
	/*public void propagate(){
		ArrayList<NodeAOUID> candidates;
		NodeAODinamicUID nodeToExpand;
		
		
		((UID)network).createGSDAG();
		
		try {
			gsdag = new GSDAG(((UID)network).getGraph());
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		initializePotentialsInGSDAG();
		
		tree = new GraphAODinamicUID((UID)network,gsdag);
		
		//We apply dinamic programming at the end of the GSDAG
		
		candidates = tree.obtainCandidatesToExpand();
		
		while (candidates.size()>0){
			//By the moment we don't apply dinamic programming, except at the beginning
			if (doWeApplyDinamicProgramming()){
				//applyDinamicProgrammingAtTheEndOfGSDAG();
				applyOneStepDinamicProgramming();
				pruneTheTreeAfterDP();
				//pruneTreeAfterDinamicProgramming();
			}
			else{//Expand a node of the tree
				nodeToExpand = selectCandidate(candidates);
				tree.expand(nodeToExpand);
			}
			candidates = tree.obtainCandidatesToExpand();
		}
		
		candidates = tree.obtainCandidatesToExpand();
		
		
	}*/

	private void applyOneStepDinamicProgramming() {
		// TODO Auto-generated method stub
		
	}

	protected void pruneTheTreeAfterDPAndMarkFInOpenNodesAsObsolete() {
		// TODO Auto-generated method stub
		tree.pruneTheTreeAfterDPAndMarkFInOpenNodesAsObsolete();
		
	}
	
	protected NodeAODinamicUID selectCandidate(ArrayList<NodeAOUID> candidates) {
		double minDepth=Double.POSITIVE_INFINITY;
		NodeAODinamicUID nodeOfMinDepth = null;
		double auxDepth;
		// TODO Auto-generated method stub
		//By the moment we select any of them. For example, the first.
		for (NodeAOUID auxCandidate:candidates){
			auxDepth = auxCandidate.getInstantiations().size();
			if (auxDepth<minDepth){
				minDepth = auxDepth;
				nodeOfMinDepth = (NodeAODinamicUID) auxCandidate;
			}
		}
		return nodeOfMinDepth;
}
	

	/*protected NodeAODinamicUID selectCandidate(ArrayList<NodeAOUID> candidates) {
		// TODO Auto-generated method stub
		//return (NodeAODinamicUID) candidates.get(0);
		  //Select the candidate to expand when we have several possibilities
	}*/
/*		protected NodeAODinamicUID selectCandidate(ArrayList<NodeAOUID> candidates) {
			double fMax=Double.NEGATIVE_INFINITY;
			NodeAODinamicUID nodeOfFMax = null;
			double auxF;
			// TODO Auto-generated method stub
			//By the moment we select any of them. For example, the first.
			System.out.println("Number of candidates to expand: "+candidates.size());
			for (NodeAOUID auxCandidate:candidates){
				auxF = auxCandidate.getF();
				if (auxF>fMax){
					fMax=auxF;
					nodeOfFMax = (NodeAODinamicUID) auxCandidate;
				}
			}
			return nodeOfFMax;
			
			
			
			
			
		
	}*/

		/**
		 * According to Nilsson's proposal. It's difficult to understand why it's a good idea!
		 * @param candidates
		 * @return
		 */
/*		protected NodeAODinamicUID selectCandidate(ArrayList<NodeAOUID> candidates) {
			double fMin=Double.POSITIVE_INFINITY;
			NodeAODinamicUID nodeOfFMin = null;
			double auxF;
			// TODO Auto-generated method stub
			//By the moment we select any of them. For example, the first.
			System.out.println("Number of candidates to expand: "+candidates.size());
			for (NodeAOUID auxCandidate:candidates){
				auxF = auxCandidate.getF();
				if (auxF<fMin){
					fMin=auxF;
					nodeOfFMin = (NodeAODinamicUID) auxCandidate;
				}
			}
			return nodeOfFMin;
			
			
			
			
			
		
	}*/
		
		

		
			 //Select the candidate to expand when we have several possibilities
	/*	protected NodeAODinamicUID selectCandidate(ArrayList<NodeAOUID> candidates) {
			double minDepth=Double.POSITIVE_INFINITY;
			NodeAODinamicUID nodeOfMinDepth = null;
			double auxDepth;
			double auxF;
			double fMax=Double.NEGATIVE_INFINITY;
			NodeAODinamicUID nodeOfFMax=null;
			
			ArrayList<NodeAODinamicUID> firstCandidates=new ArrayList();
			// TODO Auto-generated method stub
			//First, we select as candidates the nodes at the minimum level
			for (NodeAOUID auxCandidate:candidates){
				auxDepth = auxCandidate.getInstantiations().size();
				if (auxDepth<minDepth){
					firstCandidates = new ArrayList();
					minDepth = auxDepth;
					firstCandidates.add((NodeAODinamicUID) auxCandidate);
				}
				else if (auxDepth==minDepth){
					firstCandidates.add((NodeAODinamicUID) auxCandidate);
				}
			}
			
			//Second: We select, from the candidates, the node with higher F
			for (NodeAOUID auxCandidate:firstCandidates){
				auxF = auxCandidate.getF();
				if (auxF>fMax){
					fMax=auxF;
					nodeOfFMax = (NodeAODinamicUID) auxCandidate;
				}
			}
			return nodeOfFMax;
	}*/
		
	
	protected void applyDinamicProgrammingAtTheEndOfGSDAG() {
		// TODO Auto-generated method stub
		NodeGSDAG lastNodeGSDAG;
		NodeList parents;
		boolean computePolicies = false;
		
		lastNodeGSDAG = tree.getGsdag().getLastNodeGSDAG();
		
		parents = lastNodeGSDAG.getParentNodes();
		if ((parents!=null)&&(parents.size()==1)){
			applyDinamicProgrammingUntilTheFirstBranch(tree.getGsdag().getLastNodeGSDAG(), 2,computePolicies);
		}
		else{
			//We do nothing
			return;
		}
	}

	/**
	 * @param start
	 * @param maxNumNodesGSDAG Maximum number of nodesGSDAG that are going to be evaluated (branches nodes are not considered)
	 */
	protected void applyDinamicProgrammingUntilTheFirstBranch(NodeGSDAG start,int maxNumNodesGSDAG, boolean computePolicies) {
		// TODO Auto-generated method stub
		NodeGSDAG currentNode;
		NodeList parents;
		boolean isBranch=false;
		int numNodesGSDAGEvaluated = 0;
		
	
		currentNode = start;
		
		//while ((currentNode.type!=TypeOfNodeGSDAG.BRANCH)||(currentNode.getParentNodes().size()==1)){
		while ((currentNode!=null)&&(isBranch==false)&&(numNodesGSDAGEvaluated<maxNumNodesGSDAG)){
			evaluateNode(currentNode,computePolicies);
			parents = currentNode.getParentNodes();
			if ((parents!=null)&&(parents.size()==1)){
				currentNode = (NodeGSDAG) parents.elementAt(0);
				isBranch = (currentNode.type == TypeOfNodeGSDAG.BRANCH);
				if (!isBranch) numNodesGSDAGEvaluated++;
			}
			else{
				currentNode = null;
			}
		}
		
		if (isBranch){
			collectRelationsInBranch(currentNode,computePolicies);
		}
	}
	
	/**
	 * @param start
	 * @param maxNumVariables Maximum number of variables that are eliminated
	 */
	protected void applyDinamicProgrammingLimited(NodeGSDAG start,int maxNumVariables) {
		// TODO Auto-generated method stub
		NodeGSDAG currentNode;
		NodeList parents;
		boolean isBranch=false;
		int maxNumVarToElimInCurrentNode;
		int maxNumVarRemainingToElim;
		boolean collectedInBranch;
		boolean computePolicies = false;
			
		
		maxNumVarRemainingToElim = maxNumVariables;
		
	
		currentNode = start;
		
		if (start!=null){
			isBranch = (currentNode.type == TypeOfNodeGSDAG.BRANCH);
			if (isBranch==false){
				//We have to collect the results of the previous node if it's the first
			//time that we eliminate variables in the node
				if (currentNode.lastEliminatedVariable==""){
					collectRelationsInChanceOrDecision(currentNode);
				}
				//Maximum number of variables that we can eliminate
				maxNumVarRemainingToElim = maxNumVariables;
						
				//Number of variables that are not eliminated in the current node
				maxNumVarToElimInCurrentNode = getNumberOfRemainingVariablesToEliminate(currentNode);
				//We see if we are going to have to eliminate variables in other node
				if (maxNumVarRemainingToElim>=maxNumVarToElimInCurrentNode){
					//We will have variables for other node, so we eliminate the remaining variables of this node
					//and we continue with the parents
					maxNumVarRemainingToElim = maxNumVarRemainingToElim-maxNumVarToElimInCurrentNode;
					eliminateRemainingVariablesInNodeGSDAGSequentially(currentNode);
					
					parents = currentNode.getParentNodes();
					for (int i=0;i<parents.size();i++){
						//We continue with the parents
						applyDinamicProgrammingLimited((NodeGSDAG) parents.elementAt(i),maxNumVarRemainingToElim);
					}
					
				}
				else{
					//We eliminate part of the variables of the current node
					eliminateVariablesInNodeGSDAGSequentially(currentNode,maxNumVarRemainingToElim);
				}
			}
			else{//Branch
				collectedInBranch = collectRelationsInBranch(currentNode,computePolicies);
				
				if (collectedInBranch){
				
				parents = currentNode.getParentNodes();
				for (int i=0;i<parents.size();i++){
					//We continue with the parents
					applyDinamicProgrammingLimited((NodeGSDAG) parents.elementAt(i),maxNumVarRemainingToElim);
				}
				}
			}
		}
		
	
	}
	
	


/**
 * 
 * @param currentNode
 * @return
 */
private static int getNumberOfRemainingVariablesToEliminate(NodeGSDAG currentNode) {
	int index;
	int remaining;
	int size;
	
	size = currentNode.getVariables().size();
	
	if (currentNode.type==TypeOfNodeGSDAG.BRANCH){
		remaining = 0;
	}
	else if (AODinamicUID.hasAnyVariableEliminated(currentNode)){
		remaining = getIndexOfLastEliminatedVariable(currentNode);
	}
	else{
		remaining = size;
	}
	
	return remaining;
		
	}


private static int getIndexOfLastEliminatedVariable(NodeGSDAG currentNode){
	
	return currentNode.getVariables().indexOf(currentNode.lastEliminatedVariable);
	
}


private static boolean hasAnyVariableEliminated(NodeGSDAG node){
	return (node.getLastEliminatedVariable()!="");
}

/**
 * It eliminates 'numVarToEliminate' variables of the nodeGSDAG
 * @param currentNode
 * @param i Number of variables to eliminate in the node
 */
private void eliminateVariablesInNodeGSDAGSequentially(NodeGSDAG node,int numVarToEliminate) {
	// TODO Auto-generated method stub

	ArrayList<String> vars;
	int index;
	String auxName;
	boolean computePolicies=false;
	
	vars = node.getVariables();
	if (AODinamicUID.hasAnyVariableEliminated(node)) {
		index = getIndexOfLastEliminatedVariable(node);
	} else {
		index = vars.size();
	}
	if (index>0){//We eliminate variables if not all of them have been eliminated
	for(int i=index-1;i>=index-numVarToEliminate;i--){
		auxName = vars.get(i);
		//We start to eliminate from index+1
		System.out.println("Eliminating variable "+auxName);
		eliminateChanceOrDecisionVariable(node,auxName,computePolicies);
	}
	}
	
}

	private void eliminateRemainingVariablesInNodeGSDAGSequentially(
			NodeGSDAG node) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		boolean computePolicies = false;

		ArrayList<String> vars;
		int index;
		String auxName;

		vars = node.getVariables();
		if (AODinamicUID.hasAnyVariableEliminated(node)) {
			index = getIndexOfLastEliminatedVariable(node);
		} else {
			index = vars.size();
		}
		
		if (index > 0) {// We eliminate variables if not all of them have been
						// eliminated
			for (int i = index - 1; i >= 0; i--) {
				auxName = vars.get(i);
				// We start to eliminate from index+1
				System.out.println("Eliminating variable " + auxName);
				eliminateChanceOrDecisionVariable(node, auxName, computePolicies);
			}
		}
	}

	public int numberOfVariablesToEliminateFromTheRoot(NodeGSDAG nodeGSDAG) {
		// TODO Auto-generated method stub
		int total=0;
		boolean rootReached;
		NodeGSDAG current;
		
		current = nodeGSDAG;
		
		rootReached = (nodeGSDAG == gsdag.root);
		
		while (rootReached==false){
			
			total = total + AODinamicUID.getNumberOfRemainingVariablesToEliminate(current);
			rootReached = (current == gsdag.root);
			if (rootReached == false){
				current = (NodeGSDAG) current.getParentNodes().elementAt(0);
			}
					
		}
		return total;
	}

	//	We consider a step of dinamic programming over a nodeGSDAG:
	//If it's a branch --> Collect the potentials of the children and maximize
	//Else eliminate a variable of the potentials of the nodeGSDAG (if there's no more variables
	//to eliminate then we don't do anything)
/*	private void auxApplyOneStepDinamicProgramming(NodeGSDAG node) {
		// TODO Auto-generated method stub
		//Process the current node
		String varToElim;
		ArrayList<String> vars;
		int index;
		
		switch (node.type){
		case BRANCH:
			collectRelationsInBranch(node);
			break;
		case CHANCE:
		case DECISION:
			if (node.isEvaluated()==false){
				vars = node.getVariables();
				//We determine the variable to eliminate
				if (node.lastEliminatedVariable==""){
					//We have to collect the potentials of the child
					collectRelationsInChanceOrDecision(node);
					varToElim = vars.get(0);
					
				}
				else{
					varToElim = node.nextVariable(node.lastEliminatedVariable);
				}
				//We eliminate the variable
				eliminateChanceOrDecisionVariable(node,varToElim);
				//We update the attribute 'evaluated'
				index = vars.indexOf(varToElim); 
				if (index==(vars.size()-1)){
					node.setEvaluated(true);
				}
			}
			else{
				//We do nothing
			}
			
			break;
		}
		
	}*/
}
