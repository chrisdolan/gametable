

package com.galactanet.gametable;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;


public class Pog
{
    public static int       g_nextID          = 10;
    public static final Font FONT              = new Font("Arial", 0, 14);

    private Image[]          m_images          = new Image[GametableCanvas.NUM_ZOOM_LEVELS];
    private int              m_pixels[];
    private GametableCanvas  m_canvas;

    // model coordinates
    public int              m_x;
    public int              m_y;

    // size
    private int              m_face;
    public String           m_fileName;

    public  String                   m_dataStr         = "";

    public int               m_ID              = 0;
    public boolean           m_bIsUnderlay     = false;
    public boolean           m_bIsUnknownImage = false;



    public Pog()
    {
    }

    public void getUniqueID()
    {
        m_ID = g_nextID++;
    }

    public int getWidth()
    {
        return m_images[0].getWidth(m_canvas);
    }

    public int getHeight()
    {
        return m_images[0].getHeight(m_canvas);
    }

    public boolean isUnderlay()
    {
        return m_bIsUnderlay;
    }

    public void writeToPacket(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(m_fileName);
        dos.writeInt(m_x);
        dos.writeInt(m_y);
        dos.writeInt(m_face);
        dos.writeInt(m_ID);
        dos.writeUTF(m_dataStr);
        dos.writeBoolean(m_bIsUnderlay);
    }

    public void initFromPacket(DataInputStream dis) throws IOException
    {
        m_fileName = dis.readUTF();
        m_x = dis.readInt();
        m_y = dis.readInt();
        m_face = dis.readInt();
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
            switch (m_face)
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
        m_x = orig.m_x;
        m_y = orig.m_y;
        m_face = orig.m_face;

        for (int i = 0; i < GametableCanvas.NUM_ZOOM_LEVELS; i++)
        {
            m_images[i] = orig.m_images[i];
        }

        m_canvas = orig.m_canvas;
        m_fileName = orig.m_fileName;
        m_pixels = orig.m_pixels;
        m_bIsUnderlay = orig.m_bIsUnderlay;
    }

    public void init(GametableCanvas canvas, String fullSizeImagePath)
    {
        Image img = UtilityFunctions.getImage(fullSizeImagePath);
        init(canvas, img);
        m_fileName = fullSizeImagePath;
    }

    public void reaquireImages()
    {
        init(m_canvas, m_fileName);
    }

    private void init(GametableCanvas canvas, Image fullSizeImage)
    {
        m_canvas = canvas;

        // note the size of the pog
        m_face = fullSizeImage.getWidth(null) / GametableCanvas.BASE_SQUARE_SIZE;
        if (m_face < 0)
        {
            m_face = 1;
        }

        // we have to make several scaled versions of this image
        // for various zoom levels
        m_images[0] = fullSizeImage; // that one's easy. :)

        for (int i = 1; i < GametableCanvas.NUM_ZOOM_LEVELS; i++)
        {
            // we have to work with ratios, cause the pog could be large or huge, gargantuan, etc.
            int size = m_canvas.getSquareSizeForZoom(i);
            double ratio = (double)size / (double)GametableCanvas.BASE_SQUARE_SIZE;
            int imgSizeX = (int)(ratio * fullSizeImage.getWidth(null));
            int imgSizeY = (int)(ratio * fullSizeImage.getHeight(null));

            Image offscreenImg = createBI(imgSizeX, imgSizeY);
            Graphics g = offscreenImg.getGraphics();

            // blit with scaling to the offscreen
            g.drawImage(fullSizeImage, 0, 0, imgSizeX, imgSizeY, null);

            // put it in the array
            m_images[i] = offscreenImg;
        }

        // set up our internal storage of the pixels
        // (for point collisiont detection)
        setUpPixels();
    }

    public void setLoc(int x, int y)
    {
        m_x = x;
        m_y = y;
    }

    public int getX()
    {
        return m_x;
    }

    public int getY()
    {
        return m_y;
    }

    public int getFace()
    {
        return m_face;
    }

    public void setUpPixels()
    {
        Image offscreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.OPAQUE);
        Graphics g = offscreen.getGraphics();
        g.setColor(new Color(0xff00ff));
        g.fillRect(0, 0, offscreen.getWidth(m_canvas), offscreen.getHeight(m_canvas));
        g.drawImage(m_images[0], 0, 0, m_canvas);

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
            e.printStackTrace();
        }
    }

    public boolean modelPtInBounds(int x, int y)
    {
        // convert to our local coordinates
        int localX = x - m_x;
        int localY = y - m_y;

        // if it's not even in our rect, then forget it.
        if (localX < 0)
            return false;
        if (localX >= getWidth())
            return false;
        if (localY < 0)
            return false;
        if (localY >= getHeight())
            return false;

        int idx = localX + localY * getWidth();
        int c = m_pixels[idx];
        if ((c & 0x00ffffff) == 0xff00ff)
        {
            return false;
        }

        return true;
    }

    public void draw(Graphics g, int x, int y, ImageObserver observer)
    {
        // which image should we use?
        Image toDraw = m_images[0];

        // draw it
        g.drawImage(toDraw, x, y, observer);
    }

    public void drawToCanvas(Graphics g)
    {
        // convert our model coordinates to draw coordinates
        Point drawCoords = m_canvas.modelToDraw(m_x, m_y);

        // which image should we use?
        Image toDraw = m_images[m_canvas.m_zoom];

        // draw it
        g.drawImage(toDraw, drawCoords.x, drawCoords.y, m_canvas);
    }

    public void drawDataStringToCanvas(Graphics g, boolean bForceTextInBounds)
    {
        if (m_dataStr == null)
        {
            return;
        }

        if (m_dataStr.length() == 0)
        {
            return;
        }

        FontMetrics metrics = g.getFontMetrics();
        LineMetrics lineMetrics = metrics.getLineMetrics(m_dataStr, g);
        int height = (int)lineMetrics.getHeight();
        int width = metrics.stringWidth(m_dataStr);

        int totalWidth = width + 6;
        int totalHeight = height + 6;

        Rectangle backgroundRect = new Rectangle();
        Point pogDrawCoords = m_canvas.modelToDraw(m_x, m_y);
        int viewWidth = m_images[m_canvas.m_zoom].getHeight(m_canvas);
        backgroundRect.x = pogDrawCoords.x + (viewWidth - totalWidth) / 2;
        backgroundRect.y = pogDrawCoords.y - totalHeight - 4;
        backgroundRect.width = totalWidth;
        backgroundRect.height = totalHeight;

        if (bForceTextInBounds)
        {
            // force it to be on the view
            if (backgroundRect.x < m_canvas.m_scrollX)
            {
                backgroundRect.x = m_canvas.m_scrollX;
            }

            if (backgroundRect.y < m_canvas.m_scrollY)
            {
                backgroundRect.y = m_canvas.m_scrollY;
            }

            if (backgroundRect.x + totalWidth > m_canvas.m_scrollX + m_canvas.getWidth())
            {
                backgroundRect.x = m_canvas.m_scrollX + m_canvas.getWidth() - totalWidth;
            }

            if (backgroundRect.y + totalHeight > m_canvas.m_scrollY + m_canvas.getHeight())
            {
                backgroundRect.y = m_canvas.m_scrollY + m_canvas.getHeight() - totalHeight;
            }
        }

        g.setColor(new Color(255, 255, 0, 204));
        g.fillRect(backgroundRect.x, backgroundRect.y, backgroundRect.width, backgroundRect.height);
        g.setColor(new Color(32, 32, 0, 204));
        g.drawRect(backgroundRect.x, backgroundRect.y, backgroundRect.width, backgroundRect.height);

        int stringX = backgroundRect.x + 3;
        int stringY = backgroundRect.y + 3 + metrics.getAscent();

        g.setColor(Color.BLACK);
        g.drawString(m_dataStr, stringX, stringY);
    }

    protected BufferedImage createBI(int width, int height)
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
            .createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }
}
