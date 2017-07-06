package elvira.learning;

import java.util.Random;
import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * ThVNSST.java
 *
 *
 * Created: Sun Jan  9 14:11:29 2000
 *
 * @author P.Elvira
 * @version 1.0
 */

public class ThVNSST implements Runnable {
    
    NodeList variables;
    DataBaseCases cases;       // The cases for the input algorithm.
    int numberMaxOfParents;    // The number of maximal parents for each node.
    Metrics metric;            // The metric for scoring.
    Graph dag;
    double maxFitness;
    int maxNb=0;
    double localMaxAverage = 0.0;
    double numberIterationsForMaximun = 0.0;
    double numIndEval = 0.0;
    double it = 0.0;
    int indiv;
    int nProc;
    Random generator;

    public ThVNSST(){

    }

    public ThVNSST(NodeList vars, DataBaseCases cases,Metrics met,Graph maxBnet
		   ,double maxFitness,int maxNb,int nProc, int nIndiv,Random generator){
	
	int i;
	this.variables = vars.duplicate();
	this.dag = new Graph(0);
        this.generator = generator;
	for(i=0; i< variables.size(); i++){
	    try{
		dag.addNode(variables.elementAt(i));
	    }catch(InvalidEditException iee){};
	}
	this.cases = cases;
	this.metric = met;
	this.maxNb = maxNb;
	this.indiv = nIndiv;
	this.nProc = nProc;
	this.maxFitness = maxFitness;
	for(i=0 ; i< maxBnet.getLinkList().size(); i++){
	    try{
		Link link = maxBnet.getLinkList().elementAt(i);
		Node nodeT = variables.getNode(link.getTail().getName());
		Node nodeH = variables.getNode(link.getHead().getName());
		dag.createLink(nodeT,nodeH,true);
	    }catch(InvalidEditException iee){};
	}
    } 
    
    /**
     * This methods implements a H_C(DAGS) algorithm.
     */

    public void run(){

	NodeList vars,pa;
	double fitness,fitnessNew;
	boolean OkToProceed,stop;
	Link newLink,link;
	Bnet currentBnet = new Bnet();
	int op;
	int nb = 7;
	Graph maxBnet;
        double maxBnetFitness;
	Graph auxGraph = null;
	
	auxGraph = dag.duplicate();
	currentBnet = new Bnet();
	currentBnet.setNodeList(auxGraph.getNodeList());
	currentBnet.setLinkList(auxGraph.getLinkList());
	fitness = maxFitness;
	maxBnet = (Graph) currentBnet.duplicate();
	maxBnetFitness = fitness;
	System.out.println("fitness inicial["+nProc+","+indiv+"]:"+fitness+"   "+metric.getTotalStEval());
	stop = false;
	while(!stop){ 
	    OkToProceed = true;
	    while(OkToProceed ){
                //try{System.in.read();}catch(IOException e){};
		Vector vlink=new Vector();
		Vector vvars=new Vector();
		op = maxScore(vlink,vvars,currentBnet);
		link = (Link)vlink.elementAt(0);
		vars = (NodeList) vvars.elementAt(0);
		if(op!=-1){
		    if(op == 0){
			fitnessNew = metric.score(vars);
			vars = new NodeList();
			vars.insertNode(link.getHead());
			pa = currentBnet.parents(link.getHead());
			vars.join(pa);
			vars = cases.getNodeList().intersectionNames(vars).
			    sortNames(vars);
			fitness = metric.score(vars);
			if(fitnessNew > fitness){
			    fitness = fitnessNew;
			    try{
				link = currentBnet.getLink(link.getTail().
				       getName(),link.getHead().getName());
				currentBnet.removeLink(link);
				double f = metric.score(currentBnet);
                                //System.out.println("Quito la Link: "+link.toString());
				System.out.println("F["+nProc+","+indiv+"]:"+f+"   "+metric.getTotalStEval());
			    }catch(InvalidEditException iee){};
			} else OkToProceed = false;
		    }else if(op == 1){
			fitnessNew = metric.score(vars);
			vars = new NodeList();
			vars.insertNode(link.getHead());
			pa = currentBnet.parents(link.getHead());
			pa.removeNode(link.getTail());
			vars.join(pa);
			vars = cases.getNodeList().intersectionNames(vars).
			    sortNames(vars);
			fitnessNew+=metric.score(vars);
			vars = new NodeList();
			vars.insertNode(link.getHead());
			pa = currentBnet.parents(link.getHead());
			vars.join(pa);
			vars = cases.getNodeList().intersectionNames(vars).
			    sortNames(vars);
			fitness = metric.score(vars);
			vars = new NodeList();
			vars.insertNode(link.getTail());
			pa = currentBnet.parents(link.getTail());
			vars.join(pa);
			vars = cases.getNodeList().intersectionNames(vars).
			    sortNames(vars);
			fitness+= metric.score(vars);
			if(fitnessNew > fitness){
			    fitness = fitnessNew;
			    try{
				newLink=currentBnet.getLink(link.getTail().
					getName(),link.getHead().getName());
				currentBnet.removeLink(newLink);
				currentBnet.createLink(link.getHead(),
						       link.getTail(),true);
				double f = metric.score(currentBnet);
                                //System.out.println("Invierto la Link: "+newLink.toString());
				System.out.println("F["+nProc+","+indiv+"]:"+f+"   "+metric.getTotalStEval());
			    }catch(InvalidEditException iee){};
			    
			}else OkToProceed = false;
		    }else
			if(op == 2){
			    fitnessNew = metric.score(vars);
			    vars = new NodeList();
			    vars.insertNode(link.getHead());
			    pa = currentBnet.parents(link.getHead());
			    vars.join(pa);
			    vars = cases.getNodeList().intersectionNames(vars).
				sortNames(vars);  
			    fitness = metric.score(vars);
			    if(fitnessNew > fitness){
				fitness = fitnessNew;
				try{
				    currentBnet.createLink(link.getTail(),
                                                           link.getHead(),true);
				    double f = metric.score(currentBnet);
                                    //System.out.println("Pongo la Link: "+link.toString());
				    System.out.println("F["+nProc+","+indiv+"]:"+f+"   "+metric.getTotalStEval());
				}catch(InvalidEditException iee){};
			    } else OkToProceed = false;
			    
			} else OkToProceed = false;   
		}else OkToProceed = false;
	    }
	    it++;
	    fitness = metric.score(currentBnet);
	    localMaxAverage+=fitness;
	    System.out.println("Fitness["+nProc+","+indiv+"] (Maximo Local): "+fitness);
	    System.out.println("Fitness["+nProc+","+indiv+"] (Maximo): "+maxBnetFitness);
	    if (fitness > maxBnetFitness ){
		System.out.println("P["+nProc+","+indiv+"] he encontrado otra mejor con vecindad: "+nb);
		maxBnetFitness = fitness;
		maxBnet = (Graph) currentBnet.duplicate();
		nb = 7;
		numberIterationsForMaximun = it;
		if(nb <= maxNb){
		    currentBnet = disturb(maxBnet,currentBnet,nb);
		    System.out.println("P["+nProc+","+indiv+"] Despues de perturbar..Inicio: "+metric.score(currentBnet));
		}
	    }
	    else{
		nb+=5;
		if(nb <= maxNb){
		    currentBnet = disturb(maxBnet,currentBnet,nb);
		    System.out.println("P["+nProc+","+indiv+"] Despues de perturbar..Inicio: "+metric.score(currentBnet));
		}
	    } 
	    if(nb > maxNb) stop = true;
	}
	dag = maxBnet;
	maxFitness = maxBnetFitness;
    }
    


    private Bnet disturb(Graph maxBnet,Bnet current,int nb){
	int i,nNode,iR;
        double p;
	boolean ok;
        Node nodeTail,nodeHead,nodei,nodej,nodep;
        Link link,link1,link2,linkT,linkH;
        NodeList paT,paC,paH;
	Vector acc = null;
	
        System.out.println("P ["+nProc+","+indiv+"] perturbando......con vecindad: "+nb);
	
        for(i=0 ;i< current.getNodeList().size(); i++){
            nodei = (Node)current.getNodeList().elementAt(i);
            nodei.setParents(new LinkList());
            nodei.setChildren(new LinkList());
            nodei.setSiblings(new LinkList());
        }
        current.setLinkList(new LinkList());
        for(i=0 ; i< maxBnet.getLinkList().size(); i++){
            link = (Link) maxBnet.getLinkList().elementAt(i);
            nodeTail = link.getTail();
            nodeHead = link.getHead();
            nodeTail = current.getNodeList().getNode(nodeTail.getName());
            nodeHead = current.getNodeList().getNode(nodeHead.getName());
            try{
                current.createLink(nodeTail,nodeHead,true);
            }catch(InvalidEditException iee){};
        }
	for(i=0 ; i<nb ; i++){
            double next = generator.nextDouble();
            if(next >= 0.66){
		link = (Link)current.getLinkList().elementAt(
		(int)(generator.nextDouble()*current.getLinkList().size()));
		next = generator.nextDouble();
		if(next >= 0.4){
		    try{
                       //System.out.println("Quitando la link: "+link.toString());         
                       nodeHead = link.getHead();
		       nodeTail = link.getTail();
                       paH = current.parents(nodeHead);
                       paT = current.parents(nodeTail);
                       paC = paH.intersection(paT);
                       if(paC.size() > 0){
                         p = generator.nextDouble();
                         if(p <= 0.75){
                           for(iR=0 ; iR < paC.size(); iR++){     
                             nodep = paC.elementAt(iR);
                             linkT = current.getLinkList().getLinks(nodep.getName(),nodeTail.getName());
                             linkH = current.getLinkList().getLinks(nodep.getName(),nodeHead.getName());
                             //System.out.println("Quito las Links: "+linkT.toString()+ " y "+linkH.toString());
                             //try{System.in.read();}catch(IOException e){};
                             current.removeLink(linkT);
                             current.removeLink(linkH);
                             i+=2;
                           }
                         }
                       }
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
                                //System.out.println("Invirtiendo la link: "+link.toString());
                                paH = current.parents(nodeHead);
                                paT = current.parents(nodeTail);
                                paC = paH.intersection(paT);
                                if(paC.size() > 0){
                                     p = generator.nextDouble();
                                     if(p <= 0.75){
                                        for(iR=0 ; iR < paC.size(); iR++){     
                                           nodep = paC.elementAt(iR);
                                           linkT = current.getLinkList().getLinks(nodep.getName(),nodeTail.getName());
                                           linkH = current.getLinkList().getLinks(nodep.getName(),nodeHead.getName());
                                           //System.out.println("Quito las Links: "+linkT.toString()+ " y "+linkH.toString());
                                           //try{System.in.read();}catch(IOException e){};
                                           current.removeLink(linkT);
                                           current.removeLink(linkH);
                                           i+=2;
                                         }
                                     }
                                }
				current.createLink(nodeHead,nodeTail,true);
                                i++;
			    }catch(InvalidEditException iee){};
			}else {
			    try{
				current.createLink(nodeTail,nodeHead,true);
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
				    current.createLink(nodei,nodej,true);
				}catch(InvalidEditException iee){};
			    }else{
				try{
				    current.createLink(nodej,nodei,true);
				}catch(InvalidEditException iee){};
			    }
			}else ok = true;
		    }else ok = true;
		}
	    } 
        }                       
	
	return current;
	
    }




    /**
     * This method is private. It is used for searching the link operation
     * that maximize the score metric.
     * @param FiniteStates nodei. the node.
     * @param NodeList pa. The actual parents set for the node i.
     * @param int index. The position for the node i.
     * @return int operation to be done. 0-delete, 1-invertion 2-add.
     */

    private int maxScore(Vector vlinkR, Vector vvarsR,Bnet current){

	int i,j,op=-1;
	Link link,linkR=null;
	FiniteStates nodei, nodej;
	NodeList vars,paNj,paNi,varsR=null;
	double val;
	double max = (-1.0/0.0);

	for(i=0; i<current.getNodeList().size(); i++){
	    nodei = (FiniteStates)current.getNodeList().elementAt(i);
	    for(j=0 ; j<current.getNodeList().size();j++){
		nodej = (FiniteStates)current.getNodeList().elementAt(j);
                //try{System.in.read();}catch(IOException e){};
		if(i!=j){
		    link = (Link)current.getLink(nodei.getName(),
						 nodej.getName());
		    if(link != null){
			numIndEval++;
			paNj = current.parents(nodej);
			vars = new NodeList();
			vars.insertNode(nodej);
			vars.join(paNj);
			vars = cases.getNodeList().intersectionNames(vars).
			    sortNames(vars);
			double valOldj = metric.score(vars);
			paNj.removeNode(nodei);
			vars = new NodeList();
			vars.insertNode(nodej);
			vars.join(paNj);
			vars = cases.getNodeList().intersectionNames(vars).
			    sortNames(vars);
			double valNewj = metric.score(vars);
			val = valNewj - valOldj;
                        //System.out.println("Probando quitar "+nodei.getName()+"-->"+nodej.getName());
                        //System.out.println("Valor: "+val);
			if(val> max){
			    max = val;
			    op = 0;
			    linkR = link;
			    varsR = new NodeList();
			    varsR.join(vars);
			}
                        try{
                          current.removeLink(link);
                        }catch(InvalidEditException iee){};
			Vector acc = new Vector();
	                acc = current.directedDescendants(link.getTail());
                        try{
                           current.createLink(link.getTail(),link.getHead(),true);
                        }catch(InvalidEditException iee){};
			if(acc.indexOf(link.getHead()) == -1){
			    numIndEval++;
			    paNi = current.parents(nodei);
			    vars = new NodeList();
			    vars.insertNode(nodei);
			    vars.join(paNi);
			    vars = cases.getNodeList().intersectionNames(vars).
				sortNames(vars);
			    double valOldi = metric.score(vars);
			    double valOld = valOldi + valOldj;
			    paNi.insertNode(nodej);
			    vars = new NodeList();
			    vars.insertNode(nodei);
			    vars.join(paNi);
			    vars = cases.getNodeList().intersectionNames(vars).
				sortNames(vars);
			    double valNewi = metric.score(vars);
			    double valNew = valNewi + valNewj;
			    val = valNew - valOld;
                            //System.out.println("Probando invertir "+nodei.getName()+"-->"+nodej.getName());
                            //System.out.println("Valor: "+val);
			    if(val > max){
				max = val;
				op = 1;
				linkR = link;
				varsR = new NodeList();
				varsR.join(vars);
			    }
                        }
		    }else{
                        Vector acc = new Vector();
	                acc = current.directedDescendants(nodej);
	                if(acc.indexOf(nodei) == -1){
			    numIndEval++;
			    paNj = current.parents(nodej);
			    vars = new NodeList();
			    vars.insertNode(nodej);
			    vars.join(paNj);
			    vars = cases.getNodeList().intersectionNames(vars).
				sortNames(vars);
			    double valOld = metric.score(vars);
			    paNj.insertNode(nodei);
			    vars = new NodeList();
			    vars.insertNode(nodej);
			    vars.join(paNj);
			    vars = cases.getNodeList().intersectionNames(vars).
				sortNames(vars);
			    double valNew = metric.score(vars);
			    val = valNew - valOld;
                            //System.out.println("Probando insertar "+nodei.getName()+"-->"+nodej.getName());
                            //System.out.println("Valor: "+val);
			    if(val > max){
				max = val;
				op = 2;
				linkR = new Link(nodei,nodej);
				varsR = new NodeList();
				varsR.join(vars);
			    }
			}
		    }
		}
	    }
	}
	vlinkR.addElement(linkR);
 	vvarsR.addElement(varsR);
	return op;
    }

    public double getLocalMaxAverage(){
	return localMaxAverage;
    }

    public double getNumberOfIterationsForMaximun(){
	return numberIterationsForMaximun;
    }
    
    public double getNumOfIndEval(){

	return numIndEval;
    }
    
    

}// ThVNSST
