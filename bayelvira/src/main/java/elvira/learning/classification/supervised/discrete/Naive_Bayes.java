/* Naive_Bayes.java */

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
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.ConfusionMatrix;

import java.io.*;
import java.util.Vector;
import java.util.Iterator;

/**
 * Naive_Bayes.java
 * 
 * This is a public class to learns a naive-Bayes classification model.
 * A naive-Bayes classifier assumes independence between the predictives
 * variables given the class.
 *
 * @author Rosa Blanco rosa@si.ehu.es UPV.
 * @version 0.2
 * @since 26/03/2003
 */

public class Naive_Bayes extends DiscreteClassifierDiscriminativeLearning {

  /**
   * Basic Constructor
   */
  public Naive_Bayes() {
    super();
  }

  /**
   * Constructor
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean correction. To apply the laplace correction
   */
  public Naive_Bayes(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
    super(data, lap);
  }

/*  public Naive_Bayes(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
    super(data, lap, classIndex);
  }
*/

  /**
   * This abstract method learns the classifier structure.
   * The naive-Bayes model has only arcs from the class variable
   * to the predictives variables, it assumes that the predicitives
   * variables are independents given the class 
   */
  public void structuralLearning() throws elvira.InvalidEditException{
    this.evaluations = 1; //There is not search
    
  	Vector      vector      = this.cases.getRelationList();
  	Relation    relation    = (Relation)vector.elementAt(0);
  	CaseListMem caselistmem = (CaseListMem)relation.getValues();

    Vector vectorNodes = new Vector();
    for(int i= 0; i< this.nVariables; i++)
      vectorNodes.add(this.cases.getVariables().elementAt(i).copy());

    NodeList nodeList  = new NodeList(vectorNodes);
    //Node classVariable = nodeList.lastElement();
    Node classVariable = this.classVar;
    //String nameClass = classVariable.getName().concat(" ClassNode");
    //classVariable.setTitle(nameClass);
    //classVariable.setComment("ClassNode");
    
    Vector childrenLinks = new Vector();
    //for(int i= 0; i< this.nVariables-1; i++) {
    //    childrenLinks.add(new Link(classVariable, nodeList.elementAt(i)));
    //}

    for(int i= 0; i< this.nVariables; i++) {
    	if(i == this.classIndex) continue;
        childrenLinks.add(new Link(classVariable, nodeList.elementAt(i)));
    }

    
    LinkList childrenList = new LinkList();
    childrenList.setLinks(childrenLinks);
    //nodeList.elementAt(this.nVariables-1).setChildren(childrenList);
    nodeList.elementAt(this.classIndex).setChildren(childrenList);

    //for(int i= 0; i< this.nVariables-1; i++) {
    //    Vector parentsLinks = new Vector();
    //    parentsLinks.add(new Link((FiniteStates)nodeList.elementAt(this.nVariables-1),(FiniteStates)nodeList.elementAt(i)));
    //    LinkList parentsList = new LinkList();
    //   parentsList.setLinks(parentsLinks);
    //   ((FiniteStates)nodeList.elementAt(i)).setParents(parentsList);
    //}
    for(int i= 0; i< this.nVariables; i++) {
    	if(i == this.classIndex) continue;
        Vector parentsLinks = new Vector();
        parentsLinks.add(new Link(classVariable,(FiniteStates)nodeList.elementAt(i)));
        LinkList parentsList = new LinkList();
        parentsList.setLinks(parentsLinks);
        ((FiniteStates)nodeList.elementAt(i)).setParents(parentsList);
      }

    this.classifier = new Bnet();
    for(int i= 0; i< this.nVariables; i++) {
      this.classifier.addNode((FiniteStates)nodeList.elementAt(i));
      this.classifier.addRelation((FiniteStates)nodeList.elementAt(i));
    }
    this.classifier.setLinkList(childrenList);

    Vector defaultStates = new Vector();
    defaultStates.addElement(this.classifier.ABSENT);
    defaultStates.addElement(this.classifier.PRESENT);
    this.classifier.setFSDefaultStates(defaultStates);
    this.classifier.setName("classifier naive-Bayes");
    
   }

  /**
   * This method makes the factorization of the classifier
   * When naive-Bayes the factorization is made by means of 
   * the relative frecuencies given the class 
    
  public void parametricLearning(){ 
    Vector auxPotentialTables = new Vector(); 

    //Create a AuxiliarPotentialTable vector (one element for each node) in
    //order to caculate the potentials of the variables
    NodeList nodeList       = this.classifier.getNodeList(); 
    Vector vector           = this.cases.getRelationList(); 
    Relation relation       = (Relation)vector.elementAt(0); 
    CaseListMem caselistmem = (CaseListMem)relation.getValues(); 

    for(int i= 0; i< this.nVariables; i++) { 
      AuxiliarPotentialTable aux = new AuxiliarPotentialTable((FiniteStates)this.classifier.getNodeList().elementAt(i)); 
      //The table are initialized with random values of probability
      aux.initialize(0); 
      auxPotentialTables.add(aux); 
    } 

    for(int l= 0; l< this.nCases; l++) { 
      //The class haven't parents
      for(int i= 0; i< this.nVariables-1; i++) { 
        ((AuxiliarPotentialTable)auxPotentialTables.elementAt(i)).addCase((int)caselistmem.getValue(l,i), (int)caselistmem.getValue(l, this.nVariables-1), 1); 
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

    }
  */
  

   /**
     * Main for constructing a Naive Bayes classifier from a data base.
     *
     * Arguments:
     * 1. the dbc train file.
     * 2. the dbc test file. "CV" for cross-validation.
     * 3. the name of the output network
     * 4. In case of cross validation, the number of folds.
     */
  
  
  public static void main(String[] args) throws FileNotFoundException, IOException, elvira.InvalidEditException, elvira.parser.ParseException, Exception{
    //Comprobar argumentos
    if(args.length != 4) {
      System.out.println("Usage: file-train.dbc {file-test.dbc | CV } file-out.elv [number of folds]");
      System.exit(0);
    }

    
    FileInputStream fi = new FileInputStream(args[0]);
    DataBaseCases   db = new DataBaseCases(fi);
    fi.close();

    Naive_Bayes clasificador = new Naive_Bayes(db, true);
    clasificador.train();

    System.out.println("Classifier learned");

    //TESTING
    if (args[1].compareTo("CV") == 0) { //k-folds cross validation
            
         int k = Integer.valueOf(args[3]).intValue();
         ClassifierValidator validator=new ClassifierValidator(clasificador, db, db.getNodeList().size()-1); 
         ConfusionMatrix cm=validator.kFoldCrossValidation(k);
         System.out.println(k +"-folds Cross-Validation. Accuracy="+(1.0-cm.getError())+"\n\n");
     }
     else //Specific test set
     {
         FileInputStream ft = new FileInputStream(args[1]);
         DataBaseCases   dt = new DataBaseCases(ft);
         ft.close();
         //FileInputStream fTest = new FileInputStream(args[1]);
         DataBaseCases dbcTest=new DataBaseCases(ft);
         double accuracy = clasificador.test(dbcTest);
         System.out.println("Classifier tested. Train accuracy: " + accuracy);   
         clasificador.getConfusionMatrix().print();
     }
     FileWriter fo = new FileWriter(args[2]);
     clasificador.getClassifier().saveBnet(fo);
     fo.close();
  }
  
  
  
  
  
  public void saveModelToFile(String absolutePath) throws IOException {
	  java.io.FileWriter fw = new java.io.FileWriter(absolutePath+"naiveBayes.elv");
	  this.classifier.saveBnet(fw);
	  fw.close();
  }
  
}//End of class

