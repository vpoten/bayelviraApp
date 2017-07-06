/* FactorisedISDynamic.java */

package elvira.inference.approximate;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.tools.FactorisationTools;


/**
 * Class ImportanceSamplingDynamic.
 * Implements the importance sampling method of propagation
 * based on approximate node deletion, using Probability Trees.
 * Trees are updated during the simulation, try to capture the target
 * distribution.
 *
 * @author Irene Martinez (irene@ual.es)
 * @author Antonio.Salmeron@ual.es
 * @since 8/5/2006
 */

public class FactorisedISDynamic extends ImportanceSamplingDynamic {
    
    
    /**
     * The parameters for factorisation
     */
    FactorisationTools factorisationParam;
    
    
    
    /**
     * Program for performing experiments.
     * The arguments are as follows.
     * <ol>
     * <li> A double; limit for prunning.
     * <li> A double. Limit threshold for updating.
     * <li> An integer. Maximum size of a potential.
     * <li> An integer. Number of simulation steps.
     * <li> An integer. Number of experiments.
     * <li> Input file: the network.
     * <li> Output error file, where the error and computing time
     *      of each experiment will be stored.
     * <li> File with exact results.
     *................................................................
     * <li> The Factorisation Method (0:split|1:fact|2:split&fact).
     * <li> The Phase in wich the Factorisation is applied (0:compil|1:propag|2:compil&propag)
     * <li> The Approximation Method (0:aver|1:WeigAver|2:Chi|3:MSE|4:WMSE|5:KL|6:WP|7:Hel).
     * <li> The Method for calculate the divergence between trees (-1:none|1:Chi|2:NormChi|3:MSE|4:WMSE|5:KL|6:MAD|7:Hel)
     * <li> The maximun error allowed between the nodes of two trees (applied only when the former parameter is -1)  (-1 for none)
     * <li> The maximun error allowed between two trees in the divergence methods (-1 for none)
     * <li> Value between 0 and 1 indicating the percentage of proportional children that must been
     *      reached before factorise the tree (1 for all proportional)
     * <li>  Maximun level that can be reached down in the tree when looking for the variable
     * ..............................................................
     * <li> File with instantiations.
     * </ol>
     * The last argument can be omitted. In that case, it will
     * be considered that no observations are present.
     *
     *******************************************************************************
     *
     * Instead, it can be used to obtain the results of the propagation
     * rather than performing experiments. In this case, arguments
     * are:
     * <ol>
     * <li> Input file: the network.
     * <li> Output file (results of the propagation).
     * <li> A double; limit for prunning.
     * <li> A double. Limit threshold for updating.
     * <li> An integer. Maximum size of a potential. A value of -1
     *      means that no maximum size is considered.
     * <li> An integer. Number of simulation steps.
     *................................................................
     * <li> The Factorisation Method (0:split|1:fact|2:split&fact).
     * <li> The Phase in wich the Factorisation is applied (0:compil|1:propag|2:compil&propag)
     * <li> The Approximation Method (0:aver|1:WeigAver|2:Chi|3:MSE|4:WMSE|5:KL|6:WP|7:Hel).
     * <li> The Method for calculate the divergence between trees (-1:none|1:Chi|2:NormChi|3:MSE|4:WMSE|5:KL|6:MAD|7:Hel)
     * <li> The maximun error allowed between the nodes of two trees (applied only when the former parameter is -1)  (-1 for none)
     * <li> The maximun error allowed between two trees in the divergence methods (-1 for none)
     * <li> Value between 0 and 1 indicating the percentage of proportional children that must been
     *      reached before factorise the tree (1 for all proportional)
     * <li>  Maximun level that can be reached down in the tree when looking for the variable
     * ..............................................................
     * <li> File with instantiations.
     * </ol>
     */
    
    public static void main(String args[]) throws ParseException, IOException {
        
        Bnet b;
        Evidence e;
        FileInputStream networkFile, evidenceFile;
        FactorisedISDynamic propagation;
        int i, ss, nruns, ls;
        double lp, lu;
        int  method,facMethod, appMet, distMet, num, faseF;
        double dNod,dTre, propCh, lev;
        
        int numFacParam= 8; // Number of input parameters for Factorisation
        
        if (args.length < 6 + numFacParam){
            
            
            System.out.println("Wrong number of arguments. Arguments are:");
            System.out.println("");
            
            System.out.println("........ PERFORM EXPERIMENTS .......");
            System.out.println("LimitForPruning LimitForUpdating MaxPotentialSize  num_SimulationSteps  num_Experiments");
            System.out.println("ElviraFile  OutputErrorFile  InputExactResultsFile");
            System.out.println("FactorisationMethod(0:split|1:fact|2:split&fact) ");
            System.out.println("FactorisationPhase(0:compil|1:propag|2:compil&propag) ");
            System.out.println("ApproximationMethod(0:aver|1:WeigAver|2:Chi|3:MSE|4:WMSE|5:KL|6:WP|7:Hel) ");
            System.out.println("DistanceTreesMethod(-1:none|1:Chi|2:NormChi|3:MSE|4:WMSE|5:KL|6:MAD|7:Hel) ");
            System.out.println("FactorisationError_Nodes(-1 for none)  FactorisationError_Trees(-1 for none)  ProportChildren  maxLevel ");
            System.out.println("[EvidenceFile]");
            
            System.out.println("");
            System.out.println("........ PROPAGATION .......");
            System.out.println("ElviraFile  OutputFile ");
            System.out.println("LimitForPruning LimitForUpdating MaxPotentialSize(-1 for none)  num_SimulationSteps ");
            System.out.println("FactorisationMethod(0:split|1:fact|2:split&fact) ");
            System.out.println("FactorisationPhase(0:compil|1:propag|2:compil&propag) ");
            System.out.println("ApproximationMethod(0:aver|1:WeigAver|2:Chi|3:MSE|4:WMSE|5:KL|6:WP|7:Hel) ");
            System.out.println("DistanceTreesMethod(-1:none|1:Chi|2:NormChi|3:MSE|4:WMSE|5:KL|6:MAD|7:Hel) ");
            System.out.println("FactorisationError_Nodes(-1 for none)  FactorisationError_Trees(-1 for none)  ProportChildren  maxLevel ");
            System.out.println("[EvidenceFile]");
        }
        
        
        else {
            
            if (args.length < 8 + numFacParam) { // PROPAGATE option
                networkFile = new FileInputStream(args[0]);
                b = new Bnet(networkFile);
                
                if (args.length == 7 + numFacParam) {
                    evidenceFile= new FileInputStream(args[6+numFacParam]);
                    e = new Evidence(evidenceFile,b.getNodeList());
                }
                else
                    e = new Evidence();
                
                lp = (Double.valueOf(args[2])).doubleValue();
                
                lu = (Double.valueOf(args[3])).doubleValue();
                
                ls = (Integer.valueOf(args[4])).intValue();
                
                ss = (Integer.valueOf(args[5])).intValue();
                
                // Factorisation parameters
                num=6; // position of the first factorisation parameter
                
                facMethod = (Integer.valueOf(args[num])).intValue();
                faseF = (Integer.valueOf(args[num+1])).intValue();
                appMet= (Integer.valueOf(args[num+2])).intValue();
                distMet = (Integer.valueOf(args[num+3])).intValue();
                dNod = (Double.valueOf(args[num+4])).doubleValue();
                dTre = (Double.valueOf(args[num+5])).doubleValue();
                propCh = (Double.valueOf(args[num+6])).doubleValue();
                lev = (Double.valueOf(args[num+7])).doubleValue();
                
                
                propagation = new FactorisedISDynamic(b,e,lp,lu,ls,ss,1,
                dNod, dTre, propCh, lev, faseF,facMethod,appMet,distMet);
                
                propagation.propagate();
                
                propagation.saveResults(args[1]);
                
            } // end Propagation
            
            else {  //EXPERIMENT option
                
                networkFile = new FileInputStream(args[5]);
                b = new Bnet(networkFile);
                
                if (args.length == 9 + numFacParam) {
                    evidenceFile= new FileInputStream(args[8+numFacParam]);
                    e = new Evidence(evidenceFile,b.getNodeList());
                }
                else
                    e = new Evidence();
                
                lp = (Double.valueOf(args[0])).doubleValue();
                lu = (Double.valueOf(args[1])).doubleValue();
                ls = (Integer.valueOf(args[2])).intValue();
                
                ss = (Integer.valueOf(args[3])).intValue();
                
                nruns = (Integer.valueOf(args[4])).intValue();
                
                // Factorisation parameters
                num=8; // position of the first factorisation parameter
                
                facMethod = (Integer.valueOf(args[num])).intValue();
                faseF = (Integer.valueOf(args[num+1])).intValue();
                appMet= (Integer.valueOf(args[num+2])).intValue();
                distMet = (Integer.valueOf(args[num+3])).intValue();
                dNod = (Double.valueOf(args[num+4])).doubleValue();
                dTre = (Double.valueOf(args[num+5])).doubleValue();
                propCh = (Double.valueOf(args[num+6])).doubleValue();
                lev = (Double.valueOf(args[num+7])).doubleValue();
                
                
                propagation = new FactorisedISDynamic(b,e,lp,lu,ls,ss,nruns,
                dNod, dTre, propCh, lev, faseF,facMethod,appMet,distMet);
                
                System.out.println("Reading exact results");
                propagation.readExactResults(args[7]);
                System.out.println("Done");
                
                propagation.propagate(args[6]);
            }
        }
    }
    
    
    
    /**
     * Creates a new propagation with the options given as arguments.
     * @param b a belief netowrk.
     * @param e an evidence.
     * @param lp the limit for prunning.
     * @param lu the limit for updating.
     * @param ls the maximum size for potentials.
     * @param ss the sample size.
     * @param nruns the number of runs.
     */
    
    public FactorisedISDynamic(Bnet b, Evidence e, double lp, double lu, int ls,
    int ss, int nruns, double dNod, double dTre, double propChi, double lev, int fase,
    int mFact,int appMet, int distMet) {
        
        observations = e;
        network = b;
        setLimitSize(ls);
        setLimitForPrunning(lp);
        setLimitForUpdating(lu);
        setSampleSize(ss);
        setNumberOfRuns(nruns);
        positions = new Hashtable(20);
        
        factorisationParam = new FactorisationTools(dNod,dTre,mFact,appMet, distMet,
        propChi,lev,fase);
    }
    
    
    /* METHODS */
    
    
    /**
     * Compute the sampling distributions.
     * There will be a sampling distribution for each
     * variable of interest.
     * The deletion sequence will be stored in the list
     * <code>deletionSequence</code>. For each variable in that list,
     * its sampling distribution will be stored at the same position in
     * list <code>samplingDistributions</code>.
     *
     * Note that observed variables are not included in the deletion
     * sequence, since they need not be simulated.
     */
    
    public void getSamplingDistributions() {
        
        NodeList notRemoved;
        Node variableX;
        FiniteStates variableY;
        Relation rel, factorRel;
        RelationList currentRelations, tempList;
        PotentialTree pot, pot2, treeRel;
        Potential tempPot;
        ListPotential relPot, newPot;
        PairTable pairTable;
        int i, j, k, p, pos, s,l;
        Vector vecPots;
        
        
        notRemoved = new NodeList();
        pairTable = new PairTable();
        
        deletionSequence = new NodeList();
        samplingDistributions = new Vector();
        deletionDistributions =  new Vector();
        sentDistributions =  new Vector();
        
        
        // Select the variables to remove (those not observed).
        s = network.getNodeList().size();
        
        for (i=0 ; i<s ; i++) {
            variableX = (FiniteStates)network.getNodeList().elementAt(i);
            
            if (!observations.isObserved(variableX)) {
                notRemoved.insertNode(variableX);
                pairTable.addElement(variableX);
            }
        }
        
        currentRelations = getInitialRelations();
        
        
        // Now restrict the initial relations to the obervations.
        
        if (observations.size() > 0)
            restrictToObservations(currentRelations);
        
        // The next step is to factorise the initial relations
        // restricted to the observations.
        
        
        for (i=(currentRelations.size()-1) ; i >= 0 ; i--) {
            
            rel = currentRelations.elementAt(i);
            
            // This ListPotential has only one tree, but it is necessary to factorise.
            relPot = new ListPotential(rel.getValues());
            
            //relPot.limitBound(lowLimitForPruning);
            relPot.limitBound(limitForPrunning);
            
            newPot = (ListPotential)relPot.factorisePotentialAllVbles(factorisationParam);
            
            
            // newPot contains the list of factors
            // Now we must create a new relation for each factor in newPot
            // and remove rel from currentRelations.
            
            if (newPot!=null) {
                currentRelations.removeRelationAt(i);
                vecPots = newPot.getList();
                for (j=0 ; j<vecPots.size() ; j++) {
                    tempPot = (Potential)vecPots.elementAt(j);
                    rel = new Relation(tempPot);
                    currentRelations.insertRelation(rel);
                }
            }
        }
        
        for (i=0 ; i<currentRelations.size() ; i++)
            pairTable.addRelation(currentRelations.elementAt(i));
        
        
        for (i=notRemoved.size() ; i>0 ; i--) {
            // Next variable to remove
            variableX = pairTable.nextToRemove();
            
            // This variable will be in position (i-1) in results
            // and in currentConf[].
            positions.put(variableX,new Integer(i-1));
            
            notRemoved.removeNode(variableX);
            pairTable.removeVariable(variableX);
            deletionSequence.insertNode(variableX);
            
            // Get the relations containing the variable and remove them
            // from the list.
            tempList = currentRelations.getRelationsOfAndRemove(variableX);
            
            deletionDistributions.addElement(tempList);
            
            // Remove them also from the search table.
            l = tempList.size();
            rel = tempList.elementAt(0);
            pairTable.removeRelation(rel);
            treeRel = (PotentialTree)rel.getValues();
            vecPots= treeRel.factoriseOnlyPT((FiniteStates)variableX,factorisationParam);
            
            if ( vecPots.size()>0 ) { // factorisation succeed
                pot = (PotentialTree) vecPots.lastElement(); // only the last tree
                // contains the variable
                
                // The other potentials are inserted in the pair table.
                for (k=0 ; k< (vecPots.size()-1) ; k++) {
                    treeRel = (PotentialTree)vecPots.elementAt(k);
                    factorRel = new Relation(treeRel);
                    currentRelations.insertRelation(factorRel);
                    pairTable.addRelation(factorRel);
                }
            } else { //Factorisation failed
                pot = treeRel;
            }
            
            /*for (j=1 ; j< l ; j++) {
                rel = tempList.elementAt(j);
                pairTable.removeRelation(rel);
                pot = (PotentialTree) pot.combine((PotentialTree)rel.getValues());
            }*/
            
            // Now combine pot with the rest of the potentials that
            // contain variableX, but factorising beforehand.
            
            for (j=1 ; j<l ; j++) {
                rel = tempList.elementAt(j);
                pairTable.removeRelation(rel);
                
                treeRel= (PotentialTree)rel.getValues();
                vecPots= treeRel.factoriseOnlyPT((FiniteStates)variableX,factorisationParam);
                
                if ( vecPots.size()>0 ){ // factorisation succeed
                    pot2 = (PotentialTree) vecPots.lastElement(); // only the last tree
                    // contains the variable
                    
                    // The other potentials are inserted in the pair table.
                    for (k=0 ; k< (vecPots.size()-1) ; k++) {
                        treeRel = (PotentialTree)vecPots.elementAt(k);
                        factorRel = new Relation(treeRel);
                        currentRelations.insertRelation(factorRel);
                        pairTable.addRelation(factorRel);
                    }
                    
                } else { // Factorisation failed
                    pot2 = treeRel;
                }
                
                pot = (PotentialTree) pot.combine(pot2);
            }
            
            // Put the obtained list of relations as the sampling
            // distribution of the variable (initially).
            samplingDistributions.addElement(pot);
            
            pot = (PotentialTree)pot.addVariable(variableX);
            //pot.limitBound(limitForPrunning);
            
            //pot = (PotentialTree) pot.sortAndBound(limitSize);
            
            if (l == 1) {
                for (k=pot.getVariables().size()-1 ; k>=0 ; k--) {
                    variableY = (FiniteStates)pot.getVariables().elementAt(k);
                    
                    if (!pot.getTree().isIn(variableY)) {
                        if (currentRelations.isIn(variableY)) {
                            pos = pot.getVariables().indexOf(variableY);
                            pot.getVariables().removeElementAt(pos);
                        }
                    }
                }
            }
            
            sentDistributions.addElement(pot);
            
            rel = new Relation();
            
            rel.setKind(Relation.POTENTIAL);
            rel.getVariables().setNodes((Vector)pot.getVariables().clone());
            rel.setValues(pot);
            currentRelations.insertRelation(rel);
            pairTable.addRelation(rel);
        }
        
    }
    
} // end of ImportanceSamplingDynamic class
