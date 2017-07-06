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

public class NBayesMLEMMStart extends NBayesMLEM
{

  Bnet bestClassifier;
  
  public NBayesMLEMMStart(DataBaseCases cases, int numberOfClusters)
  {
    super(cases,numberOfClusters);
  }// end NBayesMLEMMStart(DataBaseCases,int)

  /**
   * Runs and returns the value given the learning method with the parameter 
   * by default (<code>LaplaceCorrection=true</code> and <code>N = 30</code>).
   */
  public double learning()
  {
    return learning(true,30);
  }
  
  /**
   * Learns <code>N</code> Naive-Bayes classifiers from the dataset specified in 
   * constructor method via EM algorithm where in each
   * iteration is performed a full averaging over all the possible structures and 
   * all the possible parameters for that structure. Then the best model of the 
   * <code>N</code> ones is stored as the learned model.
   * 
   * @param laplaceCorrection <code>true</code> to use the Laplace correction
   * when estimating the parameters, <code>false</code> otherwise.
   * @param N number of random starts for the EM algorithm.
   */
  public double learning(boolean laplaceCorrection,int N)
  {
    int i;
    double logL;
    double bestLogL = - Double.MAX_VALUE;

    for(i=0;i<N;i++)
    {
      logL = super.learning(laplaceCorrection);
      if(logL > bestLogL)
      {
        bestLogL       = logL;
        bestClassifier = classifier;
        //Set the classifier object to a new classifier in order not to modify 
        // the bestClassifier when we learn again a new classifier with the EM
        // algorithm
        super.newClassifier();
      }
    } 
    return bestLogL;
  }

   /**
   * Gets the best classifier (Bnet with Naive-Bayes structure) from the N learnings
   * with random initialization.
   * 
   * @return Bnet classifier.
   */
  public Bnet getClassifier()
  {
    return bestClassifier;
  }//end getClassifier()

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

    NBayesMLEMMStart nBayesMLEM = new NBayesMLEMMStart(db, numberOfClusters);
    System.out.println("EM por Maxima Verosimilitud");
    double loglikelihood;

    loglikelihood = nBayesMLEM.learning(true,30);
    Bnet classifier = nBayesMLEM.getClassifier();
    FileWriter fo = new FileWriter(new File(args[2]));
    classifier.saveBnet(fo);
    fo.close();
  }

}
