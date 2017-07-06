/*
 */
package elvira.inference.elimination;

import java.io.*;
import elvira.*;
import elvira.potential.Potential;
import elvira.potential.binaryprobabilitytree.*;
import elvira.parser.ParseException;
import elvira.tools.CmdLineArguments;
import elvira.tools.CmdLineArguments.CmdLineArgumentsException;
import elvira.tools.CmdLineArguments.argumentType;

/**
 * Implements de variable elimination method for Bayesian Networks, 
 * converting all the potential to binary probability tree (class 
 * PotentialBPTree)
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 */
public class VEWithPotentialBPTree extends VariableElimination {   
   private static enum pruningMethods {NO_PRUNING, KULLBACK_LEIBLER_DISTANCE};
   private pruningMethods pruningMethod = pruningMethods.NO_PRUNING;
   private double thresholdForPruning = 0.0;
   private boolean sortVariablesInTrees = false;

   /**
    * Set the pruning method
    * @param prunMethod the pruning method: (0: NO_PRUNING; 1: KULLBACK_LEIBLER_DISTANCE)
    */
   public void setPruningMethod(int prunMethod, double threshold) {
      thresholdForPruning = threshold;
      if (prunMethod==0) {
         pruningMethod = pruningMethods.NO_PRUNING;
      } else if (prunMethod ==1){
         pruningMethod = pruningMethods.KULLBACK_LEIBLER_DISTANCE;
      }
   }
   
   /**
    * Set the sorting of variables in the trees obtained for initial relations
    * @param sort true if we want to sort the variables, false otherwise
    */
   public void setSortVariablesInTrees(boolean sort){
      sortVariablesInTrees = sort;
   }
   /**
    * Constructs a new propagation for a given Bayesian network and some
    * evidence.
    *
    * @param b a <code>Bnet</code>.
    * @param e the evidence.
    */
   public VEWithPotentialBPTree(Bnet b, Evidence e) {
      super(b, e);
   }

   /**
    * Transforms the Potential of one of the original Relations in the network, 
    * to a Relation with a Potential of the class PotentialBPTree
    * @param r the <code>Relation</code> to be transformed.
    * @return The new transformed Relation
    */
   @Override
   public Relation transformInitialRelation(Relation r) {
      Relation rNew;
      PotentialBPTree potTree;

      rNew = new Relation();
      rNew.setVariables(r.getVariables().copy());
      rNew.setKind(r.getKind());

      potTree = new PotentialBPTree(r.getValues());
      if(sortVariablesInTrees){
         potTree.sort();
      }
      
      if (pruningMethod == pruningMethods.KULLBACK_LEIBLER_DISTANCE){
         potTree.limitBound(thresholdForPruning);        
      }
      rNew.setValues(potTree);

      return rNew; // Return the new relation
   }

   /**
    * Transforms a <code>PotentialTree</code> obtained as a result of
    * eliminating one variable (<code>FiniteStates</code>).
    * @param pot the <code>PotentialTree</code>.
    */
    @Override
   public Potential transformAfterAdding(Potential potential) {
     int k, pos;
     FiniteStates y;
     PotentialBPTree pot=null;
     pot = (PotentialBPTree) potential;

      if(sortVariablesInTrees){
System.out.println("Se ordenan las variables en el potencial......");        
         pot.sort();
      }

      if (pruningMethod == pruningMethods.KULLBACK_LEIBLER_DISTANCE) {
         pot.limitBound(thresholdForPruning);

//         System.out.println("Potencial tras prune()");
//         pot.print();
      }

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
      potential = pot;
      return potential;
   }

   /**
    * Method for computing the number of nodes of the trees
    */
   public long getNumberOfNodes() {
     long sum=0;
     Relation relation;
     PotentialBPTree values;

     for(int i=0; i < currentRelations.size(); i++){
        relation=currentRelations.elementAt(i);
        values=(PotentialBPTree)relation.getValues();
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
     PotentialBPTree values;

     for(int i=0; i < currentRelations.size(); i++){
        relation=currentRelations.elementAt(i);
        values=(PotentialBPTree)relation.getValues();
        sum=sum+values.getNumberOfLeaves();
     }

     // At the end return sum
     return sum;
   }

   private static final String argBnetFile = "-bnetFile";
   private static final String argOutputFile = "-outputFile";
   private static final String argEvidenceFile = "-evidenceFile";
   private static final String argPruneAfterEliminating = "-pruneAfterEliminating";
   private static final String argThresholdForPruning = "-thresholdForPruning";
   private static final String argSortVariablesInTrees = "-sortVariables";

   public static void main(String args[]) throws ParseException, IOException {
      Network b;
      Evidence e;
      FileInputStream evidenceFile;
      VEWithPotentialBPTree ve;
      String bnetFileString = null;
      String outputFileString = null;
      String evidenceFileString = null;
      int pruneAfterEliminatingInteger = 0;
      double thresholdForPruningDouble = 0.0;
      boolean sortVariablesInTreesBoolean = false;


      CmdLineArguments params = new CmdLineArguments();
      try {
         params.addArgument(argBnetFile, argumentType.s, "", "The filename of the Bnet (.elv format). No default value, must be provided.");
         params.addArgument(argOutputFile, argumentType.s, "", "The filename for the output results. No default value, must be provided.");
         params.addArgument(argEvidenceFile, argumentType.s, "", "The filename of the Evidence (.evi format). No default value, it is optional.");
         params.addArgument(argPruneAfterEliminating, argumentType.i, "0", "Kind of pruning method after eliminating a variable. Default value is 0 (NOPRUNING)." +
                 "Possible values: 0 (NOPRUNING), 1 (KullbackLeiblerDistance)");
         params.addArgument(argThresholdForPruning, argumentType.d, "0.0", "The threshold if we use a pruning method for the binary probability trees). Default value is 0.0. It is optional.");
         params.addArgument(argSortVariablesInTrees, argumentType.b, "false", "Control if we sort the variables in the initial relations. Possible values: true or false");
         
         params.parseArguments(args);
         params.print();
         bnetFileString = params.getString(argBnetFile);
         outputFileString = params.getString(argOutputFile);
         evidenceFileString = params.getString(argEvidenceFile);
         pruneAfterEliminatingInteger = params.getInteger(argPruneAfterEliminating);
         thresholdForPruningDouble = params.getDouble(argThresholdForPruning);
         sortVariablesInTreesBoolean = params.getBoolean(argSortVariablesInTrees);
      } catch (CmdLineArgumentsException ex) {
         params.printHelp();
         System.exit(1);
      }
      if (bnetFileString.equalsIgnoreCase("")) {
         System.out.println(argBnetFile + " argument not found, you must specify one!!!");
         params.printHelp();
         System.exit(1);
      }
      if (outputFileString.equalsIgnoreCase("")) {
         System.out.println(argOutputFile + " argument not found, you must specify one!!!");
         params.printHelp();
         System.exit(1);
      }

      b = Network.read(bnetFileString);
      if (evidenceFileString.equalsIgnoreCase("")) {
         e = new Evidence();
      } else {
         evidenceFile = new FileInputStream(evidenceFileString);
         e = new Evidence(evidenceFile, b.getNodeList());
      }
      ve = new VEWithPotentialBPTree((Bnet) b, e);

      if (pruneAfterEliminatingInteger>0) {
         ve.setPruningMethod(pruneAfterEliminatingInteger,thresholdForPruningDouble);
      }
      
      if (sortVariablesInTreesBoolean){
         ve.setSortVariablesInTrees(true);
      }

      ve.obtainInterest();
      System.out.println("PROPAGATING ...");
      ve.propagate(outputFileString);  // Propagate
   }
}
