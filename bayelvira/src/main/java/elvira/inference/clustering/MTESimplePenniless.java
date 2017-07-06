/* MTESimplePenniless.java */

package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.potential.Potential;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.ContinuousProbabilityTree;
import elvira.learning.MTELearning;
import elvira.learning.*;
import elvira.database.DataBaseCases;

/**
 * Class <code>MTESimplePenniless</code>.
 * Implements a propagation method over a
 * join tree. Potentials are represented by means of
 * continuous probability trees, since they are treated as MTE.
 * 
 *
 * @since 28/03/2006
 */


public class MTESimplePenniless extends SimplePenniless {

    JoinTree binTree;
  

/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * <li> Output file where the results of the propagation
 * will be written.
 * <li> Delta: Paramater for pruning.
 * <li> Epsilon: Parameter for pruning.
 * <li> Evidence file: Observations of some variables in the network.
 * </ol>
 * The last argument can be omitted. In that case, it will
 * be considered that no observations are present. If both parameters for pruning are zero, the 
 * propagation is exact.
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  MTESimplePenniless propagation;
  int i, m, ls, nTerms, nSplits;
  FileWriter f;
  PrintWriter p;
  double delta, epsilon, epsilonDisc,timePropagating, epsilonJoin;
  Vector errorVector,relVecCont,statErrorVector;
  Vector sizeVec = new Vector();
  Vector statSizeVec = new Vector();
  Vector sizeExactVec = new Vector();
  Relation contRel;
  PotentialContinuousPT contPot;
  Date date;
  
  if (args.length < 9) {
    System.out.print("Too few arguments. Arguments are: ElviraFile");
    System.out.println(" OutputFile.elv OutputErrorFile InputExactResultsFile.elv delta epsilon epsilonJoin epsilonDisc prune2 [EvidenceFile] ");
    System.out.println("InputExactResultsFile: The BNET where the exact results are stored.");
    System.out.println("Just put NORESULTS if you want to obtain the exact results");
    System.out.println("As prune2 put 1 if pruning to 2 terms is wanted, and 0 if not.");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);
    if (args.length == 10) {
      evidenceFile = new FileInputStream(args[9]);
      e = new Evidence(evidenceFile,b.getNodeList());
      //System.out.println("Evidence file"+args[6]);
    }
    else {
      e = new Evidence();
    }			  	
    
    // Before propagating I must get the number of terms and splits
    
    
    relVecCont = new Vector();
    
    for(i=0 ; i < b.getRelationList().size() ; i++){
      
      contRel = (Relation)b.getRelationList().elementAt(i);
      contPot = (PotentialContinuousPT)contRel.getValues();
      
      nTerms = contPot.obtainNumTerms();
      nSplits = contPot.obtainNumSplits();

      //System.out.println("Para este potencial: ");
      //contPot.print();
      contPot.setNumTerms(nTerms);
      contPot.setNumSplits(nSplits);
      //System.out.println("el n�mero de t�rminos es  "+contPot.getNumTerms()+" y el de splits es "+contPot.getNumSplits());
    
      contRel = new Relation();
      
      contRel.setVariables(contPot.getVariables());
      contRel.setValues(contPot);
      contRel.setKind(0);
      System.out.println("El tama�o real del potencial "+i+" es: "+contPot.actualSize());
      
      relVecCont.addElement(contRel);
    
    }
    
    b.setRelationList(relVecCont);

   
    delta = (Double.valueOf(args[4])).doubleValue();
    epsilon = (Double.valueOf(args[5])).doubleValue();
    epsilonJoin = (Double.valueOf(args[6])).doubleValue();
    epsilonDisc = (Double.valueOf(args[7])).doubleValue();
    int prune2Int = (Integer.valueOf(args[8])).intValue();
    System.out.println("Empiezo a compilar la red");
    propagation = new MTESimplePenniless(b,e,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);
    System.out.println("Ya he terminado de compilar la red");
    f = new FileWriter(args[1]);
    p = new PrintWriter(f);

    //p.println("Triangulation method: "+triangMethod);
    //propagation.binTree.display();
    System.out.println("Empezamos el propagate");
    date = new Date();
    timePropagating = (double)date.getTime();
    propagation.propagate(args[1],delta,epsilon,epsilonJoin,epsilonDisc,e,sizeVec,prune2Int);
    //System.out.println("Este es el nodelist de la red resultante: ");
    //b.getNodeList().printNames();
    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;
    
    System.out.println("finalizamos el propagate");
    f.close();
    
    //System.out.println("Results tiene tama�o: "+propagation.results.size());
    
    // Now I get the error propagating, and so:
    
    f = new FileWriter(args[2]);
    p = new PrintWriter(f);
    
    p.println("Time propagating (secs) : "+timePropagating);
    
    p.println("Limits for pruning: ");
    p.println("Delta: "+delta);
    p.println("Epsilon: "+epsilon);
    p.println("Epsilon for Join: "+epsilonJoin);
    p.println("Epsilon for Discrete: "+epsilonDisc);
    
    if(prune2Int == 0)
      p.println("Pruning up to two terms NO activated");
    else
      p.println("Pruning up to two terms activated");

    for(i=0 ; i < sizeVec.size() ; i++)
	p.println("Size "+i+" : "+((Double)sizeVec.elementAt(i)).doubleValue());

    statSizeVec = propagation.computeStatistics(sizeVec);

    p.println("These are the statistics about the sizes:");
    System.out.println("These are the statistics about the sizes:");
      
    System.out.println("Mean: "+((Double)statSizeVec.elementAt(0)).doubleValue());
    p.println("Mean :" +((Double)statSizeVec.elementAt(0)).doubleValue());

    System.out.println("Standard Deviation: "+((Double)statSizeVec.elementAt(1)).doubleValue());
    p.println("Standard Deviation : "+((Double)statSizeVec.elementAt(1)).doubleValue());

    System.out.println("Max: "+((Double)statSizeVec.elementAt(2)).doubleValue());
    p.println("Max : "+((Double)statSizeVec.elementAt(2)).doubleValue());

    System.out.println("Min: "+((Double)statSizeVec.elementAt(3)).doubleValue());
    p.println("Min : "+((Double)statSizeVec.elementAt(3)).doubleValue());

    if(!args[3].equals("NORESULTS")){
	// networkFile = new FileInputStream(args[0]);	
// 	b = new Bnet(networkFile);
// 	relVecCont = new Vector();
    
//     for(i=0 ; i < b.getRelationList().size() ; i++){
      
//       contRel = (Relation)b.getRelationList().elementAt(i);
//       contPot = (PotentialContinuousPT)contRel.getValues();
      
//       nTerms = contPot.obtainNumTerms();
//       nSplits = contPot.obtainNumSplits();

//       //System.out.println("Para este potencial: ");
//       //contPot.print();
//       contPot.setNumTerms(nTerms);
//       contPot.setNumSplits(nSplits);
//       //System.out.println("el n�mero de t�rminos es  "+contPot.getNumTerms()+" y el de splits es "+contPot.getNumSplits());
    
//       contRel = new Relation();
      
//       contRel.setVariables(contPot.getVariables());
//       contRel.setValues(contPot);
//       contRel.setKind(0);
//       System.out.println("El tama�o real del potencial "+i+" es: "+contPot.actualSize());
      
//       relVecCont.addElement(contRel);
    
//     }
    
//     b.setRelationList(relVecCont);

      System.out.println("Reading exact results");
      networkFile = new FileInputStream(args[3]);
      Bnet b2 = new Bnet(networkFile);
      //propagation.readExactResults(args[3]);
      //MTESimplePenniless propagation2 = new MTESimplePenniless(b,e,0,0,0);
      //propagation2.propagate("exactResults.txt",0,0,0,e,sizeExactVec);
      //propagation.exactResults = propagation.exactResults;
      //System.out.println("This is the size of the exactResults:");
      //(((Vector)propagation2.results)).size();
      System.out.println("Exact results read");
      Vector relVec = b2.getRelationList();
      System.out.println("Computing errors");
      errorVector = propagation.computeErrors(relVec);
      
      // Now I get the mean, standard deviation, max and min.
      
      statErrorVector = propagation.computeStatistics(errorVector);
      
      System.out.println("These are the errors:");
      
      for(i=0 ; i<errorVector.size() ; i++){
	System.out.println(((Double)errorVector.elementAt(i)).doubleValue());
	p.println("Error: "+i+" : "+((Double)errorVector.elementAt(i)).doubleValue());
      }
      p.println("These are the statistics about the errors:");
      System.out.println("These are the statistics about the errors:");
      
      System.out.println("Mean: "+((Double)statErrorVector.elementAt(0)).doubleValue());
      p.println("Mean :" +((Double)statErrorVector.elementAt(0)).doubleValue());

      System.out.println("Standard Deviation: "+((Double)statErrorVector.elementAt(1)).doubleValue());
      p.println("Standard Deviation : "+((Double)statErrorVector.elementAt(1)).doubleValue());

      System.out.println("Max: "+((Double)statErrorVector.elementAt(2)).doubleValue());
      p.println("Max : "+((Double)statErrorVector.elementAt(2)).doubleValue());

      System.out.println("Min: "+((Double)statErrorVector.elementAt(3)).doubleValue());
      p.println("Min : "+((Double)statErrorVector.elementAt(3)).doubleValue());
      
    }//End of if(!args[3].equals("NORESULTS"))
    
    f.close();
    System.out.println("Done"); 
  }
} 




    /********** CONSTRUCTORS ***************
     *
     *
     */


/**
 * Creates a new propagation for a given evidence and a given network.
 *
 * @param b a network
 * @param e an evidence
 */

public MTESimplePenniless(Bnet b, Evidence e, double delta, double epsilon, double epsilonJoin, double epsilonDisc, int prune2Int){

    RelationList irTree;

    observations = e;
    network = b;
    if(observations.size() == 0)
        binTree = new JoinTree(b,1);
    else
        binTree = new JoinTree(b, e,1);
    //System.out.println("Este es el JoinTree tras crearlo, sin hacer nada mas");
    //binTree.display();
        
    //irTree = getInitialRelations();
    //irTree.restrictToObservations(observations);
    
    //System.out.println("Este es el tama�o del irTree: "+irTree.size());
    //marginalCliques = binTree.Leaves(irTree);
    //System.out.println("Este es el size del marginalCliques: "+marginalCliques.size());
    
    
    
    //System.out.println("Este es el JoinTree tras hacer el binTree");
    //binTree.display();
        
    binTree.binTree();

    //El binTree que obtenemos tiene tantos nodos como variables, pero no afecta a la propagacion

    //Con esto asignamos una relacion a cada nodeJoinTree
    //System.out.println("Emepzamos el initRelations del binTree");
    initRelations(binTree,b,e,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);
    //System.out.println("Ya hemos hecho el init relations");

    //irTree = continuousGetInitialRelations();
    //irTree.restrictToObservations(observations);
    //marginalCliques = binTree.Leaves(irTree);
    binTree.setLabels();

    //System.out.println("Este es el JoinTree que vamos a utilizar:");
    //binTree.display();
    
}//End of constructor


/**
 * Creates a new propagation for a given network.
 *
 * @param b a network
 * 
 */

MTESimplePenniless(Bnet b){

    network = b;
    binTree = new JoinTree(b);
    
}//End of constructor



    /******** METHODS ***************/



/**
 * Carries out an exact propagation.
 */

public void propagate(Evidence e) {
 
    NodeJoinTree root = new NodeJoinTree();
    NodeList variables;
    int i;
    
   // System.out.println("Initializing messages");
     
    initMessages(0,0,0,0);  
   // System.out.println("Ficnished initializing messages");
    //I select the root node.
    boolean found = false;
    i = 0;
    while((i<binTree.size()) && (!found)){
	if(!binTree.elementAt(i).isLeaf()){
	    found = true;
	    root = binTree.elementAt(i);
	    System.out.println("El "+i+" es el root");
	}
	i++;
    }
    
   // System.out.println("Empezamos navigate");
    navigate(root,0,0,0,0,0);
    //System.out.println("Acabamos navigate");
    
    //System.out.println("Empezamos el computeMarginals");
    computeMarginals(0,0,0,0,e,new Vector(),0);
    //System.out.println("acabamos el computeMarginals");
    //System.out.println("Este es el nodelist de la red resultante:");
    //network.getNodeList().print();
    //saveResults(resultFile);
    
    //System.out.println("Hecho");

}//End of method propagate



/**
 * Carries out a propagation. Shenoy Shafer algorithm for
 * MTE model.
 * 
 * @param resultFile The name of the file where the errors will be stored
 * @param delta First pruning parameter
 * @param epsilon Second pruning parameter
 * @param epsilonJoin Third pruning parameter
 * @param epsilonDisc Fourth pruning parameter
 * @param e The evidence
 * @param sizeVec vector where the sizes are stored
 */

public void propagate(String resultFile,double delta, double epsilon, double epsilonJoin, double epsilonDisc, Evidence e, Vector sizeVec, int prune2Int) throws ParseException, IOException {

    NodeJoinTree root = new NodeJoinTree();
    NodeList variables;
    int i;
    
   // System.out.println("Initializing messages");
     
    initMessages(delta,epsilon,epsilonJoin,epsilonDisc);  
    
   // System.out.println("Ficnished initializing messages");
    //I select the root node.
    boolean found = false;
    i = 0;
    while((i<binTree.size()) && (!found)){
	if(!binTree.elementAt(i).isLeaf()){
	    found = true;
	    root = binTree.elementAt(i);
	    System.out.println("El "+i+" es el root");
	}
	i++;
    }
    
    //System.out.println("Empezamos navigate");
    navigate(root,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);
   // System.out.println("Acabamos navigate");
    
    //System.out.println("Empezamos el computeMarginals");
    computeMarginals(delta,epsilon,epsilonJoin,epsilonDisc,e,sizeVec,prune2Int);
   // System.out.println("acabamos el computeMarginals");
    //System.out.println("Este es el nodelist de la red resultante:");
    //network.getNodeList().print();
    saveResults(resultFile);
    
    //System.out.println("Hecho");

}//End of method propagate


/**
 * Initializes all potentials in messages and cliques to 1, except those 
 * potentials in cliques corresponding to leaf nodes, which will contain the 
 * potential in the node.
 *
 */
  
public void initMessages(double delta, double epsilon, double epsilonJoin, double epsilonDisc) {
  
  Relation r;
  int i, j;
  PotentialContinuousPT pot;
  NodeJoinTree node;
  NeighbourTree nt;
  NeighbourTreeList ntl;  

  //System.out.println("Hay "+binTree.getJoinTreeNodes().size()+" nodos en el join tree");
  for (i=0 ; i<binTree.getJoinTreeNodes().size() ; i++) {
    // Set the potentials in the cliques to 1.
    node = binTree.elementAt(i);    
    r = node.getNodeRelation();
    if ((r.getValues() == null))  {
      pot = (PotentialContinuousPT)makeUnitPotential(r.getVariables());
      r.setValues(pot);
    }
   // System.out.println("Tama�o real del potencial asociado al clique "+i+" del JoinTree: "+((PotentialContinuousPT)r.getValues()).actualSize());
    
    // Now set the messages
    ntl = node.getNeighbourList();
    for (j=0 ; j<ntl.size() ; j++) {
      nt = ntl.elementAt(j);
      r = nt.getMessage();
      pot = (PotentialContinuousPT)makeUnitPotential(r.getVariables());
      r.setValues(pot);
      pot = (PotentialContinuousPT)makeUnitPotential(r.getVariables());
      r.setOtherValues(pot);     
    }
    
  }
}


/**
 * Sends messages from leaves to the root, and from root to the leaves
 *
 * @param sender The <code>NodeJoinTree<\code> that sends the request
 * @param delta First pruning parameter
 * @param epsilon Second pruning parameter
 * @param epsilonJoin Third pruning parameter
 * @param epsilonDisc Fourth pruning parameter
 */

public void navigate(NodeJoinTree sender, double delta, double epsilon, double epsilonJoin, double epsilonDisc, int prune2Int){
  

    navigateUp(sender,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);
    navigateDown(sender,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);

}//End of method navigate


/**
 * Sends messages from leaves to the root (sender)
 *
 * @param sender the <code>NodeJoinTree<\code> that sends the request
 * @param delta First pruning parameter
 * @param epsilon Second pruning parameter
 * @param epsilonJoin Third pruning parameter
 * @param epsilonDisc Fourth pruning parameter 
 *
 */

public void navigateUp(NodeJoinTree sender, double delta, double epsilon, double epsilonJoin, double epsilonDisc, int prune2Int){

    NeighbourTreeList list;
    NodeJoinTree other;
    int i;

      //System.out.println("Empezamos navigateUp sender");
    
    list = sender.getNeighbourList();
    for (i=0 ; i<list.size() ; i++) {
	other = list.elementAt(i).getNeighbour();
	navigateUp(sender,other,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);
    }
    //System.out.println("Acabamos navigateUp sender");
    
}//End of method navigateUp


/**
 * Sends messages from leaves to root (sender) through the branch towards
 * the recipient.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 * @param delta  First pruning parameter
 * @param epsilon Second pruning parameter
 * @param epsilon Third pruning parameter
 * @param epsilonDisc Fourth pruning parameter
 *
 */

private void navigateUp(NodeJoinTree sender, NodeJoinTree recipient, double delta, double epsilon, double epsilonJoin, double epsilonDisc, int prune2Int) {

  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  System.out.println("Empezamos navigateUp sender, recipient");
  // Nodes to which the message will be sent downwards.
  list = recipient.getNeighbourList();

  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();

    if (other.getLabel() != sender.getLabel()) {
      navigateUp(recipient,other,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);
    }
  }
  // Thi sendMessage is where the message should be calculated and save it in the corresponding mailbox.
  sendMessage(recipient,sender,delta,epsilon,epsilonJoin,epsilonDisc, prune2Int);
  System.out.println("Terminamos navigateUp sender, recipient");
  
}//End of navigateUp


/**
 * Sends messages from root (sender) to leaves.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param delta First pruning parameter.
 * @param epsilon Second pruning parameter
 * @param epsilonJoin Third pruning parameter
 * @param epsilonDisc Fourth pruning parameter
 *
 */

protected void navigateDown(NodeJoinTree sender, double delta, double epsilon, double epsilonJoin, double epsilonDisc, int prune2Int) {
  
  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  //System.out.println("Empezamos navigateDown sender");
  list = sender.getNeighbourList();
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();
    sendMessage(sender,other,delta,epsilon, epsilonJoin,epsilonDisc,prune2Int);
    navigateDown(sender,other,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);
  }
 // System.out.println("Acabamos navigateDown sender");
}//End of navigateDown


/**
 * Sends messages from root (sender) to leaves through the brach
 * towards node recipient.
 * @param sender the <code>NodeJoinTree</code> that sends the request.
 * @param recipient the <code>NodeJoinTree</code> that receives the request.
 * @param delta First pruning parameter.
 * @param epsilon Second pruning parameter.
 * @param epsilonJoin Third pruning parameter.
 * @param epsilonDisc Fourth pruning parameter
 *
 */

public void navigateDown(NodeJoinTree sender, NodeJoinTree recipient, double delta, double epsilon, double epsilonJoin, double epsilonDisc, int prune2Int) {

  NeighbourTreeList list;
  NodeJoinTree other;
  int i;
  
  System.out.println("Empezamos navigateDown sender, rcpt");
  // Nodes to which the message will be sent downwards.
  list = recipient.getNeighbourList();

 
  
  for (i=0 ; i<list.size() ; i++) {
    other = list.elementAt(i).getNeighbour();

    if (other.getLabel() != sender.getLabel()) {
      sendMessage(recipient,other,delta,epsilon, epsilonJoin,epsilonDisc, prune2Int);
      navigateDown(recipient,other,delta,epsilon,epsilonJoin,epsilonDisc,prune2Int);
    }
  }
//  System.out.println("Acabamos navigateDown sender, recipient");
}//End of navigateDown


/**
 * Sends a message from a node to another one.
 * The message is computed by combining all the messages inwards
 * the sender except that one comming from the recipient. Then, the
 * result is combined with the potential stored in the sender
 * 
 * It is required that the nodes in the tree be labeled.
 * Use method <code>setLabels</code> if necessary.
 *
 * We do not update the clicque's potentials, but we create the messages from a 
 * clique to another, as in the Shenoy-Shafer algorithm
 *
 * @param sender the node that send the message.
 * @param recipient the node that receives the message.
 * @param delta First pruning parameter.
 * @param epsilon Second pruning parameter.
 * @param epsilonJoin Third pruning parameter.
 * @param epsilonDisc Fourth pruning parameter
 *
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient, double delta, double epsilon, double epsilonJoin, double epsilonDisc, int prune2Int) {
  
  NeighbourTreeList list, auxList;
  NeighbourTree nt;
  PotentialContinuousPT aux,pot;
  Relation rel, outwards, inwards;
  int i, label;
  Vector separator,vecVars;
  NodeList nSender, nRecipient, nInter;
  int howMany = 0;

  //System.out.println("Empezamos sendMessage");
  // Message form recipient to sender
  inwards = new Relation();
  
  //Message from sender to recipient
  outwards = new Relation();
  
  separator = new Vector();

  nSender = sender.getVariables();
  nRecipient = recipient.getVariables();
      
  nInter = nSender.intersection(nRecipient);


  //System.out.println("Variables de sender:");
  //sender.getVariables().print();
  //System.out.println("Variables de recipient:");
  //recipient.getVariables().print();

  aux = (PotentialContinuousPT)sender.getNodeRelation().getValues();
  pot = aux;
  System.out.println("El que hay en el clique tiene tama�o: "+pot.actualSize()+" y es:");
  //pot.print();
  if((willMarginaliseToOne(sender,recipient))){
      
      System.out.println("It will be just unit potential");
      pot = new PotentialContinuousPT();
      pot.setTree(new ContinuousProbabilityTree(1.0));
      
  }else{// The message won't be unity
      // This pot will be the potential stored in the clique
      
      //vble list will contain all the neigbbours of sender.
      list = sender.getNeighbourList();
      //System.out.println("Hay "+list.size()+" mensajes que le llegan");
      for (i=0 ; i<list.size() ; i++) {
	  nt = list.elementAt(i);
	  label = nt.getNeighbour().getLabel();
	  
	  // I want the one nt sends to sender, that is, othervalues
	  rel = nt.getMessage();
	  
	  // Combine the messages coming from the other neighbours
	  // It will indicate if the nodeJoinTree is leaf, if inside remains false, it will be a leaf.
	  howMany = 0; 
	  
	  if (label != recipient.getLabel()) { 
	      // Other values is the message that goes in the other direction
	      //inside = true;
	      System.out.println("Multiplico por uno de tama�o: "+((PotentialContinuousPT)rel.getOtherValues()).actualSize()+" que es �ste:");
	      //if(((PotentialContinuousPT)rel.getOtherValues()).actualSize() == 0)
	      //rel.getOtherValues().print();
	      if(((PotentialContinuousPT)rel.getOtherValues()).actualSize() == 1)// The message is unity, so it is not necessary to combine
		  System.out.println("Not combinig, since it is unity");
	      else{
		  howMany++;
		  if(howMany == 1){
		   if(pot.actualSize() == 1){//El potencial del nodo es 1

		       //System.out.println("We have combined just one arriving messages not zero, so it can still be considered conditional");
		       //pot = pot.combine((PotentialContinuousPT)rel.getOtherValues());
		       pot = (PotentialContinuousPT)rel.getOtherValues().copy();
		       if(((PotentialContinuousPT)rel.getOtherValues()).getComment() == "NotConditional"){
			   //System.out.println("But the message was not conditional, so the new message won't be conditional");
			   pot.setComment("NotConditional");
		       }
		   }else{// El potencial del clique no es 1
		       //System.out.println("Ni el del nodo ni el mensaje que llega son unidad, luego el mensaje resultante no es condicional");
		       pot = pot.combine((PotentialContinuousPT)rel.getOtherValues());
                       //System.out.println("Antes de hacer el prune2 despues de multiplicar");
                       //pot.print();
                       if(prune2Int == 1)
                        pot.prune2();
                       //System.out.println("tras hacer el prune2 despues de multiplicar");
                       //pot.print();
		       pot.setComment("NotConditional");
		       if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
			 
			 System.out.println("Ahora podamos, tras la combinaci�n");
			 pot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
			 System.out.println("Ya hemos podado");
			 
		       }
		   }
		  }else{// Llegan mas de un mensaje no unidad

		      //System.out.println("We have combined more than one arriving messages not zero, so it is no more conditional");
		      pot = pot.combine((PotentialContinuousPT)rel.getOtherValues());
                      //System.out.println("Antes de hacer el prune2 despues de multiplicar");
                      //pot.print();
                      if(prune2Int == 1)
                        pot.prune2();
                      //System.out.println("Tras hacer el prune2 despues de multiplicar");
                      //pot.print();
		      pot.setComment("NotConditional");
		      if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
	  
			System.out.println("Ahora podamos, tras la combinaci�n");
			pot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
			System.out.println("Ya hemos podado");
		      }
		      //pot.prune(0,0,0);
		      //  System.out.println("This is the result of the combination:");
		      // 			  pot.print();
		      // 			  System.out.println();
		  }	      
	      }
	  }else {//The neighbour is recipient
	      
	      outwards = rel;
	      separator =  rel.getVariables().getNodes();
	      auxList = recipient.getNeighbourList();
	      
	      //This is the message that goes from recipient to sender
	      inwards = auxList.getMessage(sender); 
	  }//End of else
      }//End of for
      //System.out.println("Al final de combinar queda con tama�o, antes de poda (y marginalizaci�n): "+pot.actualSize());
      //if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
	  
//	  System.out.println("Ahora podamos, tras la combinaci�n");
//	  pot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
	  
//      }
//      else{//End of if del prune
//	if(howMany > 0 ){// Solo hemos combinado en el caso de que howMany > 0
//	  System.out.println("Poda para igual tras combinacion");
//	  pot.prune(0,0,0);
//	}
  //    }
      //Now we marginalise over the intersection
      vecVars = new Vector();
      
      //If any variable has to be removed
      if(nInter.size() > 0){
	  
	  //System.out.println("Estas son las variables que han de quedar");
	  //System.out.println();
	  //nInter.print();
	  //if (sender.getNodeRelation().getVariables().intersection(nInter).size() == 0 )
	  //  System.out.println("En realidad no hay que marginalizar");
	  
	  for(i=0 ; i<nInter.size() ; i++)
	      vecVars.addElement(nInter.elementAt(i));
	  
	  //System.out.println("This is the potential we want to marginalise.");
	  //System.out.println();
	  //pot.print();
	  //System.out.println();
	  //System.out.println("This is the potential that remains after marginalising.");
	  if(pot.getComment() == "NotConditional"){
	      pot = (PotentialContinuousPT)pot.marginalizePotential(vecVars);
              //System.out.println("Antes de hacer el prune2 despues de marginalizar");
              //pot.print();
              if(prune2Int == 1)
                pot.prune2();
              //System.out.println("Tras hacer el prune2 despues de marginalizar");
              //pot.print();
	      pot.setComment("NotConditional");
	  }
	  else
	      pot = (PotentialContinuousPT)pot.marginalizePotential(vecVars);
	  
	  if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
	      System.out.println("Ahora podaremos, tras marginalizar");
	      pot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
	      System.out.println("Ya hemos podado");
	  }else{//End of if
	      //System.out.println("Tras marginalizar queda:");
	      //pot.print();
	      //System.out.println("Poda para igual tras marginalizacion");
	      //pot.prune(0,0,0);
	  }
      
      }//End of if(nInter.size() > 0)
  
  }// End of else
  // Now update the messages in the join tree. 
  //System.out.println("This the final Message.");
  //pot.print();
  outwards.setValues(pot);
  inwards.setOtherValues(pot);
  //System.out.println("Tras marginalizacion (y poda en su caso) queda con tama�o : "+pot.actualSize());
  //System.out.println("Acabamos sendMessage");
}//End of sendMessage
    

/**
 * Makes an identity <code>Potential</code> for the 
 * <code>NodeList nList</code> of the class used by this propagation 
 * method (<code>PotentialTree</code>). Override this method in subclases 
 * to get other behaviour.
 * @param nList a <code>NodeList</code>
 * @return an identity <code>PotentialContinuousPT</code>
 */

Potential makeUnitPotential(NodeList nList) {
  
  PotentialContinuousPT cpt;
  
  cpt = new PotentialContinuousPT(nList);
  cpt.setTree(ContinuousProbabilityTree.unitTree());
  return cpt;
}


/**
 * Computes the marginals after a propagation and put them into
 * instance variable <code>results</code>. Sets <code>positions</code>
 * for each variable, to locate the variable in <code>Vector results</code>.
 * Right now all the variables must be continuous
 *
 * @param delta First pruning parameter.
 * @param epsilon Second pruning parameter.
 * @param epsilonJoin Third pruning parameter.
 * @param epsilonDisc Fourth pruning parameter
 * @param e Evidence.
 * @param sizeVec The vector where the sizes will be stored
 */

public void computeMarginals(double delta, double epsilon, double epsilonJoin, double epsilonDisc, Evidence e, Vector sizeVec, int prune2Int) {
  
  int i, j, k, nv, pos, t;
  Vector marginal, variablesEviCont, variablesEviDisc, variablesEvi;
  NodeList variables;
  Relation r1, r2;
  PotentialContinuousPT pot, potAux;
  NodeJoinTree temp = new NodeJoinTree();
  NodeList l, lEvi, lCopy;
  Continuous v = new Continuous();
  FiniteStates discV;
  NeighbourTree nt;
  boolean found;
  Node nod, nod2;
  double tam;

  l = network.getNodeList();
  variablesEviDisc = e.getVariables();
 
  variablesEviCont = e.getContinuousVariables();
  
 // System.out.println("Estoy en el computeMarginals");

  variablesEvi = (Vector)variablesEviDisc.clone();
  for (i = 0 ; i < variablesEviCont.size() ; i++ ){
    
    nod2 = (Node)variablesEviCont.elementAt(i);
    variablesEvi.addElement(nod2);
  
  }
  
  lEvi = new NodeList(variablesEvi);
  lCopy = l.copy();
  lCopy = lCopy.difference(lEvi);

  

  for (i=0 ; i<lCopy.size() ; i++) {
   
    nod = lCopy.elementAt(i);  

    if (nod.getTypeOfVariable() == 0){//It is a continuous Variable
	v = (Continuous)nod;
	//System.out.println("Vamos a calcular la marginal para esta variable:");
	//v.print();
	j = 0;
	//temp = (NodeJoinTree)marginalCliques.get(v);

	 found = false;
 	while (found == false){
 	  temp = (NodeJoinTree)binTree.elementAt(j);  
 	    variables = temp.getVariables();
 	    // If variable v is in node temp, I take it from it, else I look for a nodeJoinTree
 	    // in which it is.
	    
 	     if (variables.getId(v) != -1){ 
 		found = true;
 		//System.out.println("We have found a clique containing this variable.");
 	    }
 	    else j++;
 	}//End of while
	r1 = temp.getNodeRelation();
	pot = (PotentialContinuousPT)r1.getValues();
	//System.out.println("");
	//System.out.println("This is the potential in this nodeJoinTree");
	//System.out.println("");
	//pot.print();
	tam = pot.actualSize();
	for (k=0 ; k<temp.getNeighbourList().size() ; k++) {
	    nt = temp.getNeighbourList().elementAt(k);
	    r2 = nt.getMessage();
	    //System.out.println("Message arriving to this nodeJoinTree");
	    //((PotentialContinuousPT)(r2.getOtherValues())).print();

	    pot = (PotentialContinuousPT)pot.combine(r2.getOtherValues());
            if(prune2Int == 1)
                pot.prune2();   
	    if(pot.actualSize() > tam )
		tam = pot.actualSize();
	    
	    if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
		System.out.println("Ahora podaremos, tras combinar");
		pot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
	    }//End of if
	}
	sizeVec.addElement(new Double(tam));
	//System.out.println("After combining every message and the clique potential, and before pruning, the potetnial is: ");
	//pot.print();
	//System.out.println("And the actual size is: "+pot.actualSize());

	// if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
// 	    System.out.println("Ahora podaremos, tras combinar");
// 	    pot.prune(delta,epsilon,epsilonJoin, epsilonDisc);
// 		}//End of if
	
	marginal = new Vector();
	marginal.addElement(v);
	//System.out.println("Now we marginalise");
	//System.out.println("This is the potential we want to marginalise");
	//pot.print();
	pot = (PotentialContinuousPT)(pot.marginalizePotential(marginal));
        if(prune2Int == 1)
            pot.prune2();
	//System.out.println("After marginalising");
	//pot.print();
	if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
	    System.out.println("After marginalising, we prune");
	    pot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
		}//End of if

	//System.out.println("We have already marginalised and pruned, and this is the outcome: ");
	//System.out.println("");
	//pot.print();
	//System.out.println("Now we normalise");
	pot.normalize();
	//System.out.println("We have already normalised");
	
	// // Quizas este potencial es solo un numero, pero eso dara fallo, asi que lo arreglamos
// 	if(pot.getTree().isProbab()){
// 	    neCp = new Vector();
// 	    neCp.addElement(new Double(v.getMin()));
// 	    neCp.addElement(new Double(v.getMax()));
// 	    newTree = new ContinuousProbabilityTree(v,neCp);
// 	    newTree.setNewChild(pot.getTree(),0);
// 	}


	//System.out.println("This is the marginal potential of the variable");
	//pot.print();
	results.addElement(pot);    
	positions.put(v,new Integer(i));
	//posResult++;
    }
    else{
	discV = (FiniteStates)nod;
	//System.out.println("Let's get the marginal for this variable:");
	//discV.print();
	j = 0;
	//temp = (NodeJoinTree)marginalCliques.get(discV);

 	found = false;
 	while (found == false){
 	    temp = (NodeJoinTree)binTree.elementAt(j);  
 	    variables = temp.getVariables();
 	    // If variable v is in node temp, I take it from it, else I look for a nodeJoinTree
 	    // in which it is.
	   
 	    if (variables.getId(discV) != -1){ 
 		found = true;
 		//System.out.println("We've already found a clique that contains this variable.");
 	    }
 	    else j++;
 	}//End of while
	    r1 = temp.getNodeRelation();
	pot = (PotentialContinuousPT)r1.getValues();
	//System.out.println("");
	//System.out.println("This is the potential in this nodeJoinTree");
	//System.out.println("");
	//pot.print();
	tam = pot.actualSize();
	for (k=0 ; k<temp.getNeighbourList().size() ; k++) {
	    nt = temp.getNeighbourList().elementAt(k);
	    r2 = nt.getMessage();
	    //System.out.println("This message arrives to this nodeJoinTree");
	    //((PotentialContinuousPT)(r2.getOtherValues())).print();
	    pot = (PotentialContinuousPT)pot.combine(r2.getOtherValues());
            if(prune2Int == 1)
                pot.prune2();
	    if(pot.actualSize() > tam )
		tam = pot.actualSize();
	    
	    if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
		System.out.println("Ahora podaremos, tras combinar");
		pot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
	    }//End of if
	}
	//System.out.println("After combinig every message");
	sizeVec.addElement(new Double(tam));
	//System.out.println("After combining every message and the clique potential, and before pruning, the potetnial is: ");
	//pot.print();
	//System.out.println("And the actual size is: "+pot.actualSize());

	// if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
// 	    //System.out.println("After combining, we will prune");
// 	    pot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
// 		}//End of if
	
	marginal = new Vector();
	marginal.addElement(discV);
	//System.out.println("Now we will marginalise this potential");
	//pot.print();
	pot = (PotentialContinuousPT)(pot.marginalizePotential(marginal));
	
	// We do NOT prune now. 
	/**
	*if ((delta != 0 ) || (epsilon != 0)){
	*    //System.out.println("After marginalising, we prune");
	*    pot.prune(delta,epsilon);
	*
	* }//End of if
	*/ 
	
	//System.out.println("We have already marginalised and pruned, and this is the result: ");
	//System.out.println("");
	//pot.print();
	//System.out.println("Now we will normalise");
	pot.normalize();
	//System.out.println("We have already normalised");
	//System.out.println("This is the marginal potential for the variable");
	//pot.print();
	if(pot.getTree().isProbab()){
	 //   System.out.println("Si es prob");
	    double[] a;
	    a = new double[discV.getNumStates()];
 	    for(t=0 ; t < discV.getNumStates() ; t++)
		a[t] = pot.getTree().getProb().getIndependent();
	    ContinuousProbabilityTree newTree = new ContinuousProbabilityTree(discV,a);
 	    pot.setTree(newTree);
 	}

	//System.out.println("This is the marginal potential for the variable");
	//pot.print();
	results.addElement(pot);    
	positions.put(discV,new Integer(i));
	//posResult++
    }
  }
}

/**
 *
 * This method assigns the relations/potentials of the original
 * network to the cliques in the JoinTree. 
 * To be used with continuous variables in the Shenoy Shafer propagation algorithm.
 *
 * @param binTree The JoinTree to initialise.
 * @param b The network to take the relations from.
 * @param e The evidende.
 */

public void initRelations(JoinTree binTree, Bnet b,Evidence e, double delta, double epsilon, double epsilonJoin,double epsilonDisc, int prune2Int){

  int posChosen = 0;
  int biggerPos,biggerSize,newSize,h,j,i,k;
  RelationList r = new RelationList();
  RelationList rCopy = new RelationList();
  RelationList rCopy2 = new RelationList();
  RelationList rCopy3 = new RelationList();
  NodeJoinTree node;
  NodeJoinTree nj;
  PotentialContinuousPT pot,newPot,potClique;
  NodeList nl,nlNod, inter;
  ContinuousProbabilityTree cpt;
  Relation rel,newRel,relClique;
  boolean elegido;
  Node childVar;
  Vector inf = new Vector();
  Vector newVec = new Vector();
  int size2, newSize2;

  // The first thing I do is to make unit potentials.

  for(i = 0 ; i< binTree.size() ; i++){
    node = binTree.elementAt(i);
    nl = node.getVariables();
    //System.out.println("These are the variables in the nodeJoinTree "+i+" :");
    //nl.print();
    pot = new PotentialContinuousPT(nl);
    cpt = new ContinuousProbabilityTree(1);
    pot.setTree(cpt);
    rel = new Relation();
    rel.setValues(pot);
    rel.setVariables(nl);  
    node.setNodeRelation(rel);
    
  }
  
  //r has every relation in the network.
  r = b.getInitialRelations();
  
  // We copy r so that we do not 'kill' the actual relations.
  rCopy = r.copy();
  
  // Now we restrict to the observations (if there is any)
  //System.out.println("Before restricting to obs");
  rCopy.restrictToObservations(e);
  rCopy2 = rCopy.copy();
  //System.out.println("After restricting to obs.");
  // Now I assign every relation to one clique.
  k = 0;
  while(rCopy2.size() > 0){
    biggerSize = ((PotentialContinuousPT)rCopy2.elementAt(0).getValues()).actualSize();
    biggerPos = 0;
    
    for(i=1 ; i < rCopy2.size() ; i++){
      newSize = ((PotentialContinuousPT)rCopy2.elementAt(i).getValues()).actualSize();
      if(newSize > biggerSize){
	biggerSize = newSize ; 
	biggerPos = i;
      }//End of if
    }// End of for
    
    rel = rCopy2.elementAt(biggerPos);
    //Relation relOrig = r.elementAt(biggerPos);
    //System.out.println("Relacion antes de restringir a la evidencia");
    //relOrig.print();
    //System.out.println("Relacion tras de restringir a la evidencia");
    //rel.print();
    //System.out.println("Quiero insertar un potencial de tama�o "+((PotentialContinuousPT)rel.getValues()).actualSize());
    rCopy2.removeRelationAt(biggerPos);
    
    //Este nuevo relationList tiene las relaciones ordenadas por tama�o
    rCopy3.insertRelation(rel);
    
    pot = (PotentialContinuousPT)rel.getValues();
    nl =  rel.getVariables();
    
    Vector vecPosiblesIndex = new Vector();
    vecPosiblesIndex.addElement(new Integer(k));
    k++;
    // Este vector tiene en la primera posicion el indice de la relacion a insertar en el rCopy3
    for(i=0 ; i<binTree.size() ; i++){// Ahora miramos cada clique del JoinTree a ver si es posible que alberge a pot
      node = binTree.elementAt(i);
      
      nlNod = node.getVariables();
      inter = nl.intersection(nlNod);
      
      if(inter.equals(nl))
	vecPosiblesIndex.addElement(new Integer(i));
    }//End of for
    
    //System.out.println("El numero de posibles cliques en los que insertar el potencial es: "+vecPosiblesIndex.size()-1);
    inf.addElement(vecPosiblesIndex);
    // el vector inf tiene los vecPosiblesIndex de todas las relaciones
  }//End of while 
  
  // Ahora vamos a colocarlas. Primero colocamos aquellas que tienen 1 sola opcion donde colocarse
  
  elegido = false;
  
  for(h=0 ; h < rCopy3.size() ; h++){
    
    newVec = (Vector)inf.elementAt(h);
    if(newVec.size() == 2){// La primera posicion es la variable en rCopy3 y la segunda el unico clique donde puede ir
      rel = rCopy3.elementAt(((Integer)newVec.elementAt(0)).intValue());
      
      //posChosen = ((Integer)newVec.elementAt(1)).intValue();
      //System.out.println("Quiero insertar un potencial de tama�o "+((PotentialContinuousPT)rel.getValues()).actualSize());
  
      posChosen = 1;
      //System.out.println("Solo hay un opcion, asi q Lo he insertado en el clique "+((Integer)newVec.elementAt(posChosen)).intValue());
      // Ahora lo inserto
      //System.out.println("Lo he insertado en el clique "+((Integer)newVec.elementAt(posChosen)).intValue());
      // Este es el clique del JoinTree elegido
      node = binTree.elementAt(((Integer)newVec.elementAt(posChosen)).intValue());
      
      // Relacion que quiero insertar
      //rel = rCopy3.elementAt(((Integer)newVec.elementAt(0)).intValue());
      
      // Potencial de la relacion que quiero insertar
      pot = (PotentialContinuousPT)rel.getValues();
      
      // Relacion del clique
      relClique = node.getNodeRelation();
      // potencial de la relacion del clique
      potClique =  (PotentialContinuousPT)relClique.getValues();
      if(potClique.isUnity()){
	newPot = (PotentialContinuousPT)pot.copy();
        if(prune2Int == 1)
            newPot.prune2();
	
	//Ahora antes de que acabemos podamos
	if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
	    //System.out.println("After marginalising, we prune");
	    newPot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
	}//End of if
      }else{
	newPot = pot.combine(potClique);
        if(prune2Int == 1)
            newPot.prune2();
	newPot.setComment("NotConditional");
	
	//Ahora antes de que acabemos podamos
	if ((delta != 0 ) || (epsilon != 0) || (epsilonDisc != 0) || (epsilonJoin != 0)){
	    //System.out.println("After marginalising, we prune");
	    newPot.prune(delta,epsilon,epsilonJoin,epsilonDisc);
	}//End of if
      }
      nlNod = new NodeList();
      nlNod = node.getVariables();
      //Ahora a�ado el potencial newPot al nodo
      
      newRel = new Relation();
      newRel.setValues(newPot);
      newRel.setVariables(nlNod);
      node.setNodeRelation(newRel);
      
    }// fin del if
    
  }// fin del for
  
  // Ahora pongo el resto
  for(h=0 ; h < rCopy3.size() ; h++){
    
    newVec = (Vector)inf.elementAt(h);
    if(newVec.size() > 2){
      elegido = false;
      // Relacion que quiero insertar
      rel = rCopy3.elementAt(((Integer)newVec.elementAt(0)).intValue());
      //System.out.println("Relacion a insertar");
      //rel.print();
      
      if(rel.getVariables().size() == 0){
      
          System.out.println("No hay que insertarlo, no tiene variables");
          
      }else{
      
      //System.out.println("Quiero insertar un potencial de tama�o "+((PotentialContinuousPT)rel.getValues()).actualSize());
      //System.out.println("El numero de posibles cliques en los que insertar el potencial es: "+(newVec.size()-1));
  
      // Potencial de la relacion que quiero insertar
      pot = (PotentialContinuousPT)rel.getValues(); 
      // 1� opcion
      for(i=1 ; i<newVec.size() ; i++){// ahora para todos los posibles cliques vemos cual es el mejor
	nj = binTree.elementAt(((Integer)newVec.elementAt(i)).intValue());
	nl = rel.getVariables();
	childVar = nl.elementAt(0);
	if((!elegido)&& (nj.isLeaf()) && (((PotentialContinuousPT)nj.getNodeRelation().getValues()).isUnity()) && (nj.deleteVar(childVar))){
	  elegido = true;
	  posChosen = i; // Posicion dentro del vecPosiblesIndex
	  //System.out.println("1� opcion");
	}// End of if
	
      }// End of for
      
      // 2� opcion
      if(!elegido){
	// Emepzamos en uno pq el cero es la relacion a insertar
	for(i=1 ; i<newVec.size() ; i++){// ahora para todos los posibles cliques vemos cual es el mejor
	  nj = binTree.elementAt(((Integer)newVec.elementAt(i)).intValue());
	  if((!elegido) && (nj.isLeaf()) && (((PotentialContinuousPT)nj.getNodeRelation().getValues()).isUnity())){
	    elegido = true;
	    posChosen = i; // Posicion dentro del vecPosiblesIndex
	    //System.out.println("2� opcion");
	  }// End of if
	}// End of for
      }// End of if
      
      // 3� opcion
      if(!elegido){
	for(i=1 ; i<newVec.size() ; i++){// ahora para todos los posibles cliques vemos cual es el mejor
	  nj = binTree.elementAt(((Integer)newVec.elementAt(i)).intValue());
	    if((!elegido)&& ((PotentialContinuousPT)nj.getNodeRelation().getValues()).isUnity()){
	      elegido = true;
	      posChosen = i; // Posicion dentro del vecPosiblesIndex
	      //System.out.println("3� opcion");
	    }// End of if
	}// End of for
      }// End of if
      
      // ultima opcion
      if(!elegido){// Esta ultima opcion he de ver en que clique insertarlo seria mas favorable (menor tama�o)
	nj = binTree.elementAt(((Integer)newVec.elementAt(1)).intValue());
	relClique = nj.getNodeRelation();
	potClique =  (PotentialContinuousPT)relClique.getValues();
	size2 = potClique.actualSize();
	posChosen = 1;
	//System.out.println("Tama�o del primero: "+size2);
	for(i=2 ; i<newVec.size() ; i++){
	  nj = binTree.elementAt(((Integer)newVec.elementAt(i)).intValue());
	  relClique = nj.getNodeRelation();
	  potClique =  (PotentialContinuousPT)relClique.getValues();
	  newSize2 = potClique.actualSize();
	  //System.out.println("Tama�o del "+i+" : "+newSize2);
	  if(newSize2 < size2){// Hay uno de menor tama�o, nos quedamos con ese
	    posChosen = i;
	    size2 = newSize2;
	    //System.out.println("El "+i+" es de menor tama�o");
	  }
	}
	//System.out.println("4� opcion");
      }
      //System.out.println("Lo he insertado en el clique "+((Integer)newVec.elementAt(posChosen)).intValue());
      // Este es el clique del JoinTree elegido
      node = binTree.elementAt(((Integer)newVec.elementAt(posChosen)).intValue());
      
      relClique = node.getNodeRelation();
      potClique =  (PotentialContinuousPT)relClique.getValues();
      if(potClique.isUnity()){
	newPot = (PotentialContinuousPT)pot.copy();
         if(prune2Int == 1)
            newPot.prune2();
      }else{
	newPot = pot.combine(potClique);
        if(prune2Int == 1)
            newPot.prune2();
	newPot.setComment("NotConditional");
      }
      nlNod = new NodeList();
      nlNod = node.getVariables();
      //Ahora a�ado el potencial newPot al nodo
      
      newRel = new Relation();
      newRel.setValues(newPot);
      newRel.setVariables(nlNod);
      node.setNodeRelation(newRel);
      }
    }// Fin del if
  }// fin del for
}//End of method


/**
 * This method computes the error we get on each marginal variable
 * when the pruning methods are used.
 *
 */

Vector computeErrors(Vector relVec){

int i,j;
Relation exactRel;
PotentialContinuousPT exactPot, approxPot;
ContinuousProbabilityTree exactCPT , approxCPT;
boolean found;
Node exactVar, approxVar;
ContinuousIntervalConfiguration conf = new ContinuousIntervalConfiguration();
double error;
Vector errorVector = new Vector();

//System.out.println("Entro en el computeErrors");

for(i=0 ; i<relVec.size() ; i++){// I select a relation from the relation

  exactPot = (PotentialContinuousPT)(((Relation)relVec.elementAt(i)).getValues());
  //exactRel = (Relation)((propagation2.results).elementAt(i));
  //exactPot = (PotentialContinuousPT)exactRel.getValues();
  exactCPT = exactPot.getTree();
  exactVar = exactCPT.getVar();
  //System.out.println("Potencial exacto");
  //exactPot.print();
  //System.out.println("Erro de la var:");
  //exactVar.print();
  //Now I try to find a potential in results containing this variable
  j = 0;
  found = false;
  while ((!found) & (j<results.size())){
  
    approxPot = (PotentialContinuousPT)results.elementAt(j);
    //System.out.println("Este es el approxPot numero "+j);
    //approxPot.print();
    approxCPT = approxPot.getTree();
    approxVar = approxCPT.getVar();
    //System.out.println("En el approx tengo la var:");
    //approxVar.print();
    if(exactVar.equals(approxVar)){//We have found the approximate version of the exact potential
      // In conf I must put the variable
      if(exactVar.getTypeOfVariable() == 0){// It is continuous
	  conf.putValue(((Continuous)exactVar),((Continuous)exactVar).getMin(),((Continuous)exactVar).getMax());
	  //System.out.println("Empiezo a calcular el error");
	  error = exactCPT.ErrorPruning(exactCPT,approxCPT,conf,1,0);
	  //System.out.println("Ya he calculado el error");
	  conf = new ContinuousIntervalConfiguration();
      }
      else{// It is discrete, the error is compute according to other formula (also the mean square error)
	
	error = exactCPT.ErrorDiscrete(exactCPT,approxCPT);
      
      }
      //System.out.println("El error es: "+error);
      errorVector.addElement(new Double(error));
      found = true;
    }// End of if
    j++;
  }// End of while
}//End of for
//System.out.println("Salgo del computeErrors");
return errorVector;

}//End of method computeErrors


/**
 * This method computes some statistics about the errors/sizes: It will return a vector 
 * in which there will be: Position 0: The mean, Position 1: The standard deviation, 
 * Position 2: The maximum, and Position 3: The minimum.
 * 
 * @param errors A vector containing the errors
 *
 * @return A vector containing the statisticas about the errors/sizes.
 *
 */

public Vector computeStatistics(Vector errors){

  int i;
  double x, mean = 0, sd = 0, max = 0, min;
  Vector stat = new Vector();
  
  if (errors.size() < 1){
    System.out.println("No errors/sizes to compute statistics about");
    System.exit(1);
  
  }
  
  min = ((Double)errors.elementAt(0)).doubleValue();
  
  for (i=0 ; i<errors.size() ; i++){
     
    x = ((Double)errors.elementAt(i)).doubleValue();
    //System.out.println("Valor del error: "+x);
    mean = mean +x;
    //System.out.println("Suma de los x ,vamos por "+i+",: "+mean);
    sd = sd + (x*x);
    max = Math.max(max,x);
    //System.out.println("Maximo ,vamos por "+i+",: "+max);
    //System.out.println("Suma de los x^2 ,vamos por "+i+",: "+sd);
    min = Math.min(min,x);
    //System.out.println("Minimo ,vamos por "+i+",: "+min);    
  }
  
  mean = mean/errors.size();
  //System.out.println("Media: "+mean);
  sd = sd/errors.size();
  sd = sd - (mean*mean);
  //System.out.println("Desv tipica: "+sd);
  stat.addElement(new Double(mean));
  stat.addElement(new Double(sd));
  stat.addElement(new Double(max));
  stat.addElement(new Double(min));
  
  return stat;
  
}// End of method

/**
 * Gets the initial relations in the network.
 * @return the initial relations present in the network, as a
 * <code>RelationList</code>.
 */

public RelationList getInitialRelations() {
  
  Relation rel, newRel;
  RelationList list;
  PotentialContinuousPT pt;
  int i;
 
  list = new RelationList();
  
  for (i=0 ; i<network.getRelationList().size() ; i++) {
    rel = (Relation)network.getRelationList().elementAt(i);
    newRel = new Relation();
    newRel.setVariables(rel.getVariables().copy());
    
    pt = (PotentialContinuousPT)rel.getValues();
    
    newRel.setValues(pt);
    newRel.setKind((int)(rel.getKind()));
    list.insertRelation(newRel);
  }
  
  return list;
}

/**
 * This method tells you if the message <code>sender<\code> will send to 
 * <code>recipient<\code> will be a unit potential before obtaining it, having into account that every relation in a 
 * clique is a conditional relation.
 * 
 * @param sender The Node JoinTree that sends the message.
 * @param recipient The Node JoinTree that receives the message.
 * 
 * @return A boolean indicating if the message is one or not. 
 * 
 */   

public boolean willMarginaliseToOne(NodeJoinTree sender,NodeJoinTree recipient){

    boolean one = false;
    int k,notUnities,label,h;
    Relation senderRelation,r2 ; 
    NodeList recipientVars ; 
    NodeList senderRelationVars;
    NodeList intersection;
    NeighbourTree nt;
    PotentialContinuousPT p2;
    Node nodeIn;
    boolean itIs;
    int howMany;
    //There are two different ways of getting an unity potential:
    // a) The sender is a leaf, with potential x1|x2,x3, and var x1 must be marginalised
    // b) The potential in sender is 1, and every message arriving it too except one of them
    // that is a conditional prob, say x1|x2,x3, and x1 must be normalised

    System.out.println("In willMarginaliseToOne");

    senderRelation = sender.getNodeRelation();
    senderRelationVars = senderRelation.getVariables();

    recipientVars = recipient.getVariables();
    
    intersection = senderRelationVars.intersection(recipientVars);
    // In this intersection must be every variable in senderRelation, except the first one.
    one = true;
    howMany = 0;
     if(((PotentialContinuousPT)senderRelation.getValues()).getComment() == "NotConditional"){
 	System.out.println("Pone NotConditional en el potencial del clique");
	one = false;
     }else{// Si es condicional
      
       for (k=0 ; k<sender.getNeighbourList().size() ; k++) {
		nt = sender.getNeighbourList().elementAt(k);
		label = nt.getNeighbour().getLabel();
		if (label != recipient.getLabel()) { 
		    r2 = nt.getMessage();
		    p2 = (PotentialContinuousPT)r2.getOtherValues();//Mensaje que llega
		    if(one){
		    System.out.println("Message arriving to this nodeJoinTree"); 
		    //p2.print();
		    System.out.println("Ahora voy a ver si es unity");
		    
		    if(p2.actualSize() == 1) {
		      System.out.println("This is unity");
		      if(((PotentialContinuousPT)senderRelation.getValues()).actualSize() != 1){
			System.out.println("El del clique no es unidad, asi que hay que ver si se borra la variable hija");
			nodeIn = (Node)sender.getNodeRelation().getVariables().elementAt(0);
			itIs = false;
			if(intersection.getId(nodeIn)!= -1){
			  System.out.println("No hay que eliminar la primera var");
			  itIs = true;
			  one = false;
			}
			if(!itIs){ 
			  System.out.println("Se quita la primera, luego ser� uno la marginal");
			  			  
			}
			
		      }else{
		      
			System.out.println("El del clique es unidad");
		      }
		    }else{// El mensaje que llega no es unidad
			howMany++;
			if(p2.getComment() == "NotConditional"){
			    System.out.println("El mensaje que llega es NoCondicional");
			    one = false;
			}else{
			    System.out.println("El mensaje que llega es conditional"); 
			    if(howMany == 1){
				if(((PotentialContinuousPT)senderRelation.getValues()).actualSize() != 1){
				    System.out.println("El potencial del clique No es unidad");
				    one = false;
				}else{
				    System.out.println("El potencial del clique Si es unidad");
				    // Ahora hay que ver si elimina la variable hija
				    itIs = false;
				    //System.out.println("The pot is conditional");
				    nodeIn = (Node)r2.getOtherValues().getVariables().elementAt(0);
				    if(intersection.getId(nodeIn)!= -1){
					System.out.println("No hay que eliminar la primera var");
					itIs = true;
					one = false;
				    }
				    if(!itIs){ 
					System.out.println("Se quita la primera, luego ser� uno la marginal");
				
				    }
				}
			    }else{// howMany es > 1
			    System.out.println("Se multiplica por al menos dos mensajes no unidad");
			    one = false;
			    }




//     //System.out.println("It is not leaf");
// 	notUnities = 0;
// 	if(((PotentialContinuousPT)senderRelation.getValues()).actualSize() == 1){
// 	  if(!(((PotentialContinuousPT)senderRelation.getValues()).getComment() == "NotConditional")){
// 	    for (k=0 ; k<sender.getNeighbourList().size() ; k++) {
// 		nt = sender.getNeighbourList().elementAt(k);
// 		label = nt.getNeighbour().getLabel();
// 		if (label != recipient.getLabel()) { 
// 		    r2 = nt.getMessage();
// 		    p2 = (PotentialContinuousPT)r2.getOtherValues();
// 		    System.out.println("Message arriving to this nodeJoinTree"); 
// 		    //p2.print();
// 		    System.out.println("Ahora voy a ver si es unity");
// 		    if((p2.actualSize() == 1)){
// 			System.out.println("This is unity");
// 		    }else{
// 			System.out.println("This is NOT unity");
// 			    notUnities++;
// 			    if(notUnities == 1){
// 				System.out.println("One not unity");	
// 				if(!(p2.getComment() == "NotConditional")){
// 				    //System.out.println("Es conditional");
// 				    itIs = false;
// 				    System.out.println("The pot is conditional");
// 				    nodeIn = (Node)r2.getOtherValues().getVariables().elementAt(0);
// 				    if(intersection.getId(nodeIn)!= -1){
// 					System.out.println("No hay que eliminar la primera var");
// 					itIs = true;
// 					one = false;
// 				    }
				    
// 				    if(!itIs){ 
// 					System.out.println("Se quita la primera, luego ser� uno la marginal");
// 					one = true;
					
// 				    }
// 				}else{ // de not conditional
// 				    System.out.println("Es NotConditional");
// 				  one = false;
// 				}
// 			    }else{// There is more than one message arriving different to 1
// 				System.out.println("More than one not unity");	
// 				one = false;
// 			    }
			    
// 		    }//End of else
// 		}
// 	    }// End of for
// 	  }//End of if (es conditional)
// 	  else{
	    
// 	    one = false;
// 	  }
// 	}else{
// 	    System.out.println("El potencial del nodo NO es uno");
// 	    if(!(((PotentialContinuousPT)senderRelation.getValues()).getComment() == "NotConditional")){
// 	      //El potencial del nodo es condicional
// 	      for (k=0 ; k<sender.getNeighbourList().size() ; k++) {
// 		nt = sender.getNeighbourList().elementAt(k);
// 		label = nt.getNeighbour().getLabel();
// 		if (label != recipient.getLabel()) { 
// 		  r2 = nt.getMessage();
// 		  p2 = (PotentialContinuousPT)r2.getOtherValues();
// 		  System.out.println("Message arriving to this nodeJoinTree"); 
// 		  //p2.print();
// 		  System.out.println("Ahora voy a ver si es unity");
// 		  if((p2.actualSize() == 1)){
// 		    System.out.println("This is unity");
// 		  }else{
// 		    System.out.println("This is NOT unity, so the message will not be unity");
// 		    one = false;    
// 		  }//End of else
// 		}
// 	      }// End of for
// 	    }else{// El potencial del nodo NO es condicional
// 	      one = false;
// 	    }
// 	}
//     System.out.println("Exiting willMarginaliseToOne");
//     return one;
			}//End of else se multiplica por mas de un mensaje
		    }//End of else
		    }else{
		      System.out.println("El one ya es falso, asi que no miro mas");
		    }
		    
		    
		}else{// End of 	if (label != recipient.getLabel()) { 
		    System.out.println("Este es el mensaje que llega del nodo recepient, luego no lo tengo en cuenta.");
		    // Hay que ver si solo llega este mensaje, si es asi he de ver si se elimina la variable hija, si es cond y no unity
		    if(((PotentialContinuousPT)sender.getNodeRelation().getValues()).actualSize() != 1){
			nodeIn = (Node)sender.getNodeRelation().getVariables().elementAt(0);
			itIs = false;
			if(intersection.getId(nodeIn)!= -1){
			    System.out.println("No hay que eliminar la primera var");
			    one = false;
			}
			if(!itIs){ 
			    System.out.println("Se quita la primera, luego puede ser uno la marginal");
	       
			}
		    }else{
			System.out.println("El pot del clique es unity, ");
		    }
		}
	    }// End of for todos los mesajes que llegan
     }//End of else si es condicional
     System.out.println("Exiting willMarginaliseToOne");
     
     return one;
}// End of method
// Para guardar los resultados como una red

    public void saveResults(String s) throws IOException{

	int i;
	NodeList varNl = new NodeList(); 
	Vector relVector = new Vector();
	Bnet net = new Bnet();
	PotentialContinuousPT pot;
	Node var;
	Relation rel;
	NodeList nl;

	for(i=0 ; i < results.size() ; i++){
	    
	    pot = (PotentialContinuousPT)results.elementAt(i);
            //System.out.println("Este es el potencial que voy a grabar: ");
            //pot.print();
	    //var = pot.getTree().getVar();
            var = (Node)((Vector)((Potential)pot).getVariables()).elementAt(0);
	    nl = new NodeList();
	    nl.insertNode(var);
	    varNl.insertNode(var);
	    rel = new Relation();
	    rel.setVariables(nl);
	    rel.setValues(pot);
	    relVector.addElement(rel);
	   
	}

	net.setRelationList(relVector);

	
	net.setNodeList(varNl);
	FileWriter f = new FileWriter(s);
	net.saveBnet(f);
	f.close();
    }

/**
 * This method computes the error we get on each marginal variable
 * when the pruning methods are used.
 *
 */

Vector computeErrors2Vectors(Vector relVecExact, Vector relVecApprox){

int i,j;
Relation exactRel;
PotentialContinuousPT exactPot, approxPot;
ContinuousProbabilityTree exactCPT , approxCPT;
boolean found;
Node exactVar, approxVar;
ContinuousIntervalConfiguration conf = new ContinuousIntervalConfiguration();
double error;
Vector errorVector = new Vector();

System.out.println("Entro en el computeErrors2Vectors");

for(i=0 ; i<relVecExact.size() ; i++){// I select a relation from the relation

  exactPot = (PotentialContinuousPT)(((Relation)relVecExact.elementAt(i)).getValues());
  //exactRel = (Relation)((propagation2.results).elementAt(i));
  //exactPot = (PotentialContinuousPT)exactRel.getValues();
  exactCPT = exactPot.getTree();
  exactVar = exactCPT.getVar();
  System.out.println("Potencial exacto");
  exactPot.print();
  System.out.println("var del exacto:");
  exactVar.print();
  //Now I try to find a potential in results containing this variable
  j = 0;
  found = false;
  while ((!found) & (j<relVecApprox.size())){
  
    approxPot = (PotentialContinuousPT)(((Relation)relVecApprox.elementAt(j)).getValues());
    //System.out.println("Este es el approxPot numero "+j);
    //approxPot.print();
    approxCPT = approxPot.getTree();
    approxVar = approxCPT.getVar();
    //System.out.println("En el approx tengo la var:");
    //approxVar.print();
    if(exactVar.equals(approxVar)){//We have found the approximate version of the exact potential
	// In conf I must put the variable
	System.out.println("Este es el approxPot numero "+j);
	approxPot.print();
	
	System.out.println("En el approx tengo la var:");
	approxVar.print();	
      if(exactVar.getTypeOfVariable() == 0){// It is continuous
	  conf.putValue(((Continuous)approxVar),((Continuous)approxVar).getMin(),((Continuous)approxVar).getMax());
	  System.out.println("Empiezo a calcular el error");
	  error = exactCPT.ErrorPruning(exactCPT,approxCPT,conf,1,0);
	  System.out.println("Ya he calculado el error");
	  conf = new ContinuousIntervalConfiguration();
      }
      else{// It is discrete, the error is compute according to other formula (also the mean square error)
	
	error = exactCPT.ErrorDiscrete(exactCPT,approxCPT);
      
      }
      System.out.println("El error es: "+error);
      errorVector.addElement(new Double(error));
      found = true;
    }// End of if
    j++;
  }// End of while
}//End of for
//System.out.println("Salgo del computeErrors");
return errorVector;

}//End of method computeErrors



}//End of class 
