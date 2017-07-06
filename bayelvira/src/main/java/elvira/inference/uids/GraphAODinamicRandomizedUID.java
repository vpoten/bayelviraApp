package elvira.inference.uids;

import java.util.ArrayList;

import elvira.NodeList;
import elvira.UID;

public class GraphAODinamicRandomizedUID extends GraphAODinamicUID {

	public GraphAODinamicRandomizedUID(UID uid, GSDAG gsdag, Boolean applyDinamicWeighting) {
		// TODO Auto-generated constructor stub
		super(uid,gsdag,applyDinamicWeighting);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * If we use a non-admissible heuristic the list of candidates of expand should be 'open'.
	 * However, we introduce randomization, so the current optimal branches have more probability of being
	 * selected.
	 * @return The list of candidates to expand in the tree rooted by 'nodeAOUID'
	 */
	public ArrayList<NodeAOUID> obtainCandidatesToExpand() {
	
	// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		
		nodesOfPartialSolution = obtainNodesOfPartialSolutionRandomly();
		candidates = new ArrayList();
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (NodeAOUID auxNodeSolution: nodesOfPartialSolution){
			if (isOpen(auxNodeSolution)){
				candidates.add(auxNodeSolution);
			}
		}
		
		return candidates;
	}
	
	
/**
	 * @param node
	 * @return list with all the nodes of the partial optimal solution rooted by 'node'
	 */
	private ArrayList<NodeAOUID> obtainNodesOfPartialSolutionRandomly() {
		// TODO Auto-generated method stub
		
			ArrayList<NodeAOUID> auxNodes;
			
			auxNodes = new ArrayList();
			root.auxObtainNodesOfPartialSolutionRandomly(auxNodes);
				
			return auxNodes;
		
		
		
		
	}



}
