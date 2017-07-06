/*PanelSpiderAllNodes.java*/

package elvira.sensitivityAnalysis;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Vector;
import java.io.IOException;

/**
 * Clase que contiene el panel donde se van a dibujar los diagramas de spider de
 * cualquier configuracion posible que haya en el diagrama.
 * @author jruiz
 * @version 1.0
 */
public class PanelSpiderAllNodes extends JPanel{

  private RangeBoxList listaRangos;
  private int margenX = 80;
  private int margenY = 50;
  private NumberFormat fm = new DecimalFormat("0.00");
  private NumberFormat fmU = new DecimalFormat("0.0000");
  private double utilidad = 0.0;
  private Color[] colores = new Color[10];

  /**
   * Constructor con el sistema principal.
   * @param lr Lista de rangos.
   * @param u Utilidad base.
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  public PanelSpiderAllNodes(RangeBoxList lr,double u) throws elvira.parser.
      ParseException, IOException {

    colores[0] = Color.blue;
    colores[1] = Color.cyan;
    colores[2] = Color.darkGray;
    colores[3] = Color.green;
    colores[4] = Color.magenta;
    colores[5] = Color.orange;
    colores[6] = Color.pink;
    colores[7] = Color.red;
    colores[8] = Color.yellow;
    colores[9] = Color.gray;
    listaRangos = lr;
    utilidad = u;
    setBackground(Color.white);
  }

  /**
   * Repinta el panel.
   * @param g
   */
  public void paint(Graphics g) {

    super.paint(g);

    if(listaRangos != null && listaRangos.size() > 0) {
      int ancho;
      int alto;
      int grosor;
      BoxResult resultado;
      BoxEntry entrada;
      RangeBox rango;
      double valorMinimo = 0.0;
      double valorMaximo = 0.0;
      double origenX;
      double origenY;
      double destinoX;
      double destinoY;
      int i;
      int j;
      double maxP;
      double minP;
      double mP;
      double MP;
      double divY;
      double divX;
      int oY = 0;
      int oX = 0;
      int dY = 0;
      int dX = 0;
      ProbUtil pu;
      Vector datos;
      double pasoX;
      double m1;
      double m2;
      double v;
      double y;

      g.setColor(Color.black);
      ancho = getWidth() - margenX - 100;
      alto = getHeight() - margenY - 50;
      grosor = alto / ( 2 * listaRangos.size());
      rango = listaRangos.getRangeBox(0);
      resultado = rango.getBoxResult();
      entrada = rango.getBoxEntry();
      valorMinimo = resultado.getMinUtil();
      valorMaximo = resultado.getMaxUtil();
      v = entrada.getNodeStateList().getValue();
      y = v;

      if (v == 0) {
        y = 1;
      }

      maxP = ((-v + entrada.getNodeStateList().getMaxValue())) / y;
      minP = ((-v + entrada.getNodeStateList().getMinValue())) / y;

      for (i=1; i < listaRangos.size(); i++) {
        rango = listaRangos.getRangeBox(i);
        resultado = rango.getBoxResult();
        entrada = rango.getBoxEntry();
        v = entrada.getNodeStateList().getValue();
        y = v;

        if (v == 0) {
          y = 1;
        }

        if (resultado.getMinUtil() < valorMinimo) {
          valorMinimo = resultado.getMinUtil();
        }

        if (resultado.getMaxUtil() > valorMaximo) {
          valorMaximo = resultado.getMaxUtil();
        }

        MP = ((-v + entrada.getNodeStateList().getMaxValue())) / y;
        mP = ((-v + entrada.getNodeStateList().getMinValue())) / y;

        if (mP < minP) {
          minP = mP;
        }

        if (MP > maxP) {
          maxP = MP;
        }

      }

      if (utilidad < valorMinimo) {
        valorMinimo = utilidad;
      }

      if (utilidad > valorMaximo) {
        valorMaximo = utilidad;
      }

      if (valorMaximo == valorMinimo) {
        divY = alto;
      } else {
        divY  = alto / (valorMaximo - valorMinimo);
      }

      divX = maxP - minP;

      if (divX <=0 ) {
        divX = 1;
      }

      pasoX = ancho / (2 * divX);
      m1 = minP;
      m2 = maxP;

      if (Math.abs(m1) < Math.abs(m2)) {
        m1 = m2;
      }

      m1 = Math.abs(m1);
      g.drawLine(margenX,margenY - 10,margenX,margenY + alto);
      g.drawLine(margenX,margenY + alto,margenX + ancho + 10,margenY + alto);
      g.drawString(fm.format(-m1 * 100) + "%",margenX - 10,margenY + alto + 20);
      g.drawString("0%" ,margenX + (ancho / 2) - 5,margenY + alto + 20);
      g.drawString(fm.format(100 * m1) + "%",margenX + ancho - 10,margenY + alto + 20);
      g.drawLine(margenX + (ancho / 2),margenY + alto,margenX + (ancho / 2),margenY + alto + 10);
      g.drawLine(margenX + ancho,margenY + alto,margenX + ancho,margenY + alto + 10);
      g.drawLine(margenX - 10,margenY,margenX,margenY);
      g.drawString("%(VM-Vm)/V",margenX + ancho + 35,margenY + alto + 20);
      g.drawString("U",margenX - 25,margenY - 15);
      g.drawString(fmU.format(valorMaximo),margenX - 70,margenY + 4);
      g.drawString(fmU.format(valorMinimo),margenX - 70,margenY + alto + 4);

      for (j=0; j < listaRangos.size(); j++) {
        rango = listaRangos.getRangeBox(j);
        resultado = rango.getBoxResult();
        entrada = rango.getBoxEntry();
        v = entrada.getNodeStateList().getValue();
        y = v;

        if (v == 0) {
          y = 1;
        }

        datos = resultado.getData();

        if (resultado != null) {
          origenX = 0.0;
          origenY = alto;

          if (datos.size() > 0) {
            pu = (ProbUtil) datos.elementAt(0);
            origenX = (ancho / 2) + (pasoX * (((-v + pu.getProbability())) / y));
            origenY = alto - (divY * (pu.getUtility() - valorMinimo));
          }

          destinoX = 0.0;
          destinoY = 0.0;
          oX = 0;
          oY = 0;
          dX = 0;
          dY = 0;

          if (rango.getColor() == Color.white) {
            g.setColor(colores[rango.getOrder() % 10]);
          } else {
            g.setColor(rango.getColor());
          }

          for (i = 1; i < datos.size(); i++) {
            pu = (ProbUtil) datos.elementAt(i);
            destinoX = (ancho / 2) + (pasoX * (((-v + pu.getProbability())) / y));
            destinoY = alto - (divY * (pu.getUtility() - valorMinimo));
            oX = (new Double(origenX)).intValue();
            oY = (new Double(origenY)).intValue();
            dX = (new Double(destinoX)).intValue();
            dY = (new Double(destinoY)).intValue();
            g.drawLine(margenX + oX, margenY + oY + 1, margenX + dX, margenY + dY + 1);
            g.drawLine(margenX + oX, margenY + oY, margenX + dX, margenY + dY);
            g.drawLine(margenX + oX, margenY + oY -1, margenX + dX, margenY + dY -1);
            g.fillRect(margenX + oX - 2,margenY + oY -2,4,4);
            g.fillRect(margenX + dX - 2,margenY + dY -2,4,4);
            origenX = destinoX;
            origenY = destinoY;
          }

        }

      }

      g.setColor(Color.red);
      g.fill3DRect(margenX - 10,margenY + (new Double((valorMaximo - utilidad) * divY)).intValue() - 1,
                   10,2,false);
      g.setColor(Color.darkGray);
      g.drawString(fmU.format(utilidad),margenX - 70,margenY
                   + (new Double((valorMaximo - utilidad) * divY)).intValue() + 4);
    }

  }

} // End of class


