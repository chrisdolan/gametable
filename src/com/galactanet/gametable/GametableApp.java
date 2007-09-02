/*
 * GametableApp.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Toolkit;

import javax.swing.UIManager;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class GametableApp
{
    private static final String NET_LOG_FILE  = "gt.net.log";
    private static final String PLAY_LOG_FILE = "gt.play.html";
    private static final String SYS_LOG_FILE  = "gt.sys.log";
    public static final String  VERSION       = "Gametable v1.3 pre-1";

    /**
     * Main method
     * 
     * @param args String[]
     */
    static public void main(final String[] args)
    {
        try
        {
            System.setProperty("swing.aatext", "true");
            System.setProperty("java.protocol.handler.pkgs", "com.galactanet.gametable.protocol");
            Log.initializeLog(Log.SYS, SYS_LOG_FILE);
            Log.initializeLog(Log.NET, NET_LOG_FILE);
            Log.initializeLog(Log.PLAY, PLAY_LOG_FILE);
            Log.log(Log.SYS, VERSION);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
            new GametableFrame().setVisible(true);
        }
        catch (final Throwable t)
        {
            Log.log(Log.SYS, t);
        }
    }
}
