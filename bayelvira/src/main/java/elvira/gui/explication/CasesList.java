/* CasesList.java */

package elvira.gui.explication;

import elvira.Elvira;
import elvira.*;
import elvira.gui.*;
import java.util.*;

/**
 * This list is defined to manage a set of cases of evidence. This list is always created
 * when the user changes from edition mode to inference mode, when a new list is defined.
 * In the list there are always two singular cases: the Current Case and the Active Case.
 * Current Case is the case to be processed at each moment.
 * Active Case is always in the next position to the last case stored. It is created when
 * a case is stored in the list of cases. In such situation the Active Case is equals to the
 * recently stored case and the user may modify it.
 * Current Case and Active Case can be at the same position or not.
 * The main objective for differentiating both cases is to give the user possibility of
 * navigating trough the list of cases


 * @since 27/7/00
 * @author Carmen Lacave, F. Javier Díez, Roberto Atienza
 * @version 0.1
 */
public class CasesList{

    /**
     * To store the set of Cases
    */
    private Vector casesList;


    /**
     * Defines the maximum number of Cases to define in memory
     */
    private int maxNumStoredCases;

    /**
     * Defines the maximum number of Cases that can be shown to the user.
     * It must be less than maxNumStoredCases
     */
    private int maxNumShownCases;

    /**
     * To avoid duplicating identifiers after removing cases
     */
    private int numCreatedCases;

    /**
     * To know which case will be processed at each moment
     */
    private int  currentCase;

    /**
     * A reference to the Bayesian net in which the propagation is carrying on
     */
    private Bnet bn;

    /**
     * To quickly access to the first and last cases to be shown
    */
    private int firstShown, lastShown;

    /** To internationalize identifiers */
    private ResourceBundle dialogBundle;

    /** constructors */
    /**
     * Creates a new empty list of cases from a Bayesian net.
      * By default, the number of cases to store in the list will be 5. This number
     * can be modified by the user.
     * @param The Bayesian net
     * @see Case
     */
    public CasesList(Bnet b){
		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs");
		                         break;
		   case Elvira.SPANISH: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs_sp");
		                        break;
		}

        maxNumStoredCases=5;
        maxNumShownCases=5;
        casesList=new Vector(maxNumStoredCases);
        bn=b;
        Case prior=new Case(bn);
        prior.setIdentifier(Elvira.localize(dialogBundle, "PriorProb"));
        casesList.addElement(prior);
        prior.setIsShown(true);
        prior.setPropagated(true);
        Case active=new Case(bn,prior,VisualExplanationFStates.colours[1]);
        active.setIsShown(false);
        active.setPropagated(false);
        casesList.addElement(active);
        active.setIdentifier(Elvira.localize(dialogBundle, "CaseNum")+" "+String.valueOf(1));
        currentCase=0;
        firstShown=0;
        lastShown=0;
        numCreatedCases=1;
    }

    public CasesList(){
        switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs");
		                         break;
		   case Elvira.SPANISH: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs_sp");
		                        break;
		}

		maxNumStoredCases=5;
        maxNumShownCases=5;
        casesList=new Vector(maxNumStoredCases);
        currentCase=-1;
        firstShown=0;
        lastShown=0;
        numCreatedCases=0;
    }

    /** Returns the Bayesian net where the list of cases is defined
     */
    public Bnet getBnet(){
        return bn;
    }

    /** Returns the number of stored cases
     */
    public int getNumStoredCases(){
        return casesList.size();    }

    /** Returns the maximum number of cases to store
     */

    public int getMaxNumStoredCases(){
        return maxNumStoredCases;
    }



    /** Gets the maximum number of cases to be shown
     */
    public int getMaxNumShownCases(){
        return maxNumShownCases;
    }

    /** Returns the position of the first case to be shown
    */
    public int getFirstShown(){
        return firstShown;
    }

    /** Returns the position of the last case to be shown
    */
    public int getLastShown(){
        return lastShown;
    }

    /** Sets the position of the first case to be shown
     * @param f The position in the list of cases of the first case to be shown
    */
    public void setFirstShown(int f){
        firstShown=f;
    }

    /** Sets the position of the last case to be shown
     * @param f The position in the list of cases of the last case to be shown
    */
    public void setLastShown(int f){
        lastShown=f;
    }

    /** Sets the maximum number of cases to be stored
     * @param f The maximum number of stored cases
    */
    public void setMaxNumStoredCases(int n){
        maxNumStoredCases=n;
    }

    /** Sets the maximum number of cases to be shown
     * @param f The maximum number of shown cases
    */
    public void setMaxNumShownCases(int n){
        if (n<maxNumStoredCases)
            maxNumShownCases=n;
        else maxNumShownCases=maxNumStoredCases;
    }

    /**
     * Adds to the Active case the finding defined by the state number v of the variable fs
     * @param fs The variable defining the finding
     * @param v The number of the state
     */
    public void addActiveCase(FiniteStates fs, int v){
        double[] p=new double[fs.getNumStates()];
        for (int i=0; i<fs.getNumStates(); i++){
            if (i==v)
                p[i]=1.0;
            else p[i]=0.0;
        }
        getActiveCase().setProbOfNode(fs,p);
        getActiveCase().setAsFinding(fs,v);
    }

    /**
     * Adds to the Active case the finding defined by the state number v of the variable fs
     * @param fs The variable defining the finding
     * @param v The number of the state
     */
    public void addActiveCase(Continuous c, double v){
        getActiveCase().setAsFinding(c,v);
    }

    /**
     * Adds to the Current case the finding defined by the state number v of the variable fs
     * @param fs The variable defining the finding
     * @param v The number of the state
     */
    public void addCurrentCase(FiniteStates fs, int v){
        double[] p=new double[fs.getNumStates()];
        for (int i=0; i<fs.getNumStates(); i++){
            if (i==v)
                p[i]=1.0;
            else p[i]=0.0;
        }
        getCurrentCase().setProbOfNode(fs,p);
        getCurrentCase().setAsFinding(fs,v);
    }

    /**
     * Adds to the Current case the finding defined by the state number v of the variable fs
     * @param fs The variable defining the finding
     * @param v The number of the state
     */
    public void addCurrentCase(Continuous c, double v){
        getCurrentCase().setAsFinding(c,v);
    }

    /** Sets the position of the case to be processed
     * @param f The position of the case
    */
    public void setCurrentCase(int n){
        if (n<casesList.size() && n>-1)
            currentCase=n;
    }

    /** Returns the position of the Active case.
    */
    public int getNumActiveCase(){
        return getNumStoredCases()-1;
    }

    /** Returns the Active case.
    */
    public Case getActiveCase(){
        return (Case)casesList.elementAt(getNumActiveCase());
    }


    /** Returns the position of the Current case.
    */
    public int getNumCurrentCase(){
        return currentCase;
    }

    /** Returns the Current case.
    */
    public Case getCurrentCase(){
        if (currentCase!=-1)
            return (Case)casesList.elementAt(currentCase);
            else return null;
    }

    /** Returns the Case that is at a given position.
     * @param n The position of the case to get.
    */
    public Case getCaseNum(int n){
        if (n>-1)
            return (Case)casesList.elementAt(n);
            else return null;
    }

    /**
     * Gets the position of a given case in the list of cases
     * @param c The case to find in the list.
     */
    public int posCase(Case c){
        return casesList.indexOf(c);
    }

    /**
     * Stores a given case in the list provided that the case has not been stored yet.
     * Then a new Active case is created equals to the stored one.
     * @param c The case to store.
    */
    public void storeCase(Case c){
        if (getNumStoredCases()==maxNumStoredCases)
            System.out.println("The list is full");
            else {
                  c.setIsShown(true);
                  currentCase=getNumActiveCase();
                  int color=getNumStoredCases()% VisualExplanationFStates.colours.length;
                  Case newc=new Case(bn,c,
                     VisualExplanationFStates.colours[color]);
                  casesList.addElement(newc);
                  newc.setIsShown(true);
                  if (((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel().AUTOPROPAGATION) newc.setPropagated(true);
                     else newc.setPropagated(false);
                  newc.setIdentifier(Elvira.localize(dialogBundle, "CaseNum")+" "+(getNumStoredCases()-1));
                  currentCase=getNumActiveCase();
                  lastShown=getNumStoredCases()-1;
            }
    	System.out.println("Casos almacenados "+getNumStoredCases());            
    }

    /**
     * Stores a new case taking the findings from the Evidence ev.
     * @param ev The given Evidence.
    */
  public void storeCase(Evidence ev){
    if (getNumStoredCases()==maxNumStoredCases)
      System.out.println("The list is full");
    else {
          if (numCreatedCases==1){
          	 for (int h=0; h<ev.size(); h++)
              addActiveCase(ev.getVariable(h), ev.getValue(h));
             getActiveCase().setIsShown(true);
   			 if (((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel().AUTOPROPAGATION)
        			getActiveCase().setPropagated(true);
      			else
        			getActiveCase().setPropagated(false);
      			getActiveCase().setIdentifier(Elvira.localize(dialogBundle, "CaseNum")+" "+(getNumStoredCases()-1));
      			currentCase=getNumActiveCase();
      			lastShown=getNumStoredCases()-1;
             
          }
          else {
      			numCreatedCases++;
      			currentCase=getNumActiveCase();
      			int color=numCreatedCases % VisualExplanationFStates.colours.length;
      			Case newc=new Case(bn,ev,VisualExplanationFStates.colours[color]);
      			casesList.addElement(newc);
      			newc.setIsShown(true);
      			if (((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel().AUTOPROPAGATION)
        			newc.setPropagated(true);
      			else
        			newc.setPropagated(false);
      			newc.setIdentifier(Elvira.localize(dialogBundle, "CaseNum")+" "+(getNumStoredCases()-1));
      			currentCase=getNumActiveCase();
      			lastShown=getNumStoredCases()-1;
   		}
   	}
}


    /**
     * Removes a given case from the list of cases
     * @param c The case to remove
     */
    public void removeCase(Case c){
        if (casesList.indexOf(c)!=-1){
    	   numCreatedCases--;
    	   if (casesList.indexOf(c)==getNumStoredCases()-1){
                casesList.removeElement(c);
                Case newc=new Case(bn, VisualExplanationFStates.colours[getNumStoredCases()]);
                newc.setIsShown(false);
                newc.setPropagated(false);
                newc.setIdentifier(Elvira.localize(dialogBundle, "CaseNum")+" "+(getNumActiveCase()+1));
                casesList.addElement(newc);
                if (currentCase==getNumActiveCase())
                    currentCase=getNumActiveCase()-1;
            }
            else {
                  casesList.removeElement(c);
                  currentCase=currentCase-1;
            }
        }
    }

     /** Adds a given case to the list of cases if and only if the case is not in the list
      * @param c The case to add
      */
     public void addCase(Case c){
        if (casesList.indexOf(c)==-1){
            casesList.addElement(c);
        }
    }

}
