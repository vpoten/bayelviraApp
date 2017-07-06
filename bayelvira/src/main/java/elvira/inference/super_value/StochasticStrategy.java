package elvira.inference.super_value;

import java.util.Hashtable;
import java.util.Vector;

import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.potential.DeterministicPotentialTable;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;

public class StochasticStrategy {
	boolean isOptimal;
	
	IDWithSVNodes id;
	
	Hashtable<FiniteStates,PotentialTable> policies;
	
	
	public StochasticStrategy(IDWithSVNodes diagram){
		
		id = diagram;
		
		policies = new Hashtable<FiniteStates,PotentialTable>();
		
		setOptimal(false);
	}
	
	
	public void calculateOptimalStrategy(){
		calculateOptimalStrategy(3);
	}
	
	
	/**
	 * @param algor
	 * It calculates the optimal StochasticStrategy (if not calculated previously)
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
			
			
			Vector vars = auxPot.getVariables();
			policies.put((FiniteStates)vars.elementAt(vars.size()-1),auxPotTable);
			
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
				
				
				Vector vars = auxPot.getVariables();
				policies.put((FiniteStates)vars.elementAt(vars.size()-1),auxPotTable);
				
			}
			}
			setOptimal(true);
		
		}
		
		
	}


	public PotentialTable getPolicy(FiniteStates decision){
		
		return policies.get(decision);
	}
	
	
	public PotentialTable setPolicy(FiniteStates decision,PotentialTable policy){
		
		return policies.put(decision,policy);
	}
	public boolean isOptimal() {
		return isOptimal;
	}


	public void setOptimal(boolean isOptimal) {
		this.isOptimal = isOptimal;
	}
	
}
