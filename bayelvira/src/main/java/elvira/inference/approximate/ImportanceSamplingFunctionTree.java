package elvira.inference.approximate;

import java.util.Hashtable;
import java.util.Random;
import java.util.Date;
import java.util.Vector;
import elvira.*;
import elvira.potential.*;
import elvira.parser.ParseException;
import java.io.*;


/**
 * Class ImportanceSamplingFunctionTree.
 * Implements the importance sampling method of propagation
 * based on approximate node deletion. Using Probability Trees.
 *
 * Last modified: 27/09/99
 */

public class ImportanceSamplingFunctionTree extends ImportanceSampling {

double limitForPrunning; // Limit of information for prunning.


/**
 * Program for performing experiments.
 * The arguments are as follows.
 * 1. A double; limit for prunning.
 * 2. An integer. Maximum size of a potential.
 * 3. An integer. Number of simulation steps.
 * 4. An integer. Number of experiments.
 * 5. Input file: the network.
 * 6. Output error file, where the error and computing time
 *    of each experiment will be stored.
 *
 * 7. File with instantiations.
 * The last argument can be omitted. In that case, it will
 * be considered that no observations are present.
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  ImportanceSamplingFunctionTree propagation;
  int i, ss, nruns, ls;
  double lp;

  if (args.length<6)
    System.out.println("Too few arguments");
  else {
    networkFile = new FileInputStream(args[4]);
    b = new Bnet(networkFile);

    if (args.length==7) {
      evidenceFile= new FileInputStream(args[6]);
      e = new Evidence(evidenceFile,b.getNodeList());
    }
    else
      e = new Evidence();

    lp = (Double.valueOf(args[0])).doubleValue();

    ls = (Integer.valueOf(args[1])).intValue();

    ss = (Integer.valueOf(args[2])).intValue();

    nruns = (Integer.valueOf(args[3])).intValue();

    propagation = new ImportanceSamplingFunctionTree(b,e,lp,ls,ss,nruns);

    propagation.propagate( args[5]);
  }
}


/**
 * Creates an empty object. Necessary for subclass definition.
 */

ImportanceSamplingFunctionTree() {

}

/**
 * Creates a new propagation with for a given evidence and
 * a given network.
 * @param b a belief netowrk.
 * @param e an evidence.
 */

ImportanceSamplingFunctionTree(Bnet b, Evidence e) {

  observations = e;
  network = b;
}


/**
 * Creates a new propagation.
 * @param b a belief netowrk.
 * @param e an evidence.
 * @param lp the limit for prunning.
 * @param ls the maximum size for potentials.
 * @param ss the sample size.
 * @param nruns the number of runs.
 */

public ImportanceSamplingFunctionTree(Bnet b, Evidence e, double lp, int ls,
		       int ss, int nruns) {

  observations = e;
  network = b;
  setLimitSize(ls);
  setLimitForPrunning(lp);
  setSampleSize(ss);
  setNumberOfRuns(nruns);
}


/**
 * Sets the limit for prunning.
 * @param epsilon the deviation respect to a uniform distribution.
 */

public void setLimitForPrunning(double epsilon) {

  limitForPrunning = 1+((0.5-epsilon) * Math.log(0.5-epsilon) /
			Math.log(2) +
			(0.5+epsilon) * Math.log(0.5+epsilon)/
			Math.log(2));
}


/**
 * @return the initial relations present
 * in the network.
 */

public RelationList getInitialRelations() {

  Relation rel, newRel;
  RelationList list;
  int i;
  PotentialFunction pf;
  Function f;

  list = new RelationList();

  for (i=0 ; i<network.getRelationList().size() ; i++) {
    rel = (Relation)network.getRelationList().elementAt(i);
    if (rel.getActive()!=false) {
      newRel = new Relation();
      newRel.setVariables(rel.getVariables().copy());

      if (rel.getValues().getClass().getName().equals("PotentialTable")) {
        newRel.setValues(((PotentialTable)rel.getValues()).toTree());
      }
      else if (rel.getValues().getClass().getName().equals("CanonicalPotential")) {
	newRel.setValues(((CanonicalPotential)rel.getValues()).toTree());
      }
      else {
	if (rel.getValues().getClass().getName().equals("PotentialFunction")){
       	   f = ((PotentialFunction)rel.getValues()).getFunction();
       	   if (f.getClass().getName().equals("FunctionSumNormIdf")){
       	     pf = ((FunctionSumNormIdf)f).sumToAddNormIdf((PotentialFunction)rel.getValues());
             rel.setValues(pf);
           }
	}
        newRel.setValues(rel.getValues());
      }
      list.insertRelation(newRel);
    }
  }

  return list;
}


/**
 * Compute the sampling distributions.
 * There will be a sampling distribution for each
 * variable of interest.
 * The deletion sequence will be stored in the list
 * DeletionSequence. For each variable in that list,
 * its sampling distribution will be at the same position in
 * list SamplingDistributions.
 *
 * Note that observed variables are not included in the deletion
 * sequence, since they need not be simulated.
 */

public void getSamplingDistributions() {

  NodeList notRemoved;
  Node variableX;
  FiniteStates variableY;
  Relation rel;
  RelationList currentRelations, tempList;
  Potential pot;
  PairTable pairTable;
  int i, j, ji, k, p, pos, s;


  notRemoved = new NodeList();
  pairTable = new PairTable();
  positions = new Hashtable(20);

  deletionSequence = new NodeList();
  samplingDistributions = new Vector();

  s = network.getNodeList().size();

  for (i=0 ; i<s ; i++) {
    variableX = (FiniteStates)network.getNodeList().elementAt(i);

    if (!observations.isObserved(variableX)) {
      notRemoved.insertNode(variableX);
      pairTable.addElement(variableX);
    }
  }

  currentRelations = getInitialRelations();


  /* Now restrict the valuations to the obervations */

  if (observations.size() > 0)
    restrictToObservations(currentRelations);

  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));


  for (i=notRemoved.size() ; i>0 ; i--) {
    variableX = pairTable.nextToRemove();
    positions.put(variableX,new Integer(i-1));

    notRemoved.removeNode(variableX);
    pairTable.removeVariable(variableX);
    deletionSequence.insertNode(variableX);
    tempList = currentRelations.getRelationsOfAndRemove(variableX);

    rel = tempList.elementAt(0);
    pairTable.removeRelation(rel);
    pot = rel.getValues();

    for (j=1 ; j<tempList.size() ; j++) {
      rel = tempList.elementAt(j);
      pairTable.removeRelation(rel);

      System.out.print("\nCombining...P1 * P2\n");
	 for (ji=0 ; ji<rel.getVariables().size() ; ji++)
    System.out.print(((FiniteStates)rel.getValues().getVariables().elementAt(ji)).getName()+" ");
    System.out.print(" and ");
    for (ji=0 ; ji<pot.getVariables().size() ; ji++)
    System.out.print(((FiniteStates)pot.getVariables().elementAt(ji)).getName()+" ");


      pot = pot.combine(rel.getValues());

      System.out.print("\n Resulting  ");
    for (ji=0 ; ji<pot.getVariables().size() ; ji++)
    System.out.print(((FiniteStates)pot.getVariables().elementAt(ji)).getName()+" ");
    }

    samplingDistributions.addElement(pot);

    if (i>1) {
      pot = pot.addVariable(variableX);
      ((PotentialTree)pot).limitBound(limitForPrunning);
    }

    pot = ((PotentialTree)pot).sortAndBound(limitSize);

    for (k=pot.getVariables().size()-1 ; k>=0 ; k--) {
      variableY = (FiniteStates)pot.getVariables().elementAt(k);

      if (!((PotentialTree)pot).getTree().isIn(variableY)) {
	if (currentRelations.isIn(variableY)) {
	  pos = pot.getVariables().indexOf(variableY);
	  pot.getVariables().removeElementAt(pos);
	}
      }
    }

    rel = new Relation();
    //rel.setKind("potential");
    rel.setKind(Relation.POTENTIAL);
    rel.getVariables().setNodes ((Vector)pot.getVariables().clone());
    rel.setValues(pot);
    currentRelations.insertRelation(rel);
    pairTable.addRelation(rel);
  }
}


/**
 * Simulates a configuration.
 * @param generator a random number generator.
 * @return true if the simulation was ok.
 */

public boolean simulateConfiguration(Random generator) {

  FiniteStates variableX;
  Potential pot;
  int i, s, v;
  boolean ok = true;

  s = samplingDistributions.size()-1;

  for (i=s ; i>=0 ; i--) {
    variableX = (FiniteStates)deletionSequence.elementAt(i);
    pot = (Potential) samplingDistributions.elementAt(i);

    v = simulateValue(variableX,s-i,pot,generator);

    if (v==-1) { // Zero
      ok = false;
      break;
    }
    currentConf[s-i] = v;
  }

  return ok;
}


/**
 * Simulates a value for a variable.
 * @param variableX a FiniteStates variable to be generated.
 * @param pos the position of variableX in the current conf.
 * @param pot the sampling distribution of variableX.
 * @param generator a random number generator.
 * @return the value simulated. -1 if the valuation is 0.
 */

public int simulateValue(FiniteStates variableX, int pos,
			 Potential pot, Random generator) {

  int i, nv, v = -1;
  double checksum = 0.0, r, cum=0.0;
  double [] values;


  nv = variableX.getNumStates();
  values = new double[nv];

  for (i=0 ; i<nv ; i++) {
    currentConf[pos] = i;
    values[i] = pot.getValue(positions,currentConf);
    checksum += values[i];
  }

  if (checksum == 0.0) {
    System.out.println("Zero valuation");
    return -1;
  }

  r = generator.nextDouble();

  for (i=0 ; i<nv ; i++) {
    cum += (values[i] / checksum);
    if (r <= cum) {
      v = i;
      break;
    }
  }

  currentWeight /= (values[v] / checksum);
  return v;
}


/**
 * Simulates a sample of size sampleSize and updates the weights
 * in simulationInformation.
 */

public void simulate() {

  int i;
  double w;
  boolean ok;
  Random generator = new Random();


  currentConf = new int[network.getNodeList().size()];

  for (i=0 ; i<sampleSize ; i++) {
    currentWeight = 1.0;

    ok = simulateConfiguration(generator);

    if (ok) {
      w = evaluate();
      currentWeight *= w;
      updateSimulationInformation();
    }
  }
}


/**
 * Computes the probability of obtaining a configuration
 * according to the original conditional distributions in
 * the network.
 * @param conf a configuration to evaluate.
 * @return the probability of conf.
 */

public double evaluate(Configuration conf) {

  int i, s;
  double value = 1.0;
  Relation rel;
  Potential pot;

  s = initialRelations.size();

  for (i=0 ; i<s ; i++) {
    rel = (Relation)initialRelations.elementAt(i);
    pot = rel.getValues();
    value *= pot.getValue(conf);
  }

  return value;
}



/**
 * Computes the probability of obtaining configuration currentConf
 * according to the original conditional distributions in
 * the network.
 * @return the probability of currentConf.
 */

public double evaluate() {

  int i, s;
  double value = 1.0;
  Relation rel;
  Potential pot;

  s = initialRelations.size();

  for (i=0 ; i<s ; i++) {
    rel = (Relation)initialRelations.elementAt(i);
    pot = rel.getValues();

    // If size == 0 it means that the potential corresponds to
    // a single variable and that variable is observed.
    // So, do not evaluate it.

    if (pot.getVariables().size() > 0)
      value *= pot.getValue(positions,currentConf);
  }

  return value;
}


/**
 * Restricts a list of relations to the observations.
 * @param list the list of relations to restrict.
 */

public void restrictToObservations(RelationList list) {

  Relation rel;
  int i, s;

  s = list.size();

  for (i=0 ; i<s ; i++) {
    rel = list.elementAt(i);
    rel.setValues(rel.getValues().restrictVariable(observations));
    rel.getVariables().setNodes (rel.getValues().getVariables());
  }
}


/**
 * Carries out a propagation. At the end of the propagation,
 * SimulationInformation will contain the result.
 *
 * @param exactFile the name of the file with the exact results.
 * @param resultFile the name of the file where the errors will
 *        be stored.
 */

public void propagate(String resultFile) throws ParseException, IOException {


  double[] errors;
  double d, g = 0.0, mse = 0.0, variance;
  int i;
  FileWriter f;
  PrintWriter p;
  Date date;
  double timeSamplingDist, timeSimulating;


  sumW = 0.0;
  sumW2 = 0.0;

  errors = new double[2];


  initialRelations = getInitialRelations();

  if (observations.size()>0)
    restrictToObservations(initialRelations);

  date = new Date();
  timeSamplingDist = (double)date.getTime();
  System.out.println("Computing sampling distributions");
  getSamplingDistributions();
  System.out.println("Sampling distributions computed");
  date = new Date();
  timeSamplingDist=((double)date.getTime()-timeSamplingDist)/1000;

  initSimulationInformation();

  System.out.println("Simulating");

  date = new Date();
  timeSimulating = (double)date.getTime();

  for (i=0 ; i<numberOfRuns ; i++) {
    simulate();
    System.out.println("Tras Simulate");
    normalizeResults();


   for (i=0 ; i<results.size() ; i++) {
      ((Potential)results.elementAt(i)).print();
      }
    // g += errors[0];
   // mse += errors[1];
  }

  date = new Date();
  timeSimulating = ((double)date.getTime() - timeSimulating) /
                    (numberOfRuns * 1000);

  d = sampleSize * numberOfRuns;
  sumW /= d;
  variance = (sumW2/(d*sumW*sumW)) - 1;

  g /= (double)numberOfRuns;
  mse /= (double)numberOfRuns;
  f = new FileWriter(resultFile);

  p = new PrintWriter(f);

  p.println("Time computing sampling distributions (secs): "+
	    timeSamplingDist);
  p.println("Time simulating (avg) : "+timeSimulating);
  p.println("G : "+g);
  p.println("MSE : "+mse);
  p.println("Variance : "+variance);
  f.close();

  System.out.println("Done");
}


public void propagate(){


  double[] errors;
  double d, g = 0.0, mse = 0.0, variance;
  int i;
  Date date;
  double timeSamplingDist, timeSimulating;


  sumW = 0.0;
  sumW2 = 0.0;

  errors = new double[2];


  initialRelations = getInitialRelations();

  if (observations.size()>0)
    restrictToObservations(initialRelations);

  date = new Date();
  timeSamplingDist = (double)date.getTime();
  System.out.println("Computing sampling distributions");
  getSamplingDistributions();
  System.out.println("Sampling distributions computed");
  date = new Date();
  timeSamplingDist=((double)date.getTime()-timeSamplingDist)/1000;

  initSimulationInformation();

  System.out.println("Simulating");

  date = new Date();
  timeSimulating = (double)date.getTime();

  for (i=0 ; i<numberOfRuns ; i++) {
    simulate();
    System.out.println("Tras Simulate");
    normalizeResults();


   for (i=0 ; i<results.size() ; i++) {
      ((Potential)results.elementAt(i)).print();
      }
    // g += errors[0];
   // mse += errors[1];
  }

  date = new Date();
  timeSimulating = ((double)date.getTime() - timeSimulating) /
                    (numberOfRuns * 1000);

  d = sampleSize * numberOfRuns;
  sumW /= d;
  variance = (sumW2/(d*sumW*sumW)) - 1;

  g /= (double)numberOfRuns;
  mse /= (double)numberOfRuns;

  System.out.println("Time computing sampling distributions (secs): "+
	    timeSamplingDist);
  System.out.println("Time simulating (avg) : "+timeSimulating);
  System.out.println("G : "+g);
  System.out.println("MSE : "+mse);
  System.out.println("Variance : "+variance);

  System.out.println("Done");
}

}
