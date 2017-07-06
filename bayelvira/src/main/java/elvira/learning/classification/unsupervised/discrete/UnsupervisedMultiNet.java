/*
 * UnsupervisedMultiNet.java
 *
 * Created on 14 de noviembre de 2005, 12:38
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package elvira.learning.classification.unsupervised.discrete;

import java.io.*;
import java.util.*;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.potential.*;
import elvira.learning.preprocessing.*;
import elvira.parser.ParseException;
import elvira.learning.classification.AuxiliarPotentialTable;



/**
 *
 * @author smc
 */
public class UnsupervisedMultiNet {
  
  protected int     numberOfVariables;
  protected DataBaseCases  cases;
  protected NodeList  nodes;
  protected int numberOfCases;
  protected K2Metrics m;
  
  /**
   * value of the loglikelihood of the dataset with the learned Naive-Bayes 
   * classifier.
   */

  protected double  score;

  
  /**
 * The multinet classifier
 */
  protected   IndepProbabilityTree  classifier;
  
    
    
 public static void main(String args[]) throws ParseException, IOException {
        
        FileInputStream fileLearn;
        DataBaseCases dbLearn;
        UnsupervisedMultiNet model;
        double x;
        
        
        if((args.length != 1)){
            System.out.println("wrong number of arguments: Usage: training_file.dbc");
            System.exit(0);
        }
        
        
        // dbc file for learning
        fileLearn = new FileInputStream(args[0]);
        dbLearn = new DataBaseCases(fileLearn);
        
        model = new UnsupervisedMultiNet(dbLearn);
        
        x= model.learning();
        
        System.out.println("Clustering ends\n Score: ") ;
                
         System.out.println(x);
         
         System.out.println("Model:\n");
        
        model.getModel().print(1);
        
        
        
     
        
        //for (i=0 ; i<predictedValues.size() ; i++) {
        //    System.out.println(((Double)predictedValues.elementAt(i)).doubleValue());
        // }
        
        
    }
     
    public UnsupervisedMultiNet(DataBaseCases data) {
        Vector vector;
        Relation rel;
        
          nodes    = data.getVariables().copy();
          cases = data;
          m = new K2Metrics(data);
          classifier = new IndepProbabilityTree();
  
    this.numberOfVariables = this.nodes.size();  
        
    }
    
    
    public IndepProbabilityTree getModel(){
        return(classifier);
    }
    
    public double learning() {
        Configuration c;
        
        c = new Configuration();
        score= buildRecursive(classifier,c,nodes);  
        return(score);
    }
    
    public double buildRecursive(IndepProbabilityTree tree, Configuration c, NodeList nodes ) {
        
      double x,y,max;
      FiniteStates n1,n2;
      int i,j,k,size,best,children;
      double[] indscore;
      double[][] condscore;
      double[] finscore;
      PotentialTable pot;
      NodeList nl;
      IndepProbabilityTree trchild;
      boolean here;
      
      size = nodes.size();
      indscore = new double[size];
      finscore = new double[size];
      condscore = new double[size][size];
      x=0.0;
      
      for(i=0; i<size; i++){
          n1 = (FiniteStates) nodes.elementAt(i);
          indscore[i] = m.score(n1,c);
          finscore[i] = indscore[i];
          x+= finscore[i];
           
          for (j=0; j<size; j++){
              if(j==i){condscore[i][i] = 0.0;}
              else {
                 n2 = (FiniteStates) nodes.elementAt(j);
                 condscore[i][j]=    m.score(n2,n1,c);
                   
                 finscore[i]+=condscore[i][j];
              }
   
      }
      }
      
      best = -1;
      max = x+70.001;
      
      
         for(i=0; i<size; i++){
              
          if (finscore[i]> max){
              best = i;
              max = finscore[i];
             
              
          }
         }
      
      System.out.println("BEst: " + best);
      if (best ==-1) 
      {
          tree.setLabel(3);
            for(i=0; i<size; i++){
               n1 = (FiniteStates) nodes.elementAt(i);
               pot = cases.getPotentialTable(n1,c);
              
               tree.insertPot(pot);
            }
      }
      else 
      {   x=0.0;
          n1 = (FiniteStates) nodes.elementAt(best);
          tree.assignVar(n1);
          nl = new NodeList();
           for(j=0; j<size; j++){
                n2 = (FiniteStates) nodes.elementAt(j);
                if (j==best){here = true;}
                else
                {
                 here = true;
                 for (k=0;k<size;k++){
                     if (j != k) {
                         if (condscore[k][j] > indscore[j]){
                             here = false;
                     }
                         
                     }
                 }
                }
               if (here)
               { 
                 pot = cases.getPotentialTable(n2,c);
                
                 tree.insertPot(pot);
                 x+= indscore[j];
               }
               else {nl.insertNode(n2);
                      System.out.println("Inserto: " +j);} 
            }
          children = n1.getNumStates();
          for(j=0; j<children; j++){
             trchild = (IndepProbabilityTree) tree.getChild(j);
             c.insert(n1,j);
             c.print();
             y =  buildRecursive(trchild,c, nl);
             c.remove(n1);
             x+= y;
          }
      }
      
        
        
     return(x);   
    }
}
