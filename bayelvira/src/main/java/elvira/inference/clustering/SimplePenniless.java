/* SimplePenniless.java */

package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.potential.ProbabilityTree;
import elvira.potential.PotentialTable;
import elvira.potential.PotentialTree;
import elvira.potential.Potential;

/**
 * Class <code>SimplePenniless</code>.
 * Implements an approximate propagation method over a
 * binary join tree. An approximation is carried out 
 * after each operation (combination and marginalization) with potentials.
 * This method of propagation uses <code>PotentialTree</code>s to carry out
 * operations, but initials relations can be of any class. 
 * This method is similar to Penniless propagation (@see Penniless), but
 * here only two propagation stages are done in the tree of cliques.
 * Override 
 * <code>convertPotential(Potential)</code> and
 * <code>makeUnitPotential(NodeList nlist)</code> in subclases to get other
 * behaviour.
 * 
 * @author Antonio Salmerón
 * @author Andrés Cano
 * @author Serafín Moral
 * @since 30/11/2001
 */


public class SimplePenniless extends Propagation {
  
/**
 * The binary join tree.
 */  
JoinTree binTree;

/**
 * An integer containing the maximum size of a potential
 * that is going to be approximated with 
 * <code>PotentialTree.sortAndBound(maximumSize)</code>
 */
public int maximumSize;

/**
 * A  boolean value.
 * It tells us if the algorithm carries out a 
 * <code>PotentialTree.sortAndBound(maximumSize)</code> after operations
 * (combination and marginalization).
 */
public boolean sortAndBound;

/**
 * Limit of error to approximate the result of an
 * operation (combination and marginalization) with 
 * <code>PotentialTree.limitBound(double limit)</code>.
 */
public double limitForPruning;

/**
 * Used to prune leaves whose addition is lower than
 * a fraction of the addition of the entire potential.
 * That fraction is indicated by this parameter.
 * <code>PotentialTree.limitBound(double, double)</code>.
 */
public double limitSumForPruning;

/**
 * Low limit of error to approximate initial <code>Potential</code>s with
 * <code>PotentialTree.limitBound(double limit)</code>.
 */
public double lowLimitForPruning;

/**
  * A <code>Hashtable</code> with the <code>NodeJoinTree</code>
  * that will be used to obtain the marginal for each variable
  */
public Hashtable marginalCliques;

/**
  * The way in which we approximate several leaves by a single double value
  * @see <code>elvira.potential.ProbabilityTree</code> for possible kinds
  */
int kindOfApprPruning;

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
 * <li> A double; limit for pruning.
 * <li> A double; low limit for pruning.
 * <li> A double; limit sum for pruning.
 * <li> An integer indicating the maximum potential size.
 * <li> A value true or false  indicating if we do
 * <code>sortAndBound</code> or not.
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
  SimplePenniless propagation;
  int i, m, ls, triangMethod;
  boolean sortAndBound;
  double[] errors;
  double g, mse, mae, lp, llp, lsp, timePropagating, timeCompiling;
  Date date, dateCompiling;
  FileWriter f;
  PrintWriter p;
  
  if (args.length < 11) {
    System.out.print("Too few arguments. Arguments are: ElviraFile");
    System.out.print(" OutputFile OutputErrorFile InputExactResultsFile");
    System.out.print(" kindOfApprPruning(AVERAGE|ZERO) LimitForPruning LowLimitForPruning LimitSumForPruning MaxTreeSize");
    System.out.println(" SortAndBound(true|false) TriangulationMethod(0|1|2) [EvidenceFile]");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);
      
    if (args.length == 12) {
      evidenceFile = new FileInputStream(args[11]);
      e = new Evidence(evidenceFile,b.getNodeList());
      System.out.println("Evidence file"+args[11]);
    }
    else {
      e = new Evidence();
    }			  	
    
    lp = (Double.valueOf(args[5])).doubleValue();
    llp = (Double.valueOf(args[6])).doubleValue();
    lsp = (Double.valueOf(args[7])).doubleValue();
    triangMethod = (Integer.valueOf(args[10])).intValue();

    System.out.println("limit for pruning: "+lp);
    System.out.println("limit sum for pruning: "+lsp);
    ls  = (Integer.valueOf(args[8])).intValue();
      
    dateCompiling = new Date();
    timeCompiling = (double)dateCompiling.getTime();
    
    sortAndBound = (Boolean.valueOf(args[9])).booleanValue();
    
    propagation = new SimplePenniless(b,e,lp,llp,lsp,ls,sortAndBound,triangMethod);
    propagation.setKindOfApprPruning(args[4]);
    
    dateCompiling = new Date();
    timeCompiling = ((double)dateCompiling.getTime()-timeCompiling) / 1000;
      
    date = new Date();
    timePropagating = (double)date.getTime();
    propagation.propagate(args[1]);
      
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
      
    
    
    

    f = new FileWriter(args[2]);
    p = new PrintWriter(f);

    p.println("Low limit: "+lp);
    p.println("Low limit for pruning: "+llp);
    p.println("Limit sum for pruning: "+lsp);
    p.println("Max size when bound: "+ls);
    p.println("Sort and Bound: "+sortAndBound);
    p.println("Triangulation method: "+triangMethod);

    p.println("Time compiling (secs) : "+timeCompiling);
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
    f.close();
    System.out.println("Done"); 
  }
}


/**
 * Creates an empty object.
 */

SimplePenniless() {
  kindOfApprPruning=ProbabilityTree.AVERAGE_APPROX;
}


/**
 * Creates a new propagation.
 * @param b a belief network.
 * @param e an evidence.
 * @param lp the limit for pruning.
 * @param llp low limit for pruning.
 * @param lsp lower sum for pruning.
 * @param ls the maximum sizes for potentials.
 * @param sortAndBound boolean that indicates whether we must or not
 * do <code>sortAndBound</code>.
 * @param triangMethod indicates the triangulation to carry out.
 * <ol>
 * <li> 0 is for considering evidence during the triangulation.
 * <li> 1 is for considering evidence and directly remove relations
 * that are conditional distributions when the conditioned variable
 * is removed.
 * <li> 2 is for not considering evidence.
 * </ol>
 */

SimplePenniless(Bnet b, Evidence e, double lp, double llp, double lsp,
		int ls, boolean sortAndBound, int triangMethod) {
  this();  

  RelationList irTree;
  int i;
  Potential pt;

  setMaximumSize(ls);
  setSortAndBound(sortAndBound);
  setLimitForPruning(lp);
  setLowLimitForPruning(llp);
  setLimitSumForPruning(lsp);
  
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
    irTree.elementAt(i).getValues().limitBound(lowLimitForPruning);
  }

  marginalCliques = binTree.Leaves(irTree);
  binTree.binTree();
  binTree.setLabels();
//System.out.println("JOIN TREE");
//binTree.display();
}

/**
 * Sets the kind of prunning
 * @see <code>elvira.potential.ProbabilityTree</code> for possible values
 */
public void setKindOfApprPruning(String kind){
  if(kind.equals("AVERAGE")){
    kindOfApprPruning=ProbabilityTree.AVERAGE_APPROX;
  }
  else if(kind.equals("ZERO")){
    kindOfApprPruning=ProbabilityTree.ZERO_APPROX;
  }
  else{
    System.out.println("Error in SimplePenniless.setKindOfApprPruning: ilegal value for kind="+kind);
    System.exit(1);
  }
}

/**
 * Sets the limit for pruning.
 * @param lp the information limit for pruning.
 */

public void setLimitForPruning(double lp) {
  
  limitForPruning = 1+((0.5-lp) * Math.log(0.5-lp) /
		       Math.log(2) +
		       (0.5+lp) * Math.log(0.5+lp)/
		       Math.log(2));
}

/**
 * Sets the limit sum for pruning.
 * @param lsp the limit value for pruning.
 */

public void setLimitSumForPruning(double lsp) {
  
  limitSumForPruning = lsp;
}


/**
 * Sets the lower limit for pruning.
 * @param llp the information <code>lowLimit</code> for pruning.
 */

public void setLowLimitForPruning(double llp) {
  
  lowLimitForPruning = 1+((0.5-llp) * Math.log(0.5-llp) /
		       Math.log(2) +
		       (0.5+llp) * Math.log(0.5+llp)/
		       Math.log(2));
}


/**
 * Sets the maximum size of probability trees during the propagation.
 * @param ls an integer with the maximum size.
 */

public void setMaximumSize(int ls) {
  
  maximumSize = ls;
}


/**
 * Sets <code>sortAndBound</code> for the propagation algorithm.
 * @param sAB is a boolean  to set in <code>sortAndBound</code>.
 */

public void setSortAndBound(boolean sAB) {
  
  sortAndBound = sAB;  
}


/**
 * Initializes all potentials in messages and cliques to 1, except those 
 * potentials in cliques corresponding to leaf nodes, which will contain the 
 * potential in the node.
 */
  
public void initMessages() {
  
  Relation r;
  int i, j;
  Potential pot;
  NodeJoinTree node;
  NeighbourTree nt;
  NeighbourTreeList ntl;  

  for (i=0 ; i<binTree.getJoinTreeNodes().size() ; i++) {
    // Set the potentials in the cliques to 1.
    node = binTree.elementAt(i);    
    r = node.getNodeRelation();
    if ((r.getValues() == null))  {
      pot = makeUnitPotential(r.getVariables());
      r.setValues(pot);
    }
    
    // Now set the messages
    ntl = node.getNeighbourList();
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      r = nt.getMessage();
      pot = makeUnitPotential(r.getVariables());
      r.setValues(pot);
      pot = makeUnitPotential(r.getVariables());
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
    
  navigate(root);

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
 * Sends messages from leaves to the root, and from the root to the leaves.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

protected void navigate(NodeJoinTree sender) {
  
  Date date;
  double timePropagating;
  
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
 * Sends messages from leaves to root (sender).
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

protected void navigateUp(NodeJoinTree sender) {
  
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
 * Sends messages from leaves to root (sender) through the branch towards
 * the recipient.
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

protected void navigateDown(NodeJoinTree sender) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  
  list = sender.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    sendMessage(sender,other,true);
    navigateDown(sender,other);
  }
}


/**
 * Sends messages from root (sender) to leaves through the brach
 * towards node recipient.
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
      sendMessage(recipient,other,true);
      navigateDown(recipient,other);
    }
  }
}


/**
 * Gets the initial relations present in the network.
 * @return a <code>RelationList</code> with the initial relations present in
 * the network, where potentials will be transformed to
 * <code>PotentialTree</code>. <p>
 * Override <code>convertPotential(Potential)</code> in subclases to get
 * other behaviour.
 * @see convertPotential(Potential pot)
 */

public RelationList getInitialRelations() {
  
  Relation rel, newRel;
  RelationList list;
  Potential pt;
  int i;
  Vector v;
 
  list = new RelationList();
  for (i=0 ; i<network.getRelationList().size() ; i++) {
    rel = (Relation)network.getRelationList().elementAt(i);
    newRel = new Relation();
    newRel.setVariables(rel.getVariables().copy());    
    pt = convertPotential(rel.getValues());
    
    newRel.setValues(pt);
    newRel.setKind(rel.getKind());
    
    list.insertRelation(newRel);
  }
  
  return list;
}


/**
 * Computes the marginals after a propagation and put them into
 * instance variable <code>results</code>. Sets <code>positions</code>
 * for each variable, to locate the variable in <code>Vector results</code>.
 */

public void computeMarginals() {
  
  int i, j, k, nv, pos;
  Vector leaves, marginal;
  NodeList variables;
  Relation r1, r2;
  Potential pot, potAux;
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
      pot = r1.getValues();
 
      for (k=0 ; k<temp.getNeighbourList().size() ; k++) {
	nt = temp.getNeighbourList().elementAt(k);
	r2 = nt.getMessage();
	pot = pot.combine(r2.getOtherValues());
      }
      marginal = new Vector();
      marginal.addElement(v);
      pot = (pot.marginalizePotential(marginal));
      potAux=pot.normalize(pot);
      results.addElement(potAux);
      
      positions.put(v,new Integer(posResult));
      posResult++;
    }
  }
}


/**
 * Sends a message from a node to another one.
 * Marks the messages as not exact when this method carries out an
 * approximation or at least one of the input messages is not exact. 
 * The message is computed by combining all the messages inwards
 * the sender except that one comming from the recipient. Then, the
 * result is sorted and bounded conditional to the message
 * going from the recipient to the sender.
 * It is required that the nodes in the tree be labeled.
 * Use method <code>setLabels</code> if necessary.
 *
 * @param sender the node that send the message.
 * @param recipient the node that receives the message.
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient, boolean down) {
  
  NeighbourTreeList list, auxList;
  NeighbourTree nt;
  Potential aux,pot;
  Relation rel, outwards, inwards;
  int i, label;
  Vector separator;
  
  inwards = new Relation();
  outwards = new Relation();
  separator = new Vector();
  
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
      
 //     pot.limitBound(maximumSize);
     
   //   if (sortAndBound) {
     //   pot = pot.sortAndBound(maximumSize);
    //  }
    //  pot.limitBound(limitForPruning);
      
    }
    else {     
      outwards = rel;
      separator =  rel.getVariables().getNodes();
      auxList = recipient.getNeighbourList();
      inwards = auxList.getMessage(sender); 
    }
  }
  
  // Now combine with the potential in the node.
  if (down) {  
    pot.limitBound(kindOfApprPruning,limitForPruning,limitSumForPruning);
    if (sortAndBound) {
      pot = pot.sortAndBound(maximumSize);  
    }
  }
  
  pot = pot.marginalizePotential(separator); 
 
  //System.out.println("Antes de lb "+pot.getSize());
  // pot.limitBound(limitForPruning);

  if(down){
    pot.limitBound(kindOfApprPruning,limitForPruning,limitSumForPruning);
  }
  else{
    pot.limitBound(ProbabilityTree.AVERAGE_APPROX,limitForPruning,
                   limitSumForPruning);
  }
  //System.out.println("Despues de lb "+pot.getSize());
   
  if (sortAndBound) {
    pot = pot.sortAndBound(maximumSize);  
  }
  
  // Prueba
  //System.out.println("Antes "+pot.getSize());
  //pot.limitBound((int)maximumSize);
  //System.out.println("Despues "+pot.getSize());

  // Now update the messages in the join tree. 
  outwards.setValues(pot);
  inwards.setOtherValues(pot);
}


/**
 * Converts  <code>pot</code> to a <code>PotentialTree</code>
 * Override this method in subclases to get other behaviour.
 * This method is called by <code>getInitialRelations()</code>
 * @param pot the <code>Potential</code> to be converted to 
 * <code>PotentialTree</code>
 * @return the <code>Potential</code> <code>pot</code> converted to a 
 * <code>PotentialTree</code>
 * @see getInitialRelations()
 */

Potential convertPotential(Potential pot) {

  Potential pt;
  
  if (pot.getClassName().equals("PotentialTree")) {
    pt = pot;
  }
  else
    pt = new PotentialTree(pot); 
  return pt;
}


/**
 * Makes an identity <code>Potential</code> for the 
 * <code>NodeList nList</code> of the class used by this propagation 
 * method (<code>PotentialTree</code>). Override this method in subclases 
 * to get other behaviour.
 * @param nList a <code>NodeList</code>
 * @return an identity <code>PotentialTree</code>
 */

Potential makeUnitPotential(NodeList nList) {
  
  PotentialTree pmt;
  
  pmt = new PotentialTree(nList);
  pmt.setTree(ProbabilityTree.unitTree());
  return pmt;
}

} // End of class
