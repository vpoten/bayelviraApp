/*
 * ShenoyShaferHC.java
 *
 * Created on 30 de junio de 2004, 12:09
 */

package elvira.inference.clustering.impreciseprob;


import elvira.inference.clustering.*;
import elvira.inference.clustering.multiplemessaging.*;
import elvira.inference.Propagation;
import elvira.*;
import elvira.potential.*;
import elvira.parser.ParseException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.io.*;


/**
 * This class implements an approximate propagation algorithm for intervals of
 * probabilities. The algorithm is based in the Shenoy-Shafer message passing
 * over a join tree and a Hill Climbing algorithm.
 * A run of the algorithm is designed to find the min (max) for the probability of
 * a given case x_i of an interest variable X_i, given the evidence (E=e):
 * min P(X_i=x_i | e) = min P(X_i=x_i, e) / P(e)
 * The algorithm makes the following steps:
 * - To convert the interval conditional distributions into credal sets using probability
 * trees (PTreeCredalSet).
 * - These credal sets are then "restricted" to the given evidence E=e
 * - A Join tree is then built from the restricted credal sets. This join tree contains
 * two messages from each clique A to another B. The first system of message is used to
 * propagated the given evidence (as usually) and the second system of message is used
 * to propagate the given evidence and the observation of one of the cases of the
 * interest variable (X_i=x_i).
 * - An initial propagation (from leaves to root) is carried out in the join tree with the
 * two system of messages.
 * - Then, several propapations from root to leaves, and leaves to root, are carried out.
 * Each time a clique is visited, we simulate its transparent variables one by one.
 *     + This operation needs to calculate the a posteriori of current transparent
 *       variable T_i with the two system of messages: P_1(T_i) and P_2(T_i).
 *     + Then, we calculate P(T_i)=P_2(T_i)/P_1(T_i). This calculus gives us a vector of
 *       different values P(X_i=x_i, e) / P(e) (that is, the function we want to
 *       minimize and maximaze).
 *     + We select the case t_i \in T_i that minimize or maximize P(X_i=x_i, e) / P(e)
 * Minimum and maximum values for P(X_i=x_i, e) / P(e) are the output for X_i=x_i
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 * @author Manuel Gómez Olmedo (mgomez@decsai.ugr.es)
 */
public class ShenoyShaferHC extends Propagation {
  
  /**
   * A join tree.
   */
  protected JoinTree joinTree;
  
  /**
   * A <code>Hashtable</code> with pairs Node-NodeJoinTree.
   * It will be used to find quickly the marginal (NodeJoinTree) for each variable (Node).
   */
  protected Hashtable<Node,NodeJoinTree> marginalCliques;
  
  /**
   * Configuration of transparent variables in the join tree
   */
  protected Configuration confTransparentVars;
  
  
  /**
   * Configuration used to put an observation of the variable of interest in the second
   * system of messages.
   */
  private Configuration confCurrentVar;
  
  /**
   * The number of propagations to carry out in the Join Tree. By default we use 3 propagations
   */
  private int nsteps=3;
  
  /**
   * Creates a new instance of ShenoyShaferHC
   * @param b the Bnet (Credal network) to be propagated
   * @param e the set of observed variables (Evidence set)
   */
  public ShenoyShaferHC(Bnet b, Evidence e) {
    RelationList irTree;
    
    confTransparentVars=new Configuration();
    network = b;
    observations=e;
    joinTree=new JoinTreeMultiple(b);
    irTree = getInitialRelations();
    irTree.restrictToObservations(observations);
    initCliques(irTree);
    for(int i=0;i<irTree.size();i++){
      Vector transVars=irTree.elementAt(i).getValues().getListTransparents();
      for(int j=0;j<transVars.size();j++){
        confTransparentVars.putValue((FiniteStates)transVars.elementAt(j),0);
      }
    }
    joinTree.setLabels();
  }
  
  /**
   * Sets the number of propagations (nsteps instance variable)
   */
  public void setNsteps(int nprops){
    nsteps=nprops;
  }
  
  /**
   * Puts 0 in the value of every transparent variable of <code>confTransparentVars</code>
   */
  private void initConfTransparentVars(){
    for(int i=0;i<confTransparentVars.size();i++){
      confTransparentVars.putValue((FiniteStates)confTransparentVars.getVariables().elementAt(i),0);
    }
  }
  
  /**
   * Transforms the Potential of one of the original relations. If the Potential
   * is a PotentialInterval then it is transformed into a PTreeCredalSet. If
   * the Potential is a PTreeCredalSet then it is not modified. Otherwise an
   * error is produced.
   * @ param r the <code>Relation</code> to be transformed.
   * @param r the Relation to be transformed
   * @return The new transformed Relation
   */
  public elvira.Relation transformInitialRelation(elvira.Relation r) {
    if(r.getValues().getClass()==PTreeCredalSet.class){
      return r;
    } else if(r.getValues() instanceof PotentialTable){//to convert PotentialTable and PotentialConvexSet
      PTreeCredalSet pTreeCS=new PTreeCredalSet((PotentialTable)r.getValues());
      Relation rNew=new Relation();
      rNew.setVariables(r.getVariables().copy());
      rNew.setKind(r.getKind());
      rNew.setValues(pTreeCS);
      return rNew;
    } else if(r.getValues() instanceof PotentialInterval){
      PTreeCredalSet pTreeCS=new PTreeCredalSet((PotentialInterval)r.getValues());
      Relation rNew=new Relation();
      rNew.setVariables(r.getVariables().copy());
      rNew.setKind(r.getKind());
      rNew.setValues(pTreeCS);
      return rNew;
    } else{
      System.out.print("Error in ShenoyShaferHC.transformInitialRelation(Relation r): ");
      System.out.println("Potentials of class "+r.getValues().getClass()+ "cannot be propagated with this class");
      System.exit(1);
    }
    return null;
  }
  
  /**
   * Initializes the potentials in the join tree as objects of
   * class <code>PTreeCredalSet</code>. The potentials are taken from
   * a given RelationList.
   * @param ir the <code>RelationList</code> which contains the initial relations.
   */
  public void initCliques(RelationList ir) {
    Potential potTree;
    NodeJoinTree node;
    Relation r, r2;
    int i, j;
    Family family;
    ArrayList families;
    
    // First we assign families to cliques
    joinTree.assignFamilies(ir);
    
    marginalCliques = new Hashtable<Node,NodeJoinTree>();
    // Secondly we create unitary potentials for all the cliques
    
    for (i=0 ; i<joinTree.getJoinTreeNodes().size() ; i++) {
      node = joinTree.elementAt(i);
      r = node.getNodeRelation();
      potTree = makeUnitPotential(r.getVariables());
      /*potTree = new PTreeCredalSet(r.getVariables().getNodes());
      potTree.setTree(ProbabilityTree.unitTree());*/
      // potTree.updateSize();
      r.setValues(potTree);
    }
    
    // Now, we initialize the potentials using the families associated
    // to each clique
    
    for(i=0;i<joinTree.getJoinTreeNodes().size();i++){
      node = joinTree.elementAt(i);
      r2 = node.getNodeRelation();
      potTree = r2.getValues();
      
      families = node.getFamilies();
      if (families.size() != 0){ // not unitary potential
        // creating the potential by combination
        for(j=0;j<families.size();j++){
          family = (Family)families.get(j);
          r = family.getRelation();
          potTree = potTree.combine(r.getValues());
          // if (r.isConditional()) {
          marginalCliques.put(r.getVariables().elementAt(0),node);
          //  }
        }
        //assigning the potential
        r2.setValues(potTree);
      }
    }
  }
  
  /**
   * Initializes all the messages to 1
   */
  public void initMessages() {
    int i,j;
    NodeJoinTree node;
    Relation r;
    //PTreeCredalSet pot;
    Potential pot;
    NeighbourTreeList ntl;
    NeighbourTreeMultiple nt;
    
    for (i=0 ; i<joinTree.getJoinTreeNodes().size() ; i++) {
      // Set the messages
      node = joinTree.elementAt(i);
      ntl = node.getNeighbourList();
      for (j=0 ; j<ntl.size() ; j++) {
        nt = (NeighbourTreeMultiple)ntl.elementAt(j);
        r = nt.getMessage();
        pot = makeUnitPotential(r.getVariables());
        
        r.setValues(pot);
        r = nt.getAdditionalMessage(0);
        pot = makeUnitPotential(r.getVariables());
        
        r.setValues(pot);
      }
    }
  }
  
  /**
   * Carries out a propagation for a target FiniteStates var.
   * The result of propagation is returned by this method as a
   * PotentialIntervalTable
   * @param var the target FiniteStates variable.
   * @return A PotentialIntervalTable with the posterior distribution for variable var
   */
  public PotentialIntervalTable propagate(FiniteStates var){
    Vector<FiniteStates> varsResults;
    PotentialIntervalTable potInterv;
    NodeJoinTreeMultiple njt,root;
    Relation rel,rel2;
    int step;
    
    root = (NodeJoinTreeMultiple)joinTree.elementAt(0);
    
    // Create a PotentialIntervalTable to save the result for var
    varsResults=new Vector<FiniteStates>();
    varsResults.addElement(var);
    potInterv=new PotentialIntervalTable(varsResults);
    potInterv.setMaxValue(0.0);
    potInterv.setMinValue(1.0);
    
    confCurrentVar=new Configuration();
    for(int j=0;j<var.getNumStates();j++){
      njt=(NodeJoinTreeMultiple)marginalCliques.get(var);
      rel=njt.getNodeRelation();
      njt.putAdditionalRelation(rel.copy());
      confCurrentVar.putValue(var,j);
      rel2=njt.getAdditionalRelation(0);
      rel2.getValues().instantiateEvidence(confCurrentVar);
      
      // Find min value for state j in variable var
      initMessages();
      initConfTransparentVars();
      navigateUp(root);
      step=1;
      while (step<nsteps){
        navigateDownUp(root,true,potInterv);
        step=step+2;
      }
      //simulateTransparentsInClique(root,true,potInterv);
      
      // Find max value for state j in variable var
      initMessages();
      initConfTransparentVars();
      navigateUp(root);
      step=1;
      while (step<nsteps){
         navigateDownUp(root,false,potInterv);
         step=step+2;
      }
      //simulateTransparentsInClique(root,false,potInterv);
      
      njt.removeAdditionalRelation(0);
    }
    
    return potInterv;
  }
  
  /**
   * Carries out a propagation. The results of propagation are saved into the file resultFile
   * @param resultFile the name of the file where the results will be stored.
   * @throws java.io.IOException when the file resulFile cannot be opened
   */
  public void propagate(String resultFile)  throws IOException{
    int n=getNumberOfInterest();
    FiniteStates var;
    PotentialIntervalTable potInterv;
    
    for(int i=0;i<n;i++){
      var=(FiniteStates)getVarInterest(i);
      potInterv=propagate(var);
      results.addElement(potInterv);
    }
    System.out.println("Saving results into "+resultFile);
    saveResultsAsNetwork(resultFile);
  }
  
  /**
   * Sends a message from a join tree node to another one.
   * The message is computed by combining all the messages inwards
   * the sender except that one comming from the recipient.
   * @param sender the node that sends the message.
   * @param recipient the node that receives the message.
   */
  public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient) {
    NeighbourTreeList list = sender.getIncomingMessages();
    NeighbourTreeMultiple nt,outwards=null;
    NodeJoinTreeMultiple neighbour;
    Relation rel,rel2;
    Potential pot,pot2,aux,aux2=null;
    Vector separator=null;
    
    aux = sender.getNodeRelation().getValues();
    if(((NodeJoinTreeMultiple)sender).getAdditionalRelationsSize()>0){
      aux2 = ((NodeJoinTreeMultiple)sender).getAdditionalRelation(0).getValues();
    }
    aux=aux.restrictVariable(confTransparentVars);
    if(aux2!=null){
      aux2=aux2.restrictVariable(confTransparentVars);
    }
    pot = makeUnitPotential();
    pot2 =makeUnitPotential();
    for (int i=0 ; i<list.size() ; i++) {
      nt = (NeighbourTreeMultiple)list.elementAt(i);
      neighbour = (NodeJoinTreeMultiple)nt.getOppositeMessage().getNeighbour();
      rel = nt.getMessage();
      rel2 = nt.getAdditionalMessage(0);
      
      if (recipient != neighbour) {
        pot = pot.combine(rel.getValues());
        pot2 = pot2.combine(rel2.getValues());
      } else {
        separator =  rel.getVariables().getNodes();
        //outwards = nt;
        outwards = (NeighbourTreeMultiple)nt.getOppositeMessage();
      }
    }
    // Now combine with the potential in the node.
    pot = pot.combine(aux);
    if(aux2==null){
      pot2 = pot2.combine(aux);
    } else{
      pot2 = pot2.combine(aux2);
    }
    pot = pot.marginalizePotential(separator);
    pot2 = pot2.marginalizePotential(separator);
    rel = outwards.getMessage();
    rel2 = outwards.getAdditionalMessage(0);
    
    rel.setValues(pot);
    rel2.setValues(pot2);
  }
  
 
  /**
   * Creates a new PTreeCredalSet initialized with a unitary ProbabilityTree.
   * It contains an empty list of variables
   * @return A new PTreeCredalSet initialized with a unitary ProbabilityTree
   */
  Potential makeUnitPotential(){
    PTreeCredalSet pot;
    pot = new PTreeCredalSet();
    pot.setTree(ProbabilityTree.unitTree());
    return pot;
  }
  
   /**
   * Creates a new PTreeCredalSet initialized with a unitary ProbabilityTree.
   * The list of variables will be a copy of the NodeList parameter.
   * @return A new PTreeCredalSet initialized with a unitary ProbabilityTree
   * @param nlist The NodeList to use as list of variables of the new PTreeCredalSet
   */
  Potential makeUnitPotential(NodeList nlist){
    PTreeCredalSet pot;
    pot = new PTreeCredalSet(nlist.getNodes());
    pot.setTree(ProbabilityTree.unitTree());
    return pot;
  }
  
  /**
   * Obtains the marginal distribution for variable var by combining the potential in
   * clique njt with all the incoming messages, and marginalizing the resulting potential
   * to var. Previous to the combination with incoming messages, potential in clique njt
   * is restricted to the current configuration of transparent variables
   * (confTransparentVars). If var is a transparent variable then is not included in
   * the restriction operation.
   * @param var The target variable
   * @param njt The NodeJoinTree (clique) used to obtain the posterior distribution for var
   * @return The marginal distribution for variable var
   */
  private Potential  getPosteriorDistributionOf(FiniteStates var,NodeJoinTreeMultiple njt){
    NeighbourTreeList list = njt.getIncomingMessages();
    Potential pot,pot2;
    NeighbourTreeMultiple nt;
    Relation rel,rel2;
    Configuration conf;
    
    conf=confTransparentVars.duplicate();
    if(var.getTransparency()==FiniteStates.TRANSPARENT){
      conf.remove(var);
    }
    pot = njt.getNodeRelation().getValues();
    pot = pot.restrictVariable(conf);
    if(((NodeJoinTreeMultiple)njt).getAdditionalRelationsSize()>0){
      pot2 = ((NodeJoinTreeMultiple)njt).getAdditionalRelation(0).getValues();
      pot2 = pot2.restrictVariable(conf);
    } else{
      pot2 = pot;
    }
    for (int i=0 ; i<list.size() ; i++) {
      nt = (NeighbourTreeMultiple)list.elementAt(i);
      rel = nt.getMessage();
      rel2 = nt.getAdditionalMessage(0);
      pot = pot.combine(rel.getValues());
      pot2 = pot2.combine(rel2.getValues());
    }
    Vector<FiniteStates> vTrans=new Vector<FiniteStates>();
    vTrans.addElement(var);
    pot = pot.marginalizePotential(vTrans);
    pot2 = pot2.marginalizePotential(vTrans);
    pot = pot2.divide(pot);
    return pot;
  }
  
  /**
   * Simulates the transparent variables in clique njt. If the new configuration
   * of transparent variables produces a best minimum (maximum) for the interval
   * of the current variable and current state, then the new best value is updated
   * in the PotentialIntervalTable potInterv.
   * @param njt the clique where the transparent variables are simulates
   * @param findMin true if we are finding the min value for the state of a variable of
   * interest, false if we are finding the max.
   * @param potInterv the PotentialIntervalTable where the current min and max are maintained
   */
  private void simulateTransparentsInClique(NodeJoinTreeMultiple njt,boolean findMin,
          PotentialIntervalTable potInterv){
    FiniteStates trans;
    Configuration conf;
    Potential pot;
    int minmax;
    double minmaxVal,val;
    boolean thereischange=false;
    
    Vector vTrans=njt.getNodeRelation().getValues().getListTransparents();
    do{ // Repeat while we continue improving min or max
      thereischange=false;
      for(int i=0;i<vTrans.size();i++){
        trans = (FiniteStates)vTrans.elementAt(i);
        pot=getPosteriorDistributionOf(trans,njt);
        conf=new Configuration();
        conf.putValue(trans,0);
        minmax=0;
        minmaxVal=pot.getValue(conf);
        for(int j=1;j<trans.getNumStates();j++){
          conf.putValue(trans,j);
          val=pot.getValue(conf);
          if(findMin){
            if(val<minmaxVal){
              minmax=j;
              minmaxVal=val;
            }
          } else{
            if(val>minmaxVal){
              minmax=j;
              minmaxVal=val;
            }
          }
        }
        int currentMinMax=confTransparentVars.getValue(trans);
        if(currentMinMax!=minmax){
          confTransparentVars.putValue(trans,minmax);
          thereischange=true;
        }
        if(findMin){
          potInterv.setMinValue(confCurrentVar,minmaxVal);
        } else{
          potInterv.setMaxValue(confCurrentVar,minmaxVal);
        }
      } // end for
    }while(thereischange);
  }
  
  /**
   * Sends messages from leaves to root (sender).
   * @param sender the <code>NodeJoinTree</code> that sends the request.
   */
  private void navigateUp(NodeJoinTree sender) {
    NeighbourTreeList list;
    NodeJoinTree other;
    int i;
    
    list = sender.getNeighbourList();
    for (i=0 ; i<list.size() ; i++) {
      other = list.elementAt(i).getNeighbour();
      navigateUp(sender,other);
    }
  }
  
  /**
   * Sends messages from leaves to root (sender) through the branch recipient.
   * @param sender the <code>NodeJoinTree</code> that sends the request.
   * @param recipient the <code>NodeJoinTree</code> that receives the request.
   */
  private void navigateUp(NodeJoinTree sender, NodeJoinTree recipient) {
    NeighbourTreeList list;
    NodeJoinTree other;
    int i;
    
    // Nodes to which the message will be sent downwards.
    list = recipient.getNeighbourList();
    for (i=0 ; i<list.size() ; i++) {
      other = list.elementAt(i).getNeighbour();
      //if (other.getLabel() != sender.getLabel()) {
      if (other != sender) {
        navigateUp(recipient,other);
      }
    }
    sendMessage(recipient,sender);
  }
  
  /**
   * Sends messages from root (sender) to leaves.
   * @param sender the <code>NodeJoinTree</code> that sends the request.
   * @param findMin true if we are finding the min value for the state of a variable of
   * interest, false if we are finding the max.
   * @param potInterv the PotentialIntervalTable where the current min and max are maintained
   */
  private void navigateDown(NodeJoinTree sender,boolean findMin,
          PotentialIntervalTable potInterv) {
    NeighbourTreeList list;
    NodeJoinTree other;
    int i;
    
    simulateTransparentsInClique((NodeJoinTreeMultiple)sender,findMin,potInterv);
    list = sender.getNeighbourList();
    for (i=0 ; i<list.size() ; i++) {
      other = list.elementAt(i).getNeighbour();
      sendMessage(sender,other);
      navigateDown(sender,other,findMin,potInterv);
    }
  }
  
  /**
   * Send messages from root (sender) to leaves through the brach recipient.
   * @param sender the <code>NodeJoinTree</code> that sends the request.
   * @param recipient the <code>NodeJoinTree</code> that receives the request.
   * @param findMin true if we are finding the min value for the state of a variable of
   * interest, false if we are finding the max.
   * @param potInterv the PotentialIntervalTable where the current min and max are maintained
   */
  private void navigateDown(NodeJoinTree sender, NodeJoinTree recipient,boolean findMin,
          PotentialIntervalTable potInterv) {
    NeighbourTreeList list;
    NodeJoinTree other;
    int i;
    
    simulateTransparentsInClique((NodeJoinTreeMultiple)recipient,findMin,potInterv);
    // Nodes to which the message will be sent downwards.
    list = recipient.getNeighbourList();
    for (i=0 ; i<list.size() ; i++) {
      other = list.elementAt(i).getNeighbour();
      //if (other.getLabel() != sender.getLabel()) {
      if (other != sender) {
        sendMessage(recipient,other);
        navigateDown(recipient,other,findMin,potInterv);
      }
    }
  }
  
  /**
   * Sends messages from root (sender) to leaves, and then from leaves
   * to root.
   * @param sender the <code>NodeJoinTree</code> that sends the request.
   * @param findMin true if we are finding the min value for the state of a variable of
   * interest, false if we are finding the max.
   * @param potInterv the PotentialIntervalTable where the current min and max are maintained
   */
  private void navigateDownUp(NodeJoinTree sender,boolean findMin,
          PotentialIntervalTable potInterv) {
    NeighbourTreeList list;
    NodeJoinTree other;
    Relation sep;
    int i;
    boolean modified=false;
    
    simulateTransparentsInClique((NodeJoinTreeMultiple)sender,findMin,potInterv);
    list = sender.getNeighbourList();
    for (i=0 ; i<list.size() ; i++) {
      other = list.elementAt(i).getNeighbour();
      sep = list.elementAt(i).getMessage();
      sendMessage(sender,other);
      navigateDownUp(sender,other,findMin,potInterv);
      modified=true;
    }
    if(modified)
       simulateTransparentsInClique((NodeJoinTreeMultiple)sender,findMin,potInterv);
  }
  
  /**
   * Sends messages from root (sender) to leaves, and then from leaves
   * to root, through the branch towards node <code>recipient</code>.
   * @param sender the <code>NodeJoinTree</code> that sends the request.
   * @param recipient the <code>NodeJoinTree</code> that receives the request.
   * @param findMin true if we are finding the min value for the state of a variable of
   * interest, false if we are finding the max.
   * @param potInterv the PotentialIntervalTable where the current min and max are maintained
   */
  private void navigateDownUp(NodeJoinTree sender, NodeJoinTree recipient,boolean findMin,
          PotentialIntervalTable potInterv) {
    NeighbourTreeList list;
    NodeJoinTree other;
    Relation sep;
    int i;
    boolean modified=false; // allow  to see if we have send any message to one adjacent clique: (we are not in a leaf node)
    
    simulateTransparentsInClique((NodeJoinTreeMultiple)recipient,findMin,potInterv); //simulate when we navigate down
    // Nodes to which the message will be sent downwards.
    list = recipient.getNeighbourList();
    for (i=0 ; i<list.size() ; i++) {
      other = list.elementAt(i).getNeighbour();
      if (other != sender) {
        sep = list.elementAt(i).getMessage();
        sendMessage(recipient,other);
        navigateDownUp(recipient,other,findMin,potInterv);
        modified=true;
      }
    }
    if(modified)  // in order to not simulate leaf nodes twice
      simulateTransparentsInClique((NodeJoinTreeMultiple)recipient,findMin,potInterv); //simulate when we navigate up
    sendMessage(recipient,sender);
  }
  
  
  public static void main(String args[]) throws ParseException, IOException {
    Network b;
    ShenoyShaferHC propagation;
    Evidence e=new Evidence();
    String resultsFile="tmp.out";
    Vector<String> varsInterest = new Vector<String>();
    int nprops=3;
    
    if (args.length < 1){
      System.out.println("ERROR:Too few arguments.");
      System.out.println("Use: bnet.elv [Options]");
      System.out.println("OPTIONS: ");
      System.out.println(" -evi <evidenceFile.evi> -->  The evidence file");
      System.out.println(" -out <resultsFile.out> --> The file with the results (if this option is not included then the output will be tmp.out)");
      System.out.println(" -interest <varName> --> Name of a variable of interest. If no -interest option is used then all non-observed variables are included.");
      System.out.println(" -steps <intValue> --> Number of propagations (>=3 , default value 3)");
      System.exit(0);
    }
    b=Network.read(args[0]);
    for (int i=1; i<args.length; i++){
      if (args[i].equals("-evi")){
        e=new Evidence(args[i+1],b.getNodeList());
        i++;
      } else if(args[i].equals("-out")){
        resultsFile=args[i+1];
        i++;
      } else if(args[i].equals("-interest")){
        varsInterest.add(args[i+1]);
        i++;
      } else if(args[i].equals("-steps")) {
        nprops=Integer.valueOf(args[i+1]);
        i++;
      }
    }
    propagation=new ShenoyShaferHC((Bnet)b,e);
    propagation.setNsteps(nprops);
    
    for(int i=0;i<varsInterest.size();i++){
      propagation.insertVarInterest(b.getNode((String)varsInterest.elementAt(i)));
    }
    propagation.obtainInterest(); // If there are not any interest variable put all the non-observed ones
    propagation.propagate(resultsFile);
  }
}
