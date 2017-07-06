package elvira.gui.explication.policytree;

public class PTBuildingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>DTBuildingException</code> without a 
     * detail message. 
     */
	public PTBuildingException() {
		super();
	}
	
    /**
     * Constructs an <code>DTBuildingException</code> with a detail message. 
     *
     * @param   s   the detail message.
     */
	public PTBuildingException(String s) {
		super(s);
	}	
}
