package elvira.gui.explication;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import elvira.gui.NetworkFrame;
import java.awt.Point;

public class Start_Analisis extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JLabel pregunta = null;

	private JLabel imagen1 = null;

	private JButton botoncon = null;

	private JButton botonsin = null;

	private JButton botonsalir = null;
	
	private char tipo;
	//If tipo equals true is "with"
	//in other case is "without"

	/**
	 * @param owner
	 */
	public Start_Analisis(NetworkFrame owner) {
		//super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(534, 200);
		this.setLocation(new Point(225, 300));
		this.setTitle("Seleccione tipo de Analisis");
		this.setModal(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			imagen1 = new JLabel();
			imagen1.setBounds(new Rectangle(15, 49, 89, 109));
			imagen1.setIcon(new ImageIcon(getClass().getResource("/elvira/gui/images/escudo.jpg")));
			imagen1.setText("JLabel");
			pregunta = new JLabel();
			pregunta.setBounds(new Rectangle(65, 9, 394, 32));
			pregunta.setHorizontalAlignment(SwingConstants.CENTER);
			pregunta.setFont(new Font("Lucida Console", Font.BOLD | Font.ITALIC, 14));
			pregunta.setText("¿Cómo desea realizar el análisis?");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(pregunta, null);
			jContentPane.add(imagen1, null);
			jContentPane.add(getBotoncon(), null);
			jContentPane.add(getBotonsin(), null);
			jContentPane.add(getBotonsalir(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes botoncon	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBotoncon() {
		if (botoncon == null) {
			botoncon = new JButton();
			botoncon.setBounds(new Rectangle(150, 65, 159, 38));
			botoncon.setToolTipText("Pulse para realizar el analisis con evidencia");
			botoncon.setIcon(new ImageIcon(getClass().getResource("/elvira/gui/images/inicial.JPG")));
			botoncon.setHorizontalAlignment(SwingConstants.LEFT);
			botoncon.setHorizontalTextPosition(SwingConstants.LEFT);
			botoncon.setText("Con Evidencia");
			botoncon.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setTipo('c');
					dispose();
				}
			});
		}
		return botoncon;
	}

	/**
	 * This method initializes botonsin	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBotonsin() {
		if (botonsin == null) {
			botonsin = new JButton();
			botonsin.setBounds(new Rectangle(149, 115, 160, 38));
			botonsin.setIcon(new ImageIcon(getClass().getResource("/elvira/gui/images/inicial.JPG")));
			botonsin.setHorizontalAlignment(SwingConstants.LEFT);
			botonsin.setHorizontalTextPosition(SwingConstants.LEFT);
			botonsin.setText("Sin   Evidencia");
			botonsin.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setTipo('s');
					dispose();
				}
			});
		}
		return botonsin;
	}

	/**
	 * This method initializes botonsalir	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBotonsalir() {
		if (botonsalir == null) {
			botonsalir = new JButton();
			botonsalir.setBounds(new Rectangle(358, 74, 126, 60));
			botonsalir.setIcon(new ImageIcon(getClass().getResource("/elvira/gui/images/UCLM.jpg")));
			botonsalir.setText("Salir");
			botonsalir.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setTipo('e');
					dispose();
				}
			});
		}
		return botonsalir;
	}
	
	//Method to return the "tipo" boolean´s value, 
	//this value indicate the choice of analisys
	protected char getTipo(){
		return tipo;
	}
	protected void setTipo(char valor){
		tipo=valor;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
