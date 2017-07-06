package elvira.inference.uids;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Vector;

import elvira.Bnet;
import elvira.Configuration;
import elvira.Evidence;
import elvira.InvalidEditException;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.Propagation;
import elvira.inference.clustering.ShenoyShaferPropagation;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.inference.uids.AnytimeUpdKAdmissBreadthSearch.NodeAOUID_Any_Upd_K_Adm_Breadth;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.tools.CronoNano;
import elvira.tools.PropagationStatisticsAOUID;
import elvira.tools.statistics.analysis.Stat;

public class AOUID extends Propagation {
	protected GraphAOUID tree;
	protected GSDAG gsdag;
	
	
	protected enum CRITERIAEXPANSION {VALUE_F, FIRST_CHILD}
	
	 /**
     * used for depuration
     */
     public static boolean debug = false;

	
	 /** Creates a new instance of BranchBound */
	  public AOUID(UID uid) {
	    network = uid;
	    
	    statistics = new PropagationStatisticsAOUID();
	    
	    	    
	   // RelationList currentRelations = getInitialRelations();
	   
	  }
	  
		public void propagate(Vector paramsForCompile){
			   
			preparateGraphsForPropagation();
			  
			propagateAfterCreatingGraphs(paramsForCompile);
	  }
		  
	
	public void propagateAfterCreatingGraphs(Vector paramsForCompile){
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
		  int decMade;
		  int optChosen;
		  boolean applyDinamicW;
		  int step = 0;
		  ShenoyShaferPropagation ssp;
		  int randomDec;
		  int randomOpt;
		  ArrayList<Integer> numStates;
		  ArrayList<Integer> accumNumStates;
		  boolean isTimeFinished=false;
		  //Configuration configuration;
		  //Variable that says if we compute the EU. It's set to false
		  //to have the results of the experiments earlier.
		  boolean computeEU;
		   
		  
		  numStates = gsdag.getNumStatesToChooseFirstInGSDAG();
		  
		  accumNumStates = getAccumulativeStatesEachDecisionFirstBranch(numStates);
		  
		  computeEU = (Boolean)(paramsForCompile.get(3));
		  
		  		  
		  //configuration = (Configuration)(paramsForCompile.get(4));
		  
		  //initializePotentialsInGSDAG(configuration);
		  tree.setDebug(debug);
		  
		  statistics.addTime(0);
		  
		  stats = (PropagationStatisticsAOUID)statistics;
		  
		  //EU of the strategy
		  if (computeEU){
			  eu = getEUOfCurrentStrategy();
		  }
		  else{
			  eu = 0.0;
		  }
		  stats.addExpectedUtility(eu);
		  stats.addF(tree.root.f);
		  
		  randomDec =(int) Math.floor((Math.random()*gsdag.root.getChildren().size()));
		  randomOpt = (int) Math.floor((Math.random()*Stat.sum(numStates)));
		  stats.addDecisionAndOption(randomDec,randomOpt);
		  //stats.addDecisionToTake(-1);
		  
		  //Bound of time
		  Double limitTime = (Double) paramsForCompile.get(2);
		  
		  crono= new CronoNano();
		crono.start();
		  
		  //candidates = tree.obtainCandidatesToExpand();
		candidates = tree.obtainAnOnlyCandidateToExpand();
		  
		  while((candidates.size()>0)&&(isTimeFinished==false)){
			  if (numExpansions<numExpansionsBeforeStat){
				  numExpansions++;
				  step++;
				  
					 System.out.println("** Step "+step);
					 if (isDebug()){
					 tree.root.printEstimates();
					 eu = getEUOfCurrentStrategy();
					 System.out.println("AOUID: The EU of the current strategy is:"+eu);
				  }
			 
					 
			  
			  //System.out.println("Depth of the tree: "+tree.getDepth());
			  //System.out.println("Nodes in the tree: "+tree.getNumberOfNodes());
			  //System.out.println("Effective branching factor: "+tree.getEffectiveBranchingFactor());
			 // System.out.println("Number of candidates to expand: "+candidates.size());
			  nodeToExpand = selectCandidate(candidates,CRITERIAEXPANSION.VALUE_F);
			  tree.expand(nodeToExpand);
			  if (isDebug()) tree.printValueOfFOfChildrenOfRoot();
//			candidates = tree.obtainCandidatesToExpand();
				candidates = tree.obtainAnOnlyCandidateToExpand();
			  }
			  else{//Computation of statistics in the middle of the evaluation
				  numExpansions=0;
				  stats.addToLastTime(crono.getTime());
				  crono.stop();
				  
				  isTimeFinished = ((Double)stats.getTimes().lastElement())>=limitTime;
				  if (computeEU){
					  eu = getEUOfCurrentStrategy();
				  }
				  else{
					  eu = 0.0;
				  }
				  //if (isDebug()){
					  if (computeEU) System.out.println("AOUID: The EU of the current strategy is:"+eu);
				  //}
				  stats.addExpectedUtility(eu);
				  stats.addF(tree.root.f);
				 //Decision and option
				  decMade = getFirstDecisionMadeInTheTree();
				  optChosen = getFirstOptionChosenInTheTree(accumNumStates);
				  stats.addDecisionAndOption(decMade,optChosen);
				  crono.start();
				  
			  }
		  }
		  if (isTimeFinished==false){
			  System.out.println("The evaluation has ended finding an approximation, but we had more time to think what to do");
		  }
		  else{
			  System.out.println("The evaluation has been stopped because we didn't have more time to think");
		  }
		  finalPot = new PotentialTable();
		  finalPot.setValue(tree.root.f);
		  //Statistics
		  statistics.setFinalExpectedUtility(finalPot);
		  tree.root.printEstimates();
		  System.out.println(getNumberOfCreatedNodes()+" nodes were created by the algorithm AO*");
		  stats.addToLastTime(crono.getTime());
		  crono.stop();
		  
		  eu = getEUOfCurrentStrategy();
		  
		  stats.addExpectedUtility(eu);
//		Decision and option
		  decMade = getFirstDecisionMadeInTheTree();
		  optChosen = getFirstOptionChosenInTheTree(accumNumStates);
		  stats.addDecisionAndOption(decMade,optChosen);
		  stats.addF(tree.root.f);

		  System.out.println("MEU of the current strategy is:"+eu);
		  stats.setCreatedNodes(getNumberOfCreatedNodes());
		  return;
		  
	  }
	
	  private void initializePotentialsInGSDAG(Configuration configuration) {
			// TODO Auto-generated method stub
			  gsdag.initializePotentials(network.getRelationList(),configuration);
			
		}
	

	 
	/**
	 * @param numStates
	 * @return The base that accumulates the sum of the previous elements in numStates. It's used to compute 
	 * the option more efficiently.
	 */
	protected ArrayList<Integer> getAccumulativeStatesEachDecisionFirstBranch(ArrayList<Integer> numStates) {
		// TODO Auto-generated method stub
		ArrayList<Integer> accum;
		
		accum = new ArrayList();
		
		accum.add(0);
		for (int i=1;i<numStates.size();i++){
			accum.add(accum.get(i-1)+numStates.get(i-1));
		}
		return accum;
	}

	protected int getFirstOptionChosenInTheTree(ArrayList<Integer> accumNumStates) {
		// TODO Auto-generated method stub
		return tree.getFirstOptionChosenInTheTree(accumNumStates);
	}

	private void preparateGraphsForPropagation() {
		boolean applyDynamicW;
		  //It indicates the minimum number of expansions to calculate the statistics
		  //about the EU
	
		   
		
		// TODO Auto-generated method stub
		((UID)network).createGSDAG();
		
		try {
			gsdag = new GSDAG(network);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
		
		gsdag.initializePotentials(((UID)network).getRelationList());
		
		
		
		//applyDinamicW = (Boolean) paramsForCompile.get(1);
		applyDynamicW = false;
		
	  tree = new GraphAOUID((UID)network,gsdag,applyDynamicW);
	}


	protected int getFirstDecisionMadeInTheTree() {
		return tree.getFirstDecisionMadeInTheTree();
		
	}
		


	/**
	 * @return The expected utility of the current strategy when we are evaluating the UID
	 */
	public double getEUOfCurrentStrategy() {
		// TODO Auto-generated method stub
		return tree.root.getEUOfCurrentStrategy();
	}

	
	/**
	 * @return The expected utility of the current strategy when we are evaluating the UID, but considering that in the unexplored part
	 * of the GSDAG we apply dynamic programming over the GSDAG
	 */
	public double getEUOfCurrentStrategyDPGSDAG() {
		// TODO Auto-generated method stub
		return tree.root.getEUOfCurrentStrategyDPGSDAG();
	}
	 //Select the candidate to expand when we have several possibilities
	protected NodeAOUID selectCandidate(ArrayList<?> candidates,CRITERIAEXPANSION criteria) {
		double fMax=Double.NEGATIVE_INFINITY;
		NodeAOUID candidate = null;
		double auxF;
		
		switch (criteria){
		case VALUE_F:
			NodeAOUID nodeOfFMax=null;
		// TODO Auto-generated method stub
		//By the moment we select any of them. For example, the first.
		for (Object auxCandidate:candidates){
			auxF = ((NodeAOUID) auxCandidate).getF();
			if (auxF>fMax){
				fMax=auxF;
				nodeOfFMax = (NodeAOUID) auxCandidate;
			}
		}
		candidate = nodeOfFMax;
		break;
		
		case FIRST_CHILD:
			
			
			
			if ((candidates==null)||(candidates.size()==0)){
				candidate = null;
			}
			else{
				candidate = (NodeAOUID) candidates.get(0);
			}
			
		}
		return candidate;
		
		
		
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
	
	protected ShenoyShaferPropagation constructShenoyShaferPropagation(UID uid) {
		// TODO Auto-generated method stub
		Bnet b=null;
	    ShenoyShaferPropagation ssp;
	    NodeList interest;
	    Evidence initialObs;
	    boolean query;
	    
	    b = constructABayesianNetworkFromUID(uid);
	    
	    try {
			b.save("D:\\bayelvira2\\bnetForSSP.elv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*try {
			b = (Bnet)(Network.read("C:\\bayelvira2\\bnetForSSP.elv"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    
	    initialObs = new Evidence();
	    
		query = false;
		
		interest = b.getNodeList().copy();
		
	    ssp = new ShenoyShaferPropagation(b,initialObs,query,interest);
	    
	    return ssp;
	}
	
	private Bnet constructABayesianNetworkFromUID(UID uid) {
		// TODO Auto-generated method stub
		CooperPolicyNetwork cpn;
		
		cpn= new CooperPolicyNetwork();
		
		cpn.setStructureOfPNFrom(uid);
		
		cpn.setRelationsOfChanceNodesFrom(uid);
		
		cpn.setRandomPoliciesAndRelationsOfDecisionNodesFrom(uid);
		
		return cpn;
	}

	public static boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	protected double getEUOfCurrentStrategyNDecs(int numDecisionsPlotMEU) {
		// TODO Auto-generated method stub
		return tree.root.getEUOfCurrentStrategyNDecs(numDecisionsPlotMEU);
	}
	
	/**
	 * @param levelDT The level of the decisions in the decision tree
	 * @return
	 */
	protected double getProportionDecisionsRight(int levelDT) {
		// TODO Auto-generated method stub
		return tree.root.getProportionDecisionsRight(levelDT);
	}
}
