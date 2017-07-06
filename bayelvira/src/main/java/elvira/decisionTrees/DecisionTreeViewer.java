package elvira.decisionTrees;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;

import elvira.gui.ElviraFrame;

/**
 * Esta clase implementa una representación gráfica para un arbol de decision dado.
 * Sigue el patrón Model-View-Controller, donde juega el papel de 'Controller', creando
 * unos objectos delegados para que se encarguen tanto del 'model', <code>DecisionTreeModel</code>,
 * como del 'view', <code>DecisionTreeCellRenderer</code> y <code>DecisionTreeUI</code> 
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.4
 *
 * version 0.1: era un JFrame, al ser 'top level window' podría ser un problema
 * 				de cara a la posterior incorporación a elvira. Se cambió a un JScrollPane
 * version 0.2: cambios en los menús e informar al TableModel de modificaciones en los nodos
 * 				debidas a cambios en las precisiones/titulos
 * version 0.3: cambio a un JTree, para facilitar la integracion en elvira (el JScrollPane
 * 				daba complicaba su integracion en el NetworkFrame
 * version 0.4: escalado del JTree mediante un factor de zoom 
 * 
 * version 1.0: verificada integración en entorno Elvira sin problemas
 * 				Eliminado metodo getTree por innecesario (desde que es un JTree)
 * 				Sobrecarga del metodo <code>getModel</code> para facilitar legibilidad del codigo 
 * 				Sobrecarga del metodo <code>getModel</code> para facilitar legibilidad del codigo 
 * 
 * DONE: considerar si delegar el tratamiento de eventos en otro objeto (ActionListener)
 */
public class DecisionTreeViewer extends JTree {
	/**
	 * Para evitar warning de serialización de este componente
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Árbol de decisión que se muestra en este componente 
	 */
	private AbstractCompositeNode decisionTree;
	
	/**
	 * Componente personalizado para dibujar cada fila del árbol
	 * Juega el rol de 'vista' en el patrón MVC que sigue esta clase
	 */
	private DecisionTreeCellRenderer cellRenderer;

	/**
	 * Factor de zoom con el que se quiere mostrar el arbol de decision
	 */
	private double zoomFactor= 1.0;
	
	/**
	 * Menú contextual
	 * DONE: considerar sacarlo a una clase exterior
	 */
	protected JPopupMenu popupMenu= new JPopupMenu();
	
	/**
	 * Submenu del popup: contendrá los items de personalización del componente
	 * (nivel de precision, uso de titulos/nombres como descripción)  
	 */
	protected JMenu menuCustomize= new JMenu();
	
	/**
	 * Opción de menu para expandir el item seleccionado completamente
	 */
	protected JMenuItem menuitemExpandAll = new JMenuItem();

	/**
	 * Opción de menu para colapsar el item seleccionado completamente
	 */
	protected JMenuItem menuitemCollapseAll = new JMenuItem();

	/**
	 * Opción de menu para expandir el item seleccionado un nivel adicional
	 */
	protected JMenuItem menuitemExpandOne = new JMenuItem();

	/**
	 * Opción de menu para colapsar el item seleccionado un nivel adicional
	 */
	protected JMenuItem menuitemCollapseOne = new JMenuItem();
	
	/**
	 * Item del menú que muestra la precision de probabilidad del nodo escogido. No es seleccionable
	 */
	protected JMenuItem menuitemShowChancePrecision = new JMenuItem();
	
	/**
	 * Incrementa la precision de la probabilidad para el nodo escogido 
	 */
	protected JMenuItem menuitemIncChancePrecision = new JMenuItem();
	
	/**
	 * Decrementa la precision de la probabilidad para el nodo escogido 
	 */
	protected JMenuItem menuitemDecChancePrecision = new JMenuItem();
	
	/**
	 * Deja al nodo escogido con la precision de probabilidad por defecto para todos los nodos
	 */
	protected JMenuItem menuitemResetChancePrecision = new JMenuItem();
	
	/**
	 * Elimina todas las particularizaciones existentes para las precisiones de probabilidades
	 */
	protected JMenuItem menuitemResetAllChancePrecisions = new JMenuItem();
	
	/**
	 * Item del menú que muestra la precision para la utilidad del nodo escogido. No es seleccionable
	 */
	protected JMenuItem menuitemShowUtilityPrecision = new JMenuItem();

	/**
	 * Incrementa la precision de la utilidad para el nodo escogido 
	 */
	protected JMenuItem menuitemIncUtilityPrecision = new JMenuItem();

	/**
	 * Decrementa la precision de la utilidad para el nodo escogido 
	 */
	protected JMenuItem menuitemDecUtilityPrecision = new JMenuItem();

	/**
	 * Deja al nodo escogido con la precision de utilidad por defecto para todos los nodos
	 */
	protected JMenuItem menuitemResetUtilityPrecision = new JMenuItem();
	
	/**
	 * Elimina todas las particularizaciones existentes para las precisiones de utilidades
	 */	
	protected JMenuItem menuitemResetAllUtilityPrecisions = new JMenuItem();

	/**
	 * Activa para el nodo escogido si su descripcion debe mostrar el titulo o la descripcion
	 */
	protected JCheckBoxMenuItem menuitemTitle = new JCheckBoxMenuItem();

	/**
	 * Elimina todas las particularizaciones existentes para las descripciones de nodos
	 */	
	protected JMenuItem menuitemResetDefaultDescription= new JMenuItem();

	/**
	 * Texto a mostrar en el item del menu que muestra la precision de las utilidades
	 */
	protected String menuitemShowUtilityPrecisionText;
	
	/**
	 * Texto a mostrar en el item del menu que muestra la precision de las probabilidades
	 */
	protected String menuitemShowChancePrecisionText;
	
	/** Crea una representación gráfica del arbol de decision a partir de la
	 * estructura de datos <code>AbstractCompositeNode</node>, utilizando el
	 * bundle de idioma apropiado para mostrar los mensajes de la interfaz
	 * 
	 * @param decisionTree Arbol de decision a mostrar
	 * @param bundle descripciones necesarias en el lenguaje seleccionado
	 * 
	 * <p>Cambios desde que el componente se considera estable (version 1.0)<p>
	 * Cambio 14/01/2006: eliminada llamada <code>setBackground(Color.white);</code> ya que
	 * es innecesaria desde que el componente hereda de JTree 
	 */
	public DecisionTreeViewer(AbstractCompositeNode decisionTree, ResourceBundle bundle) {
		this.decisionTree= decisionTree;

		/* Componente personalizado para acceder a la información en el Arbol de decision
		 * Juega el rol de 'model' en el patrón MVC que sigue esta clase
		 */
		setModel(new DecisionTreeModel(decisionTree));

		/* No se permiten selecciones multiples en este arbol: en principio se restringe
		 * como prevención, aunque seguramente no tenga ninguna influencia en la presentacion
		 * si finalmente se elimina este fragmento de codigo
		 */
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		/* Control de expansion/contraccion de nodos: Las filas del arbol que contienen a los
		 * nodos de decision y de azar no contienen ninguna información y puede inducir a error
		 * si se muestran sin su resumen asociado, es por esto que se hace necesario controlar
		 * los eventos de expansion para controlar que sus resúmenes se muestran siempre
		 */
	    addTreeExpansionListener(new TreeExpansionAdapter());
	    addTreeWillExpandListener(new TreeWillExpandAdapter());
		
		// Hace que el nodo raíz del arbol presente el boton de expansion/colapso
	    setShowsRootHandles(true);
	    
	    /* Como varios nodos del arbol pueden tener diferentes alturas (sobre todo los resumenes)
	     * se indica al componente que no utilice un tamaño prefijado, ya que provocaría que los
	     * nodos más grandes se vieran recortados
	     */
	    setRowHeight(0);
		
		// Personalizacion para el dibujo de los nodos del JTree
		cellRenderer= new DecisionTreeCellRenderer(); 
		setCellRenderer(cellRenderer);

		/* Componente personalizado para dibujar el componente
		 * Juega tambien el rol de 'vista' en el patrón MVC
		 */
		setUI(new DecisionTreeUI());	    
		
		// TODO: ver porque si no se colapsa inicialmente, solo muestra 2 nodos
		for (int i= getRowCount()-1; i >= 0; i--) {
			collapseRow(i);
		} 

		// Se añade el controlador de eventos del ratón
		addMouseListener(new TreeMouseAdapter());
		
		// Se inicializa el popup y su adaptador
		initPopup(bundle,new PopupAdapter());
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTree#getModel()
	 */
	public DecisionTreeModel getModel() {
		return (DecisionTreeModel) super.getModel();
	}
	
	/** Inicializa el menu popup asociado a este componente
	 * 
	 * @param bundle fichero con las descripciones en el idioma a utilizar
	 * @param adaptee adaptador que gestiona los eventos del popup
	 */
	void initPopup(ResourceBundle bundle, PopupAdapter adaptee) {
		// The JPopupMenu needs to know which component is poping it up
		popupMenu.setInvoker(this);
		
		// Se añaden al menu todas las opciones disponibles
		menuitemExpandAll.setText(ElviraFrame.localize(bundle,"PFC_DT.ExpandAll.label"));
		menuitemCollapseAll.setText(ElviraFrame.localize(bundle,"PFC_DT.CollapseAll.label"));
		menuitemExpandOne.setText(ElviraFrame.localize(bundle,"PFC_DT.ExpandOne.label"));
		menuitemCollapseOne.setText(ElviraFrame.localize(bundle,"PFC_DT.CollapseOne.label"));
		menuCustomize.setText(ElviraFrame.localize(bundle,"PFC_DT.Customize.label"));
		
		popupMenu.add(menuitemExpandAll);
		popupMenu.add(menuitemCollapseAll);
		popupMenu.add(menuitemExpandOne);
		popupMenu.add(menuitemCollapseOne);
		popupMenu.add(menuCustomize);
		
		// Textos de los menus
		menuitemShowUtilityPrecisionText=ElviraFrame.localize(bundle,"PFC_DT.UtilityPrecision.label");		
		menuitemShowUtilityPrecision.setText(menuitemShowUtilityPrecisionText);
		menuitemShowUtilityPrecision.setEnabled(false);
		menuitemIncUtilityPrecision.setText(ElviraFrame.localize(bundle,"PFC_DT.UtilityPrecision.Increase.label"));
		menuitemDecUtilityPrecision.setText(ElviraFrame.localize(bundle,"PFC_DT.UtilityPrecision.Decrease.label"));
		menuitemResetUtilityPrecision.setText(ElviraFrame.localize(bundle,"PFC_DT.UtilityPrecision.Reset.label"));
		menuitemResetAllUtilityPrecisions.setText(ElviraFrame.localize(bundle,"PFC_DT.UtilityPrecision.ResetAll.label"));
		
		menuitemShowChancePrecisionText=ElviraFrame.localize(bundle,"PFC_DT.ChancePrecision.label");
		menuitemShowChancePrecision.setText(menuitemShowChancePrecisionText);
		menuitemShowChancePrecision.setEnabled(false);
		menuitemIncChancePrecision.setText(ElviraFrame.localize(bundle,"PFC_DT.ChancePrecision.Increase.label"));
		menuitemDecChancePrecision.setText(ElviraFrame.localize(bundle,"PFC_DT.ChancePrecision.Decrease.label"));
		menuitemResetChancePrecision.setText(ElviraFrame.localize(bundle,"PFC_DT.ChancePrecision.Reset.label"));
		menuitemResetAllChancePrecisions.setText(ElviraFrame.localize(bundle,"PFC_DT.ChancePrecision.ResetAll.label"));

		menuitemTitle.setText(ElviraFrame.localize(bundle,"PFC_DT.Description.UseTitle.label"));
		menuitemTitle.setState(true);
		menuitemResetDefaultDescription.setText(ElviraFrame.localize(bundle,"PFC_DT.Description.ResetAll.label"));
		
		// Asociacion de menus con las acciones
		menuitemExpandAll.addActionListener(adaptee);
		menuitemExpandAll.setActionCommand("expand.all");
		menuitemCollapseAll.addActionListener(adaptee);
		menuitemCollapseAll.setActionCommand("collapse.all");
		menuitemExpandOne.addActionListener(adaptee);
		menuitemExpandOne.setActionCommand("expand.one");
		menuitemCollapseOne.addActionListener(adaptee);
		menuitemCollapseOne.setActionCommand("collapse.one");
		
	    menuitemIncUtilityPrecision.addActionListener(adaptee);
	    menuitemIncUtilityPrecision.setActionCommand("utility.increase");
	    menuitemDecUtilityPrecision.addActionListener(adaptee);
	    menuitemDecUtilityPrecision.setActionCommand("utility.decrease");
	    menuitemResetUtilityPrecision.addActionListener(adaptee);
	    menuitemResetUtilityPrecision.setActionCommand("utility.reset");
	    menuitemResetAllUtilityPrecisions.addActionListener(adaptee);
	    menuitemResetAllUtilityPrecisions.setActionCommand("utility.resetAll");
	    menuitemIncChancePrecision.addActionListener(adaptee);
	    menuitemIncChancePrecision.setActionCommand("chance.increase");
	    menuitemDecChancePrecision.addActionListener(adaptee);
	    menuitemDecChancePrecision.setActionCommand("chance.decrease");
	    menuitemResetChancePrecision.addActionListener(adaptee);
	    menuitemResetChancePrecision.setActionCommand("chance.reset");
	    menuitemResetAllChancePrecisions.addActionListener(adaptee);
	    menuitemResetAllChancePrecisions.setActionCommand("chance.resetAll");
	    
		menuitemTitle.addActionListener(adaptee);
		menuitemTitle.setActionCommand("description.change");
		menuitemResetDefaultDescription.addActionListener(adaptee);
		menuitemResetDefaultDescription.setActionCommand("description.reset");		
	}
	
	/** Configura el popup para que muestre las opciones disponibles cuando se
	 * lanza el menu contextual sobre un <code>SymmaryBox</code>
	 * 
	 * @param e el evento lanzado por el popup
	 * @param r el resumen sobre el que ha tenido lugar el evento
	 */
	protected void setPopupCustomItems(MouseEvent e, SummaryBox r) {
		// Se eliminan las opciones anteriores (pudieran ser de otro tipo de nodo)
		menuCustomize.removeAll();

		boolean mostrarItemDescripcion= false;
		int prec= cellRenderer.getPrecisionProxy().getUtilityPrecision(r);
		
		// Nodo del arbol de decision al que se refiere el cuadro de resumen 'r' a tratar
		AbstractNode source= r.getSource();
		
		/* Si es el resumen de un nodo de utilidad (o sv) y está colapsado, se mostrará
		 * el item de descripcion por titulo/nombre. Por otro lado, la precision a
		 * mostrar será la del nodo en cuestión
		 */
		if( source instanceof SuperValueNode || source instanceof UtilityNode ) {
			TreePath path = this.getPathForLocation(xx,yy); 
			if(this.isCollapsed(path)) {
				mostrarItemDescripcion= true;
				prec= cellRenderer.getPrecisionProxy().getUtilityPrecision(source);
			}
		}
		
		// Opciones de modificacion de las precisiones de utilidad
		menuitemShowUtilityPrecision.setText(menuitemShowUtilityPrecisionText+prec);
		menuCustomize.add(menuitemShowUtilityPrecision);
		menuCustomize.add(menuitemIncUtilityPrecision);
		menuCustomize.add(menuitemDecUtilityPrecision);
		menuCustomize.add(menuitemResetUtilityPrecision);
		menuCustomize.add(menuitemResetAllUtilityPrecisions);
		
		/* Si el padre del nodo es de azar, el menu contextual mostrado
		 * también se incluirán las opciones para modificar la precision de las probabilidades
		 */
		if( source.getParent() instanceof ChanceNode ) {
			menuCustomize.addSeparator();

		    prec= cellRenderer.getPrecisionProxy().getChancePrecision(r);
		    
			menuitemShowChancePrecision.setText(menuitemShowChancePrecisionText+prec);
			menuCustomize.add(menuitemShowChancePrecision);
			menuCustomize.add(menuitemIncChancePrecision);
			menuCustomize.add(menuitemDecChancePrecision);
			menuCustomize.add(menuitemResetChancePrecision);
			menuCustomize.add(menuitemResetAllChancePrecisions);
		}

		// Añadir, si procede, la opcion de mostrar la descripción por titulo o nombre 
		if( mostrarItemDescripcion ) {
			menuCustomize.addSeparator();
		    
		    boolean useTitle= cellRenderer.getDescriptionProxy().isUseTitle(source);
		    menuitemTitle.setState(useTitle);
		    
		    menuCustomize.add(menuitemTitle);
		    menuCustomize.add(menuitemResetDefaultDescription);			
		}
	}

	/** Configura el popup para que muestre las opciones disponibles cuando se
	 * lanza el menu contextual sobre un <code>UtilityNode</code>
	 * 
	 * @param e evento de raton que ha desencadenado este menu
	 * @param r nodo de utilidad sobre el que se ha producido el evento
	 */
	protected void setPopupCustomItems(MouseEvent e, UtilityNode r) {
		// Se eliminan las opciones anteriores (pudieran ser de otro tipo de nodo)
		menuCustomize.removeAll();

		// Precision de probabilidades a mostrar del nodo
		int prec= cellRenderer.getPrecisionProxy().getUtilityPrecision(r);
		
		// El menu contextual mostrado incluye las opciones para modificar la precision de las utilidades
		menuitemShowUtilityPrecision.setText(menuitemShowUtilityPrecisionText+prec);
		menuCustomize.add(menuitemShowUtilityPrecision);
		menuCustomize.add(menuitemIncUtilityPrecision);
		menuCustomize.add(menuitemDecUtilityPrecision);
		menuCustomize.add(menuitemResetUtilityPrecision);
		menuCustomize.add(menuitemResetAllUtilityPrecisions);

		menuCustomize.addSeparator();
	    
		// El menu contextual mostrado incluye las opciones para escoger que descripcion utilizar
	    boolean useTitle= cellRenderer.getDescriptionProxy().isUseTitle(r);
	    menuitemTitle.setState(useTitle);	    
	    menuCustomize.add(menuitemTitle);
	    menuCustomize.add(menuitemResetDefaultDescription);
	}

	/** Configura el popup para que muestre las opciones disponibles cuando se
	 * lanza el menu contextual sobre un <code>SuperValueNode</code>
	 * 
	 * @param e evento de raton que ha desencadenado este menu
	 * @param r nodo supervalor sobre el que se ha producido el evento
	 */
	protected void setPopupCustomItems(MouseEvent e, SuperValueNode r) {
		// Se eliminan las opciones anteriores (pudieran ser de otro tipo de nodo)
		menuCustomize.removeAll();

		// Precision de probabilidades a mostrar del nodo
		int prec= cellRenderer.getPrecisionProxy().getUtilityPrecision(r);
		
		// El menu contextual mostrado incluye las opciones para modificar la precision de las utilidades
		menuitemShowUtilityPrecision.setText(menuitemShowUtilityPrecisionText+prec);
		menuCustomize.add(menuitemShowUtilityPrecision);
		menuCustomize.add(menuitemIncUtilityPrecision);
		menuCustomize.add(menuitemDecUtilityPrecision);
		menuCustomize.add(menuitemResetUtilityPrecision);
		menuCustomize.add(menuitemResetAllUtilityPrecisions);

		menuCustomize.addSeparator();

		// El menu contextual mostrado incluye las opciones para escoger que descripcion utilizar
	    boolean useTitle= cellRenderer.getDescriptionProxy().isUseTitle(r);
	    menuitemTitle.setState(useTitle);	    	    
	    menuCustomize.add(menuitemTitle);
	    menuCustomize.add(menuitemResetDefaultDescription);
	}

	/** Configura el popup para que muestre las opciones disponibles cuando se
	 * lanza el menu contextual sobre un <code>ChanceNode</code>
	 * 
	 * @param e evento de raton que ha desencadenado este menu
	 * @param r nodo de azar sobre el que se ha producido el evento
	 */
	protected void setPopupCustomItems(MouseEvent e, ChanceNode r) {
		// Se eliminan las opciones anteriores (pudieran ser de otro tipo de nodo)
		menuCustomize.removeAll();

		// El menu contextual mostrado incluye las opciones para escoger que descripcion utilizar
	    boolean useTitle= cellRenderer.getDescriptionProxy().isUseTitle(r);
	    menuitemTitle.setState(useTitle);	    		
	    menuCustomize.add(menuitemTitle);
	    menuCustomize.add(menuitemResetDefaultDescription);
	}
	
	/** Configura el popup para que muestre las opciones disponibles cuando se
	 * lanza el menu contextual sobre un <code>DecisionNode</code>
	 * 
	 * @param e evento de raton que ha desencadenado este menu
	 * @param r nodo de decision sobre el que se ha producido el evento
	 */
	protected void setPopupCustomItems(MouseEvent e, DecisionNode r) {
		// Se eliminan las opciones anteriores (pudieran ser de otro tipo de nodo)
		menuCustomize.removeAll();

		// El menu contextual mostrado incluye las opciones para escoger que descripcion utilizar
	    boolean useTitle= cellRenderer.getDescriptionProxy().isUseTitle(r);
	    menuitemTitle.setState(useTitle);	    		
	    menuCustomize.add(menuitemTitle);
	    menuCustomize.add(menuitemResetDefaultDescription);
	}
	
	/**
	 * Coordenada x en la que se muestra el popup contextual 
	 */
	private int xx;
	
	/**
	 * Coordenada y en la que se muestra el popup contextual 
	 */
	private int yy;
	
	/** Lleva cabo la modificacion de la precision de probabilidades, llamando a los metodos adecuados
	 * según el comando de menu seleccionado
	 * 
	 * @param cmd Comando de menu a ejecutar
	 */
	protected void chanceActionPerformed(String cmd) {
		// Objeto seleccionado
		Object r= getPathForLocation(xx,yy).getLastPathComponent();
		
		if( cmd.equals("chance.increase") ) {
			// Incrementa la precision actual del nodo seleccionado
			int prec= cellRenderer.getPrecisionProxy().getChancePrecision(r)+1;
			cellRenderer.getPrecisionProxy().setChancePrecision(r,prec);
			
			// Indica al modelo que ha cambiado el valor de este nodo (es necesario
			// para que el arbol se actualice correctamente)
			getModel().fireNodesChanged(r);
		}
		else if( cmd.equals("chance.decrease") ) {
			// Decrementa la precision actual del nodo seleccionado
			int prec= cellRenderer.getPrecisionProxy().getChancePrecision(r)-1;
			if( prec < 0 ) {
				prec= 0;
			}
			cellRenderer.getPrecisionProxy().setChancePrecision(r,prec);

			// Indica al modelo que ha cambiado el valor de este nodo (es necesario
			// para que el arbol se actualice correctamente)
			getModel().fireNodesChanged(r);
		}
		else if( cmd.equals("chance.reset") ) {
			// Elimina cualquier particularizacion de la probabilidad para este nodo, haciendo que
			// en lo sucesivo se muestre con la precision por defecto seleccionada
			cellRenderer.getPrecisionProxy().removeChancePrecision(r);
			
			// Indica al modelo que ha cambiado el valor de este nodo (es necesario
			// para que el arbol se actualice correctamente)
			getModel().fireNodesChanged(r);
		}
		else if( cmd.equals("chance.resetAll") ) {
			// Elimina cualquier particularizacion de la probabilidad de todos los nodos del arbol, 
			// haciendo que en lo sucesivo se muestren con la precision seleccionada por defecto
			Iterator<Object> it= cellRenderer.getPrecisionProxy().removeAllChancePrecisions().iterator();
			
			// Recorre la coleccion recibida, informado al arbol que repinte los nodos afectados
			while( it.hasNext() ) {
				getModel().fireNodesChanged(it.next());
			}
		}
		else {
			throw new RuntimeException("command of chance menu unknow");			
		}
	}
	
	/** Lleva cabo la modificacion de la precision de utilidades, llamando a los metodos adecuados
	 * según el comando de menu seleccionado
	 * 
	 * @param cmd
	 * 
	 * TODO: parece posible refactorizarlo bastante más
	 */
	protected void utilityActionPerformed(String cmd) {
		TreePath path = this.getPathForLocation(xx,yy);
		Object r= path.getLastPathComponent();

		if( cmd.equals("utility.increase") ) {
			// Si se esta modificando la precision de un cuadro NO EXPANDIDO cuyo hijo
			// es un nodo de utilidad/supervalor, se debe actuar contra la precision
			// de dicho nodo, no contra la del cuadro
			//
			if( r instanceof SummaryBox ) {
				AbstractNode n= ((SummaryBox) r).getSource();
				if( n instanceof UtilityNode || n instanceof SuperValueNode ) {
					if( !this.isExpanded(path) ) {
						r= n;
					}
				}
			}
			
			int prec= cellRenderer.getPrecisionProxy().getUtilityPrecision(r)+1;
			cellRenderer.getPrecisionProxy().setUtilityPrecision(r,prec);
			getModel().fireNodesChanged(r);
		}
		else if( cmd.equals("utility.decrease") ) {
			// Si se esta modificando la precision de un cuadro NO EXPANDIDO cuyo hijo
			// es un nodo de utilidad/supervalor, se debe actuar contra la precision
			// de dicho nodo, no contra la del cuadro
			//
			if( r instanceof SummaryBox ) {
				AbstractNode n= ((SummaryBox) r).getSource();
				if( n instanceof UtilityNode || n instanceof SuperValueNode ) {
					if( !this.isExpanded(path) ) {
						r= n;
					}
				}
			}
			
			int prec= cellRenderer.getPrecisionProxy().getUtilityPrecision(r)-1;
			if( prec < 0 ) {
				prec= 0;
			}
			cellRenderer.getPrecisionProxy().setUtilityPrecision(r,prec);
			getModel().fireNodesChanged(r);			
		}
		else if( cmd.equals("utility.reset") ) {
			// Si se esta modificando la precision de un cuadro NO EXPANDIDO cuyo hijo
			// es un nodo de utilidad/supervalor, se debe actuar contra la precision
			// de dicho nodo, no contra la del cuadro
			//
			if( r instanceof SummaryBox ) {
				AbstractNode n= ((SummaryBox) r).getSource();
				if( n instanceof UtilityNode || n instanceof SuperValueNode ) {
					if( !this.isExpanded(path) ) {
						r= n;
					}
				}
			}
			
			cellRenderer.getPrecisionProxy().removeUtilityPrecision(r);
			getModel().fireNodesChanged(r);
		}
		else if( cmd.equals("utility.resetAll") ) {
			// Elimina cualquier particularizacion de la probabilidad de todos los nodos del arbol, 
			// haciendo que en lo sucesivo se muestren con la precision seleccionada por defecto
			Iterator<Object> it= cellRenderer.getPrecisionProxy().removeAllUtilityPrecisions().iterator();
			
			// Recorre la coleccion recibida, informado al arbol que repinte los nodos afectados
			while( it.hasNext() ) {
				getModel().fireNodesChanged(it.next());
			}
		}
		else {
			throw new RuntimeException("command of 'utility' menu unknow");			
		}
	}

	/** Lleva cabo cambios sobre la descripcion a utilizar, llamando a los metodos adecuados
	 * según el comando de menu seleccionado
	 * 
	 * @param cmd
	 * 
	 */
	protected void descriptionActionPerformed(String cmd) {
		Object r= getPathForLocation(xx,yy).getLastPathComponent();
		
		if( cmd.equals("description.change")) {
			// Si se esta modificando la descripción de un cuadro NO EXPANDIDO cuyo hijo
			// es un nodo de utilidad/supervalor, se debe actuar contra la descripcion
			// de dicho nodo, no contra la del cuadro
			//
			if( r instanceof SummaryBox ) {
				r= ((SummaryBox) r).getSource();
			}
			
			boolean useTitle= menuitemTitle.getState();
			cellRenderer.getDescriptionProxy().setUseTitle( (AbstractNode) r, useTitle );
			getModel().fireNodesChanged(r);
		}
		else if( cmd.equals("description.reset") ) {
			Iterator<Object> it= cellRenderer.getDescriptionProxy().removeAllUseTitle().iterator();
			while( it.hasNext() ) {
				getModel().fireNodesChanged(it.next());
			}
		}
		else {
			throw new RuntimeException("Command of 'description' menu unknow");			
		}		
	}

	/**
	 * Este metodo realiza la expansion completa del arbol
	 */
	public void expandAll() {
		for(int i=0; i<getRowCount();i++) {
			expandRow(i);
		}
	}

	/**
	 * Este metodo realiza la contraccion completa del arbol
	 */
	public void collapseAll() {
		for(int i=getRowCount()-1;i>=0;i--) {
			collapseRow(i);
		}
	}

	/** Este metodo recorre en amplitud el arbol de decision del modelo (esto es, incluyendo SummaryBoxes)
	 * hasta que se llega a un nivel donde hay al menos un nodo no expandido, expandiendo entonces todos
	 * los nodos no expandidos a ese nivel
	 * 
	 * @param tp TreePath del nodo cuyos niveles inferiores se quieren expandir
	 */
	protected void expandOne(TreePath tp) {
		Vector<TreePath> temp= new Vector<TreePath>();
		
		// Se recorre en amplitud cada nivel del arbol del modelo
		for( int i=0; i< getModel().getLevelProxy().getNumberOfLevels() && temp.isEmpty(); i++ ) {
			Iterator<TreePath> it= getModel().getLevelProxy().getLevel(i).iterator();
			while( it.hasNext() ) {
				TreePath th= it.next();
				
				// Solo interesa estudir los descendientes de 'tp'
				if( tp==th || !tp.isDescendant(th) ) {
					continue;
				}
				
				// Solo interesa tomar aquellos que no están expandidos 
				if( !isExpanded(th) ) {
					temp.add(th);
				}
			}
		}
		
		// Se expande cada uno de los nodos encontrados en el bucle anterior
		for (TreePath path : temp) {
			expandPath(path);
		}
	}
	
	/** Este metodo hace de enlace entre la llamada del menu y la funcion que realmente
	 * realiza la expansion
	 */
	public void expandOne() {
		// Nodo seleccionado por el popup
		TreePath tp= getPathForLocation(xx,yy);
		
		// Si se pulsa fuera del nodo, se supone que hace referencia al nodo raiz
		if( tp==null ) {
			tp= new TreePath(getModel().getRoot());
		}
		
		// Llama a la funcion que realmente hace la expansion una vez que se conoce sobre q nodo actuar
		expandOne(tp);
	}

	/** Este metodo recorre en amplitud el arbol de decision del modelo (esto es, incluyendo SummaryBoxes)
	 * desde las hojas hasta que llega a un nivel donde hay al menos un expandido, contrayendo entonces todos
	 * los nodos no contraidos a ese mismo nivel
	 * 
	 * @param tp
	 */
	public void collapseOne(TreePath tp) {
		Vector<TreePath> temp= new Vector<TreePath>();
		
		// Se recorre en amplitud cada nivel del arbol del modelo
		for( int i= getModel().getLevelProxy().getNumberOfLevels()-2; i>=0 && temp.isEmpty(); i-- ) {
			Iterator<TreePath> it= getModel().getLevelProxy().getLevel(i).iterator();
			while( it.hasNext() ) {
				TreePath th= it.next();
				// Solo interesa estudir los descendientes de 'tp'
				if( tp==th || !tp.isDescendant(th) ) {
					continue;
				}
				
				// Solo interesa tomar aquellos que están expandidos 
				if( isExpanded(th) ) {
					temp.add(th);
				}
			}
		}
		
		// Se colapsa cada uno de los nodos encontrados en el bucle anterior
		for (TreePath path : temp) {
			collapsePath(path);
		}
	}
	
	/** Este metodo hace de enlace entre la llamada del menu y la funcion que realmente
	 * realiza la expansion
	 */
	public void collapseOne() {
		// Nodo seleccionado por el popup
		TreePath tp= getPathForLocation(xx,yy);
		
		// Si se pulsa fuera del nodo, se supone que hace referencia al nodo raiz
		if( tp==null ) {
			tp= new TreePath(getModel().getRoot());
		}
		
		// Llama a la funcion que realmente hace la expansion una vez que se conoce sobre q nodo actuar
		collapseOne(tp);
	}

	/** Cambia el arbol de decision a mostrar por el componente
	 * 
	 * @param decisionTree arbol de decision a mostrar
	 */
	public void setDecisionTree(AbstractCompositeNode decisionTree) {
		this.decisionTree= decisionTree;

		/* Componente personalizado para acceder a la información en el Arbol de decision
		 * Juega el rol de 'model' en el patrón MVC que sigue esta clase
		 */
		setModel(new DecisionTreeModel(decisionTree));
		
		// TODO: ver porque si no se colapsa inicialmente, solo muestra 2 nodos
		for (int i=this.getRowCount()-1; i >= 0; i--) {
			collapseRow(i);
		} 		
	}
	
	/**
	 * Getter sobre el atributo <code>decisionTree</code>
	 *  
	 * @return Decision Tree displayed by this component 
	 */
	public AbstractCompositeNode getDecisionTree() {
		return decisionTree;
	}
	
	/** Fija el nivel de zoom a utilizar por el componente
	 * 
	 * @param zoom Factor de zoom con el que se quiere mostrar el arbol de decision en el componente
	 * 
	 * DONE: las barras de scroll no se actualizan correctamente al
	 * 		 modificar el factor de zoom (se utilizó el método revalidate en lugar de repaint)
	 * DONE: a veces las barras de scroll no llegan al final del recorrido si se utiliza el
	 * 		 mousewheel (o los botones de scroll: solo funcionaba correctamente si se arrastraba
	 * 		directamente la barra de scroll: se soluciono en el DecisionTreeFrame
	 */
	public void setZoomFactor(double zoom) {
		zoomFactor= zoom;
		
		/* Necesario para que el objeto contenedor donde se ubique este componente
		 * recalcule la extension de sus barras de scroll (de tenerlas)
		 */
		revalidate();
		
		/* OJO 06/01/2006: necesario añadirlo: si al hacer el zoomOut, el Viewport ya mostraba todo
		 * el árbol, no refrescaba la imagen y parecia que el zoom no se llevaba a caba 
		 */
		repaint();
	}
	
	/** Devuelve el nivel de zoom que tiene el componente
	 * 
	 * @return nivel de zoom utilizado
	 */
	public double getZoomFactor() {
		return zoomFactor;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		// Se borra el fondo del componente
		Rectangle r= g.getClipBounds();
		g.clearRect(r.x,r.y,r.width,r.height);
		
		// Es necesario utilizar los metodos del J2 para realizar el escalado
	    Graphics2D g2 = (Graphics2D)g;
	    
	    // Suavizado de los graficos al escalar
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	    
	    // Suavizado de los textos al escalar	    
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    
	    // Se realiza el escalado segun el factor de zoom indicado
	    AffineTransform oldTransform = g2.getTransform();
	    g2.scale(zoomFactor,zoomFactor);
	    
	    super.paintComponent(g);
	    
	    // Se reestablece el estado del canvas
	    g2.setTransform(oldTransform);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		Dimension d= super.getPreferredSize();
		
		// La dimension del componente debe estar escalada tambien
		d.height *= zoomFactor;
		d.width *= zoomFactor;
		
		return d;
	}
	
	/**
	 * @author Jorge Fernandez Suarez
	 * @version 0.2
	 * 
	 * version 0.1: era una clase externa
	 */
	class TreeExpansionAdapter implements TreeExpansionListener {
		
		/** Este metodo hace que se expanda automáticamente un nodo de icono (chance or decision)
		 * si su cuadro resumen es expandido
		 * 
		 * @param event evento producido por la expansion de un nodo
		 */
		public void treeExpanded(TreeExpansionEvent event) {
			TreePath tp1= event.getPath();
			Object tn1= tp1.getLastPathComponent();

			// Solo interesa controlar la expansion de los cuadros resumen 
			if( tn1 instanceof SummaryBox ) {
				AbstractNode n= ((SummaryBox) tn1).getSource();
				
				// ... y que el nodo asociado al resumen no sea de supervalor (solo los de decision y azar)
				if( !(n instanceof SuperValueNode) ) {
					for( int i=0; i< getModel().getChildCount(tn1); i++ ) {
						Object hijo = getModel().getChild(tn1,i);
						
						// Se expanden todas los hijos del nodo asociado 
						expandPath(tp1.pathByAddingChild(hijo));
					}
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
		 */
		public void treeCollapsed(TreeExpansionEvent event) {
			// Este evento no se utiliza por ahora en este proyecto
		}
	}
	
	/**
	 * @author Jorge Fernandez Suarez
	 * @version 0.2
	 * 
	 * version 0.1: era una clase externa
	 */
	class TreeWillExpandAdapter implements TreeWillExpandListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
		 */
		public void treeWillExpand(TreeExpansionEvent event) {
			// Este evento no se utiliza por ahora en este proyecto
		}
		
		/** Este metodo impide que un nodo de icono sea colapsado (siempre debe mostrarse si su
		 * cuadro resumen esta expandido
		 * 
		 * @param event
		 * @throws ExpandVetoException
		 */
		public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
			Object o= event.getPath().getLastPathComponent();

			// No se veta la expansion de los cuadros resumen ni de los nodos supervalor
			if( !(o instanceof SummaryBox || o instanceof SuperValueNode) ) {
				throw new ExpandVetoException(event);
			}
		}
	}
	
	/** Controla los eventos de raton que ocurren sobre el componente padre
	 * 
	 * @author Jorge Fernández Suarez
	 * @version 0.2
	 * 
	 * version 0.1: era una clase externa
	 */
	class TreeMouseAdapter extends MouseAdapter {
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}
		
		/**
		 * @param e
		 */
		private void showPopup(MouseEvent e) {
			// Se determina sobre que elemento hay que mostrar el popup 
			xx= e.getX();
			yy= e.getY();
			TreePath path = getPathForLocation(xx,yy);
			
			if( path != null ) {
				// El menu de personalizacion solo esta disponible sobre los nodos
				menuCustomize.setEnabled(true);
				
				/* Las opciones de menu son diferentes segun el tipo de nodo sobre el que
				 * se lanza el menu contextual
				 */
				Object node = path.getLastPathComponent();
				if( node instanceof SummaryBox ) {
					setPopupCustomItems(e, (SummaryBox) node);
				}
				else if( node instanceof UtilityNode ) {
					setPopupCustomItems(e, (UtilityNode) node);				
				}
				else if( node instanceof SuperValueNode ) {
					setPopupCustomItems(e, (SuperValueNode) node);				
				}
				else if( node instanceof ChanceNode ) {					
					setPopupCustomItems(e, (ChanceNode) node);
				}
				else if( node instanceof DecisionNode ) {
					setPopupCustomItems(e, (DecisionNode) node);
				}
			}
			else {
				// El menu de personalizacion no está disponible si no se hace el popup sobre un nodo
				menuCustomize.setEnabled(false);
			}

			// Mostrar el popup ya configurado en la posicion indicada
			popupMenu.show(e.getComponent(),xx,yy);
		}
	}
	
	/**
	 * @author Jorge Fernández Suarez
	 * @version 0.1
	 */
	class PopupAdapter implements ActionListener {
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae) {
			// El evento indica que comando realizar
			String cmd=ae.getActionCommand();
			
			if(cmd.startsWith("chance.")) {
				chanceActionPerformed(cmd);
			}
			else if(cmd.startsWith("utility.")) {
				utilityActionPerformed(cmd);
			}
			else if(cmd.startsWith("description.")) {
				descriptionActionPerformed(cmd);
			}		
			else if(cmd.equals("expand.all")) {
				expandAll();
			}
			else if(cmd.equals("collapse.all")) {
				collapseAll();
			}
			else if(cmd.equals("expand.one")) {
				expandOne();
			}
			else if(cmd.equals("collapse.one")) {
				collapseOne();
			}
			else {
				throw new RuntimeException("Menu command unknow");
			}
		}
	}
}
