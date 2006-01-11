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
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.galactanet.gametable.net.Connection;
import com.galactanet.gametable.net.NetworkThread;
import com.galactanet.gametable.net.Packet;
import com.galactanet.gametable.prefs.PreferenceDescriptor;
import com.galactanet.gametable.prefs.Preferences;



/**
 * The main Gametable Frame class.
 * 
 * @author sephalon
 */
public class GametableFrame extends JFrame implements ActionListener
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
            getGametableCanvas().setActiveTool(m_id);
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
            if (getFocusOwner() instanceof JTextField)
            {
                return;
            }
            getGametableCanvas().setActiveTool(m_id);
        }
    }

    /**
     * Default password for when there is no prefs file.
     */
    private static final String   DEFAULT_PASSWORD        = "";

    /**
     * Default server for when there is no prefs file.
     */
    private static final String   DEFAULT_SERVER          = "localhost";

    /**
     * Default Character name for when there is no prefs file.
     */
    private static final String   DEFAULT_CHARACTER_NAME  = "Anonymous";

    /**
     * The version of the communications protocal used by this build. This needs to change whenever an incompatibility
     * arises betwen versions.
     */
    public final static int       COMM_VERSION            = 10;
    public final static int       PING_INTERVAL           = 2500;

    public final static int       NETSTATE_NONE           = 0;
    public final static int       NETSTATE_HOST           = 1;
    public final static int       NETSTATE_JOINED         = 2;

    public final static int       REJECT_INVALID_PASSWORD = 0;
    public final static int       REJECT_VERSION_MISMATCH = 1;

    public final static int       DEFAULT_PORT            = 6812;

    public final static Integer[] COLORS                  = {
        new Integer(new Color(0, 0, 0).getRGB()), new Integer(new Color(198, 198, 198).getRGB()),
        new Integer(new Color(0, 0, 255).getRGB()), new Integer(new Color(0, 255, 0).getRGB()),
        new Integer(new Color(0, 255, 255).getRGB()), new Integer(new Color(255, 0, 0).getRGB()),
        new Integer(new Color(255, 0, 255).getRGB()), new Integer(new Color(255, 255, 0).getRGB()),
        new Integer(new Color(255, 255, 255).getRGB()), new Integer(new Color(0, 0, 132).getRGB()),
        new Integer(new Color(0, 132, 0).getRGB()), new Integer(new Color(0, 132, 132).getRGB()),
        new Integer(new Color(132, 0, 0).getRGB()), new Integer(new Color(132, 0, 132).getRGB()),
        new Integer(new Color(132, 132, 0).getRGB()), new Integer(new Color(132, 132, 132).getRGB())
                                                          };

    private final static boolean  DEBUG_FOCUS             = false;
    private final static boolean  SEND_PINGS              = false;

    /**
     * The global gametable instance.
     */
    private static GametableFrame g_gametableFrame;

    /**
     * @return The global GametableFrame instance.
     */
    public static GametableFrame getGametableFrame()
    {
        return g_gametableFrame;
    }

    private JMenuItem              m_hostMenuItem;
    private JMenuItem              m_joinMenuItem;
    private JMenuItem              m_disconnectMenuItem;

    private JCheckBoxMenuItem      m_noGridModeMenuItem     = new JCheckBoxMenuItem("No Grid");
    private JCheckBoxMenuItem      m_squareGridModeMenuItem = new JCheckBoxMenuItem("Square Grid");
    private JCheckBoxMenuItem      m_hexGridModeMenuItem    = new JCheckBoxMenuItem("Hex Grid");
    private JCheckBoxMenuItem      m_togglePrivateMapMenuItem;

    private JPanel                 m_chatPanel              = new JPanel();
    private GametableCanvas        m_gametableCanvas        = new GametableCanvas();

    private List                   m_players                = new ArrayList();

    // which player I am
    private int                    m_myPlayerIndex;

    public String                  m_playerName             = System.getProperty("user.name");
    public String                  m_characterName          = DEFAULT_CHARACTER_NAME;
    public String                  m_ipAddress              = DEFAULT_SERVER;
    public int                     m_port                   = DEFAULT_PORT;
    public String                  m_password               = DEFAULT_PASSWORD;

    private JPanel                 m_textAndEntryPanel      = new JPanel();
    private JTextField             m_textEntry              = new JTextField();
    private ChatLogPane            m_chatLog                = new ChatLogPane();
    private JSplitPane             m_mapChatSplitPane       = new JSplitPane();

    private JPanel                 m_textAreaPanel          = new JPanel();
    private JSplitPane             m_mapPogSplitPane        = new JSplitPane();
    private PogPanel               m_pogPanel               = null;
    private MacroPanel             m_macroPanel             = null;
    private JToolBar               m_toolBar                = new JToolBar();
    private ButtonGroup            m_toolButtonGroup        = new ButtonGroup();

    private Map                    m_macroMap               = new TreeMap();

    private int                    m_netStatus              = NETSTATE_NONE;

    private volatile NetworkThread m_networkThread;
    private PeriodicExecutorThread m_executorThread;

    private long                   m_lastPingTime           = 0;
    private long                   m_lastTickTime           = 0;

    public Color                   m_drawColor              = Color.BLACK;

    // window size and position
    private Point                  m_windowPos;
    private Dimension              m_windowSize;
    private boolean                m_bMaximized;

    private JTabbedPane            m_pogsTabbedPane         = new JTabbedPane();
    private JComboBox              m_colorCombo             = new JComboBox(COLORS);

    // full of Strings
    private List                   m_textSent               = new ArrayList();
    private int                    m_textSentLoc            = 0;

    // The current file path used by save and open.
    // NULL if unset.
    // one for the public map, one for the private map
    public File                    m_actingFilePublic;
    public File                    m_actingFilePrivate;

    private ToolManager            m_toolManager            = new ToolManager();
    private JToggleButton          m_toolButtons[]          = null;
    private Preferences            m_preferences            = new Preferences();
    private PogLibrary             m_pogLibrary             = null;

    public ProgressSpinner         m_progressSpinner        = new ProgressSpinner();

    // the id that will be assigned to the next player to join
    public int                     m_nextPlayerId;

    // the id that will be assigned to the change made
    public int                     m_nextStateId;

    /**
     * Construct the frame
     */
    public GametableFrame()
    {
        g_gametableFrame = this;

        try
        {
            initialize();
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
        }
    }

    /**
     * Performs initialization.
     * 
     * @throws IOException
     */
    private void initialize() throws IOException
    {
        if (DEBUG_FOCUS)
        {
            KeyboardFocusManager man = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            man.addPropertyChangeListener(new PropertyChangeListener()
            {
                /*
                 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
                 */
                public void propertyChange(PropertyChangeEvent evt)
                {
                    System.out.println("propertyChange(" + evt.getPropertyName() + ": " + evt.getOldValue() + " -> "
                        + evt.getNewValue() + ")");
                }

            });
        }

        setContentPane(new JPanel(new BorderLayout()));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(GametableApp.VERSION);
        setJMenuBar(getMainMenuBar());
        m_noGridModeMenuItem.addActionListener(this);
        m_squareGridModeMenuItem.addActionListener(this);
        m_hexGridModeMenuItem.addActionListener(this);

        m_chatPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        m_chatPanel.setLayout(new BorderLayout());

        m_textAndEntryPanel.setLayout(new BorderLayout());

        m_textEntry.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                // they hit return on the text bar
                String entered = m_textEntry.getText().trim();
                if (entered.length() == 0)
                {
                    // useless string.
                    // return focus to the map
                    getGametableCanvas().requestFocus();
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
                    postMessage(getMyPlayer().getCharacterName() + "> " + entered);
                }

                m_textEntry.setText(DEFAULT_PASSWORD);
            }
        });

        m_textEntry.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
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
                            m_textEntry.setText(DEFAULT_PASSWORD);
                        }
                        else
                        {
                            m_textEntry.setText((String)m_textSent.get(m_textSentLoc));
                        }
                    }
                }
            }
        });

        m_textAndEntryPanel.add(m_textEntry, BorderLayout.SOUTH);

        m_chatLog.setDoubleBuffered(true);
        m_textAndEntryPanel.add(m_chatLog.getComponentToAdd(), BorderLayout.CENTER);

        m_mapChatSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        m_mapChatSplitPane.setContinuousLayout(true);
        m_mapChatSplitPane.setResizeWeight(1.0);
        m_mapChatSplitPane.setBorder(null);

        m_textAreaPanel.setLayout(new BorderLayout());

        m_mapPogSplitPane.setContinuousLayout(true);
        m_mapPogSplitPane.setBorder(null);

        m_colorCombo.setMaximumSize(new Dimension(100, 21));
        m_colorCombo.setFocusable(false);
        m_toolBar.setFloatable(false);
        m_toolBar.setBorder(new EmptyBorder(2, 5, 2, 5));
        m_toolBar.add(m_colorCombo, null);
        m_toolBar.add(Box.createHorizontalStrut(5));

        initializeTools();

        getContentPane().add(m_toolBar, BorderLayout.NORTH);

        m_pogLibrary = new PogLibrary();
        getGametableCanvas().init(this);

        m_pogPanel = new PogPanel(m_pogLibrary, getGametableCanvas());
        m_pogsTabbedPane.add(m_pogPanel, "Pog Library");
        m_pogsTabbedPane.add(new JPanel(), "Active Pogs");
        m_macroPanel = new MacroPanel();
        m_pogsTabbedPane.add(m_macroPanel, "Dice Macros");
        m_pogsTabbedPane.setFocusable(false);

        m_chatPanel.add(m_textAreaPanel, BorderLayout.CENTER);
        m_textAreaPanel.add(m_textAndEntryPanel, BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 1, 1, 1)));
        panel.add(getGametableCanvas(), BorderLayout.CENTER);
        m_mapChatSplitPane.add(panel, JSplitPane.TOP);
        m_mapChatSplitPane.add(m_chatPanel, JSplitPane.BOTTOM);

        m_mapPogSplitPane.add(m_pogsTabbedPane, JSplitPane.LEFT);
        m_mapPogSplitPane.add(m_mapChatSplitPane, JSplitPane.RIGHT);
        getContentPane().add(m_mapPogSplitPane, BorderLayout.CENTER);

        m_disconnectMenuItem.setEnabled(false);

        ColorComboCellRenderer renderer = new ColorComboCellRenderer();
        m_colorCombo.setRenderer(renderer);

        // load the primary map
        getGametableCanvas().setActiveMap(getGametableCanvas().getPrivateMap());
        PacketSourceState.beginFileLoad();
        loadState(new File("autosavepvt.grm"));
        PacketSourceState.endFileLoad();

        getGametableCanvas().setActiveMap(getGametableCanvas().getPublicMap());
        loadState(new File("autosave.grm"));
        loadPrefs();

        addPlayer(new Player(m_playerName, m_characterName, -1));
        this.m_myPlayerIndex = 0;

        m_colorCombo.addActionListener(this);
        updateGridModeMenu();
        updatePrivateLayerModeMenuItem();

        addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent event)
            {
                updateWindowInfo();
            }

            public void componentMoved(ComponentEvent e)
            {
                updateWindowInfo();
            }

        });

        addWindowListener(new WindowAdapter()
        {
            /*
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             */
            public void windowClosing(WindowEvent e)
            {
                saveAll();
            }

            /*
             * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
             */
            public void windowClosed(WindowEvent e)
            {
                saveAll();
            }
        });

        m_gametableCanvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed SLASH"),
            "startSlash");
        m_gametableCanvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed ENTER"),
            "startText");
        m_gametableCanvas.getActionMap().put("startSlash", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (getFocusOwner() instanceof JTextField)
                {
                    return;
                }

                m_textEntry.setText("/");
                m_textEntry.requestFocus();
            }
        });

        m_gametableCanvas.getActionMap().put("startText", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                m_textEntry.requestFocus();
            }
        });

        initializeExecutorThread();
    }

    /**
     * Initializes the tools from the ToolManager.
     */
    private void initializeTools()
    {
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
                button.setFocusable(false);
                m_toolButtonGroup.add(button);
                m_toolButtons[toolId] = button;

                String keyInfo = DEFAULT_PASSWORD;
                if (info.getQuickKey() != null)
                {
                    String actionId = "tool" + toolId + "Action";
                    getGametableCanvas().getActionMap().put(actionId, new ToolButtonAbstractAction(toolId));
                    KeyStroke keystroke = KeyStroke.getKeyStroke("ctrl " + info.getQuickKey());
                    getGametableCanvas().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keystroke, actionId);
                    keyInfo = " (Ctrl+" + info.getQuickKey() + ")";
                }
                button.setToolTipText(info.getName() + keyInfo);
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
    }

    // --- Menus ---

    private JMenuBar getMainMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getFileMenu());
        menuBar.add(getEditMenu());
        menuBar.add(getNetworkMenu());
        menuBar.add(getMapMenu());
        menuBar.add(getDiceMenu());
        menuBar.add(getHelpMenu());

        return menuBar;
    }

    private JMenu getFileMenu()
    {
        JMenu menu = new JMenu("File");

        menu.add(getOpenMapMenuItem());
        menu.add(getSaveMapMenuItem());
        menu.add(getSaveMapAsMenuItem());
        menu.add(getScanForPogsMenuItem());
        menu.add(getQuitMenuItem());

        return menu;
    }

    private JMenu getEditMenu()
    {
        JMenu menu = new JMenu("Edit");
        menu.add(getUndoMenuItem());
        menu.add(getRedoMenuItem());

        return menu;
    }

    private JMenu getNetworkMenu()
    {
        JMenu menu = new JMenu("Network");
        menu.add(getListPlayersMenuItem());
        menu.add(getHostMenuItem());
        menu.add(getJoinMenuItem());
        menu.add(getDisconnectMenuItem());

        return menu;
    }

    private JMenu getMapMenu()
    {
        JMenu menu = new JMenu("Map");
        menu.add(getClearMapMenuItem());
        menu.add(getRecenterAllPlayersMenuItem());
        menu.add(getGridModeMenu());
        menu.add(getTogglePrivateMapMenuItem());

        return menu;
    }

    private JMenu getGridModeMenu()
    {
        JMenu menu = new JMenu("Grid Mode");
        menu.add(m_noGridModeMenuItem);
        menu.add(m_squareGridModeMenuItem);
        menu.add(m_hexGridModeMenuItem);

        return menu;
    }

    private JMenu getDiceMenu()
    {
        JMenu menu = new JMenu("Dice");
        menu.add(getAddDiceMenuItem());
        menu.add(getDeleteDiceMenuItem());
        return menu;
    }

    private JMenu getHelpMenu()
    {
        JMenu menu = new JMenu("Help");
        menu.add(getAboutMenuItem());

        // TODO: Remove later on
        JMenuItem item = new JMenuItem("testhtml");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                HtmlTestDialog dialog = new HtmlTestDialog();
                dialog.setLocationRelativeTo(m_gametableCanvas);
                dialog.setVisible(true);
            }
        });
        menu.add(item);

        return menu;
    }

    // --- MenuItems ---

    private JMenuItem getOpenMapMenuItem()
    {
        JMenuItem item = new JMenuItem("Open Map...");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed O"));
        item.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                // opening while on the public layer...
                if (getGametableCanvas().getActiveMap() == getGametableCanvas().getPublicMap())
                {
                    File openFile = UtilityFunctions.doFileOpenDialog("Open", "grm", true);

                    if (openFile == null)
                    {
                        // they cancelled out of the open
                        return;
                    }

                    m_actingFilePublic = openFile;

                    int result = UtilityFunctions.yesNoDialog(GametableFrame.this,
                        "This will load a map file, replacing all existing map data for you and all players in the "
                            + "session. Are you sure you want to do this?", "Confirm Load");
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

                            postSystemMessage(getMyPlayer().getPlayerName() + " loads a new map.");
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
        });

        return item;
    }

    public JMenuItem getSaveMapMenuItem()
    {
        JMenuItem item = new JMenuItem("Save Map");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed S"));
        item.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (getGametableCanvas().isPublicMap())
                {
                    if (m_actingFilePublic == null)
                    {
                        m_actingFilePublic = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
                    }

                    if (m_actingFilePublic != null)
                    {
                        // save the file
                        saveState(getGametableCanvas().getActiveMap(), m_actingFilePublic);
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
                        saveState(getGametableCanvas().getActiveMap(), m_actingFilePrivate);
                    }
                }
            }
        });

        return item;
    }

    public JMenuItem getSaveMapAsMenuItem()
    {
        JMenuItem item = new JMenuItem("Save Map As...");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl shift pressed S"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (getGametableCanvas().isPublicMap())
                {
                    m_actingFilePublic = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
                    if (m_actingFilePublic != null)
                    {
                        saveState(getGametableCanvas().getActiveMap(), m_actingFilePublic);
                    }
                }
                else
                {
                    m_actingFilePrivate = UtilityFunctions.doFileSaveDialog("Save As", "grm", true);
                    if (m_actingFilePrivate != null)
                    {
                        saveState(getGametableCanvas().getActiveMap(), m_actingFilePrivate);
                    }
                }
            }
        });

        return item;
    }

    private JMenuItem getScanForPogsMenuItem()
    {
        JMenuItem item = new JMenuItem("Scan for Pogs");
        item.setAccelerator(KeyStroke.getKeyStroke("F5"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                reacquirePogs();
            }
        });

        return item;
    }

    private JMenuItem getQuitMenuItem()
    {
        JMenuItem item = new JMenuItem("Quit");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });

        return item;
    }

    private JMenuItem getUndoMenuItem()
    {
        JMenuItem item = new JMenuItem("Undo");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                getGametableCanvas().undo();
            }
        });

        return item;
    }

    private JMenuItem getRedoMenuItem()
    {
        JMenuItem item = new JMenuItem("Redo");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl Y"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                getGametableCanvas().redo();
            }
        });

        return item;
    }

    private JMenuItem getListPlayersMenuItem()
    {
        JMenuItem item = new JMenuItem("List Players");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl W"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                parseSlashCommand("/who");
            }
        });
        return item;
    }

    private JMenuItem getHostMenuItem()
    {
        if (m_hostMenuItem == null)
        {
            JMenuItem item = new JMenuItem("Host...");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    host();
                }
            });

            m_hostMenuItem = item;
        }
        return m_hostMenuItem;
    }

    private JMenuItem getJoinMenuItem()
    {
        if (m_joinMenuItem == null)
        {
            JMenuItem item = new JMenuItem("Join...");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    join();
                }
            });
            m_joinMenuItem = item;
        }
        return m_joinMenuItem;
    }

    private JMenuItem getDisconnectMenuItem()
    {
        if (m_disconnectMenuItem == null)
        {
            JMenuItem item = new JMenuItem("Disconnect");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    disconnect();
                }
            });
            m_disconnectMenuItem = item;
        }
        return m_disconnectMenuItem;
    }

    private JMenuItem getClearMapMenuItem()
    {
        JMenuItem item = new JMenuItem("Clear Map");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int res = UtilityFunctions.yesNoDialog(GametableFrame.this,
                    "This will clear all lines, pogs, and underlays on the entire layer. Are you sure?", "Clear Map");
                if (res == UtilityFunctions.YES)
                {
                    eraseAllPogs();
                    eraseAllLines();
                    repaint();
                }
            }
        });

        return item;
    }

    private JMenuItem getRecenterAllPlayersMenuItem()
    {
        JMenuItem item = new JMenuItem("Recenter all Players");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int result = UtilityFunctions.yesNoDialog(GametableFrame.this,
                    "This will recenter everyone's map view to match yours, "
                        + "and will set their zoom levels to match yours. Are you sure you want to do this?",
                    "Recenter?");
                if (result == UtilityFunctions.YES)
                {
                    // get our view center
                    int viewCenterX = getGametableCanvas().getWidth() / 2;
                    int viewCenterY = getGametableCanvas().getHeight() / 2;

                    // convert to model coordinates
                    Point modelCenter = getGametableCanvas().viewToModel(viewCenterX, viewCenterY);
                    getGametableCanvas().recenterView(modelCenter.x, modelCenter.y, getGametableCanvas().m_zoom);
                    postSystemMessage(getMyPlayer().getPlayerName() + " Recenters everyone's view!");
                }
            }
        });
        return item;
    }

    private JMenuItem getTogglePrivateMapMenuItem()
    {
        if (m_togglePrivateMapMenuItem == null)
        {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem("Edit Private Map");
            item.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    toggleLayer();
                }
            });

            m_togglePrivateMapMenuItem = item;
        }

        return m_togglePrivateMapMenuItem;
    }

    private JMenuItem getAddDiceMenuItem()
    {
        JMenuItem item = new JMenuItem("Add...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addDieMacro();
            }
        });
        return item;
    }

    private JMenuItem getDeleteDiceMenuItem()
    {
        JMenuItem item = new JMenuItem("Delete...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Object[] list = m_macroMap.values().toArray();
                // give them a list of macros they can delete
                Object sel = JOptionPane.showInputDialog(GametableFrame.this, "Select Dice Macro to remove:",
                    "Remove Dice Macro", JOptionPane.PLAIN_MESSAGE, null, list, list[0]);
                if (sel != null)
                {
                    removeMacro((DiceMacro)sel);
                }
            }
        });
        return item;
    }

    private JMenuItem getAboutMenuItem()
    {
        JMenuItem item = new JMenuItem("About");
        item.setAccelerator(KeyStroke.getKeyStroke("F1"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UtilityFunctions.msgBox(GametableFrame.this, GametableApp.VERSION
                    + " by Andy Weir and David Ghandehari", "Version");
            }
        });
        return item;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == m_colorCombo)
        {
            Integer col = (Integer)m_colorCombo.getSelectedItem();
            m_drawColor = new Color(col.intValue());
        }
        else if (e.getSource() == m_noGridModeMenuItem)
        {
            getGametableCanvas().m_gridMode = getGametableCanvas().m_noGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_NONE));
            updateGridModeMenu();
            getGametableCanvas().repaint();
            postSystemMessage(getMyPlayer().getPlayerName() + " changes the grid mode.");
        }
        else if (e.getSource() == m_squareGridModeMenuItem)
        {
            getGametableCanvas().m_gridMode = getGametableCanvas().m_squareGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_SQUARES));
            updateGridModeMenu();
            getGametableCanvas().repaint();
            postSystemMessage(getMyPlayer().getPlayerName() + " changes the grid mode.");
        }
        else if (e.getSource() == m_hexGridModeMenuItem)
        {
            getGametableCanvas().m_gridMode = getGametableCanvas().m_hexGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_HEX));
            updateGridModeMenu();
            getGametableCanvas().repaint();
            postSystemMessage(getMyPlayer().getPlayerName() + " changes the grid mode.");
        }
    }

    /**
     * @return Returns the m_netStatus.
     */
    public int getNetStatus()
    {
        return m_netStatus;
    }

    public int getNewStateId()
    {
        return m_nextStateId++;
    }

    /**
     * @return The pog panel.
     */
    public PogPanel getPogPanel()
    {
        return m_pogPanel;
    }

    /**
     * @return The preferences object.
     */
    public Preferences getPreferences()
    {
        return m_preferences;
    }

    /**
     * @return The root pog library.
     */
    public PogLibrary getPogLibrary()
    {
        return m_pogLibrary;
    }

    /**
     * @return Returns the gametableCanvas.
     */
    public GametableCanvas getGametableCanvas()
    {
        return m_gametableCanvas;
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

    /**
     * @return The player representing this client.
     */
    public Player getMyPlayer()
    {
        return (Player)m_players.get(getMyPlayerIndex());
    }

    /**
     * @return Returns the player list.
     */
    public List getPlayers()
    {
        return Collections.unmodifiableList(m_players);
    }

    /**
     * @return The id of the player representing this client.
     */
    public int getMyPlayerId()
    {
        return getMyPlayer().getId();
    }

    public void loginCompletePacketReceived()
    {
        // this packet is never redistributed.
        // all we do in response to this allow pog text
        // highlights. The pogs don't know the difference between
        // inital data and actual player changes.
        PacketSourceState.endHostDump();

        // seed our undo stack with this as the bottom rung
        getGametableCanvas().getPublicMap().beginUndoableAction();
        getGametableCanvas().getPublicMap().endUndoableAction(-1, -1);
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
                logSystemMessage("The host is using a different version of the Gametable network protocol. Connection aborted.");
            }
            break;
        }
        disconnect();
    }

    public void recenterPacketReceived(int x, int y, int zoom)
    {
        getGametableCanvas().doRecenterView(x, y, zoom);

        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makeRecenterPacket(x, y, zoom));
        }
    }

    public void pogDataPacketReceived(int id, String s, Map toAdd, Set toDelete)
    {
        getGametableCanvas().doSetPogData(id, s, toAdd, toDelete);

        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makePogDataPacket(id, s, toAdd, toDelete));
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
        getGametableCanvas().doUndo(stateID);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeUndoPacket(stateID));
        }

        repaint();
    }

    public void redoPacketReceived(int stateID)
    {
        getGametableCanvas().doRedo(stateID);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeRedoPacket(stateID));
        }

        repaint();
    }

    public void pogSizePacketReceived(int id, int size)
    {
        getGametableCanvas().doSetPogSize(id, size);

        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makePogSizePacket(id, size));
        }
    }

    public void pointPacketReceived(int plrIdx, int x, int y, boolean bPointing)
    {
        // we're not interested in point packets of our own hand
        if (plrIdx != getMyPlayerIndex())
        {
            Player plr = (Player)m_players.get(plrIdx);
            plr.setPoint(x, y);
            plr.setPointing(bPointing);
        }

        if (m_netStatus == NETSTATE_HOST)
        {
            send(PacketManager.makePointPacket(plrIdx, x, y, bPointing));
        }

        getGametableCanvas().repaint();
    }

    public void movePogPacketReceived(int id, int newX, int newY)
    {
        getGametableCanvas().doMovePog(id, newX, newY);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeMovePogPacket(id, newX, newY));
        }
    }

    public void removePogsPacketReceived(int ids[])
    {
        getGametableCanvas().doRemovePogs(ids);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeRemovePogsPacket(ids));
        }
    }

    public void addPogPacketReceived(Pog pog)
    {
        getGametableCanvas().doAddPog(pog);

        // update the next pog id if necessary
        if (pog.getId() >= Pog.g_nextId)
        {
            Pog.g_nextId = pog.getId() + 5;
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
            stateID = this.getNewStateId();
            send(PacketManager.makeErasePacket(r, bColorSpecific, color, authorID, stateID));
        }

        // erase the lines
        getGametableCanvas().doErase(r, bColorSpecific, color, authorID, stateID);
    }

    public void linesPacketReceived(LineSegment[] lines, int authorID, int stateID)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            // and give it a genuine state ID first
            stateID = this.getNewStateId();
            send(PacketManager.makeLinesPacket(lines, authorID, stateID));
        }

        // add the lines to the array
        getGametableCanvas().doAddLineSegments(lines, authorID, stateID);
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
        getGametableCanvas().setGridModeByID(gridMode);
        updateGridModeMenu();

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeGridModePacket(gridMode));
        }

        repaint();
    }

    /**
     * Finds the index of a given player.
     * 
     * @param player Player to find index of.
     * @return Index of the given player, or -1.
     */
    public int getPlayerIndex(Player player)
    {
        return m_players.indexOf(player);
    }

    /**
     * @return Returns the myPlayerIndex.
     */
    public int getMyPlayerIndex()
    {
        return m_myPlayerIndex;
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
            postSystemMessage(DEFAULT_PASSWORD + dead.getPlayerName() + " has left the session");
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

        this.m_myPlayerIndex = ourIdx;

        // any time the cast changes, all the undo stacks clear
        getGametableCanvas().clearUndoStacks();
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

        if (!m_password.equals(password))
        {
            // rejected!
            kick(connection, REJECT_INVALID_PASSWORD);
            return;
        }

        // now we can associate a player with the connection
        connection.markLoggedIn();
        player.setConnection(connection);

        // set their ID
        player.setId(m_nextPlayerId);
        m_nextPlayerId++;

        // tell everyone about the new guy
        postSystemMessage(player.getPlayerName() + " has joined the session");
        addPlayer(player);

        sendCastInfo();

        // all the undo stacks clear
        getGametableCanvas().clearUndoStacks();

        // tell the new guy the entire state of the game
        // lines
        LineSegment[] lines = new LineSegment[getGametableCanvas().getPublicMap().getNumLines()];
        for (int i = 0; i < getGametableCanvas().getPublicMap().getNumLines(); i++)
        {
            lines[i] = getGametableCanvas().getPublicMap().getLineAt(i);
        }
        send(PacketManager.makeLinesPacket(lines, -1, -1), player);

        // pogs
        for (int i = 0; i < getGametableCanvas().getPublicMap().getNumPogs(); i++)
        {
            Pog pog = getGametableCanvas().getPublicMap().getPogAt(i);
            send(PacketManager.makeAddPogPacket(pog), player);
        }

        // finally, have the player recenter on the host's view
        int viewCenterX = getGametableCanvas().getWidth() / 2;
        int viewCenterY = getGametableCanvas().getHeight() / 2;

        // convert to model coordinates
        Point modelCenter = getGametableCanvas().viewToModel(viewCenterX, viewCenterY);
        send(PacketManager.makeRecenterPacket(modelCenter.x, modelCenter.y, getGametableCanvas().m_zoom), player);

        // let them know we're done sending them data from the login
        send(PacketManager.makeLoginCompletePacket(), player);
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
        m_nextPlayerId = 0;
        m_players = new ArrayList();
        Player me = new Player(m_playerName, m_characterName, m_nextPlayerId); // this means the host is always
        // player 0
        m_nextPlayerId++;
        m_players.add(me);
        me.setHostPlayer(true);
        this.m_myPlayerIndex = 0;

        m_networkThread = new NetworkThread(m_port);
        m_networkThread.start();
        // TODO: fix hosting failure detection

        m_netStatus = NETSTATE_HOST;
        String message = "Hosting on port: " + m_port;
        logSystemMessage(message);
        Log.log(Log.NET, message);

        m_hostMenuItem.setEnabled(false);
        m_joinMenuItem.setEnabled(false);
        m_disconnectMenuItem.setEnabled(true);
        setTitle(GametableApp.VERSION + " - " + me.getCharacterName());

        // when you host, all the undo stacks clear
        getGametableCanvas().clearUndoStacks();
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
            Connection conn = new Connection(m_ipAddress, m_port);
            m_networkThread.add(conn);

            // now that we've successfully made a connection, let the host know
            // who we are
            m_players = new ArrayList();
            Player me = new Player(m_playerName, m_characterName, -1);
            me.setConnection(conn);
            m_players.add(me);
            this.m_myPlayerIndex = 0;

            // reset game data
            getGametableCanvas().getPublicMap().setScroll(0, 0);
            getGametableCanvas().getPublicMap().clearPogs();
            getGametableCanvas().getPublicMap().clearLines();
            // PacketManager.g_imagelessPogs.clear();

            // send the packet
            while (!conn.isConnected())
            {
            }
            conn.sendPacket(PacketManager.makePlayerPacket(me, m_password));

            PacketSourceState.beginHostDump();

            // and now we're ready to pay attention
            m_netStatus = NETSTATE_JOINED;

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
        Player me = new Player(m_playerName, m_characterName, -1);
        m_players.add(me);
        this.m_myPlayerIndex = 0;
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
        getGametableCanvas().erase(toErase, false, 0);

        repaint();
    }

    public void eraseAllPogs()
    {
        // make an int array of all the IDs
        int removeArray[] = new int[getGametableCanvas().getActiveMap().getNumPogs()];

        for (int i = 0; i < getGametableCanvas().getActiveMap().getNumPogs(); i++)
        {
            Pog pog = getGametableCanvas().getActiveMap().getPogAt(i);
            removeArray[i] = pog.getId();
        }

        getGametableCanvas().removePogs(removeArray);
    }

    public void eraseAll()
    {
        eraseAllLines();
        eraseAllPogs();
    }

    public void updateGridModeMenu()
    {
        if (getGametableCanvas().m_gridMode == getGametableCanvas().m_noGridMode)
        {
            m_noGridModeMenuItem.setState(true);
            m_squareGridModeMenuItem.setState(false);
            m_hexGridModeMenuItem.setState(false);
        }
        else if (getGametableCanvas().m_gridMode == getGametableCanvas().m_squareGridMode)
        {
            m_noGridModeMenuItem.setState(false);
            m_squareGridModeMenuItem.setState(true);
            m_hexGridModeMenuItem.setState(false);
        }
        else if (getGametableCanvas().m_gridMode == getGametableCanvas().m_hexGridMode)
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

        if (getGametableCanvas().isPublicMap())
        {
            m_togglePrivateMapMenuItem.setState(false);
            m_toolButtons[toolId].setEnabled(false);
        }
        else
        {
            m_togglePrivateMapMenuItem.setState(true);
            m_toolButtons[toolId].setEnabled(true);
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
        m_pogPanel.populateChildren();
        getGametableCanvas().repaint();
    }

    /**
     * Toggles between the two layers.
     */
    public void toggleLayer()
    {
        // toggle the map we're on
        if (getGametableCanvas().isPublicMap())
        {
            getGametableCanvas().setActiveMap(getGametableCanvas().getPrivateMap());
        }
        else
        {
            getGametableCanvas().setActiveMap(getGametableCanvas().getPublicMap());
        }

        updatePrivateLayerModeMenuItem();

        // if they toggled the layer, whatever tool they're using is cancelled
        getToolManager().cancelToolAction();
        getGametableCanvas().requestFocus();

        repaint();
    }

    /**
     * Invokes the whole addDieMacro dialog process.
     */
    public void addDieMacro()
    {
        NewMacroDialog dialog = new NewMacroDialog();
        dialog.setVisible(true);

        if (dialog.isAccepted())
        {
            // extrace the macro from the controls and add it
            String name = dialog.getMacroName();
            String macro = dialog.getMacroDefinition();
            if (getMacro(name) != null)
            {
                int result = UtilityFunctions.yesNoDialog(GametableFrame.this, "You already have a macro named \""
                    + name + "\", " + "are you sure you want to replace it with \"" + macro + "\"?", "Replace Macro?");
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

    public ToolManager getToolManager()
    {
        return m_toolManager;
    }

    public void setToolSelected(int toolId)
    {
        m_toolButtons[toolId].setSelected(true);
    }

    public void addPlayer(Player player)
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
        m_macroPanel.refreshMacroList();
    }

    public void removeMacro(String name)
    {
        removeMacroForced(name);
        m_macroPanel.refreshMacroList();
    }

    public void removeMacro(DiceMacro dm)
    {
        removeMacroForced(dm);
        m_macroPanel.refreshMacroList();
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
    }

    private void addMacroForced(DiceMacro macro)
    {
        removeMacroForced(macro.getName());
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

    /**
     * @return Gets the list of macros.
     */
    public Collection getMacros()
    {
        return Collections.unmodifiableCollection(m_macroMap.values());
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
        m_chatLog.addText(text);
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
            logSystemMessage(m_players.size() + " player" + (m_players.size() > 1 ? "s" : DEFAULT_PASSWORD));
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
            postMessage(getMyPlayer().getCharacterName() + " rolls " + rollBuf + ": [" + resultBuf + "] = " + total);
        }
        else if (words[0].equals("/poglist"))
        {
            // macro command. this requires at least 2 parameters
            if (words.length < 2)
            {
                // tell them the usage and bail
                logSystemMessage("/poglist usage: /poglist <attribute name>");
                logSystemMessage("Examples:");
                logSystemMessage("    /poglist HP");
                logSystemMessage("    /poglist Initiative");
                logSystemMessage("Note: attribute names are case, whitespace, and punctuation-insensitive.");
                return;
            }

            String name = UtilityFunctions.stitchTogetherWords(words, 1);
            GametableMap map = m_gametableCanvas.getActiveMap();
            List pogs = map.m_pogs;
            logSystemMessage("Finding pogs with '" + name + "' attribute...");
            int tally = 0;
            for (int i = 0, size = pogs.size(); i < size; ++i)
            {
                Pog pog = (Pog)pogs.get(i);
                String value = pog.getAttribute(name);
                if (value != null && value.length() > 0)
                {
                    String pogText = pog.getText();
                    if (pogText == null || pogText.length() == 0)
                    {
                        pogText = "<unknown>";
                    }

                    pogText += " - ";

                    logSystemMessage("    " + pogText + name + ": " + value);
                    ++tally;
                }
            }
            logSystemMessage(tally + " pog" + (tally != 1 ? "s" : "") + " found.");
        }
        else if (words[0].equals("//") || words[0].equals("/help"))
        {
            // list macro commands
            logSystemMessage("/macro: macro a die roll");
            logSystemMessage("/macrodelete: deletes an unwanted macro");
            logSystemMessage("/roll: roll dice");
            logSystemMessage("/who: lists connected players");
            logSystemMessage("/poglist: lists pogs by attribute");
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

            prefDos.writeUTF(m_playerName);
            prefDos.writeUTF(m_characterName);
            prefDos.writeUTF(m_ipAddress);
            prefDos.writeInt(m_port);
            prefDos.writeUTF(m_password);
            prefDos.writeInt(getGametableCanvas().getPublicMap().getScrollX());
            prefDos.writeInt(getGametableCanvas().getPublicMap().getScrollY());
            prefDos.writeInt(getGametableCanvas().m_zoom);

            prefDos.writeInt(m_windowSize.width);
            prefDos.writeInt(m_windowSize.height);
            prefDos.writeInt(m_windowPos.x);
            prefDos.writeInt(m_windowPos.y);
            prefDos.writeBoolean(m_bMaximized);

            // divider locations
            prefDos.writeInt(m_mapChatSplitPane.getDividerLocation());
            prefDos.writeInt(m_mapPogSplitPane.getDividerLocation());

            Collection macros = m_macroMap.values();
            prefDos.writeInt(macros.size());
            for (Iterator iterator = macros.iterator(); iterator.hasNext();)
            {
                DiceMacro dm = (DiceMacro)iterator.next();
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
        if (!file.exists())
        {
            // DEFAULTS
            m_mapChatSplitPane.setDividerLocation(0.75);
            m_mapPogSplitPane.setDividerLocation(150);
            m_windowSize = new Dimension(800, 600);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            m_windowPos = new Point((screenSize.width - m_windowSize.width) / 2,
                (screenSize.height - m_windowSize.height) / 2);
            m_bMaximized = false;
            applyWindowInfo();
            addMacro("d20", "d20");
            return;
        }

        try
        {
            FileInputStream prefFile = new FileInputStream(file);
            DataInputStream prefDis = new DataInputStream(prefFile);

            m_playerName = prefDis.readUTF();
            m_characterName = prefDis.readUTF();
            m_ipAddress = prefDis.readUTF();
            m_port = prefDis.readInt();
            m_password = prefDis.readUTF();
            getGametableCanvas().setPrimaryScroll(getGametableCanvas().getPublicMap(), prefDis.readInt(),
                prefDis.readInt());
            getGametableCanvas().setZoom(prefDis.readInt());

            m_windowSize = new Dimension(prefDis.readInt(), prefDis.readInt());
            m_windowPos = new Point(prefDis.readInt(), prefDis.readInt());
            m_bMaximized = prefDis.readBoolean();
            applyWindowInfo();

            // divider locations
            m_mapChatSplitPane.setDividerLocation(prefDis.readInt());
            m_mapPogSplitPane.setDividerLocation(prefDis.readInt());

            m_macroMap.clear();
            int numMacros = prefDis.readInt();
            for (int i = 0; i < numMacros; i++)
            {
                DiceMacro dm = new DiceMacro();
                dm.initFromStream(prefDis);
                addMacro(dm);
            }

            prefDis.close();
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
            byte gridModePacket[] = PacketManager.makeGridModePacket(getGametableCanvas().getGridModeID());
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

    private void tick()
    {
        long now = System.currentTimeMillis();
        long diff = now - m_lastTickTime;
        if (m_lastTickTime == 0)
        {
            diff = 0;
        }
        m_lastTickTime = now;
        tick(diff);
    }

    private void tick(long ms)
    {
        // System.out.println("tick(" + ms + ")");
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

            if (SEND_PINGS)
            {
                m_lastPingTime += ms;
                if (m_lastPingTime >= PING_INTERVAL)
                {
                    send(PacketManager.makePingPacket());
                    m_lastPingTime -= PING_INTERVAL;
                }
            }
        }
        m_gametableCanvas.tick(ms);
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
                tick();
            }
        });
        m_executorThread.start();
    }

    /**
     * 
     */
    private void saveAll()
    {
        saveState(getGametableCanvas().getPublicMap(), new File("autosave.grm"));
        saveState(getGametableCanvas().getPrivateMap(), new File("autosavepvt.grm"));
        savePrefs();
    }
}
