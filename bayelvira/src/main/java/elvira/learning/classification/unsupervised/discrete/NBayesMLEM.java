/* NBayesMLEM*/
package elvira.learning.classification.unsupervised.discrete;

import java.io.*;
import java.util.*;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.potential.*;
import elvira.learning.preprocessing.*;
import elvira.parser.ParseException;
import elvira.learning.classification.AuxiliarPotentialTable;


/**
 * Implements a unsupervised clasification for a discrete variables dataset 
 * via EM algorithm, taking a Naive Bayes structure:
 * @author Guzmán Santafé
 * @version 0.1
 * @since 05/03/2003
 */
public class NBayesMLEM extends UnsupervisedNBayes
{
 
  /**
  *  Create a new NBayesMLEM Objet. This objet implements a classifier 
  *  (Bnet with a Naive-Bayes structure) in order to perform a clustering
  *  of a dataset.
  *  
  *  @param cases DataBasesCases object which contains the dataset stimate the classifier
  *  @param numberOfClusters number of clusters that we want to classify the dataset into
  *  
  */  
  public NBayesMLEM(DataBaseCases cases, int numberOfClusters)
  {
    super(cases,numberOfClusters);
  }// end NBayesMLEM(DataBaseCases,int)

  
  /**
   * Learn the conditional probability tables for the Bnet Naive-Bayes classifier
   * from the dataset specified in constructor method via EM algorithm. Use the 
   * Lapplace Correction by default.
   */
  public double learning()
  {
    return learning(true);
  }

  /**
   * Learn the conditional probability tables for the Bnet Naive-Bayes classifier
   * from the dataset specified in constructor method via EM algorithm
   * @param laplaceCorrection if <code>laplaceCorrection</code> is <code>true</code> 
   * the laplace correction is used when stimating the conditional probabily tables.
   */
  public double learning(boolean laplaceCorrection)
  {

    int i;
    int j;
    int k;

    // Constant for stopping criterion of the EM algorithm: we run the algorithm 
    // until the difference of the loglikelihood in two consecutive steps is 
    // bigger than EPSILON
    final double EPSILON  = 0.1;
    
    double      l1,l2; // store the loglikelihood values before and after an EM iteration.

    
    // Nijk: Sum of the probability of the cases in the dataset which i-st variable takes its k-st value 
    // and it has been classified in the j-st cluster
    // These are the parameters of the EM algorithm.
    Vector Nijk = new Vector();//each element of the vector will be a AuxiliarPotentialTable
    

    // store the parameters for the naive-Bayes model. These parameters are the same
    // than Nijk parameters. We keep this copy of the same parameters in order to 
    // improve the calculation of the new parameters in each step of the EM algorithm,
    // bthus we are able to speed up some work of the M-step doing those 
    // calculations in the E-step (otherwise we would need to go through the whole database
    // again)
    Vector theta_ijk = new Vector();//each element of the vector will be a AuxiliarPotentialTable

    // Auxiliar 2-dimensional array used in the EM algorithm
    // z[i][j] <- Probability of the i-th case to be classifyed into the j-th cluster
    double [][] z                       = new double [cases.size()][numberOfClusters];


    // Iterator elements for a more efficient sequential access over vector structures.
    // Iterator sequentialCases;
    Iterator sequentialNodes;
    Iterator sequentialCases;
    Iterator sequentialTheta_ijk;
    Iterator sequentialNijk;

    /***********************************************************************************
      1.Get the initial patameters (Theta_0 -> conditional probability tables) randomly
    ***********************************************************************************/

    // Initialize all the variables
    int                    nStatesOfVariable;
    FiniteStates           variable;
    AuxiliarPotentialTable auxiliarPotentialTable;

    
    sequentialNodes = nodes.iterator();

    for(i=0;i<numberOfVariables;i++)
    {
      variable          = (FiniteStates)sequentialNodes.next();
      nStatesOfVariable = variable.getStates().size();

      Nijk.addElement(new AuxiliarPotentialTable(variable));

      // The parameters are set to random values initially
      auxiliarPotentialTable = new AuxiliarPotentialTable(variable);
      auxiliarPotentialTable.setRandomTable();
      theta_ijk.addElement(auxiliarPotentialTable);      
    }

    // Add the variables for the classnode as the last ones in the vectors. Note
    // that in a naive-Bayes structure the class node never has parents, this
    // is a special variable and we will need to deal with it in a special way.
    Nijk.addElement(new AuxiliarPotentialTable(numberOfClusters,1));
  
    // The parameters are set to random values initially
    auxiliarPotentialTable = new AuxiliarPotentialTable(numberOfClusters,1);
    auxiliarPotentialTable.setEqualProbabilityTable();
    theta_ijk.addElement(auxiliarPotentialTable);
    
    /***********************************************************************************
      2.Calculate the likelihood of the data with Theta_0
    ***********************************************************************************/
    l1 = logLikelihood(theta_ijk);
    l2 = l1;

    /***********************************************************************************
      3. E Step (EM algorithm)
    ***********************************************************************************/
    int    [] dataCase;
    double    sum;
    boolean   stop           = false;
 
    // Auxiliar varialbes to store values used in other operations
    AuxiliarPotentialTable aux1;
    AuxiliarPotentialTable aux2;
    
///***********************************************
    int iteration = 0;
///**********************************************
    while(!stop)
    {
///***********************************************
      iteration ++;
///***********************************************

      // Initialize to 0 the Nijk values
      sequentialNijk = Nijk.iterator();
      for(i=0;i<numberOfVariables;i++)
      {
        ((AuxiliarPotentialTable)sequentialNijk.next()).initialize(0);
      }
      
      // Calculate the z values: z[i][j] <- Probability of the i-th case to be classifyed into the j-th cluster
      sequentialCases = cases.iterator();// Use dataSet (Iterator Class) for a better sequential access.
      for(i=0;sequentialCases.hasNext();i++)
      {
        dataCase = (int []) sequentialCases.next();
        sum      = 0;
        for(j=0;j<numberOfClusters;j++)
        {
          //zSum[j] = 0;
          z[i][j] = 1;
          sequentialTheta_ijk = theta_ijk.iterator();

          for(k=0;k<numberOfVariables;k++)
          {
            // P(dataCase|cluster=j) with a naive-Bayes factorization
            z[i][j] *= ((AuxiliarPotentialTable)sequentialTheta_ijk.next()).getPotential(dataCase[k],j); 
          }//end for_k
          //P(cluster = j) * P(dataCase|cluster=j)
          z[i][j] *= ((AuxiliarPotentialTable)sequentialTheta_ijk.next()).getPotential(j,0); 
          sum += z[i][j];
        }//end for_j

        // normalize the z values such that for all i sum(j=1 up to numberOfClusters) z[i][j]=1
        for(j=0;j<numberOfClusters;j++)
        {
          z[i][j]  = z[i][j] / sum;
          sequentialNijk = Nijk.iterator();          
          for(k=0;k<numberOfVariables;k++)
            {
              ((AuxiliarPotentialTable)sequentialNijk.next()).addCase(dataCase[k],j,z[i][j]);
            }            
        }//end for_j

      }//end for_i

      // The values of Nijk for the classnode (last element in Nijk)
      // are already calculated inside every Nijk AuxiliarPotentialTable structure,
      // in the denominator of every variable.
      aux1 = (AuxiliarPotentialTable)Nijk.lastElement();
      aux1.initialize(0);
      aux2 = (AuxiliarPotentialTable)Nijk.firstElement();
      for(i=0;i<numberOfClusters;i++)
      {
        aux1.addCase(i,0,aux2.getDenominator(i));
      }//end for_i
      

     /***********************************************************************************
        4. M Step (EM algorithm)
      ***********************************************************************************/
      // Initialize the iterators
      sequentialNodes            = nodes.iterator();
      sequentialNijk             = Nijk.iterator();
      sequentialTheta_ijk        = theta_ijk.iterator();

      // auxiliar variables to do calculations.
      AuxiliarPotentialTable auxTheta_ijk;
      AuxiliarPotentialTable auxNijk;


      // Copy the new parameters (Nijk) estimated before (in E-step) into the 
      // theta_ijk structure.
      for(i=0;i<numberOfVariables;i++)
      {
        // Initialize the auxiliar variables
        auxTheta_ijk      = (AuxiliarPotentialTable)sequentialTheta_ijk.next();
        auxNijk           = (AuxiliarPotentialTable)sequentialNijk.next();

        auxTheta_ijk.copyFromObject(auxNijk);

        if(laplaceCorrection)
        {
          auxTheta_ijk.applyLaplaceCorrection();
        }
      }
    // Copy the parameters for classnode
    auxNijk      = (AuxiliarPotentialTable)sequentialNijk.next();
    auxTheta_ijk = (AuxiliarPotentialTable)sequentialTheta_ijk.next(); 
    
    auxTheta_ijk.copyFromObject(auxNijk);
    if(laplaceCorrection)
    {
      auxTheta_ijk.applyLaplaceCorrection();
    }

     /***********************************************************************************
        5. Calculate the likelihood of the data with the former Theta calculated
           in E and M steps
      ***********************************************************************************/
      l2 = logLikelihood(theta_ijk); 

       if(l2 - l1 < EPSILON)
      {
        stop = true;
      }
      else
      {
        l1 = l2;
      }
    
    } //end while


    // save the learned classifier into a BNet
    Iterator relationListIterator           ;
    Iterator auxiliarPotentialTablesIterator;
  
    relationListIterator            = classifier.getRelationList().iterator();
    auxiliarPotentialTablesIterator = theta_ijk.iterator();

    Relation               relation;
    PotentialTable         potentialTable;
    
    for(i=0;relationListIterator.hasNext();i++)
    { 
      relation               = (Relation)relationListIterator.next();
      auxiliarPotentialTable = (AuxiliarPotentialTable)auxiliarPotentialTablesIterator.next();
      potentialTable         = (PotentialTable)relation.getValues();

      potentialTable.setValues(auxiliarPotentialTable.getPotentialTableCases());
    }

    this.score = l2;
    return l2;
  } //end learning()

  /**
   * implements the loolikelihood score: Probability of the dataset given the 
   * Naive-Bayes classifier (Bnet).
   * @param auxiliarPotentialTables a vector containig the conditional probability
   * tables (into AuxiliarPotentialTable objects). Each element of the vector is
   * the probability conditional table for a specific variable, beeing the last
   * one for the class variable.
   * @return the loglikelihood value.
   **/
 private double logLikelihood(Vector auxiliarPotentialTables)
  {
    // Iterators
    int i;
    int j;
    int k;

    double  logLikelihood;     // logLikelihood of the cases
    double  clusterLikelihood; // joint likelihood of a case over every possible cluster
    double  caseLikelihood;    // likelihood of a case for a specific cluster
    int []  dataCase;

    AuxiliarPotentialTable CPT; // Auxiliar variable to get access to the Conditional 
                                // Probability Table for a specific variable

    double [] CCPT;             // Auxiliar variable to get access to the Cluster
                                // Probability Table
    CCPT = ((AuxiliarPotentialTable)auxiliarPotentialTables.lastElement()).getPotentialTableCases();                        
    
    logLikelihood = 0;
    // Calsulate the logLikelihood from the whole dataSet (cases)
    for(i=0;i<cases.size();i++)
    {
      dataCase = (int []) cases.elementAt(i);
      // Calculate the likelihood of a case over every possible cluster
      clusterLikelihood = 0;      
      for(j=0;j<numberOfClusters;j++)
      {
        // Calculate the likelihood of a case in the j-th cluster
        caseLikelihood = 1;
        for(k=0;k<dataCase.length;k++)
        {
          CPT = (AuxiliarPotentialTable)auxiliarPotentialTables.elementAt(k);
          caseLikelihood *= CPT.getPotential(dataCase[k],j);
        }
        clusterLikelihood += CCPT[j] * caseLikelihood;
      }
      logLikelihood += Math.log(clusterLikelihood);
    }
    return logLikelihood;
  } // end logLikelihood
  
  /**
   * main method used to test the class.
   */
  public static void main(String[] args) throws ParseException, IOException, InvalidEditException
  {

    // Check for correct number of
    if(args.length < 3){
      System.out.println("Too few arguments: Usage: filein.dbc numberOfClusters fileout.elv ");
      System.exit(0);
    }
    FileInputStream f = new FileInputStream(args[0]);
    DataBaseCases db = new DataBaseCases(f);
    f.close();

    int numberOfClusters = (new Integer(args[1])).intValue();

    NBayesMLEM nBayesMLEM = new NBayesMLEM(db, numberOfClusters);
    System.out.println("EM por Maxima Verosimilitud");
    double loglikelihood;

    loglikelihood = nBayesMLEM.learning(true);
    Bnet classifier = nBayesMLEM.getClassifier();
    FileWriter fo = new FileWriter(new File(args[2]));
    classifier.saveBnet(fo);    
    fo.close();
  }
}
