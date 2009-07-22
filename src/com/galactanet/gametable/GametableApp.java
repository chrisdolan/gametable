/*
 * GametableApp.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Toolkit;

import javax.swing.UIManager;



/**
 * Main class for the GametableApp. It provides the entry point for the execution of the application.
 *  * @author sephalon
 */
public class GametableApp
{
    /**
     * Name of the networking log file
     */
    private static final String NET_LOG_FILE  = "logs/gt.net.log";
    /**
     * Name of the play log file
     */
    private static final String PLAY_LOG_FILE = "logs/gt.play.html";
    /**
     * Name of the system log file
     */
    private static final String SYS_LOG_FILE  = "logs/gt.sys.log";
    /**
     * String to describe gametable's chat version
     */
    public static final String VERSION        = "Gametable 2.0.RC7";
    
    public static final String LANGUAGE       = "En";
    
    /**
     * Main method
     * This is the entry point of the application. 
     * 
     * @param args String[] Like every Java program, it receives parameters from the command line
     */
    static public void main(final String[] args)
    {
        try
        {
            System.setProperty("swing.aatext", "true");         // Set anti-aliasing to true
            System.setProperty("java.protocol.handler.pkgs", "com.galactanet.gametable.protocol"); // Register the package as a protocol handler
            Log.initializeLog(Log.SYS, SYS_LOG_FILE);           // Initialize system log
            Log.initializeLog(Log.NET, NET_LOG_FILE);           // Initialize network log
            Log.initializeLog(Log.PLAY, PLAY_LOG_FILE);         // Initialize play log
            Log.log(Log.SYS, VERSION);                          // Write the version name to the system log
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    // Set the Look and Feel
            Toolkit.getDefaultToolkit().setDynamicLayout(true); // Turns dynamic layout on
            new GametableFrame().setVisible(true);              // Creates an instance of the main UI object and shows it.
                                                                // The app won't end until the main frame is closed
        }
        catch (final Throwable t)
        {
            Log.log(Log.SYS, t); // Log any error into the system log
        }
    }
}
