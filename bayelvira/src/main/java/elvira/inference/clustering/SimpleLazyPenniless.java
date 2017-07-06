package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;

import elvira.potential.Potential;
import elvira.potential.PotentialTree;
import elvira.potential.ProbabilityTree;
import elvira.potential.ListPotential;

/**
 * Class <code>SimpleLazyPenniless</code>.
 * Implements the Penniless Propagation algorithm over a binary
 * join tree, but in this version, messages in the join tree are
 * stored in factorized form by means of <code>ListPotential</code>,
 * and operations are performed using Lazy Evaluation.
 * In theory, original potentials can be of any class in the <code>Bnet</code>
 * object.
 * Every potential will be transformed to a <code>PotentialTree</code> in 
 * <code>getInitialRelations()</code>.
 * Lazy evaluation can be of different kinds when a message is sent between
 * two cliques (@see whenJoinPotentials).
 * This method of propagation allows the use of a cache, where the calculus of
 * combinations of pairs of <code>Potential</code>s are saved 
 * (@see doubleCache). By now, it should be used only
 * with pure lazy evaluation (whenJoinPotentials=0). Do doubleCache=true
 * to use the cache.
 * Also, this method of propagation allows to select among different
 * heuristics to choose which two <code>Potential</code>s combine when
 * ListPotential.createPotential(double limitForPrunning,int heuristic)
 * is called. In the future, this possibility can be also applied to 
 * ListPotential.createPotential()
 * This algorithm is equivalent to exact lazy propagation when the
 * error limits are set to zero (limit for pruning, low limit and
 * limit sum).
 *
 * The reference to this algorithm is:
 * A. Cano, S. Moral, A. Salmeron (2002). Lazy evaluation in Penniless
 * propagation over join trees. Networks, 39:175-185.
 *
 * @author Antonio Salmerón
 * @author Andrés Cano
 * @author Serafín Moral
 * @since 26/7/2002
 */


public class SimpleLazyPenniless extends SimplePenniless {

/**
 * The method to decide when joining (combining) potentials in the 
 * <code>ListPotential</code> of a calculated message between two cliques.
 * Possible values are:
 * <ul>
 * <li> 0: Never join potentials (only when it is obligatory) (pure lazy 
 * evaluation).
 * <li> 1: Join potentials of the variables that will be obligatory to
 * sum out in all the adjacent messages. (intersection)
 * <li> 2: Join potentials of the variables that will be obligatory to
 * sum out in one of the adjacent messages. (union)
 * <li> 3: Join all the potentials of the <code>ListPotential</code>.
 * <li> 4: Join two potentials if one is included into the other.
 * <li> 5: Never join potentials and do not combine when removing
 * a variable.
 * </ul>
 */
int whenJoinPotentials;

/**
 * A Hash table where we save combinations done between pairs of 
 * <code>Potential</code>s in createPotential(double limitForPruning,
 * Hashtable cache1,Hashtable cache2,boolean firstTour). These combinations
 * are not done completely (only variables are combined). This cache
 * is built in the first navigation.
 */
HashMap cache1;

/**
 * A Hash table where we save combinations done between pairs of 
 * <code>Potential</code>s in createPotential(double limitForPruning,
 * Hashtable cache1,Hashtable cache2,boolean firstTour). These combinations
 * are done completely. This cache is built in the second navigation.
 */
HashMap cache2;

/**
 * A Hash table where we save marginalizations of one variable on a 
 * <code>Potential</code>. In this hash table the key is a 
 * <code>PotentialVar</code>, that is, a pair 
 * <code>Potential</code>-<code>Node</code>, and the value associated
 * to a key is the resulting <code>Potential</code> obtained suming out the
 * <code>Node</code> on the <code>Potential</code>.
 * The resulting <code>Potential</code>s in this hash table are only 
 * marginalized with variables.
 */
HashMap cache1M;

/**
 * A Hash table where we save marginalizations of one variable on a 
 * <code>Potential</code>. In this hash table the key is a 
 * <code>PotentialVar</code>, that is, a pair 
 * <code>Potential</code>-<code>Node</code>, and the value associated
 * to a key is the resulting <code>Potential</code> obtained suming out the
 * <code>Node</code> on the <code>Potential</code>.
 * The resulting <code>Potential</code>s in this hash table are 
 * completely calculated.
 */
HashMap cache2M;

/**
 * A <code>boolean</code> that indicates whether we are in the first
 * navigation (<code>true</code>) or second (<code>false</code>) when we are
 * using cache (<code>doubleCache=true</code>).
 */
boolean firstTour;

/**
 * A <code>boolean</code> to decide whether we use a cache to save
 * combinations of <code>Potential</code> (doubleCache=true) or not
 * (doubleCache=false).
 */
boolean doubleCache;

/**
 * The heuristic used to select which two <code>Potential</code>s will be
 * combined first in a <code>ListPotential</code>. See
 * <code>ListPotential</code> for possible values.
 */
int heuristicToJoin;


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
 * <li> Method to Join Potentials (0|1|2|3|4|5)
 * <li> A double; limit for pruning.
 * <li> A double; low limit for pruning.
 * <li> A double; limit sum for pruning.
 * <li> An integer indicating the maximum potential size.
 * <li> A value <code>true</code> or <code>false</code>
 * indicating wether we do <code>sortAndBound</code> or not.
 * <li> A value <code>true</code> or <code>false</code>
 * indicating wether we use cache or not.
 * <li> A value indicating the heuristic to JoinInCreatePots(0|1|2|3|4|5).
 * <li> The triangulation method (0|1|2).
 * <li> File with instantiations.
 * </ol>
 * The last argument can be omitted. In that case, it will
 * be considered that no observations are present.
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  SimpleLazyPenniless propagation;
  int i, ls, method, heurToJoin, triangMethod;
  double lp, llp, lsp, g, mse, mae, timePropagating, timeCompiling;
  double[] errors;
  boolean sortAndBound, useCache;
  
  Date date, dateCompiling;
  FileWriter f;
  PrintWriter p;
  
  if (args.length < 13) {
    System.out.print("Too few arguments. Arguments are: ElviraFile");
    System.out.print(" OutputFile OutputErrorFile InputExactResultsFile");
    System.out.print(" MethodToJoinPotentials(0|1|2|3|4|5) LimitForPruning LowLimitForPruning LimitSumForPruning MaxSizePotential");
    System.out.print("MaxSizeWhenBound SortAndBound(true|false) ");
    System.out.print("Cache(true|false) HeuristicToJoinInCreatePots(0|1|2|3|4|5) ");
    System.out.println("TriangulationMethod(0|1|2) [EvidenceFile]");
  }
  else {
    
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);
    
    if (args.length == 14) {
      evidenceFile = new FileInputStream(args[13]);
      e = new Evidence(evidenceFile,b.getNodeList());
      System.out.println("Evidence file"+args[13]);
    }
    else
      e = new Evidence();
    
    ls = (Integer.valueOf(args[8])).intValue();
    sortAndBound = (Boolean.valueOf(args[9])).booleanValue();
    
    method = (Integer.valueOf(args[4])).intValue();
    lp = (Double.valueOf(args[5])).doubleValue();
    llp = (Double.valueOf(args[6])).doubleValue();
    lsp = (Double.valueOf(args[7])).doubleValue();
    useCache = (Boolean.valueOf(args[10])).booleanValue();
    heurToJoin = (Integer.valueOf(args[11])).intValue();
    triangMethod = (Integer.valueOf(args[12])).intValue();
    
    System.out.println("Method to join potentials: "+ method); 
    System.out.println("Low limit: "+lp);
    System.out.println("Low limit for pruning: "+llp);
    System.out.println("Limit sum for pruning: "+lsp);
    System.out.println("Max size when bound: "+ls);
    System.out.println("Sort and Bound: "+sortAndBound);
    System.out.println("Use cache: "+useCache);
    System.out.println("Heuristic to JoinInCreatePots: "+heurToJoin);
    System.out.println("Triangulation method: "+triangMethod);
    
    dateCompiling = new Date();
    timeCompiling = (double)dateCompiling.getTime();
    
    propagation = new SimpleLazyPenniless(b,e,method,lp,llp,lsp,ls,
					  sortAndBound,
					  useCache, heurToJoin,
					  triangMethod);

    dateCompiling = new Date();
    timeCompiling = ((double)dateCompiling.getTime()-timeCompiling) / 1000;

    date = new Date();
    timePropagating = (double)date.getTime();
    propagation.propagate(args[1]);
    
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
    
    
    f = new FileWriter(args[2]);
    p = new PrintWriter(f);
    
    p.println("Method to join potentials: "+ method); 
    p.println("Low limit: "+lp);
    p.println("Low limit for pruning: "+llp);
    p.println("Limit sum for pruning: "+lsp);
    p.println("Max size when bound: "+ls);
    p.println("Sort and Bound: "+sortAndBound);
    p.println("Use cache: "+useCache);
    p.println("Heuristic to JoinInCreatePots: "+heurToJoin);
    p.println("Triangulation method: "+triangMethod);

    p.println("\nTime compiling (secs) : "+timeCompiling);
    p.println("Time propagating (secs) : "+timePropagating);
    
    
    if (!args[3].equals("NORESULTS")) {
      System.out.println("Reading exact results");
      propagation.readExactResults(args[3]);
      System.out.println("Exact results read");
      
      System.out.println("Computing errors");
      errors = new double[2];
      propagation.computeError(errors);
      mae = propagation.computeMaxAbsoluteError();
      g = errors[0];
      mse = errors[1];
      
      propagation.computeKLError(errors);
      p.println("G : "+g);
      p.println("MSE : "+mse);
      p.println("Max absoulte error : "+mae);
      p.println("KL error : "+errors[0]);
      p.println("Std. deviation of KL-error : "+errors[1]);
    }
    
    propagation.binTree.calculateStatistics();
    propagation.binTree.saveStatistics(p);
    
    if (propagation.doubleCache) {
      p.println("\n\n Size of cache 1: "+propagation.cache1.size());
      p.println("Size of cache 2: "+propagation.cache2.size());
      p.println("Size of cache marg: "+propagation.cache1M.size());
      p.println("Size of cache marg: "+propagation.cache2M.size());
    }
    f.close();
    
    System.out.println("Done"); 
  }
}

  
/**
 * Creates an empty object. Necessary for subclass definition.
 */

SimpleLazyPenniless() {
  
  whenJoinPotentials = 0;  
}


 /**
 * Creates a new propagation. This method is just for compatibility
 * with GUI, but should be removed as soon as the graphical
 * interface is adapted to the new set of parameters of
 * Lazy algorithm.
 *
 * @param b a belief network.
 * @param e an evidence.
 * @param method the method to join potentials in <code>ListPotential</code> 
 * @see whenJoinPotentials
 * @param lp the limit for pruning.
 * @param llp the lowLimit for pruning;
 * @param ls the maximum sizes for potentials.
 * @param sortAndBound a boolean that indicates whether we will
 * @param useCache a boolean to decide if we use cache.
 * @param heuristic the heuristic to select two <code>Potential</code>s
 * in createPotential(double limitBound,int heuristic)
 * do <code>sortAndBound</code> when approximating potentials.
 */

public SimpleLazyPenniless(Bnet b, Evidence e, int method, double lp,
                          double llp, int ls,
                          boolean sortAndBound,
                          boolean useCache, int heuristic) {
  
  Triangulation triang;
  RelationList rel1, rel2, ir, irTree;
  NodeList numeration = new NodeList();
  int i, j;
  ListPotential listPot, newPot;
  Potential pt;
  Relation newRel, rel;
  Vector newList;
  
  setUseCache(useCache);
  setHeuristicToJoin(heuristic);
  
  setMaximumSize(ls);
  setSortAndBound(sortAndBound);
  setLimitForPruning(lp);
  setLowLimitForPruning(llp);
  setLimitSumForPruning(0.01);
  
  if (doubleCache) {
    cache1 = new HashMap(200);
    cache2 = new HashMap(20);
    cache1M = new HashMap();
    cache2M = new HashMap();
  }
  firstTour = true;

  observations = e;
  network = b;
  whenJoinPotentials = method;
  positions = new Hashtable();
  binTree = new JoinTree(b,e);
  irTree = getInitialRelations();
  irTree.restrictToObservations(observations);
  
  for (i=0 ; i<irTree.size() ; i++){
    ((ListPotential)irTree.elementAt(i).getValues()).limitBound(lowLimitForPruning);
  }

  ir = irTree;

  marginalCliques = binTree.Leaves(ir);

  binTree.binTree();
 
  binTree.setLabels();
  
  // Garbage collection
  //System.gc();
}

/**
 * Creates a new propagation.
 *
 * @param b a belief network.
 * @param e an evidence.
 * @param method the method to join potentials in <code>ListPotential</code> 
 * @see whenJoinPotentials
 * @param lp the limit for pruning.
 * @param llp the lowLimit for pruning;
 * @param lsp the limit sum for pruning.
 * @param ls the maximum sizes for potentials.
 * @param sortAndBound a boolean that indicates whether we will
 * @param useCache a boolean to decide if we use cache.
 * @param heuristic the heuristic to select two <code>Potential</code>s
 * in createPotential(double limitBound,int heuristic)
 * do <code>sortAndBound</code> when approximating potentials.
 * @param triangMethod indicates the triangulation to carry out.
 * <ol>
 * <li> 0 is for considering evidence during the triangulation.
 * <li> 1 is for considering evidence and directly remove relations
 * that are conditional distributions when the conditioned variable
 * is removed.
 * <li> 2 is for not considering evidence.
 * </ol>
 */

public SimpleLazyPenniless(Bnet b, Evidence e, int method, double lp,
			   double llp, double lsp, int ls,
			   boolean sortAndBound,
			   boolean useCache, int heuristic,
			   int triangMethod) {
  
  Triangulation triang;
  RelationList rel1, rel2, ir, irTree;
  NodeList numeration = new NodeList();
  int i, j;
  ListPotential listPot, newPot;
  Potential pt;
  Relation newRel, rel;
  Vector newList;
  
  setUseCache(useCache);
  setHeuristicToJoin(heuristic);
  
  setMaximumSize(ls);
  setSortAndBound(sortAndBound);
  setLimitForPruning(lp);
  setLowLimitForPruning(llp);
  setLimitSumForPruning(lsp);
  
  if (doubleCache) {
    cache1 = new HashMap(200);
    cache2 = new HashMap(20);
    cache1M = new HashMap();
    cache2M = new HashMap();
  }
  firstTour = true;

  observations = e;
  network = b;
  whenJoinPotentials = method;
  positions = new Hashtable();
  
  if (triangMethod == 2)
    binTree = new JoinTree(b);
  else
    binTree = new JoinTree(b,e,triangMethod);
  
  irTree = getInitialRelations();
  irTree.restrictToObservations(observations);
  
  for (i=0 ; i<irTree.size() ; i++){
    ((ListPotential)irTree.elementAt(i).getValues()).limitBound(lowLimitForPruning);
  }

  ir = irTree;

  marginalCliques = binTree.Leaves(ir);

  binTree.binTree();
 
  binTree.setLabels();
  
  // Garbage collection
  //System.gc();
}


/**
 * Sends messages from the leaves to the root, and from the root to the leaves.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

protected void navigate(NodeJoinTree sender) {
  
  Date date;
  double timePropagating;
  
  if (doubleCache) {
    date = new Date();
    timePropagating = (double)date.getTime(); 
    navigateUp(sender);
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
    System.out.println("Time navigateUp first tour: "+ timePropagating);
  
    date = new Date();
    timePropagating = (double)date.getTime();
    navigateDown(sender);
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
    System.out.println("Time navigateDown first tour: "+ timePropagating);
  }
  
  firstTour = false;
  date = new Date();
  timePropagating = (double)date.getTime(); 
  navigateUp(sender);
  date = new Date();
  timePropagating = ((double)date.getTime()-timePropagating) / 1000;
  System.out.println("Time navigateUp: "+ timePropagating);
  
  date = new Date();
  timePropagating = (double)date.getTime();
  navigateDown(sender);
  date = new Date();
  timePropagating = ((double)date.getTime()-timePropagating) / 1000;
  System.out.println("Time navigateDown: "+ timePropagating);
}


/**
 * Sets <code>doubleCache</code>.
 * @param useCache the new value for <code>doubleCache</code>.
 */

public void setUseCache(boolean useCache) {
  
  doubleCache = useCache;
}


/**
 * Sets <code>firstTour</code>.
 * @param ft the new value for <code>firstTour</code>.
 */

public void setFirstTour(boolean ft) {
  
  firstTour = ft;
}


/**
 * Sets <code>whenJoinPotentials</code>.
 * @param w the new value for <code>whenJoinPotentials</code>.
 */

public void setWhenJoinPotentials(int w) {
  
  whenJoinPotentials = w;
}


/**
 * Sets <code>heuristicToJoin</code>.
 * @param heuristic the new value for <code>heuristicToJoin</code>.
 */

public void setHeuristicToJoin(int heuristic) {
  
  heuristicToJoin = heuristic;
}


/**
 * To keep compatibility with SimplePenniless, more
 * precisely, with method navigateDown.
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient, boolean b) {

  sendMessage(sender,recipient);
}


/**
 * Sends a message from a node to another one.
 * The message is computed by combining all the messages inwards
 * the sender except that one comming from the recipient. The output 
 * <code>Potential</code> of every operation is approximated with 
 * <code>Potential.limitBound(double limit)</code>.
 * It is required that the nodes in the tree be labeled. Use method
 * <code>setLabels</code> if necessary.
 *
 * @param sender the node that send the message.
 * @param recipient the node that receives the message.
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient) {
  
  NeighbourTreeList list, auxList;
  NeighbourTree nt;
  Potential pmt, aux, pot;
  Relation rel, outwards, inwards;
  int i, label,  listSize;
  Vector separator, v;
  NodeList vars;
  ListPotential listPot1, listPot2;
    
  inwards = new Relation();
  outwards = new Relation();
  separator = new Vector();
  
  // Now combine with the potential in the node.
  aux = sender.getNodeRelation().getValues();
  pot = aux;  
  
  list = sender.getNeighbourList();
  
  for (i=0 ; i<list.size() ; i++) {
    nt = list.elementAt(i);
    label = nt.getNeighbour().getLabel();
    rel = nt.getMessage();
    
    // Combine the messages coming from the other neighbours
    
    if (label != recipient.getLabel()) { 
      pot = pot.combine(rel.getOtherValues());
    }
    else {
      outwards = rel;    
      separator =  rel.getVariables().getNodes();
      auxList = recipient.getNeighbourList();
      inwards = auxList.getMessage(sender);
    }
  } 
    
  // Decide which potentials in the list will be joined
  if ((whenJoinPotentials == 0) || (whenJoinPotentials == 5)) {
    // Never join potentials.
    // Do nothing
  }
  else if ((whenJoinPotentials == 1)||(whenJoinPotentials == 2)) {
    // We combine in the list the potentials with variables that will
    // be eliminated afterwards.
    vars = varsToCombine(sender,recipient,whenJoinPotentials);  
    for (i=0 ; i<vars.size() ; i++) {
      if(doubleCache){
        ((ListPotential)pot).combinePotentialsOf(vars.elementAt(i),
              limitForPruning,limitSumForPruning,cache1,cache2,
              firstTour,heuristicToJoin);
      }
      else{
        ((ListPotential)pot).combinePotentialsOf(vars.elementAt(i),
					       limitForPruning,
					       limitSumForPruning);
      }
    }
  }
  else if (whenJoinPotentials == 3) {
    // We combine all the potentials in the list
    Potential potAux;
    if(doubleCache)
      potAux = ((ListPotential)pot).createPotential(limitForPruning,
               limitSumForPruning,cache1,cache2,firstTour,heuristicToJoin);
    else
      potAux = ((ListPotential)pot).createPotential(limitForPruning,
						  limitSumForPruning);
    if(potAux!=null){ // potAux is null when pot contains no potential
      pot = new ListPotential(potAux);
    }
   /* if (potAux == null) {
      potAux = makeUnitPotential();
    }
    pot = new ListPotential(potAux);*/
  }
  else if (whenJoinPotentials == 4) {
    if(!doubleCache)
       ((ListPotential)pot).joinPotentials(limitForPruning,limitSumForPruning);
    else {
      System.out.println("ERROR: whenJoinPotentials == 4 cannot be used with cache");
      System.exit(1);
    }
  }
  else {
    System.out.println("Error in SimpleLazyPenniless.sendMessage(): "+
		       "wrong value for whenJoinPotentials="+
		       whenJoinPotentials);
    System.exit(1);
  }
  
  if (whenJoinPotentials == 5) {
    pot = ((ListPotential)pot).marginalizePotential(separator,
						    limitForPruning,
						    limitSumForPruning);
  }
  else {
    if(((ListPotential)pot).getListSize()>0){
    if (doubleCache)
      pot = ((ListPotential)pot).marginalizePotential(separator,
						      limitForPruning,
						      limitSumForPruning,
						      cache1,cache2,
						      cache1M,cache2M,
						      firstTour,
						      heuristicToJoin);  
    else
      pot = ((ListPotential)pot).marginalizePotential(separator,
						      limitForPruning,
						      heuristicToJoin,
						      limitSumForPruning); 
    } 
  }

  // Now perform sortAndBound if requested by the user.
  
  if (sortAndBound) {
    listPot2 = (ListPotential)pot;
    listPot1 = new ListPotential();
    listPot1.setVariables(listPot2.getVariables());
    
    listSize = listPot2.getListSize();
    
    for (i=0 ; i<listSize ; i++) {
      listPot1.insertPotential(listPot2.getPotentialAt(i).sortAndBound(maximumSize));
    }
    
    pot = listPot1;
  }
  
  // Now update the messages in the join tree.
  
  outwards.setValues(pot);
  inwards.setOtherValues(pot);

  // Garbage collection
  //System.gc();
}


/**
 * Computes a subset of variables from <code>recipient</code>.
 * This subset of variables is calculated as the intersection among the
 * the sets of variables that are eliminated
 * when clique <code>recipient</code> sends a message to each one of
 * the other cliques distinct from <code>sender</code>.
 * @param sender a <code>NodeJoinTree</code>.
 * @param recipient another <code>NodeJoinTree</code> that must be neighbour
 * to <code>sender</code>.
 * @method the method to decide which variables will be summed out.
 * <ul>
 * <li>1 for intersection
 * <li>2 for union
 * </ul>
 * @return a <code>NodeList</code> with the set of variables obtained.
 */

NodeList varsToCombine(NodeJoinTree sender, NodeJoinTree recipient,
			       int method) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  Relation sep;
  NodeList varsInSeparator = null,
           varsEliminatedInSeparator = null,
           varsInSenderSeparator = null,
	   vars = new NodeList();
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
        if (method == 1) {
	  vars = new NodeList((op.intersection(vars.getNodes(),
			       varsEliminatedInSeparator.getNodes())));
        }
        else if (method == 2) {
	  vars = new NodeList((op.union(vars.getNodes(),
			varsEliminatedInSeparator.getNodes())));
        }
        else {
          System.out.println("Error in SimpleLazyPenniless.varToCombine(): "+
             "wrong value for method="+method);
        }
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
 * Converts  <code>pot</code> to a <code>PotentialTree</code> and
 * then builds a <code>ListPotential</code> with that
 * <code>PotentialTree</code>.
 * This method is called by <code>getInitialRelations()</code>.
 * @param pot the <code>Potential</code> to be converted to
 * <code>ListPotential</code>.
 * @return the <code>Potential</code> <code>pot</code> converted to a
 * <code>ListPotential</code>.
 * @see getInitialRelations()
 */

Potential convertPotential(Potential pot) {

  Potential pt;
  ListPotential listPot;
  
  if (pot.getClassName().equals("PotentialTree")) {
    pt = pot;
  }
  else {
    pt = new PotentialTree(pot); 
  }
  Vector v = new Vector();
  v.addElement(pt);
  listPot = new ListPotential(v);

  return listPot;
}


/**
 * Makes an unitary <code>Potential</code> for the 
 * <code>NodeList nList</code> of the class used by this propagation 
 * method (<code>ListPotential</code>). Override this method in subclases 
 * to get other behaviour.
 * @param nList a <code>NodeList</code>.
 * @return an identity <code>ListPotential</code>
 */

Potential makeUnitPotential(NodeList nList) {
  
  ListPotential pmt;

  pmt = new ListPotential(nList);
  return pmt;
}


/**
 * Makes an unitary <code>Potential</code> of the class 
 * (<code>PotentialTree</code>) used by this propagation method .
 * @return an identity <code>PotentialTree</code>.
 */

Potential makeUnitPotential() {
  
  PotentialTree pmt;
  
  pmt = new PotentialTree();
  pmt.setTree(ProbabilityTree.unitTree());
  return pmt;
}

public static void printCache(HashMap cache){
  ListPotential pList;
  Potential potR;
  Iterator it=cache.keySet().iterator();
  while(it.hasNext()){
    pList=(ListPotential)it.next();
    potR=(Potential)cache.get(pList);
    System.out.println("KEY POTENTIALS:");
    pList.print();
    System.out.println("VALUE POTENTIAL:");
    potR.print();
  }
}

} // End of class
