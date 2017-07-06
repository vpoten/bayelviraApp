/*
 * TriangulationData.java
 */


package elvira.tools;

import java.io.*;
import elvira.*;

/**
 * TriangulationData: this object stores some data of interest about
 * triangulating a graph, i.e., the deletion sequence, the size, the
 * added links, ...
 * 
 * @since 5/12/2004
 *
 * @author Jose A. Gámez (jgamez@info-ab.uclm.es)
 */


public class TriangulationData {
    
/**
 * The deletion sequence
 */

private NodeList sequence = null;

/**
 * The set of links added during the triangulation
 */

private LinkList fillIns = null;
  
/**
 * Some numerical data
 * -- cliqueTreeSize: The state space size of the triangulation (cliques)
 * -- clusterTreeSize: ciqueTreeSize + size(non maximal clusters)
 * -- width: the maximal number of vars in a clique/cluster
 */

private double cliqueTreeSize = -0.0;
private double clusterTreeSize = -0.0;
private int width = -0;
private double maxCliqueSize = -0.0;

// an auxiliar value 
private double auxValue = -0.0;

/**
 * empty constructor
 */

public TriangulationData( ){

}

/**
 * access methods
 */

public void setSequence(NodeList nl){
  sequence = nl;
}

public void setFillIns(LinkList ll){
  fillIns = ll;
}

public NodeList getSequence(){
  return sequence;
}

public LinkList getFillIns(){
  return fillIns;
}

public double getCliqueTreeSize(){
  return cliqueTreeSize;
}

public double getClusterTreeSize(){
  return clusterTreeSize;
}

public int getWidth(){
  return width;
}

public double getMaxCliqueSize(){
  return maxCliqueSize;
}

public void setCliqueTreeSize(double ts){
  cliqueTreeSize=ts;
}

public void setClusterTreeSize(double ts){
  clusterTreeSize=ts;
}

public void setWidth(int w){
  width=w;
}

public void setMaxCliqueSize(double cs){
  maxCliqueSize=cs;
}

public void setAux(double aux){
  auxValue = aux;
}

public double getAux( ){
  return auxValue;
}

/**
 * update: update the numerical statistics given a new cluster/clique
 *
 * @param s the size of the new cluster/clique
 * @param clique a <code>boolean</code> indicating wether it is a clique
 * 		(true) or a (non-maximal) cluster
 * @param n the number of vars in the clique/cluster 
 */

public void update(double s, boolean clique, int n){
  if (n>width) width=n;
  if (s>maxCliqueSize) maxCliqueSize=s;
  clusterTreeSize += s;
  if (clique) cliqueTreeSize += s;
}


} // end of class