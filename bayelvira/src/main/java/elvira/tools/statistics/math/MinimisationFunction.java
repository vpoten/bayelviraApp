/*
*   Interface MinimisationFunction
*
*   Calculates the value of the function to be
*   minimised by the methods in the class, Minimisation
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:	    April 2003
*   MODIFIED:   April 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   Minimisation.html
*
*   Copyright (c) April 2004
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
****************************************************************************************/

package elvira.tools.statistics.math;

// Interface for Minimisation class
// Calculates value of function to be minimised
public interface MinimisationFunction{

    double function(double[]param);
}