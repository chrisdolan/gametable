/*
 * GametableApp.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Properties;

import javax.swing.UIManager;


/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class GametableApp
{
    public static final String   VERSION      = "Gametable v1.1";
    public static final String   SYS_LOG_FILE = "gt.sys.log";
    public static final String   NET_LOG_FILE = "gt.net.log";

    private static final boolean PACK_FRAME   = false;



    /**
     * Construct the application
     */
    public GametableApp()
    {
        GametableFrame frame = new GametableFrame();

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

    /**
     * Main method
     * 
     * @param args String[]
     */
    static public void main(String[] args)
    {
        Properties props = new Properties(System.getProperties());
        //props.setProperty("sun.java2d.opengl", "True");
        System.setProperties(props);

        Log.initializeLog(Log.SYS, SYS_LOG_FILE);
        Log.initializeLog(Log.NET, NET_LOG_FILE);
        Log.log(Log.SYS, VERSION);
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
            new GametableApp();
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
        }
    }
}
