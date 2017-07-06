package elvira.gui.explication.policytree;

import java.util.HashMap;
import java.util.Vector;

import elvira.learning.policies.RuleNode;

/**
 * Clase para manejar las descripciones por defecto/individuales de los nodos
 * (por t�tulo o por descripci�n)
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public class DescriptionProxyPT {
	/**
	 * Diccionario con los nodos que tienen una descripci�n personalizada
	 */
	private HashMap<RuleNode,Boolean> mapDescription;
	
	/**
	 * Flag para indicar que se usen los titulos como descripcion por defecto
	 */
	private boolean useTitleByDefault;
	
	/**
	 * @param useTitleByDefault indica si se deben usar por defecto los titulos o los nombres
	 */
	public DescriptionProxyPT( boolean useTitleByDefault ) {
		this.useTitleByDefault= useTitleByDefault;
		
		mapDescription= new HashMap<RuleNode,Boolean>();
	}
	
	/**
	 * @param useTitleByDefault Cambia la descripcion a utilizar por defecto
	 */
	public void setUseTitleByDefault(boolean useTitleByDefault) {
		this.useTitleByDefault= useTitleByDefault;
	}

	/**
	 * Personaliza la descripcion de un nodo
	 * @param n nodo a personalizar
	 * @param useTitle indica si su descripcion debe usar el titulo o su nombre
	 */
	public void setUseTitle(RuleNode n, boolean useTitle) {
		// Si ya existia, se sobreescribe
		mapDescription.put(n, new Boolean(useTitle));
	}

	/**
	 * Recupera el indicador de uso de titulos o nombres para un nodo dado 
	 * @param n nodo
	 * @return uso de titulos o de nombres como descripcion para este nodo
	 */
	public boolean isUseTitle(RuleNode n) {
		boolean useTitle= useTitleByDefault;
		
		if( mapDescription.containsKey(n) ) {
			useTitle= mapDescription.get(n).booleanValue();
		}
		
		return useTitle;
	}
	
	/**
	 * Elimina la particularizacion de este nodo para su descripcion
	 * @param n nodo cuya descripcion particular quiere eliminarse
	 */
	public void removeUseTitle(RuleNode n) {
		mapDescription.remove(n);
	}

	/**
	 * Elimina todas las particularizaciones de descripciones existentes
	 * @return Vector con todos los nodos que estaban particularizados
	 */
	public Vector<Object> removeAllUseTitle() {
		Vector<Object> v= new Vector<Object>();
		v.addAll(mapDescription.keySet());
		
		mapDescription.clear();
		return v;
	}
	
	/**
	 * Devuelve la descripcion asociada para un nodo dado (titulo o nombre)
	 * @param n nodo
	 * @return descripcion
	 */
	public String getDescription(RuleNode n) {
		String title= n.getVariable().getTitle();
		String name= n.getVariable().getName();
		
		// A veces ocurre que un nodo tiene titulo pero no nombre, de ah�
		// que se hagan las comprobaciones siguientes
		if( title.length()==0 ) {
			// Si no tiene titulo, deber�a tener nombre: por si acaso se contempla dicha posibilidad
			// devolviendo el texto 'unnamed mode'
			return name.length()>0 ? name : "unnamed node";
		}
		else if( name.length()==0 ) {
			return title;
		}
		
		return isUseTitle(n) ? title : name;
	}	
}
