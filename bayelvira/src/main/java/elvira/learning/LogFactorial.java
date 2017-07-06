package elvira.learning;
 
import java.io.*;
import java.util.Vector;
 
 /**
  * LogFactorial.java
  * Esta clase implementa una forma eficiente de calcular Logaritmos de 
  * factoriales de forma reiterada.
  */

public class LogFactorial implements Serializable{

Vector index;
Vector computed;


public LogFactorial(){
	
    Double valfact = new Double(0.0);
    Integer val = new Integer(0);
    index = new Vector();
    computed = new Vector();
    index.addElement(val);
    computed.addElement(valfact);
    val = new Integer(1);
    index.addElement(val);
    computed.addElement(valfact);
}
	
	
	
public double logFactorial(int x){
   
   int i,j,valbefore,valafter;
   double valfact;
   Integer valx;
   Double valcomputed;
   if(x > ((Integer)index.lastElement()).intValue()){
      valfact = ((Double)computed.lastElement()).doubleValue();
      for(j=((Integer)index.lastElement()).intValue(); j<x ; j++){
 	      valfact+=(Math.log(j+1)/Math.log(10));
      }
      valcomputed = new Double(valfact);
      computed.addElement(valcomputed);
      valx = new Integer(x);
      index.addElement(valx);
   }
   else {
      i = constains(x);
 	   if(x == ((Integer)index.elementAt(i)).intValue()) 	    
 	      return ((Double)computed.elementAt(i)).doubleValue(); 	
 	   else {
 	      if( x > ((Integer)index.elementAt(i)).intValue()){
 	         valx = new Integer(x);
 	         i++; 		
 	         index.insertElementAt(valx,i); 		
 	         computed.insertElementAt(new Double(-1.0),i); 	    
 	         }
 	      else { 		
 	         valx = new Integer(x); 		
 	         index.insertElementAt(valx,i); 		
 	         computed.insertElementAt(new Double(-1.0),i); 	    
 	         } 	
 	   }
 	   valbefore = ((Integer)index.elementAt(i-1)).intValue(); 	
 	   valafter = ((Integer)index.elementAt(i+1)).intValue(); 	 	
 	   
 	   if((valafter - x) < (x - valbefore)) { 	    
 	      valfact = ((Double)computed.elementAt(i+1)).doubleValue(); 	    
 	      for(j = valafter ; j > x ; j--){ 		
 	         valfact-=(Math.log(j)/Math.log(10)); 	    
 	         } 	
 	   } 
 	   else { 	    
 	      valfact = ((Double)computed.elementAt(i-1)).doubleValue(); 	    
 	      for(j = valbefore;j<x;j++){ 		
 	         valfact+=(Math.log(j+1)/Math.log(10)); 	    
 	      } 	
 	   } 	
 	   valcomputed = new Double(valfact); 	
 	   computed.setElementAt(valcomputed,i); 	 	     
 	} 
   return valfact; 
}	               

private int constains(int x){ 	 	
   
   int left,right,midle; 	   
   boolean found = false; 	 	
   
   left = 0; 	
   right = index.size()-1; 	
   midle = (left + right)/2; 	 
   
   while((left <= right) & (!found)){ 	    
      midle = (left + right)/2; 	    
      if(x == ((Integer)index.elementAt(midle)).intValue()) 		
         found = true; 	    
      if(x < ((Integer)index.elementAt(midle)).intValue()) 		
         right = midle - 1; 	    
      else left = midle + 1; 	     	
   }
   return midle;     
}           


static public double gammaln(double xx){
    
    double x,y,tmp,ser;
    double[] cof = new double[6];
    cof[0] = 76.18009172947146;
    cof[1] = -86.50532032941677;
    cof[2] = 24.01409824083091;
    cof[3] = -1.231739572450155;
    cof[4] = 0.1208650973866179e-2;
    cof[5] = -0.5395239384953e-5;
    int j;
    y = xx;
    x = xx;
    tmp = x+5.5;
    tmp-= (x+0.5)*Math.log(tmp);
    ser = 1.000000000190015;
    for(j=0 ; j<=5 ; j++) ser+=cof[j]/++y;
    return (-tmp+Math.log(2.5066282746310005*ser/x));

}

static public double gammp(double a, double x){

    double gammser,gammcf,gln;
    if(x<0.0 || a <= 0.0) System.out.println("Invalid Parameters in routine gammp in LogFactorial");
    if(x<(a+1.0)) {
       gammser = gser(a,x);
       return gammser;
    } else {
       gammcf = gcf(a,x);
       return (1.0 - gammcf);
    }
}

static public double gser(double a, double x){

    int ITMAX=100;
    double EPS=3.0e-7;
    int n;
    double sum,del,ap,gln,gammser;
    gln = gammaln(a);
    if(x <= 0.0){
       if(x < 0.0) System.out.println("Error: Parameter x is less than 0.0 in routine gser in LogFactorial");
       gammser = 0.0;
       return gammser;
    }else{
       ap = a;
       del = sum = 1.0/a;
       for(n=1; n<= ITMAX; n++){
          ++ap;
          del*=x/ap;
          sum+=del;
          if(Math.abs(del) < (Math.abs(sum)*EPS)){
             gammser=sum*Math.exp(-x+a*Math.log(x)-gln);
             return gammser;
          }
       }
       System.out.println("Parameter a too large, ITMAX too small in routine gser in LogFactorial");
       gammser=sum*Math.exp(-x+a*Math.log(x)-gln);      
       return gammser;
    }
}


static public double gcf(double a, double x){

    int ITMAX=100;
    double EPS=3.0e-7;
    double FPMIN=1.0e-30;
    int i;
    double an,b,c,d,del,h;
    double gln;
    double gammcf;
    gln = gammaln(a);
    b=x+1.0-a;
    c=1.0/FPMIN;
    d=1.0/b;
    h=d;
    for(i=1 ; i<= ITMAX; i++){
       an=-i*(i-a);
       b+=2.0;
       d=an*d+b;
       if(Math.abs(d) < FPMIN) d=FPMIN;
       c=b+an/c;
       if(Math.abs(c) < FPMIN) c=FPMIN;
       d=1.0/d;
       del=d*c;
       h*=del;
       if(Math.abs(del-1.0) < EPS) break;
    }
    if(i > ITMAX) System.out.println("Parameter a is too large, ITMAX too small in routine gcf");
    gammcf = Math.exp(-x+a*Math.log(x)-gln)*h;
    return gammcf;
}

static public double chiSquare(double st, double dgf){
    return (gammp(dgf/2.0,st/2.0));
}


}//end of class
