/*
 * Pog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import com.galactanet.gametable.ui.PogLibrary;
import com.galactanet.gametable.util.UtilityFunctions;



/**
 * Represents an instance of a PogType on the Map.
 * 
 * @author sephalon
 */
public class Pog implements Comparable
{
    
    public final static int LAYER_UNDERLAY  = 0;
    public final static int LAYER_OVERLAY   = 1;
    public final static int LAYER_ENV       = 2;
    public final static int LAYER_POG       = 3;
    
    // --- Constants -------------------------------------------------------------------------------------------------

    private static class Attribute
    {
        public boolean changed = true;
        public String  name;
        public String  value;

        public Attribute(final String n, final String v)
        {
            name = n;
            value = v;
        }
    }

    /**
     * Background color for pog text.
     */
    private static final Color COLOR_ATTRIBUTE_BACKGROUND = new Color(64, 255, 64, 192);

    /**
     * Background color for pog text.
     */
    private static final Color COLOR_BACKGROUND           = new Color(255, 255, 64, 192);

    /**
     * Background color for changed pog text.
     */
    private static final Color COLOR_CHANGED_BACKGROUND   = new Color(238, 156, 0, 192);

    /**
     * Font to use for displaying attribute names.
     */
    private static final Font  FONT_ATTRIBUTE_NAME        = Font.decode("sansserif-bold-12");

    /**
     * Font to use for displaying attribute names.
     */
    private static final Font  FONT_ATTRIBUTE_VALUE       = Font.decode("sansserif-12");

    // --- Types -----------------------------------------------------------------------------------------------------

    /**
     * Font to use for displaying pog text.
     */
    private static final Font  FONT_TEXT                  = Font.decode("sansserif-bold-12");

    // --- Static Members --------------------------------------------------------------------------------------------

    /**
     * Unique global Id for pogs.
     */
    public static int          g_nextId                   = 10;

    /**
     * Global min sort id for pogs.
     */
    public static long         g_nextSortId               = 0;

    // --- Members ---------------------------------------------------------------------------------------------------

    private int                m_layer                    = 0;
    private double             m_angle                    = 0.;
    private boolean            m_forceGridSnap            = false;

    /**
     * The direction which a pog has been flipped.
     */
    private int                 m_flipH                   = 0;
    private int                 m_flipV                   = 0;
    
    /**
     * Name/value pairs of the attributes assigned to this pog.
     */
    private final Map          m_attributes               = new TreeMap();

    // a special kind of hack-ish value that will cause a pog
    // to set itself to not be loaded if the values for it are
    // too out of whack to be correct. This is to prevent bad saves caused
    // by other bugs from permanently destroying a map.
    public boolean             m_bStillborn               = false;

    /**
     * True if this pog is notifying the world that it's text had changed.
     */
    private boolean            m_bTextChangeNotifying     = false;

    /**
     * Is this pog tinted?
     */
    private boolean            m_bTinted                  = false;

    /**
     * Lame handle to canvas.
     */
    private GametableCanvas    m_canvas;

    // null in most cases. If it's not null, it's a
    // card in a deck
    private DeckData.Card      m_card;

    /**
     * The unique id of this pog.
     */
    private int                m_id                       = 0;

    /**
     * Locked state for this pog.
     */
    private boolean            m_locked                   = false;

    /**
     * The PogType of this Pog.
     */
    private PogType            m_pogType;

    /**
     * Position of the pog on the map in map coordinates.
     */
    private Point              m_position                 = new Point(0, 0);

    /**
     * Scale for this pog.
     */
    private float              m_scale                    = 1f;

    /**
     * The sort order for this pog.
     */
    private long               m_sortOrder                = 0;

    /**
     * The primary label for the Pog.
     */
    private String             m_text                     = "";

    /**
     * Hit map for this pog.
     */
    public BitSet              m_hitMap;

    // --- Constructors ----------------------------------------------------------------------------------------------

    public Pog(final DataInputStream dis) throws IOException
    {
        initFromPacket(dis);
    }

    public Pog(final Pog toCopy)
    {
        init(toCopy);
    }

    public Pog(final PogType type)
    {
        init(GametableFrame.getGametableFrame().getGametableCanvas(), type);
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    // --- Initialization ---

    public void assignUniqueId()
    {
        m_id = g_nextId++;
        m_sortOrder = g_nextSortId++;
    }

    /*
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Object o)
    {
        if (equals(o))
        {
            return 0;
        }

        final Pog pog = (Pog)o;
        long diff = m_sortOrder - pog.getSortOrder();
        if (diff == 0)
        {
            diff = m_id - pog.getId();
        }

        return (diff < 0 ? -1 : (diff > 0 ? 1 : 0));
    }

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

    private void drawAttributes(final Graphics g, final int x, final int y, final boolean onlyChanged)
    {
        int numAttributes = 0;
        if (onlyChanged)
        {
            for (final Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
            {
                final Attribute attribute = (Attribute)iterator.next();
                if (attribute.changed)
                {
                    numAttributes++;
                }
            }
        }
        else
        {
            numAttributes = m_attributes.size();
        }

        if (numAttributes < 1)
        {
            return;
        }

        final Graphics2D g2 = (Graphics2D)g.create();
        final FontMetrics nameMetrics = g2.getFontMetrics(FONT_ATTRIBUTE_NAME);
        final FontMetrics valueMetrics = g2.getFontMetrics(FONT_ATTRIBUTE_VALUE);
        int height = 0;
        int width = 0;
        for (final Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
        {
            final Attribute attribute = (Attribute)iterator.next();
            if (onlyChanged && !attribute.changed)
            {
                continue;
            }

            final Rectangle nameBounds = nameMetrics.getStringBounds(attribute.name + ": ", g2).getBounds();
            final Rectangle valueBounds = valueMetrics.getStringBounds(attribute.value, g2).getBounds();
            final int attrWidth = nameBounds.width + valueBounds.width;
            if (attrWidth > width)
            {
                width = attrWidth;
            }
            final int attrHeight = Math.max(nameBounds.height, valueBounds.height);
            height += attrHeight;
        }

        final int PADDING = 3;
        final int SPACE = PADDING * 2;
        height += SPACE;
        width += SPACE;

        int drawX = x - width / 2;
        int drawY = y;
        g2.setColor(COLOR_ATTRIBUTE_BACKGROUND);
        g2.fillRect(drawX, drawY, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(drawX, drawY, width - 1, height - 1);

        drawX += PADDING;
        drawY += PADDING;
        for (final Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
        {
            final Attribute attribute = (Attribute)iterator.next();
            if (onlyChanged && !attribute.changed)
            {
                continue;
            }

            final String nameString = attribute.name + ": ";
            final String valueString = attribute.value;
            final Rectangle nameBounds = nameMetrics.getStringBounds(nameString, g2).getBounds();
            final Rectangle valueBounds = valueMetrics.getStringBounds(valueString, g2).getBounds();
            final int baseline = Math.max(-nameBounds.y, -valueBounds.y);
            g2.setFont(FONT_ATTRIBUTE_NAME);
            g2.drawString(nameString, drawX, drawY + baseline);
            g2.setFont(FONT_ATTRIBUTE_VALUE);
            g2.drawString(attribute.value, drawX + nameBounds.width, drawY + baseline);
            drawY += Math.max(nameBounds.height, valueBounds.height);
        }

        g2.dispose();
    }

    public void drawChangedTextToCanvas(final Graphics g)
    {
        if (!m_bTextChangeNotifying)
        {
            return;
        }
        drawStringToCanvas(g, true, COLOR_CHANGED_BACKGROUND);
    }

    public void drawGhostlyToCanvas(final Graphics g)
    {
        final Graphics2D g2 = (Graphics2D)g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        drawToCanvas(g2);
        g2.dispose();
    }

    public void drawScaled(final Graphics g, final int x, final int y, final float scale)
    {
          final int drawWidth = Math.round(m_pogType.getWidth(m_angle, m_forceGridSnap) * scale);
          final int drawHeight = Math.round(m_pogType.getHeight(m_angle, m_forceGridSnap) * scale);

//          g.drawImage(m_pogType.rotate(m_pogType.flip(m_pogType.m_image, m_flipH, m_flipV), m_angle), x, y, drawWidth, drawHeight, null);
          //m_pogType.flip(, m_flipH, m_flipV)
          Image im = m_pogType.rotate(m_pogType.getImage(), m_angle, m_forceGridSnap);       

          // Center the image into a square, taking into consideration the height and width
          int mw = 0;
          int mh = 0;
          if(m_angle != 0) {
              mw = Math.round(drawWidth - (m_pogType.getWidth(0, m_forceGridSnap) * scale));       
              mh = Math.round(drawHeight - (m_pogType.getWidth(0, m_forceGridSnap) * scale));       
          }
          g.drawImage(im,x-mw/2,y-mh/2,drawWidth,drawHeight,null);
    }

    private void reinitializeHitMap()
    {
        final Image img = m_pogType.getImage();
        if (img == null)  {
            m_hitMap = m_pogType.getHitMap();
        }
       
        final int iw = m_pogType.getWidth(m_angle, m_forceGridSnap);
        final int ih = m_pogType.getHeight(m_angle, m_forceGridSnap);
        if(( ih < 0) || ( iw < 0)) {
            m_hitMap = m_pogType.getHitMap();
        }

        final BufferedImage bufferedImage = new BufferedImage(iw,ih, BufferedImage.TYPE_INT_RGB);
        {
            final Graphics2D g = bufferedImage.createGraphics();
            g.setColor(new Color(0xff00ff));
            g.fillRect(0, 0, iw, ih);
            g.drawImage(m_pogType.rotate(m_pogType.flip(img, m_flipH, m_flipV), m_angle, m_forceGridSnap)
                , 0,0, iw, ih, null);
            g.dispose();
        }

        final DataBuffer buffer = bufferedImage.getData().getDataBuffer();
        final int len = iw * ih;
        m_hitMap = new BitSet(len);
        m_hitMap.clear();
        for (int i = 0; i < len; ++i)
        {
            final int pixel = buffer.getElem(i) & 0xFFFFFF;
            m_hitMap.set(i, (pixel != 0xFF00FF));
        }
    }

    // --- Accessors ---

    private void drawStringToCanvas(final Graphics gr, final boolean bForceTextInBounds, final Color backgroundColor)
    {
        drawStringToCanvas(gr, bForceTextInBounds, backgroundColor, false);
    }

    private void drawStringToCanvas(final Graphics gr, final boolean bForceTextInBounds, final Color backgroundColor,
        boolean drawAttributes)
    {
        if (m_text == null)
        {
            m_text = "";
        }
        final Graphics2D g = (Graphics2D)gr.create();
        g.setFont(FONT_TEXT);
        final FontMetrics metrics = g.getFontMetrics();
        final Rectangle stringBounds = metrics.getStringBounds(m_text, g).getBounds();

        final int totalWidth = stringBounds.width + 6;
        final int totalHeight = stringBounds.height + 1;

        final Point pogDrawCoords = m_canvas.modelToDraw(getPosition());
        final int viewWidth = getHeightForZoomLevel();
        final Rectangle backgroundRect = new Rectangle();
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

            final int stringX = backgroundRect.x + (backgroundRect.width - stringBounds.width) / 2;
            final int stringY = backgroundRect.y + (backgroundRect.height - stringBounds.height) / 2
                + metrics.getAscent();

            g.setColor(Color.BLACK);
            g.drawString(m_text, stringX, stringY);

            g.drawRect(backgroundRect.x, backgroundRect.y, backgroundRect.width - 1, backgroundRect.height - 1);
        }

        drawAttributes(g, backgroundRect.x + (backgroundRect.width / 2), backgroundRect.y + backgroundRect.height,
            !drawAttributes);
        g.dispose();
    }

    public void drawTextToCanvas(final Graphics gr, final boolean bForceTextInBounds)
    {
        drawTextToCanvas(gr, bForceTextInBounds, false);
    }

    public void drawTextToCanvas(final Graphics gr, final boolean bForceTextInBounds, final boolean drawAttributes)
    {
        drawStringToCanvas(gr, bForceTextInBounds, COLOR_BACKGROUND, drawAttributes);
        stopDisplayPogDataChange();
    }

    public void drawToCanvas(final Graphics g)
    {
        // determine the visible area of the gametable canvas
        final Rectangle visbleCanvas = m_canvas.getVisibleCanvasRect(m_canvas.m_zoom);
        // determine the area covered by the pog - replaced with a set value m_bounds
        // final Rectangle pogArea = getBounds(m_canvas);
        
        if (visbleCanvas.intersects(getBounds(m_canvas)))
        {
            // Some portion of the pog's area overlaps the visible canvas area, so
            // we paint the pog to the canvas.  

            // convert our model coordinates to draw coordinates
            final Point drawCoords = m_canvas.modelToDraw(getPosition());
            final float scale = (float)GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom)
            / (float)GametableCanvas.BASE_SQUARE_SIZE;

            drawScaled(g, drawCoords.x, drawCoords.y, scale * m_scale);

            // if we're tinted, draw tinted
            if (m_bTinted)
            {
                m_pogType.drawTint(g, drawCoords.x, drawCoords.y, scale * m_scale, Color.GREEN, m_angle, m_forceGridSnap);
            }
        }
    }
    
    /** 
     * Returns a rectangle identifying the space taken by the Pog
     * @return 
     */
    public Rectangle getBounds(GametableCanvas canvas)
    {
//        final Point drawCoords = m_canvas.modelToDraw(getPosition());
//        final float scale = 
//            m_scale * GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom) / 
//            GametableCanvas.BASE_SQUARE_SIZE;
//        
//        final int drawWidth = Math.round(m_pogType.getWidth(m_angle) * scale);
//        final int drawHeight = Math.round(m_pogType.getHeight(m_angle) * scale);
//        
//        return new Rectangle(
//            drawCoords.x, drawCoords.y,
//            drawWidth, drawHeight);
        final Rectangle pogArea = new Rectangle(m_position.x, m_position.y, getWidth(), getHeight());
        return pogArea;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        final Pog pog = (Pog)obj;
        return (pog.getId() == m_id);
    }

    public double getAngle()
    {
        return m_angle;
    }

    public boolean getForceGridSnap()
    {
        return m_forceGridSnap;
    }

    public int getFlipH()
    {
        return m_flipH;
    }

    public int getFlipV()
    {
        return m_flipV;
    }

    public String getAttribute(final String name)
    {
        final String normalizedName = UtilityFunctions.normalizeName(name);
        final Attribute a = (Attribute)m_attributes.get(normalizedName);
        if (a == null)
        {
            return null;
        }
        return a.value;
    }

    public Set getAttributeNames()
    {
        final Set s = new HashSet();
        for (final Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
        {
            final Attribute attribute = (Attribute)iterator.next();
            s.add(attribute.name);
        }
        return Collections.unmodifiableSet(s);
    }

    public DeckData.Card getCard()
    {
        return m_card;
    }

    public int getFaceSize()
    {
        if (m_scale == 1f)
        {
            return m_pogType.getFaceSize();
        }

        return Math.max(Math.round(Math.max(m_pogType.getWidth(m_angle, m_forceGridSnap), m_pogType.getHeight(m_angle, m_forceGridSnap)) * m_scale
            / GametableCanvas.BASE_SQUARE_SIZE), 1);
    }

    public String getFilename()
    {
        return m_pogType.getFilename();
    }

    public int getHeight()
    {
        if (m_scale == 1f)
        {
            return m_pogType.getHeight(m_angle, m_forceGridSnap);
        }

        return Math.round(m_pogType.getHeight(m_angle, m_forceGridSnap) * m_scale);
    }

    public int getHeightForZoomLevel()
    {
        final int size = GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom);
        final double ratio = (double)size / (double)GametableCanvas.BASE_SQUARE_SIZE;
        final int imgSizeY = (int)(ratio * getHeight());
        return imgSizeY;
    }

    public int getId()
    {
        return m_id;
    }

    public int getLayer() {
        return m_layer;
    }
    
    public PogType getPogType()
    {
        return m_pogType;
    }

    public Point getPosition()
    {
        return m_position;
    }

    /**
     * @return A vector to adjust the drag position when snapping for odd-sized pogs.
     */
    public Point getSnapDragAdjustment()
    {
        final Point adjustment = new Point();
        final int width = getWidth();
        final int height = getHeight();

        if (width < height)
        {
            adjustment.x = -(height - width) / 2;
        }
        else if (width > height)
        {
            adjustment.y = -(width - height) / 2;
        }

        return adjustment;
    }

    public long getSortOrder()
    {
        return m_sortOrder;
    }

    public String getText()
    {
        return m_text;
    }

    public int getWidth()
    {
        if (m_scale == 1f)
        {
            return m_pogType.getWidth(m_angle, m_forceGridSnap);
        }

        return Math.round(m_pogType.getWidth(m_angle, m_forceGridSnap) * m_scale);
    }

    public int getWidthForZoomLevel()
    {
        final int size = GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom);
        final double ratio = (double)size / (double)GametableCanvas.BASE_SQUARE_SIZE;
        final int imgSizeX = (int)(ratio * getWidth());
        return imgSizeX;
    }

    public int getX()
    {
        return getPosition().x;
    }

    // --- Setters ---

    public int getY()
    {
        return getPosition().y;
    }

    public boolean hasAttributes()
    {
        return !m_attributes.isEmpty();
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return m_id;
    }

    private void init(final GametableCanvas canvas, final PogType type)
    {
        m_pogType = type;
        m_canvas = canvas;
        m_layer = (type.getType());
    }

    private void init(final Pog orig)
    {
        m_position = orig.m_position;
        m_pogType  = orig.m_pogType;
        m_canvas   = orig.m_canvas;
        m_scale    = orig.m_scale;
        m_angle    = orig.m_angle;
        m_flipH    = orig.m_flipH;
        m_flipV    = orig.m_flipV;
        m_text     = orig.m_text;
        m_layer    = orig.m_layer;

        if (orig.m_card == null)
        {
            m_card = orig.m_card;
        }
        else
        {
            m_card = DeckData.createBlankCard();
            m_card.copy(orig.m_card);
        }

        for (final Iterator iterator = orig.m_attributes.values().iterator(); iterator.hasNext();)
        {
            final Attribute attribute = (Attribute)iterator.next();
            setAttribute(attribute.name, attribute.value);
        }
        stopDisplayPogDataChange();

        reinitializeHitMap();
    }

    private void initFromPacket(final DataInputStream dis) throws IOException
    {
        String filename = UtilityFunctions.getLocalPath(dis.readUTF());
        final PogLibrary lib = GametableFrame.getGametableFrame().getPogLibrary();
        filename = UtilityFunctions.getRelativePath(lib.getLocation(), new File(filename));

        final int x = dis.readInt();
        final int y = dis.readInt();
        m_position = new Point(x, y);
        final int size = dis.readInt();
        m_id = dis.readInt();
        m_sortOrder = dis.readLong();
        m_text = dis.readUTF();
        boolean underlay = dis.readBoolean();
        try {
            m_scale = dis.readFloat();
        }
        catch(IOException exp)
        {
            m_scale = 1f;
        }
        
        try {
            m_angle = dis.readDouble();
        }
        catch(IOException exp)
        {
            m_angle = 0.;
        }
        try {
            m_flipH = dis.readInt();
            m_flipV = dis.readInt();
        }
        catch(IOException exp)
        {
            m_flipH = 0;
            m_flipV = 0;
        }
        
        try {
            m_locked = dis.readBoolean();
        }
        catch(IOException exp)
        {
            m_locked = false;
        }
        
        try {
         // read in the card info, if any
            final boolean bCardExists = dis.readBoolean();
            if (bCardExists)
            {
                m_card = DeckData.createBlankCard();
                m_card.read(dis);
            }
            else
            {
                // no card
                m_card = null;
            }

            final int numAttributes = dis.readInt();
            m_attributes.clear();
            for (int i = 0; i < numAttributes; i++)
            {
                final String key = dis.readUTF();
                final String value = dis.readUTF();
                setAttribute(key, value);
            }
        }
        catch(IOException exp)
        {
            m_card = null;
        }
        
        int layer;
        try {
            layer = dis.readInt();
        }
        catch(IOException exp) {
            if(underlay) layer = LAYER_UNDERLAY;
            else layer = LAYER_POG;
        }
        
        // special case psuedo-hack check
        // through reasons unclear to me, sometimes a pog will get
        // a size of around 2 billion. A more typical size would
        // be around 1.
        if ((size > 100) || (m_scale > 100.0))
        {
            m_bStillborn = true;
            return;
        }

        stopDisplayPogDataChange();

        PogType type = lib.getPog(filename);
        if (type == null)
        {
            type = lib.createPlaceholder(filename, size);
        }
        init(GametableFrame.getGametableFrame().getGametableCanvas(), type);
        m_layer = layer; // Saving here as the init updates the layer for newly dropped pogs.

        reinitializeHitMap();
    }

    public boolean isCardPog()
    {
        if (m_card == null)
        {
            return false;
        }
        return true;
    }

    public boolean isLocked()
    {
        return m_locked;
    }

    public boolean isTinted()
    {
        return m_bTinted;
    }

    public boolean isUnderlay()
    {
        return (m_layer==LAYER_POG?false:true);
    }

    public boolean isUnknown()
    {
        return m_pogType.isUnknown();
    }

    // --- Drawing ---

    /** ************************ CARD POG STUFF ****************************** */
    public void makeCardPog(final DeckData.Card card)
    {
        // note the card info. We copy it, so we aren't affected
        // by future changes to this card instance.
        m_card = card.makeCopy();

        /*
         * Commented out because these attributes become really annoying in play. They pop up whenever the mouse is over
         * the card and it's irritating. 
         */
        // set the appropriate attributes
        if ( card.m_cardName.length() > 0 )
        {
            m_text = card.m_cardName;
        }
        if ( card.m_cardDesc.length() > 0 )
        {
            setAttribute("Desc", card.m_cardDesc);
        }
        if ( card.m_deckName.length() > 0 )
        {
            setAttribute("Deck", card.m_deckName);
        }
    }

    private Point modelToPog(final Point modelPoint)
    {
        // translation for x & y
        int sx = 0;
        int sy = 0;
        if(m_angle != 0) {
            final int dw = m_pogType.getWidth(m_angle, m_forceGridSnap);
            final int dh = m_pogType.getHeight(m_angle, m_forceGridSnap);
            final int iw = m_pogType.getWidth(0, m_forceGridSnap);
            final int ih = m_pogType.getHeight(0, m_forceGridSnap);
            // Point Shift for the drawing
            sx = Math.round((dw - iw)/2 * m_scale);
            sy = Math.round((dh - ih)/2 * m_scale);
        }
       
//        int x = modelPoint.x - m_position.x;
//        x = Math.round(x / m_scale);
//        int y = modelPoint.y - m_position.y;
//        y = Math.round(y / m_scale);

        int x = modelPoint.x - (m_position.x - sx);
        x = Math.round(x / m_scale);
        int y = modelPoint.y - (m_position.y - sy);
        y = Math.round(y / m_scale);

        return new Point(x, y);
    }

    public void removeAttribute(final String name)
    {
        final String normalizedName = UtilityFunctions.normalizeName(name);
        m_attributes.remove(normalizedName);
    }

    public void setAngle(final double angle)
    {
        m_angle = angle;
        reinitializeHitMap();
    }

    public void setForceGridSnap(final boolean forceGridSnap)
    {
        m_forceGridSnap = forceGridSnap;
        reinitializeHitMap();
    }

    public void setFlip(final int flipH, final int flipV)
    {
        m_flipH = flipH;
        m_flipV = flipV;
        reinitializeHitMap();
    }

   public void setAttribute(final String name, final String value)
    {
        final String normalizedName = UtilityFunctions.normalizeName(name);
        m_attributes.put(normalizedName, new Attribute(name, value));
        displayPogDataChange();
    }

    public void setFaceSize(final float faceSize)
    {
        if (faceSize <= 0)
        {
            if (m_scale != 1)
            {
                m_scale = 1;
                reinitializeHitMap();
            }
            return;
        }

        final float targetDimension = GametableCanvas.BASE_SQUARE_SIZE * faceSize;
        final float maxDimension = Math.max(getPogType().getWidth(0, m_forceGridSnap), getPogType().getHeight(0, m_forceGridSnap));
        if (maxDimension == 0)
        {
            throw new ArithmeticException("Zero sized pog dimension: " + this);
        }
        m_scale = targetDimension / maxDimension;
        reinitializeHitMap();
        return;
    }

    // --- Miscellaneous ---

    public void setId(final int id) {
        m_id = id;
    }
    
    public void setLayer(final int l) {
        m_layer = l;       
    }
    
    public void setLocked(final boolean b)
    {
        m_locked = b;
    }

    public void setPogType(final PogType pt) {
        m_pogType = pt;
        reinitializeHitMap();
    }
    
    // --- Comparable Implementation ---

    public void setPosition(final int x, final int y)
    {
        setPosition(new Point(x, y));
    }

    // --- Object Implementation ---

    public void setPosition(final Point pos)
    {
        m_position = pos;
    }

    public void setSortOrder(final long order)
    {
        m_sortOrder = order;
    }

    public void setText(final String text)
    {
        m_text = text;
        displayPogDataChange();
    }

    // --- Private Helpers ---

    public void setTinted(final boolean b)
    {
        m_bTinted = b;
    }

    private void stopDisplayPogDataChange()
    {
        m_bTextChangeNotifying = false;
        for (final Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
        {
            final Attribute attribute = (Attribute)iterator.next();
            attribute.changed = false;
        }
    }

    public boolean testHit(final int modelX, final int modelY)
    {
        return testHit(new Point(modelX, modelY));
    }

    public boolean testHit(final Point modelPoint)
    {
        return testHit(modelToPog(modelPoint), m_angle);
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

        if (x >= m_pogType.getWidth(angle, m_forceGridSnap))
        {
            return false;
        }

        if (y < 0)
        {
            return false;
        }

        if (y >= m_pogType.getHeight(angle, m_forceGridSnap))
        {
            return false;
        }

        // if we are unknown, then let's just go with it.
        if (m_hitMap == null)
        {
            m_hitMap = m_pogType.getHitMap();
        }

        // otherwise, let's see if they hit an actual pixel

        final int idx = x + (y * m_pogType.getWidth(angle, m_forceGridSnap));
        final boolean value = m_hitMap.get(idx);

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
        return "[Pog name: " + getFilename() + " (" + getId() + " - " + getSortOrder() + ") pos: " + getPosition()
            + " size: " + getFaceSize() + "]";
    }

    public void writeToPacket(final DataOutputStream dos) throws IOException
    {
        dos.writeUTF(getFilename());
        dos.writeInt(getX());
        dos.writeInt(getY());
        dos.writeInt(getPogType().getFaceSize());
        dos.writeInt(m_id);
        dos.writeLong(m_sortOrder);
        dos.writeUTF(m_text);
        dos.writeBoolean(isUnderlay());
        dos.writeFloat(m_scale);
        dos.writeDouble(m_angle);
        dos.writeInt(m_flipH);
        dos.writeInt(m_flipV);
        dos.writeBoolean(m_locked);

        // write out the card info, if any
        if (m_card != null)
        {
            dos.writeBoolean(true); // we have a valid card
            m_card.write(dos);
        }
        else
        {
            dos.writeBoolean(false); // no card info
        }

        dos.writeInt(m_attributes.size());
        for (final Iterator iterator = m_attributes.values().iterator(); iterator.hasNext();)
        {
            final Attribute attribute = (Attribute)iterator.next();
            dos.writeUTF(attribute.name);
            dos.writeUTF(attribute.value);
        }
        dos.writeInt(m_layer);
    }

}
