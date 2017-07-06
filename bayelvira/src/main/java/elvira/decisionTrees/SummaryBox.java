package elvira.decisionTrees;

import elvira.FiniteStates;

/**
 * Provides a text summary of its associated DT node:
 * Utility, chance and value of the variable
 *  
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.2
 * 
 * version 0.1: fixed precision for utilities and chances
 * version 0.2: HTML use and custom node precisions
 */
public class SummaryBox {
	/**
	 * DT node associated with this summary
	 */
	private AbstractNode source;
	
	/**
	 * True when the summary must show the utility value
	 * Used when supervalue/utility nodes aren't expanded (the value
	 * must appear at right hand of the supervalue/utility node and not
	 * at left hand of the node) 
	 */
	private boolean utilityDisplayed= true;
	
	/** Creates a SummaryBox
	 * 
	 * @param source DT node associated with this summary
	 */
	public SummaryBox(AbstractNode source) {
		this.source= source;
	}
	
	/**
	 * Getter of 'source' attribute
	 * 
	 * @return Associated DT node
	 */
	public AbstractNode getSource() {
		return source;
	}
	
	/**
	 * Setter of 'utilityDisplayed' attribute
	 * 
	 * @param utilityDisplayed marks when to show/hide utility in this summary
	 */
	public void setUtilityDisplayed(boolean utilityDisplayed) {
		this.utilityDisplayed= utilityDisplayed;
	}
	
	/**
	 * Getter of 'utilityDisplayed' attribute
	 * 
	 * @return true if the utility will be displayed for this DT node
	 */
	public boolean isUtilityDisplayed() {
		return utilityDisplayed;
	}

	/** Get the HTML code with the info to display of the associated node of this Summary
	 * 
	 * @param precisionProxy Object with precision info of the DT nodes in the tree
	 * 
	 * @return HTML code showed in this summary box
	 * 
	 * @throws DTBuildingException 
	 * @throws DTEvaluatingException 
	 */
	public String getHTML(PrecisionProxy precisionProxy) throws DTEvaluatingException {

		// The box is showed as a HTML table
		String txtIzq="<html><table border=1>";

		// Si finalmente se incluye la coalescencia en el árbol de decision subyacente, el tratamiento
		// del padre/padres influirá a la hora de representar el cuadro de resumen ¿q prob mostraremos?
		AbstractNode padre= source.getParent();
		
		if( padre==null ) {
			// In this condition path, the summary box is showed for the root node: only the
			// utility of this node will be represented in the box 
		}
		else if( padre instanceof ChanceNode ) {
			// If the node's parent is a chance node, it's showed the probability for its
			// branch to happen. This probability takes the custom precision for this node
			FiniteStates fs= (FiniteStates) padre.getVariable();
			double chanceValue= ((ChanceNode) padre).getChance(source); 
			String txtChance= precisionProxy.formatChance(this,chanceValue);
			
			// As much the variable value as this branch chance are written to the summary
			String estado= trimQuotes(fs.getState(source.getConfiguration().getValue(fs.getName()))); 
			txtIzq+= "<td align=center border=0>" + fs.getName() + "=" + estado +"</td>"; 
			txtIzq+= "<td align=center border=0>P=" + txtChance + "</td>";
		}
		else if( padre instanceof DecisionNode ) {
			FiniteStates fs= (FiniteStates) padre.getVariable();
			
			// If the node's parent is a decision node and is the best branch, the box will
			// have its margin painted in red color. If it's not the best branch, the margin
			// won't be painted.
			if( ((DecisionNode)padre).getBestDecision() == source ) {
				txtIzq+= "<td width=10px bgcolor=red border=0></td>";
			}
			else {
				txtIzq+= "<td width=10px border=0></td>";					
			}

			// The value of the variable is written to the summary fox
			String estado= trimQuotes(fs.getState(source.getConfiguration().getValue(fs.getName()))); 
			txtIzq+= "<td align=center border=0>" + fs.getName() + "=" + estado +"</td>"; 
		}
		else {
			// DONE: ¿crear una excepcion particular para capturarla(evitar que un problema se propague a elvira?
			throw new DTEvaluatingException("Opcion no contemplada ["+source.getParent()+"]");
		}
	
		// Show the utility only when it's marked
		if( isUtilityDisplayed() ) {
			txtIzq+= "<td align=center border=0>U="+ precisionProxy.formatUtility(this,source.getUtility())+"</td>";
		}
		
		// HTML code completion is done: table closed
		txtIzq+="</table></html>";
			
		return txtIzq;
	}
	
	/**
	 * Removes the quotes (if any) that surrounds the given text string
	 * 
	 * @param source text
	 * @return text with the quotes removed
	 */
	public String trimQuotes(String source) {
		if( source.charAt(0)=='"' && source.charAt(source.length()-1)=='"' ) {
			return source.substring(1,source.length()-1);
		}
		
		return source;
	}
}
