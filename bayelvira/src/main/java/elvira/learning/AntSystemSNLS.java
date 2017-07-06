package elvira.learning;

/**
 * AntSystemSN.java
 *
 *
 * Created: Fri Jun  2 09:10:29 2000
 *
 * @author J. M. Puerta
 * @version
 */

import elvira.*;
import elvira.database.*;
import java.util.*;
import java.io.*;
import elvira.parser.*;
import java.text.DecimalFormat;

public class AntSystemSNLS extends Learning {

    DataBaseCases cases;
    Metrics metric;
    Double feromone[][];
    double feromone_0[][];
    NodeList variables;
    double beta;
    double rho;
    double q0;
    int antNumber;
    int radius;
    int iteration;
    int[] maxIndex;
    double maxFitness[];
    Graph maxBnet;
    Hashtable values;
    Random generator = new Random();  
    double numIterForMax = 0.0;

    public static void main(String args[]) throws ParseException, IOException { 
    
	Bnet baprend;
	FileWriter f2;
	Metrics metric;
	if(args.length < 3){
	    System.out.println("too few arguments: Usage: file.dbc nCases nAnts Beta Rho q0 Cl It Metric[K2,BIC] [file.elv]");
	    System.exit(0);
	}
	int beta = Double.valueOf(args[3]).intValue();
	double rho = Double.valueOf(args[4]).doubleValue();
	double q0 = Double.valueOf(args[5]).doubleValue();
	int nAnts = Integer.valueOf(args[2]).intValue();
	int cl = Integer.valueOf(args[6]).intValue();
	int it = Integer.valueOf(args[7]).intValue();
	FileInputStream f = new FileInputStream(args[0]);	
	DataBaseCases cases = new DataBaseCases(f);
	cases.setNumberOfCases(Integer.valueOf(args[1]).intValue());
	if(args[8].equals("K2")) metric = (Metrics)new K2Metrics(cases);
	else metric = (Metrics)new BICMetrics(cases);
	K2SNOPT outputInitial = new K2SNOPT(cases,4,metric,1,0.0,cases.getNodeList().size());
	outputInitial.learning();
	System.out.println("Esta es la red que he aprendido con K2SN: "+outputInitial.getOutput().getLinkList().toString());
	AntSystemSNLS outputNet1 = new
	AntSystemSNLS(cases,metric,beta,rho,q0,nAnts,cl,it,
		    (Graph)outputInitial.getOutput());
        System.out.println("Voy a aprender...");    
	outputNet1.learning();
	DELearning outputNet3 = new DELearning(cases,outputNet1.getOutput());
	outputNet3.learning();
	double d = cases.getDivergenceKL(outputNet3.getOutput());
	System.out.println("Divergencia de KL = "+d);
	
	System.out.println("Fitness final = "+outputNet1.eval(outputNet1.maxFitness));
	System.out.println("Fitness del resultado: "+metric.score(outputNet1.getOutput()));
	System.out.println("Numero de Iteraciones para encontrar el maximo: "+outputNet1.numIterForMax);
	System.out.println("Numero de Iteraciones: "+outputNet1.iteration);
	System.out.println("Estadisticos evaluados: "+metric.getTotalStEval());      
        System.out.println("Total de estadisticos: "+metric.getTotalSt());      
        System.out.println("Numero medio de var en St: "+metric.getAverageNVars()); 





	//  f2 = new FileWriter(args[2]);
	//  	baprend = (Bnet)outputNet1.getOutput();
	//  	baprend.saveBnet(f2);
	//  	f2.close();
	
	if(args.length == 10){
	    FileInputStream fnet = new FileInputStream(args[9]);
	    Bnet net = new Bnet(fnet);
	    System.out.println("Fitness de la red aprendida: "+metric.score(outputNet1.getOutput()));
	    System.out.println("Fitness de la red Real: "+metric.score(net));
	    double d2 = cases.getDivergenceKL(net);
	    System.out.println("Divergencia real: "+(d2-d));
	    LinkList addel[] = new LinkList[3];
	    addel = outputNet1.compareOutput(net);
	    System.out.println("\nNumero de arcos añadidos: "+addel[0].size());
	    System.out.print(addel[0].toString());
	    System.out.println("\nNumero de arcos borrados: "+addel[1].size());
	    System.out.print(addel[1].toString());
	    System.out.println("\nNumero de arcos mal orientados: "+addel[2].size());
	    System.out.print(addel[2].toString());
	}  
	
    }  
    

    public AntSystemSNLS() {
	
    }
    
    public AntSystemSNLS(DataBaseCases cases, Metrics metric, double beta, 
		      double rho,double q0,int m, int cl,int it,Graph initial){
	
	int i,j,last;
	Node nodei;
	NodeList vars,paXi;
	this.cases = cases;
	this.metric = metric;
	this.beta = beta;
	this.rho = rho;
	this.q0 = q0;
	iteration = it;
	antNumber = m;
	radius = cl;
	values = new Hashtable();
	if(initial.isADag())
	    variables = initial.topologicalOrder();
	else variables = initial.getNodeList();
	maxIndex = new int[variables.size()];
	maxFitness = new double[variables.size()];
	
	for(i=0 ; i<variables.size(); i++){
	    vars = new NodeList();
	    nodei = variables.elementAt(i);
	    paXi = initial.parents(nodei);
	    nodei = cases.getNodeList().getNode(nodei.getName());
	    paXi = cases.getNodeList().intersectionNames(paXi);
	    vars.insertNode(nodei);
	    vars.join(paXi);
	    maxFitness[i] = metric.score(vars);
	    maxIndex[i]=i;
	}
	
	double maxF = eval(maxFitness);
	System.out.println("Fitness inicial = "+maxF);
	variables = variables.duplicate();
	feromone = new Double[variables.size()][variables.size()];
	feromone_0 =  new double[variables.size()][variables.size()];
	for(i=0; i< variables.size() ; i++)
	    for(j=0 ; j< variables.size() ; j++){
		feromone_0[i][j] = 1/(Math.abs(variables.size()*maxF));
		feromone[i][j] = new 
		    Double(1/(Math.abs(variables.size()*maxF)));
	    }
	//last = maxIndex[0];
	//for(i=1;i<maxIndex.length ; i++){
	//    feromone_0[last][maxIndex[i]]= 1/(Math.abs(maxF));
	//    last = maxIndex[i];
	//}
	//updateFeromone();
	maxBnet = new Graph(0);
	maxBnet.setNodeList(variables);
	copy(initial,maxBnet);
    } 

    public void learning(){
	Bnet bsalida = new Bnet(); 
	int i,j,v,w,nb=0;
	int lsIt = iteration;
        int lastImprov = 0;
	double maxF;
	AntK2K2S ants[] = new AntK2K2S[antNumber];
	ThVNSSN lsAnts[] = new ThVNSSN[antNumber];
	Thread th[] = new Thread[antNumber];
	Thread thLS[] = new Thread[antNumber];

	for(i=0 ; i<iteration ; i++){
	    for(j=0 ; j<ants.length ; j++){
		ants[j] = new AntK2K2S(feromone,feromone_0,variables,cases,
				     metric,rho,beta,q0,radius,generator);
		th[j] = new Thread(ants[j]);
	    }
	    //for(j=0 ; j<ants.length ; j++){
		//th[j].start();
	    //}
	    for(j=0 ; j<ants.length ; j++){
		try{
		  th[j].start();  
                  th[j].join();
		}catch(InterruptedException e){};
	    }
	    if(((i%lsIt == 0)||(i == (iteration-1)))&&(i>0)){
		System.out.println("Mejor Fitness sin HC: "+eval(maxFitness));
		for(j=0 ; j< ants.length ; j++){
		    lsAnts[j] = new ThVNSSN(variables,cases,metric,radius,
					    ants[j].index,ants[j].dag,
					    ants[j].fitness,nb,j,j,generator);
		    thLS[j] = new Thread(lsAnts[j]);
		}
		//for(j=0 ; j<ants.length ; j++){
		//    thLS[j].start();
		//}
		for(j=0 ; j<ants.length ; j++){
		    try{
                        thLS[j].start();
			thLS[j].join();
		    }catch(InterruptedException e){};
		}
		maxF = eval(maxFitness);
		double maxIt = (-1.0/0.0);
		int bestAntIt = -1;
		for(j=0 ; j<ants.length ; j++){
		    double valF = eval(lsAnts[j].fitness);
		    System.out.println("AntLS["+i+"]: "+valF);
		    if(valF > maxIt){
			maxIt = valF;
			bestAntIt=j;
		    }
		}
		boolean foundMax = false;
		if (maxF < eval(lsAnts[bestAntIt].fitness)){
		    System.arraycopy(lsAnts[bestAntIt].fitness,0,
				     maxFitness,0,maxFitness.length);
		    System.arraycopy(lsAnts[bestAntIt].index,0,
				     maxIndex,0,maxIndex.length);
		    copy(lsAnts[bestAntIt].dag,maxBnet);
		    foundMax = true;
                    lastImprov = 0;
		    numIterForMax = i+1;
		}else lastImprov++;
		decayFeromone();
		updateFeromone(lsAnts);
	    }
	    else{
		maxF = eval(maxFitness);
		double maxIt = (-1.0/0.0);
		int bestAntIt = -1;
		for(j=0 ; j<ants.length ; j++){
		    double valF = eval(ants[j].fitness);
		    System.out.println("Ant["+i+"]: "+valF);
		    if(valF > maxIt){
			maxIt = valF;
			bestAntIt=j;
		    }
		}
		boolean foundMax = false;
		if (maxF < eval(ants[bestAntIt].fitness)){
		    System.arraycopy(ants[bestAntIt].fitness,0,
				     maxFitness,0,maxFitness.length);
		    System.arraycopy(ants[bestAntIt].index,0,
				     maxIndex,0,maxIndex.length);
		    copy(ants[bestAntIt].dag,maxBnet);
		    foundMax = true;
                    lastImprov=0;
		    numIterForMax=i+1;
		}else lastImprov++;
		//decayFeromone();
		updateFeromone();
	    }
	    //printPheromone();
	    System.out.println("Mejor= "+eval(maxFitness));
            if(lastImprov > iteration){
                System.out.println("Actualizando Feromona");
                maxF = eval(maxFitness);
                for(v=0; v< variables.size() ; v++)
	           for(w=0 ; w< variables.size() ; w++){
		        feromone_0[v][w] = 1/(Math.abs(variables.size()*maxF));
		        feromone[v][w] = new 
		          Double(1/(Math.abs(variables.size()*maxF)));
	           }
                lastImprov = 0;
                updateFeromone();
            }
	    //try{System.in.read();}catch(IOException e){};
	}
	
	System.out.println("Mejor fitness: "+eval(maxFitness));
	
	setOutput(new Bnet());
	for(i=0 ; i<maxBnet.getNodeList().size(); i++){
	    try{
		getOutput().addNode(maxBnet.getNodeList().elementAt(i));
		Node ns = cases.getNodeList().getNode(maxBnet.getNodeList().
						      elementAt(i).getName());
		ns.setParents(new LinkList());
		ns.setChildren(new LinkList());
		ns.setSiblings(new LinkList());
		bsalida.addNode(ns);
	    }catch(InvalidEditException iee){};
	}
	for(i=0 ; i<maxBnet.getLinkList().size(); i++){
	    try{
		Link link=maxBnet.getLinkList().elementAt(i);
		getOutput().createLink(link.getTail(),link.getHead());
		Node noT = bsalida.getNodeList().
		    getNode(link.getTail().getName());
		Node noH = bsalida.getNodeList().
		    getNode(link.getHead().getName());
		bsalida.createLink(noT,noH);
	    }catch(InvalidEditException iee){};
	}
	
	System.out.println("La red: "+getOutput().getLinkList().toString());
	print(maxIndex);
	System.out.println("Fitness de la SALIDA: "+metric.score(bsalida));
	//try{System.in.read();}catch(IOException e){};
    }
    
    public void printPheromone(){
	DecimalFormat df = new DecimalFormat("0.00E00");
	for(int i=0 ; i< variables.size() ; i++){
	    System.out.print(variables.elementAt(i).getName());
	    for(int j=0 ; j< variables.size() ; j++)
		System.out.print(" "+df.format(feromone[i][j]));
	    System.out.println(" ");
	}
    }

    private void updateFeromone(ThVNSSN lsAnts[]){
	double val,fit;
	int i,j,pos;
	for(j=0 ; j< lsAnts.length ; j++){
	    pos = lsAnts[j].index[0];
	    fit = eval(lsAnts[j].fitness);
	    synchronized(feromone[pos][pos]){
		val = (1-rho)*
		    (feromone[pos][pos].doubleValue())+
		    (rho *(1/Math.abs(fit)));
		feromone[pos][pos] = new Double(val);
	    }
	    for(i=1 ; i<lsAnts[j].index.length ; i++){
		synchronized(feromone[pos][lsAnts[j].index[i]]){
		    val = (1-rho)*
			(feromone[pos][lsAnts[j].index[i]].doubleValue())+
			(rho * (1/(Math.abs(fit))));
		    feromone[pos][lsAnts[j].index[i]]= new Double(val);
		    pos = lsAnts[j].index[i];
		}
	    }
	}
    }
    
    private void updateFeromone(){
	double val,fit;
	int i;
	int pos = maxIndex[0];
	fit = eval(maxFitness);
	synchronized(feromone[maxIndex[0]][maxIndex[0]]){
	    val = (1-rho)*(feromone[maxIndex[0]][maxIndex[0]].doubleValue())+
		(rho *(1/Math.abs(fit)));
	    feromone[maxIndex[0]][maxIndex[0]] = new Double(val);
	}
	for(i=1 ; i<maxIndex.length ; i++){
	    synchronized(feromone[pos][maxIndex[i]]){
		val = (1-rho)*(feromone[pos][maxIndex[i]].doubleValue())+
		    (rho * (1/(Math.abs(fit))));
		feromone[pos][maxIndex[i]]= new Double(val);
		pos = maxIndex[i];
	    }
	}
	
    }

    private void decayFeromone(){
	int i,j;
	double val;
	synchronized(feromone){
	    for(i=0 ; i < variables.size() ; i++)
		for(j=0 ; j < variables.size() ; j++){
		    val =  (1-rho)*(feromone[i][j].doubleValue())+
			(rho * feromone_0[i][j]);
		    feromone[i][j]= new Double(val);
		}
	}
	
    }
    private void clean(Graph currentBnet){
	int i;

	currentBnet.setLinkList(new LinkList());
	for(i=0 ; i< currentBnet.getNodeList().size() ; i++){
	    Node node = currentBnet.getNodeList().elementAt(i);
	    node.setParents(new LinkList());
	    node.setChildren(new LinkList());
	    node.setSiblings(new LinkList());
	}
	
    }

    private double eval(double[] fitness){
	double sum = 0.0;
	for (int i = 0 ; i< fitness.length ; i++){
	    sum+=fitness[i];
	}
	return sum;
    }

    public void print(int[] index){
	for(int i=0 ; i< index.length ; i++)
	    System.out.print("  "+variables.elementAt(index[i]).getName());

    }

    private void copy(Graph currentGraph, Graph currentBnet){
	int i;
	
	currentBnet.setLinkList(new LinkList());
	for(i=0 ; i< currentBnet.getNodeList().size() ; i++){
	    Node node = currentBnet.getNodeList().elementAt(i);
	    node.setParents(new LinkList());
	    node.setChildren(new LinkList());
	    node.setSiblings(new LinkList());
	}

	for(i=0 ; i< currentGraph.getLinkList().size() ; i++){
	    Link link = currentGraph.getLinkList().elementAt(i);
	    Node nodeHead = link.getHead();
	    Node nodeTail = link.getTail();
	    nodeTail = currentBnet.getNodeList().getNode(nodeTail.getName());
	    nodeHead = currentBnet.getNodeList().getNode(nodeHead.getName());
	    try{
		currentBnet.createLink(nodeTail,nodeHead);
	    }catch(InvalidEditException iee){
		System.out.println("Estoy copiando......"); 
		System.out.println("Intentando crear la link: "+nodeTail.getName()+" ---> "+nodeHead.getName());   
		System.out.println("En el grafo: "+currentBnet.getLinkList().toString());
		System.exit(1);
	    };
	}
	
    }

    
    
} // AntSystemSN








