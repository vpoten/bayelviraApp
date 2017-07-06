/* HuginPropagation.java */

package elvira.inference.clustering;

import java.io.*;
import java.util.Date;
import java.util.Vector;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.potential.ProbabilityTree;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialTable;
import elvira.potential.PotentialMTree;
import elvira.potential.MultipleTree;
import elvira.potential.Potential;

/**
 * Class <code>HuginPropagation</code>. Implements the Hugin probabilistic
 * propagation, and also some auxiliar procedures that can be useful
 * to other classes.
 *
 * @since 7/3/2003
 */

public class HuginPropagation extends Propagation {

/**
 * The jointree over which the propagation is carried out.
 */

JoinTree jt;

/**
 * A string indicating the type of potential to be used in the propagation
 */

private String typeOfPotential;


/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * <li> Output file.
 * <li> typeOfPotential. (tables or trees)
 * <li> Evidence file.
 * <li> The deletion sequence
 * </ol>
 * If the evidence file is omitted, then no evidences are
 * considered
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile, sigmaFile;
  HuginPropagation hp;
  int i;
  NodeList sigma=new NodeList();
  
  if (args.length < 3) {
    System.out.println("\nToo few arguments. The arguments are:");
    System.out.println("\tNetwork OutputFile (tables|trees) [evidence] [deletionSequence]");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    System.out.print("\nLoading network ....");
    b = new Bnet(networkFile);
    System.out.print("Network loaded\n");

    if ( !( (args[2].equals("tables")) || (args[2].equals("trees")) ) ) {
      System.out.println("TypeOfPotential has to be in {tables,trees}");
      System.exit(0);
    }
    
    if (args.length == 5) {
      evidenceFile = new FileInputStream(args[3]);
      e = new Evidence(evidenceFile,b.getNodeList());
      
      sigmaFile = new FileInputStream(args[4]);
      sigma = new NodeList(sigmaFile,b.getNodeList());
    }
    else if (args.length == 4) {
      // trying if args[3] is the evidence file
      try {
        evidenceFile= new FileInputStream(args[3]);
        e = new Evidence(evidenceFile,b.getNodeList());
        sigma = new NodeList();
      }catch (ParseException pe){
        sigmaFile = new FileInputStream(args[3]);
        sigma = new NodeList(sigmaFile,b.getNodeList());
        e = new Evidence();
      }
    }
    else { 
      e = new Evidence();
      sigma = new NodeList();
    }
  
    hp = new HuginPropagation(b,e,args[2],sigma);
    System.out.println("Evidencia:" );
    e.pPrint();
    hp.propagate(hp.getJoinTree().elementAt(0),"no");
    hp.saveResults(args[1]);
  }
}


/**
 * Constructor. Creates a propagation for a given Bayesian network.
 * @param b the <code>Bnet</code> used for the compilation of the joinTree,
 * assuming tables for potentials.
 */

public HuginPropagation(Bnet b) {

  observations = new Evidence();
  interest = new NodeList();
  results = new Vector();
  setProblem("marginal");
  setMethod("Hugin");
  network = b;
  typeOfPotential = new String("tables");
  jt = new JoinTree();
  jt.treeOfCliques(network);
}


/**
 * Constructor. Creates a propagation for a given Bayesian network and
 * some evidence, assuming tables to represent potentials.
 * @param b the <code>Bnet</code> used for the compilation of the joinTree
 * @param e the <code>Evidence</code>
 */

public HuginPropagation(Bnet b, Evidence e) {

  observations = e;
  interest = new NodeList();
  results = new Vector();
  setProblem("marginal");
  setMethod("Hugin");
  network = b;
  typeOfPotential = new String("tables");
  jt = new JoinTree();
  jt.treeOfCliques(network);        
}


/**
 * Constructor. Creates a propagation for a given Bayesian network and
 * some evidence.
 * @param b the <code>Bnet</code> used for the compilation of the joinTree
 * @param e the <code>Evidence</code>
 * @param pot indicates the type of potential to be used
 */

public HuginPropagation(Bnet b, Evidence e, String pot) {

  observations = e;
  interest = new NodeList();
  results = new Vector();
  setProblem("marginal");
  setMethod("Hugin");
  network = b;
  typeOfPotential = new String(pot);
  jt = new JoinTree();
  jt.treeOfCliques(network);        
}


/**
 * Constructor. Creates a propagation for a given Bayesian network and
 * some evidence.
 * @param b the <code>Bnet</code> used for the compilation of the joinTree
 * @param e the <code>Evidence</code>
 * @param pot indicates the type of potential to be used
 * @param sigma the deletion sequence to be used (can be empty)
 */

public HuginPropagation(Bnet b, Evidence e, String pot, NodeList sigma) {

  observations = e;
  interest = new NodeList();
  results = new Vector();
  setProblem("marginal");
  setMethod("Hugin");
  network = b;
  typeOfPotential = new String(pot);
  jt = new JoinTree();
  if (sigma.size() == 0)
    jt.treeOfCliques(network);
  else
    jt.treeOfCliques(sigma,network);        
}


/**
 * Constructor. Creates a propagation for a given Bayesian network and
 * some evidence. Assumes tables for potentials.
 * Does not build the join tree.
 * @param b the <code>Bnet</code> used for the compilation of the joinTree
 * @param e the <code>Evidence</code>
 */

public HuginPropagation(Evidence e, Bnet b) {

  observations = e;
  interest = new NodeList();
  results = new Vector();
  setProblem("marginal");
  setMethod("Hugin");
  network = b;
  typeOfPotential = new String("tables");
  jt = new JoinTree();
}


/**
 * Constructor. Creates a propagation for a given Bayesian network and
 * some evidence. Does not build the join tree.
 * @param b the <code>Bnet</code> used for the compilation of the joinTree
 * @param e the <code>Evidence</code>
 * @param pot the type of potential to be used
 */

public HuginPropagation(Evidence e, Bnet b, String pot) {

  observations = e;
  interest = new NodeList();
  results = new Vector();
  setProblem("marginal");
  setMethod("Hugin");
  network = b;
  typeOfPotential = new String(pot);
  jt = new JoinTree();
}


/**
 * Gets the join tree used in the propagation.
 * @return the <code>JoinTree</code> used in the propagation.
 */

public JoinTree getJoinTree() {

  return jt;
}


/**
 * Sets the join tree to be used in the propagation
 * @param tree the <code>JoinTree</code>
 */

public void setJoinTree(JoinTree tree) {
  
  jt = tree;
}


/**
 * Gets the type of potentials used.
 * @return a <code>String</code> with the type of potentials
 * used in the propagation
 */

public String getTypeOfPotential() {
  
  return typeOfPotential;
}


/**
 * Sets the type of potential to be used in propagation
 * @param pot a <code>String</code> indicating the type of
 * potential to be used
 */

public void setTypeOfPotential(String pot) {
  
  typeOfPotential = new String(pot);
}


/**
 * Restricts the tables of the join tree associated with the
 * class to the observations.
 */

public void instantiateEvidence() {

  Relation R;
  NodeJoinTree node;
  int i, s;
  Potential pot;
  SetVectorOperations svo = new SetVectorOperations();
  Vector commonVars;
  
  s = jt.getJoinTreeNodes().size();
  
  for (i=0 ; i<s ; i++) {
    node = jt.elementAt(i);
    R = node.getNodeRelation();
    commonVars = svo.intersection(R.getVariables().getNodes(),
				  observations.getVariables());
    if (commonVars.size() != 0) {
      pot = R.getValues();
      pot.instantiateEvidence(observations);
      pot = transformPotential(pot);
    }
  }
}


/**
 * Inits the messages for a Hugin-like propagation. So, only a message
 * between two cliques is necessary, and the message X-Y and Y-X are the same.
 * IMPORTANT: the cliques must be labeled
 */

public void initHuginMessages() {

  Relation R;
  NodeJoinTree node,neighbour;
  int i, j, k, s, pos;
  int nodeLabel, neighbourLabel;  
  PotentialTable potTable; 
  PotentialTree potTree;
  PotentialMTree potMTree;
  NeighbourTreeList ntl, ntl2;
  NeighbourTree nt, nt2;
  String usedPotential;
  

  // Identifying the type of potential used in the join tree
  usedPotential = new
       String((jt.elementAt(0).getNodeRelation().getValues()).getClass().getName());

  // beginning the process

  s = jt.getJoinTreeNodes().size();
  
  for (i=0 ; i<s ; i++) {
    node = jt.elementAt(i);
    nodeLabel = node.getLabel();
    ntl = node.getNeighbourList();
    
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      neighbour = nt.getNeighbour();
      neighbourLabel = neighbour.getLabel();
      R = nt.getMessage();
  
      if (nodeLabel < neighbourLabel) { //init unitary potential
        if ( usedPotential.equals("elvira.potential.PotentialTable") ) {
	  potTable = new PotentialTable(R.getVariables());
          potTable.setValue(1.0);
          R.setValues(potTable);
        }
        else {
	    if ( usedPotential.equals("elvira.potential.PotentialTree") ) {
		potTree = new PotentialTree(R.getVariables());
		potTree.setTree(new ProbabilityTree(1.0));
		R.setValues(potTree);
	    }
	    else {
		if (usedPotential.equals("elvira.potential.PotentialMTree") ) {
		    potMTree = new PotentialMTree(R.getVariables());
		    potMTree.setTree(new MultipleTree(1.0));
		    R.setValues(potMTree);
		}
	    }
	}
      }
      else { // locate the inverse message and assign it
        pos = jt.indexOf(neighbour);
        ntl2 = jt.elementAt(pos).getNeighbourList();
        for (k=0 ; k<ntl2.size() ; k++) {
          nt2 = ntl2.elementAt(k);
          if (nt.getNeighbour().getLabel() == nodeLabel) {
            nt2.setMessage(R);
            break;
          }
        }
      }
      
    } // end for j

  } // end for i
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
 * and <code>otherValues</code> the old separator
 *
 * NOTE: all the tables (potentials and messages) are modified
 *
 * @param root the node (<code>NodeJoinTree</code>)  used as root
 * for the propagation
 * @param divide if "no" the divisions in the separators are not performed
 */

public void upward(NodeJoinTree root, String divide) {

  NeighbourTreeList ntl;
  NodeJoinTree node;
  int i;
  Relation r, r2;
  Potential pot,newSep,oldSep;

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
      
      pot.combineWithSubset(newSep.divide(oldSep));
      pot = transformPotential(pot);
    }
    else{
      pot.combineWithSubset(newSep);
      pot = transformPotential(pot);
    }
    r.setValues(pot);
  }
}


/**
 * Requests a message to a node which recursively does the same until 
 * a leaf is reached, point at which the information is propagated backward
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
  Potential pot, newSep, oldSep, message;


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
  for(i=0 ; i<ntl.size() ; i++) {
    node = ntl.elementAt(i).getNeighbour();
    if (node.getLabel() != sender.getLabel()) {
      r2 = ntl.elementAt(i).getMessage();
      newSep = r2.getValues();
      if (divide.equals("yes")) {
        oldSep = r2.getOtherValues();
        
        pot.combineWithSubset(newSep.divide(oldSep));
      }
      else
	pot.combineWithSubset(newSep);
      pot = transformPotential(pot);
      r.setValues(pot);
    }
  }
  
  // sending (storing) message to sender (father)
  // pot contains the potential stored in recipient 
  
  father = ntl.indexOf(sender);
  nt = ntl.elementAt(father);
  newSep = pot.marginalizePotential(nt.getMessage().getVariables().toVector());
  newSep = transformPotential(newSep);
  if (divide.equals("no")) {
    nt.getMessage().setValues(newSep);
  }
  else {   
    nt.getMessage().setOtherValues(nt.getMessage().getValues());
    nt.getMessage().setValues(newSep);
  } 
}


/**
 * Performs a downward propagation across the join tree, taking as root the 
 * node passed as argument. 
 *
 * NOTE: all the tables (potentials and messages) are modified
 *
 * @param root the node (<code>NodeJoinTree</code>) used
 * as root for the propagation
 */

public void downward(NodeJoinTree root) {

  NeighbourTreeList ntl;
  NeighbourTree nt;
  NodeJoinTree node;
  int i;
  Relation r, r2;
  Potential pot, newSep;

  ntl = root.getNeighbourList();
  
  // caculate the message, store it in the separator and send 

  r = root.getNodeRelation();
  pot = r.getValues();
  
  for (i=0 ; i<ntl.size() ; i++) {
    nt = ntl.elementAt(i);
    node = nt.getNeighbour();
    r2 = nt.getMessage();
    r2.setOtherValues(r2.getValues());
    newSep = pot.marginalizePotential(nt.getMessage().getVariables().toVector());
    newSep = transformPotential(newSep); 
    nt.getMessage().setValues(newSep);
    sendMessage(root,node);  
  }  
}

/**
 * Sends a message to a node which recursively does the same until 
 * a leaf is reached.
 * 
 * @param sender the node (<code>NodeJoinTree</code>) which asks for
 * the message
 * @param recipient the node (<code>NodeJoinTree</code>) asked for the
 * message
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient) {

  NeighbourTreeList ntl;
  NeighbourTree nt;
  NodeJoinTree node;
  int i, father;
  Relation r, r2;
  Potential pot, newSep, oldSep;


  // combination of the potential with the message sended by sender

  r = recipient.getNodeRelation();
  pot = r.getValues();
  
  ntl = recipient.getNeighbourList();
  father = ntl.indexOf(sender);
  nt = ntl.elementAt(father);
  newSep = nt.getMessage().getValues();
  oldSep = nt.getMessage().getOtherValues();
  
  pot.combineWithSubset(newSep.divide(oldSep));
  pot = transformPotential(pot);

  r.setValues(pot);
  
  // now send message to the children

  if (ntl.size() != 1) { // the node is not a leaf 
    for(i=0 ; i<ntl.size() ; i++) {
      node = ntl.elementAt(i).getNeighbour();
      if (node.getLabel() != sender.getLabel()) {
        nt = ntl.elementAt(i);
        r2 = nt.getMessage();
        nt.getMessage().setOtherValues(nt.getMessage().getValues());
        newSep = pot.marginalizePotential(nt.getMessage().getVariables().toVector());
        newSep = transformPotential(newSep); 
        nt.getMessage().setValues(newSep);
        sendMessage(recipient,node);
      }  
    }
  } 
}


/**
 * Procedure to obtain the probability of the observed evidence.
 * @param initialize indicates if initializations over the jointree have
 *        to be carried out ("yes" or "not").
 */

public double obtainEvidenceProbability(String initialize) {

  if (initialize.equals("yes")) {
    if (typeOfPotential.equals("tables"))
      jt.initTables(network);
    else if (typeOfPotential.equals("trees"))
      jt.initTrees(network);
    else {
      System.out.println(typeOfPotential + " is not avalilabe in HuginPropagation");
      System.exit(0);
    }
    transformRelationsInJoinTree();
    jt.setLabels();
    instantiateEvidence( );
    initHuginMessages( );
  }
  upward(jt.elementAt(0),"no");

  return jt.elementAt(0).getNodeRelation().getValues().totalPotential();
}


/**
 * Gets the smalles relation containing a given node.
 * @param fs the node (<code>FiniteStates</code) for which we want
 * to find the smallest potential containing it 
 * @return the smallest <code>Relation</code> containing <code>fs</code>.
 */

Relation locateSmallestTable(FiniteStates fs) {

  int i, j, s, s2;
  Relation r, nR;
  NodeJoinTree njt;
  NodeList vars;
  NeighbourTreeList ntl;
  NeighbourTree nt;

  // first we initialize the relation as big as possible

  r = new Relation();
  r.setVariables(network.getNodeList());

  // now we locate the smallest table

  s = jt.size();
  for (i=0 ; i<s ; i++) {
    njt = jt.elementAt(i);
    vars = njt.getVariables();
    if (vars.getId(fs.getName()) != -1) { // fs is included in this clique
      nR = njt.getNodeRelation(); 
      if ((FiniteStates.getSize(nR.getVariables().toVector())) <=
	  (FiniteStates.getSize(r.getVariables().toVector()))){
        r = nR;
      }
      // now we visit the separators
      ntl = njt.getNeighbourList();
      s2 = ntl.size();
      for (j=0 ; j<s2 ; j++) {
        nt = ntl.elementAt(j);
        // the following comparison is to ensure that a separator is visited
        // only once
        if (njt.getLabel() < nt.getNeighbour().getLabel()) {
          nR = nt.getMessage();
          if (nR.getVariables().getId(fs.getName()) != -1) {
	    if (((int)FiniteStates.getSize(nR.getVariables().toVector())) <=
                ((int)FiniteStates.getSize(r.getVariables().toVector()))){
              r = nR;
            }
          }    
        }
      }
    }

    // if r contains the table for fs, then finish at this point
    if (((int)FiniteStates.getSize(r.getVariables().toVector())) ==
        fs.getNumStates())
      break;
  }
  
  return r;
}


/**
 * Gets the posterior distributions and store them in <code>results</code>
 */

public void getPosteriorDistributions() {

  int i, s;
  FiniteStates fs;
  Relation r;
  Potential pot;
  NodeList nl;
  PotentialTable ptable;

  results = new Vector();
  s = interest.size();
  for (i=0 ; i<s ; i++) {
    fs = (FiniteStates)interest.elementAt(i);
    r = locateSmallestTable(fs);
    pot = r.getValues();
    
    if (r.getVariables().size() != 1) {
      nl = new NodeList();
      nl.insertNode(fs);
      pot = pot.marginalizePotential(nl.toVector()); 
    }
    pot.normalize();

    if (pot.getClassName().equals("PotentialTree")) {
      ptable = new PotentialTable((PotentialTree)pot);
      results.addElement(ptable);
    } 
    else {
      if (pot.getClassName().equals("PotentialMTree")) {
	ptable = new PotentialTable((PotentialMTree)pot);
	results.addElement(ptable);
      } 
      else results.addElement(pot);
    }
  }
}


/**
 * Performs a Hugin propagation
 *
 * @param root the node (<code>NodeJoinTree</code>) used as root for
 * the upward and downward phases
 * @param divide "yes" if division are carried out in the upward phase and
 *        "no" in other case
 */

public void propagate(NodeJoinTree root, String divide) {

  Date d;
  double time;


  jt.sortVariables(network.getNodeList());
  if (typeOfPotential.equals("tables"))
    jt.initTables(network);
  else {
    if (typeOfPotential.equals("trees"))
      jt.initTrees(network);
    else {
      if (typeOfPotential.equals("mtrees"))
	  jt.initMultipleTrees(network);
      else {
        System.out.println(typeOfPotential + " is not avalilabe in HuginPropagation");
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

  upward(root,divide);
  Potential pot = root.getNodeRelation().getValues();
  System.out.println("\nEvidence Probability is " + pot.totalPotential());
  downward(root);

  // calculation of the posterior distribution
  getPosteriorDistributions();
  
  // showing messages

  d = new Date();
  time = ((double)d.getTime() - time) / 1000;
  System.out.println("Posterior distributions computed.");
  System.out.println("Time (secs): " + time);
}


/**
 * Performs a Hugin propagation in an initialised join tree
 * the method is useful for propagating when new evidence has
 * been added to the old one
 *
 * @param root the node (<code>NodeJoinTree</code>) used as root for
 * the upward and downward phases
 * @param divide "yes" if division are carried out in the upward phase and
 *        "no" in other case
 */

public void rePropagate(NodeJoinTree root, String divide) {

  Date d;
  double time;


  obtainInterest();

  System.out.println("Computing posterior distributions...");
  d = new Date();
  time = (double)d.getTime();
  
  if (observations.size() > 0)
    instantiateEvidence( );  
  
  upward(root,divide);
  downward(root);

  // calculation of the posterior distribution
  getPosteriorDistributions();
  
  // showing messages

  d = new Date();
  time = ((double)d.getTime() - time) / 1000;
  System.out.println("Posterior distributions computed.");
  System.out.println("Time (secs): " + time);
}


/**
 * Saves the result of a propagation to a file.
 * @param s a <code>String</code> containing the file name.
 */

public void saveResults(String s) throws IOException {

  FileWriter f;
  PrintWriter p;
  Potential pot;
  int i;

  f = new FileWriter(s);
  
  p = new PrintWriter(f);
  
  for (i=0 ; i<results.size() ; i++) {
    pot = (Potential) results.elementAt(i);
    pot.saveResult(p);
  }
  
  f.close();
}


/**
 * Reports the results of the propagation to the standard output.
 */

public void showResults() throws IOException {

  Potential pot;
  int i;

  for (i=0 ; i<results.size() ; i++) {
    pot = (Potential)results.elementAt(i);
    pot.showResult();
  }
}


/**
 * Transforms the initial relations in the join tree if they
 * are of class <code>PotentialTree</code>. The only thing to do is
 * the pruning of nodes whose children are equal, so we use a very small
 * value as limit for prunning.
 * This method can be overloaded for special requirements.
 */

public void transformRelationsInJoinTree() {

  int i, s;
  Relation r;

  if (typeOfPotential.equals("trees")) {
    s = jt.size();
    for (i=0 ; i<s ; i++) {
      r = ((NodeJoinTree)jt.elementAt(i)).getNodeRelation();
      r = transformRelation(r);
    }
  }
}                    


/**
 * Transforms a relation if its values are of
 * class <code>PotentialTree</code>.
 * The only thing to do is the pruning of 
 * nodes which children are equals,  so we use a very small
 * value as limit for prunning.
 * This method can be overloaded for special requirements.
 * @param r the <code>Relation</code> to be transformed
 */

public Relation transformRelation(Relation r) {

  PotentialTree pot;

  //if (typeOfPotential.equals("trees")) {
  //  pot = (PotentialTree)r.getValues();
  //  pot.limitBound(10e-60);
  //  r.setValues(pot);
  //}
  return r;
}                    


/**
 * Transforms a <code>PotentialTree</code>.
 * The only thing to do is the pruning of 
 * nodes which children are equals,  so we use a very small
 * This method can be overloaded for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @param pot the  <code>PotentialTree</code> to be transformed
 */

public Potential transformPotential(Potential pot) {

  //if (typeOfPotential.equals("trees")){
  //  ((PotentialTree)pot).limitBound(10e-60);
  //}
  return pot;
}



} // end of class
