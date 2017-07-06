package elvira.inference.uids.AnytimeUpdatingK;

import java.util.Random;
import java.util.Vector;

import elvira.Configuration;
import elvira.InvalidEditException;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.Anytime.AOUID_Anytime;
import elvira.inference.uids.Anytime.GraphAOUID_Anytime;
import elvira.inference.uids.Anytime.AOUID_Anytime.HeuristicForSearching;

public class AOUID_Any_Upd_K extends AOUID_Anytime {

	public AOUID_Any_Upd_K(UID uid) {
		super(uid);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void propagate(Vector paramsForCompile) {
		// TODO Auto-generated method stub
		preparateGraphsForPropagation(paramsForCompile);
		this.propagateAfterCreatingGraphs(paramsForCompile);
		
	}

	public void preparateGraphsForPropagation(Vector paramsForCompile) {
		boolean applyDynamicW;
		HeuristicForSearching heur;
		double k_chance;
		Configuration configuration;
		boolean chooseRandomlyK;
		Random r;
		double minRandomlyK;
		double maxRandomlyK;
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
		
		
		
		configuration = (Configuration) paramsForCompile.get(4);
		
		chooseRandomlyK = (Boolean) paramsForCompile.get(5);
		if (chooseRandomlyK){
			minRandomlyK = (Double) paramsForCompile.get(6);
			maxRandomlyK = (Double) paramsForCompile.get(7);
			r = new Random();
			k_chance = minRandomlyK+r.nextDouble()*(maxRandomlyK-minRandomlyK);
			System.out.println("Initial K selected randomly: "+k_chance);
		}
		else{
			k_chance = (Double) paramsForCompile.get(1); 
		}
		
  
	//tree = new GraphAOUID_Anytime((UID)network,gsdag,heur,k_chance);
		tree = new GraphAOUID_Any_Upd_K((UID)network,gsdag,heur,k_chance,configuration);
		
	}

}
