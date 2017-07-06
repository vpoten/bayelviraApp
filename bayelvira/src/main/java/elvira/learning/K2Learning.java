package elvira.learning;

import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * K2Learning.java
 *
 *
 * Created: Tue Nov 23 10:08:13 1999
 *
 * @author P. Elvira
 * @version 1.0
 */

public class K2Learning extends Learning {
    
    NodeList nodesSorted;  // A List of Nodes sorted.
    DataBaseCases input;   // The cases for the input algorithm.
    int numberMaxOfParents;// The number of maximal parents for each node.
    Metrics metric;      // The decomposable metric for scoring.
    int begining;        // Index for the input nodes, nodei<begining are considered as root nodes.
    double[] Ffitness;

 public static void main(String args[]) throws ParseException, IOException { 
    
     Bnet baprend;
     FileWriter f2;
     double time;
     NodeList nodesSorted;
     Metrics met;
     boolean var=false;
      if(args.length < 4){
	  System.out.println("too few arguments: Usage: file.dbc numberOfMaxParents file.elv n.cases BIC,K2 [var. sorted (file.var)] [file.elv]");
	  System.exit(0);
      }
      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      cases.setNumberOfCases(Integer.valueOf(args[3]).intValue());
      if(args[4].equals("BIC")) met = (Metrics) new BICMetrics(cases);
      else met = (Metrics) new K2Metrics(cases);
      if(args.length > 5){
	if(args[5].indexOf(".var") != -1){
	   FileInputStream fvar = new FileInputStream(args[5]);
	   nodesSorted = new NodeList(fvar,cases.getNodeList());
	   var = true;
        }else nodesSorted = cases.getNodeList();
      }else nodesSorted = cases.getNodeList();
      System.out.println("Nodes: "+nodesSorted.toString2());
      //try{System.in.read();}catch(IOException e){};
      K2Learning outputNet1 = new K2Learning(cases,nodesSorted,Integer.valueOf(args[1]).intValue(),met);
      Date date = new Date();
      time = (double) date.getTime();           
      outputNet1.learning();
      date = new Date();
      time = (((double) date.getTime()) - time)/1000;
      System.out.println("Tiempo consumido: "+time);
      DELearning outputNet3 = new DELearning(cases,outputNet1.getOutput());
      outputNet3.learning();
      double d = cases.getDivergenceKL(outputNet3.getOutput());
      System.out.println("Divergencia de KL = "+d);
      System.out.println("Estadisticos evaluados: "+met.getTotalStEval());
      System.out.println("Total de estadisticos: "+met.getTotalSt());
      System.out.println("Numero medio de var en St: "+met.getAverageNVars());
            
      f2 = new FileWriter(args[2]);
      baprend = (Bnet)outputNet1.getOutput();
      baprend.saveBnet(f2);
      f2.close();

      if(args.length > 5){
	  FileInputStream fnet = null;
	  if(var)
	      fnet = new FileInputStream(args[6]);
	  else  fnet = new FileInputStream(args[5]);
	  Bnet net = new Bnet(fnet);
          double d2 = cases.getDivergenceKL(net);
          System.out.println("Divergencia real: "+(d2-d));
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

    public K2Learning(){
	setNodesSorted(null);
	setInput(null);
	setMetric(null);
	Ffitness = null;
    }

    /**
     * Constructor for the Learning Algorithm K2.
     * @param DataBaseCases. cases. The input of algorithm.
     * @param NodeList nodes. The list of nodes sorted.
     * @param int nMaxOfParents. The max number of parents for each node.
     */

    public K2Learning(DataBaseCases cases,NodeList nodes,int nMaxParents){
	int i;
	Node nodei,nodex;

	nodesSorted = new NodeList();
	for(i=0 ; i< nodes.size(); i++){
	    nodei = (Node) nodes.elementAt(i);
	    nodex = (Node) cases.getNodeList().getNode(nodei.getName());
	    nodex.setParents(new LinkList());
	    nodex.setChildren(new LinkList());
	    nodex.setSiblings(new LinkList());
	    nodesSorted.insertNode(nodex);
	}
	
	input = cases;
	numberMaxOfParents = nMaxParents;
	begining = 1;
	metric = (Metrics) new K2Metrics(cases);
	Ffitness = new double[nodesSorted.size()];
    }	

   /**
     * Constructor for the Learning Algorithm K2.
     * @param DataBaseCases. cases. The cases for the input. The variables are 
     * considered sorted as they are listed in the data base.
     * @param int nMaxOfParents. The max number of parents for each node.
     */

    public K2Learning(DataBaseCases cases,int nMaxParents){
	int i;
	Node nodei;

	nodesSorted = new NodeList();
	for(i=0 ; i< cases.getNodeList().size(); i++){
	    nodei = (Node) cases.getNodeList().elementAt(i);
	    nodei.setParents(new LinkList());
	    nodei.setChildren(new LinkList());
	    nodei.setSiblings(new LinkList());
	    nodesSorted.insertNode(nodei);
	}
	
	input = cases;
	numberMaxOfParents = nMaxParents;
	begining = 1;
	metric = (Metrics) new K2Metrics(cases);
	Ffitness = new double[nodesSorted.size()];
    }

   /**
     * Constructor for the Learning Algorithm K2.
     * @param DataBaseCases. cases. The input of algorithm.
     * @param NodeList nodes. The list of nodes sorted.
     * @param int nMaxOfParents. The max number of parents for each node.
     */

    public K2Learning(DataBaseCases cases,NodeList nodes,int nMaxParents,Metrics met){
	int i;
	Node nodei,nodex;

	nodesSorted = new NodeList();
	for(i=0 ; i< nodes.size(); i++){
	    nodei = (Node) nodes.elementAt(i);
	    nodex = (Node) cases.getNodeList().getNode(nodei.getName());
	    nodex.setParents(new LinkList());
	    nodex.setChildren(new LinkList());
	    nodex.setSiblings(new LinkList());
	    nodesSorted.insertNode(nodex);
	}
	
	input = cases;
	numberMaxOfParents = nMaxParents;
	begining = 1;
	metric = met;
	Ffitness = new double[nodesSorted.size()];
    }	

   /**
     * Constructor for the Learning Algorithm K2.
     * @param DataBaseCases. cases. The cases for the input. The variables are 
     * considered sorted as they are listed in the data base.
     * @param int nMaxOfParents. The max number of parents for each node.
     */

    public K2Learning(DataBaseCases cases,int nMaxParents,Metrics met){
	int i;
	Node nodei;

	nodesSorted = new NodeList();
	for(i=0 ; i< cases.getNodeList().size(); i++){
	    nodei = (Node) cases.getNodeList().elementAt(i);
	    nodei.setParents(new LinkList());
	    nodei.setChildren(new LinkList());
	    nodei.setSiblings(new LinkList());
	    nodesSorted.insertNode(nodei);
	}
	
	input = cases;
	numberMaxOfParents = nMaxParents;
	begining = 1;
	metric = met;
	Ffitness = new double[nodesSorted.size()];

    }
    
    /**
     * This methods implements the K2 algorithm.
     */

    public void  learning(){

	int i,j;
        double suma;
	FiniteStates nodeXi,nodeZ;
	NodeList PaXi,vars;
	double fitness,fitnessNew;
	boolean OkToProceed;
	LinkList links;
	Link newLink;
	
	links = new LinkList();
	vars = new NodeList();
        vars.insertNode(nodesSorted.elementAt(0));
	int posNode = input.getNodeList().getId(nodesSorted.elementAt(0).getName());
        suma = metric.score(vars);
	Ffitness[posNode] = suma;
	for(i=begining; i< nodesSorted.size();i++){
	    nodeXi = (FiniteStates)nodesSorted.elementAt(i);
	    posNode = input.getNodeList().getId(nodeXi.getName());
	    System.out.print(nodeXi.getName()+" ");
	    PaXi = new NodeList();
	    vars = new NodeList();
	    vars.insertNode(nodeXi);
	    fitness = metric.score(vars);
	    //System.out.println(fitness);
	    OkToProceed = true;
	    while(OkToProceed && (PaXi.size()<=numberMaxOfParents)){
		nodeZ = maxScore(nodeXi,PaXi,i);
		if(nodeZ!=null){
		    vars = new NodeList();
		    vars.insertNode(nodeXi);
		    if(PaXi.size()>0)
			vars.join(PaXi);
		    vars.insertNode(nodeZ);
		    fitnessNew = metric.score(vars);
		    //System.out.println(fitnessNew);
		    if(fitnessNew > fitness){
			fitness = fitnessNew;
			PaXi.insertNode(nodeZ);
		    }
		    else OkToProceed = false;
		}
		else OkToProceed = false;
	    }
            vars = new NodeList();
            vars.insertNode(nodeXi);
            vars.join(PaXi);
	    double valor = metric.score(vars);
	    Ffitness[posNode] = valor;
            suma+=valor;
	    for(j=0 ; j<PaXi.size();j++){
		newLink = new Link(PaXi.elementAt(j),nodeXi);
		links.insertLink(newLink);
	    }
	    //System.out.println(PaXi.toString2());
	    //try{
	    //	System.in.read();
	    //}catch (IOException e){};
	}
        System.out.println("El fitness de la red es: "+suma);
	setOutput(new Bnet());
	for(i=0 ; i< nodesSorted.size();i++)
	    try {
	       getOutput().addNode(nodesSorted.elementAt(i));
	    } catch (InvalidEditException iee) {};
	for(i=0 ; i< links.size();i++){
	    newLink = (Link) links.elementAt(i);
	    try {
	       getOutput().createLink(newLink.getTail(),newLink.getHead());
	    } catch (InvalidEditException iee) {};	    
	}
	    
    }

    /**
     * This methos is private. It is used for searching the parent for the node
     * nodei that maximize the score metric.
     * @param FiniteStates nodei. the node.
     * @param NodeList pa. The actual parents set for the node i.
     * @param int index. The position for the node i.
     * @return FiniteStates. The maximal node.
     */

    private FiniteStates maxScore(FiniteStates nodei,NodeList pa,int index){

	int i;
	FiniteStates node, nodeZ;
	NodeList vars;
	double val;
	double max = (-1.0/0.0);

	nodeZ=null;

	for(i=0; i<index; i++){
	    node = (FiniteStates)nodesSorted.elementAt(i);
	    if(pa.getId(node) == -1){
		vars = new NodeList();
		vars.insertNode(nodei);
		vars.join(pa);
		vars.insertNode(node);
		val = metric.score(vars);
		if(val > max){
		    max = val;
		    nodeZ = node;
		}
	    }
	}
	return nodeZ;
    }

    public void setNodesSorted(NodeList nl){
	nodesSorted = nl;
    }
    public NodeList getNodesSorted(){
	return nodesSorted;
    }
    public void setInput(DataBaseCases db){
	input = db;
    }
    public DataBaseCases getInput(){
	return input;
    }
    public Metrics getMetric(){
	return metric;
    }
    public void setMetric(Metrics metric){
	this.metric = metric;
    }
    public void setMaxOfParents(int max){
	numberMaxOfParents = max;
    }
    public int getMaxOfParents(){
	return numberMaxOfParents;
    }
    public void setBegining(int b){
	begining = b;
    }
    public int getBegining(){
	return begining;
    }

    public double[] getFitness(){
	return Ffitness;
    }

} // K2Learning







