package elvira.gui.explication;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.geom.Line2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

public class GraphPaint extends JPanel{

	private DataGraphs dg= new DataGraphs();
	
	private String var_interes;
	
	private String var_interes_valor;
	
	private String parametro_estudio;
	
	private String parametro_estudio_valor;
	
	GraphPaint(DataGraphs dg,String v_i,String v_i_v, String p_e,String p_e_v){
		var_interes=v_i;
		var_interes_valor=v_i_v;
		parametro_estudio=p_e;
		parametro_estudio_valor=p_e_v;
		this.dg=dg;
		this.setBounds(new Rectangle(1,1, 640, 295));
		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "Gráficos", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 16), SystemColor.inactiveCaption), BorderFactory.createBevelBorder(BevelBorder.RAISED))));
	}

	public void paintComponent(Graphics g){
				super.paintComponent(g);

				Graphics2D g1 = (Graphics2D)g;
				float[] dashPattern = {5, 2, 5,2}; 
				g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				//Eje de Coordenadas
				g1.setPaint(new Color(0,0,150)); 
				g1.setStroke(new BasicStroke(3.0f));
				g1.draw(new Line2D.Double(220,250, 420,250));
				g1.draw(new Line2D.Double(220,250, 220, 50));
				//Linea de Funcion
				g1.setColor(Color.RED);
				g1.setStroke(new BasicStroke(2.0f));
			    g1.draw(new Line2D.Double(dg.getParaInicial(),dg.getProbPrevia(),dg.getParaFinal(),dg.getProbPosterior()));
			    g1.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dashPattern,0));
			    //Valores de Probabilidad limites
			    g1.setStroke(new BasicStroke(3.0f));
			    g1.drawString("0",210,250);
			    g1.drawString("1",210,50);
			    g1.drawString("1",430,250);
			    g1.setColor(Color.BLACK);
			    
			    g1.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dashPattern,0));
			    g1.draw(new Line2D.Double(dg.getParaInicial(),dg.getProbPrevia(),dg.getParaInicial(),250));
			    
			    g1.drawString("3", (int)dg.getParaInicial(), 260);
			    g1.drawString("4", (int)dg.getParaFinal(), 260);
			    g1.draw(new Line2D.Double(dg.getParaFinal(),dg.getProbPosterior(),dg.getParaFinal(),250));
			 
			    g1.setColor(Color.GREEN);
			    g1.draw(new Line2D.Double(dg.getParaInicial(),dg.getProbPrevia(),220,dg.getProbPrevia()));
			    g1.drawString("1", 210, (int)dg.getProbPrevia());
			    g1.drawString("2", 210, (int)dg.getProbPosterior());
			    g1.draw(new Line2D.Double(dg.getParaFinal(),dg.getProbPosterior(),220,dg.getProbPosterior()));
			    
			    //Nombres a las coordenadas
			    g1.setStroke(new BasicStroke(3.0f));
			    g1.setColor(Color.BLACK);
			    g1.drawString("Parámetro a evaluar", 440,250 );
			    g1.drawString("Probabilidad de Interés",80,50);
			    
			    g1.setFont( new Font( "Comic Sans", Font.ITALIC, 13 ) );
			    g1.setColor(SystemColor.activeCaption);
			    g1.drawString("Variable de Interés :",460,50);
			    g1.drawString("Parámetro en Estudio :",460,100);
			    
			    g1.setColor(SystemColor.GREEN);
			    g1.drawString(var_interes,450,75);
			    g1.drawString(var_interes_valor,570,75);
			    g1.setColor(SystemColor.BLACK);
			    g1.drawString(parametro_estudio,450,125);
			    g1.drawString(parametro_estudio_valor,570,125);
	}
	
}

