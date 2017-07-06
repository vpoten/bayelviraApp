/* SetVectorOperations.java */
package elvira;

import java.util.Vector;

/**
 * This class contains some useful set operations implemented for
 * class <code>Vector</code>.
 *
 * @since 25/9/2000
 */
public class SetVectorOperations {

   /**
    * Gets the union between two vectors.
    * @param a a <code>Vector</code>.
    * @param b a <code>Vector</code>.
    * @return a <code>Vector</code> with the union of <code><</code> and
    * <code>b</code>.
    */
   static public Vector union(Vector a, Vector b) {

      Vector aux = new Vector();
      int i;

      aux = (Vector) a.clone();
      for (i = 0; i < b.size(); i++) {
         if (!(aux.contains(b.elementAt(i)))) {
            aux.addElement(b.elementAt(i));
         }
      }

      return aux;
   }

   /**
    * Gets the intersection between two vectors. 
    * We suppose that both vectors represent disjoints sets.
    * @param a a <code>Vector</code>.
    * @param b a <code>Vector</code>.
    * @return a <code>Vector</code> with the intersection of <code><</code> and
    * <code>b</code>.
    */
   static public Vector intersection(Vector a, Vector b) {

      Vector aux = new Vector();
      int i, j;

      for (i = 0; i < a.size(); i++) {
         if (b.contains(a.elementAt(i))) {
            aux.addElement(a.elementAt(i));
         }
      }

      return aux;
   }

   /**
    * Computes the elements of a vector that are not contained in
    * another one.
    * @param a a <code>Vector</code>.
    * @param b a <code>Vector</code>.
    * @return the elements of <code>a</code> that are not in <code>b</code>.
    */
   static public Vector notIn(Vector a, Vector b) {

      Vector aux = new Vector();
      int i, j;

      for (i = 0; i < a.size(); i++) {
         if (!b.contains(a.elementAt(i))) {
            aux.addElement(a.elementAt(i));
         }
      }

      return aux;
   }
} // End of class.
