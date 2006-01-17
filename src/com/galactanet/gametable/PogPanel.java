/*
 * PogPanel.java: GameTable is in the Public Domain.
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

    public static final int    POG_ICON_SIZE        = 32;

    private static final int   POG_TEXT_PADDING     = 4;
    private static final int   POG_PADDING          = 1;
    private static final int   POG_BORDER           = 0;
    private static final int   POG_MARGIN           = 0;

    private static final int   HOVER_MARGIN         = 8;

    private static final Color POG_BORDER_COLOR     = Color.BLACK;
    private static final Color POG_BACKGROUND_COLOR = new Color(0x66, 0x66, 0x66, 0xCC);
    private static final Color BACKGROUND_COLOR     = Color.WHITE;
    private static final Font  FONT_NODE            = Font.decode("sansserif-12");

    private static final int   SPACE                = POG_PADDING + POG_BORDER + POG_MARGIN;
    private static final int   TOTAL_SPACE          = SPACE * 2;

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
            LibraryNode root = (LibraryNode)model.getRoot();

            tree.removeTreeExpansionListener(this);
            try
            {
                Iterator iterator = new HashSet(expandedNodes).iterator();
                while (iterator.hasNext())
                {
                    PogLibrary lib = (PogLibrary)iterator.next();
                    LibraryNode node = root.findNodeFor(lib);
                    if (node != null)
                    {
                        TreePath path = new TreePath(model.getPathToRoot(node));
                        tree.expandPath(path);
                    }
                    else
                    {
                        expandedNodes.remove(node.getLibrary());
                    }
                }

                iterator = new HashSet(collapsedNodes).iterator();
                while (iterator.hasNext())
                {
                    PogLibrary lib = (PogLibrary)iterator.next();
                    LibraryNode node = root.findNodeFor(lib);
                    if (node != null)
                    {
                        TreePath path = new TreePath(model.getPathToRoot(node));
                        tree.collapseRow(tree.getRowForPath(path));
                    }
                    else
                    {
                        collapsedNodes.remove(node.getLibrary());
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
            LibraryNode node = (LibraryNode)event.getPath().getLastPathComponent();
            expandedNodes.add(node.getLibrary());
            collapsedNodes.remove(node.getLibrary());
        }

        /*
         * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
         */
        public void treeCollapsed(TreeExpansionEvent event)
        {
            LibraryNode node = (LibraryNode)event.getPath().getLastPathComponent();
            expandedNodes.remove(node.getLibrary());
            collapsedNodes.add(node.getLibrary());
        }
    }

    /**
     * A Leaf TreeNode representing a Pog.
     * 
     * @author Iffy
     */
    private static class PogNode implements TreeNode
    {
        private LibraryNode parent;
        private PogType     pog;

        public PogNode(LibraryNode parentNode, PogType child)
        {
            parent = parentNode;
            pog = child;
        }

        /**
         * @return Returns the library.
         */
        public PogLibrary getLibrary()
        {
            return parent.getLibrary();
        }

        /**
         * @return Returns the pog.
         */
        public PogType getPog()
        {
            return pog;
        }

        // --- Object Implementation ---

        /*
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return pog.getLabel();
        }

        // --- TreeNode Implementation ---

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

        /*
         * @see javax.swing.tree.TreeNode#getChildAt(int)
         */
        public TreeNode getChildAt(int childIndex)
        {
            return null;
        }

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
        public int getIndex(TreeNode node)
        {
            return -1;
        }

        /*
         * @see javax.swing.tree.TreeNode#getParent()
         */
        public TreeNode getParent()
        {
            return parent;
        }

        /*
         * @see javax.swing.tree.TreeNode#isLeaf()
         */
        public boolean isLeaf()
        {
            return true;
        }
    }

    /**
     * A TreeNode representing a library.
     * 
     * @author Iffy
     */
    private static class LibraryNode implements TreeNode
    {
        private LibraryNode parent;
        private PogLibrary  library;
        private Vector      children;

        public LibraryNode(LibraryNode mommy, PogLibrary lib)
        {
            this(lib);
            parent = mommy;
        }

        public LibraryNode(PogLibrary lib)
        {
            library = lib;
            children = new Vector();

            List childLibs = library.getChildren();
            for (int i = 0; i < childLibs.size(); i++)
            {
                children.add(new LibraryNode(this, (PogLibrary)childLibs.get(i)));
            }

            List pogs = library.getPogs();
            for (int i = 0; i < pogs.size(); i++)
            {
                children.add(new PogNode(this, (PogType)pogs.get(i)));
            }
        }

        /**
         * @return Returns the library.
         */
        public PogLibrary getLibrary()
        {
            return library;
        }

        /**
         * Recursively finds the node representing that library.
         * 
         * @param lib Library to find node for.
         * @return Node for library, or null if not found.
         */
        public LibraryNode findNodeFor(PogLibrary lib)
        {
            if (getLibrary().equals(lib))
            {
                return this;
            }

            for (int i = 0, size = children.size(); i < size; ++i)
            {
                Object o = children.get(i);
                if (!(o instanceof LibraryNode))
                {
                    continue;
                }

                LibraryNode child = (LibraryNode)o;
                LibraryNode node = child.findNodeFor(lib);
                if (node != null)
                {
                    return node;
                }
            }
            return null;
        }

        /**
         * Recursively finds the node representing that library.
         * 
         * @param lib Library to find node for.
         * @return Node for library, or null if not found.
         */
        public PogNode findNodeFor(PogType pogType)
        {
            for (int i = 0, size = children.size(); i < size; ++i)
            {
                Object o = children.get(i);
                if (o instanceof LibraryNode)
                {
                    continue;
                }

                PogNode child = (PogNode)o;
                PogType pog = child.getPog();
                if (pog.equals(pogType))
                {
                    return child;
                }
            }
            return null;
        }

        // --- Object Implementation ---

        /*
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return library.getName();
        }

        /*
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return getLibrary().hashCode();
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

            LibraryNode node = (LibraryNode)o;
            if (node.getLibrary().equals(getLibrary()))
            {
                return true;
            }

            return false;
        }

        // --- TreeNode Implementation ---

        /*
         * @see javax.swing.tree.TreeNode#children()
         */
        public Enumeration children()
        {
            return children.elements();
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
        public TreeNode getChildAt(int childIndex)
        {
            return (TreeNode)children.get(childIndex);
        }

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
        public int getIndex(TreeNode node)
        {
            return children.indexOf(node);
        }

        /*
         * @see javax.swing.tree.TreeNode#getParent()
         */
        public TreeNode getParent()
        {
            return parent;
        }

        /*
         * @see javax.swing.tree.TreeNode#isLeaf()
         */
        public boolean isLeaf()
        {
            return false;
        }
    }

    /**
     * Cell renderer for the tree.
     * 
     * @author Iffy
     */
    private static class PogTreeCellRenderer extends JComponent implements TreeCellRenderer
    {
        PogLibrary library  = null;
        PogType    pogType  = null;
        boolean    expanded = false;
        boolean    leaf     = false;

        public PogTreeCellRenderer()
        {
        }

        /*
         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object,
         *      boolean, boolean, boolean, int, boolean)
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean lf,
            int r, boolean focus)
        {
            library = null;
            pogType = null;
            if (value instanceof LibraryNode)
            {
                LibraryNode node = (LibraryNode)value;
                library = node.getLibrary();
            }
            else if (value instanceof PogNode)
            {
                PogNode node = (PogNode)value;
                pogType = node.getPog();
            }
            expanded = exp;
            leaf = lf;

            Dimension size = getMySize();
            setSize(size);
            setPreferredSize(size);

            return this;
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
                String label = pogType.getLabel();
                if (label != null && label.length() > 0)
                {
                    Font f = getMyFont();
                    FontRenderContext frc = new FontRenderContext(null, false, false);
                    Rectangle stringBounds = f.getStringBounds(label, frc).getBounds();
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
                String label = library.getName();
                if (label != null && label.length() > 0)
                {
                    Font f = getMyFont();
                    FontRenderContext frc = new FontRenderContext(null, false, false);
                    Rectangle stringBounds = f.getStringBounds(label, frc).getBounds();
                    h = stringBounds.height;
                    w = stringBounds.width;
                }
                return new Dimension(w + TOTAL_SPACE, h + TOTAL_SPACE);
            }

            return null;
        }

        /*
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;
            g2.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
            g2.setColor(Color.BLACK);
            if (pogType != null)
            {
                pogType.drawListIcon(g2, SPACE + (POG_ICON_SIZE - pogType.getListIconWidth()) / 2, SPACE
                    + (POG_ICON_SIZE - pogType.getListIconHeight()) / 2);

                String label = pogType.getLabel();
                if (label != null && label.length() > 0)
                {
                    g2.setFont(getMyFont());
                    FontMetrics fm = g2.getFontMetrics();
                    Rectangle stringBounds = fm.getStringBounds(label, g2).getBounds();
                    stringBounds.x = SPACE + POG_ICON_SIZE + POG_TEXT_PADDING;
                    stringBounds.y = SPACE + (POG_ICON_SIZE - stringBounds.height) / 2;
                    g2.drawString(label, stringBounds.x, stringBounds.y + fm.getAscent());
                }
            }
            else if (library != null)
            {
                String label = library.getName();
                if (label != null && label.length() > 0)
                {
                    g2.setFont(getMyFont());
                    FontMetrics fm = g2.getFontMetrics();
                    Rectangle stringBounds = fm.getStringBounds(label, g2).getBounds();
                    stringBounds.x = SPACE;
                    stringBounds.y = SPACE;
                    g2.drawString(label, stringBounds.x, stringBounds.y + fm.getAscent());
                }
            }
            g2.dispose();
        }
    }

    // --- Members ---------------------------------------------------------------------------------------------------

    /**
     * A handle to the canvas.
     */
    private GametableCanvas m_canvas;

    /**
     * The list of pogs held in this panel.
     */
    private PogLibrary      m_library            = null;

    // --- Pog Dragging Members ---

    /**
     * The currently grabbed pog.
     */
    private Pog             m_grabbedPog         = null;

    /**
     * The position of the currently grabbed pog.
     */
    private Point           m_grabbedPogPosition = null;

    /**
     * The offset at which the pog was grabbed.
     */
    private Point           m_grabbedPogOffset   = null;

    /**
     * Pog that mouse is hovering over, if any.
     */
    private PogType         m_hoverPog           = null;
    private Point           m_mousePosition      = null;
    private BranchTracker   m_branchTracker      = new BranchTracker();

    // --- Child Components ---

    private JScrollPane     scrollPane           = null;
    private JTree           pogTree              = null;

    // --- Constructors ----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param canvas Handle to the canvas.
     * @param bPogsMode True if for Pogs, False if for Underlays.
     */
    public PogPanel(PogLibrary library, GametableCanvas canvas)
    {
        m_library = library;
        m_canvas = canvas;
        initialize();
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        setLayout(new BorderLayout());
        add(getScrollPane(), BorderLayout.CENTER);
    }

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
    public Point getGrabPosition()
    {
        return m_grabbedPogPosition;
    }

    /**
     * @return Returns the position where the the currently grabbed pog is, in pog panel coordinates.
     */
    public Point getGrabOffset()
    {
        return m_grabbedPogOffset;
    }

    /**
     * Takes the current pog list and adds them as components.
     */
    public void populateChildren()
    {
        pogTree.setModel(new DefaultTreeModel(new LibraryNode(m_library)));
        m_branchTracker.restoreTree(pogTree);
    }

    private void grabPog(PogType p, Point pos, Point offset)
    {
        m_grabbedPog = new Pog(p);
        m_grabbedPogPosition = pos;
        m_grabbedPogOffset = offset;
        // System.out.println("grabPog(" + (m_grabbedPog != null ? m_grabbedPog.m_fileName : null) + ", "
        // + m_grabbedPogPosition + ")");

        m_canvas.pogDrag();
        repaint();
    }

    private void releasePog()
    {
        // System.out.println("releasePog(" + (m_grabbedPog != null ? m_grabbedPog.m_fileName : null) + ", "
        // + m_grabbedPogPosition + ")");
        if (m_grabbedPog != null)
        {
            m_canvas.pogDrop();
            m_grabbedPog = null;
            m_grabbedPogPosition = null;
            m_grabbedPogOffset = null;
            repaint();
        }
    }

    private void moveGrabPosition(Point pos)
    {
        if (m_grabbedPog != null)
        {
            m_grabbedPogPosition = pos;
            // System.out.println("moveGrabPosition(" + (m_grabbedPog != null ? m_grabbedPog.m_fileName : null) + ", "
            // + m_grabbedPogPosition + ")");
            m_canvas.pogDrag();
            repaint();
        }
    }

    // --- Component Implementation ---

    /*
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        if (m_grabbedPog != null)
        {
            Graphics2D g2 = (Graphics2D)g;
            Point localPos = UtilityFunctions.getComponentCoordinates(this, getGrabPosition());
            Point offset = getGrabOffset();
            g2.translate(localPos.x - offset.x, localPos.y - offset.y);
            m_grabbedPog.getPogType().drawGhostly(g2, 0, 0);
            g2.dispose();
        }
        else if (m_hoverPog != null && m_mousePosition != null)
        {
            Graphics2D g2 = (Graphics2D)g;
            int drawX = m_mousePosition.x;
            int drawY = m_mousePosition.y + 16;
            int overBottom = (drawY + m_hoverPog.getHeight()) - getHeight();
            int overTop = -(drawY - (m_hoverPog.getHeight() + 16));
            if (overBottom > overTop)
            {
                drawY -= m_hoverPog.getHeight() + 16;
            }

            if (drawX > getWidth() - m_hoverPog.getWidth() - HOVER_MARGIN)
            {
                drawX = getWidth() - m_hoverPog.getWidth() - HOVER_MARGIN;
            }

            if (drawX < HOVER_MARGIN)
            {
                drawX = HOVER_MARGIN;
            }

            g2.translate(drawX, drawY);
            g2.setColor(POG_BACKGROUND_COLOR);
            g2.fillRect(-POG_PADDING, -POG_PADDING, m_hoverPog.getWidth() + POG_PADDING * 2, m_hoverPog.getHeight()
                + POG_PADDING * 2);
            g2.setColor(POG_BORDER_COLOR);
            g2.drawRect(-POG_PADDING, -POG_PADDING, m_hoverPog.getWidth() + POG_PADDING * 2 - 1, m_hoverPog.getHeight()
                + POG_PADDING * 2 - 1);
            m_hoverPog.drawTranslucent(g2, 0, 0, 0.9f);
            g2.dispose();
        }
    }

    // --- Private Methods ----

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
                 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
                 */
                public void mousePressed(MouseEvent e)
                {
                    TreePath path = pogTree.getClosestPathForLocation(e.getX(), e.getY());
                    Object val = path.getLastPathComponent();
                    if (val instanceof PogNode)
                    {
                        PogNode node = (PogNode)val;
                        Point screenCoords = UtilityFunctions.getScreenCoordinates(pogTree, new Point(e.getX(), e
                            .getY()));
                        Point localCoords = new Point(node.getPog().getWidth() / 2, node.getPog().getHeight() / 2);
                        grabPog(node.getPog(), screenCoords, localCoords);
                    }
                }

                /*
                 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseReleased(MouseEvent e)
                {
                    releasePog();
                }

                /*
                 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseEntered(MouseEvent e)
                {
                    m_mousePosition = new Point(e.getX(), e.getY());
                }

                /*
                 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseExited(MouseEvent e)
                {
                    m_mousePosition = null;
                    if (m_hoverPog != null)
                    {
                        m_hoverPog = null;
                        repaint();
                    }
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
                    m_mousePosition = UtilityFunctions.getComponentCoordinates(PogPanel.this, screenCoords);
                    moveGrabPosition(screenCoords);

                    TreePath path = pogTree.getPathForLocation(e.getX(), e.getY());
                    PogType oldPog = m_hoverPog;
                    m_hoverPog = null;
                    if (path != null)
                    {
                        Object val = path.getLastPathComponent();
                        if (val instanceof PogNode)
                        {
                            PogNode node = (PogNode)val;
                            m_hoverPog = node.getPog();
                        }
                    }

                    if (m_hoverPog != oldPog || oldPog != null)
                    {
                        repaint();
                    }
                }
            });
        }
        return pogTree;
    }

} // @jve:decl-index=0:visual-constraint="101,42"
