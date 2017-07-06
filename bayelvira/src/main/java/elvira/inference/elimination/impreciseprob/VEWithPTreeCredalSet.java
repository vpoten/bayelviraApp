/*
 * VEWithPTreeCredalSet.java
 *
 * Created on 26 de abril de 2004, 14:48
 */
package elvira.inference.elimination.impreciseprob;

import elvira.*;
import elvira.potential.*;
import elvira.inference.elimination.VariableElimination;
import elvira.parser.ParseException;
import elvira.tools.CmdLineArguments;
import elvira.tools.CmdLineArguments.CmdLineArgumentsException;
import elvira.tools.CmdLineArguments.argumentType;

import java.io.*;
import java.util.Vector;

/** 
 * This class implements the variable elimination method of propagation
 * for intervals.
 * Initial potentials can be of class PotentialTable or PTreeCredalSet. Those
 * potentials are converted into PTreeCredalSets to make operations. Resulting
 * potentials are converted into PotentialIntervalTable
 *
 * @author  Andr�s Cano Utrera (acu@decsai.ugr.es)
 */
public class VEWithPTreeCredalSet extends VariableElimination {

    /**
     * Enumeration with the different kinds of pruning methods.
     */
    public static enum pruningMethods {

        NO_PRUNING, KULLBACK_LEIBLER_DISTANCE
    };
    /**
     * Pruning method currently being used.
     */
    private pruningMethods pruningMethod = pruningMethods.NO_PRUNING;
    /**
     * The threshold for pruning.
     */
    private double thresholdForPruning = 0.0;
    /**
     * Sets if tree variables must be sorted or not.
     */
    private boolean sortVariablesInTrees = true;

    /** Creates a new instance of VEWithPTreesCredalSet */
    public VEWithPTreeCredalSet(Bnet b, Evidence e) {
        super(b, e);
    }

    /** Creates a new instance of VEWithPTreesCredalSet */
    public VEWithPTreeCredalSet(Bnet b) {
        super(b);
    }

    /**
     * Sets the pruning method to be used.
     * @param prunMethod The pruning method to use: (0: NO_PRUNING,
     *                   1: KULLBACK_LEIBLER_DISTANCE).
     * @param threshold The threshold for pruning.
     */
    public void setPruningMethod(int prunMethod, double threshold) {
        thresholdForPruning = threshold;
        if (prunMethod == 0) {
            pruningMethod = pruningMethods.NO_PRUNING;
        } else if (prunMethod == 1) {
            pruningMethod = pruningMethods.KULLBACK_LEIBLER_DISTANCE;
        }
    }
    
    /**
     * Sets if tree variables must be sorted or not.
     * @param sort True if we want to sort the variables, false otherwise.
     */
    public void setSortVariablesInTrees(boolean sort) {
        sortVariablesInTrees = sort;
    }

    /**
     * Method to propagate for a target variable. The results of the
     * propagation will be stored in a PotentialIntervalTable
     * @param var target of the propagation
     * @param evidence used for the propagation
     */
    public PotentialIntervalTable propagate(FiniteStates var, Evidence evidence) {
        setObservations(evidence); // Set the evidence
        insertVarInterest(var);// The variable var will be fixed as the interest variable
        // Obtain the variables of interest
        //obtainInterest();
        propagate();  // Now, make the propagation itself

        // The results data member will contain the desired potential
        // in results data member
        PotentialIntervalTable result = (PotentialIntervalTable) results.elementAt(0);

        // return the result
        return result;
    }

    /**
     * Transforms the Potential of one of the original relations. If the Potential
     * is a PotentialTree, a PotentialTable or a PotentialInterval then it is
     * transformed into a PTreeCredalSet.
     * If the Potential is a PTreeCredalSet then it is not modified.
     * Otherwise an error is produced.
     * @param r The <code>Relation</code> to be transformed.
     */
    @Override
    public Relation transformInitialRelation(Relation r) {
        // Converts a PotentialTree, a PotentialTable or a PotentialInterval
        // into a PTreeCredalSet
        if(r.getValues() instanceof PTreeCredalSet || r.getValues() instanceof PotentialTree ||
                r.getValues() instanceof PotentialTable || r.getValues() instanceof PotentialInterval) {
            Relation rNew = new Relation();
            rNew.setVariables(r.getVariables().copy());
            rNew.setKind(r.getKind());

            PTreeCredalSet pTreeCS;
            if(r.getValues() instanceof PTreeCredalSet)
                pTreeCS = (PTreeCredalSet)r.getValues();
            else if(r.getValues() instanceof PotentialTree) {
                PotentialTable pt = new PotentialTable(r.getValues());
                pTreeCS = new PTreeCredalSet(pt);
            }
            else if(r.getValues() instanceof PotentialTable)
                pTreeCS = new PTreeCredalSet((PotentialTable)r.getValues());
            else
                pTreeCS = new PTreeCredalSet((PotentialInterval)r.getValues());

            // Sorts variables if needed.
            if(sortVariablesInTrees)
                pTreeCS=(PTreeCredalSet)pTreeCS.sort();

            // Prunes the tree if needed.
            if(pruningMethod == pruningMethods.KULLBACK_LEIBLER_DISTANCE) {
                pTreeCS.limitBound(thresholdForPruning);
                // Removes variables not present in the tree from the potential
                pTreeCS.removeVarsNotInTree(true, currentRelations,interest);
            }
            rNew.setValues(pTreeCS);
            return rNew;
        }
        else{
            System.out.print("\nError in " + getClass() + ".transformInitialRelation(Relation r): ");
            System.out.println("Potentials of " + r.getValues().getClassName() +  " class cannot be propagated with this class.");
            System.exit(1);
        }
        return null;
    }

    /**
     * Transforms a <code>PTreeCredalSet</code> obtained as a result of
     * adding one variable (<code>FiniteStates</code>).
     * @param potential The new <code>PTreeCredalSet</code>.
     */
    @Override
    public Potential transformAfterAdding(Potential potential) {
        PTreeCredalSet pTreeCS = (PTreeCredalSet) potential;
        // Sorts variables if needed.
        if (sortVariablesInTrees) {
            pTreeCS = (PTreeCredalSet) pTreeCS.sort();
        }
        // Prunes the tree if needed.
        if (pruningMethod == pruningMethods.KULLBACK_LEIBLER_DISTANCE) {
            pTreeCS.limitBound(thresholdForPruning);
            // Removes variables not present in the tree from the potential
            pTreeCS.removeVarsNotInTree(true, currentRelations,interest);
        }
        potential = pTreeCS;
        return potential;
    }

    /**
     * Normalizes the results of the propagation. Each result is saved as a
     * PotentialIntervalTable.
     */
    @Override
    public void normalizeResults() {
        Potential pot;

        for(int i = 0; i < results.size(); i++) {
            pot = (Potential)results.elementAt(i);
            // Remove the transparent variables which does not take part on the PTreeCredalSet
            ((PTreeCredalSet)pot).removeVarsNotInTree(true, currentRelations,interest);
            // Converts the potential into a PotentialConvexSet.
            pot = new PotentialConvexSet((CredalSet)pot);
            // Normalizes each extreme point.
            pot.normalize();
            // Converts to PotentialIntervalTable.
            pot = new PotentialIntervalTable((CredalSet)pot);
            results.setElementAt(pot, i);
        }
    }

    /**
     * Program for performing experiments from the command line.
     */
    public static void main(String args[]) throws ParseException, IOException {

        Network b;
        Evidence e = new Evidence();
        VEWithPTreeCredalSet ve;
        String resultsFile = "tmp.out";
        FileInputStream evidenceFile;

        // Command line argument variables.
        String argBnetFile = "-bnetFile";
        String argOutputFile = "-outputFile";
        String argEvidenceFile = "-evidenceFile";
        String argPrune = "-prune";
        String argThresholdForPruning = "-thresholdForPruning";
        String argSortVariablesInTrees = "-sortVariables";
        String argInterestVar = "-interestVar";
        String bnetFileString = null;
        String outputFileString = null;
        String evidenceFileString = null;
        String interestVar = null;
        int pruneAfterAddingInteger = 0;
        double thresholdForPruningDouble = 0.0;
        boolean sortVariablesInTreesBoolean = true;
        CmdLineArguments params = new CmdLineArguments();

        try {
            params.addArgument(argBnetFile, argumentType.s, "", "The filename of the Bnet (.elv format). No default value, must be provided.");
            params.addArgument(argOutputFile, argumentType.s, "", "The filename for the output results. No default value, must be provided.");
            params.addArgument(argEvidenceFile, argumentType.s, "", "The filename of the Evidence (.evi format). No default value, it is optional.");
            params.addArgument(argInterestVar, argumentType.s, "", "Name of the variable of interest. If no -interestVar option is used then all non-observed variables are included.");
            params.addArgument(argPrune, argumentType.i, "0", "Kind of pruning method to use. Default value is 0 (NOPRUNING)."
                    + "Possible values: 0 (NOPRUNING), 1 (KullbackLeiblerDistance)");
            params.addArgument(argThresholdForPruning, argumentType.d, "0.0", "The threshold if we use a pruning method for the binary probability trees). Default value is 0.0. It is optional.");
            params.addArgument(argSortVariablesInTrees, argumentType.b, "true", "Control if we sort the variables in the initial relations. Possible values: true or false");
            params.parseArguments(args);
            params.print();
            bnetFileString = params.getString(argBnetFile);
            outputFileString = params.getString(argOutputFile);
            evidenceFileString = params.getString(argEvidenceFile);
            interestVar = params.getString(argInterestVar);
            pruneAfterAddingInteger = params.getInteger(argPrune);
            thresholdForPruningDouble = params.getDouble(argThresholdForPruning);
            sortVariablesInTreesBoolean = params.getBoolean(argSortVariablesInTrees);
        } catch (CmdLineArgumentsException ex) {
            params.printHelp();
            System.exit(1);
        }

        if (bnetFileString.equalsIgnoreCase("")) {
            System.out.println(argBnetFile + " argument not found, you must specify one!!!");
            params.printHelp();
            System.exit(1);
        }
        if (outputFileString.equalsIgnoreCase("")) {
            System.out.println(argOutputFile + " argument not found, you must specify one!!!");
            params.printHelp();
            System.exit(1);
        }

        // Read the network
        b = Network.read(bnetFileString);
        if (evidenceFileString.equalsIgnoreCase("")) {
            e = new Evidence();
        } else {
            evidenceFile = new FileInputStream(evidenceFileString);
            e = new Evidence(evidenceFile, b.getNodeList());
        }

        // Creates the object for evaluating
        ve = new VEWithPTreeCredalSet((Bnet) b, e);

        if (pruneAfterAddingInteger > 0) {
            ve.setPruningMethod(pruneAfterAddingInteger, thresholdForPruningDouble);
        }

        if (sortVariablesInTreesBoolean) {
            ve.setSortVariablesInTrees(true);
        } else {
            ve.setSortVariablesInTrees(false);
        }

        if (!interestVar.equals("")) {
            ve.insertVarInterest(b.getNode(interestVar));
        }
        ve.obtainInterest();

        System.out.println("PROPAGATING ...");
        ve.propagate(outputFileString);
    }
}
