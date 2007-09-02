/*
 * CircleTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.LineSegment;
import com.galactanet.gametable.PenAsset;



/**
 * Tool for drawing circles on the map.
 * 
 */
public class CircleTool extends NullTool
{
    private GametableCanvas m_canvas;
    private Point           m_origin;
    private PenAsset        m_penAsset;
    private double          m_rad;

    /**
     * Default Constructor.
     */
    public CircleTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(final GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_penAsset = null;
    }

    public void endAction()
    {
        m_penAsset = null;
        m_canvas.repaint();
    }

    /*
     * @see com.galactanet.gametable.Tool#isBeingUsed()
     */
    public boolean isBeingUsed()
    {
        return (m_penAsset != null);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(final int x, final int y, final int modifierMask)
    {
        // TODO: move m_drawColor into some more reasonable access point
        m_penAsset = new PenAsset(GametableFrame.getGametableFrame().m_drawColor);
        m_origin = new Point(x, y);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(final int x, final int y, final int modifierMask)
    {
        // Figure the radius of the circle and use a PenAsset as if we had drawn
        // the circle with the PenTool.
        if (m_penAsset != null)
        {
            m_rad = m_origin.distance(x, y);
            // TODO With this loop, all circles are composed of the same number
            // of segments regardless of size. Maybe make theta increment
            // dependent on radius?
            for (double theta = 0; theta < 2 * Math.PI; theta += .1)
            {
                m_penAsset.addPoint((int)(m_origin.x + Math.cos(theta) * m_rad), (int)(m_origin.y + Math.sin(theta)
                    * m_rad));
            }
            m_penAsset.addPoint((int)(m_origin.x + m_rad), m_origin.y);
            // The call to smooth() reduces the number of line segments in the
            // circle, drawing it faster but making it rougher. Uncomment if
            // redrawing takes too long.
            // m_penAsset.smooth();
            final LineSegment[] lines = m_penAsset.getLineSegments();
            if (lines != null)
            {
                m_canvas.addLineSegments(lines);
            }
        }
        endAction();
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(final int x, final int y, final int modifierMask)
    {
        // if (m_penAsset != null)
        // {
        // m_penAsset.addPoint(x, y);
        // m_canvas.repaint();
        // }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    public void paint(final Graphics g)
    {
        if (m_penAsset != null)
        {
            final Graphics2D g2 = (Graphics2D)g.create();
            // g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            m_penAsset.draw(g2, m_canvas);
            g2.dispose();
        }
    }
}
