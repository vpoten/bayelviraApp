/* LocalSearchLearning.java */

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
 * A local search algorithm over the Markov Blanket
 * @author J.G. Castellano (fjgc@decsai.ugr.es)
 * @since 17/02/2004
 * @version 0.1
 */

public class LocalSearchLearning extends MarkovBlanketLearning {

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
    public LocalSearchLearning(){
	super();
	this.metricName=new String();
	this.metric=null;
    }//end basic ctor.

    /*---------------------------------------------------------------*/
    /** 
     * Initializes the input of LocalSearch algorithm as a Data Base of 
     * Cases and the LocalSearch output as a Bnet with a empty graph and 
     * the constraints with a Naive Bayes
     * @param classvar number of the variable to classify
     * @param DataBaseCases cases. The data bases of discrete cases.
     * @param boolean lap. To apply the laplace correction in the 
     *                parameter learning process.
     * @param String metric. Name of the metric to score bnets; it can be "K2", "BIC", "BDe"
     */    
    public LocalSearchLearning(DataBaseCases cases, int classvar, boolean lap, String metric) {
	super(cases,classvar,lap);
	setMetrics(null);
	this.metricName=metric;
	init();
    } //end ctor with databasecases

    /*---------------------------------------------------------------*/
    /** 
     * Initializes the input of LocalSearch algorithm as a empty graph 
     */    
    private void init() {
	int i;
	Bnet b;
	Graph graph;

	//get the class and the nodes
	NodeList nl=getInput().getNodeList();
	Node c=nl.elementAt(this.classvar);

	//build the output bnet with a empty graph
	b = new Bnet();
	b.setKindOfGraph(Graph.MIXED);
	graph = (Graph) b;

	for(i=0 ; i < (nl.size()) ;i++)
	    try {
		graph.addNode(nl.elementAt(i));
	    } catch (InvalidEditException iee) {
		System.out.println("Error when building the initial empty graph");
	    };

	//Store the empty graph
	setOutput(b);
    } //end init method

    /*---------------------------------------------------------------*/
    /** This method is used to build the bnet classifier with the
     *  the local search algorithm
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
     * This method implements the LocalSearch algorithm
     */
    public void learning(){

	//Set the metric
	Metrics met;
	if (getMetrics() == null) {
	    if(this.metricName.equals("BIC")) met = (Metrics) new BICMetrics(getInput());
	    else if(this.metricName.equals("K2")) met = (Metrics) new K2Metrics(getInput());
	    else met = (Metrics) new BDeMetrics(getInput());
	    setMetrics(met);
	}

	//we use the VNS algorithm (only local search)
	DVNSSTLearning outputnet=new DVNSSTLearning(getInput(),1,0,0,1,getMetrics());
	outputnet.setInitialBnet(getOutput());
	outputnet.learning();

	//store the learned bnet
	setOutput(outputnet.getOutput());	
    }//end learning method
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


      //Build a bnet with the LocalSearch Learning method
      LocalSearchLearning outputNet2 = new LocalSearchLearning(cases,classnumber,true,metname);
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

} // LocalSearchLearning
