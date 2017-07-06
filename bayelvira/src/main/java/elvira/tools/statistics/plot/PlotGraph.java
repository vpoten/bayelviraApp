/*
*   Class PlotGraph
*
*   A class that creates a window and displays within that window
*   a graph of one or more x-y data sets
*
*   This class extends Plot (also from Michael Thomas Flanagan's Library)
*
*   For use if you are incorporating a plot into your own JAVA program
*   See Plotter for a free standing graph plotting application
*
*   WRITTEN BY: Mick Flanagan
*
*   DATE:	 February 2002
*   UPDATED:  22 April 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   PlotGraph.html
*
*   Copyright (c) April 2004
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package elvira.tools.statistics.plot;

// Include the windowing libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JFrame;

// Declare a class that creates a window capable of being drawn to
public class PlotGraph extends Plot{
    	protected int graphwidth = 800;     	// width of the window for the graph in pixels
    	protected int graphheight = 600;    	// height of the window for the graph in pixels
    	protected int closechoice = 1;    	    // =1 clicking on close icon causes window to close
                                  		        //    and the the program is exited.
                                  		        // =2 clicking on close icon causes window to close
                                  		        //    leaving the program running.
    	// Create the window object
    	protected JFrame window = new JFrame("Michael T Flanagan's plotting program - PlotGraph");

    	// Constructor
    	// One 2-dimensional data arrays
    	public PlotGraph(double[][] data){
        	super(data);
   	    }

   	    // Constructor
    	//Two 1-dimensional data arrays
    	public PlotGraph(double[] xdata, double[] ydata){
        	super(xdata, ydata);
   	    }

    	// Rescale the y dimension of the graph window and graph
    	public void rescaleY(double yscalefactor)
    	{
        	this.graphheight=(int)Math.round((double)graphheight*yscalefactor);
        	this.ylen=(int)Math.round((double)ylen*yscalefactor);
        	this.ytop=(int)Math.round((double)ytop*yscalefactor);
        	this.ybot=this.ytop + this.ylen;
    	}

    	// Rescale the x dimension of the graph window and graph
    	public void rescaleX(double xscalefactor)
    	{
        	this.graphwidth=(int)Math.round((double)graphwidth*xscalefactor);
        	this.xlen=(int)Math.round((double)xlen*xscalefactor);
        	this.xbot=(int)Math.round((double)xbot*xscalefactor);
        	this.xtop=this.xbot + this.xlen;
    	}

    	// Get pixel width of the PlotGraph window
    	public int getGraphwidth(){
        	return this.graphwidth;
    	}

    	// Get pixel height of the PlotGraph window
    	public int getGraphheight(){
        	return this.graphheight;
    	}

    	// Reset height of graph window (pixels)
    		public void setGraphheight(int graphheight){
        	this.graphheight=graphheight;
    	}

    	// Reset width of graph window (pixels)
     		public void setGraphwidth(int graphwidth){
        	this.graphwidth=graphwidth;
    	}

    	// Get close choice
    	public int getClosechoice(){
        	return this.closechoice;
    	}

    	// Reset close choice
    	public void setClosechoice(int choice){
        	this.closechoice = choice;
    	}

    	// The paint method to draw the graph.
    	public void paint(Graphics g){

        	// Rescale - needed for redrawing if graph window is resized by dragging
        	double newgraphwidth = this.getSize().width;
        	double newgraphheight = this.getSize().height;
        	double xscale = (double)newgraphwidth/(double)this.graphwidth;
        	double yscale = (double)newgraphheight/(double)this.graphheight;
        	rescaleX(xscale);
        	rescaleY(yscale);

        	// Call graphing method
        	graph(g);
    	}

    	// Set up the window and show graph
    	public void plot(){
        	// Set the initial size of the graph window
        	setSize(this.graphwidth, this.graphheight);

        	// Set background colour
        	window.getContentPane().setBackground(Color.white);

        	// Choose close box
        	if(this.closechoice==1){
            		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	}
        	else{
            		window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        	}

        	// Add graph canvas
        	window.getContentPane().add("Center", this);

        	// Set the window up
        	window.pack();
        	window.setResizable(true);
        	window.toFront();

        	// Show the window
        	window.show();
    	}
}

