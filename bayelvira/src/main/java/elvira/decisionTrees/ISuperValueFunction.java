package elvira.decisionTrees;

import java.util.Collection;

/**
 * Interfaz com�n para todas las funciones que sean aplicables
 * a un nodo de supervalor: por ahora se proporcionan implementaciones
 * de las funciones 'Suma' y 'Producto', pero se podr�an a�adir
 * f�cilmente otras funciones simplemente implementando este interfaz
 *
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 */
public interface ISuperValueFunction {
	/**
	 * Aplica la funcion a los valores recibidos
	 * 
	 * @param collection colecci�n a evaluar
	 * @return valor resultante de aplicar la funci�n
	 * @throws DTEvaluatingException 
	 *
	 */
	double apply(Collection<AbstractNode> collection) throws DTEvaluatingException;
	
	/**
	 * S�mbolo que representa esta funci�n, por ejemplo '+' para la suma, '*' para el producto...
	 * @return cadena de texto con el simbolo representativo de la funci�n 
	 */
	String getSymbol();
}
