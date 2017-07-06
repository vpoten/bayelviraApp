package elvira.learning.constraints;

import java.util.Random;
import java.util.*;
import java.io.*; 
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.learning.*;
import elvira.learning.constraints.*;
import elvira.parser.ParseException;

/*---------------------------------------------------------------*/
/**
 * ThVNSSTCK.java
 *
 *
 * Created: Sun Jan  9 14:11:29 2000
 *
 * @author J. M. Puerta & J. G. Castellano
 * @version 1.0
 */
/*---------------------------------------------------------------*/

public class ThVNSSTCK implements Runnable {
    
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
    ConstraintKnowledge ck; //Constraints Knowledge
    Graph GUGo; //Graph G U Go
    
    
    /*---------------------------------------------------------------*/
    public ThVNSSTCK(){
    } //end basic ctor.
    

    /*---------------------------------------------------------------*/
    public ThVNSSTCK(NodeList vars, DataBaseCases cases,Metrics met,Graph maxBnet
		     ,double maxFitness,int maxNb,int nProc, int nIndiv,Random generator, 
		     ConstraintKnowledge constraints){
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
	this.ck=constraints;
	Bnet aux=new Bnet(maxBnet.getNodeList());
	if (!ck.test(aux)) {
	    //System.out.println("WARNING: Initial Bnet for ThVNSSTCK doesn't verify the constraints. Using a valid Bnet.");
	    aux=ck.repair(aux);
	    if (!ck.test(aux)) {
		System.out.println("WARNING: The repair method fails. Using a valid Bnet.");
		try {
		    aux=ck.initialBnet();
		    maxBnet = (Graph) aux.duplicate();
		    this.maxFitness = metric.score(aux);
		}catch(InvalidEditException iee){};
	    } else {
		maxBnet = (Graph) aux.duplicate();
		this.maxFitness = metric.score(aux);
	    }
	}
	for(i=0 ; i< maxBnet.getLinkList().size(); i++){
	    try{
		Link link = maxBnet.getLinkList().elementAt(i);
		Node nodeT = variables.getNode(link.getTail().getName());
		Node nodeH = variables.getNode(link.getHead().getName());
		dag.createLink(nodeT.copy(),nodeH.copy(),true);
	    }catch(InvalidEditException iee){};
	}

	//compute the G U Go union
	try{
	    GUGo=initialGUGo(this.ck.getPartialOrderConstraints(),dag);
	}catch(InvalidEditException iee){
	    System.out.println("WARNING: The union of G and G_o fails. ");
	};
    } //end ctor.


    /*---------------------------------------------------------------*/    
    /**
     * This methods implements a H_C(DAGS) algorithm.
     */
    public void run(){

	NodeList vars,pa;
	double fitness,fitnessNew;
	boolean OkToProceed,stop;
	Link newLink,link;
	Bnet currentBnet; //= new Bnet();
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
	//-System.out.println("fitness inicial["+nProc+","+indiv+"]: "+fitness);
	stop = false;
	while(!stop){ 
	    OkToProceed = true;
	    while(OkToProceed ){

		Vector vlink=new Vector();
		Vector vvars=new Vector();

		//MaxScore will tell us wht to do: insert, remove or invert a new link
		op = maxScore(vlink,vvars,currentBnet);
		link = (Link)vlink.elementAt(0);
		vars = (NodeList) vvars.elementAt(0);
		if(op!=-1){
		    if(op == 0){//remove a link
			fitnessNew = metric.score(vars);
			vars = new NodeList();
			vars.insertNode(link.getHead());
			pa = currentBnet.parents(link.getHead());
			vars.join(pa);
			vars = cases.getNodeList().intersectionNames(vars).
			    sortNames(vars);
			fitness = metric.score(vars);
			//System.out.println("\n\n\nmaxSCore dice quitar "+link+" . FitnessNew="+fitnessNew+" fitness="+fitness);
			if(fitnessNew > fitness){
			    fitness = fitnessNew;
			    try{
				link = currentBnet.getLink(link.getTail().
				       getName(),link.getHead().getName());
				this.removeLink(currentBnet,link);//currentBnet.removeLink(link);
				double f = metric.score(currentBnet);
                                //System.out.println("Quito la Link: "+link.toString());
				//System.out.println("F["+nProc+","+indiv+"]: "+f);
			    }catch(InvalidEditException iee){};
			} else OkToProceed = false;
		    }else if(op == 1){ //invert a link
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
				newLink=currentBnet.getLink(link.getTail().getName(),link.getHead().getName());
				this.removeLink(currentBnet,newLink);//currentBnet.removeLink(newLink);
				Node ta=currentBnet.getNodeList().getNode(link.getHead().getName());//link.getTail();
		    Node he=currentBnet.getNodeList().getNode(link.getTail().getName());//link.getHead();
			  Link newLink2= new Link(ta,he,true);

				this.createLink(currentBnet,newLink2);//currentBnet.createLink(link.getHead(),link.getTail(),true);
				double f = metric.score(currentBnet);
                                //System.out.println("Invierto la Link: "+newLink.toString());
				//System.out.println("F["+nProc+","+indiv+"]: "+f);
			    }catch(InvalidEditException iee){};
			    
			}else OkToProceed = false;
		    }else
			if(op == 2){//insert a new link
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
				    this.createLink(currentBnet,link);//currentBnet.createLink(link.getTail(),link.getHead(),true);
				    double f = metric.score(currentBnet);
				    //System.out.println("Pongo la Link: "+link.toString());
				    //System.out.println("F["+nProc+","+indiv+"]: "+f);
				}catch(InvalidEditException iee){};
			    } else OkToProceed = false;
			    
			} else OkToProceed = false;   
		}else OkToProceed = false;
	    }
	    it++;
	    fitness = metric.score(currentBnet);
	    localMaxAverage+=fitness;
	    //-System.out.println("Fitness["+nProc+","+indiv+"] (Maximo Local): "+fitness);
	    //-System.out.println("Fitness["+nProc+","+indiv+"] (Maximo): "+maxBnetFitness);
	    if (fitness > maxBnetFitness ){
		//-System.out.println("P["+nProc+","+indiv+"] he encontrado otra mejor con vecindad: "+nb);
		maxBnetFitness = fitness;
		maxBnet = (Graph) currentBnet.duplicate();
		nb = 7;
		numberIterationsForMaximun = it;
		if(nb <= maxNb){
		    Bnet disturbedBnet= disturb(maxBnet,currentBnet,nb);
		    //System.out.println("P["+nProc+","+indiv+"] Despues de perturbar..Inicio: "+metric.score(disturbedBnet));
		    if (!ck.test(disturbedBnet)) {        
			System.out.println("WARNING: P["+nProc+","+indiv+"] Disturbed bnet doesn't verify the constatins...Start: "+metric.score(currentBnet));
		    } else {
			currentBnet=new Bnet(((Graph)disturbedBnet.duplicate()).getNodeList());
			//after distrub compute the G U Go union
			try{
			    GUGo=initialGUGo(this.ck.getPartialOrderConstraints(),currentBnet);
			}catch(InvalidEditException iee){
			    System.out.println("WARNING: The union of G and G_o fails. ");
			};
		    }//end else
		}//end if 
	    }
	    else{
		nb+=5;
		if(nb <= maxNb){
		    Bnet disturbedBnet= disturb(maxBnet,currentBnet,nb);        
		    //System.out.println("P["+nProc+","+indiv+"] Despues de perturbar..Inicio: "+metric.score(currentBnet));
		    if (!ck.test(disturbedBnet)) {        
			System.out.println("WARNING: P["+nProc+","+indiv+"] Disturbed bnet doesn't verify the constatins...Start: "+metric.score(currentBnet));
		    } else {
			currentBnet=new Bnet(((Graph)disturbedBnet.duplicate()).getNodeList());
			//after distrub compute the G U Go union
			try{
			    GUGo=initialGUGo(this.ck.getPartialOrderConstraints(),currentBnet);
			}catch(InvalidEditException iee){
			    System.out.println("WARNING: The union of G and G_o fails. ");
			};
		    }//end else
		}//end if
	    } //end else
	    if(nb > maxNb) stop = true;
	}
	dag = maxBnet;
	maxFitness = maxBnetFitness;
    }//end run method
    

    /*---------------------------------------------------------------*/
    private Bnet disturb(Graph maxBnet,Bnet current,int nb){
        int i,nNode,iR;
        double p;
        boolean ok;
        Node nodeTail,nodeHead,nodei,nodej,nodep;
        Link link,link1,link2,linkT,linkH;
        NodeList paT,paC,paH;
      	Vector acc = null;
	
        //-System.out.println("P ["+nProc+","+indiv+"] perturbando......con vecindad: "+nb);
        /*try {
	    current=ck.initialBnet();	
        } catch (InvalidEditException iee) { */
	    for(i=0 ;i< current.getNodeList().size(); i++){
		nodei = (Node)current.getNodeList().elementAt(i);
		nodei.setParents(new LinkList());
		nodei.setChildren(new LinkList());
		nodei.setSiblings(new LinkList());
	    } //end for
	    current.setLinkList(new LinkList());
/*	}//en catch*/

        
        for(i=0 ; i< maxBnet.getLinkList().size(); i++){
            link = (Link) maxBnet.getLinkList().elementAt(i);
            nodeTail = link.getTail();
            nodeHead = link.getHead();
            nodeTail = current.getNodeList().getNode(nodeTail.getName());
            nodeHead = current.getNodeList().getNode(nodeHead.getName());
	    if (current.getLinkList().getID(nodeTail.getName(), nodeHead.getName())==-1)
		try{
		    Link auxlink=new Link(nodeTail,nodeHead,true);
		    ck.createDirectedLink(current, auxlink );
		}catch(InvalidEditException iee){};
        }//end for i ... maxBnet.getLinkList().size()

	
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
			     ck.removeLink(current, linkT );
			     ck.removeLink(current, linkH );
                             i+=2;
                           }
                         }
                       }
		       ck.removeLink(current, link );
		    }catch(InvalidEditException iee){};
		}else{
    		    ok=true;
		    while (ok) {
			//System.out.println("Invirtiendo la link: "+link.toString());
			ok = false;
			acc = new Vector();
			nodeHead = link.getHead();
			nodeTail = link.getTail();
			try{
			    ck.removeLink(current, link );
			    break;                  
			}catch(InvalidEditException iee){};
			acc = current.directedDescendants(nodeTail);
			if(acc.indexOf(nodeHead) == -1){
			    try{
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
					   ck.removeLink(current, linkT );
					   ck.removeLink(current, linkH );
                                           i+=2;
					}
                                     }
                                }
				Link auxlink=new Link(nodeHead,nodeTail,true);
				if (!ck.createDirectedLink(current, auxlink )) {
				    auxlink=new Link(nodeTail,nodeHead,true);
				    ck.createDirectedLink(current, auxlink );
				}
				
                                i++;
			    }catch(InvalidEditException iee){};
			}else {
			    try{
				Link auxlink=new Link(nodeTail,nodeHead,true);
				ck.createDirectedLink(current, auxlink );
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
				    Link auxlink=new Link(nodei,nodej,true);
				    ck.createDirectedLink(current, auxlink ); 
				}catch(InvalidEditException iee){};
			    }else{
				try{
				    Link auxlink=new Link(nodej,nodei,true);
				    ck.createDirectedLink(current, auxlink );
				}catch(InvalidEditException iee){};
			    }
			}else ok = true;
		    }else ok = true;
		}
	    }
        }//end for i ... nb

	return current;
    }//end disturb method


    /*---------------------------------------------------------------*/
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

	//we score insert or invert/remove every Link from nodei to nodej
	for(i=0; i<current.getNodeList().size(); i++){
	    nodei = (FiniteStates)current.getNodeList().elementAt(i);

	    for(j=0 ; j<current.getNodeList().size();j++){
		nodej = (FiniteStates)current.getNodeList().elementAt(j);

		if(i!=j){
		    //look if the link exists
		    link = (Link)current.getLink(nodei.getName(),nodej.getName());
		    
		    //If the link i->j exists (!=null)  we try to remove it or invert it else we try to insert it
		    if(link != null){
			double valOldj,valNewj;
			vars = new NodeList();
			val=(-1.0/0.0);
			valNewj=valOldj=val;


			//if REMOVE the link verify the constraints, score the op
			if (ck.locallyVerifyConstraints(current, new Link (nodei.copy(),nodej.copy(),true),0,this.GUGo)){
			    //if remove the link verify the constraints, score the op
			    numIndEval++;

			    //compute the score before remove the link
			    paNj = current.parents(nodej);
			    vars.insertNode(nodej);
			    vars.join(paNj);
			    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			    valOldj = metric.score(vars);

			    //compute the score after remove the link
			    paNj.removeNode(nodei);
			    vars = new NodeList();
			    vars.insertNode(nodej);
			    vars.join(paNj);
			    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			    valNewj = metric.score(vars);
			    
			    //compute the score of remove the link
			    val = valNewj - valOldj;
			    //System.out.println("Probando quitar "+nodei.getName()+"-->"+nodej.getName());//
			    //System.out.println("Valor: "+val);//
			}//end if remove verify CK

			//if the remove operation is the best, we store it
			if(val> max){
			    max = val;
			    op = 0;
			    linkR = link;
			    varsR = new NodeList();
			    varsR.join(vars);
			}
			//System.out.println("Valor Max="+max);



			//If INVERT the link verify the constraints, score the op
			val=(-1.0/0.0);			
			if (ck.locallyVerifyConstraints(current, new Link (nodei.copy(),nodej.copy(),true), 1,this.GUGo)){
			    numIndEval++;
			    
			    //compute the score before invert the link
			    paNi = current.parents(nodei);
			    vars = new NodeList();
			    vars.insertNode(nodei);
			    vars.join(paNi);
			    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			    double valOldi = metric.score(vars);
			    double valOld = valOldi + valOldj;

			    //compute the score after invert the link
			    paNi.insertNode(nodej);
			    vars = new NodeList();
			    vars.insertNode(nodei);
			    vars.join(paNi);
			    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			    double valNewi = metric.score(vars);
			    double valNew = valNewi + valNewj;

			    //compute the score of remove the link
			    val = valNew - valOld;
			    //System.out.println("Probando invertir "+nodei.getName()+"-->"+nodej.getName());//
			    //System.out.println("Valor: "+val);//
			}

			//if the invert operation is the best, we store it			 
			if(val > max){
			    max = val;
			    op = 1;
			    linkR = link;
			    varsR = new NodeList();
			    varsR.join(vars);
			}
			//System.out.println("Valor Max="+max);



		    }else{
			val=(-1.0/0.0);
			vars = new NodeList();
			//if INSERT the link verify the constraints, score the op
			if (ck.locallyVerifyConstraints(current,new Link (nodei.copy(),nodej.copy(),true), 2,this.GUGo)){
			    Vector acc = new Vector();
			    numIndEval++;

			    //compute the score before insert the link			    				
			    paNj = current.parents(nodej);
			    vars.insertNode(nodej);
			    vars.join(paNj);
			    vars = cases.getNodeList().intersectionNames(vars).	sortNames(vars);
			    double valOld = metric.score(vars);

			    //compute the score after insert the link			    
			    paNj.insertNode(nodei);
			    vars = new NodeList();
			    vars.insertNode(nodej);
			    vars.join(paNj);
			    vars = cases.getNodeList().intersectionNames(vars).	sortNames(vars);
			    double valNew = metric.score(vars);
				
			    //compute the score of insert the link			    
			    val = valNew - valOld;
			    //System.out.println("Probando insertar "+nodei.getName()+"-->"+nodej.getName());//
			    //System.out.println("Valor: "+val);//
			}

			//if the insert operation is the best, we store it
			if(val > max){
			    max = val;
			    op = 2;
			    linkR = new Link(nodei,nodej);
			    varsR = new NodeList();
			    varsR.join(vars);
			}
			//System.out.println("Valor Max="+max);
			
		    }//else if link==null
		}//end if i!=j
	    }//end for j
	}//end for i

	vlinkR.addElement(linkR);
 	vvarsR.addElement(varsR);
	return op;
    }//end maxScore method

    /*---------------------------------------------------------------*/
    /**
     * This method computes the union Grapg of the partial ordering 
     * contraints an the 
     * @param Graph Go. Graph with the ordereing constraints
     * @param Graph G. Graph that we are learning
     * @return The Union of the two graphs, that is, G U Go
     */
    private Graph initialGUGo(Graph Go, Graph g) throws InvalidEditException {

	//look if there is a directed path from y to x in G U Go
 	Graph ordergraph=new Graph(Go); //Go
	Graph uniongraph = ordergraph.union( new Graph(g.getNodeList().duplicate(),
						       g.getLinkList().duplicate(),Graph.MIXED)); // G U Go

	//We have to comply that (x->y) U (x--y) = (x->y)
	LinkList unionlinks=(uniongraph.getLinkList()).copy();
	LinkList orderlinks=(ordergraph.getLinkList()).copy();
	for (int i=0;i<unionlinks.size();i++) {
	    Link nodirected=unionlinks.elementAt(i);//x--y ?
	    //if the link it's x--y, we search x->y or x<-y in 
	    if ( !nodirected.getDirected() ) {
		Link one=new Link(nodirected.getTail(), nodirected.getHead(), true); //x->y
		Link two=new Link(nodirected.getHead(), nodirected.getTail(), true); //x<-y
		for (int j=0;j<orderlinks.size();j++)
		    //if exists, x->y (or y->x), we delete x--y and add x->y (or y->x)
		    if ( one.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(nodirected.getTail(), nodirected.getHead(), true);			
			break;
		    } else if (two.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(nodirected.getHead(), nodirected.getTail(), true);
			break;
		    }
	    }
	}//end for i

	
	return uniongraph;
    }
    /*---------------------------------------------------------------*/
    /**
     * This method create a link in the graph and in the GUGo structure.
     * @param Graph G. Graph that we are learning
     * @param Link l. The link to create.
     */
    private void createLink(Graph g, Link l) throws InvalidEditException {
	Node tail;
	Node head;

	//create the link in GUGo (if it's necessary)
	tail=GUGo.getNodeList().getNode(l.getTail().getName());
	head=GUGo.getNodeList().getNode(l.getHead().getName());
	int pos=this.GUGo.getLinkList().indexOf(new Link(tail,head,l.getDirected()));
	if (pos<0) 
	    this.GUGo.createLink(tail,head,l.getDirected());

	//create the link in G
	tail=g.getNodeList().getNode(l.getTail().getName());
	head=g.getNodeList().getNode(l.getHead().getName());
	g.createLink(tail,head,l.getDirected());
    }
    /*---------------------------------------------------------------*/
    /**
     * This method remove a link in the graph and in the GUGo structure.
     * @param Graph G. Graph that we are learning
     * @param Link l. The link to create.
     */
    private void removeLink(Graph g, Link l) throws InvalidEditException {
 	Node tail;
	Node head;

	//remove the link in GUGo (if it's necessary)
	int pos=this.ck.getPartialOrderConstraints().getLinkList().indexOf(l);
	if (pos<0) {
	    tail=GUGo.getNodeList().getNode(l.getTail().getName());
	    head=GUGo.getNodeList().getNode(l.getHead().getName());
	    this.GUGo.removeLink(new Link(tail,head,l.getDirected()));
	}
	

	//remove the link in G
        tail=g.getNodeList().getNode(l.getTail().getName());
	head=g.getNodeList().getNode(l.getHead().getName());
	g.removeLink(new Link(tail,head,l.getDirected()));
    }

    /*---------------------------------------------------------------*/
    public double getLocalMaxAverage(){
	return localMaxAverage;
    }

    /*---------------------------------------------------------------*/
    public double getNumberOfIterationsForMaximun(){
	return numberIterationsForMaximun;
    }
    
    /*---------------------------------------------------------------*/
    public double getNumOfIndEval(){

	return numIndEval;
    }
    
    

}// end ThVNSSTCK class
