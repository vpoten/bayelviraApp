/* ContinuousCaseListMem.java */

package elvira;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Vector;

import elvira.potential.Potential;

/**
 * This extends class <code>CaseListMem</code> to include continuous
 * variables. Thus, this class will represent a list of cases of
 * both discrete and continuous variables. To this end, we have
 * a list of variables (stored in variable <code>variables</code>
 * inherited from abstract class <code>Potential</code> and a
 * list of cases, represented as a vector of arrays of doubles.
 * At each position of one of those arrays, we find a value
 * of the variable stored in the same position in vector
 * <code>variables</code>. If the variable is discrete, the
 * value stored in the array must be casted to <code>int</code> to be
 * used. <p>
 *
 * Use this class to represent samples.
 *
 * @since 1/6/2007
 */

public class ContinuousCaseListMem extends CaseListMem implements Serializable {

    static final long serialVersionUID = -8560632964537685460L;
/**
 * Constructs an empty <code>ContinuousCaseListMem</code> for a
 * list of variables.
 * @param variables the list of variables.
 */

public ContinuousCaseListMem(NodeList list) {

  setVariables(list.toVector());
  setNumberOfCases(0);
  cases = new Vector(100,100);
}

/**
 * Constructs an empty <code>ContinuousCaseListMem</code> for a
 * vector of variables.
 * @param variables the vector of variables.
 */

public ContinuousCaseListMem(Vector list) {

  setVariables(list);
  setNumberOfCases(0);
  cases = new Vector(100,100);
}

/**
 * 
 *
 */
 public ContinuousCaseListMem projection(NodeList nl){
    
     /*
    int[] index=new int[nl.size()];
    NodeList tmp= new NodeList((Vector)this.getVariables().clone());
    for (int i=0; i<nl.size(); i++){
        index[i]=tmp.getId(nl.elementAt(i));
    }
    */
    
    int[] index=this.getIndexGetQuickly(nl);
    
    ContinuousCaseListMem caseL = new ContinuousCaseListMem(nl);
    for (int i=0; i<this.getNumberOfCases(); i++){
        caseL.put(this.getQuickly(i,nl,index));
    }
    
    return caseL;
 }

/**
 * Inserts a configuration at the end of the list of cases.
 * @param conf a <code>ContinuousConfiguration</code>.
 * @return true if the operation is successful, false otherwise.
 */

public boolean put(Configuration con) {

  int i, posInConf, type;
  Node var;
  boolean success = true;
  double[] cas;
  double val;
  ContinuousConfiguration conf = null;

  if (con instanceof ContinuousConfiguration)
    conf = (ContinuousConfiguration)con;
  else {
      /*
    System.out.println("Error in ContinuousCaseListMem.put(Configuration): "+
		       "<con> is not an instance of ContinuousConfiguration");
    System.exit(1);
       */
      conf=new ContinuousConfiguration(con.getVariables(),con.getValues());
  }
  
  cas = new double[getVariables().size()];
  if (cases != null) {
    for (i=0 ; i<getVariables().size() ; i++) {
      var = (Node) getVariables().elementAt(i);
      type = var.getTypeOfVariable(); 
      if (type == Node.FINITE_STATES) {
	posInConf = conf.indexOf((FiniteStates)var);
	if (posInConf != -1) {
	  val = conf.getValue(posInConf);
	  cas[i] = val; 
	}
      }
      else {
	posInConf = conf.getIndex((Continuous)var);
	if (posInConf != -1) {
	  val = conf.getContinuousValue(posInConf);
	  cas[i] = val; 
	}
      }
    }
    
    try {
      cases.addElement(cas);
    } catch (OutOfMemoryError e) {
      System.out.println(e);
      success = false;
    }
    if (success)
      setNumberOfCases(cases.size());
  }
  return success;
}


/**
 * Replaces the case at a given position.
 * @param conf a <code>ContinuousConfiguration</code> with the new case.
 * @param pos the position of the case to replace.
 * @return true if the operation is successful, false otherwise.
 */

public boolean replaceCase(Configuration con, int pos) {

  int i, posInConf, type;
  Node var;
  boolean success = true;
  double[] cas;
  double val;
  ContinuousConfiguration conf = null;

  if (con instanceof ContinuousConfiguration)
    conf = (ContinuousConfiguration)con;
  else {
    System.out.println("Error in ContinuousCaseListMem.put(Configuration): "+
		       "<con> is not an instance of ContinuousConfiguration");
    System.exit(1);
  }
  
  if (pos >= getNumberOfCases()) {
      System.out.println("Index out of range in replaceCase.");
      System.exit(1);
  }
  
  cas = new double[getVariables().size()];
  if (cases != null) {
    for (i=0 ; i<getVariables().size() ; i++) {
      var = (Node) getVariables().elementAt(i);
      type = var.getTypeOfVariable(); 
      if (type == Node.FINITE_STATES) {
	posInConf = conf.indexOf((FiniteStates)var);
	if (posInConf != -1) {
	  val = conf.getValue(posInConf);
	  cas[i] = val; 
	}
      }
      else {
	posInConf = conf.getIndex((Continuous)var);
	if (posInConf != -1) {
	  val = conf.getContinuousValue(posInConf);
	  cas[i] = val; 
	}
      }
    }
    
    try {
      cases.setElementAt(cas,pos);
    } catch (OutOfMemoryError e) {
      System.out.println(e);
      success = false;
    }
    if (success)
      setNumberOfCases(cases.size());
  }
  return success;
}


/**
 * Inserts a configuration at the end of the list of cases.
 * @param conf a <code>ContinuousConfiguration</code>.
 * @return true if the operation is successful, false otherwise.
 */

public boolean putQuickly(Configuration con, int[] posInConf) {

  int i, type;
  Node var;
  boolean success = true;
  double[] cas;
  double val;
  ContinuousConfiguration conf = null;

  if (con instanceof ContinuousConfiguration)
    conf = (ContinuousConfiguration)con;
  else {
    System.out.println("Error in ContinuousCaseListMem.put(Configuration): "+
		       "<con> is not an instance of ContinuousConfiguration");
    System.exit(1);
  }
  
  cas = new double[getVariables().size()];
  if (cases != null) {
    for (i=0 ; i<getVariables().size() ; i++) {
      var = (Node) getVariables().elementAt(i);
      type = var.getTypeOfVariable(); 
      if (type == Node.FINITE_STATES) {
	//posInConf = conf.indexOf((FiniteStates)var);
	if (posInConf[i] != -1) {
	  val = conf.getValue(posInConf[i]);
	  cas[i] = val; 
	}
      }
      else {
	//posInConf = conf.getIndex((Continuous)var);
	if (posInConf[i] != -1) {
	  val = conf.getContinuousValue(posInConf[i]);
	  cas[i] = val; 
	}
      }
    }
    
    try {
      cases.addElement(cas);
    } catch (OutOfMemoryError e) {
      System.out.println(e);
      success = false;
    }
    if (success)
      setNumberOfCases(cases.size());
  }
  return success;
}

public int[] getIndexGetQuickly(NodeList nl) {
    int [] index=new int[nl.size()];
    NodeList temp=new NodeList((Vector)this.getVariables().clone());
    for (int i=0; i<nl.size(); i++)
        index[i]=temp.getId(nl.elementAt(i));
    
    return index;
}

/**
 *
 */

public int[] getIndexPutQuickly(Configuration con) {

  int i, type;
  Node var;
  ContinuousConfiguration conf = null;

  if (con instanceof ContinuousConfiguration)
    conf = (ContinuousConfiguration)con;
  else {
    System.out.println("Error in ContinuousCaseListMem.put(Configuration): "+
		       "<con> is not an instance of ContinuousConfiguration");
    System.exit(1);
  }
  
  int[] posInConf=new int[getVariables().size()];
    for (i=0 ; i<getVariables().size() ; i++) {
      var = (Node) getVariables().elementAt(i);
      type = var.getTypeOfVariable(); 
      if (type == Node.FINITE_STATES) {
	posInConf[i] = conf.indexOf((FiniteStates)var);
      }
      else {
	posInConf[i] = conf.getIndex((Continuous)var);
      }
    }
  return posInConf;
}


/**
 * Gets the configuration stored in a given position in the list of cases.
 * @param pos the position.
 * @return the <code>ContinuousConfiguration</code> requested.
 * This configuration is null if the operation is not successful.
 */

public Configuration get(int pos){  
  int i, type;
  ContinuousConfiguration conf;
  double cas[];
  Node var;

  if (pos < getNumberOfCases()) {
    if (cases != null) {
      conf = new ContinuousConfiguration();
      cas = (double[])cases.elementAt(pos);
      for (i=0 ; i<getVariables().size() ; i++) {
	var = (Node)getVariables().elementAt(i);
	type = var.getTypeOfVariable();
	if (type == Node.FINITE_STATES)
	  conf.insert((FiniteStates)var,(int)(cas[i]));
	else
	  conf.insert((Continuous)var,cas[i]);
      }
    }
    else
      conf = null;
    
    return conf;
  }
  else{
    System.out.println("ERROR: Position "+pos+" > number of cases");
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
  
  int i, type;
  ContinuousConfiguration conf;
  double cas[];
  Node var;
  
  if (pos < getNumberOfCases()) {
    if (cases != null) {
      conf = new ContinuousConfiguration();
      cas = (double[])cases.elementAt(pos);
      for (i=0 ; i<indexOfVars.length ; i++){
        var=(Node)getVariables().elementAt(indexOfVars[i]);
        type = var.getTypeOfVariable();
        if (type == Node.FINITE_STATES)
	  conf.insert((FiniteStates)var,(int)cas[indexOfVars[i]]);
        else
          conf.insert((Continuous)var,cas[indexOfVars[i]]);
      }
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
 * Gets the configuration stored in a given position in the list of cases.
 * @param pos the position.
 * @param nl, A node list with the chance nodes that will be considered in the configuration 
 * @return the <code>ContinuousConfiguration</code> requested.
 * This configuration is null if the operation is not successful.
 */

public Configuration get(int pos,NodeList nl){  
  int i, type;
  ContinuousConfiguration conf;
  double cas[];
  Node var;
  NodeList tmp= new NodeList((Vector)this.getVariables().clone());
  
  if (pos < getNumberOfCases()) {
    if (cases != null) {
      conf = new ContinuousConfiguration();
      cas = (double[])cases.elementAt(pos);
      for (i=0 ; i<nl.size() ; i++) {
	var = nl.elementAt(i);
	type = var.getTypeOfVariable();
	if (type == Node.FINITE_STATES)
	  conf.insert((FiniteStates)var,(int)(cas[tmp.getId(var)]));
	else
	  conf.insert((Continuous)var,cas[tmp.getId(var)]);
      }
    }
    else
      conf = null;
    
    return conf;
  }
  else{
    System.out.println("ERROR: Position "+pos+" > number of cases");
    conf = null;
    return conf;
  }
}

/**
 * Gets the configuration stored in a given position in the list of cases.
 * @param pos the position.
 * @param nl, A node list with the chance nodes that will be considered in the configuration 
 * @return the <code>ContinuousConfiguration</code> requested.
 * This configuration is null if the operation is not successful.
 */

public Configuration getQuickly(int pos,NodeList nl, int[] index){  
  int i, type;
  ContinuousConfiguration conf;
  double cas[];
  Node var;
  //NodeList tmp= new NodeList((Vector)this.getVariables().clone());
  
  if (pos < getNumberOfCases()) {
    if (cases != null) {
      conf = new ContinuousConfiguration();
      cas = (double[])cases.elementAt(pos);
      for (i=0 ; i<nl.size() ; i++) {
	var = nl.elementAt(i);
	type = var.getTypeOfVariable();
	if (type == Node.FINITE_STATES)
	  conf.insert((FiniteStates)var,(int)(cas[index[i]]));
	else
	  conf.insert((Continuous)var,cas[index[i]]);
      }
    }
    else
      conf = null;
    
    return conf;
  }
  else{
    System.out.println("ERROR: Position "+pos+" > number of cases");
    conf = null;
    return conf;
  }
}

/**
 * Return the value in the pos (i,j) from the
 * matrix of cases for the continuous variables.
 */
public double getValue(int i, int j) {
        return (double) ( ((double [])cases.elementAt(i))[j] );
}


/**
 * Sets the value in the pos (i,j) in the
 * matrix of cases for the continuous variables.
 */

public void setValue(int i, int j, double value) {
    double[] cas;
    cas=(double[])cases.elementAt(i);
    cas[j]=(double)value;
}


/** 
 *  Deletes the variables specified by the Node list
 */

public void deleteVariables(NodeList nodes) {
  int i, j;
  ContinuousConfiguration conf;
  Vector vars = (Vector)getVariables().clone();
  int newNumVars = vars.size() - nodes.size();
  Vector newCases = new Vector(cases.size());
  double[] newCase = new double[newNumVars];

  
  for(i =0; i < getNumberOfCases() ; i++){
    conf = (ContinuousConfiguration)get(i);
    for(j = nodes.size()-1 ; j >= 0 ; j--){
      conf.remove(nodes.elementAt(j));
      vars.remove(nodes.elementAt(j));
    }
    for(j = 0 ; j < vars.size(); j++){
      if(vars.elementAt(j).getClass()==FiniteStates.class){
        newCase[j] = conf.getValue((FiniteStates)vars.elementAt(j));
      }else{
        newCase[j] = conf.getValue((Continuous)vars.elementAt(j));
      }
    }
    newCases.add(newCase);
    newCase = new double[newNumVars];
  }
  
  for(j = nodes.size()-1 ; j >= 0 ; j--){
      getVariables().remove(nodes.elementAt(j));
  }
  setValues(newCases);
  setVariables(vars);
  
}

/*
 *  This method return a copy of a ContinuousCaseListMem object.
 */

public Potential copy(){
 
    NodeList nodes=new NodeList((Vector)this.getVariables().clone());
    ContinuousCaseListMem cp=new ContinuousCaseListMem(nodes.copy());
    
    Vector casescopy=new Vector();
    for (int i=0; i<this.cases.size(); i++){
        double[] case1=(double[])cases.elementAt(i);
        double[] case2=new double[case1.length];
        for (int j=0; j<case1.length; j++)
            case2[j]=case1[j];
        casescopy.addElement(case2);
    }
    
    cp.setCases(casescopy);
    return cp;
}

/**
 * 
 * @author jlmateo
 *
 * @since 15-feb-2005
 * 
 * This class is used to order an array of double's, the vector of cases by
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
        double[] row1,row2;
        row1 = (double [])o1;
        row2 = (double [])o2;
        
        if (row1[classPosition] < row2[classPosition])
            return -1;

        if (row1[classPosition] > row2[classPosition])
            return 1;
        
        return 0;
    }
    
}


/**
 * Computes the mean of the values corresponding to a given variable.
 * @param position the position of the variable for which the mean will be computed.
 * @return the value of the mean.
 */

public double mean(int position) {
    
    double m = 0.0;
    int i;
    
    
    for (i=0 ; i<cases.size() ; i++) {
        m += ((double [])cases.elementAt(i))[position] ;
    }
    
    return (m / (double)(cases.size()));    
}


/**
 * Computes the variance of the given variable.
 *
 * @param position the position of the variable for which the variance will be computed.
 * @return the value of the variance.
 */

public double variance(int position) {
 
    double s, s1, s2, v;
    int i;
    
    s1 = 0.0;
    s2 = 0.0;
    
    for (i=0 ; i<cases.size() ; i++) {
        s = ((double [])cases.elementAt(i))[position] ;
        s1 += (s * s);
        s2 += s;
    }
    
    s2 /= (double)(cases.size());
    
    v = (s1 / (double)(cases.size())) - (s2 * s2);
    
    return (v);
}


/**
 * Computes the covariance of the given variables.
 *
 * @param position1 the position of the first variable.
 * @param position2 the position of the other variable.
 * @return the value of the covariance of the variables at positions
 * <code>position1</code> and <code>position2</code>.
 */

public double covariance(int position1, int position2) {
 
    double s1, s2, prodAcum, m1, m2, v;
    int i;
    
    m1 = 0.0;
    m2 = 0.0;
    prodAcum = 0.0;
    
    for (i=0 ; i<cases.size() ; i++) {
        s1 = ((double [])cases.elementAt(i))[position1] ;
        s2 = ((double [])cases.elementAt(i))[position2] ;
        
        prodAcum += (s1 * s2);
        m1 += s1;
        m2 += s2;
    }
    
    prodAcum /= (double)(cases.size());
    m1 /= (double)(cases.size());
    m2 /= (double)(cases.size());
    
    v = prodAcum - (m1 * m2);
    
    return (v);
}

 /**
   * Computes the correlation coefficient of the given variables. 
   *
   * @param position1 the position of the first variable.
   * @param position2 the position of the other variable.
   * @return the value of the correlation coefficient of the variables at positions
   * <code>position1</code> and <code>position2</code>.
   */
  public double correlation(int position1, int position2) {

    int i;
    double av1 = 0.0, av2 = 0.0, y11 = 0.0, y22 = 0.0, y12 = 0.0, c;
    int n = cases.size();
    double[] row;
    
    if (n <= 1) {
      return 1.0;
    }
    for (i = 0; i < n; i++) {
      row = (double [])cases.elementAt(i);
      av1 += row[position1];
      av2 += row[position2];
    }
    av1 /= (double) n;
    av2 /= (double) n;
    
    for (i = 0; i < n; i++) {
      row = (double [])cases.elementAt(i);
      y11 += (row[position1] - av1) * (row[position1] - av1);
      y22 += (row[position2] - av2) * (row[position2] - av2);
      y12 += (row[position1] - av1) * (row[position2] - av2);
    }
    
    if (y11 * y22 == 0.0) {
      c=1.0;
    } else {
      c = y12 / Math.sqrt(Math.abs(y11 * y22));
    }
    
    return c;
  }

  /**
   * Computes the correlation coefficient of the given variables and their means. 
   *
   * @param position1 the position of the first variable.
   * @param position2 the position of the other variable.
   * @param mean1 the mean of variable in position1
   * @param mean2 the mean of variable in position2 
   * @return the value of the correlation coefficient of the variables at positions
   * <code>position1</code> and <code>position2</code>.
   */
  public double correlation(int position1, int position2, double mean1, double mean2) {

    int i;
    double y11 = 0.0, y22 = 0.0, y12 = 0.0, c;
    int n = cases.size();
    double[] row;
    
    if (n <= 1) {
      return 1.0;
    }
    
    for (i = 0; i < n; i++) {
      row = (double [])cases.elementAt(i);
      y11 += (row[position1] - mean1) * (row[position1] - mean1);
      y22 += (row[position2] - mean2) * (row[position2] - mean2);
      y12 += (row[position1] - mean1) * (row[position2] - mean2);
    }
    
    if (y11 * y22 == 0.0) {
      c=1.0;
    } else {
      c = y12 / Math.sqrt(Math.abs(y11 * y22));
    }
    
    return c;
  }
  
  
  /**
 * 
 * @param fold
 * @param totalFolds
 * @return The cases to train the model
 */
public ContinuousCaseListMem trainCV(int fold, int totalFolds) {
    ContinuousCaseListMem returnCases;
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
    
    returnCases = new ContinuousCaseListMem(this.getVariables());
    returnCases.setCases(cases);
    return returnCases;
}


/**
 * 
 * @param fold
 * @param totalFolds
 * @return The cases to test the model
 */
public ContinuousCaseListMem testCV(int fold, int totalFolds) {
    ContinuousCaseListMem returnCases;
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
    
    returnCases = new ContinuousCaseListMem(this.getVariables());
    returnCases.setCases(cases);
    return returnCases;
}


/**
 * Used for k-fold cross validation.
 * @param fold
 * @param totalFolds
 * @return The cases to train the model
 */
public ContinuousCaseListMem getTrainCV(int fold, int totalFolds) {
    
    ContinuousCaseListMem returnCases;
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
    
    returnCases = new ContinuousCaseListMem(this.getVariables());
    returnCases.setCases(cases);
    return returnCases;
}


/**
 * Used for k-fold cross validation.
 * @param fold
 * @param totalFolds
 * @return The cases to test the model
 */

public ContinuousCaseListMem getTestCV(int fold, int totalFolds) {
    
    ContinuousCaseListMem returnCases;
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
    
    returnCases = new ContinuousCaseListMem(this.getVariables());
    returnCases.setCases(cases);
    return returnCases;
}

  
  
} // End of class
