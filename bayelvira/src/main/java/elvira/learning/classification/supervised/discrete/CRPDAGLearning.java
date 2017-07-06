/* CRPDAGLearning.java */

package elvira.learning.classification.supervised.discrete;

import elvira.*;
import elvira.learning.*;

import elvira.potential.PotentialTable;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;


import java.io.*;
import java.util.Vector;

/*---------------------------------------------------------------*/
/**
 * This class implements the C-RPDAG Learning algorithm. This method
 * carries out a simple local search in a space of C-RPDAGs (a type 
 * of partially directed acyclic graphs (PDAGs) that combine two concepts of 
 * equivalence: classification  equivalence and independence equivalence)
 * @author Luis M. de Campos (lci@decsai.ugr.es)
 * @author J.G. Castellano (fjgc@decsai.ugr.es)
 * @since 12/02/2004
 * @version 0.1
 */

public class CRPDAGLearning extends MarkovBlanketLearning {

    /**
     * Metric to score bnets
     */
    Metrics metric;

    /**
     * Name of the metric to score bnets
     */
    String metricName;

    /**
     * level of confidence for the conditional independence tests
     */
    double levelOfConfidence;

    /**
     * If it's true, we use conditional independece tests on some operators
     */
    boolean ci;


    /**
     * If it's true, we start the algorithm with a not directed Naive Bayes structure instead a empty graph
     */
    boolean nb;

    /*---------------------------------------------------------------*/
    /** Basic ctor.
     */
    public CRPDAGLearning(){
	super();
	this.metricName=new String();
	this.metric=null;
	levelOfConfidence=0.9;
	ci=false;
	nb=false;
    }//end basic ctor.

    /*---------------------------------------------------------------*/
    /**
     * Initializes the input of C-RPDAG algorithm as a Data Base of
     * Cases and the C-RPDAG output as a Bnet with a empty graph.
     * @param classvar number of the variable to classify
     * @param DataBaseCases cases. The data bases of discrete cases.
     * @param boolean lap. To apply the laplace correction in the 
     *                parameter learning process.
     * @param String metric. Name of the metric to score bnets; it can be "K2", "BIC", "BDe"
     */
    public CRPDAGLearning(DataBaseCases cases, int classvar, boolean lap, String metric) {
	super(cases,classvar,lap);
	setMetrics(null);
	this.metricName=metric;
	levelOfConfidence=0.9;
	ci=false;
	nb=false;
	init();
    } //end ctor with databasecases

    /*---------------------------------------------------------------*/
    /**
     * Initializes the input of C-RPDAG algorithm as a empty graph or as naive bayes with siblings.
     */
    private void init() {
	Bnet b;
	Graph graph;
	NodeList nodes;
	int i;

	//get the node from the dbc
	nodes = getInput().getVariables();

	//build the output bnet with a empty graph
	b = new Bnet();
	b.setKindOfGraph(Graph.MIXED);
	graph = (Graph) b;

	for(i=0 ; i < (nodes.size()) ;i++)
	    try {
		graph.addNode(nodes.elementAt(i));
	    } catch (InvalidEditException iee) {};

	    if (this.nb) {
		//get the class and the nodes
		Node c=nodes.elementAt(this.classvar);
		//store the NaiveBayes structure in the graph, but with non-directed links
		for (i=0;i<nodes.size();i++)
	    	if (i!=this.classvar)
			try {
		    	//graph.createLink(c,nodes.elementAt(i),true);
		    	graph.createLink(nodes.elementAt(i),c,false);
			} catch (InvalidEditException iee) {
		    	System.out.println("Error when adding a link in the init method");
		    	};
		}

	//Store the empty or naive bayes graph
	setOutput(b);
    } //end init method


    /*---------------------------------------------------------------*/
    /** This method is used to build the bnet classifier with the
     *  the C-RPDAG algorithm
     *	@param training training set to build the classifier
     *	@param classnumber number of the variable to classify
     */
    public void learn (DataBaseCases training, int classnumber) {
	Metrics met;

	//update the properties
	setInput(training);
	setVarToClassify(classnumber);

	//set the metric 
	if (getMetrics()==null) {
	    if(this.metricName.equals("BIC")) met = (Metrics) new BICMetrics(getInput());
	    else if(this.metricName.equals("K2")) met = (Metrics) new K2Metrics(getInput());
	    else met = (Metrics) new BDeMetrics(getInput());
	    setMetrics(met);
	} else {
	    met=getMetrics();
	    met.setData(training);
	    setMetrics(met);
	}


	//we have to start with a empty graph or a NaiveBayesSiblings structure
	init();

	//learn the structure of the bnet
	learning();

	//Learn the parameters of the bnet
	ParameterLearning outputNet2;
	if (getIfAplyLaplaceCorrection()) {
	    outputNet2 = new LPLearning(training,getOutput());
	    outputNet2.learning();
	} else {
	    outputNet2 = new DELearning(training,getOutput());
	    outputNet2.learning();
	}
	
	//we store the bnet learned (structure+params)
	setOutput(outputNet2.getOutput());

    }//end learn method

    /*---------------------------------------------------------------*/
    /**
     * This method is used for searching the operation
     * that maximize the score metric.
     * @param Graph g. The graph to evaluate
     * @param Node nodex. The node where the operation must be applied.
     * @param Node nodey. The second node (if it's needed)  where the operation
     *                    must be applied.
     * @return a Vector with, first, a Integer that is the best operation to perform
     *         and, secondo, a Double with the value that must be add to the actual
     *                      score. It it's zero, there isn't any operation
     *			    that maximize the score metric.
     */
    private Vector maxScore(Graph g,Node nodex,Node nodey){
	int i,j; //iterators
	Node x,y; //inductive variables
	Node c; //variable to classify

	double max=-1.0/0.0; //max value for a operation, intially is -inf
	double val=max;
	int op=-1; //best operation to apply
	NodeList nl=g.getNodeList(); //list of variables
	c=nl.elementAt(this.classvar); //get the var to classify

	NodeList pac=g.parents(c);  //parents of c
	NodeList sic=g.siblings(c); //siblings of c
	NodeList chc=g.children(c); //childeren of c

	for (i=0; i < nl.size(); i++) if (i!=this.classvar) {
	    x=nl.elementAt(i);
	    NodeList pax=g.parents(x);  //parents of x
	    NodeList six=g.siblings(x); //siblings of x
	    NodeList chx=g.children(x); //children of x

	    //look if x is a parent of c
	    if (pac.getId(x) != -1) {
		//case: x is parent of c

		//look i c has more or equal than 3 parents
		if (pac.size() >=3 ) {
		    val=evalDeleteParent(g,x);
		    if (val>max) { //update the best operation
			max=val;op=6;
			nodex=x;nodey=null;
		    }
		}
		else {
		    val=evalDeleteHH(g,x);
		    if (val>max) { //update the best operation
			max=val;op=7;
			nodex=x;nodey=null;
		    }

		}
		
	    } else if (pax.getId(c) != -1) {
		//case: x is child of c
		val=evalDeleteChild(g,x);
		if (val>max) { //update the best operation
		    max=val;op=8;
		    nodex=x;nodey=null;
		}

	    } else if (six.getId(c) != -1) {
		//case: x is sibling of c
		val=evalDeleteSibling(g,x);
		if (val>max) { //update the best operation
		    max=val;op=9;
		    nodex=x;nodey=null;
		}
	    } else{
		//case:x is no parent of c, c is not parent of x, c is not sibling of x
		if (pac.size()>0) {
		    //case: c has some parent
		    val=evalInsertParent(g,x);
		    if (val>max) { //update the best operation
			if (this.ci) {
			    // if independents(x,c,Pa(c)) don't update the best operation
			    Node xnode=getInput().getNode(x.getName());
			    Node cnode=getInput().getNode(c.getName());
			    NodeList parentsOfc=g.parents(cnode);
			    parentsOfc = getInput().getNodeList().intersectionNames(parentsOfc).sortNames(parentsOfc);
			    if (!getInput().independents(xnode,cnode,parentsOfc,levelOfConfidence)) {
				max=val;op=0;
				nodex=x;nodey=null;
			    }
			} else {
			    max=val;op=0;
			    nodex=x;nodey=null;
			}
		    }

		    val=evalInsertChild(g,x);
		    if (val>max) { //update the best operation
			max=val;op=2;
			nodex=x;nodey=null;
		    }
		}else{
		    //case: c hasn't any parents
		    if (sic.size()>0)
			//case: c has some siblings
			for (j=0;j<sic.size();j++) {
			    Node z=sic.elementAt(j);
			    val=evalInsertHH(g,x,z);
			    if (val>max) { //update the best operation
				if (this.ci) {
				    // if independents(x,c,z) don't update the best operation
				    Node xnode=getInput().getNode(x.getName());
				    Node cnode=getInput().getNode(c.getName());
				    Node znode=getInput().getNode(z.getName());
				    NodeList aux=new NodeList();
				    aux.insertNode(znode);
				    if (!getInput().independents(xnode,cnode,aux,levelOfConfidence)) {
					max=val;op=1;
					nodex=x;nodey=z;
				    }
				} else {
				    max=val;op=1;
				    nodex=x;nodey=z;
				}
			    }
			}//end for j

		    val=evalInsertSibling(g,x);
		    if (val>max) { //update the best operation
			max=val;op=3;
			nodex=x;nodey=null;
		    }

		}
	    }

	    //now study operators with 2 nodes
	    for (j=0; j < nl.size(); j++) if ( (i!=j) && (j!=this.classvar)) {
		y=nl.elementAt(j);
		NodeList pay=g.parents(y);//parents of y

		if (chc.getId(y) != -1) {
		    //case: y is child of c
		    if (chx.getId(y) != -1) {
			//case: y is child of c & x
			if ( (pay.size()>=3) || (pac.size()>0) ) {
			    //case: y has 3 or more parents or c has any parent
			    val=evalDeleteParentOfChild(g,x,y);
			    if (val>max) { //update the best operation
				max=val;op=10;
				nodex=x;nodey=y;
			    }
			} else {
			    val=evalDeleteHHOfChild(g,x,y);
			    if (val>max) { //update the best operation
				max=val;op=11;
				nodex=x;nodey=y;
			    }

		     }
		    } else { 
			g.setVisitedAll(false);
			if (!isThereDirectedPathFrom(g,y,x)) {
			    //case: y is child of c, but isn't of x, besides there isn't any directed path from y to x
			    val=evalInsertParentOfChild(g,x,y);
			    if (val>max) { //update the best operation
				if (this.ci) {
				    // if independents(x,y,Pa(y)) don't update the best operation
				    Node xnode=getInput().getNode(x.getName());
				    Node ynode=getInput().getNode(y.getName());
				    NodeList parentsOfy=g.parents(ynode);
				    parentsOfy = getInput().getNodeList().intersectionNames(parentsOfy).sortNames(parentsOfy);
				    if (!getInput().independents(xnode,ynode,parentsOfy,levelOfConfidence)) {
					max=val;op=4;
					nodex=x;nodey=y;
				    }
				} else {
				    max=val;op=4;
				    nodex=x;nodey=y;
				}
			    }
			 }
		    }
		} else {
		    //case: y is not child of c
		    if (sic.getId(y) != -1) {
			//case: y is sibling  of c
			g.setVisitedAll(false);
			if (!isThereDirectedPathFrom(g,y,x)) {
			    val=evalInsertHHOfChild(g,x,y);
			    if (val>max) { //update the best operation
				if (this.ci) {

				    // if independents(x,y,c) don't update the best operation
				    Node xnode=getInput().getNode(x.getName());
				    Node ynode=getInput().getNode(y.getName());
				    Node cnode=getInput().getNode(c.getName());
				    NodeList aux=new NodeList();
				    aux.insertNode(cnode);
				    if (!getInput().independents(xnode,ynode,aux,levelOfConfidence)) {
					max=val;op=5;
					nodex=x;nodey=y;
				    }
				} else {
				    max=val;op=5;
				    nodex=x;nodey=y;
				}
			    }
			 }
		    }
		}
	    }//end for j
	}//end for i

	//return the best operation to perform and its score
	Vector result=new Vector();
	result.add(new Integer(op));
	result.add(new Double(max));
	result.add(nodex);
	result.add(nodey);
	return result;

    }//end maxScore method

    /*---------------------------------------------------------------*/
    /**
     * This method implements the CRPDAG algorithm
     */
    public void learning(){
	Graph crpdag;//the C-RPDAG that is learned
	double fitness; //fitness for the C-RPDAG
	boolean okToProceed;// flag to continue
	int op; //operation to perform
	Node nodex, nodey; //Nodes used in the operation to perform
	double value; //this value must be added to the fitness

	//Set the metric
	if (getMetrics() == null) {
	    Metrics met;
	    if(this.metricName.equals("BIC")) met = (Metrics) new BICMetrics(getInput());
	    else if(this.metricName.equals("K2")) met = (Metrics) new K2Metrics(getInput());
	    else met = (Metrics) new BDeMetrics(getInput());
	    setMetrics(met);
	}

	//initialize some vars
	nodex=nodey=new FiniteStates();

	//start with the stored graph, initially is empty
	crpdag=getOutput().duplicate();
	
	//evaluate it
	fitness= metric.score(new Bnet(crpdag.getNodeList()));

	//start the loop with the building process
	okToProceed=true;
	int i=0;
	while (okToProceed) {
	    //compute the best operation for the C-RPDAG
	    Vector aux=maxScore(crpdag,nodex,nodey); 
	    op=((Integer)aux.elementAt(0)).intValue();
	    value=((Double)aux.elementAt(1)).doubleValue();
	    nodex=(FiniteStates)aux.elementAt(2);
	    nodey=(FiniteStates)aux.elementAt(3);
	    /*if (debug) {
		System.out.print("Iteración "+(i++)+" Fitness="+fitness+" Value="+value+" Op="+op);
		switch (op) {
		case 0: System.out.print(" AParent nodex="+nodex.getName());break;
		case 1: System.out.print(" AHH nodex="+nodex.getName()+" nodey="+nodey.getName());break;
		case 2: System.out.print(" AChild nodex="+nodex.getName());break;
		case 3: System.out.print(" ASibling nodex="+nodex.getName());break;
		case 4: System.out.print(" AParentOChild nodex="+nodex.getName()+" nodey="+nodey.getName());break;
		case 5: System.out.print(" AHHOfChild nodex="+nodex.getName()+" nodey="+nodey.getName());break;
		case 6: System.out.print(" DParent nodex="+nodex.getName());break;
		case 7: System.out.print(" DHH nodex="+nodex.getName()+" nodey="+nodey.getName());break;
		case 8: System.out.print(" DChild nodex="+nodex.getName());break;
		case 9: System.out.print(" DSibling nodex="+nodex.getName());break;
		case 10: System.out.print(" DParentOChild nodex="+nodex.getName()+" nodey="+nodey.getName());break;
		case 11: System.out.print(" DHHOfChild nodex="+nodex.getName()+" nodey="+nodey.getName());break;
		};
		System.out.println("");
		}*/
      
	    if (value > 0) {
		fitness+=value;

		//apply the operation to the C-RPDAG
		switch (op) {
		case 0: opAParent(crpdag,nodex);
		        break;
		case 1: opAHH(crpdag,nodex,nodey);
		        break;
		case 2: opAChild(crpdag,nodex);
		        break;
		case 3: opASibling(crpdag,nodex);
		        break;
		case 4: opAParentOfChild(crpdag,nodex,nodey);
		        break;
		case 5: opAHHOfChild(crpdag,nodex,nodey);
		        break;
		case 6: opDParent(crpdag,nodex);
		        break;
		case 7: opDHH(crpdag,nodex);
		        break;
		case 8: opDChild(crpdag,nodex);
		        break;
		case 9: opDSibling(crpdag,nodex);
		        break;
		case 10: opDParentOfChild(crpdag,nodex,nodey);
		        break;
		case 11: opDHHOfChild(crpdag,nodex,nodey);
		        break;
		default:
		    System.out.println("Error: Unknown operation to perform");
		}//end switch
	    } 
	    //if there in no best operation, stop the building process
	    else okToProceed=false;
	}//end while

	//convert the undirected links in directed links: from the class to he attributes
	repairUnOriented(crpdag);


	/*if (debug) {
	    System.out.println("El grafo resultante es:");
	    for (int j=0;j<crpdag.getLinkList().size();j++)
		System.out.print(crpdag.getLinkList().elementAt(j));
		}*/

	//store the learned bnet
	setOutput(new Bnet(crpdag.getNodeList()));	
    }//end learning method
    /*---------------------------------------------------------------*/
    /** 
     * This method repair a graph with undirected links. This undirected
     * links can be only siblings of the class, so the links will be oriented
     * from the class to the attributes.
     * @param Graph g. The graph to repair
     */    
    private void repairUnOriented(Graph g){
	int i;
	Node c=g.getNodeList().elementAt(this.classvar);//c
	NodeList sic=g.siblings(c); //Si_G(c)
	
	for (i=0;i<sic.size(); i++){
	    Node x=sic.elementAt(i);

	    //delete (x--c,G)
	    try {
		g.removeLink(x,c);
	    } catch (InvalidEditException iee) {
		System.out.println("ERROR: Invalid Edit Exception. I can't remove a unoriented link in repairUnOriented");
	    }


	    //insert (c->x,G)
	    try {
		g.createLink(c,x,true);
	    } catch (InvalidEditException iee) {
		System.out.println("ERROR: Invalid Edit Exception. I can't create a oriented link in repairUnOriented");
	    }
	}//end for 
    }
    /*---------------------------------------------------------------*/

     private boolean isThereDirectedPathFrom(Graph g, Node a, Node b) {
	 if (a.equals(b))
	     return true;
	 else {
	     int i;
	     Node ady;
	     boolean found = false;
	     NodeList cha=g.children(a); //children of a
	     for (i=0; i < cha.size(); i++)
		 if (! found) {
		     ady=cha.elementAt(i);
		     if (ady.equals(b))
			 found = true;
		     else if (!ady.getVisited()) {
			 ady.setVisited(true);
			 found = isThereDirectedPathFrom(g,ady,b);
		     }
		 }
	     return found;
	 }
     } //end isTherePath
    
    
    /*---------------------------------------------------------------*/

    /* Evaluation methods */

    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if insert a Parent of the class or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @return evaluation result
     */    
    private double evalInsertParent(Graph g, Node x){
	double val_old;
	double val_new;

	Node c=g.getNodeList().elementAt(this.classvar);//c
	NodeList pac=g.parents(c); //Pa_G(c)

	/*	if (this.ci) {
		// if independents(x,c,Pa(c)) return -inf
		Node nodex=getInput().getNode(x.getName());
		Node nodec=getInput().getNode(c.getName());
		pac = getInput().getNodeList().intersectionNames(pac).sortNames(pac);
		if (getInput().independents(nodex,nodec,pac,levelOfConfidence))
	    		return (-1.0/0.0);
			}*/

	//compute g_D(c,Pa_G(c))
	NodeList vars= new NodeList();
	pac=g.parents(c); //Pa_G(c)
	vars.insertNode(c);
	vars.join(pac);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars);

	//compute g_D(c,Pa_G(c)U{x})
	vars= new NodeList();
	vars.insertNode(c);
	pac.insertNode(x);
	vars.join(pac);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars);

	//return -g_D(c,Pa_G(c)) + g_D(c,Pa_G(c)U{x})
	//System.out.println("AP"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalInsertParent method
    /*---------------------------------------------------------------*/
    /**
     * This method evaluates if insert a HH or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @param Node y second node used in the operation
     * @return evaluation result
     */
    private double evalInsertHH(Graph g, Node x, Node y){
	double val_old;
	double val_new;

	Node c=g.getNodeList().elementAt(this.classvar);//c

	/*	if (this.ci) {
		// if independents(x,c,y) return -inf
		Node nodex=getInput().getNode(x.getName());
		Node nodec=getInput().getNode(c.getName());
		Node nodey=getInput().getNode(y.getName());
		NodeList aux=new NodeList();
		aux.insertNode(nodey);
		if (getInput().independents(nodex,nodec,aux,levelOfConfidence))
	    		return (-1.0/0.0);
			}*/

	//compute g_D(c,{y})
	NodeList vars= new NodeList();
	NodeList aux= new NodeList();
	vars.insertNode(c);
	aux.insertNode(y);
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars);

	//compute g_D(c,{x,y})
	vars= new NodeList();
	aux= new NodeList();
	vars.insertNode(c);
	aux.insertNode(x);
	aux.insertNode(y);
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars);

	//return -g_D(c,{y}) + g_D(c,{x,y})
	//System.out.println("AHH"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalInsertHH method
    /*---------------------------------------------------------------*/
    /**
     * This method evaluates if insert a child of the class or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @return evaluation result
     */    
    private double evalInsertChild(Graph g, Node x){
	double val_old;
	double val_new;
	
	//compute g_D(x,0)
	NodeList vars= new NodeList();
	NodeList aux= new NodeList();
	vars.insertNode(x); 
	//vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(x,{c})
	Node c=g.getNodeList().elementAt(this.classvar);//c
	vars= new NodeList();
	aux= new NodeList();
	vars.insertNode(x); 
	aux.insertNode(c);
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 

	//return -g_D(x,0) + g_D(x,{c})
	//System.out.println("ACh"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalInsertChild method    

    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if insert a sibling of the class or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @return evaluation result
     */    
    private double evalInsertSibling(Graph g, Node x){
	double val_old;
	double val_new;
	
	//compute g_D(c,0)
	NodeList vars= new NodeList();
	NodeList aux= new NodeList();
	Node c=g.getNodeList().elementAt(this.classvar);//c
	vars.insertNode(c); 
	//vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(c,{x})
	vars= new NodeList();
	aux= new NodeList();
	vars.insertNode(c); 
	aux.insertNode(x);
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 

	//return -g_D(c,0) + g_D(c,{x})
	//System.out.println("AS"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalInsertSibling method
    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if insert a Parent of a Child of c or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @param Node y second node used in the operation
     * @return evaluation result
     */    
    private double evalInsertParentOfChild(Graph g, Node x, Node y){

	double val_old;
	double val_new;

	NodeList pay=g.parents(y); //Pa_G(y)

	/*	if (this.ci) {
		//if independents(x,y,Pa(y)) return -inf
		Node nodex=getInput().getNode(x.getName());
		Node nodey=getInput().getNode(y.getName());

		pay = getInput().getNodeList().intersectionNames(pay).sortNames(pay);
		if (getInput().independents(nodex,nodey,pay,levelOfConfidence))
	  	  return (-1.0/0.0);
		  }*/

	//compute g_D(y,Pa_G(y))
	NodeList vars= new NodeList();
	pay=g.parents(y); //Pa_G(y)
	vars.insertNode(y);
	vars.join(pay);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars);

	//compute g_D(y,Pa_G(y)U{x})
	vars= new NodeList();
	vars.insertNode(y); 
	pay.insertNode(x); 
	vars.join(pay);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 
	
	//return -g_D(y,Pa_G(y)) + g_D(y,Pa_G(y)U{x})
	//System.out.println("APoCh"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalInsertParentOfChild method

    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if insert a HH a Child of c or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @param Node y second node used in the operation
     * @return evaluation result
     */    
    private double evalInsertHHOfChild(Graph g, Node x, Node y){
	double val_old;
	double val_new;

	Node c=g.getNodeList().elementAt(this.classvar);//c
	
	/*	if (this.ci) {
		//if independents(x,y,c) return -inf
		Node nodex=getInput().getNode(x.getName());
		Node nodey=getInput().getNode(y.getName());
		Node nodec=getInput().getNode(c.getName());
		NodeList aux= new NodeList();
		aux.insertNode(nodec);
		if (getInput().independents(nodex,nodey,aux,levelOfConfidence))
	  	  return (-1.0/0.0);
		  }*/
	//compute g_D(y,{c})
	NodeList vars= new NodeList();
	NodeList aux= new NodeList();
	vars.insertNode(y); 
	aux.insertNode(c);
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(y,{c,x})
	vars= new NodeList();
	aux= new NodeList();
	vars.insertNode(y); 
	aux.insertNode(c);
	aux.insertNode(x);
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 

	//return -g_D(y,{c}) + g_D(y,{c,x})
	//System.out.println("AHHoCh"+(-val_old+val_new)+" nodex="+x.getName()+" nodey="+y.getName());
	return (-val_old+val_new);
    }//end evalInsertHHOfChild method
    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if delete a Parent of the class or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @return evaluation result
     */    
    private double evalDeleteParent(Graph g, Node x){
	double val_old;
	double val_new;
	
	//compute g_D(c,Pa_G(c))
	NodeList vars= new NodeList();
	Node c=g.getNodeList().elementAt(this.classvar);//c
	vars.insertNode(c); 
	NodeList pac=g.parents(c); //Pa_G(c)
	vars.join(pac);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(c,Pa_G(c)\{x})
	vars= new NodeList();
	vars.insertNode(c); 
	pac.removeNode(x); 
	vars.join(pac);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 
	
	//return -g_D(c,Pa_G(c)) + g_D(c,Pa_G(c)/{x})
	//System.out.println("DP"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalDeleteParent method
    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if delete a HH or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @return evaluation result
     */    
    private double evalDeleteHH(Graph g, Node x){
	double val_old;
	double val_new;
	
	//compute g_D(c,Pa_G(c))
	NodeList vars= new NodeList();
	Node c=g.getNodeList().elementAt(this.classvar);//c
	vars.insertNode(c); 
	NodeList pac=g.parents(c); //Pa_G(c)
	vars.join(pac);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(c,Pa_G(c)\{x})
	vars= new NodeList();
	vars.insertNode(c); 
	pac.removeNode(x); 
	vars.join(pac);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 
	
	//return -g_D(c,Pa_G(c)) + g_D(c,Pa_G(c)/{x})
	//System.out.println("DHH"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalDeleteHH method
    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if delete a child of the class or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @return evaluation result
     */    
    private double evalDeleteChild(Graph g, Node x){
	double val_old;
	double val_new;
	
	//compute g_D(x,Pa_G(x))
	NodeList vars= new NodeList();
	vars.insertNode(x); 
	NodeList pax=g.parents(x); //Pa_G(x)
	vars.join(pax);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(x,0)
	//vars= new NodeList();
	//NodeList aux= new NodeList();
	vars.insertNode(x); 
	//vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 
	
	//return -g_D(x,Pa_G(x)) + g_D(x,0)
	//System.out.println("DCh"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalDeleteChild method

    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if delete a sibling of the class or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @return evaluation result
     */    
    private double evalDeleteSibling(Graph g, Node x){
	double val_old;
	double val_new;
	
	//compute g_D(c,{x})
	NodeList vars= new NodeList();
	NodeList aux=new NodeList();
	Node c=g.getNodeList().elementAt(this.classvar);//c
	vars.insertNode(c); 
	aux.insertNode(x); 
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(c,0)
	vars= new NodeList();
	//aux= new NodeList();
	vars.insertNode(c); 
	//vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 
	
	//return -g_D(c,{x}) + g_D(c,0)
	//System.out.println("DS"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalDeleteSibling method

    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if delete a Parent of a Child of c or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @param Node y second node used in the operation
     * @return evaluation result
     */    
    private double evalDeleteParentOfChild(Graph g, Node x, Node y){
	double val_old;
	double val_new;
	
	//compute g_D(y,Pa_G(y))
	NodeList vars= new NodeList();
	vars.insertNode(y); 
	NodeList pay=g.parents(y); //Pa_G(y)
	vars.join(pay);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(y,Pa_G(y)\{x})
	vars= new NodeList();
	vars.insertNode(y); 
	pay.removeNode(x); 
	vars.join(pay);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 
	
	//return -g_D(y,Pa_G(y)) + g_D(y,Pa_G(y)/{x})
	//System.out.println("DPoCh"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalDeleteParentOfChild method

    /*---------------------------------------------------------------*/
    /** 
     * This method evaluates if delete a HH a Child of c or not
     * @param Graph g. The graph to evaluate
     * @param Node x node used in the operation
     * @param Node y second node used in the operation
     * @return evaluation result
     */    
    private double evalDeleteHHOfChild(Graph g, Node x, Node y){
	double val_old;
	double val_new;
	
	//compute g_D(y,{c,x})
	NodeList vars= new NodeList();
	NodeList aux= new NodeList();
	Node c=g.getNodeList().elementAt(this.classvar);//c
	vars.insertNode(y); 
	aux.insertNode(c);
	aux.insertNode(x);
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_old=this.metric.score(vars); 

	//compute g_D(y,{c})
	vars= new NodeList();
	aux= new NodeList();
	vars.insertNode(y); 
	aux.insertNode(c);
	vars.join(aux);
	vars = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
	val_new=this.metric.score(vars); 

	//return -g_D(y,{c,x})+g_D(y,{c})
	//System.out.println("DHHoCh"+(-val_old+val_new));
	return (-val_old+val_new);
    }//end evalInsertHHOfChild method

    /*---------------------------------------------------------------*/

    /* Operation methods */

    /*---------------------------------------------------------------*/
    /** 
     * This method implements the AParent operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     */    
    private void opAParent(Graph g, Node nodex){
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x

	//insert (x->c,G)
	try {
	    g.createLink(x,c,true);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in AParent operator");
	}
    }//end operation AParent 
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the AHH operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     * @param Node nodey second node used in the operator
     */    
    private void opAHH(Graph g, Node nodex, Node nodey){
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x
	Node y=g.getNodeList().getNode(nodey.getName());//y

	//insert (x->c,G)
	try {
	    g.createLink(x,c,true);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in AHH operator");
	}


	//delete (y--c,G)
	try {
	    g.removeLink(y,c);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in AHH operator");
	}


	//insert (y->c,G)
	try {
	    g.createLink(y,c,true);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in AHH operator");
	}

	
	//For all c siblings (without y) delete c--z, insert c->z
	NodeList sic=g.siblings(c); //Sib_G(c)
	for (int i=0; i < sic.size(); i++) {
	    Node z=sic.elementAt(i);
	    //delete (z--c,G)
	    try {
		g.removeLink(z,c);
	    } catch (InvalidEditException iee) {
		System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in AHH operator");
	    }

	    //insert (c->z,G)
	    try {
		g.createLink(c,z,true);
	    } catch (InvalidEditException iee) {
		System.out.println("ERROR: Invalid Edit Exception. I can't create a link in AHH operator");
	    }
	}//end for i
    }//end operation AHH
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the AChild operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     */    
    private void opAChild(Graph g, Node nodex){
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x

	//insert (c->x,G)
	try {
	    g.createLink(c,x,true);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in AChild operator");
	}
    }//end operation AChild
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the ASibling operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     */    
    private void opASibling(Graph g, Node nodex){
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x

	//insert (x--c,G)
	try {
	    g.createLink(x,c,false);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in ASibling operator");
	}
    }//end operation ASibling
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the AParentOfChild operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     * @param Node nodey second node used in the operator
     */    
    private void opAParentOfChild(Graph g, Node nodex, Node nodey){
	Node x=g.getNodeList().getNode(nodex.getName());//x
	Node y=g.getNodeList().getNode(nodey.getName());//y

	//insert (x->y,G)
	try {
	    g.createLink(x,y,true);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in AParentOfChild operator");
	}
    }//end operation AParentOfChild
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the AHHOfChild operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     * @param Node nodey second node used in the operator
     */    
    private void opAHHOfChild(Graph g, Node nodex, Node nodey){
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x
	Node y=g.getNodeList().getNode(nodey.getName());//y
	//NodeList sic=g.siblings(c); //Sib_G(c)

	//insert (x->y,G)
	try {
	    g.createLink(x,y,true);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in AHHOfChild operator");
	}

	//delete (y--c,G)
	try {
	    g.removeLink(y,c);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in AHHOfChild operator");
	}

	//insert (c->y,G)
	try {
	    g.createLink(c,y,true);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in AHHOfChild operator");
	}

    }//end operation AHHOfChild
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the DParent operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     */    
    private void opDParent(Graph g, Node nodex){
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x

	//remove (x->c,G)
	try {
	    g.removeLink(x,c);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DParent operator");
	}
    }//end operation DParent 

    /*---------------------------------------------------------------*/
    /** 
     * This method implements the DHH operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     */    
    private void opDHH(Graph g, Node nodex){
	int i;
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x

	//delete x->c
	try {
	    g.removeLink(x,c);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DHH operator");
	}

	//look if there is more c parents
	NodeList pac=g.parents(c); //Pa_G(c)
	if (pac.size()>0) {

	    //left unoriented the parents of c
	    for (i=0;i<pac.size();i++) {
		Node z=pac.elementAt(i);
		//delete z->c
		try {
		    g.removeLink(z,c);
		} catch (InvalidEditException iee) {
		    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DHH operator");
		}
		//insert z--c
		try {
		    g.createLink(z,c,false);
		} catch (InvalidEditException iee) {
		    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in DHH operator");
		}
	    }//end for i

	    //left unoriented the children of c, if only has one parent (c)
	    NodeList chc=g.children(c); //Ch_G(c)
	    for (i=0;i<chc.size();i++) {
		Node z=chc.elementAt(i);
		NodeList paz=g.parents(z); //Pa_G(z)
		
		if (paz.size()==1) {
		    //delete c->z
		    try {
			g.removeLink(c,z);
		    } catch (InvalidEditException iee) {
			System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DHH operator");
		    }
		    //insert z--c
		    try {
			g.createLink(z,c,false);
		    } catch (InvalidEditException iee) {
			System.out.println("ERROR: Invalid Edit Exception. I can't create a link in DHH operator");
		    }
		}
	    }//end for i
	}//end if

    }//end operator DHH
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the DChild operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     */    
    private void opDChild(Graph g, Node nodex){
	Node x=g.getNodeList().getNode(nodex.getName());//x
	NodeList pax=g.parents(x); //Pa_G(x)

	//For all x parents  delete c->x
	for (int i=0; i < pax.size(); i++) {
	    Node z=pax.elementAt(i);
	    //delete (z->x,G)
	    try {
		g.removeLink(z,x);
	    } catch (InvalidEditException iee) {
		System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DChild operator");
	    }
	}//end for i
    }//end operation DChild
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the DSibling operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     */    
    private void opDSibling(Graph g, Node nodex){
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x

	//remove (x--c,G)
	try {
	    g.removeLink(x,c);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DSibling operator");
	}
    }//end operator DSibling
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the DParentOfChild operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     * @param Node nodey second node used in the operator
     */    
    private void opDParentOfChild(Graph g, Node nodex, Node nodey){
	Node x=g.getNodeList().getNode(nodex.getName());//x
	Node y=g.getNodeList().getNode(nodey.getName());//y

	//remove (x->y,G)
	try {
	    g.removeLink(x,y);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DParentOfChild operator");
	}
    }//end operation DParentOfChild
    /*---------------------------------------------------------------*/
    /** 
     * This method implements the DHHOfChild operator
     * @param Grapg g. Graph where the operator will be apply
     * @param Node nodex node used in the operator
     * @param Node nodey second node used in the operator
     */    
    private void opDHHOfChild(Graph g, Node nodex, Node nodey){
	Node c=g.getNodeList().elementAt(this.classvar);//c
	Node x=g.getNodeList().getNode(nodex.getName());//x
	Node y=g.getNodeList().getNode(nodey.getName());//y
	//NodeList sic=g.siblings(c); //Sib_G(c)

	//delete (x->y,G)
	try {
	    g.removeLink(x,y);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DHHOfChild operator");
	}

	//delete (c->y,G)
	try {
	    g.removeLink(c,y);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't remove a link in DHHOfChild operator");
	}

	//insert (y--c,G)
	try {
	    g.createLink(y,c,false);
	} catch (InvalidEditException iee) {
	    System.out.println("ERROR: Invalid Edit Exception. I can't create a link in DHHOfChild operator");
	}
    }//end operation DHHOfChild

    /*---------------------------------------------------------------*/

    /* Access methods */


    /*---------------------------------------------------------------*/
    /** 
     * Initializes/store the metric for scoring bnets.
     * @param Metric metric The metric for scoring bnets.
     */    
    public void setMetrics(Metrics metric){
	this.metric = metric;
    }
    /*---------------------------------------------------------------*/
    /** 
     * Return the used metric for scoring bnets.
     * @return the metric for scoring bnets.
     */
    public Metrics getMetrics(){
	return this.metric;
    }
    /*---------------------------------------------------------------*/
    /**
     * Return the used level of confidence for conditional independence tests
     * @return the level of confidence
     */
    public double getLevelOfConfidence(){
	return this.levelOfConfidence;
    }
    /*---------------------------------------------------------------*/
    /**
     * Set the level of confidence for conditional independence tests
     * @param level the level of confidence
     */
    public void setLevelOfConfidence(double  level){
	this.levelOfConfidence = level;
    }
    /*---------------------------------------------------------------*/
    /**
     * Method to set some parameters.
     * @param useCI if we use conditional independece tests on some operators
     * @param useNBs if  we start the algorithm with a not directed Naive Bayes 
     *               structure instead a empty graph
     */
    public void setParams(boolean useCI, boolean useNBs){
    	this.ci=useCI;
	this.nb=useNBs;
    }
    /*---------------------------------------------------------------*/


    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /**
    * For performing tests
    */
   public static void main(String args[]) throws ParseException, IOException { 

      Metrics met;
      String  metname;
      Bnet net, baprend;
      FileWriter f2;
      net = null;

      //Look the arguments for the test
      if(args.length < 4){
	  System.out.println("too few arguments: Usage: input.dbc ouput.elv class metric [file.elv]");
	  System.out.println("\tinput.dbc : DataBaseCases file for building the bnet");
	  System.out.println("\toutput.elv : For saving the result");
          System.out.println("\tclass : The number of the variable to classify if it's the first use 0.");
          System.out.println("\tmetric : Metric used to score, it can be BIC,K2,BDe");
          System.out.println("\tfile.elv: Optional. True net to be compared.");
	  System.exit(0);
      }


      //read the parameters
      //read the cases
      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);

      //get the class
      int classnumber = (new Integer(args[2])).intValue();

      //get the metric
      if(args[3].equals("BIC")) {met = (Metrics) new BICMetrics(cases);metname=args[3];}
      else if(args[3].equals("K2")) {met = (Metrics) new K2Metrics(cases);metname=args[3];}
      else {met = (Metrics) new BDeMetrics(cases);metname="BDe";}

      //Build a bnet with the C-RPDAG Learning method
      CRPDAGLearning outputNet2 = new CRPDAGLearning(cases,classnumber, true, metname);
      outputNet2.learning();

      //Learn the parameters of the bnet
      LPLearning outputNet3 = new LPLearning(cases,outputNet2.getOutput());
      outputNet3.learning();

      //show the results
      double d = cases.getDivergenceKL(outputNet3.getOutput());
      System.out.println("KL Divergence = "+d);
      System.out.println("Bayes Metric for the output net: "+met.score(outputNet2.getOutput()));

      //save the learned bnet
      f2 = new FileWriter(args[1]);
      baprend = (Bnet)outputNet3.getOutput();
      baprend.saveBnet(f2);
      f2.close();


      //compare the learned bnet with the optimal bnet
      if(args.length > 4) {
	  //Build a bnet with the C-RPDAG Learning method
	  CRPDAGLearning classifier = new CRPDAGLearning(cases,classnumber,true,metname);
	  classifier.learn(cases,classnumber);

	  f = new FileInputStream(args[4]);
	  DataBaseCases cases2 = new DataBaseCases(f);

	  System.out.println("tengo "+cases2.getNumberOfCases()+" de test");
	  CaseListMem clm=(CaseListMem)cases2.getCases();
	   for (int k=0;k<cases2.getNumberOfCases();k++){
	      Configuration onecase=clm.get(k);
	      Vector salida=classifier.classify(onecase,classnumber);

	      int max=0;
	      for (int kk=0;kk<salida.size();kk++){
		  if ( ((Double)salida.elementAt(kk)).doubleValue() > 	((Double)salida.elementAt(max)).doubleValue() )
		      max=kk;
		  //System.out.print(salida.elementAt(kk)+" , ");
		  }
	      Vector values  = onecase.getValues();
	      int real=((Integer)values.elementAt(classnumber)).intValue();
	      if (max!=real) {
		  System.out.print("Caso "+k+" ->");
		  for (int kk=0;kk<values.size();kk++)
		      System.out.print(values.elementAt(kk)+" ");
		  System.out.print("(clase predicha="+ max);//((Double)salida.elementAt(max)).doubleValue());
		  System.out.println(" y clase real -> "+real+")");
		  for (int kk=0;kk<salida.size();kk++){
       		  	System.out.print(salida.elementAt(kk)+" ");
		  }
		  System.out.println("");
	      }
	   }
	}

	//TAN
/*	System.out.print("\n\n TAN");
	if(args.length > 4) {
	  //Build a bnet with the C-RPDAG Learning method
	  Naive_Bayes tan = new Naive_Bayes();
	  tan.learn(cases,classnumber);


	  f = new FileInputStream(args[4]);
	  DataBaseCases cases2 = new DataBaseCases(f);

	  CaseListMem clm=(CaseListMem)cases2.getCases();
	   for (int k=0;k<cases2.getNumberOfCases();k++){
	      Configuration onecase=clm.get(k);
	      Vector salida=tan.classify(onecase,classnumber);

	      int max=0;
	      for (int kk=0;kk<salida.size();kk++){
		  if ( ((Double)salida.elementAt(kk)).doubleValue() > 	((Double)salida.elementAt(max)).doubleValue() )
		      max=kk;
		  //System.out.print(salida.elementAt(kk)+" , ");
		  }
	      Vector values  = onecase.getValues();
	      int real=((Integer)values.elementAt(classnumber)).intValue();
	      if (max!=real) {
		  System.out.print("Caso "+k+" ->");
		  for (int kk=0;kk<values.size();kk++)
		      System.out.print(values.elementAt(kk)+" ");
		  System.out.print("(clase predicha="+ max);//((Double)salida.elementAt(max)).doubleValue());
		  System.out.println(" y clase real -> "+real+")");
		  for (int kk=0;kk<salida.size();kk++){
       		  	System.out.print(salida.elementAt(kk)+" ");
		  }
		  System.out.println("");
	      }
	   }
	}


      //compare the learned bnet with the optimal bnet
/*      if(args.length > 4){
	  //read the optimal net
	  FileInputStream fnet = new FileInputStream(args[4]);
	  net = new Bnet(fnet);

	  //compare and show divergences
          double d2 = cases.getDivergenceKL(net);
          System.out.println("kL Divergence for optimal net: "+d2);
          System.out.println("Divergence between optimal and learned nets: "+(d2-d));

	  //show the links differences
	  LinkList addel[] = new LinkList[3];
	  addel = outputNet2.compareOutput(net);
	  System.out.print("\nAdded links: "+addel[0].size());
	  System.out.print(addel[0].toString());
	  System.out.print("\nRemoved links: "+addel[1].size());
	  System.out.print(addel[1].toString());
	  System.out.println("\nInverted links: "+addel[2].size());
	  System.out.print(addel[2].toString());
	  //System.out.print("\nUnoriented links: ");
	  //System.out.print(outputNet2.linkUnOriented().toString());
      }//end if
*/
   } //end main method

} // CRPDAGLearning
