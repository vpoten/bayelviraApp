/*
*   Class   Stat
*
*   USAGE:  Statistical functions
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:    June 2002 as part of Fmath
*   AMENDED: 12 May 2003 Statistics separated out from Fmath as a new class
*   UPDATE:  22 June 2003 and 22 April 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   Stat.html
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

package elvira.tools.statistics.analysis;

import java.util.ArrayList;
import java.util.Random;
import elvira.tools.statistics.math.Fmath;
import elvira.tools.statistics.integration.Integration;
import elvira.tools.statistics.integration.IntegralFunction;
import elvira.tools.statistics.plot.PlotGraph;
import elvira.tools.statistics.analysis.*;


// Class to evaluate the linear correlation coefficient probablity function
// Needed in calls to Integration.gaussQuad
class CorrCoeff implements IntegralFunction{

         public double a;

         public double function(double x){
                  double y = Math.pow((1.0D - x*x),a);
                  return y;
         }
}

//Statical methods class Stat
public class Stat{


        // A small number close to the smallest representable floating point number
        public static final double FPMIN = 1e-300;

        // PRIVATE MEMBERS FOR USE IN FOLLOWING PUBLIC METHODS
        // GAMMA FUNCTIONS
        //  Lanczos Gamma Function approximation - N (number of coefficients -1)
        private static int lgfN = 6;
        //  Lanczos Gamma Function approximation - Coefficients
        private static double[] lgfCoeff = {1.000000000190015, 76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5};
        //  Lanczos Gamma Function approximation - small gamma
        private static double lgfGamma = 5.0;
        //  Maximum number of iterations allowed in Incomplete Gamma Function calculations
        private static int igfiter = 1000;
        //  Tolerance used in terminating series in Incomplete Gamma Function calculations
        private static double igfeps = 1e-8;

        // METHODS
// Sum of a 1D array of doubles, aa
        public static double sum(double[] aa){
                double sum=0.0D;
                for(int i=0; i<aa.length; i++){
                        sum+=aa[i];
                }
                return sum;
        }
        
        public static long sum(long[] aa){
            long sum=0;
            for(int i=0; i<aa.length; i++){
                    sum+=aa[i];
            }
            return sum;
    }

       public static int sum(ArrayList<Integer> aa){
           int sum=0;
           for(Integer a:aa){
                   sum=sum+a;
           }
           return sum;
    	   
       }
        
//      Max of a 1D array of doubles, aa
        public static double max(double[] aa){
                double max=Double.NEGATIVE_INFINITY;
                double aux;
                for(int i=0; i<aa.length; i++){
                	aux =aa[i];
                	if (aux>max){
                		max=aux;
                	}
                }
                return max;
        }
        
       
        
//      Max of a 1D array of integers, aa
        public static int max(int[] aa){
                int max=Integer.MIN_VALUE;
                int aux;
                for(int i=0; i<aa.length; i++){
                	aux =aa[i];
                	if (aux>max){
                		max=aux;
                	}
                }
                return max;
        }
        
//      Max of a 1D array of long, aa
        public static long max(long[] aa){
            long max=Integer.MIN_VALUE;
            long aux;
            for(int i=0; i<aa.length; i++){
            	aux =aa[i];
            	if (aux>max){
            		max=aux;
            	}
            }
            return max;
    }
        
//      Min of a 1D array of integers, aa
        public static int min(int[] aa){
                int min=Integer.MAX_VALUE;
                int aux;
                for(int i=0; i<aa.length; i++){
                	aux =aa[i];
                	if (aux<min){
                		min=aux;
                	}
                }
                return min;
        }
     
//      Min of a 1D array of long, aa
        public static long min(long[] aa){
            long min=Integer.MAX_VALUE;
            long aux;
            for(int i=0; i<aa.length; i++){
            	aux =aa[i];
            	if (aux<min){
            		min=aux;
            	}
            }
            return min;
    }

//      Max of a 1D array of doubles, aa
        public static double min(double[] aa){
                double min=Double.POSITIVE_INFINITY;
                double aux;
                for(int i=0; i<aa.length; i++){
                	aux =aa[i];
                	if (aux<min){
                		min=aux;
                	}
                }
                return min;
        }
        
      
        // Mean of a 1D array of doubles, aa
        public static double mean(double[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                return sum/((double)n);
        }

        // Mean of a 1D array of floats, aa
        public static float mean(float[] aa){
                int n = aa.length;
                float sum=0.0F;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                return sum/((float)n);
        }

        // Median of a 1D array of doubles, aa
        public static double median(double[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                double med = 0.0D;
                double[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = bb[nOverTwo];
                }
                else{
                    med = (bb[nOverTwo-1]+bb[nOverTwo])/2.0D;
                }

                return med;
        }

        // Median of a 1D array of floats, aa
        public static float median(float[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                float med = 0.0F;
                float[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = bb[nOverTwo];
                }
                else{
                    med = (bb[nOverTwo-1]+bb[nOverTwo])/2.0F;
                }

                return med;
        }

        // Standard deviation of a 1D array of doubles, aa
        public static double standardDeviation(double[] aa){
                return Math.sqrt(variance(aa));
        }

        // Standard deviation of a 1D array of floats, aa
        public static float standardDeviation(float[] aa){
                return (float) Math.sqrt(variance(aa));
        }
        
        
        public static double standardDeviation(long[] aa){
            return (double) Math.sqrt(variance(aa));
    }
        /*
        // Variance of a 1D array of doubles, aa
        public static double cuasivariance(double[] aa){
                int n = aa.length;
                double sum=0.0, mean;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                mean=sum/((double)n);
                sum=0.0;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square(aa[i]-mean);
                }
                return sum/((double)(n-1));
        }*/

        // Variance of a 1D array of doubles, aa
        public static double variance(double[] aa){
                int n = aa.length;
                double sum=0.0, mean;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                mean=sum/((double)n);
                sum=0.0;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square(aa[i]-mean);
                }
                return sum/((double)(n-1));
        }

        // Variance of a 1D array of floats, aa
        public static float variance(float[] aa){
                int n = aa.length;
                float sum=0.0F, mean;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                mean=sum/((float)n);
                sum=0.0F;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square(aa[i]-mean);
                }
                return sum/((float)(n-1));
        }
        
        public static double variance(long[] aa){
            int n = aa.length;
            double sum=0.0, mean;
            for(int i=0; i<n; i++){
                    sum+=aa[i];
            }
            mean=sum/((double)n);
            sum=0.0;
            for(int i=0; i<n; i++){
                    sum+=Fmath.square(aa[i]-mean);
            }
            return sum/((double)(n-1));
    }

        // Gamma function
        // Lanczos approximation (6 terms)
        public static double gamma(double x){

                double xcopy = x;
                double first = x + lgfGamma + 0.5;
                double second = lgfCoeff[0];
                double fg = 0.0D;

                if(x>=0.0){
                        if(x>=1.0D && x-(int)x==0.0D){
                                fg = Stat.factorial(x)/x;
                        }
                        else{
                                first = Math.pow(first, x + 0.5)*Math.exp(-first);
                                for(int i=1; i<=lgfN; i++)second += lgfCoeff[i]/++xcopy;
                                fg = first*Math.sqrt(2.0*Math.PI)*second/x;
                        }
                }
                else{
                         fg = -Math.PI/(x*Stat.gamma(-x)*Math.sin(Math.PI*x));
                }
                return fg;
        }

        // Return the Lanczos constant gamma
        public static double getLanczosGamma(){
                return Stat.lgfGamma;
        }

        // Return the Lanczos constant N (number of coeeficients + 1)
        public static int getLanczosN(){
                return Stat.lgfN;
        }

        // Return the Lanczos coeeficients
        public static double[] getLanczosCoeff(){
                int n = Stat.getLanczosN()+1;
                double[] coef = new double[n];
                for(int i=0; i<n; i++){
                        coef[i] = Stat.lgfCoeff[i];
                }
                return coef;
        }

        // Return the nearest smallest representable floating point number to zero with mantissa rounded to 1.0
        public static double getFpmin(){
                return Stat.FPMIN;
        }

        // log to base e of the Gamma function
        // Lanczos approximation (6 terms)
        public static double logGamma(double x){
                double xcopy = x;
                double fg = 0.0D;
                double first = x + lgfGamma + 0.5;
                double second = lgfCoeff[0];

                if(x>=0.0){
                        if(x>=1.0 && x-(int)x==0.0){
                                fg = Stat.logFactorial(x)-Math.log(x);
                        }
                        else{
                                first -= (x + 0.5)*Math.log(first);
                                for(int i=1; i<=lgfN; i++)second += lgfCoeff[i]/++xcopy;
                                fg = Math.log(Math.sqrt(2.0*Math.PI)*second/x) - first;
                        }
                }
                else{
                        fg = Math.PI/(Stat.gamma(1.0D-x)*Math.sin(Math.PI*x));

                        if(fg!=1.0/0.0 && fg!=-1.0/0.0){
                                if(fg<0){
                                         throw new IllegalArgumentException("\nThe gamma function is negative");
                                }
                                else{
                                        fg = Math.log(fg);
                                }
                        }
                }
                return fg;
        }

        // Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        public static double incompleteGamma(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                double igf = 0.0D;

                if(x < a+1.0D){
                        // Series representation
                        igf = incompleteGammaSer(a, x);
                }
                else{
                        // Continued fraction representation
                        igf = incompleteGammaFract(a, x);
                }
                return igf;
        }

        // Complementary Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
        // Also known as the Incomplete Gamma Function
        public static double incompleteGammaComplementary(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                double igf = 0.0D;

                if(x!=0.0D){
                        if(x==1.0D/0.0D)
                        {
                                igf=1.0D;
                        }
                        else{
                                if(x < a+1.0D){
                                        // Series representation
                                        igf = 1.0D - incompleteGammaSer(a, x);
                                }
                                else{
                                        // Continued fraction representation
                                        igf = 1.0D - incompleteGammaFract(a, x);
                                }
                        }
                }
                return igf;
        }

        // Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Series representation of the function - valid for x < a + 1
        public static double incompleteGammaSer(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                if(x>=a+1) throw new IllegalArgumentException("\nx >= a+1   use Continued Fraction Representation");

                int i = 0;
                double igf = 0.0D;
                boolean check = true;

                double acopy = a;
                double sum = 1.0/a;
                double incr = sum;
                double loggamma = Stat.logGamma(a);

                while(check){
                        ++i;
                        ++a;
                        incr *= x/a;
                        sum += incr;
                        if(Math.abs(incr) < Math.abs(sum)*Stat.igfeps){
                                igf = sum*Math.exp(-x+acopy*Math.log(x)- loggamma);
                                check = false;
                        }
                        if(i>=Stat.igfiter){
                                check=false;
                                igf = sum*Math.exp(-x+acopy*Math.log(x)- loggamma);
                                System.out.println("\nMaximum number of iterations were exceeded in Stat.incompleteGammaSer().\nCurrent value returned.\nIncrement = "+String.valueOf(incr)+".\nSum = "+String.valueOf(sum)+".\nTolerance =  "+String.valueOf(igfeps));
                        }
                }
                return igf;
        }

        // Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Continued Fraction representation of the function - valid for x >= a + 1
        // This method follows the general procedure used in Numerical Recipes for C,
        // The Art of Scientific Computing
        // by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
        // Cambridge University Press,   http://www.nr.com/
        public static double incompleteGammaFract(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                if(x<a+1) throw new IllegalArgumentException("\nx < a+1   Use Series Representation");

                int i = 0;
                double ii = 0;
                double igf = 0.0D;
                boolean check = true;

                double loggamma = Stat.logGamma(a);
                double numer = 0.0D;
                double incr = 0.0D;
                double denom = x - a + 1.0D;
                double first = 1.0D/denom;
                double term = 1.0D/FPMIN;
                double prod = first;

                while(check){
                        ++i;
                        ii = (double)i;
                        numer = -ii*(ii - a);
                        denom += 2.0D;
                        first = numer*first + denom;
                        if(Math.abs(first) < Stat.FPMIN){
                            first = Stat.FPMIN;
                        }
                        term = denom + numer/term;
                        if(Math.abs(term) < Stat.FPMIN){
                            term = Stat.FPMIN;
                         }
                        first = 1.0D/first;
                        incr = first*term;
                        prod *= incr;
                        if(Math.abs(incr - 1.0D) < igfeps)check = false;
                        if(i>=Stat.igfiter){
                                check=false;
                                System.out.println("\nMaximum number of iterations were exceeded in Stat.incompleteGammaFract().\nCurrent value returned.\nIncrement - 1 = "+String.valueOf(incr-1)+".\nTolerance =  "+String.valueOf(igfeps));
                        }
                }
                igf = 1.0D - Math.exp(-x+a*Math.log(x)-loggamma)*prod;
                return igf;
        }

        // Reset the maximum number of iterations allowed in the calculation of the incomplete gamma functions
        public static void setIncGammaMaxIter(int igfiter){
                Stat.igfiter=igfiter;
        }

        // Return the maximum number of iterations allowed in the calculation of the incomplete gamma functions
        public static int getIncGammaMaxIter(){
                return Stat.igfiter;
        }

        // Reset the tolerance used in the calculation of the incomplete gamma functions
        public static void setIncGammaTol(double igfeps){
                Stat.igfeps=igfeps;
        }

        // Return the tolerance used in the calculation of the incomplete gamm functions
        public static double getIncGammaTol(){
                return Stat.igfeps;
        }

        // Beta function
        public static double beta(double z, double w){
                return Math.exp(logGamma(z) + logGamma(w) - logGamma(z + w));
        }

        // Incomplete Beta function
        // Continued Fraction approximation (see Numerical recipies for details of method)
        public static double incompleteBeta(double z, double w, double x){
            if(x<0.0D || x>1.0D)throw new IllegalArgumentException("Argument x, "+x+", must be lie between 0 and 1 (inclusive)");
            double ibeta = 0.0D;
            if(x==0.0D){
                ibeta=0.0D;
            }
            else{
                if(x==1.0D){
                    ibeta=1.0D;
                }
                else{
                    // Term before continued fraction
                    ibeta = Math.exp(Stat.logGamma(z+w) - Stat.logGamma(z) - logGamma(w) + z*Math.log(x) + w*Math.log(1.0D-x));
                    // Continued fraction
                    if(x < (z+1.0D)/(z+w+2.0D)){
                        ibeta = ibeta*Stat.contFract(z, w, x)/z;
                    }
                    else{
                        // Use symmetry relationship
                        ibeta = 1.0D - ibeta*Stat.contFract(w, z, 1.0D-x)/w;
                    }
                }
            }
            return ibeta;
        }

        // Incomplete fraction summation used in the method incompleteBeta
        // modified Lentz's method
        public static double contFract(double a, double b, double x){
            int maxit = 500;
            double eps = 3.0e-7;
            double aplusb = a + b;
            double aplus1 = a + 1.0D;
            double aminus1 = a - 1.0D;
            double c = 1.0D;
            double d = 1.0D - aplusb*x/aplus1;
            if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
            d = 1.0D/d;
            double h = d;
            double aa = 0.0D;
            double del = 0.0D;
            int i=1, i2=0;
            boolean test=true;
            while(test){
                i2=2*i;
                aa = i*(b-i)*x/((aminus1+i2)*(a+i2));
                d = 1.0D + aa*d;
                if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
                c = 1.0D + aa/c;
                if(Math.abs(c)<Stat.FPMIN)c = FPMIN;
                d = 1.0D/d;
                h *= d*c;
                aa = -(a+i)*(aplusb+i)*x/((a+i2)*(aplus1+i2));
                d = 1.0D + aa*d;
                if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
                c = 1.0D + aa/c;
                if(Math.abs(c)<Stat.FPMIN)c = FPMIN;
                d = 1.0D/d;
                del = d*c;
                h *= del;
                i++;
                if(Math.abs(del-1.0D) < eps)test=false;
                if(i>maxit){
                    test=false;
                    System.out.println("Maximum number of iterations ("+maxit+") exceeded in Stat.contFract in Stat.incomplete Beta");
                }
            }
            return h;

        }

        // Error Function
        public static double erf(double x){
                double erf = 0.0D;
                if(x!=0.0){
                        if(x==1.0D/0.0D){
                                erf = 1.0D;
                        }
                        else{
                                if(x>=0){
                                        erf = Stat.incompleteGamma(0.5, x*x);
                                }
                                else{
                                        erf = -Stat.incompleteGamma(0.5, x*x);
                                }
                        }
                }
                return erf;
        }

        // Complementary Error Function
        public static double erfc(double x){
                double erfc = 1.0D;
                if(x!=0.0){
                        if(x==1.0D/0.0D){
                                erfc = 0.0D;
                        }
                        else{
                                if(x>=0){
                                        erfc = 1.0D - Stat.incompleteGamma(0.5, x*x);
                                }
                                else{
                                        erfc = 1.0D + Stat.incompleteGamma(0.5, x*x);
                                }
                        }
                }
                return erfc;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mean  =  the mean, sd = standard deviation
        public static double normalProb(double mean, double sd, double upperlimit){
                double arg = (upperlimit - mean)/(sd*Math.sqrt(2.0));
                return (1.0D + Stat.erf(arg))/2.0D;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        // mean  =  the mean, sd = standard deviation
        public static double normalProb(double mean, double sd, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mean)/(sd*Math.sqrt(2.0));
                double arg2 = (upperlimit - mean)/(sd*Math.sqrt(2.0));

                return (Stat.erf(arg2)-Stat.erf(arg1))/2.0D;
        }

        // Gaussian (normal) probability
        // mean  =  the mean, sd = standard deviation
        public static double normal(double mean, double sd, double x){
                return Math.exp(-Fmath.square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }
        
        
        // Log normal distribution
        public static double lognormal(double mean, double sd, double x){
        	return Math.exp(-Fmath.square((Math.log(x)- mean)/sd)/2.0)/(x*sd*Math.sqrt(2.0D*Math.PI));
        }

        // Returns an array of Gaussian (normal) random deviates - clock seed
        // mean  =  the mean, sd = standard deviation, length of array
        public static double[] normalRand(double mean, double sd, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                    ran[i]=rr.nextGaussian();
                    ran[i] = ran[i]*sd+mean;
                }
                return ran;
        }

        // Returns an array of Gaussian (normal) random deviates - user provided seed
        // mean  =  the mean, sd = standard deviation, length of array
        public static double[] normalRand(double mean, double sd, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                    ran[i]=rr.nextGaussian();
                    ran[i] = ran[i]*sd+mean;
                }
                return ran;
        }


        // Lorentzian cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double lorentzianProb(double mu, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/(gamma/2.0D);
                return (1.0D/Math.PI)*(Math.atan(arg)+Math.PI/2.0);
        }

        // Lorentzian cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double lorentzianProb(double mu, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (upperlimit - mu)/(gamma/2.0D);
                double arg2 = (lowerlimit - mu)/(gamma/2.0D);
                return (1.0D/Math.PI)*(Math.atan(arg1)-Math.atan(arg2));
        }

        // Lorentzian probability
        public static double lorentzian(double mu, double gamma, double x){
                double arg =gamma/2.0D;
                return (1.0D/Math.PI)*arg/(Fmath.square(mu-x)+arg*arg);
        }


        // Returns an array of Lorentzian random deviates - clock seed
        // mu  =  the mean, gamma = half-height width, length of array
        public static double[] lorentzianRand(double mu, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                    ran[i]=Math.tan((rr.nextDouble()-0.5)*Math.PI);
                    ran[i] = ran[i]*gamma/2.0D+mu;
                }
                return ran;
        }

        // Returns an array of Lorentzian random deviates - user provided seed
        // mu  =  the mean, gamma = half-height width, length of array
        public static double[] lorentzianRand(double mu, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                    ran[i]=Math.tan((rr.nextDouble()-0.5)*Math.PI);
                    ran[i] = ran[i]*gamma/2.0D+mu;
                }
                return ran;
        }

        // Cumulative Poisson Probability Function
        // probability that a number of Poisson random events will occur between 0 and k (inclusive)
        // k is an integer greater than equal to 1
        // mean  = mean of the Poisson distribution
        public static double poissonProb(int k, double mean){
                if(k<1)throw new IllegalArgumentException("k must be an integer greater than or equal to 1");
                return Stat.incompleteGammaComplementary((double) k, mean);
        }

        // Poisson Probability Function
        // k is an integer greater than or equal to zero
        // mean  = mean of the Poisson distribution
        public static double poisson(int k, double mean){
                if(k<0)throw new IllegalArgumentException("k must be an integer greater than or equal to 0");
                return Math.pow(mean, k)*Math.exp(-mean)/Stat.factorial((double)k);
        }

        // Returns an array of Poisson random deviates - clock seed
        // mean  =  the mean,  n = length of array
        // follows the ideas of Numerical Recipes
        public static double[] poissonRand(double mean, int n){

                Random rr = new Random();
                double[] ran = poissonRandCalc(rr, mean, n);
                return ran;
        }

        // Returns an array of Poisson random deviates - user provided seed
        // mean  =  the mean,  n = length of array
        // follows the ideas of Numerical Recipes
        public static double[] poissonRand(double mean, int n, long seed){

                Random rr = new Random(seed);
                double[] ran = poissonRandCalc(rr, mean, n);
                return ran;
        }

        // Calculates and returns an array of Poisson random deviates
        private static double[] poissonRandCalc(Random rr, double mean, int n){
                double[] ran = new double[n];
                double oldm = -1.0D;
                double expt = 0.0D;
                double em = 0.0D;
                double term = 0.0D;
                double sq = 0.0D;
                double lnMean = 0.0D;
                double yDev = 0.0D;

                if(mean < 12.0D){
                    for(int i=0; i<n; i++){
                        if(mean != oldm){
                            oldm = mean;
                            expt = Math.exp(-mean);
                        }
                        em = -1.0D;
                        term = 1.0D;
                        do{
                            ++em;
                            term *= rr.nextDouble();
                        }while(term>expt);
                        ran[i] = em;
                    }
                }
                else{
                    for(int i=0; i<n; i++){
                        if(mean != oldm){
                            oldm = mean;
                            sq = Math.sqrt(2.0D*mean);
                            lnMean = Math.log(mean);
                            expt = lnMean - Stat.logGamma(mean+1.0D);
                        }
                        do{
                            do{
                                yDev = Math.tan(Math.PI*rr.nextDouble());
                                em = sq*yDev+mean;
                            }while(em<0.0D);
                            em = Math.floor(em);
                            term = 0.9D*(1.0D+yDev*yDev)*Math.exp(em*lnMean - Stat.logGamma(em+1.0D)-expt);
                        }while(rr.nextDouble()>term);
                        ran[i] = em;
                    }
                }
                return ran;
        }


        // Chi-Square Probability Function
        // probability that an observed chi-square value for a correct model should be less than chiSquare
        // nu  =  the degrees of freedom
        public static double chiSquareProb(double chiSquare, int nu){
                return Stat.incompleteGamma((double)nu/2.0D, chiSquare/2.0D);
        }

        // Chi-Square Statistic for Poisson distribution
        public static double chiSquare(double[] observed, double[] expected, double[] variance){
            int nObs = observed.length;
            int nExp = expected.length;
            int nVar = variance.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            if(nObs!=nVar)throw new IllegalArgumentException("observed array length does not equal the variance array length");
            double chi = 0.0D;
            for(int i=0; i<nObs; i++){
                chi += Fmath.square(observed[i]-expected[i])/variance[i];
            }
            return chi;
        }

        // Chi-Square Statistic for Poisson distribution for frequency data
        // and Poisson distribution for each bin
        // double arguments
        public static double chiSquareFreq(double[] observedFreq, double[] expectedFreq){
            int nObs = observedFreq.length;
            int nExp = expectedFreq.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            double chi = 0.0D;
            for(int i=0; i<nObs; i++){
                chi += Fmath.square(observedFreq[i]-expectedFreq[i])/expectedFreq[i];
            }
            return chi;
        }

        // Chi-Square Statistic for Poisson distribution for frequency data
        // and Poisson distribution for each bin
        // int arguments
        public static double chiSquareFreq(int[] observedFreq, int[] expectedFreq){
            int nObs = observedFreq.length;
            int nExp = expectedFreq.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            double[] observ = new double[nObs];
            double[] expect = new double[nObs];
            for(int i=0; i<nObs; i++){
                observ[i] = (int)observedFreq[i];
                expect[i] = (int)expectedFreq[i];
            }

            return chiSquareFreq(observ, expect);
        }

        // Returns the binomial cumulative probabilty
        public static double binomialProb(double p, int n, int k){
                if(p<0.0D || p>1.0D)throw new IllegalArgumentException("\np must lie between 0 and 1");
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Stat.incompleteBeta(k, n-k+1, p);
        }

        // Returns a binomial Coefficient as a double
        public static double binomialCoeff(int n, int k){
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Math.floor(0.5D + Math.exp(Stat.logFactorial(n) - Stat.logFactorial(k) - Stat.logFactorial(n-k)));
        }

        // Returns the F-distribution probabilty for degrees of freedom df1, df2
        // F ratio provided
        public static double fTestProb(double fValue, int df1, int df2){
            double ddf1 = (double)df1;
            double ddf2 = (double)df2;
            double x = ddf2/(ddf2+ddf1*fValue);
            return Stat.incompleteBeta(df2/2.0D, df1/2.0D, x);
        }

        // Returns the F-distribution probabilty for degrees of freedom df1, df2
        // numerator and denominator variances provided
        public static double fTestProb(double var1, int df1, double var2, int df2){
            double fValue = var1/var2;
            double ddf1 = (double)df1;
            double ddf2 = (double)df2;
            double x = ddf2/(ddf2+ddf1*fValue);
            return Stat.incompleteBeta(df2/2.0D, df1/2.0D, x);
        }


        // Returns the Student t distribution probabilty
        public static double dtudentTProb(double tValue, int df){
            double ddf = (double)df;
            double x = ddf/(ddf+tValue*tValue);
            return 1.0D - Stat.incompleteBeta(df/2.0D, 0.5D, x);
        }
        
        public static double studentTProb(int freedom, double confidence ){
            boolean fin=false;
            int cont=0;
            while (!fin){
            double val=cont*0.01;
            if (Stat.dtudentTProb(val,freedom)>confidence)
                fin=true;
            cont++;
            }
            return (cont-2)*0.01;
        }
        
        public static double[] intervalConfidence(double[] vals,double confidence){
            if (vals.length==0){
                double[] sals=new double[2];
                sals[0]=0;
                sals[1]=0;
            }
            if (vals.length<2){
                double[] sals=new double[2];
                try{
                sals[0]=vals[0];
                sals[1]=vals[0];
                }catch(Exception e){
                    sals[0]=0.0;
                    sals[1]=0.0;
                    return sals;
                }
                return sals;
            }
            int n=vals.length;
            double mean=Stat.mean(vals);
            double variance=Stat.variance(vals);
            double varianceS=variance*Math.sqrt(n/(n-1));
            double t=Stat.studentTProb(n-1, confidence);
            double[] interval=new double[2];
            
            interval[0]=mean-t*varianceS/Math.sqrt(n);
            interval[1]=mean+t*varianceS/Math.sqrt(n);
            
            return interval;
        }

        public static double[] intervalConfidence(int n, double mean, double variance, double confidence){
            double varianceS=variance*Math.sqrt(n/(double)(n-1));
            double t=Stat.studentTProb(n-1, confidence);
            double[] interval=new double[2];
            
            interval[0]=mean-t*varianceS/Math.sqrt(n);
            interval[1]=mean+t*varianceS/Math.sqrt(n);
            
            return interval;
        }
        
        /**
         * Statistics t-test for not equal variance. Return p-value for statistical significant differences 
         * between the means of the two populations. if p-value>0.05, they are not statistically difference.
         * http://www.fisterra.com/mbe/investiga/t_student/t_student.htm
         */
        public static double TStatisticisTestNotEqualVariance(double[] vals1, double[] vals2){
                
                double[] samp1=vals1;
                double[] samp2=vals2;
                int sample1=vals1.length;
                int sample2=vals2.length;
            
                double mean1=Stat.mean(Stat.adjust(samp1,sample1));
                double mean2=Stat.mean(Stat.adjust(samp2,sample2));
                
                double cuasivar1=Stat.variance(Stat.adjust(samp1,sample1));
                double cuasivar2=Stat.variance(Stat.adjust(samp2,sample2));
		
                double val1=cuasivar1/sample1 + cuasivar2/sample2;
                
                double tvalue=(mean1 -mean2)/Math.sqrt(val1);
                
                double val2=Math.pow(cuasivar1/sample1,2)/(sample1+1) + Math.pow(cuasivar2/sample2,2)/(sample2+1);
                
                long freedom=Math.round(Math.pow(val1,2)/val2-2);
                
                
                double prob=Stat.dtudentTProb(tvalue,(int)freedom);
                if (prob < 0.000001) {
                    prob = 0.0;
                }  
                else if (prob > 0.50) {
                    prob = 1.0 - prob;
                }
                prob = (Math.round(prob * 10000.0)) / 10000.0;
                return prob;
               
        }
        /**
         * Statistics t-test for equal variance. Return p-value for statistical significant 
         * differences between the means of the two populations. if p-value>0.05, they are not
         * statistically difference.
         */
        public static double TStatisticisTestEqualVariance(double[] vals1, double[] vals2){
            t_statistics test=new t_statistics(vals1,vals1.length,vals2,vals2.length);
            double t=test.calc_t();


            double prob=Stat.dtudentTProb(t,vals1.length+vals2.length-2);
            if (prob < 0.000001) {
                prob = 0.0;
            }  
            else if (prob > 0.50) {
                prob = 1.0 - prob;
            }
            prob = (Math.round(prob * 10000.0)) / 10000.0;
            return prob;
            
        }
        
            
            // Distribute data into bins to obtain histogram
        // zero bin position and upper limit provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero, double binUpper){
            int n = 0;              // new array length
            int m = data.length;    // old array length;
            for(int i=0; i<m; i++)if(data[i]<=binUpper)n++;
            if(n!=m){
                double[] newData = new double[n];
                int j = 0;
                for(int i=0; i<m; i++){
                    if(data[i]<=binUpper){
                        newData[j] = data[i];
                        j++;
                    }
                }
                System.out.println((m-n)+" data points, above histogram upper limit, excluded in Stat.histogramBins");
                return histogramBins(newData, binWidth, binZero);
            }
            else{
                 return histogramBins(data, binWidth, binZero);

            }
        }

        // Distribute data into bins to obtain histogram
        // zero bin position provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero){
            double dmax = Fmath.maximum(data);
            int nBins = (int) Math.ceil((dmax - binZero)/binWidth);
            if(binZero+nBins*binWidth==dmax)nBins++;
            int nPoints = data.length;
            int[] dataCheck = new int[nPoints];
            for(int i=0; i<nPoints; i++)dataCheck[i]=0;
            double[]binWall = new double[nBins+1];
            binWall[0]=binZero;
            for(int i=1; i<=nBins; i++){
                binWall[i] = binWall[i-1] + binWidth;
            }
            double[][] binFreq = new double[2][nBins];
            for(int i=0; i<nBins; i++){
                binFreq[0][i]= (binWall[i]+binWall[i+1])/2.0D;
                binFreq[1][i]= 0.0D;
            }
            boolean test = true;

            for(int i=0; i<nPoints; i++){
                test=true;
                int j=0;
                while(test){
                    if(data[i]>=binWall[j] && data[i]<binWall[j+1]){
                        binFreq[1][j]+= 1.0D;
                        dataCheck[i]=1;
                        test=false;
                    }
                    else{
                        if(j==nBins-1){
                            test=false;
                        }
                        else{
                            j++;
                        }
                    }
                }
            }
            int nMissed=0;
            for(int i=0; i<nPoints; i++)if(dataCheck[i]==0)nMissed++;
            if(nMissed>0)System.out.println(nMissed+" data points, below histogram lower limit, excluded in Stat.histogramBins");
            return binFreq;
        }

        // Distribute data into bins to obtain histogram
        // zero bin position calculated
        public static double[][] histogramBins(double[] data, double binWidth){
            double dmin = Fmath.minimum(data);
            double dmax = Fmath.maximum(data);
            double span = dmax - dmin;
            int nBins = (int) Math.ceil(span/binWidth);
            double rem = ((double)nBins)*binWidth-span;
            double binZero =dmin-rem/2.0D;
            return Stat.histogramBins(data, binWidth, binZero);
        }

        // Distribute data into bins to obtain histogram and plot histogram
        // zero bin position and upper limit provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, double binUpper){
            int n = 0;              // new array length
            int m = data.length;    // old array length;
            for(int i=0; i<m; i++)if(data[i]<=binUpper)n++;
            if(n!=m){
                double[] newData = new double[n];
                int j = 0;
                for(int i=0; i<m; i++){
                    if(data[i]<=binUpper){
                        newData[j] = data[i];
                        j++;
                    }
                }
                System.out.println((m-n)+" data points, above histogram upper limit, excluded in Stat.histogramBins");
                return histogramBinsPlot(newData, binWidth, binZero);
            }
            else{
                 return histogramBinsPlot(data, binWidth, binZero);

            }
        }


        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero){
            double[][] results = histogramBins(data, binWidth, binZero);
            int nBins = results[0].length;
            int nPoints = nBins*3+1;
            double[][] cdata = PlotGraph.data(1, nPoints);
            cdata[0][0]=binZero;
            cdata[1][0]=0.0D;
            int k=1;
            for(int i=0; i<nBins; i++){
                cdata[0][k]=cdata[0][k-1];
                cdata[1][k]=results[1][i];
                k++;
                cdata[0][k]=cdata[0][k-1]+binWidth;
                cdata[1][k]=results[1][i];
                k++;
                cdata[0][k]=cdata[0][k-1];
                cdata[1][k]=0.0D;
                k++;
            }

            PlotGraph pg = new PlotGraph(cdata);
            pg.setGraphTitle("Histogram:  Bin Width = "+binWidth);
            pg.setLine(3);
            pg.setPoint(0);
            pg.plot();

            return results;
        }


        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position calculated
        public static double[][] histogramBinsPlot(double[] data, double binWidth){
            double dmin = Fmath.minimum(data);
            double dmax = Fmath.maximum(data);
            double span = dmax - dmin;
            int nBins = (int) Math.ceil(span/binWidth);
            double rem = ((double)nBins)*binWidth-span;
            double binZero =dmin-rem/2.0D;
            return Stat.histogramBinsPlot(data, binWidth, binZero);
        }


        // factorial of n
        // argument and return are integer, therefore limited to 0<=n<=12
        // see below for long and double arguments
        public static int factorial(int n){
                if(n<0)throw new IllegalArgumentException("n must be a positive integer");
                if(n>12)throw new IllegalArgumentException("n must less than 13 to avoid integer overflow\nTry long or double argument");
                int f = 1;
                for(int i=1; i<=n; i++)f*=i;
                return f;
        }

        // factorial of n
        // argument and return are long, therefore limited to 0<=n<=20
        // see below for double argument
        public static long factorial(long n){
                if(n<0)throw new IllegalArgumentException("n must be a positive integer");
                if(n>20)throw new IllegalArgumentException("n must less than 21 to avoid long integer overflow\nTry double argument");
                long f = 1;
                for(int i=1; i<=n; i++)f*=i;
                return f;
        }

        // factorial of n
        // Argument is of type double but must be, numerically, an integer
        // factorial returned as double but is, numerically, should be an integer
        // numerical rounding may makes this an approximation after n = 21
        public static double factorial(double n){
                if(n<0 || (n-(int)n)!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
                double f = 1.0D;
                int nn = (int)n;
                for(int i=1; i<=nn; i++)f*=i;
                return f;
        }

        // log to base e of the factorial of n
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(int n){
                if(n<0 || (n-(int)n)!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
                double f = 0.0D;
                for(int i=2; i<=n; i++)f+=Math.log(i);
                return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(double n){
        if(n<0 || (n-(int)n)!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
                double f = 0.0D;
                int nn = (int)n;
                for(int i=2; i<=nn; i++)f+=Math.log(i);
                return f;
        }

        // Calculate correlation coefficient
        public static double corrCoeff(double[] xx, double[]yy, int nData){

            double temp0 = 0.0D, temp1 = 0.0D;  // working variables
            if(yy.length!=xx.length)throw new IllegalArgumentException("array lengths must be equal");
            int df = nData-1;
            // weighted means
            double mx = 0.0D;
            double my = 0.0D;
            for(int i=0; i<nData; i++){
                mx += xx[i];
                my += yy[i];
            }
            mx /= df;
            my /= df;

            // calculate sample variances
            double s2xx = 0.0D;
            double s2yy = 0.0D;
            double s2xy = 0.0D;
            for(int i=0; i<nData; i++){
                s2xx += Fmath.square(xx[i]-mx);
                s2yy += Fmath.square(yy[i]-my);
                s2xy += (xx[i]-mx)*(yy[i]-my);
            }
            s2xx /= df;
            s2yy /= df;
            s2xy /= df;

            // calculate corelation coefficient
            double sampleR = s2xy/Math.sqrt(s2xx*s2yy);

            return sampleR;
        }

        // Calculate correlation coefficient
        public static double corrCoeff(double[] xx, double[]yy){

            double temp0 = 0.0D, temp1 = 0.0D;  // working variables
            int nData = xx.length;
            if(yy.length!=nData)throw new IllegalArgumentException("array lengths must be equal");
            int df = nData-1;
            // weighted means
            double mx = 0.0D;
            double my = 0.0D;
            for(int i=0; i<nData; i++){
                mx += xx[i];
                my += yy[i];
            }
            mx /= df;
            my /= df;

            // calculate sample variances
            double s2xx = 0.0D;
            double s2yy = 0.0D;
            double s2xy = 0.0D;
            for(int i=0; i<nData; i++){
                s2xx += Fmath.square(xx[i]-mx);
                s2yy += Fmath.square(yy[i]-my);
                s2xy += (xx[i]-mx)*(yy[i]-my);
            }
            s2xx /= df;
            s2yy /= df;
            s2xy /= df;

            // calculate corelation coefficient
            double sampleR = s2xy/Math.sqrt(s2xx*s2yy);

            return sampleR;
        }


        // Linear correlation coefficient cumulative probablity
        public static double linearCorrCoeffProb(double rCoeff, int nu){
            if(Math.abs(rCoeff)>1.0D)throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

            // Create instances of the classes holding the function evaluation methods
            CorrCoeff cc = new CorrCoeff();

            // Assign values to constant in the function
            cc.a = ((double)nu - 2.0D)/2.0D;


            double integral = Integration.gaussQuad(cc, Math.abs(rCoeff), 1.0D, 128);

            double preterm = Math.exp(Stat.logGamma((nu+1.0D)/2.0)-Stat.logGamma(nu/2.0D))/Math.sqrt(Math.PI);

            return preterm*integral;
        }

        // Linear correlation coefficient single probablity
        public static double linearCorrCoeff(double rCoeff, int nu){
            if(Math.abs(rCoeff)>1.0D)throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

            double a = ((double)nu - 2.0D)/2.0D;
            double y = Math.pow((1.0D - Fmath.square(rCoeff)),a);

            double preterm = Math.exp(Stat.logGamma((nu+1.0D)/2.0)-Stat.logGamma(nu/2.0D))/Math.sqrt(Math.PI);

            return preterm*y;
        }

        // Minimum Gumbel cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double gumbelMinProb(double mu, double sigma, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg = -(upperlimit - mu)/sigma;
                return Math.exp(-Math.exp(arg));
        }

        // Maximum Gumbel cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double gumbelMaxProb(double mu, double sigma, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg = -(upperlimit - mu)/sigma;
                return 1.0D-Math.exp(-Math.exp(arg));
        }


        // Minimum Gumbel cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double gumbelMinProb(double mu, double sigma, double lowerlimit, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg1 = -(lowerlimit - mu)/sigma;
                double arg2 = -(upperlimit - mu)/sigma;
                double term1 = Math.exp(-Math.exp(arg1));
                double term2 = Math.exp(-Math.exp(arg2));
                return term2-term1;
        }

        // Maximum Gumbel cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double gumbelMaxProb(double mu, double sigma, double lowerlimit, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = -Math.exp(-Math.exp(arg1));
                double term2 = -Math.exp(-Math.exp(arg2));
                return term2-term1;
        }

        // Minimum Gumbel probability
        public static double gumbelMin(double mu,double sigma, double x){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg =(x-mu)/sigma;
                return (1.0D/sigma)*Math.exp(arg)*Math.exp(-Math.exp(arg));
        }

        // Maximum Gumbel probability
        public static double gumbelMax(double mu,double sigma, double x){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg =-(x-mu)/sigma;
                return (1.0D/sigma)*Math.exp(arg)*Math.exp(-Math.exp(arg));
        }

        // Returns an array of minimal Gumbel (Type I EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMinRand(double mu, double sigma,  int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
                }
                return ran;
        }

        // Returns an array of minimal Gumbel (Type I EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMinRand(double mu, double sigma,  int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
                }
                return ran;
        }

        // Returns an array of maximal Gumbel (Type I EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMaxRand(double mu, double sigma,  int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = mu-Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma;
                }
                return ran;
        }

       // Returns an array of maximal Gumbel (Type I EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMaxRand(double mu, double sigma,  int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = mu-Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma;
                }
                return ran;
        }

        // Minimum Gumbel mean
        public static double gumbelMinMean(double mu,double sigma){
                return mu - sigma*Fmath.EULER_CONSTANT_GAMMA;
        }

        // Maximum Gumbel mean
        public static double gumbelMaxMean(double mu,double sigma){
                return mu + sigma*Fmath.EULER_CONSTANT_GAMMA;
        }

        // Minimum Gumbel standard deviation
        public static double gumbelMinStandDev(double sigma){
                return sigma*Math.PI/Math.sqrt(6.0D);
        }

        // Maximum Gumbel standard deviation
        public static double gumbelMaxStandDev(double sigma){
                return sigma*Math.PI/Math.sqrt(6.0D);
        }

        // Minimum Gumbel mode
        public static double gumbelMinMode(double mu,double sigma){
            return mu;
        }

        // Maximum Gumbel mode
        public static double gumbelMaxMode(double mu,double sigma){
            return mu;
        }

        // Minimum Gumbel median
        public static double gumbelMinMedian(double mu,double sigma){
            return mu + sigma*Math.log(Math.log(2.0D));
        }

        // Maximum Gumbel median
        public static double gumbelMaxMedian(double mu,double sigma){
            return mu - sigma*Math.log(Math.log(2.0D));
        }


        // Weibull cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double weibullProb(double mu, double sigma, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-Math.pow(arg, gamma));
                return y;
        }


        // Weibull cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double weibullProb(double mu, double sigma, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-Math.pow(arg1, gamma));
                if(arg2>=0.0D)term2 = -Math.exp(-Math.pow(arg2, gamma));
                return term2-term1;
        }

        // Weibull probability
        public static double weibull(double mu,double sigma, double gamma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (gamma/sigma)*Math.pow(arg, gamma-1.0D)*Math.exp(-Math.pow(arg, gamma));
                }
                return y;
        }

        // Weibull mean
        public static double weibullMean(double mu,double sigma, double gamma){
                return mu + sigma*Stat.gamma(1.0D/gamma+1.0D);
        }

        // Weibull standard deviation
        public static double weibullStandDev(double sigma, double gamma){
                double y = Stat.gamma(2.0D/gamma+1.0D)-Fmath.square(Stat.gamma(1.0D/gamma+1.0D));
                return sigma*Math.sqrt(y);
        }

        // Weibull mode
        public static double weibullMode(double mu,double sigma, double gamma){
            double y=mu;
            if(gamma>1.0D){
                y = mu + sigma*Math.pow((gamma-1.0D)/gamma, 1.0D/gamma);
            }
            return y;
        }

        // Weibull median
        public static double weibullMedian(double mu,double sigma, double gamma){
            return mu + sigma*Math.pow(Math.log(2.0D),1.0D/gamma);
        }

        // Returns an array of Weibull (Type III EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] weibullRand(double mu, double sigma, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(-Math.log(1.0D-rr.nextDouble()),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Returns an array of Weibull (Type III EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] weibullRand(double mu, double sigma, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(-Math.log(1.0D-rr.nextDouble()),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Frechet cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double frechetProb(double mu, double sigma, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = Math.exp(-Math.pow(arg, -gamma));
                return y;
        }


        // Frechet cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double frechetProb(double mu, double sigma, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = Math.exp(-Math.pow(arg1, -gamma));
                if(arg2>=0.0D)term2 = Math.exp(-Math.pow(arg2, -gamma));
                return term2-term1;
        }

        // Frechet probability
        public static double frechet(double mu,double sigma, double gamma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (gamma/sigma)*Math.pow(arg, -gamma-1.0D)*Math.exp(-Math.pow(arg, -gamma));
                }
                return y;
        }

        // Frechet mean
        public static double frechetMean(double mu,double sigma, double gamma){
                double y = Double.NaN;
                if(gamma>1.0D){
                    y = mu + sigma*Stat.gamma(1.0D-1.0D/gamma);
                }
                return y;
        }

        // Frechet standard deviation
        public static double frechetStandDev(double sigma, double gamma){
                double y = Double.NaN;
                if(gamma>2.0D){
                    y = Stat.gamma(1.0D-2.0D/gamma)-Fmath.square(Stat.gamma(1.0D-1.0D/gamma));
                    y = sigma*Math.sqrt(y);
                }
                return y;
        }

        // Frechet mode
        public static double frechetMode(double mu,double sigma, double gamma){
                return mu + sigma*Math.pow(gamma/(1.0D+gamma), 1.0D/gamma);
         }

        // Returns an array of Frechet (Type II EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] frechetRand(double mu, double sigma, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow((1.0D/(Math.log(1.0D/rr.nextDouble()))),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Returns an array of Frechet (Type II EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] frechetRand(double mu, double sigma, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow((1.0D/(Math.log(1.0D/rr.nextDouble()))),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Exponential cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double exponentialProb(double mu, double sigma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-arg);
                return y;
        }

        // Exponential cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double exponentialProb(double mu, double sigma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-arg1);
                if(arg2>=0.0D)term2 = -Math.exp(-arg2);
                return term2-term1;
        }

        // Exponential probability
        public static double exponential(double mu,double sigma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = Math.exp(-arg)/sigma;
                }
                return y;
        }

        // Exponential mean
        public static double exponentialMean(double mu, double sigma){
                return mu + sigma;
        }

        // Exponential standard deviation
        public static double exponentialStandDev(double sigma){
                return sigma;
        }

        // Exponential mode
        public static double exponentialMode(double mu){
            return mu;
        }

        // Exponential median
        public static double exponentialMedian(double mu,double sigma){
            return mu + sigma*Math.log(2.0D);
        }

        // Returns an array of Exponential random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] exponentialRand(double mu, double sigma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = mu - Math.log(1.0D-rr.nextDouble())*sigma;
                }
                return ran;
        }

        // Returns an array of Exponential random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] exponentialRand(double mu, double sigma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = mu - Math.log(1.0D-rr.nextDouble())*sigma;
                }
                return ran;
        }


        // Rayleigh cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double rayleighProb(double sigma, double upperlimit){
                double arg = (upperlimit)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-arg*arg/2.0D);
                return y;
        }

        // Rayleigh cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double rayleighProb(double sigma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit)/sigma;
                double arg2 = (upperlimit)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-arg1*arg1/2.0D);
                if(arg2>=0.0D)term2 = -Math.exp(-arg2*arg2/2.0D);
                return term2-term1;
        }

        // Rayleigh probability
        public static double rayleigh(double sigma, double x){
                double arg =x/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (arg/sigma)*Math.exp(-arg*arg/2.0D)/sigma;
                }
                return y;
        }

        // Rayleigh mean
        public static double rayleighMean(double sigma){
                return sigma*Math.sqrt(Math.PI/2.0D);
        }

        // Rayleigh standard deviation
        public static double rayleighStandDev(double sigma){
                return sigma*Math.sqrt(2.0D-Math.PI/2.0D);
        }

        // Rayleigh mode
        public static double rayleighMode(double sigma){
            return sigma;
        }

        // Rayleigh median
        public static double rayleighMedian(double sigma){
            return sigma*Math.sqrt(Math.log(2.0D));
        }

        // Returns an array of Rayleigh random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] rayleighRand(double sigma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.sqrt(-2.0D*Math.log(1.0D-rr.nextDouble()))*sigma;
                }
                return ran;
        }

        // Returns an array of Rayleigh random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] rayleighRand(double sigma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.sqrt(-2.0D*Math.log(1.0D-rr.nextDouble()))*sigma;
                }
                return ran;
        }

        // Pareto cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double paretoProb(double alpha, double beta, double upperlimit){
                double y = 0.0D;
                if(upperlimit>=beta)y = 1.0D - Math.pow(beta/upperlimit, alpha);
                return y;
        }

        // Pareto cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double paretoProb(double alpha, double beta, double lowerlimit, double upperlimit){
                double term1 = 0.0D, term2 = 0.0D;
                if(lowerlimit>=beta)term1 = -Math.pow(beta/lowerlimit, alpha);
                if(upperlimit>=beta)term2 = -Math.pow(beta/upperlimit, alpha);
                return term2-term1;
        }

        // Pareto probability
        public static double pareto(double alpha, double beta, double x){
                double y = 0.0D;
                if(x>=beta){
                    y = alpha*Math.pow(beta, alpha)/Math.pow(x, alpha+1.0D);
                }
                return y;
        }

        // Pareto mean
        public static double paretoMean(double alpha, double beta){
                double y = Double.NaN;
                if(alpha>1.0D)y = alpha*beta/(alpha-1);
                return y;
        }

        // Pareto standard deviation
        public static double paretoStandDev(double alpha, double beta){
                double y = Double.NaN;
                if(alpha>1.0D)y = alpha*Fmath.square(beta)/(Fmath.square(alpha-1)*(alpha-2));
                return y;
        }

        // Pareto mode
        public static double paretoMode(double beta){
            return beta;
        }

        // Returns an array of Pareto random deviates - clock seed
        public static double[] paretoRand(double alpha, double beta, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(1.0D-rr.nextDouble(), -1.0D/alpha)*beta;
                }
                return ran;
        }

        // Returns an array of Pareto random deviates - user supplied seed
        public static double[] paretoRand(double alpha, double beta, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(1.0D-rr.nextDouble(), -1.0D/alpha)*beta;
                }
                return ran;
        }
        
/*
        // Returns an array of random bits (0's and 1's) - clock seed
        // see RandomBits class for details - method 1 used
        public static int[] bitsRand(int n){
                RandomBits rb = new RandomBits();
                int[] ran = rb.bitArray(n);
                return ran;
        }

        // Returns an array of random bits (0's and 1's) - user supplied seed
        // see RandomBits class for details - method 1 used
        public static int[] bitsRand(int n, long seed){
                RandomBits rb = new RandomBits(seed);
                int[] ran = rb.bitArray(n);
                return ran;
        }

*/
        /**
         *  Return a normalized data serie with mean 0 and standard deviation 1.
         */
        public static double[] normalization(double[] d){
            double mean=Stat.mean(d);
            double desv=Stat.standardDeviation(d);
            
            double[] sal=new double[d.length];
            for (int i=0; i<d.length; i++){
                sal[i]=(d[i]-mean)/desv;
            }
            
            return sal;
        }
        
        /**
         *  Return a normalized data serie with mean 0 and standard deviation 1.
         */
        public static double[] normalization(double[] d, double mean, double desv){

            double[] sal=new double[d.length];
            for (int i=0; i<d.length; i++){
                sal[i]=(d[i]-mean)/desv;
            }
            
            return sal;
        }

        /**
         *  Return a normalized data serie with mean 0 and standard deviation 1.
         */
        public static double normalization(double d, double mean, double desv){
            return (d-mean)/desv;
        }

        /**
         *  Make the follow transformation sal[i]=d[i]+Min{d[k]:1<k<n} if Min{d[k]}<0
         */
        public static double[] makePositive(double[] d){
            
            double min=Double.MAX_VALUE;
            for (int i=0; i<d.length; i++)
                if (d[i]<min)
                    min=d[i];
            if (min<=0){
                double[] result=new double[d.length];
                for (int i=0; i<d.length; i++)
                    result[i]=d[i]+Math.abs(min)+1;
                return result;
            }else
                return d;
          
        }            

        /**
         *  This method returns a new array with a size equal to parameter 'tam'
         *  and with the first 'tam' values of the other parameter 'data'.
         */
        public static double [] adjust(double [] data, int tam){
            double[] tmp= new double[tam];
            for (int i=0; i<tam; i++)
                tmp[i]=data[i];
            return tmp;
        }
        
        public static void main(String[] args) throws Exception{
            double[] vals=new double[2];
            vals[0]=1;
            vals[1]=2;
            vals=Stat.intervalConfidence(vals,0.95);
        
        }
}














