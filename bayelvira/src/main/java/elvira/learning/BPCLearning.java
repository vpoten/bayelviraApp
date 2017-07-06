/*
 * BPCLearning.java
 *
 * Created on 25 de noviembre de 2004, 9:59
 */

package elvira.learning;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

import elvira.*;
import elvira.potential.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * PCLearning.java
 *
 *
 * Created: Tue May 11 11:54:08 1999
 *
 * @author Proyecto Elvira
 * @version 1.0
 */

public class BPCLearning extends PCLearning  {

  public static void main(String args[]) throws ParseException, IOException { 

     BDeMetrics metric;
      Bnet net, baprend;
      FileWriter f2;
      int method;
      net = null;
      if(args.length < 6){
      System.out.println("too few arguments: Usage: file.dbc numberCases file.elv (for saving the results) method (0|1) refining (0|1) triangles (0|1)   [file.elv (true net to be compared)]");
      System.exit(0);
      }


      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      cases.setNumberOfCases(Integer.valueOf(args[1]).intValue());
      method = (Integer.valueOf(args[3]).intValue());
      
      metric = new BDeMetrics(cases);
      if(args.length > 6){ 
          FileInputStream fnet = new FileInputStream(args[6]);
          net = new Bnet(fnet); 
      } 
      PCLearning outputNet2 = new PCLearning(cases,method);
      outputNet2.setRefining( (Integer.valueOf(args[4]).intValue())); 
      outputNet2.setTriangles( (Integer.valueOf(args[5]).intValue())); 
      outputNet2.setLevelOfConfidence(0.99);
      outputNet2.learning();
      
      DELearning outputNet3 = new DELearning(cases,outputNet2.getOutput());
      outputNet3.learning(2.0);
      
     
      
      double d = cases.getDivergenceKL(outputNet3.getOutput());
      System.out.println("Divergencia de KL = "+d);
      f2 = new FileWriter(args[2]);
      baprend = (Bnet)outputNet3.getOutput();
      baprend.saveBnet(f2);
      f2.close();
      System.out.println("Medida Bayes. de la red resultado: "+metric.score(outputNet2.getOutput()));
       System.out.println("tiempo consumido"+outputNet2.delay);
      if(args.length > 6){
      FileInputStream fnet = new FileInputStream(args[6]);
      net = new Bnet(fnet);
          double d2 = cases.getDivergenceKL(net);
          System.out.println("Divergencia de la red real: "+d2);
          System.out.println("Divergencia real: "+(d2-d));
      LinkList addel[] = new LinkList[3];
      addel = outputNet2.compareOutput(net);
      System.out.print("\nNumero de arcos a�adidos: "+addel[0].size());
      System.out.print(addel[0].toString());
      System.out.print("\nNumero de arcos borrados: "+addel[1].size());
      System.out.print(addel[1].toString());
      System.out.println("\nNumero de arcos mal orientados: "+addel[2].size());
      System.out.print(addel[2].toString());
      System.out.print("\nArcos no orientados: ");
      System.out.print(outputNet2.linkUnOriented().toString());
      }  

      
   }  
   
  
public BPCLearning(DataBaseCases cases, NodeList nodes, int method) {

     super(cases,nodes,method);
     
    
}
 
  
public BPCLearning(DataBaseCases cases, int method) {

     super(cases,method);
     
    
}
  
    
    
    public void learning() {
    
 Graph dag;
 int n,m,i,j,pos;
 FiniteStates nodeX,nodeY;
 Hashtable sepSet;
 NodeList adyacenciesX,adyacenciesY,adyacenciesXY,
     adyacenciesYX,vars,subSet,minCut;
 LinkList linkList;
 Link link;
 Vector subSetsOfnElements,index;
 Enumeration en;
 boolean ok,encontrado=false,directed=false;
 Date D = new Date();
 delay = (double) D.getTime();

 //vars = getOutput().getNodeList();
 index = new Vector();
 for(i=0 ; i< getOutput().getNodeList().size() ;i++){
     sepSet = new Hashtable();
     index.addElement(sepSet);
 }
 //linkList = getOutput().getLinkList();
 dag = (Graph)getOutput();

 order0indep(index); 
 
  solvetrian(index);
 
 
 
    


 
 
 for (n=1 ; n <= dag.maxOfAdyacencies() ; n++)
     for (i=0 ; i<(dag.getNodeList()).size();i++){
             
		 nodeX=(FiniteStates)(dag.getNodeList()).elementAt(i);
		 adyacenciesX=dag.neighbours(nodeX);
                 m = adyacenciesX.size();
                 
	 for (j=i+1 ; j< (dag.getNodeList()).size() ;j++){
	       {
		nodeY=(FiniteStates)(dag.getNodeList()).elementAt(j);
		 encontrado=false;
		 pos = adyacenciesX.getId(nodeY);
                 if (!(pos==-1)){
		 System.out.println("Nodes: "+nodeX.getName()+", "
				  +nodeY.getName()+" Step: "+n);
		 link=getOutput().getLinkList().getLinks(nodeX.getName(),
							 nodeY.getName());
		 if(link==null)
		     link=getOutput().getLinkList().getLinks(nodeY.getName()
							     ,nodeX.getName());
		 
		 // they are adyacent.
                   
                  try {
	       dag.removeLink(link);
	    }
	    catch (InvalidEditException iee) {};
          
                   minCut = dag.minimunDSeparatingSet(  nodeX, nodeY);
                            ok=false;
                         //        System.out.println("Tama�o de corte " + minCut.size());
		     if(minCut.size() >= n){
			 subSetsOfnElements=minCut.subSetsOfSize(n);
			 en = subSetsOfnElements.elements();
                
			 while((!encontrado)&&(en.hasMoreElements()||n==0)){
			     if(n==0) subSet = new NodeList();
			     else subSet = (NodeList)en.nextElement();
			     ok=input.independents(nodeX,nodeY,subSet,levelOfConfidence);
			//     System.out.print("\n I( "+nodeX.getName()+" , "+nodeY.getName()+" | "+subSet.toString2()+") : "+ok+"\n");
			     if(ok){
				 
						     
				 pos = dag.getNodeList().getId(nodeX);
				 sepSet = (Hashtable)index.elementAt(pos);
				 sepSet.put(nodeY,subSet);
				 pos = dag.getNodeList().getId(nodeY);
				 sepSet = (Hashtable)index.elementAt(pos);
				 sepSet.put(nodeX,subSet);
				 encontrado = true;
			     
			     }
			     if(n==0) encontrado=true;
			 }
		     }
                   if(!ok) {
                        try {
	     dag.createLink(nodeX,nodeY,false);
	    }
	    catch (InvalidEditException iee) {};
                      }
		 
	     }
	 }
         }
     }

     System.out.println("fin primera fase ");

 
 headToHeadLink((Graph)dag,index);
 remainingLink((Graph)dag,index);
 
 
 extendOutput();
 
  System.out.println("fin segunda fase ");
 D = new Date();
 delay = (((double)D.getTime()) - delay) / 1000;
 }
    
   
}
    
    


    
