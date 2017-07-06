/* ContinuousConfiguration.java */

package elvira;

import java.util.Vector;
import elvira.Node;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Continuous;
import java.io.*;


/**
 * Implements a configuration of variables, allowing both
 * discrete (<code>FiniteStates</code>) and continuous
 * (<code>Continuous</code>) variables.
 * Two new fields are added to class <code>Configuration</code>: a vector of
 * continuous variables and a vector with their values.
 *
 * @since 25/5/2007
 */

public class ContinuousConfiguration extends Configuration {
  
/**
 * Continuous variables in the configuration.
 */
Vector continuousVariables;

/**
 * Values of the continuous variables.
 */
Vector continuousValues;


/**
 * Creates an empty configuration.
 */

public ContinuousConfiguration() {
  continuousVariables = new Vector();
  continuousValues = new Vector();
}
  
/**
 * Creates a configuration for a list of variables with the
 * values equal to zero. Variables are not duplicated. Just references
 * are copied.
 * @param vars a vector of variables.
 */

public ContinuousConfiguration(Vector vars) {
  this();
  
  Integer iVal;
  Double dVal;
  Node n;
  int i;

  for (i=0 ; i<vars.size() ; i++) {
    n = (Node)vars.elementAt(i);
    if (n.getTypeOfVariable() == Node.CONTINUOUS) {
      dVal = new Double(0.0);
      continuousVariables.addElement(n);
      continuousValues.addElement(dVal);
    }
    else if (n.getTypeOfVariable() == Node.FINITE_STATES) {
      iVal = new Integer(0);
      variables.addElement(n);
      values.addElement(iVal);
    }
  }
}

/**
 * Creates a configuration for the <code>Vector</code> of variables vars.
 * The value for variable in position i of vars, will be taken from the
 * position i in <code>Vector vals</code>.
 * Variables and values are not duplicated. Just references are copied.
 * @param vars a Vector of variables.
 * @param vals a Vector of values (<code>Integer</code> or <code>Double</code>)
 */

public ContinuousConfiguration(Vector vars,Vector vals) {
  this();
  Integer iVal;
  Double dVal;
  Node n;
  int i;

  for (i=0 ; i<vars.size() ; i++) {
    n = (Node)vars.elementAt(i);
    if (n.getTypeOfVariable() == Node.CONTINUOUS) {
      dVal = (Double)vals.elementAt(i);
      continuousVariables.addElement(n);
      continuousValues.addElement(dVal);
    }
    else if (n.getTypeOfVariable() == Node.FINITE_STATES) {
      iVal = (Integer)vals.elementAt(i);
      variables.addElement(n);
      values.addElement(iVal);
    }
  }  
}

/**
 * Creates a configuration with the contained values in a <code>Configuration</code> object.
 * So the new ContinuousConfiguration instance only has values for discrete variables.
 * @param conf a <code>Configuration</code> object.
 */
public ContinuousConfiguration(Configuration conf) {
    super(conf.getVariables(),conf.getValues());
    continuousVariables = new Vector();
    continuousValues = new Vector();
}
/**
 * Creates a configuration for a list of variables with the
 * values equal to zero. Variables are not duplicated. Just references
 * are copied.
 * @param vars a list of variables (<code>NodeList</code>).
 */

public ContinuousConfiguration(NodeList vars) {
  this(vars.getNodes());
}

/**
 * Sets the values for the continuous variables in the configuration.
 * @param v the vector containing the values.
 */

public void setContinuousValues(Vector v) {
  continuousValues = v;
}

/**
 * Sets the values for the configuration, from the
 * values of conf1 and conf2
 * @param <code>ContinuousConfiguration</code> conf1
 * @param <code>ContinuousConfiguration</code> conf2
 */

public void setContinuousValues(ContinuousConfiguration conf1, ContinuousConfiguration conf2){
  int index;
  int i;
  double val;
  Continuous var;

  // Copy the values from conf1
  
  for(i=0; i < conf1.size(); i++){
    var=conf1.getContinuousVariable(i);
    val=conf1.getContinuousValue(i);
    putValue(var,val);
  }

  // Copy the values from conf2
  
  for(i=0; i < conf2.size(); i++){
    var=conf2.getContinuousVariable(i);
    val=conf2.getContinuousValue(i);
    putValue(var,val);
  }
}


/**
 * Returns a vector with all the continuous variables in the configuration
 * @return a vector of continuous variables.
 */ 

public Vector getContinuousVariables() {
  return continuousVariables;
}

/**
 * Returns the variable stored in a position.
 * @param position the position.
 * @return the variable at position <code>position</code>.
 */

public Continuous getContinuousVariable(int position) {

  return (Continuous)continuousVariables.elementAt(position);
}

/**
 * Returns a vector with all the continuous values in the configuration
 * @return a vector of continuous values.
 */ 

public Vector getContinuousValues() {
  return continuousValues;
}


/**
 * Returns the value of the continuous variable stored in a given position.
 * @param position the position.
 * @return the value of the variable at position <code>position</code>.
 */

public double getContinuousValue(int position) {
  return ((Double)continuousValues.elementAt(position)).doubleValue();
}

/**
 * Returns the value of a given variable.
 * @param var the variable.
 * @return the value of variable <code>var</code>.
 */

public double getContinuousValue(Continuous var) {

  if (indexOf(var) != -1)  
    return getContinuousValue(getIndex(var));
  else
    return -1;
}


public int indexOf(Node node){
 
    if(node.getClass()==FiniteStates.class){
            return super.indexOf(node);
    }else
            return getIndex((Continuous)node);
    
}
/**
 * Returns the position of a continuous variable in the vector
 * of continuous variables, or (-1) if it is not present.
 * @param var the variable (<code>Continuous</code>).
 * @return the position of the continuous variable.
 */

public int getIndex(Continuous var) {
   for (int i=0; i<continuousVariables.size(); i++)
        if (((Node)continuousVariables.elementAt(i)).equals(var))
            return i;
   return -1;
     
}
/**
 * Returns the position of a continuous variable in the vector
 * of continuous variables, or (-1) if it is not present.
 * @param name the name of the variable (<code>Continuous</code>).
 * @return the position of the continuous variable.
 */

public int getContinuousIndex(String name) {
   for (int i=0; i<continuousVariables.size(); i++)
        if (((Node)continuousVariables.elementAt(i)).getName().compareTo(name)==0)
            return i;
   return -1;
     
}


/**
 * Returns the value of a given continuous variable.
 * @param var the variable (<code>Continuous</code>).
 * @return the value of variable <code>var</code>.
 */
public double getValue(Continuous var) {
  return getContinuousValue(continuousVariables.indexOf(var));
}



/**
 * Returns the value of a given Node variable.
 * @param var the variable (<code>Continuous</code>).
 * @return the value of variable <code>var</code>.
 */
/*
public double getValue(Node var) {
        if (var.getClass()==Continuous.class){
              return getContinuousValue((Continuous)var);
        }else
              return super.getValue((FiniteStates)var);
}
*/

/**
 * Inserts a pair (variable,value) at the end of the configuration.
 * @param var a <code>Continuous</code> variable.
 * @param val a double value.
 */

public void insert(Continuous var, double val) {
  Double i;

  continuousVariables.addElement(var);
  i = new Double(val);
  continuousValues.addElement(i);
  
}

/**
 * Puts a pair (variable,value) at the configuration. If the variable is
 * already in the configuration,
 * sets the value in the position of the variable,
 * else inserts (variable,value) at the end of the configuration.
 * @param var a <code>Continuous</code> variable.
 * @param val a <code>double</code> value.
 */

public void putValue(Continuous var, double val) {
    Double i;
    int pos;
    
    pos = getIndex(var);

    if (pos == -1)
      insert(var,val);
    else {
      i = new Double(val);
      continuousValues.setElementAt(i,pos);
    }
}

/**
 * Prints the configuration to the standard output.
 */

public void print() {
  int i;
  Node n;
 
  super.print();
  
  for (i=0 ; i<continuousVariables.size() ; i++) {
    n = (Node)continuousVariables.elementAt(i);
    System.out.println(n.getName()+" : "+getContinuousValue(i));
  }
}

public int size() {
  return super.size() + continuousVariables.size();
}

public void remove(Node n){
        if (n.getClass()==Continuous.class){
              int pos=getIndex((Continuous)n);
              if (pos!=-1){
                  continuousVariables.removeElementAt(pos);
                  continuousValues.removeElementAt(pos);
              }
        }else{
            if (super.indexOf(n)!=-1)
                super.remove(super.indexOf(n)); 
        }
}


/**
 * Saves this configuration in a file.
 * @param p the <code>PrintWriter</code> where the configuration
 * @param nodeList the <code>NodeList</code> with the order to print
 * the pairs (node, value).
 * will be written.
 */

public void save(PrintWriter p,NodeList nodeList) {
 
 if (continuousVariables.size()==0){
    super.save(p);
 }else{
     int i, j, k;
     String name; 
     Node node;
     p.print("[");

      j = nodeList.size();

     for (i=0 ; i<j ; i++) {
       node=nodeList.elementAt(i);
       if (node.getClass()==FiniteStates.class){
           name = new String((String)((FiniteStates)node).getPrintableState(((Integer)values.elementAt(indexOf(node))).intValue()));
           p.print(name);
           /*
           try {
             k = Integer.parseInt(name);
             p.print("\""+name+"\"");
           }
           catch (NumberFormatException e) {
             p.print(name);
           }*/	      
       }else if(node.getClass()==Continuous.class){
            p.print(getValue((Continuous)node));

       }

       if (i == j-1) {
         p.print("]");
       }
       else {
         p.print(",");
       }
     }
 }	      
}

public Configuration copy(){
    
    Vector cvars=(Vector)continuousVariables.clone();
    Vector cvalues=(Vector)continuousValues.clone();
    cvars.addAll(variables);
    cvalues.addAll(values);
    return new ContinuousConfiguration(cvars,cvalues);
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
  Continuous cn;
  
  if (variables.size() != conf.getVariables().size())
    return false;
  else {
    for (i=0 ; i<variables.size() ; i++) {
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
  
  if (conf instanceof ContinuousConfiguration){
      ContinuousConfiguration conf2=(ContinuousConfiguration)conf;
      if (continuousVariables.size() != conf2.getContinuousVariables().size())
        return false;
      else {
        for (i=0 ; i<continuousVariables.size() ; i++) {
          cn = getContinuousVariable(i);
          j = conf2.indexOf(cn);
          if (j == -1)
            return false;
          else {
            if (getContinuousValue(i) != conf2.getContinuousValue(j))
              return false;
          }
        }
      }
  }else{
    if (continuousVariables.size()==0)
        return true;
    else
        return false;
      
  }
  return true;
}

/**
 * This method removes the undifined values that could be in
 * the ContinuousConfiguration object.
 */
public void removeUndefinedValues(){
   
    super.removeUndefinedValues();
    
    Vector<Node> remove=new Vector<Node>();
    for (int i=0; i<this.continuousVariables.size(); i++)
       if (Continuous.isUndefined(getContinuousValue(i))){
            remove.addElement(this.getContinuousVariable(i));
    }
    for (int i=0; i<remove.size(); i++)
        this.remove(remove.elementAt(i));
}

} // End of class.
