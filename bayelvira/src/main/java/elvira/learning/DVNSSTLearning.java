
package elvira.learning;

/**
 * DVNSSTLearning.java
 *
 *
 * Created: Tue Jul 25 09:33:07 2000
 *
 * @author J. M. Puerta
 * @version
 */

import java.io.*;
import java.util.*;
import elvira.parser.*;
import elvira.*;
import elvira.learning.*;
import elvira.database.*;


public class DVNSSTLearning extends Learning {

public    DataBaseCases cases;
public    Metrics metric;
public    NodeList variables;
public    int nProc = 1;
public    int popSize = 1;
public    int maxIter = 1;
public    int maxNb = 1;
public    VNSSTProcesor[] procesor;
public    double maxFitness;
public    Graph maxDag;
public    Random generator = new Random();
public    double localMaxAverage = 0.0;
public    double numIndEval = 0.0;
public    double numberIterationsForMaximun = 0.0;
public    double it = 0.0;

    public static void main(String args[]) throws ParseException, IOException { 
    
	Bnet baprend;
	FileWriter f2;
	Metrics met;

	if(args.length < 7){
	    System.out.println("too few arguments: Usage: file.dbc nProc TamPop file.elv maxNb cases BIC,K2,BDe nItera 0,1,2,3,4 [PC01,Alea,PC,K2SN,Vacia] [file.elv]");
	  System.exit(0);
	}
	int NProc = Integer.valueOf(args[1]).intValue();
	int TamPop = Integer.valueOf(args[2]).intValue();
	int MaxNb = Integer.valueOf(args[4]).intValue();
	int Niter = Integer.valueOf(args[7]).intValue();
	FileInputStream f = new FileInputStream(args[0]);      
	DataBaseCases cases = new DataBaseCases(f);
	cases.setNumberOfCases(Integer.valueOf(args[5]).intValue());
	if(args[6].equals("BIC")) met = (Metrics) new BICMetrics(cases);
	else if(args[6].equals("K2")) met = (Metrics) new K2Metrics(cases);
             else met = (Metrics) new BDeMetrics(cases);
	Learning outputInitial = null;
	switch(Integer.valueOf(args[8]).intValue()){
	case 0:
	    outputInitial = new PC01Learning(cases);
	    outputInitial.learning();
	    break;
	case 1: 
            Random generator = new Random();           
	    NodeList nodes = new NodeList(generator,cases.getNodeList());
	    outputInitial = new K2Learning(cases,nodes,5,met);
	    outputInitial.learning();
	    break;
	case 2:
	    outputInitial = new PCLearning(cases);
	    outputInitial.learning();
	    break;
	case 3:
	    outputInitial = new K2SNOPT(cases,4,met,1,0.0,cases.getNodeList().size());
	    outputInitial.learning();
	    break;
	default:
	    break;
	}
	
	DVNSSTLearning outputNet1 = new
	    DVNSSTLearning(cases,NProc,Niter,MaxNb,TamPop,met);
	if(Integer.valueOf(args[8]).intValue()<=3) outputNet1.setInitialBnet(outputInitial.getOutput());
	outputNet1.learning();
	DELearning outputNet3 = new DELearning(cases,outputNet1.getOutput());
	outputNet3.learning();
	double d = cases.getDivergenceKL(outputNet3.getOutput());
	System.out.println("Divergencia de KL = "+d);
	System.out.println("Fitness final = "+outputNet1.maxFitness);
	System.out.println("Fitness del resultado: "+met.score(outputNet1.getOutput()));
	System.out.println("Media de Fitness de los Maximos Locales: "+(outputNet1.localMaxAverage/outputNet1.it));
	System.out.println("Numero de Iteraciones para encontrar el maximo: "+outputNet1.numberIterationsForMaximun);
	System.out.println("Numero de Indivuduos Evaluados: "+outputNet1.numIndEval);
	System.out.println("Numero Medio de Individuos Evaluados por Iteracion: "+(outputNet1.numIndEval/outputNet1.it));
	System.out.println("Numero de Iteraciones: "+outputNet1.it);
	System.out.println("Estadisticos evaluados: "+met.getTotalStEval());      
        System.out.println("Total de estadisticos: "+met.getTotalSt());      
        System.out.println("Numero medio de var en St: "+met.getAverageNVars()); 

        f2 = new FileWriter(args[3]);
        baprend = (Bnet)outputNet3.getOutput();
        baprend.saveBnet(f2);
        f2.close();
	
        if(args.length > 8){
	    FileInputStream fnet = new FileInputStream(args[9]);
	    Bnet net = new Bnet(fnet);
	    System.out.println("Fitness de la red real: "+met.score(net));
	    double d2 = cases.getDivergenceKL(net);
	    System.out.println("Divergencia real: "+(d2-d));
	    LinkList addel[] = new LinkList[3];
	    addel = outputNet1.compareOutput(net);
	    System.out.print("\nNumero de arcos añadidos: "+addel[0].size());
	    System.out.print("\n"+addel[0].toString());
	    System.out.print("\nNumero de arcos borrados: "+addel[1].size());
	    System.out.print("\n"+addel[1].toString());
	    System.out.println("\nNumero de arcos mal orientados: "+addel[2].size());
	    System.out.print(addel[2].toString());
        }
	
    }  
    
    
    
    public DVNSSTLearning() {
	
    }

    public DVNSSTLearning(DataBaseCases cases,int nProc, int nIt ,
			  int maxNb, int popSize,Metrics met) {
	int i;
	Node nodei;
	NodeList vars;
	this.cases = cases;
	this.variables = cases.getNodeList().duplicate();
	this.metric = met;
	this.nProc = nProc;
	this.popSize = popSize;
	this.maxIter = nIt;
	this.maxNb = maxNb;
	this.procesor = new VNSSTProcesor[nProc];
	maxDag = new Graph();
	for(i=0 ; i< variables.size() ; i++){
	    nodei = variables.elementAt(i);
	    try{
		maxDag.addNode(nodei);
	    }catch(InvalidEditException iee){};
	}
	Bnet auxBnet = new Bnet();
	auxBnet.setNodeList(maxDag.getNodeList());
	auxBnet.setLinkList(maxDag.getLinkList());
	maxFitness = metric.score(auxBnet);
    }


    public void learning(){

	int i;
	int iteration;
	int indexProcMax = 0;


	Thread[] thProcesor = new Thread[nProc];

	// Fase de Inicializacion

	for(i=0 ; i< nProc ; i++){
	    procesor[i] = new VNSSTProcesor(i,variables,cases,metric,popSize,maxNb,generator,maxFitness);
	    thProcesor[i] = new Thread(procesor[i]);
	}
	procesor[0].popFitness[0] = maxFitness;
	procesor[0].popDags[0] = maxDag;
	for(i=0; i< nProc ; i++) thProcesor[i].start();
	for(i=0; i< nProc ; i++){
	    try{
		thProcesor[i].join();
	    }catch(InterruptedException e){};
	}

	// Fin de la fase de inicializacion

	indexProcMax = maxFitnessProcesors();  // Fijamos el maximo de todos

	for(i=0 ; i< nProc ; i++){
	    localMaxAverage+= procesor[i].localMaxAverage;
	    numIndEval+= procesor[i].numberIndivEval;
	    it+=procesor[i].it;
	}
	if(indexProcMax != -1)
            numberIterationsForMaximun = procesor[indexProcMax].numberIterationsForMaximun;
	for(iteration = 1 ; (iteration < maxIter)&&(indexProcMax != -1) 
		; iteration++){
	    migrate();
	    for(i=0; i< nProc ; i++){
		System.out.println("Proc."+procesor[i].numberProc+"Fit: "+procesor[i].
				   popFitness[procesor[i].maxIndex]);
	    }
	    for(i=0 ; i< nProc ; i++)
		thProcesor[i] = new Thread(procesor[i]);
	    
	    for(i=0 ; i< nProc ; i++) thProcesor[i].start();
	    for(i=0; i< nProc ; i++){
		try{
		    thProcesor[i].join();
		}catch(InterruptedException e){};
	    }
	    indexProcMax = maxFitnessProcesors();
	    for(i=0 ; i< nProc ; i++){
		localMaxAverage+= procesor[i].localMaxAverage;
		numIndEval+= procesor[i].numberIndivEval;
		it+=procesor[i].it;
	    }
	    if(indexProcMax != -1) numberIterationsForMaximun+= procesor[indexProcMax].
				       numberIterationsForMaximun;
	}
	setOutput(new Bnet());
	for(i=0; i< cases.getNodeList().size() ; i++){
	    try{
		Node nd = cases.getNodeList().elementAt(i);
		nd.setParents(new LinkList());
		nd.setChildren(new LinkList());
		nd.setSiblings(new LinkList());
		getOutput().addNode(nd);
	    }catch(InvalidEditException iee){};
	}
	for(i=0 ; i< maxDag.getLinkList().size() ; i++){
	    Link link = maxDag.getLinkList().elementAt(i);
	    Node nodeT = link.getTail();
	    Node nodeH = link.getHead();
	    try{
		nodeT = cases.getNodeList().getNode(nodeT.getName());
		nodeH = cases.getNodeList().getNode(nodeH.getName());
		getOutput().createLink(nodeT,nodeH,true);
	    }catch(InvalidEditException iee){};
	}
    }
    
    private int maxFitnessProcesors(){
	int i,index;
	double valor;
	double max =  maxFitness;
	int indexR=-1;
	for(i=0 ; i< nProc ; i++){
	    index = procesor[i].maxIndex;
	    valor = procesor[i].popFitness[index];
	    if(valor > max){
		max = valor;
		indexR = i;
	    }
	}
	if(indexR != -1){
	    index = procesor[indexR].maxIndex;
	    maxFitness = procesor[indexR].popFitness[index];
	    maxDag = procesor[indexR].popDags[index];
	}
	return indexR;
    }

    private void migrate(){
	int i,j;

	for(i=0 ; i< nProc ; i++){
	    j = (i+1) % nProc;
	    copyPopulation(i,j);
	}
    }

    private void copyPopulation(int or, int dest){
	int i,j;
	double[] popFitnessOr = procesor[or].popFitness;
	Graph[] popDagsOr = procesor[or].popDags;
	for(i=0 ; i< popSize ; i++){ 
	    procesor[dest].tmpPopFitness[popSize+i] = popFitnessOr[i];
	    procesor[dest].tmpPopDags[popSize+i] = popDagsOr[i];
	}
    }    

    public void setInitialBnet(Bnet initial){
        
        NodeList nodesBnet,vars,paNodei,nodes;
        Node nodei,nodeTail,nodeHead;
        Link link;
        int i,pos;
        nodesBnet = maxDag.getNodeList();
        for(i=0 ; i< initial.getLinkList().size() ; i++){
            link = (Link) initial.getLinkList().elementAt(i);
            nodeTail = nodesBnet.getNode(link.getTail().getName());
            nodeHead = nodesBnet.getNode(link.getHead().getName());
            try{
                maxDag.createLink(nodeTail,nodeHead,true);
            }catch(InvalidEditException iee){};
        }
	Bnet auxBnet = new Bnet();
	auxBnet.setNodeList(maxDag.getNodeList());
	auxBnet.setLinkList(maxDag.getLinkList());
	maxFitness = metric.score(auxBnet);
	nodesBnet = maxDag.topologicalOrder();
	nodes = new NodeList();
	for(i=0 ; i< nodesBnet.size(); i++){
	    nodei = nodesBnet.elementAt(i);
	    pos = variables.getId(nodei.getName());
	    nodes.insertNode(variables.elementAt(pos));
	}
	variables = nodes;
    }                         
    
    
} // DVNSSNLearning
