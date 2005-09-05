/*
 * UtilityFunctions.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class UtilityFunctions
{
    /**
     * The PNG signature to verify PNG data with.
     */
    private static final byte[] PNG_SIGNATURE = {
        (byte)(137 & 0xFF), 80, 78, 71, 13, 10, 26, 10
                                              };

    public final static int     NO            = 0;
    public final static int     YES           = 1;
    public final static int     CANCEL        = -1;



    public static byte[] loadFileToArray(String filename)
    {
        File theFile = new File(filename);
        return loadFileToArray(theFile);
    }

    public static byte[] loadFileToArray(File file)
    {
        try
        {
            DataInputStream infile = new DataInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream fileData = new ByteArrayOutputStream();
            while (true)
            {
                int bytesRead = infile.read(buffer);
                if (bytesRead > 0)
                {
                    fileData.write(buffer, 0, bytesRead);
                }
                else
                {
                    break;
                }
            }
            return fileData.toByteArray();
        }
        catch (IOException ex)
        {
            Log.log(Log.SYS, ex);
            return null;
        }
    }

    public static File doFileOpenDialog(String title, String extension, boolean filterFiles)
    {
        JFileChooser chooser = new JFileChooser();

        prepareFileDialog(chooser, title, filterFiles, extension);

        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            lastDir = chooser.getSelectedFile().getParent();
            return chooser.getSelectedFile();
        }

        return null;
    }

    public static File doFileSaveDialog(String title, String extension, boolean filterFiles)
    {
        JFileChooser chooser = new JFileChooser();

        prepareFileDialog(chooser, title, filterFiles, extension);

        int returnVal = chooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            lastDir = chooser.getSelectedFile().getParent();
            return chooser.getSelectedFile();
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

    /**
     * Checks to see whether one file is an ancestor of another.
     * 
     * @param ancestor The potential ancestor File.
     * @param child The child file.
     * @return True if ancestor is an ancestor of child.
     */
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

    /**
     * Checks the given binary date to see if it is a valid PNG file. It does this by checking the
     * PNG signature.
     * 
     * @param data binary data to check
     * @return true if the binary data is a valid PNG file, false otherwise.
     */
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
            Log.log(Log.SYS, e);
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
            Log.log(Log.SYS, e);
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
