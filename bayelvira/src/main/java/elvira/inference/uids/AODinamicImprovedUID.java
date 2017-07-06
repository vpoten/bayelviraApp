package elvira.inference.uids;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import elvira.InvalidEditException;
import elvira.NodeList;
import elvira.UID;
import elvira.inference.uids.NodeGSDAG.TypeOfNodeGSDAG;
import elvira.potential.PotentialTable;
import elvira.tools.CronoNano;
import elvira.tools.PropagationStatisticsAOUID;

public class AODinamicImprovedUID extends AODinamicUID {

	protected double thresholdBranchingFactor;
	protected int minimumExpansionsBeforeDP;
	private int maxNumOfApplicationsDP;
	protected Boolean applyDinamicWeighting;



	public AODinamicImprovedUID(UID uid) {
		super(uid);
		//thresholdBranchingFactor = 1.8;
		minimumExpansionsBeforeDP = 2;
		maxNumOfApplicationsDP = 100;
		
		statistics = new PropagationStatisticsAOUID();
		// TODO Auto-generated constructor stub
	}

	public void propagate(Vector paramsForCompile) {
		ArrayList<NodeAOUID> candidates;
		NodeAODinamicUID nodeToExpand;
		PotentialTable finalPot;
		PropagationStatisticsAOUID stats;
		CronoNano crono;
		double eu;
		Random r=new Random();
		int numExpansionsStat = 0;
		  int decTaken;
		  int numExpansionsBeforeStat=2;
		  int numExpansions = 0;
		  double oldF = Double.POSITIVE_INFINITY;
		  int numChildrenRootGSDAG;
		
		//Number of times that we have expanded nodes
		int numOfExpansions = 0;
		//Number of times that we have applied dinamic programming
		int numOfApplicationsDP =0;
		int step = 0;

		((UID) network).createGSDAG();

		try {
			gsdag = new GSDAG(network);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		initializePotentialsInGSDAG();

		//We pick up the parameters for the evaluation
		
//		We set the threshold that controls the DP and the search in the AO graph
		thresholdBranchingFactor = (Double) paramsForCompile.get(0);
		
		applyDinamicWeighting = (Boolean) paramsForCompile.get(1);

		tree = new GraphAODinamicUID((UID) network, gsdag,applyDinamicWeighting);
		
		statistics.addTime(0);
		  
		  stats = (PropagationStatisticsAOUID)statistics;
		  stats.addExpectedUtility(getEUOfCurrentStrategy());
		  numChildrenRootGSDAG = gsdag.root.getChildren().size();
		  stats.addDecisionAndOption(r.nextInt(numChildrenRootGSDAG),-1);
		  
		//The minimum expansions before DP has to be a multiple of numChildrenRootGSDAG
		  //to do a fair comparison with the algorithm AODinamicImprovedFirstBranchUID
		  //minimumExpansionsBeforeDP = (Integer)paramsForCompile.get(2)*numChildrenRootGSDAG;
		  minimumExpansionsBeforeDP = (Integer)paramsForCompile.get(2);
		   
		  crono= new CronoNano();
			crono.start();
			
		// We apply dinamic programming at the end of the GSDAG
			  //this.applyDinamicProgrammingAtTheEndOfGSDAG();

		//candidates = tree.obtainCandidatesToExpand();
			candidates = tree.obtainAnOnlyCandidateToExpand();
		
	
		while (candidates.size() > 0) {
			
			// By the moment we don't apply dinamic programming, except at the
			// beginning
			if (numExpansionsStat<numExpansionsBeforeStat){
			step++;
			System.out.println("** Step "+step);
			
			
			if (oldF<tree.root.f)
			  try {
				
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Partial optimal solution: f="+tree.root.f);
			  System.out.println("Depth of the tree: "+tree.getDepth());
			  System.out.println("Nodes in the tree: "+tree.getNumberOfNodes());
			  System.out.println("Effective branching factor: "+tree.getEffectiveBranchingFactor());
			
			  			
			if (doWeApplyDinamicProgramming(numOfExpansions,numOfApplicationsDP)) {
				// applyDinamicProgrammingAtTheEndOfGSDAG();
				//applyOneStepDinamicProgramming();
				System.out.println("***** Applying DP *****");
				numOfExpansions=0;
				//We are going to see what happens if we stop while applying DP
				applyDinamicProgrammingAdvancingAllTheFrontiers();
				pruneTheTreeAfterDPAndMarkFInOpenNodesAsObsolete();
				//numOfApplicationsDP++;
				//We decrease the frequency of applying DP
				//minimumExpansionsBeforeDP++;
			} else {// Expand a node of the tree
				numOfExpansions++;
				numExpansionsStat++;
				nodeToExpand = selectCandidate(candidates);
				tree.expand(nodeToExpand);
				tree.printValueOfFOfChildrenOfRoot();
			}
//			candidates = tree.obtainCandidatesToExpand();
			candidates = tree.obtainAnOnlyCandidateToExpand();
			
			}
			else{//Compute statistics
				
				  numExpansionsStat=0;
				  stats.addToLastTime(crono.getTime());
				  crono.stop();
				  eu = getEUOfCurrentStrategy();
				  System.out.println("The EU of the current strategy is:"+eu);
				  stats.addExpectedUtility(eu);
				  decTaken = getFirstDecisionTakenInTheTree();
				  stats.addDecisionAndOption(decTaken,-1);
				  System.gc();
				  crono.start();
			}
		}
		
		  finalPot = new PotentialTable();
		  finalPot.setValue(tree.root.f);
		  statistics.setFinalExpectedUtility(finalPot);
		
		((PropagationStatisticsAOUID)statistics).setCreatedNodes(getNumberOfCreatedNodes());
		  System.out.println("Partial optimal solution: f="+tree.root.f);
		  System.out.println(tree.getNodeList().size()+" nodes were created by the algorithm AO*");
		  stats.addToLastTime(crono.getTime());
		  crono.stop();
		  eu = getEUOfCurrentStrategy();
		  stats.addExpectedUtility(eu);
		  decTaken = getFirstDecisionTakenInTheTree();
		  stats.addDecisionAndOption(decTaken,-1);
		  

		

	}

	protected void pruneTheTreeAfterDPAndMarkFInOpenNodesAsObsolete() {
		// TODO Auto-generated method stub
		this.tree.pruneTheTreeAfterDPAndMarkFInOpenNodesAsObsolete();
	}

	protected int getFirstDecisionTakenInTheTree() {
		return tree.getFirstDecisionMadeInTheTree();
		
	}
	
/*	private void applyDinamicProgramming() {
		// TODO Auto-generated method stub
		//Candidate nodes of the GSDAG where we can apply DP
		ArrayList<NodeGSDAG> candidatesForDP;
		NodeGSDAG nodeToApplyDP;
		NodeList parentsOfCandidate;
		NodeGSDAG auxParent;
		
		//Calculate the nodeGSDAGs that are the frontier where the DP is stopped
		candidatesForDP = this.gsdag.root.obtainNearestDescendantsEvaluated();
		
		nodeToApplyDP = selectCandidateToApplyDP(candidatesForDP);
		
		parentsOfCandidate = nodeToApplyDP.getParentNodes();
		
		//Apply DP in each parent until they find a branch
		for (int i=0;i<parentsOfCandidate.size();i++){
			auxParent = (NodeGSDAG)parentsOfCandidate.elementAt(i);
			this.applyDinamicProgrammingUntilTheFirstBranch(auxParent,2);
			
		}
		
	}*/

	
	/**
	 * It applies dinamic programming moving all the frontier the same number of steps (nodes of the GSDAG)
	 */
	protected void applyDinamicProgrammingAdvancingAllTheFrontiers() {
		// TODO Auto-generated method stub
		//Candidate nodes of the GSDAG where we can apply DP
		ArrayList<NodeGSDAG> nodesOfFrontierDP;
		NodeList parentsOfCandidate;
		NodeGSDAG auxParent;
		boolean applyDP;
			
		//Calculate the nodeGSDAGs that are the frontier where the DP is stopped
		nodesOfFrontierDP = this.gsdag.root.obtainNearestDescendantsWithSomeVariablesEliminated();
		
		//nodeToApplyDP = selectCandidateToApplyDP(candidatesForDP);
		
		
		
		for (NodeGSDAG nodeToApplyDP:nodesOfFrontierDP){
			//	Apply DP in each node of the frontier (continue recursively through its parents)
			//applyDP = (numberOfVariablesToEliminateFromTheRoot(nodeToApplyDP)>1);
			applyDP = true;
			if (applyDP){
				//We don't apply DP to the children of the root
				//because the decision of the first branch loses stability
				applyDinamicProgrammingLimited(nodeToApplyDP,1);
			}
			else{
				System.out.println("Trying apply DP very near from the root, but we do nothing.");
			}
				
		}
		
	}

	private NodeGSDAG selectCandidateToApplyDP(ArrayList<NodeGSDAG> candidatesForDP) {
		NodeGSDAG candidate;
		NodeList parentsOfAuxCandidate;
		boolean selected = true;
		//boolean found=false;
		ArrayList<NodeGSDAG> firstCandidates;
		NodeList secondCandidates = null;
		int minDistToLast=Integer.MAX_VALUE;
		int auxDistToLast;
		NodeGSDAG finalCandidate = null;
		
		firstCandidates = new ArrayList();
		// TODO Auto-generated method stub
		if ((candidatesForDP==null)||(candidatesForDP.size()==0)){
			candidate = null;
		}
		else{
			candidate = null;
			for (int i=0;i<candidatesForDP.size();i++){
				NodeGSDAG auxCandidate;
				auxCandidate = candidatesForDP.get(i);
				parentsOfAuxCandidate = auxCandidate.getParentNodes();
				if (parentsOfAuxCandidate.size()==0){
					//We have finished the evaluation (auxCandidate is the root)
					//candidate = null;
					//found = true;
				}
				else{
					if (parentsOfAuxCandidate.size()==1){
						NodeGSDAG onlyParentOfAuxCandidate;
						onlyParentOfAuxCandidate = (NodeGSDAG) parentsOfAuxCandidate.elementAt(0);
						if (onlyParentOfAuxCandidate.type==TypeOfNodeGSDAG.BRANCH){
							if (onlyParentOfAuxCandidate.areAllChildrenEvaluated()){
								//All the children of the BRANCH must have been evaluated if we want to evaluate it
								firstCandidates.add(auxCandidate);
								//found = true;
							}
						}
						else{
							firstCandidates.add(auxCandidate);
							//found = true;
						}
					}
					else{
						//If auxCandidate has several parents then it is candidate
						firstCandidates.add(auxCandidate);
						//found = true;
					}
				}
			}
		}
		
		//We select the candidate closer to the last node of the GSDAG
		for(NodeGSDAG auxNodeGSDAG:firstCandidates){
			auxDistToLast = auxNodeGSDAG.distanceToLastNode();
			if (auxDistToLast<minDistToLast){
				minDistToLast = auxDistToLast;
				secondCandidates = new NodeList();
				secondCandidates.insertNode(auxNodeGSDAG);
				//finalCandidate = auxNodeGSDAG;
			}
			else if (auxDistToLast==minDistToLast){
				secondCandidates.insertNode(auxNodeGSDAG);
			}
		}
		
		//Select randomly one of the candidates with minimum distance to the last node		
		finalCandidate = (NodeGSDAG) NodeAOUID.selectRandomlyANode(secondCandidates);
	
		
		return finalCandidate;
	}
	
	

	
	protected boolean doWeApplyDinamicProgramming(int numOfExpansions, int numOfApplicationsDP) {
		// TODO Auto-generated method stub
		return ((numOfExpansions>=minimumExpansionsBeforeDP)&&
				(numOfApplicationsDP<maxNumOfApplicationsDP)&&
				(tree.getEffectiveBranchingFactor()>this.thresholdBranchingFactor));
	}
	
	
	/*protected boolean doWeApplyDinamicProgramming(int numOfExpansions,int numChangesInFirstBranchFromLastDP) {
		// TODO Auto-generated method stub
		return ((numOfExpansions>=minimumExpansionsBeforeDP)&&
				(numOfApplicationsDP<maxNumOfApplicationsDP)&&
				(tree.getEffectiveBranchingFactor()>this.thresholdBranchingFactor));
	}*/
	
	public int getNumberOfCreatedNodes(){
		return tree.getNodeList().size();
	}
	
	/**
	 * @return The expected utility of the current strategy when we are evaluating the UID
	 */
	public double getEUOfCurrentStrategy() {
		// TODO Auto-generated method stub
		return tree.root.getEUOfCurrentStrategy();
	}

	
	/**
	 * @return true iff there is nodes to expand in the AO graph
	 */
	protected boolean areThereNodesToExpand() {
	// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		NodeAOUID auxNodeSolution;
		boolean areThere;
		
		nodesOfPartialSolution = tree.obtainNodesOfPartialSolution();
		
		areThere = false;
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (int i=0;i<nodesOfPartialSolution.size()&&(areThere==false);i++){
			auxNodeSolution = nodesOfPartialSolution.get(i);
			if (tree.isOpen(auxNodeSolution)){
				areThere = true;
			}
		}
		return areThere;
		
}

	
}
