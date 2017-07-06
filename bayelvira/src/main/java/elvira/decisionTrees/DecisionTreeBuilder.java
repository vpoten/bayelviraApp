package elvira.decisionTrees;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.parser.ParseException;
import elvira.potential.LogicalExpression;
import elvira.potential.Potential;
import elvira.potential.UtilityPotential;

/**
 * implements a singleton design pattern
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.2 03/10/2005
 * 
 * version 0.1 funciones de grupo hardcoded
 * version 0.2 incorpora proxy para funciones de grupo
 */
public class DecisionTreeBuilder {
	/**
	 * singleton instance (design pattern) 
	 */
	private static DecisionTreeBuilder singletonInstance;
	
	/**
	 * By default, bound all non-structural asymmetries: branches with zero probability
	 */
	private boolean podaAsimetrias= true;

	/**
	 * @param value
	 */
	public void setPodaAsimetrias(boolean value) {
		podaAsimetrias= value;
	}

	/**
	 * @return
	 */
	public boolean isPodaAsimetrias() {
		return podaAsimetrias;
	}
	
	/**
	 * By default, bound all structural asymmetries: branches with zero probability
	 */
	private boolean podaAsimetriasEstructurales= true;

	/** Setter del atributo para asimetrias estructurales
	 * @param value activa/desactiva la poda de asimetrias
	 */
	public void setPodaAsimetriasEstructurales(boolean value) {
		podaAsimetriasEstructurales= value;
	}

	/** Getter del atributo para asimetrias estructurales
	 * @return true si está activada la poda de asimetrias estructurales, false si no.
	 */
	public boolean isPodaAsimetriasEstructurales() {
		return podaAsimetriasEstructurales;
	}	
	
	/**
	 * Proxy de funciones supervalor (suma, multiplicación,...)
	 */
	private SuperValueFunctionProxy functionSVProxy= new SuperValueFunctionProxy();
	
	/**
	 * Constructor privado, tal y como se indica para el patron singleton para evitar
	 * instanciaciones no deseadas de esta clase
	 */
	private DecisionTreeBuilder() {
	}
	
	/** Asegura que solo existe un objeto singleton: si no está creado lo hace y si
	 * ya está devuelve la referencia a él
	 * 
	 * @return la única instancia creada de este objeto 
	 */
	public static DecisionTreeBuilder getInstance() {
		if(singletonInstance==null) {
			singletonInstance= new DecisionTreeBuilder();
		}
		
		return singletonInstance;
	}
	
	/**
	 * Main de prueba
	 * @param args
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void main(String args[]) throws ParseException, IOException {
		if (args.length < 1){
			System.out.println("Too few arguments. Arguments are: ElviraFile");
			System.exit(-1);
		}
		
		IDWithSVNodes diag=new IDWithSVNodes(args[0]);
		
		AbstractCompositeNode decisionTree;
		try {
			decisionTree = DecisionTreeBuilder.getInstance().buildDT(diag);
		} catch (DTBuildingException e) {
			// Error in decision tree building
			e.printStackTrace();
			return;
		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		JFrame frame;
		frame = new JFrame();
		frame.setTitle("Applet Frame");
		
		// if the window is closed, the application must be closed too
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		ResourceBundle bundle= ResourceBundle.getBundle("elvira/localize/DecisionTrees_sp");
		DecisionTreeViewer tree= new DecisionTreeViewer(decisionTree,bundle);
		
		// Add the created tree to the ScrollPane
		JScrollPane pane= new JScrollPane(tree);
		frame.add(pane, BorderLayout.CENTER);
		
		frame.setSize(400, 320);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
		frame.setVisible(true);		
	}
	
	/**
	 * Build the DT of the ID using a Shachter's Algorithm modification
	 * 
	 * @param diag DI del que se quiere generar el arbol de decision
	 * @return el arbol de decision creado
	 * @throws DTBuildingException
	 *  
	 * TODO: por ahora es destructivo... ¿hacer copia antes de operar?
	 */
	public AbstractCompositeNode buildDT(IDWithSVNodes diag) throws DTBuildingException {
		ArcReversalSV_ID2DT eval = new ArcReversalSV_ID2DT(diag);
		
		// initial checkout about the node.
		if( !eval.initialConditions() ) {
			throw new DTBuildingException("El DI no cumple las condiciones para calcular su AD");
		}
		
		// evalua el diagrama
		eval.evaluateDiagram(true,null);
		
		// Se crea el arbol de decision segun el orden de variables eliminadas por el ArcReversal 
		Configuration configuration= new Configuration( eval.getEliminatedVariables() );
		AbstractNode tree= buildTree( eval, configuration, 0 );
		
		// Si están activas las podas, podría ocurrir que se pode el arbol por completo
		if (tree==null) {
			throw new DTBuildingException("Arbol podado por completo");
		}
		
		// Devuelve el arbol creado
		return (AbstractCompositeNode) tree;
	}
	
	/** Este método se encarga de añadir los nodos de utilidad y supervalor al arbol de decision
	 * 
	 * @param eval algoritmo de evaluacion por ArcReversal
	 * @param node nodo del DI a expandir en el arbol
	 * @param configuration valores de las variables de dicho nodo en esta rama del arbol
	 * @return la estructura de nodos supervalor/utilidad creada
	 * @throws DTBuildingException 
	 * 
	 * @see elvira.decisionTrees.ArcReversalSV_ID2DT
	 * @see elvira.Node
	 * @see elvira.Configuration
	 */
	protected AbstractNode getSuperValueStructure( ArcReversalSV_ID2DT eval, Node node, Configuration configuration) throws DTBuildingException {
		/* La estructura de nodos SV original se pierde tras la evaluacion, por eso
		 * se necesita almacenar el ID original (quizá solo sus relaciones)
		 */
		Potential potential = eval.getOriginalID().getRelation(node).getValues();
		
		// Los nodos de utilidad son las hojas del arbol, terminan el proceso recursivo
		if(node.getKindOfNode()==Node.UTILITY) {
			return new UtilityNode(node, potential, configuration);
		}
		
		// Si no es un nodo de utilidad, DEBE ser un nodo supervalor
		if(node.getKindOfNode()!=Node.SUPER_VALUE) {
			throw new DTBuildingException("Se esperaba un nodo de tipo supervalor");
		}
		
		// Funcion de grupo de este nodo supervalor del diagrama de influencia
		UtilityPotential auxPotential= (UtilityPotential) potential;

		// Se busca que funcion de nodo supervalor corresponde
		ISuperValueFunction function= functionSVProxy.getSVFunction(auxPotential.getFunction().getClass());
		
		// Se crea el nodo supervalor del arbol de decision con la funcion adecuada
		SuperValueNode sv= new SuperValueNode(node, configuration, function);
		
		// Recursivamente se enlazan a este nodo supervalor los enlaces del nodo del DI
		NodeList padres= node.getParentNodes();
		for(int i=0;i<padres.size();i++) {
			Node hijo= padres.elementAt(i);
			sv.add(getSuperValueStructure(eval, hijo, configuration));
		}
		
		// Devuelve la estructura creada
		return sv;
	}
	
	/** Construye recursivamente el arbol de decision
	 * 
	 * @param eval algoritmo de orden creado por la evaluacion en ArcReversal
	 * @param configuration configuracion de las variables para este nodo del DI a expandir
	 * @param depth profundidad del arbol construido
	 * @return el arbol de decision construido
	 * @throws DTBuildingException 
	 * 
	 * @see elvira.decisionTrees.ArcReversalSVs_ID2DT
	 * @see elvira.Configuration
	 * 
	 * DONE: 07/01/2006 Encontrado problema con asimetrias no estructurales en nodos con todas sus ramas de prob 0%
	 * DONE: Podria devolver null, controlar como afecta a llamantes
	 * DONE: Distinguir entre el caso de que haya saltado una constraint -> no eliminar salvo q esté activo
	 * 		'podarChance': quiza sea interesante añadir un booleano 'podarAsimetriaEstructural'
	 * DONE: ¿es correcto el uso q se le esta dando a 'podaAsimetriasEstructurales'?
	 */
	protected AbstractNode buildTree( ArcReversalSV_ID2DT eval, Configuration configuration, int depth  ) throws DTBuildingException {
		/* Una vez tratados todos los nodos de azar y decision del arbol, según su orden de eliminación,
		 * se procede a construir el arbol con la estructura de nodos supervalor y/o nodos de utilidad
		 */
		if( depth == eval.getEliminatedVariables().size() ) {
			// Añade nodos utilidad/sv para cada una de las configuraciones posibles
			return getSuperValueStructure(eval, eval.getOriginalID().getTerminalValueNode(), configuration.duplicate());
		}
		
		// Se toma el siguiente nodo del DI a expandir en el arbol de decision siguiendo el orden del ArcReversal
		FiniteStates node= (FiniteStates) eval.getEliminatedVariables().get(depth);		
		AbstractCompositeNode arbol= null;
		
		if( node.getKindOfNode() == Node.CHANCE ) {
			// Se crea un nodo de azar con tantos hijos como estados posibles tenga
			Potential potential= eval.getDecisionTreeRelations().getRelationByNameOfNode(node.getName()).getValues();
			ChanceNode chanceNode= new ChanceNode(node,configuration.duplicate());
			int numStates= node.getNumStates();
			
			// Se crea una rama para cada estado
			for( int i=0; i<numStates; i++ ) {
				// Configuracion de la nueva rama 
				configuration.setValue(depth,i);
				AbstractNode hijo= null;
				double chance= 0;
				
				// Control de asimetrias, si hay alguna restriccion estructural se deja de expandir esta rama
				if (podaAsimetriasEstructurales) {
					if( applyConstraintsOnPotential(eval.getOriginalID(),potential,configuration) ) {
						// Cambio 21/06/2006
						chance= potential.getValue(configuration);
						if( chance > 0 || !podaAsimetrias ) {
							hijo= buildTree( eval, configuration, depth+1 );
						}
					}
				}
				else {
					// Cambio 21/06/2006
					chance= potential.getValue(configuration);
					if( chance > 0 || !podaAsimetrias ) {
						hijo= buildTree( eval, configuration, depth+1 );
					}
				}
				
				if(hijo!=null) {
					chanceNode.add(hijo, chance);
				}				
			}
			
			// Otro control de podas: si un nodo de azar solo tiene una rama, aporta poca informacion 
			if (chanceNode.getSize()==1 || !podaAsimetrias) {
				return (AbstractNode) chanceNode.getChild(0);
			}
			
			arbol= chanceNode;
			
		} else if( node.getKindOfNode() == Node.DECISION ) {
			/* TODO ¿Puede haber asimetrias estructurales sobre nodos de decision? */
			
			// Si es un nodo de decision, simplemente se expande cada posible decision que pueda tomar
			arbol= new DecisionNode(node, configuration.duplicate());
			int numStates= node.getNumStates();
			
			for( int i=0; i<numStates; i++ ) {
				configuration.setValue(depth,i);
				AbstractNode hijo= buildTree(eval, configuration, depth+1);
				
				// Se verifica que no haya sido podado
				if (hijo!=null) {
					arbol.add(hijo);
				}
			}
		} else {
			throw new DTBuildingException("Kind of node not allowed");
		}
		
		// Si un nodo composite no tiene hijos, debe eliminarse del arbol
		if (arbol.getSize()==0) {
			return null;
		}

		return arbol;
	}
	
	/** Comprueba si se cumplen las restricciones para un DI y configuracion dadas
	 *  
	 * @param diag diagrama de influencia
	 * @param potential potencial
	 * @param configuration configuracion a probar
	 * @return true si se cumplen las restricciones, false si no cumple alguna restriccion
	 * 
	 * @see elvira.inference.super_value.IDWithSVNodes
	 * @see elvira.potential.Potential
	 * @see elvira.Configuration
	 */
	protected boolean applyConstraintsOnPotential(IDWithSVNodes diag, Potential potential, Configuration configuration) {
		LogicalExpression logexp;
		Relation relation;
		NodeList common;
		NodeList varsInPot;
		
		// First at all, go over the list of relations and
		// consider the constraints
		for(int i=0; i < diag.getRelationList().size(); i++){
			relation=(Relation)diag.getRelationList().elementAt(i);
			
			// Check if it is a constraint
			if (relation.getKind() == Relation.CONSTRAINT){
				
				// First, check if both relations share some variables
				varsInPot=new NodeList(potential.getVariables());
				common=relation.getVariables().intersection(varsInPot);
				
				// If common is not empty, we have to check if this set
				// of variables is enough for the constraint to be applicable
				if (common.size() != 0){
					logexp=(LogicalExpression)(relation.getValues());
					
					// If is applicable, we have to combine the potentials
					// of both relations
					
					if (logexp.check(common.toVector())){
						if( !logexp.evaluate(configuration) ) {
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
}
