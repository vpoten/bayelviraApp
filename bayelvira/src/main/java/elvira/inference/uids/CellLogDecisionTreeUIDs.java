package elvira.inference.uids;

import java.util.ArrayList;
import java.util.HashMap;

import elvira.inference.uids.AnytimeUpdKAdmissBreadthSearch.NodeAOUID_Any_Upd_K_Adm_Breadth;

public class CellLogDecisionTreeUIDs {
	public CellLogDecisionTreeUIDs(NodeAOUID_Any_Upd_K_Adm_Breadth auxNode, Integer iterationWhenWasCreated) {
		super();
		this.iterationWhenWasCreated = iterationWhenWasCreated;
		values = new HashMap<Integer, DynamicInfCellLogDecisionTreeUIDs>();
		this.numChance = auxNode.getNumChance();
		this.numDecisions = auxNode.getNumDecisions();
		this.l = auxNode.getL();
		this.u = auxNode.getU();
		this.meu = auxNode.getEUOfCurrentStrategyForLeavesDPGSDAG();
	}
	//String nameOfNode;
	Integer iterationWhenWasCreated;
	//For each step of the evaluation we have information (h and k)
	HashMap <Integer,DynamicInfCellLogDecisionTreeUIDs> values;
	int numChance;
	int numDecisions;
	double l;
	double u;
	double meu;
	public HashMap<Integer, DynamicInfCellLogDecisionTreeUIDs> getValues() {
		return values;
	}
	public void setValues(HashMap<Integer, DynamicInfCellLogDecisionTreeUIDs> values) {
		this.values = values;
	}
	public void put(int step, DynamicInfCellLogDecisionTreeUIDs infCell) {
		// TODO Auto-generated method stub
		values.put(step,infCell);
	}
	
	public void print() {
		// TODO Auto-generated method stub
		System.out.println("# Chance nodes: "+numChance);
		System.out.println("# Decisions: "+numDecisions);
		System.out.println("Lower Bound: "+l);
		System.out.println("Upper Bound: "+u);
		System.out.println("MEU (given by DP): "+meu);
		System.out.println(values.toString());
		
	}
}
