package elvira.inference.uids.AnytimeUpdKAdmissBreadthSearch;

import java.util.ArrayList;
import java.util.Vector;

import elvira.Configuration;
import elvira.Node;
import elvira.NodeList;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.Anytime.AOUID_Anytime.HeuristicForSearching;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.GraphAOUID_Any_Upd_K_Adm;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.NodeAOUID_Any_Upd_K_Adm;

public class GraphAOUID_Any_Upd_K_Adm_Breadth extends GraphAOUID_Any_Upd_K_Adm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5127195651585433441L;

	public GraphAOUID_Any_Upd_K_Adm_Breadth(UID uid, GSDAG gsdag2,
			HeuristicForSearching heur, double k_chance2,
			Configuration configuration) {
		// TODO Auto-generated constructor stub
		
		super();
		// TODO Auto-generated constructor stub
		NodeAOUID initialState;
		// TODO Auto-generated constructor stub
		/*open = new ArrayList();
		closed = new ArrayList();*/
		
	    //Compile the UID
	    uid.setCompiledPotentialList(new Vector());
				
		gsdag = gsdag2;
		
		heurForSearching = heur;
		
		//k_chance = k_chance2;
		
		System.out.println("First state of the tree of search");
		initialState = new NodeAOUID_Any_Upd_K_Adm_Breadth(uid,gsdag,this,k_chance2);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
		
		this.updateK = updateK;
		// TODO Auto-generated constructor stub
	}
	
/*	
	public ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpandLevel(
			int level) {
		
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> candidates;
		NodeAOUID foundNode;
		
		candidates = new ArrayList<NodeAOUID>();
			
		foundNode=obtainAnOpenNodeAtLevel(level);
		
		if (foundNode!=null){
			candidates.add(foundNode);
		}
		
		return candidates;
	}
	*/
	
/*	*//**
	 * @return an open node with highest probability. If there are several nodes with the same
	 * value then the tie is broken randomly
	 *//*
	private NodeAOUID obtainAnOpenNodeAtLevel(int level) {
		// TODO Auto-generated method stub
		
		ArrayList<NodeAOUID> open;
		NodeAOUID auxNode;
		ArrayList<Double> probsOfOpen;
		double max;
		double auxProb;
		double probAuxNode;
		NodeAOUID auxOpen;
		ArrayList<NodeAOUID> listOfMax = null;
		NodeAOUID chosenNode;
		boolean found;
			
		
		open = new ArrayList();
		probsOfOpen = new ArrayList();
		auxNode = root;
		
		if (isDebug()){
			System.out.println("Let us choose a node to be expanded");
			System.out.println("Looking for an open node in the level "+level);
		}
		
		chosenNode = obtainAnOpenNodeNotPrunedAtDepth(root,level);
		
		return chosenNode;
			
	}
*/
	

	
	

}
