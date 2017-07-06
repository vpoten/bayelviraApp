/* _________________________________________________________________________
 
                           PotentialIntervalTree
 
                             Elvira Project
 
   File: PotentialIntervalTree.java
   Description: Implements a potential which elements are intervals
   Author: Manuel Gomez
 
 
 
   _________________________________________________________________________
 
   Note:
 
   ________________________________________________________________________ */

package elvira.potential;

import java.util.Vector;
import elvira.*;
import java.io.*;


/**
 * Class : PotentialIntervalTree
 * Description: Potential to represent a probability distribution
 * defined by intervals
 * Implements a potential as two set of values: one for min and
 * another for max values
 * @author Manuel Gómez Olmedo (mgomez@decsai.ugr.es)
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 */

public class PotentialIntervalTree extends PotentialInterval{
  
  private ProbabilityTree minValues;
  private ProbabilityTree maxValues;
  long size;
 
  /**
   * Constructor initializing both probability trees to null
   */
  
  public PotentialIntervalTree() {
    minValues=null;
    maxValues=null;
    size=0;
  }
  
  /**
   * Constructs a new PotentialIntervalTree
   * @param min PotentialTree with min values
   * @param max PotentialTree with max values
   */
  public PotentialIntervalTree(PotentialTree min, PotentialTree max) {
    double value;
   
    min.print();
    max.print();

    // Set the variables
    variables=(Vector)(min.getVariables()).clone();

    // Initialize minValues and maxValues
    minValues=new ProbabilityTree();
    maxValues=new ProbabilityTree();

    // The values will be copied one by one

    int nCases=(int)FiniteStates.getSize(variables);
    Configuration conf=new Configuration(variables);

    // Loop to copy the values

    for(int i=0; i < nCases; i++){
       value=min.getValue(conf);
       // Set to minValues
       setMinValue(conf,value);
       // Get max value
       value=max.getValue(conf);
       // Set yhe value
       setMaxValue(conf,value);
       conf.nextConfiguration();  
    }
  }
  
  /**
   * Constructs a new <code>PotentialIntervalTree</code> for a list of
   * variables and creates a pair of probability trees to store the
   * values
   * @param vars a <code>Vector</code> of
   * variables (<code>FiniteStates</code>).
   */
  
  public PotentialIntervalTree(Vector vars) {
    // Set the variables for this potential 
    variables = (Vector)vars.clone();
    minValues = new ProbabilityTree();
    maxValues = new ProbabilityTree();
  }
  
  /**
   * Constructs a new <code>PotentialIntervalTree</code> from a CredalSet
   */
  public PotentialIntervalTree(CredalSet pot){
    variables=pot.getListNonTransparents();
    Configuration conf=new Configuration(variables);
    int ncases=(int)FiniteStates.getSize(variables);
    
    minValues = new ProbabilityTree();
    maxValues = new ProbabilityTree();
    for(int i=0;i<ncases;i++){
      setMinValue(conf, pot.getMinimum(conf));
      setMaxValue(conf, pot.getMaximum(conf));
      conf.nextConfiguration();
    }
  }
  
  /**
   * Get a copy of the min values
   */
  
  double[] getArrayCopyMinValues(){
    PotentialIntervalTable pot=new PotentialIntervalTable(this);
    return pot.getArrayCopyMinValues();
  }
  
  /**
   * Get a copy of the array of max values
   */
  
  double[] getArrayCopyMaxValues(){
    PotentialIntervalTable pot=new PotentialIntervalTable(this);
    return pot.getArrayCopyMaxValues();
  }
  
  /**
   * Copies this potential.
   * @return a copy of this <code>PotentialIntervalTree</code>.
   */
  public Potential copy() {    
    PotentialIntervalTree pot;
    pot = new PotentialIntervalTree();
    pot.variables=(Vector)variables.clone(); 
    int ncases=(int)FiniteStates.getSize(variables);
    Configuration conf=new Configuration(variables);
    
    pot.minValues=new ProbabilityTree();
    pot.maxValues=new ProbabilityTree();
    for(int i=0;i<ncases;i++){
      setMinValue(conf, pot.getMinimum(conf));
      setMaxValue(conf, pot.getMaximum(conf));
      conf.nextConfiguration();
    }
    return pot;
  }
  
  /**
   *  Print both min and max values
   */
  
  public void print() {
    
    super.print();
    System.out.print("values = tree ( \n");
    System.out.println("Minimun values: ");
    minValues.print(2);
    System.out.println("Maximun values: ");
    maxValues.print(2);
    System.out.print("                );\n");
  }
  
  
  
  public String getClassName() {
    return new String("PotentialIntervalTree");
  }
  
  /**
   * Methods to implement
   */
  
  public Potential marginalizePotential(Vector vector){
    System.out.println("Error in PotentialIntervalTree.marginalizePotential(Vector): Method not implemented for PotentialIntervalTree");
    System.exit(1);
    return null;
  }
  
  public double entropyPotential() {
    System.out.println("Error in PotentialIntervalTree.entropyPotential(): Method not implemented for PotentialIntervalTree");
    System.exit(1);
    return 0;
  }
  
  public double entropyPotential(Configuration conf) {
    System.out.println("Error in PotentialIntervalTree.entropyPotential(Configuration): Method not implemented for PotentialIntervalTree");
    System.exit(1);
    return 0;
  }
  
  public double totalPotential() {
    System.out.println("Error in PotentialIntervalTree.totalPotential(): Method not implemented for PotentialIntervalTree");
    System.exit(1);
    return 0;
  }
  
  public double totalPotential(Configuration conf) {
    System.out.println("Error in PotentialIntervalTree.totalPotential(Configuration): Method not implemented for PotentialIntervalTree");
    System.exit(1);
    return 0;
  }
  
  /**
   * Method that returns the size of the potential, the number of values
   * it could be store on it
   */
  
  public long getSize() {
    return minValues.getSize();
  }
  
  /**
   * Sets the value in a position in the array of values.
   * @param index the position in the array to modify.
   * @param value the value to store in that position.
   */
  
  public void setValue(int index, double value) {
    System.out.println("Error in PotentialIntervalTree.setValue(int,double): Method not implemented for PotentialIntervalTree");
    System.exit(1);
    return;
  }
  
  
  /**
   * Sets the min value in a position in the array of min values.
   * @param index the position in the array to modify.
   * @param value the value to store in that position.
   */
  
  public void setMinValue(int index, double value) {
    Configuration conf=new Configuration(variables);
    int ncases=conf.possibleValues();
    int i;
    
    // Check for errors

    if (index < 0 || index > ncases-1){
       System.out.println("Error in PotentialIntervalTree.setMinValue(int,double): index not defined (max = "+ncases+") argument = "+index);
       System.exit(1);
       return;
    }

    for(i=0; i < index; i++){
      conf.nextConfiguration();
    }
   
    // Set the min value for this configuration
     
    setMinValue(conf,value);
  }
  
  /**
   * Sets the max value in a position in the array of max values.
   * @param index the position in the array to modify.
   * @param value the value to store in that position.
   */
  
  public void setMaxValue(int index, double value) {
    Configuration conf=new Configuration(variables);
    int ncases=conf.possibleValues();
    int i;
    
    // Check for errors

    if (index < 0 || index > ncases-1){
       System.out.println("Error in PotentialIntervalTree.setMinValue(int,double): index not defined (max = "+ncases+") argument = "+index);
       System.exit(1);
       return;
    }

    for(i=0; i < index; i++){
      conf.nextConfiguration();
    }
   
    // Set the min value for this configuration
     
    setMaxValue(conf,value);
  }
  
  /**
   * Sets the value for a configuration of variables.
   * @param conf a <code>Configuration</code> of variables.
   * @param value a the new value for <code>Configuration conf</code>.
   */
  
  public void setValue(Configuration conf, double value) {
    System.out.println("Error in PotentialIntervalTree.setValue(Configuration,double): Method not implemented for PotentialIntervalTree");
    System.exit(1);
    return;
  }
  
  /**
   * Sets the value for a configuration of variables.
   * @param conf a <code>Configuration</code> of variables.
   * @param value a the new value for <code>Configuration conf</code>.
   */
  
  public void setMinValue(Configuration conf, double value) {
    Configuration aux;
    ProbabilityTree tree;
    FiniteStates var;
    int i, p, val, s;
    boolean update;
  

    update = true;
    //aux = conf.copy();
    aux = conf.duplicate();
    s = conf.getVariables().size();
    tree = minValues;

    for (i=0 ; i<s ; i++) {

      if (!tree.isVariable()) {
        var = aux.getVariable(0);
        val = aux.getValue(0);
        aux.remove(0);
      
        if (tree.isProbab()){ // if the node is a probability,
           update = false;      // do not update the number of leaves.
           tree.assignVar(var,tree.value);
        }
        else{
          tree.assignVar(var);
        }
      }
      else {
        p = aux.indexOf(tree.getVar());
        var = aux.getVariable(p);
        val = aux.getValue(p);
        aux.remove(p);
      }

      tree = tree.getChild(val);
    }

    tree.assignProb(value);
    if (update)
      size++;
  }
  
  /**
   * Sets the value for a configuration of variables.
   * @param conf a <code>Configuration</code> of variables.
   * @param value a the new value for <code>Configuration conf</code>.
   */
  
  public void setMaxValue(Configuration conf, double value) {
    Configuration aux;
    ProbabilityTree tree;
    FiniteStates var;
    int i, p, val, s;
    boolean update;
  

    update = true;
    //aux = conf.copy();
    aux = conf.duplicate();
    s = conf.getVariables().size();
    tree = maxValues;

    for (i=0 ; i<s ; i++) {

      if (!tree.isVariable()) {
        var = aux.getVariable(0);
        val = aux.getValue(0);
        aux.remove(0);
      
        if (tree.isProbab()){ // if the node is a probability,
           update = false;      // do not update the number of leaves.
           tree.assignVar(var,tree.value);
        }
        else{
          tree.assignVar(var);
        }
      }
      else {
        p = aux.indexOf(tree.getVar());
        var = aux.getVariable(p);
        val = aux.getValue(p);
        aux.remove(p);
      }

      tree = tree.getChild(val);
    }

    tree.assignProb(value);
    if (update)
      size++;
  }
  
  
  /**
   * Sets all the values in a potential to a given value.
   * @param value a real value.
   */
  
  public void setValue(double value) {
    System.out.println("Error in PotentialIntervalTree.setValue(double): Method not implemented for PotentialIntervalTree");
    System.exit(-1);
    return;
  }
  
  /**
   * Sets all the min values in a potential to a given value.
   * @param value a real value.
   */
  
  public void setMinValue(double value) {
    Configuration conf=new Configuration(variables);
    int ncases=conf.possibleValues();
    
    for (int index=0 ; index < ncases ; index++)
      setMinValue(conf,value);
  }
  
  /**
   * Sets all the max values in a potential to a given value.
   * @param value a real value.
   */
  
  public void setMaxValue(double value) {
    Configuration conf=new Configuration(variables);
    int ncases=conf.possibleValues();
    
    for (int index=0 ; index < ncases ; index++)
      setMaxValue(conf,value);
  }
  
  /**
   * Gets the value for a configuration of variables.
   * @param conf a <code>Configuration</code>.
   * @return the value of the potential for <code>Configuration conf</code>.
   */
  
  public double getValue(Configuration conf) {
    System.out.println("Error in PotentialIntervalTable.getValue(Configuration): Method not implemented for PotentialIntervalTable");
    System.exit(1);
    return 0;
  }
  
  /**
   * Gets the min value for a configuration of variables.
   * @param conf a <code>Configuration</code>.
   * @return the value of the potential for <code>Configuration conf</code>.
   */
  
  public double getMinValue(Configuration conf) {
    int pos;
    Configuration aux;
    
    // Take a configuration from conf just for variables
    // in the potential.
    aux = new Configuration(variables,conf);
    return minValues.getProb(conf);
  }
  
  /**
   * Gets the max value for a configuration of variables.
   * @param conf a <code>Configuration</code>.
   * @return the value of the potential for <code>Configuration conf</code>.
   */
  
  public double getMaxValue(Configuration conf) {
    int pos;
    Configuration aux;
    
    // Take a configuration from conf just for variables
    // in the potential.
    aux = new Configuration(variables,conf);
    return maxValues.getProb(conf);
  }
  
  /**
   * Restricts this potential to the configuration of variables conf.
   * @param conf a <code>Configuration</code>.
   * @return A new <code>PotentialInterval</code> resulting from the restriction
   * of this PotentialInterval to <code>conf</code>.
   */
  
  public Potential restrictVariable(Configuration conf) {
    
    Configuration auxConf;
    Vector aux;
    FiniteStates temp;
    PotentialIntervalTree pot;
    int i,ncases;
    
    // Creates a configuration preserving the values in conf.
    auxConf = new Configuration(variables,conf);
    
    // Computes the list of variables of the new Potential.
    aux = new Vector();
    for (i=0 ; i<variables.size() ; i++) {
      temp = (FiniteStates)variables.elementAt(i);
      if (conf.indexOf(temp) == -1)
        aux.addElement(temp);
    }
   
    ncases=(int)FiniteStates.getSize(aux); 
    pot = new PotentialIntervalTree(aux);
    
    for (i=0 ; i < ncases ; i++) {
      pot.setMinValue(auxConf,getMinValue(auxConf));
      pot.setMaxValue(auxConf,getMaxValue(auxConf));
      auxConf.nextConfiguration(conf);
    }
    
    return pot;
  }
  
  /**
   * Normalizes the values of this potential.
   * The object is modified.
   */
  
  public void normalize() {
    Configuration conf=new Configuration(variables);
    int nCases=conf.possibleValues();

    minValues.normalize(nCases);
    maxValues.normalize(nCases);
  }
  
  /**
   * Saves a potential to a file. This one must be used to
   * save the results of a propagation, not to save a network.
   * It includes the name of the variables.
   * @param p the <code>PrintWriter</code> where the potential will be written.
   */
  
  public void saveResult(PrintWriter p) {
    
    int i, total;
    Configuration conf;
    
    for(i=0;i<variables.size();i++){
      p.println("node "+((FiniteStates)variables.elementAt(i)).getName());
    }
    
    saveAsConfig(p);
  }
  
  /**
   * Saves a potential to a file. This one must be used when
   * saving a network.
   * @param p the <code>PrintWriter</code> where the potential will be written.
   */
  
  public void saveAsConfig(PrintWriter p) {
    
    p.print("values = tree-interval ( \n");
   
    // Print the tree with min values

    p.print("Min values: ");
    minValues.save(p,2);

    // Print the tree with max values

    p.print("Max values: ");
    maxValues.save(p,2); 
    
    p.print("                );\n");
  }
  
  /**
   * Saves a potential to a file. This one must be used when
   * saving a network. The values are written as a table.
   * @param p the <code>PrintWriter</code> where the potential will be written.
   */
  
  public void save(PrintWriter p) {
    
    int i, total;
    
    p.print("values= tree-interval (");

    // Print the tree with min values

    p.print("Min values: ");
    minValues.save(p,2);

    // Print the tree with max values

    p.print("Max values: ");
    maxValues.save(p,2); 

    p.print(");\n");
  }
  
  /**
   * Removes the argument variable summing over all its values.
   * @param var a <code>Node</code> variable to be removed.
   * @return a new <code>PotentialTable</code> with the result of the deletion.
   */
  
  public Potential addVariable(Node var) {
    System.out.println("Error in PotentialIntervalTree.addVariable(Node): Method not implemented for PotentialIntervalTree");
    System.exit(11);
    return null;
  }
  
  /**
   * Sums over all the values of the variables in a list.
   * @param vars a <code>Vector</code> containing variables
   * (<code>FiniteStates</code>).
   * @return a new <code>PotentialTable</code> with
   * the variables in <code>vars</code> removed.
   */
  
  public PotentialIntervalTree addVariable(Vector vars) {
    System.out.println("Error in PotentialIntervalTree.addVariable(Vector): Method not implemented for PotentialIntervalTree");
    System.exit(1);
    return null;
  }
  
  /**
   * Combines two potentials. The argument <code>p</code> can be a
   * <code>PotentialTable</code>, a <code>PotentialTree</code>, a
   * a <code>CanonicalPotential</code>,  a <code>PotentialConvexSet</code> or
   * a <code>PotentialContinuousPT</code>.
   * @param p the <code>Potential</code> to combine with this.
   * @return a new <code>PotentialTable</code> with the result of
   * the combination. If <code>p</code> is a code>PotentialContinuousPT</code>
   * then the results is returned as a code>PotentialContinuousPT</code>.
   */
  
  public Potential combine(Potential p) {
    System.out.println("Error in PotentialIntervalTree.combine(Potential): Method not implemented for PotentialIntervalTree");
    System.exit(11);
    return null;
  }
  
  /**
   * Method to assign the values of a default interval
   * @param defaults Vector with the default interval
   */
  
  public void setDefaultValues(Vector defaults){
    // This method will not be used 
  }
}
