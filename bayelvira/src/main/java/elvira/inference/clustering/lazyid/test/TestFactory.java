/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package elvira.inference.clustering.lazyid.test;

/**
 *
 * @author mgomez
 */
public class TestFactory {

    /**
     * Method for creating an specific kind of Evaluator
     */
    public static IDEvaluator createTest(EnumAlgorithms algorithm, String netName){
        IDEvaluator test=null;
        
        // Create the test
        switch(algorithm){
            case VE: test=new VEIDEvaluator(netName);
                     break;
            case VEWT: test=new VEWTIDEvaluator(netName);
                     break;
            case VEWTAC: test= new VEWTACIDEvaluator(netName);
                     break;
            case LAZY: test=new LazyIDEvaluator(netName);
                     break;
            case LAZYWT: test=new LazyWTIDEvaluator(netName);
                     break;
            case LAZYWTAC: test=new LazyWTACIDEvaluator(netName);
                     break;
        }
        
        // return the test
        return test;
    }
}
