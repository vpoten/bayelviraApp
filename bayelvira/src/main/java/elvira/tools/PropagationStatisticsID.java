package elvira.tools;

import java.util.ArrayList;

public class PropagationStatisticsID extends PropagationStatistics {
	/**
	 * Array to store the order of elimination. The first element in the arraylist is the 
	 * first variable to be eliminated in the evaluation.
	 */
	ArrayList<String> orderOfElimination; 
	
	public PropagationStatisticsID() {
		super();
		orderOfElimination = new ArrayList();
	}

	public ArrayList<String> getOrderOfElimination() {
		return orderOfElimination;
	}

	public void addNameToOrderOfElimination(String auxName) {
		// TODO Auto-generated method stub
		orderOfElimination.add(auxName);
		
	}
	
	
}
