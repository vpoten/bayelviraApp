/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package elvira.inference.clustering.lazyid.test;

/**
 * Class for storing the results about the evaluation of a single
 * idiagram
 * @author mgomez
 */
public class NetResult {
   /**
    * Data member for storing the results of getting the policies
    * using different algorithms
    */
    private ExperimentResult veResult;
    private ExperimentResult vewtResult;
    private ExperimentResult vewtacResult;
    private ExperimentResult lazyResult;
    private ExperimentResult lazywtResult;
    private ExperimentResult lazywtacResult;
    
    /**
     * Method for adding a particular result 
     * @param result
     */
    public void add(ExperimentResult result){
        switch(result.getAlgorithm()){
            case VE: veResult=result;
                     break;
            case VEWT: vewtResult=result;
                     break;
            case VEWTAC: vewtacResult=result;
                     break;
            case LAZY: lazyResult=result;
                     break;
            case LAZYWT: lazywtResult=result;
                     break;
            case LAZYWTAC: lazywtacResult=result;
                     break;
        }
    }
    
    /**
     * Method for getting the results related to a given
     * algorithm
     * @param alg
     */
    public ExperimentResult getResult(EnumAlgorithms alg){
        ExperimentResult result=null;
        switch(alg){
            case VE: result=veResult;
                     break;
            case VEWT: result=vewtResult;
                     break;
            case VEWTAC: result=vewtacResult;
                     break;
            case LAZY: result=lazyResult;
                     break;
            case LAZYWT: result=lazywtResult;
                     break;
            case LAZYWTAC: result=lazywtacResult;
                     break;
        }
        
        // Return result
        return result;
    }
    
    /**
     * Method for getting the name of the net
     */
    public String getNetName(){
        String netName=null;
        
        if (veResult != null){
            netName=veResult.getNetworkName();
        }
        else{
            if (vewtResult != null){
               netName=vewtResult.getNetworkName();
            }
            else{
                if (vewtacResult != null){
                    netName=vewtacResult.getNetworkName();
                }
                else{
                    if (lazyResult != null){
                        netName=lazyResult.getNetworkName();
                    }
                    else{
                        if (lazywtResult != null){
                            netName=lazywtResult.getNetworkName();
                        }
                        else{
                            if (lazywtacResult != null){
                                netName=lazywtacResult.getNetworkName();
                            }
                        }
                    }
                }
            }
        }
        
        // Return netName
        return netName;
    }
    
    /**
     * Method for computing the improvemt between two measures
     * @param ref
     * @param comp
     * @return improvement
     */
    private double computeImprovement(double ref, double comp){
        return (100-(comp*100)/ref);
    }  
}
