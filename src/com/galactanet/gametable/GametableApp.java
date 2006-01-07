/*
 * GametableApp.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.UIManager;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class GametableApp
{
    public static final String   VERSION      = "Gametable v1.2-pre3";
    public static final String   SYS_LOG_FILE = "gt.sys.log";
    public static final String   NET_LOG_FILE = "gt.net.log";

    private static final boolean PACK_FRAME   = false;

    /**
     * Construct the application
     */
    public GametableApp(boolean host)
    {
        GametableFrame frame = new GametableFrame();
        if (host)
        {
            frame.host(true);
        }
        else
        {
            // Pack frames that have useful preferred size info, e.g. from their layout
            // Validate frames that have preset sizes
            if (PACK_FRAME)
            {
                frame.pack();
            }
            else
            {
                frame.validate();
            }

            if (!frame.m_bLoadedState)
            {
                // Center the frame
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension frameSize = frame.getSize();
                if (frameSize.height > screenSize.height)
                {
                    frameSize.height = screenSize.height;
                }

                if (frameSize.width > screenSize.width)
                {
                    frameSize.width = screenSize.width;
                }

                frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
            }
            frame.setVisible(true);
        }
    }

    /**
     * Main method
     * 
     * @param args String[]
     */
    static public void main(String[] args)
    {
        try
        {
            if (false)
            {
                Log.initializeLog(Log.SYS, System.out);
                Log.initializeLog(Log.NET, System.out);
                Log.log(Log.SYS, VERSION);
                new GametableApp(true);
            }
            else
            {
                Log.initializeLog(Log.SYS, SYS_LOG_FILE);
                Log.initializeLog(Log.NET, NET_LOG_FILE);
                Log.log(Log.SYS, VERSION);
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Toolkit.getDefaultToolkit().setDynamicLayout(true);
                new GametableApp(false);
            }
        }
        catch (Throwable t)
        {
            Log.log(Log.SYS, t);
        }
    }
}
