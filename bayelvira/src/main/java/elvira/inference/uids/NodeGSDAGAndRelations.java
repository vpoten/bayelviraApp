package elvira.inference.uids;

import elvira.inference.uids.NodeGSDAG.RelationsNodeGSDAG;

public class NodeGSDAGAndRelations {
	NodeGSDAG nodeGSDAG;
	
	RelationsNodeGSDAG relations;

	public NodeGSDAG getNodeGSDAG() {
		return nodeGSDAG;
	}

	public void setNodeGSDAG(NodeGSDAG nodeGSDAG) {
		this.nodeGSDAG = nodeGSDAG;
	}

	public RelationsNodeGSDAG getRelations() {
		return relations;
	}

	public void setRelations(RelationsNodeGSDAG relations) {
		this.relations = relations;
	}
}
