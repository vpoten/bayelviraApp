package elvira.inference.uids;

import java.util.ArrayList;
import java.util.Vector;

import elvira.UID;
import elvira.inference.clustering.ShenoyShaferPropagation;

public class GraphAOUIDLOfH extends GraphAOUID{

	

	


	public GraphAOUIDLOfH(UID uid,GSDAG gsdag2,boolean applyDinamicWeighting2, ShenoyShaferPropagation ssp2)  {
		NodeAOUID initialState;
		// TODO Auto-generated constructor stub
		/*open = new ArrayList();
		closed = new ArrayList();*/
		
	    //Compile the UID
	    uid.setCompiledPotentialList(new Vector());
				
		gsdag = gsdag2;
		
		//Lazy penniless (it has a junction tree)
		ssp = ssp2;
		
		applyDynamicWeighting = applyDinamicWeighting2;
		
		System.out.println("First state of the tree of search");
		initialState = new NodeAOUIDLOfH(uid,gsdag,this);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
		
		
	
		
		
		
	}
	


	
	
	}
