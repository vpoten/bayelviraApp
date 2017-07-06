package elvira.inference.clustering.IncrementalCompilation;
import elvira.inference.clustering.*;
import elvira.*;
import java.util.ArrayList;

/**
 * ICModificationRemoveNode.java
 * This class implements the single modification of removing a node for Incremental Compilation process 
 * (involves removing incident links first).
 * Created:  23/12/2003
 *
 * @author  Julia
 * @version 1.0
 */

public class ICModificationRemoveNode extends ICModification{
    private Node myNode;
    static int kind = 2;
    NodeJoinTree Mx;
    JoinTree JTree;
    JoinTree MTree;
    
    /** This field will represent the relevant links for the modification 
     (L in the paper algorithms)*/
    public LinkList ll;
    
    /** It will be necessary to know the IncrementalCompilation we are using this modification for.
     *  With this information we can access the actual network, moral graph and both junction
     *  and MPS trees.
     */
     IncrementalCompilation myIC;
    
    /** Creates a new empty instance of ICModificationRemoveNode */
    public ICModificationRemoveNode() {
        
    }
    
    /** The constructor. It initializes the values for start the 
    *  modification actions. 
    * @param myN is the (disconnected) <code>Node</code> we want to remove
    * @param myIC is the corresponding <code>IncrementalCompilation</code>
    */
   
    public ICModificationRemoveNode(Node myN,IncrementalCompilation ic) {
        
        myNode = myN;
        myIC = ic;
        int i;
        if (debug)
        { 
           System.out.println("My kind of class " + kind);
        }
    }
    
    
    /** This method deletes the node in both the moral graph and the network. Notice that
     *  when the node to be deleted is not disconnected, its incident links are removed first
     *  by means of ICModificationRemoveLink.
     *
     *  @param g is the <code>Graph</code> we are going to modify
     *  @param GIC will be true just for particular generated experiments done by IC designers.
     *  @return the list of links that are related to this modification
     */
   public LinkList ModifyMoralGraph(Graph g,boolean GIC){
       
       if (GIC)
       {    
       int i;
       ArrayList prevMods = new ArrayList();
       myNode = myIC.getBNET().getNode(myNode.getName());
       
       //before removing a node we remove the incident links
       LinkList incidentLinks=myNode.getParents();
       for(i=0;i<incidentLinks.size();i++)
       {
          prevMods.add(new ICModificationRemoveLink(incidentLinks.elementAt(i),myIC));
       }
       incidentLinks=myNode.getChildren();
       for(i=0;i<incidentLinks.size();i++)
       {
           prevMods.add(new ICModificationRemoveLink(incidentLinks.elementAt(i),myIC));
       }
       myIC.processRemoveLinksModifications(prevMods,GIC);
             
       }
             
      return this.ModifyMoralGraph(g);
        
      }
    
    
    /** This method deletes the node in both the moral graph and the network
     * 
     *  @param g is the <code>Graph</code> we are going to modify.
     *  @return the list of links that are related to this modification
     */
    public LinkList ModifyMoralGraph(Graph g){
        //relevant links list is empty for the modification of deleting a node
        ll = new LinkList();
        
        if (debug)
        { 
            System.out.println("We have entered ModifyMoralGraph in class ICModificationREMOVE_NODE");
        }
        //Remove node from graph and network
        try{
            myIC.getBNET().removeNode(myNode);
            myNode = g.getNodeList().getNode(myNode.getName());
            g.removeNode(myNode);
            
            }
            catch (InvalidEditException iee)
            {
                System.out.println("Problems when removing node " + myNode.getName() + " :: " + iee);
            }
        return ll;
    }
    
    
    /** When deleting a node whose induces links has necessarily deleted before
     * we don't mark any MPS, we just delete the associated clique/MPS in the 
     * join/MPS trees and re-number the cliques/MPS is the tree.
     */
    public void MarkAffectedMPSs(JoinTree JT, JoinTree MPST, ArrayList MM){
        //In the subclass this method corresponds to RemoveNode(X) which is recursive,
        //so here we just make the first call
        Mx = myIC.getCliqueWithFamily(myNode).getCorrespondingMPS();
        JTree = JT;
        MTree = MPST;
        removeNode(Mx,null,MM);
        
    }
    
    /** This method removes the node X (myNode) from the Cluster 
     * and goes recursively inside all the neighbours
     * @param MPSx is the Cluster from which we want to remove this node X
     * @param MPSy is the Cluster that previously call to this function
     * @param markedM is the list of MarkedMPSs
     */
    public void removeNode(NodeJoinTree MPSx,NodeJoinTree MPSy,ArrayList markedM)
    {
        int i;
        NodeList sepXZ;
        NeighbourTree nt;
        NodeJoinTree MPSz;
        NeighbourTreeList ntl;
        
        //1.-Remove node from Mx
        //I'm going to work directly on the variables of the relation.
        //Maybe necessary to modify something else in the relation??
        //It doesn't seem necessary to mark this subgraph since this has been deleted
                
        MPSx.getNodeRelation().getVariables().removeNode(myNode);
        //To avoid problems the node is removed in both nodeRelation and variables
        MPSx.getVariables().removeNode(myNode);
        
        //If this subgraph only had one variable (the deleted one)
        if (MPSx.getNodeRelation().getVariables().size()==0)
         {
             //Remove the MPS and its separators
              ntl = Mx.getNeighbourList();
              for(i=ntl.size()-1;i>=0;i--)
             {
                nt = ntl.elementAt(i); 
                MPSz = nt.getNeighbour();
                MPSz.removeNeighbour(Mx);
                Mx.removeNeighbour(MPSz);
             }
             
             //We need to remove from the JT the corresponding
             //clique with a single variable.
             NodeJoinTree Jx = (NodeJoinTree)Mx.getCliques().get(0);
             NodeJoinTree Jz;
             ntl = Jx.getNeighbourList();
             for(i=ntl.size()-1;i>=0;i--)
             {
                nt = ntl.elementAt(i); 
                Jz = nt.getNeighbour();
                Jz.removeNeighbour(Jx);
                Jx.removeNeighbour(Jz);
             }
             JTree.removeNodeJoinTree(Jx);
             MTree.removeNodeJoinTree(Mx);
         }
        
    }
    
        
}
