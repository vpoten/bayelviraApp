/*
 * ClassifierDBCRepository.java
 *
 * Created on 20 de febrero de 2005, 20:51
 */

package elvira.learning.classification.supervised.validation;

//import privateclass.proccesingKentRidgeRepositoryDATAtoDBC;
import elvira.tools.statistics.math.Fmath;
import elvira.learning.classification.supervised.validation.ClassifierDBC;
import elvira.database.DataBaseCases;
import elvira.learning.classification.supervised.mixed.*;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.ConfusionMatrix;
import elvira.Continuous;
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
 *  This class allows to manage a data base set. This class is used by 
 *  ClassifierDBCRepositoryValidator class to validate a data base set using
 *  several classifiers and several validation procedures. Each data base of the
 *  respository has to contain a target FiniteStates variable (for supervised learning) and
 *  it is stored as a ClassifierDBC object.
 *  
 *  Example:
 *  {
 *  ...
 *  //you have to create a specific directory where all data bases of the repository
 *  //are stored as serialized objects.
 *  ClassifierDBCRepository repos=new ClassifierDBCRepository("DNARepository","/home/andrew/DNARepository/databases/");
 *
 *  //you can add any data base.
 *  ClassifierDBC cdbc=new ClassifierDBC(new FileInputStream("lungCancer.dbc",0);
 *  respos.addClassifierDBC(cdbc);
 *
 *  cdbc=new ClassifierDBC(new FileInputStream("breastCancer.dbc",2);
 *  respos.addClassifierDBC(cdbc);
 *
 *  //At the end, you have to serialized the object.
 *  repos.writeSerialization();
 *
 *  //Print the information of this repository: the name of each data base, its number of 
 *  //cases and the variables, etc ..
 *  repos.printCompleteRepositoryInformation();
 *  ....
 *  }
 *  @author  andres
 */
public class ClassifierDBCRepository implements Serializable{
    
    static final long serialVersionUID = 157930118519853615L;
    
    /** The name of the repository*/
    protected String repositoryName=null;

    /** The directory path of the repository*/
    protected String repositoryPath=null;
    
    /** A String vector with the names of the all data bases.*/
    protected Vector cDBCNames=new Vector();
    

    public static final String fichALLXNamesPath=new String("AllDBCXNames.txt");
    

    public static final String  KENT_RIDGE_URL_PATH= new String("http://decsai.ugr.es/~andrew/dbcs/");
    

    
    public ClassifierDBCRepository(){
    }

    /** Creates a new instance of ClassifierDBCRepository */
    public ClassifierDBCRepository(String repositoryName, String repositoryPath) {
        this.repositoryName=repositoryName;
        this.repositoryPath=repositoryPath;
    }

    public void setRepositoryPath(String path) throws Exception{
        this.repositoryPath=path;
    }
    public String getRepositoryPath(){
        return this.repositoryPath;
    }
    private void initializeDBCNames() throws Exception{
        FileReader f=new FileReader(new String(this.repositoryPath+this.fichALLXNamesPath));
        LineNumberReader bf=new LineNumberReader(f);
        String s=new String();
        Vector dbcNames=new Vector();
        int cont=0;
        while((s=bf.readLine())!=null){
            this.cDBCNames.addElement(s);
            cont++;
        }
        f.close();
    }

    public ClassifierDBC getClassifierDBC(int i){
        try{
            return (ClassifierDBC)ClassifierDBC.readSerialization(this.getCDBCPath(i));
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public String getCDBCPath(int i){
        return new String(this.repositoryPath+((String)this.cDBCNames.elementAt(i)));
    }
  
    public void addClassifierDBC(ClassifierDBC cdbc) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{
        this.cDBCNames.addElement(cdbc.getName());
        cdbc.writeSerialization(new String(this.repositoryPath+cdbc.getName()));
        this.writeSerialization();
    }
    
    public void printRepositoryInformation(){
        System.out.println("Repository Name: "+this.repositoryName);
        System.out.println("Repository Path: "+this.repositoryPath);
        System.out.println("Nº Data Base of Cases: "+this.cDBCNames.size());
        System.out.println("DBCs Path: "+this.repositoryPath);
        for (int i=0; i<this.cDBCNames.size(); i++){
            System.out.println("DBC Num: "+i+", "+(String)this.cDBCNames.elementAt(i));
        }
    }
    
    public void printCompleteRepositoryInformation() throws Exception{
        System.out.println("Repository Name: "+this.repositoryName);
        System.out.println("Repository Path: "+this.repositoryPath);
        System.out.println("Nº Data Base of Cases: "+this.cDBCNames.size());
        System.out.println("DBCs Path: "+this.repositoryPath);
        for (int i=0; i<this.cDBCNames.size(); i++){
            ClassifierDBC cdbc=this.getClassifierDBC(i);
            System.out.println("DBC Num: "+i+", "+cdbc.getName());
            System.out.println("        Nº Cases: "+cdbc.getNumberOfCases());
            System.out.println("        Nº Variables: "+cdbc.getVariables().size());
            System.out.println("        Nº States: "+cdbc.getNumberOfStates());
            System.out.println();
        }
    }
    
    public static ClassifierDBCRepository readSerialization(String repositoryPath,String repositoryName, int type) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{

        System.out.println("deserializing...");        
        
        FileInputStream fis = new FileInputStream(new String(repositoryPath+repositoryName));
        ObjectInputStream ois = new ObjectInputStream( fis );
        ClassifierDBCRepository retrieved = (ClassifierDBCRepository) ois.readObject();
        ois.close();
        fis.close();
        
        if (type==1){
            try{
                retrieved.setRepositoryPath(repositoryPath);
            }catch(Exception e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        
        System.out.println("deserialized...");                
        return retrieved;
    }

    public void writeSerialization() throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException {
        this.writeSerialization(new String(this.repositoryPath+this.repositoryName));
    }

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
        fos.flush();
        fos.close();
        
        System.out.println("serialized...");        
    }

    public int getNumberOfCDBC(){
        return this.cDBCNames.size();
    }
}
