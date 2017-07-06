package elvira.inference.uids;

import elvira.tools.statistics.roots.RealRootFunction;

public class BranchingFactor implements RealRootFunction{
	int depth;
	int numberOfNodes;

	public BranchingFactor(int depth1,int numberOfNodes1){
		depth = depth1;
		numberOfNodes = numberOfNodes1;
}
	public double function(double b) {
		
		// TODO Auto-generated method stub
		if (b==1) return depth-numberOfNodes;
		else return ((Math.pow(b,depth)-1)/(b-1))-numberOfNodes;
	}

}
