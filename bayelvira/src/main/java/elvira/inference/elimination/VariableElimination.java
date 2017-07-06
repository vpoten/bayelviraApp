/* VariableElimination.java */

package elvira.inference.elimination;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.*;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.tools.Crono;


/**
 * This class implements a generic variable elimination method of propagation.
 * The initial potentials can be of any kind, but
 * they must define the methods:
 * <ul>
 * <li> <code>Potential combine(Pontential combine)</code>
 * <li> <code>Potential addVariable(FiniteStates var)</code>
 * </ul>
 * This class can be extended for special requirements. The methods
 * that can be overloaded are:
 * <ul>
 * <li> <code><a href="#transformInitialRelation(Relation)">Relation transformInitialRelation(Relation r)</a></code>
 * <li> <code><a href="#transformAfterAdding(Potential)">Potential transformAfterAdding(Potential pot)</a></code>
 * <li> <code><a href="#transformAfterEliminating(Potential)">Potential transformAfterEliminating(Potential pot)</a></code>
 * <li> <code><a href="#transformAfterOperation(Potential)">Potential transformAfterOperation(Potential pot, boolean flag)</a></code>
 * <li> <code><a href="#combine(Potential, Potential)">Potential combine(Potential pot1,Potential pot2)</a></code>
 * <li> <code><a href="#addVariable(Potential, FiniteStates)">Potential addVariable(Potential pot,FiniteStates var)</a></code>
 * </ul>
 * @author Antonio Salmer�n (Antonio.Salmeron@ual.es)
 * @author Andr�s Cano (acu@decsai.ugr.es)
 * @see VEWithPotentialTree
 * @since 14/3/2001
 */

public class VariableElimination extends Propagation {
   
   /**
    * Max number real numbers to represent each Potential
    */
   protected int limitSize;
  
   /**
    * The relations available in a given moment.
    */
   
   protected RelationList currentRelations;
   
   
   /**
    * Crono, to measure the computation times
    */
   
   protected Crono crono;

   /**
    * To show if we want to use statistics about the evaluation
    * It is required to change this flag to use statistics
    */

   public boolean generateStatistics=true;
   
   /**
    * Constructs a new propagation for a given Bayesian network and
    * some evidence.
    *
    * @param b a <code>Bnet</code>.
    * @param e the evidence.
    */
   
   public VariableElimination(Bnet b, Evidence e) {
      
      observations = e;
      network = b;
      results = new Vector();
      crono=new Crono();
      
   }
    
   /**
    * Constructs a new propagation for a given Bayesian network and
    * some evidence.
    *
    * @param b a <code>Bnet</code>.
    * @param e the evidence.
    * @param p it indicates whether we need to compute the probability of the evidence
    */
   
   public VariableElimination(Bnet b, Evidence e, boolean p) {
      
      observations = e;
      network = b;
      crono=new Crono();
      
      // Modifies the IDiagram, to add non forgetting arcs,
      // to eliminate redundancy and to transform the set
      // of initial relations
        probEvidence = p;     
   }
    
   /**
    * Constructs a new propagation for a given Bayesian network
    *
    * @param b a <code>Bnet</code>.
    */
   
   public VariableElimination(Bnet b) {
      
      network = b;
      crono=new Crono();
      observations = new Evidence();
      
   }
   
      
   /**
    * Program for performing experiments from the command line.
    * The command line arguments are as follows:
    * <ol>
    * <li> Input file: the network.
    * <li> Output file.
    * <li> Evidence file.
    * </ol>
    * If the evidence file is omitted, then no evidences are
    * considered.
    */
   
   public static void main(String args[]) throws ParseException, IOException {
      
      Network b;
      Evidence e;
      FileInputStream  evidenceFile;
      VariableElimination ve;
      String base;
      int i;
      
      if (args.length < 2)
         System.out.println("Too few arguments. Arguments are: ElviraFile OutputFile EvidenceFile");
      else {
         // networkFile = new FileInputStream(args[0]);
         // b = new Bnet(networkFile);
         b=Network.read(args[0]);
         
         if (args.length == 3) {
            evidenceFile = new FileInputStream(args[2]);
            e = new Evidence(evidenceFile,b.getNodeList());
         }
         else
            e = new Evidence();
         
         ve = new VariableElimination((Bnet)b,e);
         ve.obtainInterest();
         
         // Compose the name for the file with the statistics
        
         if (ve.generateStatistics == true){ 
            base=args[0].substring(0,args[0].lastIndexOf('.'));
            base=base.concat("_VariableElimination_data");
            ve.statistics.setFileName(base);
         }
         ve.propagate(args[1]);
         
      }
   }
   
   /**
    * Method to return the list of relations stored as currentRelations
    * @return <code>RelationList</code> relations in currentRelations
    */
   
   public RelationList getCurrentRelations(){
      return currentRelations;
   }

   /**
    * Method for getting the number of nodes of structure required for
    * storing probabilities and preferences. This method must be overriden
    * in classes derived from this one
    */
   public  long getNumberOfNodes(){
      return 0;
   }

   /**
    * Method for getting the number of nodes of structure required for
    * storing probabilities and preferences. This method must be overriden
    * in classes derived from this one
    */
   public  long getNumberOfLeaves(){
      return 0;
   }
   
   /**
    * Method to get the currentRelations data member
    * @param <code>Vector</code> list of relations
    */
   
   public void setCurrentRelations(Vector relations){
      RelationList rels=new RelationList();
      int i;
      
      for(i=0; i < relations.size(); i++){
         rels.insertRelation(((Relation)relations.elementAt(i)).copy());
      }
      
      currentRelations=rels;
   }
   
   /**
    * Combines two Potentials. This method can be overloaded in subclases
    * for special requirements.
    * @param pot1 the first <code>Potential</code>.
    * @param pot2 the second <code>Potential</code>.
    */
   
   public Potential combine(Potential pot1, Potential pot2) {
      
      Potential potaux;
      
      potaux = pot1.combine(pot2);
      
      return potaux;
   }
   
   
   /**
    * Divide pot1 by pot2. This method can be overloaded in subclases
    * for special requirements.
    * @param pot1 the first <code>Potential</code>.
    * @param pot2 the second <code>Potential</code>.
    */
   
   public Potential divide(Potential pot1, Potential pot2) {
      
      Potential potaux;
      potaux = pot1.divide(pot2);
      
      return potaux;
   }
   
   /**
    * Sum pot1 with pot2. This method can be overloaded in subclases
    * for special requirements.
    * @param pot1 the first <code>Potential</code>.
    * @param pot2 the second <code>Potential</code>.
    */
   
   public Potential addition(Potential pot1, Potential pot2) {
      
      Potential potaux;
      potaux = pot1.addition(pot2);
      
      return potaux;
   }
   
   /**
    * Removes the argument variable summing up over all its values. This
    * method can be overloaded in subclases for special requirements.
    * @param pot a <code>Potential</code> over which the operation will
    * be carried out.
    * @param var a <code>Node</code> variable to be removed.
    * @return a new <code>Potential</code> with the result of the deletion.
    */
   
   public Potential addVariable(Potential pot, Node var) {
      return pot.addVariable(var);
   }
   
   
   /**
    * Transforms the <code>Potential</code> obtained asode>Potential</code> obtained asvariable (<code>FiniteStates</code>). This
    * method can be overloaded in subclases for special requirements.
    * Right now, this method returns the argument itself.
    *
    * @param pot the <code>Potential</code> to transform.
    */
   
   public Potential transformAfterAdding(Potential pot) {
      return pot;
   }
   
   
   /**
    * Transforms the <code>Potential</code> obtained as a result of eliminating
    * one variable (<code>FiniteStates</code>). This
    * method can be overloaded in subclases for special requirements.
    * Right now, this method returns the argument itself.
    *
    * @param pot the <code>Potential</code> to transform.
    */
   
   public Potential transformAfterEliminating(Potential pot) {
      return pot;
   }
   
   /**
    * Transform an utility potential, prunning the lower
    * values
    * @param <code>Potential</code> the potential to transform
    */
   
   public Potential transformAfterOperation(Potential pot, boolean flag){
      return pot;
   }
   
   /**
    * Sets the maximum size of a potential.
    * @param n the size
    */
   
   public void setLimitSize(int n) {
      limitSize = n;
   }
   

   /**
    * Computes the posterior distributions.
    * There will be a posterior distribution for each
    * variable of interest.
    * Posterior distributions are stored in <code>results</code>.
    * Note that observed variables are not included in the deletion
    * sequence.
    */
   
   public void getPosteriorDistributions() {
      
      Node node;
      int i, s;
      
      if (generateStatistics == true){ 
         // Note down the data about the beginning of the evaluation
         statistics.addSize(getNumberOfLeaves());
         statistics.addNumberNodes(getNumberOfNodes());
      }

      s = interest.size();
      
      for (i=0 ; i<s ; i++) {
         node = interest.elementAt(i);
         
         if ( !observations.isObserved(node) ) {
            //     System.out.println("Propagating for variable "+x.getName());
            // System.out.println(s-i+" variables remaining");
            getPosteriorDistributionOf(node);

            // At the end store the size after removing the var
            if (generateStatistics == true){ 
              // Note down the data about the beginning of the evaluation
              statistics.addSize(getNumberOfLeaves());
              statistics.addNumberNodes(getNumberOfNodes());
            }
         }
      }
   }
   
   
   /**
    * Gets the posterior distribution of a given variable and
    * stores it in <code>results</code>.
    * @param v a <code>FiniteStates</code> whose posterior distribution will
    * be computed.
    */
   
   public void getPosteriorDistributionOf(Node v) {
      
      NodeList notRemoved;
      //FiniteStates  y;
      Node x;
      Relation r;
      RelationList rLtemp;
      Potential pot;
      PotentialTable table;
      PairTable pt;
      int i, j, k, p, pos, s;
 
      // Create a NodeList to remain the variables not removed yet 
      notRemoved = new NodeList();

      // The pair table will contain the variables to remove and its
      // relations
      pt = new PairTable();
      
      // s will contain the set of nodes from the network
      s = network.getNodeList().size();
      
      // Consider the nodes one by one
      for (i=0 ; i<s ; i++) {
         // A node will be included in the list of nodes to be removed if
         // it was not observed and is not the interest variable. It
         // will be included in the PairTable too
         x = network.getNodeList().elementAt(i);
         if ((!observations.isObserved(x)) && (!x.equals(v))) {
            notRemoved.insertNode(x);
            pt.addElement(x);
         }
      }
      
      // Get the relations in the network that may affect the
      // distribution of the interest variable
      currentRelations = getInitialRelations(v);
     // System.out.println("Initial Relationships " + currentRelations.size());
      
      /* Now restrict the valuations to the obervations */
      if (observations.size() > 0)
         currentRelations.restrictToObservations(observations);
      
      // The relations to use will be stored in the PairTable
      for (i=0 ; i<currentRelations.size() ; i++)
         pt.addRelation(currentRelations.elementAt(i));
      
      // While there are nodes to remove
      for (i=notRemoved.size() ; i>0 ; i--) {
         // Get the next variable to remove
         x = pt.nextToRemove();
          
       //  System.out.println("Remove variable " + x.toString());

         // This node will be deleted from notRemoved
         notRemoved.removeNode(x);

         // Also from the PairTable
         pt.removeVariable(x);

         // Get the relations where x takes part and remove
         // these relations from currentRelations
         rLtemp = currentRelations.getRelationsOfAndRemove(x);
         
         // The values for all these relations must be combined
         if (rLtemp.size() > 0) {
            // Get the first relation
            r = rLtemp.elementAt(0);
            pt.removeRelation(r);
            pot = r.getValues();

            for (j=1 ; j<rLtemp.size() ; j++) {
               // Get another relation
               r = rLtemp.elementAt(j);
               pt.removeRelation(r);
               pot = combine(pot,r.getValues());
            }
          
            // when all the potentials related to x were combined,
            // form a new relation. Before that, remove x by summation 
            pot = addVariable(pot,x);

            // Now the potential is transformed. This method will be
            // overriden by the clases derived from this
            pot = transformAfterAdding(pot);
            
            // Create a new relation to store the results of the elimination
            // of x
            r = new Relation();
           
            // Set the kind for the final relation 
            r.setKind(Relation.POTENTIAL);
            r.getVariables().setNodes((Vector)pot.getVariables().clone());
            r.setValues(pot);
         //   System.out.println("Adding potential");
        //    pot.print();
         //   System.out.println("Affter removing  " + x.toString());
            currentRelations.insertRelation(r);
            pt.addRelation(r);
         }
      }
      
      /* After this, currentRelations must only contain relations
         for variable v or none variable
       */
    if (probEvidence) {rLtemp = currentRelations;} 
    else {rLtemp = currentRelations.getRelationsOf(v);}
     
      
     // rLtemp = (RelationList) currentRelations;
      // All of these relations must be combined  to get a final result 
      r = rLtemp.elementAt(0);
      pt.removeRelation(r);
   
      // Get the potential for this relation
      pot = r.getValues();

      
      // Consider the rest of relations containing the interest variable
      for (j=1 ; j<rLtemp.size() ; j++) {
         // Get another relation
         r = rLtemp.elementAt(j);
         pt.removeRelation(r);

         pot = combine(pot,r.getValues());
         //pot.normalize();
      }

      // Transform the potential at the end. This method can be overriden by
      // the classes derived from this 
      pot = transformAfterEliminating(pot);
      
     
      // Store the results 
      if (pot.getClass() == PotentialTree.class) {
         table = new PotentialTable((PotentialTree)pot);
         results.addElement(table);
      }
      else
         results.addElement(pot);
   }
   
   
   
   /**
    * Gets the posterior distribution of a given set of variables and
    * stores it in <code>results</code>.
    * @param interest a <code>NodeList</code> whose posterior distribution will
    * be computed.
    * @param conf a <code>Configuration</code> that is added to the list of observations to conform the evidence
 * @throws InvalidEditException 
    */
   
   public PotentialTable getPosteriorDistributionOf(NodeList interest,Configuration conf) throws InvalidEditException {
      
      NodeList notRemoved;
      //FiniteStates  y;
      Node x;
      Relation r;
      RelationList rLtemp;
      Potential pot;
      PotentialTable table;
      PairTable pt;
      int i, j, k, p, pos, s;
      NodeList nodesRequired;
      RelationList relationsNodesRequired;
      RelationList instantiatedProbRels;
      PotentialTable posterior;
      RelationList relationsID;
      
      
      
      nodesRequired = getNodesRequiredForComputingPosteriorDistributionOf(interest,conf);
      
      relationsID = network.getInitialRelations();
      relationsNodesRequired = relationsID.getRelationsFirstVariableOf(nodesRequired);
      
      instantiatedProbRels = instantiateRelations(relationsNodesRequired,(observations!=null)?observations:new Configuration());
      instantiatedProbRels = instantiateRelations(instantiatedProbRels,(conf!=null)?conf:new Configuration());
      
      posterior = calculatePosteriorDistributionUsingRelations(instantiatedProbRels,interest);
           
     return posterior;
   }
   
	/**
	 * It instantiate a list of relations with the new configuration
	 * It uses the atrribute 'instantiations'
	 * The relations of the parameter 'relations' are changed if 'nodeVar' appears in them
	 * @param relations A list of the relations after the instantiation
	 */
private static RelationList instantiateRelations(RelationList relations,Configuration instantiations) {
		
			Relation auxNewRel;
			RelationList newRelations;
			Relation auxRel;
			
			ArrayList<Relation> auxRelations;
			
			newRelations = new RelationList();
			
			
				
			//Restrict the relations where the new variable appears
			//The relations where it doesn't appear don't change,
			//but the rest do.
			for (int i=0;i<relations.size();i++){
				auxRel = relations.elementAt(i);
					//Add the new relation restricted to the new configuration of variables
					auxNewRel = auxRel.copy();
					auxNewRel = auxNewRel.restrict(instantiations);
					newRelations.insertRelation(auxNewRel);
				
			}
			
			return newRelations;
			
			
		}

/**
	 * It calculates the conditioned probabilities with the variable elimination algorithm
	 * @param node
	 * @param probRels
	 * @return
	 */
	public static PotentialTable calculatePosteriorDistributionUsingRelations(RelationList probRels,NodeList interest){
		RelationList relsOfElim;
		NodeList varsToElim;
		RelationList auxRelsOfElim;
		Potential finalPot;
		Relation finalRel;
		Potential pot;
		
		//relsOfElim = probRels.getRelationsOf(node);
		//We need all the probabilistic relations to calculate the conditioned probabilities.
		relsOfElim = new RelationList();
		for (int i=0;i<probRels.size();i++){
			relsOfElim.insertRelation(probRels.elementAt(i));
		}
		
		
		
		
		//Quite variables of interest from the list of variables to eliminate
		varsToElim = relsOfElim.getVariables();
		for (int iInter=0;iInter<interest.size();iInter++){
			varsToElim.removeNode(interest.elementAt(iInter));
		}
		
		
		//We eliminate the rest of variables of the relations where varNode apeears
		for(int i=0;i<varsToElim.size();i++){
			Node nodeToElim = varsToElim.elementAt(i); 
			auxRelsOfElim =	relsOfElim.getRelationsOfAndRemove(nodeToElim);
			
			//We eliminate the variable nodeToElim of the relations where it appears			
			pot = auxRelsOfElim.elementAt(0).getValues();
			for(int j=1;j<auxRelsOfElim.size();j++){
				pot = pot.combine(auxRelsOfElim.elementAt(j).getValues());
			}
			//Sum over nodeToElim
			pot = pot.addVariable(nodeToElim);
			
	        // Create a new relation to store the results of the elimination of nodeToElim
           Relation newRel = new Relation();
           // Set the kind for the final relation 
           newRel.setKind(Relation.POTENTIAL);
           newRel.getVariables().setNodes((Vector)pot.getVariables().clone());
           newRel.setValues(pot);
     
           //Add the new relation to the remaining relations
           relsOfElim.insertRelation(newRel);
		}
		
		//We combine the rest of relations, which only depend on node
		//and we normalize them		
		pot = relsOfElim.elementAt(0).getValues();
		for(int j=1;j<relsOfElim.size();j++){
			pot = pot.combine(relsOfElim.elementAt(j).getValues());
		}
		finalPot = pot;
		finalPot.normalize();
	
		return (PotentialTable) finalPot;
	}


   
   
   /**
 * @param interest
 * @param conf
 * @return A list of nodes whose relations are required for computing the posterior distribution of a set of variables
 * @throws InvalidEditException
 */
private NodeList getNodesRequiredForComputingPosteriorDistributionOf(
		NodeList interest, Configuration conf) throws InvalidEditException {
	// TODO Auto-generated method stub
	   ConcurrentLinkedQueue<Node> candidatesToRemove;
	   ArrayList<Node> withoutChildren;
	   Node auxCandidate;
	   Graph auxGraph;
	   NodeList observ;
	   NodeList nodesConf;
	   NodeList parentsOfCandidate;
	   
	   auxGraph = network.duplicate();
	   NodeList nodesOfObservAndInteresntAndConf;
	   
	   nodesOfObservAndInteresntAndConf = new NodeList();
	   observ = new NodeList();
	   nodesConf = new NodeList();
	   
	   nodesConf.setNodes(((observations!=null)?observations.getVariables():new Vector()));
	   nodesConf.setNodes(((conf!=null)?conf.getVariables():new Vector()));
	   nodesOfObservAndInteresntAndConf.merge(interest);
	   nodesOfObservAndInteresntAndConf.merge(observ);
	   nodesOfObservAndInteresntAndConf.merge(nodesConf);
	   
	   //We start with nodes without children
	   candidatesToRemove = new ConcurrentLinkedQueue<Node>();
	   withoutChildren = getNodesWithoutChildren(auxGraph);
	   for (Node auxNode:withoutChildren){
		   candidatesToRemove.add(auxNode);
	   }
	   
	   while (!candidatesToRemove.isEmpty()){
		   auxCandidate = candidatesToRemove.remove();
		   //Only the nodes that are not observed, nor of
		   if (nodesOfObservAndInteresntAndConf.getId(auxCandidate)==-1){
			   
			   parentsOfCandidate = auxCandidate.getParentNodes();
			   
			   for (int iParent=0;iParent<parentsOfCandidate.size();iParent++){
				   Node auxParentOfCandidate;
				   
				   auxParentOfCandidate = parentsOfCandidate.elementAt(iParent);
				   if (auxParentOfCandidate.getChildren().size()==1){
					   candidatesToRemove.add(auxParentOfCandidate); 
				   }
				   auxGraph.removeLink(auxParentOfCandidate,auxCandidate);
			   }
			   auxGraph.removeNode(auxCandidate);
		   }
	   }
	   
	  return auxGraph.getNodeList();
	   
	   
	   
	 
}

private static ArrayList<Node> getNodesWithoutChildren(Graph net) {
	// TODO Auto-generated method stub
	ArrayList<Node> withoutChildren;
	NodeList nodes;
	Node auxNode;
	
	nodes = net.getNodeList();
	withoutChildren = new ArrayList<Node>();
	for (int i=0;i<nodes.size();i++){
		auxNode = nodes.elementAt(i);
		if (auxNode.getChildren().size()==0){
			withoutChildren.add(auxNode);
		}
	}
	return withoutChildren;
}

/**
    * Restricts a list of relations to the observations.
    * @param rl the <code>RelationList</code> to restrict.
    */
   
   public void restrictToObservations(RelationList rl) {
      
      Relation r;
      int i, s;
      
      s = rl.size();
      
      for (i=0 ; i<s ; i++) {
         r = rl.elementAt(i);
         r.setValues((r.getValues()).restrictVariable(observations));
         r.getVariables().setNodes(r.getValues().getVariables());
      }
   }
   
   
   /**
    * Carries out a propagation storing the results in <code>results</code>.
    */
   
   public void propagate() {

      if(network.getClass()==Bnet.class){
        // currentRelations=getInitialRelations();
         getPosteriorDistributions();
         normalizeResults();
      }
      else{
         System.out.print("Error in VariableElimination.propagate(): ");
         System.out.println("this propagation method is not implemented for "+network.getClass());
         System.exit(1);
      }
   }
   
   
   /**
    * Carries out a propagation saving the results in <code>OutputFile</code>.
    *
    * @param outputFile the file where the exact results will be
    *                   stored.
    */
   
   public void propagate(String outputFile) throws ParseException, IOException {
     
      propagate();
      saveResults(outputFile);
   }
      
   /**
    * Method to get a NodeList with all the nodes that have not
    * been observed
    * @return <code>NodeList</code> not observed nodes
    */
   
   public NodeList getNotObservedNodes() {
      NodeList notRemoved = new NodeList();
      Node x;
      int s,i;
      
      // Get the number of nodes in the network
      
      s = network.getNodeList().size();
      
      // For all of them, see if it is observed
      
      for (i=0 ; i<s ; i++) {
         x = network.getNodeList().elementAt(i);
         
         // If it is a CHANCE or DECISION NODE AND it is not observed,
         // insert it
         
        
         if(observations != null){
         try{
         if((x.getKindOfNode()!=Node.UTILITY) && (!observations.isObserved(x)))
            notRemoved.insertNode(x);
         }catch(Exception e) {
             System.out.println("");
         }
         }
      }
      
      // Return the list of not observed nodes
      
      return(notRemoved);
   }
   
   /**
    * Method to retrict the set of relations according to a set
    * of observations
    */
   
   public void restrictCurrentRelationsToObservations() {
     RelationList relationsToModify;

      // If the list contains elements, restrict to observations
      
      if (observations.size() > 0) {
         // Get the list of relations with non empty intersection given
         // a set of variables
         
         relationsToModify=currentRelations.getRelationsOf(observations);
         relationsToModify.restrictToObservations(observations);
      }
   }   
} // End of class
