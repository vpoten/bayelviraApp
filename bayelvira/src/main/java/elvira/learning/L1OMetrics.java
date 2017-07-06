/* L1OMetrics.java */

package elvira.learning;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.*;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.Bnet;
import elvira.NodeList;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.potential.*;
import elvira.parser.*;

import elvira.learning.preprocessing.*;
import elvira.tools.statistics.analysis.Stat;
/**
 * L1OMetrics.java
 *
 *
 * Created: Friday 14 Oct, 2005
 *
 * @author Serafin Moral
 * @version  1.0
 */

public class L1OMetrics extends BDeMetrics {

    static final long serialVersionUID = 838825439791343053L;    


 public static void main(String args[]) throws ParseException, IOException { 
    
     Bnet baprend;
     FileWriter f2;
     double time;
     NodeList nodesSorted;
     Metrics met;
     boolean var=false;
      if(args.length < 4){
	  System.out.println("too few arguments: Usage: file.dbc n.cases BIC,K2,L1O file.elv ");
	  System.exit(0);
      }
      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      cases.setNumberOfCases(Integer.valueOf(args[1]).intValue());
      if(args[2].equals("BIC")) met = (Metrics) new BICMetrics(cases);
      if(args[2].equals("L1O")) met = (Metrics) new L1OMetrics(cases);
    
      else met = (Metrics) new K2Metrics(cases);
            
      FileInputStream fnet = null;
      fnet = new FileInputStream(args[3]);
      Bnet net = new Bnet(fnet);
      System.out.println("La medida es: "+met.score(net));

   }  




    
    public L1OMetrics() {
	setData(null);
    }
    
    public L1OMetrics(DataBaseCases data){
	setData(data);
	cache = new Hashtable[data.getNodeList().size()];
	for(int i=0 ; i< data.getNodeList().size(); i++)
	    cache[i] = new Hashtable();
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
    

    

    public double score (NodeList vars){

	Configuration conf,confPa;
	PotentialTree totalPot,parentsPot;
	double sum = 0.0;
	NodeList ParentsXi,ParentsXid,varsaux;
	FiniteStates Xi;
	int nStatesXi,k,pos,Nij,Nijk,posXi;
	double j,nconfPa,logFactXi,partialSum;
	double time,timeInitial;
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
	    //System.out.println("Numero de estados: "+nStatesXi);
	    logFactXi = f.logFactorial(nStatesXi-1);
            //System.out.println("Su log factorial es: "+logFactXi);
	
	    for(j=0.0 ; j< nconfPa ; j++ ){
		for(k=0 ; k< nStatesXi ; k++){
		    conf = new Configuration(vars.toVector(),confPa);
		    conf.putValue(Xi,k);
                    //System.out.print("ConfTotal: ");conf.print();
		    //pos = conf.getIndexInTable();
		    //System.out.println("Su posicion en table: "+pos);
		    Nijk =(int)totalPot.getValue(conf);
		    if (Nijk>0) {sum+=Nijk*Math.log(((double)Nijk));}
	       
	            //try{System.in.read();}catch(IOException e){};
		}
		if(vars.size()>1){
		    //pos = confPa.getIndexInTable();
		    Nij=(int)parentsPot.getValue(confPa);
		}
		if(Nij>0){
                partialSum = -Nij*Math.log(((double) Nij)+nStatesXi-1);}
                else {partialSum = 0.0;}
                
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
    
 

 public double score (NodeList vars, Configuration condition){

	Configuration conf,confPa;
	PotentialTable totalPot,parentsPot;
	double sum = 0.0;
	NodeList ParentsXi,ParentsXid,varsaux;
	FiniteStates Xi;
	int nStatesXi,k,pos,Nij,Nijk,posXi;
	double j,nconfPa,logFactXi,partialSum;
	double time,timeInitial;
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
	
	{
	    totalPot = getData().getPotentialTable(vars,condition);
	    //totalPot.print();
	    Nij=0; 
	    parentsPot = null;
	    if(vars.size()>1){
		parentsPot = (PotentialTable)totalPot.addVariable(Xi);
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
	    //System.out.println("Numero de estados: "+nStatesXi);
	    logFactXi = f.logFactorial(nStatesXi-1);
            //System.out.println("Su log factorial es: "+logFactXi);
	
	    for(j=0.0 ; j< nconfPa ; j++ ){
		for(k=0 ; k< nStatesXi ; k++){
		    conf = new Configuration(vars.toVector(),confPa);
		    conf.putValue(Xi,k);
                    //System.out.print("ConfTotal: ");conf.print();
		    //pos = conf.getIndexInTable();
		    //System.out.println("Su posicion en table: "+pos);
		    Nijk =(int)totalPot.getValue(conf);
		    if (Nijk>0) {sum+=Nijk*Math.log(((double)Nijk));}
	       
	            //try{System.in.read();}catch(IOException e){};
		}
		if(vars.size()>1){
		    //pos = confPa.getIndexInTable();
		    Nij=(int)parentsPot.getValue(confPa);
		}
		if(Nij>0){
                partialSum = -Nij*Math.log(((double) Nij)+nStatesXi-1);}
                else {partialSum = 0.0;}
                
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
                if (x!=0) {a = x*Math.log(x+sk-1.0);}
                else {a=0.0;}
               total +=a;
               N  += x;
            }
            total -= nconf*f.gammaln(N+ess-1.0);
        
	
            
	  return(total);
	    
	}
    }
    
    

 
  public double score (PotentialTable t){

	int i;
        double N;
        long nconf;
        double x,a,total;
	double sk;
      
          
	
	{  N=0;
	   total=0.0;
	    nconf= t.getSize();
           
            
            for (i=0;i<nconf;i++){
                x = t.getValue(i);
                if (x!=0) {a = x*Math.log(x);}
                else {a=0.0;}
               total +=a;
               N  += x;
            }
            if (N!=0) total -= N*Math.log(N+nconf-1.0);
        
	
	  return(total);
	    
	}
    }

  
  public double scoreSimple (PotentialTable t){

	int i;
        double N;
        long nconf;
        double x,a,total;
	double sk;
      
          
	
	{  N=0;
	   total=0.0;
	    nconf= t.getSize();
           
            
            for (i=0;i<nconf;i++){
                x = t.getValue(i);
                if (x!=0) {a = x*Math.log(x);}
                else {a=0.0;}
               total +=a;
               N  += x;
            }
            if (N!=0) total -= N*Math.log(N);
        
	
            
	  return(total);
	    
	}
    }  
 
   public double scoreGroupingChiTest(int[] d1, int[] d2, int classVarNumStates){
        
        //Introducir the Yhales correction
       
        double x,a1,a2,aj,totalI, totalJ, N1, N2, NJ;
        totalI=0.0;
        totalJ=0.0;
        N1=0.0;
        N2=0.0;
        NJ=0.0;
        double mean1=0.0;
        double mean2=0.0;
        for (int i=0; i<classVarNumStates; i++){
            N1+=(d1[i]);
            N2+=(d2[i]);
            NJ+=(d1[i]+d2[i]);
        }
        for (int i=0; i<classVarNumStates; i++){
                    a1=(Math.log((d1[i]+1)/(N1+d1.length))-Math.log((d1[i]+d2[i]+1)/(NJ+d1.length)));
                    mean1+=((d1[i]+1)/(N1+d1.length))*a1;
                    
                    a2=(Math.log((d2[i]+1)/(N2+d2.length))-Math.log((d1[i]+d2[i]+1)/(NJ+d2.length)));
                    mean2+=((d2[i]+1)/(N1+d2.length))*a2;
        }

        double chivalue=2*N1*mean1+2*N2*mean2;
       
        
       if (chivalue<=0)
            return 0;
        else
            return Stat.chiSquareProb(chivalue,classVarNumStates-1);
        
  }
 
  public double scoreGroupingGTest(int[] d1, int[] d2, int classVarNumStates){
     
        double[][] ndata=new double[classVarNumStates][2];
        for (int j=0; j<classVarNumStates; j++)
            ndata[j][0]=d1[j];
        for (int j=0; j<classVarNumStates; j++)
            ndata[j][1]=d2[j];
        
        //return weka.core.ContingencyTables.chiSquared(ndata,true);
      
        
        double x,a1,a2,aj,totalI, totalJ, N1, N2, NJ;
        totalI=0.0;
        totalJ=0.0;
        N1=0.0;
        N2=0.0;
        NJ=0.0;
        double mean1=0.0;
        double mean2=0.0;
        for (int i=0; i<classVarNumStates; i++){
            N1+=(d1[i]);
            N2+=(d2[i]);
            NJ+=(d1[i]+d2[i]);
        }
        for (int i=0; i<classVarNumStates; i++){
                    a1=Math.pow(((d1[i]+1)/(N1+d1.length))-((d1[i]+d2[i]+1)/(NJ+d1.length)),2)/((d1[i]+d2[i]+1)/(NJ+d1.length));
                    mean1+=a1;
                    
                    a2=Math.pow(((d2[i]+1)/(N2+d2.length))-((d1[i]+d2[i]+1)/(NJ+d1.length)),2)/((d1[i]+d2[i]+1)/(NJ+d1.length));
                    mean2+=a2;
        }
        
        double chivalue=N1*mean1+N2*mean2;
       
        
       if (chivalue<=0)
            return 0;
        else
            return Stat.chiSquareProb(chivalue,classVarNumStates-1);
        
  }
  
public double scoreGroupingTTest(int[] d1, int[] d2, int classVarNumStates){
        
        double x,a1,a2,aj,totalI, totalJ, N1, N2, NJ;
        totalI=0.0;
        totalJ=0.0;
        N1=0.0;
        N2=0.0;
        NJ=0.0;
        double mean1=0.0;
        double mean2=0.0;
        double variance1=0.0;
        double variance2=0.0;
        for (int i=0; i<classVarNumStates; i++){
            N1+=(d1[i]);
            N2+=(d2[i]);
            NJ+=(d1[i]+d2[i]);
        }
        for (int i=0; i<classVarNumStates; i++){
                if (d1[i]!=0 && N1!=0){ 
                    a1=(Math.log((d1[i])/(N1+d1.length-1))-Math.log((d1[i]+d2[i])/(NJ+d1.length-1)));
                    mean1+=(d1[i]/(N1+d1.length-1))*a1;
                    variance1+=(d1[i]/(N1+d1.length-1))*a1*a1;
                }
                
                if (d2[i]!=0 && N2!=0){
                    a2=(Math.log(d2[i]/(N2+d2.length-1))-Math.log((d1[i]+d2[i])/(NJ+d2.length-1)));
                    mean2+=(d2[i]/(N2+d2.length-1))*a2;
                    variance2+=(d2[i]/(N2+d2.length-1))*a2*a2;
                }
        }
        
        variance1-=mean1*mean1;
        variance2-=mean2*mean2;
        
        if (N1>1) variance1*=(N1/(N1-1));
        if (N2>1) variance2*=(N2/(N2-1));

        double mean=mean1+mean2;
        double variance=variance1+variance2;
        
        double tvalue=0.0;
        if (variance!=0)
            tvalue=Math.sqrt(NJ)*mean/Math.sqrt(variance);
        else
            return 0.0;
        
        if (tvalue<=0)
           return 0.0;
        double probablity = Stat.dtudentTProb(tvalue,(int)(NJ-1));
        return probablity;
        
  }  

public double scoreJointNB ( FiniteStates X, FiniteStates Y, FiniteStates C ,Joining join){
   double val, dep,indep,Nijk,Nij,N,Nik,Njk,Nk,denom,nume,numed,denomd;
   double denomX,numeX,numedX,denomdX;
   double indepX,depX;
   int nx,ny,nc,i,j,k;
   NodeList vars;
   PotentialTable totalPot,XCPot,YCPot,CPot;
   Configuration config,conc,conxc,conyc;
   
   vars = new NodeList();
   
   vars.insertNode(X);
    vars.insertNode(Y);
    vars.insertNode(C);
   totalPot = getData().getPotentialTable(vars);
   XCPot = (PotentialTable) totalPot.addVariable(Y);
   YCPot = (PotentialTable) totalPot.addVariable(X);
    CPot = (PotentialTable) YCPot.addVariable(Y);
    
    N = CPot.totalPotential();
    
    nx = X.getNumStates(); ny = Y.getNumStates(); 
   nc = C.getNumStates();
   
   dep = 0.0;
   indep = 0.0;
   
   depX=0.0;
   indepX=0.0;
 
   double contX,contY,contXY;
   contX=contY=contXY=0;
   
   
 for (i=0; i<nx;i++) {
       config = new Configuration();
       config.putValue(X,i);
       conxc = new Configuration();
       conxc.putValue(X,i);
       for(j=0; j<ny; j++){
           config.putValue(Y, j);
           conyc = new Configuration();
           conyc.putValue(Y, j);
           denom = 0.0;denomd=0.0;
           denomX = 0.0; denomdX=0.0;
             Nij=0.0;
            for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
                conyc.putValue(C, k);
                Nijk  = totalPot.getValue(config);
                 Nij += Nijk;
               Nik = XCPot.getValue(conxc);
               Njk = YCPot.getValue(conyc);
               Nk = CPot.getValue(conc);
               
               
               nume = ((Nk)/(N+(nc)-1.0)) *  ((Nik)/(Nk+nx-1.0)) * ((Njk)/(Nk+ny-1.0));
               numed = ((Nk)/(N+(nc)-1.0)) * (Nijk/(Nk+nx*ny-1.0));
               
               denom += nume;
               denomd += numed;
               
               if (nume>0.0) { indep += Nijk*Math.log(nume);}
               if (numed>0.0) { dep += Nijk*Math.log(numed);}

               
               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);       
            }
             if (denom> 0.0) { indep -= Nij*Math.log(denom);}
             if (denomd> 0.0) { dep -= Nij*Math.log(denomd);}
       }
       
       
   }
     
   val = (dep - indep);
   
   double mean=(nx-1)*(ny-1)*(nc);
   
   return (2*(dep-indep))/Math.sqrt(2*mean);

}

public double scoreJointNBTTest ( FiniteStates X, FiniteStates Y, FiniteStates C){
   double val, dep,indep,Nijk,Nij,N,Nik,Njk,Nk,denom,nume,numed,denomd;
   double denomX,numeX,numedX,denomdX;
   double indepX,depX;
   int nx,ny,nc,i,j,k;
   NodeList vars;
   PotentialTable totalPot,XCPot,YCPot,CPot;
   Configuration config,conc,conxc,conyc;
   
   vars = new NodeList();
   
   vars.insertNode(X);
    vars.insertNode(Y);
    vars.insertNode(C);
   totalPot = getData().getPotentialTable(vars);
   XCPot = (PotentialTable) totalPot.addVariable(Y);
   YCPot = (PotentialTable) totalPot.addVariable(X);
    CPot = (PotentialTable) YCPot.addVariable(Y);
    
    N = CPot.totalPotential();
    
    nx = X.getNumStates(); ny = Y.getNumStates(); 
   nc = C.getNumStates();
   
   dep = 0.0;
   indep = 0.0;
   
   depX=0.0;
   indepX=0.0;
 
   double contX,contY,contXY;
   contX=contY=contXY=0;
   
   double[] sampleD=new double[nx*ny*nc];
   double[] sampleI=new double[nx*ny*nc];
   
   int cont1=0;
   int cont2=0;
   double mean=0;
   double variance=0;
 for (i=0; i<nx;i++) {
       config = new Configuration();
       config.putValue(X,i);
       conxc = new Configuration();
       conxc.putValue(X,i);
       for(j=0; j<ny; j++){
           config.putValue(Y, j);
           conyc = new Configuration();
           conyc.putValue(Y, j);
           denom = 0.0;denomd=0.0;
            Nij=0.0;
            for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
                conyc.putValue(C, k);
                Nijk  = totalPot.getValue(config);
                 Nij += Nijk;
               Nik = XCPot.getValue(conxc);
               Njk = YCPot.getValue(conyc);
               Nk = CPot.getValue(conc);
               
               
               nume = ((Nk)/(N+(nc)-1.0)) *  ((Nik)/(Nk+nx-1.0)) * ((Njk)/(Nk+ny-1.0));
               numed = ((Nk)/(N+(nc)-1.0)) * (Nijk/(Nk+nx*ny-1.0));
               
               denom += nume;
               denomd += numed;
               
               if (nume>0.0) { indep += Nijk*Math.log(nume);}
               if (numed>0.0) { dep += Nijk*Math.log(numed);}

               
               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);       
            }

             if (denom> 0.0) { indep -= Nij*Math.log(denom);}
             if (denomd> 0.0) { dep -= Nij*Math.log(denomd);}
             
            for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
                conyc.putValue(C, k);
                Nijk  = totalPot.getValue(config);
                 Nij += Nijk;
               Nik = XCPot.getValue(conxc);
               Njk = YCPot.getValue(conyc);
               Nk = CPot.getValue(conc);
               
               
               nume = ((Nk)/(N+(nc)-1.0)) *  ((Nik)/(Nk+nx-1.0)) * ((Njk)/(Nk+ny-1.0));
               numed = ((Nk)/(N+(nc)-1.0)) * (Nijk/(Nk+nx*ny-1.0));

               if (nume>0.0 && numed>0.0) {
                   double a=Math.log(numed/denomd) - Math.log(nume/denom);
                   mean +=(Nijk+1)*a;
                   variance+=(Nijk+1)*Math.pow(a,2);
               }
               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);       
            }
             
             
             
       }
       
       
   }
   mean/=(N+nx*ny*nc);
   variance=(N/(N-1))*(variance/(N+nx*ny*nc)-mean*mean);
   
   double tvalue=Math.sqrt(N)*mean/Math.sqrt(variance);
   if (tvalue<=0)
       return 0.0;
   double probablity = Stat.dtudentTProb(tvalue,(int)(N-1));
   return probablity;

}
public double scoreJointNBTestSimple ( FiniteStates X, FiniteStates Y, FiniteStates C){
   double val, dep,indep,Nijk,Nij,N,Nik,Njk,Nk,denom,nume,numed,denomd;
   double denomX,numeX,numedX,denomdX;
   double indepX,depX;
   int nx,ny,nc,i,j,k;
   NodeList vars;
   PotentialTable totalPot,XCPot,YCPot,CPot;
   Configuration config,conc,conxc,conyc;
   
   vars = new NodeList();
   
   vars.insertNode(X);
    vars.insertNode(Y);
    vars.insertNode(C);
   totalPot = getData().getPotentialTable(vars);
   XCPot = (PotentialTable) totalPot.addVariable(Y);
   YCPot = (PotentialTable) totalPot.addVariable(X);
    CPot = (PotentialTable) YCPot.addVariable(Y);
    
    N = CPot.totalPotential();
    
    nx = X.getNumStates(); ny = Y.getNumStates(); 
   nc = C.getNumStates();
   
   dep = 0.0;
   indep = 0.0;
   
   depX=0.0;
   indepX=0.0;
 
   double contX,contY,contXY;
   contX=contY=contXY=0;
   
   double[] sampleD=new double[nx*ny*nc];
   double[] sampleI=new double[nx*ny*nc];
   
   int cont1=0;
   int cont2=0;
   double mean=0;
   double variance=0;
 for (i=0; i<nx;i++) {
       config = new Configuration();
       config.putValue(X,i);
       conxc = new Configuration();
       conxc.putValue(X,i);
       for(j=0; j<ny; j++){
           config.putValue(Y, j);
           conyc = new Configuration();
           conyc.putValue(Y, j);
           denom = 0.0;denomd=0.0;
           denomX = 0.0; denomdX=0.0;
             Nij=0.0;
            for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
                conyc.putValue(C, k);
                Nijk  = totalPot.getValue(config);
                 Nij += Nijk;
               Nik = XCPot.getValue(conxc);
               Njk = YCPot.getValue(conyc);
               Nk = CPot.getValue(conc);
               
               
               //nume = ((Nk+1)/(N+(nc))) *  ((Nik+1)/(Nk+nx)) * ((Njk+1)/(Nk+ny));
               //numed = ((Nk+1)/(N+(nc))) * ((Nijk+1)/(Nk+nx*ny));
               
               nume = ((Nk)/(N)) *  ((Nik)/(Nk)) * ((Njk)/(Nk));
               numed = ((Nk)/(N)) * ((Nijk)/(Nk));

               denom += nume;
               denomd += numed;
               
               if (nume>0.0) { indep += Nijk*Math.log(nume);}
               if (numed>0.0) { dep += Nijk*Math.log(numed);}

               
               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);  conyc.remove(C);       
            }

             if (denom> 0.0) { indep -= Nij*Math.log(denom);}
             if (denomd> 0.0) { dep -= Nij*Math.log(denomd);}
             
            for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
                conyc.putValue(C, k);
                Nijk  = totalPot.getValue(config);
                 Nij += Nijk;
               Nik = XCPot.getValue(conxc);
               Njk = YCPot.getValue(conyc);
               Nk = CPot.getValue(conc);
               
               
               //nume = ((Nk+1)/(N+(nc))) *  ((Nik+1)/(Nk+nx)) * ((Njk+1)/(Nk+ny));
               //numed = ((Nk+1)/(N+(nc))) * ((Nijk+1)/(Nk+nx*ny));
               
               nume = ((Nk)/(N)) *  ((Nik)/(Nk)) * ((Njk)/(Nk));
               numed = ((Nk)/(N)) * ((Nijk)/(Nk));

               if (nume>0.0 && numed>0.0) {
                   double a=Math.log(numed/denomd) - Math.log(nume/denom);
                   mean +=Nijk*a;
                   variance+=Nijk*Math.pow(a,2);
               }
               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);       
            }
             
             
             
       }
       
       
   }
   mean/=N;
   variance=(N/(N-1))*(variance/N-mean*mean);
   
   double tvalue=Math.sqrt(N)*mean/Math.sqrt(variance);
   if (tvalue<=0)
       return 0.0;
   double probablity = Stat.dtudentTProb(tvalue,(int)(N-1));
   return probablity;

}

public double scoreJointNBIndepen ( FiniteStates X, FiniteStates Y, FiniteStates C             ){
   double val, dep,indep,Nijk,Nij,N,Nik,Njk,Nk,denom,nume,numed,denomd;
   double denomX;
   double indepX,depX;
   int nx,ny,nc,i,j,k;
   NodeList vars;
   PotentialTable totalPot,XCPot,YCPot,CPot;
   Configuration config,conc,conxc,conyc;
   
   vars = new NodeList();
   
   vars.insertNode(X);
    vars.insertNode(Y);
    vars.insertNode(C);
   totalPot = getData().getPotentialTable(vars);
   XCPot = (PotentialTable) totalPot.addVariable(Y);
   YCPot = (PotentialTable) totalPot.addVariable(X);
    CPot = (PotentialTable) YCPot.addVariable(Y);
    
    N = CPot.totalPotential();
    
    nx = X.getNumStates(); ny = Y.getNumStates(); 
   nc = C.getNumStates();
   
   dep = 0.0;
   indep = 0.0;
   
   depX=0.0;
   indepX=0.0;
 
   double contX,contY,contXY;
   contX=contY=contXY=0;
   
 
 for (i=0; i<nx;i++) {
       config = new Configuration();
       config.putValue(X,i);
       conxc = new Configuration();
       conxc.putValue(X,i);
       for(j=0; j<ny; j++){
           config.putValue(Y, j);
           conyc = new Configuration();
           conyc.putValue(Y, j);
           denom = 0.0;denomd=0.0;
           denomX = 0.0;
             Nij=0.0;
            for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
                conyc.putValue(C, k);
                Nijk  = totalPot.getValue(config);
                 Nij += Nijk;
               Nik = XCPot.getValue(conxc);
               Njk = YCPot.getValue(conyc);
               Nk = CPot.getValue(conc);
               
               
               nume = ((Nk)/(N+(nc)-1.0)) *  ((Nik)/(Nk+nx-1.0)) * ((Njk)/(Nk+ny-1.0));

               denom += nume;
               denomX+=(Nik*Njk)/Nk;
               //if (nume>0.0) { indep += ((Nik*Njk)/Nk)*Math.log(nume);}
               if (nume>0.0) { indep += Nijk*Math.log(nume);}
               
               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);       
            }
             //if (denom> 0.0) { indep -= denomX*Math.log(denom);}
             if (denom> 0.0) { indep -= Nij*Math.log(denom);}
       }
       
       
   }
   return indep;
}

  public double scoreJointNBSimple ( FiniteStates X, FiniteStates C             ){
   double val, dep,indep,N,Nik,Ni,Nk,denom,nume,numed,denomd;
   double denomX,numeX,numedX,denomdX;
   double indepX,depX;
   int nx,ny,nc,i,j,k;
   NodeList vars;
   PotentialTable totalPot,XCPot,YCPot,CPot;
   Configuration config,conc,conxc,conyc;
   
   vars = new NodeList();
   
   vars.insertNode(X);
    vars.insertNode(C);
   totalPot = getData().getPotentialTable(vars);
   XCPot = (PotentialTable) totalPot;
   CPot = (PotentialTable) XCPot.addVariable(X);
    
    N = CPot.totalPotential();
    
    nx = X.getNumStates();
   nc = C.getNumStates();
   
   dep = 0.0;
   indep = 0.0;
   
   depX=0.0;
   indepX=0.0;
 
   
 
 for (i=0; i<nx;i++) {
       config = new Configuration();
       config.putValue(X,i);
       conxc = new Configuration();
       conxc.putValue(X,i);
           denom = 0.0;denomd=0.0;
           denomX = 0.0; denomdX=0.0;
           Ni=0; 
           for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
               Nik = XCPot.getValue(conxc);
               Nk = CPot.getValue(conc);
               Ni+=Nik;
               
               numed = ((Nk)/(N+(nc)-1.0))*((Nik)/(Nk+nx-1.0));
               denomd += numed;
               if (numed>0.0) { dep += Nik*Math.log(numed);}

               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);       
            }
            if (denomd> 0.0) { dep -= Ni*Math.log(denomd);}

   }
  
   double mean=(nx-1)*(nc);
   return 2*dep/Math.sqrt(2*mean);
   
  }
 
   public double scoreJointNBLL ( FiniteStates X, FiniteStates C             ){
   double val, dep,indep,N,Nik,Ni,Nk,denom,nume,numed,denomd;
   double denomX,numeX,numedX,denomdX;
   double indepX,depX;
   int nx,ny,nc,i,j,k;
   NodeList vars;
   PotentialTable totalPot,XCPot,YCPot,CPot;
   Configuration config,conc,conxc,conyc;
   
   vars = new NodeList();
   
   vars.insertNode(X);
    vars.insertNode(C);
   totalPot = getData().getPotentialTable(vars);
   XCPot = (PotentialTable) totalPot;
   CPot = (PotentialTable) XCPot.addVariable(X);
    
    N = CPot.totalPotential();
    
    nx = X.getNumStates();
   nc = C.getNumStates();
   
   dep = 0.0;
   indep = 0.0;
   
   depX=0.0;
   indepX=0.0;
 
   
 
 for (i=0; i<nx;i++) {
       config = new Configuration();
       config.putValue(X,i);
       conxc = new Configuration();
       conxc.putValue(X,i);
           denom = 0.0;denomd=0.0;
           denomX = 0.0; denomdX=0.0;
           Ni=0; 
           for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
               Nik = XCPot.getValue(conxc);
               Nk = CPot.getValue(conc);
               Ni+=Nik;
               
               numed = ((Nk)/(N+(nc)-1.0))*((Nik)/(Nk+nx-1.0));
               denomd += numed;
               if (numed>0.0) { dep += Nik*Math.log(numed);}

               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);       
            }
            if (denomd> 0.0) { dep -= Ni*Math.log(denomd);}

   }

   return dep/N;
    }
 
 public double scoreJointNBSimpleCond ( FiniteStates X, FiniteStates C             ){
   double val, dep,indep,N,Nik,Ni,Nk,denom,nume,numed,denomd;
   double denomX,numeX,numedX,denomdX;
   double indepX,depX;
   int nx,ny,nc,i,j,k;
   NodeList vars;
   PotentialTable totalPot,XCPot,YCPot,CPot;
   Configuration config,conc,conxc,conyc;
   
   vars = new NodeList();
   
   vars.insertNode(X);
    vars.insertNode(C);
   totalPot = getData().getPotentialTable(vars);
   XCPot = (PotentialTable) totalPot;
   CPot = (PotentialTable) XCPot.addVariable(X);
    
    N = CPot.totalPotential();
    
    nx = X.getNumStates();
   nc = C.getNumStates();
   
   dep = 0.0;
   indep = 0.0;
   

 
 for (i=0; i<nx;i++) {
       config = new Configuration();
       config.putValue(X,i);
       conxc = new Configuration();
       conxc.putValue(X,i);
           denom = 0.0;denomd=0.0;
           denomX = 0.0; denomdX=0.0;
           Ni=0; 
           for(k=0; k<nc; k++){
               conc = new Configuration();
               conc.putValue(C, k);
                 config.putValue(C, k);
                conxc.putValue(C, k);
               Nik = XCPot.getValue(conxc);
               Nk = CPot.getValue(conc);
               Ni+=Nik;
               
               numed = Nik/Nk;//((Nk)/(N+(nc)-1.0))*((Nik)/(Nk+nx-1.0));
               if (numed>0.0) { dep += Nik*Math.log(numed);}

               
               config.remove(C);   
               conc.remove(C);   
                conxc.remove(C);       
            }
   }

   return dep/N;
    }
 
  public double score (FiniteStates X, Configuration conf){


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
	    
	    
	    nStatesX = X.getNumStates();
	    NPijk = 1;
	    NPij = nStatesX;
	    Nij=0;
		for(k=0 ; k< nStatesX ; k++){
                    //System.out.print("ConfTotal: ");conf.print();
		    //pos = conf.getIndexInTable();
		    //System.out.println("Su posicion en table: "+pos);
		    Nijk =(int)totalPot.getValue(k);
		  if(Nijk>0) { sum+=Nijk*Math.log(Nijk);}
                    Nij+= Nijk;
	            //try{System.in.read();}catch(IOException e){};
		}
		
		    //pos = confPa.getIndexInTable();
		  
		
		if (Nij>0) {partialSum = -Nij*Math.log((double)Nij+nStatesX-1);}
                else {partialSum=0.0;}
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
    
    
    } // L1OMetrics
