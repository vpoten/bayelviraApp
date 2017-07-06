package elvira.learning;

import java.util.Random;
import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * HCSTWIMAPR.java
 *
 *
 * Created: Sun Jan  9 14:11:29 2000
 *
 * @author P.Elvira
 * @version 1.0
 */

public class HCSTWIMAPR extends Learning {
    
    
    DataBaseCases input;       // The cases for the input algorithm.
    int numberMaxOfParents;    // The number of maximal parents for each node.
    Metrics metric;            // The K2 metric for scoring.
    Bnet initialBnet;
    int maxIt = 1;
    double localMaxAverage = 0.0;
    double it = 0.0;
    double numberIterationsForMaximun = 0.0;
    double numIndEval = 0.0;
    Random generator = new Random();

 public static void main(String args[]) throws ParseException, IOException { 
     
     Bnet baprend;
     FileWriter f2;
     Metrics met;
     if(args.length < 3){
	 System.out.println("too few arguments: Usage: file.dbc numberOfMaxParents file.elv [file.elv] cases BIC,K2 MaxIt 0,1,2,3,4 [PC01,Alea,PC,k2sn,vacia]");
	 System.exit(0);
     }
     FileInputStream f = new FileInputStream(args[0]);
     
     DataBaseCases cases = new DataBaseCases(f);
     cases.setNumberOfCases(Integer.valueOf(args[4]).intValue());
     if(args[5].equals("BIC")) met = (Metrics) new BICMetrics(cases);
     else met = (Metrics) new K2Metrics(cases);

     Learning outputInitial = null;
     switch(Integer.valueOf(args[7]).intValue()){
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
     
     HCSTWIMAPR outputNet1 = new HCSTWIMAPR(cases,Integer.valueOf(args[1]).intValue(),met,
				      Integer.valueOf(args[6]).intValue());
    
     if(Integer.valueOf(args[7]).intValue()<=3) outputNet1.setInitialBnet(outputInitial.getOutput());     
     outputNet1.learning();
     
     DELearning outputNet3 = new DELearning(cases,outputNet1.getOutput());
     outputNet3.learning();
     double d = cases.getDivergenceKL(outputNet3.getOutput());
     System.out.println("Divergencia de KL = "+d);
     System.out.println("Fitness del resultado: "+met.score(outputNet1.getOutput()));
     System.out.println("Media de los Maximos Locales: "+(outputNet1.localMaxAverage/outputNet1.it));
     System.out.println("Numero de Iteraciones hasta el maximo: "+outputNet1.numberIterationsForMaximun);
     System.out.println("Numero de Individuos Evaluados: "+outputNet1.numIndEval);
     System.out.println("Numero Medio de Individuos Evaluados por Iteracion: "+(outputNet1.numIndEval/outputNet1.it));
     System.out.println("Numero de Iteraciones: "+outputNet1.it);
     System.out.println("Tiempo gastado en estadisticos: "+met.getTotalTime());
     System.out.println("Tiempo gastado en eval. estd.: "+met.getTimeStEval());
     System.out.println("Total de estadisticos: "+met.getTotalSt());
     System.out.println("Estadisticos evaluados: "+met.getTotalStEval());
     System.out.println("Numero medio de var en St: "+met.getAverageNVars());
     f2 = new FileWriter(args[2]);
     baprend = (Bnet)outputNet1.getOutput();
     baprend.saveBnet(f2);
     f2.close();
     
     if(args[3] != null){
	 FileInputStream fnet = new FileInputStream(args[3]);
	 Bnet net = new Bnet(fnet);
	 
	 System.out.println("Fitness de la red real: "+met.score(net));
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
    
    public HCSTWIMAPR(){
	setInput(null);
	setMetric(null);
    }

    /**
     * Constructor for the Learning Algorithm K2.
     * @param DataBaseCases. cases. The input of algorithm.
     * @param NodeList nodes. The list of nodes sorted.
     * @param int nMaxOfParents. The max number of parents for each node.
     */

    public HCSTWIMAPR(DataBaseCases cases,int nMaxParents,Metrics met,
		      int maxIt){
	NodeList vars = cases.getNodeList();
	initialBnet = new Bnet();

	for(int i=0; i< vars.size(); i++){
	    try{
		initialBnet.addNode(vars.elementAt(i));
	    }catch(InvalidEditException iee){};
	}
	input = cases;
	numberMaxOfParents = nMaxParents;
	metric = met;
	this.maxIt = maxIt;
    } 
    
    /**
     * This methods implements the K2 algorithm without order amongs variables.
     */

    public void  learning(){

	NodeList vars,pa;
	double fitness,fitnessNew;
	boolean OkToProceed;
        boolean stop=false;
	Link newLink,link;
	Hashtable scores = new Hashtable();
	Bnet currentBnet;
	int op;
	LinkList linksToInsert,linksToRemove;
	Vector evolTime = new Vector();
	Vector evolFitness = new Vector();
	int nb = 1;
        int itwi = 0;
	Graph maxBnet;
        double maxBnetFitness;

	Date D;
	double time,timeInitial;

	D = new Date();
	timeInitial = (double)D.getTime();

		
	currentBnet = getInitialBnet();
	fitness = metric.score(currentBnet);
	maxBnet = (Graph) currentBnet.duplicate();
	maxBnetFitness = fitness;

	evolTime.addElement(new Double(0.0));
	evolFitness.addElement(new Double(fitness));
	System.out.println("fitness inicial: "+fitness);
	System.out.println("Con la red: "+currentBnet.getLinkList().toString());
	System.out.println("Is a dag? "+currentBnet.isADag());
	//try{System.in.read();}catch(IOException e){};
	//if (currentBnet.getLinkList().size() >= 0)
        //stop = I_map((Graph)currentBnet);
	//else stop = false;
	while(!stop){
            ThVNSST2 hillclimbing = new ThVNSST2(currentBnet.getNodeList(),input,metric,
                                                 currentBnet,fitness,0,0,0,generator);
            Thread hc = new Thread(hillclimbing);
            hc.start();
            try{hc.join();}catch(InterruptedException e){};
            copy(hillclimbing.dag,(Graph)currentBnet);
	    it++;
	    fitness = hillclimbing.maxFitness; //metric.score(currentBnet);
	    localMaxAverage+=fitness;
            numIndEval+=hillclimbing.getNumOfIndEval();
	    System.out.println("Fitness (Maximo Local): "+fitness);
	    System.out.println("Fitness (Maximo): "+maxBnetFitness);
	    if (fitness > maxBnetFitness ){
                itwi = 0;
		System.out.println("he encontrado otra mejor en Iteracion: "+nb);
		maxBnetFitness = fitness;
		maxBnet = (Graph) currentBnet.duplicate();
		nb++;
		numberIterationsForMaximun = it;
		stop = I_map((Graph)currentBnet);
		//currentBnet = disturb(currentBnet,(double)(maxBnet.getLinkList().size()));
                fitness = metric.score(currentBnet);
		System.out.println("Despues de perturbar..Inicio: "+fitness);
	    }
	    else{
		nb++;
                itwi++;
                //copy(maxBnet,(Graph)currentBnet);
		stop = I_map((Graph)currentBnet);
		//if (itwi >= 5){
		//     System.out.println("No mejora voy a empezar PC01 + IMAPR .....");
                //     PC01Learning outputNet = new PC01Learning(input);
                //     outputNet.learning();
                //     copy(outputNet.getOutput(),currentBnet);
                //     //currentBnet = empty(currentBnet);
                //     stop = I_map((Graph) currentBnet);
                //     itwi = 0;
                //}else{
                     //copy(maxBnet,(Graph)currentBnet);
		//     stop = I_map((Graph)currentBnet);
                //}
                fitness = metric.score(currentBnet);
		System.out.println("Despues de perturbar..Inicio: "+fitness);
	    } 
	    if(nb > maxIt) stop = true;
	}
	// try{
// 	    FileWriter fsl = new FileWriter("fsalGreedy.evol");
// 	    PrintWriter fsal = new PrintWriter(fsl);
// 	    for(int k = 0 ; k<evolTime.size() ; k++)
// 		fsal.print("\n"+evolTime.elementAt(k).toString()+"   "+evolFitness.elementAt(k).toString());
// 	    fsal.flush();
// 	}catch(IOException e){};
	
	setOutput(new Bnet());
	for(int i=0 ; i< maxBnet.getNodeList().size(); i++){
	    try{
		getOutput().addNode(maxBnet.getNodeList().elementAt(i));
	    }catch(InvalidEditException iee){};
	}
	for(int i=0 ; i< maxBnet.getLinkList().size(); i++){
	    try{
		link = (Link)maxBnet.getLinkList().elementAt(i);
		Node nodeTail = getOutput().getNodeList().getNode(link.getTail().getName());
		Node nodeHead = getOutput().getNodeList().getNode(link.getHead().getName());
		getOutput().createLink(nodeTail,nodeHead);
	    }catch(InvalidEditException iee){};
	}   	
    }
    

    

    private boolean I_map(Graph current){

	int i,j;
	double level;
	Node nodei,nodej;
	Link linkT,linkH;
	NodeList nodes,minDsep;
	boolean stop=true;
	boolean hTot,tToh;
        //System.out.println("Voy a comenzar con la comprobancion de Imap.....");
        //System.out.println("Con la red: "+current.getLinkList().toString());
        //System.out.println("Dirigida aciclica?:"+ current.isADag());
	nodes = current.getNodeList().randomOrder();
	//level = generator.nextDouble();
	//if(level < 0.5) level = 1.0 - level;
        //System.out.println("Nodos ordenados son: "+nodes.toString2());
	for(i=0 ; i < nodes.size();i++){
	    nodei = (Node) nodes.elementAt(i);
            System.out.print("  :node: "+i+" de  "+nodes.size());	
	    for(j=0 ; j< nodes.size() ; j++){
		nodej = (Node) nodes.elementAt(j);		
                //try{System.in.read();}catch(IOException e){};
		if(i!=j){
		    linkT = current.getLinkList().getLinks(nodei.getName(),
							   nodej.getName());
		    linkH = current.getLinkList().getLinks(nodej.getName(),
							   nodei.getName());
		    if((linkT == null)&&(linkH == null)){
			//System.out.println("No hay Link entre ellos...");
			minDsep = current.minimunDSeparatingSet(nodei,nodej);
			//System.out.println("Minimo: "+minDsep.toString2());
			if(minDsep == null) minDsep = new NodeList();
                        nodei = input.getNodeList().getNode(nodei.getName());
                        nodej = input.getNodeList().getNode(nodej.getName());
                        minDsep=input.getNodeList().intersectionNames(minDsep);
			level = 0.75;
                        //while ( (level < 0.5) || (level > 0.75)) level = generator.nextDouble();
			if(!input.independents(nodei,nodej,minDsep,level)){
			    hTot = true;
			    tToh = true;
			    double val = generator.nextDouble();
			    Vector acc = new Vector();
			    acc = current.directedDescendants(nodei);
			    if(acc.indexOf(nodej) != -1) hTot = false;
			    acc = current.directedDescendants(nodej);
			    if(acc.indexOf(nodei)!= -1) tToh = false;
			    if(val >= 0.5){
				try{
				    if(tToh)
					current.createLink(nodei,nodej);
				    else 
					current.createLink(nodej,nodei);
				}catch(InvalidEditException iee){};
			    }else{
				try{
				    if(hTot)
					current.createLink(nodej,nodei);
				    else
					current.createLink(nodei,nodej);
				}catch(InvalidEditException iee){};
			    }
			    stop = false;
			}
		    }else{
			try{
			    if(linkT!=null){
				//System.out.println("Si hay Link: "+linkT.toString());
				current.removeLink(linkT);
			    }
			    if(linkH!=null){
				//System.out.println("Si hay Link: "+linkH.toString());
				current.removeLink(linkH);
			    }
			}catch(InvalidEditException iee){};
			minDsep = current.minimunDSeparatingSet(nodei,nodej);
			//System.out.println("Minimo: "+minDsep.toString2());
			if(minDsep == null) minDsep = new NodeList();
                        nodei=input.getNodeList().getNode(nodei.getName());
                        nodej=input.getNodeList().getNode(nodej.getName());
                        minDsep=input.getNodeList().intersectionNames(minDsep);
                        level =  0.999;//generator.nextDouble();
                        //while ( level < 0.75 ) level = generator.nextDouble();
			if(!input.independents(nodei,nodej,minDsep,level)){
			    try{
				if(linkT!=null)
				    current.createLink(linkT.getTail(),linkT.getHead());
				if(linkH!=null)
				    current.createLink(linkH.getTail(),linkH.getHead());
			    }catch(InvalidEditException iee){};
                        }else stop = false;
		    }
		}
	    }
	}
	return stop;
    }
    
    private Bnet empty(Bnet current){
        int i,j;
	
	current.setLinkList(new LinkList());
	for(i=0 ; i< current.getNodeList().size() ; i++){
	    Node node = current.getNodeList().elementAt(i);
	    node.setParents(new LinkList());
	    node.setChildren(new LinkList());
	    node.setSiblings(new LinkList());
	} 
        NodeList nodes = current.getNodeList().randomOrder();
        for(i=0 ; i < nodes.size()-1; i++)
          for(j=i+1 ; j < nodes.size(); j++){
              Node nodei = nodes.elementAt(i);
              Node nodej = nodes.elementAt(j);
              nodei = input.getNodeList().getNode(nodei.getName());
              nodej = input.getNodeList().getNode(nodej.getName());
              NodeList minDsep = new NodeList();
              double level = 0.999;
              if(!input.independents(nodei,nodej,minDsep,level)){
                 try{
                     current.createLink(nodei,nodej);
                 }catch(InvalidEditException iee){};
              }
          } 

        return current;
    }

    private Bnet disturb(Bnet current,int nb){
	int i,nNode;
	boolean ok;
        Node nodeTail,nodeHead,nodei,nodej;
        Link link,link1,link2;
	Vector acc = null;
	
        System.out.println("Perturbando......con vecindad: "+nb);
	
        
	for(i=0 ; i<nb ; i++){
            double next = generator.nextDouble();
            if(next >= 0.7){
		link = (Link)current.getLinkList().elementAt(
		(int)(generator.nextDouble()*current.getLinkList().size()));
		next = generator.nextDouble();
		if(next >= 0.5){
		    try{
			current.removeLink(link);
		    }catch(InvalidEditException iee){};
		}else{
		    ok=true;
		    while (ok) {
			ok = false;
			acc = new Vector();
			nodeHead = link.getHead();
			nodeTail = link.getTail();
			try{
			    current.removeLink(link);
			}catch(InvalidEditException iee){};
			acc = current.directedDescendants(nodeTail);
			if(acc.indexOf(nodeHead) == -1){
			    try{
				current.createLink(nodeHead,nodeTail);
			    }catch(InvalidEditException iee){};
			}else {
			    try{
				current.createLink(nodeTail,nodeHead);
			    }catch (InvalidEditException iee){};
			    link = (Link)current.getLinkList().elementAt(
			    (int)(generator.nextDouble()*current.getLinkList().size()));
			    ok = true;
			}
		    }
		}
            }else{
		ok = true;
		while (ok){
		    nNode = (int)(generator.nextDouble()*current.getNodeList().size());
		    nodei = (Node) current.getNodeList().elementAt(nNode);
		    nNode = (int)(generator.nextDouble()*current.getNodeList().size());
		    nodej = (Node) current.getNodeList().elementAt(nNode);
		    if(!nodei.equals(nodej)){
			link1= current.getLink(nodei,nodej);
			link2= current.getLink(nodej,nodei);
			if((link1 == null) && (link2 == null)){
			    ok = false;
			    acc = new Vector();
			    acc = current.directedDescendants(nodej);
			    if(acc.indexOf(nodei) == -1){
				try{
				    current.createLink(nodei,nodej);
				}catch(InvalidEditException iee){};
			    }else{
				try{
				    current.createLink(nodej,nodei);
				}catch(InvalidEditException iee){};
			    }
			}else ok = true;
		    }else ok = true;
		}
	    } 
        }                       
	
	return current;
	
    }



    private int maxParents(Bnet current){
	int max = 0;

	for(int i=0; i< current.getNodeList().size(); i++){
	    Node nodei = (Node) current.getNodeList().elementAt(i);
	    int npa = current.parents(nodei).size();
	    if(npa > max)
		max = npa;
	}
	return max;

    }

    public void setInitialBnet(Bnet initial){
	NodeList nodes;
	Node nodei,nodeTail,nodeHead;
	Link link;
	int i;
	nodes = getInput().getNodeList();
	nodes.intersectionNames(initial.getNodeList());
	for(i=0 ; i< nodes.size(); i++){
	    nodei = (Node)nodes.elementAt(i);
	    try{
		initialBnet.addNode(nodei);
	    }catch(InvalidEditException iee){};
	}
	for(i=0 ; i< initial.getLinkList().size() ; i++){
	    link = (Link) initial.getLinkList().elementAt(i);
	    nodeTail = nodes.getNode(link.getTail().getName());
	    nodeHead = nodes.getNode(link.getHead().getName());
	    try{
		initialBnet.createLink(nodeTail,nodeHead);
	    }catch(InvalidEditException iee){};
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

    public Bnet getInitialBnet(){
	return initialBnet;
    }

    public void setInput(DataBaseCases db){
	input = db;
    }

    public void setMetric(BICMetrics metric){
	this.metric = metric;
    }
    public DataBaseCases getInput(){
	return input;
    }
    
} // HCSTWIMAPR.java











