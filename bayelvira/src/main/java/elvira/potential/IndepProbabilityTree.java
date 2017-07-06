/*
 * IndepProbabilityTree.java
 *
 * Created on 22 de noviembre de 2005, 16:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package elvira.potential;

import java.util.Vector;
import java.util.Hashtable;
import java.lang.Math;
import elvira.*;

/**
 *
 * @author smc
 */
public class IndepProbabilityTree extends ProbabilityTree {
 
ListPotential IndepPot;    

static final int FINAL_NODE=3;
 
    /**
     * Constructor. Creates an empty tree node.
     */
    
    public IndepProbabilityTree() {
        
        label = FINAL_NODE;
        value = 0.0;
        leaves = 0;
        child = new Vector();
        IndepPot = new ListPotential();
    }
    
    
    /**
     * Creates a tree with the argument as root node.
     * @param variable a <code>FiniteStates</code> variable.
     */
    
    public IndepProbabilityTree(FiniteStates variable) {
        
        int i, j;
        ProbabilityTree tree;
        
        label = FULL_NODE;
        value = 0.0;
        leaves = 0;
        var = variable;
        child = new Vector();
        IndepPot = new ListPotential();
        
        j = variable.getNumStates();
        for (i=0 ; i<j ; i++) {
            tree = new IndepProbabilityTree();
           
            child.addElement(tree);
        }
    }
    
   
    /**
     * Gets one of the children of this node.
     * @param i an <code>int</code> value: number of child to be returned
     * (first child is i=0).
     * @return the i-th child of the tree.
     */
    
    public IndepProbabilityTree getChild(int i) {
        
        return ((IndepProbabilityTree)(child.elementAt(i)));
    }
    
    
      /**
     * Insert a potential to a tree.
     * @param p a <code>Potential</code> that will be added to
     * the list of potentials of this node of the tree.
     */
    
    public void insertPot(Potential p) {
        IndepPot.insertPotential(p);
      
    }
    
          /**
     * Set the list of potentials of a tree.
     * @param l the <code>ListPotential</code> that will be set
     * as the list of potentials of the tree
     * 
     */
    
    public void setPot(ListPotential l) {
        IndepPot = l;
    }
         /**
     * Get the list of potentials associated to a tree
     * @return the list of potentials in this node of the tree
     */
    
    public ListPotential getPot() {
        return(IndepPot);
      
    }
    
  
         /**
     * Get a potential containing a variable
     * @param x the <code>FiniteStates</code> variable for which we are looking for the potential
     * @return the first potential containing a variable;
     *        null if there is no potential containing the variable
     */
    
    public Potential getPot(FiniteStates x) {
         int i;

  Potential pot;
  
  
  for (i=0 ; i<IndepPot.getSize() ; i++) {
    pot = (Potential) IndepPot.getPotentialAt(i);
    if (pot.getVariables().indexOf(x) >= 0)
      return(pot);
  }
  
 
        
        return(null);
      
    }   
  /**
     * Prints an indep probability tree to the standard output.
     * @param j a tab factor (number of blank spaces before a child
     * is written).
     */
    
    public void print(int j) {
        
        int i, l, k,size;
        PotentialTable p;
     
        
        
        if (label == PROBAB_NODE)
            System.out.print(value+";\n");
            
         
            if (label==FULL_NODE){
            System.out.print("case "+var.getName()+ "{\n");
            
            for(i=0 ; i< child.size() ; i++) {
                for (l=1 ; l<=j ; l++)
                    System.out.print(" ");
                
                System.out.print( var.getState(i) + " = ");
                ((IndepProbabilityTree) getChild(i)).print(j+10);
            }
            
            for (i=1 ; i<=j ; i++)
                System.out.print(" ");
            
            System.out.print("          } \n");
            }
        
        
          if ((label==FULL_NODE)||(label==FINAL_NODE)) {
        
        size = IndepPot.getListSize();
           for (i=1 ; i<=j ; i++)
                System.out.print(" ");   System.out.print("{\n ");
         for(i=0 ; i< size ; i++) {
            p = (PotentialTable)  IndepPot.getPotentialAt(i);
            p.print(j); 
         }
           for (i=1 ; i<=j ; i++)
                System.out.print(" ");   System.out.print("}\n ");      
    }
    }
    
    
        
    
    /**
     * Assigns a variable to an empty node.
     * Initializes as many children as values of the
     * variable, to empty trees.
     * @param variable a <code>FiniteStates</code> variable that will be
     * assigned to the node.
     */
    
    public void assignVar(FiniteStates variable) {
        
        IndepProbabilityTree tree;
        int i,j;
        
        var = variable;
        label = FULL_NODE;
        child = new Vector();
        j = variable.getNumStates();
        
        for (i=0 ; i<j ; i++) {
            tree = new IndepProbabilityTree();
             
            child.addElement(tree);
            
        }
    }
    
    
      
    /**
     * Restricts the independent probability tree starting in this node to a
     * <code>Configuration</code> of variables.
     * @param conf the <code>Configuration</code> to which the tree
     * will be restricted.
     * @param  interest a node in which we are interested and so it is never deleted from the tree 
     * @return a new <code>ProbabilityTree</code> consisting of the restriction
     * of the current tree to the values of <code>Configuration conf</code>.
     */
    
    public ProbabilityTree restrict(Configuration conf, FiniteStates interest) {
        
        PotentialTable t;
        t = null;
      return (restrict(conf,interest,1.0,t));
    }
    


   /**
     * Restricts the independent probability tree starting in this node to a
     * <code>Configuration</code> of variables.
     * @param conf the <code>Configuration</code> to which the tree
     * will be restricted.
     * @param  interest a node in which we are interested and so it is never deleted from the tree 
     * @param x a value for multiplying leaves and used to store the information
     * of the potentials in the path to the root
     * @return a new <code>ProbabilityTree</code> consisting of the restriction
     * of the current tree to the values of <code>Configuration conf</code>.
     */
    
    private ProbabilityTree restrict(Configuration conf, FiniteStates interest, double x, PotentialTable t) {
        
        ProbabilityTree tree, tree2;
        int i, nv, index,size,observed;
        PotentialTable p;
        FiniteStates var1,var2;
        double y;
        
         tree = new ProbabilityTree();
        
        if (label == PROBAB_NODE) {
           
             if (t == null){
             tree.assignProb(value*x);}
             else {
                tree.assignVar(interest);
                nv = interest.getNumStates();
                for(i=0;i<nv;i++){
                    tree2 = ((ProbabilityTree) tree.child.elementAt(i));
                    tree2.assignProb(x*value*t.getValue(i));
                }
                
            }
           
        }
        
        
        if (label == FINAL_NODE)
        {
             y = 1.0;
             size = IndepPot.getListSize();
             
            for(i=0 ; i< size ; i++) {
             p = (PotentialTable)  IndepPot.getPotentialAt(i);
             var1 = ((FiniteStates) (p.getVariables()).elementAt(0));
             index = conf.indexOf(var);
             if(var1.equals(interest))
             { t = (PotentialTable) p.copy();
               if (index>-1) 
                 
              {   
                 observed = conf.getValue(index);
                nv = interest.getNumStates();
                for(i=0;i<nv;i++){
                    
                    if (i!=observed)
                    {t.setValue(i,0.0);}
                  
                }
            }
               
             }
             else {
                  if (index>-1) {
                      observed = conf.getValue(index);
                       y *= p.getValue(observed);
                  }
             }
            
        }
             
           if (t == null){
            tree.assignProb(y*x);}
           else{
                   tree.assignVar(interest);
                nv = interest.getNumStates();
                for(i=0;i<nv;i++){
                    tree2 = ((ProbabilityTree) tree.child.elementAt(i));
                    tree2.assignProb(x*y*t.getValue(i));
                }
           }
           
        }
             
           if (label == FULL_NODE){
              y = 1.0;
             size = IndepPot.getListSize();
             
            for(i=0 ; i< size ; i++) {
             p = (PotentialTable)  IndepPot.getPotentialAt(i);
             var1 = (FiniteStates) p.getVariables().elementAt(0);
             index = conf.indexOf(var);
               if(var1.equals(interest))
             { t = (PotentialTable) p.copy();
               if (index>-1) 
                 
              {   
                 observed = conf.getValue(index);
                nv = interest.getNumStates();
                for(i=0;i<nv;i++){
                    
                    if (i!=observed)
                    {t.setValue(i,0.0);}
                  
                }
               }}
                else {
             if (index>-1) 
             {   observed = conf.getValue(index);
                 y *= p.getValue(observed);
                 
            }
            
        }
            }
            
            
        index = conf.indexOf(var);
        if (index > -1) // if var is in conf
            tree = getChild(conf.getValue(index)).restrict(conf,interest,y*x,t);
        else {
          
            tree.var = var;
            tree.label = FULL_NODE;
            
            nv = child.size();
            for (i=0 ; i<nv ; i++) {
                  if (var.equals(interest)){
                tree2 = ((IndepProbabilityTree) child.elementAt(i)).restrict(conf,interest,y*x*t.getValue(i),null);
            }
               else{tree2 = ((IndepProbabilityTree) child.elementAt(i)).restrict(conf,interest,y*x,t);}
                tree.child.addElement(tree2);
                tree.leaves += tree2.leaves;
            }
        }
        
        
    }
    
      return (tree);
    }
}
    