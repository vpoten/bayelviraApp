/* UnivariateModelBN.java */

package elvira.learning;

import java.io.*;
import java.util.Vector;
import java.text.DecimalFormat;

/**
 * Implements basic methods for a univariate probabilistic methods.
 * It is specific for learning Bayesian networks represented as
 * bidimensional arrays.
 * The model is just a three-dimensional array with the first and 
 * second dimension accounting for the variables in the model, and the 
 * third indicates how indicating the number of variables, and the second is the max number
 * of values that a variable can take. So, you must be carefull with
 * possibly empty positions. 
 */

public class UnivariateModelBN {

/**
 * The model described above
 */
public double Model[][][];

/**
 * The number of variables in the network, so we will have numVariables^2
 * variables in the model
 */

public int numVariables;


public int numCases=2;

// constructor

/**
 * Empty constructor
 */

public UnivariateModelBN(){
  Model = null;
  numVariables = 0;
  numCases = 2;
}

/** 
 * constructing model
 */

public UnivariateModelBN(int nV){

  numVariables = nV;

  numCases = 2;

  Model = new double[numVariables][numVariables][numCases];

} // end constructor

/**
 * Initializes the model as a uniform distribution
 */

public void setUniform( ){
  int i,j,k;
   
  for(i=0;i<numVariables;i++)
    for(j=0;j<numVariables;j++)
      for(k=0;k<2;k++)
        Model[i][j][k] = 0.5;
  for(i=0;i<numVariables;i++){
    Model[i][i][0]=1.0;
    Model[i][i][1]=0.0; 
  }
}



public void setNoUniform(double v){
  int i,j,k;
   
  for(i=0;i<numVariables;i++)
    for(j=0;j<numVariables;j++){
      Model[i][j][0]=v;
      Model[i][j][1]=1.0 - v;
    }
  for(i=0;i<numVariables;i++){
    Model[i][i][0]=1.0;
    Model[i][i][1]=0.0; 
  }
}

/**
 * Initializes the model to the value passed as parameter
 */

public void setToValue(double value){
  int i,j,k;
 
  for(i=0;i<numVariables;i++)
    for(j=0;j<numVariables;j++)
      for(k=0;k<2;k++)
        Model[i][j][k] = value;
  
}

/**
 * update the univariate model, by using a new model and the Pbil rule
 */

public void updateWithPbilRule(UnivariateModelBN aux,double alpha){
  int i,j,k;

  for(i=0;i<this.numVariables;i++)
    for(j=0;j<this.numVariables;j++)
      for(k=0;k<2;k++)
        this.Model[i][j][k] = (1-alpha)*this.Model[i][j][k] 
                      + alpha*aux.Model[i][j][k];

} //end of updateWithPbilRule


/** 
 * Print model
 */

public void printModel(){
  int i,j,k;
  DecimalFormat df = new DecimalFormat("0.00");

  System.out.println("\n\nCurrent Model is: \n\n");
  for(i=0;i<this.numVariables;i++){
    for(j=0;j<this.numVariables;j++){
      System.out.print("  [" + df.format(this.Model[i][j][0]) 
              + "," + df.format(this.Model[i][j][1]) + "]");
    }//end for j
    System.out.println();
  }
  System.out.println("\n");
}// end print model

} // end of class
