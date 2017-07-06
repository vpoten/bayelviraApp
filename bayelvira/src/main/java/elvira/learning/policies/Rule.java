package elvira.learning.policies;

import java.util.ArrayList;
import java.util.Vector;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.inference.super_value.ArcReversalSV;
import elvira.inference.super_value.PolicyNetwork;
import elvira.potential.DeterministicPotentialTable;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.tools.PropagationStatisticsID;

public class Rule {
	
	FiniteStates decision;
	
	/**
	 * It constructs a rule (tree with a set of rules) for the corresponding deterministic potential table
	 * @param auxDetPot 
	 * @param diagram Order of variables eliminated by ArcReversalSV. The last variable in the list is the first variable eliminated during the evaluation.
	 * @param auxElimOrder
	 * @param pruneNullConfigurationsPT 
	 * @param pn 
	 */
	public Rule(DeterministicPotentialTable auxDetPot, IDWithSVNodes diagram, ArrayList<String> auxElimOrder, boolean pruneNullConfigurationsPT, PolicyNetwork pn) {
		// TODO Auto-generated constructor stub
		Configuration conf;
		RuleNode newRoot;
		boolean debug = true;
		
		decision = auxDetPot.getVariable();
		conf = new Configuration();
		
		Integer firstEliminated = auxElimOrder.size()-1;
		root = new RuleNode();
		//root = new RuleChanceNode();
		root.constructRuleNode(decision,auxDetPot,diagram,auxElimOrder,firstEliminated, conf,null,pruneNullConfigurationsPT,pn);
		if (debug==false){
		newRoot = new RuleNode();
		newRoot.obtainRuleNodeAfterEliminatingRedundancy(decision, root);
		setRoot(newRoot);
		}
		else{
			setRoot(root);
		}
		
	}

	RuleNode root;

	public void print() {
		// TODO Auto-generated method stub
		System.out.println("*****************");
		System.out.println("Rule for decision: "+decision.getName());
		root.print(0);
	}

	public RuleNode getRoot() {
		return root;
	}

	public void setRoot(RuleNode root) {
		this.root = root;
	}
	
	

	
}
