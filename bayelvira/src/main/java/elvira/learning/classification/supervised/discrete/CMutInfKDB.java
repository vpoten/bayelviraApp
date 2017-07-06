/* CMutInfKDB.java */
package elvira.learning.classification.supervised.discrete;

import elvira.Bnet;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.database.DataBaseCases;
import elvira.learning.preprocessing.FilterMeasures;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

/**
 * CMutInfKDB.java
 * 
 * This class implements the algorithm proposed in Sahami (1996) 
 * Learning Limited Dependence Bayesian Classifiers
 * 
 * Similar to TAN, the algorithm allows up to "k" parents to each node
 *
 * @author Rosa Blanco rosa@si.ehu.es UPV
 * @version 0.1
 * @since 03/09/2003
 */

public class CMutInfKDB extends KDB {

  /**
   * Basic Constructor
   */
  public CMutInfKDB() {
    super();
  }

  /**
   * Constructor
   * @param int n. The maximun number of parents
   */
  public CMutInfKDB(int n) {
    super(n);
  }

  /**
   * Constructs a CMutInfKDB model.
   * @author dalgaard
   * @param lap - enable laplace correction in parameter estimation.
   * @param n - maximum number of parents for any single feature variable.
   */
public CMutInfKDB(boolean lap, int n){
	  super(lap, n);
  }
  
  /**
   * Constructor.
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   * @param int n. The maximun number of parents
   */
  public CMutInfKDB(DataBaseCases data, boolean lap, int n) throws elvira.InvalidEditException {
    super(data, lap, n);
  }

  /**
   * This method learns the classifier structure by means of the
   * algorithm proposed in Sahami (1996)
   */
  public void structuralLearning() throws elvira.InvalidEditException {
    this.evaluations = 1; //There is not search

    Vector<Node> vectorNodes = new Vector<Node>();
    for (int i= 0; i< this.nVariables; i++)
      vectorNodes.add(this.cases.getVariables().elementAt(i).copy());
    NodeList nodeList = new NodeList(vectorNodes);

    //FiniteStates classVariable = (FiniteStates)nodeList.lastElement();
    this.classVar = (FiniteStates)nodeList.elementAt(this.classIndex);
    String nameClass           = this.classVar.getName().concat(" ClassNode");
    this.classVar.setTitle(nameClass);

    Vector<MutInf> mutualInfXiC       = new Vector<MutInf>();
    Vector<CMutInf> condMutualInfXiXjC = new Vector<CMutInf>();

    //Calculate all the I(X_i, C)
    FilterMeasures filterMeasures = new FilterMeasures(this.cases);
    for(int i= 0 ; i< this.nVariables; i++) {
    	if(i == this.classIndex) continue;
    	MutInf mutInf = new MutInf(filterMeasures.mutualInformation(nodeList.elementAt(i)), (FiniteStates)nodeList.elementAt(i));
    	mutualInfXiC.addElement(mutInf);
    }

    //Calculate all the I(X_i, X_j | C)
    for(int i= 0; i< this.nVariables; i++){
    	if(i == this.classIndex) continue;
      	for(int j= i+1; j< this.nVariables; j++) {
      		if(j==this.classIndex) continue;
      		CMutInf cMutInf = new CMutInf(this.cases, (FiniteStates)nodeList.elementAt(i), (FiniteStates)nodeList.elementAt(j));
      		condMutualInfXiXjC.addElement(cMutInf);
      	}
    }

    //Order the vectors
    Comparator compareMutInf = new MutInfComparator();
    Collections.sort(mutualInfXiC, compareMutInf);
    
    Comparator compareCMutInf = new CMutInfComparator();
    Collections.sort(condMutualInfXiXjC, compareCMutInf);

    this.classifier = new Bnet();
    for(int i= 0; i< this.nVariables; i++) {
    	//if(i == this.classIndex) continue;
    	this.classifier.addNode(nodeList.elementAt(i));
    	this.classifier.addRelation(nodeList.elementAt(i));
    }
    //this.classifier.addNode(nodeList.elementAt(this.classIndex));
    //this.classifier.addRelation(nodeList.elementAt(this.classIndex));

    Vector<Node> includedNodes = new Vector<Node>();
    /**
     * NOTE: (dalgaard)
     * It seems to me that the following loop should rather be 
     * a while loop that terminates when mutualInfXiC.isEmpty() returns true, but
     * I am not sure so I leave it for now as it is.
     * */
    for(int s= 0; s< this.nVariables-1; s++) {
    	//if(s == this.classIndex) continue;
      MutInf selMutInf     = (MutInf)mutualInfXiC.firstElement();
      FiniteStates selNode = selMutInf.getNode();
      System.out.println("Node " + selNode.toString() + " included " + selMutInf.getScore());
      this.addLink(this.classifier, this.classVar, selNode);
      System.out.println("Add link " + this.classVar.toString() + " --> " + selNode.toString());

      int limit = this.k_parents;
      if (includedNodes.size() < limit) limit = includedNodes.size();

      Iterator condIterator = condMutualInfXiXjC.iterator();
      int addedLink         = 0;
      if (includedNodes.size() > 0) 
        while (addedLink < limit) {
          CMutInf element = (CMutInf)condIterator.next();
          if ((element.getNode1().equals(selNode)) && (includedNodes.indexOf(element.getNode2()) != -1)) {
            this.addLink(this.classifier, (FiniteStates)nodeList.elementAt(nodeList.getId(element.getNode2())), selNode);
            addedLink ++;
            System.out.println("Add link " + element.getNode2().toString() + " --> " + selNode.toString() + " " + element.getScore());
          }
          else if (element.getNode2().equals(selNode) && (includedNodes.indexOf(element.getNode1()) != -1)) {
            this.addLink(this.classifier, (FiniteStates)nodeList.elementAt(nodeList.getId(element.getNode1())), selNode);
            addedLink ++;
            System.out.println("Add link " + element.getNode1().toString() + " --> " + selNode.toString() + " " + element.getScore());
        }
      }
      mutualInfXiC.removeElement(selMutInf);
      includedNodes.addElement(selNode);
    }

    Vector defaultStates = new Vector();
    defaultStates.addElement(this.classifier.ABSENT);
    defaultStates.addElement(this.classifier.PRESENT);
    this.classifier.setFSDefaultStates(defaultStates);
    this.classifier.setName("KDB");
  }

  /**
   * Main to use the class from the command line
   */
  public static void main(String[] args) throws FileNotFoundException, IOException, elvira.InvalidEditException, elvira.parser.ParseException, Exception{
    //Comprobar argumentos
    if(args.length != 4) {
      System.out.println("Usage: file-train.dbc file-test.dbc file-out.elv max-parents");
      System.exit(0);
    }

    FileInputStream fi = new FileInputStream(args[0]);
    DataBaseCases   db = new DataBaseCases(fi);
    fi.close();

    CMutInfKDB clasificador = new CMutInfKDB(db, true, (new Integer(args[3])).intValue());
    clasificador.train();
    
    System.out.println("Classifier learned");

    FileWriter fo = new FileWriter(args[2]);
    clasificador.getClassifier().saveBnet(fo);
    fo.close();

    FileInputStream ft = new FileInputStream(args[1]);
    DataBaseCases   dt = new DataBaseCases(ft);
    ft.close();

    double accuracy = clasificador.test(dt);

    System.out.println("Classifier tested. Accuracy: " + accuracy);

    clasificador.getConfusionMatrix().print();

  }

	public void saveModelToFile(String ap) throws java.io.IOException {
		java.io.FileWriter fw = new java.io.FileWriter(ap+"CMutInfKDB.elv");
		this.classifier.saveBnet(fw);
		fw.close();
	}

  
}