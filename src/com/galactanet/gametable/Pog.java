/*
 * Pog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;



/**
 * Represents an instance of a PogType on the Map.
 * 
 * @author sephalon
 */
public class Pog
{
    // --- Constants -------------------------------------------------------------------------------------------------

    /**
     * Background color for pog text.
     */
    private static final Color COLOR_BACKGROUND           = new Color(255, 255, 64, 192);

    /**
     * Background color for pog text.
     */
    private static final Color COLOR_ATTRIBUTE_BACKGROUND = new Color(64, 255, 64, 192);

    /**
     * Background color for changed pog text.
     */
    private static final Color COLOR_CHANGED_BACKGROUND   = new Color(238, 156, 0, 192);

    /**
     * Font to use for displaying pog text.
     */
    private static final Font  FONT_TEXT                  = Font.decode("sansserif-bold-12");

    /**
     * Font to use for displaying attribute names.
     */
    private static final Font  FONT_ATTRIBUTE_NAME        = Font.decode("sansserif-bold-12");

    /**
     * Font to use for displaying attribute names.
     */
    private static final Font  FONT_ATTRIBUTE_VALUE       = Font.decode("sansserif-12");

    // --- Types -----------------------------------------------------------------------------------------------------

    private static class Attribute
    {
        public String name;
        public String value;

        public Attribute(String n, String v)
        {
            name = n;
            value = v;
        }
    }

    // --- Static Members --------------------------------------------------------------------------------------------

    /**
     * Unique global Id for pogs.
     */
    public static int       g_nextId               = 10;

    // --- Members ---------------------------------------------------------------------------------------------------

    /**
     * The PogType of this Pog.
     */
    private PogType         m_pogType;

    /**
     * Lame handle to canvas.
     */
    private GametableCanvas m_canvas;

    /**
     * Position of the pog on the map in map coordinates.
     */
    private Point           m_position             = new Point(0, 0);

    /**
     * The primary label for the Pog.
     */
    private String          m_text                 = "";

    /**
     * The unique id of this pog.
     */
    private int             m_id                   = 0;

    /**
     * Scale for this pog.
     */
    private float           m_scale                = 1f;

    /**
     * Is this pog tinted?
     */
    private boolean         m_bTinted              = false;

    /**
     * True if this pog is notifying the world that it's text had changed.
     */
    private boolean         m_bTextChangeNotifying = false;

    /**
     * Name/value pairs of the attributes assigned to this pog.
     */
    private Map             m_attributes           = new TreeMap();

    // --- Constructors ----------------------------------------------------------------------------------------------

    public Pog(DataInputStream dis) throws IOException
    {
        initFromPacket(dis);
    }

    public Pog(PogType type)
    {
        init(GametableFrame.getGametableFrame().getGametableCanvas(), type);
    }

    public Pog(Pog toCopy)
    {
        init(toCopy);
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    // --- Initialization ---

    private void initFromPacket(DataInputStream dis) throws IOException
    {
        String filename = UtilityFunctions.getLocalPath(dis.readUTF());
        PogLibrary lib = GametableFrame.getGametableFrame().getPogLibrary();
        filename = UtilityFunctions.getRelativePath(lib.getLocation(), new File(filename));

        int x = dis.readInt();
        int y = dis.readInt();
        m_position = new Point(x, y);
        int size = dis.readInt();
        m_id = dis.readInt();
        m_text = dis.readUTF();
        // boolean underlay =
        dis.readBoolean();
        m_scale = dis.readFloat();
        int numAttributes = dis.readInt();
        m_attributes.clear();
        for (int i = 0; i < numAttributes; i++)
        {
            String key = dis.readUTF();
            String value = dis.readUTF();
            setAttribute(key, value);
        }
        m_bTextChangeNotifying = false;

        PogType type = lib.getPog(filename);
        if (type == null)
        {
            type = lib.createPlaceholder(filename, size);
        }
        init(GametableFrame.getGametableFrame().getGametableCanvas(), type);
    }

    public void init(Pog orig)
    {
        m_position = orig.m_position;
        m_pogType = orig.m_pogType;
        m_canvas = orig.m_canvas;
        m_scale = orig.m_scale;
        m_text = new String(orig.m_text);
    }

    public void init(GametableCanvas canvas, PogType type)
    {
        m_pogType = type;
        m_canvas = canvas;
    }

    public void assignUniqueId()
    {
        m_id = g_nextId++;
    }

    // --- Accessors ---

    public int getId()
    {
        return m_id;
    }

    public boolean isTinted()
    {
        return m_bTinted;
    }

    public String getFilename()
    {
        return m_pogType.getFilename();
    }

    public PogType getPogType()
    {
        return m_pogType;
    }

    public int getWidth()
    {
        if (m_scale == 1f)
        {
            return m_pogType.getWidth();
        }

        return Math.round(m_pogType.getWidth() * m_scale);
    }

    public int getHeight()
    {
        if (m_scale == 1f)
        {
            return m_pogType.getHeight();
        }

        return Math.round(m_pogType.getHeight() * m_scale);
    }

    public boolean isUnderlay()
    {
        return m_pogType.isUnderlay();
    }

    public boolean isUnknown()
    {
        return m_pogType.isUnknown();
    }

    public int getHeightForZoomLevel()
    {
        int size = GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom);
        double ratio = (double)size / (double)GametableCanvas.BASE_SQUARE_SIZE;
        int imgSizeY = (int)(ratio * getHeight());
        return imgSizeY;
    }

    public int getWidthForZoomLevel()
    {
        int size = GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom);
        double ratio = (double)size / (double)GametableCanvas.BASE_SQUARE_SIZE;
        int imgSizeX = (int)(ratio * getWidth());
        return imgSizeX;
    }

    public int getX()
    {
        return getPosition().x;
    }

    public int getY()
    {
        return getPosition().y;
    }

    public int getFaceSize()
    {
        if (m_scale == 1f)
        {
            return m_pogType.getFaceSize();
        }

        return Math.round(m_pogType.getFaceSize() * m_scale);
    }

    public boolean testHit(Point modelPoint)
    {
        return m_pogType.testHit(modelToPog(modelPoint));
    }

    public boolean testHit(int modelX, int modelY)
    {
        return testHit(new Point(modelX, modelY));
    }

    public Point getPosition()
    {
        return m_position;
    }

    public String getText()
    {
        return m_text;
    }

    public String getAttribute(String name)
    {
        String normalizedName = UtilityFunctions.normalizeName(name);
        Attribute a = (Attribute)m_attributes.get(normalizedName);
        if (a == null)
        {
            return null;
        }
        return a.value;
    }

    public Set getAttributeNames()
    {
        Set s = new HashSet();
        for (Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
        {
            Attribute attribute = (Attribute)iterator.next();
            s.add(attribute.name);
        }
        return Collections.unmodifiableSet(s);
    }

    public boolean hasAttributes()
    {
        return !m_attributes.isEmpty();
    }

    // --- Setters ---

    public void setAttribute(String name, String value)
    {
        String normalizedName = UtilityFunctions.normalizeName(name);
        m_attributes.put(normalizedName, new Attribute(name, value));
        displayPogDataChange();
    }

    public void removeAttribute(String name)
    {
        String normalizedName = UtilityFunctions.normalizeName(name);
        m_attributes.remove(normalizedName);
    }

    public void setTinted(boolean b)
    {
        m_bTinted = b;
    }

    public void setPosition(Point pos)
    {
        m_position = pos;
    }

    public void setPosition(int x, int y)
    {
        setPosition(new Point(x, y));
    }

    public void setText(String text)
    {
        m_text = text;
        displayPogDataChange();
    }

    public void setFaceSize(int faceSize)
    {
        if (faceSize < 0)
        {
            m_scale = 1;
            return;
        }

        if (faceSize == 0)
        {
            m_scale = 1f / (m_pogType.getFaceSize() * 2f);
            return;
        }

        m_scale = (float)faceSize / (float)m_pogType.getFaceSize();

    }

    // --- Drawing ---

    public void drawScaled(Graphics g, int x, int y)
    {
        // we have to work with ratios, cause the pog could be large or huge, gargantuan, etc.
        float scale = (float)GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom)
            / (float)GametableCanvas.BASE_SQUARE_SIZE;
        m_pogType.drawScaled(g, x, y, scale * m_scale);
    }

    public void drawGhostlyToCanvas(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        drawToCanvas(g2);
        g2.dispose();
    }

    public void drawToCanvas(Graphics g)
    {
        // convert our model coordinates to draw coordinates
        Point drawCoords = m_canvas.modelToDraw(getPosition());
        float scale = (float)GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom)
            / (float)GametableCanvas.BASE_SQUARE_SIZE;

        m_pogType.drawScaled(g, drawCoords.x, drawCoords.y, scale * m_scale);

        // if we're tinted, draw tinted
        if (m_bTinted)
        {
            m_pogType.drawTint(g, drawCoords.x, drawCoords.y, scale * m_scale, Color.GREEN);
        }
    }

    public void drawTextToCanvas(Graphics gr, boolean bForceTextInBounds)
    {
        drawStringToCanvas(gr, bForceTextInBounds, COLOR_BACKGROUND);
        stopDisplayPogDataChange();
    }

    public void drawChangedTextToCanvas(Graphics g)
    {
        if (!m_bTextChangeNotifying)
        {
            return;
        }
        drawStringToCanvas(g, true, COLOR_CHANGED_BACKGROUND);
    }

    // --- Miscellany ---

    public void writeToPacket(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(getFilename());
        dos.writeInt(getX());
        dos.writeInt(getY());
        dos.writeInt(getFaceSize());
        dos.writeInt(m_id);
        dos.writeUTF(m_text);
        dos.writeBoolean(isUnderlay());
        dos.writeFloat(m_scale);
        dos.writeInt(m_attributes.size());
        for (Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
        {
            Attribute attribute = (Attribute)iterator.next();
            dos.writeUTF(attribute.name);
            dos.writeUTF(attribute.value);
        }
    }

    // --- Object Implementation ---

    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[Pog name: " + getFilename() + " pos: " + getPosition() + " size: " + getFaceSize() + "]";
    }

    // --- Private Helpers ---

    private void displayPogDataChange()
    {
        // we don't do this if the game is receiving inital data.
        if (PacketSourceState.isHostDumping())
        {
            return;
        }

        // we also don't do this if the game is loading a file from disk.
        if (PacketSourceState.isFileLoading())
        {
            return;
        }

        m_bTextChangeNotifying = true;
    }

    private void stopDisplayPogDataChange()
    {
        m_bTextChangeNotifying = false;
    }

    private Point modelToPog(Point modelPoint)
    {
        int x = modelPoint.x - m_position.x;
        x = Math.round(x / m_scale);
        int y = modelPoint.y - m_position.y;
        y = Math.round(y / m_scale);

        return new Point(x, y);
    }

    private void drawStringToCanvas(Graphics gr, boolean bForceTextInBounds, Color backgroundColor)
    {
        if (m_text == null)
        {
            m_text = "";
        }
        Graphics2D g = (Graphics2D)gr.create();
        g.setFont(FONT_TEXT);
        FontMetrics metrics = g.getFontMetrics();
        Rectangle stringBounds = metrics.getStringBounds(m_text, g).getBounds();

        int totalWidth = stringBounds.width + 6;
        int totalHeight = stringBounds.height + 1;

        Point pogDrawCoords = m_canvas.modelToDraw(getPosition());
        int viewWidth = getHeightForZoomLevel();
        Rectangle backgroundRect = new Rectangle();
        backgroundRect.x = pogDrawCoords.x + (viewWidth - totalWidth) / 2;
        backgroundRect.y = pogDrawCoords.y - totalHeight - 4;
        backgroundRect.width = totalWidth;
        backgroundRect.height = totalHeight;

        if (bForceTextInBounds)
        {
            // force it to be on the view
            if (backgroundRect.x < m_canvas.getActiveMap().getScrollX())
            {
                backgroundRect.x = m_canvas.getActiveMap().getScrollX();
            }

            if (backgroundRect.y < m_canvas.getActiveMap().getScrollY())
            {
                backgroundRect.y = m_canvas.getActiveMap().getScrollY();
            }

            if (backgroundRect.x + totalWidth > m_canvas.getActiveMap().getScrollX() + m_canvas.getWidth())
            {
                backgroundRect.x = m_canvas.getActiveMap().getScrollX() + m_canvas.getWidth() - totalWidth;
            }

            if (backgroundRect.y + totalHeight > m_canvas.getActiveMap().getScrollY() + m_canvas.getHeight())
            {
                backgroundRect.y = m_canvas.getActiveMap().getScrollY() + m_canvas.getHeight() - totalHeight;
            }
        }

        if (m_text.length() > 0)
        {
            g.setColor(backgroundColor);
            g.fill(backgroundRect);

            int stringX = backgroundRect.x + (backgroundRect.width - stringBounds.width) / 2;
            int stringY = backgroundRect.y + (backgroundRect.height - stringBounds.height) / 2 + metrics.getAscent();

            g.setColor(Color.BLACK);
            g.drawString(m_text, stringX, stringY);

            g.drawRect(backgroundRect.x, backgroundRect.y, backgroundRect.width - 1, backgroundRect.height - 1);
        }
        drawAttributes(g, backgroundRect.x + (backgroundRect.width / 2), backgroundRect.y + backgroundRect.height);
        g.dispose();
    }

    private void drawAttributes(Graphics g, int x, int y)
    {
        int numAttributes = m_attributes.size();
        if (numAttributes < 1)
        {
            return;
        }

        Graphics2D g2 = (Graphics2D)g.create();
        FontMetrics nameMetrics = g2.getFontMetrics(FONT_ATTRIBUTE_NAME);
        FontMetrics valueMetrics = g2.getFontMetrics(FONT_ATTRIBUTE_VALUE);
        int maxLineHeight = Math.max(nameMetrics.getHeight(), valueMetrics.getHeight());
        int height = maxLineHeight * numAttributes;
        final int PADDING = 3;
        final int SPACE = PADDING * 2;
        int width = 0;
        for (Iterator iterator = m_attributes.keySet().iterator(); iterator.hasNext();)
        {
            String name = (String)iterator.next();
            String value = getAttribute(name);
            int attrWidth = nameMetrics.stringWidth(name + ": ") + valueMetrics.stringWidth(value);
            if (attrWidth > width)
            {
                width = attrWidth;
            }
        }

        height += SPACE;
        width += SPACE;

        int drawX = x - width / 2;
        int drawY = y;
        g2.setColor(COLOR_ATTRIBUTE_BACKGROUND);
        g2.fillRect(drawX, drawY, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(drawX, drawY, width - 1, height - 1);

        drawX += PADDING;
        drawY += PADDING + nameMetrics.getAscent();
        for (Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
        {
            Attribute attribute = (Attribute)iterator.next();
            String drawString = attribute.name + ": ";
            g2.setFont(FONT_ATTRIBUTE_NAME);
            g2.drawString(drawString, drawX, drawY);
            int nameWidth = nameMetrics.stringWidth(drawString);
            g2.setFont(FONT_ATTRIBUTE_VALUE);
            drawString = attribute.value;
            g2.drawString(attribute.value, drawX + nameWidth, drawY);
            drawY += maxLineHeight;
        }

        g2.dispose();
    }

}
