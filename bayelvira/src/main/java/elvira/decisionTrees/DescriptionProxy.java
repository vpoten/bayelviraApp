package elvira.decisionTrees;

import java.util.HashMap;
import java.util.Vector;

/**
 * Clase para manejar las descripciones por defecto/individuales de los nodos
 * (por título o por descripción)
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public class DescriptionProxy {
	/**
	 * Diccionario con los nodos que tienen una descripción personalizada
	 */
	private HashMap<AbstractNode,Boolean> mapDescription;
	
	/**
	 * Flag para indicar que se usen los titulos como descripcion por defecto
	 */
	private boolean useTitleByDefault;
	
	/**
	 * @param useTitleByDefault indica si se deben usar por defecto los titulos o los nombres
	 */
	public DescriptionProxy( boolean useTitleByDefault ) {
		this.useTitleByDefault= useTitleByDefault;
		
		mapDescription= new HashMap<AbstractNode,Boolean>();
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
	public void setUseTitle(AbstractNode n, boolean useTitle) {
		// Si ya existia, se sobreescribe
		mapDescription.put(n, new Boolean(useTitle));
	}

	/**
	 * Recupera el indicador de uso de titulos o nombres para un nodo dado 
	 * @param n nodo
	 * @return uso de titulos o de nombres como descripcion para este nodo
	 */
	public boolean isUseTitle(AbstractNode n) {
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
	public void removeUseTitle(AbstractNode n) {
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
	public String getDescription(AbstractNode n) {
		String title= n.getVariable().getTitle();
		String name= n.getVariable().getName();
		
		// A veces ocurre que un nodo tiene titulo pero no nombre, de ahí
		// que se hagan las comprobaciones siguientes
		if( title.length()==0 ) {
			// Si no tiene titulo, debería tener nombre: por si acaso se contempla dicha posibilidad
			// devolviendo el texto 'unnamed mode'
			return name.length()>0 ? name : "unnamed node";
		}
		else if( name.length()==0 ) {
			return title;
		}
		
		return isUseTitle(n) ? title : name;
	}	
}
