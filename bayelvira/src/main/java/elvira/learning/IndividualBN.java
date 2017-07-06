/* Ant.java */

package elvira.learning;

import java.io.*;
import java.util.Vector;
import elvira.*;
import elvira.learning.*;
import java.util.Random;
import java.util.Hashtable;

/**
 * Implementa un individuo para algoritmos geneticos
 *
 * @version 0.1
 * @since 11/11/00
 */

public class IndividualBN {

/**
 * Un vector de integers, con tantas posiciones como nodos
 */
private int[][] chromosome;

/**
 * Un double indicando el fitness del individuo
 */

private double fitness;

/**
 * longitud de un lado del cromosoma
 */

private int lChrom;

// constructor

/**
 * El individuo es creado con el fitness pasado y un vector nulo
 *
 * @param fit el fitness a situar en el individuo
 */

public IndividualBN(double fit,int N){
  chromosome = new int[N][N];
  fitness=fit;
  lChrom = N;
}


/**
 * El individuo es creado con el fitness mínimo posible y
 * y un vector reservado pero sin inicializar
 *
 */

public IndividualBN(int N){
  chromosome = new int[N][N];
  fitness=(double)(-1.0/0.0);
  lChrom = N;
}



/**
 * El individuo es creado con fitness 0.0 y un vector nulo
 */

public IndividualBN(int N, double np, Random generador){

  int i,j,p;
  Vector aux = new Vector();
  Vector orden = new Vector();
  np = (int)(generador.nextDouble()*np);
  lChrom = N;

  // Creamos un orden aleatorio entre las posiciones 

  for(i=0; i<N ; i++) aux.addElement(new Integer(i));

  while(aux.size()>0){
       j = (int)(generador.nextDouble()*aux.size());
       orden.addElement(aux.elementAt(j));
       aux.removeElementAt(j);
  }

  chromosome = new int[N][N];
  fitness=0.0;

  for(i=0; i<N ; i++)
   for(j=0;j<N ; j++)
       chromosome[i][j] = 0;
 
  for (i=0 ; i < orden.size() ; i++){
    int index = ((Integer) orden.elementAt(i)).intValue();
    // tocamos aqui para que el numero maximo de padres sea como mucho
    // numero de variables/5
    int npar = Graph.poisson(generador,np);
    if (npar > Math.round((double)lChrom/5.0) ){
      npar = (int) Math.round((double)lChrom/5.0);
    }
    
    if(i<=npar)
	for(j=0; j<i ; j++) chromosome[j][i]=1; 
    else{
        for(j=0; j<npar ; j++){
          //do{
          p = (int)(generador.nextDouble()*i);
	  //}while(chromosome[((Integer)orden.elementAt(p)).intValue()][((Integer)orden.elementAt(i)).intValue()] == 1);
          chromosome[((Integer)orden.elementAt(p)).intValue()]
                     [((Integer)orden.elementAt(i)).intValue()] = 1;
        }
    }
  }         
  
}



// metodos de acceso

/**
 * getChromosome: devuelve el cromosoma
 */

 public int[][] getChromosome(){
   return chromosome;
 }

/**
 * setChromosome: pone como cromosoma el vector de pasado
 * Importante, no se controla que sea un vector de Integers
 * 
 * @param v un vector de Integer
 */

 public void setChromosome(int[][] v){
   chromosome = v;
 }

/**
 * getFitness: devuelve el fitness del individuo
 */

 public double getFitness(){
   return fitness;
 }

/**
 * setFitness: pone como fitness el double pasado
 *
 * @param d el valor que se pondra como fitness
 */

 public void setFitness(double d){
   fitness = d;
 }

/**
 * Sets the value for a position of chromosome
 */

public void setBit(int i,int j,int value){
  chromosome[i][j]=value;
}

/**
 * Gets the value for a position of chromosome
 */

public int getBitAt(int i,int j){
  return chromosome[i][j];
}


/**
 * Evaluate: obtains its fitness
 * returns 1 if a evaluation has been carried out 
 * and 0 in other case (fit taken from the hashtable)
 */

public int evaluate(Hashtable HT,int ifHT, Metrics met){
  int i,j,eval=0;
  Double D;  

  if ((ifHT==1) && HT.containsKey(chromosome.toString()) ){
      D = (Double) HT.get(chromosome.toString());
      fitness = D.doubleValue();
  }
  else{
    eval=1;    
    Bnet b = this.getBnet(met);
    fitness = met.score(b);
  } 

  return eval;
}

public Bnet getBnet(Metrics met){

 int i,j;

 Bnet resul = new Bnet();
 NodeList nodes = met.getData().getNodeList().duplicate();
 resul.setNodeList(nodes);
 
 for(i=0 ; i<lChrom ; i++)
   for(j=0; j<lChrom ; j++){
     if(chromosome[i][j] == 1){
       try{
	Node nodeT = nodes.elementAt(i);
        Node nodeH = nodes.elementAt(j);
	resul.createLink(nodeT,nodeH);
       }catch(InvalidEditException iee){};
     }
   }
 return resul;

}

public String getKey(){
 
   return (chromosome.toString()); 

}

} // end of class
