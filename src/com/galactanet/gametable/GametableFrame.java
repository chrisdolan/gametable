/*
 * GametableFrame.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.galactanet.gametable.chat.ChatPane;
import com.galactanet.gametable.net.Connection;
import com.galactanet.gametable.net.NetworkThread;
import com.galactanet.gametable.net.Packet;
import com.galactanet.gametable.prefs.PreferenceDescriptor;
import com.galactanet.gametable.prefs.Preferences;



/**
 * The main Gametable Frame class.
 * This class handles the display of the application objects and the response to user input.
 * The Main Content Pane contains the following control hierarchy:
 * MainContentPane:
 *  |- m_toolBar
 *  |- m_mapPogSplitPane
 *  |- m_status
 *  
 *  m_toolBar
 *  |- m_colorCombo
 *  |- buttons in the toolbar
 *  
 *  m_mapPogSplitPane
 *  |- m_pogsTabbedPane
 *  |   |- m_pogPanel
 *  |   |- m_activePogsPanel
 *  |   |- m_macroPanel
 *  |- m_mapChatSplitPlane
 *      |- m_canvasPane
 *      |   |- m_gametableCanvas
 *      |- m_chatPanel
 *          |-m_textAreaPanel
 *              |- m_textAndEntryPanel
 *              |- m_newChatLog
 *              |- entryPanel
 *                 |- StyledEntryToolbar
 *                     |- m_textEntry
 * 
 * @author sephalon
 */
public class GametableFrame extends JFrame implements ActionListener
{
    /**
     * This class provides a mechanism to store the active tool in the gametable canvas
     */
    class ToolButtonAbstractAction extends AbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 6185807427550145052L;
       
        int                       m_id;     // Which user triggers this action
        
        /**
         * Constructor
         * @param id Id from the control triggering this action
         */
        ToolButtonAbstractAction(final int id)
        {
            m_id = id;
        }

        public void actionPerformed(final ActionEvent event)
        {
            if (getFocusOwner() instanceof JTextField)
            {
                return;     // A JTextField is not an active tool.
                            // No need to save the active tool in the gametable canvas
            }
            getGametableCanvas().setActiveTool(m_id); // In any other case, save the active tool in the gametable canvas
        }
    }

    /**
     * Action listener for tool buttons.
     */
    class ToolButtonActionListener implements ActionListener
    {
        int m_id;

        ToolButtonActionListener(final int id)
        {
            m_id = id;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e)
        {
            getGametableCanvas().setActiveTool(m_id);
        }
    }

    /**
     * Array of standard colors used
     */
    public final static Integer[] COLORS                   = {
        new Integer(new Color(0, 0, 0).getRGB()), new Integer(new Color(198, 198, 198).getRGB()),
        new Integer(new Color(0, 0, 255).getRGB()), new Integer(new Color(0, 255, 0).getRGB()),
        new Integer(new Color(0, 255, 255).getRGB()), new Integer(new Color(255, 0, 0).getRGB()),
        new Integer(new Color(255, 0, 255).getRGB()), new Integer(new Color(255, 255, 0).getRGB()),
        new Integer(new Color(255, 255, 255).getRGB()), new Integer(new Color(0, 0, 132).getRGB()),
        new Integer(new Color(0, 132, 0).getRGB()), new Integer(new Color(0, 132, 132).getRGB()),
        new Integer(new Color(132, 0, 0).getRGB()), new Integer(new Color(132, 0, 132).getRGB()),
        new Integer(new Color(132, 132, 0).getRGB()), new Integer(new Color(132, 132, 132).getRGB())
                                                           };

    /**
     * The version of the communications protocol used by this build. This needs to change whenever an incompatibility
     * arises between versions.
     */
    public final static int       COMM_VERSION             = 15;

    private final static boolean  DEBUG_FOCUS              = false;

    /**
     * Default Character name for when there is no prefs file.
     */
    private static final String   DEFAULT_CHARACTER_NAME   = "Anonymous";
    /**
     * Default password for when there is no prefs file.
     */
    private static final String   DEFAULT_PASSWORD         = "";

    public final static int       DEFAULT_PORT             = 6812;
    /**
     * Default server for when there is no prefs file.
     */
    private static final String   DEFAULT_SERVER           = "localhost";

    /**
     * Xml strings with a font definitions, standard tags for messages
     */
    public final static String    ALERT_MESSAGE_FONT       = "<b><font color=\"#FF0000\">";
    public final static String    END_ALERT_MESSAGE_FONT   = "</b></font>";

    public final static String    DIEROLL_MESSAGE_FONT     = "<b><font color=\"#990022\">";
    public final static String    END_DIEROLL_MESSAGE_FONT = "</b></font>";

    public final static String    EMOTE_MESSAGE_FONT       = "<font color=\"#004477\">";
    public final static String    END_EMOTE_MESSAGE_FONT   = "</font>";

    public final static String    PRIVATE_MESSAGE_FONT     = "<font color=\"#009900\">";
    public final static String    END_PRIVATE_MESSAGE_FONT = "</font>";

    public final static String    SAY_MESSAGE_FONT         = "<font color=\"#007744\">";
    public final static String    END_SAY_MESSAGE_FONT     = "</font>";

    public final static String    SYSTEM_MESSAGE_FONT      = "<font color=\"#666600\">";
    public final static String    END_SYSTEM_MESSAGE_FONT  = "</font>";

    /**
     * The global gametable instance.
     */
    private static GametableFrame g_gametableFrame;
    
    /**
     * Constants for Net status
     */
    public final static int       NETSTATE_HOST            = 1;
    public final static int       NETSTATE_JOINED          = 2;
    public final static int       NETSTATE_NONE            = 0;

    public final static int       PING_INTERVAL            = 2500;

    public final static int       REJECT_INVALID_PASSWORD  = 0;
    public final static int       REJECT_VERSION_MISMATCH  = 1;

    private final static boolean  SEND_PINGS               = true;
    private final static boolean  USE_NEW_CHAT_PANE        = true;

    /**
     * 
     */
    private static final long     serialVersionUID         = -1997597054204909759L;

    /**
     * @return The global GametableFrame instance.
     */
    public static GametableFrame getGametableFrame()
    {
        return g_gametableFrame; // TODO: This could always return this, making g_gametableFrame unnecessary
    }

    public double                   grid_multiplier          = 5.0;
    public String                   grid_unit                = "ft";

    // The current file path used by save and open.
    // NULL if unset.
    private static File             m_mapExportSaveFolder = null;

    // files for the public map, the private map, and the die macros
    public File                     m_actingFileMacros;
    public File                     m_actingFilePrivate;
    public File                     m_actingFilePublic;

    
    private boolean                 m_bMaximized;   // Is the frame maximized?

    // all the cards you have
    private final List              m_cards                  = new ArrayList();
    public String                   m_characterName          = DEFAULT_CHARACTER_NAME; // The character name
    
    



    // only valid if this client is the host
    private final List              m_decks                  = new ArrayList(); // List of decks

    private JMenuItem               m_disconnectMenuItem;

    public Color                    m_drawColor              = Color.BLACK;

    private PeriodicExecutorThread  m_executorThread;
 
    JComboBox                       m_gridunit;             // ComboBox for grid units
    /*
     * Added variables below in order to accomodate grid unit multiplier
     */
    JTextField                      m_gridunitmultiplier;
    private final JCheckBoxMenuItem m_hexGridModeMenuItem    = new JCheckBoxMenuItem("Hex Grid");

    private JMenuItem               m_hostMenuItem;
    public String                   m_ipAddress              = DEFAULT_SERVER;
    private JMenuItem               m_joinMenuItem;
    private long                    m_lastPingTime           = 0;

    // the name of the last person who sent a private message
    public String                   m_lastPrivateMessageSender;
    private long                    m_lastTickTime           = 0;
    private final Map               m_macroMap               = new TreeMap();
    
    
 
   

    // which player I am
    private int                     m_myPlayerIndex;

    private int                     m_netStatus              = NETSTATE_NONE;

    private volatile NetworkThread  m_networkThread;

    // the id that will be assigned to the next player to join
    public int                      m_nextPlayerId;

    // the id that will be assigned to the change made
    public int                      m_nextStateId;
    private final JCheckBoxMenuItem m_noGridModeMenuItem     = new JCheckBoxMenuItem("No Grid");

    public String                   m_password               = DEFAULT_PASSWORD;
    public String                   m_playerName             = System.getProperty("user.name");

    private List                    m_players                = new ArrayList();

    private JFrame                  pogWindow                = null;
    private JFrame                  chatWindow               = null;
    private boolean                 b_pogWindowDocked        = true;
    private boolean                 b_chatWindowDocked       = true;

    private PogLibrary              m_pogLibrary             = null;
    
    

    public int                      m_port                   = DEFAULT_PORT;
    private final Preferences       m_preferences            = new Preferences();

    private final JCheckBox         m_showNamesCheckbox      = new JCheckBox("Show pog names");
    private final JCheckBoxMenuItem m_squareGridModeMenuItem = new JCheckBoxMenuItem("Square Grid");
    

    
 
 
    private JCheckBoxMenuItem       m_togglePrivateMapMenuItem;

    

    private final ButtonGroup       m_toolButtonGroup        = new ButtonGroup();

    private JToggleButton           m_toolButtons[]          = null;

    private final ToolManager       m_toolManager            = new ToolManager();
    private final List              m_typing                 = new ArrayList();
    // window size and position
    private Point                   m_windowPos;
    private Dimension               m_windowSize;
    
    
    // Controls in the Frame
    // The toolbar goes at the top of the pane
    private final JToolBar          m_toolBar                = new JToolBar(); // The main toolbar
    private final JComboBox         m_colorCombo             = new JComboBox(COLORS); // Combo box for colore
    
    // The map-pog split pane goes in the center
    private final JSplitPane        m_mapPogSplitPane        = new JSplitPane();    // Split between Pog pane and map pane
    private final JTabbedPane       m_pogsTabbedPane         = new JTabbedPane();   // The Pog pane is tabbed
    private PogPanel                m_pogPanel               = null;                // one tab is the Pog Panel
    private ActivePogsPanel         m_activePogsPanel        = null;                // another tab is the Active Pogs Panel
    private MacroPanel              m_macroPanel             = null;                // the last tab is the macro panel
    
    private final JSplitPane        m_mapChatSplitPane       = new JSplitPane();    // The map pane is really a split between the map
                                                                                    // and the chat pane
    private final JPanel            m_canvasPane             = new JPanel(new BorderLayout()); // This is the pane containing the map
    private final GametableCanvas   m_gametableCanvas        = new GametableCanvas(); // This is the map
    private final JPanel            m_chatPanel              = new JPanel(); // Panel for chat
    private final JPanel            m_textAreaPanel          = new JPanel();
    private final JPanel            m_textAndEntryPanel      = new JPanel();
    private final ChatLogPane       m_chatLog                = (USE_NEW_CHAT_PANE ? null : new ChatLogPane()); // This seems like always is null?
    private final ChatPane          m_newChatLog             = (USE_NEW_CHAT_PANE ? new ChatPane() : null); //This is always set to a new ChatPane
    private final ChatLogEntryPane  m_textEntry              = new ChatLogEntryPane(this);
    
    // The status goes at the bottom of the pane
    private final JLabel            m_status                 = new JLabel(" "); // Status Bar
    

    /**
     * Construct the frame
     */
    public GametableFrame()
    {
        g_gametableFrame = this;

        try
        {
            initialize(); // Create the menu, controls, etc.
        }
        catch (final Exception e)
        {
            Log.log(Log.SYS, e);
        }
    }

    /**
     * actionPerformed is an event handler for some of the controls in the frame
     */
    public void actionPerformed(final ActionEvent e)
    {
        /*
         * Added in order to accomodate grid unit multiplier
         */
        if (e.getSource() == m_gridunit)
        {
            // If the event is triggered by the grid unit drop down,
            // get the selected unit
            grid_unit = (String)(m_gridunit.getSelectedItem());
        }

        if (e.getSource() == m_colorCombo)
        {
            // If the event is triggered by the color drow down,
            // Get the selected color
            final Integer col = (Integer)m_colorCombo.getSelectedItem();
            m_drawColor = new Color(col.intValue());
        }
        else if (e.getSource() == m_noGridModeMenuItem)
        {
            // If the event is triggered by the "No Grid Mode" menu item then
            // remove the grid from the canvas
            // Set the Gametable canvas in "No Grid" mode
            getGametableCanvas().m_gridMode = getGametableCanvas().m_noGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_NONE));
            // Check an uncheck menu items
            updateGridModeMenu();
            // Repaint the canvas
            getGametableCanvas().repaint();
            // Notify other players
            postSystemMessage(getMyPlayer().getPlayerName() + " changes the grid mode.");
        }
        else if (e.getSource() == m_squareGridModeMenuItem)
        {
            // If the event is triggered by the "Square Grid Mode" menu item, 
            // adjust the canvas accordingly
            // Set the Gametable canvas in "Square Grid mode"
            getGametableCanvas().m_gridMode = getGametableCanvas().m_squareGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_SQUARES));
            // Check and uncheck menu items
            updateGridModeMenu();
            // Repaint the canvas
            getGametableCanvas().repaint();
            // Notify other players
            postSystemMessage(getMyPlayer().getPlayerName() + " changes the grid mode.");
        }
        else if (e.getSource() == m_hexGridModeMenuItem)
        {
            // If the event is triggered by the "Hex Grid Mode" menu item,
            // adjust the canvas accordingly
            // Set the Gametable canvas in "Hex Grid Mode"
            getGametableCanvas().m_gridMode = getGametableCanvas().m_hexGridMode;
            send(PacketManager.makeGridModePacket(GametableCanvas.GRID_MODE_HEX));
            // Check and uncheck menu items
            updateGridModeMenu();
            // Repaint the canvas
            getGametableCanvas().repaint();
            // Notify other players
            postSystemMessage(getMyPlayer().getPlayerName() + " changes the grid mode.");
        }
    }

    /**
     * Invokes the addDieMacro dialog process.
     */
    public void addDieMacro()
    {
        // Create and display add macro dialog
        final NewMacroDialog dialog = new NewMacroDialog();
        dialog.setVisible(true);

        // If the user accepted the dialog (closed with Ok)
        if (dialog.isAccepted())
        {
            // extract the macro from the controls and add it
            final String name = dialog.getMacroName();
            final String macro = dialog.getMacroDefinition();
            if (getMacro(name) != null) // if there is a macro with that name
            {
                // Confirm that the macro will be replaced
                final int result = UtilityFunctions.yesNoDialog(GametableFrame.this,
                    "You already have a macro named \"" + name + "\", " + "are you sure you want to replace it with \""
                        + macro + "\"?", "Replace Macro?");
                if (result == UtilityFunctions.YES)
                {
                    addMacro(name, macro);
                }
            }
            else // if there is no macro with that name, then add it.
            {
                addMacro(name, macro);
            }
        }
    }

    // --- Menus ---

    /**
     * adds a macro and refresh the list of available macros in the screen
     */
    public void addMacro(final DiceMacro dm)
    {
        addMacroForced(dm); // adds the macro to the collection of macros
        m_macroPanel.refreshMacroList(); // refresh the display of macro list
    }

    /**
     * creates and adds a macro, given its name and code
     * @param name name of the macro
     * @param macro macro content, the code of the macro
     */
    public void addMacro(final String name, final String macro)
    {
        final DiceMacro newMacro = new DiceMacro(); // creates a macro object
        boolean res = newMacro.init(macro, name); // initializes the macro with its name and code
        if (!res) // if the macro creation failed, log the error and exit
        {
            logAlertMessage("Error in macro");
            return;
        }
        addMacro(newMacro); //add the macro to the collection
    }

    /**
     * adds a macro to the collection, replacing any macro with the same name
     * @param macro macro being added
     */
    private void addMacroForced(final DiceMacro macro)
    {
        removeMacroForced(macro.getName()); // remove any macro with the same name
        m_macroMap.put(UtilityFunctions.normalizeName(macro.getName()), macro); // store the macro in the macro map making
                                                                                // sure conforms to java identifier rules, this is
                                                                                // it eliminates special characters from the name
    }

    /**
     * adds a player to the player list
     * @param player
     */
    public void addPlayer(final Player player)
    {
        m_players.add(player);
    }

    /**
     * handles a new pog packet
     * @param pog the Pog received
     * @param bPublicLayerPog currently ignored
     */
    public void addPogPacketReceived(final Pog pog, final boolean bPublicLayerPog)
    {

        // getGametableCanvas().doAddPog(pog, bPublicLayerPog);
        /*
         * Changed by Rizban Changed to publish to active map rather than public map. 
         * TODO: For some reason, all saved pogs are saved with data saying they are on the 
         * public map, regardless of which map they were on when saved. Check to see if there 
         *  is a reason for saving pogs with this information, if not, remove all instances.
         */
        // add the pog to the canvas indicating if the active map is the public map
        getGametableCanvas().doAddPog(pog,
            (getGametableCanvas().getActiveMap() == getGametableCanvas().getPublicMap() ? true : false));

        // update the next pog id if necessary
        if (pog.getId() >= Pog.g_nextId)
        {
            Pog.g_nextId = pog.getId() + 5;
        }

        // update the next pog id if necessary
        if (pog.getSortOrder() >= Pog.g_nextSortId)
        {
            Pog.g_nextSortId = pog.getSortOrder() + 1;
        }

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeAddPogPacket(pog));
        }
    }

    /**
     * Updates the frame size and position based on the preferences stored.
     */
    public void applyWindowInfo()
    {
        final Point locCopy = new Point(m_windowPos);
        setSize(m_windowSize);
        m_windowPos = locCopy; // this copy and use of a local variable seems unnecessary
        setLocation(locCopy);
        if (m_bMaximized) // if the preferences says the screen should be maximized
        {
            setExtendedState(MAXIMIZED_BOTH); // Maximize screen
        }
        else
        {
            setExtendedState(NORMAL); // Otherwise, set the screen to normal
        }
    }

    /**
     * clear all cards of a given deck
     * @param deckName name of the deck whose cards will be deleted
     */
    public void clearDeck(final String deckName)
    {
        // if you're the host, send out the packet to tell everyone to
        // clear their decks. If you're a joiner, don't. Either way
        // clear out your own hand of the offending cards
        if (m_netStatus == NETSTATE_HOST)
        {
            send(PacketManager.makeClearDeckPacket(deckName));
        }

        for (int i = 0; i < m_cards.size(); i++) //for each card
        {
            final DeckData.Card card = (DeckData.Card)m_cards.get(i);
            if (card.m_deckName.equals(deckName)) // if it belongs to the deck to erase
            {
                // this card has to go.
                m_cards.remove(i); // remove the card
                i--; // to keep up with the changed list
            }
        }
    }

    /**
     * grumpy function that throws an exception if we are not the host of a network game
     * @throws IllegalStateException
     */
    public void confirmHost() throws IllegalStateException
    {
        if (m_netStatus != NETSTATE_HOST)
        {
            throw new IllegalStateException("confirmHost failure");
        }
    }

    // --- MenuItems ---
    
    /**
     * throws an exception is the current status is not NETSTATE_JOINED
     * @throws IllegalStateException
     */
    public void confirmJoined() throws IllegalStateException
    {
        if (m_netStatus != NETSTATE_JOINED)
        {
            throw new IllegalStateException("confirmJoined failure");
        }
    }

    /**
     * handles a drop of the network connection
     * @param conn network connection
     */
    public void connectionDropped(final Connection conn)
    {
        if (m_netStatus == NETSTATE_JOINED) // if we were connected before
        {
            // we lost our connection to the host
            logAlertMessage("Your connection to the host was lost.");
            disconnect(); // do any disconnection processing

            m_netStatus = NETSTATE_NONE; // change the status to reflect we are not connected
            return;
        }

        // find the player who owns that connection
        final Player dead = getPlayerFromConnection(conn);
        if (dead != null) // if we found the player
        {
            // remove this player
            m_players.remove(dead);
            sendCastInfo(); //send updated list of players
            // notify other users
            postSystemMessage(dead.getPlayerName() + " has left the session");
        }
        else // if we didn't find the player then the connection failed while login in
        {
            postAlertMessage("Someone tried to log in, but was rejected.");
        }
    }

    /**
     * interprets and execute the deck commands
     * @param words array of words in the deck command
     */
    public void deckCommand(final String[] words)
    {
        // we need to be in a network game to issue deck commands
        // otherwise log the error and exit
        if (m_netStatus == NETSTATE_NONE)
        {
            logAlertMessage("You must be in a session to use /deck commands.");
            return;
        }

        // words[0] will be "/deck". IF it weren't we wouldn't be here.
        if (words.length < 2)
        {
            // they just said "/deck". give them the help text and return
            showDeckUsage();
            return;
        }

        final String command = words[1]; // since words[0] is the word "deck" we are interested in the
                                         // next word, that's why we take words[1]

        if (command.equals("create")) // create a new deck
        {
            if (m_netStatus != NETSTATE_HOST) // verify that we are the host of the network game
            {
                logAlertMessage("Only the host can create a deck.");
                return;
            }

            // create a new deck.
            if (words.length < 3) // we were expecting the deck name
            {
                // not enough parameters
                showDeckUsage();
                return;
            }

            final String deckFileName = words[2];
            String deckName;
            if (words.length == 3)
            {
                // they specified the deck, but not a name. So we
                // name it after the type
                deckName = deckFileName;
            }
            else
            {
                // they specified a name
                deckName = words[3];
            }

            // if the name is already in use, puke out an error
            if (getDeck(deckName) != null)
            {
                logAlertMessage("Error - There is already a deck named '" + deckName + "'.");
                return;
            }

            // create the deck stored in an xml file
            final DeckData dd = new DeckData();
            final File deckFile = new File("decks" + UtilityFunctions.LOCAL_SEPARATOR + deckFileName + ".xml");
            boolean result = dd.init(deckFile);

            if (!result)
            {
                logAlertMessage("Could not create the deck.");
                return;
            }

            // create a deck and add it
            final Deck deck = new Deck();
            deck.init(dd, 0, deckName);
            m_decks.add(deck);

            // alert all players that this deck has been created
            sendDeckList();
            postSystemMessage(getMyPlayer().getPlayerName() + " creates a new " + deckFileName + " deck named "
                + deckName);

        }
        else if (command.equals("destroy")) // remove a deck
        {
            if (m_netStatus != NETSTATE_HOST)
            {
                logAlertMessage("Only the host can destroy a deck.");
                return;
            }

            if (words.length < 3)
            {
                // they didn't specify a deck
                showDeckUsage();
                return;
            }

            // remove the deck named words[2]
            final String deckName = words[2];
            final int toRemoveIdx = getDeckIdx(deckName); // get the position of the deck in the deck list

            if (toRemoveIdx != -1) // if we found the deck
            {
                // we can successfully destroy the deck
                m_decks.remove(toRemoveIdx);

                // tell the players
                clearDeck(deckName);
                sendDeckList();
                postSystemMessage(getMyPlayer().getPlayerName() + " destroys the deck named " + deckName);
            }
            else
            {
                // we couldn't find a deck with that name
                logAlertMessage("There is no deck named '" + deckName + "'.");
            }
        }
        else if (command.equals("shuffle")) // shuffle the deck
        {
            if (m_netStatus != NETSTATE_HOST) // only if you are the host
            {
                logAlertMessage("Only the host can shuffle a deck.");
                return;
            }

            if (words.length < 4)
            {
                // not enough parameters
                showDeckUsage();
                return;
            }

            final String deckName = words[2];
            final String operation = words[3];

            // first get the deck
            final Deck deck = getDeck(deckName);
            if (deck == null)
            {
                // and report the error if not found
                logAlertMessage("There is no deck named '" + deckName + "'.");
                return;
            }

            if (operation.equals("all"))
            {
                // collect and shuffle all the cards in the deck.
                clearDeck(deckName); // let the other players know about the demise of those cards
                deck.shuffleAll();
                postSystemMessage(getMyPlayer().getPlayerName() + " collects all the cards from the " + deckName
                    + " deck from all players and shuffles them.");
                postSystemMessage(deckName + " has " + deck.cardsRemaining() + " cards.");
            }
            else if (operation.equals("discards"))
            {
                // shuffle only the cards in the discard pile.
                deck.shuffle();
                postSystemMessage(getMyPlayer().getPlayerName() + " shuffles the discards back into the " + deckName
                    + " deck.");
                postSystemMessage(deckName + " has " + deck.cardsRemaining() + " cards.");
            }
            else
            {
                // the shuffle operation is illegal
                logAlertMessage("'" + operation
                    + "' is not a valid type of shuffle. This parameter must be either 'all' or 'discards'.");
                return;
            }
        }
        else if (command.equals("draw")) // draw a card from the deck
        {
            // before chesking net status we check to see if the draw command was
            // legally done
            if (words.length < 3)
            {
                // not enough parameters
                showDeckUsage();
                return;
            }

            // ensure that desired deck exists -- this will work even if we're not the
            // host. Because we'll have "dummy" decks in place to track the names
            final String deckName = words[2];
            final Deck deck = getDeck(deckName);
            if (deck == null)
            {
                // that deck doesn't exist
                logAlertMessage("There is no deck named '" + deckName + "'.");
                return;
            }

            int numToDraw = 1;
            // they optionally can specify a number of cards to draw
            if (words.length >= 4)
            {
                // numToDrawStr is never used.
                // String numToDrawStr = words[3];

                // note the number of cards to draw
                try
                {
                    numToDraw = Integer.parseInt(words[3]);
                    if (numToDraw <= 0)
                    {
                        // not allowed
                        throw new Exception();
                    }
                }
                catch (final Exception e)
                {
                    // it's ok not to specify a number of cards to draw. It's not
                    // ok to put garbage in that field
                    logAlertMessage("'" + words[3] + "' is not a valid number of cards to draw");
                }
            }

            drawCards(deckName, numToDraw);
        }
        else if (command.equals("hand")) // this shows the cards in our hand
        {
            if (m_cards.size() == 0)
            {
                logSystemMessage("You have no cards");
                return;
            }

            
            logSystemMessage("You have " + m_cards.size() + " cards:");
            
            for (int i = 0; i < m_cards.size(); i++) // for each card
            {
                final int cardIdx = i + 1;
                final DeckData.Card card = (DeckData.Card)m_cards.get(i); // get the card
                // craft a message
                final String toPost = "" + cardIdx + ": " + card.m_cardName + " (" + card.m_deckName + ")";
                // log the message
                logSystemMessage(toPost);
            }
        }
        else if (command.equals("discard")) // discard a card
        {
            // discard the nth card from your hand
            // 1-indexed
            if (words.length < 3)
            {
                // note enough parameters
                showDeckUsage();
                return;
            }

            final String param = words[2];

            // the parameter can be "all" or a number
            DeckData.Card discards[];
            if (param.equals("all"))
            {
                // discard all cards
                discards = new DeckData.Card[m_cards.size()];

                for (int i = 0; i < discards.length; i++)
                {
                    discards[i] = (DeckData.Card)m_cards.get(i);
                }
            }
            else
            {
                // discard the specified card
                int idx = -1;
                try
                {
                    idx = Integer.parseInt(param);
                    idx--; // make it 0-indexed

                    if (idx < 0) // we can't discard a card with a negative index
                    {
                        throw new Exception();
                    }

                    if (idx >= m_cards.size()) // we can't discard a card higher than what we have
                    {
                        throw new Exception();
                    }
                }
                catch (final Exception e)
                {
                    // they put in some illegal value for the param
                    logAlertMessage("There is no card '" + param + "'.");
                    return;
                }
                discards = new DeckData.Card[1];
                discards[0] = (DeckData.Card)m_cards.get(idx);
            }

            // now we have the discards[] filled with the cards to be
            // removed
            discardCards(discards);
        }
        else if (command.equals("decklist"))
        {
            // list off the decks
            // we keep "dummy" decks for joiners,
            // so either a host of a joiner is safe to use this code:
            if (m_decks.size() == 0)
            {
                logSystemMessage("There are no decks");
                return;
            }

            logSystemMessage("There are " + m_decks.size() + " decks");
            for (int i = 0; i < m_decks.size(); i++)
            {
                final Deck deck = (Deck)m_decks.get(i);
                logSystemMessage("---" + deck.m_name);
            }
        }
        else
        {
            // they selected a deck command that doesn't exist
            showDeckUsage();
        }
    }

    /**
     * handles the reception of a list of decks
     * @param deckNames array of string with the names of decks received
     */
    public void deckListPacketReceived(final String[] deckNames)
    {
        // if we're the host, this is a packet we should never get
        if (m_netStatus == NETSTATE_HOST)
        {
            throw new IllegalStateException("Host received deckListPacket.");
        }

        // set up out bogus decks to have the appropriate names
        m_decks.clear();

        for (int i = 0; i < deckNames.length; i++)
        {
            final Deck bogusDeck = new Deck();
            bogusDeck.initPlaceholderDeck(deckNames[i]);
            m_decks.add(bogusDeck);
        }
    }

    /**
     * remove cards from our deck
     * @param discards array of cards to discard
     */
    public void discardCards(final DeckData.Card discards[])
    {
        if (m_netStatus == NETSTATE_JOINED)
        {
            // if we are not the host we have bogus decks, so we send a package to
            // notify of the discards. It will be processed by the host
            send(PacketManager.makeDiscardCardsPacket(getMyPlayer().getPlayerName(), discards));
        }
        else if (m_netStatus == NETSTATE_HOST)
        {
            // we are the host, so we can process the discard of the cards
            doDiscardCards(getMyPlayer().getPlayerName(), discards);
        }

        // and in either case, we remove the cards from ourselves.
        // TODO: This is a very expensive algorithm, it would be better to get
        // to the card to remove directly by index or something like that.
        for (int i = 0; i < m_cards.size(); i++) // for each card we have
        {
            final DeckData.Card handCard = (DeckData.Card)m_cards.get(i); 
            for (int j = 0; j < discards.length; j++) // compare with each card to discard
            {
                if (handCard.equals(discards[j])) // if they are the same
                {
                    // we need to dump this card
                    m_cards.remove(i);
                    i--; // to keep up with the iteration
                    break;
                }
            }
        }
    }

    /**
     * disconnect from the network game, if connected
     */
    public void disconnect()
    {
        if (m_netStatus == NETSTATE_NONE)
        {
            logAlertMessage("Nothing to disconnect from.");
            return;
        }

        if (m_networkThread != null)
        {
            // stop the network thread
            m_networkThread.interrupt();
            m_networkThread = null;
        }

        m_hostMenuItem.setEnabled(true); // enable the menu item to host a game
        m_joinMenuItem.setEnabled(true); // enable the menu item to join an existing game
        m_disconnectMenuItem.setEnabled(false); // disable the menu item to disconnect from the game 

        // make me the only player in the game
        m_players = new ArrayList();
        final Player me = new Player(m_playerName, m_characterName, -1);
        m_players.add(me);
        m_myPlayerIndex = 0;
        setTitle(GametableApp.VERSION);

        // we might have disconnected during initial data receipt
        PacketSourceState.endHostDump();

        m_netStatus = NETSTATE_NONE;
        logSystemMessage("Disconnected.");
        updateStatus();
    }

    /**
     * discards cards from a deck
     * @param playerName who is discarding the cards
     * @param discards array of cards to discard
     */
    public void doDiscardCards(final String playerName, final DeckData.Card discards[])
    {
        if (discards.length == 0) // nothing to discard
        {
            // this shouldn't happen, but let's not freak out.
            return;
        }

        // only the host should get this
        if (m_netStatus != NETSTATE_HOST)
        {
            throw new IllegalStateException("doDiscardCards should only be done by the host.");
        }

        // tell the decks about the discarded cards
        for (int i = 0; i < discards.length; i++)
        {
            final String deckName = discards[i].m_deckName;
            final Deck deck = getDeck(deckName);
            if (deck == null)
            {
                // don't panic. Just ignore it. It probably means
                // a player discarded a card right as the host deleted the deck
            }
            else
            {
                deck.discard(discards[i]);
            }
        }

        // finally, remove any card pogs that are hanging around based on these cards
        m_gametableCanvas.removeCardPogsForCards(discards);

        // tell everyone about the cards that got discarded
        if (discards.length == 1)
        {
            postSystemMessage(playerName + " discards: " + discards[0].m_cardName);
        }
        else
        {
            postSystemMessage(playerName + " discards " + discards.length + " cards.");
            for (int i = 0; i < discards.length; i++)
            {
                postSystemMessage("---" + discards[i].m_cardName);
            }
        }
    }

    /**
     * draw cards from a deck. Non-host players request it from the host. The
     * host is the one that actually draws from the deck
     * @param deckName deck to draw cards from
     * @param numToDraw how many cards are requested
     */
    public void drawCards(final String deckName, final int numToDraw)
    {
        if (m_netStatus == NETSTATE_JOINED)
        {
            // joiners send a request for cards
            send(PacketManager.makeRequestCardsPacket(deckName, numToDraw));
            return;
        }

        // if we're here, we're the host. So we simply draw the cards
        // and give it to ourselves.
        final DeckData.Card drawnCards[] = getCards(deckName, numToDraw);
        if (drawnCards != null)
        {
            receiveCards(drawnCards);

            // also, we need to add the desired cards to our own private layer
            for (int i = 0; i < drawnCards.length; i++)
            {
                final Pog cardPog = makeCardPog(drawnCards[i]);
                if (cardPog != null)
                {
                    // add this pog card to our own private layer
                    m_gametableCanvas.addCardPog(cardPog);
                }
            }
        }
    }

    /**
     * erases everything from the game canvas
     */
    public void eraseAll()
    {
        eraseAllLines();
        eraseAllPogs();
    }

    /**
     * erases the canvas
     */
    public void eraseAllLines()
    {
        // erase with a rect big enough to nail everything
        final Rectangle toErase = new Rectangle();

        toErase.x = Integer.MIN_VALUE / 2;
        toErase.y = Integer.MIN_VALUE / 2;
        toErase.width = Integer.MAX_VALUE;
        toErase.height = Integer.MAX_VALUE;

        // go to town
        getGametableCanvas().erase(toErase, false, 0);

        repaint();
    }

    /**
     * erases all pogs, also clearing the array of active pogs
     */
    public void eraseAllPogs()
    {
        // make an int array of all the IDs
        final int removeArray[] = new int[getGametableCanvas().getActiveMap().getNumPogs()];

        for (int i = 0; i < getGametableCanvas().getActiveMap().getNumPogs(); i++)
        {
            final Pog pog = getGametableCanvas().getActiveMap().getPog(i);
            removeArray[i] = pog.getId();
        }

        getGametableCanvas().removePogs(removeArray, true);
    }

    /**
     * handles a packet to erase part of the canvas
     * @param r the area to erase
     * @param bColorSpecific erase it by painting it on a color
     * @param color color to erase the area
     * @param authorID who request the erase operation
     * @param state 
     */
    public void erasePacketReceived(final Rectangle r, final boolean bColorSpecific, final int color,
        final int authorID, final int state)
    {
        int stateId = state;
        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            // and give it a genuine state ID first
            stateId = getNewStateId();
            send(PacketManager.makeErasePacket(r, bColorSpecific, color, authorID, stateId));
        }

        // erase the lines
        getGametableCanvas().doErase(r, bColorSpecific, color, authorID, stateId);
    }

    /**
     * return a macro by name. If not found, then create a new one
     * @param term name of the macro to return
     * @return an existing macro or a newly created one if not found
     */
    public DiceMacro findMacro(final String term)
    {
        final String name = UtilityFunctions.normalizeName(term); // remove special characters from the name
        DiceMacro macro = getMacro(name);
        if (macro == null) // if no macro by that name
        {
            macro = new DiceMacro(); // create a new macro
            if (!macro.init(term, null)) // assign the name to it, but no macro code
            {
                macro = null; // if something went wrong, return null
            }
        }

        return macro;
    }

    /**
     * creates the "About" menu item
     * @return the menu item
     */
    private JMenuItem getAboutMenuItem()
    {
        final JMenuItem item = new JMenuItem("About"); // creates a menu item with the "About" label
        item.setAccelerator(KeyStroke.getKeyStroke("F1")); // assign a shortcut
        item.addActionListener(new ActionListener() // when the user selects it this is what happens
        {
            public void actionPerformed(final ActionEvent e)
            {
                // show the about message
                UtilityFunctions.msgBox(GametableFrame.this, GametableApp.VERSION
                    + " by the Gametable Community\n"
                    + "Orignal program by Andy Weir and David Ghandehari", "Version");
            }
        });
        return item;
    }

    /**
     * creates the "Add macro" menu item
     * @return the menu item
     */
    private JMenuItem getAddDiceMenuItem()
    {
        final JMenuItem item = new JMenuItem("Add macro...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                addDieMacro(); // this calls the function to add a new macro
            }
        });
        return item;
    }

    /**
     * get a number of cards from a deck
     * @param deckName name of the deck to draw from
     * @param num number of cards to draw
     * @return array with the cards drawn
     */
    public DeckData.Card[] getCards(final String deckName, final int num)
    {
        int numCards = num;

        // get the deck
        final Deck deck = getDeck(deckName);
        if (deck == null)
        {
            // the deck doesn't exist. There are various ways this could happen,
            // mostly due to split-second race conditions where the host deletes the deck while
            // a card request was incoming. We just return null in this edge case.
            return null;
        }

        if (numCards <= 0)
        {
            // invalid
            throw new IllegalArgumentException("drawCards: " + numCards);
        }

        // We can't draw more cards than there are
        final int remain = deck.cardsRemaining();
        if (numCards > remain)
        {
            numCards = remain;
        }

        // make the return value
        final DeckData.Card ret[] = new DeckData.Card[numCards];

        // draw the cards
        for (int i = 0; i < numCards; i++)
        {
            ret[i] = deck.drawCard();
        }

        // now that the cards are drawn, check the deck status
        if (deck.cardsRemaining() == 0)
        {
            // no more cards in the deck, alert them
            postSystemMessage("The " + deckName + " deck is out of cards.");
        }

        return ret;
    }

    /**
     * creates the "Clear Map" menu item
     * @return the new menu item
     */
    private JMenuItem getClearMapMenuItem()
    {
        final JMenuItem item = new JMenuItem("Clear Map");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                // confirm the erase operation
                final int res = UtilityFunctions.yesNoDialog(GametableFrame.this,
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

    /**
     * gets a deck by name or null if it doesn't exist
     * @param name name of the deck to get
     * @return the requested deck or null if it doesn't exist
     */
    public Deck getDeck(final String name)
    {
        final int idx = getDeckIdx(name);
        if (idx == -1)
        {
            return null;
        }
        // Doesn't get here if idx == -1; no need for else
        // else
        {
            final Deck d = (Deck)m_decks.get(idx);
            return d;
        }
    }

    /**
     * gets the position in the list of decks of a deck with a given name or -1 if not found
     * @param name name of the deck to locate
     * @return the position of the deck in the list or -1 if not found
     */
    public int getDeckIdx(final String name)
    {
        for (int i = 0; i < m_decks.size(); i++)
        {
            final Deck d = (Deck)m_decks.get(i);
            if (d.m_name.equals(name))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * creates the menu item "Delete macro"
     * @return the new menu item
     */
    private JMenuItem getDeleteDiceMenuItem()
    {
        final JMenuItem item = new JMenuItem("Delete macro...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                final Object[] list = m_macroMap.values().toArray();
                // give them a list of macros they can delete
                final Object sel = JOptionPane.showInputDialog(GametableFrame.this, "Select Dice Macro to remove:",
                    "Remove Dice Macro", JOptionPane.PLAIN_MESSAGE, null, list, list[0]);
                if (sel != null)
                {
                    removeMacro((DiceMacro)sel);
                }
            }
        });
        return item;
    }

    /** 
     * creates the "Dice" menu 
     * @return the new menu
     */
    private JMenu getDiceMenu()
    {
        final JMenu menu = new JMenu("Dice");
        menu.add(getAddDiceMenuItem());
        menu.add(getDeleteDiceMenuItem());
        menu.add(getLoadDiceMenuItem());
        menu.add(getSaveDiceMenuItem());
        menu.add(getSaveAsDiceMenuItem());
        return menu;
    }

    /**
     * creates the "Disconnect" menu item
     * @return the new menu item
     */
    private JMenuItem getDisconnectMenuItem()
    {
        if (m_disconnectMenuItem == null)
        {
            final JMenuItem item = new JMenuItem("Disconnect");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    disconnect();
                }
            });
            m_disconnectMenuItem = item;
        }
        return m_disconnectMenuItem;
    }

    /**
     * creates the "Edit" menu
     * @return the new menu
     */
    private JMenu getEditMenu()
    {
        final JMenu menu = new JMenu("Edit");
        menu.add(getUndoMenuItem());
        menu.add(getRedoMenuItem());

        return menu;
    }

    /**
     * Builds and returns the File Menu
     * @return the file menu just built
     */
    private JMenu getFileMenu()
    {
        final JMenu menu = new JMenu("File");

        menu.add(getOpenMapMenuItem());
        menu.add(getSaveMapMenuItem());
        menu.add(getSaveAsMapMenuItem());
        menu.add(getScanForPogsMenuItem());
        menu.add(getQuitMenuItem());

        return menu;
    }

    /**
     * @return Returns the gametableCanvas.
     */
    public GametableCanvas getGametableCanvas()
    {
        return m_gametableCanvas;
    }

    /**
     * creates the "Grid Mode" menu
     * @return the new menu
     */
    private JMenu getGridModeMenu()
    {
        final JMenu menu = new JMenu("Grid Mode");
        menu.add(m_noGridModeMenuItem);
        menu.add(m_squareGridModeMenuItem);
        menu.add(m_hexGridModeMenuItem);

        return menu;
    }

    /**
     * creates the "Help" menu
     * @return the new menu
     */
    private JMenu getHelpMenu()
    {
        final JMenu menu = new JMenu("Help");
        menu.add(getAboutMenuItem());
        return menu;
    }

    /**
     * creates the "host" menu item
     * @return the new menu item
     */
    private JMenuItem getHostMenuItem()
    {
        if (m_hostMenuItem == null)
        {
            final JMenuItem item = new JMenuItem("Host...");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    host(); // this displays the dialog to host a game
                }
            });

            m_hostMenuItem = item;
        }
        return m_hostMenuItem;
    }

    /**
     * creates the "Join" menu item
     * @return the new menu item
     */
    private JMenuItem getJoinMenuItem()
    {
        if (m_joinMenuItem == null)
        {
            final JMenuItem item = new JMenuItem("Join...");
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    join(); // this displays the dialog to join a game
                }
            });
            m_joinMenuItem = item;
        }
        return m_joinMenuItem;
    }

    /**
     * creates the "list players" menu item
     * @return the new menu item
     */
    private JMenuItem getListPlayersMenuItem()
    {
        final JMenuItem item = new JMenuItem("List Players");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl W"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                parseSlashCommand("/who"); // selecting this menu item is the same as issuing the command /who
            }
        });
        return item;
    }

    private JMenuItem getLoadDiceMenuItem()
    {
        final JMenuItem item = new JMenuItem("Load macros...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                loadMacros();
            }
        });
        return item;
    }

    private JMenu getWindowMenu()
    {
        final JMenu menu = new JMenu("Window");

        menu.add(getPogWindowMenuItem());
        menu.add(getChatWindowMenuItem());

        return menu;
    }

    public DiceMacro getMacro(final String name)
    {
        final String realName = UtilityFunctions.normalizeName(name);
        return (DiceMacro)m_macroMap.get(realName);
    }

    /**
     * @return Gets the list of macros.
     */
    public Collection getMacros()
    {
        return Collections.unmodifiableCollection(m_macroMap.values());
    }

    /**
     * Builds and returns the main menu bar
     * @return The menu bar just built
     */
    private JMenuBar getMainMenuBar()
    {
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(getFileMenu());
        menuBar.add(getEditMenu());
        menuBar.add(getNetworkMenu());
        menuBar.add(getMapMenu());
        menuBar.add(getDiceMenu());
        menuBar.add(getWindowMenu());
        menuBar.add(getHelpMenu());

        return menuBar;
    }

    /** 
     * Builds and return the window menu
     * @return the menu bar just built
     */
    private JMenuBar getNewWindowMenuBar()
    {
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(getFileMenu());
        menuBar.add(getWindowMenu());

        return menuBar;
    }

    /**
     * bulds and returns the "Map" menu
     * @return the menu just built
     */
    private JMenu getMapMenu()
    {
        final JMenu menu = new JMenu("Map");
        menu.add(getClearMapMenuItem());
        menu.add(getRecenterAllPlayersMenuItem());
        menu.add(getGridModeMenu());
        menu.add(getTogglePrivateMapMenuItem());
        menu.add(getExportMapMenuItem());

        return menu;
    }

    /**
     * Get the "Export Map" menu item
     * @return JMenuItem
     */
    private JMenuItem getExportMapMenuItem()
    {
        JMenuItem item = new JMenuItem("Export Map");
        item.addActionListener(new ActionListener() {
           /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
              exportMap();
            }
        });

      return item;
    }

    /**
     * Export map to JPeg file
     */
    private void exportMap()
    {
        File out = getMapExportFile();
        if (out == null)
            return;

        try
        {
            m_gametableCanvas.exportMap(null, out);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Failed saving JPeg File", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Ask the user to choose a file name for the exported map
     * @return File object or null if the user did not choose
     */
    private File getMapExportFile()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Map to JPeg File...");  // no external resource for text
        if (m_mapExportSaveFolder != null)
        {
            chooser.setSelectedFile(m_mapExportSaveFolder);
        }

        FileFilter filter = new FileFilter()
        {
            /*
             * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
             */
            public boolean accept(File f)
            {
                String fileName = f.getPath().toLowerCase();
                return f.isDirectory() || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg");
            }

            /*
             * @see javax.swing.filechooser.FileFilter#getDescription()
             */
            public String getDescription()
            {
                return "JPeg Files (*.jpg, *.jpeg)";
            }
        };

        chooser.setFileFilter(filter);

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }

        // Add extension to file name if user did not do so
        File out = chooser.getSelectedFile();
        String fileName = out.getAbsolutePath().toLowerCase();

        if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")))
        {
            out = new File(out.getAbsolutePath() + ".jpg");
        }

        // If file exists, confirm before overwrite
        if (out.exists())
        {
            if (JOptionPane.showConfirmDialog(this,
                "Do you want to overwrite " + out.getName() + "?",
                "The specified file already exist",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            {
                return null;
            }
        }

        if (out.exists() && !out.canWrite())
        {
            JOptionPane.showMessageDialog(this,
                "Gametable does not have write access to " + out.getName(),
                "Cannot Export Map",
                JOptionPane.ERROR_MESSAGE);

            return null;
        }

        // Figure out path name and keep it for next open of dialog
        String pathName = out.getAbsolutePath();
        int idx = pathName.lastIndexOf(File.separator);
        if (idx > -1)
        {
            pathName = pathName.substring(0, idx) + File.separator + ".";
        }

        m_mapExportSaveFolder = new File(pathName);

        return out;
    }
    
    /**
     * @return The player representing this client.
     */
    public Player getMyPlayer()
    {
        return (Player)m_players.get(getMyPlayerIndex());
    }

    /**
     * @return The id of the player representing this client.
     */
    public int getMyPlayerId()
    {
        return getMyPlayer().getId();
    }

    /**
     * @return Returns the myPlayerIndex.
     */
    public int getMyPlayerIndex()
    {
        return m_myPlayerIndex;
    }

    /**
     * @return Returns the m_netStatus.
     */
    public int getNetStatus()
    {
        return m_netStatus;
    }

    /**
     * builds and return the "Network" menu
     * @return the newly built menu
     */
    private JMenu getNetworkMenu()
    {
        final JMenu menu = new JMenu("Network");
        menu.add(getListPlayersMenuItem());
        menu.add(getHostMenuItem());
        menu.add(getJoinMenuItem());
        menu.add(getDisconnectMenuItem());

        return menu;
    }

    /**
     * gets and id for a state
     * 
     * @return the next id number
     */
    public int getNewStateId()
    {
        return m_nextStateId++;
    }

    /**
     * Builds and returns the menu item for opening a new map
     * The function includes defining the action listener and the actions it will
     * perform when this item is called.
     * 
     * @return the File/Open menu item.
     */
    private JMenuItem getOpenMapMenuItem()
    {
        final JMenuItem item = new JMenuItem("Open Map...");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed O"));
        item.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                // opening while on the public layer...
                if (getGametableCanvas().getActiveMap() == getGametableCanvas().getPublicMap())
                {
                    final File openFile = UtilityFunctions.doFileOpenDialog("Open", "grm", true);

                    if (openFile == null)
                    {
                        // they cancelled out of the open
                        return;
                    }

                    m_actingFilePublic = openFile;

                    final int result = UtilityFunctions.yesNoDialog(GametableFrame.this,
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
                                final byte grmFile[] = UtilityFunctions.loadFileToArray(m_actingFilePublic);
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
                        // don't want these packets to be propagated to other players
                        final int oldStatus = m_netStatus;
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

    /**
     * gets the player associated with a connection
     * @param conn connection to use to find the player
     * @return the player object associated with the connection
     */
    public Player getPlayerFromConnection(final Connection conn)
    {
        for (int i = 0; i < m_players.size(); i++)
        {
            final Player plr = (Player)m_players.get(i);
            if (conn == plr.getConnection())
            {
                return plr;
            }
        }

        return null;
    }

    /**
     * Finds the index of a given player.
     * 
     * @param player Player to find index of.
     * @return Index of the given player, or -1.
     */
    public int getPlayerIndex(final Player player)
    {
        return m_players.indexOf(player);
    }

    /**
     * @return Returns the player list.
     */
    public List getPlayers()
    {
        return Collections.unmodifiableList(m_players);
    }

    /**
     * @return The root pog library.
     */
    public PogLibrary getPogLibrary()
    {
        return m_pogLibrary;
    }

    /**
     * @return The pog panel.
     */
    public PogPanel getPogPanel()
    {
        return m_pogPanel;
    }

    /**
     * @return The deck panel.
     */
    public JFrame getPogWindow()
    {
        return pogWindow;
    }

    /**
     * @return The preferences object.
     */
    public Preferences getPreferences()
    {
        return m_preferences;
    }
    
    /**
     * build and returns the "Quit" menu item
     * @return the newly built menu item
     */
    private JMenuItem getQuitMenuItem()
    {
        final JMenuItem item = new JMenuItem("Quit");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                if (pogWindow != null) // close the pog window
                {
                    pogWindow.dispose();
                }
                dispose();
                System.exit(0);
            }
        });

        return item;
    }

    /**
     * builds and return the "Undock Pog Window" menu item
     * @return the menu item just built
     */
    private JMenuItem getPogWindowMenuItem()
    {
        final JMenuItem item = new JMenuItem("Un/Dock Pog Window");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl P"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                dockPogWindow();
            }
        });

        return item;
    }

    /** 
     * builds and return the "Undock Chat Window"
     * @return the menu item 
     */
    private JMenuItem getChatWindowMenuItem()
    {
        final JMenuItem item = new JMenuItem("Un/Dock Chat Window");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                dockChatWindow();
            }
        });

        return item;
    }

    /**
     * builds and returns the "Recenter all Player" menu item
     * @return a menu item
     */
    private JMenuItem getRecenterAllPlayersMenuItem()
    {
        final JMenuItem item = new JMenuItem("Recenter all Players");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                // confirm the operation
                final int result = UtilityFunctions.yesNoDialog(GametableFrame.this,
                    "This will recenter everyone's map view to match yours, "
                        + "and will set their zoom levels to match yours. Are you sure you want to do this?",
                    "Recenter?");
                if (result == UtilityFunctions.YES)
                {
                    // get our view center
                    final int viewCenterX = getGametableCanvas().getWidth() / 2;
                    final int viewCenterY = getGametableCanvas().getHeight() / 2;

                    // convert to model coordinates
                    final Point modelCenter = getGametableCanvas().viewToModel(viewCenterX, viewCenterY);
                    getGametableCanvas().recenterView(modelCenter.x, modelCenter.y, getGametableCanvas().m_zoom);
                    postSystemMessage(getMyPlayer().getPlayerName() + " Recenters everyone's view!");
                }
            }
        });
        return item;
    }

    /**
     * builds and returns the "Redo" menu item
     * @return a menu item
     */
    private JMenuItem getRedoMenuItem()
    {
        final JMenuItem item = new JMenuItem("Redo");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl Y"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                getGametableCanvas().redo();
            }
        });

        return item;
    }

    /**
     * builds and return the "Save macro as" menu item
     * @return a menu item
     */
    private JMenuItem getSaveAsDiceMenuItem()
    {
        final JMenuItem item = new JMenuItem("Save macros as...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                saveMacros();
            }
        });
        return item;
    }

    /**
     * builds and returns the "Save map as" menu item
     * @return the menu item
     */
    public JMenuItem getSaveAsMapMenuItem()
    {
        final JMenuItem item = new JMenuItem("Save Map As...");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl shift pressed S"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
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

    /**
     * builds and returns the "save macros" menu item
     * @return the menu item
     */
    private JMenuItem getSaveDiceMenuItem()
    {
        final JMenuItem item = new JMenuItem("Save macros...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                try
                {
                    saveMacros(m_actingFileMacros);
                }
                catch (final IOException ioe)
                {
                    Log.log(Log.SYS, ioe);
                }
            }
        });
        return item;
    }

    /**
     * builds and returns the "save map" menu item
     * @return the menu item
     */
    public JMenuItem getSaveMapMenuItem()
    {
        final JMenuItem item = new JMenuItem("Save Map");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed S"));
        item.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
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

    /**
     * builds and returns the "Scan for pogs" menu item
     * @return the menu item
     */
    private JMenuItem getScanForPogsMenuItem()
    {
        final JMenuItem item = new JMenuItem("Scan for Pogs");
        item.setAccelerator(KeyStroke.getKeyStroke("F5"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                reacquirePogs();
            }
        });

        return item;
    }

    /**
     * builds and returns the "Edit private map" menu item
     * @return the menu item
     */
    private JMenuItem getTogglePrivateMapMenuItem()
    {
        if (m_togglePrivateMapMenuItem == null)
        {
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Edit Private Map");
            item.setAccelerator(KeyStroke.getKeyStroke("ctrl F"));
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    toggleLayer();
                }
            });

            m_togglePrivateMapMenuItem = item;
        }

        return m_togglePrivateMapMenuItem;
    }

    /**
     * gets the tools manager
     * @return the tool manager
     */
    public ToolManager getToolManager()
    {
        return m_toolManager;
    }

    /**
     * gets and returns the "Undo" menu item
     * @return the menu item
     */
    private JMenuItem getUndoMenuItem()
    {
        final JMenuItem item = new JMenuItem("Undo");
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                getGametableCanvas().undo();
            }
        });

        return item;
    }


    /**
     * handles a "grid mode" packet
     * @param gridMode grid mode
     */
    public void gridModePacketReceived(final int gridMode)
    {
        // note the new grid mode
        getGametableCanvas().setGridModeByID(gridMode);
        updateGridModeMenu(); // sets the appropriate checks in the menu

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeGridModePacket(gridMode));
        }

        repaint(); // repaint the canvas
    }

    /**
     * handles the reception of a "grm" packet
     * @param grmFile
     */
    public void grmPacketReceived(final byte grmFile[])
    {
        // only the host should ever get this packet. If a joiner gets it
        // for some reason, it should ignore it.
        // if we're offline, then sure, go ahead and load
        if (m_netStatus != NETSTATE_JOINED)
        {
            loadStateFromRawFileData(grmFile);
        }
    }

    /**
     * attempt to host a network name by prompting details of the game session
     */
    public void host()
    {
        host(false);
    }

    /**
     * host a game
     * @param force if force do not ask for details, otherwise display the host dialog
     */
    public void host(boolean force)
    {
        if (m_netStatus == NETSTATE_HOST) // if we are already the host
        {
            logAlertMessage("You are already hosting.");
            return;
        }
        if (m_netStatus == NETSTATE_JOINED) // if we are connected to a game and not hosting
        {
            logAlertMessage("You can not host until you disconnect from the game you joined.");
            return;
        }

        if (!force)
        {
            // get relevant infor from the user
            if (!runHostDialog())
            {
                return;
            }
        }

        // clear out all players
        m_nextPlayerId = 0;
        m_players = new ArrayList();
        final Player me = new Player(m_playerName, m_characterName, m_nextPlayerId); // this means the host is always
                                                                                     // player 0
        m_nextPlayerId++;
        m_players.add(me);
        me.setHostPlayer(true);
        m_myPlayerIndex = 0;

        m_networkThread = new NetworkThread(m_port);
        m_networkThread.start();
        // TODO: fix hosting failure detection

        m_netStatus = NETSTATE_HOST; // our status is now hosting
        final String message = "Hosting on port: " + m_port;
        logSystemMessage(message);

        logMessage("<a href=\"http://gametable.galactanet.com/echoip.php\">Click here to see the IP address you're hosting on.</a> (Making you click it ensures you have control over your privacy)");

        Log.log(Log.NET, message);

        m_hostMenuItem.setEnabled(false); // disable the host menu item
        m_joinMenuItem.setEnabled(false); // disable the join menu item
        m_disconnectMenuItem.setEnabled(true); // enable the disconnect menu item
        setTitle(GametableApp.VERSION + " - " + me.getCharacterName());

        // when you host, all the undo stacks clear
        getGametableCanvas().clearUndoStacks();

        // also, all decks clear
        m_decks.clear();
        m_cards.clear();
    }

    /**
     * handles a failure in the host thread
     */
    public void hostThreadFailed()
    {
        logAlertMessage("Failed to host.");
        m_networkThread.interrupt();
        m_networkThread = null;
        disconnect();
    }

    /**
     * Performs initialization. This draws all the controls in the Frame and sets up listener to react
     * to user actions.
     * 
     * @throws IOException
     */
    private void initialize() throws IOException
    {
        if (DEBUG_FOCUS) // if debugging
        {
            final KeyboardFocusManager man = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            man.addPropertyChangeListener(new PropertyChangeListener()
            {
                /*
                 * If debugging,show changes to properties in the console
                 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
                 */
                public void propertyChange(final PropertyChangeEvent e)
                {
                    System.out.println(e.getPropertyName() + ":\n    " + e.getOldValue() + "\n -> " + e.getNewValue());
                }

            });
        }

        setContentPane(new JPanel(new BorderLayout())); // Set the main UI object with a Border Layout
        setDefaultCloseOperation(EXIT_ON_CLOSE);        // Ensure app ends with this frame is closed
        setTitle(GametableApp.VERSION);                 // Set frame title to the current version
        setJMenuBar(getMainMenuBar());                  // Set the main MenuBar
        
        // Set this class to handle events from changing grid types
        m_noGridModeMenuItem.addActionListener(this);   
        m_squareGridModeMenuItem.addActionListener(this);
        m_hexGridModeMenuItem.addActionListener(this);

        // Configure chat panel
        m_chatPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        m_chatPanel.setLayout(new BorderLayout());

        // Configure chat typing panel
        m_textAndEntryPanel.setLayout(new BorderLayout());

        final JPanel entryPanel = new JPanel(new BorderLayout(0, 0));
        entryPanel.add(new StyledEntryToolbar(m_textEntry), BorderLayout.NORTH);
        entryPanel.add(m_textEntry.getComponentToAdd(), BorderLayout.SOUTH);
        m_textAndEntryPanel.add(entryPanel, BorderLayout.SOUTH);

        if (USE_NEW_CHAT_PANE)
        {
            m_textAndEntryPanel.add(m_newChatLog.getComponentToAdd(), BorderLayout.CENTER);
        }
        else
        {
            m_textAndEntryPanel.add(m_chatLog.getComponentToAdd(), BorderLayout.CENTER);
        }
        
        // Configure the panel containing the map and the chat window
        m_mapChatSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        m_mapChatSplitPane.setContinuousLayout(true);
        m_mapChatSplitPane.setResizeWeight(1.0);
        m_mapChatSplitPane.setBorder(null);

        m_textAreaPanel.setLayout(new BorderLayout());

        // Configure the panel that splits the map and the pog list
        m_mapPogSplitPane.setContinuousLayout(true);
        m_mapPogSplitPane.setBorder(null);

        // Configure the color dropdown
        m_colorCombo.setMaximumSize(new Dimension(100, 21));
        m_colorCombo.setFocusable(false);
        
        // Configure the toolbar
        m_toolBar.setFloatable(false);
        m_toolBar.setRollover(true);
        m_toolBar.setBorder(new EmptyBorder(2, 5, 2, 5));
        m_toolBar.add(m_colorCombo, null);
        m_toolBar.add(Box.createHorizontalStrut(5));

        initializeTools();

        m_toolBar.add(Box.createHorizontalStrut(5));

        /*
         * Added in order to accomodate grid unit multiplier
         */
        m_gridunitmultiplier = new JTextField("5", 3);
        m_gridunitmultiplier.setMaximumSize(new Dimension(42, 21));
        
        // Configure the units dropdown
        final String[] units = {
            "ft", "m", "u"
        };
        m_gridunit = new JComboBox(units);
        m_gridunit.setMaximumSize(new Dimension(42, 21));
        m_toolBar.add(m_gridunitmultiplier);
        m_toolBar.add(m_gridunit);
        // Add methods to react to changes to the unit multiplier
        // TODO: this is definitely better somewhere else
        m_gridunitmultiplier.getDocument().addDocumentListener(new DocumentListener()
        {
            //TODO: exceptions should be captured
            public void changedUpdate(final DocumentEvent e)
            {
                grid_multiplier = Double.parseDouble(m_gridunitmultiplier.getText());
            }

            public void insertUpdate(final DocumentEvent e)
            {
                grid_multiplier = Double.parseDouble(m_gridunitmultiplier.getText());
            }

            public void removeUpdate(final DocumentEvent e)
            {
                grid_multiplier = Double.parseDouble(m_gridunitmultiplier.getText());
            }
        });
        m_gridunit.addActionListener(this);

        // Configure the checkbox to show names
        m_showNamesCheckbox.setFocusable(false);
        // TODO: consider to place this somewhere else
        m_showNamesCheckbox.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_gametableCanvas.repaint();
            }
        });
        m_toolBar.add(m_showNamesCheckbox);

        getContentPane().add(m_toolBar, BorderLayout.NORTH);

        getGametableCanvas().init(this);
        m_pogLibrary = new PogLibrary();

        // pogWindow

        m_pogPanel = new PogPanel(m_pogLibrary, getGametableCanvas());
        m_pogsTabbedPane.add(m_pogPanel, "Pog Library");
        m_activePogsPanel = new ActivePogsPanel();
        m_pogsTabbedPane.add(m_activePogsPanel, "Active Pogs");
        m_macroPanel = new MacroPanel();
        m_pogsTabbedPane.add(m_macroPanel, "Dice Macros");
        m_pogsTabbedPane.setFocusable(false);

        m_chatPanel.add(m_textAreaPanel, BorderLayout.CENTER);
        m_textAreaPanel.add(m_textAndEntryPanel, BorderLayout.CENTER);
        m_canvasPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 1, 1, 1)));
        m_canvasPane.add(getGametableCanvas(), BorderLayout.CENTER);
        m_mapChatSplitPane.add(m_canvasPane, JSplitPane.TOP);
        m_mapChatSplitPane.add(m_chatPanel, JSplitPane.BOTTOM);

        m_mapPogSplitPane.add(m_pogsTabbedPane, JSplitPane.LEFT);
        m_mapPogSplitPane.add(m_mapChatSplitPane, JSplitPane.RIGHT);
        getContentPane().add(m_mapPogSplitPane, BorderLayout.CENTER);
        m_status.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        getContentPane().add(m_status, BorderLayout.SOUTH);
        updateStatus();

        m_disconnectMenuItem.setEnabled(false); // when the program starts we are disconnected so disable this menu item

        final ColorComboCellRenderer renderer = new ColorComboCellRenderer();
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
        m_myPlayerIndex = 0;

        m_colorCombo.addActionListener(this);
        updateGridModeMenu();

        addComponentListener(new ComponentAdapter()
        {
            /*
             * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
             */
            public void componentMoved(final ComponentEvent e)
            {
                updateWindowInfo();
            }

            /*
             * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
             */
            public void componentResized(final ComponentEvent event)
            {
                updateWindowInfo();
            }

        });

        // handle window events
        addWindowListener(new WindowAdapter()
        {
            /*
             * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
             */
            public void windowClosed(final WindowEvent e)
            {
                saveAll();
            }

            /*
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             */
            public void windowClosing(final WindowEvent e)
            {
                saveAll();
            }
        });

        /*
         * // change the default component traverse settings // we do this cause we don't really care about those //
         * settings, but we want to be able to use the tab key KeyboardFocusManager focusMgr =
         * KeyboardFocusManager.getCurrentKeyboardFocusManager(); Set set = new
         * HashSet(focusMgr.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS)); set.clear(); //
         * set.add(KeyStroke.getKeyStroke('\t', 0, false));
         * focusMgr.setDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set); set = new
         * HashSet(focusMgr.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS)); set.clear(); //
         * set.add(KeyStroke.getKeyStroke('\t', 0, false));
         * focusMgr.setDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);
         */

        m_gametableCanvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed SLASH"),
            "startSlash");
        m_gametableCanvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed ENTER"),
            "startText");
        m_gametableCanvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke("control pressed R"), "reply");

        m_gametableCanvas.getActionMap().put("startSlash", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (m_gametableCanvas.isTextFieldFocused())
                {
                    return;
                }

                // only do this at the start of a line
                if (m_textEntry.getText().length() == 0)
                {
                    // furthermore, only do the set text and focusing if we don't have
                    // focus (otherwise, we end up with two slashes. One from the user typing it, and
                    // another from us setting the text, cause our settext happens first.)
                    if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() != m_textEntry)
                    {
                        m_textEntry.setText("/");
                    }
                }
                m_textEntry.requestFocus();
            }
        });

        m_gametableCanvas.getActionMap().put("startText", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (m_gametableCanvas.isTextFieldFocused())
                {
                    return;
                }

                if (m_textEntry.getText().length() == 0)
                {
                    // furthermore, only do the set text and focusing if we don't have
                    // focus (otherwise, we end up with two slashes. One from the user typing it, and
                    // another from us setting the text, cause our settext happens first.)
                    if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() != m_textEntry)
                    {
                        m_textEntry.setText("");
                    }
                }
                m_textEntry.requestFocus();
            }
        });

        m_gametableCanvas.getActionMap().put("reply", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                // we don't do this if there's already text in the entry field
                if (m_textEntry.getText().length() == 0)
                {
                    // if they've never received a tell, just tell them that
                    if (m_lastPrivateMessageSender == null)
                    {
                        // they've received no tells yet
                        logAlertMessage("You cannot reply until you receive a /tell from another player.");
                    }
                    else
                    {
                        startTellTo(m_lastPrivateMessageSender);
                    }
                }
                m_textEntry.requestFocus();
            }
        });

        initializeExecutorThread();
    }

    /**
     * starts the execution thread
     */
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
     * Initializes the tools from the ToolManager.
     */
    private void initializeTools()
    {
        try
        {
            m_toolManager.initialize();
            final int buttonSize = m_toolManager.getMaxIconSize();
            final int numTools = m_toolManager.getNumTools();
            m_toolButtons = new JToggleButton[numTools];
            for (int toolId = 0; toolId < numTools; toolId++)
            {
                final ToolManager.Info info = m_toolManager.getToolInfo(toolId);
                final Image im = UtilityFunctions.createDrawableImage(buttonSize, buttonSize);
                {
                    final Graphics g = im.getGraphics();
                    final Image icon = info.getIcon();
                    final int offsetX = (buttonSize - icon.getWidth(null)) / 2;
                    final int offsetY = (buttonSize - icon.getHeight(null)) / 2;
                    g.drawImage(info.getIcon(), offsetX, offsetY, null);
                    g.dispose();
                }

                final JToggleButton button = new JToggleButton(new ImageIcon(im));
                m_toolBar.add(button);
                button.addActionListener(new ToolButtonActionListener(toolId));
                button.setFocusable(false);
                m_toolButtonGroup.add(button);
                m_toolButtons[toolId] = button;

                String keyInfo = "";
                if (info.getQuickKey() != null)
                {
                    final String actionId = "tool" + toolId + "Action";
                    getGametableCanvas().getActionMap().put(actionId, new ToolButtonAbstractAction(toolId));
                    final KeyStroke keystroke = KeyStroke.getKeyStroke("ctrl " + info.getQuickKey());
                    getGametableCanvas().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keystroke, actionId);
                    keyInfo = " (Ctrl+" + info.getQuickKey() + ")";
                }
                button.setToolTipText(info.getName() + keyInfo);
                final List prefs = info.getTool().getPreferences();
                for (int i = 0; i < prefs.size(); i++)
                {
                    m_preferences.addPreference((PreferenceDescriptor)prefs.get(i));
                }
            }
        }
        catch (final IOException ioe)
        {
            Log.log(Log.SYS, "Failure initializing tools.");
            Log.log(Log.SYS, ioe);
        }
    }

    /**
     * joins a network game
     */
    public void join()
    {
        if (m_netStatus == NETSTATE_HOST) // we can't join if we're hosting
        {
            logAlertMessage("You are hosting. If you wish to join a game, disconnect first.");
            return;
        }
        if (m_netStatus == NETSTATE_JOINED) // we can't join if we are already connected
        {
            logAlertMessage("You are already in a game. You must disconnect before joining another.");
            return;
        }

        boolean res = runJoinDialog(); // get details of where to connect to
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
            final Connection conn = new Connection(m_ipAddress, m_port);
            m_networkThread.add(conn);

            // now that we've successfully made a connection, let the host know
            // who we are
            m_players = new ArrayList();
            final Player me = new Player(m_playerName, m_characterName, -1);
            me.setConnection(conn);
            m_players.add(me);
            m_myPlayerIndex = 0;

            // reset game data
            getGametableCanvas().getPublicMap().setScroll(0, 0);
            getGametableCanvas().getPublicMap().clearPogs();
            getGametableCanvas().getPublicMap().clearLines();
            // PacketManager.g_imagelessPogs.clear();

            // send the packet
            while (!conn.isConnected()) // this waits until the connection is established
            {
            }
            conn.sendPacket(PacketManager.makePlayerPacket(me, m_password)); // send my data to the host

            PacketSourceState.beginHostDump();

            // and now we're ready to pay attention
            m_netStatus = NETSTATE_JOINED;

            logSystemMessage("Joined game");

            m_hostMenuItem.setEnabled(false); // disable the host menu item
            m_joinMenuItem.setEnabled(false); // disable the join menu item
            m_disconnectMenuItem.setEnabled(true); // enable the disconnect menu item
            setTitle(GametableApp.VERSION + " - " + me.getCharacterName());
        }
        catch (final Exception ex)
        {
            Log.log(Log.SYS, ex);
            logAlertMessage("Failed to connect.");
            setTitle(GametableApp.VERSION);
            PacketSourceState.endHostDump();
        }
    }

    /**
     * close a connection indicating a reason
     * @param conn connection to close
     * @param reason code for the reason to be booted
     */
    public void kick(final Connection conn, final int reason)
    {
        send(PacketManager.makeRejectPacket(reason), conn);
        conn.close();
    }

    /**
     * handle a lines packet (a player painted lines)
     * @param lines array of line segments
     * @param authorID author
     * @param state
     */
    public void linesPacketReceived(final LineSegment[] lines, final int authorID, final int state)
    {
        int stateId = state;
        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            // and give it a genuine state ID first
            stateId = getNewStateId();
            send(PacketManager.makeLinesPacket(lines, authorID, stateId));
        }

        // add the lines to the array
        getGametableCanvas().doAddLineSegments(lines, authorID, stateId);
    }

    /**
     * Docks or undocks the Pog Panel from from the main Gametable frame
     */
    private void dockPogWindow()
    {
        if (!b_pogWindowDocked) // dock the pog window
        {
            if (pogWindow != null)
            {
                pogWindow.setVisible(false); // hide the undocked window
            }

            pogWindow.getContentPane().remove(m_pogsTabbedPane);
            // remove panels depending on whether the chat window is docked or undocked
            if (b_chatWindowDocked)
            {
                GametableFrame.getGametableFrame().getContentPane().remove(m_mapChatSplitPane);
            }
            else
            {
                GametableFrame.getGametableFrame().getContentPane().remove(m_canvasPane);
            }
            // re-add the panels
            GametableFrame.getGametableFrame().getContentPane().validate();
            GametableFrame.getGametableFrame().getContentPane().validate();
            GametableFrame.getGametableFrame().m_mapPogSplitPane.add(m_pogsTabbedPane, JSplitPane.LEFT);
            if (b_chatWindowDocked)
            {
                GametableFrame.getGametableFrame().m_mapPogSplitPane.add(m_mapChatSplitPane, JSplitPane.RIGHT);
            }
            else
            {
                GametableFrame.getGametableFrame().m_mapPogSplitPane.add(m_canvasPane, JSplitPane.RIGHT);
            }
            GametableFrame.getGametableFrame().getContentPane().add(m_mapPogSplitPane, BorderLayout.CENTER);
            GametableFrame.getGametableFrame().getContentPane().validate();
            repaint();

            b_pogWindowDocked = true;
        }
        else // undock the pog window
        {
            if (pogWindow == null) // create a window for the undocked panel
            {
                pogWindow = new JFrame();
                pogWindow.setTitle("Pog Window");
                pogWindow.setSize(195, 500);
                pogWindow.setLocation(0, 80);
                pogWindow.setFocusable(true);
                pogWindow.setAlwaysOnTop(false);
            }
            pogWindow.setJMenuBar(getNewWindowMenuBar());

            // remove and add panels depending on wether the chat panel is docked or undocked
            GametableFrame.getGametableFrame().getContentPane().remove(m_mapPogSplitPane);
            if (b_chatWindowDocked)
            {
                GametableFrame.getGametableFrame().getContentPane().remove(m_mapChatSplitPane);
            }
            else
            {
                GametableFrame.getGametableFrame().getContentPane().remove(m_canvasPane);
            }
            GametableFrame.getGametableFrame().getContentPane().validate();
            if (b_chatWindowDocked)
            {
                GametableFrame.getGametableFrame().getContentPane().add(m_mapChatSplitPane, BorderLayout.CENTER);                
            }
            else
            {
                GametableFrame.getGametableFrame().getContentPane().add(m_canvasPane, BorderLayout.CENTER);
            }
            GametableFrame.getGametableFrame().getContentPane().validate();
            repaint();
            pogWindow.getContentPane().add(m_pogsTabbedPane);
            pogWindow.setVisible(true);

            b_pogWindowDocked = false;
        }
    }

    /**
     * Docks or undocks the chat window from from the main Gametable frame
     */
    private void dockChatWindow()
    {
        if (!b_chatWindowDocked)
        {
            if (chatWindow != null)
            {
                chatWindow.setVisible(false);
            }

            chatWindow.getContentPane().remove(m_chatPanel);
            if (b_pogWindowDocked)
            {
                GametableFrame.getGametableFrame().getContentPane().remove(m_mapPogSplitPane);
                GametableFrame.getGametableFrame().getContentPane().remove(m_canvasPane);
            }
            else
            {
                GametableFrame.getGametableFrame().getContentPane().remove(m_canvasPane);
            }
            GametableFrame.getGametableFrame().getContentPane().validate();
            GametableFrame.getGametableFrame().m_mapChatSplitPane.add(m_canvasPane, JSplitPane.TOP);
            GametableFrame.getGametableFrame().m_mapChatSplitPane.add(m_chatPanel, JSplitPane.BOTTOM);
            if (b_pogWindowDocked)
            {
                GametableFrame.getGametableFrame().m_mapPogSplitPane.add(m_pogsTabbedPane, JSplitPane.LEFT);
                GametableFrame.getGametableFrame().m_mapPogSplitPane.add(m_mapChatSplitPane, JSplitPane.RIGHT);
                GametableFrame.getGametableFrame().getContentPane().add(m_mapPogSplitPane, BorderLayout.CENTER);
            }
            else
            {
                GametableFrame.getGametableFrame().getContentPane().add(m_mapChatSplitPane, BorderLayout.CENTER);
            }
            GametableFrame.getGametableFrame().getContentPane().validate();
            repaint();

            b_chatWindowDocked = true;
        }
        else
        {
            if (chatWindow == null)
            {
                chatWindow = new JFrame();
                chatWindow.setTitle("Chat Window");
                chatWindow.setSize(800, 200);
                chatWindow.setLocation(195, 600);
                chatWindow.setFocusable(true);
                chatWindow.setAlwaysOnTop(true);
            }
            chatWindow.setJMenuBar(getNewWindowMenuBar());

            if (b_pogWindowDocked)
            {
                GametableFrame.getGametableFrame().getContentPane().remove(m_mapPogSplitPane);
            }
            else
            {
                GametableFrame.getGametableFrame().getContentPane().remove(m_canvasPane);
            }
            GametableFrame.getGametableFrame().getContentPane().remove(m_mapChatSplitPane);
            GametableFrame.getGametableFrame().getContentPane().remove(m_chatPanel);
            GametableFrame.getGametableFrame().getContentPane().validate();

            if (b_pogWindowDocked)
            {
                GametableFrame.getGametableFrame().m_mapPogSplitPane.add(m_pogsTabbedPane, JSplitPane.LEFT);
                GametableFrame.getGametableFrame().m_mapPogSplitPane.add(m_canvasPane, JSplitPane.RIGHT);
                GametableFrame.getGametableFrame().getContentPane().add(m_mapPogSplitPane, BorderLayout.CENTER);
            }
            else
            {
                GametableFrame.getGametableFrame().getContentPane().add(m_canvasPane, BorderLayout.CENTER);
            }
            GametableFrame.getGametableFrame().getContentPane().validate();
            repaint();
            chatWindow.getContentPane().add(m_chatPanel);
            chatWindow.setVisible(true);

            b_chatWindowDocked = false;
        }
    }

    /**
     * Pops up a dialog to load macros from a file.
     */
    public void loadMacros()
    {
        final File openFile = UtilityFunctions.doFileOpenDialog("Open", "xml", true);

        if (openFile == null)
        {
            // they cancelled out of the open
            return;
        }

        final int result = UtilityFunctions.yesNoDialog(GametableFrame.this,
            "This will load a macro file, replacing all your existing macros. Are you sure you want to do this?",
            "Confirm Load Macros");
        if (result != UtilityFunctions.YES)
        {
            return;
        }

        m_actingFileMacros = openFile;
        if (m_actingFileMacros != null)
        {
            // actually do the load if we're the host or offline
            try
            {
                loadMacros(m_actingFileMacros);
                logSystemMessage("Loaded macros from " + m_actingFileMacros + ".");
            }
            catch (final SAXException saxe)
            {
                Log.log(Log.SYS, saxe);
            }
        }
    }

    /**
     * Loads macros from the given file, if possible.
     * 
     * @param file File to load macros from.
     * @throws SAXException If an error occurs.
     */
    public void loadMacros(final File file) throws SAXException
    {
        try
        {
            // macros are contained in an XML document that will be parsed using a SAXParser
            // the SAXParser generates events for XML tags as it founds them.
            final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            final DiceMacroSaxHandler handler = new DiceMacroSaxHandler();
            parser.parse(file, handler);
            // we are done parsing the file. The handler contains all the macros, ready to be added to the macro map
            // clear the state
            m_macroMap.clear();
            for (final Iterator iterator = handler.getMacros().iterator(); iterator.hasNext();)
            {
                final DiceMacro macro = (DiceMacro)iterator.next();
                addMacro(macro);
            }
        }
        catch (final IOException ioe)
        {
            throw new SAXException(ioe);
        }
        catch (final ParserConfigurationException pce)
        {
            throw new SAXException(pce);
        }

        m_macroPanel.refreshMacroList(); // displays the macro list
    }

    /**
     * loads preferences from file
     */
    public void loadPrefs()
    {
        final File file = new File("prefs.prf");
        if (!file.exists()) // if the file doesn't exist, set some hard-coded defaults and return
        {
            // DEFAULTS
            m_mapChatSplitPane.setDividerLocation(0.7);
            m_mapPogSplitPane.setDividerLocation(150);
            m_windowSize = new Dimension(800, 600);
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            m_windowPos = new Point((screenSize.width - m_windowSize.width) / 2,
                (screenSize.height - m_windowSize.height) / 2);
            m_bMaximized = false;
            applyWindowInfo();
            addMacro("d20", "d20");
            m_showNamesCheckbox.setSelected(false);
            m_actingFileMacros = new File("macros.xml");
            try
            {
                loadMacros(m_actingFileMacros);
            }
            catch (final SAXException se)
            {
                Log.log(Log.SYS, se);
            }
            return;
        }

        // try reading preferences from file
        try
        {
            final FileInputStream prefFile = new FileInputStream(file);
            final DataInputStream prefDis = new DataInputStream(prefFile);

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

            m_actingFileMacros = new File(prefDis.readUTF());
            m_showNamesCheckbox.setSelected(prefDis.readBoolean());

            prefDis.close();
            prefFile.close();
        }
        catch (final FileNotFoundException ex1)
        {
            Log.log(Log.SYS, ex1);
        }
        catch (final IOException ex1)
        {
            Log.log(Log.SYS, ex1);
        }

        try
        {
            loadMacros(m_actingFileMacros);
        }
        catch (final SAXException se)
        {
            Log.log(Log.SYS, se);
        }
    }

    public void loadState(final byte saveFileData[])
    {
        // let it know we're receiving initial data (which we are. Just from a file instead of the host)
        try
        {
            // now we have to pick out the packets and send them in for processing one at a time
            final DataInputStream walker = new DataInputStream(new ByteArrayInputStream(saveFileData));
            int read = 0;
            int packetNum = 0;
            while (read < saveFileData.length)
            {
                final int packetLen = walker.readInt();
                read += 4;

                final byte[] packet = new byte[packetLen];
                walker.read(packet);
                read += packetLen;

                // dispatch the packet
                PacketManager.readPacket(null, packet);
                packetNum++;
            }
        }
        catch (final FileNotFoundException ex)
        {
            Log.log(Log.SYS, ex);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }

        repaint();
        refreshPogList();
    }

    public void loadState(final File file)
    {
        if (!file.exists())
        {
            return;
        }

        try
        {
            final FileInputStream input = new FileInputStream(file);
            final DataInputStream infile = new DataInputStream(input);

            // get the big hunk o data
            final int ver = infile.readInt();
            if (ver != COMM_VERSION)
            {
                // wrong version
                throw new IOException("Invalid save file version.");
            }

            final int len = infile.readInt();
            final byte[] saveFileData = new byte[len];
            infile.read(saveFileData);

            PacketSourceState.beginFileLoad();
            loadState(saveFileData);
            PacketSourceState.endFileLoad();

            input.close();
            infile.close();
        }
        catch (final FileNotFoundException ex)
        {
            Log.log(Log.SYS, ex);
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
        }
    }

    public void loadStateFromRawFileData(final byte rawFileData[])
    {
        final byte saveFileData[] = new byte[rawFileData.length - 8]; // a new array that lacks the first
        // int
        System.arraycopy(rawFileData, 8, saveFileData, 0, saveFileData.length);
        loadState(saveFileData);
    }

    public void lockPogPacketReceived(final int id, final boolean newLock)
    {
        getGametableCanvas().doLockPog(id, newLock);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeLockPogPacket(id, newLock));
        }
    }

    public void logAlertMessage(final String text)
    {
        logMessage(ALERT_MESSAGE_FONT + text + END_ALERT_MESSAGE_FONT);
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

    public void logMessage(final String text)
    {
        if (USE_NEW_CHAT_PANE) {
            m_newChatLog.getModel().receiveLine(text);
        } else {
            m_chatLog.addText(text);
        }
    }

    public void logPrivateMessage(final String fromName, final String toName, final String text)
    {
        // when they get a private message, we format it for the chat log
        logMessage(PRIVATE_MESSAGE_FONT + UtilityFunctions.emitUserLink(fromName) + " tells you: "
            + END_PRIVATE_MESSAGE_FONT + text);

        // we track who the last private message sender was, for
        // reply purposes
        m_lastPrivateMessageSender = fromName;
    }

    public void logSystemMessage(final String text)
    {
        logMessage(SYSTEM_MESSAGE_FONT + text + END_SYSTEM_MESSAGE_FONT);
    }

    // makes a card pog out of the sent in card
    public Pog makeCardPog(final DeckData.Card card)
    {
        // there might not be a pog associated with this card
        if (card.m_cardFile.length() == 0)
        {
            return null;
        }

        final PogType newPogType = getPogLibrary().getPog("pogs" + UtilityFunctions.LOCAL_SEPARATOR + card.m_cardFile);

        // there could be a problem with the deck definition. It's an easy mistake
        // to make. So rather than freak out, we just return null.
        if (newPogType == null)
        {
            return null;
        }

        final Pog newPog = new Pog(newPogType);

        // make it a card pog
        newPog.makeCardPog(card);
        return newPog;
    }

    public void movePogPacketReceived(final int id, final int newX, final int newY)
    {
        getGametableCanvas().doMovePog(id, newX, newY);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeMovePogPacket(id, newX, newY));
        }
    }

    public void packetReceived(final Connection conn, final byte[] packet)
    {
        // synch here. after we get the packet, but before we process it.
        // this is all the synching we need on the comm end of things.
        // we will also need to synch every user entry point
        PacketManager.readPacket(conn, packet);
    }

    public void parseSlashCommand(final String text)
    {
        // get the command
        final String[] words = UtilityFunctions.breakIntoWords(text);
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
                logSystemMessage("/macro usage: /macro &lt;macroName&gt; &lt;dice roll in standard format&gt;");
                logSystemMessage("Examples:");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/macro Attack d20+8");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/macro SneakDmg d4 + 2 + 4d6");
                logSystemMessage("Note: Macros will replace existing macros with the same name.");
                return;
            }

            // the second word is the name
            final String name = words[1];

            // all subsequent "words" are the die roll macro
            addMacro(name, text.substring("/macro ".length() + name.length() + 1));
        }
        else if (words[0].equals("/macrodelete") || words[0].equals("/del"))
        {
            // req. 1 param
            if (words.length < 2)
            {
                logSystemMessage(words[0] + " usage: " + words[0] + " &lt;macroName&gt;");
                return;
            }

            // find and kill this macro
            removeMacro(words[1]);
        }
        else if (words[0].equals("/who"))
        {
            final StringBuffer buffer = new StringBuffer();
            buffer.append("<b><u>Who's connected</u></b><br>");
            for (int i = 0, size = m_players.size(); i < size; ++i)
            {
                final Player player = (Player)m_players.get(i);
                buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                buffer.append(UtilityFunctions.emitUserLink(player.getCharacterName(), player.toString()));
                buffer.append("<br>");
            }
            buffer.append("<b>");
            buffer.append(m_players.size());
            buffer.append(" player");
            buffer.append((m_players.size() > 1 ? "s" : ""));
            buffer.append("</b>");
            logSystemMessage(buffer.toString());
        }
        else if (words[0].equals("/roll") || words[0].equals("/proll"))
        {
            // req. 1 param
            if (words.length < 2)
            {
                logSystemMessage("" + words[0] + " usage: " + words[0] + " &lt;Dice Roll in standard format&gt;");
                logSystemMessage("or: " + words[0]
                    + " &lt;Macro Name&gt; [&lt;+/-&gt; &lt;Macro Name or Dice Roll&gt;]...");
                logSystemMessage("Examples:");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0] + " 2d6 + 3d4 + 8");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0] + " My Damage + d4");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0] + " d20 + My Damage + My Damage Bonus");
                return;
            }

            // TODO: This should all probably be moved to DiceMacro somehow?

            // First we split the roll into terms
            final ArrayList rolls = new ArrayList();
            final ArrayList ops = new ArrayList();
            final String remaining = text.substring((words[0] + " ").length());
            final int length = remaining.length();
            int termStart = 0;
            for (int index = 0; index < length; ++index)
            {
                final char c = remaining.charAt(index);
                final boolean isLast = (index == (length - 1));
                if ((c == '+') || (c == '-') || isLast)
                {
                    final int termEnd = index + (isLast ? 1 : 0);
                    final String term = remaining.substring(termStart, termEnd).trim();
                    if (term.length() < 1)
                    {
                        rolls.add(new DiceMacro());
                    }
                    else
                    {
                        final DiceMacro macro = findMacro(term);
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

            final StringBuffer rollBuf = new StringBuffer();
            final StringBuffer resultBuf = new StringBuffer();
            int total = 0;
            boolean first = true;
            for (int i = 0; i < rolls.size(); ++i)
            {
                final DiceMacro macro = (DiceMacro)rolls.get(i);
                boolean negate = false;
                if (i > 0)
                {
                    if ("-".equals(ops.get(i - 1)))
                    {
                        negate = true;
                    }
                }

                final DiceMacro.Result result = macro.roll();
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
                if ((macro.getName() != null) && (rolls.size() > 1))
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

            if (words[0].equals("/roll"))
            {
                // this was a public roll
                final String toPost = DiceMacro.generateOutputString(getMyPlayer().getCharacterName(), rollBuf
                    .toString(), resultBuf.toString(), "" + total);
                postMessage(toPost);
            }
            else
            {
                // this was a private roll. Don't propagate it to other players
                final String toPost = DiceMacro.generatePrivateOutputString(rollBuf.toString(), resultBuf.toString(),
                    "" + total);
                logMessage(toPost);
            }
        }
        else if (words[0].equals("/poglist"))
        {
            // macro command. this requires at least 2 parameters
            if (words.length < 2)
            {
                // tell them the usage and bail
                logSystemMessage("/poglist usage: /poglist &lt;attribute name&gt;");
                logSystemMessage("Examples:");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/poglist HP");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/poglist Initiative");
                logSystemMessage("Note: attribute names are case, whitespace, and punctuation-insensitive.");
                return;
            }

            final String name = UtilityFunctions.stitchTogetherWords(words, 1);
            final GametableMap map = m_gametableCanvas.getActiveMap();
            final List pogs = map.m_pogs;
            final StringBuffer buffer = new StringBuffer();
            buffer.append("<b><u>Pogs with \'" + name + "\' attribute</u></b><br>");
            int tally = 0;
            for (int i = 0, size = pogs.size(); i < size; ++i)
            {
                final Pog pog = (Pog)pogs.get(i);
                final String value = pog.getAttribute(name);
                if ((value != null) && (value.length() > 0))
                {
                    String pogText = pog.getText();
                    if ((pogText == null) || (pogText.length() == 0))
                    {
                        pogText = "&lt;unknown&gt;";
                    }

                    buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>");
                    buffer.append(pogText);
                    buffer.append(":</b> ");
                    buffer.append(value);
                    buffer.append("<br>");
                    ++tally;
                }
            }
            buffer.append("<b>" + tally + " pog" + (tally != 1 ? "s" : "") + " found.</b>");
            logSystemMessage(buffer.toString());
        }
        else if (words[0].equals("/tell") || words[0].equals("/send"))
        {
            // send a private message to another player
            if (words.length < 3)
            {
                // tell them the usage and bail
                logSystemMessage(words[0] + " usage: " + words[0] + " &lt;player name&gt; &lt;message&gt;");
                logSystemMessage("Examples:");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0]
                    + " Dave I am the most awesome programmer on Gametable!");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + words[0] + " Andy No you're not, you suck!");
                return;
            }

            // they have a legitimate /tell or /send
            final String toName = words[1];

            // see if there is a player or character with that name
            // and note the "proper" name for them (which is their player name)
            Player toPlayer = null;
            for (int i = 0; i < m_players.size(); i++)
            {
                final Player player = (Player)m_players.get(i);
                if (player.hasName(toName))
                {
                    toPlayer = player;
                    break;
                }
            }

            if (toPlayer == null)
            {
                // nobody by that name is in the session
                logAlertMessage("There is no player or character named \"" + toName + "\" in the session.");
                return;
            }

            // now get the message portion
            // we have to do this with the original text, cause the words[] array
            // will have stripped a lot of whitespace if they had multiple spaces, etc.
            // indexOf(toName) will get us to the start of the player name it's being sent to
            // we then add the length of the name to get past that
            final int start = text.indexOf(toName) + toName.length();
            final String toSend = text.substring(start).trim();

            tell(toPlayer, toSend);
        }
        else if (words[0].equals("/em") || words[0].equals("/me") || words[0].equals("/emote"))
        {
            if (words.length < 2)
            {
                // tell them the usage and bail
                logSystemMessage("/emote usage: /emote &lt;action&gt;");
                logSystemMessage("Examples:");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/emote gets a beer.");
                return;
            }

            // get the portion of the text after the emote command
            final int start = text.indexOf(words[0]) + words[0].length();
            final String emote = text.substring(start).trim();

            // simply post text that's an emote instead of a character action
            final String toPost = EMOTE_MESSAGE_FONT + UtilityFunctions.emitUserLink(getMyPlayer().getCharacterName())
                + " " + emote + END_EMOTE_MESSAGE_FONT;
            postMessage(toPost);
        }
        else if (words[0].equals("/as"))
        {
            if (words.length < 3)
            {
                // tell them the usage and bail
                logSystemMessage("/as usage: /as &lt;name&gt; &lt;text&gt;");
                logSystemMessage("Examples:");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/as Balthazar Prepare to meet your doom!.");
                logSystemMessage("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/as Lord_Doom Prepare to meet um... me!");
                logSystemMessage("Note: Underscore characters in the name you specify will be turned into spaces");
                return;
            }
            final StringBuffer speakerName = new StringBuffer(words[1]);

            for (int i = 0; i < speakerName.length(); i++)
            {
                if (speakerName.charAt(i) == '_')
                {
                    speakerName.setCharAt(i, ' ');
                }
            }

            // get the portion of the text after the emote command
            final int start = text.indexOf(words[1]) + words[1].length();
            final String toSay = text.substring(start).trim();

            // simply post text that's an emote instead of a character action
            final String toPost = EMOTE_MESSAGE_FONT + speakerName + ": " + END_EMOTE_MESSAGE_FONT + toSay;
            postMessage(toPost);
        }
        else if (words[0].equals("/goto"))
        {
            if (words.length < 2)
            {
                logSystemMessage(words[0] + " usage: " + words[0] + " &lt;pog name&gt;");
                return;
            }

            final String name = UtilityFunctions.stitchTogetherWords(words, 1);
            final Pog pog = m_gametableCanvas.getActiveMap().getPogNamed(name);
            if (pog == null)
            {
                logAlertMessage("Unable to find pog named \"" + name + "\".");
                return;
            }
            m_gametableCanvas.scrollToPog(pog);
        }
        else if (words[0].equals("/clearlog"))
        {
            if (USE_NEW_CHAT_PANE) {
                m_newChatLog.getModel().clear();
            } else {
                m_chatLog.clearText();
            }
        }
        else if (words[0].equals("/deck"))
        {
            // deck commands. there are many
            deckCommand(words);
        }
        else if (words[0].equals("//") || words[0].equals("/help"))
        {
            // list macro commands
            logSystemMessage("<b><u>Slash Commands</u></b><br>"
                + "<b>/as:</b> Display a narrative of a character saying something<br>"
                + "<b>/deck:</b> Various deck actions. type /deck for more details<br>"
                + "<b>/emote:</b> Display an emote<br>" + "<b>/goto:</b> Centers a pog in the map view.<br>"
                + "<b>/help:</b> list all slash commands<br>" + "<b>/macro:</b> macro a die roll<br>"
                + "<b>/macrodelete:</b> deletes an unwanted macro<br>" + "<b>/poglist:</b> lists pogs by attribute<br>"
                + "<b>/proll:</b> roll dice privately<br>" + "<b>/roll:</b> roll dice<br>"
                + "<b>/tell:</b> send a private message to another player<br>"
                + "<b>/who:</b> lists connected players<br>" + "<b>//:</b> list all slash commands");
        }
    }

    public void pingPacketReceived()
    {
        // do nothing for now
    }

    public void playerJoined(final Connection connection, final Player player, final String password)
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
        final LineSegment[] lines = new LineSegment[getGametableCanvas().getPublicMap().getNumLines()];
        for (int i = 0; i < getGametableCanvas().getPublicMap().getNumLines(); i++)
        {
            lines[i] = getGametableCanvas().getPublicMap().getLineAt(i);
        }
        send(PacketManager.makeLinesPacket(lines, -1, -1), player);

        // pogs
        for (int i = 0; i < getGametableCanvas().getPublicMap().getNumPogs(); i++)
        {
            final Pog pog = getGametableCanvas().getPublicMap().getPog(i);
            send(PacketManager.makeAddPogPacket(pog), player);
        }

        // finally, have the player recenter on the host's view
        final int viewCenterX = getGametableCanvas().getWidth() / 2;
        final int viewCenterY = getGametableCanvas().getHeight() / 2;

        // convert to model coordinates
        final Point modelCenter = getGametableCanvas().viewToModel(viewCenterX, viewCenterY);
        send(PacketManager.makeRecenterPacket(modelCenter.x, modelCenter.y, getGametableCanvas().m_zoom), player);

        // let them know we're done sending them data from the login
        send(PacketManager.makeLoginCompletePacket(), player);

        // tell them the decks that are in play
        sendDeckList();
    }

    public void pogDataPacketReceived(final int id, final String s, final Map toAdd, final Set toDelete)
    {
        getGametableCanvas().doSetPogData(id, s, toAdd, toDelete);

        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makePogDataPacket(id, s, toAdd, toDelete));
        }
    }

    public void pogReorderPacketReceived(final Map changes)
    {
        getGametableCanvas().doPogReorder(changes);
        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makePogReorderPacket(changes));
        }
    }

    public void pogSizePacketReceived(final int id, final float size)
    {
        getGametableCanvas().doSetPogSize(id, size);

        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makePogSizePacket(id, size));
        }
    }

    public void pointPacketReceived(final int plrIdx, final int x, final int y, final boolean bPointing)
    {
        // we're not interested in point packets of our own hand
        if (plrIdx != getMyPlayerIndex())
        {
            final Player plr = (Player)m_players.get(plrIdx);
            plr.setPoint(x, y);
            plr.setPointing(bPointing);
        }

        if (m_netStatus == NETSTATE_HOST)
        {
            send(PacketManager.makePointPacket(plrIdx, x, y, bPointing));
        }

        getGametableCanvas().repaint();
    }

    public void postAlertMessage(final String text)
    {
        postMessage(ALERT_MESSAGE_FONT + text + END_ALERT_MESSAGE_FONT);
    }

    public void postMessage(final String text)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            // if you're the host, push to all players
            send(PacketManager.makeTextPacket(text));

            // add it to your own text log
            logMessage(text);
        }
        else if (m_netStatus == NETSTATE_JOINED)
        {
            // if you're a player, just post it to the GM
            send(PacketManager.makeTextPacket(text));
        }
        else
        {
            // if you're offline, just add it to the log
            logMessage(text);
        }
    }

    public void postPrivateMessage(final String fromName, final String toName, final String text)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            // if you're the host, push to the appropriate player(s)
            for (int i = 0; i < m_players.size(); i++)
            {
                final Player player = (Player)m_players.get(i);
                if (player.hasName(toName))
                {
                    // send the message to this player
                    send(PacketManager.makePrivateTextPacket(fromName, toName, text), player);
                }
            }

            // add it to your own text log if we're the right player
            if (getMyPlayer().hasName(toName))
            {
                logPrivateMessage(fromName, toName, text);
            }
        }
        else if (m_netStatus == NETSTATE_JOINED)
        {
            // if you're a player, just post it to the GM
            send(PacketManager.makePrivateTextPacket(fromName, toName, text));
        }
        else
        {
            // if you're offline, post it to yourself if you're the
            // person you sent it to.
            if (getMyPlayer().hasName(toName))
            {
                logPrivateMessage(fromName, toName, text);
            }
        }
    }

    public void postSystemMessage(final String text)
    {
        postMessage(SYSTEM_MESSAGE_FONT + text + END_SYSTEM_MESSAGE_FONT);
    }

    public void privateTextPacketReceived(final String fromName, final String toName, final String text)
    {
        if (m_netStatus == NETSTATE_HOST)
        {
            // if you're the host, push to all players
            postPrivateMessage(fromName, toName, text);
        }
        else
        {
            // otherwise, just add it
            logPrivateMessage(fromName, toName, text);
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

    public void receiveCards(final DeckData.Card cards[])
    {
        if (cards.length == 0)
        {
            // drew 0 cards. Ignore.
            return;
        }

        // all of these cards get added to your hand
        for (int i = 0; i < cards.length; i++)
        {
            m_cards.add(cards[i]);
            final String toPost = "You drew: " + cards[i].m_cardName + " (" + cards[i].m_deckName + ")";
            logSystemMessage(toPost);
        }

        // make sure we're on the private layer
        if (m_gametableCanvas.getActiveMap() != m_gametableCanvas.getPrivateMap())
        {
            // we call toggleLayer rather than setActiveMap because
            // toggleLayer cleanly deals with drags in action and other
            // interrupted actions.
            toggleLayer();
        }

        // tell everyone that you drew some cards
        if (cards.length == 1)
        {
            postSystemMessage(getMyPlayer().getPlayerName() + " draws from the " + cards[0].m_deckName + " deck.");
        }
        else
        {
            postSystemMessage(getMyPlayer().getPlayerName() + " draws " + cards.length + " cards from the "
                + cards[0].m_deckName + " deck.");
        }

    }

    public void recenterPacketReceived(final int x, final int y, final int zoom)
    {
        getGametableCanvas().doRecenterView(x, y, zoom);

        if (m_netStatus == NETSTATE_HOST)
        {
            m_networkThread.send(PacketManager.makeRecenterPacket(x, y, zoom));
        }
    }

    public void redoPacketReceived(final int stateID)
    {
        getGametableCanvas().doRedo(stateID);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeRedoPacket(stateID));
        }

        repaint();
    }

    /**
     * Reacquires pogs and then refreshes the pog list.
     */
    public void refreshActivePogList()
    {
        m_activePogsPanel.refresh();
    }

    /**
     * Refreshes the pog list.
     */
    public void refreshPogList()
    {
        m_pogPanel.populateChildren();
        getGametableCanvas().repaint();
    }

    public void rejectPacketReceived(final int reason)
    {
        confirmJoined();

        // you got rejected!
        switch (reason)
        {
            case REJECT_INVALID_PASSWORD:
            {
                logAlertMessage("Invalid Password. Connection refused.");
            }
            break;

            case REJECT_VERSION_MISMATCH:
            {
                logAlertMessage("The host is using a different version of the Gametable network protocol."
                    + " Connection aborted.");
            }
            break;
        }
        disconnect();
    }

    public void removeMacro(final DiceMacro dm)
    {
        removeMacroForced(dm);
        m_macroPanel.refreshMacroList();
    }

    public void removeMacro(final String name)
    {
        removeMacroForced(name);
        m_macroPanel.refreshMacroList();
    }

    private void removeMacroForced(final DiceMacro macro)
    {
        final String name = UtilityFunctions.normalizeName(macro.getName());
        m_macroMap.remove(name);
    }

    private void removeMacroForced(final String name)
    {
        final DiceMacro macro = getMacro(name);
        if (macro != null)
        {
            removeMacroForced(macro);
        }
    }

    public void removePogsPacketReceived(final int ids[])
    {
        getGametableCanvas().doRemovePogs(ids, false);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeRemovePogsPacket(ids));
        }
    }

    public void requestCardsPacketReceived(final Connection conn, final String deckName, final int numCards)
    {
        if (m_netStatus != NETSTATE_HOST)
        {
            // this shouldn't happen
            throw new IllegalStateException("Non-host had a call to requestCardsPacketReceived");
        }
        // the player at conn wants some cards
        final DeckData.Card cards[] = getCards(deckName, numCards);

        if (cards == null)
        {
            // there was a problem. Probably a race-condition thaty caused a
            // card request to get in after a deck was deleted. Just ignore this
            // packet.
            return;
        }

        // send that player his cards
        send(PacketManager.makeReceiveCardsPacket(cards), conn);

        // also, we need to send that player the pogs for each of those cards
        for (int i = 0; i < cards.length; i++)
        {
            final Pog newPog = makeCardPog(cards[i]);
            newPog.assignUniqueId();

            if (newPog != null)
            {
                // make a pog packet, saying this pog should go to the PRIVATE LAYER,
                // then send it to that player. Note that we don't add it to
                // our own layer.
                send(PacketManager.makeAddPogPacket(newPog, false), conn);
            }
        }
    }

    public void rotatePogPacketReceived(final int id, final double newAngle)
    {
        getGametableCanvas().doRotatePog(id, newAngle);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeRotatePogPacket(id, newAngle));
        }
    }

    public void flipPogPacketReceived(final int id, final int flipH, final int flipV)
    {
        getGametableCanvas().doFlipPog(id, flipH, flipV);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeFlipPogPacket(id, flipH, flipV));
        }
    }

    public boolean runHostDialog()
    {
        final JoinDialog dialog = new JoinDialog();
        dialog.setUpForHostDlg();
        dialog.setLocationRelativeTo(m_gametableCanvas);
        dialog.setVisible(true);

        if (!dialog.m_bAccepted)
        {
            // they cancelled out
            return false;
        }
        return true;
    }

    private boolean runJoinDialog()
    {
        final JoinDialog dialog = new JoinDialog();
        dialog.setLocationRelativeTo(m_gametableCanvas);
        dialog.setVisible(true);

        if (!dialog.m_bAccepted)
        {
            // they cancelled out
            return false;
        }
        return true;
    }

    /**
     * Saves everything: both maps, macros, and preferences. 
     * Called on program exit.
     */
    private void saveAll()
    {
        saveState(getGametableCanvas().getPublicMap(), new File("autosave.grm"));
        saveState(getGametableCanvas().getPrivateMap(), new File("autosavepvt.grm"));
        savePrefs();
    }

    public void saveMacros()
    {
        final File oldFile = m_actingFileMacros;
        m_actingFileMacros = UtilityFunctions.doFileSaveDialog("Save As", "xml", true);
        if (m_actingFileMacros == null)
        {
            m_actingFileMacros = oldFile;
            return;
        }

        try
        {
            saveMacros(m_actingFileMacros);
            logSystemMessage("Wrote macros to " + m_actingFileMacros.getPath());
        }
        catch (final IOException ioe)
        {
            Log.log(Log.SYS, ioe);
        }
    }

    public void saveMacros(final File file) throws IOException
    {
        final XmlSerializer out = new XmlSerializer();
        out.startDocument(new BufferedWriter(new FileWriter(file)));
        out.startElement(DiceMacroSaxHandler.ELEMENT_DICE_MACROS);
        for (final Iterator iterator = m_macroMap.values().iterator(); iterator.hasNext();)
        {
            final DiceMacro macro = (DiceMacro)iterator.next();
            macro.serialize(out);
        }
        out.endElement();
        out.endDocument();
    }

    public void savePrefs()
    {
        try
        {
            final FileOutputStream prefFile = new FileOutputStream("prefs.prf");
            final DataOutputStream prefDos = new DataOutputStream(prefFile);

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

            prefDos.writeUTF(m_actingFileMacros.getAbsolutePath());
            prefDos.writeBoolean(m_showNamesCheckbox.isSelected());

            prefDos.close();
            prefFile.close();

            saveMacros(m_actingFileMacros);
        }
        catch (final FileNotFoundException ex1)
        {
            Log.log(Log.SYS, ex1);
        }
        catch (final IOException ex1)
        {
            Log.log(Log.SYS, ex1);
        }
    }

    public void saveState(final GametableMap mapToSave, final File file)
    {
        // save out all our data. The best way to do this is with packets, cause they're
        // already designed to pass data around.
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);

        try
        {
            final LineSegment[] lines = new LineSegment[mapToSave.getNumLines()];
            for (int i = 0; i < mapToSave.getNumLines(); i++)
            {
                lines[i] = mapToSave.getLineAt(i);
            }
            final byte[] linesPacket = PacketManager.makeLinesPacket(lines, -1, -1);
            dos.writeInt(linesPacket.length);
            dos.write(linesPacket);

            // pogs
            for (int i = 0; i < mapToSave.getNumPogs(); i++)
            {
                final Pog pog = mapToSave.getPog(i);
                final byte[] pogsPacket = PacketManager.makeAddPogPacket(pog);
                dos.writeInt(pogsPacket.length);
                dos.write(pogsPacket);
            }

            // grid state
            final byte gridModePacket[] = PacketManager.makeGridModePacket(getGametableCanvas().getGridModeId());
            dos.writeInt(gridModePacket.length);
            dos.write(gridModePacket);

            final byte[] saveFileData = baos.toByteArray();
            final FileOutputStream output = new FileOutputStream(file);
            final DataOutputStream fileOut = new DataOutputStream(output);
            fileOut.writeInt(COMM_VERSION);
            fileOut.writeInt(saveFileData.length);
            fileOut.write(saveFileData);
            output.close();
            fileOut.close();
            baos.close();
            dos.close();
        }
        catch (final IOException ex)
        {
            Log.log(Log.SYS, ex);
            // failed to save. give up
        }
    }

    /**
     * Sends a public message to all players.
     * 
     * @param text Message to send.
     */
    public void say(final String text)
    {
        postMessage(SAY_MESSAGE_FONT + UtilityFunctions.emitUserLink(getMyPlayer().getCharacterName()) + ": "
            + END_SAY_MESSAGE_FONT + text);
    }

    public void send(final byte[] packet)
    {
        if (m_networkThread != null)
        {
            m_networkThread.send(packet);
        }
    }

    public void send(final byte[] packet, final Connection connection)
    {
        if (m_networkThread != null)
        {
            m_networkThread.send(packet, connection);
        }
    }

    public void send(final byte[] packet, final Player player)
    {
        if (player.getConnection() == null)
        {
            return;
        }
        send(packet, player.getConnection());
    }

    public void sendCastInfo()
    {
        // and we have to push this data out to everyone
        for (int i = 0; i < m_players.size(); i++)
        {
            final Player recipient = (Player)m_players.get(i);
            final byte[] castPacket = PacketManager.makeCastPacket(recipient);
            send(castPacket, recipient);
        }
    }

    void sendDeckList()
    {
        send(PacketManager.makeDeckListPacket(m_decks));
    }

    public void setToolSelected(final int toolId)
    {
        m_toolButtons[toolId].setSelected(true);
    }

    public boolean shouldShowNames()
    {
        return m_showNamesCheckbox.isSelected();
    }

    public void showDeckUsage()
    {
        logSystemMessage("/deck usage: ");
        logSystemMessage("---/deck create [decktype] [deckname]: create a new deck. [decktype] is the name of a deck in the decks directory. It will be named [deckname]");
        logSystemMessage("---/deck destroy [deckname]: remove the specified deck from the session.");
        logSystemMessage("---/deck shuffle [deckname] ['all' or 'discards']: shuffle cards back in to the deck.");
        logSystemMessage("---/deck draw [deckname] [number]: draw [number] cards from the specified deck.");
        logSystemMessage("---/deck hand [deckname]: List off the cards (and their ids) you have from the specified deck.");
        logSystemMessage("---/deck /discard [cardID]: Discard a card. A card's ID can be seen by using /hand.");
        logSystemMessage("---/deck /discard all: Discard all cards that you have.");
        logSystemMessage("---/deck decklist: Lists all the decks in play.");
    }

    public void startTellTo(final String name)
    {
        m_textEntry.setText("/tell " + name + "<b> </b>");
        m_textEntry.requestFocus();
        m_textEntry.toggleStyle("bold");
        m_textEntry.toggleStyle("bold");
    }

    /**
     * Sends a private message to the target player.
     * 
     * @param target Player to address message to.
     * @param text Message to send.
     */
    public void tell(final Player target, final String text)
    {
        if (target.getId() == getMyPlayer().getId())
        {
            logMessage(PRIVATE_MESSAGE_FONT + "You tell yourself: " + END_PRIVATE_MESSAGE_FONT + text);
            return;
        }

        final String fromName = getMyPlayer().getCharacterName();
        final String toName = target.getCharacterName();

        postPrivateMessage(fromName, toName, text);

        // and when you post a private message, you get told about it in your
        // own chat log
        logMessage(PRIVATE_MESSAGE_FONT + "You tell " + UtilityFunctions.emitUserLink(toName) + ": "
            + END_PRIVATE_MESSAGE_FONT + text);
    }

    public void textPacketReceived(final String text)
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

    private void tick()
    {
        final long now = System.currentTimeMillis();
        long diff = now - m_lastTickTime;
        if (m_lastTickTime == 0)
        {
            diff = 0;
        }
        m_lastTickTime = now;
        tick(diff);
    }

    private void tick(final long ms)
    {
        // System.out.println("tick(" + ms + ")");
        final NetworkThread thread = m_networkThread;
        if (thread != null)
        {
            final Set lostConnections = thread.getLostConnections();
            Iterator iterator = lostConnections.iterator();
            while (iterator.hasNext())
            {
                final Connection connection = (Connection)iterator.next();
                connectionDropped(connection);
            }

            final List packets = thread.getPackets();
            iterator = packets.iterator();
            while (iterator.hasNext())
            {
                final Packet packet = (Packet)iterator.next();
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
            updateStatus();
        }
        m_gametableCanvas.tick(ms);
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

        // if they toggled the layer, whatever tool they're using is cancelled
        getToolManager().cancelToolAction();
        getGametableCanvas().requestFocus();
        refreshActivePogList();
        repaint();
    }

    public void typingPacketReceived(final String playerName, final boolean typing)
    {
        if (typing)
        {
            m_typing.add(playerName);
        }
        else
        {
            m_typing.remove(playerName);
        }

        if (m_netStatus == NETSTATE_HOST)
        {
            send(PacketManager.makeTypingPacket(playerName, typing));
        }
    }

    public void undoPacketReceived(final int stateID)
    {
        getGametableCanvas().doUndo(stateID);

        if (m_netStatus == NETSTATE_HOST)
        {
            // if we're the host, send it to the clients
            send(PacketManager.makeUndoPacket(stateID));
        }

        repaint();
    }

    public void updateCast(final Player[] players, final int ourIdx)
    {
        // you should only get this if you're a joiner
        confirmJoined();

        // set up the current cast
        m_players = new ArrayList();
        for (int i = 0; i < players.length; i++)
        {
            addPlayer(players[i]);
        }

        m_myPlayerIndex = ourIdx;

        // any time the cast changes, all the undo stacks clear
        getGametableCanvas().clearUndoStacks();
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

    public void updateStatus()
    {
        switch (m_netStatus)
        {
            case NETSTATE_NONE:
            {
                m_status.setText(" Disconnected");
            }
            break;

            case NETSTATE_JOINED:
            {
                m_status.setText(" Connected: ");
            }
            break;

            case NETSTATE_HOST:
            {
                m_status.setText(" Hosting: ");
            }
            break;

            default:
            {
                m_status.setText(" Unknown state; ");
            }
            break;
        }

        if (m_netStatus != NETSTATE_NONE)
        {
            m_status.setText(m_status.getText() + m_players.size() + " player" + (m_players.size() == 1 ? "" : "s")
                + " connected. ");
            switch (m_typing.size())
            {
                case 0:
                {
                }
                break;

                case 1:
                {
                    m_status.setText(m_status.getText() + m_typing.get(0) + " is typing.");
                }
                break;

                case 2:
                {
                    m_status.setText(m_status.getText() + m_typing.get(0) + " and " + m_typing.get(1) + " are typing.");
                }
                break;

                default:
                {
                    for (int i = 0; i < m_typing.size() - 1; i++)
                    {
                        m_status.setText(m_status.getText() + m_typing.get(i) + ", ");
                    }
                    m_status.setText(m_status.getText() + " and " + m_typing.get(m_typing.size() - 1) + " are typing.");
                }
            }
        }
    }

    /**
     * Records the current state of the window.
     */
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
}
