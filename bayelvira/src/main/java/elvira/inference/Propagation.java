/* Propagation.java */

package elvira.inference;

import java.io.*;
import java.util.Vector;
import java.util.Dictionary;
import java.util.Hashtable;

import elvira.*;
import elvira.tools.*;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.parser.*;
import elvira.potential.PotentialTable;
import elvira.potential.Potential;


/**
 * Class Propagation. Implements a general setting for
 * performing propagations over a network.
 *
 * @since 21/12/2004
 */

public class Propagation    {
  
  /**
   * Contains the evidence with respect to which the propagation
   * will be carried out. This field must be initialized by any
   * constructor of any class below <code>Propagation</code>. If
   * the propagation task does not invlove evidence, it must be
   * initialized either, but as an empty object of class
   * <code>Evidence</code>:
   */
  public Evidence observations;
  
  /**
   * The network where the propagation will take place.
   * This field is compulsory for performing the propagation, and must
   * be fulfilled whenever a new object of a subclass of
   * <code>Propagation</code> is created.
   */
  public Bnet network;
    
  /**
   * A free comment about the propagation. This field is optional.
   */
  public String propComment;
  
     
   /**
    * A boolean variable indicating if we want to compute the probability of the observations
    * If true, we need to consider more initial relationships.
    */
   public boolean probEvidence=false;
   
  
  /**
   * The name of the problem to be solved by the propagation.
   * This field is optional.
   */
  public String problem;
  
  /**
   * The name of the propagation method. This field is optional.
   */
  public String method;
  
  /**
   * A list of goal variables. For instance, those variables for which
   * the propagation must obtain the marginal distributions. Algorithms
   * that compute marginals for all the variables not observed, do not
   * use this field.
   */
  public NodeList interest;
  
  /**
   * The results of the propagation. This <code>Vector</code> must contain,
   * at the end of the propagation task, one object of class
   * <code>Potential</code> (or one of its subclasses) containing the
   * result of the propagation. It is mainly intended for computing
   * marginal distributions.
   *
   * The position of each variable in this <code>Vector</code> is maintained
   * by the field <code>Hashtable positions</code>.
   */
  public Vector results;
  
  /**
   * The exact results of the propagation, stored as a
   * <code>RelationList</code> and usually read from a file.
   */
  public RelationList exactResults;
  
  /**
   * Some statistics about the propagation task.
   * @see elvira.tools.PropagationStatistics.java
   */
  public PropagationStatistics statistics;
  
  /**
   * Stores the position of the network variables in vector
   * <code>results</code>. This instance variable must be
   * initialized in the constructor of every subclass that uses it.
   */
  public Hashtable positions;
  
  
  /* CONSTRUCTORS */
  
  
  /**
   * Constructs a new propagation. Problem: marginal, method:
   * variable elimination.
   */
  
  public Propagation() {
    
    observations = new Evidence();
    interest = new NodeList();
    results = new Vector();
    setProblem("marginal");
    setMethod("deletion");
    statistics = new PropagationStatistics();
    positions = new Hashtable(20);
  }
  
  
  /**
   * Constructs a new propagation with observations e.
   * Problem: marginal, method:
   * variable elimination.
   * @param e the evidence.
   */
  
  public Propagation(Evidence e) {
    
    observations = e;
    interest = new NodeList();
    results = new Vector();
    setProblem("marginal");
    setMethod("deletion");
    setPropComment("");
    statistics = new PropagationStatistics();
    positions = new Hashtable(20);
  }
  
  
  /**
   * Constructs a new propagation with observations e.
   * Problem: marginal, method: variable elimination.
   * @param e the evidence.
   */
  
  public Propagation(Bnet bnet) {
    
    observations = new Evidence();
    interest = new NodeList();
    results = new Vector();
    setProblem("marginal");
    setMethod("deletion");
    setPropComment("");
    statistics = new PropagationStatistics();
    positions = new Hashtable(20);
    network = bnet;
  }
  
  
  /* METHODS */
  
  /**
   * Program for comparing exact and approximated results.
   * The arguments are as follows.
   * <ol>
   * <li> Input file: the network.
   * <li> Exact result file.
   * <li> Approximated result file.
   * </ol>
   */
  
  public static void main(String args[]) throws  ParseException, IOException {
    
    Propagation p;
    double[] errors;
    double g, mse;
    FileInputStream networkFile;
    Bnet b;
    
    if (args.length < 3) {
      System.out.print("Too few arguments. Arguments are:");
      System.out.println("ElviraFile ExactResultFile ApproxResltFile");
      System.exit(1);
    }
    
    System.out.println("Reading network file");
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);
    System.out.println("Network file read");
    p = new Propagation(b);
    
    System.out.println("Reading exact results");
    p.readExactResults(args[1]);
    System.out.println("Exact results read");
    
    System.out.println("Reading approximate results");
    p.readResults(args[2]);
    System.out.println("Approximate results read");
    
    System.out.println("Computing errors");
    errors = new double[2];
    p.computeError(errors);
    
    g = errors[0];
    mse = errors[1];
    
    System.out.println("G : "+g);
    System.out.println("MSE : "+mse);
    System.out.println("Max absolute error: "+p.computeMaxAbsoluteError());
  }
  
  /**
   * Sets the problem.
   * @param problemName the problem.
   */
  
  public void setProblem(String problemName) {
    
    if (problemName.equals("marginal"))
      problem = new String("marginal");
  }
  
  
  /**
   * Sets the method.
   * @param methodName the method.
   */
  
  public void setMethod(String methodName) {
    
    if (methodName.equals("deletion"))
      method = new String("deletion");
    
    if (methodName.equals("importanceapproximate"))
      method = new String("importanceapproximate");
  }
  
  
  /**
   * Sets the comment.
   * @param c the comment.
   */
  
  public void setPropComment(String c) {
    
    propComment = new String(c);
  }
  
  
  /**
   * Sets the statistics.
   * @param s the statistics.
   */
  
  public void setStatistics(PropagationStatistics s) {
    
    statistics = s;
  }
  
  
  /**
   * Sets the observations.
   * @param evidence the observations
   */
  
  public void setObservations(Evidence evidence) {
    
    observations = evidence;
  }
  
  /**
   * Sets the interest variables.
   * @param vars the interest variables
   */
  
  public void setInterest(NodeList vars) {
    
    interest = vars;
  }
 
  
 
 
  /**
   * Returns all the initial relations present
   * in the network.
   * @return A copy of the list of the Relations of the network
   */
  public RelationList getInitialRelations() {
    Relation r, rNew;
    RelationList ir;
    int i;
    
    ir = new RelationList();
    
    for (i=0 ; i<network.getRelationList().size() ; i++) {
      r = (Relation)((Relation)(network.getRelationList().elementAt(i))).copy();
      rNew = transformInitialRelation(r);
      ir.insertRelation(rNew);
    }
    
    return ir;
  }
 
  
  
   /**
    * Returns the initial relations present
    * in the network that can affect the posterior distribution of a
    * given node.
    * @param node the <code>Node</code> respect to which the relations will
    * be selected.
    * @return A copy of the Relations of the network that can affect the posterior
    * distribution of a given node.
    */
   
   public RelationList getInitialRelations(Node node) {
      
      Relation r, rNew;
      RelationList ir;
      int i,n;
      DSeparation dsep;
      Vector varAffecting,important;
      Node nob;
      
      
       ir = new RelationList();
       
       
      // System.out.println("Important variables");
       
      if (probEvidence) 
          { 
              important = new Vector();
             n = observations.getVariables().size();
             for(i=0; i<n; i++) {
               nob =  (Node)  observations.getVariables().elementAt(i);
               important.addElement(nob);
        //       System.out.println("nodo observado: " + nob.toString());
             }
            if (!important.contains(node)) {
                    important.addElement(node);
          //        System.out.println("nodo de interes: " + node.toString());  
            }
            varAffecting = network.ascendants(important);
           }
      else{
     
      dsep = new DSeparation(network,observations);
      varAffecting = dsep.allAffecting(node);
      }
      // System.out.println("varaibles que afectan " + varAffecting.size());
      for (i=0 ; i<varAffecting.size() ; i++) {
         // System.out.println(((Node)varAffecting.elementAt(i)).toString());
         r = (Relation)network.getRelation((Node)varAffecting.elementAt(i)).copy();
         rNew = transformInitialRelation(r);
         ir.insertRelation(rNew);
      }
      
      return ir;
   }
    
  
  /**
   * Transforms one of the original relations in a Potential. In this
   * case no transformation is carried out. This method can be
   * overloaded for special requirements.
   * @param r the <code>Relation</code> to be transformed.
   * @return The new transformed Relation
   */
  public Relation transformInitialRelation(Relation r) {
    
    return r;
  }
  
  /**
   * Gets the results.
   * @return a <code>Vector</code> with the results of the propagation.
   */
  
  public Vector getResults() {
    
    return results;
  }
  
  
  /**
   * Gets the comment.
   * @return a <code>String</code> with the comment.
   */
  
  public String getPropComment( ) {
    
    return propComment;
  }
  
  
  /**
   * Get the statistics.
   * @return a <code>PropagationStatistics</code> with the statistics
   * about the propagation task.
   */
  
  public PropagationStatistics getStatistics( ) {
    
    return statistics;
  }
  
  
  /**
   * Method not yet commented.
   */
  
  public void delete() {
    
    DSeparation dsep;
    Vector Relevant;
    Vector Rels;
    Dictionary AllGroups;
    Vector Group;
    Vector Sequence;
    
    dsep = new DSeparation(network,observations);
    Relevant = dsep.allAffecting((Node) interest.elementAt(0));
  }
  
  
  /**
   * Reads the exact results from the argument file and put them into
   * the field <code>exactResults</code>.
   * @param fileName the file with the exact results, given as
   * a <code>String</code>.
   * @throws elvira.parser.ParseException 
   * @throws java.io.IOException 
   */
  
  public void readExactResults(String fileName) throws ParseException, IOException {
    
    FileInputStream f;
    ResultParse parser;
    
    f = new FileInputStream(fileName);
    parser = new ResultParse(f);
    
    parser.initialize(network.getNodeList());
    parser.CompilationUnit();
    
    exactResults = parser.Results;
  }
  
  
  /**
   * Reads the approximate results from the argument file and put them into
   * the field <code>results</code>. Also, initializes the field
   * <code>positions</code>.
   * @param fileName the file with the approximate results
   * @throws elvira.parser.ParseException 
   * @throws java.io.IOException 
   */
  
  public void readResults(String fileName) throws ParseException, IOException {
    
    FileInputStream f;
    ResultParse parser;
    RelationList approxResults;
    int i;
    
    f = new FileInputStream(fileName);
    parser = new ResultParse(f);
    
    parser.initialize(network.getNodeList());
    parser.CompilationUnit();
    
    approxResults = parser.Results;
    
    for (i=0 ; i<approxResults.size() ; i++) {
      results.addElement(approxResults.elementAt(i).getValues());
      positions.put((FiniteStates)(((Potential)(results.elementAt(i))).getVariables().elementAt(0)),
      new Integer(i));
    }
  }
  
  
  /**
   * Saves the result of a propagation to a file.
   * @param s a <code>String</code> containing the file name.
   * @throws java.io.IOException 
   */
  
  public void saveResults(String s) throws IOException {
    
    FileWriter f;
    PrintWriter p;
    Potential pot;
    int i;
    
    f = new FileWriter(s);
    
    p = new PrintWriter(f);
    
    for (i=0 ; i<results.size() ; i++) {
      pot = (Potential)results.elementAt(i);
          pot.saveResult(p);
    }
    
    f.close();
  }
 
 /**
   * Method to save the results of propagation into a file,
   * but using a file in Elvira format (a Network)
   * @param name of the file to generate
   * @throws java.io.IOException 
   */
  public void saveResultsAsNetwork(String name) throws IOException{
    Network net=new Network();
    Vector rlist=new Vector();
    Relation rel;
    Potential pot;
    
    net.setNodeList(network.getNodeList());
    for(int i=0;i<results.size();i++){
      pot=(Potential)results.elementAt(i);
      rel=new Relation();
      rel.setVariables(pot.getVariables());
      rel.setValues(pot);
      rlist.addElement(rel);
    }
    net.setRelationList(rlist);
    net.save(name);
  }
 
  public void saveMaxResults(PrintWriter p) throws IOException {
    
    //  FileWriter f;
    // PrintWriter p;
    Potential pot;
    int i;
    
    p.print("[");
    for (i=0 ; i<results.size() ; i++) {
      pot = (Potential)results.elementAt(i);
      ((PotentialTable)pot).saveMaxResult(p);
    }
    p.println("]");
    
  }
  
  
  /**
   * Normalizes the results of a propation.
   */
  
  public void normalizeResults() {
    
    Potential pot;
    int i;
    
    for (i=0 ; i<results.size() ; i++) {
      pot = (Potential)results.elementAt(i);
      pot.normalize();
    }
  }
  
  
  /**
   * If there are not interest variables specified, set all the unobserved
   * variables as interest.
   */
  
  public void obtainInterest( ) {
    
    int i,s;
    Node node;
    if((network.getClass()==Bnet.class)||(network.getClass()==CooperPolicyNetwork.class)){
      if (interest.size() == 0) {
        s = network.getNodeList().size();
        interest = new NodeList();
        
        for (i=0; i<s; i++) {
          node = network.getNodeList().elementAt(i);
          
          if (!observations.isObserved(node)) {
            interest.insertNode(node);
          }
        }
      }
    }
  }
  
  /**
   * Gets the number of variables of interest
   * @return the number of variables of interest
   */
  public int getNumberOfInterest(){
    return interest.size();
  }
  
  /**
   * Inserts a variable of interest for the propagation method
   * @param node the variable of interest to be inserted
   */
  public void insertVarInterest(Node node){
    interest.insertNode(node);
  }
  
  /**
   * Gets one of the variables of interest
   * @param n the number of variable of interest in the NodeList
   * @return the variable of interest number n
   */
  public Node getVarInterest(int n){
    return interest.elementAt(n);
  }
  
  /**
   * Computes the error of an estimation. The exact results must
   * be stored in the instance variable <code>exactResults</code>,
   * while the obtained results are suposed to be stored in the instance
   * variable <code>results</code>.
   * @param errors an array where the errors will be stored. In
   * the first position, the g-error. In the second one, the mean
   * square error
   */
  
  public void computeError(double[] errors) {
    
    int counter, i, j, nv, pos=0;
    double c, cAcum, g, gAcum, v1, v2;
    Relation rel;
    FiniteStates var;
    PotentialTable exactPot, approxPot;
    
    gAcum = 0.0;
    cAcum = 0.0;
    counter = 0;
    
    for (i=0 ; i<exactResults.size() ; i++) {
      rel = exactResults.elementAt(i);
      exactPot = (PotentialTable)rel.getValues();
      var = (FiniteStates)exactPot.getVariables().elementAt(0);
      
      if (observations.isObserved(var))
        continue;
      
      if (positions.get(var) != null)
        pos = ((Integer)positions.get(var)).intValue();
      else {
        System.out.println(var.getName()+" not found in aproximated results file");
        continue;
      }
      
      approxPot = PotentialTable.convertToPotentialTable((Potential)(results.elementAt(pos)));
      
      g = 0.0;
      c = 0.0;
      nv = var.getNumStates();
      
      for (j=0 ; j<nv ; j++) {
        v1 = approxPot.getValue(j);
        v2 = exactPot.getValue(j);
        c += Math.pow(v2-v1,2);
        
        if ((v2==0.0)||(v2==1.0))
          continue;
        
        g += Math.pow(v1-v2,2) / (v2 * (1-v2));
      }
      
      g = Math.sqrt(g/(double)nv);
      c /= (double)nv;
      gAcum += (g * g);
      cAcum += c;
      counter++;
    }
    
    gAcum = Math.sqrt(gAcum); // g-error.
    cAcum /= (double)counter; // mean squared error
    
    errors[0] = gAcum;
    errors[1] = cAcum;
  }
  
  
  /**
   * Computes the maximum absolute error of an estimation.
   * The exact results are
   * suposed to be stored in the instance variable <code>exactResult</code>,
   * while the obtained results are suposed to be stored in the instance
   * variable <code>results</code>.
   */
  
  public double computeMaxAbsoluteError() {
    
    int  i, j, nv, pos=0;
    double v1, v2, error, maxerror;
    Relation rel;
    FiniteStates var;
    PotentialTable exactPot, approxPot;
    
    maxerror = 0.0;
    
    for (i=0 ; i<exactResults.size() ; i++) {
      rel = exactResults.elementAt(i);
      exactPot = (PotentialTable)rel.getValues();
      var = (FiniteStates)exactPot.getVariables().elementAt(0);
      
      if (observations.isObserved(var))
        continue;
      
      if (positions.get(var) != null)
        pos = ((Integer)positions.get(var)).intValue();
      else {
        System.out.println(var.getName()+" not found in aproximated results file");
        continue;
      }
      
      approxPot = PotentialTable.convertToPotentialTable((Potential)(results.elementAt(pos)));
      
      nv = var.getNumStates();
      
      for (j=0 ; j<nv ; j++) {
        v1 = approxPot.getValue(j);
        v2 = exactPot.getValue(j);
        
        error = Math.abs(v1-v2);
        if (error > maxerror)
          maxerror = error;
      }
    }
    return maxerror;
  }
  
  
  /**
   * Computes the error of an estimation using the Kullback-Leibler
   * distance. The exact results must be stored in the instance
   * variable <code>exactResults</code>, while the obtained results
   * are suposed to be stored in the instance
   * variable <code>results</code>.
   * @param errors an array where the error will be stored. In
   * the first position, the average of the errors in all of the
   * variables. In the second one, the standard deviation
   * of the errors.
   */
  
  public void computeKLError(double[] errors) {
    
    int counter, i, j, nv, pos=0;
    double s, t, kl, v1, v2;
    Relation rel;
    FiniteStates var;
    PotentialTable exactPot, approxPot;
    
    s = 0.0;
    t = 0.0;
    counter = 0;
    
    for (i=0 ; i<exactResults.size() ; i++) {
      rel = exactResults.elementAt(i);
      exactPot = (PotentialTable)rel.getValues();
      var = (FiniteStates)exactPot.getVariables().elementAt(0);
      
      if (observations.isObserved(var))
        continue;
      
      if (positions.get(var) != null)
        pos = ((Integer)positions.get(var)).intValue();
      else {
        System.out.println(var.getName()+" not found in aproximated results file");
        continue;
      }
      
      approxPot = PotentialTable.convertToPotentialTable((Potential)(results.elementAt(pos)));
      
      kl = 0.0;
      
      nv = var.getNumStates();
      
      for (j=0 ; j<nv ; j++) {
        v1 = approxPot.getValue(j);
        v2 = exactPot.getValue(j);
        
        if ((v2 == 0.0) || (v1 == 0.0))
          continue;
        
        kl += v2 * Math.log(v2/v1);
      }
      
      s += kl;
      if (counter > 0)
        t += (counter*Math.pow(kl-(s/(double)counter),2))/(double)(counter+1);
      counter++;
    }
    
    s /= (double)counter;
    t = Math.sqrt(t/(double)(counter-1));
    errors[0] = s;
    errors[1] = t;
  }
  
  
  
  /**
   * Computes the error of an estimation using the Hellinger
   * distance. The exact results must be stored in the instance
   * variable <code>exactResults</code>, while the obtained results
   * are suposed to be stored in the instance
   * variable <code>results</code>.
   * @param errors an array where the error will be stored. In
   * the first position, the average of the errors in all of the
   * variables. In the second one, the standard deviation
   * of the errors, and then the median, max and min.
   * Thearray must be created with at least five positions before
   * calling this method.
   */
  
  public void computeHellingerDistance(double[] errors) {
    
    int counter, i, j, k, nv, pos=0, nonObserved = 0;
    double s, t, hel, v1, v2;
    double[] helVector;
    boolean inserted;
    Relation rel;
    FiniteStates var;
    PotentialTable exactPot, approxPot;
    
    s = 0.0;
    t = 0.0;
    counter = 0;
    
    // This array contains the individual errors for each variable
    helVector = new double[exactResults.size()];
    for (i=0 ; i<exactResults.size() ; i++) {
        helVector[i] = 0.0;
    }
   
    for (i=0 ; i<exactResults.size() ; i++) {
      rel = exactResults.elementAt(i);
      exactPot = (PotentialTable)rel.getValues();
      var = (FiniteStates)exactPot.getVariables().elementAt(0);
      
      if (observations.isObserved(var))
        continue;
      else nonObserved++;
      
      if (positions.get(var) != null)
        pos = ((Integer)positions.get(var)).intValue();
      else {
        System.out.println(var.getName()+" not found in aproximate results file");
        continue;
      }
      
      approxPot = PotentialTable.convertToPotentialTable((Potential)(results.elementAt(pos)));
      
      hel = 0.0;
      
      nv = var.getNumStates();
      
      for (j=0 ; j<nv ; j++) {
        v1 = approxPot.getValue(j);
        v2 = exactPot.getValue(j);
        hel += Math.pow(Math.sqrt(v2) - Math.sqrt(v1),2);
      }
      
      s += Math.sqrt(hel);
      if (counter > 0)
        t += (counter*Math.pow(hel-(s/(double)counter),2))/(double)(counter+1);
      counter++;
      
      // Now insert the error for this variable in the array
      inserted = false;
      k = 0;
      while (!inserted) {
          if (helVector[k] >= hel) {
              for (j=nonObserved ; j>k ; j--) {
                  helVector[j] = helVector[j-1];
              }
              helVector[k] = hel;
              inserted = true;
          }
          else k++;
      }
    }
    
    // Average Hellinger distance
    s /= (double)counter;
    // Standard deviation
    t = Math.sqrt(t/(double)(counter-1));
    
    errors[0] = s;
    errors[1] = t;
    
    //Median Hellinger distance
    j = (nonObserved-1) / 2;
    if ((nonObserved % 2) == 0)
        errors[2] = helVector[j];
    else {
        errors[2] = (helVector[j] + helVector[j+1]) / 2;
    }
    
    // Max and min
    errors[3] = helVector[0];
    errors[4] = helVector[nonObserved-1];
  }
  
}  // end of class
