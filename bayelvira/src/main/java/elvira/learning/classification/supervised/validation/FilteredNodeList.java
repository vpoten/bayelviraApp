/*
 * FilteredNodeList.java
 *
 */

package elvira.learning.classification.supervised.validation;


import java.util.Comparator;
import java.util.Arrays;
import java.io.Serializable;

import elvira.database.DataBaseCases;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.CaseListMem;
import elvira.FiniteStates;
import elvira.Configuration;
import elvira.learning.classification.AuxiliarPotentialTable;


 /**
  * This class implements a list of <code>FilteredNode</code> that can be
  * accessed, modified, and sorted.
  * 
  * @since 28/04/04
  * @version 0.2.1
  * @author Armañanzas, R.
  * @author ISG Group - UPV/EHU
 */

 public class FilteredNodeList implements Serializable
  {
    private class FilteredComparator implements Comparator, Serializable 
    {
      public int compare (Object o1, Object o2)
      {
        if ( ((FilteredNode)o1).getDistance() > ((FilteredNode)o2).getDistance() )
          return 1;
        else if ( ((FilteredNode)o1).getDistance() < ((FilteredNode)o2).getDistance() )
          return -1;
        else
          return 0;
      }
    }//end FilteredComparator

    private FilteredNode[] nodeList;
    private FilteredComparator comparator;

    public FilteredNodeList(int capacity)
    {
      nodeList = new FilteredNode[capacity];
      comparator = new FilteredComparator();
    }//end FilteredNodeList(int)
    public FilteredNodeList copy(){
        FilteredNodeList aux=new FilteredNodeList(this.getSize());
        for (int i=0; i<aux.getSize(); i++)
            aux.setFilteredNode(this.getFilteredNode(i).copy(),i);
        return aux;
    }
    
    public void setFilteredNode (FilteredNode node, int pos)
    {
      nodeList[pos]= node;
    }//end setFilteredNode(FilteredNode, int)

    public FilteredNode getFilteredNode(int pos)
    {
      return nodeList[pos];
    }//end getFilteredNode(int)

    public FilteredNode getFilteredNode(Node node)
    {
      for (int i=0; i<this.nodeList.length; i++)
          if (this.getFilteredNode(i).getNode().equals(this.nodeList[i].getNode()))
              return this.getFilteredNode(i);
      return null;
    }//end getFilteredNode(int)

    public int getSize()
    {
      return nodeList.length;
    }//end getSize()
    
    public NodeList getNodeList(){
      NodeList nl=new NodeList();
      for (int i=0; i<this.getSize(); i++)
          nl.insertNode(this.getFilteredNode(i).getNode());
      return nl;
    }

  /**
   * Sorts the nodeList in ascendant way using the distance
   */
    public void sortAscendant()
    {
      Arrays.sort(nodeList, comparator);
    }//end sortAscendant()

  /**
   * Sorts the nodeList in descendant way using the distance
   */
    public void sortDescendant()
    {
      Arrays.sort(nodeList, comparator);

      FilteredNode[] temp = new FilteredNode[nodeList.length];
      for (int i=nodeList.length - 1; i>=0; i--)
        temp[(nodeList.length - 1) - i] = nodeList[i];

      nodeList = temp;
    }//end sortDescendant()

  /**
   * Returns the media distance of the set
   */
   public double getMedia()
   {
     double accumulate = 0;
     for (int i=0; i<nodeList.length; i++)
       accumulate += ((FilteredNode)nodeList[i]).getDistance();
     return (accumulate / nodeList.length);
   }//end getMedia()

  /**
   * Returns the variance of the distances
   */
   public double getVar()
   {
     double accumulate = 0;
     double media = this.getMedia();
     for (int i=0; i<nodeList.length; i++)
       accumulate += Math.pow((((FilteredNode)nodeList[i]).getDistance() - media), 2);
     return (accumulate / nodeList.length);
   }//end getVar()
   
  }//end FilteredNodeList
