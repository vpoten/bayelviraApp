package elvira.inference.clustering.lazyid;

import elvira.IDiagram;
import elvira.Network;
import elvira.Relation;
import elvira.potential.PotentialTree;
import elvira.potential.LogicalExpression;
import elvira.parser.ParseException;
import java.io.IOException;

public class LazyPropagationIDWithPTAC extends LazyPropagationIDWithPT {

    /**
     * Class constructor: it receives the ID to solve
     * @param diag
     * @param threshold
     * @param triangulationCriteria used for triangulation
     * @param  propagationCriteria used for removing variables: with triangulation order or
     *                 with message passing schema
     * @param eliminationCriteria used for removing variables in the cliques: offline-online
     * @param debug flag
     * @param statistics flag
     */
    public LazyPropagationIDWithPTAC(IDiagram diag, double threshold, int triangulationCriteria, int propagationCriteria,
                                   int eliminationCriteria,boolean debugFlag, boolean statisticsFlag) {
        super(diag,threshold,triangulationCriteria,propagationCriteria,eliminationCriteria,debugFlag,statisticsFlag);

        if (generateDebugInfo) {
           System.out.println("LazyPropagationIDWithPTAC:  class Constructor ----- BEGIN");
        }
        
        // Set method name
        setMethod("LayPropagationIDWithPTAC");
        
        if (generateDebugInfo) {
           System.out.println("LazyPropagationIDWithPTAC:  class Constructor ----- END");
        }
    }
    
    /**
     * Private method for building a tree node
     * @Override method in LazyPropagationID 
     */
    private void buildStrongJunctionTree() {
        if (generateDebugInfo) {
           System.out.println("LazyPropagationIDWithPTAC:  buildStrongJunctionTree ----- BEGIN");
        }
       
        tree = new StrongJunctionTreeWithPTAC((IDiagram)network, thresholdForPrunning,triangulationCriteria, 
                propagationCriteria,variableEliminationCriteria, generateDebugInfo, 
                generateStatistics);
        
        // If it is needed, set the reference to the statistics objetc
        // for the tree
        if (generateStatistics){
           tree.setStatistics(statistics);
        }
        
        if (generateDebugInfo) {
           System.out.println("LazyPropagationIDWithPTAC:  buildStrongJunctionTree ----- END");
        }
    }

    /**
     * Public method for making the propagation
     */
    public void propagate() {
        if (generateDebugInfo) {
           System.out.println("LazyPropagationIDWithPTAC:  propagate ----- BEGIN");
        }
        
        // Make the tree for the propagation
        buildStrongJunctionTree();
        
        // Set the properties for the evaluation
        ((StrongJunctionTreeWithPTAC) tree).setMinimum(minimum);
        ((StrongJunctionTreeWithPTAC) tree).setMaximum(maximum);
        ((StrongJunctionTreeWithPTAC) tree).propagate();
        
        if (generateDebugInfo) {
           System.out.println("LazyPropagationIDWithPTAC:  propagate ----- END");
        }
    }

    /**
     * Transforms one of the original relations into another one whose values
     * are of class <code>PotentialTree</code>, and adding the effect of the
     * possible constraints. This is done for normal relations, but not for
     * constraints relations
     * @ param r the <code>Relation</code> to be transformed.
     */
    public Relation transformInitialRelation(Relation r) {
        PotentialTree potTree;
        LogicalExpression logExp;
        Relation rNew;
        double minimum;
        double maximum;

        if (generateDebugInfo) {
           System.out.println("LazyPropagationIDWithPTAC:  transformInitialRelation ----- BEGIN");
        }
        
        // Call the transformInitialRelation of LazyPropagationIDWithPT
        if (r.getKind() != Relation.CONSTRAINT){
           r = super.transformInitialRelation(r);
        }
        else{
            // Now consider the use of constraints
            // May be the constraint is not evaluated
            logExp = (LogicalExpression) (r.getValues());
            potTree = logExp.getResult();
            if (potTree == null) {
                logExp.evaluate();
                potTree = logExp.getResult();
            }

            // Anyway, work with it to compact its contents
            potTree = potTree.sortAndBound(0L);
            //potTree = potTree.sortAndBoundWithoutRestrict(0L);
            //r.setValues(potTree);
            logExp.setResult(potTree);
        }
        
        if (generateDebugInfo) {
           System.out.println("LazyPropagationIDWithPTAC:  transformInitialRelation ----- END");
        }

        // Return r
        return r;
    }

    public static void main(String[] args) throws ParseException, IOException {
        String networkName = null;
        boolean debug = false;
        boolean statistics = false;
        int triangulationCriteria = -1;
        int propagationCriteria = -1;
        int eliminationCriteria = -1;
        double threshold=0.0;
        int i;

        // Check the arguments passed to the program
        if (args.length < 2) {
            // Show how the program must be called
            usage();
        } else {
            // Consider the arguments one by one
            i = 0;
            while (i < args.length) {
                if (args[i].equals("-net")) {
                    networkName = args[i + 1];
                    i = i + 2;
                } else {
                    if (args[i].equals("-g")) {
                        debug = true;
                        i++;
                    } else {
                        if (args[i].equals("-s")) {
                            statistics = true;
                            i++;
                        } else {
                            if (args[i].equals("-t")) {
                                triangulationCriteria = Integer.parseInt(args[i + 1]);
                                i = i + 2;
                            } else {
                                if (args[i].equals("-e")) {
                                    eliminationCriteria = Integer.parseInt(args[i + 1]);
                                    i = i + 2;
                                } else {
                                    if (args[i].equals("-p")) {
                                        propagationCriteria = Integer.parseInt(args[i + 1]);
                                        i = i + 2;
                                    } else {
                                        if(args[i].equals("-tp")){
                                            threshold = Double.parseDouble(args[i+1]);
                                            i=i+2;
                                        }
                                        else{
                                           usage();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // If everything is ok makes the evaluator
        Network net = Network.read(networkName);
        LazyPropagationIDWithPTAC evaluator = new LazyPropagationIDWithPTAC((IDiagram) net, threshold, 
           triangulationCriteria, propagationCriteria, eliminationCriteria,debug, statistics);

        // Set the name for the statistics file
        String base = networkName.substring(0, networkName.lastIndexOf('.'));
        base = base.concat("_LazyPropagationIDWithPTAC_data");
        evaluator.statistics.setFileName(base);

        // Make the propagation
        evaluator.propagate();

        // Now check if all the decision tables are computed
        evaluator.checkDecisionTables();

        // Print the results
        // evaluator.tree.printResults();
    }

    /**
     * Method to show how the program must be called
     */
    private static void usage() {
        System.out.println("Use: LazyPropagationIDWithPTAC -net iDiagramFile");
        System.out.println("       [-g] (generate debug information) ");
        System.out.println("       [-s] (generate statistics)");
        System.out.println("       [-t] (triangulation criteria)");
        System.out.println("       -tp (threshold for prunning)");
        System.out.println("       [-p] (propagation criteria: direct elimination (1), message passing (2))");
        System.out.println("       [-e] (elimination criteria: online triangulation (2), offline triangulation (1))");
        System.exit(0);
    }
}
