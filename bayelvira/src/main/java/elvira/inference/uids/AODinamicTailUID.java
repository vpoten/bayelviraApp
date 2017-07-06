package elvira.inference.uids;

import java.util.ArrayList;

import elvira.InvalidEditException;
import elvira.UID;
import elvira.potential.PotentialTable;

/**
 * This class implements a variant of the algorithm AODinamicUID.
 * It starts applying dinamic programming to the end of the GSDAG. When the first
 * bifurcation is found from the right it gives up completely the dinamic programming
 * and applies always the algorithm AOUID from that moment.
 * @author Manolo
 *
 */
public class AODinamicTailUID extends AODinamicUID {

	public AODinamicTailUID(UID uid) {
		super(uid);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void propagate() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> candidates;
		NodeAODinamicUID nodeToExpand;
		
		
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
		
		applyDinamicProgrammingAtTheEndOfGSDAG();
		
//		candidates = tree.obtainCandidatesToExpand();
		candidates = tree.obtainAnOnlyCandidateToExpand();
		
		while (candidates.size()>0){
			//By the moment we don't apply dinamic programming, so this evaluation
			//is equivalent to AOUID, except how the tables are stored
			//Expand a node of the tree
			System.out.println("Partial optimal solution: f="+tree.root.f);
			nodeToExpand = selectCandidate(candidates);
			tree.expand(nodeToExpand);
//			candidates = tree.obtainCandidatesToExpand();
			candidates = tree.obtainAnOnlyCandidateToExpand();
		}
		
		System.out.println("Partial optimal solution: f="+tree.root.f);
		System.out.println(tree.getNodeList().size()+" nodes were created by the algorithm AODinamicTailUID");
		System.out.println("Heuristic Maximum was selected "+tree.selectedHeuristic[0]+ "times");
		System.out.println("Heuristic Sum was selected "+tree.selectedHeuristic[1]+ "times");
	}
	
	

}
