/*
 * UtilityFunctions.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;



/**
 * A class full of various and sundry static utility functions.
 * 
 * @author sephalon
 */
public class UtilityFunctions
{
    private static final Random random              = getRandomInstance();

    /**
     * The PNG signature to verify PNG data with.
     */
    private static final byte[] PNG_SIGNATURE       = {
        (byte)(137 & 0xFF), 80, 78, 71, 13, 10, 26, 10
                                                    };

    public static final char    UNIVERSAL_SEPARATOR = '/';
    public static final char    LOCAL_SEPARATOR     = File.separatorChar;

    public final static int     NO                  = 0;
    public final static int     YES                 = 1;
    public final static int     CANCEL              = -1;

    
    public static int getRandom(int max)
    {
        return random.nextInt(max);
    }

    public static String normalizeName(String in)
    {
        in = in.trim();
        in = in.toLowerCase();
        int len = in.length();
        StringBuffer out = new StringBuffer(len);
        for (int i = 0; i < len; ++i)
        {
            char c = in.charAt(i);
            if (Character.isJavaIdentifierPart(c))
            {
                out.append(c);
            }
        }

        return out.toString();
    }

    public static byte[] loadFileToArray(String filename)
    {
        File file = new File(filename);
        return loadFileToArray(file);
    }

    public static byte[] loadFileToArray(File file)
    {
        if (!file.exists())
        {
            return null;
        }

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

    /**
     * Gets the canonical/absolute file of the given file.
     * 
     * @param file File to canonicalize.
     * @return Canonicalized file.
     */
    public static File getCanonicalFile(File file)
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException ioe)
        {
            return file.getAbsoluteFile();
        }
    }

    /**
     * Converts the filename to use UNIVERSAL_SEPERATOR.
     * 
     * @param path Path to canonicalize.
     * @return Canonicalized Path.
     */
    public static String getUniversalPath(String path)
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, size = path.length(); i < size; ++i)
        {
            char c = path.charAt(i);
            if (c == '/' || c == '\\')
            {
                buffer.append(UNIVERSAL_SEPARATOR);
            }
            else
            {
                buffer.append(c);
            }
        }

        return buffer.toString();
    }

    /**
     * Converts the filename to use File.seperatorChar.
     * 
     * @param path Path to canonicalize.
     * @return Canonicalized Path.
     */
    public static String getLocalPath(String path)
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, size = path.length(); i < size; ++i)
        {
            char c = path.charAt(i);
            if (c == '/' || c == '\\')
            {
                buffer.append(LOCAL_SEPARATOR);
            }
            else
            {
                buffer.append(c);
            }
        }

        return buffer.toString();
    }

    /**
     * Gets the child path relative to the parent path.
     * 
     * @param parent Parent path.
     * @param child Child path.
     * @return The relative path.
     */
    public static String getRelativePath(File parent, File child)
    {
        String parentPath = getLocalPath(getCanonicalFile(parent).getPath());
        if (parentPath.charAt(parentPath.length() - 1) != LOCAL_SEPARATOR)
        {
            parentPath = parentPath + LOCAL_SEPARATOR;
        }
        String childPath = getLocalPath(getCanonicalFile(child).getPath());

        return new String(childPath.substring(parentPath.length()));
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
     * Checks the given binary date to see if it is a valid PNG file. It does this by checking the PNG signature.
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

    /**
     * Simple image cache.
     */
    private static Map g_imageCache = new HashMap();

    /**
     * Gets an image, caching it if possible.
     * 
     * @param name Name of image to get.
     * @return Image retrieved, or null.
     */
    public static Image getCachedImage(String name)
    {
        Image image = (Image)g_imageCache.get(name);
        if (image == null)
        {
            image = getImage(name);
            if (image == null)
            {
                return null;
            }
            g_imageCache.put(name, image);
        }
        return image;
    }

    /**
     * Removes an image from the image cache.
     * 
     * @param name Name of the image to remove.
     */
    public static void removeCachedImage(String name)
    {
        g_imageCache.remove(name);
    }

    public static Image getImage(String name)
    {
        Image img = getImageFromJar(name);
        if (img == null)
        {
            // couldn't find it in the jar. Try the local directory
            img = loadAndWait(GametableFrame.getGametableFrame().getGametableCanvas(), name);
        }

        return img;
    }

    public static Image createDrawableImage(int width, int height)
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
            .createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }

    private static Image getImageFromJar(String name)
    {
        URL imageUrl = GametableFrame.getGametableFrame().getGametableCanvas().getClass().getResource("/" + name);
        if (imageUrl == null)
        {
            return null;
        }

        Image image = null;
        try
        {
            image = Toolkit.getDefaultToolkit().createImage(imageUrl);
            if (image == null)
            {
                return null;
            }

            MediaTracker tracker = new MediaTracker(GametableFrame.getGametableFrame().getGametableCanvas());
            tracker.addImage(image, 0);
            tracker.waitForAll();
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
            return null;
        }

        if (image.getWidth(null) < 1 || image.getHeight(null) < 1)
        {
            // Log.log(Log.SYS, "JAR invalid file? " + name + " " + image.getWidth(null) + " x " +
            // image.getHeight(null));
            return null;
        }

        return image;
    }

    private static Image loadAndWait(Component component, String name)
    {
        MediaTracker tracker = new MediaTracker(component);
        Image image = loadImage(name, tracker);

        if (image == null)
        {
            return null;
        }

        try
        {
            tracker.waitForAll(); // ignore exceptions
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
            return null;
        }

        if (image.getWidth(null) < 1 || image.getHeight(null) < 1)
        {
            // Log.log(Log.SYS, "FS invalid file? " + name + " " + image.getWidth(null) + " x " +
            // image.getHeight(null));
            return null;
        }

        tracker = null;
        return image;
    }

    public static Image getScaledInstance(Image image, float scale)
    {
        if (image == null)
        {
            return null;
        }

        waitForImage(image);

        int width = Math.round(image.getWidth(null) * scale);
        int height = Math.round(image.getHeight(null) * scale);

        return getScaledInstance(image, width, height);
    }

    public static Image getScaledInstance(Image image, int width, int height)
    {
        if (image == null)
        {
            return null;
        }

        // TODO: Option for SMOOTH vs FAST?
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        if (scaledImage == null)
        {
            return null;
        }

        waitForImage(scaledImage);

        return scaledImage;
    }

    private static void waitForImage(Image image)
    {
        MediaTracker tracker = new MediaTracker(GametableFrame.getGametableFrame());
        tracker.addImage(image, 0);
        try
        {
            tracker.waitForAll();
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
        }
    }

    private static Image loadImage(String name, MediaTracker tracker)
    {
        Image image = null;
        try
        {
            image = Toolkit.getDefaultToolkit().createImage(name);
            if (image == null)
            {
                return null;
            }
            tracker.addImage(image, 0);
        }
        catch (Exception e)
        {
            Log.log(Log.SYS, e);
        }
        return image;
    }

    public static String getLine(DataInputStream in)
    {
        try
        {
            boolean bFoundContent = false;
            StringBuffer buffer = new StringBuffer();
            int count = 0;
            while (true)
            {
                // ready an empty string. If we have tl leave early, we'll return an empty string

                char ch = (char)(in.readByte());
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
            Log.log(Log.SYS, e);
            return null;
        }
    }

    /**
     * @param line String to break into words.
     * @return An array of words found in the string.
     */
    public static String[] breakIntoWords(String line)
    {
        boolean bDone = false;
        List words = new ArrayList();
        int start = 0;
        int end;
        while (!bDone)
        {
            end = line.indexOf(" ", start);
            String newWord;
            if (end == -1)
            {
                bDone = true;
                newWord = line.substring(start);
                words.add(newWord);
            }
            else
            {
                newWord = line.substring(start, end);
                start = end + 1;
                words.add(newWord);
            }
        }

        if (words.size() == 0)
        {
            return null;
        }

        String[] ret = new String[words.size()];
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = (String)words.get(i);
        }

        return ret;
    }

    /**
     * @param component Component to get screen coordinates of.
     * @return The absolute screen coordinates of this component.
     */
    public static Point getScreenPosition(Component component)
    {
        Point retVal = new Point(component.getX(), component.getY());

        Container container = component.getParent();
        if (container != null)
        {
            Point parentPos = getScreenPosition(container);
            return new Point(retVal.x + parentPos.x, retVal.y + parentPos.y);
        }
        return retVal;
    }

    /**
     * @param component Component that componentPoint is relative to.
     * @param componentPoint Point to convert to screen coordinates, relative to the given component.
     * @return The screen-relative coordinates of componentPoint.
     */
    public static Point getScreenCoordinates(Component component, Point componentPoint)
    {
        Point screenPos = getScreenPosition(component);
        return new Point(componentPoint.x + screenPos.x, componentPoint.y + screenPos.y);
    }

    /**
     * @param component Component to get coordinates relative to.
     * @param screenPoint Screen-relative coordinates to convert.
     * @return Component-relative coordinates of the given screen coordinates.
     */
    public static Point getComponentCoordinates(Component component, Point screenPoint)
    {
        Point screenPos = getScreenPosition(component);
        return new Point(screenPoint.x - screenPos.x, screenPoint.y - screenPos.y);
    }

    /**
     * @return An instance of Random to use for all RNG.
     */
    private static Random getRandomInstance()
    {
        // SHA1PRNG
        Random rand;
        try
        {
            rand = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.log(Log.SYS, e);
            rand = new Random();
        }
        rand.setSeed(System.currentTimeMillis());
        return rand;
    }

    /**
     * Private constructor so no one can instantiate this.
     */
    private UtilityFunctions()
    {
        throw new RuntimeException("Do not do this.");
    }
}
