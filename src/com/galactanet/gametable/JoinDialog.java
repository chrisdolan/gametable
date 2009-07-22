/*
 * JoinDialog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
public class JoinDialog extends JDialog implements FocusListener
{
    /**
     * 
     */
    private static final long serialVersionUID  = -7877135247158193423L;
    JLabel                    jLabel1           = new JLabel();
    JLabel                    jLabel2           = new JLabel();
    JLabel                    jLabel3           = new JLabel();
    JLabel                    jLabel4           = new JLabel();
    boolean                   m_bAccepted;
    JButton                   m_cancel          = new JButton();
    JTextField                m_charNameEntry   = new JTextField();
    JLabel                    m_enterHostLabel  = new JLabel();
    CardLayout                m_hostPanelLayout = new CardLayout(0, 0);
    JPanel                    m_hostPanel       = new JPanel(m_hostPanelLayout);

    JButton                   m_ok              = new JButton();
    JTextField                m_passwordEntry   = new JTextField();
    JTextField                m_plrNameEntry    = new JTextField();
    JTextField                m_portEntry       = new JTextField();
    JTextField                m_textEntry       = new JTextField();

    public JoinDialog()
    {
        try
        {
            initialize();
        }
        catch (final RuntimeException e)
        {
            Log.log(Log.SYS, e);
        }

        // pack yourself
        pack();

        m_ok.requestFocus();
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

    private void getPort()
    {
        try
        {
            GametableFrame.getGametableFrame().m_port = Integer.parseInt(m_portEntry.getText());
        }
        catch (final NumberFormatException ex)
        {
            GametableFrame.getGametableFrame().m_port = GametableFrame.DEFAULT_PORT;
        }
    }

    private void initialize()
    {
        setModal(true);
        setResizable(false);

        m_ok.setText("OK");
        m_ok.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_bAccepted = true;

                // update the default names
                GametableFrame.getGametableFrame().m_characterName = m_charNameEntry.getText();
                GametableFrame.getGametableFrame().m_playerName = m_plrNameEntry.getText();
                GametableFrame.getGametableFrame().m_ipAddress = m_textEntry.getText();
                GametableFrame.getGametableFrame().m_password = m_passwordEntry.getText();
                getPort();

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

        m_enterHostLabel.setText("Enter Host Address");

        jLabel2.setText("Player Name:");
        jLabel3.setText("Char Name:");
        jLabel1.setText("Port:");
        jLabel4.setText("Password:");

        final int PADDING = 5;

        final Box outmostBox = Box.createHorizontalBox();
        getContentPane().add(outmostBox, BorderLayout.CENTER);
        outmostBox.add(Box.createHorizontalStrut(PADDING));
        final Box outerBox = Box.createVerticalBox();
        outmostBox.add(outerBox);
        outmostBox.add(Box.createHorizontalStrut(PADDING));

        outerBox.add(Box.createVerticalStrut(PADDING));

        Box nextBox = Box.createHorizontalBox();
        outerBox.add(nextBox);
        nextBox.add(jLabel2);
        nextBox.add(Box.createHorizontalStrut(5));
        nextBox.add(m_plrNameEntry);

        outerBox.add(Box.createVerticalStrut(PADDING));

        nextBox = Box.createHorizontalBox();
        outerBox.add(nextBox);
        nextBox.add(jLabel3);
        nextBox.add(Box.createHorizontalStrut(5));
        nextBox.add(m_charNameEntry);

        outerBox.add(Box.createVerticalStrut(PADDING * 2));

        outerBox.add(m_hostPanel);

        nextBox = Box.createVerticalBox();
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.add(m_enterHostLabel);
        nextBox.add(panel);
        nextBox.add(Box.createVerticalStrut(PADDING));
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.add(m_textEntry);
        nextBox.add(panel);
        m_hostPanel.add(nextBox, "join");

        panel = new JPanel();
        m_hostPanel.add(panel, "host");

        outerBox.add(Box.createVerticalStrut(PADDING * 2));

        nextBox = Box.createHorizontalBox();
        outerBox.add(nextBox);
        nextBox.add(jLabel4);
        nextBox.add(Box.createHorizontalStrut(PADDING));
        nextBox.add(m_passwordEntry);
        nextBox.add(Box.createHorizontalStrut(PADDING * 2));
        nextBox.add(jLabel1);
        nextBox.add(Box.createHorizontalStrut(PADDING));
        nextBox.add(m_portEntry);

        outerBox.add(Box.createVerticalStrut(PADDING * 3));
        outerBox.add(Box.createVerticalGlue());

        panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        outerBox.add(panel);
        panel.add(m_ok);
        panel.add(Box.createHorizontalStrut(PADDING));
        panel.add(m_cancel);

        outerBox.add(Box.createVerticalStrut(PADDING));

        // set default values
        m_charNameEntry.setText(GametableFrame.getGametableFrame().m_characterName);
        m_plrNameEntry.setText(GametableFrame.getGametableFrame().m_playerName);
        m_textEntry.setText(GametableFrame.getGametableFrame().m_ipAddress);
        m_portEntry.setText(String.valueOf(GametableFrame.getGametableFrame().m_port));
        m_passwordEntry.setText(GametableFrame.getGametableFrame().m_password);

        // we want to know if any of those text entry areas get focus
        m_textEntry.addFocusListener(this);
        m_textEntry.setPreferredSize(new Dimension(250, m_textEntry.getPreferredSize().height));
        m_plrNameEntry.addFocusListener(this);
        m_plrNameEntry.setPreferredSize(new Dimension(150, m_plrNameEntry.getPreferredSize().height));
        m_charNameEntry.addFocusListener(this);
        m_charNameEntry.setPreferredSize(new Dimension(150, m_charNameEntry.getPreferredSize().height));
        m_portEntry.addFocusListener(this);
        m_portEntry.setPreferredSize(new Dimension(60, m_portEntry.getPreferredSize().height));
        m_passwordEntry.addFocusListener(this);
        m_passwordEntry.setPreferredSize(new Dimension(75, m_passwordEntry.getPreferredSize().height));

        setUpForJoinDlg();
    }

    public void setUpForHostDlg()
    {
        m_hostPanelLayout.show(m_hostPanel, "host");
        setTitle("Host a game");
    }

    public void setUpForJoinDlg()
    {
        m_hostPanelLayout.show(m_hostPanel, "join");
        setTitle("Join a game");
    }
}
