/* ImportanceSampling.java */

package elvira.inference.approximate;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.potential.*;

/**
 * Class ImportanceSampling.
 * Implements the common aspects of importance sampling methods
 * of propagation based on approximate node deletion.
 *
 * @since 21/7/2000
 */


public class ImportanceSampling extends SimulationProp {
   
/**
 * Maximum size of a potential. The size is the number of values
 * of the potential.
 */
int limitSize;        

/**
 * Current weight of the simulated configuration. After a full
 * configuration is obtained, this field contains the ratio between
 * the exact probability and the sampling probability of the simulated
 * configuration.
 */
double currentWeight; 

/**
 * This field contains the addition of the weights of the
 * simulated configurations. Used to compute the variance of the weights.
 */
double sumW;

/**
 * This field contains the addition of the squared weights of the
 * simulated configurations. 
 * Used to compute the variance of the weights.
 */
double sumW2;

/**
 * Deletion sequence of the variables.
 */
NodeList deletionSequence;

/**
 * The sampling distributions. There is a potential for each variable
 * to simulate, in the same order as
 * in <code>NodeList deletionSequence</code>.
 */
Vector samplingDistributions;

/**
 * The current configuration being simulated. The positions of the
 * variables in this configuration is stored in the instance
 * variable <code>positions</code> (a hash table). At each position, the
 * value of the corresponding variable is stored as an <code>int</code>.
 */                     
int[] currentConf;

/**
 * Number of repetitions of the experiment.
 */
int numberOfRuns;


/* METHODS */


/**
 * Sets the number of repetitions (runs) of the experiment.
 * @param n the number of runs.
 */

public void setNumberOfRuns(int n) {
  
  numberOfRuns = n;
}


/**
 * Sets the maximum size of a potential within a propagation.
 * @param n the size.
 */

public void setLimitSize(int n) {
  
  limitSize = n;
}


/**
 * Initialises the vector containing the simulation information
 * (<code>Vector results</code>).
 * For each variable there will be a PotentialTable  with
 * all its values set to 0.
 */

public void initSimulationInformation() {
 
  PotentialTable pot;
  FiniteStates var;
  int i;
  
  results = new Vector();
  
  for (i=samplingDistributions.size()-1 ; i>=0 ; i--) {
    var = (FiniteStates)deletionSequence.elementAt(i);
    pot = new PotentialTable(var);
    results.addElement(pot);
  }
  
  // Now prepare the instance variable to compute the variance of weights.
  sumW = 0.0;
  sumW2 = 0.0;
}


/**
 * Reset to zero the results of the propagation. Used when starting
 * a new run ina sequence of experiments.
 */

public void clearSimulationInformation() {
  
  int i;
  PotentialTable pot;
  
  for (i=0 ; i<results.size() ; i++) {
    pot = (PotentialTable)results.elementAt(i);
    pot.setValue(0.0);
  }
}


/**
 * Updates the simulation information according to the
 * current weight and the argument configuration. The value of the
 * posterior probability of each pair (variable,value) in the given
 * configuration is increased by the weight of the configuration.
 * The final posterior probability will be the normalized sum of
 * weights.
 * @param conf the simulated configuration.
 */

public void updateSimulationInformation(Configuration conf) {
  
  int i, s, v;
  PotentialTable pot;
  
  s = results.size();
  
  for (i=0 ; i<s ; i++) {
    pot = (PotentialTable)results.elementAt(i);
    v = conf.getValue(i);
    pot.incValue(v,currentWeight);
  }
}


/**
 * Updates the simulation information according to the
 * current weight and configuration <code>currentConf</code>.
 * The value of the
 * posterior probability of each pair (variable,value) in the given
 * configuration is increased by the weight of the configuration.
 * The final posterior probability will be the normalized sum of
 * weights.
 */

public void updateSimulationInformation() {
  
  int i, s, v;
  PotentialTable pot;
  
  s = results.size();
  
  for (i=0 ; i<s ; i++) {
    pot = (PotentialTable)results.elementAt(i);
    v = currentConf[i];
    pot.incValue(v,currentWeight);
  }
  
  // The following is used to compute the variance of the weights.
  sumW += currentWeight;
  sumW2 += (currentWeight * currentWeight);
}


/**
 * Prints the value of <code>Vector currentConf</code> in a line
 * of the standard output.
 */

public void printCurrentConf() {
 
  int i;
  
  for (i=0 ; i<currentConf.length ; i++)
    System.out.print(currentConf[i]);
  
  System.out.println(" ");
}


/**
 * Computes the variance of the weights of the configuratons obtained
 * during a simulation.
 *
 * @return a <code>double</code> corresponding to the variance.
 */

public double varianceOfWeights() {
 
  double d, variance, s;
  
  d = sampleSize * numberOfRuns;
  s = sumW / d;
  variance = (sumW2 / (d * s * s)) - 1;

  return variance;
}
 
} // End of class