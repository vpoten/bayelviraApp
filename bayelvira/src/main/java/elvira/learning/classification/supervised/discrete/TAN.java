/* TAN.java */
package elvira.learning.classification.supervised.discrete;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.potential.PotentialTable;
import elvira.learning.classification.AuxiliarPotentialTable;
import elvira.learning.classification.ConfusionMatrix;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

/**
 * TAN.java
 * 
 * This class is parent of all the TAN models. The structuralLearning method
 * is not defined. It must be implemented in the subclasses
 *
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 0.1
 * @since 15/05/2003
 */

public class TAN extends DiscreteClassifierDiscriminativeLearning {
  /**
   * A Random object to generate numbers randomly
   * It is a class atribute to set the seed just once
   */
  protected static Random generator;

  /**
   * Basic Constructor
   */
  public TAN() {
    super();
    this.generator = new Random(System.currentTimeMillis());
  }

  /**
   * Constructor. It calls to the super constructor with the same parameter
   * and fills the <code>classElements<\code> array
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public TAN(DataBaseCases data, boolean lap) throws elvira.InvalidEditException {
    super(data, lap);
    this.generator = new Random(System.currentTimeMillis());
  }
   
/*  public TAN(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException {
    super(data, lap, classIndex);
    generator = new Random(System.currentTimeMillis());

  	Vector      vector      = this.cases.getRelationList();
   	Relation    relation    = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem = (CaseListMem)relation.getValues();

    FiniteStates classStates = new FiniteStates();
    classStates = (FiniteStates)(caselistmem.getVariables()).elementAt(this.nVariables-1);

   	this.classElements = new int[this.classNumber];
   	int nTimes;
   	for(int c= 0; c< this.classNumber; c++) {
       nTimes= 0;
       for(int i= 0; i< this.nCases; i++) {
         if (caselistmem.getValue(i, this.nVariables -1) == (classStates.getId(classStates.getState(c))))
          nTimes = nTimes + 1;
       }
       
       this.classElements[c] = nTimes;
    }
  }
*/

  /**
   * This method checks if a graph is not acyclic when an arc from head to tail is included
   * @param Graph tree. The input graph (without the link from head to tail)
   * @param Node head. The <code>head<code\> node of the undirected link
   * @param Node tail. The <code>tail<code\> node of the undirected link
   * @returns boolean. The <code> tree <code\> contains a cycle if a link between <code> head <code\> and <code> tail <code\> is added
   */

  protected boolean makesCycle(Graph tree, Node head, Node tail) {
    return(tree.undirectedPath(head, tail, new Vector()));
  }

  /**
   * This method don't do anythig, the structural learning must be implemented
   * in the subclasses
   */
  public void structuralLearning() throws elvira.InvalidEditException, java.lang.Exception {
  }

  /**
   * This method makes the factorization of the classifier
   */
    /*  public void parametricLearning() {
    Vector auxPotentialTables = new Vector();

    //Create a AuxiliarPoptentialTable vector (one element for each node) in
    //order to caculate the potentials of the variables
    NodeList nodeList       = this.cases.getVariables();
    Vector vector           = this.cases.getRelationList();
    Relation relation       = (Relation)vector.elementAt(0);
    CaseListMem caselistmem = (CaseListMem)relation.getValues();
    Node classNode          = this.cases.getNodeList().lastElement();

    for(int i= 0; i< this.nVariables; i++) {
      Configuration parentsConfiguration = new Configuration(this.classifier.getNodeList().elementAt(i).getParentNodes());
      AuxiliarPotentialTable aux;
      aux = new AuxiliarPotentialTable(((FiniteStates)this.classifier.getNodeList().elementAt(i)));
      //The table are initialized with random values of probability
      aux.initialize(0);
      auxPotentialTables.add(aux); 
    }

    for(int l= 0; l< this.nCases; l++) {
      //The class haven't parent
      for(int i= 0; i< this.nVariables; i++) {
        if(!this.classifier.getNodeList().elementAt(i).equals(classNode)) {
          NodeList parentsNode  = this.classifier.getNodeList().elementAt(i).getParentNodes();
          Vector vParentsNodes  = new Vector();
          Vector vParentsValues = new Vector();
          for(int p= 0; p< parentsNode.size(); p++) { //Parents configuration
            vParentsNodes.addElement(parentsNode.elementAt(p));
            vParentsValues.addElement(new Integer((int)caselistmem.getValue(l, this.classifier.getNodeList().getId(parentsNode.elementAt(p)))));
          }
          Configuration parentsConfiguration = new Configuration(vParentsNodes, vParentsValues);
          ((AuxiliarPotentialTable)auxPotentialTables.elementAt(i)).addCase((int)caselistmem.getValue(l,i), parentsConfiguration, 1);
        }
      }

      ((AuxiliarPotentialTable)auxPotentialTables.elementAt(this.nVariables-1)).addCase((int)caselistmem.getValue(l, this.nVariables-1), 0, 1);
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

  /**
   * Method setRandomSeed modifies the generator Random object by calling
   * the setSeed method. It is usefull to ensure that consecutive executions
   * all generate the same sequence of random numbers. In debugging, this is 
   * often very usefull.
   * 
   * @param seed - the seed to use.
   *
   * @author dalgaard
   */
public static void setRandomSeed(long seed){
	generator.setSeed(seed);  
  }
  
}//End of class
