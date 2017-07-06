/*
 * CredalSet.java
 *
 * Created on 29 de abril de 2004, 13:42
 */

package elvira.potential;

import java.util.Vector;
import elvira.Configuration;

/**
 * This interface is implemented by Potentials representing credal sets
 * @author  Andrés Cano Utrera (acu@decsai.ugr.es)
 */
public interface CredalSet {
  Vector getListNonTransparents();
  Vector getListTransparents();
  double getValue(Configuration conf);
  double getMinimum(Configuration subconf);
  double getMaximum(Configuration subconf);
}
