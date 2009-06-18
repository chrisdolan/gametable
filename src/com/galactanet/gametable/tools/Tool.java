/*
 * Tool.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.Graphics;
import java.util.List;

import com.galactanet.gametable.GametableCanvas;



/**
 * An interface for tools to be used on the map.
 * 
 * @author iffy
 */
public interface Tool
{
    int MODIFIER_ALT   = 0x02;
    int MODIFIER_CTRL  = 0x01;
    int MODIFIER_SHIFT = 0x04;
    int MODIFIER_SPACE = 0x08;

    /**
     * Called on the tool when the user makes it the active tool.
     */
    void activate(GametableCanvas canvas);

    /**
     * Called on the Tool when the user makes a different tool the active tool.
     */
    void deactivate();

    /**
     * Called when the tool action is cancelled. This should completely undo any state changes the tool may have made.
     * Internally or externally.
     * 
     */
    void endAction();

    /**
     * Called to retrieve any custom preferences specified by this tool.
     * 
     * @return a List of PreferenceDescriptors.
     */
    List getPreferences();

    /**
     * The tool should return true if the tool is in a mode where it is actively being used.
     */
    boolean isBeingUsed();

    /**
     * Called when the mouse button is pressed on the map.
     * 
     * @param x The x location of the mouse on the map when the button was pressed.
     * @param y The Y location of the mouse on the map when the button was pressed.
     * @param modifierMask The mask of modifier keys held during this event.
     */
    void mouseButtonPressed(int x, int y, int modifierMask);

    /**
     * Called when the mouse button is released on the map.
     * 
     * @param x The x location of the mouse on the map when the button was released.
     * @param y The Y location of the mouse on the map when the button was released.
     * @param modifierMask The mask of modifier keys held during this event.
     */
    void mouseButtonReleased(int x, int y, int modifierMask);

    /**
     * Called when the mouse is moved around on the map.
     * 
     * @param x The x location of the mouse on the map when the button was released.
     * @param y The Y location of the mouse on the map when the button was released.
     * @param modifierMask The mask of modifier keys held during this event.
     */
    void mouseMoved(int x, int y, int modifierMask);

    /**
     * Called after the canvas has been painted.
     * 
     * @param g Graphics context.
     */
    void paint(Graphics g);
}
