package elvira.learning;

import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * BICLearning.java
 * Implements BIC learning algorithm.
 *
 * Created: Tue Nov 23 10:08:13 1999
 *
 * @author P. Elvira
 * @version 1.0
 * @since 25/9/2000
 */

public class BICLearning extends Learning {

/**
 * A sorted list of nodes.
 */
NodeList AllNodes;

/**
 * The input cases.
 */
DataBaseCases input;

/**
 * The maximum number of parents for each node.
 */
int numberMaxOfParents;

/**
 * The BIC metric for scoring.
 */
BICMetrics metric;

/**
 * The <code>Bnet</code> structure, to be learned.
 */
Bnet baprend;


/**
 * Method to perated from the command line.
 */

public static void main(String args[]) throws ParseException, IOException { 
								
  FileWriter f2;
  
  if (args.length < 2) { // By now, max num of parents is kept
    System.out.println("too few arguments: Usage: file.dbc numMaxparents file.elv [file.elv]");
    System.exit(0);
  }
  
  FileInputStream f = new FileInputStream(args[0]);
  
  DataBaseCases cases = new DataBaseCases(f);
  BICLearning outputNet1 = new BICLearning(cases);
  
  outputNet1.learning();
  
  f2 = new FileWriter(args[1]);
  outputNet1.baprend = (Bnet) outputNet1.getOutput();
  outputNet1.baprend.saveBnet(f2);
  f2.close();
}


/**
 * Constructs an empty object.
 */

public BICLearning() {
  
  setAllNodes(null);
  setInput(null);
  setMetric(null);
}


/**
 * Constructor for the Learning Algorithm BIC.
 * @param cases the cases for the input. The variables are 
 * considered sorted as they are listed in the data base.
 */

public BICLearning(DataBaseCases cases) {
  
  int i;
  Node nodei;
  
  AllNodes = new NodeList();
  for (i=0 ; i< cases.getNodeList().size() ; i++) {
    nodei = (Node) cases.getNodeList().elementAt(i);
    AllNodes.insertNode(nodei);
  }
  
  baprend = (Bnet) cases;
  input = cases;
  metric = new BICMetrics(cases);
}


/**
 * Perfomrs the learning.
 */

public void learning() {
  
  int i, j, h, insertedLinks,maxLinks;
  FiniteStates nodeXi, nodeXj;
  NodeList vars, minDsep;
  double fitness, fitnessOld, min;
  LinkList links;
  Link newLink, linkToInsert = null;
  Bnet currentBnet;
  Graph currentGraph;
  
  currentGraph = new Graph();
  currentBnet = new Bnet();

  for (i=0 ; i< AllNodes.size() ; i++) {
    nodeXi = (FiniteStates)AllNodes.elementAt(i);
    try{
      currentBnet.addNode(nodeXi);
      currentGraph.addNode(nodeXi);
    }catch (InvalidEditException e){};
  }

  links = new LinkList();
  for (i=0 ; i< AllNodes.size() ; i++) {
    nodeXi = (FiniteStates)AllNodes.elementAt(i);
    for (j=0 ; j< AllNodes.size() ; j++) {
      if (j == i)
	continue;
      nodeXj = (FiniteStates)AllNodes.elementAt(j);
      newLink = new Link(nodeXj,nodeXi);
      links.insertLink(newLink);
    }
  }
  
  min = 0.000001;
  
  insertedLinks = 0;
  maxLinks = links.size();
  while(min>0 && (insertedLinks<maxLinks)) {
    
    min = 0.0;
    
    for (h=0 ; h < links.size() ; h++) {
      newLink = (Link)links.elementAt(h);
      System.out.println("Trial with: "+newLink.toString());
      if (!currentBnet.hasCycle(newLink.getTail(),newLink.getHead())) {
	try {
	  currentGraph.createLink(newLink.getTail(),newLink.getHead());
	  currentBnet.createLink(newLink.getTail(),newLink.getHead());
	} catch (InvalidEditException iee) {};
      }
      else
	continue;
      
      fitness = metric.score(currentBnet);
      
      if (fitness > min) {
	min = fitness;
	linkToInsert = newLink;
      }
      try {
	Link l = currentGraph.getLink(newLink.getTail(),newLink.getHead()); 
	currentGraph.removeLink(l);
	currentBnet.removeLink(l);
      } catch (InvalidEditException iee) {};
    } // for
    
    fitness = min;
    System.out.println("Al final: Link a insertar: "+linkToInsert.toString());
    System.out.println("Valor "+min);
    if (min > 0.0) {
      try {
	currentGraph.createLink(linkToInsert.getTail(),linkToInsert.getHead());
	currentBnet.createLink(linkToInsert.getTail(),linkToInsert.getHead());
      } catch (InvalidEditException iee) {};
      insertedLinks++;
      links.removeLink(linkToInsert);
    }
  } // While
  
  setOutput(currentBnet);
}


/**
 * This method is private. For stoping the learning process.
 * @param fitness fitness for the current bnet.
 * @param fitnessOld fitness for the previous bnet.
 */

private boolean stop(double fitness, double fitnessOld) {
  
  System.out.println("Fitness nuevo: "+fitness+"Valor a superar: "+fitnessOld);
  
  if (fitness >= fitnessOld)
    return true;
  return false;
}


/**
 * sets the ordered list of nodes.
 * @param nl a list of nodes.
 */

public void setAllNodes(NodeList nl) {
  
  AllNodes = nl;
}


/**
 * Sets the input cases.
 * @param db a data base of cases.
 */

public void setInput(DataBaseCases db) {
  
  input = db;
}


/**
 * Sets the metric.
 * @param a metric.
 */

public void setMetric(BICMetrics metric) {
  
  this.metric = metric;
}

} // End of class.