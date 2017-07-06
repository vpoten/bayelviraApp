package elvira.inference.uids.AnytimeUpdatingKAdmiss;

import java.util.Random;
import java.util.Vector;

import elvira.Configuration;
import elvira.InvalidEditException;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.Anytime.AOUID_Anytime.HeuristicForSearching;
import elvira.inference.uids.AnytimeUpdatingK.AOUID_Any_Upd_K;
import elvira.inference.uids.AnytimeUpdatingK.GraphAOUID_Any_Upd_K;

/**
 * @author Manolo_Luque
 * This class performs AO search using a non-admissible heuristic with updating of the parameter
 * k and finding the optimal solution because the prune is handled correctly with an aditional updating
 * of the upper bound in the nodes.
 */
public class AOUID_Any_Upd_K_Adm extends AOUID_Any_Upd_K {

	public AOUID_Any_Upd_K_Adm(UID uid) {
		super(uid);
		// TODO Auto-generated constructor stub
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
		
		/*gsdag.obtainMinSetOfVarsCoal();
		System.out.println("*********** Let us see the gsdag *************");
		gsdag.print();*/
		
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
		tree = new GraphAOUID_Any_Upd_K_Adm((UID)network,gsdag,heur,k_chance,configuration);
		
	}

}
