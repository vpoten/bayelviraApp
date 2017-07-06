/* JoinTreeStatistics.java */

package elvira.tools;

import java.io.*;
import elvira.*;
import elvira.inference.clustering.Triangulation;
import elvira.parser.ParseException;

/**
 * Class <code>JoinTreeStatistics</code>. 
 * Contains some interesting information about the join tree.
 *
 * @since 17/8/2001
 */

public class JoinTreeStatistics {

/**
 * Number of cliques in the join tree.
 */
private int numCliques;

/**
 * Minimum number of variables in a clique.
 */
private int minVarsInClique;

/**
 * Maximum number of variables in a clique.
 */
private int maxVarsInClique;

/**
 * Mean number of variables in a clique.
 */
private double meanVarsInClique;

/**
 * Size of the smallest clique in the join tree.
 */
private double minCliqueSize; 

/**
 * Size of the biggest clique in the join tree.
 */
private double maxCliqueSize;

/**
 * Mean clique size.
 */
private double meanCliqueSize;

/**
 * Size of the join tree.
 */
private double JTSize;


/**
 * Constructor.
 */ 

public JoinTreeStatistics() {
  
  numCliques = 0;
  minVarsInClique = 0;
  maxVarsInClique = 0;
  meanVarsInClique = 0.0;
  minCliqueSize = 0.0;
  maxCliqueSize = 0.0;
  meanCliqueSize = 0.0;
  JTSize = 0.0;
}


/**
 * Duplicate.
 */ 

public JoinTreeStatistics duplicate() {
  JoinTreeStatistics jts=new JoinTreeStatistics();  

  jts.setNumCliques(this.numCliques);
  jts.setMinVarsInClique(this.minVarsInClique);
  jts.setMaxVarsInClique(this.maxVarsInClique);
  jts.setMeanVarsInClique(this.meanVarsInClique);
  jts.setMinCliqueSize(this.minCliqueSize);
  jts.setMaxCliqueSize(this.maxCliqueSize);
  jts.setMeanCliqueSize(this.meanCliqueSize);
  jts.setJTSize(this.JTSize);

  return jts;
}


/**
 * Access methods
 */


/**
 * Sets the size of the join tree.
 * @param s the new size.
 */

public void setJTSize(double s) {
  
  JTSize = s;
}


/**
 * Sets the size of the smallest clique in the join tree.
 * @param s the new size.
 */

public void setMinCliqueSize(double s) {
  
  minCliqueSize = s;
}


/**
 * Sets the mean clique size in the join tree.
 * @param s the new size.
 */

public void setMeanCliqueSize(double s) {

  meanCliqueSize = s;
}


/**
 * Sets the size of the biggest clique in the join tree.
 * @param s the new size.
 */

public void setMaxCliqueSize(double s) {
  
  maxCliqueSize = s;
}


/**
 * Sets the number of cliques in the join tree.
 * @param n the number of cliques.
 */

public void setNumCliques(int n) {
  
  numCliques = n;
}


/**
 * Sets the minimum number of variables in a clique in the join tree.
 * @param v the number of variables.
 */

public void setMinVarsInClique(int v) {
  
  minVarsInClique = v;
}


/**
 * Sets the maximum number of variables in a clique in the join tree.
 * @param v the number of variables.
 */

public void setMaxVarsInClique(int v) {
  
  maxVarsInClique = v;
}


/**
 * Sets the average number of variables in a clique in the join tree.
 * @param v the number of variables.
 */

public void setMeanVarsInClique(double v) {
  
  meanVarsInClique = v;
}


/**
 * Gets the size of the join tree.
 * @return the size of the join tree.
 */

public double getJTSize() {
  
  return JTSize;
}


/**
 * Gets the size of the smallest clique.
 * @return that size.
 */

public double getMinCliqueSize( ) {
  
  return minCliqueSize;
}


/**
 * Gets the average clique size.
 * @return that size.
 */

public double getMeanCliqueSize( ) {
  
  return meanCliqueSize;
}


/**
 * Gets the size of the biggest clique.
 * @return that size.
 */

public double getMaxCliqueSize( ) {

  return maxCliqueSize;
}


/**
 * Gets the number of cliques in the join tree.
 * @return the number of cliques.
 */

public int getNumCliques( ) {
  
  return numCliques;
}


/**
 * Gets the lower number of variables in a clique in the join tree.
 * @return the obtained number of variables.
 */

public int getMinVarsInClique( ) {
  
  return minVarsInClique;
}


/**
 * Gets the higher number of variables in a clique in the join tree.
 * @return the obtained number of variables.
 */

public int getMaxVarsInClique( ) {
  
  return maxVarsInClique;
}


/**
 * Gets the average number of variables in a clique in the join tree.
 * @return the obtained number of variables.
 */

public double getMeanVarsInClique( ) {
  
  return meanVarsInClique;
}


/**
 * Saves the object to a file.
 * @param s the name of the file where the statistics
 * will be written.
*/

public void save(String s) throws IOException {

  PrintWriter p;
  FileWriter f;
  
  f = new FileWriter(s);
  p = new PrintWriter(f);
  
  save(p);
}


/**
 * Saves the object to the given output.
 * @param p the <code>PrintWriter</code> where the statistics
 * will be written.
 */

public void save(PrintWriter p) {
  
  p.println("Number of cliques  : " + numCliques);
  p.println("Minimum number of variables in a clique: " +
	    minVarsInClique);
  p.println("Maximum number of variables in a clique: " +
	    maxVarsInClique);
  p.println("Mean number of variables in a clique   : " +
	    meanVarsInClique);
  p.println();
  p.println("Minimum size clique: " + minCliqueSize);
  p.println("Maximum size clique: " + maxCliqueSize);
  p.println("Mean size clique   : " + meanCliqueSize);
  p.println("Total size         : " + JTSize);   
}


/**
 * Prints the object to the standard output.
 */

public void print( ) {
  
  System.out.println("Number of cliques  : " + numCliques);
  System.out.println("Minimum number of variables in a clique: " +
		     minVarsInClique);
  System.out.println("Maximum number of variables in a clique: " +
		     maxVarsInClique);
  System.out.println("Mean number of variables in a clique   : " +
		     meanVarsInClique);
  System.out.println();
  System.out.println("Minimum size clique: " + minCliqueSize);
  System.out.println("Maximum size clique: " + maxCliqueSize);
  System.out.println("Mean size clique   : " + meanCliqueSize);
  System.out.println("Total size         : " + JTSize);   
}


/**
 * Calculate statistics from a relation list, not from a join tree.
 * @param rl the relation list.
 */

public void calculateFromRelationList(RelationList rl) {
  
  int i, minVars, maxVars, totalVars, numVars;
  double min ,max, mean, total, meanVars, val;
  Relation r;

  // initializing values with the relation at position 0
  r = rl.elementAt(0);
  min = max = total = FiniteStates.getSize(r.getVariables().toVector());
  minVars = maxVars = totalVars = r.getVariables().size();
  
  for (i=1 ; i<rl.size() ; i++) {
    r = rl.elementAt(i);
    val = FiniteStates.getSize(r.getVariables().toVector());
    total += val;
    if (val < min)
      min = val;
    if (val > max)
      max = val;

    numVars = r.getVariables().size();
    totalVars += numVars;
    if (numVars < minVars)
      minVars = numVars;
    if (numVars > maxVars)
      maxVars = numVars;
  }
  
  mean = total / (double)rl.size();
  meanVars = totalVars / (double)rl.size();
  
  setNumCliques(rl.size());
  setMinVarsInClique(minVars);
  setMaxVarsInClique(maxVars);
  setMeanVarsInClique(meanVars);
  setMinCliqueSize(min);
  setMaxCliqueSize(max);
  setMeanCliqueSize(mean);
  setJTSize(total);
}


/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows:
 * <ul>
 * <li> the network.
 * <li> the sequence.
 * </ul>
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  FileInputStream networkFile, sequenceFile;
  NodeList deletionSequence;
  Triangulation t;
  JoinTreeStatistics stat;

  if (args.length < 2) {
    System.out.println("\nToo few arguments. The arguments are:\n");
    System.out.println("network sequence");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    System.out.print("\nLoading network ....");
    b = new Bnet(networkFile);
    System.out.print("Newtwork loaded\n");      
    
    sequenceFile = new FileInputStream(args[1]);
    System.out.print("\nLoading deletion sequence ....");
    deletionSequence = new NodeList(sequenceFile,b.getNodeList());
    System.out.print("Deletion sequence loaded\n");

    t = new Triangulation(b);
    t.setTriangulatedNodes(deletionSequence);
    t.triangulate();

    stat = new JoinTreeStatistics();
    stat.calculateFromRelationList(t.getTriangulatedRelations());

    System.out.println("\nEl tamanyo es: " + stat.getJTSize());
  }
} // end of main

} // End of class