/*
 * ClassifierBasedMeasures.java
 *
 * Created on 13 de diciembre de 2004, 12:34
 */

package elvira.learning.classification.supervised.validation;

import elvira.NodeList;
import elvira.Node;
import elvira.learning.classification.ConfusionMatrix;

import java.util.Vector;
import java.io.Serializable;
import elvira.tools.statistics.math.Fmath;
import elvira.tools.statistics.analysis.Stat;


/**
 *  This class allows to store and manage several classifier based measure orders [1].
 *  This classifier based measure orders are stored by a Vector of 
 *  <code>ClassifierMeasureNode</code> object. 
 *
 * Refs: 
 *  [1] A. Cano et al. "Selective Gaussian Naive Bayes Model for Diffuse Large 
 *  B-Cell Lymphoma Classifiation: Some Improvements in Preprocessing and
 *  Variable Elimination" (ECSQARU2005).
 *
 *  @author  andrew
 */
public class ClassifierBasedMeasures implements Serializable{
    
    static final long serialVersionUID = -7820802033756948694L;
    
    /**
     *  An array of Vector where measures[0][i] contains a String with the 
     *  name of the Classifier class that produces this order and measure[1][i] 
     *  contain a Vector of <code>ClassifierMeasureNode</code> object with the 
     *  order.
     */ 
    Vector[] measures= new Vector[2];
    
    /** 
     * Creates a new instance of ClassifierBasedMeasures 
     */
    public ClassifierBasedMeasures() {
        this.measures[0]=new Vector();
        this.measures[1]=new Vector();
    }
    
    /**
     *  Store a specific classifier based order
     */
    public void addOrder(Vector qcms){
        this.measures[0].addElement(new String(((ClassifierMeasureNode)qcms.elementAt(0)).getComment()));
        this.measures[1].addElement(qcms);
    }
    /**
     * Return a classifer based order stores in i-th position.
     */
    public Vector getOrder(int i){
       if (i<this.measures[0].size()){
            return (Vector)((Vector)this.measures[1].elementAt(i)).clone();
/*                       Vector newqcms=new Vector();
            for (int j=0; j<qcms.size(); j++){
                ClassifierMeasureNode qcm=((ClassifierMeasureNode)qcms.elementAt(j)).copy();
                newqcms.addElement(qcm);
            }
*/
       }else
            return null;
    }
    
    /**
     *  Return all orders in a only vector.
     */
    public Vector getAllOrder(){
        Vector cms=new Vector();
        int size=0;
        for (int i=0; i<this.measures[1].size(); i++){
            size+=((Vector)this.measures[1].elementAt(i)).size();
        }

        FilteredNodeList auxNl= new FilteredNodeList(size);
        int cont=0;
        for (int i=0; i<this.measures[1].size(); i++){
            for (int j=0; j<((Vector)this.measures[1].elementAt(i)).size(); j++){
                ClassifierMeasureNode qcm=(ClassifierMeasureNode)((Vector)this.measures[1].elementAt(i)).elementAt(j);
                cms.addElement(qcm);
                auxNl.setFilteredNode(new FilteredNode(qcm.getNodes().elementAt(0),qcm.getConfusionMatrix().getAccuracy()),cont);
                cont++;
            }
        }
        auxNl.sortDescendant();
        
        
        Vector qCMs=new Vector();
        for (int i=0; i<auxNl.getSize(); i++){
            Node node=auxNl.getFilteredNode(i).getNode();
            for (int j=0; j<cms.size(); ){
                ClassifierMeasureNode qcm=(ClassifierMeasureNode)cms.elementAt(j);
                if (qcm.getNodes().elementAt(0).equals(node) && auxNl.getFilteredNode(i).getDistance()==qcm.getConfusionMatrix().getAccuracy()){
                    qCMs.addElement(qcm);
                    cms.remove(j);
                }else if (qcm.getNodes().elementAt(0).equals(node)){
                    cms.remove(j);
                }else
                    j++;
            }
        }
        
        return qCMs;
    }

    /**
     *  Return all orders in a only vector.
     */
    public Vector getAllOrder2(){
        Vector cms=new Vector();
        int size=0;
        for (int i=0; i<this.measures[1].size(); i++){
            size+=((Vector)this.measures[1].elementAt(i)).size();
        }

        FilteredNodeList auxNl= new FilteredNodeList(size);
        int cont=0;
        for (int i=0; i<this.measures[1].size(); i++){
            for (int j=0; j<((Vector)this.measures[1].elementAt(i)).size(); j++){
                ClassifierMeasureNode qcm=(ClassifierMeasureNode)((Vector)this.measures[1].elementAt(i)).elementAt(j);
                cms.addElement(qcm);
                auxNl.setFilteredNode(new FilteredNode(qcm.getNodes().elementAt(0),qcm.getConfusionMatrix().getAccuracy()),cont);
                cont++;
            }
        }
        auxNl.sortDescendant();
        
        
        Vector qCMs=new Vector();
        for (int i=0; i<auxNl.getSize(); i++){
            Node node=auxNl.getFilteredNode(i).getNode();
            for (int j=0; j<cms.size(); ){
                ClassifierMeasureNode qcm=(ClassifierMeasureNode)cms.elementAt(j);
                if (qcm.getNodes().elementAt(0).equals(node) && auxNl.getFilteredNode(i).getDistance()==qcm.getConfusionMatrix().getAccuracy()){
                    qCMs.addElement(qcm);
                    cms.remove(j);
                    break;
                }else
                    j++;
            }
        }
        
        return qCMs;
    }

    /**
     *  Return the order with the name 'name'.
     */
    public Vector getOrder(String name){
        return this.getOrder(this.getIdOrder(name));
    }

    /**
     * Return a Vector of Integer with order of the nodes in 'nodes' in
     * the order with the name 'name'.
     */
    public Vector getNumberOrder(String name, NodeList nodes){
        return this.getNumberOrder(this.getIdOrder(name),nodes);
    }
    
    /**
     * Return a Vector of Integer with order of the nodes in 'nodes' in
     * the order in the i-th position.
     */
    public Vector getNumberOrder(int n, NodeList nodes){
        Vector order=new Vector();
        Vector qcms=(Vector)this.measures[1].elementAt(n);
        for (int i=0; i<qcms.size(); i++){
            ClassifierMeasureNode qcm=(ClassifierMeasureNode)qcms.elementAt(i);
            Node node=qcm.getNodes().elementAt(0);
            int id=nodes.getId(node);
            if (id!=-1)
                order.addElement(new Integer(id));                                       
            else
                return null;
        }
        return order;
    }
    
    /**
     *  This method carries out a projection of this object over a
     *  <code>NodeList</code> nodes.
     */
    public void projection(NodeList nodes){
        for (int i=0; i<this.measures[0].size();){
            Vector qcms=(Vector)this.measures[1].elementAt(i);
            for (int j=0; j<qcms.size();){
                ClassifierMeasureNode qcm=(ClassifierMeasureNode)qcms.elementAt(j);
                if (!qcm.getNodes().kindOfInclusion(nodes).equals("subset"))
                    qcms.remove(j);
                else
                    j++;
            }
            if (qcms.size()==0){
                this.measures[0].remove(i);
                this.measures[1].remove(i);
            }else
                i++;
        }
    }

    
    /**
     *  Return the position of the order named 'name'.
     */
    public int getIdOrder(String name){

        for (int i=0; i<this.measures[0].size(); i++)
            if (name.equals((String)this.measures[0].elementAt(i))){
                return i;
            }
        return -1;
    }

    /**
     *  This method returns if a specific classifier based order is already 
     *  stored in this class.
     */
    public boolean isOrder(String name){
        if (this.getIdOrder(name)==-1)
            return false;
        else
            return true;
    }

    /** 
     *  This method returns a copy of this object.
     */
    public ClassifierBasedMeasures copy(){
        ClassifierBasedMeasures clm=new ClassifierBasedMeasures();
        for (int i=0; i<this.measures[0].size(); i++){
            String name=(String)this.measures[0].elementAt(i);
            Vector qcms=(Vector)this.measures[1].elementAt(i);
            clm.measures[0].addElement(new String(name));
            Vector newqcms=new Vector();
            for (int j=0; j<qcms.size(); j++){
                ClassifierMeasureNode qcm=((ClassifierMeasureNode)qcms.elementAt(j)).copy();
                newqcms.addElement(qcm);
            }
            clm.measures[1].addElement(newqcms);
        }
        return clm;
    }
}
