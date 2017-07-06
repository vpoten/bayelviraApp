/*
 */
package elvira.potential.binaryprobabilitytree;

import java.util.Vector;
import java.io.*;
import elvira.*;
import elvira.inference.elimination.ids.IDVEWithPotentialBPTree.pruningMethods;
import elvira.potential.Potential;
import elvira.parser.ParseException;
import elvira.potential.PotentialTable;
import elvira.tools.CmdLineArguments;
import elvira.tools.CmdLineArguments.CmdLineArgumentsException;
import elvira.tools.CmdLineArguments.argumentType;
import elvira.tools.VectorManipulator;

/**
 * This is a potential whose values are represented by a binary probability tree.
 * A binary probability tree is a compact representation of a probability distribution,
 * alternative to a probability table. 
 * Each internal node represents a variable and each leaf
 * node represents a probability value. Each variable node (internal node)
 * has two children. Each child of an internal node is labeled with a set of 
 * states of the variable of that internal node.
 * The value stored in a leaf node corresponds to the probability of the
 * configurations of variables consistent with the labeling from the root to that leaf.
 * 
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 * @author Rafael Cabañas de Paz (rcabanas@decsai.ugr.es)
 */
public class PotentialBPTree extends Potential {

   /**
    * A <code>BinaryProbabilityTree</code> with the values of this
    * <code>PotentialBPTree</code>.
    */
   private BinaryProbabilityTree values;

   public PotentialBPTree() {

   }

   /**
    * Constructs a <code>PotentialBPTree</code> from another
    * <code>Potential</code>.
    *
    * @param <code>pot</code> the <code>Potential</code> to be transformed to
    * <code>PotentialBPTree</code>.
    */
   public PotentialBPTree(Potential pot) {
      Vector vars;

      vars = (Vector) pot.getVariables().clone();
      setVariables(vars);
      if(pot instanceof elvira.potential.PTreeCredalSet)
          values=BinaryProbabilityTree.getTreeFromPTreeCredalSet((elvira.potential.PTreeCredalSet)pot);
      else
          values = BinaryProbabilityTree.getTreeFromPotential(pot);
   }

       /**
     * Creates a new <code>PotentialBPTree</code> for a given list of variables
     * and a tree with a single value equal to 0.
     * @param vars a <code>Vector</code> of variables (<code>FiniteStates</code>)
     * that the potential will contain.
     */

    public PotentialBPTree(Vector vars) {

        setVariables((Vector)vars.clone());
        values = new BinaryProbabilityTree();
        values.assignProb(0);

    }

    public PotentialBPTree(NodeList vars) {
        setVariables((Vector)vars.getNodes().clone());
        values = new BinaryProbabilityTree(0);
    }


   /**
    * Combines this potential with the argument. The argument <code>pot</code>
    * can be any class of <code>Potential</code> .
    * @returns a new <code>PotentialBPTree</code> consisting of the combination
    * of <code>pot</code> and this <code>PotentialBPTree</code>.
    */
   public Potential combine(Potential pot) {
      PotentialBPTree newPot = new PotentialBPTree();
      newPot.setVariables(SetVectorOperations.union(getVariables(),
              pot.getVariables()));
      newPot.values = values.combine(((PotentialBPTree) pot).values);

      return newPot;
   }

   /**
    * Removes the variable var from this PotentialBPTree summing over all
    * its values.
    * @param var a <code>FiniteStates</code> variable.
    * @return a new <code>BinaryPotentialTree</code> with the result of the 
    * operation.
    */
   public Potential addVariable(Node var) {
      PotentialBPTree newPot = new PotentialBPTree();
      Vector potVariables;

      potVariables = (Vector) getVariables().clone();
      potVariables.removeElement(var);
      newPot.setVariables(potVariables);
      newPot.values = values.addVariable((FiniteStates) var);

      return newPot;
   }

   /**
    * Restricts this potential to a configuration of variables.
    *
    * @param conf the <code>Configuration</code>.
    * @return a new PotentialBPTree with the result of the operation.
    */
   public Potential restrictVariable(Configuration conf) {
      PotentialBPTree newPot = new PotentialBPTree();
      BinaryProbabilityTree newTree;
      Vector potVariables = new Vector();
      int position;
      boolean found = false;
      int nVars = getVariables().size();
      FiniteStates finiteStatesVar;

      newTree = values;
      for (int i = 0; i < nVars; i++) {
         finiteStatesVar = (FiniteStates) getVariables().elementAt(i);
         position = conf.indexOf(finiteStatesVar);
         if (position == -1) { // If finiteStatesVar is not in conf, add it to the new list.
            potVariables.addElement(finiteStatesVar);
         } else {  // Otherwise, restrict the tree to it.
            newTree = newTree.restrict(finiteStatesVar, conf.getValue(position));
            found = true;
         }
      }
      if (!found) { // If none variable was deleted
         newTree = values.copy(); // we make a copy of the tree        
      }
      newPot.setVariables(potVariables);
      newPot.values = newTree;
      return newPot;
   }

   /**
    * Prints this <code>PotentialBPTree</code> to the standard output.
    */
   @Override
   public void print() {
      super.print();
      System.out.println("Number of leaves: "+getNumberOfLeaves());
      values.print(10);
   }

   /**
    * Normalizes this potential to sum up to one.
    */
   @Override
   public void normalize() {
      long totalSize;

      totalSize = (long) FiniteStates.getSize(getVariables());
      values.normalize(totalSize);
   }

   /**
    * Gets the value for a configuration.
    * @param conf a <code>Configuration</code> of FiniteState variables.
    * @return the value corresponding to <code>Configuration conf</code>.
    */
   @Override
   public double getValue(Configuration conf) {
      return values.getProb(conf);
   }

   
   
   
   
   /**
    * Returns the instance of the class BinaryProbabilityTree
    * @return 
    */
    public BinaryProbabilityTree getValues() {
        return values;
    }

   
   
   
   
   /**
    * Bounds the tree associated with this potential by removing
    * nodes whose information value is lower than  threshold "limit".
    * 
    * @param limit the information limit.
    * @see BinaryProbabilityTree.prune(double limit)
    */
   public void limitBound(double limit) {
      long maxSize;
      double globalSum;

      maxSize = (long) FiniteStates.getSize(getVariables());
      globalSum = values.sum(maxSize);
      values.prune(limit, maxSize, globalSum);

   }
   
   public void sort(){
//      System.out.println("ARBOL ANTES DE SORT");
//      values.print(5);
      values = BinaryProbabilityTree.getSortedTreeFromBinaryPT(values, 
              getVariables());  
//      System.out.println("ARBOL DESPUES DE SORT");
//      values.print(5);
   }

   
   /**
    * This method sorts and prune an utility tree
    * @param sort boolean variable that indicates if tree is sorted
    * @param threshold limit value for prunning, if it's 0, it is never prunned
    */
   
   public void sortAndPruneUtility(boolean sort, double threshold){

       BinaryProbabilityTree newTree =  BinaryProbabilityTree.getSortedAndPrunedUtilityTree(values, 
              getVariables(), sort, false, pruningMethods.EUCLIDEAN, threshold, false);
       
       values = newTree;

      
   }
   
   /**
    * This method sorts and prune an utility tree
    * @param sort boolean variable that indicates if tree is sorted
    * @param threshold limit value for prunning, if it's 0, it is never prunned
    */
   
   public void sortAndPruneUtility(boolean sort, boolean normalize, pruningMethods method, double threshold, boolean Cindex){

       BinaryProbabilityTree newTree =  BinaryProbabilityTree.getSortedAndPrunedUtilityTree(values, 
              getVariables(), sort, normalize, method, threshold, Cindex);
       
       values = newTree;

      
   }
   
   
      /**
    * This method sorts and prune an utility tree using the euclidean distance
    * @param threshold limit value for prunning, if it's 0, it is never prunned
    */
   
   public PotentialBPTree sortAndPruneUtility(double threshold){

       PotentialBPTree pt = new PotentialBPTree(this);
       pt.sortAndPruneUtility(true, false, pruningMethods.EUCLIDEAN, threshold, false);
       
       return pt;      
   }
   

  /**
   * Gets the number of nodes of the potential.
   * @return the number of values (size) of the potential.
   */ 
   public long getNumberOfNodes() {
      return values.getNumberOfNodes();
   }

     /**
   * Gets the number of leaves of the potential.
   * @return the number of values (size) of the potential.
   */
   public long getNumberOfLeaves() {
      return values.getNumberOfLeaves();
   }

   /**
    * Method for getting the values
    */
   public BinaryProbabilityTree getTree(){
      return values;
   }

   /**
    * Sets a new BinaryProbabilityTree in this Potential
    * @param newTree the new tree
    */
   protected void setTree(BinaryProbabilityTree newTree){
       values=newTree;
   }

   private static final String argBnetFile = "-bnetFile";

   public static void main(String args[]) throws ParseException, IOException {
      String bnetFileString = null;

      CmdLineArguments params = new CmdLineArguments();
      try {
         params.addArgument(argBnetFile, argumentType.s, "",
                 "The filename of the Bnet (.elv format). No default value, must be provided.");
         params.parseArguments(args);
         params.print();
         bnetFileString = params.getString(argBnetFile);
      } catch (CmdLineArgumentsException ex) {
         params.printHelp();
         System.exit(1);
      }
      if (bnetFileString.equalsIgnoreCase("")) {
         System.out.println(argBnetFile + " argument not found, you must specify one!!!");
         params.printHelp();
         System.exit(1);
      }
      Network b;
      Potential pot;
      PotentialBPTree potbptree;

      b = Network.read(bnetFileString);
      Vector relList = b.getRelationList();
      for (int i = 0; i < relList.size(); i++) {
         pot = ((Relation) (relList.elementAt(i))).getValues();
         potbptree = new PotentialBPTree(pot);
         potbptree.print();
      }
   }
   


   /**
     * Marginalizes a <code>PotentialTree</code> to a list of variables.
     * It is equivalent to remove (add) the other variables.
     * @param vars a <code>Vector</code> of <code>FiniteStates</code> variables.
     * @return a new <code>PotentialTree</code> with the marginal.
     * @see addVariable(Vector vars)
     */

    @Override
    public Potential marginalizePotential(Vector vars) {

        Vector v;
        int i, j;
        boolean found;
        FiniteStates var1, var2;
        PotentialBPTree pot;
        Vector variables = getVariables();

        v = new Vector(); // List of variables to remove
        // (those not in vars).

        for (i=0 ; i<variables.size() ; i++) {
            var1 = (FiniteStates)variables.elementAt(i);
            found = false;

            for (j=0 ; j<vars.size(); j++) {
                var2 = (FiniteStates)vars.elementAt(j);
                if (var1 == var2) {
                    found = true;
                    break;
                }
            }

            if (!found)
            {v.addElement(var1);}
        }

        //eliminate each variable in v
        
        pot = this;
        for(i=0; i<v.size(); i++)
             pot = (PotentialBPTree) pot.addVariable((Node)(v.get(i)));

        return pot;
    }
    
    
        /**
     * Removes a list of variables by applying marginalization by maximum.
     * @param vars a <code>Vector</code> of <code>FiniteStates</code> variables.
     * @return a new <code>PotentialBPTree</code> with the marginal.
     */
    
    @Override
    public PotentialBPTree maxMarginalizePotential(Vector vars) {
        
        Vector aux;
        FiniteStates var1, var2;
        int i, j;
        boolean found;
        PotentialBPTree pot;
        BinaryProbabilityTree tree;
        Vector variables = getVariables();




        aux = new Vector(); // New list of variables.
        for (i=0 ; i<variables.size() ; i++) {
            var1 = (FiniteStates)variables.elementAt(i);
            found = false;
            
            for (j=0 ; j<vars.size() ; j++) {
                var2 = (FiniteStates)vars.elementAt(j);
                if (var1 == var2) {
                    found = true;
                    break;
                }
            }
            
            if (!found)
                aux.addElement(var1);
        }
        
        
        
        tree = values;
        
        for (i=0 ; i<aux.size() ; i++) {
            var1 = (FiniteStates)aux.elementAt(i);
            tree = tree.maximizeOverVariable(var1);

        }

        pot = new PotentialBPTree(vars); // The new tree.

        pot.setTree(tree);
        
        return pot;
    }
    
    /**
     * This method divides two potentials.
     * For the exception 0/0, the method computes the result as 0.
     * The exception ?/0: the method aborts with a message in the standar output.
     * @param p the <code>PotentialBPTree</code> to divide with this.
     * @return a new <code>PotentialBPTree</code> with the result of
     * dividing this potential by <code>p</code>.
     */

    @Override
    public Potential divide(Potential p) {

        Vector v, v1, v2;
        FiniteStates aux;
        int i, nv;
        PotentialBPTree pot;
        double x;
        BinaryProbabilityTree tree, tree1, tree2;

        v1 = getVariables();   // Variables of this potential.
        v2 = p.getVariables(); // Variables of the argument.
        v = new Vector(); // Variables of the new potential.

        for (i=0 ; i<v1.size() ; i++) {
            aux = (FiniteStates)v1.elementAt(i);
            v.addElement(aux);
        }

        for (i=0 ; i<v2.size() ; i++) {
            aux = (FiniteStates)v2.elementAt(i);
            if (aux.indexOf(v1) == -1)
                v.addElement(aux);
        }

        // The new Potential.
        pot = new PotentialBPTree(v);

        tree1 = getTree();                          // Tree of this potential.
        tree2 = ((PotentialBPTree)p).getTree();       // Tree of the argument.

        tree = BinaryProbabilityTree.divide(tree1,tree2); // The new tree.

        pot.setTree(tree);

        return pot;
    }


    /**
     * This method calcules the size of the tree (number of leaves).
     * @return size of the tree
     */
    @Override
    public long getSize() {
        return values.getSize();
    }
    
    
    
    /**
     * Computes a measure of the influence of each state of each variable in
     * the utility.
     * @return 
     */
    public double averageVariance() {
    
        double avg = 0;
        Vector vars = values.getVariables();
        Vector variances = new Vector();
        
        for(int i=0; i<vars.size(); i++) {
            FiniteStates v = (FiniteStates) vars.get(i);
            Vector meanLeaves = new Vector();
            for(int k=0; k<v.getNumStates(); k++) {
                BinaryProbabilityTree restrTree = values.copy().restrict(v, k);
                meanLeaves.add(VectorManipulator.mean(restrTree.getLeaves()));
            
            }
            
            variances.add(VectorManipulator.variance(meanLeaves));
            
        
        }
        
        
        return VectorManipulator.mean(variances);
        
    
    }
    
    
    
        /**
     * Copies this potential.
     * @return a copy of this <code>PotentialTree</code>.
     */
    
    @Override
    public Potential copy() {
        
        PotentialBPTree pot;
        
        
        pot = new PotentialBPTree(this);
      //  pot.values = values.copy();

        
        return pot;
    }
    
        /**
     * Sets the value for a configuration.
     * @param conf a <code>Configuration</code>.
     * @param x a <code>double</code>, the new value for <code>conf</code>.
     */
    
    public void setValue(Configuration conf, double x) {
    
        PotentialTable t = new PotentialTable(this);
        t.setValue(conf, x);
        
        PotentialBPTree bpt = new PotentialBPTree(t);
        this.values = bpt.values.copy();
        
    
    }
    
    



}
