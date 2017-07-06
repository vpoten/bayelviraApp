/* Chi2.java */
package elvira.tools;

import java.io.*;
import java.lang.Math;

public class Chi2 {

private static final double Z_MAX = 6.0;	// Maximum meaningful z value
private static final double BIGX = 20.0;               // max value to represent exp(x)
private static final double LOG_SQRT_PI = 0.5723649429247000870717135; // log(sqrt(pi))
private static final double I_SQRT_PI = 0.5641895835477562869480795;   // 1 / sqrt(pi)
private static final double CHI_EPSILON = 0.000001;	// Accuracy of critchi approximation
private static final double CHI_MAX = 99999.0;	// Maximum chi-square value
    


private static double poz(double z) 
{
  double y, x, w;
    
  if (z == 0.0) x = 0.0;
  else{
    y = 0.5 * Math.abs(z);
    if (y>=(Z_MAX*0.5)) x = 1.0;
    else if(y<1.0){
      w = y * y;
      x = ((((((((0.000124818987 * w
                      - 0.001075204047) * w + 0.005198775019) * w
                      - 0.019198292004) * w + 0.059054035642) * w
                      - 0.151968751364) * w + 0.319152932694) * w
                      - 0.531923007300) * w + 0.797884560593) * y * 2.0;
    } 
    else {
      y -= 2.0;
      x = (((((((((((((-0.000045255659 * y
                          + 0.000152529290) * y - 0.000019538132) * y
                          - 0.000676904986) * y + 0.001390604284) * y
                          - 0.000794620820) * y - 0.002034254874) * y
                          + 0.006549791214) * y - 0.010557625006) * y
                          + 0.011630447319) * y - 0.009279453341) * y
                          + 0.005353579108) * y - 0.002141268741) * y
                          + 0.000535310849) * y + 0.999936657524;
    }
  }

  return (z>0.0)?((x + 1.0) * 0.5):((1.0 - x) * 0.5);
}






private static double ex(double x) 
{
  return (x<-BIGX)?0.0:Math.exp(x);
}   

// changed visibility from private to public on Mar.12.2007 by
// dalgaard
public static double pochisq(double x, int df) 
{
    double a, y, s;
    double e, c, z;
    boolean even;	// True if df is an even number

        
    if(x<=0.0 || df<1) return 1.0;
    a = 0.5 * x;
    even = df%2==0;
    //if(df>1) y = ex(-a); --> la cambio por la de abajo (jgamez)
    y = ex(-a);
    s = (even?y:(2.0 * poz(-Math.sqrt(x))));
    if(df>2){
      x = 0.5 * (df - 1.0);
      z = (even ? 1.0 : 0.5);
      if(a>BIGX){
	e = (even ? 0.0 : LOG_SQRT_PI);
        c = Math.log(a);
                
	while(z<=x){
	  e = Math.log(z) + e;
          s += ex(c * z - a - e);
          z += 1.0;
        }
              
        return s;
      }
      else{
	e = (even ? 1.0 : (I_SQRT_PI / Math.sqrt(a)));
        c = 0.0;

        while(z<=x){
	  e = e * (a / z);
          c = c + e;
          z += 1.0;
        }
            
	return c * y + s;
      }
    }
    else return s;
}

/**
 * returns the critical value for a chi-square
 * disribution with df degrees of freedom and
 * using p as significance value
 */

public static double critchi(double p, int df) 
{   
  double minchisq = 0.0;
  double maxchisq = CHI_MAX;
  double chisqval;
        
  if(p<=0.0) return maxchisq;
  else if(p>=1.0) return 0.0;
		      
  chisqval = df / Math.sqrt(p);    // fair first value

  while((maxchisq - minchisq) > CHI_EPSILON){
    if(pochisq(chisqval,df)<p) maxchisq = chisqval;
    else minchisq = chisqval;

    chisqval = (maxchisq + minchisq) * 0.5;
  }
    
  return chisqval;
}


    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
    * For performing tests 
    */
    public static void main(String args[]) 
    { 

      if (args.length<5){
        System.out.println("\nArguments are: numSamples numClassLabels numXLabels I(C,X) significance....");
        System.exit(0);
      }
      
      int n1,n2,N;
      double MI;
      int degreesOfFreedom;
      double dos_N_MI;
      double sig;

      N = Integer.valueOf(args[0]).intValue();
      n1 = Integer.valueOf(args[1]).intValue();
      n2 = Integer.valueOf(args[2]).intValue();
      MI = Double.valueOf(args[3]).doubleValue();
      sig = Double.valueOf(args[4]).doubleValue();
   
      System.out.println("\nParameters: N="+N+", |C|="+n1+ ", |X|="+n2+", MI(C,X)="+MI); 

      dos_N_MI = 2.0 * N * MI;
      degreesOfFreedom = (n1-1)*(n2-1);    
      System.out.println("\ndos_N_MI = " + dos_N_MI + " df = " +
         degreesOfFreedom + "    chi2 = " + Chi2.critchi(1-sig,degreesOfFreedom));

      if (dos_N_MI >= Chi2.critchi(1-sig,degreesOfFreedom) )
        System.out.println("Pasa el test");
      else System.out.println("No pasa el test");
      

      //System.out.println("\n"+Chi2.critchi(sig,3));
      

      return;
    }//End main 
}


