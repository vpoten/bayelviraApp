package elvira.inference.clustering.IncrementalCompilation;

import elvira.*;
import java.util.ArrayList;
import java.util.Random;
//we use it because there are fields in other class that belong to this class
import java.util.Vector; 
import java.io.*;
import elvira.inference.clustering.*;
import elvira.parser.ParseException;
//for testing
import elvira.tools.JoinTreeStatistics;
import elvira.tools.Crono;
//for tables
import elvira.potential.PotentialTable;
import elvira.potential.PotentialTree;

/**
 * Class <code>IncrementalCompilation</code> that implements the process of compilation partially on an already compiled BN <br>
 * It will use the Maximal Prime Decomposition in order to mark the affected
 * MPSubgraphs involved in the modifications made on the original BN and re-using
 * the cliques of the original JT that remain unchanged. The algorithms associated
 * to this process were described in the paper "Incremental Compilation of Bayesian
 * Networks" by Flores,Gamez & Olesen (UAI 2003).
 *
 * Created:  23/12/2003
 * @author  Julia
 * @version 1.0
 */

public class IncrementalCompilation {
    
     //Set to true for debugging tasks
     static final boolean debug = false;
    /** The associated moral graph
     */
     public Graph moralGraph;
    
    /** 
     * Also necessary to know the directions of the links (for identification of
     *  moral and non-moral links. So, we need the associated net.
     */
    public Bnet myBayNetwork;
    
    /**
     * The associated JT 
     */
    public JoinTree myJoinTree;
    
    /** The MPST, remember this is an MPS-based Incremental Compilation technique.
     */
    public JoinTree myMPSTree;
    
    /** The list of Modifications (thinking in a batch mode)
     */
    private ArrayList modList;
    
    /** The L of the written algorithms, those relevant links for involved modifications. */
    private LinkList relevantLinks;
    
    /** The Subgraphs that have been marked, elements will be MPSs (NodeJoinTree) */
    public ArrayList markedMPS;
    
    //Notice that is not neceessary to maintain aksi a list of marked cliques, since
    //we can derive the marked cliques from the markedMPS (we always know the correspondences)
    
    //That's for random methods as triangulations
    Random generator;
    
    //Private fields necessary for connection (recombination) methods.
    private int[] possibleRootsJT;
    private int[] possibleRootsMPST;
    
    //Variable used to measure times in experiments, otherwise it can be ignored.
    public Crono cronoGIC=new Crono();
    //Statistical information for this experimental data
    double nRetVars;
    double nRetLinks;
    double extraTime;
    
    //With this two numbers we will control the current size
    //of the (JT&MPST) trees and we will be careful about duplicated
    //labels (the label has to be unique for a tree node)
    private int jlabelCount;// = myJoinTree.size();
    private int mlabelCount;// = myMPSTree.size();
    
    //Initial value for labelCount
    private int jlabelCountFirst;
        
    /**
    * Creates an instance of IncrementalCompilation.
    * @param b a belief network.
    * @param generador is just a <code>Random</code> variable to enable certain
    * processes to break ties.
    */
    public IncrementalCompilation(Bnet b,Random generador) throws InvalidEditException,IOException {
        int i;
        
        generator = generador;
                       
        //Bayesian network is taken from the parameter
        myBayNetwork = b;
        
        //moralGraph is the moralisation of b (Bnet inherits from Graph)
        moralGraph = myBayNetwork.moral();
        
        //initialization of the trees
        //First, Junction or Join Tree
        myJoinTree = new JoinTree();
        
        //Triangulation MUST be donde through GTriangulation
        //and assuring a minimal triangulation (parameter true)
        //in order to construct a valid MPST from this JT.
        myJoinTree.treeOfCliquesByGTriangulation (moralGraph, "CanoMoral", "no", generator, true) ;
        
               
        //This functions is also called
        //from initTables() and initTrees(), if we are doing so, then the next line could
        //be removed, ignored, commented... 
        myJoinTree.assignFamilies(b);
      
        //construct the Maximal Prime Subgraph Tree associated to this Join Tree
        myMPSTree = myJoinTree.getMPST(moralGraph);
        
               
        //Initialization of the list of marked MPSs, at first none marked
        markedMPS = new ArrayList();
        
        //To initialize count for labels in both trees
        jlabelCount = myJoinTree.size();
        mlabelCount = myMPSTree.size();
        
    }//end of constructor
    
    
    //
    // ----------------- Access metthods -----------------
    //

    /**
    * This method is used for accessing to the corresponding moral graph
    *
    * @return the moral <code>Graph</code> that is currently associated to
    * this Incremental Compilation
    */
     public Graph getMoralGraph(){
     return moralGraph;
    }
    
    
    /**
    * This method is used for accessing to the corresponding Bayesian network
    *
    * @return the <code>Bnet</code> (an directed Graph) that is currently associated to
    * this Incremental Compilation
    */
    public Bnet getBNET(){
     return myBayNetwork;
    }
    
  
   /**
    * This method is used for accessing to the corresponding Join Tree
    *
    * @return the <code>JoinTree</code> (tree of cliques) that is currently associated to
    * this Incremental Compilation
    */
    public JoinTree getJT(){
     return myJoinTree;
    }
    
   /**
    * This method is used for accessing to the corresponding Maximal Prime Subgraph Tree
    *
    * @return the <code>JoinTree</code> MPST (tree of subgraphs) that is currently associated to
    * this Incremental Compilation
    */ 
    public JoinTree getMPST(){
     return myMPSTree;
    }
    
    
    //
    // ------- methods for performing an incremental compilation ------
    //
   
    /**
    * This method is used for removing the links incident to a node that the user
    * has requested to eliminate. It is called from ICModificationRemoveNode, since before
    * finally removing the node, it has to be totally disconnected. Then it performs these
    * operations incrementally.
    *
    * @param ml is an <code>ArrayList</code> with the list of modifications to perform, in this
     *case all of them will be removing of links.
     *@param GIC is a boolean value set to true just for experimental purposes.
    * 
    */
    public void processRemoveLinksModifications(ArrayList ml,boolean GIC)
    {
        int i;
        ICModification icMod;
        for(i=0;i<ml.size();i++)
        {
            //debugging
            if (debug)
            {
             System.out.println("(processRemoveLinks) We are on modification number " + i);
            }
            icMod = (ICModification) ml.get(i);
                        
            this.runICModification(icMod,GIC);
            
        }
        
    }
    
         /**
     * Method that performs all the modifications especified in ml
     * @param ml is an ArrayList with elements of type ICModification (subobjects)
     * <ol>
     * As the paper explains, in the Incremental Compilation
     * <li> First we mark the affected MPS for all the modification
     * <li> Second, we connect the old trees with the new (disconnected) tree(s)
     * </ol>
     */

    public void runListOfModifications(ArrayList ml)
    {
        int i;
        ICModification icMod;
        
        //Initialize count for labels for the current situation.
        jlabelCount = myJoinTree.size();
        mlabelCount = myMPSTree.size();
        
        //for debugging
        /* We treat every mofidication in the list and run it (algorithm 3.Step 1)*/
        for(i=0;i<ml.size();i++)
        {
            //debugging
            if (debug)
            {
             System.out.println("We are on modification number " + i);
            }
            icMod = (ICModification) ml.get(i);
            
            this.runICModification(icMod);
            
        }
        //For efficiency when we have a list, first mark and then modify the network.
        ArrayList trees = this.obtainTrees();
        
        
        //debugging
        if (debug)
        {
         System.out.println("**** WE START THE CONNEXION ****");
        }
        /*And now the second step: connect (algorithm 3.Step 2)*/
        try{
        makeConnection(trees);
        } catch (InvalidEditException ie){
            System.out.println("Error when connecting " + ie);
          }
          catch (IOException ioe) {
            System.out.println("Error when connecting " + ioe);
          }
         //debugging
          if (debug)
          { System.out.println("**** CONNEXION FINNISHED****");
          
            GTriangulation gt = new GTriangulation();
            System.out.println("The resulting graph after the process is:");
            gt.pPrint(moralGraph);
            System.out.println("\nAnd the trees after the CONNECTION are:\n");
            System.out.println("===> JoinTree");
            myJoinTree.display3();
            System.out.println("\n ---> MPSTree");
            myMPSTree.display3();
           
            //Checking Families
            ArrayList list;
            Family family;
            for(i=0;i<myJoinTree.getJoinTreeNodes().size();i++){
              NodeJoinTree node = myJoinTree.elementAt(i);
              System.out.println(node.getVariables().toString2());
              list = node.getFamilies();
              for(int j=0;j<list.size();j++){
                   family = (Family)list.get(j);
                   System.out.println("\t"+family.getNode().getName());
             }
           }
          }  
     }
    
    /**
     * For experiments. "Normal" user should call the previous method.
     */

    public void runListOfModifications(ArrayList ml,boolean GIC)
    {
        int i;
        ICModification icMod;
        
        //To control the "name" (labels) of the tree nodes, and avoid duplications.
        jlabelCountFirst = jlabelCount = myJoinTree.size();
        mlabelCount = myMPSTree.size();
        
        /* We treat every mofidication in the list and run it (algorithm 3.Step 1)*/
        for(i=0;i<ml.size();i++)
        {
            //debugging
            if (debug)
            {
             System.out.println("We are on modification number " + i);
            }
            icMod = (ICModification) ml.get(i);
            
        
            this.runICModification(icMod,GIC);
        }
        //For efficiency when we have a list, first mark and then modify the network.
        ArrayList trees = this.obtainTrees();
        
        //debugging
        if (debug)
        {
         System.out.println("**** WE START THE CONNEXION ****");
        }
        /*And now the second step: connect (algorithm 3.Step 2)*/
        try{
        makeConnection(trees);
        } catch (InvalidEditException ie){
            System.out.println("Error when connecting " + ie);
          }
          catch (IOException ioe) {
            System.out.println("Error when connecting " + ioe);
          }
          
         //debugging
          if (debug)
          { System.out.println("**** CONNEXION FINNISHED****");
          
            GTriangulation gt = new GTriangulation();
            System.out.println("The resulting graph after the process is:");
            gt.pPrint(moralGraph);
            System.out.println("\nAnd the trees after the CONNECTION are:\n");
            System.out.println("===> JoinTree");
            myJoinTree.display3();
            System.out.println("\n ---> MPSTree");
            myMPSTree.display3();
           
            //Checking Families
            ArrayList list;
            Family family;
            for(i=0;i<myJoinTree.getJoinTreeNodes().size();i++){
              NodeJoinTree node = myJoinTree.elementAt(i);
              System.out.println(node.getVariables().toString2());
              list = node.getFamilies();
              for(int j=0;j<list.size();j++){
                   family = (Family)list.get(j);
                   System.out.println("\t"+family.getNode().getName());
             }
           }
          }
    }
    
    /**
     * The same, but for experiments: it records some necessary data.
     */

    public double runListOfModifications(ArrayList ml,boolean GIC,Crono crono,ArrayList dataL)
    {
        int i;
        ICModification icMod;
        double[] data;
        
    
        jlabelCountFirst = jlabelCount = myJoinTree.size();
        mlabelCount = myMPSTree.size();
        //for debugging
    
        /* We treat every mofidication in the list and run it (algorithm 3.Step 1)*/
        for(i=0;i<ml.size();i++)
        {
            //debugging
            if (debug)
            {
             System.out.println("We are on modification number " + i);
            }
            icMod = (ICModification) ml.get(i);
                        
            this.runICModification(icMod,GIC);
            
        }
        //For efficiency when we have a list, first mark and then modify the network.
        ArrayList trees = this.obtainTrees();
        
        //Measure of time
        cronoGIC.start();
        cronoGIC.toCero();
        nRetVars = 0.0;
        nRetLinks = 0.0;
        extraTime = cronoGIC.getTime();
        cronoGIC.stop();
        
        
        //debugging
        if (debug)
        {
         System.out.println("**** WE START THE CONNEXION ****");
        }
        
        /*And now the second step: connect (algorithm 3.Step 2)*/
        try{
        makeConnection(trees);
        } catch (InvalidEditException ie){
            System.out.println("Error when connecting " + ie);
          }
          catch (IOException ioe) {
            System.out.println("Error when connecting " + ioe);
          }
          
        
        //New measurement
        cronoGIC.start();
        cronoGIC.toCero();
        data = processExperimentalData();
        dataL.add(data);
        extraTime +=cronoGIC.getTime();
        cronoGIC.stop();
        
        return extraTime;
     }
     
    /**Method for particular experiments
     */
     private double[] processExperimentalData(ArrayList trees)
     {
         double[] data = new double[4];
         double totalAffectedCliques = 0.0;
         double totalNvarCliques = 0.0;
         double totalAffectedSubgraphs = 0.0;
         double totalNvarSubgraphs = 0.0;
         int i,j,k;
         ArrayList T,correspondingCliques;
         NodeJoinTree subg,clique;
         
         
         for(i=0;i<trees.size();i++)
         {
             T = (ArrayList)trees.get(i);
             totalAffectedSubgraphs += T.size();
             for(j=0;j<T.size();j++)
             {
                 subg = (NodeJoinTree)T.get(j);
                 totalNvarSubgraphs += subg.getVariables().size();
                 correspondingCliques = subg.getCliques();
                 totalAffectedCliques += correspondingCliques.size();
                 for(k=0;k<correspondingCliques.size();k++)
                 {
                     clique = (NodeJoinTree)correspondingCliques.get(k);
                     totalNvarCliques += clique.getVariables().size();
                 }
                 
             }
         }
         
         data[0] = (double)((double)(totalAffectedCliques*100.0) / (double)myJoinTree.size());
         data[1] = (double)(totalNvarCliques / (double)totalAffectedCliques);
         data[2] = (double)((double)(totalAffectedSubgraphs*100.0) / (double)myMPSTree.size());
         data[3] = (double)(totalNvarSubgraphs / (double)totalAffectedSubgraphs);
     
        return data;
         
     }
     
    /**Method for particular experiments
     */
     private double[] processExperimentalData()
     {
         double[] data = new double[2];
         data[0] = nRetVars;
         data[1] = nRetLinks;
         return data;
         
     }
    
    
     /**
     * Method that performs a single modification (STANDARD VERSION)
     * @param mod is the <code>ICModification</code> we want to run
     * <ol>
     * As the algorithm 3. Step 1 shows:
     * a) First modify the moral graph depending on the modification (polimorfism takes care of it)
     * b) And then, depending again on the modification, mark the affected subgraphs.
     * </ol>
     */
     public void runICModification(ICModification mod)
     {
           /* We need to compute L, that stands for the set of relevant links in
            * this modification (important for addition/deletion of links)*/
           relevantLinks = mod.ModifyMoralGraph(moralGraph);
           
           //debugging
           if (debug)
           { System.out.println("Relevant links are: \n" + relevantLinks +"\n");
           }
           
           /*Secondly we mark the affected Subgraphs in two ways:
            * - turn to true the isMarked field of the NodeJoinTree
            * - assert the NodeJoinTree to the list markedMPS, initially empty (constructor)
            */
           mod.MarkAffectedMPSs(myJoinTree,myMPSTree,markedMPS);
           
           if (debug)
           {
           GTriangulation gt = new GTriangulation();
           System.out.println("After marking the Graph is:");
           gt.pPrint(moralGraph);
           
           
           
           System.out.println("\nTREES AFTER MARKING PROCESS:");
           System.out.println("JoinTree");
           myJoinTree.display3();
           System.out.println("\nMPSTree");
           myMPSTree.display3();
           
           //Families
           int i;
           ArrayList list;
           Family family;
           for(i=0;i<myJoinTree.getJoinTreeNodes().size();i++){
             NodeJoinTree node = myJoinTree.elementAt(i);
             System.out.println(node.getVariables().toString2());
             list = node.getFamilies();
             for(int j=0;j<list.size();j++){
                   family = (Family)list.get(j);
                   System.out.println("\t"+family.getNode().getName());
             }
           }
          }
    }
    
        
     /**
     * Method that performs a single modification (VERSION FOR EXPERIMENTS)
     * @param mod is the <code>ICModification</code> we want to run
     * <ol>
     * As the algorithm 3. Step 1 shows:
     * a) First modify the moral graph depending on the modification (polimorfism takes care of it)
     * b) And then, depending again on the modification, mark the affected subgraphs.
     * </ol>
     */
     public void runICModification(ICModification mod,boolean GIC)
     {
           /* We need to compute L, that stands for the set of relevant links in
            * this modification (important for addition/deletion of links)*/
           relevantLinks = mod.ModifyMoralGraph(moralGraph,GIC);
           //debugging
           if (debug)
           { System.out.println("Relevant links are: \n" + relevantLinks +"\n");
           }
           
           /*Secondly we mark the affected Subgraphs in two ways:
            * - turn to true the isMarked field of the NodeJoinTree
            * - assert the NodeJoinTree to the list markedMPS, initially empty (constructor)
            */
           mod.MarkAffectedMPSs(myJoinTree,myMPSTree,markedMPS);
           
           //debugging
           if (debug)
           {
           GTriangulation gt = new GTriangulation();
           System.out.println("The graph will be then");
           gt.pPrint(moralGraph);
           
           System.out.println("\nTREES AFTER MARKING PROCESS:");
           System.out.println("JoinTree");
           myJoinTree.display3();
           System.out.println("\nMPSTree");
           myMPSTree.display3();
           
           //Families
           int i;
           ArrayList list;
           Family family;
           for(i=0;i<myJoinTree.getJoinTreeNodes().size();i++){
             NodeJoinTree node = myJoinTree.elementAt(i);
             System.out.println(node.getVariables().toString2());
             list = node.getFamilies();
             for(int j=0;j<list.size();j++){
                   family = (Family)list.get(j);
                   System.out.println("\t"+family.getNode().getName());
             }
           }
          }
    }
     
     
    
    
    /**
     * Method that finds the Clique in the Join Tree that contains the family of n
     * @param n is the <code>Node</code> whose family we want to know in which clique is located
     * @return the clique that contains the family of n
     */ 
    public NodeJoinTree getCliqueWithFamily(Node n)
    {
        int i,j;
        Vector jtnodes = myJoinTree.getJoinTreeNodes();
        Family f;
        ArrayList fams;
        
        for(i=0;i<jtnodes.size();i++)
        {
           fams = ((NodeJoinTree) jtnodes.elementAt(i)).getFamilies();
           for(j=0;j<fams.size();j++)
           {
               f = (Family) fams.get(j);
               if (f.getNode().equals(n))
                   return (NodeJoinTree)jtnodes.elementAt(i);
           }
            
            
        }
        return null;
     }
    
    /** Method that will help to divide the marking portions of
     * the tree(s) in several disconnected (and so unrelated)
     *  parts
     */
     public void disconnectedTree(NodeJoinTree M,ArrayList T,NodeJoinTree Prev,ArrayList marked)
     {
         int i,pos;
         NeighbourTreeList ntl;
         NodeJoinTree neigh;
         
         if (M.getIsMarked())
         {
             if (T.indexOf(M)==-1)
             { T.add(M);
               markedMPS.remove(markedMPS.indexOf(M));
             }
             ntl = M.getNeighbourList();
             for(i=0;i<ntl.size();i++)
             {
                 neigh = ntl.elementAt(i).getNeighbour();
       
                 if (neigh!=Prev)
                 //Go on recursively
                 { disconnectedTree(neigh,T,M,marked);
                 }
             }
         }
     }
     
     
    /** Method that will get the different parts to treat
     *  @return the list of trees to be differently (because they are disconnected)
     *  in the process of recombining them with the remaining (unchanged) trees
     */
     public ArrayList obtainTrees()
     {
        ArrayList TMPD = new ArrayList(); 
        int numMarked= markedMPS.size();
        ArrayList trees=new ArrayList();
        
        while (markedMPS.size()>0)
        { disconnectedTree((NodeJoinTree)markedMPS.get(0),TMPD,null,markedMPS);
          trees.add(TMPD);
          TMPD = new ArrayList();
        }
        return trees;
     }
     
    /**
     * Method that connects the new (mini)trees to the existing and unchanged one.
     *
     * @param trees are the portions to treat in each iteration
     **/ 
    public void makeConnection(ArrayList trees) throws InvalidEditException,IOException{
        
        GTriangulation gt = new GTriangulation();
        ArrayList TMPD;        
        int i,j;
        NodeJoinTree Mi;
        NeighbourTreeList ntl;
        NodeJoinTree Neigh;
        NodeJoinTree Clique,MPS;
        NodeList vbles;
        NodeList all_vbles = new NodeList();
        Graph miniMoralGraph;
      
        //These are for avoiding repetition of labels in new cliques/MPS
        //originated from the partial new retriangulations
        if  (jlabelCount<myJoinTree.size())
            jlabelCount=myJoinTree.size();
            
        if (mlabelCount<myMPSTree.size())
            mlabelCount = myMPSTree.size();
        
        //We first declare ArrayList since we don't know the real dimensions.
        ArrayList rootsJT = new ArrayList();
        ArrayList rootsMT = new ArrayList();
        
        //This will contain those clusters that could have been a root
        //in some (mini)tree and then has to reveive a special treatment
        //since they may lead to disconnections. First to introduce, the original
        //cluster labelled as 0 (this is a rule we decided from the beginning to make
        //everything easier and controlled).
        rootsJT.add(myJoinTree.elementAt(0));
        rootsMT.add(myMPSTree.elementAt(0));
                
        //We have to repeat the processing method for every disconnected portion
        for(i=0;i<trees.size();i++)
        {
         //take the corresponding marked disconnected tree
         TMPD = (ArrayList) trees.get(i);
        //(a): Mark all the cliques in JT, having the MPSs is only a question on seeing
        //correspondence. The information about 
        //whether a clique is marked or not can be taken from the corresponding MPS quickly
        
         if (debug)
         System.out.println("The size of the JT is " + myJoinTree.size());
         
        //(b): Choose one cluster C of this T and one MPS M of the same T_MPD
         MPS = (NodeJoinTree) TMPD.get(0);
         Clique = (NodeJoinTree) MPS.getCliques().get(0);
        
         vbles = new NodeList();
        //(c): Obtain the list V of all variables included in T_MPD
         for(j=0;j<TMPD.size();j++)
         {
             vbles.join(((NodeJoinTree)TMPD.get(j)).getNodeRelation().getVariables());
         }
        
        //(d): get a projection g^M of the moral graph over these variables V
        //We need to duplicate information, It couldn't be references, because we will lose 
        //parents, children and siblings
        miniMoralGraph = moralGraph.projectGraph(vbles);
                 
        //For measuring times (in experiments, otherwise, just ignore)
        cronoGIC.start();
        cronoGIC.toCero();
        //This is a collection of data for statistical info devoted to experiments
        nRetVars += miniMoralGraph.getNodeList().size();
        nRetLinks += miniMoralGraph.getLinkList().size();
        extraTime +=cronoGIC.getTime();
        cronoGIC.stop();
        
        if (debug)
        { 
          System.out.println("This miniGraph is");
          gt.pPrint(miniMoralGraph);
        }
        
        JoinTree miniMT,miniJT;
        //If the case that all variables in the subgraphs of TMPD have already been deleted
        //Then the projection will be null (we need to control that error, that's why >0)
        if (miniMoralGraph.getNodeList().size()>0)
        {
        //(e): t <-- ConstructJoinTree(g^M)
        //Same procedure as IC constructor
         miniJT = new JoinTree();
         miniJT.treeOfCliquesByGTriangulation (miniMoralGraph, "CanoMoral", "no", generator, true);
        
         //We don't asign families until de end (new tree obtained)
                        
         //(f): t_MPD <-- AggregateCliques(t)
         miniMT = miniJT.getMPST(miniMoralGraph);
        
         //It is time to add the clusters that have been roots in any tree (control disconnections)
         rootsJT.add(miniJT.elementAt(0));
         rootsMT.add(miniMT.elementAt(0));
         
        
         //We are going to number cliques in the small trees in a way that
         //labels are not repeated, for that counters jlabelCount and mlabelCount
         miniJT.setLabels(jlabelCount);
         jlabelCount=jlabelCount+miniJT.size();
         
         miniMT.setLabels(mlabelCount);
         mlabelCount=mlabelCount+miniMT.size();
         
         //debugging
         if (debug)
         {
         System.out.println("miniJT.display3()");
         miniJT.display3();
         System.out.println("miniJT.display3()");
         
         
         System.out.println("miniMT.display3()");
         miniMT.display3();
         System.out.println("End of miniMT.display3()");
         }
         
                 
         NodeJoinTree nodeT;
        
        //In implementation we first connect the MPSTree and then the Join Tree
        //That is because we need the same structure (when more than one only branching option is possible), 
        //that is, the Join Tree aggregated must lead to the new MPST.
        
        //(g): newT <-- connect(t,C,nil)
        //(h): newT_MPD <-- connect(t_MPD,M,nil)
         connectMT(miniMT,MPS,null);
         //If we comment the previous line and uncomment the next one
         //the result will be exactly the same, but with an iterative version.
         //connectMT_iterative(miniMT,MPS); 

         //If it is not still in the possible roots, we add this one too (after connection MPST root
         //could have changed).
         if (((NodeJoinTree)rootsMT.get(rootsMT.size()-1)).getLabel()!=miniMT.elementAt(0).getLabel())
         {  rootsMT.add(miniMT.elementAt(0));
            if (debug)
            System.out.println("We introduce another root cluster for MT");
         }
        
           
        //Those subgraphs in the miniMPSTree not yet in the final tree must be added
        for(j=0;j<miniMT.size();j++)
        {
            nodeT = miniMT.elementAt(j);
            if (myMPSTree.indexOf(nodeT)==-1)
            {
              //update information about nodes  (the entire graph) --> done at the end (all TMPDs treated)
              myMPSTree.insertNodeJoinTree(nodeT);
            }
        }

        //Same procedure as before, but now for the JT
        connectJT(miniJT,Clique,null);
        //Again two versions: recusive (line above) and iterative (line below)
        //connectJT_iterative(miniJT,Clique);      
        
        if (((NodeJoinTree)rootsJT.get(rootsJT.size()-1)).getLabel()!=miniJT.elementAt(0).getLabel())
         {  rootsJT.add(miniMT.elementAt(0));
            if (debug)
            System.out.println("Introducimos otra raíz JT");
         }
        
        //Those cliques in the miniJTree not yet in the final tree must be added
        for(j=0;j<miniJT.size();j++)
        {
            nodeT = miniJT.elementAt(j);
            if (myJoinTree.indexOf(nodeT)==-1)
            {  //update information about nodes (the entire graph) --> done at the end (all TMPDs treated)
                myJoinTree.insertNodeJoinTree(nodeT);
            }
        }     
        }
        //(i): Delete T and T_MPD
        deleteOldTrees(TMPD);
        
        
        //debugging
        if (debug)
        {
         System.out.println("AFTER deletetrees JT results");
         myJoinTree.display3();
         System.out.println("END OF AFTER deletetrees");
         
         System.out.println("AFTER deletetrees MPSTREE results");
         myMPSTree.display3();
         System.out.println("END OF AFTER deletetrees (MPSTREE)");
        }
         
         //we accumulate the "surviving" variables of this step (portion)
         all_vbles.join(vbles);
        }
        
        //Transformation from an ArrayList to regular vectors seeking ease and efficiency
        possibleRootsMPST = new int[rootsMT.size()];
        possibleRootsJT = new int[rootsJT.size()];
        int k;
        for(k=0;k<rootsMT.size();k++)
        {
            possibleRootsMPST[k] = ((NodeJoinTree)rootsMT.get(k)).getLabel();
        }
        for(k=0;k<rootsJT.size();k++)
        {
            possibleRootsJT[k] = ((NodeJoinTree)rootsJT.get(k)).getLabel();
        }
        
        //Now we have to put the families properly in the cliques associated
        myJoinTree.assignFamiliesRestrictedTo(all_vbles,myBayNetwork);
        
        //And now variables from the miniTrees (that lost environmental information) 
        //and/or affected by the modifications must take (update) information from
        //the current graph where modicationas have been carried out.
        for(j=0;j<myJoinTree.size();j++)
        {
             update_information(myJoinTree.elementAt(j),moralGraph,all_vbles);
        }
        
        //We have to "mend" relations before initialize tables 
        //(this time doesn't compute --> extra)
        cronoGIC.start();
        cronoGIC.toCero();
        //"Repairing" relations
        generateRelations(myBayNetwork);
        extraTime +=cronoGIC.getTime();
        cronoGIC.stop();
        
        //for tables
        initTables();
        
        //Same procedure with the MPSTree, and we also unmarked the subgraphs, 
        //because we have already treated them.
        for(j=0;j<myMPSTree.size();j++)
         {
             MPS = myMPSTree.elementAt(j);
             update_information(MPS,moralGraph,all_vbles);
             MPS.setIsMarked(false);
         }
        
        
        renumerateTree(mlabelCount,false,possibleRootsMPST);
        renumerateTree(jlabelCount,true,possibleRootsJT);
        
    }
    
    /**
     * Method that updates the information of a clique for the set of variables that have to be 
     * changed
     * @param MPS is the <code>NodeJoinTree</code> whose variables must be updated
     * @param g is the <code>Graph</code> from which we take the current information about variables
     * @param changeThem is the <code>NodeList</code> that indicate which nodes in the MPS have to be updated
     */ 
    private void update_information(NodeJoinTree Cluster,Graph g,NodeList changeThem)
    {
        int i;
        //nl is the list of variables in the graph
        NodeList nl = g.getNodeList();
        //myVbles are of variables from the cluster
        NodeList myVbles = Cluster.getNodeRelation().getVariables();
        //commonVbles are the variables in the cluster that need to be changed
        NodeList commonVbles = myVbles.intersectionNames(changeThem);
                      
        int posRelation,posNL;
        Node currentN;
        
        Vector v = new Vector();
        
        //For every variable to be changed in this cluster
        for(i=0;i<commonVbles.size();i++)
        {
            currentN = commonVbles.elementAt(i);
            posRelation = myVbles.getId(currentN);
            posNL = nl.getId(currentN.getName());
        
            if (posNL==-1)
            {     System.out.println("When trying to search " + currentN.getName() + " it was not found.");
            //put the information of the node contained in the graph
            } else //29/04/05
            myVbles.setElementAt(nl.elementAt(posNL),posRelation);
        
         }
     
    }
    

    //--jgamez
    /**
     * initializing probability tables only for those cliques which 
     * modified family list
     */

    public void initTables( ){

      PotentialTable potTable, potTable2;
      PotentialTree potTree;
      NodeJoinTree node;
      Relation r;
      int i, j;
      Family family;
      ArrayList families;


      // We create unitary potentials for all the cliques

      for (i=0 ; i<myJoinTree.getJoinTreeNodes().size() ; i++) {
        node = (NodeJoinTree)myJoinTree.elementAt(i);
        if (node.getLabel() < jlabelCountFirst) continue;
        r = node.getNodeRelation();
        potTable = new PotentialTable(r.getVariables());
        potTable.setValue(1.0);
        r.setValues(potTable);
      }

      // Now, we initialize the potentials using the families associated
      // to each clique

      for(i=0;i<myJoinTree.getJoinTreeNodes().size();i++){
        node = (NodeJoinTree)myJoinTree.elementAt(i);
      
        if (node.getLabel() < jlabelCountFirst) continue;
        families = node.getFamilies();
        if (families.size() == 0){ // unitary potential 
          r = node.getNodeRelation();
          potTable = new PotentialTable(r.getVariables());
          potTable.setValue(1.0);
          r.setValues(potTable);
        }
        else{ //getting the potential of the first family
          family = (Family)families.get(0);
          r = family.getRelation( );
      
          potTable = PotentialTable.convertToPotentialTable(r.getValues());
          // creating the potential by combination
          for(j=1;j<families.size();j++){
            family = (Family)families.get(j);
      
            r = family.getRelation();
            potTable = potTable.combine(
		PotentialTable.convertToPotentialTable(r.getValues()));
                     
          }
          //assigning the potential
          r = node.getNodeRelation();
          potTable2 = new PotentialTable(r.getVariables(),potTable);
          r.setValues(potTable2);
        }
      }
      
  }


  
  // We want to create the relations for those that we have lost
  // or for those that are not necessary anymore (if a node has been deleted)
  //
  /**
   * Method that generates those families that are no more consistent with
   * the information of the nodes in the network
   */
  
  private void generateRelations(Bnet b)
  {
    int i;

    Node node;
    NodeList nl,pa;
    Relation currentRelation;
             
     // creating relations
     for (i=0 ; i< b.getNodeList().size() ; i++) {
       nl = new NodeList();
       node = (FiniteStates)b.getNodeList().elementAt(i);
       nl.insertNode(node);
       pa = b.parents(node);
       nl.join(pa);
       currentRelation = b.getRelation(node);
       //This is valid because the way we act (but there could be initially X-->Y.
       //We delete X, and afterwards we add Z-->Y, the relation of Y has size 2 in both cases
       //but the potential will be different P(Y|X)!=P(Y|Z)
       if (currentRelation==null)
       {
         Relation relation = new Relation();
         relation.setVariables(nl);
         relation.setKind(Relation.CONDITIONAL_PROB);
         PotentialTable potentialTable = 
		new PotentialTable(generator,nl,1);

         relation.setValues(potentialTable);
         b.getRelationList().addElement(relation);
       }
       else  if (currentRelation.getVariables().size()!=nl.size())
       {
         currentRelation.setVariables(nl);
         currentRelation.setKind(Relation.CONDITIONAL_PROB);
         PotentialTable potentialTable = 
		new PotentialTable(generator,nl,1);

         currentRelation.setValues(potentialTable);
           
       }
    }
  }
  

   
    /**
     * Method that deletes the marked tree already treated
     * @param TMarked is an <code>ArrayList</code> with NodeJoinTree elements
     */  
    private void deleteOldTrees(ArrayList TMarked){
        int i,j;
        NodeJoinTree nMPST;
        for(i=0;i<TMarked.size();i++)
        {  
           nMPST = (NodeJoinTree)TMarked.get(i);
           for(j=0;j<nMPST.getCliques().size();j++)
           { deleteInTree((NodeJoinTree)nMPST.getCliques().get(j),myJoinTree); 
           }
           deleteInTree((NodeJoinTree)TMarked.get(i),myMPSTree);
        }
   }
   
   /**
     * Auxiliar method that deletes a certain Cluster in a tree
     * @param cluster is the <code>NodeJoinTree</code> to be deleted
     * @param tree is the <code>JoinTree</code> from which we have to delete the clsuter
     */ 
   private void deleteInTree(NodeJoinTree cluster,JoinTree tree)
   {
        int i;
        NeighbourTreeList ntl;
        ntl = cluster.getNeighbourList();
        
        //Look into all its neighbours to remove any trail
        //Both direction of the (neighbour) relationship are deleted
        for(i=ntl.size()-1;i>=0;i--)
        {
          ntl.elementAt(i).getNeighbour().removeNeighbour(cluster);  
          cluster.removeNeighbour(ntl.elementAt(i).getNeighbour());
        }
       tree.removeNodeJoinTree(cluster);
   }
   
    
    //t will be the new "mini"-JoinTree (MPSDTree)
    /**
     * Method that connects the small new mini_JoinTree (t) with the unchanged/old one
     * knowing that the call comes from Cj and for connecting Ci
     * @param t is the small <code>JoinTree</code> we have just constructed with the retriangulated miniRB
     * @param Ci is the <code>NodeJoinTree</code> from which we want to start the connexion
     * @param Cj is the <code>NodeJoinTree</code> that provoked this call to the function. It is a recursive one
     * and we want to avoid a non-ending loop
     */ 
     public void connectJT(JoinTree t, NodeJoinTree Ci, NodeJoinTree Cj)
     {
        int k,posN;
        NeighbourTreeList ntl = Ci.getNeighbourList();
        NeighbourTree nt;
        NodeJoinTree Ck,maxIntersC;
        NodeList Sep_ik;
        NodeList Sep_ij;
        
      
        //For each separator S between Ci and Ck <> Cj do
        for(k=0;k<ntl.size();k++)
        {
            nt = ntl.elementAt(k);
            Ck = nt.getNeighbour();
            Sep_ik = nt.getMessage().getVariables();
            if (Ck!=Cj)
            //If Ck is unmarked
            {  
                if (!Ck.getIsMarked())
               {
                //debugging
                 if (debug)
                {
                 System.out.println("===>Ci_label_" + Ci.getLabel() + " k is " + k + " ==> Really connecting in JT");
                }
     
                 //(a) locate cluster C belonging to t such that C \cap Ck is maximal
                maxIntersC = locateMaximalIntersJT(t,Ck);
                
                //(b) Connect C with Ck by S
                //update node information (entire graph) --> at the end
                if  (myJoinTree.indexOf(maxIntersC)==-1)
                    myJoinTree.insertNodeJoinTree(maxIntersC);
                maxIntersC.insertNeighbour(Ck);
                Ck.insertNeighbour(maxIntersC);
                
                //(c) if S == C then amalgamate C and Ck
                if (Sep_ik.equals(maxIntersC.getNodeRelation().getVariables()))
                    { if (debug)
                      System.out.println("Amalgamating in JT");
                      amalgamate(maxIntersC,Ck,myJoinTree);
                      t.removeNodeJoinTree(maxIntersC);
                      
                      Ck.setLabel(maxIntersC.getLabel());
                      
                      t.insertNodeJoinTree(Ck);
                    }
                
               }
               else connectJT(t,Ck,Ci);
            }
       }
    }
    
    
    /**
     * Method very similar to the previous one, but using MPSTrees
     */ 
    public void connectMT(JoinTree t, NodeJoinTree Mi, NodeJoinTree Mj)
    {
        int i,posN;
        NeighbourTreeList ntl = Mi.getNeighbourList();
        NeighbourTree nt;
        NodeJoinTree Mk,maxIntersM;
        NodeList Sep_ik;
        NodeList Sep_ij;
                
        //For each separator S between Ci and Ck <> Cj do
        for(i=0;i<ntl.size();i++)
        {
            nt = ntl.elementAt(i);
            Mk = nt.getNeighbour();
            Sep_ik = nt.getMessage().getVariables();
            //If Ck is unmarked
            if (Mk!=Mj)
            {
             
            if (!Mk.getIsMarked())
            {
                //debugging
                if (debug)
                {
                 System.out.println("Really connecting in MT");
                }
                //(a) locate cluster C belonging to t such that C \cap Ck is maximal
                maxIntersM = locateMaximalInters(t,Mk);
                
                //(b) Connect C with Ck by S
                //update node information (entire graph)
                if  (myMPSTree.indexOf(maxIntersM)==-1)
                    myMPSTree.insertNodeJoinTree(maxIntersM);
                maxIntersM.insertNeighbour(Mk);
                Mk.insertNeighbour(maxIntersM);
                
                //(c) if S == C then amalgamate C and Ck
                if (Sep_ik.equals(maxIntersM.getNodeRelation().getVariables()))
                    { //debugging
                      if (debug)
                      {
                      System.out.println("Amalgamating in MT");
                      }
                      amalgamate(maxIntersM,Mk,myMPSTree);
                      t.removeNodeJoinTree(maxIntersM);
                      
                      Mk.setLabel(maxIntersM.getLabel());
                      
                      t.insertNodeJoinTree(Mk);
                    }
                
              }
              else connectMT(t,Mk,Mi);
             }
         }
    }


/** Iterative version that does exactly the same as connectJT, but without recursion ****/
    
    public void connectJT_iterative(JoinTree t, NodeJoinTree Ci)
   {
        int k,posN;
        NeighbourTreeList ntl;
        NeighbourTree nt;
        NodeJoinTree Ck,maxIntersC,Clique,Cj;
        NodeList Sep_ik;
        NodeList Sep_ij;
    
        int maxSize = myJoinTree.size()+1;
   
        NodeJoinTree[] queue= new NodeJoinTree[maxSize];
        NodeJoinTree[] previous= new NodeJoinTree[maxSize];
        int top = 0;
        int pointer = 0;
        queue[top] = Ci;
        previous[top] = null;
        top++;
        
        ArrayList pairs2Amalgamate= new ArrayList();
        ArrayList pair = new ArrayList();
        ArrayList list2connect = new ArrayList();
        
        while(pointer<top)
        {
        
            Clique = queue[pointer];
            Cj = previous[pointer];
            //For each separator S between Ci and Ck <> Cj do
            ntl = Clique.getNeighbourList();
            
            for(k=ntl.size()-1;k>=0;k--)
            {
               nt = ntl.elementAt(k);
               Ck = nt.getNeighbour();
               Sep_ik = nt.getMessage().getVariables();
               if (Ck!=Cj)
               //If Ck is unmarked
               {  
                  if (!Ck.getIsMarked())
                  {
                   //debugging
                   if (debug)
                   {
                     System.out.println("===>Ci_label_" + Clique.getLabel() + " k es " + k + " Ck is " + Ck.getLabel() + " ==> Really connecting in JT");
                   }
                    //(a) locate cluster C belonging to t such that C \cap Ck is maximal
                    maxIntersC = locateMaximalIntersJT(t,Ck);
                
                    //(b) Connect C with Ck by S
                    if  (myJoinTree.indexOf(maxIntersC)==-1)
                         myJoinTree.insertNodeJoinTree(maxIntersC);
                    maxIntersC.insertNeighbour(Ck);
                    Ck.insertNeighbour(maxIntersC); 
                
                     //(c) if S = C then amalgamate C and Ck
                     if (Sep_ik.equals(maxIntersC.getNodeRelation().getVariables()))
                      { 
                         if (debug)
                           System.out.println("Amalgamating in JT");
                         amalgamate(maxIntersC,Ck,myJoinTree);
                         t.removeNodeJoinTree(maxIntersC);
                     
                         Ck.setLabel(maxIntersC.getLabel());
                      
                         t.insertNodeJoinTree(Ck);
                         
                       }
                
                   }
                   else 
                   { 
                       if (debug)
                         System.out.println("else JT");
                       queue[top] = Ck;
                       previous[top] = Clique;
                       top ++;
                   }
              } //end if k!=j
           }//end for
        pointer++;
        }
    }



/** Iterative version that does exactly the same as connectMT, but without recursion ****/
public void connectMT_iterative(JoinTree t, NodeJoinTree Mi)
    {
        int k,posN;
        NeighbourTreeList ntl;
        NeighbourTree nt;
        NodeJoinTree Mk,maxIntersM,Subgraph,Mj;
        NodeList Sep_ik;
        NodeList Sep_ij;
    
        int maxSize = myMPSTree.size();
   
        NodeJoinTree[] queue= new NodeJoinTree[maxSize];
        NodeJoinTree[] previous= new NodeJoinTree[maxSize];
        int top = 0;
        int pointer = 0;
        queue[top] = Mi;
        previous[top] = null;
        top++;
        
                
        while(pointer<top)
        {
        
            Subgraph = queue[pointer];
            Mj = previous[pointer];
            //For each separator S between Ci and Ck <> Cj do
            ntl = Subgraph.getNeighbourList();
            
            for(k=ntl.size()-1;k>=0;k--)
            {
               nt = ntl.elementAt(k);
               Mk = nt.getNeighbour();
               Sep_ik = nt.getMessage().getVariables();
               if (Mk!=Mj)
               //If Ck is unmarked
               {  
                  if (!Mk.getIsMarked())
                  {
                   //debugging
                   if (debug)
                   {
                     System.out.println("===>Mi_label_" + Subgraph.getLabel() + " k is " + k + " Mk es " + Mk.getLabel() + " ==>  Really connecting in MT");
                    }
                    //(a) locate cluster C belonging to t such that C \cap Ck is maximal
                    maxIntersM = locateMaximalInters(t,Mk);
                
                    //(b) Connect C with Ck by S
                    if  (myMPSTree.indexOf(maxIntersM)==-1)
                         myMPSTree.insertNodeJoinTree(maxIntersM);
                    maxIntersM.insertNeighbour(Mk);
                    Mk.insertNeighbour(maxIntersM); 
                
                     //(c) if S = C then amalgamate C and Ck
                     if (Sep_ik.equals(maxIntersM.getNodeRelation().getVariables()))
                      { if (debug)
                           System.out.println("Amalgamating in MT");
                         amalgamate(maxIntersM,Mk,myMPSTree);
                         t.removeNodeJoinTree(maxIntersM);
                         
                         Mk.setLabel(maxIntersM.getLabel());
                                               
                         t.insertNodeJoinTree(Mk);
                       }
                
                   }
                   else 
                   { 
                       if (debug)
                         System.out.println("else MT");
                       queue[top] = Mk;
                       previous[top] = Subgraph;
                       top ++;
                   }
              } //end if i!=j
           }//end for
        pointer++;
        }
        
    }
    

    
   /**
   * Method that amalgamates two clusters 
   * @param nodeAbsorbed is the cluster from the miniTree that is equal to a separator and then absorbed
   * @param nodeThatAbsorbs is the cluster tha contained the other one an is going to take its neighbours.
   * @param clusterTree is the <code>JoinTree</code> where this absorption takes place
   */ 
   private void amalgamate(NodeJoinTree nodeAbsorbed,NodeJoinTree nodeThatAbsorbs,JoinTree clusterTree)
   {
       //Two things
       //1.- NodeThatAbsorbs inherits all the neighbours of NodeAbsorbed (same message)
       //and also those neighbours stop being neighbours of NodeAbsorbed
        NeighbourTreeList ntl = nodeAbsorbed.getNeighbourList();
        NeighbourTree nt;
        NodeJoinTree neighbour;
        int i;
        for(i=0;i<ntl.size();i++)
        {
            nt = ntl.elementAt(i);
            neighbour = nt.getNeighbour();
            if (neighbour!=nodeThatAbsorbs) //careful with this comparation
            {
                  nodeThatAbsorbs.insertNeighbour(neighbour);
                  neighbour.removeNeighbour(nodeAbsorbed);
                  neighbour.insertNeighbour(nodeThatAbsorbs);
                
             }
            
        }
        nodeThatAbsorbs.removeNeighbour(nodeAbsorbed);
        
        //2.- NodeAbsorbed can now be eliminated
        clusterTree.removeNodeJoinTree(nodeAbsorbed); 
        //what happens with labels is controlled in the caller method
       
   }
   

   
   /** 
    *  This method search which NodeJoinTree in the tree miniJT presents the
    *  maximum intersection with nJT and returns it.
    *  @param tree is a <code>JoinTree</code> where we want to find the cluster 
    *  @param cluster is the <code>NodeJoinTree</code> whose maximum intersection in the tree
    *  we are looking for
    *  @return the <code>NodeJoinTree</code> of maximum intersection with cluster in the tree
    **/
   private NodeJoinTree locateMaximalInters(JoinTree tree,NodeJoinTree cluster)
       {
        int maxInters = 0,i,numInters;
        Vector nodesJT = tree.getJoinTreeNodes();
        NodeJoinTree currentNode;
        NodeJoinTree outNode = new NodeJoinTree();
        
        for(i=0;i<nodesJT.size();i++)
        {
            currentNode = (NodeJoinTree) nodesJT.elementAt(i);
            numInters = (currentNode.getVariables().intersectionNames(cluster.getVariables())).size();
            if (numInters>maxInters)
            {
                outNode = currentNode;
                maxInters = numInters;
            }
        }
        if (maxInters==0) 
        {   
            System.out.println("(MT) Max intersection is null with  " + cluster.getLabel());
        
            outNode = (NodeJoinTree) nodesJT.elementAt(0);
        }
        return outNode;      
     }
   
     
    /** 
    *  Similar to the previous one, but in case of a draw (same intersection), the structure
    *  of the MPSTree decides.
    **/
   
     private NodeJoinTree locateMaximalIntersJT(JoinTree tree,NodeJoinTree cluster)
       {
        int maxInters = 0,i,numInters;
        Vector nodesJT = tree.getJoinTreeNodes();
        NodeJoinTree currentNode;
        NodeJoinTree outNode = new NodeJoinTree();
        
        for(i=0;i<nodesJT.size();i++)
        {
            currentNode = (NodeJoinTree) nodesJT.elementAt(i);
            numInters = (currentNode.getVariables().intersectionNames(cluster.getVariables())).size();
            if (numInters>=maxInters)
            {
                if (numInters==maxInters)
                { if (currentNode.getCorrespondingMPS().getNeighbourList().indexOf(cluster.getCorrespondingMPS())!=-1)
                  { 
                    outNode = currentNode;
                    maxInters = numInters;
                  }
                }
                else
                  { outNode = currentNode;
                    maxInters = numInters;
                  }
            }
        }
        if (maxInters==0) 
        {  System.out.println("Max intersection is null with " + cluster.getLabel());
           
           outNode = (NodeJoinTree) nodesJT.elementAt(0);
        }
                
        return outNode;      
     }
    
     
    /**
     * Method to renumerate and relabel the tree in a way that future applications
     * of IC will be correct. (Father cluster will always be previous to its children)
     */
    public void renumerateTree(int maxLabel,boolean whichOne,int[] controlDesc)
     {
         Vector JTNs;
         NodeJoinTree cluster,neighbour,nextNode;
         Vector newJTNs = new Vector();
         NeighbourTreeList ntl;
         
         int label;
         
         if (whichOne) //JT
         { JTNs = myJoinTree.getJoinTreeNodes();
         }
         else //MPST
             JTNs = myMPSTree.getJoinTreeNodes();
         int pointer = 0;
         int top = 0;
         int treeSize = JTNs.size();
         
         NodeJoinTree queue[] = new NodeJoinTree[treeSize];
         boolean treated[] = new boolean[maxLabel];
         boolean posRoots[] = new boolean[maxLabel];
         
         boolean inserted[] = new boolean[maxLabel];
         int i,j,k;
         for(i=0;i<maxLabel;i++)
         {
             treated[i]= false;
             posRoots[i]= false;
         
             inserted[i]=false;
             
         }
         for(i=0;i<controlDesc.length;i++)
             {
                  posRoots[controlDesc[i]] = true;
             }
             
         newJTNs.addElement(JTNs.elementAt(0));
         queue[top] = (NodeJoinTree) JTNs.elementAt(0);
         
         inserted[((NodeJoinTree)JTNs.elementAt(0)).getLabel()] = true;
         top++;
         while (pointer<treeSize)
         {   
             //top indicates the next position where locate a node
             //(then queue[top] is null)
             if (pointer>=top) 
             {
                 if (debug)
                 System.out.println("pointer greater than top");
                 for (j=0;j<maxLabel;j++)
                 {
                     if (!inserted[j])
                     {
                         for(k=0;k<JTNs.size();k++)
                         {
                           nextNode = (NodeJoinTree) JTNs.elementAt(k);
                           if (nextNode.getLabel()==j)
                           {
                               nextNode.insertNeighbour((NodeJoinTree) newJTNs.elementAt(0));
                               ((NodeJoinTree) newJTNs.elementAt(0)).insertNeighbour(nextNode);
                               queue[top]=nextNode;
                               top++;
                               newJTNs.addElement(nextNode);
                               inserted[j]=true;
                               break;
                           }
                         }
                     }
                     if (pointer < top) break;
                 }
             }
             cluster = queue[pointer];
             ntl = cluster.getNeighbourList();
             
             label = cluster.getLabel();
             for(i=ntl.size()-1;i>=0;i--)
             {
                 neighbour = ntl.elementAt(i).getNeighbour();

                 if (posRoots[label])
                 {
                     if (ntl.elementAt(i).getMessage().getVariables().size()==0)
                     {
                         cluster.removeNeighbour(neighbour);
                         neighbour.removeNeighbour(cluster);
                         
                         if (!treated[neighbour.getLabel()])
                         { ((NodeJoinTree)newJTNs.elementAt(0)).insertNeighbour(neighbour);
                          neighbour.insertNeighbour((NodeJoinTree)newJTNs.elementAt(0));
                         }
                         else if (!treated[cluster.getLabel()])
                         {
                             ((NodeJoinTree)newJTNs.elementAt(0)).insertNeighbour(cluster);
                             cluster.insertNeighbour((NodeJoinTree)newJTNs.elementAt(0));
                         }
                     }
                     
                 }
                 
                 if (!inserted[neighbour.getLabel()])
                 {
                     if (top>=treeSize)
                     { System.out.println("top value is " + top + " and treeSize is " + treeSize);
                     }
                     queue[top] = neighbour;
                     top++;
                     newJTNs.addElement(neighbour);
                     
                     inserted[neighbour.getLabel()]=true;
                 }
             }
             treated[label]=true;
             pointer++;
          }   
         if (whichOne) //JT
         { myJoinTree.setJoinTreeNodes(newJTNs);
           myJoinTree.setLabels();
         }
         else //MPST
         { myMPSTree.setJoinTreeNodes(newJTNs);
           myMPSTree.setLabels();
         }
         
             
        }
     

       /** auxiliar method to detect cycles --> not used, but it was in a previous moment.*/   
        private boolean cycleTo(NodeJoinTree source, NodeJoinTree target,NodeJoinTree previous)
        {
            NeighbourTreeList ntl;
            NodeJoinTree neigh;
            boolean result = false;
            int i;
            ntl = source.getNeighbourList();
            for(i=0;i<ntl.size();i++)
            {
                neigh = ntl.elementAt(i).getNeighbour();
                if (neigh==target) return true;
                else if (previous!=neigh)
                     { if (cycleTo(neigh,target,source)) 
                       return true;
                     }
            }
            return false;
        }

        
    
 /**
 * Program for testing the IC classes. 
 * The command line arguments are as follows.
 * <ol>
 * <li> Input file: the network. 
 * <li> Number of modifications (it will be divided into four: remove link, remove node, add node, add link). 
 * </ol>
 * One call to the method could be "elvira file network" 4, that will provoke one modification of this kind
 * This procedure is run twice in order to show the batch mode of IC but also the incremental
 * option of doing several passes after several sets of changes. 
 * 
 * Number of modifications equal to 8 will make 2 modifications of each kind, and so.
 */
     
   public static void main(String args[]) throws ParseException,FileNotFoundException, IOException, InvalidEditException{
       Bnet b;
       IncrementalCompilation ic;
       FileInputStream networkFile;
       Random gen = new Random(997);
              
       if (args.length<2){
           System.out.println("Too few arguments. The arguments are:");
           System.out.println("\tNetwork number-of-modifications\n");
       }
       else
       {
        networkFile = new FileInputStream(args[0]);
              
        System.out.print("\nIC Loading network ....");
        b = new Bnet(networkFile);
        System.out.print("IC Network loaded\n");    
       
        //we create the structure for Incremental Compilation process
        ic = new IncrementalCompilation(b,gen);
       
        //creating modifications
        ArrayList mods = new ArrayList();
        ArrayList removed = new ArrayList();
        ArrayList added = new ArrayList();
        ICModification mod;
        
        int numberOFmods = ((Integer)new Integer(args[1])).intValue(); 
        numberOFmods=numberOFmods/4;
       
        Random genNum = new Random();
        
        //FIRST: Remove Links
        Link link2modify;
        int whichone;
        for(int j=0;j<numberOFmods;j++)
        { whichone = genNum.nextInt(b.getLinkList().size());
          link2modify = b.getLinkList().elementAt(whichone);
          mod = new ICModificationRemoveLink(link2modify,ic);
          System.out.println("REMOVE Link "+link2modify.getTail().getName() + " --> " + link2modify.getHead().getName());
          mods.add(mod);
        }
        
        //SECOND: Remove Node
        Node node2modify;
        for(int j=0;j<numberOFmods;j++)
        { whichone = genNum.nextInt(b.getNodeList().size());
          node2modify = b.getNodeList().elementAt(whichone);
          mod = new ICModificationRemoveNode(node2modify,ic);
          System.out.println("REMOVE Node "+node2modify.getName());
          if (!removed.contains((FiniteStates)node2modify))
          { mods.add(mod);
            removed.add(node2modify);
          }
          else j--;
        }
        
        //THIRD: Add Node
        String nameNode = new String("NewNode_");
        for(int j=0;j<numberOFmods;j++)
        { whichone = genNum.nextInt(8);
          node2modify = new FiniteStates(whichone);
          node2modify.setName(nameNode + j);
          mod = new ICModificationAddNode(node2modify,ic);
          System.out.println("ADD Node "+node2modify.getName());
          mods.add(mod);
          added.add(node2modify);
        }
        
        //FOURTH: Add Links (related to added nodes)
        FiniteStates n1,n2;
        link2modify = new Link();
        boolean onemoretry = true;
        for(int j=0;j<numberOFmods;j++)            
        { 
          while (onemoretry)
          {
           whichone = genNum.nextInt(numberOFmods);
           n1 = (FiniteStates)added.get(whichone);
           if (b.getNodePosition(n1.getName())==-1)
             b.addNode(n1); //just for looking at cycles afterwards, before IC undo
           //b.getNodeList().elementAt(b.getNodeList().size()- whichone-1);
           whichone = genNum.nextInt(b.getNodeList().size()- 1);
           n2 = (FiniteStates)b.getNodeList().elementAt(whichone);
           while (removed.contains(n2))
           {
            whichone = genNum.nextInt(b.getNodeList().size()- numberOFmods -1);
            n2 = (FiniteStates)b.getNodeList().elementAt(whichone);
           }
           whichone = genNum.nextInt(2);
           if (whichone == 0)
           {
               if (!b.hasCycle(n1,n2))
               {   link2modify = new Link(n1,n2);
                   onemoretry = false;
               }
           }
           if ((whichone==1)||onemoretry)
           {   
               if (!b.hasCycle(n2,n1))
               {  link2modify = new Link(n2,n1);
                   onemoretry = false;
               }
           }
          }
                  
        mod = new ICModificationAddLink(link2modify,ic);
        System.out.println("ADD LINK "+ link2modify.getTail().getName() + " --> " + link2modify.getHead().getName());
        mods.add(mod);
        onemoretry = true;
        }
        int pos;
        for(int k=0;k<added.size();k++)  
        {
            pos= b.getNodePosition(((FiniteStates)added.get(k)).getName());
            if (pos!=-1)
              b.removeNode(pos); 
            //we added then just to check if cycles, but this adddition will be done in IC.
            //So we need to undo this addNode
        }
       
       
        ic.runListOfModifications(mods);
        
        
         System.out.println("\n...RESULTS OF A FIRST ROUND");  
        //To see results
        int i;
        JoinTree jt = ic.getJT();
        jt.calculateStatistics();
        JoinTreeStatistics jts = jt.getStatistics();
        double treeSize = jts.getJTSize();
        System.out.println("\nThe size of the tree is " + treeSize +"\n");
        System.out.println("Statistics:\n "); jts.print();
        double ramas = 0;
        
        for(i=0;i<ic.getMPST().getJoinTreeNodes().size();i++)
            System.out.println("El mpst " + i + " tiene " + ((NodeJoinTree)ic.getMPST().getJoinTreeNodes().elementAt(i)).getVariables().size());
       
        JoinTreeStatistics mpstats;
        ic.getMPST().calculateStatistics();
        mpstats = ic.getMPST().getStatistics();
        System.out.println("Statistics MPST:\n "); mpstats.print();
              
       
       /**Second round**/
       b = ic.getBNET();       
       mods = new ArrayList();
       removed = new ArrayList();
       added = new ArrayList();
       //FIRST: Remove Links
        
        
        for(int j=0;j<numberOFmods;j++)
        { whichone = genNum.nextInt(b.getLinkList().size());
          link2modify = b.getLinkList().elementAt(whichone);
          mod = new ICModificationRemoveLink(link2modify,ic);
          System.out.println("REMOVE Link "+link2modify.getTail().getName() + " --> " + link2modify.getHead().getName());
          mods.add(mod);
        }
        
        //SECOND: Remove Node

        for(int j=0;j<numberOFmods;j++)
        { whichone = genNum.nextInt(b.getNodeList().size());
          node2modify = (FiniteStates) b.getNodeList().elementAt(whichone);
          mod = new ICModificationRemoveNode(node2modify,ic);
          System.out.println("REMOVE Node "+node2modify.getName());
          mods.add(mod);
          removed.add(node2modify);
        }
        
        //THIRD: Add Node
        nameNode = new String("NewNode_");
        for(int j=0;j<numberOFmods;j++)
        { whichone = genNum.nextInt(8);
          node2modify = new FiniteStates(whichone);
          node2modify.setName(nameNode + (j+numberOFmods)  );
          mod = new ICModificationAddNode(node2modify,ic);
          System.out.println("ADD Node "+node2modify.getName());
          mods.add(mod);
          added.add(node2modify);
        }
        
        //FOURTH: Add Links (related to added nodes)
        
        
        for(int j=0;j<numberOFmods;j++)            
        { 
          while (onemoretry)
          {
           whichone = genNum.nextInt(numberOFmods);
           n1 = (FiniteStates)added.get(whichone);
           if (b.getNodePosition(n1.getName())==-1)
              b.addNode(n1); //just for looking at cycles afterwards, before IC undo
           //b.getNodeList().elementAt(b.getNodeList().size()- whichone-1);
           whichone = genNum.nextInt(b.getNodeList().size());
           n2 = (FiniteStates)b.getNodeList().elementAt(whichone);
           while (removed.contains(n2))
           {
            whichone = genNum.nextInt(b.getNodeList().size()- numberOFmods -1);
            n2 = (FiniteStates)b.getNodeList().elementAt(whichone);
           }
           whichone = genNum.nextInt(2);
           if (whichone == 0)
           {
               if (!b.hasCycle(n1,n2))
               {   link2modify = new Link(n1,n2);
                   onemoretry = false;
               }
           }
           if ((whichone==1)||onemoretry)
           {   
               if (!b.hasCycle(n2,n1))
               {  link2modify = new Link(n2,n1);
                   onemoretry = false;
               }
           }
          }
          mod = new ICModificationAddLink(link2modify,ic);
          System.out.println("ADD LINK "+ link2modify.getTail().getName() + " --> " + link2modify.getHead().getName());
          mods.add(mod);
          onemoretry = true;
        }
        
        for(int k=0;k<added.size();k++)  
        {
            pos= b.getNodePosition(((FiniteStates)added.get(k)).getName());
            if (pos!=-1)
              b.removeNode(pos); 
            //we added then just to check if cycles, but this adddition will be done in IC.
            //So we need to undo this addNode
        }
       
       
       
        
              
        ic.runListOfModifications(mods,true);
       
               
        System.out.println("\n...RESULTS OF ANOTHER SECOND ROUND");  
        jt = ic.getJT();
        jt.calculateStatistics();
        jts = jt.getStatistics();
        treeSize = jts.getJTSize();
        System.out.println("\nThe size of the tree is " + treeSize +"\n");
        System.out.println("\nThe number of MPSs is " + ic.getMPST().getJoinTreeNodes().size() +"\n");
        System.out.println("Statistics:\n "); jts.print();
        for(i=0;i<ic.getMPST().getJoinTreeNodes().size();i++)
            System.out.println("El mpst " + i + " has " + ((NodeJoinTree)ic.getMPST().getJoinTreeNodes().elementAt(i)).getVariables().size() + "variables ");
      
       
        ic.getMPST().calculateStatistics();
        mpstats = ic.getMPST().getStatistics();
        System.out.println("Statistics MPST:\n "); mpstats.print();
        
            System.out.println("-------------------------");
        }
       System.out.println("...Ending Incremental Compilation");
   }
   
}
