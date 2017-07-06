package elvira.inference.uids;

import java.util.Vector;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.NodeList;
import elvira.UID;

public class GraphAOUIDCoalescence extends GraphAOUID {
	public GraphAOUIDCoalescence(){
		
	}
		
	public GraphAOUIDCoalescence(UID uid,GSDAG gsdag2)  {
		NodeAOUID initialState;
		// TODO Auto-generated constructor stub
		/*open = new ArrayList();
		closed = new ArrayList();*/
		
	    //Compile the UID
	    uid.setCompiledPotentialList(new Vector());
				
		gsdag = gsdag2;
		

		System.out.println("First state of the tree of search");
		initialState = new NodeAOUIDCoalescence(uid,gsdag,this);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
		
		
	
		
		
		
	}
	
	public NodeAOUID improvedGetNodeAOUID(Configuration instantiations, FiniteStates nodeUID, int val,NodeList varsReq){
		// TODO Auto-generated method stub
		Configuration auxConf,fullConf;
		NodeAOUID auxNodeAOUID;
		boolean found = false;
		NodeAOUID foundNodeAOUID = null;
		
		fullConf = instantiations.duplicate();
		fullConf.insert(nodeUID,val);


		return ((NodeAOUIDCoalescence)(root)).improvedGetNodeAOUID(fullConf,varsReq);
		
	}

	
}
