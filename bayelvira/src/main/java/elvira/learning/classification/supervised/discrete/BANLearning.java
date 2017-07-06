/* BANLearning.java */

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
 * This learn a BAN (Bayesian Network augmented Naive Bayes) 
 * structure. That is, it's a local search method 
 * over a Naive Bayes structure.
 * @author J.G. Castellano (fjgc@decsai.ugr.es)
 * @since 17/02/2004
 * @version 0.1
 */

public class BANLearning extends MarkovBlanketLearning {

    /**
     * Metric to score bnets
     */
    Metrics metric;

    /**
     * Name of the metric to score bnets
     */
    String metricName;


    /*---------------------------------------------------------------*/
    /** Basic ctor.
     */
    public BANLearning(){
	super();
	this.metricName=new String();
	this.metric=null;
    }//end basic ctor.

    /*---------------------------------------------------------------*/
    /** 
     * Initializes the input of BAN algorithm 
     * @param classvar number of the variable to classify
     * @param DataBaseCases cases. The data bases of discrete cases.
     * @param boolean lap. To apply the laplace correction in the 
     *                parameter learning process.
     * @param String metric. Name of the metric to score bnets; it can be "K2", "BIC", "BDe"
     */    
    public BANLearning(DataBaseCases cases, int classvar, boolean lap, String metric) {
	super(cases,classvar,lap);
	setMetrics(null);
	this.metricName=metric;
    } //end ctor with databasecases

    /*---------------------------------------------------------------*/
    /** This method is used to build the bnet classifier with the
     *  the BAN algorithm
     *	@param training training set to build the classifier
     *	@param classnumber number of the variable to classify
     */
    public void learn (DataBaseCases training, int classnumber) {
	Metrics met;

	//update the properties
	setInput(training);
	setVarToClassify(classnumber);

	//set the metric 
	if(this.metricName.equals("BIC")) met = (Metrics) new BICMetrics(getInput());
	else if(this.metricName.equals("K2")) met = (Metrics) new K2Metrics(getInput());
	else met = (Metrics) new BDeMetrics(getInput());
	setMetrics(met);

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
     * This method implements the BAN (NaiveBayes + VNS) algorithm
     */
    public void learning(){
	Bnet b;
	Graph graph;
	NodeList nodes;
	int i;

	//get the node from the dbc
	nodes = getInput().getVariables();

	//build the output bnet
	b = new Bnet();
	b.setKindOfGraph(Graph.MIXED);
	graph = (Graph) b;
	for(i=0 ; i < (nodes.size()) ;i++)
	    try {
		graph.addNode(nodes.elementAt(i));
	    } catch (InvalidEditException iee) {};

	//get the class and the nodes
	Node c=nodes.elementAt(this.classvar);

	//store the NaiveBayes structure in the graph
	for (i=0;i<nodes.size();i++)
	    if (i!=this.classvar)
		try {
		    graph.createLink(c,nodes.elementAt(i),true);
		} catch (InvalidEditException iee) {
		    System.out.println("Error adding a link when creating the Bnet");
		};
	
	//Set the metric
	Metrics met;
	if (getMetrics() == null) {
	    if(this.metricName.equals("BIC")) met = (Metrics) new BICMetrics(getInput());
	    else if(this.metricName.equals("K2")) met = (Metrics) new K2Metrics(getInput());
	    else met = (Metrics) new BDeMetrics(getInput());
	    setMetrics(met);
	}

	//we use local search over a NaiveBayes structure
	Bnet outputnet=BANLocalSearch(getInput(),getMetrics(), classvar);

	//store the learned bnet
	setOutput( outputnet );

    }//end learning method

    /*---------------------------------------------------------------*/
    /** This method is used to build the bnet classifier with the
     *  the BAN algorithm
     *	@param cases training set to build the classifier
     *  @param Metric metric The metric for scoring bnets.
     *	@param classnumber number of the variable to classify
     */
    public Bnet BANLocalSearch(DataBaseCases cases,Metrics metric,int classnumber) {
	Bnet maxBnet;
	double maxBnetFitness;
	NodeList vars,pa;
	double fitness,fitnessNew;
	boolean OkToProceed;
	Link newLink,link;
	Bnet currentBnet = new Bnet();
	int op;
	Graph auxGraph = null;
	NodeList variables = cases.getNodeList().duplicate();

	//init the search algoritm with the Naive Bayes structures
	Graph dag = new Graph(0);
	//add the nodes
	for(int i=0; i< variables.size(); i++){
	    try{
		dag.addNode(variables.elementAt(i));
	    }catch(InvalidEditException iee){};
	}
	//get the class
	Node c=variables.elementAt(classnumber);
	//add the links
	for (int i=0;i<variables.size();i++)
	    if (i!=classnumber)
		try {
		    dag.createLink(c,variables.elementAt(i),true);
		} catch (InvalidEditException iee) {
		    System.out.println("Error adding a link when creating the Bnet");
		};
	maxBnet=new Bnet(dag.getNodeList());

	//score the Naive Bayes structure
	maxBnetFitness = metric.score(maxBnet);

	//init the auxiliar structures used in the search process
	auxGraph = maxBnet.duplicate();
	currentBnet = new Bnet();
	currentBnet.setNodeList(auxGraph.getNodeList());
	currentBnet.setLinkList(auxGraph.getLinkList());


	//start the search algoritm
	OkToProceed = true;
	while(OkToProceed ){
	    //we search the best operation to aply (add, del or invert a link)
	    Vector vlink=new Vector();
	    Vector vvars=new Vector();
	    op = maxScore(cases,metric,vlink,vvars,currentBnet);
	    link = (Link)vlink.elementAt(0);
	    vars = (NodeList) vvars.elementAt(0);

	    //If the op==-1 we can't go further
	    if(op!=-1){
		
		//The best operation is delete a link ............. 
		if(op == 0){
		    fitnessNew = metric.score(vars);
		    vars = new NodeList();
		    vars.insertNode(link.getHead());
		    pa = currentBnet.parents(link.getHead());
		    vars.join(pa);
		    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
		    fitness = metric.score(vars);
		    if(fitnessNew > fitness){
			fitness = fitnessNew;
			try{
			    link = currentBnet.getLink(link.getTail().getName(),link.getHead().getName());
			    currentBnet.removeLink(link);
			    double f = metric.score(currentBnet);
			    //System.out.println("Del the Link: "+link.toString());
			}catch(InvalidEditException iee){};
		    } else OkToProceed = false;
		
		//The best operation is invert a link ............. 
    		}else if(op == 1){
		    fitnessNew = metric.score(vars);
		    vars = new NodeList();
		    vars.insertNode(link.getHead());
		    pa = currentBnet.parents(link.getHead());
		    pa.removeNode(link.getTail());
		    vars.join(pa);
		    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
		    fitnessNew+=metric.score(vars);
		    vars = new NodeList();
		    vars.insertNode(link.getHead());
		    pa = currentBnet.parents(link.getHead());
		    vars.join(pa);
		    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
		    fitness = metric.score(vars);
		    vars = new NodeList();
		    vars.insertNode(link.getTail());
		    pa = currentBnet.parents(link.getTail());
		    vars.join(pa);
		    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
		    fitness+= metric.score(vars);
		    if(fitnessNew > fitness){
			fitness = fitnessNew;
			try{
			    newLink=currentBnet.getLink(link.getTail().getName(),link.getHead().getName());
			    currentBnet.removeLink(newLink);
			    currentBnet.createLink(link.getHead(),link.getTail(),true);
			    double f = metric.score(currentBnet);
			    //System.out.println("Invert the Link: "+newLink.toString());
			}catch(InvalidEditException iee){};
		    }else OkToProceed = false;

		//The best operation is add a link ............. 
		}else if(op == 2){
		    fitnessNew = metric.score(vars);
		    vars = new NodeList();
		    vars.insertNode(link.getHead());
		    pa = currentBnet.parents(link.getHead());
		    vars.join(pa);
		    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);  
		    fitness = metric.score(vars);
		    if(fitnessNew > fitness){
			fitness = fitnessNew;
			try{
			    currentBnet.createLink(link.getTail(),link.getHead(),true);
			    double f = metric.score(currentBnet);
			    //System.out.println("Add the Link: "+link.toString());
			}catch(InvalidEditException iee){};
		    } else OkToProceed = false;
		} else OkToProceed = false;   
		
	    }else //there is no operation to apply, we are at the best local Bnet
		OkToProceed = false;
	}//end while

	//compute the metric
	fitness = metric.score(currentBnet);
	
	//see if the computed bnet is better than initial bnet
	if (fitness > maxBnetFitness ){
	    maxBnetFitness = fitness;
	    maxBnet = new Bnet(currentBnet.duplicate().getNodeList());
	}
	
	//return the best bnet
	return maxBnet;

    }//end BANLocalSearch method
    
    /*---------------------------------------------------------------*/
    /**
     * This method is used for searching the link operation
     * that maximize the score metric in a BAN structure.
     * @param cases training set to build the classifier
     * @param Metric metric The metric for scoring bnets.
     * @param Vector vlinkR output vector of links used in the operation
     * @param Vector vvarsR output vector of nodes used in the operation
     * @return int operation to be done. 0-delete, 1-invertion 2-add -1-nothing to do
     */
    private int maxScore(DataBaseCases cases,Metrics metric, Vector vlinkR, Vector vvarsR,Bnet current){

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

		if(i!=j){
		    link = (Link)current.getLink(nodei.getName(),nodej.getName());
		    if(link != null){
			paNj = current.parents(nodej);
			vars = new NodeList();
			vars.insertNode(nodej);
			vars.join(paNj);
			vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			double valOldj = metric.score(vars);
			paNj.removeNode(nodei);
			vars = new NodeList();
			vars.insertNode(nodej);
			vars.join(paNj);
			vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			double valNewj = metric.score(vars);
			val = valNewj - valOldj;

			//Don't delete the link if it belongs to the Naive Bayes Structure
			if ( nodei.equals(cases.getNodeList().lastElement()) ) val=(-1.0/0.0);

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
			    paNi = current.parents(nodei);
			    vars = new NodeList();
			    vars.insertNode(nodei);
			    vars.join(paNi);
			    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			    double valOldi = metric.score(vars);
			    double valOld = valOldi + valOldj;
			    paNi.insertNode(nodej);
			    vars = new NodeList();
			    vars.insertNode(nodei);
			    vars.join(paNi);
			    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			    double valNewi = metric.score(vars);
			    double valNew = valNewi + valNewj;
			    val = valNew - valOld;

			    //Don't invert the link if it belongs to the Naive Bayes Structure
			    if ( nodei.equals(cases.getNodeList().lastElement()) ) val=(-1.0/0.0);

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
			    paNj = current.parents(nodej);
			    vars = new NodeList();
			    vars.insertNode(nodej);
			    vars.join(paNj);
			    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			    double valOld = metric.score(vars);
			    paNj.insertNode(nodei);
			    vars = new NodeList();
			    vars.insertNode(nodej);
			    vars.join(paNj);
			    vars = cases.getNodeList().intersectionNames(vars).sortNames(vars);
			    double valNew = metric.score(vars);
			    val = valNew - valOld;

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

    }//end maxScore method
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
    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /**
    * For performing tests
    */
   public static void main(String args[]) throws ParseException, IOException { 

      Metrics met;
      String metname;
      Bnet net, baprend;
      FileWriter f2;
      net = null;

      //Look the arguments for the test
      if(args.length < 4){
	  System.out.println("too few arguments: Usage: input.dbc ouput.elv class metric [file.elv]");
	  System.out.println("\tinput.dbc : DataBaseCases file for building the bnet");
	  System.out.println("\toutput.dbc : For saving the result");
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

      //Build a bnet with the BAN Learning method
      BANLearning outputNet2 = new BANLearning(cases,classnumber,true,metname);
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
      if(args.length > 4){
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
      
   } //end main method

} // BANLearning
