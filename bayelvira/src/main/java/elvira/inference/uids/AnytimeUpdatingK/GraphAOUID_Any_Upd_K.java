package elvira.inference.uids.AnytimeUpdatingK;

import java.util.ArrayList;
import java.util.Vector;

import elvira.Configuration;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.GraphAOUID;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.Anytime.GraphAOUID_Anytime;
import elvira.inference.uids.Anytime.NodeAOUID_Anytime;
import elvira.inference.uids.Anytime.AOUID_Anytime.HeuristicForSearching;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.NodeAOUID_Any_Upd_K_Adm;

public class GraphAOUID_Any_Upd_K extends GraphAOUID_Anytime {

	public GraphAOUID_Any_Upd_K(UID uid, GSDAG gsdag2,
			HeuristicForSearching heur, double k_chance2,
			Configuration configuration) {
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
		initialState = new NodeAOUID_Any_Upd_K(uid,gsdag,this,k_chance2);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
	}

	public GraphAOUID_Any_Upd_K() {
		// TODO Auto-generated constructor stub
	}

	
	public ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpand() {
		ArrayList<NodeAOUID> candidates;
		ArrayList<NodeAOUID> openNodes;
		
		candidates = obtainAnOnlyCandidateToExpandHigherProb();
		//candidates = obtainAnOnlyCandidateToExpandHighestProductProbAndRange();
		
	/*	if (candidates.size()==0){
			System.out.println("*** The greedy phase has finished. We must continue the search to find the optimal solution");
			openNodes = this.obtainOpenNodes();
			System.out.println("There are still "+openNodes.size()+" open nodes:");
			for (NodeAOUID auxNode:openNodes){
				System.out.println(auxNode.getInstantiations().toString());
				System.out.println("Value of pruned: "+((NodeAOUID_Anytime)auxNode).isPruned());
				System.out.println();
			}
		}*/
		
		return candidates;
		
	}

/*	private ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpandHighestProductProbAndRange() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		NodeAOUID foundNode;
		
		candidates = new ArrayList();
			
		foundNode=obtainAnOpenNodeWithHighestProductProbAndRange();
		
		if (foundNode!=null){
			candidates.add(foundNode);
		}
		
		return candidates;
	}*/

	/*private NodeAOUID obtainAnOpenNodeWithHighestProductProbAndRange() {
		// TODO Auto-generated method stub
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
			
		
		open = new ArrayList();
		probsOfOpen = new ArrayList();
		auxNode = root;
		
		if (isDebug()){
			System.out.println("Let us choose a node to be expanded");
			System.out.println("Looking for the node to expand with the highest probability");
		}
		
		//Compute open and their probabilities
		for (int i=0;i<this.getNodeList().size();i++){
			auxNode = (NodeAOUID) getNodeList().elementAt(i);
			if (auxNode.isOpen()){
				probAuxNode = auxNode.getProbability();
				probAuxNode = probAuxNode * ((NodeAOUID_Any_Upd_K_Adm)auxNode).range();
				if (probAuxNode>0.0){
					probsOfOpen.add(probAuxNode);
					open.add(auxNode);
					if (isDebug()){
						System.out.println("The probability of the node "+auxNode.getInstantiations().toString()+": "+probAuxNode);
					//System.out.println("and its heuristic: "+auxNode.f);
					}
				}
				
			}
		}
		
		if (open.size()>0){
		//Look for the maximum of probabilities
		max = Double.MIN_VALUE;
		
		for (int i=0;i<probsOfOpen.size();i++){
			auxProb = probsOfOpen.get(i);
			auxOpen = open.get(i);
			if (auxProb>max){
				listOfMax = new ArrayList();
				listOfMax.add(auxOpen);
				max = auxProb;
			}
			else if (auxProb==max){
				listOfMax.add(auxOpen);
			}
			
		}
		
		
		chosenNode = (((listOfMax!=null)&&(listOfMax.size()>0))?((NodeAOUID) NodeAOUID.selectChildRandomlyWhenTie(listOfMax)):null);
		
		}
		else{
			chosenNode = null;
		}
		return chosenNode;
	}
	
*/
}
