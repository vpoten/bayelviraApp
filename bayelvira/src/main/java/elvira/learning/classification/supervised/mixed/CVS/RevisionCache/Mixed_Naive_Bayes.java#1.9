/* Mixed_Naive_Bayes.java */

package elvira.learning.classification.supervised.mixed;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.ContinuousCaseListMem;
import elvira.Configuration;
import elvira.ContinuousConfiguration;
import elvira.FiniteStates;
import elvira.Continuous;
import elvira.Evidence;
import elvira.Link;
import elvira.LinkList;
import elvira.Node;
import elvira.potential.Potential;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.potential.PotentialTable;
import elvira.potential.ContinuousProbabilityTree;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.MixtExpDensity;
import elvira.inference.elimination.VariableElimination;
import elvira.inference.abduction.*;
import elvira.learning.*;
import elvira.learning.classification.*;
import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;

/**
 * Naive_Bayes.java
 *
 * This is a public class to learn a naive-Bayes classification model with continuous and
 * discrete variables.
 * A naive-Bayes classifier assumes independence between the predictives
 * variables given the class.
 * 
 * This class implements the structural learning of the classification model.
 * The parametric learning is implemeted in its subclasses:
 *  - Gaussian_Naive_Bayes, when it's assumed that the continuous variables are Gaussian.
 *  - MTE_Navie_Bayes, when it's assumed that the continuous variables are Mixtures of exponentials
 * 
 * Furthermore, this class implements a structural learning method with feature selection,
 * 'public void selectiveStructuralLearning' method. So, a Naive-Bayes classifier with feature selection 
 * has to:
 *  1. Implement the Selective_Classifier interface.
 *  2. Inherit of a class that has already implemented the parametric learning of the classifier model
 * (Gaussian_Naive_Bayes,for example).
 *  3. Overwrite the following methods: 
 *  - structuralLearning, this method has to invoke selectiveStructuralLearning method now.
 *  - evaluationKFK, evaluationLOO and getNewClassifier, see the method's comments for details.
 *
 * The Selective_GNB class is an example of this type of classifiers.
 *
 * @author Rosa Blanco rosa@si.ehu.es UPV, Seraf�n Moral smc@decsai.ugr.es Univ. Granada.
 *
 * @version 0.1
 * @since 26/05/2003
 */

public class Mixed_Naive_Bayes extends MixedClassifier {

  
  
   /**
   * Basic Constructor
   */
   public Mixed_Naive_Bayes() {}

  /**
   * Constructor
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean correction. To apply the laplace correction
   */
  public Mixed_Naive_Bayes(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
    super(data, lap);
  }

  public Mixed_Naive_Bayes(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
    super(data, lap, classIndex);
  }

  /**
   * This abstract method learns the classifier structure.
   * The naive-Bayes model has only arcs from the class variable
   * to the predictives variables, it assumes that the predicitives
   * variables are independents given the class
   */
  public void structuralLearning() throws elvira.InvalidEditException{
  	Vector      vector      = this.cases.getRelationList();
  	Relation    relation    = (Relation)vector.elementAt(0);
  	CaseListMem caselistmem = (CaseListMem)relation.getValues();

    Vector vectorNodes = new Vector();
    for(int i= 0; i< this.cases.getVariables().size(); i++)
      vectorNodes.add(this.cases.getVariables().elementAt(i).copy());

    NodeList nodeList  = new NodeList(vectorNodes);
    Node classVariable = this.classVar;

//    String nameClass = classVariable.getName().concat(" ClassNode");
//    classVariable.setTitle(nameClass);

    Vector childrenLinks = new Vector();
    
   
    Enumeration enumerator = nodeList.getNodes().elements();
    Node n;
    while(enumerator.hasMoreElements()){
        n=(Node)enumerator.nextElement();
        if (!n.equals(classVariable)){ 
            childrenLinks.add(new Link(classVariable, n));
        }
    }
    
    
    LinkList childrenList = new LinkList();
    childrenList.setLinks(childrenLinks);
    nodeList.elementAt(nodeList.getId(classVariable)).setChildren(childrenList);

    enumerator = nodeList.getNodes().elements();

    
    while(enumerator.hasMoreElements()){
        n=(Node)enumerator.nextElement();
        if (!n.equals(classVariable)){ 
          Vector parentsLinks = new Vector();
          parentsLinks.add(new Link(classVariable,n));
          LinkList parentsList = new LinkList();
          parentsList.setLinks(parentsLinks);
          n.setParents(parentsList);
        }
    }
    

    this.classifier = new Bnet();
    for(int i= 0; i< this.cases.getVariables().size(); i++) {
      this.classifier.addNode((Node)nodeList.elementAt(i));

    }
    this.classifier.setLinkList(childrenList);


    Vector defaultStates = new Vector();
    defaultStates.addElement(this.classifier.ABSENT);
    defaultStates.addElement(this.classifier.PRESENT);
    this.classifier.setFSDefaultStates(defaultStates);
    this.classifier.setName("classifier naive-Bayes");

   }
  

  public long size(){
	  return this.classifier.getNumberOfFreeParameters();
  } 

}//End of class

