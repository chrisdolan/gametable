/*
 * NewMacroDialog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class NewMacroDialog extends JDialog implements FocusListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 7635834691550405596L;
    private final JLabel      dieHelpLabel1    = new JLabel();
    private final JLabel      dieHelpLabel2    = new JLabel();

    private final JLabel      dieLabel         = new JLabel();
    private boolean           m_bAccepted;
    private final JButton     m_cancel         = new JButton();
    private final JTextField  m_nameEntry      = new JTextField();
    private final JTextField  m_parentEntry    = new JTextField();
    private final JButton     m_ok             = new JButton();
    private final JTextField  m_rollEntry      = new JTextField();
    private final JLabel      nameLabel        = new JLabel();
//    private final JLabel      parentLabel      = new JLabel();
//    private final JLabel      parentHelpLabel1 = new JLabel();

    public NewMacroDialog()
    {
        try
        {
            initialize();
        }
        catch (final Exception e)
        {
            Log.log(Log.SYS, e);
        }

        // pack yourself
        pack();

        // center yourself
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension frameSize = getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        m_nameEntry.requestFocus();
    }

    public void focusGained(final FocusEvent e)
    {
        // only interested in JTextFields
        if (!(e.getSource() instanceof JTextField))
        {
            return;
        }

        final JTextField focused = (JTextField)e.getSource();
        focused.setSelectionStart(0);
        focused.setSelectionEnd(focused.getText().length());
    }

    public void focusLost(final FocusEvent e)
    {
    }

    public String getMacroDefinition()
    {
        return m_rollEntry.getText();
    }

    public String getMacroName()
    {
        return m_nameEntry.getText();
    }

    public String getMacroParent()
    {
        return m_parentEntry.getText();
    }

    private void initialize()
    {
        setTitle("Add a Dice Macro");
        setResizable(false);
        m_rollEntry.setText("d20");
        m_nameEntry.setText("");
        m_parentEntry.setText("");
        m_ok.setText("OK");
        m_ok.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (m_rollEntry.getText().length() > 0)
                {
                    m_bAccepted = true;
                }
                dispose();
            }
        });

        m_cancel.setText("Cancel");
        m_cancel.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                dispose();
            }
        });

        nameLabel.setText("Macro Name:");
//        parentLabel.setText("Macro Group:");
//        parentHelpLabel1.setText("Enter the group in which the macro belongs.");
        dieLabel.setText("Die Roll:");
        dieHelpLabel1.setText("Enter a name, and a die roll in standard");
        dieHelpLabel2.setText("notation. (Ex: 3d6 + d8 + 4)");

        final int PADDING = 5;

        final Box outmostBox = Box.createHorizontalBox();
        getContentPane().add(outmostBox, BorderLayout.CENTER);
        outmostBox.add(Box.createHorizontalStrut(PADDING));
        final Box outerBox = Box.createVerticalBox();
        outmostBox.add(outerBox);
        outmostBox.add(Box.createHorizontalStrut(PADDING));

        outerBox.add(Box.createVerticalStrut(PADDING));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(nameLabel);
        outerBox.add(panel);
        outerBox.add(Box.createVerticalStrut(PADDING));
        outerBox.add(m_nameEntry);

        outerBox.add(Box.createVerticalStrut(PADDING * 2));

//        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
//        final Box nextBox = Box.createVerticalBox();
//        nextBox.add(parentLabel);
//        nextBox.add(parentHelpLabel1);
//        panel.add(nextBox);
//        outerBox.add(panel);
//        outerBox.add(Box.createVerticalStrut(PADDING));
//        outerBox.add(m_parentEntry);
//
//        outerBox.add(Box.createVerticalStrut(PADDING * 2));

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        final Box nextBox2 = Box.createVerticalBox();
        nextBox2.add(dieLabel);
        nextBox2.add(dieHelpLabel1);
        nextBox2.add(dieHelpLabel2);
        panel.add(nextBox2);
        outerBox.add(panel);
        outerBox.add(Box.createVerticalStrut(PADDING));
        outerBox.add(m_rollEntry);

        outerBox.add(Box.createVerticalStrut(PADDING * 3));
        outerBox.add(Box.createVerticalGlue());

        panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        outerBox.add(panel);
        panel.add(m_ok);
        panel.add(Box.createHorizontalStrut(PADDING));
        panel.add(m_cancel);

        outerBox.add(Box.createVerticalStrut(PADDING));

        // we want to know if any of those text entry areas get focus
        m_nameEntry.addFocusListener(this);
        m_parentEntry.addFocusListener(this);
        m_rollEntry.addFocusListener(this);
        setModal(true);
    }

    public void initializeValues(final String name, final String definition, final String parent)
    {
        m_nameEntry.setText(name);
        m_parentEntry.setText(parent);
        m_rollEntry.setText(definition);
    }

    public boolean isAccepted()
    {
        return m_bAccepted;
    }
}
