/* ConstraintKnowledgePanel.java */
package elvira.gui;

import elvira.learning.constraints.ConstraintKnowledge;


/**
 * This class implements the panel where the result of
 * ConstraintKnowledgeDialog will be stored. The
 * objects of this class will store the Knowledge 
 * constraints.
 *
 * @author fjgc@decsai.ugr.es
 * @since  21/10/2003
 */

public class ConstraintKnowledgePanel extends ElviraPanel {

   private ConstraintKnowledge ck;

    /** This method creates a new ConstraintKnowledgePanel */
    public ConstraintKnowledgePanel() {
	ck=null;
    }//end ctor.

    /**
    *  This method return a ConstraintKnowledge object with the stored contraints
    *  @return the knowledge constraints, if the constraints are empty will return null
    */
    public ConstraintKnowledge getConstraints() {
     return ck;
    }//end getConstraints method

    /**
    *  This method store a ConstraitnKnowledge object with the contraints
    *  @param the knowledge constraints
    */
    public void setConstraints (ConstraintKnowledge constraints) {
	this.ck=constraints;
    }//end setConstraints method

}//end  ConstraintKnowledgePanel class
