package elvira.learning;

import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * K2SNOPT.java
 *
 *
 * Created: Tue Nov 23 10:08:13 1999
 *
 * @author P. Elvira
 * @version 1.0
 */

public class K2SNOPT extends Learning {
    
    NodeList nodes;  // A List of Nodes.
    NodeList nodesSorted;
    DataBaseCases input;   // The cases for the input algorithm.
    int numberMaxOfParents;// The number of maximal parents for each node.
    Metrics metric;      // The metric for scoring.
    int begining; // nodei < begining are considered as root nodes.
    Graph dag;
    int nLookaHead = 1;
    double beta = 0.0;
    int nExpl = 1;

 public static void main(String args[]) throws ParseException, IOException { 
    
     Bnet baprend;
     FileWriter f2;
     double time;
     Metrics met = null;
      if(args.length < 4){
	  System.out.println("too few arguments: Usage: file.dbc numberOfMaxParents file.elv n.cases BIC,K2 nLookAHead beta nExpl [file.elv]");
	  System.exit(0);
      }
      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      cases.setNumberOfCases(Integer.valueOf(args[3]).intValue());
      if(args[4].equals("BIC"))
	  met = (Metrics) new BICMetrics(cases);
      else met = (Metrics) new K2Metrics(cases);
      K2SNOPT outputNet1 = new K2SNOPT(cases,Integer.valueOf(args[1]).
				       intValue(),met,
		  		       Integer.valueOf(args[5]).intValue(),
				       Double.valueOf(args[6]).doubleValue(),
				       Integer.valueOf(args[7]).intValue());
      Date date = new Date();
      time = (double) date.getTime();           
      outputNet1.learning();
      date = new Date();
      time = (((double) date.getTime()) - time)/1000;
      System.out.println("Tiempo consumido: "+time);
      System.out.println("Ahora voy a aprender con K2 Alg. con el orden obtenido");
      K2Learning outputNet2 = new K2Learning(cases,outputNet1.getNodesSorted(),5);
      outputNet2.learning();
      DELearning outputNet3 = new DELearning(cases,outputNet2.getOutput());
      outputNet3.learning();
      double d = cases.getDivergenceKL(outputNet3.getOutput());
      System.out.println("Divergencia de KL = "+d);
            
      f2 = new FileWriter(args[2]);
      baprend = (Bnet)outputNet1.getOutput();
      baprend.saveBnet(f2);
      f2.close();

      if(args.length > 8){
	  FileInputStream fnet = null;
	  fnet = new FileInputStream(args[8]);
	  Bnet net = new Bnet(fnet);
          double d2 = cases.getDivergenceKL(net);
          System.out.println("Divergencia real: "+(d2-d));
	  LinkList addel[] = new LinkList[3];
	  addel = outputNet2.compareOutput(net);
	  System.out.print("\nNumero de arcos añadidos: "+addel[0].size());
	  System.out.print(addel[0].toString());
	  System.out.print("\nNumero de arcos borrados: "+addel[1].size());
	  System.out.print(addel[1].toString());
	  System.out.println("\nNumero de arcos mal orientados: "+addel[2].size());
	  System.out.print(addel[2].toString());
      }  
    
   }  

    public K2SNOPT(){
	
    }

   /**
     * Constructor for the Learning Algorithm K2.
     * @param DataBaseCases. cases. The cases for the input. The variables are 
     * considered sorted as they are listed in the data base.
     * @param int nMaxOfParents. The max number of parents for each node.
     */

    public K2SNOPT(DataBaseCases cases,int nMaxParents, Metrics met,int nLook,
		   double beta,int nExpl){
	int i;
	Node nodei;
	dag = new Graph();
	nodes = new NodeList();
	for(i=0 ; i< cases.getNodeList().size(); i++){
	    nodei = (Node) cases.getNodeList().elementAt(i);
	    nodei.setParents(new LinkList());
	    nodei.setChildren(new LinkList());
            nodei.setSiblings(new LinkList());
	    nodes.insertNode(nodei);
	    try{
              dag.addNode(nodei);
            }catch(InvalidEditException iee){};
	}
	
	input = cases;
	numberMaxOfParents = nMaxParents;
	begining = 1;
	metric = met;
	nLookaHead = nLook;
	this.beta = beta;
	this.nExpl = nExpl;
    }
    
    /**
     * This methods implements the algorithm .
     */

    public void  learning(){
	int i;
        double suma;
	FiniteStates nodeXi;
	NodeList PaXi,vars;
	double fitness;
	nodesSorted = new NodeList();
	NodeList nodesToVisit = nodes.copy();
	
	System.out.println("Empiezo a aprender con profundidad: "+nLookaHead);
	System.out.println("Nodos a explorar en cada etapa: "+nExpl);
	PaXi = new NodeList();
	fitness = nextNode(nodesToVisit,nodesSorted,PaXi,1,(-1.0/0.0),0.0);
	nodeXi = (FiniteStates)nodesSorted.elementAt(nodesSorted.size()-1);
	System.out.print(nodeXi.getName()+", ");
	//try{System.in.read();}catch(IOException e){};
	vars = new NodeList();
	nodeXi = (FiniteStates)input.getNodeList().getNode(nodeXi.getName());
        vars.insertNode(nodeXi);
        suma = metric.score(vars);

	while(nodesToVisit.size() > 0){
	    PaXi = new NodeList();
	    fitness=nextNode(nodesToVisit,nodesSorted,PaXi,1,(-1.0/0.0),0.0);
	    //try{System.in.read();}catch(IOException e){};
	    //nodesToVisit.removeNode(nodeXi);
            nodeXi = (FiniteStates)nodesSorted.elementAt(nodesSorted.size()-1);
	    vars = new NodeList();
	    vars.insertNode(nodeXi);
	    vars.join(PaXi);
	    vars = input.getNodeList().intersectionNames(vars).sortNames(vars);
	    fitness = metric.score(vars);
	    suma+=fitness;
	    //System.out.println(fitness);
	    System.out.print(nodeXi.getName()+", ");//+PaXi.toString2());
	    createParents(nodeXi,PaXi);
	}

        System.out.println("El fitness de la red es: "+suma);
	setOutput(new Bnet());
	for(i=0 ; i< nodes.size(); i++){
	    try{
		getOutput().addNode(nodes.elementAt(i));
	    }catch(InvalidEditException iee){};
	}
	for(i=0 ; i< dag.getLinkList().size(); i++){
	    Link link = dag.getLinkList().elementAt(i);
	    try{
		getOutput().createLink(link.getTail(),link.getHead());
	    }catch(InvalidEditException iee){};
	}
    }


    private FiniteStates searchFirts(){

	FiniteStates nodeR = null;
	double max = (-1.0/0.0);
	//System.out.println("Buscando el primer nodo...");
	for(int i=0 ; i< nodes.size(); i++){
	   FiniteStates node = (FiniteStates) nodes.elementAt(i);
	   NodeList vars = new NodeList();
	   vars.insertNode(node);
	   double fit = metric.score(vars);
	   if(fit > max){
	       max = fit;
	       nodeR = node;
	   }
	}
	//System.out.println("Primer nodo buscado ...... ");
        return nodeR;
    }


    private void createParents(FiniteStates nodeXi, NodeList nodesPa){

        Node n = nodes.getNode(nodeXi.getName());
        for(int i=0 ; i< nodesPa.size() ; i++){
            Node nodepa = nodes.getNode(nodesPa.elementAt(i).getName());
            try{
                dag.createLink(nodepa,n);
            }catch(InvalidEditException iee){};
        }
    }


    private double nextNode(NodeList nodesToVisit, NodeList nodesSorted,
			    NodeList nodesPa,int depth,double max,double cVal){
	int i,posi;
	double val = 0.0;
	double valAux = 0.0;
	Node nodeR = null;
	NodeList nodesR = null;
	NodeList nodesAux, nodesAux2, nodesSortedAux, nodesToVisitAux;
	boolean changed = false;
	double expl[] = new double[nExpl+1];
	int explNode[] = new int[nExpl+1];
	NodeList explParents[] = new NodeList[nExpl+1];
	int v;
	if((depth > nLookaHead)||(nodesToVisit.size()==0)) return (cVal);
	
	for(v=0 ; v <= nExpl ; v++){ 
	    expl[v] = -1.0/0.0;
	    explNode[v] = -1;
	    explParents[v] = new NodeList();
	}
 
	changed = false;
	
	for(i=0 ; i< nodesToVisit.size() ; i++){
            posi = nodes.getId(nodesToVisit.elementAt(i));
	    nodesAux = new NodeList();
	    val = heuristic(nodesAux,posi,nodesSorted);
	    val+=cVal;
	    v=nExpl;
	    while((v>=1)&&(val>expl[v-1])){
		expl[v]=expl[v-1];
		explNode[v]=explNode[v-1];
		explParents[v]=explParents[v-1];
		v--;
	    }
	    expl[v]=val;
	    explNode[v]=posi;
	    explParents[v]=nodesAux.copy();
	}
	//if(depth>=1){
	//    System.out.println("Profundidad: "+depth);
	//    for(v=0 ;(v<nExpl)&&(v<nodesToVisit.size()) ; v++){
	//       System.out.print(" N:"+nodes.elementAt(explNode[v]).getName()+" V:"+expl[v]+" P:");
	//       System.out.print(explParents[v].toString2());
	//       try{System.in.read();}catch(IOException e){};
	//    }
	//}

	for(i=0 ; ((i< nExpl)&&(i<nodesToVisit.size())) ; i++){
	    //System.out.print("Prof: "+depth+" i = "+i+" Nodo: "+nodes.elementAt(explNode[i]).getName());
	    if(max > (-1.0/0.0)){
		double avg = (double)(max/nLookaHead);
		if(avg < 0.0){
		    avg = Math.pow(Math.abs(avg),beta);
                    avg-=(1.0-beta);
                    avg*=(double)(nLookaHead-depth);
		    avg*=(-1.0);
		}else{
		    avg = Math.pow(avg,beta);
		    avg-=(1.0-beta);
                    avg*=(double)(nLookaHead-depth);
		}
		valAux = expl[i] + avg; 
	    }else valAux = expl[i];
	    //System.out.println(" valAux: "+valAux);
	    if(valAux > max){
		nodesSortedAux = nodesSorted.copy();
		nodesToVisitAux = nodesToVisit.copy();
		nodesSortedAux.insertNode(nodes.elementAt(explNode[i]));
		nodesToVisitAux.removeNode(nodes.elementAt(explNode[i]));
		nodesAux2 = new NodeList();
		val=nextNode(nodesToVisitAux,nodesSortedAux,nodesAux2,depth+1,
			     max,expl[i]);
		//if(depth >= 1){
		//    System.out.println("Profund: "+depth);
		//    System.out.print("\nNodo: "+nodes.elementAt(explNode[i]).getName());
		//    System.out.print(" Valor antes: "+expl[i]);
		//    System.out.print(" Maximo: "+max);
		//    System.out.print(" Valor despues: "+val);
		//    try{System.in.read();}catch(IOException e){};
		//}
		
		if(val > max){
		    changed = true;
		    max = val;
		    nodeR = nodes.elementAt(explNode[i]);//nodesToVisit.elementAt(i);
		    nodesR = explParents[i];//nodesAux;
		}
	    }
	}
	
	if(changed){
	    nodesPa.join(nodesR);
	    nodesSorted.insertNode(nodeR);
	    nodesToVisit.removeNode(nodeR);
	    return max;
	}else return (-1.0/0.0);
    }

    private double heuristic (NodeList nl, int i,NodeList nodesSorted){

	double fitnessOld,fitnessNew;
	NodeList vars,PaXi;
	boolean OkToProceed;
	FiniteStates nodeXi,nodeZ = null;
	PaXi = new NodeList();
	nodeXi = (FiniteStates) nodes.elementAt(i);
	nodeXi = (FiniteStates) input.getNodeList().getNode(nodeXi.getName());
	vars = new NodeList();
	vars.insertNode(nodeXi);
	fitnessOld = metric.score(vars);
	OkToProceed = true;
	while((OkToProceed)&&(PaXi.size()< numberMaxOfParents)){
	    nodeZ = maxScore(nodeXi,PaXi,nodesSorted);
	    if(nodeZ!=null){
		vars = new NodeList();
		vars.insertNode(nodeXi);
		if(PaXi.size()>0)
		    vars.join(PaXi);
		vars.insertNode(nodeZ);
		fitnessNew = metric.score(vars);
		//System.out.println(fitnessNew);
		if(fitnessNew > fitnessOld){
		    fitnessOld = fitnessNew;
		    PaXi.insertNode(nodeZ);
		}
		else OkToProceed = false;
	    }
	    else OkToProceed = false;
	}

	nl.join(PaXi);
	return fitnessOld;
    }


    /**
     * This method is private. It is used for searching the parent for the node
     * nodei that maximize the score metric.
     * @param FiniteStates nodei. the node.
     * @param NodeList pa. The actual parents set for the node i.
     * @param int index. The position for the node i.
     * @return FiniteStates. The maximal node.
     */

    private FiniteStates maxScore(FiniteStates nodei,NodeList pa,
				  NodeList nodesSorted){

	int i;
	FiniteStates node, nodeZ;
	NodeList vars;
	double val;
	double max = (-1.0/0.0);

	nodeZ=null;

	for(i=0; i<nodesSorted.size(); i++){
	    node = (FiniteStates)nodesSorted.elementAt(i);
	    if(pa.getId(node.getName()) == -1){
		vars = new NodeList();
		vars.insertNode(nodei);
		vars.join(pa);
		node=(FiniteStates)input.getNodeList().getNode(node.getName());
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

    public NodeList getNodesSorted(){

	return nodesSorted;
    }
	
} // K2Learning







