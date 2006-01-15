/*
 * GametableApp.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Toolkit;

import javax.swing.UIManager;
import java.lang.reflect.Method; 
//import java.util.Arrays; 
//import javax.swing.JOptionPane;


/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class GametableApp
{
    public static final String  VERSION      = "Gametable v1.2-pre4";
    private static final String SYS_LOG_FILE = "gt.sys.log";
    private static final String NET_LOG_FILE = "gt.net.log";

    /**
     * Main method
     * 
     * @param args String[]
     */
    static public void main(String[] args)
    {
        try
        {
            System.setProperty("swing.aatext","true");
            Log.initializeLog(Log.SYS, SYS_LOG_FILE);
            Log.initializeLog(Log.NET, NET_LOG_FILE);
            Log.log(Log.SYS, VERSION);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
            new GametableFrame().setVisible(true);
        }
        catch (Throwable t)
        {
            Log.log(Log.SYS, t);
        }
    }

    public static void launchBrowser(String url) 
    {
		String osName = System.getProperty("os.name");
		try 
		{
			if (osName.startsWith("Mac OS")) 
			{
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
				openURL.invoke(null, new Object[] {url});
			}
			else if (osName.startsWith("Windows"))
			{
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
			else 
			{ 
				//assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
				{
					if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
					{
						browser = browsers[count];
					}
				}
				if (browser == null)
				{
					throw new Exception("Could not find web browser");
				}
				else
				{
					Runtime.getRuntime().exec(new String[] {browser, url});
				}
			}
		}
		catch (Exception e) 
		{
		}
    }    
    
}
