package elvira.gui.explication;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import elvira.Bnet;
import elvira.Elvira;
import elvira.Evidence;
import elvira.Finding;
import elvira.FiniteStates;
import elvira.Node;
import elvira.gui.InferencePanel;
import elvira.gui.NetworkFrame;

public class ChoiceInterestVariable extends JDialog {

private static final long serialVersionUID = 1L;
	
	private int i,j=0;

	private JPanel jContentPane = null;

	private JPanel jPanel = null;

	private JButton jButton2 = null;

	private JButton jButton3 = null;

	private JScrollPane jScrollPane = null;

	private JButton jButton4 = null;

	private JComboBox jComboBox1 = null;

	private JPanel jPanel1 = null;

	private JButton jButton = null;

	private JList jList = null;

	private JComboBox jComboBox2 = null;

	private JScrollPane jScrollPane1 = null;

	private JList jList1 = null;

	private JLabel jLabel = null;

	private JLabel jLabel1 = null;

	private JPanel jPanel2 = null;

	private JScrollPane jScrollPane2 = null;

	private JTable jTable = null;
	
	private InferencePanel infpanel;

    private Case cedit;
    
    private Evidence evidence;
    
    private CasesList casesl;
    
    private Bnet bnet;
    
    private ResourceBundle analisysBundle;
    
    private String[][] datos;
    
    private AnalisysMainFrame ana;
    
    private JTable tabla_datos=null;

	/**
	 * This is the default constructor
	 */
	public ChoiceInterestVariable(NetworkFrame f,JTable tabla) {
		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: analisysBundle = ResourceBundle.getBundle ("elvira/localize/SensitivityAnalisys");            
			                         break;
		   case Elvira.SPANISH:  analisysBundle = ResourceBundle.getBundle ("elvira/localize/SensitivityAnalisys_sp");   
			                         break;		                         
		}	
		infpanel=f.getInferencePanel();//Panel main de Elvira
		bnet=infpanel.getBayesNet();//Red en estudio
		casesl=infpanel.getCasesList();//lista de casos asociada a la red
		cedit=casesl.getCurrentCase();//Obtener el caso Activo
		datos= new String[bnet.getNodeList().size()*2][bnet.getNodeList().size()];
		tabla_datos=tabla;
		initialize();
	}
	

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(570, 404);
		this.setModal(true);
		this.setLocation(new Point(200, 200));
		this.setResizable(false);
		this.setContentPane(getJContentPane());
		this.setTitle(Elvira.localize(analisysBundle,"Interest.Probability"));
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getJButton2(), null);
			jContentPane.add(getJButton3(), null);
			jContentPane.add(getJPanel1(), null);
			jContentPane.add(getJPanel2(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(197, 22, 61, 15));
			jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel1.setText(Elvira.localize(analisysBundle,"Value"));
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(22, 22, 84, 16));
			jLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel.setText("Variable");
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(11, 17, 333, 202));
			jPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Añadir", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Consolas", Font.PLAIN, 14), SystemColor.inactiveCaption)));
			jPanel.add(getJScrollPane(), null);
			jPanel.add(getJButton4(), null);
			jPanel.add(getJComboBox1(), null);
			jPanel.add(jLabel, null);
			jPanel.add(jLabel1, null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setBounds(new Rectangle(359, 293, 193, 31));
			jButton2.setMnemonic(KeyEvent.VK_UNDEFINED);
			jButton2.setEnabled(true);
			jButton2.setText(Elvira.localize(analisysBundle,"Accept"));
			jButton2.setEnabled(false);
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);	
					setVisible(false);
					
					
				}		
			});	
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					
					setVisible(false);
					
				}
			});
		}
		return jButton2;
	}

	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setBounds(new Rectangle(358, 332, 193, 31));
			jButton3.setText(Elvira.localize(analisysBundle,"Cancel"));
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					dispose();
				}
			});
			
		}
		return jButton3;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane(getJList());
			jScrollPane.setBounds(new Rectangle(131, 39, 189, 148));
			jScrollPane.setViewportView(getJList());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jButton4	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton4() {
		if (jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setBounds(new Rectangle(9, 165, 113, 23));
			jButton4.setText(Elvira.localize(analisysBundle,"Insert"));
			jButton4.setEnabled(false);
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					datos[0][0]=(String)jComboBox1.getSelectedItem();
					datos[0][1]=(String)jList.getSelectedValue();
					
					//COLUMNA 1 Valores
					jTable.setValueAt(getJList().getSelectedValue(),0,1);
					//Columna 2  Estados
					jTable.setValueAt(getJList().getSelectedIndex(),0 ,2);
					//Columna 0 Variables
					jTable.setValueAt(jComboBox1.getSelectedItem(),0,0);
					
					jComboBox2.addItem(jComboBox1.getSelectedItem());
					jComboBox1.removeItem(jComboBox1.getSelectedItem());
					
					//i++;
					for(int j=0;j<3;j++){	
						for(int h=1;h<jTable.getRowCount();h++){
							if(!(jTable.getValueAt(h,j)==null)){
								if(jTable.getValueAt(h-1,j)==null){
									jTable.setValueAt(jTable.getValueAt(h,j), h-1,j);
									jTable.setValueAt(null, h, j);
									jTable.repaint();
									jTable.updateUI();
								}
							}
						}
					}
					jButton.setEnabled(true);
					jButton2.setEnabled(true);
					jButton4.setEnabled(false);
				}		
			});	
		}
		return jButton4;
	}

	/**
	 * This method initializes jComboBox1	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox1() {
		if (jComboBox1 == null) {
			jComboBox1=fill_Jcombo1(cedit);
			
			jComboBox1.setBounds(new Rectangle(6, 43, 121, 28));
			jComboBox1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//Metodo para actualizar la lista
					if(jComboBox1.getItemCount()==0){
						DefaultListModel listaaux = new DefaultListModel();
						listaaux=(DefaultListModel)jList.getModel();
						listaaux.removeAllElements();
						jList.setModel(listaaux);
					}else{
					fill_Jlist1(cedit);
					}	
				}
			});
			
		}
		return jComboBox1;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(null);
			jPanel1.setBounds(new Rectangle(358, 16, 201, 249));
			jPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Eliminar", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Palatino Linotype", Font.PLAIN, 14), SystemColor.inactiveCaption)));
			jPanel1.add(getJButton(), null);
			jPanel1.add(getJComboBox2(), null);
			jPanel1.add(getJScrollPane1(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(34, 217, 138, 21));
			jButton.setText(Elvira.localize(analisysBundle,"Erase.Variable"));
			jButton.setEnabled(false);
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object element=jComboBox2.getSelectedItem();
					Object element2=jList1.getSelectedValue();
					jComboBox1.addItem(element);
					for(int j=0;j<jTable.getRowCount();j++){	
						if(element.equals(jTable.getValueAt(j,0))&& element2.equals(jTable.getValueAt(j,1))){
							jComboBox2.removeItem(element);
							DefaultListModel lista=(DefaultListModel)jList1.getModel();
							lista.removeAllElements();
							if(jList1 != null)jList1.removeAll();
							//jList1.setModel(lista);
							jTable.setValueAt(null,j,0);
							jTable.setValueAt(null,j,1);
							jTable.setValueAt(null,j,2);
													
						}
				}
				for(int j=0;j<3;j++){	
					for(int h=1;h<jTable.getRowCount();h++){
						if(!(jTable.getValueAt(h,j)==null)){
							if(jTable.getValueAt(h-1,j)==null){
								jTable.setValueAt(jTable.getValueAt(h,j), h-1,j);
								jTable.setValueAt(null, h, j);
								jTable.repaint();
								jTable.updateUI();
							}
						}
					}
				}
				if(jComboBox2.getItemCount()==0){
					jButton.setEnabled(false);
					jButton2.setEnabled(false);	
				}
				jButton4.setEnabled(true);
			}	
				
			});	
			
		}
		return jButton;
	}

	/**
	 * This method initializes jList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJList() {
		if (jList == null) {
				DefaultListModel  lista1 = new DefaultListModel();
			jList = new JList(lista1);
			
		}
		return jList;
	}

	/**
	 * This method initializes jComboBox2	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox2() {
		if (jComboBox2 == null) {
			jComboBox2 = new JComboBox();
			jComboBox2.setBounds(new Rectangle(11, 25, 180, 22));
			jComboBox2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					DefaultListModel nuevo=(DefaultListModel)jList1.getModel();
					nuevo.removeAllElements();
					jList1.setModel(nuevo);
					for(int i=0;i<datos.length;i++){
						//nuevo.removeAllElements();
						if(jComboBox2.getSelectedItem()==datos[i][0])
							System.out.println(datos[i][1]);
							nuevo.addElement(datos[i][1]);
					}
					jList1.setModel(nuevo);
					jList1.setSelectedIndex(0);
				}
				
			});
											
		}			
		
		return jComboBox2;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane(getJList1());
			jScrollPane1.setBounds(new Rectangle(13, 55, 176, 157));
			jScrollPane1.setVisible(true);
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes jList1	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJList1() {
		if (jList1 == null) {
			DefaultListModel lista1 = new DefaultListModel();			
			jList1 = new JList(lista1);
			
			
		}
		return jList1;
	}


	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 0;
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setBounds(new Rectangle(15, 231, 325, 134));
			jPanel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createTitledBorder(null, "Variables en Estudio", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.inactiveCaption))));
			jPanel2.add(getJScrollPane2(), gridBagConstraints);
		}
		return jPanel2;
	}


	/**
	 * This method initializes jScrollPane2	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setVisible(true);
			jScrollPane2.setViewportView(getJTable());
			jScrollPane2.setToolTipText("Variables bajo estudio ");
			
		}
		return jScrollPane2;
	}


	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	protected JTable getJTable() {
		if (jTable == null) {
			String[] nombres = {"Variable","Valor","Estado"};
			DefaultTableModel modelo = new DefaultTableModel(nombres,5);
			jTable = new JTable(modelo);
			jTable.setEnabled(false);
			
		}
		return jTable;
	}
	
//	Metodo para rellenar el JComboBox1
	private JComboBox fill_Jcombo1(Case c){
		int cont=0;
		JComboBox jb;
		DefaultComboBoxModel combovarmodel=new DefaultComboBoxModel();
		boolean esta=false;
		
		if(tabla_datos==null){//Analisis SIN Evidencia
			cont=0;
			for (int n=0; n<bnet.getNodeList().size(); n++){
				combovarmodel.addElement(bnet.getNodeList().elementAt(n).getNodeString(true));
			}
		}else{//Analisis CON Evidencia
			for(int i=0;i<tabla_datos.getRowCount();i++){
				if((tabla_datos.getValueAt(i,0)!=null)){
					cont++;
				}
			}
		
			for (int n=0; n<bnet.getNodeList().size(); n++){
				for(int g=0;g<cont;g++){	
					if(!(bnet.getNodeList().elementAt(n).getNodeString(true)).equals(tabla_datos.getValueAt(g,0))){
						esta=false;
					}else{
						esta=true;
						g=g+bnet.getNodeList().size();
					}
				}
				if(esta==false){
					combovarmodel.addElement(bnet.getNodeList().elementAt(n).getNodeString(true));
				}
			}	
		}
		jb= new JComboBox(combovarmodel);
		return jb;
	}
	
//	Metodo para rellenar la Lista 1 de Insertar
	public void fill_Jlist1(Case c){
		
		DefaultListModel lista= new DefaultListModel();
		
		for (int n=0; n<bnet.getNodeList().size()|| jComboBox1.getItemCount()==0; n++){
			
			if (bnet.getNodeList().elementAt(n).getClass()==FiniteStates.class){
				try{
				if(jComboBox1.getSelectedItem().equals(bnet.getNodeList().elementAt(n).getNodeString(true))){
				
										
					FiniteStates fs=(FiniteStates)bnet.getNodeList().elementAt(n);
					for(int j=0;j<fs.getNumStates();j++){ 
						lista.addElement(fs.getState(j));
						
					}
					getJList().setModel(lista);
					jButton4.setEnabled(true);
				}else{
					//lista.removeAllElements();
					getJList().setModel(lista);
				}
				}catch(NullPointerException e){
					jComboBox1.setEnabled(false);
				}
			}	
		}	
		getJList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getJList().setLayoutOrientation(jList.HORIZONTAL_WRAP);
		getJList().setSelectedIndex(0);
	}

}  //  @jve:decl-index=0:visual-constraint="10,11"