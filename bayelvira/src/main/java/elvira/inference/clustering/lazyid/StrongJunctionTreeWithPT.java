package elvira.inference.clustering.lazyid;

import elvira.IDiagram;
import elvira.NodeList;
import elvira.Graph;

public class StrongJunctionTreeWithPT extends StrongJunctionTree {

    /**
     * Data member to store the threshold for prunning numerical
     * trees
     */
    protected double thresholdForPrunning;
    /**
     * Data member to store the minimum and maximum values for utility
     */
    protected double minimum;
    protected double maximum;

    /**
     * Constructor receiving an ID as argument
     * @param IDiagram to triangulate
     * @param threshold
     * @param triangulationCriteria used for triangulation
     * @param propagationCriteria message passing or direct elimination
     *        of variables
     * @param variableEliminationCriteria using on-line triangulation
     *        or off-line triangulation
     * @param generateDebugInfo debug information flag
     * @param generateStatistics statistics information flag
     */
    public StrongJunctionTreeWithPT(IDiagram diag, double threshold, int triangulationCriteria,
                             int propagationCriteria, int variableEliminationCriteria,
                             boolean generateDebugInfo, boolean generateStatistics) {
        super(diag, triangulationCriteria, propagationCriteria,
              variableEliminationCriteria, generateDebugInfo, 
              generateStatistics);
        thresholdForPrunning=threshold;
    }

    /**
     * Private method for building a tree node
     * @param tree
     * @param clique to include in the node
     * @param index
     * @Override buildTreeNode defined on StringJunctionTree
     */
    public JunctionTreeNode buildTreeNode(StrongJunctionTree tree, NodeList clique, int index) {
        return new JunctionTreeNodeWithPT(tree, clique, index);
    }

    /**
     * Private method for adding constraint links between the variables
     * related to the constraints
     * @param graph to modify
     */
    public void addConstraintLinks(Graph graph) {
    }

    /**
     * Public method for getting the value of thresholdForPrunning
     */
    public double getThresholdForPrunning() {
        return thresholdForPrunning;
    }
    
    /**
     * Method for getting maximum
     */
    public double getMaximum(){
       return maximum;
    }
    
    /**
     * Method for getting the minimum
     */
    public double getMinimum(){
       return minimum;
    }
    
    /**
     * Method for setting the maximum
     */
    public void setMaximum(double maximum){
       this.maximum=maximum;
    }
    
    /**
     * Method for setting the minimum
     */
    public void setMinimum(double minimum){
       this.minimum=minimum;
    }
}
