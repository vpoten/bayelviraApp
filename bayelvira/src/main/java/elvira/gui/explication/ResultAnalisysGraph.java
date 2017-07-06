package elvira.gui.explication;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import elvira.Elvira;

import java.awt.event.KeyEvent;
import java.util.ResourceBundle;


public class ResultAnalisysGraph extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	
	private JPanel jPanel1 = null;

	private JPanel jPanel2 = null;

	private JLabel jLabel = null;

	private JButton jButton = null;
	
	private DataGraphs dg= new DataGraphs();

	private JLabel jLabel1 = null;

	private JLabel jLabel21 = null;

	private JLabel jLabel22 = null;

	private JLabel jLabel23 = null;

	private JLabel jLabel24 = null;

	private JLabel jLabel25 = null;

	private JLabel jLabel26 = null;

	private JLabel jLabel27 = null;

	private JButton jButton1 = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel4 = null;

	private JLabel jLabel5 = null;

	private JPanel jPanel = null;

	private JPanel jPanel3 = null;

	private JLabel jLabel6 = null;

	private JLabel jLabel7 = null;

	private JLabel jLabel8 = null;

	private JLabel jLabel9 = null;

	private JLabel jLabel10 = null;

	private JLabel jLabel11 = null;
	
	private String variable;
	
	private String variable_valor;
	
	private String parametro;

	private String parametro_valor;
	private ResourceBundle analisysBundle;

	/**
	 * Constructor
	 */
	public ResultAnalisysGraph(DataGraphs dg,String variable_i,String valor_i,String parametro_e,String parametro_valor_e) {
		super();
		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: analisysBundle = ResourceBundle.getBundle ("elvira/localize/SensitivityAnalisys");            
			                         break;
		   case Elvira.SPANISH:  analisysBundle = ResourceBundle.getBundle ("elvira/localize/SensitivityAnalisys_sp");   
			                         break;		                         
		}		
		this.dg=dg;
		variable=variable_i;
		variable_valor=valor_i;
		parametro=parametro_e;
		parametro_valor=parametro_valor_e;
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(646, 416);
		this.setModal(true);
		this.setLocation(new Point(150, 150));
		this.setContentPane(getJContentPane());
		this.setLayout(new BorderLayout());
		this.setResizable(false);
		this.setTitle(Elvira.localize(analisysBundle,"Graph.Result"));
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
			GraphPaint pg= new GraphPaint(dg,variable,variable_valor,parametro,parametro_valor);
			pg.setLayout(null);
			pg.setSize(new Dimension(640, 278));
			jContentPane.add(pg, null);
			jContentPane.add(getJPanel1(), null);
			jContentPane.add(getJPanel2(), null);
		}
		return jContentPane;
	}
	
	

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jLabel11 = new JLabel();
			jLabel11.setBounds(new Rectangle(239, 76, 119, 15));
			jLabel11.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabel11.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel11.setText("-------------");
			jLabel10 = new JLabel();
			jLabel10.setBounds(new Rectangle(238, 62, 121, 15));
			jLabel10.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel10.setFont(new Font("Dialog", Font.BOLD, 10));
			jLabel10.setText(Elvira.localize(analisysBundle,"Parameters"));
			jLabel9 = new JLabel();
			jLabel9.setBounds(new Rectangle(239, 51, 120, 11));
			jLabel9.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabel9.setForeground(Color.green);
			jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel9.setText("-------------");
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(238, 36, 121, 15));
			jLabel8.setFont(new Font("Dialog", Font.BOLD, 10));
			jLabel8.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel8.setText(Elvira.localize(analisysBundle,"Probabilities"));
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(239, 23, 119, 11));
			jLabel7.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabel7.setForeground(Color.red);
			jLabel7.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel7.setVerticalAlignment(SwingConstants.CENTER);
			jLabel7.setVerticalTextPosition(SwingConstants.CENTER);
			jLabel7.setText("-------------");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(238, 8, 121, 15));
			jLabel6.setFont(new Font("Dialog", Font.BOLD, 10));
			jLabel6.setText(Elvira.localize(analisysBundle,"Function.Sensitivity"));
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(142, 72, 20, 16));
			jLabel5.setText("(4):");
			jLabel5.setVisible(false);
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(142, 53, 21, 16));
			jLabel4.setHorizontalAlignment(SwingConstants.LEADING);
			//jLabel4.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel4.setFont(new Font("Dialog", Font.BOLD, 10));
			jLabel4.setText("(3):");
			//jLabel4.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jLabel4.setVisible(false);
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(142, 32, 18, 17));
			jLabel3.setForeground(Color.green);
			jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel3.setText("(2):");
			jLabel3.setVisible(false);
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(141, 10, 19, 17));
			jLabel2.setForeground(Color.green);
			jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel2.setText("(1):");
			jLabel2.setVisible(false);
			jLabel27 = new JLabel();
			jLabel27.setBounds(new Rectangle(170, 32, 51, 16));
			jLabel27.setForeground(SystemColor.activeCaption);
			
			jLabel26 = new JLabel();
			jLabel26.setBounds(new Rectangle(169, 12, 50, 16));
			jLabel26.setForeground(SystemColor.activeCaption);
			
			jLabel25 = new JLabel();
			jLabel25.setForeground(SystemColor.activeCaption);
			jLabel25.setBounds(new Rectangle(171, 73, 49, 15));
			
			jLabel24 = new JLabel();
			jLabel24.setForeground(SystemColor.activeCaption);
			jLabel24.setBounds(new Rectangle(172, 51, 48, 17));
			
			jLabel23 = new JLabel();
			jLabel23.setText(Elvira.localize(analisysBundle,"Probability.Later"));
			jLabel23.setBounds(new Rectangle(9, 33, 117, 16));
			jLabel23.setFont(new Font("Dialog", Font.BOLD, 10));
			jLabel23.setVisible(false);
			jLabel22 = new JLabel();
			jLabel22.setBounds(new Rectangle(8, 14, 122, 15));
			jLabel22.setText(Elvira.localize(analisysBundle,"Probability.Prior"));
			jLabel22.setFont(new Font("Dialog", Font.BOLD, 10));
			jLabel22.setVisible(false);
			jLabel21 = new JLabel();
			jLabel21.setText("Parámetro Final:");
			jLabel21.setBounds(new Rectangle(9, 73, 130, 14));
			jLabel21.setFont(new Font("Dialog", Font.BOLD, 10));
			jLabel21.setVisible(false);
			jLabel1 = new JLabel();
			jLabel1.setText(Elvira.localize(analisysBundle,"Initial.Parameter"));
			jLabel1.setBounds(new Rectangle(10, 53, 115, 16));
			jLabel1.setFont(new Font("Dialog", Font.BOLD, 10));
			jLabel1.setVisible(false);
			jPanel1 = new JPanel();
			jPanel1.setLayout(null);
			jPanel1.setBounds(new Rectangle(122, 283, 514, 98));
			jPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createLineBorder(SystemColor.windowText, 3))), null));
			jPanel1.add(getJButton(), null);
			jPanel1.add(jLabel22, null);
			jPanel1.add(jLabel26, null);
			jPanel1.add(jLabel27, null);
			jPanel1.add(jLabel2, null);
			jPanel1.add(jLabel3, null);
			jPanel1.add(jLabel4, null);
			jPanel1.add(jLabel5, null);
			jPanel1.add(getJPanel(), null);
			jPanel1.add(getJPanel3(), null);
			jPanel1.add(jLabel6, null);
			jPanel1.add(jLabel7, null);
			jPanel1.add(jLabel8, null);
			jPanel1.add(jLabel9, null);
			jPanel1.add(jLabel10, null);
			jPanel1.add(jLabel11, null);
			jPanel1.add(jLabel21, null);
			jPanel1.add(jLabel24, null);
			jPanel1.add(jLabel25, null);
			jPanel1.add(getJButton1(), null);
			jPanel1.add(jLabel23, null);
			jPanel1.add(jLabel1, null);
			
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(6, 6, 104, 73));
			jLabel.setIcon(new ImageIcon("C:/Documents and Settings/Alberto/Mis documentos/Mis imágenes/iconografico.JPG"));
			jLabel.setText("JLabel");
			jPanel2 = new JPanel();
			jPanel2.setLayout(null);
			jPanel2.setBounds(new Rectangle(3, 296, 116, 85));
			jPanel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createLineBorder(SystemColor.windowText, 3)));
			jPanel2.add(jLabel, null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(375, 25, 128, 51));
			jButton.setText(Elvira.localize(analisysBundle,"Exit.Analisys"));
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText(Elvira.localize(analisysBundle,"Data"));
			jButton1.setBounds(new Rectangle(11, 11, 106, 20));
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					jButton1.setVisible(false);
					jLabel1.setVisible(true);
					jLabel21.setVisible(true);
					jLabel22.setVisible(true);
					jLabel23.setVisible(true);
					jLabel2.setVisible(true);
					jLabel3.setVisible(true);
					jLabel4.setVisible(true);
					jLabel5.setVisible(true);
					jLabel27.setText(Double.toString(dg.getPPosterior()));
					jLabel26.setText(Double.toString(dg.getPPrevia()));
					jLabel25.setText(Double.toString(dg.getPFinal()));
					jLabel24.setText(Double.toString(dg.getPInicial()));
				}
			});
			
		}
		return jButton1;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBounds(new Rectangle(226, 7, 12, 84));
			jPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SystemColor.activeCaption, 2), new SoftBevelBorder(SoftBevelBorder.LOWERED)));
			jPanel.setBackground(Color.black);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GridBagLayout());
			jPanel3.setBounds(new Rectangle(360, 7, 12, 83));
			jPanel3.setBackground(SystemColor.windowText);
			jPanel3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SystemColor.activeCaption, 2), BorderFactory.createBevelBorder(BevelBorder.RAISED)));
		}
		return jPanel3;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
