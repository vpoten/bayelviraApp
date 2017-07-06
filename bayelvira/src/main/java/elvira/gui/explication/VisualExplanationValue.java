/*
 * Created on 02-feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package elvira.gui.explication;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.JPanel;

import elvira.Node;
import elvira.inference.super_value.CooperPolicyNetwork;

/**
 * @author Manuel Luque
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VisualExplanationValue extends JPanel {
	
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

	   private ExplanationValueNode n;
	   private int currentcase;
	   private int activecase;
	   private double priori;
	   private double[] posteriori;
	   private String name_state;
	   //private VisualValueNode visualValueNode;//for the graphical representation of each state of the node
	   VisualValueDistribution visualvalued;
	   private int maxst;
	   public int numstates;
	   public int numdistributions; // to know the number of distributions created=number of elements of the vector

	   private boolean paintprior;
	   private boolean saved;
	   private boolean log=false; //por defecto se pintarán en s. decimal
	   private int posx,posy; //positions where they are going to be painted
	   private Font fnum = new Font("Helvetica", Font.PLAIN, 9);
	   private Font fontOfStates = new Font("Times New Roman", Font.PLAIN, 11);
	   private Font fmaxstates = new Font("Times New Roman", Font.BOLD, 11);
	   private int heightbars=VisualFStatesDistribution.height;
	   private int dstates=VisualFStates.distestados;//distance between states
	   //The maximum utility and the minimum utility for this node
	   private double minOfUtilities;
	   private double maxOfUtilities;

	   public VisualExplanationValue(ExplanationValueNode nodo){
	   		CooperPolicyNetwork cpn;
	   		Node expandedNode;
	   	
	     //crea una ventana con los datos del nodo que se le pasa como parámetro
	        weight=220;

		if (nodo == null) return;
		else{

	        n=nodo;
	        name_state="EU";
	        cpn = nodo.getIDiagram().getCpn();
	        expandedNode = nodo.getNode();
	        //rangeOfUtilities = cpn.getRangeOfUtility(expandedNode);
	        minOfUtilities = cpn.getMinimumUtility(expandedNode);
	        maxOfUtilities = cpn.getMaximumUtility(expandedNode);
	        
	        posx=1;
	        posy=12;
	        numdistributions=0;
	        if (nodo.getCasesList().getNumStoredCases()!=0){
	            currentcase=nodo.getCasesList().getNumCurrentCase();
	            activecase=nodo.getCasesList().getNumActiveCase();
	            priori=n.getPrioriUtility();
	            visualvalued=new VisualValueDistribution(priori,0,minOfUtilities,maxOfUtilities);
	            numdistributions=n.getCasesList().getNumStoredCases();
	        }
	        this.setPreferredSize(new Dimension(weight, height));
	        paintprior=(nodo.getCasesList().getNumStoredCases()!=0 && nodo.getCasesList().getCaseNum(0).getIsShown());
	        saved=(nodo.getCasesList().getNumStoredCases()!=0);
		}//else
	 }//end of method

	
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

	    public void paintStateName(Graphics2D g2, int px, int py){
	        int posn=py;
	        //obtiene en maxst el índice del estado de mayor probabilidad en el caso activo

	             g2.setColor(Color.black);
	             Rectangle clip = g2.getClipBounds();
	             
	             /* Jorge-PFC 27/12/2005, el código anterior no parecía tener sentido: permitía dibujar fuera del canvas
	              * Jorge-PFC 07/01/2006 ampliar en 1 el area de clipping para q se vea bien el recuadro
	              */
	             Rectangle rt= clip.intersection(new Rectangle(px-1,posn-8,45+1,10+1));
	             g2.setClip(rt);

	             g2.setFont(fontOfStates);
                 g2.setPaint(n.getCasesList().getCaseNum(currentcase).getColor());
                 g2.draw(new Rectangle2D.Double(px-1,posn-8,45,10));
	             g2.drawString(name_state,px,posn);
	             //para que resalte el valor más probable del nodo

	             // Jorge-PFC 05/01/2006 reestablecer el area de clipping original 
	             g2.setClip(clip);	             
	    }

	    public void paintpriori(Graphics g, int px, int py){
	        
	        int posg=py;
	        
	        g.setFont(fnum);
	       // maxst=n.getMaxProbState(n.getDistribution(0));
	       // for (int i=0; i<numstates; i++){
	        visualvalued.setColor(n.getCasesList().getCaseNum(0).getColor());
	             if (log) visualvalued.setKindofunit(LOGARITMO);
	             else visualvalued.setKindofunit(DECIMAL);

	             //si el caso activo es el 0, es el priori, lo que implica que se pintarán las prob.
	             //a priori al lado de cada barra. Si no, no, pues solo se pintan las prob del caso
	             //activo.

	             if (currentcase==0) visualvalued.paintValueDistribution(g,px,posg,true);
	             else visualvalued.paintValueDistribution(g,px,posg,false);
	        
	    }

	    public void paintsaved(Graphics2D g, int px, int py){
	        VisualFStatesDistribution vfsd;
	        int numCases;
	        int posg;
	        int numd=0;
	        Case auxCase;
	        double postUtil;
	        VisualValueDistribution auxVVD;
	        
	        numCases = numdistributions;
	        
	        for (int i=1;i<numCases; i++){
	        	auxCase = n.getCasesList().getCaseNum(i);
	            if (auxCase.getIsShown() &&
	               (auxCase.getPropagated() || auxCase.getIsObserved(n))){
	                numd++;
	                posg=py+(heightbars*(numd));
	                //Obtain the utility of the case i
	                postUtil =n.getUtility(i);
	                //for (int i=0; i<numstates; i=i+1){
	                     auxVVD=new VisualValueDistribution(postUtil,i, minOfUtilities, maxOfUtilities);
	                     auxVVD.setColor(auxCase.getColor());
	                     if (i==currentcase){
	                        auxVVD.paintValueDistribution(g,px,posg,true);
	                     }
	                     else {
	                     	auxVVD.paintValueDistribution(g,px,posg,false);
	                     }
	                     if (numdistributions>0)
	                         posg=posg+(dstates+(heightbars*(numdistributions)));
	                         else posg=posg+(dstates+(heightbars*(numdistributions)));
	                }
	            
	        }
	    }


	     public void paintExplanation (Graphics2D g, int posx, int posy){
	        //pinta los nombres de los estados y las probabilidades a priori siempre
	        paintStateName(g,posx,posy);
	        if (paintprior)
	            paintpriori(g, posx+50, posy);

	        if (saved){
	            paintsaved(g,posx+50, posy);
	        }
	        paintReferenceBar(g,posx+50,posy);
	     }//fin del método paint


		/**
		 * This methods paints an horizontal line with the extrems of the utilities for this node.
		 * This lets the user to know if the obtained utility is near of some extrem.
		 * @param g
		 * @param i
		 * @param posy2
		 */
		private void paintReferenceBar(Graphics2D g, int posx, int posy) {
			// TODO Auto-generated method stub
			int k;
			int posy2;
			int lengthOfBar=80;
			  
	        k=3;
	        posy2 = posy+k+numdistributions*heightbars;
	        paintHorizontalBarOfExtrems(g,posx,posy2,lengthOfBar);
	        paintVerticalBarsOfExtrems(g,posx,posy2,lengthOfBar);
	        paintNumbersOfExtrems(g,posx,posy2,lengthOfBar);
		    			
		}


		/**
		 * @param g
		 * @param lengthOfBar
		 * @param posx2
		 * @param posy2
		 */
	private void paintNumbersOfExtrems(Graphics2D g, int posx, int posy,
			int lengthOfBar) {
		// TODO Auto-generated method stub
		int x1, y1;
		String stringMin, stringMax;

		NumberFormat nf = new DecimalFormat(elvira.Elvira.getElviraFrame()
				.getVisualPrecision());

		if (log) {
			stringMin = String.valueOf((int) Math.log(minOfUtilities));
			stringMax = String.valueOf((int) Math.log(maxOfUtilities));
		} else {
			stringMin = String.valueOf(nf.format(minOfUtilities));
			stringMax = String.valueOf(nf.format(maxOfUtilities));
		}

		x1 = posx;
		y1 = posy + 11;

		g.setColor(Color.black);

		// Extrem on the left (minimum)

		g.drawString(stringMin, x1, y1);

		// Extrem on the right (maximum)
		x1 = x1 + lengthOfBar;

		g.drawString(stringMax, x1, y1);

	}


		/**
		 * @param g
		 * @param posx2
		 * @param posy2
		 */
		private void paintVerticalBarsOfExtrems(Graphics2D g, int posx, int posy, int lengthOfBar) {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			int x1,y1,x2,y2;
			
			//Color of the line
			g.setColor(Color.black);
			
			//Line on the left
			x1 = posx;
			y1 = posy+heightbars/2;
			x2 = x1;
			y2 = posy-heightbars/2;
			g.drawLine(x1,y1,x2,y2);
			
			//Line on the right
			x1 = x1 +  lengthOfBar;
			x2 = x1;
			g.drawLine(x1,y1,x2,y2);
		
		}


		/**
		 * @param g
		 * @param lengthOfBar
		 * @param posx2
		 * @param posy2
		 */
		private void paintHorizontalBarOfExtrems(Graphics2D g, int posx, int posy, int lengthOfBar) {
			// TODO Auto-generated method stub
			int x1,y1,x2,y2;
			
			   
			x1 = posx;
			y1 = posy;
			x2 = x1+lengthOfBar;;
			y2 = y1;
			g.setColor(Color.black);
			g.drawLine(x1,y1,x2,y2);
		}


}
