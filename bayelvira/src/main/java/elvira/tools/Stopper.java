/* Stopper.java */

package elvira.tools;


import java.io.*;


/**
 * Allows to stop the execution until a key is pressed.
 * Operations:
 * <ul>
 * <li> Constructor
 * <li> read: Makes the execution stop until a key is pressed
 *
 * @author Manuel Gomez
 * @since 10/06/2003
 */

public class Stopper {

/**
 * BufferedReader used to wait for key pressing 
 */
private BufferedReader entrada;


/**
 * Constructor for Stopper 
 */

public Stopper() {
  entrada=new BufferedReader(new InputStreamReader(System.in));
}

  
/**
 * Waits until a key is pressed
 */

public void read() {
  try{
    entrada.read();
  }catch(IOException e){
    System.out.println("Problem when waiting for a key to be pressed");
    System.exit(-1);
  }
}

} // End of class
