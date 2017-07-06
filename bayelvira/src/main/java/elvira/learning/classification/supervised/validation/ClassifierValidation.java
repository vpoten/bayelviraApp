/*
 * ClassifierValidation.java
 *
 * Created on 19 de febrero de 2005, 16:50
 */

package elvira.learning.classification.supervised.validation;

import elvira.learning.classification.Classifier;
import elvira.database.DataBaseCases;
import elvira.NodeList;
import elvira.learning.classification.supervised.discrete.DiscreteClassifier;
import elvira.tools.statistics.analysis.Stat;
//import elvira.tools.ParameterManager;
import elvira.learning.classification.supervised.mixed.*;

import java.util.Vector;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;

/**
 * This package class is used to store a concrete validation of a given classifier. The
 * information that is stored is:
 *      - The validated classifier (a Classifier object).
 *      - The train and test confusion matrix (two AvancedConfusionMatrix objects).
 *      - The number of the necessary evaluations for this result.
 *      - The execution time in seconds.
 *      - A comment about the validation. 
 * This class is a utility class of ClassifierDBCValidator class.
 * @author  andres
 */
class ClassifierValidation implements Serializable{
    
    static final long serialVersionUID = 8582924014610204095L;
    
    /** The validated classifer*/
    protected Classifier cl;
    
    /** 
     * A vector with two <code>AvancedConfusionMatrix</code> objects:
     * the train and test confusion matrix respectively.
     */
    protected Vector result;    
    
    /**
     * A comment about the validation.
     */
    protected String comment = null;
    
    /**
     * The number of needy evaluations for this result.
     */
    protected int numEvalutions=-1; 
    
    /**
     * The execution time in seconds.
     */
    protected double executionTime=-1.0;
    
    /**
     * Create an empty instance.
     */
    public ClassifierValidation(){
    }
    
    /**
     * 
     * @param cl the validated classifier.
     * @param result A vector with two <code>AvancedConfusionMatrix</code> objects:
     * the train and test confusion matrix respectively.
     * @param numEvaluations The number of needy evaluations for this result. 
     * @param executionTime  The execution time in seconds.
     * @param comment A comment about the validation
     */
    public ClassifierValidation(Classifier cl, Vector result, int numEvaluations, double executionTime, String comment){
        this.cl=cl;
        this.result=result;
        this.numEvalutions=numEvaluations;
        this.executionTime=executionTime;
        this.comment=comment;
    }
    
    public Classifier getClassifier(){
        return cl;
    }
    /**
     * Print the information about the validation.
     */
    public void print(){
        
        double CLASSIFICATION_THRESHOLD=0.5;//ParameterManager.getParameter(this,0);
        
        System.out.println("ClassifierValidation Comment: \""+this.comment+"\".");
        System.out.println("Classifier Class: "+cl.getClass().getName()+".");
        if (cl.getClass().getInterfaces().length>0)
            if (cl.getClass().getInterfaces()[0]==Selective_Classifier.class){
                Selective_MixedNB sc=(Selective_MixedNB)this.cl;
                int tam=0;
                double[] dim=new double[sc.nodeLists.size()];
                for (int i=0; i<sc.nodeLists.size(); i++){
                    dim[i]=((NodeList)sc.nodeLists.elementAt(i)).size();
                    tam+=dim[i];
                    System.out.print(((NodeList)sc.nodeLists.elementAt(i)).size()+", ");
                }
                double[] a=Stat.intervalConfidence(dim, 0.95);
                System.out.println("\n Mean: "+tam/(double)sc.nodeLists.size()+", "+a[0]+", "+a[1]);
                
            }
        
        if (result.size()!=0){
            if (CLASSIFICATION_THRESHOLD==-1){
                System.out.println("Confusion Matrix Train");
                ((AvancedConfusionMatrix)result.elementAt(0)).print();
                System.out.println("Confusion Matrix Test");
                ((AvancedConfusionMatrix)result.elementAt(1)).print();
            }else{
                System.out.println("Confusion Matrix Train");
                ((AvancedConfusionMatrix)result.elementAt(0)).print(CLASSIFICATION_THRESHOLD);
                System.out.println("Confusion Matrix Test");
                ((AvancedConfusionMatrix)result.elementAt(1)).print(CLASSIFICATION_THRESHOLD);
            }
        }
        
        System.out.println("Nº Evaluations: "+this.numEvalutions);
        System.out.println("Execution Time: "+this.executionTime +" seg.");
    }
    /**
     * Read a serilized object.
     * @param objectFile 
     * @throws java.io.FileNotFoundException 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     * @return 
     */
    public static ClassifierValidation readSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{

        FileInputStream fis = new FileInputStream( objectFile );
        ObjectInputStream ois = new ObjectInputStream( fis );
        ClassifierValidation retrieved = (ClassifierValidation) ois.readObject();
        ois.close();

        return retrieved;
    }

    /**
     * Write a serliazed object. 
     * @param objectFile 
     * @throws java.io.FileNotFoundException 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     */
    public void writeSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {

        ((DiscreteClassifier)this.cl).setDataBaseCases(new DataBaseCases());
        /*
        if (this.cl.getClass()==Selective_GNBECSQARU05.class){
            ((Selective_GNBECSQARU05)this.cl).actualQCMs=new Vector();
            ((Selective_GNBECSQARU05)this.cl).qCMs=new Vector();            
        }
        */
        
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
    }

}
