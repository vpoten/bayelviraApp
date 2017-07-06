package elvira.probabilisticDecisionGraph.tools;

import elvira.*;
import java.util.*;

public class CasesOps {

	public static double[] getMarginalCounts(CaseListMem clm, FiniteStates v){
		double[] counts = new double[v.getNumStates()];
		int j = clm.getVariables().indexOf(v);
		for(int i=0;i<clm.getNumberOfCases();i++) counts[(int)clm.getValue(i, j)]++;
		return counts;
	}
	
	public static boolean configurationsAreConsistent(Configuration c1, Configuration c2){
		boolean retval = true;
		for(FiniteStates n : (Vector<FiniteStates>)c1.getVariables()){
			if(c1.getValue(n) != c2.getValue(n)){
				retval = false;
				break;
			}
		}
		return retval;
	}

	public static CaseList selectFromWhere(CaseList cl, Configuration cond){
		CaseList retval = new CaseListMem(cond.getVariables());
		Configuration conf;
		for(int i=0;i<cl.getNumberOfCases();i++){
			conf = cl.get(i);
			if(conf.isCompatibleWeak(cond)){
				retval.put(conf);
			}
		}
		return retval;
	}

	public static CaseListMem selectFromWhere(CaseList data, FiniteStates var, int state){
		//Vector<FiniteStates> vars = new Vector<FiniteStates>(); vars.add(var);
		//Vector<Integer> vals      = new Vector<Integer>(); vals.add(new Integer(state));
		//Configuration conf = new Configuration(vars,vals);
		//return selectFromWhere(data, conf);
		CaseListMem queryResult = new CaseListMem(data.getVariables());
		Configuration c;
		for(int i=0;i<data.getNumberOfCases();i++){
			c = data.get(i); 
			if(c.getValue(var) == state){
				queryResult.put(c);
			}
		}
		return queryResult;
	}
	
}
