package elvira.inference.uids.AnytimeUpdKAdmissBreadthSearch;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import elvira.Configuration;
import elvira.InvalidEditException;
import elvira.Node;
import elvira.NodeList;
import elvira.UID;
import elvira.inference.clustering.ShenoyShaferPropagation;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.GeneratorUIDs;
import elvira.inference.uids.GraphAOUID;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.Anytime.GraphAOUID_Anytime;
import elvira.inference.uids.Anytime.AOUID_Anytime.HeuristicForSearching;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.AOUID_Any_Upd_K_Adm;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.GraphAOUID_Any_Upd_K_Adm;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.NodeAOUID_Any_Upd_K_Adm;
import elvira.potential.PotentialTable;
import elvira.tools.CronoNano;
import elvira.tools.PropagationStatisticsAOUID;
import elvira.tools.statistics.analysis.Stat;

/**
 * @author Manolo_Luque
 * This class implements an anytime algorithm for UIDs with the same features:
 * - Updating of the k value (We can use it, but now it is disabled)
 * - Admissible search: It means the optimal solution at the end of the evaluation
 * - Breadth search: The option selected as the best is considered when a level of the decision tree has
 * been completed.
 */
public class AOUID_Any_Upd_K_Adm_Breadth extends AOUID_Any_Upd_K_Adm {

	private boolean usingGSDAGEvaluatedByDP;


	public AOUID_Any_Upd_K_Adm_Breadth(UID uid) {
		super(uid);
		// TODO Auto-generated constructor stub
	}
	
	public void preparateGraphsForPropagation(Vector paramsForCompile) {
		boolean applyDynamicW;
		HeuristicForSearching heur;
		double k_chance;
		Configuration configuration;
		boolean chooseRandomlyK;
		Random r;
		double minRandomlyK;
		double maxRandomlyK;
		GSDAG auxGSDAG;
		
		  //It indicates the minimum number of expansions to calculate the statistics
		  //about the EU
	
		   
		
		// TODO Auto-generated method stub
		auxGSDAG = (GSDAG) (paramsForCompile.get(9));
		
		usingGSDAGEvaluatedByDP = (auxGSDAG!=null);
		
		if (usingGSDAGEvaluatedByDP==false) {
			//The GSDAG is created to perform the evaluation

			((UID) network).createGSDAG();

			try {
				gsdag = new GSDAG(network);
			} catch (InvalidEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gsdag.initializePotentials(((UID)network).getRelationList());
		} else {
			//The GSDAG is given by paramsForCompile
			gsdag = auxGSDAG;
		}
	  
		
		
		
		/*gsdag.obtainMinSetOfVarsCoal();
		System.out.println("*********** Let us see the gsdag *************");
		gsdag.print();*/
		
		heur = (HeuristicForSearching) paramsForCompile.get(0);
		
		//applyDinamicW = (Boolean) paramsForCompile.get(1);
		
		
		
		configuration = (Configuration) paramsForCompile.get(4);
		
		chooseRandomlyK = (Boolean) paramsForCompile.get(5);
		if (chooseRandomlyK){
			minRandomlyK = (Double) paramsForCompile.get(6);
			maxRandomlyK = (Double) paramsForCompile.get(7);
			r = new Random();
			k_chance = minRandomlyK+r.nextDouble()*(maxRandomlyK-minRandomlyK);
			System.out.println("Initial K selected randomly: "+k_chance);
		}
		else{
			k_chance = (Double) paramsForCompile.get(1); 
		}

		
  
	//tree = new GraphAOUID_Anytime((UID)network,gsdag,heur,k_chance);
		tree = new GraphAOUID_Any_Upd_K_Adm_Breadth((UID)network,gsdag,heur,k_chance,configuration);
		
	}
	
	public void propagateAfterCreatingGraphs(Vector paramsForCompile){
		 // NodeAOUID candidate;
		  
		  PotentialTable finalPot;
		  int numExpansionsBeforeStat=1;
		  int numExpansions = 0;
		  //It indicates the minimum number of expansions to calculate the statistics
		  //about the EU
		  	CronoNano crono;
		  	double eu;
		  	

		  double f;
		  PropagationStatisticsAOUID stats;
		  int decMade;
		  int decMadeSaved;
		  int optChosen;
		  int optChosenSaved;
		  

		  int step = 0;

		  int randomDec;
		  int randomOpt;
		  ArrayList<Integer> numStates;
		  ArrayList<Integer> accumNumStates;
		  boolean isTimeFinished=false;
		  //Variable that says if we compute the EU. It's set to false
		  //to have the results of the experiments earlier.
		  boolean computeEU;
		  ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> nodesToExpand;
		  ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> childrenOfExpanded;
		  NodeAOUID_Any_Upd_K_Adm_Breadth nodeToExpand;
		  ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> candidates;
		  boolean endOfLevel;
		   
		  
		  numStates = gsdag.getNumStatesToChooseFirstInGSDAG();
		  
		  accumNumStates = getAccumulativeStatesEachDecisionFirstBranch(numStates);
		  
		  computeEU = (Boolean)(paramsForCompile.get(3));
		  
		  //initializePotentialsInGSDAG(configuration);
		  GraphAOUID.setDebug(debug);
		  
		  statistics.addTime(0);
		  
		  stats = (PropagationStatisticsAOUID)statistics;
		  
		  
		  Boolean beConservative = (Boolean) paramsForCompile.get(8);
		  
		  Integer initialNumDecisionsPlotMEU = (Integer) paramsForCompile.get(10);
		  Integer finalNumDecisionsPlotMEU = (Integer) paramsForCompile.get(11);
		  Integer finalNumDecsRight = (Integer) paramsForCompile.get(12);
		  
		  ((GraphAOUID_Anytime) tree).setUpdateK((Boolean) paramsForCompile.get(13));
		  
		  
		  randomDec =(int) Math.floor((Math.random()*gsdag.getRoot().getChildren().size()));
		  randomOpt = (int) Math.floor((Math.random()*Stat.sum(numStates)));
		  decMadeSaved = randomDec;
		  optChosenSaved = randomOpt;
		  
		  stats.initializeExpectedUtilityNDecs(initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU);
		  stats.initializeNumDecsRight(finalNumDecsRight);
		  
		  storeStatisticsDecisionAndOption(false,beConservative,computeEU,stats,decMadeSaved,optChosenSaved,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU,finalNumDecsRight);
		  
		  
		  //Bound of time
		  Double limitTime = (Double) paramsForCompile.get(2);
		  
		  
		  
		  
		  
		  crono= new CronoNano();
		crono.start();
		  
		crono.stop();
		nodesToExpand = new ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth>();
		nodesToExpand.add((NodeAOUID_Any_Upd_K_Adm_Breadth) tree.getRoot());
		childrenOfExpanded = new ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth>();
		candidates = obtainAnOnOnlyCandidateToExpandAndRemoveItFrom(nodesToExpand);

		crono.start();
		  
		  while((candidates.size()>0)&&(isTimeFinished==false)){
			  if (numExpansions<numExpansionsBeforeStat){
				  numExpansions++;
				  step++;
				  
					 System.out.println("** Step "+step);
					 if (isDebug()){
					 tree.getRoot().printEstimates();
					 eu = getEUOfCurrentStrategy();

					 System.out.println("AOUID: The EU of the current strategy is:"+eu);
				  }
			 
			  nodeToExpand = (NodeAOUID_Any_Upd_K_Adm_Breadth) selectCandidate(candidates, CRITERIAEXPANSION.FIRST_CHILD);
			  //Expand the node
			  tree.expand(nodeToExpand);
			  //Add its children to the next level of nodes to be expanded
			  childrenOfExpanded.addAll(nodeToExpand.getChildrenArrayList());
			  
			  
			  if (isDebug()){
				  tree.printValueOfFOfChildrenOfRoot();
			  }
			  
			  candidates = obtainAnOnOnlyCandidateToExpandAndRemoveItFrom(nodesToExpand);
			  
			  if (candidates.size()==0){//The level has been finished
				  //We force the computation of statistics to store the values when the level
				  //has been finished
				  
				  endOfLevel = true;
				  
				  if (beConservative){
					  numExpansions=0;
					  stats.addToLastTime(crono.getTime());
					  crono.stop();
					  decMadeSaved = updateDecMadeSaved(endOfLevel,beConservative,decMadeSaved,accumNumStates);
					  optChosenSaved = updateOptChosenSaved(endOfLevel,beConservative,optChosenSaved,accumNumStates);
					  storeStatisticsDecisionAndOption(endOfLevel,beConservative,computeEU,stats,decMadeSaved,optChosenSaved, initialNumDecisionsPlotMEU, finalNumDecisionsPlotMEU,finalNumDecsRight);
					  
					  isTimeFinished = ((Double)stats.getTimes().lastElement())>=limitTime;
					  
					  crono.start();
					  
				  }
				  
				  // The children of the expanded nodes are now the new
					// nodesToExpand
				  nodesToExpand = childrenOfExpanded;
				  childrenOfExpanded = new ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth>();
				  candidates = obtainAnOnOnlyCandidateToExpandAndRemoveItFrom(nodesToExpand);
				  
				  
			  }
				
			  }
			  else{//Computation of statistics in the middle of the evaluation
				  
				  numExpansions=0;
				  stats.addToLastTime(crono.getTime());
				  crono.stop();
				  endOfLevel = false;
				  isTimeFinished = ((Double)stats.getTimes().lastElement())>=limitTime;
				  decMadeSaved = updateDecMadeSaved(endOfLevel,beConservative,decMadeSaved,accumNumStates);
				  optChosenSaved = updateOptChosenSaved(endOfLevel,beConservative,optChosenSaved,accumNumStates);
				  storeStatisticsDecisionAndOption(endOfLevel,beConservative,computeEU,stats,decMadeSaved,optChosenSaved,initialNumDecisionsPlotMEU, finalNumDecisionsPlotMEU,finalNumDecsRight);
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
		  finalPot.setValue(tree.getRoot().getF());
		  //Statistics
		  statistics.setFinalExpectedUtility(finalPot);
		  tree.getRoot().printEstimates();
		  System.out.println(getNumberOfCreatedNodes()+" nodes were created by the algorithm AO*");
		  stats.addToLastTime(crono.getTime());
		  crono.stop();
		  endOfLevel = false;
		  decMadeSaved = updateDecMadeSaved(endOfLevel,beConservative,decMadeSaved,accumNumStates);
		  optChosenSaved = updateOptChosenSaved(endOfLevel,beConservative,optChosenSaved,accumNumStates);
		  storeStatisticsDecisionAndOption(endOfLevel,beConservative,computeEU,stats,decMadeSaved,optChosenSaved,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU,finalNumDecsRight);

		  System.out.println("MEU of the current strategy is:"+getEUOfCurrentStrategy());
		  stats.setCreatedNodes(getNumberOfCreatedNodes());
		  return;
		  
	  }



private int updateOptChosenSaved(boolean endOfLevel,
			Boolean beConservative, int  optChosenSaved, ArrayList<Integer> accumNumStates) {
		// TODO Auto-generated method stub
	int newOptChosen;
	 if ((beConservative==false)||endOfLevel){
		  
		newOptChosen = getFirstOptionChosenInTheTree(accumNumStates);
		if (endOfLevel){
			  if (debug) System.out.println("End of level: We save the information about the option: New option = "+ newOptChosen);
		  }
	  }
	 else{
		 newOptChosen = optChosenSaved;
	 }
	 return newOptChosen;
	}

private int updateDecMadeSaved(boolean endOfLevel, Boolean beConservative,
			int decMadeSaved, ArrayList<Integer> accumNumStates) {
		// TODO Auto-generated method stub
	int newDecMade;
	
	 if ((beConservative==false)||endOfLevel){
		  
		  newDecMade = getFirstDecisionMadeInTheTree();
		  
		  if (endOfLevel){
			  if (debug) System.out.println("End of level: We save the information about the decision: New decision = "+ newDecMade);
		  }
	 }
	 else{
		 newDecMade = decMadeSaved;
	  }
	 return newDecMade;
	 
	}

/**
 * @param endOfLevel
 * @param beConservative
 * @param computeEU
 * @param stats
 * @param decMadeSaved
 * @param optChosenSaved
 * @param numDecisionsPlotMEU Number of decisions/branches to compute euNDecs 
 * @param finalNumDecisionsPlotMEU 
 */
private void storeStatisticsDecisionAndOption(boolean endOfLevel,boolean beConservative, boolean computeEU, PropagationStatisticsAOUID stats, int decMadeSaved, int optChosenSaved,int initialNumDecisionsPlotMEU, Integer finalNumDecisionsPlotMEU,Integer finalNumDecsRight) {
		// TODO Auto-generated method stub
	double eu;
	double f;
	double euDPGSDAG;
	//double euNDecs;
	
		
		if (computeEU){
		  eu = getEUOfCurrentStrategy();
		  if (usingGSDAGEvaluatedByDP){
			  euDPGSDAG = getEUOfCurrentStrategyDPGSDAG();
			  //euNDecs = getEUOfCurrentStrategyNDecs(numDecisionsPlotMEU);
		  }
		  else{
			  euDPGSDAG = 0.0;
			  //euNDecs = 0.0;
		  }
	  }
	  else{
		  eu = 0.0;
		  euDPGSDAG = 0.0;
		  //euNDecs = 0.0;
	  }
	  
	  
	  // if (isDebug()){
	  if (computeEU) System.out.println("AOUID: The EU of the current strategy is:"+eu);
	  // }
	  
	  
	  stats.addExpectedUtility(eu);
	  stats.addExpectedUtilityDPGSDAG(euDPGSDAG);
	  storeStatisticsEUNDecs(stats,computeEU,usingGSDAGEvaluatedByDP,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU);
	  storeStatisticsPropDecsRight(stats,computeEU,usingGSDAGEvaluatedByDP,finalNumDecsRight);
	  //stats.addExpectedUtilityNDecs(euNDecs);
	  f = tree.getRoot().getF();
	  stats.addF(f);
	 // Decision and option
	 
	  stats.addDecisionAndOption(decMadeSaved,optChosenSaved);
	}





private void storeStatisticsPropDecsRight(PropagationStatisticsAOUID stats,
		boolean computeEU, boolean usingGSDAGEvaluatedByDP2,
		Integer finalNumDecsRight) {
	// TODO Auto-generated method stub
	
	double auxProp;
	ArrayList<Double> auxArrayProp;
	ArrayList<Double>[] arraysProp;

	arraysProp = stats.getPropDecsRight();

	for (int level=0;level<=finalNumDecsRight;level++){
		if (computeEU&&usingGSDAGEvaluatedByDP){
			auxProp = getProportionDecisionsRight(level);
		}
		else{
			auxProp = 0.0;
		}
		arraysProp[level].add(auxProp);
	}

	
	
}

/**
 * @param stats
 * @param computeEU
 * @param usingGSDAGEvaluatedByDP
 * @param initialNumDecisionsPlotMEU
 * @param finalNumDecisionsPlotMEU
 * It stores the statistics about the meu of the current strategy for several values of the number of decisions
 */
private void storeStatisticsEUNDecs(PropagationStatisticsAOUID stats,
		boolean computeEU, boolean usingGSDAGEvaluatedByDP,
		int initialNumDecisionsPlotMEU, int finalNumDecisionsPlotMEU) {
	double auxEU;
	ArrayList<Double> auxArrayEU;
	ArrayList<Double>[] arraysEUNDecs;
	
	arraysEUNDecs = stats.getExpectedUtilityNDecs();
	
	for (int i=initialNumDecisionsPlotMEU;i<=finalNumDecisionsPlotMEU;i++){
		if (computeEU&&usingGSDAGEvaluatedByDP){
			auxEU = getEUOfCurrentStrategyNDecs(i);
		}
		else{
			auxEU = 0.0;
		}
		int indexNumDecs = i-initialNumDecisionsPlotMEU;
		arraysEUNDecs[indexNumDecs].add(auxEU);
	}
	
}



/*
private NodeAOUID_Any_Upd_K_Adm_Breadth selectCandidate(
			ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> candidates) {
		// TODO Auto-generated method stub
		
		NodeAOUID_Any_Upd_K_Adm_Breadth candidate;
		
		if ((candidates==null)||(candidates.size()==0)){
			candidate = null;
		}
		else{
			candidate = candidates.get(0);
		}
		return candidate;
}*/



private ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> obtainAnOnOnlyCandidateToExpandAndRemoveItFrom(
			ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> nodesToExpand) {
		// TODO Auto-generated method stub
	NodeAOUID_Any_Upd_K_Adm_Breadth candidate;
	ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> candidates;
	
	candidate = anOnlyCandidateToExpandRandomlySelectedAndRemove(nodesToExpand);
	candidates = new ArrayList();
	if (candidate!=null){
		candidates.add(candidate);
		
	}
	return candidates;
	}

private NodeAOUID_Any_Upd_K_Adm_Breadth anOnlyCandidateToExpandRandomlySelectedAndRemove(
		ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> nodesToExpand) {
	// TODO Auto-generated method stub
	
		NodeAOUID_Any_Upd_K_Adm_Breadth auxNode;
		ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> nodes;
		
		nodes = new ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth>();
		for (int i=0;(i<nodesToExpand.size());i++){
			auxNode = nodesToExpand.get(i);
			if ((auxNode.isPruned()==false)&&auxNode.isOpen()){
				nodes.add(auxNode);
			}	
		}
		
		Random r=new Random();
		return chooseRandomAndRemove(nodes,r);  
}

/**
 * @param nodesToExpand List of nodes
 * @return A node to be expanded selected randomly from 'nodes'
 */
protected static NodeAOUID_Any_Upd_K_Adm_Breadth obtainANodeToBeExpandedAndRemoveItFrom(ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> nodesToExpand) {
	// TODO Auto-generated method stub
	Random r=new Random();
	if (nodesToExpand.size()>1){
		if (isDebug()) System.out.println("Breaking randomly the tie between "+nodesToExpand.size()+" children in a BRANCH/DECISION");
	}
	return chooseRandomAndRemove(nodesToExpand,r);
	
	
}


/**
 * @param nodes List of nodes where we can take several nodes randomly
 * @param r Seek to generate the random numbers
 * @return A node randomly selected, which will also be removed from 'nodes'
 */
public static NodeAOUID_Any_Upd_K_Adm_Breadth chooseRandomAndRemove(ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> nodes, Random r) {
	
	int auxRandom = 0;
	NodeAOUID_Any_Upd_K_Adm_Breadth chosenNode;
	
	
	if (nodes==null){
		chosenNode = null;
	}
	else{
		if (nodes.size()==0){
			chosenNode = null;
		}
		else{
			auxRandom = r.nextInt(nodes.size());
			chosenNode = nodes.get(auxRandom);
			nodes.remove(auxRandom);
		}
		
		
	}
	return chosenNode;

}


}
