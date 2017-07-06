package elvira.inference.uids;

import java.util.ArrayList;

import elvira.InvalidEditException;
import elvira.UID;

/**
 * This class implements a variant of the algorithm AODinamicTailUID.
 * It starts applying dinamic programming to the end of the GSDAG. When the first
 * bifurcation is found from the right it gives up the dinamic programming
 * and applies the algorithm AOUID in the best branch. When a new branch is found
 * it does the same. It looks for the minimal set of branches from the right where
 * applying the dinamic programming. 
 * @author Manolo
 *
 */
public class AODinamicTailBranchesUID extends AODinamicTailUID{

	public AODinamicTailBranchesUID(UID uid) {
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
		

	}
	
}
