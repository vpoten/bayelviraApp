/*
 * Explanation.java
 *
 * Created on 13 de noviembre de 2003, 12:20
 */

package elvira.gui.explication;

import elvira.potential.*;
import elvira.*;
import elvira.gui.*;
import elvira.gui.explication.plotFunction.*;
import java.util.*;


/**
 * This class contain the characterisctics necesary to explain visual and textually
 * the propagation result of continuous node. Symetric to ExplanationFStates class.
 * @author   andrew
 */ 

public class ExplanationContinuous extends Continuous {
    
    Continuous node;

    Bnet bnet;

    InferencePanel infpanel;

    CasesList l;

    G2Dint graph;
    
    /**
     *   Creates a new instance of Explanation 
     *  @param InferencePanel infp, the inference panel what show the propagation results.
     *  @param Benet b, the network what is propagated.
     *  @param Continuous n, the node what is explicated in this class
     */
    public ExplanationContinuous(InferencePanel infp, Bnet b,Continuous n) {
        
        node=n;
        infpanel=infp;
        bnet=b;
        l=infpanel.getCasesList();
        
    }
    /**
     *  Get the continuous node to explain.
     */
    public Continuous getNode(){
        
        return node;
    }
    
    /**
     *  Get the PotentialContinuousPT what contain node's propagation result
     */
    public PotentialContinuousPT getPCPTEvidence(){
        
        Vector v=infpanel.getPotentialList();
        for (int i=0; i<v.size(); i++){
            Node n =(Node)((Potential)v.elementAt(i)).getVariables().elementAt(0);
        
            if (n.getName().equals(node.getName())){
                
                return (PotentialContinuousPT)v.elementAt(i);
            }
        }
          
        if (isObserved()){
                Vector vec = new Vector();
                vec.add(new Double(node.getMin()));
                vec.add(new Double(node.getMax()));
                double x=infpanel.getCasesList().getCurrentCase().getEvidence().getContinuousValue(node);
                return new PotentialContinuousPT(node,vec,x);
        }
        
        return null;
    
    }
    
   /*
    * Show if node of explanation continuous is observed in the current case of inference panel
    */
    
    public boolean isObserved(){
        
        return infpanel.getCasesList().getCurrentCase().getIsObserved(node);
    }
    
    
    /**
     *  Get the PotentialContinuousPT what contain node's propagation result
     */
    public PotentialContinuousPT getPCPT(){
        
        Vector v=infpanel.getBayesNet().getCompiledPotentialList();
        for (int i=0; i<v.size(); i++){
            Node n =(Node)((Potential)v.elementAt(i)).getVariables().elementAt(0);
        
            if (n.getName().equals(node.getName())){
                
                return (PotentialContinuousPT)v.elementAt(i);
            }
        }
          
        
        
        return null;
    
    }
    
    public boolean isEvidenceIntroduced(){
        
        if (infpanel.getPotentialList()!=null)
                return true;
        else 
                return false;
        
    }
    /**
     *  Get the density function G2Dint associated to the explanation of a continuous node. 
     */
    
    public G2Dint getGraph(){
        
        return graph;
    }
    /**
     *  Set the density function G2Dint associated to the explanation of a continuous node. 
     */
        
    public void setGraph(G2Dint graph){
        
        this.graph=graph;
    }
    
    public CasesList getCasesList(){
        
        return l;
    }
    
    public InferencePanel getInferencePanel(){
        return infpanel;
        
    }
    
}
