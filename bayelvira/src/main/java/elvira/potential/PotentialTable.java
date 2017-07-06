/* PotentialTable.java */

package elvira.potential;

import java.io.*;
import java.util.Random;
import java.util.Vector;
import java.util.Hashtable;
import java.text.*;

import elvira.*;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.sensitivityAnalysis.GeneralizedPotentialTable;



/**
 * Implements a <code>Potential</code> whose values are stored in
 * an array of <code>double</code>. The variables of the potentials
 * are of class <code>FiniteStates</code>.
 *
 * @since 26/7/2000
 */

public class PotentialTable extends Potential {

/**
 * The values of the potential.
 */
private double[] values;


/* CONSTRUCTORS */


/**
 * Constructs a new <code>PotentialTable</code> with an empty list
 * of variables and a single value (not initialized).
 */

public PotentialTable() {

  variables = new Vector();
  values = new double[1];
}


/**
 * Constructs a new <code>PotentialTable</code> with an empty list
 * of variables and a given number of values in the array.
 * @param numberOfValues the number of values in the array.
 */

public PotentialTable(int numberOfValues) {

  variables = new Vector();
  values = new double[numberOfValues];
}


/**
 * Constructs a new <code>PotentialTable</code> for a single variable
 * and sets the values to 0.
 * @param var a <code>FiniteStates</code> variable.
 */

public PotentialTable(FiniteStates var) {

  int i, nv;

  nv = var.getNumStates();
  variables = new Vector();
  variables.addElement(var);
  values = new double[nv];

  for (i=0 ; i<nv ; i++)
    values[i] = 0.0;
}


/**
 * Constructs a new <code>PotentialTable</code> for a list of
 * variables and creates an array to store the values.
 * @param vars a <code>Vector</code> of
 * variables (<code>FiniteStates</code>).
 */

public PotentialTable(Vector vars) {

  int nv;

  // Compute the size of the array.
  nv = (int)FiniteStates.getSize(vars);

  variables = (Vector)vars.clone();
  values = new double[nv];
}


/**
 * Constructs a new <code>PotentialTable</code> for a list of
 * variables and creates an array to store the values.
 * @param vars the list of variables as a <code>NodeList</code>.
 */

public PotentialTable(NodeList vars) {

  int nv;

  // Compute the size of the array.
  nv = (int)vars.getSize();

  variables = (Vector)vars.getNodes().clone();
  values = new double[nv];
}

/**
 * Constructs a <code>PotentialTable</code> from a
 * <code>PotentialTree</code>.
 * @param tree a <code>PotentialTree</code> to convert to table.
 */

public PotentialTable(Potential pot) {

  int i, nv;
  Configuration conf;

  variables = (Vector) pot.getVariables().clone();
  nv = (int)FiniteStates.getSize(variables); // Size of the array.
  conf = new Configuration(variables);
  values = new double[nv];

  // Evaluate the tree for each possible configuration.
  for (i=0 ; i<nv ; i++) {
    values[i] = pot.getValue(conf);
    conf.nextConfiguration();
  }
}

/**
 * Constructs a <code>PotentialTable</code> from a <code>NodeList</code>
 * and a <code>Relation</code> defined over a subset of variables of
 * the <code>NodeList</code> passed as parameter.
 * If the potential attached to the relation passed as parameter
 * is not defined over a subset of the variables in the list,
 * the method builds a unitary potential.
 *
 * @param vars the <code>NodeList</code> of variables for the
 * new potential.
 * @param rel the <code>Relation</code> defined over a subset of
 * <code>vars</code>.
 */

public PotentialTable(NodeList vars, Relation rel) {

  int i, nv, pos;
  Configuration conf, subConf;

  variables = (Vector)vars.toVector().clone();
  nv = (int)FiniteStates.getSize(variables); // Size of the array.
  values = new double[nv];

  // determining if pot.variables is a subset of vars

  if ( (rel.getVariables().kindOfInclusion(vars)).equals("subset") ) {
    conf = new Configuration(variables);

    for (i=0 ; i<nv ; i++)
      values[i] = 0.0;

    for (i=0 ; i<nv ; i++) {
      subConf = new Configuration(conf,rel.getVariables());
      pos = subConf.getIndexInTable();
      values[i] += ((PotentialTable)rel.getValues()).getValue(pos);
      conf.nextConfiguration();
    }
  }
  else{ // unitary potential
    for (i=0 ; i<nv ; i++)
      values[i] = 1.0;
  }
}


/**
 * Constructs a <code>PotentialTable</code> from a <code>NodeList</code>
 * and a <code>Potential</code> defined over a subset of variables of
 * the <code>NodeList</code> passed as parameter. The new potential
 * is built by extending the passed potential to the whole set of
 * variables represented by the <code>NodeList</code>
 * if pot.getVariables is not a subset of vars, then an unitary potential
 * is created
 *
 * @param vars the <code>NodeList</code> of variables for the
 * new potential.
 * @param pot the <code>PotentialTabe</code> defined over a subset of
 * <code>vars</code>.
 */

public PotentialTable(NodeList vars, PotentialTable pot) {

  int i, nv, pos;
  Configuration conf, subConf;
  NodeList nl;

  variables = (Vector)vars.toVector().clone();
  nv = (int)FiniteStates.getSize(variables); // Size of the array.
  values = new double[nv];

  // determining if pot.variables is a subset of vars

  nl = new NodeList(pot.getVariables());
  if ( nl.kindOfInclusion(vars).equals("subset") ) {
    conf = new Configuration(variables);

    for (i=0 ; i<nv ; i++) {
      subConf = new Configuration(conf,nl);
      pos = subConf.getIndexInTable();
      values[i] = pot.getValue(pos);
      //setValue(conf,pot.getValue(subConf));
      conf.nextConfiguration();
    }
  }
  else{ // unitary potential
    for (i=0 ; i<nv ; i++)
      values[i] = 1.0;
  }
}




/**
 * Constructs a new Conditional P(X|Z) <code>PotentialTable</code>
 * for a list of variables and creates an array to store the values
 * generated randomly.
 * @param generator. A random number generator, instance
 * of class <code>Random</code>.
 * @param vars a <code>NodeList</code> over which the new potential
 * will be defined.
 */

public PotentialTable(Random generator, NodeList nodes, int degreeOfExtreme) {

   int nv, i;
   double sum, r;
   Configuration conf = new Configuration(nodes);
   PotentialTable potmarg, pot;


   nv = (int)FiniteStates.getSize(nodes);

   variables = nodes.copy().toVector();
   values = new double[nv];

   sum = 0.0;
   for (i=0 ; i < nv  ; i++) {
     r = generator.nextDouble();
     values[conf.getIndexInTable()] = Math.pow(r, (float) degreeOfExtreme);
     conf.nextConfiguration();
   }

   normalize();
   if (variables.size() > 1) {
     potmarg = (PotentialTable)this.addVariable((FiniteStates)
						variables.elementAt(0));
     pot = (PotentialTable)this.divide(potmarg);
     values = pot.values;
   }
}



/* METHODS */


/**
 * Constructs a new <code>PotentialTable</code>
 * for a list of variables and creates an array to store the values
 * generated randomly (uniform distribution). This method is appropriate to generate
 * randomly non-negative utility tables.
 * @param generator. A random number generator, instance
 * of class <code>Random</code>.
 * @param nodes a <code>NodeList</code> over which the new potential
 * will be defined.
 * @param d Maximum value for the potential. The range of the potential is [0,d].
 */
public PotentialTable(Random generator, NodeList nodes, double d) {
	
	  int nv, i;
	   double r;
	   Configuration conf = new Configuration(nodes);
	

	   nv = (int)FiniteStates.getSize(nodes);

	   variables = nodes.copy().toVector();
	   values = new double[nv];

	   for (i=0 ; i < nv  ; i++) {
	     r = (generator.nextDouble())*d;
	     values[conf.getIndexInTable()] = r;
	     conf.nextConfiguration();
	   }
	   
}


/**
 * Gets the variables of the potential.
 * @return the <code>Vector</code> of variables (<code>FiniteStates</code>)
 * of the potential.
 */

public Vector getVariables() {

  return(variables);
}


/**
 * Gets the values of the potential.
 * @return an array of <code>double</code> with the values of the potential.
 */

public double[] getValues() {

  return values;
}


/**
 * Gets the value for a configuration of variables.
 * @param conf a <code>Configuration</code>.
 * @return the value of the potential for <code>Configuration conf</code>.
 */

public double getValue(Configuration conf) {

  int pos;
  Configuration aux;

  // Take a configuration from conf just for variables
  // in the potential.
  aux = new Configuration(variables,conf);
  pos = aux.getIndexInTable();

  return values[pos];
}


/**
 * Ges the value stored in a given position in the array of values.
 * @param index a position in the array of values.
 * @return the value at position <code>index</code> in the array.
 */

public double getValue(int index) {

  return values[index];
}


/**
 * Gets the value for a configuration of variables. In this case, the
 * configuration is represented by means of an array of int.
 * At each position, the value for certain variable is stored.
 * To know the position in the array corresponding to a given
 * variable, we use a hash table. In that hash table, the
 * position of every variable in the array is stored.
 *
 * @param positions a <code>Hashtable</code> with the positions of the
 * variables in <code>conf</code>.
 * @param conf an array of <code>int</code>.
 * @return the value corresponding to configuration <code>conf</code>.
 */

public double getValue(Hashtable positions, int[] conf) {

  Configuration auxConf;
  FiniteStates var;
  int i, p, s;

  auxConf = new Configuration();

  s = variables.size();

  for (i=0 ; i<s ; i++) {
    var = (FiniteStates)variables.elementAt(i);
    p = ((Integer)positions.get(var)).intValue();
    auxConf.insert(var,conf[p]);
  }

  p = auxConf.getIndexInTable();

  return values[p];
}


/**
 * Sets the values of the potential.
 * @param v the values, as an array of <code>double</code>.
 */

public void setValues (double[] v) {

  values = v;
}


/**
 * Sets the value in a position in the array of values.
 * @param index the position in the array to modify.
 * @param value the value to store in that position.
 */

public void setValue(int index, double value) {

  values[index] = value;
}


/**
 * Sets the value for a configuration of variables.
 * @param conf a <code>Configuration</code> of variables.
 * @param value the new value for <code>Configuration conf</code>.
 */

public void setValue(Configuration conf, double value) {
  int index;
  Configuration aux;

  aux = new Configuration(variables,conf);
  index = aux.getIndexInTable();
  values[index] = value;
}


/**
 * Sets all the values in a potential to a given value.
 * @param value a real value.
 */

public void setValue(double value) {

  int index;

  for (index=0 ; index<values.length ; index++)
    values[index] = value;
}


/**
 * Increments the i-th value of the potential.
 * @param i the value to increment.
 * @param increment the increment.
 */

public void incValue(int i, double increment) {

  values[i] += increment;
}

/**
 * Increments all the values of a potencial.
 * @param i the value to increment.
 * @param increment the increment.
 */

public void incValue(double increment) {
int index;

  for (index=0 ; index<values.length ; index++)
  values[index] += increment;
}


/**
 * Takes the absolute value of all the values of a potencial.
 * @param i the value to increment.
 * @param increment the increment.
 */

public void changeToAbsoluteValue() {
int index;

  for (index=0 ; index<values.length ; index++){
  values[index] = Math.abs(values[index]);
  }
}

/**
 * Increments all the values of a potencial
 * dividing increment among all the values.
 * The difference with incValue is that 
 * in that procedure increment was added
 * to all the values, and in this procedure
 * increment is dividing among all the values.
 * @param i the value to increment.
 * @param increment the increment.
 */

public void incValueTotal(double increment) {
int index;

  increment = increment/((double) values.length);
  for (index=0 ; index<values.length ; index++)
  values[index] += increment;
}

/**
 * Gets the size (number of values) of the potential.
 * @return the size of the potential (number of values).
 */

public long getSize() {

  return values.length;
}


/**
 * Saves a potential to a file. This one must be used when
 * saving a network.
 * @param p the <code>PrintWriter</code> where the potential will be written.
 */

public void saveAsConfig(PrintWriter p) {

  int i, total;
  Configuration conf;

  p.print("values= table ( \n");

  total = (int)FiniteStates.getSize(variables);

  conf = new Configuration(variables);

  for (i=0 ; i<total ; i++) {
    p.print("                ");
    conf.save(p);
    p.print(" = "+values[i]+",\n");
    conf.nextConfiguration();
  }
  p.print("                );\n");
}


/**
 * Saves a potential to a file. This one must be used when
 * saving a network. The values are written as a table.
 * @param p the <code>PrintWriter</code> where the potential will be written.
 */

public void save(PrintWriter p) {

  int i, total;

  p.print("values= table (");

  total = (int)FiniteStates.getSize(variables);

  for (i=0 ; i<total ; i++) {
    p.print(values[i]+" ");
  }
  p.print(");\n");
}


/**
 * Saves a potential to a file. This one must be used to
 * save the results of a propagation, not to save a network.
 * It includes the name of the variables.
 * @param p the <code>PrintWriter</code> where the potential will be written.
 */

public void saveResult(PrintWriter p) {

  int i, total;
  Configuration conf;
  PotentialTable pot = (PotentialTable) this.copy();


  //List the variables
  for(i=0; i<pot.getVariables().size();i++){
    p.println("node "+((FiniteStates)pot.getVariables().elementAt(i)).getName());
  }

  //Prints the table
  pot.saveAsConfig(p);
}

public void saveMaxResult(PrintWriter p) {

  int i, total;
  Configuration conf,auxconf=new Configuration();

  for(i=0;i<variables.size();i++){
    p.print(((FiniteStates)variables.elementAt(i)).getName()+" = ");
    conf=getMaxConfiguration(auxconf);
    p.print((String)
		     ((FiniteStates)conf.getVariables().elementAt(i)).
		     getPrintableState(((Integer)conf.getValues().elementAt(i)).intValue())+" ");


  }

}



/**
 * Prints to the standard output the result of a propagation,
 * formatting the float numbers.
 */

public void showResult() {

  int i;
  NumberFormat df = new DecimalFormat("0.00");

  for (i=0 ; i<variables.size() ; i++) {
    System.out.print("node "+((FiniteStates)variables.elementAt(i)).getName());
    for (int v=0 ; v<getValues().length ; v++)
      System.out.print(df.format(values[v])+" ");
    System.out.println();
  }
}


/**
 * Prints this potential to the standard output.
 */

public void print() {
  super.print();

  int i, total;
  Configuration conf;

  System.out.print("values= table ( \n");
  total = (int)FiniteStates.getSize(variables);
  conf = new Configuration(variables);
  for (i=0 ; i<total ; i++) {
    System.out.print("                ");
    conf.print();
    System.out.print(" = "+values[i]+",\n");
    conf.nextConfiguration();
  }
  System.out.print("                );\n");
}


/**
 * Prints this potential to the standard output, with  tab factor (number of blank spaces before a child
     * is written)
 */

public void print(int j) {
  

  int i,l, total;
  Configuration conf;
  
  
  for (l=1 ; l<=j ; l++)
                    System.out.print(" ");
  System.out.print(" nodes ");
  for (i=0 ; i<variables.size() ; i++) {
    System.out.print(" "+((FiniteStates)variables.elementAt(i)).getName());}
  System.out.print("\n");
   for (l=1 ; l<=j ; l++)
                    System.out.print(" ");
  System.out.print("values= table ( \n");
  total = (int)FiniteStates.getSize(variables);
  conf = new Configuration(variables);
  for (i=0 ; i<total ; i++) {
      for (l=1 ; l<=j ; l++)
                    System.out.print(" ");
    System.out.print("                ");
    conf.print();
    System.out.print(" = "+values[i]+",\n");
    conf.nextConfiguration();
  }
  for (l=1 ; l<=j ; l++)
                    System.out.print(" ");
  System.out.print("                );\n");
}


/**
 * Restricts the potential to a configuration of variables.
 * @param conf a <code>Configuration</code>.
 * @return A new <code>PotentialTable</code> result of the restriction
 * of this to <code>conf</code>.
*/

public Potential restrictVariable(Configuration conf) {

  Configuration auxConf;
  Vector aux;
  FiniteStates temp;
  PotentialTable pot;
  int i;

  // Creates a configuration preserving the values in conf.
  auxConf = new Configuration(variables,conf);

  // Computes the list of variables of the new Potential.
  aux = new Vector();
  for (i=0 ; i<variables.size() ; i++) {
    temp = (FiniteStates)variables.elementAt(i);
    if (conf.indexOf(temp) == -1)
      aux.addElement(temp);
  }

  pot = new PotentialTable(aux);

  for (i=0 ; i<pot.values.length ; i++) {
    pot.values[i] = getValue(auxConf);
    auxConf.nextConfiguration(conf);
  }

  return pot;
}


/**
 * Sums over all the values of the variables in a list.
 * @param vars a <code>Vector</code> containing variables
 * (<code>FiniteStates</code>).
 * @return a new <code>PotentialTable</code> with
 * the variables in <code>vars</code> removed.
 */

public PotentialTable addVariable(Vector vars) {

  Vector aux;
  FiniteStates temp;
  int i, pos;
  Configuration auxConf1, auxConf2;
  PotentialTable pot;

  aux = new Vector();

  // Creates the list of variables of the new potential.
  for (i=0 ; i<variables.size() ; i++) {
    temp = (FiniteStates)variables.elementAt(i);
    if (vars.indexOf(temp) == -1)
      aux.addElement(temp);
  }

  // Creates the new potential and sets the values to 0.0
  pot=new PotentialTable(aux);

  for (i=0 ; i<pot.values.length ; i++)
    pot.values[i]=0.0;

  // Now for each configuration of the old potential, take
  // its value and see with which configuration of the new
  // one it corresponds. Then increment the value of the
  // new potential for that configuration.

  auxConf1 = new Configuration(variables);

  for (i=0 ; i<values.length ; i++) {
    auxConf2 = new Configuration(auxConf1,vars);
    pos = auxConf2.getIndexInTable();
    pot.values[pos] += values[i];
    auxConf1.nextConfiguration();
  }

  return pot;
}


/**
 * Removes the argument variable summing over all its values.
 * @param var a <code>Node</code> variable to be removed.
 * @return a new <code>PotentialTable</code> with the result of the deletion.
 */

public Potential addVariable(Node var) {

  Vector v;

  v = new Vector();
  v.addElement(var);
  return (addVariable(v));
}


/**
 * Marginalizes over a set of variables. It is equivalent
 * to <code>addVariable</code> over the other variables.
 * @param vars a <code>Vector</code> of variables
 * (<code>FiniteStates</code>).
 * @return a <code>PotentialTable</code> with the
 * marginalization of this potential over <code>vars</code>.
 */

public Potential marginalizePotential(Vector vars) {

  Vector vars2;
  int i;
  FiniteStates temp;
  PotentialTable pot;

  vars2 = new Vector();
  for (i=0 ; i<variables.size() ; i++) {
      temp = (FiniteStates)variables.elementAt(i);
      if (vars.indexOf(temp) == -1)
	vars2.addElement(temp);
  }

  pot = addVariable(vars2);

  return pot;
}


/**
 * Marginalizes over a set of variables using maximun as
 * marginalization operator.
 *
 * @param vars a <code>Vector</code> of variables
 * (<code>FiniteStates</code>).
 * @return a <code>PotentialTable</code> with the
 * max-marginalization of this potential over <code>vars</code>.
 */

public Potential maxMarginalizePotential(Vector vars) {

  int i,pos;
  PotentialTable pot;
  Vector vars2;
  Configuration conf,subConf;
  SetVectorOperations svo = new SetVectorOperations();

  // creates the new potential and sets the values to 0.0
  pot = new PotentialTable(vars);

  for (i=0 ; i<pot.values.length ; i++)
    pot.values[i] = Double.NEGATIVE_INFINITY;

  // Store in vars2 the variables present in "variables" but
  // not included in vars

  vars2 = svo.notIn(variables,vars);

  // Now for each configuration of the old potential, take
  // its value and see with which subconfiguration of the new
  // one it corresponds. If the new value is greater than
  // the value stored until this moment for the subconfiguration
  // then set the value as new value for the potential.

  conf = new Configuration(variables);

  for (i=0 ; i<values.length ; i++) {
    subConf = new Configuration(conf,vars2);
    pos = subConf.getIndexInTable();
    if (values[i] > pot.values[pos]) {
      pot.values[pos] = values[i];
    }
    conf.nextConfiguration();
  }

  return pot;
}



/**
 * Gets the addition of all the values in the potential.
 * @return the sum of all the values in the potential.
 */

public double totalPotential() {

  int i;
  double sum = 0.0;

  for (i=0 ; i<values.length ; i++)
    sum += values[i];

  return sum;
}


/**
 * Computes the sum of all the values of the potential
 * restricted to a configuration of variables.
 * @param conf a <code>Configuration</code> of variables.
 * @return the sum.
 */

public double totalPotential(Configuration conf) {

  Configuration auxConf;
  Vector aux;
  FiniteStates temp;
  int i, nv;
  double sum;

  aux = new Vector();

  for (i=0 ; i<variables.size() ; i++) {
    temp = (FiniteStates)variables.elementAt(i);
    if (conf.indexOf(temp) == -1)
      aux.addElement(temp);
  }

  // Number of values of the restricted potential.
  nv = (int)FiniteStates.getSize(aux);

  // Configuration preserving the values in conf.
  auxConf = new Configuration(variables,conf);

  sum = 0.0;
  for (i=0 ; i<nv ; i++) {
    sum += getValue(auxConf);
    auxConf.nextConfiguration(conf);
  }

  return sum;
}


/**
 * Gets the entropy of this potential.
 * @return the entropy of the potential.
 */

public double entropyPotential() {

  int i;
  double x, sum = 0.0;

  for (i=0 ; i<values.length ; i++) {
    x = values[i];
    if (x>0.0)
      sum += x*Math.log(x);
  }
  return ((-1.0) * sum);
}


/**
 * Computes the entropy of this potential restricted to
 * a configuration of variables.
 * @param conf a <code>Configuration</code> of variables.
 * @return the entropy.
 */

public double entropyPotential(Configuration conf) {

  Configuration auxConf;
  Vector aux;
  FiniteStates temp;
  int i, nv;
  double sum, x;

  aux = new Vector();
  for (i=0 ; i<variables.size() ; i++) {
    temp = (FiniteStates)variables.elementAt(i);
    if (conf.indexOf(temp) == -1)
      aux.addElement(temp);
  }

  // Size of the restricted potential.
  nv = (int)FiniteStates.getSize(aux);

  // Configuration preserving the values in conf.
  auxConf = new Configuration(variables,conf);

  sum = 0.0;
  for (i=0 ; i<nv ; i++) {
    x = getValue(auxConf);
    if (x>0.0)
      sum += x*Math.log(x);
    auxConf.nextConfiguration(conf);
  }

  return (-1.0 * sum);
}


/**
 * Computes the cross entropy of this potential.
 * We assume that the last n-2 variables of
 * the potential are the condicional subset.
 * @param conf a <code>Configuration</code> of variables.
 * @return the entropy.
 */

public double crossEntropyPotential() {

  Configuration auxConf, confyz, confxz, confz;
  PotentialTable pyz, pz, pxz;
  Vector aux, varsyz, varsxz, varsz;
  FiniteStates temp;
  int i, nv;
  double sum, valxyz, valyz, valxz, valz, valxZ, valyZ, valxyZ;

  // Size of the restricted potential.
  nv = (int)FiniteStates.getSize(variables);


  // Configuration preserving the values in variables.
  auxConf = new Configuration(variables);

  // Compute subsets of variables
  varsyz = (Vector)variables.clone();
  varsyz.removeElementAt(0); // remove variable x
  varsxz = (Vector)variables.clone();
  varsxz.removeElementAt(1); // remove variable y
  varsz = (Vector)varsxz.clone();
  varsz.removeElementAt(0); // remove variables x and y

  // Compute the marginal PotentialTables


  pyz = (PotentialTable)addVariable((FiniteStates)variables.elementAt(0));
  pxz = (PotentialTable)addVariable((FiniteStates)variables.elementAt(1));


  if (varsz.size() > 0) {
    pz = (PotentialTable)addVariable((FiniteStates)variables.elementAt(0));
    pz = (PotentialTable)pz.addVariable((FiniteStates)variables.elementAt(1));
  }
  else {
    pz = new PotentialTable();
  }

  sum = 0.0;
  for (i=0 ; i < nv ; i++) {
    valxyz = getValue(auxConf);
    if (valxyz > 0.0) {
      confyz = new Configuration(varsyz,auxConf);
      confxz = new Configuration(varsxz,auxConf);
      if (varsz.size() > 0){
	confz = new Configuration(varsz,auxConf);
      }
      else {
	confz = new Configuration();
      }
      valyz = pyz.getValue(confyz);
      valxz = pxz.getValue(confxz);
      if (varsz.size() > 0) {
	valz = pz.getValue(confz);
      }
      else {
	valz=1.0;
      }

      sum = sum +(valxyz * Math.log((valxyz*valz)/(valxz*valyz)));
    }
    auxConf.nextConfiguration();
  }

  return(sum);
}


/**
 * Combines two potentials. The argument <code>p</code> must be a subset of
 * the potential which receives the message, and must be
 * a <code>PotentialTable</code>
 *
 * IMPORTANT: this method modifies the object which receives the message.
 *
 * @param p the <code>PotentialTable</code> to combine with this.
 */

public void combineWithSubset(Potential p) {

  Vector v1, v2;
  Configuration conf, conf2;
  int i;
  double x;


  v1 = variables;
  v2 = p.variables;

  // Now explore all the configurations in the new potential,
  // evaluate the two operands according to this configuration,
  // and multiply the two values.

  conf = new Configuration(v1);

  for (i=0 ; i<this.values.length ; i++) {
    conf2 = new Configuration(v2,conf);

    x = getValue(conf);
    x *= p.getValue(conf2);
    setValue(conf,x);
    conf.nextConfiguration();
  }
}


/**
 * Combines two potentials. The argument <code>p</code> can be a
 * <code>PotentialTable</code>, a <code>PotentialTree</code>, a 
 * a <code>CanonicalPotential</code>,  a <code>PotentialConvexSet</code> or
 * a <code>PotentialContinuousPT</code>.
 * @param p the <code>Potential</code> to combine with this.
 * @return a new <code>PotentialTable</code> with the result of
 * the combination. If <code>p</code> is a code>PotentialContinuousPT</code>
 * then the results is returned as a code>PotentialContinuousPT</code>.
 */

public Potential combine(Potential p) {

  Vector v, v1, v2;
  Configuration conf, conf1, conf2;
  FiniteStates aux;
  int i;
  PotentialTable pot;
  double x;

  
  if ((p.getClass()==PotentialTable.class) ||
      (p.getClass()==PotentialConvexSet.class) ||
      (p.getClass()==PotentialTree.class) ||
      (p.getClass()==CanonicalPotential.class) ||
      (p.getClass()== GeneralizedPotentialTable.class)){//Modificado por jruiz
    v1 = variables;
    v2 = p.variables;
    v = new Vector(); // Variables of the new potential.

    for (i=0 ; i<v1.size() ; i++) {
      aux = (FiniteStates)v1.elementAt(i);
      v.addElement(aux);
    }

    for (i=0 ; i<v2.size() ; i++) {
      aux = (FiniteStates)v2.elementAt(i);
      if (aux.indexOf(v1) == -1)
	v.addElement(aux);
    }

    // Creates the new potential.
    pot = new PotentialTable(v);

    // Now explore all the configurations in the new potential,
    // evaluate the two operands according to this configuration,
    // and multiply the two values.

    conf = new Configuration(v);

    for (i=0 ; i<pot.values.length ; i++) {
      conf1 = new Configuration(v1,conf);
      conf2 = new Configuration(v2,conf);

      x = getValue(conf1);
      x *= p.getValue(conf2);
      pot.setValue(conf,x);
      conf.nextConfiguration();
    }
  }
  else if(p.getClass()==PotentialContinuousPT.class){
    return p.combine(this);
    //Potential paux=p.combine(this);
   // PotentialTree ptree=new PotentialTree((PotentialContinuousPT)paux);
    //pot=new PotentialTable(ptree);
  }
  else {
    System.out.println("Error in Potential PotentialTable.combine(Potential p): argument p was not a PotentialTable nor a PotentialTree nor a PotentialConvexSet nor a CanonicalPotential nor a PotentialContinuousPT");
    System.exit(1);
    pot = this;
  }
  return pot;
}



/**
 * Combines two potentials. The argument <code>p</code> can be a
 * <code>PotentialTable</code> or a <code>PotentialTree</code>.
 * @param p the <code>Potential</code> to combine with this.
 * @return a new <code>PotentialTable</code> with the result of
 * the combination.
 */

public PotentialTable combine(PotentialTable p,Function f) {
  Vector v, v1, v2;
  Configuration conf, conf1, conf2;
  FiniteStates aux;
  int i;
  PotentialTable pot;
  double x;
  double x2;

  v1 = variables;
  v2 = p.variables;
  v = new Vector(); // Variables of the new potential.

  for (i=0 ; i<v1.size() ; i++) {
    aux = (FiniteStates)v1.elementAt(i);
    v.addElement(aux);
  }

  for (i=0 ; i<v2.size() ; i++) {
    aux = (FiniteStates)v2.elementAt(i);
    if (aux.indexOf(v1) == -1)
      v.addElement(aux);
  }

  // Creates the new potential.
  pot = new PotentialTable(v);
  // Now explore all the configurations in the new potential,
  // evaluate the two operands according to this configuration,
  // and multiply the two values.

  conf = new Configuration(v);
  for (i=0 ; i<pot.values.length ; i++) {
    conf1 = new Configuration(v1,conf);
    conf2 = new Configuration(v2,conf);
    x = getValue(conf1);
    x2 = p.getValue(conf2);
    if (f.getClass()==SumFunction.class)
        x += x2;
    else if (f.getClass()==ProductFunction.class)
        x *= x2;
    else if (f.getClass()==MaxFunction.class)
    	x = (x>x2)?x:x2;
    else {
        System.out.println("Error in Potential PotentialTable.combine(Potential p, Function f): argument f was not a SumFunction, ProductFunction or MaxFunction");
        System.exit(1);
        pot = this;
    }  
    pot.setValue(conf,x);
    conf.nextConfiguration();
  }

  return pot;
}


/**
 * Sum this <code>Potential</code> with <code>Potential</code> pot
 * @param p the <code>Potential</code> to sum with this
 * <code>Potential</code>. p must be a PotentialTable, PotentialConvexSet or 
 * PotentialTree.
 * @return a new <code>PotentialTable</code> consisting of the sum
 * of <code>p</code> and this <code>Potential</code>.
 */

public Potential addition(Potential p){
  Vector v, v1, v2;
  Configuration conf, conf1, conf2;
  FiniteStates aux;
  int i;
  PotentialTable pot;
  double x;

  if (p.getClassName().equals("PotentialTable") ||
      p.getClassName().equals("PotentialConvexSet") ||
      p.getClassName().equals("PotentialTree")) {
    v1 = variables;
    v2 = p.variables;
    v = new Vector(); // Variables of the new potential.

    for (i=0 ; i<v1.size() ; i++) {
      aux = (FiniteStates)v1.elementAt(i);
      v.addElement(aux);
    }

    for (i=0 ; i<v2.size() ; i++) {
      aux = (FiniteStates)v2.elementAt(i);
      if (aux.indexOf(v1) == -1)
	v.addElement(aux);
    }

    // Creates the new potential.
    pot = new PotentialTable(v);

    // Now explore all the configurations in the new potential,
    // evaluate the two operands according to this configuration,
    // and sum the two values.

    conf = new Configuration(v);

    for (i=0 ; i<pot.values.length ; i++) {
      conf1 = new Configuration(v1,conf);
      conf2 = new Configuration(v2,conf);

      x = getValue(conf1);
      x += p.getValue(conf2);
      pot.setValue(conf,x);
      conf.nextConfiguration();
    }
  }
  else {
    System.out.println("Error in PotentialTable.addition(Potential p): argument p was not a PotentialTable nor a PotentialTree nor a PotentialConvexSet");
    System.exit(1);
    pot = this;
  }
  return pot;
}



/**
 * Combines this potential with the <code>PotentialTable</code>
 * of the argument.
 * @param p a <code>PotentialTable</code> to combine with this.
 * @return a new <code>PotentialTable</code> consisting of the combination
 * of <code>p (PotentialTable)</code> and this <code>PotentialTable</code>.
 */

public PotentialTable combine(PotentialTable p) {

  return (PotentialTable)combine((Potential)p);
}


/**
 * Combines this potential with the <code>PotentialTree</code>
 * of the argument.
 * @param p a <code>PotentialTree</code> to combine with this.
 * @return a new <code>PotentialTable</code> consisting of the combination
 * of <code>p (PotentialTree)</code> and this <code>PotentialTable</code>.
 */

public PotentialTable combine(PotentialTree p) {

  return (PotentialTable)combine((Potential)p);
}


/**
 * Multiply two <code>PotentialTable</code>s. The same as combining, but now
 * allowing to search the variables by name or reference.
 * @param p the <code>PotentialTable</code> to combine with this.
 * @param byname a boolean specifying the kind of search performed to
 * find the variables in the configurations: true=search variables by name,
 * false = search variables by reference.
 * @return a new <code>PotentialTable</code> with the result
 * of the combination.
 */
public PotentialTable multiply(PotentialTable p, boolean byname) {

  Vector v, v1, v2;
  Configuration conf, conf1, conf2;
  FiniteStates aux;
  int i;
  PotentialTable pot;
  double x;

  v1 = variables;
  v2 = p.variables;
  v = new Vector(); // Variables of the new potential.

  for (i=0 ; i<v1.size() ; i++) {
    aux = (FiniteStates)v1.elementAt(i);
    v.addElement(aux);
  }

  for (i=0 ; i<v2.size() ; i++) {
    aux = (FiniteStates)v2.elementAt(i);
    if (aux.indexOf(v1) == -1)
      v.addElement(aux);
  }

  // Creates the new potential.
  pot = new PotentialTable(v);

  // Now explore all the configurations in the new potential,
  // evaluate the two operands according to this configuration,
  // and multiply the two values.

  conf = new Configuration(v);

  for (i=0 ; i<pot.values.length ; i++) {
    conf1 = new Configuration(v1,conf,byname);
    conf2 = new Configuration(v2,conf,byname);

    x = getValue(conf1);
    x *= p.getValue(conf2);
    pot.setValue(conf,x);
    conf.nextConfiguration();
  }
  return pot;
}


/**
 * This method divides two <code>PotentialTable</code>s.
 * For the exception 0/0, the method compute the result as 0.
 * The exception ?/0: the method abort with a message in the standard output.
 * @param p the <code>PotentialTable</code> to divide this.
 * @return a new <code>PotentialTable</code> with the result
 * of the division.
 */

public PotentialTable divide(PotentialTable p) {

  return (PotentialTable) divide((Potential)p);
}


/**
 * This method divides two <code>PotentialTable</code>s.
 * For the exception 0/0, the method compute the result as 0.
 * The exception ?/0: the method abort with a message in the standard output.
 * @param p the <code>PotentialTable</code> to divide this.
 * @return a new <code>PotentialTable</code> with the result
 * of the division. The result is returned as a <code>Potential</code>.
 */

public Potential divide(Potential p) {


  Vector v, v1, v2;
  Configuration conf, conf1, conf2;
  FiniteStates aux;
  int i;
  PotentialTable pot;
  double x,y;

  v1 = variables;
  v2 = p.variables;
  v = new Vector(); // Variables of the new potential.

  for (i=0 ; i<v1.size() ; i++) {
    aux = (FiniteStates)v1.elementAt(i);
    v.addElement(aux);
  }

  for (i=0 ; i<v2.size() ; i++) {
    aux = (FiniteStates)v2.elementAt(i);
    if (aux.indexOf(v1)==-1)
      v.addElement(aux);
  }

  // Creates the new potential.
  pot = new PotentialTable(v);

  // Now explore all the configurations in the new potential,
  // evaluate the two operands according to this configuration,
  // and divide the two values.

  conf = new Configuration(v);

  for (i=0 ; i<pot.values.length ; i++) {
    conf1 = new Configuration(v1,conf);
    conf2 = new Configuration(v2,conf);

    x = getValue(conf1);
    y = p.getValue(conf2);

    if((x!=0.0) && (y!=0.0)){
	x /= y;
    }else{
	if ((x==0.0) && (y==0.0)){
	    x=0;
	}else{
	    try{
		x /= y;
	    }catch (Exception e){
		System.out.println("Divide by zero");
		          System.exit(0);
                x = 0;
	    }
	}
    }
    pot.setValue(conf,x);
    conf.nextConfiguration();
  }

  return pot;
}


/**
 * Adds the argument potential to this one.
 * The process is the same as in <code>combine</code>, but instead
 * of multiplying, now we sum.
 * @param p the <code>PotentialTable</code> to add to this.
 * @return a <code>PotentialTable</code> with the result of the addition.
 */

public PotentialTable Add(PotentialTable p) {

  Vector v, v1, v2;
  Configuration conf, conf1, conf2;
  FiniteStates aux;
  int i;
  PotentialTable pot;
  double x;

  v1 = variables;
  v2 = p.variables;
  v = new Vector(); // Variables of the new potential.

  for (i=0 ; i<v1.size() ; i++) {
    aux = (FiniteStates)v1.elementAt(i);
    v.addElement(aux);
  }

  for (i=0 ; i<v2.size() ; i++) {
    aux = (FiniteStates)v2.elementAt(i);
    if (aux.indexOf(v1) == -1)
      v.addElement(aux);
  }

  // Creates the new potential.
  pot = new PotentialTable(v);

  // Now explore all the configurations in the new potential,
  // evaluate the two operands according to this configuration,
  // and sum the two values.

  conf = new Configuration(v);

  for (i=0 ; i<pot.values.length ; i++) {
    conf1 = new Configuration(v1,conf);
    conf2 = new Configuration(v2,conf);

    x = getValue(conf1);
    x += p.getValue(conf2);
    pot.setValue(conf,x);
    conf.nextConfiguration();
  }

  return pot;
}

/**
 * Adds the argument potential to this one.
 * The variables are searched by name or by reference, when
 * constructing the subconfigurations.
 * The process is the same as in combineByName, but instead
 * of multiplying, now we sum.
 * @param p the <code>PotentialTable</code> to add to this.
 * @param byname a boolean indicating the type of search applied
 * when constructing the subconfigurations.
 * @return a <code>PotentialTable</code> with the result of the addition.
 */

public PotentialTable add(PotentialTable p, boolean byname) {

  Vector v, v1, v2;
  Configuration conf, conf1, conf2;
  FiniteStates aux;
  int i;
  PotentialTable pot;
  double x;

  v1 = variables;
  v2 = p.variables;
  v = new Vector(); // Variables of the new potential.

  for (i=0 ; i<v1.size() ; i++) {
    aux = (FiniteStates)v1.elementAt(i);
    v.addElement(aux);
  }

  for (i=0 ; i<v2.size() ; i++) {
    aux = (FiniteStates)v2.elementAt(i);
    if (aux.indexOf(v1) == -1)
      v.addElement(aux);
  }

  // Creates the new potential.
  pot = new PotentialTable(v);

  // Now explore all the configurations in the new potential,
  // evaluate the two operands according to this configuration,
  // and sum the two values.

  conf = new Configuration(v);

  for (i=0 ; i<pot.values.length ; i++) {
    conf1 = new Configuration(v1,conf,byname);
    conf2 = new Configuration(v2,conf,byname);

    x = getValue(conf1);
    x += p.getValue(conf2);
    pot.setValue(conf,x);
    conf.nextConfiguration();
  }

  return pot;
}


/**
 * Copies this potential.
 * @return a copy of this <code>PotentialTable</code>.
 */

public Potential copy() {

  PotentialTable pot;
  int i, n;

  pot = new PotentialTable(variables);

  n = (int)FiniteStates.getSize(variables);

  for (i=0 ; i<n ; i++)
    pot.values[i] = values[i];

  return pot;
}


/**
 * Converts this potential to a <code>PotentialTree</code>.
 * @return a new <code>PotentialTree</code> with the same information as this
 * <code>PotentialTable</code>.
 */

public PotentialTree toTree() {

  int i, total;
  Configuration conf;
  PotentialTree pot;
  Vector vars;


  pot = new PotentialTree(getVariables());
  vars = (Vector) getVariables().clone();
  conf = new Configuration();

  setTreeFromTable(pot.values,conf,vars);

  return pot;
}


/**
 * Recursive procedure that constructs a probability tree
 * from a probability table.
 * @param tree the tree we are constructing. This tree is modified.
 * @param conf the <code>Configuration</code> that leads to the subtree
 * we are operating in this recursion step. This configuration is
 * modified.
 * @param vars <code>Vector</code> of variables not already explored.
 * This vector is modified.
 */

public void setTreeFromTable(ProbabilityTree tree,
			     Configuration conf, Vector vars) {

  FiniteStates var;
  int i;
  Vector aux;

  if (vars.size() == 0)
    tree.assignProb(getValue(conf));
  else {
    var = (FiniteStates) vars.elementAt(0);
    vars.removeElementAt(0);
    tree.assignVar(var);

    for(i=0 ; i<var.getNumStates() ; i++) {
      aux = (Vector)vars.clone();
      conf.insert(var,i);
      setTreeFromTable(tree.getChild(i),conf,aux);
      conf.remove(conf.getVariables().size() - 1);
    }
  }
}


/**
 * Add a quantity to all the elements of a potential.
 * The object is modified.
 */

public void sum(double x) {

  int i;
 

  for (i=0 ; i<values.length ; i++)
    values[i] += x;
}

/**
 * Normalizes the values of this potential.
 * The object is modified.
 */

public void normalize() {

  int i;
  double s;

  s = totalPotential();

  for (i=0 ; i<values.length ; i++)
    values[i] /= s;
}


/**
 * Normalizes the values of this potential, but using Laplace Correction,
 * to avoid overfitting to the data in case that the table is learnt
 * from a database.
 * The object is modified.
 */

public void LPNormalize() {

  int i, nv;
  double s;
  Configuration conf = new Configuration(variables);

  s = totalPotential();
  nv = (int)FiniteStates.getSize(variables);
  for (i=0 ; i<values.length ; i++) {
    int pos = conf.getIndexInTable();
    values[pos] = (values[pos]+1) / (s+nv);
    conf.nextConfiguration();
  }
}


/**
 * This method incorporates the evidence passed as argument to the
 * potential, that is, put to 0 all the values whose configurations
 * are not consistent with the evidence. The object is modified.
 * @param evid a <code>Configuration</code> representing the evidence.
 */

public void instantiateEvidence(Configuration evid) {

  int i, total;
  Configuration conf, scEvid, scPot; // the subconfigurations are used
                                    // to test the consistence
  Vector varsEvid; //for the variables of the evidence
  Vector evidNotInPot, potNotInEvid; // variables not in ...
  SetVectorOperations svo = new SetVectorOperations();

  total = (int)FiniteStates.getSize(variables);
  varsEvid = evid.getVariables();
  evidNotInPot = svo.notIn(varsEvid,variables);
  potNotInEvid = svo.notIn(variables,varsEvid);
  scEvid = new Configuration(evid,evidNotInPot);

  conf = new Configuration(variables);

  for (i=0 ; i<total ; i++) {
    scPot = new Configuration(conf,potNotInEvid);
    if (!scPot.equals(scEvid))
      setValue(conf,0.0);
    conf.nextConfiguration();
  }
}


/**
 * Gets the configuration of maximum probability consistent with a
 * configuration of variables.
 * @param subconf the subconfiguration to ensure consistency.
 * @return the <code>Configuration</code> of maximum probability
 * included in the <code>Potential</code>, that is consistent with the
 * subConfiguration passed as parameter (this subconfiguration can be empty).
 *
 * NOTE: if there are more than one configuration with maximum
 * probability, the first one is returned.
 */

public Configuration getMaxConfiguration(Configuration subconf) {

  Configuration conf,aux,sc;
  double prob = Double.NEGATIVE_INFINITY; // we suppose that the probability always is positive
  int total, i;
  Vector vars;
  SetVectorOperations svo = new SetVectorOperations();

  total = (int)FiniteStates.getSize(variables);

  aux = new Configuration(variables);
  conf = new Configuration(variables);

  if (subconf.size() == 0) { // there is no consistency to check
    for (i=0 ; i<total ; i++) {
      if (getValue(i) > prob) {
        prob = getValue(i);
        conf.setValues((Vector)aux.getValues().clone());
      }
      aux.nextConfiguration();
    }
  }
  else{ // there is consistency to check
    vars = svo.notIn(variables,subconf.getVariables());
    for (i=0 ; i<total ; i++) {
      sc = new Configuration(aux,vars);
      if (sc.equals(subconf)) {
        if (getValue(i) > prob) {
          prob = getValue(i);
          conf.setValues((Vector)aux.getValues().clone());
        }
      }
      aux.nextConfiguration();
    }
  }

  return conf;
}


/**
 * @param subconf the subconfiguration to ensure consistency
 * @param list the list of configurations to be differents
 * @return the configuration of maximum probability included in the
 * potential, that is consistent with the subConfiguration passed as
 * parameter (this subconfiguration can be empty), and differents to
 * all the configurations passed in the vector
 *
 * NOTE: if there are more than one configuration with maximum
 * probability, the first one is returned
 */

public Configuration getMaxConfiguration(Configuration subconf,
                                         Vector list) {

  Configuration conf, aux, sc;
  double prob = Double.NEGATIVE_INFINITY; // we suppose that the probability always is positive
  int total, i;
  Vector vars;
  SetVectorOperations svo = new SetVectorOperations();

  total = (int)FiniteStates.getSize(variables);

  aux = new Configuration(variables);
  conf = new Configuration( );

  if (subconf.size() == 0) { // there is no consistency to check
    for (i=0 ; i<total ; i++) {
      if ((getValue(i) > prob) && (!aux.contained(list)) ) {
        prob = getValue(i);
        if (conf.size() == 0)
	  conf = new Configuration(variables);
        conf.setValues((Vector)aux.getValues().clone());
      }
      aux.nextConfiguration();
    }
  }
  else{ // there is consistency to check
    vars = svo.notIn(variables,subconf.getVariables());
    for (i=0 ; i<total ; i++) {
      sc = new Configuration(aux,vars);
      if (sc.equals(subconf)) {
        if ((getValue(i) > prob) && (!aux.contained(list)) ) {
          prob = getValue(i);
          if (conf.size() == 0)
	    conf = new Configuration(variables);
          conf.setValues((Vector)aux.getValues().clone());
        }
      }
      aux.nextConfiguration();
    }
  }

  return conf;
}


/**
 * Converts a <code>Potential</code> to a <code>PotentialTable</code>.
 * @param pot the <code>Potential</code> to convert.
 * @returns a new <code>PotentialTable</code> resulting from converting
 * <code>Potential pot</code>
 */

public static PotentialTable convertToPotentialTable(Potential pot) {

  PotentialTable newPot;

  if (pot.getClass().getName().equals("elvira.potential.PotentialTree")) {
    newPot = new PotentialTable((PotentialTree)pot);
  }
  else if (pot.getClass().getName().equals("elvira.potential.PotentialMTree")) {
    newPot = new PotentialTable((PotentialMTree)pot);
  }

  //Modificado por jruiz
  else if (pot.getClass().getName().equals("elvira.potential.PotentialTable") ||
           pot.getClassName().equals("GeneralizedPotentialTable")) {
    newPot = (PotentialTable)(pot.copy());
  }//Fin modificado por jruiz

  else if (pot.getClassName().equals("CanonicalPotential")) {
    newPot = ((CanonicalPotential) pot).getCPT();
  }
  else
    newPot = null;

  return newPot;
}


/**
 * Gets the name of the class.
 * @return a <code>String</code> with the name of the class.
 */

public String getClassName() {

  return new String("PotentialTable");
}



/**
 * Method to convert the probabilistic (or utility) potential to deterministic potential
 * using maximum
 * @param node node of the new deterministic potential
 * @return a new and transformed potential table, whose first variable is the deterministic node
 */

public PotentialTable toDeterministic(Node node){
	NodeList nl;
	PotentialTable newPot;
	Configuration auxConf;
	Configuration maxConf;
	Vector newVars;
	

	//Initalize the new potential to 0
	newVars = (Vector)(variables.clone());
	newVars.remove(node);
	newVars.insertElementAt(node,0);
	newPot = new PotentialTable(newVars);
	newPot.setValue(0.0);
	
	//Rest of variables of the potential
	nl = new NodeList(variables);
	nl.removeNode(node);
	
	//Compute the maximum configuration for each combination of the rest of variables
	auxConf = new Configuration(nl);
	for (int i=0;i<nl.getSize();i++){
		//Compute the maximum configuration
		maxConf = getMaxConfiguration(auxConf);
		
		//Set the maximum configuration to 1.0
		newPot.setValue(maxConf,1.0);
		
		//Next configuration
		auxConf.nextConfiguration();
	}
	
	return newPot;
	
	
}

/**
 * Computes the maximum of the values in the potential table
 * @return the maximum.
 */

public double maximumValue() {

  double max=Double.NEGATIVE_INFINITY;
  double aux;
  
  for (int i=0;i<values.length;i++){
  	aux = values[i];
  	if (aux>max){
  		max = aux;
  	}
 }
  return max;
}


/**
 * Computes the minimum of the values in the potential table
 * @return the minimum.
 */

public double minimumValue() {

  double min=Double.POSITIVE_INFINITY;
  double aux;
  
  for (int i=0;i<values.length;i++){
  	aux = values[i];
  	if (aux<min){
  		min = aux;
  	}
 }
  return min;
}


/**
 * It transforms a table of utilities by applying the Cooper's transformation.
 * @param utilityNode Utility node that will be added to the potential
 * @param minimum Minimum value in the potential of utilities
 * @param maximum Maximum value in the potential of utilities
 * @return a new potential table, whose first variable is utilityNode. The values are computed with Cooper's transformation
 */
public PotentialTable convertUtilityIntoProbability(FiniteStates utilityNode,double minimum,double maximum){
	PotentialTable newPot;
	Vector vars;
	NodeList parents;
	double originalValue;
	double transformedValue;
	Configuration auxConf;
	
	//Set the variables of the new potential table. 'utilityNode' will be the first variable
	vars = (Vector)(variables.clone());
	parents = new NodeList (vars);
	vars.add(0,utilityNode);
	
	
	//Initalize the new potential to 0
	newPot = new PotentialTable(vars);
	
		//Compute the transformation for each combination of the rest of variables
		auxConf = new Configuration(parents);
		for (int i=0;i<parents.getSize();i++){
			//Compute the transformed value
			originalValue = getValue(auxConf);
			transformedValue = CooperPolicyNetwork.directCooperTransformation(originalValue,minimum,maximum);
					
			//Set the probability for Utility = yes
			newPot.setValues(auxConf,utilityNode,0,null,transformedValue);
			//Set the probability for Utility = no
			newPot.setValues(auxConf,utilityNode,1,null,1-transformedValue);
			
			//Next configuration
			auxConf.nextConfiguration();
		}
	
	
	
	return newPot;
}

/**
 * It transforms a table of probabilities to a table of utilities by applying
 * the reverse Cooper's transformation.
 * @param utilityNode Utility node that will be added to the potential
 * @param maximum Maximum value in the potential of utilities
 * @param minimum Minimum value in the potential of utilities
 * @return a new potential table, whose first variable is utilityNode. The values are computed with Cooper's transformation
 */
public PotentialTable convertProbabilityIntoUtility(double minimum,double maximum){
	PotentialTable newPot;
	Vector vars;
	NodeList parents;
	Configuration auxConf;
	FiniteStates valueNode;
	Vector probabilitiesCooper;
	double probabilityCooperYes;
	double utility;
	
	//Set the variables of the new potential table. 'utilityNode' will be the first variable
	vars = (Vector)(variables.clone());
	valueNode = (FiniteStates)(vars.elementAt(0));
	vars.remove(valueNode);
	parents = new NodeList (vars);
			
	
	//Initalize the new potential to 0
	newPot = new PotentialTable(vars);
	
	   // Now determine the constants required for the transformation
	   double k1=maximum-minimum;
	   double k2=-minimum;
	   
		//Compute the transformation for each combination of the rest of variables
		auxConf = new Configuration(parents);
		for (int i=0;i<parents.getSize();i++){
			//Compute the transformed value
			probabilitiesCooper = this.getValuesForConf(auxConf,valueNode);
			probabilityCooperYes =((Double)(probabilitiesCooper.firstElement())).doubleValue(); 
			utility = probabilityCooperYes*k1-k2;
			
			//Set the probability for Utility = yes
			newPot.setValue(auxConf,utility);
						
			//Next configuration
			auxConf.nextConfiguration();
			
		}
	
	
	
	return newPot;
}
/**
 * Method to convert the probabilities to imprecise probabilities,
 * following imprecise Dirichlet model
 * @param n number of samples to consider
 * @param s param for the conversion
 * @return transformed potential table
 */

public Potential toImpreciseDirichletModel(int n, int s){
  Vector values;
  FiniteStates var;
  PotentialTable minValues;
  PotentialTable maxValues;
  PotentialIntervalTable finalValues;
  int sizeOfVar;
  int i,j;
  double val;
  double min;
  double max;
  
  // Get the set of variables

  Vector vars=getVariables();

  // Build two new potentials for these variables

  minValues=new PotentialTable(vars);
  maxValues=new PotentialTable(vars);

  // Get all the variables, except the first

  Vector conditioning=new Vector();

  // Add variables to conditioning

  for(i=1; i < vars.size(); i++){
    conditioning.addElement(vars.elementAt(i));
  }

  // Get the first variable to determine the number of values

  var=(FiniteStates)vars.elementAt(0);
  sizeOfVar=var.getNumStates();

  // Build a configuration for all variables except the first

  Configuration conf=new Configuration(conditioning);

  // Build a configuration for the whole set of variables

  Configuration total=new Configuration(vars);

  // Now, go on the whole set of values for the variables 
  // in conditioning

  int size=conf.possibleValues();

  // May be the potential only has one variable. In this case
  // size will be 1, and everything is ok

  for(i=0; i < size; i++){
    // Consider all the values for var

    for(j=0; j < sizeOfVar; j++){
      // Complete the total configuration

      total.resetConfiguration(conf);
      total.putValue(var,j);

      // Get the value for this configuration

      val=getValue(total);

      // Get the min

      min=(val*n)/(n+s);

      // Get the max

      max=((val*n)+s)/(n+s);

      // Show all the values

      System.out.println("Prob = "+val+" (min = "+min+", max = "+max+")");

      // Set these values to the potential tables for min and max

      minValues.setValue(total,min);
      maxValues.setValue(total,max);
    }

    // Consider the next configuration

    conf.nextConfiguration();
  }

  // Now, make the potential interval table

  finalValues=new PotentialIntervalTable(minValues,maxValues);
  finalValues.print();

  // Return the potential interval table

  return finalValues;
}


/* _______________ FUSION METHODS ______________*/



/**
 * Potential linear pool. Computes the linear pool of a vector of
 * potentials and stores it in this <code>PotentialTable</code>.
 * @param pv a <code>Vector</code> of </code>PotentialTable</code>s.
 */

public void linearPool (Vector pv) {

  int i, nv;
  double x;
  Configuration conf;
  PotentialTable pot;

  // Add all the elements
  pot = (PotentialTable)((PotentialTable)pv.elementAt(0)).copy();
  for (i=1; i<pv.size(); i++)
    pot = pot.add((PotentialTable)pv.elementAt(i),true);

  // Divide by the number of potentials being combined
  conf = new Configuration (pot.getVariables());
  for (i=0 ; i<pot.values.length ; i++) {
    x = pot.getValue(conf);
    x /= (double)pv.size();
    pot.setValue(conf, x);
    conf.nextConfiguration();
  }

  // Copy in this ProbabilityTable the values obtained
  variables = (Vector)pot.variables.clone();
  nv = (int)FiniteStates.getSize(variables); //nro. variables
  values = new double[nv];
  for (i=0 ; i<nv ; i++)
    values[i] = pot.values[i];
}


/**
 * Normalizes the values of this potential over a specified variable.
 * If the specified variable is not in the potential variables list,
 * does not do anything.
 * @param v a <code>FiniteStates</code> variable.
 */

public void normalizeOver (FiniteStates v) {

  int i, j, vvalue;
  String vname;
  double k;
  Configuration conf;
  PotentialTable pot;

  if (v.indexOf(variables) != -1) {
    //Obtain the normalization constants for each configuration
    pot = new PotentialTable (variables);
    conf = new Configuration (variables);

    vname = v.getName();
    for (i=0 ; i<values.length ; i++) {
      k = getValue(conf);
      vvalue = conf.getValue(vname);

      for (j=0 ; j<v.getNumStates() ; j++)
	if (j != vvalue) {
	  conf.putValue(vname,j);
	  k += getValue(conf);
	  conf.putValue(vname,vvalue);
	}
	pot.setValue(conf,k);
	conf.nextConfiguration();
    }

    // Normalize
    conf = new Configuration(variables);
    for (i=0 ; i<values.length ; i++) {
      setValue(conf, getValue(conf)/pot.getValue(conf));
      conf.nextConfiguration();
    }
  }
}


/**
 * Potential logarithmic pool. Computes the logarithmic pool of a
 * vector of potentials and stores it in this <code>PotentialTable</code>.
 * @param pv a <code>Vector</code> of <code>PotentialTable</code>s.
 */

public void logarithmicPool (Vector pv) {

  int i, nv;
  PotentialTable pot;

  // Copy the first potential and multiply all the elements
  pot = (PotentialTable)((PotentialTable)pv.elementAt(0)).copy();
  for (i=1 ; i<pv.size() ; i++)
    pot = pot.multiply((PotentialTable)pv.elementAt(i),true);

  // Normalizes potential values over the first variable
  pot.normalizeOver((FiniteStates)pot.getVariables().elementAt(0));

  // Copy in this ProbabilityTable the values obtained
  variables = (Vector)pot.variables.clone();
  nv = (int)FiniteStates.getSize(variables); //nro. variables
  values = new double[nv];
  for (i=0 ; i<nv ; i++)
    values[i] = pot.values[i];
}


/**
 * noisyOR combination method for Potentials of bivaluated variables.
 * Private method of noisyOR.
 *
 * We assume that the potentials in the vector are of type P(Y|X), that
 * the first binary variable Y is the same in all potentials and X is
 * a set of cause variables (binary/multivalued) of Y.
 *
 * @param pv a <code>Vector</code> of <code>PotentialTable</code>s.
 * @param loose a bolean value indicating whether we have to compute a loose
 * probability term or not.
 */

private PotentialTable binNoisyOr(Vector vp, boolean loose) {

  int i, j, k;
  NodeList nl;
  double p0, x, y, d;
  Configuration conf;
  PotentialTable pot, p;

  // Construct the list of potential's variables
  nl = new NodeList();
  for (i=0 ; i<vp.size() ; i++)
    nl.merge(new NodeList(((PotentialTable)vp.elementAt(i)).variables));

  // Compute the loose probability term
  conf = new Configuration(nl.toVector());
  conf.putValue((FiniteStates)nl.elementAt(0),1);
  if (loose) {
    p0 = 1.0;
    for (i=0 ; i<vp.size() ; i++) {
      x = ((PotentialTable)vp.elementAt(i)).getValue(conf);
      if (x < p0)
	p0 = x;
    }
  }
  else
    p0 = 0.0;

  // Compute the new noisy OR potential
  pot = new PotentialTable(nl);

  // Explore all configurations of the new potential,
  // evaluating both operands according to this configuration,
  // and compute noisyOR combination.
  conf = new Configuration(pot.variables);
  pot.setValue(conf,1.0-p0);
  conf.nextConfiguration();
  for (i=1 ; i<pot.values.length/2 ; i++) {
    x = 1.0;
    d = 1.0;
    for (j=1 ; j<pot.variables.size() ; j++)
      if (conf.getValue((FiniteStates)pot.variables.elementAt(j)) != 0) {
      x *= ((PotentialTable)vp.elementAt(j-1)).getValue(conf);
      d *= (1.0 - p0);
    }

    x /= d;
    x *= (1.0 - p0);
    pot.setValue(conf,x);
    conf.nextConfiguration();
  }

  for ( ; i<pot.values.length; i++) {
    conf.putValue((FiniteStates)pot.variables.elementAt(0),0);
    x = pot.getValue(conf);
    conf.putValue((FiniteStates)pot.variables.elementAt(0),1);
    pot.setValue(conf,1.0-x);
    conf.nextConfiguration();
  }

  return pot;
}


/**
 * noisyOR combination method for Potentials of multivaluated variables.
 * Private method of noisyOR.
 *
 * We assume that the potentials in the vector are of type P(Y|X), that
 * the first multivalued variable Y is the same in all potentials and X is
 * a unique cause variables (binary/multivalued) of Y.
 *
 * @param pv a <code>Vector</code> of <code>PotentialTable</code>s.
 * @param loose a bolean value indicating if we have to compute a loose
 * probability term or not.
 */

private PotentialTable mulNoisyOr(Vector vp, boolean loose) {

  int i, j, k, m, numStates;
  FiniteStates bin;
  PotentialTable pot, bpot;
  Configuration conf, bconf;
  NodeList bnl;
  double x;
  Vector bpv;

  // Simulate he effect variable by binary variables
  Vector sim = new Vector();
  numStates = ((FiniteStates)((PotentialTable)vp.elementAt(0)).variables.elementAt(0)).getNumStates();

  for (i = numStates - 1; i>0; i--) {
    bin = new FiniteStates(2);
    bin.setName("BinSim");
    bpv = new Vector(); // vector with the potentials of the binary variable

    // Compute the potential of the binary variable for each
    // potential in the input vector.
    for (j=0 ; j<vp.size() ; j++) {
      pot = (PotentialTable)vp.elementAt(j);

      // Form the list of variables of the potential
      bnl = new NodeList();
      bnl.insertNode(bin);
      bnl.insertNode((FiniteStates)pot.variables.elementAt(1));

      // Compute the potential of the binary variable
      bpot = new PotentialTable(bnl);
      bconf = new Configuration(bpot.variables);
      bconf.putValue(bin.getName(),1);
      bconf.nextConfiguration();

      for (k=bpot.values.length/2 ; k<bpot.values.length ; k++) {
	x = 0.0;
	for (m = numStates-1 ; m >= i ; m--) {
	  conf = new Configuration (pot.variables, bconf, true);
	  conf.putValue((FiniteStates)pot.variables.elementAt(0),m);
	  x += pot.getValue(conf);
	}
	bpot.setValue(bconf,x);
	bconf.nextConfiguration();
      }

      for (k=0 ; k<bpot.values.length/2 ; k++) {
	bconf.putValue((FiniteStates)bpot.variables.elementAt(0),1);
	x = bpot.getValue(bconf);
	bconf.putValue((FiniteStates)bpot.variables.elementAt(0),0);
	bpot.setValue(bconf,1.0-x);
	bconf.nextConfiguration();
      }

      bpv.addElement(bpot);
    }

    // Compute the noisy-OR combination for the binary variable
    sim.addElement(binNoisyOr(bpv,loose));
  }

  // Rebuild the potential of the effect variable
  // Construct the list of potential's variables
  bnl = new NodeList();
  for (i=0 ; i<vp.size() ; i++)
    bnl.merge(new NodeList(((PotentialTable)vp.elementAt(i)).variables));

  pot = new PotentialTable(bnl);

  for (i=numStates-1 ; i>0 ; i--) {
    conf = new Configuration (pot.variables);
    conf.putValue((FiniteStates)pot.variables.elementAt(0),i);
    bpot = (PotentialTable)sim.elementAt(numStates-1-i);

    for (k=0 ; k<pot.values.length/numStates ; k++) {
      x = 0.0;
      for (j=numStates-1 ; j>i ; j--) {
	conf.putValue((FiniteStates)pot.variables.elementAt(0),j);
	x += pot.getValue(conf);
      }
      conf.putValue((FiniteStates)pot.variables.elementAt(0),i);
      bconf = new Configuration (bpot.variables, conf, true);
      bconf.putValue((FiniteStates)bpot.variables.elementAt(0),1);
      pot.setValue(conf, bpot.getValue(bconf)-x);
      conf.nextConfiguration();
    }
  }

  conf = new Configuration(pot.variables);
  for (k=0 ; k<pot.values.length/numStates ; k++) {
    x = 0.0;
    for (j=numStates-1 ; j>0 ; j--) {
      conf.putValue((FiniteStates)pot.variables.elementAt(0),j);
      x += pot.getValue(conf);
    }
    conf.putValue((FiniteStates)pot.variables.elementAt(0),0);
    pot.setValue(conf, 1.0-x);
    conf.nextConfiguration();
  }


  return pot;
}


/**
 * Potential noisy-OR pool. Computes the noisy-OR pool of a
 * vector of potentials and stores it in this <code>PotentialTable</code>.
 *
 * We assume that the potentials in the vector are of type P(Y|X), that
 * the first variable Y is the same in all potentials and X is a unique
 * (not repeated) variable cause of Y.
 *
 * @param pv a <code>Vector</code> of </code>PotentialTable</code>s.
 * @param loose a boolean indicating if we have to compute a loose
 * probability term or not.
 */

public void noisyORPool(Vector pv, boolean loose) {

  int i, j, nv;
  double p0, x, d;
  Vector v; // sets of variables
  FiniteStates n1,n2;
  NodeList nl;
  Configuration conf;
  PotentialTable pot;

  // Verification of assumptions
  v = new Vector();

  // More than 2 variables in a potential table?
  for (i=0 ; i<pv.size() ; i++)
    if (((PotentialTable)pv.elementAt(i)).variables.size() != 2) {
    System.out.println("Error in void noisyOR (Vector pv: too many variables to combine.");
    System.exit(1);
  }

  // Different first variable in a potential table?
  n1 = (FiniteStates)((PotentialTable)pv.elementAt(0)).variables.elementAt(0);
  v.addElement(n1);
  for (i=1 ; i<pv.size() ; i++) {
    n2 = (FiniteStates)((PotentialTable)pv.elementAt(i)).variables.elementAt(0);
    if (n1.compareTo(n2) != 0) {
      System.out.println ("Error in void noisyOR (Vector pv): distinct first variable");
      System.exit(1);
    }
  }

  // Repeated second variable in other potential table?
  for (i=0 ; i<pv.size() ; i++) {
    n1 = (FiniteStates)((PotentialTable)pv.elementAt(i)).variables.elementAt(1);
    for (j=i+1 ; j<pv.size() ; j++) {
      n2 = (FiniteStates)((PotentialTable)pv.elementAt(j)).variables.elementAt(1);
      if ( n1.compareTo(n2) == 0) {
	System.out.println ("Error in void noisyOR (Vector pv): duplicated second variable");
	System.exit(1);
      }
    }
    v.addElement(n1);
  }

  // Construct the list of potential's variables
  nl = new NodeList();
  for (i=0 ; i<pv.size() ; i++)
    nl.merge(new NodeList(((PotentialTable)pv.elementAt(i)).variables));

  // Verify if the effect variable is binary or multivalued
  if (((FiniteStates)nl.elementAt(0)).getNumStates() == 2)
    pot = binNoisyOr(pv,loose);
  else
    pot = mulNoisyOr(pv,loose);


  // Copy in this ProbabilityTable the values obtained
  variables = (Vector)pot.variables.clone();
  nv = (int)FiniteStates.getSize(variables);
  values = new double[nv];
  for (i=0 ; i<nv ; i++)
    values[i] = pot.values[i];
}

/**
 * 
 * This method calculates the index of the potential
 * 
 * @param values - array with values
 * @param weights - array with weights
 * @return pos - position in the potential
 * 
 */
public static int getIndexInTable(int[] values, double[] weights){
    int pos = 0;
    for(int i=0; i<values.length;i++){
        pos += (values[i]*weights[i]);
    }
    return pos;
}

  /**

   * This method orders the potential making the <code>Node</code> x

   * the last of the set of variables

   * @param x the <code>Node</code> to be the last.

   */



  
  public Potential sendVarToEnd(Node x) {

    NodeList finalOrderOfVariables=new NodeList();

    Potential newPotential;

    Configuration configuration;

    Node aux;

    double value;

    int i;

    long j,size;



    // Copy the variables from the potential to finalOrderOfVariables,

    // changing the order of x



    for(i=0; i < variables.size(); i++) {

      aux=(Node)variables.elementAt(i);

      if(!aux.getName().equals(x.getName())) {

        finalOrderOfVariables.insertNode(aux);

      }

    }



    // Add the last one



    finalOrderOfVariables.insertNode(x);



    // Now, make a potential with the reordered set of variables
    newPotential=new PotentialTable(finalOrderOfVariables);
    size=(long)FiniteStates.getSize(variables);



    // Now, copy the values



    configuration=new Configuration(finalOrderOfVariables);



    // Loop to copy the values



    for(j=0; j < size; j++) {



      value=getValue(configuration);



      // This value will be copied into newPotential



      newPotential.setValue(configuration,value);



      // Move to next configuration



      configuration.nextConfiguration();

    }



    // At the end return the new Potential



    return((Potential)newPotential);

  }



} // end of class
