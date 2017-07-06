/* ContinuousFunction.java */

package elvira.tools;

import java.util.Vector;
import elvira.Node;
import elvira.Continuous;
import elvira.ContinuousConfiguration;

import java.io.*;



/**
 * Implements a quadratic function of continuous variables
 * and the elementary operations with them.
 *
 * @since 8/2/2002
 */

public abstract class ContinuousFunction implements Serializable{

/*
 * This field fixes the number of decimal digits that are considered at the time of 
 * to display this function with the print() method. This field is defined to avoid
 * displaying coefficients with a many decimal digits.
 */
    
static int printPrecision=3;

/**
 * Coefficients of the variables.
 */
public Vector coefficients;





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
 * Gets the position of a variable in a quadratic or linear function.
 * It should appear as ... coef*var + ... and not multiplied by other variable.
 * @param var a variable (<code>Node</code>).
 * @returns the position of <code>var</code> in the list of variables, or
 * -1 if <code>var</code> is not contained in the list. 
 */

abstract public int indexOf(Node var);


/**
 * Gets the variable (linear function) or the first
 * variable (quadratic function) stored in a given position.
 *
 * @param pos the position to seek.
 * @return the name of the variable at that position.
 */

abstract public Continuous getVar1(int pos);

/**
 * Gets the second variable stored in a given position.
 * In a LinearFunction. It is always the null object
 * 
 *
 * @param pos the position to seek.
 * @return the found variable
 */

abstract public Continuous getVar2(int pos);


/**
 * Inserts a pair (variable,coef) at the end of a Continuous  function.
 * @param var a <code>Continuous</code> variable.
 * @param coef a <code>double</code>, the coefficient. 
 */

abstract  void insert(Continuous var, double coef);

/**
 * Inserts a pair (variable,coef) at the end of a Continuous function.
 * @param var a <code>Continuous</code> variable.
 * @param coef a <code>double</code> object. 
 */

abstract  void insert(Continuous var, Double coef) ;

/**
 * Adds a pair (variable,coefficient) to a continuous function. 
 * If the variable is already in the  function, then
 * coefficient is added to the previous coefficient
 * @param var the continuous variable to be added.
 * @param coef the coefficient of this variable in the linear function.
 */

public  void addVariable(Continuous var, double coef){

  
  Double x;
  int pos;
  
  pos = indexOf((Node) var);
  
  if (pos == -1)
    insert(var,coef);
  else {
    x = new Double(coef + ((Double) coefficients.elementAt(pos)).doubleValue());
    coefficients.setElementAt(x,pos);
  }
}


/**
 * Adds a pair  (variable,coefficient) to a constinuous function. 
 * If the variable is already in the quadratic  function, then
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
 * It multiplies a continuous function (linear or quadratic) by a double
 * It does it by multiplying all the coefficients by this number.
 * It modifies the calling function and it does not create a new one.
 *
 * 
 * @param x the double value.
 * 
 */

public void multiply(double x) 
{int i,s;
 Double y;
 
 s = coefficients.size();
   
   for(i=0;i<s;i++)
     { y = new Double(x* ((Double) coefficients.elementAt(i)).doubleValue());
    coefficients.setElementAt(y,i);}
}



/**
 * Gets the name of the class.
 * @return a <code>String</code> with the name of the class.
 */



abstract public String getClassName();

/** 
 * It sums two continuous  functions. One is given as argument. It creates a new
 * continuous  function which is the sum of the two.
 *
 * @param continuousf the continuous function to be summed to this.
 * @returns a new quadratic  function with the result of the addition.
 */

abstract public ContinuousFunction  sumFunctions(ContinuousFunction continuousf);


/** 
 * It restricts a continuous function to the value of one variable,
 * 
 * @param var the continuos variable.
 * @param x a double: the value of <code>var</code>.
 * @param q the quadratic function obtained from the actual one by removing
 * variable <code>var</code>.
 * @return a double containing <code>x</code> by the coefficient
 * of <code>var</code>.
 */

abstract  public double restrict(Continuous var, double x, ContinuousFunction q); 
  

/** 
 * It restricts a continuous function to the value (a double object)of one variable,
 *
 * @param var the continuos variable.
 * @param x a <code>Double</code>: a double object containing the value of <code>var</code>.
 * @param q the quadratic function obtained from the actual one by removing
 * variable <code>var</code>.
 * @return a double containing <code>x</code> by the coefficient
 * of <code>var</code>.
 */

abstract public double restrict(Continuous var, Double x, ContinuousFunction q);
 


/** 
 * It restricts a continuous  function to a configuration of variables.
 * 
 * @param c the configuration.
 * @param q the continuous function obtained from this by fixing the values of
 * the configuraion.
 * @return a double containing the sum of the independent terms obtained by fixing the variable
 */

abstract public double restrict(ContinuousConfiguration c, ContinuousFunction q);





/**
 * It obtains the value of a continuous function for a given configuration.
 * It is faster than restriction, as it does not create a new continuous function
 *
 * @param c the <code>ContinuousConfiguration</code> to be evaluated.
 * @return the value of the quadratic function in configuration <code>c</code>.
 */

abstract public double getValue(ContinuousConfiguration c);


/** 
 * It obtains the value of a continuous function for a value of one variable,
 * by calculating the value of the coefficient multiplied by the
 * value of the variables.
 * 
 * @param var the continuous variable.
 * @param x a double: the value of <code>var</code>.
 * @return <code>x</code> multiplied by the coefficient of <code>var</code>.
 */

abstract public double getValue(Continuous var, double x);


/** 
 * It obtains the value of a continuous function for a value of one variable,
 * by calculating the value of the coefficient multiplied by the
 * value of the variables.
 * 
 * @param var the continuous variable.
 * @param x a Double: the object containing the value of <code>var</code>.
 * @return the value of <code>x</code> multiplied by the coefficient of <code>var</code>.
 */

abstract public double getValue(Continuous var, Double x);


/**
 * It obtains the factors afecting a variable in a continuous function
 *
 * @param var The continuous variable for which factors are going to be obtained
 * @param factor0 a continuous function the part of the function not containing var
 * @param factor1 a linear function containing the part of the function afecting to var
 *                and other variable var*(c1*var1+...+ck*vark). It obtains the part
 *                  in parenthesis
 * @param ObjFactorvar1 A double object containing the coefficient of var in the function
 * @return the coefficient of var*var in the continuous function
 *
 **/
 
public abstract double  getFactors(Continuous var,ContinuousFunction factor0, LinearFunction factor1, Double ObjFactorvar1);


/**
 * Obtains the factors afecting a variable in a continuous function.
 *
 * @param var The continuous variable for which factors are going to be obtained.
 * @param v a vector where the following will be inserted:
 * <ol>
 * <li> factor0: a continuous function representing
 *      the part of the function not containing <code>var</code>.
 * <li> factor1: a linear function containing the part of the
 *      function afecting to <code>var</code> and another
 *      variable var*(c1*var1+...+ck*vark). It obtains the part
 *      inside the parenthesis.
 * <li> ObjFactorvar1: a <code>Double</code> object containing the
 *      coefficient of <code>var</code> in the function.
 * </ol>
 * @return the coefficient of <code>var*var</code> in the continuous function
 */
 
public abstract double getFactors(Continuous var, Vector v);


/**
 * Copies a continuous function making a deep copy of all the references
 * to the coefficients and the variables (these objects are 
 * not duplicated).
 *
 * @return the deep copy of the continuous function.
 */

abstract public ContinuousFunction duplicate();


/** 
 * This function determines whether a continuous function is empty.
 *
 * @return <code>true</code> if the vector of variables and coefficients
 * is empty or <code>false</code> otherwise.
 */ 

public boolean isEmpty() {

  return coefficients.isEmpty(); 
}


/**
 * Prints the continuous function to the standard output.
 */

public abstract void print();

/*
 * Return a String with the MixtExpDensity function
 */
public abstract String ToString();


/*
 * Return a String with the MixtExpDensity function. But each term of the 
 * function is intro a pàrenthesis.
 */

public abstract String ToStringWithParentesis();

/**
 * Saves a continuous function to a file.
 *
 * @param p the <code>PrintWriter</code> where the function will be written.
 */

public abstract void save(PrintWriter p);

/**
 *  Return if the Continuous Functions are equals.
 *  @param f is a Continuous Function.
 *  @return true, if they are equals.
 *          false, otherwise.
 *  
 */

public abstract boolean equals(ContinuousFunction f);

} // End of class
