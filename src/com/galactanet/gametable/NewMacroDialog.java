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
    private JButton    m_ok          = new JButton();
    private JButton    m_cancel      = new JButton();

    private boolean    m_bAccepted;
    private JTextField m_nameEntry   = new JTextField();
    private JTextField m_rollEntry   = new JTextField();
    private JLabel     nameLabel     = new JLabel();
    private JLabel     dieLabel      = new JLabel();
    private JLabel     dieHelpLabel1 = new JLabel();
    private JLabel     dieHelpLabel2 = new JLabel();

    public NewMacroDialog()
    {
        try
        {
            initialize();
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
        }

        // pack yourself
        pack();

        // center yourself
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        m_nameEntry.requestFocus();
    }

    private void initialize()
    {
        setTitle("Add a Dice Macro");
        setResizable(false);
        m_rollEntry.setText("d20");
        m_nameEntry.setText("");
        m_ok.setText("OK");
        m_ok.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
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
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });

        nameLabel.setText("Macro Name:");
        dieLabel.setText("Die Roll:");
        dieHelpLabel1.setText("Enter a name, and a die roll in standard");
        dieHelpLabel2.setText("notation. (Ex: 3d6 + d8 + 4)");

        final int PADDING = 5;

        Box outmostBox = Box.createHorizontalBox();
        getContentPane().add(outmostBox, BorderLayout.CENTER);
        outmostBox.add(Box.createHorizontalStrut(PADDING));
        Box outerBox = Box.createVerticalBox();
        outmostBox.add(outerBox);
        outmostBox.add(Box.createHorizontalStrut(PADDING));

        outerBox.add(Box.createVerticalStrut(PADDING));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(nameLabel);
        outerBox.add(panel);
        outerBox.add(Box.createVerticalStrut(PADDING));
        outerBox.add(m_nameEntry);

        outerBox.add(Box.createVerticalStrut(PADDING * 2));

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        Box nextBox = Box.createVerticalBox();
        nextBox.add(dieLabel);
        nextBox.add(dieHelpLabel1);
        nextBox.add(dieHelpLabel2);
        panel.add(nextBox);
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
        m_rollEntry.addFocusListener(this);
        setModal(true);
    }

    public void focusGained(FocusEvent e)
    {
        // only interested in JTextFields
        if (!(e.getSource() instanceof JTextField))
        {
            return;
        }

        JTextField focused = (JTextField)e.getSource();
        focused.setSelectionStart(0);
        focused.setSelectionEnd(focused.getText().length());
    }

    public void focusLost(FocusEvent e)
    {
    }
    
    public void initializeValues(String name, String definition)
    {
        m_nameEntry.setText(name);
        m_rollEntry.setText(definition);
    }

    public boolean isAccepted()
    {
        return m_bAccepted;
    }

    public String getMacroName()
    {
        return m_nameEntry.getText();
    }

    public String getMacroDefinition()
    {
        return m_rollEntry.getText();
    }
}
