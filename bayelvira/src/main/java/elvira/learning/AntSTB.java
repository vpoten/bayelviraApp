package elvira.learning;

/**
 * AntSTB.java
 *
 *
 * Created: Thu Jun  1 09:11:50 2000
 *
 * @author J. M. Puerta
 * @version
 */
import java.util.*;
import elvira.*;
import elvira.database.*;
import java.io.*;

public class AntSTB implements Runnable {

    Double feromone[][];
    double feromone_0[][];
    NodeList variables;
    DataBaseCases cases;
    Metrics metric;
    double rho;
    double beta;
    double q0;
    double fitness;
    Graph dag;
    int tail,head;
    Random generator;
    


  public AntSTB() {

  }

  public AntSTB(Double mf[][],double[][] mf0,NodeList vars,DataBaseCases cases,
		  Metrics mt, double rho, double beta, double q0,Random generator){
	 feromone = mf;
	 feromone_0 = mf0;
	 variables = vars.duplicate();
	 this.cases = cases;
	 metric = mt;
	 this.rho = rho;
	 this.beta = beta;
	 this.q0 = q0;
	 this.generator = generator;
	 fitness = 0.0;
	 dag = new Graph(0);
	 for(int i=0 ; i < variables.size() ; i++){
	    try{
		      dag.addNode(variables.elementAt(i));
	       }catch(InvalidEditException iee){};
	 }
  }


  public void run(){

	  double [][] Ad = new double[variables.size()][variables.size()];
	  int i,j,a,d,k;
    int posnodeA, posnodeD;
	  Vector ances = null;
	  Vector desc = null;
	  Node nodei,nodej,nodeT,nodeH;
	  NodeList vars1,vars2,pa;

	  for(i=0; i< variables.size() ; i++)
	    for(j=0; j< variables.size(); j++){
                //try{System.in.read();}catch(IOException e){};
		      if(i!=j){
		         nodei = variables.elementAt(i);
		         nodej = variables.elementAt(j);
		         vars1 = new NodeList();
		         vars2 = new NodeList();
		         vars1.insertNode(nodei);
		         vars1.insertNode(nodej);  // coloco el arco j-->i
		         vars2.insertNode(nodei);
		         vars1=cases.getNodeList().intersectionNames(vars1).
			       sortNames(vars1);
		         vars2=cases.getNodeList().intersectionNames(vars2).
			       sortNames(vars2);
                    //System.out.println("Probando insertar "+nodej.getName()+"-->"+nodei.getName());
                    //System.out.println("Valor: "+(metric.score(vars1)-metric.score(vars2)));
		         Ad[j][i] = metric.score(vars1)-metric.score(vars2);
		      }else
		         Ad[j][i] = (-1.0/0.0);
	    }
	  do{
	     choice(Ad);  //funcion nos devolverá los índices tail,head seleccionados

	     if(Ad[tail][head] > 0.0){
		      localUpdate(tail,head);
		      nodeT = variables.elementAt(tail);
		      nodeH = variables.elementAt(head);
		      try{
                    //System.out.println("Pongo la link: "+nodeT.getName()+"-->"+nodeH.getName());
		          dag.createLink(nodeT,nodeH);
		         }catch(InvalidEditException iee){};
		      ances = dag.ancestral(nodeH).getNodeList().toVector();
		      desc  = dag.directedDescendants(nodeH);
          desc.addElement(nodeH);
		      for(a=0 ; a < ances.size(); a++)
		         for(d=0 ; d < desc.size(); d++){
			           posnodeA=variables.getId(((Node)ances.elementAt(a)).getName());
			           posnodeD=variables.getId(((Node)desc.elementAt(d)).getName());
			           Ad[posnodeD][posnodeA] = (-1.0/0.0);
		         }
		      for(k=0 ; k< variables.size() ; k++){
		          if(Ad[k][head] > (-1.0/0.0)){
			           pa = dag.parents(variables.elementAt(head));
			           vars1 = new NodeList();
			           vars2 = new NodeList();
			           vars1.insertNode(variables.elementAt(head));
			           vars1.join(pa);
			           vars2.join(vars1);
			           vars2.insertNode(variables.elementAt(k));
			           vars1=cases.getNodeList().intersectionNames(vars1).sortNames(vars1);
			           vars2=cases.getNodeList().intersectionNames(vars2).sortNames(vars2);
			           Ad[k][head] = metric.score(vars2)-metric.score(vars1);
		          }
		      }
	     }

	    }
    while (!stop(Ad));
	  fitness = score(dag);
  }

  private double score(Graph gr){

	  int i;
	  double fit=0.0;
 	  NodeList pa,vars;
    for(i=0 ; i< gr.getNodeList().size(); i++){
	      Node node = (Node) gr.getNodeList().elementAt(i);
        pa = gr.parents(node);
        vars = new NodeList();
        vars.insertNode(node);
        vars.join(pa);
        vars=cases.getNodeList().intersectionNames(vars).sortNames(vars);
        fit+=metric.score(vars);
    }
    return fit;
  }

  private boolean stop(double[][] Ad){

	  int i,j;
	  for(i=0; i<variables.size(); i++)
          for(j=0; j<variables.size() ; j++)
              if(Ad[i][j] > 0.0) return false;
    return true;
  }

  private void choice(double[][] Ad){

	  int i,j;
	  double q;
    double max;
    double val=0.0;
    tail=head=0;
    double prob[][];
    boolean encontrado = false;

	  q = generator.nextDouble();
	  if(q<=q0){
              max = (-1.0/0.0);
              for(i=0 ; i< variables.size() ; i++)
                 for(j=0 ; j< variables.size() ; j++)
                     if((i!=j)&&(Ad[i][j]>0.0)){
                        val=Math.pow(Ad[i][j],beta)*(feromone[i][j].doubleValue());
                        if(val > max){
                           max = val;
                           tail = i;
                           head = j;
                        }
                     }
      	     }
             else {
                   prob = probability(Ad);
                   q = generator.nextDouble();
                   while(q <= 0.0) q = generator.nextDouble();
                   encontrado = false;
                   for (i=0 ; (i< variables.size())&&(!encontrado); i++)
                       for(j=0 ; j< variables.size() ; j++){
                           val+=prob[i][j];
                           if(q<=val){
                              tail = i;
                              head = j;
                              encontrado = true;
                              break;
                           }
                   }
                  }

  }

  private double[][] probability(double Ad[][]){

	  double[][] prob = new double[variables.size()][variables.size()];
  	int i,j;
    double acum = 0.0;

	  for(i=0 ; i< variables.size() ; i++)
           for(j=0 ; j< variables.size(); j++){
              if((i!=j)&&(Ad[i][j] > 0.0)){
                 prob[i][j]=Math.pow(Ad[i][j],beta)*(feromone[i][j].doubleValue());
                 acum+=prob[i][j];
              } else {
                      prob[i][j] = 0.0;
                     }
           }
    for(i=0 ; i< variables.size();i++)
           for(j=0 ; j< variables.size(); j++)
                prob[i][j]/=acum;
    return prob;
  }

  private void localUpdate(int tail, int head){
	  synchronized(feromone[tail][head]){
	    feromone[tail][head] = new Double(
		    ((1-rho)*(feromone[tail][head].doubleValue()))+
		    (rho*feromone_0[tail][head]));
	  }
  }

  public Graph getDag()
  {
   return dag;
  }

  public double getFitness()
  {
   return fitness;
  }

} // Ant
