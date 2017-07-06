
package elvira.inference.elimination.impreciseprob;

import elvira.Bnet;
import elvira.Evidence;
import elvira.FiniteStates;
import elvira.Network;
import elvira.Relation;
import elvira.inference.elimination.VariableElimination;
import elvira.parser.ParseException;
import elvira.potential.CredalSet;
import elvira.potential.PTreeCredalSet;
import elvira.potential.Potential;
import elvira.potential.PotentialConvexSet;
import elvira.potential.PotentialInterval;
import elvira.potential.PotentialIntervalTable;
import elvira.potential.PotentialTable;
import elvira.potential.PotentialTree;
import elvira.potential.binaryprobabilitytree.PotentialBPTreeCredalSet;
import elvira.tools.CmdLineArguments;
import elvira.tools.CmdLineArguments.CmdLineArgumentsException;
import elvira.tools.CmdLineArguments.argumentType;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Approximate variable elimination algorithm for credal networks using binary
 * probability trees.
 * @author acu
 */
public class VEWithBPTreeCredalSet extends VariableElimination {

    /**
     * Enumeration with the different kinds of pruning methods.
     */
    public static enum pruningMethods {NO_PRUNING, KULLBACK_LEIBLER_DISTANCE};
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

    /**
     * Creates a new instance of VEWithBPTreeCredalSet for a given
     * Bayesian network and some evidence.
     * @param b A <code>Bnet</code>.
     * @param e The evidence.
     */
    public VEWithBPTreeCredalSet(Bnet b, Evidence e) {
        super(b, e);
    }

    /** 
     * Creates a new instance of VEWithBPTreeCredalSet for a given Bayesian network.
     * @param b A <code>Bnet</code>.
     */
    public VEWithBPTreeCredalSet(Bnet b) {
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
        if(prunMethod == 0) {
            pruningMethod = pruningMethods.NO_PRUNING;
        }
        else if(prunMethod == 1) {
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
     * Transforms the Potential of one of the original relations. If the Potential
     * is a PotentialTree, a PotentialTable or a PotentialInterval then it is
     * transformed into a PotentialBPTreeCredalSet.
     * If the Potential is a PotentialBPTreeCredalSet then it is not modified.
     * Otherwise an error is produced.
     * @param r The <code>Relation</code> to be transformed.
     */
    @Override
    public Relation transformInitialRelation(Relation r) {
        // Converts a PotentialTree, a PotentialTable or a PotentialInterval
        // into a PTreeCredalSet
        if(r.getValues() instanceof PTreeCredalSet || r.getValues() instanceof PotentialTree
                || r.getValues() instanceof PotentialTable || r.getValues() instanceof PotentialInterval) {
            Relation rNew = new Relation();
            rNew.setVariables(r.getVariables().copy());
            rNew.setKind(r.getKind());

            PotentialBPTreeCredalSet bpTreeCS;
            if(r.getValues() instanceof PTreeCredalSet)
                bpTreeCS = new PotentialBPTreeCredalSet(r.getValues());
            else if(r.getValues() instanceof PotentialTree) {
                PotentialTable pt = new PotentialTable(r.getValues());
                bpTreeCS = new PotentialBPTreeCredalSet(pt);
            }
            else if(r.getValues() instanceof PotentialTable)
                bpTreeCS = new PotentialBPTreeCredalSet((PotentialTable)r.getValues());
            else
                bpTreeCS = new PotentialBPTreeCredalSet((PotentialInterval)r.getValues());

            // Sorts variables if needed.
            bpTreeCS.removeTransNotInTree(); 
            if(sortVariablesInTrees)
                bpTreeCS.sort();
            // Prunes the tree if needed.
            if(pruningMethod == pruningMethods.KULLBACK_LEIBLER_DISTANCE) {
                bpTreeCS.limitBound(thresholdForPruning);
                // Removes variables not present in the tree from the potential
                bpTreeCS.removeVarsNotInTree(false, currentRelations,interest);
            }
            rNew.setValues(bpTreeCS);
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
     * Transforms a <code>PotentialBPTreeCredalSet</code> obtained as a result of
     * adding one variable (<code>FiniteStates</code>).
     * @param potential The new <code>PotentialBPTreeCredalSet</code>.
     */
    @Override
    public Potential transformAfterAdding(Potential potential) {
        int k, pos;
        FiniteStates y;
        PotentialBPTreeCredalSet pot = null;
        pot = (PotentialBPTreeCredalSet) potential;

        if (sortVariablesInTrees) {
            pot.sort();
        }

        if (pruningMethod == pruningMethods.KULLBACK_LEIBLER_DISTANCE) {
            pot.limitBound(thresholdForPruning);
            pot.removeVarsNotInTree(false, currentRelations, interest);
        }

        return pot;
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
            ((PotentialBPTreeCredalSet)pot).removeVarsNotInTree(true, currentRelations,interest);
            // Converts the potential into a PotentialConvexSet.
            pot = new PotentialConvexSet((CredalSet)pot);
            // Normalizes each extreme point.
            pot.normalize();
            // Converts to PotentialIntervalTable.
            pot = new PotentialIntervalTable((CredalSet)pot);
            results.setElementAt(pot, i);
        }
    }


    // Command line argument variables.
    private static final String argBnetFile = "-bnetFile";
    private static final String argOutputFile = "-outputFile";
    private static final String argEvidenceFile = "-evidenceFile";
    private static final String argPruneAfterAdding = "-pruneAfterAdding";
    private static final String argThresholdForPruning = "-thresholdForPruning";
    private static final String argSortVariablesInTrees = "-sortVariables";
    private static final String argInterestVar = "-interestVar";

    /**
     * Main method.
     * @param args Program arguments.
     * @throws ParseException
     * @throws IOException
     */
    public static void main(String args[]) throws ParseException, IOException {
        Network b;
        Evidence e;
        FileInputStream evidenceFile;
        VEWithBPTreeCredalSet ve;
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
            params.addArgument(argPruneAfterAdding, argumentType.i, "0", "Kind of pruning method after adding a variable. Default value is 0 (NOPRUNING)." +
                 "Possible values: 0 (NOPRUNING), 1 (KullbackLeiblerDistance)");
            params.addArgument(argThresholdForPruning, argumentType.d, "0.0", "The threshold if we use a pruning method for the binary probability trees). Default value is 0.0. It is optional.");
            params.addArgument(argSortVariablesInTrees, argumentType.b, "true", "Control if we sort the variables in the initial relations. Possible values: true or false");

            params.parseArguments(args);
            params.print();
            bnetFileString = params.getString(argBnetFile);
            outputFileString = params.getString(argOutputFile);
            evidenceFileString = params.getString(argEvidenceFile);
            interestVar = params.getString(argInterestVar);
            pruneAfterAddingInteger = params.getInteger(argPruneAfterAdding);
            thresholdForPruningDouble = params.getDouble(argThresholdForPruning);
            sortVariablesInTreesBoolean = params.getBoolean(argSortVariablesInTrees);
        }
        catch(CmdLineArgumentsException ex) {
            params.printHelp();
            System.exit(1);
        }

        if(bnetFileString.equalsIgnoreCase("")) {
            System.out.println(argBnetFile + " argument not found, you must specify one!!!");
            params.printHelp();
            System.exit(1);
        }
        if(outputFileString.equalsIgnoreCase("")) {
            System.out.println(argOutputFile + " argument not found, you must specify one!!!");
            params.printHelp();
            System.exit(1);
        }

        b = Network.read(bnetFileString);
        if(evidenceFileString.equalsIgnoreCase("")) {
            e = new Evidence();
        }
        else {
            evidenceFile = new FileInputStream(evidenceFileString);
            e = new Evidence(evidenceFile, b.getNodeList());
        }

        ve = new VEWithBPTreeCredalSet((Bnet)b, e);

        if(pruneAfterAddingInteger > 0) {
            ve.setPruningMethod(pruneAfterAddingInteger, thresholdForPruningDouble);
        }

        if(sortVariablesInTreesBoolean)
            ve.setSortVariablesInTrees(true);
        else
            ve.setSortVariablesInTrees(false);

        if(!interestVar.equals("")) {
            ve.insertVarInterest(b.getNode(interestVar));
        }
        ve.obtainInterest();

        System.out.println("PROPAGATING ...");
        ve.propagate(outputFileString);
    }
}
