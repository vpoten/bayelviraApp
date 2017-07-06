/*Clase PbilBN */

package elvira.learning;

import java.io.*;
import java.util.Vector;
import java.util.Random;
import java.lang.Math;
import java.lang.*;
import java.util.Hashtable;
import java.util.Date;
import java.text.DecimalFormat;
import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.parser.*;

/**
 * Clase: Pbil.  
 */

public class PbilBN extends Learning{

 // class variables 

  private DataBaseCases cases;
  public Metrics met;
  private boolean localSearch = false;

  
 /**
  * this will be tha population type we will deal with
  */
  
  private PopulationBN myPop; 

 /**
  * dimension. the number of positions in each chromosome
  */
 
  private int dimension;

 /**
  * populationSize: number of individual our population will have
  */

  private int populationSize;

 
 /**
  * maxGenerations. Maximum number of generations
  * that the algorithm will execute.
  */
 
  private int maxGenerations;

 /**
  * generations. Number of generations already done.
  */

  private int generations;
  private int bestGeneration = 0; 

 /**
  * alpha: learning ration (if alpha = 1.0 then UMDA)
  */

  private double alpha;

/**
 * numCasesToLearnFrom: the first numCasesToLearnFrom individuals of the
 * population will be used for the learning task
 */

 private int numCasesToLearnFrom;

 /**
  * time. El tiempo que ha tardado
  */

  private double time;


 /**
  * bestIndividual. It keeps the best individual so far
  * Vamos a cambiar Individual por una población de un único elemento
  */

  private IndividualBN bestIndividual;


 /**
  * bestIndFitness. It keeps the fitness of the best Individual
  * Lo mismo
  */
 
  private double bestIndFitness;


 /**
  * Los dos siguientes guardan el numero de individuos DISTINTOS evaluados
  * durante la busqueda, y lo mismo pero hasta que se encontro el mejor
  */
  private int evaluatedIndividuals;
  private int evaluatedIndividualsUntilBest;
  private double timeUntilBest;

 /**
  * Los dos siguientes guardan el numero de individuos visitados
  * durante la busqueda, y lo mismo pero hasta que se encontro el mejor
  */
  private int visitedIndividuals;
  private int visitedIndividualsUntilBest;


 /**
  * generator. Generator of random numbers.
  */
 
  public static Random generator;

 /**
  * Una tabla hash para no tener que reevaluar
  */

  private Hashtable HT;
  private int ifHashtable=0;


  private boolean improving=true;
  
  /**
  * Indica si se recolectara o no informacion, para no afectar en
  * las mediciones de tiempo
  */

  private String collectData;

 /**
  * informar: si vale 1 entonces hace los system.out.print
  */

  private int informar=0;

 // constructores

 /**
  * Constructor that needs
  * - A File name, from it we will obtain a population to work with
  * - One Vector with the Linguistic Variables (in order: antecedents and consequent)
  * associated
  * - One Vector with the Linguistic Rules associated. Also in order, each position
  * will correspond with the position in the chromosome
  * - number of maximum generations: to know when to stop
  * - alpha ???
  * - kindOfProb ???
  * - storeData ???
  */

 public PbilBN(DataBaseCases cases, Metrics met, int maxG, int tamPob, 
                         double alfa, 
                         int numCasesToLearn, String storeData, boolean ls) throws IOException{

    Vector v;
    int i,points;
    double lastValue,value;
    Date d;
    
    this.cases = cases;
    this.met = met;
    this.localSearch = ls;
    generator = new Random();
    maxGenerations = maxG;
    generations = 0;
    populationSize = tamPob;
    alpha = alfa;
    numCasesToLearnFrom = numCasesToLearn;

    // capturamos el tiempo inicial
    d = new Date();
    time = (double)d.getTime();

  
    dimension = cases.getNodeList().size();
    myPop = new PopulationBN(dimension,populationSize);
    myPop.initialize(4.0,generator);
	
    //this is for the Hash table
    evaluatedIndividuals = 0;
    evaluatedIndividualsUntilBest = 0;
    visitedIndividuals = 0;
    visitedIndividualsUntilBest = 0;

    //¿por que justo con ese tamaño?
    HT = new Hashtable( (populationSize/4)*maxGenerations );

    //¿? no sé si hace falta
    collectData = storeData;
	
 }


        



 



  

 /**
  * ejecuta el algoritmo PBIL
  */

 public void learning()
 {
   boolean mejora = true;	
   int i,j;
   improving = true;

   // the following variable will contain the uni-variate probabilistic model
   // learnt from data and used for sampling new individuals

   UnivariateModelBN uModel=new UnivariateModelBN(myPop.getDimension());
   
   // Initialize the model to uniform
   uModel.setNoUniform(0.75);
   //uModel.printModel();
	 
   //Initialize population using uModel
   //myPop.generatePopulationBySampling(uModel,generator);

   //We evaluate this population
   myPop.evaluate(HT,ifHashtable,met);
   //myPop.print();
   

   //And sort it by the fitness value
   myPop.sort();
   //myPop.print();

   System.out.println("\nMejor " + myPop.getFitnessAt(0));           	
 
   //We will repeat the following actions (learning and sampling)
   //until we reach the maximum number of genrations or until we reach
   //a situation  without certain improvement.


  while ((generations<maxGenerations)&&(improving))
  {
    System.out.println(" ***** GENERACION " + generations + " *****\n");
	
    UnivariateModelBN auxModel=myPop.learnUnivariateModel(numCasesToLearnFrom);
    //auxModel.printModel();
    uModel.updateWithPbilRule(auxModel,alpha);
    //uModel.printModel();	 

    PopulationBN popTwo = new PopulationBN(dimension,populationSize);
    popTwo.generatePopulationBySampling(uModel,generator);
    popTwo.evaluate(HT,ifHashtable,met);
    popTwo.sort();
    
    if(((IndividualBN)popTwo.getIndividual(0)).getFitness() >
       ((IndividualBN)myPop.getIndividual(0)).getFitness()) bestGeneration = generations;

    //popTwo.print();
   
    double f1 = myPop.totalFitness(populationSize);

    //chapuza
    //for(int l=1;l<populationSize;l++)
     // ((IndividualBN)myPop.getIndividual(l)).
      //      setFitness((double)(-1/0.0));
    // fin chapuza
    
    myPop = myPop.takeBest(popTwo);
    double f2 = myPop.totalFitness(populationSize);
    if(Math.abs(f1-f2) < 0.0001) improving = false;
    //myPop.print();	  
    System.out.println("\nMejor en poblacion " + generations +
                       " es " + myPop.getFitnessAt(0));           

    //Increment of the number of generations. We have just finish one
    generations ++;
	  
  } //end while


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
  IndividualBN bestIndividual = myPop.getIndividual(0);
  int[][] bestChromosome = bestIndividual.getChromosome();
  for(i=0 ; i< cases.getNodeList().size() ; i++)
     for(j=0; j < cases.getNodeList().size() ; j++){
        if(bestChromosome[i][j] == 1){
        	Node nodeT = cases.getNodeList().elementAt(i);
        	Node nodeH = cases.getNodeList().elementAt(j);
            	try{
                   getOutput().createLink(nodeT,nodeH,true);
            	}catch(InvalidEditException iee){};
        }
     }

	
 if(localSearch == true){
	Graph dag = new Graph();
	double fitness = met.score(getOutput());
	dag = ((Graph) getOutput()).duplicate();
  	ThVNSST ls = new ThVNSST(dag.getNodeList(),cases,met,dag,fitness,0,0,0,generator);
 	ls.run();
	dag = ls.dag;
	fitness = ls.maxFitness;
	setOutput(new Bnet());
	for(i=0 ; i< cases.getNodeList().size() ; i++){
		Node nnn = (Node)cases.getNodeList().elementAt(i);
                nnn.setParents(new LinkList());
                nnn.setChildren(new LinkList());
                nnn.setSiblings(new LinkList());
		try{
                   getOutput().addNode(nnn);
		}catch(InvalidEditException iee){};
        }
        for(i=0 ; i< dag.getLinkList().size() ; i++){
		Link link =(Link) dag.getLinkList().elementAt(i);        
        	Node nodeT = getOutput().getNodeList().getNode(link.getTail().getName());
        	Node nodeH = getOutput().getNodeList().getNode(link.getHead().getName());
            	try{
                   getOutput().createLink(nodeT,nodeH,true);
            	}catch(InvalidEditException iee){};
        }
 }
 } //end method learning()



 public int getBestGeneration(){
   
    return bestGeneration;
 }



 
public static void main(String args[]) throws IOException, ParseException{

  Metrics met;

  if (args.length<7){
    System.out.println("Too few arguments. The arguments are:");
    System.out.println("\tfile_name.dbc met[BIC|BDe|K2] numcases maxGenerations");
    System.out.println("\tpopulationSize alpha numCasesToLearnFrom ls(true|false) [file.elv]");
  }      
  else { //else num 1
    if (args.length >=7){

      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      cases.setNumberOfCases(Integer.valueOf(args[2]).intValue());
      if(args[1].equals("BIC")) met = (Metrics) new BICMetrics(cases);
        else if(args[1].equals("K2")) met = (Metrics) new K2Metrics(cases);
             else met = (Metrics) new BDeMetrics(cases);

      PbilBN pb = new PbilBN(cases,met,
		          (Integer.valueOf(args[3])).intValue(), 
		          (Integer.valueOf(args[4])).intValue(),
		          (Double.valueOf(args[5])).doubleValue(),
                          (Integer.valueOf(args[6])).intValue(),"no",
	                  (Boolean.valueOf(args[7])).booleanValue());
      pb.learning();


      DELearning outputNet3 = new DELearning(cases,pb.getOutput());
      outputNet3.learning();
      double d = cases.getDivergenceKL(outputNet3.getOutput());
      System.out.println("Divergencia de KL = "+d);
      System.out.println("Fitness del resultado: "+met.score(pb.getOutput()));
      System.out.println("Estadisticos evaluados: "+met.getTotalStEval());      
      System.out.println("Total de estadisticos: "+met.getTotalSt());      
      System.out.println("Numero medio de var en St: "+met.getAverageNVars());
      System.out.println("Numero de Iteraciones: "+pb.getBestGeneration()); 

      if(args.length == 9){
	    FileInputStream fnet = new FileInputStream(args[8]);
	    Bnet net = new Bnet(fnet);
	    System.out.println("Fitness de la red real: "+met.score(net));
	    double d2 = cases.getDivergenceKL(net);
	    System.out.println("Divergencia real: "+(d2-d));
	    LinkList addel[] = new LinkList[3];
	    addel = pb.compareOutput(net);
	    System.out.print("\nNumero de arcos añadidos: "+addel[0].size());
	    System.out.print(addel[0].toString());
	    System.out.print("\nNumero de arcos borrados: "+addel[1].size());
	    System.out.print(addel[1].toString());
	    System.out.println("\nNumero de arcos mal orientados: "+addel[2].size());
	    System.out.print(addel[2].toString());
      }

    }//end if
    if(args.length > 9){
      System.out.println("Too few arguments. The arguments are:");
      System.out.println("\tfile_name.dbc met(BIC|BDe|K2) numcases maxGenerations");
      System.out.println("\tpopulationSize alpha numCasesToLearnFrom ls(true|false) [file.elv]");
    }//end if
   
  }//end else num 1
} // end of main

} // end of class

