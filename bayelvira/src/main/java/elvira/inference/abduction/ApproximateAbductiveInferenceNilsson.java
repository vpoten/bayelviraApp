/* ApproximateAbductiveInferenceNilsson.java */

package elvira.inference.abduction;

import java.util.Vector;
import java.io.*;
import java.util.Date;
import elvira.*;
import elvira.inference.clustering.*;
import elvira.tools.PropagationStatistics;
import elvira.parser.ParseException;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialTable;
import elvira.potential.Potential;
import elvira.inference.abduction.AbductiveInferenceNilsson;


/**
 * Class ApproximateAbductiveInferenceNilsson
 * Implements Nilsson's algorithm for finding the K bests probable
 * explanation, but using (approximated) trees during propagation
 *
 * @since 20/01/2001
 */

public class ApproximateAbductiveInferenceNilsson extends AbductiveInferenceNilsson {

/**
 * The value used to prune the probabilityTree.
 * For exact computation the ideal value will be 0.0 but the method compares 
 * with < and not with <= so a very small value has to be used
 */

private double limitForPrunning;

/**
 * The max number of leaves in the probabilityTree.
 * For exact computation the value is taken as the maximum available.
 */

private int maximumSize; 

/**
 * A boolean variable to indicate if sortAndBound is applied
 * Default is true.
 */
  
private boolean ApplySortAndBound=false;
                                              
/**
 * Creates a new propagation.
 * @param b a belief network
 * @param e an evidence
 * @param limit the limit for prunning
 * @param maxSize the max number of leaves in a probability tree
 */

public ApproximateAbductiveInferenceNilsson(Bnet b,Evidence e,
                                     double limit,int maxSize) {

  super(b,e,"trees");

  limitForPrunning = limit;
  maximumSize = maxSize;      
}


/**
 * Creates a new propagation.
 * @param b a belief network
 * @param e an evidence
 * @param limit the limit for prunning
 * @param maxSize the max number of leaves in a probability tree
 */

public ApproximateAbductiveInferenceNilsson(Bnet b,Evidence e,
                                     double limit,int maxSize,
                                     boolean sab) {

  super(b,e,"trees");

  limitForPrunning = limit;
  maximumSize = maxSize;
  ApplySortAndBound = sab;      

  jt.setLimitForPotentialPruning(limitForPrunning);
  jt.setMaximumSizeForPotentialPrunning(maximumSize);
  jt.setApplySortAndBound(ApplySortAndBound);


}


/**
 * Program for performing experiments.
 * The arguments are as follows.
 * 0. Input file: the network.
 * 1. Output file.
 * 2. Number of explanations
 * 3. Method (total,size,restrictedSize,ratio). The last three only have
 *            effect if there is an explanation set
 * 4. The value used as limit
 * 5. The value used as maximum number of leaves in the probabilityTree. If
 *    this value is equal to 0, then no sortAndBound is applied    
 * 6. Evidence file.
 * 7. Interest file (contains the interest variables - explanation set)
 * The evidence file and the interest file are optional.
 * 
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile, interestFile;
  ApproximateAbductiveInferenceNilsson ain;
  int i;
  NodeList expSet;
  double limit;
  int maxLeaves;
  boolean sab;
  
  if (args.length<7){
    System.out.println("Too few arguments. The argumens are:");
    System.out.println("\tNetwork output-file K (total|size|restrictedSize|subtree)");
    System.out.println("\tlimit-for-prunning max-leaves sort-and-bound (true|false) [evidence-file] [interest-file]");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);
   
    limit = (Double.valueOf(args[4])).doubleValue();
    maxLeaves = (Integer.valueOf(args[5])).intValue();

    expSet = new NodeList();

    if (args.length==8) {
      // trying if args[7] is the evidence
      try {
        evidenceFile= new FileInputStream(args[7]);
        e = new Evidence(evidenceFile,b.getNodeList());
      }catch (ParseException pe){
        interestFile = new FileInputStream(args[7]);
        expSet = new NodeList(interestFile,b.getNodeList());
 
        e = new Evidence();
      }      
    }
    else e = new Evidence();

    if (args.length==9) {
      evidenceFile = new FileInputStream(args[7]);
      e = new Evidence(evidenceFile,b.getNodeList());

      interestFile = new FileInputStream(args[8]);
      expSet = new NodeList(interestFile,b.getNodeList());
    }


    if (args[6].equals("true")) sab=true;
    else sab=false;

    ain = new ApproximateAbductiveInferenceNilsson(b,e,limit,maxLeaves);

    ain.setExplanationSet(expSet);
    if (expSet.size() > 0) ain.setPartial(true);
    ain.setNExplanations((Integer.valueOf(args[2])).intValue());
    ain.setPropComment(args[3]);

    ain.propagate(args[1]);

    ain.saveResults(args[1]);
  }
}

/**
 * Set the value for ApplySortAndBond
 */

public void setApplySortAndBound(boolean sab){
  ApplySortAndBound = sab;
}


/**
 * Carries out a propagation
 *
 * @param OutputFile the file where the K MPEs will be stored
 *
 */

public void propagate(String OutputFile) {

  double pEvidence,pBest;
  ApproximateHuginPropagation hp;
  Explanation exp;
  int i;
  AIPartitionElementList pl;
  PropagationStatistics stat;
  double extraSize;
  Vector potentials;


  Date D;
  double time,t;
  

  stat = this.getStatistics();

  System.out.println("Computing best explanation ...");
  D = new Date();
  time = (double)D.getTime();       

  if (!this.getPartial()){

    // first, we calculate the probability of the evidence
    // using a hugin propagation

    hp = new ApproximateHuginPropagation(network,observations,deletionSequence,
	                   limitForPrunning,maximumSize,ApplySortAndBound);

    if (observations.size()>0){
      pEvidence = hp.obtainEvidenceProbability("yes");
    }
    else pEvidence = 1.0;
    System.out.println("Probabilidad de la evidencia: " + pEvidence);
 
    // second, we perform an upward propagation using max as 
    // marginalization operator  

    // we uses HuginPropagation for the initialization of the joinTree

    hp.getJoinTree().sortVariables(network.getNodeList());

    hp.getJoinTree().initTrees(network);
    hp.transformRelationsInJoinTree( );  
    
    hp.getJoinTree().setLabels();


    if (observations.size() > 0) hp.instantiateEvidence();
    hp.initHuginMessages();

    // now we get the joinTree from huginPropagation  

    jt = hp.getJoinTree();

  }
  else{ // partial abductive inference

    // first, we build the join tree

    if (this.getPropComment().equals("subtree")){
      hp = new ApproximateHuginPropagation(observations,network,
			limitForPrunning,maximumSize);
      jt.treeOfCliques(network,explanationSet);
      hp.setJoinTree(jt);
      
      hp.getJoinTree().setLimitForPotentialPruning(limitForPrunning);
      hp.getJoinTree().setMaximumSizeForPotentialPrunning(maximumSize);
      hp.getJoinTree().setApplySortAndBound(ApplySortAndBound);
      hp.setApplySortAndBound(ApplySortAndBound);
    }
    else hp = new ApproximateHuginPropagation(network,observations,
					limitForPrunning,maximumSize,
                                        ApplySortAndBound);


    hp.getJoinTree().sortVariables(network.getNodeList());

    hp.getJoinTree().initTrees(network);

    hp.transformRelationsInJoinTree( );  

    hp.getJoinTree().setLabels();

    // if evidence restrict tables

    if (observations.size() > 0) hp.instantiateEvidence();
    hp.initHuginMessages();

    // now we get the joinTree from huginPropagation  

    jt = hp.getJoinTree();

    // data for statistics
    jt.calculateStatistics();
    this.statistics.setJTInitialSize(jt.getStatistics().getJTSize());        

    // outer restriction

    jt.setLimitForPotentialPruning(limitForPrunning);
    jt.setMaximumSizeForPotentialPrunning(maximumSize);
    jt.setApplySortAndBound(ApplySortAndBound);
    jt.outerRestriction(explanationSet,"no","no");
    jt.setLabels();

    // storing potentials for posterior use
   
    potentials = jt.storePotentials();

    // calculating evidence probability

    if (observations.size()>0){
      hp.setJoinTree(jt);
      pEvidence = hp.obtainEvidenceProbability("no");
      jt = hp.getJoinTree();
    }
    else pEvidence = 1.0;
    System.out.println("Evidence Probability: " + pEvidence);

    // restoring potentials
    
    jt.restorePotentials(potentials);

    // Now, we do inner restriction
  


    if (!(this.getPropComment().equals("subtree"))){
      jt.setLimitForPotentialPruning(limitForPrunning);
      jt.setMaximumSizeForPotentialPrunning(maximumSize);
      extraSize = jt.innerRestriction(explanationSet,"no",this.getPropComment(),
                          network.getNodeList());
      jt.setLabels();
      //try{ jt.display2();} catch (IOException ioe) { System.out.println("");}
      stat.setJTExtraSize(extraSize);
    }    

  }


  // the rest of the process is common for partial and total abductive inference

  upward(jt.elementAt(0),"no");

  // Obtaining the kBest explanations
  
  pBest = getBestExplanation( );

  if (nExplanations > 1){
    pl = initPartitionList(pBest);
    for (i=1; i<nExplanations; i++){
      refinePartitionList(pl);
      getNextExplanation(pl);
    }
  }

  // now we divide by pEvidence
 
  if (observations.size()!=0){
    for(i=0; i < nExplanations; i++){
      exp = (Explanation) kBest.elementAt(i);
      exp.setProb(exp.getProb()/pEvidence);
    }
  }

  

  D = new Date();
  time = ((double)D.getTime() - time) / 1000;
    
  stat.setTime(time);
  jt.calculateStatistics();
  stat.setJTStat(jt.getStatistics());

  this.setStatistics(stat);  

  // showing messages

  System.out.println("Best explanation computed, Time = " + time);
}


/**
 * Transforms a potentialTree.
 * The only thing to do is to prune the
 * nodes which children are equals, so we use a smallest value
 * for limit.
 * This method can be overcharged for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @param pot the relation to be transformed
 * @return the transformed potential
 */

public Potential transformPotentialAfterMarginalization(Potential pot){

  if ( ((PotentialTree)pot).getTree().getLabel() == 1) 
     if (ApplySortAndBound)
       pot = ((PotentialTree)pot).sortAndBound(maximumSize);   
  ((PotentialTree)pot).limitBound(limitForPrunning);

  return pot;
}                

/**
 * Transforms a potentialTree.
 * The only thing to do is to prune the
 * nodes which children are equals, so we use a smallest value
 * for limit.
 * This method can be overcharged for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @param pot the relation to be transformed
 * @return the transformed potential
 */

public Potential transformPotentialAfterCombination(Potential pot){

    if ( ((PotentialTree)pot).getTree().getLabel() == 1)
      if (ApplySortAndBound)
        pot = ((PotentialTree)pot).sortAndBound(maximumSize);
    ((PotentialTree)pot).limitBound(limitForPrunning);

  return pot;        

}                

} // end of class