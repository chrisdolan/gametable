/*
 * AbstractTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Graphics;


/**
 * A basic non-functional implementation of Tool that other tools can subclass.
 * 
 * @author iffy
 */
public class AbstractTool implements Tool
{
    /**
     * Constructor.
     */
    public AbstractTool()
    {
    }

    // --- Tool Implementation ---

    /*
     * @see com.galactanet.gametable.Tool#activate(com.galactanet.gametable.GametableCanvas)
     */
    public void activate(GametableCanvas canvas)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#deactivate()
     */
    public void deactivate()
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(int x, int y)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
    }
}
