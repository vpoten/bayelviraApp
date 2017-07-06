
/**
 * Class IDResultsComparator. The objective of this class is to allow
 * the comparison between two evaluations of an influence diagram. One
 * of the evaluations will be considered as the reference one.
 * Author: Manuel G�mez
 * Since: Octubre 2004
 */

package elvira.tools.idiagram;

import elvira.*;
import elvira.potential.*;

import java.util.*;

import elvira.inference.Propagation;
import elvira.inference.elimination.ids.IDVariableElimination;
import elvira.inference.elimination.ids.IDVEWPTAndConstraints;
import elvira.parser.ParseException;
import elvira.tools.VectorManipulator;

import java.io.*;


public class IDResultsComparator{

 /**
  * Constants used to show if the objective is to work with the
  * reference results or with the results from toCompare
  */
  final static int REFERENCE=1;
  final static int TOCOMPARE=2;
  public final static int GLOBAL=3;
  public final static int OPTIMAL=4;
  
 /**
  * String containing the name of the file to analyze
  */
  public String idName;
  
 /**
  * Data member to store the reference solution. Against it will be
  * done all the comparisons
  */
  private Network reference;

 /**
  * Data member to store the solution to compare
  */
  private Propagation toCompare;
  
 /**
  * Vector to store the set of trees containing the optimal policies
  * for every decision
  */
  private Vector referenceOptimalPolicies;
  private Vector propagationOptimalPolicies;

 /**
  * Vector to store the number of matches for the policies according to
  * the optimal policies alone. Another vector will be used to store the
  * number of branches relates to optimal policies for every decision.
  * Obviously, the set of constrained configurations will not be used
  * for comparisons purposes
  */
  private Vector optimalPoliciesMatches;
  private Vector branchesWithReferenceOptimalPolicies;
  private Vector branchesWithPropagationOptimalPolicies;

 /**
  * Vector to store the number of matches for the whole set of policies.
  * Another counter is kept to count the number of unconstrained policies.
  * The constrained configurations will not be considered for comparison
  * purposes
  */
  private Vector globalPoliciesMatches;
  private Vector branchesWithReferenceGlobalPolicies;
  private Vector branchesWithPropagationGlobalPolicies;

 /**
  * Vector with the whole set of policies, even if they are not optimal
  */
  private Vector referenceGlobalPolicies;
  private Vector propagationGlobalPolicies;
  
 /**
  * Data member to consider a threshold used for matching policies. A difference
  * of utility under the given threshold will be discarded
  */
  private double threshold;

 /**
  * Vectors to store the differences of expected utility between solutions
  */
  private Vector EUDifferences;

 /**
  * <p>
  * Constructor. This receives as argument the influence diagram with
  * the reference results 
  * </p>
  * @param reference. The name of a file with and ID to analyze. The name
  * will be specified withoux extension
  */
  public IDResultsComparator(String file){
    String resultsName=file+".res";
    idName=file+".elv";
    
    // Read the results of the evaluation. This results will be used as a
    // reference solution for an influence diagram
    try{
      reference = Network.read(resultsName);
    }catch(Exception e){
      System.out.println("Problem when reading file with results: "+resultsName);
      System.out.println(e);
      e.printStackTrace();
    }
  }
  
 /**
  * Method to set the value for threshold data member
  * @param threshold value
  */
  
  public void setThreshold(double value) {
    threshold=value;
  }

 /**
  * <p>
  * This method assigns the idiagram results to compare with the
  * results stored in reference
  * </p>
  * @param toCompare. Propagation with the results to compare
  */
  public void setResultsToCompare(Propagation toCompare){
    this.toCompare=toCompare;
  }
  
 /**
  * Method to get the vector with the referenceOptimalPolicies
  * @return Vector with the optimal policies
  */
  public Vector getReferenceOptimalPolicies() {
    return referenceOptimalPolicies;
  }
  
 /**
  * Method to get the vector with the toCompareOptimalPolicies
  * @return Vector with the optimal policies
  */
  public Vector propagationOptimalPolicies(){
  	return propagationOptimalPolicies;
  }
  
 /**
  * Method to get the network containing the results used as a
  * reference for the comparison
  * @return network containing the results
  */
  public Network getReference(){
  	return reference;
  }
  
 /**
  * Method returning the value of the threshold
  * @return threshold
  */
  public double getThreshold(){
  	return threshold;
  }
  
 /**
  * Method to get the vector with the counters for the branches
  * with optimal policies for the reference solution
  * @return vector with the counters
  */
  public Vector getBranchesWithReferenceOptimalPolicies(){
  	return branchesWithReferenceOptimalPolicies;
  }
  
 /**
  * Method to get the vector with the counter for the branches
  * with optimal policies for the toCompare solution
  * @return vector with the counter
  */
  public Vector getBrancesWithPropagationOptimalPolicies(){
  	return branchesWithPropagationOptimalPolicies;
  }

 /**
  * Method to get the matches respect to optimal policies
  * @return vector with optimal policies matches
  */
  public Vector getOptimalPoliciesMatches(){
    return optimalPoliciesMatches;
  }

 /**
  * Method to get the matches respect to global policies
  * @return vector with global policies matches
  */
  public Vector getGlobalPoliciesMatches(){
    return globalPoliciesMatches;
  }

 /**
  * Method to get the difference in expected utility for the set
  * of global policies
  * @return vector with expected utility differences
  */
  public Vector getEUDifferences(){
    return EUDifferences;
  }
 
 /**
	* Method to get the policies for the reference solution. This will
	* be done once the reference solution is obtained from the file. The
	* number of branches with the policies will be obtained too. The
	* method compute the policies for optimal and global policies
	*/
  public void getReferencePolicies(){
		Double branches;
		int i;

		// Get the potential containing the reference solution for the ID
		Vector potentials=getPotentialsFromReferenceRelations();
		
		// Get the potential for the first decision
    Potential firstDecisionPotential=(Potential)potentials.elementAt(potentials.size()-1);

		// Initialize the vector to store the policy. In this case this policy
		// will be the same both for global and optimal solutions
    referenceOptimalPolicies=new Vector();
		referenceOptimalPolicies.setSize(potentials.size());
		referenceGlobalPolicies=new Vector();
		referenceGlobalPolicies.setSize(potentials.size());

		// The same for the vector with the global policies
		branchesWithReferenceOptimalPolicies=new Vector();
		branchesWithReferenceOptimalPolicies.setSize(potentials.size());
		branchesWithReferenceGlobalPolicies=new Vector();
		branchesWithReferenceGlobalPolicies.setSize(potentials.size());
		
		// Compute the policy for this decision
  	Vector solution=computePolicyForFirstDecision(firstDecisionPotential);

		// Get the number of branches related to the policy
		branches=(Double)solution.elementAt(0);

		// Get the tree with the policies
		PotentialTree policy=(PotentialTree)solution.elementAt(1);

		// Store the policy
    referenceOptimalPolicies.setElementAt(policy,potentials.size()-1);
		referenceGlobalPolicies.setElementAt(policy,potentials.size()-1);
		branchesWithReferenceOptimalPolicies.setElementAt(branches,potentials.size()-1);
		branchesWithReferenceGlobalPolicies.setElementAt(branches,potentials.size()-1);
		// Now get the policies for the rest of decisions
    for(i=potentials.size()-2; i >= 0; i--) {
       // Compute the policy for every decision with past
       Potential decisionPotential=(Potential)potentials.elementAt(i);

			 // Soultion vector will contain: number of branchres related to
			 // optimal policy, tree with optimal policy, branches related
			 // to global policies and tree with global policies
       solution=computePolicyForDecisionWithPast(decisionPotential,i);

			 // Get the results of the computation and store it in the proper
			 // vector
			 branches=(Double)solution.elementAt(0);
			 branchesWithReferenceOptimalPolicies.setElementAt(branches,i);
			 PotentialTree tree=(PotentialTree)solution.elementAt(1);
			 referenceOptimalPolicies.setElementAt(tree,i);
			 branches=(Double)solution.elementAt(2);
			 branchesWithReferenceGlobalPolicies.setElementAt(branches,i);
			 tree=(PotentialTree)solution.elementAt(3);
			 referenceGlobalPolicies.setElementAt(tree,i);
    }
	}

 /**
	* Method to get the policies for the propagation solution. This will
	* be done once the propagation is done. The
	* number of branches with the policies will be obtained too. The
	* method compute the policies for optimal and global policies
	*/
  public void getPropagationPolicies(){
		int i;
    Double branches;
		
		// Get the potential containing the reference solution for the ID
		Vector potentials=toCompare.getResults();
		
		// Get the potential for the first decision
    Potential firstDecisionPotential=(Potential)potentials.elementAt(potentials.size()-1);

		// Initialize the vector to store the policy. In this case this policy
		// will be the same both for global and optimal solutions
    propagationOptimalPolicies=new Vector();
		propagationOptimalPolicies.setSize(potentials.size());
		propagationGlobalPolicies=new Vector(potentials.size());
		propagationGlobalPolicies.setSize(potentials.size());

		// The same for the vector with the global policies
		branchesWithPropagationOptimalPolicies=new Vector();
		branchesWithPropagationOptimalPolicies.setSize(potentials.size());
		branchesWithPropagationGlobalPolicies=new Vector();
		branchesWithPropagationGlobalPolicies.setSize(potentials.size());
		
		// Compute the policy for this decision
  	Vector solution=computePolicyForFirstDecision(firstDecisionPotential);

		// Get the number of branches related to the policy
		branches=(Double)solution.elementAt(0);

		// Get the tree with the policies
		PotentialTree policy=(PotentialTree)solution.elementAt(1);

		// Store the policy
    propagationOptimalPolicies.setElementAt(policy,potentials.size()-1);
		propagationGlobalPolicies.setElementAt(policy,potentials.size()-1);
		branchesWithPropagationOptimalPolicies.setElementAt(branches,potentials.size()-1);
		branchesWithPropagationGlobalPolicies.setElementAt(branches,potentials.size()-1);
		// Now get the policies for the rest of decisions
    for(i=potentials.size()-2; i >= 0; i--) {
       // Compute the policy for every decision with past
       Potential decisionPotential=(Potential)potentials.elementAt(i);

			 // Solution vector will contain: number of branchres related to
			 // optimal policy, tree with optimal policy, branches related
			 // to global policies and tree with global policies
       solution=computePolicyForDecisionWithPast(decisionPotential,i);

			 // Get the results of the computation and store it in the proper
			 // vector
			 branches=(Double)solution.elementAt(0);
			 branchesWithPropagationOptimalPolicies.setElementAt(branches,i);
			 PotentialTree tree=(PotentialTree)solution.elementAt(1);
			 propagationOptimalPolicies.setElementAt(tree,i);
			 branches=(Double)solution.elementAt(2);
			 branchesWithPropagationGlobalPolicies.setElementAt(branches,i);
			 tree=(PotentialTree)solution.elementAt(3);
			 propagationGlobalPolicies.setElementAt(tree,i);
    }
	}

 /**
  * This method compares two solutions for the same influence diagram, looking
  * for the number of matches in the set of policies (not only optimal ones)
	* The comparison will be done for optimal and global policies
  */
  public void comparePolicies() {
    Double diff;
    double matches;
		Vector result;
		
		// Initialize the vectors storing the results of the comparison
    optimalPoliciesMatches=new Vector();
		optimalPoliciesMatches.setSize(referenceOptimalPolicies.size());
		globalPoliciesMatches=new Vector();
		globalPoliciesMatches.setSize(referenceOptimalPolicies.size());
		EUDifferences=new Vector();
		EUDifferences.setSize(referenceOptimalPolicies.size());
		
		// Compare the optimal policies. The optimal policies are
		// compared for all decisions but the last one. For the
		// last one the comparison will be obtained from the comparison
		// of global policies
		for(int i=referenceOptimalPolicies.size()-2; i >= 0; i--){
		  result=compareOptimalPoliciesForDecision(i);  

			// Get the results: number of matches and expected utility difference
			matches=((Double)result.elementAt(0)).doubleValue();

			// Compute the percentage of matches, retrieving before the number
			// of branches related to optimal policies
			double branches=((Double)branchesWithReferenceOptimalPolicies.elementAt(i)).doubleValue();
			double percentage=(matches*100)/branches;

			// Store the results for bot global and optimal policies. For reference
			// solution there is no computation for expecte utility differences.
			optimalPoliciesMatches.setElementAt(new Double(percentage),i);
		}

		// Compare global policies. It is requiered to analyze the
		// first decision table, just because the results were
		// not obtained from reference policies comparison and it will
		// be obtained now
		for(int i=referenceGlobalPolicies.size()-1; i >= 0; i--){
		  result=compareGlobalPoliciesForDecision(i);  

			// Get the results: number of matches and expected utility difference
			matches=((Double)result.elementAt(0)).doubleValue();
			diff=(Double)result.elementAt(1);

			// Compute the percentage of matches, retrieving before the number
			// of branches related to global policies
			double branches=((Double)branchesWithReferenceGlobalPolicies.elementAt(i)).doubleValue();
			double percentage=(matches*100)/branches;

			// For the first decision, the comparison of global and optimal
			// policies is the same (both policies are the same). So store
			// the results for bot global and optimal policies
			if (i == referenceGlobalPolicies.size()-1){
				optimalPoliciesMatches.setElementAt(new Double(percentage),i);
			}
			globalPoliciesMatches.setElementAt(new Double(percentage),i);
			EUDifferences.setElementAt(diff,i);
		}
  }
  
 /**
  * This method compares two decision tables looking for the number
  * of matches between compare and reference solution for optimal
	* policies
  *@param flag, showing the interest about optimal or global policies
  *@param index of the decision tables to compare
  *@return vector containing
	*   a) number of matches
	*   b) expected utility difference 
  */
  public Vector compareOptimalPoliciesForDecision(int index){  	
		Vector results;
  	boolean allCeros;
    long unconstrained=0;
    double matches=0, difference=0;
    long i;
    int indMax;
		Vector objectiveRef=null,objectiveProp=null;
//System.out.println("compareOptimalPoliciesForDecision: "+index);
//System.out.println("---------------------------------------------");

    // Get the potential trees with the policies to compare
  	PotentialTree reference=(PotentialTree)referenceOptimalPolicies.elementAt(index);
  	PotentialTree propagation=(PotentialTree)propagationOptimalPolicies.elementAt(index);

//System.out.println("Pol�ticas �ptimas de referencia: ");
//reference.print();
//System.out.println();
//System.out.println("Pol�ticas �ptimas de la propagaci�n : ");
//propagation.print();
//System.out.println("----------------------------------------------");

    // Get the vector of variables for this table
    Vector varsInTable=reference.getVariables();
    FiniteStates decision=(FiniteStates)varsInTable.elementAt(varsInTable.size()-1);

    // Make a configuration for all the relation variables but the first one 
		// (it is the variable for the decision itself)
    Configuration conf=new Configuration(varsInTable,decision.getName());
        
    // We must consider the whole set of values for this configuration
    long cases=(long)conf.possibleValues();
    for(i=0; i < cases; i++) {    

      // Now, get the policies for the reference table
      Vector refPolicies=reference.getValuesForConf(conf,decision);
      
      // The same fot toCompare
      Vector progPolicies=propagation.getValuesForConf(conf,decision);
    
      // Check if all these values are 0 in reference solution
      allCeros=VectorManipulator.checkAllCerosDoubles(refPolicies);

      // If all values are cero there is nothing more to do with
      // this configuration
      if (allCeros == true) {
        conf.nextConfiguration();
        continue;
      }
      else{
      	unconstrained++;
				
      	// Compare both vectors looking for a match
        boolean match=VectorManipulator.lookForMatchDoubles(refPolicies,progPolicies);

        if (match == true){
        	matches++;
        }

        // Consider the next configuration
        conf.nextConfiguration();
      }
    }

  	// Return the number of matches
		results=new Vector();
		results.addElement(new Double(matches));
  	return results;
  }
	
 /**
  * This method compares two decision tables looking for the number
  * of matches between compare and reference solution for global
	* policies
  *@param flag, showing the interest about optimal or global policies
  *@param index of the decision tables to compare
  *@return vector containing
	*   a) number of matches
	*   b) expected utility difference 
  */
  public Vector compareGlobalPoliciesForDecision(int index){  	
		Vector results;
  	boolean allCeros;
    long unconstrained=0;
    double matches=0, difference=0;
    long i;
    int indMax;
		Vector objectiveRef=null,objectiveProp=null;

    // Get the potential trees with the policies to compare
  	PotentialTree reference=(PotentialTree)referenceGlobalPolicies.elementAt(index);
  	PotentialTree propagation=(PotentialTree)propagationGlobalPolicies.elementAt(index);

		// Get the potentials with the utilities
		Vector referenceUtilities=getPotentialsFromReferenceRelations();
		Vector propagationUtilities=toCompare.getResults();
		Potential refUtilPot=(Potential)referenceUtilities.elementAt(index);
//System.out.println("Comparando pol�ticas globales....... ("+index+")");
//System.out.println("Utilidad de referencia: ");
//refUtilPot.print();
//System.out.println("-------------------------------------------");
		Potential progUtilPot=(Potential)propagationUtilities.elementAt(index);
//System.out.println("Utilidad de propagacion: ");
//progUtilPot.print();
//System.out.println("-------------------------------------------");
//System.out.println("Pol�tica de referencia.....................");
//reference.print();
//System.out.println("-------------------------------------------");
//System.out.println("Pol�ticas de propagaci�n...................");
//propagation.print();
//System.out.println("-------------------------------------------");
		
    // Get the vector of variables for this table
    Vector varsInTable=reference.getVariables();
    FiniteStates decision=(FiniteStates)varsInTable.elementAt(varsInTable.size()-1);

    // Make a configuration for all the relation variables but the first one (it is
    // the variable for the decision itself)
    Configuration conf=new Configuration(varsInTable,decision.getName());
        
    // We must consider the whole set of values for this configuration
    long cases=(long)conf.possibleValues();
    for(i=0; i < cases; i++) {    
      // Now, get the policies for the reference table
      Vector refPolicies=reference.getValuesForConf(conf,decision);
      
      // The same fot toCompare
      Vector progPolicies=propagation.getValuesForConf(conf,decision);
    
      // Check if all these values are 0 in reference solution
      allCeros=VectorManipulator.checkAllCerosDoubles(refPolicies);

			// Compute the medium value for the expected utility for both
			// solutions. 
			Vector refUtils=refUtilPot.getValuesForConf(conf,decision);
			Vector progUtils=progUtilPot.getValuesForConf(conf,decision);

			// Compute the medium values
			double refMedium=VectorManipulator.getMediumValueForUtility(refUtils,refPolicies);
			double progMedium=VectorManipulator.getMediumValueForUtility(progUtils,progPolicies);

      // Compute the difference
      difference+=Math.pow((refMedium-progMedium),2);
			
      // If all values are cero there is nothing more to do with
      // this configuration
      if (allCeros == true) {
        conf.nextConfiguration();
        continue;
      }
      else{
      	unconstrained++;
      
      	// Compare both vectors looking for a match
        boolean match=VectorManipulator.lookForMatchDoubles(refPolicies,progPolicies);

        if (match == true){
        	matches++;
        }

        // Consider the next configuration
        conf.nextConfiguration();
      }
    }

  	// Return the number of matches and the difference of expected utility
		results=new Vector();
		results.addElement(new Double(matches));
    difference=difference/(double)cases;
    difference=Math.sqrt(difference);
		results.addElement(new Double(difference));
  	return results;
  }
	
  /**
   * <p>
   * Private method to get the optimal policy for the first of the decisions.
   * This can be done without taking into account the rest of the decisions.
   * @param potential to analyze
   * @return vector with two elements: 
	 * a) number of branches for the policy
	 * b) potential tree with the policies
   * </p>
   */
   private Vector computePolicyForFirstDecision(Potential firstDecisionPotential){
     boolean allCeros=false;
     long unconstrained=0;
     long i;
     int indMax;
		 Vector solution=new Vector();

     // The relation for the first decision will be the last one, just because are
     // ordered
     Vector varsInTable=firstDecisionPotential.getVariables();
     Node firstDecision=(Node)varsInTable.elementAt(varsInTable.size()-1);

     // Build a PotentialTree to store the branches related to the optimal policies
     PotentialTree tree=new PotentialTree(varsInTable);

     // Make a configuration for all the relation variables but the first one (it is
     // the variable for the decision itself)
     Configuration conf=new Configuration(tree.getVariables(),firstDecision.getName());
         
     // We must consider the whole set of values for this configuration
     long cases=(long)conf.possibleValues();
     for(i=0; i < cases; i++) {
       // For every case, initialize the values of the tree with the optimal policies
       // for the configuration under analysis
       tree.setValuesForConf(conf,(FiniteStates)firstDecision,0);
     
       // Now, get the utility values for this configuration
       Vector utilities=firstDecisionPotential.getValuesForConf(conf,(FiniteStates)firstDecision);
     
       // Check if all these values are 0
       allCeros=VectorManipulator.checkAllCerosDoubles(utilities);
       
       // If all values are cero there is nothing more to do with
       // this configuration
       if (allCeros == true) {
         conf.nextConfiguration();
         continue;
       }
       else
       	unconstrained++;
       
       // If this is not true, get the maximum between the values
       // in the vector with the utilities
       indMax=VectorManipulator.findMaxDoubles(utilities);
       
       // Maybe several configurations with the same value. We must
       // check this condition to properly detect all the optimal policies
       Vector repetitions=VectorManipulator.isRepeatedMaxDoubles(utilities,indMax,threshold);
       
       // Set the values in the tree containing the configurations belonging to
       // the optimal policies
       tree.setValues(conf,(FiniteStates)firstDecision,indMax,repetitions,1.0);
       
       // Consider the next configuration
       conf.nextConfiguration();
     }
     
     // Return the results
		 solution.addElement(new Double(unconstrained));
		 solution.addElement(tree);
     return solution;
   }

  /**
   * Private method to compute the policies for a decision with past
   * decisions
   * @param Potential with the decision table for a decision
   * @param index for the decision to deal with in the vector of relations
	 * @return vector with results:
	 *   a) number of branches for optimal policies
	 *   b) tree with optimal policies
	 *   c) branches with global policies
	 *   d) tree with global policies
   */
   
   private Vector computePolicyForDecisionWithPast(Potential decisionPotential, 
                                                 int index) {
     int indMax;
     int i;
		 long optimalBranches=0,globalBranches=0;
          
     // Get the decision itself
     Vector variablesInRel=decisionPotential.getVariables();
     FiniteStates decision=(FiniteStates)decisionPotential.getVariables().elementAt(variablesInRel.size()-1);
     
     // Build a potential tree to contain the policies for this decision
     PotentialTree treeOptimal=new PotentialTree(variablesInRel);
		 PotentialTree treeGlobal=new PotentialTree(variablesInRel);
     
     // Make a configuration for the whole set of variables in the decision table,
     // except the decision itself
     Configuration conf=new Configuration(treeOptimal.getVariables(),decision.getName());
         
     // We must consider the whole set of values for this configuration
     long cases=(long)conf.possibleValues();
     for(i=0; i < cases; i++) {
if (i%500 == 0)
System.out.println("Analizando caso: "+i);
       // Initialize the values of the optimalTree related to this configuration
       treeOptimal.setValuesForConf(conf,decision,0);
			 treeGlobal.setValuesForConf(conf,decision,0);
       
       // Check if this configuration is related to the set of optimal policies
       boolean optimalInPast=checkIfOptimalInPast(index,conf);
       
       // Get the values of utility for the configuration
       // and for all of the values from the decision domain
       Vector utilities=decisionPotential.getValuesForConf(conf,decision);
       
       // Check if all the values are 0. In this case, it is an 
			 // constrained configuration and it should not be considered 
			 // for optimal policies determination
       boolean allCeros=VectorManipulator.checkAllCerosDoubles(utilities);
       
       // Discard the configuration if allCeros is true
       if (allCeros == true) {
         conf.nextConfiguration();
         continue;
       }
			 else{
			   globalBranches++;
         if (optimalInPast){
           optimalBranches++;
         }
			 }
       
       // Look for the alternative with maximum value for the expected utility
       indMax=VectorManipulator.findMaxDoubles(utilities);
       
       // May be several configuration with the same value for the utility
       Vector repetitions=VectorManipulator.isRepeatedMaxDoubles(utilities,indMax,threshold);
       
       // Set the values in the tree with the optimal policies
       treeGlobal.setValues(conf,decision,indMax,repetitions,1.0);

			 // If the configuration is related to optimal policies, add it to the
			 // tree of optimal policies
			 if (optimalInPast){
			   treeOptimal.setValues(conf,decision,indMax,repetitions,1.0);
			 }
       
       // Go to the next configuration
       conf.nextConfiguration();
     }
     
     // Store the results on the vector of output
		 Vector output=new Vector();
		 output.setSize(4);
     output.setElementAt(new Double(optimalBranches),0);
		 output.setElementAt(treeOptimal,1);
		 output.setElementAt(new Double(globalBranches),2);
		 output.setElementAt(treeGlobal,3);

		 // return the vector
		 return output;
   }
   

 /**
  * Method to get the potentials for the reference relations
  * @return vector with the potentials
  */
  private Vector getPotentialsFromReferenceRelations(){
  	Vector relations=reference.getRelationList();
  	Vector output=new Vector();
  	output.setSize(relations.size());
  	int i;
  	
  	// Consider every relation
  	for(i=0; i < relations.size(); i++){
  		Relation relation=(Relation)relations.elementAt(i);
  		Potential potential=relation.getValues();
  		output.setElementAt(potential,i);
  	}
  	
  	// Return the output vector
  	return output;
  }

 /**
  * Private method to test if a given configuration for a decision with
  * past is completely consistent with the set of optimal policies for
  * the set of previous decisions
  * @param index of the decision under analysis
  * @param configuration to check
  * @param vector with the optimal policies
  * @return results of the check
  */
  
  private boolean checkIfOptimalInPast(int index, Configuration configuration) {
    Potential previousOptimalTree;
    double value;
    int i,j;
    
    // Consider previous decisions
    for(i = reference.getRelationList().size()-1; i > index; i--) {
      // Restrict the potential for the previous decision to the variables
      // in the conf to check. If there is a one value in this restricted
      // tree, the branch can be considered as optimal
      previousOptimalTree=(Potential)referenceOptimalPolicies.elementAt(i);
      previousOptimalTree=previousOptimalTree.restrictVariable(configuration);
      
      // Check if any of the values is a 1
      Configuration confRestricted=new Configuration(previousOptimalTree.getVariables());
      for(j=0; j < confRestricted.possibleValues(); j++){
        value=previousOptimalTree.getValue(confRestricted);
        if (value != 0){
          return true;
        }
        confRestricted.nextConfiguration();
      }
      
      // If this point is reached, the configuration is not optimal
      break;
    }
    
    // In this case, return false
    return false;
  }
  
 /**
  * Method to print the information about the last comparison
  */
  public void printInformation(){
    double branches,matches;
    int tables=referenceOptimalPolicies.size();
    int i;
    
    // Begin showing the trees with the optimal policies for both solutions
    System.out.println("TABLES with optimal policies for decisions (reference) ......");
    for(i=0; i < tables; i++){
      System.out.println("Table for decision "+i);
      PotentialTree policy=(PotentialTree)referenceOptimalPolicies.elementAt(i);
      policy.print();
      System.out.println("-----------------------------------------------------------");
    }

    // Now the trees with the optimal policies for toCompare solution
    System.out.println("TABLES with optimal policies for decisions (toCompare) ......");
    for(i=0; i < tables; i++){
      System.out.println("Table for decision "+i);
      PotentialTree policy=(PotentialTree)propagationOptimalPolicies.elementAt(i);
      policy.print();
      System.out.println("-----------------------------------------------------------");
    }

    // Now print the number of branches with optimal policies for both solutions
    System.out.println("BRANCHES with optimal policies for decisions (reference).....");
    for(i=0; i < tables; i++){
      branches=((Double)branchesWithReferenceOptimalPolicies.elementAt(i)).doubleValue();
      System.out.println("Branches for decision "+i+" : "+branches);
    }

    // The same for toCompare solution
    System.out.println("BRANCHES with optimal policies for decisions (toCompare).....");
    for(i=0; i < tables; i++){
      branches=((Double)branchesWithPropagationOptimalPolicies.elementAt(i)).doubleValue();
      System.out.println("Branches for decision "+i+" : "+branches);
    }

    // Now the percentage of matches between both solutions
    System.out.println("MATCHES according to optimal policies......");
    for(i=0; i < tables; i++){
      matches=((Double)optimalPoliciesMatches.elementAt(i)).doubleValue();
      System.out.println("Matches for decision "+i+" : "+matches);   
    }

    // Now the same but taking into account the whole set of policies (not only
    // optimal)
    System.out.println("TABLES with policies for decisions (reference) ......");
    for(i=0; i < tables; i++){
      System.out.println("Table for decision "+i);
      PotentialTree policy=(PotentialTree)referenceGlobalPolicies.elementAt(i);
      policy.print();
      System.out.println("-----------------------------------------------------------");
    }

    // Now the trees with the policies for toCompare solution
    System.out.println("TABLES with policies for decisions (toCompare) ......");
    for(i=0; i < tables; i++){
      System.out.println("Table for decision "+i);
      PotentialTree policy=(PotentialTree)propagationGlobalPolicies.elementAt(i);
      policy.print();
      System.out.println("-----------------------------------------------------------");
    }

    // Now print the number of branches with policies for bot solutions
    System.out.println("BRANCHES with policies for decisions (reference).....");
    for(i=0; i < tables; i++){
      branches=((Double)branchesWithReferenceGlobalPolicies.elementAt(i)).doubleValue();
      System.out.println("Branches for decision "+i+" : "+branches);
    }

    // The same for toCompare solution
    System.out.println("BRANCHES with global policies for decisions (toCompare).....");
    for(i=0; i < tables; i++){
      branches=((Double)branchesWithPropagationGlobalPolicies.elementAt(i)).doubleValue();
      System.out.println("Branches for decision "+i+" : "+branches);
    }

    // Now the percentage of matches between both solutions
    System.out.println("MATCHES according to global policies......");
    for(i=0; i < tables; i++){
      matches=((Double)globalPoliciesMatches.elementAt(i)).doubleValue();
      System.out.println("Matches for decision "+i+" : "+matches);   
    }

    // Print the differences in expected utility
    System.out.println("EXPECTED UTILITY DIFFERENCES according to global policies.....");
    for(i=0; i < tables; i++){
      matches=((Double)EUDifferences.elementAt(i)).doubleValue();
      System.out.println("EU Difference for decision "+i+" : "+matches);   
    }
  }
  
 /**
  * Method to print the information about the policies
  */
  public void printPolicies(){
    double branches,matches;
    int tables=referenceOptimalPolicies.size();
    int i;
    
    // Begin showing the trees with the optimal policies for both solutions
    System.out.println("TABLES with optimal policies for decisions (reference) ......");
    for(i=0; i < tables; i++){
      System.out.println("Table for decision "+i);
      PotentialTree policy=(PotentialTree)referenceOptimalPolicies.elementAt(i);
      policy.print();
      System.out.println("-----------------------------------------------------------");
    }

    // Now the trees with the optimal policies for toCompare solution
    System.out.println("TABLES with optimal policies for decisions (toCompare) ......");
    for(i=0; i < tables; i++){
      System.out.println("Table for decision "+i);
      PotentialTree policy=(PotentialTree)propagationOptimalPolicies.elementAt(i);
      policy.print();
      System.out.println("-----------------------------------------------------------");
    }

    // Now the same but taking into account the whole set of policies (not only
    // optimal)
    System.out.println("TABLES with policies for decisions (reference) ......");
    for(i=0; i < tables; i++){
      System.out.println("Table for decision "+i);
      PotentialTree policy=(PotentialTree)referenceGlobalPolicies.elementAt(i);
      policy.print();
      System.out.println("-----------------------------------------------------------");
    }

    // Now the trees with the policies for toCompare solution
    System.out.println("TABLES with policies for decisions (toCompare) ......");
    for(i=0; i < tables; i++){
      System.out.println("Table for decision "+i);
      PotentialTree policy=(PotentialTree)propagationGlobalPolicies.elementAt(i);
      policy.print();
      System.out.println("-----------------------------------------------------------");
    }
  }
  
  /**
   * Method to obtain the list of domains of the optimal policies.
   * Each decision appears at the end of each nodelist.
   * If a decision doesn't appear in any nodelist, this means the decision was barren, so any of its options is optimal
   * and doesn't have required variables
   */
  public static ArrayList<NodeList> getDomainsOfPolicies(Vector resultsForPolicies){
	  ArrayList<NodeList> domains;
	  NodeList auxNodes;
	  Potential auxResultsForPolicies;
	  
	  domains = new ArrayList<NodeList>();
	  
	  for (int i=0;i<resultsForPolicies.size();i++){
		  auxResultsForPolicies = (Potential)(resultsForPolicies.elementAt(i));
		  //Barren decisions haven't list of required variables in the list
		  if (auxResultsForPolicies!=null){
			  auxNodes = new NodeList(auxResultsForPolicies.getVariables());
			  domains.add(auxNodes);
		  }
	  }
	  
	  return domains;
  }

 /**
  * Main method for testing purposes
  */

 public static void main(String args[]) throws ParseException, IOException{
    if (args.length < 1){
      System.out.println("Use: ");
      System.out.println("java IDResultsComparator resultsFile (without extension)");
      System.exit(0);
    }
    else{
      IDResultsComparator comparator=new IDResultsComparator(args[0]);
      Network red=Network.read(comparator.idName);

			// Obtain the reference policies
			comparator.getReferencePolicies();
      IDVEWPTAndConstraints propagation=new IDVEWPTAndConstraints((Bnet)red);
      propagation.setThresholdForPrunning(0.7);
      propagation.propagate();
      comparator.setResultsToCompare(propagation);
      comparator.setThreshold(0);

			// Get the propagation policies
			comparator.getPropagationPolicies();

			// Compare both solutions
			comparator.comparePolicies();

      // Print the information about the comparison
      comparator.printInformation();
    }
  }

}


