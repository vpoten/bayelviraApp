/* SimulationProp */

package elvira.inference.approximate;

import java.io.*;
import elvira.*;
import elvira.inference.Propagation;
import elvira.potential.*;

/**
 * Class SimulationProp. General class for simulation
 * algorithms.
 *
 * @since 21/07/2000
 */

public class SimulationProp extends Propagation {

/**
 * The sample size, i.e. the number of configurations that will be
 * used to obtain the estimated probabilities. It must be set when
 * creating a new propagation.
 */
int sampleSize;

/**
 * The relations contained in the network before any propagation
 * is carried out.
 */
RelationList initialRelations;


/* METHODS */


/**
 * Sets the sample size.
 * @param n the new sample size.
 */

public void setSampleSize(int n) {
  
  sampleSize = n;
}


/**
 * Gets the sample size.
 * @return the sample size.
 */

public int getSampleSize() {
  
  return sampleSize;
}


/**
 * Normalizes the results of a simulation.
 */

public void normalizeResults() {

  Potential pot;
  int i;

  for (i=0 ; i<results.size() ; i++) {
    pot = (Potential)results.elementAt(i);
    pot.normalize();
  }
}


/**
 * Saves the result of a propagation to a file. It only works
 * if the results are stored as potentials of class
 * <code>PotentialTable</code>.
 * @param fileName a <code>String</code> containing the file name.
 */

public void saveResults(String fileName) throws IOException {

  FileWriter f;
  PrintWriter p;
  PotentialTable pot;
  int i;

  f = new FileWriter(fileName);
  
  p = new PrintWriter(f);
  
  for (i=0 ; i<results.size() ; i++) {
    pot = (PotentialTable)results.elementAt(i);
    pot.saveResult(p);
  }
  
  f.close();
}

} // END OF CLASS