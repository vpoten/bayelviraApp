/*MyFileFilter.java*/

package elvira.sensitivityAnalysis;

import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.filechooser.FileFilter;

/**
 * Clase para crear un filtro para seleccion de archivos con extension .elv
 * @author jruiz
 * @version 1.0
 */
public class MyFileFilter extends FileFilter {

  private static String TYPE_UNKNOWN = "Type Unknown";
  private static String HIDDEN_FILE = "Hidden File";
  private Hashtable filters = null;
  private String description = null;
  private String fullDescription = null;
  private boolean useExtensionsInDescription = true;

  /**
   * Constructor por defecto.
   */
  public MyFileFilter() {

    this.filters = new Hashtable();
  }

  /**
   * Crear un filtro con una determinada extension.
   * @param extension Es la extension.
   */
  public MyFileFilter(String extension) {

    this(extension,null);
  }

  /**
   * Crea un filtro con una extension y una descripcion.
   * @param extension Es la extension.
   * @param description Es la descripcion.
   */
  public MyFileFilter(String extension, String description) {

    this();

    if(extension!=null) {
      addExtension(extension);
    }

    if(description!=null) {
      setDescription(description);
    }

  }

  /**
   * Crea un filtro con una serie de extensiones.
   * @param filters Array de extensiones.
   */
  public MyFileFilter(String[] filters) {

    this(filters, null);
  }

  /**
   * Crea un filtro con una serie de extensiones y una descripcion.
   * @param filters Array de extensiones.
   * @param description Descripcion.
   */
  public MyFileFilter(String[] filters, String description) {

    this();

    for (int i = 0; i < filters.length; i++) {
      // add filters one by one
      addExtension(filters[i]);
    }

    if(description!=null) setDescription(description);

  }

  /**
   * ¿Acepta Extension?.
   * @param f
   * @return
   */
  public boolean accept(File f) {

    if(f != null) {

      if(f.isDirectory()) {
        return true;
      }

      String extension = getExtension(f);

      if(extension != null && filters.get(getExtension(f)) != null) {
        return true;
      };

    }

    return false;
  }

  /**
   * Extrae extension.
   * @param f
   * @return
   */
  public String getExtension(File f) {

    if(f != null) {
      String filename = f.getName();
      int i = filename.lastIndexOf('.');

      if(i>0 && i<filename.length()-1) {
        return filename.substring(i+1).toLowerCase();
      };

    }

    return null;
  }

  /**
   * Anade extension.
   * @param extension
   */
  public void addExtension(String extension) {

    if(filters == null) {
      filters = new Hashtable(5);
    }

    filters.put(extension.toLowerCase(), this);
    fullDescription = null;
  }

  /**
   * Extrae descripcion.
   * @return
   */
  public String getDescription() {

    if(fullDescription == null) {

      if(description == null || isExtensionListInDescription()) {
        fullDescription = description==null ? "(" : description + " (";
            // build the description from the extension list
        Enumeration extensions = filters.keys();

        if(extensions != null) {
          fullDescription += "." + (String) extensions.nextElement();

          while (extensions.hasMoreElements()) {
            fullDescription += ", ." + (String) extensions.nextElement();
          }

        }

        fullDescription += ")";
      } else {
        fullDescription = description;
      }

    }

    return fullDescription;
  }

  /**
   * Pone descripcion.
   * @param description
   */
  public void setDescription(String description) {

    this.description = description;
    fullDescription = null;
  }

  /**
   *
   * @param b
   */
  public void setExtensionListInDescription(boolean b) {

    useExtensionsInDescription = b;
    fullDescription = null;
  }

  /**
   *
   * @return
   */
  public boolean isExtensionListInDescription() {

    return useExtensionsInDescription;
  }

} //End of class
