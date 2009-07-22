/*
 * ChatCommandBar.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.ui.chat;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.galactanet.gametable.util.UtilityFunctions;



/**
 * Command bar for a styled text entry area.
 * 
 * @author iffy
 */
public class StyledEntryToolbar extends JPanel
{
    /**
     * 
     */
    private static final long      serialVersionUID = 7011000446037690152L;

    private JToggleButton          boldButton;

    private final ChatLogEntryPane entryPane;
    private JToggleButton          italicsButton;
    private JToggleButton          underlineButton;

    /**
     * Contructor.
     */
    public StyledEntryToolbar(final ChatLogEntryPane entry)
    {
        entryPane = entry;
        entryPane.setToolbar(this);

        initialize();
    }

    private void initialize()
    {
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

        final Insets margin = new Insets(1, 1, 1, 1);
        boldButton = new JToggleButton(new ImageIcon(UtilityFunctions.getImage("assets/bold.png")));
        boldButton.setMargin(margin);
        boldButton.setToolTipText("<html><b>Bolds</b> selected text.");
        boldButton.setFocusable(false);
        boldButton.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                entryPane.toggleStyle("bold");
            }
        });
        add(boldButton);

        italicsButton = new JToggleButton(new ImageIcon(UtilityFunctions.getImage("assets/italics.png")));
        italicsButton.setMargin(margin);
        italicsButton.setToolTipText("<html><i>Italicizes</i> selected text.");
        italicsButton.setFocusable(false);
        italicsButton.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                entryPane.toggleStyle("italics");
            }
        });
        add(italicsButton);

        underlineButton = new JToggleButton(new ImageIcon(UtilityFunctions.getImage("assets/underline.png")));
        underlineButton.setMargin(margin);
        underlineButton.setToolTipText("<html><u>Underlines</u> selected text.");
        underlineButton.setFocusable(false);
        underlineButton.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                entryPane.toggleStyle("underline");
            }
        });
        add(underlineButton);
    }

    public void updateStyles()
    {
        boldButton.setSelected(entryPane.isCurrentStyle("bold"));
        italicsButton.setSelected(entryPane.isCurrentStyle("italics"));
        underlineButton.setSelected(entryPane.isCurrentStyle("underline"));
    }
}
