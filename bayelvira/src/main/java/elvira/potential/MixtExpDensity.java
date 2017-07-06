/* MixtExpDensity.java */

package elvira.potential;

import java.util.Vector;
import java.io.*;
import elvira.*;
import elvira.tools.ContinuousFunction;
import elvira.tools.LinearFunction;
import elvira.tools.QuadraticFunction;
import elvira.tools.statistics.math.Fmath;
import java.lang.Math;

/**
 * Class <code>MixtExpDensity</code>: This class represents
 * densities for continuos variables. The general type of
 * the densities is a linear combination of exponentials of
 * continuous (linear or quadratic) functions,
 * as  0.2 + 0.4*exp(X1-X2) + 0.3*exp(0.2*X2-0.3*X3)
 *
 * @since 19/7/2011
 * @author Serafin Moral.
 * @version 1.33
 */

public class MixtExpDensity implements Serializable {
    
    static final long serialVersionUID = 2040380578900631274L;
    /**
     * The independent term.
     */
    double independent;
    
    /**
     * The vector of factors that multiply each exponential function.
     */
    Vector factors;
    
    /**
     * The vector of exponents of the exponential functions.
     * They are linear or quadratic functions.
     */
    Vector terms;

   /**
    * This field fixes the number of decimal digits that are considered at the time of
    * to display this function with the print() method. This field is defined to avoid
    * displaying coefficients with a many decimal digits.
    */
    static int printPrecision=3;

    /**
     * Creates a density which is contant and equal to 1.
     */
    
    public MixtExpDensity()  {
        
        independent = 1.0;
        factors = new Vector();
        terms = new Vector();
        
        
    }
    
    
    /**
     * Creates a Gaussian Density given a mean and an standard deviation
     * @param var the continuos variable following the Gaussian density
     * @param mean the mean of the density
     * @param deviation, the standard deviation
     */
    
    public MixtExpDensity(Continuous var, double mean, double deviation)  {
        
        QuadraticFunction f;
        double x;
        
        independent = 0.0;
        f = new QuadraticFunction(var,mean,deviation);
        
        factors = new Vector();
        terms = new Vector();
        terms.addElement(f);
        x = Math.exp(-0.5*(mean*mean)/(deviation*deviation))/(deviation*Math.sqrt(2*Math.PI));
        factors.addElement(new Double(x));
    }
    
    
    /**
     * Creates a conditional Gaussian Density given a mean a linear function of conditioning variables
     *  and an standard deviation
     * @param var the continuos variable following the Gaussian density
     * @param mean the mean of the density
     * @param lf the linear function to be added to the mean
     * @param deviation, the standard deviation
     */
    
    public MixtExpDensity(Continuous var, double mean, LinearFunction lf, double deviation)  {
        
        QuadraticFunction f;
        double x;
        
        independent = 0.0;
        f = new QuadraticFunction(var,mean,lf,deviation);
        
        factors = new Vector();
        terms = new Vector();
        terms.addElement(f);
        x = Math.exp(-0.5*(mean*mean)/(deviation*deviation))/(deviation*Math.sqrt(2*Math.PI));
        factors.addElement(new Double(x));
    }
    
    
    /**
     * Creates a density which is contant and equal to x.
     * @param x the independent term of the new density.
     */
    
    public MixtExpDensity(double x)  {
        
        independent = x;
        factors = new Vector();
        terms = new Vector();
    }
    
    
    /**
     * Creates a density with two exponentials and an independent term.
     *    a*exp(b*x)+d*exp(f*x)+k
     *   It will be very used, for example when learning an MTE density
     */
    
    public MixtExpDensity(double a, double b, double d, double f, double k, Continuous var){

        
        factors = new Vector();
        terms = new Vector();
        
        LinearFunction exponent1, exponent2;
        
        if((b!=0)&&(f!=0)){
            
            independent = k;
            
            factors.addElement(new Double(a));
            factors.addElement(new Double(d));
            
            exponent1 = new LinearFunction();
            exponent1.addVariable(var,b);
            
            exponent2 = new LinearFunction();
            exponent2.addVariable(var,f);
            
            terms.addElement(exponent1);
            terms.addElement(exponent2);
            
        }//End of if
        
        else if(b==0){
            
            independent = k+a;
            
            factors.addElement(new Double(d));
            
            exponent2 = new LinearFunction();
            exponent2.addVariable(var,f);
            
            terms.addElement(exponent2);
            
            
        }//End of else-if
        
        else if(d==0){
            
            independent = k+d;
            
            factors.addElement(new Double(a));
            
            exponent1 = new LinearFunction();
            exponent1.addVariable(var,b);
            
            terms.addElement(exponent1);
            
        }//End of else if
        
        else{//Both exponential terms have coefficients zero
            
            independent = k+d+a;
            
        }//End of else
        
        
    }//End of constructor
    
    
    /**
     * Sets the independent term of the density (constant part).
     *
     * @param x a double that will be assigned to the independent term.
     */
    
    public void  setIndependent(double x) {
        
        independent = x;
    }
    
    
    /**
     * Adds a quantity to the independent term of the density
     * (constant part).
     *
     * @param x a double that will be added to the independent term.
     */
    
    public void  addIndependent(double x) {
        
        independent += x;
    }
    
    
    /**
     * Adds a term to the density.
     * The new function will be the old one with the addition
     * of x*exp(f)
     *
     * @param x a double: the coefficient of the exponential of
     * the linear function.
     * @param f a continuous function to appear in the exponent of
     * the term.
     */
    
    public void addTerm(double x, ContinuousFunction f) {
        
        factors.addElement(new Double(x));
        terms.addElement(f);
    }
    
    
    /**
     * Adds a term to the density.
     * The new function will be the old one with the addition
     * of valueOf(x)*exp(f)
     * @see addterm(double x, ContinuousFunction f) that is equal
     * except that now we have a <code>Double</code> object.
     * @param x a <code>Double</code> object: the coefficient of the
     * exponential of the linear function.
     * @param f a continuous function to appear in the exponent of the
     * term.
     */
    
    public void addTerm(Double x, ContinuousFunction f) {
        
        factors.addElement(x);
        terms.addElement(f);
    }
    
    
    /**
     * It carries out a deep copy of a density.
     *
     * @return the deep copy of the calling density
     */
    
    public MixtExpDensity duplicate() {
        
        MixtExpDensity aux;
        int i, s;
        
        aux = new MixtExpDensity(independent);
        s = terms.size();
        for (i=0 ; i<s ; i++)
            aux.addTerm((Double)factors.elementAt(i),((ContinuousFunction)terms.elementAt(i)).duplicate());
        return aux;
    }
    
    
    /**
     * It sums two densities.
     *
     * @argument f the linear function to be added to the calling linear
     * function.
     * @return a linear function wich is the result of the sum.
     */
    
    public MixtExpDensity sumDensities(MixtExpDensity f) {
        
        MixtExpDensity g;
        int i, s;
        
        g = new MixtExpDensity();
        
        g.independent = independent + f.independent;
        
        s = factors.size();
        
        for (i=0 ; i<s ; i++)
            g.addTerm((Double) factors.elementAt(i),((ContinuousFunction) terms.elementAt(i)).duplicate());
        
        s = f.factors.size();
        
        for (i=0 ; i<s ; i++)
            g.addTerm((Double) f.factors.elementAt(i),((ContinuousFunction) f.terms.elementAt(i)).duplicate());
        
        return g;
    }
    
    
    /**
     * Obtains the factor in the n-th term.
     * the first factor is at position 0.
     *
     * @param n the index of a factor.
     * @return the factor at position <code>n</code>.
     */
    
    public double getFactor(int n) {
        
        double value;
        
        if (n > (factors.size()-1)) {
            System.out.println("Index out of range in getFactor()\n");
            System.exit(0);
        }
        
        value = ((Double)factors.elementAt(n)).doubleValue();
        
        return value;
    }
    
    
    /**
     * Obtains the exponent in the n-th term.
     * the first exponent is at position 0.
     *
     * @param n the index of the function to retrieve.
     * @return the exponent (a continuous function) at position <code>n</code>.
     */
    
    public ContinuousFunction getExponent(int n) {
        
        ContinuousFunction f;
        
        if (n > (factors.size()-1)) {
            System.out.println("Index out of range in getExponent()\n");
            System.exit(0);
        }
        
        f = (ContinuousFunction) terms.elementAt(n);
        
        return f;
    }
    
    
    /**
     * Returns the number of exponential terms
     */
    
    public int getNumberOfExp(){

        return factors.size();
        
    }
    
    
    /**
     * Gets the independent term.
     *
     * @return the independent term.
     */
    
    public double getIndependent() {
        
        return independent;
    }
    
    
    /**
     * It multiplies two densities.
     *
     * @argument f the linear function to be multiplied by the calling linear
     * function.
     * @param flag an integer indicating if the MixtExpDensities must be simplified
     * @return a linear function wich is the result of the multiplication.
     */
    
    public MixtExpDensity multiplyDensities(MixtExpDensity f, int flag) {
        
        MixtExpDensity g;
        int i, j, s1, s2;
        double x;
        
        g = new MixtExpDensity();
        
        g.independent = independent * f.independent;
        
        s1 = factors.size();
        s2 = f.factors.size();
        
        for (i=0 ; i<s2 ; i++) {
            x =  ((Double) f.factors.elementAt(i)).doubleValue();
            g.addTerm( new Double(x*independent) ,((ContinuousFunction) f.terms.elementAt(i)).duplicate());
            for (j=0 ; j<s1 ; j++) {
                g.addTerm( new Double(x*((Double) factors.elementAt(j)).doubleValue())  ,
                ((ContinuousFunction) f.terms.elementAt(i)).sumFunctions( (ContinuousFunction) terms.elementAt(j)    ));
            }
        }
        for (j=0 ; j<s1 ; j++) {
            x = ((Double) factors.elementAt(j)).doubleValue();
            g.addTerm( new Double(x*f.independent)  ,((ContinuousFunction) terms.elementAt(j)).duplicate());
        }
        if(flag == 1)
            g.simplify();
        return g;
    }
    
    
    /**
     * It multiplies a density by an integer.
     *
     * @argument i the integer to be multiplied by the density.
     * @return a density wich is the result of the multiplication.
     */
    
    public MixtExpDensity multiplyDensities(int i) {
        
        MixtExpDensity g;
        int j, s;
        
        g = new MixtExpDensity(independent * i);
        
        s = factors.size();
        
        for (j=0 ; j<s ; j++)
            g.addTerm( new Double( ( (Double) factors.elementAt(j)).doubleValue()*i) ,((ContinuousFunction) terms.elementAt(j)).duplicate());
        
        return g;
    }
    
    
    /**
     * It multiplies a density by a double.
     *
     * @argument d the double to be multiplied by the density.
     * @return a density wich is the result of the multiplication.
     */
    
    public MixtExpDensity multiplyDensities(double d) {
        
        MixtExpDensity g;
        int j, s;
        
        g = new MixtExpDensity(independent * d);
        
        s = factors.size();
        
        for (j=0 ; j<s ; j++)
            g.addTerm(new Double(((Double)factors.elementAt(j)).doubleValue()*d),
            ((ContinuousFunction)terms.elementAt(j)).duplicate());
        
        return g;
    }
    
    
    /**
     * It gets the value of a density, when a variable is assigned to a value.
     * When the density depends on more variables, all of them are assumed
     * to be equal to 0.0.
     *
     * @argument var the continuous variable for which we know the value.
     * @argument x a double, which is the value of the continuous variable.
     * @return the value of the density for <code>var=x</code>.
     */
    
    public double getValue(Continuous var, double x) {
        
        double y;
        int i, s;
        
        y = independent;
        s = factors.size();
        
        for (i=0 ; i<s ; i++)
            y += ((Double) factors.elementAt(i)).doubleValue()* Math.exp(((ContinuousFunction) terms.elementAt(i)).getValue(var,x));
        
        
        return y;
    }
    
    
    /**
     * It gets the value of a density, when a variable is assigned to a value.
     * The value is a <code>Double</code> object.
     * When the density depends on more variables, all of them are assumed
     * to be equal to 0.0.
     *
     * @see getValue(Continuous var, double x) which is the same function,
     * but the value is a double.
     * @argument var the continuous variable for which we know the value.
     * @argument x a double object, which contains the value of the
     * continuous variable.
     * @return the value of the density for <code>var=x</code>
     */
    
    public double getValue(Continuous var, Double x) {
        
        double y;
        int i, s;
        
        y = independent;
        s = factors.size();
        
        for (i=0 ; i<s ; i++)
            y += ((Double) factors.elementAt(i)).doubleValue()* Math.exp(((ContinuousFunction) terms.elementAt(i)).getValue(var,x));
        
        return y;
    }
    
    
    /**
     * It gets the value of a density for a given configuration.
     * When the density depends on other variables that those in the
     * configuration, all of them are assumed to be equal to 0.0.
     *
     * @argument c the configuration of values for the continuous variables.
     * @return the value of the density for <code>c</code>.
     */
    
    public double getValue(ContinuousConfiguration c) {
        
        double y;
        int i, s;
        
        y = independent;
        s = factors.size();
        
        for (i=0 ; i<s ; i++)
            y += ((Double) factors.elementAt(i)).doubleValue()* Math.exp(((ContinuousFunction) terms.elementAt(i)).getValue(c));
        
        return y;
    }
    
    
    /**
     * It restricts a density to the value of a continuous variable.
     *
     * @param var a continuous variable that is going to take a value.
     * @param x the value of the continuos variable.
     * @return the density obtained from the calling density after
     * making  <code>var=x</code>.
     */
    
    public MixtExpDensity restrict(Continuous var, double x) {
        
        MixtExpDensity g;
        ContinuousFunction f;
        double y, z;
        int i, s;
        
        g = new MixtExpDensity();
        g.independent = independent;
        
        s = factors.size();
        f = null;
        
        for (i=0 ; i<s ; i++) {
            f = new QuadraticFunction();
            y = ((ContinuousFunction) terms.elementAt(i)).restrict(var,x,f);
            if(((QuadraticFunction)f).isLinear())
                f=new LinearFunction((QuadraticFunction)f);
            
            if (f.isEmpty())
                g.independent += ((Double) factors.elementAt(i)).doubleValue()*Math.exp(y);
            else {
                z = ((Double) factors.elementAt(i)).doubleValue()*Math.exp(y);
                g.addTerm( new Double(z), f);
            }
        }
        
        return g;
    }
    

    /**
     * It restricts a density to the value of a continuous variable.
     *
     * @param var a continuous variable that is going to take a value.
     * @param x a <code>Double</code> object representing the  value of the
     * continuous variable.
     * @return the density obtained from the calling density after
     * making  <code>var=x</code>.
     */
    
    public MixtExpDensity restrict(Continuous var, Double x) {
        
        MixtExpDensity g;
        ContinuousFunction f;
        double y, z;
        int i, s;
        
        g = new MixtExpDensity();
        g.independent = independent;
        
        s = factors.size();
        
        for (i=0 ; i<s ; i++) {
            f = new QuadraticFunction();
            y = ((ContinuousFunction) terms.elementAt(i)).restrict(var,x,f);
            if(((QuadraticFunction)f).isLinear())
                f=new LinearFunction((QuadraticFunction)f);
            
            if (f.isEmpty())
                g.independent += ((Double) factors.elementAt(i)).doubleValue()*Math.exp(y);
            else {
                z = ((Double) factors.elementAt(i)).doubleValue()*Math.exp(y);
                g.addTerm( new Double(z), f);
            }
        }
        
        return g;
    }
    
    
    /**
     * It restricts a density to a configuration.
     *
     * @param c a continuous configuration containing values for
     * several continuous variables.
     * @return the density obtained from the calling density after each
     * variable in the configuration is set to its value.
     */
    
    public MixtExpDensity restrict(ContinuousConfiguration c) {
        
        MixtExpDensity g;
        ContinuousFunction f;
        double y, z;
        int i, s;
        
        g = new MixtExpDensity();
        g.independent = independent;
        
        s = factors.size();
        
        for (i=0 ; i<s ; i++) {
            f = new QuadraticFunction();
            y = ((ContinuousFunction) terms.elementAt(i)).restrict(c,f);
            if(((QuadraticFunction)f).isLinear())
                f=new LinearFunction((QuadraticFunction)f);
            
            if (f.isEmpty())
                g.independent += ((Double) factors.elementAt(i)).doubleValue()*Math.exp(y);
            else {
                z = ((Double) factors.elementAt(i)).doubleValue()*Math.exp(y);
                g.addTerm( new Double(z), f);
            }
        }
        
        return g;
    }
    
    
    /**
     * It calculates the result of making the integral of a continuous density
     * with respect to one of its variables between two values.
     *
     * @param var the continuous variable with respect to which we are doing
     * the integral.
     * @param x1 the lower limit of the integral.
     * @param x2 the upper limit of the integral.
     * @param flag An integer indicating if the MixtExpDensities must be simplified
     * @return the density result of the integral.
     */

    
    public MixtExpDensity integral(Continuous var, double x1, double x2, int flag) {
        
        MixtExpDensity g;
        double factorvar1, factorvar2;
        LinearFunction factor1;
        Double ObjFactorvar1;
        ContinuousFunction factor0, qf2;
        QuadraticFunction qf = new QuadraticFunction();
        Vector v;
        
        int i, s;
        double z, r;
        
        //  System.out.println("Integral Extra�a:"+var.getName()+","+x1+","+x2);
        //  System.out.println(ToString());
        
        
        
        g = new MixtExpDensity();
        
        g.independent = independent*(x2-x1);
        
        
        s = factors.size();
        
        
        for (i=0 ; i<s ; i++) {
            
            v = new Vector();
            factorvar2 = ((ContinuousFunction) terms.elementAt(i)).getFactors(var,v);
            factor0 = (ContinuousFunction)v.elementAt(0);
            factor1 = (LinearFunction)v.elementAt(1);
            ObjFactorvar1 = (Double)v.elementAt(2);
            factorvar1 = ObjFactorvar1.doubleValue();
            
            
            if ((factorvar2 !=0.0) || ! (factor1.isEmpty())) {
                if ( (x1 != var.getMin()) || (x2 != var.getMax()))
                {System.out.println("Error. A quadratic function should be integrated in the complete range of values: "+var.getName()+","+var.getMin()+","+var.getMax()+","+x1+","+x2+",");
                 System.exit(1);
                }
                if (factorvar2 >= 0)
                {System.out.println("A quadratic variable has a possitive coefficient");
                 System.exit(1);
                }
                qf = factor1.multiply(factor1);
                qf.multiply(-1.0/(4.0*factorvar2));
                qf2 = qf.sumFunctions(factor0);
                r = ((Double) factors.elementAt(i)).doubleValue()*(1.0/Math.sqrt(-factorvar2))*
                (Math.exp(-factorvar1*factorvar1/(4.0*factorvar2)))*Math.sqrt(Math.PI);
                if (!qf2.isEmpty())
                    g.addTerm(r,(ContinuousFunction) qf2);
                else
                    g.independent+=r;
                
            }
            else
            { r =((Double) factors.elementAt(i)).doubleValue();
              if( !factor0.isEmpty())
                  if (factorvar1 != 0.0) {
                      r = r/factorvar1;
                      r = r* (Math.exp(factorvar1*x2)-Math.exp(factorvar1*x1));
                      g.addTerm(r, factor0);
                  }
                  else {r =r*(x2-x1);
                  g.addTerm(r, factor0);
                  }
              else
              { if (factorvar1 !=0.0){
                    
                    r = r/factorvar1;
                    r = r* (Math.exp(factorvar1*x2)-Math.exp(factorvar1*x1));
                    g.independent = g.independent+r;
                }
                else {g.independent = g.independent + r*(x2-x1);}
              }
              
              
            }
        }
        //    System.out.println("Integral1-:");
        //    System.out.println(g.ToString());
        
        if (flag == 1)
            g.simplify();   
        return g;
    }
    
    
    /**
     * It calculates the result of making the integral of a continuous density
     * with respect to one of its variables between two values.
     *
     * @param var the continuous variable with respect to which we are doing
     * the integral.
     * @param x1 the lower limit of the integral.
     * @param x2 the upper limit of the integral.
     * @return the density result of the integral.
     */

    
    public MixtExpDensity integral(Continuous var, double x1, double x2) {
        
        MixtExpDensity g;
        double factorvar1, factorvar2;
        LinearFunction factor1;
        Double ObjFactorvar1;
        ContinuousFunction factor0, qf2;
        QuadraticFunction qf = new QuadraticFunction();
        Vector v;
        
        int i, s;
        double z, r;
        
        //  System.out.println("Integral Extra�a:"+var.getName()+","+x1+","+x2);
        //  System.out.println(ToString());
        
        
        
        g = new MixtExpDensity();
        
        g.independent = independent*(x2-x1);
        
        
        s = factors.size();
        
        
        for (i=0 ; i<s ; i++) {
            
            v = new Vector();
            factorvar2 = ((ContinuousFunction) terms.elementAt(i)).getFactors(var,v);
            factor0 = (ContinuousFunction)v.elementAt(0);
            factor1 = (LinearFunction)v.elementAt(1);
            ObjFactorvar1 = (Double)v.elementAt(2);
            factorvar1 = ObjFactorvar1.doubleValue();
            
            
            if ((factorvar2 !=0.0) || ! (factor1.isEmpty())) {
                if ( (x1 != var.getMin()) || (x2 != var.getMax()))
                {System.out.println("Error. A quadratic function should be integrated in the complete range of values: "+var.getName()+","+var.getMin()+","+var.getMax()+","+x1+","+x2+",");
                 System.exit(1);
                }
                if (factorvar2 >= 0)
                {System.out.println("A quadratic variable has a possitive coefficient");
                 System.exit(1);
                }
                qf = factor1.multiply(factor1);
                qf.multiply(-1.0/(4.0*factorvar2));
                qf2 = qf.sumFunctions(factor0);
                r = ((Double) factors.elementAt(i)).doubleValue()*(1.0/Math.sqrt(-factorvar2))*
                (Math.exp(-factorvar1*factorvar1/(4.0*factorvar2)))*Math.sqrt(Math.PI);
                if (!qf2.isEmpty())
                    g.addTerm(r,(ContinuousFunction) qf2);
                else
                    g.independent+=r;
                
            }
            else
            { r =((Double) factors.elementAt(i)).doubleValue();
              if( !factor0.isEmpty())
                  if (factorvar1 != 0.0) {
                      r = r/factorvar1;
                      r = r* (Math.exp(factorvar1*x2)-Math.exp(factorvar1*x1));
                      g.addTerm(r, factor0);
                  }
                  else {r =r*(x2-x1);
                  g.addTerm(r, factor0);
                  }
              else
              { if (factorvar1 !=0.0){
                    
                    r = r/factorvar1;
                    r = r* (Math.exp(factorvar1*x2)-Math.exp(factorvar1*x1));
                    g.independent = g.independent+r;
                }
                else {g.independent = g.independent + r*(x2-x1);}
              }
              
              
            }
        }
        //    System.out.println("Integral1-:");
        //    System.out.println(g.ToString());
        
     
       g.simplify();   
        return g;
    }
    
    /**
     * Obtains a density from this one, but
     * restricted to a given interval. The density must be
     * defined just for one variable.
     * @param v the variable for which the density is defined.
     * @param l the lower limit of the given interval.
     * @param u the upper limit of the given interval.
     * @return a density proportional to this in the interval
     * <code>(u,l)</code>.
     */
    
    public MixtExpDensity getDensityOnInterval(Continuous v, double l,
    double u) {
        
        double fac;
        MixtExpDensity g;
        
        fac = integral(v,l,u,1).getIndependent();
        
        g = multiplyDensities(1/fac);
        
        return g;
    }
    
    
    /**
     * Simulates a value of the random variable distributed according to the
     * density that receives the call. This density must be defined just for
     * one variable.
     *
     * Depending if all the coefficients are positive or not it calls the method
     * simulate or the method simulateCoefNeg.
     *
     * @param var the variable to simulate (of class Continuous).
     * @param xmin lower value of the variable to simulate.
     * @param xmax upper value of the variable to simulate.
     *
     * @return The double generated.
     */
    
    public double simulateGen(Continuous var, double xmin, double xmax){
        
        int i = 0;
        boolean positive = true;
        double value;
        
        // First we check the sign of the coefficients, in order to
        // call one method or another
        while ((i<factors.size()) && (positive == true)) {
            
            if (getFactor(i) < 0)
                positive = false;
            i++;
        }
        
        if (getIndependent() < 0)
            positive = false;
        
        // All the coefficients are positive
        if (positive == true){
            //System.out.println("Llamo al de coef positivos.");
            value = simulate(var,xmin,xmax);
            
        }//End of if
        
        //At least one coefficient negative
        else {
            //System.out.println("Llamo al de coef negativos");
            value = simulateCoefNeg(var,xmin,xmax);
            
        }//End of else
        
        return value;
        
    }// End of method
    
    
    /**
     * Simulates a value of the random variable distributed according to the
     * density that receives the call. This density must be defined just for
     * one variable.
     *
     * The value is simulated by the composition method (see Rubinstein, 1981).
     * First of all the density is decomposed as a weighted average of
     * density functions, where the sum of weights is equal to 1, and all
     * the weights are positive. If not we cannot use this method. Then one
     * of the densities is chosen at random with probability equal to its
     * weight and a value is simulated applying the inverse transform method
     * to the selected density. Two random numbers are used in this process,
     * one for selecting the density and the other one for applying the
     * inverse transform.
     *
     * The simulated value will be within a given range. This is done
     * allow simulating variables which density is specified by parts,
     * as it is is a <code>ContinuousProbabilityTree</code>.
     *
     * @param var the variable to simulate (of class Continuous).
     * @param u1 one of the random numbers (must be between 0 and 1).
     * @param u2 the other random number (must be between 0 and 1).
     * @param xmin lower value of the variable to simulate.
     * @param xmax upper value of the variable to simulate.
     * @return a value within <code>xmin</code> and <code>xmax</code>.
     */
    
    public double simulate(Continuous var, double u1, double u2,
    double xmin, double xmax) {
        
        double simulatedValue = 0.0, s, kv, cv;
        double[] weight;
        double[] c;
        double[] k;
        int i, index, numberOfTerms;
        MixtExpDensity auxDensity, result;
        LinearFunction f;
        ContinuousFunction g;
        boolean done = false;
        
        // First compute the weights.
        
        numberOfTerms = factors.size();
        
        weight = new double[numberOfTerms + 1];
        c = new double[numberOfTerms];
        k = new double[numberOfTerms];
        
        weight[0] = independent * (xmax - xmin);
        
        for (i=1 ; i<=numberOfTerms ; i++) {
            
            // Create a new auxiliar density with the i-th term,
            // in order to compute the integral of that term.
            
            g = getExponent(i-1);
            if (g.getClass().equals("QuadraticFunction"))
            {System.out.println("Error: This procedure does not allow quadratic functions");
             System.exit(1);}
            f = (LinearFunction) g;
            
            
            
            
            auxDensity = new MixtExpDensity(0);
            auxDensity.addTerm(1,f);
            
            result = auxDensity.integral(var,xmin,xmax,1);
            
            // Now, the result of the integral must be a constant, stored
            // as the independent term of the result.
            
            c[i-1] = result.getIndependent();
            
            k[i-1] = f.getCoefficient(var);
            
            weight[i] = c[i-1] * getFactor(i-1);
            
            //System.out.println("EL peso "+i+" es: "+weight[i]);
        }
        
        // Now we select the function to simulate.
        
        done = false;
        index = 0;
        s = 0.0;
        while ((!done) && (index<numberOfTerms)) {
            
            s += weight[index];
            if (u1 <= s) {
                done = true;
            }
            else {
                index++;
            }
        }
        
        //System.out.println("I have selected index : "+index);
        
        // Now we obtain a value for var by applying the
        // inverse transform to the density at position index.
        
        if (index == 0) { // Uniform distribution
            simulatedValue = xmin + (xmax - xmin) * u2;
        }
        else {
            kv = k[index-1];
            cv = c[index-1];
            simulatedValue = Math.log(kv*cv*u2 + Math.exp(kv*xmin)) / kv;
        }
        
        return simulatedValue;
    }
    
    
    /**
     * Simulates a value of the random variable distributed according to the
     * density that receives the call. This density must be defined just for
     * one variable.
     *
     * The value is simulated by the composition method (see Rubinstein, 1981).
     * First of all the density is decomposed as a weighted average of
     * density functions, where the sum of weights is equal to 1, and all
     * the weights are positive. If not we cannot use this method. Then one
     * of the densities is chosen at random with probability equal to its
     * weight and a value is simulated applying the inverse transform method
     * to the selected density. Two random numbers are used in this process,
     * one for selecting the density and the other one for applying the
     * inverse transform.
     *
     * The simulated value will be within a given range. This is done
     * allow simulating variables which density is specified by parts,
     * as it is is a <code>ContinuousProbabilityTree</code>.
     *
     * The difference with respect to the method above is that in this case,
     * the random numbers are generated using Math.random() instead of
     * being supplied as arguments.
     *
     * @param var the variable to simulate (of class Continuous).
     * @return a value within the range of values of the random variable.
     */
    
    public double simulate(Continuous var, double xmin, double xmax) {
        
        double u1, u2;
        
        u1 = Math.random();
        u2 = Math.random();
        
        //System.out.println("u1 es: "+u1);
        //System.out.println("u2 es: "+u2);
        
        return simulate(var,u1,u2,xmin,xmax);
    }
    
    
    /**
     * Simulates a value of the random variable distributed according to the
     * density that receives the call. This density must be defined just for
     * one variable.
     *
     * The value is simulated by a rejection technique for sampling from a
     * combination of distributions with negative weigths for some of
     * the components. (see Bignami, 1970).
     * First of all the density is decomposed as a weighted average of
     * density functions, where the sum of weights is equal to 1. Then one
     * of the densities with positive weight is chosen at random with
     * probability equal to its weight divided by the sum of all positive
     * weigths and a value is simulated applying the inverse transform method
     * to the selected density. Then this value simulated will be accepted or
     * not depeneding on the value of the densitiy on that value.
     * Three random numbers are used in this process,one for selecting the
     * positive density, other one for applying the inverse transform and a
     * last one to decide if we accept or not that value. If it is not accepted we
     * repeat the whole process (with new random numbers) until a number is accepted.
     *
     * The simulated value will be within a given range. This is done
     * allow simulating variables which density is specified by parts,
     * as it is is a <code>ContinuousProbabilityTree</code>.
     *
     * @param var the variable to simulate (of class Continuous).
     * @param u1 one of the random numbers (must be between 0 and 1).
     * @param u2 other random number (must be between 0 and 1).
     * @param u3 the last random number (must be between 0 and 1).
     * @param xmin lower value of the variable to simulate.
     * @param xmax upper value of the variable to simulate.
     *
     * @return a value within the range of values of the random variable.
     */
    
    public double simulateCoefNeg(Continuous var, double u1, double u2, double u3,
    double xmin, double xmax) {
        
        double simulatedValue = 0.0, s, kv, cv, sumPosWeight = 0.0, sumDen = 0.0, integr = 0.0;
        double[] weight;
        double[] c;
        double[] k;
        int i, index, numberOfTerms;
        MixtExpDensity auxDensity, result;
        LinearFunction f;
        ContinuousFunction g;
        boolean done = false, finish;
        
        // First compute the weights.
        
        numberOfTerms = factors.size();
        
        weight = new double[numberOfTerms + 1];
        c = new double[numberOfTerms];
        k = new double[numberOfTerms];
        
        weight[0] = independent * (xmax - xmin);
        
        
        for (i=1 ; i<=numberOfTerms ; i++) {
            
            // Create a new auxiliar density with the i-th term,
            // in order to compute the integral of that term.
            
            g = getExponent(i-1);
            if (g.getClass().equals("QuadraticFunction"))
            {System.out.println("Error: This procedure does not allow quadratic functions");
             System.exit(1);}
            f = (LinearFunction) g;
            
            
            auxDensity = new MixtExpDensity(0);
            auxDensity.addTerm(1,f);
            
            
            result = auxDensity.integral(var,xmin,xmax);
            
            
            // Now, the result of the integral must be a constant, stored
            // as the independent term of the result.
            
            c[i-1] = result.getIndependent();
            
            k[i-1] = f.getCoefficient(var);

            weight[i] = c[i-1] * getFactor(i-1);
            
        }
        
        // In order to simulate we need the sum of all the positive
        // weights
        
        for (i=0 ; i<=numberOfTerms ; i++){
            
            if (weight[i] > 0){
                
                sumPosWeight += weight[i];
                
                
            }// End of if
            
        }//End of for
        
        
        //It is a cyclic structure becouse not every generated number
        // may be accepeted.
        finish = false;
        do {
            
            // Now we select the function to simulate. It will be
            // one with positive weight and with probability
            // weight[i]/sumPosWeight
            
            done = false;
            index = 0;
            s = 0.0;
            
            while ((!done) && (index<numberOfTerms)) {
                
                
                if (weight[index] > 0){
                    
                    s += (weight[index]/sumPosWeight);
                    
                    if (u1 <= s) {
                        done = true;
                        
                    }
                    else {
                        index++;
                    }//End of else
                    
                }// End of if
                
                // If weight[index] <=0
                else { index++ ;}
                
            }// End of while
            
            
            // Now we obtain a value for var by applying the
            // inverse transform to the density at position index.
            
            if (index == 0) { // Uniform distribution
                simulatedValue = xmin + (xmax - xmin) * u2;
            }
            else {
                kv = k[index-1];
                cv = c[index-1];
                simulatedValue = Math.log(kv*cv*u2 + Math.exp(kv*xmin)) / kv;
            }
            
            // Now we have to check if this value will be accepted.
            // We will accept it if it happens that
            // u3 <= f(value)/(weight[i]fi(value)) if weight[i]>0
            
            //First we will compute the sum of fi(value) for all densities
            // with positive weight
            
            //Independent term
            if (weight[0] > 0){sumDen += (1/(xmax-xmin));}
            
            for (i=1 ; i<=numberOfTerms ; i++){
                
                if (weight[i] > 0){
                    // Create a new auxiliar density with the i-th term,
                    // in order to compute the integral of that term.
                    
                    g = getExponent(i-1);
                    
                    f = (LinearFunction) g;
                    
                    
                    auxDensity = new MixtExpDensity(0);
                    auxDensity.addTerm(1,f);
                    
                    result = auxDensity.integral(var,xmin,xmax);
                    
                    // Now, the result of the integral must be a constant, stored
                    // as the independent term of the result.
                    
                    integr = result.getIndependent();
                    
                    // This is the density fi
                    auxDensity = auxDensity.multiplyDensities((1/integr));
                    
                    //So we get fi(value)
                    
                    sumDen += auxDensity.getValue(var,simulatedValue);
                    
                }//End of if
                
            }//End of for
            //System.out.println("Aceptaremos el valor generado si "+u3+" <= "+ (getValue(var,simulatedValue)/sumDen));
            if (u3 <= (getValue(var,simulatedValue)/sumDen)){
                
                //We accept the simulatedValue
                //System.out.println("aceptamos el valor generado.");
                finish = true;
                
            }//End of if
            else {//If it has not been accepted we need to repeat the whole process, so we need three
                // new random numbers.
                
                u1 = Math.random();
                u2 = Math.random();
                u3 = Math.random();
                
            }//End of else
            sumDen=0;
        }while((!finish));
        return simulatedValue;
        
    }
    
    
    /**
     * Simulates a value of the random variable distributed according to the
     * density that receives the call. This density must be defined just for
     * one variable.
     *
     * The value is simulated by a rejection technique for sampling from a
     * combination of distributions with negative weigths for some of
     * the components. (see Bignami, 1970).
     * First of all the density is decomposed as a weighted average of
     * density functions, where the sum of weights is equal to 1. Then one
     * of the densities with positive weight is chosen at random with
     * probability equal to its weight divided by the sum of all positive
     * weigths and a value is simulated applying the inverse transform method
     * to the selected density. Then this value simulated will be accepted or
     * not depeneding on the value of the densitiy on that value.
     * Three random numbers are used in this process,one for selecting the
     * positive density, other one for applying the inverse transform and a
     * last one to decide if we accept or not that value.
     *
     * The simulated value will be within a given range. This is done
     * allow simulating variables which density is specified by parts,
     * as it is is a <code>ContinuousProbabilityTree</code>.
     *
     * The difference with respect to the method above is that in this case,
     * the random numbers are generated using Math.random() instead of
     * being supplied as arguments.
     *
     * @param var the variable to simulate (of class Continuous).
     * @param xmin lower value of the variable to simulate.
     * @param xmax upper value of the variable to simulate.
     *
     * @return a value within the range of values of the random variable.
     */
    
    public double simulateCoefNeg(Continuous var, double xmin, double xmax) {
        
        double u1, u2, u3;
        
        u1 = Math.random();
        u2 = Math.random();
        u3 = Math.random();
        //System.out.println("Llamo al simulate coef neg con los valores:"+u1+" ; "+u2+" ; "+u3);
        return simulateCoefNeg(var,u1,u2,u3, xmin,xmax);
    }
    
    
    /**
     * Prints the density to the standard output.
     */
    
    
    
    public void print() {
        
        int i;
        ContinuousFunction f;
        
        System.out.print(getIndependent());
        
        for (i=0 ; i<factors.size() ; i++) {
            System.out.print(" + ("+getFactor(i)+")* exp( ");
            f = getExponent(i);
            f.print();
            System.out.print(" x)");
        }
        System.out.println(" ");
    }
    
    /**
     * Prints the density to the standard output.
     */
    
    
    
    public void print(int n) {
        
        int i;
        ContinuousFunction f;
        
        for (i=0; i<n; i++)
            System.out.print("\t");
	System.out.print(Fmath.truncate(getIndependent(),this.printPrecision));

        for (i=0 ; i<factors.size() ; i++) {
	    System.out.print(" + ("+Fmath.truncate(getFactor(i),this.printPrecision)+")* exp( ");
            f = getExponent(i);
            f.print();
            System.out.print(" x)");
        }
        System.out.println(" ");
    }
    
    /**
     * Convert to String a MixtExpFunction
     */
    public String ToString() {
        
        int i;
        ContinuousFunction f;
        String s;
        
        
        simplify();
        //System.out.print(getIndependent());
  /*if (getIndependent()==0.0)
      s=new String();
  else*/
        s=new String(String.valueOf(Fmath.truncate(getIndependent(),this.printPrecision)));


        for (i=0 ; i<factors.size() ; i++) {
            if (getFactor(i)!=0){
                if (getFactor(i)>0){
		    s=s+"+"+String.valueOf(Fmath.truncate(getFactor(i),this.printPrecision))+"* exp( ";
		}else if (getFactor(i)<0){
		    s=s+String.valueOf(Fmath.truncate(getFactor(i),this.printPrecision))+"* exp( ";
		}
                f = getExponent(i);
                s=s+f.ToString();
                s=s+")";
            }
        }
        
        return s;
    }

/*
 * Return a String with the Continuous function. But each term of the
 * function is intro a p�renthesis.
 */
    
    public String ToStringWithParentesis() {
        
        int i;
        ContinuousFunction f;
        String s;
        
        
        simplify();
        //System.out.print(getIndependent());
  /*if (getIndependent()==0.0)
      s=new String();
  else*/
        s=new String(String.valueOf(getIndependent()));
        
        
        for (i=0 ; i<factors.size() ; i++) {
            if (getFactor(i)!=0){
                if (getFactor(i)>0){
                    s=s+"+("+String.valueOf(getFactor(i))+"* exp( ";
                }else if (getFactor(i)<0){
                    s=s+"+("+String.valueOf(getFactor(i))+"* exp( ";
                }
                f = getExponent(i);
                s=s+f.ToStringWithParentesis();
                s=s+"))";
            }
        }
        
        return s;
    }
    
    /**
     * Saves the density to a file.
     *
     * @param p the <code>PrintWriter</code> where the density will be written.
     */
    
    
    
    public void save(PrintWriter p) {
        
        int i;
        ContinuousFunction f;
        
        p.print(getIndependent());
        
        for (i=0 ; i<factors.size() ; i++) {
            p.print(" + "+getFactor(i)+" exp{ ");
            f = getExponent(i);
            f.save(p);
            p.print(" }");
        }
    }

    /**
     * Saves the density to a file in R format.
     *
     * @param p the <code>PrintWriter</code> where the density will be written.
     */



    public void saveR(PrintWriter p, String condition) {

        int i;
        LinearFunction f;


        p.print(getIndependent());

        for (i=0 ; i<factors.size() ; i++) {
            p.print(" + "+getFactor(i)+" * exp( ");
            f = (LinearFunction)getExponent(i);
            f.saveR(p,condition);
            p.print(" )");
        }
    }
    
    
    
    /**
     * Computes the linear regression of two variables
     * given by two samples, X and Y. It will return a vector
     * with two elements, the first will be the independent term
     * and the second the coefficient that multiplies the X
     *
     * @param X The independent variable
     * @param Y The dependent variable
     *
     * @return A vector with the coefficients.
     */
    
    public Vector linearRegression(Vector X , Vector Y){
        
        int i;
        double a = 0,b = 0,averageX = 0, averageY = 0,covariance = 0,varianceX = 0;
        Vector solution = new Vector();
        
        // Average of X
        for (i=0 ; i<X.size() ; i++){
            
            averageX += ((Double)X.elementAt(i)).doubleValue();
            
        }//End of for
        
        averageX = (averageX/X.size());
        
        // Average of Y
        for (i=0 ; i<Y.size() ; i++){
            
            averageY += ((Double)Y.elementAt(i)).doubleValue();
            
        }//End of for
        
        averageY = (averageY/Y.size());
        
        
        // Covariance
        
        for (i=0 ; i<X.size() ; i++){
            
            covariance += ((((Double)X.elementAt(i)).doubleValue()-averageX)*(((Double)Y.elementAt(i)).doubleValue()-averageY));
            
        }//End of for
        
        covariance = (covariance/X.size());
        
        // Variance
        
        for (i=0 ; i<X.size() ; i++){
            
            varianceX += (((Double)X.elementAt(i)).doubleValue()*((Double)X.elementAt(i)).doubleValue());
            
        }// End of for
        
        varianceX = varianceX/X.size();
        varianceX = varianceX-(averageX*averageX);
        
        // Now we compute the coefficients
        
        a = (averageY - (covariance/varianceX)*averageX);
        
        b = (covariance/varianceX);
        
        //System.out.println("Valores de la regresion lineal: a: " +a+" b: "+b);
        
        solution.addElement(new Double(a));
        solution.addElement(new Double(b));
        
        return solution;
        
    }// End of method linearRegression
    
    
    /**
     * Computes an exponential regression y=a*exp(bx)
     * of two variables
     * given by two samples, X and Y. It will return a vector
     * with two elements, the first will be the coefficient of the
     * exponential and the second the exponent
     *
     * @param X The independent variable
     * @param Y The dependent variable
     *
     * @return A vector with the coefficients
     */
    
    public Vector exponentialRegression(Vector X, Vector Y){
        
        Vector intermediateVector = new Vector();
        int i;
        double intermedReal,loga;
        Vector solution = new Vector();
        
        // we have to transform y=a*exp(bx) to
        // log(y)=log(a) + bx
        // so we transform Y to log(y)
        
        for (i=0 ; i<Y.size() ; i++){
            
            //    System.out.println("el y en la regresion: "+((Double)Y.elementAt(i)).doubleValue());
            //     if (((Double)Y.elementAt(i)).doubleValue()<=0) System.out.println("Failure when computing the exponential regression.");
            intermedReal = Math.log(((Double)Y.elementAt(i)).doubleValue());
            intermediateVector.addElement(new Double(intermedReal));
        }//End of for
        
        solution = linearRegression(X,intermediateVector);
        loga = ((Double)solution.elementAt(0)).doubleValue();
        solution.setElementAt(new Double(Math.exp(loga)), 0);
        return solution;
        
    }// End of method
    
    
    /**
     * Computes an exponential regression of two variables y = a*exp(b*x)+c
     * where c will be the lowest value of y, so that we can apply a
     * standard exponential regresion to the vectors y-c , x. It will return a
     * vector with three elements, the first will be a, the second b and the third
     * will be c.
     *
     * @param X The independent variable
     * @param Y The dependent variable
     *
     * @return A vector with the coefficients
     */
    
    public Vector exponentialRegressionWithIndependent(Vector X, Vector Y){
        
        Vector result = new Vector();
        Vector aux = new Vector();
        double c, yValue,min;
        int i;
        
        // We look for the minimun of y
        
        min = ((Double)Y.elementAt(0)).doubleValue();
        for (i=1 ; i<Y.size() ; i++){
            if (((Double)Y.elementAt(i)).doubleValue() < min){
                
                min = ((Double)Y.elementAt(i)).doubleValue();
                
            }// End of if
            
        }//End of for
        
        c = (min-1);
        
        
        //We construct now our new vector y-c
        for (i=0 ; i<Y.size() ; i++){
            
            yValue = ((Double)Y.elementAt(i)).doubleValue();
            aux.addElement(new Double((yValue-c)));
            
        }//End of for
        
        result = exponentialRegression(X,aux);
        
        result.addElement(new Double(c));
        
        return result;
        
    }//End of method
    
    
    /**
     * We estimate the parameters a,b and K of y=a*exp(bx)+K taking into account
     * the derivatives. y'=b*a*exp(bx). We estimate this a and b with the
     * envelope of every 20 values, and after that, the K by mean square
     * error
     * We suppouse that both vectors are sorted.
     *
     * @param X The independent variable
     * @param Y The dependent variable
     * @param tam The number of values we will consider for each line
     *
     * @return A vector with three values, a in the first position
     *         b in the second and K in the third
     */
    
    public Vector estimateExponentialWithIndependentByDerivative(Vector X, Vector Y,int tam){
        
        Vector result = new Vector();
        Vector envelopes = new Vector();
        Vector Xenvelopes = new Vector();
        int i, numberOfEnvelopes = 0;
        
        double envelope,y1,y2,x1,x2,Xmid,a,b,k = 0,xValue,yValue;
        
        //;
        
        boolean negative = false;
        boolean zero = false;
        
        // We take every 20 values, so we calculate X.size()/20 envelopes.
        
        numberOfEnvelopes = (int)(X.size()/tam);
        //System.out.println("Numero de particiones : "+numberOfEnvelopes);
        
        for (i=0 ; i<numberOfEnvelopes ; i++){
            
            y1 = ((Double)Y.elementAt(i*tam)).doubleValue();
            y2 = ((Double)Y.elementAt((i*tam)+(tam-1))).doubleValue();
            x1 = ((Double)X.elementAt(i*tam)).doubleValue();
            x2 = ((Double)X.elementAt((i*tam)+(tam-1))).doubleValue();
            envelope = (y1-y2)/(x1-x2);
            if (envelope<0) {
                negative = true;
                //      System.out.println("Envelope negativa");
                //    System.out.println("y1 es: "+y1+" e y2 es "+y2);
            }else{
                
                //  System.out.println("Envelope positiva");
                // System.out.println("y1 es: "+y1+" e y2 es "+y2);
            }
            
            //Si una pndiente es cero, lo son todas, y entonces seria una recta.
            if (envelope==0) zero = true;
            
            //    System.out.println("Envelope : "+envelope);
            envelopes.addElement(new Double(envelope));
            Xmid = ((Double)X.elementAt((i*tam)+((tam/2)-1))).doubleValue();
            Xenvelopes.addElement(new Double(Xmid));
            
        }//End of for
        
        //If the envelopes are negative, I turn them positive and then apply the method
        
        if (negative){
            for (i=0 ; i<envelopes.size(); i++){
                
                envelope = ((Double)envelopes.elementAt(i)).doubleValue();
                envelopes.setElementAt(new Double(-envelope),i);
                
            }//End of for
        }//End of if
        
        // Now I apply an exponential regression without independent
        // term to this values.
        
        if (!zero){
            
            //      System.out.println("este es el y de la regresion justo al hacer la regresion: "+envelopes);
            result = exponentialRegression(Xenvelopes, envelopes);
            b = ((Double)result.elementAt(1)).doubleValue();
            
            //This means it is a line what we are approximating to, since all the envelopes are the same
            if (b==0){
                a = 0;
                b = 0;
            }else{
                a = ((Double)result.elementAt(0)).doubleValue()/b;
            }
            // If envelopes were negative, we undo the change.
            if (negative) {a = -a;}
            
        }else{//del zero
            
            a = 0;
            b= 0;
        }
        
        // And K will be estimated by mean square error
        // k = sum(y-a*exp(bx)/n)
        
        for (i=0 ; i <X.size() ; i++){
            
            //System.out.println("i vale : "+i);
            xValue = ((Double)X.elementAt(i)).doubleValue();
            yValue = ((Double)Y.elementAt(i)).doubleValue();
            k += (yValue-(a*Math.exp(b*xValue)));
            
        }//End of for
        
        k = k/X.size();
        
        result = new Vector();
        
        //Now we insert the values  in the otuput
        result.addElement(new Double(a));
        result.addElement(new Double(b));
        result.addElement(new Double(k));
        
        return result;
        
    }//End of method
    
    
    
    /**
     * Transforms the data (X,Y) so that we can apply exponential regression to it.
     * The problem is that Y cannot be negative.
     *
     * @param X The X values.
     * @param Y The Y (density) values
     * @return A boolean indicating what we have done.
     */
    
    
    public boolean checkData(Vector X, Vector Y){
        
        double xValue, yValue, min, K1, a, b, x1, x2, y1, y2;
        int negative = 0, positive = 0, midpoint =0, i;
        boolean change = false;
        
        //First we check if the values are negative or positive.
        for (i=0 ; i<Y.size() ; i++){
            if (((Double)Y.elementAt(i)).doubleValue() < 0 ) negative++;
            else {    //Else 1
                if (((Double)Y.elementAt(i)).doubleValue() > 0 ) positive++;
                else { //Else 2
                    positive++;
                    negative++;
                } //End of else 2
            }//End of else 1
        }//End of for
        
        if (negative == 0) // All are positive
            change = false;
        
        if (positive == 0){ // All are negative
            for (i=0 ; i<X.size() ; i++){
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                Y.setElementAt(new Double(-yValue),i);
            }// End of for
            change = true;
        }// End of if
        
        if ((positive > 0) && (negative > 0)){
            
            min = 0;
            
            for (i=0 ; i<Y.size() ; i++){
                
                //Now I will calculate the K I have to sum to yvalues
                // to be able to do the esponential regression
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                if (yValue < min ) min = yValue;
                
            }//End of for
            
            K1 = -min +1;
            
            for (i=0 ; i<Y.size() ; i++){
                
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                Y.setElementAt(new Double(yValue+K1),i);
                
            }//End of for
            
            change = false;
            
        }// End of if
        
        // Now I have to check whether data comes from a
        // convex function or not. If it does, I will have
        // to transform the data since with the exponential
        // regression the coefficient is always positive.
        
        // TO do so I will bild up the line that goes through the
        // first and the last point and see wether it passes up or down
        // the points (just one of them). If it goes down, it is convex.
        
        // The first point
        //System.out.println("Estoy en CheckData, line 1570. EL tama�o de X es: "+X.size());
        x1 = ((Double)X.elementAt(0)).doubleValue();
        y1 = ((Double)Y.elementAt(0)).doubleValue();
        
        // The last point
        x2 = ((Double)X.elementAt(X.size()-1)).doubleValue();
        y2 = ((Double)Y.elementAt(Y.size()-1)).doubleValue();
        
        // The line will be y = ax + b
        a = (y2-y1)/(x2-x1);
        b = y1 - a*x1;
        
        //Now Iwill check if it passes up or down
        if (X.size()%2 == 1) midpoint = (X.size()+1)/2;
        else midpoint = X.size()/2;
        
        yValue = ((Double)Y.elementAt(midpoint)).doubleValue();
        xValue = ((Double)X.elementAt(midpoint)).doubleValue();
        
        
        if (yValue > (a*xValue+b)){ // It is convex
            
            for (i=0 ; i<Y.size() ; i++){
                
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                Y.setElementAt(new Double(-yValue),i);
                
            }//End of for
            
            min = 0;
            
            for (i=0 ; i<Y.size() ; i++){
                
                //Now I will calculate the K I have to sum to yvalues
                // to be able to do the exponential regression
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                if (yValue < min ) min = yValue;
                
            }//End of for
            
            K1 = -min +1;
            
            for (i=0 ; i<Y.size() ; i++){
                
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                Y.setElementAt(new Double(yValue+K1),i);
                
            }//End of for
            
            change = !change;
            
        }//End of if
        
        return(change);
        
    }//End of method
    
    
    /**
     * Estimates a mixture of two exponentials for one variable. Our mixture will
     * be of the type a*exp(bx) + d*exp(fx)+constante. It si very similar to estimate1
     * but we only use constante  if it is needed. We use the simple regression method.
     *
     * @param var The variable we are trying to modelize
     * @param sample The sample we have of the variable.
     * @param values The density of the sample.
     * @param der Used for Derivative method.
     *
     * @return the MixtExpDensity which best aproximates the sample.
     */
    
    // public MixtExpDensity estimate6(Continuous var, Vector sample, Vector values, int der){
    
    //   MixtExpDensity result,exp1,exp2;
    
    //   double a = 0, newA=0;
    //   double b = 0, newB=0;
    
    //   double d = 0, newD=0;
    //   double f = 0, newF=0;
    
    //   // We will use this two variables to measure the error.
    //   double newerror=2, intermederror, lasterror=2;
    
    //   double K1 = 0;
    //   double K = 0, constant =0, newConstant;
    //   boolean change, decrease;
    //   double H = 0, H1 = 0, H2 = 0;
    
    //   //sign = 0, all positive, =1 all negative, =2 some positive and some negative
    //   int i, iteration = 1;
    //   double intermedReal;
    //   double yValue, xValue;
    //   Vector intermediateVector;
    //   Vector coef = new Vector();
    //   Vector others = new Vector();
    //   result = new MixtExpDensity(0);
    
    //   System.out.println("************************* BEGINNING TO ESTIMATE THE EXPONENTIAL *******************************");
    
    //   System.out.println("Estoy llamando a estimate6 con Sample size: "+sample.size());
    
    //   //Now we make a first estimation through the derivati to begin with
    
    //   coef=result.estimateExponentialWithIndependentByDerivative(sample,values,der);
    //   a = ((Double)coef.elementAt(0)).doubleValue();
    //   b = ((Double)coef.elementAt(1)).doubleValue();
    //   constant = ((Double)coef.elementAt(2)).doubleValue();
    //   System.out.println("Coeffcients we have with the derivative method:");
    //   System.out.println("a:"+a);
    //   System.out.println("b:"+b);
    //   System.out.println("constant:"+constant);
    
    
    //   result = new MixtExpDensity(a,b,d,f,constant,var);
    
    //   lasterror=0;
    //   for(i =0 ; i<sample.size() ; i++){
    
    //     yValue = ((Double)values.elementAt(i)).doubleValue();
    //     xValue = ((Double)sample.elementAt(i)).doubleValue();
    //     lasterror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
    
    //   }
    //   lasterror = Math.sqrt(lasterror/sample.size());
    
    //   //System.out.println("Error with derivative:"+lasterror);
    
    
    //   //Now we calculate the first exponential and work with the best one
    //   result = new MixtExpDensity(0);
    //   coef = new Vector();
    //   change = false;
    
    //   //System.out.println("Cambiamos los valores"); Lo hacemos para que el checkdata no me corrompa los datos
    //   for(i =0 ; i<values.size() ; i++){
    
    //     yValue = ((Double)values.elementAt(i)).doubleValue();
    //     others.addElement(new Double(yValue));
    
    //   }
    
    //   //System.out.println("Usamos el checkData con un vector de "+sample.size()+" elementos.");
    //   change = checkData(sample,others);
    //   //System.out.println("ya hemos Usado el checkData");
    
    //   // Now we have to calculate a regression. No  z will be
    //   // negative now
    
    //   coef = exponentialRegression(sample ,others);
    
    //   newA = ((Double)coef.elementAt(0)).doubleValue();
    //   newB = ((Double)coef.elementAt(1)).doubleValue();
    //   newConstant=0;
    //   if (change){newA = -newA;}
    
    //   //constant calculation
    //   result = new MixtExpDensity(newA,newB,d,f,newConstant,var);
    
    //   K = 0;
    //   for(i =0 ; i<sample.size() ; i++){
    
    //     yValue = ((Double)values.elementAt(i)).doubleValue();
    //     xValue = ((Double)sample.elementAt(i)).doubleValue();
    //     K = K+ (yValue-result.getValue(var,xValue));
    
    //   }//End of for
    
    //   K = K/sample.size();
    
    //   newConstant = K;
    
    //   //System.out.println("Coefficients with simple regression");
    //   //System.out.println("a:"+newA);
    //   //System.out.println("b:"+newB);
    //   //System.out.println("constant:"+newConstant);
    
    //   //We take a look at the error to compare it with the derivative one
    //   result = new MixtExpDensity(newA,newB,d,f,newConstant,var);
    
    //   newerror =0;
    //   for(i =0 ; i<sample.size() ; i++){
    
    //     yValue = ((Double)values.elementAt(i)).doubleValue();
    //     xValue = ((Double)sample.elementAt(i)).doubleValue();
    
    //     newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
    
    //   }
    //   newerror = Math.sqrt(newerror/sample.size());
    
    //   //System.out.println("Error with simple regression:"+newerror);
    
    //   if (newerror >= (lasterror) ) {
    
    //   //  System.out.println("The derivative is the best");
    //   }else {
    
    //       //System.out.println("The simple regression is the best");
    //     lasterror = newerror;
    //     a=newA;
    //     b=newB;
    //     constant = newConstant;
    //   }
    //   newA=0;
    //   newB=0;
    
    //   do {
    
    //     decrease = false;
    //     result = new MixtExpDensity(0);
    
    //     // Second Exponential
    
    //     change = false;
    //     intermediateVector = new Vector();
    //     coef = new Vector();
    
    //     // This vector will be 'z=(values-a*exp(b*sample)+K1'
    //     for (i=0 ; i<sample.size() ; i++){
    
    //       yValue = ((Double)values.elementAt(i)).doubleValue();
    //       xValue = ((Double)sample.elementAt(i)).doubleValue();
    //       intermedReal = (yValue-(a*Math.exp(b*xValue)+constant));
    //       intermediateVector.addElement(new Double(intermedReal));
    
    //     }//End of for
    
    //     change = checkData(sample,intermediateVector);
    
    //     coef = exponentialRegression(sample , intermediateVector);
    
    //     newD = ((Double)coef.elementAt(0)).doubleValue();
    //     newF = ((Double)coef.elementAt(1)).doubleValue();
    
    //     if (change){
    //       newD = -newD;
    
    //     }
    
    //     // I calculate a constant H that multplies D it. It will be calculated mean square error
    
    //     H1 = 0;
    //     H2 = 0;
    //     H = 0;
    
    //     for(i =0 ; i<sample.size() ; i++){
    
    //       yValue = ((Double)values.elementAt(i)).doubleValue();
    //       xValue = ((Double)sample.elementAt(i)).doubleValue();
    
    //       H1 += (yValue-(a*Math.exp(b*xValue))-constant)*(newD*Math.exp(newF*xValue));
    //       H2 += (newD*Math.exp(newF*xValue))*(newD*Math.exp(newF*xValue));
    
    //     }//End of for
    
    //     H = H1/H2;
    //     newD *= H;
    
    
    //     //Now I check the error
    //     result = new MixtExpDensity(a,b,newD,newF,constant,var);
    
    //     newerror=0;
    //     for(i =0 ; i<sample.size() ; i++){
    
    //       yValue = ((Double)values.elementAt(i)).doubleValue();
    //       xValue = ((Double)sample.elementAt(i)).doubleValue();
    
    //       newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
    
    //     }
    
    //     newerror = Math.sqrt(newerror/sample.size());
    
    //     if (newerror >= (1*lasterror) ) {
    
    // 	//System.out.println("No metemos una segunda exponencial, ya que el error que se produce "+newerror+" es peor que el que hay "+lasterror);
    
    //     }else {
    
    //       decrease = true;
    //       lasterror = newerror;
    //       d=newD;
    //       f=newF;
    
    //     }
    //     newD=0;
    //     newF=0;
    
    
    //     //Now we work over the first exponential
    //     result = new MixtExpDensity(0);
    //     intermediateVector = new Vector();
    //     coef = new Vector();
    //     change = false;
    
    //     // This vector will be 'z=(values-(d*exp(f*sample)+K1))'
    //     for (i=0 ; i<sample.size() ; i++){
    
    //       yValue = ((Double)values.elementAt(i)).doubleValue();
    //       xValue = ((Double)sample.elementAt(i)).doubleValue();
    //       intermedReal = (yValue -(d*Math.exp(f*xValue)+constant));
    //       intermediateVector.addElement(new Double(intermedReal));
    
    //     }//End of for
    
    //     change = checkData(sample,intermediateVector);
    
    //     // Now we have to calculate a regression. No  z will be
    //     // negative now
    
    //     coef = exponentialRegression(sample , intermediateVector);
    
    //     newA = ((Double)coef.elementAt(0)).doubleValue();
    //     newB = ((Double)coef.elementAt(1)).doubleValue();
    
    //     if (change) newA = -newA;
    
    //     //Now I calculate a constant that multpilies H. It will be calculated by mean square error
    
    //     H1 = 0;
    //     H2 = 0;
    //     H = 0;
    
    //     for(i =0 ; i<sample.size() ; i++){
    
    //       yValue = ((Double)values.elementAt(i)).doubleValue();
    //       xValue = ((Double)sample.elementAt(i)).doubleValue();
    
    //       H1 += (yValue-(d*Math.exp(f*xValue))-constant)*(newA*Math.exp(newB*xValue));
    //       H2 += (newA*Math.exp(newB*xValue))*(newA*Math.exp(newB*xValue));
    
    //     }//End of for
    
    //     H = H1/H2;
    //     newA *= H;
    
    //     //Now I check the error to see if it is worthy to include this exponential
    //     result = new MixtExpDensity(newA,newB,d,f,constant,var);
    
    
    //     newerror=0;
    //     for(i =0 ; i<sample.size() ; i++){
    
    //       yValue = ((Double)values.elementAt(i)).doubleValue();
    //       xValue = ((Double)sample.elementAt(i)).doubleValue();
    //       newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
    
    //     }
    //     newerror = Math.sqrt(newerror/sample.size());
    
    //     if (newerror >= lasterror) {
    
    // 	//System.out.println("No cambiamos la primera exponencial, ya que el error que se produce "+newerror+" es peor que el que hay "+lasterror);
    
    //     }else {
    
    //       decrease = true;
    //       lasterror = newerror;
    //       a=newA;
    //       b=newB;
    //     }
    
    //     newA=0;
    //     newB=0;
    
    //     //Now we reconstantruct the Mixture to calculate again the constant
    //     result = new MixtExpDensity(a,b,d,f,0,var);
    //     //result = ConstructMixt(1,b,1,a,d,f,0,0,var);
    
    //     K = 0;
    
    //     for(i =0 ; i<sample.size() ; i++){
    
    //       yValue = ((Double)values.elementAt(i)).doubleValue();
    //       xValue = ((Double)sample.elementAt(i)).doubleValue();
    //       K = K+ (yValue-result.getValue(var,xValue));
    
    //     }//End of for
    
    //     K = K/sample.size();
    //     newConstant = K;
    
    //     //Now I check the error, to see if it is worthy to include this exponential
    //     result = new MixtExpDensity(a,b,d,f,newConstant,var);
    
    
    //     newerror=0;
    //     for(i =0 ; i<sample.size() ; i++){
    
    //       yValue = ((Double)values.elementAt(i)).doubleValue();
    //       xValue = ((Double)sample.elementAt(i)).doubleValue();
    
    //       newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
    
    //     }
    //     newerror = Math.sqrt(newerror/sample.size());
    
    //     if (newerror >= lasterror ) {
    
    // 	//System.out.println("No cambiamos la constant ya que el error que se produce "+newerror+" es peor que el que hay "+lasterror);
    
    //     }else {
    
    //       decrease = true;
    //       lasterror = newerror;
    //       constant=newConstant;
    
    //     }
    //     newConstant=0;
    
    
    //     //Now we reconstantruct the Mixture
    //     result = new MixtExpDensity(a,b,d,f,constant,var);
    
    
    //     //System.out.println("Al final de la iteracion "+iteration);
    //     //System.out.println("a:"+a+"   b:"+b+"   d:"+d+"   f:"+f+"   constant:"+constant);
    
    //     //System.out.println("El error en la iteracion "+iteration+" es "+lasterror);
    
    //     iteration++;
    
    //     }while ((decrease) && (iteration < 101)); //End of do-while
    
    //   System.out.println("The final error is "+lasterror);
    //   System.out.println("---------------- TERMINAMOS DE ESTIMAR LA EXPONENCIAL -----------------");
    //   return result;
    
    // }//End of method
    
/*
 * Simplify a Mixture Exponential Density. For expample:
 *  0.4*exp(x2) + 0.5*exp(x2) ---> 0.9*exp(x2)
 */
    public void simplify(){
        Vector tmp=new Vector();
        //First: Remove the terms with factors nulls
        for (int i=factors.size()-1; i>=0;  i--){
            if (((Double)factors.elementAt(i)).doubleValue()==0.0){
                factors.remove(i);
                terms.remove(i);
                //i--;
            }
        }
        
        for (int i=factors.size()-1; i >= 0 ; i--){
            for (int j=factors.size()-1; j >= (i+1) ; j--){
                if (((ContinuousFunction)terms.elementAt(i)).equals((ContinuousFunction)terms.elementAt(j))){
                    double sumcoef=getFactor(i)+getFactor(j);
                    factors.setElementAt(new Double(sumcoef),i);
                    factors.remove(j);
                    terms.remove(j);
                    //j--;
                }
            }
        }
        
        
    }
    
/* if the MixtExpDensity has a normal density distribution. Get the mean of this
 * normal distribution.
 * @param pos, the MixtExpDensity'terms to get its normal mean.
 * @return double, the normal mean.
 */
    
    public double getMean(int pos){
        
        if (pos>=factors.size())
            return -1;
        
        if (getExponent(pos).getClass()!=QuadraticFunction.class)
            return -1;
        
        if (((QuadraticFunction)getExponent(pos)).isLinear())
            return -1;
        
        if (getFactor(pos)==0.0)
            return -1;
        
        QuadraticFunction qf = (QuadraticFunction)getExponent(pos);
        Vector vars = qf.getVariables();
        Continuous var= (Continuous)vars.elementAt(0);
        
        double coef1=qf.getCoefficient(var,var);
        double coef2=qf.getCoefficient(var);
        
        double desv=Math.sqrt(-1/coef1);
        
        double mean=-coef2/(2*coef1);
        
        return mean;
        
        
    }
    
/* if the MixtExpDensity has a normal density distribution. Get the desviation of this
 * normal distribution.
 * @param pos, the MixtExpDensity'terms to get its normal desviation.
 * @return double, the normal desviation.
 */
    
    public double getDesviation(int pos){
        if (pos>=factors.size())
            return -1;
        
        if (getExponent(pos).getClass()!=QuadraticFunction.class)
            return -1;
        
        if (((QuadraticFunction)getExponent(pos)).isLinear())
            return -1;
        
        if (getFactor(pos)==0.0)
            return -1;
        
        QuadraticFunction qf = (QuadraticFunction)getExponent(pos);
        Vector vars = qf.getVariables();
        Continuous var= (Continuous)vars.elementAt(0);
        
        double coef1=qf.getCoefficient(var,var);
        double coef2=qf.getCoefficient(var);
        
        double desv=Math.sqrt(-1/(2*coef1));
        
        double mean=-coef2/(2*coef1);
        
        return desv;
        
    }
    
    public boolean equals(MixtExpDensity exp){
        
        
        simplify();
        exp.simplify();
        if (!equals(independent,exp.getIndependent())){
            return false;
        }
        if (exp.getNumberOfExp()!=getNumberOfExp()){
            return false;
        }
        for (int i=0; i<exp.getNumberOfExp(); i++){
            if (!equals(getFactor(i),exp.getFactor(i)))
                return false;
            if (!getExponent(i).equals(exp.getExponent(i)))
                return false;
        }
        return true;
    }
    private boolean equals(double coef1, double coef2){
        
        if (Math.abs(coef1-coef2)>Math.pow(10,-2))
            return false;
        else
            return true;
        
    }
    
    
    /**
     * @return the vector of terms.
     */
    
    public Vector getTerms() {
        
        return terms;
    }
    
    /**
     * Estimates a mixture of two exponentials for one variable. Our mixture will
     * be of the type a*exp(bx) + d*exp(fx)+constante. It si very similar to estimate1
     * but we only use constante  if it is needed. We use the simple regression method.
     *
     * @param var The variable we are trying to modelize
     * @param sample The sample we have of the variable.
     * @param values The density of the sample.
     * @param der Used for Derivative method.
     *
     * @return the MixtExpDensity which best aproximates the sample.
     */
    
    public MixtExpDensity estimate6(Continuous var, Vector sample, Vector values, int der,double first, double last){
        
        MixtExpDensity result,exp1,exp2;
        double newErrorSube,newerrorSube;
        double a = 0, newA=0;
        double b = 0, newB=0;
        double primero, ultimo;
        double d = 0, newD=0;
        double f = 0, newF=0;
        double ystar,newDres,newFres,constanteSube,newBres,newAres;
        // We will use this two variables to measure the error.
        double newerror=2, intermederror, lasterror=2;
        
        double K1 = 0;
        double K = 0, constant =0, newConstant;
        boolean change, decrease;
        double H = 0, H1 = 0, H2 = 0;
        
       // double Segundofirst;
      //Segundofirst = ((Double)sample.elementAt(1)).doubleValue();
        
        //sign = 0, all positive, =1 all negative, =2 some positive and some negative
        int i, iteration = 1;
        double intermedReal;
        double yValue, xValue, primeroValue, ultimoValue,primerodens,ultimodens,multiplica,suma;
        Vector intermediateVector;
        Vector coef = new Vector();
        Vector others = new Vector();
        result = new MixtExpDensity(0);
        Vector coefVector = new Vector();
        //System.out.println("************************* BEGINNING TO ESTIMATE THE EXPONENTIAL *******************************");
        
        //Now we make a first estimation through the derivati to begin with
        //System.out.println("\nDoing first estimation");
        primero = ((Double)sample.firstElement()).doubleValue();
        ultimo = ((Double)sample.lastElement()).doubleValue();
        //primerodens = ((Double)values.firstElement()).doubleValue();
        primerodens = findFirstDens(values);
        //ultimodens = ((Double)values.lastElement()).doubleValue();
        ultimodens = findLastDens(values);
        
        //System.out.println("In the sample we have: x1="+primero+" -- y1="+primerodens+" , and xultimo="+ultimo+" -- yultimo="+ultimodens);

        //System.out.println("Los valores son ");
        //for (int i1 = 0; i1 < sample.size(); i1++){
          //  System.out.println(((Double)sample.elementAt(i1)).doubleValue()+" "+((Double)values.elementAt(i1)).doubleValue());
        //}
        
        coef=result.estimateExponentialWithIndependentByDerivative(sample,values,der);
        a = ((Double)coef.elementAt(0)).doubleValue();
        b = ((Double)coef.elementAt(1)).doubleValue();
        constant = ((Double)coef.elementAt(2)).doubleValue();
        //System.out.println("a: "+a+" b: "+b+" constant: "+constant);
        
        result = new MixtExpDensity(a,b,d,f,constant,var);
        
        lasterror=0;
        for(i =0 ; i<sample.size() ; i++){
            
            yValue = ((Double)values.elementAt(i)).doubleValue();
            xValue = ((Double)sample.elementAt(i)).doubleValue();
            lasterror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
            
        }
        // This is the error with the derivative method
        lasterror = Math.sqrt(lasterror/sample.size());
        //System.out.println("El error cuadratico medio E: "+lasterror);
        
        
        //Now we calculate the first exponential and work with the best one
        result = new MixtExpDensity(0);
        coef = new Vector();
        change = false;
        
         //System.out.println("Hago cambios en los yvalues (para q vaya bien el checkdata) : ");
        // Now we change the values, so that the checkData method does not mess them
        for(i =0 ; i<values.size() ; i++){
            
            yValue = ((Double)values.elementAt(i)).doubleValue();
            others.addElement(new Double(yValue));
            // System.out.print(yValue+"   ");
            
        }
        
        //System.out.println("Sample: "+sample.size()+", "+others.size());
        change = checkData(sample,others);
        
        //System.out.println("\nDespues del checkdata, los valores son: (se supone q ya no salen negativos los y) ");
        //for (int i1 = 0; i1 < sample.size(); i1++){
          //  System.out.println(((Double)sample.elementAt(i1)).doubleValue()+" "+((Double)others.elementAt(i1)).doubleValue());
        //}
        
        // Now we have to calculate a regression. No  z will be
        // negative now
        
        coef = exponentialRegression(sample ,others);
        
        newA = ((Double)coef.elementAt(0)).doubleValue();
        newB = ((Double)coef.elementAt(1)).doubleValue();
        newConstant=0;
        if (change){newA = -newA;}
        
        //constant calculation
        result = new MixtExpDensity(newA,newB,d,f,newConstant,var);
        
        K = 0;
        for(i =0 ; i<sample.size() ; i++){
            
            yValue = ((Double)values.elementAt(i)).doubleValue();
            xValue = ((Double)sample.elementAt(i)).doubleValue();
            K = K+ (yValue-result.getValue(var,xValue));
            
        }//End of for
        
        K = K/sample.size();
        
        newConstant = K;
        
        
        //We take a look at the error to compare it with the derivative one
        result = new MixtExpDensity(newA,newB,d,f,newConstant,var);
        
        newerror =0;
        for(i =0 ; i<sample.size() ; i++){
            
            yValue = ((Double)values.elementAt(i)).doubleValue();
            xValue = ((Double)sample.elementAt(i)).doubleValue();
            
            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
            
        }
        // Error with simple regression
        newerror = Math.sqrt(newerror/sample.size());
        //System.out.println("Error con regresion: "+newerror);
        
        if (newerror >= (lasterror) ) {
            
            //System.out.println("The derivative is the best");
        }else {
            
            
            lasterror = newerror;
            a=newA;
            b=newB;
            constant = newConstant;
        }
        newA=0;
        newB=0;
        
        
        // Now I will check if the first or the last element has a negative value of the density
        result = new MixtExpDensity(a,b,d,f,constant,var);
        
        primeroValue = result.getValue(var,first);
        ultimoValue = result.getValue(var,last);
        //System.out.println("En la primera estimacion, el primero es "+primeroValue+" y el ultimo "+ultimoValue);
        
        if (primeroValue < 0 ){
            //System.out.println("El primero es negativo en la primera estimacion");
            
            // I modify K in order to make the value of the density equal to priemerodens/1000
	    // First I have to check if primerodens is zero. If it is zero I will take the density of the second value.
            // THIS HAS ALREADY BEEN SOLVED BY FINDFIRSTDENS
            //if(primerodens == 0.0){
		//double second = ((Double)sample.elementAt(1)).doubleValue();
                //double seconddens = ((Double)values.elementAt(1)).doubleValue();
		//System.out.println("Como la densidad del primero es cero escogo la del segundo, que es "+seconddens);
		//primerodens = seconddens;
	    //}


            result = new MixtExpDensity(a,b,d,f,((primerodens/1000)-(a*Math.exp(b*first))),var);
            //  result.print();
            
            double AprimeroValue = result.getValue(var,first);
            double AultimoValue =  result.getValue(var,last);
            //System.out.println("Corrigiendo con el cambio de independiente, el primero es "+AprimeroValue+" y el ultimo "+AultimoValue);
            
            newerror =0;
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                
                newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                
            }
            // This is the error with the new K
            newErrorSube = Math.sqrt(newerror/sample.size());
            
            
            
            // Now I fix the constant term, and I re-estimate the others, but with values-k and restricted to (x1,primerodesn/1000))
            // This vector will be 'z=(values-K)'
            intermediateVector = new Vector();
            for (i=0 ; i<sample.size() ; i++){
                yValue = ((Double)values.elementAt(i)).doubleValue();
                intermediateVector.addElement(new Double(yValue));
            }//End of for
            
            coefVector = checkData2(sample,intermediateVector);
            
            // This is the quantity I have to multiply the exponential coefficient by, 1 or -1
            multiplica = ((Double)coefVector.elementAt(0)).doubleValue();
            
            // This is the quantity that has been added, so I have to substract it from the result
            suma = ((Double)coefVector.elementAt(1)).doubleValue();
            
            //Computing the regression coefficients, with the restriction that f(x1)=y1/1000
            
            ystar = multiplica*primerodens/1000;
            ystar = ystar + suma;
            
            coef = exponentialRegressionRestrict(sample,intermediateVector,first,ystar);
            
            newA = ((Double)coef.elementAt(0)).doubleValue();
            newB = ((Double)coef.elementAt(1)).doubleValue();
            
            newA = newA*multiplica;
            
            newConstant = suma;
            
            result = new MixtExpDensity(newA,newB,d,f,newConstant,var);
            
            newerror =0;
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                
                newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                
            }
            // This is the error with the restricted regression
            newerror = Math.sqrt(newerror/sample.size());
            
            AprimeroValue = result.getValue(var,first);
           // double Aprimero2 = result.getValue(var,Segundofirst);
            AultimoValue =  result.getValue(var,last);
            //System.out.println("Corrigiendo con el cambio de parametros, el primero es "+AprimeroValue+" , el segundo es "+Aprimero2+"y el ultimo "+AultimoValue);
        
            if (newerror < newErrorSube){
                
                //System.out.println("Eligo para solucionarlo cambiar los parametros");
                a = newA;
                b = newB;
                
                constant = newConstant;
            }
            else{
                //System.out.println("Eligo para solucionarlo cambiar el independiente");
                constant = ((primerodens/1000)-(a*Math.exp(b*first)));
            }
            
        }
        if (ultimoValue < 0){
            //System.out.println("El ultimo es negativo en la primera estimacion");
            //We change K so that f(xn)=ultimodens/1000
            result = new MixtExpDensity(a,b,d,f,((ultimodens/1000)-(a*Math.exp(b*last))),var);
            
            
            newerror =0;
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                
                newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                
            }
            // This is the error with the new K
            newErrorSube = Math.sqrt(newerror/sample.size());
            
            
            // Now I fix the constant term, and I re-estimate the others, but with values-k and restricted to (x1,ultimodesn/1000))
            // This vector will be 'z=(values-K)'
            intermediateVector = new Vector();
            for (i=0 ; i<sample.size() ; i++){
                yValue = ((Double)values.elementAt(i)).doubleValue();
                intermediateVector.addElement(new Double(yValue));
                
            }//End of for
            
            //System.out.println("Antes del checkdata2, los valores son: ");
            //for (int i1 = 0; i1 < sample.size(); i1++){
              //  System.out.println(((Double)sample.elementAt(i1)).doubleValue()+" "+((Double)intermediateVector.elementAt(i1)).doubleValue());
            //}
            
            coefVector = checkData2(sample,intermediateVector);
            
            //System.out.println("Despues del checkdata2, los valores son: ");
            //for (int i1 = 0; i1 < sample.size(); i1++){
             //   System.out.println(((Double)sample.elementAt(i1)).doubleValue()+" "+((Double)intermediateVector.elementAt(i1)).doubleValue());
            //}
            
            
            // This is the quantity that must multiply the coefficient of the exponential. 1 or -1
            multiplica = ((Double)coefVector.elementAt(0)).doubleValue();
            
            // This is the quantity that has just been added, so it has to be substracted from the result
            suma = ((Double)coefVector.elementAt(1)).doubleValue();
            
            //Computation of the regression coefficients, forcing that f(x1)=y1/1000
            
            ystar = multiplica*ultimodens/1000;
            ystar = ystar + suma;
            
            coef = exponentialRegressionRestrict(sample,others,last,ystar);
            
            newA = ((Double)coef.elementAt(0)).doubleValue();
            newB = ((Double)coef.elementAt(1)).doubleValue();
            
            newA = newA*multiplica;
            
            newConstant = suma;
            
            result = new MixtExpDensity(newA,newB,d,f,newConstant,var);
            
            newerror =0;
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                
                newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                
            }
            // This is the error associated to the restricted regression
            newerror = Math.sqrt(newerror/sample.size());
            if (newerror < newErrorSube){
                //System.out.println("Eligo para solucionarlo cambiar los parametros");
                a = newA;
                b = newB;
                lasterror = newerror;
                constant = newConstant;
            }
            else{
                //System.out.println("Eligo para solucionarlo cambiar el independiente");
                constant = ((ultimodens/1000)-(a*Math.exp(b*ultimo)));
                lasterror = newErrorSube;
            }
            
        }
        
        MixtExpDensity Aresult = new MixtExpDensity(a,b,d,f,constant,var);
        
        double AprimeroValue = Aresult.getValue(var,first);
        double AultimoValue = Aresult.getValue(var,last);
        //System.out.println("En la primera estimacion, tras corregir negativos, el primero es "+AprimeroValue+" y el ultimo "+AultimoValue);
        
        //System.out.println("\nAcaba la primera estimacion, paso al proceso iterativo");
        //System.out.println("Los valores iniciales son: a:"+a+" , b: "+b+" , K: "+constant);
        do {
            
            //System.out.println(" * Empiezo una iteracion * ");
            decrease = false;
            result = new MixtExpDensity(0);
            
            // Second Exponential
            
            change = false;
            intermediateVector = new Vector();
            coef = new Vector();
            
            // This vector will be 'z=(values-a*exp(b*sample)+K1'
            for (i=0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                intermedReal = (yValue-(a*Math.exp(b*xValue)+constant));
                intermediateVector.addElement(new Double(intermedReal));
                
            }//End of for
            
            change = checkData(sample,intermediateVector);
            
            coef = exponentialRegression(sample , intermediateVector);
            
            newD = ((Double)coef.elementAt(0)).doubleValue();
            newF = ((Double)coef.elementAt(1)).doubleValue();
            
            if (change){
                newD = -newD;
                
            }
            
            // I calculate a constant H that multplies D it. It will be calculated minimizing mean square error
            
            H1 = 0;
            H2 = 0;
            H = 0;
            
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                
                H1 += (yValue-(a*Math.exp(b*xValue))-constant)*(newD*Math.exp(newF*xValue));
                H2 += (newD*Math.exp(newF*xValue))*(newD*Math.exp(newF*xValue));
                
            }//End of for
            
            H = H1/H2;
            newD *= H;
            
            
            //Now I check the error
            result = new MixtExpDensity(a,b,newD,newF,constant,var);
            
            newerror=0;
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                
                newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                
            }//End of for
            
            newerror = Math.sqrt(newerror/sample.size());
            
            if (newerror >= (1*lasterror) ) {
                
                //System.out.println("We do not introduce a second exponential, becouse the new error "+newerror+" is worst than the former error  "+lasterror);
                
            }else {//If the error is lower
                
                
                // BEFORE INSERTING THE EXPONENTIAL WE MUST CHECK THAT THE LAST ELEMENT AND THE FIRST ONE ARE
                // POSITIVE
                
                primeroValue = result.getValue(var,first);
                ultimoValue = result.getValue(var,last);
                
                if((primeroValue > 0) && (ultimoValue > 0)){
                    decrease = true;
                    lasterror = newerror;
                    d=newD;
                    f=newF;
                }
                else{// Or the last one or the first one are negative. It must be solved, or not to introduce the term
                    
//                                        //��Meto esto aqui????
//                    if ((primeroValue < 0)&&(ultimoValue < 0 )){
//                        if (primeroValue < ultimoValue) ultimoValue = 0;
//                        else primeroValue = 0;
//                        System.out.println("El primero y el ultimo son negativos");
//                    }
                    
                    if(primeroValue < 0){//The first one is negative
                        //System.out.println("El primero es negativo tras obtener d y f, y vale : "+primeroValue);
                        
                        // We will try increasing K so that the first on is y0/1000
                        constanteSube = 0;
                        // This is the new constant
                        constanteSube = (primerodens/1000)-(a*Math.exp(b*first))-(newD*Math.exp(newF*first));
                        
                        // Now I will chechk its error
                        newerror=0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }//End of for
                        
                        newerrorSube = Math.sqrt(newerror/sample.size());
                        //This is the error associated to the new K.
                        
                        // We will try now with the restriction
                        intermediateVector = new Vector();
                        // This vector will be 'z=(values-a*exp(b*sample)+K1'
                        for (i=0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            intermedReal = (yValue-(a*Math.exp(b*xValue)+constant));
                            intermediateVector.addElement(new Double(intermedReal));
                            
                        }//End of for
                        coefVector = new Vector();
                        coefVector = checkData2(sample,intermediateVector);
                        
                        multiplica = 1;
                        // This is the quantity I have to multiply the coefficient. 1 or -1
                        
                        multiplica = ((Double)coefVector.elementAt(0)).doubleValue();
                        
                        suma = 0;
                        // this is the quantity that has just been added, so I have to substract it from the result.
                        
                        suma = ((Double)coefVector.elementAt(1)).doubleValue();
                        
                        // Now we will calcualte the regression coefficients, making that regression go through the point
                        // (x1,y*), where y* is y1/1000; Actually it is multiplica*(y*)+suma
                        
                        ystar = 0;
                        ystar = multiplica*((primerodens/1000)-(constant+(a*Math.exp(b*first))));
                        ystar = ystar + suma;
                        coef = new Vector();
                        coef = exponentialRegressionRestrict(sample,intermediateVector,first,ystar);
                        
                        newDres = ((Double)coef.elementAt(0)).doubleValue();
                        newFres = ((Double)coef.elementAt(1)).doubleValue();
                        
                        newDres = newDres*multiplica;
                        
                        newConstant = suma+constant;
                        
                        // Now it makes no sense to calculate an H, becouse there is just one H that fulfills
                        // that the function goes through (x0,y0/1000), and it is just as we have obtained the parameters
                        
                        result = new MixtExpDensity(a,b,newDres,newFres,newConstant,var);
                        
                        
                        newerror =0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }
                        newerror = Math.sqrt(newerror/sample.size());
                        // Error with the restricted function.
                        
                        //I will choose the lowet error
                        if (newerror < newerrorSube)//The restriction is better
                            if(newerror < lasterror){// It is better that what we already had
                                d = newDres;
                                f = newFres;
                                constant = newConstant;
                                lasterror = newerror;
                            }
                            else//Better increasing k
                                if(newerrorSube < lasterror){//It is better that what we already had
                                    lasterror = newerrorSube;
                                    constant = constanteSube;
                                }
                        
                    }
                    if(ultimoValue < 0 ){
                        //System.out.println("Al calcular el d y f el ultimo ha salido negativo : "+ultimoValue);
                        
                        //System.out.println("Probamos primero solo elevando el K y dejando los nuevos d y f., de forma que en el ultimo pase por yn/1000");
                        
                        constanteSube = (ultimodens/1000)-(a*Math.exp(b*last))-(newD*Math.exp(newF*last));
                        
                        //System.out.println("Esta es la nueva constante (total): "+constanteSube);
                        //System.out.println("La constante era: "+constant);
                        //System.out.println("Este es el nuevo result con esta constante");
                        
                        result = new MixtExpDensity(a,b,newD,newF,constanteSube,var);
                        //result.print();
                        //System.out.println("compruebo su error");
                        
                        newerror=0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }//End of for
                        
                        newerrorSube = Math.sqrt(newerror/sample.size());
                        //		System.out.println("El error total con la subida de K es: "+newerrorSube);
                        
                        //System.out.println("Ahora voy a probar con la restriccion.");
                        
                        intermediateVector = new Vector();
                        // This vector will be 'z=(values-a*exp(b*sample)+K1'
                        for (i=0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            intermedReal = (yValue-(a*Math.exp(b*xValue)+constant));
                            intermediateVector.addElement(new Double(intermedReal));
                            
                        }//End of for
                        coefVector = new Vector();
                        coefVector = checkData2(sample,intermediateVector);
                        
                        multiplica = 0;
                        //Esta es la cantidad por la que tengo que multiplicar el coeficiente de la exponencial. 1 o -1
                        multiplica = ((Double)coefVector.elementAt(0)).doubleValue();
                        
                        suma = 0;
                        //Esta es la cantidad que le han sumado, luego se la tengo que restar al resultado.
                        suma = ((Double)coefVector.elementAt(1)).doubleValue();
                        
                        //Ahora voy a calcular los coeficientes de la regresion, pero haciendo que dicha regresion pase por el
                        // punto (x1,y*), donde y* es y1/1000; en realidad es multiplica*(y*)+suma.
                        ystar = 0;
                        ystar = multiplica*((ultimodens/1000)-(constant+(a*Math.exp(b*last))));
                        ystar = ystar + suma;
                        coef = new Vector();
                        coef = exponentialRegressionRestrict(sample,intermediateVector,last,ystar);
                        
                        newDres = ((Double)coef.elementAt(0)).doubleValue();
                        newFres = ((Double)coef.elementAt(1)).doubleValue();
                        
                        newDres = newDres*multiplica;
                        
                        newConstant = suma+constant;
                        
                        //Ahora no tiene sentido calcular un H, porque s�lo hay un H que cumple que
                        // la funci�n pase por (x0,y0/1000), y es uno por como hemos calculado antes los parametros.
                        
                        result = new MixtExpDensity(a,b,newDres,newFres,newConstant,var);
                        
                        //System.out.println("Este es el resultado de la regresion, restringida");
                        //result.print();
                        newerror =0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }
                        newerror = Math.sqrt(newerror/sample.size());
                        //System.out.println("Este es el error con la restringida: "+newerror);
                        
                        
                        //Escogo el menor error
                        if (newerror < newerrorSube)//Es mejor restringir que subir K
                            if(newerror < lasterror){//Es mejor que lo que tenia
                                decrease = true;
                                d = newDres;
                                f = newFres;
                                constant = newConstant;
                                lasterror = newerror;
                            }
                            else//Es mejor subir K
                                if(newerrorSube < lasterror){//Es mejor que lo que tenia
                                    decrease = true;
                                    lasterror = newerrorSube;
                                    constant = constanteSube;
                                }
                        
                    }//End of if del que ha salido negativo es el ultimo
                    
                    
                }//End of else{//O el ultimo o el primero es negativo, he de solucionarlo o no meter el termino.
            }
            newD=0;
            newF=0;
            newDres = 0;
            newFres = 0;
            constanteSube = 0;
            newerrorSube = 0;
            newConstant = 0;
            
            //Now we work over the first exponential
            result = new MixtExpDensity(0);
            intermediateVector = new Vector();
            coef = new Vector();
            change = false;
            
            // This vector will be 'z=(values-(d*exp(f*sample)+K1))'
            for (i=0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                intermedReal = (yValue -(d*Math.exp(f*xValue)+constant));
                intermediateVector.addElement(new Double(intermedReal));
                
            }//End of for
            
            change = checkData(sample,intermediateVector);
            
            // Now we have to calculate a regression. No  z will be
            // negative now
            
            coef = exponentialRegression(sample , intermediateVector);
            
            newA = ((Double)coef.elementAt(0)).doubleValue();
            newB = ((Double)coef.elementAt(1)).doubleValue();
            
            if (change) newA = -newA;
            
            //Now I calculate a constant that multpilies H. It will be calculated by mean square error
            
            H1 = 0;
            H2 = 0;
            H = 0;
            
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                
                H1 += (yValue-(d*Math.exp(f*xValue))-constant)*(newA*Math.exp(newB*xValue));
                H2 += (newA*Math.exp(newB*xValue))*(newA*Math.exp(newB*xValue));
                
            }//End of for
            
            H = H1/H2;
            newA *= H;
            
            //Now I check the error to see if it is worthy to include this exponential
            result = new MixtExpDensity(newA,newB,d,f,constant,var);
            
            
            newerror=0;
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                
            }
            newerror = Math.sqrt(newerror/sample.size());
            
            if (newerror >= lasterror) {
                
                //System.out.println("We do not change the first exponential since its associated error  "+newerror+" is worst than the former one "+lasterror);
                
            }else {
                //BEFORE INSERTING THIS EXPONENTIAL, WE MUST CHECK THAT THE LAST ELEMENT AND THE FIRST ELEMENT ARE BOTH POSITIVE
                
                primeroValue = result.getValue(var,first);
                ultimoValue = result.getValue(var,last);
                
                if((primeroValue > 0) && (ultimoValue > 0)){
                    decrease = true;
                    lasterror = newerror;
                    a=newA;
                    b=newB;
                }
                else{//The first element or the last element is negative. It must be solved or not to introduce the term.
                    
//                    //��Meto esto aqui????
//                    if ((primeroValue < 0)&&(ultimoValue < 0 )){
//                        if (primeroValue < ultimoValue) ultimoValue = 0;
//                        else primeroValue = 0;
//                        System.out.println("El primero y el ultimo son negativos");
//                    }
                    

                    if(primeroValue < 0){// the first element is negative
                        //System.out.println("El primero es negativo tras obtener a y b, y vale : "+primeroValue);
                        // We modify K so that f(x0)=y0/1000
                        constanteSube = (primerodens/1000)-(d*Math.exp(f*first))-(newA*Math.exp(newB*first));
                        
                        result = new MixtExpDensity(newA,newB,d,f,constanteSube,var);
                        
                        newerror=0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }//End of for
                        
                        // This is the error associated to this new constant
                        newerrorSube = Math.sqrt(newerror/sample.size());
                        
                        //Computation of the restricted regression
                        
                        intermediateVector = new Vector();
                        // This vector will be 'z=(values-a*exp(b*sample)+K1'
                        for (i=0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            intermedReal = (yValue-(d*Math.exp(f*xValue)+constant));
                            intermediateVector.addElement(new Double(intermedReal));
                            
                        }//End of for
                        coefVector = new Vector();
                        coefVector = checkData2(sample,intermediateVector);
                        
                        // Quantity that must multiply the coefficient of the exponential. 1 or -1
                        multiplica = ((Double)coefVector.elementAt(0)).doubleValue();
                        
                        // Quantity that has just been added, so it must be substracted from the result.
                        suma = ((Double)coefVector.elementAt(1)).doubleValue();
                        
                        // Computation of the regression coefficients (restricted) so that f(x1)=y1/1000
                        
                        ystar = multiplica*((primerodens/1000)-(constant+(d*Math.exp(f*first))));
                        ystar = ystar + suma;
                        coef = new Vector();
                        coef = exponentialRegressionRestrict(sample,intermediateVector,first,ystar);
                        
                        newAres = ((Double)coef.elementAt(0)).doubleValue();
                        newBres = ((Double)coef.elementAt(1)).doubleValue();
                        
                        newAres = newAres*multiplica;
                        
                        newConstant = suma+constant;
                        
                        // Now it will make no sense to calculate an H coefficient, becouse there is just one
                        // verifying f(x1)=y1/1000, and it has just been calculated.
                        
                        result = new MixtExpDensity(newAres,newBres,d,f,newConstant,var);
                        
                        newerror =0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }
                        // THis is the error with the restricted regression
                        newerror = Math.sqrt(newerror/sample.size());
                        
                        //We choose the one with the lower errro
                        if (newerror < newerrorSube)//The restricted regression is better than modifying K.
                            if(newerror < lasterror){//The former one is better
                                a = newAres;
                                b = newBres;
                                constant = newConstant;
                                lasterror = newerror;
                            }
                            else//Modifying K is better
                                if(newerrorSube < lasterror){//The former one is better
                                    lasterror = newerrorSube;
                                    constant = constanteSube;
                                }
                        
                    }
                    if(ultimoValue < 0 ){
                        //System.out.println("El ultimo es negativo tras obtener a y b, y vale "+ultimoValue);
                        // We modify K so that f(xn)=yn/1000
                        constanteSube = (ultimodens/1000)-(d*Math.exp(f*last))-(newA*Math.exp(newB*last));
                        
                        result = new MixtExpDensity(newA,newB,d,f,constanteSube,var);
                        //		result.print();
                        
                        newerror=0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }//End of for
                        // Error associated to the new K
                        newerrorSube = Math.sqrt(newerror/sample.size());
                        
                        //Computation of the restricted regression
                        
                        intermediateVector = new Vector();
                        // This vector will be 'z=(values-a*exp(b*sample)+K1'
                        for (i=0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            intermedReal = (yValue-(d*Math.exp(f*xValue)+constant));
                            intermediateVector.addElement(new Double(intermedReal));
                            
                        }//End of for
                        coefVector = new Vector();
                        coefVector = checkData2(sample,intermediateVector);
                        
                        multiplica = 1;
                        
                        // Quantity that must multiply the coefficient of the exponential. 1 or -1
                        multiplica = ((Double)coefVector.elementAt(0)).doubleValue();
                        
                        suma = 0;
                        // Quantity that has just been added, so it must be substracted from the result.
                        suma = ((Double)coefVector.elementAt(1)).doubleValue();
                        
                        // Computation of the regression coefficients (restricted) so that f(xn)=yn/1000
                        
                        ystar = 0;
                        ystar = multiplica*((ultimodens/1000)-(constant+(d*Math.exp(f*last))));
                        ystar = ystar + suma;
                        coef = new Vector();
                        coef = exponentialRegressionRestrict(sample,intermediateVector,last,ystar);
                        
                        newAres = ((Double)coef.elementAt(0)).doubleValue();
                        newBres = ((Double)coef.elementAt(1)).doubleValue();
                        
                        newAres = newAres*multiplica;
                        
                        newConstant = suma+constant;
                        
                        // Now it will make no sense to calculate an H coefficient, becouse there is just one
                        // verifying f(xn)=yn/1000, and it has just been calculated.
                        
                        result = new MixtExpDensity(newAres,newBres,d,f,newConstant,var);
                        
                        newerror =0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }
                        newerror = Math.sqrt(newerror/sample.size());
                        //System.out.println("Este es el error con la restringida: "+newerror);
                        
                        
                        //Escogo el menor error
                        if (newerror < newerrorSube)//Es mejor restringir que subir K
                            if(newerror < lasterror){//Es mejor que lo que tenia
                                decrease = true;
                                a = newAres;
                                b = newBres;
                                constant = newConstant;
                                lasterror = newerror;
                            }
                            else//Es mejor subir K
                                if(newerrorSube < lasterror){//Es mejor que lo que tenia
                                    decrease = true;
                                    lasterror = newerrorSube;
                                    constant = constanteSube;
                                }
                        
                    }//End of if del que ha salido negativo es el ultimo
                    
                }//End of else (uno de los dos es negativo)
            }//End of else (el error es menor)
            
            newA=0;
            newB=0;
            newAres = 0;
            newBres = 0;
            constanteSube = 0;
            newerrorSube = 0;
            //Now we reconstantruct the Mixture to calculate again the constant
            result = new MixtExpDensity(a,b,d,f,0,var);
            //result = ConstructMixt(1,b,1,a,d,f,0,0,var);
            
            K = 0;
            
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                K = K+ (yValue-result.getValue(var,xValue));
                
            }//End of for
            
            K = K/sample.size();
            newConstant = K;
            
            //Now I check the error, to see if it is worthy to include this exponential
            result = new MixtExpDensity(a,b,d,f,newConstant,var);
            
            
            newerror=0;
            for(i =0 ; i<sample.size() ; i++){
                
                yValue = ((Double)values.elementAt(i)).doubleValue();
                xValue = ((Double)sample.elementAt(i)).doubleValue();
                
                newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                
            }
            newerror = Math.sqrt(newerror/sample.size());
            
            if (newerror >= lasterror ) {
                
                //System.out.println("No cambiamos la constant ya que el error que se produce "+newerror+" es peor que el que hay "+lasterror);
                
            }else {
                //ANTES DE PONERLO HE DE COMPROBAR QUE NI EL ULTIMO MI EL PRIMERO ES CERO
                
                primeroValue = result.getValue(var,first);
                ultimoValue = result.getValue(var,last);
                
                if((primeroValue > 0) && (ultimoValue > 0)){
                    decrease = true;
                    constant = newConstant;
                    lasterror = newerror;
                }
                else{//O el ultimo o el primero es negativo, he de solucionarlo o no meter el termino.
                    //System.out.println("el potencial q da valores negativos es ");
                    //result.print();
                    
                    //Por si los dos son negativos, vamos a ver cual es el mas peque�o y ese es el q se modifica:
                    if ((primeroValue < 0)&&(ultimoValue < 0 )){
                        if (primeroValue < ultimoValue) ultimoValue = 0;
                        else primeroValue = 0;
                        //System.out.println("El primero y el ultimo son negativos");
                    }
                    
                    if(primeroValue < 0){//Es el primero el que es cero
                        //System.out.println("El primero es negativo, al final, y vale "+primeroValue);
                        //pongo la constante tal que pase por (x0,y0/1000)
                        
                        newConstant = (primerodens/1000)-(a*Math.exp(b*first))-(d*Math.exp(f*first));
                        //System.out.println("La constante es "+newConstant);
                        result = new MixtExpDensity(a,b,d,f,newConstant,var);
                        //System.out.println("Tras corregirlo el primero vale "+result.getValue(var,first)+" y el ultimo "+result.getValue(var,last));
                        
                        newerror = 0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }
                        newerror = Math.sqrt(newerror/sample.size());
                    }//End of if (es el primero)
                    
                    if (ultimoValue < 0){//Es el ultimo es que es cero
                        //pongo la constante tal que pase por (x0,y0/1000)
                        //System.out.println("El ultimo es negativo al final, y vale "+ultimoValue);
                        newConstant = (ultimodens/1000)-(a*Math.exp(b*last))-(d*Math.exp(f*last));
                        //System.out.println("La constante es "+newConstant);
                        result = new MixtExpDensity(a,b,d,f,newConstant,var);
                        //System.out.println("Tras corregirlo el ultimo vale "+result.getValue(var,last)+" y el primero "+result.getValue(var,first));
                        
                        newerror = 0;
                        for(i =0 ; i<sample.size() ; i++){
                            
                            yValue = ((Double)values.elementAt(i)).doubleValue();
                            xValue = ((Double)sample.elementAt(i)).doubleValue();
                            
                            newerror += (yValue-result.getValue(var,xValue))*(yValue-result.getValue(var,xValue));
                            
                        }
                        newerror = Math.sqrt(newerror/sample.size());
                    }
                    //System.out.println("El error de antes era: "+lasterror+", y el de ahora haciendo el ultimo positivo es "+newerror);
                    if (newerror <= lasterror){
                        //System.out.println("Como el error es menor, cambio la constante");
                        if(newerror < lasterror)
                            decrease = true;
                        constant = newConstant;
                        lasterror = newerror;
                    }
                    //else
                      //  System.out.println("Como el error no es menor, no cambio la constante");
                    
                    //System.out.println("La constante es "+constant);
                    
                }//End of else //O el ultimo o el primero es negativo, he de solucionarlo o no meter el termino.
            }//End of else (el error es menor)
            
            newConstant=0;
            
            
            //Now we reconstantruct the Mixture
            //System.out.println("Al final de la iteracion "+iteration);
            result = new MixtExpDensity(a,b,d,f,constant,var);
            //System.out.println("El valor del ultimo tras corregir lo negativo es "+result.getValue(var,last));
            //System.out.println("El valor del primero tras corregir lo negativo es "+result.getValue(var,first));
            
            //System.out.println("a:"+a+"   b:"+b+"   d:"+d+"   f:"+f+"   constant:"+constant);
            
            //System.out.println("El error en la iteracion "+iteration+" es "+lasterror);
            
            iteration++;
            
        }while ((decrease) && (iteration < 101)); //End of do-while
        
        //  System.out.println("The final error is "+lasterror);
        
        //System.out.println("COMPROBAMOS EL PRIMERO Y EL ULTIMO");
        
        
        //Ahora compruebo que no devuelvo algo negativo en alguno de los dos extremos
        primeroValue = result.getValue(var,first);
        ultimoValue = result.getValue(var,last);
        //System.out.println("El primero es: "+first+"  "+primeroValue);
        //System.out.println("El ultimo es: "+last+"    "+ultimoValue);
        //System.out.println("El primero es: "+first+" y su densidad es "+primeroValue);
        //System.out.println("El ultimo es: "+last+" y su densidad es "+ultimoValue);
        
        if (primeroValue < 0){
            System.out.println("ERROR FATAL: EL PRIMERO ES NEGATIVO");
            //result.print();
        }
        if (ultimoValue < 0){
            System.out.println("ERROR FATAL: EL ULTIMO ES NEGATIVO");
            result.print();
        }
        //  System.out.println("Resultado: ");
        //  result.print();
        //System.out.println("---------------- TERMINAMOS DE ESTIMAR LA EXPONENCIAL -----------------");
        return result;
        
    }//End of method
    
    
    
    /**
     * Transforms the data (X,Y) so that we can apply exponential regression to it.
     * The problem is that Y cannot be negative. Lo voy a usar cuando luego no voy a calular la K otra vez
     *
     * @param X The X values.
     * @param Y The Y (density) values
     * @return A vector indicating what we have done. In the forst position we put the coefficient that must multiply
     * the exponential (1 or -1) and in the second plce the constant we have just added.
     */
    
    
    public Vector checkData2(Vector X, Vector Y){
        
        double xValue, yValue, min, K1, a, b, x1, x2, y1, y2,nuevaConstanteCoef,nuevaConstanteSuma;
        int negative = 0, positive = 0, midpoint =0, i;
        boolean change = false;
        Vector result = new Vector();
        result.addElement(new Double(1));
        result.addElement(new Double(0));
        //First we check if the values are negative or positive.
        for (i=0 ; i<Y.size() ; i++){
            if (((Double)Y.elementAt(i)).doubleValue() < 0 ) negative++;
            else {    //Else 1
                if (((Double)Y.elementAt(i)).doubleValue() > 0 ) positive++;
                else { //Else 2
                    positive++;
                    negative++;
                } //End of else 2
            }//End of else 1
        }//End of for
        
        if (negative == 0) // All are positive
            change = false;
        
        if (positive == 0){ // All are negative
            for (i=0 ; i<X.size() ; i++){
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                Y.setElementAt(new Double(-yValue),i);
            }// End of for
            change = true;
            //Como son todos negativos el termino que multiplica a la exponencial ha de cambiar de signo.
            result.setElementAt(new Double(-1),0);
        }// End of if
        
        if ((positive > 0) && (negative > 0)){//Some negative and some positive
            
            min = 0;
            
            for (i=0 ; i<Y.size() ; i++){
                
                //Now I will calculate the K I have to sum to yvalues
                // to be able to do the esponential regression
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                if (yValue < min ) min = yValue;
                
            }//End of for
            
            //Tengo que sumarles a todos este valor.
            K1 = -min +1;
            result.setElementAt(new Double(K1),1);
            //System.out.println("Este es el K que le sumo para poder aplicar la regresion: "+K1);
            for (i=0 ; i<Y.size() ; i++){
                
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                Y.setElementAt(new Double(yValue+K1),i);
                
            }//End of for
            
            //El termino que multiplica a la exponencial ha de permanecer como esta
            //change = false;
            result.setElementAt(new Double(1),0);
            
        }// End of if
        
        // Now I have to check whether data comes from a
        // convex function or not. If it does, I will have
        // to transform the data since with the exponential
        // regression the coefficient is always positive.
        
        // TO do so I will bild up the line that goes through the
        // first and the last point and see wether it passes up or down
        // the points (just one of them). If it goes down, it is convex.
        
        // The first point
        //System.out.println("Estoy en CheckData, line 1570. EL tama�o de X es: "+X.size());
        x1 = ((Double)X.elementAt(0)).doubleValue();
        y1 = ((Double)Y.elementAt(0)).doubleValue();
        
        // The last point
        x2 = ((Double)X.elementAt(X.size()-1)).doubleValue();
        y2 = ((Double)Y.elementAt(Y.size()-1)).doubleValue();
        
        // The line will be y = ax + b
        a = (y2-y1)/(x2-x1);
        b = y1 - a*x1;
        
        //Now Iwill check if it passes up or down
        if (X.size()%2 == 1) midpoint = (X.size()+1)/2;
        else midpoint = X.size()/2;
        
        yValue = ((Double)Y.elementAt(midpoint)).doubleValue();
        xValue = ((Double)X.elementAt(midpoint)).doubleValue();
        
        
        if (yValue > (a*xValue+b)){ // It is convex
            
            for (i=0 ; i<Y.size() ; i++){
                
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                Y.setElementAt(new Double(-yValue),i);
                
            }//End of for
            
            min = 0;
            
            for (i=0 ; i<Y.size() ; i++){
                
                //Now I will calculate the K I have to sum to yvalues
                // to be able to do the exponential regression
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                if (yValue < min ) min = yValue;
                
            }//End of for
            
            //TEngo que poner las dos constantes que he calculado.
            K1 = -min +1;
            nuevaConstanteSuma = ((Double)result.elementAt(1)).doubleValue();
            nuevaConstanteSuma = -nuevaConstanteSuma+K1;
            result.setElementAt(new Double(nuevaConstanteSuma),1);
            for (i=0 ; i<Y.size() ; i++){
                
                yValue = ((Double)Y.elementAt(i)).doubleValue();
                Y.setElementAt(new Double(yValue+K1),i);
                
            }//End of for
            
            //El termino que multplica a la exponencial ha de cambiar de signo
            
            nuevaConstanteCoef = ((Double)result.elementAt(0)).doubleValue();
            nuevaConstanteCoef = -nuevaConstanteCoef;
            result.setElementAt(new Double(nuevaConstanteCoef),0);
            //change = !change;
            
        }//End of if
        
        return(result);
        
    }//End of method
    
    /**
     * Computes an exponential regression y=a*exp(bx)
     * of two variables
     * given by two samples, X and Y. It will return a vector
     * with two elements, the first will be the coefficient of the
     * exponential and the second the exponent
     *
     * @param X The independent variable
     * @param Y The dependent variable
     *
     * @return A vector with the coefficients
     */
    
    public Vector exponentialRegressionRestrict(Vector X, Vector Y, double primero, double ystar){
        
        Vector intermediateVector = new Vector();
        int i;
        double intermedReal,loga,logystar,astar,a,b,c1,c2,xi,yi;
        Vector solution = new Vector();
        
        // we have to transform y=a*exp(bx) to
        // log(y)=log(a) + bx
        // so we transform Y to log(y)
        
        //Lo primero que he de hacer es los logaritmos
        
        logystar = Math.log(ystar);
        
        for (i=0 ; i<Y.size() ; i++){
            if (((Double)Y.elementAt(i)).doubleValue()<=0) System.out.println("Failure when computing the exponential regression. In exponentialRegressionRestrict");
            intermedReal = Math.log(((Double)Y.elementAt(i)).doubleValue());
            intermediateVector.addElement(new Double(intermedReal));
        }//End of for
        
        c1 = 0;
        c2 = 0;
        
        for (i=0 ; i<X.size() ; i++){
            xi = ((Double)X.elementAt(i)).doubleValue();
            yi = ((Double)intermediateVector.elementAt(i)).doubleValue();
            
            c1 = c1+((xi-primero)*(yi-logystar));
            c2 = c2+((xi-primero)*(xi-primero));
        }//End of for
        
        b = c1/c2;
        
        astar = logystar-(b*primero);
        
        a = Math.exp(astar);
        
        solution.addElement(new Double(a));
        solution.addElement(new Double(b));
        
        return solution;
        
    }// End of method


/**
 * This method simplifies in the following way:
 * exp(x0 + 2 x0) = exp (3 x0).
 *
 */

public void simplify2(){

  int i;
  
  for(i=0 ; i < terms.size() ; i++){
  
    ((LinearFunction)getExponent(i)).simplifyT();
    
  
  }
  
}

    /**
     * This method prunes a tree just eliminating exponentials terms
     * so that remains only two exponential terms.
     *
     * @param conf The configuration of the tree
     * 
     */
    public MixtExpDensity prune2Leaf(ContinuousIntervalConfiguration conf ){
        
        int i, tam;
        MixtExpDensity mte = new MixtExpDensity();
        int toRemove;
        double minWeight, coef;
        LinearFunction exponent; 
        MixtExpDensity mteNew, copyMTE; 
        MixtExpDensity res = duplicate();
        double k = 0, total, m = 0, maxTermI = 0;
        double first = 0, last = 0; 
        Continuous varX = new Continuous();
        boolean pos = false; // Indica si el peso es negativo o positivo
        
        ContinuousProbabilityTree copyCPT = new ContinuousProbabilityTree(this);
        
       //  System.out.println("Esta es la hoja a la que le hacemos el prune2Leaf");
        // print();
        
        for (i=0 ; i<conf.size() ; i++){
            copyCPT = copyCPT.integral((Continuous)conf.getVariable(i),conf.getLowerValue(i),conf.getUpperValue(i),1);
        }
        // This is the value of the MTE on this interval
        total = (copyCPT.getProb()).getIndependent();
       //  System.out.println("Peso total de la hoja: "+total);
        tam = (res.factors).size();
       //  System.out.println("N�mero de t�rminos exponenciales: "+tam);
        if(tam > 0){// Ahora miro a ver cu�l es la vble que aparece en la MTE, y miro los
            // valores para los que esta definida, y los guardo en first y last
            
            varX = ((LinearFunction)getExponent(0)).getVar1(0);
            first = conf.getLowerValue(conf.indexOf(varX));
            last = conf.getUpperValue(conf.indexOf(varX));
            
        }
        
        while (tam > 2){// Como hay mas de dos terminos exponenciales
                                     // tenemos que seguir eliminando
       //      System.out.println("Hay mas de tres: "+tam);
            toRemove = -1;
            minWeight = 100000000;
            
            for( i = 0 ; i < res.getNumberOfExp() ; i++){
            
                exponent = (LinearFunction)(res.getExponent(i));
                coef = res.getFactor(i);
                
                // This MixtExpDensity has just the term we want to delete
                mteNew = new MixtExpDensity(0.0);
                ((Vector)(mteNew.terms)).addElement(exponent);
                ((Vector)(mteNew.factors)).addElement(new Double(coef));
                copyMTE = new MixtExpDensity();
                copyMTE = mteNew.duplicate();
                //System.out.println("This should be zero: "+copyMTE.getIndependent());
                // Now the weight of the term is calculated
                for (int d=0 ; d<conf.size() ; d++){
                    copyMTE = copyMTE.integral((Continuous)conf.getVariable(d),conf.getLowerValue(d),conf.getUpperValue(d),1);
                }
                k = copyMTE.getIndependent();
                
               //  System.out.println("This is the weight of the term "+i+" : "+Math.abs(k));
                maxTermI = Math.max(mteNew.getValue(varX,first),mteNew.getValue(varX,last));
                // Ahora vemos si el peso es el mayor, y lo eliminamos.
                
                if(Math.abs(k) < minWeight){
                    toRemove = i;
                    minWeight = Math.abs(k);
                    m = maxTermI;
                    if(k > 0)
                        pos = true;
                    else
                        pos = false;
                }
                
                        
            }// Fin del for
            // Ahora ya sabemos cu�l es el t�rmino a eliminar.
            
         //    System.out.println("Hay que eliminar el t�rmino de la posici�n "+toRemove);
            
            //copyMTE = new MixtExpDensity(0.0);
            //copyMTE = duplicate();
            //(copyMTE.factors).removeElementAt(toRemove);
            //(copyMTE.terms).removeElementAt(toRemove);
            (res.factors).removeElementAt(toRemove);
            (res.terms).removeElementAt(toRemove);
            
            //Instead of adding the weight of the term to the independent term, we will
            // divide it up into all the terms (including independent term)
            //res = res.multiplyDensities(total/(total-k));
            // HACIENDOLO AS� HAY UN PROBLEMA, Y ES QUE SALE NEGATIVO A VECES, ASI QUE SE LO 
            // VOY A DAR AL TERMINO INDEPENDIENTE, A VER QUE SUCEDE
            // z is the volume of the intervals of the continuous variables
            
            // Para que al "eliminar" el termino en cuestion no pueda salir negativo, lo que hacemos es 
            // sumar el m�ximo valor del t�rmino que eliminamos (siempre que tenga peso positivo)
            // y luego normalizamos.
            
            if(pos){        
                
                double z = 1;// esta z tiene el "volumen", es decir, el producto de
                // las longitudes de todos los intervalos continuos
                
                for (int d=0 ; d<conf.size() ; d++){
                    z = z*(conf.getUpperValue(d)-conf.getLowerValue(d));
                }
                
                res.addIndependent(m);
                res = res.multiplyDensities(total/(total-minWeight+(m*z)));
                
            }
            else{// El peso es negativo, asi que solamente se "normaliza"
            
                 res = res.multiplyDensities(total/(total+minWeight));
            
            }
            //res.addIndependent(k/z);
                                      
            // This copy1 is the CPT that contains to copy MTE
            //ContinuousProbabilityTree copy1 = new ContinuousProbabilityTree(copyMTE);
            
            tam--;
            
        }//Fin del while
        
        
    return(res);    
    }


    /**
     * This method finds and returns the first positive value in values.
     *
     * @param values The vector with the density values
     *
     * @return The first positive density value
     * 
     */
    public double findFirstDens(Vector values){
        
        int i;
        double yValue, sol = 0; 
        
        for (i=0 ; i < values.size() ; i++){
        
            yValue = ((Double)values.elementAt(i)).doubleValue();
            if(yValue > 0.0)
                sol = yValue;
        }
        
        return sol;
    }
    
    /**
     * This method finds and returns the last positive value in values.
     *
     * @param values The vector with the density values
     *
     * @return The last positive density value
     * 
     */
    public double findLastDens(Vector values){
        
        int i;
        double yValue, sol = 0; 
        
        for (i=(values.size()-1) ; i >=0 ; i--){
        
            yValue = ((Double)values.elementAt(i)).doubleValue();
            if(yValue > 0.0)
                sol = yValue;
        }
        
        return sol;
    }
    
    
    /**
     * Replaces a variable in the density by a linear function.
     * It requires that the exponents are linear functions.
     * 
     * @param var the variable (Continuous) to replace.
     * @param lf the LinearFunction that will replace variable <code>var</code>.
     * @return a new density, where <code>var</code> is replaced by <code>lf</code>.
     */
    
    public MixtExpDensity replaceVariableByLF(Continuous v, LinearFunction lf) {
        
        MixtExpDensity newDensity = new MixtExpDensity();
        LinearFunction tempFunction, tempFunction2;
                
        for (int i=0 ; i<terms.size() ; i++) {
            tempFunction = (LinearFunction)((LinearFunction)terms.elementAt(i)).duplicate();
            
            double c = tempFunction.getCoefficient(v);
            
            if (tempFunction.indexOf(v)>=0) {
                tempFunction.removeVariable(v);
                tempFunction2 = (LinearFunction)lf.duplicate();
                tempFunction2.multiply(c);
                
                tempFunction = (LinearFunction)tempFunction.sumFunctions(tempFunction2);
                
                newDensity.addTerm((Double)factors.elementAt(i),tempFunction);
            }
        }
        
        return (newDensity);
    }
    
}// End of class
