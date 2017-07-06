/*
 * FilteredNode.java
 *
 * Created on 1 de julio de 2004, 18:24
 */

package elvira.learning.classification.supervised.validation;



import elvira.database.DataBaseCases;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.CaseListMem;
import elvira.FiniteStates;
import elvira.Configuration;
import elvira.learning.classification.AuxiliarPotentialTable;

import java.io.Serializable;

 /**
  * This class implements an object with two fields, a node, and its metric value
  * on a problem. Provides typically accesories methods for its maintenance.
  *
  * 
  * @since 28/04/04
  * @version 0.2.1
  * @author Armañanzas, R.
  * @author ISG Group - UPV/EHU
  */
  
class FilteredNode implements Serializable
  {
    private Node node;
    private double distance;

    public FilteredNode()
    {
      node = null;
      distance = 0;
    }
    public FilteredNode(Node n, double d)
    {
      node = n;
      distance = d;
    }
    public FilteredNode copy(){
     return new FilteredNode(this.getNode().copy(),this.getDistance());   
    }
    public Node getNode() { return node;}
    public double getDistance() { return distance;}
    public void setNode(Node n) { node = n;}
    public void setDistance(double d) { distance = d;}
}//end FilteredNode
