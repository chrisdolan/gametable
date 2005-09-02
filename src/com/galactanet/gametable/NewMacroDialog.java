

package com.galactanet.gametable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;


public class NewMacroDialog extends JDialog implements FocusListener
{
    JButton    m_ok        = new JButton();
    JButton    m_cancel    = new JButton();

    boolean    m_bAccepted;
    JTextField m_nameEntry = new JTextField();
    JTextField m_rollEntry = new JTextField();
    JLabel     jLabel2     = new JLabel();
    JLabel     jLabel3     = new JLabel();
    JLabel     jLabel1     = new JLabel();
    JLabel     jLabel4     = new JLabel();



    public NewMacroDialog()
    {
        try
        {
            jbInit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

    private void jbInit() throws Exception
    {
        setTitle("Add a Dice Macro");
        setResizable(false);
        m_rollEntry.setText("d20");
        m_nameEntry.setText("");
        m_ok.setActionCommand("OK");
        m_ok.setText("OK");
        m_ok.addActionListener(new NewMacroDialog_m_ok_actionAdapter(this));

        m_cancel.setActionCommand("cancel");
        m_cancel.setText("Cancel");
        m_cancel.addActionListener(new NewMacroDialog_m_cancel_actionAdapter(this));

        jLabel2.setText("Macro Name:");
        jLabel3.setText("Die Roll:");
        jLabel1.setText("Enter a name, and a die roll in standard");
        jLabel4.setText("notation. (Ex: 3d6 + d8 + 4)");

        final int PADDING = 5;

        Box outmostBox = Box.createHorizontalBox();
        add(outmostBox, BorderLayout.CENTER);
        outmostBox.add(Box.createHorizontalStrut(PADDING));
        Box outerBox = Box.createVerticalBox();
        outmostBox.add(outerBox);
        outmostBox.add(Box.createHorizontalStrut(PADDING));

        outerBox.add(Box.createVerticalStrut(PADDING));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(jLabel2);
        outerBox.add(panel);
        outerBox.add(Box.createVerticalStrut(PADDING));
        outerBox.add(m_nameEntry);

        outerBox.add(Box.createVerticalStrut(PADDING * 2));

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        Box nextBox = Box.createVerticalBox();
        nextBox.add(jLabel3);
        nextBox.add(jLabel1);
        nextBox.add(jLabel4);
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

    }

    public void m_ok_actionPerformed(ActionEvent e)
    {
        if (m_rollEntry.getText().length() > 0)
        {
            m_bAccepted = true;
        }
        dispose();
    }

    void m_cancel_actionPerformed(ActionEvent e)
    {
        dispose();
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

}


class NewMacroDialog_m_ok_actionAdapter implements java.awt.event.ActionListener
{
    NewMacroDialog adaptee;



    NewMacroDialog_m_ok_actionAdapter(NewMacroDialog a)
    {
        this.adaptee = a;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.m_ok_actionPerformed(e);
    }
}


class NewMacroDialog_m_cancel_actionAdapter implements java.awt.event.ActionListener
{
    NewMacroDialog adaptee;



    NewMacroDialog_m_cancel_actionAdapter(NewMacroDialog a)
    {
        this.adaptee = a;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.m_cancel_actionPerformed(e);
    }
}
