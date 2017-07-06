package elvira.learning;

import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * K2TSPLearning.java
 *
 *
 * Created: Tue Nov 23 10:08:13 1999
 *
 * @author P. Elvira
 * @version 1.0
 */


public class K2TSPLearning extends K2Learning {

int LONGITUD=0;
int Pobl[][];
double Eval[];
double sumaFO;
int indiv1,indiv2, BestInd, MaxParents;
int BestIndBits[]; 
Bnet baprend1;
DataBaseCases input;
Node nodei,nodej;
NodeList nodesSorted = new NodeList();
K2Learning metric2;

 public static void main(String args[]) throws ParseException, IOException { 

     Bnet baprend;
     FileWriter f2;
     DataBaseCases cases;
     int i,j,k;
     

      if(args.length < 3){
	  System.out.println("too few arguments: Usage: file.dbc numberOfMaxParents file.elv TamPop MaxIter MutProb");
	  System.exit(0);
      }

      FileInputStream f = new FileInputStream(args[0]);
      cases = new DataBaseCases(f);
      
      K2TSPLearning outputNet1 = new K2TSPLearning(cases,Integer.valueOf(args[1]).intValue());

      outputNet1.input = cases;
      outputNet1.MaxParents = Integer.valueOf(args[1]).intValue();

      outputNet1.LONGITUD = cases.getVariables().size(); //NumVars*NumVars
      //outputNet1.LONGITUD *= outputNet1.LONGITUD;     
	
      outputNet1.BestIndBits = new int[outputNet1.LONGITUD];

      outputNet1.baprend1 = cases; 

      outputNet1.mainloop(Integer.valueOf(args[3]).intValue(),Integer.valueOf(args[4]).intValue(), Double.valueOf(args[5]).doubleValue());

      //System.out.println("Best nodes order ");
      outputNet1.MuestraListaPermut  (outputNet1.BestIndBits,1.0);

      //Insert the bodes in the order corresponding to the best individual
      j=cases.getNodeList().size();
      outputNet1.nodesSorted = new NodeList();
      for(i=0; i< j; i++){
          outputNet1.nodei = (Node) cases.getNodeList().elementAt(outputNet1.BestIndBits[i]-1);
          outputNet1.nodesSorted.insertNode(outputNet1.nodei);
      }
      //System.out.println("The list has "+outputNet1.nodesSorted.size()+" nodes");

      // K2 metric value of the Bayesian Network
      outputNet1.metric2 = new K2Learning(cases, outputNet1.nodesSorted, outputNet1.MaxParents);
      outputNet1.metric2.learning();
      baprend = (Bnet) outputNet1.metric2.getOutput();


      f2 = new FileWriter(args[2]);
      //baprend = (Bnet)outputNet1.getOutput();
      baprend.saveBnet(f2);
      f2.close();

   }  

    public K2TSPLearning(){
	super();
    }

    public K2TSPLearning(DataBaseCases cases,int nMaxParents){
	super(cases,nMaxParents);
    }

   // Return a random bit (0 or 1).
   public static int BitRand() {
      return (int)(Math.random()*2);
   }

int intRand(int n)
{// a random number between 0 and n-1
    return (int)(Math.random()*n);
}

double doubleRand ()
{// a random number in the [0,1] interval
	return Math.random();
}

//------------------------------------------------------------------------

   public void setInput(DataBaseCases db) {
      input = db;
   }
   
   public void setMaxParents (int m) {
      MaxParents = m;
   }

   public Bnet learnTSP(int tamPop, int maxIter, int crossProb) {
      int i, j;
      
      LONGITUD = input.getVariables().size(); //NumVars*NumVars
      //outputNet1.LONGITUD *= outputNet1.LONGITUD;     
	
      BestIndBits = new int[LONGITUD];

      baprend1 = input; 

      mainloop(tamPop, maxIter, crossProb);

      System.out.println("Fin mainloop");
      System.out.println("El mejor orden entre los nodos es");
      MuestraListaPermut (BestIndBits,1.0);
      //baprend = new Bnet();

      //Insert the bodes in the order corresponding to the best individual
      j=input.getNodeList().size();
      nodesSorted = new NodeList();
      for(i=0; i< j; i++){
          nodei = (Node) input.getNodeList().elementAt(BestIndBits[i]-1);
          nodesSorted.insertNode(nodei);
      }
      System.out.println("La lista tiene "+nodesSorted.size()+" nodos");
/*
      for(i=0; i< j; i++){
         System.out.println("Borrando: "+i);
         outputNet1.nodei = (Node) cases.getNodeList().elementAt(0);
         cases.removeNode(outputNet1.nodei);
      }
      for(i=0; i< j; i++){
          System.out.println("Añadiendo: "+i);
          outputNet1.nodei = (Node) outputNet1.nodesSorted.elementAt(i);
	    try{
		cases.addNode(outputNet1.nodei);
	    }catch (InvalidEditException e){};
      }
*/
//      cases.setNodeList(outputNet1.nodesSorted);
      // K2 metric value of the Bayesian Network
      metric2 = new K2Learning(input, nodesSorted, MaxParents);
      metric2.learning();
      return (Bnet) metric2.getOutput();
   }


//Genetic algorithm: main function
void mainloop(int TamPob, int maxIter, double pm)
{

	int iter, hijo1, hijo2, topeHijos, mejor, peor, i, Condicion, Loops;
        double BestValue;
	//la población es el doble que el tamaño de la población, para poder contener a los hijos.
	int informar;

	informar = 1;

	//population size is TamPob + 2 in order to save also the maximum 
        // number of children, i.e, two.
        // Crossover function could return 0, 1 or 2 new individuals.

	Eval = new double[TamPob+2];

	mejor = 0;
	sumaFO = 0;
	peor = 0;

	CrearListaPermut(TamPob+2, LONGITUD);

	for(i=0;i<TamPob;i++)
	{
		InicAleatListaPermut(Pobl[i]);
		Eval[i] = evalind(Pobl[i]);
		sumaFO = sumaFO + Eval[i];
		if (Eval[i] > Eval[mejor])
			mejor = i;
		else if (Eval[i] < Eval[peor])
			peor = i;
	}


	topeHijos = TamPob;
	iter = 0;
        Condicion=0;
        Loops = 0;
        BestValue=0.0;
	while ((iter < maxIter) && (Condicion == 0))
	{
		iter++;

		SelecFuncObjMax(Eval, TamPob, sumaFO);
		hijo1 = topeHijos;
		hijo2 = hijo1+1;

		CruzarListaPermut(Pobl,indiv1,indiv2,hijo1,1.0);
		

		SelecFuncObjMax(Eval, TamPob, sumaFO);


		CruzarListaPermut(Pobl,indiv1,indiv2,hijo2,1.0);

			
		MutaListaPermut(Pobl[hijo1],pm);
		MutaListaPermut(Pobl[hijo2],pm);
		
		for(i=TamPob;i<TamPob+2;i++)
			Eval[i] = evalind(Pobl[i]);

		if(Eval[hijo2] > Eval[hijo1])
		{
			hijo1 = hijo2;
		}

		//Populatuon reduction. Elitism criteria is used for this task.
		if (Eval[peor] < Eval[hijo1])
		{
			CopiaListaPermut(Pobl[hijo1], Pobl[peor]);
			Eval[peor] = Eval[hijo1];
		}

		//Population total value, best individual value, worst individual value
		sumaFO = 0;
		mejor = 0;
		peor = 0;
		for(i=0;i<TamPob;i++)
		{
			sumaFO = sumaFO + Eval[i];
			if (Eval[i] > Eval[mejor])
				mejor = i;
			else if (Eval[i] < Eval[peor])
				peor = i;
		}

                if (Eval[mejor] > BestValue)
                  {
                   BestValue = Eval[mejor];
                   Loops = 0;
		  }
		else Loops++;

		if(informar == 1)
			System.out.println("Generation = ["+iter+"]\tnow = ["+Eval[mejor]+"]\n");

	 if (Loops > (4*TamPob)) // 4 generations
            Condicion=1;
	}
	System.out.println("Number of Iterations: "+iter);
        
        // As result, the best individual is given to the class 
	BestInd=mejor;
	CopiaListaPermut(Pobl[mejor], BestIndBits);
        
}

//------------------------------------------------------------------------
//Individual related functions (Permut)
void CrearListaPermut (int TamPop, int TamInd )
{
	Pobl = new int[TamPop][TamInd];
}


void CopiaListaPermut (int[] desde, int[] hasta)
{
	int i;
	for(i=0;i<LONGITUD;i++)
		hasta[i] = desde[i];
}


void InicAleatListaPermut  (int[] sol)
{
	int i,j,k,AddedInts,Aux;
        int[] Canonic;

        Canonic = new int[LONGITUD];
        //Selected = new int[LONGITUD];
        for (i=1;i<=LONGITUD;i++) {
           Canonic[i-1]=i;
//	   System.out.println("canonical permutation: "+i+"th bit value is "+Canonic[i-1]);
        } 	
        AddedInts=LONGITUD;
        while (AddedInts>1) {
           j = intRand(AddedInts);
           sol[LONGITUD-AddedInts] = Canonic[j];
//	   System.out.println("bit "+(LONGITUD-AddedInts)+" Value is: "+sol[LONGITUD-AddedInts]);
           AddedInts--;
	   for (k=j;k<AddedInts;k++)
                Canonic[k]=Canonic[k+1];
        }
	sol[LONGITUD-1]=Canonic[0];
}

void MuestraListaPermut  (int[] sol, double result)
{
	int i;
	System.out.print("[");
	for(i=0;i<LONGITUD-1;i++)
 	   System.out.print(sol[i]);
	System.out.println(sol[LONGITUD-1]+"] --> "+result);
}


//------------------------------------------------------------------------
//Genetic Algorithm functions
 
void CruzarListaPermut(int Pobl[][], int mom_index, int dad_index, int kid_index, double pc) {

int mom[]=Pobl[mom_index];
int dad[]=Pobl[dad_index];
int kid[]=Pobl[kid_index];
//int 	  length;
int[]      failed= new int[LONGITUD];
int[]      from= new int[LONGITUD];
int[]      indx= new int[LONGITUD];
int[]      check_list= new int[LONGITUD*10];
 
int left, right, temp, i, j, k;
int mx_fail, done, found, mx_hold;
int d_gene=1;                 // indicator for gene from dad 
int m_gene=0;                 // indicator for gene from mom 

 

/* no mutation so start up the pmx replacement algorithm */
  for (k = 0; k < LONGITUD; k++) {
    failed[k] = -1;
    from[k] = -1;
    check_list[k+1] = 0;
  }
  

  left = intRand(LONGITUD);              /* locate crossover points      */
  right = intRand(LONGITUD); 
  if (left > right) {
    temp = left;
    left = right;
    right = temp;
  }
 
  for (k = 0; k < LONGITUD; k++)    {          /* copy P2 int offspring        */
    kid[k] = dad[k];
    from[k] = d_gene;
    check_list[dad[k]]++;
  }
 
  for (k = left; k <= right; k++) {
    check_list[kid[k]]--;
    kid[k] = mom[k];
    from[k] = m_gene;
    check_list[mom[k]]++;
  }

  mx_fail = 0;
  for (k = left; k <= right; k++)   {   /* for all elements in the P1-2 */
    if (mom[k] == dad[k]) 		/* segment find the match in P2 */
      found = 1;

    else	 {
      found = 0;                          /* and substitute element of  */
      j = 0;                                  /* P2-2 corresponding to elem*/

      while ((found==0) && (j < LONGITUD))   {   /* in P2-2 into the offspring */
        if ((kid[j] == mom[k])  && (from[j] == d_gene)) {
	  check_list[kid[j]]--;
          kid[j] = dad[k];
          found = 1;
	  check_list[dad[k]]++;
        }  /* end if */

        j++;

      }  /* end while */
    }   /* end else */

    if (found==0)     {                     /* then gene was not replaced   */
      failed[mx_fail] = mom[k];
      indx[mx_fail] = k;
      mx_fail++;
    }   /* end if */

  }  /* end for */
 
  /* now to see if any genes could not be replaced */
  if (mx_fail > 0) {
    mx_hold = mx_fail;

    for (k = 0; k < mx_hold; k++) {
      found = 0;
      j = 0;

      while ((found==0) && (j < LONGITUD)) {

        if ((failed[k] == kid[j]) && (from[j] == d_gene)) {
	  check_list[kid[j]]--;
          kid[j] = dad[indx[k]];
	  check_list[dad[indx[k]]]++;
          found = 1;
          failed[k] = -1;
	  mx_fail--;
        }	/* end if    */

        j++;

      }		/* end while */
    }		/* end for   */
  }		/* end if    */

  
  for (k = 1; k <= LONGITUD; k++) {

    if (check_list[k] > 1) {
      i = 0;

      while (i < LONGITUD) {
        if ((kid[i] == k) && (from[i] == d_gene)) {
	  j = 1;

	  while (j <= LONGITUD) {
	    if (check_list[j] == 0) {
	      kid[i] = j;
	      check_list[k]--;
	      check_list[j]++;
	      i = LONGITUD + 1;
	      j = i;
	    } 	/* end if */

	    j++;
	  }	/* end while */
	}	/* end if    */

	i++;
      }  	/* end while */

    }		/*  end if   */
  }		/* end for   */

}



void CruzarListaPermut1 (int Pobl[][], int i1, 
			    int i2, int h1, int h2, double pc)
{
	int i,punto1,punto2;
	
	if(pc > doubleRand())
	{
		punto1 = intRand(LONGITUD-1)+1;
                do 
   		 punto2 = intRand(LONGITUD-1)+1;
                 while(punto2==punto1);

		for(i=0;i<punto1;i++)
		{
			(Pobl[h1])[i] = (Pobl[i1])[i];
			(Pobl[h2])[i] = (Pobl[i2])[i];
		}
		for(i=punto2;i<LONGITUD;i++)
		{
			(Pobl[h1])[i] = (Pobl[i2])[i];
			(Pobl[h2])[i] = (Pobl[i1])[i];
		}
	}
	else
	{
		h1 = -1;
		h2 = -1;
	}
}


void MutaListaPermut (int[] indiv, double pm)
{
	int gen1,gen2,genAux;
	if(pm < doubleRand())
	{
		gen1 = intRand(LONGITUD);

		do 
		  gen2 = intRand(LONGITUD);
		while (gen2 == gen1);
                
                genAux = indiv[gen1];
                indiv[gen1]=indiv[gen2];
                indiv[gen2]=genAux;
	}
}


void SelecFuncObjMax (double[] eval, int tampob, double sumafo)
{
	double Prob[];
	double aleat;
	int i,h1,h2;		
	int iguales;

	//The selection probability of individuals is proportional to its fitness value
        Prob = new double[tampob];
	Prob[0] = eval[0] / sumafo;
	for(i=1;i<tampob-1;i++)
		Prob[i] = Prob[i-1] + eval[i]/sumafo;
	Prob[tampob-1] = 1;

	//Select the first parent
	h1 = 0;
	aleat = doubleRand();
	while((h1<tampob)&&(Prob[h1]<aleat))
		h1++;
        
        indiv1=h1;

	//The second will be different. This is not necessary.
	iguales = 1;
        h2=0;
	while(iguales != 0)
	{
		h2 = 0;
		aleat = doubleRand();
		while((h2<tampob)&&(Prob[h2]<aleat))
			h2++;
		if(h1 != h2)
			iguales = 0;
	}	
        indiv2=h2;
}

// Individual evaluation and reparation. We aboid cycles in the structure by deleting arcs
// Arcs are randomly selected to be includeed in the Bayesian network, and those which 
// close a cycle are removed (not inserted). 
double evalind(int[] Individual)
{double IndValue=0.0;
 Bnet currentBnet;
 Node nodei,nodej;
 int i,j,k,ContLinks,Arcs[];
 K2Learning metricAux;
 nodesSorted = new NodeList();

 //currentBnet = new Bnet();
 for(i=0 ; i< baprend1.getNodeList().size(); i++){
    nodei = (Node) baprend1.getNodeList().elementAt(Individual[i]-1);
    nodesSorted.insertNode(nodei);
  }

 // K2 metric value of the Batesian Network
 metricAux = new K2Learning(input, nodesSorted, MaxParents);
 metricAux.learning();
 currentBnet = (Bnet) metricAux.getOutput();

 IndValue = metric.score(currentBnet);
 System.out.println("BNet Value: "+IndValue);

 return IndValue;
}

} // K2TSPLearning
