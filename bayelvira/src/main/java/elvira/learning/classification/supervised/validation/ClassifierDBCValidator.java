/*
 * ClassifierDBCEvaluator.java
 *
 * Created on 18 de febrero de 2005, 10:51
 */

package elvira.learning.classification.supervised.validation;

import elvira.*;
import elvira.learning.classification.supervised.discrete.DiscreteClassifier;
import elvira.InvalidEditException;
import elvira.learning.classification.*;
import elvira.learning.classification.supervised.mixed.*;
import elvira.database.*;
import elvira.tools.statistics.plot.*;
import elvira.tools.statistics.analysis.Stat;

import java.util.Vector;
import java.util.Date;
import java.util.Random;
import java.io.*;



/**
 *  This public class allows to manage a complete validation of a ClassifierDBC (a DataBaseCases with a
 *  FiniteStates target node) with several classifiers and to store and recover all these validations.
 *  The implemented validation procedures are:
 *      - Train and Test Validation.
 *      - K-fold cross validation.
 *      - Leave-One-Out validation.
 *  
 *  This an examaple of using this class:
 *
 *  {
 *    ....  
 *    //Load a DataBaseCase where the target variable is the first one.
 *    ClassifierDBC cdbc=new ClassifierDBC(new FileIntpuStream("lungCancer.dbc"),0); 
 *    //It's necessary to give a directory where this class stores all serialized class about the validations.
 *    ClassifierDBCValidator validator=new ClassifierDBCValidator(cdbc,"/home/andrew/lungCancer/","lungCancerValidator");
 *
 *    //We carry out a partition of lungCancer data for a k-fold-cross validation
 *    validator.initializeKfcValidation(10); 
 *
 *    //A Gaussian_Naive_Bayes classifier is validated in lungCancer data under 
 *    //a 10-fold-cross procedure.
 *    validator.validation(ClassifierDBCValidator.GAUSSIAN_NAIVE_BAYES,ClassifierDBCValidator.KFOLDCROSS_VALIDATION,"LungCancer Validation"); 
 *
 *    //A MixedCMutInfTAN classifier is validated in lungCancer data under 
 *    //a 10-fold-cross procedure.
 *    cl=new MixedCMutInfTAN(cdbc,false,0);
 *    validator.validation(1,ClassifierDBCValidator.KFOLDCROSS_VALIDATION,"LungCancer Validation");
 *    
 *    //All results and partitions are serialized for a posteriori recovering.
 *    validator.writeSerialization();  
 *    
 *    //Print the results of the classifier validations in this data base.
 *    validator.print();
 *    ....
 *  }
 *  
 *  In a posterior execution  you can put:
 *  {
 *      ...
 *      //Recover the serialized object.
 *      ClassifierDBCValidator validator=ClassifierDBCValidator.readSerialization("/home/andrew/lungCancer/lungCancerValidator.x");
 *      //You can see the reults of previous validations
 *      validator.print();
 *      //Evaluate a new classifier or other versions of your classifier using the same partitions of the previous execution. 
 *      MixedClassifier cl=new MixedCMutInfTAN(cdbc,true,0);
 *      validator.validation(1,ClassifierDBCValidator.KFOLDCROSS_VALIDATION,"LungCancer Validation");
 *      //Print all results.
 *      validator.print();
 *      validator.writeSerialization();
 *      ...
 *  }
 *  The class implementation uses the ClassifierDBCValidation and
 *  ClassiferValidation class.
 *  @author  andrew
 */
public class ClassifierDBCValidator implements Serializable{
    
    static final long serialVersionUID = 875737769957119327L;

    protected String NAME=new String("c:\\tmp\\copiaTMP.x");    
    
    /**The data base to manage.*/
    public ClassifierDBC cdbc;
   
    /**A ClassifierDBCValidation object.*/
    protected transient ClassifierDBCValidation cdbcValidation=null;
    
    
    /** A string vector with the names of the created ClassifierDBCValidation objects*/
    protected Vector cdbcValidationNames=new Vector();
    
    /** A String with the path of the directory where is stored the serilized objects*/
    protected String classifierDBCValidatorPath=null;
    
    /** The name with the name of the serilized object*/
    protected String classifierDBCValidatorName=null;
    

    /** Static variables for each possible validation procedures*/
    public static int TRAINTEST_VALIDATION=0;
    public static int KFOLDCROSS_VALIDATION=1;
    public static int LEAVEONEOUT_VALIDATION=2;
    public static int RAND_VALIATION=3;
    public static int SPECIFIC_VALIDATION=4;
    
    public static String VALIDATIONS_NAMES[] = {  
        "TrainTestValidation"                   ,
        "K-FoldCross Validation"                ,      
        "LeaveOneOut Validation"                ,
        "Random Validation"                     ,
        "Specific Validation"                   ,
    };
    

    public static int GAUSSIAN_NAIVE_BAYES=0;
    public static int MIXEDCMUTINFTAN=1;
    
    /** Creates a new instance of  */
    public ClassifierDBCValidator(ClassifierDBC data, String path, String name) {
        this.cdbc=data;
        this.classifierDBCValidatorPath=path;
        this.classifierDBCValidatorName=name;
    }
    public void setCDBCValidatorPath(String path) throws Exception{
        this.classifierDBCValidatorPath=path;
        for (int i=0; i<this.getNumTotalValidations(); i++)
            this.getCDBCValidation(i).setCDBCValidationPath(path);
    }
    public String getCompletePath(){
        return new String(this.classifierDBCValidatorPath+this.classifierDBCValidatorName);
    }
    public ClassifierDBC getClassifierDBC(){
        return this.cdbc;
    }

    public int getNumTotalValidations(){
        return this.cdbcValidationNames.size();
    }
    //Si -1, no está y si no devulve el índice
/*
    public int numLoadCDBCValidation(int n){
      for (int i=0; i<this.cdbcValidationNumbers.size(); i++)
          if (((Integer)this.cdbcValidationNumbers.elementAt(i)).intValue()==n)
              return i;
      return -1;
    }
*/
    public ClassifierDBCValidation getCDBCValidation(int n) throws Exception{
  /*      int index=this.numLoadCDBCValidation(n);
        if (index!=-1){
            return (ClassifierDBCValidation)this.cdbcValidation.elementAt(index);
        }
    */    
        //Antes de cargar un CDBCValidation, borramos el que hay cargado
        //Para eso escribimos por si hay cambios
        this.writeSerialization();        
        //this.cdbcValidation.removeAllElements();
        //this.cdbcValidationNumbers.removeAllElements();
        
        this.cdbcValidation=ClassifierDBCValidation.readSerialization(this.getCDBCValidationPath(n));
        //this.cdbcValidation.addElement(cval);
        //this.cdbcValidationNumbers.addElement(new Integer(n));
        return this.cdbcValidation;
    }
 
    public String getCDBCValidationPath(int i){
        return new String(this.classifierDBCValidatorPath+((String)this.cdbcValidationNames.elementAt(i)));        
    }

    public String getCDBCValidationName(int type){
        int num=this.getNumTotalValidations();
        return new String(this.classifierDBCValidatorName+"_"+this.VALIDATIONS_NAMES[type]+"_"+Integer.toString(num)+"_Validation.x");        
    }
    public void addCDBCValidation(ClassifierDBCValidation cval) throws Exception{
        //this.cdbcValidation.addElement(cval);
        //this.cdbcValidationNumbers.addElement(new Integer(num));
        this.writeSerialization();
        this.cdbcValidation=cval;
        this.cdbcValidationNames.addElement(cval.getName());
    }
    public int initializeRandValidation(int numSets, int numVars, double proportion) throws Exception{
      System.out.println("Inizializated Rand Validation.");

      Vector train=new Vector();
      Vector test=new Vector();
      
      Random r= new Random();
      for (int j=0; j<numSets; j++){
        NodeList nl=new NodeList();
        NodeList nl2=this.cdbc.getVariables().copy();

        nl.insertNode(nl2.elementAt(this.cdbc.getIndVarClass()));
        nl2.removeNode(this.cdbc.getIndVarClass());
        for (int i=0; i<numVars; i++){
            int a =r.nextInt(nl2.size()-1);
            nl.insertNode(nl2.elementAt(a));
            nl2.removeNode(a);
        }
        DataBaseCases newdbc=this.cdbc.copy();
        newdbc.projection(nl);

        
        ClassifierValidator cv = new ClassifierValidator(new Mixed_Naive_Bayes(),newdbc,this.cdbc.getIndVarClass());
        cv.split2Cases(proportion);
        Vector subDBCs=cv.getSubSets();

        DataBaseCases db=(DataBaseCases)subDBCs.elementAt(0);
        train.add(new ClassifierDBC(db.getName(),db.getCases(),this.cdbc.getIndVarClass()));
        db=(DataBaseCases)subDBCs.elementAt(1);
        test.add(new ClassifierDBC(db.getName(),db.getCases(),this.cdbc.getIndVarClass()));
      }

      ClassifierDBCValidation cval=new ClassifierDBCValidation(this.classifierDBCValidatorPath,this.getCDBCValidationName(this.RAND_VALIATION),this.cdbc,train,test,new String("RandValidation: "+numSets+", "+numVars),this.RAND_VALIATION);
      this.addCDBCValidation(cval);
      //this.cdbcValidation.addElement(cval);
      
      System.out.println("Inizializated Rand Validation");
      
      return this.cdbcValidationNames.size()-1;
    }
    public int initializeTrainTestValidation(ClassifierDBC cTrain, ClassifierDBC cTest, String comment) throws Exception{
        Vector train=new Vector();
        Vector test=new Vector();

        train.addElement(cTrain);
        test.addElement(cTest);
        
        ClassifierDBCValidation cval=new ClassifierDBCValidation(this.classifierDBCValidatorPath,this.getCDBCValidationName(this.SPECIFIC_VALIDATION),this.cdbc,train,test,comment,this.SPECIFIC_VALIDATION);
        this.addCDBCValidation(cval);
        //this.cdbcValidation.addElement(cval);
      
        return this.cdbcValidationNames.size()-1;
    }    

    public int initializeTrainTestValidation(Vector Train, Vector Test, String comment) throws Exception{
        
        ClassifierDBCValidation cval=new ClassifierDBCValidation(this.classifierDBCValidatorPath,this.getCDBCValidationName(this.SPECIFIC_VALIDATION),this.cdbc,Train,Test,comment,this.SPECIFIC_VALIDATION);
        this.addCDBCValidation(cval);
        //this.cdbcValidation.addElement(cval);
      
        return this.cdbcValidationNames.size()-1;
    }    

    public int initializeTrainTestValidation(int numTT, double proportion) throws Exception{
      System.out.println("Inizializated Train and Test.");

      Vector train=new Vector();
      Vector test=new Vector();
  
      for (int j=0; j<numTT; j++){
        
        System.out.println("Inizializated Train and Test.");
        
        ClassifierValidator cv = new ClassifierValidator(new Mixed_Naive_Bayes(),this.cdbc,this.cdbc.getIndVarClass());
        cv.split2Cases(proportion);
        Vector subDBCs=cv.getSubSets();

        DataBaseCases db=(DataBaseCases)subDBCs.elementAt(0);
        train.addElement(new ClassifierDBC(db.getName(),db.getCases(),this.cdbc.getIndVarClass()));
        db=(DataBaseCases)subDBCs.elementAt(1);
        test.addElement(new ClassifierDBC(db.getName(),db.getCases(),this.cdbc.getIndVarClass()));
      }
      System.out.println("Inizializated Train and Test.");

      ClassifierDBCValidation cval=new ClassifierDBCValidation(this.classifierDBCValidatorPath,this.getCDBCValidationName(this.TRAINTEST_VALIDATION),this.cdbc,train,test,new String("TrainTestValidation: "+numTT+", "+proportion),this.TRAINTEST_VALIDATION);
      this.addCDBCValidation(cval);
      //this.cdbcValidation.addElement(cval);
      
      return this.cdbcValidationNames.size()-1;
    }

    public int initializeKfcValidation(int k) throws Exception{
        System.out.println("Inizializated KFC.");        
        if (this.getCDBCValidationType(this.KFOLDCROSS_VALIDATION)==null){
            Vector train=new Vector();
            Vector test=new Vector();

            ClassifierValidator cv = new ClassifierValidator(new Mixed_Naive_Bayes(),this.cdbc,this.cdbc.getIndVarClass());
            cv.splitCases(k);
            Vector subDBCs=cv.getSubSets();

            for (int i=0; i<subDBCs.size(); i++){
                DataBaseCases db=cv.mergeCases(i);
                train.add(new ClassifierDBC(db.getName(),db.getCases(),this.cdbc.getIndVarClass()));
                db=(DataBaseCases)subDBCs.elementAt(i);
                test.add(new ClassifierDBC(db.getName(),db.getCases(),this.cdbc.getIndVarClass()));
            }
            ClassifierDBCValidation cval=new ClassifierDBCValidation(this.classifierDBCValidatorPath,this.getCDBCValidationName(this.KFOLDCROSS_VALIDATION),this.cdbc,train,test,new String("K-FOLD-VALIDATION: "+k),this.KFOLDCROSS_VALIDATION);
            this.addCDBCValidation(cval);
            //this.cdbcValidation.addElement(cval);

            return this.cdbcValidationNames.size()-1;
        }else
            return -1;
    }

    public int initializeLooValidation() throws Exception{

        if (this.getCDBCValidationType(this.LEAVEONEOUT_VALIDATION)==null){
        
            System.out.println("Inizializated LOO.");  

            int k=this.cdbc.getNumberOfCases();

            Vector train=new Vector();
            Vector test=new Vector();

            ClassifierValidator cv = new ClassifierValidator(new Mixed_Naive_Bayes(),this.cdbc,this.cdbc.getIndVarClass());
            cv.splitCases(k);
            Vector subDBCs=cv.getSubSets();

            for (int i=0; i<subDBCs.size(); i++){
                //System.out.println("Merge: "+i+" de "+subDBCs.size());
                DataBaseCases db=cv.mergeCases(i);
                train.add(new ClassifierDBC(db.getName(),db.getCases(),this.cdbc.getIndVarClass()));
                db=(DataBaseCases)subDBCs.elementAt(i);
                test.add(new ClassifierDBC(db.getName(),db.getCases(),this.cdbc.getIndVarClass()));
            }
            ClassifierDBCValidation cval=new ClassifierDBCValidation(this.classifierDBCValidatorPath,this.getCDBCValidationName(this.LEAVEONEOUT_VALIDATION),this.cdbc,train,test,new String("LOO-VALIDATION: "),this.LEAVEONEOUT_VALIDATION);
            this.addCDBCValidation(cval);
            return this.cdbcValidationNames.size()-1;
        }else
            return -1;
    }

    public ClassifierDBCValidation getCDBCValidationType(int typeValidation) throws Exception{
        for (int i=0; i<this.cdbcValidationNames.size(); i++){
            ClassifierDBCValidation cval=this.getCDBCValidation(i);//(ClassifierDBCValidation)this.cdbcValidation.elementAt(i);
            if (cval.getTypeValidation()==typeValidation)
                return cval;
        }
        return null;
    }
    public Vector validation(MixedClassifier classifier, int typeValidation, String comment) throws Exception{
        ClassifierDBCValidation cval=this.getCDBCValidationType(typeValidation);
        return cval.evaluate(classifier,comment);
    }

    public Vector validation(MixedClassifier classifier, int typeValidation) throws Exception{
        return this.validation(classifier,typeValidation,null);
    }

    public Vector validation(int classifier, int typeValidation) throws Exception{
        return this.validation(this.getClassifier(classifier),typeValidation,null);
    }
    public Vector validation(int classifier, int typeValidation, String comment) throws Exception{
        ClassifierDBCValidation cval=(ClassifierDBCValidation)this.getCDBCValidationType(typeValidation);
        return cval.evaluate(this.getClassifier(classifier),comment);
    }

    public Vector validation(int classifier, String comment) throws Exception{
        for (int i=0; i<this.getNumTotalValidations(); i++){
            ClassifierDBCValidation cval=(ClassifierDBCValidation)this.getCDBCValidation(i);
            cval.evaluate(this.getClassifier(classifier),comment);
        }
        return null;
    }

    public Vector validation(MixedClassifier classifier, String comment) throws Exception{
        for (int i=0; i<this.getNumTotalValidations(); i++){
            ClassifierDBCValidation cval=(ClassifierDBCValidation)this.getCDBCValidation(i);
            cval.evaluate(classifier,comment);
        }
        return null;
    }
    
    public MixedClassifier getClassifier(int classifier) throws Exception{
        
        MixedClassifier cl=null;
        
        if (classifier==0){
          cl=new Gaussian_Naive_Bayes(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==1){
          cl=new Selective_GNB(this.cdbc,true,this.cdbc.getIndVarClass());
        }/*else if (classifier==2){
          cl=new MixedCMutInfTAN(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==3){
          cl=new Selective_GTAN(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==4){
          cl=new MixedClassificationBasedTAN(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==5){
          cl=new Selective_GClassificationTAN(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==6){
          cl=new SelectiveDirect_GNB(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==7){
          cl=new SelectiveDirect_GTAN(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==8){
          cl=new ComposedClassifier(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==9){
          cl=new Selective_GNBPGM(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==10){
          cl=new SelectiveDirect_MixedNBPGM(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==11){
          cl=new CMutTAN2(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==12){
          cl=new Selective_QuicklyMixedNB(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==13){
          cl=new MTE_Naive_Bayes(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==14){
          cl=new Selective_QuicklyMixedNB1(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==15){
          cl=new Selective_QuicklyMixedNB2(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==16){
          cl=new Selective_QuicklyMixedNB3(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==17){
          cl=new Selective_QuicklyMixedNB4(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==18){
          cl=new Selective_QuicklyMixedNB5(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==19){
          cl=new MixedDiscreteClassifier(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==20){
          cl=new Selective_GNBECSQARU05(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==21){
          cl=new Selective_TANECSQARU05(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==22){
          cl=new Selective_GNB_LogV(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==23){
          cl=new Selective_GNB_LogVNew(this.cdbc,true,this.cdbc.getIndVarClass());
        }else if (classifier==24){
          cl=new Selective_GNBECSQARU05_Javi(this.cdbc,true,this.cdbc.getIndVarClass());
        }*/ 
        return cl;
        
    }
    
    
    public static ClassifierDBCValidator readSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{

        System.out.println("deserializing...");                
        FileInputStream fis = new FileInputStream( objectFile );
        ObjectInputStream ois = new ObjectInputStream( fis );
        ClassifierDBCValidator retrieved = (ClassifierDBCValidator) ois.readObject();
        ois.close();
        System.out.println("deserialized...");        
        
        return retrieved;
    }

    public static ClassifierDBCValidator readSerialization(InputStream fis ) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{
        ObjectInputStream ois = new ObjectInputStream( fis );
        ClassifierDBCValidator retrieved = (ClassifierDBCValidator) ois.readObject();
        ois.close();
        return retrieved;
    }
    public void writeSerialization() throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {
        this.writeSerialization(new String(this.getCompletePath()));
    }

    public void writeSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {

        System.out.println("serializing...");        
        if (this.cdbcValidation!=null) this.cdbcValidation.writeSerialization();
        
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
    /* 
    public void printAllValidations(){

        System.out.println("ClassifierDBCValidator Print Mehtod");
        System.out.println("Data Base: "+this.cdbc.getName()+" NºVars: "+this.cdbc.getVariables().size()+" NºCases: "+this.cdbc.getNumberOfCases());
        System.out.println("Nº Validaciones: "+this.cdbcValidation.size());
        for (int i=0; i<this.cdbcValidation.size(); i++){
            System.out.println("Validation: "+i);
            ClassifierDBCValidation val=(ClassifierDBCValidation)this.cdbcValidation.elementAt(i);
            val.print();
            System.out.println();
            System.out.println();
        }
    }
*/
    public void print() throws Exception{

        //System.out.println("ClassifierDBCValidator Print Mehtod");
        System.out.println("Data Base: "+this.cdbc.getName()+" NºVars: "+this.cdbc.getVariables().size()+" NºCases: "+this.cdbc.getNumberOfCases());
        System.out.println("Nº Validaciones: "+this.cdbcValidationNames.size());
        for (int i=0; i<this.cdbcValidationNames.size(); i++){
            System.out.println("Validation: "+i);
            ClassifierDBCValidation val=this.getCDBCValidation(i);//(ClassifierDBCValidation)this.cdbcValidation.elementAt(i);
            val.print();
            System.out.println();
            System.out.println();
        }
    }
    
    /*
    public void removeValidations(){
        this.cdbcValidation=new Vector();
    }
    */
    public void commentValidation(String comment){
        this.cdbcValidation.setComment(comment);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{

       //String S=new String("/home/gte/andrew/elvira/dbcs/");

        //String S=new String("g:\\andres\\elvira\\esqaru\\dbcs\\");
        //String S=new String("g:\\andres\\elvira\\esqaru\\resultados\\conjunto1\\");
       String S=new String("h:\\andres\\elvira\\esqaru\\experimentos\\mtes\\");
       //String S=new String("/windows/andres/elvira/esqaru/dbcs/");
        //ClassifierDBC dbc = new ClassifierDBC(new FileInputStream("c:\\andres\\elvira\\wright\\WGATrain-select.dbc200"),0);
        //ClassifierDBC dbc = new ClassifierDBC(new FileInputStream("g:\\andres\\elvira\\clasify\\DBCBuenos\\subgroup.dbc"),0);
        //String S=new String("c:\\andres\\elvira\\esqaru\\dbcs\\");
        //ClassifierDBC dbc= (ClassifierDBC)ClassifierDBC.readDBC(S+"wgaC.x");
        //DataBaseCases dbc = DataBaseCases.readDBC("c:\\andres\\tmp\\bayelvira2\\wga.x");
        ClassifierDBC dbc = new ClassifierDBC(new FileInputStream(S+"iris.dbc"),4);
        ClassifierDBCValidator mcv=new ClassifierDBCValidator(dbc,"c:\\tmp\\","pruebaValidator");
        //String name= new String("/home/gte/andrew/copia.x");
        
        //String name= new String(S+"validationWGA-wright200.xcopia");
        //String name= new String(S+"validationSubgroup.xcopia");
        //String name= new String("g:\\andres\\elvira\\esqaru\\resultados\\conjunto1\\iris.xcopia");
        //String name= new String(args[0]);
        //ClassifierDBCValidator mcv=ClassifierDBCValidator.readMCV(name);
        System.out.println("----------");

        //mcv.validation(12,0);        
        /*
        mcv.validation(14,0);
        mcv.writeMCV(name);

        mcv.validation(15,0);
        mcv.writeMCV(name);

        mcv.validation(16,0);
        mcv.writeMCV(name);

        mcv.validation(17,0);
        mcv.writeMCV(name);

        mcv.validation(18,0);
        mcv.writeMCV(name);

        mcv.print();
*/
        //dbc = new ClassifierDBC(new FileInputStream("g:\\andres\\elvira\\clasify\\dbcbuenos\\subgroup.dbc"),0);
        //mcv=new ClassifierDBCValidator(dbc);
        //name= new String(S+"validationWGA-wright200.xcopia");
        //name= new String(S+"subgroup.xcopia");
        //mcv=ClassifierDBCValidator.readMCV(name);        
        
        //ClassifierDBCValidator mcv=ClassifierDBCValidator.readMCV(name);
        System.out.println("----------");

        mcv.initializeLooValidation();
        mcv.validation(0,0);        
        /*
        System.out.println("JOTA");
        mcv.validation(14,2);
        mcv.writeMCV(name);

        System.out.println("JOTA");
        mcv.validation(15,2);
        mcv.writeMCV(name);

        System.out.println("JOTA");
        mcv.validation(16,2);
        mcv.writeMCV(name);
         
        System.out.println("JOTA");
        mcv.validation(17,2);
        mcv.writeMCV(name);

        System.out.println("JOTA");
        mcv.validation(18,2);
        mcv.writeMCV(name);
        */

        
        //  mcv.writeMCV(name);
     mcv.print();
        
    }
    
}


