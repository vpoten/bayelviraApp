package elvira.decisionTrees;

public class DTBuildingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>DTBuildingException</code> without a 
     * detail message. 
     */
	public DTBuildingException() {
		super();
	}
	
    /**
     * Constructs an <code>DTBuildingException</code> with a detail message. 
     *
     * @param   s   the detail message.
     */
	public DTBuildingException(String s) {
		super(s);
	}	
}
