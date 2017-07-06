/*TestParser.java*/

package elvira.parser;

import java.io.FileInputStream;
import java.io.IOException;
import elvira.parser.ParseException;
import java.io.File;
import java.io.FileFilter;

/**
 * Esta clase se encarga de realizar tests del parser.
 * @author Jacinto Ruiz
 * @version 1.0
 */
public class TestParser {

  public TestParser(String fileName) throws ParseException,IOException {

    FileInputStream f;

    f = new FileInputStream(fileName);
    BayesNetParse parser = new BayesNetParse(f);
    parser.initialize();
    parser.CompilationUnit();
  }

  public static void main(String[] args) {

    TestParser testParser;
    File path;
    TestFileFilter filter;
    File[] listaFicheros;
    File fileName = new File("");
    String[] resultado;

    path = new File("elvira/parser/tests/");
    filter = new TestFileFilter();
    listaFicheros = path.listFiles(filter);
    resultado = new String[listaFicheros.length];
    for (int i=0; i < listaFicheros.length; i++) {
      fileName = listaFicheros[i];
      try {
        testParser = new TestParser(fileName.toString());
        resultado[i]="Test satisfactorio de " + fileName;
      }
      catch (IOException e) {
        resultado[i]="El archivo " + fileName + " no existe";
      }
      catch (ParseException e1) {
        resultado[i]="Error al procesar " + fileName;
      }
    }
    System.out.println("");
    System.out.println("Resultados de los test");
    System.out.println("======================");
    System.out.println("");
    for (int j=0; j < resultado.length; j++) {
      System.out.println(resultado[j]);
    }
  }

}//End of class

class TestFileFilter implements FileFilter {

  public boolean accept(File f) {

    if(f != null) {

      if(f.isDirectory()) {
        return true;
      }

      String extension = getExtension(f);

      if(extension != null && getExtension(f).equals("elv")) {
        return true;
      };

    }

    return false;
  }

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

}
