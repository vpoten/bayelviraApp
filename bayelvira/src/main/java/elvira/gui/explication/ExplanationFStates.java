/* ExplanantionFStates.java */

package elvira.gui.explication;

import java.util.Vector;
import elvira.*;
import elvira.potential.*;
import elvira.gui.*;

/**
 * This class is used to draw an expanded Finite States node
 * @author  Carmen Lacave
 */

public class ExplanationFStates extends FiniteStates{

	/** Global variables needed in several methods*/
    private Bnet bnet;
    private InferencePanel infpanel;

    
    /** The following are some properties needed to paint an expanded node
     */
	/** The node to be expanded. It must be a finite states node */
    private FiniteStates n;
	
    /** The list of cases to be graphically displayed in each expanded node */  
    private CasesList l;
    
    /** The names of the states of the node */
    private String[] estados; 
    
    /** The prior probabilities of each state of the node */
    private double[] priorprob;
    
    /** The posterior probabilities of each state of the node after propagating some evidence*/
	private double[] postprob;


	public ExplanationFStates(InferencePanel infp, Bnet b,FiniteStates node){
        n=node;
        bnet=b;
        infpanel=infp;
        l=infpanel.getCasesList();
        setStates(node.getStates());
        setName(node.getName());
        setPriori();
    }

    /**
     * Create an ExplanationFStates for a utility node. It will have only one state (EU).
	 * @param panel
	 * @param bayesNet
	 * @param continuous
	 */
	public ExplanationFStates(InferencePanel panel, Bnet bayesNet, Continuous util) {
		Vector states;
		states = new Vector();
		states.addElement("\"EU\"");
        n=new FiniteStates(util.getName(),util.getPosX(),util.getPosY(),new Vector(states));
        bnet=bayesNet;
        infpanel=panel;
        l=infpanel.getCasesList();
        this.setStates(states);
        setName(util.getName());
        setPriori();
		
		// TODO Auto-generated constructor stub
	}

	/** This method saves the prior probabilities in the priorprob array */
	private void setPriori() {

		Potential p;

		Vector potentials;
		int kindOfNode;

		if (bnet.getIsCompiled()) {
			if (bnet.getClass() == Bnet.class) {
				potentials = bnet.getCompiledPotentialList();
			} else {// IDiagram or IDWithSVNodes
				potentials = ((IDiagram) bnet).getCpn()
						.getCompiledPotentialList();
			}

		} else
			potentials = new Vector();

		boolean found = false;

		for (int i = 0; (i < potentials.size()) && (found == false); i++) {

			p = (Potential) potentials.elementAt(i);
			/* andrew */if (n.getName().equals(
					((Node) p.getVariables().elementAt(0)).getName())) {
				found = true;
				kindOfNode = n.getKindOfNode();
				if ((kindOfNode == Node.CHANCE)
						|| (kindOfNode == Node.DECISION)) {
					if (p.getClass() == PotentialContinuousPT.class) {
						PotentialTree aux2 = new PotentialTree(
								(PotentialContinuousPT) p);
						PotentialTable aux1 = new PotentialTable(aux2);
						priorprob = aux1.getValues();
					} else
						priorprob = ((PotentialTable) p).getValues();
				}
			}
		}// for
	}

    public void setPoste(double[] d){
        int numestados=n.getNumStates();
        postprob=new double[numestados];
        for (int i=0;i<numestados;++i)
             postprob[i]=d[i];
    }
    
    public Bnet getBayesNet(){
    	return bnet; 
    }
    
    public CasesList getCasesList(){
    	return l;
    }

    public double[] getPriori(){
        return priorprob;
    }

    public double[] getPoste(){
        Case c=l.getActiveCase();
        return c.getProbOfNode(n);
    }

    public double[] getDistribution(int i){
        //devuelve la distribución de las guardadas en la posición i
        Case c=l.getCaseNum(i);
        return c.getProbOfNode(n);
    }


    public int getDistributionSize(){
        //devuelve la distribución de las guardadas en la posición i
        return l.getNumStoredCases()-1;
    }

    public int getMaxProbState(double[] prob){
        //calcula el índice del valor máximo almacenado en el array prob, que se supone que
        //contendrá los valores de las probabilidades. Si hubiera más de un máximo, devuelve -1
        int index=-1;
        double suma=0;
        for (int i=0; i<prob.length; i++)
            suma=suma+prob[i];
        double valmax=suma/((double)prob.length);
        int numiguales=0;
        for (int i=0; i<prob.length; i++){
            if (prob[i]>valmax){
               index=i;
               valmax=prob[index];
               numiguales=1;
            }
            else if (prob[i]==valmax)
                     numiguales++;

        }
        if (numiguales>1)
            index=-1;
        return index;
    }


}