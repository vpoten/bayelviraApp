package elvira.learning;

/**
 * AntSystemSTB.java
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

public class AntSystemSTB extends Learning {

    DataBaseCases cases;
    Metrics metric;
    Double feromone[][];
    double feromone_0[][];
    NodeList variables;
    double beta;
    double rho;
    double q0;
    int antNumber;
    int iteration;
    Graph maxBnet;
    double maxFitness;
    Random generator = new Random();
    double numIterForMax = 0.0;
    
    public static void main(String args[]) throws ParseException, IOException { 
    
	Bnet baprend;
	FileWriter f2;
	Metrics metric;
	if(args.length < 3){
	    System.out.println("too few arguments: Usage: file.dbc nCases nAnts Beta Rho q0 It Metric[K2,BIC,BDe] [file.elv]");
	    System.exit(0);
	}
	int beta = Double.valueOf(args[3]).intValue();
	double rho = Double.valueOf(args[4]).doubleValue();
	double q0 = Double.valueOf(args[5]).doubleValue();
	int nAnts = Integer.valueOf(args[2]).intValue();
	int it = Integer.valueOf(args[6]).intValue();
	FileInputStream f = new FileInputStream(args[0]);	
	DataBaseCases cases = new DataBaseCases(f);
	cases.setNumberOfCases(Integer.valueOf(args[1]).intValue());
	if(args[7].equals("K2")) metric = (Metrics)new K2Metrics(cases);
	else if(args[7].equals("BIC")) metric = (Metrics)new BICMetrics(cases);
        else metric = (Metrics)new BDeMetrics(cases);
	//K2SNOPT outputInitial = new K2SNOPT(cases,4,metric,1,0.0,cases.getNodeList().size());
	//outputInitial.learning();
	//System.out.println("Esta es la red que he aprendido con K2SN: "+outputInitial.getOutput().getLinkList().toString());
	AntSystemSTB outputNet1 = new
	AntSystemSTB(cases,metric,beta,rho,q0,nAnts,it);
	//	    (Graph)outputInitial.getOutput());
        System.out.println("Voy a aprender...");    
	outputNet1.learning();
	DELearning outputNet3 = new DELearning(cases,outputNet1.getOutput());
	outputNet3.learning();
	double d = cases.getDivergenceKL(outputNet3.getOutput());
	System.out.println("Divergencia de KL = "+d);

	System.out.println("Fitness final = "+outputNet1.maxFitness);
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
	
	if(args.length == 9){
	    FileInputStream fnet = new FileInputStream(args[8]);
	    Bnet net = new Bnet(fnet);
	    System.out.println("Fitness de la red aprendida: "+metric.score(outputNet1.getOutput()));
	    System.out.println("Fitness de la red Real: "+metric.score(net));
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
    

    public AntSystemSTB() {
	
    }

    public AntSystemSTB(DataBaseCases cases, Metrics metric, double beta, 
		      double rho,double q0,int m,int it){
	int i,j,last;
	Node nodei;
	NodeList vars;
	this.cases = cases;
	this.metric = metric;
	this.beta = beta;
	this.rho = rho;
	this.q0 = q0;
	iteration = it;
	antNumber = m;
	variables = cases.getNodeList();
	maxFitness = 0.0;
	
	for(i=0 ; i<variables.size(); i++){
	    vars = new NodeList();
	    nodei = variables.elementAt(i);
	    vars.insertNode(nodei);
	    maxFitness+= metric.score(vars);
	}
	
	System.out.println("Fitness inicial = "+maxFitness);
	variables = variables.duplicate();
	feromone = new Double[variables.size()][variables.size()];
	feromone_0 =  new double[variables.size()][variables.size()];
	for(i=0; i< variables.size() ; i++)
	    for(j=0 ; j< variables.size() ; j++){
		feromone_0[i][j] = 1/(Math.abs(variables.size()*maxFitness));
		feromone[i][j] = new 
		    Double(1/(Math.abs(variables.size()*maxFitness)));
	    }
	maxBnet = new Graph(0);
	maxBnet.setNodeList(variables);
	maxBnet.setLinkList(new LinkList());
	for(i=0 ; i< maxBnet.getNodeList().size() ; i++){
	    Node node = maxBnet.getNodeList().elementAt(i);
	    node.setParents(new LinkList());
	    node.setChildren(new LinkList());
	    node.setSiblings(new LinkList());
	}
    }

    public AntSystemSTB(DataBaseCases cases, Metrics metric, double beta, 
		      double rho,double q0,int m,int it,Graph initial){
	
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
	variables = initial.getNodeList();
	maxFitness = 0.0;
	
	for(i=0 ; i<variables.size(); i++){
	    vars = new NodeList();
	    nodei = variables.elementAt(i);
	    paXi = initial.parents(nodei);
	    nodei = cases.getNodeList().getNode(nodei.getName());
	    paXi = cases.getNodeList().intersectionNames(paXi);
	    vars.insertNode(nodei);
	    vars.join(paXi);
	    maxFitness+= metric.score(vars);
	}
	
	System.out.println("Fitness inicial = "+maxFitness);
	variables = variables.duplicate();
	feromone = new Double[variables.size()][variables.size()];
	feromone_0 =  new double[variables.size()][variables.size()];
	for(i=0; i< variables.size() ; i++)
	    for(j=0 ; j< variables.size() ; j++){
		feromone_0[i][j] = 1/(Math.abs(variables.size()*maxFitness));
		feromone[i][j] = new 
		    Double(1/(Math.abs(variables.size()*maxFitness)));
	    }
	maxBnet = new Graph(0);
	maxBnet.setNodeList(variables);
	copy(initial,maxBnet);
    } 

    public void learning(){
	Bnet bsalida = new Bnet(); 
	int i,j,nb=0;
	int lsIt = 10;
	double maxF;
	AntSTB ants[] = new AntSTB[antNumber];
	ThVNSST2 lsAnts[] = new ThVNSST2[antNumber];
	Thread th[] = new Thread[antNumber];
	Thread thLS[] = new Thread[antNumber];

	for(i=0 ; i<iteration ; i++){
	    for(j=0 ; j<ants.length ; j++){
		ants[j] = new AntSTB(feromone,feromone_0,variables,cases,
				     metric,rho,beta,q0,generator);
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
		if(i == (iteration-1)) System.out.println("Mejor Fitness sin HC Final: "+maxFitness);
		for(j=0 ; j< ants.length ; j++){
		    lsAnts[j] = new ThVNSST2(variables,cases,metric,ants[j].dag,
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
		maxF = maxFitness;
		double maxIt = (-1.0/0.0);
		int bestAntIt = -1;
		for(j=0 ; j<ants.length ; j++){
		    double valF = lsAnts[j].maxFitness;
		    System.out.println("AntLS["+i+"]: "+valF);
		    if(valF > maxIt){
			maxIt = valF;
			bestAntIt=j;
		    }
		}
		boolean foundMax = false;
		if (maxF < lsAnts[bestAntIt].maxFitness){
		    maxFitness = lsAnts[bestAntIt].maxFitness;
		    copy(lsAnts[bestAntIt].dag,maxBnet);
		    numIterForMax = i+1;
		    foundMax = true;
		}
		//decayFeromone();
		updateFeromone( );//lsAnts);
	    }
	    else{
		maxF = maxFitness;
		double maxIt = (-1.0/0.0);
		int bestAntIt = -1;
		for(j=0 ; j<ants.length ; j++){
		    double valF = ants[j].fitness;
		    System.out.println("Ant["+i+"]: "+valF);
		    if(valF > maxIt){
			maxIt = valF;
			bestAntIt=j;
		    }
		}
		boolean foundMax = false;
		if (maxF < ants[bestAntIt].fitness){
		    maxFitness = ants[bestAntIt].fitness;
		    copy(ants[bestAntIt].dag,maxBnet);
		    numIterForMax = i+1;
		    foundMax = true;
		}
		//decayFeromone();
		updateFeromone();
	    }
	    //printPheromone();
	    System.out.println("Mejor= "+maxFitness);
	    //try{System.in.read();}catch(IOException e){};
	}
	
	System.out.println("Mejor fitness: "+maxFitness);
	
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

    private void updateFeromone(ThVNSST2 lsAnts[]){
	double val,fit;
	int i,j,p,posNodep;
       
	for(j=0 ; j< lsAnts.length ; j++){
	    fit = lsAnts[j].maxFitness;
	    for(i=0 ; i < variables.size() ; i++){
		Node nodei = variables.elementAt(i);
                nodei=lsAnts[j].dag.getNodeList().getNode(nodei.getName());
		NodeList pa = lsAnts[j].dag.parents(nodei);
                for(p=0 ; p < pa.size(); p++){
                   Node nodep = pa.elementAt(p);
                   nodep = variables.getNode(nodep.getName());
                   posNodep = variables.getId(nodep);
		   synchronized(feromone[posNodep][i]){
		       val = (1-rho)*(feromone[posNodep][i].doubleValue())+
			     (rho * (1/(Math.abs(fit))));
                       feromone[posNodep][i]= new Double(val);
		   }
	        }
	    }
        } 
    }
    
    private void updateFeromone(){
	double val,fit;
	int i,posNodep,p;
	fit =  maxFitness;
	for(i=0 ; i< variables.size() ; i++){
	    Node nodei = variables.elementAt(i);
            nodei = maxBnet.getNodeList().getNode(nodei.getName());
            NodeList pa = maxBnet.parents(nodei);
	    NodeList vars = new NodeList();
	    vars.insertNode(nodei);
	    vars.join(pa);
	    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
	    double fitfam = metric.score(vars);
            for(p=0 ; p < pa.size(); p++){
               Node nodep = pa.elementAt(p);
               nodep = variables.getNode(nodep.getName());
               posNodep = variables.getId(nodep);
	       synchronized(feromone[posNodep][i]){
                       double valOld = feromone[posNodep][i].doubleValue();
                       //System.out.println("Valor antiguo feromona de "+nodep.getName()+"-->"+nodei.getName()+" val: "+valOld);
		       val = (1-rho)*(feromone[posNodep][i].doubleValue())+
			     (rho * (1/(Math.abs(fit))));
                       feromone[posNodep][i]= new Double(val);
                       //System.out.println("Valor nuevo: "+val);
                       //try{System.in.read();}catch(IOException e){};
	       }
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

    
    
} // AntSystemSTB








