/* EvalasClass.java */

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
import elvira.inference.elimination.*;
//import elvira.inference.clustering.*;//HuginPropagation;
import elvira.parser.ParseException;

/**
 * EvalasClass.java
 *
 *
 * Created: Mon Nov  8 11:09:40 1999
 *
 * @author P. Elvira
 * @version  1.0
 */

public class EvalasClass extends Metrics {
    //LogFactorial f; // For storing and computing the log factorial n.
    int NumCasesDB;  
    Bnet ClassifierCandidate;
    
    public EvalasClass() {
    	//f = new LogFactorial();
	setData(null);
	ClassifierCandidate = new Bnet();
	NumCasesDB=1;
    }
    
    public EvalasClass(DataBaseCases data, Bnet currentBnet){
    	//f = new LogFactorial();
        
	setData(data);
	ClassifierCandidate = currentBnet;
        NumCasesDB=data.getNumberOfCases();
    }


    public double score (Bnet b){
	
	NodeList vars,parentsX,varsXPa;
	int i,j,k; //NumCasesDB;
	double logSum = 0.0;
	double valscore, Penalization, PenalAux, ValXi;
	FiniteStates nodeX,nodeY;
        double bic;
	//int no_j;

	//no_j=1;
	//for(int j=0;j<IND_SIZE;j++) 
	//	if(m_parents[node][j]) no_j *= STATES[j];

        //System.out.println("Vamoalla score");
	
        Penalization=1.0;
	vars = b.getNodeList();

	for(i=0; i< vars.size(); i++){
//            System.out.println("Eval Bnet: Var "+i);
	    nodeX = (FiniteStates) vars.elementAt(i);
	    parentsX = b.parents(nodeX);
            // valores de los padres
            PenalAux=1.0;
            k=parentsX.size();
   	    for(j=0; j<k;  j++){
             FiniteStates nodeZ;

             nodeZ = (FiniteStates) parentsX.elementAt(j);
             PenalAux *= nodeZ.getNumStates();
            }
	    varsXPa = new NodeList();
	    varsXPa.insertNode(nodeX);
	    varsXPa.join(parentsX);
	    valscore = score(varsXPa);
	    logSum+=valscore;
//            System.out.println("Valscore "+valscore);
//            System.out.println("logsum "+logSum);
            ValXi=nodeX.getNumStates();
            Penalization *= PenalAux*(ValXi-1);
	}

	//bic -= log((double)NUM_CASES)*no_j/2;

        //System.out.println("Valor de la red: "+logSum+" penal"+((Math.log(NumCasesDB))*Penalization));
        return -(logSum -(0.5*Math.log(NumCasesDB)*Penalization)); // Penalization
    }


    public double score (NodeList vars){ // BIC metric

	Configuration conf,confPa;
	PotentialTable totalPot,parentsPot;
	double sum = 0.0;
	NodeList ParentsXi,varsaux;
	FiniteStates Xi;
	int nStatesXi,k,pos,Nij,Nijk, NumVar;
	double j,nconfPa,partialSum,div;

   for (NumVar=0; NumVar<vars.size(); NumVar++) {
	
	Xi = (FiniteStates)vars.elementAt(NumVar);

//        System.out.print("Score: nodo ");
//        System.out.println(Xi.toString());

	totalPot = getData().getPotentialTable(vars);
	Nij=0;
	parentsPot = null;
	if(vars.size()>1){
	    parentsPot = (PotentialTable)totalPot.addVariable(Xi);
	    ParentsXi = vars.copy();
	    ParentsXi.removeNode(Xi);
	    confPa = new Configuration(ParentsXi);
	    nconfPa = FiniteStates.getSize(ParentsXi);
	}
	else{
	    nconfPa = 1.0;
	    varsaux = new NodeList();
	    varsaux.insertNode(Xi);
	    confPa = new Configuration(varsaux);
	    Nij = (int)totalPot.totalPotential();
//            System.out.println("Unico nodo: Nij vale "+Nij);
            //System.out.println(Nij);
	}
	    
	nStatesXi = Xi.getNumStates();
	
	for(j=0.0 ; j< nconfPa ; j++ ){
            Nijk=0;
	    for(k=0 ; k< nStatesXi ; k++){
		conf = new Configuration(vars.toVector(),confPa);
		conf.putValue(Xi,k);
		pos = conf.getIndexInTable();
		Nijk =(int)totalPot.getValue(pos);
//                System.out.print("En el for Nijk vale ");
//                System.out.println(Nijk);
//	    }
	    if(vars.size()>1){
		pos = confPa.getIndexInTable();
		Nij=(int)parentsPot.getValue(pos);
//                System.out.print("En el for Nij vale ");
//                System.out.println(Nij);
	    }
            partialSum=0;
	    if ((Nij>0) && (Nijk>0)) {
//                 System.out.print("Al final Nijk vale ");
//                 System.out.println(Nijk);
//                 System.out.print("Al final Nij vale ");
//                 System.out.println(Nij);
                 //partialSum = Nij*(Math.log(Nijk/Nij));
		 // PRUE
		 div=(double) ((double)Nijk/(double)Nij);
//                 System.out.print("Logaritmo ");
//		 System.out.println(Math.log(div));
//		 System.out.println(div);
                 if (Nij!=0)
		   partialSum = Nij*(Math.log(div));
//                 System.out.print("Suma parcial ");
//		 System.out.println(partialSum);
                }
//                 else partialSum=0;
           
	    sum+=partialSum;
          }
	    confPa.nextConfiguration();
	}
     } 	
	return sum;
    }

//    public double wellclassified (DataBaseCases b){
    public double wellclassified (){
	
	NodeList vars;
	int i,j,k; 
	FiniteStates nodeX,nodeY;
        //HuginPropagation CaseAct;
        VariableElimination CaseAct;
        Evidence EvidAct;
        CaseList cases;
	Configuration conf;
        PotentialTable pot;
        NodeList sigma;
	Vector relationList;
        int[] indexOfvars;
        long Wellclassified=0;
        double selected_value;
        int selected,variable_value,num_vars,Case;
       
	
//	vars = this.getData().getNodeList();
	//((Bnet)getData()).compile();

	vars = ClassifierCandidate.getNodeList();//getData().getNodeList();

	//((PotentialTable) getData().   ).LPNormalize();

        num_vars=vars.size();
        indexOfvars = new int[vars.size()];
        for(i=0;i<vars.size();i++){
	  int pos = getData().getVariables().getId(vars.elementAt(i));//this.getData().getVariables().getId(vars.elementAt(i));
	  indexOfvars[i]=pos;
        }

        //CaseAct = new HuginPropagation(b);
       	//CaseAct.totalPot = getData().getPotentialTable(vars);
    
	//pot = getData().getPotentialTable(vars); //PRUE
        System.out.println("Paso 1.1: Casos "+NumCasesDB);

        //relationList=(Vector)(getData().getRelationList()).clone();      

        

	//cases = (CaseList)((Relation) relationList.elementAt(0)).getValues();
	System.out.println("Numero de relaciones: "+getData().getRelationList().size());
	if (getData().getRelationList().size()==0) return 0;
        System.out.println("Paso 1.1.1");
        cases = (CaseList)((Relation)getData().getRelationList().elementAt(0)).getValues();
        //cases = (CaseList) (((Relation) ClassifierCandidate.getRelationList().elementAt(0)).getValues());

        System.out.println("Paso 1.2");
        for(int cas=0; cas < getData().getNumberOfCases(); cas++){
 	   conf = cases.get(cas,indexOfvars);
	   //for(i=0; i< conf.size();i++){
	   //   nState = conf.getValue(i);
           //}
        }


        System.out.println("Paso 2");
        for (Case =0; Case < NumCasesDB; Case++) {
           EvidAct=new Evidence();
  	   conf = cases.get(Case,indexOfvars);

	   for(int NumVar=0; NumVar< vars.size()-1; NumVar++){
              int val;
              val=1; // Poner el valor real, no siempre 1
	      val = conf.getValue(NumVar);
		// instanciar
              //CaseAct.Observations.putValue((FiniteStates) vars.elementAt(NumVar), val);
		EvidAct.putValue((FiniteStates) vars.elementAt(NumVar), val);
             System.out.println("Eval Bnet: Var "+i+" Valor"+val);
            }
            // Propagar, ver el resultado, comparar con la clase actual
            //hp = new HuginPropagation(b,e,args[2],sigma);

            System.out.println("Paso 3.1, para el  caso "+(Case+1));
            //sigma = new NodeList(b.getNodeList());

            System.out.println("La RB tiene "+ ClassifierCandidate.getNodeList().size()+" nodos");
//0.- VariableElimination CaseAct;
//     en lugar de HuginPropagation CaseAct;
//1.- CaseAct= new VariableElimination((Bnet)b,EvidAct);
//    en lugar de CaseAct = new HuginPropagation((Bnet)
//b,EvidAct,"trees",b.getNodeList());//sigma);
//
//2.- CaseAct.obtainInterest(); (inmediatamente después de lo anterior)
//
//3.- CaseAct.propagate();
//      en lugar de  CaseAct.propagate(CaseAct.getJoinTree().elementAt(0),"no");

//	    CaseAct = new HuginPropagation(((Bnet) getData()),EvidAct,"tables",getData().getNodeList());

	    //CaseAct= new VariableElimination((Bnet)getData(),EvidAct);
	    CaseAct= new VariableElimination(ClassifierCandidate,EvidAct);
	    CaseAct.obtainInterest();
	    CaseAct.propagate();

//new HuginPropagation((Bnet) this.getData(),EvidAct,"tables",this.getData().getNodeList());//sigma);
//new HuginPropagation(b,EvidAct);
            System.out.println("Paso 3.2, caso "+(Case+1));
//PRUE            CaseAct.propagate(CaseAct.getJoinTree().elementAt(0),"no");
            //hp = new HuginPropagation(network,observations);
            //if (EvidAct.size()>0){
            //selected_value = CaseAct.obtainEvidenceProbability("yes");
            //}
  	    //else selected_value = 1.0;
	    //System.out.println("Probabilidad de la evidencia: " + selected_value);          

            System.out.println("Paso 3.3, caso "+(Case+1));
            //CaseAct.propagate(CaseAct.getJoinTree().elementAt(0),"no");
	    //CaseAct.propagate();

//CaseAct.getPosteriorDistributions(); // falta ver cual es la clase con mayor prob
            System.out.println("Paso 4, Caso "+(Case+1));

	    //NodeList variables = getNodeList();

	    //for(i=0;i<variables.size();i++){
	    //node=(FiniteStates)variables.elementAt(i);
	    //value=conf.getValue(node);
	    //confaux.insert(node,value);
	    //}

	    //nombre de la variable = var?
	    //pos = ((Integer)positions.get(var)).intValue();
 
    pot = PotentialTable.convertToPotentialTable((Potential)(CaseAct.results.elementAt(CaseAct.results.size()-1)));
//    pot.LPNormalize();
 
    //entonces,
 
    //tabla.getValues(0) te daria el primer valor de probabilidad,
    //tabla.getValues(1) el segundo, y asi sucesivamente.

	    //Emaitzak results-en daude
            //pot=CaseAct.results.elementAt(vars.Size)
            selected=0;
            selected_value=0.0;
	    //for (i=0 ; i<results.size() ; i++) {
	    //pot = (PotentialTable) CaseAct.results.elementAt(CaseAct.results.size()-1);
	    //  pot.showResult();
	    //}
            for (variable_value=0;variable_value<pot.getSize();variable_value++) {
               System.out.println("Probabilitatea "+pot.getValue(variable_value)); // de momento
               if (pot.getValue(variable_value)>selected_value)
                    {selected=variable_value;

                     selected_value= pot.getValue(variable_value);
		    }
              }
//	    nodeX = (FiniteStates) vars.elementAt(i);
            System.out.println("Valor con mayor probabilidad "+selected);
            System.out.println("Valor real de la clase "+conf.getValue(vars.size()-1));
	    if (selected==conf.getValue(vars.size()-1))
	        Wellclassified++;
	}

        System.out.println("Va a devolver bien clasificados "+Wellclassified);
        return Wellclassified;
    }


} // EvalasClass


//pot.getVariables().elementAt(0).intValue();
                     //selected_value=((pot.getVariables()).elementAt(0)).intValue();
//pot.getVariables().elementAt(variable_value).getStates().elementAt(0).intValue();
//(((Potential)(CaseAct.results.elementAt(num_vars))).getValue(num_vars)).getValue(0);
//(float)((FiniteStates)((Potential) CaseAct.results.elementAt(num_vars))).states[0];
//.intValue()
//((FiniteStates)(((Potential) CaseAct.results.elementAt(vars.size)).getVariables().elementAt(variable_value))).getStates().elementAt(0).value;
