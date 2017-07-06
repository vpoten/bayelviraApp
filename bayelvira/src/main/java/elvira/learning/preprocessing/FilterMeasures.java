/*FilterMeasures.java*/
package elvira.learning.preprocessing;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import elvira.database.DataBaseCases;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.CaseListMem;
import elvira.FiniteStates;
import elvira.Configuration;
import elvira.learning.classification.AuxiliarPotentialTable;

/**
 * Implements several filter measures methods for discrete data sets.
 * <p>
 * The filter measures is a type of quick preprocessing that computes how much information a variable
 * can apport, in supervised problems, about the class one variable. This type of metric tries to quantify the
 * uncertainty that a variable contributes against another, the class, always from a univariant point of view.
 * <p>
 * Once the ranking is made, the class give the posibility of save a projection data file of the original dataset. It
 * will include the desired number of nodes in its best ordering, and the class one, wich will be the last node of
 * the file.
 *
 * @since 28/04/04
 * @version 0.2.1
 * @author Armañanzas, R.
 * @author ISG Group - UPV/EHU
 *
 */
public final class FilterMeasures
{

/////////////////////////////////////////////////////////////////////////////// internal classes

 /**
  * Internal class that implements a object with two fields, a node, and its metric value
  * on a problem. Provides typically accesories methods for its maintenance.
  */
  public class FilteredNode
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
    public Node getNode() { return node;}
    public double getDistance() { return distance;}
    public void setNode(Node n) { node = n;}
    public void setDistance(double d) { distance = d;}
  }//end FilteredNode

 /**
  * Internal class that implements a list of <code>FilteredNodes</code> that can be
  * accessed, modified, and sorted.
  */
  public class AuxiliarNodeList
  {
    private class FilteredComparator implements Comparator
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

    public AuxiliarNodeList(int capacity)
    {
      nodeList = new FilteredNode[capacity];
      comparator = new FilteredComparator();
    }//end AuxiliarNodeList(int)

    public void setFilteredNode (FilteredNode node, int pos)
    {
      nodeList[pos]= node;
    }//end setFilteredNode(FilteredNode, int)

    public FilteredNode getFilteredNode(int pos)
    {
      return nodeList[pos];
    }//end getFilteredNode(int)

    public int getSize()
    {
      return nodeList.length;
    }//end getSize()

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
   
  }//end AuxiliarNodeList

 /**
  * Internal class used for guiding the search process in the Correlation Feature Selection method.
  * Provides typically accesories methods for its maintenance.
  */
  public class SearchState
  {
    /**
     * List of the actual selected set of nodes in the search process
     */
     private NodeList selectNodes;
     
    /**
     * List of the rest (still unselected) nodes in the search process
     */
     private NodeList restNodes;
     
    /**
     * Value of the heuristic for the actual set of selected nodes
     */
     private double heuristicValue;
    
    /**
     * Value of the mean correlation between the selected nodes and the class one
     */
     private double meanClassInterCorrelation;

    /**
     * Value of the average correlation between the selected nodes
     */
     private double averageNodeInterCorrelation;
    
    /**
     * Main constructor of the class, by the moment the forward search is the
     * only supported search politic
     * @param nodes the nodelist with the search set of attributes
     * @param mode the politic of the search - FORDWARD_SEARCH or BACKWARD_SEARCH
     */
    public SearchState(NodeList nodes, int mode)
    {
      if (mode == FORWARD_SEARCH) //Forward search
      {
        selectNodes = new NodeList();
        
        restNodes = new NodeList();
        for (int i=0; i<nodes.getNodes().size()-1; i++) //The last is the class, not included
          restNodes.insertNode((Node)nodes.getNodes().elementAt(i));

        heuristicValue=0;
        meanClassInterCorrelation=0;
        averageNodeInterCorrelation=0;
      }
      else if (mode == BACKWARD_SEARCH) //Backwards search
      {
        restNodes = new NodeList();
        selectNodes = nodes.duplicate();
        //Compute the values of the set
        heuristicValue= this.recomputeHeuristic();
      }
    }//end searchState(NodeList, int)
    
    /**
     * Recomputes all the values of the actual search state in function of the selected node set. 
     * Not yet implemented.
     * @return the value of the heuristic correspondient with the actual nodes set
     */
     private float recomputeHeuristic() //To be done !
     {
       //Asign the interClass and the interNode correlations
       //  this.setCorrelations(d1, d2)
       return Float.MAX_VALUE;
     }//end recomputeHeuristic()
     
     /**
      * Internal method for updating the values of the search parameters. This is the
      * function that recomputes the heuristic function
      * @param meanIC mean class-correlation of the new formed subset
      * @param averageIC intercorrelation between all the subset attributes
      */
      private void setCorrelations(double meanIC, double averageIC)
      { 
        this.meanClassInterCorrelation = meanIC;
        this.averageNodeInterCorrelation = averageIC;
        //Compute the heuristic's new value
        this.heuristicValue= (selectNodes.size()*meanIC) /
            (Math.pow(selectNodes.size()+(selectNodes.size()*(selectNodes.size()-1)*averageIC), 0.5));
      }//end setCorrelations(float, float)
      
     /**
      * Returns the nodes that have not been added into the optimal subset, that is, those nodes from the
      * original dataset that have not been yet included in the search process.
      * @return a nodelist with the unselected nodes
      */
      public NodeList getRestNodes()
      { return this.restNodes; }
      
     /**
      * Returns the nodes that have been added into the optimal subset, that is, those nodes from the
      * original dataset that have already been included in the search process.
      * @return a nodelist with the search selected nodes
      */
      public NodeList getSelectedNodes()
      { return this.selectNodes; }
      
    /**
     * Adds a node in the selected set of attributes, the new values for the average intercorrelation and
     * the mean class-correlation are needed. The method updates automatically the heuristic value of the 
     * selected subset of attributes
     * @param mIC mean class-correlation of the new formed subset
     * @param avIC average intercorrelation between all the subset attributes
     */
     public void addSelectedNode(Node n, double mIC, double avIC)
     {
       //Delete the Node from the rest node list
       this.restNodes.removeNode(n);
       //Add the Node in the selected list
       this.selectNodes.insertNode(n);
       this.setCorrelations(mIC, avIC);
     }

    /**
     * Returns the value of the heuristic for the actual subset of attributes
     * @return the actual heuristic value
     */
     public double getHeuristic()
     { return this.heuristicValue; }

    /**
     * Returns the mean class correlation between every attribute in the selected subset and
     * the class attribute
     * @return the mean attribute correlation with the class
     */
     public double getMeanClassIC()
     { return this.meanClassInterCorrelation; }

    /**
     * Returns the average intercorrelation between all the attributes in the selected subset
     * @return the average attribute intercorrelation
     */
     public double getAverageNodesIC()
     { return this.averageNodeInterCorrelation; }
  }//end SearchState

 /**
  * Internal class
  */
  public class SearchParameters
  {
    /**
     * Hashtable containing the a priori values of the individual entropy
     */
    private Hashtable nodesEntropies;
    
    /**
     * HashMap of Hashtables with the Mutual Information of each pair of nodes
     */
    private HashMap nodesMI;
    
    /**
     * Constructor 
     */
    public SearchParameters(AuxiliarPotentialTable[] nodePotentials, NodeList nodes, int noCases)
    {
      double numXi; double entropyAux = 0; double probXi = 0; double miAux = 0;
      AuxiliarPotentialTable values;
      Node nodeAux;
      
      //Loop for the entropys' initialization
      nodesEntropies = new Hashtable();
      for (int i=0; i<nodes.size(); i++)
      {
        entropyAux = 0;
        values = (AuxiliarPotentialTable) nodePotentials[i];
        nodeAux = (Node)nodes.getNodes().elementAt(i);
        for (int j=0; j<((FiniteStates)nodeAux).getNumStates(); j++)
        {
          numXi=0;
          for (int k=0; k<values.getNStatesOfParents(); k++) numXi += values.getNumerator(j,k);
          probXi = numXi/noCases;
          if (probXi != 0) 
            entropyAux += probXi * (Math.log(probXi)/Math.log(2));
        }
        //Add the value to its correspondant position
        nodesEntropies.put(nodeAux, new Double(entropyAux*(-1)));
      }
      
      //Loop for the mutual informations' intialization
      nodesMI = new HashMap(nodes.size());
      for (int i=0; i<nodes.size(); i++)
        nodesMI.put((Node)nodes.getNodes().elementAt(i), new Hashtable(nodes.size()));
      for (int i=0; i<nodes.size(); i++)
      {
        for (int j=0; j<i; j++)
        {
          miAux = mutualInformation((Node)nodes.getNodes().elementAt(i), (Node)nodes.getNodes().elementAt(j));                                                    
          
          ((Hashtable)nodesMI.get((Node)nodes.getNodes().elementAt(i))).put((Node)nodes.getNodes().elementAt(j),
                                                                                              new Double(miAux));
          ((Hashtable)nodesMI.get((Node)nodes.getNodes().elementAt(j))).put((Node)nodes.getNodes().elementAt(i),
                                                                                              new Double(miAux));
        }        
        ((Hashtable)nodesMI.get((Node)nodes.getNodes().elementAt(i))).put((Node)nodes.getNodes().elementAt(i),
                                                                                                  new Double(1));
      }
    }//end searchEntropies(//AuxiliarPotentialTable[], Nodelist)
    
    /**
     * getMI javadoc
     */
    public double getMI(Node n1, Node n2)
    {
      return ((Double)((Hashtable)nodesMI.get(n1)).get(n2)).doubleValue();
    }//end getEntropy(Node)

    /**
     * getEntropy javadoc
     */
    public double getEntropy(Node n)
    {
      return ((Double)nodesEntropies.get(n)).doubleValue();
    }//end getConditionalEntropy(Node)
    
    /**
     * getMaxClassCorrelatedNode
     */
    public Node getMaxClassCorrelatedNode(NodeList nodes)
    {
      double aux = this.getMI((Node)nodes.getNodes().elementAt(0), 
                                (Node)nodes.getNodes().elementAt(nodes.size()-1));
      double aux2 = 0;
      Node tempNode = (Node)nodes.getNodes().elementAt(1);
      for (int i=0; i<nodes.size()-1; i++)
      {
          aux2 = this.getMI((Node)nodes.getNodes().elementAt(i), 
                                  (Node)nodes.getNodes().elementAt(nodes.size()-1));
          if ( aux2 > aux )
          {
            aux = aux2;
            tempNode = (Node)nodes.getNodes().elementAt(i);
          }
      }
      return tempNode;
    }//end getMaxClassCorrelatedNode()
  }//end SearchParameters

///////////////////////////////////////////////////////////////////////////// internal classes end

/**
 * Constraint for the forward search politic
 */
    public static int FORWARD_SEARCH  = 0;
/**
 * Constraint for the backward search politic
 */
    public static int BACKWARD_SEARCH = 1;

/**
 * Number of cases of the origianl data set
 */
  private static int nCases;

/**
 * Number of variables of the original data set
 */
  private static int nVariables;

/**
 * Input data to be filtered
 */
  private DataBaseCases data;

/**
 * Array of <code>AuxiliarPotentialTable</code> used for calculating the elements frecuencials
 */
  private AuxiliarPotentialTable[] potentials;

/**
 * Internal structure where the nodes and the respective filter measures will be stored and sorted.
 */
  private AuxiliarNodeList nodesFilter;

/**
 * Main constructor of the class. Initializes all the structures for the subsequent calculus
 *
 * @param cases input data in databasecases format
 *
 */
  public FilterMeasures(DataBaseCases cases)
  {
    data        = cases;
    nVariables  = cases.getVariables().size();
    nCases      = cases.getNumberOfCases();
    nodesFilter = new AuxiliarNodeList(nVariables - 1);

    //Initialization of the auxiliar tables
    potentials = new AuxiliarPotentialTable[nVariables]; 
    for (int i=0; i< nVariables; i++)
    {
      if (((Node)data.getNodeList().elementAt(i)).getTypeOfVariable() != Node.FINITE_STATES) 
        throw new SecurityException("There are continuous values. First, use a Discretization method.");

      potentials[i]=new AuxiliarPotentialTable(((FiniteStates)data.getNodeList().elementAt(i)).getNumStates(),
                                    ((FiniteStates)data.getNodeList().elementAt(nVariables-1)).getNumStates());
    }
    //Values register in the potentials array
    Iterator search;
    search = ((CaseListMem)((Relation)data.getRelationList().get(0)).getValues()).getCases().iterator();
    int[] row = new int[nVariables];
    for (int i=0; i< nCases; i++)
    {
      row = (int[])search.next();
      for (int j=0; j< nVariables; j++)
      {
        ((AuxiliarPotentialTable)potentials[j]).addCase(row[j],row[nVariables-1],1);
      }
    }
    
  }//end FilterMeasures()

/**
 * Implements the main function of the class, it provides support for calculating the metrics and save
 * the dbcs using the console terminal.
 *
 * @param args the executino parameters, specified in the <code>usage</code> method
 *
 */
  public static void main(String[] args)
  {
    DataBaseCases datos;
    FileInputStream f;
    
    if ((args.length < 2 ) || (args.length > 4)) usage();
    else
    {
      int code = new Integer(args[1]).intValue();
      if ((code > 7) || (code < 0)){ usage(); System.exit(0);}

      if (code == 7) //CFS turn !
      {
        try
        {
          f = new FileInputStream(args[0]);
          System.out.print("Loading data ...");
          datos = new DataBaseCases(f);
          System.out.println("loaded !");
  
          FilterMeasures filter = new FilterMeasures(datos);
          filter.executeFilter(code);
          if (args.length == 3)
            filter.saveCFSProyection(new File(args[2]));
          
         }catch(Exception e){ System.out.println("Something went wrong: "+e+"\n .. aborting !"); e.printStackTrace();}       
      }
      else
      {
        try
        {
          f = new FileInputStream(args[0]);
          System.out.print("Loading data ...");
          datos = new DataBaseCases(f);
          System.out.println("loaded !");
  
          FilterMeasures filter = new FilterMeasures(datos);
          filter.executeFilter(code);
  
          if (args.length == 2) 
            { filter.displayNodesFilter();}
              //filter.optimalThreshold(new Integer(args[1]).intValue()); }
          else if (args.length == 3)
          {
            int numVars = new Integer(args[2]).intValue();
            if (numVars == 0) filter.displayNodesFilter(filter.optimalThreshold(code));
            else if ((numVars < nVariables) && (numVars != 0)) filter.displayNodesFilter(numVars);
            else filter.displayNodesFilter();        
          }
          else if (args.length == 4)
          {
            int numVars = new Integer(args[2]).intValue();
            if ((numVars < nVariables) && (numVars != 0))
            { 
              filter.displayNodesFilter(numVars);
              if ((numVars < nVariables-1) && (numVars > 0))
                filter.saveDBCProyection(numVars, new File(args[3]));
            }
            else if (numVars == 0)
            {
              int cutPoint = filter.optimalThreshold(code);
              filter.displayNodesFilter(cutPoint);
              filter.saveDBCProyection(cutPoint, new File(args[3]));
            }
            else filter.displayNodesFilter();        
          }
        }catch(Exception e){ System.out.println("Something went wrong: "+e+"\n .. aborting !");}
      }
    }
  }//end main(String[])

/**
 * Executes a filter measure for the <code>data</code> dataset and stores the results in the
 * <code>nodesFilter</code> field. The results are sorted in the optimum order depending of the
 * selected metric
 *
 * @param filterMeasure  <ul><li><code>0</code> - Mutual Information
 * 			 <li><code>1</code> - Euclidean distance
 * 			 <li><code>2</code> - Matusita distance
 * 			 <li><code>3</code> - Divergence of Kullback-Leibler mode 1
 * 			 <li><code>4</code> - Divergence of Kullback-Leibler mode 2
 * 			 <li><code>5</code> - Shannon entropy
 * 			 <li><code>6</code> - Bhattacharyya metric
 * </ul>
 *
 */
  public void executeFilter(int filterMeasure)
  {
    double distance;

    switch(filterMeasure)
    {
      case 0:  //MutualInformation
      {
        for (int i=0; i< nVariables-1; i++)
        {
          distance = this.mutualInformation((Node)data.getNodeList().elementAt(i));
          nodesFilter.setFilteredNode(new FilteredNode((Node)data.getNodeList().elementAt(i), distance),i);
        }
        this.sortNodesFilter(1);
        break;
      }
      case 1:  //EuclideanDistance
      {
        for (int i=0; i< nVariables-1; i++)
        {
          distance = this.euclideanDistance((Node)data.getNodeList().elementAt(i));
          nodesFilter.setFilteredNode(new FilteredNode((Node)data.getNodeList().elementAt(i), distance),i);
        }
        this.sortNodesFilter(1);      
        break;
      }
      case 2: //MatusitaDistance
      {
        for (int i=0; i< nVariables-1; i++)
        {
          distance = this.matusitaDistance((Node)data.getNodeList().elementAt(i));
          nodesFilter.setFilteredNode(new FilteredNode((Node)data.getNodeList().elementAt(i), distance),i);
        }
        this.sortNodesFilter(0);      
        break;
      }
      case 3: //KullbackLeibler-1
      {
        for (int i=0; i< nVariables-1; i++)
        {
          distance = this.kullbackLeiblerDistance((Node)data.getNodeList().elementAt(i), 1);
          nodesFilter.setFilteredNode(new FilteredNode((Node)data.getNodeList().elementAt(i), distance),i);
        }
        this.sortNodesFilter(1);      
        break;
      }
      case 4: //KullbackLeibler-2
      {
        for (int i=0; i< nVariables-1; i++)
        {
          distance = this.kullbackLeiblerDistance((Node)data.getNodeList().elementAt(i), 2);
          nodesFilter.setFilteredNode(new FilteredNode((Node)data.getNodeList().elementAt(i), distance),i);
        }
        this.sortNodesFilter(1);      
        break;
      }
      case 5: //Shanon entropy
      {
        for (int i=0; i< nVariables-1; i++)
        {
          distance = this.entropyShanon((Node)data.getNodeList().elementAt(i));
          nodesFilter.setFilteredNode(new FilteredNode((Node)data.getNodeList().elementAt(i), distance),i);
        }
        this.sortNodesFilter(1);      
        break;
      }
      case 6: //Bhattacharyya
      {
        for (int i=0; i< nVariables-1; i++)
        {
          distance = this.bhattacharyyaDistance((Node)data.getNodeList().elementAt(i));
          nodesFilter.setFilteredNode(new FilteredNode((Node)data.getNodeList().elementAt(i), distance),i);
        }
        this.sortNodesFilter(1);      
        break;
      }
      case 7: //CFS run
      {
        Vector cfsSelected;
        cfsSelected = this.cFSHeuristicValues(data.getNodeList()); 
        NodeList cfsSubset = (NodeList)cfsSelected.elementAt(cfsSelected.size()-1);
       for (int i=0; i < cfsSubset.size(); i++)
       { nodesFilter.setFilteredNode(new FilteredNode((Node)cfsSubset.getNodes().elementAt(i), 
                                               ((Double)cfsSelected.elementAt(i)).doubleValue()), i);
       }
       displayNodesFilter(cfsSubset.size());
       break;
      }
    }//end switch
  }//end executeFilter(int)
  
//**********************************************************************************************//
//           rx   ry
// Ip(X;Y) = SUM  SUM  P(X=xi, Y=yj) * log P(X=xi, Y=yj)
//           i=1  j=1
//
//           rx                           ry
//         - SUM P(X=xi) * log P(X=xi)  - SUM P(Y=yj) * log P(Y=yj)
//           i=1                          j=1
//
//
//**********************************************************************************************//
/**
 * Calculates the Mutual Information distance of a variable, based on a multiclass problem
 *
 * @param variable the node to measure
 * @return the Mutual Information distance of the node, based on this statistical function :
 */
  public double mutualInformation(Node variable)
  {
    double info = 0;
    int nClasses = ((FiniteStates)data.getNodeList().elementAt(nVariables-1)).getNumStates();
    // Values the 'variable' node can take
    AuxiliarPotentialTable values = (AuxiliarPotentialTable)potentials[data.getNodeList().getId(variable)];

    double probXiCj, probXi, probCj, numXi;
    
    for (int i=0; i<((FiniteStates)variable).getNumStates(); i++)
      for (int j=0; j< nClasses; j++)
      {
        probXiCj = values.getNumerator(i,j) / nCases;

        if ( probXiCj != 0)
          info += probXiCj * ( Math.log(probXiCj) / Math.log(10) );
      }

    for (int i=0; i<((FiniteStates)variable).getNumStates(); i++)
    {
       numXi=0;
       for (int k=0; k<values.getNStatesOfParents(); k++) numXi += values.getNumerator(i,k);
       probXi = numXi / nCases;
       if ( probXi != 0 ) info = info - ( probXi * (Math.log(probXi) /Math.log(10)) );
    }

    for (int j=0; j< nClasses; j++)
    {
       probCj = values.getDenominator(j) /nCases;
       if ( probCj != 0 ) info = info - ( probCj * (Math.log(probCj) /Math.log(10)) );
    }
    
    return info;
  }//end mutualInformation(Node)

//**********************************************************************************************//
//            rx   ry
//  Ip(X;Y) = SUM  SUM  P(X=xi, Y=yj) * log P(X=xi, Y=yj)
//            i=1  j=1
// 
//            rx                           ry
//          - SUM P(X=xi) * log P(X=xi)  - SUM P(Y=yj) * log P(Y=yj)
//            i=1                          j=1
//
//**********************************************************************************************//
/**
 * Calculates the Mutual Information distance of a variable, based on a multiclass problem
 *
 * @param variable the node X to measure
 * @param variable the node Y to measure
 * @return the Mutual Information distance of the node, based on this statistical function :
 */
  public double mutualInformation(Node variableX, Node variableY)
  {
    //Initialization of the auxiliar tables
    AuxiliarPotentialTable potentialX = this.intializeAuxiliarPotential(variableX, variableY);
    //AuxiliarPotentialTable potentialY = this.intializeAuxiliarPotential(variableY, variableY);
    AuxiliarPotentialTable potentialY = (AuxiliarPotentialTable)potentials[data.getNodeList().getId(variableY)];
    
    return this.mutualInformation(variableX, variableY, potentialX, potentialY);
  }//end mutualInformation(Node, Node)

/**
 * In basis of two nodes this method returns the auxiliar potential table with the counters
 * asociated to them
 * @param node_x the child node to treat
 * @param mode_y the parent node
 * @return the auxiliar potential table of node_x in function of the values of its parent, node_y
 */
 public AuxiliarPotentialTable intializeAuxiliarPotential(Node node_x, Node node_y)
 {
    AuxiliarPotentialTable potential;
    for (int i=0; i< nVariables; i++)
      if (((Node)data.getNodeList().elementAt(i)).getTypeOfVariable() != Node.FINITE_STATES) 
        throw new SecurityException("There are continuous values. First, use a Discretization method.");

    potential=new AuxiliarPotentialTable(((FiniteStates)node_x).getNumStates(), ((FiniteStates)node_y).getNumStates());

    //Values register in the potentials array
    Iterator search;
    search = ((CaseListMem)((Relation)data.getRelationList().get(0)).getValues()).getCases().iterator();
    int[] row = new int[nVariables];
    for (int i=0; i< nCases; i++)
    {
      row = (int[])search.next();
      potential.addCase(row[this.data.getNodeList().getId(node_x)], row[this.data.getNodeList().getId(node_y)],1);
    }
   return potential;
 }
 
//**********************************************************************************************//
//           rx   ry
// Ip(X;Y) = SUM  SUM  P(X=xi, Y=yj) * log P(X=xi, Y=yj)
//           i=1  j=1
//
//           rx                           ry
//         - SUM P(X=xi) * log P(X=xi)  - SUM P(Y=yj) * log P(Y=yj)
//           i=1                          j=1
//
//**********************************************************************************************//
/**
 * 
 */
  public double mutualInformation(Node variableX, Node variableY, AuxiliarPotentialTable potX, AuxiliarPotentialTable potY)
  {    
    double info = 0;

    double probXiCj, probXi, probCj, numXi;
    
    for (int i=0; i<((FiniteStates)variableX).getNumStates(); i++)
      for (int j=0; j<((FiniteStates)variableY).getNumStates(); j++)
      {
        probXiCj = potX.getNumerator(i,j) / nCases;

        if ( probXiCj != 0)
          info += probXiCj * ( Math.log(probXiCj) / Math.log(10) );
      }

    for (int i=0; i<((FiniteStates)variableX).getNumStates(); i++)
    {
       numXi=0;
       for (int k=0; k<potX.getNStatesOfParents(); k++) numXi += potX.getNumerator(i,k);
       probXi = numXi / nCases;
       if ( probXi != 0 ) info = info - ( probXi * (Math.log(probXi) /Math.log(10)) );
    }

    for (int j=0; j<((FiniteStates)variableY).getNumStates(); j++)
    {
       numXi=0;
       for (int k=0; k<potY.getNStatesOfParents(); k++) numXi += potY.getNumerator(j,k);
       probXi = numXi / nCases;
       if ( probXi != 0 ) info = info - ( probXi * (Math.log(probXi) /Math.log(10)) );
    }
    return info;
  }//end mutualInformation(Node, Node, AuxiliarPotentialTable, AuxiliarPotentialTable)

//**********************************************************************************************//
//            rx   rc   l<k
//  De(X) = ( SUM  SUM  SUM  P(C=cl) * P(C=ck) * abs( P(X=xi|C=cl)^^2 - P(X=xi|C=ck)^^2 ) )^^1/2
//            i=1  k=1  l=1
//  
//  Euclidean metric balanced with the relative weights of the classes probabilities
// 
//**********************************************************************************************//
/**
 * Calculates the Euclidean distance of a variable, based on a multiclass problem
 *
 * @param variable the node to measure
 * @return the Euclidean metric of the node, based on this statistical function :
 */
  public double euclideanDistance(Node variable)
  {
    double info = 0;
    int nClasses = ((FiniteStates)data.getNodeList().elementAt(nVariables-1)).getNumStates();
    // Values the decision node can take
    AuxiliarPotentialTable c_values = (AuxiliarPotentialTable)potentials[nVariables-1];

    double probXiCl, probXiCk, probCl, probCk, numCl, numCk;
    
    for (int i=0; i<((FiniteStates)variable).getNumStates(); i++)
      for (int k=0; k< nClasses; k++)
        for (int l=0; l<k; l++)
        {
          //Calculate of probCl, probCk probabilities
          numCl=0; numCk=0;
          for (int aux=0; aux<nClasses; aux++) numCl += c_values.getNumerator(l,aux);
          for (int aux2=0; aux2<nClasses; aux2++) numCk += c_values.getNumerator(k,aux2);          
          probCl = numCl / nCases; probCk = numCk / nCases;
          //-
          
          probXiCl=((AuxiliarPotentialTable)potentials[data.getVariables().getId(variable)]).getPotential(i,l);
          probXiCk=((AuxiliarPotentialTable)potentials[data.getVariables().getId(variable)]).getPotential(i,k);
          
          info += (probCl*probCk) * Math.abs(Math.pow(probXiCl,2) - Math.pow(probXiCk,2)); 
        }

    return Math.pow(info, 0.5);

  }//end euclideanDistance(Node)

//**********************************************************************************************//
//         rc   j<i                        rx
// Dm(X) = SUM  SUM  P(C=ci) * P(C=cj) * ( SUM  ( P(X=xk|C=ci)*P(X=xk|C=cj) )^^1/2 ) )
//         i=1  j=1                        k=1
//
// The better distance is the minimum matusita distance
//
//**********************************************************************************************//    
/**
 * Calculates the Matusita metric of a variable, based on a multiclass problem
 *
 * @param variable the node to measure
 * @return the Matusita metric of the node, based on this statistical function :
 */
  public double matusitaDistance(Node variable)
  {
    double info = 0; double acumulate = 0;
    int nClasses = ((FiniteStates)data.getNodeList().elementAt(nVariables-1)).getNumStates();
    // Values the decision node can take
    AuxiliarPotentialTable c_values = (AuxiliarPotentialTable)potentials[nVariables-1];

    double probXkCi, probXkCj, probCi, probCj, numCi, numCj;
    
    for (int i=0; i < nClasses; i++)
    {
      for (int j=0; j < i; j++)
      {
        //Calculate of probCi, probCj probabilities        
        numCi=0; numCj=0;
        for (int aux=0; aux<nClasses; aux++) numCi += c_values.getNumerator(i,aux);
        for (int aux2=0; aux2<nClasses; aux2++) numCj += c_values.getNumerator(j,aux2);
        probCi = numCi / nCases; probCj = numCj / nCases;
        //-
        
        for (int k=0; k < ((FiniteStates)variable).getNumStates(); k++)
        {
          probXkCi=((AuxiliarPotentialTable)potentials[data.getVariables().getId(variable)]).getPotential(k,i);
          probXkCj=((AuxiliarPotentialTable)potentials[data.getVariables().getId(variable)]).getPotential(k,j);
          acumulate += Math.pow((probXkCi * probXkCj),0.5);
        }
        info += probCi * probCj * acumulate;
        acumulate = 0;
      }
    }

    return info;

  }//end matusitaDistance(Node)

//**********************************************************************************************//
//            rc   j<i
// KL(X;C)m = SUM  SUM  P(C=ci) * P(C=cj) * KLij(X)m
//            i=1 j=1
//
// KLij(X)m -> KullBack-Leibler distance mode m from class i to class j
//
//**********************************************************************************************//    
/**
 * Calculates the divergence of Kullback-Leibler of a variable, based on a multiclass problem
 *
 * @param variable the node to measure
 * @param mode the divergence to calculate. <code>1</code>-mode 1, <code>2</code>mode 2
 * @return the Kullback Leibler <code>mode</code> metric of the node
 */
  public double kullbackLeiblerDistance(Node variable, int mode)
  {
    if ((mode != 1) && (mode != 2))
    {
      System.err.println("Error calling kullbackLeiblerDistance");
      System.exit(0);
    }

    double info = 0;
    int nClasses = ((FiniteStates)data.getNodeList().elementAt(nVariables-1)).getNumStates();
    // Values the decision node can take
    AuxiliarPotentialTable c_values = (AuxiliarPotentialTable)potentials[nVariables-1];

    double probCi, probCj, numCi, numCj;

    for (int i=0; i < nClasses; i++)
    {
      for (int j=0; j < i; j++)
      {
        //Calculate of probCi, probCj probabilities
        numCi=0; numCj=0;
        for (int aux=0; aux<nClasses; aux++) numCi += c_values.getNumerator(i,aux);
        for (int aux2=0; aux2<nClasses; aux2++) numCj += c_values.getNumerator(j,aux2);
        probCi = numCi / nCases; probCj = numCj / nCases;
        //-

        if ( mode==1 ) info += probCi * probCj * kullbackLeibler_mode1(variable, i, j);
        else info += probCi * probCj * kullbackLeibler_mode2(variable, i, j);
      }
    }

    return info;

  }//end kullbackLeiblerDistance(Node, int)

//**********************************************************************************************//    
// KLij(X)1 = Dkl(P(X|C=ci), P(X)) + Dkl(P(X|C=cj), P(X))
//
// For both Kullback-Leibler metrics :
//
// Dkl(P(X), Q(X)) = SUM  P(xi) * log (P(xi) / Q(xi))
//                   xi
//
//**********************************************************************************************//    
/**
 * Calculates a divergence of Kullback-Leibler of a variable, based on a multiclass problem. We call
 * this measure Kullback-Leibler mode 1.
 *
 * @param variable the node to measure
 * @return the Kullback Leibler mode 1 metric of the node, based on this statistical function :
 */
  private double kullbackLeibler_mode1(Node variable, int class1, int class2)
  {
    double distance = 0;
    // Values the 'variable' node can take
    AuxiliarPotentialTable values = (AuxiliarPotentialTable)potentials[data.getNodeList().getId(variable)];

    double probXiCi, probXiCj, probXi, numXi;

    for (int i=0; i<((FiniteStates)variable).getNumStates(); i++)
    {
      numXi=0;
      for (int aux=0; aux<values.getNStatesOfParents(); aux++) numXi += values.getNumerator(i,aux);
      probXi = numXi / nCases;

      probXiCi = values.getPotential(i,class1);
      probXiCj = values.getPotential(i,class2);

      if (probXi !=0)
      {
        if (probXiCi != 0) distance += (probXiCi * ( Math.log(probXiCi/probXi) / Math.log(10)) );
        if (probXiCj != 0) distance += (probXiCj * ( Math.log(probXiCj/probXi) / Math.log(10)) );
      }
    }

    return distance;
  }//end kullbackLeibler_mode1(Node, int, int)

//**********************************************************************************************//    
//  KLij(X)2 = Dkl(P(X|C=ci), P(X|C=cj)) + Dkl(P(X|C=cj), P(X|C=ci))
// 
//  For both Kullback-Leibler metrics :
// 
//  Dkl(P(X), Q(X)) = SUM  P(xi) * log (P(xi) / Q(xi))
//                    xi
//
//**********************************************************************************************//    
/**
 * Calculates a divergence of Kullback-Leibler of a variable, based on a multiclass problem. We call
 * this measure Kullback-Leibler mode 2. In order to avoid div0 in this metric, Laplace correction
 * is always applied.
 *
 * @param variable the node to measure
 * @return the Kullback Leibler mode 2 metric of the node, based on this statistical function :
 */
  private double kullbackLeibler_mode2(Node variable,  int class1, int class2)
  {
    double distance = 0;
    int nClasses = ((FiniteStates)data.getNodeList().elementAt(nVariables-1)).getNumStates();    
    // Values the 'variable' node can take
    AuxiliarPotentialTable values = (AuxiliarPotentialTable)potentials[data.getNodeList().getId(variable)];
    values.applyLaplaceCorrection(); //Laplace correction

    double probXiCi, probXiCj;

    for (int i=0; i<((FiniteStates)variable).getNumStates(); i++)
    {
      probXiCi = values.getPotential(i, class1);
      probXiCj = values.getPotential(i, class2);

      distance += (probXiCi - probXiCj) * ( Math.log(probXiCi) / Math.log(10) );
      distance += (probXiCj - probXiCi) * ( Math.log(probXiCj) / Math.log(10) );
    }

    return distance;

  }//end kullbackLeibler_mode2(Node, int, int)

//**********************************************************************************************//    
//           rc    j<i
// SH(X;C) = SUM   SUM  P(C=ci) * P(C=cj) * Hij(X)
//           i=1   j=1
//
//            rx
// Hij(X) = - SUM P(X=xi|C=ci)*log P(X=xi|C=cj) + P(X=xi|C=cj)*log P(X=xi|C=ci)
//            k=1                 2                               2
//
//**********************************************************************************************//    
/**
 * Calculates the Shannon entropy of a variable, based on a multiclass problem.
 *
 * @param variable the node to measure
 * @return the Shannon entropy of the node, based on this statistical function :
 */
  public double entropyShanon(Node variable)
  {
    double info = 0; double acumulate = 0;
    int nClasses = ((FiniteStates)data.getNodeList().elementAt(nVariables-1)).getNumStates();
    // Values the 'variable' node can take
    AuxiliarPotentialTable values = (AuxiliarPotentialTable)potentials[data.getNodeList().getId(variable)];
    // Values the decision node can take
    AuxiliarPotentialTable c_values = (AuxiliarPotentialTable)potentials[nVariables-1];

    double probXkCi, probXkCj, probCi, probCj, numCi, numCj;
    
    for (int i=0; i < nClasses; i++)
    {
      for (int j=0; j < i; j++)
      {
        //Calculate probCi, probCj
        numCi = 0; numCj = 0;
        for (int aux=0; aux<nClasses; aux++) numCi += c_values.getNumerator(i,aux);
        for (int aux2=0; aux2<nClasses; aux2++) numCj += c_values.getNumerator(j,aux2);
        probCi = numCi / nCases; probCj = numCj / nCases;
        //-

        acumulate = 0;
        for (int k=1; k<((FiniteStates)variable).getNumStates(); k++)
        {
          probXkCi = values.getPotential(k, i);
          probXkCj = values.getPotential(k, j);
          
          if (probXkCi != 0)
          {
            double aux = Math.log(probXkCj)/Math.log(2);
            if (! Double.isInfinite(aux) ) acumulate += probXkCi * aux;
          }
          if (probXkCj != 0) 
          {
            double aux = Math.log(probXkCi)/Math.log(2);
            if (! Double.isInfinite(aux) ) acumulate += probXkCj * aux;
          }

        }
        info += - ( probCi * probCj * acumulate);
      }
    }
    return info;
  }//end entropyShanon(Node)


//**********************************************************************************************//    
//           rc                    rx
// Bh(X;C) = SUM - log [ P(C=ci) * SUM ( P(X=xj|C=ci)P(X=xj) )^^1/2 ]
//           i=1                   j=1
//
//**********************************************************************************************//    
/**
 * Calculates the Bhattacharyya metric of a variable, based on a multiclass problem.
 *
 * @param variable the node to measure
 * @return the Bhattacharyya metric of the node, based on this statistical function :
 */
  public double bhattacharyyaDistance(Node variable)
  {
    double info = 0; double acumulate = 0;
    int nClasses = ((FiniteStates)data.getNodeList().elementAt(nVariables-1)).getNumStates();
    // Values the 'variable' node can take
    AuxiliarPotentialTable values = (AuxiliarPotentialTable)potentials[data.getNodeList().getId(variable)];
    // Values the decision node can take
    AuxiliarPotentialTable c_values = (AuxiliarPotentialTable)potentials[nVariables-1];
    
    double probXjCi, probXj, probCi, numCi, numXj;
    
    for (int i=0; i < nClasses; i++)
    {
      //Calculate probCi, probCj
      numCi = 0;
      for (int aux=0; aux<nClasses; aux++) numCi += c_values.getNumerator(i,aux);
      probCi = numCi / nCases;
      //-
      acumulate = 0;
      for (int j=0; j < ((FiniteStates)variable).getNumStates(); j++)
      {
        numXj=0;
        for (int aux=0; aux<values.getNStatesOfParents(); aux++) numXj += values.getNumerator(j,aux);    
        probXj = numXj / nCases;
        probXjCi = values.getPotential(j, i);
        
        acumulate += Math.pow(probXj*probXjCi, 0.5);
      }
      
      acumulate = probCi*acumulate;
      if (acumulate != 0) info += -(Math.log(acumulate)/Math.log(10));
    }
    return info;

  }//end bhattacharyyaDistance(Node)

/**
 * correlationFeatureSelection Javadoc
 * 
 */
 public NodeList correlationFeatureSelection(NodeList originalNodes)
 {
    boolean next = true;  double meanClassIC, mIC;  double averageNodesIC, avIC;  double h;  double h2;  
    NodeList nodLisTemp;  Node nodeTemp, nodeAux; 
    
    double k = 1; mIC = 0; avIC = 0; nodeAux = null;
    //Initializations of the main structures
    System.out.print("Initializing structures.. ");
    SearchParameters searchParams = new SearchParameters(this.potentials, originalNodes, nCases);
    SearchState search            = new SearchState(originalNodes, FORWARD_SEARCH);
    System.out.println("done !!");
    //Set the initial attribute as the one with the most Mutual Information respect the class
    nodeTemp       = searchParams.getMaxClassCorrelatedNode(originalNodes);
    meanClassIC    = searchParams.getMI(nodeTemp, 
                            (Node)originalNodes.getNodes().elementAt(originalNodes.size()-1)) /
                                  searchParams.getEntropy((Node)originalNodes.getNodes().elementAt(originalNodes.size()-1));
    averageNodesIC = 1 / searchParams.getEntropy(nodeTemp);
    search.addSelectedNode(nodeTemp, meanClassIC, averageNodesIC);
    //Main search loop
    System.out.print("Performing search, please wait.. ");
    while (next)
    {
      next = false;
      h = search.getHeuristic();
      nodLisTemp = search.getRestNodes();
      k = search.getSelectedNodes().size()+1;
      for (int i=0; i<nodLisTemp.size(); i++)
      {
        nodeTemp = (Node)nodLisTemp.getNodes().elementAt(i);
        //Compute the correlations and its heuristic value
        meanClassIC     = this.computeClassIC(searchParams, search, nodeTemp, 
                                                        (Node)originalNodes.getNodes().elementAt(originalNodes.size()-1));
        averageNodesIC  = this.computeNodesIC(searchParams, search, nodeTemp);
        h2 =  (k * meanClassIC) / (Math.pow(k+(k*(k-1)*averageNodesIC), 0.5));
        if (h2 > h) //Candidate node
        {
          nodeAux = nodeTemp; mIC = meanClassIC; avIC = averageNodesIC; h = h2;
          next = true;
        }
      }
      if (next) search.addSelectedNode(nodeAux, mIC, avIC);
    }
    System.out.println("done !!");
    return search.getSelectedNodes();
 }//end correlationFeatureSelection(NodeList)

/**
 * cfsHeuristicValues Javadoc
 */
 public Vector cFSHeuristicValues(NodeList originalNodes)
 {
    boolean next = true;  double meanClassIC, mIC;  double averageNodesIC, avIC;  double h;  double h2;  
    NodeList nodLisTemp;  Node nodeTemp, nodeAux; 
    Vector results = new Vector();
    
    double k = 1; mIC = 0; avIC = 0; nodeAux = null; h2 = 0;
    //Initializations of the main structures
    System.out.print("Initializing structures.. ");
    SearchParameters searchParams = new SearchParameters(this.potentials, originalNodes, nCases);
    SearchState search            = new SearchState(originalNodes, FORWARD_SEARCH);
    System.out.println("done !!");
    //Set the initial attribute as the one with the most Mutula Information respect the class
    nodeTemp       = searchParams.getMaxClassCorrelatedNode(originalNodes);
    meanClassIC    = searchParams.getMI(nodeTemp, 
                            (Node)originalNodes.getNodes().elementAt(originalNodes.size()-1)) /
                                  searchParams.getEntropy((Node)originalNodes.getNodes().elementAt(originalNodes.size()-1));
    averageNodesIC = 1 / searchParams.getEntropy(nodeTemp);
    search.addSelectedNode(nodeTemp, meanClassIC, averageNodesIC);

    results.add(new Double(search.getHeuristic()));
    //Main search loop
    System.out.print("Performing search, please wait.. ");
    while (next)
    {
      next = false;
      h = search.getHeuristic();
      nodLisTemp = search.getRestNodes();
      k = search.getSelectedNodes().size()+1;
      for (int i=0; i<nodLisTemp.size(); i++)
      {
        nodeTemp = (Node)nodLisTemp.getNodes().elementAt(i);
        meanClassIC     = this.computeClassIC(searchParams, search, nodeTemp, 
                                                        (Node)originalNodes.getNodes().elementAt(originalNodes.size()-1));
        averageNodesIC  = this.computeNodesIC(searchParams, search, nodeTemp);
        h2 =  (k * meanClassIC) / (Math.pow(k+(k*(k-1)*averageNodesIC), 0.5));
        if (h2 > h) //Candidate node
        {
          nodeAux = nodeTemp; mIC = meanClassIC; avIC = averageNodesIC; h = h2;
          next = true;
        }
      }
      if (next)
      { 
        search.addSelectedNode(nodeAux, mIC, avIC);
        results.add(new Double(search.getHeuristic()));
      }
    }
    System.out.println("done !!");
    results.add(search.getSelectedNodes());
    return results;
 }//end correlationFeatureSelection(NodeList)


/**
 * computeClassIC Javadoc
 */
 private double computeClassIC(SearchParameters params, SearchState state, Node n, Node classNode)
 {
   double k = state.getSelectedNodes().size();
   return ((k*state.getMeanClassIC())/(k+1)) +
            (params.getMI(n, classNode)/((k+1)*params.getEntropy(classNode)));
 }//end computeClassIC(SearchParameters, SearchState, Node)

/**
 * computeNodesIC Javadoc
 */
 private double computeNodesIC(SearchParameters params, SearchState state, Node n)
 {
   double k = state.getSelectedNodes().size();
   double r_ii = ((k-1)/(k+1))*state.getAverageNodesIC();
   double aux = 0; double mi; double entropy1; double entropy2;
   for (int i=0; i<state.getSelectedNodes().size(); i++)
   {
     mi = params.getMI(n, (Node)state.getSelectedNodes().getNodes().elementAt(i));
     entropy1 = params.getEntropy(n);
     entropy2 = params.getEntropy((Node)state.getSelectedNodes().getNodes().elementAt(i));
     aux += (mi * (entropy1 + entropy2)) / (entropy1 * entropy2);
   }
   return r_ii + (aux/(k*(k+1)));
 }//end computeNodesIC(SearchParameters, SearchState, Node)
 
/**
 * Writes a proyection of the original nodeList taking into account the frontier set
 * by the elbow method. As usual, the last node of the set will be the class node of the original data
 * @param file file output descriptor
 * @param metric filter measure used for the previous attribute ranking
 * @return the nodes that have being saved
 */
 public int saveDBCOptimalProyection(File file, int metric)
 {
    int no = this.optimalThreshold(metric);
    saveDBCProyection(no, file); 
    return no;
 }//end saveDBCProyection(File, int)
 
/**
 * Writes a new data base file including only the specified number of nodes. The purpose of
 * this method is to save a new dbc file with only the first <code>cut</code> nodes and its data, which
 * would have been pointed as the most important. If the <code>nodesFilter</code> field hasn't been
 * sorted, the original order will be kept. Note that the file will include the class node as the last 
 * node of it.
 * 
 * @param cut number of nodes to be saved (class node not included)
 * @param file the output file
 */
 public void saveDBCProyection(int cut, File file)
 {
   // Set the nodeList into the dbc
   Vector nodes = new Vector();
   for (int i=0; i<cut; i++)
     nodes.addElement(nodesFilter.getFilteredNode(i).getNode());
   NodeList nodelist = new NodeList(nodes);
   saveDBC(file, nodelist);
 }//end saveDBCProyection(int, String)

/**
 * Writes a new data base file including only the nodes found by the <code>correlationFeatureSelection<\code> method.
 * The file will include the class node as the last node of it.
 * 
 * @param f the output file
 * @return the number of included nodes
 */
 public int saveCFSProyection(File f)
 {
   NodeList cfsSelected = new NodeList();
   int index = 0;
   while (nodesFilter.getFilteredNode(index) != null)
   {
     cfsSelected.insertNode(nodesFilter.getFilteredNode(index).getNode());
     index++;
   }
   this.saveDBC(f, cfsSelected);
   return index;
 }//end saveCFSProyection(File)

/**
 * Generic method 
 * 
 * @param nodelist nodelist with the nodes to be saved in the proyection file
 * @param file the output file
 */
 private void saveDBC(File file, NodeList nodelist)
 {
   DataBaseCases output = new DataBaseCases();
   output.setName(file.getName());
   output.setTitle(file.getName());

   //Add the class node, the last in the original database file
   nodelist.insertNode((Node)data.getNodeList().elementAt(nVariables-1));
  
   output.setNodeList(nodelist);
    
   CaseListMem caselist = new CaseListMem(nodelist);
   Configuration dbcconf = new Configuration(nodelist);

   Vector cases = ((CaseListMem)((Relation)data.getRelationList().get(0)).getValues()).getCases();
   int[] cas = new int[nVariables];
   NodeList origin = data.getNodeList();
   
   for (int i=0; i < nCases; i++)
   {
     Configuration aux = new Configuration(nodelist);
     cas = (int[])cases.elementAt(i);

     for (int j=0; j < nodelist.getNodes().size(); j++)
     {
       FiniteStates node = (FiniteStates)nodelist.getNodes().elementAt(j);
       aux.putValue(node, cas[origin.getId(node)]);
     }
     //Include the values of the class variable, the last in the original file
     FiniteStates node = (FiniteStates)origin.getNodes().elementAt(nVariables-1);
     aux.putValue(node, cas[nVariables-1]);

     dbcconf.setValues(aux.getValues());
     caselist.put(dbcconf);
   }

   Vector   relationVector = new Vector();
   Relation relation       = new Relation();

   relation.setVariables(nodelist);
   relation.setValues(caselist);
   relationVector.addElement(relation);
   output.setRelationList(relationVector);

   try{
    FileWriter descriptor = new FileWriter(file);
    output.saveDataBase(descriptor);
    System.out.println("\nFile "+ file.getPath() + " correctly written");
   }catch (Exception e){System.out.println("An error has ocurred tryingo to write the file");}   
 }//end saveDBC(File, NodeList)
 
/**
 * Displays the pairs <code>node - measure</code> on the terminal console. 
 * All filtered nodes will be shown
 */
 public void displayNodesFilter()
 {
   displayNodesFilter(nVariables-1);
 }//end displayNodesFilter()

/**
 * Displays the pairs <code>node - measure</code> on the terminal console
 * 
 * @param number of nodes to be displayed
 */
 public void displayNodesFilter(int number)
 {
   char[] name = new char[25]; String node;
   System.out.println();
   System.out.println("Node                        Filter metric");
   System.out.println("-----------------------------------------------");
   for (int i=0; i<number; i++)
   {
     for (int aux=0; aux< name.length; aux++) name[aux]= ' ';
     node = nodesFilter.getFilteredNode(i).getNode().getName();
     node.getChars(0, node.length(), name,0);
     System.out.println( new String(name,0, 25) +"   "+nodesFilter.getFilteredNode(i).getDistance());
   }
 }//end displayNodesFilter()
  
/**
 * Sorts the main field <code>nodesFilter</code> in the specified order
 * 
 * @param mode 0-Ascendat sorting; 1-Descendant sorting
 */
 public void sortNodesFilter(int mode)
 {
    this.sortNodes(mode, this.nodesFilter);
 }//end sortNodesFilter(int)

/**
 * Sorts an AuxiliarNodeList in the specified order
 * 
 * @param mode 0-Ascendant sorting; 1-Descendant sorting
 * @param listNodes the nodes to be sorted
 */
 private void sortNodes(int mode, AuxiliarNodeList listNodes)
 {
    if (mode == 0) //Ascendant
      listNodes.sortAscendant();
    else if (mode == 1) //Descendant
      listNodes.sortDescendant();
    else
      System.err.println("Parameter not correct in sorting method");
 }//end sortNodes(int, AuxiliarNodeList)

/**
 * Allows access the data to external objects instances
 * 
 * @return a vector with the sorted nodes and the filter value
 *   pos i -> (Node) the node
 *   pos i+1 -> (Double) filter measure of i node
 */
 public Vector getNodesFiltered()
  {
    int numNodes = 0;
    while (nodesFilter.getFilteredNode(numNodes) != null)
    {
      numNodes++;
      if (numNodes == nodesFilter.getSize())
        break;
    }    
    Vector nodes = new Vector(2*numNodes);
    for (int i=0; i<numNodes; i++)
    {
      nodes.addElement(nodesFilter.getFilteredNode(i).getNode());
      nodes.addElement(new Double(nodesFilter.getFilteredNode(i).getDistance()));
    }
    return nodes;
 }//end getNodesFiltered()

/**
 * Displays the terminal usage information of the class 
 */
 private static void usage()
 {
    System.out.println("\nUsage: FilterMeasures input.dbc filterOption [noVar] [output.dbc]");
    System.out.println("       FilterMeasures input.dbc 7 [output.dbc]\n");
    System.out.println("filterOption: 0 - Mutual information");
    System.out.println("              1 - Euclidean distance");
    System.out.println("              2 - Matusita distance");
    System.out.println("              3 - Kullback-Leibler mode 1");
    System.out.println("              4 - Kullback-Leibler mode 2");    
    System.out.println("              5 - Shanon entropy");
    System.out.println("              6 - Bhattacharyya metric");
    System.out.println("              7 - Correlation-based Feature Selection\n");
    System.out.println("noVar:        number of variables to be displayed/saved");
    System.out.println("              0 - the number of variables will be determined automatically \n");
    System.out.println("output.dbc: file where the \'noVar\' filtered variables will be saved, including the class one (the last of them)");
    System.out.println("            in CFS case \'output.dbc\' file will include the selected variables by the method and the class\n");
 }//end usage()

/**
 * Returns the optimal number of attributes of the ranking in base to an elbow method
 */
 private int optimalThreshold(int measure)
 {
   //Localizar el punto que marca la distancia media
   //Crear el intervalo de distancia entre su media +/- 2std
   //Una vez determinado el intervalo de búsqueda crear los coeficientes de la búsqueda
   double media = nodesFilter.getMedia();
   double var =  nodesFilter.getVar();
   //System.out.println("Mean distance : "+media);
   //System.out.println("Set's variance: "+var);

   //Determinar el intervalo de nodos que están a dos varianzas de él
   double upperLimit = media + 2*var;
   double lowerLimit = media - 2*var;
   //System.out.println("Maximum value : "+upperLimit);
   //System.out.println("Lowest  value : "+lowerLimit);
   boolean foundUp = false; boolean foundLow = false;
   int pos = -1; int upper = 0; int lower = 0;
   if (measure == 2) //Matusita metric
   {
   while ((!foundUp) || (!foundLow))
     {
       pos++;
       if ((nodesFilter.getFilteredNode(pos).getDistance() >= upperLimit) && !foundUp)
        { upper = pos; foundUp = true; }
       if ((nodesFilter.getFilteredNode(pos).getDistance() > lowerLimit) && !foundLow)
        { lower = pos; foundLow = true; }      
     }     
   }
   else //Rest of metrics
   {
     while ((!foundUp) || (!foundLow))
     {
       pos++;
       if ((nodesFilter.getFilteredNode(pos).getDistance() < upperLimit) && !foundUp)
        { upper = pos; foundUp = true; }
       if ((nodesFilter.getFilteredNode(pos).getDistance() <= lowerLimit) && !foundLow)
        { lower = pos; foundLow = true; }      
     }
   }
   //Defino los s_i = w_i + w_{i-1}
   if (lower-upper != 0)
   {
      if (lower-upper < 0) //Interchange of the indexes
      {
        lower = upper + lower;
        upper = lower - upper;
        lower = lower - upper;
      }
      //System.out.println("Up cut point  : "+upper);
      //System.out.println("Low cut point : "+lower);      

      double[] s = new double[nVariables-1];  s[0]=0;
      double[] sigma = new double[lower];   sigma[0]=0;
      double[] K = new double[lower-upper];
      //Calculo las s_i
      for (int i=1; i<s.length; i++)
        s[i]=nodesFilter.getFilteredNode(i).getDistance() +
                                nodesFilter.getFilteredNode(i-1).getDistance();
      //Calculo las sigma_i
      for (int i=1; i<sigma.length; i++)
        sigma[i] = sigma[i-1]+s[i];
      
      //Calculo sigma_n
      double sigma_n = 0;
      for (int i=1; i<nVariables-1; i++)
        sigma_n += s[i];
        
      //Calculo las K del intervalo
      for (int i=0; i<K.length; i++)
        K[i] = 1 - ((sigma[i+upper] * ((nVariables-1) - (i+upper+1)))/((sigma_n)*(nVariables-1)) );
      
      //Calcular el máximo valor y devolver ese índice como corte
      int index = 0; double max = 0; double aux;
      for (int i=0; i<K.length ; i++)
      {
          aux = K[i];
          if (aux > max)
          { max = aux; index = i; }
      }      
      //Visualization
      //for (int i=0; i<K.length; i++)
      //  System.out.println("K["+i+"] = " + K[i]);
      
      //System.out.println("Cut point: "+ (index+upper+1));
      return index+upper+1;
   }
   else //Caso de que coincida con la media
   {
      //System.out.println("Cut point: "+ lower);
       return lower;
   }
 }//end optimalTreshold(int)


}//end FilterMeasures
