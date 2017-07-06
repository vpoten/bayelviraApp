/*
 * ProductFunction.java
 *
 * Created on 9 de enero de 2004, 18:05
 */

package elvira.potential;
import java.util.Vector;
import elvira.*;

/**
 *
 * @author  Manolo
 */
public class ProductFunction extends elvira.potential.Function {
    
    /** Creates a new instance of ProductFunction */
    public ProductFunction() {
        name="Product";
   }
    
    /** Evaluates the function for a given configuration. In order to evaluate
     *
     * this function we will use the set of arguments (arg).
     * @param conf the configuration to evaluate the function.
     * @param arg[] the arguments of the function.
     * @return the value of this function for <code>conf</code>.
     *
     */
    double PotValue(double[] arg, Configuration conf) {
          /**@todo: implement this elvira.potential.Function abstract method*/
    //System.out.println("Error: PotValue not implemented in ProductFunction!!!!");
    	  double value = 1.0;
    	    
    	    for (int i=0;i<arg.length;i++){
    	    	value = value * arg[i];
    	    }
    	    	
    	    return value;
    }
    
    /** Removes the argument variable suming over all its values.
     * @param potVar the set of variables in the original Potential.
     * @param var a <code>FiniteStates</code> variable.
     * @return a new Potential with the result of the deletion.
     *
     */
    Potential functionAddVariable(Vector potVar, Vector vars) {
          /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: functionAddVariable not implemented in ProductFunction!!!!");
    return (Potential) null;
    }
    
    /** Marginalizes over a set of variables.
     * @param vars  a <code>Vector</code> of variables.
     * @return a Potential with  the marginalization over <code>vars</code>.
     *
     *
     */
    Potential marginalizeFunctionPotential(Vector vars) {
         /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: marginalizeFunctionPotential not implemented in ProductFunction!!!!");
    return (Potential) null;
    }
    
    /** Restricts a potential specified by a function to a given configuration
     * of variables.
     * @param inputPot the potential to restrict.
     * @param conf the configuration to which <code>inputPot</code> will be
     * restricted.
     * @return the restricted <code>Potential</code>.
     *
     */
    Potential restrictFunctionToVariable(PotentialFunction inputPot, Configuration conf) {
           /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: restrictFunctionToVariable not implemented in ProductFunction!!!!");
    return (Potential) null;
    }
    
}
