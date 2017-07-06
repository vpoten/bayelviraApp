/*PanelOneParam.java*/

package elvira.sensitivityAnalysis;

import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.*;
import java.util.Vector;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.lang.Math;
import elvira.IDiagram;
import elvira.Elvira;

/**
 * Panel para sacar un grafico de la comparacion de una configuracion y su opuesta .
 * @author jruiz
 * @version 1.0
 */
public class PanelOneParam extends JPanel {

  private RangeBoxList listaRangos;
  private RangeBox rango1;
  private RangeBox rango2;
  private RangeBox rango;
  private BoxResult resultado;
  private BoxEntry entrada;
  private BoxResult resultado1;
  private BoxResult resultado2;
  private Vector datos1;
  private Vector datos2;
  private Vector datos;
  private Vector salida;
  private int margenX = 240;
  private int margenY = 80;
  private NumberFormat fm = new DecimalFormat("0.00");
  private NumberFormat fmU = new DecimalFormat("0.0000");
  protected JLabel etiqueta1;
  protected JLabel etiqueta2;
  private JLabel etiqueta3;
  private JScrollPane scroll;
  private JList lista;
  private double utilidad = 0.0;
  private SensitivityAnalysis cargaRed;
  private IDiagram diag;
  private Color[] colores = new Color[10];
  private Vector listaEstados;
  private String decisionName;

  /**
   * Crea el panel con los datos de entrada y resultados.
   * @param cr Clase principal.
   * @param lr Lista de caja de rangos.
   * @param rec Espacio que ocupa el panel.
   * @param d Decision.
   */
  public PanelOneParam(SensitivityAnalysis cr, RangeBoxList lr,Rectangle rec,Vector listaEstadosDecision,String d) {

    NodeStateList lne1;
    NodeState ne1;
    NodeStateList lne2;
    NodeState ne2;
    NodeStateList lne;
    NodeState ne;
    String t1 = "";
    int i;
    int j;
    ProbUtil pu;
    cargaRed = cr;
    listaRangos = lr;
    double distancia;
    colores[0] = Color.blue;
    colores[1] = Color.red;
    colores[2] = Color.green;
    colores[3] = Color.pink;
    colores[4] = Color.darkGray;
    colores[5] = Color.cyan;
    colores[6] = Color.magenta;
    colores[7] = Color.gray;
    colores[8] = Color.cyan;
    colores[9] = Color.orange;

    setBackground(Color.white);

    if (cargaRed.getDiag() != null && listaRangos.size() > 0) {
      diag = cr.getDiag();
	decisionName = d;
	listaEstados = listaEstadosDecision;

      //Se calcula la utilidad
      utilidad = cargaRed.getUtility(diag);

      setLayout(null);
      setBounds(rec);
      etiqueta1 = new JLabel();
      etiqueta1.setText("");
      etiqueta1.setBounds(new Rectangle(10, margenY - 60, 400, 15));
      add(etiqueta1, null);
      etiqueta2 = new JLabel();
      etiqueta2.setText("");
      etiqueta2.setBounds(new Rectangle(10, margenY - 45, 400, 15));
      add(etiqueta2, null);
      etiqueta3 = new JLabel();
      etiqueta3.setText("[ " + Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Estado")
                        +
                        " => " +Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Valor")
                        +
                        " => " +Elvira.localize(cargaRed.getDialogBundle(),
                                               "SensitivityAnalysis.Utilidad") + " ]");
      etiqueta3.setBounds(new Rectangle(10, margenY - 30, margenX - 50, 15));
      add(etiqueta3, null);
      scroll = new JScrollPane();
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setBounds(new Rectangle(10, margenY - 10, margenX - 100,
                                     getHeight() - margenY - 30));
      add(scroll);
      lista = new JList();
      rango = listaRangos.getRangeBox(0);
      resultado = rango.getBoxResult();
      entrada = rango.getBoxEntry();
      salida = new Vector();
      lne = entrada.getNodeStateList();
      ne = lne.getNodeState(0);

      if (entrada.isUtility()) {
        t1 = "U(";
      } else {
        t1 = "P(";
      }

      t1 = t1 + ne.getName();

      if (lne.size() > 1) {

        if (!entrada.isUtility()) {
          t1 = t1 + " |";
        }

      }

      for (i = 1; i < lne.size(); i++) {
        ne = lne.getNodeState(i);
        t1 = t1 + " " + ne.getName();
      }

      t1 = t1 + ") ";
      etiqueta1.setText(t1);

      for (j = 0; j < listaRangos.size(); j++) {
        rango = listaRangos.getRangeBox(j);
        resultado = rango.getBoxResult();
        datos = resultado.getData();

        for (i = 0; i < datos.size(); i++) {
          pu = (ProbUtil) datos.elementAt(i);
          salida.add(decisionName + "=" + withoutQm((String)listaEstados.elementAt(j)) 
				+ " ==> " + fm.format(pu.getProbability()) + " => " +
                        fmU.format(pu.getUtility()));
        }

      }

      if (listaRangos.size() == 2) {
        rango1 = listaRangos.getRangeBox(0);
        rango2 = listaRangos.getRangeBox(1);
        resultado1 = rango1.getBoxResult();
        resultado2 = rango2.getBoxResult();
        datos1 = resultado1.getData();
        datos2 = resultado2.getData();

        if (resultado1 != null & resultado2 != null) {
          distancia = (minUDistancia(resultado1, resultado2)).getUtility();
          /** Coded modified by mluque **/
/*          etiqueta2.setText(Elvira.localize(cargaRed.getDialogBundle(),
                                            "SensitivityAnalysis.PuntoCercano")+" [ Va,Ut ]=[ " +
                            fm.format( (minUDistancia(resultado1, resultado2)).
                                       getProbability()) +
                            " , " + fmU.format(distancia) + " ]");*/
        }

      }

      scroll.getViewport().add(lista, null);
    }

  }

  /**
   * Calcula el punto de probabilidad-utilidad de menor distancia entre los puntos de dos vectores de
   * ResultadoRango .
   * @param entrada1 es el ResultadoRango
   * @param entrada2 es el otro ResultadoRango
   * @return Devuelve la menor distancia
   */
  public ProbUtil minUDistancia(BoxResult entrada1, BoxResult entrada2) {

    double distancia = Double.POSITIVE_INFINITY;
    double e1 = 0.0;
    double e2 = 0.0;
    double p1 = 0.0;
    double p2 = 0.0;
    double punto = 0.0;
    ProbUtil pu1;
    ProbUtil pu2;
    Vector datos1;
    Vector datos2;
    ProbUtil pu = new ProbUtil();
    int i;
    int j;

    datos1 = entrada1.getData();
    datos2 = entrada2.getData();

    if (datos1.size() > 0 && datos1.size() > 0) {

      for (i=0; i < datos1.size(); i++) {
        pu1 = (ProbUtil)datos1.elementAt(i);
        p1 = pu1.getProbability();
        e1 = pu1.getUtility();

        for (j=0; j < datos2.size(); j++) {
          pu2 = (ProbUtil)datos2.elementAt(j);
          p2 = pu2.getProbability();
          e2 = pu2.getUtility();
          punto = e1;

          if (Math.abs(p1 - p2) + Math.abs(e1 - e2) < distancia) {
            distancia = Math.abs(p1 - p2) + Math.abs(e1 - e2);
            punto = e1;
            pu.setProbability(p1);
            pu.setUtility(e1);
          }

        }

      }

    }

    return pu;
  }

  /**
   * Quita comillas a una cadena.
   * @param s Cadena
   */
  private String withoutQm(String s) {
     if (s.substring(0,1).equals("\"")) {
       return (s.substring(1,s.length()-1));
     } else {
       return s;
     }
  }

  /**
   * Repinta el panel.
   * @param g
   */
  public void paint(Graphics g) {

    int ancho;
    int alto;
    double destinoX = 0.0;
    double destinoY = 0.0;
    int oX;
    int oY;
    int dX;
    int dY;
    double minU;
    double maxU;
    double minP;
    double maxP;
    double divY;
    double divX;
    double pasoX;
    double pasoY;
    double origenX;
    double origenY;
    ProbUtil pu;
    int i;
    int j;
    double valor;

    super.paint(g);

    if (listaRangos != null && listaRangos.size() > 0) {
      scroll.setBounds(new Rectangle(10, margenY - 10, margenX - 80,
                                     getHeight() - margenY - 30));
      scroll.getViewport().add(lista, null);
      lista.setListData(salida);
      g.setColor(Color.black);
      ancho = getWidth() - margenX - 50;
      alto = getHeight() - margenY - 50;
      minP = Double.POSITIVE_INFINITY;
      maxP = Double.NEGATIVE_INFINITY;
      minU = minP;
      maxU = maxP;
	valor = listaRangos.getRangeBox(0).getBoxEntry().getNodeStateList().getValue();

      for (i = 0; i < listaRangos.size(); i++) {
        rango = listaRangos.getRangeBox(i);
        entrada = rango.getBoxEntry();
        resultado = rango.getBoxResult();

        if (entrada.getMinValue() < minP) {
          minP = entrada.getMinValue();
        }

        if (entrada.getMaxValue() > maxP) {
          maxP = entrada.getMaxValue();
        }

        if (resultado.getMinUtil() < minU) {
          minU = resultado.getMinUtil();
        }

        if (resultado.getMaxUtil() > maxU) {
          maxU = resultado.getMaxUtil();
        }

        if (utilidad < minU) {
          minU = utilidad;
        }

        if (utilidad > maxU) {
          maxU = utilidad;
        }

      }

      divY = maxU - minU;

      if (divY <= 0) {
        divY = 1;
      }

      divX = maxP - minP;

      if (divX <= 0) {
        divX = 1;
      }

      pasoX = ancho / divX;
      pasoY = alto / divY;
      g.drawLine(margenX, margenY - 10, margenX, margenY + alto);
      g.drawLine(margenX, margenY + alto, margenX + ancho + 10, margenY + alto);
      g.drawString(fm.format(minP), margenX - 8, margenY + alto + 20);
      g.drawString(fm.format(minP + (maxP - minP) / 2),
                   margenX + (ancho / 2) - 8, margenY + alto + 20);
      g.drawString(fm.format(maxP), margenX + ancho - 8, margenY + alto + 20);
      g.drawLine(margenX + (ancho / 2), margenY + alto, margenX + (ancho / 2),
                 margenY + alto + 10);
      g.drawLine(margenX + ancho, margenY + alto, margenX + ancho,
                 margenY + alto + 10);
      g.drawLine(margenX - 5, margenY, margenX, margenY);
      //Code modified by mluque
      //g.drawString("Va", margenX + ancho + 20, margenY + alto + 20);
      g.drawString("U", margenX - 25, margenY - 15);

      for (i = 0; i < listaRangos.size(); i++) {
        rango = listaRangos.getRangeBox(i);
        resultado = rango.getBoxResult();

        if (resultado != null) {
          datos = resultado.getData();
          origenX = 0.0;
          origenY = alto;

          if (datos.size() > 0) {
            pu = (ProbUtil) datos.elementAt(0);
            origenX = pasoX * ( -minP + pu.getProbability());
            origenY = alto - (pasoY * (pu.getUtility() - minU));
          }

          destinoX = 0.0;
          destinoY = 0.0;
          oX = 0;
          oY = 0;
          dX = 0;
          dY = 0;
          g.setColor(colores[i % 10]);
          //Modified by mluque
          //g.drawString(decisionName+" = "+withoutQm((String)listaEstados.elementAt(i)), margenX, 20 + (10 * i));
          g.drawString(decisionName+" = "+withoutQm((String)listaEstados.elementAt(i)), margenX, 40 + (15 * i));

          for (j = 1; j < datos.size(); j++) {
            pu = (ProbUtil) datos.elementAt(j);
            destinoX = pasoX * ( -minP + pu.getProbability());
            destinoY = alto - (pasoY * (pu.getUtility() - minU));
 
           oX = (new Double(origenX)).intValue();
            oY = (new Double(origenY)).intValue();
            dX = (new Double(destinoX)).intValue();
            dY = (new Double(destinoY)).intValue();
            g.drawLine(margenX + oX, margenY + oY + 1, margenX + dX,
                       margenY + dY + 1);
            g.drawLine(margenX + oX, margenY + oY, margenX + dX, margenY + dY);
            g.drawLine(margenX + oX, margenY + oY - 1, margenX + dX,
                       margenY + dY - 1);
            g.fillRect(margenX + oX - 2, margenY + oY - 2, 4, 4);
            g.fillRect(margenX + dX - 2, margenY + dY - 2, 4, 4);
            origenX = destinoX;
            origenY = destinoY;
          }

        }

        g.setColor(Color.orange);
        g.fill3DRect(margenX + (new Double( (valor - minP) * pasoX)).intValue(),margenY,2,alto + 10,false);
        g.fill3DRect(margenX - 5,
                     margenY + 5 + (new Double( (maxU - utilidad) * pasoY)).intValue(), ancho, 2, false);
        g.setColor(Color.darkGray);
	  g.drawString(fm.format(valor),margenX - 8 + (new Double( (valor - minP) * pasoX)).intValue(),margenY + alto + 20);

      /*  g.drawString(fm.format(utilidad),margenX - 30,margenY + 5 
                     + (new Double( (maxU - utilidad) * pasoY)).intValue());
        g.drawString(fm.format(minU),margenX - 30,margenY + 5 
                     + (new Double( (maxU - minU) * pasoY)).intValue());
        g.drawString(fm.format(maxU),margenX - 30,margenY + 5 
                     + (new Double( (maxU - maxU) * pasoY)).intValue());*/
	  
	  //Modified by mluque
	    g.drawString(fm.format(utilidad),margenX - 40,margenY + 5 
      + (int)Math.round((maxU - utilidad) * pasoY));
g.drawString(fm.format(minU),margenX - 40,margenY + 5 
      + (int)Math.round((maxU - minU) * pasoY));
g.drawString(fm.format(maxU),margenX - 40,margenY + 5 
      + (int)Math.round( (maxU - maxU) * pasoY)-5);
      }

    } else {
      g.setColor(Color.black);
      g.drawString(Elvira.localize(cargaRed.getDialogBundle(),
                                   "SensitivityAnalysis.SelecUnicoParam"),10,20);
    }

  }

} //End of class