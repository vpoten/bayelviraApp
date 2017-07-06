/* SamplingOnIdStatistics.java */

package elvira.tools;

import java.io.*;
import java.util.Vector;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialTable;
import elvira.inference.approximate.SamplingOnId;

/**
 * Class <code>SamplingOnIdStatistics.java</code>.
 * Contains some interesting information in order to evaluate
 * the evaluation of IDs with simulation
 *
 * @since 20/10/2003
 */

public class SamplingOnIdStatistics extends PropagationStatistics{

/**
 * Number of phases
 */

int phases;

/**
 * To control the number of decisions
 */

private int decisions;

/**
 * Number of samples used in each phase. Each position
 * will be a vector to consider the number of samples
 * used for every decision
 */

Vector numberOfForwardSamples;
Vector numberOfReconSamples;

/**
 * Times of computation for each phase
 */

Vector forwardComputationTimes;
Vector reconComputationTimes;

/**
 * Sizes for every decision table
 */

Vector decisionTableSizes;

/**
 * Number of branches with optimal policy for every decision
 */

Vector decisionOptimalPolicyBranches;

/**
 * Vector with the number of unconstrained branches for every decision
 */

Vector unconstrainedBranches;

/**
 * Vector of matches for every phase and for every decision. Each
 * position of this vector will be a vector, containing the matches
 * for every decision. Will have vectors to analyze the matches
 * for max and samp classifiers
 */

Vector maxMatchesForForwardStageWithOptimality;
Vector maxMatchesForReconStageWithOptimality;
Vector maxMatchesForForwardStageWithoutOptimality;
Vector maxMatchesForReconStageWithoutOptimality;
Vector sampMatchesForForwardStageWithOptimality;
Vector sampMatchesForReconStageWithOptimality;
Vector sampMatchesForForwardStageWithoutOptimality;
Vector sampMatchesForReconStageWithoutOptimality;

/**
 * Vector of expected utilities difference (for maximization classifiers)
 * Each position of this vector will be a vector,
 * with a position for every decision
 */

Vector maxDifferencesForForwardStageWithOptimality;
Vector maxDifferencesForReconStageWithOptimality;
Vector maxDifferencesForForwardStageWithoutOptimality;
Vector maxDifferencesForReconStageWithoutOptimality;

/**
 * Vector of expected utilities difference (for sampling classifiers)
 * Each position of this vector will be a vector,
 * with a position for every decision
 */

Vector samplingDifferencesForForwardStageWithOptimality;
Vector samplingDifferencesForReconStageWithOptimality;
Vector samplingDifferencesForForwardStageWithoutOptimality;
Vector samplingDifferencesForReconStageWithoutOptimality;

/**
 * Vector to contain the Kullback-Leibler distances between
 * the classification tress coming from approx and exact
 * results, after forward stages. The vector will contains
 * positions for every phase. The vector for every phase will
 * contain a vector, with a element for every decision. The
 * position for each decision will be a vector, with 4 measures
 */

Vector distancesForForwardStage;
Vector distancesForReconStage;

/**
 * Constructor.
 */

public SamplingOnIdStatistics(int phases) {

  super();

  // Initialize the data members 

  this.phases=phases;
  forwardComputationTimes=new Vector();
  //forwardComputationTimes.setSize(phases);
  reconComputationTimes=new Vector();
  //reconComputationTimes.setSize(phases);

  // Create and set the size for the vector containing the number
  // of samples used during forward stage

  numberOfForwardSamples=new Vector();
  //numberOfForwardSamples.setSize(phases);
  numberOfReconSamples=new Vector();
  //numberOfReconSamples.setSize(phases);

  // Create the vectors for the distances
  
  distancesForForwardStage=new Vector();
  //distancesForForwardStage.setSize(phases);
  distancesForReconStage=new Vector();
  //distancesForReconStage.setSize(phases);

  // Create the vector for differences related to maximization 
  // classifiers

  maxDifferencesForForwardStageWithOptimality=new Vector();
  //maxDifferencesForForwardStageWithOptimality.setSize(phases);
  maxDifferencesForReconStageWithOptimality=new Vector();
  //maxDifferencesForReconStageWithOptimality.setSize(phases);
  maxDifferencesForForwardStageWithoutOptimality=new Vector();
  //maxDifferencesForForwardStageWithoutOptimality.setSize(phases);
  maxDifferencesForReconStageWithoutOptimality=new Vector();
  //maxDifferencesForReconStageWithoutOptimality.setSize(phases);

  // Create the vector for differences related to sampling 
  // classifiers

  samplingDifferencesForForwardStageWithOptimality=new Vector();
  //samplingDifferencesForForwardStageWithOptimality.setSize(phases);
  samplingDifferencesForReconStageWithOptimality=new Vector();
  //samplingDifferencesForReconStageWithOptimality.setSize(phases);
  samplingDifferencesForForwardStageWithoutOptimality=new Vector();
  //samplingDifferencesForForwardStageWithoutOptimality.setSize(phases);
  samplingDifferencesForReconStageWithoutOptimality=new Vector();
  //samplingDifferencesForReconStageWithoutOptimality.setSize(phases);

  // Create the vector for matches 

  maxMatchesForForwardStageWithOptimality=new Vector();
  //maxMatchesForForwardStageWithOptimality.setSize(phases);
  maxMatchesForReconStageWithOptimality=new Vector();
  //maxMatchesForReconStageWithOptimality.setSize(phases);
  maxMatchesForForwardStageWithoutOptimality=new Vector();
  //maxMatchesForForwardStageWithoutOptimality.setSize(phases);
  maxMatchesForReconStageWithoutOptimality=new Vector();
  //maxMatchesForReconStageWithoutOptimality.setSize(phases);
  sampMatchesForForwardStageWithOptimality=new Vector();
  //sampMatchesForForwardStageWithOptimality.setSize(phases);
  sampMatchesForReconStageWithOptimality=new Vector();
  //sampMatchesForReconStageWithOptimality.setSize(phases);
  sampMatchesForForwardStageWithoutOptimality=new Vector();
  //sampMatchesForForwardStageWithoutOptimality.setSize(phases);
  sampMatchesForReconStageWithoutOptimality=new Vector();
  //sampMatchesForReconStageWithoutOptimality.setSize(phases);

  // Set decisions to 0

  decisions=0;
}


/**
 * Method to set the number of phases used
 *param <code>int</code> number of phases
 */

public void setNumberOfPhases(int phases){
  this.phases=phases;

  // Set the size for the rest of vectors
}

/**
 * Method to set the number of samples considered
 * for a given phase
 * @param <code>Vector</code> number of samples for every decision
 * @param phase
 * @param stage
 */
public void setSamples(Vector samples, int phase, int stage){
  if (stage == SamplingOnId.FORWARDSTAGE)
     //numberOfForwardSamples.setElementAt(samples,phase);
     numberOfForwardSamples.addElement(samples);
   else
     //numberOfReconSamples.setElementAt(samples,phase);
     numberOfReconSamples.addElement(samples);
}

/**
 * Method to set the time of computation required for every
 * phase
 * @param time of computation
 * @param phase
 * @param stage
 */

public void setTime(double time, int phase, int stage){
  if (stage == SamplingOnId.FORWARDSTAGE){
     //forwardComputationTimes.setElementAt(new Double(time),phase);
     forwardComputationTimes.addElement(new Double(time));
  }
   else{
     //reconComputationTimes.setElementAt(new Double(time),phase); 
     reconComputationTimes.addElement(new Double(time)); 
   }
}

/**
 * Method to set the global size for a decision
 * @param <code>Vector</code> sizes for decision tables
 */

public void setDecisionTableSizes(Vector sizes){
  decisionTableSizes=sizes;
  decisions=sizes.size();
}

/**
 * Method to store the number of branches related to optimal policy
 * @param <code>Vector</code> number of branches with optimal policy 
 *                            for every decision
 */

public void setDecisionOptimalPolicyBranches(Vector branches){
  decisionOptimalPolicyBranches=branches;
}

/**
 * Method to set the number of unscontrained configurations for
 * every decision table
 * @param sizes vector with the number of unsconstrained configurations
 *              for every decision table
 */

public void setUnconstrainedBranches(Vector sizes){
  unconstrainedBranches=sizes;
}

/**
 * Method to store the number of matches for each phase (with optimality)
 * @param matches number of matches for the pase
 * @param phase
 * @param flag to show if max or samp classifiers were used
 * @param stage
 */

public void setMatchesWithOptimality(Vector matches, int phase, int flag, int stage){
  // Retrieve the vector related to the phase, and into it, the vector
  // containing the matches for every decision

  if (flag == SamplingOnId.MAXIMAZING){	  
    if (stage == SamplingOnId.FORWARDSTAGE)
      //maxMatchesForForwardStageWithOptimality.setElementAt(matches,phase);
      maxMatchesForForwardStageWithOptimality.addElement(matches);
     else
      //maxMatchesForReconStageWithOptimality.setElementAt(matches,phase);
      maxMatchesForReconStageWithOptimality.addElement(matches);
  }
  else{
    if (stage == SamplingOnId.FORWARDSTAGE)
      //sampMatchesForForwardStageWithOptimality.setElementAt(matches,phase);
      sampMatchesForForwardStageWithOptimality.addElement(matches);
     else
      //sampMatchesForReconStageWithOptimality.setElementAt(matches,phase);
      sampMatchesForReconStageWithOptimality.addElement(matches);
  }
}

/**
 * Method to store the number of matches for each phase (without optimality)
 * @param matches number of matches for the pase
 * @param phase
 * @param flag to show if max or samp classifiers were used
 * @param stage
 */

public void setMatchesWithoutOptimality(Vector matches, int phase, int flag, int stage){
  // Retrieve the vector related to the phase, and into it, the vector
  // containing the matches for every decision

  if (flag == SamplingOnId.MAXIMAZING){	  
    if (stage == SamplingOnId.FORWARDSTAGE)
      //maxMatchesForForwardStageWithoutOptimality.setElementAt(matches,phase);
      maxMatchesForForwardStageWithoutOptimality.addElement(matches);
     else
      //maxMatchesForReconStageWithoutOptimality.setElementAt(matches,phase);
      maxMatchesForReconStageWithoutOptimality.addElement(matches);
  }
  else{
    if (stage == SamplingOnId.FORWARDSTAGE)
      //sampMatchesForForwardStageWithoutOptimality.setElementAt(matches,phase);
      sampMatchesForForwardStageWithoutOptimality.addElement(matches);
     else
      //sampMatchesForReconStageWithoutOptimality.setElementAt(matches,phase);
      sampMatchesForReconStageWithoutOptimality.addElement(matches);
  }
}

/**
 * Method to store the differences of utility between exact policy
 * and the results of the simulation (maximization) (with optimality)
 * @param diff set of differences for every decision
 * @param phase 
 * @param flag to show if the measures are related to max and samp classif
 * @param stage  
 */

public void setDifferencesWithOptimality(Vector diff, int phase, int flag, int stage){
  if (flag == SamplingOnId.MAXIMAZING){
    if (stage == SamplingOnId.FORWARDSTAGE){
      //maxDifferencesForForwardStageWithOptimality.setElementAt(diff,phase);
      maxDifferencesForForwardStageWithOptimality.addElement(diff);
    }
    else{
      //maxDifferencesForReconStageWithOptimality.setElementAt(diff,phase);
      maxDifferencesForReconStageWithOptimality.addElement(diff);
    }
  }
  else{
    if (stage == SamplingOnId.FORWARDSTAGE){
      //samplingDifferencesForForwardStageWithOptimality.setElementAt(diff,phase);
      samplingDifferencesForForwardStageWithOptimality.addElement(diff);
    }
    else{
      //samplingDifferencesForReconStageWithOptimality.setElementAt(diff,phase);
      samplingDifferencesForReconStageWithOptimality.addElement(diff);
    }
  }
}

/**
 * Method to store the differences of utility between exact policy
 * and the results of the simulation (maximization) (without optimality)
 * @param diff set of differences for every decision
 * @param phase 
 * @param flag to show if the measures are related to max and samp classif
 * @param stage  
 */

public void setDifferencesWithoutOptimality(Vector diff, int phase, int flag, int stage){
  if (flag == SamplingOnId.MAXIMAZING){
    if (stage == SamplingOnId.FORWARDSTAGE){
      //maxDifferencesForForwardStageWithoutOptimality.setElementAt(diff,phase);
      maxDifferencesForForwardStageWithoutOptimality.addElement(diff);
    }
    else{
      //maxDifferencesForReconStageWithoutOptimality.setElementAt(diff,phase);
      maxDifferencesForReconStageWithoutOptimality.addElement(diff);
    }
  }
  else{
    if (stage == SamplingOnId.FORWARDSTAGE){
      //samplingDifferencesForForwardStageWithoutOptimality.setElementAt(diff,phase);
      samplingDifferencesForForwardStageWithoutOptimality.addElement(diff);
    }
    else{
      //samplingDifferencesForReconStageWithoutOptimality.setElementAt(diff,phase);
      samplingDifferencesForReconStageWithoutOptimality.addElement(diff);
    }
  }
}

/**
 * Method to set the distances of Killback-Leibler related to
 * every decision table and the approx classification tree for it
 * @param <code>dist</code> distances for every decision
 * @param phase
 * @param stage
 */

public void setDistances(Vector dist, int phase, int stage){
  if (stage == SamplingOnId.FORWARDSTAGE)
    //distancesForForwardStage.setElementAt(dist,phase);
    distancesForForwardStage.addElement(dist);
   else
    //distancesForReconStage.setElementAt(dist,phase);
    distancesForReconStage.addElement(dist);
}


/**
 * Access methods
 */

/**
 * Method to print all the information
 */

public void printInformation() throws IOException{
 int i,j;
 File file;
 FileWriter f;
 PrintWriter p;
 
  // Create a directory with the data

  file=new File(fileName);
  file.mkdir();

  // Create a new FileWriter object

  f=new FileWriter("data");

  // Create a new PrintWriter object

  p=new PrintWriter(f);

  // First at all, print the global information

  printPrettyGlobalInformation(p);
  
  // Print particular information for every phase
  
  printPrettyPhases(p);
  
  // Close the descriptor

  f.close();
}  

/**
 * Method to print the global information about the simulation
 * @param p PrintWriter to use for the output
 */

public void printPrettyGlobalInformation(PrintWriter p){
   double value;
   double optimal;
   double unconstrained;
   
   // Print the number of phases
  
   p.println(".................... Global information about simulation process............."); 
   p.println("Number of phases: "+phases);
   
   // Print the sizes for decision tables
  
   p.println("Sizes of decision tables: total-optim-unconst        "); 
   p.println("Unconst branches: non cero values in exact classifier derived from");
   p.println("exact evaluation (usually total size/ alternatives for decision");
   p.println("----------------------------------------------------------------");
   for(int i=0; i < decisionTableSizes.size(); i++){
      value=((Double)decisionTableSizes.elementAt(i)).doubleValue();
      optimal=((Double)decisionOptimalPolicyBranches.elementAt(i)).doubleValue();
      unconstrained=((Double)unconstrainedBranches.elementAt(i)).doubleValue();
      p.print(value+"-"+optimal+"-"+unconstrained+"        ");
   }
   p.println("\n----------------------------------------------------------------");
}

/**
 * Method to print the information related to the forward stage of a phase
 * @param p printWriter to print
 */

private void printPrettyPhases(PrintWriter p){
 double forwardTime=0,reconTime=0;
 long forwardSamples,reconSamples;
 double match1,match2,match3,match4;
 Vector forward,recon;
 Vector forward1,recon1;
 Vector distancesForward,distancesRecon;
 int j,k;
 
  // Print times of computation

  p.println("Times of computation: forward-recon");
  p.println("----------------------------------------------------------------");
  for(j=0; j < reconComputationTimes.size(); j++){
      forwardTime=((Double)forwardComputationTimes.elementAt(j)).doubleValue();
      reconTime=((Double)reconComputationTimes.elementAt(j)).doubleValue();
      p.println(forwardTime+"-"+reconTime);
  }
  p.println("----------------------------------------------------------------");

  // Print the number of samples used for every phase and for every decision

  p.println("\nNumber of samples evaluated: forward-recon");
  p.println("----------------------------------------------------------------");
  for(j=0; j < numberOfReconSamples.size(); j++){
      forward=((Vector)numberOfForwardSamples.elementAt(j));
      recon=((Vector)numberOfReconSamples.elementAt(j));
      for(k=0; k < forward.size(); k++){
        forwardSamples=((Long)(forward.elementAt(k))).longValue();
        reconSamples=((Long)(recon.elementAt(k))).longValue();
        p.print(forwardSamples+"-"+reconSamples+"      ");
      }
      p.println();
  }
  p.println("\n----------------------------------------------------------------");
  // Print the number of matches, per phase, stage and decision

  p.println("\nMatches for max classifier: forwardOptim-reconOptim");
  p.println("----------------------------------------------------------------");

  for(j=0; j < maxMatchesForReconStageWithOptimality.size(); j++){
       forward=((Vector)maxMatchesForForwardStageWithOptimality.elementAt(j));
       recon=((Vector)maxMatchesForReconStageWithOptimality.elementAt(j));
       for(k=0; k < forward.size(); k++){
         match1=((Double)(forward.elementAt(k))).doubleValue();
         match2=((Double)(recon.elementAt(k))).doubleValue();
         p.print(match1+" - "+match2+"       ");
       }
       p.println();
  }
  p.println("\n----------------------------------------------------------------");
  // Print the number of matches (without optimality), per phase, stage and decision

  p.println("\nMatches for max classifier: forwardWithoutOptim-reconWithoutOptim");
  p.println("----------------------------------------------------------------");

  for(j=0; j < maxMatchesForReconStageWithoutOptimality.size(); j++){
       forward=((Vector)maxMatchesForForwardStageWithoutOptimality.elementAt(j));
       recon=((Vector)maxMatchesForReconStageWithoutOptimality.elementAt(j));
       for(k=0; k < forward.size(); k++){
         match1=((Double)(forward.elementAt(k))).doubleValue();
         match2=((Double)(recon.elementAt(k))).doubleValue();
         p.print(match1+" - "+match2+"       ");
       }
       p.println();
  }
  p.println("\n----------------------------------------------------------------");

  p.println("\nMatches for samp classifier: forwardOptim-reconOptim");
  p.println("----------------------------------------------------------------");

  for(j=0; j < sampMatchesForReconStageWithOptimality.size(); j++){
       forward=((Vector)sampMatchesForForwardStageWithOptimality.elementAt(j));
       recon=((Vector)sampMatchesForReconStageWithOptimality.elementAt(j));
       for(k=0; k < forward.size(); k++){
         match1=((Double)(forward.elementAt(k))).doubleValue();
         match2=((Double)(recon.elementAt(k))).doubleValue();
         p.print(match1+" - "+match2+"       ");
       }
       p.println();
  }
  p.println("\n----------------------------------------------------------------");
  // Print the number of matches (without optimality), per phase, stage and decision

  p.println("\nMatches for samp classifier: forwardWithoutOptim-reconWithoutOptim");
  p.println("----------------------------------------------------------------");

  for(j=0; j < sampMatchesForReconStageWithoutOptimality.size(); j++){
      forward=((Vector)sampMatchesForForwardStageWithoutOptimality.elementAt(j));
      recon=((Vector)sampMatchesForReconStageWithoutOptimality.elementAt(j));
      for(k=0; k < forward.size(); k++){
        match1=((Double)(forward.elementAt(k))).doubleValue();
        match2=((Double)(recon.elementAt(k))).doubleValue();
        p.print(match1+" - "+match2+"       ");
      }
      p.println();
  }
  p.println("\n----------------------------------------------------------------");
  
  // Print the vector of differences, for decision, with and without optimality
  
  p.println("\n(C45) Expected utility differences: forwardOptim-reconOptim");
  p.println("----------------------------------------------------------------");
  for(j=0; j < maxDifferencesForReconStageWithOptimality.size(); j++){
    forward=((Vector)maxDifferencesForForwardStageWithOptimality.elementAt(j));
    recon=((Vector)maxDifferencesForReconStageWithOptimality.elementAt(j));
    for(k=0; k < decisions; k++){
      match1=((Double)(forward.elementAt(k))).doubleValue();
      match2=((Double)(recon.elementAt(k))).doubleValue();
      p.print(match1+"-"+match2+"       ");
    }
    p.println();
  }
  p.println("\n----------------------------------------------------------------");

  p.println("\n(C45) Expected utility differences: forwardTotal-reconTotal");
  p.println("----------------------------------------------------------------");
  for(j=0; j < maxDifferencesForReconStageWithoutOptimality.size(); j++){
    forward=((Vector)maxDifferencesForForwardStageWithoutOptimality.elementAt(j));
    recon=((Vector)maxDifferencesForReconStageWithoutOptimality.elementAt(j));
    for(k=0; k < decisions; k++){
      match1=((Double)(forward.elementAt(k))).doubleValue();
      match2=((Double)(recon.elementAt(k))).doubleValue();
      p.print(match1+"-"+match2+"       ");
    }
    p.println();
  }
  p.println("\n----------------------------------------------------------------");

  p.println("\n(Dirichlet) Expected utility differences: forwardOptim-reconOptim");
  p.println("----------------------------------------------------------------");
  for(j=0; j < samplingDifferencesForReconStageWithOptimality.size(); j++){
    forward=((Vector)samplingDifferencesForForwardStageWithOptimality.elementAt(j));
    recon=((Vector)samplingDifferencesForReconStageWithOptimality.elementAt(j));
    for(k=0; k < forward.size(); k++){
      match1=((Double)(forward.elementAt(k))).doubleValue();
      match2=((Double)(recon.elementAt(k))).doubleValue();
      p.print(match1+"-"+match2+"       ");
    }
    p.println();
  }
  p.println("\n----------------------------------------------------------------");
  
  p.println("\n(Dirichlet) Expected utility differences: forwardTotal-reconTotal");
  p.println("----------------------------------------------------------------");
  for(j=0; j < samplingDifferencesForReconStageWithoutOptimality.size(); j++){
    forward=((Vector)samplingDifferencesForForwardStageWithoutOptimality.elementAt(j));
    recon=((Vector)samplingDifferencesForReconStageWithoutOptimality.elementAt(j));
    for(k=0; k < forward.size(); k++){
      match1=((Double)(forward.elementAt(k))).doubleValue();
      match2=((Double)(recon.elementAt(k))).doubleValue();
      p.print(match1+"-"+match2+"       ");
    }
    p.println();
  }
  p.println("\n----------------------------------------------------------------");

  // Print the vector of distances, for phase and for decision criteria

  p.println("\nKullback-Leibler distances: forwardMaxOptim-reconMaxOptim (pair for decision");
  p.println("----------------------------------------------------------------");
  for(j=0; j < distancesForReconStage.size(); j++){
    forward=((Vector)distancesForForwardStage.elementAt(j));
    recon=((Vector)distancesForReconStage.elementAt(j));
    for(k=0; k < forward.size(); k++){
      distancesForward=(Vector)forward.elementAt(k);
      distancesRecon=(Vector)recon.elementAt(k);
      match1=((Double)(distancesForward.elementAt(0))).doubleValue();
      match2=((Double)(distancesRecon.elementAt(0))).doubleValue();
      p.print(match1+"-"+match2+"       ");
    }
    p.println();
  }
  p.println("\n----------------------------------------------------------------");

  p.println("\nKullback-Leibler distances: forwardMaxTotal-reconMaxTotal");
  p.println("----------------------------------------------------------------");
  for(j=0; j < distancesForReconStage.size(); j++){
    forward=((Vector)distancesForForwardStage.elementAt(j));
    recon=((Vector)distancesForReconStage.elementAt(j));
    for(k=0; k < decisions; k++){
      distancesForward=(Vector)forward.elementAt(k);
      distancesRecon=(Vector)recon.elementAt(k);
      match1=((Double)(distancesForward.elementAt(1))).doubleValue();
      match2=((Double)(distancesRecon.elementAt(1))).doubleValue();
      p.print(match1+"-"+match2+"       ");
    }
    p.println();
  }
  p.println("\n----------------------------------------------------------------");

  p.println("\nKullback-Leibler distances: forwardSampOptim-reconSampOptim");
  p.println("----------------------------------------------------------------");
  for(j=0; j < distancesForReconStage.size(); j++){
    forward=((Vector)distancesForForwardStage.elementAt(j));
    recon=((Vector)distancesForReconStage.elementAt(j));
    for(k=0; k < decisions; k++){
      distancesForward=(Vector)forward.elementAt(k);
      distancesRecon=(Vector)recon.elementAt(k);
      match1=((Double)(distancesForward.elementAt(2))).doubleValue();
      match2=((Double)(distancesRecon.elementAt(2))).doubleValue();
      p.print(match1+"-"+match2+"       ");
    }
    p.println();
  }
  p.println("\n----------------------------------------------------------------");

  p.println("\nKullback-Leibler distances: forwardSampTotal-reconSampTotal");
  p.println("----------------------------------------------------------------");
  for(j=0; j < distancesForReconStage.size(); j++){
    forward=((Vector)distancesForForwardStage.elementAt(j));
    recon=((Vector)distancesForReconStage.elementAt(j));
    for(k=0; k < decisions; k++){
      distancesForward=(Vector)forward.elementAt(k);
      distancesRecon=(Vector)recon.elementAt(k);
      match1=((Double)(distancesForward.elementAt(3))).doubleValue();
      match2=((Double)(distancesRecon.elementAt(3))).doubleValue();
      p.print(match1+"-"+match2+"       ");
    }
    p.println();
  }
  p.println("\n----------------------------------------------------------------");
}
} // End of class
