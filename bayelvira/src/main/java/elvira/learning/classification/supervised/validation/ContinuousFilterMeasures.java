/*
 * ContinuousFilterMeasures.java
 *
 * Created on 9 de junio de 2004, 11:33
 */

package elvira.learning.classification.supervised.validation;

import elvira.Continuous;
import elvira.NodeList;
import java.io.Serializable;
import java.util.Vector;

/**
 *  This class allows to store and manage several filter measure orders. This filter 
 *  measure orders are stored by an <code>FilteredNodeList</code> object. 
 *
 * Refs: 
 *  [1] A. Cano et al. "Application of Selective Gaussian Naive Bayes Classifier
 *  for Diffuse Large B-Cell Lymphoma Classification" (2004).
 *  @author  andrew
 */
class ContinuousFilterMeasures implements Serializable{
    
    static final long serialVersionUID = 3929802247873187043L;

    /*String objects for the name of a specific filter order*/
    
    /*The ranking is carried out by Anova Measure [1] from higher to lower*/
    public static String ANOVA_MEASUREORDER=new String("anovaOrder");

    public static String ANOVATAN_MEASUREORDER=new String("anovaTANOrder");

    // An array of Vector where v[0][i] contains a String with the name of the filter measure
    // and v[1][i] contain a sorted FilteredNodeList with the variables ant its associated filter measure.
    protected Vector[] filterMeasureOrders= new Vector[2];    
    
    /**
     * Constructor: Create an empty class
     */
    public ContinuousFilterMeasures(){
        filterMeasureOrders[0]=new Vector();
        filterMeasureOrders[1]=new Vector();        
    }
    
    /**
     *  This method returns if a specific measure order is already 
     *  stored in this class.
     */
    public boolean isMeasure(String measure){
        for (int i=0; i<this.getNumMeasures(); i++)
            if (this.getMeasureName(i).equals(measure))
                return true;
        
        return false;
    }
    
    /**
     * This method stores a specific measure in this class.
     * @param measure, a String with the name of the meaure order.
     * @param auxNL, an <code>FilteredNodeList</code> with the order of 
     *  the varibles and its associated masure.
     */
    public void addMeasureOrder(String measure, FilteredNodeList auxNl){
        this.filterMeasureOrders[0].addElement(measure);
        this.filterMeasureOrders[1].addElement(auxNl.copy());
    }
    
    /**
     *  This method returns the measure associated to variable node.
     */
    public double getMeasureNode(String measure, Continuous node){
        return this.getMeasureOrder(measure).getFilteredNode(node).getDistance();
    }
    
    /**
     *  Return the number of measures orders stores in this class.
     */
    public int getNumMeasures(){
        return this.filterMeasureOrders[0].size();
    }
    
   /**
    *   Return a String object with the name of the measure in the position i-th
    */
    public String getMeasureName(int i){
        return (String)this.filterMeasureOrders[0].elementAt(i);
    }
    
    /**
     *  Return an <code>AuxiliarNodeLis</code> with the i-th measure order.
     */
    public FilteredNodeList getMeasureOrder(int i){
        return (FilteredNodeList)this.filterMeasureOrders[1].elementAt(i);
    }
    
    /**
     *  Return an <code>AuxiliarNodeLis</code> with the measure order with 
     * name of parameter measure.
     */
    public FilteredNodeList getMeasureOrder(String measure){
        for (int i=0; i<this.getNumMeasures(); i++)
            if (this.getMeasureName(i).equals(measure))
                return this.getMeasureOrder(i);
        
        return null;
    }

    /**
     * Fix a specific measure order in the position i-th.
     */
    public void setMeasureOrder(int i, FilteredNodeList auxNL){
        this.filterMeasureOrders[1].setElementAt(auxNL,i);
    }

    

    /** 
     *  This method returns a copy of this object.
     */
    public ContinuousFilterMeasures copy(){
     
       ContinuousFilterMeasures cfm = new ContinuousFilterMeasures();
       for (int i=0; i<this.getNumMeasures(); i++)
           cfm.addMeasureOrder(this.getMeasureName(i), this.getMeasureOrder(i));
       return cfm;
    }

    /**
     *  This method carries out a projection of this object over a
     *  <code>NodeList</code> nodes.
     */
    public void projection(NodeList nodes){
            
      for (int k=0; k<this.getNumMeasures(); k++){
        int size=0;
        for (int i=0; i<nodes.size(); i++)
            if (nodes.elementAt(i).getClass()==Continuous.class)
                size++;
        FilteredNodeList auxnl=new FilteredNodeList(size);
        int cont=0;
        for (int i=0; i<this.getMeasureOrder(k).getSize(); i++)
             if (nodes.getId(this.getMeasureOrder(k).getFilteredNode(i).getNode())!=-1){
                 auxnl.setFilteredNode(this.getMeasureOrder(k).getFilteredNode(i),cont);
                 cont++;
             }
        auxnl.sortDescendant();
        this.setMeasureOrder(k,auxnl);
      }
    }
}
