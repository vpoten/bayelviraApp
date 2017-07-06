package elvira.inference.clustering.IncrementalCompilation;

import elvira.inference.clustering.*;
import elvira.*;
import java.util.Vector;
import java.util.ArrayList;

/**
 * ICModificationAddNode.java
 * This class implements the single modification of adding a node for Incremental Compilation process.
 * 
 * Created:  23/12/2003
 * 
 * @author  Julia
 * @version 1.0
 */
public class ICModificationAddNode extends ICModification{
    
    /** This field identifies the node we want to add in this
     * modification*/
    private Node myNode; 
    static int kind = 0;
    
    /** This field will represent the relevant links for the modification 
     (L in the paper algorithms)*/
    public LinkList ll;
    
    /** It will be necessary to know the IncrementalCompilation we are using this modification for.
     *  With this information we can access the actual network, moral graph and both junction
     *  and MPS trees.
     */
     IncrementalCompilation myIC;
     
    /** Creates a new instance of ICModificationAddLink */
    public ICModificationAddNode() {
      
    }
    
    /** Creates a new instance of ICModificationAddNode.
     * @param myN is a <code>Node</code> we want to add in this ICModification
     * @param myIC is the corresponding <code>IncrementalCompilation</code>
     */
    public ICModificationAddNode(Node myN,IncrementalCompilation ic) {
       
        //debugging
        if (debug)
        { 
            System.out.println("My kind of class " + kind);
        }
        myNode = myN;
        myIC = ic;
    }
    
    /** We are going to add this new Node in both the moral Graph and the 
     * Bayesian network of the Incremental Compilation
     */
    public LinkList ModifyMoralGraph(Graph g){
        //When adding a node the relevant links is an empty set    
        ll = new LinkList();
        if (debug)
        {
           System.out.println("We have entered ModifyMoralGraph in class ICModificationADD_NODE");
        }
        //Insert the node in the moral graph (different just in case)
        g.getNodeList().insertNode(myNode.copy());
        //Insert the node in the network
        myIC.getBNET().getNodeList().insertNode(myNode);
        return ll;
    }
    
    
   /** When adding a node there is not a strict marking of the affected MPSs, 
     * we will add both isolated MPS and clique containing this node and emptily
     * separated from the MPS/Clique in position 0 (is a convention)*/
    public void MarkAffectedMPSs(JoinTree JT, JoinTree MPST, ArrayList MM){
        //In the subclass this method corresponds to AddNode(X)
        NodeJoinTree newClique;
        NodeJoinTree newMPS;
        Family newF;
        Vector v = new Vector();
        v.addElement(myNode);
        
        Relation r = new Relation(v);
        newClique = new NodeJoinTree(r);
        //We have to connect it although separator is the empty set. Randomly we choose node 0.
        //But this fact will have to be borne in mind in order to perform other/future operations.
        newClique.insertNeighbour(JT.elementAt(0));
        (JT.elementAt(0)).insertNeighbour(newClique);
        newClique.setIsMPS(false);
        int labelJ = JT.size();
        newClique.setLabel(labelJ);
        //And we add a new family
        newF = new Family(myNode,r);
        newClique.insertFamily(newF);
        JT.insertNodeJoinTree(newClique);
        
        r = new Relation(v);
        newMPS = new NodeJoinTree(r);
        //We have to connect it although separator is the empty set. Randomly we choose node 0.
        //The same convention we had for the JT is taken for the MPST
        newMPS.insertNeighbour(MPST.elementAt(0));
        (MPST.elementAt(0)).insertNeighbour(newMPS);
        newMPS.setIsMPS(true);
        int labelM = MPST.size();
        newMPS.setLabel(labelM);
        //And we add a new family
        newF = new Family(myNode,r);
        newMPS.insertFamily(newF);
        MPST.insertNodeJoinTree(newMPS);
        
        //Now we make both (JT and MPST) nodes correspond
        newClique.setCorrespondingMPS(newMPS);
        newMPS.makeCorrespondenceWithClique(newClique);
          
    }
    
    /* Same function as before, but with a boolean parameter used by the programmers
     * to perform simulated experiments. Default use is GIC set to false.
     */
    public LinkList ModifyMoralGraph(Graph g, boolean GIC) {
        myNode = myNode.copy();
        return this.ModifyMoralGraph(g);
    }    
  
    
}
