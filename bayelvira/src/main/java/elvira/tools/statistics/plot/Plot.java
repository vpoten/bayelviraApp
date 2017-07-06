/*
*   Class   Plot
*
*   Superclass for the plotting subclasses:
*       PlotGraph, Plotter and PlotPoleZero
*
*   WRITTEN BY: Mick Flanagan
*
*   DATE:	 February 2002
*   REVISED: 17 April 2004
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

import java.awt.*;
import elvira.tools.statistics.math.Fmath;
import elvira.tools.statistics.interpolation.CubicSpline;

public class Plot extends Canvas{
    	protected double[][] data = null;   // data to be plotted
                                        	// data[i][] i = 0, 2, 4 . . .  x values
                                        	// data[i][] i = 1, 3, 5 . . .  y values for x[i-1][]
    	protected double[][] copy = null;   // copy of original data to be plotted
    	protected int ncurves = 0;          // number of curves
    	protected int[] npoints = null;     // number of points points on curve each curve
    	protected int nmpoints = 0;         // number of points points on curve with most points
    	protected int nipoints = 200;       // number of cubic spline interpolation points
    	protected int[] pointopt = null;    // point plotting option for each curve
                                        	// pointopt = 0: no points plotted
                                        	// pointopt = i where i = 1,2,3,4,5,6,7,8: points plotted
                                            	// default options
                                            		// curve 1 - open circles
                                            		// curve 2 - open squares
                                            		// curve 3 - open diamonds
                                            		// curve 4 - filled circles
                                            		// curve 5 - filled squares
                                            		// curve 6 - filled diamonds
                                            		// curve 7 - x crosses
                                            		// curve 8 - + crosses
                                            		// further curves - above sequence repeated
    	protected int[] pointsize = null;   // point size in pixels for each curve
    	protected int nptypes = 8;          // number of point types
    	protected boolean[] errorbar = null; // true - error bar plotted, flase no error bar plotted - default = false
    	protected double[][] errors = null;  // error bar values - should be an estimate of the sd of the variable
    	protected double[][] errorscopy = null; // copy of error bar values
    	protected int[] lineopt = null;     // line drawing option for each curve
                                            	// lineopt = 0: no line plotted
                                            	// lineopt = 1: cubic spline interpolation line plotted as a continuous line
                                            	// lineopt = 2: cubic spline interpolation line plotted as a dashed line
                                            	// lineopt = 3: line plotted by joining points
                                            	// default - lineopt = 1
    	protected int[] dashlength = null;   // dash length in lineopt = 2
    	protected boolean[] minmaxopt = null;// true - curve included in maximum and minimum axes value calculation
    	protected boolean[] trimopt = null;  // true - curve trimmed to fit axes rectangle

    	protected int fontsize = 14;    // text font size
    	protected int xlen = 625;       // length of the x axis in pixels
   	    protected int ylen = 375;       // length of the y axis in pixels
    	protected int xbot = 100;       // x coordinate of the bottom of the x axis in pixels
    	protected int xtop = xbot+xlen; // x coordinate of the top of the x axis in pixels
    	protected int ytop = 110;       // y coordinate of the top of the y axis in pixels
    	protected int ybot = ytop+ylen; // y coordinate of the bottom of the y axis in pixels

    	protected double xlow = 0;      // scaled lower limit data value of the x axis
    	protected double xhigh = 0;     // scaled upper limit data value of the x axis
    	protected double ylow = 0;      // scaled lower limit data value of the y axis
    	protected double yhigh = 0;     // scaled upper limit data value of the y axis
    	protected int xfac = 0;         // decadic exponent of x axis scaling factor
    	protected int yfac = 0;         // decadic exponent of y axis scaling factor
    	protected int xticks = 0;       // number of x axis ticks
    	protected int yticks = 0;       // number of y axis ticks

    	protected double xmin = 0.0D;    // minimum x data value
    	protected double xmax = 0.0D;    // maximum x data value
    	protected double ymin = 0.0D;    // minimum y data value
    	protected double ymax = 0.0D;    // maximum y data value

    	protected double xoffset = 0.0D; // x axis data value offset
   	    protected double yoffset = 0.0D; // y axis data value offset
    	protected boolean noxoffset = false; // no x axis offset allowed if true
   	    protected boolean noyoffset = false; // no y axis offset allowed if true
    	protected double xlowfac = 0.75D; // x axis data setting low factor
    	protected double ylowfac = 0.75D; // y axis data setting low factor

    	protected String graphtitle  = "  ";    // graph title
    	protected String graphtitle2 = "  ";    // graph title (secondline)
    	protected String xaxislegend = "  ";    // x axis legend title
    	protected String xaxisunits  = "  ";    // x axis unit name, e.g.  V, ohm
    	protected String yaxislegend = "  ";    // y axis legend title
    	protected String yaxisunits  = "  ";    // x axis unit name

    	protected boolean xzero = false;        // if true - a (x=0) zero line is required
    	protected boolean yzero = false;        // if true - a (y=0) zero line required
    	protected boolean noxunits = true;      // if true - no x axis units
    	protected boolean noyunits = true;      // if true - no y axis units

    	protected double[] xaxisno = new double[50];      // x axis legend numbers as double
    	protected double[] yaxisno = new double[50];      // y axis legend numbers as double
    	protected String[] xaxischar = new String[50];    // x axis legend numbers as char
    	protected String[] yaxischar = new String[50];    // y axis legend numbers as char
    	protected int[] axisticks = new int[50];          // no of ticks for scaled lengths

    	protected static double datafill = 3.0e200; // value used to initialise data array by Plot.data()


        // Constructor
    	//One 2-dimensional data arrays
    	public Plot(double[][] data){
        	this.initialise(data);
   	    }

   	    // Constructor
    	//Two 1-dimensional data arrays
    	public Plot(double[] xdata, double[] ydata){
    	    int xl = xdata.length;
    	    int yl = ydata.length;
    	    if(xl!=yl)throw new IllegalArgumentException("x-data length is not equal to the y-data length");
    	    double[][] data = new double[2][xl];
    	    for(int i=0; i<xl; i++){
    	        data[0][i] = xdata[i];
    	        data[1][i] = ydata[i];
    	    }
        	this.initialise(data);
   	    }

    	// Initialisation
    	private void initialise(double[][] cdata){

            // Calculate number of curves
        	this.ncurves  = cdata.length/2;

        	// Initialize 1D class arrays
        	this.npoints  = new int[ncurves];
        	this.lineopt  = new int[ncurves];
        	this.dashlength  = new int[ncurves];
        	this.trimopt  = new boolean[ncurves];
        	this.minmaxopt = new boolean[ncurves];
        	this.pointopt = new int[ncurves];
        	this.pointsize = new int[ncurves];
        	this.errorbar = new boolean[ncurves];

            // Calculate maximum number of points on a single curve
        	this.nmpoints = 0;
        	int l = 0;
        	for(int i=0; i<2*ncurves; i++){
            		if((l=cdata[i].length)>nmpoints)nmpoints=l;
        	}

        	// Initialize class 2D arrays
        	this.data = new double[2*ncurves][nmpoints];
        	this.copy = new double[2*ncurves][nmpoints];
        	this.errors = new double[ncurves][nmpoints];
        	this.errorscopy = new double[ncurves][nmpoints];


            // Calculate curve lengths
            // and check all individual curvs have an equal number of  abscissae and ordinates
        	int k = 0, l1 = 0, l2 = 0;
        	boolean testlen=true;
        	for(int i=0; i<ncurves; i++){
            		testlen=true;
            		l1=cdata[k].length;
            		l2=cdata[k+1].length;
            		if(l1!=l2)throw new IllegalArgumentException("an x and y array length differ");
            		npoints[i]=l1;
            		k=2*i;
            }

            // Remove both abscissae and ordinates for points equal to datafill
        	k=0;
        	boolean testopt=true;
            for(int i=0; i<ncurves; i++){
                testlen=true;
                l1=npoints[i];
                 while(testlen){
                    if(l1<0)throw new IllegalArgumentException("curve array index  "+k+ ": blank array");
                    if(cdata[k][l1-1]==datafill){
                        if(cdata[k+1][l1-1]==datafill){
                            l1--;
                            testopt=false;
                        }
                        else{
                            testlen=false;
                        }
                    }
                    else{
                        testlen=false;
                    }
                }
                npoints[i]=l1;
                k+=2;
            }

            // initialize class data variables
        	k=0;
        	int kk=1;
        	for(int i=0; i<ncurves; i++){
        	        // order arrays
        	        double[][] hold = new double[2][npoints[i]];
        	        for(int j=0; j<npoints[i]; j++){
            		    hold[0][j]=cdata[k][j];
            		    hold[1][j]=cdata[k+1][j];
                    }
                    hold = selectionSort(hold);

                    // fill arrays
            		for(int j=0; j<npoints[i]; j++){
            		    this.data[k][j]=hold[0][j];
            		    this.data[k+1][j]=hold[1][j];
                		this.copy[k][j]=hold[0][j];
                		this.copy[k+1][j]=hold[1][j];
            		}

            		this.lineopt[i] = 1;
            		this.dashlength[i] = 5;
            		this.trimopt[i] = false;
            		if(this.lineopt[i]==1)trimopt[i] = true;
            		this.minmaxopt[i]=true;
            		this.pointsize[i]= 6;
            		this.errorbar[i]= false;
            		this.pointopt[i] = kk;
            		k+=2;
            		kk++;
            		if(kk>nptypes)kk = 1;
        	}
    	}

        // sort x elements into ascending order with matching switches ofy elements
        // using selection sort method
        public static double[][] selectionSort(double[][] aa){
            int index = 0;
            int lastIndex = -1;
            int n = aa[0].length;
            double holdx = 0.0D;
            double holdy = 0.0D;
            double[][] bb = new double[2][n];
            for(int i=0; i<n; i++){
                bb[0][i]=aa[0][i];
                bb[1][i]=aa[1][i];
            }


            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[0][i]<bb[0][index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=bb[0][index];
                bb[0][index]=bb[0][lastIndex];
                bb[0][lastIndex]=holdx;
                holdy=bb[1][index];
                bb[1][index]=bb[1][lastIndex];
                bb[1][lastIndex]=holdy;

            }
            return bb;
        }


    	//Create a data array initialised to datafill;
    	public static double[][] data(int n, int m){
        	double[][] d = new double[2*n][m];
        	for(int i=0; i<2*n; i++){
            		for(int j=0; j<m; j++){
                		d[i][j]=datafill;
            		}
        	}
        	return d;
    	}

    	//Change the value used to initialise the datarray
    	public static void setDataFillValue(double dataFill){
    	    Plot.datafill=dataFill;
        }

    	//Get the value used to initialise the datarray
    	public static double getDataFillValue(){
    	    return Plot.datafill;
        }

    	// Enter primary graph title
    	public void setGraphTitle(String graphtitle){
        	this.graphtitle=graphtitle;
    	}

    	// Enter second line to graph title
    	public void setGraphTitle2(String graphtitle2){
        	this.graphtitle2=graphtitle2;
    	}

    	// Enter x axis legend
    	public void setXaxisLegend(String xaxislegend){
        	this.xaxislegend=xaxislegend;
    	}

    	// Enter y axis legend
    	public void setYaxisLegend(String yaxislegend){
        	this.yaxislegend=yaxislegend;
    	}

    	// Enter x axis unit name
    	public void setXaxisUnitsName(String xaxisunits){
        	this.xaxisunits=xaxisunits;
        	this.noxunits=false;
    	}

    	// Enter y axis unit name
    	public void setYaxisUnitsName(String yaxisunits){
        	this.yaxisunits=yaxisunits;
         	this.noyunits=false;
    	}

    	// Get pixel length of the x axis
    	public int getXaxisLen(){
        	return this.xlen;
    	}

    	// Get pixel length of the y axis
    	public int getYaxisLen(){
        	return this.ylen;
    	}

    	// Get pixel start of the x axis
    	public int getXlow(){
        	return this.xbot;
    	}

    	// Get pixel end of the y axis
    	public int getYhigh(){
        	return this.ytop;
    	}

    	// Get point size in pixels
    	public int[] getPointsize(){
        	return this.pointsize;
    	}

    	// Get dash length in pixels
    	public int[] getDashlength(){
        	return this.dashlength;
    	}

    	// Get the x axis low factor
    	public double getXlowfac(){
        	return 1.0D-this.xlowfac;
    	}

    	// Get the y axis low factor
    	public double getYlowfac(){
        	return 1.0D-this.ylowfac;
    	}

    	// Get the x axis minimum value
    	public double getXmin(){
        	return this.xmin;
    	}

    	// Get the x axis maximum value
    	public double getXmax(){
        	return this.xmax;
    	}

    	// Get the y axis minimum value
    	public double getYmin(){
        	return this.ymin;
    	}

    	// Get the y axis maximum value
    	public double getYmax(){
        	return this.ymax;
    	}

    	// get line plotting option
    	public int[] getLine(){
        	return this.lineopt;
    	}

    	// Get point plotting options
    	public int[] getPoint(){
        	return this.pointopt;
    	}

    	// Get the number of points to be used in the cubic spline interpolation
    	public int getNipoints(){
        	return this.nipoints;
    	}

    	// Get font size
    	public int getFontsize(){
        	return this.fontsize;
    	}

    	// Reset pixel length of the x axis
    	public void setXaxisLen(int xlen){
        	this.xlen=xlen;
        	this.update();
    	}

    	// Reset pixel length of the y axis
    	public void setYaxisLen(int ylen){
        	this.ylen=ylen;
        	this.update();
    	}

    	// Reset pixel start of the x axis
    	public void setXlow(int xbot){
        	this.xbot=xbot;
        	this.update();
    	}

    	// Reset pixel end of the y axis
    	public void setYhigh(int ytop){
        	this.ytop=ytop;
        	this.update();
    	}

    	// Reset the x axis low factor
    	public void setXlowfac(double xlowfac){
        	this.xlowfac=1.0D-xlowfac;
    	}

    	// Reset the y axis low factor
    	public void setYlowfac(double ylowfac){
        	this.ylowfac=1.0D-ylowfac;
    	}

    	// Reset the x axis offset option
    	public void setNoXoffset(boolean noxoffset){
        	this.noxoffset=noxoffset;
    	}

    	// Reset the y axis offset option
    	public void setNoYoffset(boolean noyoffset){
        	this.noyoffset=noyoffset;
    	}

    	// Reset both the x and y axis offset options to the same optio
    	public void setNoOffset(boolean nooffset){
        	this.noxoffset=nooffset;
        	this.noyoffset=nooffset;
    	}

    	// Get the x axis offset option
    	public boolean getNoXoffset(){
        	return this.noxoffset;
    	}

    	// RGet the y axis offset option
    	public boolean getNoYoffset(){
        	return this.noyoffset;
    	}

    	// Update axis pixel position parameters
    	protected void update(){
        	this.xtop = this.xbot + this.xlen;
        	this.ybot = this.ytop + this.ylen;
    	}

    	// Overwrite line plotting option with different options for individual curves
    	public void setLine(int[] lineopt){
        	int n=lineopt.length;
        	if(n!=ncurves)throw new IllegalArgumentException("input array of wrong length");
        	for(int i=0; i<n; i++)if(lineopt[i]<0 || lineopt[i]>3)throw new IllegalArgumentException("lineopt must be 0, 1, 2 or 3");
        	this.lineopt=lineopt;
    	}

    	// Overwrite line plotting option with a single option for all curves
    	public void setLine(int slineopt){
        	if(slineopt<0 || slineopt>3)throw new IllegalArgumentException("lineopt must be 0, 1, 2 or 3");
        	for(int i=0; i<this.ncurves; i++)this.lineopt[i]=slineopt;
    	}

    	// Overwrite dash length with different options for individual curves
    	public void setDashlength(int[] dashlength){
        	if(dashlength.length!=ncurves)throw new IllegalArgumentException("input array of wrong length");
        	this.dashlength=dashlength;
    	}

    	// Overwrite dashlength with a single option for all curves
    	public void setDashLength(int sdashlength){
        	for(int i=0; i<this.ncurves; i++)this.dashlength[i]=sdashlength;
    	}

    	// Overwrite point plotting option with different options for individual curves
    	public void setPoint(int[] pointopt){
        	int n=pointopt.length;
        	if(n!=ncurves)throw new IllegalArgumentException("input array of wrong length");
        	for(int i=0; i<n; i++)if(pointopt[i]<0 || pointopt[i]>8)throw new IllegalArgumentException("pointopt must be 0, 1, 2, 3, 4, 5, 6, 7, or 8");
        	this.pointopt=pointopt;
    	}

    	// Overwrite point plotting option with a single option for all curves
    	public void setPoint(int spointopt){
        	if(spointopt<0 || spointopt>8)throw new IllegalArgumentException("pointopt must be 0, 1, 2, 3, 4, 5, 6, 7, or 8");
        	for(int i=0; i<this.ncurves; i++)this.pointopt[i]=spointopt;
    	}

    	// Overwrite point size with different options for individual curves
    	public void setPointsize(int[] mpointsize){
         	if(mpointsize.length!=ncurves)throw new IllegalArgumentException("input array of wrong length");
        	for(int i=0; i<this.ncurves; i++){
            		if(mpointsize[i]!=(mpointsize[i]/2)*2)mpointsize[i]++;
            		this.pointsize[i]=mpointsize[i];
        	}
    	}

    	// Overwrite point size with a single option for all curves
    	public void setPointsize(int spointsize){
        	if(spointsize%2!=0)spointsize++;
        	for(int i=0; i<this.ncurves; i++)this.pointsize[i]=spointsize;
    	}

    	// Set errorbar values
    	// Must set each curve individually
    	// nc is the curve identifier (remember curves start at 0)
    	// err are the error bar values which should be an estimate of the standard devition of the experimental point
    	public void setErrorbars(int nc, double[] err){
        	if(err.length!=this.npoints[nc])throw new IllegalArgumentException("input array of wrong length");
        	this.errorbar[nc] = true;
        	for(int i=0; i<this.npoints[nc]; i++){
            		this.errors[nc][i] = err[i];
            		this.errorscopy[nc][i] = err[i];
        	}
    	}

    	// overwrite the number of points to be used in the cubic spline interpolation
    	public void setNipoints(int nipoints){
        	this.nipoints=nipoints;
    	}

    	// overwrite the font size
    	public void setFontsize(int fontsize){
        	this.fontsize=fontsize;
    	}

    	// overwrite the trim option
    	public void setTrimopt(boolean[] trim){
        	this.trimopt=trim;
    	}

    	// overwrite the minmaxopt option
    	public void setMinmaxopt(boolean[] minmax){
        	this.minmaxopt=minmax;
    	}

    	// Calculate scaling  factors
   	    public static int scale(double mmin, double mmax){
        	int fac=0;
        	double big=0.0D;
        	boolean test=false;

        	if(mmin>=0.0 && mmax>0.0){
            		big=mmax;
            		test=true;
        	}
        	else{
            		if(mmin<0.0 && mmax<=0.0){
                		big=-mmin;
                		test=true;
            		}
            		else{
                		if(mmax>0.0 && mmin<0.0){
                    			big=Math.max(mmax, -mmin);
                    			test=true;
                		}
            		}
        	}

        	if(test){
            		if(big>100.0){
                		while(big>1.0){
                    			big/=10.0;
                    			fac--;
                		}
            		}
            		if(big<=0.01){
                		while(big<=0.10){
                    			big*=10.0;
                    			fac++;
                		}
            		}
        	}
        	return fac;
    	}

    	// Set low value on axis
    	public static void limits(double low, double high, double lowfac, double[]limits){

            double facl = 1.0D;
            double fach = 1.0D;
            if(Math.abs(low)<1.0D)facl=10.0D;
            if(Math.abs(low)<0.1D)facl=100.0D;
            if(Math.abs(high)<1.0D)fach=10.0D;
            if(Math.abs(high)<0.1D)fach=100.0D;

        	double ld=Math.floor(10.0*low*facl)/facl;
        	double hd=Math.ceil(10.0*high*fach)/fach;

        	if(ld>=0.0D && hd>0.0D){
            		if(ld<lowfac*hd){
                		ld=0.0;
            		}
        	}
        	if(ld<0.0D && hd<=0.0D){
            		if(-hd <= -lowfac*ld){
                		hd=0.0;
             		}
        	}
        	limits[0] = ld/10.0;
        	limits[1] = hd/10.0;
    	}

    	// Calculate axis offset value
   	    public static double offset(double low, double high){

        	double diff = high - low;
        	double sh = Fmath.sign(high);
        	double sl = Fmath.sign(low);
        	double offset=0.0D;
        	int eh=0, ed=0;

        	if(sh == sl){
            		ed=(int)Math.floor(Fmath.log10(diff));
            		if(sh==1){
                		eh=(int)Math.floor(Fmath.log10(high));
                		if(eh-ed>1)offset = Math.floor(low*Math.pow(10, -ed))*Math.pow(10,ed);
            		}
            		else{
                		eh=(int)Math.floor(Fmath.log10(Math.abs(low)));
                		if(eh-ed>1)offset = Math.floor(high*Math.pow(10, -ed))*Math.pow(10,ed);
            		}
        	}
        	return offset;
    	}


    	// Calculate scaling and offset values for both axes
    	public void axesScaleOffset(){

        	double[] limit = new double[2];

        	// tranfer data from copy to enable redrawing
        	int k=0;
        	for(int i=0; i<ncurves; i++){
            		for(int j=0; j<npoints[i]; j++){
                		this.data[k][j]=this.copy[k][j];
                		this.data[k+1][j]=this.copy[k+1][j];
                		this.errors[i][j]=this.errorscopy[i][j];
                		if(this.errorbar[i])this.errors[i][j]+=this.data[k+1][j];
            		}
            		k+=2;
        	}

        	// Find mimium and maximum data values
        	minMax();

       		// Calculate x axis offset values and subtract it from the data
        	if(!noxoffset)this.xoffset=offset(this.xmin, this.xmax);
        	if(this.xoffset!=0.0){
            		k=0;
            		for(int i=0; i<this.ncurves; i++){
                		for(int j=0; j<this.npoints[i]; j++){
                    			this.data[k][j] -= this.xoffset;
                		}
                		k+=2;
            		}
            		this.xmin -= this.xoffset;
            		this.xmax -= this.xoffset;
        	}

        	// Calculate y axis offset values and subtract it from the data
        	if(!noyoffset)this.yoffset=offset(this.ymin, this.ymax);
        	if(this.yoffset!=0.0){
            		k=1;
            		for(int i=0; i<this.ncurves; i++){
                		for(int j=0; j<this.npoints[i]; j++){
                    			this.data[k][j] -= this.yoffset;
                    			if(this.errorbar[i])this.errors[i][j] -= this.yoffset;
                		}
                		k+=2;
            		}
            		this.ymin -= this.yoffset;
            		this.ymax -= this.yoffset;
        	}

        	// Calculate x axes scale values and scale data
        	this.xfac = scale(this.xmin, this.xmax);
        	if(this.xfac!=0){
            		k=0;
            		for(int i=0; i<this.ncurves; i++){
                		for(int j=0; j<this.npoints[i]; j++){
                    			this.data[k][j] *= Math.pow(10, this.xfac+1);
                		}
                		k+=2;
            		}
            		this.xmin *= Math.pow(10, this.xfac+1);
            		this.xmax *= Math.pow(10, this.xfac+1);
        	}

        	// Calculate y axes scale values and scale data
        	this.yfac = scale(this.ymin, this.ymax);
        	if(this.yfac!=0){
            		k=1;
            		for(int i=0; i<this.ncurves; i++){
                		for(int j=0; j<this.npoints[i]; j++){
                    			this.data[k][j] *= Math.pow(10, yfac+1);
                    			if(this.errorbar[i])this.errors[i][j] *= Math.pow(10, this.yfac+1);
                		}
                		k+=2;
            		}
            		this.ymin *= Math.pow(10, this.yfac+1);
            		this.ymax *= Math.pow(10, this.yfac+1);
        	}

        	// Calculate scaled low and high values
        	// x axis
        	limits(this.xmin, this.xmax, this.xlowfac, limit);
        	this.xlow  = limit[0];
        	this.xhigh = limit[1];
        	if(xlow<0 && xhigh>0)xzero=true;
        	// y axis
        	limits(this.ymin, this.ymax, this.ylowfac, limit);
        	this.ylow  = limit[0];
        	this.yhigh = limit[1];
        	if(ylow<0 && yhigh>0)yzero=true;

        	// Calculate tick parameters
        	// x axis
        	this.xticks = ticks(this.xlow, this.xhigh, this.xaxisno, this.xaxischar);
        	this.xhigh = this.xaxisno[this.xticks-1];
        	if(this.xlow!=this.xaxisno[0]){
        	    if(this.xoffset!=0.0D){
        	        this.xoffset = this.xoffset - this.xlow + this.xaxisno[0];
        	    }
        	    this.xlow = this.xaxisno[0];
        	}
        	// y axis
        	this.yticks = ticks(this.ylow, this.yhigh, this.yaxisno, this.yaxischar);
        	this.yhigh = this.yaxisno[this.yticks-1];
        	if(this.ylow!=this.yaxisno[0]){
        	    if(this.yoffset!=0.0D){
        	        this.yoffset = this.yoffset - this.ylow + this.yaxisno[0];
        	    }
        	    this.ylow = this.yaxisno[0];
        	}

    	}

    	// Calculate axis ticks and tick values
    	public static int ticks(double low, double high, double[] tickval, String[] tickchar){


        	// Find range
            int[] trunc = {1, 1, 1, 2, 3};
            double[] scfac1 = {1.0, 10.0, 1.0, 0.1, 0.01};
            double[] scfac2 = {1.0, 1.0, 0.1, 0.01, 0.001};

            double rmax = Math.abs(high);
            double temp = Math.abs(low);
            if(temp>rmax)rmax = temp;
            int range = 0;
            if(rmax<=100.0D){
                range = 1;
            }
            if(rmax<=10.0D){
                range = 2;
            }
            if(rmax<=1.0D){
                range = 3;
            }
            if(rmax<=0.1D){
                range = 4;
            }
            if(rmax>100.0D || rmax<0.01)range = 0;

        	// Calculate number of ticks
        	double inc = 0.0D;
        	double bot = 0.0D;
        	double top = 0.0D;
        	int sgn = 0;
        	int dirn = 0;
        	if(high>0.0D && low>=0.0D){
        	    inc = Math.ceil((high-low)/scfac1[range])*scfac2[range];
        	    dirn = 1;
        	    bot = low;
        	    top = high;
        	    sgn = 1;
        	}
        	else{
        	    if(high<=0 && low<0.0D){
        	        inc = Math.ceil((high-low)/scfac1[range])*scfac2[range];
        	        dirn = -1;
        	        bot = high;
        	        top = low;
        	        sgn = -1;
        	    }
        	    else{
        	        double up = Math.abs(Math.ceil(high));
        	        double down = Math.abs(Math.floor(low));
        	        int np = 0;
        	        if(up>=down){
        	            dirn = 2;
        	            np = (int)Math.rint(10.0*up/(up+down));
        	            inc = Math.ceil((high*10/np)/scfac1[range])*scfac2[range];
                        bot = 0.0D;
        	            top = high;
        	            sgn = 1;
        	        }
        	        else{
        	            dirn = -2;
        	            np = (int)Math.rint(10.0D*down/(up+down));
        	            inc = Math.ceil((Math.abs(low*10/np))/scfac1[range])*scfac2[range];
        	            bot = 0.0D;
        	            top = low;
        	            sgn = -1;
        	        }
        	    }
        	}

            int nticks = 1;
            double sum = bot;
            boolean test = true;
            while(test){
                sum = sum + sgn*inc;
                nticks++;
                if(Math.abs(sum)>=Math.abs(top))test=false;
            }

        	// Calculate tick values
        	int npExtra = 0;
        	switch(dirn){
        	    case 1:     tickval[0]=Fmath.truncate(low, trunc[range]);
        	                for(int i=1; i<nticks; i++){
            		            tickval[i]  = Fmath.truncate(tickval[i-1]+inc, trunc[range]);
        	                }
        	                break;
        	    case -1:    tickval[0]=Fmath.truncate(high, trunc[range]);
        	                for(int i=1; i<nticks; i++){
            		            tickval[i]  = Fmath.truncate(tickval[i-1]-inc, trunc[range]);
        	                }
        	                break;
        	    case 2:     npExtra = (int)Math.ceil(-low/inc);
        	                nticks += npExtra;
         	                tickval[0]=Fmath.truncate(-npExtra*inc, trunc[range]);
        	                for(int i=1; i<nticks; i++){
            		            tickval[i]  = Fmath.truncate(tickval[i-1]+inc, trunc[range]);
        	                }
           	                break;
           	    case -2:    npExtra = (int)Math.ceil(high/inc);
        	                nticks += npExtra;
        	                tickval[0]=Fmath.truncate(npExtra*inc, trunc[range]);
        	                for(int i=1; i<nticks; i++){
            		            tickval[i]  = Fmath.truncate(tickval[i-1]-inc, trunc[range]);
        	                }
           	                break;
           	}


            // set String form of tick values
        	for(int i=0; i<nticks; i++){
            		tickchar[i] = String.valueOf(tickval[i]);
            		tickchar[i] = tickchar[i].trim();
        	}

        	return nticks;
    	}

    	// Find minimum and maximum x and y values
    	public void minMax(){
        	boolean test  = true;

        	int ii=0;
        	while(test){
            		if(this.minmaxopt[ii]){
                		test=false;
                		this.xmin=this.data[2*ii][0];
                		this.xmax=this.data[2*ii][0];
                		this.ymin=this.data[2*ii+1][0];
                		if(this.errorbar[ii])this.ymin=2.0D*this.ymin-this.errors[ii][0];
                 		this.ymax=this.data[2*ii+1][0];
                		if(this.errorbar[ii])this.ymax=errors[ii][0];
            		}
            		else{
                		ii++;
                		if(ii>ncurves)throw new IllegalArgumentException("At least one curve must be included in the maximum/minimum calculation");
            		}
        	}

        	int k=0;
        	double ymint=0.0D, ymaxt=0.0D;
        	for(int i=0; i<this.ncurves; i++){
            		if(minmaxopt[i]){
                		for(int j=0; j<this.npoints[i]; j++){
                    			if(this.xmin>this.data[k][j])this.xmin=this.data[k][j];
                    			if(this.xmax<this.data[k][j])this.xmax=this.data[k][j];
                    			ymint=this.data[k+1][j];
                   		        if(errorbar[i])ymint=2.0D*ymint-errors[i][j];
                    			if(this.ymin>ymint)this.ymin=ymint;
                    			ymaxt=this.data[k+1][j];
                   		        if(errorbar[i])ymaxt=errors[i][j];
                    			if(this.ymax<ymaxt)this.ymax=ymaxt;
                		}
            		}
            		k+=2;
        	}

        	if(this.xmin==this.xmax){
            		if(this.xmin==0.0D){
                		this.xmin=0.1D;
                		this.xmax=0.1D;
            		}
            		else{
                		if(this.xmin<0.0D){
                    			this.xmin=this.xmin*1.1D;
                		}
                		else{
                    			this.xmax=this.xmax*1.1D;
                		}
            		}
        	}

        	if(this.ymin==this.ymax){
            		if(this.ymin==0.0D){
                		this.ymin=0.1D;
                		this.ymax=0.1D;
            		}
            		else{
                		if(this.ymin<0.0D){
                    			this.ymin=this.ymin*1.1D;
                		}
                		else{
                    			this.ymax=this.ymax*1.1D;
                		}
            		}
        	}
    	}

    	// Convert offset value to a string and reformat if in E format
    	protected static String offsetString(double offset){
        	String stroffset = String.valueOf(offset);
        	String substr1="", substr2="", substr3="";
        	String zero ="0";
        	int posdot = stroffset.indexOf('.');
        	int posexp = stroffset.indexOf('E');

		if(posexp==-1){
            		return stroffset;
        	}
        	else{
           		substr1 = stroffset.substring(posexp+1);
           		int n = Integer.parseInt(substr1);
           		substr1 = stroffset.substring(0,posexp);
           		if(n>=0){
                		for(int i=0; i<n; i++){
                			substr1 = substr1 + zero;
                		}
                		return substr1;
           		}
           		else{
                		substr2 = substr1.substring(0, posdot+1);
                		substr3 = substr1.substring(posdot+1);
                		for(int i=0; i<-n; i++){
                			substr2 = substr1 + zero;
                		}
                		substr2 = substr2 + substr3;
                		return substr2;
           		}
        	}
    	}

    	// check whether point in line segment is to be drawn
    	public boolean printcheck(boolean trim, int xoldpoint, int xnewpoint, int yoldpoint, int ynewpoint){

        	boolean btest2=true;

        	if(trim){
            		if(xoldpoint<xbot)btest2=false;
            		if(xoldpoint>xtop)btest2=false;
	            	if(xnewpoint<xbot)btest2=false;
        	    	if(xnewpoint>xtop)btest2=false;
            		if(yoldpoint>ybot)btest2=false;
	            	if(yoldpoint<ytop)btest2=false;
        	    	if(ynewpoint>ybot)btest2=false;
            		if(ynewpoint<ytop)btest2=false;
        	}

        	return btest2;
    	}

    	// Draw graph
    	public void graph(Graphics g){

        	// Set font type and size
        	g.setFont(new Font("serif", Font.PLAIN, this.fontsize));
        	FontMetrics fm = g.getFontMetrics();

        	// calculation of all graphing parameters and data scaling
        	axesScaleOffset();

        	// Draw title, legends and axes
        	String xoffstr = offsetString(xoffset);
        	String yoffstr = offsetString(yoffset);
        	String bunit1 = "  /( ";
        	String bunit2 = " )";
        	String bunit3 = "  / ";
        	String bunit4 = " ";
        	String bunit5 = " x 10";
        	String bunit6 = "10";
        	String nounit = " ";
        	String xbrack1 = bunit1;
        	String xbrack2 = bunit2;
        	String xbrack3 = bunit5;
        	if(this.xfac==0){
        	    xbrack1 = bunit3;
        	    xbrack2 = "";
        	    xbrack3 = "";
            }
            String ybrack1 = bunit1;
        	String ybrack2 = bunit2;
        	String ybrack3 = bunit5;
        	if(this.yfac==0){
        	    ybrack1 = bunit3;
        	    ybrack2 = "";
        	    ybrack3 = "";
            }
         	if(noxunits){
        	    if(xfac==0){
            		xbrack1=nounit;
            		xbrack2=nounit;
            		xbrack3=nounit;
            	}
            	else{
            	    xbrack1=bunit3;
            		xbrack2=bunit4;
            		xbrack3=bunit6;
        		}
        	}
        	if(noyunits){
        	    if(yfac==0){
            		ybrack1=nounit;
            		ybrack2=nounit;
            		ybrack3=nounit;
            	}
            	else{
            	    ybrack1=bunit3;
            	    ybrack2=bunit4;
            	    ybrack3=bunit6;
            	}
        	}

        	double xlen=xtop-xbot;
        	double ylen=ybot-ytop;

        	// Print title
        	String sp = " + ", sn = " - ";
        	String ss=sn;
        	g.drawString(this.graphtitle+" ", 15,15);
        	g.drawString(this.graphtitle2+" ", 15,35);
        	if(this.xoffset<0){
            		ss=sp;
            		xoffset=-xoffset;
        	}

        	// Print legends
	        int sw=0;
        	String ssx="", ssy="", sws1="", sws2="";
        	if(this.xfac==0 && this.xoffset==0){
                	g.drawString(this.xaxislegend+xbrack1+this.xaxisunits+xbrack2, xbot-4,ybot+32);
        	}
        	else{
            		if(this.xoffset==0){
                		ssx = this.xaxislegend + xbrack1 + this.xaxisunits + xbrack3;
	                	sw = fm.stringWidth(ssx);
        	        	g.drawString(ssx, xbot-4,ybot+42);
                		sws1=String.valueOf(-this.xfac-1);
                		g.drawString(sws1, xbot-4+sw+1,ybot+32);
                		sw += fm.stringWidth(sws1);
                		g.drawString(xbrack2, xbot-4+sw+1,ybot+42);
            		}
            		else{
                		if(this.xfac==0){
                    			g.drawString(this.xaxislegend + ss + xoffstr + xbrack1+this.xaxisunits+xbrack2, xbot-4,ybot+30);
                		}
                		else{
                    			ssx = this.xaxislegend + ss + xoffstr + xbrack1+this.xaxisunits+xbrack3;
                    			sw = fm.stringWidth(ssx);
                    			g.drawString(ssx, xbot-4,ybot+37);
                    			sws1 = String.valueOf(-this.xfac-1);
                    			g.drawString(sws1, xbot-4+sw+1,ybot+32);
                    			sw += fm.stringWidth(sws1);
                    			g.drawString(xbrack2, xbot-4+sw+1,ybot+37);
                		}
            		}
        	}

        	ss=sn;
        	if(yoffset<0){
            		ss=sp;
            		yoffset=-yoffset;
        	}

        	if(yfac==0 && yoffset==0){
            		g.drawString(this.yaxislegend+" ", 15,ytop-25);
            		g.drawString(ybrack1+this.yaxisunits+ybrack2, 15,ytop-10);
        	}
        	else{
            		if(yoffset==0){
                		g.drawString(this.yaxislegend, 15,ytop-35);
	                	sws1 = ybrack1+this.yaxisunits + ybrack3;
        	        	g.drawString(sws1, 15,ytop-15);
                		sw = fm.stringWidth(sws1);
                		sws2=String.valueOf(-this.yfac-1);
	                	g.drawString(sws2, 15+sw+1,ytop-20);
        	        	sw += fm.stringWidth(sws2);
                		g.drawString(ybrack2, 15+sw+1,ytop-15);
           		}
            		else{
                		if(yfac==0){
                    			g.drawString(this.yaxislegend + ss + yoffstr, 15,ytop-25);
                    			g.drawString(ybrack1+this.yaxisunits+ybrack2, 15,ytop-10);
                		}
                		else{
		                    ssy = this.yaxislegend + ss + yoffstr;
        			  	    g.drawString(ssy, 15,ytop-35);
 			                sws1 = ybrack1+this.yaxisunits + ybrack3;
                    			g.drawString(sws1, 15,ytop-15);
                    			sw = fm.stringWidth(sws1);
	                    		sws2=String.valueOf(-this.yfac-1);
        	            		g.drawString(sws2, 15+sw+1,ytop-20);
                	    		sw += fm.stringWidth(sws2);
                		    	g.drawString(ybrack2, 15+sw+1,ytop-15);
                		}
            		}
        	}

	        // Draw axes
	        int zdif=0, zold=0, znew=0, zzer=0;
        	double csstep=0.0D;
	        double xdenom=(double)(xhigh-xlow);
        	double ydenom=(double)(yhigh-ylow);

        	g.drawLine(xbot, ybot, xtop, ybot);
        	g.drawLine(xbot, ytop, xtop, ytop);
        	g.drawLine(xbot, ybot, xbot, ytop);
        	g.drawLine(xtop, ybot, xtop, ytop);


        	// Draw zero lines if drawn axes are not at zero and a zero value lies on an axis
        	if(xzero){
            		zdif=8;
            		zzer=xbot+(int)(((0.0-(double)xlow)/xdenom)*xlen);
            		g.drawLine(zzer,ytop,zzer,ytop+8);
            		g.drawLine(zzer,ybot,zzer,ybot-8);
            		zold=ytop;
            		while(zold+zdif<ybot){
                		znew=zold+zdif;
                		g.drawLine(zzer, zold, zzer, znew);
                		zold=znew+zdif;
            		}
        	}

        	if(yzero){
            		zdif=8;
            		zzer=ybot-(int)(((0.0-(double)ylow)/ydenom)*ylen);
            		g.drawLine(xbot,zzer,xbot+8,zzer);
            		g.drawLine(xtop,zzer,xtop-8,zzer);
            		zold=xbot;
            		while(zold+zdif<xtop){
                		znew=zold+zdif;
                		g.drawLine(zold, zzer, znew, zzer);
                		zold=znew+zdif;
            		}
        	}

         	// Draw tick marks and axis numbers
            int xt=0;
        	//double xtep=(double)(xtop-xbot)/((double)(this.xticks-1));
        	for(int ii=0; ii<this.xticks; ii++)
        	{
        	        xt=xbot+(int)(((this.xaxisno[ii]-(double)xlow)/xdenom)*xlen);
              		g.drawLine(xt,ybot,xt,ybot-8);
            		g.drawLine(xt,ytop,xt,ytop+8);
            		g.drawString(xaxischar[ii]+" ",xt-4,ybot+18);
        	}

        	int yt=0;
        	int yCharLenMax=yaxischar[0].length();
        	for(int ii=1; ii<this.yticks; ii++)if(yaxischar[ii].length()>yCharLenMax)yCharLenMax=yaxischar[ii].length();
        	int shift = (yCharLenMax-3)*5;
        	double ytep=(double)(-ytop+ybot)/((double)(this.yticks-1));
        	for(int ii=0; ii<this.yticks; ii++)
        	{
            		yt=ybot-(int)Math.round(ii*ytep);
            		yt=ybot-(int)(((this.yaxisno[ii]-(double)ylow)/ydenom)*ylen);
            		g.drawLine(xbot,yt,xbot+8,yt);
            		g.drawLine(xtop,yt,xtop-8,yt);
            		g.drawString(yaxischar[ii]+" ",xbot-30-shift,yt+4);
        	}

        	int dsum=0; // dashed line counter
        	boolean dcheck=true; // dashed line check

        	// Draw curves
        	int kk=0;
		    int xxp=0, yyp=0, yype=0;
		    int xoldpoint=0, xnewpoint=0, yoldpoint=0, ynewpoint=0;
        	int ps=0, psh=0, nxpoints=0;
        	double xics[]= new double[nipoints];
        	boolean btest2=true;

        	for(int i=0; i<this.ncurves; i++){
            		// cubic spline interpolation option
            		nxpoints=this.npoints[i];
            		double xcs[]= new double[nxpoints];
            		double ycs[]= new double[nxpoints];

     	       		if(lineopt[i]==1 || lineopt[i]==2){
                		CubicSpline cs = new CubicSpline(this.npoints[i]);
                		for(int ii=0; ii<nxpoints; ii++){
                    			xcs[ii]=this.data[kk][ii];
                		}
                		csstep=(xcs[nxpoints-1]-xcs[0])/(nipoints-1);
                		xics[0]=xcs[0];
                		for(int ii=1; ii<nipoints; ii++){
                    			xics[ii]=xics[ii-1]+csstep;
                		}
                		xics[nipoints-1] = xcs[nxpoints-1];
                 		for(int ii=0; ii<nxpoints; ii++){
                    			ycs[ii]=this.data[kk+1][ii];
                		}

                		cs.resetData(xcs, ycs);
                		cs.calcDeriv();
                		xoldpoint=xbot+(int)(((xcs[0]-(double)xlow)/xdenom)*xlen);
                		yoldpoint=ybot-(int)(((ycs[0]-(double)ylow)/ydenom)*ylen);
                		for(int ii=1; ii<nipoints; ii++){
                     			xnewpoint=xbot+(int)(((xics[ii]-(double)xlow)/xdenom)*xlen);
                    			ynewpoint=ybot-(int)(((cs.interpolate(xics[ii])-(double)ylow)/ydenom)*ylen);
                    			btest2=printcheck(trimopt[i], xoldpoint, xnewpoint, yoldpoint, ynewpoint);
                    			if(btest2){
                        			if(this.lineopt[i]==2){
                            				dsum++;
                            				if(dsum>dashlength[i]){
                                				dsum=0;
                                				if(dcheck){
                                    					dcheck=false;
                                				}
								else{
                                    					dcheck=true;
                                				}
                            				}
                        			}
                        			if(dcheck)g.drawLine(xoldpoint,yoldpoint,xnewpoint,ynewpoint);
                    			}
                    			xoldpoint=xnewpoint;
                    			yoldpoint=ynewpoint;
                		}
            		}

	            	if(lineopt[i]==3){
                		// Join points option
                		dsum=0;
                		dcheck=true;
                		xoldpoint=xbot+(int)((((this.data[kk][0])-(double)xlow)/xdenom)*xlen);
                		yoldpoint=ybot-(int)((((this.data[kk+1][0])-(double)ylow)/ydenom)*ylen);
                		for(int ii=1; ii<nxpoints; ii++){
                    			xnewpoint=xbot+(int)((((this.data[kk][ii])-(double)xlow)/xdenom)*xlen);
                    			ynewpoint=ybot-(int)((((this.data[kk+1][ii])-(double)ylow)/ydenom)*ylen);
                    			if(btest2)g.drawLine(xoldpoint,yoldpoint,xnewpoint,ynewpoint);
                    			xoldpoint=xnewpoint;
                    			yoldpoint=ynewpoint;
                		}
            		}

            		// Plot points
            		if(pointopt[i]>0){
                		for(int ii=0; ii<nxpoints; ii++){
                    			ps=this.pointsize[i];
                    			psh=ps/2;
                     			xxp=xbot+(int)(((this.data[kk][ii]-(double)xlow)/xdenom)*xlen);
                    			yyp=ybot-(int)(((this.data[kk+1][ii]-(double)ylow)/ydenom)*ylen);
                    			switch(pointopt[i]){
                        			case 1: g.drawOval(xxp-psh, yyp-psh, ps, ps);
                            				break;
                        			case 2: g.drawRect(xxp-psh, yyp-psh, ps, ps);
                            				break;
                        			case 3: g.drawLine(xxp-psh, yyp, xxp, yyp+psh);
                            				g.drawLine(xxp, yyp+psh, xxp+psh, yyp);
                            				g.drawLine(xxp+psh, yyp, xxp, yyp-psh);
                            				g.drawLine(xxp, yyp-psh, xxp-psh, yyp);
                            				break;
                          			case 4: g.fillOval(xxp-psh, yyp-psh, ps, ps);
                            				break;
                        			case 5: g.fillRect(xxp-psh, yyp-psh, ps, ps);
                            				break;
                        			case 6: for(int jj=0; jj<psh; jj++)g.drawLine(xxp-jj, yyp-psh+jj, xxp+jj, yyp-psh+jj);
                            				for(int jj=0; jj<=psh; jj++)g.drawLine(xxp-psh+jj, yyp+jj, xxp+psh-jj, yyp+jj);
                            				break;
                        			case 7: g.drawLine(xxp-psh, yyp-psh, xxp+psh, yyp+psh);
                            				g.drawLine(xxp-psh, yyp+psh, xxp+psh, yyp-psh);
                            				break;
                        			case 8: g.drawLine(xxp-psh, yyp, xxp+psh, yyp);
                            				g.drawLine(xxp, yyp+psh, xxp, yyp-psh);
                            				break;
                        			default:g.drawLine(xxp-psh, yyp-psh, xxp+psh, yyp+psh);
                            				g.drawLine(xxp-psh, yyp+psh, xxp+psh, yyp-psh);
                            				break;
                    			}

					if(this.errorbar[i]){
                        			yype=ybot-(int)(((errors[i][ii]-(double)ylow)/ydenom)*ylen);
                        			g.drawLine(xxp, yyp, xxp, yype);
                        			g.drawLine(xxp-4, yype, xxp+4, yype);
                        			yype=2*yyp-yype;
                        			g.drawLine(xxp, yyp, xxp, yype);
                        			g.drawLine(xxp-4, yype, xxp+4, yype);
                    			}
                		}
            		}
            		kk+=2;
        	}
    	}
}
