package elvira.decisionTrees;

public class DTEvaluatingException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>DTBuildingException</code> without a 
     * detail message. 
     */
	public DTEvaluatingException() {
		super();
	}
	
    /**
     * Constructs an <code>DTBuildingException</code> with a detail message. 
     *
     * @param   s   the detail message.
     */
	public DTEvaluatingException(String s) {
		super(s);
	}	
}
