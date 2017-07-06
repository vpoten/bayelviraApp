/* AbductiveInferenceNilsson.java */

package elvira.inference.abduction;

import java.io.*;
import java.util.Date;
import java.util.Vector;
import elvira.*;
import elvira.inference.clustering.*;
import elvira.tools.PropagationStatistics;
import elvira.parser.ParseException;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialTable;
import elvira.potential.Potential;


/**
 * Class <code>AbductiveInferenceNilsson</code>.
 * Implements Nilsson's algorithm for finding the K most probable
 * explanations. It works over a join tree.
 *
 * @since 15/9/2000
 */

public class AbductiveInferenceNilsson extends AbductiveInference {


/**
 * The join tree.
 */

public JoinTree jt;  

/**
 * A string indicating the type of potential to be used in the propagation.
 */

public String typeOfPotential;   

/**
 * A node list containing the deletion sequence to be used. Default is empty. 
 */

protected NodeList deletionSequence;

/**
 * Creates a new propagation.
 * @param b a belief network.
 * @param e an evidence.
 */

public AbductiveInferenceNilsson(Bnet b, Evidence e, String pot) {

  nExplanations = 1;
  partial = false;
  explanationSet = new NodeList();
  interest = new NodeList();
  kBest = new Vector();
  setProblem("K Most Probable Explanations");
  setMethod("Nilsson");
  network = b;
  typeOfPotential = new String(pot); 
  jt = new JoinTree( );
  observations = e;  
}


/**
 * Gets the join tree.
 * @return the join tree.
 */

JoinTree getJoinTree() {
  
  return jt;
}

/**
 * Set the deletion sequence
 * @param sigma a NodeList containing the deletion sequence to be used
 */

public void setDeletionSequence(NodeList sigma){
  deletionSequence = sigma;
}

/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows:
 * <ol>
 * <li> Input file: the network.
 * <li> Output file.
 * <li> Number of explanations.
 * <li> Method (total,size,restrictedSize,ratio). The last three only have
 *            effect if there is an explanation set.
 * <li> Type of potential (tables or trees).
 * <li> Evidence file.
 * <li> Interest file (contains the interest variables - explanation set)
 * </ol>
 * The evidence file and the interest file are optional.
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile, interestFile;
  AbductiveInferenceNilsson ain;
  int i;
  NodeList expSet;

  Vector explanations,postProb;
  Explanation best;

  
  if (args.length < 5){
    System.out.println("Too few arguments, the arguments are:");
    System.out.println("\tNetwork output-file K (total|size|restrictedSize|subtree)");
    System.out.println("\t(tables|trees) [evidence-file] [interest-file]");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);

    if ( !( (args[4].equals("tables")) || (args[4].equals("trees")) ) ) {
      System.out.println("TypeOfPotential have to be in {tables,trees}");
      System.exit(0);
    }

    expSet = new NodeList();

    if (args.length == 6) {
      // trying if args[6] is the evidence
      try {
        evidenceFile = new FileInputStream(args[5]);
        e = new Evidence(evidenceFile,b.getNodeList());
      }catch (ParseException pe){
        interestFile = new FileInputStream(args[5]);
        expSet = new NodeList(interestFile,b.getNodeList());
 
        e = new Evidence();
      }      
    }
    else e = new Evidence();

    if (args.length == 7) {
      evidenceFile = new FileInputStream(args[5]);
      e = new Evidence(evidenceFile,b.getNodeList());

      interestFile = new FileInputStream(args[6]);
      expSet = new NodeList(interestFile,b.getNodeList());
    }


    ain = new AbductiveInferenceNilsson(b,e,args[4]);
    ain.setExplanationSet(expSet);
    if (expSet.size() > 0)
      ain.setPartial(true);
    ain.setNExplanations((Integer.valueOf(args[2])).intValue());
    ain.setPropComment(args[3]);

    if (args[3].equals("exhaustive"))
      ain.exhaustive();
    else ain.propagate(args[1]);

    ain.saveResults(args[1]);
    //System.in.read();

    // nuevo

    explanations = ain.getKBest();
    best = (Explanation) explanations.elementAt(0);
    postProb = best.toPosteriorProbability(b.getNodeList(),e);
    //for(i=0;i<postProb.size();i++)
    //  ((PotentialTable)postProb.elementAt(i)).print();
  }
}


/**
 * Carries out a propagation.
 * @param outputFile the file where the K MPEs will be stored.
 */

public void propagate(String OutputFile) {

  double extraSize, pEvidence, pBest, t, time;
  HuginPropagation hp;
  Explanation exp;
  int i;
  AIPartitionElementList pl;
  PropagationStatistics stat;
  Vector potentials;
  Date d;  


  stat = this.getStatistics();

  System.out.println("Computing best explanation ...");
  d = new Date();
  time = (double)d.getTime();       

  if (!this.getPartial()) {

    // first, we calculate the probability of the evidence
    // using a hugin propagation
 
    NodeList delSequence = new NodeList();
    hp = new HuginPropagation(network,observations,typeOfPotential,delSequence);
    if (observations.size() > 0) {
      pEvidence = hp.obtainEvidenceProbability("yes");
    }
    else
      pEvidence = 1.0;
    System.out.println("Probabilidad de la evidencia: " + pEvidence);
 
    // second, we perform an upward propagation using max as 
    // marginalization operator.

    // we use a HuginPropagation for the initialization of the joinTree

    hp.getJoinTree().sortVariables(network.getNodeList());

    if (typeOfPotential.equals("tables"))
      hp.getJoinTree().initTables(network);
    else if (typeOfPotential.equals("trees"))
      hp.getJoinTree().initTrees(network);
    else {
      System.out.println(typeOfPotential + 
         " is not avalilabe in AbductiveInferenceNilsson");
      System.exit(0);
    }
    hp.transformRelationsInJoinTree();  
    
    hp.getJoinTree().setLabels();

    if (observations.size() > 0)
      hp.instantiateEvidence();
    hp.initHuginMessages();

    // now we get the join tree from huginPropagation  

    jt = hp.getJoinTree();

  }
  else{ // partial abductive inference

    // first, we build the join tree

    if (this.getPropComment().equals("subtree")) {
      hp = new HuginPropagation(observations,network,typeOfPotential);
      jt.treeOfCliques(network,explanationSet);
      hp.setJoinTree(jt);
    }
    else
      hp = new HuginPropagation(network,observations,typeOfPotential);

    hp.getJoinTree().sortVariables(network.getNodeList());

    if (typeOfPotential.equals("tables"))
      hp.getJoinTree().initTables(network);
    else if (typeOfPotential.equals("trees"))
      hp.getJoinTree().initTrees(network);
    else {
      System.out.println(typeOfPotential + 
         " is not avalilabe in AbductiveInferenceNilsson");
      System.exit(0);
    }
    hp.transformRelationsInJoinTree();  

    hp.getJoinTree().setLabels();

    // if evidence restrict tables

    if (observations.size() > 0)
      hp.instantiateEvidence();
    hp.initHuginMessages();



    // now we get the joinTree from huginPropagation  

    jt = hp.getJoinTree();

    // data for statistics
    jt.calculateStatistics();
    this.statistics.setJTInitialSize(jt.getStatistics().getJTSize());        

    // outer restriction

    jt.outerRestriction(explanationSet,"no","no");
    jt.setLabels();

    // storing potentials for posterior use
   
    potentials = jt.storePotentials();

    // calculating evidence probability

    if (observations.size() > 0) {
      hp.setJoinTree(jt);
      pEvidence = hp.obtainEvidenceProbability("no");
      jt = hp.getJoinTree();
    }
    else
      pEvidence = 1.0;
    System.out.println("Probabilidad de la evidencia: " + pEvidence);

    // restoring potentials
    
    jt.restorePotentials(potentials);

    // Now, we do inner restriction
  
    if (!(this.getPropComment().equals("subtree"))) {
      extraSize = jt.innerRestriction(explanationSet,"no",this.getPropComment(),
                          network.getNodeList());
      jt.setLabels();
      stat.setJTExtraSize(extraSize);
    }
  }


  // the rest of the process is common for partial and total abductive inference

  upward(jt.elementAt(0),"no");

  /*DEBUG
  try{
    System.out.println("Printing the tree after upward propagation");
    jt.display2();
  } catch (IOException ioe){
    System.out.println("Error al intentar escribir un jointree");
  }
  END DEBUG */


  // Obtaining the kBest explanations
  
  pBest = getBestExplanation( );

  if (nExplanations > 1){
    pl = initPartitionList(pBest);
    //pl.print();
    for (i=1; i<nExplanations; i++){
      refinePartitionList(pl);
      //pl.print();
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
 * Performs an upward propagation across the join tree, taking as root the 
 * node passed as argument. If divide="no", no divisions are performed. We
 * can take advantage of this parameter when we are sure that the
 * propagation is performed just after the initialisation of the potentials,
 * because in this case we will divide by 1 and so the division has no
 * sense. 
 * 
 * In messages, <code>values</code> stores the more recent separator and
 * <code>otherValues</code> the old separator.
 * 
 * MAXIMUM IS USED AS MARGINALIZATION OPERATOR.
 *
 * NOTE: all the tables (potentials and messages) are modified.
 *
 * @param root the node used as root for the propagation.
 * @param divide if "no" the division in the separators are not performed.
 */

public void upward(NodeJoinTree root, String divide) {

  NeighbourTreeList ntl;
  NodeJoinTree node;
  int i;
  Relation r, r2;
  Potential pot, newSep, oldSep, temp;

  ntl = root.getNeighbourList();
  
  // first we ask the messages

  for (i=0 ; i<ntl.size() ; i++) {
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
      temp = newSep.divide(oldSep);
      temp = transformPotentialAfterCombination(temp);
      pot.combineWithSubset(temp);
      pot = transformPotentialAfterCombination(pot);
    }
    else {
      pot.combineWithSubset(newSep);
      pot = transformPotentialAfterCombination(pot);
    }
         
    r.setValues(pot);
  }
}


/**
 * Requests a message to a node which recursively does the same until 
 * a leaf is reached, point at which the information is propagated backward.
 * 
 * MAXIMUN IS USED AS MARGINALIZATION OPERATOR.
 *
 * @param sender the node which ask for the message.
 * @param recipient the node asked for the message.
 * @param divide if "no" no division is performed in the separator.
 */

public void askMessage(NodeJoinTree sender, NodeJoinTree recipient,
		       String divide) {

  NeighbourTreeList ntl;
  NeighbourTree nt;
  NodeJoinTree node;
  int i, parent;
  Relation r, r2;
  Potential pot, newSep, oldSep, message, temp;


  ntl = recipient.getNeighbourList();
  if (ntl.size() != 1) { // recipient is not a leaf, ask more messages

    for (i=0 ; i<ntl.size() ; i++) {
      node = ntl.elementAt(i).getNeighbour();
      if (node.getLabel() != sender.getLabel())
	askMessage(recipient,node,divide);  
    }    
  }

  // combination in recipient
  
  r = recipient.getNodeRelation();
  pot = r.getValues();
  for (i=0 ; i<ntl.size() ; i++) {
    node = ntl.elementAt(i).getNeighbour();
    if (node.getLabel() != sender.getLabel()) {
      r2 = ntl.elementAt(i).getMessage();
      newSep = r2.getValues();
      if (divide.equals("yes")) {
        oldSep = r2.getOtherValues();
        temp = newSep.divide(oldSep);
        temp = transformPotentialAfterCombination(temp);
        pot.combineWithSubset(temp);
      }
      else
	pot.combineWithSubset(newSep);
      pot = transformPotentialAfterCombination(pot);
      r.setValues(pot);
    }
  }
  
  // sending (storing) message to sender (parent)
  // pot contains the potential stored in recipient 
  
  parent = ntl.indexOf(sender);
  nt = ntl.elementAt(parent);
  newSep = pot.maxMarginalizePotential(
                     nt.getMessage().getVariables().toVector());
  newSep = transformPotentialAfterMarginalization(newSep);
  if (divide.equals("no")) {
    nt.getMessage().setValues(newSep);
  }
  else {   
    nt.getMessage().setOtherValues(nt.getMessage().getValues());
    nt.getMessage().setValues(newSep);
  }
}


/** 
 * Obtains the best explanation and sets it in the first position of
 * <code>kBest</code>.
 *
 * @return the probability P(best,observations) of the explanation.
 */

public double getBestExplanation() {

  Configuration conf, aux, aux2;
  double prob;
  int i;
  Potential pot;
  FiniteStates fs;
  NeighbourTreeList ntl;
  NodeJoinTree node;
  Relation r;
  Vector vars;
  SetVectorOperations svo = new SetVectorOperations();
  Explanation exp;

  
  // if there is not explanation set, put it as the non observed variables
  if (explanationSet.size() == 0) {
    obtainInterest();
    explanationSet = interest;
  }
  
  conf = new Configuration(explanationSet);

  // first we get the maximum probability and the subconfiguration 
  // from clique 0 (the root)

  aux = new Configuration();
  pot = jt.elementAt(0).getNodeRelation().getValues();    
  aux = pot.getMaxConfiguration(aux);
  prob = pot.getValue(aux);

  // setting values for the variables in clique 0

  for (i=0 ; i<aux.size() ; i++) {
    fs = aux.getVariable(i);
    if (explanationSet.getId(fs) != -1){ // fs is in the explanation set
      conf.putValue(fs,aux.getValue(i));
    }
  }

  // now we ask for the maximum values to the children
  
  ntl = jt.elementAt(0).getNeighbourList();
  
  for (i=0 ; i<ntl.size() ; i++) {
    node = ntl.elementAt(i).getNeighbour();
    r = ntl.elementAt(i).getMessage();
    vars = svo.notIn(aux.getVariables(),r.getVariables().toVector());
    aux2 = new Configuration(aux,vars);
    askMaxConfiguration(jt.elementAt(0),node,aux2,conf);  
  }

  // Finally the explanation is created and stored
  
  exp = new Explanation(conf,prob);
  kBest.addElement(exp);

  return prob;
}


/**
 * Requests a message to a node which obtain the 
 * configuration of maximum probability consistent with the configuration
 * passed as parameter, and recursively does the same until 
 * a leaf is reached.
 *
 * @param sender the node which asks for the message.
 * @param recipient the node asked for the message.
 * @param subconf the configuration used to check the consistency.
 * @param exp the configuration being built.
 */

public void askMaxConfiguration(NodeJoinTree sender, NodeJoinTree recipient,
				Configuration subconf, Configuration exp) {

  NeighbourTreeList ntl;
  NodeJoinTree node;
  FiniteStates fs;
  int i;
  Relation r;
  Potential pot;
  Configuration aux, aux2;
  Vector vars;
  SetVectorOperations svo = new SetVectorOperations();

  // first obtain the maximum configuration

  pot = recipient.getNodeRelation().getValues();    
  aux = pot.getMaxConfiguration(subconf);

  // setting values for the variables in clique 0

  for (i=0 ; i<aux.size() ; i++) {
    fs = aux.getVariable(i);
    if (explanationSet.getId(fs) != -1){ // fs is in the explanation set
      exp.putValue(fs,aux.getValue(i));
    }
  }

  // now we ask for the maximum values to the children

  if (!recipient.isLeaf()) {  
    ntl = recipient.getNeighbourList();
  
    for (i=0 ; i<ntl.size() ; i++) {
      node = ntl.elementAt(i).getNeighbour();
      if (node.getLabel() != sender.getLabel()) {
        r = ntl.elementAt(i).getMessage();
        vars = svo.notIn(aux.getVariables(),r.getVariables().toVector());
        aux2 = new Configuration(aux,vars);
        askMaxConfiguration(recipient,node,aux2,exp);
      }
    }
  }
}


/**
 * Initializes the Partition Element List with an almost void element,
 * containint clique 0 and the index of the best explanation. This will be
 * the starting point for method <code>refinePartitionList</code>. 
 * @param prob1 the probability of the best MPE
 * @return the partition element list initialized.
 */

public AIPartitionElementList initPartitionList(double prob1) {

  AIPartitionElementList list;
  AIPartitionElement pe;
  Configuration conf = new Configuration(); // a void configuration

  pe = new AIPartitionElement(jt.elementAt(0),0,conf);
  pe.setProb(prob1);
  list = new AIPartitionElementList();
  list.addElement(pe);

  return list;
}


/**
 * Refines the list by taking the first element of the list, removing it, and
 * adding the new elements to the partition list.
 * NOTE: the list is modified.
 * @param list the partition list to be refined.
 */

public void refinePartitionList(AIPartitionElementList list) {

  AIPartitionElement pe, first;
  Explanation exp;
  int i, pos, indexParent, m;
  NodeJoinTree node, nodeAux;
  Configuration same = new Configuration();
  Configuration distinct, maxConf, aux;
  Configuration confJT;  // all the variables in the jointree
  double prob, pSeparator, pClique;
  Potential pot, potParent, pot2;
  Relation r;
  Node var;

  // taking the first element of the list and removing it
  first = list.elementAt(0);
  list.removeElementAt(0);

  // obtaining the configuration over all the variables in the
  // join tree, with the explanation and the observations

  exp = (Explanation) kBest.elementAt(kBest.size()-1);
  confJT = new Configuration(jt.getVariables().toVector(),exp.getConf());

  // now we add the values for the observed variables, if they are
  // contained in the join tree
  for (i=0 ; i<observations.size() ; i++) {
    var = observations.getVariable(i);
    pos = confJT.indexOf(var);
    if (pos != -1) {
      confJT.putValue((FiniteStates)var,observations.getValue(i)); 
    }
  }

  // now we begin the process

  for (i=first.getClique().getLabel() ; i < jt.size() ; i++) {
    node = jt.elementAt(i);
    if (i != 0) {
      r = node.getNeighbourList().elementAt(0).getMessage();
      same = new Configuration(confJT,r.getVariables());
    }
    pe = new AIPartitionElement(node,kBest.size()-1,same);
    
    // copying the list of distincts in first for first.clique
    if (node.getLabel() == first.getClique().getLabel()) {
      pe.setDistincts((Vector)first.getDistincts().clone());
    }
    
    // creating the configuration distinct as a subconfiguration
    // of confJT

    distinct = new Configuration(confJT,node.getVariables());    
    pe.addDistinct(distinct);

    // indentificating the maximum configuration 
    
    pot = node.getNodeRelation().getValues();
    maxConf = pot.getMaxConfiguration(same,pe.getDistincts());
    
    pe.setMaxSubConf(maxConf);
    

    // calculation of the associate probability

    if (maxConf.size() > 0) {
      r = node.getNodeRelation();
      aux = new Configuration(confJT,r.getVariables());
      pot2 = r.getValues();
      if (i==0) prob = pot.getValue(maxConf);
      else prob = first.getProb() *
                  (pot.getValue(maxConf) / pot2.getValue(aux));
    }
    else
      prob = 0.0;

    // adding the partition element to the list
    pe.setProb(prob);
    list.addElement(pe);
  } 

  // now we truncate the list
  list.truncate(nExplanations-1);
}


/**
 * Obtains the next explanation by taking the first element of 
 * PartitionElementList.
 *
 * @param list the partitionElementList
 */

public void getNextExplanation(AIPartitionElementList list) {

  AIPartitionElement pe;
  Configuration conf, auxConf, aux2;
  int i;
  Explanation exp, expAux;
  NodeJoinTree node, destination;
  NeighbourTreeList ntl;
  Relation r;
  FiniteStates fs;
 
  // We take the first element of list

  pe = list.elementAt(0);

  // Now we build the explanation by maxDistribution

  // first we copy the values of the best explanation which gives rise 
  // to this partition

  conf = new Configuration(explanationSet);
  expAux = (Explanation)kBest.elementAt(pe.getExpIndex());
  conf.setValues((Vector)expAux.getConf().getValues().clone());    

  // Now we put the values of the max subconfiguration in pe

  auxConf = pe.getMaxSubConf();
  for (i=0 ; i<auxConf.size() ; i++) {
    fs = auxConf.getVariable(i);
    if (explanationSet.getId(fs) != -1) // fs is in the explanation set
      conf.putValue((FiniteStates)auxConf.getVariables().elementAt(i),
                     auxConf.getValue(i));
  }

  // The configuration is completed by askingValues to the children

  node = pe.getClique();
  ntl = node.getNeighbourList();

  for (i=0 ; i<ntl.size() ; i++) {
    destination = ntl.elementAt(i).getNeighbour();
    if (node.getLabel() < destination.getLabel()) { // to avoid the parent
      r = ntl.elementAt(i).getMessage();
      aux2 = new Configuration(auxConf,r.getVariables());
      askMaxConfiguration(node,destination,aux2,conf);
    }
  }

  // building the explanation and adding to kBest

  exp = new Explanation(conf,pe.getProb());
  kBest.addElement(exp);  
}


/**
 * Transforms a <code>PotentialTree</code>.
 * The only thing to do is to prune the
 * nodes whose children are equal, so we use a smallest value
 * for limit.
 * This method can be overcharged for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @param pot the potential to be transformed.
 */

public Potential transformPotentialAfterMarginalization(Potential pot) {

//  if (typeOfPotential.equals("trees")) {
//    ((PotentialTree)pot).limitBound(10e-30);
//  }
  return pot;
}


/**
 * Transforms a <code>PotentialTree</code>.
 * The only thing to do is to prune the
 * nodes whose children are equal, so we use a smallest value
 * for limit.
 * This method can be overloaded for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @param pot the potential to be transformed.
 */

public Potential transformPotentialAfterCombination(Potential pot) {

//  if (typeOfPotential.equals("trees")){
//    ((PotentialTree)pot).limitBound(10e-30);
//  }
  return pot;
}                

} // end of class