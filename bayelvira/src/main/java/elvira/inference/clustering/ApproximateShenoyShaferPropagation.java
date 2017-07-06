/**
 * ApproximateShaferShenoyPropagation.java
 *
 * This class implements Shenoy-Shafer probability propagation by
 * using a binary join tree as graphical structure. The propagation
 * is approximate by using limitBound(delta) after each combinatino
 * between two probabillityTrees. 
 * 
 * Created on November, 5 of 2004
 */

package elvira.inference.clustering;


import elvira.*;
import elvira.potential.*;
import java.io.IOException;
import elvira.parser.ParseException;
import java.util.Hashtable;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 *
 * @author  julia@info-ab.uclm.es, jgamez@info-ab.uclm.es
 * @version 0.1
 */
public class ApproximateShenoyShaferPropagation extends ShenoyShaferPropagation {

    /**
     * The threshold for prunning 
     */  
    double limitForPrunning = 10e-30;
   

/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * <li> Output file.
 * <li> Threshold for approximation
 * <li> Evidence file.
 * <li> REstrict to observations (yes|no)
 * <li> Interest file.
 *
 * </ol>
 * If the evidence file is omitted, then no evidences are
 * considered
 */

public static void main(String args[]) throws ParseException, IOException {
  FileInputStream networkFile;
  Bnet b;
  ApproximateShenoyShaferPropagation ssp;
  Evidence e;
  NodeList interest = new NodeList();
  FileInputStream evidenceFile, interestFile;
  boolean query;
  double limit;
                      
  if (args.length < 4){
    System.out.println("Too few arguments, the arguments are:");
    System.out.println("\tNetwork OutputFile limitForPrunning query(yes|no) [evidenceFile] [interestFile]");
  }
  else{
    System.out.println("Loading network....");
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);
    System.out.println("....Network loaded.\n");
    limit = Double.valueOf(args[2]).doubleValue();

    if (args[3].equals("yes")) query=true;
    else query=false;        

    if (args.length == 6) {
      evidenceFile = new FileInputStream(args[4]);
      e = new Evidence(evidenceFile,b.getNodeList());
      
      interestFile = new FileInputStream(args[5]);
      interest = new NodeList(interestFile,b.getNodeList());
    }
    else if (args.length == 5) {
      // trying if args[4] is the evidence file
      try {
        evidenceFile= new FileInputStream(args[4]);
        e = new Evidence(evidenceFile,b.getNodeList());
        interest = new NodeList();
      }catch (ParseException pe){
        interestFile = new FileInputStream(args[4]);
        interest = new NodeList(interestFile,b.getNodeList());
        e = new Evidence();
      }
    }
    else { 
      e = new Evidence();
      interest = new NodeList();
    }
  
    ssp = new ApproximateShenoyShaferPropagation(b,e,query,interest,limit);
    ssp.introduceEvidence(e);
    System.out.print("\nEvidence:" );
    e.pPrint();
    double prEv = ssp.iterativePropagation(ssp.getJoinTree().elementAt(0), true);
    //double prEv = ssp.propagate(ssp.getJoinTree().elementAt(0), true);
    System.out.println("\nThe evidence probability is: " + prEv);
    ssp.saveResults(args[1]);
  }
                     
}//end main

 
/** 
 * Constructor: Creates new ApproximateShenoyShaferPropagation 
 *
 * @param b the <code>Bnet</code> over which propagation will be carried out
 * @param e the <code>Evidence</code> entered 
 * @param q a <code>boolean</code> indicating if the object to be created
 *        is query oriented (just one propagation) or not (possibly several
 *        propagations with different evidences).
 * @param vars a <code>NodeList</code> containing the interest variables. If
 *        empty, all the variables are consideres of interest.
 * @param delta a <code>double</code> indicating the threshold to be
 *        considered during the approximation process
 */

  public ApproximateShenoyShaferPropagation(Bnet b,Evidence e, boolean q, NodeList vars,
						double delta) {
     super(b,e,q,vars);
     limitForPrunning = delta;
  }//end of constructor
 


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

  s = binTree.size();
  for (i=0 ; i<s ; i++) {
    r = ((NodeJoinTree)binTree.elementAt(i)).getNodeRelation();
    r = transformRelation(r);
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

  pot = (PotentialTree)r.getValues();
  pot.limitBound(limitForPrunning);
  r.setValues(pot);

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

public PotentialTree transformPotential(PotentialTree pot) {
  ((PotentialTree)pot).limitBound(limitForPrunning);
  return pot;
}

    
    
} // end of class
