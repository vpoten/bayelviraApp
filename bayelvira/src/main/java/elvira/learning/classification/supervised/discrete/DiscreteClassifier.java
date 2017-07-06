/* DiscreteClassifier.java */

package elvira.learning.classification.supervised.discrete;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.learning.classification.ConfusionMatrix;
import elvira.learning.classification.SizeComparableClassifier;
import elvira.learning.classification.ClassifierException;
import elvira.learning.classification.AuxiliarPotentialTable;
import elvira.potential.PotentialTable;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.lang.Double;

/**
 * DiscreteClassifier.java
 *
 * The DiscreteClassifier is an abstract class. It was designed to be the
 * parent of all the discrete classifers to implement.
 * The DiscreteClassifier has the attributes and the main methods of all the
 * classification model for discrete variables.
 *
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 1.0
 * @since 26/03/2003
 */

public abstract class DiscreteClassifier implements SizeComparableClassifier, Serializable{

	static final long serialVersionUID = -8749379754388280655L;

	/**
	 * The learnt model from the database
	 */
	protected Bnet classifier;

	/**
	 * The confusionMatrix associated to the results of the test
	 */
	protected ConfusionMatrix confusionMatrix;

	/**
	 * The DataBase associated to the classifier to learn
	 */
	protected DataBaseCases cases;

	/**
	 * The number of instances of the database
	 */
	protected int nCases;

	/**
	 * The number of variables of the database and the classifier
	 */
	protected int nVariables;

	/**
	 * The class variable
	 */
	protected FiniteStates classVar;
	protected int classIndex;

	/**
	 * The cardinality of the class variable
	 */
	protected int classNumber;

	/**
	 * The laplace correction
	 */
	protected boolean laplace;

	/**
	 * The number of evaluations made to build the classifier
	 */
	protected int evaluations;

	/**
	 * The loglikelihood of the classifier given the cases
	 */
	protected double logLikelihood;

	/**
	 * Basic Constructor. Create a empty DiscreteClassifier
	 */
	public DiscreteClassifier() {
		this.laplace       = true;
		this.evaluations   = 0;
		this.logLikelihood = 0;
	}
	
	public DiscreteClassifier(boolean lap){
		this.laplace       = lap;
		this.evaluations   = 0;
		this.logLikelihood = 0;
	}

	/**
	 * Constructor
	 * @param DataBaseCases cases. The input to learn a classifier
	 * @param boolean lap. To apply the laplace correction
	 */
	public DiscreteClassifier(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
		this.cases         = data;
		this.nVariables  = this.cases.getVariables().size();
		this.nCases      = this.cases.getNumberOfCases();
		this.laplace       = lap;
		this.evaluations   = 0;
		this.logLikelihood = 0;

		NodeList    nodelist    = this.cases.getVariables();
		Node        node;
		Vector      vector      = this.cases.getRelationList();
		Relation    relation    = (Relation)vector.elementAt(0);
		CaseListMem caselistmem = (CaseListMem)relation.getValues();

		for (int i=0; i< this.nCases; i++)
			for (int j=0; j< nodelist.size(); j++) {
				node = (Node)(caselistmem.getVariables()).elementAt(j);
				if (node.getTypeOfVariable() == Node.CONTINUOUS) {
					System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
					System.exit(0);
				}
			}

		this.classVar            = (FiniteStates)this.cases.getNodeList().lastElement();
		this.classIndex 		 = this.cases.getNodeList().size()-1;
		this.classNumber         = classVar.getNumStates();
		this.confusionMatrix     = new ConfusionMatrix(this.classNumber);
	}

	/*
  public DiscreteClassifier(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
    DataBaseCases newData = data;
    if (classIndex != data.getVariables().size() - 1) { //The class variable is not in the last position
      Vector vNodes = new Vector();
      Node classVariable = data.getNodeList().elementAt(classIndex);
      for(int i= 0; i< data.getNodeList().size(); i++)
        if (i != classIndex) vNodes.addElement(data.getNodeList().elementAt(i));
      vNodes.addElement(classVariable);
      NodeList nodeList = new NodeList(vNodes);
      newData=data.copy();
      newData.projection(nodeList);      
    }

    this.cases      = newData;
  	this.nVariables = this.cases.getVariables().size();
   	this.nCases     = this.cases.getNumberOfCases();
    this.laplace    = lap;
   	NodeList    nodelist    = this.cases.getVariables();
    Node        node;
   	Vector      vector      = this.cases.getRelationList();
   	Relation    relation    = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem = (CaseListMem)relation.getValues();

    for (int i=0 ; i< this.nCases ; i++)
      for (int j=0 ; j< nodelist.size()  ; j++) {
        node = (Node)(caselistmem.getVariables()).elementAt(j);
        if (node.getTypeOfVariable() == Node.CONTINUOUS) {
          System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
          System.exit(0);
        }
      }

    FiniteStates classStates = new FiniteStates();
    classStates = (FiniteStates)(caselistmem.getVariables()).elementAt(this.nVariables-1);
    this.classNumber = classStates.getNumStates();
    this.confusionMatrix = new ConfusionMatrix(this.classNumber);
  }
	 */

	/**
	 * This abstract method learns the classifier structure
	 * It must be define in the all subclasses. 
	 * The structuralLearning depends on the classification model to learn
	 */
	public abstract void structuralLearning () throws elvira.InvalidEditException, Exception;

	/**
	 * This method assigns a class to the instances of the input instance. It
	 * returns the identifer of the assigned class. The class variable is the
	 * last variable. It uses the result of Pearl (1987) Evidential Reasoning 
	 * Using Stochastic Simulation of Causal Models.
	 * It is called by this.test(). 
	 * @param. double[] caseTest. A unidimensional array of size: <code>nVariables<code\>. The input intance of the database.
	 * @returns int. The identifier of the most probable class.
	 */
	public int assignClass(double[] caseTest) {
		int maxClass    = -1;
		double maxProb  = Double.MIN_VALUE;

		Node classNode                     = this.classifier.getNode(this.classVar.getName());
		Relation classRelation             = this.classifier.getRelation(classNode);
		PotentialTable classPotentialTable = (PotentialTable)classRelation.getValues();

		for(int c= 0; c< this.classNumber; c++) {
			double currentProb     = classPotentialTable.getValue(c);

			NodeList childrenNodes = classNode.getChildrenNodes();
			for(int i= 0; i< childrenNodes.size() ; i++) {
				FiniteStates currentNode      = (FiniteStates)childrenNodes.elementAt(i);
				Relation relation             = this.classifier.getRelation(currentNode);
				PotentialTable potentialTable = (PotentialTable)relation.getValues();

				NodeList parentsNodes = currentNode.getParentNodes();
				Vector vParentsNodes  = new Vector();
				Vector vParentsValues = new Vector();

				vParentsNodes.addElement(currentNode);
				vParentsValues.addElement(new Integer((int)(caseTest[this.cases.getNodeList().getId(currentNode)])));
				for(int p= 0; p< parentsNodes.size();p++) 
					if(! parentsNodes.elementAt(p).equals(classNode)) {
						vParentsNodes.addElement(parentsNodes.elementAt(p));
						vParentsValues.addElement(new Integer((int)(caseTest[this.classifier.getNodeList().getId((Node)parentsNodes.elementAt(p))])));
					}
				vParentsNodes.addElement(classNode);
				vParentsValues.addElement(new Integer(c));

				Configuration parentConfiguration = new Configuration(vParentsNodes, vParentsValues);
				currentProb = currentProb * potentialTable.getValue(parentConfiguration);
			}

			if (currentProb > maxProb) {
				maxProb  = currentProb;
				maxClass = c;
			}
		}

		return(maxClass);
	}

	/**
	 * This method assigns a class to the instances of the input instance. It
	 * returns the identifer of the assigned class. The class variable is the
	 * last variable
	 * It is called by this.test(). 
	 * @param. Configuration conf. A configuration containing the instance to classify
	 * @returns int. The identifier of the most probable class.
	 */
	public int assignClass(Configuration conf) {
		double[] instance = new double[this.nVariables];
		if(conf.getValues().size() != this.nVariables) {
			System.out.println("assignClass: the size of the configuration is not valid " + conf.getValues().size() + " > " + this.nVariables);
			System.exit(0);
		}
		for(int i= 0; i<this.nVariables; i++)
			instance[i] = (double)conf.getValue(i);
		return(this.assignClass(instance));
	}

	/**
	 * This method implements the looklikelihood score of the classifier with
	 * BIC penalization
	 * @param auxiliarPotentialTables a vector containig the conditional probability
	 * tables (into AuxiliarPotentialTable objects). Each element of the vector is
	 * the probability conditional table for a specific variable, beeing the last
	 * one for the class variable.
	 * @return the loglikelihood value with BIC penalization.
	 **/
	private void lookLikelihood(Vector auxPotentialTables) {
		this.logLikelihood  = 0;
		double penalization = 0;
		Iterator auxPotentialIterator = auxPotentialTables.iterator();

		for(int i= 0; i< this.classifier.getNodeList().size(); i++) {
			AuxiliarPotentialTable nodePotentialTable = (AuxiliarPotentialTable)auxPotentialIterator.next();
			int nStatesOfParents                      = nodePotentialTable.getNStatesOfParents();
			int nStatesOfVariable                     = nodePotentialTable.getNStatesOfVariable();

			penalization += nStatesOfParents * (nStatesOfVariable -1);

			for(int j= 0; j< nStatesOfParents; j++) 
				for(int k= 0; k< nStatesOfVariable; k++) 
					this.logLikelihood += nodePotentialTable.getNumerator(k,j) * 
					Math.log(nodePotentialTable.getPotential(k,j));

		}
		this.logLikelihood -= 0.5 * Math.log((new Double(this.nCases)).doubleValue()) * penalization;
	}

	/**
	 * This method makes the factorization of the classifier.
	 */
	public void parametricLearning() {
		Vector<AuxiliarPotentialTable> auxPotentialTables = new Vector<AuxiliarPotentialTable>();

		//Create a AuxiliarPoptentialTable vector (one element for each node) in
		//order to caculate the potentials of the variables
		NodeList nodeList       = this.classifier.getNodeList();
		Vector vector           = this.cases.getRelationList();
		Relation relation       = (Relation)vector.elementAt(0);
		CaseListMem caselistmem = (CaseListMem)relation.getValues();
		Node classNode          = this.classifier.getNode(this.classVar.getName());//this.classifier.getNodeList().lastElement();

		for(int i= 0; i< this.classifier.getNodeList().size(); i++) {
			AuxiliarPotentialTable aux = new AuxiliarPotentialTable(((FiniteStates)this.classifier.getNodeList().elementAt(i)));
			//The table are initialized with random values of probability
			aux.initialize(0);
			auxPotentialTables.add(aux); 
		}

		for(int l= 0; l< this.nCases; l++) {
			//Iterator<AuxiliarPotentialTable> auxPotentialIterator = auxPotentialTables.iterator();
			for(int i= 0; i< nodeList.size(); i++) {
				Node currentNode = nodeList.elementAt(i);
				AuxiliarPotentialTable auxPotTab = auxPotentialTables.elementAt(i);
				if(currentNode.equals(classNode)){
					//The class variable does not have parents
					auxPotTab.addCase((int)caselistmem.getValue(l, this.classIndex), 0, 1);
					continue;
				}
				
				NodeList parentsNode  = currentNode.getParentNodes();
				Vector<Node> vParentsNodes  = new Vector<Node>();
				Vector<Integer> vParentsValues = new Vector<Integer>();
				for(int p= 0; p< parentsNode.size(); p++) { //Parents configuration
					vParentsNodes.addElement(parentsNode.elementAt(p));
					vParentsValues.addElement(new Integer((int)caselistmem.getValue(l, this.cases.getNodeList().getId(parentsNode.elementAt(p)))));
				}
				Configuration parentsConfiguration = new Configuration(vParentsNodes, vParentsValues);
				int pos = this.cases.getVariables().getId(currentNode);
				auxPotTab.addCase((int)caselistmem.getValue(l,pos), parentsConfiguration, 1);
			}
		}

		//Save the learned potential table into the classifier
		Iterator relationListIterator       = this.classifier.getRelationList().iterator();
		Iterator auxPotentialTablesIterator = auxPotentialTables.iterator();

		for(int i= 0; relationListIterator.hasNext(); i++) {
			Relation relationC                       = (Relation)relationListIterator.next();
			AuxiliarPotentialTable auxPotentialTable = (AuxiliarPotentialTable)auxPotentialTablesIterator.next();
			PotentialTable potentialTable            = (PotentialTable)relationC.getValues();

			if (this.laplace) 
				auxPotentialTable.applyLaplaceCorrection();
			potentialTable.setValues(auxPotentialTable.getPotentialTableCases());
		}

		this.lookLikelihood(auxPotentialTables);
	}

	/**
	 * This method builds a new classifier.
	 * First, it makes a structural learning of the classifier.
	 * Finally, it factorizes the probability distributions of the database.
	 */
	public void train() throws elvira.InvalidEditException, Exception{
		this.structuralLearning();
		this.parametricLearning();
	}
	
	/**
	 * This method tests the learned classifier given a DataBaseCases.
	 * It returns the accuracy of the classifier.
	 * It requires the the class variable with assigned values in the test database.
	 * @param. DataBaseCases test. The test database of the classifier.
	 * @returns double. The accuracy of the classifier on the <code> test <code\> dataset
	 */
	public double test(DataBaseCases test) throws ClassifierException{
		DataBaseCases newtest = test;
		/*    if(!test.getVariables().equals(this.cases.getVariables()))
      newtest = this.projection(this.cases, test.getVariables());
		 */

		//Check: the classifier must be trained
		if (this.classifier.isEmpty())
			throw new ClassifierException(0);

		//Check: the number of variables must be the same
		if(this.nVariables != newtest.getVariables().size())
			throw new ClassifierException(1);

		//Check: the variables must have the same number of states
		NodeList    nodelistTrain    = this.cases.getVariables();
		Vector      vectorTrain      = this.cases.getRelationList();
		Relation    relationTrain    = (Relation)vectorTrain.elementAt(0);
		CaseListMem caselistmemTrain = (CaseListMem)relationTrain.getValues();

		NodeList    nodelistTest    = newtest.getVariables();
		Vector      vectorTest      = newtest.getRelationList();
		Relation    relationTest    = (Relation)vectorTest.elementAt(0);
		CaseListMem caselistmemTest = (CaseListMem)relationTest.getValues();

		int nStatesTrain, nStatesTest;
		FiniteStates varStatesTrain = new FiniteStates();
		FiniteStates varStatesTest = new FiniteStates();
		for(int i= 0; i< this.nVariables; i++) {
			varStatesTrain = (FiniteStates)(caselistmemTrain.getVariables()).elementAt(i);
			varStatesTest  = (FiniteStates)(caselistmemTest.getVariables()).elementAt(i);
			nStatesTrain = varStatesTrain.getNumStates();
			nStatesTest  = varStatesTest.getNumStates();
			if(nStatesTest != nStatesTrain)
				throw new ClassifierException(2);
		}

		int nTest = newtest.getNumberOfCases();

		Vector      vector      = newtest.getRelationList();
		Relation    relation    = (Relation)vector.elementAt(0);
		CaseListMem caselistmem = (CaseListMem)relation.getValues();

		double[] caseToTest = new double[this.nVariables];
		double accuracy = (double)0;

		for (int i= 0; i< nTest; i++) {
			for(int j= 0; j< this.nVariables; j++)
				caseToTest[j] = caselistmem.getValue(i,j);
			int assignedClass = this.assignClass(caseToTest);
			if (assignedClass == (int)caselistmem.getValue(i, this.classIndex))
				accuracy = accuracy + 1;
			this.confusionMatrix.actualize((int)caselistmem.getValue(i, this.classIndex), assignedClass);
		}
		accuracy = (accuracy / (double) nTest) * (double) 100;

		return(accuracy);
	}

	/**
	 * This method categorizes a new database. It requires that the field of the
	 * class variables appears in the file. It creates a new file with the
	 * instances categorized
	 * @param. String inputFile. The name of the file to categorize
	 * @param. String outFile. The name of the file where write the categorization
	 */
	public void categorize(String inputFile, String outputFile) throws java.io.IOException, elvira.parser.ParseException{
		int i,j;

		FileInputStream inputF = new FileInputStream(inputFile);
		DataBaseCases test = new DataBaseCases(inputF);
		inputF.close();

		DataBaseCases newtest = test;
		/*    if(!test.getVariables().equals(this.cases.getVariables()))
      newtest = this.projection(this.cases, newtest.getVariables());
		 */
		//Check: the classifier must be trained
		if (this.classifier.isEmpty()) {
			System.err.println("DiscreteClassifier: The classifier is not trained");
			System.exit(0);
		}

		//Check: the number of variables must be the same
		if(this.nVariables != newtest.getVariables().size()) {
			System.err.println("DiscreteClassifier: The number of variables of the dataset to categorize is different to the number of variables of the classifier " + nVariables + " != " + test.getNodeList().size());
			System.exit(0);
		}

		//Check: the variables must have the same number of states
		NodeList    nodelistTrain    = this.cases.getVariables();
		Vector      vectorTrain      = this.cases.getRelationList();
		Relation    relationTrain    = (Relation)vectorTrain.elementAt(0);
		CaseListMem caselistmemTrain = (CaseListMem)relationTrain.getValues();

		NodeList    nodelistTest    = newtest.getVariables();
		Vector      vectorTest      = newtest.getRelationList();
		Relation    relationTest    = (Relation)vectorTest.elementAt(0);
		CaseListMem caselistmemTest = (CaseListMem)relationTest.getValues();

		int nStatesTrain, nStatesTest;
		FiniteStates varStatesTrain = new FiniteStates();
		FiniteStates varStatesTest = new FiniteStates();
		for(i= 0; i< this.nVariables; i++) {
			varStatesTrain = (FiniteStates)(caselistmemTrain.getVariables()).elementAt(i);
			varStatesTest  = (FiniteStates)(caselistmemTest.getVariables()).elementAt(i);
			nStatesTrain = varStatesTrain.getNumStates();
			nStatesTest  = varStatesTest.getNumStates();
			if(nStatesTest != nStatesTrain) {
				System.err.println("DiscreteClassifier: The number of states of the variable " + varStatesTrain.getName() + " is the dataset to categorize is different os the number of states in the classifier");
				System.exit(0);
			}
		}

		int nTest = newtest.getNumberOfCases();

		Vector      vector      = newtest.getRelationList();
		Relation    relation    = (Relation)vector.elementAt(0);
		CaseListMem caselistmem = (CaseListMem)relation.getValues();

		double[] caseToTest = new double[this.nVariables];
		for (i= 0; i< nTest; i++) {
			for(j= 0; j< this.nVariables; j++)
				caseToTest[j] = caselistmem.getValue(i,j);
			int assignedClass = this.assignClass(caseToTest);
			caselistmem.setValue(i, this.classIndex, assignedClass);
			System.out.println("case " + i + " assignedClass " + assignedClass);
		}

		FileWriter outputF = new FileWriter(outputFile);
		newtest.saveDataBase(outputF);
		outputF.close();
	}

	/**
	 * This method assigns to the classifier attribute the Bnet
	 */
	public void setClassifier(Bnet model) {
		this.classifier = model;
	}

	/**
	 * Get the classifier.
	 * @returns Bnet. The classifier.
	 */
	public Bnet getClassifier() {
		return (this.classifier);
	}

	/**
	 * Get the confusion matrix.
	 * @returns ConfusionMatrix. The confusion matrix.
	 */
	public ConfusionMatrix getConfusionMatrix() {
		return (this.confusionMatrix);
	}

	/**
	 * Get the loglikelihood
	 * @returns double. The loglikelihood
	 */
	public double getLogLikelihood() {
		return (this.logLikelihood);
	}

	/**
	 *Get the data base of cases.
	 *@ returns DataBaseCases.
	 */
	public DataBaseCases getDataBaseCases(){
		return cases;   
	}
	/**
	 *Set the data base of cases.
	 *@ returns DataBaseCases.
	 */
	public void setDataBaseCases(DataBaseCases dbc){
		cases=dbc;   
	}

	/** 
	 * Method of Classifier interface. This method is used to build the classifier.
	 * @param training training set to build the classifier
	 * @param classIndex position of the variable to classify
	 */
	public void learn (DataBaseCases data, int indexOfClass) {
		DataBaseCases newData = data;
		/*    if (classIndex != data.getVariables().size() - 1) { //The class variable is not in the last position
      Vector vNodes = new Vector();
      Node classVariable = data.getNodeList().elementAt(classIndex);
      for(int i= 0; i< data.getNodeList().size(); i++)
        if (i != classIndex) vNodes.addElement(data.getNodeList().elementAt(i));
      vNodes.addElement(classVariable);
      NodeList nodeList = new NodeList(vNodes);
      newData = this.projection(data, nodeList);      
    }
		 */
		/***
		 * The following 3 lines were added by dalgaard.
		 ***/
		this.classIndex = indexOfClass;
		this.classVar = (FiniteStates)data.getCases().getVariables().elementAt(this.classIndex);
		this.classNumber = classVar.getNumStates();

		this.cases      = newData;
		this.nVariables = this.cases.getVariables().size();
		this.nCases     = this.cases.getNumberOfCases();
		this.logLikelihood = 0;

		NodeList    nodelist    = this.cases.getVariables();
		Node        node;
		//Vector      vector      = this.cases.getRelationList();
		//Relation    relation    = (Relation)vector.elementAt(0);
		CaseListMem caselistmem = this.cases.getCaseListMem();//(CaseListMem)relation.getValues();

		for (int j=0 ; j< nodelist.size()  ; j++) {
			node = (Node)(caselistmem.getVariables()).elementAt(j);
			if (node.getTypeOfVariable() == Node.CONTINUOUS) {
				System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
				System.exit(0);
			}
		}

		/***
		 * The following 3 lines have been commented out to clean up a bit (by dalgaard).
		 ***/
		//FiniteStates classStates = new FiniteStates();
		//classStates = (FiniteStates)(caselistmem.getVariables()).elementAt(classIndex);
		//this.classNumber = classStates.getNumStates();
		this.confusionMatrix = new ConfusionMatrix(this.classNumber);
		try {
			this.train();
		} catch(java.lang.Exception ex){
			ex.printStackTrace();
		}
	}

	/** 
	 * Method of Classifier interface. This method is used to classify a instance,
	 * it uses the result of Pearl (1987).
	 * @param instance case to classify
	 * @param classnumber number of the variable to classify
	 * @return a Vector with a probability associated to each class
	 */
	public Vector<Double> classify (Configuration instance, int classnumber) {
		/***
		 * The following line was added by dalgaard to make this method use classnumber parameter as
		 * was originally intended 
		 ***/
		this.classVar = instance.getVariable(classnumber);
		this.classNumber = this.classVar.getNumStates();
		/***
		 * untill here. 
		 ***/
		int[] caseTest = new int[this.nVariables];
		Vector<Integer> values  = instance.getValues();
		for(int i= 0; i< this.nVariables; i++) 
			caseTest[i] = values.elementAt(i).intValue();

		Vector<Double> probabilities = new Vector<Double>();

		Node classNode                     = this.classifier.getNodeList().getNode(classVar.getName()); //lastElement();
		Relation classRelation             = this.classifier.getRelation(classNode);
		PotentialTable classPotentialTable = (PotentialTable)classRelation.getValues();

		for(int c= 0; c< this.classNumber; c++) {
			double currentProb     = classPotentialTable.getValue(c);

			NodeList childrenNodes = classNode.getChildrenNodes();
			for(int i= 0; i< childrenNodes.size() ; i++) {
				FiniteStates currentNode      = (FiniteStates)childrenNodes.elementAt(i);
				Relation relation             = this.classifier.getRelation(currentNode);
				PotentialTable potentialTable = (PotentialTable)relation.getValues();

				NodeList parentsNodes = currentNode.getParentNodes();
				Vector<Node> vParentsNodes  = new Vector<Node>();
				Vector<Integer> vParentsValues = new Vector<Integer>();

				vParentsNodes.addElement(currentNode);
				vParentsValues.addElement(new Integer(caseTest[this.cases.getNodeList().getId(currentNode)]));
				for(int p= 0; p< parentsNodes.size();p++) 
					if(! parentsNodes.elementAt(p).equals(classNode)) {
						vParentsNodes.addElement(parentsNodes.elementAt(p));
						vParentsValues.addElement(new Integer(caseTest[this.classifier.getNodeList().getId((Node)parentsNodes.elementAt(p))]));
					}
				vParentsNodes.addElement(classNode);
				vParentsValues.addElement(new Integer(c));

				Configuration parentConfiguration = new Configuration(vParentsNodes, vParentsValues);
				currentProb = currentProb * potentialTable.getValue(parentConfiguration);
			}
			probabilities.addElement(new Double(currentProb));
		}
		return(probabilities);
	}

	/**
	 * Return the class variable of the classifier.
	 */
	public FiniteStates getClassVar(){
		return this.classVar;
	}
	
	
	/**
   	* Return the class variable of the classifier.
   	*/
  	public void setClassVar(FiniteStates c){
		this.classVar = c;
  	}
	
	/**
	 * Return the number of evaluations made to build the classifier
	 */
	public int getEvaluations(){
		return this.evaluations;
	}
	
	public long size(){
		return this.classifier.getNumberOfFreeParameters();
	}
	
	public void saveModelToFile(String ap) throws java.io.IOException {
		java.io.FileWriter fw = new java.io.FileWriter(ap+"discreteClassifier.elv");
		this.classifier.saveBnet(fw);
		fw.close();
	}
}//End class

