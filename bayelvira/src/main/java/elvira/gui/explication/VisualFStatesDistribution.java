/* VisualFStatesDistribution.java */



package elvira.gui.explication;



import java.awt.*;

import java.text.*;



public class VisualFStatesDistribution{

    /* cada estado se caracteriza por su nombre, posición en la pantalla y color.

     */





    public static final int weight=3;//anchura de la barra para pintarlo. Por defecto, 3

    public static final int height=weight+1; //altura de la zona en la que se pinta la barra



    private int posx,posy;//coordenadas desde donde se va a dibujar



    private Color color;//color en el que se va a dibujar

    private boolean visible;//determinará si se hace visible o no

    private double value;//valor de la probabilidad

    private int numofdist;

    private int kindofunit;//0 (decimal) 1 (logaritmo)



    public VisualFStatesDistribution(double v, int n){

        numofdist=n;

        visible=true;

        value=v;

        kindofunit=VisualExplanationFStates.DECIMAL;//por defecto, se expresarán en decimal
    }


    public void setColor(Color c){
        color=c;
    }



    public void setValue(double v){

        value=v;

    }



    public void setVisible(boolean v){

        visible=v;

    }



    public void setKindofunit(int u){

        kindofunit=u;

    }



    public Color getColor(){

        return color;

    }



    public int getWeight(){

        return weight;

    }



    public double getValue(){

        return value;

    }



    public boolean getVisible(){

        return visible;

    }



    public int getKindofunit(){

        return kindofunit;

    }



    public void paintFSD(Graphics g, int posx, int posy, boolean b){
        //el valor booleano sirve para determinar si se pinta el valor de la prob o no
	NumberFormat nf=new DecimalFormat(elvira.Elvira.getElviraFrame().getVisualPrecision());
        if (getVisible()){
           int w=getWeight();
           g.setColor(getColor());
           if (kindofunit==VisualExplanationFStates.LOGARITMO){
               if (getValue()!=0.0){
                  //lo de poner posy-5 es para que los nºs y las letras queden centrados con las barras
                  g.drawRect(posx, posy-5, (int)Math.log(getValue()), w);
                  g.fillRect(posx, posy-5, (int)Math.log(getValue()), w);
               }
               if (b) g.drawString(String.valueOf((int)Math.log(getValue())),posx+85,posy);
           }
           else {
                 if (getValue()!=0.0){
                    g.drawRect(posx, posy-5, (int)(getValue()*80), w);
                    g.fillRect(posx, posy-5, (int)(getValue()*80), w);
                 }
                 if (b) g.drawString(String.valueOf(nf.format(getValue())),posx+85,posy);
           }
        }
    }


}