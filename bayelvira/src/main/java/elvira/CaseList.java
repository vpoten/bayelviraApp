/* CaseList.java */

package elvira;

import java.util.Vector;
import elvira.potential.Potential;
import elvira.Configuration;


/**
 * This class implements a list of cases.
 * 
 * @since 20/9/2000
 */

public abstract class CaseList extends Potential{

static final long serialVersionUID = 6156295599428723316L;    
    
/**
 * Variable indicating the number of cases.
 */
private int numberOfCases;


/**
 * Gets the number of instances from the list of cases for a given
 * configuration of variables.
 * @param conf a configuration of variables.
 * @return the value of the potetial for conf.
 */

public abstract double getValue(Configuration conf);


/**
 * This method return the value in the pos (i,j) from the
 * matrix of cases for the variables.
 * @param i, index of the case
 * @param j, index of the variable
 */
public abstract double getValue(int i, int j);

/**
 * VERY IMPORTANT!!!!! This method IS NOT APPLICABLE in
 * <code>CaseList</code>.
 * @param conf a configuration of variables.
 * @param val the value for conf.
 */

abstract public void setValue(Configuration conf, double val);


/**
 * Gets the size of the list of cases.
 * @return the size of array in memory mode.
 */

public long getSize() {
  
  return (numberOfCases*getVariables().size());
}


/**
 * Gets the sum of all the values of a potential. In this case is the same
 * as the number of cases in case list.
 * @return the number of cases in case list.
 */

public double totalPotential() {
  
  return ((double)numberOfCases);
}


/**
 * Gets the sum of the instances of a case list
 * restricted to a configuration.
 * @param conf a configuration of variables.
 * @return the sum of the instances of a case list
 * restricted to configuration conf.
 */

abstract public double totalPotential(Configuration conf);


/**
 * Computes the entropy of a case list.
 * @return the sum of the values x Log x stored in the case list.
 */

abstract public double entropyPotential();


/**
 * Computes the entropy of a case list restricted to
 * a given configuration.
 * @param conf the configuration.
 * @return the sum of the values x Log x fixing configuration conf.
 */

abstract public double entropyPotential(Configuration conf);


/**** acces methods ***********/


/**
 * Gets the number of cases.
 * @return the number of cases.
 */

public int getNumberOfCases() {
  
  return numberOfCases;
}


/**
 * Sets the number of cases.
 * @param casesNumber the number of cases.
 */

public void setNumberOfCases(int casesNumber) {
  
  numberOfCases = casesNumber;
}


/******************************/


/**
 * This method stores a case configuration in the case list.
 * @param Configuration conf the configuration to put in the list.
 * @return <code>true</code> if the operation is succesful.
 */

abstract public boolean put(Configuration conf);


/**
 * This method gets a case configuration from a given position in the
 * case list.
 * @param pos the position of the configuration to retrieve..
 * @return the configuration in position <code>pos</code>. This
 * configuration is null if the operation is not successful.
 */

abstract public Configuration get(int pos);


/**
 *
 */

abstract public Configuration get(int pos, int[] indexOfVars);

	
/**
 * Marginalizes over a set of variables. It is equivalent
 * to <code>addVariable</code> over the other variables.
 * @param vars a vector of variables.
 * @return a <code>Potential</code> with the marginalization over vars.    
 */

public Potential marginalizePotential(Vector vars) {
  
  System.out.println("marginalizePotential is not implemented in CaseList");
  return null;	
}

}  // End of class
