/* Penniless.java */

package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;

import elvira.potential.PotentialTable;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialMTree;
import elvira.potential.MultipleTree;
import elvira.potential.Potential;

/**
 * Class <code>Penniless</code>.
 * Implements an approximate propagation method over a
 * binary join tree. Potentials are represented by means of
 * probability trees of class <code>MultipleTree</code>, allowing to compute,
 * during the propagation, an interval where the true probability lies.
 *
 * @since 15/07/2002
 */


public class Penniless extends Propagation {
  
/**
 * The binary join tree.
 */  
JoinTree binTree;

/**
 * A vector containing the maximum size of a potential
 * at each stage. The number of stages will be equal to
 * the size of this array.
 */
public int[] maximumSize;

/**
 * A vector of boolean values. Each value <code>sortAndBound[i]</code>
 * tell us if the algorithm carries out a <code>limitSortAndBound</code>
 * at stage <code>i</code>.
 */
public boolean[] sortAndBound;

/**
 * The number of propagation stages.
 */
public int stages;

/**
 * The current stage.
 */
public int currentStage;


/**
  * The way in which we approximate several leaves by a single double value
  * @see <code>elvira.potential.MultipleTree</code> for possible kinds
  */
int kindOfApprPruning;

/**
 * Used to prune leaves whose addition is lower than
 * a fraction of the addition of the entire potential.
 * That fraction is indicated by this parameter.
 * <code>PotentialTree.limitBound(double, double)</code>.
 */
private double[] limitSumForPruning;

/**
 * The information limit for pruning.
 */
public double[] limitForPruning;

/**
 * The information limit to consider whether the tree is exact or not
 * when pruning in <code>conditionalPrune</code>
 * or </code>conditionalPruneSimple</code>.
 */
public double[] lowLimitForPruning;

/**
 * Method for computing the information measure used for pruning.
 * <ul>
 * <li> 1 for the method of calculating entropy published in
 * "Penniless Propagation in Join Trees" 
 * <li> 2 for an improved method.
 * </ul>
 */
public int infoMeasure;

/**
 * A <code>Hashtable</code> with the <code>NodeJoinTree</code>
 * that will be used to obtain the marginal for each variable.
 */
public Hashtable marginalCliques;


private double minNormalizationFactor=Double.MAX_VALUE;

private double maxNormalizationFactor=Double.MIN_VALUE;


/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * <li> Output file where the results of the propagation
 * will be written.
 * <li> Output statistics file, where the error, computing time
 * and other statistics about each experiment will be stored.
 * <li> File with exact results. If no exact results are
 * available, use NORESULTS instead.
 * <li> A string indicating the kind of pruning (AVERAGE or ZERO)
 * <li> The method for approximating (1|2).
 * <ul>
 * <li> 1 for the method of calculating entropy published in
 * "Penniless Propagation in Join Trees (IJIS-2000)" 
 * <li> 2 for the improved method published in "Different Strategies
 * to Approximate Probability Trees in Penniless Propagation"
 * (CAEPIA-2001).
 * </ul>
 * @see MultipleTree.conditionalPrune1() and MultipleTree.conditionalPrune2() 
 * <li> A double; limit for pruning.
 * <li> A double; lowLimit to consider the tree exact or not when pruning.
 * That is, if a branch is pruned and information is lower than lowLimit then
 * we consider that no approximation has been carry out.
 * Also, this value is used  to prune initial potentials
 * (<code>PotentialTree</code>).
 * <li> An integer: number of stages. 
 * <li> An integer for each stage indicating the maximum potential
 * size at that stage.
 * <li> A value true or false for each stage indicating if we do
 * <code>sortAndBound</code> or not.
 * <li> File with instantiations.
 * </ol>
 * The last argument can be omitted. In that case, it will
 * be considered that no observations are present.
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  Penniless propagation;
  int i, m, nStages, triangMethod;
  int[] ls;
  double[] lp; double[] llp; double[] lsp;
  boolean[] sortAndBound;
  double[] errors;
  double g, mse, timePropagating;
  Date date;
  FileWriter f;
  PrintWriter p;
  
  if (args.length < 13) {
    System.out.print("Too few arguments. Arguments are: ElviraFile");
    System.out.print(" OutputFile OutputStatisticsFile InputExactResultsFile");
    System.out.print(" kindOfApprPruning(AVERAGE|ZERO|AVERAGEPRODCOND) infoMeasure(1|2)  NumberStages LimitForPruningStage1 LimitForPruningStage2 ... LowLimitForPruningStage1 LowLimitForPruningStage2 ... LimitSumForPruning1 LimitSumForPruning2 ...");
    System.out.println(" MaxSizeInStage1 MaxSizeInStage2 ... ");
    System.out.println(" SortAndBoundInStage1(true|false) SortAndBoundInStage2 ... TriangulationMethod(0|1|2) [EvidenceFile]");
  }
  else {
    nStages = (Integer.valueOf(args[6])).intValue();
    if (args.length < (5*nStages)+ 8) {
    System.out.print("Too few arguments. Arguments are: ElviraFile");
    System.out.print(" OutputFile OutputStatisticsFile InputExactResultsFile");
    System.out.print(" kindOfApprPruning(AVERAGE|ZERO|AVERAGEPRODCOND) infoMeasure(1|2)  NumberStages LimitForPruningStage1 LimitForPruningStage2 ... LowLimitForPruningStage1 LowLimitForPruningStage2 ... LimitSumForPruning1 LimitSumForPruning2 ...");
    System.out.println(" MaxSizeInStage1 MaxSizeInStage2 ... ");
    System.out.println(" SortAndBoundInStage1(true|false) SortAndBoundInStage2 ... TriangulationMethod(0|1|2) [EvidenceFile]");
    }
    else {
      networkFile = new FileInputStream(args[0]);
      b = new Bnet(networkFile);
      
      if (args.length == 5 * nStages + 9) {
	evidenceFile = new FileInputStream(args[5*nStages+8]);
	e = new Evidence(evidenceFile,b.getNodeList());
	System.out.println("Evidence file"+args[5*nStages+8]);
      }
      else
	e = new Evidence();

      triangMethod = (Integer.valueOf(args[5*nStages+7])).intValue();

      lp= new double[nStages];
      llp= new double[nStages];
      lsp= new double[nStages];      
      ls = new int[nStages];
      sortAndBound = new boolean[nStages];
      for (i=0 ; i<nStages ; i++) {
	lp[i] = (Double.valueOf(args[i+7])).doubleValue();
	llp[i] = (Double.valueOf(args[i+7+nStages])).doubleValue();
        lsp[i] = (Double.valueOf(args[i+7+2*nStages])).doubleValue();
	ls[i] = (Integer.valueOf(args[i+7+3*nStages])).intValue();
	sortAndBound[i]= (Boolean.valueOf(args[i+7+4*nStages])).booleanValue();
      }
      
      m = (Integer.valueOf(args[5])).intValue();

      System.out.println("Method to calculate info: "+m);
      System.out.println("Number of propagation stages: "+nStages);
      System.out.println("Triangulation method: "+triangMethod);
      
      propagation = new Penniless(b,e,lp,llp,lsp,ls,sortAndBound,m,triangMethod);
      propagation.setKindOfApprPruning(args[4]);
      
      date = new Date();
      timePropagating = (double)date.getTime();
      propagation.propagate(args[1]);
      
      date = new Date();
      timePropagating = ((double)date.getTime()-timePropagating) / 1000;
      
      f = new FileWriter(args[2]);
      p = new PrintWriter(f);
	
      p.println("Time propagating (secs) : "+timePropagating);

      if (!args[3].equals("NORESULTS")) {
	System.out.println("Reading exact results");
	propagation.readExactResults(args[3]);
	System.out.println("Exact results read");
	
	System.out.println("Computing errors");
	errors = new double[2];
	propagation.computeError(errors);
	
	g = errors[0];
	mse = errors[1];
	
	propagation.computeKLError(errors);
            
	p.println("G : "+g);
	p.println("MSE : "+mse);
	p.println("K-L error : "+errors[0]);
	p.println("Min Norm Factor: "+propagation.minNormalizationFactor);
	p.println("Max Norm Factor: "+propagation.maxNormalizationFactor);	
	p.println("(Max-Min)/Min: "+
		  ((propagation.maxNormalizationFactor-propagation.minNormalizationFactor)/propagation.minNormalizationFactor));
	p.println("Std dev. of K-L errors : "+errors[1]);
	p.println("Max absolute error : "+propagation.computeMaxAbsoluteError());
      }
      propagation.binTree.calculateStatistics();
      propagation.binTree.saveStatistics(p);
      f.close();
      
      System.out.println("Done"); 
    }
  }
}



/**
 * Creates an empty object.
 */

Penniless() {
  infoMeasure=1;
  kindOfApprPruning=MultipleTree.AVERAGE_APPROX; 
}


/**
 * Creates a new propagation.
 * @param b a belief network.
 * @param e an evidence.
 * @param lp a vector of double values for the limits for pruning in each stage.
 * @param llp a vector of double values for the lowLimit for pruning in each stage.
 * @param lsp a vector of double values for the lower sum for pruning in each stage.
 * @param ls a vector for the maximum sizes for potentials in each stage.
 * @param sortAndBound vector of boolean that indicates if we must or
 * not do sortAndBound in each step.
 * @param m the info measure used  for pruning the trees (1|2).
 * @param triangMethod indicates the triangulation to carry out.
 * <ol>
 * <li> 0 is for considering evidence during the triangulation.
 * <li> 1 is for considering evidence and directly remove relations
 * that are conditional distributions when the conditioned variable
 * is removed.
 * <li> 2 is for not considering evidence.
 * </ol> */

Penniless(Bnet b, Evidence e, double[] lp, double[] llp,double[] lsp,
	  int[] ls, boolean[] sortAndBound,int m, int triangMethod) {
  
  Triangulation triang;
  RelationList rel1, rel2, ir, irTree;
  NodeList numeration = new NodeList();
  int i;
  PotentialTree pt;
  PotentialMTree pmt;
  Relation newRel, rel;
  
  observations = e;
  network = b;
  positions = new Hashtable();  
  
  if (triangMethod == 2)
    binTree = new JoinTree(b);
  else
    binTree = new JoinTree(b,e,triangMethod);

  irTree = getInitialRelations();
  irTree.restrictToObservations(observations);
  for (i=0 ; i<irTree.size() ; i++) {
    ((PotentialTree)irTree.elementAt(i).getValues()).limitBound(llp[0]);
  }

  ir = new RelationList();
  for (i=0 ; i<irTree.size() ; i++) {
    rel = irTree.elementAt(i);
    newRel = new Relation();
    newRel.setVariables(rel.getVariables().copy());
    pt=(PotentialTree)rel.getValues();
    pmt = new PotentialMTree(pt);
    newRel.setValues(pmt);
    newRel.setKind(rel.getKind());
    ir.insertRelation(newRel);
  }

  marginalCliques = binTree.Leaves(ir);

  binTree.binTree();
 
  setMaximumSizes(ls);
  setSortAndBound(sortAndBound);
  setLimitForPruning(lp);
  setLowLimitForPruning(llp);
  setLimitSumForPruning(lsp);
  setInfoMeasure(m);
  kindOfApprPruning=MultipleTree.AVERAGE_APPROX;
  binTree.setLabels();
}



/**
 * Creates a new propagation: if set is not an empty node list, the
 * triangulation is constrained by set
 * @param b a belief network.
 * @param e an evidence.
 * @param lp a vector of double values for the limits for pruning in each stage.
 * @param llp a vector of double values for the lowLimit for pruning in each stage.
 * @param lsp a vector of double values for the lower sum for pruning in each stage.
 * @param ls a vector for the maximum sizes for potentials in each stage.
 * @param sortAndBound vector of boolean that indicates if we must or
 * not do sortAndBound in each step.
 * @param m the info measure used  for pruning the trees (1|2).
 * @param set a nodelist to constrain the triangulation sequence.
 */

public Penniless(Bnet b, Evidence e, double[] lp, double[] llp,double[] lsp,
	  int[] ls, boolean[] sortAndBound,int m, NodeList set) {
  
  Triangulation triang;
  RelationList rel1, rel2, ir, irTree;
  NodeList numeration = new NodeList();
  int i;
  PotentialTree pt;
  PotentialMTree pmt;
  Relation newRel, rel;
  
  observations = e;
  network = b;
  positions = new Hashtable();  

  binTree = new JoinTree(b,set);
  
  irTree = getInitialRelations();
  irTree.restrictToObservations(observations);
  //irTree.enterEvidence(observations);  
  for (i=0 ; i<irTree.size() ; i++) {
    ((PotentialTree)irTree.elementAt(i).getValues()).limitBound(llp[0]);
  }

  ir = new RelationList();
  for (i=0 ; i<irTree.size() ; i++) {
    rel = irTree.elementAt(i);
    newRel = new Relation();
    newRel.setVariables(rel.getVariables().copy());
    pt=(PotentialTree)rel.getValues();
    //pmt = new PotentialMTree(pt);
    newRel.setValues(pt);//era pmt
    newRel.setKind(rel.getKind());
    ir.insertRelation(newRel);
  }

  // it is in the next call when potentials are transformed
  // into potentialMTrees
  binTree.initMultipleTrees(ir,sortAndBound[0],lp[0],ls[0]);

  //marginalCliques = binTree.Leaves(ir);

  binTree.binTree();
 
  setMaximumSizes(ls);
  setSortAndBound(sortAndBound);
  setLimitForPruning(lp);
  setLowLimitForPruning(llp);
  setLimitSumForPruning(lsp);
  setInfoMeasure(m);
  kindOfApprPruning=MultipleTree.AVERAGE_APPROX;
  binTree.setLabels();
}


    
/**
 * Gets the join tree.
 * @return the join tree.
 */

public JoinTree getJoinTree() {

  return (binTree);
}


/**
 * Sets the join tree.
 * @param jt the join tree.
 */

public void setJoinTree(JoinTree jt) {

  binTree = jt;
}


/**
 * Sets the kind of prunning
 * @see <code>elvira.potential.MultipleTree</code> for possible values
 */
public void setKindOfApprPruning(String kind){
  if(kind.equals("AVERAGE")){
    kindOfApprPruning=MultipleTree.AVERAGE_APPROX;
  }
  else if(kind.equals("ZERO")){
    kindOfApprPruning=MultipleTree.ZERO_APPROX;
  }
  else if(kind.equals("AVERAGEPRODCOND")){
    kindOfApprPruning=MultipleTree.AVERAGEPRODCOND_APPROX;
  }
  else{
    System.out.println("Error in Penniless.setKindOfApprPruning: ilegal value for kind="+kind);
    System.exit(1);
  }
}


/**
 * Procedure to obtain the probability of the observed evidence.
 */

public double obtainEvidenceProbability() {

  NodeJoinTree root;
  
  initMessages();
  root = binTree.elementAt(0);
  
  navigateUp(root);

  return binTree.elementAt(0).getNodeRelation().getValues().totalPotential();
}



/**
 * Procedure to obtain the probability of the observed evidence.
 * In this case, as Penniless propagation has been carried out 
 * previously, evidence can be computed by summing the potential
 * obtained as the combination of roor and its incomming messages
 */

public double obtainEvidenceProbabilityFromRoot() {

  NodeJoinTree root,node;
  PotentialMTree pmt;
  int i;  
  NeighbourTreeList ntl;
  Relation r;
  Potential pot;
 

  //initMessages();
  root = binTree.elementAt(0);
  ntl = root.getNeighbourList();

  pmt = new PotentialMTree(root.getNodeRelation().getVariables());
  pmt.setTree(MultipleTree.unitTree());

  pmt = pmt.combine((PotentialMTree)root.getNodeRelation().getValues());
  for(i=0; i<ntl.size(); i++){
    node = ntl.elementAt(i).getNeighbour();
    r = ntl.elementAt(i).getMessage();
    pot = r.getOtherValues();
    pmt = (PotentialMTree)pmt.combine((PotentialMTree)pot);
  }

  return pmt.totalPotential();
}


/**
 * Sets the limit for pruning.
 * @param lp the information limit for pruning.
 */

public void setLimitForPruning(double[] lp) {
  
 limitForPruning = lp;
}

/**
 * Sets the lowLimit for pruning.
 * @param llp the information lowLimit for pruning.
 */

public void setLowLimitForPruning(double[] llp) {
  
 lowLimitForPruning = llp;
}

/**
 * Sets the limit sum for pruning.
 * @param lsp the limit value for pruning.
 */

public void setLimitSumForPruning(double[] lsp) {
  
  limitSumForPruning = lsp;
}

/**
 * Sets the maximum sizes of multiple trees at each stage of the algorithm 
 * of propagation.
 * @param ls an array with the maximum sizes.
 */

public void setMaximumSizes(int[] ls) {
 
  int i;
  
  stages = ls.length;
  maximumSize = new int[stages];
  
  for (i=0 ; i<stages ; i++)
    maximumSize[i] = ls[i];
}


/**
 * Sets <code>sortAndBound</code>  at each stage of the propagation
 * algorithm.
 * @param sAB an array of values to set in <code>sortAndBound</code>.
 */

public void setSortAndBound(boolean[] sAB) {
 
  int i;
  
  stages = sAB.length;
  sortAndBound = new boolean[stages];
  
  for (i=0 ; i<stages ; i++)
    sortAndBound[i] = sAB[i];  
}


/**
 * Sets the method of propagation.
 * @param m the method to calculate the info measure to approximate trees.
 * @see infoMeasure
 */

public void setInfoMeasure(int m) {
  
  infoMeasure = m;
}


/**
 * Gets the kind of info measure to approximate trees.
 * @see infoMeasure
 * @return the kind of info measure to approximate trees.
 */

public int getInfoMeasure() {

  return infoMeasure;
}


/**
 * Initializes all the messages to 1, except those corresponding
 * to leaf nodes, which will contain the potential in the
 * node. <P>
 * Messages between cliques are marked as not exact.
 */
  
public void initMessages() {
 
  RelationList ir;
  Relation r, r2, message, otherMessage;
  int i, j;
  PotentialMTree pot, pot2;
  Vector leaves;
  NodeJoinTree node, otherNode;
  NeighbourTree neighbour, nt;
  NeighbourTreeList ntl;
  NodeList nl1, nl2;
  

  for (i=0 ; i<binTree.getJoinTreeNodes().size() ; i++) {
    // Set the potentials in the cliques to 1.
    node = binTree.elementAt(i);    
    r = node.getNodeRelation();
    if ((r.getValues() == null) || (!r.getValues().getClassName().equals("PotentialMTree"))) {
      pot = new PotentialMTree(r.getVariables());
      pot.setTree(MultipleTree.unitTree());
      r.setValues(pot);
    }
    
    // Now set the messages
    ntl = node.getNeighbourList();
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      r = nt.getMessage();
      pot = new PotentialMTree(r.getVariables());
      pot.setTree(MultipleTree.unitTree());
      pot.setExact(false);
      r.setValues(pot);
      pot = new PotentialMTree(r.getVariables());
      pot.setTree(MultipleTree.unitTree());
      pot.setExact(false);
      r.setOtherValues(pot);
    }
  }
}

/**
 * Carries out a propagation.
 *
 * @param exactFile the name of the file with the exact results.
 * @param resultFile the name of the file where the errors will
 *        be stored.
 */

public void propagate(String resultFile) throws ParseException, IOException {

  NodeJoinTree root;
  NodeList variables;
  int i;
  Date date;
  double timePropagating;


  // Necessary for identifying the nodes during the message passing.
  binTree.setLabels();
  
  // Initialize messages
  System.out.println("Initializing messages");
  date = new Date();
  timePropagating = (double)date.getTime();
  initMessages();
  date = new Date();
  timePropagating = ((double)date.getTime()-timePropagating) / 1000;
  System.out.println("Time Initializing messages: "+ timePropagating);

 
  System.out.println("Starting propagation");
  
  // Perform the propagation
  
  root = binTree.elementAt(0);
  
  currentStage = 0;
  if (stages > 0) {
    // First stage: navigates from leaves upwards.
    System.out.println("Propagacion Etapa "+currentStage);
    date = new Date();
    timePropagating = (double)date.getTime(); 
    navigateUp(root);
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
    System.out.println("Time navigateUp: "+ timePropagating);
    stages--;
    currentStage++;
  }

  while(stages > 2) {
    // Intermediate stages: navigates down and up.
    System.out.println("Propagacion Etapa "+stages);    
    date = new Date();
    timePropagating = (double)date.getTime();
    navigateDownUp(root);
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
    System.out.println("Time navigateDownUp: "+ timePropagating);
    stages -= 2;
    currentStage += 2;
  }

  if (stages == 1){
    // Last stage: navigates down.
    System.out.println("Propagacion Etapa "+stages);    
    date = new Date();
    timePropagating = (double)date.getTime();
    navigateDown(root);
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
    System.out.println("Time navigateDown: "+ timePropagating);
    currentStage++;
  }
  else { // stages == 2
    System.out.println("Propagacion Etapa "+stages);    
    date = new Date();
    timePropagating = (double)date.getTime();
    navigateDownUpForcingDown(root);
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
    System.out.println("Time navigateDownUpForcingDown: "+ timePropagating);
    currentStage += 2;     
  }
    
  // End of propagation
  System.out.println("Propagation done");
  
  // Obtain the marginals for each single variable
  System.out.println("Computing marginals");

  date = new Date();
  timePropagating = (double)date.getTime();    
  computeMarginals(); 
  date = new Date();
  timePropagating = ((double)date.getTime()-timePropagating) / 1000;
  System.out.println("Time computeMarginals: "+ timePropagating);

  System.out.println("Done");
  
  saveResults(resultFile);
}


/**
 * Carries out a propagation specially for abduction.
 *
 * @param steps the number of stages.
 */

public void propagate(int steps) {

  NodeJoinTree root;
  NodeList variables;
  int i;

  // Necessary for identifying the nodes during the message passing.
  binTree.setLabels();
  
  // Initialize messages
  initMessages();

  // Perform the propagation
  
  root = binTree.elementAt(0);
  System.out.println("Starting penniless phase");
  currentStage = 0;
  if (steps > 0) {
    // First stage: navigates from leaves upwards.
    System.out.println("Propagation stage "+currentStage);
    navigateUp(root);
    steps--;
    currentStage++;
  }

  while(steps > 2) {
    // Intermediate stages: navigates down and up.
    System.out.println("Propagation stage "+currentStage);
    navigateDownUp(root);
    steps -= 2;
    currentStage += 2;
  }

  if (stages == 1){
    // Last stage: navigates down.
    System.out.println("Propagation stage "+currentStage);    
    navigateDown(root);
    currentStage++;
  }
  else { // stages == 2
    if (steps ==2) {
    System.out.println("Propagation stage "+currentStage);    
    navigateDownUpForcingDown(root);
    currentStage += 2;
    }
  }
    

  // End of propagation
  System.out.println("Penniless phase done");
}



/**
 * Send messages from root (sender) to leaves, and then from leaves
 * to root.
 * The method does not navigate throw a branch if the message in the
 * opposite direction (<code>getOtherValues()</code>) is exact.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

private void navigateDownUp(NodeJoinTree sender) {
  
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
	sendMessage(sender,other,false);
      navigateDownUp(sender,other);
    }
  }
}	       


/**
 * Sends messages from root (sender) to leaves, and then from leaves
 * to root, through the branch towards node <code>recipient</code>.
 * This method do not navigate throw a branch if the message in the opposite
 * direction (<code>getOtherValues()</code>) is exact.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 */

private void navigateDownUp(NodeJoinTree sender, NodeJoinTree recipient) {

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
	  sendMessage(recipient,other,false);	
	navigateDownUp(recipient,other);
      }
    }
  }
  // if opposite message is not exact
  if(!(list.getMessage(sender).getOtherValues().getExact()))  
    sendMessage(recipient,sender,true);
}


/**
 * Send messages from root (sender) to leaves, and then from leaves
 * to root.
 * When it navigates  down,  if the message in the opposite direction
 * (<code>getOtherValues()</code>) in a branch is exact, then it will 
 * continue with <code>navigateDown</code> over that branch
 * (and not doing the ascending step).
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

private void navigateDownUpForcingDown(NodeJoinTree sender) {

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
      // if previous message was not exact
      if (!(sep.getValues().getExact()))
	sendMessage(sender,other,false);
      navigateDownUpForcingDown(sender,other);
    }
    else { 
      // if previous message was not exact
      if (!(sep.getValues().getExact()))
	sendMessage(sender,other,false);
      navigateDown(sender,other);
    }
  }
}	       


/**
 * Sends messages from root (sender) to leaves, and then from leaves
 * to root, through the branch <code>recipient</code>.
 * When it navigates  down,  if the message in the opposite direction
 * (<code>getOtherValues()</code>) in a branch is exact, then it will 
 * continue with <code>navigateDown</code> over that branch
 * (and not doing the ascending step).
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 */

private void navigateDownUpForcingDown(NodeJoinTree sender,
				       NodeJoinTree recipient) {

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
	if (!(sep.getValues().getExact()))
	  sendMessage(recipient,other,false);	
	navigateDownUpForcingDown(recipient,other);
      }
      else {
	if (!(sep.getValues().getExact()))
	  sendMessage(recipient,other,false);	
	navigateDown(recipient,other);
      }	
    }
  }
  sendMessage(recipient,sender,true);
}


/**
 * Sends messages from leaves to root (sender).
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

private void navigateUp(NodeJoinTree sender) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  
  list = sender.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    navigateUp(sender,other);
  }
}	       


/**
 * Sends messages from leaves to root (sender) through the branch recipient.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 */

private void navigateUp(NodeJoinTree sender, NodeJoinTree recipient) {

  NeighbourTreeList list;
  NodeJoinTree other;
  int i;

  // Nodes to which the message will be sent downwards.
  list = recipient.getNeighbourList();

  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    
    if (other.getLabel() != sender.getLabel()) {
      navigateUp(recipient,other);
    }
  }
  
  sendMessage(recipient,sender,false);
}


/**
 * Sends messages from root (sender) to leaves.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

public void navigateDown(NodeJoinTree sender) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  
  list = sender.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    sendMessage(sender,other,false);
    navigateDown(sender,other);
  }
}


/**
 * Send messages from root (sender) to leaves through the brach recipient.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 */

public void navigateDown(NodeJoinTree sender, NodeJoinTree recipient) {

  NeighbourTreeList list;
  NodeJoinTree other;
  int i;

  // Nodes to which the message will be sent downwards.
  list = recipient.getNeighbourList();

  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();

    if (other.getLabel() != sender.getLabel()) {      
      sendMessage(recipient,other,false);
      navigateDown(recipient,other);
    }
  }
}


/**
 * Gets the initial relations in the network.
 * @return the initial relations present in the network, as a
 * <code>RelationList</code>.
 */

public RelationList getInitialRelations() {
  
  Relation rel, newRel;
  RelationList list;
  PotentialTree pt;
  PotentialMTree pmt;
  int i;
 
  list = new RelationList();
  
  for (i=0 ; i<network.getRelationList().size() ; i++) {
    rel = (Relation)network.getRelationList().elementAt(i);
    newRel = new Relation();
    newRel.setVariables(rel.getVariables().copy());
    if (rel.getValues().getClassName().equals("PotentialTable")) {
      pt = ((PotentialTable)rel.getValues()).toTree();
    }
    else
      pt = (PotentialTree)rel.getValues();
    
    newRel.setValues(pt);
    newRel.setKind((int)(rel.getKind()));
    list.insertRelation(newRel);
  }
  
  return list;
}


/**
 * Computes the marginals after a propagation and put them into the
 * instance variable <code>results</code>. Sets <code>positions</code>
 * for each variable, to indicate the index lo locate the variable
 * in <code>Vector results</code>.
 */

public void computeMarginals() {
  
  int i, j, k, nv, pos;
  Vector leaves, marginal;
  NodeList variables;
  Relation r1, r2;
  PotentialMTree pot;
  PotentialTable table;
  NodeJoinTree temp;
  NodeList l;
  FiniteStates v;
  NeighbourTree nt;
  int posResult;
  
  leaves = binTree.getLeaves();
  variables = network.getNodeList();
  nv = variables.size();
  
  posResult = 0;
  for (i=0 ; i<nv ; i++) {
    v = (FiniteStates)variables.elementAt(i);
    
    j = 0;
    temp = (NodeJoinTree)marginalCliques.get(v);
    
    if (temp != null) {
      r1 = temp.getNodeRelation();
      pot = (PotentialMTree)r1.getValues();
 
      for (k=0 ; k<temp.getNeighbourList().size() ; k++) {
	nt = temp.getNeighbourList().elementAt(k);
	r2 = nt.getMessage();
	pot = pot.combine((PotentialMTree)r2.getOtherValues());
      }
      marginal = new Vector();
      marginal.addElement(v);
      pot = (PotentialMTree)(pot.marginalizePotential(marginal));
      pot.normalize();
      results.addElement(pot);
      
      positions.put(v,new Integer(posResult));
      posResult++;
    }
  }
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
 *
 * @param sender the node that sends the message.
 * @param recipient the node that receives the message.
 * @param takeNumberNextStage <code>true</code> if we must do
 * <code>sortAndBound</code> over the message, with
 * <code>maximumSize[currentStage+1]</code> and <code>false</code> if we
 * must do <code>sortAndBound</code> over the message, with
 * <code>maximumSize[currentStage]</code>.
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient,
			boolean takeNumberNextStage) {
  
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
  
  pot = (PotentialMTree)(pot.marginalizePotential(separator)); 
  
  pot = (PotentialMTree)pot.conditional((Potential)incoming);
  pot.setExact(isExact);

  if (takeNumberNextStage) {
    step=currentStage+1;
  }
  else{
    step=currentStage;
  }
  if(infoMeasure==1) {
    pot.conditionalLimitBound(kindOfApprPruning,limitForPruning[step],
               lowLimitForPruning[step],limitSumForPruning[step],infoMeasure);
  }
  else if(infoMeasure==2){
    if(currentStage==0){
      pot.conditionalLimitBound(kindOfApprPruning,limitForPruning[step],
               lowLimitForPruning[step],0.0,infoMeasure);   
    }
    else{
      pot.conditionalLimitBound(kindOfApprPruning,limitForPruning[step],
               lowLimitForPruning[step],limitSumForPruning[step],infoMeasure);
    }
  }
  else {
    System.out.println("Error in Penniless.sendMessage(NodeJoinTree,NodeJoinTree,boolean,int): infoMeasure="+infoMeasure);
    System.exit(1);
  }

  if(stages<3){
    if(pot.getNormalizationFactor()<minNormalizationFactor)
      minNormalizationFactor=pot.getNormalizationFactor();
    if(pot.getNormalizationFactor()>maxNormalizationFactor)
      maxNormalizationFactor=pot.getNormalizationFactor();
  }
  /*if(down)
    pot.conditionalLimitBound(kindOfApprPruning,limitForPruning,
               lowLimitForPruning,limitSumForPruning,infoMeasure);
  else
    pot.conditionalLimitBound(kindOfApprPruning,limitForPruning,
               lowLimitForPruning,0.0,infoMeasure);*/
  //pot.print();
  
  if (sortAndBound[step]) {
    pot = pot.conditionalSortAndBound(maximumSize[currentStage+1],infoMeasure);
    pot.conditionalLimitBound(kindOfApprPruning,limitForPruning[step],
          lowLimitForPruning[step],limitSumForPruning[step],infoMeasure);
  }
  
  // Now update the messages in the join tree.
  
  outwards.setValues(pot);
  inwards.setOtherValues(pot);
}

} // End of class
