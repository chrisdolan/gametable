/*
 * GameTableApplet.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.applet;

import java.applet.Applet;

import com.galactanet.gametable.GametableFrame;

/**
 * Web-hosted version of GameTable.
 */
public final class GameTableApplet extends Applet
{
    public void init()
    {
        GametableFrame frame = new GametableFrame();
        frame.setVisible(true);
    }
}
