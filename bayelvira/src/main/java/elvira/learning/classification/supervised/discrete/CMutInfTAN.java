/* CMutInfTAN.java */

package elvira.learning.classification.supervised.discrete;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.Link;
import elvira.LinkList;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.learning.classification.AuxiliarPotentialTable;

import java.io.*;
import java.lang.Math;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import java.util.Random;

/**
 * ConditionalMutualInformationTAN.java
 * 
 * This class learns a TAN by means of the Chow & Liu algorithm.
 * It follows the algorithm proposed in:
 * Friedman et al. (1996) Bayesian network Classifiers
 * It implies that the set of relationships among the predictive variables
 * form a tree where a node have a unique parent.
 * It uses the conditional mutual information between two variable with
 * respect to the class variable.
 *
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 0.4.2
 * @since 26/03/2003
 */

public class CMutInfTAN extends TAN {

/**
 * CMutInf
 * 
 * Private class to construct a vector of (node, node, score) where
 * a pair of nodes is related with their conditional mutual information
 * given the class.
 * 
 */

private class CMutInf{

  /**
   * The value of the conditional mutual information.
   */
  double score;

  /**
   * The first node of the pair.
   */
  FiniteStates node1;

  /**
   * The second node of the pair.
   */
  FiniteStates node2;

  /**
   * The database
   */
  DataBaseCases data;

  /**
   * Basic Constructor.
   */
  public CMutInf() {
  }

  /**
   * Constructor. It calculates the conditional mutual information of the node1 
   * and node2 given the class variable
   * @param DataBaseCases cases. The database to calculate the conditional mutual information
   * @param int v1. The first variable
   * @param int v2. The second variable
   */
  public CMutInf(DataBaseCases cases, FiniteStates v1, FiniteStates v2){
    this.data  = cases;
    this.node1 = v1;
    this.node2 = v2;

    Vector vector           = this.data.getRelationList();
    Relation relation       = (Relation)vector.elementAt(0);
    CaseListMem caselistmem = (CaseListMem)relation.getValues();
    NodeList nodelist       = this.data.getNodeList();
    FiniteStates classNode  = (FiniteStates)nodelist.lastElement();

    int numStatesNode1 = this.getNode1().getNumStates();
    int numStatesNode2 = this.getNode2().getNumStates();

    AuxiliarPotentialTable potentialNode1 = new AuxiliarPotentialTable(this.node1.getNumStates(), classNode.getNumStates());
    potentialNode1.initialize(0);
    AuxiliarPotentialTable potentialNode2 = new AuxiliarPotentialTable(this.node2.getNumStates(), classNode.getNumStates());
    potentialNode2.initialize(0);
    AuxiliarPotentialTable potentialNode1Node2 = new AuxiliarPotentialTable(numStatesNode1 * numStatesNode2, classNode.getNumStates());
    potentialNode1Node2.initialize(0);

    //Relative frecuencies given the class: p(node1|c), p(node2|c) and p(node1, node2|c)
    for(int l= 0; l< this.data.getNumberOfCases(); l++) {
      potentialNode1.addCase((int)caselistmem.getValue(l, nodelist.getId(this.node1)), (int)caselistmem.getValue(l, nodelist.getId(nodelist.lastElement())), 1);
      potentialNode2.addCase((int)caselistmem.getValue(l, nodelist.getId(this.node2)), (int)caselistmem.getValue(l, nodelist.getId(nodelist.lastElement())), 1);
      int position = numStatesNode2 * (int)caselistmem.getValue(l, nodelist.getId(this.node1)) + (int)caselistmem.getValue(l, nodelist.getId(this.node2));
      potentialNode1Node2.addCase(position, (int)caselistmem.getValue(l, nodelist.getId(nodelist.lastElement())), 1);
    }

    double sumXYC = 0;
    for(int i= 0; i< numStatesNode1; i++) {
      for(int j= 0; j< numStatesNode2; j++) {
        for(int c= 0; c< classNode.getNumStates(); c++) {
          int position = numStatesNode2 * i + j;
          double pXYC  = potentialNode1Node2.getPotential(position, c);
          if (pXYC != 0)
            sumXYC += pXYC * (Math.log(pXYC) / Math.log(10));
        }
      }
    }

    double sumXC  = 0;
    for(int i= 0; i< numStatesNode1; i++) {
      for(int c= 0; c< classNode.getNumStates(); c++) {
        double pXC  = potentialNode1.getPotential(i,c);
        if (pXC != 0)
          sumXC += pXC * (Math.log(pXC) / Math.log(10));
      }
    }
    
    double sumYC  = 0;
    for(int i= 0; i< numStatesNode2; i++) {
      for(int c= 0; c< classNode.getNumStates(); c++) {
        double pYC  = potentialNode2.getPotential(i,c);
        if (pYC != 0)
          sumYC += pYC * (Math.log(pYC) / Math.log(10));
      }
    }

    this.score = sumXYC - sumXC - sumYC;
  }

  /**
   * Constructor
   * @param double cmi. The value of the coditional mutual information between two variables given the class
   * @param int v1. The first variable
   * @param int v2. The second variable
   */
  public CMutInf(double cmi, FiniteStates v1, FiniteStates v2){
	  this.score = cmi;
    this.node1 = v1;
    this.node2 = v2;
  }

  /**
   * Access Methods
   */
  public void setScore(double cmi){
    this.score = cmi;
  }

  public void setNode1(FiniteStates v1){
    this.node1 = v1;
  }

  public void setNode2(FiniteStates v2){
    this.node2 = v2;
  }

  public double getScore(){
    return this.score;
  }
  
  public FiniteStates getNode1(){
  	return this.node1;
  }
  
  public FiniteStates getNode2(){
  	return this.node2;
  }
} //End private class CMutInf

    /**
     * CMIComparator
     * 
     * Private class to order a vector of CMutInf
     * It implements the interfaz Comparator
     */

private class CMIComparator implements Comparator {
  /**
   * The implementation of the method compare of the interfaz Comparator
   */

  public int compare(Object o1, Object o2){
    CMutInf c1 = (CMutInf)o1;
    CMutInf c2 = (CMutInf)o2;

    if (c1.getScore() < c2.getScore()) return (1);
    if (c1.getScore() > c2.getScore()) return (-1);
    return (0);
  }
  
} // End private class CMIComparator

  /**
   * Basic Constructor
   */
  public CMutInfTAN() {
    super();
  }

  /**
   * Constructor.
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public CMutInfTAN(DataBaseCases data, boolean lap) throws elvira.InvalidEditException {
    super(data, lap);
  }
   
/*  public CMutInfTAN(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException {
    super(data, lap, classIndex);
  }
*/
  /**
   * This method learns the classifier structure by means of the
   * Chow & Liu algorithm adapted by Friedman et al.
   */
  public void structuralLearning() throws elvira.InvalidEditException {
    this.evaluations = 1; //There is not search
    
  	Vector      vector      = this.cases.getRelationList();
  	Relation    relation    = (Relation)vector.elementAt(0);
  	CaseListMem caselistmem = (CaseListMem)relation.getValues();

    Vector vectorNodes = new Vector();
    for (int i= 0; i< this.nVariables; i++)
      vectorNodes.add(this.cases.getVariables().elementAt(i).copy());
    NodeList nodeList = new NodeList(vectorNodes);

    Graph tree = new Graph(1); //A undirected empty Graph is created
    tree.setNodeList(nodeList);

  	//First, the ordered vector of pairs of nodes is created and fille
    Vector vCMutInforPairs = new Vector();
    for(int i= 0; i< this.nVariables-1; i++)
      for(int j= i+1; j< this.nVariables-1; j++) {
        CMutInf element = new CMutInf(this.cases, (FiniteStates)nodeList.elementAt(i), (FiniteStates)nodeList.elementAt(j));
        vCMutInforPairs.add(element);
      }

    Comparator compare = new CMIComparator();
    Collections.sort(vCMutInforPairs, compare);

    int selectedBranch = 0;
    int branch         = 0;
    while(selectedBranch < this.nVariables-2) {
      CMutInf element = (CMutInf)vCMutInforPairs.elementAt(branch);

      if (selectedBranch < 2) {
        tree.createLink(element.getNode1(), element.getNode2(), false);
        selectedBranch ++;
      }
      else {
        if (makesCycle(tree, element.getNode1(), element.getNode2())) ;
        else {
          Link newLink = new Link(element.getNode1(), element.getNode2(), false);
          tree.createLink(element.getNode1(), element.getNode2(), false);
          selectedBranch ++;
        }
      }
      
      branch ++;
    } //end while

    //Randomly select the root of the tree
    int root = generator.nextInt(this.nVariables-1);
    Node rootNode = tree.getNodeList().elementAt(root);
    //System.out.println("The variable " + rootNode.toString() + " is the root of the TAN");

    //Add the arcs from rootNode to the sibling
    NodeList siblingsNodesRoot = new NodeList();
    siblingsNodesRoot = rootNode.getSiblingsNodes();

    Vector<Node> visitedNodes  = new Vector<Node>();
    visitedNodes.add(rootNode);
    Vector<Link> childrenLinksRoot = new Vector<Link>();
    Vector<Link> classifierLinks   = new Vector<Link>();
    Vector<Node> toVisitNodes      = new Vector<Node>();

    NodeList classifierNodes = nodeList.copy();

    for(int i= 0; i< siblingsNodesRoot.size(); i++) {
      Link newLink = new Link(rootNode, siblingsNodesRoot.elementAt(i));
      toVisitNodes.add(siblingsNodesRoot.elementAt(i));
      childrenLinksRoot.add(newLink);
      classifierLinks.add(newLink);
      //System.out.println("CMutInfTAN: The link " + newLink.getTail().toString() + " --- " + newLink.getHead().toString() +  " is added to the classifier");

      Vector<Link> parentsLinksRoot  = new Vector<Link>();
      parentsLinksRoot.add(newLink);
      LinkList parentsListRoot = new LinkList();
      parentsListRoot.setLinks(parentsLinksRoot);
      classifierNodes.elementAt(tree.getNodeList().getId(siblingsNodesRoot.elementAt(i))).setParents(parentsListRoot);
    }

    LinkList childrenListRoot = new LinkList();
    childrenListRoot.setLinks(childrenLinksRoot);
    classifierNodes.elementAt(root).setChildren(childrenListRoot);

    //Following the ordering of the variables, direct the arcs from nodes to their sibling
    while(toVisitNodes.isEmpty() == false) {
      Node nextNode = toVisitNodes.firstElement();
      visitedNodes.add(nextNode);
      NodeList siblingsNodesNode = new NodeList();
      int indexNode = classifierNodes.getId(nextNode);
      siblingsNodesNode = tree.getNodeList().elementAt(indexNode).getSiblingsNodes();
 
      Vector<Link> childrenLinkNode = new Vector<Link>();
      for(int i= 0; i< siblingsNodesNode.size(); i++) {
        if(visitedNodes.indexOf(siblingsNodesNode.elementAt(i)) == -1) { //The siblings node is not visited
          LinkList parentsListSiblingNode = tree.getNodeList().elementAt(tree.getNodeList().getId(siblingsNodesNode.elementAt(i))).getParents();
          if (parentsListSiblingNode.size() == 0) { //The sibling node haven't a parent
            Link newLink = new Link(nextNode, siblingsNodesNode.elementAt(i));
            //System.out.println("CMutInfTAN: The link " + newLink.getTail().toString() + " --- " + newLink.getHead().toString() + " is added to the classifier");
            toVisitNodes.add(siblingsNodesNode.elementAt(i));
            childrenLinkNode.add(newLink);
            classifierLinks.add(newLink);

            Vector<Link> parentsLinksNode  = new Vector<Link>();
            parentsLinksNode.add(newLink);
            LinkList parentsListNode = new LinkList();
            parentsListNode.setLinks(parentsLinksNode);
            classifierNodes.elementAt(tree.getNodeList().getId(siblingsNodesNode.elementAt(i))).setParents(parentsListNode);
          }
          //else System.out.println("The node " + classifierNodes.elementAt(tree.getNodeList().getId(siblingsNodesNode.elementAt(i))).toString() + " have a previous parent");
        }
      }     
      
      LinkList childrenListNode = new LinkList();
      childrenListNode.setLinks(childrenLinkNode);
      classifierNodes.elementAt(indexNode).setChildren(childrenListNode);
      toVisitNodes.remove(nextNode);
    }

    //Add the arcs from the class variable to all variable
    
    /***
     * In the following: We should be able to assume that the classVar, classIndex etc. are set up
     * correctly, therefore,  instead of refering to "this.nVariables-1" we should use
     * those more general fields. (modified by dalgaard, April 16, 2007) 
     **/
    //Node classVariable = classifierNodes.elementAt(this.nVariables-1);
    Node classVariable = classifierNodes.elementAt(this.classIndex);
    String nameClass = classVariable.getName().concat(" ClassNode");
    classVariable.setTitle(nameClass);
    classVariable.setComment("ClassNode");
    
    Vector<Link> childrenLinksClass = new Vector<Link>();
    for(int i= 0; i<this.nVariables; i++) {
    	if(i == this.classIndex) continue;
        childrenLinksClass.add(new Link(classVariable, classifierNodes.elementAt(i)));
        classifierLinks.add(new Link(classVariable, classifierNodes.elementAt(i)));
    }
    
    LinkList childrenListClass = new LinkList();
    childrenListClass.setLinks(childrenLinksClass);
    
    //classifierNodes.elementAt(this.nVariables-1).setChildren(childrenListClass);
    classVariable.setChildren(childrenListClass);
    
    for(int i= 0; i< this.nVariables; i++) {
    	if(i == this.classIndex) continue;
    	//Link parentClassLink = new Link(classifierNodes.elementAt(this.nVariables-1), classifierNodes.elementAt(i));
    	Link parentClassLink = new Link(classVariable, classifierNodes.elementAt(i));
    	LinkList parentsListClass = classifierNodes.elementAt(i).getParents();
    	//insert Link at the end of the list.
    	parentsListClass.getLinks().addElement(parentClassLink);
      classifierNodes.elementAt(i).setParents(parentsListClass);
    }

    this.classifier = new Bnet();

    for(int i= 0; i< this.nVariables; i++) {
      this.classifier.addNode(classifierNodes.elementAt(i));
      this.classifier.addRelation(classifierNodes.elementAt(i));
    }
    LinkList classifierList = new LinkList();
    classifierList.setLinks(classifierLinks);
    this.classifier.setLinkList(classifierList);

    Vector<String> defaultStates = new Vector<String>();
    defaultStates.addElement(Bnet.ABSENT);
    defaultStates.addElement(Bnet.PRESENT);
    this.classifier.setFSDefaultStates(defaultStates);
    this.classifier.setName("classifier TAN");
  }

  /**
   * Main to use the class from the command line
   */
  public static void main(String[] args) throws FileNotFoundException, IOException, elvira.InvalidEditException, elvira.parser.ParseException, Exception{
    //Comprobar argumentos
    if(args.length != 3) {
      System.out.println("Usage: file-train.dbc file-test.dbc file-out.elv");
      System.exit(0);
    }

    FileInputStream fi = new FileInputStream(args[0]);
    DataBaseCases   db = new DataBaseCases(fi);
    fi.close();

    CMutInfTAN clasificador = new CMutInfTAN(db, true);
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
	public void saveModelToFile(String ap) throws java.io.IOException {
		java.io.FileWriter fw = new java.io.FileWriter(ap+"CMutInfTAN.elv");
		this.classifier.saveBnet(fw);
		fw.close();
	}

} //End of class
