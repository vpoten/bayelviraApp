package elvira.gui.explication.policytree;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.HashMap;
import elvira.Node;
import elvira.learning.policies.RuleNode;

/**
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.2
 * 
 * version 0.2: Diferentes cambios desde la primera version
 * - Introduccion de pools de iconos (utilizando la factoria de iconos creada)
 * - proxys para las precisiones y descripcion de los nodos
 * - Utilizacion del polimorfismo en el codigo del getTreeCellRendererComponent
 * 
 * Razones por las que se extiende de JPanel y no de BasicTreeCellRenderer
 * - Por requisitos del proyecto, es necesario contar con la posibilidad de colocar
 *   informacion a ambos lados del icono: se ha realizado con 2 JLabel.
 */
public class PolicyTreeCellRenderer extends JPanel implements TreeCellRenderer {
	/**
	 * Identificador de serialización; se ha añadido para evitar el warning correspondiente
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Texto del cuadro de resumen / icono correspondiente a una variable 
	 */
	private JLabel label_izq= new JLabel();
	
	/**
	 * Texto con la utilidad para los nodos de utilidad/supervalor 
	 */
	private JLabel label_der= new JLabel();
	
	/**
	 * Pool de iconos con el titulo de la variable utilizada en un nodo 
	 */
	private HashMap<Node,Icon> iconosTitle= new HashMap<Node,Icon>();
	
	/**
	 * Pool de iconos con el nombre de la variable utilizada en un nodo 
	 */
	private HashMap<Node,Icon> iconosName= new HashMap<Node,Icon>();
	
	/**
	 * Fuente utilizada para el texto de los iconos de las variables
	 */
	private Font fontNodos;
	
		
	
	/**
	 * Proxy de descripciones: personalización de cada nodo, indica si un nodo
	 * determinado debe mostrar el titulo o su nombre
	 */
	private DescriptionProxyPT descriptionProxy;

	/** Getter para el objeto de control de descripciones
	 * 
	 * @return el proxy de descripciones
	 */
	public DescriptionProxyPT getDescriptionProxy() {
		return descriptionProxy;
	}
	
	/** Constructor: maqueta los JLabel necesarios sobre el JPanel base
	 * @param font fuente a utilizar en los textos e iconos
	 * 
	 * DONE: crear un constructor q permita indicar la fuente a utilizar,
	 */
	public PolicyTreeCellRenderer(Font font) {
		super( new BorderLayout() );
		
		this.add(label_izq, BorderLayout.WEST);
		this.add(label_der, BorderLayout.CENTER);
		
		label_izq.setHorizontalAlignment(JLabel.CENTER);
		label_izq.setHorizontalTextPosition(JLabel.LEADING);
		label_der.setHorizontalAlignment(JLabel.RIGHT);
		
		setBackground(Color.white);
		
		fontNodos= font;
		
			
		// Por defecto, se utilizan los titulos como descripcion de cada nodo
		descriptionProxy= new DescriptionProxyPT(true);		
	}
	
	/** Constructor por defecto
	 * DONE: crear un atributo para el color de fondo del componente (innecesario, modificable directamente) 
	 */
	public PolicyTreeCellRenderer() {
		this(new Font("Helvetica", Font.BOLD, 15));
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 * 
	 * DONE: lanza una RuntimeException si el objecto enviado no está entre los tratados... quizá
	 * sea interesante crear un tipo de excepcion particular de este renderer y capturarlo para
	 * no propagar ningún problema a Elvira (no se ha hecho finalmente ya que esto se considera un
	 * error de programación y por lo tanto debe provocar una RuntimeException
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected,
			boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		
		label_izq.setText(null);
		label_der.setText(null);
		
		Component retCode= null;
		
		// Cada tipo de nodo tiene su presentacion particular
		if( value instanceof ValueBox ) {
			retCode= getTreeCellRendererComponent(tree,(ValueBox) value,selected,expanded,leaf,row,hasFocus);
		}
		else if( value instanceof RuleNode ) {			
			retCode= getTreeCellRendererComponent(tree,(RuleNode) value,selected,expanded,leaf,row,hasFocus);
		}
		else {
			throw new RuntimeException("Kind of node not allowed");
		}
		
		return retCode;
	}
	
	/**
	 * Dibuja el nodo del JTree cuando éste quiere representar los datos de un cuadro resumen
	 * En principio sólo dibuja un cuadro de texto, salvo que no esté expandido y tenga como
	 * asociado un nodo de utilidad o supervalor. En ese caso mostrará el icono a la derecha
	 * del cuadro de texto
	 * 
	 * @param tree Componente que está representando el árbol de decision
	 * @param r Cuadro de resumen a componer
	 * @param selected indica si el nodo está seleccionado (por ahora no se usa)
	 * @param expanded
	 * @param leaf indica si es una hoja (no se usa y además es imposible que un cuadro resumen sea a la vez hoja
	 * @param row fila del componente en la que se encuentra el nodo a representar
	 * @param hasFocus si el nodo tiene el foco (modo edición?) por ahora no se usa
	 * 
	 * @return una referencia al propio objeto DecisionTreeCellRenderer
	 */
	public Component getTreeCellRendererComponent(JTree tree, ValueBox r,
			boolean selected,
			boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		
		label_izq.setIcon(null);
		//r.setUtilityDisplayed(true);
		
		// Los cuadros resumen únicamente pueden tener un hijo: la variable
		RuleNode hijo= (RuleNode) tree.getModel().getChild(r,0);
		//boolean tipoUtilidad= hijo instanceof SuperValueNode || hijo instanceof UtilityNode;
		
		// Si el cuadro no está expandido y es el resumen de un nodo de utilidad, se
		// dibuja dicho nodo a la derecha del cuadro de resumen, omitiendo de éste la
		// utilidad pues ya se muestra en el propio icono dibujado a la derecha
	/*	if( tipoUtilidad && !expanded ) {
			r.setUtilityDisplayed(false);
			getTreeCellRendererComponent(tree,hijo,selected,expanded,leaf,row,hasFocus);			
		}*/
		
		String html;
		try {
			html = r.getHTML();
		} catch (PTEvaluatingException e) {
			html= "Decision tree evaluating error";
		}
		
		// Se dibuja el HTML con la informacion del cuadro resumen
		label_izq.setText(html);
		
		return this;
	}
	
	/**
	 * Dibuja el nodo del JTree cuando éste quiere representar los datos de un nodo que no es un cuadro resumen
	 * 
	 * @param tree Componente que está representando el árbol de decision
	 * @param r Nodo del arbol de decision a componer
	 * @param selected indica si el nodo está seleccionado (por ahora no se usa)
	 * @param expanded
	 * @param leaf indica si es una hoja (no se usa y además es imposible que un cuadro resumen sea a la vez hoja
	 * @param row fila del componente en la que se encuentra el nodo a representar
	 * @param hasFocus si el nodo tiene el foco (modo edición?) por ahora no se usa
	 * 
	 * @return una referencia al propio objeto DecisionTreeCellRenderer
	 */
	public Component getTreeCellRendererComponent(JTree tree, RuleNode n,
			boolean selected,
			boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		
		// Segun esté activo el uso de titulos o de nombres, se tienen que crear iconos diferentes
		HashMap<Node,Icon> iconos= iconosTitle;
		if( !descriptionProxy.isUseTitle(n) ) {
			iconos= iconosName;
		}
		
		// Ver si el icono asociado a este nodo ya se ha creado anteriormente
		if( iconos.containsKey(n.getVariable()) ) {
			label_izq.setIcon( iconos.get(n.getVariable()) );
		}
		else {
			// No se ha creado, luego vemos cual es la descripcion a utilizar por el icono
			String description= descriptionProxy.getDescription(n);
			
						
			// Crea el icono y lo inserta en el pool de iconos para su posterior reutilización 
			Icon icon= createNodeIcon(n,description);
			iconos.put(n.getVariable(),icon);
			label_izq.setIcon(icon);
		}
		
		// Si es un nodo de utilidad/sv, debe mostrar su utilidad en la etiqueta a la derecha del icono
		/*if( n instanceof SuperValueNode || n instanceof UtilityNode ) {
			String utilityValue;
			try {
				utilityValue = precisionProxy.formatUtility(n);
			} catch (PTEvaluatingException e) {
				utilityValue= "Decision tree evaluating error";
			}
			label_der.setText(" U="+ utilityValue);
		}*/
		
		return this;
	}
	
	/** Crea un icono con la descripcion indicada
	 * 
	 * @param n nodo fuente
	 * @param description descripcion a incluir en el icono
	 * @return el icono creado
	 */
	protected Icon createNodeIcon(RuleNode n, String description) {
		Icon icon= null;
		Integer kindOfNode;
		
		kindOfNode = n.getVariable().getKindOfNode();
		
		// Segun el tipo de nodo que sea, se crea el correspondiente icono
		switch (kindOfNode){
		case Node.CHANCE:
			icon= IconFactoryPT.createChanceIcon(description,fontNodos);
			break;
		case Node.DECISION:
			icon= IconFactoryPT.createDecisionIcon(description,fontNodos);
			break;
		default:
			throw new RuntimeException("Unknow RuleNode subclass");
		}
		
		return icon;
	}
}