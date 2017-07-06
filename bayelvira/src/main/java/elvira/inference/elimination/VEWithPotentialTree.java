/* _________________________________________________________________________

			      VEWithPotentialTree

			     Elvira Project

   File: VEWithPotentialTree.java
   Path: /home/gte/acu/Desarrollo/bayelvira/VEWithPotentialTree.java
   Description:
   Created: Mon Oct  4 18:40:55 CEST 1999
   Author: Andres Cano,,244258,,
   Modified: Mon Oct  4 19:28:48 CEST 1999
   Last maintained by: Andres Cano,,244258,,

   RCS $Revision: 1.18 $ $State: Exp $


   _________________________________________________________________________

   Note:

   ________________________________________________________________________ */


package elvira.inference.elimination;

import java.io.*;
import elvira.*;
import elvira.parser.ParseException;
import elvira.potential.*;

/**
 * Class <code>VEWithPotentialTree</code>.
 * Implements the variable elimination method of propagation using
 * potentials of class <code>PotentialTree</code>. If the initial potentials
 * are not PotentialTrees then they are converted to PotentialTrees.
 *
 * @since 11/9/2000
 */

public class VEWithPotentialTree extends VariableElimination {

/**
 * A very low limit for prunning, allowing almost exact calculations.
 */
private static final double limitForPrunning = 0.0001;

/*
 * Limit to produce the identification of leaves in the tree
 * This value can be changed with setThresholdForPrunning
 */

private double thresholdForPrunning;

/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows:
 * <ul>
 * <li> Input file: the network.
 * <li> Output file.
 * <li> Evidence file.
 * </ul>
 * If the evidence file is omitted, then no evidences are
 * considered.
 */

public static void main(String args[]) throws ParseException, IOException {

  Network b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  VEWithPotentialTree ve;
  String base;
  int i;

  if (args.length < 3){
    System.out.println("Too few arguments. Arguments are: ElviraFile OutputFile thresholdForPrunning EvidenceFile");
	 System.exit(-1);
  }
  else {
    //networkFile = new FileInputStream(args[0]);
    //b = new Bnet(networkFile);
    b=Network.read(args[0]);

    if (args.length == 4) {
      evidenceFile = new FileInputStream(args[3]);
      e = new Evidence(evidenceFile,b.getNodeList());
    }
    else
      e = new Evidence();

    ve = new VEWithPotentialTree((Bnet)b,e);
    ve.obtainInterest();

	 // Compose the name to store the statistics about the evaluation

	 base=args[0].substring(0,args[0].lastIndexOf('.'));
	 base=base.concat("_VEWithPotentialTree_data");
	 ve.statistics.setFileName(base);

	 // Set the threshold for prunning

	 ve.setThresholdForPrunning((new Double(args[2])).doubleValue());

	 // Propagate

    ve.propagate(args[1]);
  }
}

/**
 * Method to sed the threshold for prunning operations
 */

public void setThresholdForPrunning(double value){
  thresholdForPrunning=value;
}

/**
 * Constructs a new propagation for a given Bayesian network and some
 * evidence.
 *
 * @param b a <code>Bnet</code>.
 * @param e the evidence.
 */

public VEWithPotentialTree(Bnet b, Evidence e) {

  super(b,e);
}

/**
 * Constructs a new propagation for a given Bayesian network
 *
 * @param b a <code>Bnet</code>.
 */

public VEWithPotentialTree(Bnet b) {

  super(b);
}


/**
 * Transforms one of the original relations into another one whose values
 * are of class <code>PotentialTree</code>.
 * @ param r the <code>Relation</code> to be transformed.
 */

public Relation transformInitialRelation(Relation r) {
  PotentialTree potTree;
  Relation rNew;
  double minimum,maximum;

  // Transform the relation, but only for non constraints relations

  if (r.getKind() != Relation.CONSTRAINT){
    rNew = new Relation();
    rNew.setVariables(r.getVariables().copy());
    rNew.setKind(r.getKind());
    if (r.getValues().getClassName().equals("PotentialTable")) {
      potTree=((PotentialTable)r.getValues()).toTree();
    }
    else if (r.getValues().getClassName().equals("CanonicalPotential")) {
      potTree=((CanonicalPotential)r.getValues()).toTree();
    }
    else {
      potTree=(PotentialTree)(r.getValues());
    }

    // Now, prune the tree
    potTree=potTree.sortAndBound(thresholdForPrunning);

    // Store the final potential
    rNew.setValues(potTree);

    // Return the new relation
    return rNew;
  }
  else{
    // For constraints, do not change it
    return r;
  }
}

/**
 * Transforms a <code>PotentialTree</code> obtained as a result of adding
 * over one variable (<code>FiniteStates</code>).
 * @param pot is the <code>PotentialTree</code>.
 */

public Potential transformAfterAdding(Potential potential) {

  PotentialTree pot;
  int k, pos;
  FiniteStates y;

  pot = (PotentialTree)potential;

  pot.limitBound(thresholdForPrunning);
  //pot = (PotentialTree) pot.sortAndBound(10000);

  for (k=pot.getVariables().size()-1 ; k>=0 ; k--) {
    y = (FiniteStates)pot.getVariables().elementAt(k);
    if (y.getKindOfNode() == Node.CHANCE) {
      if (!pot.getTree().isIn(y)) {
        if (currentRelations.isIn(y)) {
	        pos = pot.getVariables().indexOf(y);
	        pot.getVariables().removeElementAt(pos);
        }
      }
    }
  }

  return pot;
}


/**
 * Transforms a <code>PotentialTree</code> obtained as a result of
 * eliminating one variable (<code>FiniteStates</code>).
 * @param pot the <code>PotentialTree</code>.
 */

//public Potential transformAfterEliminating(Potential potential) {

//  PotentialTree pot;
//  pot = (PotentialTree)potential;
  //pot=pot.sortAndBound(thresholdForPrunning);
//  pot.limitBound(thresholdForPrunning);
//  return pot;
//}

/**
 * Transform an utility potential, prunning the lower
 * values if possible
 * @param <code>Potential</code> the potential to transform
 * @param <code>boolean</code> is a utility?
 */

public Potential transformAfterOperation(Potential pot, boolean utility){
  PotentialTree potTree;

  // Try to prune, joining identical values

  if (pot.getClassName().equals("PotentialTable"))
	   potTree=((PotentialTable)pot).toTree();
	 else
     potTree=(PotentialTree)pot;

	// Prune the tree
    potTree=potTree.sortAndBound(thresholdForPrunning);

	// Return the modified pot

  return potTree;
}

/**
 * Method for computing the number of nodes of the trees
 */
public long getNumberOfNodes() {
  long sum=0;
  Relation relation;
  PotentialTree values;

  for(int i=0; i < currentRelations.size(); i++){
     relation=currentRelations.elementAt(i);
     values=(PotentialTree)relation.getValues();
     sum=sum+values.getNumberOfNodes();
  }

  // At the end return sum
  return sum;
}

/**
 * Method for computing the number of nodes of the trees
 */
public long getNumberOfLeaves() {
  long sum=0;
  Relation relation;
  PotentialTree values;

  for(int i=0; i < currentRelations.size(); i++){
     relation=currentRelations.elementAt(i);
     values=(PotentialTree)relation.getValues();
     sum=sum+values.getNumberOfLeaves();
  }

  // At the end return sum
  return sum;
}

}
