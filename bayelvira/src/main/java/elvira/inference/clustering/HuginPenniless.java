/* HuginPenniless.java */

package elvira.inference.clustering;

import java.util.Vector;
import java.io.*;
import java.util.Date;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialMTree;
import elvira.potential.MultipleTree;
import elvira.potential.PotentialTable;
import elvira.potential.Potential;
import elvira.inference.clustering.HuginPropagation;
import elvira.inference.clustering.ApproximateHuginPropagation;

/**
 * Class HuginPenniless. Implements the the Penniless propagation algorithm
 * based on Hugin-like messages.
 *
 * @since 7/03/03
 */

public class HuginPenniless extends ApproximateHuginPropagation {


/**
 * Number of propagation stages.
 */

public int stages;



/**
 * Program for performing experiments.
 * The arguments are as follows.
 * 1. Input file: the network.
 * 2. Output file.
 * 3. The value used as limit for pruning.
 * 4. The number of propagation stages.
 * 5. Evidence file.
 * If the evidence file is omitted, then no evidences are
 * considered
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  HuginPenniless hp;
  int i;
  double limit;
  int numStages;
 
  
  if (args.length<4){
    System.out.println("Too few arguments. The arguments are:");
    System.out.println("\tNetwork output-file limit-for-prunning number-of-stages [evidence-file]\n");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);

    limit = (Double.valueOf(args[2])).doubleValue();
    numStages = (Integer.valueOf(args[3])).intValue();
    

    if (args.length == 5) {
      evidenceFile= new FileInputStream(args[4]);
      e = new Evidence(evidenceFile,b.getNodeList());
    }
    else
      e = new Evidence();
   
    hp = new HuginPenniless(b,e,limit,numStages);


    hp.propagate(hp.getJoinTree().elementAt(0),"no");
    hp.saveResults(args[1]);
  }
}



/**
 * Set the number of stages.
 * @param n the number of propagation stages.
 */
   
public void setStages(int n) {

  stages = n;
}


/**
 * Get the number of stages.
 * @return the number of propagation stages.
 */
   
public int getStages() {

  return (stages);
}


/**
 * Constructor.
 * @param b the Bnet used for the compilation of the joinTree
 * @param e the evidence
 * @param limit the limit for pruning the trees.
 * @param numStages the number of propagation stages.
 */

public HuginPenniless(Bnet b, Evidence e, double limit, int numStages) {

  super(b,e,"mtrees");

  setLimitForPruning(limit);
  setStages(numStages);    

  jt.setLimitForPotentialPruning(limit);
}   


/**
 * Transforms a relation if it is a PotentialMTree.
 * The only thing to do is pruning the 
 * nodes which children are equal, so we use a very small value
 * for limit.
 * This method can be overcharged for special requirements.
 * @param r the relation to be transformed
 */

public Relation transformRelation(Relation r) {

  PotentialMTree pot;

  pot = (PotentialMTree)r.getValues();
  //pot.conditionalLimitBound(MultipleTree.AVERAGE_APPROX,
  //			    getLimitForPruning(),1E-6,0,2);
  pot.limitBound(getLimitForPruning());
  r.setValues(pot);

  return r;
}                      


/**
 * Transforms a PotentialMTree.
 * The only thing to do is to prune the 
 * nodes whose children are equal, so we use a very small value
 * for limit.
 * This method can be overcharged for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @param pot the relation to be transformed
 */

public Potential transformPotential(Potential pot) {
    
    //((PotentialMTree)pot).conditionalLimitBound(MultipleTree.AVERAGE_APPROX,
    //						getLimitForPruning(),1E-6,0,2);
    ((PotentialMTree)pot).limitBound(getLimitForPruning());    

    return pot;
}                      


/**
 * Transforms a PotentialMTree.
 * The only thing to do is to prune the 
 * nodes whose children are equal, so we use a very small value
 * for limit.
 * This method can be overcharged for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @param pot the relation to be transformed
 */

public Potential transformPotentialByConditional(Potential pot) {
    
    ((PotentialMTree)pot).conditionalLimitBound(MultipleTree.AVERAGE_APPROX,
    						getLimitForPruning(),1E-6,0,2);
    //((PotentialMTree)pot).limitBound(getLimitForPruning());

    return pot;
}                      


/**
 * Performs an upward propagation across the join tree, taking as root the 
 * node passed as argument. If divide="no", no divisions are performed. We
 * can take advantage of this parameter when we are sure that the
 * propagation is performed just after the initialisation of the potentials,
 * because in this case we will divide by 1 and so the division has no
 * sense. 
 * 
 * In messages, <code>values</code> stores the more recent separator
 * and <code>otherValues</code> the old separator.
 *
 * NOTE: all the potentials (in cliques and in separators) are modified.
 *
 * @param root the node (<code>NodeJoinTree</code>)  used as root
 * for the propagation.
 * @param divide if "no" the divisions in the separators are not performed.
 */

public void upward(NodeJoinTree root, String divide) {

  NeighbourTreeList ntl;
  NodeJoinTree node;
  int i;
  Relation r, r2;
  Potential conditioningPot, pot, newSep, oldSep;

  ntl = root.getNeighbourList();
  
  // first we ask the messages

  for(i=0 ; i<ntl.size() ; i++) {
    node = ntl.elementAt(i).getNeighbour();
    askMessage(root,node,divide);  
  }
  
  // now, the messages are combined with the potential stored in root
  
  r = root.getNodeRelation();
  pot = r.getValues();
  
  for (i=0 ; i<ntl.size() ; i++) {
    node = ntl.elementAt(i).getNeighbour();
    r2 = ntl.elementAt(i).getMessage();
    newSep = r2.getValues();
    if (divide.equals("yes")) {
      oldSep = r2.getOtherValues();
      conditioningPot = pot.divide(oldSep);
      
      //pot.combineWithSubset(conditioningPot.combine(newSep));
      pot = conditioningPot.combine(newSep);
      
      pot = pot.conditional(conditioningPot);
      //pot = transformPotential(pot);
      pot = transformPotentialByConditional(pot);
    }
    else {
      conditioningPot = pot.copy();
      pot.combineWithSubset(newSep);
      pot = pot.conditional(conditioningPot);

      //pot = transformPotential(pot);
      pot = transformPotentialByConditional(pot);
    }
    r.setValues(pot);
  }
}


/**
 * Requests a message to a node which recursively does the same until 
 * a leaf is reached, point at which the information is propagated backward.
 * 
 * @param sender the node (<code>NodeJoinTree</code>) which ask for
 * the message
 * @param recipient the node (<code>NodeJoinTree</code>) asked
 * for the message
 * @param divide if "no" no division is performed in the separator
 */

public void askMessage(NodeJoinTree sender, NodeJoinTree recipient,
		       String divide) {

  NeighbourTreeList ntl;
  NeighbourTree nt;
  NodeJoinTree node;
  int i, father;
  Relation r, r2;
  Potential conditioningPot, pot, newSep, oldSep, message;


  ntl = recipient.getNeighbourList();
  if (ntl.size() != 1) { // recipient is not a leaf, ask for more messages

    for (i=0 ; i<ntl.size() ; i++) {
      node = ntl.elementAt(i).getNeighbour();
      if (node.getLabel() != sender.getLabel())
         askMessage(recipient,node,divide);  
    } 
  }

  // combination in recipient
  
  r = recipient.getNodeRelation();
  pot = r.getValues();
  for(i=0 ; i<ntl.size() ; i++) {
    node = ntl.elementAt(i).getNeighbour();
    if (node.getLabel() != sender.getLabel()) {
      r2 = ntl.elementAt(i).getMessage();
      newSep = r2.getValues();
      if (divide.equals("yes")) {
	oldSep = r2.getOtherValues();
	conditioningPot = pot.divide(oldSep);
	
	//pot.combineWithSubset(conditioningPot.combine(newSep));
        pot = conditioningPot.combine(newSep);	

	pot = pot.conditional(conditioningPot);
      }
      else {
	conditioningPot = pot.copy();
	pot.combineWithSubset(newSep);
	pot = pot.conditional(conditioningPot);
      }
      //pot = transformPotential(pot);
      pot = transformPotentialByConditional(pot);
      r.setValues(pot);
    }
  }
  
  // sending (storing) message to sender (father)
  // pot contains the potential stored in recipient 
  
  father = ntl.indexOf(sender);
  nt = ntl.elementAt(father);
  newSep = pot.marginalizePotential(nt.getMessage().getVariables().toVector());
  //newSep = transformPotential(newSep);
  if (divide.equals("no")) {
    nt.getMessage().setValues(newSep);
  }
  else {   
    nt.getMessage().setOtherValues(nt.getMessage().getValues());
    nt.getMessage().setValues(newSep);
  } 
}


/**
 * Sends a message to a node which recursively does the same until 
 * a leaf is reached.
 * 
 * @param sender the node (<code>NodeJoinTree</code>) which asks for
 * the message.
 * @param recipient the node (<code>NodeJoinTree</code>) asked for the
 * message.
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient) {

  NeighbourTreeList ntl;
  NeighbourTree nt;
  NodeJoinTree node;
  int i, father;
  Relation r, r2;
  Potential conditioningPot, pot, newSep, oldSep;


  // combination of the potential with the message sent by sender

  r = recipient.getNodeRelation();
  pot = r.getValues();
  
  ntl = recipient.getNeighbourList();
  father = ntl.indexOf(sender);
  nt = ntl.elementAt(father);
  newSep = nt.getMessage().getValues();
  oldSep = nt.getMessage().getOtherValues();

  conditioningPot = pot.divide(oldSep);
  
  //pot.combineWithSubset(conditioningPot.combine(newSep));
  pot = conditioningPot.combine(newSep);  

  pot = pot.conditional(conditioningPot);
  
  //pot = transformPotential(pot);
  pot = transformPotentialByConditional(pot);

  r.setValues(pot);
  
  // now send the messages to the children

  if (ntl.size() != 1) { // the node is not a leaf 
    for (i=0 ; i<ntl.size() ; i++) {
      node = ntl.elementAt(i).getNeighbour();
      if (node.getLabel() != sender.getLabel()) {
        nt = ntl.elementAt(i);
        r2 = nt.getMessage();
        nt.getMessage().setOtherValues(nt.getMessage().getValues());
        newSep = pot.marginalizePotential(nt.getMessage().getVariables().toVector());
        //newSep = transformPotential(newSep); 
        nt.getMessage().setValues(newSep);
        sendMessage(recipient,node);
      }
    }
  } 
}


/**
 * Performs a Hugin-Penniless propagation.
 *
 * @param root the node (<code>NodeJoinTree</code>) used as root for
 * the upward and downward phases.
 * @param divide "yes" if division are carried out in the upward phase and
 *        "no" in other case.
 */

public void propagate(NodeJoinTree root, String divide) {

  Date d;
  double time;
  int stagesLeft;


  jt.sortVariables(network.getNodeList());
  if (getTypeOfPotential().equals("tables"))
    jt.initTables(network);
  else {
    if (getTypeOfPotential().equals("trees"))
      jt.initTrees(network);
    else {
      if (getTypeOfPotential().equals("mtrees"))
	  jt.initMultipleTrees(network);
      else {
        System.out.println(getTypeOfPotential() + " is not avalilabe in HuginPenniless");
        System.exit(0);
      }
    }
  }
  transformRelationsInJoinTree();
  jt.setLabels();
  obtainInterest();


  System.out.println("Computing posterior distributions...");
  d = new Date();
  time = (double)d.getTime();
  
  if (observations.size() > 0)
    instantiateEvidence( );  
  
  initHuginMessages( );  

  stagesLeft = getStages();
  
  while (stagesLeft > 0) {
    if (stagesLeft == getStages()){
      upward(root,divide);
      //jt.display();
    }
    else // Only in the first stage the division can be omitted.
      upward(root,"yes");
    stagesLeft--;
    if (stagesLeft > 0)
      downward(root);
    stagesLeft--;
  }

  // calculation of the posterior distribution
  getPosteriorDistributions();
  
  // showing messages

  d = new Date();
  time = ((double)d.getTime() - time) / 1000;
  System.out.println("Posterior distributions computed.");
  System.out.println("Time (secs): " + time);
}


} // end of class
