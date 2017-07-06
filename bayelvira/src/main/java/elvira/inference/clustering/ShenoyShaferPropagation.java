/**
 * ShaferShenoyPropagation.java
 *
 * This class implements Shenoy-Shafer probability propagation by
 * using a binary join tree as graphical structure. Some details 
 * concerning the implementation are:
 *    - probability trees are used to represent distributions
 * 
 * Created on 20 de abril de 2004, 11:37
 */

package elvira.inference.clustering;


import elvira.*;
import elvira.potential.*;
import java.io.IOException;
import elvira.parser.ParseException;
import java.util.Hashtable;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 *
 * @author  julia@info-ab.uclm.es, jgamez@info-ab.uclm.es
 * @version 0.1
 */
public class ShenoyShaferPropagation extends elvira.inference.Propagation {

    /**
     * The binary join tree.
     */  
     public JoinTree binTree; 
     
     /**
      * A <code>Hashtable</code> with the <code>NodeJoinTree</code>
      * that will be used to obtain the marginal for each variable.
      */
      public Hashtable marginalCliques;
      
     /**
      * Another <code>Hashtable</code> with the <code>NodeJoinTree</code>
      * that will be used to obtain the clique where we should introduce the
      * evidence about a variable in our "atomic" propagations
      */
      public Hashtable singleCliques;

     /**
      * indicates if the model is constructed for a given query or for
      * general (several) inferences
      */
      public boolean queryOriented = false;
  
     /**
      * indicates if new evidence has arrived to a clique. It is used
      * to decide if a clique should sent information to its parent in
      * the upward phase
      */

      private Hashtable updated;

     /**
      * Contains the clique being used as current root of the join tree.
      * It is used during propagation.
      */

      private NodeJoinTree currentRoot;

     /**
      * used for depuration
      */
      public static boolean debug = false;


/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * <li> Output file.
 * <li> Evidence file.
 * <li> The deletion sequence
 * </ol>
 * If the evidence file is omitted, then no evidences are
 * considered
 */

public static void main(String args[]) throws ParseException, IOException {
  FileInputStream networkFile;
  Bnet b;
  ShenoyShaferPropagation ssp;
  Evidence e;
  NodeList interest = new NodeList();
  FileInputStream evidenceFile, interestFile;
  boolean query;
                      
  if (args.length < 3){
    System.out.println("Too few arguments, the arguments are:");
    System.out.println("\tNetwork OutputFile query(yes|no) [evidenceFile] [interestFile]");
  }
  else{
    System.out.println("Loading network....");
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);
    System.out.println("....Network loaded.\n");
    if (args[2].equals("yes")) query=true;
    else query=false;        

    if (args.length == 5) {
      evidenceFile = new FileInputStream(args[3]);
      e = new Evidence(evidenceFile,b.getNodeList());
      
      interestFile = new FileInputStream(args[4]);
      interest = new NodeList(interestFile,b.getNodeList());
    }
    else if (args.length == 4) {
      // trying if args[3] is the evidence file
      try {
        evidenceFile= new FileInputStream(args[3]);
        e = new Evidence(evidenceFile,b.getNodeList());
        interest = new NodeList();
      }catch (ParseException pe){
        interestFile = new FileInputStream(args[3]);
        interest = new NodeList(interestFile,b.getNodeList());
        e = new Evidence();
      }
    }
    else { 
      e = new Evidence();
      interest = new NodeList();
    }
  
    ssp = new ShenoyShaferPropagation(b,e,query,interest);
    ssp.introduceEvidence(e);
    System.out.print("\nEvidence:" );
    e.pPrint();
    double prEv = ssp.iterativePropagation(ssp.getJoinTree().elementAt(0), true);
    //double prEv = ssp.propagate(ssp.getJoinTree().elementAt(0), true);
    System.out.println("\nThe evidence probability is: " + prEv);
    ssp.saveResults(args[1]);
  }
                     
}//end main

 
/** 
 * Constructor: Creates new ShaferShenoyPropagation 
 *
 * @param b the <code>Bnet</code> over which propagation will be carried out
 * @param e the <code>Evidence</code> entered 
 * @param q a <code>boolean</code> indicating if the object to be created
 *        is query oriented (just one propagation) or not (possibly several
 *        propagations with different evidences).
 * @param vars a <code>NodeList</code> containing the interest variables. If
 *        empty, all the variables are consideres of interest.
 */

  public ShenoyShaferPropagation(Bnet b,Evidence e, boolean q, NodeList vars) {
     RelationList rel1, rel2, irTree;
     int i;
     PotentialTree pt;
     Relation newRel, rel;
  
     // initialising member variables
     network = b;
     queryOriented = q;
     positions = new Hashtable();  
     interest = vars;
     observations = new Evidence();
     initInterest(e);
     //observations = e;
  
     // First construct the tree by ensuring that there is a clique for each
     // initial relation
     irTree = getInitialRelations();
     binTree= new JoinTree();
     if (queryOriented){
       irTree.restrictToObservations(observations);
       irTree.removeConstantRelations();
       binTree = new JoinTree(b,irTree);
     }
     else binTree = new JoinTree(b);
     
     binTree.expandByAssigningRelations(irTree);
     transformRelationsInJoinTree();
     //binTree.initTrees(b,irTree);
     // adding single cliques for each variable in iterest
     RelationList singleRelations = getSingleRelationsForInterestVariables(irTree);
     singleCliques = binTree.Leaves(singleRelations);

     //Then make it binary
     binTree.binTree2();
     binTree.setLabels();
     initMessages( );
     
     currentRoot = binTree.elementAt(0);

     //initialising updated hashtable
     updated = new Hashtable(binTree.size());
     for(i=0;i<binTree.size();i++)
       updated.put(binTree.elementAt(i),new Integer(1));

     if (debug){
        System.out.println("EL ARBOL INICIAL ES");
        try{
          binTree.display2();
        }catch(IOException ioe){
          System.out.println("IOException " + ioe);
        }
        System.out.println("--------------- (fin) EL ARBOL ES ---------------");
     }
      
  }//end of constructor
 
  /** 
 * Constructor: Creates new ShaferShenoyPropagation that used for MAP
 *
 * @param b the <code>Bnet</code> over which propagation will be carried out
 * @param e the <code>Evidence</code> entered 
 * @param vars a <code>NodeList</code> containing the interest variables. If
 *        empty, all the variables are consideres of interest.
 */

  public ShenoyShaferPropagation(Bnet b,Evidence e, NodeList vars) {
     RelationList rel1, rel2, irTree;
     int i;
     PotentialTree pt;
     Relation newRel, rel;
  
     // initialising member variables
     network = b;
     queryOriented = true;
     positions = new Hashtable();  
     interest = vars;
     observations = new Evidence();
     initInterest(e);
     //observations = e;
  
     // First we construct a jointre 
     irTree = getInitialRelations();
     binTree= new JoinTree();
     if (queryOriented){
       irTree.restrictToObservations(observations);
       irTree.removeConstantRelations();
       binTree = new JoinTree(b,irTree);
     }
     else binTree = new JoinTree(b);
    
     //initialising potentials and messages 
     binTree.initTrees(b,irTree);
     transformRelationsInJoinTree();
     binTree.setLabels();
     initMessages();
     
     // pruning the tree with respect to the explanation set
     binTree.outerRestriction(vars,"no","no");
     
     // adding single cliques for each variable in iterest
     RelationList singleRelations = getSingleRelationsForInterestVariables(irTree);
     singleCliques = binTree.Leaves(singleRelations);

     //Then make it binary
     binTree.binTree2();
     binTree.setLabels();
     initMessages( );
     
     //binTree.display3();
     
     currentRoot = binTree.elementAt(0);

     //initialising updated hashtable
     updated = new Hashtable(binTree.size());
     for(i=0;i<binTree.size();i++)
       updated.put(binTree.elementAt(i),new Integer(1));

     if (debug){
        System.out.println("EL ARBOL INICIAL ES");
        try{
          binTree.display2();
        }catch(IOException ioe){
          System.out.println("IOException " + ioe);
        }
        System.out.println("--------------- (fin) EL ARBOL ES ---------------");
     }
      
  }//end of constructor
  
//
// ******************* fin de los constructores
//


/**
 * initInterest: initialises interest variable by taking into
 * account if the object is being constructed for a query or
 * not.
 */

public void initInterest(Evidence ev){
  if (!queryOriented) // all the variables are added to interest
    obtainInterest();
  else{
    observations = ev;
    obtainInterest();
  }
  
  positions = new Hashtable(interest.size());
  for(int i=0;i<interest.size();i++){
    positions.put((Node)interest.elementAt(i),new Integer(i));
  }
}

/**
 * buildSingleRelations: this methods build a list of indiviudal
 * relations for those variables included in Interest. 
 * 
 * @returns a <code>RelationList</code>
 */

public RelationList getSingleRelationsForInterestVariables(RelationList initials){
  RelationList singleRelations = new RelationList();
  Node n;
  Vector v;
  PotentialTree pt;
  Integer I;  
  Relation newRel,rel;


  for (int i=0;i<initials.size();i++) {
    rel = initials.elementAt(i);
    if (rel.getKind()==Relation.CONDITIONAL_PROB){
      n = rel.getVariables().elementAt(0);   
      I = (Integer) positions.get(n);
      if (I != null){
        v = new Vector();
        v.add(n);
        newRel = new Relation();
        newRel.setVariables(v);
        pt = new PotentialTree();
        pt.setTree(ProbabilityTree.unitTree());
        newRel.setValues(pt);
        newRel.setKind(rel.getKind());
        singleRelations.insertRelation(newRel);
      }
    }
  }

  return singleRelations;
}

    
 //Copied from Penniless
/**
 * Gets the initial relations in the network.
 * @return the initial relations present in the network, as a
 * <code>RelationList</code>.
 */

public RelationList getInitialRelations() {
  
  Relation rel, newRel;
  RelationList list;
  PotentialTree pt;
  //PotentialMTree pmt;
  int i;
 
  list = new RelationList();
  
  for (i=0 ; i<network.getRelationList().size() ; i++) {
    rel = (Relation)network.getRelationList().elementAt(i);
    newRel = new Relation();
    newRel.setVariables(rel.getVariables().copy());
    if (rel.getValues().getClassName().equals("PotentialTable")) {
      pt = ((PotentialTable)rel.getValues()).toTree();
    }
    else
      pt = (PotentialTree)rel.getValues();
    
    newRel.setValues(pt);
    newRel.setKind((int)(rel.getKind()));
    list.insertRelation(newRel);
  }
  
  return list;
}




/**
 * Sends messages from leaves to root (sender).
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */

private void navigateUp(NodeJoinTree sender) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  
  if (debug)
    System.out.println("Hago navigateUp desde " + sender.getLabel());
  
  list = sender.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    if (debug)
        System.out.println("LLamo a navigateUp(" + sender.getLabel() + " , " + other.getLabel() + ")");
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
    
    if (other.getLabel() != sender.getLabel()) {
      if (debug)
          System.out.println("LLamo a navigateUp(" + recipient.getLabel() + " , " + other.getLabel() + ")");
      navigateUp(recipient,other);
    }
  }
  
  if (debug)
         System.out.println("Mando mensaje de " + recipient.getLabel() + " a " + sender.getLabel());
  sendMessage(recipient,sender);
}
    



/**
 * Sends messages from root (sender) to leaves.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 */
public void navigateDown(NodeJoinTree sender) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  
  list = sender.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    if (debug)
     System.out.println("Mando mensaje de " + sender.getLabel() + " a " + other.getLabel());
    sendMessage(sender,other);
    navigateDown(sender,other);
  }
}


   
    
/**
 * Send messages from root (sender) to leaves through the brach recipient.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 */
 
public void navigateDown(NodeJoinTree sender, NodeJoinTree recipient){

  NeighbourTreeList list;
  NodeJoinTree other;
  int i;

  // Nodes to which the message will be sent downwards.
  list = recipient.getNeighbourList();

  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();

    if (other.getLabel() != sender.getLabel()) {      
     if (debug)
       System.out.println("Mando mensaje de " + recipient.getLabel() 
						+ " a " + other.getLabel());
      sendMessage(recipient,other);
      navigateDown(recipient,other);
    }
  }
}


/***
 * Sends a message from a node to another one.
 * The message is computed by combining all the messages inwards
 * the sender except that one comming from the recipient.
 * It is required that the nodes in the tree be labeled.
 * Use method <code>setLabels</code> if necessary.
 *
 * @param sender the node that sends the message.
 * @param recipient the node that receives the message.
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient) {
  
  NeighbourTreeList list, auxList;
  NeighbourTree nt;
  PotentialTree aux, pot, incoming;
  Relation rel, outwards, inwards;
  int i, label;
  Vector separator;
  //boolean isExact = true;
  //int step;
  
  //System.out.print("SendMessage: " + sender.getLabel() + " ---> " +
  //			recipient.getLabel()+ " [");

  //potencial nuevo
  incoming = new PotentialTree();
  outwards = new Relation();
  inwards = new Relation();
  separator = new Vector();
  
  aux = (PotentialTree)sender.getNodeRelation().getValues();
  
  pot = new PotentialTree();
  pot.setTree(ProbabilityTree.unitTree());
  
  list = sender.getNeighbourList();
  
  for (i=0 ; i<list.size() ; i++) {
    nt = list.elementAt(i);
    label = nt.getNeighbour().getLabel();
    rel = nt.getMessage();
        
    // Combine the messages coming from the other neighbours
    
    if (label != recipient.getLabel()) { 
      if (debug)
      { System.out.println("Combinamos ");   
        pot.print();
        System.out.println(" con ");   
        ((PotentialTree)rel.getOtherValues()).print();
      }
      //System.out.print(" " + FiniteStates.getSize(rel.getOtherValues().getVariables()));
      pot = (PotentialTree) pot.combine((PotentialTree)rel.getOtherValues());
      pot = transformPotential(pot);
       /*if (!((PotentialMTree)rel.getOtherValues()).getExact()) {
	isExact = false;
      }*/
    }
    else {
      //comments 05-08-04
      //incoming is the potential of rel
      incoming = (PotentialTree)rel.getOtherValues();
      //outwards is the relation
      outwards = rel;
      //separator is the list of variables in the relation
      separator =  rel.getVariables().getNodes();
      //auxList are the neifgbours of recipient
      auxList = recipient.getNeighbourList();
      //and inwards is the message received from sender
      inwards = auxList.getMessage(sender);
    }
  }
  
  if (debug)
  {   System.out.println("Y finalmente con: ");   
      aux.print();
  }
  // Now combine with the potential in the node.
  pot = (PotentialTree)pot.combine(aux);
  pot = transformPotential(pot);
  //System.out.print(" " + FiniteStates.getSize(pot.getVariables()));
  pot = (PotentialTree)(pot.marginalizePotential(separator)); 
  pot = transformPotential(pot);
  
  
  // Now update the messages in the join tree.
  outwards.setValues(pot);
  inwards.setOtherValues(pot);
  
  if (debug)
  { System.out.println("El msje es:[outwards.setValues(pot)]");
    ((PotentialTree)outwards.getValues()).print();
    System.out.println("Fin msje");
    System.out.println("mmmmmmmmm");
  }

  //System.out.println("] ... fin");
}


/**
 * enter evidence in the jointree by modifying probability trees
 * associated to the single cliques which contains observed nodes.
 *
 * Important: only has sense if queryOriented is false
 *
 * @param ev the <code>Evidence</code> to be entered
 */
public void introduceEvidence(Evidence ev)
{
  int i,j,v;
  FiniteStates vble;
  ProbabilityTree tree,twig;
  NodeJoinTree myClique;
   
  if (!queryOriented){
    for(i=0;i<ev.size();i++){
      vble = ev.getVariable(i);
      v = ev.getValue(i);

      // building a tree for variable
      tree = new ProbabilityTree(vble);
      for (j=0 ; j<tree.getChild().size() ; j++) {
        twig = (ProbabilityTree) tree.getChild().elementAt(j);
        twig.setLabel(2);
        if (j == v) twig.setValue(1.0);
        tree.oneMoreLeaf();
      }
        
      myClique = (NodeJoinTree)this.singleCliques.get(vble);
      ((PotentialTree)myClique.getNodeRelation().getValues()).setTree(tree);
    }
  }
}

/**
 * Given a variable and one of its values, we build a probability
 * tree with 1.0 in the twig corresponding to value and 0.0 in the other.
 * We use this method to build the potential trees of observed variables.
 */
private ProbabilityTree buildObservedTree(FiniteStates vble,int value){
  ProbabilityTree tree,twig;
  int j;

  tree = new ProbabilityTree(vble);
  for (j=0 ; j<tree.getChild().size() ; j++) {
    twig = (ProbabilityTree) tree.getChild().elementAt(j);
    twig.setLabel(2);
    if (j == value) twig.setValue(1.0);
    tree.oneMoreLeaf();
  }
  return tree;
}


public void addEvidenceItem(FiniteStates vble, int value){
  // building a probability tree for this evidence item
  ProbabilityTree tree = buildObservedTree(vble,value);
  // locating single clique for vble and assigning the new prob. tree
  NodeJoinTree clique = (NodeJoinTree)this.singleCliques.get(vble);
  ((PotentialTree)clique.getNodeRelation().getValues()).setTree(tree);
  // setting to 1 in updated
  //Integer I = (Integer) updated.get(clique);
  //I = new Integer(1); 
  updated.remove(clique);
  updated.put(clique,new Integer(1));
  // modifying observations 
  observations.insert(vble,value);
  // modifying currentRoot
  currentRoot = clique;
}

public void modifyEvidenceItem(FiniteStates vble, int value){
  // building a probability tree for this evidence item
  ProbabilityTree tree = buildObservedTree(vble,value);
  // locating single clique for vble and assigning the new prob. tree
  NodeJoinTree clique = (NodeJoinTree)this.singleCliques.get(vble);
  ((PotentialTree)clique.getNodeRelation().getValues()).setTree(tree);
  // setting to 1 in updated
  //Integer I = (Integer) updated.get(clique);
  //I = new Integer(1); 
  updated.remove(clique);
  updated.put(clique,new Integer(1));
  // modifying observations 
  observations.putValue(vble,value);
  // modifying currentRoot
  currentRoot = clique;
}

public void retractEvidenceItem(FiniteStates vble){
  // building a unitary probability tree for this vble
  ProbabilityTree tree = ProbabilityTree.unitTree();
  // locating single clique for vble and assigning the new prob. tree
  NodeJoinTree clique = (NodeJoinTree)this.singleCliques.get(vble);
  ((PotentialTree)clique.getNodeRelation().getValues()).setTree(tree);
  // setting to 1 in updated
  //Integer I = (Integer) updated.get(clique);
  //I = new Integer(1); 
  updated.remove(clique);
  updated.put(clique,new Integer(1));
  // modifying observations 
  observations.remove((Node)vble);
  // modifying currentRoot
  currentRoot = clique;

}

/**
 * enter evidence in the jointree by modifying probability trees
 * associated to the single cliques which contains observed nodes.
 * Also the evidence is updated with respect to observations member
 * variable, modifying the values of the hashtable updated if necessary.
 *
 * Important: only has sense if queryOriented is false
 *
 * @param ev the <code>Evidence</code> to be updated
 */
public void updateEvidence(Evidence ev)
{
  int i,j,value;
  FiniteStates vble;
  ProbabilityTree tree,twig;
  NodeJoinTree myClique;
   
  if (!queryOriented){
    // studying what variables of observations are not longer observed
    for(i=0;i<observations.size(); ){
      vble = observations.getVariable(i);
      if ( !ev.isObserved((Node)vble) )
        retractEvidenceItem(vble);
      else i++;
    }
    // studying what variables of evidence are already in observations
    for(i=0;i<ev.size();i++){
      vble = ev.getVariable(i);
      value = ev.getValue(i);
      if ( observations.isObserved((Node)vble) ){
        if (observations.getValue(vble) != value)
          modifyEvidenceItem(vble,value);
      } 
      else addEvidenceItem(vble,value);
    }
  }
}



/**
 * Computes the marginals after a propagation and put them into the
 * instance variable <code>results</code>. Sets <code>positions</code>
 * for each variable, to indicate the index lo locate the variable
 * in <code>Vector</code> results.
 */

public void computeMarginals() {
  
  int i, j, k, nv, pos;
  //Vector leaves, marginal;
  Vector marginal;
  NodeList variables;
  Relation r1, r2;
  PotentialTree pot;
  PotentialTable table;
  NodeJoinTree temp;
  NodeList l;
  FiniteStates v;
  NeighbourTree nt;
  int posResult;
  
  nv = interest.size();
  results = new Vector(); 
  posResult = 0;

  for (i=0 ; i<nv ; i++) {
    v = (FiniteStates)interest.elementAt(i);
    j = 0;
    temp = (NodeJoinTree)singleCliques.get(v);
    if (temp != null) {
      r1 = temp.getNodeRelation();
      pot = (PotentialTree)r1.getValues();
 
      for (k=0 ; k<temp.getNeighbourList().size() ; k++) {
	nt = temp.getNeighbourList().elementAt(k);
	r2 = nt.getMessage();
	pot = (PotentialTree)pot.combine((PotentialTree)r2.getOtherValues());
        pot = transformPotential(pot);
      }
      marginal = new Vector();
      marginal.addElement(v);
      pot = (PotentialTree)(pot.marginalizePotential(marginal));
      pot = transformPotential(pot);
      pot.normalize();
      results.addElement(pot);
      
      positions.put(v,new Integer(posResult));
      posResult++;
    }
  }
}



/*** access methods ****/

/**
 * Gets the join tree.
 * @return the join tree.
 */

public JoinTree getJoinTree() {
  return (binTree);
}

public Hashtable getMarginalCliques() {
  return (marginalCliques);
}
    
public Hashtable getSingleCliques() {
  return (singleCliques);
}

public Hashtable getPositions() {
  return (positions);
}
    
    
//09/05/04 --> copied from Penniless
/**
 * Initializes all the messages to 1, except those corresponding
 * to leaf nodes, which will contain the potential in the
 * node. <P>
 * Messages between cliques are marked as not exact.
 */
    
    public void initMessages() {
        
    RelationList ir;
    Relation r, r2, message, otherMessage;
    int i, j;
    PotentialTree pot, pot2;
    Vector leaves;
    NodeJoinTree node, otherNode;
    NeighbourTree neighbour, nt;
    NeighbourTreeList ntl;
    NodeList nl1, nl2;
    
    for (i=0 ; i<binTree.getJoinTreeNodes().size() ; i++) {
    // Set the potentials in the cliques to 1.
    node = binTree.elementAt(i);    
    r = node.getNodeRelation();
    //05-08-04
    if (r.getValues() == null) {
    //if ((r.getValues() == null) || (!r.getValues().getClassName().equals("PotentialTree"))) {
      pot = new PotentialTree(r.getVariables());
      pot.setTree(ProbabilityTree.unitTree());
      r.setValues(pot);
    }
    else if (!r.getValues().getClassName().equals("PotentialTree"))
    {
        pot = new PotentialTree(r.getValues());
        r.setValues(pot);
    }
    // Now set the messages
    ntl = node.getNeighbourList();
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      r = nt.getMessage();
      pot = new PotentialTree(r.getVariables());
      pot.setTree(ProbabilityTree.unitTree());
      //pot.setExact(false);
      //Julia 09/05/04
      //pot.setExact(true); ==> 11/05/04 no nos deja hacer un setExact
      //sobe un PotentialTree, si nos dejaba con respecto a un PotentialMTree
      r.setValues(pot);
      pot = new PotentialTree(r.getVariables());
      pot.setTree(ProbabilityTree.unitTree());
      //pot.setExact(false);
      //pot.setExact(false);
      //Julia 09/05/04
      r.setOtherValues(pot);
    }
  }
    }
    
/**
 * Procedure to obtain the probability of the observed evidence.
 * A potential is computed by combining the potential resident in 
 * tempRoot with all its incoming messages. Then, all the entries
 * of this potential are totalized (by summation).
 *
 * Important: this methods has to be invoked after carrying an
 * upward propagation started from tempRoot node
 *
 * @param tempRoot the node from which the evidence is computed. 
 *
 */

 public double obtainEvidenceProbabilityFrom(NodeJoinTree tempRoot) {
  NodeJoinTree node;
  PotentialTree pt;
  int i;  
  NeighbourTreeList ntl;
  Relation r;
  Potential pot;

  //initMessages();
  ntl = tempRoot.getNeighbourList();

  pt = new PotentialTree(tempRoot.getNodeRelation().getVariables());
  pt.setTree(ProbabilityTree.unitTree());

  pt = (PotentialTree)pt.combine(
		(PotentialTree)tempRoot.getNodeRelation().getValues());
  pt = transformPotential(pt);
  for(i=0; i<ntl.size(); i++){
    node = ntl.elementAt(i).getNeighbour();
    r = ntl.elementAt(i).getMessage();
    pot = r.getOtherValues();
    pt = (PotentialTree)pt.combine((PotentialTree)pot);
    pt = transformPotential(pt);
  }

  return pt.totalPotential();
}
    

    
/**
 * Carries out a propagation following the ShenoyShafer approach
 *
 * @param nodeFrom The node/clique from which the inference is initiated
 * @param probEvidence If true the probability of the entered evidence is
 *                     returned
 * @returns the probability of the entered evidence if probEvidence=true
 *          or -1.0 otherwise  
 */

public double propagate(NodeJoinTree nodeFrom,boolean probEvidence) {

  double prEv=-1.0;
  Date date;
  double time;

  // Necessary for identifying the nodes during the message passing.
  binTree.setLabels();
  
  System.out.println("\nComputing posterior distributions ...");
  date = new Date();
  time = (double)date.getTime();

  // Initialize messages
  initMessages();

  if (debug){
    System.out.println("\nJoin Tree after initialising messages ");
    binTree.display3();
  }

  // Perform the propagation
  if (debug)
   System.out.println("Starting upwards phase in SS propagation");

  //Upwards phase
  navigateUp(nodeFrom);

  System.out.println("\n**** upward phase finalizada ******");
  System.out.println();

  if (debug){
    System.out.println("\nJoin Tree after upward phase ");
    binTree.display3();
  }

  if (probEvidence){
      prEv = this.obtainEvidenceProbabilityFrom(nodeFrom); 
  }

  if (debug)
   System.out.println("Starting downwards phase in SS propagation");

  //Downwards phase
  navigateDown(nodeFrom);

  System.out.println("\n**** downward phase finalizada ******");
  System.out.println();

  if (debug){
    System.out.println("\nJoin Tree after downward phase ");
    binTree.display3();
  }

    
  //computing posterior distributions
  computeMarginals();

  // End of propagation
  if (debug)
   System.out.println("ShenoyShaferPropagation done");
  
  date = new Date();
  time = ((double)date.getTime() - time) / 1000;
  System.out.println("... Posterior distributions computed.");
  System.out.println("Time (secs): " + time);


  //Returns probability for evidence (or -1.0)
  return prEv;
}



/**
 * iterativeUpward: implements the upward step of propagation
 * as an iterative procedure.
 * It is supossed that joinTreeNodes in the jointree are
 * sorted in ancestral ordering, being node in position 0 the root
 */

public void iterativeUpward( ){
  int i, j, n;
  NodeJoinTree node,child,father;
  NeighbourTreeList ntl;
  NeighbourTree nt;
  PotentialTree result, pot, incoming;
  Relation rel, outwards = new Relation(), inwards = new Relation();
  Vector separator = new Vector();
  Integer I;

  n = binTree.size();
  // main loop - traverse the tree in ascendent order
  for(i=n-1;i>0;i--){
    node = binTree.elementAt(i);
    I = (Integer) updated.get(node);
    if (I == null) {
      System.out.println("\n *** error in updated ****\n");
      System.exit(0);
    }         
    if (I.intValue()==1){ //node contains new evidence and so we propagate it upwards
      updated.remove(node);
      updated.put(node,new Integer(0));
      //I = new Integer(0);
      ntl = node.getNeighbourList();
      result = new PotentialTree();
      result.setTree(ProbabilityTree.unitTree());

      // locating father of node and combining with children
      for(j=0;j<ntl.size();j++){
        nt = ntl.elementAt(j);
        child = nt.getNeighbour();
        rel = nt.getMessage();
        if (child.getLabel() < node.getLabel()){ // taking necessary values
          father = child;
          outwards = rel;
          separator = rel.getVariables().getNodes();
          inwards = father.getNeighbourList().getMessage(node);

          I = (Integer) updated.get(father);
          if (I == null) {
            System.out.println("\n *** error in updated ****\n");
            System.exit(0);
          }              
          //I = new Integer(1);
          updated.remove(father);
          updated.put(father,new Integer(1));

        }
        else{ // combining
          pot = (PotentialTree)rel.getOtherValues();
          result = (PotentialTree)result.combine(pot);
          result = transformPotential(result);
        }
      }
      // combining with node and marginalising to separator
      result = (PotentialTree) result.combine(node.getNodeRelation().getValues());
      result = transformPotential(result);
      result = (PotentialTree) result.marginalizePotential(separator);
      result = transformPotential(result);
      // updating messages in the join tree
      outwards.setValues(result);
      inwards.setOtherValues(result);
    } // end if updated
  } // end for
}

/**
 * iterativeDownward: implements the downward step of propagation
 * as an iterative procedure.
 * It is supossed that joinTreeNodes in the jointree are
 * sorted in ancestral ordering, being node in position 0 the root
 */

public void iterativeDownward( ){
  int i, j, k, n, posFather;
  NodeJoinTree node,child,father,brother;
  NeighbourTreeList ntl;
  NeighbourTree nt,nt2;
  PotentialTree temp, pot, incoming;
  Relation rel = new Relation(), outwards = new Relation(), inwards = new Relation();
  Relation relBrother = new Relation();
  Vector separator = new Vector();


  n = binTree.size();
  // main loop - traverses the tree in descendent order
  for(i=0;i<n;i++){
    node = binTree.elementAt(i);
    ntl = node.getNeighbourList();
    //identifying father
    father = null;
    posFather = -1;
    for(j=0;j<ntl.size();j++){
      nt = ntl.elementAt(j);
      child = nt.getNeighbour();
      if (child.getLabel() < node.getLabel()){
        father = child;
        posFather = j;
        rel = nt.getMessage();
        break;
      }
    }
    // creating a potential and combining father with node
    temp = new PotentialTree();
    temp.setTree(ProbabilityTree.unitTree());
    if (father != null) temp = (PotentialTree) temp.combine(rel.getOtherValues());
    temp = (PotentialTree) temp.combine(node.getNodeRelation().getValues());
    temp = transformPotential(temp);    

    //computing (and sending) messages for children
    for(j=0;j<ntl.size();j++){
      if (j != posFather){
        nt = ntl.elementAt(j);
        child = nt.getNeighbour();
        rel = nt.getMessage();
        // sending message to child
        outwards = rel;
        separator = rel.getVariables().getNodes();
        inwards = child.getNeighbourList().getMessage(node);
        
        pot = temp;
        for(k=0;k<ntl.size();k++){
          if ((k!=j) && (k!=posFather)){
            nt2 = ntl.elementAt(k);
            brother = nt2.getNeighbour();
            relBrother = nt2.getMessage();
            pot = (PotentialTree)pot.combine((PotentialTree)relBrother.getOtherValues());
            pot = transformPotential(pot);
          }
        }
        pot = (PotentialTree) pot.marginalizePotential(separator);
        pot = transformPotential(pot);
        outwards.setValues(pot);
        inwards.setOtherValues(pot);         
      }
    }
  }

}



/**
 * Carries out a propagation following the ShenoyShafer approach
 *
 * @param nodeFrom The node/clique from which the inference is initiated
 * @param probEvidence If true the probability of the entered evidence is
 *                     returned
 * @returns the probability of the entered evidence if probEvidence=true
 *          or -1.0 otherwise  
 */

public double iterativePropagation(boolean probEvidence){
  return iterativePropagation(currentRoot,probEvidence);
}

public double iterativePropagation(NodeJoinTree root,boolean probEvidence) {

  double prEv=-1.0;
  Date date;
  double time;

  // Necessary to carry out an iterative scheme for propagation
  binTree.ancestralLabelling(root);
  
  System.out.println("\nComputing posterior distributions ...");
  date = new Date();
  time = (double)date.getTime();

  // Initialize messages
  // initMessages();

  if (debug){
    System.out.println("\nJoin Tree after initialising messages ");
    binTree.display3();
  }

  // Perform the propagation
  if (debug)
   System.out.println("Starting upwards phase in SS propagation");

  //Upwards phase
  iterativeUpward( );

  System.out.println("\n**** upward phase finalizada ******");
  System.out.println();

  if (debug){
    System.out.println("\nJoin Tree after upward phase ");
    binTree.display3();
  }

  if (probEvidence){
      prEv = this.obtainEvidenceProbabilityFrom(root);
  }

  if (debug)
   System.out.println("Starting downwards phase in SS propagation");

  //Downwards phase
  iterativeDownward();

  System.out.println("\n**** downward phase finalizada ******");
  System.out.println();

  if (debug){
    System.out.println("\nJoin Tree after downward phase ");
    binTree.display3();
  }

    
  //computing posterior distributions
  computeMarginals();

  // End of propagation
  if (debug)
   System.out.println("ShenoyShaferPropagation done");
  
  date = new Date();
  time = ((double)date.getTime() - time) / 1000;
  System.out.println("... Posterior distributions computed.");
  System.out.println("Time (secs): " + time);


  //Returns probability for evidence (or -1.0)
  return prEv;
}


/**
 * Transforms the initial relations in the join tree if they
 * are of class <code>PotentialTree</code>. The only thing to do is
 * the pruning of nodes whose children are equal, so we use a very small
 * value as limit for prunning.
 * This method can be overloaded for special requirements.
 */

public void transformRelationsInJoinTree() {

  int i, s;
  Relation r;

  s = binTree.size();
  for (i=0 ; i<s ; i++) {
    r = ((NodeJoinTree)binTree.elementAt(i)).getNodeRelation();
    r = transformRelation(r);
  }
}                    


/**
 * Transforms a relation if its values are of
 * class <code>PotentialTree</code>.
 * The only thing to do is the pruning of 
 * nodes which children are equals,  so we use a very small
 * value as limit for prunning.
 * This method can be overloaded for special requirements.
 * @param r the <code>Relation</code> to be transformed
 */

public Relation transformRelation(Relation r) {

  PotentialTree pot;

  //pot = (PotentialTree)r.getValues();
  //pot.limitBound(10e-30);
  //r.setValues(pot);

  return r;
}                    


/**
 * Transforms a <code>PotentialTree</code>.
 * The only thing to do is the pruning of 
 * nodes which children are equals,  so we use a very small
 * This method can be overloaded for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @param pot the  <code>PotentialTree</code> to be transformed
 */

public PotentialTree transformPotential(PotentialTree pot) {
  //((PotentialTree)pot).limitBound(10e-30);
  return pot;
}

    
    
} // end of class
