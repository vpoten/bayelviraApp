/* PropagationStatistics */

package elvira.tools;

import java.io.*;
import java.util.Vector;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialTable;
import elvira.potential.binaryprobabilitytree.PotentialBPTree;

/**
 * Class <code>PropagationStatistics</code>.
 * Contains some interesting information in order to evaluate
 * the propagation carried out.
 *
 * @since 11/10/2000
 */

public class PropagationStatistics {

/**
 * Time spent in the propagation.
 */
private double time;

/**
 * Statistics about the join tree.
 */
private JoinTreeStatistics JTStat;

/**
 * These two values are necesary in exact partial abductive inference,
 * because the join tree is modified.
 */
private double JTExtraSize;
private double JTInitialSize;

/**
 * Vector to add the operations needed to evaluate a network
 * with elimination methods
 */

private Vector operations;

/**
 * Vector to store the sizes of the network during the
 * evaluation of the network with the elimination methods
 */

private Vector sizes;
private Vector numberNodes;

/**
 * Vector to store the times of computation until completion
 * of each operation
 */

private Vector times;

/**
 * Vector with a measure of importance for the variables of
 * each of the decisions
 */

private Vector explanations;

/**
 * Potential to store the final expected utility for
 * influence diagrams evaluation
 */

private Potential finalExpectedUtility;

/**
 * To store the name of the elvira file used in this
 * propagation
 */

protected String fileName;

/**
 * Vector with the sizes of decision trees
 */
protected Vector decSizes;

/**
 * Error of the initial Utitliy
 */
protected double initalUtilityError;


/**
 * Size of the initial utility potential
 */

protected long initialUtilitySize;


/**
 * Constructor.
 */

public PropagationStatistics() {

  time = 0.0;
  JTStat = new JoinTreeStatistics();
  JTExtraSize = 0.0;
  JTInitialSize = 0.0;

  // Initialize the vector of operations and sizes

  operations=new Vector();
  sizes=new Vector();
  numberNodes=new Vector();
  times=new Vector();
  explanations=new Vector();
  decSizes= new Vector();
}


/**
 * Access methods
 */


/**
 * Sets the time spent in the propagation.
 * @param t the time.
 */

public void setTime(double t) {

  time = t;
}


/**
 * Sets the statistics about the join tree.
 * @param s the statistics.
 */

public void setJTStat(JoinTreeStatistics s) {

  JTStat = s;
}


/**
 * Sets the initial size.
 * @param s new size.
 */

public void setJTInitialSize(double s) {

  JTInitialSize = s;
}


/**
 * Sets the extra size.
 * @param s new size.
 */

public void setJTExtraSize(double s) {

  JTExtraSize = s;
}


/**
 * Gets the time spent in the propagation.
 * @return the time.
 */

public double getTime( ) {

  return time;
}


/**
 * Gets the statistics about the join tree.
 * @return the statistics.
 */

public JoinTreeStatistics getJTStat(){
  return JTStat;
}


/**
 * Gets the final expected Utility.
 */

public Potential getFinalExpectedUtility() {
  return finalExpectedUtility;
}

/**
 * Gets the initial size.
 * @return the initial size.
 */

public double getJTInitialSize( ){

  return JTInitialSize;
}


/**
 * Gets the extra size.
 * @return the extra size.
 */

public double getJTExtraSize( ) {

  return JTExtraSize;
}

/**
 * Gets the vector of times
 * @return times
 */

public Vector getTimes(){
  return times;
}

/**
 * Gets the vector of sizes
 * @return sizes
 */

public Vector getSizes(){
  return sizes;
}

/**
 * Gets the vector of sizes
 * @return sizes
 */

public Vector getNumberNodes(){
  return numberNodes;
}

/**
 * Gets the fileName
 * @return fileName
 */

public String getFileName(){
  return fileName;
}

/**
 * Prints the object to the standard output.
 */

public void print( ) {

  this.print(false);
}


/**
 * Prints the object to the standard output.
 * @param partial indicates whether the propagation is an exact
 * partial abductive inference (<code>true</code>).
 */

public void print(boolean partial) {

  System.out.println("Printing statistics about the propagation");
  System.out.println("\tTime: " + time);
  System.out.println();

  if (partial) {
    System.out.println("\tSize of the initial jt: " + JTInitialSize);
    System.out.println("\tSize of the whole jt: " +
		       (JTInitialSize+JTExtraSize));
    System.out.println("\tData about the jt used in abductive inference");
  }

  JTStat.print();
}

/**
 * Method to store a new operation in the vector of operations
 */

public void addOperation(String operation){
  operations.addElement((Object)operation);
}

/**
 * Method to add a new size if the vector of sizes
 */

public void addSize(double size){
  sizes.addElement(new Double(size));
}


/**
 * Method to add a new decision size if the vector of sizes
 */

public void addDecSize(double size){
  decSizes.addElement(new Double(size));
}



/**
 * Get the sizes of decision trees
 * @return 
 */
public Vector getDecSizes() {
    return decSizes;
}

/**
 * Method to add a new nodes value if the vector of sizes
 */

public void addNumberNodes(double size){
  numberNodes.addElement(new Double(size));
}

/**
 * Method to add a new time if the vector of times
 */

public void addTime(double time){
  times.addElement(new Double(time));
}

/**
 * Method to store the final expected utility
 * @param <code>Potential</code> the final expected utility
 */

public void setFinalExpectedUtility(Potential pot){
  finalExpectedUtility=pot;
}

/**
 * Method to store the name of the file used for this
 * propagation
 * @param <code>String</code> the name of the elvira file
 */

public void setFileName(String name){
  fileName=name;
}

/**
 * Method to store a vector, with the measure of importance for
 * the sets of variables of each of the decision tables
 */

public void setExplanation(String varName,Potential pot){
  PotentialTree potTree;

	// Deals with the potential to get a measure for the variables
   // belonging to it. Anyway, do it as potentialTree
  //
   if (pot instanceof PotentialTable){
		//Transform it to potentialTree
		potTree=((PotentialTable)pot).toTree();
	}
   else if(pot instanceof PotentialBPTree)
                potTree = new PotentialTree(pot);
   else

		potTree=(PotentialTree)pot;

	// Now, measure the relevance for the variables

//	explanations.addElement(potTree.measureRelevance(varName));
}

/**
 * Method to save to a file the vectors of operations and sizes
 * @param <code>String</code> the name of file where the data will
 *                            ve stored
 */

public void printOperationsAndSizes() throws IOException{
	File file;
  FileWriter f;
  PrintWriter p;
  Vector explanation;
	String directory;
  int i,j;

	 // Create a directory to store the data about the
	 // evaluation

   directory=fileName+"_dir";
	 file=new File(directory);
	 System.out.println("Creacion directorio: "+file.mkdir());

   // Create a new FileWriter object

   f=new FileWriter(directory+"/op_util_exp");

   // Create a new object PrintWriter object

   p=new PrintWriter(f);

   // Now, go on operation to operation showing the
   // list of operations and the final utility

   for(i=0; i < operations.size(); i++){
     p.println((String)operations.elementAt(i));
   }

   // Finally shows the final expectedUtility

   if (finalExpectedUtility != null) {
    finalExpectedUtility.saveResult(p);
   }

	 // Shows the explanation

	 for(i=0; i < explanations.size() ; i++){
		 explanation=(Vector)explanations.elementAt(i);

		 // Prints the measure for this table

		 p.println();
		 p.println("++++++++++++++++++++++++++++++++++++++++++");

		 for(j=0; j < explanation.size(); j++){
		 	p.println((String)explanation.elementAt(j));
	   }

		 p.println();
		 p.println("++++++++++++++++++++++++++++++++++++++++++");
	}

	// Shows the time needed to evaluate

	p.println();
	p.println("Computation time: "+time);

   // Close the descriptor for file writting

   f.close();

	 // Create a new file for sizes of operations

	 f=new FileWriter(directory+"/sizes");
	 p=new PrintWriter(f);

	 for(i=0; i < sizes.size(); i++){
			p.println((i+1)+" "+sizes.elementAt(i).toString());
	 }

	 // Close the descriptor

	 f.close();

	 // Create a new file for times of computation

	 f=new FileWriter(directory+"/times");
	 p=new PrintWriter(f);

	 for(i=0; i < times.size(); i++){
			p.println((i+1)+" "+times.elementAt(i).toString());
	 }

	 // Close the descriptor

	 f.close();
}

/**
 * Method to get the total computation time from
 * vector of times
 * @return the last time stored
 */

public double getMaximumTime(){
  return(((Double)times.elementAt(times.size()-1)).doubleValue());
}

/**
 * Method to return the maximum size reached during the
 * evaluation
 * @return maximum size
 */

public double getMaximumSize(){
  int i;
  double val,max;

  for(i=0,max=0; i < sizes.size(); i++){
    val=((Double)sizes.elementAt(i)).doubleValue();
    if (val > max)
      max=val;
  }

  // Return max

  return max;
}

/**
 * Method for computing the average size during the computation
 * @return getAverageSize
 */
public double getAverageSize(){
    double average=0,value;

    // Consider the values
    for(int i=0; i < sizes.size(); i++){
       value=(Double)sizes.elementAt(i);
       average+=value;
    }

    // Compute the average
    average=average/sizes.size();

    // Return average
    return average;
}

/**
 * Method to return the maximum size reached during the
 * evaluation
 * @return maximum size
 */

public double getMaximumNumberNodes(){
  int i;
  double val,max;

  for(i=0,max=0; i < numberNodes.size(); i++){
    val=((Double)numberNodes.elementAt(i)).doubleValue();
    if (val > max)
      max=val;
  }

  // Return max

  return max;
}

/**
 * Method for computing the average size during the computation
 * @return getAverageSize
 */
public double getAverageNumberNodes(){
    double average=0,value;

    // Consider the values
    for(int i=0; i < numberNodes.size(); i++){
       value=(Double)numberNodes.elementAt(i);
       average+=value;
    }

    // Compute the average
    average=average/numberNodes.size();

    // Return average
    return average;
}

    public double getInitalUtilityError() {
        return initalUtilityError;
    }

    public void setInitalUtilityError(double ininitalUtilityError) {
        this.initalUtilityError = ininitalUtilityError;
    }

    public long getInitialUtilitySize() {
        return initialUtilitySize;
    }

    public void setInitialUtilitySize(long initialUtilitySize) {
        this.initialUtilitySize = initialUtilitySize;
    }





} // End of class
