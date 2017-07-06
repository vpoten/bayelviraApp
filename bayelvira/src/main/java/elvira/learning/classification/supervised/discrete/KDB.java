/* KDB.java */
package elvira.learning.classification.supervised.discrete;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Link;
import elvira.LinkList;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.learning.classification.AuxiliarPotentialTable;
import elvira.potential.PotentialTable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

/**
 * KDB.java
 * 
 * This class is parent of all the KDB models. The structuralLearning method
 * is not defined. It must be implemented in the subclasses
 *
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 0.1
 * @since 03/09/2003
 */

public class KDB extends DiscreteClassifier {

/**
 * MutInf
 * 
 * Protected class to construct a vector of (node, score) where
 * a node is related with it mutual information
 * given the class.
 * 
 */

protected class MutInf{

  /**
   * The value of the conditional mutual information.
   */
  double score;

  /**
   * The node.
   */
  FiniteStates node;

  /**
   * The database
   */
  DataBaseCases data;

  /**
   * Basic Constructor.
   */
  public MutInf() {
  }

  /**
   * Constructor
   * @param double cmi. The value of the mutual information
   * @param int v. The node
   */
  public MutInf(double cmi, FiniteStates v){
	  this.score = cmi;
    this.node  = v;
  }

  /**
   * Access Methods
   */
  public void setScore(double cmi){
    this.score = cmi;
  }

  public void setNode(FiniteStates v){
    this.node = v;
  }

  public double getScore(){
    return this.score;
  }
  
  public FiniteStates getNode(){
  	return this.node;
  }
} //End private class MutInf

/**
 * CMutInf
 * 
 * Protected class to construct a vector of (node, node, score) where
 * a pair of nodes is related with their conditional mutual information
 * given the class.
 * 
 */

protected class CMutInf{

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
 * MutInfComparator
 * 
 * Protected class to order a vector of MutInf
 * It implements the interfaz Comparator
 */

protected class MutInfComparator implements Comparator {
  /**
   * The implementation of the method compare of the interfaz Comparator
   */
  public int compare(Object o1, Object o2){
    MutInf c1 = (MutInf)o1;
    MutInf c2 = (MutInf)o2;

    if (c1.getScore() < c2.getScore()) return (1);
    if (c1.getScore() > c2.getScore()) return (-1);
    return (0);
  }
} // End private class MutInfComparator

/**
 * CMutInfComparator
 * 
 * Protected class to order a vector of CMutInf
 * It implements the interfaz Comparator
 */

protected class CMutInfComparator implements Comparator {
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
  
} // End private class CMutInfComparator


  /**
   * The maximum number of parents
   */
  protected int k_parents;

  /**
   * Basic constructor
   */
  public KDB() {
    super();
    this.k_parents = 2;
  }

  /**
   * Constructor
   * @param int n. The maximun number of parents
   */
  public KDB(int n) {
    super();
    this.k_parents = n;
  }

  /**
   * Constructor. 
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   * @param int n. The maximun number of parents
   */
  public KDB(DataBaseCases data, boolean lap, int n) throws elvira.InvalidEditException {
    super(data, lap);
    this.k_parents = n;
  }
  
  /**
   * Constructs a KDB model.
   * @author dalgaard
   * @param lap - enable laplace correction.
   * @param n - the maximum number of parents allowed.
   */
  public KDB(boolean lap, int n){
	  	super(lap);
	  	this.k_parents = n;
  }

  /**
   * This method add a link in a bnet
   * @param Bnet net. The net.
   * @param FiniteStates parent. The parent of the link.
   * @param FiniteStates son. The son of the link.
   */
  protected void addLink(Bnet net, FiniteStates parent, FiniteStates son) {
    Vector relations        = net.getRelationList();
    int indexRelationParent = -1;
    int indexRelationSon    = -1;
    for(int i= 0; i< relations.size(); i++) {
      if (((Relation)relations.elementAt(i)).getVariables().firstElement().equals(parent))
        indexRelationParent = i;
      if (((Relation)relations.elementAt(i)).getVariables().firstElement().equals(son))
        indexRelationSon    = i;
    }

    Link newLink = new Link(parent, son);

    LinkList netLinks = net.getLinkList();
    netLinks.insertLink(newLink);

    LinkList childrenLinks = parent.getChildren();
    childrenLinks.insertLink(newLink);
    LinkList parentsLinks  = son.getParents();
    parentsLinks.insertLink(newLink);

    Relation relationParent = new Relation(parent);
    Relation relationSon    = new Relation(son);
    relations.setElementAt(relationParent, indexRelationParent);
    relations.setElementAt(relationSon, indexRelationSon);
  }

  /**
   * This method remove a link in a bnet
   * @param Bnet net. The net.
   * @param FiniteStates parent. The parent of the link.
   * @param FiniteStates son. The son of the link.
   */
  protected void deleteLink(Bnet net, FiniteStates parent, FiniteStates son) {
    Vector relations        = net.getRelationList();
    int indexRelationParent = -1;
    int indexRelationSon    = -1;
    for(int i= 0; i< relations.size(); i++) {
      if (((Relation)relations.elementAt(i)).getVariables().firstElement().equals(parent))
        indexRelationParent = i;
      if (((Relation)relations.elementAt(i)).getVariables().firstElement().equals(son))
        indexRelationSon    = i;
    }

    Link newLink = new Link(parent, son);

    LinkList netLinks = net.getLinkList();
    netLinks.removeLink(newLink);

    LinkList childrenLinks = parent.getChildren();
    LinkList parentsLinks  = son.getParents();
    childrenLinks.removeLink(newLink);
    parentsLinks.removeLink(newLink);

    Relation relationParent = new Relation(parent);
    Relation relationSon    = new Relation(son);
    relations.setElementAt(relationParent, indexRelationParent);
    relations.setElementAt(relationSon, indexRelationSon);
  }

  /**
   * This method don't do anythig, the structural learning must be implemented
   * in the subclasses
   */
  public void structuralLearning() throws elvira.InvalidEditException, java.lang.Exception {
  }

  /**
   * This method makes the factorization of the classifier.
   */
    /*  public void parametricLearning() {
    Vector auxPotentialTables = new Vector();

    //Create a AuxiliarPoptentialTable vector (one element for each node) in
    //order to caculate the potentials of the variables
    NodeList nodeList       = this.classifier.getNodeList();
    Vector vector           = this.cases.getRelationList();
    Relation relation       = (Relation)vector.elementAt(0);
    CaseListMem caselistmem = (CaseListMem)relation.getValues();
    Node classNode          = this.classifier.getNodeList().lastElement();

    for(int i= 0; i< this.classifier.getNodeList().size(); i++) {
      AuxiliarPotentialTable aux = new AuxiliarPotentialTable(((FiniteStates)this.classifier.getNodeList().elementAt(i)));
      //The table are initialized with random values of probability
      aux.initialize(0);
      auxPotentialTables.add(aux); 
    }

    for(int l= 0; l< this.nCases; l++) {
      //The class haven't parent
      Iterator auxPotentialIterator = auxPotentialTables.iterator();
      for(int i= 0; i< nodeList.size(); i++) {
        Node currentNode = nodeList.elementAt(i);
        if(!this.classifier.getNodeList().elementAt(i).equals(classNode)) {
          NodeList parentsNode  = currentNode.getParentNodes();
          Vector vParentsNodes  = new Vector();
          Vector vParentsValues = new Vector();
          for(int p= 0; p< parentsNode.size(); p++) { //Parents configuration
            vParentsNodes.addElement(parentsNode.elementAt(p));
            vParentsValues.addElement(new Integer((int)caselistmem.getValue(l, this.cases.getNodeList().getId(parentsNode.elementAt(p)))));
          }
          Configuration parentsConfiguration = new Configuration(vParentsNodes, vParentsValues);
          int pos = this.cases.getVariables().getId(currentNode);
//          System.out.println(currentNode.toString() + " l " + l + " i " + i + " " + (int)caselistmem.getValue(l,pos) + " " + parentsConfiguration.getIndexInTable() + " " + parentsConfiguration.toString());
          ((AuxiliarPotentialTable)auxPotentialIterator.next()).addCase((int)caselistmem.getValue(l,pos), parentsConfiguration, 1);
        }
      }
      ((AuxiliarPotentialTable)auxPotentialTables.lastElement()).addCase((int)caselistmem.getValue(l, this.nVariables-1), 0, 1);
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
    }*/

}
