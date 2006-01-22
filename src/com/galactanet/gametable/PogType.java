/*
 * PogType.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.BitSet;



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

    private Image               m_image;
    private Image               m_listIcon;
    private Image               m_lastScaledImage;
    private float               m_lastScale;

    private BitSet              m_hitMap;
    private int                 m_faceSize;
    private String              m_filename;
    private boolean             m_bUnderlay;
    private boolean             m_bUnknown;

    // --- Constructors ----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public PogType(String filename, int reportedFaceSize, boolean underlay)
    {
        m_filename = UtilityFunctions.getLocalPath(filename);
        m_bUnderlay = underlay;
        m_faceSize = reportedFaceSize;
        load();
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * Loads or reloads the pog.
     */
    public void load()
    {
        Image oldImage = m_image;
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
                    int size = m_faceSize * GametableCanvas.BASE_SQUARE_SIZE;
                    m_image = UtilityFunctions.getScaledInstance(m_image, size, size);
                }
            }
        }
        else
        {
            // File loaded okay, calculate facing
            m_bUnknown = false;
            float realFaceSize = Math.max(m_image.getWidth(null), m_image.getHeight(null))
                / (float)GametableCanvas.BASE_SQUARE_SIZE;
            m_faceSize = (int)realFaceSize;
            if ((realFaceSize - (int)realFaceSize) > 0)
            {
                ++m_faceSize;
            }
        }

        if (m_faceSize < 1)
        {
            m_faceSize = 1;
        }

        // prepare the hit map
        initializeHitMap();
    }

    /**
     * @return True if this pog cannot find or load it's file.
     */
    public boolean isUnknown()
    {
        return m_bUnknown;
    }

    /**
     * @return The native width of this pog.
     */
    public int getWidth()
    {
        if (m_image == null)
        {
            return m_faceSize * GametableCanvas.BASE_SQUARE_SIZE;
        }
        return m_image.getWidth(null);
    }

    /**
     * @return The native height of this pog.
     */
    public int getHeight()
    {
        if (m_image == null)
        {
            return m_faceSize * GametableCanvas.BASE_SQUARE_SIZE;
        }
        return m_image.getHeight(null);
    }

    public int getListIconWidth()
    {
        return getListIcon().getWidth(null);
    }

    public int getListIconHeight()
    {
        return getListIcon().getHeight(null);
    }

    /**
     * @return True if this pog is an underlay.
     */
    public boolean isUnderlay()
    {
        return m_bUnderlay;
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
     * @return A nice label for this pog.
     */
    public String getLabel()
    {
        String label = getFilename();
        int start = label.lastIndexOf(UtilityFunctions.LOCAL_SEPARATOR) + 1;
        int end = label.lastIndexOf('.');
        if (end < 0)
        {
            end = label.length();
        }

        label = label.substring(start, end);

        return new String(label);
    }

    /**
     * Tests whether a point hits one of this pog's pixels.
     * 
     * @param p Point to test, relative to upper-left corner of pog.
     * @return Returns true if the point hits this pog.
     */
    public boolean testHit(Point p)
    {
        return testHit(p.x, p.y);
    }

    /**
     * Tests whether a point hits one of this pog's pixels.
     * 
     * @param x X coordinate of point to test relative to upper-left corner of pog.
     * @param y Y coordinate of point to test relative to upper-left corner of pog.
     * @return Returns true if the point hits this pog.
     */
    public boolean testHit(int x, int y)
    {
        // if it's not in our rect, then forget it.
        if (x < 0)
        {
            return false;
        }

        if (x >= getWidth())
        {
            return false;
        }

        if (y < 0)
        {
            return false;
        }

        if (y >= getHeight())
        {
            return false;
        }

        // if we are unknown, then let's just go with it.
        if (m_hitMap == null)
        {
            initializeHitMap();
            if (m_hitMap == null)
            {
                return true;
            }
        }

        // otherwise, let's see if they hit an actual pixel
        int idx = x + (y * getWidth());
        return m_hitMap.get(idx);
    }

    // --- Drawing Methods ---

    /**
     * Draws the pog onto the given graphics context.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     */
    public void draw(Graphics g, int x, int y)
    {
        g.drawImage(m_image, x, y, null);
    }

    /**
     * Draws the pog onto the given graphics context at the given scale.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     * @param scale What scale to draw the pog at.
     */
    public void drawScaled(Graphics g, int x, int y, float scale)
    {
        if (FAST_SCALING)
        {
            int drawWidth = Math.round(getWidth() * scale);
            int drawHeight = Math.round(getHeight() * scale);
            g.drawImage(m_image, x, y, drawWidth, drawHeight, null);
        }
        else
        {
            g.drawImage(getScaledImage(scale), x, y, null);
        }
    }

    /**
     * Draws the pog onto the given graphics context at the given scale.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     * @param scale What scale to draw the pog at.
     */
    public void drawListIcon(Graphics g, int x, int y)
    {
        g.drawImage(getListIcon(), x, y, null);
    }

    /**
     * Draws the pog onto the given graphics context in "ghostly" form.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     */
    public void drawGhostly(Graphics g, int x, int y)
    {
        drawTranslucent(g, x, y, 0.5f);
    }

    /**
     * Draws the pog onto the given graphics context at the given opacity.
     * 
     * @param g Context to draw onto.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     * @param opacity 0 for fully transparent - 1 for fully opaque.
     */
    public void drawTranslucent(Graphics g, int x, int y, float opacity)
    {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        draw(g2, x, y);
        g2.dispose();
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
    public void drawTint(Graphics g, int x, int y, float scale, Color tint)
    {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 0x7f));
        g2.fillRect(x, y, Math.round(getWidth() * scale), Math.round(getHeight() * scale));
        g2.dispose();
    }

    // --- Object Implementation ---

    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[PogType@" + hashCode() + " name: " + m_filename + " size: " + m_faceSize + ", unknown: " + isUnknown()
            + "]";
    }

    // --- Private Methods ---

    private Image getListIcon()
    {
        if (m_listIcon == null)
        {
            int maxDim = Math.max(getWidth(), getHeight());
            float scale = PogPanel.POG_ICON_SIZE / (float)maxDim;
            m_listIcon = UtilityFunctions.getScaledInstance(m_image, scale);
        }
        return m_listIcon;
    }

    private Image getScaledImage(float scale)
    {
        if (scale == 1.0)
        {
            return m_image;
        }

        if (m_lastScaledImage == null || Math.round(m_lastScale * 100) != Math.round(scale * 100))
        {
            // System.out.println(this + " scale: " + m_lastScale + " -> " + scale);
            m_lastScale = scale;
            m_lastScaledImage = UtilityFunctions.getScaledInstance(m_image, m_lastScale);
        }
        return m_lastScaledImage;
    }

    /**
     * Initializes the hit map from the source image.
     */
    private void initializeHitMap()
    {
        if (m_image == null || getWidth() < 0 || getHeight() < 0)
        {
            return;
        }

        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        {
            Graphics2D g = bufferedImage.createGraphics();
            g.setColor(new Color(0xff00ff));
            g.fillRect(0, 0, getWidth(), getHeight());
            draw(g, 0, 0);
            g.dispose();
        }

        DataBuffer buffer = bufferedImage.getData().getDataBuffer();
        int len = getWidth() * getHeight();
        m_hitMap = new BitSet(len);
        m_hitMap.clear();
        for (int i = 0; i < len; ++i)
        {
            int pixel = buffer.getElem(i) & 0xFFFFFF;
            m_hitMap.set(i, (pixel != 0xFF00FF));
        }
    }
}
