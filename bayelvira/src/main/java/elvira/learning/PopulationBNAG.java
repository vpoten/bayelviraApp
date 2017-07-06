/* PopulationBNAG.java */ 

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


public class PopulationBNAG{


// A unidimensional array containing PopulationSize individuals

private IndividualBNAG[] matricialDat; 

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
  	  public PopulationBNAG(int dim, int tam)
	  {
		matricialDat = new IndividualBNAG[tam];
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
    matricialDat[i] = new IndividualBNAG(tamChromosome,np,generador);
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
    v = ((IndividualBNAG)matricialDat[i]).evaluate(HT,ifHT,met);
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
		return ((IndividualBNAG) matricialDat[i]).getFitness();
	   } 


/**
  * Sorts the population according to
  * the fitness.
  * Sort method: selection.
  */

public void sort( ){

  int i,k,posMayor;
  double fitnessMayor;
  IndividualBNAG mayor,aux;

  for(i=0; i<(populationSize -1) ;i++){
    
    posMayor = i;
    mayor = (IndividualBNAG) matricialDat[i];
    fitnessMayor = mayor.getFitness();  

    for(k=i+1;k<populationSize;k++){
      aux = (IndividualBNAG)matricialDat[k];
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
   IndividualBNAG ind;

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
     ind = new IndividualBNAG(tamChromosome);
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
                 ind.setBit(index1,index2,1);
                 try{
                      auxGraph.createLink(nodesAux.elementAt(index1),nodesAux.elementAt(index2));
                 }catch(InvalidEditException iee){
		      ind.setBit(index1,index2,0);
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
         value = ((IndividualBNAG)matricialDat[i]).getBitAt(j1,j2);
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
     if ( ((IndividualBNAG)matricialDat[i]).getBitAt(v1,v2)==value) total++;

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
     if ( ((IndividualBNAG)matricialDat[i]).getBitAt(v1,v2)==value) total++;

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
                  ((IndividualBNAG)matricialDat[i]).getBitAt(v1,v2)));
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


public IndividualBNAG getIndividual(int pos){

    return (IndividualBNAG) matricialDat[pos];

}
 
  /**
   * Take the nBest best individuals we find between the populations
   * p1 and p2 and return them in a new population.
   */
  public PopulationBNAG takeBest(PopulationBNAG p2)
  { 
     PopulationBNAG pop = new PopulationBNAG(this.tamChromosome,this.populationSize);
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

/**
 * This method will generate a new population by appying crossover and
 * mutation tho the one passed as parameter
 *
 * Parents are selected for crossover by using its rank
 */

public void generateNewGeneticPopulation(PopulationBNAG pop,double pM,Random generador){

  double pSelect[] = new double[populationSize];
  int i,j,pos1,pos2,cont;
  double den,r;
  IndividualBNAG children[];

  // calculating accumulated probabilities of selection
  den = (double) (populationSize*(populationSize+1))/2.0;
  pSelect[0] = (double) (populationSize/den); 
  for(i=1;i<populationSize;i++){
    pSelect[i] = pSelect[i-1] + (double) ((populationSize-i)/den);
  }  
  
  // creating the new population
  cont=0;
  for(i=0;i<populationSize/2;i++){
    //selection based on rank
    for(r=generador.nextDouble(),pos1=0; r>pSelect[pos1]; pos1++) ;
    do{
      for(r=generador.nextDouble(),pos2=0; r>pSelect[pos2]; pos2++) ;
    } while (pos1==pos2);

    // crossover and mutation
    children = uniformCrossoverAndMutation(
                              pop.getIndividual(pos1),
                              pop.getIndividual(pos2),
                              pM, generador);
    
    matricialDat[cont++]=(IndividualBNAG)children[0];
    matricialDat[cont++]=(IndividualBNAG)children[1];
  }

  if (cont != populationSize){ // odd populationSize 
    IndividualBNAG ind = new IndividualBNAG(tamChromosome);
    for(i=0;i<tamChromosome;i++)
      for(j=0;j<tamChromosome;j++)
        ind.setBit(i,j,(pop.getIndividual(0)).getBitAt(i,j));
    matricialDat[cont]=ind;
  }

}


/**
 * crossover: given two individuals performs the uniform crossover
 * operation. Individuals are created in such a way that aciclicity is 
 * warranted.
 *
 * The method returns an array with the two individuals (children)
 */

private IndividualBNAG[] uniformCrossoverAndMutation(IndividualBNAG parent1, 
              IndividualBNAG parent2, double pM, Random generador){

  IndividualBNAG children[] = new IndividualBNAG[2]; 
  IndividualBNAG child1,child2;
  Graph auxGraph1,auxGraph2;
  NodeList nodesAux1 = new NodeList();
  NodeList nodesAux2 = new NodeList();
  int i,j,pos1,pos2,index1,index2,posI,posJ;
  double r;
  int nParents, maxParents = (int) Math.round(5.0); //(double)tamChromosome/5.0);

  for(i=0 ; i<tamChromosome ; i++){
    FiniteStates node1 = new FiniteStates();
    node1.setName(new Integer(i).toString());
    nodesAux1.insertNode(node1);
    FiniteStates node2 = new FiniteStates();
    node2.setName(new Integer(i).toString());
    nodesAux2.insertNode(node2);
  }

  child1 = new IndividualBNAG(tamChromosome);
  child2 = new IndividualBNAG(tamChromosome);

  auxGraph1 = new Graph();
  auxGraph1.setNodeList(nodesAux1);
  auxGraph2 = new Graph();
  auxGraph2.setNodeList(nodesAux2);

  // we randomly select initial positions for both coordinates 

  posI = (int) (generador.nextDouble()*tamChromosome);
  posJ = (int) (generador.nextDouble()*tamChromosome);

  for(i=0;i<tamChromosome;i++)
    for(j=0;j<tamChromosome;j++){
      index1 = (posI+i)%tamChromosome;
      index2 = (posJ+j)%tamChromosome;
      r = generador.nextDouble();
      if (r<0.5){ // select from the same index p1 -> ch1, p2 -> ch2
        if (parent1.getBitAt(index1,index2) == 1){
          nParents = auxGraph1.parents(nodesAux1.elementAt(index2)).size();
          if (nParents < maxParents){
            child1.setBit(index1,index2,1);
            try{
              auxGraph1.createLink(nodesAux1.elementAt(index1),
                                 nodesAux1.elementAt(index2));
            }catch(InvalidEditException iee){
		      child1.setBit(index1,index2,0);
            }
          }
          else{ // no permitimos mas padres
            child1.setBit(index1,index2,0);
          }
        }
        else child1.setBit(index1,index2,0);

        if (parent2.getBitAt(index1,index2) == 1){
          nParents = auxGraph2.parents(nodesAux2.elementAt(index2)).size();
          if (nParents < maxParents){
            child2.setBit(index1,index2,1);
            try{
              auxGraph2.createLink(nodesAux2.elementAt(index1),
                                 nodesAux2.elementAt(index2));
            }catch(InvalidEditException iee){
		      child2.setBit(index1,index2,0);
            }
          }
          else{ // no permitimos mas padres
            child2.setBit(index1,index2,0);
          }
        }
        else child2.setBit(index1,index2,0);

      }
      else{ // select from the contrary index p1 -> ch2, p2 -> ch1
        if (parent2.getBitAt(index1,index2) == 1){
          nParents = auxGraph1.parents(nodesAux1.elementAt(index2)).size();
          if (nParents < maxParents){
            child1.setBit(index1,index2,1);
            try{
              auxGraph1.createLink(nodesAux1.elementAt(index1),
                                 nodesAux1.elementAt(index2));
            }catch(InvalidEditException iee){
		      child1.setBit(index1,index2,0);
            }
          }
          else{ // no permitimos mas padres
            child1.setBit(index1,index2,0);
          }
        }
        else child1.setBit(index1,index2,0);

        if (parent1.getBitAt(index1,index2) == 1){
          nParents = auxGraph2.parents(nodesAux2.elementAt(index2)).size();
          if (nParents < maxParents){
            child2.setBit(index1,index2,1);
            try{
              auxGraph2.createLink(nodesAux2.elementAt(index1),
                                 nodesAux2.elementAt(index2));
            }catch(InvalidEditException iee){
		      child2.setBit(index1,index2,0);
            }
          }
          else{ // no permitimos mas padres
            child2.setBit(index1,index2,0);
          }
        }
        else child2.setBit(index1,index2,0);
      }
  
    } // end crossover 


  // now we perform mutation

  // mutation for child1
  
  // we randomly select initial positions for both coordinates 

  posI = (int) (generador.nextDouble()*tamChromosome);
  posJ = (int) (generador.nextDouble()*tamChromosome);

  for(i=0;i<tamChromosome;i++)
    for(j=0;j<tamChromosome;j++){
      index1 = (posI+i)%tamChromosome;
      index2 = (posJ+j)%tamChromosome;
      if (index1!=index2){//preserving the diagonal
        r = generador.nextDouble();
        if (r <= pM) { // mutating
          if (child1.getBitAt(index1,index2)==0){ // trying to add a new link
            nParents = auxGraph1.parents(nodesAux1.elementAt(index2)).size();
            if (nParents < maxParents){
              child1.setBit(index1,index2,1);
              try{
                auxGraph1.createLink(nodesAux1.elementAt(index1),
                                 nodesAux1.elementAt(index2));
              }catch(InvalidEditException iee){
		      child1.setBit(index1,index2,0);
              }
            }
            else{ // no se permiten mas padres, asi que nasti de plasti
               // sentencia nula
            }
          }
          else{// removing the link
            child1.setBit(index1,index2,0);
            //locating the link in linkList
            Link link = auxGraph1.getLinkList().getLinks(
                                nodesAux1.elementAt(index1).getName(),
                                nodesAux1.elementAt(index2).getName());
            try{
              auxGraph1.removeLink(link);
            }catch(InvalidEditException iee){
              child1.setBit(index1,index2,0);
              System.out.println("\n---- Algo raro pasa en la mutacion ----\n");
            }
          }
        } // end if
      } // end if (diagonal)
    } // end mutating child1 

  // mutation for child2
  
  // we randomly select initial positions for both coordinates 

  posI = (int) (generador.nextDouble()*tamChromosome);
  posJ = (int) (generador.nextDouble()*tamChromosome);

  for(i=0;i<tamChromosome;i++)
    for(j=0;j<tamChromosome;j++){
      index1 = (posI+i)%tamChromosome;
      index2 = (posJ+j)%tamChromosome;
      if (index1 != index2){ // preserving the diagonal
        r = generador.nextDouble();
        if (r <= pM) { // mutating
          if (child2.getBitAt(index1,index2)==0){ // trying to add a new link
            nParents = auxGraph2.parents(nodesAux2.elementAt(index2)).size();
            if (nParents < maxParents){
              child2.setBit(index1,index2,1);
              try{
                auxGraph2.createLink(nodesAux2.elementAt(index1),
                                 nodesAux2.elementAt(index2));
              }catch(InvalidEditException iee){
		      child2.setBit(index1,index2,0);
              }
            }
            else{ //nasti de plasti, ver arriba
              // sentencia vacia
            }
          }
          else{// removing the link
            child2.setBit(index1,index2,0);
            //locating the link in linkList
            Link link = auxGraph2.getLinkList().getLinks(
                                nodesAux2.elementAt(index1).getName(),
                                nodesAux2.elementAt(index2).getName());
            try{
              auxGraph2.removeLink(link);
            }catch(InvalidEditException iee){
              child2.setBit(index1,index2,0);
              System.out.println("\n---- Algo raro pasa en la mutacion ----\n");
            }
          }
        } // end if
      } // end if diagonal
    } // end mutating child2 

  // end of mutation

  children[0] = child1;
  children[1] = child2;
  
  return children;
}


}//end of class PopulationBNAG
