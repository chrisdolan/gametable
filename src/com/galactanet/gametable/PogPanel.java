/*
 * PogPanel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;



/**
 * Tree-based pog library panel.
 * 
 * @author iffy
 */
public class PogPanel extends JPanel
{
    // --- Constants -------------------------------------------------------------------------------------------------

    /**
     * Class to track the status of branches in the pog tree.
     * 
     * @author Iffy
     */
    private class BranchTracker implements TreeExpansionListener
    {
        private boolean   allExpanded    = false;
        private final Set collapsedNodes = new HashSet();
        private final Set expandedNodes  = new HashSet();

        public BranchTracker()
        {
        }

        public void collapseAll(final JTree tree)
        {
            final DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            collapseAll(tree, (TreeNode)model.getRoot());
            allExpanded = false;
        }

        private void collapseAll(final JTree tree, final TreeNode node)
        {
            if (node.isLeaf() || !node.getAllowsChildren() || (node.getChildCount() == 0))
            {
                return;
            }

            final DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            tree.collapsePath(new TreePath(model.getPathToRoot(node)));
            for (int i = 0, size = node.getChildCount(); i < size; ++i)
            {
                collapseAll(tree, node.getChildAt(i));
            }
        }

        public void expandAll(final JTree tree)
        {
            final DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            expandAll(tree, (TreeNode)model.getRoot());
            allExpanded = true;
        }

        private void expandAll(final JTree tree, final TreeNode node)
        {
            if (node.isLeaf() || !node.getAllowsChildren() || (node.getChildCount() == 0))
            {
                return;
            }

            final DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            tree.expandPath(new TreePath(model.getPathToRoot(node)));
            for (int i = 0, size = node.getChildCount(); i < size; ++i)
            {
                expandAll(tree, node.getChildAt(i));
            }
        }

        public void reset()
        {
            expandedNodes.clear();
            collapsedNodes.clear();
            allExpanded = false;
        }

        public void restoreTree(final JTree tree)
        {
            if (allExpanded)
            {
                expandAll(tree);
                return;
            }

            final DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            final LibraryNode root = (LibraryNode)model.getRoot();

            tree.removeTreeExpansionListener(this);
            try
            {
                Iterator iterator = new HashSet(expandedNodes).iterator();
                while (iterator.hasNext())
                {
                    final PogLibrary lib = (PogLibrary)iterator.next();
                    final LibraryNode node = root.findNodeFor(lib);
                    if (node != null)
                    {
                        final TreePath path = new TreePath(model.getPathToRoot(node));
                        tree.expandPath(path);
                    }
                    else
                    {
                        expandedNodes.remove(lib);
                    }
                }

                iterator = new HashSet(collapsedNodes).iterator();
                while (iterator.hasNext())
                {
                    final PogLibrary lib = (PogLibrary)iterator.next();
                    final LibraryNode node = root.findNodeFor(lib);
                    if (node != null)
                    {
                        final TreePath path = new TreePath(model.getPathToRoot(node));
                        tree.collapseRow(tree.getRowForPath(path));
                    }
                    else
                    {
                        collapsedNodes.remove(lib);
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
         * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
         */
        public void treeCollapsed(final TreeExpansionEvent event)
        {
            final LibraryNode node = (LibraryNode)event.getPath().getLastPathComponent();
            expandedNodes.remove(node.getLibrary());
            collapsedNodes.add(node.getLibrary());
            allExpanded = false;
        }

        /*
         * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
         */
        public void treeExpanded(final TreeExpansionEvent event)
        {
            final LibraryNode node = (LibraryNode)event.getPath().getLastPathComponent();
            expandedNodes.add(node.getLibrary());
            collapsedNodes.remove(node.getLibrary());
        }
    }

    /**
     * A TreeNode representing a library.
     * 
     * @author Iffy
     */
    private static class LibraryNode implements TreeNode
    {
        private final Vector     children;
        private final PogLibrary library;
        private LibraryNode      parent;

        public LibraryNode(final LibraryNode mommy, final PogLibrary lib)
        {
            this(lib);
            parent = mommy;
        }

        public LibraryNode(final PogLibrary lib)
        {
            library = lib;
            children = new Vector();

            final List childLibs = library.getChildren();
            for (int i = 0; i < childLibs.size(); i++)
            {
                children.add(new LibraryNode(this, (PogLibrary)childLibs.get(i)));
            }

            final List pogs = library.getPogs();
            for (int i = 0; i < pogs.size(); i++)
            {
                children.add(new PogNode(this, (PogType)pogs.get(i)));
            }
        }

        /*
         * @see javax.swing.tree.TreeNode#children()
         */
        public Enumeration children()
        {
            return children.elements();
        }

        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(final Object o)
        {
            if (o == this)
            {
                return true;
            }

            final LibraryNode node = (LibraryNode)o;
            if (node.getLibrary().equals(getLibrary()))
            {
                return true;
            }

            return false;
        }

        /**
         * Recursively finds the node representing that library.
         * 
         * @param lib Library to find node for.
         * @return Node for library, or null if not found.
         */
        public LibraryNode findNodeFor(final PogLibrary lib)
        {
            if (getLibrary().equals(lib))
            {
                return this;
            }

            for (int i = 0, size = children.size(); i < size; ++i)
            {
                final Object o = children.get(i);
                if (!(o instanceof LibraryNode))
                {
                    continue;
                }

                final LibraryNode child = (LibraryNode)o;
                final LibraryNode node = child.findNodeFor(lib);
                if (node != null)
                {
                    return node;
                }
            }
            return null;
        }

        // --- Object Implementation ---

        /**
         * Recursively finds the node representing that library.
         * 
         * @param lib Library to find node for.
         * @return Node for library, or null if not found.
         */
        public PogNode findNodeFor(final PogType pogType)
        {
            for (int i = 0, size = children.size(); i < size; ++i)
            {
                final Object o = children.get(i);
                if (o instanceof LibraryNode)
                {
                    continue;
                }

                final PogNode child = (PogNode)o;
                final PogType pog = child.getPog();
                if (pog.equals(pogType))
                {
                    return child;
                }
            }
            return null;
        }

        /*
         * @see javax.swing.tree.TreeNode#getAllowsChildren()
         */
        public boolean getAllowsChildren()
        {
            return true;
        }

        /*
         * @see javax.swing.tree.TreeNode#getChildAt(int)
         */
        public TreeNode getChildAt(final int childIndex)
        {
            return (TreeNode)children.get(childIndex);
        }

        // --- TreeNode Implementation ---

        /*
         * @see javax.swing.tree.TreeNode#getChildCount()
         */
        public int getChildCount()
        {
            return children.size();
        }

        /*
         * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
         */
        public int getIndex(final TreeNode node)
        {
            return children.indexOf(node);
        }

        /**
         * @return Returns the library.
         */
        public PogLibrary getLibrary()
        {
            return library;
        }

        /*
         * @see javax.swing.tree.TreeNode#getParent()
         */
        public TreeNode getParent()
        {
            return parent;
        }

        /*
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return getLibrary().hashCode();
        }

        /*
         * @see javax.swing.tree.TreeNode#isLeaf()
         */
        public boolean isLeaf()
        {
            return false;
        }

        /*
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return library.getName();
        }
    }

    /**
     * A Leaf TreeNode representing a Pog.
     * 
     * @author Iffy
     */
    private static class PogNode implements TreeNode
    {
        private final LibraryNode parent;
        private final PogType     pog;

        public PogNode(final LibraryNode parentNode, final PogType child)
        {
            parent = parentNode;
            pog = child;
        }

        /*
         * @see javax.swing.tree.TreeNode#children()
         */
        public Enumeration children()
        {
            return null;
        }

        /*
         * @see javax.swing.tree.TreeNode#getAllowsChildren()
         */
        public boolean getAllowsChildren()
        {
            return false;
        }

        // --- Object Implementation ---

        /*
         * @see javax.swing.tree.TreeNode#getChildAt(int)
         */
        public TreeNode getChildAt(final int childIndex)
        {
            return null;
        }

        // --- TreeNode Implementation ---

        /*
         * @see javax.swing.tree.TreeNode#getChildCount()
         */
        public int getChildCount()
        {
            return 0;
        }

        /*
         * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
         */
        public int getIndex(final TreeNode node)
        {
            return -1;
        }

        /**
         * @return Returns the library.
         */
        public PogLibrary getLibrary()
        {
            return parent.getLibrary();
        }

        /*
         * @see javax.swing.tree.TreeNode#getParent()
         */
        public TreeNode getParent()
        {
            return parent;
        }

        /**
         * @return Returns the pog.
         */
        public PogType getPog()
        {
            return pog;
        }

        /*
         * @see javax.swing.tree.TreeNode#isLeaf()
         */
        public boolean isLeaf()
        {
            return true;
        }

        /*
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return pog.getLabel();
        }
    }
    /**
     * Cell renderer for the tree.
     * 
     * @author Iffy
     */
    private static class PogTreeCellRenderer extends JComponent implements TreeCellRenderer
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2706069607127310996L;
        boolean                   expanded         = false;
        boolean                   leaf             = false;
        PogLibrary                library          = null;
        PogType                   pogType          = null;

        public PogTreeCellRenderer()
        {
        }

        /**
         * @return The font to be used to draw the label.
         */
        private Font getMyFont()
        {
            return FONT_NODE;
        }

        /**
         * @return The computed dimensions for this PogComponent, based on the pog and label.
         */
        private Dimension getMySize()
        {
            if (pogType != null)
            {
                int w = POG_ICON_SIZE;
                int h = POG_ICON_SIZE;
                final String label = pogType.getLabel();
                if ((label != null) && (label.length() > 0))
                {
                    final Font f = getMyFont();
                    final FontRenderContext frc = new FontRenderContext(null, false, false);
                    final Rectangle stringBounds = f.getStringBounds(label, frc).getBounds();
                    w += stringBounds.width + POG_TEXT_PADDING;
                    if (stringBounds.height > h)
                    {
                        h = stringBounds.height;
                    }
                }

                return new Dimension(w + TOTAL_SPACE, h + TOTAL_SPACE);
            }
            else if (library != null)
            {
                int w = 0;
                int h = 0;
                final String label = library.getName();
                if ((label != null) && (label.length() > 0))
                {
                    final Font f = getMyFont();
                    final FontRenderContext frc = new FontRenderContext(null, false, false);
                    final Rectangle stringBounds = f.getStringBounds(label, frc).getBounds();
                    h = stringBounds.height;
                    w = stringBounds.width;
                }
                return new Dimension(w + TOTAL_SPACE, h + TOTAL_SPACE);
            }

            return null;
        }

        /*
         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object,
         *      boolean, boolean, boolean, int, boolean)
         */
        public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
            final boolean exp, final boolean lf, final int r, final boolean focus)
        {
            library = null;
            pogType = null;
            if (value instanceof LibraryNode)
            {
                final LibraryNode node = (LibraryNode)value;
                library = node.getLibrary();
            }
            else if (value instanceof PogNode)
            {
                final PogNode node = (PogNode)value;
                pogType = node.getPog();
            }
            expanded = exp;
            leaf = lf;

            final Dimension size = getMySize();
            setSize(size);
            setPreferredSize(size);

            return this;
        }

        /*
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        protected void paintComponent(final Graphics g)
        {
            final Graphics2D g2 = (Graphics2D)g;
            g2.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
            g2.setColor(Color.BLACK);
            if (pogType != null)
            {
                pogType.drawListIcon(g2, SPACE + (POG_ICON_SIZE - pogType.getListIconWidth()) / 2, SPACE
                    + (POG_ICON_SIZE - pogType.getListIconHeight()) / 2);

                final String label = pogType.getLabel();
                if ((label != null) && (label.length() > 0))
                {
                    g2.setFont(getMyFont());
                    final FontMetrics fm = g2.getFontMetrics();
                    final Rectangle stringBounds = fm.getStringBounds(label, g2).getBounds();
                    stringBounds.x = SPACE + POG_ICON_SIZE + POG_TEXT_PADDING;
                    stringBounds.y = SPACE + (POG_ICON_SIZE - stringBounds.height) / 2;
                    g2.drawString(label, stringBounds.x, stringBounds.y + fm.getAscent());
                }
            }
            else if (library != null)
            {
                final String label = library.getName();
                if ((label != null) && (label.length() > 0))
                {
                    g2.setFont(getMyFont());
                    final FontMetrics fm = g2.getFontMetrics();
                    final Rectangle stringBounds = fm.getStringBounds(label, g2).getBounds();
                    stringBounds.x = SPACE;
                    stringBounds.y = SPACE;
                    g2.drawString(label, stringBounds.x, stringBounds.y + fm.getAscent());
                }
            }
            g2.dispose();
        }
    }

    private static final Color    BACKGROUND_COLOR     = Color.WHITE;
    private static final Font     FONT_NODE            = Font.decode("sansserif-12");

    private static final int      HOVER_MARGIN         = 8;

    private static final Color    POG_BACKGROUND_COLOR = new Color(0x66, 0x66, 0x66, 0xCC);
    private static final int      POG_BORDER           = 0;
    private static final Color    POG_BORDER_COLOR     = Color.BLACK;
    public static final int       POG_ICON_SIZE        = 32;

    private static final int      POG_MARGIN           = 0;
    private static final int      POG_PADDING          = 1;

    // --- Types -----------------------------------------------------------------------------------------------------

    private static final int      POG_TEXT_PADDING     = 4;

    /**
     * 
     */
    private static final long     serialVersionUID     = 4592355721815311412L;

    private static final int      SPACE                = POG_PADDING + POG_BORDER + POG_MARGIN;

    private static final int      TOTAL_SPACE          = SPACE * 2;

    // --- Members ---------------------------------------------------------------------------------------------------

    private final BranchTracker   m_branchTracker      = new BranchTracker();

    /**
     * A handle to the canvas.
     */
    private final GametableCanvas m_canvas;

    // --- Pog Dragging Members ---

    /**
     * The currently grabbed pog.
     */
    private Pog                   m_grabbedPog         = null;

    /**
     * The offset at which the pog was grabbed.
     */
    private Point                 m_grabbedPogOffset   = null;

    /**
     * The position of the currently grabbed pog.
     */
    private Point                 m_grabbedPogPosition = null;

    /**
     * Pog that mouse is hovering over, if any.
     */
    private PogType               m_hoverPog           = null;
    /**
     * The list of pogs held in this panel.
     */
    private PogLibrary            m_library            = null;
    private Point                 m_mousePosition      = null;

    // --- Child Components ---

    private JTree                 pogTree              = null;
    private JScrollPane           scrollPane           = null;

    // --- Constructors ----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param canvas Handle to the canvas.
     * @param bPogsMode True if for Pogs, False if for Underlays.
     */
    public PogPanel(final PogLibrary library, final GametableCanvas canvas)
    {
        m_library = library;
        m_canvas = canvas;
        initialize();
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * @return Returns the currently grabbed pog.
     */
    public Pog getGrabbedPog()
    {
        return m_grabbedPog;
    }

    /**
     * @return Returns the position where the the currently grabbed pog is, in pog panel coordinates.
     */
    public Point getGrabOffset()
    {
        return m_grabbedPogOffset;
    }

    /**
     * @return Returns the position where the the currently grabbed pog is, in pog panel coordinates.
     */
    public Point getGrabPosition()
    {
        return m_grabbedPogPosition;
    }

    /**
     * This method initializes pogTree
     * 
     * @return javax.swing.JTree
     */
    private JTree getPogTree()
    {
        if (pogTree == null)
        {
            pogTree = new JTree(new LibraryNode(m_library));
            pogTree.setBackground(BACKGROUND_COLOR);
            pogTree.setRootVisible(false);
            pogTree.setShowsRootHandles(true);
            pogTree.setToggleClickCount(1);
            pogTree.setSelectionModel(null);
            pogTree.setCellRenderer(new PogTreeCellRenderer());
            pogTree.setRowHeight(0);
            pogTree.addTreeExpansionListener(m_branchTracker);
            pogTree.setFocusable(false);

            pogTree.addMouseListener(new MouseAdapter()
            {
                /*
                 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseEntered(final MouseEvent e)
                {
                    m_mousePosition = new Point(e.getX(), e.getY());
                }

                /*
                 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseExited(final MouseEvent e)
                {
                    m_mousePosition = null;
                    if (m_hoverPog != null)
                    {
                        m_hoverPog = null;
                        repaint();
                    }
                }

                /*
                 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
                 */
                public void mousePressed(final MouseEvent e)
                {
                    final TreePath path = pogTree.getClosestPathForLocation(e.getX(), e.getY());
                    final Object val = path.getLastPathComponent();
                    if (val instanceof PogNode)
                    {
                        final PogNode node = (PogNode)val;
                        final Point screenCoords = UtilityFunctions.getScreenCoordinates(pogTree, new Point(e.getX(), e
                            .getY()));
                        final Point localCoords = new Point(node.getPog().getWidth(0) / 2,
                            node.getPog().getHeight(0) / 2);
                        grabPog(node.getPog(), screenCoords, localCoords);
                    }
                }

                /*
                 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseReleased(final MouseEvent e)
                {
                    releasePog();
                }

            });

            pogTree.addMouseMotionListener(new MouseMotionAdapter()
            {
                /*
                 * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
                 */
                public void mouseDragged(final MouseEvent e)
                {
                    mouseMoved(e);
                }

                /*
                 * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
                 */
                public void mouseMoved(final MouseEvent e)
                {
                    final Point screenCoords = UtilityFunctions.getScreenCoordinates(pogTree, new Point(e.getX(), e
                        .getY()));
                    m_mousePosition = UtilityFunctions.getComponentCoordinates(PogPanel.this, screenCoords);
                    moveGrabPosition(screenCoords);

                    final TreePath path = pogTree.getPathForLocation(e.getX(), e.getY());
                    final PogType oldPog = m_hoverPog;
                    m_hoverPog = null;
                    if (path != null)
                    {
                        final Object val = path.getLastPathComponent();
                        if (val instanceof PogNode)
                        {
                            final PogNode node = (PogNode)val;
                            m_hoverPog = node.getPog();
                        }
                    }

                    if ((m_hoverPog != oldPog) || (oldPog != null))
                    {
                        repaint();
                    }
                }
            });
        }
        return pogTree;
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
            scrollPane = new JScrollPane();
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setViewportView(getPogTree());
        }
        return scrollPane;
    }

    private JToolBar getToolbar()
    {
        final JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setMargin(new Insets(2, 2, 2, 2));
        toolbar.setRollover(true);

        final Insets margin = new Insets(2, 2, 2, 2);
        final Image collapseImage = UtilityFunctions.getImage("assets/collapse.png");
        final JButton collapseButton = new JButton("Collapse All", new ImageIcon(collapseImage));
        collapseButton.setFocusable(false);
        collapseButton.setMargin(margin);
        collapseButton.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_branchTracker.collapseAll(getPogTree());
            }
        });
        toolbar.add(collapseButton);

        final Image expandImage = UtilityFunctions.getImage("assets/expand.png");
        final JButton expandButton = new JButton("Expand All", new ImageIcon(expandImage));
        expandButton.setMargin(margin);
        expandButton.setFocusable(false);
        expandButton.addActionListener(new ActionListener()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_branchTracker.expandAll(getPogTree());
            }
        });
        toolbar.add(expandButton);

        return toolbar;
    }

    private void grabPog(final PogType p, final Point pos, final Point offset)
    {
        m_grabbedPog = new Pog(p);
        m_grabbedPogPosition = pos;
        m_grabbedPogOffset = offset;
        m_canvas.pogDrag();
        repaint();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        setLayout(new BorderLayout());
        add(getScrollPane(), BorderLayout.CENTER);
        add(getToolbar(), BorderLayout.NORTH);
    }

    // --- Component Implementation ---

    private void moveGrabPosition(final Point pos)
    {
        if (m_grabbedPog != null)
        {
            m_grabbedPogPosition = pos;
            m_canvas.pogDrag();
            repaint();
        }
    }

    // --- Private Methods ----

    /*
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(final Graphics g)
    {
        super.paint(g);
        if (m_grabbedPog != null)
        {
            final Graphics2D g2 = (Graphics2D)g;
            final Point localPos = UtilityFunctions.getComponentCoordinates(this, getGrabPosition());
            final Point offset = getGrabOffset();
            g2.translate(localPos.x - offset.x, localPos.y - offset.y);
            m_grabbedPog.getPogType().drawGhostly(g2, 0, 0, 0, 1, 1);
            g2.dispose();
        }
        else if ((m_hoverPog != null) && (m_mousePosition != null))
        {
            final Graphics2D g2 = (Graphics2D)g;
            int drawX = m_mousePosition.x;
            int drawY = m_mousePosition.y + 16;
            final int overBottom = (drawY + m_hoverPog.getHeight(0)) - getHeight();
            final int overTop = -(drawY - (m_hoverPog.getHeight(0) + 16));
            if (overBottom > overTop)
            {
                drawY -= m_hoverPog.getHeight(0) + 16;
            }

            if (drawX > getWidth() - m_hoverPog.getWidth(0) - HOVER_MARGIN)
            {
                drawX = getWidth() - m_hoverPog.getWidth(0) - HOVER_MARGIN;
            }

            if (drawX < HOVER_MARGIN)
            {
                drawX = HOVER_MARGIN;
            }

            g2.translate(drawX, drawY);
            g2.setColor(POG_BACKGROUND_COLOR);
            g2.fillRect(-POG_PADDING, -POG_PADDING, m_hoverPog.getWidth(0) + POG_PADDING * 2, m_hoverPog.getHeight(0)
                + POG_PADDING * 2);
            g2.setColor(POG_BORDER_COLOR);
            g2.drawRect(-POG_PADDING, -POG_PADDING, m_hoverPog.getWidth(0) + POG_PADDING * 2 - 1, m_hoverPog
                .getHeight(0)
                + POG_PADDING * 2 - 1);
            m_hoverPog.drawTranslucent(g2, 0, 0, 0.9f, 0, 1, 1);
            g2.dispose();
        }
    }

    /**
     * Takes the current pog list and adds them as components.
     */
    public void populateChildren()
    {
        pogTree.setModel(new DefaultTreeModel(new LibraryNode(m_library)));
        m_branchTracker.restoreTree(pogTree);
    }

    private void releasePog()
    {
        if (m_grabbedPog != null)
        {
            m_canvas.pogDrop();
            m_grabbedPog = null;
            m_grabbedPogPosition = null;
            m_grabbedPogOffset = null;
            repaint();
        }
    }
}
