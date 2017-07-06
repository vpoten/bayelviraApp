package elvira.decisionTrees;

import java.util.Vector;
import java.util.HashSet;
import javax.swing.tree.TreePath;

/**
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.1
 * 
 * Guarda por niveles los nodos según un recorrido en anchura
 */
public class LevelProxy {

	Vector<Vector<TreePath>> niveles= new Vector<Vector<TreePath>>();
	Vector<HashSet<Object>> objectsInLevel= new Vector<HashSet<Object>>();
	DecisionTreeModel model;
	
	public LevelProxy(DecisionTreeModel model) {
		this.model= model;
		update();
	}
	
	protected void update() {
		niveles.clear();
		
		HashSet<Object> setNivel= new HashSet<Object>();
		setNivel.add(model.getRoot());

		while( setNivel.size()>0 ) {
			Vector<TreePath> pathsNivel= new Vector<TreePath>();
			HashSet<Object> setTemporal= new HashSet<Object>();
			
			// Añadido el 05/12/2005 para actualizacion del arbol
			// por cambio general de precisiones/descripciones
			objectsInLevel.add(setNivel);
			
			for (Object nodo : setNivel) {
				// Solo se añaden al nivel los nodos q son resumen o utilidades
				if( nodo instanceof SummaryBox || nodo instanceof UtilityNode || nodo instanceof SuperValueNode ) {
					Vector<Object []> paths= model.getPathsToRoot(nodo);
					
					for (Object []treePath : paths) {
						pathsNivel.add(new TreePath(treePath));
					}
					
					/*
					Iterator<Object []> itPath= paths.iterator();
					while( itPath.hasNext() ) {
						pathsNivel.add(new TreePath(itPath.next()));
					}
					*/
				}
				
				for( int i=0; i<model.getChildCount(nodo); i++ ) {
					Object hijo= model.getChild(nodo,i);
					setTemporal.add(hijo);
				}				
			}
			
			/*
			Iterator<Object> it= setNivel.iterator();
			while( it.hasNext() ) {
				Object nodo= it.next();
				
				// Solo se añaden al nivel los nodos q son resumen o utilidades
				if( nodo instanceof SummaryBox || nodo instanceof UtilityNode || nodo instanceof SuperValueNode ) {
					Vector<Object []> paths= model.getPathsToRoot(nodo);
					Iterator<Object []> itPath= paths.iterator();
					while( itPath.hasNext() ) {
						pathsNivel.add(new TreePath(itPath.next()));
					}
				}
				
				for( int i=0; i<model.getChildCount(nodo); i++ ) {
					Object hijo= model.getChild(nodo,i);
					setTemporal.add(hijo);
				}
			}
			*/
			
			if( pathsNivel.size()>0 ) {
				niveles.add(pathsNivel);
			}
			
			setNivel= setTemporal;
		}
	}
	
	public Vector<TreePath> getLevel(int k) {
		return niveles.get(k);
	}
	
	public int getNumberOfLevels() {
		return niveles.size();
	}
}
