/* Fusion.java */

package elvira.fusion;

import java.io.*;
import java.util.*;
import elvira.*;
import elvira.potential.*;
import elvira.inference.Propagation;
import elvira.inference.elimination.VariableElimination;
import elvira.parser.*;
import elvira.tools.DSeparation;

/**
 * Class containing the operations needed to perform the fusion of
 * Bayesian networks.
 *
 * Created: Thu Jul 13 19:07:19 2000
 * @since 25/0/2000
 * @author José del Sagrado
 * @version   1.0
 */

public class Fusion {

/**
 * A Bayesian network.
 */
private Bnet fBn;

/**
 * Time, in seconds, needed to compute the network.
 */
private double time;


/**
 * Creates an empty fusion object.
 */

public Fusion() {
  
  fBn = new Bnet();
  time = 0.0;
}


/**
 * Creates a fusion object from a <code>Bnet</code>.
 */

public Fusion(Bnet Bn) {
  
  fBn = Bn;
  time = 0.0;
}


/**
 * Creates a new fusion object containing a Bayesian network obtained
 * from the fusion of another two networks.
 *
 * @param qualitativeType an int indicating the type of qualitative fusion.
 * @param quantitativeType an int indicating the type of quantitative fusion.
 * @param bN1 a <code>Bnet</code> to fuse.
 * @param bN2 the other <code>Bnet</code> to fuse.
 */

public Fusion(int qualitativeType, int quantitativeType, Bnet bN1, Bnet bN2) throws InvalidEditException {
  
  Date d;

  switch (quantitativeType) {
  case 0: // Linear pool
    d = new Date();
    time = (double)d.getTime();
    linearPool (qualitativeType, bN1, bN2);
    d = new Date();
    time = ((double)d.getTime() - time) / 1000;
    // Dep
    System.out.println("Time (secs.) = "+time);
    // Fin Dep
    break;
  case 1: // Logarithmic Pool
    d = new Date();
    time = (double)d.getTime();
    logarithmicPool (qualitativeType, bN1, bN2);
    d = new Date();
    time = ((double)d.getTime() - time) / 1000;
    // Dep
    System.out.println("Time (secs.) = "+time);
    // Fin Dep
    break;
  case 2: // Noisy-OR
    d = new Date();
    time = (double)d.getTime();
    noisyORPool (qualitativeType, bN1, bN2, false);
    d = new Date();
    time = ((double)d.getTime() - time) / 1000;
    // Dep
    System.out.println("Time (secs.) = "+time);
    // Fin Dep
    break;
  case 3: // Noisy-OR with loose probability
    d = new Date();
    time = (double)d.getTime();
    noisyORPool (qualitativeType, bN1, bN2, true);
    d = new Date();
    time = ((double)d.getTime() - time) / 1000;
    // Dep
    System.out.println("Time (secs.) = "+time);
    // Fin Dep
    break;
  }
  
}


/**
 * Gets the network associated to the object.
 * @return the network.
 */

public Bnet getBnet() {
  
  return fBn; 
}


/**
 * Splits a Bayesian network. Creates a vector containing two new
 * Bayesian networks with a percentage of the original nodes and
 * sharing a percentage of common nodes. The sum of the percentage
 * of nodes and half of the percentage of shared nodes can not be greater
 * than 100.
 *
 * @param pcNodes an int indicating the percentage of nodes
 * @parma pcShared an int indicating the percentage of shared nodes
 * @return a <code>Vector</code> with two new networks.
 */

public Vector split (int pcNodes, int pcShared) {
  
  if ((pcNodes<0) || (pcNodes>100)) {
    System.out.println(" porcentaje de nodos incorrecto ");
    System.exit(1);
  }
  
  if ((pcShared<0) || (pcShared>100)) {
    System.out.println(" porcentaje de nodos comunes incorrecto ");
    System.exit(1);
  }
  
  if ((pcNodes + pcShared/2 > 100) || (100 - pcNodes + pcShared/2 > 100)) {
    System.out.println(" porcentajes equivocados ");
    System.exit(1);
  }
  
  Vector net = new Vector();
  Graph g;
  int i, j, lim;
  Link link;
  Node tail, head;
  Bnet Bn;
  
  NodeList sorted = fBn.topologicalOrder();
  
  // Obtain the first network
  // Build the list of nodes and links
  lim = (sorted.size() * (pcNodes + pcShared/2))/100;
  NodeList Bnnl = new NodeList();
  for (i=0 ; i<lim ; i++)
    Bnnl.insertNode(sorted.elementAt(i));

  LinkList Bnll = new LinkList();
  for (i=0 ; i<fBn.getLinkList().size() ; i++) {
    link = fBn.getLinkList().elementAt(i);
    tail = link.getTail();
    head = link.getHead();
    if ((Bnnl.getId(tail)!=-1) && (Bnnl.getId(head)!=-1))
      Bnll.insertLink(link);
  }
  g = new Graph(Bnnl,Bnll,0);
  Bn = new Bnet();
  Bn.setName(fBn.getName()+"Split1");
  Bn.setNodeList(g.getNodeList());
  Bn.setLinkList(g.getLinkList());
  
  // Dep
  System.out.println("\nObtaining first network...");
  // Fin DEP

  // Build the list of relations, copied directly from the original network.
  Relation Bnr;
  PotentialTable p;
  
  for (i=0 ; i<g.getNodeList().size() ; i++) {
    tail = g.getNodeList().elementAt(i);
    Bnnl = new NodeList();
    Bnnl.insertNode(tail);
    Bnnl.merge(tail.getParentNodes());
    Bnr = new Relation();
    Bnr.setVariables(Bnnl);
    Bnr.setName(Bnnl.toString2());

    // Copy the potential
    p = (PotentialTable)(fBn.getRelation(tail).getValues()).copy();
    Bnr.setValues(p);
    Bn.addRelation(Bnr);
  }
  net.addElement(Bn);
  
  // Obtain the second network
  // Build the list of nodes and links
  lim = (sorted.size() * (pcNodes - pcShared/2))/100;
  Bnnl = new NodeList();
  for (i=lim ; i<sorted.size() ; i++)
    Bnnl.insertNode(sorted.elementAt(i));
  
  Bnll = new LinkList();
  for (i=0 ; i<fBn.getLinkList().size() ; i++) {
    link = fBn.getLinkList().elementAt(i);
    tail = link.getTail();
    head = link.getHead();
    if ((Bnnl.getId(tail)!=-1) && (Bnnl.getId(head)!=-1))
      Bnll.insertLink(link);
  }
  g = new Graph(Bnnl,Bnll,0);
  Bn = new Bnet();
  Bn.setName(fBn.getName()+"Split2");
  Bn.setNodeList(g.getNodeList());
  Bn.setLinkList(g.getLinkList());
  
  // Dep
  System.out.println("Obtaining second network...");
  // Fin DEP
  
  // Build the list of relations.
  // The potentials of nodes which parents are different of those in the
  // original network, are computed. The rest are copied directly from the
  // original network.
  for (i=0 ; i<g.getNodeList().size() ; i++) {
    tail = g.getNodeList().elementAt(i);
    Bnnl = new NodeList();
    Bnnl.insertNode(tail);
    for (int k = 0 ; k<i ; k++)
      if (g.getLinkList().parent(g.getNodeList().elementAt(k),tail))
	Bnnl.insertNode(g.getNodeList().elementAt(k));
    Bnr = new Relation();
    
    // Create or copy the potential
    // Compute the new list of parents
    NodeList parents = new NodeList();
    for (int k = 1 ; k<Bnnl.size() ; k++)
      parents.insertNode(Bnnl.elementAt(k));
    
    if (parents.equals(Bnnl.elementAt(0).getParentNodes()))
      // Copy the potential
      p = (PotentialTable)(fBn.getRelation(tail).getValues()).copy();
    else
      // Compute the potential
      p = getPotentialTable(fBn,Bnnl);
    
    Bnnl = new NodeList(p.getVariables());
    Bnr.setVariables(Bnnl);
    Bnr.setName(Bnnl.toString2());
    Bnr.setValues(p);
    Bn.addRelation(Bnr);
  }
  net.addElement(Bn);
  
  return net;
}


/**
 * Splits randomly a Bayesian network. Creates a vector containing two new
 * marginalizatedBayesian networks with a percentage of the original nodes
 * and sharing a percentage of nodes.
 *
 * @param pcNodes an int indicating the percentage of nodes.
 * @param pcShared an int indicating the percentage of shared nodes.
 * @return a <code>Vector</code> with two new networks.
 */

public Vector randomSplit (int pcNodes, int pcShared) throws InvalidEditException {

  if ((pcNodes<0) || (pcNodes>100)) {
    System.out.println(" porcentaje de nodos incorrecto ");
    System.exit(1);
  }
  
  if ((pcShared<0) || (pcShared>100)) {
    System.out.println(" porcentaje de nodos comunes incorrecto ");
    System.exit(1);
  }
  
  int i, j, pos, lim;
  Node node, tail;
  NodeList Bnnl, Bn1nl, Bn2nl, sorted;
  Bnet Bn;
  Random generator = new Random();
  Vector net = new Vector();
  Graph g;
  
  sorted = fBn.topologicalOrder();
  
  
  // Generate the lists of nodes
  lim = sorted.size()*pcNodes/100;
  Bn1nl = new NodeList();
  for (i=0 ; i<lim ; i++) {
    pos = (int) (generator.nextDouble()*(sorted.size()-1));
    node = sorted.elementAt(pos);
    Bn1nl.insertNode(node);
    sorted.removeNode(node);
  }

  // Add the common nodes
  Bn2nl = new NodeList();
  i = 0;
  while (i < Bn1nl.size()*pcShared/100) {
    pos = (int) (generator.nextDouble()*(Bn1nl.size()-1));
    node = Bn1nl.elementAt(pos);
    if (Bn2nl.getId(node) == -1) {
      Bn2nl.insertNode(node);
      i++;
    }
  }
  
  i = 0;
  while (i < sorted.size()*pcShared/100) {
    pos = (int) (generator.nextDouble()*(sorted.size()-1));
    node = sorted.elementAt(pos);
    if (Bn1nl.getId(node) == -1) {
      Bn1nl.insertNode(node);
      i++;
    }
  }
  
  for (i=0 ; i<sorted.size() ; i++) {
    node = sorted.elementAt(i);
    Bn2nl.insertNode(node);
  }
  
  // Create the new graph marginalizing the initial one to the obtained
  // lists of nodes.
  
  // Obtain the first network.
  g = new Graph();
  g = fBn.marginalization(Bn1nl);
  
  Bn = new Bnet();
  Bn.setName(fBn.getName()+"RSplit1");
  Bn.setNodeList(g.getNodeList());
  Bn.setLinkList(g.getLinkList());
  
  // Dep
  System.out.println("\nGenerating first network...");
  // Fin Dep
  
  // Construct the list of relations
  Relation Bnr;
  PotentialTable p;
  
  // The potentials of nodes which parents are different of those in the
  // original network, are computed. The rest are copied directly from the
  // original network.
  for (i=0 ; i<g.getNodeList().size() ; i++) {
    tail = g.getNodeList().elementAt(i);
    pos = fBn.getNodeList().getId(tail.getName());
    Bnnl = new NodeList();
    Bnnl.insertNode(tail);
    
    for (j=0 ; j<g.getNodeList().size() ; j++)
      if (g.getLinkList().parent(g.getNodeList().elementAt(j), tail))
	Bnnl.insertNode(g.getNodeList().elementAt(j));
    
    // Create or copy the potential
    Bnr = new Relation();
    
    // Compute the new list of parents.
    NodeList parents = new NodeList();
    for (j = 1 ; j<Bnnl.size() ; j++)
      parents.insertNode(Bnnl.elementAt(j));
    
    p = new PotentialTable();
    if (parents.equals(fBn.getNodeList().elementAt(pos).getParentNodes()))
      // Copy the potential
      p = (PotentialTable)(fBn.getRelation(tail).getValues()).copy();
    else
      // Compute the potential
      p = getPotentialTable(fBn, Bnnl);
    Bnnl = new NodeList(p.getVariables());
    Bnr.setVariables(Bnnl);
    Bnr.setName(Bnnl.toString2());
    Bnr.setValues(p);
    Bn.addRelation(Bnr);
  }
  net.addElement(Bn);
  
  // Obtain the second network.
  g = new Graph();
  g = fBn.marginalization(Bn2nl);
  
  Bn = new Bnet();
  Bn.setName(fBn.getName()+"RSplit2");
  Bn.setNodeList(g.getNodeList());
  Bn.setLinkList(g.getLinkList());
  
  // Dep
  System.out.println("\nGenerating second network...");
  // Fin Dep

  // Construct the list of relations
  // The potentials of nodes which parents are different of those in the
  // original network, are computed. The rest are copied directly from the
  // original network.
  for (i=0 ; i<g.getNodeList().size() ; i++) {
    tail = g.getNodeList().elementAt(i);
    pos = fBn.getNodeList().getId(tail.getName());
    Bnnl = new NodeList();
    Bnnl.insertNode(tail);
    for (j=0 ; j<g.getNodeList().size() ; j++)
      if (g.getLinkList().parent(g.getNodeList().elementAt(j), tail))
	Bnnl.insertNode(g.getNodeList().elementAt(j));
    
    // Create or copy the potential
    Bnr = new Relation();
    // Compute the new list of parents
    NodeList parents = new NodeList();
    for (j = 1 ; j<Bnnl.size() ; j++)
      parents.insertNode(Bnnl.elementAt(j));
    p = new PotentialTable();
    if (parents.equals(fBn.getNodeList().elementAt(pos).getParentNodes()))
      // Copy the potential
      p = (PotentialTable)(fBn.getRelation(tail).getValues()).copy();
    else
      // Compute the potential
      p = getPotentialTable(fBn, Bnnl);
    Bnnl = new NodeList(p.getVariables());
    Bnr.setVariables(Bnnl);
    Bnr.setName(Bnnl.toString2());
    Bnr.setValues(p);
    Bn.addRelation(Bnr);
  }
  net.addElement(Bn);
  
  return net;
}


/**
 * Computes the posterior probability distribution of a given NodeList in
 * the specified network. The first variable in the list is considered the
 * objetive variables and the rests of variables are considered as its
 * parents.
 * @param Bn a <code>Bnet</code>. 
 * @param nl a <code>NodeList</code> containing the variables of interest.
 * @return a <code>PotentialTable</code> defining the posterior probability
 * distribution of the specified <code>NodeList</code>.
 */

public PotentialTable getPotentialTable (Bnet Bn, NodeList nl) {
  
  int i, j, pos;
  FiniteStates n;
  NodeList voi = new NodeList();   // variables of interest
  NodeList vars = new NodeList();  // variables of interest belonging to Bn inside nl
  NodeList parents = new NodeList(); // parents of the first variable of interest
  Evidence e;
  VariableElimination ve;
  PotentialTable pt, pve;
  
  pos = Bn.getNodeList().getId(nl.elementAt(0).getName());
  n = (FiniteStates)Bn.getNodeList().elementAt(pos);
  voi.insertNode(n);
  vars.insertNode(nl.elementAt(0));
  
  for (i=1 ; i<nl.size() ; i++) {
    pos = Bn.getNodeList().getId(nl.elementAt(i).getName());
    if (pos != -1) {
      parents.insertNode(Bn.getNodeList().elementAt(pos));
      voi.insertNode(Bn.getNodeList().elementAt(pos));
      vars.insertNode(nl.elementAt(i));
    }
  }
  
  pt = new PotentialTable(voi);
  if (parents.size() == 0) {
    e = new Evidence();
    ve = new VariableElimination(Bn,e);
    ve.getPosteriorDistributionOf(n);
    ve.normalizeResults();
    pt = (PotentialTable)ve.getResults().elementAt(0);
    
  }
  else {
    // Explore configurations
    Configuration confp = new Configuration (parents);
    
    for (i=0 ; i<parents.getSize() ; i++) {
      e = new Evidence (confp);
      ve = new VariableElimination (Bn,e);
      ve.getPosteriorDistributionOf(n);
      ve.normalizeResults();
      pve = (PotentialTable)ve.getResults().elementAt(0);
      Configuration confve = new Configuration (pve.getVariables());
      for (j=0 ; j<pve.getSize() ; j++) {
	pt.setValue(new Configuration(confve,confp,voi),pve.getValue(confve));
	confve.nextConfiguration();
      }
      confp.nextConfiguration();
    }
  }
  pt.setVariables(vars.getNodes());
  
  return pt;
}


/**
 * Obtains the linear pool of two Bayesian networks.
 * @param qualitativeType is an int indicating the type of qualitative fusion
 * that is going to be applied in order to obtain the structure of the fusion
 * network.
 * @param Bn1 is the first Bayesian network implied in the fusion process.
 * @param Bn2 is the second Bayesian network implied in the fusion process.
 */

public void linearPool (int qualitativeType, Bnet Bn1, Bnet Bn2) throws InvalidEditException {
  
  int i, j, pos1, pos2;
  FiniteStates n;
  NodeList nl;
  Link l;
  Relation r;
  PotentialTable p;
  
  fBn = new Bnet();
  
  // Create graph
  Graph gfBn = new Graph(Bn1);
  switch (qualitativeType) {
  case 0: // Union
    gfBn = gfBn.union(Bn2);
    fBn.setComment("Lineal Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph union.");
    break;
  case 1: // Intersection
    gfBn = gfBn.intersection(Bn2);
    fBn.setComment("Lineal Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph intersection.");
    break;
  case 2: // Intersection Extended
    gfBn = gfBn.intersectionExtended(Bn2);
    fBn.setComment("Lineal Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph intersection extended.");
    break;
  case 3: // Maximal
    gfBn = gfBn.maximal(Bn2);
    fBn.setComment("Linear Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using maximal graph combination.");
    break;
  }
  
  
  fBn.setName(Bn1.getName()+Bn2.getName());
  fBn.setNodeList(gfBn.topologicalOrder());
  fBn.setLinkList(gfBn.getLinkList());
  
  // Create relations
  for (i=0 ; i<fBn.getNodeList().size() ; i++) {
    n = (FiniteStates) fBn.getNodeList().elementAt(i);
    
    // Construct list of variables associated to relation
    nl = new NodeList();
    nl.insertNode(n);
    for (j=0 ; j<fBn.getLinkList().size() ; j++) {
      l = fBn.getLinkList().elementAt(j);
      if (n.compareTo(l.getHead()) == 0)
	nl.insertNode((FiniteStates)l.getTail());
    }
    
    // Constructs potential
    pos1 = Bn1.getNodeList().getId(n.getName());
    pos2 = Bn2.getNodeList().getId(n.getName());
    
    if (pos1 == -1) {
      // Proper variable of Bn2
      if (((FiniteStates)Bn2.getNodeList().elementAt(pos2)).getParentNodes().size() == nl.size()-1) {
	// Copy relation
	r = Bn2.getRelation(Bn2.getNodeList().elementAt(pos2));
	p = (PotentialTable)(r.getValues()).copy();
      }
      else
	p = getPotentialTable(Bn2,nl);
      }
    else if (pos2 == -1) {
      // Proper variable of Bn1
      if (((FiniteStates)Bn1.getNodeList().elementAt(pos1)).getParentNodes().size() == nl.size()-1) {
	// Copy relation
	r = Bn1.getRelation(Bn1.getNodeList().elementAt(pos1));
	p = (PotentialTable)(r.getValues()).copy();
      }
      else
	p = getPotentialTable(Bn1,nl);
    }
    else {
      // Shared variable
      Vector v = new Vector();
      NodeList parentsBn1 = ((FiniteStates)Bn1.getNodeList().elementAt(pos1)).getParentNodes();
      NodeList parentsBn2 = ((FiniteStates)Bn2.getNodeList().elementAt(pos2)).getParentNodes();
      NodeList parents = new NodeList();
      parents.merge(nl);
      parents.removeNode(0);
      
      if (parents.size() == 0) {
	p = new PotentialTable();
	v.addElement(getPotentialTable(Bn1,nl));
	v.addElement(getPotentialTable(Bn2,nl));
	p.linearPool(v);
      }
      else {
	parentsBn1 = parentsBn1.intersectionNames(parents);
	parentsBn2 = parentsBn2.intersectionNames(parents);
	if (((parentsBn1.size()>0) && (parentsBn2.size()>0)) || ((parentsBn1.size()==0) && (parentsBn2.size()==0))) {
	  p = new PotentialTable();
	  v.addElement(getPotentialTable(Bn1,nl));
	  v.addElement(getPotentialTable(Bn2,nl));
	  p.linearPool(v);
	}
	else if (parentsBn1.size() == 0) {
	  r = Bn2.getRelation(Bn2.getNodeList().elementAt(pos2));
	  p = (PotentialTable)(r.getValues()).copy();
	}
	else {
	  r = Bn1.getRelation(Bn1.getNodeList().elementAt(pos1));
	  p = (PotentialTable)(r.getValues()).copy();
	}
      }
    } // end nested if
    
    // Obtain the list of variables of the computed potential and
    // establish the corresponding relation.
    nl = new NodeList(p.getVariables());
    r = new Relation();
    r.setName(nl.toString2());
    r.setVariables(nl);
    r.setValues(p);
    fBn.addRelation(r);
  } // end for i
}


/**
 * Obtains the logarithmic pool of two Bayesian networks.
 * @param qualitativeType is an int indicating the type of qualitative fusion
 * that is going to be applied in order to obtain the structure of the fusion
 * network.
 * @param Bnet1 is the first Bayesian network implied in the fusion process.
 * @param Bnet2 is the second Bayesian network implied in the fusion process.
 */

public void logarithmicPool (int qualitativeType, Bnet Bn1, Bnet Bn2) throws InvalidEditException {
  
  int i, j, pos1,pos2;
  FiniteStates n;
  NodeList nl;
  Link l;
  Relation r;
  PotentialTable p;
  
  fBn = new Bnet();
  
  // Create graph
  Graph gfBn = new Graph(Bn1);
  switch (qualitativeType) {
  case 0: // Union
    gfBn = gfBn.union(Bn2);
    fBn.setComment("Logarithmic Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph union.");
    break;
  case 1: // Intersection
    gfBn = gfBn.intersection(Bn2);
    fBn.setComment("Logarithmic Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph intersection.");
    break;
  case 2: // Intersection Extended
    gfBn = gfBn.intersectionExtended(Bn2);
    fBn.setComment("Logarithmic Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph intersection extended.");
    break;
  case 3: // Maximal
    gfBn = gfBn.maximal(Bn2);
    fBn.setComment("Logarithmic Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using maximal graph combination.");
    break;
  }
  
  fBn.setName(Bn1.getName()+Bn2.getName());
  fBn.setNodeList(gfBn.topologicalOrder());
  fBn.setLinkList(gfBn.getLinkList());
  
  // Create relations
  for (i=0 ; i<fBn.getNodeList().size() ; i++) {
    n = (FiniteStates) fBn.getNodeList().elementAt(i);
    
    // Construct list of variables associated to relation
    nl = new NodeList();
    nl.insertNode(n);
    for (j=0 ; j<fBn.getLinkList().size() ; j++) {
      l = fBn.getLinkList().elementAt(j);
      if (n.compareTo(l.getHead()) == 0)
	nl.insertNode((FiniteStates)l.getTail());
    }
    
    // Construct potential
    pos1 = Bn1.getNodeList().getId(n.getName());
    pos2 = Bn2.getNodeList().getId(n.getName());
    
    if (pos1 == -1) {
      // Proper variable of Bn2
      if (((FiniteStates)Bn2.getNodeList().elementAt(pos2)).getParentNodes().size() == nl.size()-1) {
	// Copy relation
	r = Bn2.getRelation(Bn2.getNodeList().elementAt(pos2));
	p = (PotentialTable)(r.getValues()).copy();
      }
      else
	p = getPotentialTable(Bn2,nl);
    }
    else if (pos2 == -1) {
      // Proper variable of Bn1
      if (((FiniteStates)Bn1.getNodeList().elementAt(pos1)).getParentNodes().size() == nl.size()-1) {
	// Copy relation
	r = Bn1.getRelation(Bn1.getNodeList().elementAt(pos1));
	p = (PotentialTable)(r.getValues()).copy();
      }
      else
	p = getPotentialTable(Bn1,nl);
    }
    else {
      // Shared variable
      Vector v = new Vector();
      NodeList parentsBn1 = ((FiniteStates)Bn1.getNodeList().elementAt(pos1)).getParentNodes();
      NodeList parentsBn2 = ((FiniteStates)Bn2.getNodeList().elementAt(pos2)).getParentNodes();
      NodeList parents = new NodeList();
      parents.merge(nl);
      parents.removeNode(0);
      
      if (parents.size() == 0) {
	p = new PotentialTable();
	v.addElement(getPotentialTable(Bn1,nl));
	v.addElement(getPotentialTable(Bn2,nl));
	p.logarithmicPool(v);
      }
      else {
	parentsBn1 = parentsBn1.intersectionNames(parents);
	parentsBn2 = parentsBn2.intersectionNames(parents);
	if (((parentsBn1.size()>0) && (parentsBn2.size()>0)) || ((parentsBn1.size()==0) && (parentsBn2.size()==0))) {
	  p = new PotentialTable();
	  v.addElement(getPotentialTable(Bn1,nl));
	  v.addElement(getPotentialTable(Bn2,nl));
	  p.logarithmicPool(v);
	}
	else if (parentsBn1.size() == 0) {
	  r = Bn2.getRelation(Bn2.getNodeList().elementAt(pos2));
	  p = (PotentialTable)(r.getValues()).copy();
	}
	else {
	  r = Bn1.getRelation(Bn1.getNodeList().elementAt(pos1));
	  p = (PotentialTable)(r.getValues()).copy();
	}
      }
    } // end nested if
    
    // Obtain the list of variables of the calculated potential and
    // establish the corresponding relation.
    nl = new NodeList(p.getVariables());
    r = new Relation();
    r.setName(nl.toString2());
    r.setVariables(nl);
    
    r.setValues(p);
    fBn.addRelation(r);
  } // end for i
}


/**
 * Obtains the noisy-OR pool of two Bayesian networks.
 * NOTE: This procedure must only be used if ALL the nodes are noisy-OR nodes.
 * @param qualitativeType is an int indicating the type of qualitative fusion
 * that is going to be applied in order to obtain the structure of the fusion
 * network.
 * @param Bnet1 is the first Bayesian network implied in the fusion process.
 * @param Bnet2 is the second Bayesian network implied in the fusion process.
 * @param loose a boolean value indicating if we have to compute a loose
 * probability term or not.
 */

public void noisyORPool (int qualitativeType, Bnet Bn1, Bnet Bn2, boolean loose) throws InvalidEditException {
  
  int i, j, k, pos1, pos2;
  FiniteStates n;
  NodeList nl;
  Link l;
  Relation r;
  PotentialTable p;
  
  fBn = new Bnet();
  
  // Create graph
  Graph gfBn = new Graph(Bn1);
  switch (qualitativeType) {
  case 0: // Union
    gfBn = gfBn.union(Bn2);
    fBn.setComment("Noisy-OR Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph union.");
    break;
  case 1: // Intersection
    gfBn = gfBn.intersection(Bn2);
    fBn.setComment("Noisy-OR Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph intersection.");
    break;
  case 2: // Intersection Extended
    gfBn = gfBn.intersectionExtended(Bn2);
    fBn.setComment("Noisy-OR Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph intersection extended.");
    break;
  case 3: // Maximal
    gfBn = gfBn.maximal(Bn2);
    fBn.setComment("Noisy-OR Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using maximal graph combination.");
    break;
  }
  
  fBn.setName(Bn1.getName()+Bn2.getName());
  fBn.setNodeList(gfBn.topologicalOrder());
  fBn.setLinkList(gfBn.getLinkList());
  
  // Create relations
  for (i=0 ; i<fBn.getNodeList().size() ; i++) {
    n = (FiniteStates) fBn.getNodeList().elementAt(i);
    
    // Construct list of variables associated to relation
    nl = new NodeList();
    nl.insertNode(n);
    for (j=0 ; j<fBn.getLinkList().size() ; j++) {
      l = fBn.getLinkList().elementAt(j);
      if (n.compareTo(l.getHead()) == 0)
	nl.insertNode((FiniteStates)l.getTail());
    }
    
    // Construct potential
    pos1 = Bn1.getNodeList().getId(n.getName());
    pos2 = Bn2.getNodeList().getId(n.getName());
    
    if (pos1 == -1) {
      // Proper variable of Bn2
      if (((FiniteStates)Bn2.getNodeList().elementAt(pos2)).getParentNodes().size() == nl.size()-1) {
	// Copy relation
	r = Bn2.getRelation(Bn2.getNodeList().elementAt(pos2));
	p = (PotentialTable)(r.getValues()).copy();
      }
      else
	p = getPotentialTable(Bn2,nl);
    }
    else if (pos2 == -1) {
      // Proper variable of Bn1
      if (((FiniteStates)Bn1.getNodeList().elementAt(pos1)).getParentNodes().size() == nl.size()-1) {
	// Copy relation
	r = Bn1.getRelation(Bn1.getNodeList().elementAt(pos1));
	p = (PotentialTable)(r.getValues()).copy();
      }
      else
	p = getPotentialTable(Bn1,nl);
    }
    else {
      // Shared variable
      Vector v = new Vector();
      NodeList parentsBn1 = ((FiniteStates)Bn1.getNodeList().elementAt(pos1)).getParentNodes();
      NodeList parentsBn2 = ((FiniteStates)Bn2.getNodeList().elementAt(pos2)).getParentNodes();
      NodeList parents = new NodeList();
      parents.merge(nl);
      parents.removeNode(0);
      
      p = new PotentialTable();
      
      if (parents.size() == 0) {
	v.addElement(getPotentialTable(Bn1,nl));
	v.addElement(getPotentialTable(Bn2,nl));
	p.linearPool(v);
      }
      else {
	// Obtain a vactor with potentials of the variable given
	// each one of the causes individually
	FiniteStates c, naux;
	NodeList vars = new NodeList();
	PotentialTable paux;
	boolean found;
	Vector vaux;
	
	vars.insertNode(n);
	for (j=0 ; j<parents.size() ; j++) {
	  naux = ((FiniteStates) parents.elementAt(j));
	  vaux = new Vector();
	  
	  found = false;
	  for (k=0 ; (k<parentsBn1.size()) && (!found); k++) {
	    c = ((FiniteStates) parentsBn1.elementAt(k));
	    if (naux.compareTo(c) == 0) {
	      found = true;
	      vars.insertNode(c);
	      vaux.addElement(getPotentialTable(Bn1,vars));
	      vars.removeNode(1);
	    }
	  }
	  
	  found = false;
	  for (k=0 ; (k<parentsBn2.size()) && (!found); k++) {
	    c = ((FiniteStates) parentsBn2.elementAt(k));
	    if (naux.compareTo(c) == 0) {
	      found = true;
	      vars.insertNode(c);
	      vaux.addElement(getPotentialTable(Bn2,vars));
	      vars.removeNode(1);
	    }
	  }
	  
	  paux = new PotentialTable();
	  if (vaux.size() > 1)
	    paux.linearPool(vaux);
	  else
	    paux = (PotentialTable)((PotentialTable)(vaux.elementAt(0))).copy();
	  
	  v.addElement(paux);
	}
	
	p.noisyORPool(v,loose);
      }
      
    } // end nested if

    // Obtain the list of variables of the calculated potential and
    // establish the corresponding relation.
    nl = new NodeList(p.getVariables());
    r = new Relation();
    r.setName(nl.toString2());
    r.setVariables(nl);
    
    r.setValues(p);
    fBn.addRelation(r);
  } // end for i
}


/**
 * Obtains the noisy-OR pool of two Bayesian networks.
 * @param qualitativeType is an int indicating the type of qualitative fusion
 * that is going to be applied in order to obtain the structure of the fusion
 * network.
 * @param Bn1 is the first Bayesian network implied in the fusion process.
 * @param Bn2 is the second Bayesian network implied in the fusion process.
 * @param ORnodes is the list of common noisyOR nodes (the noisy OR combination
 * only applies to this list).
 * @param loose a boolean value indicating if we have to compute a loose
 * probability term or not.
 */

public void noisyORPool (int qualitativeType, Bnet Bn1, Bnet Bn2, NodeList ORnodes, boolean loose) throws InvalidEditException {
  
  int i, j, k, pos1, pos2;
  FiniteStates n;
  NodeList nl;
  Link l;
  Relation r;
  PotentialTable p;
  
  fBn = new Bnet();
  
  // Create graph
  Graph gfBn = new Graph(Bn1);
  switch (qualitativeType) {
  case 0: // Union
    gfBn = gfBn.union(Bn2);
    fBn.setComment("Noisy-OR Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph union.");
    break;
  case 1: // Intersection
    gfBn = gfBn.intersection(Bn2);
    fBn.setComment("Noisy-OR Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph intersection.");
    break;
  case 2: // Intersection Extended
    gfBn = gfBn.intersectionExtended(Bn2);
    fBn.setComment("Noisy-OR Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using graph intersection extended.");
    break;
  case 3: // Maximal
    gfBn = gfBn.maximal(Bn2);
    fBn.setComment("Noisy-OR Pool of "+Bn1.getName()+" and "+Bn2.getName()+" using maximal graph combination.");
    break;
  }
  
  fBn.setName(Bn1.getName()+Bn2.getName());
  fBn.setNodeList(gfBn.topologicalOrder());
  fBn.setLinkList(gfBn.getLinkList());
  
  // Create relations
  for (i=0 ; i<fBn.getNodeList().size() ; i++) {
    n = (FiniteStates) fBn.getNodeList().elementAt(i);
    
    // Construct list of variables associated to relation
    nl = new NodeList();
    nl.insertNode(n);
    for (j=0 ; j<fBn.getLinkList().size() ; j++) {
      l = fBn.getLinkList().elementAt(j);
      if (n.compareTo(l.getHead()) == 0)
	nl.insertNode((FiniteStates)l.getTail());
    }
    
    // Construct potential
    pos1 = Bn1.getNodeList().getId(n.getName());
    pos2 = Bn2.getNodeList().getId(n.getName());
    
    if (pos1 == -1) {
      // Proper variable of Bn2
      if (((FiniteStates)Bn2.getNodeList().elementAt(pos2)).getParentNodes().size() == nl.size()-1) {
	// Copy relation
	r = Bn2.getRelation(Bn2.getNodeList().elementAt(pos2));
	p = (PotentialTable)(r.getValues()).copy();
      }
      else
	p = getPotentialTable(Bn2,nl);
    }
    else if (pos2 == -1) {
      // Proper variable of Bn1
      if (((FiniteStates)Bn1.getNodeList().elementAt(pos1)).getParentNodes().size() == nl.size()-1) {
	// Copy relation
	r = Bn1.getRelation(Bn1.getNodeList().elementAt(pos1));
	p = (PotentialTable)(r.getValues()).copy();
      }
      else
	p = getPotentialTable(Bn1,nl);
    }
    else {
      // Shared variable
      Vector v = new Vector();
      NodeList parentsBn1 = ((FiniteStates)Bn1.getNodeList().elementAt(pos1)).getParentNodes();
      NodeList parentsBn2 = ((FiniteStates)Bn2.getNodeList().elementAt(pos2)).getParentNodes();
      NodeList parents = new NodeList();
      parents.merge(nl);
      parents.removeNode(0);
      
      p = new PotentialTable();
      
      if ((parents.size()==0) || (ORnodes.getId(n.getName())!=-1)) {
	v.addElement(getPotentialTable(Bn1,nl));
	v.addElement(getPotentialTable(Bn2,nl));
	p.linearPool(v);
      }
      else {
	// Obtain a vector with potentials of the variable given
	// each one of the causes.
	FiniteStates c, naux;
	NodeList vars = new NodeList();
	PotentialTable paux;
	boolean found;
	Vector vaux;
	
	vars.insertNode(n);
	for (j=0 ; j<parents.size() ; j++) {
	  naux = ((FiniteStates) parents.elementAt(j));
	  vaux = new Vector();
	  
	  found = false;
	  for (k=0 ; (k<parentsBn1.size()) && (!found) ; k++) {
	    c = ((FiniteStates) parentsBn1.elementAt(k));
	    if (naux.compareTo(c) == 0) {
	      found = true;
	      vars.insertNode(c);
	      vaux.addElement(getPotentialTable(Bn1,vars));
	      vars.removeNode(1);
	    }
	  }
	  
	  found = false;
	  for (k=0 ; (k<parentsBn2.size()) && (!found) ; k++) {
	    c = ((FiniteStates) parentsBn2.elementAt(k));
	    if (naux.compareTo(c) == 0) {
	      found = true;
	      vars.insertNode(c);
	      vaux.addElement(getPotentialTable(Bn2,vars));
	      vars.removeNode(1);
	    }
	  }
	  
	  paux = new PotentialTable();
	  if (vaux.size() > 1)
	    paux.linearPool(vaux);
	  else
	    paux = (PotentialTable)((PotentialTable)(vaux.elementAt(0))).copy();
	  
	  v.addElement(paux);
	}
	
	p.noisyORPool(v,loose);
      }
    } // end nested if
    
    // Obtain the list of variables of the calculated potential
    // and establish the corresponding relation.
    nl = new NodeList(p.getVariables());
    r = new Relation();
    r.setName(nl.toString2());
    r.setVariables(nl);
    
    r.setValues(p);
    fBn.addRelation(r);
  } // end for i
}


/**
 * WARNING: In order to the comparison be correct, the lists of parents,
 * children, etc. in the <code>Fusion</code> object must have been created.
 * This is only achieved when the network resulting from the fusion is
 * read from a file and the <code>Fusion</code> object is created from
 * that network.
 *
 * Compares this network with another Bayesian network. The comparison
 * is done counting coincident links, inverted links, added links and
 * computing the Kullback-Leibler divergence between the joint distributions.
 * The unique requisite between the networks being compared is that they have
 * the same set of variables.
 *
 * @param Bn a Bayesian net to be compared with this.
 */

public Vector compare (Bnet Bn) {
  
  int i, j;
  Link link;
  
  NodeList common = new NodeList();
  common.merge(fBn.getNodeList());
  common.intersectionNames(Bn.getNodeList());
  
  if (common.size() != fBn.getNodeList().size()) {
    System.out.println(" error distinct node lists ");
    System.exit(1);
  }
  
  LinkList coincident = new LinkList();
  LinkList inverted = new LinkList();
  LinkList added = new LinkList();
  
  LinkList links = fBn.getLinkList();
  LinkList BnLinks = Bn.getLinkList();
  
  FiniteStates n;
  PotentialTable pfBn, pBn;
  
  Vector comparison = new Vector();
  
  // Comparar la parte cualitativa
  for (i=0 ; i<BnLinks.size() ; i++) {
    link = BnLinks.elementAt(i);
    if (links.getID(link.getTail().getName(), link.getHead().getName()) != -1)
      coincident.insertLink(link);
    else if (links.getID(link.getHead().getName(), link.getTail().getName())!=-1)
      inverted.insertLink(link);
    else added.insertLink(link);
  }
  
  comparison.addElement(new Integer(coincident.size()));
  comparison.addElement(new Integer(inverted.size()));
  comparison.addElement(new Integer(added.size()));
  
  // Dep
  System.out.println("Original links = "+links.size());
  System.out.println("Coincident links = "+coincident.size());
  System.out.println("Inverted links   = "+inverted.size());
  System.out.println("Added links      = "+added.size());
  // Fin DEP
  
  // Compares the quantitative part
  comparison.addElement(new Double(crossEntropy(Bn)));
  
  System.out.println("KL-divergence = "+((Double)comparison.elementAt(3)).doubleValue());
  
  return comparison;
}


/**
 * Method to compute the Kullback-Leibler divergence between the
 * distributions encoded in two bayesian networks defined on the
 * same set of variables. Used by method <code>compare</code>.
 * @param Bn a Bayesian network.
 * @return the cross entropy between this network and <code>Bn</code>.
 */

public double crossEntropy (Bnet Bn) {
  
  int i, j;
  FiniteStates n;
  NodeList parents;
  double sumconfp, sum = 0.0;
  PotentialTable weight, p1, p2;
  Relation r;
  
  for (i=0 ; i<fBn.getNodeList().size() ; i++) {
    n = (FiniteStates) fBn.getNodeList().elementAt(i);
    parents = n.getParentNodes();
    if (parents.size() > 0) {
      sumconfp = 0.0;
      weight = getPotentialTable(fBn, parents);
      r = fBn.getRelation(n);
      p1 = (PotentialTable) r.getValues();
      p2 = getPotentialTable (Bn, r.getVariables());
      Configuration confp = new Configuration(parents);
      for (j=0 ; j<parents.getSize() ; j++)
	sumconfp += weight.getValue(confp) * distance (p1,p2,confp);
      sum += sumconfp;
        }
  }
  return sum;
}


/**
 * Private method to compute the Kullback-Leibler divergence between two
 * conditional probability distributions defined on the same set of variables.
 * Used by method <code>crossEntropy</code>.
 * @param pt1 a <code>PotentialTable</code>.
 * @param pt2 a <code>PotentialTable</code>.
 * @param conf a configuration for which both potentials will be evaluated.
 */

private double distance (PotentialTable pt1, PotentialTable pt2,
			 Configuration conf) {
  
  int k, noc;
  double pr1, pr2, divergence = 0.0;
  
  noc = ((FiniteStates)pt1.getVariables().elementAt(0)).getNumStates();
  Configuration conf1 = new Configuration (pt1.getVariables(), conf, true);
  Configuration conf2 = new Configuration (pt2.getVariables(), conf, true);
  for (k=0 ; k<noc ; k++) {
    conf1.putValue(conf1.getVariable(0), k);
    conf2.putValue(conf2.getVariable(0), k);
    pr1 = pt1.getValue(conf1);
    pr2 = pt2.getValue(conf2);
    
    if (pr1 != 0.0)
      divergence += pr1 * Math.log(pr1/pr2);
    else
      divergence += 0.0;
  }
  
  return divergence;
}


/**
 * Saves a this fusion object as a Bayesian network.
 */

public void save (FileWriter f) throws IOException {
  
  fBn.saveBnet(f);
}


/**
 * Shows this fusion object on a text screen. 
 */

public void print() {
  
  int i;
  Link l;
  Relation r;
  
  System.out.println("Nodes = "+fBn.getNodeList().toString2());
  System.out.print("Links = ");
  for (i=0 ; i<fBn.getLinkList().size() ; i++) {
    l = fBn.getLinkList().elementAt(i);
    System.out.print("("+l.getTail().getName()+","+l.getHead().getName()+") ");
  }
  System.out.println("\nRelations");
  for (i=0 ; i<fBn.getRelationList().size() ; i++) {
    r = (Relation)fBn.getRelationList().elementAt(i);
    ((PotentialTable)r.getValues()).print();
  }
}

} // End of class.