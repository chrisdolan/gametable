/*
 * LineTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.*;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.LineSegment;


/**
 * Tool for drawing lines onto the map.
 * 
 * @author iffy
 */
public class LineTool extends NullTool
{
    private GametableCanvas m_canvas;
    private Point           m_mouseAnchor;
    private Point           m_mouseFloat;



    /**
     * Default Constructor.
     */
    public LineTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_mouseAnchor = null;
        m_mouseFloat = null;
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(int x, int y, int modifierMask)
    {
        m_mouseAnchor = new Point(x, y);
        m_mouseFloat = m_mouseAnchor;
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y, int modifierMask)
    {
        if (m_mouseAnchor != null)
        {
            m_mouseFloat = new Point(x, y);
            m_canvas.repaint();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y, int modifierMask)
    {
        if (m_mouseAnchor != null)
        {
            LineSegment ls = new LineSegment(m_mouseAnchor, m_mouseFloat, GametableFrame.g_gameTableFrame.m_drawColor);
            m_canvas.addLineSegments(new LineSegment[] { ls });
            
            m_mouseAnchor = null;
            m_mouseFloat = null;
            m_canvas.repaint();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        if (m_mouseAnchor != null)
        {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color drawColor = GametableFrame.g_gameTableFrame.m_drawColor;
            g2.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 102));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Point drawAnchor = m_canvas.modelToDraw(m_mouseAnchor);
            Point drawFloat = m_canvas.modelToDraw(m_mouseFloat);
            g2.drawLine(drawAnchor.x, drawAnchor.y, drawFloat.x, drawFloat.y);
            g2.dispose();
        }
    }
}
