package elvira.gui.explication;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class LimitadorDeDocumento extends DefaultStyledDocument { 
	int caracteresMaximos; 

	public LimitadorDeDocumento( int caracteresMaximos ) { 
		this.caracteresMaximos = caracteresMaximos; 
	} 

	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException { 
		if ( str.indexOf("?") == -1  && (getLength() + str.length()) <= caracteresMaximos) 
			super.insertString(offs, str, a); 
		else 
			Toolkit.getDefaultToolkit().beep(); 
	} 

} 

