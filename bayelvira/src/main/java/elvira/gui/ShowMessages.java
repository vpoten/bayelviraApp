/* ShowMessages.java */

package elvira.gui;

import javax.swing.JOptionPane;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import elvira.Elvira;

public class ShowMessages {

   // Edition messages
	/**/ 
   public final static String PATH_REDUNDANT = "PathRedundant";
   public final static String POT_TREE_INCOMP = "PotentialTreeIncomplete";
   	
   public final static String EMPTY_STATES = "EmptyState";
   public final static String WRONG_RELEVANCE = "WrongRelevance";
   public final static String WRONG_CELL_VALUE = "WrongCellValue";
   public final static String LESS_THAN_O = "LessThanO";
   public final static String HIGHER_THAN_ONE = "HigherThanOne";
   public final static String CYCLE = "Cycle";
   public final static String WRONG_EXPANSION_THRESHOLD = "WrongExpansion";
   public final static String UNSAVED_NETWORK = "Unsaved";
   public final static String ZOOM_NOT_IN_INTERVAL = "ZoomNotInInterval";
   public final static String WRONG_ZOOM = "WrongZoom";
   public final static String OVERWRITE = "OverWrite";
   public final static String WRONG_LINK = "WrongLink";
   public final static String WRONG_VERSION = "WrongVersion";
   public final static String NODE_NAME_EXISTS = "NodeNameExists";
   public final static String EXISTS_LINK = "LinkExists";
   public final static String PROB_NOT_ONE = "ProbNotOne";
   public final static String NOT_DETERMINISTIC = "NotDeterministic";
   public final static String OR_ONLY_BINARY = "OrOnlyBinary";
   public final static String OR_PRESENT_ABSENT = "OrPresentAbsent";
   public final static String CHANGE_RELATION_TYPE = "ChangeRelationType";
   public final static String EQUIPROBABILITY = "EquiProbability";
   public final static String ENOUGH_STATES = "EnoughStates";
   public final static String WRONG_NETWORKS_SELECTED = "WrongNumberSelected";
   public final static String NOT_ENOUGH_NETWORKS = "NotEnoughNetworks";
   public final static String DECISION_TABLE_NULL = "DecisionTableNull";

   // Inference messages
   public final static String EVIDENCE_CASE_FULL= "EvidenceCaseFull";
   public final static String CASE_ALREADY_STORED= "CaseAlreadyStored";
   public final static String NEXT_CASE= "NextCase";
   public final static String PREVIOUS_CASE= "PreviousCase";
   public final static String LAST_CASE= "LastCase";
   public final static String FIRST_CASE= "FirstCase";
   public final static String FULL_CASESLIST= "FullCasesList";
   public final static String EMPTY_ACTIVE_CASE= "EmptyActiveCase";
   public final static String DUPLICATED_FINDING= "DuplicatedFinding";
   public final static String DELETING_FINDING= "DeletingFinding";
   public final static String EMPTY_FINDING_NAME= "EmptyFindingName";
   public final static String ACTIVE_CASE_NO_PROPAGATED="ActiveCaseNoPropagated";
   public final static String CURRENT_CASE_NO_PROPAGATED="CurrentCaseNoPropagated";
   public final static String DELETE_ACTIVE_CASE_NO_PROPAGATED="DeleteActiveCaseNoPropagated";
   public final static String CURRENT_CASE_SHOWN="CurrentCaseShown";
   public final static String IMPOSIBLE_FINDING= "ImposibleFinding";
   public final static String IMPOSIBLE_DECISION= "ImposibleDecision";
   public final static String IMPOSIBLE_EVIDENCE= "ImposibleEvidence";
   public final static String PRIORI_UNCHANGED= "PrioriUnchanged";
   public final static String PRIORI_UNCHANGED_NEW= "PrioriUnchangedNew";
   public final static String DELETE_PRIORICASE= "DeletePrioriCase";
   public final static String EDIT_SEVERAL_CASES= "EditSeveralCases";
   public final static String NO_ROW_SELECTED= "NoRowSelected";
   public final static String NO_VAR_SELECTED= "NoVarSelected";
   public static final String RECOMPILE_ID_PROBABILITIES = "RecompileIDiagramProbabilities";
   public static final String RECOMPILE_ID_UTILITIES = "RecompileIDiagramUtilities";
   public static final String ADD_TERMINAL_VALUE_NODE = "AddTerminalValueNode";
   public static final String ADD_TERMINAL_VALUE_NODE_INFLUENCES = "AddTerminalValueNodeInfluences";
   
   public final static int GUI_ERROR = 0;
   public final static int COMMAND_LINE_ERROR = 1;


  	/**
	 * Contains the strings for the languaje selected
	 */

	private static ResourceBundle messagesBundle;


   static {
		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: messagesBundle = ResourceBundle.getBundle ("elvira/localize/Messages");
		                         break;
		   case Elvira.SPANISH: messagesBundle = ResourceBundle.getBundle ("elvira/localize/Messages_sp");
		                        break;
		}
   }


   public static String message (String msg) {
      try {
         return messagesBundle.getString(msg);
      }
      catch (MissingResourceException e) {
         return msg;
      }
   }


   public static int showOptionDialog (String msg, int type,
                  Object[] options, int index) {
      String text,title;
      text = message (msg+".Text.label");
      title = message (msg+".Title.label");
      return JOptionPane.showOptionDialog (null, text,
                           title, JOptionPane.DEFAULT_OPTION,
                           type, null,
                           options, options[index]);
   }


   public static void showMessageDialog (String msg, int type) {
      String text,title;
      text = message (msg+".Text.label");
      title = message (msg+".Title.label");
    	JOptionPane.showMessageDialog(null, text, title, type);
	}


	public static String obtainText (String msg, Object[] strings) {
	   int i;
	   String text = new String();
	   for (i=0; i<strings.length; i++) {
	      text = text + message (msg+".Text.label"+(i+1));
	      text = text + " " + strings[i] + " ";
	   }

	   text = text + message (msg+".Text.label"+(i+1));

	   return text;
	}


	public static int showOptionDialogPlus (String msg, int type,
	               Object[] options, int index, Object[] strings) {
	   String text, title;
	   title = message (msg+".Title.label");
	   text = obtainText (msg, strings);

      return JOptionPane.showOptionDialog (null, text,
                           title, JOptionPane.DEFAULT_OPTION,
                           type, null,
                           options, options[index]);
	}


	public static void showMessageDialogPlus (String msg, int type,
	               Object[] strings) {
	   String text, title;
	   title = message (msg+".Title.label");
	   text = obtainText (msg, strings);
      JOptionPane.showMessageDialog(null, text, title, type);
   }


	public static int showConfirmDialogPlus (String msg, int type,
	               int buttons, Object[] strings) {
	   String text, title;
	   title = message (msg+".Title.label");
	   text = obtainText (msg, strings);

      return JOptionPane.showConfirmDialog(null, text,
                     title, buttons, type);
	}


	public static void display (int errorType, Throwable error) {
	   String message = null;
	   Class c = error.getClass();
	   while ((message == null) && (c != Object.class)) {
	      try {
	         message = messagesBundle.getString(c.getName());
	      } catch (MissingResourceException e) {
	         c = c.getSuperclass();
	      }
	   }

	   if (message == null)
	      message = "Error";

	   if (errorType == GUI_ERROR)
	      JOptionPane.showMessageDialog(null, message,
	               "Elvira - Error", JOptionPane.ERROR_MESSAGE);
	   else
	      System.out.println (message);
	}

}  // end of ShowMessages class