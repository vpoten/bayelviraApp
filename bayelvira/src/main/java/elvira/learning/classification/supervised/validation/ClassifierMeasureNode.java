/*
 * ClassifierMeasureNode.java
 *
 * Created on 28 de noviembre de 2004, 12:55
 */

package elvira.learning.classification.supervised.validation;


import elvira.NodeList;
import elvira.Node;
import elvira.learning.classification.ConfusionMatrix;
import elvira.tools.statistics.math.Fmath;
import elvira.tools.statistics.analysis.Stat;
import elvira.learning.classification.supervised.mixed.*;

import java.util.Vector;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;


/**
 *  This class is used for the implementation of the Variable Elimination method 
 *  proposed in [1]. 
 *
 *
 *  [1] A. Cano et al. "Selective Gaussian Naive Bayes Model for Diffuse Large 
 *  B-Cell Lymphoma Classifiation: Some Improvements in Preprocessing and
 *  Variable Elimination" (ECSQARU2005).
 *  @author  andrew
 */
public class ClassifierMeasureNode implements Serializable{
    
    static final long serialVersionUID = 7046585327172612505L;
    
    protected String comment=new String();
    
    protected MixedClassifier cl;
    
    /*A node list with the nodes used in the classifier construction.*/
    public NodeList nodes=new NodeList();
    
    /*A threshold for considering unclassified samples*/
    public static double umbral=0.5;
    
    /*An AConsufionMatrix object for storing the evaluation data of the classifier*/
    protected AvancedConfusionMatrix acm;
    
    protected transient int[] classAccuracy=new int[0];
    
    protected transient int[] classPredicted=new int[0];
    
    public int[] typeDistribution=new int[0];
    // 0 -> Gauss
    // -a -> discretizado en a intervalos.
    
    /** Creates a new empty instance of ClassifierMeasureNode */
    public ClassifierMeasureNode() {
        this.classAccuracy=new int[0];
        this.classPredicted=new int[0];
    }
    
   /** 
    *   Creates a new instance of AConfusionMatrix 
    *   @param cm, a <code>AConfusionMatrix</code> object with the evaluation of 
    *   the classifier.
    *   @param nl, a NodeList object with the nodes used in this evaluation
    *   @param cl, the classifier
    */
    public ClassifierMeasureNode(AvancedConfusionMatrix cm, NodeList nl, MixedClassifier cl) {
        this.acm=cm;
        this.nodes=nl.copy();
        this.typeDistribution=new int[nl.size()];
        this.cl=cl;
    }
    
   /** 
    *   Creates a new instance of AConfusionMatrix 
    *   @param qcms, a Vector of ClassifierMeasureNode with the nodes that compose
    *   this new object.
    */
    public ClassifierMeasureNode(AvancedConfusionMatrix cm, Vector qcms, MixedClassifier cl) {
        this.acm=cm;
        this.nodes=new NodeList();
        int tam=0;
        for (int i=0; i<qcms.size(); i++){
            ClassifierMeasureNode q=(ClassifierMeasureNode)qcms.elementAt(i);
            tam+=q.nodes.size();
        }
        this.typeDistribution=new int[tam];

        int cont=0;
        for (int i=0; i<qcms.size(); i++){
            ClassifierMeasureNode q=(ClassifierMeasureNode)qcms.elementAt(i);
            for (int j=0; j<q.nodes.size(); j++){
                this.nodes.insertNode(q.nodes.elementAt(j).copy());
                this.typeDistribution[cont]=q.typeDistribution[j];
                cont++;
            }
        }
        //this.nodes=nl.copy();
        //this.typeDistribution=new int[nl.size()];
        this.cl=cl;
    }

    /**
     *  Return the classifier field
     */
    public MixedClassifier getMClassifier(){
        return this.cl;
    }
    /**
     *  Return the nodes field.
     */
    public NodeList getNodes(){
        return this.nodes;
    }
    
    public AvancedConfusionMatrix getConfusionMatrix(){
        return this.acm;
    }
    /**
     *  Set a new Comment.
     */
    public void setComment(String c){
        this.comment=c;
    }
    
    public String getComment(){
        return this.comment;
    }
    /**
     *  Set the type distribtion for the node in n-th position.
     */ 
    public void setTypeDistribution(int n,int type){
        this.typeDistribution[n]=type;
    }
    
    public int[] getClassAccuracy(){

          this.classAccuracy=new int[this.acm.getCases()];
          for (int i=0; i<this.acm.getCases(); i++){
            int clase=this.acm.getMaxProb(i);
            if (this.acm.getProbab(i)[clase]>=this.umbral){
                if (clase==this.acm.getRealClass(i))
                    this.classAccuracy[i]=1;
                else
                    this.classAccuracy[i]=-1;
            }else
                this.classAccuracy[i]=0;
          }
          return this.classAccuracy;
     }    
    
     public int[] getClassPredicted(){
        
          this.classPredicted=new int[this.acm.getCases()];

          for (int i=0; i<this.acm.getCases(); i++){
            int clase=this.acm.getMaxProb(i);
            if (this.acm.getProbab(i)[clase]>=this.umbral){
                if (clase==0)
                    this.classPredicted[i]=1;
                else
                    this.classPredicted[i]=-1;
            }else
                this.classPredicted[i]=0;
          }
          return this.classPredicted;
     }    

     public int isCover2(ClassifierMeasureNode qcmActual){
         int[] c1=this.getClassAccuracy();
         int[] c2=qcmActual.getClassAccuracy();
         
         boolean cover=true;
         int cont=0;
         for (int i=0; i<c1.length; i++){
            if (c2[i]>c1[i]){
                cont++;
                //if (qcmi.getClassPredicted()[i]==-1)
                //        cont+=1.0;
            }
            //if (qcm.getClassPredicted()[i]==-1)
            //cont++;
            //if (c2[i]==-1 && c1[i]==-1)
         }
         return cont;
     }
     public int isCover2(ClassifierMeasureNode qcmActual, ClassifierMeasureNode qcmi){
         int[] c1=this.getSumClass2(qcmActual);
         int[] c2=qcmi.getClassAccuracy();
         
         boolean cover=true;
         int cont=0;
         for (int i=0; i<c1.length; i++){
            if (c2[i]>c1[i]){
                cont++;
                //if (qcmi.getClassPredicted()[i]==-1)
                //        cont+=1.0;
            }
            //if (qcm.getClassPredicted()[i]==-1)
            //cont++;
            //if (c2[i]==-1 && c1[i]==-1)
         }
         
         return cont;
         
     }

     public boolean isCover(ClassifierMeasureNode qcmActual, ClassifierMeasureNode qcmi){
         int[] c1=this.getSumClass2(qcmActual);
         int[] c2=qcmi.getClassAccuracy();
         
         boolean cover=true;
         int cont=0;
         for (int i=0; i<c1.length; i++){
            if (c2[i]>c1[i]){
                cont++;
                //if (qcm.getClassPredicted()[i]==-1)
                //    return false;//cont++;
            }
            //if (qcm.getClassPredicted()[i]==-1)
            //cont++;
            //if (c2[i]==-1 && c1[i]==-1)
         }
         
         if (cont/(double)this.acm.getCases()>0.02)
             return false;
         else
             return true;
        
     }

     public boolean isCover(ClassifierMeasureNode qcm){
     
         if (this.nodes.getId(qcm.nodes.elementAt(0).getName())!=-1)
             return true;
             
         
         int[] c1=this.getClassAccuracy();
         int[] c2=qcm.getClassAccuracy();
         
         boolean cover=true;
         int cont=0;
         for (int i=0; i<c1.length; i++){
            if (c2[i]>c1[i]){
                cont++;
                //if (qcm.getClassPredicted()[i]==-1)
                //    cont++;
            }
            //if (qcm.getClassPredicted()[i]==-1)
            //cont++;
            //if (c2[i]==-1 && c1[i]==-1)
         }
         if (cont/(double)this.acm.getCases()>0.02)
             return false;
         else
             return true;

     }
     
     public boolean isCover(ClassifierMeasureNode qcm, double umbral){
     
         if (this.nodes.getId(qcm.nodes.elementAt(0).getName())!=-1)
             return true;
             
         
         int[] c1=this.getClassAccuracy();
         int[] c2=qcm.getClassAccuracy();
         
         boolean cover=true;
         int cont=0;
         for (int i=0; i<c1.length; i++){
            if (c2[i]>c1[i]){
                cont++;
                //if (qcm.getClassPredicted()[i]==-1)
                //    cont++;
            }
            //if (qcm.getClassPredicted()[i]==-1)
            //cont++;
            //if (c2[i]==-1 && c1[i]==-1)
         }
         if (cont/(double)this.acm.getCases()>umbral)
             return false;
         else
             return true;

     }

     public double getSumAccuracy2(ClassifierMeasureNode qcm){
        
        double[][] p=new double[this.acm.getCases()][this.acm.getDimension()];
        
        for (int i=0; i<this.acm.getCases(); i++){
            for (int j=0; j<this.acm.getDimension(); j++){
                p[i][j]=this.acm.getProbab(i)[j]*qcm.acm.getProbab(i)[j];
            }
            normalize(p[i]);
        }
        
        
        double cont=0;
        for (int i=0; i<this.acm.getCases(); i++){
            if (this.getIndMax(p[i])==0 && this.acm.getRealClass(i)==0)
                cont++;
            else if (this.getIndMax(p[i])==1 && this.acm.getRealClass(i)==1)
                cont++;
           /* else if (cr[i]==0)
                cont+=0.5;
            */
        }
        
        return cont/this.acm.getCases();
    }    
    private int getIndMax(double[] p){
        double max=-Double.MAX_VALUE;
        int ind=-1;
        for (int i=0; i<p.length; i++)
            if (p[i]>max){
                max=p[i];
                ind=i;
            }
        return ind;
    }
    private void normalize(double[] p){
        double sum=0.0;
        for (int i=0; i<p.length; i++)
            sum+=p[i];

        for (int i=0; i<p.length; i++){
            p[i]/=sum;
/*            if (p[i]==0.0){
                p[i]=0.01;
                normalize(p);
                return;
            }*/
        }            
    }
    public double getSumAccuracy(ClassifierMeasureNode qcm){
        
        if (this.nodes.size()==0)
            return qcm.acm.getAccuracy();
        
        int[] c1=this.getClassPredicted();
        int[] c2=qcm.getClassPredicted();
        int[] cr=new int[c1.length];
        
        for (int i=0; i<c1.length; i++){
            if (c1[i]==1 && c2[i]==1)
                cr[i]=1;
            else if (c1[i]==-1 && c2[i]==-1)
                cr[i]=-1;
            else
                cr[i]=c1[i]+c2[i];
        }
        
        double cont=0;
        for (int i=0; i<cr.length; i++){
            if (cr[i]==1 && this.acm.getRealClass(i)==0)
                cont++;
            else if (cr[i]==-1 && this.acm.getRealClass(i)==1)
                cont++;
            else if (cr[i]==0)
                cont+=0.5;
        }
        
        return cont/(double)cr.length;
    }

    public int[] getSumClass2(ClassifierMeasureNode qcm){
        double[][] p=new double[this.acm.getCases()][this.acm.getDimension()];
        
        for (int i=0; i<this.acm.getCases(); i++){
            for (int j=0; j<this.acm.getDimension(); j++){
                p[i][j]=this.acm.getProbab(i)[j]*qcm.acm.getProbab(i)[j];
            }
            normalize(p[i]);
        }
        
        
        int[] cr=new int[this.acm.getCases()];
        double cont=0;
        for (int i=0; i<this.acm.getCases(); i++){
            try{

            if (p[i][this.getIndMax(p[i])]<this.umbral)
                cr[i]=0;
            else if (this.getIndMax(p[i])==0 && this.acm.getRealClass(i)==0)
                cr[i]=1;//cont++;
            else if (this.getIndMax(p[i])==1 && this.acm.getRealClass(i)==1)
                cr[i]=1;//cont++;
            else 
                cr[i]=-1;
            
            }catch(Exception e){
                    e.printStackTrace();
                    cr[i]=-1;
            }
          
        }
        
        return cr;
    }    

    public int[] getSumClass(ClassifierMeasureNode qcm){
        
        
        int[] c1=this.getClassPredicted();
        int[] c2=qcm.getClassPredicted();
        int[] cr=new int[c1.length];
        
        for (int i=0; i<c1.length; i++){
            if (c1[i]==1 && c2[i]==1 && this.acm.getRealClass(i)==0)
                cr[i]=1;
            else if (c1[i]==-1 && c2[i]==-1 && this.acm.getRealClass(i)==1)
                cr[i]=1;
            else if (c1[i]==1 && c2[i]==0 && this.acm.getRealClass(i)==0)
                cr[i]=1;
            else if (c1[i]==0 && c2[i]==1 && this.acm.getRealClass(i)==0)
                cr[i]=1;
            else if (c1[i]==0 && c2[i]==-1 && this.acm.getRealClass(i)==1)
                cr[i]=1;
            else if (c1[i]==-1 && c2[i]==0 && this.acm.getRealClass(i)==1)
                cr[i]=1;
            else if (c1[i]==1 && c2[i]==-1)
                cr[i]=0;
            else if (c1[i]==-1 && c2[i]==1)
                cr[i]=0;
            else if (c1[i]==0 && c2[i]==0)
                cr[i]=0;
            else
                cr[i]=-1;
        }
        return cr;
    }

    public boolean compare(ClassifierMeasureNode qcm){
        if (qcm.acm.getCases()!=this.acm.getCases())
            return false;
            
        for (int i=0; i<this.acm.getCases(); i++)
            if (this.acm.getRealClass(i)!=qcm.acm.getRealClass(i))
                return false;
        
        return true;
    }
    
    public ClassifierMeasureNode copy(){
        ClassifierMeasureNode qcm=new ClassifierMeasureNode();
        
        qcm.acm=this.acm.copy();
        qcm.umbral=this.umbral;
        qcm.nodes=this.nodes.copy();
        qcm.comment=new String(this.comment);
        //qcm.classAccuracy=(int[])this.classAccuracy.clone();
        //qcm.classPredicted=(int[])this.classPredicted.clone();
        qcm.typeDistribution=(int[])this.typeDistribution.clone();
        return qcm;
    }
    public AvancedConfusionMatrix fusion(ClassifierMeasureNode qcm){
        
        double[][] p=new double[this.acm.getCases()][this.acm.getDimension()];
        
        for (int i=0; i<this.acm.getCases(); i++){
            for (int j=0; j<this.acm.getDimension(); j++){
                p[i][j]=this.acm.getProbab(i)[j]*qcm.acm.getProbab(i)[j];
            }
            normalize(p[i]);
        }
        AvancedConfusionMatrix acm=new AvancedConfusionMatrix(this.acm.getDimension());
        for (int i=0; i<p.length; i++){
            Vector v=new Vector();
           for (int j=0; j<p[i].length; j++)
               v.addElement(new Double(p[i][j]));
            acm.actualize(this.acm.getRealClass(i),v);
        }
        return acm;
    }
    
    public ClassifierMeasureNode combine(ClassifierMeasureNode q){
        Vector v=new Vector();
        v.addElement(this);
        v.addElement(q);
        return new ClassifierMeasureNode(this.fusion(q),v,this.cl);
    }
    public static Vector sortDescendent(Vector qcms){

        Vector qcms2=new Vector();
        
        while (qcms.size()>0){
            int indMax=0;
            ClassifierMeasureNode qMax=(ClassifierMeasureNode)qcms.elementAt(0);
            
            for (int i=1; i<qcms.size(); i++){
                ClassifierMeasureNode q=(ClassifierMeasureNode)qcms.elementAt(i);
                if (q.acm.getAccuracy()>qMax.acm.getAccuracy()){
                    indMax=i;
                    qMax=q;
                }
            }
            qcms2.addElement(qMax);
            qcms.remove(indMax);
        }
        return qcms2;
    }
    
    // Vector de ClassifierMeasureNode
    public static Vector unify(Vector vqcm,int cont){
        if (cont==0)
            return vqcm;
        
        Vector vqcm1=new Vector();
        for (int i=0; i<vqcm.size(); i++)
            vqcm1.addElement(((ClassifierMeasureNode)vqcm.elementAt(i)).copy());
            
            
        double alfa=0.0;
        boolean change=false;
        Vector vqcm2=new Vector();
        
        while(vqcm1.size()>1){
            ClassifierMeasureNode q1=(ClassifierMeasureNode)vqcm1.elementAt(0);
            int indMax=1;
            ClassifierMeasureNode qMax=q1.combine((ClassifierMeasureNode)vqcm1.elementAt(1));
            for (int j=2;j<vqcm1.size(); j++){
                ClassifierMeasureNode q2=(ClassifierMeasureNode)vqcm1.elementAt(j);
                ClassifierMeasureNode q3=q1.combine(q2);
                if (q3.acm.getAccuracy()>qMax.acm.getAccuracy()){
                    qMax=q3;
                    indMax=j;
                }
            }
            //if (qMax.acm.getAccuracy()>q1.acm.getAccuracy() && qMax.isCover(q1,alfa) && qMax.isCover((ClassifierMeasureNode)vqcm1.elementAt(indMax),alfa)){
            //if (qMax.acm.getAccuracy()>q1.acm.getAccuracy() && qMax.isCover(q1,alfa)){
            if (qMax.acm.getAccuracy()>q1.acm.getAccuracy()){
                vqcm2.addElement(q1);
                vqcm2.addElement(vqcm1.elementAt(indMax));

                vqcm1.remove(indMax);
                vqcm2.addElement(qMax);
                change=true;
            //    if (!qMax.isCover(q1,alfa))
            //        vqcm2.addElement(q1);
            }else{
                vqcm2.addElement(q1);
            }
            vqcm1.remove(0);
        }

        if (change){
            
            //vqcm2=ClassifierMeasureNode.clean(vqcm2);
            vqcm2=ClassifierMeasureNode.sortDescendent(vqcm2);
            return unify(vqcm2,cont-1);
            //return vqcm2;
        }else
            return vqcm2;
    }
    public static Vector clean(Vector vqcm2){
            double alfa=0.0;
            int cont1=0;
            while(cont1<vqcm2.size()){
                ClassifierMeasureNode q1=(ClassifierMeasureNode)vqcm2.elementAt(cont1);
                int cont2=cont1+1;;
                while (cont2<vqcm2.size()){
                    if(cont1!=cont2){
                        ClassifierMeasureNode q2=(ClassifierMeasureNode)vqcm2.elementAt(cont2);
                        if (q1.isCover(q2,alfa)){
                            vqcm2.remove(cont2);
                        }else
                            cont2++;
                    }
                }
                cont1++;
            }
            return vqcm2;
    }
}
