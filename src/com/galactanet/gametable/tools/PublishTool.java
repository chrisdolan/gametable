package com.galactanet.gametable.tools;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.galactanet.gametable.*;



/**
 * Map tool for erasing lines.
 * 
 * @author iffy
 */
public class PublishTool extends NullTool
{
    private static Rectangle createRectangle(final Point a, final Point b)
    {
        final int x = Math.min(a.x, b.x);
        final int y = Math.min(a.y, b.y);
        final int width = Math.abs(b.x - a.x) + 1;
        final int height = Math.abs(b.y - a.y) + 1;

        return new Rectangle(x, y, width, height);
    }

    private GametableCanvas m_canvas;
    private GametableMap    m_from;
    private Point           m_mouseAnchor;
    private Point           m_mouseFloat;
    
    //private int             m_color = (GametableFrame.getGametableFrame().m_drawColor).getRGB();

    // private boolean m_bEraseColor;

    private GametableMap    m_to;

    /**
     * Default Constructor.
     */
    public PublishTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(final GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_mouseAnchor = null;
        m_mouseFloat = null;
    }

    // turns off all the tinting for the pogs
    public void clearTints()
    {
        for (int i = 0; i < m_from.getNumPogs(); i++)
        {
            m_from.getPog(i).setTinted(false);
        }
    }

    public void endAction()
    {
        clearTints();
        m_mouseAnchor = null;
        m_mouseFloat = null;
        m_canvas.repaint();
    }

    /*
     * @see com.galactanet.gametable.Tool#isBeingUsed()
     */
    public boolean isBeingUsed()
    {
        return (m_mouseAnchor != null);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(final int x, final int y, final int modifierMask)
    {

        if (m_canvas.isPublicMap())
        {
            /* this tool is not usable on the public map. So we cancel this action
             * GametableFrame.getGametableFrame().getToolManager().cancelToolAction();
             * return;
             * 
             * Use from public map to return objects to private map.
             */
            m_from = m_canvas.getPublicMap();
            m_to = m_canvas.getPrivateMap();
        }
        else
        {
            m_from = m_canvas.getPrivateMap();
            m_to = m_canvas.getPublicMap();
        }

        m_mouseAnchor = new Point(x, y);
        m_mouseFloat = m_mouseAnchor;
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int, int)
     */
    public void mouseButtonReleased(final int x, final int y, final int modifierMask)
    {
        if ((m_mouseAnchor != null) && !m_mouseAnchor.equals(m_mouseFloat))
        {
            // GametableFrame frame = GametableFrame.g_gameTableFrame;

            // first off, copy all the pogs/underlays over to the public layer
            for (int i = 0; i < m_from.getNumPogs(); i++)
            {
                final Pog pog = m_from.getPog(i);
                if (pog.isTinted() && (!pog.isLocked() || (modifierMask & MODIFIER_SHIFT) != 0))
                {
                    // this pog gets copied
                    final Pog newPog = new Pog(pog);
                    newPog.assignUniqueId();

                    m_canvas.setActiveMap(m_to);
                    m_canvas.addPog(newPog);
                    if (pog.isLocked())
                    {
                        newPog.setLocked(true);
                    }
                    m_canvas.setActiveMap(m_from);
                }
            }

            // now, copy over all the line segments. we run through all the
            // line segments on the private layer, and collect a list of the
            // ones that are at least partially in the rect
            final List lineList = new ArrayList();

            for (int i = 0; i < m_from.getNumLines(); i++)
            {
                final LineSegment ls = m_from.getLineAt(i);
                final LineSegment result = ls.getPortionInsideRect(m_mouseAnchor, m_mouseFloat);

                if (result != null)
                {
                    lineList.add(result);
                }
            }

            // now we have the list of lines to move over. Make them into
            // a LineSegment[]
            final LineSegment[] toAdd = new LineSegment[lineList.size()];
            for (int i = 0; i < toAdd.length; i++)
            {
                toAdd[i] = (LineSegment)lineList.get(i);
            }

            m_canvas.setActiveMap(m_to);
            m_canvas.addLineSegments(toAdd);
            m_canvas.setActiveMap(m_from);

            boolean bDeleteFromPrivate = false;
            if ((modifierMask & MODIFIER_CTRL) == 0) // not holding control
            {
                bDeleteFromPrivate = true;
            }

            // if bDeleteFromPrivate is set, then this is a MOVE, not a COPY,
            // so we have to remove the pieces from the private layer.

            if (bDeleteFromPrivate)
            {
                // remove the pogs that we moved
                for (int i = 0; i < m_from.getNumPogs(); i++)
                {
                    final Pog pog = m_from.getPog(i);
                    if (pog.isTinted() && (!pog.isLocked() || (modifierMask & MODIFIER_SHIFT) != 0))
                    {
                        m_canvas.removePog(pog.getId(), false);
                        //i--; // causing me an infinite loop for some reason.
                        if (!((m_canvas.getNetStatus() == 2) && m_canvas.isPublicMap())) { i--; }
                    }
                }

                // remove the line segments
                final Rectangle eraseRect = createRectangle(m_mouseAnchor, m_mouseFloat);
                m_canvas.erase(eraseRect, false, -1);
            }
        }
        endAction();
    }

    /*
     * public final static int       NETSTATE_HOST            = 1;
     * public final static int       NETSTATE_JOINED          = 2;
     * public final static int       NETSTATE_NONE            = 0;
     */

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(final int x, final int y, final int modifierMask)
    {
        if (m_mouseAnchor != null)
        {
            m_mouseFloat = new Point(x, y);
            setTints(modifierMask);
            m_canvas.repaint();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    public void paint(final Graphics g)
    {
        if (m_mouseAnchor != null)
        {
            final Graphics2D g2 = (Graphics2D)g.create();
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
                2f
            }, 0f));
            final Rectangle rect = createRectangle(m_canvas.modelToDraw(m_mouseAnchor), m_canvas
                .modelToDraw(m_mouseFloat));
            g2.draw(rect);
            g2.dispose();
        }
    }

    // sets all the pogs we're touching to be tinted
    public void setTints(final int modifierMask)
    {
        final Rectangle selRect = createRectangle(m_mouseAnchor, m_mouseFloat);

        for (int i = 0; i < m_from.getNumPogs(); i++)
        {
            final Pog pog = m_from.getPog(i);

            final int size = pog.getFaceSize() * GametableCanvas.BASE_SQUARE_SIZE;
            final Point tl = new Point(pog.getPosition());
            final Point br = new Point(pog.getPosition());
            br.x += size;
            br.y += size;
            final Rectangle pogRect = createRectangle(tl, br);

            if (selRect.intersects(pogRect) && (!pog.isLocked() || (modifierMask & MODIFIER_SHIFT) != 0))
            {
                // this pog will be sent
                pog.setTinted(true);
            }
            else
            {
                pog.setTinted(false);
            }
        }
    }
}
