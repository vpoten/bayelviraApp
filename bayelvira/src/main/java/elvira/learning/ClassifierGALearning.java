package elvira.learning;

import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * ClassifierGALearning.java
 *
 *
 * Created: Tue Nov 23 10:08:13 1999
 *
 * @author P. Elvira
 * @version 1.0
 */


public class ClassifierGALearning extends ClassifierLearning {

int LONGITUD=0;
int Pobl[][];
double Eval[];
double sumaFO;
int indiv1,indiv2, BestInd;
int BestIndBits[]; 
Bnet baprend1=new Bnet();
Node nodei,nodej;
//DataBaseCases cases;

 public static void main(String args[]) throws ParseException, IOException { 

     Bnet baprend;
     DataBaseCases cases;
     FileWriter f2;
     int i,j,k;
     

      if(args.length < 3){
	  System.out.println("too few arguments: Usage: file.dbc file.elv TamPop MaxIter CrossProb");
	  System.exit(0);
      }

      FileInputStream f = new FileInputStream(args[0]);
      cases = new DataBaseCases(f);

      baprend = (Bnet) cases;
      ClassifierGALearning outputNet1 = new ClassifierGALearning(cases,baprend);
      
      outputNet1.LONGITUD = cases.getVariables().size(); //NumVars*NumVars
      outputNet1.LONGITUD *= outputNet1.LONGITUD;     

      outputNet1.baprend1 = cases; 
	
      outputNet1.BestIndBits = new int[outputNet1.LONGITUD];

      outputNet1.mainloop(Integer.valueOf(args[2]).intValue(),Integer.valueOf(args[3]).intValue(), Double.valueOf(args[4]).doubleValue());

      baprend = cases;
      k=0; 
	//There are no cycles in this individual, which represents the best BN structure
      	//found by the GA taking into account the k2 score.
      for(i=0 ; i< baprend.getNodeList().size(); i++){
        for(j=0 ; j< baprend.getNodeList().size(); j++){
         if (outputNet1.BestIndBits[k]==1) {// i parent of j
              outputNet1.nodei = (Node) baprend.getNodeList().elementAt(i);
              outputNet1.nodej = (Node) baprend.getNodeList().elementAt(j);
            try{
     	        baprend.createLink(outputNet1.nodei,outputNet1.nodej);
            }catch (InvalidEditException e){};
         }
        k++;
       }
      }
 
      f2 = new FileWriter(args[1]);
      //baprend = (Bnet)outputNet1.getOutput();
      baprend.saveBnet(f2);
      f2.close();

   }  

    public ClassifierGALearning(){
	super();
    }

    public ClassifierGALearning(DataBaseCases cases,Bnet b1){
	//super(cases,nMaxParents);
        //cases = new DataBaseCases(f);
	//baprend1 = new Bnet();

	//super(cases,b1);
	super(cases);
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


   public Bnet learn(int tamPop, int maxIter, int crossProb) {
      int i,j,k;
      Bnet baprend;
      
      LONGITUD = input.getVariables().size(); //NumVars*NumVars
      LONGITUD *= LONGITUD;     

      baprend1 = input; 
   	
      BestIndBits = new int[LONGITUD];

      mainloop(tamPop, maxIter, crossProb);

      baprend = input;
      k=0; 
      //There are no cycles in this individual, which represents the best BN structure
      //found by the GA taking into account the k2 score.
      for(i=0 ; i< baprend.getNodeList().size(); i++){
         for(j=0 ; j< baprend.getNodeList().size(); j++){
         if (BestIndBits[k]==1) {// i parent of j
               nodei = (Node) baprend.getNodeList().elementAt(i);
               nodej = (Node) baprend.getNodeList().elementAt(j);
            try{
     	         baprend.createLink(nodei,nodej);
            }catch (InvalidEditException e){};
         }
         k++;
         }
      }
      
      return baprend;
      
   }

//------------------------------------------------------------------------


//Genetic algorithm: main function
void mainloop(int TamPob, int maxIter, double pm) // throws ParseException, IOException
{

	int iter, hijo1, hijo2, topeHijos, mejor, peor, i, Condicion, Loops;
        double BestValue;
	//la población es el doble que el tamaño de la población, para poder contener a los hijos.
	int informar;

	informar = 1;

	//el tamaño de la población es TamPob + 2, con el fin de alojar
	//a toda la población, más el máximo número de hijos, que es dos.
	//El máximo número de hijos es dos, ya que las funciones de cruce
	//pueden devolver, 0,1 o 2 hijos, según la probabilidad de cruce.
	//como en este caso la Pc es 1, siempre devolverán dos hijos, por
	//lo que hace falta espacio para alojarlos.

	Eval = new double[TamPob+2];

	mejor = 0;
	sumaFO = 0;
	peor = 0;

	CrearListaBin(TamPob+2, LONGITUD);

	for(i=0;i<TamPob;i++)
	{
		InicAleatListaBin(Pobl[i]);
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

		CruzarListaBinUnPunto(Pobl,indiv1,indiv2,hijo1,hijo2,1.0);
			
		MutaListaBin(Pobl[hijo1],pm);
		MutaListaBin(Pobl[hijo2],pm);
		
		for(i=TamPob;i<TamPob+2;i++)
			Eval[i] = evalind(Pobl[i]);

		//dejo en hijo1, el mejor de los hijos
		if(Eval[hijo2] > Eval[hijo1])
		{
			hijo1 = hijo2;
		}

		//reduzco la población. Para ello, miro si el hijo elegido
		//es mejor que el peor de la población, y lo copio
		if (Eval[peor] < Eval[hijo1])
		{
			CopiaListaBin(Pobl[hijo1], Pobl[peor]);
			Eval[peor] = Eval[hijo1];
		}

		//hallo el sumatorio de las funciones objetivos de la población actual
		//y el mejor individuo de la nueva población así como el peor
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
		// Ha cambiado el mejor?
                if (Eval[mejor] > BestValue)
                  {
                   BestValue = Eval[mejor];
                   Loops = 0;
		  }
		else Loops++;

		if(informar == 1)
			System.out.println("Generacion = ["+iter+"]\tactual = ["+Eval[mejor]+"]\n");

	 if (Loops > (4*TamPob)) // 4 generations
            Condicion=1;
	}
	System.out.println("Numero de Iteraciones: "+iter);
        
        // As result, the best individual is given to the class 
	BestInd=mejor;
	CopiaListaBin(Pobl[mejor], BestIndBits);
        
}

//------------------------------------------------------------------------
//binary functions
void CrearListaBin (int TamPop, int TamInd )
{
	Pobl = new int[TamPop][TamInd];
}


void CopiaListaBin (int[] desde, int[] hasta)
{
	int i;
	for(i=0;i<LONGITUD;i++)
		hasta[i] = desde[i];
}


void InicAleatListaBin  (int[] sol)
{
	int i;
	for(i=0;i<LONGITUD;i++)
	{
		sol[i] = BitRand();
	}	
}

void MuestraListaBin  (String nom, int[] sol, double result)
{
	int i;
	System.out.print("[");
	for(i=0;i<LONGITUD-1;i++)
 	   System.out.print(sol[i]);
	System.out.println(sol[LONGITUD-1]+"] --> "+result);
}


//------------------------------------------------------------------------
//Genetic Algorithm functions
void CruzarListaBinUnPunto (int Pobl[][], int i1, 
			    int i2, int h1, int h2, double pc)
{
	int i,punto;
	
	if(pc > doubleRand())
	{
		punto = intRand(LONGITUD);
		for(i=0;i<punto;i++)
		{
			(Pobl[h1])[i] = (Pobl[i1])[i];
			(Pobl[h2])[i] = (Pobl[i2])[i];
		}
		for(i=punto;i<LONGITUD;i++)
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


void MutaListaBin (int[] indiv, double pm)
{
	int gen;
	if(pm < doubleRand())
	{
		gen = intRand(LONGITUD);
                if (indiv[gen]==0) indiv[gen]=1;
                  else indiv[gen]=0;
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
double evalind(int[] Individual) //, DataBaseCases cases) //, String fname) throws ParseException, IOException
{double IndValue=0.0;
 //Bnet currentBnet;
 Bnet currentBnet;
 Node nodei,nodej;
 int i,j,k,ContLinks,Arcs[];
 LinkList AllLinks, AllLinksAux;
 Link newLink;
 EvalasClass  EvalMetric; 
 nodesSorted = new NodeList();

 //NOTA: No hacerlos sobre input, sino sobre una red bayesiana: currentBnet!!!!

 //FileInputStream f = new FileInputStream(fname);
 //currentBnet = new DataBaseCases(f);//(DataBaseCases) new Bnet();
 System.out.println("Empieza evaluar"); 

 currentBnet = new Bnet();

 for(i=0 ; i< input.getNodeList().size(); i++){
    nodei = (Node) input.getNodeList().elementAt(i);
    try{
	currentBnet.addNode(nodei);
    }catch (InvalidEditException e){};
  }

 System.out.println("Elige los arcos"); 
 Arcs = new int[LONGITUD];
 AllLinks = new LinkList();
 AllLinksAux = new LinkList();
 newLink = new Link();
 k=0;
 ContLinks=0;
 //System.out.println("Va a incluir "+baprend1.getNodeList().size()+" arcos");
 for(i=0 ; i< currentBnet.getNodeList().size(); i++){
   for(j=0 ; j< currentBnet.getNodeList().size(); j++){
    if (j==i) continue;
    if (Individual[k]==1) {// node_i parent of node_j
         nodei = (Node) currentBnet.getNodeList().elementAt(i); //baprend1 ???
         nodej = (Node) currentBnet.getNodeList().elementAt(j);
 	 newLink = new Link(nodei,nodej);
//         System.out.println("Creo el arco: "+newLink.toString());
         AllLinks.insertLink(newLink);
	 Arcs[ContLinks]=k;
	 ContLinks++;
    }
   k++;
  }
 }

 // Select the arc to include (randomly)
 System.out.println("Va a crearlos"); 
 while (ContLinks>0)
   {int SelectedArc;
    SelectedArc=intRand(ContLinks);
    newLink = (Link)AllLinks.elementAt(SelectedArc);
       if (!currentBnet.hasCycle(newLink.getTail(), newLink.getHead())) { //CurrentBnet ???
          try{
  	      currentBnet.createLink(newLink.getTail(), newLink.getHead());
              System.out.println("Creo el arco: "+newLink.toString());
              //AllLinksAux.insertLink(newLink);
          }catch (InvalidEditException e){};
       }
	 else {
           k=Arcs[ContLinks];
           Individual[k]=0; // Individual is repared by deleting the arc
           }
     AllLinks.removeLink(newLink);
     ContLinks--;
   }

 // LPNormalize?
 LPLearning lpnormalizated = new LPLearning(input, currentBnet);
 lpnormalizated.learning();
 Bnet normalizedBnet = lpnormalizated.getOutput();

 // metric value of the Bayesian Network
 System.out.println("Va a crear la metrica"); 
// EvalMetric = new EvalasClass(input,currentBnet);
 EvalMetric = new EvalasClass(input,normalizedBnet);
 System.out.println("Va a evaluar"); 
 //setInput(input);
 //setMetric(EvalMetric);
 IndValue = EvalMetric.wellclassified();
 //IndValue = metric.wellclassified();
// Falta eliminar los arcos creados.
for (i=0;i<AllLinksAux.size();i++)
   {
		try {
    input.removeLink(AllLinksAux.elementAt(i).getTail(), AllLinksAux.elementAt(i).getHead());
		} catch (InvalidEditException iee) {};
   }

 System.out.println("BNet Value: "+IndValue);

 return IndValue;
}

} // ClassifierGALearning
