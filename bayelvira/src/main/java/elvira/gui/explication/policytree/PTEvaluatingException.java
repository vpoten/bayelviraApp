package elvira.gui.explication.policytree;

public class PTEvaluatingException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>DTBuildingException</code> without a 
     * detail message. 
     */
	public PTEvaluatingException() {
		super();
	}
	
    /**
     * Constructs an <code>DTBuildingException</code> with a detail message. 
     *
     * @param   s   the detail message.
     */
	public PTEvaluatingException(String s) {
		super(s);
	}	
}
