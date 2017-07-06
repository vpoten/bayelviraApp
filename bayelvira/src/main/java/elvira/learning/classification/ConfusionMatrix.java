/* ConfusionMatrix.java */
package elvira.learning.classification;

import elvira.FiniteStates;
import java.util.Vector;
import java.io.Serializable;
import elvira.tools.statistics.math.Fmath;

/**
 * ConfusionMatrix.java
 * 
 * This class stores the confusion matrix used in the classification
 * 
 * @author Rosa Blanco. UPV
 * @author J.G. Castellano. fjgc@decsai.ugr.es
 * @version 0.1
 * @since 11/04/2003
 */

public class ConfusionMatrix  implements Serializable{
  
  /**
   * The confusion matrix
   */
  protected double[][] confusionMatrix;

  /**
   * The confusion matrix variances
   */
  protected double[][] confusionMatrixVariances;


  /**
   * The dimensionality of the matrix
   */
  protected int dimension;

  /**
   * The average error for a list of confusion matrixes
   */
  protected double error;

  /**
   * The variance of the average error for a list of confusion matrixes
   */
  protected double variance;

  /**
   * The number of cases used to build the confusion matrix
   */
  protected double cases;


  /**
   * Basic constructor
   */
  public ConfusionMatrix() {
      this.dimension=0;
      this.error=-1.0;
      this.variance=0.0;
      this.cases=0;
  }
  
  /**
   * Constructor
   * @param FiniteStates classVariable. The class variable.
   */
  public ConfusionMatrix(FiniteStates classVariable) {
    this.dimension = classVariable.getStates().size();
    this.error=-1.0;
    this.variance=0.0;
    this.cases=0;
    this.confusionMatrix = new double[this.dimension][this.dimension];
    this.confusionMatrixVariances= new double[this.dimension][this.dimension];
    for(int i= 0; i< this.dimension; i++)
	for(int j= 0; j< this.dimension; j++) {
         this.confusionMatrix[i][j] = 0.0;
         this.confusionMatrixVariances[i][j] = 0.0;
	}
  }

  /**
   * Constructor
   * @param ints classNumber. The munber of values of the class variable
   */
  public ConfusionMatrix(int classNumber) {
    this.dimension = classNumber;
    this.error=-1.0;
    this.variance=-1.0;
    this.cases=0;
    confusionMatrix = new double[this.dimension][this.dimension];
    this.confusionMatrixVariances= new double[this.dimension][this.dimension];
    for(int i= 0; i< this.dimension; i++)
	for(int j= 0; j< this.dimension; j++) {
	    this.confusionMatrix[i][j] = 0.0;
	    this.confusionMatrixVariances[i][j] = 0.0;
	}
  }

  /**
   * Add 1 in confusionMatrix in the appropiate position
   * @param int realClass. The identifier of the real class of the instance
   * @param int assignedClass. The identifier of the assigned class of the instance
   */
  public void actualize(int realClass, int assignedClass) {
    this.confusionMatrix[realClass][assignedClass] ++;
    this.cases++;
  }
  
  /**
   * Get the value of the corresponding position of the matrix
   * @param int realClass. The identifier of the real class of the instance
   * @param int assignedClass. The identifier of the assigned class of the instance
   */
  public double getValue(int realClass, int assignedClass) {
    return this.confusionMatrix[realClass][assignedClass];
  }

  /**
   * Set the <code>value<code\> in the coresponding position of the matrix
   * @param int realClass. The identifier of the real class of the instance
   * @param int assignedClass. The identifier of the assigned class of the instance
   */
  public void setValue(int realClass, int assignedClass, double value) {
    this.cases-=confusionMatrix[realClass][assignedClass];
    this.cases+=value;
    this.confusionMatrix[realClass][assignedClass] = value;
  }

  /**
   * Make the average of a set of confusionMatrix
   * @param Vector vConfusionMatrix. The container of the set of confusionMatrix
   *                                 to make the average
   */
  public void average(Vector vConfusionMatrix) {
      int m,i,j;
    int nMatrix = vConfusionMatrix.size();
    double errors[]= new double[nMatrix];


    //compute the average confusion matrix
    
    //compute the sum
    this.cases=0;
    for(m= 0; m< nMatrix; m++) {
      for(i= 0; i< this.dimension; i++)
	  for(j= 0; j< this.dimension; j++) {
	      this.confusionMatrix[i][j] = this.confusionMatrix[i][j] + ((ConfusionMatrix)vConfusionMatrix.elementAt(m)).getValue(i,j);
	      this.cases += ((ConfusionMatrix)vConfusionMatrix.elementAt(m)).getValue(i,j);
	  }//End for j

      //Store the error for each Confusion Matrix
      errors[m]=((ConfusionMatrix)vConfusionMatrix.elementAt(m)).getError();
    }//end for m
    //divide by the number of matrixes
    for(i= 0; i< this.dimension; i++)
      for(j= 0; j< this.dimension; j++)
        this.confusionMatrix[i][j] = (this.getValue(i,j)) / nMatrix;

    //compute the average confusion matrix variance   
    for(m= 0; m< nMatrix; m++) {
	for(i=0; i< this.dimension; i++)
	    for(j= 0; j< this.dimension; j++) {
		double aux = ((ConfusionMatrix)vConfusionMatrix.elementAt(m)).getValue(i,j) - this.confusionMatrix[i][j];
		aux *=aux;
		this.confusionMatrixVariances[i][j] +=aux;
	    }//end for j
    }//end for m
    //divide by the number of matrixes
    for(i= 0; i< this.dimension; i++)
	for(j= 0; j< this.dimension; j++) 
	  this.confusionMatrixVariances[i][j] = this.confusionMatrixVariances[i][j] / nMatrix;




    //compute the average error 
    this.error=0;
    for (m=0; m<nMatrix; m++) 
	this.error+=errors[m];
    this.error=this.error/nMatrix;



    
    //compute the average error variance
    this.variance=0;
    for (i=0; i<nMatrix; i++) 
	this.variance+=(errors[i]-this.error)*(errors[i]-this.error);
    this.variance=this.variance/(nMatrix);
    
  }//end average method


  /**
   * Return the stored confusion matrix
   * @return an array with the confusion matrix
   */
   public double [][] getMatrix() {
       return this.confusionMatrix;
   }//end getMatrix method 

  /**
   * Return the stored variance for the confusion matrix
   * @return an array with the variance of confusion matrix
   */
   public double [][] getMatrixVariances() {
       return this.confusionMatrixVariances;
   }//end getMatrix method 


  /**
   * Return the dimension of the confusion matrix
   * @return the dimension of the confusion matrix
   */
  public int getDimension() {
      return this.dimension;
  }//end getDimension method 


  /**
   * This method compute the error rate of the  Confusion Matrix 
   * @return the error rate (mispredicted clases / number of cases)
   */
  public double getError () {
      //look if the error is calculated using the average method
      if (this.error>=0) 
	  return this.error;

      //compute it in other case
      //      double cases=0;
      double mispredictedclasses=0;;
      int i,j;
      for (i=0;i<this.dimension;i++)
	  for (j=0;j<this.dimension;j++) {
	      if (i!=j) mispredictedclasses+=this.confusionMatrix[i][j];
	      //	      cases+=this.confusionMatrix[i][j];
	  }
      
      //return the error
      return (mispredictedclasses/this.cases);
  }//end getError method 

  /**
   * This method return variance of the error rate of the  Confusion Matrix 
   * @return the variance of the error 
   */
  public double getVariance () {
	  return this.variance;
  }

  /**
   * This method return the standard deviation of the error rate 
   * @return the standard deviation of the error 
   */
  public double getStandardDeviation () {
	  return Math.sqrt(this.variance);
  }


  /**
   * This method compute the standard error of the Confusion Matrix. 
   * @return the standard error (sqrt( (e*(1-E)) / n ) )
   */
  public double getStandardError () {
      //      double cases=0;
      double mispredictedclasses=0;
      double error;
      int i,j;

      //compute the error if it isn't computed before with the average method
      if (this.error<=0) {
	  for (i=0;i<this.dimension;i++)
	      for (j=0;j<this.dimension;j++) {
		  if (i!=j) mispredictedclasses+=this.confusionMatrix[i][j];
		  //	      cases+=this.confusionMatrix[i][j];
	      }
	  //compute the error
	  error=(mispredictedclasses/this.cases);
      }
      else error=this.error; 
	  
      
      //return the standar error
      return Math.sqrt( (error*(1-error))/(double)this.cases  );
  }//end getStandardError method 

  /**
   * This method compute the the degree of agreement between the measured value
   *  and the true value in the Confusion Matrix, expressed as percent of full scale.
   * @return the accuracy rate ( (predicted clases / number of cases)*100 )
   */
  public double getAccuracy () {
      return ( (1-getError()));
  }//end getError method 

  /**
   * This method return variance of the accuracy of the Confusion Matrix 
   * @return the variance of the accuracy
   */
  public double getAccuracyVariance () {
	  return (100*100*this.variance);
  }

  /**
   * This method return the standard deviation of the accuracy
   * @return the standard deviation of the accuracy
   */
  public double getAccuracyStandardDeviation () {
	  return Math.sqrt(getAccuracyVariance());
  }

  /**
   * This method compute the number of cases stored in the  Confusion Matrix 
   * @return the number of cases
   */
  public int getCases () {
      return (int)this.cases;
  }//end getCases method 


  /**
   * Print the confusion matrix in the command line
   */
  public void print() {
    System.out.println("Confusion Matrix");
    System.out.print("  real");
    for(int i= 0; i< this.dimension; i++) 
      System.out.print("       " + i);
    System.out.println();
    for(int i= 0; i< this.dimension; i++) 
      System.out.print("--------------");
    System.out.println();

    System.out.println("assigned |");
    for(int j= 0; j< this.dimension; j++) {
      System.out.print(j + "        | ");
      for(int i= 0; i< this.dimension; i++)
        System.out.print(this.getValue(i,j) + "     ");
      System.out.println();
    }
   System.out.println();    

  }

  /**
   * Print the confusion matrix variance in the command line
   */
  public void printVariance() {
    System.out.println("Confusion Matrix Variance");
    System.out.print("  real");
    for(int i= 0; i< this.dimension; i++) 
      System.out.print("       " + i);
    System.out.println();
    for(int i= 0; i< this.dimension; i++) 
      System.out.print("--------------");
    System.out.println();

    System.out.println("assigned |");
    for(int j= 0; j< this.dimension; j++) {
      System.out.print(j + "        | ");
      for(int i= 0; i< this.dimension; i++)
        System.out.print(Fmath.truncate(this.confusionMatrixVariances[i][j],2) + "     ");
      System.out.println();
    }
  }
}//End of class
