/*
 * HandTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.Point;

import com.galactanet.gametable.GametableCanvas;


/**
 * TODO: comment
 * 
 * @author iffy
 */
public class HandTool extends NullTool
{
    private GametableCanvas m_canvas;
    private Point           m_startScroll;
    private Point           m_startMouse;



    /**
     * Constructor;
     */
    public HandTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_startScroll = null;
        m_startMouse = null;
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(int x, int y, int modifierMask)
    {
        m_startScroll = m_canvas
            .drawToModel(m_canvas.getSharedMap().getScrollX(), m_canvas.getSharedMap().getScrollY());
        m_startMouse = m_canvas.modelToView(x, y);
        m_canvas.setToolCursor(1);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y, int modifierMask)
    {
        if (m_startScroll != null)
        {
            Point mousePosition = m_canvas.modelToView(x, y);
            Point viewDelta = new Point(m_startMouse.x - mousePosition.x, m_startMouse.y - mousePosition.y);
            Point modelDelta = m_canvas.drawToModel(viewDelta);
            m_canvas.scrollMapTo(m_startScroll.x + modelDelta.x, m_startScroll.y + modelDelta.y);
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y, int modifierMask)
    {
        m_startScroll = null;
        m_startMouse = null;
        m_canvas.setToolCursor(0);
    }

}
