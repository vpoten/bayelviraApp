/*
 * ClassifierDBCRepositoryValidator.java
 *
 * Created on 4 de mayo de 2005, 11:02
 */

package elvira.learning.classification.supervised.validation;


//import privateclass.proccesingKentRidgeRepositoryDATAtoDBC;
import elvira.tools.statistics.math.Fmath;
import elvira.learning.classification.supervised.validation.ClassifierDBC;
import elvira.database.DataBaseCases;
import elvira.learning.classification.supervised.mixed.*;
import elvira.learning.classification.*;
import elvira.Continuous;
import elvira.NodeList;
import elvira.tools.statistics.analysis.Stat;

import java.util.Vector;
import java.util.Date;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.InputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.net.URL;

/**
 * This class is used to validate a classfier set with a data base repository. This class
 * allows to manage several validations procedures, as a specific validation in a train 
 * and  test set or as k-fold-cross procedure too. In addition, all the validation results are 
 * saved. This class uses a ClassifierDBCValidator object for each data base of the classifier.
 * 
 * From a given ClassifierDBCRepository object, this is an example of use of this class:
 *
 * {
 * ...
 * ClassifierDBCRepository repos=new ClassifierDBCRepository(.....);
 * ...
 * 
 * //you have to create a specific directory where all validations of the repository are saved as serialized object.
 * ClassifierDBCRepositoryValidator reposValidator=new ClassifierDBCRepositoryValidator(repos,"DNAValidator","/home/andrew/DNARepository/validations/");
 * 
 * //Initialize a k-fold-cross partition for all dbcs of the repository
 * for (int i=0; i<reposValidator.getNumberOfCDBC(); i++)
 *      reposValidator.getCDBCValidator(i).initializeKFC(10);
 * 
 * //Initialize another partition for a validation of the data base number one and three using 
 * //ten train and test sets, where the train set contains the seventy five per cent 
 * //of the cases of the data base and the test set, the rest of them.
 * reposValidator.getCDBCValidtor(1).initializeTrainTestValidation(10,0.75);
 * reposValidator.getCDBCValidtor(3).initializeTrainTestValidation(10,0.75);
 * 
 * //With this method, you validate a Gaussian Naive Bayes classifier for each data base
 * //of the repository and using the validation procedure that you have initialized previously.
 * reposValidator.allValidation(ClassifierDBCValidator.GAUSSIAN_NAIVE_BAYES,"Gaussian Naive Bayes classifier")
 * 
 * //Saved the results.
 * reposValidator.writeSerialization();
 * 
 * //Print all validations results
 * reposValidator.printValidationResults();
 * 
 *
 * ...
 * }
 * The initializations of the validation procedure have to be done only once time. So for 
 * a posterior validation of other classifier, you only have to put: 
 *
 * {
 * //you read the previous serialized ClassifierDBCRepositoryValidator object.
 * ClassifierDBCRepositoryValidator reposValidator=ClassifierDBCRepositoryValidator.readSerialization("/home/andrew/DNARepository/validations/","DNAValidator");
 * 
 * //With this method, you validate a TAN classifier for each data base
 * //of the repository and using the validation procedure that you had initialized in the previous execution.
 * reposValidator.allValidation(ClassifierDBCValidator.MIXEDCMUTINFTAN,"TAN classifier")
 * 
 * //Saved the results.
 * reposValidator.writeSerialization();
 *
 * //Print all validations results
 * reposValidator.printValidationResults();
 * ...
 * }
 * @author andrew
 */
public class ClassifierDBCRepositoryValidator implements Serializable{
    
    static final long serialVersionUID = 4287173299926772153L;
    
    ClassifierDBCRepository cdbcRepository;
    
    protected String cdbcRepositoryValidatorName=null;
    protected String cdbcRepositoryValidatorPath=null;
    protected transient ClassifierDBCValidator classifierDBCValidator=null;
    protected Vector classifierDBCValidatorsNames=new Vector();
    
    /** Creates a new instance of ClassifierDBCRepositoryValidator */
    public ClassifierDBCRepositoryValidator(ClassifierDBCRepository cdbcRepository,String name, String path) throws Exception{
        this.cdbcRepository=cdbcRepository;
        this.cdbcRepositoryValidatorName=name;
        this.cdbcRepositoryValidatorPath=path;

        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            ClassifierDBC cdbc=this.cdbcRepository.getClassifierDBC(i);
            ClassifierDBCValidator cval=new ClassifierDBCValidator(cdbc,this.cdbcRepositoryValidatorPath,this.getCDBCValidatorName(cdbc));
            cval.writeSerialization();
            this.classifierDBCValidator=cval;
            this.classifierDBCValidatorsNames.addElement(this.getCDBCValidatorName(cdbc));
        }

    }
    
    /** Creates a new instance of ClassifierDBCRepositoryValidator */
    public ClassifierDBCRepositoryValidator(String cdbcRepositoryPath, String cdbcRepositoryName, String name, String path) {
        try{
            this.cdbcRepository=ClassifierDBCRepository.readSerialization(cdbcRepositoryPath,cdbcRepositoryName,0);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        this.cdbcRepositoryValidatorName=name;
        this.cdbcRepositoryValidatorPath=path;
    }
    
    public String getCDBCValidatorPath(int i){
        return new String(this.cdbcRepositoryValidatorPath+((String)this.classifierDBCValidatorsNames.elementAt(i)));        
    }

    
    public ClassifierDBCValidator getCDBCValidator(int n) throws Exception{

        this.writeSerialization();        
        this.classifierDBCValidator=ClassifierDBCValidator.readSerialization(this.getCDBCValidatorPath(n));
        return this.classifierDBCValidator;
    }
    

    public void addTrainTestValidation(int numDBC, ClassifierDBC train, ClassifierDBC test, String comment) throws Exception{
        ((ClassifierDBCValidator)this.getCDBCValidator(numDBC)).initializeTrainTestValidation(train,test,comment);
    }
    
    public void addTrainTestValidation(int numDBC, Vector train, Vector test, String comment) throws Exception{
        ((ClassifierDBCValidator)this.getCDBCValidator(numDBC)).initializeTrainTestValidation(train,test,comment);
    }
    
    public Vector validation(int numDBC, int classifier, int typeValidation, String comment) throws Exception{
        ClassifierDBCValidator cval=this.getCDBCValidator(numDBC);
        return cval.validation(classifier,typeValidation,comment);
    }

    public Vector validation(int numDBC, MixedClassifier classifier, int typeValidation, String comment) throws Exception{
        ClassifierDBCValidator cval=this.getCDBCValidator(numDBC);
        return cval.validation(classifier,typeValidation,comment);
    }

    public Vector validation(int numDBC, MixedClassifier classifier, int typeValidation) throws Exception{
        ClassifierDBCValidator cval=this.getCDBCValidator(numDBC);
        return cval.validation(classifier,typeValidation,null);
    }

    public Vector validation(int numDBC, int classifier, int typeValidation) throws Exception{
        ClassifierDBCValidator cval=this.getCDBCValidator(numDBC);
        return cval.validation(classifier,typeValidation,null);
    }

    public Vector validation(int numDBC, int classifier) throws Exception{
        ClassifierDBCValidator cval=this.getCDBCValidator(numDBC);
        return cval.validation(classifier,null);
    }
    
    public Vector validation(int numDBC, int classifier, String comment) throws Exception{
        ClassifierDBCValidator cval=this.getCDBCValidator(numDBC);
        return cval.validation(classifier,comment);
    }

    public Vector validation(int numDBC, MixedClassifier classifier, String comment) throws Exception{
        ClassifierDBCValidator cval=this.getCDBCValidator(numDBC);
        return cval.validation(classifier,comment);
    }

    public Vector allValidations(int dbcini, int classifier, String comment) throws Exception{
        for (int i=dbcini; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            System.out.println("allValidations Partial: "+i);
            this.validation(i,classifier,comment);
        }
        return null;
    }

    public Vector allValidations(int classifier, String comment) throws Exception{
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            System.out.println("allValidations Partial: "+i);
            this.validation(i,classifier,comment);
        }
        return null;
    }

    public Vector allValidations(MixedClassifier classifier, String comment) throws Exception{
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            System.out.println("allValidations Partial: "+i); 
            this.validation(i,classifier,comment);
        }
        return null;
    }
    public int getNumberOfCDBC(){
        return this.cdbcRepository.getNumberOfCDBC();
    }
    public void printValidationResults() throws Exception{
        System.out.println("Validation Resulst for respository:");
        this.cdbcRepository.printRepositoryInformation();
        System.out.println("Validations:");
        for (int i=0; i<this.getNumberOfCDBC(); i++){
            System.out.println("DataBase: "+i);
            this.printClassifierDBCValidator(i);
        }
    }
    public void printClassifierDBCValidator(int numDBC) throws Exception{
        ClassifierDBCValidator cval=this.getCDBCValidator(numDBC);
        cval.print();
    }    

    public String getCDBCValidatorName(ClassifierDBC data){
        return new String(new String(data.getName()+"_Validator.x"));        
    }

    
    
    public void addClassifierDBC(ClassifierDBC cdbc) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{
        this.cdbcRepository.addClassifierDBC(cdbc);

        this.writeSerialization();
        
        ClassifierDBCValidator cval=new ClassifierDBCValidator(cdbc,this.cdbcRepositoryValidatorPath,this.getCDBCValidatorName(cdbc));
        this.classifierDBCValidator=cval;
        this.classifierDBCValidatorsNames.addElement(this.getCDBCValidatorName(cdbc));

        this.writeSerialization();
        
    }

    public void saveAllDBCsResults(String path) throws Exception{

        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            ClassifierDBCValidator cdbcvalidator=this.getCDBCValidator(i);
            for (int j=0; j<cdbcvalidator.getNumTotalValidations(); j++){
                ClassifierDBCValidation cdbcvalidation=cdbcvalidator.getCDBCValidation(j);
                for (int k=0; k<cdbcvalidation.getNumTT(); k++){
                    ClassifierDBC train=cdbcvalidation.getTrainCDBC(k);
                    ClassifierDBC test=cdbcvalidation.getTestCDBC(k);
                    for (int l=0; l<cdbcvalidation.getNumberClassifierValidations(); l++){
                        ClassifierValidation cvalidation=cdbcvalidation.getClassifierValidation(l);
                        Classifier cl=cvalidation.getClassifier();
                        if (cl.getClass().getInterfaces().length>0)
                            if (cl.getClass().getInterfaces()[0]==Selective_Classifier.class){
                                Selective_MixedNB sc=(Selective_MixedNB)cl;
                                for (int m=k; m<k+1; m++){
                                    NodeList nodes=((NodeList)sc.nodeLists.elementAt(m));
                                    NodeList nodes2=new NodeList();
                                    nodes2.insertNode(train.getVarClass());
                                    for (int n=0; n<nodes.size(); n++){
                                        nodes2.insertNode(nodes.elementAt(n));
                                    }
                                    
                                    DataBaseCases train2=train.copy();
                                    train2.projection(nodes2);
                                    //train2.saveDataBase(new FileWriter(path+cdbcvalidation.getName()+"__Validation_"+k+"__Classifier_"+cl.getClass().getName()+"_"+l+"__Train_"+m+".dbc"));
                                    train2.saveDataBase(new FileWriter(path+cdbcvalidation.getName()+"__Validation_"+k+"__"+l+"__Train.dbc"));

                                    DataBaseCases test2=test.copy();
                                    test2.projection(nodes2);
                                    test2.saveDataBase(new FileWriter(path+cdbcvalidation.getName()+"__Validation_"+k+"__"+l+"__Test.dbc"));
                                }
                            }
                    }
                }
            }
        }
    }

    public void saveAllDBCs(String path) throws Exception{
        Vector dbcs=this.getAllCDBCs();
        for (int i=0; i<dbcs.size(); i++){
            Vector v=(Vector)dbcs.elementAt(i);
            for (int j=0; j<v.size(); j++){
                DataBaseCases dbc=(DataBaseCases)v.elementAt(j);
                dbc.saveDataBase(new FileWriter(new String(path+dbc.getName())));
                System.out.println(new String(path+dbc.getName()));
            }
        }
    }
    
    public Vector getAllCDBCs() throws Exception{
        Vector dbcs=new Vector();
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            Vector partialdbcs=new Vector();
            ClassifierDBCValidator cdbcvalidator=this.getCDBCValidator(i);
            partialdbcs.addElement(cdbcvalidator.getClassifierDBC());
            for (int j=0; j<cdbcvalidator.getNumTotalValidations(); j++){
                ClassifierDBCValidation cdbcvalidation=cdbcvalidator.getCDBCValidation(j);
                for (int k=0; k<cdbcvalidation.getNumTT(); k++){
                    partialdbcs.addElement(cdbcvalidation.getTrainCDBC(k));
                    partialdbcs.addElement(cdbcvalidation.getTestCDBC(k));
                }
            }
            dbcs.addElement(partialdbcs);
        }
        return dbcs;
    }

/*    
    public void writeClassifierDBCSerialization() throws Exception{
        for (int i=0; i<this.cDBCNames.size(); i++){
            ClassifierDBCValidator cval=(ClassifierDBCValidator)this.getCDBCValidator(i);
            cval.getClassifierDBC().writeSerialization(this.getCDBCPath(i));
        }
    }
*/    
    public void IR_getClassifierBasedOrder() throws Exception{
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            this.IR_getClassifierBasedOrder(i);
        }
    }

    public void IR_getClassifierBasedOrder(int n) throws Exception{
            ClassifierDBCValidator cdbcvalidator=this.getCDBCValidator(n);
            DataBaseCases dbc=cdbcvalidator.getClassifierDBC();
            System.out.println("IR_getClassifierBasedOrder Partial: "+n);
            for (int j=0; j<cdbcvalidator.getNumTotalValidations(); j++){
                ClassifierDBCValidation cdbcvalidation=cdbcvalidator.getCDBCValidation(j);
                for (int k=0; k<cdbcvalidation.getNumTT(); k++){
                    System.out.println("Partial2: "+k+" de "+cdbcvalidation.getNumTT()+" en "+n+" dbc");                    
                    ClassifierDBC train=cdbcvalidation.getTrainCDBC(k);
                    //ClassifierDBC test=cdbcvalidation.getTestCDBC(k);
                    train.getGNBBasedOrder();
                    //train.getSortAnovaNodes();
                    //train.anovaFilter(0.9);
                }
            }
    }
    public void IR_initializeKFC() throws Exception{
    
        
        for (int k=0; k<this.cdbcRepository.getNumberOfCDBC(); k++){
            System.out.println("IR_initializeKFC Partial: "+k);
            ClassifierDBCValidator val=this.getCDBCValidator(k);
            val.initializeKfcValidation(10);
            //val.initializeLooValidation();
        }
    }

    public static void generateKentRidgeRepository(String pathBase) throws Exception{
        //String pathBase="/home/gte/andrew/repository/";
        String repositoryValidatorName=new String("KentRidgeRepositoryValidator.x");
        String repositoryValidatorPath=new String(pathBase+"repository/");
        //String repositoryValidatorPath=new String("D:/andres/elvira/datasets/Kent Ridge Bio-medical Data Set Repository/KentRidgeRepository/repository/");
        
        String repositoryName=new String("KentRidgeRepository.x");
        String repositoryPath=new String(pathBase+"primarydata/");
        //String repositoryPath=new String("D:/andres/elvira/datasets/Kent Ridge Bio-medical Data Set Repository/KentRidgeRepository/primarydata/");
        
        //ClassifierDBCRepository repos=ClassifierDBCRepository.readSerialization(repositoryPath, repositoryName, 0);
        //repos.printRepositoryInformation();
        //ClassifierDBCRepositoryValidator reposValidator = new ClassifierDBCRepositoryValidator(repos, repositoryValidatorName,repositoryValidatorPath);
        
        /*
        String[] args=new String[1];
        args[0]=cDBCPath;
        proccesingKentRidgeRepositoryDATAtoDBC.main(args);
         */
        
        ClassifierDBCRepositoryValidator reposValidator = ClassifierDBCRepositoryValidator.readSerialization(repositoryValidatorPath,repositoryValidatorName,0);
        
        
        //ClassifierDBCRepositoryValidator reposValidator=ClassifierDBCRepositoryValidator.loadKentRidgeRepositoryDisk(repositoryValidatorName,repositoryValidatorPath,repositoryName,repositoryPath);
        
        //reposValidator.writeSerialization();
        
        /*
        System.out.println("ABA:");
        reposValidator.logTransformation();        
        reposValidator.writeSerialization();
        
        System.out.println("ABA:");
        reposValidator.normalizeRepository();        
        reposValidator.writeSerialization();
        */
        /*
        System.out.println("ABA:");
        reposValidator.discretization();
        reposValidator.writeSerialization();        
        */
        /*
        System.out.println("ABA:");
        reposValidator.setMinMaxDBCs();
        reposValidator.writeSerialization();        
        */
        /*
        System.out.println("ABA:");
        reposValidator.IR_initializeKFC();
        reposValidator.writeSerialization();        
       
    
        System.out.println("ABA:");
        reposValidator.IR_getClassifierBasedOrder();
        reposValidator.writeSerialization();        
        */
        
        System.out.println("ABA:");
        reposValidator.allValidations(10,24,"Parameters01 todo con GNB");
        reposValidator.writeSerialization();
        
        reposValidator.printValidationResults();
        //reposValidator.saveAllDBCs("c:/temp/");
        reposValidator.saveAllDBCsResults(pathBase+"results/");
        
        /*
        System.out.println("ABA:");
        repos.validation(14,21,ClassifierDBCValidator.SPECIFIC_VALIDATION,"Validation con TAN");        
        repos.validation(14,21,"Validation con TAN");        
        repos.validation(15,21,"Validation con TAN");        
        repos.writeSerialization();        
        repos.printClassifierDBCValidator(0);    
        */
        
/*      int N=14;
        repos.getCDBCValidator(N).initializeLooValidation();
        repos.IR_getClassifierBasedOrder(N);
        repos.validation(N,20,ClassifierDBCValidator.LEAVEONEOUT_VALIDATION);
        repos.printClassifierDBCValidator(N);    
 */
    }

    public static void generateECSQARURepository() throws Exception{
        String repositoryValidatorName=new String("ECSQARURepositoryValidator.x");
        //String repositoryValidatorPath=new String("/windows/andres/elvira/esqaru/repositoryECSQARU/");
        String repositoryValidatorPath=new String("c:\\andres\\elvira\\esqaru\\repositoryECSQARU\\");
        
        String repositoryName=new String("ECSQARURepository.x");
        String repositoryPath=new String("c:\\andres\\elvira\\esqaru\\dbcs\\dbcs200\\");
        //String repositoryPath=new String("/windows/andres/elvira/esqaru/dbcs/dbcs200/");
        
        //ClassifierDBCRepository repos=ClassifierDBCRepository.readSerialization(repositoryPath, repositoryName, 0);
        //ClassifierDBCRepositoryValidator reposValidator= new ClassifierDBCRepositoryValidator(repos, repositoryName, repositoryPath);
        /*
        ClassifierDBCRepository repos= new ClassifierDBCRepository("IrisRepository", "d:\\andres\\elvira\\esqaru\\iris\\dbc\\");
        repos.addClassifierDBC(new ClassifierDBC(new FileInputStream("d:\\andres\\elvira\\esqaru\\iris\\dbc\\iris.dbc"),4));
        ClassifierDBCRepositoryValidator reposValidator= new ClassifierDBCRepositoryValidator(repos, "IrisRepositoryValidator", "d:\\andres\\elvira\\esqaru\\iris\\repos\\");
        reposValidator.addClassifierDBC(new ClassifierDBC(new FileInputStream("d:\\andres\\elvira\\esqaru\\iris\\dbc\\wga_20.dbc"),0));
        */
        ClassifierDBCRepositoryValidator reposValidator=ClassifierDBCRepositoryValidator.readSerialization("d:\\andres\\elvira\\esqaru\\iris\\repos\\","IrisRepositoryValidator", 0);

        //ClassifierDBCRepositoryValidator reposValidator=ClassifierDBCRepositoryValidator.loadECSQARURepositoryDisk(repositoryValidatorName, repositoryValidatorPath, repositoryName, repositoryPath);
        //reposValidator.writeSerialization();
        
    /*    
        System.out.println("ABA:");
        reposValidator.normalizeRepository();        
        reposValidator.writeSerialization();
*/
        /*
        System.out.println("ABA:");
        reposValidator.discretization();
        reposValidator.writeSerialization();        
*/
  /*              
        System.out.println("ABA:");
        reposValidator.setMinMaxDBCs();
        reposValidator.writeSerialization();        
        
        
        System.out.println("ABA:");
        reposValidator.IR_initializeKFC();
        reposValidator.writeSerialization();        
        
        
        System.out.println("ABA:");
        reposValidator.IR_getClassifierBasedOrder();
        reposValidator.writeSerialization();        
*/
/*        System.out.println("ABA:");
        //reposValidator.validation(0,20,ClassifierDBCValidator.SPECIFIC_VALIDATION);
        reposValidator.allValidations(20,null);
        reposValidator.writeSerialization();        
  */
        reposValidator.printValidationResults();
       
/*
        System.out.println("ABA:");
        reposValidator.validation(0,20,ClassifierDBCValidator.SPECIFIC_VALIDATION);        
        reposValidator.writeSerialization();        
        
        reposValidator.printClassifierDBCValidator(0);                        
 */
        //reposValidator.validation(0,20,ClassifierDBCValidator.SPECIFIC_VALIDATION);        
        //reposValidator.printCompleteRepositoryInformation();
    }
    
    public static void generateAlbertoRepository() throws Exception{    
        String repositoryValidatorName=new String("AlbertoRepositoryValidator.x");
        String repositoryValidatorPath=new String("e:\\andres\\alberto\\repository\\");
        
        String repositoryName=new String("AlbertoRepository.x");
        String repositoryPath=new String("e:\\andres\\alberto\\");
/*        
        ClassifierDBCRepository repos=new ClassifierDBCRepository(repositoryName,cDBCRootPath, repositoryPath);
        
        String cdbcPath=new String("e:\\andres\\alberto\\facetotal.dbc");
        ClassifierDBC cdbc=new ClassifierDBC(new FileInputStream(cdbcPath),0);
        
        repos.addClassifierDBC(cdbc);
        repos.writeSerialization();
 */       
        ClassifierDBCRepository repos=ClassifierDBCRepository.readSerialization(repositoryPath, repositoryName,0);
        ClassifierDBCRepositoryValidator reposValidator = new ClassifierDBCRepositoryValidator(repos, repositoryName, repositoryPath); 
        
        reposValidator.IR_initializeKFC();
        reposValidator.IR_getClassifierBasedOrder();
         

        reposValidator.allValidations(0,null);
        reposValidator.allValidations(1,null);
        reposValidator.allValidations(20,null);
        //reposValidator.printCompleteRepositoryInformation();
        //repos.writeSerialization();
    }

    public static void generateLogVRepository(String rValidatorPath, String rPath) throws Exception{    
        String repositoryValidatorName=new String("LogVRepositoryValidator.x");
        String repositoryValidatorPath=rValidatorPath;//new String("d:\\andres\\elvira\\algra\\logV\\repository\\");
        
        String repositoryName=new String("LogVRepository.x");
        String repositoryPath=rPath;//new String("d:\\andres\\elvira\\algra\\logV\\dbc\\");
        /*
        ClassifierDBCRepository repos=new ClassifierDBCRepository(repositoryName,repositoryPath);
        
        FileReader f=new FileReader(new String(repositoryPath+"AllDBCNames.txt"));
        LineNumberReader bf=new LineNumberReader(f);
        String s=new String();
        Vector dbcNames=new Vector();
        int cont=0;
        while((s=bf.readLine())!=null){
            String cdbcPath=new String(repositoryPath+s);
            ClassifierDBC cdbc=new ClassifierDBC(new FileInputStream(cdbcPath));
            if (s.compareTo("wga_20.dbc")==0)
                cdbc=new ClassifierDBC(new FileInputStream(cdbcPath),0);

            repos.addClassifierDBC(cdbc);
            repos.writeSerialization();
            cont++;
        }
        f.close();
        */
        /*
        String cdbcPath=new String("d:\\tmp\\hepatitis.dbc");
        ClassifierDBC cdbc=new ClassifierDBC(new FileInputStream(cdbcPath));
        repos.addClassifierDBC(cdbc);
        repos.writeSerialization();
        
        cdbcPath=new String("d:\\tmp\\heart.dbc");
        cdbc=new ClassifierDBC(new FileInputStream(cdbcPath));
        repos.addClassifierDBC(cdbc);
        repos.writeSerialization();
        
        cdbcPath=new String("d:\\tmp\\australian-d.dbc");
        cdbc=new ClassifierDBC(new FileInputStream(cdbcPath));
        repos.addClassifierDBC(cdbc);
        repos.writeSerialization();
        
        cdbcPath=new String("d:\\tmp\\wga_20.dbc");
        cdbc=new ClassifierDBC(new FileInputStream(cdbcPath),0);
        repos.addClassifierDBC(cdbc);
        repos.writeSerialization();
        */
        
        //ClassifierDBCRepository repos=ClassifierDBCRepository.readSerialization(repositoryPath, repositoryName,0);
        //ClassifierDBCRepositoryValidator reposValidator = new ClassifierDBCRepositoryValidator(repos, repositoryValidatorName, repositoryValidatorPath); 
        //reposValidator.writeSerialization();
        ClassifierDBCRepositoryValidator reposValidator = ClassifierDBCRepositoryValidator.readSerialization(repositoryValidatorPath,repositoryValidatorName, 0);

        
        //reposValidator.IR_initializeKFC();
        //reposValidator.IR_getClassifierBasedOrder();
         

        reposValidator.allValidations(17,1,"Classificador con ERROR solamente");
        reposValidator.writeSerialization();        
        reposValidator.allValidations(22,"Classificador con LOGV solamente");
        reposValidator.writeSerialization();        
        reposValidator.allValidations(23,"Classificador con LOGV nuevo");
        reposValidator.writeSerialization();        
        //reposValidator.allValidations(1,null);
        //reposValidator.allValidations(20,null);
        reposValidator.printValidationResults();
        reposValidator.writeSerialization();
    }
    
    public void normalizeRepository() throws Exception{
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            ClassifierDBCValidator cdbcvalidator=this.getCDBCValidator(i);
            double[][] statistics=cdbcvalidator.getClassifierDBC().normalization();
            System.out.println("normalizeRepository Partial: "+i);
            for (int j=0; j<cdbcvalidator.getNumTotalValidations(); j++){
                ClassifierDBCValidation cdbcvalidation=cdbcvalidator.getCDBCValidation(j);
                for (int k=0; k<cdbcvalidation.getNumTT(); k++){
                    cdbcvalidation.getTrainCDBC(k).normalization(statistics);
                    cdbcvalidation.getTestCDBC(k).normalization(statistics);
                }
            }
        }
    }
    
    public void logTransformation() throws Exception{
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            ClassifierDBCValidator cdbcvalidator=this.getCDBCValidator(i);
            cdbcvalidator.getClassifierDBC().logABSTransformation();
            System.out.println("logTransformation Partial: "+i);
            for (int j=0; j<cdbcvalidator.getNumTotalValidations(); j++){
                ClassifierDBCValidation cdbcvalidation=cdbcvalidator.getCDBCValidation(j);
                for (int k=0; k<cdbcvalidation.getNumTT(); k++){
                    cdbcvalidation.getTrainCDBC(k).logABSTransformation();
                    cdbcvalidation.getTestCDBC(k).logABSTransformation();
                }
            }
        }
    }

    
    public void discretization() throws Exception{
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            ClassifierDBCValidator cdbcvalidator=this.getCDBCValidator(i);
            ClassifierDBC cdbc=cdbcvalidator.getClassifierDBC().discretize();
            System.out.println("discretization Partial: "+i);
            for (int j=0; j<cdbcvalidator.getNumTotalValidations(); j++){
                ClassifierDBCValidation cdbcvalidation=cdbcvalidator.getCDBCValidation(j);
                for (int k=0; k<cdbcvalidation.getNumTT(); k++){
                    ClassifierDBC cdbcTrain=cdbcvalidator.cdbc.discretizeAgain(cdbcvalidation.getTrainCDBC(k));
                    cdbcvalidation.train=cdbcTrain;
                    ClassifierDBC cdbcTest=cdbcvalidator.cdbc.discretizeAgain(cdbcvalidation.getTestCDBC(k));
                    cdbcvalidation.test=cdbcTest;
                }
            }
            cdbcvalidator.cdbc=cdbc;
        }
    }
    
    public void setMinMaxDBCs() throws Exception{
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++){
            ClassifierDBCValidator cdbcvalidator=this.getCDBCValidator(i);
            DataBaseCases dbc=cdbcvalidator.getClassifierDBC();
            dbc.setMinMax();
            System.out.println("setMinMaxDBCs Partial: "+i);
            for (int j=0; j<cdbcvalidator.getNumTotalValidations(); j++){
                ClassifierDBCValidation cdbcvalidation=cdbcvalidator.getCDBCValidation(j);
                for (int k=0; k<cdbcvalidation.getNumTT(); k++){
                    DataBaseCases train=cdbcvalidation.getTrainCDBC(k);
                    DataBaseCases test=cdbcvalidation.getTestCDBC(k);
                    for (int l=0; l<dbc.getVariables().size(); l++){
                        if(dbc.getVariables().elementAt(l).getClass()==Continuous.class){
                            Continuous node=(Continuous)dbc.getVariables().elementAt(l);
                            if (train.getVariables().getId(node)!=-1){
                                train.setNodeMin(node, node.getMin());
                                train.setNodeMax(node, node.getMax());
                                test.setNodeMin(node, node.getMin());
                                test.setNodeMax(node, node.getMax());
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static ClassifierDBCRepositoryValidator readSerialization(String cdbcRepositoryValidatorPath,String cdbcRepositoryValidatorName, int type) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{

        System.out.println("deserializing...");        
        
        FileInputStream fis = new FileInputStream(new String(cdbcRepositoryValidatorPath+cdbcRepositoryValidatorName));
        ObjectInputStream ois = new ObjectInputStream( fis );
        ClassifierDBCRepositoryValidator retrieved = (ClassifierDBCRepositoryValidator)ois.readObject();
        ois.close();
        fis.close();

        if (type==1){
            try{
                retrieved.setRepositoryValidatorPath(cdbcRepositoryValidatorPath);
            }catch(Exception e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        
        System.out.println("deserialized...");                
        return retrieved;
    }

    public void writeSerialization() throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {
        this.writeSerialization(new String(this.cdbcRepositoryValidatorPath+this.cdbcRepositoryValidatorName));
    }

    public void writeSerialization(String objectFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {

        System.out.println("serializing...");        
        
        this.cdbcRepository.writeSerialization();
        
        if (this.classifierDBCValidator!=null) 
            this.classifierDBCValidator.writeSerialization();        
        
        File objectF = new File(objectFile);
        if ( objectF.exists() ) {
        objectF.delete();
        }
        FileOutputStream fos = new FileOutputStream( objectF);
        ObjectOutputStream oos = new ObjectOutputStream( fos );
        oos.writeObject( this );
        oos.flush();
        oos.close();
        fos.flush();
        fos.close();
        
        System.out.println("serialized...");        
    }
    
    public void setRepositoryValidatorPath(String path) throws Exception{
        this.cdbcRepositoryValidatorPath=path;
        for (int i=0; i<this.cdbcRepository.getNumberOfCDBC(); i++)
            this.getCDBCValidator(i).setCDBCValidatorPath(path);
    }
    

    public static ClassifierDBCRepositoryValidator loadKentRidgeRepositoryURL(String repositoryValidatorName, String repositoryValidatorPath, String repositoryName, String repositoryPath) throws Exception{

        
        URL url=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+ClassifierDBCRepository.fichALLXNamesPath));
        InputStream input=url.openStream();
        
        Vector cDBCPaths=new Vector();
        Vector cDBCNames=new Vector();
        int cont=0;
        byte chara;
        byte[] sequence=new byte[1000];

        while((chara=(byte)input.read())!=-1){
            sequence[0]=chara;
            int k=1;
            while ((chara=(byte)input.read())!='\n'){
                sequence[k]=chara;
                k++;
            }
            String name=new String(sequence,0,k);
            cDBCNames.addElement(name);
            cDBCPaths.addElement(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH + ((String)cDBCNames.elementAt(cont))));
            cont++;
        }
        input.close();

        ClassifierDBCRepository repository=new ClassifierDBCRepository(repositoryName, repositoryPath);
        ClassifierDBCRepositoryValidator repos = new ClassifierDBCRepositoryValidator(repository, repositoryValidatorName, repositoryValidatorPath);
        
        int N=1;
        URL urlDBC;
        ClassifierDBC dbc;
        ClassifierDBC dbcTrain;
        ClassifierDBC dbcTest;
            
        //BreastCancer 0
        N=1;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //BreastCancer train and test
        N=2;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        N=0;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addTrainTestValidation(0,dbcTrain, dbcTest, new String("Train and Test used in Veer et al. 2002"));

        //ColonTumor 1
        N=3;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);

        //DLBCL-Harvard-Outcome 2
        N=4;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        

        //DLBCL-Harvard-Tumor 3
        N=5;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);


        //DLBCL-NIH 4
        N=7;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //DLBCL-NIH train and test
        N=8;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        N=6;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addTrainTestValidation(4,dbcTrain, dbcTest, new String("Train and Test used in Rosenwald et al. 2002"));
        

        //DLBCL-Stanford 5
        N=9;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);


        //leukemia-ALL_AML 6
        N=11;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //leukemia-ALL_AML train and test
        N=12;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        N=10;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addTrainTestValidation(6,dbcTrain, dbcTest, new String("Train and Test used in Golub et al. 1999"));
        

        //ML 7
        N=14;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //leukemia-ALL_AML train and test
        N=15;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        N=13;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addTrainTestValidation(7,dbcTrain, dbcTest, new String("Train and Test used in Armstrong et al. 2002"));


        //lungcancer-hardvard2 8
        N=17;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //lungcancer-hardvard2 train and test
        N=18;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        N=16;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addTrainTestValidation(8,dbcTrain, dbcTest, new String("Train and Test used in Gordon et al. 2002"));
        
        
        //lungcancer-ontario 9
        N=19;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);

        //lungcancer-hardvard1 10
        N=20;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //lungcancer-michigan 11
        N=21;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);

        //centralNervous-outcome 12
        N=22;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);

        
        //Ovarian_61902 13
        N=23;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //prostate_outcome 14
        N=24;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //prostate_TumorVSNormal 15
        N=26;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addClassifierDBC(dbc);
        
        //prostate_TumorVSNormal train and test
        N=27;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        N=25;
        urlDBC=new URL(new String(ClassifierDBCRepository.KENT_RIDGE_URL_PATH+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(urlDBC.openStream()),0);
        repos.addTrainTestValidation(15,dbcTrain, dbcTest, new String("Train and Test used in Gordon et al. 2002"));
        
        return repos;
    }


    
    public static ClassifierDBCRepositoryValidator loadKentRidgeRepositoryDisk(String repositoryValidatorName, String repositoryValidatorPath, String repositoryName, String repositoryPath) throws Exception{

        
        FileReader f=new FileReader(new String(repositoryPath+ClassifierDBCRepository.fichALLXNamesPath));
        LineNumberReader bf=new LineNumberReader(f);
        
        Vector cDBCPaths=new Vector();
        Vector cDBCNames=new Vector();
        int cont=0;
        byte chara;
        byte[] sequence=new byte[1000];

        String name=null;
        while((name=bf.readLine())!=null){
            cDBCNames.addElement(name);
            cDBCPaths.addElement(new String(repositoryPath + ((String)cDBCNames.elementAt(cont))));
            cont++;
        }
        f.close();

        ClassifierDBCRepository repository=new ClassifierDBCRepository(repositoryName, repositoryPath);
        ClassifierDBCRepositoryValidator repos = new ClassifierDBCRepositoryValidator(repository, repositoryValidatorName, repositoryValidatorPath);
        
        
        int N=1;
        //FileInputStream file;
        ClassifierDBC dbc;
        ClassifierDBC dbcTrain;
        ClassifierDBC dbcTest;
        /*
        for (N=0; N<cDBCNames.size(); N++){
            FileInputStream file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
            dbc=new ClassifierDBC(file,0);
            repos.addClassifierDBC(dbc);
        }*/

        //BreastCancer 0
        N=1;
        FileInputStream file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //BreastCancer train and test
        N=2;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        N=0;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addTrainTestValidation(0,dbcTrain, dbcTest, new String("Train and Test used in Veer et al. 2002"));

        //ColonTumor 1
        N=3;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);

        //DLBCL-Stanford 2
        N=4;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);

        //DLBCL-Harvard-Outcome 3
        N=5;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        

        //DLBCL-Harvard-Tumor 4
        N=6;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);


        //DLBCL-NIH 5
        N=8;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //DLBCL-NIH train and test
        N=9;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        N=7;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addTrainTestValidation(5,dbcTrain, dbcTest, new String("Train and Test used in Rosenwald et al. 2002"));
        

        //leukemia-ALL_AML 6
        N=11;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //leukemia-ALL_AML train and test
        N=12;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        N=10;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addTrainTestValidation(6,dbcTrain, dbcTest, new String("Train and Test used in Golub et al. 1999"));
        

        //ML 7
        N=14;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //ML train and test
        N=15;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        N=13;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addTrainTestValidation(7,dbcTrain, dbcTest, new String("Train and Test used in Armstrong et al. 2002"));


        //lungcancer-hardvard2 8
        N=17;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //lungcancer-hardvard2 train and test
        N=18;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        N=16;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addTrainTestValidation(8,dbcTrain, dbcTest, new String("Train and Test used in Gordon et al. 2002"));
        
        
        //lungcancer-ontario 9
        N=19;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);

        //lungcancer-hardvard1 10
        N=20;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //lungcancer-michigan 11
        N=21;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);

        //centralNervous-outcome 12
        N=22;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);

        
        //Ovarian_61902 13
        N=23;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //prostate_outcome 14
        N=24;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //prostate_TumorVSNormal 15
        N=26;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addClassifierDBC(dbc);
        
        //prostate_TumorVSNormal train and test
        N=27;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        N=25;
        file=new FileInputStream(new String(repositoryPath+cDBCNames.elementAt(N)));
        dbcTest=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        repos.addTrainTestValidation(15,dbcTrain, dbcTest, new String("Train and Test used in Gordon et al. 2002"));
        
        return repos;
 
    }

    public static ClassifierDBCRepositoryValidator loadECSQARURepositoryDisk(String repositoryValidatorName, String repositoryValidatorPath, String repositoryName, String repositoryPath) throws Exception{

        ClassifierDBCRepository repository=new ClassifierDBCRepository(repositoryName, repositoryPath);
        
        ClassifierDBCRepositoryValidator repos = new ClassifierDBCRepositoryValidator(repository, repositoryValidatorName, repositoryValidatorPath);
        
        FileInputStream file;
        ClassifierDBC dbc;
        ClassifierDBC dbcTrain;
        ClassifierDBC dbcTest;
            

        file=new FileInputStream(new String(repositoryPath+"wga.x"));
        dbc=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
        //dbc=new ClassifierDBC(new DataBaseCases(new String(repositoryPath+"wga.dbc")),0);
        //dbc.writeSerialization(new String(repositoryPath+"wga.x"));
        repos.addClassifierDBC(dbc);
        
        Vector train=new Vector();
        Vector test=new Vector();
        for (int i=0; i<10; i++){

            file=new FileInputStream(new String(repositoryPath+"WGATrain-select.dbc"+Integer.toString(200+i)));
            //dbcTrain=new ClassifierDBC(DataBaseCases.readSerialization(file),0);
            dbcTrain=new ClassifierDBC(new DataBaseCases(file),0);
            dbcTrain.setName(new String("WGATrain"+i));
            train.addElement(dbcTrain);
            file=new FileInputStream(new String(repositoryPath+"WGATest-select.dbc"+Integer.toString(200+i)));
            dbcTest=new ClassifierDBC(new DataBaseCases(file),0);
            dbcTest.setName(new String("WGATest"+i));
            test.addElement(dbcTest);
            
        }
        repos.addTrainTestValidation(0,train, test, new String("Train and Test used in PGM04 paper"));
        
        return repos;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        /*
        String rValidatorPath=null;
        String rPath=null;
        if (args.length!=2){
            rValidatorPath=new String("d:\\andres\\elvira\\algra\\logV\\repository\\");
            rPath=new String("d:\\andres\\elvira\\algra\\logV\\dbc\\");
        }else{
            rValidatorPath=args[0];
            rPath=args[1];
        }
        ClassifierDBCRepositoryValidator.generateLogVRepository(rValidatorPath,rPath);
        */
        
        ClassifierDBCRepositoryValidator.generateKentRidgeRepository(args[0]);
        //ClassifierDBCRepositoryValidator.generateECSQARURepository();
        //ClassifierDBCRepositoryValidator.generateAlbertoRepository();
        
        //proccesingKentRidgeRepositoryDATAtoDBC.main(null);
        
        //String repositoryName=new String("KentRidgeRepository.x");
        //String repositoryPath=new String("c:\\tmp\\repos\\repository\\");
        //String repositoryPath=new String("h:\\andres\\elvira\\datasets\\Kent Ridge Bio-medical Data Set Repository\\repository\\");
        //String repositoryPath=new String("/windows/andres/elvira/datasets/Kent Ridge Bio-medical Data Set Repository/repository/");
        //String repositoryPath=new String("/home/gte/andrew/repository/repository/");
        //String cDBCPath=new String("h:\\andres\\elvira\\datasets\\Kent Ridge Bio-medical Data Set Repository\\primarydata\\");
        //ClassifierDBCRepository repos=new ClassifierDBCRepository(repositoryName,repositoryPath);
        //ClassifierDBCRepository repos=ClassifierDBCRepository.loadKentRidgeRepositoryDisk(repositoryName,cDBCPath,repositoryPath);
        //repos.writeSerialization();
        //repos.addAllClassiferDBCURL();
        

        
        //ClassifierDBCRepository repos=ClassifierDBCRepository.loadECSQARURepositoryDisk(repositoryName, repositoryPath, cDBCRootPath);

        
        //String repositoryName=new String("ECSQARURepository.x");
        //String repositoryPath=new String("/windows/andres/elvira/esqaru/repositoryECSQARU/");
        //String cDBCPath=new String("/windows/andres/elvira/esqaru/dbcs/dbcs200/");
        //String repositoryPath=new String("d:\\andres\\elvira\\esqaru\\repositoryECSQARU\\");

        //ClassifierDBCRepository repos=ClassifierDBCRepository.readSerialization(repositoryPath,repositoryName,0);        
        //repos.IR_getClassifierBasedOrder();
        //repos.saveAllDBCs("c:\\tmp\\p\\");
        //repos.allValidations(20);
        //repos.writeSerialization();
        
        //repos.printRepositoryInformation();
        //repos.printCompleteRepositoryInformation();
        //repos.setMinMaxDBCs();
        //repos.validation(0,20,ClassifierDBCValidator.SPECIFIC_VALIDATION);
        //repos.validation(0,19,ClassifierDBCValidator.SPECIFIC_VALIDATION);
        //repos.validation(0,16,ClassifierDBCValidator.SPECIFIC_VALIDATION);
        //repos.validation(0,17,ClassifierDBCValidator.SPECIFIC_VALIDATION);
        
        
        //repos.IR_getClassifierBasedOrder();
        //repos.writeSerialization();

        //repos.printClassifierDBCValidator(0);                
        
        
        
        /*        ClassifierDBC d=repos.getCDBCValidator(8).getCDBCValidation(0).getTestCDBC(0);
        double[] data=d.getDataAll((Continuous)d.getVariables().elementAt(10));
        System.out.println(Stat.mean(data));
        System.out.println(Stat.standardDeviation(data));
*/        
        //repos.printCompleteRepositoryInformation();
        //repos.normalizeRepository();
        //System.out.println("Min Max");
        //repos.setMinMaxDBCs();
        //repos.saveAllDBCs("c:\\tmp\\");
        
        //repos.writeSerialization();
        

/*
        
        for (int k=0; k<repos.getNumberOfCDBC(); k++){
            ClassifierDBC cdbc=repos.getCDBCValidator(k).getClassifierDBC();
            cdbc.getSortAnovaNodes();
            repos.writeSerialization();
            double[][][] r=cdbc.correlationBetweenNodes(1000);
            System.out.println("\nDBC: "+k+" : ");
            for (int i=0; i<r.length; i++){
                System.out.println("Histogram Class: "+i);
                System.out.println("Intervals \t Number");
                for (int j=0; j<r[i][0].length; j++){
                        System.out.println(Fmath.truncate(r[i][0][j],2)+" \t "+r[i][1][j]);
                }
            }
            System.out.println();
        }
        repos.writeSerialization();
 */
/*        
        for (int k=0; k<15; k++){
            ClassifierDBC cdbc=repos.getCDBCValidator(k).getClassifierDBC();
            double[] r=cdbc.KSLogNormalityTest(10);
            System.out.println("\nDBC: "+k+" : ");
            for (int i=0; i<r.length; i++){
                System.out.print(r[i]+", ");
            }
            System.out.println();
        }
*/
   /*
        for (int k=0; k<repos.getNumberOfCDBC(); k++){
            ClassifierDBC cdbc=repos.getCDBCValidator(k).getClassifierDBC();

            double[][] r=new double[1][cdbc.getVariables().size()];
            for (int l=1; l<cdbc.getVariables().size(); l++){
                  Continuous variable=(Continuous)cdbc.getVariables().elementAt(l);
                  int[] ngroup=new int[cdbc.getNumberOfStates()];
                  double[][] datos=cdbc.getData(variable,ngroup);
                  r[0][0]=0;
                  for (int m=0; m<cdbc.getNumberOfStates(); m++){
                    r[0][l]+=Stat.standardDeviation(Stat.adjust(datos[m],ngroup[m]));
                  }
                  r[0][l]/=cdbc.getNumberOfStates();
            }
            
            System.out.println("DBC: "+k);
            System.out.println(Stat.mean(r[0]));
            System.out.println(Stat.standardDeviation(r[0]));
            
            r=cdbc.distributionTest(1000);
            
            System.out.println("\nDBC: "+k+" : ");
            for (int i=0; i<r.length; i++){
                for (int j=0; j<r[i].length; j++)
                    System.out.print(r[i][j]+", ");
                System.out.println();
            }
            System.out.println();
             
        }
*/
    }
    
}
