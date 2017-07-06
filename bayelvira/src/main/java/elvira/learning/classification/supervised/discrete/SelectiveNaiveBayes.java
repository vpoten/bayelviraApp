/* SelectiveNaiveBayes.java */
package elvira.learning.classification.supervised.discrete;

import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;

import java.util.Vector;

/**
 * SelectiveNaiveBayes.java
 *
 * This is an abstract class. The subclasses must learn a selective naive Bayes
 * classification model. A selective naive Bayes is a typical naive Bayes where
 * not all the predictives variables are considered.
 * 
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 1.0
 * @since 15/04/2003
 */

public abstract class SelectiveNaiveBayes extends DiscreteClassifier  {

  /**
   * The best classifier obtained
   */
  Naive_Bayes accurateClassifier;

  /**
   * Basic Constructor
   */
  public SelectiveNaiveBayes() {
    super();
  }
  
  /**
   * Constructor. It calls to the super constructor with the same parameter
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public SelectiveNaiveBayes(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
    super(data, lap);
  }

/*  public SelectiveNaiveBayes(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
    super(data, lap, classIndex);
  }
*/

  /**
   * This method learns the classifier structure
   */
  public abstract void structuralLearning() throws elvira.InvalidEditException, Exception;

  /**
   * This method assigns a class value to the instances of the input instance
   * When SelectiveNaiveBayes the assigned class is the class which maximizes:
   * p(c) prod( p(x_i|c) ) as in naive-Bayes.
   * However, the NodeList don't contain all the predictive variables
   * @param. double[] caseTest. The input instance to assign a class
   */
/*  public int assignClass(double[] caseTest) {
    if(caseTest.length == this.accurateClassifier.getClassifier().getNodeList().size()) 
      return(this.accurateClassifier.assignClass(caseTest));
    else {
      Vector      vector       = this.cases.getRelationList();
      Relation    relation     = (Relation)vector.elementAt(0);
      CaseListMem caselistmem  = (CaseListMem)relation.getValues();
      NodeList    realNodeList = this.cases.getNodeList();

      int      caseSize = this.accurateClassifier.getClassifier().getNodeList().size();
      double[] instance = new double[caseSize];
      for(int i= 0; i< caseSize; i++) {
        int pos = realNodeList.getId(this.accurateClassifier.getClassifier().getNodeList().elementAt(i));
        instance[i] = caseTest[pos];
      }
      return(this.accurateClassifier.assignClass(instance));
    }
  }
*/

  /**
   * This method makes the factorization of the classifier
   * In this case, the factorization is like a NaiveBayes.
   * However, the NodeList don't contain all the predictive variables
   */
  public void parametricLearning() {
    this.accurateClassifier.parametricLearning();
    this.logLikelihood = this.accurateClassifier.getLogLikelihood();
  }

  /** 
   * Method of Classifier interface. This method is used to classify a instance
	 * @param instance case to classify
	 * @param classnumber number of the variable to classify
	 * @return a Vector with a probability associated to each class
   */
  public Vector classify (Configuration instance, int classnumber) {
    if(instance.size() == this.accurateClassifier.getClassifier().getNodeList().size()) 
      return(this.accurateClassifier.classify(instance, classnumber));      
    else {
      Vector      vector       = this.cases.getRelationList();
      Relation    relation     = (Relation)vector.elementAt(0);
      CaseListMem caselistmem  = (CaseListMem)relation.getValues();
      NodeList    realNodeList = this.cases.getNodeList();

      int caseSize          = this.accurateClassifier.getClassifier().getNodeList().size();
      Vector vReducedNodes  = new Vector();
      Vector vReducedValues = new Vector();
      for(int i= 0; i< caseSize; i++) {
        vReducedNodes.addElement(this.accurateClassifier.getClassifier().getNodeList().elementAt(i));
        int posNode = this.cases.getNodeList().getId(this.accurateClassifier.getClassifier().getNodeList().elementAt(i));
        vReducedValues.addElement(new Integer(instance.getValue(posNode)));
      }
      Configuration reducedInstance = new Configuration(vReducedNodes, vReducedValues);
      return(this.accurateClassifier.classify(reducedInstance, classnumber));
    }
  }
}
