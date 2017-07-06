package elvira;

import java.util.Vector;
import java.util.Hashtable;


/**
 * Implements a focal set for a mass function (basic probability
 * assignment) or any other representation of a
 * Dempster-Shafer multivariate belief function. A focal set is
 * represented as a set of configurations for which the belief
 * function is defined. In order to avoid storing several times
 * the same configuration, they are kept in a hash table, and the
 * focal set actually includes references to the hash table.
 *
 * @author Antonio.Salmeron@ual.es
 * @since 21/5/2002
 */

public class FocalSet {

/**
 * The hash table containing the configurations. This table
 * will be the sample space or universe representing
 * the problem (context) in which the belief function is being
 * used. This focal set is made of configurations contained
 * in this table. The elements stored in this object are of class
 * <code>Configuration</code>.
 */
Hashtable table;

/**
 * The configurations of <code>table</code> that actually belong
 * to this focal set. This vector contains elements of class
 * Integer.
 */
Vector content;


/**
 * Creates an empty focal set.
 */

public FocalSet() {

  table = new Hashtable();
  content = new Vector();
}


/**
 * Creates a focal set for a given reference set of configurations.
 *
 * @param universe the global set of configurations, which is
 * a superset of this focal set.
 */

public FocalSet(Hashtable t) {
 
  table = t;
  content = new Vector();
}


/**
 * Creates a copy of this object. The hash table is shared.
 *
 * @return the new object.
 */

public FocalSet copy() {

  FocalSet newSet;
  int i, s;
  
  newSet = new FocalSet(table);
  s = getSize();
  
  for (i=0 ; i<s ; i++) {
    newSet.content.addElement(new Integer(elementAt(i).intValue()));
  }
  
  return (newSet);
}


/**
 * Gets the number of configurations in this focal set.
 *
 * @return the size of the focal set.
 */

public int getSize() {
  
  return (content.size());
}


/**
 * Determines whether a focal set is a subset of this.
 *
 * @param set the set for which inclusion will be checked..
 * @return true if <code>set</code> is contained in this set,
 * and false otherwise.
 */

public boolean isSubset(FocalSet set) {

  Integer key;
  int i;
  
  for (i=0 ; i<set.getSize() ; i++) {
    key = set.elementAt(i);
    if (!contains(key))
      return (false);
  }
  
  return (true);
}


/**
 * Determines whether a configuration, given as a key, is
 * contained in this set.
 *
 * @param key the hash key of a configuration.
 * @return true if <code>key</code> is contained in this set,
 * and false otherwise.
 */

public boolean contains(Integer key) {

  int i;
  
  for (i=0 ; i<getSize() ; i++) {
    if (key.equals(elementAt(i)))
      return (true);
  }
  
  return (false);
}


/**
 * Retrieves the configuration stored in a given position.
 *
 * @param pos the position of the configuration to retrieve.
 * @return the configuration (its hash key) at position
 * <code>pos</code>.
 */

public Integer elementAt(int pos) {

  return ((Integer)elementAt(pos));
}


/**
 * Adds a configuration to this focal set. The object is modified.
 * @param c the configuration to insert in this focal set.
 */

public void addConfiguration(Configuration c) {

  int code;
  Integer key;
  
  code = c.hashCode();
  key = new Integer(code); // The key for the hash table.
  
  // If the configuration is not in the general hash table,
  // insert it there.
  
  if (!table.containsKey(key))
    table.put(key,c);

  content.addElement(key);
}


/**
 * Retrieves a configuration given its key.
 *
 * @param key the code of the configuration to retrieve.
 * @return the configuration whose key is <code>key</code>.
 */

public Configuration getConfiguration(Integer key) {

  return ((Configuration)table.get(key));
}


/**
 * Computes the union of the receiving focal set and the argument.
 * It is assumed that the universes over which both focal sets
 * are defined are the same. Thus, there is no need to update
 * the corresponding hash tables, and any of both (which are
 * actually the same) will be used for the new focal set.
 *
 * @param f a <code>FocalSet</code>.
 * @return a new <code>FocalSet</code> containing the union of
 * <code>f</code> and this.
 */

public FocalSet union(FocalSet f) {
 
  FocalSet u;
  Integer key;
  int i, j, s;
  
  u = copy();
  
  s = f.getSize();
  
  for (i=0 ; i<s ; i++) {
    key = f.elementAt(i);
    if (!u.contains(key))
      u.content.addElement(key);
  }
  
  return (u);
}


/**
 * Computes the intersecion of the receiving focal set
 * and the argument. It is assumed that the universes over which
 * both focal sets are defined are the same. Thus, there is no
 * need to update the corresponding hash tables, and any of both
 * (which are actually the same) will be used for the new focal set.
 *
 * @param f a <code>FocalSet</code>.
 * @return a new <code>FocalSet</code> containing the intersection
 * of <code>f</code> and this.
 */

public FocalSet intersection(FocalSet f) {
 
  FocalSet inter;
  Integer key;
  int i, j, s;
  
  inter = new FocalSet(table);
  
  s = f.getSize();
  
  for (i=0 ; i<s ; i++) {
    key = f.elementAt(i);
    if (this.contains(key))
      inter.content.addElement(key);
  }
  
  return (inter);
}


/**
 * Projects a focal set over a set of variables.
 *
 * @param v a vector with the variables over which the
 * focal set will be projected.
 * @return a new focal set, resulting from the projection.
 */

public FocalSet projection(Vector v) {
 
  NodeList nl;
  Configuration proj;
  int i, s = getSize();
  Integer key;
  FocalSet pr  = new FocalSet(table);
  
  nl = new NodeList(v);
  
  for (i=0 ; i<s ; i++) {
    key = elementAt(i);
    proj = getConfiguration(key);
    pr.addConfiguration(new Configuration(proj,nl));
  }
  
  return (pr);
}

} // End of class FocalSet