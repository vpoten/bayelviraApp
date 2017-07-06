/*MainFrame.java*/

package elvira.sensitivityAnalysis;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JFileChooser;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import elvira.Elvira;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import elvira.Node;
import elvira.gui.ElviraPanel;
import elvira.gui.explication.VisualNode;
import java.awt.FontMetrics;
import elvira.Bnet;
import javax.swing.event.*;

/**
 * Esta clase es la ventana que contiene todo.
 * @author jruiz
 * @version 1.0
 */
public class MainFrame extends JFrame {

  private PanelAnalysis panelAnalisis;
  private PanelModifyRanges panelTablas;
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private SensitivityAnalysis cargaRed;
  private JMenu menu;
  private JMenuItem[] elementos1 = new JMenuItem[3];
  private JMenuItem[] elementos2 = new JMenuItem[4];
  private JMenuBar mb;
  private File file = new File("");
  private String name;
  private Date d = new Date();

  /**
   * Opcion de cargar un diagrama.
   */
  private ActionListener a0 = new ActionListener() {

    public void actionPerformed(ActionEvent e0) {

      JFileChooser c= new JFileChooser();
      MyFileFilter filter=new MyFileFilter();
      filter.addExtension("elv");
      filter.setDescription("Diagrama Elvira");
      c.setFileFilter(filter);

      //Abre el dialogo para la seleccion del archivo
      int rVal = c.showOpenDialog(MainFrame.this);

      //Se acepta el archivo
      if (rVal == JFileChooser.APPROVE_OPTION) {
        try {
          //Se carga el archivo en la clase base
          cargaRed.setNetPath(c.getSelectedFile().getAbsolutePath());
          cargaRed.setNet();

          setTitle(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Titulo") + " " +
                   cargaRed.getNetPath());

        //Borramos los panales anteriores
        jTabbedPane1.removeAll();

        //Añadimos los nuevos paneles con el diagrama
        panelTablas = new PanelModifyRanges(cargaRed,
                                            jTabbedPane1,
                                            new Rectangle(50, 160, getWidth() - 30, getHeight() - 150));
        panelAnalisis = null;
        jTabbedPane1.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.ModParam"),
                            new ImageIcon("elvira/gui/images/modifyParams.gif"),
                            panelTablas);
        jTabbedPane1.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Analisis"),
                            new ImageIcon("elvira/gui/images/analysis.gif"),
                            panelAnalisis);

        jTabbedPane1.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if(jTabbedPane1.getSelectedIndex()==1 && panelAnalisis==null) {
               try {
		     panelAnalisis = new PanelAnalysis(cargaRed,new Rectangle(30, 80, getWidth() - 30, getHeight()
                                                   - 160));
              } catch (Exception e1) {}
              jTabbedPane1.setComponentAt(1,panelAnalisis);
            }
          }
        });

        } catch(Exception e) {
          //Aviso de que ha habido un error al cargar el diagrama
          JOptionPane.showMessageDialog(getParent(), Elvira.localize(cargaRed.getDialogBundle(),
              "SensitivityAnalysis.Error"));
        }
      }

    }
  };

  /**
   * Opcion de guardar.
   */
  private ActionListener a1 = new ActionListener() {

    public void actionPerformed(ActionEvent e1) {

      cargaRed.save();
    }
  };

  /**
   * Opcion de guardar como.
   */
  private ActionListener a2 = new ActionListener() {

    public void actionPerformed(ActionEvent e2) {

      JFileChooser c = new JFileChooser();
      c.setDialogType(JFileChooser.SAVE_DIALOG);
      MyFileFilter filter = new MyFileFilter();
      filter.addExtension("elv");
      filter.setDescription("Diagrama Elvira");
      c.setFileFilter(filter);
      String fileName;
      File file = null;

      //Dialogo para salvar archivos
      int rVal = c.showSaveDialog(MainFrame.this);

      //Se ha seleccionado aceptar
      if (rVal == JFileChooser.APPROVE_OPTION) {
        try {

          if (cargaRed.getDiag() != null) {
            fileName = c.getSelectedFile().getAbsolutePath();

            //Si el archivo no tiene la extension adecuada , se la añadimos
            if (fileName.indexOf('.', 0) == -1) {
              file = new File(fileName + ".elv");
            }
            else {
              file = new File(fileName);
            }

            //Guardamos el diagrama
            cargaRed.setNetPath(file.getAbsolutePath());
            cargaRed.save();

            setTitle(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Titulo") + " " +
                     cargaRed.getNetPath());
          }

        } catch(Exception e) {}
      }

    }
  };

  /**
   * Opcion de salir.
   * Esta opcion esta activa solo si se ejecuta aparte de Elvira .
   */
  private ActionListener a3 = new ActionListener() {

    public void actionPerformed(ActionEvent e3) {

     System.exit(0);
    }
  };

  /**
   * Constructor por defecto.
   * @param cr Clase principal.
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  public MainFrame(SensitivityAnalysis cr) throws elvira.parser.ParseException,IOException {

    getContentPane().setLayout(null);
    cargaRed = cr;
    menu = new JMenu(Elvira.localize(cargaRed.getMenuBundle(),"SensitivityAnalysis.Opciones"));
    elementos1[0] = new JMenuItem(Elvira.localize(cargaRed.getMenuBundle(),
                                                  "SensitivityAnalysis.CargarDiagrama"),
                                  new ImageIcon("elvira/gui/images/open.gif"));
    elementos1[1] = new JMenuItem(Elvira.localize(cargaRed.getMenuBundle(),
                                                  "SensitivityAnalysis.GuardarDiagrama"),
                                  new ImageIcon("elvira/gui/images/save.gif"));
    elementos1[2] = new JMenuItem(Elvira.localize(cargaRed.getMenuBundle(),
                                                  "SensitivityAnalysis.GuardarDiagramaComo"));
    elementos2[0] = new JMenuItem(Elvira.localize(cargaRed.getMenuBundle(),
                                                  "SensitivityAnalysis.CargarDiagrama"),
                                  new ImageIcon("elvira/gui/images/open.gif"));
    elementos2[1] = new JMenuItem(Elvira.localize(cargaRed.getMenuBundle(),
                                                  "SensitivityAnalysis.GuardarDiagrama"),
                                  new ImageIcon("elvira/gui/images/save.gif"));
    elementos2[2] = new JMenuItem(Elvira.localize(cargaRed.getMenuBundle(),
                                                  "SensitivityAnalysis.GuardarDiagramaComo"));
    elementos2[3] = new JMenuItem(Elvira.localize(cargaRed.getMenuBundle(),
                                                  "SensitivityAnalysis.Salir"));
    mb = new JMenuBar();

    setBounds(0, 0, 750, 630);
    this.addComponentListener(new MainFrame_this_componentAdapter(this));
    jTabbedPane1.setBounds(new Rectangle(10,10,getWidth() - 25,getHeight() - 75));
    getContentPane().add(jTabbedPane1,null);
    setTitle(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Titulo"));

    //Si se ejecuta el programa independientemente de Elvira o no
    if (cargaRed.main) {
      elementos2[0].addActionListener(a0);
      elementos2[1].addActionListener(a1);
      elementos2[2].addActionListener(a2);
      elementos2[3].addActionListener(a3);
      menu.add(elementos2[0]);
      menu.add(elementos2[1]);
      menu.add(elementos2[2]);
      menu.add(elementos2[3]);
      mb.add(menu);
      setJMenuBar(mb);
    } else if (cargaRed.getElvFrame() == null) {
      elementos1[0].addActionListener(a0);
      elementos1[1].addActionListener(a1);
      elementos1[2].addActionListener(a2);
      menu.add(elementos1[0]);
      menu.add(elementos1[1]);
      menu.add(elementos1[2]);
      mb.add(menu);
      setJMenuBar(mb);
    }

    setResizable(true);

    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();

    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }

    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }

    this.setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);

    setVisible(true);

    addWindowListener(new WindowAdapter() {
      //Cuando cerramos el programa verificamos si el diagrama ha cambiado , y si es asi , 
      //cargamos los cambios en el diagrama actual de Elvira
      public void windowClosing(WindowEvent e) {
        if (panelTablas != null && panelTablas.modify) {
          try {
            //cargaRed.getElvFrame().reopenNetwork((Bnet)cargaRed.getDiag(),cargaRed.getNetPath(),false); 
		  cargaRed.getElvFrame().getCurrentEditorPanel().load((Bnet)cargaRed.getDiag());
              cargaRed.getElvFrame().getCurrentEditorPanel().setModifiedNetwork(true);  
          } catch (Exception e1) {}
        }
      }
    });

    if (cargaRed != null && cargaRed.getDiag() != null) {
      try {
        setTitle(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Titulo") + " " +
                 cargaRed.getNetPath());

        //Borramos los panales anteriores
        jTabbedPane1.removeAll();

        //Añadimos los nuevos paneles con el diagrama
        panelTablas = new PanelModifyRanges(cargaRed,
                                            jTabbedPane1,
                                            new Rectangle(50, 160, getWidth() - 30, getHeight() - 150));
        panelAnalisis = null;
        jTabbedPane1.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.ModParam"),
                            new ImageIcon("elvira/gui/images/modifyParams.gif"),
                            panelTablas);
        jTabbedPane1.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Analisis"),
                            new ImageIcon("elvira/gui/images/analysis.gif"),
                            panelAnalisis);

        jTabbedPane1.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if(jTabbedPane1.getSelectedIndex()==1 && panelAnalisis==null) {
               try {
		     panelAnalisis = new PanelAnalysis(cargaRed,new Rectangle(30, 80, getWidth() - 30, getHeight()
                                                   - 160));
              } catch (Exception e1) {}
              jTabbedPane1.setComponentAt(1,panelAnalisis);
            }
          }
        });
	} catch(Exception e) {}
    }

  }

  void this_componentResized(ComponentEvent e) {

    try {
      jTabbedPane1.setBounds(new Rectangle(10,10,getWidth() - 25,getHeight() - 75));
    } catch (Exception e1) {}
  }

} //End of class

class MainFrame_this_componentAdapter extends java.awt.event.ComponentAdapter {
  MainFrame adaptee;

  MainFrame_this_componentAdapter(MainFrame adaptee) {
    this.adaptee = adaptee;
  }
  public void componentResized(ComponentEvent e) {
    adaptee.this_componentResized(e);
  }

}


