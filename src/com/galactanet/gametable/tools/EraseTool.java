/*
 * EraseTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.*;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableFrame;



/**
 * Map tool for erasing lines.
 * 
 * @author iffy
 */
public class EraseTool extends NullTool
{
    private static Rectangle createRectangle(final Point a, final Point b)
    {
        final int x = Math.min(a.x, b.x);
        final int y = Math.min(a.y, b.y);
        final int width = Math.abs(b.x - a.x) + 1;
        final int height = Math.abs(b.y - a.y) + 1;

        return new Rectangle(x, y, width, height);
    }

    private final boolean   m_bEraseColor;
    private GametableCanvas m_canvas;
    private Point           m_mouseAnchor;

    private Point           m_mouseFloat;

    /**
     * Default Constructor.
     */
    public EraseTool()
    {
        this(false);
    }

    /**
     * Constructor specifying color mode.
     */
    public EraseTool(final boolean color)
    {
        m_bEraseColor = color;
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

    public void endAction()
    {
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
        m_mouseAnchor = new Point(x, y);
        m_mouseFloat = m_mouseAnchor;
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(final int x, final int y, final int modifierMask)
    {
        if ((m_mouseAnchor != null) && !m_mouseAnchor.equals(m_mouseFloat))
        {
            if (m_bEraseColor)
            {
                m_canvas.erase(createRectangle(m_mouseAnchor, m_mouseFloat), true,
                    GametableFrame.getGametableFrame().m_drawColor.getRGB());
            }
            else
            {
                m_canvas.erase(createRectangle(m_mouseAnchor, m_mouseFloat), false, 0);
            }
        }
        endAction();
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(final int x, final int y, final int modifierMask)
    {
        if (m_mouseAnchor != null)
        {
            m_mouseFloat = new Point(x, y);
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

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
                2f
            }, 0f));
            Rectangle rect = createRectangle(m_canvas.modelToDraw(m_mouseAnchor), m_canvas.modelToDraw(m_mouseFloat));
            g2.draw(rect);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
                2f
            }, 2f));
            rect = createRectangle(m_canvas.modelToDraw(m_mouseAnchor), m_canvas.modelToDraw(m_mouseFloat));
            g2.draw(rect);

            g2.dispose();
        }
    }
}
