package elvira.learning.policies;

import java.util.ArrayList;
import java.util.Vector;

import elvira.IDWithSVNodes;
import elvira.Network;
import elvira.inference.super_value.ArcReversalSV;
import elvira.inference.super_value.PolicyNetwork;
import elvira.potential.DeterministicPotentialTable;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.tools.PropagationStatisticsID;

public class PolicyExplanationRules {
	ArrayList<Rule> rules;

	public void obtainRulesFromDiagram(IDWithSVNodes diagram, boolean evaluated) {
		// TODO Auto-generated method stub
		DeterministicPotentialTable auxDetPot;
		Rule auxRule;
		int a;
		
		if (!evaluated){
			diagram.compile(3,null);
		}
		
		ArcReversalSV prop = ((ArcReversalSV)diagram.getPropagation());
		
		Vector<Potential> potOfPolicies = prop.getResultsForPolicies();
		
		ArrayList<String> auxElimOrder = ((PropagationStatisticsID)(prop.getStatistics())).getOrderOfElimination();
		
		rules = new ArrayList<Rule>();
		
		PolicyNetwork pn = new PolicyNetwork(diagram);
		
		for (Potential auxPot:potOfPolicies){
			
			PotentialTable auxPotTable = (PotentialTable) auxPot;
			
			auxDetPot = new DeterministicPotentialTable(auxPotTable);
			auxRule = new Rule(auxDetPot,diagram,auxElimOrder, true,pn);
			rules.add(auxRule);
			
			
		}
		
		
	}
	
	public void print(){
		for (Rule auxRule:rules){
			auxRule.print();
		}
	}
}
