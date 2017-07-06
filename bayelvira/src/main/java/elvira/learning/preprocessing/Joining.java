/*
 * Joining.java
 *
 * Created on 22 de marzo de 2006, 12:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package elvira.learning.preprocessing;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.potential.*;
import elvira.learning.preprocessing.*;
import elvira.parser.ParseException;
import elvira.learning.classificationtree.*;
import elvira.tools.ParameterManager;
import elvira.learning.classification.supervised.validation.*;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.supervised.mixed.Gaussian_Naive_Bayes;
import elvira.tools.statistics.analysis.Stat;
import java.io.*;
import java.lang.Runtime;
import java.lang.Math;
import java.util.*;
import java.util.regex.*;

/**
 *
 * Implements supervised methods for joining variables in supervised
 * classification. 
 * It is specially designed as a previous step for Naive Bayes
 * Some of the procedures have been taked for Grouping class 
 *
 * @author smc@decsai.ugr.es
 * @version 0.1
 * @since 22/03/2006
 */

public class Joining implements Serializable{
        
        static final long serialVersionUID = 3218802236754252918L;    
        
    /* These constants are used to set the joint+group scheme*/
        public static final int SCHEME0    = 0;  // Gropuing + JoinxGrouping
        public static final int SCHEME1    = 1;  // JoinxGrouping + Grouping
        public static final int SCHEME2    = 2;  // (JoingxJoinx.....|Grouping)xJoin... (lazy form)
        public static final int SCHEME3    = 3;  // Recursive (JoinxGroupin)
        public static final int SCHEME4    = 4;  // Joing + Groping
        public static final int SCHEME5    = 5;  // The same that SCHEME1
        public static final int SCHEME6    = 6;  // Only Join
        public static final int SCHEME7    = 7;  // Only Grouping
        public static final int NUM_JOIN_GROUP_SCHEMES =  8;
    
    double maxscoreActual=-Double.MAX_VALUE;
    double maxscoreBefore=-Double.MAX_VALUE;
    
    int mScheme=-1;
    /*If debug information is showed.*/
    public boolean debug=false;

    /*If it is used a threshold to join the variables*/
    public boolean THRESHOLD_JOIN=true;
    
    /*If it is used a threshold to group the states of the variables*/
    public boolean THRESHOLD_GROUPING=true;
    
    /*If states with null frequency are grouped in a unique state*/
    boolean GROUP_NULLFREQUENCY_STATES=true;
    
    /* These constants are used to set the metric grouping method */
        public static final int GNOMETRIC    = -1;
        public static final int GBDeMetric   = 0; 
        public static final int GL1OMetric   = 1; 
        public static final int GPValueChiTest    = 2; 
        public static final int GPValueTTest    = 3; 
        public static final int GNUM_METRICS =  4;

    /* These constants are used to set the joining method */
        public static final int NO_JOINING_ALGORITHM    = -1;
        
        /*Search the best pair of varibles in O(n^2)*/
        public static final int FIRSTSEARCH             = 0; 
        
        /*Search the best pair of varibles in O(n^3)*/    
        public static final int WHOLESEARCH             = 1;
        
        public static final int NUM_JOINING_ALGORITHMS =  3;
   
    /* These constants are used to set the used Metric  */
    public static final int JNOMETRIC    = -1;
    public static final int JBDeMetric    = 0;
    public static final int JL1OMetric    = 1;
    public static final int JPValueChiTest = 2;
    public static final int JPValueTTest = 3;
    public static final int JNUM_METRICS =  4;

    
    public static final String  ErrorMessage[] = {  
        "ERROR: Variable previously configured"                     ,
        "ERROR: Mode not valid"                                     ,
        "ERROR: Operation not valid"                                ,
        "ERROR: Algorith not valid"                                 ,
        "ERROR: Variable out bounds"                                ,
        "ERROR: Type of Variable not compatible or out of bounds"   ,
        "ERROR: Intervals number too low or too high"               ,
        "ERROR: Intervals number incorrect"                         ,
        "ERROR: Variable non-existent"                              ,
        "ERROR: Varaible selection not valid"                       ,
        "ERROR: Intervals number too high"                          };


    /**
     * Default grouping method.
     */

    /* Members variables for configuring the grouping process */
    ;
  
    int             mGroupingAlgorithm                 = GNOMETRIC ;
    int             mJoiningAlgorithm                 = NO_JOINING_ALGORITHM ;

    Vector          mDiscreteVariablesVector   = new Vector()          ;
    DataBaseCases   mCases                                             ;
    DataBaseCases   testCases                                          ;
    Vector          mDBCInformation            = new Vector()          ;

    boolean test=false;
    int         mClassVar;    // index of class variable
    int         mClassVarType=0;  // 0=discrete, 1=continuous     
    Vector      mTargetVariables;  // variables to be joined
    Metrics         metric;
    int nactive; //number of active variables
    
    int             metricType                  = JNOMETRIC;
    
    
    /* This vector is used to store the created groups, in order to can apply again 
       the same grouping procedure in another data base.*/
    Vector groupings=new Vector();

    /* This vector is used to store the joined variables, in order to can apply again 
       the same joining procedure in another data base.*/
    Vector joinvars=new Vector();
    
    
    public Vector<Vector<Double>> scores=new Vector<Vector<Double>>();

    
    
    double[][] scorematrix;
    boolean[][] scoreupdated;
    
    NodeList primarynodes;
    
    boolean[] active;
    
    double alphalevel=0.99;
    
    /** Creates a new instance of Joining */
    public Joining() {
    }
    
    /**
     * Return if the states with null frequency are grouped in a unique state.
     */
    public boolean getGroupNFrequencyStates(){
        return this.GROUP_NULLFREQUENCY_STATES;
    }

    /**
     * Set if the states with null frequency are grouped in a unique state.
     */
    public void setGroupNFrequencyStates(boolean state){
        this.GROUP_NULLFREQUENCY_STATES=state;
    }
 
    public int getMetricJoining(){
        return this.metricType;
    }
    
    public int getGroupingAlgorithm(){
        return mGroupingAlgorithm;
    }
    
    public double getAlphaLevel(){
        return this.alphalevel;
    }

    public void setAlphaLevel(double alpha){
        this.alphalevel=alpha;
    }
    public void setMetricGrouping(int i){
      if ((i>=0) && (i<GNUM_METRICS)) mGroupingAlgorithm=i;
      else System.out.println("\nERROR -> Grouping::setGroupingAlgorithm -> method not available");
    }

    public int getJoiningAlgorithm(){
        return mJoiningAlgorithm;
    }
    

    public void setJoiningAlgorithm(int i){
      //if ((i>=0) && (i<NUM_JOINING_ALGORITHMS)) mJoiningAlgorithm=i;
      //else System.out.println("\nERROR -> Joining:setJoiningAlgorithm -> method not available");
      mJoiningAlgorithm=1;
      mScheme=i;
    }

    public void setMetricJoining(int i){
      switch (i) {
          case JBDeMetric :  
                            metricType=i;
                            metric = new BDeMetrics(mCases);
                            break;
          case JL1OMetric:   
                            metricType=i;
                            metric = new L1OMetrics(mCases);
                            break;
          case JPValueChiTest:   
                            metricType=i;
                            metric = new L1OMetrics(mCases);
                            break;
          case JPValueTTest:           
                            metricType=i;
                            metric = new L1OMetrics(mCases);
                            break;
          default           :  System.out.println("Metric Number  " + 
                i + " does not exist .... exiting \n");
                               System.exit(0);
                               break;               
      }
    }
    
    private void setNewMetric(){
      switch (this.metricType) {
          case JBDeMetric :  metric=new BDeMetrics(mCases);
                            break;
          case JL1OMetric:  metric=new L1OMetrics(mCases);
                            break;
          case JPValueChiTest:  metric=new L1OMetrics(mCases);
                            break;
          case JPValueTTest:  metric=new L1OMetrics(mCases);
                            break;
      }
    }
    
    
    private double getScoreSimple(FiniteStates node1, FiniteStates nodeclass){
        return ((L1OMetrics)metric).scoreJointNBSimpleCond(node1,nodeclass);
    }
    
    
    private double getScoreDep(FiniteStates node1, FiniteStates node2, FiniteStates nodeclass){
        NodeList nodes=null;
      switch (this.metricType) {
          case JBDeMetric :  
                            nodes=new NodeList();
                            nodes.insertNode(nodeclass);
                            return -((BDeMetrics)metric).scoreDep(node1,node2,nodes);
          case JL1OMetric:  
                            return ((L1OMetrics)metric).scoreJointNB(node1,node2,nodeclass,this);

          case JPValueChiTest:
                            nodes=new NodeList();
                            nodes.insertNode(nodeclass);
                            return this.mCases.testValue(node1,node2,nodes);
          case JPValueTTest:
                            return ((L1OMetrics)this.metric).scoreJointNBTTest(node1,node2,nodeclass);
          default:          
                            return -Double.MAX_VALUE; 
      }
      
    }
    
    
    public void setClassVar(int i){
      if (i> mCases.getVariables().size()){
        System.out.println("\n*** Error ***: classVar index (" + i
        +") is greater than number of vars in data base ... exiting");
        System.exit(0);
      }
      
      mClassVar = i-1;
      //setting classVarType      
      if (IsDiscreteVariable(mClassVar)) mClassVarType=0;
      else mClassVarType=1;
    }
    
    public void setTest(boolean val){
        this.test=val;
    }

    public void setTargetVariables(Vector v){
      mTargetVariables = new Vector();
      int i,value;
      String s;
      Integer I;
      Node node;
 
     
      s = (String)v.elementAt(0);

      if (s.equals("list")){
        nactive = v.size()-1;
        for(i=1;i<v.size();i++){
          value = ((Integer)v.elementAt(i)).intValue();
          mTargetVariables.addElement(new Integer(value-1));
        }
      }
      else if (s.equals("all") || s.equals("first-last")){
        nactive = mDiscreteVariablesVector.size()-1;
        for(i=0;i<mDiscreteVariablesVector.size();i++){
          I = (Integer) mDiscreteVariablesVector.elementAt(i);
          if (I.intValue()!=mClassVar) 
            mTargetVariables.addElement(I);
        } /*
        //ClassifierDBC cdbc=new ClassifierDBC(this.mCases,this.mClassVar);
        //Vector order=cdbc.getNumberGNBBasedOrder();
        for(i=0;i<mDiscreteVariablesVector.size();i++){
        //for(i=0;i<order.size();i++){
          I = (Integer) mDiscreteVariablesVector.elementAt(i);
          //I=(Integer) order.elementAt(order.size()-1-i);
          //I=(Integer) order.elementAt(i);
          if (I.intValue()!=mClassVar) 
            mTargetVariables.addElement(I);
        } 
        */
      }
      
      else{
        System.out.println("ERROR: this type of identifying target variables is not allowed ... exiting ");
        System.exit(0);
      }  

   
    }
    
    
    /**
    *  Obtains the number of cases of the database.
    *  @return <code>int</code> Number of cases of the database.
    */
    private int GetNumberOfCases() {
        Vector      DataBaseVector  =   mCases.getRelationList();
        Relation    RelationList    =   (Relation)DataBaseVector.elementAt(0);
        CaseListMem caselistmem     =   (CaseListMem)RelationList.getValues();
        int         NumberOfCases   =   caselistmem.getNumberOfCases();
        return NumberOfCases;
    }

    /**
    *  Method for obtaining the number of variables of the database.
    *  @return <code>int</code> Number of variables of the database.
    */
    private int GetNumberOfVariables() {
        int         i                   ;
        int         NumberOfVariables   ;
        NodeList    Nodes               ;
        
        Nodes=mCases.getVariables(); 
        NumberOfVariables=Nodes.size();
        return      NumberOfVariables;
    }

    /**
    *  Method for obtaining the number of discrete variables of the database.
    *  @return <code>int</code> Number of discrete variables of the database.
    */
    private int GetNumberOfDiscreteVariables() {
      return mDiscreteVariablesVector.size();
    }  
    
    
    
  
    /**
    *  Method for obtaining a selected discrete variable.
    *  @return <code>int</code> Index of the selected discrete variable.
    */
    private int GetDiscreteVariable(int  Index) {
      int variable=((Integer)
           mDiscreteVariablesVector.elementAt(Index)).intValue();
      return  variable;
    }

    
    /**
    *  Method for determining if a variable is discrete or not.
    *  @return <code>boolean</code> Result: true if the variable is discrete
    *          and false if not.
    */
    private boolean IsDiscreteVariable(int Variable) {
        int         i;
        boolean Found;
        Found=false;
        for(i=0;i<GetNumberOfDiscreteVariables();i++) {
            if ( Variable == GetDiscreteVariable(i) ) {
                Found=true;
            }
        }
        return Found;
    }   
    
 
    /**
     * isTarget(Node n)
     * returns the position of n in targetVariables or -1 in other case
     */

    private int isTargetVariable(int p){
      int pos=-1;
      Integer I;

      for(int i=0;i<mTargetVariables.size();i++){
        I = (Integer) mTargetVariables.elementAt(i);
        if (I.intValue() == p) {pos=i;break;}
      }

      return pos;
    }   
 
    /**
    *  Loads the dbc file filename.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    public void loadData(String Filename) throws ParseException, IOException
    {
        FileInputStream File = new FileInputStream(Filename);
        DataBaseCases dbc = new DataBaseCases(File);
        File.close();
        loadData(dbc);
    }    
    /**
    *  Method for loading dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    public void loadData(DataBaseCases dbc) throws ParseException, IOException
    {
        int j;
        mCases = dbc.copy();

        Node        node            ;
        NodeList    nodes           =   mCases.getVariables()              ;
        Vector      vector          =   mCases.getRelationList()           ;
        Relation    relation        =   (Relation)vector.elementAt(0)       ;
        CaseListMem caselistmem     =   (CaseListMem)relation.getValues()   ;

        for (j=0 ; j< nodes.size()  ; j++) {
          node =(Node)(caselistmem.getVariables()).elementAt(j);
         
            if (node.getTypeOfVariable()==Node.FINITE_STATES) {
           
                mDiscreteVariablesVector.add(new Integer(j));
          }
        }

        //mDBCInformation.add ( Filename                                     );
        mDBCInformation.add ( new Integer(nodes.size())                    );
        mDBCInformation.add ( new Integer(caselistmem.getNumberOfCases())  );
        for(j=0 ; j < nodes.size() ; j++ ) {
          node = (Node)(caselistmem.getVariables()).elementAt(j);
          mDBCInformation.add( new String (node.getName()          ) );
          mDBCInformation.add( new Integer(node.getTypeOfVariable()) );
        }

        return;
    }
    /**
    *  Method for loading dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    public void loadData(DataBaseCases dbc1, DataBaseCases dbc2) throws ParseException, IOException
    {
        int j;

        mCases = dbc1.copy();
        testCases = dbc2.copy();

        Node        node            ;
        NodeList    nodes           =   mCases.getVariables()              ;
        Vector      vector          =   mCases.getRelationList()           ;
        Relation    relation        =   (Relation)vector.elementAt(0)       ;
        CaseListMem caselistmem     =   (CaseListMem)relation.getValues()   ;

        for (j=0 ; j< nodes.size()  ; j++) {
          node =(Node)(caselistmem.getVariables()).elementAt(j);
         
            if (node.getTypeOfVariable()==Node.FINITE_STATES) {
           
                mDiscreteVariablesVector.add(new Integer(j));
          }
        }

        //mDBCInformation.add ( Filename                                     );
        mDBCInformation.add ( mCases.getName()                             );
        mDBCInformation.add ( new Integer(nodes.size())                    );
        mDBCInformation.add ( new Integer(caselistmem.getNumberOfCases())  );
        for(j=0 ; j < nodes.size() ; j++ ) {
          node = (Node)(caselistmem.getVariables()).elementAt(j);
          mDBCInformation.add( new String (node.getName()          ) );
          mDBCInformation.add( new Integer(node.getTypeOfVariable()) );
        }

        return;
        
    }

    /**
    *  Method for loading dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    public void loadData(String Filename, String Filename2) throws ParseException, IOException
    {

        FileInputStream File = new FileInputStream(Filename);
        DataBaseCases dbc1 = new DataBaseCases(File);
        File.close();
        File = new FileInputStream(Filename2);
        DataBaseCases dbc2 = new DataBaseCases(File);
        File.close();
        this.loadData(dbc1, dbc2);
    }
   
    
    

    public DataBaseCases getDataBaseCases() {
        return this.mCases;
    } 

    public DataBaseCases getTestDataBaseCases() {
        if (this.test)
            return this.testCases;
        else
            return null;
    } 

    /**
    *  Method for saving dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
   public void saveData(String Filename) throws IOException
    {
        FileWriter  File = new FileWriter(Filename);
        mCases.saveDataBase(File);
        File.close();
        return;
    }


    /**
    *  Method for saving dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    public void saveData(String Filename, String Filename2) throws IOException
    {
        FileWriter  File = new FileWriter(Filename);
        mCases.saveDataBase(File);
        File.close();   
        File = new FileWriter(Filename2);
        testCases.saveDataBase(File);
        File.close();
        return;
    }
 
    public DataBaseCases joinVariablesAgain (int i, int j, DataBaseCases dbc){
        
        int pos1,pos2,Nobserva;
        int k,l;
        FiniteStates node1,node2;
        int ncases1,ncases2,ncases;
        CaseListMem clm;
        int[] case1;
        String[] stat;

        DataBaseCases data=dbc.copy();
        
        pos1 =  ((Integer) mTargetVariables.elementAt(i)).intValue();
        pos2 =  ((Integer) mTargetVariables.elementAt(j)).intValue();

        node1 = (FiniteStates) data.getVariables().elementAt(pos1);
        node2 = (FiniteStates) data.getVariables().elementAt(pos2);

        //Redefine variable i with the product variable
        ncases1 = node1.getNumStates(); 
        ncases2 = node2.getNumStates();
        ncases = ncases1*ncases2;

        //Redefine variable i with the product variable
        node1.setName(joinNameVariables(node1,node2));
        //node1.setTitle(joinTitleVariables(node1,node2));
        
        stat = new String[ncases];
        for(k=0;k<ncases1;k++) {
            for(l=0; l<ncases2; l++) {               
                stat[k*ncases2+l] = getNewNameState(node1.getState(k),node2.getState(l));
            }
        }
        node1.setStates(stat);
       
        clm = (CaseListMem) data.getCases();
        Nobserva = clm.getNumberOfCases();

        for(k=0;k<Nobserva;k++){
            case1 = clm.getCase(k); 
            if (case1[pos1]==-1 || case1[pos2]==-1)
                case1[pos1]=-1;
            else  
                case1[pos1] = case1[pos1]*ncases2 +   case1[pos2];
        } 
        
        return data;
    }
    
    /**
    *  Apply the results of the last used joining method to a DataBaseCases object.
    *  So, the previous joining variables and formed state groups are used again to transform this data base.
    *  @return <code>DataBaseCases</code> the new Data Base.
    */
    public DataBaseCases applyAgain(DataBaseCases dbc) throws InvalidEditException,IOException
    {   
        DataBaseCases data=dbc.copy();
        
        boolean[] active=new boolean[dbc.getVariables().size()];
        for (int i=0; i<active.length ; i++){
            active[i]=true;
        }
        active[this.mClassVar]=true;
        int cont=0;
        Grouping group=null;
        
        if (mScheme==SCHEME0 || mScheme==SCHEME7){//Grouping at the beggining
            group=(Grouping)this.groupings.elementAt(0);
            data=group.applyAgain(data);

            for(int i=0;i<mTargetVariables.size();i++){
                int pos =  ((Integer) mTargetVariables.elementAt(i)).intValue();
                FiniteStates node =(FiniteStates)(data.getVariables()).elementAt(pos);
                if (node.getNumStates() == 1){
                    active[pos] = false;
                }
            }
            cont=1;
        }        
        
        
        for (int i=0; i<this.joinvars.size(); i++){
           
            if ((mScheme==SCHEME2 || mScheme==SCHEME3) && this.joinvars.elementAt(i)==null){
                
                group=(Grouping)this.groupings.elementAt(cont);
                data=group.applyAgain(data);
                cont++;
                for(int k=0;k<mTargetVariables.size();k++){
                    int pos =  ((Integer) mTargetVariables.elementAt(k)).intValue();
                    FiniteStates node =(FiniteStates)(data.getVariables()).elementAt(pos);
                    if (node.getNumStates() == 1){
                        active[pos] = false;
                    }
                }
                 
            }else{
                int[] pair=(int[])this.joinvars.elementAt(i);

                if (pair[0]>=0 && pair[1]>=0){
                    int pos =  ((Integer) mTargetVariables.elementAt(pair[0])).intValue();
                    if (active[pos]){
                            pos =  ((Integer) mTargetVariables.elementAt(pair[1])).intValue();
                            active[pos]=false;
                            data=joinVariablesAgain(pair[0],pair[1],data);
                            if (mScheme==SCHEME0 || mScheme==SCHEME1 || mScheme==SCHEME2 || mScheme==SCHEME3 || mScheme==SCHEME4 || mScheme==SCHEME5 || mScheme==SCHEME6){
                                group=(Grouping)this.groupings.elementAt(cont);
                                data=group.applyAgain(data);
                                cont++;
                            }
                    }
                }else if(pair[0]>=0 && pair[1]==-2){
                    int pos =  ((Integer) mTargetVariables.elementAt(pair[0])).intValue();
                    active[pos]=false;
                }
            }
        }

         if (mScheme==SCHEME1 || mScheme==SCHEME4 || mScheme==SCHEME5){//Grouping at the end
            group=(Grouping)this.groupings.elementAt(cont);
            data=group.applyAgain(data);

            for(int i=0;i<mTargetVariables.size();i++){
                int pos =  ((Integer) mTargetVariables.elementAt(i)).intValue();
                FiniteStates node =(FiniteStates)(data.getVariables()).elementAt(pos);
                if (node.getNumStates() == 1){
                    active[pos] = false;
                }
            }
        }        
        
        NodeList nodes=new NodeList();
        for (int i=0; i<data.getVariables().size(); i++){
            if (active[i])
                nodes.insertNode(data.getVariables().elementAt(i));
        }
        data.projection(nodes);
        
        return data;
    }    

    public int getStates(FiniteStates f){
        try{
        String name=f.getName();
        
        name=name.replace("[","");
        name=name.replace("]","");
        name=name.replace("\"","");
        Pattern p = Pattern.compile(",");
        String[] variables=p.split(name);

        int numstates=1;
        for (int i=0; i<variables.length; i++){
            numstates*=((FiniteStates)primarynodes.elementAt(primarynodes.getId(variables[i]))).getNumStates();
        }
        return numstates;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
            return 0;
        }

        
    }
    
    public NodeList getNodes(FiniteStates f){
        try{
            NodeList nodes=new NodeList();
        String name=f.getName();
        
        name=name.replace("[","");
        name=name.replace("]","");
        name=name.replace("\"","");
        Pattern p = Pattern.compile(",");
        String[] variables=p.split(name);

        int numstates=1;
        for (int i=0; i<variables.length; i++){
            nodes.insertNode(((FiniteStates)primarynodes.elementAt(primarynodes.getId(variables[i]))));
        }
        return nodes;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
        
    }
    public boolean isDisjoint(FiniteStates f1, FiniteStates f2){
        NodeList nodes1=this.getNodes(f1);
        return nodes1.difference(this.getNodes(f2)).size()==nodes1.size();
    }

    /**
    *  Apply a joining method to a DataBaseCases object. Replacing
     *  the joined variables for a new variable.
     * A grouping is applyied before and after the algorithm.
     * The method for grouping is Bayes method 
    *   The orgininal datebase cases is transformed.
     * It is applied while there is variables to be joined.
    */
    public void selfapply() throws InvalidEditException,IOException
    {   
        
        FiniteStates node;
        Grouping group;
      
        Vector interest; 
     
        

        int pos;
        int totalvar,i,j;        
//Data strucutures to go through values and vars
        
        primarynodes=this.getDataBaseCases().getVariables().copy();
        
        this.groupings=new Vector();
        this.joinvars=new Vector();
        
        totalvar = mCases.getVariables().size();
        
        active = new boolean[totalvar];
        
          for(i=0;i<totalvar;i++){
               active[i] = true;
          }

         /*********************/
         this.scorematrix=new double[totalvar][totalvar];
         this.scoreupdated=new boolean[totalvar][totalvar];
        for(i=0;i<totalvar;i++){
             for(j=0;j<totalvar;j++){
                this.scoreupdated[i][j] = false;
             }
          }
        
        /**********************/
        
        System.out.println("\n------------------------------------------------------");
        System.out.println("-------------- Beginning Joining  Process ------------");
        System.out.println("------------------------------------------------------");
   
        


        //if (debug) System.out.println("\n------------------------------------------------------");
        //if (debug) System.out.println("-------------- First Grouping is Applied ------------");
        //if (debug) System.out.println("------------------------------------------------------");
        
        if (mScheme==SCHEME0 || mScheme==SCHEME7){//Grouping at the beggining
            group = new Grouping(this);
            group.selfapply(); 
            group.clean();
            this.groupings.addElement(group);
            for(i=0;i<mTargetVariables.size();i++){
                pos =  ((Integer) mTargetVariables.elementAt(i)).intValue();
                node =(FiniteStates)(mCases.getVariables()).elementAt(pos);
                if (node.getNumStates() == 1){
                    active[pos] = false;
                    nactive--; 
                }
            }
        }

  if (mScheme!=SCHEME7){
        System.out.println("Number of active variables:  "+nactive);
        
        System.out.println("\n------------------------------------------------------");
        System.out.println("-------------- Starting Joining Variables ------------");
        System.out.println("------------------------------------------------------"); 
        
        i=0;
        boolean end=false;
        boolean next=false;
        boolean change=false;
        
        while (!end)
        {

        //if (debug) System.out.println("\n------------------------------------------------------");
        //if (debug) System.out.println("-------------- Computing two variables to join ------------");
        //if (debug) System.out.println("-------------- First variable: " + i + "--------------  " );   
        //if (debug) System.out.println("----------------------------------------------    --------");  
        
        int[] found=new int[2];
      
           switch (mJoiningAlgorithm){  
               case Joining.FIRSTSEARCH: 
                   if (active[((Integer) mTargetVariables.elementAt(i)).intValue()]){
                        found = variablestojoin(active,i);
                   }else{
                       found[0]=i;
                       found[1]=-2;
                   }
                   if (found[1]<0){
                       int[] pair=new int[2];
                       pair[0]=found[0];
                       pair[1]=found[1];
                       this.joinvars.addElement(pair);
                       i++;
                       next=true;
                       if (i>=this.mTargetVariables.size() || nactive==1)
                           end=true;
                    }else{
                       next=false;
                    }
                        
                   break;
               case Joining.WHOLESEARCH: 
                   found = variablestojoinAll(active);
                   if (found[0]==-1 || found[1]==-1) {
                           int[] pair=new int[2];
                           pair[0]=-1;
                           pair[1]=-1;
                           this.joinvars.addElement(pair);
                           next=true;
                           end=true;
                   }else
                           next=false;
                   break;
               case 2:
                   found = variablestojoinAllNew(active);
                   if (found[0]==-1 || found[1]==-1) {
                           int[] pair=new int[2];
                           pair[0]=-1;
                           pair[1]=-1;
                           this.joinvars.addElement(pair);
                           next=true;
                           end=true;
                   }else
                           next=false;
                   break;
               default:  
                   System.out.println("Algorithm number " + 
                   mJoiningAlgorithm + " does not exist .... exiting \n");
                   System.exit(0);
                   break;   
           }
           
           
           if (!next){
               change=true;
               System.out.println("Joining variables: ("+((Integer) mTargetVariables.elementAt(found[0])).intValue()+") "+ this.mCases.getVariables().elementAt(((Integer) mTargetVariables.elementAt(found[0])).intValue()).getName()+ "-- (" +((Integer) mTargetVariables.elementAt(found[1])).intValue()+") "+ this.mCases.getVariables().elementAt(((Integer) mTargetVariables.elementAt(found[1])).intValue()).getName());
               
               joinVariables(found[0],found[1], active);
               nactive--;

               int[] pair=new int[2];
               pair[0]=found[0];
               pair[1]=found[1];
               this.joinvars.addElement(pair);
               System.out.println("Number of active variables:  "+nactive);
                
               if (mScheme==SCHEME0 || mScheme==SCHEME1 || mScheme==SCHEME3 || mScheme==SCHEME5){
                   //if (debug) System.out.println("Grouping resulting  variable " + i);
                   interest = new Vector();
                   interest.addElement( mTargetVariables.elementAt(found[0])     );
                   group = new Grouping(this);
                   group.setTarget(interest); 
                   group.selfapply();
                   group.clean();
                   this.groupings.addElement(group);
               }
               if (mScheme==SCHEME2 || mScheme==SCHEME4 || mScheme==SCHEME6){
                   //if (debug) System.out.println("Grouping resulting  variable " + i);
                   interest = new Vector();
                   interest.addElement( mTargetVariables.elementAt(found[0])     );
                   group = new Grouping(this);
                   group.setTarget(interest); 
                   group.setAlgorithm(Grouping.NULLFREQUENCY_GROPUING);
                   group.selfapply();
                   group.clean();
                   this.groupings.addElement(group);

               }               
            }
            if (end && (mScheme==SCHEME2 || mScheme==SCHEME3) && change){
                
                for (int k=0; k<this.joinvars.size(); k++){
                    if (this.joinvars.elementAt(k)!=null){
                        int[] pair=(int[])this.joinvars.elementAt(k);
                        if (pair[1]>=0){
                            active[pair[0]]=true;
                            active[pair[1]]=false;
                        }
                    }
                }
                
               
                this.joinvars.addElement(null); 
                change=false;
                end=false;
                i=0;
                
                
                interest = new Vector();
                for(int k=0;k<mTargetVariables.size();k++){
                    pos =  ((Integer) mTargetVariables.elementAt(k)).intValue();
                    if (active[pos])
                        interest.addElement(mTargetVariables.elementAt(k));
                }
                group = new Grouping(this);
                group.setTarget(interest); 
                group.selfapply(); 
                group.clean();
                this.groupings.addElement(group);
                for(int k=0;k<mTargetVariables.size();k++){
                    pos =  ((Integer) mTargetVariables.elementAt(k)).intValue();
                    if (active[pos]){
                        node =(FiniteStates)(mCases.getVariables()).elementAt(pos);
                        if (node.getNumStates() == 1){
                            active[pos] = false;
                            nactive--; 
                        }
                    }
                }
                
            }
        }
 
         if (mScheme==SCHEME1 || mScheme==SCHEME4 || mScheme==SCHEME5){//Grouping at the end
            interest = new Vector();
            for(int k=0;k<mTargetVariables.size();k++){
                pos =  ((Integer) mTargetVariables.elementAt(k)).intValue();
                if (active[pos])
                    interest.addElement(mTargetVariables.elementAt(k));
            }
            group = new Grouping(this);
            group.setTarget(interest); 
            group.selfapply(); 
            group.clean();
            this.groupings.addElement(group);
            for(int k=0;k<mTargetVariables.size();k++){
                pos =  ((Integer) mTargetVariables.elementAt(k)).intValue();
                if (active[pos]){
                    node =(FiniteStates)(mCases.getVariables()).elementAt(pos);
                    if (node.getNumStates() == 1){
                        active[pos] = false;
                        nactive--; 
                    }
                }
            }
        }
    }
        System.out.println("\n------------------------------------------------------");
        System.out.println("-------------- Compacting resulting database ------------");
        System.out.println("------------------------------------------------------"); 
        
 
               
                compact(active,mCases);
               
              if (test)  {
                   compact(active,testCases);
              }

        System.out.println("\n------------------------------------------------------");
        System.out.println("-------------- Joining Process Finalized ------------");
        System.out.println("------------------------------------------------------");
        System.out.println("Number of final variables: "+this.mCases.getVariables().size()+" .");    
    }    


    /*******************************************************************
    * This procedure removes non-active variables from the database cases.
    
    ************************************************************************/
    
    private void compact (boolean[] active, DataBaseCases db){
        NodeList nodes=new NodeList();
        for (int i=0; i<active.length; i++)
            if (active[i])
                nodes.insertNode(db.getVariables().elementAt(i));
        db.projection(nodes);
    }
    
    
    
    /**
     * Joins two target variables (at positions i and j in Vector mTargetVariables)
     * into only one variable. The result replaces variable i, and variable j is set to
     * non-active in the array active.
     * @param i position in mTargetVariables of the first variable to be joined
     * @param j position in mTargetVariables of the second variable to be joined
     * @param active an array of boolean with the active positions (variables) in mTargetVariables
     */
    
    private void joinVariables (int i, int j, boolean[] active){
        int pos1,pos2,Nobserva;
        int k,l;
        FiniteStates node1,node2;
        int ncases1,ncases2,ncases;
        CaseListMem clm;
        int[] case1;
        String[] stat;

        pos1 =  ((Integer) mTargetVariables.elementAt(i)).intValue();
        pos2 =  ((Integer) mTargetVariables.elementAt(j)).intValue();
        active[pos2] = false;
           
        
        /** Saving the variables to join **/
        


        /**********************************/
        node1 = (FiniteStates) mCases.getVariables().elementAt(pos1);
        node2 =  (FiniteStates) mCases.getVariables().elementAt(pos2);

        ncases1 = node1.getNumStates(); ncases2 = node2.getNumStates();
        ncases = ncases1*ncases2;

        //System.out.println("Joined Variabels: "+ncases+" : "+ncases1+" : "+ncases2);
        //Redefine variable i with the product variable
        node1.setName(joinNameVariables(node1,node2));
        //node1.setTitle(joinTitleVariables(node1,node2));

        stat = new String[ncases];
        for(k=0;k<ncases1;k++) {
            for(l=0; l<ncases2; l++) {               
                stat[k*ncases2+l] = getNewNameState(node1.getState(k),node2.getState(l));
            }
        }
        node1.setStates(stat);
              
          
            
        //Set new values of product variable


        clm = (CaseListMem) mCases.getCases();
        Nobserva = clm.getNumberOfCases();


        double value;

        for(k=0;k<Nobserva;k++){
            case1 = clm.getCase(k); 
            if (case1[pos1]==-1 || case1[pos2]==-1)
                case1[pos1]=-1;
            else
                case1[pos1] = case1[pos1]*ncases2 +   case1[pos2];   
        } 
         
         if (test){
            //testCases.getVariables().setElementAt(node1,pos1);
            node1 = (FiniteStates) testCases.getVariables().elementAt(pos1);
            Vector stat2=((FiniteStates)mCases.getVariables().elementAt(pos1)).getStates();
            ((FiniteStates)node1).setStates((Vector)stat2.clone());
            
            clm = (CaseListMem) testCases.getCases();
            Nobserva = clm.getNumberOfCases();

            for(k=0;k<Nobserva;k++){
                case1 = clm.getCase(k); 
                if (case1[pos1]==-1 || case1[pos2]==-1)
                    case1[pos1]=-1;
                else
                    case1[pos1] = case1[pos1]*ncases2 +   case1[pos2];   
            } 
         }
    }
/*
    private void joinVariablesNew (int i, int j){
        int pos1,pos2,Nobserva;
        int k,l;
        FiniteStates node1,node2;
        int ncases1,ncases2,ncases;
        CaseListMem clm;
        int[] case1;
        String[] stat;

        pos1 =  ((Integer) mTargetVariables.elementAt(i)).intValue();
        pos2 =  ((Integer) mTargetVariables.elementAt(j)).intValue();
        active[pos2] = false;


        node1 = (FiniteStates) mCases.getVariables().elementAt(pos1);
        node2 =  (FiniteStates) mCases.getVariables().elementAt(pos2);

        ncases1 = node1.getNumStates(); ncases2 = node2.getNumStates();
        ncases = ncases1*ncases2;

        System.out.println("Joined Variabels: "+ncases+" : "+ncases1+" : "+ncases2);
        //Redefine variable i with the product variable
        String newname=joinNameVariables(node1,node2);
        //node1.setTitle(joinTitleVariables(node1,node2));

        stat = new String[ncases];
        for(k=0;k<ncases1;k++) {
            for(l=0; l<ncases2; l++) {               
                stat[k*ncases2+l] = getNewNameState(node1.getState(k),node2.getState(l));
            }
        }
        
        this.mCases=this.mCases.joinVariables2(pos1,pos2);

        node1 = (FiniteStates) mCases.getVariables().elementAt(pos1);
        node1.setName(newname);
        node1.setStates(stat);
        
        
        boolean[] active2=new boolean[active.length+1];
        for (k=0; k<active.length; k++)
            active2[k]=active[k];
        active2[active.length]=false;
        active=active2;
        
        double[][] newscorematrix=new double[this.scorematrix.length+1][this.scorematrix.length+1];
        boolean[][] newscoreupdated=new boolean[this.scorematrix.length+1][this.scorematrix.length+1];
        
        for (k=0; k<this.scorematrix.length; k++){
            for (l=0; l<this.scorematrix.length; l++){
                newscorematrix[k][l]=this.scorematrix[k][l];
                newscoreupdated[k][l]=this.scoreupdated[k][l];
            }
        }
        for (k=0; k<this.scorematrix.length; k++){
            newscorematrix[k][this.scorematrix.length]=newscorematrix[k][i];
            if (k!=i)
                newscoreupdated[k][this.scorematrix.length]=true;
            else
                newscoreupdated[k][this.scorematrix.length]=false;
            
            newscorematrix[this.scorematrix.length][k]=newscorematrix[i][k];
            if (k!=i)
                newscoreupdated[this.scorematrix.length][k]=true;
            else
                newscoreupdated[this.scorematrix.length][k]=false;

        }
        this.scorematrix=newscorematrix;
        this.scoreupdated=newscoreupdated;

        primarynodes=this.getDataBaseCases().getVariables().copy();     
        this.mTargetVariables.addElement(this.getDataBaseCases().getVariables().size()-1);
    }
*/
    /**
     * Concats the names of the states of two variables
     * @param state1 The name of the state of the first variable
     * @param state2 The name of the state of the second variable
     * @return the concatenation of the names of the two variables
     */
    private String getNewNameState(String state1,String state2){
        if(state1.charAt(0)=='\"') // eliminate quotes
            state1=state1.substring(1,state1.length()-1);
        if(state2.charAt(0)=='\"') // eliminate quotes
            state2=state2.substring(1,state2.length()-1);        
        return new String("\"["+state1 + "," + state2+"]\"");
    }
    
    /**
     * Joins the name of the variables node1 and node2 with "," between them and square brackets
     * at begin and end of the new name
     * @param node1 first variable
     * @param node2 second variable
     * @return the joint of the names of node1 and node2
     */
    private String joinNameVariables(Node node1, Node node2){
        String newName=null;
        String oldName1=node1.getName();
        String oldName2=node2.getName();
        if (oldName1.charAt(0)=='\"') // eliminate quotes
            oldName1=oldName1.substring(1,oldName1.length()-1);
        if (oldName2.charAt(0)=='\"') // eliminate quotes
            oldName2=oldName2.substring(1,oldName2.length()-1);
        newName="\"["+oldName1+","+oldName2+"]\"";
        return newName;
    }
    
    /**
     * Joins the title of the variables node1 and node2 with "," between them and square brackets
     * at begin and end of the new name
     * @param node1 first variable
     * @param node2 second variable
     * @return the joint of the titles of node1 and node2
     */
    private String joinTitleVariables(Node node1, Node node2){
        String newName=null;
        String oldName1=node1.getTitle();
        String oldName2=node2.getTitle();
        if (oldName1.charAt(0)=='\"') // eliminate quotes
            oldName1=oldName1.substring(1,oldName1.length()-1);
        if (oldName2.charAt(0)=='\"') // eliminate quotes
            oldName2=oldName2.substring(1,oldName2.length()-1);
        newName="["+oldName1+","+oldName2+"]";
        if (debug) System.out.println("Nuevo title="+newName);
        return newName;
    }
    private double combinatorial(int n, int m){
        //return factorial(n)/(factorial(n-m)*factorial(m));
        if (m>n)
            return 1;
        double prod=1.0;
        for (int i=1; i<=m; i++){
            prod*=(n-m+i)/(double)(m+1-i);
        }
        return prod;
    }
    private int[] variablestojoin(boolean active[],int start){
        
        int i,j,k,pos;
        boolean found;
        FiniteStates node1,node2,nodeclass;
        double max,value;
        NodeList conditional;
        
        int[] indexmax=new int[2];
        indexmax[0]=start;
        indexmax[1]=-1;
        
        nodeclass = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
         
        conditional = new NodeList();
        conditional.insertNode(nodeclass);
        double t2=0,t1=0; 
        double threshold=0.0;
        if (nactive >2 && this.THRESHOLD_JOIN){
            threshold=Math.log(0.57721566490153286061+Math.log((nactive)*(nactive-1.0)/2.0));
            //if (debug)
            //threshold=Math.log(0.57721566490153286061+Math.log(nactive));
            
        }
        max = threshold;
         double acum=0.0;
         setNewMetric();
         pos =  ((Integer) mTargetVariables.elementAt(start)).intValue();
         if (active[pos]){
             node1 = (FiniteStates) mCases.getVariables().elementAt(pos);
             //if (debug)
             //    max=-Double.
             for (i=start+1; i <mTargetVariables.size(); i++ ){
                 int pos2 =  ((Integer) mTargetVariables.elementAt(i)).intValue();
                 
                 if (active[pos2]) {
                     node2 = (FiniteStates) mCases.getVariables().elementAt(pos2);
                     value =  this.getScoreDep(node1,node2,nodeclass);//- ((BDeMetrics) metric).scoreDep(node1,node2,conditional);
                     acum+=value;
                     if (value>max) {
                          max = value;
                          indexmax[1] = i;
                      }
                 }
             }
         }
         return(indexmax);
    }

    //Para union
    private int[] variablestojoin2(boolean active[],int start){
        
        int i,j,k,pos;
        boolean found;
        FiniteStates node1,node2,nodeclass;
        double max,value;
        NodeList conditional;
        
        int[] indexmax=new int[2];
        indexmax[0]=start;
        indexmax[1]=-1;
        
        nodeclass = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
         
        conditional = new NodeList();
        conditional.insertNode(nodeclass);
        double t2=0,t1=0; 
        double threshold=0.0;
        if (nactive >2 && this.THRESHOLD_JOIN){
            threshold=Math.log(0.57721566490153286061+Math.log((nactive)*(nactive-1.0)/2.0));
        }
        max = threshold;
         value=0.0;
         metric = new L1OMetrics(mCases);
         
         pos =  ((Integer) mTargetVariables.elementAt(start)).intValue();
         if (active[pos]){
             node1 = (FiniteStates) mCases.getVariables().elementAt(pos);
             double tmp1 = ((L1OMetrics) metric).scoreJointNBSimpleCond(node1,nodeclass);
             if (value>max) {
                  max = value;
                  active[pos]=false;
                  indexmax[1] = -2;
              }
          }
          return(indexmax);
    }
    
    private int[] variablestojoinAll(boolean active[]){
        
        int i,j,k,pos1,pos2;
        boolean found;
        FiniteStates node1,node2,nodeclass,node1M=null,node2M=null;
        double max,value,pvalue,threshold;
        NodeList conditional;
        
        int[] indexmax=new int[2];
        indexmax[0]=-1;
        indexmax[1]=-1;
         
        nodeclass = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
         
        conditional = new NodeList();
        conditional.insertNode(nodeclass);
         
        max = 0.0;
        threshold=0.0;

        for (i=1; i<=(nactive*(nactive-1)/2); i++){
            threshold+=1/(double)i;
        }
        
        if (nactive >2 && this.THRESHOLD_JOIN){
                if (this.metricType==this.JBDeMetric) max=Math.log(threshold);
                if (this.metricType==this.JL1OMetric) max=threshold;
        }
         
         setNewMetric();
         for (int start=0; start<mTargetVariables.size(); start++){
            pos1 =  ((Integer) mTargetVariables.elementAt(start)).intValue();
            if (active[pos1]){
                 node1 = (FiniteStates) mCases.getVariables().elementAt(pos1);
                 for (i=start+1; i <mTargetVariables.size(); i++ ){
                     pos2 =  ((Integer) mTargetVariables.elementAt(i)).intValue();
                     if (active[pos2]) {
                         node2 = (FiniteStates) mCases.getVariables().elementAt(pos2);
                         if (!this.scoreupdated[pos1][pos2]){
                            value =  this.getScoreDep(node1,node2,nodeclass);//- ((BDeMetrics) metric).scoreDep(node1,node2,conditional);
                            this.scorematrix[pos1][pos2]=value;
                            this.scoreupdated[pos1][pos2]=true;
                         }else{
                             value = this.scorematrix[pos1][pos2];
                         }
                         
                         if (this.metricType==this.JPValueChiTest || this.metricType==this.JPValueTTest){
                                if (value<(1-this.alphalevel/threshold))
                                    value=-1.0; //No Join the variables
                         }
                         
                         if (value>max) {
                              max = value;
                              indexmax[0] = start;
                              indexmax[1] = i;
                          }
                     }
                 }
            }
         }
         
         if (indexmax[1]!=-1){
             pos1=((Integer) mTargetVariables.elementAt(indexmax[0])).intValue();
             for (i=0; i<this.scoreupdated.length; i++)
                this.scoreupdated[pos1][i]=false;
             for (i=0; i<this.scoreupdated.length; i++)
                this.scoreupdated[i][pos1]=false;
         }
         
         return(indexmax);
    }
   
    private int[] variablestojoinAllNew(boolean active[]){
        
        int i,j,k,pos1,pos2;
        boolean found;
        FiniteStates node1,node2,nodeclass;
        double max,value,threshold;
        NodeList conditional;
        
        int[] indexmax=new int[2];
        indexmax[0]=-1;
        indexmax[1]=-1;
         
        nodeclass = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
         
        conditional = new NodeList();
        conditional.insertNode(nodeclass);

        threshold=0.0;
        for (i=1; i<=mTargetVariables.size(); i++){
            threshold+=1/(double)i;
        }
         
        max = 0.0;
        if (nactive >2 && this.THRESHOLD_JOIN){
            max=Math.log(0.57721566490153286061+Math.log((nactive)*(nactive-1.0)/2.0));
            max=Math.log(0.57721566490153286061+Math.log((this.primarynodes.size())*(this.primarynodes.size()-1.0)/2.0));
            
        }
         
         setNewMetric();
         for (int start=0; start<mTargetVariables.size(); start++){
                 pos1 =  ((Integer) mTargetVariables.elementAt(start)).intValue();
                 node1 = (FiniteStates) mCases.getVariables().elementAt(pos1);

                 for (i=start+1; i <mTargetVariables.size(); i++ ){
                     pos2 =  ((Integer) mTargetVariables.elementAt(i)).intValue();
                     if (active[pos1] || active[pos2]) {
                         node2 = (FiniteStates) mCases.getVariables().elementAt(pos2);
                         if (!this.scoreupdated[pos1][pos2]){
                            value =  this.getScoreDep(node1,node2,nodeclass);//*Math.sqrt((node1.getNumStates()-1)*(node2.getNumStates()-1)*(nodeclass.getNumStates()-1))/this.mCases.getNumberOfCases();
                            this.scorematrix[pos1][pos2]=value;
                            this.scoreupdated[pos1][pos2]=true;
                         }else{
                             value = this.scorematrix[pos1][pos2];
                         }
                         /*
                         if (!active[pos1])
                             value-=((L1OMetrics)metric).scoreJointNBSimple(node1,nodeclass);

                         if (!active[pos2])
                             value-=((L1OMetrics)metric).scoreJointNBSimple(node2,nodeclass);
                         */
                         /*
                         if ((this.mCases.testValue(node1,node2,conditional))<(1-this.alphalevel/threshold))
                            value=-1.0;
                         */
                         
                         if (!this.isDisjoint((FiniteStates)this.mCases.getVariables().elementAt(pos1),(FiniteStates)this.mCases.getVariables().elementAt(pos2)))
                            value=-Double.MAX_VALUE;

                         if (this.metricType==this.JPValueChiTest || this.metricType==this.JPValueTTest){
                                if (value<(1-this.alphalevel))//threshold))
                                    value=-1.0; //No Join the variables
                         }
                         
                         if (value>max) {
                              max = value;
                              indexmax[0] = start;
                              indexmax[1] = i;
                          }
                     }
                 }
                 
         }
         
         if (indexmax[1]!=-1){
             pos1=((Integer) mTargetVariables.elementAt(indexmax[0])).intValue();
             for (i=0; i<this.scoreupdated.length; i++)
                this.scoreupdated[pos1][i]=false;
             for (i=0; i<this.scoreupdated.length; i++)
                this.scoreupdated[i][pos1]=false;
         }
             
         
         return(indexmax);
    }

    /**
    * For performing tests 
    */
    public static void main(String args[]) throws ParseException, IOException, InvalidEditException
    {
        Vector    myVector;
        Joining  myJoining;

        if ( args.length == 0 ) {
          System.out.print("USAGE:  <program> <input file.dbc> <output file.dbc> <input test.dbc|none> <output test.dbc|none>"); 
          System.out.println(" <algorithm> <metric> <class> \n\t <all|more numbins|list v1 v2 ...>"); // <intervals> <options>");
          System.out.println("\n<algorithm> :");
          System.out.println("Grouping Algorithm: 0 => Bayesian Score                             ");
             System.out.println("Metric: 0 => Bayesian Dirichlet Equivalent                             ");
          System.out.println("\n<class> : the index of the class variable (1,2,...");
          System.out.println("\n<target> : all or list followed by the indexes");
        } else {
          if ( args.length < 8){ 
            System.out.println("Run the program without parameters to get help");
            System.exit(0);
          }
          //myVector = new Vector();
          //if ( Integer.valueOf(args[2]).intValue() == 2 ) {
          //  myVector.add( new Double(Double.valueOf(args[4]).doubleValue() ));
          //}          

          int algorithm = Integer.valueOf(args[4]).intValue();
          int classIndex = Integer.valueOf(args[6]).intValue();
          DataBaseCases dbc=null;
          if (classIndex==-1){
              dbc=new DataBaseCases(new FileInputStream(args[0]));
              classIndex=dbc.getVariables().size();
          }
          
          
          int metricIndex = Integer.valueOf(args[5]).intValue();
          
          Vector target = new Vector();
          target.addElement(args[7]);
          for(int i=8;i<args.length;i++)
            target.addElement(new Integer(Integer.valueOf(args[i]).intValue()));
         
 
          System.out.println("\nEl programa ha sido llamado con los siguientes parametros:");
          System.out.println("\tinput: " + args[0]);
          System.out.println("\toutput: " + args[1]);
             System.out.println("\ttestinput: " + args[2]);
          System.out.println("\ttestoutput: " + args[3]);
          System.out.println("\talgorithm: " + args[4]);
          System.out.println("\tclass: " + classIndex);
          System.out.print("\ttarget: ");
          System.out.print((String)(target.elementAt(0)) + " ");
          for(int i=1;i<target.size();i++) 
            System.out.print( ((Integer)target.elementAt(i)).intValue() + " ");
          System.out.println();

  

          myJoining = new Joining( );
           if (args[2].equals("none") ) {myJoining.test = false;}
          else {myJoining.test=true;}
          
          if (myJoining.test) {myJoining.loadData(args[0],args[2]);}
          else {myJoining.loadData(args[0]);}
          myJoining.setJoiningAlgorithm(algorithm);
          myJoining.setClassVar(classIndex);
      myJoining.setTargetVariables(target);
          myJoining.setMetricJoining(metricIndex);
          myJoining.selfapply();

          //myGrouping.SetMode(DISCRETIZE_INDIVIDUALLY);
          //myGrouping.SetOperation(MASSIVE_OPERATION);
          //myGrouping.ConfigureIndividual(Integer.valueOf(args[2]).intValue(),Integer.valueOf(args[3]).intValue(),myVector);

        if (myJoining.test) {
              myJoining.saveData(args[1],args[3]);
        }else {
               myJoining.saveData(args[1]);
        }
        
        }

        return;
    }//End main 
    
    
    
    
}
