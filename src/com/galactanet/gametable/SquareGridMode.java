/*
 * GridMode.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;



/**
 * 
 * @author sephalon
 */
public class SquareGridMode extends GridMode
{
    public SquareGridMode()
    {
    }

    // draws the lines to the canvas. Assumes there is a properly offset graphics object passed in
    public void drawLines(final Graphics g, final int topLeftX, final int topLeftY, final int width, final int height)
    {
        if (m_canvas.m_zoom == 4)
        {
            // we don't draw lines at the furthest zoom level
            return;
        }
        // This code works out which is the first square to draw on the visible
        // portion of the map, and how many to draw

        // we are "tiling" an image across the visible area. In the case of square mode,
        // we do this just by drawing lines. For hexes we have an actual image to tile.
        // A trick here we have to deal with is that hexes are not horizontally interchangeable
        // across one unit size. That is to say: If you shift a hex map over 1 hex width to the
        // left or right, it will not look the same as it used to. Because the hexes in row N
        // are 1/2 a hex higher than the nexes in row N-1 and row N+1. Because of this, when in hex
        // mode,
        // we have to make our "tiling square size" twice as wide.

        final int tilingSquareX = m_canvas.m_squareSize;
        final int tilingSquareY = m_canvas.m_squareSize;

        int qx = Math.abs(topLeftX) / tilingSquareX;
        if (topLeftX < 0)
        {
            qx++;
            qx = -qx;
        }

        int qy = Math.abs(topLeftY) / tilingSquareY;
        if (topLeftY < 0)
        {
            qy++;
            qy = -qy;
        }

        final int linesXOffset = qx * tilingSquareX;
        final int linesYOffset = qy * tilingSquareY;
        final int vLines = width / tilingSquareX + 2;
        final int hLines = height / tilingSquareY + 2;

        g.setColor(Color.GRAY);

        // draw a square grid
        if (m_canvas.m_zoom < 4)
        {
            for (int i = 0; i < vLines; i++)
            {
                g.drawLine(i * m_canvas.m_squareSize + linesXOffset, topLeftY,
                    i * m_canvas.m_squareSize + linesXOffset, height + topLeftY);
            }
            for (int i = 0; i < hLines; i++)
            {
                g.drawLine(topLeftX, i * m_canvas.m_squareSize + linesYOffset, width + topLeftX, i
                    * m_canvas.m_squareSize + linesYOffset);
            }
        }
    }

    public void init(final GametableCanvas canvas)
    {
        super.init(canvas);
    }

    public Point snapPointEx(final Point modelPointIn, final boolean bSnapForPog, final int pogSize)
    {
        int x = getGridSnap(modelPointIn.x);
        int y = getGridSnap(modelPointIn.y);
        // Use old behavior when we're dealing with a pog.
        if( bSnapForPog )
        {
            return new Point(x, y);
        }
        // Otherwise allow center snapping as well.
        Point closest = null;
        final Point candidates[] = new Point[5];
        int foo = GametableCanvas.BASE_SQUARE_SIZE/2;
        candidates[0] = new Point( x, y ); // Me
        candidates[1] = new Point( x-foo, y-foo ); // Nearby center
        candidates[2] = new Point( x+foo, y-foo ); // Nearby center
        candidates[3] = new Point( x-foo, y+foo ); // Nearby center
        candidates[4] = new Point( x+foo, y+foo ); // Nearby center
        closest = getClosestPoint(modelPointIn, candidates);
        if (closest == null)
        {
            System.out.println("Error snapping to point");
            return new Point(x, y);
        }
        return closest;
    }

    private double pointDistance(final Point p1, final Point p2)
    {
        final int dx = p1.x - p2.x;
        final int dy = p1.y - p2.y;
        final double dist = Math.sqrt(dx * dx + dy * dy);
        return dist;
    }

    private Point getClosestPoint(final Point target, final Point candidates[])
    {
        double minDist = -1.0;
        Point winner = null;
        for (int i = 0; i < candidates.length; i++)
        {
            final double distance = pointDistance(target, candidates[i]);
            if ((minDist == -1.0) || (distance < minDist))
            {
                minDist = distance;
                winner = candidates[i];
            }
        }

        return winner;
    }
}
