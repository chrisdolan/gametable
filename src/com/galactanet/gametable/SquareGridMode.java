/*
 * GridMode.java: GameTable is in the Public Domain.
 */

package com.galactanet.gametable;

import java.awt.*;

/**
 * 
 * @author sephalon
 */
public class SquareGridMode extends GridMode
{
	public SquareGridMode()
	{
	}
	
	public void init(GametableCanvas canvas)
	{
		super.init(canvas);
	}

    public Point snapPointEx(Point modelPointIn, boolean bSnapForPog, int pogSize)
    {
        // snapping for a pog or not is irrelevant in square mode.
        int x = getGridSnap(modelPointIn.x);
        int y = getGridSnap(modelPointIn.y);
        return new Point(x, y);
    }
    
    // draws the lines to the canvas. Assumes there is a properly offset graphics object passed in 
    public void drawLines(Graphics g, int topLeftX, int topLeftY, int width, int height)
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
        int tilingSquareY = m_canvas.m_squareSize;

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

        int linesXOffset = qx * tilingSquareX;
        int linesYOffset = qy * tilingSquareY;
        int vLines = width / tilingSquareX + 2;
        int hLines = height / tilingSquareY + 2;

        g.setColor(Color.GRAY);

        // draw a square grid
        if (m_canvas.m_zoom < 4)
        {
            for (int i = 0; i < vLines; i++)
            {
                g.drawLine(i * m_canvas.m_squareSize + linesXOffset, topLeftY, i * m_canvas.m_squareSize + linesXOffset, height
                    + topLeftY);
            }
            for (int i = 0; i < hLines; i++)
            {
                g.drawLine(topLeftX, i * m_canvas.m_squareSize + linesYOffset, width + topLeftX, i * m_canvas.m_squareSize
                    + linesYOffset);
            }
        }
    }
}
