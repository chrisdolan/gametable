/*
 * ActivePogsPanel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.util.*;
import java.util.List;

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

    private static final float CLICK_THRESHHOLD = 2f;
    
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

    /**
     * The main component for this damn thing.
     */
    private JTree                     pogTree;

    /**
     * A map of GametableMaps to BranchTrackers for thier pog lists.
     */
    private Map                       trackers            = new HashMap();

    // --- Pog Dragging Members ---

    private ActivePogTreeCellRenderer pogRenderer         = new ActivePogTreeCellRenderer();

    /**
     * The currently grabbed pog.
     */
    private PogNode                   m_grabbedNode       = null;

    /**
     * The position of the currently grabbed pog.
     */
    private Point                     m_grabPosition      = null;

    /**
     * The offset at which the pog was grabbed.
     */
    private Point                     m_grabOffset        = null;

    private int                       m_numClicks         = 0;
    private Point                     m_lastPressPosition = null;

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

    // --- Dragging methods ---

    private void grabPog(PogNode p, Point pos, Point offset)
    {
        m_grabbedNode = p;
        m_grabOffset = offset;
        m_grabPosition = pos;
    }

    private void releasePog()
    {
        if (m_grabbedNode != null)
        {
            Point localPos = UtilityFunctions.getComponentCoordinates(this, m_grabPosition);
            PogNode node = getClosestPogNode(localPos.x, localPos.y);
            if (node != null)
            {
                boolean after = false;
                int row = getRowForNode(node);
                if (row > -1)
                {
                    Rectangle bounds = pogTree.getRowBounds(row);
                    if (localPos.y > bounds.y + bounds.height)
                    {
                        after = true;
                    }
                }

                Pog sourcePog = m_grabbedNode.getPog();
                Pog targetPog = node.getPog();
                if (!sourcePog.equals(targetPog))
                {
                    List pogs = new ArrayList(GametableFrame.getGametableFrame().getGametableCanvas().getActiveMap()
                        .getOrderedPogs());
                    int sourceIndex = pogs.indexOf(sourcePog);
                    int targetIndex = pogs.indexOf(targetPog);
                    Map changes = new HashMap();
                    if (sourceIndex < targetIndex)
                    {
                        // Moving a pog down in the list
                        if (!after)
                        {
                            --targetIndex;
                            targetPog = (Pog)pogs.get(targetIndex);
                        }
                        changes.put(new Integer(sourcePog.getId()), new Long(targetPog.getSortOrder()));
                        for (int i = sourceIndex + 1; i <= targetIndex; ++i)
                        {
                            Pog a = (Pog)pogs.get(i);
                            Pog b = (Pog)pogs.get(i - 1);

                            changes.put(new Integer(a.getId()), new Long(b.getSortOrder()));
                        }
                    }
                    else
                    {
                        // Moving a pog up in the list
                        changes.put(new Integer(sourcePog.getId()), new Long(targetPog.getSortOrder()));
                        for (int i = targetIndex; i < sourceIndex; ++i)
                        {
                            Pog a = (Pog)pogs.get(i);
                            Pog b = (Pog)pogs.get(i + 1);

                            changes.put(new Integer(a.getId()), new Long(b.getSortOrder()));
                        }
                    }
                    GametableFrame.getGametableFrame().getGametableCanvas().reorderPogs(changes);
                }
            }

            m_grabbedNode = null;
            m_grabPosition = null;
            m_grabOffset = null;
            repaint();
        }
    }

    private void moveGrabPosition(Point pos)
    {
        if (m_grabbedNode != null)
        {
            m_grabPosition = pos;
            repaint();
        }
    }

    private PogNode getPogNode(int x, int y)
    {
        TreePath path = pogTree.getPathForLocation(x, y);
        if (path == null)
        {
            return null;
        }

        for (int i = path.getPathCount(); i-- > 0;)
        {
            Object val = path.getPathComponent(i);
            if (val instanceof PogNode)
            {
                return (PogNode)val;
            }
        }

        return null;
    }

    private PogNode getClosestPogNode(int x, int y)
    {
        TreePath path = pogTree.getClosestPathForLocation(x, y);
        if (path == null)
        {
            return null;
        }

        for (int i = path.getPathCount(); i-- > 0;)
        {
            Object val = path.getPathComponent(i);
            if (val instanceof PogNode)
            {
                return (PogNode)val;
            }
        }

        return null;
    }

    private int getRowForNode(PogNode node)
    {
        DefaultTreeModel model = (DefaultTreeModel)pogTree.getModel();
        TreePath path = new TreePath(model.getPathToRoot(node));
        return pogTree.getRowForPath(path);
    }

    // --- Component Implementation ---

    /*
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        if (m_grabbedNode != null)
        {
            Graphics2D g2 = (Graphics2D)g;
            Point localPos = UtilityFunctions.getComponentCoordinates(this, m_grabPosition);

            PogNode node = getClosestPogNode(localPos.x, localPos.y);
            if (node != null)
            {
                int row = getRowForNode(node);
                if (row > -1)
                {
                    Rectangle bounds = pogTree.getRowBounds(row);
                    int drawY = bounds.y + 2;
                    if (localPos.y > bounds.y + bounds.height)
                    {
                        drawY += bounds.height;
                    }
                    final int PADDING = 5;
                    int drawX = PADDING;
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawLine(drawX, drawY, drawX + pogTree.getWidth() - (PADDING * 2), drawY);
                }
            }

            g2.translate(localPos.x - m_grabOffset.x, localPos.y - m_grabOffset.y);
            JComponent comp = (JComponent)pogRenderer.getTreeCellRendererComponent(pogTree, m_grabbedNode, false,
                false, true, 0, false);
            comp.paint(g2);
            g2.dispose();
        }
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
            pogTree.setCellRenderer(pogRenderer);
            pogTree.setRowHeight(0);
            pogTree.setFocusable(false);
            pogTree.addMouseListener(new MouseAdapter()
            {
                /*
                 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
                 */
                public void mousePressed(MouseEvent e)
                {
                    m_lastPressPosition = new Point(e.getX(), e.getY());
                    TreePath path = pogTree.getPathForLocation(e.getX(), e.getY());
                    if (path == null)
                    {
                        return;
                    }

                    Object val = path.getLastPathComponent();
                    if (val instanceof PogNode)
                    {
                        PogNode node = (PogNode)val;
                        Point screenCoords = UtilityFunctions.getScreenCoordinates(pogTree, m_lastPressPosition);
                        Point localCoords = new Point(node.getPog().getPogType().getListIconWidth() / 2, node.getPog()
                            .getPogType().getListIconHeight() / 2);
                        grabPog(node, screenCoords, localCoords);
                    }
                }

                /*
                 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseReleased(MouseEvent e)
                {
                    releasePog();
                    Point p = new Point(e.getX(), e.getY());
                    
                    if (p.distance(m_lastPressPosition) <= CLICK_THRESHHOLD)
                    {
                        m_numClicks++;
                    }

                    if (m_numClicks == 2)
                    {
                        PogNode node = getPogNode(e.getX(), e.getY());
                        if (node != null)
                        {
                            GametableFrame.getGametableFrame().getGametableCanvas().scrollToPog(node.getPog());
                        }
                    }
                }

                /*
                 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseExited(MouseEvent e)
                {
                }

            });

            pogTree.addMouseMotionListener(new MouseMotionAdapter()
            {
                /*
                 * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
                 */
                public void mouseDragged(MouseEvent e)
                {
                    mouseMoved(e);
                }

                /*
                 * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
                 */
                public void mouseMoved(MouseEvent e)
                {
                    Point screenCoords = UtilityFunctions.getScreenCoordinates(pogTree, new Point(e.getX(), e.getY()));
                    moveGrabPosition(screenCoords);
                    m_numClicks = 0;
                }
            });
            refresh();
        }
        return pogTree;
    }
}
