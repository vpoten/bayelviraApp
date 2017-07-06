/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elvira.tools;


import elvira.tools.VectorManipulator;
import java.util.Vector;

/**
 *
 * @author rcabanas
 */
public class Distances {
    
    
    /**
     * Computes the euclidean distance between two vectors
     * @param v1
     * @param v2
     * @return
     * @throws Exception 
     */
    public static double euclidean(Vector<Double> v1, Vector<Double> v2) throws Exception {        
        return VectorManipulator.euclideanNorm(VectorManipulator.subtraction(v1, v2));
        
    } 
    
    
    /**
     * Computes the euclidean distance normalized with the function exponential.
     * It decreases from 1 to 0. Equidistant point have distance 1
     * @param v1
     * @param v2
     * @return
     * @throws Exception 
     */
    public static double euclidean_exp(Vector<Double> v1, Vector<Double> v2) throws Exception{
        return Math.exp(-Math.pow(euclidean(v1, v2),2));
    }
    
     /**
     * Computes the euclidean distance normalized.
     * It decreases from 1 to 0. Equidistant point have distance 1
     * @param v1
     * @param v2
     * @return
     * @throws Exception 
     */
    public static double euclidean_norm(Vector<Double> v1, Vector<Double> v2) throws Exception{
        return 1/(1+euclidean(v1, v2));
    }
    
    
    
    /**
     * Computes the Cosine Distance.
     * It decreases from 1 to 0. Equidistant point have distance 1.
     * D(k*v, v) = D(v,v) = 1 where k is a scalar value and v a vector.
     * @param v1
     * @param v2
     * @return
     * @throws Exception 
     */
    public static double cosine(Vector<Double> v1, Vector<Double> v2) throws Exception{
        
        
  //      VectorManipulator.print(v1);
 //       VectorManipulator.print(v2);
        
        double euNorm1 = VectorManipulator.euclideanNorm(v1);
        double euNorm2 = VectorManipulator.euclideanNorm(v2);
        
        
        
        double numer = VectorManipulator.multiply(v1, v2);
        double denom = euNorm1 * euNorm2;
        
        
        // if one of the vector is [0,0,...0], and the other it is not
        //they have the maximun distance 0
        if(denom==0) {
            if(euNorm1==0 && euNorm2==0)
                return 1;
            else
                return 0;
        }
        
        
        
        
        return numer/denom;
        
    }
    
        /**
     * Computes the extended Jaccard distance.
     * It decreases from 1 to 0. Equidistant point have distance 1.
     * @param v1
     * @param v2
     * @return
     * @throws Exception 
     */
    public static double ext_jaccard(Vector<Double> v1, Vector<Double> v2) throws Exception{
        
        
        double norm1 = VectorManipulator.norm(v1);
        double norm2 = VectorManipulator.norm(v2);
        
        
        
        
        double numer = VectorManipulator.multiply(v1, v2);
        double denom = Math.pow(norm1, 2) *  Math.pow(norm2,2) - numer;
        

        if(denom==0) {
            if(norm1==0 && norm2==0)
                return 1;
            else
                return 0;
        }
        
        return numer/denom;
        
    }
    
    
    
    /**
     * Computes the distance between an original vector v2 and its aproximation v1
     * @param v1 aproximation
     * @param v2 original vector
     * @return
     * @throws Exception 
     */
    public static double kullbackLeibler(Vector<Double> v1, Vector<Double> v2) throws Exception{
  
        
         if(v1.size() != v2.size())
            throw new Exception("Vector of different sizes");
     
        
        double dist = 0;
        for(int i=0; i<v1.size(); i++) {
            dist+= v1.get(i)*Math.log(v1.get(i)/v2.get(i));
            
            //System.out.println("d("+v1.get(i)+","+v2.get(i)+") = "+v1.get(i)*Math.log(v1.get(i)/v2.get(i))+"\t"+dist);
        
        }
        
        
        return dist;

    }

    /**
     * Computes the relative error between an original vector v2 and its aproximation v1
     * @param v1 aproximation
     * @param v2 original vector
     * @return
     * @throws Exception 
     */
    public static double distanciaRelativa(Vector<Double> v1, Vector<Double> v2) throws Exception{
  
        
         if(v1.size() != v2.size())
            throw new Exception("Vector of different sizes");
     
        
        double dist = 0;
        for(int i=0; i<v1.size(); i++) {
            dist+= Math.abs((v1.get(i) - v2.get(i))/v2.get(i));
            
            //System.out.println("d("+v1.get(i)+","+v2.get(i)+") = "+v1.get(i)*Math.log(v1.get(i)/v2.get(i))+"\t"+dist);
        
        }
        
        
        return dist;

    }
    
    
    
        /**
     * Computes the relative error between an original vector v2 and its aproximation v1
     * @param v1 aproximation
     * @param v2 original vector
     * @return
     * @throws Exception 
     */
    public static double distanciaRelativa2(Vector<Double> v1, Vector<Double> v2) throws Exception{
  
        
         if(v1.size() != v2.size())
            throw new Exception("Vector of different sizes");
     
        
        double dist = 0;
        for(int i=0; i<v1.size(); i++) {
            if((v2.get(i) + v2.get(i))==0)
                dist+=0;
            else
                dist+= Math.abs((v1.get(i) - v2.get(i))/(v2.get(i) + v2.get(i)));
            
            //System.out.println("d("+v1.get(i)+","+v2.get(i)+") = "+v1.get(i)*Math.log(v1.get(i)/v2.get(i))+"\t"+dist);
        
        }
        
        
        return dist;

    }
    
    /**
     * Computes the C-index of two vectors
     * @param v1
     * @param v2
     * @return 
     */
    public static double Cindex(Vector<Double> v1, Vector<Double> v2) {
        
        //System.out.println("Computing Cindex");
        double c;
        int n = VectorManipulator.numPairs(v1.size()) + VectorManipulator.numPairs(v2.size());
        
        double S = VectorManipulator.sumDist(v1) + VectorManipulator.sumDist(v2);
        double[] Smaxmin = VectorManipulator.maxminSumDist(VectorManipulator.concat(v1, v2), n);
        double Smax = Smaxmin[0];
        double Smin = Smaxmin[1];
        
        
        
        c = (S - Smin) / (Smax - Smin);
        //System.out.println("Cindex computed");
        
        return c;
        
    }

    
    
}
