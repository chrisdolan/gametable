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
    public void activate(final GametableCanvas canvas)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#deactivate()
     */
    public void deactivate()
    {
    }

    public void endAction()
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#getPreferences()
     */
    public List getPreferences()
    {
        return Collections.EMPTY_LIST;
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
    public void mouseButtonPressed(final int x, final int y, final int modifierMask)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#mouseButtonReleased(int, int)
     */
    public void mouseButtonReleased(final int x, final int y, final int modifierMask)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#mouseMoved(int, int)
     */
    public void mouseMoved(final int x, final int y, final int modifierMask)
    {
    }

    /*
     * @see com.galactanet.gametable.Tool#paint(java.awt.Graphics)
     */
    public void paint(final Graphics g)
    {
    }

}
