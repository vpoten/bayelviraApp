// Function NormIdf giving the normalized Idf

package elvira.potential;

import java.util.Vector;
import elvira.*;

/**
 * NormIdf Function:
 * This fucntion calculates the probability that a given document d is relevant
 * givent the term t, i.e., P(d|t).
 * where P(d|t) = { Idf_t / log(n_doc) if the term t is relevant
 *                { 1 / n_doc          if the term is not relevant
 * The arguments are:
 * <ol>
 * <li> n_doc a float representing the number of documents in the collection
 * <li> n_term_in_d a float representing the number of terms in document d
 * <li> Idf a float representing the Idf of the term t
 * </ol>
 * @author Juan F. Huete
 * @since 13/9/2000
 */

public class FunctionNormIdf  extends Function {

/**
 * Constructor
 */

public FunctionNormIdf() {
  
  name = new String("NormIdf");
  tp = 7;
}


/**
 * PotValue evaluates the potential function for a given configuration
 * Function AddNormIdf
 * @param arg a set of values, where arg[i] represents the arguments used by
 * this function
 * @param conf the configuration that we are evaluating
 * @return P(d|t)
 */

public double PotValue(double arg[], Configuration conf) {
  
  int i;
  int tama = conf.size();
  double aux, prDocTer = 0.0;
  
  FiniteStates tmp;
  System.out.println("En function NORM");
  
  for (i=0 ; i<conf.getVariables().size() ; i++) {
    tmp = (FiniteStates)conf.getVariable(i);
    System.out.print("  "+tmp.getName() );
  }
  conf.print();
  for (i=0 ; i<tama ; i++)
    System.out.println(" arg "+arg[i]);
  	
  if ((int) conf.getValue(1) == 1) {
    aux = (arg[2]/Math.log(arg[0]));
    prDocTer = aux ;
  }
  else
    prDocTer = 1.0/Math.log(arg[0]);
  if (conf.getValue(0) == 1)
    return prDocTer;
  else
    return 1.0-prDocTer;
}


/**
 * This fuction restrict the potential, given that we know the values of a variable
 * @param inputPot the potential to restrict
 * @param conf a Configuration
 * @return a  PotentialTable
 */

public Potential restrictFunctionToVariable(PotentialFunction inputPot,
					    Configuration conf) {

  Configuration auxConf;
  Vector aux;
  PotentialTable pot;
  int i, j;
  FiniteStates temp, tmp;
  double argAux[] = new double[inputPot.getArguments().size()];

  aux = new Vector();
  
  System.out.println("En restric Function NornIdf");
  
  for (i=0 ; i<conf.getVariables().size() ; i++) {
    tmp = (FiniteStates)conf.getVariable(i);
    System.out.print("  "+tmp.getName() );
  }
  conf.print();
  
  for (i=0 ; i<inputPot.getVariables().size() ; i++) {
    temp = (FiniteStates)inputPot.getVariables().elementAt(i);
    if (conf.indexOf(temp) == -1)
      aux.addElement(temp);
  }
  
  pot = new PotentialTable(aux);
  
  for (i=0 ; i<argAux.length ; i++) {
    argAux[i]= ( (Double) inputPot.getArguments().elementAt(i) ).doubleValue();
  }
  
  auxConf = new Configuration(inputPot.getVariables(),conf);
  
  for (i=0 ; i<pot.getValues().length ; i++) {
    pot.setValue (i, inputPot.getFunction().PotValue(argAux, auxConf));
    auxConf.nextConfiguration(conf);
  }

  return pot;
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
 *
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
    else varaux = temp;
  }
  
  pot = new PotentialTree(aux);
  
  pot.setTree(T);

  return pot;
}

} // End of class