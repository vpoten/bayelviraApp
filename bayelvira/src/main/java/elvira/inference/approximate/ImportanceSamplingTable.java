/* ImportanceSamplingTable */

package elvira.inference.approximate;

import java.io.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Date;
import elvira.*;
import elvira.parser.ParseException;
import elvira.potential.*;

/**
 * Class ImportanceSamplingTable.
 * Implements the importance sampling method of propagation
 * based on approximate node deletion using probability tables.
 *
 * @author Antonio.Salmeron@ual.es
 *
 * @since 27/2/2001
 */


public class ImportanceSamplingTable extends ImportanceSampling {

/**
 * Program for performing experiments.
 * The arguments are as follows.
 * <ol>
 * <li> An integer. Maximum size of a potential.
 * <li> An integer. Number of simulation steps.
 * <li> An integer. Number of experiments.
 * <li> Input file: the network.
 * <li> Output error file, where the error and computing time
 *      of each experiment will be stored.
 * <li> File with exact results.
 * <li> File with instantiations.
 * </ol>
 * The last argument can be omitted. In that case, it will
 * be considered that no observations are present.
 *
 * Instead, it can be used to obtain the results of the propagation
 * rather than performing experiments. In this case, arguments
 * are:
 * <ol>
 * <li> Input file: the network.
 * <li> Output file (results of the propagation).
 * <li> An integer. Maximum size of a potential.
 * <li> An integer. Number of simulation steps.
 * <li> File with instantiations.
 * </ol>
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  ImportanceSamplingTable propagation;
  int i, ss, nruns, ls;


  if (args.length < 4)
    System.out.println("Too few arguments");
  else {
    if (args.length < 6) {
      networkFile = new FileInputStream(args[0]);
      b = new Bnet(networkFile);

      if (args.length == 5) {
	evidenceFile= new FileInputStream(args[4]);
	e = new Evidence(evidenceFile,b.getNodeList());
      }
      else
	e = new Evidence();

      ls = (Integer.valueOf(args[2])).intValue();

      ss = (Integer.valueOf(args[3])).intValue();

      propagation = new ImportanceSamplingTable(b,e,ls,ss,1);

      propagation.propagate();

      propagation.saveResults(args[1]);

    }
    else {
      networkFile = new FileInputStream(args[3]);
      b = new Bnet(networkFile);

      if (args.length == 7) {
	evidenceFile = new FileInputStream(args[6]);
	e = new Evidence(evidenceFile,b.getNodeList());
      }
      else
	e = new Evidence();

      ls = (Integer.valueOf(args[0])).intValue();

      ss = (Integer.valueOf(args[1])).intValue();

      nruns = (Integer.valueOf(args[2])).intValue();

      propagation = new ImportanceSamplingTable(b,e,ls,ss,nruns);

      System.out.println("Reading exact results");
      propagation.readExactResults(args[5]);
      System.out.println("Done");

      propagation.propagate(args[4]);
    }
  }
}


/* CONSTRUCTORS */


/**
 * Creates an empty object. Necessary for subclass definitions.
 */

ImportanceSamplingTable() {

}


/**
 * Creates a new propagation with a given evidence and
 * a given network.
 * @param b a belief network (<code>Bnet</code>).
 * @param e an evidence (<code>Evidence</code>).
 */

ImportanceSamplingTable(Bnet b, Evidence e) {

  observations = e;
  network = b;
  positions = new Hashtable(20);
}


/**
 * Creates a new propagation with the options specified in
 * the arguments.
 * @param b a belief network (<code>Bnet</code>).
 * @param e an evidence (<code>Evidence</code>).
 * @param ls the maximum size for potentials.
 * @param ss the sample size.
 * @param nruns the number of runs.
 */

public ImportanceSamplingTable(Bnet b, Evidence e, int ls, int ss, int nruns) {

  observations = e;
  network = b;
  setLimitSize(ls);
  setSampleSize(ss);
  setNumberOfRuns(nruns);
  positions = new Hashtable(20);
}


/* METHODS */


/**
 * Computes the relations in the original network before any propagation
 * is carried out. If the potential associated with the relation is of class
 * <code>PotentialTree</code>, then it is transformed in one of class
 * <code>PotentialTable</code>.
 * @return a <code>RelationList</code> with the initial relations
 * present in the network, before any propagation is done.
 */

public RelationList getInitialRelations() {

  Relation rel, newRel;
  RelationList list;
  PotentialTable pot;
  int i;

  list = new RelationList();

  for (i=0 ; i<network.getRelationList().size() ; i++) {
    rel = (Relation)network.getRelationList().elementAt(i);
    if (rel.getActive()) {
      newRel = new Relation();
      newRel.setVariables(rel.getVariables().copy());

      if (rel.getValues().getClass().getName().equals("elvira.potential.PotentialTree")) {
        pot = new PotentialTable((PotentialTree)rel.getValues());
        newRel.setValues(pot);
      }
      else if (rel.getValues().getClass().getName().equals("elvira.potential.CanonicalPotential")) {
	newRel.setValues(((CanonicalPotential)rel.getValues()).getCPT());
      }
      else {
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
 * <code>deletionSequence</code>. For each variable in that list,
 * its sampling distribution will be stored at the same position in
 * list <code>samplingDistributions</code>.
 *
 * Note that observed variables are not included in the deletion
 * sequence, since they need not be simulated.
 */

public void getSamplingDistributions() {

  NodeList notRemoved;
  Node variableX;
  FiniteStates variableY;
  Relation rel, rel1, rel2;
  RelationList currentRelations, tempList, list;
  PotentialTable pot;
  PairTable pairTable;
  double inc, min = 1e20, minInc, size = 0.0, size1, size2, totalSize;
  int i, j, k, l, p, p1 = 0, p2 = 0, pos, s;
  boolean modified;


  notRemoved = new NodeList();
  pairTable = new PairTable();

  deletionSequence = new NodeList();
  samplingDistributions = new Vector();

  // Select the variables to remove (those not observed).

  for (i=0 ; i<network.getNodeList().size() ; i++) {
    variableX = (FiniteStates)network.getNodeList().elementAt(i);
    if (!observations.isObserved(variableX)) {
      notRemoved.insertNode(variableX);
      pairTable.addElement(variableX);
    }
  }

  currentRelations = getInitialRelations();

  // Now restrict the initial relations to the obervations.

  if (observations.size() > 0)
    restrictToObservations(currentRelations);

  for (i=0 ; i<currentRelations.size() ; i++)
    pairTable.addRelation(currentRelations.elementAt(i));


  for (i=notRemoved.size() ; i>0 ; i--) {
    // Next variable to remove
    variableX = pairTable.nextToRemove();

    // This variable will be in position (i-1) in results
    // and in currentConf[].
    positions.put(variableX,new Integer(i-1));

    notRemoved.removeNode(variableX);
    pairTable.removeVariable(variableX);
    deletionSequence.insertNode(variableX);

    // Get the relations containing the variable and remove them
    // from the list.
    tempList = currentRelations.getRelationsOfAndRemove(variableX);

    // Remove them also from the search table.
    for (j=0 ; j<tempList.size() ; j++) {
      pairTable.removeRelation(tempList.elementAt(j));
    }

    // Put the obtained list of relations as the sampling
    // distribution of the variable (initially).
    samplingDistributions.addElement(tempList.copy());

    // Now combine the relations in the sampling distribution
    // while the threshold is not surpassed.

    modified = true;

    while (modified) {
      modified = false;
      s = tempList.size();
      minInc = 1e20;

      for (j=0 ; j<s-1 ; j++) {
	list = new RelationList();
	rel = tempList.elementAt(j);
	list.insertRelation(rel);
	size1 = ((PotentialTable)rel.getValues()).getValues().length;

	for (l=j+1 ; l<s ; l++) {
	  rel = tempList.elementAt(l);
	  size2 = ((PotentialTable)rel.getValues()).getValues().length;
	  if (size1 > size2)
	    size = size1;
	  else
	    size = size2;

	  list.insertRelation(rel);
	  totalSize = list.totalSize();
	  inc = totalSize - size;

	  if (inc < minInc) {
	    p1 = j;
	    p2 = l;
	    modified = true;
	    minInc = inc;
	    min = totalSize;
	  }
	  list.removeRelationAt(1);
	}
      }

      if (modified && (min<=(double)limitSize)) {
	rel1 = tempList.elementAt(p1);
	rel2 = tempList.elementAt(p2);

	tempList.removeRelationAt(p2);
	tempList.removeRelationAt(p1);
	pot = (PotentialTable)rel1.getValues();

	pot = pot.combine((PotentialTable)rel2.getValues());

	rel = new Relation();

        rel.setKind(Relation.POTENTIAL);
	rel.getVariables().setNodes ((Vector)pot.getVariables().clone());
	rel.setValues(pot);
	tempList.insertRelation(rel);
      }
      else {
	modified = false;
      }
    }

    // Now remove the variable and update the list
    // of current relations.
    if (i>1) {
      for (j=0 ; j<tempList.size() ; j++) {
	rel = tempList.elementAt(j);
	if (rel.getVariables().size()>1) {
	  pot = (PotentialTable)rel.getValues();
	  pot = (PotentialTable)pot.addVariable(variableX);

          rel.setKind(Relation.POTENTIAL);
	  rel.getVariables().setNodes((Vector)pot.getVariables().clone());
	  rel.setValues(pot);
	  currentRelations.insertRelation(rel);
	  pairTable.addRelation(rel);
	}
      }
    }
  }
}


/**
 * Simulates a configuration by the inverse transform method.
 * If the simulation is successful, the simulated configuration
 * is stored in <code>currentConf</code>.
 *
 * @param generator a random number generator.
 * @return <code>true</code> if the simulation was ok,
 * <code>false</code> otherwise.
 */

public boolean simulateConfiguration(Random generator) {

  FiniteStates variableX;
  RelationList list;
  int i, s, v;
  boolean ok = true;


  s = samplingDistributions.size()-1;

  for (i=s ; i>=0 ; i--) {
    variableX = (FiniteStates)deletionSequence.elementAt(i);
    list = (RelationList)samplingDistributions.elementAt(i);
    v = simulateValue(variableX,s-i,list,generator);

    if (v == -1) { // Zero valuation
      ok = false;
      break;
    }
    currentConf[s-i] = v;
  }

  return ok;
}


/**
 * Simulates a value for a variable, by the inverse transform method.
 * @param variableX a <code>FiniteStates</code> variable to be simulated.
 * @param pos the position of variableX in the <code>currentConf</code>.
 * @param list the list of sampling distributions of variableX. Actually,
 * a single sampling distribution specified as a list (product) of
 * potentials.
 * @param generator a random number generator.
 * @return the value simulated. -1 if the valuation is constantly equal to 0.
 */

public int simulateValue(FiniteStates variableX, int pos,
			 RelationList list, Random generator) {

  int i, nv, v = -1;
  double checksum = 0.0, r, cum = 0.0;
  double [] values;


  nv = variableX.getNumStates();
  values = new double[nv];

  for (i=0 ; i<nv ; i++) {
    currentConf[pos] = i;
    values[i] = evaluate(list);
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
 * Simulates a sample of size <code>sampleSize</code> and updates the
 * simulation information. It modifies instance variable
 * <code>currentWeight</code>.
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
 * Evaluates a <code>RelationList</code> for a given
 * <code>Configuration</code>. To evaluate means to get the value of each
 * relation in the list for the given configuration and compute the
 * product of those values.
 * @param list the <code>RelationList</code> to evaluate.
 * @param conf a <code>Configuration</code> for which the list of relations
 * will be evaluated.
 * @return the probability resulting from the evaluation.
 */

public double evaluate(RelationList list, Configuration conf) {

  int i, s;
  double value = 1.0;
  Relation rel;
  PotentialTable pot;

  s = list.size();
  for (i=0 ; i<s ; i++) {
    rel = list.elementAt(i);
    pot = (PotentialTable)rel.getValues();
    value *= pot.getValue(conf);
  }

  return value;
}


/**
 * Evaluates a <code>RelationList</code> for the configuration stored
 * in <code>currentConf</code>. To evaluate means to get the value of each
 * relation in the list for the given configuration and compute the
 * product of those values.
 * @param list the <code>RelationList</code> to evaluate.
 * @return the probability of <code>currentConf</code> according to
 * the list of relations.
 */

public double evaluate(RelationList list) {

  int i, s;
  double value = 1.0;
  Relation rel;
  PotentialTable pot;

  s = list.size();
  for (i=0 ; i<s ; i++) {
    rel = list.elementAt(i);
    pot = (PotentialTable)rel.getValues();

    // If size == 0 it means that the potential corresponds to
    // a single variable and that variable is observed.
    // So, do not evaluate it.

    if (pot.getVariables().size() > 0)
      value *= pot.getValue(positions,currentConf);
  }

  return value;
}


/**
 * Computes the probability of obtaining a configuration
 * according to the original conditional distributions in
 * the network.
 * @param conf a <code>Configuration</code> to evaluate.
 * @return the probability of conf.
 */

public double evaluate(Configuration conf) {

  int i, s;
  double value = 1.0;
  Relation rel;
  PotentialTable pot;

  s = initialRelations.size();
  for (i=0 ; i<s ; i++) {
    rel = (Relation)initialRelations.elementAt(i);
    pot = (PotentialTable)rel.getValues();

    // If size == 0 it means that the potential corresponds to
    // a single variable and that variable is observed.
    // So, do not evaluate it.

    if (pot.getVariables().size() > 0)
      value *= pot.getValue(conf);
  }

  return value;
}


/**
 * Computes the probability of obtaining <code>currentConf</code>
 * according to the original conditional distributions in
 * the network.
 * @return the probability of currentConf.
 */

public double evaluate() {

  int i, s;
  double value = 1.0;
  Relation rel;
  PotentialTable pot;

  s = initialRelations.size();
  for (i=0 ; i<s ; i++) {
    rel = (Relation)initialRelations.elementAt(i);
    pot = (PotentialTable)rel.getValues();

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
 * @param list the <code>RelationList</code> to restrict.
 */

public void restrictToObservations(RelationList list) {

  Relation rel;
  int i, s;

  s = list.size();

  for (i=0 ; i<s ; i++) {
    rel = list.elementAt(i);
    rel.setValues (((PotentialTable)rel.getValues()).
      restrictVariable(observations));
    rel.getVariables().setNodes (rel.getValues().getVariables());
  }
}


/**
 * Carries out a propagation. At the end of the propagation,
 * <code>results</code> will contain the result.
 * NOTE: The exact results must be ready in <code>exactResults</code>
 * before calling this method.
 * @param resultFile the name of the file where the errors will be stored.
 */

public void propagate(String resultFile) throws IOException {


  double[] errors;
  double g = 0.0, mse = 0.0;
  int i;
  FileWriter f;
  PrintWriter p;
  Date date;
  double timeSamplingDist, timeSimulating = 0.0, parTime;


  // In the first position, the g-error,
  // in the second, the mean squared error.
  errors = new double[2];

  initialRelations = getInitialRelations();

  // Restrict the relations to the observations.
  if (observations.size()>0)
    restrictToObservations(initialRelations);

  date = new Date();
  timeSamplingDist = (double)date.getTime();

  // Compute the sampling distributions.
  System.out.println("Computing sampling distributions");
  getSamplingDistributions();
  System.out.println("Sampling distributions computed");

  date = new Date();
  timeSamplingDist=((double)date.getTime()-timeSamplingDist)/1000;

  // Initialize the simulation information.
  initSimulationInformation();

  // The simulation itself begins here.
  System.out.println("Simulating");

  for (i=0 ; i<numberOfRuns ; i++) {
    date = new Date();
    parTime = (double)date.getTime();

    simulate();
    normalizeResults();

    date = new Date();
    parTime = ((double)date.getTime() - parTime) / 1000;

    timeSimulating += parTime;

    // Compute the errors and update the error counts.
    computeError(errors);
    g += errors[0];
    mse += errors[1];

    if (i < (numberOfRuns - 1)) {
      // If there are more runs left, clear the simulation information.
      clearSimulationInformation();
    }

  }

  timeSimulating /= numberOfRuns;

  g /= (double)numberOfRuns;
  mse /= (double)numberOfRuns;

  f=new FileWriter(resultFile);

  p=new PrintWriter(f);

  p.println("Time computing sampling distributions (secs): "+
	    timeSamplingDist);
  p.println("Time simulating (avg) : "+timeSimulating);
  p.println("G : "+g);
  p.println("MSE : "+mse);
  p.println("Variance : "+varianceOfWeights());
  f.close();

  System.out.println("Done");
}


/**
 * Carries out a propagation. At the end of the propagation,
 * <code>reaults</code> will contain the result.
 * NOTE: The exact results must be ready before calling to this method.
 * Some statistics are reported to the standard output.
 */

public void propagate() {


  double[] errors;
  double g = 0.0, mse = 0.0;
  int i;
  Date date;
  double timeSamplingDist, timeSimulating = 0.0, parTime;


  errors = new double[2];

  initialRelations = getInitialRelations();

  // Restrict the relations to the observations.
  if (observations.size()>0)
    restrictToObservations(initialRelations);

  date = new Date();
  timeSamplingDist = (double)date.getTime();

  // Compute the sampling distributions.
  System.out.println("Computing sampling distributions");
  getSamplingDistributions();
  System.out.println("Sampling distributions computed");

  date = new Date();
  timeSamplingDist=((double)date.getTime()-timeSamplingDist)/1000;

  // Initialize the simulation information.
  initSimulationInformation();

  // The simulation itself begins here.
  System.out.println("Simulating");

  for (i=0 ; i<numberOfRuns ; i++) {
    date = new Date();
    parTime = (double)date.getTime();

    simulate();
    normalizeResults();

    date = new Date();
    parTime = ((double)date.getTime() - parTime) / 1000;

    timeSimulating += parTime;

    // Compute the errors and update the error counts.
    if (exactResults!=null)
      computeError(errors);
    g += errors[0];
    mse += errors[1];

    if (i < (numberOfRuns - 1)) {
      // If there are more runs left, clear the simulation information.
      clearSimulationInformation();
    }

  }

  timeSimulating /= numberOfRuns;

  g /= (double)numberOfRuns;
  mse /= (double)numberOfRuns;

  System.out.println("Time computing sampling distributions (secs): "+
	    timeSamplingDist);
  System.out.println("Time simulating (avg) : "+timeSimulating);
  System.out.println("G : "+g);
  System.out.println("MSE : "+mse);
  System.out.println("Variance : "+varianceOfWeights());

  System.out.println("Done");
}

} // End of class
