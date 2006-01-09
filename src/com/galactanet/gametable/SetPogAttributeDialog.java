/*
 * SetPogAttributeDialog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;
import javax.swing.JPanel;



/**
 * TODO: comment
 * 
 * @author iffy
 */
public class SetPogAttributeDialog extends JDialog
{

    private JPanel     contentPanel          = null;
    private JPanel     bottomPanel           = null;
    private JButton    okButton              = null;
    private JButton    cancelButton          = null;
    private JPanel     centerPanel           = null;
    private JTextField nameTextField         = null;
    private JTextField valueTextField        = null;
    private JLabel     nameLabel             = null;
    private JLabel     valueLabel            = null;
    private JPanel     horizontalSpacerPanel = null;

    private String     name                  = null;
    private String     value                 = null;

    /**
     * This is the default constructor
     */
    public SetPogAttributeDialog()
    {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);
        this.setResizable(false);
        this.setTitle("Set Pog Attribute");
        this.setContentPane(getContentPanel());
        this.pack();
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getContentPanel()
    {
        if (contentPanel == null)
        {
            contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(getBottomPanel(), java.awt.BorderLayout.SOUTH);
            contentPanel.add(getCenterPanel(), java.awt.BorderLayout.CENTER);
        }
        return contentPanel;
    }

    /**
     * This method initializes bottomPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getBottomPanel()
    {
        if (bottomPanel == null)
        {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            bottomPanel = new JPanel();
            bottomPanel.setLayout(flowLayout);
            bottomPanel.add(getOkButton(), null);
            bottomPanel.add(getCancelButton(), null);
        }
        return bottomPanel;
    }

    /**
     * This method initializes okButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getOkButton()
    {
        if (okButton == null)
        {
            okButton = new JButton();
            okButton.setText("Ok");
            okButton.setSelected(true);
            okButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    name = nameTextField.getText();
                    value = valueTextField.getText();
                    dispose();
                }
            });
        }
        return okButton;
    }

    /**
     * This method initializes cancelButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton()
    {
        if (cancelButton == null)
        {
            cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    dispose();
                }
            });
        }
        return cancelButton;
    }

    /**
     * This method initializes centerPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPanel()
    {
        if (centerPanel == null)
        {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 1;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.gridy = 3;
            valueLabel = new JLabel();
            valueLabel.setText("Value: ");
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.gridy = 0;
            nameLabel = new JLabel();
            nameLabel.setText("Name: ");
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints21.gridy = 3;
            gridBagConstraints21.weightx = 1.0;
            gridBagConstraints21.gridx = 2;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.gridx = 2;
            centerPanel = new JPanel();
            centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 0, 5));
            centerPanel.setLayout(new GridBagLayout());
            centerPanel.add(getNameTextField(), gridBagConstraints1);
            centerPanel.add(getValueTextField(), gridBagConstraints21);
            centerPanel.add(nameLabel, gridBagConstraints3);
            centerPanel.add(valueLabel, gridBagConstraints4);
            centerPanel.add(getHorizontalSpacerPanel(), gridBagConstraints);
        }
        return centerPanel;
    }

    /**
     * This method initializes nameTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getNameTextField()
    {
        if (nameTextField == null)
        {
            nameTextField = new JTextField();
            nameTextField.setPreferredSize(new java.awt.Dimension(150, 20));
        }
        return nameTextField;
    }

    /**
     * This method initializes valueTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getValueTextField()
    {
        if (valueTextField == null)
        {
            valueTextField = new JTextField();
            valueTextField.setPreferredSize(new java.awt.Dimension(150, 20));
        }
        return valueTextField;
    }

    /**
     * This method initializes horizontalSpacerPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getHorizontalSpacerPanel()
    {
        if (horizontalSpacerPanel == null)
        {
            horizontalSpacerPanel = new JPanel();
            horizontalSpacerPanel.setPreferredSize(new java.awt.Dimension(5, 5));
        }
        return horizontalSpacerPanel;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Returns the value.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Loads the dialog with some existing values.
     * 
     * @param newName
     * @param newValue
     */
    public void loadValues(String newName, String newValue)
    {
        getNameTextField().setText(newName);
        getValueTextField().setText(newValue);
    }
    
} // @jve:decl-index=0:visual-constraint="10,10"
