/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elvira.inference.clustering.lazyid.test;

import elvira.IDiagram;
import elvira.Network;
import elvira.Bnet;
import elvira.Evidence;
import elvira.inference.elimination.ids.IDVEWithPotentialTree;
import elvira.parser.ParseException;

import java.io.*;

/**
 *
 * @author mgomez
 */
public class VEWTIDEvaluator implements IDEvaluator {

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
   public VEWTIDEvaluator(String netName) {
      if (generateDebugInfo) {
         System.out.println("VEWTIDEvaluator:  class Constructor ----- BEGIN");
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

      // Make the result object
      result = new ExperimentResult(netName, EnumAlgorithms.VEWT);
      
      if (generateDebugInfo) {
         System.out.println("VEWTIDEvaluator:  class Constructor ----- END");
      }
   }

   /**
    * Method for running the tests
    */
   public void run() {
      if (generateDebugInfo) {
         System.out.println("VEWTIDEvaluator:  run ----- BEGIN");
      }
      
      // Remove constraint relations
      diagram.removeConstraintRelations();

      // Set the initial data about the diagram
      result.setInitialData(diagram);

      // Set to 0 the number of constraints
      result.setConstrainedConfigurations(0);

      // Creates the evaluator
      IDVEWithPotentialTree evaluator = new IDVEWithPotentialTree((Bnet) diagram, new Evidence());
      evaluator.generateDebugInfo=generateDebugInfo;

      // Evaluate the model
      evaluator.propagate();

      // At the end, set the final data about the evaluation
      result.setFinalData(evaluator.statistics);

      // And finally save the result into a file
      result.saveObject();
      
      if (generateDebugInfo) {
         System.out.println("VEWTIDEvaluator:  run ----- END");
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
