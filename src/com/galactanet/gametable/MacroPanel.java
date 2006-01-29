/*
 * MacroPanel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;



/**
 * Panel listing macros.
 * 
 * @author Iffy
 */
public class MacroPanel extends JPanel
{
    // --- Types -----------------------------------------------------------------------------------------------------

    /**
     * A component representing a macro.
     * 
     * @author Iffy
     */
    private class MacroEntryPanel extends JPanel
    {
        private DiceMacro macro;
        private JLabel    nameLabel;
        private JButton   rollButton;
        private JButton   editButton;
        private JButton   deleteButton;

        /**
         * This is the default constructor
         */
        public MacroEntryPanel(DiceMacro mac)
        {
            macro = mac;
            initialize();
        }

        public DiceMacro getMacro()
        {
            return macro;
        }

        /**
         * This method initializes this
         * 
         * @return void
         */
        private void initialize()
        {
            // There has to be better ways to organize all this without the constants?

            setMaximumSize(new Dimension(32768, 31));
            setMinimumSize(new Dimension(100, 31));

            SpringLayout layout = new SpringLayout();
            setLayout(layout);

            nameLabel = new JLabel(macro.getName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
            nameLabel.setToolTipText(macro.toString());

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 3));
            panel.add(nameLabel);
            add(panel);

            Image deleteImage = UtilityFunctions.getCachedImage("assets/delete.png");

            deleteButton = new JButton(new ImageIcon(deleteImage));
            deleteButton.setMargin(new Insets(0, 0, 0, 0));
            deleteButton.setFocusable(false);
            deleteButton.addActionListener(new DeleteMacroActionListener(macro));
            add(deleteButton);

            Image editImage = UtilityFunctions.getCachedImage("assets/edit.png");

            editButton = new JButton(new ImageIcon(editImage));
            editButton.setMargin(new Insets(0, 0, 0, 0));
            editButton.setFocusable(false);
            editButton.addActionListener(new EditMacroActionListener(macro));
            add(editButton);

            Image rollImage = UtilityFunctions.getCachedImage("assets/roll.png");

            rollButton = new JButton(new ImageIcon(rollImage));
            rollButton.setMargin(new Insets(0, 0, 0, 0));
            rollButton.setFocusable(false);

            rollButton.addActionListener(new MacroActionListener(macro));
            add(rollButton);

            layout.getConstraints(panel).setX(Spring.constant(2));
            layout.getConstraints(panel).setY(Spring.constant(2));
            layout.putConstraint(SpringLayout.NORTH, panel, 2, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.SOUTH, panel, -2, SpringLayout.SOUTH, this);

            layout.putConstraint(SpringLayout.EAST, rollButton, -2, SpringLayout.EAST, this);
            layout.putConstraint(SpringLayout.NORTH, rollButton, 2, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.SOUTH, rollButton, -2, SpringLayout.SOUTH, this);

            layout.putConstraint(SpringLayout.EAST, editButton, -2, SpringLayout.WEST, rollButton);
            layout.putConstraint(SpringLayout.NORTH, editButton, 2, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.SOUTH, editButton, -2, SpringLayout.SOUTH, this);

            layout.putConstraint(SpringLayout.EAST, deleteButton, -2, SpringLayout.WEST, editButton);
            layout.putConstraint(SpringLayout.NORTH, deleteButton, 2, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.SOUTH, deleteButton, -2, SpringLayout.SOUTH, this);

            setBorder(new CompoundBorder(new MatteBorder(2, 2, 2, 2, Color.WHITE), new MatteBorder(1, 1, 1, 1,
                Color.BLACK)));

            updateFromMacro();
        }

        /*
         * @see javax.swing.JComponent#getMinimumSize()
         */
        public Dimension getMinimumSize()
        {
            Dimension d = super.getMinimumSize();
            return new Dimension(d.width, 31);
        }

        /*
         * @see javax.swing.JComponent#getMaximumSize()
         */
        public Dimension getMaximumSize()
        {
            Dimension d = super.getMaximumSize();
            return new Dimension(d.width, 31);
        }

        /*
         * @see javax.swing.JComponent#getPreferredSize()
         */
        public Dimension getPreferredSize()
        {
            int minWidth = 100;
            int width = 8;
            for (int i = 0, size = getComponentCount(); i < size; ++i)
            {
                Component component = getComponent(i);
                width += component.getPreferredSize().width + 2;
            }

            return new Dimension(Math.max(minWidth, width), 31);
        }

        private void updateFromMacro()
        {
            nameLabel.setText(macro.getName());
            rollButton.setToolTipText("Roll " + macro.toString());
            editButton.setToolTipText("Edit " + macro.getName());
            deleteButton.setToolTipText("Delete " + macro.getName());
        }
    }

    /**
     * Action for macro buttons.
     * 
     * @author Iffy
     */
    private static class MacroActionListener implements ActionListener
    {
        private DiceMacro macro;

        public MacroActionListener(DiceMacro mac)
        {
            macro = mac;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            GametableFrame.getGametableFrame().postMessage(macro.doMacro());
        }
    }

    /**
     * Action for macro buttons.
     * 
     * @author Iffy
     */
    private static class DeleteMacroActionListener implements ActionListener
    {
        private DiceMacro macro;

        public DeleteMacroActionListener(DiceMacro mac)
        {
            macro = mac;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            GametableFrame.getGametableFrame().removeMacro(macro);
        }
    }

    /**
     * Action for macro buttons.
     * 
     * @author Iffy
     */
    private class EditMacroActionListener implements ActionListener
    {
        private DiceMacro macro;

        public EditMacroActionListener(DiceMacro mac)
        {
            macro = mac;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            NewMacroDialog dialog = new NewMacroDialog();
            dialog.initializeValues(macro.getName(), macro.getMacro());
            dialog.setVisible(true);
            if (dialog.isAccepted())
            {
                String name = dialog.getMacroName();
                String def = dialog.getMacroDefinition();
                DiceMacro existingMacro = GametableFrame.getGametableFrame().getMacro(name);
                if (existingMacro != null && existingMacro != macro)
                {
                    int result = UtilityFunctions.yesNoDialog(GametableFrame.getGametableFrame(),
                        "You already have a macro named \"" + name + "\", "
                            + "are you sure you want to replace it with \"" + def + "\"?", "Replace Macro?");
                    if (result == UtilityFunctions.YES)
                    {
                        GametableFrame.getGametableFrame().removeMacro(name);
                        GametableFrame.getGametableFrame().addMacro(name, def);
                    }
                    return;
                }
                GametableFrame.getGametableFrame().removeMacro(macro);
                GametableFrame.getGametableFrame().addMacro(name, def);
            }
        }
    }

    // --- Members ---------------------------------------------------------------------------------------------------

    private JScrollPane scrollPane = null;
    private JPanel      macroPanel = null;
    private JPanel      topPanel   = null;

    // --- Constructors ----------------------------------------------------------------------------------------------

    /**
     * This is the default constructor
     */
    public MacroPanel()
    {
        initialize();
        refreshMacroList();
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * Updates the macro list from the latest data.
     */
    public void refreshMacroList()
    {
        Collection macros = GametableFrame.getGametableFrame().getMacros();
        macroPanel.removeAll();

        for (Iterator iterator = macros.iterator(); iterator.hasNext();)
        {
            DiceMacro macro = (DiceMacro)iterator.next();
            MacroEntryPanel panel = new MacroEntryPanel(macro);
            macroPanel.add(panel);
        }
        macroPanel.validate();
        scrollPane.validate();
        macroPanel.repaint();
    }

    // --- Private Methods ---

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        setLayout(new BorderLayout());
        add(getTopPanel(), BorderLayout.SOUTH);
        add(getScrollPane(), BorderLayout.CENTER);
    }

    private JPanel getTopPanel()
    {
        if (topPanel == null)
        {
            topPanel = new JPanel();
            topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            JButton addButton = new JButton("Add...");
            addButton.setFocusable(false);
            addButton.addActionListener(new ActionListener()
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    GametableFrame.getGametableFrame().addDieMacro();
                }
            });
            topPanel.add(addButton);
        }
        return topPanel;
    }

    /**
     * This method initializes scrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getScrollPane()
    {
        if (scrollPane == null)
        {
            scrollPane = new JScrollPane(getMacroPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        return scrollPane;
    }

    /**
     * This method initializes macroPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMacroPanel()
    {
        if (macroPanel == null)
        {
            macroPanel = new JPanel();
            macroPanel.setLayout(new BoxLayout(macroPanel, BoxLayout.Y_AXIS));
            macroPanel.setBackground(Color.WHITE);
        }
        return macroPanel;
    }

}
