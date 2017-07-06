/* LazyPenniless.java */

package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import elvira.*;
import elvira.inference.Propagation;
import elvira.inference.clustering.Penniless;
import elvira.parser.ParseException;

import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialMTree;
import elvira.potential.MultipleTree;
import elvira.potential.ListPotential;

/**
 * Class <code>LazyPenniless</code>.
 * Implements the Penniless Propagation algorithm over a binary
 * join tree, but in this version, messages in the join tree are
 * stored in factorized form, and operations are performed
 * using Lazy Evaluation.
 *
 * @since 14/9/2000
 */


public class LazyPenniless extends Penniless {
  
  
/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * <li> Output file results:
 * <li> Output error file, where the error and computing time
 * of each experiment will be stored.
 * <li> File with exact results.
 * <li> The method for approximating (0|1).
 * <li> A double; limit for pruning.
 * <li> A double; lowLimit to consider the tree exact or not when pruning.
 * That is, if a branch is pruned and information is lower than lowLimit then
 * we consider that no approximation has been carry out.
 * Also, this value is used  to prune initial potentials
 * (<code>PotentialTree</code>).
 * <li> An integer: number of stages. 
 * <li> An integer for each stage indicating the maximum potential size
 * at that stage.
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
  LazyPenniless propagation;
  int i, m, nstages;
  int[] ls;
  double[] lp=new double[1]; double[] llp=new double[1];
  boolean[] sortAndBound;  
  double[] errors;
  double g, mse, timePropagating;
  Date date;
  FileWriter f;
  PrintWriter p;
  
  if (args.length < 8) {
    System.out.print("Too few arguments. Arguments are: ElviraFile");
    System.out.print(" OutputFile OutputErrorFile InputExactResultsFile");
    System.out.print(" MethodForPropagating(0|1) LimitForPruning LowLimitForPruning NumberStages");
    System.out.println(" MaxSizeInStage1 MaxSizeInStage2 ... ");
    System.out.println(" SortAndBoundInStage1(true|false) SortAndBoundInStage2 ... [EvidenceFile]");
  }
  else {
    nstages = (Integer.valueOf(args[7])).intValue();
    
    if (args.length < (2*nstages)+8) {
      System.out.print("Too few arguments. Arguments are: ElviraFile");
      System.out.print(" OutputFile OutputErrorFile InputExactResultsFile");
      System.out.print(" MethodForPropagating(0|1) LimitForPruning LowLimitForPruning NumberStages");
      System.out.println(" MaxSizeInStage1 MaxSizeInStage2 ... ");
      System.out.println(" SortAndBoundInStage1(true|false) SortAndBoundInStage2 ... [EvidenceFile]");
    }
    else {
      networkFile = new FileInputStream(args[0]);
      b = new Bnet(networkFile);
      
      if (args.length == 2*nstages+9) {
	evidenceFile = new FileInputStream(args[2*nstages+8]);
	e = new Evidence(evidenceFile,b.getNodeList());
	System.out.println("Evidence file"+args[2*nstages+8]);
      }
      else
	e = new Evidence();
      
      ls = new int[nstages];
      sortAndBound = new boolean[nstages];
      
      for (i=0 ; i<nstages ; i++) {
	ls[i] = (Integer.valueOf(args[i+8])).intValue();
	sortAndBound[i] = (Boolean.valueOf(args[i+8+nstages])).booleanValue();
      }
      
      lp[0] = (Double.valueOf(args[5])).doubleValue();
      llp[0] = (Double.valueOf(args[6])).doubleValue();
      
      System.out.println("Low limit: "+lp);
      System.out.println("Low limit for pruning: "+llp);
      
      m = (Integer.valueOf(args[4])).intValue();

      propagation = new LazyPenniless(b,e,lp,llp,ls,sortAndBound,m);

      date = new Date();
      timePropagating = (double)date.getTime();
      propagation.propagate(args[1]);
      
      date = new Date();
      timePropagating = ((double)date.getTime()-timePropagating) / 1000;
      
      System.out.println("Reading exact results");
      propagation.readExactResults(args[3]);
      System.out.println("Exact results read");
      
      System.out.println("Computing errors");
      errors = new double[2];
      propagation.computeError(errors);
      
      g = errors[0];
      mse = errors[1];
      
      f = new FileWriter(args[2]);
      p = new PrintWriter(f);
      
      p.println("Time propagating (secs) : "+timePropagating);
      p.println("G : "+g);
      p.println("MSE : "+mse);
      f.close();
      
      System.out.println("Done"); 
    }
  }
}

  
/**
 * Creates an empty object. Necessary for subclass definition.
 */

LazyPenniless() {
  
}


/**
 * Creates a new propagation.
 *
 * @param b a belief network.
 * @param e an evidence.
 * @param lp the limit for pruning.
 * @param llp the lowLimit for pruning;
 * @param ls the maximum sizes for potentials.
 * @param sortAndBound vector of boolean that indicates whether we will
 * do <code>sortAndBound</code> in each step or not.
 * @param m the info measure for pruning the trees.
 */

LazyPenniless(Bnet b, Evidence e, double[] lp, double[] llp,
	      int[] ls, boolean[] sortAndBound, int m) {
  
  Triangulation triang;
  RelationList rel1, rel2, ir, irTree;
  NodeList numeration = new NodeList();
  int i, j;
  ListPotential listPot, newPot;
  Potential pt;
  PotentialMTree pmt;
  Relation newRel, rel;
  Vector newList;

  
  observations = e;
  network = b;
  positions = new Hashtable();
  
  binTree = new JoinTree(b,e);
  irTree = getInitialRelations();
  irTree.restrictToObservations(observations);
  
  for (i=0 ; i<irTree.size() ; i++)
    ((ListPotential)irTree.elementAt(i).getValues()).limitBound(llp[0]);

  ir = new RelationList();
  for (i=0 ; i<irTree.size() ; i++) {
    newList = new Vector();
    rel = irTree.elementAt(i);
    newRel = new Relation();
    newRel.setVariables(rel.getVariables().copy());
    listPot = (ListPotential)rel.getValues();
    
    for (j=0 ; j<listPot.getListSize() ; j++) {
      pt = listPot.getPotentialAt(j);    
      pmt = new PotentialMTree((PotentialTree)pt);
      newList.addElement(pmt);
    }
    
    newPot = new ListPotential(newList);
    newRel.setValues(newPot);
    newRel.setKind(rel.getKind());
    ir.insertRelation(newRel);
  }


  marginalCliques = binTree.Leaves(ir);

  binTree.binTree();
 
  setMaximumSizes(ls);
  setSortAndBound(sortAndBound);
  setLimitForPruning(lp);
  setLowLimitForPruning(llp);
  setInfoMeasure(m);
  binTree.setLabels();
}


/**
 * Gets the initial relations in the network.
 * @return the initial relations present in the network, where the
 * potentials are of class <code>ListPotential</code>.
 */

public RelationList getInitialRelations() {
  
  Relation rel, newRel;
  RelationList list;
  PotentialTree pt;
  ListPotential listPot;
  int i;
  Vector v;
 
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
    
    v = new Vector();
    v.addElement(pt);
    listPot = new ListPotential(v);
    
    newRel.setValues(listPot);
    newRel.setKind(rel.getKind());
    
    list.insertRelation(newRel);
  }
  
  return list;
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
  ListPotential pot, pot2;
  Vector leaves;
  NodeJoinTree node, otherNode;
  NeighbourTree neighbour, nt;
  NeighbourTreeList ntl;
  NodeList nl1, nl2;
  

  for (i=0 ; i<binTree.getJoinTreeNodes().size() ; i++) {
    // Set the potentials in the cliques to 1.
    node = binTree.elementAt(i);    
    r = node.getNodeRelation();
    if ((r.getValues() == null) || (!r.getValues().getClassName().equals("ListPotential"))) {
      pot = new ListPotential(r.getVariables());
      r.setValues(pot);
    }
    
    // Now set the messages
    ntl = node.getNeighbourList();
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      r = nt.getMessage();
      pot = new ListPotential(r.getVariables());

      pot.setExact(false);
      r.setValues(pot);
      pot = new ListPotential(r.getVariables());

      pot.setExact(false);
      r.setOtherValues(pot);
    }
  }
}


/**
 * Sends a message from a node to another one.
 * Marks the messages as not exact when this method carries out an
 * approximation or at least one of the input messages is not exact. 
 * The message is computed by combining all the messages inwards
 * the sender but that one comming from the recipient. Then, the
 * result is sorted and bounded conditional to the message
 * going from the recipient to the sender.
 * It is required that the nodes in the tree be labeled.
 * Use method <code>setLabels</code> if necessary.
 *
 * @param sender the node that send the message.
 * @param recipient the node that receives the message.
 * @param takeNumberNextStage <code>true</code> if we must do
 * <code>sortAndBound</code> over the message with
 * <code>maximumSize[currentStage+1]</code> and <code>false</code> if we must
 * do <code>sortAndBound</code> over the message with
 * <code>maximumSize[currentStage]</code>.
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient,
			boolean takeNumberNextStage) {
  
  NeighbourTreeList list, auxList;
  NeighbourTree nt;
  ListPotential aux, pot, incoming;
  Potential pmt;
  Relation rel, outwards, inwards;
  int i, label;
  Vector separator, v;
  boolean isExact = true;
  NodeList vars;
  
  
  incoming = new ListPotential();
  outwards = new Relation();
  inwards = new Relation();
  separator = new Vector();
  
  aux = (ListPotential)sender.getNodeRelation().getValues();
  
  pmt = makeUnitPotential();
  v = new Vector(); // Used to create a new ListPotential.
  v.addElement(pmt);
  pot = new ListPotential(v);
  
  
  list = sender.getNeighbourList();
  
  for (i=0 ; i<list.size() ; i++) {
    nt = list.elementAt(i);
    label = nt.getNeighbour().getLabel();
    rel = nt.getMessage();
    
    // Combine the messages coming from the other neighbours
    
    if (label != recipient.getLabel()) { 
      pot = pot.combine((ListPotential)rel.getOtherValues());
      if (!((ListPotential)rel.getOtherValues()).getExact()) {
	isExact = false;
      }
    }
    else {
      incoming = (ListPotential)rel.getOtherValues();
      outwards = rel;
      
      separator =  rel.getVariables().getNodes();
      auxList = recipient.getNeighbourList();
      inwards = auxList.getMessage(sender);
    }
  } 
  // Now combine with the potential in the node.
  pot = pot.combine(aux);  

  
  // We combine in the list the potentials with variables that will
  // be eliminated afterwards.
  vars = varsToCombine(sender,recipient);  
  
  Potential unit;
  unit = makeUnitPotential();
  for (i=0 ; i<vars.size() ; i++) {
    pot.combinePotentialsOf(vars.elementAt(i),unit,
	  limitForPruning[0],lowLimitForPruning[0],infoMeasure);
  }

  pot = (ListPotential)(pot.marginalizePotential(separator,
		 limitForPruning[0],lowLimitForPruning[0],infoMeasure));

  // Now update the messages in the join tree.
  
  outwards.setValues(pot);
  inwards.setOtherValues(pot);
}


/**
 * Computes a subset of variables from <code>recipient</code>.
 * This subset of variables is calculated as the intersection among the
 * the sets of variables that are eliminated
 * when clique <code>recipient</code> sends a message to each one of
 * the other cliques distinct from <code>sender</code>
 * @param sender a <code>NodeJoinTree</code>.
 * @param recipient another <code>NodeJoinTree</code> that must be neighbour
 * to <code>sender</code>.
 * @return a <code>NodeList> with the set of variables.
 */

private NodeList varsToCombine(NodeJoinTree sender, NodeJoinTree recipient) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  Relation sep;
  NodeList varsInSeparator = null, varsEliminatedInSeparator = null,
           varsInSenderSeparator = null;
  NodeList vars = new NodeList();
  SetVectorOperations op = new SetVectorOperations();
  
  list = recipient.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    sep = list.elementAt(i).getMessage();
    if (other.getLabel() != sender.getLabel()) {
      varsInSeparator = sep.getVariables();
      varsEliminatedInSeparator = new NodeList(op.notIn(
		recipient.getNodeRelation().getVariables().getNodes(),
		varsInSeparator.getNodes()));
      if (i == 0) {
	vars = new NodeList(varsEliminatedInSeparator.getNodes());
      }
      else {
	vars = new NodeList((op.union(vars.getNodes(),
			     varsEliminatedInSeparator.getNodes())));
      }
    }
    else {
      varsInSenderSeparator = sep.getVariables();
    }
  }
  vars = new NodeList(op.intersection(vars.getNodes(),
                                      varsInSenderSeparator.getNodes()));
  return vars;
}


/**
 * Computes the marginals after a propagation and put them into the
 * instance variable <code>results</code>. Sets <code>positions</code>
 * for each variable.
 */

public void computeMarginals() {
  
  int i, j, k, nv, pos;
  Vector leaves, marginal;
  NodeList variables;
  Relation r1, r2;
  ListPotential listPot;
  Potential pmt;
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
      listPot = (ListPotential)r1.getValues();
 
      for (k=0 ; k<temp.getNeighbourList().size() ; k++) {
	nt = temp.getNeighbourList().elementAt(k);
	r2 = nt.getMessage();
	listPot = listPot.combine((ListPotential)r2.getOtherValues());
      }
      
      marginal = new Vector();
      marginal.addElement(v);
      listPot = (ListPotential)(listPot.marginalizePotential(marginal));
      
      pmt = listPot.createPotential();
      
      pmt.normalize();
      results.addElement(pmt);
      
      positions.put(v,new Integer(posResult));
      posResult++;
    }
  }
}


/**
 * Makes a unit potential:
 * @return the unit potential.
 */

Potential makeUnitPotential() {
  
  PotentialMTree pmt;
  
  pmt = new PotentialMTree();
  pmt.setTree(MultipleTree.unitTree());
  return pmt;
}

} // End of class
