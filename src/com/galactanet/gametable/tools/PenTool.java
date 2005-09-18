/*
 * PenTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.Graphics;
import java.awt.Graphics2D;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.LineSegment;
import com.galactanet.gametable.PenAsset;


/**
 * Tool for freehand drawing on the map.
 * 
 * @author iffy
 */
public class PenTool extends NullTool
{
    private GametableCanvas m_canvas;
    private PenAsset        m_penAsset;



    /**
     * Default Constructor.
     */
    public PenTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_penAsset = null;
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
    public void mouseButtonPressed(int x, int y, int modifierMask)
    {
        // TODO: move m_drawColor into some more reasonable access point
        m_penAsset = new PenAsset(GametableFrame.g_gameTableFrame.m_drawColor);
        m_penAsset.addPoint(x, y);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y, int modifierMask)
    {
        if (m_penAsset != null)
        {
            m_penAsset.addPoint(x, y);
            m_canvas.repaint();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y, int modifierMask)
    {
        if (m_penAsset != null)
        {
            m_penAsset.smooth();
            LineSegment[] lines = m_penAsset.getLineSegments();
            if (lines != null)
            {
                m_canvas.addLineSegments(lines);
            }

            m_penAsset = null;
            m_canvas.repaint();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        if (m_penAsset != null)
        {
            Graphics2D g2 = (Graphics2D)g.create();
            // g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            m_penAsset.draw(g2, m_canvas);
            g2.dispose();
        }
    }
}
