package elvira.learning;

import elvira.*;
import elvira.parser.*;
import elvira.learning.*;
import elvira.database.*;
import java.io.*;
import java.util.Random;

/**
 * CHCSTLearning.java
 *
 *
 * Created: Mon Jan 28 13:57:38 2002
 *
 * @author J. Miguel Puerta
 * @version 1.0
 */

public class CHCSTLearning extends Learning {

    
    public    ThVNSSTPC initialStep;
    public    ThVNSST2 secondStep;
    public    DataBaseCases cases;
    public    Metrics metric;
    public    NodeList variables;
    public    double maxFitness;
    public    Graph maxDag;
    public    Random generator = new Random();


    public CHCSTLearning(DataBaseCases cases, Metrics met){

	variables = cases.getNodeList().duplicate();
	this.cases = cases;
	this.metric = met;

    }


    public void learning(){


        int i;
        Node nd;
        NodeList vars;
	maxDag = new Graph();
        maxFitness = 0;
        for( i= 0 ; i< variables.size(); i++){
        try{
		nd = variables.elementAt(i);
		nd.setParents(new LinkList());
		nd.setChildren(new LinkList());
		nd.setSiblings(new LinkList());
		maxDag.addNode(nd);
                vars = new NodeList();
                vars.insertNode(nd);
                vars = cases.getNodeList().intersectionNames(vars);
                maxFitness+=metric.score(vars);
	    }catch(InvalidEditException iee){};
	}
	initialStep = new ThVNSSTPC(variables,cases,metric,maxDag,maxFitness,0,0,0,generator);
	initialStep.run();
        maxDag = initialStep.dag;
	maxFitness = initialStep.maxFitness;
	secondStep = new ThVNSST2(variables,cases,metric,maxDag,maxFitness,0,0,0,generator);
	secondStep.run();
	maxDag = secondStep.dag;
	maxFitness = secondStep.maxFitness;
	setOutput(new Bnet());
	for(i=0; i< cases.getNodeList().size() ; i++){
	    try{
		nd = cases.getNodeList().elementAt(i);
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
		getOutput().createLink(nodeT,nodeH,true);
	    }catch(InvalidEditException iee){};
	}
	
    }



    public static void main(String args[]) throws ParseException, IOException { 
    
	Bnet baprend;
	FileWriter f2;
	Metrics met;
	
	if(args.length < 4){
	    System.out.println("too few arguments: Usage: file.dbc file.elv cases BIC,K2,BDe [file.elv]");
	  System.exit(0);
	}
	FileInputStream f = new FileInputStream(args[0]);      
	DataBaseCases cases = new DataBaseCases(f);
	cases.setNumberOfCases(Integer.valueOf(args[2]).intValue());
	if(args[3].equals("BIC")) met = (Metrics) new BICMetrics(cases);
	else if(args[3].equals("K2")) met = (Metrics) new K2Metrics(cases);
	else met = (Metrics) new BDeMetrics(cases);

	
	CHCSTLearning outputNet1 = new CHCSTLearning(cases,met);
 
	outputNet1.learning();
	LPLearning outputNet3 = new LPLearning(cases,outputNet1.getOutput());
	outputNet3.learning();
	double d = cases.getDivergenceKL(outputNet3.getOutput());
	System.out.println("Divergencia de KL = "+d);
	System.out.println("Fitness final = "+outputNet1.maxFitness);
	System.out.println("Fitness del resultado: "+met.score(outputNet1.getOutput()));
	
	System.out.println("Estadisticos evaluados: "+met.getTotalStEval());      
        System.out.println("Total de estadisticos: "+met.getTotalSt());      
        System.out.println("Numero medio de var en St: "+met.getAverageNVars()); 

        f2 = new FileWriter(args[1]);
        baprend = (Bnet)outputNet3.getOutput();
        baprend.saveBnet(f2);
        f2.close();
	
        if(args.length > 4){
	    FileInputStream fnet = new FileInputStream(args[4]);
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
    
    
    
} // CHCSTLearning