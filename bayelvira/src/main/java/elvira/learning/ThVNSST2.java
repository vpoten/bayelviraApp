package elvira.learning;

import java.util.Random;
import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * ThVNSST2.java
 *
 *
 * Created: Sun Jan  9 14:11:29 2000
 *
 * @author P.Elvira
 * @version 1.0
 */

public class ThVNSST2 implements Runnable {
    
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

    public ThVNSST2(){

    }

    public ThVNSST2(NodeList vars, DataBaseCases cases,Metrics met,Graph maxBnet
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
		dag.createLink(nodeT,nodeH);
	    }catch(InvalidEditException iee){};
	}
    } 
    
    /**
     * This methods implements a H_C(DAGS) algorithm.
     */

    public void run(){

	NodeList vars,pa,vars2;
	double fitness,fitnessNew;
	boolean OkToProceed,stop;
	Link newLink,link;
	Bnet currentBnet = new Bnet();
	int op,ww;
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
	System.out.println("fitness inicial["+nProc+","+indiv+"]: "+fitness+"   "+metric.getTotalStEval());
	stop = false;
	while(!stop){ 
	    OkToProceed = true;
	    while(OkToProceed){
		//System.out.println("Una vuelta mas...");
                //try{System.in.read();}catch(IOException e){};
		Vector vlink=new Vector();
		Vector vvars=new Vector();
		op = maxScore(vlink,vvars,currentBnet);
		link = (Link)vlink.elementAt(0);
		vars = (NodeList) vvars.elementAt(0);
		vars2 = (NodeList) vvars.elementAt(1);
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
				System.out.println("F["+nProc+","+indiv+"]: "+f+"   "+metric.getTotalStEval());
			    }catch(InvalidEditException iee){};
			} else OkToProceed = false;
		    }else if(op == 1){
			if(vars2 == null){
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
							   link.getTail());
				    double f = metric.score(currentBnet);
				    //System.out.println("Invierto la Link: "+newLink.toString());
				    System.out.println("F["+nProc+","+indiv+"]: "+f+"   "+metric.getTotalStEval());
				}catch(InvalidEditException iee){};
				
			    }else OkToProceed = false;
			}else{
			    
			    fitnessNew = metric.score(vars);
			    vars = new NodeList();
			    vars.join(vars2);
			    vars = cases.getNodeList().intersectionNames(vars).
				sortNames(vars);
			    fitnessNew+=metric.score(vars);
			    //System.out.println("Nuevo fitness: "+fitnessNew);
			    vars = new NodeList();
			    vars.insertNode(link.getHead());
			    pa = currentBnet.parents(link.getHead());
			    vars.join(pa);
			    vars = cases.getNodeList().intersectionNames(vars).
				sortNames(vars);
			    fitness = metric.score(vars);
			    //System.out.println("Fitness anti con: "+vars.toString2());
			    vars = new NodeList();
			    vars.insertNode(link.getTail());
			    pa = currentBnet.parents(link.getTail());
			    vars.join(pa);
			    vars = cases.getNodeList().intersectionNames(vars).
				sortNames(vars);
			    fitness+= metric.score(vars);
			    //System.out.println("y: "+vars.toString2()+" "+fitness);
			    if(fitnessNew > fitness){
				fitness = fitnessNew;
				try{
				    newLink=currentBnet.getLink(link.getTail().
								getName(),link.getHead().getName());
				    currentBnet.removeLink(newLink);
				    //currentBnet.createLink(link.getHead(),
				    //link.getTail());
				    //System.out.println("Hasta aqui to va bien....");
				    vars = (NodeList)vvars.elementAt(0);
				    vars2 = (NodeList)vvars.elementAt(1);
				    vars.removeNode(vars.getId(link.getTail().getName()));
				    vars2.removeNode(vars2.getId(link.getHead().getName()));
				    NodeList paOldi = currentBnet.parents(link.getTail());
				    NodeList paOldj = currentBnet.parents(link.getHead());
				    //System.out.println("Pdres Nuevos: "+vars.toString2()+" ... "+vars2.toString2());
				    paOldi.removeNode(link.getHead());
				    //System.out.println("Pdres Antiguos: "+paOldj.toString2()+" ... "+paOldi.toString2());
				    for(ww = 0 ; ww < paOldi.size();ww++){
					newLink = currentBnet.getLink(paOldi.elementAt(ww).getName(),
								      link.getTail().getName());
					currentBnet.removeLink(newLink);
				    }
				    for(ww = 0 ; ww < paOldj.size();ww++){
					newLink = currentBnet.getLink(paOldj.elementAt(ww).getName(),
								      link.getHead().getName());
					currentBnet.removeLink(newLink);
				    }
				    for(ww = 0 ; ww < vars.size();ww++){
					Node nodep = currentBnet.getNode(vars.elementAt(ww).getName());
					currentBnet.createLink(nodep,link.getTail());
				    }
				    for(ww = 0 ; ww < vars2.size();ww++){
					Node nodep = currentBnet.getNode(vars2.elementAt(ww).getName());
					currentBnet.createLink(nodep,link.getHead());
				    }
				    double f = metric.score(currentBnet);
				    //System.out.println("Invierto la Link: "+newLink.toString());
				    System.out.println("F["+nProc+","+indiv+"]: "+f+"   "+metric.getTotalStEval());
				}catch(InvalidEditException iee){};
				
			    }else OkToProceed = false;
			}
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
							   link.getHead());
				    double f = metric.score(currentBnet);
                                    //System.out.println("Pongo la Link: "+link.toString());
				    System.out.println("F["+nProc+","+indiv+"]: "+f+"   "+metric.getTotalStEval());
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
		if(nb > maxNb) stop = true;
		else{
		   currentBnet = disturb(maxBnet,currentBnet,nb);
		   System.out.println("P["+nProc+","+indiv+"] Despues de perturbar..Inicio: "+metric.score(currentBnet));
                }   
	    }
	    else{
		nb+=5;
                if(nb > maxNb) stop = true;
                else{
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
                current.createLink(nodeTail,nodeHead);
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
				current.createLink(nodeHead,nodeTail);
                                i++;
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
        NodeList varsR2 = null;

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
			    varsR2 = null;
			}
                        try{
                          current.removeLink(link);
                        }catch(InvalidEditException iee){};
			Vector acc = new Vector();
	                acc = current.directedDescendants(link.getTail());
                        try{
                          current.createLink(link.getTail(),link.getHead());
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
				varsR2 = null;
			    }

                            Vector vparentsNewj = new Vector();
			    Vector vparentsNewi = new Vector();
			    //System.out.println("Valor antiguo antes de entrar: "+valOld);
			    
                            computeSubSetOfParentsWHC(valOld,current,nodei,nodej,vparentsNewj,vparentsNewi);
			    
			    NodeList parentsNewj = (NodeList) vparentsNewj.elementAt(0);
			    NodeList parentsNewi = (NodeList) vparentsNewi.elementAt(0);
			    if((parentsNewi != null)&&(parentsNewj != null)){
				//System.out.println("Padres al salir del todo: "+parentsNewi.toString2()+" y "+parentsNewj.toString2());
				//System.out.println("Padres antiguos:");
				//NodeList paAni = current.parents(nodei);
				//NodeList paAnj = current.parents(nodej);
				//System.out.println(" "+paAni.toString2()+" ... "+paAnj.toString2());
			       vars = new NodeList();
			       vars.insertNode(nodej);
			       vars.join(parentsNewj);
			       vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			       valNewj = metric.score(vars);
			       vars = new NodeList();
			       vars.insertNode(nodei);
			       parentsNewi.insertNode(nodej);
			       vars.join(parentsNewi);
			       vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			       valNewi = metric.score(vars);
			       valNew = valNewi + valNewj;
			       val = valNew - valOld;
			       //System.out.println("Probando invertir modificando padres:"+nodei.getName()+"-->"+nodej.getName());
                               //System.out.println("Valor: "+val);
			       //System.out.println("Con nuevos padres: "+parentsNewi.toString2()+" y "+parentsNewj.toString2());
			       if(val > max){
				 max = val;
				 op = 1;
				 linkR = link;
				 varsR = new NodeList();
				 varsR.join(vars);
				 varsR2 = new NodeList();
				 vars = new NodeList();
				 vars.insertNode(nodej);
				 vars.join(parentsNewj);
				 varsR2.join(vars);
			       }
			    }//try{System.in.read();}catch(IOException e){};
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
				varsR2 = null;
			    }
			}
		    }
		}
	    }
	}
	vlinkR.addElement(linkR);
 	vvarsR.addElement(varsR);
	vvarsR.addElement(varsR2);
	
	return op;
    }


    private void computeSubSetOfParents(double valOld,Bnet current,Node nodei,Node nodej,
					Vector vparentsNewj,Vector vparentsNewi){
	int i,j,k,h;
	double val;
	NodeList parentsOldj = current.parents(nodej);
	parentsOldj.removeNode(nodei);
	NodeList parentsOldi = current.parents(nodei);
	NodeList parentsij = parentsOldj.intersection(parentsOldi);
	//System.out.println("La interseccion de los nodos es: "+parentsij.toString2());
	if(parentsij.size()>0){
	    NodeList parentsNewj = new NodeList();
	    NodeList parentsNewi = new NodeList();
	    NodeList vars = new NodeList();
	    vars.insertNode(nodei);
	    vars.insertNode(nodej);
	    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
	    //System.out.println("Valor antiguo despues de entrar: "+valOld+" para vars "+vars.toString2());
	    double valNew = metric.score(vars); 
	    vars.removeNode(vars.getId(nodei.getName()));
	    valNew+=metric.score(vars);
	    double max = valNew - valOld;// Mido sin ningun padre el nodo invertido x_i <-- x_j
	    //System.out.println("El arco solo invertido vale: "+max+" Con valor nuevo: "+valNew);
	    parentsij = new NodeList();
	    parentsij.join(parentsOldj);
	    parentsij.join(parentsOldi);
	    //System.out.println("La familia antigua mide: "+valOld);
	    //System.out.println("La union de los padres de los nodos es: "+parentsij.toString2());
	    for(i=0 ; i<= parentsij.size(); i++){
		Vector subsetParentsi = parentsij.subSetsOfSize(i);
		for(j=0 ; j<= parentsij.size(); j++){
		    Vector subsetParentsj = parentsij.subSetsOfSize(j);
		    if((j > 0) && (i > 0)){
			for(h = 0 ; h < subsetParentsi.size() ; h++){
			    NodeList parentsi = (NodeList) subsetParentsi.elementAt(h);
			    for(k = 0; k < subsetParentsj.size();k++){
				NodeList parentsj = (NodeList) subsetParentsj.elementAt(k);
				//System.out.println("Probando con padres para i: "+parentsi.toString2());
				//System.out.println("Probando con padres para j: "+parentsj.toString2());
				vars = new NodeList();
				vars.insertNode(nodei);
				parentsi.insertNode(nodej);
				vars.join(parentsi);
				vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
				parentsi.removeNode(nodej);
				val = metric.score(vars);
				vars = new NodeList();
				vars.insertNode(nodej);
				vars.join(parentsj);
				vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
				val+= metric.score(vars);
				val = val - valOld;
				//System.out.println("Valor: "+val+" Maximo: "+max);
				if(val > max){
				    max = val;
				    parentsNewi = parentsi;
				    parentsNewj = parentsj;
				}
			    }
			}
		    }else{
			if((i > 0) && ( j == 0)){
			    for(h = 0 ; h < subsetParentsi.size() ; h++){
				NodeList parentsi = (NodeList) subsetParentsi.elementAt(h);
				//System.out.println("Probando con padres para i(j==0): "+parentsi.toString2());
				vars = new NodeList();
				vars.insertNode(nodei);
				parentsi.insertNode(nodej);
				vars.join(parentsi);
				vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
				parentsi.removeNode(nodej);
				val = metric.score(vars);
				vars = new NodeList();
				vars.insertNode(nodej);
				vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
				val+=metric.score(vars);
				val = val - valOld;
				//System.out.println("Valor: "+val+" Maximo: "+max);
				if(val > max){
				    max = val;
				    parentsNewi = parentsi;
				}
			    }
			}
			else{
			    if((i == 0) && ( j > 0)){
				for(h = 0 ; h < subsetParentsj.size() ; h++){
				    NodeList parentsj = (NodeList) subsetParentsj.elementAt(h);
				    //System.out.println("Probando con padres para j(i==0): "+parentsj.toString2());
				    vars = new NodeList();
				    vars.insertNode(nodej);
				    vars.join(parentsj);
				    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
				    val = metric.score(vars);
				    vars = new NodeList();
				    vars.insertNode(nodei);
				    vars.insertNode(nodej);
				    val+= metric.score(vars);
				    val = val - valOld;
				    //System.out.println("Valor: "+val+" Maximo: "+max);
				    if(val > max){
					max = val;
					parentsNewj = parentsj;
				    }
				}
			    }
			}
		    }
		}
	    }
	
	    //System.out.println("Al salir los padres son: "+parentsNewi.toString2()+" y "+parentsNewj.toString2());
	    vparentsNewi.addElement(parentsNewi);
	    vparentsNewj.addElement(parentsNewj);
        }else{
            vparentsNewi.addElement(null);
            vparentsNewj.addElement(null);
        }
    }

  private void computeSubSetOfParentsWHC(double valOld,Bnet current,Node nodei,Node nodej,
					Vector vparentsNewj,Vector vparentsNewi){
	int i,j,k,h;
	double val;
	NodeList parentsOldj = current.parents(nodej);
	parentsOldj.removeNode(nodei);
	NodeList parentsOldi = current.parents(nodei);
	NodeList parentsij = parentsOldj.intersection(parentsOldi);
	//System.out.println("La interseccion de los nodos es: "+parentsij.toString2());
	if(parentsij.size()>0){
	    NodeList parentsNewj = new NodeList();
	    NodeList parentsNewi = new NodeList();
	    NodeList vars = new NodeList();
	    vars.insertNode(nodei);
	    vars.insertNode(nodej);
	    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
	    //System.out.println("Valor antiguo despues de entrar: "+valOld+" para vars "+vars.toString2());
	    double valNew = metric.score(vars); 
	    vars.removeNode(vars.getId(nodei.getName()));
	    valNew+=metric.score(vars);
	    double max = valNew - valOld;// Mido sin ningun padre el nodo invertido x_i <-- x_j
	    //System.out.println("El arco solo invertido vale: "+max+" Con valor nuevo: "+valNew);
	    parentsij = new NodeList();
	    parentsij.join(parentsOldj);
	    parentsij.join(parentsOldi);
	    //System.out.println("La familia antigua mide: "+valOld);
	    //System.out.println("La union de los padres de los nodos es: "+parentsij.toString2());
            parentsNewi.insertNode(nodej);
	    parentsNewi = computeParentsWHC((FiniteStates)nodei,parentsNewi,parentsij);
            parentsNewj = computeParentsWHC((FiniteStates)nodej,parentsNewj,parentsij);
	    //System.out.println("Al salir los padres son: "+parentsNewi.toString2()+" y "+parentsNewj.toString2());
	    vparentsNewi.addElement(parentsNewi);
	    vparentsNewj.addElement(parentsNewj);
        }else{
            vparentsNewi.addElement(null);
            vparentsNewj.addElement(null);
        }
    }


    private NodeList computeParentsWHC(FiniteStates node, NodeList pa, NodeList ppa) {

       int numberMaxOfParents = 10;
       NodeList vars = new NodeList();
       vars.insertNode(node);
       vars.join(pa);
       vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
       double fitness = metric.score(vars);
       boolean OkToProceed = true;
       while(OkToProceed && (pa.size()<=numberMaxOfParents)){
	  FiniteStates nodeZ = maxScore(node,pa,ppa);
	  if(nodeZ!=null){
	     vars = new NodeList();
	     vars.insertNode(node);
	     if(pa.size()>0)
		vars.join(pa);
	     vars.insertNode(nodeZ);
             vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
             double fitnessNew = metric.score(vars);
             if(fitnessNew > fitness){
	         fitness = fitnessNew;
		 pa.insertNode(nodeZ);
	     }
             else OkToProceed = false;
	  }
	  else OkToProceed = false;
       }
       return pa;       
    }
    /**
     * This method is private. It is used for searching the parent for the node
     * nodei that maximize the score metric.
     * @param FiniteStates nodei. the node.
     * @param NodeList pa. The actual parents set for the node i.
     * @param int index. The position for the node i.
     * @return FiniteStates. The maximal node.
     */

    private FiniteStates maxScore(FiniteStates nodei,NodeList pa,NodeList ppa){

	int i;
	FiniteStates node, nodeZ;
	NodeList vars;
	double val;
	double max = (-1.0/0.0);

	nodeZ=null;

	for(i=0; i < ppa.size(); i++){
	    node = (FiniteStates) ppa.elementAt(i);
	    if(pa.getId(node) == -1){
		vars = new NodeList();
		vars.insertNode(nodei);
		vars.join(pa);
		vars.insertNode(node);
                vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
		val = metric.score(vars);
		if(val > max){
		    max = val;
		    nodeZ = node;
		}
	    }
	}
	return nodeZ;
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
