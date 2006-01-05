/*
 * Pog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class Pog
{
    private static final Color COLOR_BACKGROUND         = new Color(255, 255, 64, 192);
    private static final Color COLOR_CHANGED_BACKGROUND = new Color(238, 156, 0, 192);

    public static int          g_nextID                 = 10;
    public static final Font   FONT                     = new Font("Arial", 0, 14);

    private PogType            m_pogType;
    private GametableCanvas    m_canvas;

    // model coordinates
    private Point              m_position               = new Point(0, 0);
    private String             m_dataStr                = "";
    private int                m_Id                     = 0;
    private boolean            m_bTinted                = false;
    private boolean            m_bTextChangeNotifying   = false;


    public Pog(DataInputStream dis) throws IOException
    {
        initFromPacket(dis);
    }

    public Pog(PogType type)
    {
        init(GametableFrame.getGametableFrame().m_gametableCanvas, type);
    }

    public Pog(Pog toCopy)
    {
        init(toCopy);
    }

    public void getUniqueID()
    {
        m_Id = g_nextID++;
    }

    public int getId()
    {
        return m_Id;
    }

    public boolean isTinted()
    {
        return m_bTinted;
    }
    
    public void setTinted(boolean b)
    {
        m_bTinted = b;
    }
    
    public String toString()
    {
        return "[Pog name: " + getFilename() + " pos: " + getPosition() + " size: " + getFaceSize() + "]";
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
        return m_pogType.getWidth();
    }

    public int getHeight()
    {
        return m_pogType.getHeight();
    }

    public boolean isUnderlay()
    {
        return m_pogType.isUnderlay();
    }

    public void writeToPacket(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(getFilename());
        dos.writeInt(getX());
        dos.writeInt(getY());
        dos.writeInt(getFaceSize());
        dos.writeInt(m_Id);
        dos.writeUTF(m_dataStr);
        dos.writeBoolean(isUnderlay());
    }

    private void initFromPacket(DataInputStream dis) throws IOException
    {
        String filename = dis.readUTF();
        int x = dis.readInt();
        int y = dis.readInt();
        m_position = new Point(x, y);
        int size = dis.readInt();
        m_Id = dis.readInt();
        m_dataStr = dis.readUTF();
        // boolean underlay =
        dis.readBoolean();

        PogLibrary lib = GametableFrame.getGametableFrame().getPogLibrary();
        PogType type = lib.getPog(filename);
        if (type == null)
        {
            type = lib.createPlaceholder(filename, size);
        }
        init(GametableFrame.getGametableFrame().m_gametableCanvas, type);
    }

    public void init(Pog orig)
    {
        m_position = orig.m_position;
        m_pogType = orig.m_pogType;
        m_canvas = orig.m_canvas;
        m_dataStr = new String(orig.m_dataStr);
    }

    public void init(GametableCanvas canvas, PogType type)
    {
        m_pogType = type;
        m_canvas = canvas;
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
        return m_pogType.getFaceSize();
    }

    public boolean testHit(Point modelPoint)
    {
        return m_pogType.testHit(modelToPog(modelPoint));
    }

    public boolean testHit(int modelX, int modelY)
    {
        return m_pogType.testHit(modelToPog(modelX, modelY));
    }

    public Point getPosition()
    {
        return m_position;
    }

    public void setPosition(Point pos)
    {
        m_position = pos;
    }

    public void setPosition(int x, int y)
    {
        setPosition(new Point(x, y));
    }

    public String getText()
    {
        return m_dataStr;
    }

    public void setText(String text)
    {
        m_dataStr = text;
        displayPogDataChange();
    }

    void displayPogDataChange()
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

    void stopDisplayPogDataChange()
    {
        m_bTextChangeNotifying = false;
    }

    public void drawChangeText(Graphics g)
    {
        if (!m_bTextChangeNotifying)
        {
            return;
        }
        drawStringToCanvas(g, true, COLOR_CHANGED_BACKGROUND);
    }

    public void draw(Graphics g, int x, int y, ImageObserver observer)
    {
        m_pogType.draw(g, x, y);
    }

    public void drawScaled(Graphics g, int x, int y)
    {
        // we have to work with ratios, cause the pog could be large or huge, gargantuan, etc.
        float scale = (float)GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom)
            / (float)GametableCanvas.BASE_SQUARE_SIZE;
        m_pogType.drawScaled(g, x, y, scale);
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

        m_pogType.drawScaled(g, drawCoords.x, drawCoords.y, scale);

        // if we're tinted, draw tinted
        if (m_bTinted)
        {
            m_pogType.drawTint(g, drawCoords.x, drawCoords.y, scale, Color.GREEN);
        }
    }

    public void drawDataStringToCanvas(Graphics gr, boolean bForceTextInBounds)
    {
        drawStringToCanvas(gr, bForceTextInBounds, COLOR_BACKGROUND);
        stopDisplayPogDataChange();
    }

    private Point modelToPog(Point modelPoint)
    {
        return new Point(modelPoint.x - m_position.x, modelPoint.y - m_position.y);
    }

    private Point modelToPog(int modelX, int modelY)
    {
        return new Point(modelX - m_position.x, modelY - m_position.y);
    }

    private void drawStringToCanvas(Graphics gr, boolean bForceTextInBounds, Color backgroundColor)
    {
        Graphics2D g = (Graphics2D)gr.create();
        if (m_dataStr == null)
        {
            return;
        }

        if (m_dataStr.length() == 0)
        {
            return;
        }

        FontMetrics metrics = g.getFontMetrics();
        Rectangle stringBounds = metrics.getStringBounds(m_dataStr, g).getBounds();

        int totalWidth = stringBounds.width + 6;
        int totalHeight = stringBounds.height + 1;

        Rectangle backgroundRect = new Rectangle();
        Point pogDrawCoords = m_canvas.modelToDraw(getPosition());
        int viewWidth = getHeightForZoomLevel();
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

        g.setColor(backgroundColor);
        g.fill(backgroundRect);

        int stringX = backgroundRect.x + (backgroundRect.width - stringBounds.width) / 2;
        int stringY = backgroundRect.y + (backgroundRect.height - stringBounds.height) / 2 + metrics.getAscent();

        g.setColor(Color.BLACK);
        g.drawString(m_dataStr, stringX, stringY);

        g.drawRect(backgroundRect.x, backgroundRect.y, backgroundRect.width - 1, backgroundRect.height - 1);
    }
}
