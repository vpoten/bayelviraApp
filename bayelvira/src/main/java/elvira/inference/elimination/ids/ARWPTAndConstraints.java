/* ARWPTAndConstraints.java */ 
 
package elvira.inference.elimination.ids; 
 
import java.io.*; 
import java.util.*; 
import elvira.*; 
import elvira.parser.ParseException; 
import elvira.potential.*;
 
/**  
 * Class <code>ARWPTAndConstraints</code>. Implements the solution of 
 * a regular ID (Influence Diagram) with potential trees and constraints.
 * @author Manuel Gomez 
 * @since 09/6/2002
 */ 
 
public class ARWPTAndConstraints extends ARWithPotentialTree {

/**
 * To set the threshold for prunning operations
 */
private double thresholdForPrunning;

/**
 * Default constructor
 * @param diag IDiagram to evaluate
 */

public ARWPTAndConstraints (IDiagram diag) {

  super(diag);
}

/**
 * Method to set the limit for prunning operations
 */

public void setThresholdForPrunning(double value){
  thresholdForPrunning=value;
}

/** 
 * Program for performing experiments from the command line.
 * The command line arguments are as follows. 
 * <ol> 
 * <li> Input file: the network. 
 * <li> Output file. 
 * <li> Evidence file. 
 * </ol> 
 * If the evidence file is omitted, then no evidences are 
 * considered .
 */

public static void main(String args[]) throws ParseException, IOException { 

  ARWPTAndConstraints eval; 
  FileInputStream networkFile; 
  String base;
  int evaluationMode; 
  boolean evaluable;
  IDiagram diag;
  
  diag = (IDiagram)Network.read(args[0]); 
  eval = new ARWPTAndConstraints(diag); 

  // Check the number of arguments

  if (args.length < 3){
    System.out.println("Use: elv_file, results_file, threshold");
    System.exit(-1);
  }
  
  // Set the threshold for prunning operations

  eval.setThresholdForPrunning((new Double(args[2])).doubleValue());

  // initial chekout about the node. 
  
  evaluable = eval.initialConditions(); 
  System.out.print("Evaluable : " + evaluable + "\n\n"); 
 
  // Compose the name of the file to store the statistics

  base=args[0].substring(0,args[0].lastIndexOf('.'));
  base=base.concat("_ARWPTAndConstraints_data");
  eval.statistics.setFileName(base);
 
  // If the diagram is suitable to be evaluated, then do it.
  
  if (evaluable == true) 
  {
    eval.evaluateDiagram(); 
    eval.saveResultsAsNetwork(args[1]);
  }
}

/**
 * Transforms one of the original relations into another one whose values
 * are of class <code>PotentialTree</code>, adding the effect of possible
 * constraints on the relation
 * @param <code>Relation</code> the relation to transform
 */

public Relation transformInitialRelation(Relation r) {    
  LogicalExpression logExp;
  PotentialTree potTree;
  double maximum,minimum;

  if (r.getKind() != Relation.CONSTRAINT){
    // Do the conversion

    if (r.getValues().getClassName().equals("PotentialTable")) {
      potTree=((PotentialTable)r.getValues()).toTree();
      r.setValues(potTree);
    }

    // Now, check the possible presence of constraints on this
    // relation. This also try to prune to join the identical values
   
    ((IDiagram)network).applyConstraintsOnRelation(r);

    // Retrieve the new potential to prune it

    potTree=(PotentialTree)r.getValues();

    // Prune operation, initially exact 
  
    if(r.getKind() == Relation.UTILITY){

      // First at all look for minimum and maximum

      minimum=potTree.getTree().minimumValue();
      maximum=potTree.getTree().maximumValue();
      setMinimum(minimum);
      setMaximum(maximum);
      potTree=potTree.sortUtilityAndPrune(minimum,maximum,0L);
    }
    else{
      potTree=potTree.sortAndBound(thresholdForPrunning);
    }

		// Store the new potential
		r.setValues(potTree);
  }
  else{
    // If the constraint is not evaluated, do it

    logExp=(LogicalExpression)(r.getValues());
    potTree=logExp.getResult();
    if(potTree == null){
      logExp.evaluate(); 
    }
  }

  // Return r

  return r;
}

/**
 * To tranform a potential after an operation on it
 * @param <code>Potential</code> potential to transform
 * @param <code>boolean</code> flag to show if the potential is
 *                             an utility
 * @return <code>Potential</code> the modified potential
 */

public Potential transformAfterOperation(Potential pot,boolean utility){
  PotentialTree potFinal,potResult;
  int k, pos;
  FiniteStates y;

  if (!pot.getClassName().equals("PotentialTree"))
    potFinal=((PotentialTable)pot).toTree();
   else
    potFinal = (PotentialTree)pot;
 
	// First at all examine the set of relations contained in currentRelations.
  // Remove the constraint relations related to variables already deleted
  removeConstraintRelations();
 
  // Apply constraints, if needed. This also try to prune to join the 
  // identical values
  potResult=((IDiagram)network).applyConstraintsOnPotential(potFinal,utility);

  // Prune operation 
  
  if(utility){
    potResult=potResult.sortUtilityAndPrune(getMinimum(),getMaximum(),0L);
  }
  else{
    // Here, the distributions are marginals or conditioned, and so
    // the normalization must be done on the leaf nodes
    potResult=potResult.sortAndBound(thresholdForPrunning);
  }

  // Return potResult
  
  return potResult;
}

/**
 * Method to compute the proportion a certain quantity of utility
 * means respect to the global range of this function
 * @param the expected utility to express as percentage
 */

public double computeEUProportion(double val){
  return((val*100)/(getMaximum()-getMinimum()));
}

/**
 * Examine constraint relations trying to remove that constraints relating
 * variables already removed
 */
private void removeConstraintRelations(){
   Relation rel;
   Relation relNonConst;
   NodeList varsInConstraint,varsInRel,intersection;
   boolean emptyIntersection=true;
	 Vector relations=((Network)diag).getRelationList();
	 RelationList currentRelations=new RelationList();
	 currentRelations.setRelations(relations);

   for(int i=0; i < currentRelations.size(); i++){
      rel=currentRelations.elementAt(i);

      // Considere the relation if it is a contraint relation
      if (rel.getKind() == Relation.CONSTRAINT){
         // Get the set of variables for this relation
         varsInConstraint=rel.getVariables();

         // Check if this set of variables has non empty intersection
         // with the other relations (not constraint relations)
         for(int j=0; j < currentRelations.size(); j++){
            if (j != i){
               // Consider only non constraint relations
               relNonConst=currentRelations.elementAt(j);

               // Analyze the variables if it is not a constraint
               // relation
               if (relNonConst.getKind() != Relation.CONSTRAINT){
                  varsInRel=relNonConst.getVariables();

                  // Check if there is a non empty intersection between
                  // both sets of variables
                  intersection=varsInConstraint.intersection(varsInRel);
                  emptyIntersection=(intersection.size() == 0);

                  // If the result is false, the constraint will not be deleted
                  if (emptyIntersection == false)
                     break;
               }
            }
         }

         // If emptyIntersection is true, the relation must be deleted
         if (emptyIntersection == true){
            currentRelations.removeRelationAt(i);

            // Decrement one to i so that the next relation be considered
            i--;
         }
      }
   }
}

} // End of class
