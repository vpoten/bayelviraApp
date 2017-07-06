package elvira.inference.clustering.lazyid.test;

import java.io.*;

import java.util.*;

public class GlobalResults{
  // Array to store the whole set of ExperimentResult objects
  private ArrayList<NetResult> results;
  
  // HashMap for storing the names of the processed idiagrams
  private TreeSet<String> processedIdiagrams;
  
  // Array containig the names of the files containing results
  File [] files;

  // Data member for storing the average number of chance nodes
  private double averageChanceNodes;

  // Data member for storing the average number of links
  private double averageLinks;

  // Data member for storing the improvements for the average sizes 
  // for the potentials at the beginning
  private double averageImpInitSize;

  // Data member for storing the average improv in max size
  private double averageImpMaxSize;

  // Data member for storing the average improv in avg. size
  private double averageImpAvgSize;

  // Data member for storing the average improvement for time
  private double averageImpTime;

  // Data member for storing the ref algorithm and the algorithm
  // to compare with
  private EnumAlgorithms refAlg, compAlg;

  /**
   * Class constructor
   */
  public GlobalResults(String basename, EnumAlgorithms refAlg, EnumAlgorithms compAlg){
    File dir;
    String netName;

    // Set the data members
    this.refAlg=refAlg;
    this.compAlg=compAlg;

    // Make the results arrayList
    results=new ArrayList<NetResult>();
    
    // Make the hashmap
    processedIdiagrams=new TreeSet<String>();

    // Get the files containing results
    try{
      dir=new File(".");
      files=dir.listFiles(new ConcreteFileFilter(basename,".resLazy"));
    }
    catch(Exception e){
        e.printStackTrace();
        System.exit(0);
    }

    // For every file, get the name of the id and store
    // it in the Set of names
    for(int i=0; i < files.length; i++){
        netName=getNetName(files[i].getName());
        
        // Put the name into the set of names
        if (!processedIdiagrams.contains(netName)){
            processedIdiagrams.add(netName);
        }
    }
    
    // Now get the idiagrams names, one by one, and get the
    // available results for it
    Iterator<String> iterator=processedIdiagrams.iterator();
    while(iterator.hasNext()){
      netName=iterator.next();
      
      // Read the results for this net
      readData(netName);
    }
  }

  /** 
   * Method for processing the results related to every net
   */
  public void resume(){
    NetResult netResults;
    ExperimentResultComparator comparator;
    int chanceNodes,links;
    double initialSize, maxSize, avgSize, time;

    // Print the algorithms under analysis
    System.out.println("Ref alg: "+refAlg.toString()+"  Comp alg: "+compAlg.toString());
    System.out.println();
    System.out.println("Chance  Decis   Value   Links    In.PS   Const   MaxPS   AvgPS   Time");
    System.out.println();
    
    // Consider every net
    for(int i=0; i < results.size(); i++){
        netResults=results.get(i);
        
        // Print the name
        System.out.println("----------"+netResults.getNetName()+" ----------");
                
        // Make a comparator
        comparator=new ExperimentResultComparator(netResults,refAlg,compAlg);
        
        // Compute the data
        comparator.compute();
        
        // Get the results for computing the final averages
        chanceNodes=comparator.getChanceNodes();
        averageChanceNodes+=chanceNodes;
        
        // Get the number of links
        links=comparator.getLinks();
        averageLinks+=links;
        
        // Get the initial size
        initialSize=comparator.getInitialPotSizeImprovement();
        averageImpInitSize+=initialSize;
        
        // Get the max size
        maxSize=comparator.getMaxPotSizeImprovement();
        averageImpMaxSize+=maxSize;
        
        // Get the average size
        avgSize=comparator.getAvgPotSizeImprovement();
        averageImpAvgSize+=avgSize;
        
        // Get the time
        time=comparator.getTimeImprovement();
        averageImpTime+=time;
        
        // Print the data with a certain format
        //System.out.printf("%8d %8d %10.2f %10.2f %10.2f %10.2f",chanceNodes,links,initialSize,maxSize,avgSize,time);
    }

    // Now print the results
    System.out.println("--------------------------       Global ----------------------------------");
    System.out.println("Avg. chance nodes: "+(averageChanceNodes/results.size()));
    System.out.println("Avg. links: "+(averageLinks/results.size()));
    System.out.println("Init. size: "+(averageImpInitSize/results.size()));
    System.out.println("Max. Size: "+(averageImpMaxSize/results.size()));
    System.out.println("Avg. Size: "+(averageImpAvgSize/results.size()));
    System.out.println("Avg. Time: "+(averageImpTime/results.size()));
  }

  /**
   * Private method for reading the results about a given idiagram
   * @param netName
   */
  private void readData(String netName){
     ArrayList<File> filesForNet;
     NetResult netResult;
     FileInputStream fileInput;
     ObjectInputStream stream;
     ExperimentResult result;
     
     // Get all the files related to this netName
     filesForNet=getFilesForNet(netName);
     netResult=new NetResult();
     
     // Now deal with every file: it must be read and stored in
     // a NetResult object and at the end stored in results array
     for(File file : filesForNet){
         try{
           fileInput=new FileInputStream(file.getName());
           stream=new ObjectInputStream(fileInput);
           result=(ExperimentResult)stream.readObject();
           netResult.add(result);
         }catch(Exception e){
             System.out.println("Error reading result file");
             System.exit(0);
         }
     }

     // At the end the netResult object is stored in results
     results.add(netResult);
  }
  
  /**
   * Private method for getting the name of the idiagrams
   * from a given file name
   * @param fileName
   */
  private String getNetName(String fileName){
      String netName;
      
      // Look the "-" char
      int indexOfMinus=fileName.indexOf('.');
      
      // Get the netName: from 0 to indexOfMinus-1
      netName=fileName.substring(0, indexOfMinus);
      
      // Return netName
      return netName;
  }
  
  /**
   * Private method for getting the files for a given net
   * @param netName
   */
  private ArrayList<File> getFilesForNet(String fileName){
      ArrayList<File> filesForNet=new ArrayList<File>();
      String fileNetName;
      
      // Consider the names of the files, one by one, and
      // store those related to filename
      for(int i=0; i < files.length; i++){
         fileNetName=getNetName(files[i].getName());
         if (fileName.equals(fileNetName)){
             filesForNet.add(files[i]);
         }
      }
      
      // Return fileForNet
      return filesForNet;
  }
  
  
  /* Main method
   * @param args
   */
  public static void main(String args[]){
     // Read the arguments related to the algorithms to compare
     String baseName=null;
     EnumAlgorithms algorithm1=null,algorithm2=null;
     EnumAlgorithms algorithms[]=EnumAlgorithms.values();
     int i=0;

     // Check the number of arguments used
     if (args.length != 6){
        printUsage();
     }
     
     // Read the arguments
      while(i < args.length){
            if (args[i].equals("-name")){
                baseName=args[i+1];
                System.out.println("Basename: "+baseName);
                i=i+2;
            }
            else{
                if (args[i].equals("-alg1")){
                    for(int j=0; j < algorithms.length; j++){
                        if (algorithms[j].toString().equals(args[i+1])){
                            algorithm1=algorithms[j];
                            System.out.println("algoritmo1: "+algorithm1.name());
                            break;
                        }
                    }
                    i=i+2;
                }
                else{
                   if (args[i].equals("-alg2")){
                      for(int j=0; j < algorithms.length; j++){
                         if (algorithms[j].toString().equals(args[i+1])){
                            algorithm2=algorithms[j];
                            System.out.println("algoritmo2: "+algorithm2.name());
                            break;
                         }
                      }
                      i=i+2;
                   }
                   else{
                      printUsage();
                   }
                }
            }
        }

     // Resume the content
     if (baseName != null && algorithm1 != null && algorithm2 != null){
       //Creates an object
       GlobalResults global=new GlobalResults(baseName, algorithm1, algorithm2);

       // Compare the results
       global.resume();
     }
  }
  
    /**
     * Method for printing the usage information
     */
    private static void printUsage(){
        System.out.println("Usage: ");
        
        System.out.println("-name <base name for the ids to evaluate (no elv ext and no number>");
        
        System.out.println("-alg1 <id1> -alg2 <id2> ");
        System.out.println("Possible values for id1 and id2");
        EnumAlgorithms algorithms[]=EnumAlgorithms.values();
        for(int i=0; i < algorithms.length; i++){
            System.out.println("     "+algorithms[i].toString());
        }
        
        System.exit(0);
    }
    
}

