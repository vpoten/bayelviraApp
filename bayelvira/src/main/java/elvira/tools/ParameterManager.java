/*
 * ParameterManager.java
 *
 * Created on 19 de marzo de 2005, 10:49
 */

package elvira.tools;

import java.util.Vector;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.util.regex.Pattern;


/**
 * This class can be used to manage the parameters of an algorithm.
 * The parameters are loaded from a text file.
 * @author  andres
 */
public class ParameterManager {
    
    //protected static String fichName=new String("D:/andres/elvira/datasets/Kent Ridge Bio-medical Data Set Repository/KentRidgeRepository/resultados/parameters01.txt");
    //protected static String fichName=new String("d:\\andres\\tmp3\\parameters.txt");
    //protected static String fichName=new String("/home/gte/andrew/logV/parameters.txt");
    //protected static String fichName=new String("/home/gte/andrew/repositoryJGNB/parameters.txt");
    protected static String fichName=new String("d:\\andres\\software\\putty\\parameters.txt");
    
    /** Creates a new instance of ParameterManager */
    public ParameterManager() {
    }
    
    private static int getClassPosition(Vector allParams, String className){
        int pos=-1;
        for (int i=0; i<allParams.size(); i++){
            String classnameP=(String)((Vector)allParams.elementAt(i)).elementAt(0);
            if (classnameP.compareTo(className)==0){
                pos=i;
                break;
            }
        }
        return pos;
    }
    public static double getParameter(Object obj, int numParameter){
        return ParameterManager.getParameter(obj.getClass().getName(),numParameter);
    }
    public static double getParameter(String className, int numParameter){
        return Double.valueOf(ParameterManager.getStringParameter(className,numParameter)).doubleValue();
    }
    
    public static String getStringParameter(String className, int numParameter){
        Vector allParams=ParameterManager.loadParemeters();
        int pos=ParameterManager.getClassPosition(allParams, className);
        if (pos==-1){
            System.out.println("Parameter Not Found. Class Name not exist: "+className);
            System.exit(0);
        }
        Vector parametersFound=(Vector)((Vector)allParams.elementAt(pos)).elementAt(1);
        if (parametersFound.size()<=numParameter){
            System.out.println("Parameter Not Found. Paremeter Number not found: "+numParameter);
            System.exit(0);
        }
        
        String[] param=(String[])parametersFound.elementAt(numParameter);
        return param[1];
    }    
    public static void addParameter(String className, String comment, double value) throws Exception{
        Vector allParams=ParameterManager.loadParemeters();
        int pos=ParameterManager.getClassPosition(allParams, className);
        
        if (pos!=-1){
            Vector parametersFound=(Vector)((Vector)allParams.elementAt(pos)).elementAt(1);
            String[] paramnew=new String[3];
            paramnew[0]=Integer.toString(parametersFound.size());
            paramnew[1]=Double.toString(value);
            paramnew[2]=comment;
            parametersFound.addElement(paramnew);
        }else{
            Vector newclass=new Vector();
            newclass.addElement(className);
            Vector parametersFound=new Vector();
            String[] paramnew=new String[3];
            paramnew[0]=Integer.toString(parametersFound.size());
            paramnew[1]=Double.toString(value);
            paramnew[2]=comment;
            parametersFound.addElement(paramnew);
            newclass.addElement(parametersFound);
            allParams.addElement(newclass);
        }
        ParameterManager.saveParameters(allParams);
    }
    
    private static Vector loadParemeters(){
      try{
        FileReader f=new FileReader(new String(ParameterManager.fichName));
        LineNumberReader bf=new LineNumberReader(f);
        String s=new String();
        int cont=0;

        Vector allParameters=new Vector();
        while((s=bf.readLine())!=null){
            java.util.regex.Pattern x=Pattern.compile("\t");
            String[] names=x.split(s);
            Vector newclass=new Vector();
            newclass.addElement(names[0]); //Add the name of the class.
            int numparam=Integer.valueOf(names[1]).intValue(); //Return the number of parameters for this class
            Vector parameter=new Vector();
            for (int i=0; i<numparam; i++){
                s=bf.readLine();
                parameter.addElement(x.split(s));
            }
            newclass.addElement(parameter);
            allParameters.addElement(newclass);
            cont++;
        }
        f.close();
        return allParameters;
      }catch(Exception e){
          e.printStackTrace();
          System.exit(0);
          return null;
      }
      
    }
    private static void saveParameters(Vector allParameters) throws Exception{
        FileWriter f=new FileWriter(new String(ParameterManager.fichName));
        for (int i=0; i<allParameters.size(); i++){
            Vector newclass=(Vector)allParameters.elementAt(i);
            String nameclass=(String)newclass.elementAt(0);
            Vector parameter=(Vector)newclass.elementAt(1);
            f.write(new String(nameclass+"\t"+parameter.size()+"\n"));
            for (int j=0; j<parameter.size(); j++){
                String[] param=(String[])parameter.elementAt(j);
                f.write(new String(param[0]+"\t"+param[1]+"\t"+param[2]+"\n"));
            }
        }
        f.close();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        
    }
    
}
