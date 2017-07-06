package elvira.inference.uids;

import java.util.ArrayList;
import java.util.Vector;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.InvalidEditException;
import elvira.NodeList;
import elvira.UID;
import elvira.inference.clustering.ShenoyShaferPropagation;
import elvira.potential.PotentialTable;
import elvira.tools.CronoNano;
import elvira.tools.PropagationStatisticsAOUID;

public class AOUIDCoalescenceAndConservative extends AOUID {
	
	
	public class StatsExploration {
		String nameOfVariable;
		int levelsExplored;
		
		public StatsExploration(){
			
		}

		public void print() {
			// TODO Auto-generated method stub
			System.out.println(nameOfVariable+": "+levelsExplored+" levels reached");
		}
		
		
		
		
		

	}

	public AOUIDCoalescenceAndConservative(UID uid) {
		super(uid);
		// TODO Auto-generated constructor stub
	}

	public void propagate(Vector paramsForCompile){
		  ArrayList<NodeAOUID> candidates;
		  NodeAOUID nodeToExpand;
		  PotentialTable finalPot;
		  int numExpansionsBeforeStat=1;
		  int numExpansions = 0;
		  //It indicates the minimum number of expansions to calculate the statistics
		  //about the EU
		  	CronoNano crono;
		  int auxTime;
		  double eu;
		  PropagationStatisticsAOUID stats;
		  int decTaken;
		  boolean applyDinamicW;
		  int step = 0;
		  ShenoyShaferPropagation ssp;
		  ArrayList<StatsExploration> statsExplor = null;
		   
		
		  
			((UID)network).createGSDAG();
			
			try {
				gsdag = new GSDAG(network);
			} catch (InvalidEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  
			
			gsdag.initializePotentials(((UID)network).getRelationList());
			
			
			
					
		  tree = new GraphAOUIDCoalescence((UID)network,gsdag);
		  
		  statistics.addTime(0);
		  
		  stats = (PropagationStatisticsAOUID)statistics;
		  stats.addExpectedUtility(0.0);
		  stats.addDecisionAndOption(-1,-1);
		  stats.addF(0.0);
		  
		  crono= new CronoNano();
		crono.start();
		  
		candidates = tree.obtainAnOnlyCandidateToExpand();
		
		while(candidates.size()>0){
		
			  if (numExpansions<numExpansionsBeforeStat){
				  numExpansions++;
				  step++;
				  System.out.println();
					System.out.println("** Step "+step);
			 
			  System.out.println("Partial optimal solution: f="+tree.root.f);
			  //System.out.println("Depth of the tree: "+tree.getDepth());
			  //System.out.println("Nodes in the tree: "+tree.getNumberOfNodes());
			  //System.out.println("Effective branching factor: "+tree.getEffectiveBranchingFactor());
			 // System.out.println("Number of candidates to expand: "+candidates.size());
			  nodeToExpand = selectCandidate(candidates, CRITERIAEXPANSION.FIRST_CHILD);
			  tree.expand(nodeToExpand);
			  tree.printValueOfFOfChildrenOfRoot();
			  statsExplor=computeStatisticsOfExplorationInEachSubtree(tree.root);
			  //printStatististicsOfExplorationInEachSubtree(statsExplor);
			  
//			candidates = tree.obtainCandidatesToExpand();
				candidates = tree.obtainAnOnlyCandidateToExpand();
			 
			  }
			  else{//Computation of statistics in the middle of the evaluation
				  numExpansions=0;
				  stats.addToLastTime(crono.getTime());
				  crono.stop();
				  eu = getEUOfCurrentStrategy();
				  System.out.println("The EU of the current strategy is:"+eu);
				  stats.addExpectedUtility(eu);
				  stats.addF(tree.root.f);
				  decTaken = getFirstDecisionTakenInTheTreeConservatively(statsExplor);
				  stats.addDecisionAndOption(decTaken,-1);
				  crono.start();
				  
			  }
		  }
		  
		  finalPot = new PotentialTable();
		  finalPot.setValue(tree.root.f);
		  //Statistics
		  statistics.setFinalExpectedUtility(finalPot);
		  System.out.println("Partial optimal solution: f="+tree.root.f);
		  System.out.println(getNumberOfCreatedNodes()+" nodes were created by the algorithm AO*");
		  stats.addToLastTime(crono.getTime());
		  crono.stop();
		  eu = getEUOfCurrentStrategy();
		  stats.addExpectedUtility(eu);
		  decTaken = getFirstDecisionTakenInTheTreeConservatively(statsExplor);
		  stats.addDecisionAndOption(decTaken,-1);
		  stats.addF(tree.root.f);
		  System.out.println("The EU of the current strategy is:"+eu);
		  stats.setCreatedNodes(getNumberOfCreatedNodes());
		  stats.printDecisionToTakeInEachStep();
		  return;
		  
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

	private void printStatististicsOfExplorationInEachSubtree(ArrayList<StatsExploration> arrayStats) {
		
		System.out.println("Number of levels reached for exploration of the children of the root:");
		for (StatsExploration stats:arrayStats){
		
			stats.print();
		}
		
	}

	private int getFirstDecisionTakenInTheTreeConservatively(ArrayList<StatsExploration> statsExplor) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
//		 TODO Auto-generated method stub
		int first=-1;
		NodeAOUID bestChild;
		NodeList children;
		boolean found=false;
		NodeAOUID auxChild;
		NodeAOUID root;
		ArrayList<NodeAOUID> childrenMoreExplored;
		int maxLevels;
		root = tree.root;
		int auxLevels;

		
		switch(root.type){
		case CHANCE:
			//We don't have to select any decision
			first = -1;
			break;
		case DECISION:
			bestChild = root.bestChildInPartialSolution;
			children = root.getChildrenNodes();
			//To look for the index of the best child
			for (int i=0;(i<children.size())&&(found==false);i++){
				auxChild = (NodeAOUID)children.elementAt(i);
				if (auxChild==bestChild){
					first = i;
					found = true;
				}
			}
			break;
		
		case BRANCH:
			//computeStatisticsOfExplorationInEachSubtree(root);
			bestChild = root.bestChildInPartialSolution;
			children = root.getChildrenNodes();
			
			maxLevels = 0;
		//Look for the children more explored
			childrenMoreExplored = new ArrayList();
			for (int i=0;(i<children.size());i++){
				auxChild = (NodeAOUID)children.elementAt(i);
				
				auxLevels = statsExplor.get(i).levelsExplored;
				if (auxLevels>maxLevels){//We improve newF
					childrenMoreExplored = new ArrayList();
					childrenMoreExplored.add(auxChild);
					maxLevels = auxLevels;
				}
				else if (auxLevels==maxLevels){//We have a tie in newF
					childrenMoreExplored.add(auxChild);
				}
			}
			
			
			
			
			//To look for the index of the best child among children more explored
			for (int i=0;(i<childrenMoreExplored.size())&&(found==false);i++){
				auxChild = (NodeAOUID)childrenMoreExplored.get(i);
				if (auxChild==bestChild){
					first = i;
					found = true;
				}
			}
			break;
		}
		return first;
	}

	private ArrayList<StatsExploration> computeStatisticsOfExplorationInEachSubtree(NodeAOUID root) {
		NodeList children;
		StatsExploration auxStats;
		ArrayList<StatsExploration> arrayStats;
		NodeAOUID auxNode;
		
		arrayStats = new ArrayList();
		
		children = root.getChildrenNodes();
		
		
		for (int i=0;(i<children.size());i++){
			auxStats = new StatsExploration();
			auxNode = (NodeAOUID)(children.elementAt(i));
			auxStats.levelsExplored = auxNode.getDepth();
			auxStats.nameOfVariable = auxNode.nameOfVariable;
			arrayStats.add(auxStats);
		}
		
		return arrayStats;
		// TODO Auto-generated method stub
		
	}
	
	
/*	
	private void performExpansionsInEachBranch(int expansionsInEachBranch) {
		// TODO Auto-generated method stub
					int	numChildrenRootGSDAG;
					ArrayList<NodeAOUID> candidates;
					NodeList childrenOfRoot;
					NodeAOUID root;
					int numChildrenRoot;
					boolean areThereMoreNodesToExpand;
					NodeAOUID nodeToExpand;
					NodeAOUID auxChild;
					
					root = (NodeAOUID) tree.root;
					
					childrenOfRoot = root.getChildrenNodes();
					
					numChildrenRoot = childrenOfRoot.size();
					
										
					if ((numChildrenRoot==0)||(root.type==NodeAOUID.TypeOfNodeAOUID.CHANCE)){
						//We are in the initial stat or the root is chance, so we compute
						//the list of candidates in the traditional way
//						candidates = tree.obtainCandidatesToExpand();
						candidates = tree.obtainAnOnlyCandidateToExpand();
						nodeToExpand = selectCandidate(candidates);
						tree.expand(nodeToExpand);
					}
					else{
						for (int i=0;i<numChildrenRoot;i++){
							auxChild = (NodeAODinamicUID) childrenOfRoot.elementAt(i);
							//We have to expand 'expansionsInEachBranch' nodes in each branch
							candidates = tree.obtainAnOnlyCandidateToExpand();
							areThereMoreNodesToExpand = candidates.size()>0;
							
							for (int j=0;(j<expansionsInEachBranch)&&areThereMoreNodesToExpand;j++){
								nodeToExpand = selectCandidate(candidates);
								tree.expand(nodeToExpand);
								candidates = tree.obtainAnOnlyCandidateToExpand();
								areThereMoreNodesToExpand = candidates.size()>0;
							}
							
						}
					}
					
		
	}*/

	

	  
}
