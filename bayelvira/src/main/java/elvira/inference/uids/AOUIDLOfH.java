package elvira.inference.uids;


	

	import java.io.IOException;
import java.util.ArrayList;
	import java.util.TreeSet;
	import java.util.Vector;

	import elvira.Bnet;
	import elvira.Evidence;
	import elvira.InvalidEditException;
import elvira.Network;
import elvira.NodeList;
	import elvira.Relation;
	import elvira.RelationList;
	import elvira.UID;
	import elvira.inference.Propagation;
import elvira.inference.clustering.ShenoyShaferPropagation;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.parser.ParseException;
	import elvira.potential.Potential;
	import elvira.potential.PotentialTable;
	import elvira.tools.CronoNano;
import elvira.tools.PropagationStatisticsAOUID;

	/**
	 * @author Manolo
	 *
	 */
	/**
	 * @author Manolo
	 *
	 */
	public class AOUIDLOfH extends AOUID {
		GraphAOUIDLOfH tree;
		private GSDAG gsdag;
		
				
		 /** Creates a new instance of BranchBound */
		  public AOUIDLOfH(UID uid) {
			  super(uid);
		    network = uid;
		    
		    statistics = new PropagationStatisticsAOUID();
		    
		    	    
		   // RelationList currentRelations = getInitialRelations();
		   
		  }
		  
		
		public void propagate(Vector paramsForCompile){
			  ArrayList<NodeAOUID> candidates;
			  NodeAOUID nodeToExpand;
			  PotentialTable finalPot;
			  int numExpansionsBeforeStat=20;
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
			   
			
			  
				((UID)network).createGSDAG();
				
				try {
					gsdag = new GSDAG(network);
				} catch (InvalidEditException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  
				
				gsdag.initializePotentials(((UID)network).getRelationList());
				
				//lp = constructJunctionTree((UID)network);
				//ssp = constructShenoyShaferPropagation((UID)network);
				ssp = null;
				
				
				//applyDinamicW = (Boolean) paramsForCompile.get(1);
				applyDinamicW = false;
			
			  tree = new GraphAOUIDLOfH((UID)network,gsdag,applyDinamicW,ssp);
			  
			  statistics.addTime(0);
			  
			  stats = (PropagationStatisticsAOUID)statistics;
			  stats.addExpectedUtility(0.0);
			  stats.addDecisionAndOption(-1,-1);
			  
			  crono= new CronoNano();
			crono.start();
			  
			  //candidates = tree.obtainCandidatesToExpand();
			candidates = tree.obtainAnOnlyCandidateToExpand();
			  
			  while(candidates.size()>0){
				  if (numExpansions<numExpansionsBeforeStat){
					  numExpansions++;
					  step++;
						//System.out.println("** Step "+step);
				 
				  System.out.println("Partial optimal solution: f="+tree.root.f);
				 // System.out.println("Depth of the tree: "+tree.getDepth());
				  //System.out.println("Nodes in the tree: "+tree.getNumberOfNodes());
				 // System.out.println("Effective branching factor: "+tree.getEffectiveBranchingFactor());
				 // System.out.println("Number of candidates to expand: "+candidates.size());
				  nodeToExpand = selectCandidate(candidates);
				  tree.expand(nodeToExpand);
				  tree.printValueOfFOfChildrenOfRoot();
//				candidates = tree.obtainCandidatesToExpand();
					candidates = tree.obtainAnOnlyCandidateToExpand();
				  }
				  else{//Computation of statistics in the middle of the evaluation
					  numExpansions=0;
					  stats.addToLastTime(crono.getTime());
					  crono.stop();
					  //eu = getEUOfCurrentStrategy();
					  eu = 0.0;
					  //System.out.println("The EU of the current strategy is:"+eu);
					  stats.addExpectedUtility(eu);
					  decTaken = getFirstDecisionTakenInTheTree();
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
			  //eu = getEUOfCurrentStrategy();
			  eu = 0.0;
			  stats.addExpectedUtility(eu);
			  decTaken = getFirstDecisionTakenInTheTree();
			  stats.addDecisionAndOption(decTaken,-1);
			  System.out.println("The EU of the current strategy is:"+eu);
			  stats.setCreatedNodes(getNumberOfCreatedNodes());
			  return;
			  
		  }
		  
		 
/*		private LazyPropagation constructJunctionTree(UID uid) {
			// TODO Auto-generated method stub
			Bnet b;
			JoinTree j;
			double[] lp;
			double[] llp;
		    int[] ls;
		    boolean[] sortAndBound;
		    int m;
		    LazyPropagation lazyProp;
		    
			b = constructABayesianNetworkFromUID(uid);
			
			lazyProp = new LazyPropagation(b);
			
			return lazyProp;
		}*/

		


	


		private int getFirstDecisionTakenInTheTree() {
			return tree.getFirstDecisionMadeInTheTree();
			
		}
			



		 //Select the candidate to expand when we have several possibilities
		protected NodeAOUID selectCandidate(ArrayList<NodeAOUID> candidates) {
			double fMax=Double.NEGATIVE_INFINITY;
			NodeAOUID nodeOfFMax = null;
			double auxF;
			// TODO Auto-generated method stub
			//By the moment we select any of them. For example, the first.
			for (NodeAOUID auxCandidate:candidates){
				auxF = auxCandidate.getF();
				if (auxF>fMax){
					fMax=auxF;
					nodeOfFMax = auxCandidate;
				}
			}
			return nodeOfFMax;
			
			
			
			
			
		}
		
	/*	 //Select the candidate to expand when we have several possibilities
		protected NodeAOUID selectCandidate(ArrayList<NodeAOUID> candidates) {
			double minDepth=Double.POSITIVE_INFINITY;
			NodeAOUID nodeOfMinDepth = null;
			double auxDepth;
			// TODO Auto-generated method stub
			//By the moment we select any of them. For example, the first.
			for (NodeAOUID auxCandidate:candidates){
				auxDepth = auxCandidate.getInstantiations().size();
				if (auxDepth<minDepth){
					minDepth = auxDepth;
					nodeOfMinDepth = auxCandidate;
				}
			}
			return nodeOfMinDepth;
		}*/
		
		public int getNumberOfCreatedNodes(){
			return tree.getNodeList().size();
		}
		
		
	}


