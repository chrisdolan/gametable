/*
 * LineTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.*;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.LineSegment;
import com.galactanet.gametable.UtilityFunctions;



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
    private Point           m_mousePosition;

    /**
     * Default Constructor.
     */
    public LineTool()
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
        m_mousePosition = new Point(x, y);
        m_mouseAnchor = m_mousePosition;
        if ((modifierMask & MODIFIER_CTRL) == 0)
        {
            m_mouseAnchor = m_canvas.snapPoint(m_mouseAnchor);
        }
        m_mouseFloat = m_mouseAnchor;
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(final int x, final int y, final int modifierMask)
    {
        if (m_mouseAnchor != null)
        {
            final LineSegment ls = new LineSegment(m_mouseAnchor, m_mouseFloat,
                GametableFrame.getGametableFrame().m_drawColor);
            m_canvas.addLineSegments(new LineSegment[] {
                ls
            });
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
            m_mousePosition = new Point(x, y);
            m_mouseFloat = m_mousePosition;
            if ((modifierMask & MODIFIER_CTRL) == 0)
            {
                m_mouseFloat = m_canvas.snapPoint(m_mouseFloat);
            }
            m_canvas.repaint();
        }
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    /*
     * Modified from original to put the distance indicator near the middle of the line.
     */
    public void paint(final Graphics g)
    {
        if (m_mouseAnchor != null)
        {
            final Graphics2D g2 = (Graphics2D)g.create();

            g2.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            final double dist = m_canvas.getGridMode().getDistance(m_mouseFloat.x, m_mouseFloat.y, m_mouseAnchor.x,
                m_mouseAnchor.y);
            double squaresDistance = m_canvas.modelToSquares(dist);
            squaresDistance = Math.round(squaresDistance * 100) / 100.0;

            final Color drawColor = GametableFrame.getGametableFrame().m_drawColor;
            g2.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 102));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            final Point drawAnchor = m_canvas.modelToDraw(m_mouseAnchor);
            final Point drawFloat = m_canvas.modelToDraw(m_mouseFloat);
            g2.drawLine(drawAnchor.x, drawAnchor.y, drawFloat.x, drawFloat.y);

            if (squaresDistance >= 0.75)
            {
                final Graphics2D g3 = (Graphics2D)g.create();
                g3.setFont(Font.decode("sans-12"));

                final String s = squaresDistance + GametableFrame.getGametableFrame().grid_unit;
                final FontMetrics fm = g3.getFontMetrics();
                final Rectangle rect = fm.getStringBounds(s, g3).getBounds();

                rect.grow(3, 1);

                // Point drawPoint = m_canvas.modelToDraw(m_mousePosition);
                final Point drawPoint = new Point((drawAnchor.x + drawFloat.x) / 2, (drawAnchor.y + drawFloat.y) / 2);
                /*
                 * drawPoint.y -= rect.height + rect.y + 10; Point viewPoint =
                 * m_canvas.modelToView(m_canvas.drawToModel(drawPoint)); if (viewPoint.y - rect.height < 0) { drawPoint =
                 * m_canvas.modelToDraw(m_mousePosition); drawPoint.y -= rect.y - 24; }
                 * 
                 * if (viewPoint.x + rect.width >= m_canvas.getWidth()) { drawPoint.x -= rect.width + 10; }
                 */
                g3.translate(drawPoint.x, drawPoint.y);
                g3.setColor(new Color(0x00, 0x99, 0x00, 0xAA));
                g3.fill(rect);
                g3.setColor(new Color(0x00, 0x66, 0x00));
                g3.draw(rect);
                g3.setColor(new Color(0xFF, 0xFF, 0xFF, 0xCC));
                g3.drawString(s, 0, 0);
                g3.dispose();
            }

            g2.dispose();
        }
    }
}
