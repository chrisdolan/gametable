/*
 * PogType.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.BitSet;
import java.util.Hashtable;
import javax.swing.ImageIcon;



/**
 * Class encapsulating all static data for a type of pog.
 * 
 * @author iffy
 */
public class PogType
{
    // --- Options ---------------------------------------------------------------------------------------------------
    public final static boolean FAST_SCALING = true;

    // --- Members ---------------------------------------------------------------------------------------------------

    public static boolean hasAlpha(final Image image)
    {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage)
        {
            final BufferedImage bimage = (BufferedImage)image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        final PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try
        {
            pg.grabPixels();
        }
        catch (final InterruptedException e)
        {
        }

        // Get the image's color model
        final ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }

    private static BufferedImage toBufferedImage(final Image image)
    {
        if (image instanceof BufferedImage)
        {
            return (BufferedImage)image;
        }

        Image outImage = image;

        // This code ensures that all the pixels in the image are loaded
        outImage = new ImageIcon(outImage).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        final boolean hasAlpha = hasAlpha(outImage);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try
        {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha)
            {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            final GraphicsDevice gs = ge.getDefaultScreenDevice();
            final GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(outImage.getWidth(null), outImage.getHeight(null), transparency);
        }
        catch (final HeadlessException e)
        {
            // The system does not have a screen
        }

        if (bimage == null)
        {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha)
            {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(outImage.getWidth(null), outImage.getHeight(null), type);
        }

        // Copy image to buffered image
        final Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(outImage, 0, 0, null);
        g.dispose();

        return bimage;
    }

    private final boolean   m_bUnderlay;
    private boolean         m_bUnknown;

    private int             m_faceSize;
    private final String    m_filename;
    private BitSet          m_hitMap;
    private final Hashtable m_iconcache = new Hashtable();
    private Image           m_image;
    private float           m_lastScale;

    // --- Constructors ----------------------------------------------------------------------------------------------

    private Image           m_lastScaledImage;

    // --- Methods ---------------------------------------------------------------------------------------------------

    private Image           m_listIcon;

    /**
     * Constructor.
     */
    public PogType(final String filename, final int reportedFaceSize, final boolean underlay)
    {
        m_filename = UtilityFunctions.getLocalPath(filename);
        m_bUnderlay = underlay;
        m_faceSize = reportedFaceSize;
        load();
    }

    /**
     * Draws the pog onto the given graphics context.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     */
    public void draw(final Graphics g, final int x, final int y, final double angle, final int flipH, final int flipV)
    {
        g.drawImage(rotate(flip(m_image, flipH, flipV), angle), x, y, null);
    }

    /**
     * Draws the pog onto the given graphics context in "ghostly" form.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     */
    public void drawGhostly(final Graphics g, final int x, final int y, final double angle, final int flipH, final int flipV)
    {
        drawTranslucent(g, x, y, 0.5f, angle, flipH, flipV);
    }

    /**
     * Draws the pog onto the given graphics context at the given scale.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     * @param scale What scale to draw the pog at.
     */
    public void drawListIcon(final Graphics g, final int x, final int y)
    {
        g.drawImage(getListIcon(), x, y, null);
    }

    /**
     * Draws the pog onto the given graphics context at the given scale.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     * @param scale What scale to draw the pog at.
     */
    public void drawScaled(final Graphics g, final int x, final int y, final float scale, final double angle, final int flipH, final int flipV)
    {
        if (FAST_SCALING)
        {
            final int drawWidth = Math.round(getWidth(angle) * scale);
            final int drawHeight = Math.round(getHeight(angle) * scale);
            if ((m_listIcon != null) && (drawWidth == m_listIcon.getWidth(null))
                && (drawHeight == m_listIcon.getHeight(null)))
            {
                drawListIcon(g, x, y);
            }
            else
            {
                g.drawImage(rotate(flip(m_image, flipH, flipV), angle), x, y, drawWidth, drawHeight, null);
            }
        }
        else
        {
            g.drawImage(rotate(flip(getScaledImage(scale), flipH, flipV), angle), x, y, null);
        }
    }

    /**
     * Draws a tint for the pog onto the given graphics context.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     * @param scale What scale to draw the pog at.
     * @param tint Color with which to tint the pog.
     */
    public void drawTint(final Graphics g, final int x, final int y, final float scale, final Color tint,
        final double angle)
    {
        final Graphics2D g2 = (Graphics2D)g.create();
        g2.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 0x7f));
        g2.fillRect(x, y, Math.round(getWidth(angle) * scale), Math.round(getHeight(angle) * scale));
        g2.dispose();
    }

    /**
     * Draws the pog onto the given graphics context at the given opacity.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     * @param opacity 0 for fully transparent - 1 for fully opaque.
     */
    public void drawTranslucent(final Graphics g, final int x, final int y, final float opacity, final double angle, final int flipH, final int flipV)
    {
        final Graphics2D g2 = (Graphics2D)g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        draw(g2, x, y, angle, flipH, flipV);
        g2.dispose();
    }

    /**
     * @return The face size in squares.
     */
    public int getFaceSize()
    {
        return m_faceSize;
    }

    /**
     * @return Returns the filename of this pog.
     */
    public String getFilename()
    {
        return m_filename;
    }

    /**
     * @return The native height of this pog.
     */
    public int getHeight(final double angle)
    {

        if (m_image == null)
        {
            return m_faceSize * GametableCanvas.BASE_SQUARE_SIZE;
        }

        if (angle == 0)
        {
            return m_image.getHeight(null);
        }
        Dimension bounds = (Dimension)(m_iconcache.get(Double.valueOf(angle)));
        if (bounds == null)
        {
            rotate(m_image, angle);
            bounds = (Dimension)(m_iconcache.get(Double.valueOf(angle)));
        }
        // If caching images instead of dimensions, this should be
        // return (int)(bounds.getHeight(null));
        return (int)(bounds.getHeight());
    }

    /**
     * @return A nice label for this pog.
     */
    public String getLabel()
    {
        String label = getFilename();
        final int start = label.lastIndexOf(UtilityFunctions.LOCAL_SEPARATOR) + 1;
        int end = label.lastIndexOf('.');
        if (end < 0)
        {
            end = label.length();
        }

        label = label.substring(start, end);

        return new String(label);
    }

    // --- Drawing Methods ---

    private Image getListIcon()
    {
        if (m_listIcon == null)
        {
            final int maxDim = Math.max(getWidth(0), getHeight(0));
            final float scale = PogPanel.POG_ICON_SIZE / (float)maxDim;
            m_listIcon = UtilityFunctions.getScaledInstance(m_image, scale);
        }
        return m_listIcon;
    }

    public int getListIconHeight()
    {
        return getListIcon().getHeight(null);
    }

    public int getListIconWidth()
    {
        return getListIcon().getWidth(null);
    }

    private Image getScaledImage(final float scale)
    {
        if (scale == 1.0)
        {
            return m_image;
        }

        if ((m_lastScaledImage == null) || (Math.round(m_lastScale * 100) != Math.round(scale * 100)))
        {
            // System.out.println(this + " scale: " + m_lastScale + " -> " + scale);
            m_lastScale = scale;
            m_lastScaledImage = UtilityFunctions.getScaledInstance(m_image, m_lastScale);
        }
        return m_lastScaledImage;
    }

    /**
     * @return The native width of this pog.
     */
    public int getWidth(final double angle)
    {
        if (m_image == null)
        {
            return m_faceSize * GametableCanvas.BASE_SQUARE_SIZE;
        }

        if (angle == 0)
        {
            return m_image.getWidth(null);
        }
        Dimension bounds = (Dimension)(m_iconcache.get(Double.valueOf(angle)));
        if (bounds == null)
        {
            rotate(m_image, angle);
            bounds = (Dimension)(m_iconcache.get(Double.valueOf(angle)));
        }
        // If caching images instead of dimensions, this should be
        // return (int)(bounds.getWidth(null));
        return (int)(bounds.getWidth());

    }

    /**
     * Initializes the hit map from the source image.
     */
    private void initializeHitMap(final double angle, final int flipH, final int flipV)
    // TODO get the hit detection to work
    {
        if ((m_image == null) || (getWidth(angle) < 0) || (getHeight(angle) < 0))
        {
            return;
        }

        final BufferedImage bufferedImage = new BufferedImage(getWidth(angle), getHeight(angle),
            BufferedImage.TYPE_INT_RGB);
        {
            final Graphics2D g = bufferedImage.createGraphics();
            g.setColor(new Color(0xff00ff));
            g.fillRect(0, 0, getWidth(angle), getHeight(angle));
            draw(g, 0, 0, angle, flipH, flipV);
            g.dispose();
        }

        final DataBuffer buffer = bufferedImage.getData().getDataBuffer();
        final int len = getWidth(angle) * getHeight(angle);
        m_hitMap = new BitSet(len);
        m_hitMap.clear();
        for (int i = 0; i < len; ++i)
        {
            final int pixel = buffer.getElem(i) & 0xFFFFFF;
            m_hitMap.set(i, (pixel != 0xFF00FF));
        }
    }

    // --- Object Implementation ---

    /**
     * @return True if this pog is an underlay.
     */
    public boolean isUnderlay()
    {
        return m_bUnderlay;
    }

    // --- Private Methods ---

    /**
     * @return True if this pog cannot find or load it's file.
     */
    public boolean isUnknown()
    {
        return m_bUnknown;
    }

    /**
     * Loads or reloads the pog.
     */
    public void load()
    {
        final Image oldImage = m_image;
        m_image = UtilityFunctions.getImage(m_filename);
        m_listIcon = null;
        m_lastScaledImage = null;
        if (m_image == null)
        {
            // the file doesn't exist. load up the a placeholder
            m_bUnknown = true;
            if (oldImage != null)
            {
                m_image = oldImage;
            }
            else
            {
                String fileToLoad = "assets/pog_unk_1.png";
                switch (m_faceSize)
                {
                    case 2:
                    {
                        fileToLoad = "assets/pog_unk_2.png";
                    }
                    break;

                    case 3:
                    {
                        fileToLoad = "assets/pog_unk_3.png";
                    }
                    break;
                }

                m_image = UtilityFunctions.getCachedImage(fileToLoad);
                if (m_faceSize > 3)
                {
                    final int size = m_faceSize * GametableCanvas.BASE_SQUARE_SIZE;
                    m_image = UtilityFunctions.getScaledInstance(m_image, size, size);
                }
            }
        }
        else
        {
            // File loaded okay, calculate facing
            m_bUnknown = false;
            final float realFaceSize = Math.max(m_image.getWidth(null), m_image.getHeight(null))
                / (float)GametableCanvas.BASE_SQUARE_SIZE;
            m_faceSize = (int)realFaceSize;
            if ((realFaceSize - (int)realFaceSize) > 0.0)
            {
                ++m_faceSize;
            }
        }

        if (m_faceSize < 1)
        {
            m_faceSize = 1;
        }

        // prepare the hit map
        initializeHitMap(0, 0, 0);
    }

    private Image rotate(final Image i, final double angle)
    {
        if (angle == 0)
        {
            return i;
            /*
             * This will speed up the program, but require more memory as it caches the image not dimensions, remove the
             * uncommented lines in the lower if. uncomment the commented line and change Dimension to Image in the
             * getHeight() and getWidth() methods to fully implement this if
             * (m_iconcache.containsKey(Double.valueOf(angle))) { return
             * (Image)(m_iconcache.get(Double.valueOf(angle))); }
             */
        }

        final BufferedImage bufferedImage = toBufferedImage(i);
        final AffineTransform tx = new AffineTransform();
        tx.rotate(Math.toRadians(angle), bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2);
        final AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        final Image returnedImage = Toolkit.getDefaultToolkit().createImage(op.filter(bufferedImage, null).getSource());

        if (!m_iconcache.containsKey(Double.valueOf(angle)))
        {
            final Dimension bounds = new Dimension(returnedImage.getWidth(null), returnedImage.getHeight(null));
            m_iconcache.put(Double.valueOf(angle), bounds);
            // m_iconcache.put(Double.valueOf(angle),returnedImage);
        }

        return returnedImage;
    }

    private Image flip(final Image i, final int flipH, final int flipV)
    {
        if (flipH == 0 && flipV == 0)
        {
            return i;
        }

        final BufferedImage bufferedImage = toBufferedImage(i);
        final AffineTransform tx = AffineTransform.getScaleInstance(flipH, flipV);
        tx.translate((flipH >= 0 ? 1 : (flipH * bufferedImage.getWidth())), (flipV >= 0 ? 1 : (flipV * bufferedImage.getHeight())));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        final Image returnedImage = Toolkit.getDefaultToolkit().createImage(op.filter(bufferedImage, null).getSource());

        if (!m_iconcache.containsKey(Double.valueOf(0)))
        {
            final Dimension bounds = new Dimension(returnedImage.getWidth(null), returnedImage.getHeight(null));
            m_iconcache.put(Double.valueOf(0), bounds);
            // m_iconcache.put(Double.valueOf(angle),returnedImage);
        }

        return returnedImage;
    }

    /**
     * Tests whether a point hits one of this pog's pixels.
     * 
     * @param x X coordinate of point to test relative to upper-left corner of pog.
     * @param y Y coordinate of point to test relative to upper-left corner of pog.
     * @return Returns true if the point hits this pog.
     */
    public boolean testHit(final int x, final int y, final double angle)
    {
        // if it's not in our rect, then forget it.
        if (x < 0)
        {
            return false;
        }

        if (x >= getWidth(angle))
        {
            return false;
        }

        if (y < 0)
        {
            return false;
        }

        if (y >= getHeight(angle))
        {
            return false;
        }

        // if we are unknown, then let's just go with it.
        if (m_hitMap == null)
        {
            initializeHitMap(angle, 0, 0);
            if (m_hitMap == null)
            {
                initializeHitMap(0, 0, 0);
                return true;
            }
        }

        // otherwise, let's see if they hit an actual pixel
        // TODO buggy?
        initializeHitMap(angle, 0, 0);
        final int idx = x + (y * getWidth(angle));
        final boolean value = m_hitMap.get(idx);
        initializeHitMap(0, 0, 0);
        return value;
    }

    /**
     * Tests whether a point hits one of this pog's pixels.
     * 
     * @param p Point to test, relative to upper-left corner of pog.
     * @return Returns true if the point hits this pog.
     */
    public boolean testHit(final Point p, final double angle)
    {
        return testHit(p.x, p.y, angle);
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[PogType@" + hashCode() + " name: " + m_filename + " size: " + m_faceSize + ", unknown: " + isUnknown()
            + "]";
    }
}
