package elvira.inference.uids.Anytime;

import java.util.Vector;

import elvira.Configuration;
import elvira.InvalidEditException;
import elvira.UID;
import elvira.inference.Propagation;
import elvira.inference.uids.AOUID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.GraphAOUID;
import elvira.tools.PropagationStatisticsAOUID;



/**
 * @author Manolo
 * Class that implements the evaluation of an UID through a branch and bound algorithm that
 * performs the search in the decision tree
 *
 */
public class AOUID_Anytime extends AOUID{
	public enum HeuristicForSearching {DYNAMIC_W, MIXED_HEUR}


	
	


	
	 /** Creates a new instance of BranchBound */
	  public AOUID_Anytime(UID uid) {
	    super(uid);
	    
	  	   
	  }

	@Override
	public void propagate(Vector paramsForCompile) {
		// TODO Auto-generated method stub
		preparateGraphsForPropagation(paramsForCompile);
		this.propagateAfterCreatingGraphs(paramsForCompile);
		
	}
	  
	  
	private void preparateGraphsForPropagation(Vector paramsForCompile) {
		boolean applyDynamicW;
		HeuristicForSearching heur;
		double k_chance;
		Configuration configuration;
		  //It indicates the minimum number of expansions to calculate the statistics
		  //about the EU
	
		   
		
		// TODO Auto-generated method stub
		((UID)network).createGSDAG();
		
		try {
			gsdag = new GSDAG(network);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
		
		gsdag.initializePotentials(((UID)network).getRelationList());
		
		heur = (HeuristicForSearching) paramsForCompile.get(0);
		
		//applyDinamicW = (Boolean) paramsForCompile.get(1);
		
		k_chance = (Double) paramsForCompile.get(1);
		
		configuration = (Configuration) paramsForCompile.get(4);
  
	//tree = new GraphAOUID_Anytime((UID)network,gsdag,heur,k_chance);
		tree = new GraphAOUID_Anytime((UID)network,gsdag,heur,k_chance,configuration);
		
	}

	  

}
