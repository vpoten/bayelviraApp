/*
 * ClassifierDBCValidation.java
 *
 * Created on 19 de febrero de 2005, 15:56
 */

package elvira.learning.classification.supervised.validation;

import elvira.learning.classification.supervised.validation.ClassifierDBC;
import elvira.database.DataBaseCases;
import elvira.learning.classification.supervised.mixed.*;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.ConfusionMatrix;

import java.util.Vector;
import java.util.Date;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This package class is used to store a partition set of a ClassifierDBC object for 
 * validating different classifiers. The partition set is composed by several pairs
 * of train and test ClassifierDBC objects. In addition, each validation of a classifier 
 * over these partitions is stored as a ClassifierValidation object. 
 *
 * This class is a utility class of the ClassifierDBCValidator class.
 *
 * @author  andrew
 */
class ClassifierDBCValidation implements Serializable{
    
    static final long serialVersionUID = -4271528945133073380L;
    
    
    /** The ClassifierDBC object that is partitioned*/
    protected ClassifierDBC cdbc;
    
    /** Transient variable where is stored a train data base case*/
    public transient ClassifierDBC train=null;//new Vector();
    
    /** Transient variable where is stored a test data base case*/
    public transient ClassifierDBC test=null;//new Vector();
    
    /**An index indicating the pair that is stored in the train and test fields. */
    protected transient int numCDBCLoad=-1;
    
    /**A string vector with the names of all pair of train and test data base*/
    protected Vector cdbcNames=new Vector();
    
    /**A ClassifierValidation object vector with the several validatons carried out.*/
    protected Vector classifierValidationNames=new Vector();
    
    /** A string with a comment about the partition stored in this object*/
    protected String comment;
    
    protected int typeValidation=-1;
    
    /** String with the path where all dates are stored*/
    protected String cDBCValidationPath=null;
    
    /** String with the name of the serilized ClassifierDBCValidtion object */
    protected String cDBCValidationName=null;
    

    protected transient Vector matrixTrain;
    protected transient Vector matrixTest;

    /** Creates a new instance of ClassifierDBCValidation*/
    public ClassifierDBCValidation(String path, String name, ClassifierDBC cdbc, Vector trains, Vector tests, String comment, int type) throws Exception{
        this.cdbc=cdbc;
        this.comment=comment;
        this.typeValidation=type;
        this.cDBCValidationPath=path;
        this.cDBCValidationName=name;

        for (int i=0; i<trains.size(); i++){
          this.addTrainTest((ClassifierDBC)trains.elementAt(i),(ClassifierDBC)tests.elementAt(i));
        }
    }
    public void setCDBCValidationPath(String path){
        this.cDBCValidationPath=path;
    }
    public void addTrainTest(ClassifierDBC train, ClassifierDBC test) throws Exception{
        int num=this.cdbcNames.size();
        String name=this.getCDBCName(num);
        this.cdbcNames.addElement(name);
        train.writeSerialization(this.cDBCValidationPath+name+"_Train.x");
        test.writeSerialization(this.cDBCValidationPath+name+"_Test.x");        
    }
    
    public String getTrainCDBCName(int num) {
        return new String(this.cDBCValidationPath+(String)this.cdbcNames.elementAt(num)+"_Train.x");
    }
    
    public String getTestCDBCName(int num) {
        return new String(this.cDBCValidationPath+(String)this.cdbcNames.elementAt(num)+"_Test.x");
    }

    public ClassifierDBC getTrainCDBC(int num) throws Exception{
        if (num==this.numCDBCLoad)
            return this.train;
        else{
            this.writeSerialization();
            this.train=(ClassifierDBC)ClassifierDBC.readSerialization(new String(this.cDBCValidationPath+(String)this.cdbcNames.elementAt(num)+"_Train.x"));
            this.test=(ClassifierDBC)ClassifierDBC.readSerialization(new String(this.cDBCValidationPath+(String)this.cdbcNames.elementAt(num)+"_Test.x"));
            this.numCDBCLoad=num;
            return this.train;
        }
    }
    public ClassifierDBC getTestCDBC(int num) throws Exception{
        if (num==this.numCDBCLoad)
            return this.test;
        else{
            this.writeSerialization();
            this.train=(ClassifierDBC)ClassifierDBC.readSerialization(new String(this.cDBCValidationPath+(String)this.cdbcNames.elementAt(num)+"_Train.x"));
            this.test=(ClassifierDBC)ClassifierDBC.readSerialization(new String(this.cDBCValidationPath+(String)this.cdbcNames.elementAt(num)+"_Test.x"));
            this.numCDBCLoad=num;
            return this.test;
        }
    }

    public void resetNumCDBCLoad(){
        this.numCDBCLoad=-1;
    }
    public String getCDBCName(int num){
        return new String(cDBCValidationName+"_CDBC_"+Integer.toString(num));
    }
    public String getClassifierValidationName(int num){
        return new String(cDBCValidationName+"_Validation_"+Integer.toString(num)+".x");
    }
    public String getName(){
        return this.cDBCValidationName;
    }
    public String getCompletePath(){
        return new String(this.cDBCValidationPath+this.cDBCValidationName);
    }
    
    public void setTypeValidation(int typeValidation){
        this.typeValidation=typeValidation;
    }
    public int getTypeValidation(){
        return this.typeValidation;
    }
    public int getNumTT(){
        return this.cdbcNames.size();
    }
    public void setComment(String comment){
        this.comment=comment;
    }
    public Vector evaluate(MixedClassifier cl,String Comment) throws Exception{

        Date date = new Date();
        double time = (double) date.getTime();           

        
        this.matrixTrain=new Vector();
        this.matrixTest=new Vector();
        int cont=0;
        
        for (int j=0; j<this.getNumTT(); j++){
            System.out.println("Eval: "+j);
            Vector subDBCs=new Vector();
            
            subDBCs.add(this.getTrainCDBC(j));
            subDBCs.add(this.getTestCDBC(j));
/*
            if (cl.getClass()==Selective_QuicklyMixedNB.class){
                ((Selective_QuicklyMixedNB)cl).test=((DataBaseCases)subDBCs.elementAt(1)).copy();
            }
*/            
            ClassifierValidator cv = new ClassifierValidator(cl,this.cdbc,subDBCs,this.cdbc.getIndVarClass(),ClassifierValidator.TRAINANDTEST);
            Vector confusionMatrixs=cv.trainAndTest();
            ConfusionMatrix cmtrain =(ConfusionMatrix)confusionMatrixs.elementAt(0);
            ConfusionMatrix cmtest =(ConfusionMatrix)confusionMatrixs.elementAt(1);
            this.matrixTrain.add(cmtrain);
            this.matrixTest.add(cmtest);
            System.out.println("Matrix Train: "+j);
            cmtrain.print();
            System.out.println("Matrix Test: "+j);
            cmtest.print();
        }
        
        date=new Date();
        time = (((double) date.getTime()) - time)/1000;

        AvancedConfusionMatrix cm=new AvancedConfusionMatrix(this.cdbc.getVarClass().getNumStates());
        cm.average(this.matrixTrain);
        
        Vector result=new Vector();
        result.addElement(cm);
        cm=new AvancedConfusionMatrix(this.cdbc.getVarClass().getNumStates());
        cm.average(this.matrixTest);
        result.addElement(cm);

        ClassifierValidation cvalidation=new ClassifierValidation(cl,result,cl.getEvaluations(),time,Comment);
        
        String cvalidationname=this.getClassifierValidationName(this.classifierValidationNames.size());
        this.classifierValidationNames.addElement(cvalidationname);
        cvalidation.writeSerialization(new String(this.cDBCValidationPath+cvalidationname));
        this.writeSerialization();
        //this.classifierValidations.addElement(cvalidation);
        
        cvalidation.print();
        return result;
    }
    
    public void print() throws Exception{
        System.out.println("ClassifierDBCValidation Comment: \""+this.comment+"\".");
        System.out.println("Type: "+ClassifierDBCValidator.VALIDATIONS_NAMES[this.typeValidation]);
        for (int i=0; i<this.classifierValidationNames.size(); i++){
            System.out.println("Classifier Validation: "+i);
            //ClassifierValidation cval=(ClassifierValidation)this.classifierValidations.elementAt(i);
            ClassifierValidation cval=ClassifierValidation.readSerialization(new String(this.cDBCValidationPath+((String)this.classifierValidationNames.elementAt(i))));
            cval.print();
            System.out.println();
        }
        
        
    }

    public int getNumberClassifierValidations(){
        return this.classifierValidationNames.size();
    }
    
    public ClassifierValidation getClassifierValidation(int k) throws Exception {
        return ClassifierValidation.readSerialization(new String(this.cDBCValidationPath+((String)this.classifierValidationNames.elementAt(k))));
    }

    public static ClassifierDBCValidation readSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{

        System.out.println("deserializing...");                
        
        FileInputStream fis = new FileInputStream( objectFile );
        ObjectInputStream ois = new ObjectInputStream( fis );
        ClassifierDBCValidation retrieved = (ClassifierDBCValidation) ois.readObject();
        ois.close();

        retrieved.resetNumCDBCLoad();
        
        System.out.println("deserialized...");                
        return retrieved;

    }
    public void writeSerialization() throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {
        this.writeSerialization(this.getCompletePath());
    }

    public void writeSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {
        System.out.println("serializing...");
        
        if (this.numCDBCLoad!=-1){
            this.train.writeSerialization(new String(this.cDBCValidationPath+(String)this.cdbcNames.elementAt(this.numCDBCLoad)+"_Train.x"));
            this.test.writeSerialization(new String(this.cDBCValidationPath+(String)this.cdbcNames.elementAt(this.numCDBCLoad)+"_Test.x"));
        }
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
    
}

