

package com.galactanet.gametable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;



public class PogsPanel extends JButton implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener
{
    public final static int MAX_FACE  = 5;

    public final static int GUTTER_X  = 8;
    public final static int GUTTER_Y  = 8;
    public final static int SPACING_X = 4;
    public final static int SPACING_Y = 4;



    public void reaquirePogs()
    {
        m_pogs = new Vector();
        m_underlays = new Vector();
        m_scrollY = 0;
        init(m_canvas, m_bIsPogsMode);
        repaint();
    }

    public void init(GametableCanvas canvas, boolean bPogsMode)
    {
        m_canvas = canvas;
        m_bIsPogsMode = bPogsMode;

        if (m_bIsPogsMode)
        {
            // look in the "pogs" directory for pogs
            File pogPath = new File("pogs");
            if (pogPath.exists())
            {
                String[] files = pogPath.list();

                for (int i = 0; i < files.length; i++)
                {
                    String filename = "pogs/" + files[i];
                    File test = new File(filename);

                    if (test.isFile())
                    {
                        try
                        {
                            Pog toAdd = new Pog();
                            toAdd.init(m_canvas, filename);

                            // add it to the appropriate size array
                            addPog(toAdd);
                        }
                        catch (Exception ex)
                        {
                            // any exceptions thrown in this process cancel
                            // the addition of that one pog.
                        }
                    }
                }
            }
        }
        else
        {
            // look in the "underlays" directory for underlays
            File underlayPath = new File("underlays");
            if (underlayPath.exists())
            {
                String[] files = underlayPath.list();

                for (int i = 0; i < files.length; i++)
                {
                    String filename = "underlays/" + files[i];
                    File test = new File(filename);

                    if (test.isFile())
                    {
                        try
                        {
                            Pog toAdd = new Pog();
                            toAdd.init(m_canvas, filename);

                            // add it to the appropriate size array
                            addUnderlay(toAdd);
                        }
                        catch (Exception ex)
                        {
                            // any exceptions thrown in this process cancel
                            // the addition of that one pog.
                        }
                    }
                }
            }
        }

        m_pogs = sort(m_pogs);
        m_underlays = sort(m_underlays);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);
        addKeyListener(m_canvas);
    }

    public Vector getPogs()
    {
        if (m_bIsPogsMode)
        {
            return m_pogs;
        }

        return m_underlays;
    }

    public void componentResized(ComponentEvent e)
    {
        boundScroll();
    }

    public void componentMoved(ComponentEvent e)
    {
    }

    public void componentShown(ComponentEvent e)
    {
    }

    public void componentHidden(ComponentEvent e)
    {
    }

    public void addPog(Pog toAdd)
    {
        toAdd.m_bIsUnderlay = false;
        m_pogs.add(toAdd);
    }

    public void addUnderlay(Pog toAdd)
    {
        toAdd.m_bIsUnderlay = true;
        m_underlays.add(toAdd);
    }

    public Vector sort(Vector toSort)
    {
        // sort the pogs by height
        Vector heightSortedPogs = new Vector();
        while (toSort.size() > 0)
        {
            // find the smallet height pog
            Pog smallestPog = null;
            for (int i = 0; i < toSort.size(); i++)
            {
                Pog pog = (Pog)toSort.elementAt(i);
                if (smallestPog == null)
                {
                    smallestPog = pog;
                }
                else
                {
                    if (pog.getHeight() < smallestPog.getHeight())
                    {
                        smallestPog = pog;
                    }
                }
            }

            heightSortedPogs.add(smallestPog);
            toSort.remove(smallestPog);
        }

        return heightSortedPogs;
    }

    public void paint(Graphics g)
    {
        g.setColor(new Color(0x734D22));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.translate(0, -m_scrollY);

        // draw all the pogs, flowing them left to right, top to bottom.
        // "line breaks" at size changes
        int x = GUTTER_X;
        int y = GUTTER_Y;

        int tallestThisLine = 0;

        for (int i = 0; i < getPogs().size(); i++)
        {
            Pog toDraw = (Pog)getPogs().elementAt(i);
            Pog nextToDraw = null;
            if (i < getPogs().size() - 1)
            {
                nextToDraw = (Pog)getPogs().elementAt(i + 1);
            }

            // draw the pog
            toDraw.draw(g, x, y, this);

            // note it's position
            toDraw.setLoc(x, y);

            // advance the x and y
            x += toDraw.getWidth();
            x += SPACING_X;

            if (toDraw.getHeight() > tallestThisLine)
            {
                tallestThisLine = toDraw.getHeight();
            }

            if (nextToDraw != null)
            {
                if (x + nextToDraw.getWidth() > getWidth())
                {
                    // the next one won't fit. drop down a level
                    x = GUTTER_X;
                    y += tallestThisLine;
                    y += SPACING_Y;
                    tallestThisLine = 0;
                }
            }
        }

        // calculate the total height in use
        if (x != GUTTER_X)
        {
            // we didn't happen to just finish a line, so...
            y += tallestThisLine;
            y += SPACING_Y;
            x = GUTTER_X;
        }

        m_height = y;

        if (m_bLDragging && !isHandMode() && m_selectedPog != null)
        {
            // they're dragging a pog around. draw it
            m_selectedPog.draw(g, m_dragX - m_pogDragMouseInsetX, m_dragY - m_pogDragMouseInsetY + m_scrollY, this);
        }

        g.translate(0, m_scrollY);
    }

    public void paintSwitch(Graphics g)
    {
        // paint the little switch in the corner

    }

    public void updateDragInfo(MouseEvent e)
    {
        m_dragX = e.getX();
        m_dragY = e.getY();

        if (isHandMode())
        {
            m_scrollY = m_prevScrollY + m_clickY - m_dragY;
        }
        else if (m_selectedPog != null)
        {
            // let the canvas know what's going on
            m_canvas.pogDrag();
        }
        boundScroll();
        repaint();
    }

    public void boundScroll()
    {
        if (m_scrollY + getHeight() > m_height)
        {
            m_scrollY = m_height - getHeight();
        }

        if (m_scrollY < 0)
        {
            m_scrollY = 0;
        }
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON3)
        {
            m_bRDragging = true;
        }

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            m_bLDragging = true;
        }

        updateToolState();

        m_clickX = e.getX();
        m_clickY = e.getY();
        m_prevScrollY = m_scrollY;

        int modelX = m_clickX;
        int modelY = m_clickY + m_scrollY;

        updateDragInfo(e);

        if (!isHandMode())
        {
            // they are clicking normally. They may be clicking a pog
            m_selectedPog = null;
            for (int j = 0; j < getPogs().size(); j++)
            {
                Pog check = (Pog)getPogs().elementAt(j);
                if (check.modelPtInBounds(modelX, modelY))
                {
                    // that's where they clicked
                    m_selectedPog = new Pog();
                    m_selectedPog.init(check);
                    m_pogDragMouseInsetX = modelX - m_selectedPog.getX();
                    m_pogDragMouseInsetY = modelY - m_selectedPog.getY();

                    // make them "pick it up" a little for the visual queue
                    m_pogDragMouseInsetX -= 5;
                    m_pogDragMouseInsetY += 5;

                    break;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        m_bRDragging = false;
        m_bLDragging = false;
        updateDragInfo(e);

        if (m_selectedPog != null)
        {
            m_canvas.pogDrop();
            m_selectedPog = null;
        }

        updateToolState();
        repaint();
    }

    public void mouseEntered(MouseEvent e)
    {
        updateToolState();
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mouseDragged(MouseEvent e)
    {
        updateDragInfo(e);
        repaint();
    }

    public void mouseMoved(MouseEvent e)
    {
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        int nextScrollUpDist = GametableCanvas.BASE_SQUARE_SIZE * 10;
        int nextScrollDownDist = GametableCanvas.BASE_SQUARE_SIZE * 10;

        for (int i = 0; i < MAX_FACE; i++)
        {
            for (int j = 0; j < getPogs().size(); j++)
            {
                Pog pog = (Pog)getPogs().elementAt(j);

                int distAbove = m_scrollY - pog.m_y;
                if (distAbove > 0)
                {
                    if (distAbove < nextScrollUpDist)
                    {
                        nextScrollUpDist = distAbove;
                    }
                }

                int distBelow = pog.m_y - m_scrollY;
                if (distBelow > 0)
                {
                    if (distBelow < nextScrollDownDist)
                    {
                        nextScrollDownDist = distBelow;
                    }
                }
            }
        }

        if (e.getWheelRotation() < 0)
        {
            // scroll up
            m_scrollY -= nextScrollUpDist;
        }
        else if (e.getWheelRotation() > 0)
        {
            // scroll down
            m_scrollY += nextScrollDownDist;
        }

        boundScroll();

        repaint();
    }

    public void updateToolState()
    {
        // if we have a drag in progress, we're not willing to change our mode
        m_bIsHandMode = false;
        if (m_bRDragging)
        {
            m_bIsHandMode = true;
        }
        setCursor();
    }

    public void setCursor()
    {
        if (isHandMode())
        {
            setCursor(m_canvas.m_handCursor);
        }
        else
        {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public boolean isHandMode()
    {
        return m_bIsHandMode;
    }



    Vector          m_pogs      = new Vector();
    Vector          m_underlays = new Vector();
    GametableCanvas m_canvas;

    boolean         m_bIsHandMode;
    boolean         m_bLDragging;
    boolean         m_bRDragging;

    // scrolling and whatnot
    int             m_scrollY;
    int             m_height;

    int             m_clickX;
    int             m_clickY;
    int             m_prevScrollY;
    int             m_dragX;
    int             m_dragY;

    // pog drag
    Pog             m_selectedPog;
    int             m_pogDragMouseInsetX;
    int             m_pogDragMouseInsetY;

    boolean         m_bIsPogsMode;
}
