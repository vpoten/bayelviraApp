package elvira.learning.constraints;

/*---------------------------------------------------------------*/
/**
 * VNSSTProcesorCK.java
 *
 *
 * Created: Mon Jul 24 12:41:43 2000
 * 
 * TODO: Change inicializePopulation funtions to verify constraints
 *
 * @author J. M. Puerta & J. G. Castellano
 * @version 1.0
 */
/*---------------------------------------------------------------*/
import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.learning.constraints.*;

public class VNSSTProcesorCK implements Runnable {
    
    NodeList variables;
    DataBaseCases cases;
    Metrics metric;
    int popSize;
    double[] popFitness;
    double[] tmpPopFitness;
    Graph[] popDags;
    Graph[] tmpPopDags;
    int maxIndex;
    int iteration,maxIteration;
    int maxNb;
    int numberProc;
    Random generator;
    double localMaxAverage = 0.0;
    double numberIndivEval = 0.0;
    double numberIterationsForMaximun = 0.0;
    double it = 0.0;
    double evalIni;
    ConstraintKnowledge ck;

    /*---------------------------------------------------------------*/
    public VNSSTProcesorCK() {
	
    }

    /*---------------------------------------------------------------*/
    public VNSSTProcesorCK(int nproc,NodeList vars,DataBaseCases data,Metrics met,int popSize,
			   int maxNb,Random generator,double evalIni,ConstraintKnowledge constraints){

	this.numberProc = nproc;
	variables = vars.duplicate();
	cases = data;
	metric = met;
	this.popSize = popSize;
	maxIteration = 1;
	iteration = 0;
	this.maxNb = maxNb;
        this.generator = generator;
	popFitness = new double[popSize];
	popDags = new Graph[popSize];
	tmpPopFitness = new double[popSize*2];
	tmpPopDags = new Graph[popSize*2];
        this.evalIni = evalIni;
	this.ck=constraints;
    }

    /*---------------------------------------------------------------*/
    public void run(){
	
	int i,j;
	Thread[] th = new Thread[popSize];
	ThVNSSTCK[] thvnsst = new ThVNSSTCK[popSize];
	System.out.println("Procesador: "+numberProc+" Iteracion : "+iteration);
	if(iteration == 0) inicializePopulationBS();
	maxIndex = searchMax();
	selectIndiv();
	iteration++;
	for(i = 0 ; i< popSize ; i++){
	    thvnsst[i] = new ThVNSSTCK(variables,cases, metric,
                             popDags[i],popFitness[i],maxNb,numberProc,i,generator,this.ck);
	    th[i] = new Thread(thvnsst[i]);
	}
	for(i = 0 ; i< popSize ; i++){
	    th[i].start();
	    //for(i = 0 ; i< popSize ; i++){
	    try{
		th[i].join();
	    }catch(InterruptedException e){};
	}
	for(i=0; i< popSize ; i++){
	    popFitness[i] = thvnsst[i].maxFitness;
	    tmpPopFitness[i] = popFitness[i];
	    popDags[i] = thvnsst[i].dag;
	    tmpPopDags[i] = popDags[i];
	    localMaxAverage+= thvnsst[i].getLocalMaxAverage();
	    numberIndivEval+= thvnsst[i].getNumOfIndEval();
	    it+= thvnsst[i].it;
	}
	maxIndex = searchMax();
	numberIterationsForMaximun = thvnsst[maxIndex].
	    getNumberOfIterationsForMaximun();
	//printPop();
	//try{System.in.read();}catch(IOException e){};
    }

//     public void printPop(){
// 	int i,j;
// 	for(i=0; i< popSize ; i++){
// 	    System.out.print("\n Individuo: ");
// 	    for(j=0 ; j< variables.size() ; j++)
// 		System.out.print(" "+popIndex[i][j]);
// 	    System.out.println("---> "+eval(i,popFitness));
// 	}

//     }

    /*---------------------------------------------------------------*/
    private int searchMax(){

	double valor;
	int i,ind=0;
	double max = (-1.0/0.0);
	for(i=0 ; i< popSize ; i++){
	    valor = popFitness[i];
	    if(valor > max){
		ind = i;
		max = valor;
	    }
	}
	return ind;
    }

    /*---------------------------------------------------------------*/
    private void inicializePopulationBS(){

	int i=0;
	int j,h,k;
	NodeList vars;
        Double[][] feromone;
        double[][] feromone_0;
        feromone = new Double[variables.size()][variables.size()];
	feromone_0 =  new double[variables.size()][variables.size()];
	for(h=0; h< variables.size() ; h++)
	   for(k=0 ; k< variables.size() ; k++){
	      feromone_0[h][k] = 1.0/Math.abs(evalIni);
	      feromone[h][k] = new Double(1.0/Math.abs(evalIni));
	   }
        if(numberProc == 0) i=1;
	synchronized(cases){
	    while(i<popSize){
              AntSTB ant;
	      Thread th;
              double q0 = 0.9;
              ant = new AntSTB(feromone,feromone_0,variables,cases,
				 metric,0.4,1.0,q0,generator);
	      th = new Thread(ant);
	      try{
                th.start();
                th.join();
              }catch(InterruptedException e){};
              popDags[i] = ant.getDag();
              popFitness[i] = ant.getFitness();
	      i++;
	    }
	}
    }

    /*---------------------------------------------------------------*/
    private void inicializeRandomPopulation(){

	int i=0;
	int j,h;
	NodeList vars;
	int[] index = new int[variables.size()];
	if(numberProc == 0) i=1;
	synchronized(cases){
	    while(i<popSize){
		index = aleaIndex();
		vars = new NodeList();
		for(j=0 ; j<index.length; j++){
		    Node nodej = variables.elementAt(index[j]);
		    vars.insertNode(nodej);
		}
		
		K2Learning k2 = new K2Learning(cases,vars,5,metric);
		k2.learning();
		popDags[i] = ((Graph) k2.getOutput()).duplicate();
		popFitness[i] = metric.score(k2.getOutput());		
		i++;
	    }
	}
    }

    /*---------------------------------------------------------------*/
    private int[] aleaIndex(){

	int[] tmpIndex = new int[variables.size()];
	int nvariables = variables.size();
	int i,j,k,AddedInts;
        int[] Canonic;

        Canonic = new int[nvariables];
	
        for (i=0;i<nvariables;i++)
	    Canonic[i]=i;
	
        AddedInts = nvariables;
        while (AddedInts>1) {
	    j = (int)(generator.nextDouble()*AddedInts);
	    tmpIndex[nvariables-AddedInts] = Canonic[j];
	    AddedInts--;
	    for (k=j;k<AddedInts;k++)
                Canonic[k]=Canonic[k+1];
        }
	tmpIndex[nvariables-1] = Canonic[0];
	return tmpIndex;
    }

    /*---------------------------------------------------------------*/
    private void selectIndiv(){
	
	int i,j,ind,k;
	double max;
	double valor;
	ind = 0;
	int selected[] = new int[popSize*2];
	for(i=0; i< selected.length ; i++) selected[i]=0;
	
	if(iteration > 0){
	    for(j=0 ; j<popSize ; j++){
		max = (-1.0/0.0);
		for(i=0 ; i < tmpPopFitness.length ; i++){
		    valor = tmpPopFitness[i];
		    if((max < valor)&&(selected[i]==0)){ 
			max = valor;
			ind = i;
		    }
		}
		selected[ind]=1;
		popFitness[j] = tmpPopFitness[ind];
		popDags[j] = tmpPopDags[ind];
	    }
	}
    }
    
} // end VNSSNProcesorCK class

