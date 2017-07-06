/*
*   Class PlotPoleZero
*
*   Plots, in a window, the poles and zeros of a transfer function,
*   of the form of a polynomial over a polynomial, in either the s- or
*   z-plane given the coefficients of the polynomials either as two arrays
*   or as two types ComplexPolynom()
*
*   WRITTEN BY: Mick Flanagan
*
*   DATE:       July 2002
*   REVISED:    22 June 2003
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   PlotPoleZero.html
*
*   Copyright (c) June 2003
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
import elvira.tools.statistics.complex.Complex;
import elvira.tools.statistics.complex.ComplexPoly;
import elvira.tools.statistics.io.FileOutput;

public class PlotPoleZero{
        private ComplexPoly numerCoeff = null;     // ComplexPoly instance of the numerator polynomial
        private ComplexPoly denomCoeff = null;     // ComplexPoly instance of the denominator polynomial
        private Complex[] numerRoots = null;       // Roots of the numerator polynomial
        private Complex[] denomRoots = null;       // Roots of the denominator polynomial
        private double[][] data = null;            // Data for PlotGraph
        private int ndeg = 0;                      // degree of numerator polynomial
        private int ddeg = 0;                      // degree of denominator polynomial
        private int mdeg = 0;                      // maximum of the two polynomial degrees
        private int sorz = 0;                      // if 0 s or z plot, =1 s plane plot, =2 z plane plot
        private boolean zcircle = false;           // if true - a unit radius circle is plotted
        private boolean noImag = true;             // if true - no imaginary non-zero values
        private boolean noReal = true;             // if true - no real non-zero values

        // Constructors
        // numer    Array of coefficients of the numerator polynomial
        // denom    Array of coefficients of the denominator polynomial
        // ComplexPoly coefficients
        public PlotPoleZero(ComplexPoly numer, ComplexPoly denom){

                this.ndeg = numer.getDeg();
                this.ddeg = denom.getDeg();
                this.mdeg = (int)Math.max(ndeg, ddeg);
                this.numerCoeff = ComplexPoly.copy(numer);
                this.denomCoeff = ComplexPoly.copy(denom);
                this.numerRoots = Complex.oneDarray(ndeg);
                this.denomRoots = Complex.oneDarray(ddeg);
        }

        // Two arrays of Complex coefficients
                public PlotPoleZero(Complex[] numer, Complex[] denom){

                this.ndeg = numer.length-1;
                this.ddeg = denom.length-1;
                this.mdeg = (int)Math.max(ndeg, ddeg);
                this.numerCoeff = new ComplexPoly(numer);
                this.denomCoeff = new ComplexPoly(denom);
                this.numerRoots = Complex.oneDarray(ndeg);
                this.denomRoots = Complex.oneDarray(ddeg);
        }

        // Two arrays of double coefficients
        public PlotPoleZero(double[] numer, double[] denom){

                this.ndeg = numer.length-1;
                this.ddeg = denom.length-1;
                this.mdeg = (int)Math.max(ndeg, ddeg);
                this.numerCoeff = new ComplexPoly(numer);
                this.denomCoeff = new ComplexPoly(denom);
                this.numerRoots = Complex.oneDarray(ndeg);
                this.denomRoots = Complex.oneDarray(ddeg);
        }

        // Sets plot to s-plane plot
        public void setS(){
                this.sorz=1;
        }

        // Sets plot to z-plane plot
        public void setZ(){
                this.sorz=2;
                this.zcircle=true;
        }

        // Sets plot a unit radius circle.
        public void setCircle(){
                this.zcircle=true;
                if(this.sorz!=2)sorz=2;
        }

        // Unsets plot a unit radius circle.
        public void unsetCircle(){
                this.zcircle=false;
        }

        // Calculate roots and plot and write to text file
        // Plot title given
        public void pzPlot(String title){
                double[] zerosReal = null;
                double[] zerosImag = null;
                double[] polesReal = null;
                double[] polesImag = null;
                double[] xAxisIfRealZero = null;
                double[] yAxisIfRealZero = null;
                double[] xAxisIfImagZero = null;
                double[] yAxisIfImagZero = null;
                double[] xAxisCircle1 = null;
                double[] yAxisCircle1 = null;
                double[] xAxisCircle2 = null;
                double[] yAxisCircle2 = null;
                double absReal = 0.0D;
                double absImag = 0.0D;
                double zeroLimit = 1e-5;
                int ncirc = 600;
                double stp = 2.0/(double)(ncirc-1);
                int maxPoints = 0;

                int mm=0;
                if(this.ndeg>0){
                    mm++;
                    zerosReal = new double[this.ndeg];
                    zerosImag = new double[this.ndeg];
                    this.numerRoots = this.numerCoeff.roots();
                    for(int i=0; i<this.ndeg; i++){
                        zerosReal[i] = this.numerRoots[i].getReal();
                        zerosImag[i] = this.numerRoots[i].getImag();
                        if(!numerRoots[i].isZero()){
                            absReal = Math.abs(zerosReal[i]);
                            absImag = Math.abs(zerosImag[i]);
                            if(absReal>absImag){
                                if(absImag<zeroLimit*absReal)zerosImag[i]=0.0D;
                            }
                            else{
                                if(absReal<zeroLimit*absImag)zerosReal[i]=0.0D;
                            }
                        }
                        if(zerosReal[i]!=0.0D)this.noReal=false;
                        if(zerosImag[i]!=0.0D)this.noImag=false;
                    }
                    maxPoints = ndeg;
                }

                if(this.ddeg>0){
                    mm++;
                    polesReal = new double[this.ddeg];
                    polesImag = new double[this.ddeg];
                    this.denomRoots = this.denomCoeff.roots();
                      for(int i=0; i<this.ddeg; i++){
                        polesReal[i] = this.denomRoots[i].getReal();
                        polesImag[i] = this.denomRoots[i].getImag();
                        if(!denomRoots[i].isZero()){
                            absReal = Math.abs(polesReal[i]);
                            absImag = Math.abs(polesImag[i]);
                            if(absReal>absImag){
                                if(absImag<zeroLimit*absReal)polesImag[i]=0.0D;
                            }
                            else{
                                if(absReal<zeroLimit*absImag)polesReal[i]=0.0D;
                            }
                        }
                        if(polesReal[i]!=0.0D)this.noReal=false;
                        if(polesImag[i]!=0.0D)this.noImag=false;
                    }
                    if(ddeg>maxPoints)maxPoints=ddeg;
                }

                if(this.noReal){
                    mm++;
                    xAxisIfRealZero = new double[2];
                    xAxisIfRealZero[0]=1.D;
                    xAxisIfRealZero[1]=-1.0D;
                    yAxisIfRealZero = new double[2];
                    yAxisIfRealZero[0]=0.0D;
                    yAxisIfRealZero[1]=0.0D;
                    if(2>maxPoints)maxPoints=2;
                }

                if(this.noImag){
                    mm++;
                    xAxisIfImagZero = new double[2];
                    xAxisIfImagZero[0]=0.0D;
                    xAxisIfImagZero[1]=0.0D;
                    yAxisIfImagZero = new double[2];
                    yAxisIfImagZero[0]=1.0D;
                    yAxisIfImagZero[1]=-1.0D;
                    if(2>maxPoints)maxPoints=2;
                }

                if(this.zcircle){
                    mm+=2;
                    xAxisCircle1[0]=-1.0;
                    yAxisCircle1[0]=0.0;
                    xAxisCircle2[0]=-1.0;
                    yAxisCircle2[0]=0.0;
                    for(int i=1; i<ncirc; i++){
                        xAxisCircle1[i]=xAxisCircle1[i-1]+stp;
                        yAxisCircle1[i]=Math.sqrt(1.0-xAxisCircle1[i]*xAxisCircle1[i]);
                        xAxisCircle2[i]=xAxisCircle2[i-1]+stp;
                        yAxisCircle2[i]=-yAxisCircle1[i];
                    }
                    if(ncirc>maxPoints)maxPoints=ncirc;
                }
                int ii = 0;

                // Create array for data to be plotted
                double[][] data = PlotGraph.data(mm, maxPoints);
                boolean[] trim = new  boolean[mm];
                boolean[] minmax = new  boolean[mm];
                int[] line = new int[mm];
                int[] point = new int[mm];

                // Fill above array with data to be plotted
                ii=0;
                if(this.ndeg>0){
                        line[ii]=0;
                        point[ii]=1;
                        trim[ii]=false;
                        minmax[ii]=true;
                        for(int i=0; i<ndeg; i++){
                            data[2*ii][i]=zerosReal[i];
                            data[2*ii+1][i]=zerosImag[i];
                        }
                        ii++;
                }
                if(this.ddeg>0){
                        line[ii]=0;
                        point[ii]=7;
                        trim[ii]=false;
                        minmax[ii]=true;
                        for(int i=0; i<ddeg; i++){
                            data[2*ii][i]=polesReal[i];
                            data[2*ii+1][i]=polesImag[i];
                        }
                        ii++;
                }
                if(this.zcircle){
                        line[ii]=2;
                        point[ii]=0;
                        trim[ii]=true;
                        minmax[ii]=false;
                        for(int i=0; i<ncirc; i++){
                            data[2*ii][i]=xAxisCircle1[i];
                            data[2*ii+1][i]=yAxisCircle1[i];
                        }
                        ii++;
                        line[ii]=2;
                        point[ii]=0;
                        trim[ii]=true;
                        minmax[ii]=false;
                        for(int i=0; i<ncirc; i++){
                            data[2*ii][i]=xAxisCircle2[i];
                            data[2*ii+1][i]=yAxisCircle2[i];
                        }
                        ii++;
                }
                if(this.noReal){
                        line[ii]=0;
                        point[ii]=0;
                        trim[ii]=false;
                        minmax[ii]=true;
                        for(int i=0; i<2; i++){
                            data[2*ii][i]=xAxisIfRealZero[i];
                            data[2*ii+1][i]=yAxisIfRealZero[i];
                        }
                        ii++;
                }
                if(this.noImag){
                        line[ii]=0;
                        point[ii]=0;
                        trim[ii]=false;
                        minmax[ii]=true;

                        for(int i=0; i<2; i++){
                            data[2*ii][i]=xAxisIfImagZero[i];
                            data[2*ii+1][i]=yAxisIfImagZero[i];
                        }
                        ii++;
                }

                // Create an instance of PlotGraph with above data
                PlotGraph pg = new PlotGraph(data);
                pg.setLine(line);
                pg.setPoint(point);
                pg.setTrimopt(trim);
                pg.setMinmaxopt(minmax);
                pg.setXlowfac(0.0D);
                pg.setYlowfac(0.0D);
                pg.setNoOffset(true);

                switch(sorz){
                        case 0:
                                pg.setGraphTitle("Pole Zero Plot: "+title);
                                pg.setXaxisLegend("Real part of s or z");
                                pg.setYaxisLegend("Imaginary part of s or z");
                                break;
                        case 1:
                                pg.setGraphTitle("Pole Zero Plot (s-plane): "+title);
                                pg.setXaxisLegend("Real part of s");
                                pg.setYaxisLegend("Imaginary part of s");
                                break;
                        case 2:
                                pg.setGraphTitle("Pole Zero Plot (z-plane): "+title);
                                pg.setXaxisLegend("Real part of z");
                                pg.setYaxisLegend("Imaginary part of z");
                                break;
                }

                // Plot poles and zeros
                pg.plot();

                // Open and write an output file

                Complex[] numval = numerCoeff.polyNomCopy();
                Complex[] denval = denomCoeff.polyNomCopy();

                FileOutput fout = new FileOutput("PoleZeroOutput.txt");

                fout.println("Output File for Program PlotPoleZero");
                if(this.sorz==1)fout.println("An s-plane plot");
                if(this.sorz==2)fout.println("A z-plane plot");
                fout.dateAndTimeln(title);
                fout.println();
                fout.println("Numerator polynomial coefficients");
                for(int i=0;i<=ndeg;i++){
                    fout.print(numval[i].toString());
                    if(i<ndeg){
                        fout.printcomma();
                        fout.printsp();
                    }
                }
                fout.println();
                fout.println();

                fout.println("Denominator polynomial coefficients");
                for(int i=0;i<=ddeg;i++){
                    fout.print(denval[i].toString());
                    if(i<ddeg){
                        fout.printcomma();
                        fout.printsp();
                    }
                }
                fout.println();
                fout.println();

                fout.println("Numerator roots (zeros)");
                    if(ndeg<1){
                        fout.println("No zeros");
                    }
                    else{
                        for(int i=0;i<ndeg;i++){
                            fout.print(Complex.truncate(numerRoots[i],6));
                            if(i<ndeg-1){
                                fout.printcomma();
                                fout.printsp();
                            }
                        }
                        fout.println();
                        fout.println();
                    }

                    fout.println("Denominator roots (poles)");
                    if(ddeg<1){
                        fout.println("No poles");
                    }
                    else{
                        for(int i=0;i<ddeg;i++){
                            fout.print(Complex.truncate(denomRoots[i],6));
                            if(i<ddeg-1){
                                fout.printcomma();
                                fout.printsp();
                            }
                        }
                        fout.println();
                        fout.println();
                    }

                    if(this.sorz==2){
                        fout.println("Denominator pole radial distances on the z-plane");
                        if(ddeg<1){
                            fout.println("No poles");
                        }
                        else{
                            for(int i=0;i<ddeg;i++){
                                fout.print(Fmath.truncate(denomRoots[i].abs(),6));
                                if(i<ddeg-1){
                                    fout.printcomma();
                                    fout.printsp();
                                }
                            }
                        }
                        fout.println();
                        fout.println();
                    }

                    boolean testroots=true;
                    if(this.sorz==1){
                        for(int i=0;i<ddeg;i++){
                            if(denomRoots[i].getReal()>0)testroots=false;
                        }
                        if(testroots){
                                fout.println("All pole real parts are less than or equal to zero - stable system");
                        }
                        else{
                                fout.println("At least one pole real part is greater than zero - unstable system");
                        }
                    }

                    if(this.sorz==2){
                        for(int i=0;i<ddeg;i++){
                                if(Fmath.truncate(denomRoots[i].abs(),6)>1.0)testroots=false;
                        }
                        if(testroots){
                                fout.println("All pole distances from the z-plane zero are less than or equal to one - stable system");
                        }
                        else{
                                fout.println("At least one pole distance from the z-plane zero is greater than one - unstable system");
                        }
                }

                fout.println();
                fout.println("End of file");
                fout.close();

        }

        // Calculate roots and plot and write to text file
        // No plot title given
        public void pzPlot(){
                String title = "no file title provided";
                pzPlot(title);
        }

}
