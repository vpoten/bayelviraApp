/* QuadraticFunction.java */

package elvira.tools;

import java.util.Vector;
import elvira.Node;
import elvira.Continuous;
import elvira.ContinuousConfiguration;
import elvira.tools.LinearFunction;
import elvira.tools.statistics.math.Fmath;
import java.io.*;



/**
 * Implements a quadratic function of continuous variables
 * and the elementary operations with them.
 *
 * @since 8/2/2002
 */

public class QuadraticFunction extends ContinuousFunction {

/**
 * Variables for which the function is defined.
 */
private Vector variables;

/**
 * Second variables.
 */

private Vector variables2;




/**
 * Creates an empty <code>QuadraticFunction</code>.
 */

public QuadraticFunction() {

  variables = new Vector();
  variables2 = new Vector();
  coefficients = new Vector();
}


/**
 * Creates a <code>QuadraticFunction</code> with two vector of variables
 * and a vector of coefficients.
 * The complete vectors are copied.
 *
 * @param vars1 a vector of variables.
 * @param vars2 a vector of variables.
 * @param coef a vector of coefficients.
 */

public QuadraticFunction(Vector vars1, Vector vars2, Vector coef) {


  variables = (Vector)vars1.clone();
  variables2 = (Vector)vars2.clone();
  coefficients = (Vector)coef.clone();
}



/**
 * Creates a <code>QuadraticFunction</code>
 * from a linear function.
 * The complete vectors are copied.
 *
 * @param linearf a linear function.
**/

public QuadraticFunction(LinearFunction linearf) {
int i,n;

  variables = linearf.getVariables();
  coefficients = linearf.getCoefficients();
  variables2 = new Vector();
  n = coefficients.size();
  for(i=0;i<n;i++)
     {variables2.addElement(null);}
}


/**
* It creates a quadratic function that is the exponent of a Gaussian
* density with a mean that is a float and a constant
* standard deviation.
* @param var a continuous variable that follows the Gaussian density
* @param mean a double that  is the mean of the distribution
* @param deviation a double: the standard deviation of the distribution
*
**/

public QuadraticFunction(Continuous var, double mean, double deviation) {

Double x;
double y;

  variables = new Vector();
  variables2 = new Vector();
  coefficients = new Vector();

y =deviation*deviation;

  variables.addElement(var);
  variables2.addElement(var);
  x = new Double(-0.5/y);
  coefficients.addElement(x);

  variables.addElement(var);
 variables2.addElement(null);
 x = new Double(mean/y);
  coefficients.addElement(x);


}

/**
* It creates a quadratic function that is the exponent of a Gaussian
* density with a mean that is a double plus a linear function of a set of
* continuous variables and a constant
* standard deviation.
* @param var a continuous variable that follows the Gaussian density
* @param mean a double that  is the mean of the distribution
* @param fr a linear function expressing the dependence of the variable of conditional variables
* @param deviation a double: the standard deviation of the distribution
*
**/

public QuadraticFunction(Continuous var, double mean, LinearFunction fr, double deviation) {

Double x;
double  variance;
QuadraticFunction fq,fq2;
LinearFunction fl;


variance = deviation*deviation;


fq = fr.multiply(fr);


fq.multiply(-0.5/variance);


  fl = new LinearFunction();
  fl.addVariable(var,1.0);

  fq2 = fr.multiply(fl);

fq2.multiply(1.0/variance);


 fq   = (QuadraticFunction)   fq.sumFunctions((ContinuousFunction) fq2);


 fr.multiply(-mean/variance);

 fq   = (QuadraticFunction)  fq.sumFunctions((ContinuousFunction) fr);

  variables = fq.variables;

  variables2 = fq.variables2;
  coefficients = fq.coefficients;



  variables.addElement(var);
  variables2.addElement(var);
  x = new Double(-0.5/variance);
  coefficients.addElement(x);

  variables.addElement(var);
 variables2.addElement(null);
 x = new Double(mean/variance);
  coefficients.addElement(x);



}







/**
 * Gets the position of a variable in a quadratic function.
 * It should appear as ... coef*var + ... and not multiplied by other variable.
 * @param var a variable (<code>Node</code>).
 * @returns the position of <code>var</code> in the list of variables, or
 * -1 if <code>var</code> is not contained in the list.
 */

public int indexOf(Node var) {

  int i;
  Node aux,aux2;

  for (i=0 ; i<variables.size() ; i++) {
    aux = (Node)variables.elementAt(i);
     aux2 = (Node)variables2.elementAt(i);
     if ((aux.equals(var)) && (aux2 == null))
      return i;
  }

  return (-1);
}


/**
 * Gets the name of the class.
 * @return a <code>String</code> with the name of the class.
 */

public String getClassName() {
  
  return new String("QuadraticFunction");
}



/*
 * It obtains the vector of coefficients
 *
 * @return a copy of  the  vector of the coefficients of the quadratic function
 *
*/

public Vector getCoefficients()

{ return (Vector) coefficients.clone();
}


/*
 * It obtains the vector of variables
 *
 * @return a copy of  the  vector of the variables of the quadratic function
 *
*/

public Vector getVariables()

{ return (Vector) variables.clone();
}

/* It obtains the second vector of variables
 *
 * @return a copy of  the  vector of the variables of the quadratic function
 *
*/

public Vector getVariables2()

{ return (Vector) variables2.clone();
}


/**
 * Gets the position of a pair of  variables in a quadratic function.
 * It should appear as ... coef*var1*var2  + ... or ... coef*var2*var1  + ...
 * @param var1 a variable (<code>Node</code>).
 * @param var2 a variable (<code>Node</code>).
 * @returns the position of <code>(var1,var2)</code> in the lists of variables,variables2
 * -1 if <code>var</code> the pair is not contained in the list.
 */

public int indexOf(Node var1, Node var2) {

  int i;
  Node aux,aux2;

  for (i=0 ; i<variables.size() ; i++) {
    aux = (Node)variables.elementAt(i);
     aux2 = (Node)variables2.elementAt(i);
     if ( ((aux == var1) && (aux2 == var2))||((aux == var2) && (aux2 == var1))  )
      return i;
  }
  
  return (-1);
}


/**
 * Inserts a pair (variable,coef) at the end of a quadratic  function.
 * @param var a <code>Continuous</code> variable.
 * @param coef a <code>double</code>, the coefficient. 
 */

void insert(Continuous var, double coef) {

  Double x;

  variables.addElement(var);
  variables2.addElement(null);
  x = new Double(coef);
  coefficients.addElement(x);
}


/**
 * Inserts a pair (variable,coef) at the end of a Quadratic function.
 * @param var a <code>Continuous</code> variable.
 * @param coef a <code>double</code> object. 
 */

void insert(Continuous var, Double coef) {

  variables.addElement(var);
   variables2.addElement(null);
  coefficients.addElement(coef);
}


/**
 * Inserts a triple (var1,var2,coef) at the end of a quadratic  function.
 * @param var1 a <code>Continuous</code> variable.
 * @param var2 a <code>Continuous</code> variable.
 * @param coef a <code>double</code>, the coefficient. 
 */

public void insert(Continuous var1, Continuous var2, double coef) {

  Double x;

  variables.addElement(var1);
  variables2.addElement(var2);
  x = new Double(coef);
  coefficients.addElement(x);
}



/**
 * Inserts a triple (var1,var2,coef) at the end of a quadratic  function.
 * @param var1 a <code>Continuous</code> variable.
 * @param var2 a <code>Continuous</code> variable.
 * @param coef a <code>double</code> object, containing the coefficient. 
 */

private void insert(Continuous var1, Continuous var2, Double coef) {

  Double x;

  variables.addElement(var1);
  variables2.addElement(var2);
  coefficients.addElement(coef);
}

/**
 * It obtains the coefficient of a variable in a <code>QuadraticFunction</code>.
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
 * It obtains the coefficient of a couple of variables in a <code>QuadraticFunction</code>.
 * It returns 0 if the variables do not appear in the function.
 *
 * @param var1 a continuous variable.
 * @param var2 a continuous variable
 * @returns the value of the coefficient of <code>var</code> in the linear
 * function, or 0.0 if the variable is not in the linear function.
 */

public double getCoefficient(Continuous var1, Continuous var2) {
  
  int pos;
  double x;
  
  pos = indexOf(var1,var2);
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
 * Gets the name of the first variable stored in a given position.
 *
 * @param pos the position to seek.
 * @return the name of the variable at that position.
 */

public String getVar1Name(int pos) {
 
  Node var;
  
  var = (Node)variables.elementAt(pos);
  
  return var.getName();
}

/**
 * Gets the name of the second variable stored in a given position.
 *
 * @param pos the position to seek.
 * @return the name of the variable at that position.
 */

public String getVar2Name(int pos) {

  Node var;
  
  var = (Node)variables2.elementAt(pos);
  
  return var.getName();
}

/**
 * Gets the first variable stored in a given position.
 *
 * @param pos the position to seek.
 * @return the name of the variable at that position.
 */

public Continuous getVar1(int pos) {
 
  Continuous var;
  
  var = (Continuous)variables.elementAt(pos);
  
  return var;
}

/**
 * Gets the second variable stored in a given position.
 *
 * @param pos the position to seek.
 * @return the name of the variable at that position.
 */

public Continuous  getVar2(int pos) {

  Continuous var;
  
  var = (Continuous)variables2.elementAt(pos);
  
  return var;
}

/**
 * Adds a pair (variable,coefficient) to a quadratic function. 
 * If the variable is already in the quadratic  function, then
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
 * Adds a pair  (variable,coefficient) to a quadratic function. 
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
 * Adds a triple (var1,var2,coefficient) to a quadratic function. 
 * If the couple of variables is already in the quadratic  function, then
 * coefficient is added to the previous coefficient
 * @param var1 the first continuous variable to be added.
 * @param var2 the second continuous variable to be added.
 * @param coef the coefficient of this variable in the linear function.
 */

public void addVariable(Continuous var1, Continuous var2, double coef) {
  
  Double x;
  int pos;
  
  pos = indexOf(var1,var2);

  if (pos == -1)
    insert(var1,var2,coef);
  else {
    x = new Double(coef + ((Double) coefficients.elementAt(pos)).doubleValue());
    coefficients.setElementAt(x,pos);
  }
}




/**
 * Adds a triple (var1,var2,coefficient) to a quadratic function.
 * If the couple of variables is already in the quadratic  function, then
 * coefficient is added to the previous coefficient
 * @param var1 the first continuous variable to be added.
 * @param var2 the second continuous variable to be added.
 * @param coef a double object containing the coefficient of this variable in the linear function.
 */

public void addVariable(Continuous var1, Continuous var2, Double coef) {

  Double x;
  int pos;

  pos = indexOf(var1,var2);

  if (pos == -1)
    insert(var1,var2,coef);
  else {
    x = new Double(coef.doubleValue() + ((Double) coefficients.elementAt(pos)).doubleValue());
    coefficients.setElementAt(x,pos);
  }
}






/**
 * It sums two quadratic  functions. One is given as argument. It creates a new
 * quadratic  function which is the sum of the two.
 *
 * @param quadraticf the linear function to be summed to this.
 * @returns a new quadratic  function with the result of the addition.
 */

public ContinuousFunction  sumFunctions(ContinuousFunction cf) {

  int i, s;

  QuadraticFunction x;

  x = new QuadraticFunction(variables,variables2,coefficients);

  s = cf.coefficients.size();
  for (i=0 ; i<s ; i++)
    x.addVariable((Continuous) cf.getVar1(i),(Continuous) cf.getVar2(i), (Double) cf.coefficients.elementAt(i));

  return x;
}


/**
 * It restricts a quadratic function to the value of one variable,
 *
 * @param var the continuos variable.
 * @param x a double: the value of <code>var</code>.
 * @param q the quadratic function obtained from the actual one by removing
 * variable <code>var</code>.
 * @return a double containing <code>x</code> by the coefficient
 * of <code>var</code>.
 */

public double restrict(Continuous var, double x, ContinuousFunction q) {

  int pos, i, s;
  double coe;
  QuadraticFunction qr;
  
  
 coe=0.0;
  
  
  s = variables.size();  
  //qr = new QuadraticFunction();
  qr=(QuadraticFunction)q;
  
  for (i=0 ; i<variables.size() ; i++) {
    if  (variables.elementAt(i) == var) 
      {if (variables2.elementAt(i) == null)
        { coe+=((Double) coefficients.elementAt(i)).doubleValue()*x;}
       else 
	 { if (variables2.elementAt(i) == var)
	   {coe+=((Double) coefficients.elementAt(i)).doubleValue()*x*x;}
	   else
	     { qr.insert( (Continuous)variables2.elementAt(i),((Double)coefficients.elementAt(i)).doubleValue()*x);
	     }
	 }
      }
    else 
      { if (variables2.elementAt(i) == var)
	{qr.insert( (Continuous)variables.elementAt(i),((Double)coefficients.elementAt(i)).doubleValue()*x);
	}
	else 
	  {qr.insert((Continuous)variables.elementAt(i),(Continuous)variables2.elementAt(i),(Double)coefficients.elementAt(i));
	  }
      }
  }
  
 //q= qr;
  return coe;
}


/** 
 * It restricts a quadratic function to the value (a double object)of one variable,
 *
 * @param var the continuos variable.
 * @param x a <code>Double</code>: a double object containing the value of <code>var</code>.
 * @param q the quadratic function obtained from the actual one by removing
 * variable <code>var</code>.
 * @return a double containing <code>x</code> by the coefficient
 * of <code>var</code>.
 */

public double restrict(Continuous var, Double x, ContinuousFunction q) {  
  
  int  i, s;
  double coe;
  QuadraticFunction qr;
  
  
coe=0.0;
  
  
  s = variables.size();  
  //qr = new QuadraticFunction();
  qr=(QuadraticFunction)q;
  
  
  for (i=0 ; i<variables.size() ; i++) {
    if  (variables.elementAt(i) == var) 
      {if (variables2.elementAt(i) == null)
        { coe+=((Double) coefficients.elementAt(i)).doubleValue()*x.doubleValue();}
       else 
	 { if (variables2.elementAt(i) == var)
	   {coe+=((Double) coefficients.elementAt(i)).doubleValue()*x.doubleValue()*x.doubleValue();}
	   else
	     { qr.insert( (Continuous)variables2.elementAt(i),((Double)coefficients.elementAt(i)).doubleValue()*x.doubleValue());
	     }
	 }
      }
    else 
      { if (variables2.elementAt(i) == var)
	{qr.insert( (Continuous)variables.elementAt(i),((Double)coefficients.elementAt(i)).doubleValue()*x.doubleValue());
	}
	else 
	  {qr.insert((Continuous)variables.elementAt(i),(Continuous)variables2.elementAt(i), (Double)coefficients.elementAt(i));
	  }
      }
  }
  
 //q= qr;
  return coe;
}
 


/** 
 * It restricts a quadratic  function to a configuration of variables.
 * 
 * @param c the configuration.
 * @param q the quadratic function obtained from this by fixing the values of
 * the configuraion.
 * @return a double containing the sum of the independent terms obtained by fixing the variable
 */

public double restrict(ContinuousConfiguration c, ContinuousFunction q) {
  
  int i, s,pos1,pos2; 
  double x;
  Vector listVar;
  Continuous var1,var2;
  QuadraticFunction qr;
  
  s = variables.size();
  x = 0.0;
  listVar = c.getContinuousVariables(); 
  //qr = new QuadraticFunction();  
  qr=(QuadraticFunction)q;
   
  for (i=0 ; i<variables.size() ; i++) {
    var1= (Continuous) variables.elementAt(i);
    var2=(Continuous) variables2.elementAt(i);
    pos1=listVar.indexOf(var1);
    pos2=listVar.indexOf(var2);
    if  (pos1 == -1)
      { if ( (pos2 != -1) && (var2 != null))
          { qr.insert( var1, ((Double) coefficients.elementAt(i)).doubleValue()* c.getContinuousValue(pos2));
	  }
	if   (pos2 == -1)
	  {qr.insert(var1,var2, (Double) coefficients.elementAt(i));
	  }
      }
    else 
      { if (var2==null)
	 { x+= ((Double) coefficients.elementAt(i)).doubleValue() * c.getContinuousValue(pos1) ;
  	 }
	else 
	  { if (pos2 == -1)
	    { qr.insert(var2, ((Double) coefficients.elementAt(i)).doubleValue()*c.getContinuousValue(pos1) );
	    }
	    else 
	      { x+= ((Double) coefficients.elementAt(i)).doubleValue()*  c.getContinuousValue(pos1)*c.getContinuousValue(pos2);
	      }
	  }
      }
	

  }
  //q = qr;
  return x;
  
}


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
 
public  double  getFactors(Continuous var,ContinuousFunction factor0, LinearFunction factor1, Double ObjFactorvar1)
{  int  i, s;
  double coe2,coe1;
 QuadraticFunction qf;
  
    
 coe1=0.0;
  
  
  s = variables.size();  
  qf = new QuadraticFunction();
  factor1 = new LinearFunction();
  coe2=0.0;
  
  for (i=0 ; i<variables.size() ; i++) {
    if  (variables.elementAt(i) == var) 
      {if (variables2.elementAt(i) == null)
        { coe1+=((Double) coefficients.elementAt(i)).doubleValue();}
       else 
	 { if (variables2.elementAt(i) == var)
	   {coe2+=((Double) coefficients.elementAt(i)).doubleValue();}
	   else
	     { factor1.insert( (Continuous)variables2.elementAt(i),(Double)coefficients.elementAt(i)) ;
	     }
	 }
      }
    else 
      { if (variables2.elementAt(i) == var)
	{factor1.insert( (Continuous)variables.elementAt(i),(Double)coefficients.elementAt(i));
	}
	else 
	  {qf.insert((Continuous)variables.elementAt(i),(Continuous)variables2.elementAt(i),(Double)coefficients.elementAt(i));
	  }
      }
  }
  
 factor0 =  (ContinuousFunction) qf;
 ObjFactorvar1 = new Double(coe1);
  return coe2;
  
}


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
 
public double getFactors(Continuous var, Vector v) {

  int  i, s;
  double coe2 = 0.0, coe1 = 0.0;
  QuadraticFunction qf;
  LinearFunction f1;
  
  s = variables.size();  
  qf = new QuadraticFunction();
  f1 = new LinearFunction();
  
  for (i=0 ; i<variables.size() ; i++) {
    if  (((Node)variables.elementAt(i)).getName() == var.getName()) {
      if (variables2.elementAt(i) == null) {
	coe1 += ((Double) coefficients.elementAt(i)).doubleValue();
      }
      else {
	if (((Node)variables2.elementAt(i)).getName() == var.getName()){
	  coe2 += ((Double) coefficients.elementAt(i)).doubleValue();
	}
	else {
	  f1.insert( (Continuous)variables2.elementAt(i),(Double)coefficients.elementAt(i));
	}
      }
    }
    else if (variables2.elementAt(i) != null) {
      if (((Node)variables2.elementAt(i)).getName() == var.getName()){
	f1.insert( (Continuous)variables.elementAt(i),(Double)coefficients.elementAt(i));
      }
      else {
	qf.insert((Continuous)variables.elementAt(i),(Continuous)variables2.elementAt(i),(Double)coefficients.elementAt(i));
      }
    }else{
       	qf.insert((Continuous)variables.elementAt(i),(Double)coefficients.elementAt(i));
    }
        
  }
  

  v.addElement((ContinuousFunction)qf);
  v.addElement(f1);
  v.addElement(new Double(coe1));
  
  return coe2;
}


/** 
 * It obtains the value of a quadratic function for a value of one variable,
 * by calculating the value of the coefficient multiplied by the
 * value of the variables.
 * 
 * @param var the continuous variable.
 * @param x a double: the value of <code>var</code>.
 * @return <code>x</code> multiplied by the coefficient of <code>var</code>.
 */

 public double getValue(Continuous var, double x)
{  
  
  int i, s;
  double coe;
  
  
  
 coe=0.0;
  
  
  s = variables.size();  
  
  
  
  for (i=0 ; i<variables.size() ; i++) {
    if  (variables.elementAt(i) == var) 
      {if (variables2.elementAt(i) == null)
        { coe+=((Double) coefficients.elementAt(i)).doubleValue()*x;}
       else 
	 { if (variables2.elementAt(i) == var)
	   {coe+=((Double) coefficients.elementAt(i)).doubleValue()*x*x;}
	   
	 }
      }
   
  }
  return coe;
}

    
    
    

/** 
 * It obtains the value of a quadratic function for a value of one variable,
 * by calculating the value of the coefficient multiplied by the
 * value of the variables.
 * 
 * @param var the continuous variable.
 * @param x a Double: the object containing the value of <code>var</code>.
 * @return the value of <code>x</code> multiplied by the coefficient of <code>var</code>.
 */

 public double getValue(Continuous var, Double x)

{  
  
  int i, s;
  double coe;
  
  
  
 coe=0.0;
  
  
  s = variables.size();  
  
  
  
  for (i=0 ; i<variables.size() ; i++) {
    if  (variables.elementAt(i) == var) 
      {if (variables2.elementAt(i) == null)
        { coe+=((Double) coefficients.elementAt(i)).doubleValue()*x.doubleValue();}
       else 
	 { if (variables2.elementAt(i) == var)
	   {coe+=((Double) coefficients.elementAt(i)).doubleValue()*x.doubleValue()*x.doubleValue();}
	   
	 }
      }
   
  }
  return coe;
}

    


/**
 * It obtains the value of a quadratic function for a given configuration.
 * It is faster than restriction, as it does not create a new quadratic function
 *
 * @param c the <code>ContinuousConfiguration</code> to be evaluated.
 * @return the value of the quadratic function in configuration <code>c</code>.
 */

public double getValue(ContinuousConfiguration c) {


  
  int i, s,pos1,pos2; 
  double x;
  Vector listVar;
  Node var1,var2;
  
  s = variables.size();
  x = 0.0;
  listVar = c.getContinuousVariables(); 
 
  
   
  for (i=0 ; i<variables.size() ; i++) {
    var1=(Continuous) variables.elementAt(i);
    var2=(Continuous) variables2.elementAt(i);
    pos1=listVar.indexOf(var1);
    pos2=listVar.indexOf(var2);
    if  (pos1 != -1) 
      { if (var2==null)
	 { x+= ((Double) coefficients.elementAt(i)).doubleValue() * c.getContinuousValue(pos1);
  	 }
	else 
	  { if (pos2 != -1) 
	      { x+= ((Double) coefficients.elementAt(i)).doubleValue()*  c.getContinuousValue(pos1)*c.getContinuousValue(pos2);
	      }
	  }
      }
  }	

  
  
  
  
  return x;
}


/**
 * Copies a quadratic function making a deep copy of all the references
 * to the coefficients and the variables (these objects are 
 * not duplicated).
 *
 * @return the deep copy of the linear function.
 */

public ContinuousFunction duplicate() {

  QuadraticFunction f;
  
  f = new QuadraticFunction( variables, variables2, coefficients);
  return f;
}


/**
 * This function determines whether a quadratic function is empty.
 *
 * @return <code>true</code> if the vector of variables and coefficients
 * is empty or <code>false</code> otherwise.
 */

public boolean isEmpty() {

  return variables.isEmpty();
}


/**
 * This function determines whether a quadratic function is really a linear function.
 * i.e. there is not quadratic terms in it
 * @return <code>true</code> if the vector of variables and coefficients
 * is empty or <code>false</code> otherwise.
 */

public boolean isLinear() {
int i,n;

  n = coefficients.size();

for(i=0; i< n; i++)
{

if (variables2.elementAt(i) !=null)
  {return(false);
  }

}  
return(true);
}






/**
 * Prints the quadratic function to the standard output.
 */

public void print() {
  
  int i;
  
  System.out.print(getCoefficient(0)+" * "+getVar1Name(0));
  if (variables2.elementAt(0) != null) 
    {
    if (getVar1Name(0).equals(getVar2Name(0)))
         {System.out.print("^2 ");}
      else{System.out.print(" * "+getVar2Name(0));}
  }


  for (i=1 ; i<variables.size() ; i++) {
    System.out.print(" + "+getCoefficient(i)+" * "+getVar1Name(i));
     if (variables2.elementAt(i) != null)
    {     if (getVar1Name(i).equals( getVar2Name(i)))
         {System.out.print("^2 ");}
     else {System.out.print(" * "+getVar2Name(i));}
  }
}
}

/**
 * Saves the quadratic function to a file.
 *
 * @param p the <code>PrintWriter</code> where the function will be written.
 */

public void save(PrintWriter p) {
 
  int i;
  
  p.print(getCoefficient(0)+" * "+getVar1Name(0));
  if (variables2.elementAt(0) != null) 
    { 
       if (getVar1Name(0).equals(getVar2Name(0)))
        {p.print("^2 ");}
         else
      {p.print(" * "+getVar2Name(0));}
   }
  for (i=1 ; i<variables.size() ; i++) {
    p.print(" + "+getCoefficient(i)+" * "+getVar1Name(i));
     if (variables2.elementAt(i) != null)
      if (getVar1Name(i).equals( getVar2Name(i)))
         {p.print("^2 ");}
    else { p.print(" * "+getVar2Name(i));}
  }
}


/**
* Convert to string a QuadraticFunction
*/
public String ToString(){
   int i;
   String t=new String("");

  String s=new String(t.valueOf(Fmath.truncate(getCoefficient(0),this.printPrecision))+" * "+getVar1Name(0));
  if (variables2.elementAt(0) != null)
    {
     if (getVar1Name(0).equals(getVar2Name(0))) { s=s+"^2";}
    else {   s=s+" * "+t.valueOf(getVar2Name(0));}
    }

    for (i=1 ; i<variables.size() ; i++) {
          s=s+"+"+ t.valueOf(Fmath.truncate(getCoefficient(i),this.printPrecision))+" * "+getVar1Name(i);
          if (variables2.elementAt(i) != null)
	    {    if (getVar1Name(i).equals(getVar2Name(i))) { s=s+"^2";}
            else     {   s=s+" * "+t.valueOf(getVar2Name(i));}
            }
          
    }

    return s;
}

/*
 * Return a String with the MixtExpDensity function. But each term of the 
 * function is intro a pàrenthesis.
 */
public String ToStringWithParentesis(){
   int i;
   String t=new String("");

  String s=new String(t.valueOf("("+getCoefficient(0))+" * "+getVar1Name(0));
  if (variables2.elementAt(0) != null)
    {
     if (getVar1Name(0).equals(getVar2Name(0))) { s=s+"^2)";}
    else {   s=s+" * "+t.valueOf(getVar2Name(0))+")";}
    }else s=s+")";

    for (i=1 ; i<variables.size() ; i++) {
          s=s+" + (" + t.valueOf(getCoefficient(i))+" * "+getVar1Name(i);
          if (variables2.elementAt(i) != null)
	    {    if (getVar1Name(i).equals(getVar2Name(i))) { s=s+"^2 )";}
            else     {   s=s+" * "+t.valueOf(getVar2Name(i))+")";}
            }
          else s=s+")";
    }

    return s;
}

/**
 *  Return if the quadratic functions are equals.
 *  @param f is a quadratic function.
 *  @return true, if they are equals.
 *          false, otherwise.
 *  
 */

public boolean equals(ContinuousFunction f) {
    //Not implemented yet
    
    QuadraticFunction Q;

    if (f.getClass()==LinearFunction.class)
        Q=new QuadraticFunction((LinearFunction)f);
    else if (f.getClass()==QuadraticFunction.class)
        Q=(QuadraticFunction)f;
    else
        return false;
    
    Vector newVars11 = (Vector)variables.clone();
    Vector newVars12 = (Vector)variables2.clone();    
    Vector newVars21 = (Vector)Q.getVariables().clone();
    Vector newVars22 = (Vector)Q.getVariables2().clone();    
    boolean equals=true;
    Continuous var1,var2;
    
    while(newVars11.size()>0 && newVars21.size()>0 && equals){
            var1=(Continuous)newVars11.elementAt(0);
            var2=(Continuous)newVars12.elementAt(0);
            
            if (Q.indexOf(var1,var2)==-1)
                equals=false;
            else{
                double coef1=getCoefficient(var1,var2);                        
                double coef2=Q.getCoefficient(var1,var2);
                
                if (Math.abs(coef1-coef2)<=Math.pow(10,-var1.getPrecision())){
                    newVars11.remove(var1);
                    newVars12.remove(var2);  
                    
                    if (Q.getVar1(Q.indexOf(var1,var2)).getName()==var1.getName()){
                        newVars21.remove(var1);
                        newVars22.remove(var2);                                    
                    }else{
                        newVars21.remove(var2);
                        newVars22.remove(var1);                                    
                    }
                }else
                    equals=false;
            }
    }
    
    if (equals && newVars11.size()==0 && newVars21.size()==0)
        return true;
    else 
        return false;
     
    
    
}

/*
public void simplify(){
    
    Vector var1=new Vector();
    Vector var2=new Vector();
    Vector coef=new Vector();
    
    //for (int i=0;i<this.variables.size();i++){
          //Node n1=(Node)this.variables.elementAt(i);
    while(this.variables.size()>0){
      
        Node n1=this.variables.firstElement();
        double c=((Double)this.coefficients.firstElement());
        var1.addElement(n1);
        var2.addElement(this.variables2.firstElement());
        double c=((Double)this.coefficients.firstElement()).doubleValue();
        //coef.addElement(this.coefficients.firstElement());
        
        this.variables.removeElementAt(0);
        this.variables2.removeElementAt(0);
        this.coefficients.removeElementAt(0);

        boolean cuadrado;
        if (this.variables2.firstElement()==null){
            cuadrado=false;
            Vector remove=new Vector();
            for (int j=0;j<this.variables.size();j++){
                Node n2=(Node)this.variables.elementAt(j);
                double d=0;
                if (n1.equals(n2) && this.variables2.elementAt(j)==null){
                    d+=((Double)this.coefficients.elementAt(j)).doubleValue();
                    remove.addElement(new Integer(j));
                }
            }
            c+=d;
            coef.addElement(new Double(c));
            for (int j=0; remove.size();j++){
                this.variables.remove(((Integer)remove.elementAt(j)).intValue());
                this.variables2.remove(((Integer)remove.elementAt(j)).intValue());
                this.coefficients.remove(((Integer)remove.elementAt(j)).intValue());
            }
            
        }else{
            cuadrado=true;
            Vector remove=new Vector();
            for (int j=0;j<this.variables.size();j++){
                Node n2=(Node)this.variables.elementAt(j);
                double d=0;
                if (n1.equals(n2) && this.variables2.elementAt(j)==null){
                    d+=((Double)this.coefficients.elementAt(j)).doubleValue();
                    remove.addElement(new Integer(j));
                }
            }
            c+=d;
            coef.addElement(new Double(c));
            for (int j=0; remove.size();j++){
                this.variables.remove(((Integer)remove.elementAt(j)).intValue());
                this.variables2.remove(((Integer)remove.elementAt(j)).intValue());
                this.coefficients.remove(((Integer)remove.elementAt(j)).intValue());
            }

        
        }

        
        
        while(this.variables.size()>0){
                    Node n2=this.variables.firstElement();
         }
                        
        
    }
    
    
}
 */
} // End of class

