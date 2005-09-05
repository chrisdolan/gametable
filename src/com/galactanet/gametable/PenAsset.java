/*
 * PenAsset.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Color;
import java.awt.Point;
import java.util.Vector;


/**
 * TODO: comment
 *
 * @author sephalon
 */
public class PenAsset
{

    public final static int    MINIMUM_MOVE_DISTANCE = 1;
    public final static double WIGGLE_TOLERANCE      = 2.0;

    Vector                     m_points              = new Vector();
    GametableCanvas            m_canvas;
    Color                      m_color;



    public PenAsset()
    {
    }

    public void init(Color color)
    {
        m_color = color;
    }

    public Color getColor()
    {
        return m_color;
    }

    public void addPoint(int modelX, int modelY)
    {
        Point toAdd = new Point(modelX, modelY);

        if (m_points.size() > 0)
        {
            // only add it if it's a reasonable distance from the last one.
            Point lastPoint = (Point)m_points.elementAt(m_points.size() - 1);

            int dx = lastPoint.x - modelX;
            int dy = lastPoint.y - modelY;

            int distSq = dx * dx + dy * dy;
            if (distSq < MINIMUM_MOVE_DISTANCE * MINIMUM_MOVE_DISTANCE)
            {
                // they didn't move far enough to interest us.
                return;
            }
        }

        m_points.add(toAdd);
    }

    public LineSegment[] getLineSegments()
    {
        if (m_points.size() < 2)
        {
            return null;
        }

        LineSegment[] ret = new LineSegment[m_points.size() - 1];
        for (int i = 0; i < m_points.size() - 1; i++)
        {
            ret[i] = new LineSegment();
            Point start = (Point)m_points.elementAt(i);
            Point end = (Point)m_points.elementAt(i + 1);
            ret[i].init(start, end, m_color);
        }

        return ret;
    }

    public void smooth()
    {
        // System.out.println("before Smooth: "+m_points.size());
        // cull out unneeded points
        if (m_points.size() < 2)
        {
            return;
        }

        Vector newPoints = new Vector();
        newPoints.add(m_points.elementAt(0)); // no matter what, the first point will have to be
        // in there
        int currentIdx = 0;

        while (currentIdx < m_points.size() - 1) // keep going till we hit the end point
        {
            currentIdx = getNextUsefulPoint(currentIdx);
            newPoints.add(m_points.elementAt(currentIdx)); // no matter what, the first point will
            // have to be in there
        }

        // we're done.
        m_points = newPoints;
        // System.out.println("after Smooth: "+m_points.size());
    }

    protected int getNextUsefulPoint(int startIdx)
    {
        // starting from startIdx, look forward as many points as we can,
        // checking intervening points to see if they're close enough to be
        // on the line and thus ignored

        if (startIdx == m_points.size() - 1)
        {
            // this is the endpoint.
            return startIdx;
        }

        if (startIdx == m_points.size() - 1)
        {
            // this is the one before the endpoint. The endpoint
            // is invariable valid
            return m_points.size() - 1;
        }

        // work forward till you get an invalid one, then go back a step
        int lastGoodIdx = startIdx + 1;
        for (int i = startIdx + 2; i < m_points.size(); i++)
        {
            // check the points for valid lines
            if (pointsOutsideDirectLine(startIdx, i))
            {
                // we found a point outside the direct line. So
                // we're done with the search. we return the last good value
                return lastGoodIdx;
            }

            // there were no points outside the direct line. Carry on with the
            // next point
            lastGoodIdx = i;
        }

        // if we made it out of the loop, it means we got all the way to the
        // end point without failing. So the end point is the next useful point
        return m_points.size() - 1;
    }

    protected boolean pointsOutsideDirectLine(int startIdx, int endIdx)
    {
        Point checkStart = (Point)m_points.elementAt(startIdx);
        Point checkEnd = (Point)m_points.elementAt(endIdx);
        for (int i = startIdx + 1; i < endIdx; i++)
        {
            Point checkMiddle = (Point)m_points.elementAt(i);
            double dist = distanceToLine(checkStart, checkEnd, checkMiddle);
            if (dist > WIGGLE_TOLERANCE)
            {
                // we found a point that's outside the line
                return true;
            }
        }

        // if we're here, we must not have found any invalid points
        return false;
    }

    double distanceToLine(Point lineStart, Point lineEnd, Point point)
    {
        // zero everything. put lineStart at the origin
        Point vectorB = new Point();
        vectorB.x = lineEnd.x - lineStart.x;
        vectorB.y = lineEnd.y - lineStart.y;

        Point vectorA = new Point();
        vectorA.x = point.x - lineStart.x;
        vectorA.y = point.y - lineStart.y;

        // A dot B divided by the length of B is A's projection along B
        // in this case, A is the vector to the point
        double A_dot_B = vectorA.x * vectorB.x + vectorA.y * vectorB.y;
        double normB = Math.sqrt(vectorB.x * vectorB.x + vectorB.y * vectorB.y);
        double proj = A_dot_B / normB;

        // now we need to find the point that is "proj" along vector B
        double ratioProj = (proj / normB);
        double projPointX = ratioProj * vectorB.x;
        double projPointY = ratioProj * vectorB.y;

        // proj is the distance along vector B that A projects to. Now we
        // need to get the distance between it and the sent in point
        // (which is now vector A)
        double dx = projPointX - vectorA.x;
        double dy = projPointY - vectorA.y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        return dist;
    }
}
