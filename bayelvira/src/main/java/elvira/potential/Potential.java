/* Potential.java */
package elvira.potential;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.io.*;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.RelationList;
import elvira.SetVectorOperations;
import elvira.potential.binaryprobabilitytree.BinaryProbabilityTree;
import elvira.potential.binaryprobabilitytree.PotentialBPTree;

/**
 *
 * Abstract class Potential. Specifies some methods and instance
 * variables that are common to all kinds of potentials.
 * The basic structure of a potential is a list of variables
 * for which it is defined, and a piece of information about
 * those variables. Usually, that information will be given
 * as a probability table or as a probability tree.
 * @since 2/4/2001
 *
 */
public abstract class Potential implements Cloneable, Serializable {

   static final long serialVersionUID = 3955066704255756705L;
   /**
    * A <code>Vector</code> with the <code>Node</code>s of this
    * <code>Potential</code>.
    */
   Vector variables;
   public static final int ONLY_VARS = 0;
   public static final int ONLY_VALUES = 1;
   private static int nTrans = 0;

   /*
    * ABSTRACT METHODS
    */
   /**
    *
    * Combines this <code>Potential</code> with <code>Potential</code> pot
    * @param pot the <code>Potential</code> to combine with this
    * <code>Potential</code>.
    * @return The combination of the two potentials.
    */
   abstract public Potential combine(Potential pot);

   /**
    *
    * Sum this <code>Potential</code> with <code>Potential</code> pot
    * @param pot the <code>Potential</code> to sum with this
    * <code>Potential</code>.
    * @return The addition of the two potentials.
    */
   public Potential addition(Potential pot) {
      System.out.println("Error in Potential.addition: this method must be overloaded in " + this.getClass());
      System.exit(1);
      return null;
   }

   /**
    * Removes the argument variable summing over all its values.
    * @param var a Node variable.
    * @return a new Potential with the result of the deletion.
    */
   abstract public Potential addVariable(Node var);

   /**
    * Saves this potential to a <code>PrintWriter</code> as a
    * <code>PotentialTable</code>. This method must be used to
    * save the results of a propagation algorithm, not to save a network.
    * @param p the <code>PrintWriter</code> where the potential will be
    * written.
    */
   public void saveResult(PrintWriter p) {
      PotentialTable pot2;
      pot2 = new PotentialTable(this);
      pot2.saveResult(p);
   }

   /**
    * Saves the potential to the file represented by the
    * <code>PrintWriter p</code>.
    * @param p the <code>PrintWriter</code>.
    */
   public void save(PrintWriter p) {
      System.out.println("Error in Potential.save(PrintWriter): this method must be overloaded in " + this.getClass());
      System.exit(1);
   }

   /**
    * Normalizes the values of this potential. The object is modified.
    */
   public void normalize() {
      System.out.println("Error in Potential.normalize(): this method must be overloaded in " + this.getClass());
      System.exit(1);
   }

   /**
    *
    * Gets the value of a potential for a given configuration of
    * variables.
    * @param conf a <code>Configuration</code> of variables.
    * @return the value of the potetial for
    * <code>conf</code>.
    */
   public double getValue(Configuration conf) {
      System.out.println("Error in Potential.getValue(Configuration): this method must be overloaded in " + this.getClass());
      System.exit(1);
      return 0.0;
   }

   /**
    * Sets the value of a <code>Potential</code> for a given
    * <code>Configuration</code>.
    * @param conf a <code>Configuration</code> of variables.
    * @param val the value for conf.
    */
   public void setValue(Configuration conf, double val) {
      System.out.println("Error in Potential.setValue(Configuration,double): this method must be overloaded in " + this.getClass());
      System.exit(1);
   }

   /**
    * Gets the size of the potential. The size of a potential is the number
    * of real numbers used to represent it.
    * @return the size of the array in table mode;
    * the number of nodes in tree mode.
    */
   public long getSize() {
      System.out.println("Error in Potential.getSize(): this method must be overloaded in " + this.getClass());
      System.exit(1);
      return 0;
   }

   /**
    * Gets the addition of all the values of the potential.
    * @return the addition of all the values of the potential.
    */
   public double totalPotential() {
      System.out.println("Error in Potential.totalPotential(): this method must be overloaded in " + this.getClass());
      System.exit(1);
      return 0;
   }

   /**
    * Gets the addition of the values of a potential that are consistent
    * with a given configuration of variables.
    * @param conf a <code>Configuration</code> of variables.
    * @return the sum of the values of this potential restricted to
    * <code>Configuration conf</code>.
    */
   public double totalPotential(Configuration conf) {
      System.out.println("Error in Potential.totalPotential(Configuration): this method must be overloaded in " + this.getClass());
      System.exit(1);
      return 0;
   }

   /**
    * Computes the entropy of a potential.
    * @return the sum of the values x * Log x stored in the potential.
    */
   public double entropyPotential() {
      System.out.println("Error in Potential.entropyPotential(): this method must be overloaded in " + this.getClass());
      System.exit(1);
      return 0;
   }

   /**
    * Computes the entropy of a potential restricted to
    * a given configuration.
    * @param conf the <code>Configuration</code>.
    * @return the sum of the values x Log x fixing
    * <code>Configuration conf</code>.
    */
   public double entropyPotential(Configuration conf) {
      System.out.println("Error in Potential.entropyPotential(Configuration): this method must be overloaded in " + this.getClass());
      System.exit(1);
      return 0;
   }

   /**
    * Restricts the potential to a configuration of variables.
    * @param conf the <code>Configuration</code>.
    * @return the restricted <code>Potential</code>.
    */
   abstract public Potential restrictVariable(Configuration conf);

   /**
    * Marginalizes over a set of variables. It is equivalent
    * to <code>addVariable</code> over the other variables.
    * @param vars a <code>Vector</code> of variables (<code>Node</code>).
    * @return a <code>Potential</code> with the marginalization
    * over <code>vars</code>.
    */
   public Potential marginalizePotential(Vector vars) {
      System.out.println("Error in Potential.marginalizePotential(Vector): this method must be overloaded in " + this.getClass());
      System.exit(1);
      return null;
   }

   /*
    * REGULAR METHODS
    */

   /**
    * Makes a new <code>Potential</code>.
    */
   public Potential makePotential() {
      return null;
   }

   /**
    * Gets the variables of the potential.
    * @return a <code>Vector</code> of objects of class
    * <code>Node</code>
    * with the variables of the potential.
    */
   public Vector getVariables() {
      return variables;
   }

   /**
    * Sets the variables of the potential.
    * @param a <code>Vector</code> of <code>Node</code> with the variables
    * of the potential.
    */
   public void setVariables(Vector v) {
      variables = v;
   }

   /**
    * Combines in this <code>Potential</code> the <code>Potential</code> pot1
    * and pot2, but only the fields indicated by <code>field</code> with
    * the following meaning:
    * <UL>
    * <LI> field=ONLY_VARS then combine only field <code>variable</code>
    * </UL>
    * @param pot1 the first <code>Potential</code> to be combined.
    * @param pot2 the second <code>Potential</code> to be combined.
    */
   public void combine(Potential p1, Potential p2, int field) {
      if (field == ONLY_VARS) { // Combine only field variable of pot1 and pot2
         Vector v1, v2;
         int i;
         Node aux;

         v1 = p1.variables;
         v2 = p2.variables;
         if (variables.size() != 0) {
            System.out.println("Error in Potential.combine("
                + "Potential p1,Potential p2,int field): " + "variables.size!=0");
            System.exit(1);
         }

         for (i = 0; i < v1.size(); i++) {
            aux = (Node) v1.elementAt(i);
            variables.addElement(aux);
         }

         for (i = 0; i < v2.size(); i++) {
            aux = (Node) v2.elementAt(i);
            if (aux.indexOf(v1) == -1) {
               variables.addElement(aux);
            }
         }
      }
   }

   /**
    * This method orders the potential making the <code>Node</code> x
    * the last of the set of variables
    * @param x the <code>Node</code> to be the last.
    */
   public Potential sendVarToEnd(Node x) {
      PotentialTable pot = new PotentialTable(this);
      return pot.sendVarToEnd(x);
   }

   /**
    * This method orders the potential making the order of the variables is
    * consistent with 'order'. Not all the variables of the potential have 
    * to be elements of 'order'.
    * This method is used to present the policies of the ID with the variables
    * in temporal order.
    * @param x the <code>Node</code> to be the last.
    */
   public Potential placeVariablesAccordingToOrder(ArrayList<String> order) {

      NodeList finalOrderOfVariables = new NodeList();
      Potential newPotential;
      Configuration configuration;
      double value;
      int i;
      Node auxNode;
      long j, size;
      boolean found;

      // Copy the variables from the potential to finalOrderOfVariables,
      // changing the order
      for (String aux : order) {
         found = false;
         for (i = 0; ((i < variables.size()) && (found == false)); i++) {
            auxNode = (Node) variables.elementAt(i);
            if (auxNode.getName().equals(aux)) {
               finalOrderOfVariables.insertNode(auxNode);
               found = true;
            }
         }
      }

      // Now, make a potential with the reordered set of variables
      if (getClassName().equals("PotentialTree")) {
         newPotential = new PotentialTree(finalOrderOfVariables);
      } else {
         newPotential = new PotentialTable(finalOrderOfVariables);
      }

      size = (long) FiniteStates.getSize(variables);

      // Now, copy the values
      configuration = new Configuration(finalOrderOfVariables);

      // Loop to copy the values
      for (j = 0; j < size; j++) {
         value = getValue(configuration);

         // This value will be copied into newPotential
         newPotential.setValue(configuration, value);

         // Move to next configuration
         configuration.nextConfiguration();
      }

      // At the end return the new Potential
      return ((Potential) newPotential);
   }

   /**
    * Removes in <code>Potential pot</code> the argument variable
    * summing over all its values, but only the fields indicated by
    * <code>field</code> with the following  meaning:
    * <UL>
    * <LI> field=ONLY_VARS then sum out only field <code>variable</code>
    * </UL>
    * The result is put in this <code>Potential</code>
    * @param pot a <code>Potential</code> to be sum out over <code>var</code>.
    * @param var a FiniteStates variable.
    * @param field a int to determine to which field the
    * marginalization affects.
    */
   public void addVariable(Potential pot, FiniteStates var, int field) {
      if (field == ONLY_VARS) { // sum out only in field variable
         variables = (Vector) pot.getVariables().clone();
         variables.removeElement(var);
      }
   }

   /**
    * Normalizes a given potential to sum up to one.
    * @param pot a <code>Potential</code>.
    * @return a new normalized
    * <code>Potential</code>.
    */
   public Potential normalize(Potential pot) {
      Potential pt;
      pt = pot.copy();
      pt.normalize();
      return pt;
   }

   /**
    * Restricts the potential to a configuration of variables
    * except a goal variable.
    * @param conf the <code>Configuration</code>.
    * @param goalVar the goal variable (<code>Node</code>).
    * @return the restricted <code>Potential</code>.
    *
    */
   public Potential restrictVariable(Configuration conf, Node goalVar) {
      Potential pot = null;
      System.out.println("restrict: Not implemented in this class");
      System.exit(1);
      return pot;
   }

   /**
    * Prints the potential to the standard output.
    */
   public void print() {
      int i;

      System.out.println("Potential of class: " + getClass().getName());
      System.out.print(variables.size() + " Variables in potential:");
      for (i = 0; i < variables.size(); i++) {
         System.out.print(" " + ((Node) (variables.elementAt(i))).getName());
      }
      System.out.println(" HashCode: " + hashCode());
   }
   
   /**
    * Method for printing the domain of a potential
    */
   public void printDomain() {
      Node node;
      
      System.out.print("pot(");
      for (int i = 0; i < variables.size(); i++) {
         node = (Node) variables.elementAt(i);
         System.out.print(" " + node.getName());
      }
      System.out.print(") --- pot. size: " + getSize());
   }    

   /**
    * This method combines the potential which receives the message with
    * the one passed as parameter.
    * The method is not defined as abstract to avoid the need of definining
    * it in all the subclasses of <code>Potential</code>. Those subclasses
    * that really need this method should overwrite it.
    * (At the moment the method is overwriten only in <code>PotentialTable</code>).
    * @param a <code>Potential</code> that has to be a subset of the
    * <code>Potential</code> which receives the message.
    */
   public void combineWithSubset(Potential p) {
      System.out.println("At the moment the method (Potential)combineWithSubset is not implemented for " + this.getClass().getName());
      System.exit(0);
   }

   /**
    * This method incorporates the evidence passed as argument to the
    * <code>Potential</code>, that is, sets to 0 all the values whose
    * configurations are not consistent with the evidence.
    * @param ev a <code>Configuration</code> representing the evidence.
    * The method is not defined as abstract to avoid the need of definining
    * it in all the subclasses of <code>Potential</code>. Those subclasses
    * that really need this method should overwrite it. (At the moment the
    * method is overwriten only in <code>PotentialTable</code>).
    */
   public void instantiateEvidence(Configuration evid) {
      System.out.println("Error in Potential.instantiateEvidenceI(Configuration): At the moment the method (Potential)instantiateEvidence is not implemented for "
              + this.getClass().getName());
      System.exit(0);
   }

   /**
    * This method divides two potentials.
    * For the exception 0/0, the method computes the result as 0.
    * The exception ?/0: the method aborts with a message in the standar output.
    * @param p the <code>Potential</code> to divide with this.
    * @return a new <code>Potential</code> with the result of the combination.
    * The method is not defined as abstract to avoid the need of definining
    * it in all the subclasses of <code>Potential</code>. Those subclasses
    * that really need this method should overwrite it. (At the moment the
    * method is overwriten only in <code>PotentialTable</code>).
    */
   public Potential divide(Potential p) {
      System.out.println("At the moment the method (Potential)divide is not implemented for " + this.getClass().getName());
      System.exit(0);
      return this; // the method never will arrive to this point
   }

   /**
    * Marginalizes over a set of variables using MAXIMUM as marginalization
    * operator.
    * @param vars a <code>Vector</code> of variables (<code>Node</code>).
    * @return a <code>Potential</code> with the marginalization over
    * <code>vars</code>.
    * The method is not defined as abstract to avoid the need of definining
    * it in all the subclasses of <code>Potential</code>. Those subclasses
    * that really need this method should overwrite it. (At the moment the
    * method is overwriten only in <code>PotentialTable</code>).
    *
    */
   public Potential maxMarginalizePotential(Vector vars) {
      System.out.println("At the moment the method (Potential)maxMarginalizePotential is not implemented for " + this.getClass().getName());
      System.exit(0);
      return this; // the method never will arrive until this point
   }

   /**
    * Gets the configuration of maximum probability consistent with a
    * configuration of variables.
    * @param subconf the subconfiguration to ensure consistency.
    * @return the <code>Configuration</code> of maximum probability
    * included in the <code>Potential</code>, that is consistent with the
    * subConfiguration passed as parameter (this subconfiguration can be empty).
    * NOTE: if there are more than one configuration with maximum
    * probability, the first one is returned.
    * The method is not defined as abstract to avoid the need of definining
    * it in all the subclasses of <code>Potential</code>. Those subclasses
    * that really need this method should overwrite it. (At the moment the
    * method is overwriten only in <code>PotentialTable</code>).
    */
   public Configuration getMaxConfiguration(Configuration subconf) {
      System.out.println("At the moment the method (Potential)getMaxConfiguration is not implemented for " + this.getClass().getName());
      System.exit(0);
      return subconf; // the method never will arrive until this point
   }

   /**
    * @param subconf the subconfiguration to ensure consistency
    * @param list the list of configurations to be differents
    * @return the configuration of maximum probability included in the
    * potential, that is consistent with the subConfiguration passed as
    * parameter (this subconfiguration can be empty), and differents to
    * all the configurations passed in the vector
    * NOTE: if there are more than one configuration with maximum
    * probability, the first one is returned
    * The method is not defined as abstract to avoid the need of definining
    * it in all the subclasses of <code>Potential</code>. Those subclasses
    * that really need this method should overwrite it. (At the moment the
    * method is overwriten only in <code>PotentialTable</code>).
    */
   public Configuration getMaxConfiguration(Configuration subconf,
           Vector list) {
      System.out.println("At the moment the method (Potential)getMaxConfiguration is not implemented for " + this.getClass().getName());
      System.exit(0);
      return subconf; // the method never will arrive until this point
   }

   /**
    * Gets the minimum probability consistent with a configuration of variables.
    * @param subconf the subconfiguration to ensure consistency.
    * @return the minimum probability included in this <code>Potential</code>,
    * that is consistent with the subConfiguration passed as parameter 
    * (this subconfiguration can be empty).
    */
   public double getMinimum(Configuration subconf) {
      Configuration sc;
      double prob = Double.MAX_VALUE;
      int total, i;
      Vector vars;
      Vector varsSubConf, varsNotInSubConf;
      SetVectorOperations svo = new SetVectorOperations();

      varsSubConf = subconf.getVariables();
      vars = new Vector(varsSubConf);
      varsNotInSubConf = svo.notIn(variables, varsSubConf);
      vars.addAll(svo.notIn(variables, varsSubConf));
      sc = new Configuration(vars, subconf);
      total = (int) FiniteStates.getSize(varsNotInSubConf);

      if (total == 0) {
         total = (int) FiniteStates.getSize(variables);
      }

      for (i = 0; i < total; i++) {
         if (getValue(sc) < prob) {
            prob = getValue(sc);
         }
         sc.nextConfiguration();
      }

      return prob;
   }

   /**
    * Gets the maximum probability consistent with a configuration of variables.
    * @param subconf the subconfiguration to ensure consistency.
    * @return the maximum probability included in this <code>Potential</code>,
    * that is consistent with the subConfiguration passed as parameter 
    * (this subconfiguration can be empty).
    */
   public double getMaximum(Configuration subconf) {
      Configuration sc;
      double prob = Double.MIN_VALUE;
      int total, i;
      Vector vars;
      Vector varsSubConf, varsNotInSubConf;
      SetVectorOperations svo = new SetVectorOperations();

      varsSubConf = subconf.getVariables();
      vars = new Vector(varsSubConf);
      varsNotInSubConf = svo.notIn(variables, varsSubConf);
      vars.addAll(svo.notIn(variables, varsSubConf));
      sc = new Configuration(vars, subconf);
      total = (int) FiniteStates.getSize(varsNotInSubConf);

      if (total == 0) {
         total = (int) FiniteStates.getSize(variables);
      }

      for (i = 0; i < total; i++) {
         if (getValue(sc) > prob) {
            prob = getValue(sc);
         }
         sc.nextConfiguration();
      }

      return prob;
   }

   /**
    * To be defined by the subclasses that need it.
    */
   public void showResult() {
      System.out.println("At the moment the method (Potential)showResults is not implemented for " + this.getClass().getName());

      /*
       * andrew
       */ print();
      // System.exit(0);
   }

   /**
    * Gets the name of the class.
    * @return a <code>String</code> with the name of the class.
    */
   public String getClassName() {
      return new String("Potential");
   }

   /**
    * Tells whether the <code>Potential</code> is exact or not.
    * @return <code>true</code> if the <code>Potential</code> is exact,
    * <code>false</code> otherwise. By default, returns <code>true</code>.
    */
   public boolean getExact() {
      return true;
   }

   /**
    * Gets the value for a configuration of variables. In this case, the
    * configuration is represented by means of an array of int.
    * At each position, the value for certain variable is stored.
    * To know the position in the array corresponding to a given
    * variable, we use a hash table. In that hash table, the
    * position of every variable in the array is stored.
    *
    * @param positions a <code>Hashtable</code>.
    * @param conf an array of <code>int</code>.
    * @return the value corresponding to configuration <code>conf</code>.
    */
   public double getValue(Hashtable positions, int[] conf) {
      System.out.println("WARNING: At the moment the method (Potential)getValue(Hashtable,int[])  is not implemented for "
              + this.getClass().getName());

      //System.exit(0);
      /*
       * andrew
       */ print();
      return 0;
   }

   /**
    * Prunes the potential according to an information value.
    * the object is modified.
    * @param limitForPrunning the information value (<code>double</code>)
    * under which the potential will be prunned.
    */
   public void limitBound(double limitForPrunning) {
      System.out.println("Error in Potential.limitBound(double): this method must be overloaded in subclasses of Potential");
   }

   /**
    * Bounds the tree associated with the potential by removing
    * nodes whose information value is lower than a given threshold
    * or whose addition is lower than a given value.
    * THE TREE IS MODIFIED.
    * @param limit the information limit.
    * @param limitSum the limit sum for pruning.
    */
   public void limitBound(double limit, double limitSum) {
      System.out.println("Error in Potential.limitBound(double,double): this method must be overloaded in subclasses of Potential");
   }

   /**
    * Bounds the tree associated with the potential by removing
    * nodes whose information value is lower than a given threshold
    * or whose addition is lower than a given value.
    * THE TREE IS MODIFIED.
    * @param kindOfApprPruning the method used to approximate several leaves
    * with a double value (AVERAGE_APPROX, ZERO_APPROX, AVERAGEPRODCOND_APPROX
    * ...)
    * @param limit the information limit.
    * @param limitSum the limit sum for pruning.
    */
   public void limitBound(int kindOfApprPruning, double limit, double limitSum) {
      System.out.println("Error in Potential.limitBound(int,double,double): this method must be overloaded in subclasses of Potential");
   }

   /**
    * Prunes the potential keeping the branches with
    * higher probability.
    * The object is modified.
    * @param n the number of leaves to keep (<code>int</code>)
    * under which the potential will be prunned.
    */
   public void limitBound(int n) {
      System.out.println("Error in Potential.limitBound(int): this method must be overloaded in subclasses of Potential");
   }

   /**
    * Sorts the variables in the potential placing first the most
    * informative ones, and pruned the potential not to exceed a maximum
    * number of values.
    * @param maxLeaves the maximum number of values allowed.
    * @return the resulting
    * <code>Potential</code>.
    */
   public Potential sortAndBound(int maxLeaves) {
      System.out.println("Error: sortAndBound de Potential");
      return this;
   }

   /**
    * Conditions this potential to another one. To condition is to store
    * in one potential the values of the other one.
    * @param pot the conditioning <code>Potential</code>.
    * @return the conditioned
    * <code>Potential</code>.
    */
   public Potential conditional(Potential pot) {
      System.out.println("Error:conditional de Potential");
      return this;
   }

   /**
    * Prunes the potential according to an information value.
    * the object is modified.
    * @param lp the information value (<code>double</code>)
    * under which the potential will be prunned (depending on the method).
    * @param llp the lower limit for prunning (used depending on the method).
    * @param method the method of prunning (see subclass definitions).
    */
   public void conditionalLimitBound(double lp, double llp, int method) {
      System.out.println("conditionalLimitBound de Potential");
   }

   /**
    * Copies this potential.
    * @return a copy of this <code>Potential</code>.
    */
   public Potential copy() {
      System.out.println("Error: class " + getClassName() + " do not have method copy() implemented");
      System.exit(1);
      return this;
   }

   /**
    * Method to transform the potential and to make the values be converted
    * to imprecise Dirichlet model
    * @param n number of samples
    * @param s param for conversion
    * @return transformed potential table
    *
    */
   public Potential toImpreciseDirichletModel(int n, int s) {
      Vector values;
      FiniteStates var;
      PotentialTable minValues;
      PotentialTable maxValues;
      PotentialIntervalTable finalValues;
      int sizeOfVar;
      int i, j;
      double val;
      double min;
      double max;

      // Get the set of variables
      Vector vars = getVariables();

      // Build two new potentials for these variables
      minValues = new PotentialTable(vars);
      maxValues = new PotentialTable(vars);

      // Get all the variables, except the first
      Vector conditioning = new Vector();

      // Add variables to conditioning
      for (i = 1; i < vars.size(); i++) {
         conditioning.addElement(vars.elementAt(i));
      }

      // Get the first variable to determine the number of values
      var = (FiniteStates) vars.elementAt(0);
      sizeOfVar = var.getNumStates();

      // Build a configuration for all variables except the first
      Configuration conf = new Configuration(conditioning);

      // Build a configuration for the whole set of variables
      Configuration total = new Configuration(vars);

      // Now, go on the whole set of values for the variables 
      // in conditioning
      int size = conf.possibleValues();

      // May be the potential only has one variable. In this case
      // size will be 1, and everything is ok
      for (i = 0; i < size; i++) {
         // Consider all the values for var
         for (j = 0; j < sizeOfVar; j++) {
            // Complete the total configuration
            total.resetConfiguration(conf);
            total.putValue(var, j);

            // Get the value for this configuration
            val = getValue(total);

            // Get the min
            min = (val * n) / (n + s);

            // Get the max
            max = ((val * n) + s) / (n + s);

            // Show all the values
            System.out.println("Prob = " + val + " (min = " + min + ", max = " + max + ")");

            // Set these values to the potential tables for min and max
            minValues.setValue(total, min);
            maxValues.setValue(total, max);
         }

         // Consider the next configuration
         conf.nextConfiguration();
      }

      // Now, make the potential interval table
      finalValues = new PotentialIntervalTable(minValues, maxValues);
      finalValues.print();

      // Return the potential interval table
      return finalValues;
   }

   /**
    * Method to change the values of a potential so that be defined with a
    * convex set. The extreme points are generated from the values already
    * present
    * @param n number of extreme points to generate for every configuration
    * of parent values
    * @param range amplitude of the interval used to generate the extreme points
    * of the convex set
    * @return result of the transformation
    */
   public Potential toConvexSetModel(int n, double range) {
      PTreeCredalSet convexDefinition;
      FiniteStates var;
      int i, j, k, sizeOfVar;
      Random random = new Random();
      double change;
      double val;
      double sum = 0;

      // Get the set of variables
      Vector vars = getVariables();

      // Create a PTreeCredalSet for this variables
      convexDefinition = new PTreeCredalSet(vars);

      // Get all the variables, except the first
      Vector conditioning = new Vector();

      // Add variables to conditioning
      for (i = 1; i < vars.size(); i++) {
         conditioning.addElement(vars.elementAt(i));
      }

      // Get the first variable to determine the number of values
      var = (FiniteStates) vars.elementAt(0);
      sizeOfVar = var.getNumStates();

      // Build a configuration for all variables except the first
      Configuration conf = new Configuration(conditioning);

      // Build a configuration for the whole set of variables
      Configuration total = new Configuration(vars);

      // Now, go on the whole set of values for the variables 
      // in conditioning
      int size = conf.possibleValues();

      // May be the potential only has one variable. In this case
      // size will be 1, and everything is ok
      for (i = 0; i < size; i++) {
         // Add a transparent variable for every configuration. The new variable
         // will have the number of values indicated by n
         FiniteStates transVar = convexDefinition.appendTransparentVariable(n);

         // Make a configuration with the variables of total + the transparent itself
         conditioning.addElement(transVar);
         conditioning.addElement(var);
         Configuration completeConf = new Configuration(conditioning);

         // We must generate extreme points for all the values of transparent
         // variable
         for (j = 0; j < n; j++) {
            // The values for the extreme points will be stored in this vector
            Vector newValues = new Vector();

            // First at all, modify the actual set of values, adding or removing
            // a random quantity
            total.resetConfiguration(conf);

            // Put sum to 0
            sum = 0;

            for (k = 0; k < sizeOfVar; k++) {
               // Complete the total configuration
               total.putValue(var, k);

               // With this we can get the actual value
               val = getValue(total);

               // Generate a random numbre
               change = random.nextFloat();

               // The random number is modified. At the end must be in the range
               // -range,+range, and will be added to val
               change = (2 * range) * change - range;
               val = val + change;

               // Avoid the number be less than 0
               if (val < 0) {
                  val = -val;
               }

               // Se modifica para evitar valores negativos y mayores que 1
               newValues.addElement(new Double(val));

               // Consider the sum to normalize at the end
               sum += val;
            }

            // A new loop is required to normalize the values
            for (k = 0; k < newValues.size(); k++) {
               val = ((Double) newValues.elementAt(k)).doubleValue() / sum;
               newValues.removeElementAt(k);
               newValues.insertElementAt(new Double(val), k);
            }

            // This set of values will be stored in the PTreeCredalSet
            // It is required a new loop over the values of var. First
            // at all fix the value for the transparent variable
            completeConf.resetConfiguration(conf);
            completeConf.putValue(transVar, j);

            // Now consider all the values for var
            for (k = 0; k < sizeOfVar; k++) {
               // Set the value for this variable
               completeConf.putValue(var, k);

               // Set the value
               convexDefinition.setValue(completeConf, ((Double) newValues.elementAt(k)).doubleValue());
            }
         }

         // The same for the next configuration
         conf.nextConfiguration();

         // Remove the transparent variable and var itself, to prepare
         // the new configuration of parents values
         conditioning.removeElementAt(conditioning.size() - 1);
         conditioning.removeElementAt(conditioning.size() - 1);
      }

      // Now, return the PTreeCredalSet
      //convexDefinition.print();
      return convexDefinition;
   }

   /**
    *
    * Method to change the values of a potential so that be defined with a
    * convex set. The extreme points are generated from the values already
    * present
    * @param limit prob for receiving n extreme points
    * @param n number of extreme points to generate for every configuration
    * of parent values
    * @param range amplitude of the interval used to generate the extreme points
    * of the convex set
    * @param keepCeros flag to indicate the need to keep ceros
    * @return result of the transformation
    */
   public Potential toConvexSetModel(double percentage, int n, double range, boolean keepCeros) {

      PTreeCredalSet convexDefinition;
      FiniteStates var;
      int i, j, k, sizeOfVar;
      Random random = new Random();
      double change;
      double val;
      double sum = 0;
      double conditioningSetSize;
      int finalExtremePoints;
      Random generator = new Random();
      double prob;

      // Get the set of variables
      Vector vars = getVariables();

      // Create a PTreeCredalSet for this variables
      convexDefinition = new PTreeCredalSet(vars);

      // Get all the variables, except the first
      Vector conditioning = new Vector();

      // Add variables to conditioning
      for (i = 1; i < vars.size(); i++) {
         conditioning.addElement(vars.elementAt(i));
      }

      // Get the first variable to determine the number of values
      var = (FiniteStates) vars.elementAt(0);
      sizeOfVar = var.getNumStates();

      // Build a configuration for all variables except the first
      Configuration conf = new Configuration(conditioning);
      conditioningSetSize = conf.possibleValues();

      // Build a configuration for the whole set of variables
      Configuration total = new Configuration(vars);

      // Now, go on the whole set of values for the variables
      // in conditioning
      int size = conf.possibleValues();

      // May be the potential only has one variable. In this case
      // size will be 1, and everything is ok
      for (i = 0; i < size; i++) {
         // Add a transparent variable for every configuration.
         // The number of extreme points will depend on the percentage:
         // we select a random number between 0 and 1. A prob. value under
         // percentage will produce a transparent with n states and over it
         // with only 1 state
         prob = generator.nextDouble();
         if (prob < percentage) {
            // will have the number of values indicated by n
            finalExtremePoints = n;
         } else {
            finalExtremePoints = 1;
         }

         FiniteStates transVar = convexDefinition.appendTransparentVariable(finalExtremePoints);

         // Make a configuration with the variables of total + the transparent itself
         conditioning.addElement(transVar);
         conditioning.addElement(var);
         Configuration completeConf = new Configuration(conditioning);

         // We must generate extreme points for all the values of transparent
         // variable
         for (j = 0; j < finalExtremePoints; j++) {
            // The values for the extreme points will be stored in this vector
            Vector newValues = new Vector();

            // First at all, modify the actual set of values, adding or removing
            // a random quantity
            total.resetConfiguration(conf);

            // Put sum to 0
            sum = 0;
            for (k = 0; k < sizeOfVar; k++) {
               // Complete the total configuration
               total.putValue(var, k);

               // With this we can get the actual value
               val = getValue(total);

               // If the value is cero and it must be kept, do nothing
               if (!(val == 0 && keepCeros)) {
                  // Generate a random numbre
                  change = random.nextFloat();

                  // The random number is modified. At the end must be in the range
                  // -range,+range, and will be added to val
                  change = (2 * range) * change - range;
                  val = val + change;

                  // Avoid the number be less than 0
                  if (val < 0) {
                     val = -val;
                  }
               }

               // Se modifica para evitar valores negativos y mayores que 1
               newValues.addElement(new Double(val));

               // Consider the sum to normalize at the end
               sum += val;
            }

            // A new loop is required to normalize the values
            for (k = 0; k < newValues.size(); k++) {
               val = ((Double) newValues.elementAt(k)).doubleValue() / sum;
               newValues.removeElementAt(k);
               newValues.insertElementAt(new Double(val), k);
            }

            // This set of values will be stored in the PTreeCredalSet
            // It is required a new loop over the values of var. First
            // at all fix the value for the transparent variable
            completeConf.resetConfiguration(conf);
            completeConf.putValue(transVar, j);

            // Now consider all the values for var
            for (k = 0; k < sizeOfVar; k++) {
               // Set the value for this variable
               completeConf.putValue(var, k);

               // Set the value
               convexDefinition.setValue(completeConf, ((Double) newValues.elementAt(k)).doubleValue());
            }
         }

         // The same for the next configuration
         conf.nextConfiguration();

         // Remove the transparent variable and var itself, to prepare
         // the new configuration of parents values
         conditioning.removeElementAt(conditioning.size() - 1);
         conditioning.removeElementAt(conditioning.size() - 1);
      }

      // Now, return the PTreeCredalSet
      //convexDefinition.print();
      return convexDefinition;



   }

   /**
    *
    * Append a new transparent variable to this Potential with
    * <code>npoints</code> cases
    * @param npoints the number of cases for the new transparent variable
    * @return the new transparent variable
    */
   public FiniteStates appendTransparentVariable(int npoints) {
      FiniteStates trans_node = new FiniteStates(npoints);
      trans_node.setTransparency(FiniteStates.TRANSPARENT);
      trans_node.setName("Transparent" + nTrans);
      nTrans++;
      variables.addElement(trans_node);
      return trans_node;
   }

   /**
    * Gets a copy of the list of transparent variables from this potential
    * @return a Vector with the list of transparent variables from this
    * potential
    */
   public Vector getListTransparents() {
      Vector list = new Vector();
      FiniteStates var;
      for (int i = 0; i < variables.size(); i++) {
         var = (FiniteStates) (variables.elementAt(i));
         if (var.getTransparency() == FiniteStates.TRANSPARENT) {
            list.addElement(var);
         }
      }
      return list;
   }

   /**
    * Gets a copy of the list of non transparent variables from this potential
    * @return a Vector with the list of non transparent variables from this
    * potential
    */
   public Vector getListNonTransparents() {
      Vector list = new Vector();
      FiniteStates var;

      for (int i = 0; i < variables.size(); i++) {
         var = (FiniteStates) (variables.elementAt(i));
         if (var.getTransparency() != FiniteStates.TRANSPARENT) {
            list.addElement(var);
         }
      }
      return list;
   }

   /**
    * Method to assign a set of values to a potential. The values to assign
    * are related to a given configuration. Among the variables of the
    * configuration
    * (first argument) there will be a special one. From the given
    * configuration,
    * taking into account the whole domain for the special variable, will be
    * generated a set of configurations where a certain value will be assigned
    * @param configuration base Configuration     *
    * @param variable special variable. Must be contained in the variables of
    * the potential
    * @param value to assign
    */
   public void setValuesForConf(Configuration configuration, FiniteStates variable, double value) {

      // Create a new configuration for the whole set of the potential variables
      Vector allVars = getVariables();
      Configuration completeConf = new Configuration(allVars);
      Node var;
      boolean found = false;
      int i;

      // Set the values for the variables contained in configuration
      completeConf.resetConfiguration(configuration);

      // Consider now the whole set of values for the special variable. First at
      // all check if the variable belongs to the set of potential variables
      for (i = 0; i < allVars.size(); i++) {
         var = (Node) allVars.elementAt(i);
         if (var.getName().equals(variable.getName()) == true) {
            found = true;
            break;
         }
      }

      // If the variable was not found, there is nothing to do
      if (found == true) {
         // Consider the values for variable
         for (i = 0; i < variable.getNumStates(); i++) {
            // Set the value for variable in completeConf
            completeConf.putValue(variable, i);

            // Set the value for the potential in this position
            setValue(completeConf, value);
         }
      } else {
         System.out.println("Error in class Potential: Method setValuesForConf");
         System.out.println("The variable: " + variable.getName() + " is not present in potential");
         System.exit(0);
      }
   }

   /**
    *
    * Method to get a set of values from a potential. The values to get are
    * related to a given configuration, but will consider all the values for 
    * a special variable passed as second argument. This method is used for 
    * utilities related to a given configuration, and for all the alternatives 
    * for that decision
    * @param configuration base Configuration    
    * @param variable special variable. Must be contained in the variables of
    * the potential
    * @return Vector with tha values
    */
   public Vector getValuesForConf(Configuration configuration, FiniteStates variable) {
      Vector allVars = getVariables();
      Potential restricted;
      Vector results = null;
      Node var;
      boolean found = false;
      double value;
      int i;

      // Consider now the whole set of values for the special variable. First at
      // all check if the variable belongs to the set of potential variables
      for (i = 0; i < allVars.size(); i++) {
         var = (Node) allVars.elementAt(i);
         if (var.getName().equals(variable.getName()) == true) {
            found = true;
            break;
         }
      }

      // If the variable was not found, there is nothing to do
      if (found == true) {
         // Give size to the vector of results
         results = new Vector();
         results.setSize(variable.getNumStates());

         // Restrict the potential to the values given by conf
         restricted = restrictVariable(configuration);

         // Create a configuration for the special variable. This is
         // requiered to retrieve the values of the potential consistent
         // with the configuration
         Vector vectorForVar = new Vector();
         vectorForVar.addElement(variable);
         Configuration confForVar = new Configuration(vectorForVar);

         // Consider the set of values for variable
         for (i = 0; i < variable.getNumStates(); i++) {
            value = restricted.getValue(confForVar);

            // Store this value
            results.setElementAt(new Double(value), i);

            // Consider the next value
            confForVar.nextConfiguration();
         }
      } else {
         System.out.println("Error in class Potential: Method getValuesForConf");
         System.out.println("The variable: " + variable.getName() + " is not present in potential");
         System.exit(0);
      }

      // Finally, return the vector with the values
      return results;
   }

   /**
    * Method to set values in the potential. The values will be stored in a
    * configuration composed with the configuration passed as argument and 
    * with another variables passed as a second argument. The rest of arguments 
    * will be a vector of repetitions (only if this vector is not null it will 
    * be needed to store several values in the potential) and the value to store
    * @param baseConf where to store the value
    * @param variable to add to the variables in configuration. So it is
    * completely defined the configuration where the value will be stored
    * @param valForVariable to assign to the variable, to complete the
    * configuration where to store a value 
    * @param repetitions of repetitions. If this vector is not null several
    * positions in the potential will be assigned the same value    
    * @param valueToStore to store in the set of selected configurations
    */
   public void setValues(Configuration baseConf, FiniteStates variable, int valForVariable, Vector repetitions, double valueToStore) {
      Vector allVars;
      Configuration completeConf;
      int index;
      int i;

      // Create a configuration with all the variables
      allVars = getVariables();

      // Create a configuration
      completeConf = new Configuration(allVars);

      // Set the values for the variables in conf
      completeConf.resetConfiguration(baseConf);

      // Consider the whole set of indices stored in the vector, but only
      // if it is required (there are repetitions)
      if (repetitions != null) {
         for (i = 0; i < repetitions.size(); i++) {
            index = ((Integer) (repetitions.elementAt(i))).intValue();

            // Set the value for var in completeConf
            completeConf.putValue(variable, index);

            //Set value in this position
            setValue(completeConf, valueToStore);
         }
      }

      // Work now with the position given by maxIndex
      completeConf.putValue(variable, valForVariable);

      // Set the value
      setValue(completeConf, valueToStore);
   }

   /**
    * Method to remove from the potential the variables which does not take part
    * into the PotentialTree or PotentialBPTree used to store the values.
    * @param removeOnlyTrans Boolean to indicate whether all or only transparent
    * variables must be removed.
    * @param currentRelations Current relations among variables.
    * @param interest variables of interest. These will not nbe removed
    */
   public void removeVarsNotInTree(boolean removeOnlyTrans, RelationList currentRelations, NodeList interest) {
      Vector varsToRemove = new Vector();
      ProbabilityTree pTree = null;
      BinaryProbabilityTree bpTree = null;

      // Depends on the kind of tree
      if (this instanceof PotentialTree) {
         pTree = ((PotentialTree) this).getTree();
      } else if (this instanceof PotentialBPTree) {
         bpTree = ((PotentialBPTree) this).getTree();
      }

      // For each variable in potential
      for (int i = 0; i < variables.size(); i++) {
         FiniteStates var = (FiniteStates) variables.elementAt(i);
         // If all variables must be removed
         if (!removeOnlyTrans && interest.getId(var.getName()) == -1) {
            if (pTree != null && !pTree.isIn(var)) {
               varsToRemove.addElement(var);
            } else if (bpTree != null && !bpTree.isIn(var)) {
               varsToRemove.addElement(var);
            }
         } // Continues if var is transparent, since only transparent variables
         // must be removed
         else if (var.getTransparency() == FiniteStates.TRANSPARENT /*
                  * && var.getKindOfNode() == Node.CHANCE && currentRelations.isIn(var)
                  */ /*
                  * && currentRelations.size() > 1
                  */) {

            if (pTree != null && !pTree.isIn(var)) {
               varsToRemove.addElement(var);
            } else if (bpTree != null && !bpTree.isIn(var)) {
               varsToRemove.addElement(var);
            }
         }
      }

      // Removes all varsToRemove not present in the tree from the potential
      for (int i = 0; i < varsToRemove.size(); i++) {
         variables.remove(varsToRemove.elementAt(i));
      }
   }
} // end of class

