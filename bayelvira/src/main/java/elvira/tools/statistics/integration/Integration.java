/*
*   Class Integration
*       interface IntegralFunction also required
*
*   Contains the methods for Gaussian-Legendre quadrature,
*   the backward and forward rectangular rules
*   and the trapezium rule
*
*   The function to be integrated is supplied by means of
*       an interface, IntegralFunction
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:	 February 2002
*   UPDATE:  22 June 2003
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   Integration.html
*
*   Copyright (c) April 2004
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package elvira.tools.statistics.integration;

import java.util.*;

// Numerical integration class
public class Integration{

    	// Vectors to hold Gauss-Legendre Coefficients saving repeated calculation
    	private static Vector gaussQuadIndex = new Vector();
    	private static Vector gaussQuadDistVector = new Vector();
    	private static Vector gaussQuadWeightVector = new Vector();

    	private static int trapIntervals = -100;   // number of intervals in trapezium at which accuracy was satisfied

        public Integration(){
        }

    	// Numerical integration using n point Gaussian-Legendre quadrature
    	public static double gaussQuad(IntegralFunction g, double lowerLimit, double upperLimit, int nPoints){

        	double[] gaussQuadDist = new double[nPoints];
        	double[] gaussQuadWeight = new double[nPoints];
        	double sum=0.0D;
        	double xplus = 0.5D*(upperLimit + lowerLimit);
        	double xminus = 0.5D*(upperLimit - lowerLimit);
        	double dx = 0.0D;
        	boolean test = true;
        	int k=-1, kn=-1;

        	// Get Gauss-Legendre coefficients, i.e. the weights and scaled distances
        	// Check if coefficients have been already calculated on an earlier call
        	if(!gaussQuadIndex.isEmpty()){
            		for(k=0; k<gaussQuadIndex.size(); k++){
                		Integer ki = (Integer)gaussQuadIndex.elementAt(k);
                		if(ki.intValue()==nPoints){
                    			test=false;
                    			kn = k;
                		}
            		}
        	}

        	if(test){
            		// Calculate and store coefficients
            		gaussQuadCoeff(gaussQuadDist, gaussQuadWeight, nPoints);
            		gaussQuadIndex.addElement(new Integer(nPoints));
            		gaussQuadDistVector.addElement(gaussQuadDist);
            		gaussQuadWeightVector.addElement(gaussQuadWeight);
        	}
        	else{
        		// Recover coefficients
            		gaussQuadDist = (double[]) gaussQuadDistVector.elementAt(kn);
            		gaussQuadWeight = (double[]) gaussQuadWeightVector.elementAt(kn);
        	}

        	// Perform summation
        	for(int i=0; i<nPoints; i++){
            		dx = xminus*gaussQuadDist[i];
            		sum += gaussQuadWeight[i]*g.function(xplus+dx);
        	}
        	return sum*xminus;   // rescale and return
    	}

    	// Returns the distance (guassQuadDist) and weight coefficients (gaussQuadCoeff)
    	// for an n point Gauss-Legendre Quadrature.
    	// The Gauss-Legendre distances, gaussQuadDist, are scaled to -1 to 1
    	// See Numerical Recipes for details
    	public static void gaussQuadCoeff(double[] guassQuadDist, double[] guassQuadWeight, int n){

	    	double	z=0.0D, z1=0.0D;
		    double  pp=0.0D, p1=0.0D, p2=0.0D, p3=0.0D;

	    	double 	eps = 3e-11;	// set required precision
	    	double	x1 = -1.0D;		// lower limit
	    	double	x2 = 1.0D;		// upper limit

	    	//  Calculate roots
	    	// Roots are symmetrical - only half calculated
	    	int m  = (n+1)/2;
	    	double	xm = 0.5D*(x2+x1);
	    	double	xl = 0.5D*(x2-x1);

	    	// Loop for  each root
	    	for(int i=1; i<=m; i++){
			// Approximation of ith root
		    	z = Math.cos(Math.PI*(i-0.25D)/(n+0.5D));

		    	// Refinement on above using Newton's method
		    	do{
			    	p1 = 1.0D;
			    	p2 = 0.0D;

			    	// Legendre polynomial (p1, evaluated at z, p2 is polynomial of
			    	//  one order lower) recurrence relationsip
			    	for(int j=1; j<=n; j++){
				    	p3 = p2;
				    	p2 = p1;
				    	p1= ((2.0D*j - 1.0D)*z*p2 - (j - 1.0D)*p3)/j;
			    	}
			    	pp = n*(z*p1 - p2)/(z*z - 1.0D);    // Derivative of p1
			    	z1 = z;
			    	z = z1 - p1/pp;			            // Newton's method
		    	} while(Math.abs(z - z1) > eps);

		    	guassQuadDist[i-1] = xm - xl*z;		    // Scale root to desired interval
		    	guassQuadDist[n-i] = xm + xl*z;		    // Symmetric counterpart
		    	guassQuadWeight[i-1] = 2.0*xl/((1.0 - z*z)*pp*pp);	// Compute weight
		    	guassQuadWeight[n-i] = guassQuadWeight[i-1];		// Symmetric counterpart
	    	}
    	}

    	// Numerical integration using the trapeziodal rule
    	public static double trapezium(IntegralFunction g, double lowerLimit, double upperLimit, int nIntervals){
        	double 	y1 = 0.0D, sum = 0.0D;
        	double 	interval = (upperLimit - lowerLimit)/nIntervals;
        	double	x0 = lowerLimit;
        	double 	x1 = lowerLimit + interval;
        	double	y0 = g.function(x0);

		for(int i=0; i<nIntervals; i++){
            		y1 = g.function(x1);
            		sum += 0.5D*(y0+y1)*interval;
            		x0 = x1;
            		y0 = y1;
            		x1 += interval;
        	}
        	return sum;
    	}

    	// Numerical integration using an iteration on the number of intervals in the trapeziodal rule
    	// until two successive results differ by less than a predtermined accuracy times the penultimate result
    	public static double trapezium(IntegralFunction g, double lowerLimit, double upperLimit, double accuracy, int maxIntervals){

        	Integration.trapIntervals = -100;
        	double  summ = trapezium(g, lowerLimit, upperLimit, 1);
        	double oldSumm = summ;
        	int i = 0;
        	for(i=2; i<=maxIntervals; i++){
            		summ = trapezium(g, lowerLimit, upperLimit, i);
            		if(Math.abs(summ - oldSumm)<accuracy*Math.abs(oldSumm))break;
            		oldSumm = summ;
        	}

		    if(maxIntervals == 1 || i > maxIntervals){
            		System.out.println("accuracy criterion was not met in Integration.trapezium - summ was returned as result.");
            		Integration.trapIntervals = -200;
        	}
        	else{
            		Integration.trapIntervals = i;
        	}
        	return summ;
    	}

    	// Get the number of intervals at which accuracy was last met in trapezium
    	public static int getTrapIntervals(){
        	if(trapIntervals<0){
            		if(trapIntervals==-100){
                		System.out.println("trapIntervals has not been changed since last initialisation");
                		System.out.println("-100 returned");
            		}
            		else{
                		System.out.println("accuracy test was not satisfied in last call to trapezium");
                		System.out.println("-200 returned");
            		}
        	}
        	return trapIntervals;
    	}

    	// Reset trapIntervals to -100
    	public static void resetTrapIntervals(){
        	Integration.trapIntervals = -100;
    	}

    	// Numerical integration using the backward rectangular rule
    	public static double backward(IntegralFunction g, double lowerLimit, double upperLimit, int nIntervals){

        	double interval = (upperLimit - lowerLimit)/nIntervals;
        	double x = lowerLimit + interval;
        	double y = g.function(x);
        	double sum = 0.0D;

        	for(int i=0; i<nIntervals; i++){
            		y = g.function(x);
            		sum += y*interval;
            		x += interval;
        	}
        	return sum;
    	}

    	// Numerical integration using the foreward rectangular rule
    	public static double foreward(IntegralFunction g, double lowerLimit, double upperLimit, int nIntervals){

        	double interval = (upperLimit - lowerLimit)/nIntervals;
        	double x = lowerLimit;
        	double y = g.function(x);
        	double sum = 0.0D;

        	for(int i=0; i<nIntervals; i++){
            		y = g.function(x);
            		sum += y*interval;
            		x += interval;
        	}
        	return sum;
    	}
}
