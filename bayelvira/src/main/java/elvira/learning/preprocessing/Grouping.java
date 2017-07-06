/* Grouping.java */
package elvira.learning.preprocessing;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.potential.*;
import elvira.learning.preprocessing.*;
import elvira.parser.ParseException;
import elvira.tools.Chi2;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.supervised.mixed.Gaussian_Naive_Bayes;
import elvira.tools.statistics.analysis.Stat;

import java.io.*;
import java.lang.Runtime;
import java.lang.Math;
import java.util.*;

/**
 *
 * Implements several grouping methods for nominal/categorical variables
 * with many outcomes/states
 * It is possible to select the target variables as well as the algorithm
 *
 * @author jose.gamez@uclm.es
 * @version 0.1
 * @since 13/04/2004
 */
public class Grouping implements Serializable{
    
    static final long serialVersionUID = 7474661873080226491L;    
    
    /*If debug information is showed.*/
    public boolean debug=false;

    /*If it is used a threshold to group the states of the variables*/
    public boolean THRESHOLD_GROUPING=true;

    /*If states with null frequency are grouped in a unique state*/
    boolean GROUP_NULLFREQUENCY_STATES=true;

    /* These constants are used to set the grouping method */
    public static final int NO_GROUPING_ALGORITHM   = -1;
    public static final int KEX_GROUPING        =  0;
    public static final int KEX2_GROUPING       =  1;
    public static final int BAYES_GROUPING              =  2;
    public static final int BAYES2_GROUPING              =  3;
    public static final int BAYESORDER_GROUPING              =  4;
    public static final int NULLFREQUENCY_GROPUING      =5;
    public static final int NUM_GROUPING_ALGORITHMS =  6;

    /* These constants are used for configuring the grouping */
    public static final int NO_GROUPING                     = 0;
    public static final int GROUPING_GLOBALLY               = 1;
    public static final int GROUPING_INDIVIDUALLY           = 2;

    /* These constants are used for setting operation in 
       individual grouping */
    public static final int NO_GROUPING_OPERATION           = 0;
    public static final int NORMAL_GROUPING_OPERATION       = 1;
    public static final int MASSIVE_GROUPING_OPERATION      = 2;
    
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
    int             mMode                      = NO_GROUPING         ;
    int             mOperation                 = NO_GROUPING_OPERATION ;
    int             mAlgorithm                 = BAYES2_GROUPING;//NO_GROUPING_ALGORITHM ;
    Vector          mOptions                   = new Vector()          ;
    Vector          mConfiguration             = new Vector()          ;

    Vector          mDiscreteVariablesVector   = new Vector()          ;
    DataBaseCases   mCases                                             ;
    DataBaseCases   testCases                                          ;
    DataBaseCases   mGroupedCasesDB                                    ;
    DataBaseCases   testGroupedCasesDB                                    ;
    Vector          mDBCInformation            = new Vector()          ;

    boolean test=false;
    int         mClassVar;    // index of class variable
    int         mClassVarType=0;  // 0=discrete, 1=continuous     
    Vector      mTargetVariables;  // variables to be grouped
    int         mNumBins; // -1 is it has to be discovered    
    Vector          mTargetNumBins; // store the number of bins for each target variable

    /* This vector is used to store the created groups, in order to can apply again 
       the same grouping procedure in another data base.*/
    Vector groupsDefinition=null;
    
    int metricType=1;
    Metrics metric=null;
    
    
    double alphalevel=0.99;
    /**
    *  Basic Constructor
    */
    public Grouping( ) {  }


    /**
    *  Constructor from a Joining Problem
    */
    public Grouping(Joining joinproblem ) {
        int i;
    mCases = joinproblem.mCases;
    mClassVar = joinproblem.mClassVar;
    mClassVarType = joinproblem.mClassVarType;
    mTargetVariables = joinproblem.mTargetVariables;
      setNumBins(-1);
   
    
    this.alphalevel=joinproblem.getAlphaLevel();
    
      this.GROUP_NULLFREQUENCY_STATES=joinproblem.getGroupNFrequencyStates();
    this.THRESHOLD_GROUPING=joinproblem.THRESHOLD_GROUPING;
    
    this.debug=joinproblem.debug;
    test = joinproblem.test;
    if(test) { testCases = joinproblem.testCases;}
    mTargetNumBins = new Vector();
     
       for(i=0;i<mTargetVariables.size();i++) 
        mTargetNumBins.addElement(new Integer(mNumBins));
    
        this.setMetric(joinproblem.mGroupingAlgorithm);
    /*
        switch (joinproblem.mGroupingAlgorithm){
         case Joining.BAYESIAN : this.setMetric(this.BDeMetric);//setAlgorithm(this.BAYES2_GROUPING);
         break;
         case Joining.BAYESIAN2 : this.setMetric(this.L1OMetric);//setAlgorithm(this.BAYES_GROUPING);
         break;
         case Joining.BAYESIAN3 : this.setMetric(2);//setAlgorithm(this.BAYES_GROUPING);                                
         break;
         case Joining.BAYESIAN4 : this.setMetric(3);//setAlgorithm(this.BAYES_GROUPING);                                
         break;

         default           :  System.out.println("Non parameters are defined for grouping with " +
                                   "this joining algorithm: " + 
                joinproblem.mGroupingAlgorithm + "  .... exiting \n");
                               System.exit(0);
                               break;    
    
    }
*/

    }

    public double getAlphaLevel(){
        return this.alphalevel;
    }

    public void setAlphaLevel(double alpha){
        this.alphalevel=alpha;
    }
    
    public void setData(DataBaseCases data){
        this.mCases=data;
    }
    public DataBaseCases getData(){
        return this.mCases;
    }
    public void clean(){
        this.mCases=null;
        this.mGroupedCasesDB=null;
        this.testCases=null;
        this.testGroupedCasesDB=null;
    }
    public int getAlgorithm(){
        return mAlgorithm;
    }
    public void setAlgorithm(int i){
      if ((i>=0) && (i<NUM_GROUPING_ALGORITHMS)) mAlgorithm=i;
      else System.out.println("\nERROR -> Grouping::setAlgorithm -> method not available");
        
    }

    
    
    
    public void setMetric(int i){
      
        switch (i) {
          case Joining.GBDeMetric:  
                            metricType=i;
                            metric = new BDeMetrics(mCases);
                            break;
          case Joining.GL1OMetric:   
                            metricType=i;
                            metric = new L1OMetrics(mCases);
                            break;
          case Joining.GPValueChiTest:
                            metricType=i;
                            metric = new L1OMetrics(mCases);
                            break;
          case Joining.GPValueTTest:
                            metricType=i;
                            metric = new L1OMetrics(mCases);
                            break;
          default           :  
                            System.out.println("Metric Number  " + 
                            i + " does not exist .... exiting \n");
                            System.exit(0);
                               break;               
      }
    }
    
    private void setNewMetric(){
      switch (this.metricType) {
          case Joining.GBDeMetric :  
                            metric=new BDeMetrics(mCases);
                            break;
          case Joining.GL1OMetric:  
                            metric=new L1OMetrics(mCases);
                            break;
          case Joining.GPValueChiTest:           
                            metric=new L1OMetrics(mCases);
                            break;
          case Joining.GPValueTTest:           
                            metric = new L1OMetrics(mCases);
                            break;
                            
      }
    }
    
    private double getScore(PotentialTable t, double ess){
      switch (this.metricType) {
          case Joining.GBDeMetric :  return ((BDeMetrics)metric).score(t,ess);
          case Joining.GL1OMetric:  return ((L1OMetrics)metric).score(t);
          case Joining.GPValueChiTest:  return ((L1OMetrics)metric).score(t);
          case Joining.GPValueTTest:  return ((L1OMetrics)metric).score(t);
          default: return -Double.MAX_VALUE;
      }
    }
    public void setClassVariable(int i){
        mClassVar=i;
        mClassVarType=0;
    }
    public void setClassVar(int i){
      if (i> mCases.getVariables().size()){
        System.out.println("\n*** Error ***: classVar index (" + i
        +") is greater than number of vars in data base ... exiting");
        System.exit(0);
      }
      
      mClassVar = i-1;
      //setting classVarType      
      if (IsDiscreteVariable(i-1)) mClassVarType=0;
      else mClassVarType=1;
    }


    private void setNumBins(int i){
      mNumBins = i;
    }

    public void setTarget(Vector v){
      mTargetVariables = v;
    }

    private void setTargetVariables(Vector v){
      mTargetVariables = new Vector();
      int i,value;
      String s;
      Integer I;
      Node node;
 
     
      s = (String)v.elementAt(0);

      if (s.equals("list")){
        for(i=1;i<v.size();i++){
          value = ((Integer)v.elementAt(i)).intValue();
          mTargetVariables.addElement(new Integer(value-1));
        }
      }else if (s.equals("all")){
        for(i=0;i<mDiscreteVariablesVector.size();i++){
          I = (Integer) mDiscreteVariablesVector.elementAt(i);
          if (I.intValue()!=mClassVar) 
            mTargetVariables.addElement(I);
        } 
      }else if (s.equals("more")){
        int nBins = ((Integer)v.elementAt(1)).intValue();
        for(i=0;i<mDiscreteVariablesVector.size();i++){
          I = (Integer) mDiscreteVariablesVector.elementAt(i);
          node = (Node) (mCases.getVariables()).elementAt(I.intValue());
          int numStates = ((FiniteStates)node).getNumStates();
          if ( (I.intValue()!=mClassVar)  && (numStates>=nBins)) 
            mTargetVariables.addElement(I);
        } 
      }else{
        System.out.println("ERROR: this type of identifying target variables is not allowed ... exiting ");
        System.exit(0);
      }  

      mTargetNumBins = new Vector();
      for(i=0;i<mTargetVariables.size();i++) 
        mTargetNumBins.addElement(new Integer(mNumBins));
    }

    

    /**
    *  Method for obtaining the number of cases of the database.
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
    *  Method for loading dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    private void loadData(String Filename) throws ParseException, IOException
    {
        int j;

        FileInputStream File = new FileInputStream(Filename);
        mCases = new DataBaseCases(File);
        File.close();

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

        mDBCInformation.add ( Filename                                     );
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
     * @param <code>Filename2</code> the test DBC file name.
    *  @return <code>void</code>
    */
    private void loadData(String Filename,String Filename2) throws ParseException, IOException
    {
        int j;

        FileInputStream File = new FileInputStream(Filename);
        mCases = new DataBaseCases(File);
        File.close();

        File = new FileInputStream(Filename2);
        testCases = new DataBaseCases(File);
        File.close();
        
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

        mDBCInformation.add ( Filename                                     );
        mDBCInformation.add ( new Integer(nodes.size())                    );
        mDBCInformation.add ( new Integer(caselistmem.getNumberOfCases())  );
        for(j=0 ; j < nodes.size() ; j++ ) {
          node = (Node)(caselistmem.getVariables()).elementAt(j);
          mDBCInformation.add( new String (node.getName()          ) );
          mDBCInformation.add( new Integer(node.getTypeOfVariable()) );
        }

        return;
    }
    /*---------------------------------------------------------------*/ 

    /**
    *  Method for saving dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    public void saveData(String Filename) throws IOException
    {
        FileWriter  File = new FileWriter(Filename);
        mGroupedCasesDB.saveDataBase(File);
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
        mGroupedCasesDB.saveDataBase(File);
        File.close();
         File = new FileWriter(Filename2);
        testGroupedCasesDB.saveDataBase(File);
         File.close();
        return;
    }

    /**
    *  Apply the results of the last used grouping method to a DataBaseCases object.
    *  So, the previous state groups are used to transform this data base.
    *  @return <code>DataBaseCases</code> the new Data Base.
    */
    public DataBaseCases applyAgain(DataBaseCases dbc) throws InvalidEditException,IOException
    {
        
        return transformGroupedDataBaseAgain(this.groupsDefinition,dbc);    
        
    }    
    
    /*---------------------------------------------------------------*/
    /**
    *  Apply a grouping method to a DataBaseCases object. Replacing
    *  the missing values for new computed values.
    *  @return <code>DataBaseCases</code> the new Data Base discretized.
    */
    public DataBaseCases apply() throws InvalidEditException,IOException
    {

        this.groupsDefinition = new Vector();

        //Data strucutures to go through values and vars
        NodeList    nodes      ;
        Vector      vector     ;
        Relation    relation   ;
        CaseListMem caselistmem;
        Node        node       ;




        if (debug) System.out.println("\n------------------------------------------------------");
        if (debug) System.out.println("-------------- Beginning Grouping Process ------------");
        if (debug) System.out.println("------------------------------------------------------");
   
        switch (mAlgorithm){                       
          // KEX_GROUPING 
          case KEX_GROUPING :  groupsDefinition = Kex();
                               break;
          case KEX2_GROUPING : groupsDefinition = Kex(); // the difference
                               break; // is treated inside Kex()
          case BAYES_GROUPING : groupsDefinition = bayes(); 
                               break; 
          case BAYES2_GROUPING : groupsDefinition = bayes2(); 
                               break; 
          case BAYESORDER_GROUPING : groupsDefinition = bayesorder(); 
                               break; 
          case NULLFREQUENCY_GROPUING : groupsDefinition = nullFrequencyGrouping(); 
                               break; 
                                                    
            default           :  System.out.println(KEX_GROUPING + "--" + "Algorithm number " + 
                mAlgorithm + " does not exist .... exiting \n");
                               System.exit(0);
                               break;   
        }
        
        
        for(int i=0;i<groupsDefinition.size();i++){
          if (debug) System.out.println("\nVariable tratada en posici�n " + i);
          int[] def;
          def = (int[])groupsDefinition.elementAt(i);
            if (debug) System.out.println("Nbins = " + ((Integer)mTargetNumBins.elementAt(i)).intValue());
          for(int j=0;j<def.length;j++){
            if (debug) System.out.println("\testado " + j + " ---> grupo " + def[j]);
          }
        }
        
       mGroupedCasesDB = getGroupedDataBase(groupsDefinition,mCases);

       testGroupedCasesDB = getGroupedDataBase(groupsDefinition,testCases);

        if (debug) System.out.println("\n------------------------------------------------------");
        if (debug) System.out.println("-------------- Grouping Process Finalized ------------");
        if (debug) System.out.println("------------------------------------------------------");

        return mGroupedCasesDB;
    }    

    /**
    *  Apply a grouping method to a DataBaseCases object. 
     *  It replaces the old variables by the new ones in the same database.
    *  @return <code>DataBaseCases
    */
    public void selfapply() throws InvalidEditException,IOException
    {

        this.groupsDefinition = new Vector();

        //Data strucutures to go through values and vars
        NodeList    nodes      ;
        Vector      vector     ;
        Relation    relation   ;
        CaseListMem caselistmem;
        Node        node       ;




        if (debug) System.out.println("\n------------------------------------------------------");
        if (debug) System.out.println("-------------- Beginning Grouping Process ------------");
        if (debug) System.out.println("------------------------------------------------------");
   
        switch (mAlgorithm){                       
          // KEX_GROUPING 
          case KEX_GROUPING :  groupsDefinition = Kex();
                               break;
          case KEX2_GROUPING : groupsDefinition = Kex(); // the difference
                               break; // is treated inside Kex()
          case BAYES_GROUPING : groupsDefinition = bayes(); 
                               break;
          case BAYES2_GROUPING : groupsDefinition = bayes2(); 
                               break; 
          case BAYESORDER_GROUPING : groupsDefinition = bayesorder(); 
                               break;                    
          case NULLFREQUENCY_GROPUING : groupsDefinition = nullFrequencyGrouping(); 
                               break; 
            default           :  System.out.println(KEX_GROUPING + "--" + "Algorithm number " + 
                mAlgorithm + " does not exist .... exiting \n");
                               System.exit(0);
                               break;   
        }

        for(int i=0;i<groupsDefinition.size();i++){
          if (debug) System.out.println("\nVariable tratada en posici�n " + i);
          int[] def;
          def = (int[])groupsDefinition.elementAt(i);
          for(int j=0;j<def.length;j++){
            if (debug) System.out.println("\testado " + j + " ---> grupo " + def[j]);
          }
        }

         transformGroupedDataBase(groupsDefinition);

        if (debug) System.out.println("\n------------------------------------------------------");
        if (debug) System.out.println("-------------- Grouping Process Finalized ------------");
        if (debug) System.out.println("------------------------------------------------------");

     
    }    

    

    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
    * Apply Kex algorithm for grouping..
    * This is a supervised method. The variable is grouped into n+1 
    * values, being n the number of values in the class variable. 
    * There is a group for each class' value and another one termed
    * unknown for variables not showing a special tendency.
    * 
    */
    public Vector Kex( ){ 
      FiniteStates fs;
      int classVarNumStates;
      int i,j,k,pos,maxStatesOfTargetVariable;
      Vector auxVector = new Vector(mTargetVariables.size());
      Vector definition;
      int[] states,def,distribution;
      int[][][] counts;  // target, states(target),states(classvar)
      CaseListMem caselistmem;
      //double row[]; // to store a case/row of the database
      int value;

      if (mClassVarType == 1){
        System.out.println("\n*** Error *** class type has to be discrete ... exiting");
        System.exit(0);
      }
  
      if (debug) System.out.println(".... Applying Kex ");

      fs = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
      classVarNumStates = fs.getNumStates();
      mNumBins = classVarNumStates + 1; // cambiar a classVarNumStates + 1;
      for(i=0;i<mTargetNumBins.size();i++)
        mTargetNumBins.setElementAt(new Integer(mNumBins),i);

      if (debug) System.out.println(".... NumBins = " + mNumBins);
      if (debug) System.out.println(".... " + mTargetVariables.size() + " target variables");

      // deleting unnecessary variables from mTargetVariables
      for(i=0;i<mTargetVariables.size(); ){
        pos = ( (Integer) mTargetVariables.elementAt(i) ).intValue();
        fs = (FiniteStates) mCases.getVariables().elementAt(pos);
        if (fs.getNumStates() <= mNumBins){
          if (debug) System.out.println("    .... skipping variable in position " + pos
                + ". Only " + fs.getNumStates() + " states"); 
          mTargetVariables.removeElementAt(i);          
          mTargetNumBins.removeElementAt(i);                       
        }else{
          auxVector.addElement(new Integer(fs.getNumStates()));
          i++;
        }
      }

      if (debug) System.out.println(".... " + mTargetVariables.size() + " target variables\n");

      // computing frequency counts for the target variables

      states = new int[mTargetVariables.size()];
      maxStatesOfTargetVariable = 0;
      for(i=0;i<mTargetVariables.size();i++){ 
        states[i] = ((Integer)auxVector.elementAt(i)).intValue();
        if (states[i] > maxStatesOfTargetVariable) 
          maxStatesOfTargetVariable = states[i]; 
      }

      counts = new int[mTargetVariables.size()]
            [maxStatesOfTargetVariable][classVarNumStates];

      for(i=0;i<mTargetVariables.size();i++)
        for(j=0;j<maxStatesOfTargetVariable;j++)
          for(k=0;k<classVarNumStates;k++)
        counts[i][j][k] = 0;


      caselistmem = (CaseListMem) mCases.getCases();
      for(i=0;i<GetNumberOfCases();i++){
        //row = caselistmem.getCase(i);
        for(j=0;j<mTargetVariables.size();j++){
          pos = ( (Integer) mTargetVariables.elementAt(j) ).intValue();
          value = (int) caselistmem.getValue(i,pos);
          if (value !=-1 ) 
            counts[j][value][(int)caselistmem.getValue(i,mClassVar)]++;
        }
      }

      // building groups
      definition = new Vector(mTargetVariables.size());
      distribution = new int[classVarNumStates];  
      for(i=0;i<mTargetVariables.size();i++){
        def = new int[states[i]];
        for(j=0;j<states[i];j++){
          int total=0;
          int maxPos=0;
          int secondMaxPos=-1;
          for(k=0;k<classVarNumStates;k++){
            distribution[k]=counts[i][j][k]; 
            total+=distribution[k];
            if (distribution[k] > distribution[maxPos]){
              secondMaxPos=maxPos;
              maxPos=k;
            }else{
              if (secondMaxPos==-1){
                if (maxPos!=k) secondMaxPos=k;
              } else if (distribution[k] > distribution[secondMaxPos]) 
                                secondMaxPos=k;
            }  
          }
          //System.out.print("\nVariable: " + i + " estado " + j + "\n\t");
          //for(k=0;k<classVarNumStates;k++) System.out.print(distribution[k]+" ");
          if (differenceInDistribution(distribution,total,0.95) ){
            if (mAlgorithm == KEX2_GROUPING){ 
              int[] dist = new int[2];
              dist[0] = distribution[maxPos];
              dist[1] = distribution[secondMaxPos];
              //System.out.print(" ("+dist[0]+","+dist[1]+") ");
              if (differenceInDistribution(dist,dist[0]+dist[1],0.9) ) def[j] = maxPos;
              else def[j]=classVarNumStates;
            } else def[j]=maxPos;
          } else def[j]=classVarNumStates;
          //System.out.println(" -----> " + def[j]);
        }
        definition.addElement(def);
      }

      return definition;
    }
 /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
    * Apply a Bayes algorithm for grouping..
    * This is a supervised method. The variable is grouped into 
     * a number of values depending of a Bayesian score
    * 
    */
    private Vector bayes( ){ 
      FiniteStates fs;
      int classVarNumStates,NumStates;
      double max,jointscore,diff,threshold;
      BDeMetrics metric;
      PotentialTable t,t1,t2;
      int i,j,k,l,pos,maxStatesOfTargetVariable,current,indexmax,stat;
      Vector auxVector = new Vector(mTargetVariables.size());
      Vector definition;
      int[] states,def,distribution;
      int[] original;
      int[][][] counts;  // target, states(target),states(classvar)
      double[] scores;
      CaseListMem caselistmem;
      //double row[]; // to store a case/row of the database
      int value;
      boolean change;
      boolean better;
      

      if (mClassVarType == 1){
        System.out.println("\n*** Error *** class type has to be discrete ... exiting");
        System.exit(0);
      }
      
      metric = new BDeMetrics();
  
      if (debug) System.out.println(".... Applying Bayes Method ");

      fs = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
      classVarNumStates = fs.getNumStates();
      
      t = new PotentialTable(fs);
     t1 = new PotentialTable(fs);
     t2 = new PotentialTable(fs);
    

      if (debug) System.out.println(".... " + mTargetVariables.size() + " target variables");

      

      // computing frequency counts for the target variables

      states = new int[mTargetVariables.size()];
      
      maxStatesOfTargetVariable = 0;
      
      for(i=0;i<mTargetVariables.size();i++){ 
           pos = ( (Integer) mTargetVariables.elementAt(i) ).intValue();
          fs = (FiniteStates) mCases.getVariables().elementAt(pos);
          states[i]  = fs.getNumStates();
          if (states[i] >maxStatesOfTargetVariable  ) {
              maxStatesOfTargetVariable = fs.getNumStates() ;
          }
      }       
     
      counts = new int[mTargetVariables.size()]
            [maxStatesOfTargetVariable][classVarNumStates];

      for(i=0;i<mTargetVariables.size();i++)
        for(j=0;j<maxStatesOfTargetVariable;j++)
          for(k=0;k<classVarNumStates;k++)
        counts[i][j][k] = 0;

    

      caselistmem = (CaseListMem) mCases.getCases();
      for(i=0;i<GetNumberOfCases();i++){
        //row = caselistmem.getCase(i);
        for(j=0;j<mTargetVariables.size();j++){
          pos = ( (Integer) mTargetVariables.elementAt(j) ).intValue();
          value = (int) caselistmem.getValue(i,pos);
          if (value !=-1 ) 
            counts[j][value][(int)caselistmem.getValue(i,mClassVar)]++;
        }
      }
 
      // building groups
      definition = new Vector(mTargetVariables.size());
      
      for(i=0;i<mTargetVariables.size();i++){
        if (debug) System.out.println("Starting <variable " + i);
        def = new int[states[i]];
        NumStates = states[i];
     
        original = new int[states[i]];
        scores = new double[states[i]];
        for(j=0;j<states[i];j++){
         def[j] = j;
         original[j] = j;
         
          if (debug) System.out.println("Score case " + j + " computed ");
          for(k=0;k<classVarNumStates;k++){
            t.setValue(k, (double) counts[i][j][k]);}
             scores[j] =  metric.score(t, 0.5);
            if (scores[j]>0 && scores[j]<0.0000000001)
                scores[j]=0.0;
        }
        if (debug) System.out.println("Initial scores computed ");
        current = 0;
        while (current< states[i]) {
            change = true;
          
            while (change){
                 better=false;
                 indexmax=current;  
                 threshold = 0.0;
                 max=0.0;
                if (states[i]>2 && this.THRESHOLD_GROUPING){
                    threshold = Math.log(0.57721566490153286061+Math.log ((states[i]*(states[i]-1.0)/2.0   ) ));
                    max =  threshold;
                }

                 for (j=current+1; j< states[i]; j++) {
                      for(k=0;k<classVarNumStates;k++){
                         t.setValue(k, (double) (counts[i][j][k] + counts[i][current][k])   ); 
                      }
                         jointscore = metric.score(t, 1.0);
                         diff = jointscore - scores[current] - scores[j];
                         if (debug) System.out.println("Jonint Score case (" + current +", "+ j +")  computed: "+jointscore);                            
                         if (GROUP_NULLFREQUENCY_STATES && scores[current]==0.0 && scores[j]==0.0){
                            diff=threshold+0.0000001;
                         }
                         
                         if (diff > max) {
                             better=true;
                             max= diff;
                             indexmax = j;
                         }
                 }

                // Joint the states current and indexmax in a single state with number current, all the states
                // greater than indexmax are decreased by 1.                   
                if (better){
                    stat = def[original[current]];
                    for (l=0; l<NumStates;l++){
                         if (def[l] > stat) {
                             def[l]--;
                         } else {
                         if  (def[l] == stat) {def[l] = def[original[current]];}

                         }
                     }

                     for(k=0;k<classVarNumStates;k++){
                        counts[i][current][k] += counts[i][indexmax][k]; 
                     }   
                    if (GROUP_NULLFREQUENCY_STATES && scores[current]==0.0 && scores[indexmax]==0.0)
                        scores[current]=0.0;
                    else
                        scores[current] = max + scores[current] + scores[indexmax];
                     for (l=indexmax; l< states[i]-1; l++){
                         original[l] = original[l+1];
                         scores[l] = scores[l+1];
                         for(k=0;k<classVarNumStates;k++){
                            counts[i][l][k] = counts[i][l+1][k]; 
                         }
                     }
                     states[i]--;

                 } else  {
                     change = false;
                 }
             }
             current++;
            }
                 
            mTargetNumBins.setElementAt(new Integer(states[i]),i);      
            definition.addElement(def);
      }

      return definition;
    }
    private double combinatorial(int n, int m){
        //return factorial(n)/(factorial(n-m)*factorial(m));
        if (m>n)
            return 1;
        double prod=1.0;
        for (int i=1; i<=m; i++){
            prod*=(n-m+i)/(double)i;
        }
        return prod;
    }

 /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
    * Apply a Bayes algorithm for grouping..
    * This is a supervised method. The difference of bayes() method
    * is that it tries the best score of all grouping possible
    * operations, instead of computing only the scores of 
    * a current value with all the other values.
    * 
    */
    private Vector bayes2( ){ 
      double increment=0.0, bestincrement=0.0;;
      FiniteStates fs;
      int classVarNumStates,NumStates;
      double max,jointscore,diff,threshold;
      PotentialTable t,t1,t2;
      int i,j,k,l,pos,maxStatesOfTargetVariable,current,indexmaxi,indexmaxj,stat;
      Vector auxVector = new Vector(mTargetVariables.size());
      Vector definition;
      int[] states,def,distribution;
      int[] original;
      int[][][] counts;  // target, states(target),states(classvar)
      double[] scores;
      CaseListMem caselistmem;
      //double row[]; // to store a case/row of the database
      int value;
      boolean change;
      boolean better;
      
      if (mClassVarType == 1){
        System.out.println("\n*** Error *** class type has to be discrete ... exiting");
        System.exit(0);
      }
      
      this.setNewMetric();
  
      if (debug) System.out.println(".... Applying Bayes Method ");

      fs = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
      classVarNumStates = fs.getNumStates();
      
      t = new PotentialTable(fs);
     t1 = new PotentialTable(fs);
     t2 = new PotentialTable(fs);
    

      if (debug) System.out.println(".... " + mTargetVariables.size() + " target variables");

      

      // computing frequency counts for the target variables

      states = new int[mTargetVariables.size()];
      
      maxStatesOfTargetVariable = 0;
      
      for(i=0;i<mTargetVariables.size();i++){ 
           pos = ( (Integer) mTargetVariables.elementAt(i) ).intValue();
          fs = (FiniteStates) mCases.getVariables().elementAt(pos);
          states[i]  = fs.getNumStates();
          if (states[i] >maxStatesOfTargetVariable  ) {
              maxStatesOfTargetVariable = fs.getNumStates() ;
          }
      }       
     
      counts = new int[mTargetVariables.size()]
            [maxStatesOfTargetVariable][classVarNumStates];

    for(i=0;i<mTargetVariables.size();i++)
        for(j=0;j<maxStatesOfTargetVariable;j++)
            for(k=0;k<classVarNumStates;k++)
                counts[i][j][k] = 0;

    

      caselistmem = (CaseListMem) mCases.getCases();
      for(i=0;i<GetNumberOfCases();i++){
        //row = caselistmem.getCase(i);
        for(j=0;j<mTargetVariables.size();j++){
          pos = ( (Integer) mTargetVariables.elementAt(j) ).intValue();
          value = (int) caselistmem.getValue(i,pos);
          if (value !=-1 ) 
            counts[j][value][(int)caselistmem.getValue(i,mClassVar)]++;
        }
      }
 
      // building groups
      definition = new Vector(mTargetVariables.size());
      
      for(i=0;i<mTargetVariables.size();i++){
        
        if (debug){
           int tmpi = ( (Integer) mTargetVariables.elementAt(i) ).intValue();
           FiniteStates tmpfs = (FiniteStates) mCases.getVariables().elementAt(tmpi);
            
            System.out.println("Starting <variable " + i + "name: "+tmpfs.getName()+"     : "+tmpfs.getNumStates()+" states");
        }
        def = new int[states[i]];
        NumStates = states[i];
     
        original = new int[states[i]];
        scores = new double[states[i]];
        for(j=0;j<states[i];j++){
            def[j] = j;
            original[j] = j;
            
            if (debug) System.out.println("Score case " + j + " computed ");
            for(k=0;k<classVarNumStates;k++){
                t.setValue(k, (double) counts[i][j][k]);}
            scores[j] =  this.getScore(t, 0.5);
            if (scores[j]>0 && scores[j]<0.0000000001)
                scores[j]=0.0;
        }
        
        double[][] matrixjointscores=new double[states[i]][states[i]];
        boolean[][] updatedjointscores=new boolean[states[i]][states[i]];
        for(j=0;j<states[i];j++){
            for(k=0;k<states[i];k++){
                updatedjointscores[j][k]=false;
            }
        }
        
        
            if (debug) System.out.println("Initial scores computed ");
     
            change = true;
          
            while (change){
                indexmaxi = 1;
                indexmaxj = 1;  
                better = false;
                max=0.0;
                threshold=0.0;
                double alpha=0.0;
                for (k=1; k<=(states[i]*(states[i]-1)/2.0); k++){
                    alpha+=1/(double)k;
                }
                
                if (states[i]>2 && this.THRESHOLD_GROUPING){
                    if (this.metricType==Joining.GBDeMetric) threshold = -Math.log(alpha);
                    if (this.metricType==Joining.GL1OMetric) threshold = -alpha;
                    max =  threshold;
                }
                for (current=0; current< states[i]; current++) {
                    for (j=current+1; j< states[i]; j++) {
                        if (!updatedjointscores[current][j]){
                            for(k=0;k<classVarNumStates;k++){
                                t.setValue(k, (double) (counts[i][j][k] + counts[i][current][k])   ); 
                            }
                            jointscore = this.getScore(t, 1.0);
                            matrixjointscores[current][j]=jointscore;
                            updatedjointscores[current][j]=true;
                        }else{
                            jointscore= matrixjointscores[current][j];
                        }
                        
                        
                        diff = jointscore - scores[current] - scores[j];
                        
                        if (this.metricType==Joining.GPValueChiTest && (scores[j]!=0.0 || scores[current]!=0.0)){

                            double level=((L1OMetrics)metric).scoreGroupingChiTest(counts[i][j],counts[i][current],classVarNumStates);
                            
                            if (debug) System.out.println("ATTENTION: "+i+","+j+","+diff+", "+level+", "+(1-0.01*alpha));
                            if (level<=(1-this.alphalevel/alpha)){
                                    diff=1-level;
                            }else{
                                diff=threshold-0.000001;
                            }
                        }
                             
                             
                        
                        if (this.metricType==Joining.GPValueTTest && scores[j]!=0.0 && scores[current]!=0.0){
                            double level=((L1OMetrics)metric).scoreGroupingTTest(counts[i][j],counts[i][current],classVarNumStates);
                            if (debug) System.out.println("ATTENTION: "+i+","+j+","+diff+", "+level+", "+(1-0.01*alpha));
                            if (level<=(1-this.alphalevel/alpha)){
                                    diff=1-level;
                            }else{
                                diff=threshold-0.000001;
                            }
                        }

                        if (debug) System.out.println("Jonint Score case (" + current +", "+ j +")  computed: "+jointscore+" Diff: "+diff);   
                        if (debug) System.out.println("Frequency Score case (" + counts[i][current][0] +", "+ counts[i][j][0] +")"+" case (" + counts[i][current][1] +", "+ counts[i][j][1] +")");
                        if (GROUP_NULLFREQUENCY_STATES && scores[current]==0.0 && scores[j]==0.0){
                            diff=threshold+0.0000001;
                        }

                         if (diff > max) {
                             max= diff;
                             indexmaxi = current; indexmaxj = j;
                             better = true;
                             bestincrement=increment;
                         }
                    }
                }
                
                if (debug) System.out.println("DIFF MAX: "+max+"  "+indexmaxi+"  "+indexmaxj);
                // Joint the states current and indexmax in a single state with number current, all the states
                // greater than indexmax are decreased by 1.                   
                if (better){
                    stat = def[original[indexmaxj]];
                    for (l=0; l<NumStates;l++){
                        if (def[l] > stat) {
                            def[l]--;
                        } else {
                            if  (def[l] == stat) {def[l] = def[original[indexmaxi]];}
                        }
                    }
                    for(k=0;k<classVarNumStates;k++){
                        counts[i][indexmaxi][k] += counts[i][indexmaxj][k]; 
                    }
                    
                    if (GROUP_NULLFREQUENCY_STATES && scores[indexmaxi]==0.0 && scores[indexmaxj]==0.0)
                        scores[indexmaxi]=0.0;
                    /*else{
                        for(k=0;k<classVarNumStates;k++){
                            t.setValue(k, (double) (counts[i][indexmaxi][k] + counts[i][indexmaxj][k])   ); 
                        }
                        scores[indexmaxi] = this.getScore(t, 1.0);
                    }*/
                    else
                        scores[indexmaxi] = max + scores[indexmaxi] + scores[indexmaxj];
                    
                    for (l=indexmaxj; l< states[i]-1; l++){
                        original[l] = original[l+1];
                        scores[l] = scores[l+1];
                        for(k=0;k<classVarNumStates;k++){
                            counts[i][l][k] = counts[i][l+1][k]; 
                        }
                    }
                    
                    for (l=0; l< indexmaxj; l++){
                        for (k=indexmaxj;k<states[i]-1; k++){
                            matrixjointscores[l][k]=matrixjointscores[l][k+1];
                            updatedjointscores[l][k]=updatedjointscores[l][k+1];
                        }
                    }

                    for (l=indexmaxj; l< states[i]-1; l++){
                        for (k=0;k<states[i]; k++){
                            matrixjointscores[l][k]=matrixjointscores[l+1][k];
                            updatedjointscores[l][k]=updatedjointscores[l+1][k];
                        }
                        for (k=indexmaxj;k<states[i]-1; k++){
                            matrixjointscores[l][k]=matrixjointscores[l][k+1];
                            updatedjointscores[l][k]=updatedjointscores[l][k+1];
                        }
                    }

                    states[i]--;
                    
                    for(k=0;k<matrixjointscores.length;k++){
                        updatedjointscores[indexmaxi][k]=false;
                        updatedjointscores[k][indexmaxi]=false;
                    }
                    
                    
                }else  {
                    change = false;
                }
            }
            if (debug) System.out.println("stados variable: " + states[i]);
            mTargetNumBins.setElementAt(new Integer(states[i]),i);      
            definition.addElement(def);
        }
        return definition;
    }
    

    private Vector nullFrequencyGrouping( ){ 
      FiniteStates fs;
      int classVarNumStates,NumStates;
      double max,jointscore,diff,threshold;
      BDeMetrics metric;
      PotentialTable t,t1,t2;
      int i,j,k,l,pos,maxStatesOfTargetVariable,current,indexmaxi,indexmaxj,stat;
      Vector auxVector = new Vector(mTargetVariables.size());
      Vector definition;
      int[] states,def,distribution;
      int[] original;
      int[][][] counts;  // target, states(target),states(classvar)
      double[] scores;
      CaseListMem caselistmem;
      //double row[]; // to store a case/row of the database
      int value;
      boolean change;
      boolean better;
      
      if (mClassVarType == 1){
        System.out.println("\n*** Error *** class type has to be discrete ... exiting");
        System.exit(0);
      }
      
      metric = new BDeMetrics();
  
      if (debug) System.out.println(".... Applying Bayes Method ");

      fs = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
      classVarNumStates = fs.getNumStates();
      
      t = new PotentialTable(fs);
     t1 = new PotentialTable(fs);
     t2 = new PotentialTable(fs);
    

      if (debug) System.out.println(".... " + mTargetVariables.size() + " target variables");

      

      // computing frequency counts for the target variables

      states = new int[mTargetVariables.size()];
      
      maxStatesOfTargetVariable = 0;
      
      for(i=0;i<mTargetVariables.size();i++){ 
           pos = ( (Integer) mTargetVariables.elementAt(i) ).intValue();
          fs = (FiniteStates) mCases.getVariables().elementAt(pos);
          states[i]  = fs.getNumStates();
          if (states[i] >maxStatesOfTargetVariable  ) {
              maxStatesOfTargetVariable = fs.getNumStates() ;
          }
      }       
     
      counts = new int[mTargetVariables.size()]
            [maxStatesOfTargetVariable][classVarNumStates];

    for(i=0;i<mTargetVariables.size();i++)
        for(j=0;j<maxStatesOfTargetVariable;j++)
            for(k=0;k<classVarNumStates;k++)
                counts[i][j][k] = 0;

    

      caselistmem = (CaseListMem) mCases.getCases();
      for(i=0;i<GetNumberOfCases();i++){
        //row = caselistmem.getCase(i);
        for(j=0;j<mTargetVariables.size();j++){
          pos = ( (Integer) mTargetVariables.elementAt(j) ).intValue();
          value = (int) caselistmem.getValue(i,pos);
          if (value !=-1 ) 
            counts[j][value][(int)caselistmem.getValue(i,mClassVar)]++;
        }
      }
 
      // building groups
      definition = new Vector(mTargetVariables.size());
      
      for(i=0;i<mTargetVariables.size();i++){
        
        if (debug){
           int tmpi = ( (Integer) mTargetVariables.elementAt(i) ).intValue();
           FiniteStates tmpfs = (FiniteStates) mCases.getVariables().elementAt(tmpi);
            
            System.out.println("Starting <variable " + i + "name: "+tmpfs.getName());
        }
        def = new int[states[i]];
        NumStates = states[i];
     
        original = new int[states[i]];
        scores = new double[states[i]];
        for(j=0;j<states[i];j++){
            def[j] = j;
            original[j] = j;
            
            if (debug) System.out.println("Score case " + j + " computed ");
            for(k=0;k<classVarNumStates;k++){
                t.setValue(k, (double) counts[i][j][k]);}
            scores[j] =  metric.score(t, 0.5);
            if (scores[j]>0 && scores[j]<0.0000000001)
                scores[j]=0.0;
            
        }
        if (debug) System.out.println("Initial scores computed ");
     
            change = true;
          
            while (change){
                indexmaxi = 1;
                indexmaxj = 1;  
                better = false;
                max=0.0;
                threshold=0.0;
                if (true){
                    threshold = 1000000000;
                    max =  threshold;
                }
                for (current=0; current< states[i] && !better; current++) {
                    for (j=current+1; j< states[i]  && !better ; j++) {
                        for(k=0;k<classVarNumStates;k++){
                            t.setValue(k, (double) (counts[i][j][k] + counts[i][current][k])   ); 
                        }
                        jointscore = metric.score(t, 1.0);
                        diff = jointscore - scores[current] - scores[j];
                        if (debug) System.out.println("Jonint Score case (" + current +", "+ j +")  computed: "+jointscore+" Diff: "+diff);   
                        if (debug) System.out.println("Frequency Score case (" + counts[i][current][0] +", "+ counts[i][j][0] +")"+" case (" + counts[i][current][1] +", "+ counts[i][j][1] +")");
                        if (scores[current]==0.0 && scores[j]==0.0){
                            diff=threshold+0.0000001;
                        }

                         if (diff > max) {
                             max= diff;
                             indexmaxi = current; indexmaxj = j;
                             better = true;
                             break;
                         }
                    }
                }
                
                if (debug) System.out.println("DIFF MAX: "+max+"  "+indexmaxi+"  "+indexmaxj);
                // Joint the states current and indexmax in a single state with number current, all the states
                // greater than indexmax are decreased by 1.                   
                if (better){
                    stat = def[original[indexmaxj]];
                    for (l=0; l<NumStates;l++){
                        if (def[l] > stat) {
                            def[l]--;
                        } else {
                            if  (def[l] == stat) {def[l] = def[original[indexmaxi]];}
                        }
                    }
                    for(k=0;k<classVarNumStates;k++){
                        counts[i][indexmaxi][k] += counts[i][indexmaxj][k]; 
                    }
                    if (scores[indexmaxi]==0.0 && scores[indexmaxj]==0.0)
                        scores[indexmaxi]=0.0;
                    else
                        scores[indexmaxi] = max + scores[indexmaxi] + scores[indexmaxj];
                    for (l=indexmaxj; l< states[i]-1; l++){
                        original[l] = original[l+1];
                        scores[l] = scores[l+1];
                        for(k=0;k<classVarNumStates;k++){
                            counts[i][l][k] = counts[i][l+1][k]; 
                        }
                    }
                    states[i]--;
                         
                }else  {
                    change = false;
                }
            }
            if (debug) System.out.println("stados variable: " + states[i]);
            mTargetNumBins.setElementAt(new Integer(states[i]),i);      
            definition.addElement(def);
        }
        return definition;
    }
    

    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
    * Apply a Bayes algorithm for grouping..
    * This is a supervised method. The difference of former method
    * is that it considers that values are ordered and, therefore,
    * only consider grouping of an element with an adjacent one
    * 
    */
    private Vector bayesorder( ){ 
      FiniteStates fs;
      int classVarNumStates,NumStates;
      double max,jointscore,diff,threshold;
      BDeMetrics metric;
      PotentialTable t,t1,t2;
      int i,j,k,l,pos,maxStatesOfTargetVariable,current,indexmaxi,indexmaxj,stat;
      Vector auxVector = new Vector(mTargetVariables.size());
      Vector definition;
      int[] states,def,distribution;
      int[] original;
      int[][][] counts;  // target, states(target),states(classvar)
      double[] scores;
      CaseListMem caselistmem;
      //double row[]; // to store a case/row of the database
      int value;
      boolean change;
      boolean better;
      
      if (mClassVarType == 1){
        System.out.println("\n*** Error *** class type has to be discrete ... exiting");
        System.exit(0);
      }
      
      metric = new BDeMetrics();
  
      if (debug) System.out.println(".... Applying Bayes Method ");

      fs = (FiniteStates) mCases.getVariables().elementAt(mClassVar);
      classVarNumStates = fs.getNumStates();
      
      t = new PotentialTable(fs);
     t1 = new PotentialTable(fs);
     t2 = new PotentialTable(fs);
    

      if (debug) System.out.println(".... " + mTargetVariables.size() + " target variables");

      

      // computing frequency counts for the target variables

      states = new int[mTargetVariables.size()];
      
      maxStatesOfTargetVariable = 0;
      
      for(i=0;i<mTargetVariables.size();i++){ 
           pos = ( (Integer) mTargetVariables.elementAt(i) ).intValue();
          fs = (FiniteStates) mCases.getVariables().elementAt(pos);
          states[i]  = fs.getNumStates();
          if (states[i] >maxStatesOfTargetVariable  ) {
              maxStatesOfTargetVariable = fs.getNumStates() ;
          }
      }       
     
      counts = new int[mTargetVariables.size()]
            [maxStatesOfTargetVariable][classVarNumStates];

      for(i=0;i<mTargetVariables.size();i++)
        for(j=0;j<maxStatesOfTargetVariable;j++)
          for(k=0;k<classVarNumStates;k++)
        counts[i][j][k] = 0;

    

      caselistmem = (CaseListMem) mCases.getCases();
      for(i=0;i<GetNumberOfCases();i++){
        //row = caselistmem.getCase(i);
        for(j=0;j<mTargetVariables.size();j++){
          pos = ( (Integer) mTargetVariables.elementAt(j) ).intValue();
          value = (int) caselistmem.getValue(i,pos);
          if (value !=-1 ) 
            counts[j][value][(int)caselistmem.getValue(i,mClassVar)]++;
        }
      }
 
      // building groups
      definition = new Vector(mTargetVariables.size());
      
      for(i=0;i<mTargetVariables.size();i++){
        if (debug) System.out.println("Starting <variable " + i);
        def = new int[states[i]];
        NumStates = states[i];
     
        original = new int[states[i]];
        scores = new double[states[i]];
        for(j=0;j<states[i];j++){
         def[j] = j;
         original[j] = j;
         
          if (debug) System.out.println("Score case " + j + " computed ");
          for(k=0;k<classVarNumStates;k++){
            t.setValue(k, (double) counts[i][j][k]);}
             scores[j] =  metric.score(t, 0.5);
        }
        if (debug) System.out.println("Initial scores computed ");
     
            change = true;
          
            while (change){
                indexmaxi = 1;
                indexmaxj = 1;  
                better = false;
                max=0.0;
                threshold=0.0;
                if (states[i]>2 && this.THRESHOLD_GROUPING){
                       threshold=Math.log(1.0 + Math.log (states[i]-1.0)) ; 
                       max = threshold;
                }
                for (current=0; current< states[i]-1; current++) {
                    j=current+1;
                    for(k=0;k<classVarNumStates;k++){
                     t.setValue(k, (double) (counts[i][j][k] + counts[i][current][k])   ); 
                    }
                    jointscore = metric.score(t, 1.0);
                    diff = jointscore - scores[current] - scores[j];
                    if (debug) System.out.println("Jonint Score case (" + current +", "+ j +")  computed: "+jointscore);   
                    if (GROUP_NULLFREQUENCY_STATES && scores[current]==0.0 && scores[j]==0.0){
                        diff=threshold+0.0000001;
                    }


                    if (diff > max) {
                        better = true;
                        max= diff;
                        indexmaxi = current; indexmaxj = j;
                    }
                }
             
                // Joint the states current and indexmax in a single state with number current, all the states
                // greater than indexmax are decreased by 1.                   
                if (better){
                    stat = def[original[indexmaxj]];
                    for (l=0; l<NumStates;l++){
                        if (def[l] > stat) {
                            def[l]--;
                        } else {
                            if  (def[l] == stat) {def[l] = def[original[indexmaxi]];}
                        }
                    }
                        
                    for(k=0;k<classVarNumStates;k++){
                        counts[i][indexmaxi][k] += counts[i][indexmaxj][k]; 
                    }   
                    if (GROUP_NULLFREQUENCY_STATES && scores[indexmaxi]==0.0 && scores[indexmaxj]==0.0)
                        scores[indexmaxi]=0.0;
                    else
                        scores[indexmaxi] = max + scores[indexmaxi] + scores[indexmaxj];

                    for (l=indexmaxj; l< states[i]-1; l++){
                        original[l] = original[l+1];
                        scores[l] = scores[l+1];
                        for(k=0;k<classVarNumStates;k++){
                            counts[i][l][k] = counts[i][l+1][k]; 
                        }
                    }
                    states[i]--;
                         
                } else  {
                    change = false;
                }
            }
            mTargetNumBins.setElementAt(new Integer(states[i]),i);      
            definition.addElement(def);
        }
        return definition;
    }
    /*---------------------------------------------------------------*/
    /** Uses chi-square to test if the given distribution follows  
     *  (for a given significance level) the uniform distribution or not
     */

    private boolean differenceInDistribution(int[] dist,int numSamples,
                            double alpha){
      int n=dist.length; //number of states
      int i;
      double e_i = (double)numSamples/(double)n; //expected values in a uniform
      double T=0.0,chi;

      if (numSamples < n) return false; 

      //computing the statistic
      for(i=0;i<n;i++) T += ((double)dist[i]*dist[i])/e_i;
      T -= numSamples;
      //computing chi2 critical value
      chi = Chi2.critchi(1-alpha,n-1);
      // accepting or rejecting 
      if (T >= chi) return true;
      else return false;
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
   
    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
     * Build a new data base by replacing the values of the variables
     * which have been grouped
     * @param groups a Vector with the definition of each group. Each position i in the Vector groups
     * corresponds to a variable in the data base. Each element in the Vector contains
     * an array of ints. An element j in the array at position i of the Vector is the new number of state
     * for the old state j of the variable i in the list mTargetVariables.
     * @param db The original DataBaseCases
     * @return A new DataBaseCases with  grouped values 
     */

    private DataBaseCases getGroupedDataBase(Vector groups, DataBaseCases data) throws InvalidEditException {

      NodeList nl = new NodeList();
      Node node,node2;
      FiniteStates fs;
      int i,j,pos,nBins;
      String[] stat;
      CaseListMem clm;
      CaseListMem clm2;
      Integer I;
      int[] targetPositions; //the position of a variable in targetVariables
      int [] def;
      //or -1 in other case    

      // computing targetPositions
      DataBaseCases db=data.copy();
      targetPositions = new int[data.getVariables().size()];
      for(i=0;i<targetPositions.length;i++) targetPositions[i]=-1;
      for(i=0;i<mTargetVariables.size();i++){
        I = (Integer) mTargetVariables.elementAt(i);
        targetPositions[I.intValue()] = i;
      }

      // creating the variables
   
      for(i=0;i<db.getVariables().size();i++){
        node = db.getVariables().elementAt(i);
        if (debug) System.out.println("transformando ");
        node.print();
        node2 = node.copy();
        pos = targetPositions[i];
        if (pos!=-1) {
          if (debug) System.out.println("Posicion " + pos);
          nBins = ((Integer)mTargetNumBins.elementAt(pos)).intValue();
          if (debug) System.out.println("Bins " + nBins);
          if (debug){
              def=(int [])groups.elementAt(i);
              stat=getNewNameStates(def,nBins,(FiniteStates)node2);
          }else{
              stat = new String[nBins];
              for(j=0;j<nBins;j++) stat[j] = new String("state"+j);
          }
              
          ((FiniteStates)node2).setStates(stat);
        }
       
        nl.insertNode(node2);
      }

      // creating data

      clm2 = new CaseListMem(nl);
      clm = (CaseListMem) db.getCases();
      int nCases = clm.getNumberOfCases();
      int[] caseNew;
      int[] groupDef;
      Vector v = new Vector();
      int value;
      
      for(i=0;i<nCases;i++){
        //case1 = clm.getCase(i);
        caseNew = new int[nl.size()];
        for(j=0;j<nl.size();j++){
          value = (int)clm.getValue(i,j);
          if (targetPositions[j] != -1){
            if (value == -1) caseNew[j]=-1;
            else{
              groupDef = (int[])groups.elementAt(targetPositions[j]);
              caseNew[j] = groupDef[(int)value];
            }
          }
          else caseNew[j] = value;
        }
        v.addElement(caseNew);
      }
  
      clm2.setNumberOfCases(nCases);
      clm2.setCases(v);

      // returning the new data base
      
      return new DataBaseCases(db.getName(),nl,clm2);
    }

 
    /**
     * Transforms a database cases according to a group definition
     * @param groups a Vector with the definition of each group. Each position i in the Vector groups
     * corresponds to a variable in the data base. Each element in the Vector contains
     * an array of ints. An element j in the array at position i of the Vector is the new number of state
     * for the old state j of the variable i in the list mTargetVariables.
     */

    private void transformGroupedDataBase(Vector groups) throws InvalidEditException {

        
        NodeList nl;
        Node node,node2;
        FiniteStates fs;
        int i,j,pos,nBins;
        String[] stat;
        CaseListMem clm;
        int[] case1;
        Integer I;
        int [] def;


        // computing targetPositions

        // transforming the variables
        for(i=0;i< mTargetVariables.size() ;i++){
            pos = ((Integer) mTargetVariables.elementAt(i)).intValue();
            node = mCases.getVariables().elementAt(pos);
            if (pos!=-1) {
                nBins = ((Integer)mTargetNumBins.elementAt(i)).intValue();
                if (debug) System.out.println("Bins " + nBins);
                if (debug){ 
                    def=(int [])groups.elementAt(i);
                    stat=getNewNameStates(def,nBins,(FiniteStates)node);
                }else{
                  stat = new String[nBins];
                  for(j=0;j<nBins;j++) stat[j] = new String("state"+j);
                }
                ((FiniteStates)node).setStates(stat);
            }
        }

        // creating data
        clm = (CaseListMem) mCases.getCases();
        int nCases = clm.getNumberOfCases();

        int[] groupDef;

        double value;

        for(i=0;i<nCases;i++){
            case1 = clm.getCase(i);
            for(j=0;j<  mTargetVariables.size() ;j++){
                pos = ((Integer) mTargetVariables.elementAt(j)).intValue();
                value = case1[pos];
                if (value != FiniteStates.UNDEFVALUE ) {   // -1.0)){
                    groupDef = (int[])groups.elementAt(j);
                    case1[pos] = groupDef[(int)value];
                }
            }
        }



        if (test) {
            for(i=0;i< mTargetVariables.size() ;i++){
                pos = ((Integer) mTargetVariables.elementAt(i)).intValue();
                node = testCases.getVariables().elementAt(pos);
                if (pos!=-1) {
                    nBins = ((Integer)mTargetNumBins.elementAt(i)).intValue();
                    if (debug){
                      def=(int [])groups.elementAt(i);
                      stat=getNewNameStates(def,nBins,(FiniteStates)node);
                    }else{
                      stat = new String[nBins];
                      for(j=0;j<nBins;j++) stat[j] = new String("state"+j);
                    }
                    ((FiniteStates)node).setStates(stat);
                }
            }
            clm = (CaseListMem) testCases.getCases();
            nCases = clm.getNumberOfCases();

            for(i=0;i<nCases;i++){
                case1 = clm.getCase(i);
                for(j=0;j<  mTargetVariables.size() ;j++){
                    pos = ((Integer) mTargetVariables.elementAt(j)).intValue();
                    value = case1[pos];
                    if (value != FiniteStates.UNDEFVALUE ) {   // -1.0)){
                        groupDef = (int[])groups.elementAt(j);
                        case1[pos] = groupDef[(int)value];
                    }
                }
            }
        }

        // returning the new data base

        }
      
    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
     * Transforma a database cases according a a group defition
     */

    private DataBaseCases transformGroupedDataBaseAgain(Vector groups, DataBaseCases dbc) throws InvalidEditException {
      
      NodeList nl;
      Node node,node2;
      FiniteStates fs;
      int i,j,pos,nBins;
      String[] stat;
      CaseListMem clm;
      int[] case1;
      Integer I;
      int [] def;


      // computing targetPositions

      if (debug) System.out.println("Apply Again: Target  " + mTargetVariables.size());
  
      DataBaseCases data=dbc.copy();
      // transforming the variables
   
      for(i=0;i< mTargetVariables.size() ;i++){
        pos = ((Integer) mTargetVariables.elementAt(i)).intValue();
          
        node = data.getVariables().elementAt(pos);
       
       
        if (pos!=-1) {
          nBins = ((Integer)mTargetNumBins.elementAt(i)).intValue();
          //if (debug) System.out.println("Bins " + nBins);
          if (debug){
              def=(int [])groups.elementAt(i);
              stat=getNewNameStates(def,nBins,(FiniteStates)node);
          }else{
              stat = new String[nBins];
              for(j=0;j<nBins;j++) stat[j] = new String("state"+j);
          }
          ((FiniteStates)node).setStates(stat);
        }
      }

      // creating data

     
      
      clm = (CaseListMem) data.getCases();
      int nCases = clm.getNumberOfCases();
    
      int[] groupDef;
     
      double value;
      
      for(i=0;i<nCases;i++){
         case1 = clm.getCase(i);
      
        for(j=0;j<  mTargetVariables.size() ;j++){
              pos = ((Integer) mTargetVariables.elementAt(j)).intValue();
          
          value = case1[pos];
        
            if (value != FiniteStates.UNDEFVALUE ) {   // -1.0)){
         
              groupDef = (int[])groups.elementAt(j);
              case1[pos] = groupDef[(int)value];
            }
          
        }
      }
          
      return data;
      // returning the new data base
        
    }    
    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 

    /**
     * Obtains the new names of the cases of the variable fs by concatenating the old names and 
     * putting square brackets around it
     * @param def array of ints with the new group (number of case) for the each case in fs
     * @param nBins the new number of cases
     * @param fs the variable to be grouped
     * @return an array of Strings with the names for the cases of the grouped variable
     */
    String[] getNewNameStates(int def[],int nBins,FiniteStates fs){
        String oldState;
        int j;
        String[] stat = new String[nBins];
        for(j=0;j<nBins;j++)
            stat[j] = new String("");
        for(j=0;j<def.length;j++){ // concat the names of the old states
            oldState=fs.getState(j);
            if (oldState.charAt(0)=='\"') // eliminate quotes
                oldState=oldState.substring(1,oldState.length()-1);
            if(!stat[def[j]].equals("")){
                stat[def[j]]=stat[def[j]]+"+"+oldState;
            }
            else{
                stat[def[j]]=oldState;
            }
        }
        for(j=0;j<nBins;j++){  //put parenthesis around names of states
            stat[j]="\"{"+stat[j]+"}\"";
        }
        return stat;


    }

    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
    * For performing tests 
    */
    public static void main(String args[]) throws ParseException, IOException, InvalidEditException
    {
        Vector    myVector;
        Grouping  myGrouping;

        if ( args.length == 0 ) {
          System.out.print("USAGE:  <program> <input file.dbc> <output file.dbc> <input test.dbc|none> <output test.dbc|none>"); 
          System.out.println(" <algorithm> <class> \n\t<numbins|-1> <all|more numbins|list v1 v2 ...>"); // <intervals> <options>");
          System.out.println("\n<algorithm> :");
          System.out.println("\n The test databases can be none");
          System.out.println("Algorithm: 0 => KEX                             ");
          System.out.println("Algorithm: 1 => KEX2                            ");
          System.out.println("Algorithm: 2 => BAYES                            ");
          System.out.println("Algorithm: 3 => BAYES2                            ");
            System.out.println("Algorithm: 4 => BAYESORDER                            ");
          System.out.println("\n<class> : the index of the class variable (1,2,...");
          System.out.println("\n<numBins> : the number of bins or -1 if has to be discovered by the algorithm");
          System.out.println("\n<target> : all, more numbins or list followed by the indexes");
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
          int classIndex = Integer.valueOf(args[5]).intValue();
          int numBins = Integer.valueOf(args[6]).intValue();

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
          System.out.println("\tnumBins: " + numBins);
          System.out.print("\ttarget: ");
          System.out.print((String)(target.elementAt(0)) + " ");
          for(int i=1;i<target.size();i++) 
            System.out.print( ((Integer)target.elementAt(i)).intValue() + " ");
          System.out.println();
          
       
 
          myGrouping = new Grouping( );
          
            
          if (args[2].equals("none") ) {myGrouping.test = false;}
          else {myGrouping.test=true;}
          
          if (myGrouping.test) {myGrouping.loadData(args[0],args[2]);}
          else  {myGrouping.loadData(args[0]);}
          myGrouping.setAlgorithm(algorithm);
          myGrouping.setClassVar(classIndex);
          myGrouping.setNumBins(numBins);
          myGrouping.setTargetVariables(target);
          myGrouping.apply();

          //myGrouping.SetMode(DISCRETIZE_INDIVIDUALLY);
          //myGrouping.SetOperation(MASSIVE_OPERATION);
          //myGrouping.ConfigureIndividual(Integer.valueOf(args[2]).intValue(),Integer.valueOf(args[3]).intValue(),myVector);

            if (myGrouping.test) {myGrouping.saveData(args[1],args[3]);}
            else {myGrouping.saveData(args[1]);}
        }

        return;
    }//End main 
}
//End Grouping class
