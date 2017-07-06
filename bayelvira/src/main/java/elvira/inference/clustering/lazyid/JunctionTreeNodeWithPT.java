
package elvira.inference.clustering.lazyid;

import elvira.NodeList;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;

public class JunctionTreeNodeWithPT extends JunctionTreeNode{

  /**
   * Constructor
   * @param tree 
   * @param variables
   * @param index
   */
  public JunctionTreeNodeWithPT(StrongJunctionTree tree,NodeList variables,int index){
    super(tree,variables,index);
  }

  /**
   * Method for building a new tree node
   * @param tree
   * @param clique nodelist to include in the node
   * @param index of the node
   */
  protected JunctionTreeNode buildTreeNode(StrongJunctionTree tree, 
                                         NodeList clique, int index){
    JunctionTreeNodeWithPT node=new JunctionTreeNodeWithPT(tree,clique,index);
    return node;
  }

  /**
   * Private method for post processing the utility potentials. For
   * PotentialTable this method does nothing, but with probability
   * potentials the variables will be sorted and a prunning will
   * be done if required
   * @param potential
   */
  protected Potential postProcessUtility(Potential potential){
    double minimum=((StrongJunctionTreeWithPT)tree).getMinimum();
    double maximum=((StrongJunctionTreeWithPT)tree).getMaximum();
    double threshold=((StrongJunctionTreeWithPT)tree).getThresholdForPrunning();
    PotentialTree finalPot=((PotentialTree)potential).sortUtilityAndPrune(minimum,
                                                      maximum,threshold);
    //PotentialTree finalPot=((PotentialTree)potential).sortUtilityAndPruneWithoutRestrict(minimum, maximum,threshold,PotentialTree.DISTANCE_WITH_CEROS);
    return finalPot;
  }

  /**
   * Private method for post processing the probability potentials. For
   * PotentialTable this method does nothing, but with probability
   * potentials the variables will be sorted and a prunning will
   * be done if required
   * @param potential
   */
  protected Potential postProcessProbability(Potential potential){
    double threshold=((StrongJunctionTreeWithPT)tree).getThresholdForPrunning();
    PotentialTree finalPot=((PotentialTree)potential).sortAndBound(threshold);
    //PotentialTree finalPot=((PotentialTree)potential).sortAndBoundWithoutRestrict(threshold);
    return finalPot;
  }
}
