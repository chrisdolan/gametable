/*
 * AbstractTool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.Graphics;
import java.util.Collections;
import java.util.List;

import com.galactanet.gametable.GametableCanvas;
import com.galactanet.gametable.Tool;


/**
 * A basic non-functional implementation of Tool that other tools can subclass.
 * 
 * @author iffy
 */
public class NullTool implements Tool
{
    /**
     * Constructor.
     */
    public NullTool()
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
     * @see com.galactanet.gametable.Tool#isBeingUsed()
     */
    public boolean isBeingUsed()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * @see com.galactanet.gametable.Tool#mouseButtonPressed(int, int)
     */
    public void mouseButtonPressed(int x, int y, int modifierMask)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#mouseMoved(int, int)
     */
    public void mouseMoved(int x, int y, int modifierMask)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(int x, int y, int modifierMask)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#getPreferences()
     */
    public List getPreferences()
    {
        return Collections.EMPTY_LIST;
    }

}
