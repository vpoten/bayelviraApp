/*
 * SamplingOnId.java
 *
 * Created on 22 de diciembre de 2003, 18:44
 */

package elvira.inference.approximate;

import java.util.Vector;
import java.util.Random;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.tools.Stopper;
import elvira.tools.Crono;
import elvira.tools.SamplingOnIdStatistics;
import elvira.inference.Propagation;
import elvira.inference.elimination.ids.*;
import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.database.*;
import elvira.learning.classificationtree.*;
/**
 *
 * @author  Manuel G�mez
 * @author Seraf�n Moral
 *
 * @since 15/03/2002
 */
public class SamplingOnId extends Propagation{
   
  /**
   * Define some constants for this class
   */

  final static int PARENTSNOTOBSERVED=-1;
  final static int WRONGCONFIGURATION=-2;
  public final static int FORWARDSTAGE=1;
  public final static int RECONSIDERATIONSTAGE=2;
  final static double PERCENTAGEOFUTILITY=10;
  final static float THRESHOLDFORPAST=10;
  public final static int SAMPLING=1;
  public final static int MAXIMAZING=2;
  final static int ALL=3;
  final static int ABSOLUTE=1;
  final static int RELATIVE=2;
  
  /**
   * The list used to keep the decision variables in order: from first
   * one to last one. This order will be imposed over all vectors with
   * information related to the decisions
   */
  
  private NodeList decisions;
  
  /**
   * Vector with the set of relations used to store quantitative information
   * about decisions. The information for the decision will be stored
   * following the order the decisions have in the previous data member
   */
  
  private RelationList decisionsRelations;
  
  /**
   * Vector with all the relations for the variables in the ID. Here we keep
   * the relations once constraints have been applied. This data member is
   * needed just because the evaluation method for every configuration will
   * change the relations, and we have to restore them for new evaluations
   */
  
  private Vector relations;
  
  /**
   * Data member to allow qualitative evaluation of ID, to determine the order
   * of instantiation and the variables related to every decision
   */
  
  private IDQualitativeVariableElimination qualitativeEval;
  
  /**
   * Data member to store the order of instantiation that will be used
   * during the evaluation of the influence diagram
   */
  
  private Vector orderOfInstantiation;
  
  /**
   * Data member to allow quantitative evaluation of ID
   */
  
  private IDVEWPTAndConstraints quantitativeEval;
  
  /**
   * Vector to store the results comming from the exact evaluation of the ID.
   * These results will be used to make comparisons adn to measure the
   * distance between exact and approximate solution
   */ 
  
  private Vector exactResults;
  
  /**
   * Data member to store the number of phases requiered to complete the
   * propagation. This value will be introduced when the method is called
   */
  
  private int propagationPhases;
  
  /**
   * Data member to show the phase that the algorithm is right now executing
   */
  
  private int phase;
  
  /**
   * To store the number of samples that must be generated for every phase
   */
  
  private int samplesToGenerate;
  
  /**
   * To identify the decision under observation. It will be an integer, related
   * to the position where the decision is stored in decisions data member
   */
  
  private int indexOfDecisionConsidered;
  
  /**
   * Vector to store the number of samples analyzed for every decision
   */
  
  private Vector samplesStored;
  
  /**
   * Random number generator used to get new samples
   */
  
  private Random random;
  
  /**
   * Vector to store the cases related to every decision. Every position
   * of this vector will contain a caseListMem
   */
  
  private Vector cases;

  /**
   * Data member for teh cases evaluated for the last decision
   */

  private CaseListMem casesEvaluatedForLastDecision;
  
  /**
   * Vector containing the classifiers for every decision. This vector will
   * contain the classifiers using C4.5
   */
  
  private Vector maximizationClassifiers;
  
  /**
   * Vector containing the classifiers used to get new samples. This vector
   * will contain classifiers using Dirichlet
   */
  
  private Vector samplingClassifiers;
  
  /**
   * Vector with the classifiers used to keep the optimal policies for the ID.
   * One of them will contain classifiers generated taking into account only
   * the branches related to optimal policies
   */
  
  private Vector exactClassifiersWithOptimality;
  
  /**
   * Vector containing the classifiers used to keep optimal policies. The ones
   * stored in this vector consider all the branches in decision tables
   */
  
  private Vector exactClassifiersWithoutOptimality;

  /**
   * Vector of potential trees to store the branches related to optimal
   * policy, but not as a result of a classification, but analyzing the
   * tables obtained from the exact evaluation
   */

  private Vector optimalPolicy;
  
  /**
   * To store statistics about evaluation
   */
  
  private SamplingOnIdStatistics statistics;
  
  /**
   * Data members used to control max and min values for utility
   * This values are obtained during the evaluation of the ID, with
   * VariableElimination method (VEWPTAndCOnstraints)
   */
  
  private double maximum;
  private double minimum;
  
  /**
   * Counter to control the number of samples evaluated for every decision
   */
  
  private long evaluated[];
  
  /**
   * To control the stage where the algorithm is: forward or reconsideration
   */
  
  private int stage;
  
  /**
   * To control the presence of changes during the staged of the algorithm. There
   * will be a flag for every decision
   */
  
  private boolean changes[];
  
  /**
   * Data members to configure a measure of proximity between several alternatives
   */
  
  private double thresholdOfProximity;

  /**
   * Stopper, for debuggiong purposes
   */

  Stopper stopper;

  /**
   * Crono, to control times of computation
   */

  Crono crono;
  
  /**
   * Method to create one object to evaluate IDs with simulations. This constructor
   * will receive the next arguments:
   * @param diag <code>IDiagram</code> influence diagram to be evaluated
   * @param phases String showing the number of phases to consider
   * @param samples String showing the number of samples to consider for every phase
   */
  
  public SamplingOnId(IDiagram diag, String phases, String samples){
     int i;
     
     // First at all, assign the number of phases for the simulation process
     
     propagationPhases=Integer.parseInt(phases);
     
     // Assign the number of samples to consider for every phase
     
     samplesToGenerate=Integer.parseInt(samples);
     
     // Build the random number generator
     
     random=new Random();
     
     // Build the object to use to evaluate (quantitative evaluation) the ID
    
     quantitativeEval=new IDVEWPTAndConstraints(diag);
     quantitativeEval.generateStatistics=true;
    
     // The network data member of this object, inherited from Propagation, will
     // be assigned to network data member of quantitativeEval
     
     network=quantitativeEval.network;
     
     // Get the list of decisions
     
     decisions=diag.getDecisionList();
     
     // Build the object used to evaluate (qualitative evaluation) the ID
     
     qualitativeEval=new IDQualitativeVariableElimination(diag);
     
     // Build the vector to store the order of instantiation
     
     orderOfInstantiation=new Vector();
     orderOfInstantiation.setSize(decisions.size());
     
     // Create the vector used to store the samples analyzed for every
     // decision
     
     cases=new Vector();

     // Fix the size of this vector

     cases.setSize(decisions.size());
     
     // Create the vector of classifiers
     
     maximizationClassifiers=new Vector();
     samplingClassifiers=new Vector();
     exactClassifiersWithOptimality=new Vector();
     exactClassifiersWithoutOptimality=new Vector();

     // Create the vector with the optimal policy, without classification

     optimalPolicy=new Vector();
     optimalPolicy.setSize(decisions.size());
     
     // Show that the actual phase is 0
     
     phase=0;
     
     // At the beginning the stage is forward
     
     stage=FORWARDSTAGE;
     
     // Create the object used to store the statistics
     
     statistics=new SamplingOnIdStatistics(propagationPhases);
     
     // Initializa evaluated, to store the number of samples evaluated
     // for every decision. The same for the vector with the flags showing
     // the presence of changes
     
     evaluated=new long[decisions.size()];
     changes=new boolean[decisions.size()];
     for(i=0; i < decisions.size(); i++){
        evaluated[i]=0;
        changes[i]=false;
     }
     
     // At the beginning, the threshold of proximity is 0
     
     thresholdOfProximity=0;

     // Create a stopper for helping debugging

     stopper=new Stopper();

     // Create the crono

     crono=new Crono();
     
  }// end of Constructor
  
  
  /**
   * Method to carry out the evaluation on the ID
   */
  
  public void propagate(){
     // Initialize the information required to proceed with the
     // simulation
     
     initSimulation();
     
     // Now, its the time to get an exact evaluation of the influence
     // diagram. This is done to control the progress of the algorithm
     // solving the influence diagram
     
     getExactEvaluation();
     
     // Once the exact evaluation is done, get the optimal policies
     // derived from the evaluation. This is done through a double
     // classification: one taking into account only the branches
     // related to optimal policies and another one for all the
     // branches
     
     classifyExactResultsWithOptimality();
     classifyExactResultsWithoutOptimality();
     
     // Store the initial values about the influence diagram
     // and its exact evaluation
    
     getAndStoreInitialMeasures();
     
     // Once the preparation is done, we can proceed to apply the
     // simulation
    
     crono.start(); 
     simulate();
     
     // Finally, put the information store in statistics data member
     
     try{
       statistics.printInformation();
     }catch(IOException e){};
  }
  
  /**
   * Method to carry out the simulation for the influence diagram
   */
  
  private void simulate(){
     // Consider all the phases we want for the simulation
     
     for(phase=0; phase < propagationPhases; phase++){

        // Initialize the phase FORWARD
        
        initStage(FORWARDSTAGE);
        
        // Proceed with the stage
   
        treatDecisions();
        
        // Get the measures after the end of this stage
       
        if (phase == propagationPhases-1) 
          getAndStoreMeasuresForForwardStage();
        
        // Proceed with RECONSIDERATION stage
        
        initStage(RECONSIDERATIONSTAGE);
        
        // Go on with the decisions
        
        reconsiderDecisions();
        
        // Get and store the measures for this phase
        
        if (phase == propagationPhases-1) 
          getAndStoreMeasuresForPhase();
     }
     
     // Once the simulation is done, we have to get the set of final
     // measures about the process of simulation
   
     try{
        statistics.printInformation();
     }
     catch(IOException e){}
  }
  
  /**
   * Method to initialize the data to begin with a new phase
   * @param stage the stage that is going to begin
   */
  
  private void initStage(int stage){
     int i;
     
     // This method will be responsible for getting updated the
     // data relative to changes flag for decisions as well as
     // the number of evaluations made for every decision
     
     for(i=0; i < decisions.size(); i++){
       changes[i]=false;
     }
     
     // Set the value of stage data member
     
     this.stage=stage;
  }
  
  /**
   * Method to treat decisions in the FORWARD stage of the algorithm
   */
  
  private void treatDecisions(){
     // This method will begin with the last decision, and from it to the
     // beginning. For the last decision it will be generated a set of cases
     // at random. This set of cases will be the base to deal with the rest
     // of decisions
     
     treatLastDecision();
     
     // Now act on decisions with future decisions
     
     treatDecisionsWithFuture();
     
     // Convert to decision nodes the nodes related to
     // decision variables
     
     convertToDecisionNodes(SAMPLING);
  }
  
  /**
   * Method to treat decisions in the RECONSIDERATION stage of the algoritm
   */
  
  private void reconsiderDecisions(){
     // Reconsider the last decision
     
     reconsiderLastDecision();
     
     // Reconsider intermediate decisions. This is the same that
     // treat intermediate decisions

     treatDecisionsWithFuture();
     
     // Convert to decision nodes, but now the values
     // related to each decision will be the ones obtained
     // from sampling classifiers
     
     convertToDecisionNodes(SAMPLING);
  }
  
  /**
   * Method to reconsider the results of the last decision
   */
  
  private void reconsiderLastDecision(){
    Vector casesWithoutContext;
    CaseListMem allCases;
    CaseListMem filteredCases;
    boolean newCases;
   
    // We must analyze the data base of cases already considered
    // to determine if the cases have context (for the same combination
    // of values for chance nodes, there are different values only
    // for decision). If a case has context, this means the optimal
    // policy for it was evaluated with criteria

    casesWithoutContext=analyzeContext();

    // Now, we must add context for all of this cases. At the end, the
    // vector will contain new cases, if it was possible to get them

    newCases=addContext(casesWithoutContext);

    // If it is needed, evaluate the cases

    if (newCases){

      // Get the case list mem with the whole set of cases

      allCases=(CaseListMem)cases.elementAt(decisions.size()-1);

      // Evaluate the cases
      
      evaluateNewSamples(allCases);

      // Filter the cases

      filteredCases=filterCaseListMem(casesEvaluatedForLastDecision,decisions.size()-1);

      // Finally, proceed to classify

      classify(decisions.size()-1, filteredCases, ALL);
    }

    // The node must be converted into chance node

    convertToChanceNode(decisions.size()-1,MAXIMAZING);
  }
  
  /**
   * Method to reconsider the results of intermediate decisions
   */
  
  private void reconsiderIntermediateDecisions(){
  }
  
  /**
   * Method to reconsider one of the intermediate decisions
   * @param index of the decision
   */
  
  private int reconsiderIntermediateDecision(int index){
     return 0;
  }
  
  /**
   * Method to reconsider the results of the first decision
   */
  
  private void reconsiderFirstDecision(){
  }

  /**
   * Private method to analyze the set of cases already evaluated
   * to determine which of them could take to non well-based decisions
   * @return Vector with cases without enough context. Each position
   *         will contain the cases related to it
   */

  private Vector analyzeContext(){
    int i,j,index,number;
    Vector casesInContext;
    Vector casesWithoutContext=new Vector();
    Vector related=null;
    CaseListMem dataBase;
    Vector contexts;
    long completePast;
    boolean analyzed[];

    // Get the data base of cases evaluated for the last decision. In
    // it we will look for cases where past of the decision is not well
    // possed (there are few alternatives for them)

    dataBase=(CaseListMem)cases.elementAt(decisions.size()-1);
    number=dataBase.getNumberOfCases();
    analyzed=new boolean[number];

    // Get the number of alternatives allowed for past decisions

    completePast=getNumberOfAlternativesForCompletePast(decisions.size()-1);

    // Go on every case

    for(i=0; i < number; i++){
      if (analyzed[i] != true){
        analyzed[i]=true;

        // Now, we must get cases related to i case (the same context)
        // Will be configuration where the changes will be centered
        // on the past decisions. The method getCasesInContext will
        // analyze configurations related with the one stored in
        // i position. The vector cases in context will include the
        // indices of the cases related to i case

        casesInContext=getCasesInContext(dataBase,i,analyzed);

        // If the number of similar cases is under the threshold of past
        // add the case to casesWithoutContext

        if (((double)casesInContext.size()/completePast)*100 < THRESHOLDFORPAST){
          // Create a new context

          contexts=new Vector();

          // Add the case
          
          contexts.addElement(dataBase.get(i));

          // Add the cases related to the context

          for(j=0; j < casesInContext.size(); j++){

            if (j == 0){
              related=new Vector();
            }
            
            index=((Integer)casesInContext.elementAt(j)).intValue();

            // Put the case into contexts

            related.addElement(dataBase.get(index));
          }

          // Set this vector as second element in contexts

          if (related != null)
             contexts.addElement(related);

          // Add contexts vector to casesInContext vector

          casesWithoutContext.addElement(contexts);
        }
      }
    }

    // Return the vector of cases without context

    return casesWithoutContext;
  }

  /**
   * Private method to compute the number of alternatives taking part
   * into the past of a given decision
   * @param index index of the decision under consideration
   * @return long number of alternatives for complete past
   */

  private long getNumberOfAlternativesForCompletePast(int index){
    FiniteStates decision;
    long result=1;
    int i;

    for(i=0; i < index; i++){
      // Get the decision

      decision=(FiniteStates)decisions.elementAt(i);

      // Get the number of alternatives

      result=result*decision.getNumStates();
    }

    // Return result

    return result;
  }

  /**
   * Private method to get the cases belonging to the context of a given
   * one
   * @param dataBase data base of cases to anlyze
   * @param index of case under consideration
   * @param analyzed array of boolean to ahow the cases already
   *        used
   * @return vector of indices of cases related to case in index position
   */

  private Vector getCasesInContext(CaseListMem dataBase, int index, boolean analyzed[]){
    Configuration base=dataBase.get(index);
    Configuration considered;
    boolean inContext;
    Vector results=new Vector();
    int i;

    for(i=0; i < analyzed.length; i++){
      
      // Test only non analyzed cases

      if (analyzed[i] != true){

        // Get the case
        
        considered=dataBase.get(i);
        inContext=testIfBelongsToContext(base,considered);

        // If it is in the context of base, add it to the vector
        // of results

        if (inContext){
          analyzed[i]=true;
          results.addElement(new Integer(i));
        }
      }
    }

    // Finalyy, return results

    return results;
  }

  /**
   * Private method to determine if two cases belongs to the same
   * context: the differences between them are related to chance
   * nodes only
   * @param base one configuration
   * @param other another configuration
   * @return boolean value
   */

  private boolean testIfBelongsToContext(Configuration base, Configuration other){
    FiniteStates var;
    int stateBase,stateOther;
    int i;
    
    // The values for chance nodes must be the same in both configurations

    for(i=0; i < base.size(); i++){
      var=base.getVariable(i);

      // See its value, if it is a chance node

      if (var.getKindOfNode() == Node.CHANCE){
        // Get the value for this variable in base conf
        
        stateBase=base.getValue(var.getName());

        // Get the value for this variable in other conf

        stateOther=other.getValue(var.getName());

        // Check if the values are the same

        if (stateBase != stateOther)
          return false;
      }
    }

    // If we reach this point, both configurations belong to the same
    // context

    return true;
  }

  /**
   * Private method to add the cases to complete the context into the
   * database of cases to evaluate. The final result of this method
   * will be to add new cases to the data base of cases to evaluate, but
   * now the new cases are related to the previous ones
   * @param limitedCases vector with cases with incomplete context
   * @return boolean to show if any case was introduced
   */
 
  private boolean addContext(Vector limitedCases){
    Configuration base;
    Vector context;
    Vector related=null;
    PotentialTree pastTree;
    ProbabilityTree probTree;
    long alreadyInContext=1;
    long allAlternatives=getNumberOfAlternativesForCompletePast(decisions.size()-1);
    long rest;
    double value;
    boolean added=false;
    int i,j;
   
    // Every position of the vector passed as argument contains:
    // first: the base configuration
    // second: ac vector with the cases related to its context

    for(i=0; i < limitedCases.size(); i++){
      // Get the base configuration

      context=(Vector)limitedCases.elementAt(i);

      // Get the base configuration

      base=(Configuration)context.elementAt(0);

      // Get the vector with the set of related cases

      if (context.size() > 1){
        related=(Vector)context.elementAt(1);
      }

      // Make a CaseListMem to store these and new cases

      CaseListMem considered=new CaseListMem(base.getVariables());

      // Add all these cases to the CaseListMem

      considered.put(base);

      if (context.size() > 1){
        for(j=0; j < related.size(); j++){
          considered.put((Configuration)related.elementAt(j));
          alreadyInContext++;
        }
      }

      // Make a potentialTree with the decision variables belonging
      // to the past
      
      Vector past=new Vector();

      for(j=0; j < decisions.size()-1; j++){
        past.addElement((Node)decisions.elementAt(j));
      }

      pastTree=new PotentialTree(past);

      // Assign to this potential tree a probability tree with an
      // uniform distribution for the alternatives not considered 

      rest=allAlternatives-alreadyInContext;
      value=1.0/rest;

      // Make a probability tree with this value

      probTree=new ProbabilityTree(value);

      // This probability tree is assigned to pastTree

      pastTree.setTree(probTree);

      // Set 0 values to already considered combinations

      setCeros(pastTree,base,related);

      // Check the constraints for this set of variables

      pastTree=((IDiagram)network).applyConstraintsOnPotential(pastTree,false); 

      // Generate new samples from this distribution

      long numberOfNewCases=generateSamplesForContext(pastTree,considered,allAlternatives);

      // Modify added if needed

      if (numberOfNewCases > 0){
        added=true;

        // Put the new cases in the database of cases for the last decisions

        addNewCases(considered,numberOfNewCases,decisions.size()-1);

        // Set the flag changes

        changes[decisions.size()-1]=true;
      }
    }
    
    // Return added

    return added;
  }

  /**
   * Private method to set ceros in a given PotentialTree. The positions
   * will be given by a configuration and a vector of configurations
   * @param pastTree potentialTree to modify
   * @param base configuration defining the context
   * @param related Vector of related configurations
   */

  private void setCeros(PotentialTree pastTree, Configuration base, Vector related){
    Configuration restricted;
    Configuration confRelated;
    int i;

    // Insert a 0 in the position given by base

    restricted=new Configuration(pastTree.getVariables());
    restricted.resetConfiguration(base);

    // Set a cero

    pastTree.setValue(restricted,0.0);

    // Consider all related cases

    if (related != null){
      for(i=0; i < related.size(); i++){
        confRelated=(Configuration)related.elementAt(i);

        // Set the values from confRelated

        restricted.resetConfiguration(confRelated);

        // Set the cero

        pastTree.setValue(restricted,0.0);
      }
    }
  }

  /**
   * Method to generate samples related to a given one
   * @param pastTree potentialTree with an uniform distribution for the
   *        rest of alternatives
   * @param dataBase CaseListMem with cases already considered for a given
   *        context 
   * @param numberOfAlternatives number of possible alternatives for the decisions
   * @return counter with the number of added cases
   */

  private long generateSamplesForContext(PotentialTree pastTree, 
                                   CaseListMem considered, long numberOfAlternatives){
    Configuration conf;
    Configuration base;
    Configuration newContext;
    double value;
    double discarded=0;
    int i;
    double summ;
    boolean found,used;
    long generated=0;
    double limit;
   
    // Get the percentage of covered alternatives

    double rate=(double)(considered.getNumberOfCases()-1)/numberOfAlternatives*100;

    // Get the vars in pastTree

    Vector vars=pastTree.getVariables();

    // Make a configuration for these variables

    conf=new Configuration(vars);

    // Get the base configuration

    base=considered.get(0);

    // We have to reach the threshold or to discard a certain number of
    // samples

    if (numberOfAlternatives > 1000)
      limit=numberOfAlternatives*THRESHOLDFORPAST/100;
     else
       limit=numberOfAlternatives/2;

    while(rate < THRESHOLDFORPAST && discarded < limit){
      // Generate a random number
      
      value=random.nextDouble();
     
      // Consider the values stored in the potential, until
      // reaching the value generated
     
      for(i=0,summ=0,found=false; i < FiniteStates.getSize(vars); i++){
        summ+=pastTree.getValue(conf);
        
        // Check if the summ is bigger than number
        
        if (summ > value){
          found=true;
          break;
        }
        
        // Continue with the next configuration

        conf.nextConfiguration();
      }

      // If at this point found is true, this means we got a new set of
      // alternatives

      if (found == true){

        // Make a complete configuration with this context

        newContext=new Configuration(base.getVariables());

        // Set the values for the variables

        newContext.setValues(base,conf);

        // The new case is added to considered, but only if this configuration
        // was not added before

        used=isAlreadyConsidered(considered,newContext);

        if (used == false){
           considered.put(newContext);

           // Add 1 to generated

           generated++;
        }
        else{
          discarded++;
        }
      }
      else{
        discarded++;
      }

      // Compute again the rate

      rate=((double)considered.getSize())/numberOfAlternatives*100;
    }

    // Return the number of generated

    return generated;
  }

  /**
   * Method to add new cases to the data base of cases for a given decision
   * @param newCases CaseListMem with the new cases to include
   * @param numberOfNewCases counter for new cases. Remember that
   *        not all the cases if newCases are really new cases: the first
   *        ones were evaluated before and define a context
   * @param index for the decision to consider
   */

  private void addNewCases(CaseListMem newCases, long numberOfNewCases, int index){
    Configuration newCase;
    int i,j;
    
    // Get the case list mem with the cases previously used for this 
    // decision

    CaseListMem used=(CaseListMem)cases.elementAt(index);

    // The cases in considered must be added, but only the recently
    // generated

    for(i=newCases.getNumberOfCases()-1,j=0; j < numberOfNewCases; j++,i--){
      // Get the new case

      newCase=newCases.get(i);

      // Add it ro used

      used.put(newCase);
    }
  }

  /**
   * Method to get the initial values about the influence diagram, as
   * number of decisions, sizes of its tables, number of branches related
   * to optimal policies....
   */
  
  private void getAndStoreInitialMeasures(){
     Potential result;
     Vector sizes=new Vector();
     Vector branches=new Vector();
     Vector unconstrained=new Vector();
     
     // Store the sizes of decision tables
     // For that, we have to make a vector with the sizes for
     // every decision
     
     for(int i=0; i < exactResults.size(); i++){
        result=(Potential)exactResults.elementAt(i);
        
        // Store the size in a vector
        
        sizes.addElement(new Double((double)result.getSize()));
     }
     
     // Finally, store this vector
     
     statistics.setDecisionTableSizes(sizes);
     
     // Now, determine the number of branches related to optimal
     // policies for every decision table
     // Reset the vector of sizes
     
     computeBranchesOfOptimalPolicies(branches);

     // Determine the branches containing non constrained configurations

     computeUnconstrainedBranches(unconstrained);
     
     // Once computed, set these values
     
     statistics.setDecisionOptimalPolicyBranches(branches);
     statistics.setUnconstrainedBranches(unconstrained);
  }
  
  /**
   * Auxiliar method to compute the number of branches related to
   * optimal policies in the classifiers with optimal policy
   * @param sizes vector to return the number of branches related to the
   *        optimal policy
   */
  
  private void computeBranchesOfOptimalPolicies(Vector sizes){
     int i;
     ClassificationTree classifier;
     ProbabilityTree pTree;
     long optimalBranches;
     
     // Consider all the decisions
     
     for(i=0; i < decisions.size(); i++){
        // Get the classifier
        
        classifier=(ClassificationTree)exactClassifiersWithOptimality.elementAt(i);
        
        // Get the probability tree
        
        pTree=classifier.getProbabilityTree();
        
        // Count the number of non cero values in this tree
        
        optimalBranches=countNonCeroValues(pTree);
        
        // Store this number in the vector
        
        sizes.addElement(new Double(optimalBranches));
     }
  }

  /**
   * Auxiliar method to compute the number of branches related to
   * unconstrained configurations 
   * @param sizes vector to return the number of branches related to the
   *        unconstrained configurations
   */
  
  private void computeUnconstrainedBranches(Vector sizes){
     int i;
     ClassificationTree classifier;
     ProbabilityTree pTree;
     long unconstrainedBranches;
     
     // Consider all the decisions
     
     for(i=0; i < decisions.size(); i++){
        // Get the classifier
        
        classifier=(ClassificationTree)exactClassifiersWithoutOptimality.elementAt(i);
        
        // Get the probability tree
        
        pTree=classifier.getProbabilityTree();
        
        // Count the number of non cero values in this tree
        
        unconstrainedBranches=countNonCeroValues(pTree);
        
        // Store this number in the vector
        
        sizes.addElement(new Double(unconstrainedBranches));
     }
  }
  
  /**
   * Method to get and store the measures after the forward stage
   */
  
  private void getAndStoreMeasuresForForwardStage(){
    Vector samples=new Vector();
    Vector distances;
    Vector differences;
    int i;
    
     // Store the time of computation

     statistics.setTime(crono.getTime(),phase,stage);
     
     // First at all, get the number of samples evaluated for every decision
     
     for(i=0; i < decisions.size(); i++){
       samples.addElement(new Long(evaluated[i]));
     }

     // Store these measures

     statistics.setSamples(samples,phase,stage);

     // Compute the distances, as a measure of this stage

     distances=computeDistances();

     // Store this measures for this stage
    
     statistics.setDistances(distances,phase,stage); 

     // Compare the differences taking into account expected utilities

     differences=compareExpectedUtilities(maximizationClassifiers,
                                                        maximum,minimum);

     // Set this differences

     storeMeasures(differences,MAXIMAZING);

     // The same for sampling classifiers

     differences=compareExpectedUtilities(samplingClassifiers,
                                                        maximum,minimum);

     // Set this differences

     storeMeasures(differences,SAMPLING);
  }
  
  /**
   * Method to get and store the measures obtained after a phase
   */
  
  private void getAndStoreMeasuresForPhase(){
    Vector samples=new Vector();
    Vector distances;
    Vector differences;
    int i;
    
     // Store the time of computation

     statistics.setTime(crono.getTime(),phase,stage);

     // First at all, get the number of samples evaluated for every decision
     
     for(i=0; i < decisions.size(); i++){
       samples.addElement(new Long(evaluated[i]));
     }

     // Store these measures

     statistics.setSamples(samples,phase,stage);

     // Compute the distances, as a measure of this stage

     distances=computeDistances();

     // Store this measures for this stage
    
     statistics.setDistances(distances,phase,stage); 

     // Compare the differences taking into account expected utilities

     differences=compareExpectedUtilities(maximizationClassifiers,
                                                        maximum,minimum);

     // Set this differences

     storeMeasures(differences,MAXIMAZING);

     // The same for sampling classifiers

     differences=compareExpectedUtilities(samplingClassifiers,
                                                        maximum,minimum);

     // Set this differences

     storeMeasures(differences,SAMPLING);
  }
  
  /**
   * Method to deal with the last decision
   */
  
  private void treatLastDecision(){
     CaseListMem newCases;
     CaseListMem filteredCases;
     
     // The method will begin generating the samples required. The
     // samples will allways be generated for the case of the last
     // decision. For the rest of them, the samples will be retrieved
     // from this database
     
     newCases=generateNewSamples();
     
     // Now, evaluate the ID for all this cases
     
     evaluateNewSamples(newCases);
    
     // May be there are no new cases

     if (casesEvaluatedForLastDecision.getNumberOfCases() != 0){
       // The cases evaluated must be filtered before the classification. The
       // idea is to remove the variables that do not appear in the table with
       // the policies for this decision
     
       filteredCases=filterCaseListMem(casesEvaluatedForLastDecision,decisions.size()-1);
     
       // Finally, classify the results of the evalution with both criteria
     
       classify(decisions.size()-1, filteredCases, ALL);
     }
     
     // Finally, convert the decision in a chance node
     
     convertToChanceNode(decisions.size()-1,MAXIMAZING);
  }
  
  /**
   * Method to treat intermediate decisions
   */
  
  private void treatDecisionsWithFuture(){
    int i;

    // Consider all the intermediate decisions
    
    for(i=decisions.size()-2; i >= 0; i--){
      // Call the method to deal with an intermediate decision

      treatDecisionWithFuture(i);
    }
  }
  
  /**
   * Method to treat a decision that is not the last
   * @param index of the decision to deal with
   */
  
  private void treatDecisionWithFuture(int index){
    CaseListMem newCases;
    CaseListMem filteredCases;
    CaseListMem evaluated;

    // Generate the set of samples for this decision

    newCases=generateSamples(index);

    // Evaluate the ID for this cases

    evaluated=evaluateSamples(index,newCases);

    // Filter the data base of cases, to remove the variables
    // that do not appear in the decision table for this decision.
    // This is required if there were new cases

    if (evaluated.getNumberOfCases() != 0){

      filteredCases=filterCaseListMem(evaluated,index);

      // Classifiy the results

      classify(index,filteredCases,ALL);
    }

    // Make the decision to be a chance node

    convertToChanceNode(index,MAXIMAZING);
  }
  
  /**
   * Method used to generate a new set of samples for the simulation
   */
  
  private CaseListMem generateNewSamples(){
     CaseListMem newCases;
     Configuration conf;
     Vector vars=null;
     Vector nodes;
     Node decision=decisions.elementAt(decisions.size()-1);
     boolean used;
     int discarded;
     int i;
 
     // For the first phase will be required to create the data base
     // of cases. The rest of phases, it will be retrieved the stored
     // data base

     if (phase == 0 && stage == FORWARDSTAGE){
       // Get the whole set of variables that must be sampled. This vector is
       // stored in orderOfInstantiation
     
       vars=(Vector)orderOfInstantiation.elementAt(decisions.size()-1);

       // Add the decision itself as first element of vars

       vars.insertElementAt(decision.getName(),0);

       // Make the caseListMem
     
       nodes=makeVectorOfNodesFromNames(vars);

       // Build the case list

       newCases=new CaseListMem(nodes);
     }
     else{

        // Get the data base for this decision
        
        newCases=(CaseListMem)cases.elementAt(decisions.size()-1);

        // Get the variables of this data base

        nodes=newCases.getVariables();

        // The method generateRandomSample requires the names of the
        // variables, instead of the variables itself

        vars=makeVectorOfNamesFromNodes(nodes);
     }
     
     // Generate the required number of samples
    
     for(i=0,discarded=0; i < samplesToGenerate && discarded < (samplesToGenerate*500);){
        // Generate new sample. Remember that the value for the decision
        // will be -1 until the evaluation is donde
        
        conf=generateRandomSample(vars);

        // Check if the sample was already generated. Test if this operation is 
        // to be kept for the final release of the program.
       
        used=isAlreadyConsidered(newCases,conf);
        
        // If was used before, discard it
        
        if (used == true){
           discarded++;
        }
        else{
           newCases.put(conf);

           // In this case, mark the flag changes for this decision

           changes[decisions.size()-1]=true;

	         // Add 1 to i

	         i++;
        }
     }

     // Store this vector of cases in cases data member, but only
     // the first time

     if (phase == 0){
        cases.setElementAt(newCases,decisions.size()-1);
     }
     
     // Return the vector of cases

     return newCases;
  }
  
  /**
   * Method to generate a random sample: a random value for all
   * the variables stored in the argument passed as argument.
   * The first variable will be the decision itself and will
   * not be stored any value for this position (this shows that this
   * case is not evaluated yet)
   * @param order Vector of variables to generate values for
   * @return conf configuration with values for the variables in
   * order
   */
  
  private Configuration generateRandomSample(Vector order){
     FiniteStates decision;
     Node node;
     Configuration conf=new Configuration();
     Vector vars;
     String nodeName;
     int observedValue=PARENTSNOTOBSERVED;
     int i;
        
     // Get a reference to the decision unser consideration. This
     // method allways consider the random generation of samples
     // is only for the last decision
     
     decision=(FiniteStates)decisions.elementAt(decisions.size()-1);

     // Make a configuration for vars. Before that, we must get the
     // nodes related to the names stored in order
     
     vars=makeVectorOfNodesFromNames(order);
     
     // Make the configuration. Initially will be empty, to allow
     // to detect if all the parents of the nodes were observed.
     // The variables will be added once its values are obtained
     // For the first variable will not be stored any  value (this variable
     // is related to the decision itself, and in this position will
     // be stored the optimal policy for the configuration defined
     // by the rest of variables)
     
     conf=new Configuration();

     // Get values for the rest of variables 
     
     for(i=1; i < order.size(); i++){
        node=(Node)vars.elementAt(i);
        
        // Get an observed value for this node

        observedValue=getObservedValue(node,conf);
        
        // Consider different situations:
        // a) the method gets the value
        // b) the parents of the node are not observed
        //    and the value is not generated. This should
        //    never happen, just because the order of
        //    instantitations was checked to avoid it
        // c) the configuration is constrained and the
        //    the value wont be observed
        
        switch(observedValue){
           case PARENTSNOTOBSERVED:
              System.out.println("Error during sample generation");
              conf.print();
              break;
           case WRONGCONFIGURATION:
              System.out.println("Constrained configuration");
              conf.print();
              System.out.println("\nTrying to get another configuration....");
              i=0;
              conf.resetConfiguration(null);
              conf=new Configuration();
	      System.out.println("Configuracion reseteada.......");
	      conf.print();
	      System.out.println("..............................");
              break;
           default: // There was no problem
              conf.insert((FiniteStates)node,observedValue);
              break;
        }
     }
     
     // Finally, return the configuration
     
     return conf;
  }

  /**
   * Method to generate the samples for a decision. In this case,
   * the samples will be obtained from data base of cases for
   * future decisions
   * @param index index of the decision
   * @return data base with cases to evaluate
   */

  private CaseListMem generateSamples(int index){
    Vector vars=null;
    Vector nodes;
    Node decision=decisions.elementAt(index);
    CaseListMem newCases;
    CaseListMem source;
    Configuration conf;
    boolean considered;
    long firstSample,lastSample;
    long i;

    // The first time this method is called, is needed to create
    // the data base of cases

    if (phase == 0 && stage == FORWARDSTAGE){
      // Get the whole set of variables that must be observed
      // for this decision

      vars=(Vector)orderOfInstantiation.elementAt(index);

      // Add the decision itself, as first variable

      vars.insertElementAt(decision.getName(),0);

      // Make the data base

      nodes=makeVectorOfNodesFromNames(vars);
      newCases=new CaseListMem(nodes);
    }
    else{
      // The data base already exists

      newCases=(CaseListMem)cases.elementAt(index);

      // Get the variables of newCases

      nodes=newCases.getVariables();

      // Make a vector with the names of the variables

      vars=makeVectorOfNamesFromNodes(nodes);
    }

    // Set the index of the first sample. If there is a change
    // for the last decision, is required to evaluate all the
    // cases

    if (changes[decisions.size()-1] == true)
      firstSample=0;
     else
       firstSample=evaluated[index];

     // Get the data base related to the next decision

     source=(CaseListMem)cases.elementAt(index+1);

     // Get the index for the last sample

     lastSample=source.getNumberOfCases();

     // Loop to get the cases

     for(i=firstSample; i < lastSample; i++){
       conf=generateSampleFromDataBase(source,nodes,(int)i);

       // Check if this configuration have been analyzed

       considered=isAlreadyConsidered(newCases,conf);

       // Insert the configuration in the new data base if it is
       // not considered

       if (considered == false)
           newCases.put(conf);
     }

     // The first time, its needed to store the data base
     // in the vector cases

     if (phase == 0){
       cases.setElementAt(newCases,index);
     }

     // Return the data base of cases

     return newCases;
  }

  /**
   * Method to generate a sample for a decision, from the database
   * related to the next decision
   * @param source data base where to get the cases
   * @param varsOfInterest variables we are interested on
   * @param indexOfCase index of the case used to get the values for
   *        the variables of interest
   * @return configuration with values for the variables of interest
   */

  private Configuration generateSampleFromDataBase(CaseListMem source,
                                      Vector varsOfInterest, int indexOfCase){
    Configuration newConf=new Configuration(varsOfInterest);
    Vector varsInDataBase;
    String varName;
    int i, indexOfVar;
    double value;

    // Get the variables present in the data base

    varsInDataBase=source.getVariables();

    // Consider the variables of interest, except the first one (just
    // because this is the decision and it not needed to generate a
    // value for it)

    for(i=1; i < varsOfInterest.size(); i++){
      // Get the name of the variable

      varName=((Node)varsOfInterest.elementAt(i)).getName();

      // Find the position this variable has in the set of variables
      // of the data base

      indexOfVar=findIndexOfVar(varsInDataBase,varName);

      // Get the value for the variable

      if (indexOfVar != -1){
        value=source.getValue(indexOfCase,indexOfVar);

        // Put this value in newConf

        newConf.putValue(varName,(int)value);
      }
      else{
        System.out.println("Error in generateSampleFromDataBase.....");
        System.out.println("Variable "+varName+" is not found in data base");
        System.exit(-1);
      }
    }

    // Return the new configuration

    return newConf;
  }
  
  /**
   * Method to evaluate the new cases stored in a data base of cases
   * @param cases data base of cases to evaluate
   */
  
  private void evaluateNewSamples(CaseListMem cases){
     Configuration conf,newConf;
     Configuration policy=new Configuration();
     FiniteStates decision;
     PotentialTree result;
     Vector anothersConf=new Vector();
     Vector removeVars=new Vector();
     boolean repeated;
     long i;
    
     // Build the case list with the whole set of evaluated cases
     
     if (phase == 0 && stage == FORWARDSTAGE){
       casesEvaluatedForLastDecision=new CaseListMem(cases.getVariables());
     }

     // Get a reference to the decision
     
     decision=(FiniteStates)decisions.elementAt(decisions.size()-1);

     // Get the number of samples to consider
     
     long firstSample=evaluated[decisions.size()-1];
     long lastSample=cases.getNumberOfCases();

     // The variable to remove from the data base will be the decision
     // itself

     removeVars.addElement(decision);
     
     // Evaluate the ID for the samples

     for(i=firstSample; i < lastSample; i++){
        // Get the case
        
        conf=cases.get((int)i);

        // Make a new configuration removing the first variable
        // (it will be the decision itself)

        newConf=new Configuration(conf,removeVars);

        // Restore results data member of quantitativeEval
        
        quantitativeEval.results=new Vector();
        
        // Make the evaluation
        
        quantitativeEval.propagate(newConf);
        
        // Get the results related to the decision
        
        result=getResultsOfQuantitativeEvaluation();
        
        // Get the policy for this configuration
        
        policy=result.getMaxConfiguration(new Configuration());
     
        // The best alternative will be stored in conf
       
        conf.putValue(decision.getName(),policy.getValue(decision)); 
        //cases.setValue((int)i,0,policy.getValue(decision));

        // May be alternatives with similar utility
        
        repeated=analyzeNearAlternatives(result,policy,conf,anothersConf);
        
        // If there are alternatives with similar utility, we must add
        // artificial configurations to classify. This will be added at
        // the end of the database of cases, and we wont have to evaluate
        // for them
        
        if (repeated==true){
           storeNearAlternativesForLastDecision(anothersConf);
           
           // reset anothersConf for the next loop
           
           anothersConf.removeAllElements();
        }

        // Insert optimal, but only if it is a new case

        if (casesEvaluatedForLastDecision.getValue(conf) == 0.0)
           casesEvaluatedForLastDecision.put(conf);

        // Restore the relations of the ID, to evaluate for a new case
        
        quantitativeEval.setCurrentRelations(relations);
     }
    
     // Increase the counter of evaluations for this decision
     
     evaluated[decisions.size()-1]=cases.getNumberOfCases();
  }

  /**
   * Method to evaluate a given set of samples, for a given variable
   * @param index of the decision of interest
   * @param casesToEvaluate data base with the cases to evaluate
   * @return data base with the optimal policies for the cases evaluated
   */

  private CaseListMem evaluateSamples(int index, CaseListMem casesToEvaluate){
    PotentialTree result;
    Vector anothersConf=new Vector();
    Vector removeVars=new Vector();
    FiniteStates decision;
    Configuration conf,newConf,policy;
    CaseListMem solutions=new CaseListMem(casesToEvaluate.getVariables());
    long firstSample,lastSample;
    boolean repeated;
    long i;

    // Get a reference to the decision

    decision=(FiniteStates)decisions.elementAt(index);

    // Get the number of samples to consider

    if (changes[decisions.size()-1] == true)
      firstSample=0;
     else
       firstSample=evaluated[index];

    lastSample=casesToEvaluate.getNumberOfCases();

    // Make a vector containing the decision itself, to remove
    // it from the cases

    removeVars.addElement(decision);

    // Make the evaluations

System.out.println("A evaluar: "+(lastSample-firstSample));
    for(i=firstSample; i < lastSample; i++){
      // Get the case

      conf=casesToEvaluate.get((int)i);

      // Make a new configuration, removing the decision

      newConf=new Configuration(conf,removeVars);

      // Restore the results data member of quantitativeEval

      quantitativeEval.results=new Vector();

      // Evaluate for this conf

      quantitativeEval.propagate(newConf);

      // Get the results

      result=getResultsOfQuantitativeEvaluation();

      // Get the policy

      policy=result.getMaxConfiguration(new Configuration());

      // Set the policy in conf to check if this configuration
      // is already stored

      conf.putValue(decision.getName(),policy.getValue(decision));

      // Look for alternatives with similar utility

      repeated=analyzeNearAlternatives(result,policy,conf,anothersConf);

      // If there are alternatives with similar utility, store them

      if (repeated == true){
        storeNearAlternatives(solutions,anothersConf);

        // reset anothersConf for the next evaluation

        anothersConf.removeAllElements();
      }

      // Store this configuration in solutions data base

      solutions.put(conf);

      // Restore the relations to prepare a new evaluation

      quantitativeEval.setCurrentRelations(relations);
    }

    // Increase the flag evaluated

    evaluated[index]=casesToEvaluate.getNumberOfCases();

    // return the data base with the solutions

    return solutions;
  }

  /**
   * Method to classify the data obtained after a set of evaluations
   * @param index of the decision
   * @param cases CaseListMem with the cases to classify
   * @kind int value showing the kind of classification to apply
   */
  
  private void classify(int index, CaseListMem cases, int kind){
     ClassificationTree ctree;
     ProbabilityTree pTree;

     // The operation to do depends on the lind of classification to do
     
     switch(kind){
        case MAXIMAZING:
           // Get the classification tree
           
           ctree=(ClassificationTree)maximizationClassifiers.elementAt(index);
           
           // Call the method learn
          
           ctree.learn(new DataBaseCases("dbtmp",cases),0);
           break;
           
        case SAMPLING:
           // Get the classification tree
           
           ctree=(ClassificationTree)samplingClassifiers.elementAt(index);
           
           // Call the method learn
           
           ctree.learn(new DataBaseCases("dbtmp",cases),0);
           break;
           
        case ALL:
           // Get both classification trees and classify
           
           ctree=(ClassificationTree)maximizationClassifiers.elementAt(index);
           ctree.learn(new DataBaseCases("dbtmp",cases),0);

           ctree=(ClassificationTree)samplingClassifiers.elementAt(index);
           ctree.learn(new DataBaseCases("dbtmp",cases),0);
           break;
     }
  }
  
  /**
   * Method to get the results of the quantitative evaluation
   * for a given decision
   * @return potential tree with the results
   */
  
  private PotentialTree getResultsOfQuantitativeEvaluation(){
     PotentialTree result;

     // The pos is allways cero, just because we allways evaluate
     // for a decision

     result=(PotentialTree)quantitativeEval.results.elementAt(0);
     
     // return result
     
     return result;
  }
  
  /**
   * Method devoted to initialize all the data required to begin with
   * the simulation process
   */
  
  public void initSimulation(){
     // First at all, get the relations related to the influence
     // diagram, but once they are converted to potential trees and
     // once constraints are already applied. This is done with
     // the method retrieveRelations
     
     retrieveRelations();
     
     // Once this is done, we have to get the order of instantiation
     // that will be used for every decision
     
     getOrderOfInstantiation();
     printOrderOfInstantiation();
     
     // Once the order of instantiation have been analyzed, its
     // the time to get the relations related to the decisions.
     // For that we made a qualitative evaluation with the ID,
     // to get the decision tables
     
     getDecisionRelations();
     //printDecisionRelations();
     
     // Now we assign an uniform distribution for these new
     // relations. These distributions will be used to sample
     // from the decisions at the beginning
     
     assignUniformDistributionsToDecisionsRelations();
     
     // Finally, build the classifiers that will be used during
     // the evaluation of the influence diagram
     
     buildClassifiers();
  }
  
  /**
   * Private method devoted to retrieve the relations comming from
   * the influence diagram, but once constraints are applied and the
   * relations are converted into potential trees. This set of relations
   * will be used to allow several evaluations on the same influence
   * diagram
   */
  
  private void retrieveRelations(){
     RelationList rels;
     int i;
     
     // Get the relations from quantitativeEval
     
     rels=quantitativeEval.getCurrentRelations();
     
     // To impose this set of relations to the network data member of this
     // object, we have to convert into a vector
     
     relations=new Vector();
     
     // Consider every relation
     
     for(i=0; i < rels.size(); i++){
       relations.addElement(rels.elementAt(i).copy());
     }
     
     // Once the relatiosn have been copied, assign them to
     // network data member
     
     network.setRelationList(relations);
  }
  
  /**
   * Method to get the order of instantiation that will be used
   * to evaluate the ID. This procedure will get the order of
   * instantiation required for every decision in the ID
   */
  
  private void getOrderOfInstantiation(){
     Vector order;
     
     // Initially, get the order of instantiation on qualitativeEval
     // data member
     
     qualitativeEval.produceOrderOfInstantiation();
     
     // Retrieve the order, as the previous method produce
     
     order=qualitativeEval.getOrderOfInstantiation();
     
     // This vector is analyzed to determine the order of
     // instantiation for every decision
     
     analyzeOrderOfInstantiation(order);
  }
  
  /**
   * Private method to analyze the order of instantiation, as it
   * is produced in QualitativeVariableElimiantion class
   * @param order Vector produced in class QualitativeVariableElimination
   */
  
  private void analyzeOrderOfInstantiation(Vector order){
     // The analysis will be done according to the possitions of
     // the decision nodes
     
     analyzeOrderOfInstantiationForLastDecision(order);
     analyzeOrderOfInstantiationForIntermediateDecisions(order);
     analyzeOrderOfInstantiationForFirstDecision(order);
  }
  
  /**
   * Method to analyze the order of instantitation for the last decision
   * In this case, the only operation required to transform the
   * format used in QualitativeVariableElimination to store the order
   * of the variables
   * @param order order produced in QualitativeVariableElimination
   */
  
  private void analyzeOrderOfInstantiationForLastDecision(Vector order){
     Vector orderForLastDecision=new Vector();
     Vector observed=new Vector();
     Vector phaseOfElimination;
     FiniteStates decision;
     String nodeName;
     boolean pObserved;
     int i,j;
     
     // Get a reference to the decision under consideration
     
     decision=(FiniteStates)decisions.elementAt(decisions.size()-1);
     
     //Analyze the content of order
     
     for(i=0; i < order.size(); i++){
        
        // Every position in order vector will contain itself a vector
        
        phaseOfElimination=(Vector)order.elementAt(i);
        
        // Get the names for the variables eliminated during this phase
        
        for(j=0; j < phaseOfElimination.size(); j++){
           // All the positions of this vector will contain names of nodes
           // As a name is retrieved will be stored in orderForLastDecision.
           // We have to discard the last decision
           
           nodeName=(String)phaseOfElimination.elementAt(j);
           
           if (nodeName.equals(decision.getName()) == false){
             // Check if all its parents are already observed
              
             pObserved=parentsObserved(nodeName,orderForLastDecision);
             
             // If the result of the last operation is true, then
             // add the name of the node to the vector of already
             // observed variables
             
             if (pObserved == true){
                
                // Insert the node in the vector to give the
                // order of instantiation
                
                orderForLastDecision.addElement(nodeName);
             }
           }
        }
     }
     
     // Once we have finished, this vector will be stored in the data member
     // where the order of instantitation for every decision will be hold
     
     orderOfInstantiation.setElementAt(orderForLastDecision,decisions.size()-1);
  }
  
  /**
   * Method to get the order of instantiation for the intermediate decisions in
   * the influence diagram
   * @param order the vector of instantiation produced by QualitativeArcReversal
   */
  
  private void analyzeOrderOfInstantiationForIntermediateDecisions(Vector order){
     Vector orderForDecision;
     FiniteStates decision;
     int i;
     
     // This method consider all the intermediate decisions and analyze
     // the order of instantiation for every one of them
     
     for(i=decisions.size()-2; i > 0; i--){
        
        // Get a reference to the decision
        
        decision=(FiniteStates)decisions.elementAt(i);
        
        // Call the method which must analyze the order of instanitation for a given
        // decision. The method will store the names of variables in the right position
        // of orderOfInstantiation vector
        
        orderForDecision=analyzeOrderOfInstantiationForIntermediateDecision(decision,order);
        
        // This vector will be introduced in the vector orderOfInstantiation, 
        // in the position related to the decision
        
        orderOfInstantiation.setElementAt(orderForDecision, i);
     }
  }
  
  /**
   * Method to analyze an intermediate variable, to detect the right order of
   * instantiation
   * @param decision decision under analysis
   * @param order the vector with the order of instantiation, as it is produced
   *        in produceOrderOfInstantiation method (class QualitativeArcReversal)
   * @return Vector with the variables to sample for this decision
   */
  
  private Vector analyzeOrderOfInstantiationForIntermediateDecision(FiniteStates decision, Vector order){
     Vector orderForDecision=new Vector();
     Vector phaseOfElimination;
     String nodeName;
     Node node;
     int i,j;
     
     //Analyze the content of order
     
     for(i=0; i < order.size(); i++){
        
        // Every position in order vector will contain itself a vector
        
        phaseOfElimination=(Vector)order.elementAt(i);
        
        // Get the names for the variables eliminated during this phase
        
        for(j=0; j < phaseOfElimination.size(); j++){
           // All the positions of this vector will contain names of nodes
           // As a name is retrieved we have to check if the parents were
           // observed before it. If so, the node can be sample. Anyother
           // way, it will not be sampled
           
           // Get the name of the node
           
           nodeName=(String)phaseOfElimination.elementAt(j);
           
           // Discard the decision itself
           
           if (nodeName.equals(decision.getName()) == false){
          
              // Test if its parents were already observed
              
              if (parentsObserved(nodeName,orderForDecision) == true){
              
                // Include the node in orderForDecision
              
                orderForDecision.addElement(nodeName);
             }
           }
        }
     }
     
     // The vector will be returned
     
     return orderForDecision;
  }
  
  /**
   * Method to get the variables to analyze for the first decision
   * @param order: the order, as was given by QualitativeArcReversal
   *        (method produceOrderOfInstantiation)
   */
  
  private void analyzeOrderOfInstantiationForFirstDecision(Vector order){
     FiniteStates decision=(FiniteStates)decisions.elementAt(0);
     Vector orderForDecision;
     
     // We use the method developed for intermediate decisions
     
     orderForDecision=analyzeOrderOfInstantiationForIntermediateDecision(decision, order);
     
     // Store this vector ih the position 0 of orderOfInstantiation
     
     orderOfInstantiation.setElementAt(orderForDecision, 0);
  }
  
  /**
   * Method to test if the parents of the node are already observed 
   * @param nodeName Node to observe
   * @param observed the vector with the names of the variables already observed
   * @return the result of the test 
   */

  private boolean parentsObserved(String nodeName,Vector observed) {
    NodeList parents;
    Node parent;
    Node nodeConsidered=network.getNode(nodeName);
    String varName;
    boolean found=false;
    int i,j;

    // Get the parents of the node

    parents=nodeConsidered.getParentNodes();

    // Go on this list of parents. All of them should be in observed

    for(i=0; i < parents.size(); i++){
      parent=parents.elementAt(i);

      // Test for this node

      found=false;
      for(j=0; j < observed.size(); j++){
        varName=(String)observed.elementAt(j);

        if (varName.equals(parent.getName())){
          found=true;
          break;
        }
      }

      // If the node is not in conf, return false

      if (found == false)
        return false;
    }

    //Anyother way, return true

    return true;
  }
  
  /**
   * Method to detect if the parents of a given node were already
   * observed
   * @param node to test
   * @parm conf with the variables already observed
   * @return boolean with the result of the test
   */
  
  private boolean parentsObserved(Node node, Configuration conf){
     NodeList parents;
     Vector varsInConf;
     Node parent;
     Node var;
     boolean found;
     int i,j;
     
     // Get the list od node parents
     
     parents=node.getParentNodes();
     
     // Get the variables in conf (all of the parents must be 
     // in the list of vars comming from conf)
     
     varsInConf=conf.getVariables();
     
     // Consider all of them
     
     for(i=0; i < parents.size(); i++){
        // Get a parent
        
        parent=(Node)parents.elementAt(i);
        
        // Look for this parent int the vector of vars comming from
        // conf
        
        found=false;
        
        for(j=0; j < varsInConf.size(); j++){
           
           // Get a reference to the var in pos j
           
           var=(Node)varsInConf.elementAt(j);
           
           // Check if it the same as parent
           
           if (var.getName().equals(parent.getName())){
              // The parent is in conf. Break an make fount = true
              
              found=true;
              break;
           }
        }
        
        // If parent is not in conf, return false
        
        if (found == false)
            return false;
     }
     
     // Anyother way, return true
     
     return true;
  }
  
  /**
   * Debug method used to print the order of instantiation  
   * for all the decisions of the influence diagram
   */
  
  private void printOrderOfInstantiation(){
     FiniteStates decision;
     Vector order;
     String name;
     double size;
     int i,j;
     
     for(i=0; i < decisions.size(); i++){
        decision=(FiniteStates)decisions.elementAt(i);
        
        System.out.println("Orden para decision: "+decision.getName());
        
        order=(Vector)orderOfInstantiation.elementAt(i);
        
        for(j=0,size=1; j < order.size(); j++){
           name=(String)order.elementAt(j);
           System.out.println("    Variable[ " + j +"] = "+name);
        }

        System.out.println("Posibles valores: "+getPossibleValues(order));
     }
  }
  
  /**
   * Method to get the decision relations corresponding to the ID
   * to evaluate
   */
  
  private void getDecisionRelations(){
     // Get the decision relations using the decisionsRelations data
     // member
     
     decisionsRelations=qualitativeEval.getDecisionTables();
     
     // But these tables are related to the nodes in the influence
     // diagram used to make the qualitative evaluation. But both
     // influence diagrams (that and the one used for this evaluation)
     // do not have the same objects as nodes. So it is needed to
     // translate the decision tables, so the variables in the tables
     // point to the nodes of this influence diagram
     
     decisionsRelations=((IDiagram)network).translateRelations(decisionsRelations);
     
     // Now. we have to reorder this decision tables, so the decision variables
     // for which the tables are done appear as the first one. At the same
     // time, the decisions must be stored in the order showed by decisions
     // data member
     
     reorderDecisionRelations();
  }
  
  /**
   * Method to reorder the decision tables required for this influence
   * diagram, putting the decision variables at the beginning and making
   * the decision tables be stored with the order fixed at decisions
   * data member
   */
  
  private void reorderDecisionRelations(){
     Node decision;
     NodeList reorderedVars;
     NodeList vars;
     Relation rel;
     int pos;
     int i,j;
     
     RelationList finalRelations=new RelationList();
     
     // This vector will be initialized with the relations
     // in decisionsRelations. This is required to fix the
     // size of decisionRelations
     
     for(i=0; i < decisionsRelations.size(); i++){
      finalRelations.insertRelation(decisionsRelations.elementAt(i));  
     }
        
     
     // Go on the list of relations as they are retrieved
     // from the qualitative evaluation
     
     for(i=0; i < decisionsRelations.size(); i++){
        // Build a nodelist to reorder the variables
        
        reorderedVars=new NodeList();
        
        // Get one relation
        
        rel=decisionsRelations.elementAt(i);
        
        // Get the variables for this relation
        
        vars=rel.getVariables();
        
        // The last variable should be the first
        
        decision=vars.elementAt(vars.size()-1);
        reorderedVars.insertNode(decision);
        
        // Now, put the others
        
        for(j=0; j < vars.size()-1; j++){
           reorderedVars.insertNode(vars.elementAt(j));
        }
        
        // The relation will have this set of variables
        
        rel.setVariables(reorderedVars);
        
        // Finally, we have to put this relation in the final
        // list of relations, according to the order for
        // decisions
        
        pos=findIndexForDecision(decision.getName());
        finalRelations.setElementAt(rel,pos);
     }
     
     // At the end, the data member decisionRelations will point
     // to finalRelations
     
     decisionsRelations=finalRelations;
  }
  
  /**
   * Method to print the decision relations. This is method is
   * used for debugging purposes only.
   */
  
  private void printDecisionRelations(){
     Relation rel;
     int i;
     
     // Go on the whole set of relations
     
     for(i=0; i < decisionsRelations.size(); i++){
        FiniteStates decision=(FiniteStates)decisions.elementAt(i);
        decision.print();
        System.out.println("Table for decision: "+decision.getName());
        
        rel=decisionsRelations.elementAt(i);
        rel.print();
     }
  }

  /**
   * Private method to print the cases related to a given context
   * @param vector Vector of related cases 
   * @param dataBase data base with the cases
   * @param index of the case analyzed
   */

  private void printCasesInContext(Vector related, CaseListMem dataBase, int index){
    int pos;
    int i;
    
    Configuration conf=dataBase.get(index);
    System.out.println("Context of interest: ...............");
    conf.print();
    System.out.println("------------------------------------");
    System.out.println("Related: "+related.size());
    for(i=0; i < related.size(); i++){
      pos=((Integer)related.elementAt(i)).intValue();

      // Get the case

      conf=dataBase.get(pos);
      conf.print();
      System.out.println();
    }
    System.out.println("-------------------------------------");
    System.out.println();
    System.out.println();
  }
  
  /**
   * Method used to assign uniform distributions to the relations
   * stored in decisionsRelations data member
   */
  
  private void assignUniformDistributionsToDecisionsRelations(){
     Relation rel;
     NodeList vars;
     PotentialTree pt;
     ProbabilityTree probTree;
     FiniteStates decision;
     int alternatives;
     double value;
     int i;
     
     // Consider every decision relation
     
     for(i=0; i < decisionsRelations.size(); i++){
        // Get the relation
        
        rel=decisionsRelations.elementAt(i);
        
        // Get the vars of its domain
        
        vars=rel.getVariables();
        
        // Create a new potential tree for all these variables
        
        pt=new PotentialTree(vars);
        
        // Get the decision for which this table is
        
        decision=(FiniteStates)decisions.elementAt(i);
        
        // Get the number of alternatives for this decision
        
        alternatives=decision.getNumStates();
        
        // Teh value to set to the potential will be 1/alternatives
        
        value=1.0/alternatives;
        
        // Create a probabilityTree to contain the values
        
        probTree=new ProbabilityTree(value);
        
        // This probability tree is assigned to the potential
        // tree already created
        
        pt.setTree(probTree);
        
        // Finally, this potential tree is set as the quantitative
        // information of the relation
        
        rel.setValues(pt);

        // May be constraints related to this relations. Remember
        // these relations does not appear on the original influence
        // diagram
        
        ((IDiagram)network).applyConstraintsOnRelation(rel);
     }
  }
  
  /**
   * Method to build the classifiers to use during the evaluation
   * process
   */
  
  private void buildClassifiers(){
     int i;
     
     // Every decision will have the next set of classifiers:
     // a) one for the optimal policy with optimality
     // b) one for the optimal policy, taking into account all the
     //    branches
     // c) one for maximization (with C4.5)
     // d) one for sampling (with Dirichlet)
     
     for(i=0; i < decisions.size(); i++){
        // Create the classifier for optimal policy with optimality
        
        exactClassifiersWithOptimality.addElement(new ClassificationTree(
                                 ClassificationTree.C45, 
                                 ClassificationTree.NONE, 0));
        
        exactClassifiersWithoutOptimality.addElement(new ClassificationTree(
                                 ClassificationTree.C45, 
                                 ClassificationTree.NONE, 0));
        
        maximizationClassifiers.addElement(new ClassificationTree(ClassificationTree.C45,
                                 ClassificationTree.NONE, 0));

        samplingClassifiers.addElement(new ClassificationTree(ClassificationTree.DIRICHLET,
                                 ClassificationTree.NONE, 0));
     }
  }
  
  /**
   * Method to produce an exact evaluation of the influence diagram.
   * The results of the evaluation will be used to control the
   * progress of the simulation algorithm
   */
  
  private void getExactEvaluation(){
     // First at all, get the evaluation
     
     quantitativeEval.propagate();
     
     // Get the maximum and minimum value for the utility
     
     maximum=quantitativeEval.getMaximum();
     minimum=quantitativeEval.getMinimum();
     
     // Fix the thresholdOfProximity
     
     thresholdOfProximity=PERCENTAGEOFUTILITY*(maximum-minimum)/1000;
System.out.println("Maximo: "+maximum+" Minimo: "+minimum+" Umbral: "+thresholdOfProximity);
     
     // Store the results from this evaluation. The following
     // method will reorder the results, so the tables to
     // match the order fixed for decisions
     
     exactResults=copyAndSortResults(quantitativeEval.results);

     // Restore the relations of the ID, to prepare the new
     // evaluation

     quantitativeEval.setCurrentRelations(relations);
  }
  
  /**
   * Method to determine the position that a given decision
   * has in the vector called decisions
   * @param nodeName name of the decision
   * @return position of decision in decisions data member
   */
  
  private int findIndexForDecision(String nodeName){
     int j;
    
     for(j=0; j < decisions.size(); j++){
        if (nodeName.equals((((Node)decisions.elementAt(j))).getName())){
              // This is the position
              
              return j;
        }
     }
     
     // If we reach this point, the decision is not found
     
     System.out.println("Decision "+nodeName+" not found");
     System.exit(-1);
     return -1;
  }
  
  /**
   * Method to copy the results of an evaluation. At the same
   * time, the results will be ordered to match the order of
   * the decisions in this data member
   * @param results vector with the results of an exact evaluation
   * @return vector with the sorted copy of the results
   */
  
  private Vector copyAndSortResults(Vector results){
    Potential pot;
    Potential potFinal;
    Vector vars;
    Node decision;
    Vector finalResults=new Vector();
    int i;
    
    // Give to finalResults the final size
    
    finalResults.setSize(decisions.size());
    
    // Consider all the potentials stored in result
    
    for(i=0; i < results.size(); i++){
       pot=(Potential)results.elementAt(i);
       
       // Get the variables used in pot
       
       vars=pot.getVariables();
       
       // The decision which owns this potential is the last
       // variable
       
       decision=(Node)vars.elementAt(vars.size()-1);
       
       // Find the position for decision in decisions data
       // member
       
       int pos=findIndexForDecision(decision.getName());
       
       // Use this position to store the copy of pot
       
       potFinal=pot.copy();
       
       // Store the potential
       
       finalResults.setElementAt(potFinal, pos);
    }
    
    // Return the vector of potentials
    
    return finalResults;
  }
  
  /**
   * Method to determine the optimal policies. This method will be
   * called once the influence diagram have been evaluated with an
   * exact evaluation
   */
  
  private void classifyExactResultsWithOptimality(){
     // Begin with the optimal policies for branches related to
     // optimal policies
     
     classifyOptimalPolicyForFirstDecision();
     classifyOptimalPoliciesForDecisionsWithPast();
  }
  
  /**
   * Method to determine the set ot optimal policies related to the
   * first decision (temporal order) of an influence diagram
   */

  public void classifyOptimalPolicyForFirstDecision(){
    Potential result;
    FiniteStates decision;
    Configuration conf;
    Configuration auxiliar;
    Vector vectorForDecision=new Vector();
    Vector utilities=new Vector();
    Vector repetitions=new Vector();
    Vector nodes;
    CaseListMem policies;
    ClassificationTree ctree;
    PotentialTree optimalTree;
    boolean allCeros;
    boolean repeated;
    double utility;
    long cases;
    int i,j,indMax;

    // Get the potential related to the first decision

    result=(Potential)exactResults.elementAt(0);

    // Make a potential tree to store the branches related to the
    // optimal policy

    optimalTree=new PotentialTree(result.getVariables());

    // Get the decision

    decision=(FiniteStates)decisions.elementAt(0);
    
    // Build a configuration with all the variables, except
    // the decision itself

    conf=makeConfigurationExcludingVariable(result,decision.getName());

    // Make a vector with all the variables of the configuration and with
    // the decision. This vector will be used to create the caseListMem
    // We have to avoid to use the same vector of variables used for the
    // configuration

    auxiliar=conf.duplicate();
    nodes=auxiliar.getVariables();
    nodes.insertElementAt(decision,0);

    // Build the caseListMem used to store the branches related
    // to optimal policies
    
    policies=new CaseListMem(nodes);

    // Loop over the whole set of cases for this configuration

    cases=(long)FiniteStates.getSize(conf.getVariables());     
    
    for(i=0; i < cases; i++){
      // Initialize the values for optimalTree

      initializeValues(optimalTree,conf,decision,0);

      // Get the values of utility for the configuration that
      // it is considered

      getValuesForConf(decision,result,conf,utilities);

      // Test if all the values are 0. If so, it is a constrained
      // configuration and there is nothing more to do

      allCeros=checkAllCeros(utilities);
      
      if (allCeros == true){
        conf.nextConfiguration();
        continue;
      }

      // Get the max value

      indMax=findMax(utilities);

      // May be several configurations with the same value

      repeated=isRepeatedMax(utilities,indMax,repetitions);

      // Generate configurations to store all this policies. The configurations
      // stored will be used to get a classification
      
      generateAndStoreConfigurations(policies,conf,indMax,repetitions,repeated,decision);

      // Set the values in optimalTree. If nothisg is repeated, the vector
      // of repetitions is not considered

      setValues(optimalTree,conf,decision,indMax,repetitions,1.0);

      // If there are repetitions, remove all its elements

      if (repeated == true){

       repetitions.removeAllElements();
      }
      
      // Go to the next case
       
      conf.nextConfiguration();
    }

    // Finally, make the classification for this decision

    ctree=(ClassificationTree)exactClassifiersWithOptimality.elementAt(0);
    ctree.learn(new DataBaseCases("dbtmp",policies),0);
    
    // This classification is the same taking into account only optimal
    // branches and all of the branches
    
    exactClassifiersWithoutOptimality.setElementAt(ctree, 0);

    // Store the tree with the exact optimal policy, without classification

    optimalPolicy.setElementAt(optimalTree,0);
  }
  
  /**
   * Method to determine the optimal policies for decisions which have
   * another decisions as predecessors
   */
  
  private void classifyOptimalPoliciesForDecisionsWithPast(){
     int i;
     
     // we must consider all the decisions with another
     // decisions as parents
     
     for(i=1; i < decisions.size(); i++){
        // Call the method to determine the optimal policies
        // for a decision with past
        
        classifyOptimalPolicyForDecisionWithPast(i);
     }
  }
  
  /**
   * Method to determine the optimal policies for a given decision
   * with past. This method will be used to consider the branches
   * related to optimal policies, and only these branches
   * @param index of decision to analyze
   */
  
  private void classifyOptimalPolicyForDecisionWithPast(int index){
     FiniteStates decision=(FiniteStates)decisions.elementAt(index);
     Configuration conf,auxiliar;
     ClassificationTree ctree;
     PotentialTree optimalTree;
     CaseListMem policies;
     Vector nodes;
     Vector utilities=new Vector();
     Vector repetitions=new Vector();
     Potential result;
     boolean optimalInPast,allCeros,repeated;
     long cases;
     int i,indMax;
     
     // Get the potential related to decision
     
     result=(Potential)exactResults.elementAt(index);

     // Build the potential tree with the optimal policy withou classification

     optimalTree=new PotentialTree(result.getVariables());
     
     // Build a configuration for all variables except the
     // decision itself
     
     conf=makeConfigurationExcludingVariable(result,decision.getName());
     
     // Make a vector with all the variables of the configuration and with
     // the decision. This vector must avoid the use of the same variables
     // present (the same objects) in conf. That is the reason why we need
     // to create an auxiliar configuration
     
     auxiliar=conf.duplicate();
     nodes=auxiliar.getVariables();
     nodes.insertElementAt(decision,0);
     
     // Build the caseListMem to classify the optimal policies for
     // this decision
     
     policies=new CaseListMem(nodes);
     
     // Consider all the cases for the configuration containing all
     // variables except the decision
     
     cases=(long)FiniteStates.getSize(conf.getVariables());
     
     for(i=0; i < cases; i++){

        // Initialize the values of optimal tree related to this configuration

        initializeValues(optimalTree,conf,decision,0);

        // First at all, we must check if this configuration
        // is related to optimal policies taking into account
        // the optimal policies for previous decisions
        
        optimalInPast=checkIfOptimalInPast(index,conf);
        
        // If this configuration is optimal, respect to previous
        // decisions, we must proceed to get the values of utility
        // for this configuration. If not, the configuration is
        // not considered
        
        if (optimalInPast == false){
           conf.nextConfiguration();
           continue;
        }
        
        // If we reach this part, the configuration is optimal
        
        getValuesForConf(decision, result, conf, utilities);
        
        // Check if all values are 0. If so, it a constrained
        // configuration an wont be considered
        
        allCeros=checkAllCeros(utilities);
        
        if (allCeros == true){
           conf.nextConfiguration();
           continue;
        }
        
        // Now, we have to look the index related to the optimal
        // alternative
        
        indMax=findMax(utilities);
        
        // May be several configurations with the same utility
        
        repeated=isRepeatedMax(utilities, indMax,  repetitions);
        
        // Generate configurations to store the branches related to
        // optimal policies
        
        generateAndStoreConfigurations(policies,conf, indMax, repetitions, repeated, decision);

        // Set the values in optimalTree. If nothisg is repeated, the vector
        // of repetitions is not considered

        setValues(optimalTree,conf,decision,indMax,repetitions,1.0);

        // If there are repetitions, remove all its elements

        if (repeated == true)
          repetitions.removeAllElements();
        
        // Go to the next case
        
        conf.nextConfiguration();
     }
     
     // Finally, make the classification for the decision
     
     ctree=(ClassificationTree)exactClassifiersWithOptimality.elementAt(index);
     ctree.learn(new DataBaseCases("dbtmp",policies),0);

     // Store the tree with the exact policy

     optimalPolicy.setElementAt(optimalTree,index);
  }
  
  /**
   * Method to classify the policies for the decision tables, but without discarding
   * the branches not related to optimal policies
   */
  
  private void classifyExactResultsWithoutOptimality(){
     int i;
     
     // Consider all the decisions
     
     for(i=1; i < decisions.size(); i++){
        // Call the method to deal with a particular decision
        
        classifyGlobalPolicyForDecision(i);
     }
  }
  
  /**
   * Method to get the optimal policies for a given decision, but without
   * discarding the branches not related to optimal policies
   * @param index of the decision
   */
  
  private void classifyGlobalPolicyForDecision(int index){
     FiniteStates decision;
     Potential result;
     Configuration conf,auxiliar;
     ClassificationTree ctree;
     CaseListMem policies;
     Vector nodes;
     Vector values=new Vector();
     Vector repetitions=new Vector();
     boolean repeated;
     long cases;
     int i, indMax;
     
     
     // Get a reference to the decision under analysis
     
     decision=(FiniteStates)decisions.elementAt(index);
     
     // Get a reference to the results for this decision
     
     result=(Potential)exactResults.elementAt(index);
     
     // Make a configuration for all variables, except the last one
     
     conf=makeConfigurationExcludingVariable(result,decision.getName());
     
     // We have to build a caseListMem to store all the configurations and
     // to proceed to classify
     
     auxiliar=conf.duplicate();
     nodes=auxiliar.getVariables();
     nodes.insertElementAt(decision,0);
     policies=new CaseListMem(nodes);
     
     // Compute the number of cases to consider
     
     cases=(long)FiniteStates.getSize(conf.getVariables());
     
     // COnsider the cases, one by one 
     
     for(i=0; i < cases; i++){
        // Get the values for this configuration
        
        getValuesForConf(decision,result,conf, values);
        
        // Check if all of them are 0
        
        if (checkAllCeros(values) == true){
           // Avoid this configuration and go on
           
           conf.nextConfiguration();
           continue;
        }
        
        // If it is a non constrained configuration, go on
        
        indMax=findMax(values);
        
        // Get values with near to optimal alternative
        
        repeated=isRepeatedMax(values, indMax, repetitions);
        
        // Generate and store the configurations for optimal policies
        
        generateAndStoreConfigurations(policies,conf,indMax, repetitions, repeated, decision);

        // Remove the repetitions, if needed

        if (repeated == true)
          repetitions.removeAllElements();
        
        // Go to the next case
        
        conf.nextConfiguration();
     }
     
     // Finally, make the classification for the decision
     
     ctree=(ClassificationTree)exactClassifiersWithoutOptimality.elementAt(index);
     ctree.learn(new DataBaseCases("dbtmp",policies),0);
     
     // Store this classifier in the vector
     
     exactClassifiersWithoutOptimality.setElementAt(ctree,index);
  }
  
  /**
   * Auxiliar method to determine if a given configuration is optimal
   * taking into account the optimal policies for previous decisions
   * @param index the index of the decision in decisions data member
   * @param conf configuration to consider
   * @return boolean value with the result of the test
   */
  
  private boolean checkIfOptimalInPast(int index, Configuration conf){
     ProbabilityTree optimal;
     ClassificationTree ctree;
     double value;
     int i;
     
     // We must consider previous decision
     
     for(i=0; i < index; i++){
        // Get the tree with the optimal policies for the previous decision
        // considered
        
        ctree=(ClassificationTree)exactClassifiersWithOptimality.elementAt(i);
        optimal=ctree.getProbabilityTree();
        
        // Get the value of the potential related with this configuration
        
        value=optimal.getProb(conf);
        
        // The configuration is related to an optimal policy if the value
        // is not 0
        
        if (value == 0)
           return false;
     }
     
     // If this point is reached, this means the configuration is optimal
     
     return true;
  }
  
  /**
   * Method to store in a given caseList the policies related to optimal
   * policies, and represented with configurations
   * @param policies caseListMem where to store the configuration
   * @param conf the configuration under observation. For it may be
   *        several values related to the optimal policy
   * @param indMax the alternative with max utility
   * @param repetitions vector to store another alternatives near to
   *        the optimal value of utility
   * @param repeated boolean flag to show if there are (or not) several
   *        alternatives with optimal utility for this configuration
   * @param var var representing the decision
   */
  
  private void generateAndStoreConfigurations(CaseListMem policies, Configuration conf,
                                                   int indMax, Vector repetitions, boolean repeated,
                                                   FiniteStates decision){
    Configuration newConf;
    int value;
    int i;
    
    // If the flag repeated is true, this means the value of indMax is also stored
    // in the vector of repetitions
    
    if (repeated == true){
       // Generate configurations for all the indices stored in repetitions
       
       for(i=0; i < repetitions.size(); i++){
          newConf=conf.duplicate();
          
          // The value is stored in the first position of the configuration
          
          value=((Integer)repetitions.elementAt(i)).intValue();
          newConf.putValueAt(decision,value,0);
          
          // The configuration is stored in the caseListMem
          
          policies.put(newConf);
       }
    }
    else{
      // Anyother way we have to add the conf passed as argument
    
      newConf=conf.duplicate();
      newConf.putValueAt(decision,indMax,0);
      policies.put(newConf);
    }
  }
  
  /**
   * Method to get a configuration containing all the variables of
   * a potential, excluding the last one. The last variable will
   * be the decision which results are stored in the table
   * @param result Potential used to create the configuration
   * @param nodeName node to exclude from the configuration
   * @return configuration with selected variables
   */

  public Configuration makeConfigurationExcludingVariable(Potential result, String nodeName){
    Vector varsInPotential;
    Vector varsForConf=new Vector();
    Node var;
    Configuration conf;
    int i;

    // Get the variables in the potential
  
    varsInPotential=result.getVariables();

    // Make a vector with all of them, except the last one

    for(i=0; i < varsInPotential.size(); i++){
     var=(Node)varsInPotential.elementAt(i);
     if (nodeName.equals(var.getName()) == false){
       varsForConf.addElement(var);
     }
    }

    // Make the configuration for this variables

    conf=new Configuration(varsForConf);

    // Return conf
   
    return conf;
  }
  
  /**
   * Method to get all the values of a potential related to a given 
   * configuration. The method will return a vector with the values
   * for a variable that is passed as first argument.
   * @param var of interest
   * @param pot potential where to take the values from
   * @param conf configuration for all the variables
   *         in the potential, except for var
   * @param vector with the values: this will be used to return
   *        the requiered set of values
   */

  private void getValuesForConf(FiniteStates var, Potential pot,
                              Configuration conf, Vector values){
    Configuration confForVar;
    Vector vectorForVar=new Vector();
    Potential restricted;
    double value;
    int i;

    // The vector of results will have the size given by the number
    // of states of var

    values.setSize(var.getNumStates());

    // Restrict the potential to the values present in conf

    restricted=pot.restrictVariable(conf);

    // Create a configuration for var. Is needed to catch the
    // values stored in restricted

    vectorForVar.addElement(var);
    confForVar=new Configuration(vectorForVar);

    // Consider all the values for var

    for(i=0; i < var.getNumStates(); i++){
      value=restricted.getValue(confForVar);

      // This value is stored in a vector to analyze it

      values.setElementAt(new Double(value),i);

      // Go to the next value

      confForVar.nextConfiguration();
    }
  }

  /**
   * Method to get all the values of a potential related to a given 
   * configuration. The method will return a vector with the values
   * for a variable that is passed as first argument.
   * @param var of interest
   * @param pot probabilityTree where to take the values from
   * @param conf configuration for all the variables
   *         in the potential, except for var
   * @param vector with the values: this will be used to return
   *        the requiered set of values
   */

  private void getValuesForConf(FiniteStates var, ProbabilityTree pot,
                              Configuration conf, Vector values){
    Configuration confForVar;
    Vector vectorForVar=new Vector();
    ProbabilityTree restricted;
    double value;
    int i;

    // The vector of results will have the size given by the number
    // of states of var

    values.setSize(var.getNumStates());

    // Restrict the potential to the values present in conf

    restricted=pot.restrict(conf);

    // Create a configuration for var. Is needed to catch the
    // values stored in restricted

    vectorForVar.addElement(var);
    confForVar=new Configuration(vectorForVar);

    // Consider all the values for var

    for(i=0; i < var.getNumStates(); i++){
      value=restricted.getProb(confForVar);

      // This value is stored in a vector to analyze it

      values.setElementAt(new Double(value),i);

      // Go to the next value

      confForVar.nextConfiguration();
    }
  }
  
  /**
   * Method to get a value for a given node. The relation for this
   * node will be restricted to be consistent with the values observed
   * for several variables.
   * @param node node to get the value for
   * @param conf values of observed variables
   * @return value for the node
   */
  
  private int getObservedValue(Node node, Configuration conf){
     boolean pObserved;
     Relation rel;
     int value;
     int kind;
     
     // Check if all the parents of node were already observed

     pObserved=parentsObserved(node,conf);
     
     // Get the kind of node
     
     kind=node.getKindOfNode();
     
     // If the parents were not observed, return this flag
     
     if (pObserved == false)
        return PARENTSNOTOBSERVED;
     
     // Get the value for the node
     
     if (kind == Node.CHANCE)
        rel=network.getRelation(node);
     else
        rel=getRelationFromDecisionsRelations(node);
     
     // Get the value

     value=getValueFromRelation(node,rel,conf);
     
     // Return the value
     
     return value;
  }
  
  /**
   * Method to get the relation with quantitative information about
   * a decision
   * @param node decision node to consider
   * @return related relation or null
   */
  
  private Relation getRelationFromDecisionsRelations(Node decision){
     Relation rel=null;
     NodeList vars;
     Node first;
     int i;
     
     // Consider the relations stored in decisionsRelations
     
     for(i=0; i < decisionsRelations.size(); i++){
        rel=(Relation)decisionsRelations.elementAt(i);
        
        // Get the variables of this relation
        
        vars=rel.getVariables();
        
        //Look the first variable (the owner of the relation)
        
        first=(Node)vars.elementAt(0);
        
        // If first is the decision consider, this is the relation
        // to return
        
        if (first.getName().equals(decision.getName())){
           break;
        }
        else
           rel=null;
     }
     
     // If we reach this point, if rell == null, something is wrong
     
     if (rel == null){
        // Show information to debug the program
        
        System.out.println("The relation of the node "+decision.getName());
        System.out.println("was not found");
     }
     
     return rel;
  }
  
  /**
   * Method to get a value for a variable, sampling from its
   * relation
   * @param node to get the value for
   * @param relation to consider
   * @param conf with the values for variables in rel
   * @return value observed
   */
  
  private int getValueFromRelation(Node node, Relation rel, Configuration conf){
     PotentialTree potential;
     Configuration confForVar;
     Vector vars=new Vector();
     double number,summ;
     int i;
     
     // Make a configuration for the variable to observe
     
     vars.addElement(node);
     confForVar=new Configuration(vars);
     
     // Copy the potential related to rel
     
     potential=(PotentialTree)(rel.getValues()).copy();
     
     // Consider the values already observed
     
     potential=(PotentialTree)potential.restrictVariable(conf);

     // Get a random number
     
     number=random.nextDouble();
     
     // Consider the values stored in the potential, until
     // reaching the value generated
     
     for(i=0,summ=0; i < FiniteStates.getSize(vars); i++){
        confForVar.putValue((FiniteStates)node,i);
        summ+=potential.getValue(confForVar);
        
        // Check if the summ is bigger than number
        
        if (summ > number){
           return i;
        }
     }
     
     // If this point is reached, something is wrong
     
     //System.out.println("Error applying getValueFromRelation..............");
     //rel.print();
     //System.out.println("\n\nPotential after considering the configuration");
     //potential.print();
     
     // Finally, return the flag showing the situation
     
     return WRONGCONFIGURATION;
  }
  
  /**
   * Method to check if a given configuration is stored in a data
   * base of cases
   * @param cases CaseListMem where to find
   * @param conf configuration to look for
   * @return boolean with the result of the test
   */
  
  private boolean isAlreadyConsidered(CaseListMem cases, Configuration conf){
     double ocurrences=cases.getValue(conf);
     //Vector removeVars=new Vector();
     Configuration partial,stored;
     boolean compatible;
     int i;
   
     if (ocurrences == 0){
        // In this case, we must analyze the ocurrences to determine if
        // the only difference it is based on the value for the decision
        // itself
        // The variable to remove from conf is the first one
     
        //removeVars.addElement(cases.getVariable(0));

        //Make the partial configuration

        //partial=new Configuration(conf,removeVars);

        // This is the configuration to consider. We will look for
        // configurations in cases that are compatible with this

        for(i=0; i < cases.getNumberOfCases(); i++){
          stored=cases.get(i);

          // Now, we have to determine if both are compatibles

          //compatible=partial.isCompatible(stored);
          compatible=conf.isCompatible(stored);

          // if compatible is true, return true

          if (compatible == true){
            return true;
          }
        }

        // If none of the configurations already stored is compatible
        // with partial, return false
        
        return false;
     }
     else{
        return true;
     }
  }
 
  /**
  * Private method to store the measures of the comparison between
  * approx and exact results
  * @param <code>Vector</code> Vector of measures for forward stage
  * @param flag, to show if the measures come from maximizing or sampling classifiers
  */

  private void storeMeasures(Vector measures,int flag){
    Vector matchesForDecisionsWithOptim=new Vector();
    Vector matchesForDecisionsWithoutOptim=new Vector();
    Vector differencesWithOptim=new Vector();
    Vector differencesWithoutOptim=new Vector();
    Vector measuresForDec;
    int i;

    // Once we have obtained the results, print them

    for(i=0; i < measures.size(); i++){

        // Get the vector with the measures for every decision

        measuresForDec=(Vector)measures.elementAt(i);
        
        // Get the number of matches with optimality

        matchesForDecisionsWithOptim.addElement(measuresForDec.elementAt(0));

        // Get the number of matches without optimality

        matchesForDecisionsWithoutOptim.addElement(measuresForDec.elementAt(1));

        // Now the differences with optimality

        differencesWithOptim.addElement(measuresForDec.elementAt(2));

        // Now the differences with optimality

        differencesWithoutOptim.addElement(measuresForDec.elementAt(3));
    }

    // Set the differences with and without optim
   
    statistics.setDifferencesWithOptimality(differencesWithOptim,phase,flag,stage); 
    statistics.setDifferencesWithoutOptimality(differencesWithOptim,phase,flag,stage); 

    // Set the number of matches

    statistics.setMatchesWithOptimality(matchesForDecisionsWithOptim,phase,flag,stage);
    statistics.setMatchesWithoutOptimality(matchesForDecisionsWithoutOptim,phase,flag,stage);
  }

  /**
   * Method to determine the maximum value stored in a vector
   * of values
   * @param values vector with the values where to look
   */

  private int findMax(Vector values){
    int i;
    int indMax=0;
    double value;
    double max=0;

    // Consider the values stored in the vector
    
    for(i=0; i < values.size(); i++){
      value=((Double)values.elementAt(i)).doubleValue();
      if (value > max){
        max=value;
        indMax=i;
      }
    }

    // Return indMax
  
    return indMax;
  }
  
  /**
   * Method to determine the set of repeated indixes of a given vector
   * @param values vector with values
   * @param indMax index related to maximum value
   * @param results vector of indices to return the repetitions
   * @return <code>boolean</code> true if the maximum value is repeated
   */

  private boolean isRepeatedMax(Vector values, int indMax, Vector results){
    boolean coincidence=false;
    int i,j;
    double value1,value2;
    int flag=0;

    // Get the value related with max index
  
    value1=((Double)values.elementAt(indMax)).doubleValue();

    // Look this value in the whole vector, anyway
  
    for(i=0; i < values.size(); i++){
      // Do not consider max

      if (i != indMax){
         value2=((Double)values.elementAt(i)).doubleValue();

         // Compare both values

         if (Math.abs(value1-value2) < thresholdOfProximity){
           // Insert max

           if(flag == 0){
              results.addElement(new Integer(indMax));
              flag=1;
           }

           // Insert the repeated value
         
           results.addElement(new Integer(i));

           // Anyway mark coincidence as true

           coincidence=true;
         }
      }
    }

    // Return coincidence

    return coincidence;
  }

  /**
   * Method to determine the set of repeated indixes of a given vector
   * @param values vector with values
   * @param indMax index related to maximum value
   * @param results vector of indices to return the repetitions
   * @param double to set a threshold of proximity
   * @return <code>boolean</code> true if the maximum value is repeated
   */

  private boolean isRepeatedMax(Vector values, int indMax, Vector results, double threshold){
    boolean coincidence=false;
    int i,j;
    double value1,value2;
    int flag=0;

    // Get the value related with max index
  
    value1=((Double)values.elementAt(indMax)).doubleValue();

    // Look this value in the whole vector, anyway
  
    for(i=0; i < values.size(); i++){
      // Do not consider max

      if (i != indMax){
         value2=((Double)values.elementAt(i)).doubleValue();

         // Compare both values

         if (Math.abs(value1-value2) <= threshold){

           // Insert max

           if(flag == 0){
              results.addElement(new Integer(indMax));
              flag=1;
           }

           // Insert the repeated value
         
           results.addElement(new Integer(i));

           // Anyway mark coincidence as true

           coincidence=true;
         }
      }
    }

    // Return coincidence

    return coincidence;
  }
  
  /**
   * Method to test if all the values in the vector passed
   * as argument (of doubles) are 0
   * @param values vector of doubles
   * @return <code>boolean</code> result of test
   */

  private boolean checkAllCeros(Vector values){
    double value;
    int i;

    for(i=0; i < values.size(); i++){
      value=((Double)values.elementAt(i)).doubleValue();

      if (value != 0.0)
        return false;
    }

    // Return true

    return true;
  }
  
  /**
   * Method that return a vector with the nodes which names appear
   * in a vector of names
   */
  
  private Vector makeVectorOfNodesFromNames(Vector names){
     Vector vars=new Vector();
     NodeList nodes=network.getNodeList();
     Node node;
     int i;
     
     for(i=0; i < names.size(); i++){
        node=nodes.getNode((String)names.elementAt(i));
        // Insert the node in the new vector of vars
        
        vars.addElement(node);
     }
     
     // Return the vector with the nodes
     
     return vars;
  }

  /**
   * Method to convert a vector of nodes to a vector with their names
   * @param nodes Vector with the nodes
   * @return vector with the names
   */

  private Vector makeVectorOfNamesFromNodes(Vector nodes){
    Node node;
    Vector names=new Vector();
    int i;

    for(i=0; i < nodes.size(); i++){
      node=(Node)nodes.elementAt(i);
      
      // Insert the name in the vector of names
      
      names.addElement(node.getName());
    }

    // Return the vector of names

    return names;
  }

  /**
   * Method that get the cardinal of the cartesina product
   * given a set of variable names
   */
  
  private double getPossibleValues(Vector names){
     NodeList nodes=network.getNodeList();
     Node node;
     double tam=1;
     int i;
     
     for(i=0; i < names.size(); i++){
        node=nodes.getNode((String)names.elementAt(i));

        // Add the size

        tam*=((FiniteStates)node).getNumStates();
     }
     
     // Return the size
    
     return tam; 
  }
  
  /**
   * Method to print the optimal policy for the decisions of the influence
   * diagram
   */
  
  private void printOptimalPolicies(){
     ClassificationTree ctreeWithOptimality;
     ClassificationTree ctreeWithoutOptimality;
     ProbabilityTree optimal;
     ProbabilityTree complete;
     int i;
     
     // Consider all the decisions
     
     for(i=0; i < decisions.size(); i++){
        // Get the classifiers
        
        ctreeWithOptimality=(ClassificationTree)exactClassifiersWithOptimality.elementAt(i);
        ctreeWithoutOptimality=(ClassificationTree)exactClassifiersWithoutOptimality.elementAt(i);
        
        // Get the probability trees
        
        optimal=ctreeWithOptimality.getProbabilityTree();
        complete=ctreeWithoutOptimality.getProbabilityTree();
        
        // Print both of them
        
        System.out.println("Classification tree with optimality...............");
        optimal.print(2);
        System.out.println("Classification tree without optimality............");
        complete.print(2);
     }
  }

  /**
   * Method to print the policies for the decisions of the influence
   * diagram, as stated witch approx classifiers obtained with
   * C4.5
   */
  
  private void printApproxMaxPolicies(){
     ClassificationTree ctree;
     ProbabilityTree complete;
     int i;
     
     // Consider all the decisions
     
     for(i=0; i < decisions.size(); i++){
        // Get the classifiers
        
        ctree=(ClassificationTree)maximizationClassifiers.elementAt(i);
        
        // Get the probability trees
        
        complete=ctree.getProbabilityTree();
        
        // Print both of them
        
        System.out.println("C4.5 Classification tree ............");
        complete.print(2);
     }
  }

  /**
   * Method to print the policies for the decisions of the influence
   * diagram, as stated witch approx classifiers obtained with
   * Dirichlet 
   */
  
  private void printApproxSampPolicies(){
     ClassificationTree ctree;
     ProbabilityTree complete;
     int i;
     
     // Consider all the decisions
     
     for(i=0; i < decisions.size(); i++){
        // Get the classifiers
        
        ctree=(ClassificationTree)samplingClassifiers.elementAt(i);
        
        // Get the probability trees
        
        complete=ctree.getProbabilityTree();
        
        // Print both of them
        
        System.out.println("Dirichlet Classification tree ............");
        complete.print(2);
     }
  }

  /**
   * Method to print the variables which take part in a given
   * configuration
   * @param conf configuration to print
   */
  
  private void printVariablesInConfiguration(Configuration conf){
     Vector vars=conf.getVariables();
     int i;
     
     System.out.println("\n\n.............................................");
     for(i=0; i < vars.size(); i++){
        System.out.println("Var ["+i+"] = "+((Node)vars.elementAt(i)).getName());
     }
     System.out.println(".............................................");
  }
  
  /**
   * Method for debugging purposes. This method will print the values
   * stored in a vector
   * @param vector to print
   */
  
  private void printVectorOfDoubles(Vector vector){
     int i;
     double value;
     
     for(i=0; i <vector.size(); i++){
        value=((Double)vector.elementAt(i)).doubleValue();
        System.out.println("Vector["+i+"] = "+value);
     }
  }
  
   /**
   * Method for debugging purposes. This method will print the values
   * stored in a vector (of integers)
   * @param vector to print
   */
  
  private void printVectorOfIntegers(Vector vector){
     int i;
    int value;
     
     for(i=0; i <vector.size(); i++){
        value=((Integer)vector.elementAt(i)).intValue();
        System.out.println("Vector["+i+"] = "+value);
     }
  }

  /**
   * Method to analyze the potential of an evaluation, trying to determine
   * the existence of alternatives of similar utility. In this case, will
   * be added new configurations to consider that solutions
   * @param <code>PotentialTree</code> result of the evaluation
   * @param <code>Configuration</code> configuration related to the selected policy
   * @param <code>Configuration</code> configuration to replicate
   * @param <code>Vector</code> vector to return the new set of configurations
   * @return <code>boolean</code> to show if new configurations were added
   */

  private boolean analyzeNearAlternatives(PotentialTree result, Configuration max,
                    Configuration toCopy, Vector anothersConf){
    double min=0;
    double maxUtility;
    double value;
    double threshold;
    int indMax;
    FiniteStates decision=max.getVariable(0);
    Configuration newConf;
    int i,first=0;
    int index;
    Vector indixes=new Vector();
    boolean repeated=false;

    // Get the index related with the maximal configuration. The configuration
    // only contains one variable, and the index related to the maximum utility
    // will be stored in it

    indMax=max.getValue(0);

    // Get the value of utility for this configuration

    maxUtility=result.getValue(max);

    // Consider the rest of values

    for(i=0; i < decision.getNumStates(); i++){
      // Only for another values

      if (i != indMax){
         max.putValue(decision,i);
         value=result.getValue(max);

         if (first == 0){
           min=value;
           first=1;
         }
         else{
           if (value < min)
              min=value;
         }
      }
    }

    // Get values near optimal

    for(i=0; i < decision.getNumStates(); i++){
      if (i != indMax){
        max.putValue(decision,i);
        value=result.getValue(max);

        // Get the distance between this value and maxUtility

        if ((maxUtility-value) <= thresholdOfProximity){
          indixes.addElement(new Integer(i));
          repeated=true;
        }
      }
    }

    // Now, if there are repetitions, create the vector of configurations
    // and duplicate toCopy, for each index in indixes

    if (repeated == true){
      for(i=0; i < indixes.size(); i++){
        index=((Integer)indixes.elementAt(i)).intValue();
        newConf=toCopy.duplicate();

        // Insert a new variable for this conf
      
        // Set the value

        newConf.putValue(decision.getName(),index);

        // Insert it in anothersConf

        anothersConf.addElement(newConf);
      }
    }

    // Return repeated

    return repeated;
  }

  /**
   * Method to store a set of configurations in a CaseListMem. The new
   * cases will be added at the end of data base
   * @param cases Data base of cases
   * @param confs Vector with the configurations to add
   */

  private void storeNearAlternatives(CaseListMem cases, Vector confs){
    int i;
    Configuration conf;
    boolean considered;

    // Consider all the configurations in confs
    
    for(i=0; i < confs.size(); i++){
      conf=(Configuration)confs.elementAt(i);

      // This configuration will be added to cases

      cases.put(conf);
    }
  }

  /**
   * Method to store a set of configurations in the CaseListMem with
   * the cases evaluated for the last decision. The insertion of a
   * new case will consider if it is already present in the data base 
   * The new cases will be added at the end of data base
   * @param confs Vector with the configurations to add
   */

  private void storeNearAlternativesForLastDecision(Vector confs){
    int i;
    Configuration conf;
    boolean considered;

    // Consider all the configurations in confs
    
    for(i=0; i < confs.size(); i++){
      conf=(Configuration)confs.elementAt(i);

      // This configuration will be added to cases, but only if it is
      // not present already

      if (casesEvaluatedForLastDecision.getValue(conf) == 0.0)
         casesEvaluatedForLastDecision.put(conf);
    }
  }
  
  /**
   * Method to convert to decision nodes the set of chance nodes
   * related to decision variables
   * @param flag to show the kind of values to assign to the
   *        relation owned by the decision
   */
  
  private void convertToDecisionNodes(int flag){
    int i;

    // Consider all the decisions, and convert one by one

    for(i=0; i < decisions.size(); i++){
      convertToDecisionNode(i,flag);
    }
  }

  /**
   * Method to convert a chance node related to a decision variable
   * to a decision node again
   * @param index of decision
   * @param flag showing the values that will be assigned to the relation
   */
  
  private void convertToDecisionNode(int index, int flag){
    Relation rel;
    Node node;
    ClassificationTree classTree;
    ProbabilityTree pTree;
    NodeList varsInRel;
    PotentialTree result;

    // Get the node to transform to chance node

    node=decisions.elementAt(index);

    // Change kind data member

    node.setKindOfNode(Node.DECISION);

    // Get the selected classifier

    classTree=getClassifier(index,flag);

    // Get the probabilityTree of the classifier

    pTree=classTree.getProbabilityTree();

    // Get the relation related to this node

    rel=decisionsRelations.elementAt(index);

    // Get the vars in this relation

    varsInRel=rel.getVariables();

    // Make a potential on this variables

    result=new PotentialTree(varsInRel);

    // This potential tree will have the values stored in
    // pTree

    result.setTree(pTree);

    // This potential is stored in rel
    
    rel.setValues(result);

    // Set the kind of this new relation

    rel.setKind(Relation.POTENTIAL);

    // Is needed to apply the constraints on this potential?
    // Yes, because maybe a classifier (Dirichlet, for example)
    // put values not equal to cero for constrained configurations

    ((IDiagram)network).applyConstraintsOnRelation(rel);

    // Finally, delete the relations for this node, required
    // to evaluate the ID when the node was a CHANCE node

    deleteRelationsOfNode(node);
  }
  
  /**
   * Method to convert a decision node related to a decision variable
   * to a chance node
   * @param index of decision
   * @param flag showing the values that will be assigned to the relation
   */
  
  private void convertToChanceNode(int index, int flag){
    Relation rel,newRel;
    Node node;
    ClassificationTree classTree;
    ProbabilityTree pTree;
    NodeList varsInRel;
    PotentialTree result;

    // Get the node to transform to chance node

    node=decisions.elementAt(index);

    // Change kind data member

    node.setKindOfNode(Node.CHANCE);

    // Get the selected classifier

    classTree=getClassifier(index,flag);

    // Get the probabilityTree of the classifier

    pTree=classTree.getProbabilityTree();

    // Get the relation related to this node

    rel=decisionsRelations.elementAt(index);

    // Get the vars in this relation

    varsInRel=rel.getVariables();

    // Make a potential on this variables

    result=new PotentialTree(varsInRel);

    // This potential tree will have the values stored in
    // pTree

    result.setTree(pTree);

    // This potential is stored in rel
    
    rel.setValues(result);

    // Set the kind of this new relation

    rel.setKind(Relation.POTENTIAL);

    // Is needed to apply the constraints on this potential?
    // Yes, because maybe a classifier (Dirichlet, for example)
    // put values not equal to cero for constrained configurations

    ((IDiagram)network).applyConstraintsOnRelation(rel);

    // This relation must be inserted in the vector of relations
    // stored for the simulation

    newRel=rel.copy();
    relations.addElement(newRel);

    // Finally, refresh the list of currentRelations used for
    // quantitativeEval

    quantitativeEval.setCurrentRelations(relations);
  }

  /**
   * Method to get a classifier related to a decision node
   * @param index index of decision
   * @param flag kind of classifier to access
   * @return the selected classifier
   */

  private ClassificationTree getClassifier(int index, int flag){
    ClassificationTree classTree=null;

    switch(flag){
      case MAXIMAZING:
         classTree=(ClassificationTree)maximizationClassifiers.elementAt(index);
         break;
      case SAMPLING:
         classTree=(ClassificationTree)samplingClassifiers.elementAt(index);
         break;
    }

    // Return the selected classifier

    return classTree;
  }

  /**
   * Method to delete the relations where a variable is the first one
   * @param <code>Node</code> node which relations will be erased
   */

  private void deleteRelationsOfNode(Node node){
    NodeList vars;
    Relation rel;
    Node first;
    int i;

    // Go on the list relations

    for(i=0; i < relations.size(); i++){
      rel=(Relation)relations.elementAt(i);

      // Get the list of variables

      vars=rel.getVariables();

      first=vars.elementAt(0);
      if (first.getName().equals(node.getName())){
        // Remove the relation from relations

        relations.removeElement(rel);
      } 
    }

    // Restore relations for new propagation

    quantitativeEval.setCurrentRelations(relations);
  }
  
  /**
   * Method to filter a data base of cases, to delete the variables
   * that do not appear in a decision table
   * @param cases Data base of cases to filter
   * @param index of the decision owning the decision table
   * @return the data base once cases have been filtered
   */
  
  private CaseListMem filterCaseListMem(CaseListMem cases, int index){
    CaseListMem finalCases=null;
    Configuration finalConf;
    NodeList varsOfInterest;
    Relation rel;
    boolean result;
    boolean considered;
    int i;

    // Get the decision table for this decision

    rel=decisionsRelations.elementAt(index);

    // Get the variables of interest
    
    varsOfInterest=rel.getVariables();
    
    // Consider all the cases stored
    
    for(i=0; i < cases.getNumberOfCases(); i++){
      // Filter the configuration

      finalConf=new Configuration(cases.get(i),varsOfInterest);

      // The first time, build the caseListMem 

      if (i == 0){
        finalCases=new CaseListMem(finalConf.getVariables());
      }

      // Add the new configuration to finalCases

      result=finalCases.put(finalConf);
    }

    // Return the new data base
     
    return finalCases;
  }

  /**
   * Method to compute distances between the classifiers
   * @return <code>Vector</code> Vector with the distances for every decision table
   */
  private Vector computeDistances(){
    ProbabilityTree exactClassifierWithOptimality;
    ProbabilityTree exactClassifierWithoutOptimality;
    ProbabilityTree approxMaxClassifier;
    ProbabilityTree approxSamplingClassifier;
    ClassificationTree ctree;
    Potential result;
    Vector vars;
    Vector globalDistances=new Vector();
    Vector distances;
    double distanceOne,distanceTwo,distanceThree,distanceFour;
    int i;

    for(i=0; i < maximizationClassifiers.size(); i++){
      result=(Potential)(exactResults.elementAt(i));
      vars=result.getVariables();
      ctree=(ClassificationTree)(maximizationClassifiers.elementAt(i));
      approxMaxClassifier=ctree.getProbabilityTree();

      ctree=(ClassificationTree)(samplingClassifiers.elementAt(i));
      approxSamplingClassifier=ctree.getProbabilityTree();
      
      ctree=(ClassificationTree)(exactClassifiersWithOptimality.elementAt(i));
      exactClassifierWithOptimality=ctree.getProbabilityTree();

      ctree=(ClassificationTree)(exactClassifiersWithoutOptimality.elementAt(i));
      exactClassifierWithoutOptimality=ctree.getProbabilityTree();

      // Create the vector to store the measures for this decision

      distances=new Vector();

      // Compute distances

      distanceOne=exactClassifierWithOptimality.computeKullbackLeiblerDistance(approxMaxClassifier,vars);
      distances.addElement(new Double(distanceOne));
      distanceTwo=exactClassifierWithoutOptimality.computeKullbackLeiblerDistance(approxMaxClassifier,vars);
      distances.addElement(new Double(distanceTwo));
      distanceThree=exactClassifierWithOptimality.computeKullbackLeiblerDistance(approxSamplingClassifier,vars);
      distances.addElement(new Double(distanceThree));
      distanceFour=exactClassifierWithoutOptimality.computeKullbackLeiblerDistance(approxSamplingClassifier,vars);
      distances.addElement(new Double(distanceFour));

      // The vector with the distances for this decision are stored in the
      // global vector

      globalDistances.addElement(distances);
    }

    // Return the vector of global distances

    return globalDistances;
  }

  /**
   * Method to compare the policies obtained as a consequence
   * of two evaluations on the same IDiagram. One of them
   * will be the exact and the second will be the solution
   * obtained with simulation. The solution of simulation
   * will be stored as classification trees. In this method,
   * the difference is obtained as follows:
   * - Prepare a configuration with all variables but the decision
   * - Loop over all the values for this configuration
   *      -For the value of configuration, get the optimal policies
   *        derived from exact evaluation
   *      -Get the proposal given by the approx. evaluation
   *      - If there is coincidence, add 1 to matches counter
   *      - Anyyway, get the difference of utility between both
   *        proposals. Get the utility related to exact optimal
   *        policy (may be several alterntives with the same
   *        utility, or near). In this case, get the medium value
   *        for utility. Compute the difference:
   *             Uexact(exactPolicy)-Uexact(approxPolicy)
   * @param <code>resultsToCompare</code> evaluation to compare with
   * @param <code>double</code> maximum value for utility
   * @param <code>double</code>minimum value for utility
   * @return <code>Vector</code> Vector containing in every position another 
   *         vector, with the next content:
   *         a) percentage of hits for both situations (with and witohout optimality)
   *         b) difference of utility between exact and approximate evaluations
   *            for both situations
   */

  public Vector compareExpectedUtilities(Vector resultsToCompare,
                                         double maxUtil,double minUtil){
    FiniteStates decision;
    Vector resultsForDecision;
    Vector optimalPolicies=new Vector();
    Vector approxPolicies=new Vector();
    Vector utilities=new Vector();
    Vector nearOptimalAlternatives;
    Vector output=new Vector();
    Potential result;
    PotentialTree optimal;
    ProbabilityTree optimalTree, resultToCompare;
    ClassificationTree cTree;
    Configuration conf;
    long cases;
    double diffWithOptimality, diffWithoutOptimality;
    double totalUtility,optimalGlobalUtility,approxGlobalUtility;
    long branchesOfOptimalPolicy, unconstrainedBranches;
    double matchesWithOptimality;
    double matchesWithoutOptimality;
    boolean allCeros;
    int i,k;

    // To compare both policies we must see the expected
    // utility of the proposed policy for the evaluation
    // respect to the evaluation passed as an argument
   
    for(i=0; i < decisions.size(); i++){

      // Get the decision

      decision=(FiniteStates)decisions.elementAt(i);

      // Create a vector for the results of this table

      resultsForDecision=new Vector();

      // Select the exact results giving the decision table for this
      // decision

      result=(Potential)exactResults.elementAt(i);

      // Get the tree describing the optimal policy for this decision

      optimal=(PotentialTree)optimalPolicy.elementAt(i);
      optimalTree=optimal.getTree();

      // Select the classifier we are interested
      // with this
  
      cTree=(ClassificationTree)resultsToCompare.elementAt(i); 
      resultToCompare=cTree.getProbabilityTree();

      // Make a configuration over the whole set of
      // variables except the last one. The last one is related
      // with the decision variable and it must be kept appart
      // to loop over it

      conf=makeConfigurationExcludingVariable(result,decision.getName());

      // Compute the number of states from the variables
      // belonging to conf

      cases=(long)FiniteStates.getSize(conf.getVariables());   

      // Initialize the measures

      diffWithOptimality=0;
      diffWithoutOptimality=0;
      branchesOfOptimalPolicy=0;
      unconstrainedBranches=0;
      matchesWithOptimality=0;
      matchesWithoutOptimality=0;

      // Consider all the cases

      for(k=0; k < cases; k++){

        // Get the utilities for this configuration. Maybe several
        // alternatives with the same utility: in this case it is
        // returned its medium value

        getValuesForConf(decision,result,conf,utilities);

        // Test if all values are 0. In this case, do no consider the
        // configuration: it is under a constraint

	     allCeros=checkAllCeros(utilities);

        // If all ceros is true, the configuration should not be
        // considered at all, just because it is a constrained configuration
     
        if (allCeros == true){
          conf.nextConfiguration();
          continue;
        }

        // Anyother way, add 1 to unconstrainedBranches

        unconstrainedBranches++;

        // Get the vector with the optimal policy related to this configuration.
        // It will be returned a vector with 0's and 1's

        getValuesForConf(decision,optimalTree,conf,optimalPolicies);     

        // Get the values given by the approx solution. It will be probabilty
        // values

        getValuesForConf(decision,resultToCompare,conf,approxPolicies);

        // Get the total value for the optimal alternative, as given
	// by the exact evaluation and without taking into account
	// optimal branches
	
        totalUtility=getGlobalUtility(utilities);

        // Get the value of utility related to the optimal alternatives

        optimalGlobalUtility=getGlobalUtility(optimalPolicies,utilities,ABSOLUTE);

        // Get the value of utility for the approx solution

        approxGlobalUtility=getGlobalUtility(approxPolicies,utilities,RELATIVE);
        
        // The measures to get will depend on considering only optimal branches
        // or not

        // Begin with branches related to optimal policy
        
        if (checkAllCeros(optimalPolicies) != true){
          // This is a configuration related to the optimal policy
          // and must be considered

          // Add 1 to branchesOfOptimalPolicy

          branchesOfOptimalPolicy++;

          // Look for a match between the optimal policies recommended following
          // exact and approx evaluation

          if (lookForMatch(optimalPolicies,ABSOLUTE,approxPolicies,ABSOLUTE)){
            matchesWithOptimality++;
          }
          else{
            // Compute the difference between globalOptimalUtility and globalApproxUtility
  
            diffWithOptimality+=Math.pow((optimalGlobalUtility-approxGlobalUtility),2);
          }
        }
        else{
          // It is a branch not related to the optimal policy, but anyway, look
          // for a match: only if there vector with proposals from approx
	        // evaluations is not full of 0's

	        if (checkAllCeros(approxPolicies) != true){
            if (lookForMatch(utilities,RELATIVE,approxPolicies,ABSOLUTE)){
              matchesWithoutOptimality++;
            }
            else{
              // Compute difference between globalOptimalUtility and globalApproxUtility
  
              diffWithoutOptimality+=Math.pow((totalUtility-approxGlobalUtility),2);
            }
	        }
        }

        // Go to the next configuration

        conf.nextConfiguration();
      }

      // The number of global matches will have to increase the number of
      // matches for optimal branches

      matchesWithoutOptimality+=matchesWithOptimality;

      // The same for the difference

      diffWithoutOptimality+=diffWithOptimality;

      // Compute the number of matches respect to the number of cases

      matchesWithOptimality=(matchesWithOptimality*100)/branchesOfOptimalPolicy;
      matchesWithoutOptimality=(matchesWithoutOptimality*100)/unconstrainedBranches;

      // Store this value

      resultsForDecision.addElement(new Double(matchesWithOptimality));
      resultsForDecision.addElement(new Double(matchesWithoutOptimality));
   
      // Once finished, divide the differences by the number of cases

      diffWithOptimality=diffWithOptimality/branchesOfOptimalPolicy;
      diffWithoutOptimality=diffWithoutOptimality/unconstrainedBranches;

      // Get square root, as the global diff

      diffWithOptimality=Math.sqrt(diffWithOptimality);
      diffWithoutOptimality=Math.sqrt(diffWithoutOptimality);

      // Store the difference

      resultsForDecision.addElement(new Double(diffWithOptimality));
      resultsForDecision.addElement(new Double(diffWithoutOptimality));

      // This vector of results for this decision will be packed into
      // output vector

      output.addElement(resultsForDecision); 
    }

    return(output);
   }

  /**
   * Private method to compute the medium value for utility, given
   * the vector with the utilities from exact evaluation. This method
   * does not consider optimal policies, but all the branches 
   * @param vector with utilities
   * @return value for utility 
   */

  private double getGlobalUtility(Vector utilities){
    boolean repeated;
    int indexOfMaxValue;
    Vector repetitions=new Vector();
    
    // Find the optimal alternative
    
    indexOfMaxValue=findMax(utilities);

    // Look for repetitions

    repeated=isRepeatedMax(utilities,indexOfMaxValue,repetitions,thresholdOfProximity);

    // Call the method to compute the global utility

    return getGlobalUtility(repeated,indexOfMaxValue,repetitions,utilities);
  }

  /**
   * Private method to compute the medium value for utility, given
   * two vector: one for alternatives and another for utilites. The
   * flag is used to show if the alternatives already represent
   * optimal alternatives or not. In the first case, all the non cero
   * values will be used. For the second case, first it is needed
   * to get the value with max probability, and nearest, and after
   * that the utility will be recovered
   * @param vector with alternatives
   * @param vector with utilities
   * @param flag to show the way to compute
   */

  private double getGlobalUtility(Vector policies, Vector utilities, int flag){
    boolean repeated;
    int indexOfMaxValue;
    Vector repetitions=new Vector();
    
    if (flag == ABSOLUTE){
      // All non cero values in policies determine optimal alternatives
      
      return getGlobalUtility(policies,utilities);
    }
    else{
      // Now all the values in policies determine optimal policies. Will be
      // probability values and we must get the biggest and that nearest from
      // it

      indexOfMaxValue=findMax(policies);

      // Look for repetitions

      repeated=isRepeatedMax(policies,indexOfMaxValue,repetitions,0.01);

      // Call the method to compute the global utility

      return getGlobalUtility(repeated,indexOfMaxValue,repetitions,policies,utilities);
    }
  }
  
  /**
   * Private method to get the medium value for utility, given
   * two vectors: one containing optimal policies for a configuration
   * and another containing the utilites related to them. The optimal
   * policies come from a classification tree for the exact results
   * @param <code>Vector</code> vector with policies for a configuration
   * @param <code>Vector</code> vector with utilities for a configuration
   * @return <code>double</code> the medium value for utility
   */

  private double getGlobalUtility(Vector policies, Vector utilities){
     int i;
     double optimal;
     int numberOfOptimalAlternatives=0;
     double globalUtility=0;
  
     // Consider all positions in policies with 1 as value
  
     for(i=0; i < policies.size(); i++){
       optimal=((Double)(policies.elementAt(i))).doubleValue();

       if (optimal != 0){
         numberOfOptimalAlternatives++;
         globalUtility+=((Double)(utilities.elementAt(i))).doubleValue()*optimal;
       }
     }

     // Return this value

     if (numberOfOptimalAlternatives != 0)
        globalUtility=globalUtility/numberOfOptimalAlternatives;
     
     // Return globalUtility

     return globalUtility;
  }

  /**
   * Method to get the global utility related to several alternatives
   * @param <code>boolean</code> flag showing the presence of repetitions
   *                             in the alternatives 
   * @param <code>int</code> value of index related to the maximal utility
   *                         alternative
   * @param <code>Vector</code> Vector of repetitions
   * @param <code>Vector</code> Vector with the approx policy for the conf
   * @param <code>Vector</code> Vector with values of utility exact utility
   * @return <code>double</code> Value of utility related to optiml alternatives
   */

  private double getGlobalUtility(boolean repeated, int indMax, Vector repetitions,
                                Vector policies, Vector utilities){
    int index;
    int i;
    double globalUtility=0;
    double policyProb;
    long branches=1;
 
    // Get the prob related to optimal policy

    policyProb=((Double)policies.elementAt(indMax)).doubleValue();
    globalUtility+=((Double)(utilities.elementAt(indMax))).doubleValue()*policyProb;

    // If there are not repetitions, return the utility for the optimal policy

    if (repeated == false){
      return globalUtility; 
    }

    // Any other case, consider all the alternatives

    for(i=0; i < repetitions.size(); i++,branches++){
      index=((Integer)(repetitions.elementAt(i))).intValue();
      policyProb=((Double)policies.elementAt(index)).doubleValue();
      //globalUtility+=((Double)(utilities.elementAt(index))).doubleValue()*policyProb;
      globalUtility+=((Double)(utilities.elementAt(index))).doubleValue();
    }

    // Return this value

    return globalUtility/branches;
  }

  /**
   * Method to get the global utility related to several alternatives
   * @param <code>boolean</code> flag showing the presence of repetitions
   *                             in the alternatives 
   * @param <code>int</code> value of index related to the maximal utility
   *                         alternative
   * @param <code>Vector</code> Vector of repetitions
   * @param <code>Vector</code> Vector with values of exact utility
   * @return <code>double</code> Value of utility related to optiml alternatives
   */

  private double getGlobalUtility(boolean repeated, int indMax, Vector repetitions,
                                Vector utilities){
    int index;
    int i;
    double globalUtility=0;
    long branches=1;
 
    // If there are not repetitions, return the utility for the optimal policy

    globalUtility+=((Double)(utilities.elementAt(indMax))).doubleValue();

    if (repeated == false){
      return globalUtility;
    }

    // Any other case, consider all the alternatives

    for(i=0; i < repetitions.size(); i++,branches++){
      index=((Integer)(repetitions.elementAt(i))).intValue();
      globalUtility+=((Double)(utilities.elementAt(index))).doubleValue();
    }

    // Return this value

    return globalUtility/branches;
  }

  /**
   * Private method to detect matches between two vectors
   * @param max1 index related to maximum value for vector1
   * @param rep1 first vector of indices
   * @param max2 index related to maximum value for vector2
   * @param rep2 second vector of indices
   * @return boolean the result of this operation
   */
  private boolean lookForMatch(int max1, Vector rep1, int max2, Vector rep2){
    int i;
    int index;
    
    // If there are not repetitions, maxExact and maxApprox are compared

    if (max1 == max2){
      return true;
    }

    // Anyother way, compare them

    if (rep1.size() == 0){
      if (rep2.size() == 0){
        // return false. The only repetitions is between maxExact and maxApprox
        // and that is not true

        return false;
      }

      // Only one exact alternative and several for approx solution

      return findMatchInVector(max1,rep2);
    }
    else{
      if (rep2.size() == 0){
        // The maxApprox must be in repExact

        return findMatchInVector(max2,rep1);
      }
      else{
        rep1.insertElementAt(new Integer(max1),0);
        rep2.insertElementAt(new Integer(max2),0);
        
        return findMatchInVectors(rep1,rep2);
      }
    }
  }

  /**
   * Method to get for a match between two vector of int values
   * @param ref1 first vector on double values
   * @param flag1 to show the kind of threshold for proximity
   *        for ref1
   * @param ref2 second vector of double values
   * @param flag2 to show the kind of threshold for proximity
   *        for ref2
   * @return result of the comparison
   */

  private boolean lookForMatch(Vector ref1, int flag1,Vector ref2,int flag2){
    boolean coincidence=false;
    int max1,max2;
    int i,j=0;
    boolean repeated1,repeated2;
    Vector repetitions1=new Vector();
    Vector repetitions2=new Vector();
    double threshold;
    double threshold1,threshold2;

    // First at all, look for the max in both vector
    
    max1=findMax(ref1);
    max2=findMax(ref2);

    // If both of them are the same, return true

    if (max1 == max2)
      return true;

    // Set the threshold, depending on flag

    if (flag1 == ABSOLUTE){
      threshold1=0.01;
    }
    else{
      threshold1=thresholdOfProximity;
    }
    
    if (flag2 == ABSOLUTE){
      threshold2=0.01;
    }
    else{
      threshold2=thresholdOfProximity;
    }

    // Look for repetitions

    repeated1=isRepeatedMax(ref1,max1,repetitions1,threshold1);
    repeated2=isRepeatedMax(ref2,max2,repetitions2,threshold2);

    // Consider repetitions 
   
    coincidence=lookForMatch(max1,repetitions1,max2,repetitions2);

    return coincidence;
}

  /**
   * Auxiliar method to find a value in a a vector
   * @param value to find
   * @param vector where to find
   * @return boolean result
   */

  private boolean findMatchInVector(int value, Vector vector){
    int i;
    int valInVector;

    for(i=0; i < vector.size(); i++){
      valInVector=((Integer)vector.elementAt(i)).intValue();

      if (valInVector == value)
        return true;
    }

    return false;
  }

  /**
   * Auxiliar method to detect if there are non empty intersection
   * between to vectors
   * @param vector1 first vector to consider
   * @param vector2 second vector to consider
   * @return boolean result for the operation
   */

  private boolean findMatchInVectors(Vector vector1, Vector vector2){
    int i;
    int valInVector1;
    boolean result;

    for(i=0; i < vector1.size(); i++){
      valInVector1=((Integer)vector1.elementAt(i)).intValue();

      // Look a match for this value in vector2

      result=findMatchInVector(valInVector1,vector2);

      if (result == true)
        return true;
    }

    return false;
  }

  /**
   * Auxiliar method to find the position a variable has in a vector
   * of nodes
   * @param vars vector of nodes
   * @param name of the variable to look for
   * @return index of the variable: -1 if it is not present
   */

  private int findIndexOfVar(Vector vars, String name){
    FiniteStates node;
    int i;

    for(i=0; i < vars.size(); i++){
      node=(FiniteStates)vars.elementAt(i);

      // Compare the names

      if (name.equals(node.getName()))
        return i;
    }

    // If this point is reached, the variable was not found

    return -1;
  }
  
  /**
   * Auxiliar methos to count the non cero values stored in a
   * probability tree
   * @param pTree probability tree to analyze
   * @return number of non cero values
   */
  
  private long countNonCeroValues(ProbabilityTree pTree){
     NodeList vars;
     Configuration conf;
     double value;
     long cases;
     long nonCeroValues=0;
     long i;
     
     // Get the variables of the probability tree
     
     vars=pTree.getVarList();
     
     // Make a configuration with these variables
     
     conf=new Configuration(vars);
     
     // Compute the number of cases to consider
     
     cases=(long)FiniteStates.getSize(conf.getVariables());
     
     // Consider all the cases
     
     for(i=0; i < cases; i++){
        // Get the value for this configuration
        
        value=pTree.getProb(conf);
        
        // If the value is non cero, add one to nonCeroValues
        
        if (value != 0.0)
           nonCeroValues++;
        
        // Get to the next configuration
        
        conf.nextConfiguration();
     }
     
     // Return nonceroValues
     
     return nonCeroValues;
  }

  /**
   * Method to print the data bases of cases for every decision
   */

  private void printDataBasesOfCases(){
    CaseListMem dataBase;
    int i;

    for(i=0; i < decisions.size(); i++){
      System.out.println("Data base for decision at "+i);
      System.out.println("-------------------------------------------");

      // Get the data base
      
      dataBase=(CaseListMem)cases.elementAt(i);

      // Print the cases

      if (dataBase != null)
         dataBase.print();
       else
         System.out.println("No hay BBDD para posicion "+i);

      System.out.println("-------------------------------------------");
    }
  }

  /**
   * Method to set values in all the positions of a potential
   * under configurations related to a reference configuration
   * passed as argument
   * @param result potential to use
   * @param conf configuration to consider
   * @param var for all the values of this
   *        var will be stored a value given by the next parameter
   * @param value to store
   */

  private void initializeValues(Potential result, Configuration conf,
                              FiniteStates var, double value){
    Vector allVars;
    Configuration completeConf;
    int i;

    // Create a configuration with all the variables

    allVars=result.getVariables();

    // Create a configuration

    completeConf=new Configuration(allVars);

    // Set the values for the variables in conf

    completeConf.resetConfiguration(conf);

    // Consider the whole set of values for var

    for(i=0; i < var.getNumStates(); i++){
      // Set the value for var in completeConf
      completeConf.putValue(var,i);

      //Set value in this position

      result.setValue(completeConf,value);
    }
  }
  
  /**
   * Method to set values in several positions of a potential
   * under configurations related to a reference configuration
   * passed as argument
   * @param result potential to use
   * @param conf configuration to consider
   * @param var for the values of var stored 
   *        in the vector passed as next argument, a 1 will be added 
   * @param index maxIndex where to fix value
   * @param indices Vector of indices where to add value too
   * @param value value to set
   */

  private void setValues(Potential result, Configuration conf,
                              FiniteStates var, int maxIndex, Vector indices, double value){
    Vector allVars;
    Configuration completeConf;
    int index;
    int i;

    // Create a configuration with all the variables

    allVars=result.getVariables();

    // Create a configuration

    completeConf=new Configuration(allVars);

    // Set the values for the variables in conf

    completeConf.resetConfiguration(conf);

    // Consider the whole set of indices stored in the vector, but only
    // if it is required

    for(i=0; i < indices.size(); i++){

      index=((Integer)(indices.elementAt(i))).intValue();

      // Set the value for var in completeConf
      completeConf.putValue(var,index);

      //Set value in this position

      result.setValue(completeConf,value);
    }

    // Work now with the position given by maxIndex

    completeConf.putValue(var,maxIndex);

    // Set the value

    result.setValue(completeConf,value);
  }

  /**
   * Main method used to test the prgram. The command line arguments
   * required for this class are explained below:
   * <ol>
   *   <li> integer: number of phases to use
   *   <li> integer: number of samples to generate in every phase
   *   <li> String: input file
   *   <li> String: ouput file
   */
  
  public static void main (String args[]) throws ParseException, IOException {
    IDiagram diag; 
    String base;
    SamplingOnId sampling;
    int i;

    // Control the aqrguments used to execute this class
    
    if (args.length < 4){
      System.out.println("Too few arguments. Arguments are: ");
      System.out.println("  Phases ");
      System.out.println("  SamplesNumber ");
      System.out.println("  ElviraFile ");
      System.out.println("  OutputFile");
    }
    else {
      
      // Arguments used OK
       
      // Build the ID
       
      diag=(IDiagram)Network.read(args[2]);
      
      // Build the object of this class
      
      sampling = new SamplingOnId(diag,args[0],args[1]);

      // Compose the name of the file to store the statistics

      base=args[2].substring(0,args[2].lastIndexOf('.'));
      base=base.concat("_sampling_data");

      // Set the file name to store the results of the simulation

      sampling.statistics.setFileName(base);

      // Print the set of decisions
    
      for(i=0; i < sampling.decisions.size(); i++)
          System.out.println("Decision [ "+i+" ] = "+sampling.decisions.elementAt(i).getName());

      // Proceed to propagate
    
      sampling.propagate();

      // Print the information

      //try{
      //  sampling.statistics.printInformation();
      //}catch(IOException e){};
    }
  }// end of main
  
}
