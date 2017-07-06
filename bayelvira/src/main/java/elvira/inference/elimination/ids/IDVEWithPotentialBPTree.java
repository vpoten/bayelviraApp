package elvira.inference.elimination.ids;

import java.io.*;
import elvira.*;
import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.potential.binaryprobabilitytree.PotentialBPTree;
import elvira.tools.idiagram.EUComparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class
 * <code>VEWithPotentialBPTree</code>. Implements the variable elimination
 * method of propagation using potentials of class
 * <code>PotentialBPTree</code>. If the initial potentials are not
 * PotentialBPTrees then they are converted to PotentialBPTrees.
 *
 * @author Rafael Cabañas de Paz (rcabanas@decsai.ugr.es)
 */
public class IDVEWithPotentialBPTree extends IDVariableElimination {

    /**
     * A very low limit for prunning, allowing almost exact calculations.
     */
    protected static final double limitForPrunning = 0.0001;

    /**
     * All posible methods for pruning the tree
     */
    public static enum pruningMethods {

        NO_PRUNING, KULLBACK_LEIBLER_DISTANCE, EUCLIDEAN, EUCLIDEAN_NORM, EUCLIDEAN_EXP, COSINE, EXT_JACCARD, RELATIVE2, CINDEX
    };
    //                                      0               1                       2           3               4          5            6           7      8                         
    /**
     * Indicates the pruning method used for probability trees. By default, it
     * is not used any method
     */
    private pruningMethods pruningProbMethod = pruningMethods.NO_PRUNING;
    /**
     * Indicates the pruning method used for utility trees. By default, it is
     * not used any method
     */
    private pruningMethods pruningUtilityMethod = pruningMethods.NO_PRUNING;
    /**
     * Indicates if the c-index is used for making the partitions when pruning
     * utility trees
     */
    private boolean Cindex = false;
    /**
     * Limit to produce the identification of leaves in the tree This value can
     * be changed with setThresholdForPruning
     */
    private double thresholdForPruningProb = 0.0;
    /**
     * Limit to produce the identification of leaves in the tree This value can
     * be changed with setThresholdForPruningUtility
     */
    private double thresholdForPruningUtility = 0.0;
    /**
     * Indicate if variables are sorted after each operation
     */
    private boolean sortProbTrees = true;
    /**
     * Indicate if variables are sorted after each operation
     */
    private boolean sortUtilityTrees = true;
    /**
     * Data members to store the max and min values for the potentials
     */
    protected double maximum, minimum;
    /**
     * Indicate if utility trees are normalized
     */
    boolean normalize = false;
    /**
     * Indicates if transformation for utility trees are only performed at the
     * begining or after each operation
     */
    boolean onlyInitialTransformation = false;

    /**
     * Constructs a new propagation for a given Bayesian network and some
     * evidence.
     *
     * @param b a
     * <code>Bnet</code>.
     * @param e the evidence.
     */
    public IDVEWithPotentialBPTree(Bnet b, Evidence e) {
        super(b, e);
    }

    /**
     * Constructs a new propagation for a given Bayesian network
     *
     * @param b a
     * <code>Bnet</code>.
     */
    public IDVEWithPotentialBPTree(Bnet b) {
        super(b, new Evidence());
    }

    /**
     * Transforms one of the original relations into another one whose values
     * are of class
     * <code>PotentialBPTree</code>. @ param r the
     * <code>Relation</code> to be transformed.
     */
    @Override
    public Relation transformInitialRelation(Relation r) {
        PotentialBPTree potTree;
        Relation rNew;

        // Transform the relation, but only for non constraints relations

        if (r.getKind() != Relation.CONSTRAINT) {
            rNew = new Relation();
            rNew.setVariables(r.getVariables().copy());
            rNew.setKind(r.getKind());

            //Transforms the tree
            if (!r.getValues().getClassName().equals("PotentialBPTree")) {
                potTree = new PotentialBPTree(r.getValues());
            } else {
                potTree = (PotentialBPTree) r.getValues();
            }

            // Now, prune the tree. If it is a utility, sort its
            // variables. Initially only exact prune operations


            if (r.getKind() == Relation.UTILITY) { //It is an utility tree

                double threshold = 0;
                if (pruningUtilityMethod != pruningMethods.NO_PRUNING) {
                    threshold = thresholdForPruningUtility;
                }


                Potential tinit = new PotentialTable(potTree);

                potTree.sortAndPruneUtility(sortUtilityTrees, normalize, pruningUtilityMethod, threshold, Cindex);


                //Saves the statistics of the initial prune
                if (generateStatistics) {
                    this.statistics.setInitalUtilityError(EUComparator.compareUtilities(tinit, potTree));
                    this.statistics.setInitialUtilitySize(potTree.getSize());
                }



            } else {

                //It is a probability potential
                if (sortProbTrees) {
                    potTree.sort();
                }
                if (pruningProbMethod == pruningMethods.KULLBACK_LEIBLER_DISTANCE) {
                    potTree.limitBound(thresholdForPruningProb);
                }
            }

            // Store the final potential

            rNew.setValues(potTree);

            // Return the new relation

            return rNew;
        } else {
            // For constraints, do not change it
            return r;
        }
    }

    /**
     * Transform an utility potential, prunning the lower values if possible
     *
     * @param <code>Potential</code> the potential to transform
     * @param <code>boolean</code> is a utility?
     */
    @Override
    public Potential transformAfterOperation(Potential pot, boolean utility) {
        PotentialBPTree potTree;



        // Try to prune, joining identical values

        if (pot.getClassName().equals("PotentialTable")) {
            potTree = new PotentialBPTree(pot);
        } else {
            potTree = (PotentialBPTree) pot;
        }

        // Prune the tree. If it is a utility tree, sort its variables
        // Now prune operation is done using thresholdForPrunning

        if (utility) { // It is an utility potential

            double threshold = 0;
            if (pruningUtilityMethod != pruningMethods.NO_PRUNING) {
                threshold = thresholdForPruningUtility;
            }

            potTree.sortAndPruneUtility(sortUtilityTrees, normalize, pruningUtilityMethod, threshold, Cindex);


        } else {    //It is a probability potential



            if (sortProbTrees) {
                potTree.sort();
            }
            if (pruningProbMethod == pruningMethods.KULLBACK_LEIBLER_DISTANCE) {
                potTree.limitBound(thresholdForPruningProb);
            }

        }

        // Return the modified pot

        return potTree;


    }

    /**
     * Method to set the value for maximum
     *
     * @param value to set
     */
    public void setMaximum(double value) {
        if (value > maximum) {
            maximum = value;
        }
    }

    /**
     * Method to set the value for minimum
     *
     * @param value to set
     */
    public void setMinimum(double value) {
        if (value < minimum) {
            minimum = value;
        }
    }

    /**
     * Method for setting the pruning method with probability trees
     *
     * @param pruningMethod
     */
    public void setPruningProbMethod(pruningMethods pruningMethod) {
        this.pruningProbMethod = pruningMethod;
    }

    /**
     * Method to indicate if the variables in probability trees must be sort
     *
     * @param sortVariablesInTrees
     */
    public void setSortProbTrees(boolean sortVariablesInTrees) {
        this.sortProbTrees = sortVariablesInTrees;
    }

    /**
     * Sets the treshold for pruning probability trees
     *
     * @param thresholdForPruning
     */
    public void setThresholdForPruningProb(double thresholdForPruning) {
        this.thresholdForPruningProb = thresholdForPruning;
    }

    /**
     * Method for setting the pruning method with Utility trees
     *
     * @param pruningMethod
     */
    public void setPruningUtilityMethod(pruningMethods pruningMethod) {
        this.pruningUtilityMethod = pruningMethod;
    }

    /**
     * Method to indicate if the variables in Utility trees must be sort
     *
     * @param sortVariablesInTrees
     */
    public void setSortUtilityTrees(boolean sortVariablesInTrees) {
        this.sortUtilityTrees = sortVariablesInTrees;
    }

    /**
     * Sets the treshold for pruning probability trees
     *
     * @param thresholdForPruning
     */
    public void setThresholdForPruningUtility(double thresholdForPruning) {
        this.thresholdForPruningUtility = thresholdForPruning;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;


    }

    /**
     * Method which indicates if transformations are made only with the initial
     * potentials
     *
     * @return
     */
    public boolean isOnlyInitialTransformation() {
        return onlyInitialTransformation;
    }

    /**
     * Method to set the transformation only at the begining or after each
     * operation
     *
     * @param onlyInitialTransformation
     */
    public void setOnlyInitialTransformation(boolean onlyInitialTransformation) {
        this.onlyInitialTransformation = onlyInitialTransformation;
    }

    public boolean isCindex() {
        return Cindex;
    }

    public void setCindex(boolean Cindex) {
        this.Cindex = Cindex;
    }
}
