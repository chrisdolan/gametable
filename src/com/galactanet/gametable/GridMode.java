/*
 * GridMode.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Graphics;
import java.awt.Point;



/**
 * @author sephalon
 */
public class GridMode
{
    protected GametableCanvas m_canvas;

    public GridMode()
    {
    }

    // draws the lines to the canvas. Assumes there is a properly offset graphics object passed in
    public void drawLines(final Graphics g, final int topLeftX, final int topLeftY, final int width, final int height)
    {
        // default behavior is to not draw anything
    }

    public double getDistance(final int x1, final int y1, final int x2, final int y2)
    {
        final int dx = x2 - x1;
        final int dy = y2 - y1;

        double dxSquared = dx * dx;
        double dySquared = dy * dy;

        final double multX = m_canvas.getGridMode().getDistanceMultplierX();
        final double multY = m_canvas.getGridMode().getDistanceMultplierY();

        // we've squared the dx and dy already, so we need to apply the multX and multY squared
        dxSquared = dxSquared * multX * multX;
        dySquared = dySquared * multY * multY;

        final double dist = Math.sqrt(dxSquared + dySquared);

        return dist;
    }

    // some grids might not have the same scale in the x direction as they do in the y
    public double getDistanceMultplierX()
    {
        return 1.0;
    }

    public double getDistanceMultplierY()
    {
        return 1.0;
    }

    // gets the grid for this element to snap to
    protected int getGridSnap(final int i)
    {
        if (i < 0)
        {
            return ((i - GametableCanvas.BASE_SQUARE_SIZE / 2) / GametableCanvas.BASE_SQUARE_SIZE)
                * GametableCanvas.BASE_SQUARE_SIZE;
        }
        return ((i + GametableCanvas.BASE_SQUARE_SIZE / 2) / GametableCanvas.BASE_SQUARE_SIZE)
            * GametableCanvas.BASE_SQUARE_SIZE;
    }

    public void init(final GametableCanvas canvas)
    {
        m_canvas = canvas;
    }

    public void snapPogToGrid(final Pog pog)
    {
        // int squareSize = GametableCanvas.BASE_SQUARE_SIZE * pog.getFaceSize();
        final Point snappedPoint = snapPointEx(new Point(pog.getX(), pog.getY()), true,
            GametableCanvas.BASE_SQUARE_SIZE * pog.getFaceSize());

        /*
         * if (pog.getWidth() < squareSize) { snappedPoint.x += (squareSize - pog.getWidth()) / 2; }
         * 
         * if (pog.getHeight() < squareSize) { snappedPoint.y += (squareSize - pog.getHeight()) / 2; }
         */
        pog.setPosition(snappedPoint.x, snappedPoint.y);
    }

    /** ******************** HELPER FUNCTIONS AND REDIRECTS ********************* */
    // returns where this point will snap to
    public Point snapPoint(final Point modelPoint)
    {
        return snapPointEx(modelPoint, false, 0);
    }

    /** ******************** FUNCTIONS TO OVERRIDE ********************* */
    // if bSnapForPog is true, it will return snap locations where a pog of
    // the sent in size could snap to. Note this is not the same as ANY
    // snap points, cause you don't want your pogs snapping to the
    // vertex of a hex.
    // pogSize is ignored if bSnapForPog is false. And it's expected to be the
    // pog's size in model coordinate pixels.
    public Point snapPointEx(final Point modelPointIn, final boolean bSnapForPog, final int pogSize)
    {
        // default behavior is to not snap at all.
        return new Point(modelPointIn);
    }
}
