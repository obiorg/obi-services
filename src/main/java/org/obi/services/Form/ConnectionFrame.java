/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.obi.services.Form;

import java.awt.Color;
import java.awt.TrayIcon;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.obi.services.core.moka7.IntByRef;
import org.obi.services.core.moka7.S7;
import org.obi.services.core.moka7.S7CpInfo;
import org.obi.services.core.moka7.S7CpuInfo;
import org.obi.services.core.moka7.S7OrderCode;
import org.obi.services.core.moka7.S7Szl;
import org.obi.services.app.ConnexionForm;
import org.obi.services.app.MachineConnection;
import org.obi.services.entities.Machines;
import org.obi.services.listener.ConnectionListener;
import org.obi.services.listener.DatabaseFrameListener;
import org.obi.services.model.DatabaseModel;
import org.obi.services.moka.OrderCode;
import org.obi.services.util.Ico;
import org.obi.services.util.Settings;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class ConnectionFrame extends javax.swing.JInternalFrame
        implements InternalFrameListener, ConnectionListener {

    /**
     * Counter frame allow to count the frame
     */
    static Integer openFrameCount = 0;
    static final int xOffset = 30, yOffset = 30;

    private TrayIcon trayIcon;

    // List containing machines description setup on updateConnection
    private List<MachineConnection> machinesConnections = new ArrayList<>();

    // Mange envent lister 
    private ArrayList<ConnectionListener> connectionListeners = new ArrayList<>();

    public void addListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    /**
     * Creates new form ConfigFrame
     */
    public ConnectionFrame() {
        trayIcon = new TrayIcon(Ico.i16("/img/obi/obi-signet-dark.png", this).getImage());
        initComponents();
        updateConnexionList();

        // 
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        openFrameCount++; // increment configFrame counter

        this.setClosable(true);
        addInternalFrameListener(this);
    }

    /**
     * Creates new form ConfigFrame
     */
    public ConnectionFrame(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
        initComponents();
        updateConnexionList();

        // 
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        openFrameCount++; // increment configFrame counter

        this.setClosable(true);
        addInternalFrameListener(this);
    }

    private void updateConnexionList() {
        // Read saved data
        Object tmp = Settings.read(Settings.CONFIG, Settings.URL_OBI);
        if (tmp == null) {
            tmp = "jdbc:sqlserver:<hostname>\\<instance>:<port 1433>;databaseName=<dbName>?<user>?<password>";
            trayIcon.displayMessage("OBI",
                    "Connexion schema does not exist ! Please Configure database and save", TrayIcon.MessageType.ERROR);
            return;
        }
        String urlOBI = tmp.toString();//"jdbc:sqlserver:10.116.26.35\\SQLSERVER:1433;databaseName=optimaint?sa?Opt!M@!nt";

        // Récupoère le modèle et valide que l'on peut se connecter
        DatabaseModel model = DatabaseModel.parseFull(urlOBI);

        String url = "";
        try {
            Connection conn = DatabaseFrame.toConnection(model);
            if (conn == null) {
                trayIcon.displayMessage("OBI",
                        "Unable to parse schema defined in database configuration  ! Check you settings", TrayIcon.MessageType.ERROR);
                return;
            }

            // Prepare query and statement before getting result and metadata for colum
            String query = "SELECT * FROM dbo.machines";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData rsMetaData = rs.getMetaData();

            // Create Model to old the list of machine
            DefaultListModel lst = new DefaultListModel();
            listConnexions.removeAll();
            machinesConnections.stream().forEach((machineConnection) -> {
                machineConnection.doStop();
                machineConnection.removeClientListener(machineConnection);
            });

            // Loop over element and convert to object
            while (rs.next()) {
                Machines m = new Machines();
                int count = rsMetaData.getColumnCount();
                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    String c = rsMetaData.getColumnName(i);
                    m.update(rs, c);
                }
                // Add machine to list element
                lst.addElement(m);

                // Create corresponding machine list
                MachineConnection mc = new MachineConnection(m);
                mc.addClientListener(this);
                machinesConnections.add(mc);

            }
            listConnexions.setModel(lst);

        } catch (SQLException ex) {
            Util.out(ConnexionForm.class.getName() + " >> btnRefreshConnexionActionPerformed for url(" + url + ") : " + ex.getLocalizedMessage());

        } finally {

        }

    }

    private void resetTester() {

        plcSetConnectionState.setBackground(Color.LIGHT_GRAY);
        plcSetConnectionResult.setText("-");
        plcSetConnectionNegociation.setText("-");
        plcSetConnectionState.setText("-");
        plcSetConnectionDuration.setText("???? ???");

        plcSetConnectionDateHeure.setText("??");

        plcOrderCode.setText("??");
        plcFirmwareVersion.setText("??");
        plcStatus.setText("??");

        plcModuleTypeName.setText("??");
        plcSerialNumber.setText("??");
        plcASName.setText("??");
        plcCopyRight.setText("??");
        plcModuleName.setText("??");

        plcCPMaxPDULength.setText("??");
        plcCPMaxConnections.setText("??");
        plcCPMaxMPIRate.setText("??");
        plcCPMaxBusRate.setText("??");

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableLinkPopupMenu = new javax.swing.JPopupMenu();
        mainSplitPanel = new javax.swing.JSplitPane();
        menuPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listConnexions = new javax.swing.JList<>();
        btnRefreshConnexion = new javax.swing.JButton();
        contentPanel = new javax.swing.JPanel();
        contentSplitPanel = new javax.swing.JSplitPane();
        paramPanel = new javax.swing.JPanel();
        _ip = new javax.swing.JLabel();
        address = new javax.swing.JTextField();
        _rack = new javax.swing.JLabel();
        rack = new javax.swing.JSpinner();
        _slot = new javax.swing.JLabel();
        slot = new javax.swing.JSpinner();
        btnClear = new javax.swing.JButton();
        btnTest = new javax.swing.JButton();
        plcSetConnectionState = new javax.swing.JToggleButton();
        plcStatusPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel4 = new javax.swing.JPanel();
        connectionPanel = new javax.swing.JPanel();
        _connection = new javax.swing.JLabel();
        _pduNegociation = new javax.swing.JLabel();
        _plcDateHeure = new javax.swing.JLabel();
        plcSetConnectionResult = new javax.swing.JTextField();
        plcSetConnectionNegociation = new javax.swing.JTextField();
        plcSetConnectionDateHeure = new javax.swing.JTextField();
        _labForOrderCode = new javax.swing.JLabel();
        _labForFirmwareVersion = new javax.swing.JLabel();
        _labForPlcStatus = new javax.swing.JLabel();
        plcOrderCode = new javax.swing.JTextField();
        plcFirmwareVersion = new javax.swing.JTextField();
        plcStatus = new javax.swing.JTextField();
        _duration = new javax.swing.JLabel();
        plcSetConnectionDuration = new javax.swing.JLabel();
        orderCodePanel = new javax.swing.JPanel();
        _labForPlcModuleTypeName1 = new javax.swing.JLabel();
        _labForPlcSerialNumber1 = new javax.swing.JLabel();
        _labForPlcASName1 = new javax.swing.JLabel();
        _labForCopyRiht1 = new javax.swing.JLabel();
        _labForPlcModuleName1 = new javax.swing.JLabel();
        plcModuleTypeName = new javax.swing.JTextField();
        plcSerialNumber = new javax.swing.JTextField();
        plcASName = new javax.swing.JTextField();
        plcCopyRight = new javax.swing.JTextField();
        plcModuleName = new javax.swing.JTextField();
        _labForCpMaxPDULength = new javax.swing.JLabel();
        _labForCpMaxConnections = new javax.swing.JLabel();
        _labForCpMaxMPIRate = new javax.swing.JLabel();
        _labForCpMaxBusRate = new javax.swing.JLabel();
        plcCPMaxPDULength = new javax.swing.JTextField();
        plcCPMaxConnections = new javax.swing.JTextField();
        plcCPMaxMPIRate = new javax.swing.JTextField();
        plcCPMaxBusRate = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();

        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("bundles/Fr_fr"); // NOI18N
        setTitle(bundle.getString("ConfigFrameTitle")); // NOI18N
        setToolTipText("Fenêtre des configurations");
        setFrameIcon(Ico.i16("/img/obi/obi-signet-light.png", this));
        setPreferredSize(new java.awt.Dimension(889, 522));
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }
        setVisible(true);

        mainSplitPanel.setDividerLocation(240);
        mainSplitPanel.setMinimumSize(new java.awt.Dimension(240, 1));

        menuPanel.setPreferredSize(new java.awt.Dimension(300, 464));

        jLabel1.setText("Connexions");

        listConnexions.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Vide" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        listConnexions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listConnexions.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listConnexionsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listConnexions);

        btnRefreshConnexion.setIcon(Ico.i16("/img/std/Refresh.png", this));
        btnRefreshConnexion.setText("");
        btnRefreshConnexion.setToolTipText("Update machine list");
        btnRefreshConnexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshConnexionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout menuPanelLayout = new javax.swing.GroupLayout(menuPanel);
        menuPanel.setLayout(menuPanelLayout);
        menuPanelLayout.setHorizontalGroup(
            menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(menuPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRefreshConnexion, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        menuPanelLayout.setVerticalGroup(
            menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(btnRefreshConnexion, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainSplitPanel.setLeftComponent(menuPanel);

        contentSplitPanel.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        _ip.setText("Adresse IP");

        address.setText("-");
        address.setEnabled(false);
        address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressActionPerformed(evt);
            }
        });

        _rack.setText("Rack (defaut 0)");

        rack.setEnabled(false);

        _slot.setText("Slot (défaut 2)");

        slot.setEnabled(false);
        slot.setValue(2);

        btnClear.setText("Initialiser");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnTest.setText("PROCESS FINISHED");
        btnTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestActionPerformed(evt);
            }
        });

        plcSetConnectionState.setText("-");
        plcSetConnectionState.setToolTipText("");
        plcSetConnectionState.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcSetConnectionStateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout paramPanelLayout = new javax.swing.GroupLayout(paramPanel);
        paramPanel.setLayout(paramPanelLayout);
        paramPanelLayout.setHorizontalGroup(
            paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paramPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(_rack, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                    .addComponent(_ip, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(_slot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(slot)
                    .addComponent(address, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addComponent(rack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(paramPanelLayout.createSequentialGroup()
                        .addComponent(btnTest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plcSetConnectionState)))
                .addContainerGap())
        );
        paramPanelLayout.setVerticalGroup(
            paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paramPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_ip)
                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_rack)
                    .addComponent(rack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_slot)
                    .addComponent(slot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnTest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(plcSetConnectionState)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contentSplitPanel.setTopComponent(paramPanel);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Schéma Protéction"));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 297, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Connexion & Appareil"));

        _connection.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _connection.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _connection.setText("Connection à ");

        _pduNegociation.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _pduNegociation.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _pduNegociation.setText("PDU négociation");

        _plcDateHeure.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _plcDateHeure.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _plcDateHeure.setText("Date et heure PLC");

        plcSetConnectionResult.setEditable(false);
        plcSetConnectionResult.setText("??");
        plcSetConnectionResult.setBorder(null);

        plcSetConnectionNegociation.setEditable(false);
        plcSetConnectionNegociation.setText("??");
        plcSetConnectionNegociation.setBorder(null);
        plcSetConnectionNegociation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcSetConnectionNegociationActionPerformed(evt);
            }
        });

        plcSetConnectionDateHeure.setEditable(false);
        plcSetConnectionDateHeure.setText("??");
        plcSetConnectionDateHeure.setBorder(null);
        plcSetConnectionDateHeure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcSetConnectionDateHeureActionPerformed(evt);
            }
        });

        _labForOrderCode.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForOrderCode.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForOrderCode.setText("Order Code");

        _labForFirmwareVersion.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForFirmwareVersion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForFirmwareVersion.setText("Firmware version");

        _labForPlcStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForPlcStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForPlcStatus.setText("PLC Staus");

        plcOrderCode.setEditable(false);
        plcOrderCode.setText("??");
        plcOrderCode.setBorder(null);
        plcOrderCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcOrderCodeActionPerformed(evt);
            }
        });

        plcFirmwareVersion.setEditable(false);
        plcFirmwareVersion.setText("??");
        plcFirmwareVersion.setBorder(null);
        plcFirmwareVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcFirmwareVersionActionPerformed(evt);
            }
        });

        plcStatus.setEditable(false);
        plcStatus.setText("??");
        plcStatus.setBorder(null);
        plcStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcStatusActionPerformed(evt);
            }
        });

        _duration.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _duration.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _duration.setText("Durée trait. [ms]");

        plcSetConnectionDuration.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        plcSetConnectionDuration.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        plcSetConnectionDuration.setText("### ###");

        javax.swing.GroupLayout connectionPanelLayout = new javax.swing.GroupLayout(connectionPanel);
        connectionPanel.setLayout(connectionPanelLayout);
        connectionPanelLayout.setHorizontalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(connectionPanelLayout.createSequentialGroup()
                        .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addComponent(_connection, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(plcSetConnectionResult))
                            .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addComponent(_pduNegociation, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(plcSetConnectionNegociation)))
                        .addGap(6, 6, 6))
                    .addGroup(connectionPanelLayout.createSequentialGroup()
                        .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(_duration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_labForPlcStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_labForOrderCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_plcDateHeure, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_labForFirmwareVersion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(plcSetConnectionDateHeure, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                            .addComponent(plcOrderCode, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(plcFirmwareVersion)
                            .addComponent(plcStatus)
                            .addComponent(plcSetConnectionDuration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        connectionPanelLayout.setVerticalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_connection)
                    .addComponent(plcSetConnectionResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_pduNegociation)
                    .addComponent(plcSetConnectionNegociation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_plcDateHeure)
                    .addComponent(plcSetConnectionDateHeure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_labForOrderCode)
                    .addComponent(plcOrderCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_labForFirmwareVersion)
                    .addComponent(plcFirmwareVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_labForPlcStatus)
                    .addComponent(plcStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_duration)
                    .addComponent(plcSetConnectionDuration))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        orderCodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("CPU Info & CP Info"));

        _labForPlcModuleTypeName1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForPlcModuleTypeName1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForPlcModuleTypeName1.setText("Type Module");

        _labForPlcSerialNumber1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForPlcSerialNumber1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForPlcSerialNumber1.setText("Numéro Série");

        _labForPlcASName1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForPlcASName1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForPlcASName1.setText("Nom AS");

        _labForCopyRiht1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForCopyRiht1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForCopyRiht1.setText("CopyRight");

        _labForPlcModuleName1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForPlcModuleName1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForPlcModuleName1.setText("Nom Module");

        plcModuleTypeName.setEditable(false);
        plcModuleTypeName.setText("??");
        plcModuleTypeName.setBorder(null);

        plcSerialNumber.setEditable(false);
        plcSerialNumber.setText("??");
        plcSerialNumber.setBorder(null);
        plcSerialNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcSerialNumberActionPerformed(evt);
            }
        });

        plcASName.setEditable(false);
        plcASName.setText("??");
        plcASName.setBorder(null);
        plcASName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcASNameActionPerformed(evt);
            }
        });

        plcCopyRight.setEditable(false);
        plcCopyRight.setText("??");
        plcCopyRight.setBorder(null);
        plcCopyRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcCopyRightActionPerformed(evt);
            }
        });

        plcModuleName.setEditable(false);
        plcModuleName.setText("??");
        plcModuleName.setBorder(null);
        plcModuleName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcModuleNameActionPerformed(evt);
            }
        });

        _labForCpMaxPDULength.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForCpMaxPDULength.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForCpMaxPDULength.setText("Max long PDU");

        _labForCpMaxConnections.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForCpMaxConnections.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForCpMaxConnections.setText("Max connections");

        _labForCpMaxMPIRate.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForCpMaxMPIRate.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForCpMaxMPIRate.setText("Max MPI rate (bps)");

        _labForCpMaxBusRate.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        _labForCpMaxBusRate.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labForCpMaxBusRate.setText("Max Bus Rate");

        plcCPMaxPDULength.setEditable(false);
        plcCPMaxPDULength.setText("??");
        plcCPMaxPDULength.setBorder(null);

        plcCPMaxConnections.setEditable(false);
        plcCPMaxConnections.setText("??");
        plcCPMaxConnections.setBorder(null);
        plcCPMaxConnections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcCPMaxConnectionsActionPerformed(evt);
            }
        });

        plcCPMaxMPIRate.setEditable(false);
        plcCPMaxMPIRate.setText("??");
        plcCPMaxMPIRate.setBorder(null);
        plcCPMaxMPIRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcCPMaxMPIRateActionPerformed(evt);
            }
        });

        plcCPMaxBusRate.setEditable(false);
        plcCPMaxBusRate.setText("??");
        plcCPMaxBusRate.setBorder(null);
        plcCPMaxBusRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plcCPMaxBusRateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout orderCodePanelLayout = new javax.swing.GroupLayout(orderCodePanel);
        orderCodePanel.setLayout(orderCodePanelLayout);
        orderCodePanelLayout.setHorizontalGroup(
            orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(orderCodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(orderCodePanelLayout.createSequentialGroup()
                        .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(_labForCpMaxMPIRate, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(_labForCpMaxBusRate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(_labForCpMaxConnections, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(plcCPMaxBusRate, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                            .addComponent(plcCPMaxMPIRate)
                            .addComponent(plcCPMaxConnections)))
                    .addGroup(orderCodePanelLayout.createSequentialGroup()
                        .addComponent(_labForCpMaxPDULength, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plcCPMaxPDULength))
                    .addComponent(jSeparator2)
                    .addGroup(orderCodePanelLayout.createSequentialGroup()
                        .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_labForPlcASName1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_labForPlcModuleTypeName1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_labForPlcSerialNumber1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_labForCopyRiht1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_labForPlcModuleName1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(plcSerialNumber, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(plcASName, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(plcCopyRight, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(plcModuleName)
                            .addComponent(plcModuleTypeName))))
                .addContainerGap())
        );
        orderCodePanelLayout.setVerticalGroup(
            orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(orderCodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(orderCodePanelLayout.createSequentialGroup()
                        .addComponent(plcModuleTypeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(plcSerialNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_labForPlcSerialNumber1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(plcASName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_labForPlcASName1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(plcCopyRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_labForCopyRiht1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(plcModuleName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_labForPlcModuleName1)))
                    .addComponent(_labForPlcModuleTypeName1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_labForCpMaxPDULength)
                    .addComponent(plcCPMaxPDULength, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_labForCpMaxConnections)
                    .addComponent(plcCPMaxConnections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_labForCpMaxMPIRate)
                    .addComponent(plcCPMaxMPIRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(orderCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_labForCpMaxBusRate)
                    .addComponent(plcCPMaxBusRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("SZL"));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 84, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout plcStatusPanelLayout = new javax.swing.GroupLayout(plcStatusPanel);
        plcStatusPanel.setLayout(plcStatusPanelLayout);
        plcStatusPanelLayout.setHorizontalGroup(
            plcStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plcStatusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(plcStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(plcStatusPanelLayout.createSequentialGroup()
                        .addComponent(connectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orderCodePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)))
            .addGroup(plcStatusPanelLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        plcStatusPanelLayout.setVerticalGroup(
            plcStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plcStatusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(plcStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(orderCodePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(connectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(plcStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(60, 60, 60))
        );

        contentSplitPanel.setRightComponent(plcStatusPanel);

        javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contentSplitPanel)
                .addContainerGap())
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contentSplitPanel)
                .addContainerGap())
        );

        mainSplitPanel.setRightComponent(contentPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 876, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void listConnexionsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listConnexionsValueChanged
        //        System.out.println("First index: " + evt.getFirstIndex());
        //        System.out.println(", Last index: " + evt.getLastIndex());
        boolean adjust = evt.getValueIsAdjusting();
        //        System.out.println(", Adjusting? " + adjust);
        if (!adjust) {
            resetTester();
            JList list = (JList) evt.getSource();
            int selections[] = list.getSelectedIndices();
            List<Object> selectionValues = list.getSelectedValuesList();
            for (int i = 0, n = selections.length; i < n; i++) {
                if (i == 0) {
                    System.out.println(" Selections: ");
                }
                System.out.println(selections[i] + "/" + selectionValues.get(i) + " ");

                MachineConnection mc = machinesConnections.get(selections[i]);
                if (!mc.isAlive()) {
                    mc.start();
                    mc.connect();
                } else {
                    address.setText(mc.getMachine().getAddress());
                    rack.setValue(mc.getMachine().getRack());
                    slot.setValue(mc.getMachine().getSlot());
                    mc.connectState();
                }

                mc.dateTime();
                mc.orderCode();
                mc.plcStatus();
                mc.plcInfo();
                mc.cpInfo();
                mc.SzlInfo();
                btnTest.setEnabled(false);

            }
        }
    }//GEN-LAST:event_listConnexionsValueChanged

    private void btnRefreshConnexionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshConnexionActionPerformed
        updateConnexionList();
    }//GEN-LAST:event_btnRefreshConnexionActionPerformed

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addressActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        resetTester();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestActionPerformed
        // TODO add your handling code here:
        //        if(!plcSetConnection.isSelected()){
        //            // Reset All
        //            resetTester();
        //            Util.out("PLCTesterFrame >> btnTestActionPerformed : Tester reset ...");
        //            return;
        //        }
        //
        //        PLC testAPI = new PLC(ipAddress.getText(), (int) rack.getValue(), (int) slot.getValue());
        //        testAPI.addClientListener(this);
        //        testAPI.start();
        //        testAPI.connect();
        //        btnTest.setEnabled(false);
    }//GEN-LAST:event_btnTestActionPerformed

    private void plcSetConnectionStateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcSetConnectionStateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcSetConnectionStateActionPerformed

    private void plcSetConnectionNegociationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcSetConnectionNegociationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcSetConnectionNegociationActionPerformed

    private void plcSetConnectionDateHeureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcSetConnectionDateHeureActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcSetConnectionDateHeureActionPerformed

    private void plcOrderCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcOrderCodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcOrderCodeActionPerformed

    private void plcFirmwareVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcFirmwareVersionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcFirmwareVersionActionPerformed

    private void plcStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcStatusActionPerformed

    private void plcSerialNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcSerialNumberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcSerialNumberActionPerformed

    private void plcASNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcASNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcASNameActionPerformed

    private void plcCopyRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcCopyRightActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcCopyRightActionPerformed

    private void plcModuleNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcModuleNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcModuleNameActionPerformed

    private void plcCPMaxConnectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcCPMaxConnectionsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcCPMaxConnectionsActionPerformed

    private void plcCPMaxMPIRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcCPMaxMPIRateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcCPMaxMPIRateActionPerformed

    private void plcCPMaxBusRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plcCPMaxBusRateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plcCPMaxBusRateActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel _connection;
    private javax.swing.JLabel _duration;
    private javax.swing.JLabel _ip;
    private javax.swing.JLabel _labForCopyRiht1;
    private javax.swing.JLabel _labForCpMaxBusRate;
    private javax.swing.JLabel _labForCpMaxConnections;
    private javax.swing.JLabel _labForCpMaxMPIRate;
    private javax.swing.JLabel _labForCpMaxPDULength;
    private javax.swing.JLabel _labForFirmwareVersion;
    private javax.swing.JLabel _labForOrderCode;
    private javax.swing.JLabel _labForPlcASName1;
    private javax.swing.JLabel _labForPlcModuleName1;
    private javax.swing.JLabel _labForPlcModuleTypeName1;
    private javax.swing.JLabel _labForPlcSerialNumber1;
    private javax.swing.JLabel _labForPlcStatus;
    private javax.swing.JLabel _pduNegociation;
    private javax.swing.JLabel _plcDateHeure;
    private javax.swing.JLabel _rack;
    private javax.swing.JLabel _slot;
    private javax.swing.JTextField address;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnRefreshConnexion;
    private javax.swing.JButton btnTest;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JSplitPane contentSplitPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JList<String> listConnexions;
    private javax.swing.JSplitPane mainSplitPanel;
    private javax.swing.JPanel menuPanel;
    private javax.swing.JPanel orderCodePanel;
    private javax.swing.JPanel paramPanel;
    private javax.swing.JTextField plcASName;
    private javax.swing.JTextField plcCPMaxBusRate;
    private javax.swing.JTextField plcCPMaxConnections;
    private javax.swing.JTextField plcCPMaxMPIRate;
    private javax.swing.JTextField plcCPMaxPDULength;
    private javax.swing.JTextField plcCopyRight;
    private javax.swing.JTextField plcFirmwareVersion;
    private javax.swing.JTextField plcModuleName;
    private javax.swing.JTextField plcModuleTypeName;
    private javax.swing.JTextField plcOrderCode;
    private javax.swing.JTextField plcSerialNumber;
    private javax.swing.JTextField plcSetConnectionDateHeure;
    private javax.swing.JLabel plcSetConnectionDuration;
    private javax.swing.JTextField plcSetConnectionNegociation;
    private javax.swing.JTextField plcSetConnectionResult;
    private javax.swing.JToggleButton plcSetConnectionState;
    private javax.swing.JTextField plcStatus;
    private javax.swing.JPanel plcStatusPanel;
    private javax.swing.JSpinner rack;
    private javax.swing.JSpinner slot;
    private javax.swing.JPopupMenu tableLinkPopupMenu;
    // End of variables declaration//GEN-END:variables

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        String methodName = getClass().getSimpleName() + Logger.getLogger(Util.class
                .getName()).getResourceBundleName() + " : internalFrameOpened() >> ";
        System.out.println(methodName + "internalFrameOpened !");
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        String methodName = getClass().getSimpleName() + Logger.getLogger(Util.class
                .getName()).getResourceBundleName() + " : internalFrameClosing() >> ";
        System.out.println(methodName + "internalFrameClosing !");
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        String methodName = getClass().getSimpleName() + Logger.getLogger(Util.class
                .getName()).getResourceBundleName() + " : internalFrameClosed() >> ";
        System.out.println(methodName + "internalFrameClosed !");
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
        String methodName = getClass().getSimpleName() + Logger.getLogger(Util.class
                .getName()).getResourceBundleName() + " : internalFrameIconified() >> ";
        System.out.println(methodName + "internalFrameIconified !");
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
        String methodName = getClass().getSimpleName() + Logger.getLogger(Util.class
                .getName()).getResourceBundleName() + " : internalFrameDeiconified() >> ";
        System.out.println(methodName + "internalFrameDeiconified !");
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        String methodName = getClass().getSimpleName() + Logger.getLogger(Util.class
                .getName()).getResourceBundleName() + " : internalFrameActivated() >> ";
        System.out.println(methodName + "internalFrameActivated !");
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        String methodName = getClass().getSimpleName() + Logger.getLogger(Util.class
                .getName()).getResourceBundleName() + " : internalFrameDeactivated() >> ";
        System.out.println(methodName + "internalFrameDeactivated !");
    }

    @Override
    public void onNewError(int errorCode, String err) {
        plcSetConnectionState.setBackground(Color.RED);
        plcSetConnectionState.setText("OFF");
        plcSetConnectionDuration.setText("-");
        plcSetConnectionResult.setText(errorCode + " : " + err);
        Util.out("ConnexionForm : onNewError >>" + errorCode + " : " + err);
    }

    @Override
    public void onConnectionSucced(Integer duration) {
        plcSetConnectionState.setBackground(Color.GREEN);
        plcSetConnectionState.setText("ON");
        plcSetConnectionDuration.setText(duration.toString());
        plcSetConnectionResult.setText(address.getText()
                + " (Rack=" + rack.getValue()
                + ", Slot=" + slot.getValue() + ")");
        btnTest.setEnabled(true);
        Util.out("ConnexionForm : onConnectionSucced :" + duration + "ms");
    }

    @Override
    public void onConnectionSucced(Machines machine) {
        address.setText(machine.getAddress());
        rack.setValue(machine.getRack());
        slot.setValue(machine.getSlot());

    }

    @Override
    public void onConnectionError(Integer duration) {
        plcSetConnectionState.setBackground(Color.RED);
        plcSetConnectionState.setText("OFF");
        plcSetConnectionDuration.setText(duration.toString());
        btnTest.setEnabled(true);
        Util.out("ConnexionForm : onConnectionWrong :" + duration + "ms");
    }

    @Override
    public void onPDUUpdate(Integer PDUNegotiationByte) {
        plcSetConnectionNegociation.setText("PDU negotiated : "
                + PDUNegotiationByte + " bytes");
        Util.out("ConnexionForm : onPDUUpdate : PDU negotiated = " + PDUNegotiationByte + " byte");
    }

    @Override
    public void onDateTimeResponse(Date plcDateTime) {
        plcSetConnectionDateHeure.setText(DateFormat.getInstance().format(plcDateTime));
        Util.out("ConnexionForm : onDateTimeResponse >> CPU Date/Time " + plcDateTime.toString());
    }

    @Override
    public void onOrderCodeResponse(S7OrderCode orderCode) {
        plcOrderCode.setText(orderCode.Code());
        plcFirmwareVersion.setText(OrderCode.firmware(orderCode));
    }

    @Override
    public void onPLCStatusResponse(IntByRef status) {
        if (status != null) {
            String r = status.Value == S7.S7CpuStatusRun ? "y" : "n";
            String s = status.Value == S7.S7CpuStatusStop ? "y" : "n";
            String u = r.matches("n") && s.matches("n") ? "y" : "-";

            Util.out("ConnexionForm : onPLCStatus >> status "
                    + "RUN[" + r + "] "
                    + "STOP[" + s + "] "
                    + "Indefini[" + s + "] = "
                    + status.Value);

            switch (status.Value) {
                case S7.S7CpuStatusRun:
                    plcStatus.setText("RUN");
                    break;
                case S7.S7CpuStatusStop:
                    plcStatus.setText("STOP");
                    break;
                default:
                    plcStatus.setText("Unknown (" + status.Value + ")");
                    break;
            }

        } else {
            Util.out("ConnexionForm : onPLCStatus >> status is null !");
        }
    }

    @Override
    public void isProcessing() {

    }

    @Override
    public void onPLCInfoResponse(S7CpuInfo CpuInfo) {
        plcModuleTypeName.setText(CpuInfo.ModuleTypeName());
        plcSerialNumber.setText(CpuInfo.SerialNumber());
        plcASName.setText(CpuInfo.ASName());
        plcCopyRight.setText(CpuInfo.Copyright());
        plcModuleName.setText(CpuInfo.ModuleName());
    }

    @Override
    public void onCpInfoResponse(S7CpInfo CpInfo) {
        plcCPMaxPDULength.setText(Integer.valueOf(CpInfo.MaxPduLength).toString());
        plcCPMaxConnections.setText(Integer.valueOf(CpInfo.MaxConnections).toString());
        plcCPMaxMPIRate.setText(Integer.valueOf(CpInfo.MaxMpiRate).toString());
        plcCPMaxBusRate.setText(Integer.valueOf(CpInfo.MaxBusRate).toString());
    }

    @Override
    public void onReadSzlResponse(S7Szl SZL) {
        Util.out("ConnectionForm >> onReadSzlResponse >> pas implémenté");
    }

}
