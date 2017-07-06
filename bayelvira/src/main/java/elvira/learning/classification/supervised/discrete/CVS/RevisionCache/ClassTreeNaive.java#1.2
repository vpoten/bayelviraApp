/*
 * ClassTreeNaive.java
 *
 * Created on 24 de mayo de 2007, 10:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elvira.learning.classification.supervised.discrete;

/**
 *
 * @author smc
 */

import java.util.Vector;
import elvira.database.DataBaseCases;
import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.learning.classification.ConfusionMatrix;
import elvira.potential.*;
import java.io.*;

public class ClassTreeNaive extends DiscreteClassifier {
    
    ClusterVar cluster;
    Vector listclus;
    Vector listtrees;
    NodeList attributes;
    PotentialTable cprior;
    double significance;
    double treesign;
    double prior;
    
    
    /** Creates a new instance of ClassTreeNaive */
    public ClassTreeNaive() {
            super();
    }
    
   public ClassTreeNaive(DataBaseCases data) {
          this.cases         = data;
  	this.nVariables  = this.cases.getVariables().size();
   	this.nCases      = this.cases.getNumberOfCases();
    this.laplace       = true;
    this.evaluations   = 0;
    this.logLikelihood = 0;
    this.significance = 0.05;
      this.treesign = 0.05;
      this.prior = 3.0;
      this.attributes = new NodeList();
    
   NodeList    nodelist    = this.cases.getVariables();
    Node        node;
   	Vector      vector      = this.cases.getRelationList();
   	Relation    relation    = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem = (CaseListMem)relation.getValues();
        attributes = new NodeList();
 
      for (int j=0; j< nodelist.size(); j++) {
        node = (Node)(caselistmem.getVariables()).elementAt(j);
        if (node.getTypeOfVariable() == Node.CONTINUOUS) {
          System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
          System.exit(0);
        }
      if (j != (nodelist.size()-1)){
        attributes.insertNode(node);     
      }
      
      }
      

    classVar = (FiniteStates)this.cases.getNodeList().lastElement();
    this.classNumber         = classVar.getNumStates();
    this.confusionMatrix     = new ConfusionMatrix(this.classNumber);   
     this.listtrees = new Vector();
       
    }  
     public ClassTreeNaive(DataBaseCases data, int classn) {
             this.significance = 0.01;
          this.cases         = data;
  	this.nVariables  = this.cases.getVariables().size();
   	this.nCases      = this.cases.getNumberOfCases();
    this.laplace       = true;
    this.evaluations   = 0;
    this.logLikelihood = 0;
     this.significance = 0.01;
      this.treesign = 0.01;
     this.prior = 2.0;
       this.attributes = new NodeList();
       
       
   NodeList    nodelist    = this.cases.getVariables();

    Node        node;
   	Vector      vector      = this.cases.getRelationList();
       
   	Relation    relation    = (Relation)vector.elementAt(0);
        System.out.println(nCases);
   	CaseListMem caselistmem = (CaseListMem)relation.getValues();

 
      for (int j=0; j< nodelist.size(); j++) {
        node = (Node)(caselistmem.getVariables()).elementAt(j);
        if (node.getTypeOfVariable() == Node.CONTINUOUS) {
          System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
          System.exit(0);
        }
          if (j != classn) {
        attributes.insertNode(node);     
      }
      
      }
      

    classVar = (FiniteStates)this.cases.getNodeList().elementAt(classn);
    this.classNumber         = classVar.getNumStates();
    this.confusionMatrix     = new ConfusionMatrix(this.classNumber);   
    this.listtrees = new Vector();
       
    }  
    /**
   * This method learns the structure and parameters 
   */
  public void structuralLearning() throws elvira.InvalidEditException{
      int sizel,i;
      NodeList localnodes;
      ProbabilityTree localtree,tree2;
      PotentialTable freque;
      double nleaves;
      
      NodeList aux;
      aux = new NodeList();
      aux.insertNode(classVar);
      
     
     cprior = cases.getPotentialTable(aux);
      
      cprior.sum(prior);
      
      cluster = new ClusterVar(classVar,attributes,cases,0.01);
      cluster.computeDegree();
      cluster.computeDep();
      cluster.computeCluster();
      cluster.printclusters();
      listclus = cluster.getCluster();
      
      sizel = listclus.size();
      
      for(i=0; i<sizel; i++){
          localnodes = (NodeList) listclus.elementAt(i);
          localtree = learn(localnodes);
        
          localtree.updateSize();
          
       
          nleaves = localtree.getSize()/((double) classVar.getNumStates());
        
          localtree.sum(prior/nleaves); 
          localtree.divide(classVar,cprior); 
   
          listtrees.add(localtree);
          
          
          
      }
      
     cprior.normalize(); 
    
      
  }  
  
  public ProbabilityTree learn(NodeList lnodes) {
      ProbabilityTree tree;
      Configuration c;
      
   
       c = new Configuration();
      
      tree = learnr(lnodes,c);
      
      
      
      return(tree);
      
  }
  
  
  private ProbabilityTree learnr(NodeList lnodes, Configuration c) {
  
   ProbabilityTree tree;
   tree = new ProbabilityTree();
   int i,nvar,max,nc;
   double depdegree,x;
   PotentialTable f2;
   FiniteStates maxVar;
   
   nvar = lnodes.size();
  
   max = -1;
   depdegree = 0.0;
   
   for(i=0; i<nvar;i++){
       
      x =  cases.testValue(classVar,(FiniteStates) lnodes.elementAt(i),c) - 1.0 + treesign;
      if (x>depdegree) 
      {
          max = i;
          depdegree = x;
      }
       
   }
      nc = classVar.getNumStates();
   if (max==-1){
     f2   = cases.getPotentialTable(classVar,c);
     tree.assignVar(classVar);
  
     for (i=0; i<nc;i++){
        tree.getChild(i).assignProb(f2.getValue(i));     
     }
   }
     else {
       maxVar = (FiniteStates) lnodes.elementAt(max);
       nc = maxVar.getNumStates();
       tree.assignVar(maxVar);
         lnodes.removeNode(max);
          for (i=0; i<nc;i++){
          c.insert(maxVar,i);   
         tree.replaceChild(learnr(lnodes,c),i);
         c.remove(maxVar);
          }
         lnodes.insertNode(maxVar);
     }
     
   
   
   
      
   return(tree);
      
  }
   
    
     public double[] computePosterior(Configuration instance) {
         ProbabilityTree t1,t2;
         
         int i,nc,nv,j,k,l;
         double[] result;
         NodeList nl;
         FiniteStates nob;
         double sum;
         
         nv = classVar.getNumStates();
         result = new double[nv];
         
         for (i=0; i<nv; i++) {result[i] = cprior.getValue(i);}
         
         
         
         nc = listtrees.size();
         
         for(i=0; i<nc; i++) {
           t1 = (ProbabilityTree) listtrees.elementAt(i);  
           t2 = t1.restrict(instance);
           nl = t2.getVarList();
           k = nl.size();
          
           for(j=0; j<k; j++){
               nob = (FiniteStates) nl.elementAt(j);
               
               if (!(nob == classVar) ){
                  t2 = t2.addVariable(nob);  
               }    
           }
           for(l=0; l<nv; l++){
               result[l] *= t2.getChild(l).getProb();
              
           } 
           
             
         }
         
         
       sum=0.0;
       
           for(l=0; l<nv; l++){
               sum += result[l];
           } 
         
           for(l=0; l<nv; l++){
               result[l] /= sum;
             
           } 
         
         
         return result;
    }  

     
     public int assignClass(Configuration conf) {
         double[] result;
         int i,max,nv;
         double x;
         
         result = computePosterior(conf);
         
         max=-1;
         x = -1.0;
          nv = classVar.getNumStates();
         for (i=0;i<nv;i++){
              if (result[i] > x) {
                  x = result[i];
                  max = i;
              }
         }
        return max; 
         
         
     } 
  
   /**
   * This method tests the learned classifier given a DataBaseCases.
   * It returns the accuracy of the classifier.
   * It requires the the class variable with assigned values in the test database.
   * @param. DataBaseCases test. The test database of the classifier.
   * @returns double. The accuracy of the classifier on the <code> test <code\> dataset
   */
  public double test(DataBaseCases test) {
    DataBaseCases newtest = test;
    Configuration c;
    int trueclass;
/*    if(!test.getVariables().equals(this.cases.getVariables()))
      newtest = this.projection(this.cases, test.getVariables());
*/

   
;

    //Check: the variables must have the same number of states
  	NodeList    nodelistTrain    = this.cases.getVariables();
   	Vector      vectorTrain      = this.cases.getRelationList();
   	Relation    relationTrain    = (Relation)vectorTrain.elementAt(0);
   	CaseListMem caselistmemTrain = (CaseListMem)relationTrain.getValues();

   	NodeList    nodelistTest    = newtest.getVariables();
   	Vector      vectorTest      = newtest.getRelationList();
   	Relation    relationTest    = (Relation)vectorTest.elementAt(0);
   	CaseListMem caselistmemTest = (CaseListMem)relationTest.getValues();

    int nStatesTrain, nStatesTest;
    FiniteStates varStatesTrain = new FiniteStates();
    FiniteStates varStatesTest = new FiniteStates();
   

    int nTest = newtest.getNumberOfCases();

    Vector      vector      = newtest.getRelationList();
  	Relation    relation    = (Relation)vector.elementAt(0);
    CaseListMem caselistmem = (CaseListMem)relation.getValues();
    caselistmem.setVariables (nodelistTrain.toVector());

    double accuracy = (double)0;
caselistmem.initializeIterator();
    while(caselistmem.hasNext()) {
      
    		c = caselistmem.getNext();
//                c.print();
               trueclass = c.getValue(classVar);
//                System.out.println("true Class " + trueclass );
                c.remove(classVar);
      int assignedClass = this.assignClass(c);
      if (assignedClass == trueclass)
    		accuracy = accuracy + 1;
      this.confusionMatrix.actualize(trueclass, assignedClass);
    }
    accuracy = (accuracy / (double) nTest) * (double) 100;

  	return(accuracy);
  }
     
     
     
 /**
   * Main to use the class from the command line
   */
  public static void main(String[] args) throws FileNotFoundException, IOException, elvira.InvalidEditException, elvira.parser.ParseException, Exception{
    int nvar;
      
    if(args.length != 4) {
      System.out.println("Usage: file-train.dbc classnumber file-test.dbc file-out.elv");
      System.exit(0);
    }
  nvar =(Integer.valueOf(args[1]).intValue());
    FileInputStream fi = new FileInputStream(args[0]);
    DataBaseCases   db = new DataBaseCases(fi);
    fi.close();

    ClassTreeNaive clasificador = new ClassTreeNaive(db, nvar);
    clasificador.structuralLearning();

    System.out.println("Classifier learned");

    FileInputStream ft = new FileInputStream(args[2]);
    DataBaseCases   dt = new DataBaseCases(ft);
    ft.close();

    double accuracy = clasificador.test(dt);

    System.out.println("Classifier tested. Accuracy: " + accuracy);

    clasificador.getConfusionMatrix().print();

  }     
     
}
