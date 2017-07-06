package elvira.inference.uids;

import java.util.ArrayList;

import elvira.Configuration;
import elvira.Relation;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.clustering.LazyPenniless;
import elvira.potential.PotentialTable;
import elvira.tools.statistics.analysis.Stat;

public class NodeAOUIDLOfH extends NodeAOUID {

	
	double []listOfF;
	
	public NodeAOUIDLOfH(UID uid2, GSDAG gsdag, GraphAOUIDLOfH graphUID2) {
		// TODO Auto-generated constructor stub
		
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
		
		calculateValueOfHeuristic(null);
	
	}

	public NodeAOUIDLOfH() {
		// TODO Auto-generated constructor stub
	}

	//Computes the value of the heuristic with the sum of the maximum for
	//the utility nodes 
	protected void calculateValueOfHeuristic() {
		NodeGSDAG last;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		double f1;
		double f2;
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();
		instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
		//instantProbRels = instantiateRelations(last.probabilityRelations);
		
		listOfF = computeListOfHeuristics(null,instantUtilRels);
		
		f=Stat.sum(listOfF);
			
	}
	
	/**
	 * @param instantProbRels
	 * @param instantUtilRels
	 * @return List of heuristics for the different utility nodes.
	 */
	protected double[] computeListOfHeuristics(RelationList instantProbRels, RelationList instantUtilRels) {
		// TODO Auto-generated method stub
		double f1,f2;
		double h;
		int numRels;
		double []listOfHeur;
		
		numRels = instantUtilRels.size();
		
		listOfHeur = new double[numRels];
		
		for (int i=0;i<numRels;i++){
			listOfHeur[i]=heuristicMaximum(instantUtilRels.elementAt(i));
		}
		
		return listOfHeur;
	
	}
	
	//Computes the value of the heuristic with the sum of the maximum for
	//the utility nodes 
	public static double heuristicMaximum(Relation rel) {
			
		return ((PotentialTable)(rel.getValues())).maximumValue();
			
		
	}

	/* (non-Javadoc)
	 * @see elvira.inference.uids.NodeAOUID#calculateValueOfHeuristic(elvira.inference.uids.NodeAOUID)
	 * If father==null then we have to calculate the heuristics. Otherwise we could
	 * save some computations.
	 */
	@Override
	protected void calculateValueOfHeuristic(NodeAOUID father) {
		// TODO Auto-generated method stub
		
		NodeGSDAG last;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		double f1;
		double f2;
		Relation auxRel;
		RelationList rels;
		String varOfFather;
		int numRels;
		
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();
		
		rels = last.getCurrentUtilityRelations();
		
		numRels = rels.size();
		
		listOfF = new double[numRels];
		
		for (int i=0;i<rels.size();i++){
			auxRel = rels.elementAt(i);
			if (father!=null){
			if (auxRel.getVariables().getId(father.getNameOfVariable())!=-1){
				//The variable belongs to the utility potential, so we have to recalculate the local heuristic
				listOfF[i]=heuristicMaximum(instantiateRelation(auxRel));
			}
			else{
				//We can reuse the heuristic
				listOfF[i]=((NodeAOUIDLOfH)father).getListOfF()[i];
			}
			}
			else{
				listOfF[i]=heuristicMaximum(instantiateRelation(auxRel));
			}
			
		}
		
		f=Stat.sum(listOfF);
		
	}

	public double[] getListOfF() {
		return listOfF;
	}

	@Override
	protected void setFInChildOfBranch(NodeAOUID branch) {
		// TODO Auto-generated method stub
		double listOfFBranch[];
		super.setFInChildOfBranch(branch);
		
		listOfFBranch = ((NodeAOUIDLOfH) branch).getListOfF();
		
		listOfF = new double[listOfFBranch.length];
				 
		for (int i=0;i<listOfFBranch.length;i++){
			listOfF[i]=listOfFBranch[i];
			
		}
	}

	public NodeAOUIDLOfH copy(){
		NodeAOUIDLOfH auxNode;
		
		auxNode = new NodeAOUIDLOfH();
		auxNode.uid = uid;
		auxNode.graphUID = graphUID;
		auxNode.instantiations = instantiations.duplicate();
		auxNode.f = f;
		auxNode.type = type;
		auxNode.nameOfVariable = nameOfVariable;
		auxNode.nodeGSDAG = nodeGSDAG;

		return auxNode;
	}
	

}


