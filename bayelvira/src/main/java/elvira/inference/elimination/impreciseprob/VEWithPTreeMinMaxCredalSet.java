/*
 * VEWithPTreeMinMaxCredalSet.java
 *
 * Created on 26 de abril de 2004, 14:48
 */

package elvira.inference.elimination.impreciseprob;

import java.io.*;
import java.util.*;
import elvira.*;
import elvira.potential.*;
import elvira.inference.elimination.VariableElimination;
import elvira.parser.ParseException;


/** 
 * This class implements the variable elimination method of propagation
 * for intervals. It is a modification of VEWithPTreeCredalSet in order
 * to make approximate propagation using prunning in probability trees. Prunning
 * is made for initial relations and after eliminating a variable.
 * Initial potentials can be of class PotentialTable or PTreeCredalSet. Those
 * potentials are converted into PTreeCredalSets to make operations. Resulting
 * potentials are converted into PotentialIntervalTable
 *
 * @author  Andrés Cano Utrera (acu@decsai.ugr.es), Manuel Gómez (mgomez@decssai.ugr.es)
 */
public class VEWithPTreeMinMaxCredalSet extends VEWithPTreeCredalSet {
  /**
   * The paramater to use when a probability tree is pruned
   */
  double limitForPruning=0.0;
  
  /** Creates a new instance of VEWithPTreesCredalSet */
  public VEWithPTreeMinMaxCredalSet(Bnet b, Evidence e) {
    super(b,e);
  }
  
  /** Creates a new instance of VEWithPTreesCredalSet */ 
  public VEWithPTreeMinMaxCredalSet(Bnet b) {
    super(b);
  }
  
  /**
   * Set the field limiForPruning
   * @param sigma the new value for the field limitForPruning
   */
  public void setLimitForPruning(double sigma){
    limitForPruning=sigma;
  }

  /**
   * Method to propagate for a target variable. The results of the
   * propagation will be stored in a PotentialIntervalTable
   * @param var target of the propagation
   * @param evidence used for the propagation
   */
  public PotentialIntervalTable propagate(FiniteStates var, Evidence evidence){
    setObservations(evidence); // Set the evidence  
    //getPosteriorDistributionOf(var); 
    
    insertVarInterest(var); // The variable var will be fixed as the interest variable    
    propagate();  // Now, make the propagation itself

    // The results data member will contain the desired potential
    // in results data member
    PotentialIntervalTable result=(PotentialIntervalTable)results.elementAt(0);

    // return the result
    return result;
  }

  
  
 /**
  * Transforms the Potential of one of the original relations. If the Potential
  * is a PotentialInterval then it is transformed into a PTreeCredalSet. If
  * the Potential is a PTreeCredalSet then it is not modified. Otherwise an
  * error is produced.
  * @ param r the <code>Relation</code> to be transformed.
  */
  public elvira.Relation transformInitialRelation(elvira.Relation r) {
    if(r.getValues().getClass()==PTreeCredalSet.class){
      PTreeMinMaxCredalSet pTree=new PTreeMinMaxCredalSet((PTreeCredalSet)r.getValues());
      Relation rNew=new Relation();
      rNew.setVariables(r.getVariables().copy());
      rNew.setKind(r.getKind());
      rNew.setValues(pTree);
      return rNew;
    }
    else if(r.getValues() instanceof PotentialTable){//to convert PotentialTable and PotentialConvexSet
      PTreeMinMaxCredalSet pTree=new PTreeMinMaxCredalSet((PotentialTable)r.getValues());
      Relation rNew=new Relation();
      rNew.setVariables(r.getVariables().copy());
      rNew.setKind(r.getKind());
      rNew.setValues(pTree);
      return rNew;     
    }
    else if(r.getValues() instanceof PotentialInterval){
      PTreeCredalSet pTreeCS=new PTreeCredalSet((PotentialInterval)r.getValues());
      PTreeMinMaxCredalSet pTree=new PTreeMinMaxCredalSet(pTreeCS);
      Relation rNew=new Relation();
      rNew.setVariables(r.getVariables().copy());
      rNew.setKind(r.getKind());
      rNew.setValues(pTree);
      return rNew;
    }
    else{
      System.out.print("Error in VEWithPTreeMinMaxCredalSet.transformInitialRelation(Relation r): ");
      System.out.println("Potentials of "+r.getValues().getClassName()+ " class cannot be propagated with this class");
      System.exit(1);
    }
    return null;
  }
  
 /**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows:
 * <ol>
 * <li> Input file: the network.
 * <li> -evi <evidenceFile.evi> --> The evidence file
 * <li> -out <resultsFile.out> --> The file with the results (if this option is not included the results will be stored in tmp.out
 * <li> -interest <varName> --> Name of the variable of interest. If no -interest option is used then all non-observed variables are included
 * <li> -sigma <limitForPruning> --> A double value used to prune probability trees in inner nodes of the search tree (default 0.0")
 * </ol>
 * If the evidence file is omitted, then no evidences are
 * considered.
 */

  public static void main(String args[]) throws ParseException, IOException {
    
    Network b;
    Evidence e=new Evidence();
    VEWithPTreeMinMaxCredalSet ve;
    String resultsFile="tmp.out";
    double sigma=0.0;
    Vector varsInterest=new Vector();
    
    if (args.length < 1){
      System.out.println("ERROR: Too few arguments.");
      System.out.println("Use: bnet.elv [Options]");
      System.out.println("OPTIONS: ");
      System.out.println("-evi <evidenceFile.evi> --> The evidence file");
      System.out.println("-out <resultsFile.out> --> The file with the results (if this option is not included the results will be stored in tmp.out");
      System.out.println("-interest <varName> --> Name of the variable of interest. If no -interest option is used then all non-observed variables are included");
      System.out.println("-sigma <limitForPruning> --> A double value used to prune probability trees in inner nodes of the search tree (default 0.0");
      System.exit(0);
    }

    // Read the network to evaluate
    b=Network.read(args[0]);
      
    // Deal with the arguments
    for(int i=1; i < args.length; i++){
      if (args[i].equals("-evi")){
        // Create the evidence object
        e=new Evidence(args[i+1],b.getNodeList());
        i++;
      }
      else if (args[i].equals("-out")){
         resultsFile=args[i+1];
         i++;
      }
      else if (args[i].equals("-interest")){
        varsInterest.add(args[i+1]);
         i++;
      } 
      else if (args[i].equals("-sigma")){
        sigma=Double.valueOf(args[i+1]);
        i++;
      }
    }
   
    // Create the objet from VEWithPTreeMinMaxCredalSet 
    ve = new VEWithPTreeMinMaxCredalSet((Bnet)b,e);
    ve.setLimitForPruning(sigma);

    // Deal with variables of interest
    for(int i=0; i < varsInterest.size(); i++){
      ve.insertVarInterest(b.getNode((String)varsInterest.elementAt(i)));
    }

    // Propagate
    ve.obtainInterest();
    // If we wish to store the results in a file, call propagate with
    // an argument: the name of the file
    ve.propagate(resultsFile);
    //ve.propagate();
  }
  
  /**
   * Transforms a <code>PotentialTree</code> obtained as a result of
   * eliminating one variable (<code>FiniteStates</code>).
   * @param pot the <code>PotentialTree</code>.
   */
  public Potential transformAfterEliminating(Potential potential) {
    PTreeMinMaxCredalSet pot=(PTreeMinMaxCredalSet)potential;
    pot.limitBound(limitForPruning);
    return pot;
  }
  
  /**
   * Transforms a <code>PotentialTree</code> obtained as a result of
   * eliminating one variable (<code>FiniteStates</code>).
   * @param pot the <code>PotentialTree</code>.
   */
  public Potential transformAfterAdding(Potential potential) {
    PTreeMinMaxCredalSet pot=(PTreeMinMaxCredalSet)potential;
    pot.limitBound(limitForPruning);
    return pot;
  }
  
  /**
   * Normalizes the results of the propation. Each result is saved as a PotentialIntervalTable
   */
  
  public void normalizeResults() {
    PTreeMinMaxCredalSet pot;
    PotentialInterval result;
    int i;
    
    for (i=0 ; i<results.size() ; i++) {
      pot = (PTreeMinMaxCredalSet)results.elementAt(i);
      // Normalize the potential and takes the final result as a
      // PotentialIntervalTable
      result=pot.normalizeWithMinMax();
      results.setElementAt(result,i);
    }
  }

  /**
   * Saves the result of a propagation to a file.
   * @param s a <code>String</code> containing the file name.
   */
  
  public void saveResults(String s) throws IOException {
    FileWriter f;
    PrintWriter p;
    PotentialInterval pot;
    int i;
    
    f = new FileWriter(s);
    p = new PrintWriter(f);
   
    for (i=0 ; i<results.size() ; i++) {
      pot = (PotentialInterval)results.elementAt(i);
      pot.saveResult(p);
    }
    
    f.close();
  }
}
