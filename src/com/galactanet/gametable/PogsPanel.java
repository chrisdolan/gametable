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
import java.io.File;
import java.util.*;
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

    private static final int   TEXT_PADDING     = 0;
    private static final int   PADDING          = 2;
    private static final int   BORDER           = 1;
    private static final int   MARGIN           = 0;

    private static final Color COLOR_BORDER     = Color.BLACK;
    private static final Color COLOR_BACKGROUND = new Color(0x66, 0x66, 0x66, 0x66);

    private static final int   SPACE            = PADDING + BORDER + MARGIN;
    private static final int   TOTAL_SPACE      = SPACE * 2;

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
        private Pog    pog;

        /**
         * The label to display underneath this pog.
         */
        private String label;

        /**
         * Whether this component is selected or not.
         */
        boolean        selected = false;

        /**
         * Constructor.
         * 
         * @param p Pog to adapt.
         */
        public PogComponent(Pog p)
        {
            pog = p;
            label = pog.m_fileName;
            int start = label.lastIndexOf(File.separatorChar) + 1;
            int end = label.lastIndexOf('.');
            if (end < 0)
            {
                end = label.length();
            }

            label = label.substring(start, end);
            setSize(getMySize());
            setPreferredSize(getMySize());

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
        public Pog getPog()
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
                h += Math.round(lm.getHeight() - lm.getLeading()) + TEXT_PADDING;
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
                g2.setColor(COLOR_BACKGROUND);
                g2.fillRect(MARGIN, MARGIN, d.width - (MARGIN * 2), d.height - (MARGIN * 2));
                g2.setColor(COLOR_BORDER);
                g2.drawRect(MARGIN, MARGIN, d.width - (MARGIN * 2) - 1, d.height - (MARGIN * 2) - 1);
            }

            pog.draw(g2, (d.width - pog.getWidth()) / 2, SPACE, null);

            if (label != null && label.length() > 0)
            {
                g2.setFont(getMyFont());
                FontMetrics fm = g2.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(label, g2).getBounds();
                // g2.setColor(new Color(128, 128, 128, 255));
                stringBounds.x = (d.width - stringBounds.width) / 2;
                stringBounds.y = SPACE + pog.getHeight() + TEXT_PADDING;
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
     * True if this panel is for pogs, false for underlays.
     */
    private boolean         m_bIsPogsMode;

    /**
     * The list of pogs held in this panel.
     */
    private List            m_pogs                = new ArrayList();

    /**
     * The list of PogComponents held in this panel.
     */
    private List            m_pogComponents       = new ArrayList();

    /**
     * Set of acquired pog names.
     */
    private Set             m_acquiredPogs        = new HashSet();

    
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
    public PogsPanel(GametableCanvas canvas, boolean bPogsMode)
    {
        super(new FlowLayout(FlowLayout.LEADING, 5, 5), true);
        setBackground(new Color(0x734D22));
        m_canvas = canvas;
        m_bIsPogsMode = bPogsMode;
        addKeyListener(m_canvas);
        acquirePogs();
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * Ensures that this panel has all the available pogs loaded.
     */
    public void acquirePogs()
    {
        // TODO: only "acquire" new pogs
        List newPogs = new ArrayList();
        String modeStr = m_bIsPogsMode ? "pogs" : "underlays";
        File pogPath = new File(modeStr);
        if (pogPath.exists())
        {
            String[] files = pogPath.list();

            int len = files.length;
            for (int i = 0; i < len; ++i)
            {
                String filename = modeStr + File.separator + files[i];
                if (m_acquiredPogs.contains(filename))
                {
                    continue;
                }
                
                File test = new File(filename);

                if (test.isFile())
                {
                    try
                    {
                        Pog toAdd = new Pog();
                        toAdd.init(m_canvas, filename);

                        // add it to the appropriate array
                        toAdd.m_bIsUnderlay = !m_bIsPogsMode;
                        newPogs.add(toAdd);
                        m_acquiredPogs.add(filename);
                    }
                    catch (Exception ex)
                    {
                        // any exceptions thrown in this process cancel
                        // the addition of that one pog.
                        Log.log(Log.SYS, ex);
                    }
                }
            }
        }

        m_pogs.addAll(newPogs);
        sortPogsByHeight(m_pogs);
        removeAll();
        populateChildren(m_pogs);
    }

    /**
     * @return The list of pogs held in this panel.
     */
    public List getPogs()
    {
        return Collections.unmodifiableList(m_pogs);
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

    private void grabPog(Pog p, Point pos, Point offset)
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
     * Takes the current pog list and adds them as components.
     */
    private void populateChildren(List pogs)
    {
        int size = pogs.size();
        for (int i = 0; i < size; ++i)
        {
            Pog p = (Pog)pogs.get(i);
            PogComponent c = new PogComponent(p);
            m_pogComponents.add(c);
            add(c);
        }
        setSize(getPreferredSize());
        revalidate();
        repaint();
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
                Pog pa = (Pog)a;
                Pog pb = (Pog)b;
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
