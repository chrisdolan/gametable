/*
 * Pog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;


/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class Pog
{
    public static int        g_nextID          = 10;
    public static final Font FONT              = new Font("Arial", 0, 14);

    private Image            m_image;
    private int              m_pixels[];
    private GametableCanvas  m_canvas;

    // model coordinates
    private Point            m_position        = new Point(0, 0);

    // size
    private int              m_faceSize;
    public String            m_fileName;

    public String            m_dataStr         = "";

    public int               m_ID              = 0;
    public boolean           m_bIsUnderlay     = false;
    public boolean           m_bIsUnknownImage = false;

    public boolean           m_bTinted = false;
    
    public boolean           m_bTextChangeNotifying = false;
    
    public static final Color COLOR_BACKGROUND = new Color(255, 255, 64, 192);
    public static final Color COLOR_CHANGED_BACKGROUND = new Color(238, 156, 0, 192);

    public Pog()
    {
    }

    public Pog(Pog toCopy)
    {
        init(toCopy);
    }

    public void getUniqueID()
    {
        m_ID = g_nextID++;
    }

    public int getWidth()
    {
        return m_image.getWidth(m_canvas);
    }

    public int getHeight()
    {
        return m_image.getHeight(m_canvas);
    }

    public boolean isUnderlay()
    {
        return m_bIsUnderlay;
    }

    public void writeToPacket(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(m_fileName);
        dos.writeInt(getX());
        dos.writeInt(getY());
        dos.writeInt(m_faceSize);
        dos.writeInt(m_ID);
        dos.writeUTF(m_dataStr);
        dos.writeBoolean(m_bIsUnderlay);
    }

    public void initFromPacket(DataInputStream dis) throws IOException
    {
        m_fileName = dis.readUTF();
        int x = dis.readInt();
        int y = dis.readInt();
        m_position = new Point(x, y);
        m_faceSize = dis.readInt();
        m_ID = dis.readInt();
        m_dataStr = dis.readUTF();
        m_bIsUnderlay = dis.readBoolean();

        if (pogFileExists(m_fileName))
        {
            init(GametableFrame.getGametableFrame().m_gametableCanvas, m_fileName);
        }
        else
        {
            String fileToLoad = "assets/pog_unk_1.png";
            // the file doesn't exist. load up the default instead
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

            String properFilename = m_fileName;
            init(GametableFrame.getGametableFrame().m_gametableCanvas, fileToLoad);
            m_fileName = properFilename;
            m_bIsUnknownImage = true;
        }
    }

    public static boolean pogFileExists(String filename)
    {
        // if we're here we need to check for it in the pogs directory
        File pogFile = new File(filename);
        return pogFile.exists();
    }

    public void init(Pog orig)
    {
        setPosition(orig.getPosition());
        m_faceSize = orig.getFaceSize();
        m_image = orig.m_image;
        m_canvas = orig.m_canvas;
        m_fileName = orig.m_fileName;
        m_pixels = orig.m_pixels;
        m_bIsUnderlay = orig.m_bIsUnderlay;
        m_dataStr = new String(orig.m_dataStr);
    }

    public void init(GametableCanvas canvas, String fullSizeImagePath)
    {
    	m_image = UtilityFunctions.getImage(fullSizeImagePath);

        m_canvas = canvas;

        // note the size of the pog
        m_faceSize = m_image.getWidth(null) / GametableCanvas.BASE_SQUARE_SIZE;
        if (m_faceSize < 0)
        {
            m_faceSize = 1;
        }
        
        m_fileName = fullSizeImagePath;
        
        // prepare the hit pixels
        setUpPixels();
    }

    public void reaquireImages()
    {
        init(m_canvas, m_fileName);
    }

    public int getHeightForZoomLevel()
    {
    	int size = GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom);
    	double ratio = (double)size / (double)GametableCanvas.BASE_SQUARE_SIZE;
        int imgSizeY = (int)(ratio * m_image.getHeight(null));
        return imgSizeY;
    }

    public int getWidthForZoomLevel()
    {
    	int size = GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom);
    	double ratio = (double)size / (double)GametableCanvas.BASE_SQUARE_SIZE;
        int imgSizeX = (int)(ratio * m_image.getWidth(null));
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
        return m_faceSize;
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

    public void setUpPixels()
    {
        Image offscreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.OPAQUE);
        Graphics g = offscreen.getGraphics();
        g.setColor(new Color(0xff00ff));
        g.fillRect(0, 0, offscreen.getWidth(m_canvas), offscreen.getHeight(m_canvas));
        g.drawImage(m_image, 0, 0, m_canvas);

        // now grab the pixels
        m_pixels = new int[offscreen.getWidth(m_canvas) * offscreen.getHeight(m_canvas)];
        PixelGrabber pg = new PixelGrabber(offscreen, 0, 0, offscreen.getWidth(m_canvas),
            offscreen.getHeight(m_canvas), m_pixels, 0, offscreen.getWidth(m_canvas));
        try
        {
            pg.grabPixels();
        }
        catch (InterruptedException e)
        {
            Log.log(Log.SYS, e);
            Thread.currentThread().interrupt();
            return;
        }
    }

    public boolean modelPtInBounds(int x, int y)
    {
        // convert to our local coordinates
        int localX = x - getX();
        int localY = y - getY();

        // if it's not even in our rect, then forget it.
        if (localX < 0)
        {
            return false;
        }

        if (localX >= getWidth())
        {
            return false;
        }

        if (localY < 0)
        {
            return false;
        }

        if (localY >= getHeight())
        {
            return false;
        }

        int idx = localX + localY * getWidth();
        int c = m_pixels[idx];
        if ((c & 0x00ffffff) == 0xff00ff)
        {
            return false;
        }

        return true;
    }
    
    void displayPogDataChange()
    {
    	// we don't do this if the game is receiving inital data.
    	if ( PacketSourceState.isHostDumping() )
    	{
    		return;
    	}
    	
    	// we also don't do this if the game is loading a file from disk.
    	if ( PacketSourceState.isFileLoading() )
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
    	if ( !m_bTextChangeNotifying )
    	{
    		return;
    	}
    	drawStringToCanvas(g, true, COLOR_CHANGED_BACKGROUND);
    }

    public void draw(Graphics g, int x, int y, ImageObserver observer)
    {
        // which image should we use?
        Image toDraw = m_image;

        // draw it
        g.drawImage(toDraw, x, y, observer);
    }
    
    public void drawScaled(Graphics g, int x, int y)
    {
        // we have to work with ratios, cause the pog could be large or huge, gargantuan, etc.
        int size = GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom);
        double ratio = (double)size / (double)GametableCanvas.BASE_SQUARE_SIZE;
        int imgSizeX = (int)(ratio * m_image.getWidth(null));
        int imgSizeY = (int)(ratio * m_image.getHeight(null));

        // blit with scaling 
        g.drawImage(m_image, x, y, imgSizeX, imgSizeY, null);

        // g.drawImage(m_image, x, y, m_canvas);
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
    	// if we're tinted, draw tinted instead
    	if ( m_bTinted )
    	{
    		drawTintedToCanvas(g);
    		return;
    	}
    	
        // convert our model coordinates to draw coordinates
        Point drawCoords = m_canvas.modelToDraw(getPosition());

        drawScaled(g, drawCoords.x, drawCoords.y);
    }

    public void drawTintedToCanvas(Graphics g)
    {
        // convert our model coordinates to draw coordinates
        Point drawCoords = m_canvas.modelToDraw(getPosition());

        // draw it
        drawScaled(g, drawCoords.x, drawCoords.y);
        
        // now draw a green 50% alpha square over it
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2.setColor(Color.GREEN);
        int size = GametableCanvas.getSquareSizeForZoom(m_canvas.m_zoom);
        g2.fillRect(drawCoords.x, drawCoords.y, size, size);
        g2.dispose();
    }
    
    public void drawDataStringToCanvas(Graphics gr, boolean bForceTextInBounds)
    {
    	drawStringToCanvas(gr, bForceTextInBounds, COLOR_BACKGROUND);
    	stopDisplayPogDataChange();
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
