/* Prueba del fichero PotFunciones.java */



package elvira.potential;



import java.util.Vector;

import elvira.*;





/**
 * Abstract class <code>Function</code>: Specifies those methods wich
 * are common to all kind of functions, particularly when these functions
 * are considered as
<code>PotentialFunction</code>.

 * The basic structure includes the name of the function and an integer

 * representing its code number.
 * @see PotentialFunction

 * @since 13/9/2000

 * @author Juan F. Huete.

 * @version 1.0

 */




public abstract class Function {


/**
 * Code number.
 */
int  tp;

/**
 * Function name.
 */
String name;

/**
 * Restricts a potential specified by a function to a given configuration
 * of variables.
 * @param inputPot the potential to restrict.
 * @param conf the configuration to which <code>inputPot</code> will be
 * restricted.
 * @return the restricted <code>Potential</code>.
 */

abstract Potential restrictFunctionToVariable(PotentialFunction inputPot,
					      Configuration conf);



/**
 * Marginalizes over a set of variables.
 * @param vars  a <code>Vector</code> of variables.
 * @return a Potential with  the marginalization over <code>vars</code>.

 */


abstract Potential marginalizeFunctionPotential(Vector vars);


/**
 * Removes the argument variable suming over all its values.
 * @param potVar the set of variables in the original Potential.
 * @param var a <code>FiniteStates</code> variable.
 * @return a new Potential with the result of the deletion.
 */

abstract Potential  functionAddVariable(Vector potVar, Vector vars);



/**
 * Evaluates the function for a given configuration. In order to evaluate

 * this function we will use the set of arguments (arg).
 * @param conf the configuration to evaluate the function.
 * @param arg[] the arguments of the function.
 * @return the value of this function for <code>conf</code>.
 */


abstract double PotValue(double arg[], Configuration conf);


/**
 * Gets the type (code= of the function.
 * @return type of this function.
 */

public int getType() {

  
  return tp;
}


/**
 * Gets the name of the function.
 * @return the name of this function.
 */

public String getName() {
  
  return name;
}

} // End of class