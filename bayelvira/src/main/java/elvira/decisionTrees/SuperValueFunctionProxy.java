package elvira.decisionTrees;

import java.util.HashMap;

import elvira.potential.ProductFunction;
import elvira.potential.SumFunction;

/**
 * @author Casa
 *
 */
public class SuperValueFunctionProxy {
	/**
	 * Diccionario con la asociacion funcion Elvira - funcion de grupo
	 */
	private HashMap<Class,ISuperValueFunction> mapFunction= new HashMap<Class,ISuperValueFunction>();

	public SuperValueFunctionProxy() {
		mapFunction.put(SumFunction.class,new SuperValueAddFunction());
		mapFunction.put(ProductFunction.class,new SuperValueMulFunction());
	}
	
	/**
	 * @param o
	 * @return
	 * @throws DTBuildingException 
	 */
	public ISuperValueFunction getSVFunction(Class o) throws DTBuildingException {
		if (!mapFunction.containsKey(o)) {
			throw new DTBuildingException("Unknow group function");
		}
		
		return mapFunction.get(o);
	}
}
