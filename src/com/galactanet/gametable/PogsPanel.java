/*
 * PogsPanel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;



/**
 * A Swing panel for a grabbable list of pogs.
 * 
 * @author sephalon
 * @author iffy
 */
public class PogsPanel extends JPanel implements Scrollable
{
    // --- Constants -------------------------------------------------------------------------------------------------

    private static final int   POG_TEXT_PADDING     = 0;
    private static final int   POG_PADDING          = 2;
    private static final int   POG_BORDER           = 1;
    private static final int   POG_MARGIN           = 1;

    private static final Color POG_BORDER_COLOR     = Color.BLACK;
    private static final Color POG_BACKGROUND_COLOR = new Color(0x66, 0x66, 0x66, 0x66);
    private static final Color BACKGROUND_COLOR     = new Color(0x73, 0x4D, 0x22);

    private static final int   SPACE                = POG_PADDING + POG_BORDER + POG_MARGIN;
    private static final int   TOTAL_SPACE          = SPACE * 2;

    // --- Types -----------------------------------------------------------------------------------------------------

    /**
     * A class that adapts a Pog into a Swing JComponent.
     * 
     * @author iffy
     */
    private class PogComponent extends JComponent
    {
        /**
         * The pog this PogComponent is adapting.
         */
        private PogType pog;

        /**
         * The label to display underneath this pog.
         */
        private String  label;

        /**
         * Whether this component is selected or not.
         */
        boolean         selected = false;

        /**
         * Constructor.
         * 
         * @param p Pog to adapt.
         */
        public PogComponent(PogType p)
        {
            pog = p;
            label = pog.getFilename();
            int start = label.lastIndexOf("/") + 1;
            int end = label.lastIndexOf('.');
            if (end < 0)
            {
                end = label.length();
            }

            label = label.substring(start, end);
            Dimension size = getMySize();
            setSize(size);
            setPreferredSize(size);

            addMouseListener(new MouseAdapter()
            {
                /*
                 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
                 */
                public void mousePressed(MouseEvent e)
                {
                    selected = true;
                    Point localCoords = new Point(e.getX(), e.getY());
                    Point screenCoords = UtilityFunctions.getScreenCoordinates(PogComponent.this, localCoords);
                    grabPog(pog, screenCoords, localCoords);
                }

                /*
                 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
                 */
                public void mouseReleased(MouseEvent e)
                {
                    selected = false;
                    releasePog();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter()
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
                    Point screenCoords = UtilityFunctions.getScreenCoordinates(PogComponent.this, new Point(e.getX(), e
                        .getY()));
                    moveGrabPosition(screenCoords);
                }
            });
        }

        /**
         * @return The pog for this PogComponent.
         */
        public PogType getPog()
        {
            return pog;
        }

        /**
         * @return The font to be used to draw the label.
         */
        private Font getMyFont()
        {
            return Font.decode("system-bold");
        }

        /**
         * @return The computed dimensions for this PogComponent, based on the pog and label.
         */
        private Dimension getMySize()
        {
            int w = pog.getWidth();
            int h = pog.getHeight();
            if (label != null && label.length() > 0)
            {
                Font f = getMyFont();
                FontRenderContext frc = new FontRenderContext(null, false, false);
                Rectangle stringBounds = f.getStringBounds(label, frc).getBounds();
                LineMetrics lm = f.getLineMetrics(PogsPanel.this.toString(), frc);
                h += Math.round(lm.getHeight() - lm.getLeading()) + POG_TEXT_PADDING;
                if (stringBounds.width > w)
                {
                    w = stringBounds.width;
                }
            }

            return new Dimension(w + TOTAL_SPACE, h + TOTAL_SPACE);
        }

        /*
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;
            Dimension d = getSize();
            if (selected)
            {
                g2.setColor(POG_BORDER_COLOR);
                g2.fillRect(POG_MARGIN, POG_MARGIN, d.width - (POG_MARGIN * 2), d.height - (POG_MARGIN * 2));
                g2.setColor(BACKGROUND_COLOR);
                g2.fillRect(POG_MARGIN + POG_BORDER, POG_MARGIN + POG_BORDER,
                    d.width - ((POG_MARGIN + POG_BORDER) * 2), d.height - ((POG_MARGIN + POG_BORDER) * 2));
                g2.setColor(POG_BACKGROUND_COLOR);
                g2.fillRect(POG_MARGIN + POG_BORDER, POG_MARGIN + POG_BORDER,
                    d.width - ((POG_MARGIN + POG_BORDER) * 2), d.height - ((POG_MARGIN + POG_BORDER) * 2));
            }

            pog.draw(g2, (d.width - pog.getWidth()) / 2, SPACE);

            if (label != null && label.length() > 0)
            {
                g2.setFont(getMyFont());
                FontMetrics fm = g2.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(label, g2).getBounds();
                // g2.setColor(new Color(128, 128, 128, 255));
                stringBounds.x = (d.width - stringBounds.width) / 2;
                stringBounds.y = SPACE + pog.getHeight() + POG_TEXT_PADDING;
                stringBounds.height -= fm.getLeading();
                // g2.fill(stringBounds);
                g2.setColor(Color.WHITE);
                g2.drawString(label, stringBounds.x, stringBounds.y + fm.getAscent() - fm.getLeading());
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
    private PogLibrary      m_pogs                = null;

    // --- Pog Dragging Members ---

    /**
     * The currently grabbed pog.
     */
    private Pog             m_grabbedPog          = null;

    /**
     * The position of the currently grabbed pog.
     */
    private Point           m_grabbedPogPosition  = null;

    /**
     * The offset at which the pog was grabbed.
     */
    private Point           m_grabbedPogOffset    = null;

    /**
     * The current component for the grabbed pog.
     */
    private PogComponent    m_grabbedPogComponent = null;

    // --- Constructors ----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param canvas Handle to the canvas.
     * @param bPogsMode True if for Pogs, False if for Underlays.
     */
    public PogsPanel(PogLibrary library, GametableCanvas canvas)
    {
        super(new FlowLayout(FlowLayout.LEADING, 5, 5), true);
        m_pogs = library;
        m_canvas = canvas;
        setBackground(BACKGROUND_COLOR);
        addKeyListener(m_canvas);
        populateChildren();
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * @return The pog library represented by this panel.
     */
    public PogLibrary getPogLibrary()
    {
        return m_pogs;
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
        removeAll();
        List pogs = new ArrayList(m_pogs.getAllPogs());
        sortPogsByHeight(pogs);
        int size = pogs.size();
        for (int i = 0; i < size; ++i)
        {
            PogType p = (PogType)pogs.get(i);
            PogComponent c = new PogComponent(p);
            add(c);
        }
        setSize(getPreferredSize());
        revalidate();
        repaint();
    }

    private void grabPog(PogType p, Point pos, Point offset)
    {
        m_grabbedPog = new Pog(p);
        m_grabbedPogPosition = pos;
        m_grabbedPogOffset = offset;
        // System.out.println("grabPog(" + (m_grabbedPog != null ? m_grabbedPog.m_fileName : null) + ", "
        // + m_grabbedPogPosition + ")");
        m_grabbedPogComponent = new PogComponent(p);

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
            m_grabbedPogComponent = null;
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

    /**
     * In-place sorts the list of pogs by height.
     * 
     * @param toSort List of Pogs to sort.
     */
    private static void sortPogsByHeight(List toSort)
    {
        Collections.sort(toSort, new Comparator()
        {
            /*
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare(Object a, Object b)
            {
                PogType pa = (PogType)a;
                PogType pb = (PogType)b;
                return (pa.getHeight() - pb.getHeight());
            }
        });
    }

    // --- Component Implementation ---

    /*
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        if (m_grabbedPogComponent != null)
        {
            Graphics2D g2 = (Graphics2D)g;
            Point localPos = UtilityFunctions.getComponentCoordinates(this, getGrabPosition());
            Point offset = getGrabOffset();
            g2.translate(localPos.x - offset.x, localPos.y - offset.y);
            m_grabbedPogComponent.paint(g2);
            g2.dispose();
        }
    }

    /*
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        int maxY = 0;
        int maxX = 0;
        Component[] comps = getComponents();
        for (int i = 0; i < comps.length; ++i)
        {
            Rectangle r = comps[i].getBounds();
            int y = (r.y + r.height) - 1;
            if (y > maxY)
            {
                maxY = y;
            }

            int x = r.width - 1;
            if (x > maxX)
            {
                maxX = x;
            }
        }

        if (getParent() != null)
        {
            Rectangle r = getParent().getBounds();
            int y = (r.height - 1);
            if (y > maxY)
            {
                maxY = y;
            }

            int x = (r.width - 1);
            if (x > maxX)
            {
                maxX = x;
            }
        }

        return new Dimension(maxX + 2, maxY + 2);
    }

    // --- Scrollable Implementation ---

    /*
     * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        // TODO: calculate size based on content
        return getPreferredSize();
    }

    /*
     * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return visibleRect.height * 3 / 4;
    }

    /*
     * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
     */
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }

    /*
     * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    /*
     * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return visibleRect.height / 15;
    }

}
