/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elvira.inference.clustering.lazyid.test;

import elvira.tools.idiagram.pairtable.IDPairTable;
import elvira.IDiagram;
import elvira.Network;
import elvira.Bnet;
import elvira.Evidence;
import elvira.inference.clustering.lazyid.LazyPropagationIDWithPT;
import elvira.inference.clustering.lazyid.StrongJunctionTree;
import elvira.parser.ParseException;

import java.io.*;

/**
 *
 * @author mgomez
 */
public class LazyWTIDEvaluator implements IDEvaluator {

   /**
    * Data member for storing the name of the net
    */
   private String netName;
   /**
    * Data member for the ID to evaluate
    */
   private IDiagram diagram;
   /**
    * Data member for storing the results
    */
   private ExperimentResult result;
   /**
    * Data member to activate/deactivate debug information
    */
   private boolean generateDebugInfo = false;

   /**
    * Constructor for the class
    *
    * @param netName
    */
   public LazyWTIDEvaluator(String netName) {
      if (generateDebugInfo) {
         System.out.println("LazyWTIDEvaluator:  class Constructor ----- BEGIN");
      }

      this.netName = netName;
      try {
         diagram = (IDiagram) Network.read(netName);
      } catch (ParseException e) {
         System.out.println("Problema de parseo con red");
         System.exit(0);
      } catch (IOException e) {
         System.out.println("Problema de E/S");
         System.exit(0);
      };

      // Get information about the diagram
      diagram.getInformation();

      // Make the result object
      result = new ExperimentResult(netName, EnumAlgorithms.LAZYWT);
      
      if (generateDebugInfo) {
         System.out.println("LazyWTIDEvaluator:  class Constructor ----- END");
      }
   }

   /**
    * Method for running the tests
    */
   public void run() {
      if (generateDebugInfo) {
         System.out.println("LazyWTIDEvaluator:  run ----- BEGIN");
      }
      
      // Remove constraint relations
      diagram.removeConstraintRelations();

      // Creates the evaluator
      LazyPropagationIDWithPT evaluator = new LazyPropagationIDWithPT(diagram, 0, IDPairTable.WEIGHTFILL,
              StrongJunctionTree.DIRECT_ELIMINATION,
              StrongJunctionTree.OFFLINE_TRIANGULATION,
              generateDebugInfo, true);

      // Set the initial data about the diagram
      result.setInitialData(diagram);

      // Set the initial potential size
      result.setInitialPotentialSize(diagram.calculateSizeOfPotentials());

      // Set to 0 the number of constraints
      result.setConstrainedConfigurations(0);

      // Evaluate the model
      evaluator.propagate();

      // At the end, set the final data about the evaluation
      result.setFinalData(evaluator.statistics);

      // And finally save the result into a file
      result.saveObject();
      
      if (generateDebugInfo) {
         System.out.println("LazyWTIDEvaluator:  run ----- END");
      }
   }

   /**
    * Method for getting the results
    */
   public ExperimentResult getResults() {
      // return result
      return result;
   }
}
