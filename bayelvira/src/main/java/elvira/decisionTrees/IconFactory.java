package elvira.decisionTrees;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;

/**
 * Crea iconos para representar las variables en la presentaci�n
 * del �rbol de decisi�n
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * @version 0.2
 * 
 * version 0.1: formato y colores propios
 * version 0.2: intento de usar colores elvira / �valos similares al Netica  
 */
abstract public class IconFactory {
	/**
	 * Crea la representaci�n gr�fica de un nodo de azar
	 * @param text texto a incluir en el nodo
	 * @param f tipo de letra a utilidar
	 * @return icono con la representaci�n gr�fica
	 */
	public static Icon createChanceIcon( String text, Font f ) {
		// Se toman las dimensiones del texto que se quiere dibujar
		FontRenderContext fr= new FontRenderContext(null,false,false);
		TextLayout t= new TextLayout(text, f, fr );
		
		// M�rgenes horizontales y verticales del icono
		int margenH=6;
		int margenV=6;
		
		// Ajuste del tama�o del icono teniendo en cuenta dimensiones del texto y de los m�rgenes
		Rectangle2D r= t.getBounds();
		int width= (int) r.getWidth() + 2*(margenH+1); // No quedaba centrado
		int height= (int) r.getHeight() + 2*margenV;
		
		// Se crea un buffer donde dibujar el icono
		BufferedImage image = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g= (Graphics2D) image.createGraphics();
		
		// Ajuste del ancho del �valo seg�n las dimensiones 
		int anchoOval= Math.min(height,width);

		// Color de fondo en amarillo pseudo-elvira
		g.setColor(new Color(255,255,200));
		
		// Arcos laterales del �valo
		g.fillArc(0,0,anchoOval,height-1,90,180);
		g.fillArc(width-anchoOval-1,0,anchoOval,height-1,270,180);
		
		// La parte central es un rect�ngulo (si el texto es muy largo)
		g.fillRect(anchoOval/2,0,width-anchoOval, height-1);
		
		// Se pintan los bordes del �valo dibujado
		g.setColor(Color.black);
		g.drawArc(0,0,anchoOval,height-1,90,180);
		g.drawArc(width-anchoOval-1,0,anchoOval,height-1,270,180);
		g.drawLine(anchoOval/2,0,width-anchoOval/2,0);
		g.drawLine(anchoOval/2,height-1,width-anchoOval/2,height-1);

		// Se imprime el texto dentro del �valo
		t.draw( g, margenH, height-margenV-1 );
		
		// Se convierte la imagen creada a icono
		return new ImageIcon( image );
	}
	
	/**
	 * Crea la representaci�n gr�fica de un nodo de decisi�n
	 * @param text texto a incluir en el nodo
	 * @param f tipo de letra a utilidar
	 * @return icono con la representaci�n gr�fica
	 */
	public static Icon createDecisionIcon( String text, Font f ) {
		// Se toman las dimensiones del texto que se quiere dibujar
		FontRenderContext fr= new FontRenderContext(null,false,false);
		TextLayout t= new TextLayout(text, f, fr );
		
		// M�rgenes horizontales y verticales del icono
		int margenH=6;
		int margenV=6;
		
		// Ajuste del tama�o del icono teniendo en cuenta dimensiones del texto y de los m�rgenes
		Rectangle2D r= t.getBounds();
		int width= (int) r.getWidth() + 2*margenH;
		int height= (int) r.getHeight() + 2*margenV;

		// Se crea un buffer donde dibujar el icono
		BufferedImage image = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g= (Graphics2D) image.getGraphics();
		
		// Color de fondo en azul pseudo-elvira
		g.setColor(new Color(200,255,255));
		
		// Un nodo de decisi�n es un triste rect�ngulo ;-)
		g.fillRect(0,1,width-2,height-2);

		// Se pintan los bordes del rect�ngulo dibujado
		g.setColor(Color.black);
		g.drawRect(0,1,width-2,height-2);
		
		// Se imprime el texto dentro del rect�ngulo
		t.draw( g, margenH, height-margenV );
		
		// Se convierte la imagen creada a icono
		return new ImageIcon( image );
	}
	
	/**
	 * Crea la representaci�n gr�fica de un nodo de utilidad
	 * @param text texto a incluir en el nodo
	 * @param f tipo de letra a utilidar
	 * @return icono con la representaci�n gr�fica
	 */
	static Icon createUtilityIcon( String text, Font f ) {
		// Se toman las dimensiones del texto que se quiere dibujar
		FontRenderContext fr= new FontRenderContext(null,false,false);
		TextLayout t= new TextLayout(text, f, fr );
		
		// Ajuste del margen del icono teniendo en cuenta dimensiones del texto y de los m�rgenes
		Rectangle2D r= t.getBounds();
		int margenH= (int) (6+r.getHeight()/2);
		int margenV=6;
		
		// Ajuste del tama�o del icono teniendo en cuenta dimensiones del texto y de los m�rgenes
		int width= (int) (r.getWidth() + 2*margenH);
		int height= (int) (r.getHeight() + 2*margenV);
		
		// Se crea un buffer donde dibujar el icono
		BufferedImage image = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g= (Graphics2D) image.getGraphics();

		// Se crea un pol�gono con la forma caracter�stica de peonza de los nodos de utilidad
		Polygon poly= new Polygon();
		poly.addPoint(1,height/2);
		poly.addPoint(height/2,height-1);
		poly.addPoint(width-height/2,height-1);
		poly.addPoint(width-1,height/2);
		poly.addPoint(width-height/2,1);
		poly.addPoint(height/2,1);
		
		// Color de fondo en color ��?? tomado de elvira
		g.setColor(new Color(200,255,200));
		
		// Se dibuja la peonza
		g.fillPolygon(poly);
		
		// Se pintan los bordes de la peonza dibujada
		g.setColor(Color.black);
		g.drawPolygon(poly);
		
		// Se imprime el texto dentro del rect�ngulo
		t.draw( g, margenH, height-margenV );
		
		// Se convierte la imagen creada a icono
		return new ImageIcon( image );
	}
}
