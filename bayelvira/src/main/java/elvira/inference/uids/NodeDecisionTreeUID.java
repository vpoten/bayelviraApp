package elvira.inference.uids;

import java.util.ArrayList;

import elvira.NodeList;
import elvira.RelationList;
import elvira.UID;
import elvira.tools.statistics.analysis.Stat;

public class NodeDecisionTreeUID extends NodeAODinamicUID{

	public NodeDecisionTreeUID(UID uid, GSDAG gsdag, GraphAODinamicUID dinamicUID) {
		// TODO Auto-generated constructor stub
		super(uid,gsdag,dinamicUID);
	}

	
	//It takes into account the possible descendants to compute the heuristic
	protected void calculateValueOfHeuristic() {
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
		
			
	}
	
protected double heuristic(RelationList instantProbRels, RelationList instantUtilRels) {
		// TODO Auto-generated method stub
		double f;
			
		f = nonAdmissibleHeuristic(null,instantUtilRels);
		
		return f;
	}


public void auxObtainNodesOfPartialSolutionWithRandomization(ArrayList<NodeAOUID> auxNodes) {
	// TODO Auto-generated method stub
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

}