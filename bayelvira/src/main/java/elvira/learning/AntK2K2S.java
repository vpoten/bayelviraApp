package elvira.learning;

/**
 * AntK2S.java
 *
 *
 * Created: Thu Jun  1 09:11:50 2000
 *
 * @author J. M. Puerta
 * @version
 */
import java.util.*;
import elvira.*;
import elvira.database.*;
import java.io.*;

public class AntK2K2S implements Runnable {

    Double feromone[][];
    double feromone_0[][];
    NodeList variables;
    DataBaseCases cases;
    Metrics metric;
    double rho;
    double beta;
    double q0;
    int radius;
    double fitness[];
    int index[];
    Graph dag;
    Random generator;

    public AntK2K2S() {
	
    }
    
    public AntK2K2S(Double mf[][],double[][] mf0,NodeList vars,DataBaseCases cases,Metrics mt, double rho, double beta, double q0,int
radius,Random generator){
	feromone = mf;
	feromone_0 = mf0;
	variables = vars.duplicate();
	this.cases = cases;
	metric = mt;
	this.rho = rho;
	this.beta = beta;
	this.q0 = q0;
	this.radius = radius;
        this.generator = generator;
	fitness = new double[variables.size()];
	index = new int[variables.size()];
	dag = new Graph(0);
	for(int i=0 ; i < variables.size() ; i++){
	    try{
		dag.addNode(variables.elementAt(i));
	    }catch(InvalidEditException iee){};
	}
    }


    public void run(){

	int i = 0;
	int last,current;
	Node xS,nodeXs;
	NodeList vars,nodesPaXs,nodesPaNodeXs;
	NodeList nodesVisited = new NodeList();
	NodeList nodesToVisit = variables.copy();

	xS = begin();
	//System.out.println("Nodo: "+xS.getName());
	nodesVisited.insertNode(xS);
	last = variables.getId(xS);
	localUpdate(last,last);
	//System.out.println("Indice: "+last);
	index[i++] = last;
	nodeXs = cases.getNodeList().getNode(xS.getName());
	vars = new NodeList();
	vars.insertNode(nodeXs);
	fitness[last] = metric.score(vars);
	nodesToVisit.removeNode(xS);
	while(nodesToVisit.size()>0){
            //System.out.println(nodesToVisit.toString2());
	    nodesPaXs = new NodeList();
	    xS = nextNode(xS,nodesToVisit,nodesVisited,nodesPaXs);
	    //System.out.println("siguiente: "+xS.getName());
	    nodesToVisit.removeNode(xS);
	    nodesVisited.insertNode(xS);
	    current = variables.getId(xS);
	    nodeXs = cases.getNodeList().getNode(xS.getName());
	    vars = new NodeList();
	    vars.insertNode(nodeXs);
	    nodesPaNodeXs = cases.getNodeList().intersectionNames(nodesPaXs);
	    vars.join(nodesPaNodeXs);
	    fitness[current] = metric.score(vars);
	    createParents(xS,nodesPaXs);
	    //System.out.println("Con indice: "+current);
	    index[i++] = current;
	    localUpdate(last,current);
	}
	
	//System.out.println("este es el orden que he sacado: "+nodesVisited.toString2());
	//k2Search(nodesVisited);
        //System.out.println("Esta es la red que he aprendido: "+dag.getLinkList().toString());
    }


    private void createParents(Node nodeXi, NodeList nodesPa){

	Node n = variables.getNode(nodeXi.getName());
	for(int i=0 ; i< nodesPa.size() ; i++){
	    Node nodepa = variables.getNode(nodesPa.elementAt(i).getName());
	    try{
		dag.createLink(nodepa,n);
	    }catch(InvalidEditException iee){};
	}
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
		node=(FiniteStates)cases.getNodeList().getNode(node.getName());
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

    private void localUpdate(int last, int current){
	synchronized(feromone[last][current]){
	    feromone[last][current] = new Double( 
		((1-rho)*(feromone[last][current].doubleValue()))+
		(rho*feromone_0[last][current]));
	}
    }

    private Node nextNode(Node node,NodeList nodesToVisit,
			  NodeList nodesSorted,NodeList nodesPa){
	int i,posi;
	double val = 0.0;
	double[] pij;
	double max = (-1.0/0.0);
	
	Node nodeR = null;
	NodeList nodesR, nodes;
	nodesR=null;
	int pos = variables.getId(node);
	double q = (double) generator.nextDouble();
	//System.out.println("Exploro o no ?" +q);
	if(q <= q0){
	    for(i=0 ; (i< nodesToVisit.size()) && (i<radius) ; i++){
		posi = variables.getId(nodesToVisit.elementAt(i));
		nodes = new NodeList();
                //double valh = heuristic(nodes,posi,nodesSorted);
                //double valf = (feromone[pos][posi].doubleValue());
                //System.out.println("Nodo: "+i+" Valor (h*f): "+valh+" * "+valf);
		val=(feromone[pos][posi].doubleValue())*
		    heuristic(nodes,posi,nodesSorted);
                //System.out.println("Resultado: "+val+" con max: "+max);
                //try{System.in.read();}catch(IOException e){};
		if(val > max){
		    max = val;
		    nodeR = nodesToVisit.elementAt(i);
		    nodesR = nodes;
		    
		}
	    }
	}else{
	    Vector nodelistList = new Vector();
	    pij = probability(pos,nodesToVisit,nodelistList,nodesSorted);
	    q = (double) generator.nextDouble();
	    for(i=0 ; i< pij.length ; i++){
		//System.out.println(pij[i]);
		val+= pij[i];
		if(q<=val){
		    nodeR = nodesToVisit.elementAt(i);
		    nodesR = (NodeList) nodelistList.elementAt(i);
		    break;
		}
	    }
	}
	nodesPa.join(nodesR);
	return nodeR;
    }

    private double heuristic (NodeList nl, int i,NodeList nodesSorted){

	double fitnessOld,fitnessNew;
	NodeList vars,PaXi;
	boolean OkToProceed;
	FiniteStates nodeXi,nodeZ = null;
	PaXi = new NodeList();
	nodeXi = (FiniteStates) variables.elementAt(i);
	nodeXi = (FiniteStates) cases.getNodeList().getNode(nodeXi.getName());
	vars = new NodeList();
	vars.insertNode(nodeXi);
	fitnessOld = metric.score(vars);
	OkToProceed = true;
	while(OkToProceed){
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
	fitnessOld=(1.0/Math.abs(fitnessOld));
	return (Math.pow(fitnessOld,beta));
    }

    private double[] probability (int pos, NodeList nodesToVisit,Vector nlList,
				  NodeList nodesSorted){
	NodeList nl;
	int i,posi;
	double val[];
	double cte = 0.0;
	if(radius > nodesToVisit.size())
	     val = new double[nodesToVisit.size()];
	else val = new double[radius];
        for(i=0 ; i< val.length ; i++){
	    posi = variables.getId(nodesToVisit.elementAt(i));
	    nl = new NodeList();
	    val[i] = (feromone[pos][posi].doubleValue())*heuristic(nl,posi,
							      nodesSorted);
	    cte+=val[i];
	    nlList.addElement(nl);
	}
	for(i=0 ; i< val.length ; i++){
	    val[i]/=cte;
        }
	return val;
    }

    private Node begin(){
	
	Node nodeR = null;
	int i;
	
	double val;
	double max = (-1.0/0.0);
	double cte = 0.0;
	double q = (double) generator.nextDouble();
        //System.out.println("Exploro no no para el primero nodo: "+q+" <= "+q0);
	if(q<=q0){
	    for(i=0 ; i < variables.size() ; i++){
		NodeList vars = new NodeList();
		Node xi = cases.getNodeList().getNode(variables.elementAt(i).getName());
                //System.out.println("Nodo: "+xi.getName());
                vars.insertNode(xi);
                val = Math.pow((1.0/Math.abs(metric.score(vars))),beta);
                //System.out.println("Valor metrica: "+val);
		double val2= (feromone[i][i].doubleValue());
                //System.out.println("Valor fero: "+val2);
                val*=val2;
                //System.out.println("Valor final: "+val+"  Max actual: "+max);
                //try{System.in.read();}catch(IOException e){};
		if(val > max){
		    max = val;
		    nodeR = variables.elementAt(i);
		}
	    }
	}
	else{
	    double[] p = new double[variables.size()];
	    for(i=0 ; i < variables.size() ; i++){
                NodeList vars = new NodeList();
                Node xi = cases.getNodeList().getNode(variables.elementAt(i).getName());
                vars.insertNode(xi);
                val = Math.pow((1.0/Math.abs(metric.score(vars))),beta);
		val*= (feromone[i][i].doubleValue());
		p[i] = val;
		cte+= val;
	    }
	    for(i=0 ; i < variables.size() ; i++){
		p[i]/=cte;
	    }
	    val = 0.0;
	    q = (double) generator.nextDouble();
	    for(i=0 ; i< p.length ; i++){
		//System.out.println(pij[i]);
		val+= p[i];
		if(q<=val){
		    nodeR = variables.elementAt(i);
		    break;
		}
	    }
	}
	return nodeR;
    }
    
} // AntK2S
