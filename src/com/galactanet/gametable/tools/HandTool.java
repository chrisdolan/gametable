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
    private Point           m_startMouse;
    private Point           m_startScroll;

    /**
     * Constructor;
     */
    public HandTool()
    {
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(final GametableCanvas canvas)
    {
        m_canvas = canvas;
        m_startScroll = null;
        m_startMouse = null;
    }

    public void endAction()
    {
        m_startScroll = null;
        m_startMouse = null;
        m_canvas.setToolCursor(0);
    }

    /*
     * @see com.galactanet.gametable.Tool#isBeingUsed()
     */
    public boolean isBeingUsed()
    {
        return (m_startScroll != null);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(final int x, final int y, final int modifierMask)
    {
        m_startScroll = m_canvas
            .drawToModel(m_canvas.getPublicMap().getScrollX(), m_canvas.getPublicMap().getScrollY());
        m_startMouse = m_canvas.modelToView(x, y);
        m_canvas.setToolCursor(1);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(final int x, final int y, final int modifierMask)
    {
        endAction();
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(final int x, final int y, final int modifierMask)
    {
        if (m_startScroll != null)
        {
            final Point mousePosition = m_canvas.modelToView(x, y);
            final Point viewDelta = new Point(m_startMouse.x - mousePosition.x, m_startMouse.y - mousePosition.y);
            final Point modelDelta = m_canvas.drawToModel(viewDelta);
            m_canvas.scrollMapTo(m_startScroll.x + modelDelta.x, m_startScroll.y + modelDelta.y);
        }
    }

}
