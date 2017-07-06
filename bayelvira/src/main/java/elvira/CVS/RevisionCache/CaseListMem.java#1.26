/* CaseListMem.java */
  
package elvira;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import elvira.potential.Potential;

/**
 * This class implements a case list as a vector of integer arrays.
 * This arrays are the states index of the variables.
 * @since 3/3/2004
 
 */

public class CaseListMem extends CaseList implements Serializable {

static final long serialVersionUID = -6201032095044876869L;
/**
 * A vector of cases for the variables corresponding to this list
 * of cases.
 */
Vector cases;
/**
 * Iterator used to obtain all the cases from the vector cases in an efficient way.
 */
Iterator iterator;

/**
 * This constructor method is the default constructor of the class.
 */

public CaseListMem() {
  
  cases = null;
}


/**
 * This constructor method stores the case list variables and 
 * the vector of cases.
 * @param <code>NodeList</code> list of nodes to consider
 */

public CaseListMem(NodeList variables) {
  
  setVariables (variables.toVector());
  setNumberOfCases(0);
  cases = new Vector();
}

/**
 * This constructor method create a new CaseList given a vector of
 * variables
 * @param <code>Vector</code> of variables
 */

public CaseListMem(Vector variables) {
  setVariables(variables);
  setNumberOfCases(0);
  cases=new Vector();
}


/**
 * This method gets the vector of cases.
 * @return the set of cases.
 */

public Vector getCases() {
  
  return cases;
}

/**
 * This method gets the case (row) in position i
 * @return a case
 */

public int[] getCase(int i) {
  
  return (int[]) cases.elementAt(i);
}



/**
 * This method sets the vector of cases.
 * @return the set of cases.
 */

public void setCases(Vector v) {
  
  cases = v;
  setNumberOfCases(v.size());
}


/**
 * This method sets observation number i in the 
 * vector of cases
 * @return the set of cases.
 */

public void setCase(int[] obser,int i) {
  
  cases.setElementAt(obser,i);
 
}




/**
 * Method to clean the vector of cases. This is used to keep
 * the list of variables and to prepare the storage for new
 * cases
 */

public void removeCases(){
  cases=new Vector();
}

/**
 * This method computes the absolute counts of a configuration given as
 * parameter.
 * @param conf a configuration.
 * @return the counts of <code>conf</code>.
 */


public double getValue(Configuration conf){
  int i;
  double cont;
  Configuration confAux,confAux2;
  Vector vars = conf.getVariables();
  int[] indexOfVars = new int[vars.size()];
  for(i=0 ; i<vars.size(); i++){
    indexOfVars[i] = getVariables().indexOf(vars.elementAt(i));
    //System.out.println(indexOfVars[i]);
  }
  cont = 0.0;
  for(i=0 ;i< getNumberOfCases(); i++){
    confAux = get(i,indexOfVars);
    //confAux.print();
    //System.out.println("Numero: "+cont);
    //try{System.in.read();}catch(IOException e){};
    //confAux2 = new Configuration(vars,confAux);
    if(confAux.equals(conf)) cont++;
  }
  
  return cont;
  
}


/**
 * This method computes the absolute counts of a configuration given as
 * parameter.
 * @param conf a configuration.
 * @return the counts of <code>conf</code>.
 */


public double getCompatibleValue(Configuration conf){
  int i;
  double cont;
  Configuration confAux,confAux2;
  Vector vars = conf.getVariables();
  int[] indexOfVars = new int[vars.size()];
  for(i=0 ; i<vars.size(); i++){
    indexOfVars[i] = getVariables().indexOf(vars.elementAt(i));
    //System.out.println("indexOfVars[i]= " + indexOfVars[i]);
  }
  cont = 0.0;
  for(i=0 ;i< getNumberOfCases(); i++){
    confAux = get(i,indexOfVars);
    //System.out.println("Configuraci�n: "+confAux.toString());
    //confAux.print();
    //System.out.println("Numero: "+cont);
    //try{System.in.read();}catch(IOException e){};
    //confAux2 = new Configuration(vars,confAux);
    if(confAux.isCompatible(conf))
      cont++;
  }
  
  return cont;
  
}


/**
 * This method is not used, but it is necessary to compile
 * (abstract method inherited from <code>Potential</code>).
 */

public double getValue(Hashtable positions, int[] conf) {
  
  return 0.0;
}

/**
   * Returns the next case in a Configuration objet. The cases are obtained 
   * sequentially and efficiently
   * @return Configuration
   */
public Configuration getNext()
{
  int i;
  int [] dataCase = ((int [])this.iterator.next());
  Vector l;
  Vector values = new Vector();
  l = new Vector();
 
  for(i=0;i<dataCase.length;i++)
  { l.addElement( this.getVariables().elementAt(i) );
    values.addElement(new Integer(dataCase[i]));
  }//for_i
  return new Configuration(l,values);  
}// getNext()
/**
 * Returns true if there are more elemnets left to be sequentially obtained. 
 */
public boolean hasNext()
{
  return this.iterator.hasNext();
}

/**
 * Initialize the class CaseListMem in order to obtain the cases sequentially and in an efficient way
 */
public void initializeIterator()
{
  this.iterator = this.cases.iterator();
}

/**
 * This method is not used, but it is necessary to compile
 * (abstract method inherited from <code>Potential</code>).
 */

public void setValue(Configuration conf,double val) {
  
  System.out.println("CaseList: Not implemented");
}
    
/**
 * This method return the value in the pos (i,j) from the
 * matrix of cases for the variables.
 */
public double getValue(int i, int j) {
  return (double) ( ((int[])cases.elementAt(i))[j] );
}

/**
 * This method sets the value in the pos (i,j) in the
 * matrix of cases for the variables.
 */
 
public void setValue(int i, int j, double value) {
  int[] cas;
  cas=(int[])cases.elementAt(i);
  cas[j]=(int)value;
}


/**
 * This method computes the absolute counts of a configuration given as
 * parameter.
 * @param conf a configuration.
 * @return the counts of <code>conf</code>.
 * @see this#getValue()
 */

public double totalPotential(Configuration conf) {
  
  return (getValue(conf));
}


/**
 * This method puts a configuration at the end of the list of cases.
 * @param conf the configuration to insert.
 * @return <code>true</code> if the operation is succesful.
 */

public boolean put(Configuration conf) {
  
  int i, posInConf, val;
  FiniteStates var;
  boolean success = true;
  int[] cas;
  
  cas = new int[getVariables().size()];
  for (i=0 ; i<getVariables().size() ; i++)
    cas[i] = -1;
  
  if (cases != null) {
    for (i=0 ; i<getVariables().size() ; i++) {
      var = (FiniteStates) getVariables().elementAt(i);
      posInConf = conf.indexOf(var);
      if (posInConf != -1) {
	val = conf.getValue(posInConf);
	cas[i]=val; 
      }
    }
    try{
      cases.addElement(cas);
    }catch (OutOfMemoryError e){
      System.out.println(e);
      success = false;
    }
    if (success)
      setNumberOfCases(cases.size());
  }
  return success;
}


/**
 * This method gets a case configuration from the argument position in
 * case list.
 * @param pos a position in the list.
 * @return The configuration at position <code>pos</code>.
 * This configuration is null if the operation is not successful.
 */

public Configuration get(int pos) {
  
  int i;
  Configuration conf;
  int cas[];
  
  if (pos < getNumberOfCases()) {
    if (cases != null) {
      conf = new Configuration();
      cas = (int[])cases.elementAt(pos);
      
      for (i=0 ; i<getVariables().size() ; i++)
	conf.insert((FiniteStates)getVariables().elementAt(i),cas[i]);
    }
    else
      conf = null;
    return conf;
  }
  else {
    System.out.println("Position "+pos+" > number of cases");
    conf = null;
    return conf;
  }
}


/**
 * This method gets a case configuration from the given position in case list.
 * @param pos a position in the list.
 * @param indexOfVars the index of variables in the case list.
 * @return the onfiguration at position <code>pos</code>.
 * This configuration is null if the operation is not successful.
*/

public Configuration get(int pos,int[] indexOfVars) {
  
  int i;
  Configuration conf;
  int cas[];
  
  if (pos < getNumberOfCases()) {
    if (cases != null) {
      conf = new Configuration();
      cas = (int[])cases.elementAt(pos);
      for (i=0 ; i<indexOfVars.length ; i++)
	conf.insert((FiniteStates)getVariables().
		    elementAt(indexOfVars[i]),cas[indexOfVars[i]]);
    }
    else
      conf = null;
    return conf;
  }
  else {
    System.out.println("Position "+pos+" > number of cases");
    conf = null;
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
 * Sets the vector of cases 
 */

public void setValues(Vector vectorOfCases){
  cases = vectorOfCases;
}

/** 
 *  Deletes the variables specified by the indexes included in the list
 */

public void deleteVariables(Vector indexOfVars) {
  int i, j;
  Configuration conf;
  Vector vars = getVariables();
  int newNumVars = vars.size() - indexOfVars.size();
  Vector newCases = new Vector(cases.size());
  int[] newCase = new int[newNumVars];
  
  for(i =0; i < getNumberOfCases() ; i++){
    conf = get(i);
    for(j = indexOfVars.size()-1 ; j >= 0 ; j--){
      conf.remove(((Integer)indexOfVars.elementAt(j)).intValue());
    }
    for(j = 0 ; j < newNumVars ; j++){
      newCase[j] = conf.getValue(j);
    }
    newCases.add(newCase);
    newCase = new int[newNumVars];
  }
  
  setValues(newCases);
  
  for(j = indexOfVars.size()-1 ; j >= 0  ; j--){
    vars.removeElementAt(((Integer)indexOfVars.elementAt(j)).intValue());
  }
  
  setVariables(vars);
  
}


/**
 * The next methods are not implemented (just to keep compatibility with
 * <code>Potential</code>.
 */

public double entropyPotential() {
  
  return (-1.0);
}


public double entropyPotential(Configuration conf) {
  
  return (-1.0);
}


/**
 * Combines current potential with the given one.
 * @param pot the potential to combine with this
 * @return an empty case list. 
 */

public Potential combine(Potential pot) {
  
  return (Potential)(new CaseListMem());
}


/**
 * Removes the argument variable summing over all its values.
 * @param var a Node variable.
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(Node var) {
  
  return (Potential)(new CaseListMem());
}


/**
 * Saves the potential into the file represented by the PrintWriter P
 * @param p is the <code>PrintWriter</code>
 */

public void saveResult(PrintWriter p) {
  
  System.out.println("CaseListMem: Not implemented");
}


/**
 * Normalizes the values of this potential.
 */

public void normalize() {
  
  System.out.println("CaseListMem: Not implemented");
}


public Potential restrictVariable(Configuration conf) {
  
  return (Potential)(new CaseListMem());
}


/**
 * This method separates the cases with missing values
 * from complete cases.
 * 
 * @param missingCases an object of <code>CaseListMem</code> class
 * where the cases with missing values will be stored. This object
 * must be initialised before this method is called.
 * @param completeCases an object of <code>CaseListMem</code> class
 * where the cases without missing values will be stored. This object
 * must be initialised before this method is called.
 */

public void separateMissingValues(CaseListMem missingCases,
				  CaseListMem completeCases) {
  //Iterators
  int i = 0;
  int j = 0;

  int numCases = getNumberOfCases(); 
  int numValues;
  
  boolean noPutError = true;
  boolean foundMissingValue = false;
  
  Configuration currentCase;
  Configuration copyCase;
  
  double value, undefValue;

  // Initialise the variables of the CaseLists
  missingCases.setVariables(getVariables());
  missingCases.setCases(new Vector());
  completeCases.setVariables(getVariables());
  completeCases.setCases(new Vector());
  
  //Check errors adding a case
  while ((i != numCases) && noPutError){    
    currentCase = get(i);
    
    //Check errors getting a case
    if (currentCase != null){
      copyCase = currentCase.copy();
      j = 0;
      numValues = currentCase.size();
      foundMissingValue = false;
      while((j != numValues) && !foundMissingValue){
	value = currentCase.getValue(j);
	undefValue = currentCase.getVariable(j).undefValue();
	//Check if the value is a missing value
	if (value==undefValue)
	  foundMissingValue = true;
	j++;
      }
      
      //Store depending on there are missing values or not
      if(foundMissingValue)
	noPutError = missingCases.put(copyCase);
      else
	noPutError = completeCases.put(copyCase);
      
    }//end if
    else
      System.out.println("Error getting a case.");
    
    i++;
    
  }//end while

  if (!noPutError) System.out.println("Error adding a case.");

}



/**
 *  Appends all of the elements in the specified list of cases 
 *  extraCases at the beginning of this list of cases. 
 *
 *  @param extraCases The <code>CaseListMem</code> whose cases are
 *  inserted into this <code>CaseListMem</code>.
 */

public void merge (CaseListMem extraCases){
  
  Vector casesVector;
  Vector extraCasesVector;
  
  casesVector = this.getCases();
  extraCasesVector = extraCases.getCases();
  
  //Uses the corresponding method to class Vector
  extraCasesVector.addAll( casesVector);
 
  this.setCases(extraCasesVector);
  
}

/** 
 *  Deletes the variables specified by the Node list
 */
 
public void deleteVariables(NodeList nodes){
 
    Vector index=new Vector();
    for (int i=0; i<nodes.size(); i++){
     index.add(new Integer(getVariables().indexOf(nodes.elementAt(i))));   
    }
    deleteVariables(index);
}

/*
 *  This method return a copy of a CaseListMem object.
 */

public Potential copy(){
 
    NodeList nodes=new NodeList((Vector)this.getVariables().clone());
    CaseListMem cp=new CaseListMem(nodes.copy());
    Vector casescopy=new Vector();
    for (int i=0; i<this.cases.size(); i++){
        int[] case1=(int[])cases.elementAt(i);
        int[] case2=new int[case1.length];
        for (int j=0; j<case1.length; j++)
            case2[j]=case1[j];
        casescopy.addElement(case2);
    }
    
    cp.setCases(casescopy);
    return cp;
}

public ContinuousCaseListMem projection(NodeList nl){
    ContinuousCaseListMem caseL=new ContinuousCaseListMem(nl);
    
    int[] index=new int[nl.size()];
    NodeList tmp=new NodeList(this.getVariables());
    for (int i=0;i<nl.size();i++)
        index[i]=tmp.getId(nl.elementAt(i));
        
    for (int i=0; i<this.getNumberOfCases(); i++){
        caseL.put(this.get(i,index));
    }
    
    return caseL;
}

/**
 * Swap to rows 
 * @param i index of the first row
 * @param j index of the second row
 */
private void swap(int i, int j) {
    Object tmp = cases.get(i);
    cases.setElementAt(cases.get(j),i);
    cases.setElementAt(tmp,j);
}
/**
 * Shuffle cases
 * @param random generator
 */
public void randomize(Random random) {
    for (int i = cases.size()-1; i > 0; i--) {
        int pos = random.nextInt(i+1);
        swap(i,pos);
    }
}

/**
 * 
 * @author jlmateo
 *
 * @since 15-feb-2005
 * 
 * This class is used to order an array of int's, the vector of cases by
 * the class variable
 */
private class ClassVarComparator implements Comparator {

    
    private int classPosition; 
    
    /**
     * 
     * @param classPosition 
     */
    public ClassVarComparator(int classPosition) {
        this.classPosition = classPosition;
    }
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {
        int[] row1,row2;
        row1 = (int [])o1;
        row2 = (int [])o2;
        
        if (row1[classPosition] < row2[classPosition])
            return -1;

        if (row1[classPosition] > row2[classPosition])
            return 1;
        
        return 0;
    }
    
}

/**
 * Stratify the vector of cases in order to prepare them for splitting
 *  
 * @param nFolds Number of folds 
 */
public void stratify(int nFolds) {
    
    //assume class variable is the last one
    java.util.Collections.sort(cases,new ClassVarComparator(this.getVariables().size()-1));
    
    Vector newCases = new Vector(cases.size());
    
    int start = 0, j;
    
    // create stratified batch
    while (newCases.size() < cases.size()) {
        j = start;
        while (j < cases.size()) {
            newCases.addElement(cases.get(j));
            j = j + nFolds;
        }
        start++;
    }
    
    cases = newCases;
}

/**
 * 
 * @param fold
 * @param totalFolds
 * @return The cases to train the model
 */
public CaseListMem getTrainCV(int fold, int totalFolds) {
    CaseListMem returnCases;
    Vector cases;
    int i,testCases,firstTestCase;
    
    if ((totalFolds<2) || (totalFolds>this.cases.size()))
        return null;
    
    if (fold < this.cases.size() % totalFolds) {
        testCases = this.cases.size() / totalFolds +1;
        firstTestCase = fold * this.cases.size() / totalFolds + fold;
    } else {
        testCases = this.cases.size() / totalFolds;
        firstTestCase = fold * this.cases.size() / totalFolds;
    }
    
    cases = new Vector(this.cases.size()-testCases);
    for (i = 0; i < firstTestCase; i++) {
        cases.add(this.cases.get(i));
    }
    
    for (i = i+testCases; i < this.cases.size(); i++) {
        cases.add(this.cases.get(i));
    }
    
    returnCases = new CaseListMem(this.getVariables());
    returnCases.setCases(cases);
    return returnCases;
}


/**
 * 
 * @param fold
 * @param totalFolds
 * @return The cases to test the model
 */
public CaseListMem getTestCV(int fold, int totalFolds) {
    CaseListMem returnCases;
    Vector cases;
    int i,testCases,firstTestCase;
    
    if ((totalFolds<2) || (totalFolds>this.cases.size()))
        return null;
    
    if (fold < this.cases.size() % totalFolds) {
        testCases = this.cases.size() / totalFolds +1;
        firstTestCase = fold * this.cases.size() / totalFolds + fold;
    } else {
        testCases = this.cases.size() / totalFolds;
        firstTestCase = fold * this.cases.size() / totalFolds;
    }
    
    cases = new Vector(this.cases.size()-testCases);
    for (i = 0; i < testCases; i++) {
        cases.add(this.cases.get(i + firstTestCase));
    }
    
    returnCases = new CaseListMem(this.getVariables());
    returnCases.setCases(cases);
    return returnCases;
}

/**
 * 
 * @author dalgaard
 * 
 * This method appends the cases in newCases to this CaseListMem.
 * 
 * @param newCases - the cases to add. This object is not modified.
 * 
 */
public void appendCases(final CaseListMem newCases){
	this.cases.addAll(newCases.getCases());
	this.setNumberOfCases(this.cases.size());
}

/**
 *
 * This method gets some variables from cases, and determines wether 
 * there are missing values or not
 * 
 * @param cas - the index of the case we want to use
 * @param indexOfVars - index of variables we want to get
 * @param values - array to store the values
 * @return missing - true if there are missing values, false if not
 * 
 */
public boolean getValues(int cas, int[] indexOfVars, int[] values){
    
    boolean missing = false;
    for(int i=0; i<indexOfVars.length;i++){
        values[i] = (int) this.getValue(cas, indexOfVars[i]);
        if(values[i]==-1){
            missing=true;
            break;
        }
    }
    return missing;
} 

} // End of class

