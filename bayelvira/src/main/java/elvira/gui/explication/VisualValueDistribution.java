/*
 * Created on 02-feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package elvira.gui.explication;

import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import elvira.inference.super_value.CooperPolicyNetwork;

/**
 * @author Manolo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VisualValueDistribution {
	
	  /* cada estado se caracteriza por su nombre, posición en la pantalla y color.

     */





    public static final int weight=3;//anchura de la barra para pintarlo. Por defecto, 3

    public static final int height=weight+1; //altura de la zona en la que se pinta la barra



    private int posx,posy;//coordenadas desde donde se va a dibujar



    private Color color;//color en el que se va a dibujar

    private boolean visible;//determinará si se hace visible o no

    private double valueU;//valor de la utilidad
    
    private double normalizedValueU; //probabilityOfCooper
    
    private double range;//range of the utilities

    private int numofdist;

    private int kindofunit;//0 (decimal) 1 (logaritmo)

	/**
	 * @param maxOfUtilities
	 * @param priori
	 * @param i
	 */
	public VisualValueDistribution(double util, int n,double min, double max) {

		numofdist=n;

        visible=true;

        valueU=util;
        normalizedValueU = CooperPolicyNetwork.directCooperTransformation(util,min,max);
        kindofunit=VisualExplanationFStates.DECIMAL;//por defecto, se expresarán en decimal

		
		// TODO Auto-generated constructor stub
	}
	
	  
	
	  public void paintValueDistribution(Graphics g, int posx, int posy, boolean b){
        //el valor booleano sirve para determinar si se pinta el valor de la utilidad o no
	  	
	NumberFormat nf=new DecimalFormat(elvira.Elvira.getElviraFrame().getVisualPrecision());
        if (isVisible()){
           int w=getWeight();
           g.setColor(getColor());
           if (kindofunit==VisualExplanationFStates.LOGARITMO){
               if (normalizedValueU!=0.0){
                  //lo de poner posy-5 es para que los nºs y las letras queden centrados con las barras
                  g.drawRect(posx, posy-5, (int)Math.log(normalizedValueU), w);
                  g.fillRect(posx, posy-5, (int)Math.log(normalizedValueU), w);
               }
               if (b) g.drawString(String.valueOf((int)Math.log(normalizedValueU)),posx+85,posy);
           }
           else {
                 if (normalizedValueU!=0.0){
                    g.drawRect(posx, posy-5, (int)(normalizedValueU*80), w);
                    g.fillRect(posx, posy-5, (int)(normalizedValueU*80), w);
                 }
                 if (b) g.drawString(String.valueOf(nf.format(valueU)),posx+85,posy);
           }
        }
    }



	/**
	 * @return
	 */
	private int getWeight() {
		// TODO Auto-generated method stub
		return weight;
	}



	
	/**
	 * @return Returns the color.
	 */
	public Color getColor() {
		return color;
	}
	
	
	/**
	 * @return Returns the visible.
	 */
	public boolean isVisible() {
		return visible;
	}
	/**
	 * @return Returns the normalizedValueU.
	 */
	public double getNormalizedValueU() {
		return normalizedValueU;
	}
	/**
	 * @param color The color to set.
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	/**
	 * @return Returns the kindofunit.
	 */
	public int getKindofunit() {
		return kindofunit;
	}
	/**
	 * @param kindofunit The kindofunit to set.
	 */
	public void setKindofunit(int kindofunit) {
		this.kindofunit = kindofunit;
	}
}
