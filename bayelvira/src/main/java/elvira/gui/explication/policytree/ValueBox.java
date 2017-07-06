package elvira.gui.explication.policytree;

import elvira.FiniteStates;
import elvira.learning.policies.Rule;
import elvira.learning.policies.RuleNode;

/**
 * Provides a text summary of its associated PT node:
 * Utility, chance and value of the variable
 *  
 * @author Manuel Luque Gallego
 * @version 0.1
 */
public class ValueBox  {
	/**
	 * DT node associated with this summary
	 */
	private RuleNode source;
	
	/**
	 * True when the summary must show the utility value
	 * Used when supervalue/utility nodes aren't expanded (the value
	 * must appear at right hand of the supervalue/utility node and not
	 * at left hand of the node) 
	 */
	//private boolean utilityDisplayed= true;
	
	/** Creates a SummaryBox
	 * 
	 * @param rule DT node associated with this summary
	 */
	public ValueBox(RuleNode rule) {
		this.source= rule;
	}
	
	/**
	 * Getter of 'source' attribute
	 * 
	 * @return Associated DT node
	 */
	public RuleNode getSource() {
		return source;
	}
	
	/**
	 * Setter of 'utilityDisplayed' attribute
	 * 
	 * @param utilityDisplayed marks when to show/hide utility in this summary
	 */
	/*public void setUtilityDisplayed(boolean utilityDisplayed) {
		this.utilityDisplayed= utilityDisplayed;
	}*/
	
	/**
	 * Getter of 'utilityDisplayed' attribute
	 * 
	 * @return true if the utility will be displayed for this DT node
	 */
	/*public boolean isUtilityDisplayed() {
		return utilityDisplayed;
	}*/

	/** Get the HTML code with the info to display of the associated node of this Summary
	 * 
	 * @param precisionProxy Object with precision info of the DT nodes in the tree
	 * 
	 * @return HTML code showed in this summary box
	 * 
	 * @throws PTBuildingException 
	 * @throws PTEvaluatingException 
	 */
	public String getHTML() throws PTEvaluatingException {
		String stateString;
		
		// The box is showed as a HTML table
		String txtIzq="<html><table border=1>";

		// Si finalmente se incluye la coalescencia en el árbol de decision subyacente, el tratamiento
		// del padre/padres influirá a la hora de representar el cuadro de resumen ¿q prob mostraremos?
		RuleNode padre= source.getParent();
		//RuleNode padre = source;
		
		if( padre==null ) {
			// In this condition path, the summary box is showed for the root node: only the
			// utility of this node will be represented in the box 
		}
		else if( padre instanceof RuleNode ) {
			// If the node's parent is a chance node, it's showed the probability for its
			// branch to happen. This probability takes the custom precision for this node
			FiniteStates fs= (FiniteStates) padre.getVariable();
						
			// As much the variable value as this branch chance are written to the summary
			
			//stateString = trimQuotes(obtainNameOfStateAssignedInRuleNode());
			stateString = obtainNameOfStateAssignedInRuleNode();
			txtIzq+= "<td width=10px border=0></td>";
			txtIzq= txtIzq+"<td align=center border=0>" + fs.getName() + "=" + stateString +"</td>"; 
			 
		}
		else {
			// DONE: ¿crear una excepcion particular para capturarla(evitar que un problema se propague a elvira?
			throw new PTEvaluatingException("Opcion no contemplada ["+source.getParent()+"]");
		}
	
				
		// HTML code completion is done: table closed
		txtIzq+="</table></html>";
			
		return txtIzq;
	}
	
	private String obtainNameOfStateAssignedInRuleNode() {

		String stateString;
		Integer state;

		RuleNode father = source.getParent();

		if (source != null) {

			//FiniteStates fs = (FiniteStates) father.getVariable();

			stateString = source.getNameOfValueAssignedToVariableOfParent();
		}
		else {
			stateString = "";
		}
		return stateString;
	}

	/**
	 * Removes the quotes (if any) that surrounds the given text string
	 * 
	 * @param source text
	 * @return text with the quotes removed
	 */
	public String trimQuotes(String source) {
		String string="";
		if (source.length()>0){
		if( source.charAt(0)=='"' && source.charAt(source.length()-1)=='"' ) {
			string = source.substring(1,source.length()-1);
		}
		}
		else{
			string = "";
		}
		
		return string;
	}
}
