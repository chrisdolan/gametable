/*
 * Tool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Graphics;

/**
 * An interface for tools to be used on the map.
 * 
 * @author iffy
 */
public interface Tool
{
    /**
     * Called on the tool when the user makes it the active tool.
     */
    void activate(GametableCanvas canvas);

    /**
     * Called on the Tool when the user makes a different tool the active tool.
     */
    void deactivate();

    /**
     * Called when the mouse button is pressed on the map.
     * 
     * @param x The x location of the mouse on the map when the button was pressed.
     * @param y The Y location of the mouse on the map when the button was pressed.
     */
    void mouseButtonPressed(int x, int y);

    /**
     * Called when the mouse button is released on the map.
     * 
     * @param x The x location of the mouse on the map when the button was released.
     * @param y The Y location of the mouse on the map when the button was released.
     */
    void mouseButtonReleased(int x, int y);

    /**
     * Called when the mouse is moved around on the map.
     * 
     * @param x The x location of the mouse on the map when the button was released.
     * @param y The Y location of the mouse on the map when the button was released.
     */
    void mouseMoved(int x, int y);
    
    /**
     * Called after the canvas has been painted. 
     * 
     * @param g Graphics context.
     */
    void paint(Graphics g);
}
