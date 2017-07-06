/*
 * AvancedConfusionMatrix.java
 *
 * Created on 9 de noviembre de 2004, 11:42
 */

package elvira.learning.classification.supervised.validation;

import elvira.*;
import elvira.database.DataBaseCases;
import elvira.potential.*;
import elvira.inference.elimination.VariableElimination;
import elvira.learning.classification.supervised.mixed.*;
import elvira.learning.classification.ConfusionMatrix;
import elvira.tools.statistics.math.Fmath;
import elvira.tools.statistics.analysis.Stat;

import java.util.Vector;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.*;

/**
 * This class inherits from ConfusionMatrix class. 
 * This class adds some functionalities to its parent class.
 * It is possible to manage classification threshold and to obtain
 * the mean of the minor of logarithm of the likelihood  and the confidence
 * intervals for this measure and for the accuracy.
 *
 * @author  andrew
 */
public class AvancedConfusionMatrix extends ConfusionMatrix{
    
    static final long serialVersionUID = 1181440653426492322L;
    

    //double loglikelihood=0.0;
    //double loglikelihoodVariance=0.0;
    //private double[] logLLExpanded;
    //double threshold=-1.0;
    //private double[] unclassified;
    //private double[] unclassifiedVariances;
    
    //public Vector probAcum=new Vector();    
    
    //probab(i) contains the probabilities estimated by the classifier
    //for each class and for the case number i.
    private Vector probab=new Vector();
    
    //private float[][] probabMatrix;
    
    //realClass[i] contain the real class for the case number i.
    private Vector realClasses= new Vector();
    
    //private int[] realClassesMatrix;
    
    //This field defines if this AvancedConfusionMatrix object has to be obtained
    //by a average process.
    private boolean composed=false;
    
    
    //If the field 'composed' is true, size[i] contains the number of cases
    //of the ConfusionMatrix object number i used in the average process.
    private int[] size=null; 
    
    
    /** Creates a new instance of AvancedConfusionMatrix */
    public AvancedConfusionMatrix() {
        super();
    }
    
    /** Creates a new instance of AvancedConfusionMatrix 
     *  @param classVariable, a FiniteStates node that is the target 
     *  variable of the classification problem.
     */
    public AvancedConfusionMatrix(FiniteStates classVariable) {
        super(classVariable);
/*        this.logLLExpanded=new double[this.dimension];
        for(int i= 0; i< this.dimension; i++)
            this.logLLExpanded[i]=0.0;
*/
/*        this.unclassified=new double[this.dimension];
        this.unclassifiedVariances=new double[this.dimension];
        for(int i= 0; i< this.dimension; i++){
            unclassified[i]=0.0;
            unclassifiedVariances[i]=0.0;
        }*/
    }

    /** Creates a new instance of AvancedConfusionMatrix 
     *  @param classNumber, the number of states of the class variable.
     */
    public AvancedConfusionMatrix(int classNumber) {
        super(classNumber);

  /*      this.logLLExpanded=new double[this.dimension];
        for(int i= 0; i< this.dimension; i++)
            this.logLLExpanded[i]=0.0;
   */
/*
        this.unclassified=new double[this.dimension];
        this.unclassifiedVariances=new double[this.dimension];
        for(int i= 0; i< this.dimension; i++){
            unclassified[i]=0.0;
            unclassifiedVariances[i]=0.0;
        }
  */  
    }

    
    /** Add a new cases in the ConfusionMatrix.
     *  @param int realClass. The identifier of the real class of the instance
     *  @param Vector probabilities. A Vector with the estimated probabilities 
     *  for each state of the class variable by the classifier.
     */

    public void actualize(int realClass, Vector probabilities) {
        //super.actualize(realClass,probabilities);

        int assignedClass=-1;
        double max=-1.0;
        for (int i=0; i<probabilities.size();i++){
            double tmp=((Double)probabilities.elementAt(i)).doubleValue();
            if (((Double)probabilities.elementAt(i)).isNaN()){
                //System.out.println("Problem: NaN in Actualize ConfusionMatrix: ");
                tmp=1/(double)this.dimension;
            }
            if (tmp>max){// && tmp>this.threshold){
             max=tmp;
             assignedClass=i;
            }
        }

        //if (assignedClass!=-1)
            this.confusionMatrix[realClass][assignedClass] ++;
        //else
        //    this.unclassified[realClass]++;
        this.cases++;
        double tmp=-Math.log(((Double)probabilities.elementAt(realClass)).doubleValue());

        //this.loglikelihood=(this.loglikelihood*(this.cases-1)+tmp)/this.cases;

        int casesReal=0;
        for (int i=0; i<this.dimension; i++)
            casesReal+=this.confusionMatrix[realClass][i];

        //this.logLLExpanded[realClass]=(this.logLLExpanded[realClass]*(casesReal-1)+tmp)/casesReal;

        //this.probAcum.add(probabilities);
        
        
        
        
        
        
        
        
        float[] pr=new float[probabilities.size()];
        for (int i=0; i<pr.length; i++)
            pr[i]=((Double)probabilities.elementAt(i)).floatValue();
            
        this.probab.add(pr);
        this.realClasses.add(new Integer(realClass));
    }
    
    /** Add a new cases in the ConfusionMatrix.
     *  @param int realClass. The identifier of the real class of the instance
     *  @param Vector probabilities. A Vector with the estimated probabilities 
     *  for each state of the class variable by the classifier.
     */

    public void actualize(int realClass, float[] probabilities) {
        Vector pr=new Vector();
        for (int i=0; i<probabilities.length; i++)
            pr.addElement(new Double(probabilities[i]));
        
        this.actualize(realClass,pr);
        
//        this.probab.add((float[])probabilities.clone());
//        this.realClasses.add(new Integer(realClass));
    }

    
    /**
     *
     * This method computes the error rate of the  Confusion Matrix considering
     * a classification threshold.
     *
     * @return the error rate (mispredicted clases / number of classified cases)
     */

    public double getError(double threshold){
        int cont=0;
        int contError=0;
        for (int i=0; i<this.probab.size(); i++){
            //Vector v=(Vector)this.probab.elementAt(i);
            float[] v=(float[])this.probab.elementAt(i);
            int ind=this.getMaxProb(i);
            //double p=((Double)v.elementAt(ind)).doubleValue();
            float p=v[ind];
            if (p>=threshold){
                cont++;
                if (ind!=((Integer)this.realClasses.elementAt(i)).intValue())
                    contError++;
            }
        }  
        
        return contError/(double)cont;
    }

    /**
     *  This method returns 1 - getError(threshold);
     */
    public double getAccuracy(double threshold){
        return 1-this.getError(threshold);
    }
    
    public double getLikelihood(int n){
        return this.getProbab(n)[this.getRealClass(n)];
    }
    /**
     *  This method returns the state of the class variable
     *  with the highest a posteriori probability in the case
     *  number n. 
     */
    public int getMaxProb(int n){
        double max=-Double.MIN_VALUE;
        int ind=-1;
        //Vector prob=(Vector)this.probab.elementAt(n);
        float[] prob=(float[])this.probab.elementAt(n);
        for (int i=0; i<this.getDimension();i++)
            if (max<prob[i]){
                max=prob[i];
                ind=i;
            }
        if (ind==-1){
            ind=0;
            //System.out.println("Problem in getMaxProb in AvancedConfusionMatrix. Case: "+n);
        }
            
        return ind;
    }
    
    /**
     *  This method returns a confidence interval for the -log likelihood
     *  with a confidence level of 'confidence'*100 per cent.
     */
    public double[] getLogLikelihoodIC(double confidence){

        //if (!this.composed){
        if(true){
            double[] vals=new double[this.probab.size()];
            for (int i=0; i<this.probab.size();i++){
                    //double vero=((Double)((Vector)this.probab.elementAt(i)).elementAt(((Integer)this.realClasses.elementAt(i)).intValue())).doubleValue();
                    double vero=((float[])this.probab.elementAt(i))[((Integer)this.realClasses.elementAt(i)).intValue()];
                    vals[i]=-Math.log(vero);
            }
            return Stat.intervalConfidence(vals,confidence);
        }else{
            int cont=0;
            double[] vals=new double[this.size.length];
            for (int i=0; i<this.size.length; i++){
                double mean=0.0;
                for (int j=0; j<this.size[i]; j++){
                    //double vero=((Double)((Vector)this.probab.elementAt(cont)).elementAt(((Integer)this.realClasses.elementAt(cont)).intValue())).doubleValue();                
                    double vero=((float[])this.probab.elementAt(cont))[((Integer)this.realClasses.elementAt(cont)).intValue()];                    
                    Double vero2=new Double(vero);
                    if (!vero2.isNaN())
                        mean=-Math.log(vero);
                    cont++;
                    
                }
                vals[i]=mean;//this.size[i];
            }
            return Stat.intervalConfidence(vals,confidence);
        }
    }
    
    /**
     *  This method returns a confidence interval for the accuracy
     *  with a confidence level of 'confidence'*100 per cent.
     */
    public double[] getAccuracyIC(double confidence){
        
        //if (!this.composed){
        if (true){
            double[] vals=new double[this.probab.size()];
            for (int i=0; i<this.probab.size();i++){
                int r=((Integer)this.realClasses.elementAt(i)).intValue();    
                if (r==this.getMaxProb(i))
                    vals[i]=1;
                else
                    vals[i]=0;
            }
            return Stat.intervalConfidence(vals,confidence);
        }else{
            int cont=0;
            double[] vals=new double[this.size.length];
            for (int i=0; i<this.size.length; i++){
                double mean=0.0;
                for (int j=0; j<this.size[i]; j++){
                    int r=((Integer)this.realClasses.elementAt(cont)).intValue();    
                    if (r==this.getMaxProb(cont))
                        mean+=1;
                    cont++;
                }
                vals[i]=mean/this.size[i];
            }
            return Stat.intervalConfidence(vals,confidence);
        }
    }

  /**
   * Make the average of a set of confusionMatrix
   * @param Vector vConfusionMatrix. The container of the set of confusionMatrix
   *                                 to make the average
   */
    public void average(Vector vConfusionMatrix) {
        super.average(vConfusionMatrix);
        
        this.composed=true;
        this.size=new int[vConfusionMatrix.size()];
        
        this.probab=new Vector();
        this.realClasses= new Vector();
        for (int i=0; i<vConfusionMatrix.size();i++){
            AvancedConfusionMatrix cm = (AvancedConfusionMatrix)vConfusionMatrix.elementAt(i);
            size[i]=cm.probab.size();
            for (int j=0; j<cm.probab.size();j++){
                this.probab.add(cm.probab.elementAt(j));
                this.realClasses.add(cm.realClasses.elementAt(j));                
            }
        }
    }

    /**
     *  Print the confusion matrix in the command line, same that ConfusionMatrix
     *  method but now it is showed the confidence interval for the accuract and 
     *  the -log likelihood.
     */
    public void print() {
//        super.print();
        System.out.println("Confusion Matrix");
        System.out.print("  real");
        for(int i= 0; i< this.dimension; i++) 
          System.out.print("       " + i);
        System.out.println();
        for(int i= 0; i< this.dimension; i++) 
          System.out.print("--------------");
        System.out.println();

        System.out.println("assigned |");
        for(int j= 0; j< this.dimension; j++) {
          System.out.print(j + "        | ");
          for(int i= 0; i< this.dimension; i++)
            System.out.print(this.getValue(i,j) + "     ");
          System.out.println();
        }

       System.out.println();    

       System.out.println("Accuracy: "+this.getAccuracy());
       System.out.println("LogLikelihood: "+this.getLoglikelihood());//this.loglikelihood);
       System.out.println("LogLikelihood Variance: "+getLoglikelihoodVariance());//this.loglikelihoodVariance);
       System.out.print("LogLikelihood Expanded:");
       for (int i=0;i<this.dimension;i++)
           System.out.print("\t"+this.getLogLikelihoodPartial(i));//logLLExpanded[i]);
       System.out.println();

        double[] vero=this.getLogLikelihoodIC(0.95);
        double[] acu=this.getAccuracyIC(0.95);
        System.out.println("V:"+vero[0]+", "+vero[1]+", "+((vero[1]+vero[0])/2)+", "+((vero[1]-vero[0])/2));
        System.out.println("A:"+acu[0]+", "+acu[1]+", "+((acu[1]+acu[0])/2)+", "+((acu[1]-acu[0])/2));
        //System.out.println("LogLikelihoodNew: "+this.getLoglikelihoodNew());

    double[][][][] data=this.getClassificationDataTotal();


    System.out.println("Confusion Matrix Classification Rate");
    System.out.print("  real");
    for(int i= 0; i< this.dimension; i++) 
      System.out.print("       " + i);
    System.out.println();
    for(int i= 0; i< this.dimension; i++) 
      System.out.print("--------------");
    System.out.println();

    System.out.println("assigned |");
    for(int j= 0; j< this.dimension; j++) {
      System.out.print(j + "        | ");
      for(int i= 0; i< this.dimension; i++){
        double mean=(data[i][j][0][0]+data[i][j][0][1])/2;
        double inc=mean-data[i][j][0][0];

        System.out.print(Fmath.truncate(mean,2)+":"+Fmath.truncate(inc,2)+" ");
      }
      System.out.println();

    }

    }
    

  /**
   * Print the confusion matrix in the command line but it
   * is showed considering unclassified cases.
   * @param double thereshold, the classification threshold.
   */
  public void print(double threshold) {
    System.out.println("Confusion Matrix");
    System.out.print("  real");
    for(int i= 0; i< this.getDimension(); i++) 
      System.out.print("       " + i);
    System.out.println();
    for(int i= 0; i< this.getDimension(); i++) 
      System.out.print("--------------");
    System.out.println();

    System.out.println("assigned |");
    for(int j= 0; j< this.getDimension(); j++) {
      System.out.print(j + "        | ");
      for(int i= 0; i< this.getDimension(); i++)
        System.out.print(this.getValue(i,j,threshold) + "     ");
      System.out.println();
    }


    System.out.print("not Class| ");
    for(int i= 0; i< this.getDimension(); i++)
    System.out.print(this.getUnclassified(i,threshold) + "     ");
    
   System.out.println();    

   System.out.println("Accuracy: "+this.getAccuracy(threshold));
   System.out.println("LogLikelihood: "+this.getLoglikelihood());
   System.out.println("LogLikelihood Variance: "+getLoglikelihoodVariance());//this.loglikelihoodVariance);
   System.out.print("LogLikelihood Expanded:");
   for (int i=0;i<this.getDimension();i++)
       System.out.print("\t"+this.getLogLikelihoodPartial(i));
   System.out.println();

    double[] vero=this.getLogLikelihoodIC(0.95);
    double[] acu=this.getAccuracyIC(0.95);
    System.out.println("V:"+vero[0]+", "+vero[1]+", "+((vero[1]+vero[0])/2)+", "+((vero[1]-vero[0])/2));
    System.out.println("A:"+acu[0]+", "+acu[1]+", "+((acu[1]+acu[0])/2)+", "+((acu[1]-acu[0])/2));
    //System.out.println("LogLikelihoodNew: "+this.getLoglikelihoodNew());

    double[][][][] data=this.getClassificationDataTotal();


    System.out.println("Confusion Matrix Classification Rate");
    System.out.print("  real");
    for(int i= 0; i< this.dimension; i++) 
      System.out.print("       " + i);
    System.out.println();
    for(int i= 0; i< this.dimension; i++) 
      System.out.print("--------------");
    System.out.println();

    System.out.println("assigned |");
    for(int j= 0; j< this.dimension; j++) {
      System.out.print(j + "        | ");
      for(int i= 0; i< this.dimension; i++){
        double mean=(data[i][j][0][0]+data[i][j][0][1])/2;
        double inc=mean-data[i][j][0][0];

        System.out.print(Fmath.truncate(mean,2)+":"+Fmath.truncate(inc,2)+" ");
      }
      System.out.println();
    }
  }


  /**
   *    Return the value in the confusion matrix for the cases
   *    belonging to real class 'realClass', that have been assigned 
   *    to class 'assignedClass' and they have not been unclassified.
   *
   */
  public double getValue(int realClass, int assignedClass, double threshold) {

        int cont=0;
        for (int i=0; i<this.probab.size(); i++){
            if (realClass==((Integer)this.realClasses.elementAt(i)).intValue()){
                //Vector v=(Vector)this.probab.elementAt(i);
                float[] v=(float[])this.probab.elementAt(i);
                int ind=this.getMaxProb(i);
                float p=v[ind];
                if (p>=threshold && ind==assignedClass){
                    cont++;
                }
            }
        }  
        if (this.size==null || this.size.length==0)
            return cont;
        else
            return cont/(double)this.size.length;
  }

 
  /**
   *  Return the number of cases belonging to real class 'realClass' and 
   *  that have been unclassified
   */
  
  public double getUnclassified(int realClass, double threshold){
        int cont=0;
        for (int i=0; i<this.probab.size(); i++){
            if (realClass==((Integer)this.realClasses.elementAt(i)).intValue()){
                //Vector v=(Vector)this.probab.elementAt(i);
                float[] v=(float[])this.probab.elementAt(i);
                int ind=this.getMaxProb(i);
                //double p=((Double)v.elementAt(ind)).doubleValue();
                float p=v[ind];
                if (p<threshold){
                    cont++;
                }
            }
        }  
        if (this.size==null || this.size.length==0)
            return cont;
        else
            return cont/(double)this.size.length;
  }
  
  /**
   *   Return the -log likelihood only associated to class
   *   n-th.
   */
  public double getLogLikelihoodPartial(int n){
  
        double mean=0.0;
        int cont=0;
        for (int i=0; i<this.probab.size();i++){
            if (((Integer)this.realClasses.elementAt(i)).intValue()==n){
                double vero=((float[])this.probab.elementAt(i))[n];            
                mean+=-Math.log(vero);
                cont++;
            }
        }
        
        return mean/cont;
        
      /*
        int cont=0;
        double[] vals=new double[this.size.length];
        for (int i=0; i<this.size.length; i++){
            double mean=0.0;
            for (int j=0; j<this.size[i]; j++){
                //double vero=((Double)((Vector)this.probab.elementAt(cont)).elementAt(((Integer)this.realClasses.elementAt(cont)).intValue())).doubleValue();                
                double vero=((float[])this.probab.elementAt(cont))[((Integer)this.realClasses.elementAt(cont)).intValue()];
                mean=-Math.log(vero);
                cont++;
            }
            vals[i]=mean;
        }
        return vals[n];
*/
  }

  /**
   *  Return a array where a(i) is the assigned probability to class i
   *  in the case n-th.
   */
  public double[] getProbab(int n){
      double[] probab=new double[this.getDimension()];
      for (int i=0; i<this.getDimension(); i++)
        //probab[i]=((Double)((Vector)this.probab.elementAt(n)).elementAt(i)).doubleValue();
          probab[i]=((float[])this.probab.elementAt(n))[i];
      
      return probab;
  }

  /**
   * Return the real class of the case n-th.
   */
  public int getRealClass(int n){
    return ((Integer)this.realClasses.elementAt(n)).intValue();
  }

  
  /**
   * Return a valid copy of this object.
   */ 
  public AvancedConfusionMatrix copy(){
    AvancedConfusionMatrix acm=new AvancedConfusionMatrix(this.getDimension());
    //acm.probab=(Vector)this.probab.clone();
    //acm.realClasses=(Vector)this.realClasses.clone();
    
    acm.composed=this.composed;
    if (this.size!=null)
        acm.size=(int[])this.size.clone();
  
    for (int i=0; i<this.probab.size(); i++)
        acm.actualize(((Integer)this.realClasses.elementAt(i)).intValue(),(float[])this.probab.elementAt(i));

    return acm;
  }

  public double getLoglikelihoodNew2(){
    //return this.loglikelihood;
    double mean=0.0;
    int cont=0;
    //double LIMIT=0.5;
    double LIMIT=0.5;
    double SUM=1;
    
    double[] classmean=new double[this.dimension];
    int[] contmean=new int[this.dimension];
    for (int i=0; i<this.dimension; i++){
        classmean[i]=0.0;
        contmean[i]=0;
    }
    for (int i=0; i<this.probab.size();i++){
        double vero=((float[])this.probab.elementAt(i))[this.getRealClass(i)];            
        if (vero>LIMIT){
            classmean[this.getRealClass(i)]+=(Math.pow(Math.E,-1)*vero+(1-Math.pow(Math.E,-1))*1);
            contmean[this.getRealClass(i)]++;
        }else{
            classmean[this.getRealClass(i)]+=SUM;
            contmean[this.getRealClass(i)]++;
        }
    }    
    for (int i=0; i<this.dimension; i++){
        classmean[i]/=contmean[i];
    }

    for (int i=0; i<this.probab.size();i++){
        
        double vero=((float[])this.probab.elementAt(i))[this.getRealClass(i)];            
            double x=classmean[this.getRealClass(i)];
/*            if (x<LIMIT)
                x=(x+SUM)/2;
 */
            double y=vero;
            //double f=x*Math.log(x/y) + (1 - x)*Math.log((1 - x)/(1 - y))-Math.log(y);
            //double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)))-Math.log(y);
            double f=x*Math.log(x/y) + (1 - x)*Math.log((1 - x)/(1 - y));
            //double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)));

            if (vero<0.5)
                f=-Math.log(vero);
            
            if (x==1)
                f=-Math.log(vero);
            mean+=f;
    
            
            cont++;
        
    }

    return mean/cont;
  }
  public int getNumCases(){
      return this.probab.size();
  }
  public double getLoglikelihoodNew2(AvancedConfusionMatrix acm){
    //return this.loglikelihood;
    double mean=0.0;
    int cont=0;

    double LIMIT=0.5;
    double SUM=1;
    //double LIMIT=0.802;
    AvancedConfusionMatrix base=acm;
    
    double[] classmean=new double[base.getDimension()];
    int[] contmean=new int[base.getDimension()];
    for (int i=0; i<base.getDimension(); i++){
        classmean[i]=0.0;
        contmean[i]=0;
    }
    for (int i=0; i<base.getNumCases();i++){
        double vero=base.getProbab(i)[base.getRealClass(i)];            
        if (vero>LIMIT){
            classmean[base.getRealClass(i)]+=(Math.pow(Math.E,-1)*vero+(1-Math.pow(Math.E,-1))*1);
            //classmean[base.getRealClass(i)]+=vero;
            contmean[base.getRealClass(i)]++;
        }else{
            classmean[base.getRealClass(i)]+=SUM;
            contmean[base.getRealClass(i)]++;
        }
    }    
    for (int i=0; i<base.getDimension(); i++){
        classmean[i]/=contmean[i];
    }
    
    for (int i=0; i<this.probab.size();i++){
        if (this.getRealClass(i)!=acm.getRealClass(i))
            System.exit(0);
        double vero=((float[])this.probab.elementAt(i))[this.getRealClass(i)];            
        double x=acm.getProbab(i)[this.getRealClass(i)];

        //if (x<LIMIT)
            x=classmean[this.getRealClass(i)];
            //x=1;
            //x=(classmean[this.getRealClass(i)]+SUM)/2;

        double y=vero;
        double f=x*Math.log(x/y) + (1 - x)*Math.log((1 - x)/(1 - y));//-Math.log(vero);
        //double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)))-Math.log(y);
        //double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)));

        if (vero<0.5)
            f=-Math.log(vero);

        if (x==1)
            f=-Math.log(vero);
        mean+=f;

        
        cont++;
    }

    return mean/cont;
  }
  
  public double getLoglikelihoodNew(AvancedConfusionMatrix acm){
    //return this.loglikelihood;
    double mean=0.0;
    int cont=0;

    double LIMIT=0.5;
    double SUM1=0.85;
    double SUM2=0.15;
    //double[][] meanFixed={{0.99,0.01},{0.01,0.99}};
    //double[][] meanFixed={{0.95,0.05},{0.05,0.95}};    
    //double[][] meanFixed={{0.97,0.03},{0.03,0.97}};
    double[][] meanFixed={{0.75,0.25},{0.05,0.95}};
    //double LIMIT=0.802;
    AvancedConfusionMatrix base=acm;
    
    double[][] classmean=new double[base.getDimension()][base.getDimension()];
    int[] contmean=new int[base.getDimension()];
    for (int i=0; i<base.getDimension(); i++){
        for (int j=0; j<base.getDimension(); j++){
            classmean[i][j]=0.0;
            contmean[i]=0;
        }
    }

    for (int i=0; i<base.getNumCases();i++){
        double vero=base.getProbab(i)[base.getRealClass(i)];        
        int realclass=base.getRealClass(i);
        int maxclass=base.getMaxProb(i);
        double[] probabT=base.getProbab(i);
        if (realclass==maxclass){
            //classmean[realclass][maxclass]+=probabT[maxclass];//(Math.pow(Math.E,-1)*vero+(1-Math.pow(Math.E,-1))*1);
            contmean[realclass]++;
            for (int j=0; j<base.getDimension(); j++)
                if (j!=realclass)
                    classmean[realclass][j]+=(Math.pow(Math.E,-1)*probabT[j]+(1-Math.pow(Math.E,-1))*SUM2);//probabT[j];
                else
                    classmean[realclass][j]+=(Math.pow(Math.E,-1)*probabT[j]+(1-Math.pow(Math.E,-1))*SUM1);
                
        }else{
            for (int j=0; j<base.getDimension(); j++)
                if (j==realclass)
                    classmean[realclass][realclass]+=SUM1;
                else
                    classmean[realclass][j]+=SUM2;
            
            contmean[realclass]++;
        }
    }    
    for (int i=0; i<base.getDimension(); i++){
        for (int j=0; j<base.getDimension(); j++)
            classmean[i][j]/=contmean[i];
    }
    
    for (int i=0; i<this.probab.size();i++){
        if (this.getRealClass(i)!=acm.getRealClass(i))
            System.exit(0);
        double vero=((float[])this.probab.elementAt(i))[this.getRealClass(i)];            
        double x;//=acm.getProbab(i)[this.getRealClass(i)];
        double f=0;
        double y;

        for (int j=0; j<base.getDimension(); j++){
            x=meanFixed[this.getRealClass(i)][j];
            //x=classmean[this.getRealClass(i)][j];
            /*x=acm.getProbab(i)[j];
            if (acm.getRealClass(i)!=acm.getMaxProb(i))
                x=classmean[this.getRealClass(i)][j];
             */
            y=this.getProbab(i)[j];
            if (x==1)
                f+=(-Math.log(y));
            else if (x!=0)
                f+=(x*Math.log(x/y));
            //double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)))-Math.log(y);
            //double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)));
        }
        /*if (vero<0.5)
            f=-Math.log(vero);
*/
        mean+=f;

        cont++;
    }

    return mean/cont;
  }
  
  public double getLoglikelihoodNew(){
    //return this.loglikelihood;
    double mean=0.0;
    int cont=0;

    double LIMIT=0.5;
    double SUM1=0.85;
    double SUM2=0.15;
    //double[][] meanFixed={{0.9,0.1},{0,1}};
    //double[][] meanFixed={{0.93,0.07},{0.1,0.9}};
    //double[][] meanFixed={{0.97,0.03},{0.03,0.97}};
    //double[][] meanFixed={{0.98,0.02},{0.02,0.98}};
    double[][] meanFixed={{0.99,0.01},{0.01,0.99}};
    //double[][] meanFixed={{0.8,0.2},{0.2,0.8}};
    //double LIMIT=0.802;
    AvancedConfusionMatrix base=this;
    
    double[][] classmean=new double[base.getDimension()][base.getDimension()];
    int[] contmean=new int[base.getDimension()];
    for (int i=0; i<base.getDimension(); i++){
        for (int j=0; j<base.getDimension(); j++){
            classmean[i][j]=0.0;
            contmean[i]=0;
        }
    }

    for (int i=0; i<base.getNumCases();i++){
        double vero=base.getProbab(i)[base.getRealClass(i)];        
        int realclass=base.getRealClass(i);
        int maxclass=base.getMaxProb(i);
        double[] probabT=base.getProbab(i);
        if (realclass==maxclass){
            //classmean[realclass][maxclass]+=probabT[maxclass];//(Math.pow(Math.E,-1)*vero+(1-Math.pow(Math.E,-1))*1);
            contmean[realclass]++;
            for (int j=0; j<base.getDimension(); j++)
                if (j!=realclass)
                    classmean[realclass][j]+=(Math.pow(Math.E,-1)*probabT[j]+(1-Math.pow(Math.E,-1))*SUM2);//probabT[j];
                else
                    classmean[realclass][j]+=(Math.pow(Math.E,-1)*probabT[j]+(1-Math.pow(Math.E,-1))*SUM1);
                
        }else{
            for (int j=0; j<base.getDimension(); j++)
                if (j==realclass)
                    classmean[realclass][realclass]+=SUM1;
                else
                    classmean[realclass][j]+=SUM2;
            
            contmean[realclass]++;
        }
    }    
    for (int i=0; i<base.getDimension(); i++){
        for (int j=0; j<base.getDimension(); j++)
            classmean[i][j]/=contmean[i];
    }
    
    for (int i=0; i<this.probab.size();i++){
        double vero=((float[])this.probab.elementAt(i))[this.getRealClass(i)];            
        double x;//=acm.getProbab(i)[this.getRealClass(i)];
        double f=0;
        double y;

        for (int j=0; j<base.getDimension(); j++){
            x=meanFixed[this.getRealClass(i)][j];
            //x=classmean[this.getRealClass(i)][j];
            /*x=acm.getProbab(i)[j];
            if (acm.getRealClass(i)!=acm.getMaxProb(i))
                x=classmean[this.getRealClass(i)][j];
             */
            y=this.getProbab(i)[j];
            if (x==1)
                f+=(-Math.log(y));
            else if (x!=0)
                f+=(x*Math.log(x/y));
            //double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)))-Math.log(y);
            //double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)));
        }
        /*if (vero<0.5)
            f=-Math.log(vero);
*/
        mean+=f;

        cont++;
    }

    return mean/cont;
  }

  public double[] getLoglikelihoodICNew(double confidence){
    //return this.loglikelihood;
    double mean=0.0;
    int cont=0;

    double[] classmean=new double[this.dimension];
    int[] contmean=new int[this.dimension];
    for (int i=0; i<this.dimension; i++){
        classmean[i]=0.0;
        contmean[i]=0;
    }
    for (int i=0; i<this.probab.size();i++){
        double vero=((float[])this.probab.elementAt(i))[this.getRealClass(i)];            
        if (vero>0.5){
            classmean[this.getRealClass(i)]+=vero;
            contmean[this.getRealClass(i)]++;
        }else{
            classmean[this.getRealClass(i)]+=1;
            contmean[this.getRealClass(i)]++;
        }
    }    
    for (int i=0; i<this.dimension; i++){
        classmean[i]/=contmean[i];
    }
    double[] vals=new double[this.probab.size()];
    for (int i=0; i<this.probab.size();i++){
        /*for (int j=0; j<this.dimension; j++){
            double vero=((float[])this.probab.elementAt(i))[j];            
            mean+=-Math.log(vero);
            cont++;
        }*/
        double vero=((float[])this.probab.elementAt(i))[this.getRealClass(i)];            
        //mean+=-Math.log(vero);
        //mean+=2*(-0.25-0.125*Math.log(1-vero)-0.375*Math.log(vero));
        //double a=0.75;
        //double b=0.041667;
        //double f=-(1-a)*Math.log(1-vero)-a*Math.log(vero);
        //mean+=
        //mean+=-b*Math.log(1-vero)-a*Math.log(vero);
        
        if (vero<0.5){
            //double x=(classmean[this.getRealClass(i)]+classmean[1-this.getRealClass(i)])/2;
            double x=classmean[this.getRealClass(i)];
            double y=vero;
            //double f=x*Math.log(x/y) + (1 - x)*Math.log((1 - x)/(1 - y));
            double f=x*Math.abs(Math.log(x/y))+(1 - x)*Math.abs(Math.log((1 - x)/(1 - y)));
            mean+=f;
            vals[i]=f;
        }else
            vals[i]=0.0;
        
        cont++;
        
    }

    return Stat.intervalConfidence(vals,confidence);
    //return mean/cont;
  }
  
  
  public double getLoglikelihood(){
    //return this.loglikelihood;
    double mean=0.0;
    int cont=0;
    for (int i=0; i<this.probab.size();i++){
        /*for (int j=0; j<this.dimension; j++){
            double vero=((float[])this.probab.elementAt(i))[j];            
            mean+=-Math.log(vero);
            cont++;
        }*/
        double vero=((float[])this.probab.elementAt(i))[this.getRealClass(i)];            
        mean+=-Math.log(vero);
        cont++;
        
    }

    return mean/cont;
  }

  public static double getPosterior(MixedClassifier mixedclassifier, int ncase){
      double total=0.0;
      for (int i=0; i<mixedclassifier.getClassVar().getNumStates(); i++)
          total+=AvancedConfusionMatrix.getPosterior(mixedclassifier,i,ncase);
      return total;
  }
  public static double getPosterior(MixedClassifier mixedclassifier, int nclass, int ncase){
    
    mixedclassifier.getClassifier();

    
    FiniteStates classVar=(FiniteStates)mixedclassifier.getClassVar().copy();
    Evidence ev;
    VariableElimination prop;
    Vector<Potential> res;
    Vector probabilities=new Vector();
    Potential posteriori;
    Configuration c;

    
    
    NodeList nl=new NodeList();
    nl.insertNode(classVar);
    ContinuousConfiguration conf=new ContinuousConfiguration(nl);
    conf.putValue(classVar, nclass);
    ev = new Evidence(conf);
    //ev.print();
    prop = new VariableElimination(mixedclassifier.getClassifier(), ev);
    nl=mixedclassifier.getClassifier().getNodeList().copy();
    nl.removeNode(classVar);
    prop.setInterest(nl);
    prop.getPosteriorDistributions();
    res = prop.getResults();
    
    Potential total=res.elementAt(0);
    for (int i=1; i<res.size(); i++){
        //pot.elementAt(i).print();
        res.elementAt(i).normalize();
        total=total.combine(res.elementAt(i));
        //total.normalize();
    }
    //total.normalize();
    ContinuousCaseListMem cm=(ContinuousCaseListMem)mixedclassifier.getDataBaseCases().getCaseListMem();
    conf=(ContinuousConfiguration)((ContinuousConfiguration)cm.get(ncase)).copy();
    conf.remove(classVar);
    
    return total.getValue(conf)/classVar.getNumStates();
    
    
    /*
    for(int i=0;i<classVar.getNumStates();i++)ss
    {
        c = new ContinuousConfiguration();
        c.insert((FiniteStates) classVar,i);
        probabilities.add(new Double(posteriori.getValue(c)));
    }
     */
    
  }
  
  public double getLoglikelihood(MixedClassifier mixedclassifier){
    //return this.loglikelihood;
    double mean=0.0;
    int cont=0;
    for (int i=0; i<this.probab.size();i++){
        for (int j=0; j<this.dimension; j++){
            double vero=((float[])this.probab.elementAt(i))[j];            
            mean+=-Math.log(vero)*this.getPosterior(mixedclassifier,i);
            cont++;
        }
    }

    return mean/cont;
  }

  public double getLoglikelihoodVariance(){
//    return this.loglikelihoodVariance;

    double mean=0.0;
    double acum=0.0;
    int cont=0;
    for (int i=0; i<this.probab.size();i++){
        for (int j=0; j<this.dimension; j++){
            double vero=((float[])this.probab.elementAt(i))[j];            
            mean+=-Math.log(vero);
            acum+=(-Math.log(vero))*(-Math.log(vero));
            cont++;
        }
    }

    return acum/cont - (mean/cont)*(mean/cont);
      
  }
  /*public double getThreshold(){
    return this.threshold;
  }*/
/*  public double getUnclassified(int realClass){
   return this.unclassified[realClass];   
  }*/
 /* 
  public double[] getLogLLExpanded(){
    return this.logLLExpanded;
  }
  */

  /**
   * Return the following data:
   * [i][j][0][0,1] --> accuracy medio para la clasificación de la clase i asignada a la case j.
   * [i][j][1][0,1] --> accuracy varianza para la clasificación de la clase i asignada a la case j.
   * [i][j][2][0,1] --> logV medio para la clasificación de la clase i asignada a la case j.
   * [i][j][3][0,1] --> logV varianza para la clasificación de la clase i asignada a la case j.
   */
  public double[][][][] getClassificationDataTotal(){
      
      double[][][][] data=new double[this.dimension][this.dimension][4][2];
      for (int i=0; i<data.length; i++)
          for (int j=0; j<data[i].length; j++){
            for (int k=0; k<data[i][j].length; k++){
              data[i][j][k][0]=0;
              data[i][j][k][1]=0;
            }
          }
      
      double[][][][] dataR=new double[this.dimension][this.dimension][2][this.probab.size()];
      int[][][] contdataR=new int[this.dimension][this.dimension][2];
  
      for (int i=0; i<contdataR.length; i++)
          for (int j=0; j<contdataR[i].length; j++)
            for (int k=0; k<contdataR[i][j].length; k++)
                contdataR[i][j][k]=0;

      for (int i=0; i<this.probab.size(); i++){
          int realclass=this.getRealClass(i);
          int asignedclass=this.getMaxProb(i);
          double[] probabV=this.getProbab(i);
          for (int j=0; j<this.dimension; j++){
              double prob=this.getProbab(i)[j];
              dataR[realclass][j][0][contdataR[realclass][j][0]]=probabV[j];
              contdataR[realclass][j][0]++;

              dataR[realclass][j][1][contdataR[realclass][j][1]]=-Math.log(probabV[j]);
              contdataR[realclass][j][1]++;
          }
      }
      for (int i=0; i<this.dimension; i++){
          for (int j=0; j<this.dimension; j++){
              data[i][j][0]=Stat.intervalConfidence(Stat.adjust(dataR[i][j][0],contdataR[i][j][0]),0.95);
              data[i][j][1][0]=Stat.variance(Stat.adjust(dataR[i][j][0],contdataR[i][j][0]));

              data[i][j][2]=Stat.intervalConfidence(Stat.adjust(dataR[i][j][1],contdataR[i][j][1]),0.95);
              data[i][j][3][0]=Stat.variance(Stat.adjust(dataR[i][j][1],contdataR[i][j][1]));
          }
      }

      return data;
  }
  
  
  
  /**
   * Return the following data:
   * [i][0][0][0,1] --> accuracy medio para la clasificación correcta de la clase i.
   * [i][1][0][0,1] --> accuracy varianza para la clasificación correcta de la clase i.
   * [i][2][0][0,1] --> logV medio para la clasificación correcta de la clase i.
   * [i][3][0][0,1] --> logV varianza para la clasificación correcta de la clase i.

   * [i][0][1][0,1] --> accuracy medio para la clasificación incorrecta de la clase i.
   * [i][1][1][0,1] --> accuracy varianza para la clasificación incorrecta de la clase i.
   * [i][2][1][0,1] --> logV medio para la clasificación incorrecta de la clase i.
   * [i][3][1][0,1] --> logV varianza para la clasificación incorrecta de la clase i.
   */
  public double[][][][] getClassificationData(){
      
      double[][][][] data=new double[this.dimension][4][2][2];
      for (int i=0; i<data.length; i++)
          for (int j=0; j<data[i].length; j++){
            for (int k=0; k<data[i][j].length; k++){
              data[i][j][k][0]=0;
              data[i][j][k][1]=0;
            }
          }
      
      double[][][] dataR=new double[4][this.dimension][this.probab.size()];
      int[][] contdataR=new int[4][this.dimension];
      for (int i=0; i<contdataR.length; i++)
          for (int j=0; j<contdataR[i].length; j++){
            contdataR[i][j]=0;
          }      
      for (int i=0; i<this.probab.size(); i++){
          int classe=this.getRealClass(i);
          double prob=this.getProbab(i)[this.getRealClass(i)];
          if (prob>0.5){
              dataR[0][classe][contdataR[0][classe]]=prob;
              dataR[1][classe][contdataR[1][classe]]=-Math.log(prob);
              contdataR[0][classe]++;
              contdataR[1][classe]++;

          }else{
              dataR[2][classe][contdataR[2][classe]]=1-prob;
              dataR[3][classe][contdataR[3][classe]]=-Math.log(prob);
              contdataR[2][classe]++;
              contdataR[3][classe]++;
          }
      }
      for (int i=0; i<this.dimension; i++){
          data[i][0][0]=Stat.intervalConfidence(Stat.adjust(dataR[0][i],contdataR[0][i]),0.95);//Stat.mean(Stat.adjust(dataR[0][i],contdataR[0][i]));
          data[i][1][0][0]=Stat.variance(Stat.adjust(dataR[0][i],contdataR[0][i]));
          data[i][2][0]=Stat.intervalConfidence(Stat.adjust(dataR[1][i],contdataR[1][i]),0.95);//Stat.mean(Stat.adjust(dataR[1][i],contdataR[1][i]));
          data[i][3][0][0]=Stat.variance(Stat.adjust(dataR[1][i],contdataR[1][i]));

          data[i][0][1]=Stat.intervalConfidence(Stat.adjust(dataR[2][i],contdataR[2][i]),0.95);//Stat.mean(Stat.adjust(dataR[2][i],contdataR[2][i]));
          data[i][1][1][0]=Stat.variance(Stat.adjust(dataR[2][i],contdataR[2][i]));
          data[i][2][1]=Stat.intervalConfidence(Stat.adjust(dataR[3][i],contdataR[3][i]),0.95);//Stat.mean(Stat.adjust(dataR[3][i],contdataR[3][i]));
          data[i][3][1][0]=Stat.variance(Stat.adjust(dataR[3][i],contdataR[3][i]));

      }
      return data;
  }
  
  public double getMaxProbability(int n){
      return this.getProbab(n)[this.getMaxProb(n)];
  }

  public double adjustThreshold(AvancedConfusionMatrix train){
      double[][][][] dataTrain=train.getClassificationData();

      double[] umbral = new double[2];
      if (dataTrain[0][0][0][0]>dataTrain[1][0][1][1])
        umbral[0]=(dataTrain[0][0][0][0]+dataTrain[1][0][1][1])/2;
      else
        umbral[0]=(dataTrain[0][0][0][1]+dataTrain[1][0][1][0])/2;  

      if (dataTrain[1][0][0][0]>dataTrain[0][0][1][1])
        umbral[1]=(dataTrain[1][0][0][0]+dataTrain[0][0][1][1])/2;
      else
        umbral[1]=(dataTrain[1][0][0][1]+dataTrain[0][0][1][0])/2;  

      int acierto=0;
      for (int i=0; i<this.probab.size(); i++){
          double vero=this.getMaxProbability(i);//this.getLikelihood(i);
          if (vero>umbral[this.getMaxProb(i)]){
              if (this.getMaxProb(i)==this.getRealClass(i))
                  acierto++;
          }else{ 
              if (this.getMaxProb(i)!=this.getRealClass(i)){
                acierto++;
              }

              double[] f1=this.getProbab(i);
              float[] fp=new float[2];
              fp[0]=(float)(1-f1[0]);
              fp[1]=(float)(1-f1[1]);
              
              this.probab.remove(i);
              this.probab.insertElementAt(fp, i);
          } 
      }
      return acierto/(double)this.probab.size();
  }
  
  
  public static void main(String args[]) throws Exception {
      
    DataBaseCases data=new DataBaseCases(new FileInputStream("d:\\tmp\\iris.dbc"));
    Gaussian_Naive_Bayes gn = new Gaussian_Naive_Bayes(data,false,data.getVariables().size()-1);
    gn.train();
    double pot=AvancedConfusionMatrix.getPosterior(gn,1);
    System.out.println(pot);
    /*
    Potential total=pot.elementAt(0);
    for (int i=0; i<pot.size(); i++){
        //pot.elementAt(i).print();
        total=total.combine(pot.elementAt(i));
    }*/
    //total.normalize();
    //total.getValue()
    //total.print();
  }


}

