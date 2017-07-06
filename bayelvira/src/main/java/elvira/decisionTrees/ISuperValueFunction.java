package elvira.decisionTrees;

import java.util.Collection;

/**
 * Interfaz común para todas las funciones que sean aplicables
 * a un nodo de supervalor: por ahora se proporcionan implementaciones
 * de las funciones 'Suma' y 'Producto', pero se podrían añadir
 * fácilmente otras funciones simplemente implementando este interfaz
 *
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public interface ISuperValueFunction {
	/**
	 * Aplica la funcion a los valores recibidos
	 * 
	 * @param collection colección a evaluar
	 * @return valor resultante de aplicar la función
	 * @throws DTEvaluatingException 
	 *
	 */
	double apply(Collection<AbstractNode> collection) throws DTEvaluatingException;
	
	/**
	 * Símbolo que representa esta función, por ejemplo '+' para la suma, '*' para el producto...
	 * @return cadena de texto con el simbolo representativo de la función 
	 */
	String getSymbol();
}
