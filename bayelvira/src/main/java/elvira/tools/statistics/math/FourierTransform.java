/*
*   Fourier Transform
*
*   This class contains the method for performing a
*   Fast Fourier Transform (FFT) and associated methods
*   e.g. for obtaining a power spectrum, for windowing data.
*   Adapted from the Numerical Recipes methods written in the C language
*   Numerical Recipes in C, The Art of Scientific Computing,
*   W.H. Press, S.A. Teukolsky, W.T. Vetterling & B.P. Flannery,
*   Cambridge University Press, 2nd Edition (1992) pp 496 - 558.
*   (http://www.nr.com/).
*
*   AUTHOR: Michael Thomas Flanagan
*   DATE:   20 December 2003
*   UPDATE: 22 April 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   FourietTransform.html
*
*
*   Copyright (c) April 2004  Michael Thomas Flanagan
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

package elvira.tools.statistics.math;

import elvira.tools.statistics.math.*;
import elvira.tools.statistics.io.*;
import elvira.tools.statistics.complex.*;
import elvira.tools.statistics.plot.*;

public class FourierTransform{

	private double[] realData = null;       // array to hold a data set of real (double) numbers
	private boolean realDataSet = false;    // if true - the real data input array has been filled, if false - it has not.
	private Complex[] complexData = null;   // array to hold a data set of Complex numbers
	private boolean complexDataSet = false; // if true - the complex data input array has been filled, if false - it has not.
	private double[] fftData = null;        // array to hold a data set of complex numbers arranged as alternating
	                                        // real and imaginary parts, e.g. real_0 imag_0, real_1 imag_1, for the fast Fourier Transform method
	private boolean fftDataSet = false;     // if true - the fftData array has been filled, if false - it has not.
    private int fftDataLength = 0;          // Data length - length of complexData or half the length of realData
                                            //  must be an integer power of 2
	private Complex[] transformedDataComplex = null;  // transformed data set of Complex numbers f
	private double[] transformedDataFft = null; // transformed data set of double adjacent real and imaginary parts

    private double[] powerSpectrumResult = null;  // array to hold calculated power spectrum
    private boolean powSpecDone = false;    // = false - PowerSpectrum has not been called
                                            // = true  - PowewrSpectrum has been called

	private int windowOption = 0;	        //	Window Option
									        //		= 1; Bartlett - default option
									        //		= 2; Welch
									        //      = 3; Hann
									        //		= 4; Square
	private int segmentNumber = 0;	        //	Number of segments into which the data has been split
	private int segmentLength = 0;	        //	Number of of data points in a segment
	private int dataLength = 0;		        //  Number of data points in powerSpectrum method
	private boolean overlap = true;	        //	Data segment overlap option
									        //	= true; overlap by half segment length - smallest spectral variance per data point
									        //		good where data already recorded and data reduction is after the process
									        //		defalt option
									        //	= false;  no overlap - smallest spectral variance per conputer operation
	    							        //		good for real time data collection where data reduction is computer limited

	// constructors
	// No initialisation of the data variables
	public FourierTransform(){
	}

    // constuctor entering a data array of real numbers
	public FourierTransform(double[] realData){
	    this.realData = realData;
	    this.realDataSet = true;
	    int n = realData.length;
	    this.fftDataLength = n;
	    this.complexData = Complex.oneDarray(n);
	    this.fftData = new double[2*n];
	    int j = 0;
	    for(int i=0; i<n; i++){
	        this.complexData[i].reset(realData[i], 0.0D);
	        this.fftData[j] = realData[i];
	        j++;
	        this.fftData[j] = 0.0D;
	        j++;
	    }
	    this.complexDataSet = true;
	    this.fftDataSet = true;
	}

    // constuctor entering a data array of complex numbers
	public FourierTransform(Complex[] complexData){
	    this.complexData = complexData;
	    this.complexDataSet = true;
	    int n = complexData.length;
	    this.fftDataLength = n;
	    this.fftData = new double[2*n];
	    int j = 0;
	    for(int i=0; i<n-1; i++){
	        this.fftData[j] = this.complexData[i].getReal();
	        j++;
	        this.fftData[j] = this.complexData[i].getImag();
	        j++;
	    }
	    this.fftDataSet = true;
	}

    // Enter a data array of real numbers
    public void setData(double[] realData){
	    this.realData = realData;
	    this.realDataSet = true;
	    int n = realData.length;
	    this.fftDataLength = n;
	    this.complexData = Complex.oneDarray(n);
	    this.fftData = new double[2*n];
	    int j = 0;
	    for(int i=0; i<n; i++){
	        this.complexData[i].reset(realData[i], 0.0D);
	        this.fftData[j] = realData[i];
	        j++;
	        this.fftData[j] = 0.0D;
	        j++;
	    }
	    this.complexDataSet = true;
	    this.fftDataSet = true;
	}

    // Enter a data array of complex numbers
    public void setData(Complex[] complexData){
	    this.complexData = complexData;
	    this.complexDataSet = true;
	    int n = complexData.length;
	    this.fftDataLength = n;
	    this.fftData = new double[2*n];
	    int j = 0;
	    for(int i=0; i<n-1; i++){
	        this.fftData[j] = this.complexData[i].getReal();
	        j++;
	        this.fftData[j] = this.complexData[i].getImag();
	        j++;
	    }
	    this.fftDataSet = true;
	}

	// Enter a data array of adjacent alternating real and imaginary parts for fft method, fastFourierTransform
    public void setFftData(double[] fftdata){
	    this.fftData= fftdata;
	    this.fftDataSet = true;
	    this.fftDataLength = fftdata.length/2;

        this.complexData = Complex.oneDarray(this.fftDataLength);
	    for(int i=0; i<this.fftDataLength; i++){
	        complexData[i].reset(fftdata[2*i], fftdata[2*i+1]);
	    }
	    this.complexDataSet = true;

	    this.transformedDataComplex = Complex.oneDarray(this.fftDataLength);
	    this.transformedDataFft = new double[2*this.fftDataLength];
	}

	// Get the input data array (previously entered as real)
    public double[] getRealInputData(){
        if(!this.realDataSet){
		    System.out.println("real data set not entered or calculated - null returned");
		}
		return this.realData;
	}

	// Get the input data array
    public Complex[] getComplexInputData(){
        if(!this.complexDataSet){
		    System.out.println("complex data set not entered or calculated - null returned");
		}
		return this.complexData;
	}

	// Get the entered fast Fourier data array as adjacent real and imaginary pairs
    public double[] getAlternateInputData(){
        if(!this.fftDataSet){
		    System.out.println("fft data set not entered or calculted - null returned");
		}
	    return this.fftData;
	}

	// Get the transformed data as Complex
    public Complex[] getTransformedDataAsComplex(){
		return this.transformedDataComplex;
	}

	// Get the transformed data array as adjacent real and imaginary pairs
    public double[] getTransformedDataAsAlternate(){
		return this.transformedDataFft;
	}

	// Print the power spectrum to a text file
    public void printPowerSpectrum(String filename){
        if(!this.powSpecDone){
            System.out.println("printPowerSpectrum - powerSpectrum has not been called - no file printed");
		}
		else{
		    FileOutput fout = new FileOutput(filename);
		    fout.println("Power Spectrum Estimate Output File from FourierTransform");
		    fout.dateAndTimeln(filename);
		    fout.println();
		    fout.printtab("Frequency");
		    fout.println("Mean Square");
		    fout.printtab("(cycles per");
		    fout.println("Amplitude");
		    fout.printtab("gridpoint)");
		    fout.println(" ");
		    int n = this.powerSpectrumResult.length;
		    for(int i=0; i<n; i++){
		        fout.printtab(Fmath.truncate((double)i/(double)this.segmentLength, 4));
		        fout.printtab(Fmath.truncate(this.powerSpectrumResult[i], 4));
		    }
		    fout.close();
		}
	}

    // Display a plot of the power spectrum
    public void plotPowerSpectrum(String graphTitle){
        if(!this.powSpecDone){
            System.out.println("printPowerSpectrum - powerSpectrum has not been called - no plot displayed");
		}
		else{
		    int n = this.powerSpectrumResult.length;
		    double[] freq = new double[n];
		    for(int i=0; i<n; i++){
		        freq[i] = (double)i/(double)this.segmentLength;
		    }

		    PlotGraph pg = new PlotGraph(freq, this.powerSpectrumResult);
		    pg.setGraphTitle(graphTitle);
		    pg.setXaxisLegend("Frequency");
		    pg.setXaxisUnitsName("cycles per grid point");
		    pg.setYaxisLegend("Mean Square Amplitude");
		    pg.plot();
		}
	}

	// Display a plot of the power spectrum
	// no graph title provided
    public void plotPowerSpectrum(){
        if(!this.powSpecDone){
            System.out.println("printPowerSpectrum - powerSpectrum has not been called - no plot displayed");
		}
		else{
		    String graphTitle = "Estimation of Power Spectrum";
		    plotPowerSpectrum(graphTitle);
		}
	}

	// Get the power spectrum
    public double[] getPowerSpectrumResult(){
        if(!this.powSpecDone)System.out.println("getPowerSpectrumResult - powerSpectrum has not been called - null returned");
		return this.powerSpectrumResult;
	}

    // Base method for performing a Fast Fourier Transform
    // Based on the Numerical Recipes procedure four1
    // If isign is set to +1 this method replaces fftData[0 to 2*nn-1] by its discrete Fourier Transform
    // If isign is set to -1 this method replaces fftData[0 to 2*nn-1] by nn times its inverse discrete Fourier Transform
    // nn MUST be an integer power of 2.  This is not checked for in this method, fastFourierTransform(...), for speed.
    // If not checked for by the calling method, e.g. powerSpectrum(...) does, the method checkPowerOfTwo() may be used to check this.
    // The real and imaginary parts of the data are stored adjacently
    // i.e. fftData[0] holds the real part, fftData[1] holds the corresponding imaginary part of a data point
    // data array and data array length over 2 (nn) transferred as arguments
    // result NOT returned to this.transformedDataFft
    public void basicFft(double[] fftData, long nn, int isign)
    {
        double dtemp = 0.0D, wtemp = 0.0D, tempr = 0.0D, tempi = 0.0D;
        double theta = 0.0D, wr = 0.0D, wpr = 0.0D, wpi = 0.0D, wi = 0.0D;
	    long istep = 0L, m = 0L, mmax = 0L;
	    long n = nn << 1L;
	    long j = 1L;
	    int jj = 0;
	    for (int i=1;i<n;i+=2L) {
	        jj = (int)j-1;
		    if (j > i) {
		        dtemp = fftData[jj];
		        fftData[jj] = fftData[i-1];
		        fftData[i-1] = dtemp;
		        dtemp = fftData[jj+1];
		        fftData[jj+1] = fftData[i];
		        fftData[i] = dtemp;
		    }
		    m = n >> 1L;
		    while (m >= 2L && j > m) {
			    j -= m;
			    m >>= 1;
		    }
		    j += m;
	    }
	    mmax=2;
	    while (n > mmax) {
		    istep=mmax << 1L;
		    theta=isign*(6.28318530717959D/mmax);
		    wtemp=Math.sin(0.5D*theta);
		    wpr = -2.0D*wtemp*wtemp;
		    wpi=Math.sin(theta);
		    wr=1.0D;
		    wi=0.0D;
		    for (m=1;m<mmax;m+=2L) {
			    for (int i=(int)m;i<=n;i+=istep) {
					jj=(int)(i+mmax-1);
				    tempr=wr*fftData[jj]-wi*fftData[jj+1];
				    tempi=wr*fftData[jj+1]+wi*fftData[jj];
				    fftData[jj]=fftData[i-1]-tempr;
				    fftData[jj+1]=fftData[i]-tempi;
				    fftData[i-1] += tempr;
				    fftData[i] += tempi;
			    }
			    wr=(wtemp=wr)*wpr-wi*wpi+wr;
			    wi=wi*wpr+wtemp*wpi+wi;
		    }
		    mmax=istep;
	    }
    }


    // Method for performing a Fast Fourier Transform
    // Based on the Numerical Recipes procedure four1
    // If this.fftInverseOpt is set to false performs discrete Fourier Transform
    // If this.fftInverseOpt is set to true performs inverse discrete Fourier Transform
    public void transform(){

        // set up data array
        int isign = 1;
        if(!this.fftDataSet)throw new IllegalArgumentException("No data has been entered for the Fast Fourier Transform");
	    if(!FourierTransform.checkPowerOfTwo(this.fftDataLength))throw new IllegalArgumentException("Fast Fourier Transform data length ," + this.fftDataLength + ", is not an integer power of two");

        // Perform fft
        double[] hold = new double[this.fftDataLength*2];
        for(int i=0; i<this.fftDataLength*2; i++)hold[i] = this.fftData[i];
        basicFft(hold, this.fftDataLength, isign);
        for(int i=0; i<this.fftDataLength*2; i++)this.transformedDataFft[i] = hold[i];

        // fill transformed data arrays
        for(int i=0; i<this.fftDataLength; i++){
            this.transformedDataComplex[i].reset(this.transformedDataFft[2*i], this.transformedDataFft[2*i+1]);
        }
     }

    // Method for performing an inverse Fast Fourier Transform
    // Based on the Numerical Recipes procedure four1
    public void inverse(){

        // set up data array
        int isign = -1;
        if(!this.fftDataSet)throw new IllegalArgumentException("No data has been entered for the inverse Fast Fourier Transform");
	    if(!FourierTransform.checkPowerOfTwo(this.fftDataLength))throw new IllegalArgumentException("Inverse Fast Fourier Transform data length ," + this.fftDataLength + ", is not an integer power of two");

        // Perform fft
        double[] hold = new double[this.fftDataLength*2];
        for(int i=0; i<this.fftDataLength*2; i++)hold[i] = this.fftData[i];
        basicFft(hold, this.fftDataLength, isign);
        for(int i=0; i<this.fftDataLength*2; i++)this.transformedDataFft[i] = hold[i]/this.fftDataLength;

        // fill transformed data arrays
        for(int i=0; i<this.fftDataLength; i++){
            this.transformedDataComplex[i].reset(this.transformedDataFft[2*i], this.transformedDataFft[2*i+1]);
        }
     }

    // Obtains estimate of the power spectrum on data stored in instance variable realData
	// all other data; the number of segments, points per segment,
	// interval between points is entered through set methods
	public void powerSpectrum(){

        if(!this.realDataSet)throw new IllegalArgumentException("no real data has been entered");

		// perform the fft
		this.powerSpectrumResult = powerSpectrumCalc();
	    this.powSpecDone = true;
	}


	// Obtains estimate of the power spectrum on data in data file of the name stored in the String filename
	// all the data is entered from the data file: the number of segments, points per segment,
	// interval between points followed by the sampled data points at each sampling interval
	public void powerSpectrum(String filename){

   		// input the segmenting data
   		FileInput fin = new FileInput(filename);
		this.segmentNumber = fin.readInt();
		if((this.segmentNumber % 2)!=0)throw new IllegalArgumentException("Segment number must be a multiple of two");
		this.segmentLength = fin.readInt();
		if(!FourierTransform.checkPowerOfTwo(this.segmentLength))throw new IllegalArgumentException("Segment length must be a power of two");

		// perform the fft
		this.powerSpectrumResult = powerSpectrumCalc(fin);
	    this.powSpecDone = true;
	}

	// performs and returns results of the fft for the method powerSpectrum
	// uses instance variable input
	// based on the Numerical Recipe's C procedure spctrm
	private double[] powerSpectrumCalc(){

		int m   = this.segmentLength/2;
		int m2  = this.segmentLength;
		int m4  = this.segmentLength*2;
		int m43 = m4 + 3;
		int m44 = m4 + 4;

		int k   = this.segmentNumber/2;
		int iInput = 0;

		if(this.overlap){
			this.dataLength = this.segmentLength*(this.segmentNumber+1)/2;
		}
		else{
			this.dataLength = this.segmentLength*this.segmentNumber;
		}

	    double[] w1 = new double[m4];
		double[] w2 = new double[m];
	    double[] p  = new double[m];

	    int joffn = 0,j2 = 0;
	    double w = 0.0D,sumw = 0.0D,den = 0.0D;

	    double facm = m;
	    double facp = 1.0D/m;
	    for (int j=1;j<=m2;j++) sumw += Fmath.square((this.window(j,facm,facp)));  // accumulate the squared sum of weights
	    for (int j=0;j<m;j++) p[j]=0.0;     // initialise the spectrum to zero
	    if (this.overlap){
		    for (int j=0;j<m;j++){          // initialise the 'save' half buffer
		        w2[j] = this.realData[iInput];
		        iInput++;
		    }
		}
	    for (int kk=1;kk<=k;kk++) {
		    // loop over data set segments in groups of two
		    for (int joff = -1;joff<=0;joff++) { // get two complete segments into the work space
			    if (this.overlap) {
				    for (int j=0;j<m;j++) w1[joff+j+j]=w2[j];
				    for (int j=0;j<m;j++){
				        w2[j] = this.realData[iInput];
		                iInput++;
		            }
				    joffn=joff + m2;
				    for (int j=0;j<m;j++) w1[joffn+j+j]=w2[j];
			    }
			    else{
				    for (int j=joff+1;j<m4;j+=2){
				        w1[j] = this.realData[iInput];
		                iInput++;
		            }
			    }
		    }
		    for (int j=1;j<=m2;j++){         // apply window to data
			    j2=j+j;
			    w=this.window(j,facm,facp);
			    w1[j2-1] *= w;
			    w1[j2-2] *= w;
		    }
		    this.basicFft(w1,m2,1);            // Fourier transform the windowed data
		    p[0] += Fmath.square(w1[0])+Fmath.square(w1[1]);    // sum results into previous segments
		    for (int j=2;j<=m;j++) {
			    j2=j+j;
			    p[j-1] += Fmath.square(w1[j2-1])+Fmath.square(w1[j2-2])+Fmath.square(w1[m44-j2-1])+Fmath.square(w1[m43-j2-1]);
		    }
		    den += sumw;
	    }
	    den *= m4;                              // correct normalisation
	    for (int j=0;j<m;j++) p[j] /= den;      // normalise the output

	    return p;
	}

	// performs and returns results of the fft for the method powerSpectrum
	// uses file input
	// based on the Numerical Recipe's C procedure spctrm
	private double[] powerSpectrumCalc(FileInput fin){

		int m   = this.segmentLength/2;
		int m2  = this.segmentLength;
		int m4  = this.segmentLength*2;
		int m43 = m4 + 3;
		int m44 = m4 + 4;

		int k   = this.segmentNumber/2;

		if(this.overlap){
			this.dataLength = this.segmentLength*(this.segmentNumber+1)/2;
		}
		else{
			this.dataLength = this.segmentLength*this.segmentNumber;
		}

	    double[] w1 = new double[m4];
		double[] w2 = new double[m];
	    double[] p  = new double[m];

	    int joffn = 0,j2 = 0;
	    double w = 0.0D,sumw = 0.0D,den = 0.0D;

	    double facm = m;
	    double facp = 1.0D/m;
	    for (int j=1;j<=m2;j++) sumw += Fmath.square((this.window(j,facm,facp)));  // accumulate the squared sum of weights
	    for (int j=0;j<m;j++) p[j]=0.0;     // initialise the spectrum to zero
	    if (this.overlap){
		    for (int j=0;j<m;j++) w2[j] = fin.readDouble(); // initialise the 'save' half buffer
		}
	    for (int kk=1;kk<=k;kk++) {
		    // loop over data set segments in groups of two
		    for (int joff = -1;joff<=0;joff++) { // get two complete segments into the work space
			    if (this.overlap) {
				    for (int j=0;j<m;j++) w1[joff+j+j]=w2[j];
				    for (int j=0;j<m;j++) w2[j] = fin.readDouble();
				    joffn=joff + m2;
				    for (int j=0;j<m;j++) w1[joffn+j+j]=w2[j];
			    }
			    else{
				    for (int j=joff+1;j<m4;j+=2)w1[j] = fin.readDouble();
			    }
		    }
		    for (int j=1;j<=m2;j++){         // apply window to data
			    j2=j+j;
			    w=this.window(j,facm,facp);
			    w1[j2-1] *= w;
			    w1[j2-2] *= w;
		    }
		    this.basicFft(w1,m2,1);            // Fourier transform the windowed data
		    p[0] += Fmath.square(w1[0])+Fmath.square(w1[1]);    // sum results into previous segments
		    for (int j=2;j<=m;j++) {
			    j2=j+j;
			    p[j-1] += Fmath.square(w1[j2-1])+Fmath.square(w1[j2-2])+Fmath.square(w1[m44-j2-1])+Fmath.square(w1[m43-j2-1]);
		    }
		    den += sumw;
	    }
	    den *= m4;                              // correct normalisation
	    for (int j=0;j<m;j++) p[j] /= den;      // normalise the output

	    fin.close();
	    return p;
	}



	// method to perform windowing
	private double window(int i, double a, double b){
		double weight = 0.0D;

		switch(this.windowOption){
			// Bartlett  - default option
			case 1:	weight = 1.0D - Math.abs((i-a)*b);
					break;
			// Welch
			case 2:	weight = 1.0D - Fmath.square((i-a)*b);
					break;
			// Hann
			case 3:	weight = (1.0D - Math.cos(i*Math.PI*b))/2.0D;
					break;
			// Square
			case 4:	weight = 1.0D;
					break;
		}
		return weight;
	}

	// set window option - see above for options
	public void setWindowOption(int windowOpt){
		if(windowOpt<1 || windowOpt>4)throw new IllegalArgumentException("Window option must be 1, 2, 3 or 4; you have entered "+windowOpt);
		this.windowOption = windowOpt;
	}

	// get window option - see above for options
	public int getWindowOption(){
		return this.windowOption;
	}

	// set overlap option - see above for options
	public void setOverlapOption(boolean overlapOpt){
		this.overlap = overlapOpt;
	}

	// get overlap option - see above for options
	public boolean getOverlapOption(){
		return this.overlap;
	}

	// get the number of data points
	public int getDataLength(){
		return this.dataLength;
	}

	// set the segment length
	public void setSegmentLength(int sLen){
		if(!this.checkPowerOfTwo(sLen))throw new IllegalArgumentException("Segment length must be a power of two");
		this.segmentLength = sLen;
	}

	// get the segment length
	public int getSegmentLength(){
		return this.segmentLength;
	}

	// set the number of segments
	public void setSegmentNumber(int sNum){
		if((sNum % 2)!=0)throw new IllegalArgumentException("Segment number must be a multiple of two");
		this.segmentNumber = sNum;
	}

	// get the number of segments
	public int getSegmentNumber(){
		return this.segmentNumber;
	}

    // calculate the number of data points given the:
    // segment length (segLen), number of segments (segNum)
    // and the overlap option (overlap: true - overlap, false - no overlap)
    public static int calcDataLength(boolean overlap, int segLen, int segNum){
        if(overlap){
            return (segNum+1)*segLen/2;
        }
        else{
            return segNum*segLen;
        }
    }

	// Checks whether the argument n is a power of 2
	public static boolean checkPowerOfTwo(int n){
		boolean test = true;
		int m = n;
		while(test && m>1){
			if((m % 2)!=0){
				test = false;
			}
			else{
				m /= 2;
			}
		}
		return test;
	}

}
