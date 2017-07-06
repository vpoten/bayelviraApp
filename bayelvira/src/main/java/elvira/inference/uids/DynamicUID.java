package elvira.inference.uids;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.InvalidEditException;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.Propagation;
import elvira.inference.uids.NodeGSDAG.RelationsNodeGSDAG;
import elvira.inference.uids.NodeGSDAG.TypeOfNodeGSDAG;
import elvira.potential.MaxFunction;
import elvira.potential.PotentialTable;
import elvira.potential.SumFunction;
import elvira.tools.CronoNano;
import elvira.tools.PropagationStatisticsAOUID;
import elvira.tools.statistics.analysis.Stat;

//Evaluates the GSDAG with dinamic programming (Jensen and Vomleova'02)
public class DynamicUID extends Propagation{

	GSDAG gsdag;
	double utilDecs[];
	double utilOpts[];
	int optimalDec;
	int optimalOpt;
	double randomUtilDecs[];
	double randomUtilOpts[];
	private static boolean debug = true;
	/**
	 * array containing the initial euNDec for different number of decisions
	 */
	double initialEUNDecs[];

	
	 /** Creates a new instance of BranchBound */
	  public DynamicUID(UID uid) {
	    network = uid;
	    statistics = new PropagationStatisticsAOUID();
	    utilDecs = null;
	    utilOpts = null;
	  }
	  
	  /**
	 * By default method for propagating
	 */
	public void propagate(){
		boolean computeEUStrategy;
		Configuration configuration;
		Vector params;
		boolean isReferenceEvaluation;
		GSDAG gsdagReference;
		int initialNumDecisionsPlotMEU;
		int finalNumDecisionsPlotMEU;
		int finalNumDecisionsRight;
		boolean monitorStatistics;
		boolean computePolicies;
		
		
		computeEUStrategy = false;
		configuration = new Configuration();
		params = new Vector();
		isReferenceEvaluation = false;
		gsdagReference = null;
		initialNumDecisionsPlotMEU=0;
		finalNumDecisionsPlotMEU=0;
		finalNumDecisionsRight=0;
		monitorStatistics = false;
		computePolicies = true;
		
		params.add(computeEUStrategy);
		params.add(configuration);
		params.add(isReferenceEvaluation);
		params.add(gsdagReference);		
		params.add(initialNumDecisionsPlotMEU);
		params.add(finalNumDecisionsPlotMEU);
		params.add(finalNumDecisionsRight);
		params.add(monitorStatistics);
		params.add(computePolicies);
		
		
		
		  propagate(params);
	  }
	  
	  public void propagate(Vector paramsForCompile){

		  PotentialTable finalPot;
		  PropagationStatisticsAOUID stats;
		  CronoNano crono=new CronoNano();
		  double eu;
		  boolean computeEU;
		  Configuration configuration;
		  int initialNumDecisionsPlotMEU = 0;
		  int finalNumDecisionsPlotMEU = 0;
		  int finalNumDecsRight = 0;
		  GSDAG gsdagReference;
		  boolean isReferenceEvaluation;
		  boolean monitorStatistics = true;
		  Boolean computePolicies = false;
		  
		  
		((UID)network).createGSDAG();
			
			try {
				gsdag = new GSDAG(network);
			} catch (InvalidEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if ((paramsForCompile!=null)&&(paramsForCompile.size()>0)){
			//Collect the parameters of the propagation
			computeEU = (Boolean)(paramsForCompile.get(0));
			
			}
			else{
				computeEU = false;
			}
			
			if ((paramsForCompile!=null)&&(paramsForCompile.size()>1)){
				configuration = (Configuration)(paramsForCompile.get(1));
			}
			else{
				configuration = new Configuration();
			}
			
			if (paramsForCompile!=null){
			isReferenceEvaluation = (Boolean) (paramsForCompile.get(2));
			
			gsdagReference = (GSDAG) (paramsForCompile.get(3));
			
			initialNumDecisionsPlotMEU = (Integer) (paramsForCompile.get(4));
			finalNumDecisionsPlotMEU = (Integer) (paramsForCompile.get(5));
			
			finalNumDecsRight = (Integer) (paramsForCompile.get(6));
			
			if (paramsForCompile.size()>7){
				monitorStatistics = (Boolean) paramsForCompile.get(7);
			}
			else{
				monitorStatistics = true;
			}
			if (paramsForCompile.size()>8){
				computePolicies  = (Boolean) paramsForCompile.get(8);
			}
			}
			else{
				isReferenceEvaluation = true;
				gsdagReference = null;
				computePolicies = false;
			}
			
			
			
			
			initializePotentialsInGSDAG(configuration);
			
			stats = (PropagationStatisticsAOUID)statistics;
			
			stats.addTime(0);
			stats.addExpectedUtility(getEUOfCurrentStrategy(computeEU));
			stats.addF(0.0);
			stats.addDecisionAndOption(-1,-1);
			
			if ((isReferenceEvaluation==false)&&monitorStatistics){
				stats.initializeExpectedUtilityNDecs(initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU);
				stats.initializeNumDecsRight(finalNumDecsRight);
				storeStatisticsEUNDecsAndPropDecsRight(stats,computeEU,gsdagReference,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU,finalNumDecsRight);
			}
			
			crono.start();
			eliminateVariablesOfUID(crono,stats,computeEU, isReferenceEvaluation, gsdagReference,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU,finalNumDecsRight, monitorStatistics,computePolicies);
			stats.addToLastTime(crono.getTime());
			crono.stop();
			eu = getEUOfCurrentStrategy(computeEU);
			if ((isReferenceEvaluation==false)&&monitorStatistics){
				storeStatisticsEUNDecsAndPropDecsRight(stats,computeEU,gsdagReference,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU,finalNumDecsRight);
			}

			System.out.println("DP: The EU of the current strategy is:"+eu);
			finalPot = (PotentialTable)getGsdag().getRoot().getCurrentUtilityRelations().elementAt(0).getValues();
			eu = finalPot.maximumValue();
			stats.addExpectedUtility(eu);
			if ((isReferenceEvaluation==false)&&monitorStatistics){
				storeStatisticsEUNDecsAndPropDecsRight(stats,computeEU,gsdagReference,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU,finalNumDecsRight);
			}
			stats.addF(0.0);
			
			stats.addDecisionAndOption(getFirstDecisionMade(),getFirstOptionChosenInTheTree());
			
			System.out.println("MEU: "+finalPot.maximumValue());
			
			statistics.setFinalExpectedUtility(finalPot);
			
			/*if (isReferenceEvaluation==false){
			storeStatisticsInitialEUNDecs(stats,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU);
			}*/
			
			//Statistics for the experiments
			if (monitorStatistics){
			computeUtilitiesAndOptimalDecisionAndOptionForTheFirstBranch();
			computeUtilitiesRandomDecisionAndOptionForTheFirstBranch(configuration);
			}
			
			
			
	  }
	  
	  
	

		/*	private void computeInitialEUNDecs(PropagationStatisticsAOUID stats, int initialNumDec, int finalNumDec) {
		
			// TODO Auto-generated method stub
				int range;
				ArrayList<Double>[] auxEUNDecs = stats.getExpectedUtilityNDecs();
			
			range = finalNumDec - initialNumDec + 1 ;
			
			initialEUNDecs = new double [range];

			for (int i = 0; i < range; i++) {
				
				initialEUNDecs[i] = auxEUNDecs[i].get(0);

			}
			
			
			
			double auxEU;
			
			ArrayList<Double>[] arraysEUNDecs;
			
			arraysEUNDecs = stats.getExpectedUtilityNDecs();
			
			for (int i=initialNumDecisionsPlotMEU;i<=finalNumDecisionsPlotMEU;i++){
				auxEU = getEUCurrentStrategyNDecs(computeEU, gsdagReference, i);
				int indexNumDecs = i-initialNumDecisionsPlotMEU;
				
				arraysEUNDecs[indexNumDecs].add(auxEU);
				
				
			}

	}
*/

			private void storeStatisticsEUNDecs(PropagationStatisticsAOUID stats,
			boolean computeEU, GSDAG gsdagReference,
			int initialNumDecisionsPlotMEU, int finalNumDecisionsPlotMEU) {
		// TODO Auto-generated method stub
				double auxEU;
				
				ArrayList<Double>[] arraysEUNDecs;
				
				arraysEUNDecs = stats.getExpectedUtilityNDecs();
				
				for (int i=initialNumDecisionsPlotMEU;i<=finalNumDecisionsPlotMEU;i++){
					auxEU = getEUCurrentStrategyNDecs(computeEU, gsdagReference, i);
					int indexNumDecs = i-initialNumDecisionsPlotMEU;
					
					arraysEUNDecs[indexNumDecs].add(auxEU);
					
					
				}
		}


			/**
			 * @param gsdagReference 
			 * @param numDecisionsPlotMEU
			 * @return The EU for the leaves of the DT, assuming that 
			 */
	  private double getEUCurrentStrategyNDecs(boolean computeEU,
			GSDAG gsdagReference, int numDecisionsPlotMEU) {

		ArrayList<NodeGSDAGAndRelations> nearestDescsNDecs;
		NodeGSDAG auxNodeGSDAG;
		RelationList auxUtilRels;
		RelationList auxProbRels;
		double euOfDescI;
		double probSelectDescI;
		double globalEU;
		NodeGSDAGAndRelations auxNodeGSDAGAndRels;
		RelationsNodeGSDAG auxRels;

		// This variable must be referred to nodeGSDAGs and relations from
		// gsdagreference
		nearestDescsNDecs = obtainNearestDescendantsAfterDeciding(gsdag.root,
				gsdagReference.root, numDecisionsPlotMEU);

		if (computeEU) {

			globalEU = 0.0;

			for (int i = 0; i < nearestDescsNDecs.size(); i++) {
				auxNodeGSDAGAndRels = nearestDescsNDecs.get(i);
				auxRels = auxNodeGSDAGAndRels.getRelations();
				// We instantiate the utility potentials
				auxUtilRels = auxRels.getUtilityRelations().copy();
				auxProbRels = auxRels.getProbabilityRelations().copy();
				// We calculate the value of the EU for the utility potentials
				// insantiated
				euOfDescI = NodeAOUID.getEU(auxProbRels, auxUtilRels);
				auxNodeGSDAG = auxNodeGSDAGAndRels.getNodeGSDAG();
				probSelectDescI = gsdagReference.root
						.obtainProbabilityOfSelectGSDAGReference(auxNodeGSDAG);
				globalEU = globalEU + euOfDescI * probSelectDescI;
			}
		} else {
			globalEU = 0.0;
		}
		return globalEU;

	}

	private ArrayList<NodeGSDAGAndRelations> obtainNearestDescendantsAfterDeciding(
					NodeGSDAG rootGSDAG, NodeGSDAG rootGSDAGReference, int numDecisionsPlotMEU) {
				// TODO Auto-generated method stub
		
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			ArrayList<NodeGSDAGAndRelations> list;
			
			
			list = new ArrayList();
			
			auxObtainNearestDescendantsAfterDeciding(rootGSDAG,rootGSDAGReference,numDecisionsPlotMEU,list);
			
			return list;
		}


	private void auxObtainNearestDescendantsAfterDeciding(NodeGSDAG nodeGSDAG,
			NodeGSDAG nodeGSDAGReference, int numDecisionsPlotMEU,
			ArrayList<NodeGSDAGAndRelations> list) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NodeList children;
		NodeList childrenRef;
		int numDecsOrBranches;
		NodeGSDAGAndRelations aux;
		int newNumDecisionsPlotMEU;
		NodeGSDAG childINodeGSDAG;
		NodeGSDAG childINodeGSDAGReference;
			
				
			if (numDecisionsPlotMEU==0){
				//We take the first relations
				aux = new NodeGSDAGAndRelations();
				aux.setNodeGSDAG(nodeGSDAGReference);
				aux.setRelations(nodeGSDAGReference.getCurrentRelations());
				list.add(aux);
			}
			else{
				numDecsOrBranches = nodeGSDAG.getNumberOfDecisionsOrBranches();
				int numDecsOrBranchesEliminated = nodeGSDAG.getNumberOfDecisionsOrBranchesEliminated();
				if ((numDecsOrBranches>numDecisionsPlotMEU)||(numDecsOrBranchesEliminated>0)){
					
					//We look for the relations in this node
					aux = new NodeGSDAGAndRelations();
					aux.setNodeGSDAG(nodeGSDAGReference);
					aux.setRelations(nodeGSDAGReference.listRelations.get(indexOfRelationsForEUNDec(numDecisionsPlotMEU,numDecsOrBranches,numDecsOrBranchesEliminated,nodeGSDAG.type)));
						
					list.add(aux);
				}
				else{
					//We have to look for in the children
					newNumDecisionsPlotMEU = numDecisionsPlotMEU-numDecsOrBranches;
					children = nodeGSDAG.getChildrenNodes();
					childrenRef = nodeGSDAGReference.getChildrenNodes();
					if (children!=null){
						for (int i=0;i<children.size();i++){
							childINodeGSDAG = ((NodeGSDAG)children.elementAt(i));
							childINodeGSDAGReference = ((NodeGSDAG)childrenRef.elementAt(i));
							auxObtainNearestDescendantsAfterDeciding(childINodeGSDAG,childINodeGSDAGReference,newNumDecisionsPlotMEU,list);
						}
					}	
				}
			}
	


	}


	/**
	 * @param numDecisionsPlotMEU
	 * @param numDecsOrBranches
	 * @param numDecsOrBranchesEliminated
	 * @param type
	 * @return The index of the relations that must be used to compute the EUNDec. The method
	 * assumes that the parameters are referred to a nodeGSDAG where the relations must be taken from.
	 */
	private int indexOfRelationsForEUNDec(int numDecisionsPlotMEU,
			int numDecsOrBranches, int numDecsOrBranchesEliminated,
			TypeOfNodeGSDAG type) {
		int index;
		// TODO Auto-generated method stub
		if (type!=TypeOfNodeGSDAG.CHANCE){
			index = Math.min(numDecisionsPlotMEU,numDecsOrBranches-numDecsOrBranchesEliminated);
		}
		else{
			index = 0;
		}
		return index;
		
	}


	/**
	 * It computes the utilities for the decision and the options of the first branch, assuming a random strategy in the GS-DAG
	 * @param configuration 
	 */
	private void computeUtilitiesRandomDecisionAndOptionForTheFirstBranch(Configuration configuration) {
		// TODO Auto-generated method stub
		
		  
			// TODO Auto-generated method stub
		  NodeGSDAG root;
		  NodeList children;
		  int numChildren;
		  ArrayList<Integer> numStates;
		  
		  
		  	  
		  
		  root = gsdag.root;
		  
		  children = root.getChildrenNodes();
		  
		  numChildren = children.size();
		  
		  numStates = gsdag.getNumStatesToChooseFirstInGSDAG();

		  
		  
		  //Compute utildecs and optimalDec
		  switch (root.type){
		  case CHANCE:
			  computeUtilRandomDecAPrioriInformation(configuration);
			  computeUtilRandomOptAPrioriInformation(configuration);
			  break;
		  case DECISION:
			 // break;
		  case BRANCH:
			  computeUtilRandomDec(configuration);
			  computeUtilRandomOpt(configuration);
			  break;
		  }
	}


	private void computeUtilRandomOpt(Configuration configuration) {
		// TODO Auto-generated method stub
		computeUtilRandomOpt(gsdag.root,configuration);
	}


	


	/**
	 * This method computes the utility of selecting randomly a decision in the first branch/decision, but assuming
	 * also even distribution in all the policies of the GS-DAG
	 * @param childOfRoot
	 * @param configuration
	 */
	private void computeUtilRandomOpt(NodeGSDAG nodeGSDAG,
			Configuration configuration) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		RelationList instantUtilRels;
		RelationList instantProbRels;
		NodeGSDAG last;
		double util;
		int totalNumStates;
		ArrayList<Integer> numStates;
		int indexOpt;
		Configuration newConf;
		
		String nameDec;
		
		numStates = gsdag.getNumStatesToChooseFirstInGSDAG();
		
		totalNumStates = Stat.sum(numStates);
		
		last = gsdag.getLastNodeGSDAG();
		
		randomUtilOpts = new double[totalNumStates];
		
		for (int globalIndexOpt=0;globalIndexOpt<totalNumStates;globalIndexOpt++){
			//See which is the decision and the option for the instanation
			nameDec = gsdag.getDecisionToChooseFirstInGSDAG(nodeGSDAG, globalIndexOpt);
			indexOpt = gsdag.getStateToChooseFirstInGSDAG(nodeGSDAG,globalIndexOpt);
			newConf = configuration.duplicate();
			newConf.insert((FiniteStates) network.getNode(nameDec),indexOpt);
			
			//Instantiate the potentials
			instantUtilRels = NodeAOUID.instantiateRelations(last.getCurrentUtilityRelations(),newConf);
			instantProbRels = NodeAOUID.instantiateRelations(last.getCurrentProbabilityRelations(),newConf);
			
			util = NodeAOUID.heuristicEvenDistributionForDecisions(instantProbRels, instantUtilRels);
			
			randomUtilOpts[globalIndexOpt]=util;
		}
		
	}


	private void computeUtilRandomDec(Configuration configuration) {
		// TODO Auto-generated method stub
		computeUtilRandomDec(gsdag.root,configuration);
	}


	private void computeUtilRandomOptAPrioriInformation(Configuration configuration) {
		// TODO Auto-generated method stub
		  NodeGSDAG root;
		  NodeGSDAG childOfRoot;

	
		 //In chance nodes we consider the decision or branch is the child of root
	  root = gsdag.root;
	  childOfRoot = (NodeGSDAG) root.getChildrenNodes().elementAt(0);
		  
	  computeUtilRandomOpt(childOfRoot,configuration);

	}


	private void computeUtilRandomDecAPrioriInformation(Configuration configuration) {
		// TODO Auto-generated method stub
		
		// TODO Auto-generated method stub
		  NodeGSDAG root;
		  NodeGSDAG childOfRoot;

	
		 //In chance nodes we consider the decision or branch is the child of root
	  root = gsdag.root;
	  childOfRoot = (NodeGSDAG) root.getChildrenNodes().elementAt(0);
		  
	  computeUtilRandomDec(childOfRoot,configuration);
		
	}


	/**
	 * This method computes the utility of selecting randomly a decision in the first branch/decision, but assuming
	 * also even distribution in all the policies of the GS-DAG
	 * @param childOfRoot
	 * @param configuration
	 */
	private void computeUtilRandomDec(NodeGSDAG nodeGSDAG, Configuration configuration) {
		// TODO Auto-generated method stub
		RelationList instantUtilRels;
		RelationList instantProbRels;
		NodeGSDAG last;
		double util;
		int numDecs;
		
		last = gsdag.getLastNodeGSDAG();
		
		instantUtilRels = NodeAOUID.instantiateRelations(last.getCurrentUtilityRelations(),configuration);
		instantProbRels = NodeAOUID.instantiateRelations(last.getCurrentProbabilityRelations(),configuration);
		
		util = NodeAOUID.heuristicEvenDistributionForDecisions(instantProbRels, instantUtilRels);
		
		numDecs = nodeGSDAG.getChildren().size();
		
		randomUtilDecs = new double[nodeGSDAG.getChildren().size()];

		//randomUtilDecs is identical for the different decisions
		for(int i=0;i<numDecs;i++){
			randomUtilDecs[i]=util;
		}
		
		
	}


	
	private void initializePotentialsInGSDAG(Configuration configuration) {
		// TODO Auto-generated method stub
		  gsdag.initializePotentials(network.getRelationList(),configuration);
		
	}


	private double getEUOfCurrentStrategy(boolean computeEU) {
		// TODO Auto-generated method stub
		  double eu;
		  if (computeEU){
			  eu = getEUOfCurrentStrategy();
		  }
		  else{
			  eu = 0.0;
		  }
		 return eu;
	}


	private int getFirstOptionChosenInTheTree() {
		// TODO Auto-generated method stub
		return -1;
	}


	private void computeUtilitiesAndOptimalDecisionAndOptionForTheFirstBranch() {
		// TODO Auto-generated method stub
		  NodeGSDAG root;
		  NodeList children;
		  int numChildren;
		  ArrayList<Integer> numStates;
		  
		  
		  optimalDec = -1;
		  
		  
		  root = gsdag.root;
		  
		  children = root.getChildrenNodes();
		  
		  numChildren = children.size();
		  
		  numStates = gsdag.getNumStatesToChooseFirstInGSDAG();

		  utilDecs = new double[numChildren];
		  utilOpts = new double[Stat.sum(numStates)];
		
		  
		  
		  //Compute utildecs and optimalDec
		  switch (root.type){
		  case CHANCE:
			  computeUtilAndOptimalDecAPrioriInformation();
			  computeUtilAndOptimalOptAPrioriInformation();
			  break;
		  case DECISION:
			 // break;
		  case BRANCH:
			  computeUtilAndOptimalDec();
			  computeUtilAndOptimaOpt();
			  break;
		  }
	}
			  
	





	private void computeUtilAndOptimalDec() {
		// TODO Auto-generated method stub


		computeUtilAndOptimalDec(gsdag.root);
	
		
	}
	
	private void computeUtilAndOptimalDec(NodeGSDAG nodeDecOrBranch) {
		// TODO Auto-generated method stub
		  //NodeGSDAG root;


		  RelationList relChildren;
		  Relation auxRel;
		  double auxUtil;
		  double maxUtil;
	
		  
	  //root = gsdag.root;
		  
		  utilDecs = new double[nodeDecOrBranch.getChildren().size()];

		// TODO Auto-generated method stub
		 //Compute utildecs and optimalDec
		relChildren = obtainDifferentUtilityRelations(new RelationList(),nodeDecOrBranch);
		maxUtil = Double.NEGATIVE_INFINITY;
		if (relChildren!=null){
			for (int i=0;i<relChildren.size();i++){
				auxRel = relChildren.elementAt(i);
				auxUtil=((PotentialTable)auxRel.getValues()).maximumValue();
				utilDecs[i]=auxUtil;
				if (auxUtil>maxUtil){
					maxUtil=auxUtil;
					optimalDec = i;
				}
			}
		}
		
	}
	
	private void computeUtilAndOptimalDecAPrioriInformation() {
		// TODO Auto-generated method stub
		  NodeGSDAG root;
		  NodeGSDAG childOfRoot;

	
		 //In chance nodes we consider the decision or branch is the child of root
	  root = gsdag.root;
	  childOfRoot = (NodeGSDAG) root.getChildrenNodes().elementAt(0);
		  
	  computeUtilAndOptimalDec(childOfRoot);

		
	}
	
	private void computeUtilAndOptimalOptAPrioriInformation() {
		  NodeGSDAG root;
		  NodeGSDAG childOfRoot;

	
		 //In chance nodes we consider the decision or branch is the child of root
	  root = gsdag.root;
	  childOfRoot = (NodeGSDAG) root.getChildrenNodes().elementAt(0);
		  
	  computeUtilAndOptimalOpt(childOfRoot);
		
	}

	private void computeUtilAndOptimalOpt(NodeGSDAG nodeGSDAG) {
		// TODO Auto-generated method stub
		if (nodeGSDAG.getTypeOfNodeGSDAG()==NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
			computeUtilAndOptimaOptBranch(nodeGSDAG);
		}
		else{//We assume we have a decision
			computeUtilAndOptimalOptDecisionNode(nodeGSDAG);
		}
	}


	private void computeUtilAndOptimaOpt() {
		computeUtilAndOptimalOpt(gsdag.root);
		
		
	}
	
	
	  /**
	 * It computes the utilities and the optimal option when the root of the gsdag is a decision instead of a branch
	 */
	private void computeUtilAndOptimalOptDecisionNode(NodeGSDAG nodeGSDAG) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		//NodeGSDAG root;
		// NodeList children;
		Relation auxRel;
		double auxUtil;
		double maxUtil;
		RelationList relChildren;
		PotentialTable auxPot;
		int indexOpt;


		// children = root.getChildrenNodes();

		// Compute utilOpts and optimalOpt
		// Index global for all the decisions. It is
		indexOpt = 0;
		maxUtil = Double.NEGATIVE_INFINITY;

		relChildren = obtainDifferentUtilityRelations(new RelationList(), nodeGSDAG);
		if (relChildren != null) {
			if (relChildren.size() == 1) {
				auxRel = relChildren.elementAt(0);
				auxPot = ((PotentialTable) auxRel.getValues());
				for (int j = 0; j < auxPot.getSize(); j++) {
					auxUtil = auxPot.getValue(j);
					utilOpts[indexOpt] = auxUtil;
					if (auxUtil > maxUtil) {
						maxUtil = auxUtil;
						optimalOpt = indexOpt;
					}
					indexOpt++;
				}
			} else {
				System.out
						.println("Error in method cumputeUtilities... of class DinamicUID.");
				System.out.println("We require an only relation");
			}
		}
		
		
	}


	private void computeUtilAndOptimaOptBranch(NodeGSDAG nodeGSDAG) {
			// TODO Auto-generated method stub
		  //NodeGSDAG root;
		  NodeList children;
		  Relation auxRel;
		  double auxUtil;
		  double maxUtil;
		  RelationList relGrandChildren;
		  PotentialTable auxPot;
		  int indexOpt;
		  
		  //root = gsdag.root;
		  
		  children = nodeGSDAG.getChildrenNodes();
		  
//			Compute utilOpts and optimalOpt
			//Index global for all the decisions. It is 
			indexOpt = 0;
			maxUtil = Double.NEGATIVE_INFINITY;
			for (int i=0;i<children.size();i++){
				relGrandChildren = obtainDifferentUtilityRelations(new RelationList(),(NodeGSDAG) children.elementAt(i));
				if (relGrandChildren!=null){
					if (relGrandChildren.size()==1){
						auxRel = relGrandChildren.elementAt(0);
						auxPot = ((PotentialTable)auxRel.getValues());
						//if (auxPot.getSize()==utilOpts.length){
						for (int j=0;j<auxPot.getSize();j++){
							auxUtil = auxPot.getValue(j);
							utilOpts[indexOpt]= auxUtil;
							if (auxUtil>maxUtil){
								maxUtil = auxUtil;
								optimalOpt = indexOpt;
							}
							indexOpt++;
						}
						//}
						/*else{//It is an error situation. However, given that I don't find now how to solve, I return
							//all the options to zero.
							for (int k=0;k<utilOpts.length;k++){
								utilOpts[k]=0.0;
							}
							optimalOpt = 0;
						}*/
					}
					else{
						System.out.println("Error in method cumputeUtilities... of class DinamicUID.");
						System.out.println("We require an only relation");
					}
}
	}
	  
	  //Compute utilOpts and optimalOpt
}
	private int getFirstDecisionMade() {
		  NodeGSDAG rootGSDAG;
		  int first;
		//We don't implement completely this method by the moment.  
		  first = -1;
		  return first;
		  
	/*	  
		  rootGSDAG = gsdag.root;
		  
		  switch(rootGSDAG.type){
		  case CHANCE:
			  first = -1;
			  break;
		  case DECISION:
			  
			  
		  }
		// TODO Auto-generated method stub
		return 0;*/
	}


	public void initializePotentialsInGSDAG(){
		  gsdag.initializePotentials(network.getRelationList());
	  }

private void eliminateVariablesOfUID(CronoNano crono, PropagationStatisticsAOUID stats,boolean computeEU, boolean isReferenceEvaluation, GSDAG gsdagReference, int initialNumDecisionsPlotMEU, int finalNumDecisionsPlotMEU, int finalNumDecsRight, boolean monitorStatistics, boolean computePolicies) {
		// TODO Auto-generated method stub
	NodeGSDAG last;
	
	last = gsdag.getLastNodeGSDAG();
	evaluateUID(last,crono,stats,computeEU,isReferenceEvaluation,gsdagReference,initialNumDecisionsPlotMEU,finalNumDecisionsPlotMEU,finalNumDecsRight,monitorStatistics,computePolicies);	
	}

	//It evaluates the UID starting in the node 'node'. Branches are responsible of the synchronization
//to perform a correct evaluation.
	private void evaluateUID(NodeGSDAG node, CronoNano crono, PropagationStatisticsAOUID stats,boolean computeEU, boolean isReferenceEvaluation, GSDAG gsdagReference, int initialNumDecisionsPlotMEU, int finalNumDecisionsPlotMEU,int finalNumDecsRight,boolean monitorStatistics,boolean computePolicies) {
	
		NodeGSDAG auxNodeGSDAG;
		NodeList children;
		NodeList parents;
		boolean evaluated = false;
		double eu;
		
		//Process the current node
		switch (node.type){
		case BRANCH:
			evaluated=collectRelationsInBranch(node,computePolicies);
			break;
		case CHANCE:
		case DECISION:
			collectRelationsInChanceOrDecision(node);
			eliminateAllVariablesInNodeGSDAGSequentially(node,computePolicies);
			node.setCompletelyEvaluated(true);
			evaluated = true;
			//We sample the statistics
			stats.addToLastTime(crono.getTime());
			crono.stop();
			eu = getEUOfCurrentStrategy(computeEU);
			//eu = 0.0;
			if (computeEU) System.out.println("DP: The EU of the current strategy is:"+eu);
			stats.addExpectedUtility(eu);
			//stats.addExpectedUtilityNDecs(getEUCurrentStrategyNDecs(computeEU,gsdagReference,numDecisionsPlotMEU));
			if ((isReferenceEvaluation==false)&&(monitorStatistics)){
				this.storeStatisticsEUNDecsAndPropDecsRight(stats, computeEU, gsdagReference, initialNumDecisionsPlotMEU, finalNumDecisionsPlotMEU,finalNumDecsRight);
			}
			stats.addDecisionAndOption(-1,-1);
			stats.addF(0.0);
			
			crono.start();
			break;
		}
		if (evaluated){
		//Process the parents
		parents = node.getParentNodes();
		for (int i=0;i<parents.size();i++){
			evaluateUID((NodeGSDAG) parents.elementAt(i),crono,stats,computeEU,isReferenceEvaluation,gsdagReference, initialNumDecisionsPlotMEU, finalNumDecisionsPlotMEU,finalNumDecsRight,monitorStatistics,computePolicies);
		}
		}
		
	
}
	
	private void storeStatisticsEUNDecsAndPropDecsRight(
			PropagationStatisticsAOUID stats, boolean computeEU,
			GSDAG gsdagReference, int initialNumDecisionsPlotMEU,
			int finalNumDecisionsPlotMEU, int finalNumDecsRight) {
		// TODO Auto-generated method stub
		storeStatisticsEUNDecs(stats, computeEU, gsdagReference, initialNumDecisionsPlotMEU, finalNumDecisionsPlotMEU);
		storeStatisticsPropDecsRight(stats, computeEU, gsdagReference,finalNumDecsRight);
		
	}


		private void storeStatisticsPropDecsRight(PropagationStatisticsAOUID stats,
			boolean computeEU, GSDAG gsdagReference, int finalNumDecsRight) {
		// TODO Auto-generated method stub
		
			double auxProp;
			ArrayList<Double> auxArrayProp;
			ArrayList<Double>[] arraysProp;

			arraysProp = stats.getPropDecsRight();

			for (int level=0;level<=finalNumDecsRight;level++){
				if (computeEU){
					auxProp = getProportionDecisionsRight(level,gsdagReference);
				}
				else{
					auxProp = 0.0;
				}
				arraysProp[level].add(auxProp);
			}

			
			
	}


		/**
		 * @param level Level of the decision tree in which we want to compute the stastics
		 * @param gsdagReference GSDAG evaluated previously
		 * @return
		 */
		private double getProportionDecisionsRight(int level, GSDAG gsdagReference) {
			// TODO Auto-generated method stub
			return getProportionDecisionsRight(level,gsdag.root,gsdagReference.root);
		}
		
		
		
		
		private double getProportionDecisionsRight(int levelDT, NodeGSDAG nodeCurrentGSDAG, NodeGSDAG nodeGSDAGReference) {
			// TODO Auto-generated method stub
			
			// TODO Auto-generated method stub
			
			double proportion = 0.0;
			NodeList childrenCurrent;
			NodeList childrenReference;
			int numChildren;
			int newLevelDT = 0;
			FiniteStates auxDec;
		
				
				if (levelDT > 0) {

				childrenCurrent = nodeCurrentGSDAG.getChildrenNodes();
				numChildren = childrenCurrent.size();
				if (numChildren == 0) {
					proportion = 1.0;
				}
				else {
					TypeOfNodeGSDAG typeNodeGSDAG = nodeCurrentGSDAG.type;
					int numVarsInNode;
					ArrayList<String> varsInNode = nodeCurrentGSDAG.getVariables();
					numVarsInNode = varsInNode.size();
					int sizeListRelationsCurrent = nodeCurrentGSDAG.getListRelations().size();
					int numVarsInNodeGSDAG = nodeCurrentGSDAG.getVariables().size();
					if ((typeNodeGSDAG==TypeOfNodeGSDAG.DECISION)&&(levelDT<=numVarsInNode)){
						if (sizeListRelationsCurrent+levelDT-1>numVarsInNodeGSDAG){
							//The policy has been calculated
							proportion = 1.0;
						}
						else{
						//Random policy
						auxDec = (FiniteStates) network.getNode(varsInNode.get(levelDT-1));
						proportion = 1.0/(auxDec.getNumStates());
						}
					}
					else if ((typeNodeGSDAG==TypeOfNodeGSDAG.BRANCH)&&(levelDT==1)){
						if (sizeListRelationsCurrent>0){
							//The branch has been evaluated
							proportion = 1.0;
						}
						else{
							proportion = 1.0/nodeCurrentGSDAG.getChildren().size();
						}
						
						
					}
					else{
						switch (typeNodeGSDAG){
						case CHANCE:
							newLevelDT = levelDT;
							break;
						case DECISION:
							newLevelDT = levelDT - numVarsInNode;
							break;
						case BRANCH:
							newLevelDT = levelDT-1;
							break;
						}
						
						childrenCurrent = nodeCurrentGSDAG.getChildrenNodes();
						childrenReference = nodeGSDAGReference.getChildrenNodes();
						numChildren = childrenCurrent.size();
						for (int i=0;i<numChildren;i++){
							NodeGSDAG auxChildCurrent = (NodeGSDAG) childrenCurrent.elementAt(i);
							NodeGSDAG auxChildReference = (NodeGSDAG) childrenReference.elementAt(i);
							proportion = proportion + getProportionDecisionsRight(newLevelDT,auxChildCurrent,auxChildReference)/numChildren;
						}
					}
					

				}
			}
			else {// We take the value given by DP
				proportion = 1.0;
			}
			return proportion;
		}




		
		
		
		
		
		
		
		
		
		
		
		


		//It evaluates the node 'node'. Branches are responsible of the synchronization
//	to perform a correct evaluation. It's similar to evaluateUID, but here we only
	//evaluate a nodeGSDAG (i.e. all the variables contained)
		protected boolean evaluateNode(NodeGSDAG node,boolean computePolicies) {
		
			boolean evaluated=false;
			
			//Process the current node
			switch (node.type){
			case BRANCH:
				evaluated=collectRelationsInBranch(node,computePolicies);
				break;
			case CHANCE:
			case DECISION:
				collectRelationsInChanceOrDecision(node);
				eliminateAllVariablesInNodeGSDAGSequentially(node,computePolicies);
				node.setCompletelyEvaluated(true);
				evaluated=false;
				break;
			}
			return evaluated;
		
			
		
	}

//It takes the relations of the child in the GSDAG
	protected void collectRelationsInChanceOrDecision(NodeGSDAG node) {
		// TODO Auto-generated method stub
		
		int numChildren;
		NodeGSDAG onlyChild;
		NodeList children;
		
		// TODO Auto-generated method stub
//		Collect the potentials of the children (if necessary)
		children = node.getChildrenNodes();
		
		numChildren = children.size();
			
		if (numChildren==1){
				onlyChild = (NodeGSDAG) children.elementAt(0);
			//Copy the probability potentials of a child
			node.copyRelationsFrom(onlyChild);
		
		}
		else{
			System.out.println("Error in method collectRelationsInChanceOrDecision of class DinamicGSDAG. Decision and chance nodes in GSDAG must have an only child");
			
		}
		
	}


/*	private void eliminateAllVariablesInNodeGSDAG(NodeGSDAG node) {
		// TODO Auto-generated method stub
	//Eliminate all the variables of the nodeGSDAG
		for(String auxName:node.getVariables()){
			System.out.println("Eliminating variable "+auxName);
			eliminateChanceOrDecisionVariable(node,auxName);
		}
		
	}*/

	private void eliminateAllVariablesInNodeGSDAGSequentially(NodeGSDAG node,boolean computePolicies) {
		// TODO Auto-generated method stub
		ArrayList<String> vars;
		String auxName;
	//Eliminate all the variables of the nodeGSDAG
		vars = node.getVariables();
		for (int i=vars.size()-1;i>=0;i--){
		//for(String auxName:node.getVariables()){
			auxName = vars.get(i);
			if (debug ) System.out.println("Eliminating variable "+auxName);
			eliminateChanceOrDecisionVariable(node,auxName,computePolicies);
		}
		
	}


	protected boolean collectRelationsInBranch(NodeGSDAG node,boolean computePolicies) {
	
		int numChildren;
		NodeGSDAG firstChild;
		RelationList commonUtilRelations;
		RelationList differentRelations;
		RelationList newProbRels;
		RelationList newUtilRels;
		NodeList children;
		
		// TODO Auto-generated method stub
//		Collect the potentials of the children (if necessary)
		children = node.getChildrenNodes();
		
		boolean evaluated=false;
		//We only do something if all the children have finished their 
		//evaluation
		if (node.areAllChildrenEvaluated()){
			if (debug) System.out.println("Collecting potentials in branch");
			evaluated = true;
			numChildren = children.size();
			
			if (numChildren>0){
				firstChild = (NodeGSDAG) children.elementAt(0);
			//Copy the probability potentials of a child
			newProbRels = firstChild.getCurrentProbabilityRelations().copy();
						
			commonUtilRelations = obtainCommonUtilityRelations(node);
						
			//node.setUtilityRelations(commonRelations);
			newUtilRels = commonUtilRelations;
			if (commonUtilRelations.size()<firstChild.getCurrentUtilityRelations().size()){
				//There are different relations in the children
				differentRelations = obtainDifferentUtilityRelations(commonUtilRelations,node);
				if (computePolicies){
					computeAndSetUtilitiesAndPoliciesForDecisionTable(node,differentRelations);
				}
				
				
				newUtilRels.insertRelation(maximizeUtilityRelations(differentRelations));
			}
			
			node.setCurrentRelations(newProbRels, newUtilRels);
		
			}
		}
		else{
			evaluated = false;
			if (debug){
				System.out.println("We can't still collect the potentials in the branch node of children:");
				children= node.getChildrenNodes();
				for (int i=0;i<children.size();i++){
					System.out.print(((NodeGSDAG)children.elementAt(i)).getVariables().toString());
				}
			}
			
			
		}
		
		return evaluated;
				
		
		/*//Distribute the potentials to the parents (if necessary)
		NodeList parents;
		parents = node.getParentNodes();
		for (int i=0;i<parents.size();i++){
			auxNodeGSDAG = (NodeGSDAG) parents.elementAt(i);
			auxNodeGSDAG.copyRelationsFrom(node);
			*/
			
		//}
		
	}
		
		private void computeAndSetUtilitiesAndPoliciesForDecisionTable(
			NodeGSDAG node, RelationList listRelationsForStepPolicy) {
		// TODO Auto-generated method stub
			RelationList utilitiesList;
			RelationList policiesList;
			PotentialTable utilPot,policyPot;
			String nameForVariableBranch = "Branch";
			
			utilitiesList = obtainUtilityRelationsForDecisionTable(node);
			policiesList = listRelationsForStepPolicy;
			
			utilPot = constructAPotentialTableWithBranchAsVariable(node,utilitiesList,nameForVariableBranch);
			policyPot = constructAPotentialTableWithBranchAsVariable(node,policiesList,nameForVariableBranch);
			
			node.storeUtilitiesAndPolicyForDecisionTable(utilPot, policyPot, nameForVariableBranch);
		
	}

		private PotentialTable constructAPotentialTableWithBranchAsVariable(NodeGSDAG nodeGSDAG,
				RelationList utilitiesList, String nameForVariableBranch) {
			// TODO Auto-generated method stub
			FiniteStates varBranch;
			int numChildrenBranch;
			String statesVarBranch[];
			NodeList childrenOfBranch;
			PotentialTable pot;
			Configuration auxConf;
			Configuration conf;
			ArrayList<String> namesVariables;
			
			varBranch = new FiniteStates();
			
			varBranch.setName(nameForVariableBranch);
							
			childrenOfBranch = nodeGSDAG.getChildrenNodes();
			numChildrenBranch = childrenOfBranch.size();
			statesVarBranch = new String [numChildrenBranch];
			for (int i=0;i<numChildrenBranch;i++){
				statesVarBranch[i]=((NodeGSDAG)childrenOfBranch.elementAt(i)).getVariables().toString();
			}
			
			varBranch.setStates(statesVarBranch);
			
			
			namesVariables = nodeGSDAG.getAnAdmissibleOrderOfThePast();
			NodeList variables = new NodeList();
			for (String auxName:namesVariables){
				variables.insertNode(this.network.getNode(auxName));
			}
			variables.insertNode(varBranch);
			
			
			conf = new Configuration(variables);
			pot = new PotentialTable(variables);
			int numberOfValues = (int)FiniteStates.getSize(variables);
			
			for (int i=0 ; i<numberOfValues ; i++) {
				 int indexOfChild = conf.getValue(varBranch);
				 auxConf = new Configuration(variables.getNodes(),conf);
				 auxConf.remove(varBranch);
				 double auxValue = ((PotentialTable)(((Relation) utilitiesList.getRelations().get(indexOfChild)).getValues())).getValue(auxConf);
				 pot.setValue(conf, auxValue);
			     conf.nextConfiguration();
			}
			pot = (PotentialTable) pot.sendVarToEnd(varBranch);
			return pot;
		}

		private Relation maximizeUtilityRelations(RelationList utilRels) {
		// TODO Auto-generated method stub
			Relation newRel;
			
			PotentialTable utilPot = (PotentialTable)(utilRels.elementAt(0).getValues());
			for(int j=1;j<utilRels.size();j++){
				utilPot = utilPot.combine((PotentialTable) utilRels.elementAt(j).getValues(),new MaxFunction());
			}
					
			//Create a new relation
		    newRel = new Relation();
		    // Set the kind for the final relation 
		    newRel.setKind(Relation.POTENTIAL);
		    newRel.getVariables().setNodes((Vector)utilPot.getVariables().clone());
		    newRel.setValues(utilPot);
		    
		    return newRel;
	}

//It returns a list with the agregated relation for each children, which will be used to determine the step policy
		private RelationList obtainDifferentUtilityRelations(RelationList commonRelations, NodeGSDAG node) {
		// TODO Auto-generated method stub
		NodeList children;
		NodeGSDAG auxChild;
		RelationList auxUtilRels;
		Relation auxRel;
		RelationList auxDifferents;
		Relation newRel;
		RelationList newDifferents;
		
		
		children = node.getChildrenNodes();
		
		auxDifferents = new RelationList();
		
		newDifferents = new RelationList();
		
		for(int i=0;i<children.size();i++){
			auxDifferents = new RelationList();
			auxChild = (NodeGSDAG) children.elementAt(i);
			auxUtilRels = auxChild.getCurrentUtilityRelations();
			//Find out the different relations of each child
			for (int j=0;j<auxUtilRels.size();j++){
				auxRel = auxUtilRels.elementAt(j);
				if (commonRelations.getRelations().contains(auxRel)==false){
					auxDifferents.insertRelation(auxRel);
				}
			}
			//Sum of the different relations for each child
			newRel = sumUtilityRelations(auxDifferents);
			//Add the new relation
			newDifferents.insertRelation(newRel);
			
		}
			
		return newDifferents;
	}
		
		/**
		 * @param node
		 * @return This method is used by branch nodes. It returns a relations list, where each relation is the sum of the utility relations of each child of the branch. It is used
		 * for presenting the step-policy in the GUI of Elvira
		 */
		private RelationList obtainUtilityRelationsForDecisionTable(NodeGSDAG node) {
			
			
			return obtainDifferentUtilityRelations(new RelationList(),node);
			
		
		}

		//It creates a new relation by summing other utility relations
	public static Relation sumUtilityRelations(RelationList utilRels){
		Relation newRel;
		
		PotentialTable utilPot = (PotentialTable)(utilRels.elementAt(0).getValues());
		for(int j=1;j<utilRels.size();j++){
			utilPot = utilPot.combine((PotentialTable) utilRels.elementAt(j).getValues(),new SumFunction());
		}
				
		//Create a new relation
	    newRel = new Relation();
	    // Set the kind for the final relation 
	    newRel.setKind(Relation.POTENTIAL);
	    newRel.getVariables().setNodes((Vector)utilPot.getVariables().clone());
	    newRel.setValues(utilPot);
	    
	    return newRel;
		
	}
	
	


		//It returns the list of utility relations that are commmon to all the children,
		//so they don't have to be considered for the maximization
		public RelationList obtainCommonUtilityRelations(NodeGSDAG node){
			//RelationList utilsNode;
			RelationList utilsOfFirstChild;
			RelationList newRelList;
			int numChildren;
			NodeList children;
			Relation auxRel;
			
			newRelList =new RelationList(); 
			
			//utilsNode = node.getUtilityRelations();
			 
			
			children = node.getChildrenNodes();
			
			utilsOfFirstChild = ((NodeGSDAG)children.elementAt(0)).getCurrentUtilityRelations(); 
			
			numChildren = children.size();
			//We insert the relations that are in common with the second child (they will be in common with the other children)
			for (int i=0;i<utilsOfFirstChild.size();i++){
				auxRel = utilsOfFirstChild.elementAt(i);
				//Checking the equality between relations is performed watching their pointers.
				//Else they are considered different.
				if ((numChildren == 1)||
						((NodeGSDAG)children.elementAt(1)).getCurrentUtilityRelations().getRelations().contains(auxRel)){
					newRelList.insertRelation(auxRel);
				}
			}
			return newRelList;
		}
		
		

	

	protected void eliminateChanceOrDecisionVariable(NodeGSDAG node, String name,boolean computePolicies) {
		// TODO Auto-generated method stub
		Node nodeToElim;
		RelationList probRelsOfElim;
		RelationList utilRelsOfElim;
		PotentialTable newProbPot = null;
		PotentialTable probPot = null;
		Relation newProbRel;
		Relation newUtilRel;
		RelationList newProbRels;
		RelationList newUtilRels;
		PotentialTable utilsForDecisionTable, policyForDecisionTable;
		
		nodeToElim = this.network.getNodeList().getNode(name);
		
		newProbRels = node.getCurrentProbabilityRelations().copy();
		
		probRelsOfElim =	newProbRels.getRelationsOfAndRemove(nodeToElim);
			
		//Elimination of the variable from the probability potentials	
		if (probRelsOfElim.size()>0){
			probPot = (PotentialTable) probRelsOfElim.elementAt(0).getValues();
			for(int j=1;j<probRelsOfElim.size();j++){
				probPot = (PotentialTable) probPot.combine(probRelsOfElim.elementAt(j).getValues());
			}
			//Sum or maximize over nodeToElim
			newProbPot = applyMarginalization(probPot,nodeToElim);
			
	        // Create a new relation that is added to probability relations
            newProbRel = new Relation();
            // Set the kind for the final relation 
            newProbRel.setKind(Relation.POTENTIAL);
            newProbRel.getVariables().setNodes((Vector)newProbPot.getVariables().clone());
            newProbRel.setValues(newProbPot);
      
            //Add the new relation to the remaining probability relations
            newProbRels.insertRelation(newProbRel);
		}
            
        //Elimination of the variable from the utility potentials
	
		//Combine the utility potentials
			newUtilRels = node.getCurrentUtilityRelations().copy();
			
			utilRelsOfElim = newUtilRels.getRelationsOfAndRemove(nodeToElim);
         
            if (utilRelsOfElim.size()>0){
            
            PotentialTable utilPot = sumRelationsIntoAPotentialTable(utilRelsOfElim);
    		
			if (probPot!=null){
			//Multiply the probability and the utility potential
			utilPot = utilPot.combine(probPot);
			}
			
			//Store the utilities and policy for the decision table of the GUI
			if ((node.getTypeOfNodeGSDAG() == TypeOfNodeGSDAG.DECISION)&&(computePolicies)) {
				
				policyForDecisionTable = utilPot;

				utilsForDecisionTable = sumRelationsIntoAPotentialTable(node.getCurrentUtilityRelations().copy());
				if (probPot != null) {
					utilsForDecisionTable = utilsForDecisionTable
							.combine(probPot);
				}
				
				if (newProbPot!=null){
					utilsForDecisionTable = (PotentialTable)(utilsForDecisionTable.divide(newProbPot));
					}
				
				utilsForDecisionTable = (PotentialTable) utilsForDecisionTable.sendVarToEnd(nodeToElim);
				policyForDecisionTable = (PotentialTable) policyForDecisionTable.sendVarToEnd(nodeToElim);
				node.storeUtilitiesAndPolicyForDecisionTable(
						utilsForDecisionTable, policyForDecisionTable, name);
			}
			
			//Sum or maximize the utility potential over nodeToElim
			utilPot = (PotentialTable)(applyMarginalization(utilPot,nodeToElim));
			
		//Division by the probability potential
			if (newProbPot!=null){
			utilPot = (PotentialTable)(utilPot.divide(newProbPot));
			}
			
			//Create a new relation that is added to the utility relations
            newUtilRel = new Relation();
            // Set the kind for the final relation 
            newUtilRel.setKind(Relation.POTENTIAL);
            newUtilRel.getVariables().setNodes((Vector)utilPot.getVariables().clone());
            newUtilRel.setValues(utilPot);
      
            //Add the new relation to the remaining utility relations
            newUtilRels.insertRelation(newUtilRel);
            }
            
            node.setCurrentRelations(newProbRels,newUtilRels);
            
            
            //We annotate the variable that has just been eliminated
            node.setLastEliminatedVariable(name);
            
            //We update evaluated in node
            node.setCompletelyEvaluated(name==node.getVariables().get(0));
            
   }

	
	/**
	 * @param relations
	 * @return The sum of the potential tables of relations
	 */
	private static PotentialTable sumRelationsIntoAPotentialTable(RelationList relations){
	PotentialTable sum = (PotentialTable)(relations.elementAt(0).getValues());
	for(int j=1;j<relations.size();j++){
		sum = sum.combine((PotentialTable) relations.elementAt(j).getValues(),new SumFunction());
	}
	return sum;
	}

	private PotentialTable applyMarginalization(PotentialTable pot, Node nodeToElim) {
		// TODO Auto-generated method stub
		PotentialTable newPot=null;
		Vector vars;
		
		switch(nodeToElim.getKindOfNode()){
		case Node.CHANCE:
			newPot = (PotentialTable) pot.addVariable(nodeToElim);
			break;
		case Node.DECISION:
			vars = new Vector(pot.getVariables());
			vars.removeElement(nodeToElim);
			newPot = (PotentialTable) pot.maxMarginalizePotential(vars);
			break;
		default:
			System.out.println("Error: Method applyMarginalization in class DinamicGSDAG. The nodeToElim must CHANCE or DECISION");
		}
		return newPot;
	}


	public GSDAG getGsdag() {
		return gsdag;
	}


	public double[] getUtilDecs() {
		return utilDecs;
	}
	
	
	
	public ArrayList<Double> convertUtilDecsToArrayList(){
		ArrayList<Double> array;
		
		array = new ArrayList<Double>();
		for (int i=0;i<utilDecs.length;i++){
			array.add(utilDecs[i]);
		}
		return array;
	}


	public int getOptimalDec() {
		return optimalDec;
	}
	
	

	public double[] getUtilOpts() {
		return utilOpts;
	}


	public int getOptimalOpt() {
		return optimalOpt;
		
	}
	
	public double getEUOfCurrentStrategy() {
		ArrayList<NodeGSDAG> nearestDescsEvaluated;
		NodeGSDAG auxNodeGSDAG;
		RelationList auxInstantUtilRels;
		RelationList auxInstantProbRels;
		double euOfDescs[];
		double probSelectDesc[];
		double globalEU;
		NodeGSDAG nodeGSDAG;
		// TODO Auto-generated method stub
		
		nodeGSDAG = gsdag.root;
		
		nearestDescsEvaluated = nodeGSDAG.obtainNearestDescendantsWithSomeVariablesEliminated();
		
		
		globalEU = 0.0;
		euOfDescs = new double[nearestDescsEvaluated.size()];
		probSelectDesc = new double[nearestDescsEvaluated.size()];
		
		for (int i=0;i<nearestDescsEvaluated.size();i++){
			auxNodeGSDAG = nearestDescsEvaluated.get(i);
			//We instantiate the utility potentials
			auxInstantUtilRels = copyRelations(auxNodeGSDAG.getCurrentUtilityRelations());
			auxInstantProbRels = copyRelations(auxNodeGSDAG.getCurrentProbabilityRelations());
			//We calculate the value of the EU for the utility potentials instantiated
			euOfDescs[i] = NodeAOUID.getEU(auxInstantProbRels,auxInstantUtilRels);
			probSelectDesc[i]=nodeGSDAG.obtainProbabilityOfSelect(auxNodeGSDAG);
			globalEU = globalEU+euOfDescs[i]*probSelectDesc[i];
		}
		return globalEU;
		
		
	}
	
	
	/**
	 * It copies a list of relations
	 * It uses the atrribute 'instantiations'
	 * The relations of the parameter 'relations' are changed if 'nodeVar' appears in them
	 * @param relations A list of the relations after the instantiation
	 */
	protected RelationList copyRelations(RelationList relations) {
		// TODO Auto-generated method stub
	
		Relation auxNewRel;
		RelationList newRelations;
		Relation auxRel;
		
		ArrayList<Relation> auxRelations;
		
		newRelations = new RelationList();
		
		
			
		
		for (int i=0;i<relations.size();i++){
			auxRel = relations.elementAt(i);
				//Add the new relation restricted to the new configuration of variables
				auxNewRel = auxRel.copy();
				
				newRelations.insertRelation(auxNewRel);
			
		}
		
		return newRelations;
		
		
	}


	public double[] getRandomUtilDecs() {
		return randomUtilDecs;
	}


	public void setRandomUtilDecs(double[] randomUtilDecs) {
		this.randomUtilDecs = randomUtilDecs;
	}


	public double[] getRandomUtilOpts() {
		return randomUtilOpts;
	}


	public void setRandomUtilOpts(double[] randomUtilOpts) {
		this.randomUtilOpts = randomUtilOpts;
	}


	public double[] getInitialEUNDecs() {
		return initialEUNDecs;
	}


	public void setInitialEUNDecs(double[] initialEUNDecs) {
		this.initialEUNDecs = initialEUNDecs;
	}


	
	
	
	
	
}//end of class
