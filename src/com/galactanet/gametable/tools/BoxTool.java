/*
 * EraseTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.*;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.LineSegment;
import com.galactanet.gametable.UtilityFunctions;



/**
 * Map tool for erasing lines.
 * 
 * @author iffy
 */
public class BoxTool extends NullTool
{
    private GametableCanvas m_canvas;
    private Point           m_mouseAnchor;
    private Point           m_mouseFloat;
    private Point           m_mousePosition;

    /**
     * Default Constructor.
     */
    public BoxTool()
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
     * @see com.galactanet.gametable.Tool#isBeingUsed()
     */
    public boolean isBeingUsed()
    {
        return (m_mouseAnchor != null);
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(int x, int y, int modifierMask)
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
     * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y, int modifierMask)
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
     * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y, int modifierMask)
    {
        if (m_mouseAnchor != null && !m_mouseAnchor.equals(m_mouseFloat))
        {
            // we're going to add 4 lines
            Color drawColor = GametableFrame.getGametableFrame().m_drawColor;
            Point topLeft = new Point(m_mouseAnchor);
            Point bottomRight = new Point(m_mouseFloat);
            Point topRight = new Point(bottomRight.x, topLeft.y);
            Point bottomLeft = new Point(topLeft.x, bottomRight.y);

            LineSegment top = new LineSegment(topLeft, topRight, drawColor);
            LineSegment left = new LineSegment(topLeft, bottomLeft, drawColor);
            LineSegment right = new LineSegment(topRight, bottomRight, drawColor);
            LineSegment bottom = new LineSegment(bottomLeft, bottomRight, drawColor);

            LineSegment[] toAdd = new LineSegment[] {
                top, left, right, bottom
            };

            m_canvas.addLineSegments(toAdd);
        }
        endAction();
    }

    public void endAction()
    {
        m_mouseAnchor = null;
        m_mouseFloat = null;
        m_canvas.repaint();
    }

    /*
     * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
     */
    /*
     * Modified from original to have separate distance indicators for
     * each dimension.
     */
    public void paint(Graphics g)
    {
        if (m_mouseAnchor != null)
        {
            Graphics2D g2 = (Graphics2D)g.create();

            g2.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color drawColor = GametableFrame.getGametableFrame().m_drawColor;
            g2.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 102));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Rectangle drawRect = createRectangle(m_canvas.modelToDraw(m_mouseAnchor), m_canvas
                .modelToDraw(m_mouseFloat));
            g2.draw(drawRect);
            g2.dispose();

            Rectangle modelRect = createRectangle(m_mouseAnchor, m_mouseFloat);
            --modelRect.width;
            --modelRect.height;
            double squaresWidth = m_canvas.modelToSquares(modelRect.width);
            double squaresHeight = m_canvas.modelToSquares(modelRect.height);
            double indicatorThreshold = .75 * GametableFrame.getGametableFrame().grid_multiplier;
            if (squaresWidth> indicatorThreshold)
            {
                squaresWidth = Math.round(squaresWidth * 100) / 100.0;

                Graphics2D g3 = (Graphics2D)g.create();

                g3.setFont(Font.decode("sans-12"));
                
//                String s1 = squaresWidth + " x " + squaresHeight + "u";
                String sw = Double.toString(squaresWidth)+  GametableFrame.getGametableFrame().grid_unit;
                
                FontMetrics fm = g3.getFontMetrics();
                Rectangle rect = fm.getStringBounds(sw, g3).getBounds();

                rect.grow(3, 1);

                
/*                Point drawPoint = m_canvas.modelToDraw(m_mousePosition);
                drawPoint.y -= rect.height + rect.y + 10;
                Point viewPoint = m_canvas.modelToView(m_canvas.drawToModel(drawPoint));
                if (viewPoint.y - rect.height < 0)
                {
                    drawPoint = m_canvas.modelToDraw(m_mousePosition);
                    drawPoint.y -= rect.y - 24;
                }

                if (viewPoint.x + rect.width >= m_canvas.getWidth())
                {
                    drawPoint.x -= rect.width + 10;
                }
*/
                Point drawPoint = m_canvas.modelToDraw(m_mouseAnchor);
                Point mousePoint = m_canvas.modelToDraw(m_mouseFloat);
                drawPoint.x = (drawPoint.x + mousePoint.x)/2;
                drawPoint.y = mousePoint.y - 10;
                g3.translate(drawPoint.x, drawPoint.y);
                g3.setColor(new Color(0x00, 0x99, 0x00, 0xAA));
                g3.fill(rect);
                g3.setColor(new Color(0x00, 0x66, 0x00));
                g3.draw(rect);
                g3.setColor(new Color(0xFF, 0xFF, 0xFF, 0xCC));
                g3.drawString(sw, 0, 0);
                g3.dispose();
                
            }
            if (squaresHeight > indicatorThreshold) 
            {
                Point drawPoint = m_canvas.modelToDraw(m_mouseAnchor);
                Point mousePoint = m_canvas.modelToDraw(m_mouseFloat);
                Graphics2D g4 = (Graphics2D)g.create();
                squaresHeight = Math.round(squaresHeight * 100) / 100.0;
                g4.setFont(Font.decode("sans-12"));
                String sh = Double.toString(squaresHeight) + GametableFrame.getGametableFrame().grid_unit;
                FontMetrics fm2 = g4.getFontMetrics();
                Rectangle rect2 = fm2.getStringBounds(sh, g4).getBounds();
                rect2.grow(3,1);
                drawPoint.x = mousePoint.x + 10;
                drawPoint.y = (drawPoint.y + mousePoint.y)/2;
                g4.translate(drawPoint.x, drawPoint.y);
                g4.setColor(new Color(0x00, 0x99, 0x00, 0xAA));
                g4.fill(rect2);
                g4.setColor(new Color(0x00, 0x66, 0x00));
                g4.draw(rect2);
                g4.setColor(new Color(0xFF, 0xFF, 0xFF, 0xCC));
                g4.drawString(sh, 0, 0);
                g4.dispose();
            }
        }
    }

    private static Rectangle createRectangle(Point a, Point b)
    {
        int x = Math.min(a.x, b.x);
        int y = Math.min(a.y, b.y);
        int width = Math.abs(b.x - a.x) + 1;
        int height = Math.abs(b.y - a.y) + 1;

        return new Rectangle(x, y, width, height);
    }
}
