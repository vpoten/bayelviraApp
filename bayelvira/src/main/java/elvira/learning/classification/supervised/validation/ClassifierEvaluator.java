package elvira.learning.classification.supervised.validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import elvira.*;
import elvira.tools.*;
import elvira.tools.CmdLineArguments.CmdLineArgumentsException;
import elvira.tools.CmdLineArguments.argumentType;
import elvira.database.DataBaseCases;
import elvira.learning.classification.*;
import elvira.learning.classification.supervised.discrete.CMutInfTAN;
import elvira.parser.ParseException;
import elvira.probabilisticDecisionGraph.PDG;
import elvira.probabilisticDecisionGraph.PDGException;
import elvira.probabilisticDecisionGraph.PGM;
import elvira.probabilisticDecisionGraph.tools.MathUtils;
import elvira.probabilisticDecisionGraph.tools.VectorOps;
import elvira.learning.classification.supervised.discrete.*;
import elvira.learning.classificationtree.*;

public class ClassifierEvaluator {

	private final DataBaseCases data;
	private final Random rand;
	private final int classIdx;
	//private Vector<CaseListMem> folds = new Vector<CaseListMem>();
	
	public ClassifierEvaluator(DataBaseCases dbc, long seed, int classVariableId){
		classIdx = classVariableId;
		data = dbc;
		rand = new Random(seed);
	}
	
	public static classificationResult testClassifier(SizeComparableClassifier model, DataBaseCases test, int classnumber){
		FiniteStates target = (FiniteStates)test.getVariables().elementAt(classnumber);
		int numTargetStates = target.getNumStates();
		classificationResult cr = new classificationResult(numTargetStates);
		CaseListMem testcl = test.getCaseListMem();
		Double[] targetPosterior = new Double[numTargetStates];
		Configuration instance;
		int correctLabel, predictedLabel, numCases = testcl.getNumberOfCases();
		
		for(int i=0;i<numCases;i++){
			instance = test.getCaseListMem().get(i).duplicate();
			correctLabel = instance.getValue(target);
			model.classify(instance, classnumber).toArray(targetPosterior);
			predictedLabel = VectorOps.getIndexOfMaxValue(targetPosterior);
			cr.addResult(correctLabel, predictedLabel, targetPosterior);
		}
		cr.modelSize = model.size();
		return cr;
	}
		
	public classificationResult[] kFoldCrossValidation(int K, SizeComparableClassifier model, boolean saveModels) throws InvalidEditException {
		
		//splitCases(K);
		classificationResult[] results = new classificationResult[K];
	    String prefix = System.getProperty("user.dir");
		prefix+="/models";
		File dir = new File(prefix);
		if(!dir.exists()) dir.mkdir();
	    for(int fold = 0;fold < K;fold++){
			DataBaseCases test  = data.getTestCV(fold, K);// new DataBaseCases("test"+fold+".dbc",folds.elementAt(fold));
			DataBaseCases train = data.getTrainCV(fold, K); //mergeCasesExcludeFold(fold);
			model.learn(train, classIdx);
			results[fold] = testClassifier(model, test, classIdx);
			if(saveModels){
				try{
					File localDir = new File(prefix+"/"+fold);
					if(!localDir.exists()) localDir.mkdir();
					model.saveModelToFile(localDir.getAbsolutePath()+"/");
				} catch(IOException ioe){
					System.out.println("Could not save model - the following exception occured:");
					ioe.printStackTrace();
					System.out.println("... we continue, but probably you want to do something about this!");
				}
			}
		}
		return results;
	}
	
	public static final void printKFoldStatistics(classificationResult[] results, boolean verbose, boolean horizontal){
		double[] rates = new double[results.length];
		double size = 0;
		for(int i=0; i < results.length;i++){
			if(verbose){
				System.out.println("-------Fold "+i+"-------");
				results[i].printStatistics();
			}
			rates[i] = results[i].rate();
			size += results[i].modelSize;
		}
		VectorOps.printMeanVarSD(rates, horizontal);
		System.out.print("\t"+(size / results.length));
	}
	
/*	public DataBaseCases mergeCasesExcludeFold__(int fold){
		CaseListMem retval = new CaseListMem(data.getVariables());
		for(int i=0; i< folds.size(); i++){
			if(i == fold) continue;
			retval.appendCases(folds.elementAt(i));
		}
		return new DataBaseCases("train.dbc",retval);
	}
	*/
	
	/*private void splitCases__(int K) throws InvalidEditException{
		Vector<Configuration> casesVect = new Vector<Configuration>();
		CaseListMem clm = this.data.getCaseListMem();
		Configuration conf;
		for(int i=0;i<clm.getNumberOfCases();i++) casesVect.add(clm.get(i));
		
		CaseListMem[] folds = new CaseListMem[K];
		for(int i=0;i<K;i++) folds[i] = new CaseListMem(this.data.getVariables());
		while(!casesVect.isEmpty()){
			for(int i=0;i<K && !casesVect.isEmpty();i++){
				int pos = this.rand.nextInt(casesVect.size());
				conf = casesVect.elementAt(pos);
				casesVect.removeElementAt(pos);
				folds[i].put(conf);
			}
		}
		this.folds.clear();
		for(int i=0;i<K;i++){
			this.folds.add(folds[i]);
		}
	}*/

	public static class classificationResult{
		private double[] predictedCounts;
		private double[] counts;
		private int correct = 0, total = 0;
		private double ll;
		public long modelSize;
		public classificationResult(int numLabels){
			predictedCounts = new double[numLabels];
			counts = new double[numLabels];
			ll = 0.0;
		}
		
		public double rate(){
			return ((double)correct)/total;
		}
		
		public double logLikelihood(){
			return ll;
		}
		
		public double logLikelihoodPerCase(){
			return ll/total;
		}
		
		public void addResult(int correctLabel, int predictedLabel, Double[] classPosterior){
			total++;
			predictedCounts[predictedLabel]++;
			counts[correctLabel]++;
			ll += MathUtils.log2(classPosterior[predictedLabel]);
			if(correctLabel == predictedLabel) correct++;
		}
		
		public void addResult(int correctLabel, int predictedLabel, double[] classPosterior){
			total++; 
			predictedCounts[predictedLabel]++; 
			counts[correctLabel]++; 
			ll += MathUtils.log2(classPosterior[predictedLabel]);
			if(correctLabel == predictedLabel) correct++;
		}
		
		public double[] getDistributionOfPredictions(){
			return VectorOps.copyAndNormalise(predictedCounts);
		}
		
		//public double kFoldCVErrorRate(int k, DataBaseCases dbc){
		//	return -1.0;
		//}
		
		public double[] getLabelDistributionOfLabels(){
			return VectorOps.copyAndNormalise(counts);
		}
		
		public void printStatistics(){
			System.out.println("Distribution of predictions : "+VectorOps.doubleArrayToString(VectorOps.copyAndNormalise(predictedCounts))+"\n" +
							   "Distribution of labels      : "+VectorOps.doubleArrayToString(VectorOps.copyAndNormalise(counts))+"\n" +
							   "Classification rate         : "+this.correct+"/"+this.total+" = "+this.rate()+"\n");
		}
	}
	
	private static void addCT(int method, String name, Vector<SizeComparableClassifier> models, Vector<String> mNames){
		ClassificationTree model = new ClassificationTree(method, ClassificationTree.EBP, 0.25);
		models.add(model);
		System.out.println("adding Classification tree ("+name+" + error based pruning)");
		mNames.add(name+"-EDP");
		model = new ClassificationTree(method, ClassificationTree.REP, 0.25);
		models.add(model);
		System.out.println("adding Classification tree ("+name+" + reduced error pruning)");
		mNames.add(name+"-REP");
		model = new ClassificationTree(method, ClassificationTree.NONE, 0.25);
		models.add(model);
		System.out.println("adding Classification tree ("+name+" + no pruning)");
		mNames.add(name+"-NONE");
	}
	
	private static final String argMaxDepth = "max-depth";
	private static final String argCollapse = "collapse";
	private static final String argMerge = "merge";
	private static final String argMergeFinal = "merge-final";
	private static final String argSplit = "split";
	private static final String argPdg = "pdg";
	private static final String argNBTAN = "nbtan";
	private static final String argNB = "nb";
	private static final String argC45 = "c45";
	private static final String argID3 = "id3";
	private static final String argDir = "dir";
	private static final String argKDB = "kdb";
	private static final String argData = "data";
	private static final String argRndSeed = "random-seed";
	private static final String argSaveModels = "save-models";
	private static final String argPDGVarSel = "pdg-var-sel";
	private static final String argSelectivePDG = "pdg-selective";
	private static final String argSmoothing = "smoothing";
	private static final String argFanToPdg = "fan-to-pdg";
	//private static final String argSelectiveNB = "selective-nb";
	private static final String argUseValData = "pdg-validationdata";
	
	public static void main(String argv[]) throws CmdLineArgumentsException {
 		CmdLineArguments params = new CmdLineArguments();
		params.addArgument(argData, argumentType.s, "", "The filename of the database (.dbc format). No default value, must be provided.");
		params.addArgument(argMaxDepth, argumentType.i, ""+Integer.MAX_VALUE, "The maximal depth of the PDGClassifier. 0 corresponds to naivebayes. Default is equal to the number of features in the database, which means unconstrained learning.");
		params.addArgument(argCollapse, argumentType.b, "true", "Value 'true' will enable collapsing parameternodes of the PDGClasifier, 'false' will disable. Default is 'true'.");
		params.addArgument(argMerge, argumentType.b, "true", "Value 'true' will enable merging of parameternodes of the , 'false' will disable.");
		params.addArgument(argMergeFinal, argumentType.b, "false", "Value 'true' will enable a final refinement of the model, default is 'false'.");
		//params.addArgument(argSplit, argumentType.b, "false", "(NOT IMPLEMENTED YET)");
		params.addArgument(argPdg, argumentType.b, "true", "Value 'true' will include the PDG classifier, 'false' will exclude. Default is 'true'.");
		params.addArgument(argC45, argumentType.b, "true", "Value 'true' will include the Classification Tree (C4.5), 'false' will exclude. Default is 'true'.");
		params.addArgument(argID3, argumentType.b, "true", "Value 'true' will include the Classification Tree (ID3), 'false' will exclude. Default is 'true'.");
		params.addArgument(argDir, argumentType.b, "true", "Value 'true' will include the Classification Tree (Dirichlet), 'false' will exclude. Default is 'true'.");
		params.addArgument(argNBTAN, argumentType.b, "true", "Value 'true' will include the Tree-augmented Naive Bayes classifier, 'false' will exclude. Default is 'true'.");
		params.addArgument(argNB, argumentType.b, "true", "Value 'true' will include the Naive Bayes classifier, 'false' will exclude. Default is 'true'.");
		params.addArgument(argKDB, argumentType.b, "true", "Value 'true' will include the KDB classifier, 'false' will exclude. Default is 'true'.");
		params.addArgument(argRndSeed, argumentType.l, ""+System.currentTimeMillis(), "The seed for random function. Default is the current system time.");
		params.addArgument(argSaveModels,argumentType.b, "true", "Value 'true' will save the models, 'false' will not do this. 'true' is default.");
		params.addArgument(argPDGVarSel,argumentType.s, ""+PDGClassifier.variableInclusionCriteria.MAX_CR, "Sets the evaluations function. Value '"+PDGClassifier.variableInclusionCriteria.MAX_CMUT+"' enables the use of conditional mutual information when constructing the variable tree of the PDG classifier, value '"+
				PDGClassifier.variableInclusionCriteria.MAX_CR+"' enabels the use of classification rate, value '"+PDGClassifier.variableInclusionCriteria.MAX_CMUT+"' enables conditional mutual information criteria.");
		params.addArgument(argSelectivePDG, argumentType.b, "false", "Value 'true' will enable feature selection in the learning (using a wrapper approach) - false will disable this.");
		params.addArgument(argSmoothing, argumentType.b, "true", "Value 'true' will enable smooting of parameters in pdg models, 'false' will disable.");
		params.addArgument(argFanToPdg, argumentType.b, "true", "Value 'true' will enable FanToPDG learning of pdg models, 'false' will disable. (A TAN model is used and not the more general FAN model.)");
		//params.addArgument(argSelectiveNB, argumentType.b, "true", "Value 'true' will include the NB model with feature selecetion (wrapper approach) - false will exclude this model.");
		params.addArgument(argUseValData, argumentType.b, "false", "'true' enables the use of validation data in pdg-learning. Default is 'false'");
		//params.addArgument(new argument())
		params.parseArguments(argv);
		params.print();
		String dataFile    = params.getString(argData);
		long rndSeed       = params.getLong(argRndSeed);
		int maxDepth       = params.getInteger(argMaxDepth);
		boolean collapse   = params.getBoolean(argCollapse);
		boolean merge      = params.getBoolean(argMerge);
		boolean mergeFinal = params.getBoolean(argMergeFinal);
		//boolean split      = params.getBoolean(argSplit);
		boolean usePDG     = params.getBoolean(argPdg);
		boolean useNB      = params.getBoolean(argNB);
		boolean useNBTAN   = params.getBoolean(argNBTAN);
		boolean useKDB     = params.getBoolean(argKDB);
		boolean useCTc45   = params.getBoolean(argC45);
		boolean useCTdir   = params.getBoolean(argDir);
		boolean useCTid3   = params.getBoolean(argID3);
		boolean saveModels = params.getBoolean(argSaveModels);
		boolean selectivePDG = params.getBoolean(argSelectivePDG);
		boolean pdgSmooth  = params.getBoolean(argSmoothing);
		boolean fanToPdg   = params.getBoolean(argFanToPdg);
		boolean useValData = params.getBoolean(argUseValData);
		//boolean selectiveNB = params.getBoolean(argSelectiveNB);
		
		PDGClassifier.variableInclusionCriteria pdgVarIncl = (params.getString(argPDGVarSel).compareToIgnoreCase(""+PDGClassifier.variableInclusionCriteria.MAX_CR) == 0 ? PDGClassifier.variableInclusionCriteria.MAX_CR : PDGClassifier.variableInclusionCriteria.MAX_CMUT);
		if(dataFile.equalsIgnoreCase("")){
			System.out.println("'data' argument not found, you must specify a datafile!!!");
			params.printHelp();
			System.exit(112);
		}
		DataBaseCases train = null;
		try {
			train = new DataBaseCases(dataFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("There was a problem loading the data file!");
			System.exit(112);
		}
		//boolean collapse = Boolean.parseBoolean(argv[3]);
		//boolean merge = Boolean.parseBoolean(argv[4]);
		//boolean includeTAN = Boolean.parseBoolean(argv[5]);
		Vector<SizeComparableClassifier> models = new Vector<SizeComparableClassifier>();
		Vector<String> modelNames = new Vector<String>();
		if(usePDG){
			PDGClassifier classifier = new PDGClassifier(maxDepth);
			classifier.setCollapseEnabled(collapse);
			classifier.setMergeEnabled(merge);
			classifier.setFinalMerge(mergeFinal);
			classifier.setSeed(rndSeed);
			classifier.setMinimumDataSupport(5);
			classifier.setVariableInclusionCriteria(pdgVarIncl);
			classifier.setSmooth(pdgSmooth);
			classifier.setUseValidationData(useValData);
			models.add(classifier);
			System.out.println("adding PDG-"+pdgVarIncl);
			modelNames.add("PDG-"+pdgVarIncl);
		}
		if(selectivePDG){
			PDGClassifier classifier = new PDGClassifier(maxDepth);
			classifier.setMergeEnabled(merge);
			classifier.setFinalMerge(mergeFinal);
			classifier.setMinimumDataSupport(5);
			classifier.setVariableInclusionCriteria(PDGClassifier.variableInclusionCriteria.MAX_CR);
			classifier.setSelectiveLearning(selectivePDG);
			classifier.setSmooth(pdgSmooth);
			models.add(classifier);
			System.out.println("adding PDG-fs");
			modelNames.add("PDG-fs");
		}
		if(useNBTAN){
			TreeAugmentedNaiveBayes tan = new TreeAugmentedNaiveBayes();
			//CMutInfTAN tan = new CMutInfTAN();
			//TAN.setRandomSeed(rndSeed);
			models.add(tan);
			System.out.println("adding TAN");
			modelNames.add("TAN");
		}
		if(useNB){
			Naive_Bayes nb = new Naive_Bayes();
			models.add(nb);
			System.out.println("adding NB");
			modelNames.add("NB");
		}
		if(useCTc45){
			addCT(ClassificationTree.C45, "c45", models, modelNames);
		}
		if(useCTid3){
			addCT(ClassificationTree.ID3, "id3", models, modelNames);
		}
		if(useCTdir){
			addCT(ClassificationTree.DIRICHLET, "dir", models, modelNames);
		}
		if(useKDB){
			int maxKDBParents = 4;
			CMutInfKDB model = new CMutInfKDB(true, maxKDBParents);
			models.add(model);
			System.out.println("adding KDB ("+maxKDBParents+")");
			modelNames.add("KDB-"+maxKDBParents);
		}
		if(fanToPdg){
			PDGClassifier classifier = new PDGClassifier(maxDepth);
			classifier.setCollapseEnabled(collapse);
			classifier.setMergeEnabled(merge);
			classifier.setFinalMerge(mergeFinal);
			classifier.setSeed(rndSeed);
			classifier.setMinimumDataSupport(5);
			classifier.setVariableInclusionCriteria(pdgVarIncl);
			classifier.setSmooth(pdgSmooth);
			classifier.setFanLearning(true);
			classifier.setUseValidationData(useValData);
			models.add(classifier);
			System.out.println("adding FAN-PDG-"+pdgVarIncl);
			modelNames.add("FAN-PDG-"+pdgVarIncl);
		}
		int cVarIdx = train.getClassId();
		// The following hack fixes the fact that not all classifiers
		// can handle the classvariable at an arbitrary place in the database
		// but instead expects it to be at the last index
		if(cVarIdx != (train.getVariables().size() - 1)){
			train.swapColumns(cVarIdx, train.getVariables().size() - 1);
			cVarIdx = train.getClassId();
		}
		int K=5;
		java.util.Iterator<String> itr = modelNames.iterator();
		System.out.println("# mean\t variance\t std.dev\t num-free-params");
		ClassifierEvaluator cv = new ClassifierEvaluator(train, rndSeed, cVarIdx);
		for(SizeComparableClassifier m : models){
			try{
				ClassifierEvaluator.classificationResult[] results = cv.kFoldCrossValidation(K, m, saveModels);
				System.out.print(itr.next()+" results:\t");
				ClassifierEvaluator.printKFoldStatistics(results, false, true);
				System.out.print("\n");
			} catch(InvalidEditException e){
				e.printStackTrace();
				System.out.println("We try to continue, but you should check why this exception was raised and fix the bug!!!");
			}
		}
	}	
}
