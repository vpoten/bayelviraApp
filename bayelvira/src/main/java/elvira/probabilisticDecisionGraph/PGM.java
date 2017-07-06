package elvira.probabilisticDecisionGraph;

import elvira.*;
import java.util.*;

public interface PGM {

	public boolean insertEvidence(Configuration conf) throws PDGIncompatibleEvidenceException;
	//public void synchronizeVariables(Vector<Node> v);
	
	public void removeEvidence();
	
	public double probabilityOfEvidence();
	
	public void updateBeliefs();
	
	public double[] getBelief(Node queryVariable) throws PDGVariableNotFoundException;
	
	public String getName();

}
