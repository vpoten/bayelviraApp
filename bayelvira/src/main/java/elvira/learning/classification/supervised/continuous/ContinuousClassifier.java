package elvira.learning.classification.supervised.continuous;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.Continuous;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.learning.classification.ConfusionMatrix;
import elvira.learning.classification.Classifier;
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
 * ContinuousClassifier.java
 *
 * The ContinuousClassifier is an abstract class. It was designed to be the
 * parent of all the continuous classifers to implement.
 * The DiscreteClassifier has the attributes and the main methods of all the
 * classification model for discrete variables.
 *
 * @author Ildik� Flesch
 * @version 1.0
 * @since 1/06/2007
 */

public abstract class ContinuousClassifier implements Classifier, Serializable{

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
  protected Continuous classVar;

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
  public ContinuousClassifier() {
    this.laplace       = true;
    this.evaluations   = 0;
    this.logLikelihood = 0;
  }

  /**
   * Constructor
   * @param DataBaseCases cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public ContinuousClassifier(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
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
        if (node.getTypeOfVariable() == Node.FINITE_STATES) {
          System.err.println("ERROR: There is discrete value. ");
          System.exit(0);
        }
      }

    Continuous classStates = (Continuous)this.cases.getNodeList().lastElement();
    //this.classNumber         = classStates..getNumStates(); // TODO
    this.confusionMatrix     = new ConfusionMatrix(this.classNumber);
  }


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
  /**
   * This method assigns a class to the instances of the input instance. It
   * returns the identifer of the assigned class. The class variable is the
   * last variable
   * It is called by this.test(). 
   * @param. Configuration conf. A configuration containing the instance to classify
   * @returns int. The identifier of the most probable class.
   */
/*  public int assignClass(Configuration conf) {
    double[] instance = new double[this.nVariables];
    if(conf.getValues().size() != this.nVariables) {
      System.out.println("assignClass: the size of the configuration is not valid " + conf.getValues().size() + " > " + this.nVariables);
      System.exit(0);
    }
    for(int i= 0; i<this.nVariables; i++)
      instance[i] = (double)conf.getValue(i);
    return(this.assignClass(instance));
  }*/

  /**
   * This method implements the looklikelihood score of the classifier with
   * BIC penalization
   * @param auxiliarPotentialTables a vector containig the conditional probability
   * tables (into AuxiliarPotentialTable objects). Each element of the vector is
   * the probability conditional table for a specific variable, beeing the last
   * one for the class variable.
   * @return the loglikelihood value with BIC penalization.
   **/
  /*private void lookLikelihood(Vector auxPotentialTables) {
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
*/
  /**
   * This method makes the factorization of the classifier.
   */
  public void parametricLearning() {
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
	  double i = 0; // TODO change code 
	  return i;
  }

  /**
   * This method categorizes a new database. It requires that the field of the
   * class variables appears in the file. It creates a new file with the
   * instances categorized
   * @param. String inputFile. The name of the file to categorize
   * @param. String outFile. The name of the file where write the categorization
   */
  public void categorize(String inputFile, String outputFile) throws java.io.IOException, elvira.parser.ParseException{
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
  public void learn (DataBaseCases data, int classIndex) {
  }

  /** 
   * Method of Classifier interface. This method is used to classify a instance,
   * it uses the result of Pearl (1987).
	 * @param instance case to classify
	 * @param classnumber number of the variable to classify
	 * @return a Vector with a probability associated to each class
   */
  public Vector classify (Configuration instance, int classnumber) {
    Vector probabilities = new Vector(); // TODO change code
    return(probabilities);
  }
  
  /**
   * Return the class variable of the classifier.
   */
  public Continuous getClassVar(){
      return this.classVar;
  }
  /**
   * Return the number of evaluations made to build the classifier
   */
  public int getEvaluations(){
      return this.evaluations;
  }
  
  public void saveModelToFile(String ap) throws java.io.IOException {
	  java.io.FileWriter fw = new java.io.FileWriter(ap+"continousClassificatier.elv");
	  this.classifier.saveBnet(fw);
	  fw.close();
  }
}//End class

