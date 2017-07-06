/*
 * VisualExplanationContinuous.java
 *
 * Created on 13 de noviembre de 2003, 14:01
 */

package elvira.gui.explication;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.Vector;

import elvira.Elvira;
import elvira.gui.explication.plotFunction.*;
import elvira.potential.*;
import elvira.*;


/**
 * This class contain the methods necesary to show the visual information of a bayesnet propagation result of a 
 *  continuous node.
 * @author   andrew
 */
public class VisualExplanationContinuous{
    
      public G2Dint graph;
      Axis xaxis;
      Axis yaxis;
      public DataSet data;

      public Color colorEvidence=new Color(200,0,0);
      
      TextField pinput;
      TextField mininput;
      TextField maxinput;
      TextField finput;
      

      Vector functionlabel=new Vector();
      Vector function=new Vector();
      Vector cutPoints=new Vector();
      
      ExplanationContinuous exp;
     
    /** 
     *  Creates a new instance of VisualExplanationContinuous.
     *  @param, ExplanationContinuous exp, what contain the information to show.
     */
    public VisualExplanationContinuous(ExplanationContinuous exp) {
   
                 super();
      this.exp=exp;
      graph      = new G2Dint();   // Graph class to do the plotting
      pinput     = new TextField(5);       // Number of points 
      mininput   = new TextField(10);      // Minimum x value input
      maxinput   = new TextField(10);      // Maximum x value input
      finput     = new TextField(30);      // Input for the function to plot
      

         

         
        
        graph.setBorder(new javax.swing.border.MatteBorder(null));
        graph.setVisible(true);


         String tmp=new String("");
         
         
if (exp!=null){        
         PotentialContinuousPT p = exp.getPCPT();
         
         if (p.getTree().isProbab()){
            cutPoints=null;
            
            pinput.setText("500"); 
            mininput.setText(tmp.valueOf(new Double(exp.getNode().getMin()))); 
            maxinput.setText(tmp.valueOf(new Double(exp.getNode().getMax()))); 
            
            String a1 = p.getTree().getProb().ToStringWithParentesis();
            String a2 = a1.replaceAll("("+exp.getNode().getName()+")","x");
            functionlabel.addElement(a2);


            function.addElement(new ParseFunction((String)functionlabel.elementAt(0)));
            if(!((ParseFunction)function.elementAt(0)).parse()) {
                System.out.println("EROOR!: "+(String)functionlabel.elementAt(0));
                System.out.println("Failed to parse function!");
                      return;
            }

            data = new DataSet();
         }else{
             cutPoints=(Vector)p.getTree().extractAllCutPoints().clone();
             if (cutPoints==null){
                 System.out.println("Cut Points Nulls: "+exp.getNode().getName());
                 return;
             }
             Vector probs=p.getTree().extractAllProbabilities();
             for (int i=0; i<probs.size(); i++){
                    String t1=((MixtExpDensity)probs.elementAt(i)).ToStringWithParentesis();
                    String t2=t1.replaceAll("("+exp.getNode().getName()+")","x");
                    functionlabel.addElement(t2);
                    
                    ParseFunction fi=new ParseFunction(t2);
                    
                             if(!fi.parse()) {
                                 System.out.println("EROOR!!: "+(String)functionlabel.elementAt(i));
                                 System.out.println("Failed to parse function!!");
                                  return;
                                }
                    function.addElement(fi);

             }
             


             finput.setText("exp(-X*X)"); 
             pinput.setText("500"); 
             
             mininput.setText(tmp.valueOf(((Double)cutPoints.elementAt(0)).doubleValue()));     
             maxinput.setText(tmp.valueOf(((Double)cutPoints.elementAt(cutPoints.size()-1)).doubleValue())); 

             data = new DataSet();

             data.setCutPoints(cutPoints);
         }

         
         
         xaxis = graph.createXAxis();
         xaxis.setTitleText("X");

         yaxis = graph.createYAxis();

         data.linecolor = exp.getCasesList().getCaseNum(0).getColor();
         xaxis.attachDataSet(data);
         yaxis.attachDataSet(data);
         graph.attachDataSet(data);


         graph.setDataBackground(new Color(255,200,175));
         graph.setBackground(new Color(200,150,100));

         createGraph();  
         
         
         if (exp.isEvidenceIntroduced()){
                
                
                createGraphEvidence();
             
             
             
         }
}
}

    private void createGraphEvidence(){
        
        
        function.clear();
        functionlabel.clear();
        
        PotentialContinuousPT p = exp.getPCPTEvidence();
        if (p==null)
            return;
        if (exp.isObserved()){
            
            cutPoints=null;
            double val = exp.getCasesList().getCurrentCase().getEvidence().getContinuousValue(exp.getNode());

            pinput.setText("5000"); 
            mininput.setText(Double.toString(exp.getNode().getMin())); 
            maxinput.setText(Double.toString(exp.getNode().getMax())); 
            
            functionlabel.addElement("Observed");
            System.out.println("VAlor:"+val);

            function.addElement(new ParseFunction("omega("+Double.toString(val)+",x)"));

            if(!((ParseFunction)function.elementAt(0)).parse()) {
                     System.out.println("Failed to parse function!");
                      return;
            }

            data = new DataSet();
            
            
         }else if (p.getTree().isProbab()){
            cutPoints=null;
            
            pinput.setText("500"); 
            mininput.setText(Double.toString(exp.getNode().getMin())); 
            maxinput.setText(Double.toString(exp.getNode().getMax())); 
            
            
            String a1 = p.getTree().getProb().ToStringWithParentesis();
            String a2 = a1.replaceAll("("+exp.getNode().getName()+")","x");
            functionlabel.addElement(a2);

            function.addElement(new ParseFunction((String)functionlabel.elementAt(0)));
            if(!((ParseFunction)function.elementAt(0)).parse()) {
                     System.out.println("Failed to parse function!");
                      return;
            }

            data = new DataSet();
         }else{
             cutPoints=(Vector)p.getTree().extractAllCutPoints().clone();

             if (cutPoints==null){

                 System.out.println("Cut Points Nulls: "+exp.getNode().getName());
                 return;
             }
         
             Vector probs=p.getTree().extractAllProbabilities();
             for (int i=0; i<probs.size(); i++){
                    String t1=((MixtExpDensity)probs.elementAt(i)).ToStringWithParentesis();
                    String t2=t1.replaceAll("("+exp.getNode().getName()+")","x");
                    functionlabel.addElement(t2);
                    
                    ParseFunction fi=new ParseFunction(t2);
                    
                             if(!fi.parse()) {
                                 System.out.println("EROOR!!: "+(String)functionlabel.elementAt(i));
                                 System.out.println("Failed to parse function!!");
                                  return;
                                }
                    function.addElement(fi);

             }



             finput.setText("exp(-X*X)"); 
             pinput.setText("500"); 
             mininput.setText(((Double)cutPoints.elementAt(0)).toString());     
             maxinput.setText(((Double)cutPoints.elementAt(cutPoints.size()-1)).toString()); 



             data = new DataSet();

             data.setCutPoints(cutPoints);
         }
         
         data.linecolor = exp.getCasesList().getCurrentCase().getColor();
         xaxis.attachDataSet(data);
         yaxis.attachDataSet(data);
         graph.attachDataSet(data);

         createGraph();
         
    }
    /**
     *  Create de data set associated to the graph function.
     */
    private void createGraph(){
         int points;
         double maximum;
         double minimum;
         double x;
         int count = 0;
         boolean error = false;

         try {
              points   = Integer.parseInt(pinput.getText());
         } catch(Exception e) {
              System.out.println("Number of points error "+e.getMessage());
              return;
         }

         try {
            maximum = Double.valueOf(maxinput.getText()).doubleValue();
         } catch(Exception e) {
              System.out.println("X maximum error "+e.getMessage());
              return;
         }

         try {
            minimum = Double.valueOf(mininput.getText()).doubleValue();
         } catch(Exception e) {
              System.out.println("X minimum error "+e.getMessage());
              return;
         }




         double d[] = new double[2*points];




        if (cutPoints!=null){
             for(int i=0; i<points; i++) {

                 x = minimum + i*(maximum-minimum)/(points-1);
                 d[count] = x;
                 if (x>((Double)cutPoints.elementAt(1)).doubleValue()){// && function.size()>=1){
                        function.removeElementAt(0);
                        cutPoints.removeElementAt(0);
                        
                        functionlabel.removeElementAt(0);
                 }
                 try {

                      d[count+1] = ((ParseFunction)function.elementAt(0)).getResult(x);
                      count += 2;

                 } catch(Exception e) { error = true; }

             }
        }else{
             for(int i=0; i<points; i++) {

                 x = minimum + i*(maximum-minimum)/(points-1);
                 d[count] = x;
                 try {

                      d[count+1] = ((ParseFunction)function.elementAt(0)).getResult(x);
                      count += 2;

                 } catch(Exception e) { 
                     System.out.println("Fallo getResult.");
                     error = true; }

             }
        }
 

         
         if(count <= 2) {
             System.out.println("Error NO POINTS to PLOT!");
             return;
         } else
         if( error ) {
             System.out.println("Error while calculating points!");
	 }


         yaxis.setTitleText(finput.getText());

         data.deleteData();

         try {
               data.append(d,count/2);
         } catch(Exception e) {
             System.out.println("Error while appending data!");
             return;
	 }
   
        
    }
    
    
    
    
    /**
     *  Paint a MixtExpFunction graph as a propagation result.
     */
    
    public void paintFunction(Graphics2D g2, int px, int py,int width, int height){

         graph.setBounds(px,py,width,height);
         graph.paint(g2);
        
        
    }
    public G2Dint getGraph(){
        
        return graph;
    }
    /*public static void main(String args[]) {
        
        VisualExplanationContinuous v = new VisualExplanationContinuous(null);
        v.setEnabled(true);
        
        v.getContentPane().setLayout(new BorderLayout(0,0));
	
        v.setSize(578,376);
        v.setResizable(true);

                
        v.setVisible(true);
        
        v.graph.setBounds(50,50,200,200);
        
        v.paint(v.getGraphics());
        
        
    }*/
}
