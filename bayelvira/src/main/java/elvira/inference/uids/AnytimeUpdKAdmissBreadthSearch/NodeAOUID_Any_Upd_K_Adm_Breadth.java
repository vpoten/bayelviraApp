package elvira.inference.uids.AnytimeUpdKAdmissBreadthSearch;

import java.util.ArrayList;

import elvira.Node;
import elvira.NodeList;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.GraphAOUID_Any_Upd_K_Adm;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.NodeAOUID_Any_Upd_K_Adm;

public class NodeAOUID_Any_Upd_K_Adm_Breadth extends NodeAOUID_Any_Upd_K_Adm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1942916901896189118L;

	public NodeAOUID_Any_Upd_K_Adm_Breadth(UID uid, GSDAG gsdag,
			GraphAOUID_Any_Upd_K_Adm_Breadth graphAOUID,
			double k_chance2) {
		super(uid,gsdag,graphAOUID,k_chance2);
		// TODO Auto-generated constructor stub
	}

	
	public NodeAOUID_Any_Upd_K_Adm_Breadth() {
		// TODO Auto-generated constructor stub
	}


	public ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> getChildrenArrayList(){
		ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth> list;
		NodeList children;
		
		children = this.getChildrenNodes();
		list = new ArrayList<NodeAOUID_Any_Upd_K_Adm_Breadth>();
		for (int i=0;i<children.size();i++){
			list.add((NodeAOUID_Any_Upd_K_Adm_Breadth) children.elementAt(i));
		}
		return list;
		
	}
	
	@Override
	public NodeAOUID_Any_Upd_K_Adm_Breadth copy() {
		// TODO Auto-generated method stub
		
		NodeAOUID_Any_Upd_K_Adm_Breadth auxNode = new NodeAOUID_Any_Upd_K_Adm_Breadth();
		auxNode.setUid(this.getUid());
		auxNode.graphUID = graphUID;
		auxNode.setInstantiations(this.getInstantiations().duplicate());
		auxNode.setF(this.getF());
		auxNode.setType(this.getTypeOfNodeAOUID());
		auxNode.setNameOfVariable(this.getNameOfVariable());
		auxNode.setNodeGSDAG(this.getNodeGSDAG());
		auxNode.setK_chance(this.getK_chance());
		auxNode.setC(this.getC());
		auxNode.setD(this.getD());
		auxNode.setU(this.getU());
		auxNode.setL(this.getL());
		auxNode.setFLower(this.getFLower());
		auxNode.setFUpper(this.getFUpper());
		auxNode.setNumChance(this.getNumChance());
		auxNode.setNumDecisions(this.getNumDecisions());
	
		
		//auxNode.setPruned(this.isPruned());
		
		return auxNode;
	}

	
	


}
