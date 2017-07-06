/* ElviraDesktopPane.java */

package elvira.gui;

import javax.swing.*;
import java.beans.PropertyVetoException;

/**
 * The ElviraDesktopPane object is set to place the
 * NetworkFrame objects. To do this uses all the methods of his
 * ancestor. This class implements all the necessary methods to
 * sort the NetworkFrames displayed.
 * 
 * 
 * Jorge-PFC: cambiado el acceso de package a public el 26/12/2005
 * (no tenia mucho sentido q el metodo getDesktopPane de ElviraFrame
 * fuese publico y no se pudiera utilizar por tener este metodo
 * un tipo de acceso mas restrictivo)
 */
public class ElviraDesktopPane extends javax.swing.JDesktopPane  {
	
	private int xoffset = 20, yoffset = 20, w = 500, h = 400;

	public void closeAll() {
		JInternalFrame[] frames = getAllFrames();

		for(int i=0; i < frames.length; i++) {
			if(!frames[i].isIcon()) {
				try {
					frames[i].setIcon(true);
				}
				catch(java.beans.PropertyVetoException ex) {
					System.out.println("iconification vetoed!");
				}
			}
		}
	}
	
	public void openAll() {
		JInternalFrame[] frames = getAllFrames();

		for(int i=0; i < frames.length; i++) {
			if(frames[i].isIcon()) {
				try {
					frames[i].setIcon(false);
				}
				catch(java.beans.PropertyVetoException ex) {
					System.out.println("restoration vetoed!");
				}
			}
		}
	}
	
	
	public void restoreAll() {
		JInternalFrame[] frames = getAllFrames();

		for(int i=0; i < frames.length; i++) {
			if(frames[i].isIcon()) {
				try {
					frames[i].setIcon(false);
				}
				catch(java.beans.PropertyVetoException ex) {
					System.out.println("restoration vetoed!");
				}
			}
			frames[i].setSize(frames[i].getPreferredSize());
		}
	}
		
	
	public void cascade() {
		JInternalFrame[] frames = getAllFrames();
		int x = 0, y = 0;

		for(int i=0; i < frames.length; i++) {
	      try {
	         if (frames[i].isIcon())
	            frames[i].setIcon(false);
	      }
			catch(java.beans.PropertyVetoException ex) {
				System.out.println("restoration vetoed!");
			}
	            
			frames[i].setBounds(x,y,w,h);
				
			x += xoffset;
			y += yoffset;
		}
	}
	
	
	/**
	 * Unselect the actually InternalFrame and select the
	 * next to it. If the actual InternalFrame is the last one, 
	 * the new InternalFrame selected it is the first.
	 */
	
	public void next() {
	   JInternalFrame[] frames = getAllFrames();
	   boolean finish = false;
	   int i=0;
	   
	   if (frames.length==0)
	      finish=true;
	   
	   while (!finish) {
	      if (frames[i].isSelected()) {
	         if (i==frames.length-1) 
	            try {
	               frames[0].setSelected(true); }
	            catch (PropertyVetoException e) { }
	         else
	            try {
	               frames[i+1].setSelected(true); }
	            catch (PropertyVetoException e) { }
	            
	         finish=true;
	      }
	      else
	         i++;
	   }
	}
	
	
	/**
	 * Unselect the actually InternalFrame and select the
	 * previous to it. If the actual InternalFrame is the first, 
	 * the new InternalFrame selected it is the last one.
	 */
	 
	public void previous() {
	   JInternalFrame[] frames = getAllFrames();
	   boolean finish = false;
	   int i=0;
	   
	   if (frames.length==0)
	      finish=true;
	   
	   while (!finish) {
	      if (frames[i].isSelected()) {
	         if (i==0) 
	            try {
	               frames[frames.length-1].setSelected(true); }
	            catch (PropertyVetoException e) { }
	         else
	            try {
	               frames[i-1].setSelected(true); }
	            catch (PropertyVetoException e) { }
	            
	         finish=true;
	      }
	      else
	         i++;
	   }
	}
	   
}
