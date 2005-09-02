

package com.galactanet.gametable;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.UIManager;


public class GametableApp
{
    boolean packFrame = false;

    /**
     * Construct the application
     */
    public GametableApp()
    {
        GametableFrame frame = new GametableFrame();

        // Pack frames that have useful preferred size info, e.g. from their layout
        // Validate frames that have preset sizes
        if (packFrame)
            frame.pack();
        else
            frame.validate();

        if (!frame.m_bLoadedState)
        {
            // Center the frame
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = frame.getSize();
            if (frameSize.height > screenSize.height)
                frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width)
                frameSize.width = screenSize.width;
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
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
            new GametableApp();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
