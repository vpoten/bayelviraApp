/**
 * Created by dalgaard on Feb 20, 2007
 */

package elvira.learning.classification.supervised.discrete;


import java.util.*;
import java.io.*;

import elvira.parser.ParseException;
import elvira.probabilisticDecisionGraph.PDGParameterNode.StateNumberException;
import elvira.probabilisticDecisionGraph.tools.*;
//import elvira.probabilisticDecisionGraph.tools.Comparator.classificationResult;

import elvira.probabilisticDecisionGraph.*;
import elvira.database.*;
import elvira.*;

import elvira.learning.classification.*;
import elvira.learning.classification.supervised.validation.ClassifierEvaluator;
import elvira.learning.classification.supervised.validation.ClassifierEvaluator.classificationResult;

/**
 * @author dalgaard 
 *
 */
public class PDGClassifier extends PDG implements SizeComparableClassifier {

	//public enum mergeHeuristic {KL, VARENTROPY, INFLOW};
	
	private final int maxLevel;
	private long seed = System.currentTimeMillis();
	private final Random rnd = new Random(seed);
	private boolean collapseEnabled = true;
	private int minimumDataSupport = 5;
	private boolean mergeEnabled = true;
	private boolean finalMergeEnabeled = true;
	private Vector<Long> learningTime = new Vector<Long>();
	private Vector<Integer> numCollapses = new Vector<Integer>();
	private int sumOfCollapses = 0;
	private Vector<Integer> numMerges = new Vector<Integer>();
	private int sumOfMerges = 0;
	private long mergeTimeLimit = -1;
	private boolean selectiveLearning = false;
	private boolean smooth = true;
	private boolean fanLearning = false;
	private boolean useValidationData = false;
	
	public static enum mergeMethod {CLASS_AWARE, CLASS_UNAWARE};
	//private mergeMethod mergem = mergeMethod.CLASS_UNAWARE;
	
	public static enum variableInclusionCriteria {MAX_CMUT, MAX_CR};
	private variableInclusionCriteria varInclusionCriteria = variableInclusionCriteria.MAX_CMUT;
	
	private static int numberOfLearn = 0;
	
	public PDGClassifier(){
		super();
		maxLevel = 1;
	}

	public PDGClassifier(int level){
		super();
		maxLevel = level;
	}

	public PDGClassifier(Vector<PDGVariableNode> vf){
		super(vf);
		maxLevel = 1;
	}
	
	public PDGClassifier(Vector<PDGVariableNode> vf, int level){
		super(vf);
		maxLevel = level;
	}
	
	public PDGClassifier(Vector<PDGVariableNode> f, String modelName){
		super(f,modelName);
		maxLevel = 1;
	}

	public PDGClassifier(Vector<PDGVariableNode> f, String modelName, int level){
		super(f,modelName);
		maxLevel = level;
	}
	
	public PDGClassifier copy(){
		PDG pdgClone = super.copy();
		return new PDGClassifier(pdgClone.getVariableForestCopy(), name, maxLevel);
	}

	/**
	 * Specify the seed to use for random operations. If not specified, the system time is
	 * used.
	 * 
	 * @param s - the seed.
	 */
	public void setSeed(long s){
		seed = s;
		rnd.setSeed(seed);
	}
	
	
	/**
	 * Enable/disable collapse of zero-support nodes, that is, nodes that has no 
	 * data support.
	 * 
	 * @param t -
	 * 	true to enable, false to disable.
	 * 
	 */
	public void setCollapseEnabled(boolean t){
		collapseEnabled = t;
	}
	
	/**
	 * Enable/disable merging.
	 * 
	 * @param t -
	 * 			true to enable, false to disable.
	 */
	public void setMergeEnabled(boolean t){
		mergeEnabled = t;
	}
	
	public void setFinalMerge(boolean t){
		finalMergeEnabeled = t;
	}
	
	public void setFanLearning(boolean t){
		fanLearning = t;
	}
	
	/**
	 * Set the time limit for performing score uptimising merge operations. 
	 * The number of possible merge operations is polynomial in the number of
	 * parameter nodes, and as we need to compute the classification rate from a validation 
	 * set for every combination, if may be neccesary to specify a limit.
	 * 
	 * @param limit - the maximum allowed time for each PDGVariableNode in milliseconds.
	 */
	public void setMergeTimeLimit(long limit){
		mergeTimeLimit = limit;
	}
	
	/*public void setSaveModels(boolean b){
		saveModels = b;
	}*/
	
	public void setMinimumDataSupport(int s){
		this.minimumDataSupport = s;
	}

	public void setSelectiveLearning(boolean v){
		this.selectiveLearning = v;
	}
	
	public void setVariableInclusionCriteria(variableInclusionCriteria vic){
		this.varInclusionCriteria = vic;
	}
	
	public void setSmooth(boolean s){
		this.smooth = s;
	}
	
	public void setUseValidationData(boolean b){
		this.useValidationData = b;
	}
	
	/**
	 * 
	 * This method learns a PDGClassifier by first inducing a TAN model and then
	 * inducing an equivalent PDGClassifier. Finally, nodes may be merged if this is enabled. 
	 * The merging is guided by the classification rate.
	 * 
	 * @param dbc - the database to learn from.
	 * @param cVarIdx - the index of the class variable in the database.
	 * @throws VectorOpsException
	 * @throws PDGException
	 */
	private void learnFromFan(DataBaseCases dbc, int cVarIdx) throws VectorOpsException, PDGException{
		TreeAugmentedNaiveBayes tan = new TreeAugmentedNaiveBayes();
		
		DataBaseCases valData = new DataBaseCases();
		DataBaseCases trainData = new DataBaseCases();
		// divide data into training and validation folds
		if(this.useValidationData){
			dbc.divideIntoTrainAndTest(trainData, valData, 0.7, this.rnd);
		} else {
			trainData = dbc;
			valData = dbc;
		}
		trainData = dbc;
		valData = dbc;
		tan.learn(trainData, cVarIdx);		

		Bnet tanModel = tan.getClassifier();
		PDG model = new PDG(tanModel, tan.getClassVar());
		//System.out.println(model.toString());
		this.variableForest.removeAllElements();
		this.variableForest.addAll(model.getVariableForestCopy());
		this.initialiseReach();
		this.updateReach(trainData.getCaseListMem());
		this.learnParametersFromReach(this.smooth);

		//checkBnetEquivalence(tanModel);
		
		//this.learnParameters(trainData.getCases(), this.smooth);		
		if(this.mergeEnabled){
			this.mergeNodes(valData, trainData, cVarIdx);
			this.learnParameters(trainData.getCases(), this.smooth);
		}
	}

	public void learn(DataBaseCases dbc, int cVarIdx){
		long t0 = System.currentTimeMillis();
		try{
			//learnHierarchicalNaiveClassifier(dbc, cVarIdx, false);
			if(this.fanLearning) learnFromFan(dbc, cVarIdx);
			else learnClassifier(dbc, cVarIdx, false, 3);
		} catch(Exception e){
			e.printStackTrace();
			System.exit(112);
		}
		learningTime.add(System.currentTimeMillis() - t0);
		numMerges.add(resetMergeCount());
		numCollapses.add(resetCollapseCount());
		/*if(saveModels) { 
			try{
				PDGio.save(this, "model-"+numberOfLearn+".pdg");
			} catch(IOException ioe){
				System.out.println("could not save model - exception :");
				System.out.println(ioe.getMessage());
			}
		}*/
		numberOfLearn++;
	}
		
	private int resetMergeCount(){
		int tmp = sumOfMerges;
		sumOfMerges = 0;
		return tmp;
	}
	
	private int resetCollapseCount(){
		int tmp = sumOfCollapses;
		sumOfCollapses = 0;
		return tmp;
	}
	
	private static void sortPDGParameterNodeVectorOnReachAscending(Vector<PDGParameterNode> v){
		Collections.sort(v, new Comparator<PDGParameterNode>(){
			public int compare(PDGParameterNode o1, PDGParameterNode o2) {
				return o1.getReach().getNumberOfCases() - o2.getReach().getNumberOfCases();
			}	
		});	
	}
	
	private static void sortPDGParameterNodeVectorOnReachDescending(Vector<PDGParameterNode> v){
		Collections.sort(v, new Comparator<PDGParameterNode>(){
			public int compare(PDGParameterNode o1, PDGParameterNode o2){
				return o2.getReach().getNumberOfCases() - o1.getReach().getNumberOfCases();
			}
		});
	}
	
	private void split(PDGLink l){
		PDGParameterNode clone = l.head.shallowCopy();
		PDGVariableNode headVar = l.head.getPDGVariableNode();
		double[] marginalCounts = CasesOps.getMarginalCounts(l.flow, headVar.getFiniteStates());
		try {
			//update the values for the new clone:
			clone.setValues(marginalCounts);
		} catch (StateNumberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//update the values for the head node aswell
		double[] headVals = l.head.getValues();
		int numOldReach = l.head.getReach().getNumberOfCases();
		int edgeFlow = l.flow.getNumberOfCases();
		int diff = numOldReach - edgeFlow;
		for(int i=0;i<headVals.length;i++) 
			headVals[i] = ((headVals[i]*numOldReach - marginalCounts[i]*edgeFlow)/ diff);
		VectorOps.normalise(headVals);
		l.tail.setSuccessor(clone, headVar, l.label);
	}
	
	/**
	 * 
	 * This method returns a copy of this PDG model in which the head of link l 
	 * has been split which means: 1) the old head of the link has 
	 * lost this link as incomming link, and 2) a new node has been constructed with
	 * l as the single incomming link. The CaseListMem reaching the involved nodes are not updated, 
	 * the values however will be correctly updated. That is, given that the model contains maximum 
	 * likelihood parameters before the operation, it will contain maximum likelihood parameters after the operation.
	 * 
	 * <b>IMPORTANT</b>: this method assumes that the old 
	 * head will not be orphaned if link l is removed from it.
	 * 
	 * @param l   - the link that identifies the split
	 * 
	 * @return PDGClassifier - a copy of this PDG in which the split has been performed.
	 */
	private PDG copyAndSplit(PDGLink l){
		PDGClassifier pdgcp = this.copy();
		PDGVariableNode headVar = pdgcp.getPDGVariableNode(l.head.getPDGVariableNode().getFiniteStates());
		PDGVariableNode tailVar = pdgcp.getPDGVariableNode(l.tail.getPDGVariableNode().getFiniteStates());
		PDGParameterNode head = headVar.getParameterNodeByVectorIndex(l.head.getVectorIndex());
		PDGParameterNode tail = tailVar.getParameterNodeByVectorIndex(l.tail.getVectorIndex());
		PDGLink lcp = new PDGLink(tail,head,l.flow,l.label);
		pdgcp.split(lcp);
		return pdgcp;
	}
	
	private void splitNodesLocal(PDGVariableNode pdgVar, 
			DataBaseCases validationData, 
			DataBaseCases trainData,
			double pseudoCount,
			int classnumber){
		Vector<PDGParameterNode> parameterNodesCopy = pdgVar.getParameterNodesCopy();
		sortPDGParameterNodeVectorOnReachDescending(parameterNodesCopy);
		double oldScore;
		boolean splitAgain = true;
		PDGVariableNode varPredecessor = pdgVar.predecessor();
		int statesOfParent = varPredecessor.getNumStates();
		while(splitAgain){
			oldScore = ClassifierEvaluator.testClassifier(this, validationData, classnumber).rate();
			PDG copy = this.copy();
			for(PDGParameterNode p : parameterNodesCopy){
				//TODO
				//TODO
				//TODO
				//TODO
				//TODO
				//TODO
			}
		}
	}
	
	private class parameterNodePair{
		PDGParameterNode node1;
		PDGParameterNode node2;
		double score;
		public parameterNodePair(PDGParameterNode p1, PDGParameterNode p2, double sc){
			node1 = p1;
			node2 = p2;
			score = sc;
		}
	}
	
	private parameterNodePair getPairForMergeRandomized(Vector<PDGParameterNode> parameterNodesCopy, 
			DataBaseCases validationData, 
			DataBaseCases trainData, 
			int classnumber) throws VectorOpsException, PDGException{
		if(parameterNodesCopy.size() == 1) return null;
		int i;
		double currentScore = ClassifierEvaluator.testClassifier(this, validationData, classnumber).rate();
		//System.out.println("currentScore == "+currentScore);
		//Collections.shuffle(parameterNodesCopy,this.rnd);
		PDGParameterNode p1, p2;
		parameterNodePair retval = null;
		i=0;
		while(i<(parameterNodesCopy.size()*2) && (retval == null)){
			do{
				p1 = parameterNodesCopy.elementAt(this.rnd.nextInt(parameterNodesCopy.size()));
				p2 = parameterNodesCopy.elementAt(this.rnd.nextInt(parameterNodesCopy.size()));
			} while(p1==p2);
			double mergeScore = this.mergeScoreGeneralisationRate(p1, p2, validationData, trainData, classnumber, currentScore);
			//System.out.println("mergeScore("+i+","+j+") == "+mergeScore);
			if(mergeScore > (1.0/validationData.getNumberOfCases())){
				retval = new parameterNodePair(p1,p2,mergeScore);
			}
			i++;
		}
		return retval;
	}
	
	private parameterNodePair getBestPairForMerge(Vector<PDGParameterNode> parameterNodesCopy, 
			DataBaseCases validationData, 
			DataBaseCases trainData, 
			//double pseudoCount, 
			int classnumber) throws VectorOpsException, PDGException{
		//	sortPDGParameterNodeVectorOnReachAscending(parameterNodesCopy);
		PDGParameterNode mergeCandidate1 = null, mergeCandidate2 = null;
		double bestMergeScore = 0.0;
		int i, j;
		double currentScore = ClassifierEvaluator.testClassifier(this, validationData, classnumber).rate();
		//System.out.println("currentScore == "+currentScore);
		outer :
		for(i = 0; i < parameterNodesCopy.size(); i++){
			for(j=i+1;j < parameterNodesCopy.size(); j++){
				PDGParameterNode p1 = parameterNodesCopy.elementAt(i);
				PDGParameterNode p2 = parameterNodesCopy.elementAt(j);
				//if(p1 == p2) continue;
				double mergeScore = this.mergeScoreGeneralisationRate(p1, p2, validationData, trainData, classnumber, currentScore);
				//System.out.println("mergeScore("+i+","+j+") == "+mergeScore);
				if(mergeScore > bestMergeScore){
					bestMergeScore = mergeScore;
					mergeCandidate1 = p1;
					mergeCandidate2 = p2;
					if(parameterNodesCopy.size() > 100){
						break outer;
					}
				}
			}
			if(parameterNodesCopy.size() > 5) break;
		}//outer	
		return new parameterNodePair(mergeCandidate1, mergeCandidate2, bestMergeScore);
	}
	
	
	protected int mergeNodesLocal(Vector<PDGParameterNode> parameterNodes, 
			DataBaseCases validationData, 
			DataBaseCases trainData, 
			int classnumber) throws VectorOpsException, PDGException{
		if(parameterNodes.isEmpty()){
			System.out.println("WARNING: empty Vector of PDGParameterNodes given to mergeNodesLocal!!");
			return 0;
		}
		int merged = 0;

		final long t0 = System.currentTimeMillis();
		//We extract the PDGVariableNode from the first element of parameterNodes
		PDGVariableNode pdgVar = parameterNodes.firstElement().getPDGVariableNode();
		System.out.println("Merging nodes for variable "+pdgVar.getName());
		boolean timeout = false;
		boolean continueMerge = true;
		double minimumImprovement = 1.0 / validationData.getNumberOfCases();
		do{
			Vector<PDGParameterNode> parameterNodesCopy = new Vector<PDGParameterNode>(parameterNodes);
			//parameterNodePair pnp = this.getBestPairForMerge(parameterNodesCopy, validationData, trainData, classnumber);
			parameterNodePair pnp = this.getPairForMergeRandomized(parameterNodesCopy, validationData, trainData, classnumber);
			if(pnp == null) break;
			if(pnp.score > minimumImprovement){
				System.out.println("merging nodes ("+pdgVar.getIndexOf(pnp.node1)+","+pdgVar.getIndexOf(pnp.node2)+") of "
						+pdgVar.getName()+" (has "+pdgVar.getNumberOfParameterNodes()+" nodes):  score = "+pnp.score);
				pnp.node1.safeMerge(pnp.node2, false, this.smooth);
				parameterNodes.remove(pnp.node2);
				merged++;
			} else {
				continueMerge = false;
			}
			if(mergeTimeLimit > 0){
				timeout = (mergeTimeLimit < (System.currentTimeMillis() - t0));
			}
			continueMerge &= !timeout;
		} while(continueMerge);
		if(timeout) System.out.println("(timeout)");
		this.sumOfMerges += merged;
		return merged;
	}
	
	protected int mergeNodes(DataBaseCases validationData, 
			DataBaseCases trainData, 
			int classnumber) throws VectorOpsException, PDGException{
		Stack<PDGVariableNode> pending = getDepthFirstStack();
		int merged = 0;
		PDGVariableNode pdgVar;
		while(!pending.empty()){
			pdgVar = pending.pop();
			merged += mergeNodesLocal(pdgVar.getParameterNodesCopy(), validationData, trainData, classnumber);
		}
		if(merged > 0){
			learnParameters(trainData.getCases(), this.smooth);
		}
		return merged;
	}
	
	private int mergeNodesBotomUp(PDGVariableNode pdgVar, int level, DataBaseCases valData, DataBaseCases trainData, int classIndex, Map<PDGVariableNode, Vector<Vector<PDGParameterNode>>> map) throws VectorOpsException, PDGException{
		int merged = 0;
		if(map != null){
			Vector< Vector<PDGParameterNode>> vvp = map.get(pdgVar);
			if(vvp == null){
				// we are at the root, no possibility to merge anything
				return 0;
			}
			Vector<PDGParameterNode> parameterNodesCopy = pdgVar.getParameterNodesCopy();
			for(Vector<PDGParameterNode> vp : vvp){
				merged += this.mergeNodesLocal(vp, valData, trainData, classIndex);
			}
			if(level-1 != 0){
				if(pdgVar.predecessor() != null){
					merged += mergeNodesBotomUp(pdgVar.predecessor(), level-1, valData, trainData, classIndex, map);
				}
			}
		} else {
			merged += this.mergeNodesLocal(pdgVar.getParameterNodesCopy(),valData,trainData,classIndex);
		}
		return merged;
	}
	
	private parentAndScore getBestParentForFeatureCMUT(FiniteStates feature, PDGVariableNode treeRoot, int level){
		double cmut = Measures.normalisedCMI(feature, treeRoot.getFiniteStates(), treeRoot.getPartitions());
		parentAndScore retval = new parentAndScore(treeRoot, cmut);
		if(level >= 0){
			for(PDGVariableNode p : treeRoot.getSuccessors()){
				parentAndScore pac = getBestParentForFeatureCMUT(feature, p, level - 1);
				if(retval.score < pac.score) retval = pac;
			}
		}
		return retval;
	}

	private parentAndScore getBestParentForFeatureCR(FiniteStates feature, 
			PDGVariableNode treeRoot, 
			int level,
			DataBaseCases trainData,
			DataBaseCases valData,
			int classNumber) throws PDGVariableNotFoundException {
		double cr = 0.0;
		PDGClassifier pdgc = this.copy();
		PDGVariableNode bestParent, copyRoot;
		bestParent = copyRoot = pdgc.getPDGVariableNode(treeRoot.getFiniteStates());
		PDGVariableNode newSucc = new PDGVariableNode(feature);
		copyRoot.addFullyExpandedSuccessor(newSucc);
		pdgc.learnParameters(trainData.getCases(), false);
		cr = ClassifierEvaluator.testClassifier(pdgc, valData, classNumber).rate();
		parentAndScore retval = new parentAndScore(treeRoot, cr);
		if(level >= 0){
			for(PDGVariableNode p : treeRoot.getSuccessors()){
				parentAndScore pac = getBestParentForFeatureCR(feature, p, level - 1, trainData, valData, classNumber);
				if(cr < pac.score) 
					retval = pac;
			}
		}
		return retval;
	}

	
	private class parentAndScore{
		protected final PDGVariableNode parent;
		protected final double score;
		public parentAndScore(PDGVariableNode p, double c){
			parent = p;
			score = c;
		}
	}

	
	private class featureParentAndScore extends parentAndScore{
		protected final Node feature;
		public featureParentAndScore(Node f, PDGVariableNode p, double c){
			super(p,c);
			feature = f;
		}
	}
	
	
	private featureParentAndScore getNextFeatureCMUT(Vector<Node> pendingFeatures){
		double bestCandidateCMUT, currentCandidateCMUT = 0.0;
		PDGVariableNode bestCandidateParent, currentCandidateParent, cVarNode;
		cVarNode = this.variableForest.elementAt(0);
		// make sure that returnvalues are initialised
		bestCandidateParent = cVarNode;
		Node retval = pendingFeatures.firstElement();
		bestCandidateCMUT = 0.0;
		featureParentAndScore bestCandidate = null;
		for(Node feature : pendingFeatures){
			parentAndScore pac = getBestParentForFeatureCMUT((FiniteStates)feature, cVarNode, maxLevel);
			if(bestCandidate == null ||
				pac.score > bestCandidate.score){
				bestCandidate = new featureParentAndScore(feature, pac.parent, pac.score);
			}
		}
		return bestCandidate;
	}
	
	private featureParentAndScore getNextFeatureCR(Vector<Node> pendingFeatures, DataBaseCases trainData, DataBaseCases valData, int classNumber) throws PDGVariableNotFoundException{
		double bestCandidateScore, currentCandidateScore = 0.0;
		PDGVariableNode bestCandidateParent, currentCandidateParent, cVarNode;
		cVarNode = this.variableForest.elementAt(0);
		// make sure that returnvalues are initialised
		bestCandidateParent = cVarNode;
		bestCandidateScore = Double.NEGATIVE_INFINITY;
		featureParentAndScore bestCandidate = null;
		for(Node feature : pendingFeatures){
			parentAndScore pac = getBestParentForFeatureCR((FiniteStates)feature, cVarNode, maxLevel, trainData, valData, classNumber);
			if(bestCandidate == null ||
				pac.score > bestCandidate.score){
				bestCandidate = new featureParentAndScore(feature, pac.parent, pac.score);
			}
		}
		return bestCandidate;
	}
	
	private void addParameterNodePartitionToMap(featureParentAndScore fpc, 
			PDGVariableNode cVarNode, 
			PDGVariableNode featureVarNode,
			Map<PDGVariableNode, Vector< Vector<PDGParameterNode>>> map){
		if(fpc.parent == cVarNode){
			Vector< Vector<PDGParameterNode>> avp = new Vector< Vector<PDGParameterNode> >();
			for(int label = 0; label < cVarNode.getNumStates(); label++){
				Vector<PDGParameterNode> v = new Vector<PDGParameterNode>();
				v.add(fpc.parent.getParameterNodesCopy().firstElement().succ(featureVarNode, label));
				avp.add(v);
			}
			map.put(featureVarNode, avp);
		} else {
			Vector< Vector<PDGParameterNode>> part = map.get(fpc.parent);
			Vector< Vector<PDGParameterNode>> newpart = new Vector< Vector<PDGParameterNode>>();
			for(int label = 0; label < part.size(); label++){
				Vector<PDGParameterNode> vp = part.elementAt(label);
				Vector<PDGParameterNode> newvp = new Vector<PDGParameterNode>();
				for(PDGParameterNode p : vp){
					for(int states = 0; states < p.getPDGVariableNode().getNumStates(); states++){
						newvp.add(p.succ(featureVarNode, states));
					}
				}
				newpart.add(newvp);
			}
			map.put(featureVarNode, newpart);
		}
	}
	
	private void collapse(PDGVariableNode featureVarNode) throws PDGException, VectorOpsException {
		System.out.print("collapsing zero nodes...");
		int collapsed = 0;
		collapsed = collapseZeroReachNodesLocal(featureVarNode.getParameterNodesCopy(),5);
		System.out.print(collapsed+" nodes removed\n");
	}
	private void learnClassifier(DataBaseCases data, 
			int idxClassVar, 
			boolean verbose, int mergeLevel) throws PDGException, VectorOpsException{
		System.out.println("Building Hierachical PDG classifier:");
		//PDG resultPDG = new PDG();
		this.variableForest.clear();
		NodeList nl = data.getVariables();
		Vector<Node> variables = nl.getNodes();
		DataBaseCases valData = new DataBaseCases();
		DataBaseCases trainData = new DataBaseCases();

		// divide data into training and validation folds
		if(this.useValidationData){
			data.divideIntoTrainAndTest(trainData, valData, 0.7, this.rnd);
		} else {
			trainData = data;
			valData = data;
		}
		CaseListMem trainDataCases = (CaseListMem)trainData.getCases();
		//double laplaceSmooth = this.smooth ? (1.0 / trainDataCases.getNumberOfCases()) : 0.0;

		//int trainSize = trainDataCases.getNumberOfCases();
		Vector<Node> pending = new Vector<Node>(variables);
		FiniteStates classVar = (FiniteStates)pending.remove(idxClassVar);
		PDGVariableNode cVarNode = new PDGVariableNode(classVar);
		PDGParameterNode cParNode = new PDGParameterNode(cVarNode);
		cParNode.initializeReach(nl);
		cParNode.updateReach(trainDataCases);

		cParNode.updateValuesFromReach(this.smooth);

		this.addTree(cVarNode);
		//double pseudoCount = 0.001;
		//HashMap<PDGVariableNode, Vector<Vector<PDGParameterNode>>> map;
		//if(mm == mergeMethod.CLASS_AWARE){ 
		//	map = new HashMap<PDGVariableNode, Vector<Vector<PDGParameterNode>>>();
		//} else {
		//	map = null;
		//}
		double currentClassificationRate = ClassifierEvaluator.testClassifier(this, valData, idxClassVar).rate();
		while(!pending.isEmpty()){
			//if(currentClassificationRate >= 0.0) 

			featureParentAndScore fpc = null;
			switch(varInclusionCriteria){
			case MAX_CMUT:
				fpc = getNextFeatureCMUT(pending);
				break;
			case MAX_CR:
				fpc = getNextFeatureCR(pending, trainData, valData, idxClassVar);
				break;
			}
			PDGVariableNode featureVarNode = new PDGVariableNode((FiniteStates)fpc.feature);
			if(this.selectiveLearning
			   && this.varInclusionCriteria == variableInclusionCriteria.MAX_CR){
				if(fpc.score < currentClassificationRate){
					System.out.println("Current CR: "+currentClassificationRate+"\n" +
							           "Adding '"+fpc.feature.getName()+"' yields CR: "+fpc.score+"\n" +
							           pending.size()+" feature variables in pending.");
					//new PDGParameterNode(featureVarNode);
					//this.addTree(featureVarNode);
					//continue;
					return;
				}
			}
			System.out.println("Adding "+fpc.feature.getName()+" under "+fpc.parent.getName()+" (score: "+fpc.score+", depth: "+featureVarNode.getDepthFromRoot()+")");
			fpc.parent.addFullyExpandedSuccessor(featureVarNode);
			//if(mm == mergeMethod.CLASS_AWARE){
			//	addParameterNodePartitionToMap(fpc,cVarNode,featureVarNode,map);
			//}
			
			
			if(this.collapseEnabled) collapse(featureVarNode);
			if(this.mergeEnabled){
				if(featureVarNode.getDepthFromRoot() > mergeLevel){
					 mergeNodesBotomUp(featureVarNode, mergeLevel, valData, trainData, idxClassVar, null);
				}
			}
			this.updateReach(trainDataCases);
			this.learnParametersFromReach(smooth);
			pending.remove(fpc.feature);
			currentClassificationRate = ClassifierEvaluator.testClassifier(this, valData, idxClassVar).rate();
			System.out.println("classification rate : "+currentClassificationRate);
		}
		if(this.finalMergeEnabeled)
			mergeNodes(valData, trainData, idxClassVar);
		this.learnParameters(trainDataCases, this.smooth);
		this.printStats();
	}
	
	public void learnParameters__(CaseList cl) throws PDGVariableNotFoundException{
		clearCounts();
		int size = cl.getNumberOfCases();
		for(int i=0;i<size;i++){
			countConfiguration(cl.get(i));
		}
	}
	
	protected double mergeScore(PDGParameterNode p1, PDGParameterNode p2){
		double ms = p1.getInflow()*VectorOps.KLDivergence(p1.getValues(), p2.getValues()) + 
			p2.getInflow()*VectorOps.KLDivergence(p2.getValues(), p1.getValues());
		return ms;
	}
	
	protected double mergeScoreGeneralisationRate(PDGParameterNode p1, 
			PDGParameterNode p2, 
			DataBaseCases validationData, 
			DataBaseCases train, 
			int classnumber, double currentClassificationRate) throws PDGException, VectorOpsException{
		if(p1.getPDGVariableNode() != p2.getPDGVariableNode()){
			throw new PDGException("Can not merge PDGParameterNode's from different PDGVariableNode's!!!");
		}
		PDGVariableNode var = p1.getPDGVariableNode();
		double oldRate = currentClassificationRate;
		//double laplaceSmooth = 1.0 / train.getNumberOfCases();
		if(oldRate < 0.0)
			oldRate = ClassifierEvaluator.testClassifier(this, validationData, classnumber).rate();
		PDGClassifier testPDG = this.copy();
		FiniteStates fs = var.getFiniteStates();
		Vector<PDGParameterNode> vpn = var.getParameterNodesCopy();
		PDGVariableNode n = testPDG.getPDGVariableNode(fs);
		Vector<PDGParameterNode> testvpn = n.getParameterNodesCopy();
		PDGParameterNode testp1 = testvpn.elementAt(vpn.indexOf(p1));
		PDGParameterNode testp2 = testvpn.elementAt(vpn.indexOf(p2));
		testp1.safeMerge(testp2, true, this.smooth);
		//testPDG.learnParameters(train.getCases(), this.smooth);
		classificationResult cr = ClassifierEvaluator.testClassifier(testPDG, validationData, classnumber);
		return cr.rate() - oldRate;
	}
	
	protected void redirectEdge(PDGParameterNode tail, PDGVariableNode variableSuccessor, int value, PDGParameterNode newHead){
		PDGParameterNode oldHead = tail.succ(variableSuccessor, value);
		tail.setSuccessor(newHead, variableSuccessor, value);
		//CaseList oldReach = tail.selectFromReach(value);
		//oldHead.removeFromReach(oldReach);
		oldHead.safeRemoveParent(tail);
	}
	
	protected int collapseZeroReachNodes()throws PDGException, VectorOpsException{
		return collapseZeroReachNodes(this.minimumDataSupport);
	}
	
	protected int collapseZeroReachNodesLocal(Vector<PDGParameterNode> pv, int triviallityLevel) throws PDGException, VectorOpsException{
		PDGParameterNode trashNode = null;
		int nodesCollapsed = 0;
		PDGVariableNode pdgVar = pv.firstElement().getPDGVariableNode();
		for(PDGParameterNode pnode : pv){
			CaseListMem reach = pnode.getReach();
			if(reach.getNumberOfCases() <= triviallityLevel * (pdgVar.getNumStates() - 1)){
				if(trashNode == null){
					trashNode = pnode; 
				} else {
					trashNode.safeMerge(pnode, this.smooth);
					nodesCollapsed++;
				}
			}
			this.sumOfCollapses += nodesCollapsed;
		}
		return nodesCollapsed;
	}
	
	protected int collapseZeroReachNodes(int triviallityLevel) throws PDGException, VectorOpsException{
		//Stack<PDGVariableNode> pending = new Stack<PDGVariableNode>();
		//for(PDGVariableNode p : variableForest)
		//	pending.push(p);
		Stack<PDGVariableNode> pending = super.getDepthFirstStack();
		PDGVariableNode current;
		int nodesCollapsed = 0;
		while(!pending.empty()){
			current = pending.pop();
			nodesCollapsed += collapseZeroReachNodesLocal(current.getParameterNodesCopy(), triviallityLevel);
			//for(PDGVariableNode p : current.getSuccessors())
			//	pending.push(p);
		}
		return nodesCollapsed;
	}
	
	
	/**
	 * This method prints various statistics collected from the learning
	 * processes that this object has performed. In a K-fold crossvalidation 
	 * setting where typically the learn method is invoked for each of the K
	 * training splits, this method prints interesting statistics. The following
	 * is collected for each invocation of the learn method:
	 * <ul>
	 * <li>learning time
	 * <li>number of merged nodes
	 * <li>number of collapsed nodes
	 * </ul>
	 * For each of the above quantities this method prints mean, variance and 
	 * standard deviation.
	 */
	public void printLearningStatistics(){
		double[] lt = new double[learningTime.size()];
		double[] merges = new double[numMerges.size()];
		double[] collapses = new double[numCollapses.size()];
		int i = 0;
		for(long t : learningTime) lt[i++] = t;
		i=0;
		for(int m : numMerges) merges[i++] = m;
		i=0;
		for(int c : numCollapses) collapses[i++] = c;
		System.out.println("Statistics of Learning procedure");
		System.out.println("--------------------------------");
		System.out.println("Time :");
		VectorOps.printMeanVarSD(lt, false);
		System.out.println("Merge operation : ");
		VectorOps.printMeanVarSD(merges, false);
		System.out.println("Collapse operations : ");
		VectorOps.printMeanVarSD(collapses, false);
	}
	
	public Vector<Double> classify(Configuration conf, int classVarIdx){
		FiniteStates cv = conf.getVariable(classVarIdx);
		conf.remove(cv);
		double[] retval = new double[cv.getNumStates()];
		try{
			insertEvidence(conf);
			updateBeliefs();
			retval = getBelief(cv);
		}	catch(PDGException e){
			e.printStackTrace();
			System.out.println("Problems with classification of incompatible configuration. We will use the classlabel with maximal prior and continue, but you probably want to investigate this - it is not supposed to happen.");
			removeEvidence();
			updateBeliefs();
			try{
				retval = getBelief(cv);
			} catch(PDGException pdge){
				pdge.printStackTrace();
				retval = VectorOps.getNormalisedUniformDoubleArray(cv.getNumStates());
			}
		}
		Vector<Double> vretval = new Vector<Double>();
		for(int i=0;i<retval.length;i++) 
			vretval.add(i, retval[i]);
		return vretval;
	}

	
	private void addReachedParameterNodesBelow(PDGVariableNode pdgvn, 
			PDGVariableNode succ, NodeList variables){
		CaseListMem reach, reachNew;
		PDGParameterNode succPnode = null;
		Vector<Integer> zeroReachIndex = new Vector<Integer>();
		for(PDGParameterNode pnode : pdgvn.getParameterNodesCopy()){
			reach = pnode.getReach();
			for(int h=0;h<pdgvn.getNumStates();h++){
				reachNew = CasesOps.selectFromWhere(reach, pdgvn.getFiniteStates(), h);
				if(reachNew.getNumberOfCases() > 0){
					succPnode = new PDGParameterNode(succ);
					succPnode.initializeReach(variables);
					succPnode.updateReach(reachNew);
					pnode.setSuccessor(succPnode, succ, h);
					pnode.setCount(h, reachNew.getNumberOfCases());
				} else { zeroReachIndex.add(h); }
			}
			for(int h : zeroReachIndex){
				pnode.setSuccessor(succPnode, succ, h);
			}
			pnode.updateValuesFromCount(false);
		}
	}
	
	/**
	 * This method builds a PDG representation of the training data, that is, 
	 * every unique data instance will correspond to a unique path in the PDG structure.
	 * This PDG will inherently overfit the training data and subsequent merges is usually 
	 * performed to increase classification rate.
	 * 
	 * @param classVar - This classification variable. It will be included as the last variable
	 * in the PDG, that is, it will be the leaf of the linear tree-structure.
	 * 
	 * @return the constructed PDG model.
	 */
	public void buildLinearDataPDG(FiniteStates classVar, DataBaseCases data){
		System.out.print("Building a PDG representation of the data ");		
		CaseListMem cases = data.getCaseListMem();
		//PDG linearPdg = new PDG();
		this.variableForest.clear();
		double laplaceSmooth = 1.0 / data.getNumberOfCases();
		PDGVariableNode varNode, lastVarNode = null;
		NodeList nl = new NodeList(cases.getVariables());
		Vector<FiniteStates> variables = new Vector<FiniteStates>(cases.getVariables());
		variables.remove(classVar);
		// construct root PDGVariableNode
		FiniteStates currentVar = variables.remove(0);
		varNode = new PDGVariableNode(currentVar);
		PDGParameterNode p = new PDGParameterNode(varNode);
		p.initializeReach(nl);
		// all cases will reach the root node
		p.updateReach(cases);
		lastVarNode = varNode;
		this.addTree(varNode);
		//System.out.println(currentVar.getName()+" ["+varNode.getParameterNodes().size()+"]");
		System.out.print(".");
		for(FiniteStates var : variables){
			varNode = new PDGVariableNode(var);
			lastVarNode.addSuccessor(varNode);
			addReachedParameterNodesBelow(lastVarNode, varNode, nl);
			lastVarNode = varNode;
			System.out.print(".");
		}
		PDGVariableNode cVarNode = new PDGVariableNode(classVar);
		lastVarNode.addSuccessor(cVarNode);
		addReachedParameterNodesBelow(lastVarNode, cVarNode, nl);
		
		for(PDGParameterNode pn : cVarNode.getParameterNodesCopy()) 
			pn.updateValuesFromReach(this.smooth);
		System.out.println(". done.");
		this.printStats();
		//return linearPdg;
	}
	
	public static class NaiveBayesPDG{
		public static void main(String argv[]) throws IOException, ParseException, PDGException, InvalidEditException {
			DataBaseCases train = null, test = null;
			if (argv.length == 1){
				DataBaseCases tmp = new DataBaseCases(argv[0]);
				train = new DataBaseCases();
				test  = new DataBaseCases();
				tmp.divideIntoTrainAndTest(train, test, 1.0/3.0);
			} else if(argv.length == 2){
				train = new DataBaseCases(argv[0]);
				test = new DataBaseCases(argv[1]);
			} else {
				System.out.println("usage: PDGLearner$NaiveBayesPDG <training data> <test data>\n" +
						"The second argument optioinal - when only one argument is given 1/3 of the \n" +
						"training data is used for testing.");
				System.exit(0);
			}
			PDGClassifier classifier = new PDGClassifier(0);
			int cVarIdx = train.getClassId();
			//classifier.learnHierarchicalNaiveClassifier(train, cVarIdx);
			PDGio.save(classifier, argv[0]+".pdg");
			ClassifierEvaluator comp = new ClassifierEvaluator(train, 1, cVarIdx);
			System.out.println("performing 5-fold crossvalidation.");
			ClassifierEvaluator.classificationResult[] cr = comp.kFoldCrossValidation(5, classifier, true);
			System.out.println("Result of 5-fold crossvalidation");
			ClassifierEvaluator.printKFoldStatistics(cr, false, false);
		}
	}

	public static class BuildDataPDG{
		public static void main(String argv[]) throws IOException, ParseException, PDGException{
			DataBaseCases train = null, test = null;
			FiniteStates classVar;
			String trainFile = "", testFile = "";
			if (argv.length == 1){
				trainFile = argv[0];
				DataBaseCases tmp = new DataBaseCases(trainFile);
				train = new DataBaseCases();
				test  = new DataBaseCases();
				tmp.divideIntoTrainAndTest(train, test, 1.0/3.0);
			} else if(argv.length == 2){
				trainFile = argv[0]; testFile = argv[1];
				train = new DataBaseCases(trainFile);
				test = new DataBaseCases(testFile);
			} else {
				System.out.println("usage: PDGLearner$BuildDataPDG <training data> <test data>\n" +
						"The second argument optioinal - when only one argument is given 1/3 of the \n" +
						"training data is used for testing.");
				System.exit(0);
			}

			PDGClassifier classifier = new PDGClassifier();
			int cVarIdx = train.getClassId();
			classVar = (FiniteStates)train.getVariables().elementAt(cVarIdx);
			classifier.buildLinearDataPDG(classVar, train);
			//dataPDG.learnParameters(train.getCases(), 0.1);
			
			PDGVariableNode pdgClassVar = classifier.getPDGVariableNode(classVar);
			if(pdgClassVar == null){
				System.out.println("did not find classVariable : "+classVar.getName());
				classifier.printNames();
			}
			PDGio.save(classifier, train.getName()+".pdg");
			ClassifierEvaluator.classificationResult cr;
			cr = ClassifierEvaluator.testClassifier(classifier, test, cVarIdx);
			cr.printStatistics();
			//cr = ClassifierEvaluator.getClassificationRate(classifier, cVarIdx, test.getCases(), true);
			//System.out.println("cr : "+cr.rate());
			//System.out.println("ll : "+cr.logLikelihoodPerCase());
		}
	}
	
	private class PDGLink{
		public final PDGParameterNode head;
		public final PDGParameterNode tail;
		public final CaseListMem flow;
		public final int label;
		public PDGLink(PDGParameterNode t, PDGParameterNode h, CaseListMem f, int l){
			tail = t;
			head = h;
			flow = f;
			label = l;
		}
	}
	
	private static void printHelp(){
		System.out.println("usage : PDGClassifier <max-depth> <training-data> <random-seed> <collapse-zero-support> <merge>\n" +
				"----------------------------------------------------------------------------\n" +
				"<max-depth>           - the maximal depth of the PDG. Depth 0 corresponds to naive\n" +
				"                        bayes.\n" +
				"<training-data>       - data to use for training.\n" +
				"<random-seed>         - seed for random function, if negative the current system time will be used.\n" +
				"<collapse-zero-merge> - 'true' will enable collapsing, 'false' will disable\n" +
				"<merge>               - 'true' will enable merging, 'false' will disable\n" +
				"----------------------------------------------------------------------------");
	}
	
	public static void main(String argv[]){
		if(argv.length != 6){ 
			printHelp(); 
			System.exit(0);
		}
		try{
			int maxDepth = Integer.parseInt(argv[0]);
			DataBaseCases train = new DataBaseCases(argv[1]);
			//DataBaseCases test = new DataBaseCases(argv[2]);
			long seed = Long.parseLong(argv[2]);
			if(seed < 0){
				seed = System.currentTimeMillis();
			}
			boolean collapse = Boolean.parseBoolean(argv[3]);
			boolean merge = Boolean.parseBoolean(argv[4]);
			PDGClassifier classifier = new PDGClassifier(maxDepth);
			classifier.setCollapseEnabled(collapse);
			classifier.setMergeEnabled(merge);
			classifier.setSeed(seed);
			classifier.setMergeTimeLimit(120000);
			classifier.setMinimumDataSupport(50);
			int cVarIdx = train.getClassId();
			int K=5;
			ClassifierEvaluator cv = new ClassifierEvaluator(train, seed, cVarIdx);
			ClassifierEvaluator.classificationResult[] results = cv.kFoldCrossValidation(K, classifier, true);
			System.out.println("results:");
			ClassifierEvaluator.printKFoldStatistics(results, false, false);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public long size(){
		return this.numberOfIndependentParameters();
	}
	
	public void saveModelToFile(String ap) throws IOException {
		String fn = new String(ap);
		fn += "pdg-"+(this.fanLearning ? "fan" : "direct");
		PDGio.save(this, fn+".pdg");
	}


	private static class TestFanLearning{
		public static void main(String argv[]) throws FileNotFoundException, IOException, ParseException, StateNumberException, PDGIncompatibleEvidenceException, PDGVariableNotFoundException{
			DataBaseCases dbc = new DataBaseCases(argv[0]);

			TreeAugmentedNaiveBayes tan = new TreeAugmentedNaiveBayes();
			tan.learn(dbc, dbc.getClassId());
			//classifier.setCollapseEnabled(false);
			//classifier.setMergeEnabled(false);
			//classifier.setFinalMerge(false);
			//classifier.setSeed(1);
			//classifier.setMinimumDataSupport(5);
			//classifier.setVariableInclusionCriteria(variableInclusionCriteria.MAX_CR);
			//classifier.setSmooth(true);
			//classifier.setFanLearning(true);
			//classifier.setUseValidationData(false);
			PDG model = new PDG(tan.getClassifier(), (FiniteStates)dbc.getNodeList().elementAt(dbc.getClassId()));
			System.out.println("1) before learning parameters");
			model.checkBnetEquivalence(tan.getClassifier(), true);
			model.learnParameters(dbc.getCases(), true);
			System.out.println("2a) after learning parameters");
			model.checkBnetEquivalence(tan.getClassifier(), true);
			model.updateReach(dbc.getCaseListMem());
			model.learnParametersFromReach(true);
			System.out.println("2b) after learning parameters using reach field");
			model.checkBnetEquivalence(tan.getClassifier(), true);
		}
	}
}
