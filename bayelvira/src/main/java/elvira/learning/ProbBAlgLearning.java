package elvira.learning;

import elvira.*;
import elvira.parser.*;
import elvira.learning.*;
import elvira.database.*;
import java.io.*;
import java.util.Random;


/**
 * ProbBAlgLearning.java
 *
 *
 * Created: Fri Jan 25 11:48:28 2002
 *
 * @author J. Miguel Puerta
 * @version
 */

public class ProbBAlgLearning extends Learning {

    public    double q0;
    public    AntSTB threadAlgorithm;
    public    DataBaseCases cases;
    public    Metrics metric;
    public    NodeList variables;
    public    double maxFitness;
    public    Graph maxDag;
    public    Random generator = new Random();


    public static void main(String args[]) throws ParseException, IOException { 
    
	Bnet baprend;
	FileWriter f2;
	Metrics met;

	if(args.length < 5){
	    System.out.println("too few arguments: Usage: file.dbc file.elv cases BIC,K2,BDe q0([0..1])[file.elv]");
	  System.exit(0);
	}
	double q0 = Double.valueOf(args[4]).doubleValue();
	FileInputStream f = new FileInputStream(args[0]);      
	DataBaseCases cases = new DataBaseCases(f);
	cases.setNumberOfCases(Integer.valueOf(args[2]).intValue());
	if(args[3].equals("BIC")) met = (Metrics) new BICMetrics(cases);
	else if(args[3].equals("K2")) met = (Metrics) new K2Metrics(cases);
             else met = (Metrics) new BDeMetrics(cases);

	
	ProbBAlgLearning outputNet1 = new ProbBAlgLearning(cases,met,q0);
 
	outputNet1.learning();
	DELearning outputNet3 = new DELearning(cases,outputNet1.getOutput());
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
	    FileInputStream fnet = new FileInputStream(args[5]);
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
    

    
    public ProbBAlgLearning(DataBaseCases cases, Metrics met,double q0) {

	variables = cases.getNodeList().duplicate();
	Double[][] fer = new Double[variables.size()][variables.size()];
	double[][] fer_0 = new double[variables.size()][variables.size()];
	this.cases = cases;
	this.metric = met;
	this.q0 = q0;
        for (int i = 0 ; i< variables.size(); i++){
	    for(int j = 0 ; j< variables.size(); j++){
		fer_0[i][j] = 1.0;
                fer[i][j] = new Double(1.0);
	    }
	}
	threadAlgorithm = new AntSTB(fer,fer_0,variables,cases,metric,0.0,1.0,
				     q0,generator);
    }



    public ProbBAlgLearning(DataBaseCases cases, Metrics met,double q0,
			    Random generator) {

	variables = cases.getNodeList().duplicate();
	Double[][] fer = new Double[variables.size()][variables.size()];
	double[][] fer_0 = new double[variables.size()][variables.size()];
	this.generator = generator;
	this.cases = cases;
	this.metric = met;
	this.q0 = q0;
        for (int i = 0 ; i< variables.size(); i++){
	    for(int j = 0 ; j< variables.size(); j++){
		fer_0[i][j] = 1.0;
                fer[i][j] = new Double(1.0);
	    }
	}
	threadAlgorithm = new AntSTB(fer,fer_0,variables,cases,metric,0.0,1.0,
				     q0,generator);
 }

    public void learning(){


	int i;

	threadAlgorithm.run();
	maxDag = threadAlgorithm.getDag();
	maxFitness = threadAlgorithm.getFitness();
	
	setOutput(new Bnet());
	for(i=0; i< cases.getNodeList().size() ; i++){
	    try{
		Node nd = cases.getNodeList().elementAt(i);
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

    
} // ProbBAlgLearning
