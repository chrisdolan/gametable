

package com.galactanet.gametable;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class UtilityFunctions
{

    public final static int NO     = 0;
    public final static int YES    = 1;
    public final static int CANCEL = -1;



    public static String doFileOpenDialog(String title, String extension, boolean filterFiles)
    {
        JFileChooser chooser = new JFileChooser();

        prepareFileDialog(chooser, title, filterFiles, extension);

        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            lastDir = chooser.getSelectedFile().getParent();
            return chooser.getSelectedFile().getPath();
        }

        return null;
    }

    public static String doFileSaveDialog(String title, String extension, boolean filterFiles)
    {
        JFileChooser chooser = new JFileChooser();

        prepareFileDialog(chooser, title, filterFiles, extension);

        int returnVal = chooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            lastDir = chooser.getSelectedFile().getParent();
            return chooser.getSelectedFile().getName();
        }

        return null;
    }

    static private void prepareFileDialog(JFileChooser chooser, String title, boolean filter, final String extension)
    {
        if (lastDir != null)
        {
            chooser.setCurrentDirectory(new File(lastDir));
        }

        chooser.setDialogTitle(title);

        if (filter)
        {
            chooser.setFileFilter(new javax.swing.filechooser.FileFilter()
            {
                public boolean accept(File file)
                {
                    if (file.getName().endsWith(extension) || file.isDirectory())
                    {
                        return true;
                    }
                    return false;
                }

                public String getDescription()
                {
                    return (extension + " files");
                }
            });
        }

    }



    static private String lastDir = null;



    public static int yesNoCancelDialog(Component parent, String msg, String title)
    {
        int ret = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
        switch (ret)
        {
            case JOptionPane.YES_OPTION:
            {
                return YES;
            }
            case JOptionPane.NO_OPTION:
            {
                return NO;
            }
            default:
            {
                return CANCEL;
            }
        }
    }

    public static int yesNoDialog(Component parent, String msg, String title)
    {
        int ret = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_OPTION);
        switch (ret)
        {
            default:
            {
                return YES;
            }
            case JOptionPane.NO_OPTION:
            {
                return NO;
            }
        }
    }

    public static void msgBox(Component parent, String msg)
    {
        msgBox(parent, msg, "Error!");
    }

    public static void msgBox(Component parent, String msg, String title)
    {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // naming policy for cuts, comps and anims
    public static boolean isValidName(String name)
    {
        if (name == null)
        {
            return false;
        }

        if (name.length() == 0)
        {
            return false;
        }

        // check character validity
        for (int i = 0; i < name.length(); i++)
        {
            char curChar = name.charAt(i);
            if (!(((curChar >= 'A') && (curChar <= 'Z')) || ((curChar >= '0') && (curChar <= '9')) || curChar == '_'))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean isAncestorFile(File ancestor, File child)
    {
        File parent = child.getParentFile();
        if (parent == null)
        {
            return false;
        }
        
        if (parent.equals(ancestor))
        {
            return true;
        }

        boolean b = isAncestorFile(ancestor, parent);
        return b;
    }

    private static final byte[] PNG_SIGNATURE = 
    {
        (byte)(137 & 0xFF), 80, 78, 71, 13, 10, 26, 10
    };
    
    public static boolean isPngData(byte[] data)
    {
        for (int i = 0; i < PNG_SIGNATURE.length; i++)
        {
            if (data[i] != PNG_SIGNATURE[i])
            {
                return false;
            }
        }

        return true;
    }

    public static Image getImage(String name)
    {
        Image img = getImageFromJar(name);
        if (img == null)
        {
            // couldn't find it in the jar. Try the local directory
            img = loadAndWait(GametableFrame.getGametableFrame().m_gametableCanvas, name);
        }
        return img;
    }

    private static Image getImageFromJar(String name)
    {
        URL imgURL = GametableFrame.getGametableFrame().m_gametableCanvas.getClass().getResource("/" + name);
        if (imgURL == null)
        {
            return null;
        }

        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = null;
        try
        {
            MediaTracker m = new MediaTracker(GametableFrame.getGametableFrame().m_gametableCanvas);
            img = tk.getImage(imgURL);
            m.addImage(img, 0);
            m.waitForAll();
        }
        catch (Exception e)
        {
        }

        return img;
    }

    private static Image loadAndWait(Component comp, String strName)
    {
        MediaTracker pMT = new MediaTracker(comp);
        Image pImage = loadImage(strName, pMT);
        try
        {
            pMT.waitForID(0); // ignore exceptions
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        pMT = null;
        return pImage;
    }

    private static Image loadImage(String strName, MediaTracker pMT)
    {
        Image pImage = null;
        try
        {
            pImage = Toolkit.getDefaultToolkit().getImage(strName);
            pMT.addImage(pImage, 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return pImage;
    }

    static public String getLine(DataInputStream ds)
    {
        try
        {
            boolean bFoundContent = false;
            StringBuffer buffer = new StringBuffer();
            int count = 0;
            while (true)
            {
                // ready an empty string. If we have tl leave early, we'll return an empty string

                char ch = (char)(ds.readByte());
                if (ch == '\r' || ch == '\n')
                {
                    // if it's just a blank line, then press on
                    // but if we've already had valid characters,
                    // then don't
                    if (bFoundContent)
                    {
                        // it's the end of the line!
                        return buffer.toString();
                    }
                }
                else
                {
                    // it's a non-CR character.
                    bFoundContent = true;
                    buffer.append(ch);
                }

                count++;
                if (count > 300)
                {
                    return buffer.toString();
                }
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }

}
