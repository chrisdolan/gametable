/*
 * GametableCanvas.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import com.galactanet.gametable.tools.NullTool;


/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class GametableCanvas extends JButton implements MouseListener, MouseMotionListener, KeyListener,
    ComponentListener, ItemSelectable, MouseWheelListener
{
    public final static int     VALUE_X               = 0;
    public final static int     VALUE_Y               = 1;

    public final static int     SNAP_DISTANCE         = 8;

    // drag modes
    public final static int     TOOL_MODE_ARROW       = 0;
    public final static int     TOOL_MODE_POINT       = 1;
    public final static int     TOOL_MODE_PEN         = 2;
    public final static int     TOOL_MODE_HAND        = 3;
    public final static int     TOOL_MODE_ERASER      = 4;
    public final static int     TOOL_MODE_LINE        = 5;

    public final static int     NUM_POINT_CURSORS     = 8;
    public final static int     POINT_CURSOR_OFFSET_X = 5;
    public final static int     POINT_CURSOR_OFFSET_Y = 6;

    // the size of a square at max zoom level (0)
    public final static int     BASE_SQUARE_SIZE      = 64;

    public final static int     NUM_ZOOM_LEVELS       = 5;

    public Image                m_mapBk;

    // the size of a square at the current zoom level
    public int                  m_squareSize;

    // this is the map (or layer) that all players share
    private GametableMap        m_sharedMap           = new GametableMap(true);

    // this points to whichever map is presently active
    private GametableMap        m_activeMap;

    // some cursors
    Cursor                      m_handCursor;
    Cursor                      m_penCursor;
    Cursor                      m_emptyCursor;
    Cursor                      m_eraserCursor;
    Image[]                     m_pointCursorImages   = new Image[NUM_POINT_CURSORS];

    // the frame
    private GametableFrame      m_gametableFrame;

    // zoom and top-left state

    // This is the number of screen pixels that are
    // used per model pixel. It's never less than 1
    public int                  m_zoom;

    // hand tool
    int                         m_handToolStartX;
    int                         m_handToolStartY;
    int                         m_scrollStartX;
    int                         m_scrollStartY;

    // misc flags
    boolean                     m_bSpaceKeyDown;
    boolean                     m_bShiftKeyDown;
    boolean                     m_bControlKeyDown;

    // drag stuff
    // where they first clicked
    int                         m_clickX;
    int                         m_clickY;

    // where the mouse is now
    int                         m_dragX;
    int                         m_dragY;

    // hand tool
    int                         m_preClickScrollX;
    int                         m_preClickScrollY;

    // Pen tool
    PenAsset                    m_penAsset;

    boolean                     m_bLDragging;
    boolean                     m_bRDragging;
    boolean                     m_bNeedToDrawPointCursor;
    int                         m_toolMode;

    Image                       m_offscreen;
    int                         m_offscreenX;
    int                         m_offscreenY;

    boolean                     m_bPogBeingDragged;
    Pog                         m_pogBeingDragged;
    int                         m_pogDragInsetX;
    int                         m_pogDragInsetY;

    int                         m_currentMouseX;
    int                         m_currentMouseY;
    boolean                     m_bMouseOnView;

    Pog                         m_pogMouseOver;

    private int                 m_activeToolId        = -1;
    private static final Tool   NULL_TOOL             = new NullTool();
    public static final boolean NEW_TOOL              = false;



    public GametableCanvas()
    {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addComponentListener(this);

        m_activeMap = m_sharedMap;
    }

    public void init(GametableFrame frame)
    {
        m_gametableFrame = frame;
        m_mapBk = UtilityFunctions.getImage("assets/mapbk.png");

        if (!NEW_TOOL)
        {
            m_handCursor = createCursor("assets/hand.png", 8, 8);
            m_penCursor = createCursor("assets/pen.png", 0, 14);
            m_emptyCursor = createCursor("assets/emptyCurs.png", 0, 0);
            m_eraserCursor = createCursor("assets/eraser.png", 7, 4);
        }

        m_pointCursorImages[0] = UtilityFunctions.getImage("assets/whiteHand.png");
        m_pointCursorImages[1] = UtilityFunctions.getImage("assets/brownHand.png");
        m_pointCursorImages[2] = UtilityFunctions.getImage("assets/purpleHand.png");
        m_pointCursorImages[3] = UtilityFunctions.getImage("assets/blueHand.png");
        m_pointCursorImages[4] = UtilityFunctions.getImage("assets/redHand.png");
        m_pointCursorImages[5] = UtilityFunctions.getImage("assets/greenHand.png");
        m_pointCursorImages[6] = UtilityFunctions.getImage("assets/greyHand.png");
        m_pointCursorImages[7] = UtilityFunctions.getImage("assets/yellowHand.png");

        setPrimaryScroll(m_sharedMap, 0, 0);

        addMouseWheelListener(this);
        setZoom(0);
        if (NEW_TOOL)
        {
            setActiveTool(0);
        }
    }

    public int getModifierFlags()
    {
        return ((m_bControlKeyDown ? Tool.MODIFIER_CTRL : 0) | (m_bSpaceKeyDown ? Tool.MODIFIER_SPACE : 0) | (m_bShiftKeyDown ? Tool.MODIFIER_SHIFT
            : 0));
    }

    public void setToolCursor(int index)
    {
        setCursor(m_gametableFrame.getToolManager().getToolInfo(m_activeToolId).getCursor(index));
    }

    public void setActiveTool(int index)
    {
        Tool oldTool = getActiveTool();
        oldTool.deactivate();

        m_activeToolId = index;

        Tool tool = getActiveTool();
        tool.activate(this);
        setToolCursor(0);
    }

    public Tool getActiveTool()
    {
        if (m_activeToolId < 0)
        {
            return NULL_TOOL;
        }
        return m_gametableFrame.getToolManager().getToolInfo(m_activeToolId).getTool();
    }

    public GametableMap getSharedMap()
    {
        return m_sharedMap;
    }

    public GametableMap getActiveMap()
    {
        return m_activeMap;
    }

    private Cursor createCursor(String pngFile, int cx, int cy)
    {
        Image img = UtilityFunctions.getImage(pngFile);
        Cursor ret = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(cx, cy), pngFile);
        return ret;
    }

    public PogsPanel getActivePogsArea()
    {
        PogsPanel ret = (PogsPanel)m_gametableFrame.jTabbedPane1.getSelectedComponent();
        return ret;
    }

    // conversion from pogsview coords
    public Point pogsViewToCanvasView(int x, int y)
    {
        x -= getActivePogsArea().getWidth();
        x -= m_gametableFrame.jSplitPane2.getDividerSize();
        y += getActivePogsArea().getY();
        return new Point(x, y);
    }

    public int getSquareSizeForZoom(int level)
    {
        int ret = BASE_SQUARE_SIZE;
        switch (level)
        {
            case 0:
            {
                ret = BASE_SQUARE_SIZE;
            }
                break;

            case 1:
            {
                ret = (BASE_SQUARE_SIZE / 4) * 3;
            }
                break;

            case 2:
            {
                ret = BASE_SQUARE_SIZE / 2;
            }
                break;

            case 3:
            {
                ret = BASE_SQUARE_SIZE / 4;
            }
                break;

            case 4:
            {
                ret = BASE_SQUARE_SIZE / 8;
            }
                break;
        }

        return ret;
    }

    public void setZoom(int zoomLevel)
    {
        m_zoom = zoomLevel;

        if (m_zoom < 0)
        {
            m_zoom = 0;
        }

        if (m_zoom >= NUM_ZOOM_LEVELS)
        {
            m_zoom = NUM_ZOOM_LEVELS - 1;
        }

        m_squareSize = getSquareSizeForZoom(m_zoom);
    }

    // returns a good line width to draw things
    public int getLineStrokeWidth()
    {
        switch (m_zoom)
        {
            case 0:
            {
                return 3;
            }

            case 1:
            {
                return 2;
            }

            case 2:
            {
                return 2;
            }

            case 3:
            {
                return 1;
            }

            default:
            {
                return 1;
            }
        }
    }

    // conversion from model to view coordinates
    public Point modelToDraw(Point modelPoint)
    {
        double squaresX = (double)modelPoint.x / (double)BASE_SQUARE_SIZE;
        double squaresY = (double)modelPoint.y / (double)BASE_SQUARE_SIZE;

        int viewX = (int)Math.round(squaresX * m_squareSize);
        int viewY = (int)Math.round(squaresY * m_squareSize);

        return new Point(viewX, viewY);
    }

    public Point modelToDraw(int modelX, int modelY)
    {
        return modelToDraw(new Point(modelX, modelY));
    }

    public Point modelToView(Point modelPoint)
    {
        double squaresX = (double)modelPoint.x / (double)BASE_SQUARE_SIZE;
        double squaresY = (double)modelPoint.y / (double)BASE_SQUARE_SIZE;

        int viewX = (int)Math.round(squaresX * m_squareSize);
        int viewY = (int)Math.round(squaresY * m_squareSize);

        viewX -= getActiveMap().getScrollX();
        viewY -= getActiveMap().getScrollY();

        return new Point(viewX, viewY);
    }

    public Point modelToView(int modelX, int modelY)
    {
        return modelToView(new Point(modelX, modelY));
    }

    public Point drawToModel(Point drawPoint)
    {
        double squaresX = (double)(drawPoint.x) / (double)m_squareSize;
        double squaresY = (double)(drawPoint.y) / (double)m_squareSize;

        int modelX = (int)(squaresX * BASE_SQUARE_SIZE);
        int modelY = (int)(squaresY * BASE_SQUARE_SIZE);

        return new Point(modelX, modelY);
    }

    public Point drawToModel(int modelX, int modelY)
    {
        return drawToModel(new Point(modelX, modelY));
    }

    public Point viewToModel(Point viewPoint)
    {
        double squaresX = (double)(viewPoint.x + getActiveMap().getScrollX()) / (double)m_squareSize;
        double squaresY = (double)(viewPoint.y + getActiveMap().getScrollY()) / (double)m_squareSize;

        int modelX = (int)(squaresX * BASE_SQUARE_SIZE);
        int modelY = (int)(squaresY * BASE_SQUARE_SIZE);

        return new Point(modelX, modelY);
    }

    public Point viewToModel(int viewX, int viewY)
    {
        return viewToModel(new Point(viewX, viewY));
    }

    /** *********************************************************** */
    // MouseListener/MouseMotionListener overrides:
    /** *********************************************************** */
    public void mouseDragged(MouseEvent e)
    {
        mouseMoved(e);
        if (!NEW_TOOL)
        {
            updateDrag(e);
        }
    }

    public void mouseMoved(MouseEvent e)
    {
        if (NEW_TOOL)
        {
            Point modelClick = viewToModel(e.getX(), e.getY());
            getActiveTool().mouseMoved(modelClick.x, modelClick.y, getModifierFlags());
        }
        else
        {
            if (m_bSpaceKeyDown)
            {
                // if we're pointing, we're not interested mouse updates
                return;
            }

            m_currentMouseX = e.getX();
            m_currentMouseY = e.getY();

            Pog prevPog = m_pogMouseOver;
            m_pogMouseOver = getPogMouseOver();

            if (prevPog != m_pogMouseOver)
            {
                repaint();
            }
        }
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        Point modelClick = viewToModel(e.getX(), e.getY());
        if (NEW_TOOL)
        {
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                getActiveTool().mouseButtonPressed(modelClick.x, modelClick.y, getModifierFlags());
            }
        }
        else
        {

            if (m_bSpaceKeyDown)
            {
                // if we're pointing, we're not interested in clicks
                return;
            }

            if (e.getButton() == MouseEvent.BUTTON3)
            {
                m_bRDragging = true;
            }

            if (e.getButton() == MouseEvent.BUTTON1)
            {
                m_bLDragging = true;
            }

            m_clickX = e.getX();
            m_clickY = e.getY();

            m_preClickScrollX = getActiveMap().getScrollX();
            m_preClickScrollY = getActiveMap().getScrollY();

            switch (m_toolMode)
            {
                case TOOL_MODE_PEN:
                    m_penAsset = new PenAsset();
                    m_penAsset.init(m_gametableFrame.m_drawColor);
                    break;
                case TOOL_MODE_ARROW:
                    if (m_bLDragging && m_bShiftKeyDown)
                    {
                        // set the current pog's data str
                        if (setCurrentPogData())
                        {
                            return;
                        }
                    }

                    // see what pog they hit, if any
                    // work backward through the pog vector, to get the
                    // one drawn last (topmost)
                    m_pogBeingDragged = getActiveMap().getPogAt(modelClick);
                    if (m_pogBeingDragged != null)
                    {
                        m_pogDragInsetX = modelClick.x - m_pogBeingDragged.getX();
                        m_pogDragInsetY = modelClick.y - m_pogBeingDragged.getY();
                    }
                    break;
            }

            updateToolState();
            updateDrag(e);

            if (m_toolMode == TOOL_MODE_LINE)
            {
                // if they aren't holding Control when they click, the line will
                // snap to the vertex
                checkSnap();
                m_clickX = m_dragX;
                m_clickY = m_dragY;
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        Point modelClick = viewToModel(e.getX(), e.getY());
        if (NEW_TOOL)
        {
            getActiveTool().mouseButtonReleased(modelClick.x, modelClick.y, getModifierFlags());
        }
        else
        {
            updateDrag(e);

            m_bRDragging = false;
            m_bLDragging = false;

            Point modelStart = viewToModel(m_clickX, m_clickY);
            Point modelEnd = viewToModel(m_dragX, m_dragY);

            switch (m_toolMode)
            {
                case TOOL_MODE_PEN:
                {
                    // get the line segments and add them in
                    m_penAsset.smooth();
                    LineSegment[] lines = m_penAsset.getLineSegments();
                    m_penAsset = null;

                    if (lines != null)
                    {
                        addLineSegments(lines);
                    }
                }
                    break;

                case TOOL_MODE_ERASER:
                {
                    // erase stuff. Lots of segments will probably
                    // be deleted. So we make a new container to hold the
                    // survivors
                    Rectangle r = new Rectangle(modelStart.x, modelStart.y, modelEnd.x - modelStart.x, modelEnd.y
                        - modelStart.y);
                    if (m_gametableFrame.m_colorEraserButton.isSelected())
                    {
                        // they're doing a color erase
                        erase(r, true, m_gametableFrame.m_drawColor.getRGB());
                    }
                    else
                    {
                        // full erase. clear everything.
                        erase(r, false, 0);
                    }
                }
                    break;

                case TOOL_MODE_LINE:
                {
                    // easy. They made a line. We add it to the lines vector
                    LineSegment ls = new LineSegment(modelStart, modelEnd, m_gametableFrame.m_drawColor);
                    LineSegment[] lines = new LineSegment[1];
                    lines[0] = ls;
                    addLineSegments(lines);
                }
                    break;

                case TOOL_MODE_ARROW:
                {
                    if (m_pogBeingDragged != null)
                    {
                        if (!pogInViewport(m_pogBeingDragged))
                        {
                            // they removed this pog
                            int removeArray[] = new int[1];
                            removeArray[0] = m_pogBeingDragged.m_ID;
                            removePogs(removeArray);
                        }
                        else
                        {
                            movePog(m_pogBeingDragged.m_ID, m_pogBeingDragged.getX(), m_pogBeingDragged.getY());
                        }
                    }
                    m_pogBeingDragged = null;
                }
                    break;
            }

            updateToolState();
        }
    }

    public void mouseEntered(MouseEvent e)
    {
        updateToolState();
        m_bMouseOnView = true;
        // requestFocus();
    }

    public void mouseExited(MouseEvent e)
    {
        m_bMouseOnView = false;
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (e.getWheelRotation() < 0)
        {
            // zoom in
            centerZoom(-1);
        }
        else if (e.getWheelRotation() > 0)
        {
            // zoom out
            centerZoom(1);
        }
        repaint();
    }

    // returns true if a pog was hit
    public boolean setCurrentPogData()
    {
        m_currentMouseX = m_clickX;
        m_currentMouseY = m_clickY;
        Pog pog = getPogMouseOver();
        if (pog == null)
        {
            return false;
        }

        // we have poggage. Get the new name for it
        String s = (String)JOptionPane.showInputDialog(m_gametableFrame, "Enter new Pog text:", "Pog Text",
            JOptionPane.PLAIN_MESSAGE, null, null, pog.m_dataStr);

        if (s != null)
        {
            setPogData(pog.m_ID, s);
        }

        m_bLDragging = false;
        m_bRDragging = false;
        m_bShiftKeyDown = false;
        m_bControlKeyDown = false;

        return true;
    }

    public Pog getPogByID(int id)
    {
        for (int i = 0; i < getActiveMap().getNumPogs(); i++)
        {
            Pog pog = getActiveMap().getPogAt(i);
            if (pog.m_ID == id)
            {
                return pog;
            }
        }
        return null;
    }

    public void recenterView(int modelCenterX, int modelCenterY, int zoomLevel)
    {
        m_gametableFrame.push(PacketManager.makeRecenterPacket(modelCenterX, modelCenterY, zoomLevel));

        if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
        {
            doRecenterView(modelCenterX, modelCenterY, zoomLevel);
        }
    }

    /*
     * This function will set the scroll for all maps, keeping their relative offsets preserved. The
     * x,y values sent in will become the scroll values for the desired map. All others maps will
     * preserve offsets from that.
     */
    public void setPrimaryScroll(GametableMap mapToSet, int x, int y)
    {
        int dx = x - mapToSet.getScrollX();
        int dy = y - mapToSet.getScrollY();
        m_sharedMap.setScroll(dx + mapToSet.getScrollX(), dy + mapToSet.getScrollY());
    }

    public void doRecenterView(int modelCenterX, int modelCenterY, int zoomLevel)
    {
        // make the sent in x and y our center, ad the sent in zoom.
        // So start with the zoom
        setZoom(zoomLevel);

        // find the view coordinate for the model center
        setPrimaryScroll(m_sharedMap, 0, 0);

        // we need to get the coords for the shared map, even if we're not on that at the moment
        // so we cheezily set to the shared map, then return it to normal after the
        // call to modelToView
        GametableMap storedMap = m_activeMap;
        m_activeMap = m_sharedMap;
        Point viewCenter = modelToView(modelCenterX, modelCenterY);
        m_activeMap = storedMap;

        // find where the top left would have to be, based on our size
        int tlX = viewCenter.x - getWidth() / 2;
        int tlY = viewCenter.y - getHeight() / 2;

        // that is our new scroll position
        setPrimaryScroll(m_sharedMap, tlX, tlY);

        repaint();
    }

    public void setPogData(int id, String s)
    {
        m_gametableFrame.push(PacketManager.makePogDataPacket(id, s));

        if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
        {
            doSetPogData(id, s);
        }
    }

    public void doSetPogData(int id, String s)
    {
        Pog pog = getPogByID(id);
        if (pog == null)
        {
            return;
        }

        pog.m_dataStr = s;
        repaint();
    }

    public void movePog(int id, int newX, int newY)
    {
        m_gametableFrame.push(PacketManager.makeMovePogPacket(id, newX, newY));

        if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
        {
            doMovePog(id, newX, newY);
        }
    }

    public void doMovePog(int id, int newX, int newY)
    {
        Pog toMove = getPogByID(id);
        if (toMove == null)
        {
            return;
        }

        toMove.setPosition(newX, newY);

        // this pog moves to the end of the array
        getActiveMap().removePog(toMove);
        getActiveMap().addPog(toMove);

        repaint();
    }

    public void removePog(int id)
    {
        int removeArray[] = new int[1];
        removeArray[0] = id;
        removePogs(removeArray);
    }

    public void removePogs(int ids[])
    {
        m_gametableFrame.push(PacketManager.makeRemovePogsPacket(ids));

        if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
        {
            doRemovePogs(ids);
        }
    }

    public void doRemovePogs(int ids[])
    {
        for (int i = 0; i < ids.length; i++)
        {
            doRemovePog(ids[i]);
        }
    }

    public void doRemovePog(int id)
    {
        Pog toRemove = getPogByID(id);
        if (toRemove != null)
        {
            getActiveMap().removePog(toRemove);
        }

        // if they were dragging that pog, stop it
        if (m_bLDragging && m_pogBeingDragged != null && m_pogBeingDragged.m_ID == id)
        {
            m_pogBeingDragged = null;
            m_gametableFrame.logSystemMessage("The pog you were holding disappears out of your hands!");
        }
        repaint();
    }

    public void addPog(Pog toAdd)
    {
        toAdd.getUniqueID();
        m_gametableFrame.push(PacketManager.makeAddPogPacket(toAdd));

        if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
        {
            doAddPog(toAdd);
        }
    }

    public void doAddPog(Pog toAdd)
    {
        getActiveMap().addPog(toAdd);
        repaint();
    }

    public void addLineSegments(LineSegment[] lines)
    {
        // if we're the host, push it to everyone and add the lines.
        // if we're a joiner, just push it to the host
        m_gametableFrame.push(PacketManager.makeLinesPacket(lines));

        // if we're the host or if we're offline, go ahead and add them now
        if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
        {
            doAddLineSegments(lines);
        }
    }

    public void doAddLineSegments(LineSegment[] lines)
    {
        if (lines != null)
        {
            for (int i = 0; i < lines.length; i++)
            {
                getActiveMap().addLine(lines[i]);
            }
        }
        repaint();
    }

    public void erase(Rectangle r, boolean bColorSpecific, int color)
    {
        // if we're the host, push it to everyone and add the lines.
        // if we're a joiner, just push it to the host
        m_gametableFrame.push(PacketManager.makeErasePacket(r, bColorSpecific, color));
        if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
        {
            doErase(r, bColorSpecific, color);
        }

    }

    public void doErase(Rectangle r, boolean bColorSpecific, int color)
    {
        Point modelStart = new Point(r.x, r.y);
        Point modelEnd = new Point(r.x + r.width, r.y + r.height);

        ArrayList survivingLines = new ArrayList();
        for (int i = 0; i < getActiveMap().getNumLines(); i++)
        {
            LineSegment ls = getActiveMap().getLineAt(i);

            if (!bColorSpecific || ls.getColor().getRGB() == color)
            {
                // we are the color being erased, or we're in erase all
                // mode
                LineSegment[] result = ls.crop(modelStart, modelEnd);

                if (result != null)
                {
                    // this line segment is still alive
                    for (int j = 0; j < result.length; j++)
                    {
                        survivingLines.add(result[j]);
                    }
                }
            }
            else
            {
                // we are not affected by this erasing because we
                // aren't the color being erased.
                survivingLines.add(ls);
            }
        }

        // now we have just the survivors
        // replace all the lines with this list
        getActiveMap().clearLines();
        for (int i = 0; i < survivingLines.size(); i++)
        {
            getActiveMap().addLine((LineSegment)survivingLines.get(i));
        }
        repaint();
    }

    // called by the pogs area when a pog is being dragged
    public void pogDrag()
    {
        m_bPogBeingDragged = true;
        updatePogDropLoc();

        repaint();
    }

    public void pogDrop()
    {
        m_bPogBeingDragged = false;
        updatePogDropLoc();

        // only add the pog if it's in the viewport
        if (pogInViewport(getActivePogsArea().m_selectedPog))
        {
            // add this pog to the list
            addPog(getActivePogsArea().m_selectedPog);
        }

        // make the arrow the current tool
        m_gametableFrame.m_arrowButton.setSelected(true);
        updateToolState();
    }

    public boolean isPointVisible(Point modelPoint)
    {
        Point portalTL = viewToModel(0, 0);
        Point portalBR = viewToModel(getWidth(), getHeight());
        if (modelPoint.x > portalBR.x)
        {
            return false;
        }

        if (modelPoint.y > portalBR.y)
        {
            return false;
        }

        if (modelPoint.x < portalTL.x)
        {
            return false;
        }

        if (modelPoint.y < portalTL.y)
        {
            return false;
        }
        return true;
    }

    public boolean pogInViewport(Pog pog)
    {
        // only add the pog if they dropped it in the visible area
        int width = pog.getFaceSize() * BASE_SQUARE_SIZE;

        // get the model coords of the viewable area
        Point portalTL = viewToModel(0, 0);
        Point portalBR = viewToModel(getWidth(), getHeight());

        if (pog.getX() > portalBR.x)
        {
            return false;
        }
        if (pog.getY() > portalBR.y)
        {
            return false;
        }
        if (pog.getX() + width < portalTL.x)
        {
            return false;
        }
        if (pog.getY() + width < portalTL.y)
        {
            return false;
        }
        return true;
    }

    public void updatePogDropLoc()
    {
        int pogViewX = getActivePogsArea().m_dragX;
        int pogViewY = getActivePogsArea().m_dragY;

        // convert to our coordinates
        Point canvasView = pogsViewToCanvasView(pogViewX, pogViewY);

        // now convert to model coordinates
        Point canvasModel = viewToModel(canvasView.x, canvasView.y);

        // now, snap to grid if they don't have the control key down

        // massage the pog location
        m_pogDragInsetX = getActivePogsArea().m_pogDragMouseInsetX;
        m_pogDragInsetY = getActivePogsArea().m_pogDragMouseInsetY;
        getActivePogsArea().m_selectedPog.setPosition(canvasModel.x - m_pogDragInsetX, canvasModel.y - m_pogDragInsetY);

        // this function is only called when we're dragging from the pogs
        // area. So we have to cheexe our "current Mouse" coordinates
        m_currentMouseX = canvasView.x;
        m_currentMouseY = canvasView.y;

        if (!m_bControlKeyDown)
        {
            snapPogToGrid(getActivePogsArea().m_selectedPog);
        }
    }

    public void snapPogToGrid(Pog pog)
    {
        pog.setPosition(getSnap(pog.getX()), getSnap(pog.getY()));
    }

    public void scrollMapTo(int modelX, int modelY)
    {
        Point target = modelToDraw(modelX, modelY);
        System.out.println("scrollMapTo(" + target.x + ", " + target.y + ")");
        m_sharedMap.setScroll(target.x, target.y);
        repaint();
    }

    public void updateDrag(MouseEvent e)
    {
        updateLocation(e);

        // if they're doing a hand drag, we respons immediately
        if (m_toolMode == TOOL_MODE_HAND)
        {
            int scrX = m_preClickScrollX + m_clickX - m_dragX;
            int scrY = m_preClickScrollY + m_clickY - m_dragY;
            setPrimaryScroll(getActiveMap(), scrX, scrY);
        }

        if (m_toolMode == TOOL_MODE_PEN)
        {
            // add a point to the Pen asset
            Point modelMouse = viewToModel(e.getX(), e.getY());
            m_penAsset.addPoint(modelMouse.x, modelMouse.y);
        }

        if (m_toolMode == TOOL_MODE_LINE)
        {
            if (m_bShiftKeyDown)
            {
                // force to 90 degree increments
                int dx = Math.abs(m_dragX - m_clickX);
                int dy = Math.abs(m_dragY - m_clickY);

                if (dx > dy)
                {
                    // force to horizontal
                    m_dragY = m_clickY;
                }
                else
                {
                    // force to vertical
                    m_dragX = m_clickX;
                }
            }

            checkSnap();
        }

        if (m_toolMode == TOOL_MODE_ARROW)
        {
            // get the model loc of the current mouse position
            Point modelClick = viewToModel(m_dragX, m_dragY);

            if (m_pogBeingDragged != null)
            {
                m_pogBeingDragged.setPosition(modelClick);

                if (m_bControlKeyDown)
                {
                    m_pogBeingDragged.setPosition(m_pogBeingDragged.getX() - m_pogDragInsetX, m_pogBeingDragged.getY()
                        - m_pogDragInsetY);
                }
                else
                {
                    m_pogBeingDragged.setPosition(m_pogBeingDragged.getX() - m_pogBeingDragged.getFaceSize()
                        * BASE_SQUARE_SIZE / 2, m_pogBeingDragged.getY() - m_pogBeingDragged.getFaceSize()
                        * BASE_SQUARE_SIZE / 2);
                    snapPogToGrid(m_pogBeingDragged);
                }
            }
        }

        repaint();
        getActivePogsArea().repaint();
    }

    public void checkSnap()
    {
        if (!m_bLDragging)
        {
            return;
        }

        if (m_toolMode != TOOL_MODE_LINE)
        {
            return;
        }

        // snap to grid if control is not pressed
        if (!m_bControlKeyDown)
        {
            int snapToX = getSnapX(m_dragX);
            int snapToY = getSnapY(m_dragY);

            if (Math.abs(snapToX - m_dragX) < SNAP_DISTANCE)
            {
                m_dragX = snapToX;
            }
            if (Math.abs(snapToY - m_dragY) < SNAP_DISTANCE)
            {
                m_dragY = snapToY;
            }
        }
        else
        {
            m_dragX = m_currentMouseX;
            m_dragY = m_currentMouseY;
        }
    }

    // Takes a model coordinate
    private int getSnap(int i)
    {
        if (i < 0)
        {
            return ((i - BASE_SQUARE_SIZE / 2) / BASE_SQUARE_SIZE) * BASE_SQUARE_SIZE;
        }
        return ((i + BASE_SQUARE_SIZE / 2) / BASE_SQUARE_SIZE) * BASE_SQUARE_SIZE;
    }

    // takes VIEW coords
    private int getSnapX(int x)
    {
        return modelToView(getSnap(viewToModel(x, 0).x), 0).x;
    }

    // takes VIEW coords
    private int getSnapY(int y)
    {
        return modelToView(getSnap(viewToModel(0, y).y), 0).y;
    }

    public void updateLocation(MouseEvent e)
    {
        if (e.getSource() == getActivePogsArea())
        {
            Point canvasView = pogsViewToCanvasView(e.getX(), e.getY());
            m_dragX = canvasView.x;
            m_dragY = canvasView.y;
        }
        else
        {
            m_dragX = e.getX();
            m_dragY = e.getY();

            m_currentMouseX = e.getX();
            m_currentMouseY = e.getY();
        }
    }

    public void setToolMode(int mode)
    {
        m_toolMode = mode;
    }

    public void setCursor()
    {
        if (NEW_TOOL)
        {
            // do nothing
        }
        else
        {
            switch (m_toolMode)
            {
                case TOOL_MODE_ARROW:
                {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                    break;

                case TOOL_MODE_PEN:
                {
                    setCursor(m_penCursor);
                }
                    break;

                case TOOL_MODE_HAND:
                {
                    setCursor(m_handCursor);
                }
                    break;

                case TOOL_MODE_ERASER:
                {
                    setCursor(m_eraserCursor);
                }
                    break;

                case TOOL_MODE_POINT:
                {
                    setCursor(m_emptyCursor);
                }
                    break;

                case TOOL_MODE_LINE:
                {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
                    break;
            }
        }
    }

    public void updateToolState()
    {
        // if they're right-dragging, it's a point
        if (m_bSpaceKeyDown)
        {
            setToolMode(TOOL_MODE_POINT);
            setCursor();
            getActivePogsArea().updateToolState();
            return;
        }

        // if they're left-dragging, we don't want to change the tool
        if (m_bLDragging)
        {
            setCursor();
            getActivePogsArea().updateToolState();
            return;
        }

        // if the pogs view is dragging, we don't want to change anything either
        if (getActivePogsArea().m_bLDragging || getActivePogsArea().m_bRDragging)
        {
            return;
        }

        // if they have space or shift down, it's a hand
        if (m_bRDragging)
        {
            setToolMode(TOOL_MODE_HAND);
            setCursor();
            getActivePogsArea().updateToolState();
            return;
        }

        // otherwise, ask the buttons
        if (m_gametableFrame.m_arrowButton.isSelected())
        {
            setToolMode(TOOL_MODE_ARROW);
        }
        if (m_gametableFrame.m_penButton.isSelected())
        {
            setToolMode(TOOL_MODE_PEN);
        }
        if (m_gametableFrame.m_eraserButton.isSelected())
        {
            setToolMode(TOOL_MODE_ERASER);
        }
        if (m_gametableFrame.m_colorEraserButton.isSelected())
        {
            setToolMode(TOOL_MODE_ERASER);
        }
        if (m_gametableFrame.m_lineButton.isSelected())
        {
            setToolMode(TOOL_MODE_LINE);
        }

        setCursor();

        // now tell the pogs area about it
        getActivePogsArea().updateToolState();
    }

    public void centerZoom(int delta)
    {
        // can't do this at all if we're dragging
        if (m_bLDragging || m_bRDragging || m_bPogBeingDragged)
        {
            return;
        }
        // note the model location of the center
        Point modelCenter = viewToModel(getWidth() / 2, getHeight() / 2);

        // do the zoom
        setZoom(m_zoom + delta);

        // note the view location of the model center
        Point viewCenter = modelToView(modelCenter.x, modelCenter.y);

        // note the present actual center
        int presentCenterX = getWidth() / 2;
        int presentCenterY = getHeight() / 2;

        // set up the scroll to enforce the center being where it's supposed to be
        int scrX = getActiveMap().getScrollX() - (presentCenterX - viewCenter.x);
        int scrY = getActiveMap().getScrollY() - (presentCenterY - viewCenter.y);
        setPrimaryScroll(getActiveMap(), scrX, scrY);
    }

    /** *********************************************************** */
    // KeyListener events
    /** *********************************************************** */
    public void addItemListener(ItemListener l)
    {
        listenerList.add(ItemListener.class, l);
    }

    public void removeItemListener(ItemListener l)
    {
        listenerList.remove(ItemListener.class, l);
    }

    public Object[] getSelectedObjects()
    {
        if (isSelected() == false)
        {
            return null;
        }
        Object[] selectedObjects = new Object[1];
        selectedObjects[0] = getText();
        return selectedObjects;
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_SPACE)
        {
            if (m_bLDragging || m_bRDragging)
            {
                // no pointing in the middle of a drag
                return;
            }

            if (!m_bMouseOnView)
            {
                // no pointing if the mouse is outside the view area
                return;
            }

            // we're only interested in doing this if they aren't already
            // holding the space key.
            if (m_bSpaceKeyDown == false)
            {

                m_bSpaceKeyDown = true;

                // pointing
                Player me = m_gametableFrame.getMePlayer();
                Point modelMouse = viewToModel(m_currentMouseX, m_currentMouseY);
                me.setPointing(true);
                me.setPoint(modelMouse);

                m_gametableFrame.push(PacketManager.makePointPacket(m_gametableFrame.m_myPlayerIdx, me.getPoint().x, me
                    .getPoint().y, true));

                updateToolState();

                e.consume();
            }
        }

        if (code == KeyEvent.VK_SHIFT)
        {
            m_bShiftKeyDown = true;
            e.consume();
        }
        if (code == KeyEvent.VK_CONTROL)
        {
            m_bControlKeyDown = true;
            checkSnap();
            e.consume();
        }
        if (code == KeyEvent.VK_MINUS || code == KeyEvent.VK_SUBTRACT)
        {
            centerZoom(1);
            e.consume();
        }
        if (code == KeyEvent.VK_EQUALS || code == KeyEvent.VK_ADD)
        {
            centerZoom(-1);
            e.consume();
        }
        if (code == KeyEvent.VK_ENTER)
        {
            // shift focus to the chat area
            m_gametableFrame.m_textEntry.requestFocus();
        }
        if (code == KeyEvent.VK_A)
        {
            // become the arrow tool
            m_gametableFrame.m_arrowButton.setSelected(true);
        }
        if (code == KeyEvent.VK_SLASH)
        {
            m_gametableFrame.m_textEntry.setText("/");
            m_gametableFrame.m_textEntry.requestFocus();
        }
        updateToolState();
        repaint();
    }

    public void keyReleased(KeyEvent e)
    {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_SPACE)
        {
            m_bSpaceKeyDown = false;

            // they're done pointing
            m_gametableFrame.getMePlayer().setPointing(false);
            m_gametableFrame.push(PacketManager.makePointPacket(m_gametableFrame.m_myPlayerIdx, 0, 0, false));

            e.consume();
        }
        if (code == KeyEvent.VK_SHIFT)
        {
            m_bShiftKeyDown = false;
            e.consume();
        }
        if (code == KeyEvent.VK_CONTROL)
        {
            m_bControlKeyDown = false;
            checkSnap();
            e.consume();
        }
        updateToolState();
        repaint();
    }

    /** *********************************************************** */
    // drawing
    /** *********************************************************** */
    public void update(Graphics g)
    {
        paint(g);
    }

    public void paint(Graphics g)
    {
        if (false)
        {
            final Map HINTS = new HashMap();
            HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ((Graphics2D)g).addRenderingHints(HINTS);
        }
        else
        {
            final Map HINTS = new HashMap();
            HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            ((Graphics2D)g).addRenderingHints(HINTS);
        }

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.translate(-getActiveMap().getScrollX(), -getActiveMap().getScrollY());

        drawMatte(g, getActiveMap().getScrollX(), getActiveMap().getScrollY(), getWidth(), getHeight());

        // draw all the underlays here
        for (int i = 0; i < getActiveMap().getNumPogs(); i++)
        {
            Pog pog = getActiveMap().getPogAt(i);
            if (pog.isUnderlay())
            {
                pog.drawToCanvas(g);
            }
        }

        // if they're dragging an underlay, draw it here
        // there could be a pog drag in progress
        if (m_bPogBeingDragged)
        {
            if (getActivePogsArea().m_selectedPog.isUnderlay())
            {
                getActivePogsArea().m_selectedPog.drawToCanvas(g);
            }
        }

        // there could be an internal pog move being done. we draw it again here to ensure
        // it's on top of the heap
        if (m_pogBeingDragged != null)
        {
            if (m_pogBeingDragged.isUnderlay())
            {
                m_pogBeingDragged.drawToCanvas(g);
            }
        }

        drawLines(g, getActiveMap().getScrollX(), getActiveMap().getScrollY(), getWidth(), getHeight());

        // lines
        for (int i = 0; i < getActiveMap().getNumLines(); i++)
        {
            LineSegment ls = getActiveMap().getLineAt(i);

            // LineSegments police themselves, performance wise. If they won't touch the current
            // viewport, they don't draw
            ls.draw(g, this);
        }

        // ******************** LEFT DRAG ***********************/
        if (m_bLDragging)
        {
            // ******************** PEN ASSET ***********************/
            if (m_penAsset != null)
            {
                LineSegment[] lines = m_penAsset.getLineSegments();
                if (lines != null)
                {
                    for (int i = 0; i < lines.length; i++)
                    {
                        lines[i].draw(g, this);
                    }
                }
            }

            // ******************** ERASER ASSET ***********************/
            if (m_toolMode == TOOL_MODE_ERASER)
            {
                // draw a rubber-band line to represent the selection
                g.setColor(Color.BLACK);
                drawDottedRect(g, m_clickX + getActiveMap().getScrollX(), m_clickY + getActiveMap().getScrollY(),
                    m_dragX - m_clickX, m_dragY - m_clickY);
            }

            // ******************** LINE ASSET ***********************/
            if (m_toolMode == TOOL_MODE_LINE)
            {
                // draw the rubber band line
                g.setColor(Color.BLACK);
                g.drawLine(m_clickX + getActiveMap().getScrollX(), m_clickY + getActiveMap().getScrollY(), m_dragX
                    + getActiveMap().getScrollX(), m_dragY + getActiveMap().getScrollY());
            }
        }

        // pogs
        for (int i = 0; i < getActiveMap().getNumPogs(); i++)
        {
            Pog pog = getActiveMap().getPogAt(i);
            if (!pog.isUnderlay())
            {
                pog.drawToCanvas(g);
            }
        }

        // there could be a pog drag in progress
        if (m_bPogBeingDragged)
        {
            if (!getActivePogsArea().m_selectedPog.isUnderlay())
            {
                getActivePogsArea().m_selectedPog.drawToCanvas(g);
            }
        }

        // there could be an internal pog move being done. we draw it again here to ensure
        // it's on top of the heap
        if (m_pogBeingDragged != null)
        {
            if (!m_pogBeingDragged.isUnderlay())
            {
                m_pogBeingDragged.drawToCanvas(g);
            }
        }

        // draw the cursor overlays
        for (int i = 0; i < m_gametableFrame.m_players.size(); i++)
        {
            Player plr = (Player)m_gametableFrame.m_players.get(i);
            if (plr.isPointing())
            {
                // draw this player's point cursor
                Point pointingAt = modelToDraw(plr.getPoint().x, plr.getPoint().y);
                int idx = i % m_pointCursorImages.length;
                Image hand = m_pointCursorImages[idx];

                // 5px offset to align with mouse pointer
                g.drawImage(hand, pointingAt.x, pointingAt.y - 5, null);
            }
        }

        // mousing around
        if (m_bMouseOnView)
        {
            // we don't do this at all if they're presently
            // drawing a line
            if (!(m_bLDragging && m_toolMode == TOOL_MODE_LINE))
            {
                Pog mouseOverPog = getPogMouseOver();
                if (m_bShiftKeyDown)
                {
                    // this shift key is down. Show all pog data
                    for (int i = 0; i < getActiveMap().getNumPogs(); i++)
                    {
                        Pog pog = getActiveMap().getPogAt(i);
                        if (pog != mouseOverPog)
                        {
                            pog.drawDataStringToCanvas(g, false);
                        }
                    }
                }
                if (mouseOverPog != null)
                {
                    mouseOverPog.drawDataStringToCanvas(g, true);
                }
            }
        }
        getActiveTool().paint(g);
        g.translate(getActiveMap().getScrollX(), getActiveMap().getScrollY());
    }

    public Pog getPogMouseOver()
    {
        Point modelMouse = viewToModel(m_currentMouseX, m_currentMouseY);
        for (int i = getActiveMap().getNumPogs() - 1; i >= 0; i--)
        {
            Pog pog = getActiveMap().getPogAt(i);
            if (pog.modelPtInBounds(modelMouse.x, modelMouse.y))
            {
                return pog;
            }
        }
        return null;
    }

    // topLeftX and topLeftY are the coordinates of where the
    // top left of the map area is in whatever coordinate system g is set up to be
    public void drawMatte(Graphics g, int topLeftX, int topLeftY, int width, int height)
    {
        // background image
        int qx = Math.abs(topLeftX) / m_mapBk.getWidth(null);
        if (topLeftX < 0)
        {
            qx++;
            qx = -qx;
        }

        int qy = Math.abs(topLeftY) / m_mapBk.getHeight(null);
        if (topLeftY < 0)
        {
            qy++;
            qy = -qy;
        }

        int linesXOffset = qx * m_mapBk.getWidth(null);
        int linesYOffset = qy * m_mapBk.getHeight(null);
        int vLines = width / m_mapBk.getWidth(null) + 2;
        int hLines = height / m_mapBk.getHeight(null) + 2;

        for (int i = 0; i < vLines; i++)
        {
            for (int j = 0; j < hLines; j++)
            {
                g.drawImage(m_mapBk, i * m_mapBk.getWidth(null) + linesXOffset, j * m_mapBk.getHeight(null)
                    + linesYOffset, null);
            }
        }
    }

    public void drawLines(Graphics g, int topLeftX, int topLeftY, int width, int height)
    {
        // draw some lines
        int qx = Math.abs(topLeftX) / m_mapBk.getWidth(null);
        if (topLeftX < 0)
        {
            qx++;
            qx = -qx;
        }

        int qy = Math.abs(topLeftY) / m_mapBk.getHeight(null);
        if (topLeftY < 0)
        {
            qy++;
            qy = -qy;
        }

        int linesXOffset = qx * m_mapBk.getWidth(null);
        int linesYOffset = qy * m_mapBk.getHeight(null);
        int vLines = width / m_mapBk.getWidth(null) + 2;
        int hLines = height / m_mapBk.getHeight(null) + 2;

        qx = Math.abs(topLeftX) / m_squareSize;
        if (topLeftX < 0)
        {
            qx++;
            qx = -qx;
        }

        qy = Math.abs(topLeftY) / m_squareSize;
        if (topLeftY < 0)
        {
            qy++;
            qy = -qy;
        }

        linesXOffset = qx * m_squareSize;
        linesYOffset = qy * m_squareSize;
        vLines = width / m_squareSize + 2;
        hLines = height / m_squareSize + 2;

        g.setColor(Color.GRAY);

        if (m_zoom < 4)
        {
            for (int i = 0; i < vLines; i++)
            {
                g.drawLine(i * m_squareSize + linesXOffset, topLeftY, i * m_squareSize + linesXOffset, height
                    + topLeftY);
            }
            for (int i = 0; i < hLines; i++)
            {
                g
                    .drawLine(topLeftX, i * m_squareSize + linesYOffset, width + topLeftX, i * m_squareSize
                        + linesYOffset);
            }
        }
    }

    public static void drawDottedRect(Graphics g, int x, int y, int width, int height)
    {
        Graphics2D g2d = (Graphics2D)g;
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
            2f
        }, 0f));

        if (width < 0)
        {
            x += width;
            width = -width;
        }
        if (height < 0)
        {
            y += height;
            height = -height;
        }
        g.drawRect(x, y, width, height);
        g2d.setStroke(oldStroke);
    }

    public static void drawDottedLine(Graphics g, int x, int y, int x2, int y2)
    {
        Graphics2D g2d = (Graphics2D)g;
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
            2f
        }, 0f));
        g.drawLine(x, y, x2, y2);
        g2d.setStroke(oldStroke);
    }

    /** *********************************************************** */
    // component listening
    /** *********************************************************** */
    public void componentResized(ComponentEvent e)
    {
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
}
