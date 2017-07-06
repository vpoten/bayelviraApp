/* SemiNaiveBayes.java */
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
import elvira.potential.PotentialTable;

import java.io.*;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

/**
 * SemiNaiveBayes.java
 *
 * This is an abstract class. The subclasses must learn a semi naive Bayes
 * classification model. A semi naive Bayes is a typical naive Bayes where:
 *  + new predictive variables are considered:
 *       - the cartesian product of two or more predictive variable
 *       - the members of a cartesian product don't appear in the model
 *  + not all the original variables appears in the model
 * 
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 1.0
 * @since 08/04/2003
 */


public abstract class SemiNaiveBayes extends DiscreteClassifier  {

  /**
   * CartesianProduct
   * 
   * Private class to perform the cartesian product of a set of nodes
   */
protected class CartesianProduct {
  /**
   * The number of nodes of the cartesian product
   */
  private int numNodes;

  /**
   * The nodes of the cartesian product
   */
  private Vector vNodes;

  /**
   * The states of the cartesian product
   */
  private Vector vStates;

  /**
   * The real nodelist
   */
  private NodeList realNodeList;

  /**
   * The cartesian product node
   */
  private FiniteStates cartesianNode;

  /**
   * Basic constructor
   */
  public CartesianProduct() {
  }

  /**
   * Constructor
   * @param NodeList nodes. The nodes of the cartesian product
   * @param NodeList real. The real nodelist
   */
  public CartesianProduct(NodeList nodes, NodeList real) {
    this.realNodeList = real.copy();
    this.numNodes     = nodes.size();
    this.vStates      = new Vector();
    this.vNodes       = new Vector();
    for(int i= 0; i< this.numNodes ; i++)
      this.vNodes.addElement(nodes.elementAt(i).copy());
    this.generateStates();
    this.generateCartesianNode();
  }

  /**
   * Constructor
   * @param Vector nodes. The nodes of the cartesian product
   * @param NodeList real. The real nodelist
   */
  public CartesianProduct(Vector nodes, NodeList real) {
    this.realNodeList = real.copy();
    this.numNodes     = nodes.size();
    this.vStates      = new Vector();
    this.vNodes       = new Vector();
    for(int i= 0; i< this.numNodes ; i++)
      this.vNodes.addElement(((FiniteStates)nodes.elementAt(i)).copy());
    this.generateStates();
    this.generateCartesianNode();
  }

  /**
   * To build the cartesian product node
   */
  private void generateCartesianNode() {
    String name  = ((FiniteStates)this.vNodes.firstElement()).getName();
    String title = ((FiniteStates)this.vNodes.firstElement()).getTitle();
    for(int i= 1; i< this.numNodes-1; i++) {
      name  = name  + "_x_" + ((FiniteStates)this.vNodes.elementAt(i)).getName();
      title = title + "_x_" + ((FiniteStates)this.vNodes.elementAt(i)).getTitle();
    }
    name  = name  + "_x_" + ((FiniteStates)this.vNodes.lastElement()).getName();
    title = title + "_x_" + ((FiniteStates)this.vNodes.lastElement()).getTitle();

    this.cartesianNode = new FiniteStates();
    this.cartesianNode.setStates(this.vStates);
    this.cartesianNode.setName(name);
    this.cartesianNode.setTitle(title);
    this.cartesianNode.setComment("cartesian product");
  }

  /**
   * To build the states of the cartesian product node
   */
  private void generateStates() {
    Vector[] previousStates = new Vector[this.numNodes];
    int numStates           = 1;
    for(int i= 0; i< this.numNodes; i++) {
      previousStates[i] = ((FiniteStates)this.vNodes.elementAt(i)).getStates();
      numStates *= ((FiniteStates)this.vNodes.elementAt(i)).getNumStates();
    }

    if (this.numNodes == 2) {
      Iterator sFirstIterator = previousStates[0].iterator();
      while(sFirstIterator.hasNext()) {
        String name = (String)sFirstIterator.next() + "_x_";
        for(int i= 1; i< this.numNodes; i++){
          Iterator statesIterator = previousStates[i].iterator();
          while(statesIterator.hasNext()) {
            if (i == this.numNodes-1) {
              String nameFin = name + (String)statesIterator.next();
              this.vStates.addElement(nameFin);
            }
            else
              name = name + (String)statesIterator.next() + "_x_";
          }
        }
      }
    }
    else {
      Vector currentStates = this.generateStatesOfTwoNodes((FiniteStates)this.vNodes.elementAt(0), (FiniteStates)this.vNodes.elementAt(1));
      for(int i= 2; i< this.numNodes; i++)
        currentStates = this.generateStatesOfTwoNodes(currentStates, (FiniteStates)this.vNodes.elementAt(i));
      this.vStates = currentStates;
    }
    
  }

  /**
   * Given two nodes, it build the states corresponding to the cartesian product
   * of the two nodes
   * @param FiniteStates node1. The first node
   * @param FiniteStates node2. The second node
   * @returns Vector. Container of the states
   */
  private Vector generateStatesOfTwoNodes(FiniteStates node1, FiniteStates node2) {
    Vector newStates = new Vector();

    for(int i= 0; i< node1.getNumStates(); i++) {
      String nameStates = node1.getState(i);
      for(int j= 0; j< node2.getNumStates(); j++) {
        String name = nameStates + "_x_" + node2.getState(j);
        newStates.addElement(name);
      }
    }
    return(newStates);
  }

  /**
   * It build the states adding the states of the node to the states in the 
   * vector
   * @param Vector states. Container of states
   * @param FiniteStates node2. The second node
   * @returns Vector. Container of the states
   */
  private Vector generateStatesOfTwoNodes(Vector states, FiniteStates node2) {
    Vector newStates = new Vector();

    for(int i= 0; i< states.size(); i++) {
      String nameStates = (String)states.elementAt(i);
      for(int j= 0; j< node2.getNumStates(); j++) {
        String name = nameStates + "_x_" + node2.getState(j);
        newStates.addElement(name);
      }
    }
    return(newStates);
  }

  /**
   * Return the number of the cartesian product node's state given the values
   * of the nodes
   * @param Vector values. The values of the nodes
   * @return int. The corresponding identifier
   */
  public int getId(Vector values) {
    if(values.size() != this.numNodes) {
      System.out.println("CartesianProduct: Incorrect values ==> " + values.size() + " != " + this.numNodes);
      System.exit(-1);
    }

    String name = "";
    for(int i= 0; i< this.numNodes-1; i++) {
      String nameState = ((FiniteStates)realNodeList.elementAt(realNodeList.getId((Node)this.vNodes.elementAt(i)))).getState(((Integer)values.elementAt(i)).intValue());
      name = name + nameState + "_x_";
    }
    String nameState = ((FiniteStates)realNodeList.elementAt(realNodeList.getId((Node)this.vNodes.lastElement()))).getState(((Integer)values.lastElement()).intValue());
    name = name + nameState;

    return(this.vStates.indexOf(name));
  }

  /**
   * Return the first node of the cartesian product
   * @returns FiniteStates. The first node of the cartesian product
   */
  public FiniteStates getFirstNode() {
    return((FiniteStates)this.vNodes.firstElement());
  }

  /**
   * Return the nodes of the cartesian product
   * @returns Vector. The nodes of the cartesian product.
   */
  public Vector getNodes() {
    return(this.vNodes);
  }

  /**
   * Return the number of nodes of the cartesian product
   * @returns int. The number of nodes
   */
  public int getNumNodes() {
    return(this.numNodes);
  }

  /**
   * Return the cartesian product node
   * @returns FiniteStates. The cartesian product node
   */
  public FiniteStates getCartesianNode() {
    return ((FiniteStates)(this.cartesianNode.copy()));
  }

} //end private class

  /**
   * Individual
   * 
   * Private class to control the cartesian products.
   * 
   * The individual (1, 0, 1, 2, 1) is representing the
   * predictive variables: (X_0, X_2, X_4), X_3
   * Note that the variable X_1 is not in the model
   */
protected class Individual {

  /**
   * The individual size.
   */
  private int indSize;

  /**
   * The individual genes.
   */
  private int[] individual;

  /**
   * The scoring function of the individual.
   */
  private double score;

  /**
   * The states number of the genes.
   */
  private int states;

  /**
   * The method
   */
  private String method;

  /**
   * The Vector of cartesian products of nodes. Required when the semiNB is carried out
   */
  private Vector vCartesianNodes;

  /**
   * Basic Constructor.
   */
  public Individual() { 
  }

  /**
   * Constructor.
   * @param int size. The individual size.
   * @param int[] ind. The array of genes.
   * @param int st. The states number of the genes.
   */
  public Individual(int size, int[] ind, int st, String met) {
    this.indSize         = size;
    this.method          = met;
    this.score           = Double.MIN_VALUE;
    this.vCartesianNodes = new Vector();
    this.states          = st;
    this.individual      = new int[this.indSize];
    for(int i= 0; i< this.indSize; i++)
      this.individual[i] = ind[i];
  }

  /**
   * Given a cartesian product node, return the cartesian product
   * @param Vector vProducts. The vector of nodes
   * @param Node node. The cartesian product node
   */
  private CartesianProduct getCartesianNode(Vector vProducts, Node node) {
    for(int i= 0; i< vProducts.size(); i++) {
      CartesianProduct c = (CartesianProduct)vProducts.elementAt(i);
      if(c.getCartesianNode().getName().equals(node.getName()))
        return (c);
    }
    return(new CartesianProduct());
  }

  /**
   * Return a new DataBaseCases following the struture of the individual
   * @param DataBaseCases data. The original database
   * @param DataBaseCases. The new database
   */
  public DataBaseCases generateCartesianDBC(DataBaseCases data) {
    Vector cartesianProducts = new Vector();
    for(int s= 1; s< this.states; s++) {
      Vector thisGroup = new Vector();
      for(int i= 0; i< this.indSize; i++)
        if(this.getValue(i) == s) thisGroup.addElement(data.getNodeList().elementAt(i).copy());
      cartesianProducts.addElement(thisGroup);
    }
    Vector thisGroup = new Vector();
    thisGroup.addElement(data.getNodeList().lastElement());
    cartesianProducts.addElement(thisGroup);

    return(this.generateCartesianDBC(data, cartesianProducts));
  }

  /**
   * Return a new DataBaseCases following the struture of the individual
   * @param DataBaseCases data. The original database
   * @param Vector cProducts. The vector of cartesian products
   * @param DataBaseCases. The new database
   */
  private DataBaseCases generateCartesianDBC(DataBaseCases data, Vector cProducts) {
    DataBaseCases output = new DataBaseCases();
    output.setName(data.getName());
    output.setTitle(data.getName());

   	Vector      vector       = data.getRelationList();
  	Relation    relation     = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem  = (CaseListMem)relation.getValues();
    NodeList    realNodeList = data.getNodeList();

    //Add the created groups to the newNodeList
    Vector newNodes           = new Vector();
    Vector vCartesianProducts = new Vector();
    for(int i= 0; i< cProducts.size(); i++) {
      Vector group = (Vector)cProducts.elementAt(i);
      if(group.size() == 1)
        newNodes.addElement((Node)group.firstElement());
      if(group.size() > 1) {
        CartesianProduct cartesian = new CartesianProduct(group, realNodeList);
        vCartesianProducts.addElement(cartesian);
        newNodes.addElement(cartesian.getCartesianNode());
      }
    }
    //Add the class
    NodeList newNodeList = new NodeList(newNodes);
    
    CaseListMem newCaseList  = new CaseListMem(newNodeList);
    Configuration newConf    = new Configuration(newNodeList);

    Vector vCases = ((CaseListMem)((Relation)data.getRelationList().get(0)).getValues()).getCases();
    int[] instance = new int[data.getVariables().size()];

    //add the changed instances
    for(int l= 0; l< data.getNumberOfCases(); l++) {
      Configuration auxConf = new Configuration(newNodeList);
//      double[] original     = (double[])vCases.elementAt(l);
//      for(int j= 0; j< original.length; j++)
//        instance[j] = (int)original[j];
      instance = (int[])vCases.elementAt(l);

      for(int j=0; j< newNodeList.size(); j++) {
        FiniteStates auxNode = (FiniteStates)newNodeList.getNodes().elementAt(j);
        if(auxNode.getComment().equals("cartesian product")) {
          CartesianProduct cartes = this.getCartesianNode(vCartesianProducts, auxNode);
          Vector vNodes  = cartes.getNodes();
          Vector vValues = new Vector();
          for(int v= 0; v< vNodes.size(); v++)
            vValues.addElement(new Integer(instance[realNodeList.getId((Node)vNodes.elementAt(v))]));
          auxConf.putValue(auxNode, cartes.getId(vValues));
        }
        else
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
    output.setNodeList(newNodeList);
    output.setRelationList(vRelation);
    output.setNumberOfCases(data.getNumberOfCases());
    
    return(output);
    
  }

  /**
   * Returns an estimation of the accuracy of the structure
   * @param DataBaseCases cases. The original data to estimate the accuracy of
   *                             the structure
   * @param int k. The k of the fold-cross-validation
   * @returns double. The estimation of the accuracy
   */
  private double evaluateSemi(DataBaseCases cases, int k) throws elvira.InvalidEditException, java.lang.Exception{
    Vector cartesianProducts = new Vector();
    Random generator         = new Random(System.currentTimeMillis());
    for(int s= 1; s< this.states; s++) {
      int nStates = 1;
      Vector thisGroup = new Vector();
      for(int i= 0; i< this.indSize; i++)
        if(this.getValue(i) == s) {
          thisGroup.addElement(cases.getNodeList().elementAt(i).copy());
          nStates *= ((FiniteStates)cases.getNodeList().elementAt(i)).getNumStates();
        }
      while ((nStates > 10000)  || (nStates < 1)) {
        int aleat         = generator.nextInt(thisGroup.size());
        this.individual[cases.getNodeList().getId((Node)thisGroup.elementAt(aleat))] = 0;
        nStates = nStates / ((FiniteStates)cases.getNodeList().elementAt(cases.getNodeList().getId((Node)thisGroup.elementAt(aleat)))).getNumStates();
        if (nStates < 1) {
          nStates = 1;
          for(int t= 0; t< thisGroup.size(); t++)
            nStates *= ((FiniteStates)(thisGroup.elementAt(t))).getNumStates();
        }
        thisGroup.removeElementAt(aleat);
      }

      cartesianProducts.addElement(thisGroup);
    }
    Vector thisGroup = new Vector();
    thisGroup.addElement(cases.getNodeList().lastElement());
    cartesianProducts.addElement(thisGroup);

    DataBaseCases data            = this.generateCartesianDBC(cases, cartesianProducts);
    Naive_Bayes nb                = new Naive_Bayes();
    ClassifierValidator validator = new ClassifierValidator(nb, data, data.getVariables().size()-1);
    return(1 - (validator.kFoldCrossValidation(k).getError()));
  }

  /**
   * Calculate an estimation of the accuracy of the structure
   * @param DataBaseCases cases. The original data to estimate the accuracy of
   *                             the structure
   * @param int k. The k of the fold-cross-validation
   */
  public void evaluate(DataBaseCases cases, int k) throws java.lang.Exception{
    this.score = this.evaluateSemi(cases, k);
  }

  /**
   * Return the value of the gene on the given position.
   * @param int position. The position of the required gene.
   * @returns int. The value of the <code> position <code\> gene.
   */
  public int getValue(int position) {
    return this.individual[position];
  }

  /**
   * Return the individual size.
   * @returns int. The individual size.
   */
  private int getSize() {
    return this.indSize;
  }

  /**
   * Return the scoring function of the individual
   * @returns double. The scoring function of the individual.
   */
  public double getScore() {
    return this.score;
  }

}//End private class

  /**
   * The best classifier obtained
   */
  Naive_Bayes accurateClassifier;

  /**
   * The cartesian nodes;
   */
  Individual bestIndividual;

  /**
   * Basic Constructor
   */
  public SemiNaiveBayes() {
    super();
    this.bestIndividual = new Individual();
  }

  /**
   * Constructor. It calls to the super constructor with the same parameter
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public SemiNaiveBayes(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
    super(data, lap);
    this.bestIndividual = new Individual();
  }

/*  public SemiNaiveBayes(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
    super(data, lap, classIndex);
    this.vCartesianNodes = new Vector();
  }
*/

  /**
   * This method learns the classifier structure
   * It is defined with parameters, but it calls to the another
   * structuralLearnig with 5 as k_fold and true to make the laplace correction
   */
  public abstract void structuralLearning() throws elvira.InvalidEditException, Exception;
  
  /**
   * This method assign a class to the instances of the input instance
   * When SemiNaiveBayes the assigned class is the class which maximizes:
   * p(c) prod( p(x_i|c) ) as in naive-Bayes.
   * However, the NodeList don't contain all the predictive variables
   * and several variables might be a cartesian product of two (or more)
   * predictive variables
   * @param. double[] caseTest. The input instance to assign a class
   */
  public int assignClass(double[] caseTest) {
    if(caseTest.length == this.accurateClassifier.getClassifier().getNodeList().size()) 
      return(this.accurateClassifier.assignClass(caseTest));
    else {
      Vector      vector       = this.cases.getRelationList();
      Relation    relation     = (Relation)vector.elementAt(0);
      CaseListMem caselistmem  = (CaseListMem)relation.getValues();
      NodeList    realNodeList = this.cases.getNodeList();

      int instanceSize  = this.accurateClassifier.getClassifier().getNodeList().size();
      double[] instance = new double[instanceSize];
      
      //If the resulting classifier is a normal NB
      if (realNodeList.size() == this.accurateClassifier.getClassifier().getNodeList().size())
        return(this.accurateClassifier.assignClass(caseTest));

      //If not
      NodeList nodeList = this.accurateClassifier.getClassifier().getNodeList();

      for(int c= 0; c< instanceSize - 1; c++) {
        int value = -1;
        int state = -1;
        if (!nodeList.elementAt(c).getComment().equals("cartesian product"))
          state = this.bestIndividual.getValue(realNodeList.getId(nodeList.elementAt(c)));
        else {
          String name = nodeList.elementAt(c).getName();
          String nodeName = name.substring(0, name.indexOf("_x_"));
          state = this.bestIndividual.getValue(realNodeList.getId(nodeName));
        }

        Vector vNodes  = new Vector();
        Vector vValues = new Vector();
        for(int i= 0; i< this.bestIndividual.getSize(); i++) {
          if (this.bestIndividual.getValue(i) == state) {
            vNodes.addElement(realNodeList.elementAt(i));
            vValues.addElement(new Integer((int)caseTest[i]));
          }
        }
        if (vNodes.size() == 1)
          value = (int)caseTest[realNodeList.getId((Node)vNodes.firstElement())];
        else {
          CartesianProduct cartes = new CartesianProduct(vNodes, realNodeList);
          value                   = cartes.getId(vValues);
        }
        instance[c] = value;
      }
      instance[instance.length -1] = (int)caseTest[caseTest.length -1];

      return(this.accurateClassifier.assignClass(instance));
    }
  }

  /** 
   * Method of Classifier interface. This method is used to classify a instance
	 * @param instance case to classify
	 * @param classnumber number of the variable to classify
	 * @return a Vector with a probability associated to each class
   */
  public Vector classify (Configuration instance, int classnumber) {
    if(instance.getVariables().size() == this.accurateClassifier.getClassifier().getNodeList().size()) 
      return(this.accurateClassifier.classify(instance, classnumber));
    else {
      Vector      vector       = this.cases.getRelationList();
      Relation    relation     = (Relation)vector.elementAt(0);
      CaseListMem caselistmem  = (CaseListMem)relation.getValues();
      NodeList    realNodeList = this.cases.getNodeList();

      int instanceSize         = this.accurateClassifier.getClassifier().getNodeList().size();
      double[] reducedInstance = new double[instanceSize];
      Vector reducedNodes      = this.accurateClassifier.getClassifier().getNodeList().getNodes();
      Vector reducedValues     = new Vector();
      
      NodeList nodeList = this.accurateClassifier.getClassifier().getNodeList();

      int numberClass = ((FiniteStates)this.accurateClassifier.getClassifier().getNodeList().lastElement()).getNumStates();
      for(int l= 0; l< instanceSize - 1; l++) {
        int value = -1;
        int state = -1;
        if (!nodeList.elementAt(l).getComment().equals("cartesian product"))
          state = this.bestIndividual.getValue(realNodeList.getId(nodeList.elementAt(l)));
        else {
          String name = nodeList.elementAt(l).getName();
          String nodeName = name.substring(0, name.indexOf("_x_"));
          state = this.bestIndividual.getValue(realNodeList.getId(nodeName));
        }

        Vector vNodes  = new Vector();
        Vector vValues = new Vector();
        for(int i= 0; i< this.bestIndividual.getSize(); i++) {
          if (this.bestIndividual.getValue(i) == state) {
            vNodes.addElement(realNodeList.elementAt(i));
            vValues.addElement(instance.getValues().elementAt(i));
          }
        }
        if (vNodes.size() == 1)
          value = (int)instance.getValue((FiniteStates)vNodes.firstElement());
        else {
          CartesianProduct cartes = new CartesianProduct(vNodes, realNodeList);
          value                   = cartes.getId(vValues);
        }
        reducedInstance[l] = value;
        reducedValues.addElement(new Integer(value));
      }
      
      reducedValues.addElement(instance.getValues().lastElement());//Class added
      Configuration reducedConfiguration = new Configuration(reducedNodes, reducedValues);
      return(this.accurateClassifier.classify(reducedConfiguration, classnumber));
    }    
  }
  
  /**
   * This method makes the factorization of the classifier
   * In this case, the factorization is like a NB.
   * However, the NodeList don't contain all the predictive variables
   * and several variables might be a cartesian product of two (or more)
   * predictive variables
   */
  public void parametricLearning() {
    this.accurateClassifier.parametricLearning();
    this.logLikelihood = this.accurateClassifier.getLogLikelihood();
  }


} //End of class
