/* Metrics.java */

package elvira.learning;

import elvira.database.DataBaseCases;
import elvira.Bnet;
import elvira.Node;
import elvira.NodeList;
import java.io.Serializable;
import elvira.ConditionalIndependence;
/**
 * Metrics.java
 *
 * This class implements the abstract class of the all possible metrics that 
 * score a Bayes Net or a list of nodes from data.
 *
 * Created: Thu Nov  4 18:22:31 1999
 *
 * @author P. Elvira
 * @version 1.0
 */

public abstract class Metrics  implements Serializable, ConditionalIndependence{
    
    private DataBaseCases data;    // The data
    public  double tStEval = 0.0;  
    public  double totalSt = 0.0;  
    public  double totalTime = 0.0;
    public  double timeStEval = 0.0;
    public  double avStNVar = 0.0;


    /**
     * This method scores a Bayes Net from data base of cases.
     * @param Bnet b. The Bayes Net to be scoring.
     * @return double. The score.
     */

    
    public abstract double score (Bnet b);

    /**
     * This method scores a Node List from data base of cases.
     * @param NodeList vars. The Node List.
     * @return double. The score.
     */


    public abstract double score (NodeList vars);

    /** Access methods **/

    public DataBaseCases getData(){
	return data;
    }

    public void setData(DataBaseCases data){
	this.data = data;
    }
    
    public double getTotalTime(){
        return totalTime;
    }
    public double getTimeStEval(){
        return timeStEval;
    }
    public double getTotalSt(){
        return totalSt;
    }
    public double getTotalStEval(){
        return tStEval;
    }
    public double getAverageNVars(){
        return (avStNVar/tStEval);
    }
       public double getDep (Node x, Node y, NodeList z) {
  return(scoreDep(x,y,z));
    
    
}   
    
    public double scoreDep (Node x, Node y, NodeList z) {
    
    NodeList aux;
    double x1,x2;
    
    aux = new NodeList();
    aux.insertNode(x);
    aux.join(z);
    x1 = score(aux);
    aux.insertNode(y);
    x2 = score(aux);
    return(x2-x1);
    
    
}
    
public boolean independents (Node x, Node y, NodeList z) {
    
  double aux;
  
  aux = scoreDep(x,y,z);
  if (aux>=0) {return(true);}
  else {return(false);}
    
 
  
}


   
public boolean independents (Node x, Node y, NodeList z, int degree) {
    
  return(independents(x,y,z));
    
 
  
}


   
public boolean independents (Node x, Node y, NodeList z, double degree) {
    
  return(independents(x,y,z));
    
 
  
}
   
    
   public NodeList getNodeList(){
        
        return(getData().getNodeList());
    }  
    
    
}
// Metrics

