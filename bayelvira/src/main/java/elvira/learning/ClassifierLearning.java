package elvira.learning;

import java.util.*;
import java.io.*;
import elvira.*;
import java.util.Enumeration;
import java.util.Hashtable;
import elvira.database.*; //DataBaseCases;
import elvira.potential.*;
//import elvira.potential.PotentialTable;
import elvira.inference.*;
import elvira.inference.clustering.*;//HuginPropagation;
import elvira.parser.ParseException;



//import java.util.*;
//import java.io.*;
//import elvira.*;
//import elvira.database.DataBaseCases;

/**
 * ClassifierLearning.java
 *
 *
 * Created: Tue Nov 23 10:08:13 1999
 *
 * @author P. Elvira
 * @version 1.0
 */

public class ClassifierLearning extends Learning {
    
    NodeList nodesSorted;  // A List of Nodes sorted.
    DataBaseCases input;   // The cases for the input algorithm.
    int numberMaxOfParents;// The number of maximal parents for each node.
    EvalasClass metric;      // The metric for scoring.
    int begining; // Index for the input nodes, nodei<begining are considered as root nodes.


 public static void main(String args[]) throws ParseException, IOException { 
    
     Bnet naivebayesnet;
     FileWriter f2;
     Node nodei,nodex;
     int i;

      if(args.length < 2){
	  System.out.println("too few arguments: Usage: file.dbc file.elv [file.elv]");
	  System.exit(0);
      }
      FileInputStream f = new FileInputStream(args[0]);
      
      DataBaseCases cases = new DataBaseCases(f);
//      cases.compile();

        naivebayesnet = new Bnet();

        //System.out.println("Hay "+cases.getNodeList().size()+" nodos");

        for(i=0 ; i< cases.getNodeList().size(); i++){
          nodei = (Node) cases.getNodeList().elementAt(i);
           try{
	     naivebayesnet.addNode(nodei);
              }catch (InvalidEditException e){};
        }

        System.out.println("Nodos añadidos");
        nodei = (Node) naivebayesnet.getNodeList().elementAt(naivebayesnet.getNodeList().size()-1);
        for(i=0 ; i< naivebayesnet.getNodeList().size()-1; i++){
        //i=2;
           nodex = (Node) naivebayesnet.getNodeList().elementAt(i);
           try{
     	      naivebayesnet.createLink(nodei,nodex);
                 }catch (InvalidEditException e){};
        }
        System.out.println("arcos añadidos");

      ClassifierLearning outputNet1 = new ClassifierLearning(cases, naivebayesnet);
           
      //outputNet1.learning();

      System.out.println("Va a clasificar");

      System.out.println(outputNet1.metric.wellclassified());
      //System.out.println(outputNet1.metric.wellclassified(cases));

      System.out.println("Ha clasificado?");

            
      //f2 = new FileWriter(args[1]);
      //baprend = (Bnet)outputNet1.getOutput();
      //baprend.saveBnet(f2);
      //f2.close();

/*
      if(args[2] != null){
	  FileInputStream fnet = new FileInputStream(args[2]);
	  Bnet net = new Bnet(fnet);
	  LinkList addel[] = new LinkList[3];
	  addel = outputNet1.compareOutput(net);
	  System.out.print("\nNumero de arcos añadidos: "+addel[0].size());
	  System.out.print(addel[0].toString());
	  System.out.print("\nNumero de arcos borrados: "+addel[1].size());
	  System.out.print(addel[1].toString());
	  System.out.println("\nNumero de arcos mal orientados: "+addel[2].size());
	  System.out.print(addel[2].toString());
      }  
*/
    
   }  

    public ClassifierLearning(){
	setNodesSorted(null);
	setInput(null);
	setMetric(null);
    }

    /**
     * Constructor for the Learning Algorithm K2.
     * @param DataBaseCases. cases. The input of algorithm.
     * @param NodeList nodes. The list of nodes sorted.
     * @param int nMaxOfParents. The max number of parents for each node.
     */

    public ClassifierLearning(DataBaseCases cases, Bnet naivebayes){
	int i;
	Node nodei,nodex;
        //Bnet naivebayes;

        //naivebayes = new Bnet();

        //System.out.println("Hay "+cases.getNodeList().size()+" nodos");

        //for(i=0 ; i< cases.getNodeList().size(); i++){
        //  nodei = (Node) cases.getNodeList().elementAt(i);
        //   try{
	//     naivebayes.addNode(nodei);
        //      }catch (InvalidEditException e){};
        //}

        //System.out.println("Nodos añadidos");

	/*
        nodesSorted = new NodeList();
	for(i=0 ; i< cases.getNodeList().size(); i++){
	    nodei = (Node) cases.getNodeList().elementAt(i);
	    nodex = (Node) cases.getNodeList().getNode(nodei.getName());
	    nodesSorted.insertNode(nodex);
	}
	
        nodei = (Node) cases.getNodeList().elementAt(cases.getNodeList().size()-1);

        System.out.println("Hay "+cases.getNodeList().size()+" nodos");
	*/
//        nodei = (Node) cases.getNodeList().elementAt(cases.getNodeList().size()-1);
//        for(i=0 ; i< naivebayes.getNodeList().size()-1; i++){
        //i=2;
//           nodex = (Node) naivebayes.getNodeList().elementAt(i);
//           try{
//     	      naivebayes.createLink(nodei,nodex);
//                 }catch (InvalidEditException e){};
//        }
//        System.out.println("arcos añadidos");

	input = cases;

	//input.compile();

        // LPNormalize?
	 LPLearning lpnormalizated = new LPLearning(input, naivebayes);
	 lpnormalizated.learning();
	 Bnet normalizedBnet = lpnormalizated.getOutput();

	begining = 1;
	metric = new EvalasClass(input, normalizedBnet);
    }

    public ClassifierLearning(DataBaseCases cases){
	int i;
	Node nodei,nodex;
        Bnet naivebayes;

        naivebayes = new Bnet();

        //System.out.println("Hay "+cases.getNodeList().size()+" nodos");

        for(i=0 ; i< cases.getNodeList().size(); i++){
          nodei = (Node) cases.getNodeList().elementAt(i);
           try{
	     naivebayes.addNode(nodei);
              }catch (InvalidEditException e){};
        }

        //System.out.println("Nodos añadidos");

	/*
        nodesSorted = new NodeList();
	for(i=0 ; i< cases.getNodeList().size(); i++){
	    nodei = (Node) cases.getNodeList().elementAt(i);
	    nodex = (Node) cases.getNodeList().getNode(nodei.getName());
	    nodesSorted.insertNode(nodex);
	}
	
        nodei = (Node) cases.getNodeList().elementAt(cases.getNodeList().size()-1);

        System.out.println("Hay "+cases.getNodeList().size()+" nodos");
	*/
//        nodei = (Node) cases.getNodeList().elementAt(cases.getNodeList().size()-1);
//        for(i=0 ; i< naivebayes.getNodeList().size()-1; i++){
        //i=2;
//           nodex = (Node) naivebayes.getNodeList().elementAt(i);
//           try{
//     	      naivebayes.createLink(nodei,nodex);
//                 }catch (InvalidEditException e){};
//        }
//        System.out.println("arcos añadidos");

	input = cases;

	//input.compile();

        // LPNormalize?
	 LPLearning lpnormalizated = new LPLearning(input, naivebayes);
	 lpnormalizated.learning();
	 Bnet normalizedBnet = lpnormalizated.getOutput();

	begining = 1;
	metric = new EvalasClass(input, normalizedBnet);
    }	

	// Evaluate the naive-Bayes structure
    public void  learning(){

	int i,j;
	FiniteStates nodeXi,nodeZ;
	NodeList PaXi,vars;
	double fitness,fitnessNew;
	boolean OkToProceed;
	LinkList links;
	Link newLink;
	
	links = new LinkList();

        System.out.println("Vamoalla");
	
	for(i=0; i< nodesSorted.size()-1;i++){
            System.out.println("Va a añadir el arco"+i);
	    nodeXi = (FiniteStates)nodesSorted.elementAt(i);
 	    newLink = new Link(nodesSorted.elementAt(nodesSorted.size()-1),nodeXi);
	    links.insertLink(newLink);
            System.out.println("Añadido el arco"+i);
	    //System.out.println(newLink);
	    //try{
//		System.in.read();
//	    }catch (IOException e){};
	}

	setOutput(new Bnet());
	for(i=0 ; i< nodesSorted.size();i++)
	    try {
	       getOutput().addNode(nodesSorted.elementAt(i));
	    } catch (InvalidEditException iee) {};
	for(i=0 ; i< links.size();i++){
	    newLink = (Link) links.elementAt(i);
	    try {
	       getOutput().createLink(newLink.getTail(),newLink.getHead());
	    } catch (InvalidEditException iee) {};	    
	}
	    
    }


    public void setNodesSorted(NodeList nl){
	nodesSorted = nl;
    }
    public void setInput(DataBaseCases db){
	input = db;
    }
    public void setMetric(EvalasClass metric){
	this.metric = metric;
    }
} // ClassifierLearning







