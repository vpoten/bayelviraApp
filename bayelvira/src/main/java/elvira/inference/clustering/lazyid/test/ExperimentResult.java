package elvira.inference.clustering.lazyid.test;

import elvira.IDiagram;
import elvira.tools.PropagationStatistics;

import java.io.*;

class ExperimentResult implements Serializable{
  /**
   * Data member for storing the network name
   */
  private String networkName;
    
  /**
   * Data member for storing the kind of algorithm
   */
  private EnumAlgorithms algorithm;
  
  /**
   * Data member for storing the number of chance nodes
   */
  private int chanceNodes;
  
  /**
   * Data member for storing the number of decision nodes
   */
  private int decisionNodes;
  
  /**
   * Data member for storing the number of value nodes
   */
  private int valueNodes;
  
  /**
   * Data member for storing the number of value nodes
   */
  private int links;
  
  /**
   * Data member for storing the initial potential size
   */
  private double initialPotentialSize;
  
  /**
   * Data member for storing the number of constrained configurations
   */
  private double constrainedConfigurations;
  
  /**
   * Data member for storing the max. potential size
   */
  private double maxPotentialSize;
  
  /**
   * Data member for avg. potential size
   */
  private double avgPotentialSize;
  
  /**
   * Data member for storing the computation time 
   */
  private double time;
  
  /**
   * Constructor
   */
  public ExperimentResult(String networkName, EnumAlgorithms algorithm){
     this.algorithm=algorithm;
     this.networkName=networkName;
  }
  
  /**
   * Method for getting the name
   */
  public String getNetworkName(){
      return networkName;
  }
  
  /**
   * Method for getting the kind of algorithm
   */
  public EnumAlgorithms getAlgorithm(){
      return algorithm;
  }
  
  /**
   * Public method for setting the initial data
   * @param diagram
   */
  public void setInitialData(IDiagram diagram){
System.out.println("ExperimentResult: setInitialData");
     chanceNodes=diagram.numberOfChanceNodes();
      decisionNodes=diagram.numberOfDecisions();
      valueNodes=diagram.numberOfValueNodes();
      links=diagram.numberOfLinks();    
      initialPotentialSize=diagram.calculateSizeOfPotentials();  
      // Print the information
      System.out.println("Chance: "+chanceNodes+"  Decision: "+decisionNodes+
                         "  Value: "+valueNodes+"  Links: "+links+
                         "  Size: "+initialPotentialSize+
                         "  Constrained confs: "+constrainedConfigurations);
System.out.println("FIN ExperimentResult: setInitialData");      
  }
  
  /**
   * Method for setting the initial potential size
   * @param size
   */
  public void setInitialPotentialSize(double size){
      initialPotentialSize=size;
  }
  
  /**
   * Method for setting the number of constrained configurations
   * @param constrainedConf
   */
  public void setConstrainedConfigurations(double constrainedConf){
      constrainedConfigurations=constrainedConf;
  }
  
  /**
   * Public method for setting the final data about an evaluation
   * @param diagram
   */
  public void setFinalData(PropagationStatistics statistics){
       // Get the final values of max size, average size and time of computation
       maxPotentialSize=statistics.getMaximumSize();
       avgPotentialSize=statistics.getAverageSize();
       time=statistics.getMaximumTime();
  }

  // Present results
  public void printResults(){
     // Print the data about lazy propagation without trees
     printData();
     System.out.println();
  }


  /**
   * Print the data about the propagation
   */
  private void printData(){
    System.out.printf("%5d %5d %5d %5d %10.2f %10.2f %10.2f %10.2f %10.2f",
            chanceNodes,
            decisionNodes,
            valueNodes,
            links,
            initialPotentialSize,
            constrainedConfigurations,
            maxPotentialSize,
            avgPotentialSize,
            time);
  }
  
  /**
   * Method for saving the result object
   */
  public void saveObject(){
      // Compound the name of the file: netName + algorithm + extension
      String finalName=new String(networkName+"-"+algorithm.toString()+".resLazy");
      try{
        FileOutputStream file=new FileOutputStream(finalName);
        ObjectOutputStream stream=new ObjectOutputStream(file);
        stream.writeObject(this);
      }
      catch(IOException e){
          System.out.println("Error storing experiment result");
          System.exit(0);
      }
  }
  
  /**
   * Method for returning the number of chance nodes
   */
  public int getChanceNodes(){
      return chanceNodes;
  }
  
  /**
   * Method for returning the number of links
   */
  public int getLinks(){
      return links;
  }
  
  /**
   * Method for returning the initial size
   */
  public double getInitialPotentialSize(){
      return initialPotentialSize;
  }
  
  /**
   * Method for getting the max potential size
   */
  public double getMaxPotentialSize(){
      return maxPotentialSize;
  }
  
  /**
   * Method for getting the average potential size
   */
  public double getAvgPotentialSize(){
      return avgPotentialSize;
  }
  
  /**
   * Method for getting the time
   */
  public double getTime(){
      return time;
  }
}

