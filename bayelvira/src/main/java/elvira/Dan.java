package elvira;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Manolo_Luque
 *
 */
public class Dan extends IDWithSVNodes
{

	
	/**
	 * Saves the header of the file that will contain this decision analysis network
	 * @param p the file.
	 */

	public void saveHead(PrintWriter p) throws IOException {

	  p.print("// Decision Analysis Network\n");
	  p.print("//   Elvira format \n\n");
	  p.print("dan  \""+getName()+"\" { \n\n");
	  
	}
}
