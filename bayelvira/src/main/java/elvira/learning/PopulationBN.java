/* PopulationBN.java */ 

package elvira.learning;

import java.io.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Random;
import java.lang.Math;
import java.text.DecimalFormat;
import elvira.*;
import elvira.learning.*;


public class PopulationBN{


// A unidimensional array containing PopulationSize individuals

private IndividualBN[] matricialDat; 

// tamChromosome: the number of variables in the network. So the length
// of the chromosome is tamChromosome^2, because is an array[][]

private int tamChromosome;



//The maximum number of cases in each chromosome, if for example is 4, 
//then we can have -1,0,1,2

private int maxNumberOfValuesPerGene = 2;

// population size

private int populationSize;


     /*******************
	  *	Constructor
      *****************/
  	  public PopulationBN(int dim, int tam)
	  {
		matricialDat = new IndividualBN[tam];
		tamChromosome = dim;
                populationSize = tam;
	  }


     /*******************
	  *	getDimension: number of elements for each vector, at the end
	  *	that is the number of rules
      *****************/
	  public int getDimension()
	  {
        return tamChromosome;
	  }



     /*******************
	  *	setMaximumCases
      *****************/
	  public void setMaxNumberOfValuesPerGene(int m)
          {
            maxNumberOfValuesPerGene=m;
	  }
	  
 

/**
 * inicializa la poblacion
 */
    
public void initialize(double np, Random generador){
  int i;

  for(i=0 ; i< populationSize ; i++)
    matricialDat[i] = new IndividualBN(tamChromosome,np,generador);
}


/**
 * evalua la poblacion
 * returns the number of actually evaluated individuals (not in HT)
 */
    
public int evaluate(Hashtable HT,int ifHT, Metrics met){
  int i;
  int cont=0;
  int v;

  for(i=0 ; i< populationSize ; i++){
    v = ((IndividualBN)matricialDat[i]).evaluate(HT,ifHT,met);
    if (v == 1){
      cont++;
    }
  }
  return cont;
}




	 /*******************
	  * getFitnessAt()	
	  *****************/
	  public double getFitnessAt(int i)
	   {
		return ((IndividualBN) matricialDat[i]).getFitness();
	   } 


/**
  * Sorts the population according to
  * the fitness.
  * Sort method: selection.
  */

public void sort( ){

  int i,k,posMayor;
  double fitnessMayor;
  IndividualBN mayor,aux;

  for(i=0; i<(populationSize -1) ;i++){
    
    posMayor = i;
    mayor = (IndividualBN) matricialDat[i];
    fitnessMayor = mayor.getFitness();  

    for(k=i+1;k<populationSize;k++){
      aux = (IndividualBN)matricialDat[k];
      if ( aux.getFitness() > fitnessMayor ){ 
        posMayor = k;
        mayor = aux;
        fitnessMayor = mayor.getFitness(); 
      }
    }

    matricialDat[posMayor] = matricialDat[i];
    matricialDat[i] = mayor;	

  }

}


 /**
  * Generates a new population by sampling a univariate model
  */

 public void generatePopulationBySampling(UnivariateModelBN um,Random generador){
   double r,accumulate;
   int i,j,j1,j2,k,h,index1,index2;
   IndividualBN ind;
   int nParents;
   int maxParents = (int) Math.round((double)tamChromosome/5.0);

   Graph auxGraph;
   NodeList nodesAux = new NodeList();
   for(i=0 ; i<tamChromosome ; i++){
      FiniteStates node = new FiniteStates();
      node.setName(new Integer(i).toString());
      nodesAux.insertNode(node);
   }


   for(i=0;i<populationSize;i++){
     auxGraph = new Graph();
     auxGraph.setNodeList(nodesAux);
     ind = new IndividualBN(tamChromosome);
     Vector aux1 = new Vector();
     Vector aux2 = new Vector();
     Vector orden1 = new Vector();
     Vector orden2 = new Vector();
 
  // Creamos un orden aleatorio entre las posiciones 

     for(h=0; h<tamChromosome ; h++) aux1.addElement(new Integer(h));
     for(h=0; h<tamChromosome ; h++) aux2.addElement(new Integer(h));
     while(aux1.size()>0){
       j = (int)(generador.nextDouble()*aux1.size());
       orden1.addElement(aux1.elementAt(j));
       aux1.removeElementAt(j);
       j = (int)(generador.nextDouble()*aux2.size());
       orden2.addElement(aux2.elementAt(j));
       aux2.removeElementAt(j);
     }

     for(j1=0;j1<tamChromosome;j1++)
       for(j2=0;j2<tamChromosome;j2++){
         index1 = ((Integer) orden1.elementAt(j1)).intValue();
         index2 = ((Integer) orden2.elementAt(j2)).intValue();
         if (index1==index2) {ind.setBit(index1,index2,0);}
         else{
           r = generador.nextDouble();
           if (r<=um.Model[index1][index2][0]) ind.setBit(index1,index2,0);
           else{
             nParents = auxGraph.parents(nodesAux.elementAt(index2)).size();
             if (nParents<maxParents){
                 ind.setBit(index1,index2,1);
                 try{
                      auxGraph.createLink(nodesAux.elementAt(index1),nodesAux.elementAt(index2));
                      nParents++;
                 }catch(InvalidEditException iee){
		      ind.setBit(index1,index2,0);
                 }
             }
             else{ // no permitimos mas padres
               ind.setBit(index1,index2,0);
               //System.out.println("I'm sorry no more parents allowed :-)");
             }

           }
         }
       }//end del for j2

     matricialDat[i]=ind;
     for(h=0 ; h<tamChromosome ; h++){
          nodesAux.elementAt(h).setParents(new LinkList());
          nodesAux.elementAt(h).setChildren(new LinkList());
          nodesAux.elementAt(h).setSiblings(new LinkList());
     }
   }//end for i 
 
 }// end generatePopulationBySampling



 /**
  * Learning an UnivariateModel from the current population using the 
  * first/best N cases
  */

 public UnivariateModelBN learnUnivariateModel(int N){
   int i,j,j1,j2,k,value;
   double total;
   UnivariateModelBN um = new UnivariateModelBN(tamChromosome);

   um.setToValue(0.0);

   for(i=0;i<N;i++)
     for(j1=0;j1<tamChromosome;j1++)
       for(j2=0;j2<tamChromosome;j2++){
         value = ((IndividualBN)matricialDat[i]).getBitAt(j1,j2);
         um.Model[j1][j2][value] += 1.0;
       } // end for j2

   // Aproximacion con la correccion de Laplace

   total = N + um.numCases;
   for(i=0;i<um.numVariables;i++)
     for(j=0;j<um.numVariables;j++)
       for(k=0;k<um.numCases;k++)
         um.Model[i][j][k] = (double)(um.Model[i][j][k]+1)/(double)total;

   // la diagonal tiene que ser 0 para uno y uno para cero
   for(i=0;i<um.numVariables;i++){
     um.Model[i][i][0]=1.0;
     um.Model[i][i][1]=0.0;
   }
    
   return um;
 } // end of learning 


 /**
  * countfor(variable,value,N)
  * counts the number of times in which variable [v1,v2] takes value in
  * the N first elements of population
  */

 public int countFor(int v1,int v2,int value,int N){
   int i,total=0;

   for(i=0;i<N;i++)
     if ( ((IndividualBN)matricialDat[i]).getBitAt(v1,v2)==value) total++;

   return total;
 }


 /**
  * getProbabiltiy(variable,value,N)
  * gets the probability of variable [v1,v2] takes value in the N first
  * elements of population
  */

 public double getProbability(int v1,int v2,int value,int N){
   int i,total=0;

   for(i=0;i<N;i++)
     if ( ((IndividualBN)matricialDat[i]).getBitAt(v1,v2)==value) total++;

   return (double)(total/(double)N);
 }



 /**
  * printPopulation
  */

 public void print(){
   int i,j,v1,v2;
   DecimalFormat df = new DecimalFormat("0.000");
   DecimalFormat df2 = new DecimalFormat("0");
    
   System.out.println("\n\nCurrent Population is: \n\n");
   for(i=0;i<populationSize;i++){
     System.out.println();
     for(v1=0;v1<tamChromosome;v1++){
       for(v2=0;v2<tamChromosome;v2++)
         System.out.print(" " + df2.format(
                  ((IndividualBN)matricialDat[i]).getBitAt(v1,v2)));
       System.out.println();
     }  
     System.out.println("---> " + df.format(this.getFitnessAt(i)));
   } // end for i
   System.out.println();

 } //end printPopulation     


/**
 * getMaximumCases
 */
public int getMaxNumberOfValuesPerGene( )
{
  return maxNumberOfValuesPerGene;
}


/**
 * Returns the log2 of the input
 */

public double log2(double x){
  return (double) (Math.log(x)/Math.log(2));
}


public IndividualBN getIndividual(int pos){

    return (IndividualBN) matricialDat[pos];

}
 
  /**
   * Take the nBest best individuals we find between the populations
   * p1 and p2 and return them in a new population.
   */
  public PopulationBN takeBest(PopulationBN p2)
  { 
     PopulationBN pop = new PopulationBN(this.tamChromosome,this.populationSize);
     int i1,i2;
     int num = 0;
     //current indexes for this and p2
     i1 = 0;
     i2 = 0;
	 
     while (num<this.populationSize)
     {
       if (this.getFitnessAt(i1)>=p2.getFitnessAt(i2))
       { 
	 pop.matricialDat[num] = this.getIndividual(i1); 
         i1++;
       }else{
         pop.matricialDat[num] = p2.getIndividual(i2); 
         i2++;
       }
       num++;
     }
		  
     return pop;

   }//end of takeBest


public double totalFitness(int n){

   int i;
   double total = 0.0;
   for(i=0; i < n ; i++) total+=(this.getFitnessAt(i));
   return total;
}
}//end of class PopulationBN
