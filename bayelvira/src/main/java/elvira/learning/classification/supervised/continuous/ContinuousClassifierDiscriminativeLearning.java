package elvira.learning.classification.supervised.continuous;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Link;
import elvira.LinkList;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.database.DataBaseCases;
import elvira.potential.PotentialTable;
import elvira.tools.Jama.LUDecomposition;
import elvira.tools.Jama.Matrix;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * This is a new abstract class. It was designed for the
 * discriminative learning of the continuous classifiers.
 *
 * @author Ildikó Flesch
 * @version 1.0
 * @since 1/06/2007
 */

public abstract class ContinuousClassifierDiscriminativeLearning extends ContinuousClassifier 
{ 
   
   /**
    * if verbose == true -> print information in the screen
    */
   protected boolean verbose;
   
   /**
   * Constructor
   * @param DataBaseCases cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public ContinuousClassifierDiscriminativeLearning()
  {
    super();
    verbose = false;
  }

  /**
   * Constructor
   * @param DataBaseCases cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public ContinuousClassifierDiscriminativeLearning(DataBaseCases data, boolean lap) throws elvira.InvalidEditException
  {
    super(data,lap);
    verbose = false;
  }
  
  
  /**
   * Set the verbose flag on, print information in the standar output
   */
  public void setVerboseOn()
  {
    this.verbose = true;
  }//setVerboseOn()
  
  /**
   * Set the verbose flag off, do not print information in the standar output
   */
  public void setVerboseOff()
  {
    this.verbose = false;
  }  
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
  }
}