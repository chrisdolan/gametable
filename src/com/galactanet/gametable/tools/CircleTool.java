/*
 * CircleTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.*;

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
    private PenAsset        m_penAsset;
    private double          m_rad;
    private Point           m_mouseAnchor;
    private Point           m_mouseFloat;

    private Point           m_mousePosition;

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
        //m_penAsset = null;
        m_mouseAnchor = null;
        m_mouseFloat = null;
    }

    public void endAction()
    {
        //m_penAsset = null;
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
        m_penAsset = new PenAsset(GametableFrame.getGametableFrame().m_drawColor);

        // Figure the radius of the circle and use a PenAsset as if we had drawn
        // the circle with the PenTool.
        if ((m_mouseAnchor != null) && !m_mouseAnchor.equals(m_mouseFloat))
        {
            m_rad = m_mouseAnchor.distance(m_mouseFloat.x, m_mouseFloat.y);
            // TODO With this loop, all circles are composed of the same number of segments regardless of size. 
            // Maybe make theta increment dependent on radius?
            for (double theta = 0; theta < 2 * Math.PI; theta += .1)
            {
                m_penAsset.addPoint((int)(m_mouseAnchor.x + Math.cos(theta) * m_rad), (int)(m_mouseAnchor.y + Math.sin(theta) * m_rad));
            }
            m_penAsset.addPoint((int)(m_mouseAnchor.x + m_rad), m_mouseAnchor.y);
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
    public void paint(final Graphics g)
    {
        if (m_mouseAnchor != null)
        {
            int circleDiameter = 0;
            int circleRadius = 0;

            Graphics2D g2 = (Graphics2D)g.create();

            //g2.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double dist = m_canvas.getGridMode().getDistance(m_mouseFloat.x, m_mouseFloat.y, m_mouseAnchor.x,
                m_mouseAnchor.y);
            double squaresDistance = m_canvas.modelToSquares(dist);
            squaresDistance = Math.round(squaresDistance * 100) / 100.0;

            Color drawColor = GametableFrame.getGametableFrame().m_drawColor;
            g2.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 102));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Point drawAnchor = m_canvas.modelToDraw(m_mouseAnchor);
            Point drawFloat = m_canvas.modelToDraw(m_mouseFloat);
            // draw line out to circle circumference
            g2.drawLine(drawAnchor.x, drawAnchor.y, drawFloat.x, drawFloat.y);
            // get the length of the hypotenuse
            circleRadius = (int)(java.lang.Math.sqrt(java.lang.Math.pow(Math.abs(drawFloat.x - drawAnchor.x), 2)
                + java.lang.Math.pow(Math.abs(drawFloat.y - drawAnchor.y), 2)));
            // locate the upper left corner for the demo circle
            circleDiameter = 2 * circleRadius;
            // draw the circle
            g2.drawOval((drawAnchor.x - circleRadius), (drawAnchor.y - circleRadius), circleDiameter, circleDiameter);

            double indicatorThreshold = .10 * GametableFrame.getGametableFrame().grid_multiplier;
            if (squaresDistance >= indicatorThreshold)
            {
                Graphics2D g3 = (Graphics2D)g.create();
                g3.setFont(Font.decode("sans-12"));

                String s = squaresDistance + GametableFrame.getGametableFrame().grid_unit;
                FontMetrics fm = g3.getFontMetrics();
                Rectangle rect = fm.getStringBounds(s, g3).getBounds();

                rect.grow(3, 1);
                // display the radius
                Point drawPoint = new Point((drawAnchor.x + drawFloat.x) / 2, (drawAnchor.y + drawFloat.y) / 2);
                g3.translate(drawPoint.x, drawPoint.y);
                g3.setColor(new Color(0x00, 0x99, 0x00, 0xAA));
                g3.fill(rect);
                g3.setColor(new Color(0x00, 0x66, 0x00));
                g3.draw(rect);
                g3.setColor(new Color(0xFF, 0xFF, 0xFF, 0xCC));
                g3.drawString(s, 0, 0);
                g3.dispose();
            }
            // don't forget the penAsset
            //m_penAsset.draw(g2, m_canvas);
            g2.dispose();
        }
    }
}
