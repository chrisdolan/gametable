/*
 * MacroPanel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

import com.galactanet.gametable.DiceMacro;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.NewMacroDialog;
import com.galactanet.gametable.util.UtilityFunctions;



/**
 * Panel listing macros.
 * 
 * @author Iffy
 */
public class MacroPanel extends JPanel
{
    // --- Types -----------------------------------------------------------------------------------------------------

    /**
     * Action for macro buttons.
     * 
     * @author Iffy
     */
    private static class DeleteMacroActionListener implements ActionListener
    {
        private final DiceMacro macro;

        public DeleteMacroActionListener(final DiceMacro mac)
        {
            macro = mac;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e)
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
        private final DiceMacro macro;

        public EditMacroActionListener(final DiceMacro mac)
        {
            macro = mac;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e)
        {
            final NewMacroDialog dialog = new NewMacroDialog();
            dialog.initializeValues(macro.getName(), macro.getMacro());
            dialog.setVisible(true);
            if (dialog.isAccepted())
            {
                final String name = dialog.getMacroName();
                final String def = dialog.getMacroDefinition();
                final DiceMacro existingMacro = GametableFrame.getGametableFrame().getMacro(name);
                if ((existingMacro != null) && (existingMacro != macro))
                {
                    final int result = UtilityFunctions.yesNoDialog(GametableFrame.getGametableFrame(),
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

    /**
     * Action for macro buttons.
     * 
     * @author Iffy
     */
    private static class MacroActionListener implements ActionListener
    {
        private final DiceMacro macro;

        public MacroActionListener(final DiceMacro mac)
        {
            macro = mac;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e)
        {
            if (privateMacroRoll == 2) //private roll
            {
                GametableFrame.getGametableFrame().parseSlashCommand("/proll " + macro.getMacro());
            }
            else if (privateMacroRoll == 1) //semiprivate roll
            {
                GametableFrame.getGametableFrame().postMessage(
                    GametableFrame.DIEROLL_MESSAGE_FONT
                        + UtilityFunctions.emitUserLink(GametableFrame.getGametableFrame().getMyPlayer()
                            .getCharacterName()) + " is rolling dice..." + GametableFrame.END_DIEROLL_MESSAGE_FONT);
                GametableFrame.getGametableFrame().parseSlashCommand("/proll " + macro.getMacro());
            }
            else //public roll
            {
                GametableFrame.getGametableFrame().postMessage(macro.doMacro());
            }
        }
    }

    /**
     * A component representing a macro.
     * 
     * @author Iffy
     */
    private class MacroEntryPanel extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6134370797200018514L;
        private JButton           deleteButton;
        private JButton           editButton;
        private final DiceMacro   macro;
        private JLabel            nameLabel;
        private JButton           rollButton;

        /**
         * This is the default constructor
         */
        public MacroEntryPanel(final DiceMacro mac)
        {
            macro = mac;
            initialize();
        }

        public DiceMacro getMacro()
        {
            return macro;
        }

        /*
         * @see javax.swing.JComponent#getMaximumSize()
         */
        public Dimension getMaximumSize()
        {
            final Dimension d = super.getMaximumSize();
            return new Dimension(d.width, 31);
        }

        /*
         * @see javax.swing.JComponent#getMinimumSize()
         */
        public Dimension getMinimumSize()
        {
            final Dimension d = super.getMinimumSize();
            return new Dimension(d.width, 31);
        }

        /*
         * @see javax.swing.JComponent#getPreferredSize()
         */
        public Dimension getPreferredSize()
        {
            final int minWidth = 100;
            int width = 8;
            for (int i = 0, size = getComponentCount(); i < size; ++i)
            {
                final Component component = getComponent(i);
                width += component.getPreferredSize().width + 2;
            }

            return new Dimension(Math.max(minWidth, width), 31);
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

            final SpringLayout layout = new SpringLayout();
            setLayout(layout);

            nameLabel = new JLabel(macro.getName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
            nameLabel.setToolTipText(macro.toString());

            final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 3));
            panel.add(nameLabel);
            add(panel);

            final Image deleteImage = UtilityFunctions.getCachedImage("assets/delete.png");

            deleteButton = new JButton(new ImageIcon(deleteImage));
            deleteButton.setMargin(new Insets(0, 0, 0, 0));
            deleteButton.setFocusable(false);
            deleteButton.addActionListener(new DeleteMacroActionListener(macro));
            add(deleteButton);

            final Image editImage = UtilityFunctions.getCachedImage("assets/edit.png");

            editButton = new JButton(new ImageIcon(editImage));
            editButton.setMargin(new Insets(0, 0, 0, 0));
            editButton.setFocusable(false);
            editButton.addActionListener(new EditMacroActionListener(macro));
            add(editButton);

            final Image rollImage = UtilityFunctions.getCachedImage("assets/roll.png");

            rollButton = new JButton(new ImageIcon(rollImage));
            rollButton.setMargin(new Insets(0, 0, 0, 0));
            rollButton.setFocusable(false);

            rollButton.addActionListener(new MacroActionListener(macro));
            add(rollButton);

            layout.getConstraints(panel).setX(Spring.constant(2));
            layout.getConstraints(panel).setY(Spring.constant(2));
            layout.putConstraint(SpringLayout.NORTH, panel, 2, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.SOUTH, panel, -2, SpringLayout.SOUTH, this);
            layout.putConstraint(SpringLayout.WEST, panel, 2, SpringLayout.EAST, rollButton);

            layout.putConstraint(SpringLayout.WEST, rollButton, 2, SpringLayout.EAST, editButton);
            layout.putConstraint(SpringLayout.NORTH, rollButton, 2, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.SOUTH, rollButton, -2, SpringLayout.SOUTH, this);

            layout.putConstraint(SpringLayout.WEST, editButton, 2, SpringLayout.EAST, deleteButton);
            layout.putConstraint(SpringLayout.NORTH, editButton, 2, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.SOUTH, editButton, -2, SpringLayout.SOUTH, this);

            layout.putConstraint(SpringLayout.WEST, deleteButton, 2, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, deleteButton, 2, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.SOUTH, deleteButton, -2, SpringLayout.SOUTH, this);

            setBorder(new CompoundBorder(new MatteBorder(2, 2, 2, 2, Color.WHITE), new MatteBorder(1, 1, 1, 1,
                Color.BLACK)));

            updateFromMacro();
        }

        private void updateFromMacro()
        {
            nameLabel.setText(macro.getName());
            rollButton.setToolTipText("Roll " + macro.toString());
            editButton.setToolTipText("Edit " + macro.getName());
            deleteButton.setToolTipText("Delete " + macro.getName());
        }
    }

    public static int    privateMacroRoll = 0;

    // --- Members ---------------------------------------------------------------------------------------------------

    /**
     * 
     */
    private static final long serialVersionUID = -8000107664792911955L;
    private JPanel            macroPanel       = null;
    private JScrollPane       scrollPane       = null;
    //private JCheckBox         semiPrivateBox   = null;
    private JRadioButton      publicRoll       = null;
    private JRadioButton      semiprivateRoll  = null;
    private JRadioButton      privateRoll      = null;
    private JSplitPane        topPanel         = null;
    private JPanel            privateRolls     = null;
    private JPanel            addMacro         = null;

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

    // --- Private Methods ---

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
            scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        }
        return scrollPane;
    }

    private JSplitPane getTopPanel()
    {
        if (topPanel == null)
        {
            topPanel = new JSplitPane();
            //topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            topPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            topPanel.setContinuousLayout(true);
            topPanel.setBorder(null);

            if (privateRolls == null)
            {
                privateRolls = new JPanel();
                privateRolls.setLayout(new FlowLayout(FlowLayout.LEFT));
                privateRolls.setLayout(new GridLayout(0, 1));
                
                ButtonGroup group = new ButtonGroup();
                
                publicRoll = new JRadioButton("Public rolls", true);
                semiprivateRoll = new JRadioButton("Semiprivate rolls");
                privateRoll = new JRadioButton("Private rolls");
                
                publicRoll.setFocusable(false);
                semiprivateRoll.setFocusable(false);
                privateRoll.setFocusable(false);

                publicRoll.addItemListener(new SelectItemListener());
                semiprivateRoll.addItemListener(new SelectItemListener());
                privateRoll.addItemListener(new SelectItemListener());

                group.add(publicRoll);
                group.add(semiprivateRoll);
                group.add(privateRoll);

                privateRolls.add(publicRoll);
                privateRolls.add(semiprivateRoll);
                privateRolls.add(privateRoll);
            }
            if (addMacro == null)
            {
                addMacro = new JPanel();
                addMacro.setLayout(new FlowLayout(FlowLayout.RIGHT));
                
                final JButton addButton = new JButton("Add...");
                addButton.setFocusable(false);
                addButton.addActionListener(new ActionListener()
                {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    public void actionPerformed(final ActionEvent e)
                    {
                        GametableFrame.getGametableFrame().addDieMacro();
                    }
                });
                addMacro.add(addButton);
            }
        }
        topPanel.add(privateRolls, JSplitPane.LEFT);
        topPanel.add(addMacro, JSplitPane.RIGHT);

        return topPanel;
    }

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

    /**
     * Updates the macro list from the latest data.
     */
    public void refreshMacroList()
    {
        final Collection macros = GametableFrame.getGametableFrame().getMacros();
        macroPanel.removeAll();

        for (final Iterator iterator = macros.iterator(); iterator.hasNext();)
        {
            final DiceMacro macro = (DiceMacro)iterator.next();
            final MacroEntryPanel panel = new MacroEntryPanel(macro);
            macroPanel.add(panel);
        }
        macroPanel.validate();
        scrollPane.validate();
        macroPanel.repaint();
    }

}
class SelectItemListener implements ItemListener{
    public void itemStateChanged(ItemEvent e){
        //get object
        AbstractButton sel = (AbstractButton)e.getItemSelectable();
        //checkbox select or not
        if(e.getStateChange() == ItemEvent.SELECTED){
            if (sel.getText().equals("Semiprivate rolls"))
            {
                MacroPanel.privateMacroRoll = 1;
            }
            else if (sel.getText().equals("Private rolls"))
            {
                MacroPanel.privateMacroRoll = 2;
            }
            else
            {
                MacroPanel.privateMacroRoll = 0;
            }
        }
    }
}

