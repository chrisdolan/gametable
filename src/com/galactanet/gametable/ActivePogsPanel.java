/*
 * ActivePogsPanel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;



/**
 * Tree view for active pogs and attributes.
 * 
 * @author iffy
 */
public class ActivePogsPanel extends JPanel
{
    // --- Constants -------------------------------------------------------------------------------------------------

    private static final int           POG_TEXT_PADDING         = 4;
    private static final int           POG_PADDING              = 1;
    private static final int           POG_BORDER               = 0;
    private static final int           POG_MARGIN               = 0;

    private static final Color         BACKGROUND_COLOR         = Color.WHITE;
    private static final Font          FONT_NODE                = Font.decode("sansserif-12");
    private static final Font          FONT_VALUE               = FONT_NODE;
    private static final Font          FONT_KEY                 = FONT_VALUE.deriveFont(Font.BOLD);

    private static final int           SPACE                    = POG_PADDING + POG_BORDER + POG_MARGIN;
    private static final int           TOTAL_SPACE              = SPACE * 2;

    // --- Types -----------------------------------------------------------------------------------------------------

    private static class RootNode extends DefaultMutableTreeNode
    {
        public RootNode(GametableMap map)
        {
            super(map, true);
            for (Iterator iterator = getMap().getOrderedPogs().iterator(); iterator.hasNext();)
            {
                add(new PogNode((Pog)iterator.next()));
            }
        }

        public GametableMap getMap()
        {
            return (GametableMap)getUserObject();
        }
    }

    /**
     * A TreeNode representing a library.
     * 
     * @author Iffy
     */
    private static class PogNode extends DefaultMutableTreeNode
    {
        public PogNode(Pog pog)
        {
            super(pog, true);
            for (Iterator iterator = getPog().getAttributeNames().iterator(); iterator.hasNext();)
            {
                add(new AttributeNode((String)iterator.next()));
            }
        }

        /**
         * @return Returns the pog for this node.
         */
        public Pog getPog()
        {
            return (Pog)getUserObject();
        }

        // --- Object Implementation ---

        /*
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return getPog().hashCode();
        }

        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }

            PogNode node = (PogNode)o;
            return (node.getPog().equals(getPog()));
        }
    }

    /**
     * A Leaf TreeNode representing a Pog's attribute.
     * 
     * @author Iffy
     */
    private static class AttributeNode extends DefaultMutableTreeNode
    {
        public AttributeNode(String att)
        {
            super(att, false);
        }

        /**
         * @return Returns the pog for this node.
         */
        public Pog getPog()
        {
            return getPogNodeParent().getPog();
        }

        /**
         * @return Returns the attribute for this node.
         */
        public String getAttribute()
        {
            return (String)getUserObject();
        }

        // --- Object Implementation ---

        /*
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return getPog().hashCode() ^ getAttribute().hashCode();
        }

        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }

            AttributeNode node = (AttributeNode)o;
            return (node.getPog().equals(getPog()) && node.getAttribute().equals(getAttribute()));
        }

        // --- Private Methods ---

        private PogNode getPogNodeParent()
        {
            return (PogNode)getParent();
        }
    }

    /**
     * Cell renderer for the tree.
     * 
     * @author Iffy
     */
    private static class ActivePogTreeCellRenderer extends JComponent implements TreeCellRenderer
    {
        Pog     pog       = null;
        String  attribute = null;
        boolean expanded  = false;
        boolean leaf      = false;

        public ActivePogTreeCellRenderer()
        {
        }

        /*
         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object,
         *      boolean, boolean, boolean, int, boolean)
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean lf,
            int r, boolean focus)
        {
            pog = null;
            attribute = null;
            if (value instanceof PogNode)
            {
                PogNode node = (PogNode)value;
                pog = node.getPog();
            }
            else if (value instanceof AttributeNode)
            {
                AttributeNode node = (AttributeNode)value;
                pog = node.getPog();
                attribute = node.getAttribute();
            }
            else
            {
                return this;
            }
            expanded = exp;
            leaf = lf;

            Dimension size = getMySize();
            setSize(size);
            setPreferredSize(size);

            return this;
        }

        private String getValue()
        {
            String value = pog.getAttribute(attribute);
            if (value == null)
            {
                return "";
            }

            return value;
        }

        private String getLabel()
        {
            if (attribute != null)
            {
                return attribute + ": ";
            }
            return pog.getText();
        }

        /**
         * @return The computed dimensions for this PogComponent, based on the pog and label.
         */
        private Dimension getMySize()
        {
            if (pog == null)
            {
                return new Dimension(1, 1);
            }

            if (attribute == null)
            {
                int w = PogPanel.POG_ICON_SIZE;
                int h = PogPanel.POG_ICON_SIZE;
                String label = getLabel();
                if (label != null && label.length() > 0)
                {
                    FontRenderContext frc = new FontRenderContext(null, true, false);
                    Rectangle stringBounds = FONT_NODE.getStringBounds(label, frc).getBounds();
                    w += stringBounds.width + POG_TEXT_PADDING;
                    if (stringBounds.height > h)
                    {
                        h = stringBounds.height;
                    }
                }

                return new Dimension(w + TOTAL_SPACE, h + TOTAL_SPACE);
            }

            int w = 0;
            int h = 0;

            FontRenderContext frc = new FontRenderContext(null, true, false);
            Rectangle keyBounds = FONT_KEY.getStringBounds(getLabel(), frc).getBounds();
            Rectangle valueBounds = FONT_VALUE.getStringBounds(getValue(), frc).getBounds();
            h = Math.max(keyBounds.height, valueBounds.height);
            w = keyBounds.width + valueBounds.width;

            return new Dimension(w + TOTAL_SPACE, h + TOTAL_SPACE);
        }

        /*
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        protected void paintComponent(Graphics g)
        {
            if (pog == null)
            {
                return;
            }

            PogType pogType = pog.getPogType();
            Graphics2D g2 = (Graphics2D)g;
            g2.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
            g2.setColor(Color.BLACK);
            if (attribute == null)
            {
                pogType.drawListIcon(g2, SPACE + (PogPanel.POG_ICON_SIZE - pogType.getListIconWidth()) / 2, SPACE
                    + (PogPanel.POG_ICON_SIZE - pogType.getListIconHeight()) / 2);

                String label = getLabel();
                if (label != null && label.length() > 0)
                {
                    g2.setFont(FONT_NODE);
                    FontMetrics fm = g2.getFontMetrics();
                    Rectangle stringBounds = fm.getStringBounds(label, g2).getBounds();
                    int drawX = SPACE + PogPanel.POG_ICON_SIZE + POG_TEXT_PADDING;
                    int drawY = SPACE + (PogPanel.POG_ICON_SIZE - stringBounds.height) / 2 - stringBounds.y;
                    g2.drawString(label, drawX, drawY);
                }
            }
            else
            {
                String label = getLabel();
                String value = getValue();
                Rectangle keyBounds = g2.getFontMetrics(FONT_KEY).getStringBounds(label, g2).getBounds();
                Rectangle valueBounds = g2.getFontMetrics(FONT_VALUE).getStringBounds(value, g2).getBounds();
                int drawX = SPACE;
                int drawY = SPACE + Math.max(Math.abs(keyBounds.y), Math.abs(valueBounds.y));
                g2.setFont(FONT_KEY);
                g2.drawString(label, drawX, drawY);
                g2.setFont(FONT_VALUE);
                g2.drawString(value, drawX + keyBounds.width, drawY);
            }
            g2.dispose();
        }
    }

    // --- Members ---------------------------------------------------------------------------------------------------

    private JTree tree;

    // --- Constructors ----------------------------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ActivePogsPanel()
    {
        super(new BorderLayout());
        add(getScrollPane(), BorderLayout.CENTER);
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    public void refresh()
    {
        tree.setModel(new DefaultTreeModel(new RootNode(GametableFrame.getGametableFrame().getGametableCanvas()
            .getActiveMap())));
    }

    // --- Initialization methods ---

    private JScrollPane getScrollPane()
    {
        JScrollPane scrollPane = new JScrollPane(getTree());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    private JTree getTree()
    {
        if (tree == null)
        {
            tree = new JTree();
            tree.setBackground(BACKGROUND_COLOR);
            tree.setRootVisible(false);
            tree.setShowsRootHandles(true);
            tree.setToggleClickCount(3);
            tree.setSelectionModel(null);
            tree.setCellRenderer(new ActivePogTreeCellRenderer());
            tree.setRowHeight(0);
            // TODO: tree.addTreeExpansionListener(null);
            tree.setFocusable(false);
            refresh();
        }
        return tree;
    }
}
