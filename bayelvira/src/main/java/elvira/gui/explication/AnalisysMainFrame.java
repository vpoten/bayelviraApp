package elvira.gui.explication;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;
import java.util.ResourceBundle;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import elvira.Bnet;
import elvira.Configuration;
import elvira.Elvira;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.gui.InferencePanel;
import elvira.gui.NetworkFrame;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;

import javax.swing.JList;
import javax.swing.JScrollBar;



public class AnalisysMainFrame extends JDialog {

	
	private static final long serialVersionUID = 1L;
		
	private InferencePanel infpanel;

    private Case cedit;
    
    private Object parametro_aux;
    
    private Vector variables = new Vector();  //  @jve:decl-index=0:
    
    private Vector estados = new Vector();  //  @jve:decl-index=0:
    
    private Relation relacion;
    
    private Configuration conf;
    
    private Node node;
    
    private Node nodo; //nodo del parametro a estudiar
        
    private CasesList casesl;
    
    private Bnet bnet;
    
    private double valor=0.0;
    
    private String numero_bin="";  //  @jve:decl-index=0:
    
	private JPanel jContentPane = null;
	
	private JPanel Panel2 = null;
	
	private JTextField  jTextfield= null;

	private JLabel jLabel1 = null;

	private JComboBox jComboBox = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel5 = null;

	private JLabel jLabel6 = null;

	private JPanel Panel1 = null;

	private JButton jButton1 = null;

	private JLabel jLabel = null;
	
	private JScrollPane jScrollParametro = null;

	private JProgressBar jProgressBar = null;

	private JLabel jLabel7 = null;

	private JLabel jLabel8 = null;

	private JLabel jLabel9 = null;

	private JLabel jLabel10 = null;

	private JLabel jLabel11 = null;

	private JLabel jLabel12 = null;

	private JLabel jLabel13 = null;

	private JButton jButton2 = null;

	private JLabel jLabel14 = null;

	private JButton jButton = null;

	private JLabel jLabel15 = null;

	private JLabel jLabel16 = null;

	private JButton jButton3 = null;

	private JButton jButton5 = null;

	private JButton jButton6 = null;

	private JScrollPane jScrollPane1 = null;

	private JScrollPane jScrollPane2 = null;

	private JButton jButton7 = null;

	private JLabel jLabel17 = null;

	private JLabel jLabel18 = null;
	
	private NetworkFrame frame2;
	
	private JTable tabla_datos=null;
	
	private JTable tabla_interes=null;

	private JList jListParametro = null;

	private JScrollPane jScrollTablaParametro = null;

	private JTable jTableParametro = null;

	private JLabel jLabel19 = null;
	
	private boolean seleccion=false;//Selección de tipo de analisis
	
	private char seleccion2;

	private JButton jButton4 = null;

	private JButton evidencia2 = null;
	
	private ResourceBundle analisysBundle;

	/**
	 * This is the default constructor
	 */
	public AnalisysMainFrame(NetworkFrame frame) {
		
		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: analisysBundle = ResourceBundle.getBundle ("elvira/localize/SensitivityAnalisys");            
			                         break;
		   case Elvira.SPANISH:  analisysBundle = ResourceBundle.getBundle ("elvira/localize/SensitivityAnalisys_sp");   
			                         break;		                         
		}		
		
		frame2=frame;
		infpanel=frame.getInferencePanel();//Panel main de Elvira
		bnet=infpanel.getBayesNet();//Red en estudio
		casesl=infpanel.getCasesList();//lista de casos asociada a la red
		cedit=casesl.getCurrentCase();//Obtener el caso Activo

		
		//¿Hay hallazgos introducidos por el usuario inicialmente?
		for(int i=0;i<bnet.getNodeList().size();i++){
			if(cedit.getIsObserved(bnet.getNodeList().elementAt(i))){
				
				seleccion=true;
			}
		}
/*SI--*/if(seleccion==true){
			initialize(frame);
		}else{
		
/*NO-->*/	Start_Analisis start= new Start_Analisis(frame);
			start.setVisible(true);
			if(start.getTipo()=='c'){
				seleccion2='c';
				initialize(frame);//CON evidencia
			}else{
				if(start.getTipo()=='s'){
					seleccion2='s';
					initialize2(frame);//SIN evidencia
				}else{
					this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
			}
		}
	}	

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(NetworkFrame frame) {
		this.setSize(862, 551);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(Elvira.getElviraFrame());
		this.setResizable(false);
		this.setModal(true);
		this.setContentPane(getJContentPane());
		this.setTitle(Elvira.localize(analisysBundle,"Sensitivity.Analisys"));
		//Code to know if we have entered any evidence before
		for(int i=0;i<bnet.getNodeList().size();i++){
			if(cedit.getIsObserved(bnet.getNodeList().elementAt(i))){
				evidencia2.setVisible(true);
				jButton.setBounds(new Rectangle(10,89,120,40));
				jButton.setText(Elvira.localize(analisysBundle,"Add.Observations"));
				jButton.setToolTipText(Elvira.localize(analisysBundle,"Evidence.ToolTip"));
			}
		}	
		if(casesl.getNumCurrentCase()==0){
		JOptionPane.showMessageDialog(frame2,
                "Ningún caso almacenado \n"+"Se va a crear un nuevo caso de estudio para el Análisis de Sensibilidad",
                "Número de casos de estudio =0",
                JOptionPane.INFORMATION_MESSAGE);
		}
		this.setVisible(true);
	}
 
	// 2º metodo para inicializar 
	// en el caso de que el usuario decida 
	// utilizar el analisis SIN evidencia
	
	private void initialize2(NetworkFrame frame){
		this.setSize(862, 551);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(Elvira.getElviraFrame());
		this.setResizable(false);
		this.setModal(true);
		this.setContentPane(getJContentPane());
		this.setTitle(Elvira.localize(analisysBundle,"Sensitivity.Analisys"));
		
		if(casesl.getNumCurrentCase()==0){
			JOptionPane.showMessageDialog(frame2,
	                "Análisis sin Evidencia\n"+"No se registrarán casos de estudio para el Análisis de Sensibilidad",
	                "Número de casos de estudio =0",
	                JOptionPane.INFORMATION_MESSAGE);
			}
		jLabel14.setText(Elvira.localize(analisysBundle,"Analisys.without.Evidence"));
		jButton.setEnabled(false);
		jButton.setText(Elvira.localize(analisysBundle,"Study.without.Evidence"));
		IncrementBar();
		jButton7.setVisible(true);
		jLabel15.setVisible(true);
		
		this.setVisible(true);
		
	}
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel18 = new JLabel();
			jLabel18.setBounds(new Rectangle(360, 466, 184, 29));
			jLabel18.setForeground(Color.green);
			jLabel18.setText(Elvira.localize(analisysBundle,"Probability.Later"));
			jLabel18.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabel18.setVisible(false);
			jLabel17 = new JLabel();
			jLabel17.setBounds(new Rectangle(360, 427, 181, 29));
			jLabel17.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabel17.setForeground(Color.red);
			jLabel17.setText(Elvira.localize(analisysBundle,"Probability.Prior"));
			jLabel17.setVisible(false);
			jLabel13 = new JLabel();
			jLabel13.setBounds(new Rectangle(311, 389, 33, 27));
			jLabel13.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel13.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel13.setFont(new Font("DialogInput", Font.BOLD, 18));
			jLabel13.setText("3");
			jLabel13.setBorder(BorderFactory.createEtchedBorder(null, null));
			jLabel13.setVisible(false);
			jLabel10 = new JLabel();
			jLabel10.setBounds(new Rectangle(495, 465, 42, 24));
			jLabel10.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel10.setForeground(Color.green);
			jLabel10.setText("JLabel");
			jLabel10.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel10.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel10.setText("0.69");
			jLabel10.setVisible(false);
			jLabel9 = new JLabel();
			jLabel9.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel9.setForeground(Color.red);
			jLabel9.setText("0.345");
			jLabel9.setBounds(new Rectangle(493, 436, 43, 23));
			jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel9.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel9.setVisible(false);
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(305, 465, 184, 24));
			jLabel8.setText(Elvira.localize(analisysBundle,"Probability.Later.Interest"));
			jLabel8.setVisible(false);
			jLabel7 = new JLabel();
			jLabel7.setText(Elvira.localize(analisysBundle,"Probability.Prior.Interest"));
			jLabel7.setBounds(new Rectangle(304, 436, 180, 24));
			jLabel7.setVisible(false);
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getPanel2(), null);
			jContentPane.add(getPanel1(), null);
			jContentPane.add(getJButton1(), null);
			jContentPane.add(getJProgressBar(), null);
			jContentPane.add(jLabel8, null);
			jContentPane.add(jLabel10, null);
			jContentPane.add(jLabel13, null);
			jContentPane.add(getJButton2(), null);
			jContentPane.add(getJButton5(), null);
			jContentPane.add(getJButton6(), null);
			jContentPane.add(jLabel9, null);
			jContentPane.add(jLabel7, null);
			jContentPane.add(jLabel17, null);
			jContentPane.add(jLabel18, null);
			
		}
		
		
		return jContentPane;
	};

	/**
	 * This method initializes Panel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPanel2() {
		if (Panel2 == null) {
			jLabel19 = new JLabel();
			jLabel19.setBounds(new Rectangle(224, 106, 282, 22));
			jLabel19.setText(Elvira.localize(analisysBundle,"Choose.Values.Parents"));
			jLabel16 = new JLabel();
			jLabel16.setBounds(new Rectangle(250, 303, 31, 23));
			jLabel16.setText("0.");
			jLabel12 = new JLabel();
			jLabel12.setBounds(new Rectangle(10, 5, 32, 26));
			jLabel12.setFont(new Font("DialogInput", Font.BOLD, 18));
			jLabel12.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel12.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel12.setText("2");
			jLabel12.setBorder(BorderFactory.createEtchedBorder(null, null));
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(105, 306, 63, 20));
			jLabel6.setText("");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(171, 303, 75, 22));
			jLabel5.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel5.setText(Elvira.localize(analisysBundle,"NewValue"));
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(14, 104, 174, 20));
			jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel3.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel3.setText(Elvira.localize(analisysBundle,"Choose.Parameter.Value"));
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(10, 52, 149, 20));
			jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel2.setText(Elvira.localize(analisysBundle,"Choose.Variable"));
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(173, 7, 233, 20));
			jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel1.setFont(new Font("Dialog", Font.BOLD, 15));
			jLabel1.setToolTipText(Elvira.localize(analisysBundle,"Study.Conditional.Probability"));
			jLabel1.setText(Elvira.localize(analisysBundle,"Study.Parameter"));
			Panel2 = new JPanel();
			Panel2.setLayout(null);
			Panel2.setBounds(new Rectangle(314, 13, 527, 343));
			Panel2.add(jLabel1, null);
			Panel2.add(getJComboBox(), null);
			Panel2.add(jLabel2, null);
			Panel2.add(jLabel3, null);
			Panel2.add(jLabel5, null);
			Panel2.add(jLabel6, null);
			Panel2.setBorder(BorderFactory.createRaisedBevelBorder());
			Panel2.setVisible(false);
			Panel2.add(jLabel12, null);
			Panel2.add(jLabel16, null);
			Panel2.add(getJButton3(), null);
			Panel2.add(getJTextField());
			Panel2.add(getJListParametro(), null);
			Panel2.add(getJScrollTablaParametro(), null);
			Panel2.add(jLabel19, null);
			Panel2.add(getJButton4(), null);
			
		}
		return Panel2;
	}

	
	
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox=new JComboBox();
			//jComboBox=fill_Jcombo1();
			jComboBox.setBounds(new Rectangle(166, 54, 151, 23));	
			
			jComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					//Metodo para actualizar la tabla
					variables.removeAllElements();
					
					String name_nodo=(String)jComboBox.getSelectedItem();
					node=bnet.getNodeList().getNodeString(name_nodo,true);
					//relacion=bnet.getRelation(bnet.getNodeList().getNodeString(name_nodo,true));
					fill_JlistParametro(node);
				
					//Para activar el Boton de Valor inicial del Parametro
					if(!(node.hasParentNodes()))jButton4.setEnabled(true);
					if(node.hasParentNodes())jButton4.setEnabled(false);
					
					jButton4.setEnabled(true);
							
					variables.add(node);
					fill_JTableParametro(node);
				
					}
				});
		
		}
		
		
		return jComboBox;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextfield == null) {
			LimitadorDeDocumento limitador = new LimitadorDeDocumento(3);
			jTextfield = new JTextField();
			jTextfield.setBounds(new Rectangle(264, 305, 47, 22));
			jTextfield.setEnabled(false);
			jTextfield.setDocument(limitador);
			jTextfield.setToolTipText(Elvira.localize(analisysBundle,"Only.Numeric.Values"));
									
			jTextfield.addKeyListener(new KeyAdapter()
			{
				public void keyTyped(KeyEvent e)
			   {
			      char caracter = e.getKeyChar();
			      
			      // Verificar si la tecla pulsada no es un digito
			      if(((caracter < '0') ||
			         (caracter > '9')) &&
			         (caracter != KeyEvent.VK_BACK_SPACE))
			      {
			         e.consume();  // ignorar el evento de teclado
			        
			      }
			      if(jTextfield.getText().length()==2)jButton3.setEnabled(true);
			   }
			});
								

		}
		return jTextfield;
	}

	/**
	 * This method initializes Panel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPanel1() {
		if (Panel1 == null) {
			jLabel15 = new JLabel();
			jLabel15.setBounds(new Rectangle(16, 227, 246, 26));
			jLabel15.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel15.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel15.setText(Elvira.localize(analisysBundle,"Choose.Interest.Variable"));
			jLabel15.setToolTipText(Elvira.localize(analisysBundle,"Interest.Variable.Tooltip"));
			jLabel15.setVisible(false);
			jLabel14 = new JLabel();
			jLabel14.setBounds(new Rectangle(8, 57, 259, 26));
			jLabel14.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel14.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel14.setToolTipText(Elvira.localize(analisysBundle,"Observations.Tooltip"));
			jLabel14.setText(Elvira.localize(analisysBundle, "Select.the.Evidence"));
			jLabel11 = new JLabel();
			jLabel11.setBounds(new Rectangle(10, 5, 31, 24));
			jLabel11.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel11.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel11.setFont(new Font("DialogInput", Font.BOLD, 18));
			jLabel11.setText("1");
			jLabel11.setBorder(BorderFactory.createEtchedBorder(null, null));
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(53, 10, 186, 18));
			jLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel.setFont(new Font("Dialog", Font.BOLD, 15));
			jLabel.setToolTipText(Elvira.localize(analisysBundle,"Step_one"));
			jLabel.setText(Elvira.localize(analisysBundle,"Interest.Probability"));
			Panel1 = new JPanel();
			Panel1.setLayout(null);
			Panel1.setBounds(new Rectangle(11, 10, 283, 490));
			Panel1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			Panel1.add(jLabel, null);
			Panel1.add(jLabel11, null);
			Panel1.add(jLabel14, null);
			Panel1.add(getJButton(), null);
			Panel1.add(jLabel15, null);
			Panel1.add(getJScrollPane1(), null);
			Panel1.add(getJScrollPane2(), null);
			Panel1.add(getJButton7(), null);
			Panel1.add(getEvidencia2(), null);
		}
		return Panel1;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setBounds(new Rectangle(644, 371, 199, 42));
			jButton1.setText(Elvira.localize(analisysBundle,"Graphic.Result"));
			jButton1.setIcon(new ImageIcon("C:/Documents and Settings/Alberto/Mis documentos/Mis imágenes/grafico2.JPG"));
			jButton1.setHorizontalAlignment(SwingConstants.RIGHT);
			jButton1.setHorizontalTextPosition(SwingConstants.LEFT);
			jButton1.setToolTipText(Elvira.localize(analisysBundle,"Graphic.Result.Tooltip"));
			jButton1.setVisible(false);
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					DataGraphs dt= new DataGraphs(
						
						Double.valueOf(jLabel6.getText()).doubleValue(),
					   (Double.valueOf(jTextfield.getText()).doubleValue())/1000,
						Double.valueOf(jLabel9.getText()).doubleValue(),
						Double.valueOf(jLabel10.getText()).doubleValue());
					
					ResultAnalisysGraph gr= new ResultAnalisysGraph(dt,(String)tabla_interes.getValueAt(0,0),(String)tabla_interes.getValueAt(0,1),(String)jComboBox.getSelectedItem(),(String)jListParametro.getSelectedValue());
					gr.setVisible(true);
					
				}		
			});	
		}
		return jButton1;
	}

	/**
	 * This method initializes jProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
			jProgressBar.setBounds(new Rectangle(312, 496, 286, 19));
			jProgressBar.setStringPainted(true);
			jProgressBar.setMaximum(7);
			jProgressBar.setToolTipText(Elvira.localize(analisysBundle,"Execution.Percent"));
			jProgressBar.setVisible(true);
		}
		return jProgressBar;
	}

	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setBounds(new Rectangle(351, 388, 213, 28));
			jButton2.setText(Elvira.localize(analisysBundle,"Calculate.Probabilities"));
			jButton2.setToolTipText(Elvira.localize(analisysBundle,"Calculate.Probabilities.Tooltip"));
			jButton2.setVisible(false);
		
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					IncrementBar();
					int p=0;
					//Analisis SIN Evidencia  Nivel 3.2 del Algoritmo
					if(tabla_datos==null){
						int s=(Integer)tabla_interes.getValueAt(0,2);
						for(int i=0;i<bnet.getNodeList().size();i++){
							if(bnet.getNodeList().elementAt(i).getNodeString(true).equals(tabla_interes.getValueAt(0,0))){
								double resul=cedit.getProbOfStateNode((FiniteStates)bnet.getNodeList().elementAt(i),s);
								p=i;
								jLabel9.setText(String.valueOf(resul));
							}
						}			
						AnalisysFunctionalRelation rf= new AnalisysFunctionalRelation(cedit,jTextfield.getText(),(String)jComboBox.getSelectedItem(),numero_bin,bnet,p,s,(String)tabla_interes.getValueAt(0,0));
						try{
						jLabel10.setText(rf.Sin_Evidencia(infpanel,infpanel));
						} catch (Throwable n) {
							// TODO Auto-generated catch block
							n.printStackTrace();
						}
						jLabel10.setVisible(true);
						jButton2.setVisible(false);
						jButton1.setVisible(true);
						jLabel9.setVisible(true);
						jButton5.setVisible(true);
						jProgressBar.setVisible(true);
						jLabel8.setVisible(true);
						jLabel13.setVisible(false);
						jTextfield.setEditable(false);
						jButton.setEnabled(false);
						jButton7.setEnabled(false);
						jLabel7.setVisible(true);
						jLabel17.setVisible(false);
						jLabel18.setVisible(false);
						infpanel.repaint();
						infpanel.updateUI();
					}
					//Analisis CON Evidencia  Nivel 3.2 del Algoritmo
					else{
//					Evidencia seleccionada desde la red directamente
						if(evidencia2.isShowing()){
							cedit=casesl.getCurrentCase();
							//Para saber si se han ampliado con nuevos hallazgos
							for(int h=0;h<tabla_datos.getRowCount();h++){
								if(tabla_datos.getValueAt(h,0)!=null){
									for(int g=0;g<bnet.getNodeList().size();g++){
										if(bnet.getNodeList().elementAt(g).getNodeString(true).equals(tabla_datos.getValueAt(h,0))){
											if(!(cedit.getIsObserved(bnet.getNodeList().elementAt(g)))){
												System.out.println("Hallazgo de la red introducido "+bnet.getNodeList().elementAt(g).getNodeString(true));
												FiniteStates fm=(FiniteStates)bnet.getNodeList().elementAt(g);
												int v=(Integer)tabla_datos.getValueAt(h,2);
												cedit.setAsFinding(fm, v);
												casesl.addCurrentCase(fm, v);
												cedit.propagate();
												infpanel.propagate(casesl.getCurrentCase());
												infpanel.repaint();
												infpanel.updateUI();
											}
										}
									}
								}
							}
					
							int s=(Integer)tabla_interes.getValueAt(0,2);
							for(int i=0;i<bnet.getNodeList().size();i++){
								if(bnet.getNodeList().elementAt(i).getNodeString(true).equals(tabla_interes.getValueAt(0,0))){
									double resul=cedit.getProbOfStateNode((FiniteStates)bnet.getNodeList().elementAt(i),s);
									p=i;
									jLabel9.setText(String.valueOf(resul));
								}
							}
							AnalisysFunctionalRelation rf= new AnalisysFunctionalRelation(cedit,jTextfield.getText(),(String)jComboBox.getSelectedItem(),numero_bin,bnet,p,s,(String)tabla_interes.getValueAt(0,0));
							try {
								jLabel10.setText(rf.Con_Evidencia());
							} catch (Throwable e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							jLabel10.setVisible(true);
							jButton2.setVisible(false);
							jButton1.setVisible(true);
							jLabel9.setVisible(true);
							jButton5.setVisible(true);
							jProgressBar.setVisible(true);
							jLabel8.setVisible(true);
							jLabel13.setVisible(false);
							jTextfield.setEditable(false);
							jButton.setEnabled(false);
							jButton7.setEnabled(false);
							jLabel7.setVisible(true);
							jLabel17.setVisible(false);
							jLabel18.setVisible(false);
					
							//Hallazgos que se introducen en el Análisis y 
							// por tanto crean un nuevo caso de estudio
						}else{
							casesl.setCurrentCase(1);
							cedit=casesl.getCurrentCase();
						
							for(int g=0;g<tabla_datos.getRowCount();g++){
								System.out.println(tabla_datos.getValueAt(g,0));
								if(!(tabla_datos.getValueAt(g,0)==null)){
									for(int d=0;d<bnet.getNodeList().size();d++){
									//System.out.println("red :" +bnet.getNodeList().elementAt(d).getNodeString(true));
										if(((String)tabla_datos.getValueAt(g,0)).equals(bnet.getNodeList().elementAt(d).getNodeString(true))){
											if(!(cedit.getIsObserved(bnet.getNodeList().elementAt(d)))){
												FiniteStates fx=(FiniteStates)bnet.getNodeList().elementAt(d);
												int v=(Integer)tabla_datos.getValueAt(g,2);
												cedit.setAsFinding(fx, v);
												casesl.addCurrentCase(fx, v);
												cedit.propagate();
												infpanel.propagate(casesl.getCurrentCase());
												infpanel.repaint();
												infpanel.updateUI();
											}	
										}
									}
								}	
							}//Si solo elegimos ¡¡¡¡UNA¡¡¡¡ variable de interes//OJO
							int s=(Integer)tabla_interes.getValueAt(0,2);
							for(int i=0;i<bnet.getNodeList().size();i++){
								if(bnet.getNodeList().elementAt(i).getNodeString(true).equals(tabla_interes.getValueAt(0,0))){
									double resul=cedit.getProbOfStateNode((FiniteStates)bnet.getNodeList().elementAt(i),s);
									p=i;
									jLabel9.setText(String.valueOf(resul));
								}
							}
							AnalisysFunctionalRelation rf= new AnalisysFunctionalRelation(cedit,jTextfield.getText(),(String)jComboBox.getSelectedItem(),numero_bin,bnet,p,s,(String)tabla_interes.getValueAt(0,0));
							try {
								jLabel10.setText(rf.Con_Evidencia());
							} catch (Throwable e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							jButton2.setVisible(false);
							jButton1.setVisible(true);
							jLabel9.setVisible(true);
							jButton5.setVisible(true);
							jProgressBar.setVisible(true);
							jLabel8.setVisible(true);
							jLabel10.setVisible(true);
							jLabel13.setVisible(false);
							jTextfield.setEditable(false);
							jButton.setEnabled(false);
							jButton7.setEnabled(false);
							jLabel7.setVisible(true);
							jLabel17.setVisible(false);
							jLabel18.setVisible(false);
						}
					}//Fin del else de ANalisis Con evidencia
				}		
			});	
				infpanel.updateUI();
				infpanel.repaint();
			
		}// fin del IF del componente Botón
		return jButton2;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(54, 90, 182, 38));
			jButton.setToolTipText(Elvira.localize(analisysBundle,"Choose.Evidence"));
			jButton.setText(Elvira.localize(analisysBundle,"Evidence"));
			
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					IncrementBar();
					evidencia2.setEnabled(false);
					ChoiceEvidence evi= new ChoiceEvidence(frame2);
					if(evi.getDefaultCloseOperation()==WindowConstants.DISPOSE_ON_CLOSE){
															//BOTON DE CANCELAR
						jScrollPane1.setVisible(false);		//EN EL jDIALOG DE Evidencias
						jLabel5.setVisible(false);
						jButton7.setVisible(false);
						jScrollPane2.setVisible(false);
					}else{
						jScrollPane1.setViewportView(evi.getJTable());
						tabla_datos=evi.getJTable();
						jButton.setEnabled(false);			//Boton de ACEPTAR 
						jScrollPane1.setVisible(true);		//EN EL jDIALOG DE EVIDENCIAS
						jLabel5.setVisible(true);
						jButton7.setVisible(true);
						jScrollPane2.setVisible(false);
						jLabel15.setVisible(true);
						
					}
					
				}	
			});	
			
		}
		return jButton;
	}
	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setBounds(new Rectangle(329, 305, 142, 22));
			jButton3.setEnabled(false);
			jButton3.setText(Elvira.localize(analisysBundle,"Insert"));
				
			
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					IncrementBar();
					jButton2.setVisible(true);
					jLabel13.setVisible(true);
					jButton3.setEnabled(false);
					jButton1.setVisible(false);
					jButton5.setVisible(false);
					jLabel8.setVisible(false);
					jLabel10.setVisible(false);
					jLabel17.setVisible(true);
					jLabel18.setVisible(true);
					jButton4.setEnabled(false);
			
				}		
			});	
				
		}
		return jButton3;
	}

	/**
	 * This method initializes jButton5	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton5() {
		if (jButton5 == null) {
			jButton5 = new JButton(Elvira.localize(analisysBundle,"NewAnalisys"),new ImageIcon("C:/Documents and Settings/Alberto/Mis documentos/Mis imágenes/icono2.JPG"));
			jButton5.setBounds(new Rectangle(643, 422, 195, 39));
			jButton5.setHorizontalAlignment(SwingConstants.RIGHT);
			jButton5.setHorizontalTextPosition(SwingConstants.LEFT);
			jButton5.setToolTipText(Elvira.localize(analisysBundle,"NewAnalisys.Tooltip"));
			jButton5.setVisible(false);
			
			jButton5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					AnalisysMainFrame nuevo= new AnalisysMainFrame(frame2);
					nuevo.setVisible(true);
				}		
			});	
			
			
		}
		return jButton5;
	}

	/**
	 * This method initializes jButton6	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton6() {
		if (jButton6 == null) {
			jButton6 = new JButton();
			jButton6.setBounds(new Rectangle(643, 471, 196, 36));
			jButton6.setHorizontalTextPosition(SwingConstants.CENTER);
			jButton6.setToolTipText(Elvira.localize(analisysBundle,"Exit.Analisys.Tooltip"));
			jButton6.setText(Elvira.localize(analisysBundle,"Exit.Analisys"));
			jButton6.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
			
		}
		return jButton6;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	protected JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setBounds(new Rectangle(15, 136, 247, 82));
			jScrollPane1.setToolTipText(Elvira.localize(analisysBundle,"Choose.Observations.Tooltip"));
			jScrollPane1.setVisible(false);
			
		}
		return jScrollPane1;
	}
	/**
	 * This method initializes jScrollPane2	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setBounds(new Rectangle(20, 321, 237, 88));
			jScrollPane2.setToolTipText(Elvira.localize(analisysBundle,"Choose.Variables"));
			jScrollPane2.setVisible(false);
		}
		return jScrollPane2;
	}
 
	/**
	 * This method initializes jButton7	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton7() {
		if (jButton7 == null) {
			jButton7 = new JButton();
			jButton7.setBounds(new Rectangle(28, 264, 222, 37));
			jButton7.setText(Elvira.localize(analisysBundle,"Study.Variable"));
			jButton7.setToolTipText(Elvira.localize(analisysBundle,"Choose.Interest.Variable.Tooltip"));
			jButton7.setVisible(false);
			
			jButton7.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					IncrementBar();
					ChoiceInterestVariable vi= new ChoiceInterestVariable(frame2,tabla_datos);
					vi.setVisible(true);
					///////////////////
					if(vi.getDefaultCloseOperation()==WindowConstants.DISPOSE_ON_CLOSE){
															//BOTON DE CANCELAR
						jScrollPane2.setVisible(false);		//EN EL jDIALOG DE Evidencias
						Panel2.setVisible(false);
						
					}else{
						
						jScrollPane2.setViewportView(vi.getJTable());
						tabla_interes=vi.getJTable();		//Boton de ACEPTAR 
						jScrollPane2.setVisible(true);		//EN EL jDIALOG DE EVIDENCIAS
						Panel2.setVisible(true);
						jButton7.setEnabled(false);
						try {
							fill_Jcombo1();
						} catch (Throwable e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}	
			});	
	
		}
		return jButton7;
	}
	
	
//	Metodo para rellenar el JComboBox1
	private void fill_Jcombo1() throws Throwable{
		
		DefaultComboBoxModel combovarmodel=new DefaultComboBoxModel();
		//Dependiendo del tipo de Analisis tendremos que obtener el 
		//conjunto sensible o no
		
		//SIN EVidencia NO hay conjunto Sensible
		if(seleccion==false && seleccion2=='s'){
			for(int n=0; n<bnet.getNodeList().size(); n++){
				combovarmodel.addElement(bnet.getNodeList().elementAt(n).getNodeString(true));
			}
		}else{//NIVEL 2 -->Creación del Conjunto Sensible
			String [] hallazgos= new String[bnet.getNodeList().size()];
			for(int i=0;i<tabla_datos.getRowCount();i++){
				if(tabla_datos.getValueAt(i,0)!=null){
					hallazgos[i]=(String)tabla_datos.getValueAt(i,0);
				}
			}
			System.out.println("");
			System.out.println("CONJUNTO SENSIBLE EJECUTANDOSE...");
			for (int n=0; n<bnet.getNodeList().size(); n++){
				cedit=casesl.getCurrentCase(); 
				Sensitive_Group grupo= new Sensitive_Group(bnet.getNodeList().elementAt(n).getNodeString(true),bnet,(String)tabla_interes.getValueAt(0,0),hallazgos,tabla_datos);
				if(grupo.Es_Sensible()){
					combovarmodel.addElement(bnet.getNodeList().elementAt(n).getNodeString(true));
				}
			}
			
			for (int n=0; n<bnet.getNodeList().size(); n++){
				for(int g=0;g<tabla_datos.getRowCount();g++){
					if(tabla_datos.getValueAt(g,0)!=null){
						String aux=(String)tabla_datos.getValueAt(g,0);
						if(bnet.getNodeList().elementAt(n).getNodeString(true).compareTo(aux)==0){
						  combovarmodel.removeElement(aux);												
						}
					}
				}
			}
			
		}	
		jComboBox.setModel(combovarmodel);
		
	}


	/**
	 * This method initializes jListParametro	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJListParametro() {
		if (jListParametro == null) {
			DefaultListModel listaparametro= new DefaultListModel();
			jListParametro = new JList(listaparametro);
			jListParametro.setBounds(new Rectangle(13, 137, 134, 125));
			jListParametro
					.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(javax.swing.event.ListSelectionEvent e) {
							jScrollTablaParametro.setVisible(true);
							numero_bin=String.valueOf(jListParametro.getSelectedIndex());
							jListParametro.setEnabled(false);
							IncrementBar();
							jComboBox.setEnabled(false);
						}
					});
		}
		return jListParametro;
	}
	
	private JScrollPane getScrollParametro(){
		if(jScrollParametro==null){
			jScrollParametro = new JScrollPane(getJListParametro());
			jScrollParametro.setViewportView(getJListParametro());
		}
		return jScrollParametro;
	}


public void fill_JlistParametro(Node nodopara){
		
		DefaultListModel lista= new DefaultListModel();
										
					FiniteStates fs=(FiniteStates)nodopara;
					for(int j=0;j<fs.getNumStates();j++){ 
						lista.addElement(fs.getState(j));
						
					}
		 getJListParametro().setModel(lista);
		 getJListParametro().setLayoutOrientation(getJListParametro().HORIZONTAL_WRAP);
		 getJListParametro().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
}


/**
 * This method initializes jScrollTablaParametro	
 * 	
 * @return javax.swing.JScrollPane	
 */
private JScrollPane getJScrollTablaParametro() {
	if (jScrollTablaParametro == null) {
		jScrollTablaParametro = new JScrollPane();
		jScrollTablaParametro.setBounds(new Rectangle(199, 135, 312, 126));
		jScrollTablaParametro.setViewportView(getJTableParametro());
		jScrollTablaParametro.setVisible(false);
	}
	return jScrollTablaParametro;
}


/**
 * This method initializes jTableParametro	
 * 	
 * @return javax.swing.JTable	
 */
private JTable getJTableParametro() {
	if (jTableParametro == null) {
		PTableModel tabla= new PTableModel();
		
		jTableParametro = new JTable(tabla);
		jTableParametro.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableParametro.setCellSelectionEnabled(true);
		jTableParametro.setRowSelectionAllowed(false);
		jTableParametro.setColumnSelectionAllowed(false);
		jTableParametro.setToolTipText(Elvira.localize(analisysBundle,"Values.Parents.Tooltip"));
		jTableParametro.setEnabled(true);
		
		
	}
	return jTableParametro;
}
	 /*
	  * Metodo para rellenar la tabla con los valores
	  * de los padres del nodo escogido como parametro
	  * para Analizar.
	  */
public void fill_JTableParametro(Node nodoactual){
	String []nombres={"Padres","Valores","Estado"};
	String [][]indexar= new String[nodoactual.getParentNodes().size()][20];
	PTableModel aux= new PTableModel(nombres,nodoactual.getParentNodes().size());
	
	
	getJTableParametro().setModel(aux);
	NodeList listanodos=nodoactual.getParentNodes();
	//Obtengo la columna de valores para modelarla
	TableColumn col = getJTableParametro().getColumnModel().getColumn(1);
	
	
	for(int i=0;i<nodoactual.getParentNodes().size();i++){
		
	indexar[i][0]=	(String)listanodos.elementAt(i).getNodeString(true);
	getJTableParametro().setValueAt(listanodos.elementAt(i).getNodeString(true), i,0);
    
	//Obtengo los estados de los padres
	FiniteStates fs=(FiniteStates)listanodos.elementAt(i);
	String estados[]= new String[fs.getNumStates()];
	
	
	for(int y=0;y<fs.getNumStates();y++){
	
			estados[y]=(fs.getState(y));
			indexar[i][y+1]=fs.getState(y);
		
		}
	
	setUpValuesColum(col,estados,listanodos.elementAt(i),indexar);
	}
	
	
}
	//Para visualizar las características de la Tabla

public void setUpValuesColum (TableColumn columna,String []estados,Node e,String [][] indexar){
	//columna.setCellRenderer(new MyComboBoxRenderer(estados));
	
	columna.setCellEditor(new MyTableCellEditor(estados,e,indexar));
		
	//Indicar ToolTip para la cabecera de la Tabla
	TableCellRenderer headerender= columna.getHeaderRenderer();
	if(headerender instanceof DefaultTableCellRenderer){
		((DefaultTableCellRenderer)headerender).setToolTipText(Elvira.localize(analisysBundle,"State.Column"));
		
	}
}

public class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor{
	MyComboBoxRenderer box;
	Node e;
	String[][] indexar;
	int fila=0;
	int columna=0;
	Object valor=null;
	
	public MyTableCellEditor(String[] items,Node a,String [][]index) {
     
		box= new MyComboBoxRenderer(items,index);
        e=a;
        indexar=index;
        
    }

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		
			fila=row;
			columna=column;
			valor=value;
			String padre=(String)table.getValueAt(row,column -1);
			
			box.removeAllItems();
			
			for(int t=0;t<indexar.length;t++){
				if(padre.equals(indexar[t][0])){
					int y=0;
					do{
						box.addItem(indexar[t][y+1]);
						y++;
					}while(indexar[t][y]!=null);
						
			    }
		   }	
			jButton4.setEnabled(true);
			
		return  box;
	}
	

	public Object getCellEditorValue() {
			
			jTableParametro.setValueAt(box.getSelectedIndex(), fila,columna+1);
				
				return box.getSelectedItem();
	}
} 



public class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {
    
	public MyComboBoxRenderer(String[] items,String [][]indexaraux) {
		super(items);
      }
    

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
    	
    	
    	if (isSelected) {
    		
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        
        this.setToolTipText(Elvira.localize(analisysBundle,"State.Column.Tooltip"));
        
        // Select the current value
        setSelectedItem(value);
        return this;
    }
    public JComboBox getCombo(){
    	
    	return this;
    }
    

}
//TableModel para que la tabla donde se representar los 
//parámetros en estudio no se editable ya que "DefaultTableModel"
//por defecto no lo impide
public class PTableModel extends DefaultTableModel{
		
	public PTableModel(){
		super();
	}
	public PTableModel(String []campos,int dimension){
			super(campos,dimension);
		}
	public boolean isCellEditable(int row, int col){
		
		if(col==0||col ==2){
			return false;
		}else{
		 return true;	
		}
	}
}
/**
 * This method initializes jButton4	
 * 	
 * @return javax.swing.JButton	
 */
private JButton getJButton4() {
	if (jButton4 == null) {
		jButton4 = new JButton();
		jButton4.setBounds(new Rectangle(12, 306, 88, 23));
		jButton4.setToolTipText(Elvira.localize(analisysBundle,"Parameter.Initial.Tooltip"));
		jButton4.setText(Elvira.localize(analisysBundle,"Parameter.Initial"));
		jButton4.setEnabled(false);
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButton4.setEnabled(false);
				IncrementBar();
//				2º Paso para introducir numero binario
				for(int i=0;i<jTableParametro.getRowCount();i++){
					if(jTableParametro.getValueAt(i,2)!=null){
						numero_bin=numero_bin+jTableParametro.getValueAt(i,2);
					}
				}
				relacion=bnet.getRelation(node);
				PotentialTable pot=(PotentialTable)relacion.getValues();
				//relacion.print();
				
				jLabel6.setText(String.valueOf(pot.getValue(calcular_decimal(numero_bin,node))));
				jTableParametro.setEnabled(false);
				jTextfield.setEnabled(true);
			}
		});
	}
	return jButton4;
}
	public  int calcular_decimal(String binario,Node nodo){
		NodeList listapadres=nodo.getParentNodes();
		Vector listapadres2= listapadres.getNodes();
		FiniteStates fsnodo=(FiniteStates)nodo;
		Vector variables= new Vector();
		variables.add(nodo);
		int decimal=0;
		for(int o=0;o<listapadres.size();o++){
			variables.add(listapadres2.get(o));
		}
		int dimension=1;
		int numero_var=1+listapadres.size();
		for(int u=0;u<listapadres.size();u++){
			FiniteStates fn=(FiniteStates)listapadres2.get(u);
			dimension=dimension*fn.getNumStates();
		}
		String[]buscar= new String[(fsnodo.getNumStates()*dimension)+1];
		for(int r=0;r<buscar.length;r++){
			buscar[r]="";
		}
		buscar[0]="";
		System.out.println(buscar.length);
		int lon=0;
		int i=0;
		boolean primero=true;
		do{
			Node aux=(Node)variables.get(0);
			FiniteStates fsaux=(FiniteStates )aux;
			variables.remove(0);
			if(primero){
				lon=(buscar.length-1)/fsaux.getNumStates();
			}else{
				lon=lon/fsaux.getNumStates();
			}
				do{
					for(int l=0;l<fsaux.getNumStates();l++){
						for(int q=0;q<lon;q++){
							buscar[i]=buscar[i]+ String.valueOf(l);
							System.out.println(buscar[i]);
							i++;
						}
					}
				}while(i!=buscar.length-1);	
				numero_var--;
				primero=false;
				i=0;
		}while(numero_var!=0);
				
		
		
		for(int d=0;d<buscar.length;d++){
			if(buscar[d].compareTo(binario)==0){
				decimal=d;
			}
		}
		return decimal;
	}


	/**
	 * This method initializes evidencia2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getEvidencia2() {
		if (evidencia2 == null) {
			evidencia2 = new JButton();
			evidencia2.setBounds(new Rectangle(140, 89, 130, 40));
			evidencia2.setText(Elvira.localize(analisysBundle,"Show.Observations"));
			evidencia2.setToolTipText(Elvira.localize(analisysBundle,"Show.Observations.Tooltip"));
			evidencia2.setVisible(false);
			evidencia2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					IncrementBar();
					jButton.setEnabled(false);
					evidencia2.setEnabled(false);
					jLabel15.setVisible(true);
					jButton7.setVisible(true);
					boolean es=true;
					ChoiceEvidence ev= new ChoiceEvidence(frame2,es);
					ev.setVisible(false);
					tabla_datos=ev.getJTable();
					actualizarTabla();
					jScrollPane1.setViewportView(ev.getJTable());
					tabla_datos.setVisible(true);
					jScrollPane1.setVisible(true);
				}
			});
		}
		return evidencia2;
	}
	/**
	 * Method to increment the value of the bar progress
	 */
	public void IncrementBar(){
		getJProgressBar().setValue(jProgressBar.getValue()+1);
		if((getJProgressBar().getValue())==7){
			getJProgressBar().setString("100"+" %");//14.28
		}else{
		valor += 14.289;
		getJProgressBar().setString(String.valueOf(valor)+" %");//14.28
		}
	}
	/**
	 * Method to update the evidence table
	 *
	 */
	public void actualizarTabla(){
		for(int j=0;j<3;j++){	
			for(int h=1;h<tabla_datos.getRowCount();h++){
				if(!(tabla_datos.getValueAt(h,j)==null)){
					if(tabla_datos.getValueAt(h-1,j)==null){
						tabla_datos.setValueAt(tabla_datos.getValueAt(h,j), h-1,j);
						tabla_datos.setValueAt(null, h, j);
						tabla_datos.repaint();
						tabla_datos.updateUI();
					}
				}
			}
		}
	}
	
} //  @jve:decl-index=0:visual-constraint="9,9"
