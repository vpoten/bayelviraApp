// Function SumNormIdf giving the normalized Idf

package elvira.potential;

import java.util.Vector;
import elvira.*;

/**
 * SumNormIdf Function:
 * This function calculates the relevance probability of a document (d) given a
 * set of terms (t1, t2, ....,tn), i.e., P(d|t1,...,tn)
 * The arguments of this function are:
 * <ol>
 * <li> Number of documents
 * <li> Number of terms in the actual document
 * <li> A set of Idf values (one for each term belonging to the document)
 * </ol>
 * @author Juan F. Huete
 * @since 13/9/2000
 */

public class FunctionSumNormIdf extends Function {
  
/**
 * Constructor: Creating a instance of the function SumNormIdf.
 */

public FunctionSumNormIdf() {
  
  name = new String("SumNormIdf");
  tp = 5;
}


/**
 * Translate SumNormIdf Function to AddNormIdf (which includes NormIdf functions	
 * as arguments).
 * @return PotentialFunction AddNormIdf, given the same output
 */
	
public static PotentialFunction sumToAddNormIdf(PotentialFunction pot) {
  
  PotentialFunction addIdf;
  PotentialFunction normIdf;
  Vector vars, varaux, arg;
  int i, tama;
  
  
  addIdf = new PotentialFunction( pot.getVariables());
  addIdf.setFunction("AddNormIdf");
  
  vars = pot.getVariables();
  tama = pot.getVariables().size();
  arg = pot.getArguments();
  for (i=1 ; i< tama ;i++) {
    varaux = new Vector();
    varaux.addElement(vars.elementAt(0));
    varaux.addElement(vars.elementAt(i));
    normIdf = new PotentialFunction(varaux);
    
    normIdf.setFunction("NormIdf");
    
    normIdf.addArgument(((Double) arg.elementAt(0)).doubleValue());
    normIdf.addArgument(((Double) arg.elementAt(1)).doubleValue());
    normIdf.addArgument(((Double) arg.elementAt(i+1)).doubleValue());

    addIdf.addArgument(normIdf);
  }
  return addIdf;	 		
}


/**
 * This method has not been implemented
 */

public Potential restrictFunctionToVariable(PotentialFunction inputPot,
					    Configuration conf) {
  
  PotentialFunction pf = new PotentialFunction();
  return pf;
}


/**
 * Evaluate this function
 * @param arg The set of arguments used in this function
 * @param conf the configuration to be evaluated
 * @return  the function value
 */

public double PotValue(double arg[], Configuration conf) {
  
  int i, tama;
  double v = 0.0;
  tama = conf.size();
  double aux = 0;
  double auxPDT, prDocTer = 0.0;
  for (i=1 ; i<tama ; i++) {
    if ( conf.getValue(i) == 1) {
      aux = (arg[i+1]/Math.log(arg[0]));
      auxPDT = aux;
    }
    else auxPDT = 1.0/arg[0];
    
    if (conf.getValue(0) == 0)
      auxPDT = 1-auxPDT;
    if (conf.getValue(0) == 1) // Calculating document relevance probability
      prDocTer = prDocTer + (1-prDocTer) * auxPDT;
    else
      prDocTer = prDocTer + (1-prDocTer)*(1-auxPDT);	
  }
  if (conf.getValue(0) == 1)
    return prDocTer;
  else
    return 1.0-prDocTer;
}


/**
 * Marginalizes over a set of variables
 *  In this particular case, we return a probability tree with the value 1
 * stored in the root node
 * @param vars,  a vector of variables
 * @return a Potential with  the marginalization over vars
 */

public Potential marginalizeFunctionPotential(Vector vars) {
  
  PotentialTree p = new PotentialTree(vars);
  ProbabilityTree t = new ProbabilityTree(1);
  
  p.setTree(t);
  return p;
}


/**
 * Removes the argument variable suming over all its values
 * @param potVar the variables in the original potential
 * @param var - a FiniteStates variable
 * @return a new Potential with the result of the deletion
 */

public Potential functionAddVariable(Vector potVar, Vector vars) {
  
  Vector aux;
  FiniteStates temp, varaux;
  int i;
  PotentialTree pot;
  ProbabilityTree T = new ProbabilityTree(1.0);
  
  varaux = new FiniteStates();
  aux = new Vector();
  
  // Creates the list of variables of the new potential.
  for (i=0 ; i<potVar.size() ; i++) {
    temp = (FiniteStates)potVar.elementAt(i);
    if (vars.indexOf(temp) == -1)
      aux.addElement(temp);
    else
      varaux = temp;
  }
  
  pot = new PotentialTree(aux);
  
  pot.setTree(T);

  return pot;
}

} // End of class