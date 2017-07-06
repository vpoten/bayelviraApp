/* ContinuousMCMC.java */

package elvira.inference.approximate;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.parser.*;
import elvira.potential.*;
import elvira.database.DataBaseCases;


/**
 * Class ContinuousMCMC.
 * Implements a  Markov Chain Monte Carlo algorithm for networks where
 * potentials are of class <code>PotentialContinuousPT</code>.
 *
 * @since 10/10/2000
 */


public class ContinuousMCMC extends MarkovChainMonteCarlo {

  
public static int CONTINUOUS = 0;
public static int FINITE_STATES = 1;


/**
 * Program for performing experiments.
 * The arguments are as follows.
 * <ol>
 * <li> An integer. Number of simulation steps.
 * <li> An integer. Number of experiments.
 * <li> Input file: the network.
 * <li> Output error file, where the error and computing time
 *      of each experiment will be stored.
 * <li> Result file, where the results of the propagation for each
 *      variable will be stored.
 * <li> Evidence file. This argument is optional.
 * </ol>
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  ContinuousMCMC propagation;
  int i, ss, nruns;
  
  
  if (args.length<4)
    System.out.println("Too few arguments");
  else {
    networkFile = new FileInputStream(args[2]);
    b = new Bnet(networkFile);

    if (args.length == 6) {
      evidenceFile= new FileInputStream(args[5]);
      e = new Evidence(evidenceFile,b.getNodeList());
    }
    else
      e = new Evidence();

    ss = (Integer.valueOf(args[0])).intValue();
    
    nruns = (Integer.valueOf(args[1])).intValue();
 
    propagation = new ContinuousMCMC(b,e,ss,nruns);
   
    propagation.propagate(args[3],args[4]);
  }
}


/**
 * Creates a new propagation for a given evidence and
 * a given network.
 *
 * @param b a belief network.
 * @param e an evidence.
 */

public ContinuousMCMC(Bnet b, Evidence e) {

  observations = e;
  network = b;
}  


/**
 * Creates a new propagation.
 * @param b a belief network.
 * @param e an evidence.
 * @param s the sample size.
 * @param nRuns the number of repetitions of the experiment.
 */

public ContinuousMCMC(Bnet b, Evidence e, int s, int nRuns) {

  observations = e;
  network = b;
  setSampleSize(s);
  setNumberOfRuns(nRuns);
}


/**
 * Obtains an initial configuration by forward sampling.
 * It is assumed that <code>distributions</code> has been initialized
 * with the distributions in each Markov Blanket.
 *
 * @return the <code>ContinuousConfiguration</code> obtained.
 */

public ContinuousConfiguration getInitialConfiguration() {

  NodeList ancestralOrder, variables;
  Node var;
  ContinuousConfiguration initialConf;
  MixtExpDensity density;
  PotentialContinuousPT pot;
  Relation rel = null;
  RelationList blanket;
  int i, j;
  double value;
  boolean found;
  
  
  initialConf = new ContinuousConfiguration();
  ancestralOrder = network.topologicalOrder();
  
  // Now simulate the variables in ancestral order
  
  for (i=0 ; i<ancestralOrder.size() ; i++) {
    
    var = ancestralOrder.elementAt(i);
    
    // If the variable is observed, do not simulate it.
    
    if (observations.isObserved(var)) {
      if (var.getTypeOfVariable() == CONTINUOUS) {
	value = observations.getValue((Continuous)var);
	initialConf.insert((Continuous)var,value);
      }
      else {
	value = observations.getValue((FiniteStates)var);
	initialConf.insert((FiniteStates)var,(int)value);
      }
    }
    else { // The variable is not observed
      blanket = (RelationList)distributions.get(var);
      
      // restricts the Markov Blanket of the variable to simulate, to
      // the values already simulated.
      blanket = blanket.restrict(initialConf,var);
      
      // obtain the sampling distribution of the variable as
      // the distribution that contains only the interest variable.
      j=0;
      found = false;
      while ((j < blanket.size()) && (!found)) {
	rel = blanket.elementAt(j);
	variables = rel.getVariables();
	if ((variables.size() == 1) && (variables.getId(var) >= 0)) {
	  found = true;
	}
	j++;
      }
      
      if (!found) {
	System.out.println("Error in getInitialConfiguration");
	System.exit(1);
      }
      
      // Now simulate a value.
      
      pot = (PotentialContinuousPT)rel.getValues();
      
      value = pot.simulateValue();
      
      if (var.getTypeOfVariable() == CONTINUOUS)
	initialConf.insert((Continuous)var,value);
      else
	initialConf.insert((FiniteStates)var,(int)value);
    }
  }
  
  return initialConf;
}


/**
 * Initializes the simulation information to be stored in the
 * instance variable <code>results</code>.
 */

public void initSimulationInformation() {

  int i, j, number;
  double inc, max, min;
  Node node;
  NodeList list;
  PotentialContinuousPT pot;
  Vector cp;
  
  list = network.getNodeList();
  results = new Vector();
  
  for (i=0 ; i<list.size() ; i++) {
    node = list.elementAt(i);
    if (node.getTypeOfVariable() == FINITE_STATES)
      results.addElement(new PotentialTable((FiniteStates)node));
    else {
      min = ((Continuous)node).getMin();
      max = ((Continuous)node).getMax();
      
      // Allow about 100 observations per cutpoint.
      number = (sampleSize / 100) + 1;
      
      inc = (max - min) / (double)number;
      cp = new Vector();
      
      for (j=0 ; j<number ; j++) {
	cp.addElement(new Double(min));
	min += inc;
      }
      cp.addElement(new Double(max));
     
      pot = new PotentialContinuousPT((Continuous)node,cp,0);      
      results.addElement(pot);
    }
  }
}


/**
 * Clears the simulation information stored in the
 * instance variable <code>results</code>.
 */

public void clearSimulationInformation() {

  int i, j, number;
  double inc, max, min;
  Node node;
  NodeList list;
  Potential pot;
  Vector cp;
  
  list = network.getNodeList();
  
  for (i=0 ; i<list.size() ; i++) {
    pot = (Potential)results.elementAt(i);
    node = list.elementAt(i);
    if (node.getTypeOfVariable() == FINITE_STATES) {
      ((PotentialTable)pot).setValue(0);
    }
    else {
      ((PotentialContinuousPT)pot).getTree().setToZero();
    }
  }
}


/**
 * Updates the simulation information according to the
 * current weight and the argument configuration.
 * @param conf the simulated configuration.
 */

public void updateSimulationInformation(ContinuousConfiguration conf) {
  
  int i, s, t;
  double v;
  Node node;
  ContinuousProbabilityTree tree;
  Potential pot;
  
  s = results.size();
  
  for (i=0 ; i<s ; i++) {
    pot = (Potential)results.elementAt(i);
    
    node = (Node)pot.getVariables().elementAt(0);
    if (node.getTypeOfVariable() == FINITE_STATES) {
      t = conf.getValue((FiniteStates)node);
      ((PotentialTable)pot).incValue(t,1.0);
    }
    else {
      tree = ((PotentialContinuousPT)pot).getTree();      
      v = conf.getValue((Continuous)node);
      t = tree.getCutPoint(v);
      tree.incValue(t,1.0);
    }
  }
}

/**
 * Prints the result of a propagation to the standard output.
 */

public void printResults() {

  int i;
  Potential pot;
    
  System.out.println();
  
  for (i=0 ; i<results.size() ; i++) {
    pot = (Potential)results.elementAt(i);
    pot.print();
  }
  
  System.out.println();
}


/**
 * Generates a sample by MCMC from an initial configuration.
 * The sample is stored in variable <code>sample</code>.
 * Argument <code>initialConf</code> is modified.
 * 
 * @param initialConf the initial configuration.
 */

public void generateSample(ContinuousConfiguration initialConf) {
 
  ContinuousCaseListMem currentSample;
  Node var;
  NodeList list;
  PotentialContinuousPT pot1, pot2;
  RelationList blanket;
  int i, j, k;
  double simulatedValue;
  
  list = network.getNodeList();
  currentSample = new ContinuousCaseListMem(list);
  
  for (k=0 ; k<sampleSize ; k++) {
    for (i=0 ; i<list.size() ; i++) {
      var = list.elementAt(i); // The variable to simulate.
      
      // If the variable is observed, do not simulate it.
      
      if (!observations.isObserved(var)) {
	
	// Obtain the distributions in the Markov Blanket of the variable.
	blanket = (RelationList)distributions.get(var);
	
	// Obtain the combined potential, restricting to the current
	// configuration except for the variable being simulated.
	
	pot1 = (PotentialContinuousPT)blanket.elementAt(0).getValues();
	
	pot1 = (PotentialContinuousPT)pot1.restrictVariable(initialConf,var);
	
	for (j=1 ; j<blanket.size() ; j++) {
	  pot2 = (PotentialContinuousPT)blanket.elementAt(j).getValues();
	  pot2 = (PotentialContinuousPT)pot2.restrictVariable(initialConf,var);	
	  pot1 = pot1.combine(pot2);
	}
	
	// Now simulate a value for val and update the configuration.
	simulatedValue = pot1.simulateValue();
	
	if (var.getTypeOfVariable() == FINITE_STATES)
	  initialConf.putValue((FiniteStates)var,(int)simulatedValue);
	else
	  initialConf.putValue((Continuous)var,simulatedValue);
      }
    }
    // Insert the simulated configuration in the sample.
    currentSample.put(initialConf);
    
    // Update the simulation information.
    updateSimulationInformation(initialConf);
  }
  
  // Now put the sample in its corresponding field.
  sample = currentSample;
}


/**
 * Carries out a propagation. 
 * 
 * @param resultFile the name of the file where the results
 * will be reported.
 */

public void propagate(String errorFile, String resultFile) throws IOException {

  int i;
  ContinuousConfiguration initialConf;
  FileWriter f;
  PrintWriter p;
  Date date;
  double timeSimulating = 0.0, parTime;

  
  System.out.println("\nSimulating\n");
  
  initSimulationInformation();
  
  System.out.println("\nInformacion de simulacion inicializada\n");
  
  determineMarkovBlanket();
  
  System.out.println("\nMarkov blanket calculado\n");
  
  initialConf = getInitialConfiguration();
  
  System.out.println("\nConfiguracion incial generada\n");
  
  initialConf.print();
  
  System.out.println();
  
  for (i=0 ; i<numberOfRuns ; i++) {
    date = new Date();
    parTime = (double)date.getTime();

    generateSample(initialConf);
    
    date = new Date();
    parTime = ((double)date.getTime() - parTime) / 1000;
    
    timeSimulating += parTime;
    
    normalizeResults();
    
    // Now, error must be computed.
    
    
    // Now, save the results (As a dbc file).
    
    //saveResults(resultFile);
    DataBaseCases dbcSimulated = new DataBaseCases("SampleMCMC",sample);
    FileWriter f1=new FileWriter(resultFile);
    dbcSimulated.saveDataBase(f1);
    f1.close();

    
    if (i < (numberOfRuns - 1))
      clearSimulationInformation();
  }
  
  timeSimulating /= numberOfRuns;

  
  f=new FileWriter(errorFile);
  
  p=new PrintWriter(f);
  
  p.println("Time simulating (avg) : "+timeSimulating);
  f.close();

  System.out.println("Done");
}


} // End of class
  
