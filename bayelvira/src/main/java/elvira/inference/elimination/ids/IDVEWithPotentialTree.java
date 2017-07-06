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

 RCS $Revision: 1.5 $ $State: Exp $


 _________________________________________________________________________

 Note:

 ________________________________________________________________________ */
package elvira.inference.elimination.ids;

import java.io.*;
import elvira.*;
import elvira.tools.idiagram.EUComparator;
import elvira.parser.ParseException;
import elvira.potential.*;

/**
 * Class
 * <code>VEWithPotentialTree</code>. Implements the variable elimination method
 * of propagation using potentials of class
 * <code>PotentialTree</code>. If the initial potentials are not PotentialTrees
 * then they are converted to PotentialTrees.
 *
 * @since 11/9/2000
 */
public class IDVEWithPotentialTree extends IDVariableElimination {

   /**
    * A very low limit for prunning, allowing almost exact calculations.
    */
   protected static final double limitForPrunning = 0.0001;

   /*
    * Limit to produce the identification of leaves in the tree This value can
    * be changed with setThresholdForPrunning
    */
   protected double thresholdForPrunning = 0.0;
   /**
    * Limit to produce the identification of leaves in the tree This value can
    * be changed with setThresholdForPruningUtility
    */
   private double thresholdForPruningUtility = 0.0;
   /**
    * Data members to store the max and min values for the potentials
    */
   protected double maximum, minimum;
   /**
    * Indicates if transformation for utility trees are only performed at the
    * begining or after each operation
    */
   private boolean onlyInitialTransformation = false;

   /**
    * Method to sed the threshold for prunning operations
    */
   public void setThresholdForPrunning(double value) {
      thresholdForPrunning = value;
   }

   /**
    * Sets the treshold for pruning probability trees
    *
    * @param thresholdForPruning
    */
   public void setThresholdForPruningUtility(double thresholdForPruning) {
      this.thresholdForPruningUtility = thresholdForPruning;
   }

   /**
    * Program for performing experiments from the command line. The command line
    * arguments are as follows: <ul> <li> Input file: the network. <li> Output
    * file. <li> Evidence file. </ul> If the evidence file is omitted, then no
    * evidences are considered.
    */
   public static void main(String args[]) throws ParseException, IOException {

      Network b;
      Evidence e;
      FileInputStream networkFile, evidenceFile;
      IDVEWithPotentialTree ve;
      String base;
      int i;

      if (args.length < 3) {
         System.out.println("Too few arguments. Arguments are: ElviraFile OutputFile thresholdForPrunning EvidenceFile");
         System.exit(-1);
      } else {
         //networkFile = new FileInputStream(args[0]);
         //b = new Bnet(networkFile);
         b = Network.read(args[0]);

         if (args.length == 4) {
            evidenceFile = new FileInputStream(args[3]);
            e = new Evidence(evidenceFile, b.getNodeList());
         } else {
            e = new Evidence();
         }

         ve = new IDVEWithPotentialTree((Bnet) b, e);
         ve.obtainInterest();

         // Compose the name to store the statistics about the evaluation
         base = args[0].substring(0, args[0].lastIndexOf('.'));
         base = base.concat("_VEWithPotentialTree_data");
         ve.statistics.setFileName(base);

         // Set the threshold for prunning
         ve.setThresholdForPrunning((new Double(args[2])).doubleValue());

         // Propagate
         ve.propagate(args[1]);
      }
   }

   /**
    * Constructs a new propagation for a given Bayesian network and some
    * evidence.
    *
    * @param b a
    * <code>Bnet</code>.
    * @param e the evidence.
    */
   public IDVEWithPotentialTree(Bnet b, Evidence e) {
      super(b, e);

      if (generateDebugInfo) {
         System.out.println("IDVEWithPotentialTree:  class Constructor(Bnet,Evidence) ----- BEGIN");
      }

      // Call to initial conditions in order to get measures 
      // about potential sizes, etc
      boolean evaluable = initialConditions();
      if (evaluable == false) {
         System.out.println("Non evaluable diagram......");
         System.exit(0);
      }

      if (generateDebugInfo) {
         System.out.println("IDVEWithPotentialTree:  class Constructor ----- END");
      }
   }

   /**
    * Constructs a new propagation for a given Bayesian network
    *
    * @param b a
    * <code>Bnet</code>.
    */
   public IDVEWithPotentialTree(Bnet b) {
      super(b, null);

      if (generateDebugInfo) {
         System.out.println("IDVEWithPotentialTree:  class Constructor(Bnet) ----- BEGIN");
      }

      // Call to initial conditions in order to get measures 
      // about potential sizes, etc
      boolean evaluable = initialConditions();
      if (evaluable == false) {
         System.out.println("Non evaluable diagram......");
         System.exit(0);
      }

      if (generateDebugInfo) {
         System.out.println("IDVEWithPotentialTree:  class Constructor ----- END");
      }
   }

   /**
    * Transforms one of the original relations into another one whose values are
    * of class
    * <code>PotentialTree</code>. @ param r the
    * <code>Relation</code> to be transformed.
    */
   @Override
   public Relation transformInitialRelation(Relation r) {
      PotentialTree potTree;
      Relation rNew = r;

      if (generateDebugInfo) {
         System.out.println("IDVEWithPotentialTree:  transformInitialRelation ----- BEGIN");
      }

      // Transform the relation, but only for non constraints relations
      if (r.getKind() != Relation.CONSTRAINT) {
         rNew = new Relation();
         rNew.setVariables(r.getVariables().copy());
         rNew.setKind(r.getKind());
         if (r.getValues().getClassName().equals("PotentialTable")) {
            potTree = ((PotentialTable) r.getValues()).toTree();
         } else if (r.getValues().getClassName().equals("CanonicalPotential")) {
            potTree = ((CanonicalPotential) r.getValues()).toTree();
         } else {
            potTree = (PotentialTree) (r.getValues());
         }

         // Now, prune the tree. If it is a utility, sort its
         // variables. Initially only exact prune operations

         if (r.getKind() == Relation.UTILITY) {


            potTree.updateSize();
            // First at all look for minimum and maximum
            setMaximum(potTree.getTree().maximumValue());
            setMinimum(potTree.getTree().minimumValue());
            Potential tinit = new PotentialTable(potTree.copy());
            potTree = potTree.sortUtilityAndPrune(minimum, maximum, thresholdForPruningUtility);

            if (generateStatistics) {
               double error = EUComparator.compareUtilities(tinit, potTree);
               this.statistics.setInitalUtilityError(error);
               this.statistics.setInitialUtilitySize(potTree.getSize());
            }
         } else {
            potTree = potTree.sortAndBound(thresholdForPrunning);
         }

         // Store the final potential

         rNew.setValues(potTree);
      }

      if (generateDebugInfo) {
         System.out.println("IDVEWithPotentialTree:  transformInitialRelation ----- END");
      }

      // Return the new relation
      return rNew;
   }

   /**
    * Transform an utility potential, prunning the lower values if possible
    *
    * @param <code>Potential</code> the potential to transform
    * @param <code>boolean</code> is a utility?
    */
   @Override
   public Potential transformAfterOperation(Potential pot, boolean utility) {
      PotentialTree potTree;
      Potential pResult;

      if (generateDebugInfo) {
         System.out.println("IDVEWithPotentialTree:  transformAfterOperation ----- BEGIN");
      }

      if (onlyInitialTransformation) {
         pResult=pot;
      } 
      else {
         // Try to prune, joining identical values
         if (pot.getClassName().equals("PotentialTable")) {
            potTree = ((PotentialTable) pot).toTree();
         } else {
            potTree = (PotentialTree) pot;
         }

         // Prune the tree. If it is a utility tree, sort its variables
         // Now prune operation is done using thresholdForPrunning

         if (utility) {
            potTree.updateSize();
            potTree = potTree.sortUtilityAndPrune(minimum, maximum, thresholdForPruningUtility);
         } else {
            potTree = potTree.sortAndBound(thresholdForPrunning);
         }
         
         // Anyway, pResult will point to potTree
         pResult=potTree;
      }
      
      if (generateDebugInfo) {
         System.out.println("IDVEWithPotentialTree:  transformAfterOperation ----- END");
      }

      // Return the modified pot
      return pResult;
   }

   public boolean isOnlyInitialTransformation() {
      return onlyInitialTransformation;
   }

   public void setOnlyInitialTransformation(boolean onlyInitialTransformation) {
      this.onlyInitialTransformation = onlyInitialTransformation;
   }
}
