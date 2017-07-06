package elvira.learning;

import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * BBenedict.java
 * Implements the basic Benedict learning algorithm.
 *
 * Created: Tue Nov 23 10:08:13 1999
 *
 * @author P. Elvira
 * @version 1.0
 * @since 25/9/2000
 */

public class BBenedict extends Learning {

/**
 * A sorted list of nodes.
 */
NodeList nodesSorted;

/**
 * The data base of cases from which learning will be performed.
 */
DataBaseCases input;

/**
 * Thresholds for stopping the learning process.
 */
double threshold1;
double threshold2;

/**
 * The metric for scoring bnets.
 */
KLMetrics metric;

/**
 * Index for the input nodes, nodes < begining are considered as root nodes.
 */
int begining;


/**
 * For performing experiments from the command line.
 */

public static void main(String args[]) throws ParseException, IOException { 
      
  NodeList nodes;
  Bnet baprend;
  FileWriter f2;
  
  if (args.length < 2) {
    System.out.println("too few arguments: Usage: file.dbc file.elv [file.elv]");
    System.exit(0);
  }
  FileInputStream f = new FileInputStream(args[0]);
  
  DataBaseCases cases = new DataBaseCases(f);
  BBenedict outputNet1 = new BBenedict(cases,0.01,0.01);
  
  outputNet1.learning();
  
  f2 = new FileWriter(args[1]);
  baprend = (Bnet)outputNet1.getOutput();
  baprend.saveBnet(f2);
  f2.close();
  
  if (args[2] != null) {
    FileInputStream fnet = new FileInputStream(args[2]);
    Bnet net = new Bnet(fnet);
    LinkList addel[] = new LinkList[3];
    addel = outputNet1.compareOutput(net);
    System.out.print("\nNumero de arcos añadidos: "+addel[0].size());
    System.out.print(addel[0].toString());
    System.out.print("\nNumero de arcos borrados: "+addel[1].size());
    System.out.print(addel[1].toString());
    System.out.println("\nNumero de arcos mal orientados: "+addel[2].size());
    System.out.print(addel[2].toString());
  }
}  


/**
 * Constructs an empty object.
 */

public BBenedict() {
  
  setInput(null);
  setNodesSorted(null);
  setMetric(null);
}


/** 
 * Constructor for the Learning Algoritm Basic Benedict.
 * @param cases The cases for the input.
 * @param nodes. A List of nodes sorted.
 * @param u1. For stoping the learning process.
 * @param u2. For stoping the learning process.
 */

public BBenedict(DataBaseCases cases, NodeList nodes, double u1, double u2) {
  
  Bnet bLearned;
  Node nodei, nodex;
  int i;
  nodesSorted = new NodeList();
  
  for (i=0 ; i< nodes.size() ; i++) {
    nodei = (Node) nodes.elementAt(i);
    nodex = (Node) cases.getNodeList().getNode(nodei.getName());
    nodesSorted.insertNode(nodex);
  }
  
  input = cases;
  threshold1 = u1;
  threshold2 = u2;
  begining = 1;	
  metric = new KLMetrics(cases);
}


/** 
 * Constructor for the Learning Algoritm Basic Benedict.
 * @param cases the cases for the input. The variables are 
 * considered sorted as they are listed in the data base.
 * @param double u1. For stoping the learning process.
 * @param double u2. For stoping the learning process.
 */

public BBenedict(DataBaseCases cases, double u1, double u2) {
  
  Bnet bLearned;
  Node nodei;
  int i;
  nodesSorted = new NodeList();
  
  for (i=0 ; i< cases.getVariables().size() ; i++) {
    nodei = (Node) cases.getVariables().elementAt(i);
    nodesSorted.insertNode(nodei);
  }
  
  input = cases;
  threshold1 = u1;
  threshold2 = u2;
  begining = 1;	
  metric = new KLMetrics(cases);
}


/**
 * This method computes the learning process.
 */

public void learning() {
  
  int i, j, h;
  FiniteStates nodeXi, nodeXj;
  NodeList vars, minDsep;
  double fitness, fitnessOld, min;
  LinkList links;
  Link newLink, linkToInsert = null;
  Bnet currentBnet;
  Graph currentGraph;
  
  currentGraph = new Graph();
  currentBnet = new Bnet();
  
  for (i=0 ; i< nodesSorted.size() ; i++) {
    nodeXi = (FiniteStates)nodesSorted.elementAt(i);
    try{
      currentBnet.addNode(nodeXi);
      currentGraph.addNode(nodeXi);
    }catch (InvalidEditException e){};
  }
  fitness = metric.score(currentBnet);
  System.out.println(fitness);
  links = new LinkList();
  
  for (i=begining ; i< nodesSorted.size() ; i++) {
    nodeXi = (FiniteStates)nodesSorted.elementAt(i);
    for (j=0 ; j<i ; j++) {
      nodeXj = (FiniteStates)nodesSorted.elementAt(j);
      newLink = new Link(nodeXj,nodeXi);
      links.insertLink(newLink);
    }
  }
  
  min = fitness;
  fitnessOld = (new Double(1.0/0.0)).doubleValue();
  System.out.println(fitnessOld);
  
  while (!stop(fitness,fitnessOld)) {
    fitnessOld = fitness;
    for (h=0 ; h < links.size() ; h++) {
      newLink = (Link)links.elementAt(h);
      System.out.println(newLink.toString());
      try {
	currentGraph.createLink(newLink.getTail(),newLink.getHead());
      } catch (InvalidEditException iee) {};
      System.out.println("Actual grafo es: "+currentGraph.getNodeList().toString2()+"  "+currentGraph.getLinkList().toString());
      fitness = 0.0;
      for (i=0 ; i< nodesSorted.size() ; i++) {
	nodeXi = (FiniteStates)nodesSorted.elementAt(i);
	for (j=0 ; j<i ; j++) {
	  nodeXj = (FiniteStates)nodesSorted.elementAt(j);
	  if (currentGraph.parents(nodeXi).getId(nodeXj) == -1) {
	    minDsep = currentGraph.minimunDSeparatingSet((Node)nodeXi,
							 (Node)nodeXj);
	    System.out.println(nodeXi.getName()+" , "+
			       nodeXj.getName()+" , dsep: "+
			       minDsep.toString2());
	    vars = new NodeList();
	    vars.insertNode(nodeXi);
	    vars.insertNode(nodeXj);
	    vars.join(minDsep);
	    fitness += metric.score(vars);
	    System.out.println(fitness);
	  }
	}
      }
      System.out.println(fitness+"   "+min);
      if (fitness < min) {
	min = fitness;
	linkToInsert = newLink;
	System.out.println("link minima: "+linkToInsert.toString());
      }
      try {
	Link l = currentGraph.getLink(newLink.getTail(),newLink.getHead()); 
	currentGraph.removeLink(l);
      } catch (InvalidEditException iee) {};
    }
    fitness = min;
    try {
      currentGraph.createLink(linkToInsert.getTail(),linkToInsert.getHead());
    } catch (InvalidEditException iee) {};
    links.removeLink(linkToInsert);
  }
  
  setOutput(currentBnet);
  
  for (i=0 ; i< currentGraph.getLinkList().size() ; i++) {
    newLink = (Link) currentGraph.getLinkList().elementAt(i);
    try {
      getOutput().createLink(newLink.getTail(),newLink.getHead());
    } catch (InvalidEditException iee) {};	    
  }
  
}


/**
 * This method is private. For stoping the learning process.
 * @param fitness fitness for the current bnet.
 * @param fitnessOld fitness for the previous bnet.
 */

private boolean stop(double fitness, double fitnessOld) {
  
  if (fitness < threshold1)
    return true;
  if ((Math.abs(fitness-fitnessOld)) < threshold2)
    return true;
  return false;
}


/**
 * Sets the input data file.
 * @param data the input data file, containing the cases.
 */

public void setInput(DataBaseCases data) {
  
  this.input = data;
}


/**
 * Sets the ordered list of nodes.
 * @param ndl a list of nodes.
 */

public void setNodesSorted(NodeList ndl) {
  
  nodesSorted = ndl;
}


/**
 * Sets the metric.
 * @param metric a metric.
 */

public void setMetric(KLMetrics metric) {
  
  this.metric = metric;
}

} // End of class.