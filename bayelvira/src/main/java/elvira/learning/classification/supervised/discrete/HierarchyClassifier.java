/*
 * HierarchyClassifier.java
 *
 * Created on 13 de diciembre de 2006, 16:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package elvira.learning.classification.supervised.discrete;

/**
 *
 * @author smc
 */



import elvira.Configuration;
import elvira.database.DataBaseCases;
import java.util.Vector;
import elvira.HierarchyBnet;
import elvira.Hierarchy;
import elvira.Evidence;
import elvira.Bnet;
import elvira.potential.PotentialTable;
import elvira.FiniteStates;
import elvira.inference.elimination.VariableElimination;

public class HierarchyClassifier extends DiscreteClassifier{
    
public static final int NAIVE = 1;
public static final int SELECTIVENAIVE = 2;
    
    
protected HierarchyBnet tree;    
protected DiscreteClassifier localcla;
protected int method;
    
    
    
    /** Creates a new instance of HierarchyClassifier */
    public HierarchyClassifier(DataBaseCases data, Hierarchy h, int m) throws elvira.InvalidEditException{
        super(data,true);
        method = m;
        tree = new HierarchyBnet(h);
     
    
}
    public HierarchyClassifier(DataBaseCases data, HierarchyBnet b, int m) throws elvira.InvalidEditException{
        super(data,true);
        method = m;
        tree = b;
     
    }
    
    
    
 
 
    /** 
   * Method of Classifier interface. This method is used to classify a instance,
	 * @param instance case to classify
	 * @return a double arrray with a probability associated to each class value
   */
  public double[] classify (Configuration instance) {
    double valprob[];
    int i,n;
    
    n = classVar.getNumStates();
    valprob = new double[n];
    
    for(i=0;i<n;i++ ) {valprob[i] = 1.0;}
    
    classify (instance, 1.0, valprob);
    return(valprob);
    
  } 
    
  
 
    /** 
   * Recursive auxiliar evaluation procedure. This method is used to classify a instance,
	 * @param instance case to classify
	 * @return a double arrray with a probability associated to each class value
   */
  public void  classify (Configuration instance, double x, double[] valprob) {
  
    int i,n,current,nc;
    Evidence e;
    Bnet localdec;
    VariableElimination prog;
    FiniteStates nodevar; 
    PotentialTable result;
    
    e = new Evidence(instance);
  
    localdec = this.tree.net;
    nodevar = tree.getAuxVar();
    n = classVar.getNumStates();
   
    prog = new VariableElimination(localdec,e);
    
    prog.getPosteriorDistributionOf(nodevar);
    
    result = (PotentialTable) prog.getResults().elementAt(0);
    
    if (this.tree.isLeaf()) {
        current=0;
        for(i=0; i<n; i++) {
            if (this.tree.getMembers()[i]) {
                valprob[i] = x*result.getValue(current);
                current++;
            }
        }
        
    }
    else { 
        nc= tree.getNumChildren() ;
                for (i=0;i<nc;i++){
        classify (instance,x*result.getValue(i),valprob);
    }
    }
    
    
  }   
  
     public int assignClass(Configuration c){
         int i,n,index;
         double maxp;
         double[] result;
         
         
         maxp = 0.0;
         index=0;
         n = classVar.getNumStates();
         result = classify(c);
         for (i=1; i<n;i++){
             if(result[i]>maxp){
                 index= i;
                 maxp = result[i];
             }
             
         }
         
         
         return(index);
     }
  
  
    
  /**
   * This method learns the classifier structure and parameters. It is better to do both things at the same time
   * by efficiency reasons.
   */
    
    public  void structuralLearning() throws elvira.InvalidEditException, Exception
  {  DataBaseCases localcases;
     HierarchyClassifier childClass;
     int i,nc;
     
    
          localcases = cases.transform(classVar, tree); 
         
        
         
            switch (method){
        case NAIVE: {localcla =  new Naive_Bayes(localcases, true);
                    break;
        }
        
        case SELECTIVENAIVE: {localcla =  new WrapperSelectiveNaiveBayes(localcases, true);
                    break;
        }
    }
            
            localcla.setClassVar((FiniteStates) tree.auxvar);
            localcla.structuralLearning();
            localcla.parametricLearning();
            tree.net = localcla.getClassifier();
            
          if (!tree.isLeaf())  {
                nc= tree.getNumChildren() ;
                for (i=0;i<nc;i++){
                    childClass = new HierarchyClassifier (cases ,tree.getChildat(i), method);
                    childClass.structuralLearning();                 
                    
                }
                
          }
    
  }
}
