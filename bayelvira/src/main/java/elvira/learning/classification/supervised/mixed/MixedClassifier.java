/* MixedClassifier.java */

package elvira.learning.classification.supervised.mixed;

import elvira.*;
import elvira.database.DataBaseCases;
import elvira.learning.classification.supervised.validation.ClassifierDBC;
import elvira.learning.classification.ConfusionMatrix;
import elvira.learning.classification.Classifier;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.supervised.discrete.DiscreteClassifier;
import elvira.potential.*;
import elvira.inference.elimination.VariableElimination;

import java.io.*;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import java.lang.Double;
import elvira.learning.classification.supervised.validation.AvancedConfusionMatrix;

/**
 * MixedClassifier.java
 *
 * The MixedClassifier is an abstract class. It was designed to be the
 * parent of all the mixed classifers to implement.
 * The MixedClassifier has the attributes and the main methods of all the
 * classification model for discrete and continuous variables.
 * It inherits from discrete classifier and it only makes the modifications
 * to include continuous variables.
 *
 * @author Rosa Blanco rosa@si.ehu.es UPV, Seraf�n Moral smc@decsai.ugr.es UGR
 * @version 1.0
 * @since 15/01/2008
 */

public abstract class MixedClassifier extends DiscreteClassifier {

  static final long serialVersionUID = 322871816686876559L;  
  
  /**
   * Basic Constructor. Create a empty DiscreteClassifier
   */
  public MixedClassifier() {
  }

  /**
   * Constructor
   * @param DataBaseCases cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public MixedClassifier(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
        this.cases      = data;
  	this.nVariables = this.cases.getVariables().size();
   	this.nCases     = this.cases.getNumberOfCases();
        this.laplace    = lap;
   	NodeList    nodelist    = this.cases.getVariables();
        Node node;
   	Vector      vector      = this.cases.getRelationList();
   	Relation    relation    = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem = (CaseListMem)relation.getValues();


    node = (Node)(caselistmem.getVariables()).elementAt(this.nVariables-1);
        if (node.getTypeOfVariable() == Node.CONTINUOUS) {
          System.err.println("ERROR-C: You can only classify discrete variables. First, use a Discretization method.");
          System.exit(0);
        }


    this.classVar = (FiniteStates) node;
    this.classNumber = classVar.getNumStates();
    this.confusionMatrix = new ConfusionMatrix(this.classNumber);

  }

  /**
   * Constructor
   * @param DataBaseCases cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   * @param classnumber number of the variable to classify
   */
  public MixedClassifier(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
    this.cases      = data;
  	this.nVariables = this.cases.getVariables().size();
   	this.nCases     = this.cases.getNumberOfCases();
    this.laplace    = lap;
   	NodeList    nodelist    = this.cases.getVariables();
    Node        node;
   	Vector      vector      = this.cases.getRelationList();
   	Relation    relation    = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem = (CaseListMem)relation.getValues();


    node = (Node)(caselistmem.getVariables()).elementAt(classIndex);
        if (node.getTypeOfVariable() == Node.CONTINUOUS) {
          System.err.println("ERROR-A: You can only classify discrete variables. First, use a Discretization method.");
          System.exit(0);
        }

    this.classIndex=classIndex;
    this.classVar = (FiniteStates) node;
    this.classNumber = classVar.getNumStates();
    this.confusionMatrix = new ConfusionMatrix(this.classNumber);

  }

  /**
   * This method tests the learned classifier given a DataBaseCases.
   * It returns the accuracy of the classifier.
   * It requires the the class variable with assigned values in the test database.
   * @param. DataBaseCases test. The test database of the classifier.
   * @returns double. The accuracy of the classifier on the <code> test <code\> dataset.
   */
  public double test(DataBaseCases test, int classIndex) {
     Node varStatesTrain;
    Node varStatesTest;

    DataBaseCases newtest = test;

    int i,j;
    double accuracy = (double)0;

    //Check: the classifier must be trained
    if (this.classifier.isEmpty()) {
      System.err.println("MixedClassifier: The classifier is not trained");
      System.exit(0);
    }

    //Check: the number of variables must be the same
    if(this.cases.getVariables().size() != newtest.getVariables().size()) {
      System.err.println("MixedClassifier: The number of variables of the dataset to test is different to the number of variables of the classifier " + nVariables + " != " + test.getNodeList().size());
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

    for(i= 0; i< this.cases.getVariables().size(); i++) {

        varStatesTrain = (Node)(caselistmemTrain.getVariables()).elementAt(i);
        varStatesTest  = (Node)(caselistmemTest.getVariables()).elementAt(i);

        if (!varStatesTrain.getName().equals( varStatesTest.getName())){
               System.err.println("MixedClassifier: The names of variables " + varStatesTest.getName() +
                                         " is the dataset to test is different  of the name  in the classifier "+varStatesTrain.getName() );
               System.exit(0);
        }

        if (!(varStatesTrain.getTypeOfVariable() == varStatesTest.getTypeOfVariable()))
        {
               System.err.println("MixedClassifier: The type of variable " + varStatesTest.getName() +
                                         " is the dataset to test is different  of the type  in the classifier "+varStatesTrain.getName() );
               System.exit(0);
        }



         if (varStatesTrain.getTypeOfVariable() ==  Node.FINITE_STATES)
         {
              nStatesTrain = ((FiniteStates) varStatesTrain).getNumStates();
              nStatesTest  =  ((FiniteStates) varStatesTest).getNumStates();
              if(nStatesTest != nStatesTrain) {
                System.err.println("DiscreteClassifier: The number of states of the variable " + varStatesTrain.getName() + " is the dataset to test is different os the number of states in the classifier");
                System.exit(0);
              }
          }
    }

    //int nTest = newtest.getNumberOfCases();

    Vector      vector      = newtest.getRelationList();
  	Relation    relation    = (Relation)vector.elementAt(0);
    CaseListMem caselistmem = (CaseListMem)relation.getValues();
    int nTest = caselistmem.getCases().size();
    double[] caseToTest = new double[this.cases.getVariables().size()];
    
    this.confusionMatrix     = new AvancedConfusionMatrix(this.classNumber);
    ContinuousConfiguration conf;
        
    for (i= 0; i< nTest; i++) {

      conf = (ContinuousConfiguration)caselistmem.get(i);
     // System.out.println("classindex="+classIndex);
      Vector result=this.classify (conf, classIndex);      
      //System.out.println(result.size());
      ((AvancedConfusionMatrix)this.confusionMatrix).actualize((int)caselistmem.getValue(i, classIndex), result);

    }
  
    return this.confusionMatrix.getAccuracy();

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
   Node varStatesTrain;
   Node varStatesTest;
    

    FileInputStream inputF = new FileInputStream(inputFile);
    DataBaseCases test = new DataBaseCases(inputF);
    inputF.close();

    DataBaseCases newtest = test;
    //Check: the classifier must be trained
    if (this.classifier.isEmpty()) {
      System.err.println("DiscreteClassifier: The classifier is not trained");
      System.exit(0);
    }

    //Check: the number of variables must be the same
    if(this.cases.getVariables().size() != newtest.getVariables().size()) {
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
   for(i= 0; i< this.cases.getVariables().size(); i++) {
      varStatesTrain = (Node)(caselistmemTrain.getVariables()).elementAt(i);
      varStatesTest  = (Node)(caselistmemTest.getVariables()).elementAt(i);

      if (!varStatesTrain.getName().equals( varStatesTest.getName())){
           System.err.println("MixedClassifier: The names of variables " + varStatesTest.getName() +
	                             " is the dataset to test is different  of the name  in the classifier "+varStatesTrain.getName() );
           System.exit(0);
	      }

if (!(varStatesTrain.getTypeOfVariable() == varStatesTest.getTypeOfVariable()))
{
           System.err.println("MixedClassifier: The type of variable " + varStatesTest.getName() +
	                             " is the dataset to test is different  of the type  in the classifier "+varStatesTrain.getName() );
           System.exit(0);
	      }



if (varStatesTrain.getTypeOfVariable() ==  Node.FINITE_STATES)
   {
      nStatesTrain = ((FiniteStates) varStatesTrain).getNumStates();
      nStatesTest  =  ((FiniteStates) varStatesTest).getNumStates();
      if(nStatesTest != nStatesTrain) {
        System.err.println("DiscreteClassifier: The number of states of the variable " + varStatesTrain.getName() + " is the dataset to test is different os the number of states in the classifier");
        System.exit(0);
      }
      }
    }

    
    


    int nTest = newtest.getNumberOfCases();

    Vector      vector      = newtest.getRelationList();
  	Relation    relation    = (Relation)vector.elementAt(0);
    CaseListMem caselistmem = (CaseListMem)relation.getValues();

  	double[] caseToTest = new double[this.cases.getVariables().size()];
    for (i= 0; i< nTest; i++) {
      for(j= 0; j< this.cases.getVariables().size(); j++)
    		caseToTest[j] = caselistmem.getValue(i,j);
        int assignedClass = this.assignClass(caseToTest);
        caselistmem.setValue(i, this.cases.getVariables().size()-1, assignedClass);
        System.out.println("case " + i + " assignedClass " + assignedClass);
      }

    FileWriter outputF = new FileWriter(outputFile);
    newtest.saveDataBase(outputF);
    outputF.close();
  }


  /**
   * This method is used to build the classifier.
	 * @param training training set to build the classifier
	 * @param classIndex position of the variable to classify
   */
  public void learn (DataBaseCases data, int classIndex){
    
//      ((ClassifierDBC)data).filter1(3);
//      ((ClassifierDBC)data).filter2(10);      
      
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
        this.cases      = newData;
  	this.nVariables = this.cases.getVariables().size();
   	this.nCases     = this.cases.getNumberOfCases();
   	NodeList    nodelist    = this.cases.getVariables();
        Node        node;
   	Vector      vector      = this.cases.getRelationList();
   	Relation    relation    = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem = (CaseListMem)relation.getValues();

        node = (Node)(caselistmem.getVariables()).elementAt(classIndex);
        if (node.getTypeOfVariable() == Node.CONTINUOUS) {
          System.err.println("ERROR-B: You can only classify discrete variables. First, use a Discretization method.");
          System.exit(0);
        }


    FiniteStates classStates = new FiniteStates();
    classStates = (FiniteStates)(caselistmem.getVariables()).elementAt(classIndex);
    this.classNumber = classStates.getNumStates();
    this.confusionMatrix = new ConfusionMatrix(this.classNumber);
    try{
    this.train();
    }catch(java.lang.Exception ex){
        ex.printStackTrace();
        System.out.println("Exception in Method Learn. MixedClassifier Class:\n"+ex);
        System.exit(0);
    }
  }

 
  /**
   * This method assign a class to the instances of the input instance
   * When naive-Bayes the assigned class is the class which maximizes:
   * p(c) prod( p(x_i|c) );
   * @param. double[] caseTest. The input instance to assign a class
   */

  public int assignClass(Configuration conf) {
    int maxClass    = -1;
    double maxProb  = -1;

/*    
    Node classNode                     = classVar;//this.classifier.getNodeList().elementAt(this.classnumber);
    Relation classRelation             = this.classifier.getRelation(classNode);
    PotentialTable classPotentialTable = (PotentialTable)classRelation.getValues();

    for(int c= 0; c< this.classNumber; c++) {
      double currentProb = classPotentialTable.getValue(c);
      for(int i= 0; i< this.nVariables-1; i++) {
        Node currentNode              = this.classifier.getNodeList().elementAt(i);
        Relation relation             = this.classifier.getRelation(currentNode);
        PotentialTable potentialTable = (PotentialTable)relation.getValues();
        int nState = (int)caseTest[i];
        int position = this.classNumber * nState + c;
        currentProb = currentProb * potentialTable.getValue(position);
      }
      if (currentProb > maxProb) {
        maxProb  = currentProb;
        maxClass = c;
      }
    }
    return(maxClass);

  */
      
    NodeList nl=classifier.getNodeList();
    
    Vector posterior=classify((ContinuousConfiguration)conf,nl.getId(classVar));
    
    double val;
    for(int c= 0; c< this.classNumber; c++) {
      val=((Double)posterior.elementAt(c)).doubleValue();
      if (val> maxProb) {
        maxProb  = val;
        maxClass = c;
      }
     }

    if (maxProb==0) 
        System.out.println("Prob 0.0");
    return maxClass;
  }

  
  
/**
* This method is used to classify a instance
* @param instance case to classify
* @param classnumber number of the variable to classify
* @return a Vector with a probability associated to each class
*/
public Vector classify (Configuration instance, int classnumber) {
 
    Evidence ev;
    VariableElimination prop;
    Vector res;
    Vector probabilities=new Vector();
    Potential posteriori;
    Configuration c;
    int i,j;
    
    ContinuousConfiguration conf = (ContinuousConfiguration)instance.copy();
    conf.remove(classVar);
    NodeList nl=classifier.getNodeList().copy();
    
    for (j=0; j<nl.size(); j++){
        if (nl.elementAt(j).getClass()==Continuous.class){
          if (conf.getValue((Continuous)nl.elementAt(j))==((Continuous)nl.elementAt(j)).undefValue()){
            conf.remove(nl.elementAt(j));
          }
        }else if (nl.elementAt(j).getClass()==FiniteStates.class){
          if (conf.getValue((FiniteStates)nl.elementAt(j))==((FiniteStates)nl.elementAt(j)).undefValue()){
            conf.remove(nl.elementAt(j));
          }
        }
    }

    
    ev = new Evidence(conf);
    //ev.print();
    prop = new VariableElimination(this.classifier, ev);
    prop.getPosteriorDistributionOf((FiniteStates) this.classVar);
    res = prop.getResults();
    posteriori = (Potential) res.elementAt(0);

    for(i=0;i<this.classNumber;i++)
    {
    c = new ContinuousConfiguration();
    c.insert((FiniteStates) this.classVar,i);
    probabilities.add(new Double(posteriori.getValue(c)));
    }



    return probabilities;
}

public ClassifierDBC getClassifierDBC(){
    if (this.cases.getClass()==ClassifierDBC.class)
       return (ClassifierDBC)this.cases;
    else
       return new ClassifierDBC(this.cases.getName(),this.cases.getCases(),this.cases.getVariables().getId(this.classVar));
}

  /**
   * This method return the train error of the classifier with the evaluation on data base
   * 'cases' with k fold cross validation.
   * @param DataBaseCases cases, data base of cases who is evaluated.
   * @param int numClass, the number of class variable in data base 'cases'. 
   * @return double, the train error of the classifier in data base 'cases'.
   */
  public ConfusionMatrix evaluationKFC(DataBaseCases cases, int numClass){
        
        try{
        
        int KEvaluation=10;

        //cases= new ClassifierDBC(new String(cases.getName()),cases.getCases(),numClass);
       
        
        ClassifierDBC dbcTmp= (ClassifierDBC)this.cases.copy();
        dbcTmp.projection(cases.getVariables());
        cases=dbcTmp;
        numClass=cases.getVariables().getId(this.classVar);
        Vector tmp=((ClassifierDBC)cases).getDbcKFC(KEvaluation);
        Vector subDBCs=new Vector();
        for (int i=0; i<tmp.size();i++)
            subDBCs.add(((ClassifierDBC)tmp.elementAt(i)));

        ClassifierValidator cv = new ClassifierValidator(this.getNewClassifier(cases),subDBCs,numClass,ClassifierValidator.KFOLD);

        //ClassifierValidator cv = new ClassifierValidator(this.getNewClassifier(cases), cases, numClass);
        return cv.kFoldCrossValidation(KEvaluation);

        }catch(Exception e){
            System.out.println("Exception in evaluationLOO.");
            e.printStackTrace();
            System.exit(0);
            return null;
        }

  }
  
  /**
   * This method return the train error of the classifier with the evaluation on data base
   * 'cases' with leave one out validation.
   * @param DataBaseCases cases, data base of cases who is evaluated.
   * @param int numClass, the number of class variable in data base 'cases'. 
   * @return double, the train error of the classifier in data base 'cases'.
   */
  public ConfusionMatrix evaluationLOO(DataBaseCases cases, int numClass) {

    try{
        ClassifierValidator validator = new ClassifierValidator(this.getNewClassifier(cases), cases, numClass);
        return validator.leaveOneOut();
    }catch(Exception e){
        System.out.println("Exception in evaluationLOO.");
        e.printStackTrace();
        System.exit(0);
        return null;
    }
  }
  
  /**
   * This method has to return a new trained classifier with the base of cases.
   * This method is not implemented yet, but it's neccesary to complete the 
   * implementation of the selectiveStructuralLearning and 
   * setGreatestProbabilityConfiguration methods.
   * @param DataBaseCases cases, the data base with the trainning data.
   * @returns Bnet, a new trained classifier.
   */
  
  public MixedClassifier getNewClassifier(DataBaseCases cases){
      System.out.println("Method getNewClassifiernot implemented in class: "+this.getClass().getName());
      System.exit(0);
      return null;
  }


  public DataBaseCases testDBCPreprocessing(DataBaseCases data){
     return data;
  }
}//End class

