/*
 * ClusterVar.java
 *
 * Created on 3 de mayo de 2007, 13:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */


package elvira.learning.classification.supervised.discrete;

import elvira.FiniteStates;
import elvira.NodeList;
import java.util.Vector;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

import java.io.*;

/**
 *
 * @author smc
 */
public class ClusterVar implements Serializable{
  FiniteStates classvar;
  NodeList listvar;
  DataBaseCases data;
  double[][] degree;
  boolean[][] dependence;
  Vector clusterlist;
  int nvar;
  double significance;
  
  /**
   * Data member to store the output name
   */
  private String outputName;
  
  /**
   * Data member to store the index of the variable to
   * classify
   */
  private int classVarIndex;
  
  
  /** Creates a new instance of Class */
  public static void main(String args[]) throws ParseException, IOException {
    
    
    
    //If the number of arguments is incorrect
    //Arguments: file.dbc, variable a clasificar, file.elv
    
    if((args.length < 3)){
      System.out.println("too few arguments: Usage: file.dbc variable fileout ");
      System.exit(0);
    }
    
    else if((args.length > 3)){
      System.out.println("too much arguments: Usage: file.dbc variable fileout ");
      System.exit(0);
    }
    
    
    
    
    //Getting the dbc file
    FileInputStream f = new FileInputStream(args[0]);
    //Getting the databasecases from dbc file
    DataBaseCases cases = new DataBaseCases(f);
    //The final net
    
    //File where the net will be saved
    FileWriter f2;
    f2 = new FileWriter(args[2]);
    
    //Making the node list
    NodeList nodelist=new NodeList();
    // SE AGREGA LO DE DUPLICATE..........
    nodelist=cases.getNodeList().duplicate();//Getting the nodes from the dbc
    
    
    //Assigning the nodes to the net
    
    
    int nodetoclasify= Integer.parseInt(args[1]);
    
    FiniteStates nod= (FiniteStates) nodelist.elementAt(nodetoclasify);
    nodelist.removeNode(nodetoclasify);
    String name=nod.getName();
    System.out.println("");
    System.out.println("----------------------------------------------------------------------------");
    System.out.println("Wait...making Clusters  with variable "+name);
    System.out.println("----------------------------------------------------------------------------");
    System.out.println("Node list: ");
    //Making the links
    
    ClusterVar clusterc = new ClusterVar(nod,nodelist,cases);
    
    clusterc.computeDegree();
    
    clusterc.computeDep();
    
    clusterc.computeCluster();
    
    clusterc.printclusters();
    clusterc.saveclusters(f2);
    
    f2.close();
    
  }
  
  
  
  /**
   * Constructor receiving a databasecases, an integer and a name for
   * the output
   * @param database
   * @param index of var to classify
   * @param significance
   * @param output name
   */
  public ClusterVar(DataBaseCases data, int classVarIndex, double significance, String outputName){
      this.data=data;
      this.classVarIndex=classVarIndex;
      this.significance=significance;
      this.outputName=outputName;
      
      // Check if the index for the variable to classify it is a proper
      // index for a variable of the database
      listvar=data.getNodeList().duplicate();
      classvar = (FiniteStates) listvar.elementAt(classVarIndex);
      
      // If node is null, go out
      if (classvar == null){
          System.out.println("Index of var to classify is not ok: "+classVarIndex);
          System.exit(0);
      }

      // Now remove from dataBaseVars the variable to classify
      listvar.removeNode(classVarIndex);

      // Initialize the data 
      nvar=listvar.size();
      degree = new double[nvar][nvar];
      dependence = new boolean[nvar][nvar];
      clusterlist = new Vector();
  }
  
  /** Creates a new instance of ClusterVar */
  public ClusterVar() {
  }
  
  public ClusterVar(FiniteStates x, NodeList l, DataBaseCases d, double y) {
    classvar = x;
    listvar = l;
    significance = y;
    nvar = l.size();
    data = d;
    degree = new double[nvar][nvar];
    dependence = new boolean[nvar][nvar];
    clusterlist = new Vector();
    
  }
  
  public ClusterVar(FiniteStates x, NodeList l, DataBaseCases d) {
    this(x,l,d,0.01);
    
  }
  
  
  public Vector getCluster(){
      return clusterlist;
      
  }
  
  /**
   * Method making the work of this cluster method
   */
  public void run(){
    // Compute the degree
    computeDegree();

    // Compute the dependence
    computeDep();
    
    // Get the clusters
    computeCluster();
    
    // Print the clusters
    printclusters();
    
    // Save the clusters
    saveclusters();    
  }
  
  public void computeDegree(){
    int i,j;
    NodeList condition;
    FiniteStates nodei,nodej;
    
    
    condition = new NodeList();
    condition.insertNode(classvar);
    
    
    for (i=0; i<nvar; i++){
      nodei = (FiniteStates) listvar.elementAt(i);
      degree[i][i]=1.0;
      for (j=i+1;j<nvar;j++){
        nodej = (FiniteStates) listvar.elementAt(j);
        degree[i][j] =  1.0 - data.testValue(nodei,nodej,condition);
        System.out.println("Variable "+ i + " , " + j + ":" + degree[i][j]);
        degree[j][i] = degree[i][j];
      }
    }
  }
  
  
  
  public void computeDep(){
    int i,j,i2,j2;
    double nt;
    int maxrank;
    double x;
    int[][] ranking;
    double cm;
    
    
    nt = nvar*(nvar-1.0)/2.0;
    ranking = new int[nvar][nvar];
    
    for (i=0; i<nvar; i++){
      for (j=i+1;j<nvar;j++){
        ranking[i][j]=1;
        for(i2=0; i2<nvar; i2++)   {
          for (j2=i2+1; j2<nvar;j2++){
            
            if(  degree[i2][j2] < degree[i][j] ) {ranking[i][j]++;}
            if(  degree[i2][j2] < degree[i][j] ) {ranking[i][j]++;}
            
            
          }
        }
      }
    }
    
    cm = 0;
    for (i=1;i<=nt;i++){
      cm= cm+ (1.0/i);
    }
    
    maxrank = 0;
    
    
    
    
    for (i=0; i<nvar; i++){
      for (j=i+1;j<nvar;j++){
        x = (ranking[i][j]*significance)/(nt*cm);
        if (degree[i][j] < x){
          if (maxrank< ranking[i][j]) {maxrank= ranking[i][j];}
          
        }
      }
    }
    
    for (i=0; i<nvar; i++){
      for (j=i+1;j<nvar;j++){
        
        if (ranking[i][j] > maxrank){
          dependence[i][j] = false;
          dependence[j][i] = false;
        } else {
          dependence[i][j] = true;
          dependence[j][i] = true;
        }
      }
    }
  }
  
  
  public void computeCluster(){
    boolean[] active,thiscluster;
    int i,j,first;
    boolean changes,remaining;
    NodeList clus;
    
     
    active = new boolean[nvar];
    thiscluster = new boolean[nvar];
    
    for(i=0;i<nvar;i++) {
      active[i] = true;
      thiscluster[i] = false;
    }
    
    
    if (nvar>0){
      remaining = true;
      thiscluster[0] = true;
      active[0] = false;
    } 
    else {
      remaining = false;
    }
    
    while (remaining){
      changes=true;
      while(changes) {
        changes=false;
        for(i=0;i<nvar;i++){
          if (active[i]){
            for(j=0;j<nvar;j++){
              if(thiscluster[j]&& dependence[i][j]){
                thiscluster[i] = true;
                changes = true;
                active[i]=false;
                break;
              }
            }
            
          }
          
        }
        
      }

      clus = new NodeList();
      
      for(i=0;i<nvar;i++){
        if (thiscluster[i]){
          clus.insertNode(listvar.elementAt(i));
        }
      }
      
      clusterlist.addElement(clus);
      remaining = false;
      for(i=0;i<nvar;i++){
        thiscluster[i] = false;
      }
      for(i=0;i<nvar;i++){
        if (active[i] && !remaining){
          thiscluster[i] = true;
          active[i] = false;
          remaining = true;
          break;
        }
      }
    }
    
    
    
    
    //return(clusterlist);
  }
  
  public void printclusters(){
    int i,j,sizec,sizel;
    NodeList nl;
    FiniteStates node;
    
    sizel = clusterlist.size();

    for(i=0;i<sizel;i++) {
      System.out.println("==============================");
      
      System.out.println("Cluster N. " + i);
      System.out.println("------------------------------");
      nl = (NodeList) clusterlist.elementAt(i);
      sizec = nl.size();
      for (j=0;j<sizec;j++){
        node = (FiniteStates) nl.elementAt(j);
        System.out.println(node.getName());
      }
      
    }
    
    
  }
  
  
  public void saveclusters(FileWriter f2){
    int i,j,sizec,sizel;
    NodeList nl;
    FiniteStates node;
    
    PrintWriter p = new PrintWriter(f2);
    
    sizel = clusterlist.size();
    for(i=0;i<sizel;i++) {
      p.println("Cluster N. "+i);
      
      nl = (NodeList) clusterlist.elementAt(i);
      sizec = nl.size();
      for (j=0;j<sizec;j++){
        node = (FiniteStates) nl.elementAt(j);
        p.println(node.getName());
      }
      
    }
    p.close();
    
  }
  
  
    public void saveclusters(){
    FileWriter f2=null;
    int i,j,sizec,sizel;
    NodeList nl;
    FiniteStates node;
    
    try{
      f2=new FileWriter(outputName);
    }catch(IOException e){
        System.out.println("Problem making "+outputName+" file");
        System.exit(0);
    }
    
    PrintWriter p = new PrintWriter(f2);
    
    sizel = clusterlist.size();
    for(i=0;i<sizel;i++) {
      
      p.println("Cluster N. "+i);
      
      nl = (NodeList) clusterlist.elementAt(i);
      sizec = nl.size();
      for (j=0;j<sizec;j++){
        node = (FiniteStates) nl.elementAt(j);
        p.println(node.getName());
      }
      
    }
    p.close();
  }
    
    /**
     * Method for getting the number of clusters
     */
    public int getNumClusters(){
        return clusterlist.size();
    }
}



