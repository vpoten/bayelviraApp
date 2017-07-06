package elvira.learning;
/**
 * ThVNSSN.java
 *
 *
 * Created: Mon Jun  5 16:01:21 2000
 *
 * @author J. M. Puerta
 * @version
 */

import elvira.*;
import elvira.database.*;
import java.util.*;

public class ThVNSSN implements Runnable {

    NodeList variables;
    DataBaseCases cases;
    Metrics metric;
    int radius;
    int initialRadius;
    int maxNb = 1;
    int nb = 1;
    double fitness[];
    int index[];
    Graph dag;
    double it = 0.0;
    boolean found = false;
    int swapi,swapj;
    int numberProc;
    int indiv;
    Random generator;
    double localMaxAverage = 0.0;
    double numberIterationsForMaximun = 0.0;
    double numberIndividualsEvaluated = 0.0;



    public ThVNSSN() {
	
    }

    public ThVNSSN (NodeList vars, DataBaseCases cases, Metrics mt,int radius,
		    int[] maxIndex, Graph maxBnet, double[] maxFitness,int maxNb,
		    int Proc,int Indiv,Random generator){
	int i;
	variables = vars.duplicate();
	this.cases = cases;
	metric = mt;
	this.numberProc = Proc;
	this.indiv = Indiv;
	this.maxNb = maxNb;
	this.radius = radius;
	this.initialRadius = radius;
        this.generator = generator;
	fitness = new double[vars.size()];
	System.arraycopy(maxFitness,0,fitness,0,fitness.length);
	index = new int[vars.size()];
	System.arraycopy(maxIndex,0,index,0,index.length);
	dag = new Graph(0);
	
	for(i=0 ; i < variables.size() ; i++){
	    try{
		dag.addNode(variables.elementAt(i));
	    }catch(InvalidEditException iee){};
	}
	for(i=0 ; i< maxBnet.getLinkList().size(); i++){
	    try{
		Link link = maxBnet.getLinkList().elementAt(i);
		Node nodeT = variables.getNode(link.getTail().getName());
		Node nodeH = variables.getNode(link.getHead().getName());
		dag.createLink(nodeT,nodeH);
	    }catch(InvalidEditException iee){};
	}
    }


    public ThVNSSN (int nb, int maxNb, NodeList vars,
		    DataBaseCases cases, Metrics mt, 
		    int radius,int[] maxIndex,Graph maxBnet,double[] maxFitness,
		    int Proc,int Indiv,Random generator){
	int i;
	this.numberProc = Proc;
	this.indiv = Indiv;
	variables = vars.duplicate();
	this.cases = cases;
	metric = mt;
	this.maxNb = maxNb;
	this.radius = radius;
	this.initialRadius = radius;
        this.generator = generator;
	fitness = new double[vars.size()];
	System.arraycopy(maxFitness,0,fitness,0,fitness.length);
	index = new int[vars.size()];
	System.arraycopy(maxIndex,0,index,0,index.length);
	dag = new Graph(0);
	
	for(i=0 ; i < variables.size() ; i++){
	    try{
		dag.addNode(variables.elementAt(i));
	    }catch(InvalidEditException iee){};
	}
	for(i=0 ; i< maxBnet.getLinkList().size(); i++){
	    try{
		Link link = maxBnet.getLinkList().elementAt(i);
		Node nodeT = variables.getNode(link.getTail().getName());
		Node nodeH = variables.getNode(link.getHead().getName());
		dag.createLink(nodeT,nodeH);
	    }catch(InvalidEditException iee){};
	}
	int[] currentIndex = disturb(nb,index);
	getIndexChanged(currentIndex,index);
	double[] currentFitness = learn(swapi,swapj,currentIndex,dag);
	System.arraycopy(currentFitness,0,fitness,0,fitness.length);
	System.arraycopy(currentIndex,0,index,0,index.length);
	
    }

    public void run(){

	boolean OkToProceed;
	Graph currentGraph,maxDag;
	int[] currentIndex;
	int[] maxIndex;
	double[] currentFitness;
	double[] maxFitness;
	double fitnessNew,fitnessOld;
	int i;
	maxFitness = new double[variables.size()];
	maxIndex = new int[variables.size()];
	maxDag = dag.duplicate();
	System.arraycopy(fitness,0,maxFitness,0,fitness.length);
	System.arraycopy(index,0,maxIndex,0,index.length);
	//currentIndex = disturb(nb,index);
	//getIndexChanged(currentIndex,index);
	//fitness = learn(swapi,swapj,currentIndex,(Graph)dag);
	//System.arraycopy(currentIndex,0,index,0,index.length);
	
	
	boolean stop = false;
	while (!stop){
	    OkToProceed = true;
	    while (OkToProceed) {
		currentGraph = (Graph) dag.duplicate();
		currentIndex = maxScore(index,currentGraph);
		if(found){
		    getIndexChanged(currentIndex,index);
		    //System.out.println(".................... ");
		    //print(index);
		    //System.out.println(" ");
		    //print(currentIndex);
		    //System.out.println(" ");
		    //System.out.println("Estos son los indices ..........");
		    currentFitness = learn(swapi,swapj,currentIndex,currentGraph);
		    fitnessOld = fitnessNew = 0.0;
		    for(i=0 ; i< fitness.length ; i++){
			fitnessOld += fitness[i];
			fitnessNew += currentFitness[i];
		    } 
		    System.out.println("\nF:["+numberProc+":"+indiv+"]\t"+fitnessNew);
		    if(fitnessNew > fitnessOld){
			System.arraycopy(currentFitness,0,fitness,0,
					 fitness.length);
			System.arraycopy(currentIndex,0,index,0,
					 index.length);
			copy(currentGraph,dag);
		    }else OkToProceed = false;
		  
		}else OkToProceed = false; 
	    }
	    it++;
	    localMaxAverage+=eval(fitness);
	    System.out.println("Maximo Local: "+eval(fitness));
	    System.out.println("Maximo Global: "+eval(maxFitness));
	    if(eval(fitness)> eval(maxFitness)){
		System.out.print("Mejora  ["+numberProc+":"+indiv+"] con radio: "+radius+" y vecindad: "+nb);
		nb = 1;
		numberIterationsForMaximun = it;
		radius = initialRadius;
		System.out.println("Nueva Vecindad :"+nb);
		System.arraycopy(index,0,maxIndex,0,index.length);  
		index = disturb(nb,maxIndex,dag);
		getIndexChanged(index,maxIndex);
		copy(dag,maxDag);
		System.arraycopy(fitness,0,maxFitness,0,maxFitness.length);
		currentFitness = learn(swapi,swapj,index,(Graph)dag);
		System.out.println("NF:["+numberProc+":"+indiv+"]\t"+eval(currentFitness));  
		System.arraycopy(currentFitness,0,fitness,0,fitness.length);
	    }else{
		nb++;
		radius++;
		if(nb <= maxNb){
		    System.out.println("No mejora ["+numberProc+":"+indiv+"].Vecindad :"+nb+" Radio: "+radius);
		    index = disturb(nb,maxIndex,maxDag);
		    getIndexChanged(index,maxIndex);
		    System.arraycopy(maxFitness,0,fitness,0,maxFitness.length);
		    copy(maxDag,dag);
		    currentFitness = learn(swapi,swapj,index,(Graph)dag);
		    System.out.print("NF:["+numberProc+":"+indiv+"]\t"+eval(currentFitness)); 
		    System.arraycopy(currentFitness,0,fitness,0,fitness.length);
		}
	    }
	    if(nb > maxNb) stop = true;
	}
	System.arraycopy(maxFitness,0,fitness,0,fitness.length);
	System.arraycopy(maxIndex,0,index,0,index.length);
	copy(maxDag,dag);
	System.out.println("Fin del Indiv: "+numberProc+":"+indiv+" ---> "+eval(fitness));
    }   
    
    private void getIndexChanged(int[] index, int[] currentIndex){

	int i=0;

	while((i<currentIndex.length)&&(index[i] == currentIndex[i])) i++;
	if(i==currentIndex.length){
	    swapi=swapj=0;
	    return;
	}
	swapi = i;
	for(i= swapi ; i< currentIndex.length ; i++){
	    if(index[i] != currentIndex[i]) swapj = i;
	}

    }

    private int[] disturb(int nb, int[] currentIndex){

	
	int[] tmp;
	tmp  = new int[currentIndex.length];
	boolean stop = false;
	int aux,indi,indj,i;
	indi=indj=0;
	System.arraycopy(currentIndex,0,tmp,0,tmp.length);
	for(i=0 ; i<nb ; i++){
	    stop = false;
	    while(!stop){
		stop = true;
		indi = (int)(generator.nextDouble()*tmp.length);
		indj = (int)(generator.nextDouble()*tmp.length);
		if(indi==indj) stop = false;
	    }
	    aux = tmp[indj];
	    tmp[indj]=tmp[indi];
	    tmp[indi]=aux;
	}
	
	return tmp;

    }
    
    private int[] disturb(int nb, int[] currentIndex,Graph currentGraph){
	
	int[] tmp;
	Node nodeIndj,nodeIndi;
	NodeList paNodeIndi;
        Vector directDescNodeIndi;
	tmp  = new int[currentIndex.length];
	boolean stop = false;
	int aux,indi,indj,i;
	indi=indj=0;
	System.arraycopy(currentIndex,0,tmp,0,tmp.length);
	for(i=0 ; i<nb ; i++){
	    stop = false;
	    while(!stop){
		stop = true;
		indi = (int)(generator.nextDouble()*tmp.length);
		nodeIndi = variables.elementAt(indi);
		nodeIndi = currentGraph.getNodeList().getNode(nodeIndi.getName());
		directDescNodeIndi = new Vector();
		directDescNodeIndi = currentGraph.directedDescendants(nodeIndi);
		if(directDescNodeIndi.size()>0){
		    indj = (int)(generator.nextDouble()*directDescNodeIndi.size());
		    nodeIndj = (Node) directDescNodeIndi.elementAt(indj);
		    indj = variables.getId(nodeIndj.getName());
		}else{
		    paNodeIndi = currentGraph.parents(nodeIndi);
		    if(paNodeIndi.size()>0){
			indj = (int)(generator.nextDouble()*paNodeIndi.size());
			nodeIndj = paNodeIndi.elementAt(indj);
			indj = variables.getId(nodeIndj.getName());
		    }else stop = false;
		}
		if(indi==indj) stop = false;
	    }
	    aux = tmp[indj];
	    tmp[indj]=tmp[indi];
	    tmp[indi]=aux;
	}
	
	return tmp;
	
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
	    }catch(InvalidEditException iee){};
	}
	
    }

    public double[] learn(int si,int sj,int[] index,Graph currentBnet){

	int i,ind;
	double[] Rfitness = new double[index.length];
  
     //System.out.println("Estoy en learn intercambiando: "+si+" "+sj+" con el index: ");
     //print(index);

	for(i = 0 ; i< si ; i++){
	    ind = index[i];
	    Rfitness[ind] = fitness[ind];
	}
	for(i = si ; i<= sj ; i++){
	    ind = index[i];
	    Rfitness[ind] = learn(i,currentBnet,index);
	}
	for(i = sj+1 ; i<index.length ; i++){
	    ind = index[i];
	    Rfitness[ind] = fitness[ind];
	}
     
	return Rfitness;
    }

    public double learn(int ind, Graph currentBnet, int[] index){

	Node node,nodeCG,nodePa,nodeMax;
	NodeList pa,fixedPa;
	NodeList vars;
	Link link;
	int i,j,pos;
	boolean found,OkToProceed;
	double value,max;

	node = (Node) variables.elementAt(index[ind]);
	node = (Node) cases.getNodeList().getNode(node.getName());
	nodeCG = currentBnet.getNodeList().getNode(node.getName());
	pa = currentBnet.parents(nodeCG);
	fixedPa = new NodeList();
	for(i=0 ; i< pa.size(); i++){
	    nodePa = (Node) pa.elementAt(i);
	    pos = variables.getId(nodePa.getName());
	    found = false;
	    for(j=0 ; j<= ind ; j++){
		if(pos == index[j]){
		    found = true;
		    break;
		}    
	    }
	    found = false; // Esto es para que quite todos los padres siempre.
	    if(!found){
		try{
		    link = currentBnet.getLinkList().
			getLinks(nodePa.getName(),node.getName());
		    currentBnet.removeLink(link);
		}catch(InvalidEditException iee){};
	    } else fixedPa.insertNode(nodePa);
	}
	fixedPa = cases.getNodeList().intersectionNames(fixedPa);
	vars = new NodeList();
	vars.insertNode(node);
	vars.join(fixedPa);     
	max = metric.score(vars);
     //System.out.println("Estoy en learn con el indice: "+ind+" Del nodo: "+node.getName());
     //System.out.println("y los padres son: "+fixedPa.toString2());
     //try{System.in.read();}catch(IOException e){};
	if(ind == 0) return max;

	OkToProceed = true;
	while (OkToProceed) {
	    pa = currentBnet.parents(nodeCG);
	    pa = cases.getNodeList().intersectionNames(pa);
	    nodeMax = maxScore(ind,index,pa);
	    link = currentBnet.getLinkList().
		getLinks(nodeMax.getName(),node.getName());
	    if(link == null){
		vars = new NodeList();
		vars.insertNode(node);
		vars.join(pa);
		vars.insertNode(nodeMax);
		value = metric.score(vars);
		if(value > max){
		    max = value;
		    try{
			nodeMax = currentBnet.getNodeList().
			    getNode(nodeMax.getName());
			currentBnet.createLink(nodeMax,nodeCG);
		    }catch(InvalidEditException iee){};
		}else OkToProceed = false;
	    }else{
		pa.removeNode(nodeMax);
		vars = new NodeList();
		vars.insertNode(node);
		vars.join(pa);
		value = metric.score(vars);
		if(value > max){
		    max = value;
		    try{
			currentBnet.removeLink(link);
		    }catch(InvalidEditException iee){};
		} else OkToProceed = false;
	    }
	}
	return max;
    }

    public Node maxScore(int ind,int[] index,NodeList parents){ 

	double max = (-1.0/0.0);
	int i,pos;
	Node nodeInd,node,Rnode;
	NodeList vars;
	double value;

	Rnode = null;
	nodeInd = variables.elementAt(index[ind]);
	nodeInd = cases.getNodeList().getNode(nodeInd.getName());
     //System.out.println("Estoy maximizando con el nodo: "+nodeInd.toString());
     //System.out.println("Con padres : "+parents.toString2());
	for(i=0; i< ind ; i++){
	    pos = index[i];
	    node = variables.elementAt(pos);
	    node = cases.getNodeList().getNode(node.getName());
	    if((parents.getId(node) == -1)){
		vars = new NodeList();
		vars.insertNode(nodeInd);
		vars.join(parents);
		vars.insertNode(node);
		value = metric.score(vars);
	    }else{
		vars = new NodeList();
		vars.insertNode(nodeInd);
		vars.join(parents);
		vars.removeNode(node);
		value = metric.score(vars);
	    }
	    if(value > max){
		max = value;
		Rnode = node;
	    }
	}                       
	return Rnode;
    }

    public int[] maxScore(int[] currentIndex, Graph currentBnet){

	double max = (-1.0/0.0);//0.0;
	double[] values;
	int i,j,h,tmp,rd;
	int[] tmpIndex = new int[currentIndex.length];
	int[] RIndex = new int[currentIndex.length];
	double fitnessNew;
	found = false;
	//for(i=0 ; i<fitness.length ; i++) max+=fitness[i];

	System.arraycopy(currentIndex,0,tmpIndex,0,currentIndex.length);

	for(i=0 ; i< tmpIndex.length -1 ; i++)
	    for(j=i+1,rd=0; (j< tmpIndex.length)&&(rd < radius) ; j++,rd++){
		tmp = tmpIndex[i];
		tmpIndex[i] = tmpIndex[j];
		tmpIndex[j] = tmp;
		numberIndividualsEvaluated++;
		Graph currentGraph = currentBnet.duplicate();
		values = learn(i,j,tmpIndex,currentGraph);
		fitnessNew=0.0;
		for(h=0 ; h< values.length ; h++){
		    fitnessNew +=values[h];
		}
		//System.out.print("\nOrden: ");
		//print(tmpIndex);
		//System.out.print(": "+fitnessNew);
		if(fitnessNew > max){
		    //System.out.print("\b\b\b\b\b\b\b[*"+numberProc+":"+indiv+"]"+(char)(Math.random()*24));
		    max = fitnessNew;
		    System.arraycopy(tmpIndex,0,RIndex,0,tmpIndex.length);
		    found = true;   
		}
		tmp = tmpIndex[i];
		tmpIndex[i] = tmpIndex[j];
		tmpIndex[j] = tmp;
		//if(found) return RIndex;
	    }
	return RIndex;
    }
    
    public int[] maxScoreSt(int[] currentIndex, Graph currentBnet){

	double max = (-1.0/0.0);//0.0;
	double[] values;
	int i,j,h,tmp,rd;
	int[] tmpIndex = new int[currentIndex.length];
	int[] RIndex = new int[currentIndex.length];
	double fitnessNew;
	Node nodei,nodej;
	found = false;
	//for(i=0 ; i<fitness.length ; i++) max+=fitness[i];

	System.arraycopy(currentIndex,0,tmpIndex,0,currentIndex.length);

	for(i=0 ; i< tmpIndex.length -1 ; i++)
	    for(j=i+1; (j< tmpIndex.length); j++){
		nodei = variables.elementAt(tmpIndex[i]);
		nodej = variables.elementAt(tmpIndex[j]);
		nodei = currentBnet.getNodeList().getNode(nodei.getName());
		nodej = currentBnet.getNodeList().getNode(nodej.getName());
		Vector desc = currentBnet.directedDescendants(nodei);
		if(desc.indexOf(nodej)!= -1){
		    tmp = tmpIndex[i];
		    tmpIndex[i] = tmpIndex[j];
		    tmpIndex[j] = tmp;
		    Graph currentGraph = currentBnet.duplicate();
		    values = learn(i,j,tmpIndex,currentGraph);
		    fitnessNew=0.0;
		    for(h=0 ; h< values.length ; h++){
			fitnessNew +=values[h];
		    }
		    //System.out.print("\nOrden: ");
		    //print(tmpIndex);
		    //System.out.print(": "+fitnessNew);
		    if(fitnessNew > max){
			System.out.print("[*"+numberProc+":"+indiv+"]");
			max = fitnessNew;
			System.arraycopy(tmpIndex,0,RIndex,0,tmpIndex.length);
			found = true;   
		    }
		    tmp = tmpIndex[i];
		    tmpIndex[i] = tmpIndex[j];
		    tmpIndex[j] = tmp;
		    //if(found) return RIndex;
		}
	    }
	return RIndex;
    }

    public double eval(double[] fit){
	double sum = 0.0;
	for (int i = 0 ; i< fit.length ; i++){
	    sum+=fit[i];
	}
	return sum;
    }
    
    public void print(int[] index){
	for(int i=0 ; i< index.length ; i++)
	    System.out.print("  "+variables.elementAt(index[i]).getName());

    }

    public double getLocalMaxAverage(){
	return localMaxAverage;
    }

    public double getNumberOfIterationsForMaximun(){
	return numberIterationsForMaximun;
    }

    public double getNumOfIndEval(){
	return numberIndividualsEvaluated;
    }
	
} // thVNSSN
