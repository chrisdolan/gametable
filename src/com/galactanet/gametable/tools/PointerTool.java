/*
 * PointerTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.Graphics;
import java.awt.Point;

import com.galactanet.gametable.AbstractTool;
import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.Pog;
import com.galactanet.gametable.Tool;


/**
 * TODO: comment
 * 
 * @author iffy
 */
public class PointerTool extends AbstractTool implements Tool
{
    private GametableCanvas m_canvas;
    private Pog             m_grabbedPog;
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
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(int x, int y)
    {
        m_mouseAnchor = new Point(x, y);
        m_mouseFloat = m_mouseAnchor;
        m_grabbedPog = m_canvas.getPogAt(m_mouseAnchor);
        m_grabOffset = new Point(m_mouseAnchor.x - m_grabbedPog.getX(), m_mouseAnchor.y - m_grabbedPog.getY());
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y)
    {
        m_grabbedPog = null;
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y)
    {
        m_mouseFloat = new Point(x, y);
        m_grabbedPog.setPosition(m_mouseFloat);
        m_grabbedPog.setPosition(m_grabbedPog.getX() - m_grabbedPog.getFaceSize() * GametableCanvas.BASE_SQUARE_SIZE / 2,
            m_grabbedPog.getY() - m_grabbedPog.getFaceSize() * GametableCanvas.BASE_SQUARE_SIZE / 2);
        m_canvas.snapPogToGrid(m_grabbedPog);
        m_canvas.repaint();
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
    }

}
