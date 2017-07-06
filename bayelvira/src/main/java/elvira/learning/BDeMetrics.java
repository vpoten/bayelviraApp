/* BDeMetrics.java */

package elvira.learning;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Date;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.Bnet;
import elvira.Node;
import elvira.NodeList;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.ConditionalIndependence;
import elvira.potential.*;
import elvira.parser.*;

/**
 * BDeMetrics.java
 *
 *
 * Created: Mon Nov  8 11:09:40 1999
 *
 * @author P. Elvira
 * @version  1.0
 */

public class BDeMetrics extends Metrics implements ConditionalIndependence{
  
    LogFactorial f; // For storing and computing the log factorial n.
    Hashtable[] cache;
    int tme = 1;

 public static void main(String args[]) throws ParseException, IOException { 
   
     Bnet baprend;
     FileWriter f2;
     double time;
     NodeList nodesSorted;
     Metrics met;
     boolean var=false;
      if(args.length < 4){
	  System.out.println("too few arguments: Usage: file.dbc n.cases BIC,K2,BDe file.elv ");
	  System.exit(0);
      }
      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      cases.setNumberOfCases(Integer.valueOf(args[1]).intValue());
      if(args[2].equals("BIC")) met = (Metrics) new BICMetrics(cases);
      else if(args[2].equals("K2")) met = (Metrics) new K2Metrics(cases);
      else met =  met = (Metrics) new BDeMetrics(cases);
            
      FileInputStream fnet = null;
      fnet = new FileInputStream(args[3]);
      Bnet net = new Bnet(fnet);
      System.out.println("La medida es: "+met.score(net));

   }  




    
    public BDeMetrics() {
    	f = new LogFactorial();
	setData(null);
    }
    
    public BDeMetrics(DataBaseCases data){
    	f = new LogFactorial();
	setData(data);
	cache = new Hashtable[data.getNodeList().size()];
	for(int i=0 ; i< data.getNodeList().size(); i++)
	    cache[i] = new Hashtable();
    }

    public BDeMetrics(DataBaseCases data,int tme){
    	f = new LogFactorial();
	setData(data);
	cache = new Hashtable[data.getNodeList().size()];
	for(int i=0 ; i< data.getNodeList().size(); i++)
	    cache[i] = new Hashtable();
	this.tme = tme;
    }

    public NodeList getNodeList(){
        
        return(getData().getNodeList());
    }
    
    public double score (Bnet b){
	
	NodeList vars,parentsX,varsXPa;
	int i,j;
	double logSum = 0.0;
	double valscore;
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
	    logSum+=valscore;
	}
	
	return logSum;

    }

    public double score (Bnet b, Hashtable scores){

	NodeList vars,parentsX,varsXPa;
	int i,j;
	double logSum = 0.0;
	double valscore;
	FiniteStates nodeX,nodeY;
	
	vars = b.getNodeList();
	
	for(i=0; i< vars.size(); i++){
	    nodeX = (FiniteStates) vars.elementAt(i);
	    parentsX = b.parents(nodeX);
	    varsXPa = new NodeList();
	    varsXPa.insertNode(nodeX);
	    varsXPa.join(parentsX);
	    valscore = score(varsXPa);
	    scores.put(nodeX,(new Double(valscore)));
	    logSum+=valscore;
	}
	
	return logSum;
    }


    public double score (Hashtable scores){
       
       FiniteStates nodex;
       double logSum=0.0;
       Enumeration nodes = scores.keys();
       
       while(nodes.hasMoreElements()){
	   nodex = (FiniteStates)nodes.nextElement();
	   logSum+= ((Double)scores.get(nodex)).doubleValue();
       }
       return logSum;
    }
    

    public double score (NodeList vars , Hashtable scores){

	double val;
	FiniteStates node;

	val = score(vars);
	node = (FiniteStates)vars.elementAt(0);
	scores.put(node,(new Double(val)));
	return score(scores);
    }
    
    
    public double score (NodeList vars){

	Configuration conf,confPa;
	PotentialTree totalPot,parentsPot;
	double sum = 0.0;
	NodeList ParentsXi,ParentsXid,varsaux;
	FiniteStates Xi;
	int nStatesXi,k,pos,Nij,Nijk,posXi;
	double j,nconfPa,logFactXi,partialSum;
	double time,timeInitial,NPijk,NPij,a;
	Date D;
	D = new Date();
	timeInitial = (double)D.getTime();
	
	conf = confPa = null;
	Xi = (FiniteStates)vars.elementAt(0);
	ParentsXi = vars.copy();
	ParentsXi.removeNode(Xi);
	ParentsXid = vars.copy();
	ParentsXid.removeNode(Xi);
	posXi = getData().getNodeList().getId(Xi);
	ParentsXid.sort(getData().getNodeList());
	Double valor = (Double) cache[posXi].get(ParentsXid.toString2());
	
	if (valor == null){
	    totalPot = getData().getPotentialTree(vars);
	    //totalPot.print();
	    Nij=0; 
	    parentsPot = null;
	    if(vars.size()>1){
		parentsPot = (PotentialTree)totalPot.addVariable(Xi);
		//System.out.print("Tabla de los Padres :\n");
		//parentsPot.print();
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
	    NPijk = tme/(nStatesXi*nconfPa);
	    NPij = tme/nconfPa;
	    for(j=0.0 ; j< nconfPa ; j++ ){
		for(k=0 ; k< nStatesXi ; k++){
		    conf = new Configuration(vars.toVector(),confPa);
		    conf.putValue(Xi,k);
                    //System.out.print("ConfTotal: ");conf.print();
		    //pos = conf.getIndexInTable();
		    //System.out.println("Su posicion en table: "+pos);
		    Nijk =(int)totalPot.getValue(conf);
		    a = f.gammaln((((double)Nijk)+NPijk));
		    sum+=(a-f.gammaln(NPijk));
	            //try{System.in.read();}catch(IOException e){};
		}
		if(vars.size()>1){
		    //pos = confPa.getIndexInTable();
		    Nij=(int)parentsPot.getValue(confPa);
		}
		partialSum = f.gammaln(NPij) - f.gammaln((((double)Nij)+NPij));
		sum+=partialSum;
		confPa.nextConfiguration();
	    }
	    D = new Date();
            time = (double)D.getTime();
            totalTime+= (time - timeInitial)/1000;
	    timeStEval+=(time - timeInitial)/1000;
	    totalSt++;
            tStEval++;
            avStNVar+=(ParentsXi.size()+1);
	    valor = new Double(sum);
	    cache[posXi].put(ParentsXid.toString2(),valor);
	    return (valor.doubleValue());
	    
	}else{
             D = new Date();
	     time = (double)D.getTime();
             totalTime+= (time - timeInitial)/1000;
             totalSt++;
	     return (valor.doubleValue());
	}
    }
    
 
   
    
    public double score (FiniteStates X, Configuration conf){

	Configuration confPa;
	PotentialTable totalPot;
	double sum = 0.0;
	NodeList ParentsX,varsaux;
	int nStatesX,k,pos,Nij,Nijk,posX;
	double j,nconfPa,logFactXi,partialSum;
	double time,timeInitial,NPijk,NPij,a;
	Date D;
	D = new Date();
	timeInitial = (double)D.getTime();
	
	
        ParentsX = new  NodeList(conf.getVariables()); 
	
	totalPot =  getData().getPotentialTable(X,conf);
          
	
	{
	  
	    //totalPot.print();
	    Nij=0; 
	    if(ParentsX.size()>0){
		//System.out.print("Tabla de los Padres :\n");
		//parentsPot.print();
		nconfPa = FiniteStates.getSize(ParentsX);
	    }
	    else{
		nconfPa = 1.0;
	    }
	    
	    nStatesX = X.getNumStates();
	    NPijk = tme/(nStatesX*nconfPa);
	    NPij = tme/nconfPa;
	    Nij=0;
		for(k=0 ; k< nStatesX ; k++){
                    //System.out.print("ConfTotal: ");conf.print();
		    //pos = conf.getIndexInTable();
		    //System.out.println("Su posicion en table: "+pos);
		    Nijk =(int)totalPot.getValue(k);
		    a = f.gammaln((((double)Nijk)+NPijk));
		    sum+=(a-f.gammaln(NPijk));
                    Nij+= Nijk;
	            //try{System.in.read();}catch(IOException e){};
		}
		
		    //pos = confPa.getIndexInTable();
		  
		
		partialSum = f.gammaln(NPij) - f.gammaln((((double)Nij)+NPij));
		sum+=partialSum;
		
	    
	    D = new Date();
            time = (double)D.getTime();
            totalTime+= (time - timeInitial)/1000;
	    timeStEval+=(time - timeInitial)/1000;
	    totalSt++;
            tStEval++;
	    return (sum);
	    
	}
    }
 
    
    
    
    public double score (PotentialTable t, double ess){

	int i;
        double N;
        long nconf;
        double x,a,total;
	double sk;
      
          
	
	{  N=0;
	   total=0.0;
	    nconf= t.getSize();
            sk = ess/nconf;
            
            for (i=0;i<nconf;i++){
                x = t.getValue(i);
                a = f.gammaln(x+sk);
               total +=a;
               N  += x;
            }
            total -= nconf*f.gammaln(sk);
        
	   total += f.gammaln(ess) - f.gammaln(N+ess);
            
	  return(total);
	    
	}
    }
    
    
    
    
    public double scoreDep (Node x, Node y, NodeList z) {
    
    NodeList aux;
    double x1,x2;
    
    aux = new NodeList();
    aux.insertNode(x);
    aux.join(z);
    x1 = score(aux);
    aux.insertNode(y);
    x2 = score(aux);
    double n=1.0;
    n=(((FiniteStates)x).getNumStates()-1)*(((FiniteStates)y).getNumStates()-1);
    for (int i=0; i<z.size(); i++)
        n*=(((FiniteStates)z.elementAt(i)).getNumStates());
    return(x1-x2)/Math.sqrt(n);
    
    
}
   
    public double scoreDep2 (Node x, Node y, NodeList z) {
    
    NodeList aux;
    double x1,x2;
    
    aux = new NodeList();
    aux.insertNode(x);
    aux.insertNode(y);
    aux.join(z);
    x2 = score(aux);
    return(x2);
    
    
}
  
public boolean independents (Node x, Node y, NodeList z) {
    
  double aux;
  
  aux = scoreDep(x,y,z);
  if (aux>=0) {return(true);}
  else {return(false);}
    
 
  
}


   
public boolean independents (Node x, Node y, NodeList z, int degree) {
    
  return(independents(x,y,z));
    
 
  
}


   
public boolean independents (Node x, Node y, NodeList z, double degree) {
    
  return(independents(x,y,z));
    
 
  
}


} // BDeMetrics


