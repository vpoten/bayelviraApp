package elvira.inference.super_value;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.learning.policies.Rule;
import elvira.potential.DeterministicPotentialTable;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.tools.PropagationStatisticsID;


public class Strategy {
	
	boolean isOptimal;
	
	IDWithSVNodes id;
	
	Hashtable<FiniteStates,DeterministicPotentialTable> policies;
	
	
	public Strategy(IDWithSVNodes diagram){
		
		id = diagram;
		
		policies = new Hashtable<FiniteStates,DeterministicPotentialTable>();
		
		setOptimal(false);
	}
	
	
	public void calculateOptimalStrategy(){
		calculateOptimalStrategy(3);
	}
	
	
	/**
	 * @param algor
	 * It calculates the optimal strategy (if not calculated previously)
	 */
	public void calculateOptimalStrategy(int algor){
		DeterministicPotentialTable auxDetPot;
		
		
		if (!isOptimal){
				
		
		//Take the policies
		ArcReversalSV prop = ((ArcReversalSV)id.getPropagation());
		
		if (prop==null){//The diagram is not evaluated
			//Evaluate the diagram
			id.compile(algor,null);
			//Take the policies
			prop = ((ArcReversalSV)id.getPropagation());
		
		}
		
		Vector<Potential> potOfPolicies = prop.getResultsForPolicies();
		
		for (Potential auxPot:potOfPolicies){
			
			PotentialTable auxPotTable = (PotentialTable) auxPot;
			
			auxDetPot = new DeterministicPotentialTable(auxPotTable);
			policies.put(auxDetPot.getVariable(),auxDetPot);
			
		}
		}
		setOptimal(true);
		
	}
	
	
	public void takeOptimalStrategyFromEvaluatedID(){
		DeterministicPotentialTable auxDetPot;
		
		
		if (!isOptimal){
				
		
		//Take the policies
		ArcReversalSV prop = ((ArcReversalSV)id.getPropagation());
		
		if (prop!=null){//The diagram is not evaluated
			Vector<Potential> potOfPolicies = prop.getResultsForPolicies();
			
			for (Potential auxPot:potOfPolicies){
				
				PotentialTable auxPotTable = (PotentialTable) auxPot;
				
				auxDetPot = new DeterministicPotentialTable(auxPotTable);
				policies.put(auxDetPot.getVariable(),auxDetPot);
				
			}
			}
			setOptimal(true);
		
		}
		
		
	}


	public DeterministicPotentialTable getPolicy(FiniteStates decision){
		
		return policies.get(decision);
	}
	
	
	public void setPolicy(FiniteStates decision,DeterministicPotentialTable policy){
		
		policies.put(decision,policy);
	}
	public boolean isOptimal() {
		return isOptimal;
	}


	public void setOptimal(boolean isOptimal) {
		this.isOptimal = isOptimal;
	}
	
	
}
