/* VisualExplanationFStates */

package elvira.gui.explication;


import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.Vector;

/** This class defines a panel for drawing an expanded node
 * 
 * @author Carmen
 *
 */

public class VisualExplanationFStates extends JPanel{
   private int weight, height;

   public static final int DECIMAL=0;
   public static final int LOGARITMO=1;
   public static final Color green=new Color(0,153,51);
   public static final Color orange=new Color(255,153,51);
   public static final Color red=new Color(255,0,51);
   public static final Color blue=new Color(0,102,255);
   public static final Color pink=new Color(255,153,153);
   public static final Color purple=new Color(254,101,255);
   public static final Color[] colours={green, red, blue, pink, purple, orange};

   private ExplanationFStates n;
   private int currentcase;
   private int activecase;
   private double[] priori;
   private double[] posteriori;
   private Vector name_states;
   private VisualFStates[] visualfstates;//for the graphical representation of each state of the node
   private int maxst;
   public int numstates;
   public int numdistributions; // to know the number of distributions created=number of elements of the vector

   private boolean paintprior;
   private boolean saved;
   private boolean log=false; //por defecto se pintarán en s. decimal
   private int posx,posy; //positions where they are going to be painted
   private Font fnum = new Font("Helvetica", Font.PLAIN, 9);
   private Font fstates = new Font("Times New Roman", Font.PLAIN, 11);
   private Font fmaxstates = new Font("Times New Roman", Font.BOLD, 11);
   private int heightbars=VisualFStatesDistribution.height;
   private int dstates=VisualFStates.distestados;//distance between states

   public VisualExplanationFStates(ExplanationFStates nodo){
     //crea una ventana con los datos del nodo que se le pasa como parámetro
        weight=220;

	if (nodo == null)
	  return;

        n=nodo;
        name_states=n.getStates();
        numstates=n.getNumStates();
        posx=1;
        posy=12;
        numdistributions=0;
        if (nodo.getCasesList().getNumStoredCases()!=0){
            currentcase=nodo.getCasesList().getNumCurrentCase();
            activecase=nodo.getCasesList().getNumActiveCase();
            priori=n.getPriori();
            visualfstates=new VisualFStates[numstates];
            for (int i=0; i<numstates; i++){
                 visualfstates[i]=new VisualFStates();
                 VisualFStatesDistribution vfsd=new VisualFStatesDistribution(priori[i],0);
                 visualfstates[i].addVfstatesdist(vfsd);
            }
            numdistributions=n.getCasesList().getNumStoredCases();
        }
        this.setPreferredSize(new Dimension(weight, height));
        paintprior=(nodo.getCasesList().getNumStoredCases()!=0 && nodo.getCasesList().getCaseNum(0).getIsShown());
        saved=(nodo.getCasesList().getNumStoredCases()!=0);
     }

     public VisualExplanationFStates (Vector estados) {
        weight=220;
        name_states=estados;
        numstates=estados.size();
        posx=1;
        posy=12;
        numdistributions=0;
        this.setPreferredSize(new Dimension(weight, height));
        paintprior=true;
        saved=false;
     }

     /**
      * Returns the kind of font for the numbers in the expanded nodes
      */

     public Font getNumFont() {
        return fnum;
     }

     public Dimension getMinimumSize() {
        return getPreferredSize();
     }

     public Dimension getPreferredSize() {
         return new Dimension(150, super.getPreferredSize().height);
     }

     public Dimension getMaximumSize() {
         return getPreferredSize();
     }

     void setAltura(int w){
        height=w;
     }

     void setAnchura(int h){
        weight=h;
     }

    public void setGuardadas(boolean b){
        saved=b;
    }

    public void setPriori(boolean b){
        paintprior=b;
    }
    
    private String withoutQm(String s)
    {
            if (s.substring(0,1).equals("\""))
            {
                return (s.substring(1,s.length()-1));
            }
            else {
                return s;
            }
    }

    public void paintStatesNames(Graphics2D g2, int px, int py){
        int posn=py;
        //obtiene en maxst el índice del estado de mayor probabilidad en el caso activo
        if (n==null || n.getCasesList().getNumStoredCases()==0 /*|| n==null || (currentcase==0)*/)
           maxst = -1;
        else{
           maxst=n.getMaxProbState(n.getDistribution(currentcase));
           if (!n.getCasesList().getCurrentCase().getPropagated())
               maxst=-1;
        }
        for (int i=0; i<numstates; i=i+1){
             g2.setColor(Color.black);
             Rectangle clip = g2.getClipBounds();
             
             /* Jorge-PFC 27/12/2005, el código anterior no parecía tener sentido: permitía dibujar fuera del canvas
              * Jorge-PFC 07/01/2006 ampliar en 1 el area de clipping para q se vea bien el recuadro
              */
             Rectangle rt= clip.intersection(new Rectangle(px-1,posn-8,45+1,10+1));
             g2.setClip(rt);
             
             if (i==maxst){
                g2.setFont(fstates);
                g2.setPaint(n.getCasesList().getCaseNum(currentcase).getColor());
                g2.draw(new Rectangle2D.Double(px-1,posn-8,45,10));
             }
             else g2.setFont(fstates);
             g2.drawString(withoutQm((String)name_states.elementAt(i)),px,posn);
             //para que resalte el valor más probable del nodo

             if (numdistributions>0)
                posn=posn+(dstates+(heightbars*(numdistributions)));
                else posn=posn+(5+dstates+(heightbars*(numdistributions)));
             
             // Jorge-PFC 05/01/2006 reestablecer el area de clipping original 
             g2.setClip(clip);	             
        }
    }

    public void paintpriori(Graphics g, int px, int py){
        VisualFStatesDistribution vfsd;
        int posg=py;
        int posn=py;
        g.setFont(fnum);
        maxst=n.getMaxProbState(n.getDistribution(0));
        for (int i=0; i<numstates; i++){
             vfsd=(VisualFStatesDistribution)visualfstates[i].elementAt(0);
             vfsd.setColor(n.getCasesList().getCaseNum(0).getColor());
             if (log)
                vfsd.setKindofunit(LOGARITMO);
                else vfsd.setKindofunit(DECIMAL);

             //si el caso activo es el 0, es el priori, lo que implica que se pintarán las prob.
             //a priori al lado de cada barra. Si no, no, pues solo se pintan las prob del caso
             //activo.

             if (currentcase==0)
                vfsd.paintFSD(g,px,posg,true);
                else vfsd.paintFSD(g,px,posg,false);
             if (numdistributions>0)
                posg=posg+(dstates+(heightbars*(numdistributions)));
                else posg=posg+(5+dstates+(heightbars*(numdistributions)));
            }
    }

    public void paintsaved(Graphics2D g, int px, int py){
        VisualFStatesDistribution vfsd;
        int posg;
        int numd=0;
        for (int p=1;p<numdistributions; p++){
            if (n.getCasesList().getCaseNum(p).getIsShown() &&
               (n.getCasesList().getCaseNum(p).getPropagated() || n.getCasesList().getCaseNum(p).getIsObserved(n))){
                numd++;
                posg=py+(heightbars*(numd));
                double[] post=n.getDistribution(p);
                for (int i=0; i<numstates; i=i+1){
                     vfsd=new VisualFStatesDistribution(post[i],p);
                     vfsd.setColor(n.getCasesList().getCaseNum(p).getColor());
                     if (p==currentcase)
                        vfsd.paintFSD(g,px,posg,true);
                        else vfsd.paintFSD(g,px,posg,false);
                     if (numdistributions>0)
                         posg=posg+(dstates+(heightbars*(numdistributions)));
                         else posg=posg+(dstates+(heightbars*(numdistributions)));
                }
            }
        }
    }


     public void paintExplanation (Graphics2D g, int posx, int posy){
        //pinta los nombres de los estados y las probabilidades a priori siempre
        paintStatesNames(g,posx,posy);
        if (paintprior)
            paintpriori(g, posx+50, posy);

        if (saved){
            paintsaved(g,posx+50, posy);
        }
     }//fin del método paint

}//fin de la clase

