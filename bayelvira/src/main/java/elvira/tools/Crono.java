/* Crono.java */

package elvira.tools;


import java.io.*;
import java.util.Date;



/**
 * Implements a crono to measure execution times.
 * Operations:
 * <ul>
 * <li> Constructor
 * <li> start: Makes the crono begins to count
 * <li> toCero: resets the crono, but keep on working
 * <li> stop: finish the count. To initiate a new one,
 *            start must be executed
 * <li> viewTime: gets the ellapsed seconds from the
 *            last execution of start or toCero
 *
 * @author Manuel Gomez
 * @since 18/04/2002
 */

public class Crono {

/**
 * date to retrieve the time 
 */
private Date date;

/**
 * time, to store the number of milliseconds 
 */
private double time;

/**
 * timePrev, to store the previous number of milliseconds
 */
private double timePrev;

/**
 * stoped, to know the state of the crono
 */
private boolean stopped;


/**
 * Constructor for Crono
 */

public Crono() {
  stopped=true;
}

  
/**
 * Start the crono 
 */

public void start() {

   /*
    * Valid operation only if the crono is already
    * stopped
    */
 
   if (stopped == true){ 
     /*
      * Create new date
      */

     date=new Date();

     /*
      * Gets actual number of milliseconds
      */

     timePrev=(double)date.getTime();

     /*
      * Change the state of Crono
      */

     stopped=false;
   }
   else{
       System.out.println("Crono already started.......");
   }
}


/**
 * Reset crono and give elapsed time, but keep on working 
 */

public void toCero() {

   /*
    * Valid only if the crono is already started
    */

   if (stopped == false){
     /*
      * Create new date
      */

     date=new Date();

     /*
      * Gets actual number of milliseconds
      */

     time=(double)date.getTime();

     /*
      * Prints difference between time and timePrev
      */

     System.out.println("(RESET) Ellapsed seconds: "+(time-timePrev)/1000);

     /*
      * Refresh timePrev
      */

     timePrev=time;
   }
   else{
       System.out.println("Non valid operation on stopped crono.......");
   }
}

/**
 * Stop crono and give elapsed time, wont work until new start 
 */

public void stop() {

   /*
    * Stop crono only if it is working
    */

   if(stopped == false){
     /*
      * Create new date
      */

     date=new Date();

     /*
      * Gets actual number of milliseconds
      */

     time=(double)date.getTime();

     /*
      * Prints difference between time and timePrev
      */

     System.out.println("(STOP) Ellapsed seconds: "+(time-timePrev)/1000);

     /*
      * Change the state
      */

     stopped=true;
   }
   else{
       System.out.println("Non valid operation on stopped crono.......");
   }
}


/**
 * Gets the number of milliseconds from last start or stop,
 * but without initializing timePrev at the end. If clock is
 * stopped, show a message
 */

public void viewTime() {
  
   if(stopped == false){ 
     /*
      * Create new date
      */

     date=new Date();

     /*
      * Gets actual number of milliseconds
      */

     time=(double)date.getTime();

     /*
      * Prints difference
      */

     System.out.println("Ellapsed seconds: "+(time-timePrev)/1000);
   }
   else{
       System.out.println("Non valid operation on stopped crono.......");
   }
}

/**
 * Gets the number of milliseconds from last start or stop,
 * but without initializing timePrev at the end. If clock is
 * stopped, show a message
 */

public double getTime() {
  
   if(stopped == false){ 
     /*
      * Create new date
      */

     date=new Date();

     /*
      * Gets actual number of milliseconds
      */

     time=(double)date.getTime();

	  /*
		* Return the number of milliseconds
		*/

	  return((time-timePrev)/1000);
   }
   else{
       System.out.println("Non valid operation on stopped crono.......");
		 return(0);
   }
}

} // End of class
