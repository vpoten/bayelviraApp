/* ContinuousIntervalConfiguration.java */

package elvira;

import java.util.Vector;
import elvira.Continuous;
import java.io.*;

/**
 * Implements a configuration of intervals for continuous variables.
 *
 * @since 21/9/2000
 */

public class ContinuousIntervalConfiguration {

/**
 * Continuous variables in the configuration.
 */
Vector variables;

/**
 * Vector of lower limits of the intervals.
 */
Vector inf;

/**
 * Vector of upper limits of the intervals.
 */
Vector sup;


/**
 * Creates an empty configuration.
 */

public ContinuousIntervalConfiguration() {
  
  variables = new Vector();
  inf = new Vector();
  sup = new Vector();
}


/** 
 * It makes a deep copy of an interval configuration.
 * @param c the interval configuration to be duplicated.
 * @return the deep copy of the configuration.
 */

public ContinuousIntervalConfiguration duplicate() {
  
  ContinuousIntervalConfiguration aux;
  
  aux = new ContinuousIntervalConfiguration();
  aux.variables = (Vector) variables.clone();
  aux.inf = (Vector) inf.clone();
  aux.sup = (Vector) sup.clone();
  return aux;
}


/**
 * Gets the position of a variable in an interval configuration.
 * @param var a variable (<code>Node</code>).
 * @returns the position of var in the list of variables.
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
 * Gets the position of a variable in this configuration.
 * @param name the name of the node to search for.
 * @returns the position of <code>name</code> in the list of variables.
 * @returns -1 if <code>name</code> is not contained in the list.
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
 * Inserts an element (variable,infvalue,supvalue) at the end of the interval
 * configuration. If the variable is in the configuration, then it is
 * calculated the intersection of the old and new intervals.
 * If the intersection is empty, then the new interval and the
 * configuration are said to be inconsistent.
 * @param var a <code>Continuous</code> variable.
 * @param x a double representing the lower limit of the interval.
 * @param y a double representing the upper limit of the interval
 * @return a boolean saying whether the new interval is consistent with
 * the interval configuration (<code>true</code>) or inconsistent
 * (<code>false</code>).
 */

public boolean putValue(Continuous var, double x, double y) {
  
  boolean consistent;
  double x1, y1;
  int i;
  consistent = true;
  if (y <= x)
    consistent = false;
  else {
    i = indexOf(var);
    if (i == -1) {
      variables.addElement(var);
      inf.addElement(new Double(x));
      sup.addElement(new Double(y));
      
    }
    else { 
      x1 = ( (Double) inf.elementAt(i)).doubleValue();
      y1 = ( (Double) sup.elementAt(i)).doubleValue();
      if (x < x1)
	x = x1;
      if (y > y1)
	y = y1;
      if (x >= y) 
	consistent = false;
      else {
	inf.setElementAt( new Double(x),i);
	sup.setElementAt( new Double(y),i);
      }
    }
  }

  return consistent;
}


/**
 * Determines whether a new interval (variable,infvalue,supvalue)
 * is consistent with and interval configuration. It is similar to
 * <code>putValue</code> with the difference that here
 * the element is not added. Only consistency is checked.
 * @param var a <code>Continuous</code> variable.
 * @param x a double representing the lower limit of the interval.
 * @param y a double representing the upper limit of the interval
 * @return a boolean saying whether the new interval is consistent with
 * the interval configuration (<code>true</code>) or inconsistent
 * (<code>false</code>).
 */

public boolean consistent(Continuous var, double x, double y) {

  boolean consistent;
  double x1, y1;
  int i;

  consistent = true;
  if (y <= x)
    consistent = false;
  else {
    i = indexOf(var);
    if (i != -1) {
      x1 = ( (Double) inf.elementAt(i)).doubleValue();
      y1 = ( (Double) sup.elementAt(i)).doubleValue();
      if (x < x1) 
	x = x1;
      if (y > y1)
	y = y1;
      if (x >= y) 
	consistent = false;
    }
  }

  return consistent;
}


/**
 * Removes a set (variable,inf,sup).
 * @param position the position of the interval to remove.
 * If <code>position</code> is outside the range of the list, an error
 * is produced.
 */

public void remove(int position) {

  variables.removeElementAt(position);
  inf.removeElementAt(position);
  sup.removeElementAt(position);
}


/**
 * Returns the lower limit of the interval of the variable stored in a
 * position.
 * @param position the position.
 * @return the lower limit of the interval of the variable at
 * position <code>position</code>.
 */

public double getLowerValue(int position) {
  
  if (position >= 0)
    return ((Double)inf.elementAt(position)).doubleValue();
  else
    return -1.0;
}


/**
 * Returns the upper limit of the interval of the variable stored in
 * a position.
 * @param position the position.
 * @return the upper limit of the interval of the variable at
 * position <code>position</code>.
 */

public double getUpperValue(int position) {

  if (position >= 0)
    return ((Double)sup.elementAt(position)).doubleValue();
  else
    return -2.0;
}


/**
 * Returns the variable stored in a position.
 * @param position the position.
 * @return the variable at that position.
 */

public Continuous getVariable(int position) {
  
  return (Continuous)variables.elementAt(position);
}


/**
 * Gets the size of the configuration.
 * @return the number of variables in the configuration.
 */

public int size() {
 
  return variables.size();
}


/**
 * Gets the variables in the configuration.
 * @return the variables in the configuration.
 */

public Vector getVariables() {
 
  return variables;
}




/** 
 * Prints the Configuration to the standard output
 *
 *
 */

public void print(){

int s,i;

s = variables.size();

if (s == 0)
    System.out.println("This continuous configuration is empty");
else{
  System.out.println("This continuous configuration has the following elements: ");
  for (i = 0 ; i < s ; i++){
    System.out.print("Variable: ");
    getVariable(i).print();
    System.out.println("MIN: "+getLowerValue(i)+". MAX: "+getUpperValue(i));
    System.out.println();
  }
}

}

} // End of class
