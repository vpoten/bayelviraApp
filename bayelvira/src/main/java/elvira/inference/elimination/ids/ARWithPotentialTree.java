/* ArcReversal.java */
package elvira.inference.elimination.ids;

import java.io.*;
import elvira.*;
import elvira.parser.ParseException;
import elvira.potential.*;

/**  
 * Class <code>ARWithPotentialTree</code>. Implements the solution of 
 * a regular ID (Influence Diagram) with potential trees.
 * @author Manuel Gomez 
 * @since 12/6/2001
 */
public class ARWithPotentialTree extends ArcReversal {

    /**
     * To store the threshold for prunning operations
     */
    private double thresholdForPrunning;

    /**
     * Maximum and minimum values reached during IDs evaluation
     */
    protected double maximum;
    protected double minimum;

    /**
     * Default constructor
     * @param diag IDiagram to evaluate
     */
    public ARWithPotentialTree(IDiagram diag) {

        super(diag);
    }

    /**
     * Program for performing experiments from the command line.
     * The command line arguments are as follows.
     * <ol>
     * <li> Input file: the network.
     * <li> Output file.
     * <li> Evidence file.
     * </ol>
     * If the evidence file is omitted, then no evidences are
     * considered .
     */
    public static void main(String args[]) throws ParseException, IOException {

        ARWithPotentialTree eval;
        FileInputStream networkFile;
        String base;
        int evaluationMode;
        boolean evaluable;
        IDiagram diag;

        if (args.length < 3) {
            System.out.println("Use: ElviraFile, OutputFile, thresholdForPrunning");
            System.exit(-1);
        }

        diag = (IDiagram) Network.read(args[0]);
        eval = new ARWithPotentialTree(diag);

        // Set the threshold for prunning operations

        eval.setThresholdForPrunning((new Double(args[2])).doubleValue());

        // initial chekout about the node.

        evaluable = eval.initialConditions();
        System.out.print("Evaluable : " + evaluable + "\n\n");

        // Compose the name of the file to store the statistics

        base = args[0].substring(0, args[0].lastIndexOf('.'));
        base = base.concat("_ARWithPotentialTree_data");
        eval.statistics.setFileName(base);

        // If the diagram is suitable to be evaluated, then do it.

        if (evaluable == true) {
            eval.evaluateDiagram();
            eval.saveResults(args[1]);
        }
    }

    /**
     * Method to return the value of maximum
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * Method to set the value for maximum
     * @param value to set
     */
    public void setMaximum(double value) {
        if (value > maximum) {
            maximum = value;
        }
    }

    /**
     * Method to return the value of minimum
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * Method to set the value for minimum
     * @param value to set
     */
    public void setMinimum(double value) {
        if (value < minimum) {
            minimum = value;
        }
    }

    /**
     * Method to sed the threshold for prunning operations
     */
    public void setThresholdForPrunning(double value) {
        thresholdForPrunning = value;
    }

    /**
     * Transforms one of the original relations into another one whose values
     * are of class <code>PotentialTree</code>.
     * @param <code>Relation</code> relation to transform
     */
    public Relation transformInitialRelation(Relation r) {
        PotentialTree potTree;
        double maximum, minimum;

        //Apply only to non constraint relations

        if (r.getKind() != Relation.CONSTRAINT) {
            if (r.getValues().getClassName().equals("PotentialTable")) {
                potTree = ((PotentialTable) r.getValues()).toTree();
            } else {
                potTree = (PotentialTree) r.getValues();
            }

            // Try to prune the tree, joining identical values

            // If it is a utility relation, sort it
            // Initially there is not approximation

            if (r.getKind() == Relation.UTILITY) {

                // First at all look for minimum and maximum

                minimum = potTree.getTree().minimumValue();
                maximum = potTree.getTree().maximumValue();
                setMinimum(minimum);
                setMaximum(maximum);
                potTree = potTree.sortUtilityAndPrune(minimum, maximum, 0L);
            } else {
                potTree = potTree.sortAndBound(thresholdForPrunning);
            }

            // Store the new potential
            r.setValues(potTree);
        }

        // Return the relation

        return r;
    }

    /**
     * To tranform a potential after an operation on it
     * @param <code>Potential</code> potential to transform
     * @param <code>boolean</code>  is a utility?
     * @return <code>Potential</code> the modified potential
     */
    public Potential transformAfterOperation(Potential pot, boolean flag) {
        PotentialTree potTree;

        // Try to prune joining identical values

        if (pot.getClassName().equals("PotentialTable")) {
            potTree = ((PotentialTable) pot).toTree();
        } else {
            potTree = (PotentialTree) pot;
        }

        // Prune it
        // If the potential belongs to an utility relation, sort it
        // Now we use the thresholdForPrunning as measure for the
        // operations on the tree

        if (flag) {
            potTree = potTree.sortUtilityAndPrune(getMinimum(), getMaximum(), 0L);
        } else {
            potTree = potTree.sortAndBound(thresholdForPrunning);
        }

        // Return the transformed pot

        return (potTree);
    }
} // End of class

