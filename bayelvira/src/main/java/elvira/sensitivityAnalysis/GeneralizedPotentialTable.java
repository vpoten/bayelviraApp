/* GeneralizedPotentialTable.java */

package elvira.sensitivityAnalysis;

import java.io.*;
import java.util.Random;
import java.util.Vector;
import java.util.Hashtable;
import java.text.*;
import elvira.potential.*;

import elvira.*;


/**
 * Implementa una tabla generalizada.
 *
 * @author jruiz
 */

public class GeneralizedPotentialTable extends PotentialTable {

  /**
   * Valores generalizados para utilizar rangos.
   */
  private GeneralizedValues generalizedValues;

  /* CONSTRUCTORS */

  /**
   * Constructs a new <code>GeneralizedPotentialTable</code> with an empty list
   * of variables and a single value (not initialized).
   */

  public GeneralizedPotentialTable() {

    super();

    generalizedValues = new GeneralizedValues(1);
  }

  /**
   * Constructs a <code>GeneralizedPotentialTable</code> from
   * a <code>PotentialTable</code>.
   * @param gpt a <code>PotentialTable</code>.
   */
  public GeneralizedPotentialTable(PotentialTable pot) {

    int i, nv;

    setVariables((Vector) pot.getVariables().clone());
    nv = (int)FiniteStates.getSize(getVariables()); // Size of the array.
    setValues(new double[nv]);

    // Evaluate the tree for each possible configuration.
    for (i=0 ; i<nv ; i++) {
      getValues()[i] = pot.getValue(i);
    }
    setGeneralizedValues(new GeneralizedValues(nv));
    setNames(null);
  } 

  /**
   * Constructs a new <code>GeneralizedPotentialTable</code> with an empty list
   * of variables and a given number of values in the array.
   * @param numberOfValues the number of values in the array.
   */

  public GeneralizedPotentialTable(int numberOfValues) {

    super(numberOfValues);

    generalizedValues = new GeneralizedValues(numberOfValues);
  }

  /**
   * Constructs a new <code>GeneralizedPotentialTable</code> for a single variable
   * and sets the values to 0.
   * @param var a <code>FiniteStates</code> variable.
   */

  public GeneralizedPotentialTable(FiniteStates var) {

    super(var);

    generalizedValues = new GeneralizedValues(getSize());
  }

  /**
   * Constructs a new <code>GeneralizedPotentialTable</code> for a list of
   * variables and creates an array to store the values.
   * @param vars a <code>Vector</code> of
   * variables (<code>FiniteStates</code>).
   */

  public GeneralizedPotentialTable(Vector vars) {

    super(vars);

    generalizedValues = new GeneralizedValues(getSize());
  }

  /**
   * Constructs a new <code>GeneralizedPotentialTable</code> for a list of
   * variables and creates an array to store the values.
   * @param vars the list of variables as a <code>NodeList</code>.
   */

  public GeneralizedPotentialTable(NodeList vars) {

    super(vars);

    generalizedValues = new GeneralizedValues(getSize());
  }

  /**
   * Constructs a <code>GeneralizedPotentialTable</code> from a <code>NodeList</code>
   * and a <code>Relation</code> defined over a subset of variables of
   * the <code>NodeList</code> passed as parameter.
   * If the potential attached to the relation passed as parameter
   * is not defined over a subset of the variables in the list,
   * the method builds a unitary potential.
   *
   * @param vars the <code>NodeList</code> of variables for the
   * new potential.
   * @param rel the <code>Relation</code> defined over a subset of
   * <code>vars</code>.
   */

  public GeneralizedPotentialTable(NodeList vars, Relation rel) {
    int i, nv, pos;
    Configuration conf, subConf;
    NodeList nl;
    Range range;
    String name;

    setVariables((Vector) vars.toVector().clone());
    nv = (int) FiniteStates.getSize(getVariables()); // Size of the array.
    setValues(new double[nv]);

    generalizedValues = new GeneralizedValues(nv);

    // determining if pot.variables is a subset of vars

    if ( (rel.getVariables().kindOfInclusion(vars)).equals("subset")) {
      conf = new Configuration(getVariables());

      for (i = 0; i < nv; i++)
        getValues()[i] = 0.0;

      for (i = 0; i < nv; i++) {
        subConf = new Configuration(conf, rel.getVariables());
        pos = subConf.getIndexInTable();
        getValues()[i] += ( (GeneralizedPotentialTable) rel.getValues()).getValue(pos);

        range = ( (GeneralizedPotentialTable) rel.getValues()).generalizedValues.getRange(
            pos);
        generalizedValues.setRange(range, i);
        name = ( (GeneralizedPotentialTable) rel.getValues()).generalizedValues.getName(
            pos);
        generalizedValues.setName(name, i);

        conf.nextConfiguration();
      }
    }
    else { // unitary potential
      for (i = 0; i < nv; i++)
        getValues()[i] = 1.0;

      generalizedValues.setRange(null, i);
      generalizedValues.setName(null, i);
    }
  }

  /**
   * Constructs a <code>GeneralizedPotentialTable</code> from a <code>NodeList</code>
   * and a <code>Potential</code> defined over a subset of variables of
   * the <code>NodeList</code> passed as parameter. The new potential
   * is built by extending the passed potential to the whole set of
   * variables represented by the <code>NodeList</code>
   * if pot.getVariables is not a subset of vars, then an unitary potential
   * is created
   *
   * @param vars the <code>NodeList</code> of variables for the
   * new potential.
   * @param pot the <code>PotentialTabe</code> defined over a subset of
   * <code>vars</code>.
   */

  public GeneralizedPotentialTable(NodeList vars, GeneralizedPotentialTable pot) {

    int i, nv, pos;
    Configuration conf, subConf;
    NodeList nl;
    Range range;
    String name;

    setVariables((Vector) vars.toVector().clone());
    nv = (int) FiniteStates.getSize(getVariables()); // Size of the array.
    setValues(new double[nv]);

    generalizedValues = new GeneralizedValues(nv);

    // determining if pot.variables is a subset of vars

    nl = new NodeList(pot.getVariables());
    if (nl.kindOfInclusion(vars).equals("subset")) {
      conf = new Configuration(getVariables());

      for (i = 0; i < nv; i++) {
        subConf = new Configuration(conf, nl);
        pos = subConf.getIndexInTable();
        getValues()[i] = pot.getValue(pos);

        range = pot.generalizedValues.getRange(pos);
        generalizedValues.setRange(range, i);
        name = pot.generalizedValues.getName(pos);
        generalizedValues.setName(name, i);

        //setValue(conf,pot.getValue(subConf));
        conf.nextConfiguration();
      }
    }
    else { // unitary potential
      for (i = 0; i < nv; i++)
        getValues()[i] = 1.0;

      generalizedValues.setRange(null, i);
      generalizedValues.setName(null, i);
    }
  }

  /**
   * Constructs a new Conditional P(X|Z) <code>GeneralizedPotentialTable</code>
   * for a list of variables and creates an array to store the values
   * generated randomly.
   * @param generator. A random number generator, instance
   * of class <code>Random</code>.
   * @param vars a <code>NodeList</code> over which the new potential
   * will be defined.
   */

  public GeneralizedPotentialTable(Random generator, NodeList nodes, int degreeOfExtreme) {

    int nv, i;
    double sum, r;
    Configuration conf = new Configuration(nodes);
    GeneralizedPotentialTable potmarg, pot;
    Range range;
    String name;

    nv = (int) FiniteStates.getSize(nodes);

    setVariables(nodes.copy().toVector());
    setValues(new double[nv]);

    generalizedValues = new GeneralizedValues(nv);

    sum = 0.0;
    for (i = 0; i < nv; i++) {
      r = generator.nextDouble();
      getValues()[conf.getIndexInTable()] = Math.pow(r, (float) degreeOfExtreme);

      range = null;
      generalizedValues.setRange(range, i);
      name = null;
      generalizedValues.setName(name, i);

      conf.nextConfiguration();
    }

    normalize();
    if (getVariables().size() > 1) {
      potmarg = (GeneralizedPotentialTable)this.addVariable( (FiniteStates)
                                                 getVariables().elementAt(0));
      pot = (GeneralizedPotentialTable)this.divide(potmarg);
      setValues(pot.getValues());

      generalizedValues = pot.generalizedValues.copy();
    }
  }

  /* METHODS */

  /**
   * Constructs a new <code>GeneralizedPotentialTable</code>
   * for a list of variables and creates an array to store the values
   * generated randomly (uniform distribution). This method is appropriate to generate
   * randomly non-negative utility tables.
   * @param generator. A random number generator, instance
   * of class <code>Random</code>.
   * @param nodes a <code>NodeList</code> over which the new potential
   * will be defined.
   * @param d Maximum value for the potential. The range of the potential is [0,d].
   */
  public GeneralizedPotentialTable(Random generator, NodeList nodes, double d) {

    int nv, i;
    double r;
    Configuration conf = new Configuration(nodes);
    Range range;
    String name;

    nv = (int) FiniteStates.getSize(nodes);

    setVariables(nodes.copy().toVector());
    setValues(new double[nv]);

    generalizedValues = new GeneralizedValues(nv);

    for (i = 0; i < nv; i++) {
      r = (generator.nextDouble()) * d;
      getValues()[conf.getIndexInTable()] = r;

      range = null;
      generalizedValues.setRange(range, conf.getIndexInTable());
      name = null;
      generalizedValues.setName(name, conf.getIndexInTable());

      conf.nextConfiguration();
    }

  }

  /**
   * Devuelve el numero de estados de la tabla de probabilidades.
   * @return Numero de estados.
   */
  public int getNumStates() {

    return ( (FiniteStates) getVariables().elementAt(0)).getNumStates();
  }

  /**
   * Este metodo convierte # en los complementarios de los valores en un nodo de azar.
   */
  public void complementValues() {

    double v;
    Double d;
    int j;
    int i;
    int s;
    double resto;
    int nEstados;
    int k = 0;
    int salto;

    nEstados = getNumStates();
    salto = (int) (getSize() / nEstados);
    s = 0;
    while (s < getSize()) {
      i = s;
      while (i < getSize()) {
        v = getValue(i);
        d = new Double(v);
        if (d.isNaN()) {
          resto = 0;
          for (j = 1; j < nEstados; j++) {
            k = (i + (int) (j * salto)) % (int) getSize();
            resto = resto + getValue(k);
          }
          setValue(i, 1 - resto);
          break;
        }
        i = i + salto;
      }
      s = s + 1;
    }
  }

  /**
   * Este metodo convierte # en los complementarios de los rangos en un nodo de azar.
   */
  public void complementRanges() {

    double v;
    double r11;
    double r12;
    Double d;
    Double d11;
    Double d12;
    String n1 = null;
    int j;
    int i;
    int s;
    double resto1;
    double resto2;
    int nEstados;
    int k = 0;
    int salto;

    nEstados = getNumStates();
    salto = (int) (getSize() / nEstados);
    s = 0;
    while (s < getSize()) {
      i = s;
      while (i < getSize()) {
        v = getValue(i);
        d = new Double(v);
        r11 = getMinRange(i);
        r12 = getMaxRange(i);
        n1 = getName(i);
        d11 = new Double(r11);
        d12 = new Double(r12);
        if (d11.isNaN() || d12.isNaN()) {
          resto1 = 0;
          resto2 = 0;
          for (j = 1; j < nEstados; j++) {
            k = (i + (int) (j * salto)) % (int) getSize();
            resto1 = resto1 + getMinRange(k);
            resto2 = resto2 + getMaxRange(k);
          }
          setRange(i, 1 - resto2, 1 - resto1);
          if (n1 == null)
            setName(i, "");
          break;
        }
        i = i + salto;
      }
      s = s + 1;
    }
  }

  /**
   * Este metodo convierte en # los complementarios de los valores en un nodo de azar.
   */
  public void uncomplementChance() {

    double v = Double.NaN;
    String n = null;
    int i;
    int j;
    int salto;
    boolean alguno,todos;

    salto = (int) (getSize() / getNumStates());
    for (j = 0; j < salto; j++) {
      i = j;
	todos = true;
      while (i < getSize()) {
        if (getRange(i) != null) {
          todos = false;
          break;
        }
        i = i + salto;
      }
	if(todos==false) {
        alguno = false;
        i = j;
        while (i < getSize()) {
          if (getRange(i) == null) {
            setRange(i, Double.NaN,Double.NaN);
            setName(i, n);
            alguno = true;
            break;
          }
          i = i + salto;
        }
      }
    }
  }

  /**
   * Pone un valor de rango en todas las posiciones.
   * @param min Valor minimo del rango.
   * @param max Valor maximo del rango.
   */
  public void setRange(double min, double max) {

    Range range;

    for (int i = 0; i < generalizedValues.size(); i++) {
      range = new Range(min, max);
      generalizedValues.setRange(range, i);
    }
  }

  /**
   * Pone un valor de rango en todas las posiciones.
   * @param range
   */
  public void setRange(Range range) {

    for (int i = 0; i < generalizedValues.size(); i++) {
      generalizedValues.setRange(range, i);
    }
  }

  /**
   * Pone un valor de rango en una posicion concreta.
   * @param pos Posicion dentro del vector de valores generalizados.
   * @param min Valor minimo del rango.
   * @param max Valor maximo del rango.
   */
  public void setRange(int pos, double min, double max) {

    Range range = new Range(min, max);
    generalizedValues.setRange(range, pos);
  }

  /**
   * Pone un rango en una configuracion.
   * @param conf Configuracion.
   * @param min Minimo.
   * @param max Maximo.
   */
  public void setRange(Configuration conf, double min, double max) {

    int index;
    Configuration aux;

    aux = new Configuration(getVariables(), conf);
    index = aux.getIndexInTable();
    setRange(index, min, max);
  }

  /**
   * Pone un rango en una configuracion.
   * @param conf Configuracion.
   * @param range Rango.
   */
  public void setRange(Configuration conf, Range range) {

    int index;
    Configuration aux;

    aux = new Configuration(getVariables(), conf);
    index = aux.getIndexInTable();
    setRange(index, range.getMin(), range.getMax());
  }

  /**
   * Pone un rango en una posicion.
   * @param pos Posicion.
   * @param range Rango.
   */
  public void setRange(int pos, Range range) {

    generalizedValues.setRange(range, pos);
  }

  /**
   * Extrae un rango de una posicion.
   * @param pos Posicion.
   * @return rango.
   */
  public Range getRange(int pos) {

    return (Range) generalizedValues.getRange(pos);
  }

  /**
   * Extrae un rango de una configuracion.
   * @param conf Configuracion.
   * @return Rango.
   */
  public Range getRange(Configuration conf) {

    int pos;
    Configuration aux;

    aux = new Configuration(getVariables(), conf);
    pos = aux.getIndexInTable();
    return getRange(pos);
  }

  /**
   * Comprueba si los rangos estan vacion.
   * @return True o false.
   */
  public boolean allRangesNull() {

    boolean retorno = true;
    Double min;
    Double max;
    int i;

    for (i = 0; i < generalizedValues.size(); i++) {
      min = new Double(getMinRange(i));
      max = new Double(getMaxRange(i));
      if (!min.isNaN()) {
        retorno = false;
        break;
      }
      else if (!max.isNaN()) {
        retorno = false;
        break;
      }
    }
    return retorno;
  }

  /**
   * Gets the ranges of the potential.
   * @return an Vector of <code>ranges</code> with the ranges of the potential.
   */
  public GeneralizedValues getRanges() {

    return generalizedValues;
  }

  /**
   * Devuelve el valor minimo de rango de una configuracion.
   * Si no hay rangos definidos , le da el valor fijo.
   * @param conf Configuracion.
   * @return Valor minimo del rango.
   */
  public double getMinRange(Configuration conf) {

    int pos;
    Configuration aux;
    Range range;
    // Take a configuration from conf just for variables
    // in the potential.
    aux = new Configuration(getVariables(), conf);
    pos = aux.getIndexInTable();
    if (generalizedValues.size() > pos) {
      range = generalizedValues.getRange(pos);
      return range.getMin();
    }
    else {
      return getValues()[pos];
    }
  }

  /**
   * Devuelve el valor maximo de rango de una configuracion.
   * Si no hay rangos definidos , le da el valor fijo.
   * @param conf Configuracion.
   * @return Valor maximo del rango.
   */
  public double getMaxRange(Configuration conf) {

    int pos;
    Configuration aux;
    Range range;
    // Take a configuration from conf just for variables
    // in the potential.
    aux = new Configuration(getVariables(), conf);
    pos = aux.getIndexInTable();
    if (generalizedValues.size() > pos) {
      range = generalizedValues.getRange(pos);
      return range.getMax();
    }
    else {
      return getValues()[pos];
    }
  }

  /**
   * Devuelve el valor minimo de rango de una posicion.
   * @param index Posicion.
   * @return Valor minimo del rango.
   */
  public double getMinRange(int index) {

    Range range = generalizedValues.getRange(index);

    return range.getMin();
  }

  /**
   * Devuelve el valor maximo de rango de una posicion.
   * @param index Posicion.
   * @return Valor maximo del rango.
   */
  public double getMaxRange(int index) {

    Range range = generalizedValues.getRange(index);

    return range.getMax();
  }

  /**
   * Sets the generalized values of the potential.
   * @param r the ranges, as an array of <code>double</code>.
   */
  public void setGeneralizedValues(GeneralizedValues r) {

    generalizedValues = r;
  }

  /**
   * Pone un valor de nombre en todas las posiciones.
   * @name Nombre.
   */
  public void setName(String name) {

    for (int i = 0; i < generalizedValues.size(); i++) {
      generalizedValues.setName(name, i);
    }
  }

  /**
   * Pone un valor de nombre en todas las posiciones.
   * @name Nombre.
   */
  public void setNames(String name) {

    for (int i = 0; i < generalizedValues.size(); i++) {
      generalizedValues.setName(name, i);
    }
  }

  /**
   * Pone un valor de nombre en una posicion concreta.
   * @param pos Posicion dentro del vector de valores generalizados.
   * @name Nombre.
   */
  public void setName(int pos, String name) {

    generalizedValues.setName(name, pos);
  }

  /**
   * Pone un valor de nombre en una posicion concreta.
   * @name Nombre.
   * @param pos Posicion dentro del vector de valores generalizados.
   */
  public void setName(String name, int pos) {

    generalizedValues.setName(name, pos);
  }

  /**
   * Pone un nombre en una configuracion.
   * @param conf Configuracion.
   * @param name Nombre.
   */
  public void setName(Configuration conf, String name) {

    int index;
    Configuration aux;

    aux = new Configuration(getVariables(), conf);
    index = aux.getIndexInTable();
    setName(index, name);
  }

  /**
   * Extrae un nombre de una posicion.
   * @param pos Posicion.
   * @return Nombre.
   */
  public String getName(int pos) {

    return (String) generalizedValues.getName(pos);
  }

  /**
   * Extrae un nombre de una configuracion.
   * @param conf Configuracion.
   * @return Nombre.
   */
  public String getName(Configuration conf) {

    int pos;
    Configuration aux;

    aux = new Configuration(getVariables(), conf);
    pos = aux.getIndexInTable();
    return getName(pos);
  }

  /**
   * Comprueba si los nombres estan vacios.
   * @return True o false.
   */
  public boolean allNamesNull() {

    boolean retorno = true;
    int i;

    for (i = 0; i < generalizedValues.size(); i++) {
      if (generalizedValues.getName(i) != null) {
        retorno = false;
        break;
      }
    }
    return retorno;
  }

  /**
   * Gets the generalized values of the potential.
   * @return an Vector of <code>ranges</code> with the names of the potential.
   */
  public GeneralizedValues getGeneralizedValues() {

    return generalizedValues;
  }

  /**
   * Saves a potential to a file. This one must be used when
   * saving a network.
   * @param p the <code>PrintWriter</code> where the potential will be written.
   */

  public void saveAsConfig(PrintWriter p) {

    int i, total;
    Double min, max;
    String m1 = "";
    String m2 = "";
    Range range;
    String name;
    Configuration conf;

    p.print("values= generalizedTable (");

    total = (int) FiniteStates.getSize(getVariables());

    conf = new Configuration(getVariables());
    
    for (i = 0; i < total; i++) {
      p.print("                ");
      conf.save(p);
      range = generalizedValues.getRange(i);
      if(range==null) {
        p.print(getValues()[i]);
	} else {
        min = new Double(range.getMin());
        max = new Double(range.getMax());
        name = generalizedValues.getName(i);
        if (min.isNaN() && max.isNaN()) {
          p.print("#");
        }
        else {
          p.print(getValues()[i]);
          if (!min.isNaN() && !max.isNaN()) {
            m1 = min.toString();
            m2 = max.toString();
            p.print("|range(" + m1 + "," + m2 + ")");
          }
          if (name != null && !name.equals("") && !name.equals("\"\"")) {
            p.print("|" + name);
          }
        }
      }
      p.print(",\n");
      conf.nextConfiguration();
    }
    p.print("                );\n");
  }

  /**
   * Saves a potential to a file. This one must be used when
   * saving a network. The values are written as a table.
   * @param p the <code>PrintWriter</code> where the potential will be written.
   */

  public void save(PrintWriter p) {

    int i, total;
    Double min, max;
    String m1 = "";
    String m2 = "";
    Range range;
    String name;

    p.print("values= generalizedTable (");

    total = (int) FiniteStates.getSize(getVariables());
    
    for (i = 0; i < total; i++) {
      range = generalizedValues.getRange(i);
      if(range==null) {
        p.print(getValues()[i]);
	} else {
        min = new Double(range.getMin());
        max = new Double(range.getMax());
        name = generalizedValues.getName(i);
        if (min.isNaN() && max.isNaN()) {
          p.print("#");
        }
        else {
          p.print(getValues()[i]);
          if (!min.isNaN() && !max.isNaN()) {
            m1 = min.toString();
            m2 = max.toString();
            p.print("|range(" + m1 + "," + m2 + ")");
          }
          if (name != null && !name.equals("") && !name.equals("\"\"")) {
            p.print("|" + name);
          }
        }
      }
      p.print(" ");
    }
    p.print(");\n");
  }

  /**
   * Prints to the standard output the result of a propagation,
   * formatting the float numbers.
   */

  public void showResult() {

    int i;
    NumberFormat df = new DecimalFormat("0.00");
    int v;
    Double min, max;
    String m1 = "";
    String m2 = "";
    Range range;
    String name;

    for (i = 0; i < getVariables().size(); i++) {
      System.out.print("node " +
                       ( (FiniteStates) getVariables().elementAt(i)).getName());
      for (v = 0; v < getValues().length; v++) {
        range = generalizedValues.getRange(v);
        min = new Double(range.getMin());
        max = new Double(range.getMax());
        name = generalizedValues.getName(i);
        System.out.print(df.format(getValues()[v]));
        if (!min.isNaN() && !max.isNaN()) {
          m1 = min.toString();
          m2 = max.toString();
          System.out.print("|range(" + df.format(m1) + "," +
                           df.format(m2) + ")");
        }
        if (name != null) {
          if (name.equals("")) {
            System.out.println("|\"\"");
          }
          else {
            System.out.print("|" + name);
          }
        }
        System.out.print(" ");
      }
      System.out.println();
    }
  }

  /**
   * Prints this potential to the standard output.
   */

  public void print() {

    int i, total;
    Double min, max;
    String m1 = "";
    String m2 = "";
    Configuration conf;
    Range range;
    String name;
    
    System.out.println("Potential of class: "+getClass().getName());
    System.out.print(getVariables().size()+" Variables in potential:");
    
    for(i=0 ; i<getVariables().size() ; i++) {
      System.out.print(" "+((Node)(getVariables().elementAt(i))).getName());
    }

    System.out.println(" HashCode: "+hashCode());

    System.out.print("values= generalizedTable ( \n");
    total = (int) FiniteStates.getSize(getVariables());
    conf = new Configuration(getVariables());
    for (i = 0; i < total; i++) {
      System.out.print("                ");
      conf.print();
      range = generalizedValues.getRange(i);
      if(range == null) {
        System.out.print(" = " + getValues()[i]);
      } else {
        min = new Double(range.getMin());
        max = new Double(range.getMax());
        name = generalizedValues.getName(i);
        System.out.print(" = " + getValues()[i]);
        if (!min.isNaN() && !max.isNaN()) {
          m1 = min.toString();
          m2 = max.toString();
          System.out.print("|range(" + m1 + "," +
                           m2 + ")");
        }
        if (name != null) {
          if (name.equals("")) {
            System.out.println("|\"\"");
          }
          else {
            System.out.print("|" + name);
          }
        }
      }
      System.out.print(",\n");
      conf.nextConfiguration();
    }
    System.out.print("                );\n");
  }

  /**
   * Restricts the potential to a configuration of variables.
   * @param conf a <code>Configuration</code>.
   * @return A new <code>GeneralizedPotentialTable</code> result of the restriction
   * of this to <code>conf</code>.
   */

  public Potential restrictVariable(Configuration conf) {

    Configuration auxConf;
    Vector aux;
    FiniteStates temp;
    GeneralizedPotentialTable pot;
    int i;

    // Creates a configuration preserving the values in conf.
    auxConf = new Configuration(getVariables(), conf);

    // Computes the list of variables of the new Potential.
    aux = new Vector();
    for (i = 0; i < getVariables().size(); i++) {
      temp = (FiniteStates) getVariables().elementAt(i);
      if (conf.indexOf(temp) == -1) {
        aux.addElement(temp);
      }
    }

    pot = new GeneralizedPotentialTable(aux);

    for (i = 0; i < pot.getValues().length; i++) {
      pot.getValues()[i] = getValue(auxConf);

      pot.setRange(i, getRange(auxConf));
      pot.setName(i, getName(auxConf));

      auxConf.nextConfiguration(conf);
    }

    return pot;
  }

  /**
   * Sums over all the values of the variables in a list.
   * @param vars a <code>Vector</code> containing variables
   * (<code>FiniteStates</code>).
   * @return a new <code>GeneralizedPotentialTable</code> with
   * the variables in <code>vars</code> removed.
   */

  public GeneralizedPotentialTable addVariable(Vector vars) {

    Vector aux;
    FiniteStates temp;
    int i, pos;
    Configuration auxConf1, auxConf2;
    GeneralizedPotentialTable pot;

    aux = new Vector();

    // Creates the list of variables of the new potential.
    for (i = 0; i < getVariables().size(); i++) {
      temp = (FiniteStates) getVariables().elementAt(i);
      if (vars.indexOf(temp) == -1) {
        aux.addElement(temp);
      }
    }

    // Creates the new potential and sets the values to 0.0
    pot = new GeneralizedPotentialTable(aux);

    for (i = 0; i < pot.getValues().length; i++) {
      pot.getValues()[i] = 0.0;
      pot.setRange(i, Double.NaN, Double.NaN);
      pot.setName(i, null);
    }

    // Now for each configuration of the old potential, take
    // its value and see with which configuration of the new
    // one it corresponds. Then increment the value of the
    // new potential for that configuration.

    auxConf1 = new Configuration(getVariables());

    for (i = 0; i < getValues().length; i++) {
      auxConf2 = new Configuration(auxConf1, vars);
      pos = auxConf2.getIndexInTable();
      pot.getValues()[pos] += getValues()[i];

      pot.setRange(pos, getMinRange(i), getMaxRange(i));
      pot.setName(pos, getName(i));

      auxConf1.nextConfiguration();
    }

    return pot;
  }

  /**
   * Copies this potential.
   * @return a copy of this <code>GeneralizedPotentialTable</code>.
   */

  public Potential copy() {

    GeneralizedPotentialTable pot;
    Range range;
    String name;
    int i, n, j;

    pot = new GeneralizedPotentialTable(getVariables());

    n = (int) FiniteStates.getSize(getVariables());

    for (i = 0; i < n; i++) {
      pot.getValues()[i] = getValues()[i];
      range = generalizedValues.getRange(i);
      if(range!=null){
	  pot.setRange(i, range.getMin(), range.getMax());
	}
      name = generalizedValues.getName(i);
      pot.setName(i, name);
    }
    return pot;
  }

  /**
   * Converts a <code>Potential</code> to a <code>GeneralizedPotentialTable</code>.
   * @param pot the <code>Potential</code> to convert.
   * @returns a new <code>GeneralizedPotentialTable</code> resulting from converting
   * <code>Potential pot</code>
   */

  public static GeneralizedPotentialTable convertToGeneralizedPotentialTable(Potential pot) {

    GeneralizedPotentialTable newPot;

    if (pot.getClass().getName().equals("elvira.potential.GeneralizedPotentialTable")) {
      newPot = (GeneralizedPotentialTable) (pot.copy());
    }
    else {
      newPot = null;
    }

    return newPot;
  }

  /**
   * Gets the name of the class.
   * @return a <code>String</code> with the name of the class.
   */

  public String getClassName() {

    return new String("GeneralizedPotentialTable");
  }

} // end of class