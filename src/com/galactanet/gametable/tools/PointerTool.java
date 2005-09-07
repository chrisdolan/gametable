/*
 * PointerTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.Graphics;
import java.awt.Point;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableMap;
import com.galactanet.gametable.Pog;
import com.galactanet.gametable.Tool;


/**
 * TODO: comment
 * 
 * @author iffy
 */
public class PointerTool extends NullTool implements Tool
{
    private GametableCanvas m_canvas;
    private Pog             m_grabbedPog;
    private Pog             m_ghostPog;
    private boolean         m_snapping;
    private Point           m_grabOffset;
    private Point           m_mouseAnchor;
    private Point           m_mouseFloat;



    /**
     * Constructor
     */
    public PointerTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_grabbedPog = null;
        m_ghostPog = null;
        m_grabOffset = null;
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
        GametableMap map = m_canvas.getActiveMap();
        m_grabbedPog = map.getPogAt(m_mouseAnchor);
        if (m_grabbedPog != null)
        {
            m_ghostPog = new Pog(m_grabbedPog);
            m_grabOffset = new Point(m_grabbedPog.getX() - m_mouseAnchor.x, m_grabbedPog.getY() - m_mouseAnchor.y);
            setSnapping(modifierMask);
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y, int modifierMask)
    {
        setSnapping(modifierMask);
        m_mouseFloat = new Point(x, y);
        if (m_grabbedPog != null)
        {
            if (m_snapping)
            {
                m_ghostPog.setPosition(m_mouseFloat.x + m_grabOffset.x, m_mouseFloat.y + m_grabOffset.y);
                m_canvas.snapPogToGrid(m_ghostPog);
            }
            else
            {
                m_ghostPog.setPosition(m_mouseFloat.x + m_grabOffset.x, m_mouseFloat.y + m_grabOffset.y);
            }
            m_canvas.repaint();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y, int modifierMask)
    {
        mouseMoved(x, y, modifierMask);
        if (m_grabbedPog != null)
        {
            m_grabbedPog.setPosition(m_ghostPog.getPosition());
            if (!m_canvas.isPointVisible(m_mouseFloat))
            {
                // they removed this pog
                m_canvas.removePog(m_grabbedPog.m_ID);
            }
            else
            {
                m_canvas.movePog(m_grabbedPog.m_ID, m_ghostPog.getX(), m_ghostPog.getY());
            }
        }
        m_grabbedPog = null;
        m_ghostPog = null;
        m_grabOffset = null;
        m_mouseAnchor = null;
        m_mouseFloat = null;
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        if (m_ghostPog != null && m_canvas.isPointVisible(m_mouseFloat))
        {
            m_ghostPog.drawGhostlyToCanvas(g);
        }
    }
    
    private void setSnapping(int modifierMask)
    {
        if ((modifierMask & MODIFIER_CTRL) > 0)
        {
            m_snapping = false;
        }
        else
        {
            m_snapping = true;
        }
    }
}
