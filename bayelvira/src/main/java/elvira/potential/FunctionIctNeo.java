// Function IctNeo 
 
package elvira.potential; 
 
import java.util.Vector; 
import java.lang.Math;
import elvira.*; 
 
/**
 * IctNeo Function: 
 * This function calculates the utility function 
 * @author Manuel Gomez
 * @since 13/9/2000
 */ 
 
public class FunctionIctNeo extends Function {

  double arguments[];
  int num_args=9;
  static int first_call=0;

/** 
 * Constructor: Creating a instance of the function IctNeo. 
 */ 
 
public FunctionIctNeo() {
 
  name = new String("IctNeo"); 
  tp = 5; 
}


/**
 * This method has not been implemented.
 */

public Potential restrictFunctionToVariable(PotentialFunction inputPot,
					    Configuration conf) {

  return null;
}


/**
 * Evaluates this function.
 * @param arg The set of arguments used in this function 
 *             The arguments are used to define the 
 *             evaluation function, only the first time
 * @param conf <code>configuration</code> Configuration to be evaluated 
 * @return  a double value 
 */

public double PotValue(double arg[], Configuration conf) {
 
 int i;
 double uy1,uy2,uy3,uy4;
 double utility;

  /*
   * The first time we add the global arguments that
   * define the function
   */ 

  if (first_call == 0) {
    /*
     * Call the function that stores the arguments
     */

    arguments=new double[num_args];

    for(i=0; i < num_args; i++) {
      arguments[i]=arg[i];
    }
    first_call=1;
  }

  /*
   * Evaluate u1, as a function of x1 (CEco)
   */

  uy1=evaluateUy1(conf.getValue(0));

  /*
   * Evaluate u2, as a function of x2 and x3 (CSoc and CEmo)
   */

  uy2=evaluateUy2(conf.getValue(1),conf.getValue(2));

  /*
   * Evaluate u3 as a function of x4 (RIng)
   */

  uy3=evaluateUy3(conf.getValue(3));

  /*
   * Evaluate u4 as a function of x5 and x6 (DTer and DHBrb)
   */

  uy4=evaluateUy4(conf.getValue(4),conf.getValue(5));

  /*
   * Aggregation of the individual values
   */ 

  utility=combineUtilities(uy1,uy2,uy3,uy4);
  return(utility);
}


/**
 * Marginalizes over a set of variables
 * In this particular case, we return a probability tree with the value 1
 * stored in the root node
 * @param vars,  a vector of variables
 * @return a Potential with  the marginalization over vars
 */

public Potential marginalizeFunctionPotential(Vector vars) {

  return null;
}


/**
 * Removes the argument variable suming over all its values
 * @param potVar the variables in the original potential
 * @param var - a FiniteStates variable
 * @return a new Potential with the result of the deletion
 */

public Potential functionAddVariable(Vector potVar, Vector vars) {

  return null;
}

/**
 * Prints the general arguments of the function. The arguments
 * are needed to generate the evaluation function for IctNeo
 */

public void printArguments() {
  int i;

  // Loop over the arguments

  for(i=0; i < num_args; i++) {
    System.out.println("Argumento " + i + " = " + arguments[i]);
  }
}

/**
 * Function to evaluate the utility respect economical
 * cost
 * @param The level of the economical cost
 * @return Calculated utility
 */

private double evaluateUy1(double x1)
{
   return(1.604-0.604*Math.exp(0.00077*(x1)*315));
}

/**
 * Function to evaluate the utility respect social and
 * emotional cost
 * @param The level of the social and emotional cost
 * @return Calculated utility
 */

private double evaluateUy2(double x2,double x3)
{
  double ux2,ux3;

   ux2=(-0.1108+1.111*Math.exp(-1.153*(x2)));
   ux3=(-0.225+1.225*Math.exp(-0.8473*(x3)));

   // Combine both values

   return(arguments[5]*ux2+arguments[6]*ux3);
}

/**
 * Function to evaluate the utility respect risk of being
 * admitted
 * @param The level of the risk
 * @return Calculated utility
 */

private double evaluateUy3(double x4)
{
   return(1.227-0.2766*Math.exp(0.5098*(x4)));
}


/**
 * Function to evaluate the utility respect injuries
 * (due to treatment and due to hiperbil.) 
 * @param The level of the injuries 
 * @return Calculated utility
 */

private double evaluateUy4(double x5,double x6)
{
  double ux5,ux6;

   ux5=(1.361-0.361*Math.exp(0.3316*(x5)));
   ux6=(1.408-0.4083*Math.exp(0.2476*(x6)));

   // Combine both values

   return(arguments[7]*ux5+arguments[8]*ux6);
}

/**
 * Function to combine the utilities 
 * @param The individual utilities 
 * @return Calculated utility
 */
private double combineUtilities(double uy1, double uy2, double uy3, double uy4)
{
  double utility;
  double k=arguments[0];
  double k1=arguments[1];
  double k2=arguments[2];
  double k3=arguments[3];
  double k4=arguments[4];
  double k1u1=k1*uy1;
  double k2u2=k2*uy2;
  double k3u3=k3*uy3;
  double k4u4=k4*uy4;

  /*
   * Term with single interaction
   */

  utility=k1u1+k2u2+k3u3+k4u4;

  /*
   * Term with two-two relations 
   */

  utility=utility+k*
          (k1u1*k2u2+k1u1*k3u3+k1u1*k4u4+k2u2*k3u3+k2u2*k4u4+k3u3*k4u4);

  /*
   * Term with three-three relations
   */

  utility=utility+k*k* 
          (k1u1*k2u2*k3u3 + k1u1*k2u2*k4u4 + k1u1*k3u3*k4u4 + k2u2*k3u3*k4u4);

  /*
   * Term with complete interaction
   */

  utility=utility+k*k*k*(k1u1*k2u2*k3u3*k4u4);

  return(utility);
}

} // End of class
