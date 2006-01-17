/*
 * ActivePogsPanel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.util.*;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;



/**
 * Tree view for active pogs and attributes.
 * 
 * @author iffy
 */
public class ActivePogsPanel extends JPanel
{
    // --- Constants -------------------------------------------------------------------------------------------------

    private static final int   POG_TEXT_PADDING = 4;
    private static final int   POG_PADDING      = 1;
    private static final int   POG_BORDER       = 0;
    private static final int   POG_MARGIN       = 0;

    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Font  FONT_NODE        = Font.decode("sansserif-12");
    private static final Font  FONT_VALUE       = FONT_NODE;
    private static final Font  FONT_KEY         = FONT_VALUE.deriveFont(Font.BOLD);

    private static final int   SPACE            = POG_PADDING + POG_BORDER + POG_MARGIN;
    private static final int   TOTAL_SPACE      = SPACE * 2;

    // --- Types -----------------------------------------------------------------------------------------------------

    /**
     * Class to track the status of branches in the pog tree.
     * 
     * @author Iffy
     */
    private class BranchTracker implements TreeExpansionListener
    {
        private Set expandedNodes  = new HashSet();
        private Set collapsedNodes = new HashSet();

        public BranchTracker()
        {
        }

        public void reset()
        {
            expandedNodes.clear();
            collapsedNodes.clear();
        }

        public void restoreTree(JTree tree)
        {
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            RootNode root = (RootNode)model.getRoot();

            tree.removeTreeExpansionListener(this);
            try
            {
                Iterator iterator = new HashSet(expandedNodes).iterator();
                while (iterator.hasNext())
                {
                    Pog pog = (Pog)iterator.next();
                    PogNode node = root.findNodeFor(pog);
                    if (node != null)
                    {
                        TreePath path = new TreePath(model.getPathToRoot(node));
                        tree.expandPath(path);
                    }
                    else
                    {
                        expandedNodes.remove(pog);
                    }
                }

                iterator = new HashSet(collapsedNodes).iterator();
                while (iterator.hasNext())
                {
                    Pog pog = (Pog)iterator.next();
                    PogNode node = root.findNodeFor(pog);
                    if (node != null)
                    {
                        TreePath path = new TreePath(model.getPathToRoot(node));
                        tree.collapseRow(tree.getRowForPath(path));
                    }
                    else
                    {
                        collapsedNodes.remove(pog);
                    }
                }
            }
            finally
            {
                tree.addTreeExpansionListener(this);
            }
        }

        // --- TreeExpansionListener Implementation ---

        /*
         * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
         */
        public void treeExpanded(TreeExpansionEvent event)
        {
            PogNode node = (PogNode)event.getPath().getLastPathComponent();
            expandedNodes.add(node.getPog());
            collapsedNodes.remove(node.getPog());
        }

        /*
         * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
         */
        public void treeCollapsed(TreeExpansionEvent event)
        {
            PogNode node = (PogNode)event.getPath().getLastPathComponent();
            expandedNodes.remove(node.getPog());
            collapsedNodes.add(node.getPog());
        }
    }

    /**
     * Root node class for the tree.
     * 
     * @author iffy
     */
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

        public PogNode findNodeFor(Pog pog)
        {
            for (int i = 0, size = getChildCount(); i < size; ++i)
            {
                PogNode node = (PogNode)getChildAt(i);
                if (pog.equals(node.getPog()))
                {
                    return node;
                }
            }

            return null;
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

    private JTree pogTree;
    private Map   trackers = new HashMap();

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
        removeTrackers();
        GametableMap map = GametableFrame.getGametableFrame().getGametableCanvas().getActiveMap();
        pogTree.setModel(new DefaultTreeModel(new RootNode(map)));
        BranchTracker tracker = getTrackerFor(map);
        pogTree.addTreeExpansionListener(tracker);
        tracker.restoreTree(pogTree);
    }

    // --- Accessor methods ---

    private void removeTrackers()
    {
        for (Iterator iterator = trackers.values().iterator(); iterator.hasNext();)
        {
            pogTree.removeTreeExpansionListener((TreeExpansionListener)iterator.next());
        }
    }

    private BranchTracker getTrackerFor(GametableMap map)
    {
        BranchTracker tracker = (BranchTracker)trackers.get(map);
        if (tracker == null)
        {
            tracker = new BranchTracker();
            trackers.put(map, tracker);
        }

        return tracker;
    }

    // --- Initialization methods ---

    private JScrollPane getScrollPane()
    {
        JScrollPane scrollPane = new JScrollPane(getPogTree());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    private JTree getPogTree()
    {
        if (pogTree == null)
        {
            pogTree = new JTree();
            pogTree.setBackground(BACKGROUND_COLOR);
            pogTree.setRootVisible(false);
            pogTree.setShowsRootHandles(true);
            pogTree.setToggleClickCount(3);
            pogTree.setSelectionModel(null);
            pogTree.setCellRenderer(new ActivePogTreeCellRenderer());
            pogTree.setRowHeight(0);
            pogTree.setFocusable(false);
            refresh();
        }
        return pogTree;
    }
}
