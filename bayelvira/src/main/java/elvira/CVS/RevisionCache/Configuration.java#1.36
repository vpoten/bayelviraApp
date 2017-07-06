/* Configuration.java */

package elvira;

import java.util.Vector;
import elvira.FiniteStates;
import java.io.*;

/**
 * Implements a configuration, consisting of a list of
 * pairs (variable,value), where variable is a
 * <code>FiniteStates</code> node
 * and value is an <code>Integer</code>.
 *
 * @since 2/3/2005
 */

public class Configuration {

/**
 * Variables in the configuration.
 */
Vector variables;

/**
 * Values of variables in <code>variables</code>.
 */
Vector values;


/**
 * Creates an empty <code>Configuration</code>.
 */

public Configuration() {
  
  variables = new Vector();
  values = new Vector();
}
  

/**
 * Creates a configuration for a list of variables with the
 * values equal to zero.
 * @param vars a vector of variables.
 */

public Configuration(Vector vars) {

  Integer val;
  int i;

  variables = (Vector)vars.clone();
  values = new Vector();

  for (i=0 ; i<vars.size() ; i++) {
    val = new Integer(0);
    values.addElement(val);
  }
}


/**
 * Creates a configuration for a list of variables with the
 * values equal to zero.
 * @param vars a <code>NodeList</code> containing the variables.
 */

public Configuration(NodeList vars) {

  Integer val;
  int i;

  variables = (Vector)vars.getNodes().clone();
  values = new Vector();

  for (i=0 ; i<vars.size() ; i++) {
    val = new Integer(0);
    values.addElement(val);
  }
}


/**
 * Creates a new configuration from a list of variables
 * and a list of values of those variables.
 * @param vars a vector of variables (<code>FiniteStates</code>). It is
 * directly taken as the list of variables of the configuration. Thus, do
 * not modify it outside.
 * @param vals a vector of values (<code>Integer</code>). It is directly
 * taken as the list of values of the configuration. Thus, do
 * not modify it outside.
 */

public Configuration(Vector vars, Vector vals) {
  
  variables = vars;
  values = vals;
}  


/**
 * Creates a configuration for the variables given with all
 * the values set to zero, except for those variables which
 * are also in the argument configuration, whose value
 * will be equal to the one they have in the argument
 * configuration.
 * @param vars a vector of <code>FiniteStates</code>.
 * @param conf a <code>Configuration</code>.
 */

public Configuration(Vector vars, Configuration conf) {

  Integer val;
  FiniteStates aux;
  int i, x, pos;

  variables = (Vector)vars.clone();
  values = new Vector();

  for (i=0 ; i<vars.size() ; i++) {
    aux = (FiniteStates)vars.elementAt(i);
    pos = conf.indexOf(aux.getName());
    
    // If the variable is in conf, keep its old value.
    if (pos >= 0)
      x = ((Integer)conf.values.elementAt(pos)).intValue();
    else
      x = 0;

    val = new Integer(x);
    values.addElement(val);
  }
}


/**
 * Creates a configuration for the variables given with all
 * the values set to zero, except for those variables which
 * are also in the argument configuration, whose value will
 * be equal to the one they have in the argument configuration.
 * The search is done by name (true) or by reference (false).
 * @param vars a <code>Vector</code> of <code>FiniteStates</code>.
 * @param conf a <code>Configuration</code>.
 * @param byName a boolean indicating the type of search. 
 */

public Configuration(Vector vars, Configuration conf, boolean byName) {
   
   Integer val;
   FiniteStates aux;
   int i, x, pos;

   variables = (Vector)vars.clone();
   values = new Vector();

   for (i=0 ; i<vars.size() ; i++) {
      aux = (FiniteStates)vars.elementAt(i);
      if (byName)
	pos = conf.indexOf(aux.getName());
      else
	pos = conf.indexOf(aux);

      // If the variable is in conf, keep its old value.
      if (pos >= 0)
	x = ((Integer)conf.values.elementAt(pos)).intValue();
      else
	x = 0;
      
      val = new Integer(x);
      values.addElement(val); 
   }
}


/**
 * Creates a new configuration equal to the one passed as
 * argument, but dropping those variables contained in the
 * argument vector.
 * @param conf a <code>Configuration</code>.
 * @param vars a <code>Vector</code> of <code>FiniteStates</code>.
 */

public Configuration(Configuration conf, Vector vars) {

  int i;
  Integer val;
  FiniteStates temp;

  variables = new Vector();
  values = new Vector();

  for (i=0 ; i<conf.variables.size() ; i++) {
    temp = (FiniteStates)conf.variables.elementAt(i);
    
    // If the variable is not in vars, take it.
    if (vars.indexOf(temp) == -1) {
      variables.addElement(temp);
      val = new Integer(((Integer)conf.values.elementAt(i)).intValue());
      values.addElement(val);
    }
  }
}


/**
 * Creates a new configuration equal to the one passed as
 * argument, but only with the variables pased as argument, and
 * mantaining the order in that nodeList
 * @param conf a <code>Configuration</code>.
 * @param nl a <code>NodeList</code> of <code>FiniteStates</code>.
 */

public Configuration(Configuration conf, NodeList nl) {

  int i, pos;
  Integer val;
  FiniteStates temp;

  variables = new Vector();
  values = new Vector();

  for (i=0 ; i<nl.size() ; i++) {
    temp = (FiniteStates)nl.elementAt(i);

    // if the variable is in conf take it
    pos = conf.getVariables().indexOf(temp);
    if (pos != -1) {
      variables.addElement(temp);
      val = new Integer(((Integer)conf.values.elementAt(pos)).intValue());
      values.addElement(val);
    }
  }
}

/**
 * Creates a new configuration equal to the two configurations passed as
 * argument, but only with the variables passed in the <code>NodeList</code>
 * and mantaining the order of the variables in that <code>NodeList</code>.
 * the first argument configuration prevails over the second one.
 * @param conf1 the first <code>Configuration</code>.
 * @param conf2 the second <code>Configuration</code>.
 * @param nl a <code>NodeList</code>.
 */

public Configuration(Configuration conf1, Configuration conf2, NodeList nl) {

   int i, pos;
   FiniteStates temp;
   Integer val;

   variables = new Vector();
   values = new Vector();

   for (i=0 ; i<nl.size() ; i++) {
      temp = (FiniteStates)nl.elementAt(i);
      pos = conf1.indexOf(temp.getName());

      if (pos >= 0)
	val = new Integer (((Integer)conf1.values.elementAt(pos)).intValue());
      else {
	pos = conf2.indexOf(temp.getName());
	if (pos >= 0)
	  val = new Integer (((Integer)conf2.values.elementAt(pos)).intValue());
	else
	  val = new Integer(0);
      }
      variables.addElement(temp);
      values.addElement(val);
   }
}

/** Create a new configuration for the variables passed as first argument,
 * but dropping variables included in the configuration passed as second
 * argument
 * @param toDrop variables to drop
 * @param vars variables to make the configuration
 * @param val value to initialize the configuration 
 */

public Configuration(Configuration toDrop, Vector vars, int val){
  Node node;
  Integer value;
  int i;

  variables=new Vector();
  values=new Vector();

  // Consider the variables in vars

  for(i=0; i < vars.size(); i++){
    node=(Node)vars.elementAt(i);
    if (toDrop.indexOf(node) == -1){
      variables.add(node);
    }
  }

  for(i=0; i < variables.size(); i++){
    value=new Integer(val);
    values.addElement(value);
  }
}

/**
 * Create a new configuration for the variables passed as first argument,
 * but removing the variable which name appears as second argument
 * @param vars to be included in the configuration
 * @param name var to exclude in the configuration (must be in vars)
 */

public Configuration(Vector vars, String name){
  Node var;
  boolean found=false;

  // Create the vectors for variables and values
  variables=new Vector();
  values=new Vector();
  
  // Go on the vector vars to include the variables, except the
  // one which names matches with name, into the finalVars vector
  for(int i=0; i < vars.size(); i++) {
    // Get the the variable in the current position
    var=(Node)vars.elementAt(i);
    
    // Compares it name with the name passed as argument
    if (name.equals(var.getName()) == false){
      variables.addElement(var);
      values.addElement(new Integer(0));
    }
    else {
      found=true;
    }
  }
  
  // Finally, check if found is true. If not, there was a problem
  // in the method call
  if (found == false) {
    System.out.println("Error in elvira.Configuration:");
    System.out.println("method:  Configuration(Vector vars, String name");
    System.out.println("The variable named "+ name+ " does not appear in vector");
    System.exit(0);
  }
}
 
 
/**
 * Constructs a configuration from an evidence.
 * It works with discrete variables
 * @param conf the evidence.
 */

public Configuration(Evidence evi) {

  variables = (Vector) evi.getVariables().clone();
  values = (Vector) evi.getValues().clone();
  
}



/**
 * Sets the values for the configuration.
 * @param v the vector containing the values.
 */

public void setValues(Vector v) {
  
  values = v;
}

/**
 * Sets the values for the configuration, from the
 * values of conf1 and conf2
 * @param <code>Configuration</code> conf1
 * @param <code>Configuration</code> conf2
 */

public void setValues(Configuration conf1, Configuration conf2){
  int index;
  int i;
  int val;
  FiniteStates var;

  // Copy the values from conf1
  
  for(i=0; i < conf1.size(); i++){
    var=conf1.getVariable(i);
    val=conf1.getValue(i);
    putValue(var,val);
  }

  // Copy the values from conf2
  
  for(i=0; i < conf2.size(); i++){
    var=conf2.getVariable(i);
    val=conf2.getValue(i);
    putValue(var,val);
  }
}


/**
 * Saves this configuration in a file.
 * @param p the <code>PrintWriter</code> where the configuration
 * will be written.
 */

public void save(PrintWriter p) {
 
 int i, j, k;
 String name; 
 
 p.print("[");
 
 j = values.size();
 
 for (i=0 ; i<j ; i++) {
   name = new String((String) 
		     ((FiniteStates)variables.elementAt(i)).
		     getPrintableState(((Integer)values.elementAt(i)).intValue()));
   p.print(name);
   /*
   try {
     k = Integer.parseInt(name);
     p.print("\""+name+"\"");
   }
   catch (NumberFormatException e) {
     p.print(name);
   }*/	      
   if (i == j-1) {
     p.print("]");
   }
   else {
     p.print(",");
   }
 }		      
}


/**
 * Gets a vector with the states of the configuration
 */

public Vector<Integer> getStates() {
 
 int i, j, k;
 String name; 
 
 Vector v = new Vector();
 
 
 j = values.size();
 
 for (i=0 ; i<j ; i++) {
        v.add(Integer.parseInt(((FiniteStates)variables.elementAt(i)).getPrintableState(((Integer)values.elementAt(i)).intValue())));

 }
 
 
 return v;
 
}




/**
 * Prints this configuration to the standard output.
 */

public void print() {
  
  int state, i, j ,k;
  String name; 
  
  System.out.print("[");
  
  j = values.size();
  
  for (i=0 ; i<j ; i++) {

    // Get the value for the variable. If it is defined, get the name for the
    // state. Anyother way, put X

    state = ((Integer)values.elementAt(i)).intValue();
    
    if (state != -1){
      name = new String((String)((FiniteStates)variables.elementAt(i)).getPrintableState(state));
    }
    else{
      name = new String("X");
    }

    System.out.print(name);
    /*
    try {
      k = Integer.parseInt(name);
      System.out.print("\""+name+"\"");
    }
    catch (NumberFormatException e) {
      System.out.print(name);
    }
     */	      
    if (i == j-1) {
      System.out.print("]");
    }
    else {
      System.out.print(",");
    }
  }		      
}


/**
 * Prints this configuration to the standard output.
 * pPrint is a shortname for PrettyPrint.
 */

public void pPrint() {
  
  int i, j, k;
  String name; 
  
  System.out.print("[");
  
  j = values.size();
  
  for (i=0 ; i<j ; i++) {
    if (variables.elementAt(i).getClass()==FiniteStates.class)
      name = new String((String) 
		      ((FiniteStates)variables.elementAt(i)).
		      getState(((Integer)values.elementAt(i)).intValue()));
    else
      name = new String("X");
    
    try {
      k = Integer.parseInt(name);
      System.out.print("\""+name+"\"");
    }
    catch (NumberFormatException e) {
      System.out.print(getVariable(i).getName()+"="+name);
    }	      
    if (i == j-1) {
      System.out.print("]");
    }
    else {
      System.out.print(",");
    }
  }		      
}


/**
 * Generates a string in the same way method <code>pPrint</code>.
 * @return the string generated.
 */

public String toString() {
  
  StringBuffer sb = new StringBuffer("");
  int i, j, k;
  String name; 
  
  sb.append("[");
  
  j = values.size();
  
  if (j == 0)
    sb.append("No Evidence");
  else {
    for (i=0 ; i<j ; i++) {
      name = new String((String) ((FiniteStates)variables.elementAt(i)).getState(((Integer)
			values.elementAt(i)).intValue()));
      try {
	k = Integer.parseInt(name);
	sb.append("\""+name+"\"");
      }
      catch (NumberFormatException e) {
	sb.append(getVariable(i).getNodeString(true)+"="+name);
      }	      
      if (i == j-1) 
	sb.append("]");
      else
	sb.append(",\n");
    }
  }
  
  return sb.toString();
}


/**
 * Creates a copy of this configuration. Both vectors
 * <code>variables</code> and <code>values</code> will be shared.
 * @return a <code>Configuration</code> with the copy.
 */

public Configuration copy() {

  Configuration aux;

  aux = new Configuration(variables,values);
  
  return aux;
}


/**
 * Creates a deep copy of this configuration. Both vectors
 * <code>variables</code> and <code>values</code> will be copied.
 * @return a <code>Configuration</code> with the copy.
 */

public Configuration duplicate() {

  Configuration aux;

  aux = new Configuration();
  aux.values = (Vector) values.clone();
  aux.variables = (Vector) variables.clone();
  return aux;
}


/**
 * Modifies this configuration and makes it be equal to
 * the next. For example, if a configuration for two binary
 * variables has values (0,0), the next would be (0,1),
 * the next (1,0) and so on.
 */

public void nextConfiguration() {

  int i;
  int carry = 1;
  FiniteStates aux;
  Integer valI;
  Integer valJ;

  for (i=variables.size()-1 ; i>=0 ; i--) {
    
    if (carry == 0) // It is done.
      break;
    
    aux = (FiniteStates)variables.elementAt(i);
    valJ = (Integer)values.elementAt(i);
    
    // If the variable is in its last value,
    // set it to 0 and carry 1.
    if ((aux.getNumStates()-1) == valJ.intValue()) {
      valI = new Integer("0");
      values.setElementAt(valI,i);
      carry = 1;
    }
    // Otherwise, increment its value and carry 0
    else {
      valI = new Integer(valJ.intValue()+1);
      values.setElementAt(valI,i);
      carry = 0;
    }
  }
}


/**
 * Computes the next configuration from this, and the result is
 * stored in this. In this case the variables contained in
 * the argument configuration remain unchanged.
 * @param conf a <code>Configuration</code> with the variables that must
 * remain unchanged.
 */

public void nextConfiguration(Configuration conf) {

  int i, pos, carry = 1;
  FiniteStates aux;
  Integer valI;
  Integer valJ;

  for (i=variables.size()-1 ; i>=0 ; i--) {
    
    if (carry == 0) // It is done.
      break;
    
    aux = (FiniteStates)variables.elementAt(i);
    valJ = (Integer)values.elementAt(i);

    pos = conf.indexOf(aux);

    // If the variable is in conf, continue
    if (pos >= 0)
      continue;
    
    // If the variable is in its last value,
    // set it to 0 and carry 1.
    if ((aux.getNumStates()-1) == valJ.intValue()) {
      valI = new Integer("0");
      values.setElementAt(valI,i);
      carry = 1;
    }
    // Otherwise, increment its value and carry 0
    else {
      valI = new Integer(valJ.intValue()+1);
      values.setElementAt(valI,i);
      carry = 0;
    }
  }
}


/**
 * Computes the index in the array of values of a potential
 * corresponding to this configuration. For example, if a
 * configuration for two variables with i and j cases respectively
 * is l,m then the position is l*j+m.
 * @return the index in the array of values of a <code>PotentialTable</code>
 * corresponding to this configuration.
 */

public int getIndexInTable() {

  int i, nv, pos, numVal;
  FiniteStates aux;
  Integer temp;

  if (variables.size() > 0) {
    nv = variables.size(); // Number of variables.
    
    aux = (FiniteStates)variables.elementAt(nv-1); // The last one.
    temp = (Integer)values.elementAt(nv-1); // its value.

    numVal = aux.getNumStates();
    pos = temp.intValue();
  
    // Computes position
    for (i=nv-2 ; i>=0 ; i--) {
      aux = (FiniteStates)variables.elementAt(i);
      temp = (Integer)values.elementAt(i);

      pos += (temp.intValue()) * numVal;
      numVal *= aux.getNumStates();
    }
  }
  else {
    pos = 0;
  }

  return pos;
}

/**
 * Method to jump to a combination of values for the variables
 * according to a given index
 * @param index index of the configuration to reach
 */
public void goToConfiguration(long index){
  // Determine the values for the variables from the index
  for(int i=variables.size()-1; i >= 0; i--){
  	FiniteStates var=(FiniteStates)variables.elementAt(i);
  	
  	// Get the number of values for this variables
  	int values=var.getNumStates();
  	int value=(int)index%values;

  	// Divide the index by the number of values. The rest
  	// will be used to keep the divisions
  	putValue(var,value);
  	index=index/values;
  }
}


/* Old hashCode
/**
 * Calculates a hash code for this configuration. The code is
 * computed as the sum of the hash codes of each of the variables
 * and their values.
 *
 * @return the hash code.
 
public int hashCode() {

  int i, code = 0;
  
  for (i=0 ; i<variables.size() ; i++) {
    code += (getVariable(i).hashCode() + getValue(i));
  }
  
  return (code);
}*/


/**
 * Calculates a hash code for this configuration. The code is
 * computed as the sum of the hash codes of each of the variables
 * and their values.
 *
 * @return the hash code.
 */

public int hashCode() {

  int i, code = 0;
  
  for (i=0 ; i<variables.size() ; i++) 
  {
    FiniteStates var = this.getVariable(i);    
    code           += (var.hashCode() * getValue(i));
    //code += (getVariable(i).hashCode() + getValue(i));
  }  
  return (code);
}

/**
 * Build a string concatenating the values in the configuration. It 
 * can be used as key for a has table
 *
 * @return a <code>String</code>.
 */

public String stringValues() {
  String s = new String();
  
  for (int i=0 ; i<values.size() ; i++) {
    s += getValue(i);
  }
  
  return s;
}


/**
 * Gets the position of a variable in a configuration.
 * @param var a variable (<code>Node</code>).
 * @returns the position of <code>var</code> in the list of variables.
 * @returns -1 if var is not contained in the list.
 */

public int indexOf(Node var) {
  
  int i;
  Node aux;
  for (i=0 ; i<variables.size() ; i++) {
    aux = (Node)variables.elementAt(i);
    if (aux.equals(var))
      return i;
  }
  
  return (-1);
}


/**
 * Gets the position of a variable in a configuration.
 * @param name the name of the node to search for.
 * @returns the position of variable <code>name</code> in the list of
 * variables.
 * @returns -1 if var is not contained in the list.
 */

public int indexOf(String name) {
   
  int i;
  Node aux;
  
  for (i=0 ; i<variables.size() ; i++) {
    aux = (Node)variables.elementAt(i);
    if (name.compareTo(aux.getName()) == 0)
      return i;
  }
  return (-1);
}


/**
 * Inserts a pair (variable,value) at the end of the configuration.
 * @param var a <code>FiniteStates</code> variable.
 * @param val an integer value.
 */

public void insert(FiniteStates var, int val) {

  Integer i;

  variables.addElement(var);
  i = new Integer(val);
  values.addElement(i);
}


/**
 * Puts a pair (variable,value) at the configuration. If the variable is
 * already in the configuration, sets the value in the position of the
 * variable, otherwise, inserts (variable,value) at the end of the
 * configuration.
 * @param var a <code>FiniteStates</code> variable.
 * @param val an integer value.
 */

public void putValue(FiniteStates var, int val) {
  
  Integer i;
  int pos;
  
  pos = indexOf(var);
  
  if (pos == -1)
    insert(var,val);
  else {
    i = new Integer(val);
    values.setElementAt(i,pos);
  }
}

/**
 * Replaces the value for a given position.
 * @param pos an <code>int</code> 
 * @param val an integer value.
 */

public void setValue(int pos, int val){
  values.setElementAt(new Integer(val),pos);
}


/**
 * Puts a pair (variable,value) at the configuration in position p. If the variable is
 * already in the configuration, sets the value in the position of the
 * variable, otherwise, inserts (variable,value) at the end of the
 * configuration.
 * @param var a <code>FiniteStates</code> variable.
 * @param val an integer value.
 */

public void putValueAt(FiniteStates var, int val, int p) {
  
  Integer i;
  int pos;
  
  pos = indexOf(var);
  
  i = new Integer(val);  
  if (pos == -1){
    variables.insertElementAt(var,p);
    values.insertElementAt(i,p);
  }
  else {
    values.setElementAt(i,pos);
  }
}

/**
 * Puts a pair (variable,value) at the configuration. If the variable is in
 * cofiguration, sets the value in the position of the variable having the
 * specified name. Otherwise it does not do anything.
 * @param name the name of a <code>FiniteStates</code> variable.
 * @param val an integer value.
 */

public void putValue(String name, int val) {

  Integer i;
  int pos;
  
  pos = indexOf(name);
  
  if (pos != -1) {
    i = new Integer(val);
    values.setElementAt(i,pos);
  }
}


/**
 * Removes a pair (variable,value).
 * @param position the position of the pair to remove.
 * If <code>position</code> is outside the range of the list, an error
 * is produced.
 */

public void remove(int position) {

  variables.removeElementAt(position);
  values.removeElementAt(position);
}


/**
 * Removes a pair (variable,value).
 * @param <code>node</code> to remove. It's removed the first ocurrence.
 * If <code>position</code> is outside the range of the list, an error
 * is produced.
 */

public void remove(Node node) {
  
  remove(indexOf(node));
}


/**
 * Returns the value of the variable stored in a position.
 * @param position the position.
 * @return the value of the variable at position <code>position</code>.
 */

public int getValue(int position) {
  
  if (position >= 0)
    return ((Integer)values.elementAt(position)).intValue();
  else
    return -1;
}


/**
 * Returns the value of a given variable.
 * @param var the variable.
 * @return the value of variable <code>var</code>.
 */

public int getValue(FiniteStates var) {

  if (indexOf(var) != -1)  
    return getValue(indexOf(var));
  else
    return -1;
}


/**
 * Returns the value of a given variable.
 * @param name the variable name.
 * @return the value of variable <code>name</code>.
 */

public int getValue(String name) {

  return getValue(indexOf(name));
}


/**
 * Returns the variable stored in a position.
 * @param position the position.
 * @return the variable at position <code>position</code>.
 */

public FiniteStates getVariable(int position) {

  return (FiniteStates)variables.elementAt(position);
}


/**
 * Gets the number of variables in thi configuration.
 * @return the number of variables in the configuration.
 */

public int size() {
 
  return variables.size();
}

/**
 * Gets the number of possible values for a configuration
 * @return the cardinal of the cartesian product of the variables
 */

public int possibleValues() {
  int i;
  int nv=1;
  FiniteStates var;

  for(i=0; i < variables.size(); i++){
    var=(FiniteStates)variables.elementAt(i);
    nv*=var.getNumStates();
  }

  return nv;
}

/**
 * Gets the number of possible values for a configuration - using a long to represent
 * the number of possible values. Should be used for configurations with many variables.
 * @return the cardinal of the cartesian product of the variables
 */

public long saferPossibleValues() {
  int i;
  long nv=1;
  FiniteStates var;

  for(i=0; i < variables.size(); i++){
    var=(FiniteStates)variables.elementAt(i);
    nv*=var.getNumStates();
  }
  return nv;
}


/**
 * Gets the variables in the configuration.
 * @return the variables in the configuration.
 */

public Vector getVariables() {
 
  return variables;
}


/**
 * Gets the values in the configuration.
 * @return the values in the configuration.
 */

public Vector getValues() {
 
  return values;
}

/**
 * Compares two configurations.
 * @param conf a configuration.
 * @return <code>true</code> if <code>conf</code> is
 * compatible (does not have different variables neither different
 * values for the variables in common and <code>false</code> in other case.
 */

public boolean isCompatible(Configuration conf) {
  
  int i, j; 
  FiniteStates fs;
  
  for (i=0 ; i<size() ; i++) {
      fs = getVariable(i);
      j = conf.indexOf(fs);
      if (j == -1) {
	     return false;		  
      }else {
        if (getValue(i) != conf.getValue(j)){
			  //System.out.println("valores distintos: "+ fs.getName() + " = " + getValue(i) + " y " +conf.getValue(j));
	     	  return false;
		  }
      }
  }
  //System.out.println("Compatibles");	     
  return true;
}

/**
 * Compares two configurations.
 * @param conf a configuration.
 * @return <code>true</code> if <code>conf</code> is
 * compatible (does not have different
 * values for the variables in common and <code>false</code> in other case.
 */

public boolean isCompatibleWeak(Configuration conf) {
  
  int i, j; 
  FiniteStates fs;
  
  for (i=0 ; i<size() ; i++) {
      fs = getVariable(i);
      j = conf.indexOf(fs);
      if (j != -1)  {
        if (getValue(i) != conf.getValue(j)){
			  //System.out.println("valores distintos: "+ fs.getName() + " = " + getValue(i) + " y " +conf.getValue(j));
	     	  return false;
		  }
      }
  }
  //System.out.println("Compatibles");	     
  return true;
}
/**
 * Compares two configurations.
 * @param conf a configuration.
 * @return <code>true</code> if <code>conf</code> is
 * equal to the configuration which receives the message
 * and <code>false</code> in other case.
 */

public boolean equals(Object conf) 
{
  if(conf != null && conf instanceof Configuration)
  {
    return this.equals((Configuration) conf);
  }  
  // false if the Object is null or it is not a Configuration
  return false;  
}


/**
 * Compares two configurations.
 * @param conf a configuration.
 * @return <code>true</code> if <code>conf</code> is
 * equal to the configuration which receives the message
 * and <code>false</code> in other case.
 */

public boolean equals(Configuration conf) {
  
  int i, j; 
  FiniteStates fs;
  
  if (size() != conf.size())
    return false;
  else {
    for (i=0 ; i<size() ; i++) {
      fs = getVariable(i);
      j = conf.indexOf(fs);
      if (j == -1)
	return false;
      else {
        if (getValue(i) != conf.getValue(j))
	  return false;
      }
    }
  }
  
  return true;
}


/**
 * Determines whither this configuration is contained in a vector.
 * @param v the vector of configurations.
 * @return <code>true</code> if this configuration is in vector
 * <code>v</cde>.
 */

public boolean contained(Vector v) {

  int i;
  Configuration conf;
  
  for (i=0 ; i<v.size() ; i++) {
    conf = (Configuration) v.elementAt(i);
    if (this.equals(conf))
      return true;
  }

  return false;
}


/**
 * @return the first configuration from the set of configurations
 * represented by the object which receives the message (-1 stands for every
 * value) that is not included in list.
 * If there isn't any valid configuration, an empty configuration is returned. 
 */

public Configuration getFirstNotInList(Vector list) {
  
  int i, j;
  long s;
  Vector v; // the vector with all the variables with -1 as value 
  Configuration subConf, conf;
  Vector vals;
  boolean found = false;  
  
  
  v = new Vector();
  for (i=0 ; i<this.size() ; i++)
    if (this.getValue(i) == -1)
      v.addElement(this.getVariable(i));
  
  subConf = new Configuration(v);
  s = (long) FiniteStates.getSize(v);
  if (subConf.size() == 0) {
    if (!this.contained(list))
      return this;
  }
  else {

    // creating a clone of this
    vals = new Vector();
    for (j=0 ; j<values.size() ; j++)
      vals.addElement(new Integer(((Integer)values.elementAt(j)).intValue()));
    conf = new Configuration(this.getVariables(),vals);
  
    for (i=0 ; i<s ; i++) {
      
      // setting in conf the values of subConf 
      for (j=0 ; j<subConf.size() ; j++) {
	conf.putValue(subConf.getVariable(j),subConf.getValue(j));
      }
      // searching if subConf is contained in list      
      if (!conf.contained(list))
	return conf;
      else
	subConf.nextConfiguration();
    }
  }
  
  return new Configuration();
}


/**
 * Resets a configuration (the object which receives the message)
 * with all the values set to zero, except for those variables which 
 * are also in the argument configuration, whose value will be equal 
 * to the one they have in this argument.
 * 
 * @param conf a <code>Configuration</code>.
 */

public void resetConfiguration(Configuration conf) {

  FiniteStates aux;
  int i, pos, val;

  for (i=0 ; i<this.size() ; i++) {
    aux = (FiniteStates)this.variables.elementAt(i);

    // With this change we can remove all the values

    if (conf != null)
      pos = conf.indexOf(aux);
     else
      pos=-1;
    
    // If the variable is in conf, keep its old value.
    if (pos >= 0)
      val = ((Integer)conf.values.elementAt(pos)).intValue();
    else
      val = 0;

    this.values.setElementAt(new Integer(val),i);
  }
}


/**
 * Creates a new configuration with the same content
 * but in reverse order.
 * @return a <code>Configuration</code> with the reversed copy.
 */

public Configuration reverse() {

  Configuration aux;
  int i, s;

  s = this.size();
  aux = new Configuration();
  
  for (i=(s-1) ; i >=0 ; i--) {
      aux.insert(this.getVariable(i),this.getValue(i));
  }
  return aux;
}

/**
 * This method removes the undifined values that could be in
 * the Configuration object.
 */
public void removeUndefinedValues(){
    Vector<Node> remove=new Vector<Node>();
    for (int i=0; i<this.variables.size(); i++)
        if (this.getValue(i)==(int)this.getVariable(i).undefValue())
            remove.addElement(this.getVariable(i));
    for (int i=0; i<remove.size(); i++)
        this.remove(remove.elementAt(i));
}

} // End of class
