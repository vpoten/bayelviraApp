/* MNode.java */

package elvira.inference.uids;

import java.util.*;

import elvira.FiniteStates;
import elvira.Node;
import elvira.UID;
import elvira.potential.*; 


/**
 * Implements the class of nodes corresponding to Marta's solution graph.
 * All the objects of this class have instance variable
 * <code>kindOfNode</code> set to CHANCE and <code>typeOfVariable</code>
 * FINITE_STATES.
 *
 * @since 22/2/2002
 */
  
public class MNode extends FiniteStates  {


    /**
 * store information for propagation
 */

    private TreeSet eliminated;
    private TreeSet observations;
    private TreeSet toEliminate;
    private TreeSet obsEliminate;
    private Vector utilities=null;
    private Vector potentials=null;
    private Vector results=null;
    private UID diagram=null;


    /**
 * Creates a new empty <code>MNode</code> object. 
 */

    public MNode() {
  
	super();
	setTypeOfVariable(FINITE_STATES);
	setKindOfNode(CHANCE);
	setAxis(30,30);
	setPosX(10);
	setPosY(50);
	observations=new TreeSet();
    }

    public  MNode(String n,TreeSet defDecisions, UID uid){

	this();
	setPosX(10);
	setPosY(50);
	setName(n);
	setEliminated(defDecisions);
	diagram=uid;
    }


    /* ****************** Access methods **************** */
    public void setRelations(Vector pots, Vector utils) {
	potentials=pots;
	utilities=utils;
	return ;
    }
    public void setRelations(Vector pots, Vector utils, Vector res) {
	potentials=pots;
	utilities=utils;
	results=res;
	return ;
    }
    public void setResults(Vector res) {
	results=res;
	return ;
    }
    public Vector getResults() {
	return results;
    }

    public void setUtilities(Vector utils) {
	utilities=utils;
	return ;
    }
    public Vector getUtilities() {
	return utilities;
    }

    public void setPotentials(Vector pots) {
	potentials=pots;
	return ;
    }
    public Vector getPotentials() {
	return potentials;
    }

    /**
 * Gets the number of states of the variable.
 * @return the number of states of this node.
 */

    public int getNumDecisions() {
  	int i=0;
  	for(Iterator it=eliminated.iterator(); it.hasNext();){
  		Node node=diagram.getNode((String)it.next());
  		if(node.getKindOfNode()==Node.DECISION)
  			i++;
  	}
	return i;
    }

    public int getNumObservations() {
  
	return observations.size();
    }

    /**
 * gets the states of the variable.
 * @return the <code>TreeSet</code> of states of this node.
 */

    public TreeSet getEliminated() {
  
	return eliminated;
    }
    public TreeSet getObservations() {
  
	return observations;
    }
    public TreeSet getToEliminate() {
  
	return toEliminate;
    }
    public TreeSet getObsEliminate() {
  
	return obsEliminate;
    }

    /************************ Modifiers *********************/
    /**
     * Sets the decisions of the variable.
     * @return the <code>TreeSet</code> of states of this node.
     */

    public void setEliminated(TreeSet dec) {
	eliminated=dec;
    }
    public void setToEliminate(TreeSet dec) {
	toEliminate=dec;
    }
    public void setObsEliminate(TreeSet dec) {
	obsEliminate=dec;
    }
    public void setObservations(TreeSet obs) {
	observations=obs;
    }


    public void freeDO(){
	observations=null;
	eliminated=null;

    }

    public void onClick(){
	System.out.print("********************************************************************************\n");

	System.out.print(getName()+"\n");
	if(obsEliminate!=null)
	    System.out.print("obsEliminate: "+obsEliminate.toString()+"\n");
	else
	    System.out.print("obsEliminate: == null\n");
	    
	if(toEliminate!=null)
	    System.out.print("toEliminate: "+toEliminate.toString()+"\n");
	else
	    System.out.print("toEliminate: == null\n");
	
/*	System.out.print("Utilities --------------\n");
	for(Enumeration enumeration=utilities.elements(); enumeration.hasMoreElements();){
	    Potential re=(Potential)enumeration.nextElement();
	    re.print();
	}
	System.out.print("Potentials --------------\n");
	for(Enumeration enumeration=potentials.elements(); enumeration.hasMoreElements();){
	    Potential re=(Potential)enumeration.nextElement();
	    re.print();
	}
	System.out.print("Results --------------\n");
	if(results !=null)
	    for(Iterator it=results.iterator();it.hasNext();){
		PotentialTable re=(PotentialTable)it.next();
		re.print();
	    }
	if(getChildren().size()==1){
	System.out.print("Evaluate --------------\n");
		    MNode  child=(MNode)getChildren().elementAt(0).getHead();
		    diagram.evaluate(this, (Vector)child.getPotentials().clone(), (Vector)child.getUtilities().clone());
	}
*/
    }

}  // End of class.
