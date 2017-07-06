package elvira.inference.uids;

import java.util.HashMap;

import elvira.inference.uids.AnytimeUpdKAdmissBreadthSearch.NodeAOUID_Any_Upd_K_Adm_Breadth;

/**
 * @author Manolo_Luque
 * This class implements a table containing important information about the evolution of the evaluation
 * of the corresponding decision tree for the UID
 */
public class TableLogDecisionTreeUIDs {
	public TableLogDecisionTreeUIDs() {
		super();
		values = new HashMap<NodeAOUID_Any_Upd_K_Adm_Breadth, CellLogDecisionTreeUIDs>();
	}

	HashMap <NodeAOUID_Any_Upd_K_Adm_Breadth,CellLogDecisionTreeUIDs> values;

	public HashMap<NodeAOUID_Any_Upd_K_Adm_Breadth, CellLogDecisionTreeUIDs> getValues() {
		return values;
	}

	public void setValues(HashMap<NodeAOUID_Any_Upd_K_Adm_Breadth, CellLogDecisionTreeUIDs> values) {
		this.values = values;
	}
}
