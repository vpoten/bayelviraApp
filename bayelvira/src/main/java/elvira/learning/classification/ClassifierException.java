/* ClassifierException.java */
package elvira.learning.classification;

/**
 * This class is define in order to throw the exception related to
 * classifier.
 * 
 * @author Rosa Blanco rosa@si.ehu.es, UPV
 * @version 1.0
 * @since 08/10/2003
 */

public class ClassifierException extends Exception{
    public static final int classifierEmpty    = 0;
    public static final int differentVariables = 1;
    public static final int differentStates    = 2;

    /**
     * Constructor
     * @param int k. The kind of the exception to thorw
     */
    public ClassifierException(int k){
      super("Classifier Exception");
      switch (k){
        case classifierEmpty   : System.out.println("The classifier is not trained");
                                 break;
        case differentVariables: System.out.println("The number of variables is different to the classifier number of variables");
                                 break;
        case differentStates   : System.out.println("The number of states of variable is different to the number of states in the classifier");
                                 break;
      }
    }
} //End of class
