/* LogicSampling.java */

package elvira.inference.approximate;

import java.util.Vector;
import java.util.Random;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.potential.*;


/**
 * This class implements the Probabilistic Logic Sampling method of propagation.
 *
 * @author Antonio Angel F. Contreras (afernandez@melkor.com)
 * @author Andrés Cano (acu@decsai.ugr.es)
 *
 * @since 04/07/2001
 */

public class LogicSampling extends Propagation {

/**
 * Constructs a new propagation for a given Bayesian network and
 * some evidence.
 *
 * @param b a <code>Bnet</code>.
 * @param e the evidence.
 */

public LogicSampling(Bnet b, Evidence e) {

  observations = e;
  network = b;
}


/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows:
 * <ol>
 * <li> An integer. Number of simulation steps.
 * <li> Input file: the network.
 * <li> Output file, where the results of the propagation for each
 *      variable will be stored.
 * <li> Evidence file.
 * </ol>
 * If the evidence file is omitted, then no evidences are
 * considered.
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  LogicSampling ar;
  int i;

  if (args.length < 3)
    System.out.println("Too few arguments. Arguments are: SimulationSteps ElviraFile OutputFile [EvidenceFile]");
  else {
    networkFile = new FileInputStream(args[1]);
    b = new Bnet(networkFile);

    if (args.length == 4) {
      evidenceFile = new FileInputStream(args[3]);
      e = new Evidence(evidenceFile,b.getNodeList());
    }
    else
      e = new Evidence();

    ar = new LogicSampling(b,e);
    ar.propagate(args[0], args[2]);
  }
}


/**
 * Carries out a propagation storing the results in <code>results</code>.
 */

public void propagate(String simulationSteps) {

  NodeList nl;
  FiniteStates nodo;
  int i, j, z, tama, z1, ss;
  boolean salir = false;
  Relation relacion;
  PotentialTable pot, posteriori;
  Configuration conf;
  double aleatorio = 0, valor;
  Random generator = new Random();
  conf = new Configuration();

  nl = network.topologicalOrder();
  ss = (Integer.valueOf(simulationSteps)).intValue();
  for (i=0; i < ss; i++) {
    for (j=0; j < nl.size(); j++) {
      nodo = (FiniteStates)nl.elementAt(j);
      relacion = network.getRelation(nodo);
      pot = (PotentialTable)relacion.getValues().copy();
      pot = (PotentialTable)pot.restrictVariable(conf);
      aleatorio = generator.nextDouble();
      valor = 0;
      for (z=0; z < pot.getSize(); z++){
        valor = valor + pot.getValue(z);
        if (valor > aleatorio) {
          if ( (observations.isObserved(nodo)) && (observations.getValue(nodo) != z) )
            salir = true;
          else conf.insert(nodo, z);
          break;
        }
      }
      if (salir) {
        salir = false;
        break;
      }
    }
    if (conf.size() == nl.size()) {
      for (z=0; z < conf.size(); z++){
        posteriori = (PotentialTable)results.elementAt(z);
        posteriori.incValue(conf.getValue(z),1);
      }
    }
    tama = conf.size();
    for (z=0; z < tama; z++){
      conf.remove(0);
    }
  }

  normalizeResults();
}

/**
 * Initializes the simulation information to be stored in the
 * instance variable <code>results</code>.
 */

public void initSimulationInformation() {

  int i;
  FiniteStates nodo;
  NodeList list;
  PotentialTable pot;

  list = network.topologicalOrder();
  for (i=0 ; i<list.size() ; i++) {
    nodo = (FiniteStates)list.elementAt(i);
    results.addElement(new PotentialTable(nodo));
  }
}

/**
 * Carries out a propagation saving the results in <code>OutputFile</code>.
 *
 * @param outputFile the file where the results will be
 *                   stored.
 */

public void propagate(String simulationSteps, String outputFile) throws ParseException, IOException {

  Date d;
  double time;
  int z;
  PotentialTable posteriori;

  initSimulationInformation();

  d = new Date();
  time = (double)d.getTime();

  propagate(simulationSteps);

  d = new Date();
  time = ((double)d.getTime() - time) / 1000;

  for (z=0; z < results.size(); z++) {
    posteriori = (PotentialTable)results.elementAt(z);
    posteriori.print();
  }
  System.out.println("");
  System.out.println("Time (secs): " + time);

  saveResults(outputFile);
}





/**
 * getSample: returns a new sample for all the variables in the network.
 * We assume no evidence has been observed.
 * 
 * This is an interesting method to be used to generate new individual
 * population in EDA-like schemes
 */

public int[] getSample(Random generator){
  NodeList nl;
  int j,i,nStates;
  int[] sample;
  FiniteStates v;
  Relation rel;
  PotentialTable pot;
  Configuration conf = new Configuration();
  double[] table;  
  double r,total; // a [0,1] random number
 

  nl = network.topologicalOrder();
  sample = new int[nl.size()];

  for (j=0; j < nl.size(); j++) {
    v = (FiniteStates)nl.elementAt(j);
    rel = network.getRelation(v);
    pot = (PotentialTable)rel.getValues();
    nStates = v.getNumStates();
    table = new double[nStates];
    for(i=0;i<nStates;i++){
      conf.putValue(v,i);
      table[i] = pot.getValue(conf); 
    }
      
    r = generator.nextDouble();
    for(i=0,total=0.0;i<nStates;i++){
      total += table[i];
      if (r <= total) break;
    }
    conf.putValue(v,i);    
    sample[j]=i;
  }

  return sample;
}


} // End of class

// java -classpath "D:\elvira\bayelvira2\classes;D:\elvira\bayelvira2"
// elvira.inference.approximate.LogicSampling 1000 asia.elv result.res
