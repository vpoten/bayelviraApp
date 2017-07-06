package elvira.inference.clustering;

import java.io.*;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.tools.FactorisationTools;

import elvira.potential.Potential;
import elvira.potential.PotentialTree;
import elvira.potential.ProbabilityTree;
import elvira.potential.ListPotential;

/**
 * Class <code>FactorisedSLP</code>.
 * Modifies the Simple Lazy Penniless algorithm to
 * take advantage of new factorisations of the potentials
 * involved in the operations.
 * @author Antonio Salmeron
 * @author Irene Martinez
 * @since 20/1/2003
 */


public class FactorisedSLP extends SimpleLazyPenniless {

/**
 * The parameters for factorisation
 */
FactorisationTools factorisationParam;


/**
 * Program for performing experiments from the command line.
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network.
 * <li> Output file where the results of the propagation
 * will be written.
 * <li> Output statistics file, where the error, computing time
 * and other statistics about each experiment will be stored.
 * <li> File with exact results. If no exact results are
 * available, use NORESULTS instead.
 * <li> Method to Join Potentials (0|1|2|3|4|5)
 * <li> A double; limit for pruning.
 * <li> A double; low limit for pruning.
 * <li> A double; limit sum for pruning.
 * <li> A value indicating the heuristic to JoinInCreatePots(0|1|2|3|4|5).
 * <li> The triangulation method (0|1|2).
 * <li> The Factorisation Method (0:split|1:fact|2:split&fact).
 * <li> The Phase in wich the Factorisation is applied (0:compil|1:propag|2:compil&propag) 
 * <li> The Approximation Method (0:aver|1:WeigAver|2:Chi|3:MSE|4:WMSE|5:KL|6:WP|7:Hel).
 * <li> The Method for calculate the divergence between trees (-1:none|1:Chi|2:NormChi|3:MSE|4:WMSE|5:KL|6:MAD|7:Hel) 
 * <li> The maximun error allowed between the nodes of two trees (applied only when the former parameter is -1)  (-1 for none) 
 * <li> The maximun error allowed between two trees in the divergence methods (-1 for none)
 * <li> Value between 0 and 1 indicating the percentage of proportional children that must been 
 *      reached before factorise the tree (1 for all proportional)
 * <li>  Maximun level that can be reached down in the tree when looking for the variable
 * <li> File with instantiations.
 * </ol>
 * The last argument can be omitted. In that case, it will
 * be considered that no observations are present.
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  FactorisedSLP propagation;
  int i, method, heurToJoin,  triangMethod,facMethod=2 , appMet, distMet, 
      numParam, faseF;
  double dNod,dTre, lp, llp, lsp, g, mse, mae, propCh,
     timePropagating, timeCompiling, lev;
  double[] errors;
  boolean exactCase=false;

  Date date, dateCompiling;
  FileWriter f;
  PrintWriter p;

  
  // ................ Checking the input parameters ...............//  
  
  
  numParam=18; // Minimum number of input parameters in command line

  if (args.length < numParam) {
    System.out.println("Too few arguments. Arguments are:");
    System.out.println("");
    System.out.println("ElviraFile OutputFile OutputErrorFile InputExactResultsFile");
    System.out.println("MethodToJoinPotentials(0|1|2|3|4|5) LimitForPruning LowLimitForPruning LimitSumForPruning");
    System.out.print("HeuristicToJoinInCreatePots(0|1|2|3|4|5) ");
    System.out.println("TriangulationMethod(0|1|2) ");
    System.out.println("FactorisationMethod(0:split|1:fact|2:split&fact) ");
    System.out.println("FactorisationPhase(0:compil|1:propag|2:compil&propag) ");
    System.out.println("ApproximationMethod(0:aver|1:WeigAver|2:Chi|3:MSE|4:WMSE|5:KL|6:WP|7:Hel) ");
    System.out.println("DistanceTreesMethod(-1:none|1:Chi|2:NormChi|3:MSE|4:WMSE|5:KL|6:MAD|7:Hel) ");
    System.out.println("FactorisationError_Nodes(-1 for none)  FactorisationError_Trees(-1 for none)  ProportChildren  maxLevel ");
    System.out.println("[EvidenceFile]");
  }
  else {

    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);

    if (args.length == numParam +1) {
      evidenceFile = new FileInputStream(args[numParam]);
      e = new Evidence(evidenceFile,b.getNodeList());
      System.out.println("Evidence file: "+args[numParam]);
    }
    else
      e = new Evidence();


    method = (Integer.valueOf(args[4])).intValue();
    lp = (Double.valueOf(args[5])).doubleValue();
    llp = (Double.valueOf(args[6])).doubleValue();
    lsp = (Double.valueOf(args[7])).doubleValue();
    heurToJoin = (Integer.valueOf(args[8])).intValue();
    triangMethod = (Integer.valueOf(args[9])).intValue();
    facMethod = (Integer.valueOf(args[10])).intValue();
    faseF= (Integer.valueOf(args[11])).intValue();
    appMet = (Integer.valueOf(args[12])).intValue();
    distMet = (Integer.valueOf(args[13])).intValue();
    dNod = (Double.valueOf(args[14])).doubleValue();
    dTre = (Double.valueOf(args[15])).doubleValue();
    propCh = (Double.valueOf(args[16])).doubleValue();
    lev = (Double.valueOf(args[17])).doubleValue();

    if(distMet<0) // only distance between nodes has been selected
    {    
      dTre=-1.0; // no distance between trees
       if(dNod<0){ // check for an invalid error
         System.out.println("->ERROR: No Divergence (Trees) method selected. Setting the FactorisationError_Nodes = 0");
         dNod=0.0;
       }
    }
    
    if(dTre<0.0 && dNod<0.0){
      System.out.println("->ERROR: any factorisation error has been selected");
      System.exit(0);
    }
    
    System.out.println("Method to join potentials: "+ method);
    System.out.println("Low limit: "+lp);
    System.out.println("Low limit for pruning: "+llp);
    System.out.println("Limit sum for pruning: "+lsp);
    System.out.println("Heuristic to JoinInCreatePots: "+heurToJoin);
    System.out.println("Triangulation method: "+triangMethod);

    if(dTre==0.0 || dNod==0.0)
    {
        exactCase=true;
        dNod=0.0;
        dTre=-1.0;  // none
        appMet= -1; // none
        distMet=-2; // none
    }
    
    System.out.println(FactorisationTools.printFactoriMethod(facMethod));
    if(facMethod>2){ //SimpleLazyPen.
        faseF=-1;
        System.out.println("Factorisation Phase: None");
    }
    else
        System.out.println("Factorisation Phase: "+faseF);
    
    System.out.println(FactorisationTools.printApproxMethod(appMet));
    System.out.println(FactorisationTools.printDivergenceMethod(distMet));
    System.out.println("Factorisation error (between nodes): "+dNod);
    if (exactCase)
       System.out.println("Factorisation error (between trees): None");
    else
       System.out.println("Factorisation error (between trees): "+dTre);
    System.out.println("Proportional Children: "+propCh);
    System.out.println("Max Factorisation level: "+lev);

    
   // ................ Start propagation ........................//   
    
    dateCompiling = new Date();
    timeCompiling = (double)dateCompiling.getTime();

    propagation = new FactorisedSLP(b,e,method,lp,llp,lsp, heurToJoin,triangMethod,
                                    dNod, dTre, propCh, lev, faseF,
                                    facMethod,appMet,distMet); 

    dateCompiling = new Date();
    timeCompiling = ((double)dateCompiling.getTime()-timeCompiling) / 1000;

    date = new Date();
    timePropagating = (double)date.getTime();

    propagation.propagate(args[1]);

    date = new Date();
    timePropagating = ((double)date.getTime()-timePropagating) / 1000;

    
   // ...........Compute erros and save the results ...............//

    f = new FileWriter(args[2]);
    p = new PrintWriter(f);

    p.println("Method to join potentials: "+ method);
    p.println("Low limit: "+lp);
    p.println("Low limit for pruning: "+llp);
    p.println("Limit sum for pruning: "+lsp);
    p.println("Heuristic to JoinInCreatePots: "+heurToJoin);
    p.println("Triangulation method: "+triangMethod);
    p.println(FactorisationTools.printFactoriMethod(facMethod));
    if(facMethod>2) //SimpleLazyPen.
        p.println("Factorisation Phase: None");
    else
        p.println("Factorisation Phase: "+faseF);
    
    p.println(FactorisationTools.printApproxMethod(appMet));
    p.println(FactorisationTools.printDivergenceMethod(distMet));
    p.println("Factorisation error (between nodes): "+dNod);
    if (exactCase)
       p.println("Factorisation error (between trees): None");
    else
       p.println("Factorisation error (between trees): "+dTre);
    p.println("Proportional Children: "+propCh);
    p.println("Factorisation level: "+lev);
    p.println("");

    p.println("\nTime compiling (secs) : "+timeCompiling);
    p.println("Time propagating (secs) : "+timePropagating);


    if (!args[3].equals("NORESULTS")) {
      System.out.println("Reading exact results");
      propagation.readExactResults(args[3]);
      System.out.println("Exact results read");

      System.out.println("Computing errors");
      errors = new double[2];
      propagation.computeError(errors);
      mae = propagation.computeMaxAbsoluteError();
      g = errors[0];
      mse = errors[1];

      propagation.computeKLError(errors);
      p.println("G : "+g);
      p.println("MSE : "+mse);
      p.println("Max absolute error : "+mae);
      p.println("KL error : "+errors[0]);
      p.println("Std. deviation of KL-error : "+errors[1]);
    }
    
    propagation.binTree.calculateStatistics();
    propagation.binTree.saveStatistics(p);
    
    
    // ........... Print and save the factorisation statistics ...............//
    
    if(propagation.factorisationParam.sizesPot) //size of potentials
    { 
       Vector statSizeVec = propagation.factorisationParam.getClassStatistic(0);   
       if(statSizeVec!=null){
           p.println(".....These are the statistics about the sizes:.....");
           p.println("Mean :" +((Double)statSizeVec.elementAt(0)).doubleValue());
           p.println("Standard Deviation : "+((Double)statSizeVec.elementAt(1)).doubleValue());
           p.println("Max : "+((Double)statSizeVec.elementAt(2)).doubleValue());
           p.println("Min : "+((Double)statSizeVec.elementAt(3)).doubleValue());

           System.out.println(".....These are the statistics about the sizes:.....");
           propagation.factorisationParam.printStatistics(statSizeVec);
       }
    }
     
    if(facMethod>0){// Size of approximations  
       Vector statSizeVec= propagation.factorisationParam.getClassStatistic(1);
       if(statSizeVec!=null){
           p.println(".....These are the statistics about the probability factors in the approximations: .......");
           p.print("Number of approximations :" +propagation.factorisationParam.vecDistApproxim.size());
           p.println(" (" +propagation.factorisationParam.getCounterFCompil() +" in compilation)");
           p.println("Mean (of max of probability factors) :" +((Double)statSizeVec.elementAt(0)).doubleValue());
           p.println("Standard Deviation : "+((Double)statSizeVec.elementAt(1)).doubleValue());
           p.println("Max : "+((Double)statSizeVec.elementAt(2)).doubleValue());
           p.println("Min : "+((Double)statSizeVec.elementAt(3)).doubleValue());

           System.out.println(".....These are the statistics about the probability factors in the approximations: ......");
           System.out.print("Number of approximations :" +propagation.factorisationParam.vecDistApproxim.size());
           System.out.println(" (" +propagation.factorisationParam.getCounterFCompil() +" in compilation)");
           propagation.factorisationParam.printStatistics(statSizeVec);
       }
       else{
           p.println("Number of approximations : NONE");
           System.out.println(".....No factorisation made ......");
       }
    } 
    if(facMethod!=1){
           p.println("Number of split operations : "+propagation.factorisationParam.getNumSplit());
           System.out.println(".....Number of split operations: "+propagation.factorisationParam.getNumSplit());
    }
        
 
    if (propagation.doubleCache) {
      p.println("\n\n Size of cache 1: "+propagation.cache1.size());
      p.println("Size of cache 2: "+propagation.cache2.size());
      p.println("Size of cache marg: "+propagation.cache1M.size());
      p.println("Size of cache marg: "+propagation.cache2M.size());
    }
    f.close();

    System.out.println("Done.");
  }
}



/**
 * Creates a new propagation.
 *
 * @param b a belief network.
 * @param e an evidence.
 * @param method the method to join potentials in <code>ListPotential</code>
 * @see whenJoinPotentials
 * @param lp the limit for pruning.
 * @param llp the lowLimit for pruning;
 * @param lsp the limit sum for pruning.
 * @param ls the maximum sizes for potentials.
 * @param heuristic the heuristic to select two <code>Potential</code>s
 * in createPotential(double limitBound,int heuristic)
 * @param triangMethod indicates the triangulation to carry out.
 * <ol>
 * <li> 0 is for considering evidence during the triangulation.
 * <li> 1 is for considering evidence and directly remove relations
 * that are conditional distributions when the conditioned variable
 * is removed.
 * <li> 2 is for not considering evidence.
 * </ol>
 * @param dNod factorisation error between nodes.
 * @param dTre factorisation error between trees.
 * @param propChi number of proportional childs for factorise.
 * @param lev max level (down) for look for the variable.
 * @param fase the phase in wich factorise (compilation or propagation).
 * @param mfact method to factorise.
 * @param appMet method for obtain the proportionality factor
 * @param distMet divergence method (between trees).
 */

public FactorisedSLP(Bnet b, Evidence e, int methodJPot, double lp,
             double llp, double lsp,  int heuristic, int triangMethod,
                     double dNod, double dTre,double propChi, double lev, int fase,
                     int mFact,int appMet, int distMet) {

  Triangulation triang;
  RelationList rel1, rel2, ir, irTree;
  NodeList numeration = new NodeList();
  int i, j;
  ListPotential relPot;
  Potential newPot;
  Relation newRel, rel;
  Vector newList;

  setHeuristicToJoin(heuristic);

  setSortAndBound(sortAndBound);
  setLimitForPruning(lp);
  setLowLimitForPruning(llp);
  setLimitSumForPruning(lsp);

  /* double fNod, double fTr, int mFact, int appMethod,
                          int divMethod, float porcentProp, int level)*/

  factorisationParam = new FactorisationTools(dNod,dTre,mFact,appMet, distMet,
                           propChi,lev,fase);
  
  setFirstTour(true);

  setUseCache(false);
  setSortAndBound(false);

  observations = e;
  network = b;
  setWhenJoinPotentials(methodJPot);
  positions = new Hashtable();

  if (triangMethod == 2)
    binTree = new JoinTree(b);
  else
    binTree = new JoinTree(b,e,triangMethod);

  irTree = getInitialRelations();
  irTree.restrictToObservations(observations);

  factorisationParam.setcompilPhase(); // compil = true
  
  for (i=0 ; i<irTree.size() ; i++){
     rel= irTree.elementAt(i);
     relPot= (ListPotential)rel.getValues() ;
     relPot.limitBound(lowLimitForPruning);
      
     if(fase==0 || fase==2 ){
        newPot= relPot.factorisePotentialAllVbles(factorisationParam);
        
        if(newPot!=null)
            rel.setValues(newPot); 
     }
  }
  
  factorisationParam.setCounterFCompil(); //save the size of the vector
  factorisationParam.setcompilPhase(); // compil =false
  
  setLimitForPruning(0.0);    // set to 0 the parameters for pruning
  setLowLimitForPruning(0.0);
  setLimitSumForPruning(0.0);
  
  ir = irTree;

  marginalCliques = binTree.Leaves(ir);

  binTree.binTree();

  binTree.setLabels();
}

/**
 * Sends a message from a node to another one.
 * The message is computed by combining all the messages inwards
 * the sender except that one comming from the recipient. The output
 * <code>Potential</code> of every operation is approximated with
 * <code>Potential.limitBound(double limit)</code>.
 * It is required that the nodes in the tree be labeled. Use method
 * <code>setLabels</code> if necessary. The marginalisation operations
 * are carried out factorising the potentials beforehand.
 *
 * @param sender the node that sends the message.
 * @param recipient the node that receives the message.
 */

public void sendMessage(NodeJoinTree sender, NodeJoinTree recipient) {

  NeighbourTreeList list, auxList;
  NeighbourTree nt;
  Potential pmt, aux, pot;
  Relation rel, outwards, inwards;
  int i, label,  listSize;
  Vector separator, v;
  NodeList vars;
  ListPotential listPot1, listPot2;

  inwards = new Relation();
  outwards = new Relation();
  separator = new Vector();

  // Now combine with the potential in the node.
  aux = sender.getNodeRelation().getValues();
  pot = aux;

  list = sender.getNeighbourList();

  for (i=0 ; i<list.size() ; i++) {
    nt = list.elementAt(i);
    label = nt.getNeighbour().getLabel();
    rel = nt.getMessage();

    // Combine the messages coming from the other neighbours

    if (label != recipient.getLabel()) {
      pot = pot.combine(rel.getOtherValues());
    }
    else {
      outwards = rel;
      separator =  rel.getVariables().getNodes();
      auxList = recipient.getNeighbourList();
      inwards = auxList.getMessage(sender);
    }
  }

  // Decide which potentials in the list will be joined
  if ((whenJoinPotentials == 0) || (whenJoinPotentials == 5)) {
    // Never join potentials.
    // Do nothing
  }
  else if ((whenJoinPotentials == 1)||(whenJoinPotentials == 2)) {
    // We combine in the list the potentials with variables that will
    // be eliminated afterwards.
    vars = varsToCombine(sender,recipient,whenJoinPotentials);
    for (i=0 ; i<vars.size() ; i++) {
      if(doubleCache){
        ((ListPotential)pot).combinePotentialsOf(vars.elementAt(i),
              limitForPruning,limitSumForPruning,cache1,cache2,
              firstTour,heuristicToJoin);
      }
      else{
        ((ListPotential)pot).combinePotentialsOf(vars.elementAt(i),
                           limitForPruning,
                           limitSumForPruning);
      }
    }
  }
  else if (whenJoinPotentials == 3) {
    // We combine all the potentials in the list
    Potential potAux;
    if(doubleCache)
      potAux = ((ListPotential)pot).createPotential(limitForPruning,
               limitSumForPruning,cache1,cache2,firstTour,heuristicToJoin);
    else
      potAux = ((ListPotential)pot).createPotential(limitForPruning,
                          limitSumForPruning);
    if(potAux!=null){ // potAux is null when pot contains no potential
      pot = new ListPotential(potAux);
    }
  }
  else if (whenJoinPotentials == 4) {
    if(!doubleCache)
       ((ListPotential)pot).joinPotentials(limitForPruning,limitSumForPruning);
    else {
      System.out.println("ERROR: whenJoinPotentials == 4 cannot be used with cache");
      System.exit(1);
    }
  }
  else {
    System.out.println("Error in SimpleLazyPenniless.sendMessage(): "+
               "wrong value for whenJoinPotentials="+
               whenJoinPotentials);
    System.exit(1);
  }

  if (whenJoinPotentials == 5) {
    pot = ((ListPotential)pot).marginalizePotential(separator,
                            limitForPruning,
                            limitSumForPruning);
  }
  else {
    if(((ListPotential)pot).getListSize()>0){
      if (doubleCache)
         pot = ((ListPotential)pot).marginalizePotential(separator,
                              limitForPruning,
                              limitSumForPruning,
                              cache1,cache2,
                              cache1M,cache2M,
                              firstTour,
                              heuristicToJoin);
      else{
         if(factorisationParam.getFacPhase()==0) //only factorisation in compilation
             factorisationParam.setFactMethod(3); // skip the factorisation
         
         pot = ((ListPotential)pot).factorMarginalizePotential(separator,
                                limitForPruning,
                                heuristicToJoin,
                                limitSumForPruning,
                                factorisationParam);
      /*
        pot = ((ListPotential)pot).marginalizePotential(separator,
						      limitForPruning,
						      heuristicToJoin,
       					      limitSumForPruning); */ 
      }
    }
  }

  // Now perform sortAndBound if requested by the user.

  if (sortAndBound) {
    listPot2 = (ListPotential)pot;
    listPot1 = new ListPotential();
    listPot1.setVariables(listPot2.getVariables());

    listSize = listPot2.getListSize();

    for (i=0 ; i<listSize ; i++) {
      listPot1.insertPotential(listPot2.getPotentialAt(i).sortAndBound(maximumSize));
    }

    pot = listPot1;
  }

  // Now update the messages in the join tree.

  outwards.setValues(pot);
  inwards.setOtherValues(pot);

}


} // End of class
