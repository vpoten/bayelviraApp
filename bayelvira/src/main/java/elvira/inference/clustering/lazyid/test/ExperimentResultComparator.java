/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package elvira.inference.clustering.lazyid.test;

/**
 *
 * @author mgomez
 */
public class ExperimentResultComparator {
    // Constants
    enum EnumData {INITPOTSIZE, AVGPOTSIZE, MAXPOTSIZE, TIME};
    
    /**
     * Data member for storing the reference experiment results
     */
    private ExperimentResult ref;
    
    /**
     * Data member for storing the other experiment results
     */
    private ExperimentResult other;
    
    /**
     * Data member for storing the number of chance nodes
     */
    private int chanceNodes;
  
    /**
     * Data member for storing the number of value nodes
     */
    private int links;
  
    /**
     * Data member for storing the initial potential size
     */
    private double initialPotSizeImprovement;
  
    /**
     * Data member for storing the max. potential size
     */
    private double maxPotSizeImprovement;
  
    /**
     * Data member for avg. potential size
     */
    private double avgPotSizeImprovement;
  
    /**
     * Data member for storing the computation time 
     */
    private double timeImprovement;

    
    /**
     * Class constructor
     */
    public ExperimentResultComparator(NetResult netResult, EnumAlgorithms refAlg, EnumAlgorithms otherAlg){
        ref=netResult.getResult(refAlg);
        other=netResult.getResult(otherAlg);
        // Check if both data are available
        if (ref == null || other == null){
            throw new RuntimeException("Data not available: ExperimentResultComparator");
        }
    }
    
    /**
     * Get the measurements
     */
    public void compute(){
        // Print the data
        ref.printResults();
        other.printResults();
        
        // Get the number of chance nodes 
        chanceNodes=ref.getChanceNodes();
        
        // Get the number of links
        links=ref.getLinks();
        
        // Compute the improvements
        initialPotSizeImprovement=computeData(EnumData.INITPOTSIZE);
        avgPotSizeImprovement=computeData(EnumData.AVGPOTSIZE);
        maxPotSizeImprovement=computeData(EnumData.MAXPOTSIZE);
        timeImprovement=computeData(EnumData.TIME);
    }
    
    /**
     * Method for computing a certain data
     */
    private double computeData(EnumData data){
        double refData=0,otherData=0;
        switch(data){
            case INITPOTSIZE: refData=ref.getInitialPotentialSize();
                              otherData=other.getInitialPotentialSize();
                              break;
            case AVGPOTSIZE: refData=ref.getAvgPotentialSize();
                             otherData=other.getAvgPotentialSize();
                             break;
            case MAXPOTSIZE: refData=ref.getMaxPotentialSize();
                             otherData=other.getMaxPotentialSize();
                             break;
            case TIME: refData=ref.getTime();
                       otherData=other.getTime();
                       break;
        }
        
        // Return the computed data
        return (100-(otherData*100/refData));
    }
    
    /**
     * Method for reaching chanceNodes
     */
    public int getChanceNodes(){
        return chanceNodes;
    }
    
    /**
     * Method for getting the links
     */
    public int getLinks(){
        return links;
    }
    
    /**
     * Method for getting the initial potential size
     * improvement
     */
    public double getInitialPotSizeImprovement(){
        return initialPotSizeImprovement;
    }
    
    /**
     * Method for getting the max potential size improvement
     */
    public double getMaxPotSizeImprovement(){
        return maxPotSizeImprovement;
    }
    
    /**
     * Method for getting the average potential size improvement
     */
    public double getAvgPotSizeImprovement(){
        return avgPotSizeImprovement;
    }
    
    /**
     * Method for getting the time improvement
     */
    public double getTimeImprovement(){
        return timeImprovement;
    }
}
