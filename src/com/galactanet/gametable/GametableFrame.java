/*
 * GametableFrame.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.galactanet.gametable.net.Connection;
import com.galactanet.gametable.net.NetworkThread;
import com.galactanet.gametable.net.Packet;
import com.galactanet.gametable.prefs.PreferenceDescriptor;
import com.galactanet.gametable.prefs.Preferences;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class GametableFrame extends JFrame implements ComponentListener, PropertyChangeListener, ActionListener,
    KeyListener
{
    class ToolButtonActionListener implements ActionListener
    {
        int m_id;

        ToolButtonActionListener(int id)
        {
            m_id = id;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            m_gametableCanvas.setActiveTool(m_id);
            m_gametableCanvas.requestFocus();
        }
    }

    class ToolButtonAbstractAction extends AbstractAction
    {
        int m_id;

        ToolButtonAbstractAction(int id)
        {
            m_id = id;
        }

        public void actionPerformed(ActionEvent event)
        {
            m_gametableCanvas.setActiveTool(m_id);
            m_gametableCanvas.requestFocus();
        }
    }

    // Global instance of the frame
    public static GametableFrame   g_gameTableFrame;

    // this gets bumped up every time the comm protocols change
    public final static int        COMM_VERSION               = 10;
    public final static int        PING_INTERVAL              = 2500;

    JPanel                         m_contentPane;

    JMenuBar                       m_menuBar                  = new JMenuBar();

    JMenu                          m_fileMenu                 = new JMenu("File");
    JMenuItem                      m_openMenuItem             = new JMenuItem("Open...");
    JMenuItem                      m_saveMenuItem             = new JMenuItem("Save");
    JMenuItem                      m_saveAsMenuItem           = new JMenuItem("Save As...");
    JMenuItem                      m_reacquirePogsMenuItem    = new JMenuItem("Reacquire Pogs");
    JMenuItem                      m_exitMenuItem             = new JMenuItem("Exit");

    JMenu                          m_networkMenu              = new JMenu("Network");
    JMenuItem                      m_listPlayersMenuItem      = new JMenuItem("List Players");
    JMenuItem                      m_hostMenuItem             = new JMenuItem("Host...");
    JMenuItem                      m_joinMenuItem             = new JMenuItem("Join...");
    JMenuItem                      m_disconnectMenuItem       = new JMenuItem("Disconnect");

    JMenu                          m_editMenu                 = new JMenu("Edit");
    JMenuItem                      m_undoMenuItem             = new JMenuItem("Undo");
    JMenuItem                      m_redoMenuItem             = new JMenuItem("Redo");

    JMenu                          m_mapMenu                  = new JMenu("Map");
    JMenuItem                      m_clearMapMenuItem         = new JMenuItem("Clear Layer");
    JMenuItem                      m_recenterMapMenuItem      = new JMenuItem("Recenter All Players");
    JCheckBoxMenuItem              m_privateLayerModeMenuItem = new JCheckBoxMenuItem("Manipulate Private Layer");

    JMenu                          m_gridModeMenu             = new JMenu("Grid Mode");
    JCheckBoxMenuItem              m_noGridModeMenuItem       = new JCheckBoxMenuItem("No Grid");
    JCheckBoxMenuItem              m_squareGridModeMenuItem   = new JCheckBoxMenuItem("Square Grid");
    JCheckBoxMenuItem              m_hexGridModeMenuItem      = new JCheckBoxMenuItem("Hex Grid");

    JMenu                          m_diceMacrosMenu           = new JMenu("Dice Macros");
    JMenuItem                      m_addDiceMacroMenuItem     = new JMenuItem("Add...");
    JMenuItem                      m_removeDiceMacroMenuItem  = new JMenuItem("Remove...");

    JMenu                          m_toolsMenu                = new JMenu("Tools");

    JMenu                          m_helpMenu                 = new JMenu("Help");
    JMenuItem                      m_aboutMenuItem            = new JMenuItem("About");

    JPanel                         m_propertiesArea           = new JPanel();
    public GametableCanvas         m_gametableCanvas          = new GametableCanvas();

    boolean                        m_bInitted;
    Dimension                      m_prevRightSize            = new Dimension(-1, -1);

    public List                    m_players                  = new ArrayList();

    // which player I am
    public int                     m_myPlayerIdx;

    String                         m_defaultName              = System.getProperty("user.name");
    String                         m_defaultCharName          = "Nochar";
    String                         m_defaultIP                = "localhost";
    public int                     m_defaultPort              = DEFAULT_PORT;
    String                         m_defaultPassword          = "";

    JPanel                         m_textAndEntryPanel        = new JPanel();
    public JTextField              m_textEntry                = new JTextField();
    JScrollPane2                   m_textLogScroller          = new JScrollPane2();
    JTextPane                      m_textLog                  = new JTextPane();
    JSplitPane                     m_mapChatSplitPane         = new JSplitPane();

    boolean                        m_bFirstPaint              = true;
    boolean                        m_bDisregardDividerChanges = true;
    int                            m_prevDividerLocFromBottom = -1;
    JPanel                         m_textAreaPanel            = new JPanel();
    JScrollPane                    m_macrosScrollPane         = new JScrollPane(
                                                                  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                                  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    JPanel                         m_macroButtonsPanel        = new JPanel();
    public JSplitPane              m_mapPogSplitPane          = new JSplitPane();
    public PogsPanel               m_pogsPanel                = null;
    public PogsPanel               m_underlaysPanel           = null;
    public PogPanel                m_pogPanel                 = null;
    private JToolBar               m_toolBar                  = new JToolBar();
    private ButtonGroup            m_toolButtonGroup          = new ButtonGroup();

    private List                   m_macros                   = new ArrayList();
    private Map                    m_macroMap                 = new HashMap();
    private List                   m_macroButtons             = new ArrayList();

    public final static int        NETSTATE_NONE              = 0;
    public final static int        NETSTATE_HOST              = 1;
    public final static int        NETSTATE_JOINED            = 2;
    public int                     m_netStatus                = NETSTATE_NONE;

    private volatile NetworkThread m_networkThread;
    private PeriodicExecutorThread m_executorThread;

    private long                   m_lastPingTime             = 0;

    public Color                   m_drawColor                = Color.BLACK;

    // window size and position
    private Point                  m_windowPos;
    private Dimension              m_windowSize;
    private boolean                m_bMaximized;

    // a flag to tell the app
    // not to size or center us.
    public boolean                 m_bLoadedState;
    public JTabbedPane             m_pogsTabbedPane           = new JTabbedPane();
    private JComboBox              m_colorCombo               = new JComboBox(g_comboColors);

    // full of Strings
    private List                   m_textSent                 = new ArrayList();
    private int                    m_textSentLoc              = 0;

    public final static int        REJECT_INVALID_PASSWORD    = 0;
    public final static int        REJECT_VERSION_MISMATCH    = 1;

    public final static int        DEFAULT_PORT               = 6812;

    public final static Integer[]  g_comboColors              = {
        new Integer(new Color(0, 0, 0).getRGB()), new Integer(new Color(198, 198, 198).getRGB()),
        new Integer(new Color(0, 0, 255).getRGB()), new Integer(new Color(0, 255, 0).getRGB()),
        new Integer(new Color(0, 255, 255).getRGB()), new Integer(new Color(255, 0, 0).getRGB()),
        new Integer(new Color(255, 0, 255).getRGB()), new Integer(new Color(255, 255, 0).getRGB()),
        new Integer(new Color(255, 255, 255).getRGB()), new Integer(new Color(0, 0, 132).getRGB()),
        new Integer(new Color(0, 132, 0).getRGB()), new Integer(new Color(0, 132, 132).getRGB()),
        new Integer(new Color(132, 0, 0).getRGB()), new Integer(new Color(132, 0, 132).getRGB()),
        new Integer(new Color(132, 132, 0).getRGB()), new Integer(new Color(132, 132, 132).getRGB()),
                                                              };

    // The current file path used by save and open.
    // NULL if unset.
    // one for the public map, one for the private map
    public File                    m_actingFilePublic;
    public File                    m_actingFilePrivate;

    private ToolManager            m_toolManager              = new ToolManager();
    private JToggleButton          m_toolButtons[]            = null;
    private Preferences            m_preferences              = new Preferences();
    private PogLibrary             m_pogLibrary               = null;

    public ProgressSpinner         m_progressSpinner          = new ProgressSpinner();

    // the id that will be assigned to the next player to join
    public int                     m_nextPlayerID;

    // the id that will be assigned to the next player to join
    public int                     m_nextStateID;

    /**
     * Construct the frame
     */
    public GametableFrame()
    {
        setTitle(GametableApp.VERSION);
        g_gameTableFrame = this;

        this.addComponentListener(this);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            m_contentPane = (JPanel)this.getContentPane();
            setSize(new Dimension(600, 500));

            m_openMenuItem.addActionListener(this);
            m_saveMenuItem.addActionListener(this);
            m_saveAsMenuItem.addActionListener(this);
            m_reacquirePogsMenuItem.addActionListener(this);
            m_exitMenuItem.addActionListener(this);
            m_undoMenuItem.addActionListener(this);
            m_redoMenuItem.addActionListener(this);
            m_clearMapMenuItem.setActionCommand("clearPogs");
            m_clearMapMenuItem.addActionListener(this);
            m_noGridModeMenuItem.addActionListener(this);
            m_squareGridModeMenuItem.addActionListener(this);
            m_hexGridModeMenuItem.addActionListener(this);
            m_privateLayerModeMenuItem.addActionListener(this);
            m_hostMenuItem.addActionListener(this);
            m_joinMenuItem.addActionListener(this);
            m_listPlayersMenuItem.addActionListener(this);
            m_disconnectMenuItem.addActionListener(this);
            m_recenterMapMenuItem.addActionListener(this);
            m_addDiceMacroMenuItem.addActionListener(this);
            m_removeDiceMacroMenuItem.addActionListener(this);
            m_aboutMenuItem.addActionListener(this);

            m_contentPane.setLayout(new BorderLayout());
            m_contentPane.setMaximumSize(new Dimension(32767, 32767));
            m_contentPane.setPreferredSize(new Dimension(2000, 300));
            m_contentPane.setRequestFocusEnabled(true);
            m_propertiesArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            m_propertiesArea.setMinimumSize(new Dimension(10, 10));
            m_propertiesArea.setPreferredSize(new Dimension(70, 100));
            m_propertiesArea.setRequestFocusEnabled(true);
            m_propertiesArea.setLayout(new BorderLayout());
            m_textAndEntryPanel.setLayout(new BorderLayout());
            m_textLog.setDoubleBuffered(true);
            m_textLog.setPreferredSize(new Dimension(500, 21));
            m_textEntry.addActionListener(new GametableFrame_m_textEntry_actionAdapter(this));
            m_textLogScroller.setDoubleBuffered(true);
            m_textLogScroller.setPreferredSize(new Dimension(450, 31));
            m_mapChatSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            m_mapChatSplitPane.setContinuousLayout(true);
            m_mapChatSplitPane.setResizeWeight(1.0);
            m_mapChatSplitPane.setBorder(null);
            m_gametableCanvas.setMaximumSize(new Dimension(32767, 32767));
            m_gametableCanvas.setPreferredSize(new Dimension(500, 400));
            m_textAndEntryPanel.setPreferredSize(new Dimension(500, 52));
            m_textAreaPanel.setLayout(new BorderLayout());
            m_mapPogSplitPane.setContinuousLayout(true);
            m_mapPogSplitPane.setBorder(null);
            m_macroButtonsPanel.setLayout(new BoxLayout(m_macroButtonsPanel, BoxLayout.Y_AXIS));
            m_colorCombo.setMaximumSize(new Dimension(100, 21));
            m_toolBar.setFloatable(false);
            m_toolBar.add(m_colorCombo, null);
            m_toolBar.add(Box.createHorizontalStrut(5));
            try
            {
                m_toolManager.initialize();
                int buttonSize = m_toolManager.getMaxIconSize();
                int numTools = m_toolManager.getNumTools();
                m_toolButtons = new JToggleButton[numTools];
                for (int toolId = 0; toolId < numTools; toolId++)
                {
                    ToolManager.Info info = m_toolManager.getToolInfo(toolId);
                    Image im = UtilityFunctions.createDrawableImage(buttonSize, buttonSize);
                    {
                        Graphics g = im.getGraphics();
                        Image icon = info.getIcon();
                        int offsetX = (buttonSize - icon.getWidth(null)) / 2;
                        int offsetY = (buttonSize - icon.getHeight(null)) / 2;
                        g.drawImage(info.getIcon(), offsetX, offsetY, null);
                        g.dispose();
                    }

                    JToggleButton button = new JToggleButton(new ImageIcon(im));
                    m_toolBar.add(button);
                    button.addActionListener(new ToolButtonActionListener(toolId));
                    m_toolButtonGroup.add(button);
                    m_toolButtons[toolId] = button;

                    String actionId = "tool" + toolId + "Action";
                    m_gametableCanvas.getActionMap().put(actionId, new ToolButtonAbstractAction(toolId));
                    KeyStroke keystroke = KeyStroke.getKeyStroke(info.getQuickKey(), 0, false);
                    m_gametableCanvas.getInputMap().put(keystroke, actionId);
                    button.setToolTipText(info.getName());
                    List prefs = info.getTool().getPreferences();
                    for (int i = 0; i < prefs.size(); i++)
                    {
                        m_preferences.addPreference((PreferenceDescriptor)prefs.get(i));
                    }
                }
            }
            catch (IOException ioe)
            {
                Log.log(Log.SYS, "Failure initializing tools.");
                Log.log(Log.SYS, ioe);
            }

            // add tile combo here.

            m_menuBar.add(m_fileMenu);
            m_fileMenu.add(m_openMenuItem);
            m_fileMenu.add(m_saveMenuItem);
            m_fileMenu.add(m_saveAsMenuItem);
            m_fileMenu.add(m_reacquirePogsMenuItem);
            m_fileMenu.add(m_exitMenuItem);

            m_gridModeMenu.add(m_noGridModeMenuItem);
            m_gridModeMenu.add(m_squareGridModeMenuItem);
            m_gridModeMenu.add(m_hexGridModeMenuItem);

            m_menuBar.add(m_editMenu);
            m_editMenu.add(m_undoMenuItem);
            m_editMenu.add(m_redoMenuItem);

            m_menuBar.add(m_networkMenu);
            m_menuBar.add(m_mapMenu);
            m_mapMenu.add(m_clearMapMenuItem);
            m_mapMenu.add(m_recenterMapMenuItem);
            m_mapMenu.add(m_gridModeMenu);
            m_mapMenu.add(m_privateLayerModeMenuItem);
            m_menuBar.add(m_diceMacrosMenu);
            m_menuBar.add(m_helpMenu);
            m_helpMenu.add(m_aboutMenuItem);
            m_textAndEntryPanel.add(m_textEntry, BorderLayout.SOUTH);
            m_textAndEntryPanel.add(m_textLogScroller, BorderLayout.CENTER);
            m_contentPane.add(m_toolBar, BorderLayout.NORTH);
            m_textLogScroller.getViewport().add(m_textLog, null);
            m_textLog.addKeyListener(m_gametableCanvas);

            m_pogLibrary = new PogLibrary();
            m_gametableCanvas.init(this);
            addKeyListener(m_gametableCanvas);

            m_pogsPanel = new PogsPanel(m_pogLibrary.getChild("pogs"), m_gametableCanvas);
            m_underlaysPanel = new PogsPanel(m_pogLibrary.getChild("underlays"), m_gametableCanvas);
            m_pogPanel = new PogPanel(m_pogLibrary, m_gametableCanvas);
            m_pogsTabbedPane.add(m_pogPanel, "Tree");
            m_pogsTabbedPane.add(new JScrollPane(m_pogsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), "Pogs");
            m_pogsTabbedPane.add(new JScrollPane(m_underlaysPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), "Underlays");

            m_propertiesArea.add(m_textAreaPanel, BorderLayout.CENTER);
            m_textAreaPanel.add(m_macrosScrollPane, BorderLayout.WEST);
            m_macrosScrollPane.setViewportView(m_macroButtonsPanel);
            m_macrosScrollPane
                .setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 5), m_macrosScrollPane.getBorder()));
            m_textAreaPanel.add(m_textAndEntryPanel, BorderLayout.CENTER);
            m_mapChatSplitPane.addPropertyChangeListener(this);
            m_mapChatSplitPane.add(m_gametableCanvas, JSplitPane.TOP);
            m_mapChatSplitPane.add(m_propertiesArea, JSplitPane.BOTTOM);

            m_mapPogSplitPane.add(m_pogsTabbedPane, JSplitPane.LEFT);
            m_mapPogSplitPane.add(m_mapChatSplitPane, JSplitPane.RIGHT);
            m_contentPane.add(m_mapPogSplitPane, BorderLayout.CENTER);

            m_disconnectMenuItem.setEnabled(false);

            ColorComboCellRenderer renderer = new ColorComboCellRenderer();
            m_colorCombo.setRenderer(renderer);

            m_myPlayerIdx = 0;

            m_textLog.setEditable(false);
            m_gametableCanvas.requestFocus();
            m_networkMenu.add(m_listPlayersMenuItem);
            m_networkMenu.add(m_hostMenuItem);
            m_networkMenu.add(m_joinMenuItem);
            m_networkMenu.add(m_disconnectMenuItem);
            m_diceMacrosMenu.add(m_addDiceMacroMenuItem);
            m_diceMacrosMenu.add(m_removeDiceMacroMenuItem);

            addMacro("d20", "d20");

            // load the primary map
            m_gametableCanvas.setActiveMap(m_gametableCanvas.getPrivateMap());
            PacketSourceState.beginFileLoad();
            loadState(new File("autosavepvt.grm"));
            PacketSourceState.endFileLoad();

            m_gametableCanvas.setActiveMap(m_gametableCanvas.getPublicMap());
            loadState(new File("autosave.grm"));
            loadPrefs();

            addPlayer(new Player(m_defaultName, m_defaultCharName, -1));

            m_textEntry.addKeyListener(this);
            m_colorCombo.addActionListener(this);
            updateGridModeMenu();
            m_bInitted = true;
            setJMenuBar(m_menuBar);

            updatePrivateLayerModeMenuItem();
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
        }
    }

    public static GametableFrame getGametableFrame()
    {
        return g_gameTableFrame;
    }

    public int getNewStateID()
    {
        int ret = m_nextStateID;
        m_nextStateID++;
        return ret;
    }

    public Preferences getPreferences()
    {
        return m_preferences;
    }

    public PogLibrary getPogLibrary()
    {
        return m_pogLibrary;
    }

    public void updateWindowInfo()
    {
        // we only update our internal size and
        // position variables if we aren't maximized.
        if ((getExtendedState() & MAXIMIZED_BOTH) != 0)
        {
            m_bMaximized = true;
        }
        else
        {
            m_bMaximized = false;
            m_windowSize = getSize();
            m_windowPos = getLocation();
        }
    }

    // interface overrides
    public void componentResized(ComponentEvent event)
    {
        updateWindowInfo();
    }

    public Player getMePlayer()
    {
        return (Player)m_players.get(m_myPlayerIdx);
    }

    // returns the player id of this client
    public int getMeID()
    {
        return getMePlayer().getId();
    }

    public void componentMoved(ComponentEvent e)
    {
        updateWindowInfo();
    }

    public void componentShown(ComponentEvent e)
    {
    }

    public void componentHidden(ComponentEvent e)
    {
    }

    public void loginCompletePacketReceived()
    {
        // this packet is never redistributed.
        // all we do in response to this allow pog text
        // highlights. The pogs don't know the difference between
        // inital data and actual player changes.
        PacketSourceState.endHostDump();

        // seed our undo stack with this as the bottom rung
        m_gametableCanvas.getPublicMap().beginUndoableAction();
        m_gametableCanvas.getPublicMap().endUndoableAction(-1, -1);
    }

    public void pingPacketReceived()
    {
        // do nothing for now
    }

    public void rejectPacketReceived(int reason)
    {
        confirmJoined();

        // you got rejected!
        switch (reason)
        {
            case REJECT_INVALID_PASSWORD:
            {
                logSystemMessage("Invalid Password. Connection refused.");
            }
            break;

            case REJECT_VERSION_MISMATCH:
            {
                logSystemMessage("The host is running a different comm-version of Gametable. Connection refused.");
            }
            break;
        }
        disconnect();
    }

    public void recenterPacketReceived(int x, int y, int zoom)
    {
        m_gametableCanvas.doRecenterView(x, y, zoom);

        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makeRecenterPacket(x, y, zoom));
        }
    }

    public void pogDataPacketReceived(int id, String s)
    {
        // when you get a pog data change, the pog will show that change
        // to you for a while
        Pog pog = m_gametableCanvas.getPogByID(id);
        if (pog != null)
        {
            pog.displayPogDataChange();
        }

        m_gametableCanvas.doSetPogData(id, s);

        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makePogDataPacket(id, s));
        }
    }

    public void grmPacketReceived(byte grmFile[])
    {
        // only the host should ever get this packet. If a joiner gets it
        // for some reason, it should ignore it.
        // if we're offline, then sure, go ahead and load
        if (m_netStatus != NETSTATE_JOINED)
        {
            loadStateFromRawFileData(grmFile);
        }
    }

    public void undoPacketReceived(int stateID)
    {
        m_gametableCanvas.doUndo(stateID);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeUndoPacket(stateID));
        }

        repaint();
    }

    public void redoPacketReceived(int stateID)
    {
        m_gametableCanvas.doRedo(stateID);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeRedoPacket(stateID));
        }

        repaint();
    }

    public void pointPacketReceived(int plrIdx, int x, int y, boolean bPointing)
    {
        // we're not interested in point packets of our own hand
        if (plrIdx != m_myPlayerIdx)
        {
            Player plr = (Player)m_players.get(plrIdx);
            plr.setPoint(x, y);
            plr.setPointing(bPointing);
        }

        if (m_netStatus == NETSTATE_HOST)
        {
            send(PacketManager.makePointPacket(plrIdx, x, y, bPointing));
        }

        m_gametableCanvas.repaint();
    }

    public void movePogPacketReceived(int id, int newX, int newY)
    {
        m_gametableCanvas.doMovePog(id, newX, newY);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeMovePogPacket(id, newX, newY));
        }
    }

    public void removePogsPacketReceived(int ids[])
    {
        m_gametableCanvas.doRemovePogs(ids);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeRemovePogsPacket(ids));
        }
    }

    public void addPogPacketReceived(Pog pog)
    {
        m_gametableCanvas.doAddPog(pog);

        // update the next pog id if necessary
        if (pog.getId() >= Pog.g_nextID)
        {
            Pog.g_nextID = pog.getId() + 5;
        }

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeAddPogPacket(pog));
        }
    }

    public void erasePacketReceived(Rectangle r, boolean bColorSpecific, int color, int authorID, int stateID)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            // and give it a genuine state ID first
            stateID = this.getNewStateID();
            send(PacketManager.makeErasePacket(r, bColorSpecific, color, authorID, stateID));
        }

        // erase the lines
        m_gametableCanvas.doErase(r, bColorSpecific, color, authorID, stateID);
    }

    public void linesPacketReceived(LineSegment[] lines, int authorID, int stateID)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            // and give it a genuine state ID first
            stateID = this.getNewStateID();
            send(PacketManager.makeLinesPacket(lines, authorID, stateID));
        }

        // add the lines to the array
        m_gametableCanvas.doAddLineSegments(lines, authorID, stateID);
    }

    public void textPacketReceived(String text)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            // if you're the host, push to all players
            postMessage(text);
        }
        else
        {
            // otherwise, just add it
            logMessage(text);
        }
    }

    public void gridModePacketReceived(int gridMode)
    {
        // note the new grid mode
        m_gametableCanvas.setGridModeByID(gridMode);
        updateGridModeMenu();

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeGridModePacket(gridMode));
        }

        repaint();
    }

    public int getPlayerIdx(Player plr)
    {
        return m_players.indexOf(plr);
    }

    public Player getPlayerFromConnection(Connection conn)
    {
        for (int i = 0; i < m_players.size(); i++)
        {
            Player plr = (Player)m_players.get(i);
            if (conn == plr.getConnection())
            {
                return plr;
            }
        }

        return null;
    }

    public void connectionDropped(Connection conn)
    {
        if (m_netStatus == NETSTATE_JOINED)
        {
            // we lost our connection to the host
            logSystemMessage("Your connection to the host was lost.");
            disconnect();

            m_netStatus = NETSTATE_NONE;
            return;
        }

        // find the player who owns that connection
        Player dead = getPlayerFromConnection(conn);
        if (dead != null)
        {
            // remove this player
            m_players.remove(dead);
            sendCastInfo();
            postSystemMessage("" + dead.getPlayerName() + " has left the session");
        }
        else
        {
            postSystemMessage("Someone tried to log in, but was rejected.");
        }
    }

    public void confirmHost()
    {
        if (m_netStatus != NETSTATE_HOST)
        {
            throw new IllegalStateException("confirmHost failure");
        }
    }

    public void confirmJoined()
    {
        if (m_netStatus != NETSTATE_JOINED)
        {
            throw new IllegalStateException("confirmJoined failure");
        }
    }

    public boolean hostDlg()
    {
        JoinDialog dlg = new JoinDialog();
        dlg.setModal(true);
        dlg.setUpForHostDlg();
        dlg.setVisible(true);

        if (!dlg.m_bAccepted)
        {
            // they cancelled out
            return false;
        }
        return true;
    }

    public boolean inputConnectionIP()
    {
        JoinDialog dlg = new JoinDialog();
        dlg.setModal(true);
        dlg.setVisible(true);

        if (!dlg.m_bAccepted)
        {
            // they cancelled out
            return false;
        }
        return true;
    }

    public void updateCast(Player[] players, int ourIdx)
    {
        // you should only get this if you're a joiner
        confirmJoined();

        // set up the current cast
        m_players = new ArrayList();
        for (int i = 0; i < players.length; i++)
        {
            addPlayer(players[i]);
        }

        m_myPlayerIdx = ourIdx;

        // any time the cast changes, all the undo stacks clear
        m_gametableCanvas.clearUndoStacks();
    }

    public void send(byte[] packet)
    {
        if (m_networkThread != null)
        {
            m_networkThread.send(packet);
        }
    }

    public void send(byte[] packet, Connection connection)
    {
        if (m_networkThread != null)
        {
            m_networkThread.send(packet, connection);
        }
    }

    public void send(byte[] packet, Player player)
    {
        if (player.getConnection() == null)
        {
            return;
        }
        send(packet, player.getConnection());
    }

    public void kick(Connection conn, int reason)
    {
        send(PacketManager.makeRejectPacket(reason), conn);
        conn.close();
    }

    public void playerJoined(Connection connection, Player player, String password)
    {
        confirmHost();

        if (!m_defaultPassword.equals(password))
        {
            // rejected!
            kick(connection, REJECT_INVALID_PASSWORD);
            return;
        }

        // now we can associate a player with the connection
        connection.markLoggedIn();
        player.setConnection(connection);

        // set their ID
        player.setId(m_nextPlayerID);
        m_nextPlayerID++;

        // tell everyone about the new guy
        postSystemMessage(player.getPlayerName() + " has joined the session");
        addPlayer(player);

        sendCastInfo();

        // all the undo stacks clear
        m_gametableCanvas.clearUndoStacks();

        // tell the new guy the entire state of the game
        // lines
        LineSegment[] lines = new LineSegment[m_gametableCanvas.getPublicMap().getNumLines()];
        for (int i = 0; i < m_gametableCanvas.getPublicMap().getNumLines(); i++)
        {
            lines[i] = m_gametableCanvas.getPublicMap().getLineAt(i);
        }
        send(PacketManager.makeLinesPacket(lines, -1, -1), player);

        // pogs
        for (int i = 0; i < m_gametableCanvas.getPublicMap().getNumPogs(); i++)
        {
            Pog pog = m_gametableCanvas.getPublicMap().getPogAt(i);
            send(PacketManager.makeAddPogPacket(pog), player);
        }

        // let them know we're done sending them data from the login
        send(PacketManager.makeLoginCompletePacket(), player);

        // finally, have the player recenter on the host's view
        int viewCenterX = m_gametableCanvas.getWidth() / 2;
        int viewCenterY = m_gametableCanvas.getHeight() / 2;

        // convert to model coordinates
        Point modelCenter = m_gametableCanvas.viewToModel(viewCenterX, viewCenterY);
        send(PacketManager.makeRecenterPacket(modelCenter.x, modelCenter.y, m_gametableCanvas.m_zoom), player);
    }

    public void sendCastInfo()
    {
        // and we have to push this data out to everyone
        for (int i = 0; i < m_players.size(); i++)
        {
            Player recipient = (Player)m_players.get(i);
            byte[] castPacket = PacketManager.makeCastPacket(recipient);
            send(castPacket, recipient);
        }
    }

    public void packetReceived(Connection conn, byte[] packet)
    {
        // synch here. after we get the packet, but before we process it.
        // this is all the synching we need on the comm end of things.
        // we will also need to synch every user entry point
        PacketManager.readPacket(conn, packet);
    }

    public void host()
    {
        host(false);
    }

    public void host(boolean force)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            logSystemMessage("You are already hosting.");
            return;
        }
        if (m_netStatus == NETSTATE_JOINED)
        {
            logSystemMessage("You can not host until you disconnect from the game you joined.");
            return;
        }

        if (!force)
        {
            // get relevant infor from the user
            if (!hostDlg())
            {
                return;
            }
        }

        // clear out all players
        m_nextPlayerID = 0;
        m_players = new ArrayList();
        Player me = new Player(m_defaultName, m_defaultCharName, m_nextPlayerID); // this means the host is always
        // player 0
        m_nextPlayerID++;
        m_players.add(me);
        me.setHostPlayer(true);
        m_myPlayerIdx = 0;

        m_networkThread = new NetworkThread(m_defaultPort);
        m_networkThread.start();
        // TODO: fix hosting failure detection

        initializeExecutorThread();
        m_netStatus = NETSTATE_HOST;
        String message = "Hosting on port: " + m_defaultPort;
        logSystemMessage(message);
        Log.log(Log.NET, message);

        m_hostMenuItem.setEnabled(false);
        m_joinMenuItem.setEnabled(false);
        m_disconnectMenuItem.setEnabled(true);
        setTitle(GametableApp.VERSION + " - " + me.getCharacterName());

        // when you host, all the undo stacks clear
        m_gametableCanvas.clearUndoStacks();
    }

    public void hostThreadFailed()
    {
        logAlertMessage("Failed to host.");
        m_networkThread.interrupt();
        m_networkThread = null;
        disconnect();
    }

    public void join()
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            logSystemMessage("You are hosting. If you wish to join a game, disconnect first.");
            return;
        }
        if (m_netStatus == NETSTATE_JOINED)
        {
            logSystemMessage("You are already in a game. You must disconnect before joining another.");
            return;
        }

        boolean res = inputConnectionIP();
        if (!res)
        {
            // they cancelled out
            return;
        }

        // now we have the ip to connect to. Try to connect to it
        try
        {
            m_networkThread = new NetworkThread();
            m_networkThread.start();
            Connection conn = new Connection(m_defaultIP, m_defaultPort);
            m_networkThread.add(conn);

            // now that we've successfully made a connection, let the host know
            // who we are
            m_players = new ArrayList();
            Player me = new Player(m_defaultName, m_defaultCharName, -1);
            me.setConnection(conn);
            m_players.add(me);
            m_myPlayerIdx = 0;

            // reset game data
            m_gametableCanvas.getPublicMap().setScroll(0, 0);
            m_gametableCanvas.getPublicMap().clearPogs();
            m_gametableCanvas.getPublicMap().clearLines();
            // PacketManager.g_imagelessPogs.clear();

            // send the packet
            while (!conn.isConnected())
            {
            }
            conn.sendPacket(PacketManager.makePlayerPacket(me, m_defaultPassword));

            PacketSourceState.beginHostDump();

            // and now we're ready to pay attention
            m_netStatus = NETSTATE_JOINED;
            initializeExecutorThread();

            logSystemMessage("Joined game");

            m_hostMenuItem.setEnabled(false);
            m_joinMenuItem.setEnabled(false);
            m_disconnectMenuItem.setEnabled(true);
            setTitle(GametableApp.VERSION + " - " + me.getCharacterName());
        }
        catch (Exception ex)
        {
            Log.log(Log.SYS, ex);
            logSystemMessage("Failed to connect.");
            setTitle(GametableApp.VERSION);
            PacketSourceState.endHostDump();
        }
    }

    public void disconnect()
    {
        if (m_netStatus == NETSTATE_NONE)
        {
            logSystemMessage("Nothing to disconnect from.");
            return;
        }

        if (m_executorThread != null)
        {
            m_executorThread.interrupt();
            m_executorThread = null;
        }
        if (m_networkThread != null)
        {
            m_networkThread.interrupt();
            m_networkThread = null;
        }

        m_hostMenuItem.setEnabled(true);
        m_joinMenuItem.setEnabled(true);
        m_disconnectMenuItem.setEnabled(false);

        m_players = new ArrayList();
        Player me = new Player(m_defaultName, m_defaultCharName, -1);
        m_players.add(me);
        m_myPlayerIdx = 0;
        setTitle(GametableApp.VERSION);

        // we might have disconnected during inital data recipt
        PacketSourceState.endHostDump();

        m_netStatus = NETSTATE_NONE;
        logSystemMessage("Disconnected.");
    }

    public void eraseAllLines()
    {
        // erase with a rect big enough to nail everything
        Rectangle toErase = new Rectangle();

        toErase.x = Integer.MIN_VALUE / 2;
        toErase.y = Integer.MIN_VALUE / 2;
        toErase.width = Integer.MAX_VALUE;
        toErase.height = Integer.MAX_VALUE;

        // go to town
        m_gametableCanvas.erase(toErase, false, 0);

        repaint();
    }

    public void eraseAllPogs()
    {
        // make an int array of all the IDs
        int removeArray[] = new int[m_gametableCanvas.getActiveMap().getNumPogs()];

        for (int i = 0; i < m_gametableCanvas.getActiveMap().getNumPogs(); i++)
        {
            Pog pog = m_gametableCanvas.getActiveMap().getPogAt(i);
            removeArray[i] = pog.getId();
        }

        m_gametableCanvas.removePogs(removeArray);
    }

    public void eraseAll()
    {
        eraseAllLines();
        eraseAllPogs();
    }

    public void updateGridModeMenu()
    {
        if (m_gametableCanvas.m_gridMode == m_gametableCanvas.m_noGridMode)
        {
            m_noGridModeMenuItem.setState(true);
            m_squareGridModeMenuItem.setState(false);
            m_hexGridModeMenuItem.setState(false);
        }
        else if (m_gametableCanvas.m_gridMode == m_gametableCanvas.m_squareGridMode)
        {
            m_noGridModeMenuItem.setState(false);
            m_squareGridModeMenuItem.setState(true);
            m_hexGridModeMenuItem.setState(false);
        }
        else if (m_gametableCanvas.m_gridMode == m_gametableCanvas.m_hexGridMode)
        {
            m_noGridModeMenuItem.setState(false);
            m_squareGridModeMenuItem.setState(false);
            m_hexGridModeMenuItem.setState(true);
        }
    }

    public void updatePrivateLayerModeMenuItem()
    {
        // note the tool ID of the publish tool
        int toolId = m_toolManager.getToolInfo("Publish").getId();

        if (m_gametableCanvas.isPublicMap())
        {
            m_privateLayerModeMenuItem.setState(false);
            m_toolButtons[toolId].setEnabled(false);
        }
        else
        {
            m_privateLayerModeMenuItem.setState(true);
            m_toolButtons[toolId].setEnabled(true);
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == m_clearMapMenuItem)
        {
            int res = UtilityFunctions.yesNoDialog(this,
                "This will clear all lines, pogs, and underlays on the entire layer. Are you sure?", "Clear Map");
            if (res == UtilityFunctions.YES)
            {
                eraseAllPogs();
                eraseAllLines();
                repaint();
            }
        }

        if (e.getSource() == m_colorCombo)
        {
            Integer col = (Integer)m_colorCombo.getSelectedItem();
            m_drawColor = new Color(col.intValue());
        }

        if (e.getSource() == m_undoMenuItem)
        {
            m_gametableCanvas.undo();
        }

        if (e.getSource() == m_redoMenuItem)
        {
            m_gametableCanvas.redo();
        }

        if (e.getSource() == m_exitMenuItem)
        {
            saveState(m_gametableCanvas.getPublicMap(), new File("autosave.grm"));
            saveState(m_gametableCanvas.getPrivateMap(), new File("autosavepvt.grm"));
            savePrefs();
            System.exit(0);
        }
        if (e.getSource() == m_openMenuItem)
        {
            // opening while on the public layer...
            if (m_gametableCanvas.getActiveMap() == m_gametableCanvas.getPublicMap())
            {
                File openFile = UtilityFunctions.doFileOpenDialog("Open", "grm", true);

                if (openFile == null)
                {
                    // they cancelled out of the open
                    return;
                }

                m_actingFilePublic = openFile;

                int result = UtilityFunctions
                    .yesNoDialog(
                        this,
                        "This will load a map file, replacing all existing map data for you and all players in the session. Are you sure you want to do this?",
                        "Confirm Load");
                if (result == UtilityFunctions.YES)
                {

                    if (m_actingFilePublic != null)
                    {
                        // clear the state
                        eraseAll();

                        // load
                        if (m_netStatus == NETSTATE_JOINED)
                        {
                            // joiners dispatch the save file to the host
                            // for processing
                            byte grmFile[] = UtilityFunctions.loadFileToArray(m_actingFilePublic);
                            if (grmFile != null)
                            {
                                send(PacketManager.makeGrmPacket(grmFile));
                            }
                        }
                        else
                        {
                            // actually do the load if we're the host or offline
                            loadState(m_actingFilePublic);
                        }

                        postSystemMessage(getMePlayer().getPlayerName() + " loads a new map.");
                    }
                }
            }
            else
            {
                // opening while on the private layer
                m_actingFilePrivate = UtilityFunctions.doFileOpenDialog("Open", "grm", true);
                if (m_actingFilePrivate != null)
                {
                    // we have to pretend we're not connected while loading. We
                    // don't want these packets to be propagatet to other players
                    int oldStatus = m_netStatus;
                    m_netStatus = NETSTATE_NONE;
                    PacketSourceState.beginFileLoad();
                    loadState(m_actingFilePrivate);
                    PacketSourceState.endFileLoad();
                    m_netStatus = oldStatus;
                }
            }
        }
        if (e.getSource() == m_saveMenuItem)
        {
            if (m_gametableCanvas.isPublicMap())
            {
                if (m_actingFilePublic == null)
                {
                    m_actingFilePublic = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
                }

                if (m_actingFilePublic != null)
                {
                    // save the file
                    saveState(m_gametableCanvas.getActiveMap(), m_actingFilePublic);
                }
            }
            else
            {
                if (m_actingFilePrivate == null)
                {
                    m_actingFilePrivate = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
                }

                if (m_actingFilePrivate != null)
                {
                    // save the file
                    saveState(m_gametableCanvas.getActiveMap(), m_actingFilePrivate);
                }
            }

        }
        if (e.getSource() == m_saveAsMenuItem)
        {
            if (m_gametableCanvas.isPublicMap())
            {
                m_actingFilePublic = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
                if (m_actingFilePublic != null)
                {
                    // save the file
                    saveState(m_gametableCanvas.getActiveMap(), m_actingFilePublic);
                }
            }
            else
            {
                m_actingFilePrivate = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
                if (m_actingFilePrivate != null)
                {
                    // save the file
                    saveState(m_gametableCanvas.getActiveMap(), m_actingFilePrivate);
                }
            }
        }
        if (e.getSource() == m_reacquirePogsMenuItem)
        {
            reacquirePogs();
        }

        if (e.getSource() == m_hostMenuItem)
        {
            host();
        }
        if (e.getSource() == m_joinMenuItem)
        {
            join();
        }
        if (e.getSource() == m_listPlayersMenuItem)
        {
            parseSlashCommand("/who");
        }
        if (e.getSource() == m_disconnectMenuItem)
        {
            disconnect();
        }
        if (e.getSource() == m_aboutMenuItem)
        {
            UtilityFunctions.msgBox(this, GametableApp.VERSION + " by Andy Weir and David Ghandehari", "Version");
        }

        if (e.getSource() == m_noGridModeMenuItem)
        {
            m_gametableCanvas.m_gridMode = m_gametableCanvas.m_noGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_NONE));
            updateGridModeMenu();
            repaint();
            postSystemMessage(getMePlayer().getPlayerName() + " changes the grid mode.");
        }
        if (e.getSource() == m_squareGridModeMenuItem)
        {
            m_gametableCanvas.m_gridMode = m_gametableCanvas.m_squareGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_SQUARES));
            updateGridModeMenu();
            repaint();
            postSystemMessage(getMePlayer().getPlayerName() + " changes the grid mode.");
        }
        if (e.getSource() == m_hexGridModeMenuItem)
        {
            m_gametableCanvas.m_gridMode = m_gametableCanvas.m_hexGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_HEX));
            updateGridModeMenu();
            repaint();
            postSystemMessage(getMePlayer().getPlayerName() + " changes the grid mode.");
        }

        if (e.getSource() == m_privateLayerModeMenuItem)
        {
            toggleLayer();
        }

        if (e.getSource() == m_recenterMapMenuItem)
        {
            int result = UtilityFunctions.yesNoDialog(this, "This will recenter everyone's map view to match yours, "
                + "and will set their zoom levels to match yours. Are you sure you want to do this?", "Recenter?");
            if (result == UtilityFunctions.YES)
            {
                // get our view center
                int viewCenterX = m_gametableCanvas.getWidth() / 2;
                int viewCenterY = m_gametableCanvas.getHeight() / 2;

                // convert to model coordinates
                Point modelCenter = m_gametableCanvas.viewToModel(viewCenterX, viewCenterY);
                m_gametableCanvas.recenterView(modelCenter.x, modelCenter.y, m_gametableCanvas.m_zoom);
                postSystemMessage(getMePlayer().getPlayerName() + " Recenters everyone's view!");
            }
        }

        if (e.getSource() == m_addDiceMacroMenuItem)
        {
            NewMacroDialog dlg = new NewMacroDialog();
            dlg.setModal(true);
            dlg.setVisible(true);

            if (dlg.m_bAccepted)
            {
                // extrace the macro from the controls and add it
                String name = dlg.m_nameEntry.getText();
                String macro = dlg.m_rollEntry.getText();
                if (getMacro(name) != null)
                {
                    int result = UtilityFunctions.yesNoDialog(this, "You already have a macro named \"" + name + "\", "
                        + "are you sure you want to replace it with \"" + macro + "\"?", "Replace Macro?");
                    if (result == UtilityFunctions.YES)
                    {
                        addMacro(name, macro);
                    }
                }
                else
                {
                    addMacro(name, macro);
                }
            }
        }

        if (e.getSource() == m_removeDiceMacroMenuItem)
        {
            Object[] list = m_macros.toArray();
            // give them a list of macros they can delete
            Object sel = JOptionPane.showInputDialog(this, "Select Dice Macro to remove:", "Remove Dice Macro",
                JOptionPane.PLAIN_MESSAGE, null, list, list[0]);
            if (sel != null)
            {
                removeMacro((DiceMacro)sel);
            }
        }

        // check the macro buttons
        for (int i = 0; i < m_macroButtons.size(); i++)
        {
            if (e.getSource() == m_macroButtons.get(i))
            {
                // found our man
                DiceMacro macro = (DiceMacro)m_macros.get(i);
                String result = macro.doMacro();
                postMessage(result);

                // send focus back where it belongs
                m_gametableCanvas.requestFocus();
            }
        }
    }

    /**
     * Reacquires pogs and then refreshes the pog list.
     */
    public void reacquirePogs()
    {
        m_pogLibrary.acquirePogs();
        refreshPogList();
    }

    /**
     * Reacquires pogs and then refreshes the pog list.
     */
    public void refreshPogList()
    {
        m_pogsPanel.populateChildren();
        m_underlaysPanel.populateChildren();
        m_pogPanel.populateChildren();
        m_gametableCanvas.repaint();
    }

    public void toggleLayer()
    {
        // toggle the map we're on
        if (m_gametableCanvas.isPublicMap())
        {
            m_gametableCanvas.setActiveMap(m_gametableCanvas.getPrivateMap());
        }
        else
        {
            m_gametableCanvas.setActiveMap(m_gametableCanvas.getPublicMap());
        }

        updatePrivateLayerModeMenuItem();

        // if they toggled the layer, whatever tool they're using is cancelled
        getToolManager().cancelToolAction();

        repaint();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals("dividerLocation"))
        {
            if (m_bDisregardDividerChanges)
            {
                return;
            }

            // note the new divider location
            int fromBottom = m_mapChatSplitPane.getHeight() - m_mapChatSplitPane.getDividerLocation();
            if (fromBottom == 16)
            {
                m_bDisregardDividerChanges = true;
                m_mapChatSplitPane.setDividerLocation(getHeight() - m_prevDividerLocFromBottom);
                m_bDisregardDividerChanges = false;
                return;
            }
            m_prevDividerLocFromBottom = fromBottom;
        }
    }

    public void paint(Graphics g)
    {
        if (m_bFirstPaint)
        {
            if (!m_bLoadedState)
            {
                m_mapChatSplitPane.setDividerLocation(0.7);
                m_prevDividerLocFromBottom = m_mapChatSplitPane.getHeight() - m_mapChatSplitPane.getDividerLocation();
            }

            m_bFirstPaint = false;
            m_bDisregardDividerChanges = false;
            m_gametableCanvas.requestFocus();

            repaint();
        }
        super.paint(g);
    }

    public ToolManager getToolManager()
    {
        return m_toolManager;
    }

    public void setToolSelected(int toolId)
    {
        m_toolButtons[toolId].setSelected(true);
    }

    public void addPlayer(Object player)
    {
        m_players.add(player);
    }

    public void addMacro(String name, String macro)
    {
        DiceMacro newMacro = new DiceMacro();
        boolean res = newMacro.init(macro, name);
        if (!res)
        {
            logSystemMessage("Error in macro");
            return;
        }
        addMacro(newMacro);
    }

    public void addMacro(DiceMacro dm)
    {
        addMacroForced(dm);
        rebuildMacroButtons();
    }

    public void removeMacro(String name)
    {
        removeMacroForced(name);
        rebuildMacroButtons();
    }

    public void removeMacro(DiceMacro dm)
    {
        removeMacroForced(dm);
        rebuildMacroButtons();
    }

    private void removeMacroForced(String name)
    {
        DiceMacro macro = getMacro(name);
        if (macro != null)
        {
            removeMacroForced(macro);
        }
    }

    private void removeMacroForced(DiceMacro macro)
    {
        String name = UtilityFunctions.normalizeName(macro.getName());
        m_macroMap.remove(name);
        m_macros.remove(macro);
    }

    private void addMacroForced(DiceMacro macro)
    {
        removeMacroForced(macro.getName());
        m_macros.add(macro);
        m_macroMap.put(UtilityFunctions.normalizeName(macro.getName()), macro);
    }

    public DiceMacro findMacro(String term)
    {
        String name = UtilityFunctions.normalizeName(term);
        DiceMacro macro = getMacro(name);
        if (macro == null)
        {
            macro = new DiceMacro();
            if (!macro.init(term, null))
            {
                macro = null;
            }
        }

        return macro;
    }

    public DiceMacro getMacro(String name)
    {
        String realName = UtilityFunctions.normalizeName(name);
        return (DiceMacro)m_macroMap.get(realName);
    }

    public void rebuildMacroButtons()
    {
        m_macroButtonsPanel.removeAll();
        m_macroButtons = new ArrayList();

        for (int i = 0; i < m_macros.size(); i++)
        {
            DiceMacro dm = (DiceMacro)m_macros.get(i);

            JButton newButton = new JButton();
            newButton.setMaximumSize(new Dimension(120, 20));
            newButton.setMinimumSize(new Dimension(120, 20));
            newButton.setPreferredSize(new Dimension(120, 20));
            newButton.setText(dm.getName());
            newButton.addActionListener(this);
            newButton.setToolTipText(dm.toString());

            m_macroButtons.add(newButton);
            m_macroButtonsPanel.add(newButton);
        }

        m_macrosScrollPane.revalidate();

        repaint();
    }

    public void logSystemMessage(String text)
    {
        logMessage(">>> " + text);
    }

    public void logAlertMessage(String text)
    {
        logMessage("!!! " + text);
    }

    public void logMessage(String text)
    {
        double prevHeight = m_textLog.getPreferredSize().getHeight();
        String log = m_textLog.getText();
        log += text;
        log += "\n";
        m_textLog.setText(log);
        double scrollerHeight = m_textLogScroller.getHeight();

        if (prevHeight > scrollerHeight)
        {
            m_textLogScroller.disregardNextPaint();
        }
    }

    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            saveState(m_gametableCanvas.getPublicMap(), new File("autosave.grm"));
            saveState(m_gametableCanvas.getPrivateMap(), new File("autosavepvt.grm"));
            savePrefs();
            System.exit(0);
        }
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
        if (e.getSource() == m_textEntry)
        {
            int code = e.getKeyCode();

            if (code == KeyEvent.VK_UP)
            {
                m_textSentLoc--;
                if (m_textSentLoc < 0)
                {
                    m_textSentLoc = 0;
                }
                else
                {
                    m_textEntry.setText((String)m_textSent.get(m_textSentLoc));
                }
            }

            if (code == KeyEvent.VK_DOWN)
            {
                m_textSentLoc++;

                if (m_textSentLoc > m_textSent.size())
                {
                    m_textSentLoc = m_textSent.size();
                }
                else
                {
                    if (m_textSentLoc == m_textSent.size())
                    {
                        m_textEntry.setText("");
                    }
                    else
                    {
                        m_textEntry.setText((String)m_textSent.get(m_textSentLoc));
                    }
                }
            }
        }
    }

    public void keyReleased(KeyEvent e)
    {
    }

    void m_textEntry_actionPerformed(ActionEvent e)
    {
        // they hit return on the text bar
        String entered = m_textEntry.getText();
        if (entered.length() == 0)
        {
            // useless string.
            // return focus to the map
            m_gametableCanvas.requestFocus();
            return;
        }

        m_textSent.add(entered);
        m_textSentLoc = m_textSent.size();

        // parse for commands
        if (entered.charAt(0) == '/')
        {
            parseSlashCommand(entered);
        }
        else
        {
            postMessage(getMePlayer().getCharacterName() + "> " + entered);
        }

        m_textEntry.setText("");
    }

    public void parseSlashCommand(String text)
    {
        // get the command
        String[] words = UtilityFunctions.breakIntoWords(text);
        if (words == null)
        {
            return;
        }
        else if (words[0].equals("/macro"))
        {
            // macro command. this requires at least 2 parameters
            if (words.length < 3)
            {
                // tell them the usage and bail
                logSystemMessage("/macro usage: /macro <macroName> <dice roll in standard format>");
                logSystemMessage("Examples:");
                logSystemMessage("    /macro Attack d20+8");
                logSystemMessage("    /macro SneakDmg d4 + 2 + 4d6");
                logSystemMessage("Note: Macros will replace existing macros with the same name.");
                return;
            }

            // the second word is the name
            String name = words[1];

            // all subsequent "words" are the die roll macro
            addMacro(name, text.substring("/macro ".length() + name.length() + 1));
        }
        else if (words[0].equals("/macrodelete") || words[0].equals("/del"))
        {
            // req. 1 param
            if (words.length < 2)
            {
                logSystemMessage(words[0] + " usage: " + words[0] + " <macroName>");
                return;
            }

            // find and kill this macro
            removeMacro(words[1]);
        }
        else if (words[0].equals("/who"))
        {
            logSystemMessage("Who's connected:");
            for (int i = 0, size = m_players.size(); i < size; ++i)
            {
                Player player = (Player)m_players.get(i);
                logSystemMessage("   " + player.toString());
            }
            logSystemMessage(m_players.size() + " player" + (m_players.size() > 1 ? "s" : ""));
        }
        else if (words[0].equals("/roll"))
        {
            // req. 1 param
            if (words.length < 2)
            {
                logSystemMessage("/roll usage: /roll <Dice Roll in standard format>");
                logSystemMessage("or: /roll <Macro Name> [<+/-> <Macro Name or Dice Roll>]...");
                logSystemMessage("Examples:");
                logSystemMessage("Example: /roll 2d6 + 3d4 + 8");
                logSystemMessage("Example: /roll My Damage + d4");
                logSystemMessage("Example: /roll d20 + My Damage + My Damage Bonus");
                return;
            }

            // TODO: This should all probably be moved to DiceMacro somehow?

            // First we split the roll into terms
            ArrayList rolls = new ArrayList();
            ArrayList ops = new ArrayList();
            String remaining = text.substring("/roll ".length());
            int length = remaining.length();
            int termStart = 0;
            for (int index = 0; index < length; ++index)
            {
                char c = remaining.charAt(index);
                boolean isLast = (index == (length - 1));
                if (c == '+' || c == '-' || isLast)
                {
                    int termEnd = index + (isLast ? 1 : 0);
                    String term = remaining.substring(termStart, termEnd).trim();
                    if (term.length() < 1)
                    {
                        rolls.add(new DiceMacro());
                    }
                    else
                    {
                        DiceMacro macro = findMacro(term);
                        if (macro == null)
                        {
                            logSystemMessage("Invalid macro name or die term: " + term + ".");
                            return;
                        }

                        rolls.add(macro);
                    }

                    ops.add(String.valueOf(c));
                    termStart = index + 1;
                }
            }

            StringBuffer rollBuf = new StringBuffer();
            StringBuffer resultBuf = new StringBuffer();
            int total = 0;
            boolean first = true;
            for (int i = 0; i < rolls.size(); ++i)
            {
                DiceMacro macro = (DiceMacro)rolls.get(i);
                boolean negate = false;
                if (i > 0)
                {
                    if ("-".equals(ops.get(i - 1)))
                    {
                        negate = true;
                    }
                }

                DiceMacro.Result result = macro.roll();
                if (result == null)
                {
                    continue;
                }

                if (!negate)
                {
                    total += result.value;
                    if (!first)
                    {
                        rollBuf.append(" + ");
                        resultBuf.append(" + ");
                    }
                }
                else
                {
                    total -= result.value;
                    if (!first)
                    {
                        rollBuf.append(' ');
                        resultBuf.append(' ');
                    }

                    rollBuf.append("- ");
                    resultBuf.append("- ");
                }
                rollBuf.append(result.roll);
                if (macro.getName() != null && rolls.size() > 1)
                {
                    resultBuf.append('(');
                    resultBuf.append(result.result);
                    resultBuf.append(')');
                }
                else
                {
                    resultBuf.append(result.result);
                }

                first = false;
            }
            postMessage(getMePlayer().getCharacterName() + " rolls " + rollBuf + ": [" + resultBuf + "] = " + total);
        }
        else if (words[0].equals("//") || words[0].equals("/help"))
        {
            // list macro commands
            logSystemMessage("/macro: macro a die roll");
            logSystemMessage("/macrodelete: deletes an unwanted macro");
            logSystemMessage("/roll: roll dice");
            logSystemMessage("/who: lists connected players");
            logSystemMessage("// or /help: list all slash commands");
        }
    }

    public void postSystemMessage(String toSay)
    {
        postMessage(">>> " + toSay);
    }

    public void postAlertMessage(String toSay)
    {
        postMessage("!!! " + toSay);
    }

    public void postMessage(String toSay)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            // if you're the host, push to all players
            send(PacketManager.makeTextPacket(toSay));

            // add it to your own text log
            logMessage(toSay);
        }
        else if (m_netStatus == NETSTATE_JOINED)
        {
            // if you're a player, just post it to the GM
            send(PacketManager.makeTextPacket(toSay));
        }
        else
        {
            // if you're offline, just add it to the log
            logMessage(toSay);
        }
    }

    public void savePrefs()
    {
        try
        {
            FileOutputStream prefFile = new FileOutputStream("prefs.prf");
            DataOutputStream prefDos = new DataOutputStream(prefFile);

            prefDos.writeUTF(m_defaultName);
            prefDos.writeUTF(m_defaultCharName);
            prefDos.writeUTF(m_defaultIP);
            prefDos.writeInt(m_defaultPort);
            prefDos.writeUTF(m_defaultPassword);
            prefDos.writeInt(m_gametableCanvas.getPublicMap().getScrollX());
            prefDos.writeInt(m_gametableCanvas.getPublicMap().getScrollY());
            prefDos.writeInt(m_gametableCanvas.m_zoom);

            prefDos.writeInt(m_windowSize.width);
            prefDos.writeInt(m_windowSize.height);
            prefDos.writeInt(m_windowPos.x);
            prefDos.writeInt(m_windowPos.y);
            prefDos.writeBoolean(m_bMaximized);

            // divider locations
            prefDos.writeInt(m_mapChatSplitPane.getDividerLocation());
            prefDos.writeInt(m_mapPogSplitPane.getDividerLocation());

            prefDos.writeInt(m_macros.size());
            for (int i = 0; i < m_macros.size(); i++)
            {
                DiceMacro dm = (DiceMacro)m_macros.get(i);
                dm.writeToStream(prefDos);
            }

            prefDos.close();
            prefFile.close();
        }
        catch (FileNotFoundException ex1)
        {
            Log.log(Log.SYS, ex1);
        }
        catch (IOException ex1)
        {
            Log.log(Log.SYS, ex1);
        }
    }

    public void loadPrefs()
    {
        File file = new File("prefs.prf");
        if (!file.canWrite())
        {
            Log.log(Log.SYS, "Unable to write: " + file);
        }

        if (!file.exists())
        {
            // DEFAULTS
            m_mapChatSplitPane.setDividerLocation(0.8);
            m_mapPogSplitPane.setDividerLocation(150);
            m_windowSize.width = 800;
            m_windowSize.height = 600;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            m_windowPos.x = (screenSize.width - m_windowSize.width) / 2;
            m_windowPos.y = (screenSize.height - m_windowSize.height) / 2;
            m_bMaximized = false;
            applyWindowInfo();
            return;
        }

        try
        {
            FileInputStream prefFile = new FileInputStream(file);
            DataInputStream prefDis = new DataInputStream(prefFile);

            m_defaultName = prefDis.readUTF();
            m_defaultCharName = prefDis.readUTF();
            m_defaultIP = prefDis.readUTF();
            m_defaultPort = prefDis.readInt();
            m_defaultPassword = prefDis.readUTF();
            m_gametableCanvas.setPrimaryScroll(m_gametableCanvas.getPublicMap(), prefDis.readInt(), prefDis.readInt());
            m_gametableCanvas.setZoom(prefDis.readInt());

            m_windowSize.width = prefDis.readInt();
            m_windowSize.height = prefDis.readInt();
            m_windowPos.x = prefDis.readInt();
            m_windowPos.y = prefDis.readInt();
            m_bMaximized = prefDis.readBoolean();
            applyWindowInfo();

            // divider locations
            m_bDisregardDividerChanges = true;
            m_mapChatSplitPane.setDividerLocation(prefDis.readInt());
            m_mapPogSplitPane.setDividerLocation(prefDis.readInt());
            m_bDisregardDividerChanges = false;

            m_macros = new ArrayList();
            int numMacros = prefDis.readInt();
            for (int i = 0; i < numMacros; i++)
            {
                DiceMacro dm = new DiceMacro();
                dm.initFromStream(prefDis);
                addMacro(dm);
            }

            prefDis.close();
            prefFile.close();

            m_bLoadedState = true;
        }
        catch (FileNotFoundException ex1)
        {
            Log.log(Log.SYS, ex1);
        }
        catch (IOException ex1)
        {
            Log.log(Log.SYS, ex1);
        }
    }

    /**
     * Updates the frame size and position based on the preferences stored.
     */
    public void applyWindowInfo()
    {
        Point locCopy = new Point(m_windowPos);
        setSize(m_windowSize);
        m_windowPos = locCopy;
        setLocation(locCopy);
        if (m_bMaximized)
        {
            setExtendedState(MAXIMIZED_BOTH);
        }
        else
        {
            setExtendedState(NORMAL);
        }
    }

    public void saveState(GametableMap mapToSave, File file)
    {
        // save out all our data. The best way to do this is with packets, cause they're
        // already designed to pass data around.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try
        {
            LineSegment[] lines = new LineSegment[mapToSave.getNumLines()];
            for (int i = 0; i < mapToSave.getNumLines(); i++)
            {
                lines[i] = mapToSave.getLineAt(i);
            }
            byte[] linesPacket = PacketManager.makeLinesPacket(lines, -1, -1);
            dos.writeInt(linesPacket.length);
            dos.write(linesPacket);

            // pogs
            for (int i = 0; i < mapToSave.getNumPogs(); i++)
            {
                Pog pog = mapToSave.getPogAt(i);
                byte[] pogsPacket = PacketManager.makeAddPogPacket(pog);
                dos.writeInt(pogsPacket.length);
                dos.write(pogsPacket);
            }

            // grid state
            byte gridModePacket[] = PacketManager.makeGridModePacket(m_gametableCanvas.getGridModeID());
            dos.writeInt(gridModePacket.length);
            dos.write(gridModePacket);

            byte[] saveFileData = baos.toByteArray();
            FileOutputStream output = new FileOutputStream(file);
            DataOutputStream fileOut = new DataOutputStream(output);
            fileOut.writeInt(COMM_VERSION);
            fileOut.writeInt(saveFileData.length);
            fileOut.write(saveFileData);
            output.close();
            fileOut.close();
            baos.close();
            dos.close();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            // failed to save. give up
        }
    }

    public void loadState(File file)
    {
        if (!file.exists())
        {
            return;
        }

        try
        {
            FileInputStream input = new FileInputStream(file);
            DataInputStream infile = new DataInputStream(input);

            // get the big hunk o daya
            int ver = infile.readInt();
            if (ver != COMM_VERSION)
            {
                // wrong version
                throw new IOException("Invalid save file version.");
            }

            int len = infile.readInt();
            byte[] saveFileData = new byte[len];
            infile.read(saveFileData);

            PacketSourceState.beginFileLoad();
            loadState(saveFileData);
            PacketSourceState.endFileLoad();

            input.close();
            infile.close();
        }
        catch (FileNotFoundException ex)
        {
            Log.log(Log.SYS, ex);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public void loadStateFromRawFileData(byte rawFileData[])
    {
        byte saveFileData[] = new byte[rawFileData.length - 8]; // a new array that lacks the first
        // int
        System.arraycopy(rawFileData, 8, saveFileData, 0, saveFileData.length);
        loadState(saveFileData);
    }

    public void loadState(byte saveFileData[])
    {
        // let it know we're receiving initial data (which we are. Just fro ma file instead of the host)
        try
        {
            // now we have to pick out the packets and send them in for processing one at a time
            DataInputStream walker = new DataInputStream(new ByteArrayInputStream(saveFileData));
            int read = 0;
            while (read < saveFileData.length)
            {
                int packetLen = walker.readInt();
                read += 4;

                byte[] packet = new byte[packetLen];
                walker.read(packet);
                read += packetLen;

                // dispatch the packet
                PacketManager.readPacket(null, packet);
            }
        }
        catch (FileNotFoundException ex)
        {
            Log.log(Log.SYS, ex);
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
        }

        repaint();
        refreshPogList();
    }

    private void initializeExecutorThread()
    {
        if (m_executorThread != null)
        {
            m_executorThread.interrupt();
            m_executorThread = null;
        }

        // start the poll thread
        m_executorThread = new PeriodicExecutorThread(new Runnable()
        {
            public void run()
            {
                NetworkThread thread = m_networkThread;
                if (thread != null)
                {
                    Set lostConnections = thread.getLostConnections();
                    Iterator iterator = lostConnections.iterator();
                    while (iterator.hasNext())
                    {
                        Connection connection = (Connection)iterator.next();
                        connectionDropped(connection);
                    }

                    List packets = thread.getPackets();
                    iterator = packets.iterator();
                    while (iterator.hasNext())
                    {
                        Packet packet = (Packet)iterator.next();
                        packetReceived(packet.getSource(), packet.getData());
                    }

                    if (System.currentTimeMillis() - m_lastPingTime >= PING_INTERVAL)
                    {
                        send(PacketManager.makePingPacket());
                        m_lastPingTime = System.currentTimeMillis();
                    }
                }
            }
        });
        m_executorThread.start();
    }
}



class GametableFrame_m_textEntry_actionAdapter implements java.awt.event.ActionListener
{
    GametableFrame adaptee;

    GametableFrame_m_textEntry_actionAdapter(GametableFrame a)
    {
        adaptee = a;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.m_textEntry_actionPerformed(e);
    }
}
