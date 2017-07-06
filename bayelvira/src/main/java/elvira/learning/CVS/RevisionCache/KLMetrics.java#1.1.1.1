package elvira.learning;

import elvira.NodeList;
import elvira.potential.PotentialTable;
import elvira.Bnet;
import elvira.FiniteStates;
import elvira.database.DataBaseCases;

/**
 * KLMetrics.java
 * This class implements the Kullback-Leibler metrics.
 *
 * Created: Mon Nov  8 11:09:40 1999
 *
 * @author P. Elvira
 * @version  1.0
 */

public class KLMetrics extends Metrics {
    
    /** Constructors methods. **/

    public KLMetrics() {
	setData(null);
    }
    
    public KLMetrics(DataBaseCases data){
	setData(data);
    }


    /**
     * This method compute the score for a Bayes Net from data base of cases.
     * We use score = g(bnet,cases) = sum_i K-L(xi,xj|MIN_DSEP(xi,xj)).
     * where the MIN_DSEP is the smallest subset that d-separates xi from xj.
     * @param Bnet b.
     * @return double.
     */

    public double score (Bnet b){
	
	NodeList vars,varsXYZ,minDsep,nbX;
	int i,j;
	double sum = 0.0;
	double valscore;
	FiniteStates nodeX,nodeY;

	vars = b.getNodeList();

	for(i=0; i< vars.size()-1; i++){
	    nodeX = (FiniteStates) vars.elementAt(i);
	    nbX = b.neighbours(nodeX);
	    for(j= i+1; j<vars.size(); j++){
		nodeY = (FiniteStates) vars.elementAt(j);
		if(nbX.getId(nodeY) == -1){
		    minDsep = b.minimunDSeparatingSet(nodeX,nodeY);
		    varsXYZ = new NodeList();
		    varsXYZ.insertNode(nodeX);
		    varsXYZ.insertNode(nodeY);
		    varsXYZ.join(minDsep);
		    valscore = score(varsXYZ);
		    sum += valscore;
		}
	    }
	}
	
	return sum;

    }

    /** 
     * This method compute the K-L cross entropy for the nodes in vars.
     * @param NodeList vars. Node list. the two firts positions in vars are 
     * considered as singlenton ( xi and xj ).
     * @see Potential#crossEntropyPotential.
     */

    public double score (NodeList vars){

	PotentialTable potential;
	double dxyz;
	
	potential = getData().getPotentialTable(vars);
	potential.normalize();
	dxyz = potential.crossEntropyPotential();
	return dxyz;

    }

} // KLMetrics






