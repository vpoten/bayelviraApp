package elvira.tools;

import java.util.ArrayList;
import java.util.Hashtable;

import elvira.inference.uids.TableLogDecisionTreeUIDs;

public class PropagationStatisticsAOUID extends PropagationStatistics {
	
	
	
	//Number of nodes created in the AO graph
	int createdNodes;
	//Expected utility in each instant of time
	private ArrayList<Double> expectedUtility;
	
	//First decision selected in each instant of time, if we have a branch or a decision
	//Otherwise we have value -1 for each box of the array.
	//For the decision we store the index of the value selected
	//For the branches we store the index of the child selected
	private ArrayList<Integer> decisionToMake;
	
//	First option selected in each instant of time, if we have a branch or a decision
	//Otherwise we have value -1 for each box of the array.
	//We store the index of the value selected
	//For the branches we store the index of the child selected
	private ArrayList<Integer> optionToChoose;
	
	//Expected utility computed with DP over the GSDAG, in each instant of time
	private ArrayList<Double> expectedUtilityDPGSDAG;
	
	//Expected utility where only N branches or decisions are taken from the GSDAG, and the rest is computed with DP.
	//For those decisions (of the first N) that have no policies we assume uniform distribution
	//private ArrayList<Double> expectedUtilityNDecs;
	private ArrayList<Double> expectedUtilityNDecs[];
	
	
	/**
	 * It indicates the proportion of decisions we get right in each level ot the decision tree. We store the number obtained for each level of the decision tree
	 *  In each level the value is in the range [0,1]. propDecsRight[0] corresponds to the level 0, and the last element corresponds
	 *  to the finalNumDecs
	 */
	private ArrayList<Double> propDecsRight[];
	
	
	
	public ArrayList<Integer> numStatesEachDecisionFirstBranch;
	
	
	
	private ArrayList<Double> euStrategyForEachBranch[];
	//Value of f in the root
	private ArrayList<Double> f;
	
	TableLogDecisionTreeUIDs tableLog;
	

	public PropagationStatisticsAOUID() {
		super();
		createdNodes = 0;
		expectedUtility = new ArrayList<Double>();
		decisionToMake = new ArrayList();
		optionToChoose = new ArrayList();
		f = new ArrayList();
		expectedUtilityDPGSDAG = new ArrayList<Double>();
		tableLog = new TableLogDecisionTreeUIDs();
		
		//expectedUtilityNDecs = new Hashtable<Integer,ArrayList<Double>>();
		//euStrategyForEachBranch[] = new ArrayList();
		// TODO Auto-generated constructor stub
	}

	public int getCreatedNodes() {
		return createdNodes;
	}

	public void setCreatedNodes(int createdNodes) {
		this.createdNodes = createdNodes;
	}

	//It adds a new value for time, increasing the last time existing in 'times'.
	//It's used when we have to stop the crono and continue after a certain time.
	public void addToLastTime(double time) {
		// TODO Auto-generated method stub
		this.addTime((Double)(this.getTimes().lastElement())+time);
	}

	public void addExpectedUtility(double d) {
		// TODO Auto-generated method stub
		expectedUtility.add(d);
	}
	
	public void addExpectedUtilityDPGSDAG(double d) {
		// TODO Auto-generated method stub
		expectedUtilityDPGSDAG.add(d);
	}
	
	/*private void addDecisionToMake(int d) {
		// TODO Auto-generated method stub
		decisionToMake.add(d);
	}*/

	public ArrayList<Integer> getDecisionToMake() {
		return decisionToMake;
	}

	public void setDecisionTaken(ArrayList<Integer> decisionToTake) {
		this.decisionToMake = decisionToTake;
	}

	public ArrayList<Double> getExpectedUtility() {
		return expectedUtility;
	}

	public void setExpectedUtility(ArrayList<Double> expectedUtility) {
		this.expectedUtility = expectedUtility;
	}

	public void printDecisionToTakeInEachStep() {
		// TODO Auto-generated method stub
		ArrayList<Double> estimator;
		System.out.println("Step  Decision-To-Take EU-Strat F-Root Combination");
		estimator=calculateEstimatorMEU();
		for (int i=0;i<decisionToMake.size();i++){
			
			System.out.println(i+"\t"+decisionToMake.get(i)+"\t"+expectedUtility.get(i)+"\t"+f.get(i)+"\t"+estimator.get(i));
		}
	}

	private ArrayList<Double> calculateEstimatorMEU() {
		// TODO Auto-generated method stub
		ArrayList<Double> estimator;
		double weigthEU;
		
		weigthEU = 0.66;
		estimator = new ArrayList();
		for (int i=0;i<expectedUtility.size();i++){
			//estimator.add((weigthEU*expectedUtility.get(i)+(1-weigthEU)*f.get(i)));
			estimator.add(calculateEstimatorMEU(expectedUtility.get(i),f.get(i),i));
		}
		return estimator;
		
	}
	
	private static double calculateEstimatorMEU(double eu,double f,double s){
		double weigthEU = 0.8;
		double estimator;
		
		weigthEU = weigthEU*(Math.exp(-s/20.0));
		estimator = (weigthEU*eu+(1-weigthEU)*f);
		return estimator;
		
	}

	
	public void addF(double auxF) {
		// TODO Auto-generated method stub
		f.add(auxF);
	}

	public ArrayList<Double> getF() {
		// TODO Auto-generated method stub
		return f;
	}

	public ArrayList<Integer> getOptionToChoose() {
		// TODO Auto-generated method stub
		return optionToChoose;
	}
			
		
	
	/**
	 * @param decision
	 * @param option
	 * @return The index of the state of the decision 'decision' correponding to 'option'
	 */
	public int getStateOfOptionToChoose(Integer option)
	{
		boolean found;
		int state=0;
		int sum;
		int auxNumStates;
		sum = 0;
		found = false;
		for (int i=0;(i<numStatesEachDecisionFirstBranch.size())&&(found==false);i++){
			auxNumStates = numStatesEachDecisionFirstBranch.get(i); 
			if (sum+auxNumStates>option){
				found = true;
				state = option-sum;				
			}
			else{
				sum = sum+auxNumStates;
			}
			
		}
		return state;
	}
	


	public void setNumStatesEachDecisionFirstBranch(ArrayList<Integer> numStatesEachDecision) {
		this.numStatesEachDecisionFirstBranch = numStatesEachDecision;
	}

	public ArrayList<Integer> getNumStatesEachDecisionFirstBranch() {
		return numStatesEachDecisionFirstBranch;
	}

	/*private void addOptionToChoose(int optChosen) {
		// TODO Auto-generated method stub
		optionToChoose.add(optChosen);
	}*/
	
	
	public void addDecisionAndOption(int decision,int option){
		decisionToMake.add(decision);
		optionToChoose.add(option);
	}

	public ArrayList<Double> getExpectedUtilityDPGSDAG() {
		return expectedUtilityDPGSDAG;
	}


		public ArrayList<Double>[] getExpectedUtilityNDecs() {
		return expectedUtilityNDecs;
	}

	public void setExpectedUtilityNDecs(
			ArrayList<Double>[] expectedUtilityNDecs) {
		this.expectedUtilityNDecs = expectedUtilityNDecs;
	}

	public void initializeExpectedUtilityNDecs(int initialNumDecs,
			int finalNumDecs) {
		// TODO Auto-generated method stub
		expectedUtilityNDecs = new ArrayList[finalNumDecs-initialNumDecs+1];
		for (int i=0;i<expectedUtilityNDecs.length;i++){
			expectedUtilityNDecs[i]= new ArrayList<Double>();
		}
	}

	
	public void initializeNumDecsRight(int finalNumDecsRight) {
		// TODO Auto-generated method stub
		propDecsRight = new ArrayList[finalNumDecsRight+1];
		for (int i=0;i<propDecsRight.length;i++){
			propDecsRight[i]= new ArrayList<Double>();
		}
	}

	public ArrayList<Double>[] getPropDecsRight() {
		return propDecsRight;
	}

	public void setPropDecsRight(ArrayList<Double>[] propDecsRight) {
		this.propDecsRight = propDecsRight;
	}

	public TableLogDecisionTreeUIDs getTableLog() {
		return tableLog;
	}

	public void setTableLog(TableLogDecisionTreeUIDs tableLog) {
		this.tableLog = tableLog;
	}




	
	

}
