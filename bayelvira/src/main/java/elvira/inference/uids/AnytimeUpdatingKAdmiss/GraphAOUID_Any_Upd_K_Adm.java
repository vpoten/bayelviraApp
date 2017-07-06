package elvira.inference.uids.AnytimeUpdatingKAdmiss;

import java.util.ArrayList;
import java.util.Vector;

import elvira.Configuration;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.GraphAOUID;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.Anytime.AOUID_Anytime.HeuristicForSearching;
import elvira.inference.uids.AnytimeUpdatingK.GraphAOUID_Any_Upd_K;
import elvira.inference.uids.AnytimeUpdatingK.NodeAOUID_Any_Upd_K;

public class GraphAOUID_Any_Upd_K_Adm extends GraphAOUID_Any_Upd_K {

	public GraphAOUID_Any_Upd_K_Adm(UID uid, GSDAG gsdag2,
			HeuristicForSearching heur, double k_chance2,
			Configuration configuration) {
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
		initialState = new NodeAOUID_Any_Upd_K_Adm(uid,gsdag,this,k_chance2);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
		// TODO Auto-generated constructor stub
	}


	/*public ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpand() {
		return obtainAnOnlyCandidateToExpandRandomly();
		//return obtainAnOnlyCandidateToExpandLessDeep();
		//return obtainAnOnlyCandidateToExpandHighestWeigth();
	}
*/

	public GraphAOUID_Any_Upd_K_Adm() {
		// TODO Auto-generated constructor stub
	}


	private ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpandRandomly() {
		// TODO Auto-generated method stub
		
			// TODO Auto-generated method stub
			ArrayList<NodeAOUID> nodesOfPartialSolution;
			ArrayList<NodeAOUID> candidates;
			NodeAOUID foundNode;
			
			candidates = new ArrayList();
				
			foundNode=obtainAnOpenNodeRandomly();
			
			if (foundNode!=null){
				candidates.add(foundNode);
			}
			
			return candidates;
		}





	
}
