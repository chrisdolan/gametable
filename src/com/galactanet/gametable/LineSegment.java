/*
 * LineSegment.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class LineSegment
{
    private Point m_start = new Point();
    private Point m_end   = new Point();
    private Color m_color;



    public LineSegment(Point start, Point end, Color color)
    {
        m_start = new Point(start);
        m_end = new Point(end);
        m_color = color;
    }

    public LineSegment(DataInputStream dis) throws IOException
    {
        initFromPacket(dis);
    }

    public Color getColor()
    {
        return m_color;
    }

    public void writeToPacket(DataOutputStream dos) throws IOException
    {
        dos.writeInt(m_start.x);
        dos.writeInt(m_start.y);
        dos.writeInt(m_end.x);
        dos.writeInt(m_end.y);
        dos.writeInt(m_color.getRGB());
    }

    public void initFromPacket(DataInputStream dis) throws IOException
    {
        m_start.x = dis.readInt();
        m_start.y = dis.readInt();
        m_end.x = dis.readInt();
        m_end.y = dis.readInt();
        int col = dis.readInt();
        m_color = new Color(col);
    }

    // returns true if it still has content
    public LineSegment[] crop(Point start, Point end)
    {
        Rectangle r = new Rectangle();
        int x = start.x;
        int y = start.y;
        int width = end.x - start.x;
        int height = end.y - start.y;
        if (width < 0)
        {
            x += width;
            width = -width;
        }
        if (height < 0)
        {
            y += height;
            height = -height;
        }
        r.setBounds(x, y, width, height);

        if (r.contains(m_start) && r.contains(m_end))
        {
            // totally inside. we dead
            return null;
        }

        // find the intersections (There are 4 possibles.)
        Point leftInt = getIntersection(r.x, true);
        Point rightInt = getIntersection(r.x + r.width, true);
        Point topInt = getIntersection(r.y, false);
        Point bottomInt = getIntersection(r.y + r.height, false);

        leftInt = confirmOnRectEdge(leftInt, r);
        rightInt = confirmOnRectEdge(rightInt, r);
        topInt = confirmOnRectEdge(topInt, r);
        bottomInt = confirmOnRectEdge(bottomInt, r);

        // figure out which of our points is leftmost, rightmost, etc.
        Point leftMost = m_start;
        Point rightMost = m_end;
        if (m_end.x < m_start.x)
        {
            leftMost = m_end;
            rightMost = m_start;
        }

        Point topMost = m_start;
        Point bottomMost = m_end;
        if (m_end.y < m_start.y)
        {
            topMost = m_end;
            bottomMost = m_start;
        }

        // now we can start making some lines
        List returnLines = new ArrayList();

        // first off, if we didn't intersect the rect at all, it's just us
        if (leftInt == null && rightInt == null && topInt == null && bottomInt == null)
        {
            returnLines.add(this);
        }

        if (leftInt != null)
        {
            addLineSegment(returnLines, leftMost, leftInt);
        }
        if (rightInt != null)
        {
            addLineSegment(returnLines, rightMost, rightInt);
        }
        if (topInt != null)
        {
            addLineSegment(returnLines, topMost, topInt);
        }
        if (bottomInt != null)
        {
            addLineSegment(returnLines, bottomMost, bottomInt);
        }

        if (returnLines.size() == 0)
        {
            // this shouldn't happen, actually.
            // but, play it safe.
            return null;
        }

        Object[] objArray = returnLines.toArray();
        LineSegment[] ret = new LineSegment[objArray.length];
        for (int i = 0; i < objArray.length; i++)
        {
            ret[i] = (LineSegment)(objArray[i]);
        }
        return ret;
    }

    private void addLineSegment(List vector, Point start, Point end)
    {
        LineSegment ls = new LineSegment(start, end, m_color);
        vector.add(ls);
    }

    protected Point confirmOnRectEdge(Point p, Rectangle r)
    {
        // garbage in, garbage out
        if (p == null)
        {
            return null;
        }

        // if the point is within the rect than that counts
        if (r.contains(p))
        {
            return p;
        }

        // return the point if it's on the edge. null if it isn't
        if (p.x == r.x || p.x == r.x + r.width)
        {
            if (p.y > r.y && p.y < r.y + r.height)
            {
                return p;
            }
        }

        if (p.y == r.y || p.y == r.y + r.height)
        {
            if (p.x > r.x && p.x < r.x + r.width)
            {
                return p;
            }
        }

        return null;
    }

    // returns the intersection of this line segment with
    // a given pure vertical or horizontal. pos is either the x or the y,
    // depending on the boolean sent with it.
    // returns null if there is no intersection
    public Point getIntersection(int pos, boolean bIsVertical)
    {
        if (bIsVertical)
        {
            if (m_start.x < pos && m_end.x < pos)
            {
                // completely on one side of it.
                return null;
            }
            if (m_start.x > pos && m_end.x > pos)
            {
                // completely on the other side of it.
                return null;
            }
            if (m_end.x == m_start.x)
            {
                // we're parallel to it so we don't intersect at all
                return null;
            }

            // if we're here, we cross the line
            double ratio = ((double)(pos - m_start.x)) / (double)(m_end.x - m_start.x);
            double intersectX = pos;
            double intersectY = m_start.y + ratio * (m_end.y - m_start.y);
            Point ret = new Point();
            ret.x = (int)intersectX;
            ret.y = (int)intersectY;
            return ret;
        }

        if (m_start.y < pos && m_end.y < pos)
        {
            // completely on one side of it.
            return null;
        }
        if (m_start.y > pos && m_end.y > pos)
        {
            // completely on the other side of it.
            return null;
        }
        if (m_end.y == m_start.y)
        {
            // we're parallel to it so we don't intersect at all
            return null;
        }

        // if we're here, we cross the line
        double ratio = ((double)(pos - m_start.y)) / (double)(m_end.y - m_start.y);
        double intersectY = pos;
        double intersectX = m_start.x + ratio * (m_end.x - m_start.x);
        Point ret = new Point();
        ret.x = (int)intersectX;
        ret.y = (int)intersectY;
        return ret;
    }

    public void draw(Graphics g, GametableCanvas canvas)
    {
        /*
         * Graphics2D g2d = (Graphics2D)g; g2d.setStroke(new
         * BasicStroke(canvas.getLineStrokeWidth()));
         */

        // convert to draw coordinates
        Point drawStart = canvas.modelToDraw(m_start.x, m_start.y);
        Point drawEnd = canvas.modelToDraw(m_end.x, m_end.y);

        // don't draw if we're not touching the viewport at any point

        // get the draw coords of the top-left of the viewable area
        // and of the lower right
        Point portalDrawTL = new Point(canvas.getActiveMap().getScrollX(), canvas.getActiveMap().getScrollY());
        Point portalDrawBR = new Point(canvas.getActiveMap().getScrollX() + canvas.getWidth(), canvas.getActiveMap()
            .getScrollY()
            + canvas.getHeight());
        Rectangle portalRect = new Rectangle((int)portalDrawTL.getX(), (int)portalDrawTL.getY(), (int)portalDrawBR
            .getX()
            - (int)portalDrawTL.getX(), (int)portalDrawBR.getY() - (int)portalDrawTL.getY());

        // now we can start comparing to see if the line is in the rect at all
        boolean bDrawLine = false;

        // first off, if either terminus point is in the box, draw the line.
        if (portalRect.contains(drawStart))
        {
            bDrawLine = true;
        }
        else if (portalRect.contains(drawEnd))
        {
            bDrawLine = true;
        }
        else
        {
            // neither point is inside the rect. Now we have to do things the slightly harder way.
            // presume it IS in view, now and work backward
            bDrawLine = true;

            // if both ends of the line are on the outside of one of the walls of the visible
            // area, it can't possibly be onscrene. For instance, if both endpoints'
            // x-values are less than the wall's left side, it can't be on screen.
            if (drawStart.getX() < portalRect.getX() && drawEnd.getX() < portalRect.getX())
            {
                // the line segment is entirely on the left. don't draw it
                return;
            }
            if (drawStart.getX() > portalRect.getX() + portalRect.getWidth()
                && drawEnd.getX() > portalRect.getX() + portalRect.getWidth())
            {
                // the line segment is entirely on the right. don't draw it
                return;
            }
            if (drawStart.getY() < portalRect.getY() && drawEnd.getY() < portalRect.getY())
            {
                // the line segment is entirely above. don't draw it
                return;
            }
            if (drawStart.getY() > portalRect.getY() + portalRect.getHeight()
                && drawEnd.getY() > portalRect.getY() + portalRect.getHeight())
            {
                // the line segment is entirely below. don't draw it
                return;
            }

            // if we're here, it means the line segment:
            // 1) Has neither point within the rect
            // 2) Has points on both sides of a vertical or horizontal wall.

            // in some cases, there will be lines told to draw that didn't need to (if they
            // intersect
            // a vertical or horizontal line that is colinear with one of the edges of the viewable
            // rect).
            // but that inefficiency is preferable to the slope-intersection calculations needed to
            // check
            // to see if the line
        }

        if (!bDrawLine)
        {
            return;
        }

        g.setColor(m_color);

        int width = canvas.getLineStrokeWidth();
        int halfWidth = width / 2;

        int dx = Math.abs(m_end.x - m_start.x);
        int dy = Math.abs(m_end.y - m_start.y);

        int nudgeX = 0;
        int nudgeY = 0;
        if (dx > dy)
        {
            // vertical doubling
            nudgeY = 1;
        }
        else
        {
            // horizontal doubling
            nudgeX = 1;
        }

        int x1 = drawStart.x - nudgeX * halfWidth;
        int y1 = drawStart.y - nudgeY * halfWidth;
        int x2 = drawEnd.x - nudgeX * halfWidth;
        int y2 = drawEnd.y - nudgeY * halfWidth;

        for (int i = 0; i < width; i++)
        {
            g.drawLine(x1, y1, x2, y2);
            x1 += nudgeX;
            y1 += nudgeY;
            x2 += nudgeX;
            y2 += nudgeY;
        }
    }
}
