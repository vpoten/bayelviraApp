/* LikelihoodSampling.java */

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
 * This class implements the Likelihood Weighting method of propagation.
 *
 * @author Severino Fernández Galán (seve@dia.uned.es)
 *
 * @since 12/10/2002
 */

public class LikelihoodWeighting extends Propagation {

/**
 * Constructs a new propagation for a given Bayesian network and
 * some evidence.
 *
 * @param b a <code>Bnet</code>.
 * @param e the evidence.
 */

public LikelihoodWeighting(Bnet b, Evidence e) {

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
  LikelihoodWeighting lw;

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

    lw = new LikelihoodWeighting(b,e);
    lw.propagate(args[0], args[2]);
  }
}


/**
 * Carries out a propagation storing the results in <code>results</code>.
 */

public void propagate(String simulationSteps) {

   NodeList nl;
   FiniteStates node;
   int ss;
   Relation rela;
   PotentialTable pot, posteriori;
   Configuration conf;
   double aleatorio = 0, valor;
   Random generator = new Random();
   conf = new Configuration();
   int evidenceValue;
   double conditionalProbability, configurationScore;
   boolean configurationIsValid; //Determines whether the present configuration is discarded (score = 0)

   nl = network.topologicalOrder();
   ss = (Integer.valueOf(simulationSteps)).intValue();
   for (int i=0; i < ss; i++) {
      configurationIsValid = true;
      configurationScore = 1;
      for (int j=0; j < nl.size(); j++) {
         node = (FiniteStates)nl.elementAt(j);
         rela = network.getRelation(node);
         pot = (PotentialTable) rela.getValues().restrictVariable(conf);
         if (!observations.isObserved(node)) {
            aleatorio = generator.nextDouble();
            valor = 0;
            for (int z=0; z < pot.getSize(); z++) {
               valor += pot.getValue(z);
               if (valor > aleatorio) {
                  conf.insert(node, z);
                  break;
               }
            }
         } else {
            evidenceValue = observations.getValue(node);
            conf.insert(node, evidenceValue); //The evidence node is introduced in the configuration
            conditionalProbability = pot.getValue(evidenceValue);
            if (conditionalProbability > 0)
               configurationScore *= conditionalProbability;
            else {
               configurationIsValid = false; //Partial configuration discarded
               break;
            }
         }
      }
      if (configurationIsValid) {
         for (int z=0; z < conf.size(); z++) {
            posteriori = (PotentialTable)results.elementAt(z);
            posteriori.incValue(conf.getValue(z), configurationScore);
         }
      }
      conf.getVariables().removeAllElements();
      conf.getValues().removeAllElements();
   }
   normalizeResults();
}

/**
 * Initializes the simulation information to be stored in the
 * instance variable <code>results</code>.
 */

public void initSimulationInformation() {

  FiniteStates node;
  NodeList list;

  list = network.topologicalOrder();
  for (int i=0 ; i<list.size() ; i++) {
    node = (FiniteStates)list.elementAt(i);
    results.addElement(new PotentialTable(node));
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
  PotentialTable posteriori;

  initSimulationInformation();

  d = new Date();
  time = (double)d.getTime();

  propagate(simulationSteps);

  d = new Date();
  time = ((double)d.getTime() - time) / 1000;

  for (int z=0; z < results.size(); z++) {
    posteriori = (PotentialTable)results.elementAt(z);
    posteriori.print();
  }
  System.out.println("");
  System.out.println("Time (secs): " + time);

  saveResults(outputFile);
}


} // End of class

// java -classpath "D:\elvira\bayelvira2\classes;D:\elvira\bayelvira2"
// elvira.inference.approximate.LikelihoodWeighting 1000 asia.elv result.res
