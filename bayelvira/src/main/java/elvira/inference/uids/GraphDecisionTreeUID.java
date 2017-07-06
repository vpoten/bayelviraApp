package elvira.inference.uids;

import java.util.ArrayList;

import elvira.UID;

public class GraphDecisionTreeUID extends GraphAODinamicUID {

	public GraphDecisionTreeUID(UID uid, GSDAG gsdag2, Boolean applyDinamicWeighting2) {
	NodeAODinamicUID initialState;
		// TODO Auto-generated constructor stub
		/*open = new ArrayList();
		closed = new ArrayList();*/
		
		applyDynamicWeighting = applyDinamicWeighting2;
		gsdag = gsdag2;
		
		initialState = new NodeDecisionTreeUID(uid,gsdag,this);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
	}

	
/*	*//**
	 * If we use a non-admissible heuristic the list of candidates of expand should be 'open'.
	 * However, we introduce randomization, so the current optimal branches have more probability of being
	 * selected.
	 * @return The list of candidates to expand in the tree rooted by 'nodeAOUID'
	 *//*
	public ArrayList<NodeAOUID> obtainCandidatesToExpand(NodeAOUID nodeAOUID) {
	
	// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		
		nodesOfPartialSolution = obtainNodesOfPartialSolutionWithRandomization(nodeAOUID);
		candidates = new ArrayList();
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (NodeAOUID auxNodeSolution: nodesOfPartialSolution){
			if (isOpen(auxNodeSolution)){
				candidates.add(auxNodeSolution);
			}
		}
		
		return candidates;
	}*/

	
}
