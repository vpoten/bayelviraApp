
	/* CronoNano.java */

	package elvira.tools;


	import java.io.*;
	import java.util.Date;



	/**
	 * Implements a crono to measure execution times. Time is measured in nanoseconds.
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
	 * @author Manuel Luque
	 * @since 31/05/2006
	 */

	public class CronoNano {

	
	/**
	 * time, to store the number of nanoseconds 
	 */
	private long time;

	/**
	 * timePrev, to store the previous number of nanoseconds
	 */
	private long timePrev;

	/**
	 * stoped, to know the state of the crono
	 */
	private boolean stopped;
	
	private static boolean debug = false;


	/**
	 * Constructor for Crono
	 */

	public CronoNano() {
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

	     

	     /*
	      * Gets actual number of nanoseconds
	      */

	     timePrev=System.nanoTime();

	     /*
	      * Change the state of Crono
	      */

	     stopped=false;
	   }
	   else{
		   if (debug) System.out.println("Crono already started.......");
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
	      * Gets actual number of nanoseconds
	      */

	     time=System.nanoTime();

	     /*
	      * Prints difference between time and timePrev
	      */

	     if (debug) System.out.println("(RESET) Ellapsed nano seconds: "+(time-timePrev));

	     /*
	      * Refresh timePrev
	      */

	     timePrev=time;
	   }
	   else{
		   if (debug) System.out.println("Non valid operation on stopped crono.......");
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
	      * Gets actual number of nanoseconds
	      */

	     time=System.nanoTime();

	     /*
	      * Prints difference between time and timePrev
	      */

	     if (debug) System.out.println("(STOP) Ellapsed seconds: "+(time-timePrev)/1000);

	     /*
	      * Change the state
	      */

	     stopped=true;
	   }
	   else{
	       if (debug) System.out.println("Non valid operation on stopped crono.......");
	   }
	}


	/**
	 * Gets the number of nanoseconds from last start or stop,
	 * but without initializing timePrev at the end. If clock is
	 * stopped, show a message
	 */

	public void viewTime() {
	  
	   if(stopped == false){ 
	 
	     /*
	      * Gets actual number of nanoseconds
	      */

	     time=System.nanoTime();

	     /*
	      * Prints difference
	      */

	     if (debug) System.out.println("Ellapsed seconds: "+(time-timePrev)/1000);
	   }
	   else{
	       if (debug) System.out.println("Non valid operation on stopped crono.......");
	   }
	}

	/**
	 * Gets the number of nanoseconds from last start or stop,
	 * but without initializing timePrev at the end. If clock is
	 * stopped, show a message
	 */

	public double getTime() {
	  
	   if(stopped == false){ 
	  
	     /*
	      * Gets actual number of nanoseconds
	      */

	     time=System.nanoTime();

		  /*
			* Return the number of nanoseconds
			*/

		  return(time-timePrev);
	   }
	   else{
	       if (debug) System.out.println("Non valid operation on stopped crono.......");
			 return(0);
	   }
	}

	} // End of class


