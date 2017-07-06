/* LinearFunction.java */

package elvira.tools;

import java.util.Vector;
import elvira.Node;
import elvira.Continuous;
import elvira.ContinuousConfiguration;
import elvira.tools.QuadraticFunction;
import elvira.tools.ContinuousFunction;
import elvira.tools.statistics.math.Fmath;
import java.io.*;



/**
 * Implements a linear function of continuous variables
 * and the elementary operations with them.
 *
 * @since 19/7/2011
 */

public class LinearFunction extends ContinuousFunction implements Serializable{

/**
 * Variables for which the function is defined.
 */
private Vector variables;



/**
 * Creates an empty <code>LinearFunction</code>.
 */
  
public LinearFunction() {
  
  variables = new Vector();
  coefficients = new Vector();
}


/**
 * Creates a <code>LinearFunction</code> with a vector of variables
 * and a vector of coefficients.
 * The complete vectors are copied.
 *
 * @param vars a vector of variables.
 * @param coef a vector of coefficients.
 */

public LinearFunction(Vector vars, Vector coef) {


  variables = (Vector)vars.clone();
  coefficients = (Vector)coef.clone();
}

/**
 * Creates a <code>LinearFunction</code> from
 * a quadratic function. If we call this function
 * with a true quadratic function (order two terms)
 * then the information is lost. Only one variable
 * is kept.
 *
 * The complete vectors are copied. 
 * The functions getCoefficients and getVariables
 * do the copy.
 *
 * @param qf the quadratic function.
 * @param coef a vector of coefficients.
 */

public LinearFunction(QuadraticFunction qf) {


  variables = qf.getVariables();
  coefficients =  qf.getCoefficients();
}



/**
 * Gets the position of a variable in a linear function.
 * @param var a variable (<code>Node</code>).
 * @returns the position of <code>var</code> in the list of variables, or
 * -1 if <code>var</code> is not contained in the list.
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
 * Gets the variable stored in a given position.
 *
 * @param pos the position to seek.
 * @return  the variable at that position.
 */

public Continuous  getVar1(int pos) {
 
  Continuous var;
  
  var = (Continuous) variables.elementAt(pos);
  
  return var;
}

/**
 * Gets the second variable stored in a given position.
 * As it is a LinearFunction. It is always the null object
 * 
 *
 * @param pos the position to seek.
 * @return always the null object
 */

public Continuous getVar2(int pos) {
 
 
  
  return null;
}

/**
 * Inserts a pair (variable,coef) at the end of a linear function.
 * @param var a <code>Continuous</code> variable.
 * @param coef a <code>double</code>, the coefficient. 
 */

public void insert(Continuous var, double coef) {

  Double x;

  variables.addElement(var);
  x = new Double(coef);
  coefficients.addElement(x);
}


/**
 * Inserts a pair (variable,coef) at the end of a linear function.
 * @param var a <code>Continuous</code> variable.
 * @param coef a <code>double</code> object.
 */

public void insert(Continuous var, Double coef) {

  variables.addElement(var);
  
  coefficients.addElement(coef);
}



/*
 * It obtains the vector of coefficients
 *
 * @return a copy of  the  vector of the coefficients of the linear function
 *
*/

public Vector getCoefficients()

{ return (Vector) coefficients.clone();
}


/*
 * It obtains the vector of variables
 *
 * @return a copy of  the  vector of the variables of the linear function
 *
*/

public Vector getVariables()

{ return (Vector) variables.clone();
}

/**
 * It obtains the coefficient of a variable in a <code>LinearFunction</code>.
 * It returns 0 if the variable does not appear in the function.
 *
 * @param var the continuos variable.
 * @returns the value of the coefficient of <code>var</code> in the linear
 * function, or 0.0 if the variable is not in the linear function.
 */

public double getCoefficient(Continuous var) {
  
  int pos;
  double x;

  pos = indexOf(var);
  if (pos == -1)
    return 0.0;
  else {
    x =  ((Double) coefficients.elementAt(pos)).doubleValue();
    return x;
  }
}


/**
 * Gets the coefficient at a given position.
 *
 * @param por the position to seek.
 * @return the coefficient at that position.
 */

public double getCoefficient(int pos) {
 
  double x;
  
  x =  ((Double) coefficients.elementAt(pos)).doubleValue();
  return x;
}



/**
 * Gets the name of the class.
 * @return a <code>String</code> with the name of the class.
 */

public String getClassName() {
  
  return new String("LinearFunction");
}



/**
 * Gets the name of the variable stored in a given position.
 *
 * @param pos the position to seek.
 * @return the name of the variable at that position.
 */

public String getVarName(int pos) {
 
  Node var;
  
  var = (Node)variables.elementAt(pos);
  
  return var.getName();
}


/**
 * Adds a pair (variable,coefficient) to a linear function. 
 * If the variable is already in the linear function, then
 * coefficient is added to the previous coefficient
 * @param var the continuous variable to be added.
 * @param coef the coefficient of this variable in the linear function.
 */

public void addVariable(Continuous var, double coef) {
  
  Double x;
  int pos;
  
  pos = indexOf(var);
  
  if (pos == -1)
    insert(var,coef);
  else {
    x = new Double(coef + ((Double) coefficients.elementAt(pos)).doubleValue());
    coefficients.setElementAt(x,pos);
  }
}


/**
 * Adds a pair  (variable,coefficient) to a linear function. 
 * If the variable is already in the linear function, then
 * coefficient is added to the previous coefficient
 * @param var the continuous variable to be added.
 * @param coef the coefficient as a <code>Double</code> object.
 */

public void addVariable(Continuous var, Double coef) {
  
  Double x;
  int pos;
  
  pos = indexOf(var);
  
  if (pos == -1)
    insert(var,coef);
  else {
    x = new Double(coef.doubleValue() + ((Double) coefficients.elementAt(pos)).doubleValue());
    coefficients.setElementAt(x,pos);
  }
}


/** 
 * It sums two linear functions. One is given as argument. It creates a new
 * linear function which is the sum of the two.
 *
 * @param linearf the linear function to be summed to this.
 * @returns a new linear function with the result of the addition.
 */

public ContinuousFunction sumFunctions(ContinuousFunction cf) {

  int i, s;
  
  ContinuousFunction x;
  
    x =  cf.duplicate();
  
  
  s = variables.size();
  for (i=0 ; i<s ; i++)
    {
    x.addVariable((Continuous) variables.elementAt(i), (Double) coefficients.elementAt(i));
    }
 
  return x;
}


/** 
 * It multiplies two linear functions. It creates a new quadratic function
 * which is the result of the multiplication
 *
 * @param linearf the linear function to be multiplied to this.
 * @returns a new quadratic function with the result of the multiplication.
 */

public QuadraticFunction multiply(LinearFunction lf) {
  
  int i,j, s,r;
  
  QuadraticFunction qf;
  
    qf =  new QuadraticFunction();
  
  
  s = variables.size();  
  r = lf.variables.size();
  for (i=0 ; i<s ; i++)
    for(j=0;j<r;j++)
    {
      qf.insert((Continuous) variables.elementAt(i),
		(Continuous) lf.variables.elementAt(j), 
		((Double) coefficients.elementAt(i)).doubleValue()* ((Double) lf.coefficients.elementAt(j)).doubleValue());
    }
 
  return qf;
}

/** 
 * It restricts a linear function to the value of one variable,
 * by calculating the product of the value of the coefficient by the
 * given value and the remaining part of the linear 
 * function.
 * 
 * @param var the continuos variable.
 * @param x a double: the value of <code>var</code>.
 * @param f the linear function obtained from the actual one by removing
 * variable <code>var</code>. This object must be created before calling this method.
 * @return a double containing <code>x</code> by the coefficient
 * of <code>var</code>.
 */

public double restrict(Continuous var, double x, ContinuousFunction f) {  
  
  int pos, i, s;
  double coe;
  
  pos = indexOf(var);
  
  if (f == null){
    System.out.println("Error (restrict): The object f must be created before calling the method.");
    System.exit(1);
  }
  
  if (pos == -1)
    coe = 0.0;
  else
    coe = x * ((Double) coefficients.elementAt(pos)).doubleValue();
    
  s = variables.size();  

  for (i=0 ; i<variables.size() ; i++) {
    if (i != pos){
       f.insert((Continuous)variables.elementAt(i),(Double)coefficients.elementAt(i));
     }
  }
  
  return coe;
}


/** 
 * It restricts a linear function to the value of one variable,
 * by calculating the product of the value of the coefficient by the
 * given value and the remaining part of the linear 
 * function.
 * 
 * @param var the continuos variable.
 * @param x a <code>Double</code>: the value of <code>var</code>.
 * @param f the linear function obtained from the actual one by removing
 * variable <code>var</code>.This object must be created before calling this method.
 * @return a double containing <code>x</code> by the coefficient
 * of <code>var</code>.
 */

public double restrict(Continuous var, Double x, ContinuousFunction f) {  
  
  int pos, i, s;
  double coe;
  
  pos = indexOf(var);
 
  if (f == null){
    System.out.println("Error (restrict): The object f must be created before calling the method.");
    System.exit(1);
  }
  
  if (pos == -1)
    coe = 0.0;
  else
    coe = x.doubleValue() * ((Double) coefficients.elementAt(pos)).doubleValue();
  
  s = variables.size();  
  
  //f = new LinearFunction();
  
  for (i=0 ; i<s ; i++) {
    if (i != pos)
      f.insert((Continuous) variables.elementAt(i), (Double) coefficients.elementAt(i));
  }
  return coe;
}


/**
 * It obtains the factors afecting a variable in a continuous function
 *
 * @param var The continuous variable for which factors are going to be obtained
 * @param factor0 a linear function the part of the function not containing var
 * @param factor1 In this case an empty function.
 * @param ObjFactorvar1 A double object containing the coefficient of var in the function
 * @return always the value 0.0
 *
 **/
 
public double  getFactors(Continuous var,ContinuousFunction factor0, LinearFunction factor1, Double ObjFactorvar1)
{LinearFunction lf;
 double x;
 
 lf = new LinearFunction();
  x = restrict(var,1.0,lf);
 factor0 = new LinearFunction();
 factor1 = lf;
 ObjFactorvar1 = new Double(x);
 
 return 0.0;
}


/**
 * Obtains the factors afecting a variable in a continuous function.
 *
 * @param var The continuous variable for which factors are going to be obtained.
 * @param v a vector where the following will be inserted:
 * <ol>
 * <li> factor0: a continuous function representing
 *      the part of the function not containing <code>var</code>.
 * <li> factor1: in this case an empty function.
 * <li> ObjFactorvar1: a <code>Double</code> object containing the
 *      coefficient of <code>var</code> in the function.
 * </ol>
 * @return always the value 0.0
 */
 
public double getFactors(Continuous var, Vector v) {
  
  LinearFunction lf, factor0;
  double x;
  
 lf = new LinearFunction();
 x = restrict(var,1.0,lf);
 factor0 = new LinearFunction();
 v.addElement(lf);
 v.addElement((ContinuousFunction)factor0);
 v.addElement(new Double(x));

 return 0.0;
}


/** 
 * It restricts a linear function to a configuration of variables.
 * 
 * @param c the configuration.
 * @param f the linear function obtained from this by fixing the values of
 * the configuraion.f has to be a empty continuous function.
 * @return a double containing the sum of the values of the variables in
 * the configuration multiplied by their coefficients.
 */

public double restrict(ContinuousConfiguration c, ContinuousFunction f) {
  
  int i, s; 
  double x;
  int present[];
  Vector listVar;
    
  s = variables.size();
  x = 0.0;
  present = new int[s];
  listVar = c.getContinuousVariables(); 
  
  for (i=0 ; i<s ; i++)
    present[i] = listVar.indexOf(variables.elementAt(i));

   // f = new LinearFunction();
  
  for (i=0 ; i<s ; i++) {
    if (present[i] == -1)
      f.insert((Continuous) variables.elementAt(i), (Double) coefficients.elementAt(i));
    else
      x = x + c.getContinuousValue(present[i]) * ((Double) coefficients.elementAt(i)).doubleValue();
  }
  
  return x;
}


/** 
 * It obtains the value of a linear function for a value of one variable,
 * by calculating the value of the coefficient multiplied by the
 * value of the variables.
 * 
 * @param var the continuous variable.
 * @param x a double: the value of <code>var</code>.
 * @return <code>x</code> multiplied by the coefficient of <code>var</code>.
 */

public double getValue(Continuous var, double x) {
  
  int pos, i;
  double z;
  
  pos = indexOf(var);
  
  if (pos == -1)
    return 0.0;
  
  else {

      
    z = x * ((Double) coefficients.elementAt(pos)).doubleValue();
//      if (Double.isNaN(z))      System.out.println("multiplico "+x+" por "+((Double) coefficients.elementAt(pos)).doubleValue()+" n�coefici "+coefficients.size());
    return z;
  }
}


/** 
 * It obtains the value of a linear function for a value of one variable,
 * by calculating the value of the coefficient multiplied by the value of
 * the variables.
 * 
 * @param var the continuous variable.
 * @param x a <code>Double</code> object containing the value
 * of <code>var</code>.
 * @return the value of <code>x</code> multiplied by the coefficient
 * of <code>var</code>.
 */

public double getValue(Continuous var, Double x) {  
  
  int pos, i;
  double z;
  
  pos = indexOf(var);
  
  if (pos == -1)
    return 0.0;
  else {
    z= x.doubleValue() * ((Double) coefficients.elementAt(pos)).doubleValue();
    return z;
  }
}


/**
 * It obtains the value of a linear function for a given configuration.
 *
 * @param c the <code>ContinuousConfiguration</code> to be evaluated.
 * @return the value of the linear function in configuration <code>c</code>.
 */

public double getValue(ContinuousConfiguration c) {
  
  int i, j, s; 
  Vector listVar;
  double x;
  
  listVar = c.getContinuousVariables();
  s = listVar.size();
  x = 0.0;
  
 
  
  
  for (i=0 ; i<s ; i++) {
    j =  indexOf((Continuous) listVar.elementAt(i));
    if (j != -1) {
      x = x +  c.getContinuousValue(i) * ((Double) coefficients.elementAt(j)).doubleValue();
    }
  }
  
  return x;
}


/**
 * Copies a linear function making a deep copy of all the references
 * to the coefficients and the variables (these objects are 
 * not duplicated).
 *
 * @return the deep copy of the linear function.
 */

public ContinuousFunction duplicate() {

  LinearFunction f;
  
  f = new LinearFunction( variables, coefficients);
  return f;
}


/** 
 * This function determines whether a linear function is empty.
 *
 * @return <code>true</code> if the vector of variables and coefficients
 * is empty or <code>false</code> otherwise.
 */ 

public boolean isEmpty() {

  return variables.isEmpty(); 
}


/**
 * Prints the linear function to the standard output.
 */

public void print() {
  
  int i;
  
  if(getVariables().size()==0){
  System.out.println("The object is empty");
  return;
  
  }
  
  System.out.print(getCoefficient(0)+" * "+getVarName(0));
  
  for (i=1 ; i<variables.size() ; i++) {
    System.out.print(" + "+Fmath.truncate(getCoefficient(i),this.printPrecision)+" * "+getVarName(i));
  }
}

/**
 *Same that print, but the output is saved in a String
 */
public String ToStringWithParentesis(){
 
  int i;
  String t=new String("");
   
  String s=new String("("+t.valueOf(getCoefficient(0))+" * "+getVarName(0)+")");
  for (i=1 ; i<variables.size() ; i++) {
   s=s+" +("+t.valueOf(getCoefficient(i))+" * "+getVarName(i)+")";
  }
  
  return s;
    
}
/**
 *Same that print, but the output is saved in a String
 */
public String ToString(){
 
  int i;
  String t=new String("");
   
  String s=new String(t.valueOf(Fmath.truncate(getCoefficient(0),this.printPrecision))+" * "+getVarName(0));
  for (i=1 ; i<variables.size() ; i++) {
   s=s+"+"+t.valueOf(Fmath.truncate(getCoefficient(i),this.printPrecision))+" * "+getVarName(i);
  }
  
  return s;
    
}

/**
 * Saves the linear function to a file.
 *
 * @param p the <code>PrintWriter</code> where the function will be written.
 */

public void save(PrintWriter p) {
 
  int i;
  
  p.print(getCoefficient(0)+" * "+getVarName(0));
  
  for (i=1 ; i<variables.size() ; i++) {
    p.print(" + "+getCoefficient(i)+" * "+getVarName(i));
  }
}


/**
 * Saves the linear function to a file in R format.
 *
 * @param p the <code>PrintWriter</code> where the function will be written.
 */

public void saveR(PrintWriter p, String condition) {

  int i;

  p.print(getCoefficient(0)+" * "+getVarName(0)+condition);

  for (i=1 ; i<variables.size() ; i++) {
    p.print(" + "+getCoefficient(i)+" * "+getVarName(i)+condition);
  }
}


/**
 *  Return if the linear functions are equals.
 *  @param f is a Linear function.
 *  @return true, if they are equals.
 *          false, otherwise.
 *  
 */

public boolean equals(ContinuousFunction f){
    
    LinearFunction L;
    if (f.getClass()==QuadraticFunction.class){
         return ((QuadraticFunction)f).equals(this);
    }else if (f.getClass()==LinearFunction.class){
         L=(LinearFunction)f;
    }else
        return false;
        
 
    Vector newVars1 = (Vector)variables.clone();
    Vector newVars2 = (Vector)L.getVariables().clone();
    boolean equals=true;
    Continuous var;
    
    while(newVars1.size()>0 && newVars2.size()>0 && equals){
            var=(Continuous)newVars1.elementAt(0);
            
            if (L.indexOf(var)==-1)
                equals=false;
            else{
                double coef1=getCoefficient(var);                        
                double coef2=L.getCoefficient(var);
                
                //if (Math.abs(coef1-coef2)<=Math.pow(10,-var.getPrecision())){
                if( coef1 == coef2){  
		  newVars1.remove(var);
                    newVars2.remove(var);                
                }else
                    equals=false;
            }
    }
    
    if (equals && newVars1.size()==0 && newVars2.size()==0)
        return true;
    else 
        return false;
     
 
}



/**
 *  Simplifies the linear function.If a variables appears two or more times,
 *  the coefficients are added. 
 *  
 */

public void simplifyT(){

  int i = 0;
  int j;
  Continuous var;
  double coef1; 
  double coef2;
  //System.out.println("� simplifico este LinearFunction?");
  //print();
  
  while(i <  variables.size()){
    var = getVar1(i);
    j = i+1;
    
    while(j <  variables.size()){
          
      if(var.equals(getVar1(j))){// La vble aparece dos veces
	
	coef2 = getCoefficient(j);
	coef1 = getCoefficient(i);
	//System.out.println("Acabo de simplificar");
	coef1 = coef1 + coef2;
	coefficients.setElementAt(new Double(coef1),i);
	coefficients.removeElementAt(j);
	variables.removeElementAt(j);
	
	
      }else{
      
	j++;
      
      }
    
    }
    
    i++;
  }

}


/**
 * Removes a variable and its corresponding coefficient from the function.
 *
 * The object is modified.
 *
 * @param v the variable to remove.
 */
 
 public void removeVariable(Continuous v) {
     
     int p = indexOf(v);
     variables.removeElementAt(p);
     coefficients.removeElementAt(p);
 }

}
