/*
 * KernelDensity.java
 *
 */

package elvira.learning;

import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.tools.*;
import elvira.*;
import elvira.database.DataBaseCases;

import java.util.*;
import java.io.*;



/**
 *
 * Implements kernel densities from some known kernel functions.
 *
 * @author  avrofe
 * @version 1.0
 * @since 4/11/04
 */

public class KernelDensity {
    
    //the sample
    Vector sample; 
    //number of elements in the sample
    int n;
    
    //the bandwidth
    double h;
    //the bandwidth for gaussians, depends on the sample
    double h0;
    
    //indicate which kernel function we are going to use
    int k;
    
    static final int GAUSSIAN = 0;
    static final int BIWEIGTH = 1;
    static final int TRIWEIGTH = 2;
    static final int EPANECHNIKOV = 3;
    static final int COSINUS = 4;
    static final int TRIANGULAR = 5;
    
    
    /**
     * Creates a new instance of KernelDensity.
     */
    
    public KernelDensity(){
        
    }
    
    
    /**
     * Creates a new instance of KernelDensity from a sample.
     *
     * @param X a <code>Vector</code> with the sample
     */
    
    public KernelDensity(Vector X) {
        setSample(X);
        n = X.size();
        k = GAUSSIAN;
        //calculates h0 corresponding to this sample
        getGaussianH(X);
        h = h0;
    }
    
    
    /**
     * Creates a new instance of KernelDensity from a sample, especifying
     * the kernel function to use.
     *
     * @param X a <code>Vector</code> with the sample
     * @param kernelFunction an integer indicating which kernel function we are going to use
     */
    
    public KernelDensity(Vector X, int kernelFunction){
        
        setSample(X);
        n = X.size();
        //compruebo q es una de las funciones kernel q estoy considerando
        if ((kernelFunction>=0)&&(kernelFunction<=3)){
            k = kernelFunction;
            //calculates h0 corresponding to this sample
            getGaussianH(X);
            //calculates h according to the kernel function
            //except for cosinus and triangular, in this case, we consider h = h0
            if ((kernelFunction == 4)||(kernelFunction == 5))
                h = h0;
            else
                assignOptimalH();
        }
        else
            System.out.println("Please, specify the kernel function");
    }
    
    
    /**
     * Assigns to H the optimal bandwidth with the current kernel function.
     *
     */
    
    public void assignOptimalH(){
        
        switch(k){
            case GAUSSIAN: h = h0;
            break;
            case BIWEIGTH: h = h0 * 2.623;
            break;
            case TRIWEIGTH: h = h0 * 2.978;
            break;
            case EPANECHNIKOV: h = h0 * 2.214;
            break;
            default: System.out.println("Please, specify the kernel function");
        }
        
    }
    
    
    /*
     * Gets the Gaussian bandwidth for this sample, and assigns it to h0.
     *
     * @param X a <code>Vector</code> with the sample
     * @return h0 bandwith for Gaussian kernel
     */
    
    public double getGaussianH(Vector X){
        
        double average = 0 , variance = 0, sigma;
        
        // Average of X
        for (int i = 0 ; i < X.size() ; i++){
            average = average + ((Double)X.elementAt(i)).doubleValue();
        }//End of for
        average = (average/X.size());
        
        // Variance
        for (int i = 0 ; i < X.size() ; i++){
            variance = variance + (((Double)X.elementAt(i)).doubleValue()*((Double)X.elementAt(i)).doubleValue());
        }// End of for
        variance = variance/X.size();
        variance = variance-(average*average);
        
        sigma = Math.sqrt(variance);
        
        h0 = 1.059 * sigma * Math.pow(n, -(1/5.));
        return h0;
    }
    
    
    /*
     * Gets the current Gaussian bandwidth.
     *
     * @return h0 bandwidth for a gaussian with this sample
     */
    
    public double getGaussianH(){
        return h0;
    }
    
    
    /*
     * Calculates kernel density of a value of the sample. Uses boundary corrections.
     *
     * @param x a value of the sample
     * @return kernel density in that value
     */
    
    public double getValue(double x){
        //y is used to the kernel function and b to the kernel function with
        //boundary corrections
        double y = 0, b = 0;
        double ki = 0, bi = 0;
        
        //calcutes minimum and maximum of the sample
        double minimo = ((Double) sample.elementAt(0)).doubleValue();
        double aux1;
        for (int i = 1 ; i< n; i++ ){
            aux1 = ((Double) sample.elementAt(i)).doubleValue();
            if (aux1 < minimo ) minimo = aux1;
        }
        double maximo = ((Double) sample.elementAt(0)).doubleValue();
        double aux;
        for (int i = 1 ; i< n; i++ ){
            aux = ((Double) sample.elementAt(i)).doubleValue();
            if (aux > maximo ) maximo = aux;
        }
        
        //checks if all values are the same
        if(((Double)sample.elementAt(0)).doubleValue() == ((Double)sample.elementAt(n-1)).doubleValue()){
           // System.out.println("All values are the same, return only one value with kernel density equal to 1");
            return 1.;
        }
        
        for (int i = 0; i < n; i++){
            //for every value of the sample, removes xi and divides by h
            double u = (x - ((Double) sample.elementAt(i)).doubleValue())/h;
            
            //use a kernel function according to k
            switch(k){
                //if we use Gaussian kernel
                case GAUSSIAN:
                    ki = Math.pow(2*Math.PI, -0.5) * Math.pow(Math.E, -(u*u)/2);
                    break;
                    
                    //if we use Biweigth kernel
                case BIWEIGTH:
                    //all kernel, except for the gaussian, are zero outside the interval [-1,1]
                    if (Math.abs(u)<=1){
                        ki = (15/16.)*(1 - (u*u) )*(1 - (u*u) );
                        bi = ki;
                        //boundary corrections :
                        //correct left boundary
                        double p = (x-minimo)/h;
                        if ((p>=0)&&(p<1)){
                            double a0, a1, a2;
                            a0 = (15./16)*((8./15) + p - ((2./3)*Math.pow(p,3)) + (Math.pow(p,5)/5.));
                            a1 = (15./16)*(-(1/6.) + ((p*p)/2) - ((1/2.)*Math.pow(p,4)) + (Math.pow(p,6)/6.));
                            a2 = (15./16)*((8./105) + (Math.pow(p,3)/3.) - ((2/5.)*Math.pow(p,5)) + (Math.pow(p,7)/7.));
                            bi = ((a2 - (a1*u))*ki)/(a0*a2 - a1*a1);
                        }
                        //correct rigth boundary
                        p = (x-maximo)/h;
                        if ((p>(-1))&&(p<=0)){
                            double a0, a1, a2;
                            a0 = (15./16)*((8./15) - p + ((2./3)*Math.pow(p,3)) - (Math.pow(p,5)/5.));
                            a1 = (15./16)*((1/6.) - ((p*p)/2) + ((1/2.)*Math.pow(p,4)) - (Math.pow(p,6)/6.));
                            a2 = (15./16)*((8./105) - (Math.pow(p,3)/3.) + ((2/5.)*Math.pow(p,5)) - (Math.pow(p,7)/7.));
                            bi = ((a2 - (a1*u))*ki)/(a0*a2 - a1*a1);
                        }
                    }
                    else{
                     //   System.out.println("Para x="+x+" tengo u="+u+" ,h="+h+" y xi="+((Double) sample.elementAt(i)).doubleValue());    
                        ki = 0;
                        bi = 0;
                    }
                    break;
                    
                    //if we use Triweigth kernel
                case TRIWEIGTH:
                    //it is defined non zero only in the interval [-1, 1]
                    if (Math.abs(u)<=1){
                        ki = (35/32.)*(1 - (u*u) )*(1 - (u*u) )*(1 - (u*u) );
                        bi = ki;
                        //boundary corrections :
                        //correct left boundary
                        double p = (x-minimo)/h;
                        if ((p>=0)&&(p<1)){
                            double a0, a1, a2;
                            a0 = (35./32)*((16./35) + p - p*p*p + ((3./5)*Math.pow(p,5)) - (Math.pow(p,7)/7.));
                            a1 = (35./32)*(-(1/8.) + ((p*p)/2) - ((3/4.)*Math.pow(p,4)) + (Math.pow(p,6)/2.)-(Math.pow(p,8)/8.));
                            a2 = (35./32)*((16./315) + (Math.pow(p,3)/3.) - ((3/5.)*Math.pow(p,5)) + ((3*Math.pow(p,7))/7.) - (Math.pow(p,9)/9.) );
                            bi = ((a2 - (a1*u))*ki)/(a0*a2 - a1*a1);
                        }
                        //correct rigth boundary
                        p = (x-maximo)/h;
                        if ((p>(-1))&&(p<=0)){
                            double a0, a1, a2;
                            a0 = (35./32)*((16./35) - p + p*p*p - ((3./5)*Math.pow(p,5)) + (Math.pow(p,7)/7.));
                            a1 = (35./32)*((1/8.) - ((p*p)/2) + ((3/4.)*Math.pow(p,4)) - (Math.pow(p,6)/2.)+(Math.pow(p,8)/8.));
                            a2 = (35./32)*((16./315) - (Math.pow(p,3)/3.) + ((3/5.)*Math.pow(p,5)) - ((3*Math.pow(p,7))/7.) + (Math.pow(p,9)/9.) );
                            bi = ((a2 - (a1*u))*ki)/(a0*a2 - a1*a1);
                        }
                    }
                    else{
                        ki = 0;
                        bi = 0;
                    }
                    break;
                    
                    //if we use Epanechnikov kernel
                case EPANECHNIKOV:
                    //it is defined non zero only in the interval [-1, 1]
                    if (Math.abs(u)<=1){
                        ki = (3/4.)*(1 - (u*u) );
                        bi = ki;
                        //boundary corrections :
                        //correct left boundary
                        double p = (x-minimo)/h;
                        if ((p>=0)&&(p<1)){
                            double a0, a1, a2;
                            a0 = (3./4)*((2./3) + p - ((p*p*p)/3.));
                            a1 = (3./4)*(-(1/4.) + ((p*p)/2) - ((1/4.)*Math.pow(p,4)));
                            a2 = (3./4)*((2./15) + (Math.pow(p,3)/3.) - ((1/5.)*Math.pow(p,5)) );
                            bi = ((a2 - (a1*u))*ki)/(a0*a2 - a1*a1);
                        }
                        //correct rigth boundary
                        p = (x-maximo)/h;
                        if ((p>(-1))&&(p<=0)){
                            double a0, a1, a2;
                            a0 = (3./4)*((2./3) - p + ((p*p*p)/3.));
                            a1 = (3./4)*((1/4.) - ((p*p)/2) + ((1/4.)*Math.pow(p,4)));
                            a2 = (3./4)*((2./15) - (Math.pow(p,3)/3.) + ((1/5.)*Math.pow(p,5)) );
                            bi = ((a2 - (a1*u))*ki)/(a0*a2 - a1*a1);
                        }
                    }
                    else{
                        ki = 0;
                        bi = 0;
                    }
                    break;
                    
                    //if we use Cosinus kernel
                case COSINUS:
                    //it is defined non zero only in the interval [-1, 1]
                    if (Math.abs(u)<=1)
                        ki = (Math.PI/4) * Math.cos((Math.PI/2)*u) ;
                    else
                        ki = 0;
                    break;
                    
                    //if we use Triangular kernel
                case TRIANGULAR:
                    //it is defined non zero only in the interval [-1, 1]
                    if (Math.abs(u)<=1)
                        ki = (1 - Math.abs(u));
                    else
                        ki = 0;
                    break;
                default: System.out.println("Please, specify the kernel function");
            }
            y = y + ki;
            b = b + bi;
        }
        
        y = y/(n*h);
        b = b/(n*h);
        
        //we don't considere boundary corrections
        return y;
    }
    
    
    /*
     * Transforms a set of values of the sample in two vectors, one with
     * the values of the sample without repetitions, and another one
     * with its kernel densities.
     *
     * @param A a <code>Vector</code> of values of the sample
     * @return Two vectors with the values.
     */
    
    public Vector getValues(Vector A){
        
        //System.out.println("A tiene "+A.size()+" elementos");
        //remove repeated values of A
        ContinuousProbabilityTree t = new ContinuousProbabilityTree();
        //sort the vector
        t.sort(A);
        Vector x = new Vector();
        //x.add((Double) A.elementAt(0));
        //we suppose all values are different
        for (int i = 0; i < A.size(); i++ )
            x.add((Double) A.elementAt(i));
                
        double minimo = 0;
        Vector y = new Vector();
        double xi = 0, yi = 0;
        
        //store for each non-repeated values, this and its density
        for (int i = 0; i < x.size(); i++){
            xi = ((Double) x.elementAt(i)).doubleValue();
            yi = getValue(xi);
            
            y.insertElementAt(new Double(yi), i);
        }
        
        Vector result = new Vector();
        result.add(x);
        //System.out.println("x (salida) tiene "+x.size()+" elementos");
        result.add(y);
        //System.out.println("y (salida) tiene "+y.size()+" elementos");
        
        return result;
    }
    
    
    
    /*********************    Access methods      *************************
     *
     *
     *
     * /*
     * Sets the bandwidth.
     *
     * @param bandwith a double
     */
    
    public void setH(double bandwidth){
        h = bandwidth;
    }
    
    
    /*
     * Gets the bandwidth
     *
     * @return a double
     */
    
    public double getH(){
        return h;
    }
    
    
    /*
     * Sets the kernel method. Lets assign to h the optimal one for this method.
     *
     * @param kernelFunction an integer
     * @param assignOptimalH a boolean, it's true if you want to assign to h the optimal one for this method
     */
    
    public void setK(int kernelFunction, boolean assignOptimalH){
        k = kernelFunction;
        if (assignOptimalH)
            assignOptimalH();
        if (assignOptimalH && (k > 3))
            System.out.println("Optimal bandwidth can not be assigned");
    }
    
    
    /*
     * Gets the kind of kernel method.
     *
     * @return an integer which indicates the kernel method
     */
    
    public int getK(){
        return k;
    }
    
    
    /**
     * Sets the sample, assign H0 and sort the values.
     *
     * @param a <code>Vector>/code> with the sample
     */
    
    public void setSample(Vector X){
        sample = new Vector();
        ContinuousProbabilityTree auxtree = new ContinuousProbabilityTree();
        auxtree.sort(X);
        n = X.size();
        for (int i = 0; i < n; i++){
            sample.insertElementAt(X.elementAt(i), i);
        }
        getGaussianH(X);
    }
    
    
    /*
     * Gets the sample that I am considering.
     *
     * @return <code>Vector>/code> with the sample
     */
    
    public Vector getSample(){
        return sample;
    }
    
    
     /**
     * Method to carry experiments.
     * This method needs the kernel function we want to use. Generates a sample of a normal distribution
     * and fits it to an univariate MTE density. 
     *
     */
    
    public static void main(String args[]) throws ParseException, IOException, elvira.InvalidEditException {
        
        if(args.length < 1){
            System.out.println("too few arguments: Usage: kernelFunction(0:Gaussian, 1:Biweigth, 2:Triweigth, 3:Epanechnicov)");
            System.exit(0);
        }
        
        Vector values = new Vector();
        Vector varsVector = new Vector();
        Continuous x = new Continuous("x");
        varsVector.add(x);
        NodeList vars = new NodeList(varsVector);
        DataBaseCases cases;
        ContinuousCaseListMem sample = new ContinuousCaseListMem(vars);
        
        int kernel = Integer.valueOf(args[0]).intValue();
        Vector X = new Vector();
        //generando la muestra aleatoriamente
        SampleGenerator samplegenera = new SampleGenerator();
        for (int i = 0; i < 1000 ; i++ ){
            double value = samplegenera.randomNormal(0,1);
            X.add(new Double(value));
            values = new Vector();
            values.add(new Double(value));
            ContinuousConfiguration conf = new ContinuousConfiguration(varsVector, values);
            sample.put(conf);
            }
        cases = new DataBaseCases("Normal",vars, sample);
                 
        KernelDensity d = new KernelDensity(X, kernel);
        d.setH(d.getH());
        double xi;
        for (int j = 0; j < d.n; j ++ ){
            xi = ((Double) X.elementAt(j)).doubleValue();
            }
        
        //learns a MTE with this sample
        MTELearning ok = new MTELearning(cases);
        NodeList newX = new NodeList();
        ContinuousProbabilityTree T;
        T = new ContinuousProbabilityTree();
        int intervals = 3 , numpoints = 4;
        //without any parent
        NodeList parent = new NodeList();
        
        T = ok.learnConditional(x,parent,cases,intervals,numpoints);
        
        //System.out.println("The result is: ");
        //T.print();
                        
    }//end of main    
    
    
}//end Class KernelDensity
