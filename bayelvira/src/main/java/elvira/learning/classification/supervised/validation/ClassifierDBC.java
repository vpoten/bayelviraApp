/*
 * ClassifierDBC.java
 *
 * Created on 27 de julio de 2004, 15:01
 */


package elvira.learning.classification.supervised.validation;

import elvira.tools.statistics.analysis.Regression;
import elvira.tools.statistics.analysis.Stat;
import elvira.tools.statistics.anova.anova;
import elvira.tools.statistics.analysis.t_statistics;
import elvira.tools.statistics.math.Fmath;
import elvira.tools.statistics.analysis.MathMAE;
import elvira.Node;
import elvira.Continuous;
import elvira.FiniteStates;
import elvira.ContinuousCaseListMem;
import elvira.CaseList;
import elvira.NodeList;
import elvira.ContinuousConfiguration;
import elvira.Relation;
import elvira.CaseListMem;
import elvira.learning.preprocessing.Discretization;
import elvira.learning.classification.ClassifierValidator;
import elvira.tools.LinearFunction;
import elvira.parser.ParseException;
import elvira.potential.ContinuousProbabilityTree;
import elvira.potential.MixtExpDensity;
import elvira.learning.MTELearning;
import elvira.database.DataBaseCases;
import elvira.learning.classification.supervised.mixed.*;


import java.io.Serializable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Vector;
import java.util.Random;
import java.lang.Math;

/**
 *  This class implements a specialized data base of cases of the variables of
 *  a specific Bayesian network, Classifiers. It inherits from DataBaseCases class. 
 *  A ClassifierDBC object is a DataBaseCases object where there is a class variable, 
 *  a FiniteStates node,  and the rest of the variables are attributes, discrete or continuous, 
 *  of this class variable.
 *
 *  This class contains several fields with the aim of make easy the handling of this
 *  type of DataBaseCases. It's allowed to store several filter measures and classfication based 
 *  measures for each variable of the data base and a specific partitions of the cases of this data base.
 *  These fields are specilly useful when we manage large data base of cases with many cases or 
 *  with many variables.
 *  
 *  The main methods of this class are:
 *      - Obtain filter measures associated to a <code>Continuous</code>  node of 
 *      a DataBaseCases. Implemented Filter Measues: Anova Measure, TAN Anova Measure,
 *      Conditional Mutual Information given the class for Continuous nodes, 
 *      pearson correlation coefficient given the class, t-statistic ...
 *
 *      - It's possible to do a Kolmogorov-Smirnov normality test for each class of given
 *        Continuous node of the DataBaseCase.
 *
 *      - Obtain Classification based Measures associated to a 
 *      <code>Continuous</code>  node of a DataBaseCases. They are described in
 *      [2].
 *
 *      - Implements several Filter Methods:
 *          - Anova Filter Method described in [1]
 *          - FirstAnovaNodes.
 *
 *      - Obtain the Regression equation of a Continuous node respect to a list of Continuous 
 *      nodes for each target class.
 *
 *      - Obtain a <code>ContinuousProbabilityTree</code> with the conditional 
 *      Gaussian Distribution of a <code>Continuous</code> node respect to a 
 *      list of <code>Continuous</code> nodes and each target class. 
 *
 *  
 * Refs: 
 *  [1] A. Cano et al. "Application of Selective Gaussian Naive Bayes Classifier
 *  for Diffuse Large B-Cell Lymphoma Classification" (PGM2004).
 *  [2] A. Cano et al. "Selective Gaussian Naive Bayes Model for Diffuse Large 
 *  B-Cell Lymphoma Classifiation: Some Improvements in Preprocessing and
 *  Variable Elimination" (ECSQARU2005).
 *
 *
 *  @author  andrew
 */
public class ClassifierDBC extends DataBaseCases implements Serializable{
    
    static final long serialVersionUID = 2917793633560519646L;
    
    /**
     * The target FiniteStates variable with the labels to predict by the classifier.
     */
    protected FiniteStates varClass;
    
    /**
     * The position the the varClass field in the variables of the field data.
     */
    protected int indVarClass;
    
    /**
     * The number of states of the varClass field.
     */
    protected int numClass;
    
    /** 
     * This field contains several filter measures for this data base cases
     */
    protected ContinuousFilterMeasures cfm;

    /**
     * This field contains several classification based measures for this data base cases
     */
    protected ClassifierBasedMeasures cMeasures = new ClassifierBasedMeasures();
    
 
    /**
     * This field conatins a set of ClassifierDBC that are a k-partition of this
     * ClassifierDBC object. With this field, a k-fold cross validition could
     * always be carried out with the same partitions
     */
    protected Vector KFC=new Vector();

    /**
     * This field is equal to previos KFC field, but for the leave-one-out validation
     */
    protected Vector LOO=new Vector();
    
    
    protected Discretization discretization=null; 
    
    protected static int numInterval=3;
    
    
    
    /**
     * Constructor: Create an empty class
     */
    public ClassifierDBC(){
        super();
    }

    /**
     *  Constructor: Creates a new object with the DataBaseCases dbc, where
     *  the variable indClass-th is the target varible class.
     */
    public ClassifierDBC(DataBaseCases dbc, int indClass){
        super(new String(dbc.getName()),(CaseList)dbc.getCases().copy());
        this.cfm=new ContinuousFilterMeasures();           

        varClass=(FiniteStates)dbc.getVariables().elementAt(indClass);
        this.indVarClass=indClass;
        numClass=varClass.getNumStates();
    }

    /**
     *  Constructor: Creates a new object with the DataBaseCases is readed form
     *  file f and where the variable indClass-th is the target varible class.
     */
    public ClassifierDBC(FileInputStream f) throws IOException, ParseException, FileNotFoundException {
        super(f);
        this.cfm=new ContinuousFilterMeasures();

        varClass=(FiniteStates)super.getVariables().elementAt(this.getVariables().size()-1);
        this.indVarClass=this.getVariables().size()-1;
        numClass=varClass.getNumStates();
    }

    
    /**
     *  Constructor: Creates a new object with the DataBaseCases is readed form
     *  file f and where the variable indClass-th is the target varible class.
     */
    public ClassifierDBC(FileInputStream f, int indClass) throws IOException, ParseException, FileNotFoundException {
        super(f);
        this.cfm=new ContinuousFilterMeasures();

        varClass=(FiniteStates)super.getVariables().elementAt(indClass);
        this.indVarClass=indClass;
        numClass=varClass.getNumStates();
    }

    /**
     *  Constructor: Creates a new object where the DataBaseCases is composed
     *  by the fields name and cases and where the variable indClass-th is 
     *  the target varible class.
     */
    public ClassifierDBC(String name, CaseList cases, int indClass){
        super(name,cases);
        this.cfm=new ContinuousFilterMeasures();           

        varClass=(FiniteStates)super.getVariables().elementAt(indClass);
        this.indVarClass=indClass;
        numClass=varClass.getNumStates();
    }

    /**
     * Return the position of the variable to classify.
     */
    public int getIndVarClass(){
        return this.getVariables().getId(this.varClass);
    }
    
    /**
     *  Return the number of states of the target variable
     */
    public int getNumberOfStates(){
        return this.varClass.getNumStates();
    }
    /**
     *  Return the variable to classify.
     */

    public FiniteStates getVarClass(){
        return this.varClass;
    }

    /**
     * Return the cMeasures field.
     */
    public ClassifierBasedMeasures getCBasedMeasures(){
        return this.cMeasures;
    }

    /**
     *  Set the cMeasures field
     */
    public void setCBasedMeasures(ClassifierBasedMeasures cM){
        this.cMeasures=cM;
    }
    
    /**
     * Return the cfm field.
     */
    public ContinuousFilterMeasures getCFilterMeasures(){
        return this.cfm;
    }
    
    /**
     *  Set the cfm field
     */
    public void setCFilterMeasures(ContinuousFilterMeasures cfm){
        this.cfm=cfm;
    }

    /**
     *  Return the Anova Measure, described in [1], for the variable 'variable'. 
     *  This method uses the field <code>ContinuousFilterMeasures</code>, so if 
     *  the Anova measure order had to be recalculated, no more evaluations are neccesary,
     *  in the other case, the complete Anova mesure order is calculated and stored.
     */
    public double anovaMeasure(Continuous variable){
        
        if (!this.cfm.isMeasure(ContinuousFilterMeasures.ANOVA_MEASUREORDER)){
            this.cfm.addMeasureOrder(ContinuousFilterMeasures.ANOVA_MEASUREORDER,this.getSortAnovaNodes());
        }
        return this.cfm.getMeasureNode(ContinuousFilterMeasures.ANOVA_MEASUREORDER, variable);
    }

    /**
     * Return the Anova Measure, describe in [1], for the variable 'variable'.
     * If this measure order hasn't already calculated, only it's computed the measure
     * for this variable.
     */
    public double calcAnovaMeasure(Continuous variable){
        
        if (!this.cfm.isMeasure(ContinuousFilterMeasures.ANOVA_MEASUREORDER)){
            anova A=new anova();
            int[] ngroup=new int[numClass];
            double[][] datos=this.getData(variable,ngroup);

            A.setData(datos,ngroup,numClass);

            return A.getF();
        }else{
            return this.cfm.getMeasureNode(ContinuousFilterMeasures.ANOVA_MEASUREORDER, variable);
        }
    }

    /**
     * Return the i-th node with the most high anova measure.
     */
    public Node getAnovaNode(int N){
        return this.getSortAnovaNodes().getNodeList().elementAt(N);
    }

    /**
     *  Return the node with the lowest Anova measure.
     */
    public Node getLastAnovaNode(){
        return this.getSortAnovaNodes().getNodeList().lastElement();
    }
    
    /**
     * Return the i-th node with the most high anova measure.
     */
    public Node getTANAnovaNode(int N){
        return this.getSortTANAnovaNodes().getNodeList().elementAt(N);
    }
    
    /**
     *  Return a Vector of <code>ClassifierMeasureNode</code> with the Gaussia_Naive_Bayes
     *  classifier based order for the variables of this data set.
     */
    public Vector getGNBBasedOrder(){
        try{
            if (!this.cMeasures.isOrder(Gaussian_Naive_Bayes.class.getName())){
                Gaussian_Naive_Bayes gn =new Gaussian_Naive_Bayes(this,false,this.getIndVarClass());    
                this.cMeasures.addOrder(this.calculateClassifierBasedOrder(gn));
            }
            return this.cMeasures.getOrder(Gaussian_Naive_Bayes.class.getName());
          }catch(Exception e){
              e.printStackTrace();
              System.exit(0);
              return null;
          }
    }

    /**
     *  Return a Vector of Integer, where v(i) conatins the number of the variable
     *  that has the i-th position in the Gaussian_Naive_Bayes classifier order.
     */
    public Vector getNumberGNBBasedOrder(){
        try{
            if (!this.cMeasures.isOrder(Gaussian_Naive_Bayes.class.getName())){
                Gaussian_Naive_Bayes gn =new Gaussian_Naive_Bayes(this,false,this.getIndVarClass());    
                this.cMeasures.addOrder(this.calculateClassifierBasedOrder(gn));
            }
            return this.cMeasures.getNumberOrder(Gaussian_Naive_Bayes.class.getName(),this.getVariables());
          }catch(Exception e){
              e.printStackTrace();
              return null;
          }

    }
    
    /**
     *  Return a Vector of <code>ClassifierMeasureNode</code> with the cl
     *  classifier based order for the variables of this data set.
     */
    public Vector getClassifierBasedOrder(MixedClassifier cl){
        if (!this.cMeasures.isOrder(cl.getClass().getName())){
            this.cMeasures.addOrder(this.calculateClassifierBasedOrder(cl));
        }
        return this.cMeasures.getOrder(cl.getClass().getName());
    }

    /**
     *  Return a Vector of Integer, where v(i) conatins the number of the variable
     *  that has the i-th position in the Gaussian_Naive_Bayes classifier order.
     */
    public Vector getNumberClassifierBasedOrder(MixedClassifier cl){
        if (!this.cMeasures.isOrder(cl.getClass().getName())){
            this.cMeasures.addOrder(this.calculateClassifierBasedOrder(cl));
        }
        return this.cMeasures.getNumberOrder(cl.getClass().getName(),this.getVariables());
    }
    
    /**
     *  Remove all classifier based orders store in this class.
     */
    public void removeClassifierBasedOrders(){
        this.cMeasures=new ClassifierBasedMeasures();
    }

    /**
     *  This method return a copy of a DataBaseCases object.
     */
    public DataBaseCases copy(){

        Node n=this.getVarClass();
        int ind=this.getVariables().getId(n);
        ClassifierDBC cdbc=new ClassifierDBC(new String(getName()),(CaseList)getCases().copy(),ind);//cfm.getIndVarClass());

        cdbc.KFC=new Vector();
        if (this.KFC!=null)
            for (int i=0; i<this.KFC.size(); i++)
                cdbc.KFC.addElement(((ClassifierDBC)this.KFC.elementAt(i)).copy());
        cdbc.LOO=new Vector();
        if (this.LOO!=null)
            for (int i=0; i<this.LOO.size(); i++)
                cdbc.LOO.addElement(((ClassifierDBC)this.LOO.elementAt(i)).copy());


        cdbc.cfm=this.cfm.copy();
        cdbc.cMeasures=this.cMeasures.copy();

        //if (this.discretization!=null)
        //    cdbc.discretization=this.discretization.copy();
        
        
        return cdbc;   
    }

    /**
     *  This method projects a this database object and all fields
     *  that have whatever <code>NodeList</code> above the NodList nodes.
     */
    public void projection(NodeList nodes){

      Vector vector= this.getRelationList();
      Relation relation= (Relation) vector.elementAt(0);
      CaseListMem caselistmem=(CaseListMem)relation.getValues();

      ContinuousCaseListMem c= caselistmem.projection(nodes);
      relation.setValues(c);
      relation.setVariables(c.getVariables());
      this.setNodeList(new NodeList(c.getVariables()));

      for (int i=0;i<this.KFC.size();i++)
          ((ClassifierDBC)this.KFC.elementAt(i)).projection(nodes);

      for (int i=0;i<this.LOO.size();i++)
          ((ClassifierDBC)this.LOO.elementAt(i)).projection(nodes);

      this.cfm.projection(nodes);
      this.cMeasures.projection(nodes);
    }


    /**
     * 
     * @param objectFile 
     * @throws java.io.FileNotFoundException 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     * @return 
     */
    public static DataBaseCases readSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{

        System.out.println("deserializing...");        
        
        FileInputStream fis = new FileInputStream( objectFile );
        ObjectInputStream ois = new ObjectInputStream( fis );
        ClassifierDBC retrieved = (ClassifierDBC) ois.readObject();
        ois.close();
        
        System.out.println("deserialized...");        

        return retrieved;

    }

    /**
     * 
     * @param fis 
     * @throws java.io.FileNotFoundException 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     * @return 
     */
    public static DataBaseCases readSerialization(InputStream fis) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{
        ObjectInputStream ois = new ObjectInputStream( fis );
        ClassifierDBC retrieved = (ClassifierDBC) ois.readObject();
        ois.close();
        return retrieved;
    }

    /**
     * 
     * @param objectFile 
     * @throws java.io.FileNotFoundException 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     */
    public void writeSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {
        
        System.out.println("serializing...");
        
        File objectF = new File(objectFile);
        if ( objectF.exists() ) {
        objectF.delete();
        }
        FileOutputStream fos = new FileOutputStream( objectF);
        ObjectOutputStream oos = new ObjectOutputStream( fos );
        oos.writeObject( this );
        oos.flush();
        oos.close();
        oos.flush();
        fos.close();
        System.out.println("serialized...");
    }

    /**
     *  Return a Vector of ClassifierDBC with a k partition of this data set.
     *  This partition is stored for further operations.
     */
    public Vector getDbcKFC(int k){

        if (this.KFC.size()!=k){
            //System.out.println("EOEEOOOOEEEEEOO");
            DataBaseCases dbc=new DataBaseCases(new String(getName()),(CaseList)getCases().copy());
            this.KFC=new Vector();
            ClassifierValidator cv = new ClassifierValidator(new Mixed_Naive_Bayes(),dbc,this.getIndVarClass());
            try{
            cv.splitCases(k);
            }catch(Exception e){
                System.out.println("Exception en getDbcKFOLD");
                e.printStackTrace();
            };
            Vector subDBCs=cv.getSubSets();
            for (int i=0; i<subDBCs.size(); i++){
                DataBaseCases db=(DataBaseCases)subDBCs.elementAt(i);
                ClassifierDBC dbCont=new ClassifierDBC(db.getName(),db.getCases(),this.getIndVarClass());
                //dbCont.discretization=this.discretization;
                this.KFC.add(dbCont);
            }
        }

        return this.KFC;
    }

    
    
    /**
     *  This method applies a filter method to carry out a reduction of
     *  the number of  variables of this data set. The applied filter method 
     *  is named "Anova Phase" and it is described in [1].
     *
     *  @param threshold, it's a double that defines when a given variable is correlated with other one and 
     *  it has to be removed. 
     *  @return DataBaseCases, the DataBaseCases field projected over the selected variables
     *  by this fileter method.
     */
    
    public ClassifierDBC anovaFilter(double threshold){
        
        System.out.println("Hola");
        NodeList nl=this.getVariables();
        NodeList newnl=this.getSortAnovaNodes().getNodeList();
        
        NodeList newnl2=new NodeList();
        for (int indClass=0; indClass<this.numClass; indClass++){
            newnl2=new NodeList();
            System.out.println(newnl.size());
            while (newnl.size()>0){
                System.out.println("-"+newnl.size());
                Continuous nodeP=(Continuous)newnl.elementAt(0);
                newnl2.insertNode(nodeP);
                newnl.removeNode(0);
                int cont=0;
                while(cont<newnl.size()){
                    if (cont%100==0) System.out.print("-"+cont); 
                    double corr=this.correlationNodes(nodeP,(Continuous)newnl.elementAt(cont),indClass);
                    //if (corr<0) System.out.print(" C: "+corr);
                    if (Math.abs(corr)>threshold){
                        //System.out.print(" C: "+corr);
                        newnl.removeNode(cont);
                    }else
                        cont++;
                }
            }
            newnl=newnl2.copy();
        }
        
        //newnl2.insertNode(nl.elementAt(indVarClass));
        if (newnl2.getId(this.varClass)==-1)
            newnl2.insertNode(this.varClass);
        ClassifierDBC db=(ClassifierDBC)this.copy();
        db.projection(newnl2);
        System.out.println("FIN:"+this.getVariables().size()+"------------- "+db.getVariables().size());
        return db;
    }
    
    /**
     *  This class return a elvira.tools.statistic.analysis.Regression class with the regression of
     *  a Continuous node, nodeY, respect to a list of Continuous nodes, nodesX and only considering the 
     *  values for all nodes respect to the class number "Class".
     *  The method Regression.getCoeff() retursn a double[] object, where:
     *  nodeY=double[0] + double[1]*nodesX(1) + ... + double[n]*nodesX(n)
     */

    public Regression getRegressionClass(Continuous nodeY, NodeList nodesX, int Class){
        
        NodeList nodes= nodesX.copy();
        nodes.insertNode(nodeY);
        int[] ngroup = new int[this.numClass];
        double[][][] datos =this.getData(nodes,ngroup);
        
        double[] datosY=new double[ngroup[Class]];
        double[][] datosX=new double[nodesX.size()][ngroup[Class]];

        for (int i=0; i<nodes.size()-1; i++){
            for (int j=0; j<ngroup[Class]; j++)
                datosX[i][j]=datos[i][Class][j];
        }
        for (int j=0; j<ngroup[Class]; j++){
            datosY[j]=datos[nodes.size()-1][Class][j];
        }

        Regression R= new Regression(datosX,datosY);
        R.linear();
        
        return  R;
    }
    
    /**
     *  This method calls to "getRegressionClass" method, but only one node is considered in the 
     *  list of nodes.  
     */
    public Regression getRegressionClass(Continuous nodeY, Continuous nodeX, int Class){
        NodeList nl=new NodeList();
        nl.insertNode(nodeX);
        return this.getRegressionClass(nodeY,nl,Class);
    }

    
    /**
     *  Return a <code>ContinuousProbabilityTree</code> containing with the conditional 
     *  Gaussian Distribution of a <code>Continuous</code> node, nodeY, respect to a 
     *  list of <code>Continuous</code>nodes and each classifier class. 
     */
    public ContinuousProbabilityTree conditionalGaussianDistribution(Continuous nodeY, NodeList nodesX){

        ContinuousProbabilityTree tree = new ContinuousProbabilityTree(this.varClass);

        for (int k=0; k<this.numClass; k++){

            Regression R = this.getRegressionClass(nodeY,nodesX,k);
            double[] coef = R.getCoeff();
            Vector vals=new Vector();
            for (int i=0; i<nodesX.size(); i++)
                vals.add(new Double(coef[i+1]));
            LinearFunction lf = new LinearFunction(nodesX.getNodes(),vals);
            double desv=R.getResidualStandardDeviation();
            
            MixtExpDensity exp = new MixtExpDensity(nodeY,coef[0],lf,desv);

            tree.setChild(new ContinuousProbabilityTree(exp),k);
        }
        return tree;
    }
    

    /**
     *  Return the Pearson correlation coefficient given the class between the 
     *  two nodes 'node1' and 'node2'.
     */
    public double correlationNodes(Continuous node1, Continuous node2, int indClass){
            int [] ngroup = new int[this.numClass];
            NodeList nl = new NodeList();
            nl.insertNode(node1);
            nl.insertNode(node2);
            double[][][] datos=this.getData(nl, ngroup);
            
            return Stat.corrCoeff(datos[0][indClass],datos[1][indClass],ngroup[indClass]);
    }
    
    
    /**
     *  Return the Pearson correlation coefficient between the 
     *  two nodes 'node1' and 'node2'.
    */
    public double correlationNodes(Continuous node1, Continuous node2){
            int [] ngroup = new int[this.numClass];
            NodeList nl = new NodeList();
            nl.insertNode(node1);
            nl.insertNode(node2);
            double[][][] datos=this.getData(nl, ngroup);
            int total=0;
            for (int i=0; i<this.numClass; i++)
                total+=ngroup[i];
            
            int cont=0;
            double[] d1=new double[total];
            double[] d2=new double[total];
            for (int i=0; i<this.numClass; i++)
                for (int j=0; j<ngroup[i]; j++){
                    d1[cont]=datos[0][i][j];
                    d2[cont]=datos[1][i][j];
                    cont++;
                }
            
            return Stat.corrCoeff(d1,d2,cont);
    }
    

    /**
     * @return <code>int[]</code> an integer vector, where a[i] contain the 
     *  number of cases belonging to class i.
     */
    public int[] getCasesClass(){
        int[] casesclass=new int[numClass];
        for (int j=0; j<this.numClass; j++)
            casesclass[j]=0;

        for (int i=0; i<this.getNumberOfCases(); i++){
            double clas=this.getCaseListMem().getValue(i,this.indVarClass);
            casesclass[(int)clas]++;
        }
        
        return casesclass;
    }
    
    /**
     * @return <code>int[][]</code> an matrix of integers, where a[i] is a vector that
     *  contains the number of cases of the DataBaseCases whose state of class variable
     * is the number i-th.
     */
    public int[][] getNumberCasesByClass(){
        
        int[][] casesclass=new int[this.numClass][this.getNumberOfCases()];
        int[] numcasesclass=new int[this.numClass];
        for (int j=0; j<this.numClass; j++)
            numcasesclass[j]=0;

        for (int i=0; i<this.getNumberOfCases(); i++){
            double clas=this.getCaseListMem().getValue(i,this.indVarClass);
            casesclass[(int)clas][numcasesclass[(int)clas]]=i;
            numcasesclass[(int)clas]++;
        }
        
        return casesclass;
    }

    /*
     * @return <code>int[]</code>, an integer vector where a[i] contains the number
     *  of cases that are defined, there isn't missing values, for the two nodes.
     */
    /**
     * 
     * @param node1 
     * @param node2 
     * @return 
     */
    protected int[] getCasesClassNodes(Node node1, Node node2){
            NodeList nl = new NodeList();
            nl.insertNode(node1);
            nl.insertNode(node2);
            return this.getCasesClassNodes(nl);
    }

    /*
     * @return <code>int[]</code>, an integer vector where a[i] contains the number
     *  of cases that are defined, there isn't missing values, for all nodes in the 
     *  <code>NodeList</code> nl.
     */
    /**
     * 
     * @param nl 
     * @return 
     */
    protected int[] getCasesClassNodes(NodeList nl){
        
        int [] cases = new int[this.numClass];
        for (int i=0; i<this.numClass; i++)
            cases[i]=this.getNumberOfCases()+1;
        for (int i=0; i<nl.size(); i++){
            int [] ngroup=new int[this.numClass];
            double[][] datos=this.getData((Continuous)nl.elementAt(i),ngroup);
            for (int j=0; j<this.numClass; j++)
                if (cases[j]>ngroup[j])
                    cases[j]=ngroup[j];
        }
        return cases;
    }

    /**
     *  Return a double vector with the values in the DataBaseCases data in the 
     *  case number 'numCase'.
     */
    protected double[] getDataClass(int numCase){
        
        ContinuousCaseListMem cases=(ContinuousCaseListMem)this.getCaseListMem();
        
        Vector datos=new Vector();
        for (int i=0; i<cases.getVariables().size(); i++)
            if (cases.getVariables().elementAt(i).getClass()==Continuous.class)
                datos.addElement(new Double(cases.getValue(numCase,i)));
        
        double[] data=new double[datos.size()];
        for (int i=0; i<data.length; i++)
            data[i]=((Double)datos.elementAt(i)).doubleValue();
        return data;
    }
    
    /**
     *  Return the data for all the cases and for each class to classify of a 
     *  Continuous variable. So, data[i] contains all values for the i-th classifier
     *  class. In addition, ngroup[i] contains the number of cases the variable 
     *  for the i-th class witout missing values.
     */
    public double [][] getData(Continuous variable, int [] ngroup){

        for (int i=0; i<ngroup.length; i++)
            ngroup[i]=0;

        int n=this.getNumberOfCases();
        double[][] datos=new double[numClass][n];
        ContinuousCaseListMem cl=(ContinuousCaseListMem)this.getCases();

        NodeList nl=new NodeList();
        nl.insertNode(varClass);
        nl.insertNode(variable);
        
        /*
        int[] index=new int[nl.size()];
        for (int i=0; i<nl.size(); i++){
            index[i]=this.getVariables().getId(nl.elementAt(i));
        }
        */
        int[] index=cl.getIndexGetQuickly(nl);
        
        for (int i=0; i<numClass; i++){
            ngroup[i]=0;
            int cont=0;
            for (int j=0; j<n; j++){
                ContinuousConfiguration conf= (ContinuousConfiguration)cl.getQuickly(j,nl,index);

                int state=conf.getValue(varClass);
                if (state==i && conf.getValue(variable)!=variable.undefValue()){
                    ngroup[i]++;
                    datos[i][cont]=conf.getValue(variable);
                    cont++;
                }
            }
        }

        return datos;
    }
       
    /**
     *  Return the data for all the cases,  for each class to classify and for 
     *  each Continuous variable of the NodeList nodes, where there isn't any missing
     *  value in any case of any variable of the NodeList nodes.
     *  So, data[i][j] contains all values for the j-th classifier
     *  class and for the i-th variable of NodeList nodes. In addition, ngroup[i] contains the number of cases 
     *  for the i-th class witout missing values in any variable.
     */

    public double[][][] getData(NodeList nodes, int[] ngroup){
        
        ContinuousCaseListMem cl=(ContinuousCaseListMem)this.getCases();

        for (int i=0; i<ngroup.length; i++)
            ngroup[i]=0;

        NodeList nl=new NodeList();
        nl.insertNode(varClass);
        for (int i=0; i<nodes.size(); i++)
            nl.insertNode(nodes.elementAt(i));
        
        /*
        int[] index=new int[nl.size()];
        for (int i=0; i<nl.size(); i++){
            index[i]=this.getVariables().getId(nl.elementAt(i));
        }*/
        
        int[] index=cl.getIndexGetQuickly(nl);
        
        int n=this.getNumberOfCases();
        
        double[][][] datos=new double[nodes.size()][numClass][n];
        
        
        for (int i=0; i<numClass; i++){
            ngroup[i]=0;
            int cont=0;
            Vector number=new Vector();
            for (int j=0; j<n;j++)
                number.addElement(new Integer(j));

            for (int j=0; j<n; j++){
             ContinuousConfiguration conf= (ContinuousConfiguration)cl.getQuickly(j,nl,index);
             int state=conf.getValue(varClass);
             boolean completo=true;
             for (int k=0; k<nodes.size();k++){
                 Continuous variable = (Continuous)nodes.elementAt(k);
                 if (conf.getValue(variable)==variable.undefValue())
                     completo=false;
             }
             if (state==i && completo){
                 ngroup[i]++;
                 for (int k=0; k<nodes.size();k++){
                     Continuous variable = (Continuous)nodes.elementAt(k);
                     datos[k][i][cont]=conf.getValue(variable);
                 }
                 cont++;
             }
            }
        }

        return datos;
    }
    
    /**
     *  Return the t-statistic Measure, described in [1], for the variable 'variable'. 
     */
    public double calcTMeasure(Continuous variable){
        
        int n=this.getNumberOfCases();
        int[] ngroup=new int[2];
        double[][] datos=getData(variable,ngroup);

        t_statistics T=new t_statistics(datos[0],ngroup[0],datos[1],ngroup[1]);
        return T.calc_t();
    }
    
    /**
     *  Return the number of loss data, described in [1], for the variable 'variable'. 
     */
    public int numLossData(Continuous variable){
     
        int indice=this.getVariables().getId(variable);
        int cont=0;
        for (int i=0; i<this.getNumberOfCases(); i++)
              if (((ContinuousCaseListMem)this.getCases()).getValue(i,indice)==variable.undefValue())
                      cont++;
        return cont;
    }

    
    /**
     * Return an <code>FilteredNodeList</code> class with the variables sorted by its 
     * Anova measure, from higher to lower.
     */
    public FilteredNodeList getSortAnovaNodes(){
        if (!this.cfm.isMeasure(ContinuousFilterMeasures.ANOVA_MEASUREORDER)){
            NodeList nl=this.getVariables();
            FilteredNodeList auxNl= new FilteredNodeList(nl.size()-1);
            for (int i=0,cont=0; i<nl.size(); i++){
                if (i!=this.indVarClass){
                    Node auxnode=nl.elementAt(i);
                    auxNl.setFilteredNode(new FilteredNode(auxnode,this.calcAnovaMeasure((Continuous)auxnode)),cont);
                    cont++;
                    if (i%500==0) System.out.println("Anova: "+i);
                }
            }

            auxNl.sortDescendant();
            this.cfm.addMeasureOrder(ContinuousFilterMeasures.ANOVA_MEASUREORDER,auxNl);
            return auxNl;
        }else{
            return this.cfm.getMeasureOrder(ContinuousFilterMeasures.ANOVA_MEASUREORDER);
        }
    }

   /**
    *  This method returns the conditional mutual information given the class for a pair
    *  of continuous nodes.
    */
    public double cMutInf(Continuous node1, Continuous node2){
     
        int[] casesclass=this.getCasesClass();
        double acum=0.0;
        for (int i=0; i<this.varClass.getNumStates(); i++){
            double corr=this.correlationNodes(node1,node2, i);
            if (corr==1.0){
                acum=Double.MAX_VALUE;
                break;
            }else{
                acum+=(casesclass[i]/(double)this.getNumberOfCases())*(-0.5)*Math.log(1-corr*corr);
            }
        }

        return acum/this.varClass.getNumStates();

    }


    /**
     *  Return a projection of the n first Anova nodes of this 
     *  data set. The n first Anova nodes is given by 
     *  "getSortAnovaNodes" method.
     */
    public ClassifierDBC filterFirstAnovaNodes(int n){

        NodeList nl= new NodeList();
        nl.insertNode(this.varClass);
        NodeList nl2=this.getSortAnovaNodes().getNodeList();
        for (int i=0; i<n; i++)
            nl.insertNode(nl2.elementAt(i));

        ClassifierDBC dbc=(ClassifierDBC)this.copy();
        dbc.projection(nl);
        return dbc;
    }


    /**
     *  Return a ClassifierMeasureNode Vector with the order given by 
     *  the reached accurcay using the classifier 'cl'. 
     *  The complete definition of this order can be found in [2].
     */

    public Vector calculateClassifierBasedOrder(MixedClassifier cl){
        
        NodeList nl=cl.getDataBaseCases().getVariables().copy();
        nl.removeNode(this.varClass);
        FilteredNodeList auxNl= new FilteredNodeList(nl.size());
        Vector cms=new Vector();
        int cont=0;
        for (int i=0; i<nl.size(); i++){
            try{
                if (i%100==0)
                    System.out.println("A: "+i+" de "+nl.size());
                DataBaseCases dbc=cl.getDataBaseCases().copy();
                NodeList nodes=new NodeList();
                nodes.insertNode(this.varClass);
                nodes.insertNode(nl.elementAt(i));
                dbc.projection(nodes);
                int ind=dbc.getVariables().getId(this.varClass);
                AvancedConfusionMatrix acm=(AvancedConfusionMatrix)cl.evaluationLOO(dbc,ind);
                nodes.removeNode(this.varClass);
                ClassifierMeasureNode qcm=new ClassifierMeasureNode(acm,nodes,cl);
                qcm.setComment(new String(cl.getClass().getName()));
                //qcm.getClassAccuracy();
                //qcm.getClassPredicted();
                //qcm.setTypeDistribution(0,cl.getType());
                qcm.setTypeDistribution(0,0);
                cms.addElement(qcm);
                auxNl.setFilteredNode(new FilteredNode(nl.elementAt(i),acm.getAccuracy()),cont);
                cont++;
            }catch(Exception e){
                System.out.println("Exception en: "+this.getClass().getName());
                e.printStackTrace();
            }
        }
        auxNl.sortDescendant();
        
        Vector order=new Vector();
        Vector qCMs=new Vector();
        for (int i=0; i<auxNl.getSize(); i++){
            Node node=auxNl.getFilteredNode(i).getNode();
            for (int j=0; j<cms.size(); j++){
                ClassifierMeasureNode qcm=(ClassifierMeasureNode)cms.elementAt(j);
                if (qcm.getNodes().elementAt(0).equals(node)){
                    qCMs.addElement(qcm);
                    order.addElement(new Integer(this.getVariables().getId(node)));                               
                    break;
                }
            }
        }
        return qCMs;
    }

    /**
     *  This method returns the p-value of a Continuous node follows a Normal Distribution given a class 
     *  using the Kolmogorov-Smirnov test.
     *  @return null, if there has been a failure in the process.
     *          double[], where double[i] contains the p-value with the probability of 
     *          the Continuous node follows a normal distribution given the i-th class.
     *          if double[i]>MathMAE.P_THRESHOLD, the hypothesis is usually not rejected.
     */

    public double[] KSNormalityTestClass(Continuous node){
        double[] returns=new double[this.numClass];
        int[] ngroup = new int[this.numClass];
        double[][] data=this.getData(node, ngroup);
        for (int i=0; i<this.numClass; i++){
            if (!MathMAE.nr_ksoneNormal(Stat.adjust(data[i],ngroup[i])))
                return null;
            returns[i]=MathMAE.probKS;
        }
        return returns;
    }

    
    
    /**
     *  This method returns the p-value of a Continuous node follows a LogNormal Distribution given a class 
     *  using the Kolmogorov-Smirnov test.
     *  @return null, if there has been a failure in the process.
     *          double[], where double[i] contains the p-value with the probability of 
     *          the Continuous node follows a normal distribution given the i-th class.
     *          if double[i]>MathMAE.P_THRESHOLD, the hypothesis is usually not rejected.
     */
    public double[] KSLogNormalityTestClass(Continuous node){
        double[] returns=new double[this.numClass];
        int[] ngroup = new int[this.numClass];
        double[][] data=this.getData(node, ngroup);
        for (int i=0; i<this.numClass; i++){
            double[] data2=Stat.adjust(data[i],ngroup[i]);
            double[] data3=Stat.makePositive(data2);
            if (!MathMAE.nr_ksoneLogNormal(data3))
                return null;
            returns[i]=MathMAE.probKS;
        }
        return returns;
    }

    
    /*
     *
     */
    /**
     * 
     * @param n 
     * @return 
     */
    public double[][] distributionTest(int n){
       NodeList nl=this.getVariables().randomOrder();
       NodeList nl2=new NodeList();
       for (int i=0; i<n; i++)
           if (nl.elementAt(i).getClass()==Continuous.class)
                nl2.insertNode(nl.elementAt(i));
       
       double[][] result=new double[4][this.numClass+1];
       
       result[0]=this.SeveralKSNormalityTestClass(nl2);
       result[1]=this.SeveralKSLogNormalityTestClass(nl2);
       
       result[2][0]=0.0;
       result[3][0]=0.0;
       for (int i=0; i<nl2.size(); i++){
           if (this.KSNormalityTest((Continuous)nl2.elementAt(i))) 
            result[2][0]++;
           if (this.KSLogNormalityTest((Continuous)nl2.elementAt(i)))
            result[3][0]++;
       }
       result[2][0]/=nl2.size();
       result[3][0]/=nl2.size();
       
       return result;
    }
    
    /**
     * Carry out a Normality Kolomogorov-Smirnov Test for n random nodes
     * given the class in the data set.
     * @param n, the number of random nodes to test.
     * @return double[], double[0], the proportion of variables that follow
     * a Normal distribution given the class for all the classes. double[i] the proportion
     * of variables que follow a Normal distribution given the class (i-1)-th taken a threshold
     * for the p-vale equals to MathMAE.P_THRESHOLD.
     */
    public double[] SeveralKSNormalityTestClass(int n){
       NodeList nl=this.getVariables().randomOrder();
       NodeList nl2=new NodeList();
       for (int i=0; i<n; i++)
           nl2.insertNode(nl.elementAt(i));
       return this.SeveralKSNormalityTestClass(nl2);
    }
    
    /**
     * Carry out a Normality Kolomogorov-Smirnov Test for each node of NodeList nl
     * given the class in the data set.
     * @param nl, a NodeList with the nodes to test.
     * @return double[], double[0], the proportion of variables that follow
     * a Normal distribution given the class for all the classes. double[i] the proportion
     * of variables que follow a Normal distribution given the class (i-1)-th taken a threshold
     * for the p-vale equals to MathMAE.P_THRESHOLD.
     */
    public double[] SeveralKSNormalityTestClass(NodeList nl){
    
        double[] result=new double[this.numClass+1];
        for (int i=0; i<result.length; i++)
            result[i]=0.0;
        int cont=0;

        while (cont < nl.size()){
            if (nl.elementAt(cont).getClass()==Continuous.class){
                double[] test=this.KSNormalityTestClass((Continuous)nl.elementAt(cont));
                if (test!=null){
                    boolean all=true;
                    for (int j=0; j<this.numClass; j++){
                        try{
                        if (test[j]<MathMAE.P_THRESHOLD)
                            all=false;
                        else
                            result[j+1]++;
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    if (all)
                        result[0]++;
                }
                cont++;
            }
        }
        for (int i=0; i<result.length; i++)
            result[i]/=nl.size();
        
        return result;
    }

    /**
     * Carry out a LogNormality Kolomogorov-Smirnov Test for n random nodes
     * given the class in the data set.
     * @param n, the number of random nodes to test.
     * @return double[], double[0], the proportion of variables that follow
     * a Normal distribution given the class for all the classes. double[i] the proportion
     * of variables que follow a LogNormal distribution given the class (i-1)-th taken a threshold
     * for the p-vale equals to MathMAE.P_THRESHOLD.
     */
    public double[] SeveralKSLogNormalityTestClass(int n){
       NodeList nl=this.getVariables().randomOrder();
       NodeList nl2=new NodeList();
       for (int i=0; i<n; i++)
           nl2.insertNode(nl.elementAt(i));
       return this.SeveralKSLogNormalityTestClass(nl2);
    }

    /**
     * Carry out a Normality Kolomogorov-Smirnov Test for each node of NodeList nl
     * given the class in the data set.
     * @param nl, a NodeList with the nodes to test.
     * @return double[], double[0], the proportion of variables that follow
     * a Normal distribution given the class for all the classes. double[i] the proportion
     * of variables que follow a Normal distribution given the class (i-1)-th taken a threshold
     * for the p-vale equals to MathMAE.P_THRESHOLD.
     */
    public double[] SeveralKSLogNormalityTestClass(NodeList nl){
        //NodeList nl=this.getVariables().randomOrder();
        double[] result=new double[this.numClass+1];
        for (int i=0; i<result.length; i++)
            result[i]=0.0;
        int cont=0;
        while (cont < nl.size()){
            if (nl.elementAt(cont).getClass()==Continuous.class){
                double[] test=this.KSLogNormalityTestClass((Continuous)nl.elementAt(cont));
                if (test!=null){
                    boolean all=true;
                    for (int j=0; j<this.numClass; j++){
                        if (test[j]<MathMAE.P_THRESHOLD)
                            all=false;
                        else
                            result[j+1]++;
                    }
                    if (all)
                        result[0]++;
                }
                cont++;
            }
        }
        for (int i=0; i<result.length; i++)
            result[i]/=nl.size();
        
        return result;
    }
    
    /**
     * 
     * @param TAM_MUESTRA 
     * @return 
     */
    public double[][][] correlationBetweenNodes(int TAM_MUESTRA){
/*
        Vector v= this.getGNBBasedOrder();
        NodeList nodes=new NodeList();
        for (int i=0; i<TAM_MUESTRA; i++){
            ClassifierMeasureNode measure=(ClassifierMeasureNode)v.elementAt(i);
            nodes.insertNode(measure.getNodes().elementAt(0));
        }
 */
   
        NodeList nodes=new NodeList();
        for (int i=0; i<TAM_MUESTRA; i++){
            nodes.insertNode(this.getSortAnovaNodes().getNodeList().elementAt(i));
        }
        
        Random r= new Random();
        Node node1,node2;
        double[][] datos=new double[this.getNumberOfStates()+1][TAM_MUESTRA];
        for (int i=0; i<TAM_MUESTRA; i++){
            System.out.print(" M: "+i);
            do{
                int n=r.nextInt(nodes.size()-1);
                node1=nodes.elementAt(n);
            }while(node1.getClass()!=Continuous.class);
            do{
                int n=r.nextInt(nodes.size()-1);
                node2=nodes.elementAt(n);
            }while(node2.getClass()!=Continuous.class);
            
            for (int j=0; j<this.getNumberOfStates(); j++){
                datos[j][i]=Math.abs(this.correlationNodes((Continuous)node1,(Continuous)node2,j));
            }
            datos[this.getNumberOfStates()][i]=Math.abs(this.correlationNodes((Continuous)node1,(Continuous)node2));
        }
        
        double[][][] retorno=new double[this.getNumberOfStates()+1][][];
        for (int i=0; i<this.getNumberOfStates(); i++)
            retorno[i]=Stat.histogramBins(datos[i],0.1,0.0);
        retorno[this.getNumberOfStates()]=Stat.histogramBins(datos[this.getNumberOfStates()],0.1,0.0);
        return retorno;
    }
    
    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//      All this methods currently are in probes.
    
    /**
     * 
     * @return 
     */
    
    public ClassifierDBC discretize(){
        if (this.discretization==null){
            Vector  Opts;
            Opts=new Vector();
            this.discretization = new Discretization();
            this.discretization.LoadData(super.copy());
            this.discretization.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
            this.discretization.SetOperation(Discretization.MASSIVE_OPERATION);
            this.discretization.ConfigureIndividual(Discretization.EQUAL_FREQUENCY,this.numInterval,Opts);
            try{
                return new ClassifierDBC(this.discretization.apply(),this.indVarClass);
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }else{
            return this.discretizeAgain(super.copy());
        }
    }
      
    /**
     * 
     * @param data 
     * @return 
     */
    
    public ClassifierDBC discretizeAgain(DataBaseCases data){

        Vector  Opts;
        Opts=new Vector();
        this.discretization.LoadData(data);
        this.discretization.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
        this.discretization.SetOperation(Discretization.MASSIVE_OPERATION);
        this.discretization.ConfigureIndividual(Discretization.EQUAL_WIDTH,this.numInterval,Opts);
        
        try{
            return new ClassifierDBC(this.discretization.applyAgain(),this.indVarClass);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }
     
    /**
     * 
     * @return 
     */
    
    public Discretization getDiscretization(){
        if (this.discretization==null){
            this.discretize();
        }
        return this.discretization;
    }

    /**
     *  Return an <code>FilteredNodeList<code> with a new order based on 
     *  in the search of an optimal TAN order. The order is defined as:
     *  o(1)=arg Max{Anova(n_i)};
     *  o(i+i)=Max{Anova(
     *
     *
     */

    //Anova total
    public FilteredNodeList getSortTANAnovaNodes(){

        NodeList nl=this.getSortAnovaNodes().getNodeList();

        NodeList newnl= new NodeList();
        newnl.insertNode(nl.elementAt(0));

        FilteredNodeList anovaTANOrder=new FilteredNodeList(this.getVariables().size()-1);
        anovaTANOrder.setFilteredNode(new FilteredNode(nl.elementAt(0),this.anovaMeasure((Continuous)nl.elementAt(0))),0);            

        for (int K=1; K<this.getVariables().size()-1; K++){
            System.out.println(K);

            Continuous actual=(Continuous)newnl.lastElement();
            nl.removeNode(actual);
            int[] ngroupActual=new int[numClass];        
            double[][] datosActual=this.getData(actual,ngroupActual);
            FilteredNodeList auxNl= new FilteredNodeList(nl.size());
            for (int i=0; i<nl.size(); i++){
                Continuous continuo=(Continuous)nl.elementAt(i);
                anova A=new anova();
                int[] ngroup=new int[numClass];
                double[][] datos=this.getData(continuo,ngroup);

                int[] ngroupMin =this.getCasesClassNodes(continuo,actual);

                for (int j=0; j<this.numClass; j++){

                    Regression R=this.getRegressionClass(continuo,actual,j);
                    double[] coef = R.getCoeff();
                    //for (int k=0; k<coef.length; k++)
                    for (int k=0; k<ngroupMin[j]; k++)
                        datos[j][k]-=(datosActual[j][k]*coef[1]+coef[0]);
                }
                A.setData(datos,ngroupMin,numClass);
                //                return A.getF();
                auxNl.setFilteredNode(new FilteredNode(continuo,A.getF()),i);                
            }
            auxNl.sortDescendant();
            newnl.insertNode(auxNl.getFilteredNode(0).getNode());
            anovaTANOrder.setFilteredNode(auxNl.getFilteredNode(0),K);
        }
        return anovaTANOrder;
    
    }

    /**
     * 
     * @return 
     */
    public FilteredNodeList getSortTANAnovaNodes2(){
        
       
        
            NodeList nl=this.getSortAnovaNodes().getNodeList();

            NodeList newnl= new NodeList();
            newnl.insertNode(nl.elementAt(0));
            
            FilteredNodeList anovaTANOrder=new FilteredNodeList(this.getVariables().size()-1);
            anovaTANOrder.setFilteredNode(new FilteredNode(nl.elementAt(0),this.anovaMeasure((Continuous)nl.elementAt(0))),0);            
            
            for (int K=1; K<this.getVariables().size()-1; K++){
                System.out.println(K);
                nl.removeNode(newnl.lastElement());

                int[] ngroupActual=new int[numClass];        
                double[][][] datosActual=this.getData(newnl,ngroupActual);
                FilteredNodeList auxNl= new FilteredNodeList(nl.size());

                for (int L=0; L<newnl.size();L++){
                    Continuous actual=(Continuous)newnl.elementAt(L);
                    
                    for (int i=0; i<nl.size(); i++){
                            Continuous continuo=(Continuous)nl.elementAt(i);
                            anova A=new anova();
                            int[] ngroup=new int[numClass];
                            double[][] datos=this.getData(continuo,ngroup);

                            NodeList tmp=newnl.copy();
                            tmp.insertNode(continuo);
                            int[] ngroupMin =this.getCasesClassNodes(tmp);

                            for (int j=0; j<this.numClass; j++){
                                Regression R=this.getRegressionClass(continuo,newnl,j);
                                double[] coef = R.getCoeff();
                                //for (int k=0; k<coef.length; k++)
                                for (int k=0; k<ngroupMin[j]; k++){
                                    datos[j][k]-=coef[0];
                                    for (int l=0; l<coef.length-1; l++)
                                        datos[j][k]-=(datosActual[l][j][k]*coef[l+1]);
                                }
                            }
                            A.setData(datos,ngroupMin,numClass);
                            //                return A.getF();
                            auxNl.setFilteredNode(new FilteredNode(continuo,A.getF()),i);                
                        }
                }
                auxNl.sortDescendant();
                newnl.insertNode(auxNl.getFilteredNode(0).getNode());
                anovaTANOrder.setFilteredNode(auxNl.getFilteredNode(0),K);
            }
            return anovaTANOrder;
    
    }
    
    
    /**
     * 
     * @param nodesX 
     * @return 
     */
    public FilteredNodeList getSortTANAnovaNodesMoralPartial(NodeList nodesX){
           
        
            NodeList nl=this.getSortAnovaNodes().getNodeList();
            for (int i=0; i<nodesX.size();i++)
                nl.removeNode(nodesX.elementAt(i));

            FilteredNodeList auxNl=new FilteredNodeList(nl.size());            
            
            for (int i=0; i<nl.size();i++){

                //System.out.println("TAN :"+i+" de "+nl.size());
                Continuous actual =(Continuous) nl.elementAt(i);
                
                int indMax=-1;
                double valMax=Double.MAX_VALUE;
                for (int j=0; j<nodesX.size();j++){
                    try{
                        double [][] data =new double[this.varClass.getNumStates()][];
                        int [] ngroup=new int[this.varClass.getNumStates()];
                        for (int k=0; k<this.varClass.getNumStates();k++){
                            Regression R=this.getRegressionClass(actual,(Continuous)nodesX.elementAt(j),k);
                            data[k]=R.getResiduals();
                            ngroup[k]=data[k].length;
                        }
                        anova A=new anova();
                        A.setData(data,ngroup,this.varClass.getNumStates());
                        double val=A.getF();
                        if (val<valMax){
                            valMax=val;
                            indMax=j;
                        }
                   }catch(Exception e){};
                }
                auxNl.setFilteredNode(new FilteredNode(actual,valMax),i);                
            }
                
            auxNl.sortDescendant();            
            return auxNl;//.getFilteredNode(0).getNode();
    }

    /**
     * 
     * @param nodesX 
     * @return 
     */
    public FilteredNodeList getSortTANAnovaNodesMoralPartialSimple(NodeList nodesX){
           
        
            NodeList nl=this.getSortAnovaNodes().getNodeList();
            for (int i=0; i<nodesX.size();i++)
                nl.removeNode(nodesX.elementAt(i));

            FilteredNodeList auxNl=new FilteredNodeList(nl.size());            
            
            for (int i=0; i<nl.size();i++){

                Continuous actual =(Continuous) nl.elementAt(i);
                
                int indMax=-1;
                double valMax=Double.MIN_VALUE;
                for (int j=nodesX.size()-1; j<nodesX.size();j++){
                    double [][] data =new double[this.varClass.getNumStates()][];
                    int [] ngroup=new int[this.varClass.getNumStates()];
                    for (int k=0; k<this.varClass.getNumStates();k++){
                        Regression R=this.getRegressionClass(actual,(Continuous)nodesX.elementAt(j),k);
                        data[k]=R.getResiduals();
                        ngroup[k]=data[k].length;
                    }
                    anova A=new anova();
                    A.setData(data,ngroup,this.varClass.getNumStates());
                    double val=A.getF();
                    
                    if (val>valMax){
                        valMax=val;
                        indMax=j;
                    }
                }
                auxNl.setFilteredNode(new FilteredNode(actual,valMax),i);                
            }
                
            auxNl.sortDescendant();            
            return auxNl;//.getFilteredNode(0).getNode();
    }

    /**
     * 
     * @return 
     */
    public FilteredNodeList getSortTANAnovaNodesMoral(){
        
        //if (this.anovaTANOrder==null){
        if(!this.cfm.isMeasure(ContinuousFilterMeasures.ANOVATAN_MEASUREORDER)){
            NodeList order=new NodeList();

            NodeList nl=this.getSortAnovaNodes().getNodeList().copy();
            order.insertNode(nl.elementAt(0));

            for (int i=1;i<nl.size();i++){
                FilteredNodeList auxNl=this.getSortTANAnovaNodesMoralPartial(order);
                Node n = auxNl.getFilteredNode(0).getNode();
                order.insertNode(n);
                System.out.println("TAN: "+i);
            }

            FilteredNodeList auxnl=new FilteredNodeList(order.size());
            for (int i=0; i<order.size();i++)
                auxnl.setFilteredNode(new FilteredNode(order.elementAt(i),0.0), i);

            return auxnl;
        }else{
            //return this.anovaTANOrder;
            return this.cfm.getMeasureOrder(ContinuousFilterMeasures.ANOVATAN_MEASUREORDER);
        }
    }


    /**
     * 
     * @param TAM_MUESTRA 
     * @throws java.lang.Exception 
     */
    public static void analizaCorrelacion1variable(int TAM_MUESTRA) throws Exception{
        
        String fichero=new String("c:\\andres\\tmp\\bayelvira2\\WGA.x");
        
        //FileInputStream f=new FileInputStream(fichero);
        DataBaseCases dbc=DataBaseCases.readSerialization(fichero);//new DataBaseCases(fichero);
        System.out.println("putamadre");
        
        int NC=dbc.getNumberOfCases();
        
        ClassifierDBC cfm=new ClassifierDBC(dbc,0);
        PrintWriter salida = new PrintWriter(new FileWriter("c:\\andres\\tmp\\bayelvira2\\Correlacion1Variable.txt"));
        
        int N=dbc.getVariables().size();
        double [][] datos = new double[4][TAM_MUESTRA];
        NodeList nodes= dbc.getVariables().copy();
        nodes.removeNode(0);
        //for (int i=1; i<N; i++){
        int cont=0;
        Random r= new Random();
        while(cont<TAM_MUESTRA){
            System.out.println("Nodo: "+cont);
            int n=r.nextInt(nodes.size()-1);
            Continuous node=(Continuous)nodes.elementAt(n);
            nodes.removeNode(n);
            NodeList nl=new NodeList();
            nl.insertNode(dbc.getVariables().elementAt(0));
            nl.insertNode(node);
            ContinuousCaseListMem caseL=(ContinuousCaseListMem)dbc.getCases();
            DataBaseCases dbc2= new DataBaseCases("hoila",nl,caseL.projection(nl));
            //System.out.println("putamadre1");
            Gaussian_Naive_Bayes gnb=new Gaussian_Naive_Bayes(dbc2,false,0);
            ClassifierValidator cf=new ClassifierValidator(gnb,dbc2,0);
            //double accuracy=1-cf.leaveOneOut().getError();
            //System.out.println("putamadre2");
            double accuracy=1-cf.kFoldCrossValidation(10).getError();
            //System.out.println("putamadre3");
            datos[0][cont]=cfm.anovaMeasure(node);
            datos[1][cont]=cfm.numLossData(node);
            datos[2][cont]=accuracy;
            datos[3][cont]=cfm.calcTMeasure(node);
            //System.out.println("putamadre4");
            for (int k=0; k<datos.length; k++)
                    salida.write(datos[k][cont]+"\t");
            salida.write("\n");
            cont++;
        }
        salida.close();
        
        for (int i=0; i<datos.length; i++)
            for (int j=i+1; j<datos.length; j++){
                System.out.println(Stat.corrCoeff(datos[i],datos[j]));
            }
    }
    
    /**
     * 
     * @param TAM_MUESTRA 
     * @throws java.lang.Exception 
     */
    public static void analizaCorrelacion2variable(int TAM_MUESTRA) throws Exception{
        
        String fichero=new String("c:\\andres\\tmp\\bayelvira2\\WGA.x");
        //FileInputStream f=new FileInputStream(fichero);
        DataBaseCases dbc=DataBaseCases.readSerialization(fichero);//new DataBaseCases(fichero);
        System.out.println("putamadre");

        ClassifierDBC cfm=new ClassifierDBC(dbc,0);
        PrintWriter salida = new PrintWriter(new FileWriter("c:\\andres\\tmp\\bayelvira2\\Correlacion2Variable.txt"));
        
        int N=dbc.getVariables().size();
        double [][] datos = new double[9][TAM_MUESTRA];
        int cont=0;
        NodeList nodes=dbc.getVariables().copy();
        nodes.removeNode(0);
        
        Random r=new Random();
        while(cont<TAM_MUESTRA){
            System.out.println("Nodo: "+cont);
            int n=r.nextInt(N-1);
            Continuous node1=(Continuous)nodes.elementAt(n);
            int m=0;
            while ((m=r.nextInt(N-1))==n);
            Continuous node2=(Continuous)nodes.elementAt(m);

            NodeList nl=new NodeList();
            nl.insertNode(dbc.getVariables().elementAt(0));
            nl.insertNode(node1);
            nl.insertNode(node2);

            ContinuousCaseListMem caseL=(ContinuousCaseListMem)dbc.getCases();
            DataBaseCases dbc2= new DataBaseCases("hoila",nl,caseL.projection(nl));

            Gaussian_Naive_Bayes gnb=new Gaussian_Naive_Bayes(dbc2,false,0);
            ClassifierValidator cf=new ClassifierValidator(gnb,dbc2,0);
            double accuracy1_2=1-cf.kFoldCrossValidation(10).getError();

            nl=new NodeList();
            nl.insertNode(dbc.getVariables().elementAt(0));
            nl.insertNode(node1);

            caseL=(ContinuousCaseListMem)dbc.getCases();
            dbc2= new DataBaseCases("hoila",nl,caseL.projection(nl));

            gnb=new Gaussian_Naive_Bayes(dbc2,false,0);
            cf=new ClassifierValidator(gnb,dbc2,0);
            double accuracy1=1-cf.kFoldCrossValidation(10).getError();

            nl=new NodeList();
            nl.insertNode(dbc.getVariables().elementAt(0));
            nl.insertNode(node2);

            caseL=(ContinuousCaseListMem)dbc.getCases();
            dbc2= new DataBaseCases("hoila",nl,caseL.projection(nl));

            gnb=new Gaussian_Naive_Bayes(dbc2,false,0);
            cf=new ClassifierValidator(gnb,dbc2,0);
            double accuracy2=1-cf.kFoldCrossValidation(10).getError();


            
            
            int [] ngroup1 = new int[2];
            int [] ngroup2 = new int[2];            
            double[][] datos1=cfm.getData(node1, ngroup1);
            double[][] datos2=cfm.getData(node2, ngroup2);
            
            /*                reg r0=new reg();
            r0.initialize(datos1[0],datos2[0], ngroup[0]);
            reg r1=new reg();
            r1.initialize(datos1[1],datos2[1], ngroup[1]);

            datos[0][cont]=Math.abs(Math.abs(r0.getr())+Math.abs(r1.getr()));
            */
            datos[0][cont]=Stat.corrCoeff(datos1[0],datos2[0],Math.min(ngroup1[0],ngroup2[0]));
            datos[1][cont]=Stat.corrCoeff(datos1[1],datos2[1],Math.min(ngroup1[1],ngroup2[1]));            

            double [] datosA1=cfm.getDataAll(node1);
            double [] datosA2=cfm.getDataAll(node2);
            
            datos[2][cont]=Stat.corrCoeff(datosA1,datosA2,Math.min(datosA1.length,datosA2.length));            
            
            datos[3][cont]=cfm.numLossData(node1)+cfm.numLossData(node2);
            datos[4][cont]=accuracy1_2;
            datos[5][cont]=accuracy1;
            datos[6][cont]=accuracy2;
            datos[7][cont]=cfm.anovaMeasure(node1);
            datos[8][cont]=cfm.anovaMeasure(node2);

            for (int k=0; k<datos.length; k++)
                    salida.write(datos[k][cont]+"\t");
            salida.write("\n");
            
            cont++;
            
        }
        salida.close();
        System.out.println(cont);
        for (int i=0; i<datos.length; i++)
            for (int j=i+1; j<datos.length; j++){
                System.out.println(i+"-"+j+": "+Stat.corrCoeff(datos[i],datos[j]));
            }

    }

    /**
     * 
     * @param file 
     * @throws java.lang.Exception 
     */
    public static void estadisticasFichero(String file) throws Exception{
        FileReader fr = new FileReader(file);    
        BufferedReader entrada = new BufferedReader(fr);
        Vector entradas=new Vector();
        String s;
        while((s = entrada.readLine())!=null){
            if (Double.valueOf(s.split("\t")[7]).doubleValue()>=10.0 && Double.valueOf(s.split("\t")[8]).doubleValue()>=10.0)
                entradas.addElement(s.split("\t"));
        }
        
        int n=((String [])entradas.elementAt(0)).length;
        double[][] datos=new double[n][entradas.size()];
        for (int i=0; i<entradas.size(); i++){
            String[] tmp=(String [])entradas.elementAt(i);
            for (int  j=0; j<tmp.length; j++)
                if (j==0 || j==1 ||j==2)
                    datos[j][i]=Math.abs(Double.valueOf(tmp[j]).doubleValue());
                else
                    datos[j][i]=Double.valueOf(tmp[j]).doubleValue();
        }
        
        System.out.println(entradas.size());
        for (int i=0; i<datos.length; i++)
            for (int j=i+1; j<datos.length; j++){
                System.out.println(i+"-"+j+": "+Stat.corrCoeff(datos[i],datos[j]));
            }
        
        int[] tmp=new int[3];
        tmp[0]=2;
        tmp[1]=7;
        tmp[2]=8;

        
        //Regression R= new Regression(datos[0],datos[2]);
        Regression R= new Regression(joinDatas(datos,tmp),datos[4]);
        R.linear();
        R.print("c:\\tmp\\Regression.txt");
        //R.plotXY();
        //R.plotYY();*/
    }


    private static double [][] joinDatas(double [][] datos,int [] v){
        
        double [][] salida = new double[v.length][datos[0].length];
        
        for (int i=0; i<v.length; i++)
            for (int j=0; j<datos[v[i]].length; j++)
                salida[i][j]=datos[v[i]][j];
        
        return salida;
    }



    /**
     * 
     * @param nodeY 
     * @param nodesX 
     * @return 
     */
    public ContinuousProbabilityTree conditionalGaussianDistribution2(Continuous nodeY, NodeList nodesX){

        ContinuousProbabilityTree tree = new ContinuousProbabilityTree(this.varClass);

        for (int k=0; k<this.numClass; k++){

             MixtExpDensity exp=null;
             
             if (nodesX.size()>0  && Math.abs(this.correlationNodes(nodeY,(Continuous)nodesX.elementAt(0)))>=0.0){
//                System.out.println("E "+k+", "+nodesX.size()+", "+ Math.abs(this.correlationNodes(nodeY,(Continuous)nodesX.elementAt(0),k)));
                Regression R = this.getRegressionClass(nodeY,nodesX,k);
                double[] coef = R.getCoeff();
                Vector vals=new Vector();
                for (int i=0; i<nodesX.size(); i++)
                    vals.add(new Double(coef[i+1]));
                LinearFunction lf = new LinearFunction(nodesX.getNodes(),vals);
                double desv=R.getResidualStandardDeviation();

                exp = new MixtExpDensity(nodeY,coef[0],lf,desv);
             }else{
                int[] ngroup=new int[this.numClass];
                double[][] data=this.getData(nodeY,ngroup);
                double mean=Stat.mean(Stat.adjust(data[k],ngroup[k]));
                double desviation=Stat.standardDeviation(Stat.adjust(data[k],ngroup[k]));
                exp = new MixtExpDensity(nodeY,mean,desviation);
             }
            tree.setChild(new ContinuousProbabilityTree(exp),k);
        }
        return tree;
    }

    /**
     * 
     * @param nodeY 
     * @param nodesX 
     * @param empiricIntFactor 
     * @param intervalsFactor 
     * @param numpoints 
     * @return 
     */
    public ContinuousProbabilityTree conditionalMTEDistribution(Continuous nodeY, NodeList nodesX, double empiricIntFactor, double  intervalsFactor, int numpoints){
        
        ContinuousProbabilityTree tree = new ContinuousProbabilityTree(this.varClass);
        ContinuousProbabilityTree child=null;

        double mean=0.0;
        for (int k=0; k<this.numClass; k++){
            int[] ngroup=new int[this.numClass];
            double[][] data= this.getData(nodeY,ngroup);
            //The max intervals will be a % of the sample   
            int intervals=(int)Math.round(data[k].length*intervalsFactor);
            mean+=intervals;
        }
        mean/=this.numClass;
        mean=4;
        if (nodesX.size()>0){
            MTELearning mteL=new MTELearning(this);
            return mteL.learnConditional(nodeY, nodesX, this, (int)mean, numpoints);
         }else{

         for (int k=0; k<this.numClass; k++){

            int[] ngroup=new int[this.numClass];
            double[][] data= this.getData(nodeY,ngroup);
            //The empiric intervals will be a % of the sample    
            int empiricInt=(int)Math.round(data[k].length*empiricIntFactor);
            //The max intervals will be a % of the sample   
            int intervals=(int)Math.round(data[k].length*intervalsFactor);

            //empiricInt and intervals must be greater than 2
            if (empiricInt < 3) empiricInt=3;
            if (intervals <3) intervals=3;

             if (data[k].length>=3) {

                   //Estimate the MTE
                   boolean reduce;
                   do {
                       reduce=false;
                       /*//DEBUG vemos  parametros			   
                         System.out.println("\nempiricInt="+empiricInt+" intervals="+intervals+" numpoints="+numpoints);*/

                       //intervals=10;
                       Vector tmp=new Vector();
                       for (int b=0; b<data[k].length; b++)
                           tmp.addElement(new Double(data[k][b]));
                       child = ContinuousProbabilityTree.learnUnivariate(nodeY,tmp,intervals,numpoints); 

                       //Check if all teh values of the interval are the same 
                       for (int ii=0;ii < child.getNumberOfChildren() && (!reduce) ;ii++) {
                           Double independent=new Double(child.getChild(ii).getProb().getIndependent());
                           if ( independent.isNaN())  {
                               reduce=true;
                               //decrementing the intervals
                               empiricInt--;
                           }
                       }//end for i

                       if (empiricInt<3) {
                           reduce=false;
                           System.out.println("Warning:Repetition of the same value. Using Gaussian instead MTE");
                           MixtExpDensity f = new MixtExpDensity(nodeY,Stat.mean(data[k]),Stat.standardDeviation(data[k]));
                           child = new ContinuousProbabilityTree(f);
                       }
                   } while (reduce);


               } else {
                   System.out.println("Warning: Few values. Using Gaussian instead MTE");
                   MixtExpDensity f = new MixtExpDensity(nodeY,Stat.mean(data[k]),Stat.standardDeviation(data[k]));
                   child = new ContinuousProbabilityTree(f);
               } 
               tree.setChild(child,k);   
            }
        }
        return tree;
    }



    /**
     * 
     * @param acm 
     * @return 
     */
    public double accuracy(AvancedConfusionMatrix acm){
        
        double sum=0.0;
        int[] numCasesClass=this.getCasesClass();
        for (int i=0; i<this.numClass; i++){
            double[] x=new double[numCasesClass[i]];
            double[] y=new double[numCasesClass[i]];
            int cont=0;
            for (int j=0; j<this.getNumberOfCases(); j++){
                if (((ContinuousCaseListMem)this.getCaseListMem()).get(j).getValue(this.varClass)==i){
                    x[cont]=this.norma(this.getDataClass(j));
                    y[cont]=acm.getProbab(j)[acm.getRealClass(j)];//-Math.log(acm.getProbab(j)[acm.getRealClass(j)]);
                    cont++;
                }
            }
            double[] x1=Fmath.selectionSort(x);
            double[] y1=new double[y.length];
            for (int k=0; k<y.length; k++)
                for (int l=0; l<y.length; l++)
                    if (x[k]==x1[l])
                        y1[l]=y[k];

            double suma=0.0;
            for (int k=0; k<y.length-1; k++)
                    suma+=Math.abs((y1[k+1]-y1[k])/(x1[k+1]-x1[k]));
                
            //CubicSpline cs=new CubicSpline(x1, y1);
            //cs.calcDeriv();
            //sum+=this.norma(cs.getDeriv());
            suma/=y.length;
            sum+=suma;
        }
        sum/=this.numClass;
        return sum;//1000000;
    }
    
    /**
     * 
     * @param a 
     * @return 
     */
    public static double norma(double[] a){
        double cont=0.0;
        for (int i=0; i<a.length; i++)
            cont+=a[i]*a[i];
        
        return Math.sqrt(cont)/(a.length);
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException,FileNotFoundException,IOException {
/*        
    try{
        String S=new String("d:\\andres\\elvira\\esqaru\\dbcs\\");        
        String repositoryPath=new String("d:\\andres\\elvira\\datasets\\Kent Ridge Bio-medical Data Set Repository\\repository\\BreastCancer_Total_Validator.x");        
        ClassifierDBCValidator cdbcv=ClassifierDBCValidator.readSerialization(repositoryPath);
        //ClassifierDBC cdbc= (ClassifierDBC)ClassifierDBC.readDBC(S+"wgaC.x");
        ClassifierDBC cdbc=cdbcv.getClassifierDBC();
        double[][] r=cdbc.distributionTest(3);
        for (int i=0; i<r.length; i++){
            for (int j=0; j<r[i].length; j++)
                System.out.print(r[i][j]+", ");
            System.out.println();
        }
        System.out.println();
    }catch(Exception e){
        e.printStackTrace();
    }*/
        
    }
    
}
