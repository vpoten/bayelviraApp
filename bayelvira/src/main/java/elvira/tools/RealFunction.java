/* RealFunction.java */

package elvira.tools;


import java.io.*;



/**
 * Implements a univariate real function .
 *
 *
 * @author Rafael Rumi
 * @since 14/01/2002
 */

public class RealFunction {

/**
 * Lower limit of the interval where the function is defined
 */
double lowerLimit;

/**
 * Upper limit of the interval where the function is defined
 */
double upperLimit;


/**
 * Creates a function with the given domain
 * @param x The lower limit of the domain
 * @param y The upper limit of the domain
 */

public RealFunction(double x, double y) {
  
  lowerLimit = x;
  upperLimit = y;
}

  
/**
 * Sets the upper limit of the domain
 * @param x The upper limit of the domain
 */

public void setUpperLimit(double x) {

  upperLimit = x;
}


/**
 * Sets the lower limit of the domain
 * @param x The lower limit of the domain
 */

public void setLowerLimit(double x) {

  lowerLimit = x;
}


/**
 * Gets the upper limit of the domain
 *
 *@return The upper limit of the domain
 */

public double getUpperLimit() {

  return upperLimit;
}


/**
 * Gets the lower limit of the domain
 *
 *@return The lower limit of the domain
 */

public double getLowerLimit() {

  return lowerLimit;
}


/**
 * Returns the value of the function for a given point. 
 * This method must be defined by the subclasses
 *
 * @param x The point to evaluate
 * @return The value of the function for x. In this case,
 * it always returns -1
 */


public double getValue(double x) {

  return -1;
}


} // End of class
