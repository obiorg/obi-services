package org.obi.services.Docking;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme;
import net.infonode.docking.*;
import net.infonode.docking.drag.DockingWindowDragSource;
import net.infonode.docking.drag.DockingWindowDragger;
import net.infonode.docking.drag.DockingWindowDraggerProvider;
import net.infonode.docking.mouse.DockingWindowActionMouseButtonListener;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.*;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.Form.DatabaseFrame;
import org.obi.services.Form.DatabaseInformationsFrame;
import org.obi.services.Form.output.CapturePane;
import org.obi.services.Form.output.StreamCapturer;
import org.obi.services.OBIServiceTrayIcon;
import org.obi.services.app.DataCollectorWriterThread;
import org.obi.services.app.ManagerControllerThread;
import org.obi.services.entities.tags.Tags;
import org.obi.services.model.DatabaseModel;
import org.obi.services.util.Ico;
import org.obi.services.util.Util;
import org.obi.services.listener.thread.SystemThreadListener;

/**
 * Future TrayIcon
 *
 * @author r.hendrick
 * @version 1.0
 */
public class MainWindowDocking implements SystemThreadListener {

    private static final int ICON_SIZE = 8;

    /**
     * Custom view icon.
     */
    private static final Icon VIEW_ICON = new Icon() {
        @Override
        public int getIconHeight() {
            return ICON_SIZE;
        }

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color oldColor = g.getColor();

            g.setColor(new Color(70, 70, 70));
            g.fillRect(x, y, ICON_SIZE, ICON_SIZE);

            g.setColor(new Color(100, 230, 100));
            g.fillRect(x + 1, y + 1, ICON_SIZE - 2, ICON_SIZE - 2);

            g.setColor(oldColor);
        }
    };

    /**
     * Custom view button icon.
     */
    private static final Icon BUTTON_ICON = new Icon() {
        @Override
        public int getIconHeight() {
            return ICON_SIZE;
        }

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color oldColor = g.getColor();

            g.setColor(Color.BLACK);
            g.fillOval(x, y, ICON_SIZE, ICON_SIZE);

            g.setColor(oldColor);
        }
    };

    /**
     * The one and only root window
     */
    private RootWindow rootWindow;

    /**
     * An array of the static views
     *
     * [0] : Output event
     *
     * [1] : Configurations
     *
     * [2] : Connections
     *
     * [3] : Database driver
     *
     * [4] : Manager controller
     *
     * [5] : Thread Frame
     */
    private final View[] views = new View[6];

    /**
     * Contains all the static views
     */
    private ViewMap viewMap = new ViewMap();

    /**
     * The view menu items
     */
    private JMenuItem[] viewItems = new JMenuItem[views.length];

    /**
     * Contains the dynamic views that has been added to the root window
     */
    private HashMap dynamicViews = new HashMap();

    /**
     * The currently applied docking windows theme
     */
    private DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();

    /**
     * The application setting frame
     */
    private SettingsApplicationFrame settingsApplicationFrame = null;

    /**
     * The application connection Frame
     */
    private ConnectionFrame connectionsFrame = null;

    /**
     * The application database informations frame
     */
    private DatabaseInformationsFrame databaseInformationsFrame = null;

    /**
     * The application Manager controller Frame for connectivity in production
     */
    private ManagerControllerFrame managerControllerFrame = new ManagerControllerFrame();

    /**
     * The application Thread Frame for thread analyse
     */
    private ThreadFrame threadFrame = new ThreadFrame();

    /**
     * Tray Icon System to be use in this class
     */
    TrayIcon trayIcon;

    /**
     * Manager Controller Thread Manage all thread generated
     */
    ManagerControllerThread managerCtrlThread;

    /**
     * Manager of Data Collector Writer : to process reading in data
     */
    DataCollectorWriterThread dataCollectorWriterThread;

    /**
     * Main Windows Pane
     */
    private DockingWindow mainTabWindow = new TabWindow();

    /**
     * A dynamically created view containing an id.
     */
    private static class DynamicView extends View {

        private int id;

        /**
         * Constructor.
         *
         * @param title the view title
         * @param icon the view icon
         * @param component the view component
         * @param id the view id
         */
        DynamicView(String title, Icon icon, Component component, int id) {
            super(title, icon, component);
            this.id = id;
        }

        /**
         * Returns the view id.
         *
         * @return the view id
         */
        public int getId() {
            return id;
        }
    }

    /**
     * In this properties object the modified property values for close buttons
     * etc. are stored. This object is cleared when the theme is changed.
     */
    private RootWindowProperties properties = new RootWindowProperties();

    /**
     * Where the layouts are stored.
     */
    private byte[][] layouts = new byte[3][];

    /**
     * The application frame
     */
    private JFrame frame = new JFrame("OBI Service");

    public MainWindowDocking(TrayIcon trayIcon, ManagerControllerThread _managerCtrlThread, Boolean startMode) {

        createRootWindow();
        setDefaultLayout();
        initComponents();

        this.trayIcon = trayIcon;
        this.managerCtrlThread = _managerCtrlThread;
        this.managerCtrlThread.addClientListener(this);
        this.managerCtrlThread.addClientListener(managerControllerFrame);
        this.managerCtrlThread.addMachinesEvent(managerControllerFrame);
        Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + " : Application >> Started ...");

        this.dataCollectorWriterThread = new DataCollectorWriterThread();
        
        if (startMode) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + " : Application >> Auto start service processeing ...");
            startTagCollectorMenuItemActionPerformed(null);
        }
    }

    /**
     * Creates a view component containing the specified text.
     *
     * @param text the text
     * @return the view component
     */
    private static JComponent createViewComponent(String text) {
        StringBuffer sb = new StringBuffer();

        for (int j = 0; j < 100; j++) {
            sb.append(text + ". This is line " + j + "\n");
        }

        return new JScrollPane(new JTextArea(sb.toString()));
    }

    /**
     * Returns a dynamic view with specified id, reusing an existing view if
     * possible.
     *
     * @param id the dynamic view id
     * @return the dynamic view
     */
    private View getDynamicView(int id) {
        View view = (View) dynamicViews.get(id);

        if (view == null) {
            view = new DynamicView("Dynamic View " + id, Ico.i16("/img/oz/exit.png", this), createViewComponent("Dynamic View " + id), id);
        }

        return view;
    }

    /**
     * Returns the next available dynamic view id.
     *
     * @return the next available dynamic view id
     */
    private int getDynamicViewId() {
        int id = 0;

        while (dynamicViews.containsKey(id)) {
            id++;
        }

        return id;
    }

    /**
     * Creates the root window and the views.
     */
    private void createRootWindow() {
        // [0] : Evènement de sortie
        CapturePane capturePane = new CapturePane();
        PrintStream ps = System.out;
        System.setOut(new PrintStream(new StreamCapturer("obi", capturePane, ps)));
        Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + " : createRootWindow >> Started ...");
        views[0] = new View("Sorties", Ico.i16("/img/javadocking/icons/terminal.png", this), capturePane);
        viewMap.addView(0, views[0]);

        // [1] : Configuration
        settingsApplicationFrame = new SettingsApplicationFrame();
        views[1] = new View("Configurations", Ico.i16("/img/oz/config.png", this), settingsApplicationFrame);
        viewMap.addView(1, views[1]);
        mainTabWindow.add(views[1]);
        views[1].setEnabled(true);
        views[1].close();

        // [2] : Connection 
        connectionsFrame = new ConnectionFrame();
        views[2] = new View("S7 Connexions", Ico.i16("/img/std/Refresh.png", this), connectionsFrame);
        viewMap.addView(2, views[2]);
        mainTabWindow.add(views[2]);
        views[2].setEnabled(false);
        views[2].close();

        // [3] : Database driver
        // [4] : Manager controller
        //managerControllerFrame = new ManagerControllerFrame(); /!\ already init in object
        views[4] = new View("Manager Controller", Ico.i16("/img/std/Tree.png", this), managerControllerFrame);
        viewMap.addView(4, views[4]);
        mainTabWindow.add(views[4]);
        views[4].setEnabled(false);
        views[4].close();

        // [5] : Thread Frame
        views[5] = new View("Thread", Ico.i16("/img/std/thread.png", this), threadFrame);
        viewMap.addView(5, views[5]);
        mainTabWindow.add(views[5]);
        views[5].setEnabled(false);
        views[5].close();

        // The mixed view map makes it easy to mix static and dynamic views inside the same root window
        MixedViewHandler handler = new MixedViewHandler(viewMap, new ViewSerializer() {
            public void writeView(View view, ObjectOutputStream out) throws IOException {
                out.writeInt(((DynamicView) view).getId());
            }

            public View readView(ObjectInputStream in) throws IOException {
                return getDynamicView(in.readInt());
            }
        });

        rootWindow = DockingUtil.createRootWindow(viewMap, handler, true);

        // Set gradient theme. The theme properties object is the super object of our properties object, which
        // means our property value settings will override the theme values
        properties.addSuperObject(currentTheme.getRootWindowProperties());

        // Our properties object is the super object of the root window properties object, so all property values of the
        // theme and in our property object will be used by the root window
        rootWindow.getRootWindowProperties().addSuperObject(properties);

        // Enable the bottom window bar
        rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);

        // Add a listener which shows dialogs when a window is closing or closed.
        rootWindow.addListener(new DockingWindowAdapter() {
            public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
                updateViews(addedWindow, true);
            }

            public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
                updateViews(removedWindow, false);
            }

            public void windowClosing(DockingWindow window) throws OperationAbortedException {
                if (JOptionPane.showConfirmDialog(frame, "Confirmer fermeture fenêtre '" + window + "'?") != JOptionPane.YES_OPTION) {
                    throw new OperationAbortedException("La fermeture de la fenêtre a été annulée!");
                }
            }

        });

        // Add a mouse button listener that closes a window when it's clicked with the middle mouse button.
        rootWindow.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
    }

    /**
     * Update view menu items and dynamic view map.
     *
     * @param window the window in which to search for views
     * @param added if true the window was added
     */
    private void updateViews(DockingWindow window, boolean added) {
        if (window instanceof View) {
            if (window instanceof DynamicView) {
                if (added) {
                    dynamicViews.put(((DynamicView) window).getId(), window);
                } else {
                    dynamicViews.remove(((DynamicView) window).getId());
                }
            } else {
                for (int i = 0; i < views.length; i++) {
                    if (views[i] == window && viewItems[i] != null) {
                        viewItems[i].setEnabled(!added);

                        switch (i) {
                            case 0:
                                outputMenuItem.setEnabled(!added);
                                break;
                            case 1:
                                configMenuItem.setEnabled(!added);
                                break;
                            case 2:
                                connectionsMenuItem.setEnabled(!added);
                                break;
                            default:
                                break;
                        }

                    }
                }
            }
        } else {
            for (int i = 0; i < window.getChildWindowCount(); i++) {
                updateViews(window.getChildWindow(i), added);
            }
        }
    }

    /**
     * Sets the default window layout.
     */
    private void setDefaultLayout() {
        TabWindow tabWindow = new TabWindow(views[0]);
        mainTabWindow = new TabWindow(views[1]);
        rootWindow.setWindow(
                new SplitWindow(false,
                        0.7f,
                        mainTabWindow,
                        tabWindow));

        WindowBar windowBar = rootWindow.getWindowBar(Direction.DOWN);

        while (windowBar.getChildWindowCount() > 0) {
            windowBar.getChildWindow(0).close();
        }
    }

    /**
     * Creates the frame tool bar.
     *
     * @return the frame tool bar
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        JLabel label = new JLabel("Drag New View");
        toolBar.add(label);
        new DockingWindowDragSource(label, new DockingWindowDraggerProvider() {
            public DockingWindowDragger getDragger(MouseEvent mouseEvent) {
                return getDynamicView(getDynamicViewId()).startDrag(rootWindow);
            }
        });
        return toolBar;
    }

    /**
     * Sets the docking windows theme.
     *
     * @param theme the docking windows theme
     */
    private void setTheme(DockingWindowsTheme theme) {
        properties.replaceSuperObject(currentTheme.getRootWindowProperties(),
                theme.getRootWindowProperties());
        currentTheme = theme;
    }

    /**
     * Exit Menu
     *
     * Allow to dispose window and leave it working background
     *
     * @param evt event
     */
    private void hideMenuItemActionPerformed(ActionEvent evt) {
        frame.dispose();
    }

    /**
     * Dispose button Allow to hide window to tray
     *
     * @param evt action event on button click
     */
    private void tbBtnHideActionPerformed(ActionEvent evt) {
        hideMenuItemActionPerformed(evt);
    }

    /**
     * Exit Application Allow to exit totaly the application
     *
     * @param evt event
     */
    private void exitAppMenuItemActionPerformed(ActionEvent evt) {
        System.exit(0);
    }

    /**
     * Exit Application Allow to exit totaly the application
     *
     * @param evt event
     */
    private void tbBtnExitActionPerformed(ActionEvent evt) {
        System.exit(0);
    }

    /**
     * Allow to display window for database infos
     *
     * First check if connection is already defined. Otherwise try to make it.
     * If result are present a new window will be create to show information.
     *
     * @param evt action event
     */
    private void databaseInfoMenuActionPerformed(ActionEvent evt) {
        Connection conn = null;

        try {
            conn = DatabaseFrame.toConnection(DatabaseModel.databaseModel());

            if (conn != null) {
                DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());

                DatabaseInformationsFrame dbiForm = new DatabaseInformationsFrame(frame, true);
                dbiForm.doUpdateWithDatabaseMetaData(dm);
                //dbiForm.setLocationRelativeTo(this.mainDesktopPane);
                dbiForm.setLocationRelativeTo(frame);
                dbiForm.setVisible(true);
            } else {
                System.out.println("OBI Service >> Unable to connect !");
            }
        } catch (SQLException ex) {
            Logger.getLogger(OBIServiceTrayIcon.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Convenien method to display window manager Controller
     *
     * @param evt display manager controller
     */
    private void managerControllerWindowDisplayActionPerformed(ActionEvent evt) {
        if (views[4].getRootWindow() != null) {
            views[4].restoreFocus();
            managerControllerMenuItem.setEnabled(false);
        } else {
            DockingUtil.addWindow(views[4], rootWindow);
        }
    }

    /**
     * Start Tag Collector
     *
     * Allow to start the main thread for data collection
     *
     * @param evt should be menuItem action
     */
    private void startTagCollectorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (!managerCtrlThread.isRunning()) {
            managerControllerFrame.clearMachineTable();
            managerCtrlThread.doRelease();
            if (!managerCtrlThread.isAlive()) {
//                managerCtrlThread.kill();
//                managerCtrlThread = new ManagerControllerThread(trayIcon);
                managerCtrlThread.start();
            } else {
//                managerCtrlThread.doRelease();
            }
            // Done in SystemThreadListener
//            trayIcon.displayMessage("OBI",
//                    bundle.getString("TagsCollector_Msg_Start"),
//                    TrayIcon.MessageType.INFO);
        } else {
            trayIcon.displayMessage("OBI",
                    "Processus is already running. Please stop before start !",
                    TrayIcon.MessageType.WARNING);
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + " : startTagCollectorMenuItem >> Processus is already running. Please stop before start !");
        }

        if (!dataCollectorWriterThread.isRunning()) {
            dataCollectorWriterThread.doRelease();
            if (!dataCollectorWriterThread.isAlive()) {
                dataCollectorWriterThread.start();
            } else {
            }
        } else {
            trayIcon.displayMessage("OBI",
                    "DataCollectorWriterThread is already running. Please stop before start !",
                    TrayIcon.MessageType.WARNING);
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + " : startTagCollectorMenuItem >> DataCollectorWriterThread is already running. Please stop before start !");
        }

        startTagCollectorMenuItem.setEnabled(false);
        stopTagCollectorMenuItem.setEnabled(true);

        tbBtnToolsTagsControllerStart.setEnabled(false);
        tbBtnToolsTagsControllerStop.setEnabled(true);
    }

    /**
     * Stop Tag Collector
     *
     * Allow to stop the main thread for data collection
     *
     * @param evt should be menuItem action
     */
    private void stopTagCollectorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (managerCtrlThread.isRunning()) {
            managerCtrlThread.doStop();
            // Done in tagCollectorThreadListener
//            trayIcon.displayMessage("OBI",
//                    "Tags collector est arrêté !",
//                    TrayIcon.MessageType.INFO);

        } else {
            trayIcon.displayMessage("OBI",
                    "Processus is already stopped. Please start before any stop !",
                    TrayIcon.MessageType.WARNING);
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + " : stopTagCollectorMenuItem >> Processus is already stopped. Please start before any stop !");
        }

        if (dataCollectorWriterThread.isRunning()) {
            dataCollectorWriterThread.doStop();
        } else {
            trayIcon.displayMessage("OBI",
                    "DataCollectorWriterThread is already stopped. Please start before any stop !",
                    TrayIcon.MessageType.WARNING);
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + " : stopTagCollectorMenuItem >> DataCollectorWriterThread is already stopped. Please start before any stop !");
        }

        startTagCollectorMenuItem.setEnabled(true);
        stopTagCollectorMenuItem.setEnabled(false);

        tbBtnToolsTagsControllerStart.setEnabled(true);
        tbBtnToolsTagsControllerStop.setEnabled(false);
    }

    /**
     * Convenien method to display window Thread Frame
     *
     * @param evt display thread frame
     */
    private void threadWindowDisplayActionPerformed(ActionEvent evt) {
        if (views[5].getRootWindow() != null) {
            views[5].restoreFocus();
            managerControllerMenuItem.setEnabled(false);
        } else {
            DockingUtil.addWindow(views[5], rootWindow);
        }
    }

    /**
     * Theme action menu : metal
     *
     *
     * @param evt menuItem source for theme
     */
    private void metalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
    }

    /**
     * Theme action menu : nimbus
     *
     *
     * @param evt menuItem source for theme
     */
    private void nimbusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
    }

    /**
     * Theme action menu : CDEMotif
     *
     *
     * @param evt menuItem source for theme
     */
    private void CDEMotifMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
    }

    /**
     * Theme action menu : windowMenu
     *
     *
     * @param evt menuItem source for theme
     */
    private void windowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
    }

    /**
     * Theme action menu : windowClassic
     *
     *
     * @param evt menuItem source for theme
     */
    private void windowClassicMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
    }

    /**
     * Theme action menu : flatLightLaf
     *
     *
     * @param evt menuItem source for theme
     */
    private void flatLightLafMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (Exception e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);

    }

    /**
     * Theme action menu : flatDarkLaf
     *
     *
     * @param evt menuItem source for theme
     */
    private void flatDarkLafMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (Exception e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
    }

    /**
     * Theme action menu : flatGitHubDark
     *
     *
     * @param evt menuItem source for theme
     */
    private void flatGitHubDarkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel(new FlatGitHubDarkIJTheme());
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (Exception e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
    }

    /**
     * Theme action menu : flatSolorizedDark
     *
     *
     * @param evt menuItem source for theme
     */
    private void flatSolorizedDarkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel(new FlatSolarizedDarkIJTheme());
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (Exception e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
    }

    /**
     * Theme action menu : flatSolorizedLight
     *
     *
     * @param evt menuItem source for theme
     */
    private void flatSolorizedLightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel(new FlatSolarizedLightIJTheme());
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (Exception e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
    }

    /**
     * Theme action menu : flatGitHubLight
     *
     *
     * @param evt menuItem source for theme
     */
    private void flatGitHubLightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            UIManager.setLookAndFeel(new FlatGitHubIJTheme());
            SwingUtilities.updateComponentTreeUI(frame);
            //frame.pack();
        } catch (Exception e) {
            Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + e.getLocalizedMessage());
        }
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);

    }

    // Variables declaration - do not modify   
    private ResourceBundle bundle = ResourceBundle.getBundle("bundles/Fr_fr"); // NOI18N

    private JMenuItem CDEMotifMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem configMenuItem;
    private JMenuItem connectionsMenuItem;
    private JMenuItem outputMenuItem;
    private JMenuItem contentMenuItem;
    private JMenuItem databaseInfoMenu;
    private JDesktopPane desktopPane;
    private JMenu displayMenu;
    private JMenu editMenu;
    private JMenuItem exitAppMenuItem;
    private JMenuItem hideMenuItem;
    private JMenu fileMenu;
    private JMenuItem flatDarkLafMenuItem;
    private JMenuItem flatGitHubDarkMenuItem;
    private JMenuItem flatGitHubLightMenuItem;
    private JMenuItem flatLightLafMenuItem;
    private JMenuItem flatSolorizedDarkMenuItem;
    private JMenuItem flatSolorizedLightMenuItem;
    private JMenu helpMenu;
    private JPopupMenu.Separator jSeparator1;
    private JToolBar mainToolBar;
    private JToolBar toolsToolBar;
    private JMenuBar menuBar;
    private JMenuItem metalMenuItem;
    private JMenuItem nimbusMenuItem;
    private JMenuItem startTagCollectorMenuItem;
    private JMenuItem stopTagCollectorMenuItem;
    private JMenuItem managerControllerMenuItem;
    private JMenuItem threadMenuItem;
    private JButton tbBtnHide;
    private JButton tbBtnExit;
    private JButton tbBtnToolsS7Connexion;
    private JButton tbBtnToolsManagerController;
    private JButton tbBtnToolsTagsControllerStart;
    private JButton tbBtnToolsTagsControllerStop;
    private JButton tbBtnToolsThreadFrame;
    private JMenu themeMenu;
    private JPopupMenu.Separator themeSeparator;
    private JMenu toolsMenu;
    private JMenuItem windowClassicMenuItem;
    private JMenuItem windowMenuItem;
    // End of variables declaration    

    /**
     * Initialize main view interface
     */
    private void initComponents() {
        /**
         * MANAGING MENU
         */
        //> FILE MENU - HideMenuItem
        hideMenuItem = new JMenuItem();
        hideMenuItem.setIcon(Ico.i16("/img/oz/hide_minimize_minimum_window_size_ui.png", this));
        hideMenuItem.setMnemonic('x');
        hideMenuItem.setToolTipText("Permet de réduire l'application");
        hideMenuItem.setText("Réduire Tray");
        hideMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                hideMenuItemActionPerformed(evt);
            }
        });

        //> FILE MENU - ExitMenuItem
        exitAppMenuItem = new JMenuItem();
        exitAppMenuItem.setIcon(Ico.i16("/img/oz/exit.png", this));
        exitAppMenuItem.setMnemonic('x');
        exitAppMenuItem.setToolTipText("Permet de quitter l'application");
        exitAppMenuItem.setText("Quitter");
        exitAppMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exitAppMenuItemActionPerformed(evt);
            }
        });

        /// FILE MENU - SETUP
        fileMenu = new JMenu();
        fileMenu.setMnemonic('f');
        fileMenu.setText(bundle.getString("MenuFile")); // NOI18N
        fileMenu.setToolTipText("Gestion de l'application");
        fileMenu.add(hideMenuItem);
        fileMenu.add(exitAppMenuItem);

        //> DISPLAY MENU - database Information Menu
        databaseInfoMenu = new JMenuItem();
        databaseInfoMenu.setIcon(Ico.i16("/img/std/DlgInfos.png", this));
        databaseInfoMenu.setMnemonic('c');
        databaseInfoMenu.setText(bundle.getString("showBDDInfos")); // NOI18N
        databaseInfoMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseInfoMenuActionPerformed(evt);
            }
        });

        //> DISPLAY MENU - output window
        outputMenuItem = new JMenuItem();
        outputMenuItem.setIcon(Ico.i16("/img/javadocking/icons/terminal.png", this));
        outputMenuItem.setMnemonic('t');
        outputMenuItem.setText(bundle.getString("MenuItemOutput")); // NOI18N
        outputMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (views[0].getRootWindow() != null) {
                    views[0].restoreFocus();
                } else {
                    DockingUtil.addWindow(views[0], rootWindow);
                }
            }
        });
        outputMenuItem.setEnabled(false);

        /// DISPLAY MENU - SETUP
        displayMenu = new JMenu();
        displayMenu.setMnemonic('h');
        displayMenu.setText(bundle.getString("Window")); // NOI18N
        displayMenu.setToolTipText("Gérer les fenêtres d'affichage");
        displayMenu.add(databaseInfoMenu);
        displayMenu.add(new JPopupMenu.Separator());
        displayMenu.add(createLayoutMenu());
        displayMenu.add(createFocusViewMenu());
        displayMenu.add(createPropertiesMenu());
        displayMenu.add(new JPopupMenu.Separator());
        displayMenu.add(createWindowBarsMenu());
        displayMenu.add(createViewMenu());
        displayMenu.add(new JPopupMenu.Separator());
        displayMenu.add(outputMenuItem);

        //> TOOLS MENU - connectionsMenuItem
        connectionsMenuItem = new JMenuItem();
        connectionsMenuItem.setIcon(Ico.i16("/img/std/Refresh.png", this));
        connectionsMenuItem.setMnemonic('c');
        connectionsMenuItem.setText(bundle.getString("toolsS7Connections")); // NOI18N
        connectionsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (views[2].getRootWindow() != null) {
                    views[2].restoreFocus();
                } else {
                    DockingUtil.addWindow(views[2], rootWindow);
                }
            }
        });

        //> TOOLS MENU - startTagCollectorMenuItem
        startTagCollectorMenuItem = new JMenuItem();
        startTagCollectorMenuItem.setIcon(Ico.i16("/img/std/onOff/start.png", this));
        startTagCollectorMenuItem.setMnemonic('s');
        startTagCollectorMenuItem.setText(bundle.getString("toolsStartTagsCollector")); // NOI18N
        startTagCollectorMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startTagCollectorMenuItemActionPerformed(evt);
            }
        });

        //> TOOLS MENU - stopTagCollectorMenuItem
        stopTagCollectorMenuItem = new JMenuItem();
        stopTagCollectorMenuItem.setIcon(Ico.i16("/img/std/onOff/stop.png", this));
        stopTagCollectorMenuItem.setMnemonic('c');
        stopTagCollectorMenuItem.setText(bundle.getString("toolsStopTagsCollector")); // NOI18N
        stopTagCollectorMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopTagCollectorMenuItemActionPerformed(evt);
            }
        });
        stopTagCollectorMenuItem.setEnabled(false);

        //> TOOLS MENU - managerControllerMenuItem
        managerControllerMenuItem = new JMenuItem();
        managerControllerMenuItem.setIcon(Ico.i16("/img/std/Tree.png", this));
        managerControllerMenuItem.setMnemonic('t');
        managerControllerMenuItem.setText(bundle.getString("MenuItemControllerManager")); // NOI18N
        managerControllerMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (views[4].getRootWindow() != null) {
                    views[4].restoreFocus();
                    managerControllerMenuItem.setEnabled(false);
                } else {
                    DockingUtil.addWindow(views[4], rootWindow);
                }
            }
        });
        managerControllerMenuItem.setEnabled(true);

        //> TOOLS MENU - threadMenuItem
        threadMenuItem = new JMenuItem();
        threadMenuItem.setIcon(Ico.i16("/img/std/thread.png", this));
        threadMenuItem.setMnemonic('t');
        threadMenuItem.setText(bundle.getString("MenuItemThread")); // NOI18N
        threadMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (views[5].getRootWindow() != null) {
                    views[5].restoreFocus();
                    threadMenuItem.setEnabled(false);
                } else {
                    DockingUtil.addWindow(views[5], rootWindow);
                }
            }
        });
        managerControllerMenuItem.setEnabled(true);

        /// TOOLS MENU - SETUP
        toolsMenu = new JMenu();
        toolsMenu.setMnemonic('o');
        toolsMenu.setText(bundle.getString("Tools")); // NOI18N
        toolsMenu.setToolTipText("Gérer les fenêtres d'affichage");
        toolsMenu.add(connectionsMenuItem);
        jSeparator1 = new JPopupMenu.Separator();
        toolsMenu.add(jSeparator1);
        toolsMenu.add(startTagCollectorMenuItem);
        toolsMenu.add(stopTagCollectorMenuItem);
        toolsMenu.add(new JPopupMenu.Separator());
        toolsMenu.add(managerControllerMenuItem);
        toolsMenu.add(new JPopupMenu.Separator());
        toolsMenu.add(threadMenuItem);

        //> EDIT MENU - configMenuItem
        final View view = views[1];
        configMenuItem = new JMenuItem();
        configMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        configMenuItem.setMnemonic('t');
        configMenuItem.setText(view.getTitle());
        configMenuItem.setEnabled(views[1].getRootWindow() == null);
        configMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (views[1].getRootWindow() != null) {
                    views[1].restoreFocus();
                } else {
                    DockingUtil.addWindow(views[1], rootWindow);
                }
            }
        });
        configMenuItem.setEnabled(false);

        /// EDIT MENU - SETUP
        editMenu = new JMenu();
        editMenu.setMnemonic('e');
        editMenu.setText(bundle.getString("MenuConfig")); // NOI18N
        editMenu.setToolTipText("Gestion des configurations");
        editMenu.add(configMenuItem);

        /// THEME MENU
        initComponentThemeMenu();

        //> HELP MENU - contentMenuItem
        contentMenuItem = new JMenuItem();
        contentMenuItem.setIcon(Ico.i16("/img/std/DlgInfos.png", this));
        contentMenuItem.setMnemonic('c');
        contentMenuItem.setText(bundle.getString("MenuHelpDocs"));

        //> HELP MENU - aboutMenuItem
        aboutMenuItem = new JMenuItem();
        //contentMenuItem.setIcon(Ico.i16("/img/std/DlgInfos.png", this));
        contentMenuItem.setMnemonic('a');
        aboutMenuItem.setText(bundle.getString("MenuHelpAbout")); // NOI18N

        /// HELP MENU - SETUP
        helpMenu = new JMenu();
        helpMenu.setMnemonic('h');
        helpMenu.setText(bundle.getString("MenuHelp")); // NOI18N
        helpMenu.setToolTipText("Trouver de l'aide");
        helpMenu.add(contentMenuItem);
        helpMenu.add(aboutMenuItem);

        /**
         * GENERATE MENU BAR
         */
        menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(displayMenu);
        menuBar.add(toolsMenu);
        menuBar.add(editMenu);
        menuBar.add(themeMenu);
        menuBar.add(helpMenu);

        /**
         * Toolbar setup
         */
        // Toolbar - Button Hide
        tbBtnHide = new JButton();
        tbBtnHide.setIcon(Ico.i48("/img/oz/hide_minimize_minimum_window_size_ui.png", this));
        tbBtnHide.setText("Réduire");
        tbBtnHide.setFocusable(false);
        tbBtnHide.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbBtnHide.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbBtnHide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                tbBtnHideActionPerformed(evt);
            }
        });

        // Toolbar - Button Exit
        tbBtnExit = new JButton();
        tbBtnExit.setIcon(Ico.i48("/img/oz/exit.png", this));
        tbBtnExit.setText("Quitter");
        tbBtnExit.setFocusable(false);
        tbBtnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbBtnExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbBtnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                tbBtnExitActionPerformed(evt);
            }
        });

        // Manage toolbar insertion
        mainToolBar = new JToolBar();
        mainToolBar.setRollover(true);
        mainToolBar.add(tbBtnHide);
        mainToolBar.add(tbBtnExit);
        mainToolBar.setAlignmentX(0);

        /**
         * Toolbar TOOLS
         */
        // Toolbar - Tools - Button S7 Connection
        tbBtnToolsS7Connexion = new JButton();
        tbBtnToolsS7Connexion.setIcon(Ico.i48("/img/std/Refresh.png", this));
        tbBtnToolsS7Connexion.setText(bundle.getString("toolsS7Connections"));
        tbBtnToolsS7Connexion.setFocusable(false);
        tbBtnToolsS7Connexion.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbBtnToolsS7Connexion.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbBtnToolsS7Connexion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (views[2].getRootWindow() != null) {
                    views[2].restoreFocus();
                } else {
                    DockingUtil.addWindow(views[2], rootWindow);
                }
            }
        });
        tbBtnToolsS7Connexion.setEnabled(true);

        // Toolbar - Tools - Button ManagerController
        tbBtnToolsManagerController = new JButton();
        tbBtnToolsManagerController.setIcon(Ico.i48("/img/std/Tree.png", this));
        tbBtnToolsManagerController.setText(bundle.getString("MenuItemControllerManager"));
        tbBtnToolsManagerController.setFocusable(false);
        tbBtnToolsManagerController.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbBtnToolsManagerController.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbBtnToolsManagerController.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                managerControllerWindowDisplayActionPerformed(evt);
            }
        });
        tbBtnToolsManagerController.setEnabled(true);

        // Toolbar - Tools - Button tbBtnToolsTagsControllerStart
        tbBtnToolsTagsControllerStart = new JButton();
        tbBtnToolsTagsControllerStart.setIcon(Ico.i48("/img/std/onOff/start.png", this));
        tbBtnToolsTagsControllerStart.setText(bundle.getString("toolsStartTagsCollector"));
        tbBtnToolsTagsControllerStart.setFocusable(false);
        tbBtnToolsTagsControllerStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbBtnToolsTagsControllerStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbBtnToolsTagsControllerStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startTagCollectorMenuItemActionPerformed(evt);
            }
        });
        tbBtnToolsTagsControllerStart.setEnabled(true);

        // Toolbar - Tools - Button tbBtnToolsTagsControllerStop
        tbBtnToolsTagsControllerStop = new JButton();
        tbBtnToolsTagsControllerStop.setIcon(Ico.i48("/img/std/onOff/stop.png", this));
        tbBtnToolsTagsControllerStop.setText(bundle.getString("toolsStopTagsCollector"));
        tbBtnToolsTagsControllerStop.setFocusable(false);
        tbBtnToolsTagsControllerStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbBtnToolsTagsControllerStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbBtnToolsTagsControllerStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopTagCollectorMenuItemActionPerformed(evt);
            }
        });
        tbBtnToolsTagsControllerStop.setEnabled(false);

        // Toolbar - Tools - Button tbBtnToolsThreadFrame
        tbBtnToolsThreadFrame = new JButton();
        tbBtnToolsThreadFrame.setIcon(Ico.i48("/img/std/thread.png", this));
        tbBtnToolsThreadFrame.setText(bundle.getString("MenuItemThread"));
        tbBtnToolsThreadFrame.setFocusable(false);
        tbBtnToolsThreadFrame.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbBtnToolsThreadFrame.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbBtnToolsThreadFrame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                threadWindowDisplayActionPerformed(evt);
            }
        });
        tbBtnToolsManagerController.setEnabled(true);

        // Manage toolbar Tools start/Stop
        toolsToolBar = new JToolBar();
        toolsToolBar.setRollover(true);
        toolsToolBar.add(tbBtnToolsS7Connexion);
        toolsToolBar.add(tbBtnToolsManagerController);
        toolsToolBar.add(new JToolBar.Separator());
        toolsToolBar.add(tbBtnToolsTagsControllerStart);
        toolsToolBar.add(tbBtnToolsTagsControllerStop);
        toolsToolBar.add(new JToolBar.Separator());
        toolsToolBar.add(tbBtnToolsThreadFrame);
        toolsToolBar.setAlignmentX(0);

        JPanel toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new BoxLayout(toolBarPanel, FlowLayout.LEFT));
        toolBarPanel.add(mainToolBar);
        toolBarPanel.add(toolsToolBar);

        /**
         * Managing main frame
         */
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        frame.setTitle(bundle.getString("AppName") + " " + bundle.getString("AppVersion")); // NOI18N
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setIconImage(Ico.i16("/img/obi/obi-signet-dark.png", this).getImage());
        frame.setName("MainWindowFrame"); // NOI18N
        frame.setSize(new java.awt.Dimension(1024, 680));

        frame.getContentPane().add(createToolBar(), BorderLayout.NORTH);
//        frame.getContentPane().add(mainToolBar, BorderLayout.NORTH);
//        frame.getContentPane().add(toolsToolBar, BorderLayout.NORTH);
        frame.getContentPane().add(toolBarPanel, BorderLayout.NORTH);
        frame.getContentPane().add(rootWindow, BorderLayout.CENTER);
        frame.setJMenuBar(menuBar);
        frame.setSize(900, 700);

    }

    /**
     * Creates the menu where the theme can be changed.
     *
     * @return the theme menu
     */
    private JMenu createThemesMenu() {
        JMenu themesMenu = new JMenu("Docking");

        DockingWindowsTheme[] themes = {new DefaultDockingTheme(),
            new BlueHighlightDockingTheme(),
            new SlimFlatDockingTheme(),
            new GradientDockingTheme(),
            new ShapedGradientDockingTheme(),
            new SoftBlueIceDockingTheme(),
            new ClassicDockingTheme()};

        ButtonGroup group = new ButtonGroup();

        for (int i = 0; i < themes.length; i++) {
            final DockingWindowsTheme theme = themes[i];

            JRadioButtonMenuItem item = new JRadioButtonMenuItem(theme.getName());
            item.setSelected(i == 4);
            group.add(item);

            themesMenu.add(item).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Clear the modified properties values
                    properties.getMap().clear(true);

                    setTheme(theme);
                }
            });
        }

        return themesMenu;
    }

    /**
     * Creates the menu where views can be shown and focused.
     *
     * @return the focus view menu
     */
    private JMenu createFocusViewMenu() {
        JMenu viewsMenu = new JMenu("Focus fenêtre");

        for (int i = 0; i < views.length; i++) {
            final View view = views[i];
            if (view != null) {
                System.out.println("View :" + view.getTitle());
                Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName()
                        + " >> createFocusViewMenu >> " + view.getTitle());
                viewsMenu.add("Focus " + view.getTitle()).addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                // Ensure the view is shown in the root window
                                DockingUtil.addWindow(view, rootWindow);

                                // Transfer focus to the view
                                view.restoreFocus();
                            }
                        });
                    }
                });
            } else {
                Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName()
                        + " >> createFocusViewMenu >> " + "views[" + i + "] is null !");
            }
        }

        return viewsMenu;
    }

    /**
     * Creates the menu where different property values can be modified.
     *
     * @return the properties menu
     */
    private JMenu createPropertiesMenu() {
        JMenu buttonsMenu = new JMenu("Propriétés docking");

        buttonsMenu.add("Fermeture activée").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                properties.getDockingWindowProperties().setCloseEnabled(true);
            }
        });

        buttonsMenu.add("Fermeture désactivée").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                properties.getDockingWindowProperties().setCloseEnabled(false);
            }
        });

        buttonsMenu.add("Bloquer Layout").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Disable window operations
                properties.getDockingWindowProperties().setDragEnabled(false);
                properties.getDockingWindowProperties().setCloseEnabled(false);
                properties.getDockingWindowProperties().setMinimizeEnabled(false);
                properties.getDockingWindowProperties().setRestoreEnabled(false);
                properties.getDockingWindowProperties().setMaximizeEnabled(false);

                // Enable tab reordering inside tabbed panel
                properties.getTabWindowProperties().getTabbedPanelProperties().setTabReorderEnabled(true);
            }
        });

        buttonsMenu.add("Libérer Layout").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Enable window operations
                properties.getDockingWindowProperties().setDragEnabled(true);
                properties.getDockingWindowProperties().setCloseEnabled(true);
                properties.getDockingWindowProperties().setMinimizeEnabled(true);
                properties.getDockingWindowProperties().setRestoreEnabled(true);
                properties.getDockingWindowProperties().setMaximizeEnabled(true);

                // Disable tab reordering inside tabbed panel
                properties.getTabWindowProperties().getTabbedPanelProperties().setTabReorderEnabled(false);
            }
        });

        return buttonsMenu;
    }

    /**
     * Creates the menu where layout can be saved and loaded.
     *
     * @return the layout menu
     */
    private JMenu createLayoutMenu() {
        JMenu layoutMenu = new JMenu("Layout");

        layoutMenu.add("Layout (par défaut)").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDefaultLayout();
            }
        });

        layoutMenu.addSeparator();

        for (int i = 0; i < layouts.length; i++) {
            final int j = i;

            layoutMenu.add("Sauver Layout " + i).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        // Save the layout in a byte array
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(bos);
                        rootWindow.write(out, false);
                        out.close();
                        layouts[j] = bos.toByteArray();
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            });
        }

        layoutMenu.addSeparator();

        for (int i = 0; i < layouts.length; i++) {
            final int j = i;

            layoutMenu.add("Charger Layout" + j).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (layouts[j] != null) {
                                try {
                                    // Load the layout from a byte array
                                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(layouts[j]));
                                    rootWindow.read(in, true);
                                    in.close();
                                } catch (IOException e1) {
                                    throw new RuntimeException(e1);
                                }
                            }
                        }
                    });
                }
            });
        }

        return layoutMenu;
    }

    /**
     * Creates the menu where individual window bars can be enabled and
     * disabled.
     *
     * @return the window bar menu
     */
    private JMenu createWindowBarsMenu() {
        JMenu barsMenu = new JMenu("Barre de fenêtre");

        for (int i = 0; i < 4; i++) {
            final Direction d = Direction.getDirections()[i];
            String tmp = "haut";
            if (d.toString().matches("Up")) {
                tmp = "le Haut";
            } else if (d.toString().matches("Right")) {
                tmp = "la Droite";
            } else if (d.toString().matches("Down")) {
                tmp = "le Bas";
            } else if (d.toString().matches("Left")) {
                tmp = "la Gauche";
            }

            JCheckBoxMenuItem item = new JCheckBoxMenuItem("Vers " + tmp);
            item.setSelected(d == Direction.DOWN);
            barsMenu.add(item).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Enable/disable the window bar
                    rootWindow.getWindowBar(d).setEnabled(!rootWindow.getWindowBar(d).isEnabled());
                }
            });
        }

        return barsMenu;
    }

    /**
     * Creates the menu where not shown views can be shown.
     *
     * @return the view menu
     */
    private JMenu createViewMenu() {
        JMenu menu = new JMenu("Fenêtres");

        for (int i = 0; i < views.length; i++) {
            final View view = views[i];
            if (view != null) {
                viewItems[i] = new JMenuItem(view.getTitle());
                viewItems[i].setEnabled(views[i].getRootWindow() == null);
                menu.add(viewItems[i]).addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (view.getRootWindow() != null) {
                            view.restoreFocus();
                        } else {
                            DockingUtil.addWindow(view, rootWindow);
                        }
                    }
                });
            } else {
                Util.out(Util.errLine() + MainWindowDocking.class.getSimpleName() + " : createViewMenu >> createViewMenu : "
                        + "views[" + i + "] is null !");
            }
        }

        return menu;
    }

    /**
     * When init component and Menu Theme is cretae
     */
    private void initComponentThemeMenu() {

        //> THEME MENU - metalMenuItem
        metalMenuItem = new JMenuItem();
        //metalMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        metalMenuItem.setMnemonic('t');
        metalMenuItem.setText("Metal");
        metalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                metalMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - nimbusMenuItem
        nimbusMenuItem = new JMenuItem();
        //nimbusMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        nimbusMenuItem.setMnemonic('t');
        nimbusMenuItem.setText("Nimbus");
        nimbusMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nimbusMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - CDEMotifMenuItem
        CDEMotifMenuItem = new JMenuItem();
        //CDEMotifMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        CDEMotifMenuItem.setMnemonic('t');
        CDEMotifMenuItem.setText("CDE/Motif");
        CDEMotifMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CDEMotifMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - windowMenuItem
        windowMenuItem = new JMenuItem();
        //windowMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        windowMenuItem.setMnemonic('t');
        windowMenuItem.setText("Windows");
        windowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - windowClassicMenuItem
        windowClassicMenuItem = new JMenuItem();
        //windowClassicMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        windowClassicMenuItem.setMnemonic('t');
        windowClassicMenuItem.setText("Windows Classic");
        windowClassicMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowClassicMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - flatLightLafMenuItem
        flatLightLafMenuItem = new JMenuItem();
        //flatLightLafMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        flatLightLafMenuItem.setMnemonic('t');
        flatLightLafMenuItem.setText("Flat - Light");
        flatLightLafMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flatLightLafMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - flatDarkLafMenuItem
        flatDarkLafMenuItem = new JMenuItem();
        //flatDarkLafMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        flatDarkLafMenuItem.setMnemonic('t');
        flatDarkLafMenuItem.setText("Flat - Dark");
        flatDarkLafMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flatDarkLafMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - flatGitHubLightMenuItem
        flatGitHubLightMenuItem = new JMenuItem();
        //flatGitHubLightMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        flatGitHubLightMenuItem.setMnemonic('t');
        flatGitHubLightMenuItem.setText("Flat - GitHub Light");
        flatGitHubLightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flatGitHubLightMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - flatGitHubDarkMenuItem
        flatGitHubDarkMenuItem = new JMenuItem();
        //flatGitHubDarkMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        flatGitHubDarkMenuItem.setMnemonic('t');
        flatGitHubDarkMenuItem.setText("Flat - GitHub Dark");
        flatGitHubDarkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flatGitHubDarkMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - flatSolorizedLightMenuItem
        flatSolorizedLightMenuItem = new JMenuItem();
        //flatSolorizedLightMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        flatSolorizedLightMenuItem.setMnemonic('t');
        flatSolorizedLightMenuItem.setText("Flat - Solarized Light");
        flatSolorizedLightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flatSolorizedLightMenuItemActionPerformed(evt);
            }
        });

        //> THEME MENU - flatSolorizedDarkMenuItem
        flatSolorizedDarkMenuItem = new JMenuItem();
        //flatSolorizedDarkMenuItem.setIcon(Ico.i16("/img/oz/config.png", this));
        flatSolorizedDarkMenuItem.setMnemonic('t');
        flatSolorizedDarkMenuItem.setText("Flat - Solarized Dark");
        flatSolorizedDarkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flatSolorizedDarkMenuItemActionPerformed(evt);
            }
        });

        /// MENU DOCKING
        /// THEME MENU - SETUP
        themeMenu = new JMenu();
        themeMenu.setMnemonic('e');
        themeMenu.setText("Thèmes");
        themeMenu.setToolTipText("Gestion des configurations");
        themeMenu.add(metalMenuItem);
        themeMenu.add(nimbusMenuItem);
        themeMenu.add(CDEMotifMenuItem);
        themeMenu.add(windowMenuItem);
        themeMenu.add(windowClassicMenuItem);
        themeSeparator = new JPopupMenu.Separator();
        themeMenu.add(themeSeparator);
        themeMenu.add(flatLightLafMenuItem);
        themeMenu.add(flatDarkLafMenuItem);
        themeMenu.add(flatGitHubLightMenuItem);
        themeMenu.add(flatGitHubDarkMenuItem);
        themeMenu.add(flatSolorizedLightMenuItem);
        themeMenu.add(flatSolorizedDarkMenuItem);
        themeMenu.add(new JPopupMenu.Separator());
        themeMenu.add(createThemesMenu());

    }

    /**
     * Set frame visible
     *
     * @param visible true to render visible
     */
    public void setVisible(boolean visible) {
        frame.setVisible(true);
    }

    /**
     * Tag Collector Thread Listener : On Processing Thread
     *
     * Will be call when thread is processing and display message to trayIcon
     */
    @Override
    public void onProcessingThread(Thread t) {
        trayIcon.displayMessage("OBI",
                "Main Tag collector system has start !",
                TrayIcon.MessageType.INFO);
    }

    /**
     * Tag Collector Thread Listener : On Olding Thread
     *
     * Will be call when thread is olding and display message to trayIcon
     */
    @Override
    public void onProcessingSubStopThread(Thread t) {
        trayIcon.displayMessage("OBI",
                "Main Tag collector system has stop !",
                TrayIcon.MessageType.INFO);
    }

    /**
     * *
     * Tag Collector Thread Listener : On process kill Thread
     *
     * @param m process to be killed
     */
    @Override
    public void onProcessingStopThread(Thread m) {
        trayIcon.displayMessage("OBI",
                "Main Tag collector system has stop !",
                TrayIcon.MessageType.ERROR);
    }

    @Override
    public void onSubProcessActivityState(Thread thread, Boolean activity) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onCollectionCount(Thread thread, int count) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onDuration(Thread t, int i, long toEpochMilli) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /**
     *
     * @param thread the value of thread
     * @param ms the value of ms
     */
    @Override
    public void onProcessingSubCycleTime(Thread thread, Long ms) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onProcessingSubThread(Thread thread) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /**
     *
     * @param thread the value of thread
     * @param ms the value of ms
     */
    @Override
    public void onProcessingCycleTime(Thread thread, Long ms) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onErrorCollection(Thread thread, String message) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
