package elvira.inference.uids.Anytime;

import java.util.ArrayList;
import java.util.Vector;

import elvira.Configuration;
import elvira.Evidence;
import elvira.NodeList;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.GraphAOUID;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.Anytime.AOUID_Anytime.HeuristicForSearching;

public class GraphAOUID_Anytime extends GraphAOUID {
	
	protected HeuristicForSearching heurForSearching;
	
	private double k_chance;
	
	protected boolean updateK;
	
	
public GraphAOUID_Anytime(){
		
	}
		
	public GraphAOUID_Anytime(UID uid,GSDAG gsdag2,HeuristicForSearching heur, double k_chance2)  {
		NodeAOUID initialState;
		// TODO Auto-generated constructor stub
		/*open = new ArrayList();
		closed = new ArrayList();*/
		
	    //Compile the UID
	    uid.setCompiledPotentialList(new Vector());
				
		gsdag = gsdag2;
		
		heurForSearching = heur;
		
		k_chance = k_chance2;
		
		if (isDebug()) System.out.println("First state of the tree of search");
		initialState = new NodeAOUID_Anytime(uid,gsdag,this);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
		
		
		
	}
	
	public GraphAOUID_Anytime(UID uid,GSDAG gsdag2,HeuristicForSearching heur, double k_chance2,Configuration initialInstant)  {
		NodeAOUID initialState;
		// TODO Auto-generated constructor stub
		/*open = new ArrayList();
		closed = new ArrayList();*/
		
	    //Compile the UID
	    uid.setCompiledPotentialList(new Vector());
				
		gsdag = gsdag2;
		
		heurForSearching = heur;
		
		k_chance = k_chance2;
		
		System.out.println("First state of the tree of search");
		initialState = new NodeAOUID_Anytime(uid,gsdag,this,initialInstant);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
		
		
		
	}

	
	public ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpand() {
		ArrayList<NodeAOUID> candidates;
		ArrayList<NodeAOUID> openNodes;
		
		candidates = obtainAnOnlyCandidateToExpandHigherProb();
		
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

/*	private NodeAOUID selectAnOpenNodeToImproveGreedySolution(ArrayList<NodeAOUID> openNodes) {
		NodeAOUID foundNode;
		// TODO Auto-generated method stub
		return openNodes.get(0);
	}*/

	public HeuristicForSearching getHeurForSearching() {
		return heurForSearching;
	}

	public void setHeurForSearching(HeuristicForSearching heurForSearching) {
		this.heurForSearching = heurForSearching;
	}

	public double getK_chance() {
		return k_chance;
	}

	public void setK_chance(double k_chance) {
		this.k_chance = k_chance;
	}

	public boolean updateK() {
		return updateK;
	}

	public void setUpdateK(boolean updateK) {
		this.updateK = updateK;
	}
	
/*	public void printValueOfFOfChildrenOfRoot() {
		// TODO Auto-generated method stub
		NodeList children;
		NodeAOUID_Anytime auxNode;
		
		System.out.println("Value of F of the children of the root:");
		children = root.getChildrenNodes();
		for (int i=0;i<children.size();i++){
			auxNode = (NodeAOUID_Anytime)(children.elementAt(i));
			System.out.println(auxNode.getNameOfVariable()+":"+auxNode.getF()+" decomposed in L="+auxNode.getFLowerBound()+" and F="+auxNode.getFUpperBound());
			
		}
	}*/

}
