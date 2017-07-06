/* WrapperSemiNaiveBayes */
package elvira.learning.classification.supervised.discrete;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * WrapperSemiNaiveBayes.java
 *
 * This class learns a special NaiveBayes by means of FSSJ algorithm proposed in 
 * M. Pazzani (1997) Searching for Dependencies in Bayesian Classifier.
 * 
 * More o less the algorithm is as follows:
 * - Start with an empty classifier (only the class variable)
 * - At each step choose the 'best' decision (in accuracy) :
 *    + add an outer variable as a new variable of the classifier
 *    + join an outer variable with an existing variable of the classifier
 * 
 * This is the forward version, a backward version requires a huge
 * computational cost
 * 
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 1.0
 * @since 08/04/2003
 */

public class WrapperSemiNaiveBayes extends SemiNaiveBayes {
  /**
   * The k_fold of the evaluation
   */
  private int k_fold;

  /**
   * Basic Constructor
   */
  public WrapperSemiNaiveBayes() {
    super();
    this.k_fold = 5;
  }

  /**
   * Constructor.
   * @param int k. The k of the evaluation
   */
  public WrapperSemiNaiveBayes(int k) {
    super();
    this.k_fold = k;
  }

  /**
   * Constructor. It calls to the super constructor with the same parameter
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public WrapperSemiNaiveBayes(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
    super(data, lap);
    this.k_fold = 5;
  }

  /**
   * Constructor. It calls to the super constructor with the same parameter
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   * @param int k. The k of the evaluation
   */
  public WrapperSemiNaiveBayes(DataBaseCases data, boolean lap, int k) throws elvira.InvalidEditException{
    super(data, lap);
    this.k_fold = k;
  }

  /**
   * This method learns the classifier structure. Bearing in mind that
   * the underlying structure is a naive-Bayes, it must be noted that the structural
   * learning looks fot the more accurate subset of variables or joined variables.
   * It calculates the accuracy by means a cross-validation. The k is required
   * @param int k_fold. The k of the of the k-fold-cross-validation
   */
  public void structuralLearning() throws elvira.InvalidEditException, Exception {
    //at the begining all the nodes are out of the classifier except the class variable
    Vector includedNodes = new Vector();
    Vector excludedNodes = new Vector();
    for(int i= 0; i< this.nVariables-1; i++)
      excludedNodes.addElement(this.cases.getNodeList().elementAt(i).copy());

    double bestAccuracy       = Double.MIN_VALUE;
    boolean stop              = false;

    int[] ind  = new int[this.nVariables-1];
    int states = this.nVariables + 1;
    for(int i= 0; i< ind.length; i++)
      ind[i] = 0;

    while (!stop) {
      double bestInclusionAccuracy   = Double.MIN_VALUE;
      double bestJoinAccuracy        = Double.MIN_VALUE;
      Individual bestInclusionInd    = new Individual();
      Individual bestJoinInd         = new Individual();
      FiniteStates bestInclusionNode = new FiniteStates();
      FiniteStates bestJoinNode      = new FiniteStates();

      //First, look fot the best inclusion of a node in the model
      for(int e= 0; e< excludedNodes.size(); e++) {
        ind[this.cases.getNodeList().getId((Node)excludedNodes.elementAt(e))] = this.cases.getNodeList().getId((Node)excludedNodes.elementAt(e)) + 1;
        Individual individual = new Individual(this.nVariables-1, ind, states, "SemiNaiveBayes");
        individual.evaluate(this.cases, this.k_fold);
        double accuracy = individual.getScore();
        ind[this.cases.getNodeList().getId((Node)excludedNodes.elementAt(e))] = 0;
        this.evaluations ++;
        if(accuracy > bestInclusionAccuracy) {
          bestInclusionAccuracy = accuracy;
          bestInclusionInd      = individual;
          bestInclusionNode     = (FiniteStates)excludedNodes.elementAt(e);
        }
      }
      System.out.println("Inclusion:    " + bestInclusionAccuracy + " " + bestInclusionNode);

      CartesianProduct cartes = new CartesianProduct();
      //Look for the best joining
      for(int i= 0; i< includedNodes.size(); i++) {
        int includedValue = ind[this.cases.getNodeList().getId((Node)includedNodes.elementAt(i))];
        for(int e= 0; e< excludedNodes.size(); e++) {
          int excludedValue = ind[this.cases.getNodeList().getId((Node)excludedNodes.elementAt(e))];
          ind[this.cases.getNodeList().getId((Node)excludedNodes.elementAt(e))] = includedValue;
          Individual individual = new Individual(this.nVariables-1, ind, states, "SemiNaiveBayes");
          individual.evaluate(this.cases, this.k_fold);
          double accuracy = individual.getScore();
          this.evaluations ++;
          if(accuracy > bestJoinAccuracy) {
            bestJoinAccuracy = accuracy;
            bestJoinInd      = individual;
            Vector vNodes    = new Vector();
            for(int v= 0; v< ind.length; v++)
              if (ind[v] == includedValue)
                vNodes.addElement(this.cases.getNodeList().elementAt(v).copy());
            cartes       = new CartesianProduct(vNodes, this.cases.getNodeList().copy());
            bestJoinNode = cartes.getCartesianNode();
          }
          ind[this.cases.getNodeList().getId((Node)excludedNodes.elementAt(e))] = excludedValue;
        }
      }
      System.out.println("Join:         " + bestJoinAccuracy + " " + bestJoinNode);

      //Now, decide between include a new node or join a existeing node with a new node
      if ((bestInclusionAccuracy > bestJoinAccuracy) && (bestInclusionAccuracy > bestAccuracy)) {
        bestAccuracy        = bestInclusionAccuracy;
        this.bestIndividual = bestInclusionInd;
        includedNodes.addElement(bestInclusionNode);
        excludedNodes.removeElement(bestInclusionNode);
        for(int i= 0; i< ind.length; i++)
          ind[i] = bestIndividual.getValue(i);
      }
      else 
        if (bestJoinAccuracy > bestAccuracy) {
          bestAccuracy        = bestJoinAccuracy;
          this.bestIndividual = bestJoinInd;
          for(int v= 0; v< cartes.getNodes().size(); v++) 
            excludedNodes.removeElement(this.cases.getNodeList().elementAt(this.cases.getNodeList().getId((Node)cartes.getNodes().elementAt(v))));
          for(int i= 0; i< ind.length; i++)
            ind[i] = this.bestIndividual.getValue(i);
        }
        else stop = true; //not improvement reach

    }//end while

    DataBaseCases bestData  = this.bestIndividual.generateCartesianDBC(this.cases);

    System.out.println();
    System.out.println("NodeList " + bestData.getNodeList().toString2());
    System.out.println("Best Accuracy " + bestAccuracy);

    this.accurateClassifier = new Naive_Bayes(bestData, this.laplace);
    this.accurateClassifier.train();
    classifier = new Bnet();
    classifier = accurateClassifier.getClassifier();
    System.out.println();
    System.out.println(this.accurateClassifier.getClassifier().getNodeList().toString());
    System.out.println("    " + (this.accurateClassifier.getClassifier().getNodeList().size() -1) + " selected variables");
    System.out.println("    " + this.evaluations + " evaluated solutions");
  }

  public static void main(String[] args) throws FileNotFoundException, IOException, elvira.InvalidEditException, elvira.parser.ParseException, Exception{
    //Comprobar argumentos
    if(args.length != 3) {
      System.out.println("Usage: file-train.dbc file-test.dbc file-out.elv");
      System.exit(0);
    }

    FileInputStream fi = new FileInputStream(args[0]);
    DataBaseCases   db = new DataBaseCases(fi);
    fi.close();

    WrapperSemiNaiveBayes clasificador = new WrapperSemiNaiveBayes(db, true);
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
