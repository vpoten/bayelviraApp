package elvira;

import java.io.*;
import java.util.Vector;
import java.util.Hashtable;
import elvira.potential.Potential;

/**
 * This class implements a list of cases as a random access file of
 * <code>integer</code>. 
 * This integers are the states index of the variables.
 * @since 20/9/2000 
 */

public class CaseListOutMem extends CaseList implements Serializable {
    
/**
 * The file of cases.
 */
private RandomAccessFile cases;


/**
 * This constructor method is the default constructor of the class.
 */

public CaseListOutMem() {
  
  cases = null;
}


/**
 * This constructor method stores the case list variables and the file of
 * cases.
 * @param variables the variables to which the cases correspond.
 * @param fileName the name of the file.
 */

public CaseListOutMem(NodeList variables, String fileName) {
  
  int nVars, cas, pos, index;
  
  setVariables(variables.toVector());
  nVars = getVariables().size();
  setNumberOfCases(0);
  try{
    cases = new RandomAccessFile(fileName,"rw");
  }catch (IOException e){
    cases = null;
    System.out.println(e);
  }
}


/**
 * This method gets the file of cases.
 * @return the set of cases.
 */

public RandomAccessFile getCases() {
  
  return cases;
}


/**
 * This method computes the absolute frequency of a configuration given as 
 * parameter.
 * @param conf the configuration whose frequency will be computed.
 * @return the frequency.
 */

public double getValue(Configuration conf) {
  
  int i, cont;
  Configuration confAux, confAux2;
  Vector vars;
  
  cont = 0;
  for (i=0 ; i< getNumberOfCases() ; i++) {
    confAux = get(i);
    vars = conf.getVariables();
    confAux2 = new Configuration(vars,confAux);
    if (confAux2.equals(conf))
      cont++;
  }
  
  return (double)cont;
}


/**
 * These two methods are not used, but it is necessary to compile.
 */

public double getValue(Hashtable positions, int[] conf) {

  return 0.0;
}

/**
 * This method return the value in the pos (i,j) from the
 * matrix of cases for the variables.
 * @param i, index of the case
 * @param j, index of the variable
 */
public double getValue(int i, int j) {
    
  Configuration conf=this.get(i);
  return conf.getValue(j);
}

public void setValue(Configuration conf,double val) {
  
  System.out.println("CaseList: Not implemented");
}


/**
 * This method computes the absolute frequency of a configuration given as 
 * parameter.
 * @param conf the configuration whose frequency will be computed.
 * @return the frequency.
 * @see this#getValue()
 */

public double totalPotential(Configuration conf) {
  
  return getValue(conf);
}


/**
 * These two methods are not used, but it is necessary to compile.
 */

public double entropyPotential() {
  
  return (-1.0);
}

public double entropyPotential(Configuration conf){

  return (-1.0);
}


/**
 * This method puts a configuration at the end of the case list.
 * @param conf the configuration to put in the list.
 * @return <code>true</code> if the operation is successful.
 */

public boolean put(Configuration conf) {
  
  int i, posInConf, val, index;
  FiniteStates var;
  boolean success = true;
  int pos;
  
  pos = getNumberOfCases();
  
  for (i=0 ; i<getVariables().size() ; i++) {
    index = getIndex(pos,i);
    try{
      cases.seek(index);
      cases.writeInt(-1);
    }catch (IOException e){
      success = false;
      System.out.println(e);
      return success;
    }
  }
  
  for (i=0 ; i<getVariables().size() ; i++) {
    var = (FiniteStates) getVariables().elementAt(i);
    posInConf = conf.indexOf(var);
    if (posInConf != -1) {
      val = conf.getValue(posInConf);
      index = getIndex(pos,i);
      try{
	cases.seek(index);
	cases.writeInt(val);
	success = true;
      }catch (IOException e){
	success = false;
	System.out.println(e);
	return success;
      }
    }
  }
  
  if (success) {
    pos++;
    setNumberOfCases(pos);
  }
  return success;
}


/**
 * This method gets a case configuration from a given position in the
 * case list.
 * @param pos the position of the configuration to retrieve.
 * @return the configuration at position <code>pos</code>. This
 * configuration is null if the operation is not successful.
 */

public Configuration get(int pos) {
  
  int i, val, index;
  
  if (pos < getNumberOfCases()) {
    Configuration conf = new Configuration(getVariables());
    for (i=0 ; i<getVariables().size() ; i++) {
      index = getIndex(pos,i);
      try{
	cases.seek(index);
	val = cases.readInt();
	conf.putValue((FiniteStates)getVariables().elementAt(i),val);
      }catch (IOException e){
	conf = null;
	System.out.println(e);
      }
    }
    return conf;
  }
  else {
    System.out.println("Position "+pos+" > number of cases");
    Configuration conf = null;
    return conf;
  }
}


/**
 * This method gets a case configuration from a given position in
 * the case list.
 * @param pos the position of the configuration to retrieve.
 * @param indexOfVars the index of variables in the case list.
 * @return the configuration at position <code>pos</code>. This
 * configuration is null if the operation is not successful.
 */

public Configuration get(int pos,int[] indexOfVars) {
  
  int i, val, index;
  
  if (pos < getNumberOfCases()) {
    Configuration conf = new Configuration();
    for (i=0 ; i<indexOfVars.length ; i++) {
      index = getIndex(pos,indexOfVars[i]);
      try{
	cases.seek(index);
	val = cases.readInt();
	conf.insert((FiniteStates)getVariables().elementAt(indexOfVars[i]),
		    val);
      }catch (IOException e){
	conf = null;
	System.out.println(e);
      }
    }
    return conf;
  }
  else {
    System.out.println("Position "+pos+" > number of cases");
    Configuration conf = null;
    return conf;
  }
}


/**
 * This method prints a case list to the standard output.
 */

public void print() {
  
  for (int i=0 ; i< getNumberOfCases() ; i++) {
    Configuration conf = get(i);
    conf.print();
    System.out.print("\n");
  }
}


/**
 * This method is private to the class, and gets the index into the file.
 * @param row row of the value whose index is requested.
 * @param column column of the value whose index is requested.
 * @return the index of the value at (<code>row,column</code>).
 */

private int getIndex(int row, int column) {
  
  int pos;
  
  pos = (row*getVariables().size()*4)+(column*4);
  return pos;
}


/**
 * These methods are not implemented. Just for compatibility.
 */


/**
 * Combine current potential with Potential pot
 * @param pot
 * @return The combination of the two potential 
 */

public Potential combine(Potential pot) {
  
  return (Potential)(new CaseListOutMem());
}


/**
 * Removes the argument variable summing over all its values.
 * @param var a Node variable.
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(Node var) {
  
  return (Potential)(new CaseListOutMem());
}


/**
 * Save the potential into the file represented by the PrintWriter P
 * @param P is the PrintWriter
 */

public void saveResult(PrintWriter P) {
  
  System.out.println("CaseListMem: Not implemented");
}


/**
 * Normalizes the values of this potential.
 */

public void normalize() {
  
  System.out.println("CaseListMem: Not implemented");
}


public Potential restrictVariable(Configuration conf) {
  
  return (Potential)(new CaseListOutMem());
}

} // End of class
