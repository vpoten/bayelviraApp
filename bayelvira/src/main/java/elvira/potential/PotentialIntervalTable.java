/* _________________________________________________________________________
 
                           PotentialIntervalTable
 
                             Elvira Project
 
   File: PotentialIntervalTable.java
   Description: Implements a potential which elements are intervals
   Author: Manuel Gomez, Andrés Cano
 
 
 
   _________________________________________________________________________
 
   Note:
 
   ________________________________________________________________________ */

package elvira.potential;

import java.util.Vector;
import elvira.*;
import java.io.*;


/**
 * Class : PotentialIntervalTable
 * Description: Potential to represent a probability distribution
 * defined by intervals
 * Implements a potential as two set of values: one for min and
 * another for max values
 * @author Manuel Gómez Olmedo (mgomez@decsai.ugr.es)
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 */

public class PotentialIntervalTable extends PotentialInterval{
  
  private double minValues[];
  private double maxValues[];
  
  public PotentialIntervalTable() {
    minValues=null;
    maxValues=null;
  }
  
  /**
   * Constructs a new PotentialIntervalTable
   * @param min PotentialTable with min values
   * @param max PotentialTable with max values
   */
  public PotentialIntervalTable(PotentialTable min, PotentialTable max) {
    double newValues[];
    // Set the variables
    variables=(Vector)(min.getVariables()).clone();
    // The values for min will be stored in the field minValues 
    minValues=min.getValues();
    // Set space for this new vector
    newValues=new double[minValues.length];
    // Copy all of these values
    System.arraycopy(minValues,0,newValues,0,minValues.length);
    minValues=newValues;
    // The same for max
    maxValues=max.getValues();
    // Set space for the new vector
    newValues=new double[maxValues.length];
    // Copy both arrays
    System.arraycopy(maxValues,0,newValues,0,maxValues.length);
    maxValues=newValues;
  }
  
  /**
   * Constructs a new <code>PotentialIntervalTable</code> for a list of
   * variables and creates an array to store the values.
   * @param vars a <code>Vector</code> of
   * variables (<code>FiniteStates</code>).
   */
  
  public PotentialIntervalTable(Vector vars) {
    int nv;
    
    // Compute the size of the array.
    nv = (int)FiniteStates.getSize(vars);
    
    variables = (Vector)vars.clone();
    minValues = new double[nv];
    maxValues = new double[nv];
  }
  
  /**
   * Constructs a new <code>PotentialIntervalTable</code> from a CredalSet
   */
  public PotentialIntervalTable(CredalSet pot){
    variables=pot.getListNonTransparents();
    Configuration conf=new Configuration(variables);
    int ncases=(int)FiniteStates.getSize(variables);
    
    minValues = new double[ncases];
    maxValues = new double[ncases];
    for(int i=0;i<ncases;i++){
      setMinValue(conf, pot.getMinimum(conf));
      setMaxValue(conf, pot.getMaximum(conf));
      conf.nextConfiguration();
    }
  }

  /**
   * Constructs a PotentialIntervalTable given a PotentialIntervalTree
   */
  public PotentialIntervalTable(PotentialIntervalTree pot){
    variables=(Vector)pot.getVariables().clone();
    Configuration conf=new Configuration(variables);
    int ncases=(int)FiniteStates.getSize(variables);

    // Allocate space for minValues and maxValues

    minValues=new double[ncases];
    maxValues=new double[ncases];

    // Loop to get all the values

    for(int i=0; i < ncases; i++){
      setMinValue(conf,pot.getMinValue(conf));
      setMaxValue(conf,pot.getMaxValue(conf));
      conf.nextConfiguration();
    }
  }
  
  /**
   * Method to actualize the values of the potential given a PotentialConvexSet
   * with a set of extreme points
   * @param pot PotentialConvexSet with the set of extreme points to convert
   * into intervals.The update is done if the values in the potential convex
   * set are bigger than or lesser than the values already stored in the intervals 
   */
  public void actualizeValues(PotentialConvexSet pot) {
    // Make a configuration with the non transparent variables in pot
    Configuration nonTrans=new Configuration(pot.getListNonTransparents());
    
    // Consider all the cases
    for(long i=0; i < FiniteStates.getSize(nonTrans.getVariables()); i++) {
      // Get max value for this configuration in the potential convex set
      double max=pot.getMaximum(nonTrans);
      double min=pot.getMinimum(nonTrans);
      
      // Actualize max and min values if required. For that is required to
      // get tge max and min values stored in this potential
      double actualMin=getMinValue(nonTrans);
      double actualMax=getMaxValue(nonTrans);
      
      // Update if requiered
      if (max > actualMax && max != Double.NaN) {
        setMaxValue(nonTrans,max);
      }
      if (min < actualMin && min != -Double.NaN) {
        setMinValue(nonTrans,min);
      }
      
      // Go to the next configuration
      nonTrans.nextConfiguration();
    }
  }
  
  /**
   * Get a copy of the array of min values
   */
  
  double[] getArrayCopyMinValues(){
    double mValues[]=new double[minValues.length];
    System.arraycopy(minValues,0, mValues, 0, minValues.length);
    return mValues;
  }
  
  /**
   * Get a copy of the array of max values
   */
  
  double[] getArrayCopyMaxValues(){
    double mValues[]=new double[maxValues.length];
    System.arraycopy(maxValues,0, mValues, 0, maxValues.length);
    return mValues;
  }
  
  /**
   * Copies this potential.
   * @return a copy of this <code>PotentialTable</code>.
   */
  public Potential copy() {    
    PotentialIntervalTable pot;
    int  n;
    
    pot = new PotentialIntervalTable();
    pot.variables=(Vector)variables.clone(); 
    pot.minValues=getArrayCopyMinValues();
    pot.maxValues=getArrayCopyMaxValues();
    return pot;
  }
  
  /**
   *  Print both min and max values
   */
  
  public void print() {
    int i, total;
    Configuration conf;
    
    super.print();
    System.out.print("values = table-interval ( \n");
    total = (int)FiniteStates.getSize(variables);
    conf = new Configuration(variables);
    for (i=0 ; i<total ; i++) {
      System.out.print("                ");
      conf.print();
      System.out.print(" ["+minValues[i]+","+maxValues[i]+"],\n");
      conf.nextConfiguration();
    }
    System.out.print("                );\n");
  }
  
  
  
  public String getClassName() {
    return new String("PotentialIntervalTable");
  }
  
  /**
   * Methods to implement
   */
  
  public Potential marginalizePotential(Vector vector){
    System.out.println("Error in PotentialIntervalTable.marginalizePotential(Vector): Method not implemented for PotentialIntervalTable");
    System.exit(1);
    return null;
  }
  
  public double entropyPotential() {
    System.out.println("Error in PotentialIntervalTable.entropyPotential(): Method not implemented for PotentialIntervalTable");
    System.exit(1);
    return 0;
  }
  
  public double entropyPotential(Configuration conf) {
    System.out.println("Error in PotentialIntervalTable.entropyPotential(Configuration): Method not implemented for PotentialIntervalTable");
    System.exit(1);
    return 0;
  }
  
  public double totalPotential() {
    System.out.println("Error in PotentialIntervalTable.totalPotential(): Method not implemented for PotentialIntervalTable");
    System.exit(1);
    return 0;
  }
  
  public double totalPotential(Configuration conf) {
    System.out.println("Error in PotentialIntervalTable.totalPotential(Configuration): Method not implemented for PotentialIntervalTable");
    System.exit(1);
    return 0;
  }
  
  /**
   * Method that returns the size of the potential, the number of values
   * it could be store on it
   */
  
  public long getSize() {
    return minValues.length;
  }
  
  /**
   * Sets the value in a position in the array of values.
   * @param index the position in the array to modify.
   * @param value the value to store in that position.
   */
  
  public void setValue(int index, double value) {
    System.out.println("Error in PotentialIntervalTable.setValue(int,double): Method not implemented for PotentialIntervalTable");
    System.exit(1);
    return;
  }
  
  
  /**
   * Sets the min value in a position in the array of min values.
   * @param index the position in the array to modify.
   * @param value the value to store in that position.
   */
  
  public void setMinValue(int index, double value) {
    // Esto no esta bien, pero para que tire
    
    minValues[index] = value;
  }
  
  /**
   * Sets the max value in a position in the array of max values.
   * @param index the position in the array to modify.
   * @param value the value to store in that position.
   */
  
  public void setMaxValue(int index, double value) {
    // Esto no esta bien, pero para que tire
    
    maxValues[index] = value;
  }
  
  /**
   * Sets the value for a configuration of variables.
   * @param conf a <code>Configuration</code> of variables.
   * @param value a the new value for <code>Configuration conf</code>.
   */
  
  public void setValue(Configuration conf, double value) {
    System.out.println("Error in PotentialIntervalTable.setValue(Configuration,double): Method not implemented for PotentialIntervalTable");
    System.exit(1);
    return;
  }
  
  /**
   * Sets the value for a configuration of variables.
   * @param conf a <code>Configuration</code> of variables.
   * @param value a the new value for <code>Configuration conf</code>.
   */
  
  public void setMinValue(Configuration conf, double value) {
    int index;
    Configuration aux;
    
    aux = new Configuration(variables,conf);
    index = aux.getIndexInTable();
    minValues[index] = value;
  }
  
  /**
   * Sets the value for a configuration of variables.
   * @param conf a <code>Configuration</code> of variables.
   * @param value a the new value for <code>Configuration conf</code>.
   */
  
  public void setMaxValue(Configuration conf, double value) {
    int index;
    Configuration aux;
    
    aux = new Configuration(variables,conf);
    index = aux.getIndexInTable();
    maxValues[index] = value;
  }
  
  /**
   * Method to actualize the values for max, if the values passed
   * as argument is greater than the 
   * @param value
   */
  
  
  /**
   * Sets all the values in a potential to a given value.
   * @param value a real value.
   */
  
  public void setValue(double value) {
    System.out.println("Error in PotentialIntervalTable.setValue(double): Method not implemented for PotentialIntervalTable");
    System.exit(-1);
    return;
  }
  
  /**
   * Sets all the min values in a potential to a given value.
   * @param value a real value.
   */
  
  public void setMinValue(double value) {
    int index;
    
    for (index=0 ; index<minValues.length ; index++)
      minValues[index] = value;
  }
  
  /**
   * Sets all the max values in a potential to a given value.
   * @param value a real value.
   */
  
  public void setMaxValue(double value) {
    int index;
    
    for (index=0 ; index<minValues.length ; index++)
      maxValues[index] = value;
  }
  
  /**
   * Method to set the whole set of min values for the intervals
   * @param minVals array with the min values
   */
  public void setMinValues(double[] minVals) {
    minValues=minVals;
  }
  
  /**
   * Method to set the whole set of max values for the intervals
   * @param maxVals array with the min values
   */
  public void setMaxValues(double[] maxVals) {
    maxValues=maxVals;
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
    pos = aux.getIndexInTable();
    
    return minValues[pos];
  }

  /**
   * Method to get the minimum value related to a given position
   * The position shows the configuration of the leaf to reach
   * @param index
   * @return minimum value for this configuration
   */
  public double getMinValue(long index){
  	// Creta e aconfiguration with the variables of this potential
  	Configuration conf=new Configuration(variables);
  	
  	// Jump to the wished set of values
  	conf.goToConfiguration(index);
  	
  	// Get the minValue for this configuration
  	return(getMinValue(conf));
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
    pos = aux.getIndexInTable();
    
    return maxValues[pos];
  }

  /**
   * Method to get the minimum value related to a given position
   * The position shows the configuration of the leaf to reach
   * @param index
   * @return minimum value for this configuration
   */
  public double getMaxValue(long index){
  	// Creta e aconfiguration with the variables of this potential
  	Configuration conf=new Configuration(variables);
  	
  	// Jump to the wished set of values
  	conf.goToConfiguration(index);
  	
  	// Get the minValue for this configuration
  	return(getMaxValue(conf));
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
    PotentialIntervalTable pot;
    int i;
    
    // Creates a configuration preserving the values in conf.
    auxConf = new Configuration(variables,conf);
    
    // Computes the list of variables of the new Potential.
    aux = new Vector();
    for (i=0 ; i<variables.size() ; i++) {
      temp = (FiniteStates)variables.elementAt(i);
      if (conf.indexOf(temp) == -1)
        aux.addElement(temp);
    }
    
    pot = new PotentialIntervalTable(aux);
    
    for (i=0 ; i<pot.minValues.length ; i++) {
      pot.minValues[i] = getMinValue(auxConf);
      pot.maxValues[i] = getMaxValue(auxConf);
      auxConf.nextConfiguration(conf);
    }
    
    return pot;
  }
  
  /**
   * Normalizes the values of this potential.
   * The object is modified.
   */
  
  public void normalize() {
    int i;
    double s;
    
    s = totalPotential();
    
    for (i=0 ; i<minValues.length ; i++){
      minValues[i] /= s;
      maxValues[i] /= s;
    }
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
    int i, total;
    Configuration conf;
    
    p.print("values = table-interval ( \n");
    
    total = (int)FiniteStates.getSize(variables);
    
    conf = new Configuration(variables);
    
    for (i=0 ; i<total ; i++) {
      p.print("                ");
      conf.save(p);
      p.print(" = ["+minValues[i]+","+maxValues[i]+"] ,\n");
      conf.nextConfiguration();
    }
    p.print("                );\n");
  }
  
  /**
   * Saves a potential to a file. This one must be used when
   * saving a network. The values are written as a table.
   * @param p the <code>PrintWriter</code> where the potential will be written.
   */
  
  public void save(PrintWriter p) {
    
    int i, total;
    
    p.print("values= table-interval (");
    
    total = (int)FiniteStates.getSize(variables);
    
    for (i=0 ; i<total ; i++) {
      p.println("("+minValues[i]+", "+maxValues[i]+")");
    }
    p.print(");\n");
  }
  
  /**
   * Removes the argument variable summing over all its values.
   * @param var a <code>Node</code> variable to be removed.
   * @return a new <code>PotentialTable</code> with the result of the deletion.
   */
  
  public Potential addVariable(Node var) {
    System.out.println("Error in PotentialIntervalTable.addVariable(Node): Method not implemented for PotentialIntervalTable");
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
  
  public PotentialIntervalTable addVariable(Vector vars) {
    System.out.println("Error in PotentialIntervalTable.addVariable(Vector): Method not implemented for PotentialIntervalTable");
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
    System.out.println("Error in PotentialIntervalTable.combine(Potential): Method not implemented for PotentialIntervalTable");
    System.exit(11);
    return null;
  }
  
  /**
   * Method to assign teh values of a default interval
   * @param defaults Vector with the default interval
   */
  
  public void setDefaultValues(Vector defaults){
    double min=((Double)defaults.elementAt(0)).doubleValue();
    double max=((Double)defaults.elementAt(1)).doubleValue();
    int i, total;
    
    total = (int)FiniteStates.getSize(variables);
    
    for (i=0 ; i<total ; i++) {
      if (minValues[i] == -1){
        if (maxValues[i] == -1){
          minValues[i]=min;
          maxValues[i]=max;
        }
        else{
          System.out.println("Error in PotentialIntervalTable.setDefaultValues: min value fixed and max value not fixed");
          System.exit(0);
        }
      }
    }
  }
}
