//UnsupervisedNBayes
package elvira.learning.classification.unsupervised.discrete;

import java.io.*;
import java.util.*;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.potential.*;
import elvira.learning.preprocessing.*;
import elvira.parser.ParseException;
import elvira.learning.classification.AuxiliarPotentialTable;


/**
 * Abstract class that implements a unsupervised clasification for a discrete 
 * variables dataset taking a Naive Bayes structure:
 * @author Guzmán Santafé
 * @version 0.1
 * @since 05/03/2003
 */

public abstract class UnsupervisedNBayes 
{ 
 
  protected int     numberOfClusters;
  protected int     numberOfVariables;
  protected Vector  cases;
  protected Vector  nodes;
  /**
   * value of the loglikelihood of the dataset with the learned Naive-Bayes 
   * classifier.
   */
  protected double  score;
//  private double  undefValue;
  /**
 * Naive-Bayes classifier.
 */
  protected Bnet    classifier;
  
  /**
  *  Create a new NBayesMLEM Objet. This objet implements a classifier 
  *  (Bnet with a Naive-Bayes structure) in order to perform a clustering
  *  of a dataset.
  *  
  *  @param dataCases DataBasesCases object which contains the dataset stimate the classifier
  *  @param numberOfClusters number of clusters that we want to classify the dataset into
  *  
  */  
  public UnsupervisedNBayes(DataBaseCases dataCases, int numberOfClusters)
  {
    //Iterators
    int i;
    int j;
    int k;

    //Data strucutures to go through values and vars
    NodeList    nodeList   ;
    Vector      vector     ;
    Relation    relation   ;
    CaseListMem caselistmem;
    //Node        node       ;

    // Iniatializing variables.
    nodeList    = dataCases.getVariables().copy();
    vector      = dataCases.getRelationList();
    relation    = (Relation)vector.elementAt(0);
    caselistmem = (CaseListMem)relation.getValues();

    // Set the value of class variables
    this.numberOfClusters  = numberOfClusters;
    this.nodes             = nodeList.getNodes();
    this.numberOfVariables = this.nodes.size();

    // Set the missing arguments in the nodes
    Iterator nodeListIterator;
    nodeListIterator = nodes.iterator();
    FiniteStates node;
    for(i=0;nodeListIterator.hasNext();i++)
    {
      node = (FiniteStates)nodeListIterator.next();
      node.setTitle(node.getName());
      //node.setAxis(1,1);
    }
    

    // Stores all the cases from the DataBaseCase 
    this.cases = caselistmem.getCases();

        
    // Create the class node and add it to the nodeList.
    FiniteStates classnode = new FiniteStates(numberOfClusters);

    // possible states of the class node
    Vector states = new Vector();
    for(i=0;i<numberOfClusters;i++)
    {
      // the states value must be strings {"C0","C1", ...}
      states.add("C"+ (new Integer(i)).toString());
    }
    // add the states to the node.
    classnode.setStates(states);

    classnode.setName("ClassNode");
    classnode.setTitle("ClassNode");
    classnode.setKindOfNode(0); //Set the kindOfNode as Chance Node.

    // Construct the children LinkList.
    Vector childrenLinks = new Vector();
    for(i=0;i<numberOfVariables;i++)
    {
      childrenLinks.add(new Link(classnode,(FiniteStates)nodes.elementAt(i)));
    }
    LinkList childrenLinkList = new LinkList();
    childrenLinkList.setLinks(childrenLinks);
    classnode.setChildren(childrenLinkList);

    // Set the classnode as father of all the nodes.
    for(i=0;i<numberOfVariables;i++)
    {
      Vector parentsLinks = new Vector();
      parentsLinks.add(new Link(classnode,(FiniteStates)nodes.elementAt(i)));
      LinkList parentsLinkList = new LinkList();
      parentsLinkList.setLinks(parentsLinks);
      ((FiniteStates)nodes.elementAt(i)).setParents(parentsLinkList);      
    }

    // add the class node as the last node in the nodes vector.
    this.nodes.addElement(classnode);

    // Initialize the classifier (Bnet) except for the Potential Tables which
    // will be obtained by learning().
    classifier = new Bnet();
    classifier.setNodeList(new NodeList(this.nodes));
    classifier.setLinkList(childrenLinkList);

    // add the RelationList to the classifier
    for(i=0;i<numberOfVariables+1;i++) // numberOfVariables+1 in order to include the last element (classNode)
    {
      classifier.addRelation((FiniteStates)nodes.elementAt(i));     
    }
    // add the default states
    Vector defaultStates = new Vector();
    defaultStates.addElement(Bnet.ABSENT);
    defaultStates.addElement(Bnet.PRESENT);
    classifier.setFSDefaultStates(defaultStates);
  }// end NBayesMLEM(DataBaseCases,int)

  /**
   * Initializes the naive Bayes classifier to a new naive Bayes classifier
   */
  public void newClassifier()
  {
    //Iterators
    int i;

    //Data strucutures to go through values and vars
    NodeList    nodeList   ;
    LinkList    linkList   ;
 
    // Iniatializing variables.
    nodeList = classifier.getNodeList().copy();
    linkList = classifier.getLinkList().copy();  

    // Initialize the classifier (Bnet) except for the Potential Tables which
    // will be obtained by learning().
    classifier = new Bnet();
    classifier.setNodeList(nodeList);
    classifier.setLinkList(linkList);

    // add the RelationList to the classifier
    Iterator nodesIt = nodeList.getNodes().iterator();
    for(i=0;nodesIt.hasNext();i++) // numberOfVariables+1 in order to inc slude the last element (classNode)
    {
      classifier.addRelation((FiniteStates)nodesIt.next());     
    }
    // add the default states
    Vector defaultStates = new Vector();
    defaultStates.addElement(Bnet.ABSENT);
    defaultStates.addElement(Bnet.PRESENT);
    classifier.setFSDefaultStates(defaultStates);
  }// end newClassifier()
  
  /**
   * Gets the classifier (Bnet with Naive-Bayes structure).
   * @return Bnet classifier.
   */
  public Bnet getClassifier()
  {
    return classifier;
  }//end getClassifier()

  /**
   * Learn the conditional probability tables for the Bnet Naive-Bayes classifier
   * from the dataset specified in constructor method via EM algorithm
   * @param laplaceCorrection if <code>laplaceCorrection</code> is <code>true</code> 
   * the laplace correction is used when stimating the conditional probabily tables.
   */
  public abstract double learning();

  /**
 * Set the probabilities from the conditional probability tables inside every 
 * relation into a new order of the class values. 
 * For example if we have two possible values of the classNode C0, and C1 we can 
 * exchange the values of the probabilities for each class value like this:
 * <code>
 * IncompleteNaiveBayes inb;
 * Bnet                 net;
 *  ......
 * Vector order = new Vector();
 * order.addElement(new Integer(1));
 * order.addElement(new Integer(0));
 * inb.rearrageClassProbabilities(order)
 * net = inb.getClassifier();
 * 
 * @param order Vector of Integer with the new order for the class values.
 */

  public void rearrangeClassProbabilities(Vector order)
  {
  int i;
  int j;
  //int nOfClusters = ((FiniteStates)classifier.getNodeList().lastElement()).getNumStates();
  // Check if the elements of the vector correspond to class value
  if(order.size() > numberOfClusters)
  {
    System.out.println("Too much values into order vector in rearrangeClassProbabilities");
    System.exit(-1);
  }
  Iterator orderIt = order.iterator();
  while(orderIt.hasNext())
  {
    int val = ((Integer)orderIt.next()).intValue();
    if(val >= numberOfClusters)
    { // This is no a possible value for the class
      System.out.println("No a possible value for the class found in rearrangeClassProbabilities");
      System.exit(-1);
    }
    
  }

  Iterator relationIt = classifier.getRelationList().iterator();
  
  for(i=0;i<numberOfVariables;i++)
  {
    PotentialTable pt = ((PotentialTable)((Relation)relationIt.next()).getValues());
    double [] oldPotentials = pt.getValues();
    double [] newPotentials = new double [oldPotentials.length];

    int nStatesOfVar = ((FiniteStates)pt.getVariables().firstElement()).getNumStates();
    int nStatesOfParents = oldPotentials.length / nStatesOfVar;

    if(nStatesOfParents == numberOfClusters)
    {
      int index = 0;
      for(j=0;j<nStatesOfVar;j++)
      {
        orderIt = order.iterator();
        while(orderIt.hasNext())
        {
          int val = ((Integer)orderIt.next()).intValue();
          newPotentials[index] = oldPotentials[j*numberOfClusters + val];
          index ++;
        }
      }
    }
    else
    {
      newPotentials = oldPotentials;
    }
    pt.setValues(newPotentials);
  }

  //ClassNode is a special node and is needed to be rearranged in a special way.
  PotentialTable pt = ((PotentialTable)((Relation)relationIt.next()).getValues());
  double [] oldPotentials = pt.getValues();
  double [] newPotentials = new double [oldPotentials.length];
  int index = 0;
  orderIt = order.iterator();
  while(orderIt.hasNext())
  {
    int val = ((Integer)orderIt.next()).intValue();
    newPotentials[index] = oldPotentials[val];
    index ++;
  }
  pt.setValues(newPotentials);
  
  }
}
