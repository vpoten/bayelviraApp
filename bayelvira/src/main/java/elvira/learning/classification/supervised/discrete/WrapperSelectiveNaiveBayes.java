/* WrapperSelectiveNaiveBayes */
package elvira.learning.classification.supervised.discrete;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.supervised.discrete.Naive_Bayes;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Vector;

/**
 * WrapperSelectiveNaiveBayes.java
 *
 * It implements the algorithm proposed by in Langley and Sage (1994) 
 * Induction of selective Bayesian classifiers
 *
 * This class learns a special NaiveBayes where some features are selected.
 * 
 * More o less the algorithm is as follows:
 * - Start with an empty classifier (only the class variable)
 * - At each step add an outer variable as a new variable of the classifier
 *
 * This is the forward version, a backward version requires a huge
 * computational cost
 * 
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 1.0
 * @since 15/04/2003
 */

public class WrapperSelectiveNaiveBayes extends SelectiveNaiveBayes  {
  /**
   * The k_fold of the evaluation
   */
  private int k_fold;

  /**
   * Basic Constructor
   */
  public WrapperSelectiveNaiveBayes() {
    super();
    this.k_fold = 5;
  }

  /**
   * Constructor.
   * @param int k. The k of the evaluation
   */
  public WrapperSelectiveNaiveBayes(int k) {
    super();
    this.k_fold = k;
  }

  /**
   * Constructor. It calls to the super constructor with the same parameter
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public WrapperSelectiveNaiveBayes(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
    super(data, lap);
    this.k_fold = 5;
  }

  /**
   * Constructor. It calls to the super constructor with the same parameter
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   * @param int k. The k of the evaluation
   */
  public WrapperSelectiveNaiveBayes(DataBaseCases data, boolean lap, int k) throws elvira.InvalidEditException{
    super(data, lap);
    this.k_fold = k;
  }

  /**
   * generateDbcInclude returns a DataBaseCases built from a input NodeList
   * and one node. This Node isn't in the input NodeList.
   * The output DataBaseCases has one node more than the NodeList
   * @param NodeList nodeList. The current NodeList
   * @param Node node. The node to be included in data.
   */
  private DataBaseCases generateDbcInclude(NodeList nodeList, Node node){
    DataBaseCases output = new DataBaseCases();
    output.setName(this.cases.getName());
    output.setTitle(this.cases.getName());

    //add node to the nodeList and set nodeList to the ouput DataBase
    Node classVariable = nodeList.lastElement().copy();
    Vector vNodeList = new Vector();
    for(int i= 0; i< nodeList.size() -1 ; i++) //The last element is the classVariable
      vNodeList.addElement(nodeList.elementAt(i).copy());
    vNodeList.addElement(node.copy());
    vNodeList.addElement(classVariable);
    NodeList newNodeList = new NodeList(vNodeList);
    output.setNodeList(newNodeList);

   	Vector      vector       = this.cases.getRelationList();
  	Relation    relation     = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem  = (CaseListMem)relation.getValues();
    NodeList    realNodeList = this.cases.getNodeList();

    CaseListMem newCaseList = new CaseListMem(newNodeList);
    Configuration newConf   = new Configuration(newNodeList);

    Vector vCases = ((CaseListMem)((Relation)this.cases.getRelationList().get(0)).getValues()).getCases();
    int[] instance = new int[this.nVariables];

    //add the changed instances
    for(int l= 0; l< this.nCases; l++) {
      Configuration auxConf = new Configuration(newNodeList);
//      double[] original     = (double[])vCases.elementAt(l);
//      for(int j= 0; j< this.nVariables; j++)
//        instance[j] = (int)original[j];
      instance = (int[])vCases.elementAt(l);

      for(int j=0; j< newNodeList.size(); j++) {
        FiniteStates auxNode = (FiniteStates)newNodeList.getNodes().elementAt(j);
        auxConf.putValue(auxNode, instance[realNodeList.getId(auxNode)]);
      }

      newConf.setValues(auxConf.getValues());
      newCaseList.put(newConf);
    }

    Vector vRelation   = new Vector();
    Relation rRelation = new Relation();

    rRelation.setVariables(newNodeList);
    rRelation.setValues(newCaseList);
    vRelation.addElement(rRelation);
    output.setRelationList(vRelation);
    output.setNumberOfCases(this.cases.getNumberOfCases());

    return(output);
  }

  /**
   * This method learns the classifier structure. Bearing in mind that
   * the underlying structure is a naive-Bayes, it must be noted that the structural
   * learning looks for the most accurate subset of variables.
   * It calculates the accuracy by means a cross-validation. The k is required
   */
  public void structuralLearning() throws elvira.InvalidEditException, Exception {
    Vector vNodes = new Vector();
    vNodes.addElement(this.cases.getVariables().elementAt(nVariables-1));
    NodeList nodeList = new NodeList(vNodes); //add the class to the NodeList;

    //at the begining all the nodes are out of the classifier except the class variable
    Vector includedNodes = new Vector();
    Vector excludedNodes = new Vector();
    for(int i= 0; i< this.nVariables-1; i++)
      excludedNodes.addElement(this.cases.getVariables().elementAt(i));

    double bestAccuracy = Double.MIN_VALUE;
    DataBaseCases bestData = new DataBaseCases();
    boolean stop = false;

    while(!stop) {
      double bestInclusionAccuracy    = Double.MIN_VALUE;
      DataBaseCases bestInclusionData = new DataBaseCases();
      Node bestInclusionNode          = new FiniteStates();
      //First, look for the best inclusion of a node in the model
      for(int i= 0; i< excludedNodes.size(); i++) {
        Node includedNode           = (Node)excludedNodes.elementAt(i);
        DataBaseCases inclusionData = this.generateDbcInclude(nodeList, includedNode);

        Naive_Bayes nb                = new Naive_Bayes();
        //ClassifierValidator validator = new ClassifierValidator(nb, inclusionData, inclusionData.getVariables().size()-1);
        ClassifierValidator validator = new ClassifierValidator(nb, inclusionData, inclusionData.getClassId());
        double inclusionAccuracy      = (1 - validator.kFoldCrossValidation(this.k_fold).getError());
        
        this.evaluations ++;
        if (inclusionAccuracy > bestInclusionAccuracy) {
          bestInclusionAccuracy = inclusionAccuracy;
          bestInclusionData = inclusionData;
          bestInclusionNode = includedNode;
        }
      }
      
      if(bestInclusionAccuracy > bestAccuracy) {
        bestAccuracy = bestInclusionAccuracy;
        bestData     = bestInclusionData;
        nodeList     = bestData.getNodeList();
        includedNodes.addElement(bestInclusionNode);
        excludedNodes.removeElement(bestInclusionNode);
        System.out.println(bestAccuracy + " " + bestInclusionNode.toString());
      }
      else stop = true;
    }

    this.accurateClassifier = new Naive_Bayes(bestData, this.laplace);
    this.accurateClassifier.train();

    classifier = new Bnet();
    classifier = this.accurateClassifier.getClassifier();
    System.out.println();
    System.out.println(this.accurateClassifier.getClassifier().getNodeList().toString());
    System.out.println("    " + (this.accurateClassifier.getClassifier().getNodeList().size() -1) + " selected variables");
    System.out.println("    " + this.evaluations + " evaluated solutions");
  }

  public static void main(String[] args) throws java.io.FileNotFoundException, java.io.IOException, elvira.InvalidEditException, elvira.parser.ParseException, Exception{
    //Comprobar argumentos
    if(args.length != 3) {
      System.out.println("Usage: file-train.dbc file-test.dbc file-out.elv");
      System.exit(0);
    }

    FileInputStream fi = new FileInputStream(args[0]);
    DataBaseCases   db = new DataBaseCases(fi);
    fi.close();

    WrapperSelectiveNaiveBayes clasificador = new WrapperSelectiveNaiveBayes(db, true);
    clasificador.train();

    System.out.println("Classifier learned");

    FileInputStream ft = new FileInputStream(args[1]);
    DataBaseCases   dt = new DataBaseCases(ft);
    ft.close();

    double accuracy = clasificador.test(dt);
    System.out.println("Classifier tested. Accuracy: " + accuracy);

    clasificador.getConfusionMatrix().print();

    FileWriter fo = new FileWriter(args[2]);
    clasificador.getClassifier().saveBnet(fo);
    fo.close();
  }

}