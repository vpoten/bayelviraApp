/* FactorisationTools */

package elvira.tools;


import java.io.*;
import java.util.Vector;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;


/**
 * Class <code>FactorisationTools</code>.
 * Contains the parameters and  the methods used in approximate 
 * factorisation of probability trees.
 *
 * @author Irene Martinez (irene@ual.es)
 * @since 11/06/2004
 */

public class FactorisationTools {    

/**
 * This field indicates the kind of factorisation to carry out:
 * <ol>
 * <li>0: split only.
 * <li>1: factorise only.
 * <li>2: split and factorise.
 * </ol>
 */
private int factMethod;

/**
 * The method selected for computing the proportionality factor in approximate 
 * factorisation.
 */
private int approxMethod;   

/**
 * The method for measure the divergence between the trees
 *       (the accuracy of the approximation)
 */
private int divergenceMethod; 

/**
 * Maximun error allowed between the nodes of two trees.
 * It´s applied only when the @param divergenceMethod is -1.
 */
private double deltaNode;

/**
 * Maximun distance allowed between two trees in the divergence methods
 */
private double deltaTree;  

/**
 * Value between 0 an 1 needed for obtain the maximun level that can be reached, 
 * to the bottom of the tree, when the variable is being looked for.
 * This level will be calculated just before explore each potential tree,
 * as the product of: @param paramLevel * num_Variables_of_Potential 
 */
private double paramLevel;

/**
 * Parameter loading the maximun level that can be reached exploring the
 * actual potential tree.
 */
private int levelDown;

/**
 * Actual level of the node. Its initial value is -1.  
 */
private int actualLevel; 

/**
 * Flag indicating whether check the distance between nodes
 */
private boolean distanceNodes;

/**
 * Flag indicating whether check the distance between trees
 */
private boolean distanceTrees; 

/**
 * Flag indicating whether check the sizes of the potentials.
 */
public boolean sizesPot = true; 

/**
 * Indicates the phase in wich the factorisation will be applied:
 *  0:compilation phase, 1:propagation phase, 2:both phases
 */
private int facPhase;  

/** 
 * Counter of the number of split operations made
 */
private int numSplit=0; 

/**
 * Vector to store the sizes of the potentials during the
 * marginalization, just before delete the variable.
 */
public Vector vecPotSizes; 

/**
 * Vector to store the maximun of the proportionality factors in each of the 
 * factorisations made.
 */
public Vector vecDistApproxim ;

/**
 * Percentage of proportional children that must been reached before factorise 
 * the tree. Value of 1 means that all the children must be proportional.
 */
private double proporChild;

/**
 * Counter of number of approximations carried out  in the compilation phase.
 */ 
private int counterFacCompil;

/**
 * This flag remains equal true while the factorisation is being carried out in 
 * compilation phase.   
 */
private boolean compilPhase= false;



/**
 * Constructors.
 */

/**
 * Create an instance of the class initializing the parameters for exact
 * factorisation.
 */
public FactorisationTools(int method, int fase) {

  
  setFactorisationErrors(0.0,-1); 
  setFactMethod(method);                                 
  setApproxMethod(-1);
  setDivergenceMethod(-2);
  setProporChild(1);
  setParamLevel(1);
  setActualLevel(-1);
  setFacPhase(fase);
  
   // Initialize the vectors
  vecDistApproxim=new Vector();  
  vecPotSizes=new Vector();
  
  setCounterFCompil();
  
  
}


public FactorisationTools(double fNod, double fTr, int mFact, int appMethod, 
                          int divMethod, double percentProp, double level,int f) {

  
  setFactorisationErrors(fNod,fTr); 
  setFactMethod(mFact);                                 
  setApproxMethod(appMethod);
  setDivergenceMethod(divMethod);
  setProporChild(percentProp);
  setParamLevel(level);
  setActualLevel(-1);
  setFacPhase(f);
  
   // Initialize the vectors
  vecDistApproxim=new Vector();  
  vecPotSizes=new Vector();
  
  setCounterFCompil();
  
}



/**
 * ..........................Access methods.........................
 */


/**
 * Sets the factorisation errors, if they are non negative.
 * @param fN,fT the factorisation errors: distance between Nodes and distance 
 * between Trees.
 */
public void setFactorisationErrors(double fN, double fT) {
  if(fN>=0.){
      deltaNode= fN;
      distanceNodes=true;
  }
  else {
      deltaNode=1000;
      distanceNodes=false;
  }
  
  if(fT>=0.){
      deltaTree= fT;
      distanceTrees=true;
  }
  else{ 
      deltaTree=1000;
      distanceTrees=false;
  }
}

/**
 * Sets the factorisation method.
 * @param op the factorisation method (0 means only
 * split, 1 only factorise and 2 means split and factorise).
 */
public void setFactMethod(int op) {
 factMethod = op;
 }

public void setApproxMethod(int appM){
 approxMethod =  appM;  
}

public void setDivergenceMethod(int d){
 divergenceMethod =  d;  
}

public void  setProporChild(double por){
 proporChild = por;
}

public void  setActualLevel(int lev){
 actualLevel =lev;  
}

public void setParamLevel(double pl){
  paramLevel=pl;
}

public void setMaxLevelD(int numV){
 levelDown = (int) Math.floor(paramLevel*numV);  //ceil( paramLevel*numV);  
}

public void setFacPhase(int f){
 facPhase =  f;  
}

/**
 * Changes the value of the boolean flag 'compilPhase'.
 */
public void setcompilPhase(){
 if (compilPhase)
     compilPhase=false;
 else
     compilPhase=true;
}

/**
 * Set the field as the number of elements of  vector 'vecDistApproxim'.
 * It's used only when factorisation is made in both phases.
 */
public void setCounterFCompil(){
  counterFacCompil= getVecOfDistA().size();
}

/**
 * Increments the counter of number of split operations
 */
public void incNumSplit(){
  numSplit++;  
}

/**
 * Increments the counter of actual level
 */
public void incActualLevel(){
  actualLevel++;  
}
 
/**
 * Decrements the counter of actual level
 */
public void decActualLevel(){
  actualLevel--;  
}
/**
 * Returns the factorisation errors.
 * If @param n is  zero returns distance between Nodes, in other case returns 
 *  the distance between Trees.
 */
public double getFactorisationErrors(int n) {
  if(n==0) 
      return deltaNode;
  else 
      return(deltaTree);
}

/**
 * Returns the factorisation method,
 *(0 means only split, 1 only factorise and 2 means split and factorise).
 */
public int getFactMethod() {
 return factMethod;
}

public int getApproxMethod(){
 return approxMethod ;  
}

public int getDivergenceMethod(){
 return divergenceMethod;  
}

public double  getProporChild(){
 return proporChild;
}

public int  getActualLevel(){
 return actualLevel;  
}

public int  getMaxLevelD(){
 return levelDown;  
}

public int getCounterFCompil(){
  return counterFacCompil;
}
/**
 *@return true if the maximum level exploring the tree has been reached.
 */
public boolean isMaxLevel(){
 if( getActualLevel() >= getMaxLevelD() )
     return true;
 else 
     return false;
}

/**
 *@return true if the factorisation method is 0 (split only).
 */
public boolean isOnlySplit(){

  if(factMethod==0) 
        return true;
    else 
        return false;
}

public Vector getVecOfSizes(){
    return vecPotSizes;
}

public Vector getVecOfDistA(){
    return vecDistApproxim;
}

public int getFacPhase(){
  return facPhase;  
}

public int getNumSplit(){
  return numSplit;  
}

public boolean iscompilPhase(){
  return compilPhase;
}


public static String printFactoriMethod( int m){
    
  String salida="Factorisation method: ";
  
  switch (m) {
        
        case 0: 
            return(salida + "Split only ");
            
        case 1: 
            return(salida + "Factorise only");    
  
        case 2: 
            return(salida + "Split & Factorise");
            
        case 3:  // without factorisation
            return(salida + "SimpleLazyPeniless!!");
            
    }//switch
  return(salida+"?");  
}

public static String printApproxMethod( int app){
    
  String salida="Approximation method: ";
  
  switch (app) {
        
       case -1: //Exact case
            return(salida + "EXACT factorisation ");
            
        case 0: // Method of average
            return(salida + "average ");
            
        case 1: // Weighted average method 
            return(salida + "Weighted Average method");    
  
        case 2: // Method of minimum Chi^2 divergence
            return(salida + "minimum Chi^2 divergence");
  
        case 3: // Method of minimum mean squared error
            return(salida + "minimum Mean Squared Error");
       
        case 4: // Method of weighted mean squared error
            return(salida + "Weighted Mean Squared Error");
            
        case 5: // Method of null Kullback-Leibler divergence
            return(salida + "minimum Kullback-Leibler divergence");
           
        case 6: // Weight preserving method
            return(salida + "Weight Preserving method");
            
        case 7: // Hellinger method 
            return(salida + "minimum Hellinger divergence");    
    }//switch
  return(salida+"?");  
}


public static String printDivergenceMethod( int div){
    
  String salida="Divergence method: ";
  
  switch (div) {
      
        case -2: // No method for exact factorisation
            return(salida + " None, EXACT factorisation ");
        
        case -1: // No method
            return(salida + " None, only distance between nodes ");
  
        case 1: // Chi^2 method 
            return(salida + "Chi^2 method");
            
        case 2: //  Chi^2 norm
            return(salida + "Chi^2 normalised");
  
        case 3: // Method of minimum mean squared error MSE
            return(salida + "Mean Squared Error");
        
        case 4: // Method of weighted mean squared error WMSE
            return(salida + "Weighted Mean Squared Error");
            
        case 5: // Method of null Kullback-Leibler divergence
            return(salida + "Kullback-Leibler ");
           
        case 6: // Max Absolute Difference MAD
            return(salida + "Max Absolute Difference");
            
        case 7: // Hellinger method 
            return(salida + "Hellinger divergence"); 
    }//switch
  return(salida+ "?");  
}



/**
 * This method computes some statistics about the errors/sizes: It will return a vector
 * in which there will be: 
 *  Position 0: The mean, 
 *  Position 1: The standard deviation,
 *  Position 2: The maximum, and 
 *  Position 3: The minimum.
 *
 * @param data A vector of Double types. 
 *
 * @return A vector containing the statisticas about the errors/sizes 
 * ( or null if vector @param data is empty )
 *
 */

public static Vector vectorStatistics(Vector data){

  int i;
  double x, mean = 0.0, sd = 0.0, max = 0.0, min;
  Vector stat = new Vector();

  if (data.size() < 1){
    System.out.println("No errors/sizes to compute statistics about");
    return(null);
  }

  min = ((Double)data.elementAt(0)).doubleValue();

  for (i=0 ; i<data.size() ; i++){

    x = ((Double)data.elementAt(i)).doubleValue();
    mean = mean +x;
    sd = sd + (x*x);
    max = Math.max(max,x);
    min = Math.min(min,x);
  }

  mean = mean/data.size();
  
  sd = sd/data.size();
  sd = sd - (mean*mean);
  
  stat.addElement(new Double(mean));
  stat.addElement(new Double(sd));
  stat.addElement(new Double(max));
  stat.addElement(new Double(min));

  return stat;

}

/**
 * Prints the mean, standard deviation, max and min values
 * loaded in the vector @param statVec.
 */
public static void printStatistics(Vector statVec){
    
    System.out.println("Mean: "+((Double)statVec.elementAt(0)).doubleValue());
    System.out.println("Standard Deviation: "+((Double)statVec.elementAt(1)).doubleValue());
    System.out.println("Max: "+((Double)statVec.elementAt(2)).doubleValue());
    System.out.println("Min: "+((Double)statVec.elementAt(3)).doubleValue());
}

/**
 * @return one of the pair of vectors used for save values in factorisation.
 * If @param v is 0 return @param vecPotSizes, and @param vecDistApproxim in 
 * other case.
 */
public Vector getClassStatistic(int v){
    
    if (v==0) //sizes of potentials
      return vectorStatistics(getVecOfSizes());
    else
      return vectorStatistics(getVecOfDistA());   
}

/*
 * ..........................  Approximation Methods  ..........................
 *
 *     Several methods for computing the proportionality factor, alpha,
 *     some of them, under the restriction of minimising different measures
 *     of divergence, and others in such way that they could ensure that the
 *     weight of the original and the approximate tree coincide.
 *
 *     The null values for Pi and Qi are considered independiently in each
 *     method, been allowed in some of them and discarded in others.
*/

/**
 *  Method of minimum Chi^2 divergence: obtains an alpha that minimise
 *  this measure. 
 *
 *  @return false if alpha is rejected by the divergence method (not factorise),
 *          true in other case.
 */

public boolean appMinChi2Diverg(Vector vProbs, Vector vAlphaI, double alpha[]){

  int i,j;
  double coeficK,sumPi,sumPi2divQi,Pi2, Qi;
  Double Alphai,valueI, valueJ;
        
  sumPi2divQi=0.0;   
  sumPi=0.0;
  
  for ( i=0,j=0; i< vAlphaI.size(); i++, j=j+2){
     Alphai = ((Double) vAlphaI.elementAt(i)) ; 
     
     if (Alphai.doubleValue() != -1.0){  //skip the null values 
         
        valueI= (Double) vProbs.elementAt(j) ;
        valueJ= (Double) vProbs.elementAt(j+1) ;
        Qi= valueJ.doubleValue(); 
        if(Qi!=0.0) {      //skip Pi=0  
          Pi2= valueI.doubleValue() * valueI.doubleValue();
          sumPi += valueI.doubleValue(); 
          sumPi2divQi += Pi2/Qi;  
        }
     }
  }//for 
      
  if(sumPi2divQi==0.0)  return false;
  
  coeficK = sumPi/sumPi2divQi;
        
  if( ! checkDivergences(vProbs, vAlphaI, coeficK) ) 
     return(false);             
  
  alpha[0]=coeficK;       
  
  return (true); //  (proportional)

}

/**
 *  Method of minimum mean squared error (MSE): obtains an alpha that minimise
 *  this measure. 
 */

public boolean appMinMeanSqError(Vector vProbs, Vector vAlphaI, double alpha[]){

  int i,j;
  double coeficK,Pi2,sumPi2,sumQiPi;
  Double Alphai,valueI, valueJ;
       
  sumQiPi=0.0;   
  sumPi2=0.0;
  
  for ( i=0,j=0; i< vAlphaI.size(); i++, j=j+2){
     Alphai = (Double) vAlphaI.elementAt(i) ; 
     
     if (Alphai.doubleValue() != -1.0){  //skip the null values
        valueI= (Double) vProbs.elementAt(j) ;
        valueJ= (Double) vProbs.elementAt(j+1) ;
        Pi2= valueI.doubleValue() * valueI.doubleValue();
        sumPi2 += Pi2; 
        sumQiPi +=  valueI.doubleValue()*valueJ.doubleValue();       
     }
  }//for 
      
  if(sumPi2==0.0)  return false;
  
  coeficK = sumQiPi/sumPi2;
        
  if( ! checkDivergences(vProbs, vAlphaI, coeficK) ) 
     return(false);             
  
  alpha[0]=coeficK;       
  
  return (true); 

}


/**
 *  Method of weighted mean squared error (WMSE): obtains an alpha that minimise
 *  this measure. 
 */

public boolean appWeightMeanSqError(Vector vProbs, Vector vAlphaI, double alpha[]){

  int i,j;
  double coeficK,Pi2,sumHiPi2,sumHiQiPi, sumQi,wHi;
  Double Alphai,valueI, valueJ;
    
  //Calculate the sum of all the Qi  (needed for the weight Hi)
  sumQi=0.0;
  for ( i=0; i<vProbs.size() ; i=i+2){    
       valueJ= (Double)vProbs.elementAt(i+1);
       sumQi+= valueJ.doubleValue();         
  } 
  
  if(sumQi==0.0) return false;
  
  //Calculate alpha
  sumHiQiPi=0.0;   
  sumHiPi2=0.0;
  
  for ( i=0,j=0; i< vAlphaI.size(); i++, j=j+2){
     Alphai = (Double) vAlphaI.elementAt(i) ; 
     
     if (Alphai.doubleValue() != -1.0){  //skip the null values
        valueI= (Double) vProbs.elementAt(j) ;
        valueJ= (Double) vProbs.elementAt(j+1) ;
        Pi2= valueI.doubleValue() * valueI.doubleValue();
        wHi= valueJ.doubleValue()/sumQi; 
        
        sumHiQiPi += wHi  *  valueI.doubleValue() * valueJ.doubleValue(); 
        sumHiPi2 += wHi*Pi2;        
     }
  }//for 
 
  if(sumHiPi2==0.0) return false;
  
  coeficK = sumHiQiPi/sumHiPi2;
        
 if( ! checkDivergences(vProbs, vAlphaI, coeficK) ) 
     return(false);             
  
  alpha[0]=coeficK;     
  
  return (true); 
}


/**
 * Method of null Kullback-Leibler divergence (KL): 
 * obtains an alpha that minimise this measure. 
 */

public boolean appNullKLDiverg(Vector vProbs, Vector vAlphaI, double alpha[]){

  int i,j;
  double coeficK,Qi,Pi,sumQi,sumQilogAlphai,QilogAlphai;
  Double Alphai,valueJ, valueI;
       
  sumQilogAlphai=0.0;   
  sumQi=0.0;
  
  for ( i=0,j=0; i< vAlphaI.size(); i++, j=j+2){
     Alphai = (Double) vAlphaI.elementAt(i) ; 
     
     if (Alphai.doubleValue() != -1.0){  //skip the null values
        valueI= (Double) vProbs.elementAt(j) ;  
        valueJ= (Double) vProbs.elementAt(j+1) ;
        Pi= valueI.doubleValue();
        if(Pi!=0.0){ //skip Pi=0
            Qi= valueJ.doubleValue();
            sumQi = sumQi + Qi;     
            QilogAlphai= Qi * Math.log( Alphai.doubleValue() );
            sumQilogAlphai += QilogAlphai;          
        }
     }
        
  }//for 
      
  if(sumQi==0.0) return false;
  
  coeficK = Math.pow(2.0, sumQilogAlphai/sumQi);
        
  if( ! checkDivergences(vProbs, vAlphaI, coeficK) ) 
     return(false);             
  
  alpha[0]=coeficK;   
  
  return (true); 
}


/**
 *  Weight preserving method  (WP): obtain an alpha such that the weigth
 *  of the original and the approximate trees coincide.
 */

public boolean appWeightPreserve(Vector vProbs, Vector vAlphaI, double alpha[]){

  int i;
  double coeficK,pI,qI, sumPi,sumQi;
  Double Alphai,valueI,valueJ;

 
  sumQi=0.0;  
  sumPi=0.0;
  
  for ( i=0; i<vProbs.size() ; i=i+2){     
     valueI= (Double) vProbs.elementAt(i) ;    
     sumPi += valueI.doubleValue();    
     
     valueJ= (Double)vProbs.elementAt(i+1);
     sumQi+= valueJ.doubleValue();   
  } 
      
  if(sumPi==0.0) return false;
  
  coeficK = sumQi/sumPi;
   
  if( ! checkDivergences(vProbs, vAlphaI, coeficK) ) 
     return(false);             
  
  alpha[0]=coeficK;      
  
  return (true); 
}

/**
 *  Weighted average method (WA): computes alpha as a weighted average of
 *  the the ratios between the leaves of both trees.
 */

public boolean appWeightedAverage(Vector vProbs, Vector vAlphaI, double alpha[]){

  int i,j;
  double coeficK,sumHiAlphai, sumQi,wHi;
  Double Alphai, valueJ, valueI;
  
   
  //Calculate the sum of all the Qi  (needed for the weight Hi)
  sumQi=0.0;
  for ( i=0; i<vProbs.size() ; i=i+2){    
       valueJ= (Double)vProbs.elementAt(i+1);
       sumQi+= valueJ.doubleValue();         
  } 
  if(sumQi==0.0) return false;
 
  //Calculate alpha
  sumHiAlphai=0.0;   
  
  for ( i=0,j=0; i< vAlphaI.size(); i++, j=j+2){
     Alphai = (Double) vAlphaI.elementAt(i) ; 
     
     if (Alphai.doubleValue() != -1.0){  //skip the null values
        valueI= (Double) vProbs.elementAt(j) ; 
        valueJ= (Double) vProbs.elementAt(j+1) ;
        wHi= valueJ.doubleValue()/sumQi; 
        if (valueI.doubleValue()!=0.0) //skip Pi=0
           sumHiAlphai += wHi  *  Alphai.doubleValue(); 
     }
  }//for 
      
  coeficK = sumHiAlphai;
        
  if( ! checkDivergences(vProbs, vAlphaI, coeficK) ) 
     return(false);             
  
  alpha[0]=coeficK;      
  
  return (true); 
}

/**
 *  Hellinger method 
 */

public boolean appHellinger(Vector vProbs, Vector vAlphaI, double alpha[]){

  int i,j;
  double coeficK,pI,sumPi,sumPi_sqrtAlphai;
  Double Alphai,valueI;
        
  sumPi_sqrtAlphai=0.0;   
  sumPi=0.0;
  
  for ( i=0,j=0; i< vAlphaI.size(); i++, j=j+2){
     Alphai = ((Double) vAlphaI.elementAt(i)) ; 
     
     if (Alphai.doubleValue() != -1.0){  //skip the null values
        valueI= (Double) vProbs.elementAt(j) ;
        if (valueI.doubleValue()!=0.0){ //skip Pi=0
          sumPi = sumPi + valueI.doubleValue(); 
          sumPi_sqrtAlphai += valueI.doubleValue()*Math.sqrt(Alphai.doubleValue()); 
        }
     }
  }//for 
      
  coeficK = Math.pow( sumPi_sqrtAlphai/sumPi, 2 );
        
  if( ! checkDivergences(vProbs, vAlphaI, coeficK) ) 
     return(false);             
  
  alpha[0]=coeficK;     
  
  return (true); 
}

/**
 *   average method : alpha is the mean of all the ratios qi/pi.
 */

public boolean appAverage(Vector vProbs, Vector vAlphaI, double alpha[]){

  int i,j, num=0;
  Double Alphai, valueI;
  double coeficK, sumAlphai;
  
  sumAlphai=0.0;
  
  for (i=0,j=0; i< vAlphaI.size(); i++, j=j+2){
     Alphai = (Double) vAlphaI.elementAt(i) ; 
     
     if (Alphai.doubleValue() != -1.0){  //skip the null values
        valueI= (Double) vProbs.elementAt(j) ;
        if (valueI.doubleValue()!=0.0){
          sumAlphai +=  Alphai.doubleValue();
          num++;
        }
     }
  }//for    
    
  if(num==0.0) return false;
  
  coeficK = sumAlphai/num;
  
  if( ! checkDivergences(vProbs, vAlphaI, coeficK) ) 
     return(false);             
  
  alpha[0]=coeficK;      
  
  return (true); 

}


/*
* ........................  Divergence Methods ................................
*/

/** 
 * Check the divergences between nodes and/or between trees, 
 * depending on the error values selected in the input parameters.
 *  @return <code>false</code>  if one of these divergences is > error 
 *          <code>true</code>  otherwise.
 *
*/

public boolean checkDivergences(Vector vProbs, Vector vAlphaI, double alpha){
    
 if(distanceNodes)
     if( ! checkNodesDivergence(vProbs, vAlphaI, alpha) ) 
     return(false);  
 
 if(distanceTrees)
     if ( !checkTreesDivergence(vProbs, vAlphaI, alpha) ) 
     return(false);
 
 return(true);
}


/** 
 * Divergence between each pair of values p_i (from Tree1) and q_i (from Tree2)
 *  @return <code>false</code>  if one of these divergences is > deltaNode 
 *          <code>true</code>  otherwise.
*/

public boolean checkNodesDivergence(Vector vProbs, Vector vAlphaI, double alpha){

 int i;   
 double Pi,Qi;
 Double valueI,valueJ;
  
 for(i=0; i<vProbs.size() ; i=i+2){       
      valueI= (Double)vProbs.elementAt(i); 
      Pi= valueI.doubleValue();
      valueJ= (Double)vProbs.elementAt(i+1);
      Qi= valueJ.doubleValue();
        
      if( Math.abs( Qi - (alpha*Pi) ) > deltaNode )  
        return(false);             
  }
 return(true);
}

/** 
 * General method for calculate the divergence between two trees.
 *  Depending on the divergence method selected in input parameters (from 1 to 7)
 *  is called one of the them. They return the divergence and 
 *  this is compared with a value  deltaTree (from input parameters).
 *  @return <code>false</code>  if divergence > deltaTree,
 *        <code>true</code>  otherwise.
*/

public boolean checkTreesDivergence(Vector vProbs, Vector vAlphaI, double alpha){

 int i;   
 double divergence=1.;
 Double valueI,valueJ;
 
  
 switch(getDivergenceMethod()){
     
     case 1:{ // Chi^2 
              int num[]= new int[2];
              divergence= divergChi2( vProbs, vAlphaI, alpha, num);
              break;
     }
     case 2:{ // Chi^2 Normalised  
              divergence= divergChi2Normal( vProbs, vAlphaI, alpha);
              break;
     }
     case 3:{ // Mean Squared Error (MSE)
              divergence= divergMeanSqErr( vProbs, vAlphaI, alpha);
              break;
     }
     case 4:{ // Weighted Mean Squared Error (WMSE)
              divergence= divergWeigMeanSqErr( vProbs, vAlphaI, alpha);
              break;
     }
     case 5:{ // Kullback-Leibler (KL)
              divergence= divergKL( vProbs, vAlphaI, alpha);
              break;
     }
     case 6:{ // Maximum Absolute Difference (MAD)
              divergence= divergMaxAbsoluteD( vProbs, vAlphaI, alpha);
              break;
     }    
     case 7:{ // Hellinger  (H)
              divergence= divergHellinger( vProbs, vAlphaI, alpha);
              break;
     }
 }
 

 if( divergence> deltaTree) 
    return(false);             
 
 return(true);
}

/** 
 * Calculates the Chi^2 divergence between two trees.
 */

public double divergChi2(Vector vProbs, Vector vAlphaI, double alpha,int num[]){
    
  int i, count=0;   
  double Pi,Qi,sqDif, sumSqDif_Qi;
  Double valueI,valueJ;
  
  sumSqDif_Qi=0.;
  
  for(i=0; i<vProbs.size() ; i=i+2){       
      valueI= (Double)vProbs.elementAt(i); 
      Pi= valueI.doubleValue();
      valueJ= (Double)vProbs.elementAt(i+1);
      Qi= valueJ.doubleValue();
        
      if(Qi!=0.){
        sqDif= Math.pow( Qi - (alpha*Pi), 2);
        sumSqDif_Qi += sqDif/Qi;
        count++;
      }
  }
  num[0]=count;
  return(sumSqDif_Qi);
}


/** 
 * Calculates the Chi^2 normalised divergence between two trees. 
 * It takes values between 0 and 1.
 */

public double divergChi2Normal(Vector vProbs, Vector vAlphaI, double alpha){
    
  double dChi2, dChi2Norm,Qi;
  int n,i;
  int num[]= new int[2];
  Double Alphai,valueJ;
   
  
  dChi2 = divergChi2(vProbs,vAlphaI,alpha, num);
  
  n= num[0]; // number of alphaI that have been calculated 
   
  dChi2Norm= Math.sqrt(dChi2/(dChi2+n));
  
  return(dChi2Norm);
}


/** 
 * Calculates the Mean Squared Error divergence (MSE) betwwen two trees.
 */

public double divergMeanSqErr(Vector vProbs, Vector vAlphaI, double alpha){

  int i,n;   
  double Pi,Qi,sqDif, sumSqDif;
  Double valueI,valueJ,Alphai;
   
  for(i=0, n=0; i<vAlphaI.size();i++){ // number of alphaI calculated 
     Alphai = (Double) vAlphaI.elementAt(i) ;      
     if (Alphai.doubleValue()!= -1.0) 
        n++;
  }
  
  sumSqDif=0.;
  
  for(i=0; i<vProbs.size() ; i=i+2){       
      valueI= (Double)vProbs.elementAt(i); 
      Pi= valueI.doubleValue();
      valueJ= (Double)vProbs.elementAt(i+1);
      Qi= valueJ.doubleValue();
      
      sqDif= Math.pow( Qi - (alpha*Pi), 2);
      sumSqDif += sqDif;  
  }
  return(sumSqDif/n);
}


/** 
 * Calculates the Weighted Mean Squared Error divergence (WMSE) between two trees.
 */

public double divergWeigMeanSqErr(Vector vProbs, Vector vAlphaI, double alpha){
    
  int i;   
  double Pi,Qi, sumQi, wHi,difHiAlphaPi, sumWSqDif;
  Double valueI,valueJ;
  
 //Calculate the sum of all the Qi
  sumQi=0.0;
  for ( i=0; i<vProbs.size() ; i=i+2){    
       valueJ= (Double)vProbs.elementAt(i+1);
       sumQi+= valueJ.doubleValue();         
  } 
  
  if(sumQi==0.0) // can not apply this method
      return(1000);
  
  //Calculate divergence
  
  sumWSqDif=0.;
  
  for(i=0; i<vProbs.size() ; i=i+2){       
      valueI= (Double)vProbs.elementAt(i); 
      Pi= valueI.doubleValue();
      valueJ= (Double)vProbs.elementAt(i+1);
      Qi= valueJ.doubleValue();
            
      wHi= Qi/sumQi; 
      difHiAlphaPi= wHi* Math.pow(Qi - (alpha*Pi), 2) ;
      sumWSqDif += difHiAlphaPi;
     
  }
  return(sumWSqDif);
}


/** 
 * Calculates the Kullback-Leibler divergence (KL)  between two trees.
 */

public double divergKL(Vector vProbs, Vector vAlphaI, double alpha){

  int i;   
  double Pi,Qi,sumQiLog, qiLog;
  Double valueI,valueJ;
  
  sumQiLog=0.;
  
  for(i=0; i<vProbs.size() ; i=i+2){       
      valueI= (Double)vProbs.elementAt(i); 
      Pi= valueI.doubleValue();
      valueJ= (Double)vProbs.elementAt(i+1);
      Qi= valueJ.doubleValue();
      
      if(Pi!=0.) { 
        qiLog= Qi * Math.log( Qi/(alpha*Pi) );
        sumQiLog += qiLog;
      }
  }
  return(sumQiLog);
}


/** 
 * Calculates the Maximum Absolute Difference divergence (MAD) between the trees.
 */

public double divergMaxAbsoluteD(Vector vProbs, Vector vAlphaI, double alpha){
 
  int i;   
  double Pi,Qi,absDif, maxAbsDif;
  Double valueI,valueJ;
  
  maxAbsDif=0.;
  
  for(i=0; i<vProbs.size() ; i=i+2){       
      valueI= (Double)vProbs.elementAt(i); 
      Pi= valueI.doubleValue();
      valueJ= (Double)vProbs.elementAt(i+1);
      Qi= valueJ.doubleValue();
        
      absDif= Math.abs( Qi - (alpha*Pi));
      maxAbsDif= Math.max(absDif, maxAbsDif);
      
  }
  return(maxAbsDif);   
}


/** 
 * Hellinger divergence method  (H)
 */

public double divergHellinger(Vector vProbs, Vector vAlphaI, double alpha){
 
  int i;   
  double Pi,Qi,sumRoots2, difR;
  Double valueI,valueJ;
  
  sumRoots2=0.;
  
  for(i=0; i<vProbs.size() ; i=i+2){       
      valueI= (Double)vProbs.elementAt(i); 
      Pi= valueI.doubleValue();
      valueJ= (Double)vProbs.elementAt(i+1);
      Qi= valueJ.doubleValue();
      
      difR= Math.sqrt(Pi) - Math.sqrt(Qi);
      sumRoots2 += Math.pow(difR,2);   
  }    
  
  return( Math.sqrt(sumRoots2) );
}



}// end of class
