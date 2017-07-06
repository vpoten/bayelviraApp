package elvira.inference.clustering.lazyid;

import elvira.RelationList;
import elvira.IDiagram;
import elvira.Bnet;
import elvira.Network;
import elvira.Node;
import elvira.NodeList;
import elvira.tools.idiagram.pairtable.IDPairTable;
import elvira.Relation;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;
import elvira.parser.ParseException;
import elvira.inference.Propagation;
import java.io.IOException;
import java.util.Vector;

public class LazyPropagationID extends Propagation {

   /**
    * Constants to indicate the possible states of evaluable
    */
   private enum EvaluableStates {

      NOCHECKED, EVALUABLE, NOEVALUABLE
   };
   /**
    * Data member to indicate if the diagram is evaluable: it is prepared for
    * evaluation. Initially this value is NOCHECKED and its value is determined
    * after a call to initialConditions
    */
   private EvaluableStates evaluable;
   /**
    * The relations related to the IDiagram. This is needed to convert the
    * relation potential to trees if they are not in this form
    */
   protected RelationList currentRelations;
   /**
    * The propagation will be done using a strong junction tree
    */
   protected StrongJunctionTree tree;
   /**
    * Data member to store the criteria used for generation the deletion
    * sequence when triangulating
    */
   protected int triangulationCriteria = IDPairTable.WEIGHTFILL;
   /**
    * Criteria used for removing variables, with online or offline triangulation
    */
   protected int variableEliminationCriteria = StrongJunctionTree.OFFLINE_TRIANGULATION;
   /**
    * Criteria used for propagation: using the triangulation order directly or
    * using the message passing schema
    */
   protected int propagationCriteria = StrongJunctionTree.DIRECT_ELIMINATION;
   /**
    * Data member to store if the evaluation will generate statistics
    */
   protected boolean generateStatistics = false;
   /**
    * Data member to store if the evaluation will generate debug information
    */
   protected boolean generateDebugInfo = true;

   /**
    * Class constructor: it receives the ID to solve
    *
    * @param diag
    * @param triangulationCriteria used for triangulation
    * @param propagationCriteria used for removing variables: with triangulation
    * order or with message passing schema
    * @param eliminationCriteria used for removing variables in the cliques:
    * offline-online
    * @param debug flag
    * @param statistics flag
    * @param makeTree flag showing if the tree must be built
    */
   public LazyPropagationID(IDiagram diag, int triangulationCriteria, int propagationCriteria,
           int eliminationCriteria, boolean debug, boolean statistics) {

      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  class Constructor ----- BEGIN");
      }

      // Sets evaluable to NOCHECKED
      evaluable = EvaluableStates.NOCHECKED;

      // Create results vector
      results = new Vector();

      // Set method name
      setMethod("LayPropagationID");

      // Print the network in Hugin format
      //try {
      //diag.saveWithHuginFormat();
      //System.exit(0);
      //} catch (IOException e) {
      //}
      //;

      if (triangulationCriteria != -1) {
         this.triangulationCriteria = triangulationCriteria;
      }

      // Assign the value for the propagation criteria
      if (propagationCriteria != -1) {
         this.propagationCriteria = propagationCriteria;
      }

      // Assign the value for removing criteria
      if (eliminationCriteria != -1) {
         this.variableEliminationCriteria = eliminationCriteria;
      }

      // Set the flags for debug and statistics
      generateDebugInfo = debug;
      generateStatistics = statistics;

      // Fix the network
      network = (Bnet) diag;

      // Remove constraint relations if required
      if (getClass().getName().equals("elvira.inference.clustering.lazyid.LazyPropagationIDWithPTAC") == false) {
         diag.removeConstraintRelations();
      }

      // Now, deal with the IDiagram to avoid redundant links,
      // and to prepare it for evaluation. All this is done
      // in method initialConditions
      boolean state = initialConditions();

      if (state == false) {
         evaluable = EvaluableStates.NOEVALUABLE;
         System.out.println("The IDiagram is not evaluable");
         System.exit(0);
      } else {
         evaluable = EvaluableStates.EVALUABLE;
      }
      
      if (generateDebugInfo){
         System.out.println("FIN LazyPropagationID:  class Constructor ----- END");
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
      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  transformInitialRelation ----- BEGIN");
         System.out.println("LazyPropagationID:  transformInitialRelation ----- END");
      }      
      // It is a constraint and there is nothing to do with it
      return r;
   }

   /**
    * Private method for building a tree node
    */
   private void buildStrongJunctionTree() {
      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  buildStronJunctionTree ----- BEGIN");
      }
      
      tree = new StrongJunctionTree((IDiagram) network, triangulationCriteria, propagationCriteria,
              variableEliminationCriteria, generateDebugInfo, generateStatistics);

      // If it is needed, set the reference to the statistics objetc
      // for the tree
      if (generateStatistics) {
         tree.setStatistics(statistics);
      }
      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  buildStronJunctionTree ----- END");
      }
   }

   /**
    * Method for getting the current relations
    *
    * @return currentRelations
    */
   public RelationList getCurrentRelations() {
if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  getCurrentRelations ----- BEGIN");
         System.out.println("LazyPropagationID:  getCurrentRelations ----- END");
      }      
      return currentRelations;
   }

   /**
    * Public method for making the propagation
    */
   public void propagate() {
      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  propagate ----- BEGIN");
      }
      
      // Make the strong junction tree
      buildStrongJunctionTree();

      // Call the proper method for propagation
      tree.propagate();
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  propagate ----- END");
      }
   }

   /**
    * Checks if an influence diagram has the properties required for being
    * evaluated
    *
    * @return true or false
    */
   public boolean initialConditions() {
      boolean state = false;
      String errorMessage = null;
      IDiagram diag = (IDiagram) network;

      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  initialConditions ----- BEGIN");
      }
      
      // Makes the checks if avaluable is false
      if (evaluable == EvaluableStates.NOCHECKED) {

         // Check if all the links are directed
         state = diag.directedLinks();

         // Error if evaluable is false and return false
         if (state == false) {
            errorMessage = "Influence Diagram with no directed links\n\n";
         } else {
            // Check the presence of cycles
            state = diag.hasCycles();
            if (state == true) {
               errorMessage = "Influence Diagram with cycles\n\n";
            } else {
               // Add non forgetting arcs
               diag.addNonForgettingArcs();

               // Check if there is a path linking all the decisions
               state = diag.pathBetweenDecisions();
               if (state == false) {
                  errorMessage = "Influence Diagram with non ordered decisions\n\n";
               } else {
                  // Transform the set of initial relations
                  currentRelations = getInitialRelations();
                  diag.setRelationList(currentRelations.getRelations());
               }
            }
         }
      }

      // Prints the error message if needed
      if (!state) {
         evaluable = EvaluableStates.NOEVALUABLE;
         System.out.println(errorMessage);
      } else {
         evaluable = EvaluableStates.EVALUABLE;
      }
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  initialConditions ----- END");
      }

      // Return true
      return state;
   }

   /**
    * Method to check if all the decision tables are computed
    */
   public void checkDecisionTables() {
      NodeList decisions = tree.diag.getDecisionList();
      JunctionTreeNode clique;
      Potential table;
      Node decision;

      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  checkDecisionTables ----- BEGIN");
      }
      
      // Consider the decisions one by one
      for (int i = decisions.size() - 1; i >= 0; i--) {
         decision = decisions.elementAt(i);

         //Check if the table for this decision is computed
         table = tree.results.get(decision);

         if (table == null) {
            if (generateDebugInfo) {
               System.out.println("No computada tabla para " + decision.getName());
            }

            // Get the clique where to compute the policy for this variable
            clique = tree.getNearestClique(decision);

            // Now, remove the decision variable
            clique.computeDecisionTable(decision);
         }
      }

      // At the end, force the generation of the file with the
      // statistics data, if needed
      if (generateStatistics) {
         tree.printFinalStatisticsData();
      }
      
      if (generateDebugInfo) {
         System.out.println("LazyPropagationID:  checkDecisionTables ----- END");
      }
   }

   public static void main(String[] args) throws ParseException, IOException {
      String networkName = null;
      boolean debug = false;
      boolean statistics = false;
      int triangulationCriteria = -1;
      int propagationCriteria = -1;
      int eliminationCriteria = -1;
      int i;

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
                              usage();
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
      LazyPropagationID evaluator = new LazyPropagationID((IDiagram) net, triangulationCriteria,
              propagationCriteria, eliminationCriteria, debug, statistics);

      // Set the name for the statistics file
      String base = networkName.substring(0, networkName.lastIndexOf('.'));
      base = base.concat("_LazyPropagationID_data");
      evaluator.statistics.setFileName(base);

      // Make the propagation
      evaluator.propagate();

      // Now check if all the decision tables are computed
      evaluator.checkDecisionTables();

      // Print the results
      //evaluator.tree.printResults();
   }

   /**
    * Method to show how the program must be called
    */
   private static void usage() {
      System.out.println("Use: LazyPropagationID -net iDiagramFile");
      System.out.println("       [-g] (generate debug information) ");
      System.out.println("       [-s] (generate statistics)");
      System.out.println("       [-t] (triangulation criteria)");
      System.out.println("       [-p] (propagation criteria: direct elimination (1), message passing (2))");
      System.out.println("       [-e] (elimination criteria: online triangulation (2), offline triangulation (1))");
      System.exit(0);
   }
}
