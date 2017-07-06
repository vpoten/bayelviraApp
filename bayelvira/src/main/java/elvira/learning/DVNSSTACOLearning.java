package elvira.learning;

/**
 * DNSSTACOLearning.java
 *
 *
 * Created: Mon Jul 24 12:41:43 2000
 *
 * @author J. M. Puerta
 * @version 1.0
 */
import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.parser.*;

public class DVNSSTACOLearning extends Learning {
    
public    NodeList variables;
public    DataBaseCases cases;
public    Metrics metric;
public    double[] popFitness;
public    Graph[] popDags;
public    int maxIndex;
public    int maxIteration;
public    int maxNb;
public    int numberProc;
public    Random generator = new Random();
public    double localMaxAverage = 0.0;
public    double numIndEval = 0.0;
public    double numberIterationsForMaximun = 0.0;
public    double it = 0.0;
public    double rho;
public    double beta;
public    double q0;
public    Double feromone[][];
public    double feromone_0[][];
public    Graph maxDag;
public    double maxFitness;



    public static void main(String args[]) throws ParseException, IOException { 
    
	Bnet baprend;
	FileWriter f2;
	Metrics met;
	
	if(args.length < 8){
	    System.out.println("too few arguments: Usage: file.dbc nProc beta q0 rho maxNb cases BIC,K2,BDe nItera 0,1,2,3,4 [PC01,Alea,PC,K2SN,Vacia] [file.elv]");
	  System.exit(0);
	}
	int NProc = Integer.valueOf(args[1]).intValue();
	double Beta = Double.valueOf(args[2]).doubleValue();
	double Q0 = Double.valueOf(args[3]).doubleValue();
	double Rho = Double.valueOf(args[4]).doubleValue();
	int MaxNb = Integer.valueOf(args[5]).intValue();
	int Niter = Integer.valueOf(args[8]).intValue();
	FileInputStream f = new FileInputStream(args[0]);      
	DataBaseCases cases = new DataBaseCases(f);
	cases.setNumberOfCases(Integer.valueOf(args[6]).intValue());
	if(args[7].equals("BIC")) met = (Metrics) new BICMetrics(cases);
	else if(args[7].equals("K2")) met = (Metrics) new K2Metrics(cases);
             else met = (Metrics) new BDeMetrics(cases);
	Learning outputInitial = null;
	switch(Integer.valueOf(args[9]).intValue()){
	case 0:
	    outputInitial = new PC01Learning(cases);
	    outputInitial.learning();
	    break;
	case 1:
	    FileInputStream fInNodes = new FileInputStream("alarmAlea.var");
	    NodeList nodes = new NodeList(fInNodes,cases.getNodeList());
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
	
	DVNSSTACOLearning outputNet1 = new DVNSSTACOLearning(NProc,Niter,cases,met,MaxNb,Rho,Beta,Q0);
	if(Integer.valueOf(args[9]).intValue()<=3) outputNet1.setInitialBnet(outputInitial.getOutput());
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

        f2 = new FileWriter("f.sal");
        baprend = (Bnet)outputNet3.getOutput();
        baprend.saveBnet(f2);
        f2.close();
	
        if(args.length > 9){
	    FileInputStream fnet = new FileInputStream(args[10]);
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
    

    public DVNSSTACOLearning() {
	
    }

    public DVNSSTACOLearning(int nproc,int iters,DataBaseCases data,Metrics met
			     ,int maxNb,double rho,double beta,double q0){

	int i;
	Node nodei;
	
	this.numberProc = nproc;
	variables = data.getNodeList().duplicate();
	cases = data;
	metric = met;
	maxIteration = iters;
	this.maxNb = maxNb;
	this.rho = rho;
	this.beta = beta;
	this.q0 = q0;
	popFitness = new double[numberProc];
	popDags = new Graph[numberProc];
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
	feromone = new Double[variables.size()][variables.size()];
	feromone_0 = new double[variables.size()][variables.size()];
	for(int v=0; v < variables.size() ; v++)
	    for(int w=0 ; w < variables.size() ; w++){
		feromone_0[v][w] = 1/(Math.abs(variables.size()*maxFitness));
		feromone[v][w] = new 
		    Double(1/(Math.abs(variables.size()*maxFitness)));
	    }
	
    }

    public void learning(){
	
	int i;
	Thread[] th = new Thread[numberProc];
	ThVNSSTACO[] thvnsst = new ThVNSSTACO[numberProc];

	inicializePopulationBS();
	printPop();
	for(int iteration = 0 ; iteration < maxIteration ; iteration++){
	    for(i = 0 ; i < numberProc ; i++){
		thvnsst[i] = new ThVNSSTACO(feromone,rho,variables,cases, metric,
					    popDags[i],popFitness[i],maxFitness,maxNb,numberProc,i,
					    generator);
		th[i] = new Thread(thvnsst[i]);
	    }
	    for(i = 0 ; i < numberProc ; i++){
		th[i].start();
		try{
		    th[i].join();
		}catch(InterruptedException e){};
	    }
	    for(i=0; i< numberProc ; i++){
		popFitness[i] = thvnsst[i].maxFitness;
		popDags[i] = thvnsst[i].dag;
		localMaxAverage+= thvnsst[i].getLocalMaxAverage();
		numIndEval+= thvnsst[i].getNumOfIndEval();
		it+= thvnsst[i].it;
	    }
	    maxIndex = searchMax();
	    if(maxIndex != -1){
		numberIterationsForMaximun+= thvnsst[maxIndex].getNumberOfIterationsForMaximun();
		maxFitness = popFitness[maxIndex];
		maxDag = popDags[maxIndex].duplicate();
	    }
	    if(iteration < maxIteration){
		for(i=0 ; i< numberProc ; i++){
		    AntSTB antstb = new AntSTB(feromone,feromone_0,variables,cases,
					       metric,rho,beta,q0,generator);
		    Thread thstb = new Thread(antstb);
		    thstb.start();
		    try{
			thstb.join();
		    }catch(InterruptedException e){};
		    popFitness[i]=antstb.getFitness();
		    popDags[i]=antstb.getDag();
		}
	    }
	    updateFeromone();
	    printPop();
	    
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
		getOutput().createLink(nodeT,nodeH);
	    }catch(InvalidEditException iee){};
	}
    }


    private void printPop(){

	for(int i=0 ; i< numberProc ; i++)
	    System.out.println("Ind: "+i+" --> "+popFitness[i]);

	//try{System.in.read();}catch(IOException iee){};
    }

    private int searchMax(){

	double valor;
	int i,ind=-1;
	double max = maxFitness;
	for(i=0 ; i< numberProc ; i++){
	    valor = popFitness[i];
	    if(valor > max){
		ind = i;
		max = valor;
	    }
	}
	return ind;
    }


    private void inicializePopulationBS(){

	int i=0;
	int j,h,k;
	NodeList vars;
    
	synchronized(cases){
	    while(i < numberProc){
		AntSTB ant;
		Thread th;
		ant = new AntSTB(feromone,feromone_0,variables,cases,
				 metric,rho,beta,q0,generator);
		th = new Thread(ant);
		try{
		    th.start();
		    th.join();
		}catch(InterruptedException e){};
		popDags[i] = ant.dag;
		popFitness[i] = ant.fitness;
		i++;
	    }
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
                maxDag.createLink(nodeTail,nodeHead);
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

	for(int v=0; v < variables.size() ; v++)
	    for(int w=0 ; w < variables.size() ; w++){
		feromone_0[v][w] = 1/(Math.abs(variables.size()*maxFitness));
		feromone[v][w] = new 
		    Double(1/(Math.abs(variables.size()*maxFitness)));
	    }
	
    }                         
    

    private void updateFeromone(){
	double val,fit;
	int i,j;
	
	fit = maxFitness;
	for(i=0 ; i < variables.size(); i++){
	    Node nodei = variables.elementAt(i);
	    for(j=0; j < variables.size();j++){
		Node nodej = variables.elementAt(j);
		Link link = maxDag.getLinkList().getLinks(nodei.getName(),nodej.getName());
		//NodeList vars = new NodeList();
		//vars.insertNode(nodej);
		//nodej = maxDag.getNodeList().getNode(nodej.getName());
		//NodeList pa = maxBnet.parents(nodej);
		//vars.join(pa);
		//vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
		//double fitfam = metric.score(vars);
		if(link != null){
		    //System.out.println("Actualizando fero St: "+link.toString());
		    //try{System.in.read();}catch(IOException e){};
		    synchronized(feromone[i][j]){
			val = (1-rho)*(feromone[i][j].doubleValue())+
			    (rho * (1/(Math.abs(fit))));
			feromone[i][j]= new Double(val);
		    }
		}else{
		    synchronized(feromone[i][j]){
			val = (1-rho)*(feromone[i][j].doubleValue());
			feromone[i][j]= new Double(val);
		    }
		}
	    }
	}
    }
    

    
} //// Fin de la clase

