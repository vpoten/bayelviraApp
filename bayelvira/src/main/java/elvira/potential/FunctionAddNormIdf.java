// Function AddNormIdf giving the normalized Idf
    
package elvira.potential;

import java.util.Vector;
import elvira.*;

/**
 * SumNormIdf Function:
 * This function calculates the relevance probability of a document (d) given a
 * set of relations  (d-t1, d-t2, ....,d-tn), i.e., the output is P(d|t1,...,tn)
 * Each relation d-ti represents the probability P(d|ti) given by any kind of
 * Potential (table, tree or function).
 * The arguments of this function are:
 * <ol>
 * <li> A set of relation names (one for each term belonging to the document).
 * This relations must be declarated as non active relation in the networl file
 * </ol>
 * @author Juan F. Huete
 * @since 13/9/2000
 */

public class FunctionAddNormIdf extends Function {

/**
 * Constructor.
 */

public FunctionAddNormIdf() {
  
  name = new String("AddNormIdf");
  tp = 8;
}


/**
 * PotValue evaluates the potential function for a given configuration.
 * @param arg a set of values, where arg[i] represents the probability value
 * of P(d|ti) evaluated with the Configuration conf.
 * @param conf the configuration that we are evaluating
 */


public double PotValue(double arg[], Configuration conf) {
  
  int i, siz;
  double v = 0.0;
  siz = arg.length;
  double aux = 0;
  double pdr = 0.0;  // the probability of relevance of the document
  double pdnr = 0.0; // the probability of not relevance of the document
  double okrel = 0.0, oknr = 0.0;
  FiniteStates tmp;
  
  System.out.println("En function ADD");
  for (i=0 ; i<conf.getVariables().size() ; i++) {
    tmp = (FiniteStates)conf.getVariable(i);
    System.out.print("  "+tmp.getName() );
  }
  conf.print();
  for (i=0 ; i<siz ; i++)
    System.out.println(" arg "+arg[i]);
	
  for (i=0 ; i<siz ; i++) {
    if (conf.getValue(i) == 0) { // the i-th term is not relevant
      if (conf.getValue(0) == 0) // the document is not relevant
	pdnr = pdnr +(1-pdnr)* arg[i];
      else
	pdnr = pdnr +(1-pdnr) * (1-arg[i]);
      oknr = 1.0;
    }
    else { // the i-th term is relevant
      if (conf.getValue(0) == 1) // the document is relevant
	pdr = pdr + (1-pdr)*arg[i];
      else
	pdr = pdr + (1-pdr)*(1-arg[i]);
      okrel = 1.0;
    }
  }
  
  
  if (conf.getValue(0) == 1)
    return   (pdr +(1-pdr)*(1-pdnr)*oknr);
  else
    return 1-  (pdr +(1-pdr)*(1-pdnr)*oknr);
}


/**
 * This fuction restricts the potential, given that we know the values of a
 * variable.
 * The restriction process has been done by, first restricting each
 * argument to the same configuration, and after that,  we create a new AddNormIdf
 * function including as arguments these restricted potentials.
 * @param inputPot the potential to restrict
 * @param conf a Configuration
 * @return a new PotentialFunction ( AddNormIdf )
 */

public Potential restrictFunctionToVariable(PotentialFunction inputPot,
					    Configuration conf) {
  
  int i;
  Potential potaux;
  Double d = new Double(0.0);
  PotentialFunction outputPot;
  FiniteStates temp,tmp;
  Vector aux;
  
  System.out.println("En restric Function AddNormIdf");
  
  
  for (i=0 ; i<conf.getVariables().size(); i++) {
    tmp = (FiniteStates)conf.getVariable(i);
    System.out.print("  "+tmp.getName() );
  }
  conf.print();

  aux = new Vector();
  for (i=0 ; i<inputPot.getVariables().size() ; i++) {
    temp = (FiniteStates)inputPot.getVariables().elementAt(i);
    if (conf.indexOf(temp) == -1)
      aux.addElement(temp);
  }
  
  outputPot = new PotentialFunction(aux);
  outputPot.setFunction( inputPot.getFunction());
  
  
  for (i=0 ; i<inputPot.arguments.size() ; i++) {
    outputPot.addArgument(inputPot.getStrArgument(i));
    if (inputPot.arguments.elementAt(i).getClass() != d.getClass() ) {
      potaux =  ((Potential)(inputPot.arguments.elementAt(i))).restrictVariable(conf);
      outputPot.setArgumentAt(potaux,i);
    }
    else {
      d = ((Double) inputPot.arguments.elementAt(i));
      outputPot.setArgumentAt(d ,i);
    }
  }
  
  return outputPot;
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
  * @param potVar the set of variables in the original potential
  * @param var - a FiniteStates variable
  * @return a new Potential with the result of the deletion
  */

public Potential  functionAddVariable(Vector potVar, Vector vars) {
  
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