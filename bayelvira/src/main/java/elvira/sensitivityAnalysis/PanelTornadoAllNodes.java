/*PanelTornadoAllNodes.java*/

package elvira.sensitivityAnalysis;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.io.IOException;

/**
 * Clase que contiene el panel donde se van a dibujar los diagramas de tornado de
 * cualquier configuracion posible que haya en el diagrama.
 * @author jruiz
 * @version 1.0
 */
public class PanelTornadoAllNodes extends JPanel {

  private RangeBoxList listaRangos;
  private int margenX = 60;
  private int margenY = 30;
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
  public PanelTornadoAllNodes(RangeBoxList lr,double u) throws elvira.parser.
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
      RangeBox rango;
      double valorMinimo = 0.0;
      double valorMaximo = 0.0;
      double[] origenX = new double[listaRangos.size()];
      double[] origenY = new double[listaRangos.size()];
      double[] destinoX = new double[listaRangos.size()];
      double[] destinoY = new double[listaRangos.size()];
      int i;
      int j;
      int k;
      double divY;
      int oY = 0;
      int oX = 0;
      int dY = 0;
      int dX = 0;

      g.setColor(Color.black);
      ancho = getWidth() - margenX - 80;
      alto = getHeight() - margenY - 50;
      listaRangos = listaRangos.ordenarResultados();
      grosor = alto / ( 2 * listaRangos.size());
      rango = listaRangos.getRangeBox(0);
      resultado = rango.getBoxResult();
      origenY[0] = 20;
      origenX[0] = resultado.getMinUtil();
      destinoY[0] = origenY[0];
      destinoX[0] = resultado.getMaxUtil();
      valorMinimo = resultado.getMinUtil();
      valorMaximo = resultado.getMaxUtil();

      for (i=1; i < listaRangos.size(); i++) {
        rango = listaRangos.getRangeBox(i);
        resultado = rango.getBoxResult();
        origenY[i] = (i + 1) * 20;
        origenX[i] = resultado.getMinUtil();
        destinoY[i] = origenY[i];
        destinoX[i] = resultado.getMaxUtil();

        if( resultado.getMinUtil() < valorMinimo) {
          valorMinimo = resultado.getMinUtil();
        }

        if (resultado.getMaxUtil() > valorMaximo) {
          valorMaximo = resultado.getMaxUtil();
        }

      }

      i--;

      if (utilidad < valorMinimo) {
        valorMinimo = utilidad;
      }

      if (utilidad > valorMaximo) {
        valorMaximo = utilidad;
      }

      for (j=0; j < listaRangos.size(); j++) {
        origenX[j] = origenX[j] - valorMinimo;
        destinoX[j] = destinoX[j] - valorMinimo;
      }

      if (valorMaximo == valorMinimo) {
        divY = ancho;
      } else {
        divY  = ancho / (valorMaximo - valorMinimo);
      }

      g.drawLine(margenX,margenY - 10,margenX,margenY + ((i + 1) * 20));
      g.drawLine(margenX,margenY + ((i + 1) * 20),margenX + ancho + 10,margenY + ((i + 1) * 20));
      g.drawString("U",margenX + ancho + 30,margenY + ((i + 1) * 20) + 20);
      g.drawString("Conf",margenX + (new Double((utilidad -  valorMinimo) * divY)).intValue()- 12,
                   margenY - 15);
      g.drawString("UMin",margenX - 55,margenY - 15);
      g.drawString("UMax",margenX + ancho + 10,margenY - 15);
      g.drawString(fm.format(valorMinimo),margenX - 5,margenY + ((i + 1) * 20) + 20);
      g.drawString(fm.format(valorMaximo),margenX + ancho - 10,margenY + ((i + 1) * 20) + 20);

      for (k=0; k < listaRangos.size(); k++) {
        oY = (new Double(origenY[k])).intValue();
        oX = (new Double(origenX[k]*divY)).intValue();
        dY = (new Double(destinoY[k])).intValue();
        dX = (new Double(destinoX[k]*divY)).intValue();
        rango = listaRangos.getRangeBox(k);
        resultado = rango.getBoxResult();
        g.setColor(Color.blue);
        g.drawLine(margenX,margenY + oY - 5,margenX + ancho,margenY + oY - 5);

        if (rango.getColor() == Color.white) {
          g.setColor(colores[rango.getOrder() % 10]);
        } else {
          g.setColor(rango.getColor());
        }

        g.fill3DRect(margenX + oX,margenY + oY - 8,dX - oX,dY - oY + 8,false);
        g.setColor(Color.black);
        g.drawString(fmU.format(resultado.getMinUtil()),margenX - 55,margenY + oY);
        g.drawString(fmU.format(resultado.getMaxUtil()),margenX + ancho + 10,margenY + oY);
      }

      g.setColor(Color.red);
      g.fill3DRect(margenX + (new Double((utilidad -  valorMinimo) * divY)).intValue(),
                   margenY,1,margenY + (i * 20) - 5,false);
      g.setColor(Color.black);
      g.setFont(new Font("System",Font.BOLD,12));

      for(k=0; k < listaRangos.size(); k++) {
        rango = listaRangos.getRangeBox(k);
        oY = (new Double(origenY[k])).intValue();
        g.drawString(String.valueOf(rango.getOrder()),
                     margenX + (new Double((utilidad -  valorMinimo) * divY)).intValue() + 2,
                     margenY + oY - 9);
      }

      g.setColor(Color.darkGray);
      g.drawString(fm.format(utilidad),
                   margenX + (new Double((utilidad -  valorMinimo) * divY)).intValue()- 15,
                   margenY + ((i + 1) * 20) + 20);

    }

  }

} // End of class


