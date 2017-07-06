package elvira.inference.clustering.IncrementalCompilation;

import elvira.*;
import elvira.inference.clustering.*;
import java.util.ArrayList;


/**
 * ICModification.java
 * This class implements the Modification for Incremental Compilation process.
 *
 * Created:  23/12/2003
 *
 * @author julia
 * @version 1.0
 */

abstract public class ICModification{

    //Since it is an abstract class, the kind of modification is not defined yet (set to -1)
    static int kind = -1;
    //Just for debugging tasks
    static boolean debug = false;
    
    
    /**
     * This method carries out the modification over the moral graph.
     */
    public abstract LinkList ModifyMoralGraph(Graph g);
    
    
    /**
     * Same method as before used for experimentations.
     */
    public abstract LinkList ModifyMoralGraph(Graph g,boolean GIC);
    
    /**
     * This method marks in the MPS Tree the affected MPSs by this modification
     * @param JT is the Junction Tree
     * @param MPST is the Maximal Prime Subgraph Tree
     * @param MM will contained the list of marked subgraphs
     */
    public abstract void MarkAffectedMPSs(JoinTree JT,JoinTree MPST,ArrayList MM);
    
    
    /** access method that returns the kind of modication
     */
    public int getKind()
    {
        return kind;
    }
}


