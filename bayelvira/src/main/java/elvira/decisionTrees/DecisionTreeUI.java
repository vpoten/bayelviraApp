package elvira.decisionTrees;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

/**
 * Basado en el PrototipoPFC05 realizado con JBuilder
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1 (no incluia zoom)
 * @version 0.2 Modificacion del MouseListener interno para ajustar las coordenadas del raton al nivel de zoom
 */
public class DecisionTreeUI extends BasicTreeUI {
	
	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicTreeUI#paintExpandControl(java.awt.Graphics, java.awt.Rectangle, java.awt.Insets, java.awt.Rectangle, javax.swing.tree.TreePath, int, boolean, boolean, boolean)
	 */
	protected void paintExpandControl(Graphics g,
			Rectangle clipBounds, Insets insets,
			Rectangle bounds, TreePath path,
			int row, boolean isExpanded,
			boolean hasBeenExpanded,
			boolean isLeaf) {
		
		Object value = path.getLastPathComponent();
		
		// Draw icons if not a leaf and either hasn't been loaded,
		// or the model child count is > 0.
		if (!isLeaf && (!hasBeenExpanded || treeModel.getChildCount(value) > 0)) {
			int middleXOfKnob;
			
			if (tree.getComponentOrientation().isLeftToRight()) {
				middleXOfKnob = bounds.x - (getRightChildIndent() - 1);
			}
			else {
				middleXOfKnob = bounds.x + bounds.width + getRightChildIndent();
			}
			int middleYOfKnob = bounds.y + (bounds.height / 2);
			
			if (isExpanded) {
				Icon expandedIcon = getExpandedIcon();
				
				/* Solo se muestra el knob si el tipo de nodo es un SummaryBox o un Nodo Supervalor, evitando
				 * de esta forma que se pueda colapsar la representación gráfica de los otros tipos de nodo
				 */
				if(expandedIcon != null && (value instanceof SummaryBox || value instanceof SuperValueNode) ) {
					drawCentered(tree, g, expandedIcon, middleXOfKnob, middleYOfKnob );
				}
			}
			else {
				Icon collapsedIcon = getCollapsedIcon();

				/* Solo se muestra el knob si el tipo de nodo es un SummaryBox o un Nodo Supervalor, evitando
				 * de esta forma que se pueda colapsar la representación gráfica de los otros tipos de nodo
				 */
				if(collapsedIcon != null && (value instanceof SummaryBox || value instanceof SuperValueNode) ) {
					drawCentered(tree, g, collapsedIcon, middleXOfKnob, middleYOfKnob);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicTreeUI#createMouseListener()
	 */
	protected java.awt.event.MouseListener createMouseListener() {
		return new ZoomMouseListener();
	}
	
	/** Modifica las selecciones con el raton para que tengan en cuenta el factor de zoom actual del componente
	 * @author Jorge Fernández Suárez
	 * @version 0.1
	 */
	protected class ZoomMouseListener extends MouseAdapter{
		public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			handleSelection(mouseEvent);
		}
		
		protected void handleSelection(java.awt.event.MouseEvent evt){
			if(tree != null && tree.isEnabled()) {
				if (isEditing(tree) && tree.getInvokesStopCellEditing() &&
						!stopEditing(tree)) {
					return;
				}
				
				if (tree.isRequestFocusEnabled()) {
					tree.requestFocus();
				}
				
				// Correccion de la posicion donde se ha pulsado el raton según el factor de zoom
				double zoomFactor= ((DecisionTreeViewer) tree).getZoomFactor();
				int x= (int) (evt.getX()/zoomFactor);
				int y= (int) (evt.getY()/zoomFactor);
				
				MouseEvent nevt= new MouseEvent(evt.getComponent(),evt.getID(),evt.getWhen(),evt.getModifiers(),
						x, y, evt.getClickCount(), evt.isPopupTrigger(), evt.getButton());
				
				TreePath path = getClosestPathForLocation(tree, x, y);
				
				if(path!=null){
					Rectangle bounds = getPathBounds(tree, path);
					
					if(y > (bounds.y + bounds.height)) {
						return;
					}
					
					if(SwingUtilities.isLeftMouseButton(nevt)) {
						checkForClickInExpandControl(path, x, y);
					}
					
					if(!startEditing(path, nevt)){
						selectPathForEvent(path, nevt);
					}
				}
			}
		}
	}	
}
