/*
 * GridMode.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import com.galactanet.gametable.util.UtilityFunctions;



/**
 * @author sephalon
 */
public class HexGridMode extends GridMode
{
    private final int[]   m_hexImageOffsets = new int[GametableCanvas.NUM_ZOOM_LEVELS];  // how far

    // data
    private final Image[] m_hexImages       = new Image[GametableCanvas.NUM_ZOOM_LEVELS]; // one hex

    public HexGridMode()
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

        int tilingSquareX = m_canvas.m_squareSize;
        final int tilingSquareY = m_canvas.m_squareSize;
        tilingSquareX *= 2;

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

        // draw a hex grid
        final Image toTile = m_hexImages[m_canvas.m_zoom];

        // this offsets the hexes to be "centered" in the square grid that would
        // be there if we were in square mode (Doing things this way means that the
        // x-position treatment of pogs doesn't have to change while in hex mode.
        final int offsetX = -m_hexImageOffsets[m_canvas.m_zoom] / 2;

        // each tiling of hex images is 4 hexes high. so we incrememt by 4
        for (int j = 0; j < hLines; j += 4)
        {
            // every "tiling" of the hex map draws 4 vertical rows of hexes
            // that works out to be 2 tileSquare sizes (cause each tileSquare width is
            // 2 columns of hexes.
            for (int i = 0; i < vLines; i += 2)
            {
                // the x value:
                // starts at the "linesXOffset" calculated at the top of this routine.
                // that represents the first vertical column of hexes visible on screen.
                // add to that i*m_squareSize, to offset horizontally as we traverse the loop.
                // then add our little offsetX, whose purpose is described in it's declaration.
                final int x = linesXOffset + i * tilingSquareX + offsetX;

                // the y location is much the same, except we need no x offset nudge.
                final int y = linesYOffset + j * tilingSquareY;
                g.drawImage(toTile, x, y, m_canvas);
            }
        }
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

    // overrides. We don't have the same scale in x as we do in y
    // the y is still 1.0, so we don't override it. But the X is a different story
    public double getDistanceMultplierX()
    {
        return 0.866;
    }

    public void init(final GametableCanvas canvas)
    {
        super.init(canvas);

        // init the hex images
        m_hexImages[0] = UtilityFunctions.getImage("assets/hexes_64.png");
        m_hexImages[1] = UtilityFunctions.getImage("assets/hexes_48.png");
        m_hexImages[2] = UtilityFunctions.getImage("assets/hexes_32.png");
        m_hexImages[3] = UtilityFunctions.getImage("assets/hexes_16.png");
        m_hexImages[4] = null; // no lines are drawn at this zoom level. So there's no hex image
        // for it.

        // magic numbers - these represent the distance in along the x-axis that the corner
        // of the hex is.
        // Note that the top left corner of the first hex is not aligned with the left of
        // the image
        // | ----------
        // | /
        // |/
        // |\
        // | \
        // | ----------
        // That distance is what is represented here.

        m_hexImageOffsets[0] = 19;
        m_hexImageOffsets[1] = 15;
        m_hexImageOffsets[2] = 10;
        m_hexImageOffsets[3] = 5;
        m_hexImageOffsets[4] = 0; // irrelevant. There is no image for this level. Lines aren't
        // drawn at this zoom level.
    }

    private boolean isOffsetColumn(final int col)
    {
        int columnNumber = col;
        if (columnNumber < 0)
        {
            columnNumber = -columnNumber;
        }
        if (columnNumber % 2 == 1)
        {
            return true;
        }
        return false;
    }

    private double pointDistance(final Point p1, final Point p2)
    {
        final int dx = p1.x - p2.x;
        final int dy = p1.y - p2.y;
        final double dist = Math.sqrt(dx * dx + dy * dy);
        return dist;
    }

    public Point snapPointEx(final Point modelPointIn, final boolean bSnapForPog, final int pogSize)
    {
        final Point modelPoint = new Point(modelPointIn);
        if (bSnapForPog)
        {
            // we're snapping for a pog. We've been sent the upper left corner of that
            // pog. We need to know it's center.
            modelPoint.x += pogSize / 2;
            modelPoint.y += pogSize / 2;
        }

        // in hex mode, we have to snap to any of the vertices of a hex,
        // plus the center. How annoying is that, eh?

        // start with the grid snap location for the x coordinate
        int x = getGridSnap(modelPoint.x);

        // from that, get the grid location.
        final int gridX = x / GametableCanvas.BASE_SQUARE_SIZE;

        // note that items in the odd columns are half a grid square down
        int offsetY = 0;
        if (isOffsetColumn(gridX))
        {
            offsetY = GametableCanvas.BASE_SQUARE_SIZE / 2;
        }

        // now work out which "grid" (hex, really) the y value is in
        int y = getGridSnap(modelPoint.y - offsetY);

        // add back the offset
        y += offsetY;

        // add in the x offset needed to put it on the corner of the hex
        x += m_hexImageOffsets[0] / 2; // [0] is the model coordinate size

        // let's number the hexagon points 0 through 5. Let's number them
        // clockwise starting from the upper left one. What we have done so
        // far is snap to the nearest point 0. That's not good enough.
        // There are 3 hexagon points adjacent to a point 0 that are not
        // other hexagon point 0's. And we might be closer to one of them
        // than to the point we just snapped to.
        // so we now have 4 "candidates" for nearest point. The point 0 we just
        // found, and the other three points nearby. Those other three points
        // will be:
        //
        // --Our hex's point 1
        // --Our hex's point 5
        // --Our upstairs neighbor's point 5
        //
        // In addition to that, there are 3 hex centers we need to check:
        // --Our hex center
        // --Our upstairs neighbor's hex center
        // -- Our neighbor to the left's hex center

        Point closest = null;

        if (bSnapForPog)
        {
            // we're snapping to valid pog locations. We have been sent the
            // upp left corner of the graphic. We converted that to the center already.
            // now, we need to stick that to either vertices or hex centers, depending
            // on the size of the pog. If it's a size 1 (64 px_ pog, we snap to centers only.
            // if it's size 2, we snap to vertices only. If size 3, back to centers. etc.
            final int face = pogSize / GametableCanvas.BASE_SQUARE_SIZE;
            if (face % 2 == 1)
            {
                // odd faces snap to centers
                final Point candidates[] = new Point[3];

                final Point point1 = new Point(x + GametableCanvas.BASE_SQUARE_SIZE - m_hexImageOffsets[0], y); // Our
                // hex's
                // point
                // 1,
                // for
                // use
                // in
                // calculating
                // the
                // center

                candidates[0] = new Point(x + (point1.x - x) / 2, y + GametableCanvas.BASE_SQUARE_SIZE / 2); // Our
                // hex
                // center
                candidates[1] = new Point(candidates[0].x, candidates[0].y - GametableCanvas.BASE_SQUARE_SIZE); // Our
                // upstairs
                // neighbor's
                // center
                candidates[2] = new Point(candidates[0].x - GametableCanvas.BASE_SQUARE_SIZE, candidates[0].y
                    - GametableCanvas.BASE_SQUARE_SIZE / 2); // Our upstairs neighbor's center

                closest = getClosestPoint(modelPoint, candidates);
            }
            else
            {
                // even faces snap to vertices
                final Point candidates[] = new Point[4];
                candidates[0] = new Point(x, y); // Our hex's point 0
                candidates[1] = new Point(x + GametableCanvas.BASE_SQUARE_SIZE - m_hexImageOffsets[0], y); // Our
                // hex's
                // point
                // 1
                candidates[2] = new Point(x - m_hexImageOffsets[0], y + GametableCanvas.BASE_SQUARE_SIZE / 2); // Our
                // hex's
                // point
                // 5
                candidates[3] = new Point(candidates[2].x, candidates[2].y - GametableCanvas.BASE_SQUARE_SIZE); // Our
                // upstairs
                // neighbor's
                // point
                // 5

                closest = getClosestPoint(modelPoint, candidates);
            }

            if (closest != null)
            {
                // offset the values for the pog size
                closest.x -= pogSize / 2;
                closest.y -= pogSize / 2;
            }
        }
        else
        {
            // we're snapping to any vertex
            final Point candidates[] = new Point[7];
            candidates[0] = new Point(x, y); // Our hex's point 0
            candidates[1] = new Point(x + GametableCanvas.BASE_SQUARE_SIZE - m_hexImageOffsets[0], y); // Our
            // hex's
            // point
            // 1
            candidates[2] = new Point(x - m_hexImageOffsets[0], y + GametableCanvas.BASE_SQUARE_SIZE / 2); // Our
            // hex's
            // point
            // 5
            candidates[3] = new Point(candidates[2].x, candidates[2].y - GametableCanvas.BASE_SQUARE_SIZE); // Our
            // upstairs
            // neighbor's
            // point
            // 5
            candidates[4] = new Point(candidates[0].x + (candidates[1].x - candidates[0].x) / 2, y
                + GametableCanvas.BASE_SQUARE_SIZE / 2); // Our hex center
            candidates[5] = new Point(candidates[4].x, candidates[4].y - GametableCanvas.BASE_SQUARE_SIZE); // Our
            // upstairs
            // neighbor's
            // center
            candidates[6] = new Point(candidates[4].x - GametableCanvas.BASE_SQUARE_SIZE, candidates[4].y
                - GametableCanvas.BASE_SQUARE_SIZE / 2); // Our
            // upstairs
            // neighbor's
            // center

            closest = getClosestPoint(modelPoint, candidates);
        }

        if (closest == null)
        {
            // uh... if we're here something went wrong
            // defensive coding, just return that nearest Point 0
            System.out.println("Error snapping to point");
            return new Point(x, y);
        }
        return closest;
    }
}
