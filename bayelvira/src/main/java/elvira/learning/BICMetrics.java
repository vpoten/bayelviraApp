package elvira.learning;


/**
 * BICMetrics.java
 *
 *
 * Created: Mon Nov  8 11:09:40 1999
 *
 * @author P. Elvira
 * @version  1.0
 */
import java.util.Hashtable;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.learning.*;
import elvira.database.*;
import elvira.potential.*;

public class BICMetrics extends Metrics {
  
  Hashtable[] cache;  
  public double efectividad;
  //double tStEval = 0.0;    
  //double totalSt = 0.0;    
  //double totalTime = 0.0;
  //double timeStEval = 0.0;

   public BICMetrics(){
	setData(null);
    }
    
    public BICMetrics(DataBaseCases data){
	setData(data);
	efectividad = 0.0;
	cache = new Hashtable[data.getNodeList().size()];
	for(int i=0 ; i< data.getNodeList().size(); i++)
	    cache[i] = new Hashtable();
    }


    public double score (Bnet b){
	
	NodeList vars,parentsX,varsXPa;
	int i,j;
	double sum = 0.0;
	double valscore,penal;
	FiniteStates nodeX,nodeY;

	vars = b.getNodeList();
	for(i=0; i< vars.size(); i++){
	    nodeX = (FiniteStates) vars.elementAt(i);
	    parentsX = b.parents(nodeX);
	    varsXPa = new NodeList();
	    varsXPa.insertNode(nodeX);
	    varsXPa.join(parentsX);
	    varsXPa = getData().getNodeList().
		      intersectionNames(varsXPa).sortNames(varsXPa);
	    valscore = score(varsXPa);
	    sum+=valscore;
	}
	
	return sum;

    }

    public double score (NodeList vars){

	Configuration conf,confPa;
	PotentialTable totalPot,parentsPot;
	double sum = 0.0;
	NodeList ParentsXid,ParentsXi,varsaux;
	FiniteStates Xi;
	int nStatesXi,k,pos,Nij,Nijk,posXi;
	double j,nconfPa,partialSum;
	double time,timeInitial;
        Date D;
        D = new Date();
        timeInitial = (double)D.getTime();
       
	Xi = (FiniteStates) vars.elementAt(0);
	//System.out.println("Vars: "+vars.toString2());
	ParentsXi = vars.copy();
	ParentsXi.removeNode(Xi);
	ParentsXid = vars.copy();
	ParentsXid.removeNode(Xi);
	//System.out.println("Xi: "+Xi.getName()+" Parents: "+ParentsXid.toString2());
	posXi = getData().getNodeList().getId(Xi);
	//System.out.println("Posicion: "+posXi);
	ParentsXid.sort(getData().getNodeList());
	//System.out.println("Parents Sorted: "+ParentsXid.toString2());
	//System.out.println("Clave hash: "+ParentsXid.hashCode());
	Double valor = (Double) cache[posXi].get(ParentsXid.toString2());
	//System.out.println(cache[posXi].toString());
	if(valor == null){
	    totalPot = getData().getPotentialTable(vars);
	    Nij=0;
	    parentsPot = null;
	    if(vars.size()>1){
		parentsPot = (PotentialTable)totalPot.addVariable(Xi);
		confPa = new Configuration(ParentsXi);
		nconfPa = FiniteStates.getSize(ParentsXi);
	    }
	    else{
		nconfPa = 1.0;
		varsaux = new NodeList();
		varsaux.insertNode(Xi);
		confPa = new Configuration(varsaux);
		Nij = (int)totalPot.totalPotential();
	    }
	    
	    
	    nStatesXi = Xi.getNumStates();	
	    
	    for(j=0.0 ; j< nconfPa ; j++ ){
		if(vars.size()>1){
		    pos = confPa.getIndexInTable();
		    Nij=(int)parentsPot.getValue(pos);
		}
		for(k=0 ; k< nStatesXi ; k++){
		    conf = new Configuration(vars.toVector(),confPa);
		    conf.putValue(Xi,k);
		    pos = conf.getIndexInTable();
		    Nijk =(int)totalPot.getValue(pos);
		    if((Nij>0) && (Nijk>0)){
			partialSum = Math.log((double)Nijk);
			partialSum -= Math.log((double)Nij);
			sum+=(partialSum*(double)Nijk);
		    }
		}
		confPa.nextConfiguration();
	    }
            D = new Date();
            time = (double)D.getTime();
    	    totalTime+= (time - timeInitial)/1000;
            timeStEval+=(time - timeInitial)/1000;
            totalSt++;
            tStEval++;
            avStNVar+=(ParentsXi.size()+1);
	    valor = new Double(sum-penalty(vars));
	    //System.out.println("Valor a poner: "+valor.toString());
	    cache[posXi].put(ParentsXid.toString2(),valor);
	    //try{System.in.read();}catch(IOException e){};
	    return (valor.doubleValue());
	}
	else{
	    //System.out.println("Valor que he recuperado: "+valor.toString());
	    efectividad = efectividad + 1.0;
            D = new Date();
            time = (double)D.getTime();
            totalTime+= (time - timeInitial)/1000;
            totalSt++;
	    // try{System.in.read();}catch(IOException e){};
	    return (valor.doubleValue());
	}
	
    }
    
  
  public double penalty(NodeList vars){
    int j;
    FiniteStates nodeXi,pj;
    NodeList paXi;
    double nvXi,nvPj;
    double penal = 1.0;
    
    
    nodeXi = (FiniteStates)vars.elementAt(0);
    nvXi = (double)nodeXi.getNumStates();
    paXi = vars.copy();
    paXi.removeNode(nodeXi);
    for(j=0;j<paXi.size();j++){
    	pj = (FiniteStates)paXi.elementAt(j);
    	nvPj = (double)pj.getNumStates();
    	penal*=nvPj;
    }
    if(paXi.size() == 0) penal=(nvXi-1.0);
    else penal = penal*(nvXi-1.0);
    return ((Math.log(getData().getNumberOfCases())*0.5)*penal);
  }



} // BICMetrics

