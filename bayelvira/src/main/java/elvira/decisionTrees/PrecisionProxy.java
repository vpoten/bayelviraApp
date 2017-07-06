package elvira.decisionTrees;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class provides custom and default precision for the existing nodes
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public class PrecisionProxy {
	/**
	 * Precision dictionary for nodes with custom utility values
	 */
	private HashMap<Object,Integer> mapUtilityPrecision;
	
	/**
	 * Precision dictionary for nodes with custom chance values
	 */
	private HashMap<Object,Integer> mapChancePrecision;

	/**
	 * Default utility precision, it's applied to every node without custom utility precision
	 */
	private int defaultUtilityPrecision;

	/**
	 * Default chance precision, it's applied to every node without custom chance precision
	 */
	private int defaultChancePrecision;
	
	/**
	 * Creates the precision pool. Defaults precisions are given.
	 *  
	 * @param utilityPrecision Default utility precision to use
	 * @param chancePrecision Default chance precision to use
	 */
	public PrecisionProxy( int utilityPrecision, int chancePrecision ) {
		setDefaultUtilityPrecision(utilityPrecision);
		setDefaultChancePrecision(chancePrecision);
		
		mapUtilityPrecision= new HashMap<Object,Integer>();
		mapChancePrecision= new HashMap<Object,Integer>();
	}
		
	/**
	 * Setter for default utility precision attribute
	 * @param precision new default utility precision
	 */
	public void setDefaultUtilityPrecision(int precision) {
		defaultUtilityPrecision= precision;
	}

	/**
	 * Getter of default utility precision attribute
	 * @return default utility precision to use
	 */
	public int getDefaultUtilityPrecision() {
		return defaultUtilityPrecision;
	}
	
	/**
	 * Setter for default chance precision attribute
	 * @param precision new default chance precision
	 */
	public void setDefaultChancePrecision(int precision) {
		defaultChancePrecision= precision;
	}

	/**
	 * Getter of default chance precision attribute
	 * @return default chance precision to use
	 */
	public int getDefaultChancePrecision() {
		return defaultChancePrecision;
	}
	
	/**
	 * @param n nodo a personalizar
	 * @param precision precisión de utilidades personalizada
	 */
	public void setUtilityPrecision(Object n,int precision) {
		// Si ya existia, se sobreescribe
		mapUtilityPrecision.put(n, new Integer(precision));
	}

	/**
	 * @param n nodo a personalizar
	 * @param precision precisión de probabilidades personalizada
	 */
	public void setChancePrecision(Object n,int precision) {
		// Si ya existia, se sobreescribe
		mapChancePrecision.put(n, new Integer(precision));
	}
	
	/**
	 * @param n nodo del arbol de decision
	 * @return precision personalizado si la tiene o el valor por defecto si no
	 */
	public int getUtilityPrecision(Object n) {
		if( mapUtilityPrecision.containsKey(n) ) {
			return mapUtilityPrecision.get(n).intValue();
		}
		
		return getDefaultUtilityPrecision();
	}
	
	/**
	 * Elimina la personalizacion de la precision de la utilidad para un nodo dado
	 * @param n nodo con precisión personalizada
	 */
	public void removeUtilityPrecision(Object n) {
		mapUtilityPrecision.remove(n);
	}

	/**
	 * Elimina todas las personalizaciones de utilidades existentes
	 * @return vector con los elementos que tenían una personalización de la precision
	 * de sus utilidades
	 */
	public Vector<Object> removeAllUtilityPrecisions() {
		Vector<Object> v= new Vector<Object>();
		v.addAll(mapUtilityPrecision.keySet());
		
		mapUtilityPrecision.clear();
		return v;
	}
	
	/**
	 * Devuelve la precision aplicable a las probabilidades de un nodo
	 * @param n nodo del arbol de decision
	 * @return precision personalizada si la tiene o el valor por defecto si no
	 */
	public int getChancePrecision(Object n) {
		if( mapChancePrecision.containsKey(n) ) {
			return mapChancePrecision.get(n).intValue();
		}
		
		return getDefaultChancePrecision();
	}

	/**
	 * Elimina la personalizacion de la precision de la probabilidad para un nodo dado
	 * @param n nodo con precisión personalizada
	 */
	public void removeChancePrecision(Object n) {
		mapChancePrecision.remove(n);
	}

	/**
	 * Elimina todas las personalizaciones de probabilidades existentes
	 * @return vector con los elementos que tenían una personalización de la precision
	 * de sus probabilidades
	 */
	public Vector<Object> removeAllChancePrecisions() {
		Vector<Object> v= new Vector<Object>();
		v.addAll(mapChancePrecision.keySet());
		
		mapChancePrecision.clear();
		return v;
	}
	
	/**
	 * Devuelve una cadena de texto con la utilidad asociada al nodo indicado
	 * debidamente formateada con la precisión que le sea aplicable
	 * 
	 * @param n nodo
	 * @return cadena con la utilidad del nodo debidamente formateada
	 * @throws DTEvaluatingException 
	 */
	public String formatUtility(AbstractNode n) throws DTEvaluatingException {
		NumberFormat nf = NumberFormat.getInstance();
		
		int precision= getUtilityPrecision(n);
		
		nf.setMaximumFractionDigits( precision );
		nf.setMinimumFractionDigits( precision );
		
		return nf.format(n.getUtility());
	}

	/**
	 * Devuelve una cadena de texto con el valor de utilidad recibido 
	 * formateado segun la precision que tenga el cuadro de resumen
	 * 
	 * @param n cuadro de resumen
	 * @param valor valor a formatear
	 * @return cadena formateada segun la precision que tenga el nodo
	 */
	public String formatUtility(SummaryBox n, double valor) {
		NumberFormat nf = NumberFormat.getInstance();
		
		int precision= getUtilityPrecision(n);
		
		nf.setMaximumFractionDigits( precision );
		nf.setMinimumFractionDigits( precision );
		
		return nf.format(valor);
	}

	/**
	 * Devuelve una cadena de texto con el valor de probabilidad recibido 
	 * formateado segun la precision que tenga el cuadro de resumen
	 * 
	 * @param n cuadro de resumen
	 * @param valor valor a formatear
	 * @return cadena formateada segun la precision que tenga el nodo
	 */
	public String formatChance(SummaryBox n,double valor) {
		NumberFormat nf = NumberFormat.getInstance();
		
		int precision= getChancePrecision(n);
		
		nf.setMaximumFractionDigits( precision );
		nf.setMinimumFractionDigits( precision );
		
		return nf.format(valor);
	}
}
