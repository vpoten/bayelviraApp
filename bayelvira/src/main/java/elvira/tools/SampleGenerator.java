/* SampleGenerator.java */

package elvira.tools;
import java.util.Vector;
import java.lang.Math; 

import java.io.*;



/**
 * Implements some functions for generating samles from knwon distributions .
 *
 *
 * @author Rafael Rumi
 * @since 06/04/2004
 */

public class SampleGenerator {

/**
 * Vector where the sample will be stored
 */

Vector sample;

/**
 * Creates an empty sample.
 * 
 */

public SampleGenerator() {
  
    sample = new Vector();
  
}


/**
 * Simulates randomly a value from an exponential distribution 
 * with mean <code>mean</code>.
 * 
 * @param mean The mean of the distribution
 * @return A double.
 */ 

public double randomExponential(double mean){

    double a, b;
    
    return -mean*(Math.log(1-Math.random()));

}



/**
 * Simulates randomly a value from a normal distribution 
 * with mean <code>mean</code>, and standard deviation <code> sdev </code>.
 * 
 * @param mean The mean of the distribution
 * @param sdev The standard deviation of the distribution.
 * @return A double.
 */ 

public double randomNormal(double mean, double sdev){

    double u1, z;
    double exp2, exp1 = 0;
    boolean found = false;

    while(!found){
    exp1 = randomExponential(1);
    exp2 = randomExponential(1);

    if (exp2 >= ((exp1 - 1)*(exp1 - 1)/2))
	found = true;

    }// End of while

    u1 = Math.random();
    if(u1 >= 0.5)
	z = -exp1;
    else
	z = exp1;

    return (z*sdev)+mean;
}


/**
 * Simulates randomly a value from a poisson distribution 
 * with mean <code>mean</code>.
 * 
 * @param mean The mean of the distribution
 * @return A double.
 */ 

public int randomPoisson(double mean){

    double sum = 0;
    double u = 0.5;
    int i, resul;
    Vector vectorValues = new Vector();
    Vector vectorProbabilities = new Vector();

    // Now I must create the vectors according to the parameter mean. We will stop in the 10th value
    //System.out.println("We are in randomPoisson");
    for(i=0 ; i< 11 ; i++){
	vectorValues.addElement(new Integer(i));
	//System.out.println("We have added "+i+" as a possible value");
	if(i<10){ 
	    u = Math.exp(-mean)*Math.pow(mean,i)/factorial(i);
	    vectorProbabilities.addElement(new Double(u));
	    //System.out.println("Its value is "+u);
	    sum = sum + u;
	}else{// In the last one we insert the rest so that it is a probability distribution
	    vectorProbabilities.addElement(new Double(1-sum));
	    //System.out.println("Its value is "+(1-sum));
	}// End of else
    }// End of for

    resul = getRandomValue(vectorValues, vectorProbabilities);
    return resul;
    
}// End of method


    /**
     * A recursive method to compute the factorial operation.
     * @param a The number to get the operation.
     * 
     * @return A double.
     */

public double factorial(int a){

    int i, acu;
    
    if(a > 0)
	return a*factorial(a-1);
    else
	return 1;

}

/**
 * Gets randomly a value from a probability distribution.
 *
 * @param value The values of the distribution
 * @param prob The probabilities
 */ 

public int getRandomValue(Vector values, Vector prob){

  int i, val = 0;
  double probVal, aleaVal, acu;
  //Random alea = new Random();
  boolean found = false;

  if (values.size() != prob.size()){
    System.out.println("Error: Both vector MUST be the same size");
    System.exit(0);
  }
  else{
    i = 0;
    aleaVal = Math.random();
    System.out.println("This is the random number "+aleaVal);
    acu = 0;
    while((i<values.size()) & (!found)){

      probVal = ((Double)prob.elementAt(i)).doubleValue();
      acu = acu+probVal;
      //System.out.println("This is the acu number "+acu);
      if(aleaVal <= acu){
	val = ((Integer)values.elementAt(i)).intValue();
	//System.out.println("We will return  "+val);
	found = true;
      }// End of if
      i++;
    }// End of while
   
  }//End of else
  
 return val;
    
}// End of method

} // End of class
