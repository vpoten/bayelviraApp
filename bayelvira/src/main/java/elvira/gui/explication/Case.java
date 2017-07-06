/* Case.java */

package elvira.gui.explication;

import elvira.*;
import elvira.gui.*;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.potential.*;
import java.util.*;
import java.awt.Color;
import java.io.*;

/**
 * Contains all the properties corresponding to a set of findings. This set can be empty.
 * The properties of a case are: probability of every node in the BN, set of observed nodes, identifier, color,
 * comment, associated evidence, etc.
 *
 * @since 25/7/00
 * @author Carmen Lacave, F. Javier D�ez, Roberto Atienza
 * @version 0.1
 */

public class Case{

    /** prob[i][j] contains the probability of state j of node i, being i the position of the
     * node in the list of nodes that defines the BN
     */
    private double[][] prob;

    /** observed[i] is true if node i is observed in this case
    */
    private boolean[] observed;

    /** to know if this case is going to be shown. By default, every case is shown */
    private boolean isShown=true;

    private String comment="";

    /** Every case has an identifier
    */
    private String identifier;

    /** Every case has a color which visually identifies it
    */
    private Color color;

    /** to know if the evidence associated has alredy been propagated
    */
    private boolean propagated=false;

    Bnet bn;

    Evidence evi;
    
    private boolean hasContinuous=false;

    /*** constructors***/

    /**
     * Creates an empty Case object
     */
    public Case() {
	    bn = new Bnet();
	    evi = new Evidence();
    }

    /**
     * Creates a new Case from a given evidence e for a Bnet b
     * @param b The Bayesian net to be processed
     * @param e The evidence for the case
     */
    public Case(Bnet b, Evidence e){

        evi=new Evidence();
        for (int v=0; v<e.getVariables().size(); v++)
            evi.putValue(e.getVariable(v), e.getValue(v));
        bn=b;
        prob=new double[bn.getNodeList().size()][];
        observed=new boolean[bn.getNodeList().size()];
        
        for (int i=0; i<bn.getNodeList().size(); i++){
            if (bn.getNodeList().elementAt(i).getClass()==Continuous.class && bn.getNodeList().elementAt(i).getKindOfNode()==Node.CHANCE){
                    hasContinuous=true;
            }
        
        }   
        
        for (int i=0; i<bn.getNodeList().size(); i++){
          prob[i]=null;
          Node node=bn.getNodeList().elementAt(i);
          if (node.getClass()==FiniteStates.class){  
            
            FiniteStates fs=(FiniteStates)node;
            if (e.isObserved(fs)){
                observed[i]=true;
                prob[i]=new double[fs.getNumStates()];
                for (int j=0; j<fs.getNumStates(); j++)
                    if (j==e.getValue(fs))
                        prob[i][j]=1.0;
                    else prob[i][j]=0.0;
            }
            else {
                  prob[i]=new double[fs.getNumStates()];
                  observed[i]=false;
            }
/*andrew*/}else if (node.getClass()==Continuous.class && node.getKindOfNode()==Node.CHANCE){
              
              Continuous cont=(Continuous)node;
              
                if (e.isObserved(cont)){
                    observed[i]=true;
                }
                else {
                      observed[i]=false;
                }
              
          }
            
            
            
        }//end for
    }//end constructor

    /**
     * Creates a new Case from a given evidence e for a Bnet b
     * @param b The Bayesian net to be processed
     * @param e The evidence for the case
     * @param color The color to be assigned to the new Case
     */
    public Case(Bnet b, Evidence e, Color col){
      this(b,e);
      color = col;
    }

    /**
     * Creates a new Case with identifier s from a given evidence e for a Bnet b
     * @param b The Bayesian net to be processed
     * @param e The evidence for the case
     * @param s The identifier for the case
     */
    public Case(Bnet b, Evidence e, String s){
        this(b,e);
        identifier=s;
    }

    /**
     * Creates a new Case from a given Case c (for a Bnet b). The new Case is equals to Case c
     * @param b The Bayesian net to be processed
     * @param c The known case
     */
    public Case(Bnet b, Case c){
        bn=b;
        prob=new double[bn.getNodeList().size()][];
        observed=new boolean[bn.getNodeList().size()];
    
        for (int i=0; i<bn.getNodeList().size();i++){
          prob[i]=null;
          observed[i]=c.observed[i];
          if (c.prob[i] != null) {
            prob[i]=new double[c.prob[i].length];
            for (int j=0; j<c.prob[i].length; j++){
                prob[i][j]=c.prob[i][j];
            }
          }
        }
        
       for (int i=0; i<bn.getNodeList().size(); i++){
            if (bn.getNodeList().elementAt(i).getClass()==Continuous.class){
                    hasContinuous=true;
            }
        
        }   

        evi=new Evidence();
        if (hasContinuous){
            for (int v=0; v<c.getEvidence().getContinuousVariables().size(); v++)
                evi.putValue(c.getEvidence().getContinuousVariable(v), c.getEvidence().getContinuousValue(v));

        }else{
            for (int v=0; v<c.getEvidence().getVariables().size(); v++)
                evi.putValue(c.getEvidence().getVariable(v), c.getEvidence().getValue(v));
        }
    
    }//end constructor

    /**
     * Creates a new Case with color col from a given Case c (for a Bnet b).
     * @param b The Bayesian net to be processed
     * @param c The known case
     * @param color The color to be assigned to the new Case
     */
    public Case (Bnet b, Case c, Color col) {
      this (b,c);
      color = col;
    }

    /**
     * Creates a new empty Case with color col for a Bnet b. The empty case is
     * definde by an empty set of findings. Then it contains the prior probabilities of every
     * node in the Bayesian net.
     * @param b The Bayesian net to be processed
     * @param color The color to be assigned to the new Case
     */
    public Case (Bnet b, Color col) {
      this (b);
      color = col;
    }

    /**
	 * Creates an new empty Case for a Bnet b.
	 * 
	 * @param b
	 *            The Bayesian net to be processed
	 */
	public Case(Bnet b) {
		Node auxNode;
		int kindOfNode;

		bn = b;

		Vector cpots;
		Class<? extends Bnet> bClass = b.getClass();
		if (bClass == Bnet.class) {
			cpots = bn.getCompiledPotentialList();
		} else if (bClass!=UID.class){ // ((b.getClass()==IDWithSVNodes.class)||(b.getClass()==IDWithSVNodes.class))
			cpots = ((IDiagram) b).getCpn().getCompiledPotentialList();
		}else{//UID
			cpots = new Vector();
		}

		prob = new double[bn.getNodeList().size()][];
		observed = new boolean[bn.getNodeList().size()];
		for (int i = 0; i < bn.getNodeList().size(); i++) {
			prob[i] = null;
			auxNode = bn.getNodeList().elementAt(i);
			kindOfNode = auxNode.getKindOfNode();
			if (auxNode.getClass() == FiniteStates.class) {
				FiniteStates fs = (FiniteStates) auxNode;

				boolean found = false;
				int ip = 0;
				while (!found && ip < cpots.size()) {

					Potential p = (Potential) cpots.elementAt(ip);
					/* andrew */if (fs.getName().equals(
							((Node) p.getVariables().elementAt(0)).getName())) {

						if (p.getClass() == PotentialContinuousPT.class) {
							PotentialTree aux2 = new PotentialTree(
									(PotentialContinuousPT) p);
							p = new PotentialTable(aux2);
						}

						found = true;
						double[] theValues = new double[(int) p.getSize()];
						Configuration config = new Configuration(p
								.getVariables());
						for (int k = 0; k < p.getSize(); k++) {
							theValues[k] = p.getValue(config);
							config.nextConfiguration();
						}
						prob[i] = theValues;
					} else
						ip++;
				}
			}/* andrew */
			else if ((kindOfNode == Node.UTILITY)
					|| (kindOfNode == Node.SUPER_VALUE)) {/* mluque */
				// FiniteStates fs = (FiniteStates)
				// bn.getNodeList().elementAt(i);
				CooperPolicyNetwork cpn=((IDiagram)b).getCpn();
				boolean found=false;

				/*Potential p = (PotentialTable) (((IDiagram) bn).getCpn()
						.obtainUtility(n.getName()));*/
				for (int j=0;((j<cpots.size())&&(found==false));j++){
					PotentialTable p = (PotentialTable) cpots.elementAt(j);
					PotentialTable newPot;
					if (auxNode.getName().equals(((Node) p.getVariables().elementAt(0)).getName())) {
						found = true;
						
					newPot = p.convertProbabilityIntoUtility(cpn.getMinimumUtility(auxNode),cpn.getMaximumUtility(auxNode));
					double[] theValues = new double[(int) newPot.getSize()];
					Configuration config = new Configuration(newPot.getVariables());
					for (int k = 0; k < newPot.getSize(); k++) {
						theValues[k] = newPot.getValue(config);
						config.nextConfiguration();
					}					
					prob[i] = theValues;
					}
					
				}
				

				

			}

			observed[i] = false;
			((Node) bn.getNodeList().elementAt(i)).setObserved(false);

		}// end for
		color = new Color(0, 153, 51);
		evi = new Evidence();
	}// end constructor

	
	/**
	 * Transform a case referred to an ID to a case of this CPN
	 * A case of an ID consists in a set of pairs (node, value), where value can be a probability
	 * or an utility
	 * A case of a CPN only consists in a set of pairs (node, probability).
	 * This method applies direct Cooper's transformation for value nodes.
	 * The nodes of the case c are referred to the influence diagram, but the nodes of
	 * the case returned by the method must be referred to the CPN
	 * @param c Case of nodes refered to this CPN
	 */
	public Case(CooperPolicyNetwork cpn,Case c){
        int numberOfNodes;
        Evidence eviInID;
        IDiagram id;
        Node auxNode;
        double probOfYes;
        
        numberOfNodes=cpn.getNodeList().size();
        
        prob=new double[numberOfNodes][];
        observed=new boolean[numberOfNodes];
        //The new case is referred to the cpn
        bn = cpn;
        
        //Influence diagram of the case c
        id = (IDiagram)(c.getBnet());
        
//      Probabilities and observed nodes for the new case

        for (int i=0; i<numberOfNodes;i++){
          prob[i]=null;
          observed[i]=c.observed[i];
          auxNode = id.getNodeList().elementAt(i); 
          if ((auxNode.getKindOfNode()!=Node.UTILITY)&&(auxNode.getKindOfNode()!=Node.SUPER_VALUE)){
            //Probabilities for chance nodes don't change
          	if (c.prob[i] != null) {
                prob[i]=new double[c.prob[i].length];
                for (int j=0; j<c.prob[i].length; j++){
                    prob[i][j]=c.prob[i][j];
                }
              }
          }
          else{
          	//Utilities of value nodes of the id must be transformed into probabilities in cpn
          	if (c.prob[i] != null) {
          		prob[i]=new double[2];
          		probOfYes = CooperPolicyNetwork.directCooperTransformation(prob[i][0],cpn.getMaximumUtility(auxNode),cpn.getMinimumUtility(auxNode));
          		prob[i][0]= probOfYes;
          		prob[i][1]= 1-probOfYes;
          	}
          }

        }
        
 
//      Evidence for the new case
        evi = new Evidence();
        eviInID = c.getEvidence();
		
        for (int i=0;i<eviInID.getVariables().size();i++){
        	evi.putValue((FiniteStates)(cpn.getNode(eviInID.getVariable(i).getName())),eviInID.getValue(i));
        }

      
    }


/**
 * Saves a case to a file.
 * 
 * @param f
 *            the file.
 */

public void save(FileWriter f) throws IOException{
  PrintWriter p;

  p = new PrintWriter(f);

  p.print("// Case \n");
  p.print("//   Elvira format \n\n");


  p.print("case "+getIdentifier()+" { \n\n");


  getEvidence().save(f);

  saveAux(p);


  p.print ("}\n");
}


/**
 * Used by save.
 * @param p the PrintWriter where the case will be written.
 */

private void saveAux(PrintWriter p) throws IOException {

  int i,j,k;
  String n;

  p.print("// Case Properties\n\n");

  p.print("color = "   +getColor().toString() +";\n\n");

  if (!getComment().equals(""))
    p.print("comment = \""+ getComment()+"\";\n");

  if (getIsShown())
    p.print("shown = true;\n");
    else p.print("shown = false;\n");

  if (getPropagated())
    p.print("propagated = true;\n");
    else p.print("propagated = false;\n");

  if (!(getBnet().getName()).equals(""))
    p.print("BnetName = \""+ getBnet().getName()+"\";\n");

  p.print("Observed nodes\n[");

  for(i=0 ; i<getObserved().length ; i++)
    p.print(getNode(i).getName()+" = "+getObserved()[i]+",");
  p.print("]\n");

  p.print("Probabilities\n");

  for (i=0; i<prob.length; i++){
      p.print("[");
      if (prob[i]!=null){
          for (j=0; j<prob[i].length; j++)
            p.print(prob[i][j]+",");
      }
      p.print("],\n");
  }

}
    /***modifiers***/

    /**
     * Assigns the probabilities stored in array d to the node n
     * @param n The node whose probabilities are going to be modified
     * @param d The array which contains the probabilities for every state of node n
    */
    public void setProbOfNode(Node n, double[] d){
        if (bn.getNodeList().getId(n)!=-1){
            prob[bn.getNodeList().getId(n)]=d;
        }
    }
    
    

    /**
     * Sets as a new finding for this case the value v for the variable n
     * @param n The random node that is observed
     * @param v The number of the state of node n that is going to be set as finding
    */
    public void setAsFinding(FiniteStates n, int v){
             observed[bn.getNodeList().getId(n)]=true;
             ((Node)bn.getNode(n.getName())).setObserved(true);
             if (evi.indexOf(n)==-1)
                evi.insert(n,v);
                else evi.putValue(n,v);
    }

    /**
     * Sets as a new finding for this case the value v for the variable n
     * @param n The random node that is observed
     * @param v The number of the state of node n that is going to be set as finding
    */
    public void setAsFinding(Continuous n, double v){
             observed[bn.getNodeList().getId(n)]=true;
             ((Node)bn.getNode(n.getName())).setObserved(true);
                System.out.println("Hiolass2: "+evi.indexOf(n));
             if (evi.indexOf(n)==-1)
                evi.insert(n,v);
             else evi.putValue(n,v);
    }
    
    /**
     * Removes the finding defined by node n from the evidence of the current case
     * @param n The node that is going to be removed from the evidence
    */
    public void unsetAsFinding(Node n){
             observed[bn.getNodeList().getId(n)]=false;
             ((Node)bn.getNode(n.getName())).setObserved(false);
             
             if (n.getClass()==FiniteStates.class)
                 setProbOfNode((FiniteStates)n, ((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel().getCasesList().getCaseNum(0).getProbOfNode((FiniteStates)n));
             if (evi.indexOf(n)!=-1)
                evi.remove(n);
    }

    /**
     * Assigns the probabilities stored in array d to the node whose position in the list of
     * nodes is n
     * @param n The position of the node whose probabilities are going to be modified
     * @param d The array which contains the probabilities for every state of node n
    */
    public void setProbOfNode(int n, double[] d){
        if (n<bn.getNodeList().size())
            if (getNode(n).getClass()==FiniteStates.class)   
                prob[n]=d;
    }

    /**
     * Assigns the probability stored in d to the state number s of the node n
     * @param n The node whose probabilities are going to be modified
     * @param s The number of the state whose probability is going to be modified
     * @param d The new probabability
    */
    public void setProbOfStateNode(FiniteStates n, int s, double d){
        if (bn.getNodeList().getId(n)!=-1){
            FiniteStates fs=(FiniteStates)bn.getNodeList().getNode(n.getName());
            if((s>=0)&&(s<fs.getNumStates()))
               prob[bn.getNodeList().getId(n)][s]=d;
        }
    }

    /**
     * Assigns the probability stored in d to the state number s of the node n whose
     * position in the list of nodes is n
     * @param n The position of the node
     * @param s The number of the state whose probability is going to be modified
     * @param d The new probabability
    */
    public void setProbOfStateNode(int n, int s, double d){
        if (n<bn.getNodeList().size()){
            if (getNode(n).getClass()==FiniteStates.class){
                FiniteStates fs=(FiniteStates)bn.getNodeList().elementAt(n);
                if((s>=0)&&(s<fs.getNumStates()))
                   prob[n][s]=d;
            }
        }
    }

    /**
     * Sets as s the identifier of the case
     * @param s The String defining new identifier of the case
     */
    public void setIdentifier(String s){
        identifier=s;
    }

    /**
     * Sets as s the comment of the case
     * @param s The String defining new comment of the case
     */
    public void setComment(String s){
        comment=s;
    }

    /**
     * If b is true, the case will be shown. In other case, it will not.
     * @param b The boolean value
     */
    public void setIsShown(boolean b){
        isShown=b;
    }

    /**
     * Sets as c the color of the case
     * @param c The Color defining the color of the case
     */
    public void setColor(Color c){
      color = c;
    }

    /**
     * Sets as observed such nodes in the BN whose position in the array obs have the true value.
     * @param obs The array containing the boolean values.
     */
    public void setObserved(boolean[] obs){
        for (int o=0; o<obs.length; o++)
            observed[o]=obs[o];
    }

    /**
     * If p is true means that the evidence of this case has been propagated
     * @param p The boolean value indicating if the evidence has been propagated
     */
    public void setPropagated(boolean p){
        propagated=p;
    }

    /**
     * Adds to the evidence of this case the finding f. A finding is defined by an observed value for a node.
     * @param f The finding to add to the case
     */
     public void addFinding(Finding f){
        Node n=bn.getNodeList().getNode(f.getNode().getName());
        if (n.getClass()==FiniteStates.class){
            FiniteStates fs=(FiniteStates) n;
            observed[bn.getNodeList().getId(n)]=true;
            double prob[]=new double[fs.getNumStates()];
            Vector states=fs.getStates();
            for (int s=0; s<fs.getNumStates(); s++)
                if (s==f.getStateNode())
                    prob[s]=1;
                    else prob[s]=0;
            setProbOfNode(fs, prob);
        }else if(n.getClass()==Continuous.class){
            
            observed[bn.getNodeList().getId(n)]=true;
            
        }
    }

/*** accesors ***/

    /**
     * Gets true if the evidence of the case has been propagated and false if not.
    */
    public boolean getPropagated(){
        return propagated;
    }

    /**
     * Gets the array of boolean values corresponding to the positions of the observed nodes in this case
    */
    public boolean[] getObserved(){
        return observed;
    }

    /**
     * Gets true if the case must be shown; otherwise returns false
    */
    public boolean getIsShown(){
        return isShown;
    }

    /**
     * Gets true or false depending on if the node n has been observed or not in this case
     * @param n The node to be asked for an observation
    */
    public boolean getIsObserved(Node n){
        int posn=bn.getNodeList().getId(n);
        if (posn!=-1)
            return observed[posn];
            else return false;
    }

    /**
     * Gets the array of probabilities of the node n in this case
     * @param n The node to be asked for its probability
    */
    public double[] getProbOfNode(FiniteStates n){

        int posn=bn.getNodeList().getId(n);
        if (posn!=-1) {

            return prob[posn];
 
        }

        else return null;
    }

    /**
     * Gets the array of probabilities of the node whose position is n in the BN
     * @param n The position of the node to be asked for its probability
    */
    public double[] getProbOfNode(int n){
        if (n<bn.getNodeList().size())

                return prob[n];
        return null;
    }

    /**
     * Gets the probability of the state number s of the node n
     * @param n The node to be asked for
     * @param s The number of the state of the node to be asked for its probability
    */
    public double getProbOfStateNode(FiniteStates n, int s){
        if (bn.getNodeList().getId(n)!=-1){
            FiniteStates fs=(FiniteStates)bn.getNodeList().getNode(n.getName());
            if((s>=0)&&(s<fs.getNumStates()))
               return prob[bn.getNodeList().getId(n)][s];
            else return 0.0;
        }
        else return 0.0;
    }

    /**
     * Gets the probability of the state number s of the node n whose position is n in the BN
     * @param n The position of the node to be asked for
     * @param s The number of the state of the node to be asked for its probability
    */
    public double getProbOfStateNode(int n, int s){
        if (n<bn.getNodeList().size()){
            if (getNode(n).getClass()==FiniteStates.class){
                FiniteStates fs=(FiniteStates)bn.getNodeList().elementAt(n);
                if((s>=0)&&(s<fs.getNumStates()))
                    return prob[n][s];
                else return 0.0;
            }
            else{//Utility and super value nodes
            	return prob[n][0];
            }
        }
        else return 0.0;
    }

   /**
     * Gets the identifier of the case
    */
    public String getIdentifier(){
        return identifier;
    }

   /**
     * Gets the comment of the case
    */
    public String getComment(){
        return comment;
    }

   /**
     * Gets the color of the case
    */
    public Color getColor(){
      return color;
    }

   /**
     * Gets the position of the node in the list of nodes
     * @param n The node to be asked for
    */
    public int getPositionNode(Node n){
        return bn.getNodeList().getId(n);
    }

   /**
     * Gets the node whose position is n in the list of nodes
     * @param n The position
    */
    public Node getNode(int n){
        return (Node)bn.getNodeList().elementAt(n);
    }

   /**
     * Gets the name of the state of the observed node whose position is n in the list of nodes.
     * If the node is not observed returns the empty string.
     * @param n The position of the node
    */
    public String getObservedStateNode(int n){
        if (getNode(n).getClass()==FiniteStates.class){
            String state=new String("");
            int posc=0;
            boolean found=false;
            while (!found && posc<prob[n].length){
                if (prob[n][posc]==1){
                    state=((FiniteStates)getNode(n)).getState(posc);
                    found=true;
                }
                else posc++;
            }
            return state;
        }else 
            return null;
    }

    /**
     * Gets the value of continuous observed Node
     */
    public double getObservedValueNode(int n){
     
        if (getNode(n).getClass()==Continuous.class){
            
            return getEvidence().getContinuousValue(getEvidence().indexOf(getNode(n)));
            
        }else
            return -1.0;
        
    }
    /**
     * Gets the evidence of the case
    */
    public Evidence getEvidence(){
        return evi;
    }

    /**
     * Gets the Bayesian net in which the case is beein processed
    */
    public Bnet getBnet(){
        return bn;
    }

    /**
     * Prints the basic information associated to the case to the standard output
    */
    public void showCase(){

        for (int i=0; i<bn.getNodeList().size(); i++){
            if (getNode(i).getClass()==FiniteStates.class)
                for (int j=0; j<prob[i].length; j++)
                    System.out.println("Prob["+i+"]["+j+"]="+prob[i][j]);
            if (observed[i])
                System.out.println("Observed");
                else System.out.println("Not Observed");
        }
    }
    /** Returns true if this case is equals to case c. Two cases are equal when they have the same set of findings
     * @param c The case to be compared to
    */
    public boolean equals(Case c){
        if (c!=null){
            Evidence evio=c.getEvidence();
            int obs=0;
            boolean eq=true;
            int numobs=bn.getNodeList().size();
            while (obs<numobs && eq)
                if (observed[obs]==c.getObserved()[obs]==true){
                    Node n=bn.getNodeList().elementAt(obs);
                    if (n.getClass()==FiniteStates.class){
                        if (evi.getValue((FiniteStates)n)!=evio.getValue((FiniteStates)n))
                            eq=false;
                        else obs++;
                    }else if (n.getClass()==Continuous.class){
                        if (evi.getContinuousValue((Continuous)n)!=evio.getContinuousValue((Continuous)n))
                            eq=false;
                        else obs++;
                    }

                }
                else eq=false;
            return eq;
        }
        else return false;
    }
//Alberto Ruiz    
    /**
     * Gets the index of the state of the observed node whose
     * position is "n" inside of the list nodes
     * If there is any error the method return -1,
     * otherway it will return the state´s index
     */
    public int getIndexObservedStateNode(int n){
    	if (getNode(n).getClass()==FiniteStates.class){
            String state=new String("");
            int posc=0;
            boolean found=false;
            while (!found && posc<prob[n].length){
                if (prob[n][posc]==1){
                    state=((FiniteStates)getNode(n)).getState(posc);
                    found=true;
                }
                else posc++;
            }
            return posc;
        }else 
            return -1;
    }
    
    //Fin Alberto Ruiz

     public boolean propagate() {
       //propagamos
       InferencePanel infpanel=((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel();
       Vector cpots=infpanel.propagateMethod(this.getBnet(),this, infpanel.getInferenceMethod(), infpanel.getParameters()).results;

       if (bn.getClass() == IDiagram.class) {
	 bn.setCompiledPotentialList(cpots);
       }

       //obtenemos las probabilidades para cada nodo no observado
       boolean[] observed=getObserved();

       boolean correct=true;
       for (int j=0; j<bn.getNodeList().size() && correct; j++)
          if (!observed[j] && bn.getNodeList().elementAt(j).getClass()==FiniteStates.class){
             FiniteStates fs=(FiniteStates)bn.getNodeList().elementAt(j);

//             ((Node)bn.getNodeList().elementAt(j)).setObserved(false);

             double[] postprob=new double[fs.getNumStates()];
             boolean found = false;
	         int  ip= 0;
	         while (!found && ip<cpots.size()) {
	      	        Potential p = (Potential) cpots.elementAt(ip);
	      	        //p.showResult();
                    if (fs.equals(((FiniteStates)p.getVariables().elementAt(0)))){
                        found=true;
                        postprob = ((PotentialTable) p).getValues();

                    }
                    else ip++;
	        }
            int ps=0;
            Double nan=new Double(Double.NaN);
            while (correct && ps<postprob.length){
                  Double d=new Double(postprob[ps]);
                  if (d.equals(nan))
                      correct=false;
                      else ps++;
            }
	        if (correct){
                setProbOfNode(fs, postprob);
            }
          }
//          else ((Node)bn.getNodeList().elementAt(j)).setObserved(true);


//       Elvira.getElviraFrame().setNodeName(c.getIdentifier());
//       Elvira.getElviraFrame().setColorNodeName(c.getColor());
       setPropagated(true);
       setIsShown(false);
/*       if (cases.posCase(c)>cases.getLastShown())
           cases.setLastShown(cases.posCase(c));
       if (cases.posCase(c)<cases.getFirstShown())
           cases.setFirstShown(cases.posCase(c));

       repaint();
*/       return correct;
   }

   /**
    *Return true if case has a continuous node as evidence
    */
    public boolean hasContinuous(){
        
        return hasContinuous;
    }
     
 
    
}//end of class Case
