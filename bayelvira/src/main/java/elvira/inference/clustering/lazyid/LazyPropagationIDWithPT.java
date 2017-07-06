package elvira.inference.clustering.lazyid;

import elvira.IDiagram;
import elvira.Network;
import elvira.Relation;
import elvira.potential.PotentialTable;
import elvira.potential.PotentialTree;
import elvira.parser.ParseException;
import java.io.IOException;

public class LazyPropagationIDWithPT extends LazyPropagationID {

   /**
    * Values for storing max and min values for utility
    */
   protected double maximum;
   protected double minimum;
   /**
    * Value for storing the threshold forprunning
    */
   protected double thresholdForPrunning;

   /**
    * Class constructor: it receives the ID to solve
    *
    * @param diag
    * @param threshold
    * @param triangulationCriteria used for triangulation
    * @param propagationCriteria used for removing variables: with triangulation
    * order or with message passing schema
    * @param eliminationCriteria used for removing variables in the cliques:
    * offline-online
    * @param debug flag
    * @param statistics flag
    */
   public LazyPropagationIDWithPT(IDiagram diag, double threshold, int triangulationCriteria, int propagationCriteria,
           int eliminationCriteria, boolean debugFlag, boolean statisticsFlag) {
      super(diag, triangulationCriteria, propagationCriteria, eliminationCriteria, debugFlag, statisticsFlag);

      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  class Constructor ----- BEGIN");
      }

      // Store the threshold for prunning
      thresholdForPrunning = threshold;

      // Set method name
      setMethod("LayPropagationIDWithPT");
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  class Constructor ----- END");
      }
   }

   /**
    * Private method for building a tree node
    *
    * @param diag influence diagram
    */
   private void buildStrongJunctionTree() {
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  buildStringJunctionTree ----- BEGIN");
      }
      
      tree = new StrongJunctionTreeWithPT((IDiagram) network, thresholdForPrunning, triangulationCriteria, propagationCriteria,
              variableEliminationCriteria, generateDebugInfo, generateStatistics);

      // If it is needed, set the reference to the statistics objetc
      // for the tree
      if (generateStatistics) {
         tree.setStatistics(statistics);
      }
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  buildStringJunctionTree ----- END");
      }      
   }

   /**
    * Public method for making the propagation @Override propagate method of
    * super clase
    */
   public void propagate() {
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  propagate ----- BEGIN");
      }
            
      // Make the strong junction tree
      buildStrongJunctionTree();

      // Set the properties for the evaluation
      ((StrongJunctionTreeWithPT) tree).setMinimum(minimum);
      ((StrongJunctionTreeWithPT) tree).setMaximum(maximum);
      ((StrongJunctionTreeWithPT) tree).propagate();
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  propagate ----- BEGIN");
      }
   }

   /**
    * Method to set the value for maximum
    *
    * @param value to set
    */
   public void setMaximum(double value) {
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  setMaximum ----- BEGIN");
      }
      
      if (value > maximum) {
         maximum = value;
      }
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  setMaximum ----- END");
      }
   }

   /**
    * Method to set the value for minimum
    *
    * @param value to set
    */
   public void setMinimum(double value) {
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  setMinimum ----- BEGIN");
      }
      
      if (value < minimum) {
         minimum = value;
      }
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  setMinimum ----- END");
      }
   }

   /**
    * Transforms one of the original relations into another one whose values are
    * of class
    * <code>PotentialTree</code>, and adding the effect of the possible
    * constraints. This is done for normal relations, but not for constraints
    * relations @ param r the
    * <code>Relation</code> to be transformed. @Override method in Propagation
    * class
    */
   public Relation transformInitialRelation(Relation r) {
      PotentialTree potTree;
      Relation rNew;
      double minimumValue;
      double maximumValue;

      if (generateDebugInfo) {
         System.out.println("LazyPropagationIDWithPT:  transformInitialRelation ----- BEGIN");
      }
      
      if (r.getKind() != Relation.CONSTRAINT) {
         // We do the conversion
         rNew = new Relation();
         rNew.setVariables(r.getVariables().copy());
         rNew.setKind(r.getKind());
         if (r.getValues().getClassName().equals("PotentialTable")) {
            potTree = ((PotentialTable) r.getValues()).toTree();
         } else {
            potTree = (PotentialTree) (r.getValues());
         }

         // Store the new potential
         rNew.setValues(potTree);

         // Now, check the possible presence of constraints on this relation
         if (this.getClass().getName().equals("elvira.inference.clustering.lazyid.LazyPropagationIDWithPTAC")) {
            ((IDiagram) network).applyConstraintsOnRelation(rNew);
            potTree = (PotentialTree) rNew.getValues();
         }

         // Prune operation, initially exact
         if (r.getKind() == Relation.UTILITY) {
            // First at all look for minimum and maximum
            minimumValue = potTree.getTree().minimumValue();
            maximumValue = potTree.getTree().maximumValue();
            setMinimum(minimumValue);
            setMaximum(maximumValue);
            potTree = potTree.sortUtilityAndPrune(minimumValue, maximumValue, 0L);

            //potTree = potTree.sortUtilityAndPruneWithoutRestrict(minimumValue, maximumValue, 0L, PotentialTree.DISTANCE_WITH_CEROS);
         } else {
            potTree = potTree.sortAndBound(thresholdForPrunning);
            //potTree = potTree.sortAndBoundWithoutRestrict(thresholdForPrunning);
         }

         // Store the new potential
         rNew.setValues(potTree);

         if (generateDebugInfo) {
            System.out.println("LazyPropagationIDWithPT:  transformInitialRelation ----- END");
         }

         // Return the new relation
         return rNew;
      } 
      else {
         if (generateDebugInfo) {
            System.out.println("LazyPropagationIDWithPT:  transformInitialRelation ----- END");
         }
         // It is a constraint and there is nothing to do with it
         return r;
      }
   }

   public static void main(String[] args) throws ParseException, IOException {
      String networkName = null;
      boolean debug = false;
      boolean statistics = false;
      int triangulationCriteria = -1;
      int propagationCriteria = -1;
      int eliminationCriteria = -1;
      double threshold = 0.0;
      int i;

      // Show the arguments used when calling the program
      for (i = 0; i < args.length; i++) {
         System.out.print(args[i] + " ");
      }
      System.out.println();

      // Check the arguments passed to the program
      if (args.length < 2) {
         // Show how the program must be called
         usage();
      } else {
         // Consider the arguments one by one
         i = 0;
         while (i < args.length) {
            if (args[i].equals("-net")) {
               networkName = args[i + 1];
               i = i + 2;
            } else {
               if (args[i].equals("-g")) {
                  debug = true;
                  i++;
               } else {
                  if (args[i].equals("-s")) {
                     statistics = true;
                     i++;
                  } else {
                     if (args[i].equals("-t")) {
                        triangulationCriteria = Integer.parseInt(args[i + 1]);
                        i = i + 2;
                     } else {
                        if (args[i].equals("-e")) {
                           eliminationCriteria = Integer.parseInt(args[i + 1]);
                           i = i + 2;
                        } else {
                           if (args[i].equals("-p")) {
                              propagationCriteria = Integer.parseInt(args[i + 1]);
                              i = i + 2;
                           } else {
                              if (args[i].equals("-tp")) {
                                 threshold = Double.parseDouble(args[i + 1]);
                                 i = i + 2;
                              } else {
                                 usage();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      // If everything is ok makes the evaluator
      Network net = Network.read(networkName);
      LazyPropagationIDWithPT evaluator = new LazyPropagationIDWithPT((IDiagram) net, threshold, triangulationCriteria, propagationCriteria, eliminationCriteria, debug, statistics);

      // Set the name for the statistics file
      String base = networkName.substring(0, networkName.lastIndexOf('.'));
      base = base.concat("_LazyPropagationIDWithPT_data");
      evaluator.statistics.setFileName(base);

      // Make the propagation
      evaluator.propagate();

      // Now check if all the decision tables are computed
      evaluator.checkDecisionTables();

      // Print the results
      // evaluator.tree.printResults();
   }

   /**
    * Method to show how the program must be called
    */
   private static void usage() {
      System.out.println("Use: LazyPropagationIDWithPT -net iDiagramFile");
      System.out.println("       [-g] (generate debug information) ");
      System.out.println("       [-s] (generate statistics)");
      System.out.println("       [-t] (triangulation criteria)");
      System.out.println("       -tp (threshold for prunning)");
      System.out.println("       [-p] (propagation criteria: direct elimination (1), message passing (2))");
      System.out.println("       [-e] (elimination criteria: online triangulation (2), offline triangulation (1))");
      System.exit(0);
   }
}
