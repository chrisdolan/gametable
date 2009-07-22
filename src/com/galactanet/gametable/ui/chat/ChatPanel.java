/*
 * ChatPanel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.ui.chat;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.Player;
import com.galactanet.gametable.ui.chat.ChatLogEntryPane;
import com.galactanet.gametable.ui.chat.ChatLogPane;
import com.galactanet.gametable.ui.chat.PrivateMessageDialog;
import com.galactanet.gametable.util.UtilityFunctions;

/**
 * TODO: comment
 * 
 * @author Rizban
 */
public class ChatPanel extends JPanel
{
    private JFrame                  m_chatWindow           = null;

    private boolean                 m_useMechanicsLog      = false;
    public String                   m_lastPrivateMessageSender;     // the name of the last person who sent a private message
    private final ChatLogPane       m_mechanicsLog         = new ChatLogPane(1, false); // 1 = not default (for now)
    private final ChatLogPane       m_chatLog              = new ChatLogPane(0, false); // 0 = default chat log
    private final JSplitPane        m_chatSplitPane        = new JSplitPane();    // The chat pane is really a split between the chat and mechanics

    private final ChatLogEntryPane  m_textEntry            = new ChatLogEntryPane();
    private final JPanel            m_textAreaPanel        = new JPanel();
    private final JPanel            m_textAndEntryPanel    = new JPanel();

    // List of players to whom to send a private message
    private final JComboBox         pmSendTo               = new JComboBox();
//    private int                     pmToID                 = 0;
    
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

    public final static int       NETSTATE_HOST            = 1;
    public final static int       NETSTATE_JOINED          = 2;
    public final static int       NETSTATE_NONE            = 0;

    public ChatPanel()
    {
        initialize();
    }
    
    private void initialize()
    {
        this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.setLayout(new BorderLayout());

        // Configure chat typing panel
        m_textAndEntryPanel.setLayout(new BorderLayout());

        final JPanel entryPanel = new JPanel(new BorderLayout(0, 0));
        entryPanel.add(new StyledEntryToolbar(m_textEntry), BorderLayout.NORTH);
        entryPanel.add(m_textEntry.getComponentToAdd(), BorderLayout.SOUTH);
        m_textAndEntryPanel.add(entryPanel, BorderLayout.SOUTH);
        
        if(m_useMechanicsLog) {
            m_chatSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            m_chatSplitPane.setContinuousLayout(true);
            m_chatSplitPane.setResizeWeight(1.0);
            m_chatSplitPane.setBorder(null);        
            m_chatSplitPane.add(m_chatLog.getComponentToAdd(),JSplitPane.LEFT);
            m_chatSplitPane.add(m_mechanicsLog.getComponentToAdd(), JSplitPane.RIGHT);
            m_textAndEntryPanel.add(m_chatSplitPane, BorderLayout.CENTER);
        } else {        
            m_textAndEntryPanel.add(m_chatLog.getComponentToAdd(), BorderLayout.CENTER);
        }
        
        m_textAreaPanel.setLayout(new BorderLayout());
        m_textAreaPanel.add(m_textAndEntryPanel, BorderLayout.CENTER);
        this.add(m_textAreaPanel, BorderLayout.CENTER);
    }

    public void init_sendTo() {
        pmSendTo.removeAllItems();
        for(int i = 0;i < GametableFrame.getGametableFrame().getPlayers().size(); i++) {
            final Player player = (Player)GametableFrame.getGametableFrame().getPlayers().get(i);
            pmSendTo.addItem(player.getCharacterName());
        }
    }

    public void clearText() {
        m_chatLog.clearText();
    }

    public JSplitPane getChatSplitPane()
    {
        return m_chatSplitPane;
    }

    public JFrame getChatWindow()
    {
        return m_chatWindow;
    }

    public JComboBox getpmSendTo()
    {
        return pmSendTo;
    }

    public ChatLogEntryPane getTextEntry()
    {
        return m_textEntry;
    }

    public boolean getUseMechanicsLog()
    {
        return m_useMechanicsLog;
    }

    public void logAlertMessage(final String text)
    {
        logMechanics(ALERT_MESSAGE_FONT + text + END_ALERT_MESSAGE_FONT);
    }

    public void logMessage(final String text)
    {
        m_chatLog.addText(text);
    }

    public void logMechanics(final String text)
    {
        if(m_useMechanicsLog) m_mechanicsLog.addText(text);
        else logMessage(text);
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
        logMechanics(SYSTEM_MESSAGE_FONT + text + END_SYSTEM_MESSAGE_FONT);
    }

    public boolean openPrivChatWindowDialog()
    {
//        if (GametableFrame.getGametableFrame().getNetStatus() != NETSTATE_HOST && 
//              GametableFrame.getGametableFrame().getNetStatus() != NETSTATE_JOINED)
//        {
//            logMechanics(SYSTEM_MESSAGE_FONT + "You must be connected to open a private chat."
//                + END_SYSTEM_MESSAGE_FONT);
//            return false;
//        }
        
        final PrivateMessageDialog pmDialog = new PrivateMessageDialog();
        pmDialog.setLocationRelativeTo(GametableFrame.getGametableFrame().getGametableCanvas());
        pmDialog.setVisible(true);

        if (!pmDialog.m_bAccepted)
        {
            // they canceled out
            return false;
        }
        return true;
    }

    public void setChatSplitPaneDivider (final int divLoc)
    {
        m_chatSplitPane.setDividerLocation(divLoc);
    }

    public void setUseMechanicsLog(final boolean useMechanicsLog)
    {
        m_useMechanicsLog = useMechanicsLog;
    }
    
    public void toggleMechanicsWindow()
    {
        //Rebuild ChatPanel
        if(!m_useMechanicsLog) {
            m_chatSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            m_chatSplitPane.setContinuousLayout(true);
            m_chatSplitPane.setResizeWeight(1.0);
            m_chatSplitPane.setBorder(null);        
            m_chatSplitPane.add(m_mechanicsLog.getComponentToAdd(), JSplitPane.RIGHT);
            m_chatSplitPane.add(m_chatLog.getComponentToAdd(),JSplitPane.LEFT);
            m_textAndEntryPanel.add(m_chatSplitPane, BorderLayout.CENTER);
            m_useMechanicsLog = true;
            m_mechanicsLog.addText("Mechanics Output window has been Enabled.");
        } else {        
            m_textAndEntryPanel.remove(m_chatSplitPane);
            m_textAndEntryPanel.validate();

            m_textAndEntryPanel.add(m_chatLog.getComponentToAdd(), BorderLayout.CENTER);
            m_useMechanicsLog = false;
            logMessage("Mechanics Output window has been Disabled.");
        }
        m_textAreaPanel.add(m_textAndEntryPanel, BorderLayout.CENTER);
        this.add(m_textAreaPanel, BorderLayout.CENTER);
    }
}
