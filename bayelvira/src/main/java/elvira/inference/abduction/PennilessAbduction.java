/* PennilessAbduction.java */

package elvira.inference.abduction;

import java.util.Vector;
import java.io.*;
import java.util.Date;
import elvira.*;
import elvira.inference.clustering.*;
import elvira.tools.PropagationStatistics;
import elvira.parser.ParseException;
import elvira.potential.PotentialMTree;
import elvira.potential.PotentialTable;
import elvira.potential.Potential;
import elvira.potential.MultipleTree;
import elvira.inference.abduction.AbductiveInferenceNilsson;


/**
 * Class PennilessAbduction
 * Implements Nilsson's algorithm for finding the K bests probable
 * explanation, but the potentials in the join tree are
 * computed using Penniless Propagation.
 *
 * @author Jose A. Gamez
 * @author Antonio Salmeron
 * @since 23/07/2002
 */

public class PennilessAbduction extends AbductiveInferenceNilsson {

/**
 * The values used to prune the probabilityTree (the same in all stages)
 * For exact computation the ideal value will be 0.0 but the method compares 
 * with < and not with <= so a very small value has to be used
 */

private double limitForPruning;

/** 
 * Number of stages in the previous Penniless propagation.
 */

private int stages;

/** 
 * Number of max stages in the max Penniless propagation.
 */

private int maxStages;
private int currentMaxStage;

/**
 * some constants used in the penniless aproximation
 */

private int infoMeasure = 2;

/**
 * The information values (one for each stage) under which trees will be pruned but will
 * be considered as if they were exact. The reason is that in the Penniless
 * phases, very soft prunings indicate that the tree should not
 * be recomputed, since the gains would be rather low.
 */

private double lowLimitForPruning=1E-6;

/**
 * This values (one for each stage) indicate a fraction of the probability mass represented
 * in the tree. Leaves with lower mass will be replaced by zero.
 */

private double limitSum = 0;

/**
 * The max number of leaves in the probabilityTree (one for each stage).
 * For exact computation the value is taken as the maximum available.
 */

private int maximumSize; 

/**
 * A boolean variable to indicate if sortAndBound is applied
 * Default is true.
 */
  
private boolean applySortAndBound;


private double minNormalizationFactor=Double.MAX_VALUE;
private double maxNormalizationFactor=Double.MIN_VALUE;


                                              
/**
 * Creates a new propagation.
 * @param b a belief network
 * @param e an evidence
 * @param lp a double containing the value of limitForPrunning
 * @param mSize an int indicating the maximum size for potentials 
 * @param sab a boolean that indicates if we must or not do sortAndBound 
 * 
 */

public PennilessAbduction(Bnet b,Evidence e, double lp, int mSize, 
			boolean sab) {

  super(b,e,"trees");

  limitForPruning = lp;
  maximumSize = mSize;
  applySortAndBound = sab;
}



/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * <li> Output file.
 * <li> Number of explanations.
 * <li> An integer: number of penniless stages.
 * <li> An integer: number of max-penniless stages.
 * <li> A double; limit for pruning 
 * <li> An integer indicating the maximum potential size 
 * <li> A value true or false indicating if we do <code>sortAndBound</code> or not.
 * <li> Interest file (contains the interest variables - explanation set)
 * If this parameter equals to "total" then total abduction is carried out
 * <li> File with instantiations.
 * The evidence file is optional.
 * </ol>
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile, interestFile;
  PennilessAbduction ain;
  int i, m, nStages, nMaxStages;
  int numExplanations;
  double lp; 
  FileWriter f;
  NodeList expSet;
  String interest;
  int maxSize;
  boolean sab;
  
  if (args.length < 9) {
    System.out.println("Too few arguments. Arguments are:");
    System.out.println("\tNetwork OutputFile K number_penni_stages number_max_penni_stages");
    System.out.println("\tLimitForPruning maximumSize SortAndBound=(yes|no)");
    System.out.println("\t(InterestFile|total) [EvidenceFile]\n");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);

    lp = (Double.valueOf(args[5])).doubleValue();
    numExplanations = (Integer.valueOf(args[2])).intValue();
 
    if (args.length == 10){
      evidenceFile = new FileInputStream(args[9]);
      e = new Evidence(evidenceFile,b.getNodeList());
      System.out.println("Evidence file "+args[9]);
    }
    else{
      e = new Evidence();
    }

    maxSize = (Integer.valueOf(args[6])).intValue();
    sab = (Boolean.valueOf(args[7])).booleanValue();
    
    ain = new PennilessAbduction(b,e,lp,maxSize,sab);

    nStages = (Integer.valueOf(args[3])).intValue();
    nMaxStages = (Integer.valueOf(args[4])).intValue();
    ain.stages = nStages;
    ain.maxStages = nMaxStages;
      
    ain.setNExplanations(numExplanations);

    interest = args[8];
    if (interest.equals("total")){
      ain.setPartial(false);
      ain.setPropComment("total");
    }
    else{
      interestFile = new FileInputStream(interest);
      System.out.println("Interest file "+interest);
      expSet = new NodeList(interestFile,b.getNodeList());
      ain.setPartial(true);
      ain.setExplanationSet(expSet);
      ain.setPropComment("subtree");
    }
    
    ain.propagate(args[1]);
    ain.saveResults(args[1]);
  }
}


/**
 * Initialising arrays for (non max) penniles initialisation
 */

private void initPennilessArrays(double lfp[],double llfp[],double lfs[],
				int ms[],boolean sab[]){
  int i;

  if (stages != 2){
    lfp = new double[stages];
    llfp = new double[stages];
    lfs = new double[stages];
    ms = new int[stages];
    sab = new boolean[stages];
  }

  for(i=0;i<stages;i++){
    lfp[i]=limitForPruning;
    llfp[i] = lowLimitForPruning;
    lfs[i] = limitSum;
    ms[i] = maximumSize;
    sab[i] = applySortAndBound;
  }

}

/**
 * Carries out a propagation
 *
 * @param OutputFile the file where the K MPEs will be stored
 *
 */

public void propagate(String OutputFile) {

  double pEvidence, pBest, extraSize, time, t;
  Penniless penni;
  Explanation exp;
  int i;
  AIPartitionElementList pl;
  PropagationStatistics stat;
  Vector potentials;
  Date d;
  double lfp[]=new double[2],llfp[]=new double[2],lfs[]=new double[2];
  int ms[]=new int[2];
  boolean sab[]=new boolean[2];

  stat = this.getStatistics();

  System.out.println("Computing best explanation ...");
  d = new Date();
  time = (double)d.getTime();       

  
  // first, we build the join tree
  jt = new JoinTree( );
  jt.treeOfCliques(network,explanationSet);

  // building arrays for penniless initialisation 
  // initialising penniless

  //if (stages < 2) stages=2;
  initPennilessArrays(lfp,llfp,lfs,ms,sab);

  penni = new Penniless(network,observations,lfp,llfp,lfs,ms,
				sab,infoMeasure,explanationSet);
  
  penni.getJoinTree().sortVariables(network.getNodeList());
  
  
  // now we get the joinTree from Penniless propagation
  
  jt = penni.getJoinTree();

  // data for statistics
  jt.calculateStatistics();
  this.statistics.setJTInitialSize(jt.getStatistics().getJTSize());  

  // Perform Penniless propagation HERE
  if (stages > 0) 
    penni.propagate(stages);
  else penni.initMessages();

  // calculating evidence probability

  if (observations.size()>0) {
    if (stages == 0) penni.propagate(1);  /* because no
		propagation has been carried out previously */
    pEvidence = penni.obtainEvidenceProbabilityFromRoot();
  }
  else pEvidence = 1.0;
  System.out.println("Evidence Probability: " + pEvidence);

  penni.initMessages();
  
  // si fuera parcial ahora habría que hacer el outer restriction

  // ****************

  // the rest of the process is common for partial and 
  // total abductive inference

  System.out.println("The number of nodes in jt is: " + jt.size());  
  //jt.display();

  maxPropagate(maxStages);


  combineWithChildren(jt.elementAt(0));
  System.out.println("Combining with children ended, looking for MPEs.");

  System.out.println("\nJoin tree after combining with children\n");
  jt.display();

  // Obtaining the kBest explanations

  pBest = getBestExplanation( );

  if (nExplanations > 1) {
    pl = initPartitionList(pBest);
    for (i=1 ; i<nExplanations ; i++) {
	refinePartitionList(pl);
	getNextExplanation(pl);
    }
  }

  // now we divide by pEvidence

  if (observations.size()!=0) {
    for (i=0 ; i < nExplanations ; i++) {
      exp = (Explanation) kBest.elementAt(i);
      exp.setProb(exp.getProb()/pEvidence);
    }
  }




  // finishing

  d = new Date();
  time = ((double)d.getTime() - time) / 1000;

  stat.setTime(time);
  jt.calculateStatistics();
  stat.setJTStat(jt.getStatistics());

  this.setStatistics(stat);

  // showing messages

  System.out.println("Best explanation computed, Time = " + time);




}


/**
 * Carries out a propagation over the cliques of the
 * explanation set using max as marginalisation operator.
 *
 * @param steps the number of stages of max-penniless.
 * 
 * the number of steps is fixed to an odd number, with a minimum of 1
 */

public void maxPropagate(int steps) {

  NodeJoinTree root;
  NodeList variables;
  int i;

  // adapting steps (if needed)

  if ((steps%2)==0){
    steps++;
    System.out.println("\nThe number of maxStages has been increased to " + steps);
  }

  // Perform the propagation
  
  root = jt.elementAt(0);
  System.out.println("Starting max-penniless phase");
  currentMaxStage = 0;
  
  // First stage: navigates from leaves upwards.
  System.out.println("Max-Propagation stage "+currentMaxStage+"(up)");
  maxNavigateUp(root);
  steps--;
  currentMaxStage++;

  System.out.println("\nJoin Tree despues de upward: ");
  jt.display();

  // remaining steps: navigates down and up
  while (steps >= 2) {
    System.out.println("Max-Propagation stage "+currentMaxStage+"(down)");
    System.out.println("Max-Propagation stage "+(currentMaxStage+1)+"(up)");
    maxNavigateDownUp(root);
    steps -= 2;
    currentMaxStage += 2;

    //System.out.println("\nJoin Tree despues de down-up(ward): ");
    //jt.display();
  }

  // End of propagation
  System.out.println("Max-Penniless phase done");
}


/**
 * Sends messages from leaves to root (sender).
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

private void maxNavigateUp(NodeJoinTree sender) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  
  list = sender.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    maxNavigateUp(sender,other);
  }
}	       

/**
 * Sends messages from leaves to root (sender) through the branch recipient.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 */

private void maxNavigateUp(NodeJoinTree sender, NodeJoinTree recipient) {

  NeighbourTreeList list;
  NodeJoinTree other;
  int i;

  // Nodes to which the message will be sent downwards.
  list = recipient.getNeighbourList();

  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    
    if (other.getLabel() != sender.getLabel()) {
      maxNavigateUp(recipient,other);
    }
  }
  
  sendMaxMessage(recipient,sender);
}


/**
 * Sends a message from a node to another one.
 * Marks the messages as not exact when this method carries out
 * an approximation or one of the input messages are not exact. 
 * The message is computed by combining all the messages inwards
 * the sender except that one comming from the recipient. Then, the
 * result is sorted and bounded conditional to the message
 * going from the recipient to the sender.
 * It is required that the nodes in the tree be labeled.
 * Use method <code>setLabels</code> if necessary.
 * The marginal over the separator is computed using
 * maximum operator instead of sum.
 *
 * @param sender the node that sends the message.
 * @param recipient the node that receives the message.
 */

public void sendMaxMessage(NodeJoinTree sender, NodeJoinTree recipient) {
  
  NeighbourTreeList list, auxList;
  NeighbourTree nt;
  PotentialMTree aux, pot, incoming;
  Relation rel, outwards, inwards;
  int i, label;
  Vector separator;
  boolean isExact = true;
  int step;
  
  incoming = new PotentialMTree();
  outwards = new Relation();
  inwards = new Relation();
  separator = new Vector();
  
  aux = (PotentialMTree)sender.getNodeRelation().getValues();
  
  pot = new PotentialMTree();
  pot.setTree(MultipleTree.unitTree());
  
  list = sender.getNeighbourList();
  
  for (i=0 ; i<list.size() ; i++) {
    nt = list.elementAt(i);
    label = nt.getNeighbour().getLabel();
    rel = nt.getMessage();
    
    // Combine the messages coming from the other neighbours
    
    if (label != recipient.getLabel()) { 
      pot = pot.combine((PotentialMTree)rel.getOtherValues());
      if (!((PotentialMTree)rel.getOtherValues()).getExact()) {
	isExact = false;
      }
    }
    else {
      incoming = (PotentialMTree)rel.getOtherValues();
      outwards = rel;
      
      separator =  rel.getVariables().getNodes();
      auxList = recipient.getNeighbourList();
      inwards = auxList.getMessage(sender); 
    }
  }
  
  // Now combine with the potential in the node.
  
  pot = pot.combine(aux);
  pot = (PotentialMTree)(pot.maxMarginalizePotential(separator)); 
  
  pot = (PotentialMTree)pot.conditional((Potential)incoming);
  pot.setExact(isExact);

  step = stages + currentMaxStage;
  
  pot.conditionalLimitBound(MultipleTree.ZERO_APPROX,limitForPruning,
			    lowLimitForPruning,limitSum,infoMeasure);
  
    
  if (applySortAndBound) {
    pot = pot.conditionalSortAndBound(maximumSize,infoMeasure);
    pot.conditionalLimitBound(MultipleTree.ZERO_APPROX,limitForPruning,
			lowLimitForPruning,limitSum,infoMeasure);
  }
  
  // Now update the messages in the join tree.
  
  outwards.setValues(pot);
  inwards.setOtherValues(pot);
}


/**
 * Send messages from root (sender) to leaves, and then from leaves
 * to root.
 * The method does not navigate throw a branch if the message in the
 * opposite direction (<code>getOtherValues()</code>) is exact.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

private void maxNavigateDownUp(NodeJoinTree sender) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  Relation sep;
  int i;
  
  list = sender.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    sep = list.elementAt(i).getMessage();
    
    // if opposite message is not exact
    if (!(sep.getOtherValues().getExact())) {
      // if previous messages was not exact
      if (!(sep.getValues().getExact()))
	sendMaxMessage(sender,other);
      maxNavigateDownUp(sender,other);
    }
  }
}	       


/**
 * Sends messages from root (sender) to leaves, and then from leaves
 * to root, through the branch towards node <code>recipient</code>.
 * This method does not navigate throw a branch if the message in the opposite
 * direction (<code>getOtherValues()</code>) is exact.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 */

private void maxNavigateDownUp(NodeJoinTree sender, NodeJoinTree recipient) {

  NeighbourTreeList list;
  NodeJoinTree other;
  Relation sep;
  int i;
    
  // Nodes to which the message will be sent downwards.
  list = recipient.getNeighbourList();

  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour(); 
    if (other.getLabel() != sender.getLabel()) {
      sep = list.elementAt(i).getMessage();

      // if opposite message is not exact
      if (!(sep.getOtherValues().getExact())) { 
	// if previous message was not exact
        if (!(sep.getValues().getExact()))
	  sendMaxMessage(recipient,other);	
	maxNavigateDownUp(recipient,other);
      }
    }
  }
  // if the opposite message is not exact
  if (!(list.getMessage(sender).getOtherValues().getExact()))  
    sendMaxMessage(recipient,sender);
}



/**
 * This method is used to combine the potential in each clique with 
 * the icomming messages in its children.
 * 
 * Please notice the fact than messages have to be calculated previously
 *
 * @param root the node used as root in the join tree
 * 
 */

public void combineWithChildren(NodeJoinTree root){

  NeighbourTreeList ntl;
  NodeJoinTree node;
  int i;
  Potential message,pot;
  Relation r;  

  ntl = root.getNeighbourList();
  
  // performing combination and recursive calls

  r = root.getNodeRelation();
  pot = r.getValues();

  for(i=0;i<ntl.size();i++){
    node = ntl.elementAt(i).getNeighbour();
    //message = ntl.elementAt(i).getMessage().getValues();
    message = ntl.elementAt(i).getMessage().getOtherValues();
    pot.combineWithSubset(message);    
    // ---- call to transform potential    
    //pot = transformPotentialAfterCombination((PotentialMTree)pot);

    combineWithChildren(node,root);    
  }
  r.setValues(pot);
}



/**
 * This method is used to combine the potential in each clique with 
 * the icomming messages in its children. 
 * 
 * Please notice the fact than messages have to be calculated previously
 *
 * @param node the node receiving the call
 * @param parent the paren node in the join tree
 */

public void combineWithChildren(NodeJoinTree node,NodeJoinTree parent){

  NeighbourTreeList ntl;
  NodeJoinTree child;
  int i;
  Potential message,pot;
  Relation r;  

  ntl = node.getNeighbourList();
  
  // performing combination and recursive calls

  r = node.getNodeRelation();
  pot = r.getValues();

  for(i=0;i<ntl.size();i++){
    child = ntl.elementAt(i).getNeighbour();
    if (child.getLabel() != parent.getLabel()){
      //message = ntl.elementAt(i).getMessage().getValues();
      message = ntl.elementAt(i).getMessage().getOtherValues();
      pot.combineWithSubset(message);    
      // ---- call to transform potential    
      //pot = transformPotentialAfterCombination((PotentialMTree)pot);

      combineWithChildren(child,node);
    }
  }
  r.setValues(pot);
}


/**
 * Transforms a <code>PotentialMTree</code> after a combination operator.
 * IMPORTANT: the potential passed as parameter is modified.
 * @param pot the <code>PotentialMTree</code> to be transformed.
 * @return the transformed <code>PotentialMTree</code>.
 */

public Potential transformPotentialAfterCombination(Potential pot) {

    if ( ((PotentialMTree)pot).getTree().getLabel() == 1)
      if (applySortAndBound) 
        pot = ((PotentialMTree)pot).sortAndBound(maximumSize);

    ((PotentialMTree)pot).limitBound(limitForPruning);
  
    return pot;
}



} // end of class
