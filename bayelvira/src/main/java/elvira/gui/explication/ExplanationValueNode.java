/*
 * Created on 01-feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package elvira.gui.explication;

import java.util.Vector;

import elvira.Bnet;
import elvira.Continuous;
import elvira.FiniteStates;
import elvira.IDiagram;
import elvira.Node;
import elvira.gui.InferencePanel;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.potential.Potential;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.PotentialTable;
import elvira.potential.PotentialTree;

/**
 * @author Manolo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExplanationValueNode extends Continuous{
	/** Global variables needed in several methods*/
    private IDiagram id;
    private InferencePanel infpanel;

    
    /** The following are some properties needed to paint an expanded node
     */
	/** The node to be expanded. It must be a continuous node (utility or sv node) */
    private Continuous node;
	
    /** The list of cases to be graphically displayed in each expanded node */  
    private CasesList l;
    
       
    /** The prior utility of the node */
    private double priorutil;
    
    /** The posterior utility  of the node after propagating some evidence*/
	private double postutil;


	public ExplanationValueNode(InferencePanel infp, IDiagram id2,Continuous node2){
        node=node2;
        id=id2;
        infpanel=infp;
        l=infpanel.getCasesList();
        setPriori();
    }

  


	/** This method saves the prior utilities in the priorutil array */
	private void setPriori() {
		Potential p;
		Vector potentials;
		Node auxNode;
		CooperPolicyNetwork cpn = null;

		if (id.getIsCompiled()) {
			cpn = id.getCpn();
			potentials = cpn.getCompiledPotentialList();
		} else {
			potentials = new Vector();
		}

		boolean found = false;

		for (int i = 0; (i < potentials.size()) && (found == false); i++) {

			p = (Potential) potentials.elementAt(i);

			if (node.getName().equals(
					((Node) p.getVariables().elementAt(0)).getName())) {
				found = true;

				auxNode = (Node) (p.getVariables().elementAt(0));

				priorutil = (((PotentialTable) p)
						.convertProbabilityIntoUtility(cpn
								.getMinimumUtility(auxNode), cpn
								.getMaximumUtility(auxNode)).getValues())[0];

			}
		}
	}

 
    
    public IDiagram getIDiagram(){
    	return id; 
    }
    
    public CasesList getCasesList(){
    	return l;
    }

    public double getPrioriUtility(){
        return priorutil;
    }

    public double getPosteUtility(){
    	int posn=id.getNodeList().getId(node);
        Case c=l.getActiveCase();
        return c.getProbOfStateNode(posn,0);
    }

    public double getUtility(int i){
    	int posn=id.getNodeList().getId(node);
    	
        //devuelve la utilidad de las guardadas en la posición i de la lista de casos
        Case c=l.getCaseNum(i);
                
        if (posn!=-1) {
        	return c.getProbOfStateNode(posn,0);
        }
        else return 0.0;
    }


    public int getDistributionSize(){
        //devuelve la distribución de las guardadas en la posición i
        return l.getNumStoredCases()-1;
    }

 


	/**
	 * @return Returns the node.
	 */
	public Continuous getNode() {
		return node;
	}
}
