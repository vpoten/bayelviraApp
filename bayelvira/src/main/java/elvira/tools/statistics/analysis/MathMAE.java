/** File: MathMAE.java */

package elvira.tools.statistics.analysis;

import elvira.tools.statistics.analysis.Stat;
import elvira.tools.statistics.miscellany.Sort;
import java.lang.Math;
import java.util.Vector;

/**
 * This class contains math functions used for computing statistics in MAExplorer. 
 * Some of these methods were derived from WebGel, GELLAB-II, and 
 * Numerical Recipes in C.
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The MAExplorer 
 * Mozilla and Legal files are available on http://maexplorer.sourceforge.net/.
 *<P>
 * @author P. Lemkin (NCI), G. Thornwall (SAIC), NCI-Frederick, Frederick, MD
 * @version $Date: 2005/05/17 16:11:05 $   $Revision: 1.2 $
 * @see <A HREF="http://maexplorer.sourceforge.net/">MAExplorer Home</A>
 */
 
 public class MathMAE
 {
     
   public final static double P_THRESHOLD = 0.05;     
   
   final static double
     gammln_cof[] = {
                      76.18009173,
                      -86.50532033,
                      24.01409822,
                      -1.231739516,
                      0.120858003e-2,
                      -0.536382e-5
                    };

   /** log(10 base e */
   final public static double
     ln10= 2.302585092994046;     
   /** log(e) base 10 */
   final public static double
     log10e= 0.43429448190325176;
   
   /** log(2) base e */
   final public static double
     ln2= 0.6931471805599453;     
   /** log(e) base 2 */
   final public static double
     log2e= 1.4426950408889634;          
       
   /** RTN: if nr_betacf() was ok */
   public static boolean
    okBetaCF;                  
   /** RTN: if nr_betai() was ok */  
   public static boolean
    okBetaI;                 
   /** RTN: if nr_probks() was ok */
    public static boolean
    okProbKS;               
    
   /** RTN: K-S statistic */
   public static double
     dKS;	              
   /** RTN: probability of null hypoth same distrib*/
   public static double
     probKS; 
   /** RTN: if calcXYstat */  
   public static int
     nXY;                    
   /** RTN: correlation coefficient */
   public static double
     rSq;                                        
   /** RTN: mean X */ 
   public static double
     mnX;                                       
   /** RTN: mean Y */
   public static double
     mnY;                                        
   /** RTN: standard deviation X */ 
   public static double
     sdX;                                       
   /** RTN: standard deviation Y */
   public static double
     sdY;
     	             
   
   /**
    * nr_gammln() - Return ln(gamma()) of x by polynomial evaluation.    
    * This method was derived from GELLAB-II which was derived from 
    * Numerical Recipes in C, Pg 168, Section 6 N.R.C.
    * @param xx arg
    * @return value
    */
   public static double nr_gammln(double xx)
   { /* nr_gammln */
     double
     x,
     tmp,
     ser;
     int
     j;
     
     x= xx-1.0;
     tmp= x+5.5;
     tmp -= (x+0.5)*Math.log(tmp);
     ser=1.0;
     for (j=0;j<=5;j++)
     {
       x += 1.0;
       ser += gammln_cof[j]/x;
     }
     
     return(-tmp+Math.log(2.50662827465*ser));
   } /* nr_gammln */
   
   
   /**
    * nr_betacf() - evaluation fct for the incomplete Beta function 'x(a,b).    
    * This method was derived from GELLAB-II which was derived from 
    * Numerical Recipes in C, Pg 180, Section 6.3 N.R.C.
    * Set the RTN: okBetaCF flag to false if there is a problem
    * @param a arg
    * @param b arg
    * @param x arg
    * @return value
    */
   public static double nr_betacf(double a, double b, double x)
   { /* nr_betacf */
     double
       qap,
       qam,
       qab,
       em,
       tem,
       d;
     double
       bz,
       bm= 1.0,
       bp,
       bpp;
     double
      az=1.0,
      am=1.0,
      ap,
      app,
      aold;
     int
       m,
       iterationsMax= 100;
     double epsilon= 3.0e-7;
     
     qab= a+b;
     qap= a+1.0;
     qam= a-1.0;
     bz= 1.0-qab*x/qap;
     
     for (m=1;m<=iterationsMax;m++)
     {
       em= (double) m;
       tem= em+em;
       d= em*(b-em)*x/((qam+tem)*(a+tem));
       ap= az+d*am;
       bp= bz+d*bm;
       d= -(a+em)*(qab+em)*x/((qap+tem)*(a+tem));
       app=ap+d*az;
       bpp=bp+d*bz;
       aold=az;
       am=ap/bpp;
       bm=bp/bpp;
       az=app/bpp;
       bz=1.0;
       if (Math.abs(az-aold) < (epsilon*Math.abs(az)))
       {
         okBetaCF= true;
         return(az);
       }
     }
     
     /* FAILED */
     /*
     System.out.println("betacf(): a("+a+") or b(" + b +
                        ") too big, or #iter.Max("+
                        iterationsMax+  "d) too small");
     */
     okBetaCF= false;
     
     return(az);
   } /* nr_betacf */
   
   
   /**
    * nr_betai() - return the incomplete Beta function 'x(a,b).    
    * This method was derived from GELLAB-II which was derived from 
    * Numerical Recipes in C, Pg 179, Section 6.3 N.R.C.
    * @param a - a parameter of 'x(a,b)
    * @param b - b parameter of 'x(a,b)
    * @param x - x parameter of 'x(a,b)
    * @return value if succeed else 0.0 if it fails and set the
    *         RTN: okBetaI flag to false.
    * @see #nr_betacf
    * @see #nr_gammln
    */
   public static double nr_betai(double a, double b, double x)
   { /* nr_betai */
     double
       rVal,
       bt;
     
     if (x < 0.0 || x > 1.0)
     {
       //System.out.println("Bad x=" + x + " in betai().");
       okBetaI= false;
       return(0.0);
     }
     
     if (x == 0.0 || x == 1.0)
       bt= 0.0;
     else
     {
       double
       v1= nr_gammln(a+b),
       v2= nr_gammln(a),
       v3= nr_gammln(b);
       bt= Math.exp(v1 - v2 - v3 + a*Math.log(x) + b*Math.log(1.0-x));
     }
     
     if (x < (a+1.0)/(a+b+2.0))
       rVal= (bt * nr_betacf(a,b,x)/a);
     else
       rVal= (1.0 - bt * nr_betacf(b, a, 1.0-x)/b);
     if(!okBetaCF)
       return(0.0);
     
     okBetaI= true;
     return(rVal);
   } /* nr_betai */
   
   
   /**
    * nr_sort() - quick-sort of bin of data[0:n-1] in assend. numerical order.
    * Uses the partitioning-exchange sorting method.
    * This method was derived from GELLAB-II which was derived from 
    * Numerical Recipes in C.
    * @param n is the amount of data.
    * @param data is the set of data [0:n-1].
    * @return false if blow the stack or other problems.
    */
   public static boolean nr_sort(int n, double data[])
   { /* nr_sort */
     int
       mThreshold= 7,		/* trigger size for insertion sort */
       maxPDL= 50,	  	/* size 2*log2(N) - N is max(n) allowed*/
       i,
       ir= n-1,
       j,
       k,
       lb= 0,
       topPDL= 0;
     int pdl[]= new int[maxPDL+1];
     double
       d,
       tmpD;
     
     /* [1] Make sure valid data */
     if(n<0 || data==null)
       return(false);		       /* bad data */
     
     /* [2] Not already sorted - do the sort */
     while(true)
     { /* Sort the data */
       if ((ir-lb) < mThreshold)
       { /* [2.1] Do insertion sort for small sub arrays < mThreshold */
         for (j=lb+1; j<=ir; j++)
         {
           d= data[j];
           for (i=j-1; i>=0; i--)
           {
             if(data[i] <= d)
               break;
             data[i+1]= data[i];
           }
           data[i+1]= d;
         }
         if (topPDL == 0)
           break;		/* finished insert. sort. */
         
         /* Pop stack and start next partition.. */
         ir= pdl[topPDL--];
         lb=  pdl[topPDL--];
       } /* Sort the data */
       
       else
       { /* [2.2] Choose median of left center, and right elements
          * as partitioning element a. Also rearrange so that
          * a[l+1] <= a[0] <= a[ir].
          */
         k= ((lb+ir) >> 1);
         
         tmpD= data[k];                /* swap it */
         data[k]= data[lb+1];
         data[lb+1]= tmpD;
         
         if(data[lb+1] > data[ir])
         { /* left - swap it */
           tmpD= data[lb+1];
           data[lb+1]= data[ir];
           data[ir]= tmpD;
         }
         
         if(data[lb] > data[ir])
         { /* center - swap it */
           tmpD= data[lb];
           data[lb]= data[ir];
           data[ir]= tmpD;
         }
         
         if(data[lb+1] > data[lb])
         { /* right  - swap it */
           tmpD= data[lb+1];
           data[lb+1]= data[ir];
           data[ir]= tmpD;
         }
         
         i= lb+1;		        /* init ptrs for partitioning */
         j= ir;
         d= data[lb];		    /* the partitioning element */
         while(true)
         { /* innermost loop */
           do
             i++;
           while (data[i] < d);  /* scan for elements > a */
           
           do
             j--;
           while (data[j] > d);  /* scan for elements < a */
           
           if( j < i)
             break;		          /* ptrs crossed. Partitioning is complete */
           tmpD= data[i];       /* swap it */
           data[i]= data[j];
           data[j]= tmpD;
         } /* innermost loop */
         
         data[lb]= data[j];   	/* insert partitioning element */
         data[j]= d;
         topPDL +=2;
         
         /* Push ptrs into larger subarray on stack, process
          * smaller subarray immediately.
          */
         if(topPDL > maxPDL)
         { /* [nr_sort] DRYROT pdl[0:maxPDL] too small. */
           return(false);
         }
         if((ir-i+1) >= (j-1))
         {
           pdl[topPDL]=   ir;
           pdl[topPDL-1]= i;
           ir= (j-1);
         }
         else
         {
           pdl[topPDL]=   j-1;
           pdl[topPDL-1]= lb;
           lb= i;
         }
       }
     } /* Sort the data */
     
     return(true);
   } /* nr_sort */
   

   /**
    * nr_ksone() - Kolmogorov-Smirnov statistic d and the
    * probability of the null hypothesis of a data serie follow 
    * a Log Normal distribution.
    * This method was derived from GELLAB-II which was derived from 
    * Numerical Recipes in C, Pg 625, Section 14.3 N.R.C. 2nd Edition.
    * @param data [0:n1-1] data
    * @param n # of items in data
    * @return false if any errors in the data or calcs. Data is returned
    * in the global class variables: returns data in global class variables:
    *<PRE>
    *    RTN: dKS     -  K-S statistic, and
    *    RTN: probKS  - probl of null hypoth same distribution
    *</PRE>
    * @see #nr_probks
    * @see #nr_sort
    */
   public static boolean nr_ksoneLogNormal(double data[])
   { 
        Vector logdata= new Vector();
        
        for (int i=0; i<data.length; i++){
            
            double value=data[i];
            
            //if value==0 is not include in the test.
            if (value<0){
                value=-Math.log(-value);
                logdata.addElement(new Double(value));                
            }if (value>0){
                value=Math.log(value);
                logdata.addElement(new Double(value));
            }
        }
        
        double[] logdata2=new double[logdata.size()];
        for (int i=0; i<logdata.size(); i++)
            logdata2[i]=((Double)logdata.elementAt(i)).doubleValue();
        
        return MathMAE.nr_ksoneNormal(logdata2);
   }     

   /**
    * nr_ksone() - Kolmogorov-Smirnov statistic d and the
    * probability of the null hypothesis of a data serie follow 
    * a Normal distribution.
    * This method was derived from GELLAB-II which was derived from 
    * Numerical Recipes in C, Pg 625, Section 14.3 N.R.C. 2nd Edition.
    * @param data [0:n1-1] data
    * @param n # of items in data
    * @return false if any errors in the data or calcs. Data is returned
    * in the global class variables: returns data in global class variables:
    *<PRE>
    *    RTN: dKS     -  K-S statistic, and
    *    RTN: probKS  - probl of null hypoth same distribution
    *</PRE>
    * @see #nr_probks
    * @see #nr_sort
    */
   public static boolean nr_ksoneNormal(double data[])
   { 
        double dt,en,ff,fn,fo=0.0;
        int n=data.length;
        
        double mean = Stat.mean(data);
        double sd = Stat.standardDeviation(data);
        
        double probKS= 0.0;			/*  default values */
        double dKS= 0.0;

        if(n<=1)
            return false;

        Sort.sort(data);
        en=n;
        for (int j=0;j<n;j++) {
            fn=j/en;
            ff=Stat.normalProb(mean,sd,data[j]);
            dt=Math.max(Math.abs(fo-ff),Math.abs(fn-ff));
            if (dt > dKS) 
                dKS=dt;
            fo=fn;
        }
        
        en=Math.sqrt(en);
        
        MathMAE.dKS=dKS;
        probKS= MathMAE.nr_probks((en+0.12+0.11/en)*(dKS));
        MathMAE.probKS=probKS;

        if(!MathMAE.okProbKS)
            return(false);
        
        return(true);        
    }     

   /**
    * nr_kstwo() - Kolmogorov-Smirnov statistic d and the
    * probability of the null hypothesis of 2 bins of data.
    * This method was derived from GELLAB-II which was derived from 
    * Numerical Recipes in C, Pg 625, Section 14.3 N.R.C. 2nd Edition.
    * @param data1 [0:n1-1] data1
    * @param n2 # of items in data 1
    * @param data1 [0:n2-1] data2
    * @param n2 # of items in data 2
    * @return false if any errors in the data or calcs. Data is returned
    * in the global class variables: returns data in global class variables:
    *<PRE>
    *    RTN: dKS     -  K-S statistic, and
    *    RTN: probKS  - probl of null hypoth same distribution
    *</PRE>
    * @see #nr_probks
    * @see #nr_sort
    */
   public static boolean nr_kstwo(double data1[], int n1, 
                                  double data2[], int n2)
   { /*nr_kstwo*/
     int
       j1= 0,
       j2= 0;
     double
       alam,
       d1,
       d2,
       dt,
       en1,
       en2,
       en,
       fn1= 0.0,
       fn2= 0.0;

     probKS= 0.0;			/*  default values */
     dKS= 0.0;
     
     if(n1<=1 || n2<=1 || data1==null || data2==null)
       return(false);
          
     if(!nr_sort(n1,data1) || !nr_sort(n2,data2))
       return(false);
     
     en1= n1;
     en2= n2;
     
     while(j1 < n1 && j2 < n2)
     {
       if((d1= data1[j1]) <= (d2= data2[j2]))
         fn1= (j1++)/en1;
       if(d2 <= d1)
         fn2= (j2++)/en2;
       if((dt= Math.abs(fn2 - fn1)) > dKS)
         dKS= dt;
     }
     
     en= Math.sqrt((en1 * en2)/(en1 + en2));     
     alam= (en + 0.12 + (0.11/en)) * dKS;
     
     probKS= nr_probks(alam);
    
     if(!okProbKS)
       return(false);
     
     return(true);
   } /* nr_kstwo */
      
   
   /**
    * nr_probks() - Calc Kolmogorov-Smirnov probability qKS.
    * This method was derived from GELLAB-II which was derived from 
    * Numerical Recipes in C, Pg 626, Section 14.3 N.R.C. 2nd Edition.
    * @param alam the value computed in kstwo().
    * @return the probability. Set okProbKS to
    * false if there is a problem.
    */
   public static double nr_probks(double alam)
   { /* nr_probks*/
     int j;
     double
       eps1= 0.001,		/* magic #s */
       eps2= 1.0E-8,
       a2,
       fac= 2.0,
       sum= 0.0,			/* value returned */
       term,
       termbf= 0.0,
       absTerm;
     
     a2= -2.0 * alam * alam;
     for(j=1; j<=100; j++)
     {
       term= fac * Math.exp(a2*j*j);
       sum += term;
       absTerm= (term>0.0) ? term : -term;  /* Math.abs(term) */
       if(absTerm <= eps1*termbf || absTerm <= eps2*sum)
       {
         okProbKS= true;
         return(sum);             /* done with computation */
       }
       fac= -fac;		        /* keep computing w/alt. signs*/
       termbf= (term>0.0) ? term : -term;  /* Math.abs(term) */
     }
     
     sum= 1.0;			/* if failed to converge */
     okProbKS= false;
     return(sum);
   } /* nr_probks*/
   
   
   /**
    * euclidDist() - compute Euclidean distance or (sum dist**2)
    * The data is data1[0:n-1] and data2[0:n-1].
    * @param data1 is vector [0:n-1] of object 1
    * @param data2 is vector [0:n-1] of object 2
    * @param n is size of vector
    * @param rtnDistSqFlag return(sum dist**2) else Euclidean distance.
    * @return euclidean distance or distSq, else -1.0 if an error
    */
   public static float euclidDist(float data1[], float data2[], int n,
                                 boolean rtnDistSqFlag)
   { /* euclidDist*/
     if(n==0 || data1==null || data2==null)
       return(-1.0F);
     double sumSq= 0.0;
     float diff;
     
     for(int i=0; i<n;i++)
     {
       diff= (data1[i] - data2[i]);
       sumSq += diff*diff;
     }
     if(rtnDistSqFlag)
       return((float)sumSq);
     else
       return((float)Math.sqrt(sumSq/n));
   } /* euclidDist*/
   
   /**
    * cityBlockDist() - compute city-block distance of 2 vectors.
    * The data vectors are data1[0:n-1] and data2[0:n-1].
    * @param data1 is vector [0:n-1] of object 1
    * @param data2 is vector [0:n-1] of object 2
    * @param n is size of vector
    * @param rtnAbsSumFlag return (sum absDiffs) else mean city block distance.
    * @return city-block distance. Return -1.0 if there is an error
    */
   public static float cityBlockDist(float data1[], float data2[], int n,
                                     boolean rtnAbsSumFlag)
   { /* cityBlockDist */
     if(n==0 || data1==null || data2==null)
       return(-1.0F);
     float 
       sum= 0.0F,
       diff;
     for(int i=0; i<n;i++)
     {
       diff= (data1[i] - data2[i]);
       sum += Math.abs(diff);
     }
     if(rtnAbsSumFlag)
       return(sum);
     else
       return((float)Math.sqrt((double)sum/n));
   } /* cityBlockDist */
   
   
   /**
    * calcPearsonCorrCoef() - compute Pearson correlation coefficient
    * The data is data1[0:n-1] and data2[0:n-1].
    * This method was derived fromWeinstein) U. Scherf, Nat.Genetics 
    * (2000) 24:236-244, pg 243, and the version for large samples in 
    * Snedecore & Cochran 1st Edition page 175.
    * @param data1 is vector [0:n-1] of object 1
    * @param data2 is vector [0:n-1] of object 2
    * @param n is size of vector
    * @param usePopulationCovar flag to compute population covariance
    *      (Weinstein) U. Scherf, Nat.Genetics (2000) 24:236-244, pg 243.
    *      else version for large samples in Snedecore & Cochran 1st
    *      Edition page 175.
    * @return calcPearsonCorrCoef else 1000.0 if there is an error
    */
   public static float calcPearsonCorrCoef(float data1[], float data2[], int n,
                                           boolean usePopulationCovar)
   { /* calcPearsonCorrCoef */
     if(n==0 || data1==null || data2==null)
       return(1000);
     
     double
       d1,
       d2,
       sum1= 0.0,
       sum2= 0.0,
       mn1,
       mn2,
       sum1Sq= 0.0,
       sum2Sq= 0.0,
       sum12= 0.0,
       sqrt1,
       sqrt2,
       r;
     
     for(int i=0; i<n;i++)
     {
       d1= data1[i];
       d2= data2[i];
       sum1 += d1;
       sum2 += d2;
       sum1Sq += d1*d1;
       sum2Sq += d2*d2;
       sum12 += d1*d2;
     }
     mn1= sum1/n;
     mn2= sum2/n;
     
     if(usePopulationCovar)
     { /* use population covariance */
       /* (Weinstein) U. Scherf, Nat.Genetics (2000) 24:236-244, pg 243,  */
       sqrt1= Math.sqrt(sum1Sq - n*mn1*mn1);
       sqrt2= Math.sqrt(sum2Sq - n*mn2*mn2);
       r= (sum12 - n*mn1*mn2) / (sqrt1*sqrt2);
     }
     else
     { /* version for large samples */
       /* Snedecore & Cochran 1st Edition page 175 */
       sqrt1= Math.sqrt(sum1Sq*sum2Sq);
       r= sum12/sqrt1;
     }
     
     return((float)r);
   } /* calcPearsonCorrCoef */
   
   
   /**
    * log2Zero() - compute log2((x==0.0 ? 0.0 : x) - avoid log2(0.0)!
    * This defaults log2(0.0) to log2(1.0).
    * @param x argument for log2
    * @return log2((x==0.0 ? 0.0 : x) - avoid log2(0.0)!
    * @see #log2
    */
   public static double log2Zero(double x)
   { /* 1og2Zero */
     if(x==0.0)
       return(0.0);
     else
       return(log2(x));
   } /* log2Zero */
   
   
   /**
    * log2() - compute log(x) base 2.
    * Since log2(x)= log2(e)*ln(x).
    * @param x argument for log2
    * @return log(x) base 2
    */
   public static double log2(double x)
   { /* log2 */
     return(log2e * Math.log(x));
   } /* log2 */
   
   
   /**
    * alog2() - compute alog(x) base 2.
    * Since ln(x)= log2(x)/log2(e) = log2(x) * ln2.
    * @param x argument for alog
    * @return alog(x) base 2
    */
   public static double alog2(double x)
   { /* alog2 */
     return(Math.exp(ln2 * x));
   } /* alog2 */
   
   
   /**
    * logZero() - compute log10((x==0.0 ? 0.0 : x) - avoid log(0.0)!
    * This defaults log10(0.0) to log(1.0).
    * @param x argument for log
    * @return log10((x==0.0 ? 0.0 : x) - avoid log(0.0)!
    * @see #log10
    */
   public static double logZero(double x)
   { /* 1ogZero */
     if(x==0.0)
       return(0.0);
     else
       return(log10(x));
   } /* logZero */
   
   
   /**
    * log10() - compute log(x) base 10.
    * Since log10(x)= log10e * ln(x).
    * @param x argument for log
    * @return log(x) base 10
    */
   public static double log10(double x)
   { /* log10 */
     return(log10e * Math.log(x));
   } /* log10 */
   
   
   /**
    * alog10() - compute alog(x) base 10.
    * Since ln(x)= log10(x)/log10(e) = log10(x) * ln10.
    * @param x argument for alog
    * @return alog(x) base 10
    */
   public static double alog10(double x)
   { /* alog10 */
     return(Math.exp(ln10 * x));
   } /* alog10 */
   
   
   /**
    * calcLogConversionConstants() - compute the log conversion constants.
    * Note: uncomment it when you need to run it and remove private etc.
    */
   //private void calcLogConversionConstants()
   //{ /* calcLogConversionConstants */
   /*
     double
       ln2= Math.log(2.0),
       ln10= Math.log(10.0),
       log2e= 1.0/ln2,
       log10e= 1.0/ln10,
       xxx= 1.234567,
       log2XXX= log2e * Math.log(xxx),
       log10XXX= log10e * Math.log(xxx),
       xxx2= Math.exp(ln2 * log2XXX),
       xxx10= Math.exp(ln10 * log10XXX);
     
     System.out.println(" ln2= Math.log(2.0)="+ln2);
     System.out.println(" ln10= Math.log(10.0)="+ln10);
     System.out.println(" exp(ln2)= Math.exp(ln2)="+Math.exp(ln2));
     System.out.println(" exp(ln10)= Math.exp(10.0)="+Math.exp(ln10));
     System.out.println(" Math.E="+Math.E);
     System.out.println(" log2e= 1/ln2="+log2e);
     System.out.println(" log10e= 1/ln10 ="+log10e);
     System.out.println(" xxx ="+xxx);
     System.out.println(" log2XXX= log2e * Math.log(xxx) ="+log2XXX);
     System.out.println(" log10XXX= log10e * Math.log(xxx) ="+log10XXX);
     System.out.println(" xxx2= Math.exp(ln2 * log2XXX) ="+xxx2);
     System.out.println(" xxx10= Math.exp(ln10 * log10XXX) ="+xxx10);
     */
 //} /* calcLogConversionConstants */
   
   
 } /* end of class MathMAE */
 
