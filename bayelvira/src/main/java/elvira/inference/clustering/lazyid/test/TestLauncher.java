/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package elvira.inference.clustering.lazyid.test;

import java.io.*;

/**
 *
 * @author mgomez
 */
public class TestLauncher {

    /**
     * Data member for storing the baseName
     */
    private String baseName;
    
    /**
     * Data member for storing the algorithms
     */
    private EnumAlgorithms algorithm;
        
    /**
     * Array for storing the files containing the IDs to be evaluated
     */
    private File[] files;
        
    /**
     * Constructor
     */
    public TestLauncher(String baseName,EnumAlgorithms algorithm){
        this.baseName=baseName;
        this.algorithm=algorithm;
        
        // Get the files that must be evaluated
        getIds();
    }
    
    /**
     * Private method for getting the ids to evaluate
     */
    private void getIds(){
        File dir;
        ConcreteFileFilter fileFilter=new ConcreteFileFilter(baseName,".elv");
        
        // Look for ids in this directory
        try{
           dir=new File(".");
           files=dir.listFiles(fileFilter);
        }
        catch(Exception e){
           e.printStackTrace();
           System.exit(0);
        }
    }
    
    private void launchTests(){
        IDEvaluator evaluator;
        ExperimentResult result;
        
        for(int i=0; i < files.length; i++){
           // Create an IDEvaluator for every net
System.out.println("Evaluando "+algorithm.toString()+" sobre red: "+files[i].getName());
           evaluator=TestFactory.createTest(algorithm, files[i].getName());
           
           // Perform the computation
           evaluator.run();
           
           // Get the results and store them in a file
           result=evaluator.getResults();
           result.saveObject();
        }
    }
    /**
     * Main method
     */
    public static void main(String args[]){
        EnumAlgorithms algorithms[]=EnumAlgorithms.values();
        EnumAlgorithms algorithm=null;
        String baseName=null;
        int i=0;
        
        if (args.length != 4){
            printUsage();
        }
        
        // Now read the arguments
        while(i < args.length){
            if (args[i].equals("-name")){
                baseName=args[i+1];
                i=i+2;
            }
            else{
                if (args[i].equals("-alg")){
                    for(int j=0; j < algorithms.length; j++){
                        if (algorithms[j].toString().equals(args[i+1])){
                            algorithm=algorithms[j];
                            break;
                        }
                    }
                    i=i+2;
                }
            }
        }
        
        // Now create a test launcher and perform a concrete test
        // for the given algorithm and for every net matching basename
        TestLauncher launcher=new TestLauncher(baseName,algorithm);
System.out.println("Se ha creado el lanzador....");        
        // Perform the tests
        launcher.launchTests();
    }
    
    /**
     * Method for printing the usage information
     */
    private static void printUsage(){
        System.out.println("Usage: ");
        
        System.out.println("-name <base name for the ids to evaluate (no elv ext and no number>");
        
        System.out.println("-alg ");
        EnumAlgorithms algorithms[]=EnumAlgorithms.values();
        for(int i=0; i < algorithms.length; i++){
            System.out.println("     "+algorithms[i].toString());
        }
        
        System.exit(0);
    }
}
