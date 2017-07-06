/* ImportanceSampligTree.java */

package elvira.inference.approximate;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Date;  
import java.io.*;
import elvira.*;
import elvira.parser.ParseException;
import elvira.potential.*;


/**
 * Class ImportanceSamplingDynamic.
 * Implements the importance sampling method of propagation
 * based on approximate node deletion, using Probability Trees.
 * Trees are updated during the simulation, try to capture the target
 * distribution.
 *
 * @author Serafin Moral (smc@decsai.ugr.es)
 * @author Antonio.Salmeron@ual.es
 * @since 8/5/2006
 */

public class ImportanceSamplingDynamic extends ImportanceSamplingTree {



/**
 * The value for which the computed tree will be updated.
 * Each time that the value of a variable is going to be simulated
 * if the ratio between the real normalization value and the
 * normalization value given by the tree is lower than this
 * double, then the approximate tree is updated to avoid this
 * in the future
 */

double limitForUpdating;

/**
 * A vector. In each position and with the same order than
 * the deletion sequence we have the potential that is added to
 * the list of potentials after deleting a variable: the
 * marginalization of the combination of all the potentials
 * containing a variable
 */

Vector deletionDistributions;


/**
 * A vector. In each position and with the same order than
 * the deletion sequence we have the distribution
 */

Vector sentDistributions;


/**
 * Program for performing experiments.
 * The arguments are as follows.
 * <ol>
 * <li> A double; limit for pruning.
 * <li> A double; limit for updating.
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
 * <li> A double; limit for prunning.
 * <li> A double; limit for updating.
 * <li> An integer. Maximum size of a potential.
 * <li> An integer. Number of simulation steps.
 * <li> File with instantiations.
 * </ol>
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  ImportanceSamplingTree propagation;
  int i, ss, nruns, ls;
  double lp,lu;

  if (args.length < 6)
    System.out.println("Wrong number of arguments.");
  else {
    if (args.length < 8) {
      networkFile = new FileInputStream(args[0]);
      b = new Bnet(networkFile);

      if (args.length == 7) {
	evidenceFile = new FileInputStream(args[6]);
	e = new Evidence(evidenceFile,b.getNodeList());
      }
      else
	e = new Evidence();

      lp = (Double.valueOf(args[2])).doubleValue();

      lu = (Double.valueOf(args[3])).doubleValue();
      ls = (Integer.valueOf(args[4])).intValue();

      ss = (Integer.valueOf(args[5])).intValue();

      propagation = new ImportanceSamplingDynamic(b,e,lp,lu,ls,ss,1);

      propagation.propagate();

      propagation.saveResults(args[1]);
    }
    else {
      networkFile = new FileInputStream(args[5]);
      b = new Bnet(networkFile);

      if (args.length == 9) {
	evidenceFile= new FileInputStream(args[8]);
	e = new Evidence(evidenceFile,b.getNodeList());
      }
      else
	e = new Evidence();

      lp = (Double.valueOf(args[0])).doubleValue();
      lu = (Double.valueOf(args[1])).doubleValue();
      ls = (Integer.valueOf(args[2])).intValue();

      ss = (Integer.valueOf(args[3])).intValue();

      nruns = (Integer.valueOf(args[4])).intValue();

      propagation = new ImportanceSamplingDynamic(b,e,lp,lu,ls,ss,nruns);

      System.out.println("Reading exact results");
      propagation.readExactResults(args[7]);
      System.out.println("Done");

      propagation.propagate(args[6]);
    }
  }
}


/**
 * Empty constructor, for compatibility with subclasses.
 */

public ImportanceSamplingDynamic() {

}


/**
 * Creates a new propagation with the options given as arguments.
 * @param b a belief netowrk.
 * @param e an evidence.
 * @param lp the limit for prunning.
 * @param lu the limit for updating.
 * @param ls the maximum size for potentials.
 * @param ss the sample size.
 * @param nruns the number of runs.
 */

public ImportanceSamplingDynamic(Bnet b, Evidence e, double lp,
				 double lu, int ls,
				 int ss, int nruns) {
  
  observations = e;
  network = b;
  setLimitSize(ls);
  setLimitForPrunning(lp);
  setLimitForUpdating(lu);
  setSampleSize(ss);
  setNumberOfRuns(nruns);
  positions = new Hashtable(20);
}


/* METHODS */

/**
 * Sets the limit for prunning as the entropy of a binary
 * distribution which is deviated from the uniform distribution
 * as indicated by the argument.
 * @param epsilon the deviation with respect to a
 * uniform distribution.
 */

public void setLimitForPrunning(double epsilon) {

  limitForPrunning = 1 + ((0.5-epsilon) * Math.log(0.5-epsilon) /
			  Math.log(2) +
			  (0.5+epsilon) * Math.log(0.5+epsilon) /
			  Math.log(2));
}


/**
 * Sets the limit for updating.
 * @param epsilon the value for updating.
 */

public void setLimitForUpdating(double epsilon) {

  limitForUpdating = epsilon;
}




/**
 * Simulates a sample of size <code>sampleSize</code> and
 * updates the weights in the simulation information.
 */

public void simulate() {

  int i;
  double w;
  boolean ok;
  Random generator = new Random();


  currentConf = new int[network.getNodeList().size()];
  
  i = 0;
  
  while (i < sampleSize) {
    currentWeight = 1.0;
    
    ok = simulateConfiguration(generator,1.0);
    
    if (ok) {
      w = evaluate();
      currentWeight *= w;
      updateSimulationInformation();
      i++;
    }
  }

}


/**
 * Simulates a configuration by the inverse transform method.
 * If the simulation is successful, the simulated configuration
 * is stored in <code>currentConf</code>. The simulation order is
 * opposite to the elimination order.
 * @param generator a random number generator.
 * @return <code>true</code> if the simulation was ok,
 * <code>false</code> otherwise.
 */

public boolean simulateConfiguration(Random generator, double alpha) {
  
  FiniteStates variableX;
  PotentialTree pot;
  RelationList listVarX;
  int i, s, v;
  boolean ok = true;
  
  s = samplingDistributions.size()-1;
  
  for (i=s ; i>=0 ; i--) {
    variableX = (FiniteStates)deletionSequence.elementAt(i);
    pot = (PotentialTree)sentDistributions.elementAt(i);
    listVarX = (RelationList) deletionDistributions.elementAt(i);
    
    v = simulateValue(variableX,s-i,pot,listVarX,generator,alpha);
    
    if (v==-1) { // A zero valuation is obtained
      ok = false;
      break;
    }
    currentConf[s-i] = v;
  }
  
  return ok;
}


/**
 * Simulates a value for a variable, by the inverse transform method.
 * @param variableX a <code>FiniteStates</code> variable to be
 * simulated.
 * @param pos the position of variableX in the current conf.
 * @param pot the potential that was sent to the following level when removing
 * variable <code> variableX </code>.
 * @param generator a random number generator.
 * @return the value simulated. -1 if the valuation is 0.
 */

public int simulateValue(FiniteStates variableX, int pos,
			 PotentialTree pot, RelationList listVarX,
			 Random generator, double alpha) {

  int i, j, s, nv, v = -1;
  double checksum = 0.0, sentvalue, a, b, r, cum = 0.0;
  double [] values;
  Vector listPotentials;
  Vector listActiveNodes;


  nv = variableX.getNumStates();
  values = new double[nv];
  
  listPotentials = new Vector();

  s = listVarX.size();
  
  
  for (i=0 ; i<nv ; i++)
    values[i] = 1.0;
  
  for (j=0 ; j<s ; j++)
    ((PotentialTree) listVarX.elementAt(j).getValues()).getVectors(positions,pos,nv,currentConf,values);
  
  for (i=0 ; i<nv ; i++)
    checksum += values[i];
  
  if (checksum == 0.0) {
    listActiveNodes = new Vector();
    for (j=0 ; j<s ; j++)
      ((PotentialTree) listVarX.elementAt(j).getValues()).getActiveNodes(positions,pos,currentConf,listActiveNodes);
    
    pot.update(positions,currentConf,listActiveNodes,0.0,0);

    return -1;
  }
  
  sentvalue = pot.getValue(positions, currentConf);
  
  a = checksum / sentvalue;
  b = sentvalue / checksum;
  
  if (a>b)
    a = b;

  if (a < limitForUpdating) {
    listActiveNodes = new Vector();
    for (j=0 ; j<s ; j++)
      ((PotentialTree) listVarX.elementAt(j).getValues()).getActiveNodes(positions,pos,currentConf,listActiveNodes);
    
    pot.update(positions,currentConf,listActiveNodes,checksum,0);
  }
  
  r = generator.nextDouble();
  
  checksum = 0.0;
  
  for (i=0 ; i<nv ; i++) {
    // if (values[i]>0.0) {values[i] = 1.0/nv;}
    values[i] = Math.pow(values[i],alpha);
    checksum += values[i];
  }
    
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
  Relation rel;
  RelationList currentRelations, tempList;
  PotentialTree pot;
  PairTable pairTable;
  int i, j, k, p, pos, s,l;


  notRemoved = new NodeList();
  pairTable = new PairTable();

  deletionSequence = new NodeList();
  samplingDistributions = new Vector();
  deletionDistributions =  new Vector();
  sentDistributions =  new Vector();


  // Select the variables to remove (those not observed).
  s = network.getNodeList().size();
  
  for (i=0 ; i<s ; i++) {
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

    deletionDistributions.addElement(tempList);

    // Remove them also from the search table.
    l = tempList.size();
    rel = tempList.elementAt(0);
    pairTable.removeRelation(rel);
    pot = (PotentialTree)rel.getValues();

    for (j=1 ; j< l ; j++) {
      rel = tempList.elementAt(j);
      pairTable.removeRelation(rel);
      pot = (PotentialTree) pot.combine((PotentialTree)rel.getValues());
    }
    
    // Put the obtained list of relations as the sampling
    // distribution of the variable (initially).
    samplingDistributions.addElement(pot);
    
    pot = (PotentialTree)pot.addVariable(variableX);
    pot.limitBound(limitForPrunning);
    
    pot = (PotentialTree) pot.sortAndBound(limitSize);
    
    if (l == 1) {
      for (k=pot.getVariables().size()-1 ; k>=0 ; k--) {
	variableY = (FiniteStates)pot.getVariables().elementAt(k);
	
	if (!pot.getTree().isIn(variableY)) {
	  if (currentRelations.isIn(variableY)) {
	    pos = pot.getVariables().indexOf(variableY);
	    pot.getVariables().removeElementAt(pos);
	  }
	}
      }
    }
    
    sentDistributions.addElement(pot);

    rel = new Relation();
    
    rel.setKind(Relation.POTENTIAL);
    rel.getVariables().setNodes ((Vector)pot.getVariables().clone());
    rel.setValues(pot);
    currentRelations.insertRelation(rel);
    pairTable.addRelation(rel);
  }
  
}

} // end of ImportanceSamplingDynamic class
