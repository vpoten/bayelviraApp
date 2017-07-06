package elvira.decisionTrees;

import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.SpinnerNumberModel;
import javax.swing.JScrollPane;
import elvira.Elvira;

/**
 * @author Jorge Fernandez Suarez
 * @version 0.1 26/12/2005
 * 
 * Versiones preliminares extendiendo JInternalFrame y NetworkFrame previamente
 * Tambien algun modificacion directa sobre la clase NetworkFrame
 */
public class DecisionTreeFrame extends JInternalFrame {
	/** Only for serialization purpose */
	private static final long serialVersionUID = 1L;
	
	/** Panel con barras de scroll que contiene el arbol de decision */
	JScrollPane scrollPane = new JScrollPane();
	
	/** Decision Tree component */
	private DecisionTreeViewer decisionTreeViewer;
	
	/** Metodo getter sobre el componente del arbol de decision
	 * 
	 * @return el arbol de decision
	 */
	public DecisionTreeViewer getDecisionTreeViewer() {
		return decisionTreeViewer;
	}
	
	/** Constructor: crea una ventana insertable en elvira que muestra el arbol de decision indicado
	 * 
	 * @param decisionTree Estructura de datos que contiene el arbol de decision a mostrar
	 * @param title titulo del marco
	 * @param bundle Fichero con las descripciones en el idioma seleccionado por el usuario
	 * 
	 * DONE: q reciba el nombre de la ventana como parametro
	 * DONE: q reciba el bundle como parametro
	 */
	public DecisionTreeFrame(AbstractCompositeNode decisionTree, String title, ResourceBundle bundle) {
		setTitle(title);
		
		// DONE: sacar al mediator y recibirlo como parametro
		decisionTreeViewer= new DecisionTreeViewer(decisionTree, bundle);
		
		// Se lee el valor por defecto a expandir cuando se muestra un arbol
		int maxLevels= decisionTreeViewer.getModel().getLevelProxy().getNumberOfLevels()-1;
		int seleccion= Math.min(ElviraGUIMediator.expandedLevelsByDefault,maxLevels);
		
		// Se crea el modelo de datos asociado al spinner de esta ventana
		state.spinnerModelNivelExpansion= new SpinnerNumberModel(seleccion,0,maxLevels,1); 
		
		// Propiedades de la ventana a mostrar
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		setClosable(true);
		getContentPane().setLayout(new BorderLayout(0,0));
		
		// Tamaño inicial
		setSize(393,274);
		
		// TODO: es un valor a dedo... tomar de la vista/viewport como scrollable
		// si no se le da valor, la barra de scroll no llega al tope cuando se
		// pasa de un determinado factor de zoom (ver setUnitIncrement en JScrollPane.ScrollBar)
		scrollPane.getVerticalScrollBar().setUnitIncrement(6);
		
		// Se configura el scrollpane, insertandolo en el marco
		getContentPane().add(scrollPane);
		
		// Se da tamaño al scrollpane y se le inserta el componente de visualizacion de arboles de decision
		scrollPane.setBounds(0,0,393,274);
		scrollPane.getViewport().add(decisionTreeViewer);

		// Se crea el adaptador que gestiona los eventos producidos sobre la ventana (activacion, desactivacion...)
		addInternalFrameListener(new SymInternalFrame());
	}
	
	/** Devuelve el titulo de esta ventana
	 * 
	 * @return el titulo de la ventana
	 */
	public String localize() {
		return title;
	}
	
	/** Trata los eventos de gestion de frames recibidos por esta ventana
	 * 
	 * @author Jorge Fernandez Suarez
	 * 
	 * @version 0.1
	 */
	class SymInternalFrame implements javax.swing.event.InternalFrameListener
	{
		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameListener#internalFrameClosed(javax.swing.event.InternalFrameEvent)
		 */
		public void internalFrameClosed(javax.swing.event.InternalFrameEvent event) {
			Object object = event.getSource();
			if (object == DecisionTreeFrame.this) {
				DecisionTreeFrame_internalFrameClosed(event);
			}
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameListener#internalFrameDeactivated(javax.swing.event.InternalFrameEvent)
		 */
		public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent event) {
			Object object = event.getSource();
			if (object == DecisionTreeFrame.this) {
				// Al desactivarse la ventana, deben cambiar los botones y menus activos de Elvira
				Elvira.getElviraFrame().disableDecisionTreeToolbars();
			}
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameListener#internalFrameDeiconified(javax.swing.event.InternalFrameEvent)
		 */
		public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent event) {
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameListener#internalFrameActivated(javax.swing.event.InternalFrameEvent)
		 */
		public void internalFrameActivated(javax.swing.event.InternalFrameEvent event) {
			if (Elvira.getElviraFrame() == null)
				return;
			
			Enumeration windowMenu = Elvira.getElviraFrame().getWindowGroup().getElements();
			boolean exit = false;
			
			// Marca en el menu de ventanas el item que representa a este frame
			while (windowMenu.hasMoreElements() && !exit) {
				JMenuItem windowItem = (JMenuItem) windowMenu.nextElement();
				if (windowItem.getText().equals(localize())) {
					if (!windowItem.isSelected()) {
						windowItem.setSelected(true);
					}
					exit = true;
				}
			}

			// Activa los botones apropiados
			Elvira.getElviraFrame().enableDecisionTreeToolbars(DecisionTreeFrame.this);
			
			// Copia en el interfaz de elvira el nivel de zoom de esta ventana
			Elvira.getElviraFrame().setZoom(state.zoomFactor);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameListener#internalFrameIconified(javax.swing.event.InternalFrameEvent)
		 */
		public void internalFrameIconified(javax.swing.event.InternalFrameEvent event) {
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameListener#internalFrameClosing(javax.swing.event.InternalFrameEvent)
		 */
		public void internalFrameClosing(javax.swing.event.InternalFrameEvent event) {
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.InternalFrameListener#internalFrameOpened(javax.swing.event.InternalFrameEvent)
		 */
		public void internalFrameOpened(javax.swing.event.InternalFrameEvent event) {
		}
	}
	
	/**
	 * @param event
	 */
	void DecisionTreeFrame_internalFrameClosed(javax.swing.event.InternalFrameEvent event)
	{
		Elvira.getElviraFrame().disableDecisionTreeToolbars();
		
		Elvira.getElviraFrame().getCurrentNetworkFrame();			
		Elvira.getElviraFrame().reestructWindowMenu(getTitle());		
		if (!Elvira.getElviraFrame().getWindowGroup().getElements().hasMoreElements()) {
			Elvira.getElviraFrame().enableMenusOpenFrames(false); 
		}
	}
	
	/** Metodo para incrementar el factor de zoom de esta ventana
	 * @param delta
	 */
	public void modifyZoomFactor(double delta) {
		double newZoomFactor= state.zoomFactor+delta;

		// Se verifica que el factor de zoom está dentro de los limites permitidos
		if(newZoomFactor>=0.1 && newZoomFactor<=5.0) {
			setZoomFactor(newZoomFactor);
			Elvira.getElviraFrame().setZoom(newZoomFactor);
		}
	}
	
	/** Metodo para modificar el factor de zoom con el que se muestra el arbol de esta ventana
	 * Modifica el estado de la ventana e informa al componente de visualizacion del nuevo grado de zoom
	 * 
	 * @param newZoomFactor
	 */
	public void setZoomFactor(double newZoomFactor) {
		if(newZoomFactor!=state.zoomFactor) {
			state.zoomFactor= newZoomFactor;
			decisionTreeViewer.setZoomFactor(newZoomFactor);
		}
	}
	
	/** Modifica la precision de utilidades para esta ventana
	 * @param index
	 */
	public void saveUtilityPrecisionIndex(int index) {
		state.precisionUtilidades= index;
	}
	
	/** Modifica la precision de probabilidades para esta ventana
	 * @param index
	 */
	public void saveChancePrecisionIndex(int index) {
		state.precisionProbabilidades= index;
	}
	
	/** Modifica que descripcion usar para las variables: titulo o nombre
	 * @param descriptionByTitle
	 */
	public void saveDescriptionByTitle(boolean descriptionByTitle) {
		state.descriptionByTitle= descriptionByTitle;
	}
	
	/**
	 * Estado particular del frame, guarda la informacion relevante necesaria para
	 * luego ser restaurada al interfaz de elvira
	 */
	private State state= new State();
	
	/** Devuelve el estado actual de la ventana
	 * 
	 * @return estado interno de la ventana
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * @author Jorge Fernández Suárez
	 *
	 * TODO: convertir todos los atributos a private y añadirles sus correspondientes get/set
	 */
	public class State {
		/**
		 * Nivel de zoom del componente asociado a esta ventana
		 */
		public double zoomFactor= 1.0;
		
		/**
		 * Indica si utilizar la descripcion por titulo o por nombre 
		 */
		public boolean descriptionByTitle= true;
		
		/**
		 * Precision de las utilidades
		 */
		public int precisionUtilidades= 1;
		
		/**
		 * Precision de las probabilidades
		 */
		public int precisionProbabilidades= 1;
		
		/**
		 * Niveles mostrados en el interfaz de Elvira
		 */
		SpinnerNumberModel spinnerModelNivelExpansion;
	}
}
