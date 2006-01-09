/*
 * GametableCanvas.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.galactanet.gametable.tools.NullTool;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class GametableCanvas extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener
{
    public final static int   VALUE_X                = 0;
    public final static int   VALUE_Y                = 1;

    public final static int   SNAP_DISTANCE          = 8;

    // drag modes
    public final static int   TOOL_MODE_ARROW        = 0;
    public final static int   TOOL_MODE_POINT        = 1;
    public final static int   TOOL_MODE_PEN          = 2;
    public final static int   TOOL_MODE_HAND         = 3;
    public final static int   TOOL_MODE_ERASER       = 4;
    public final static int   TOOL_MODE_LINE         = 5;

    public final static int   NUM_POINT_CURSORS      = 8;
    public final static int   POINT_CURSOR_OFFSET_X  = 5;
    public final static int   POINT_CURSOR_OFFSET_Y  = 6;

    // grid modes
    public final static int   GRID_MODE_NONE         = 0;
    public final static int   GRID_MODE_SQUARES      = 1;
    public final static int   GRID_MODE_HEX          = 2;

    // the size of a square at max zoom level (0)
    public final static int   BASE_SQUARE_SIZE       = 64;

    public final static int   NUM_ZOOM_LEVELS        = 5;

    private static final int  KEYBOARD_SCROLL_AMOUNT = 50;

    public Image              m_mapBk;

    // this is the map (or layer) that all players share
    private GametableMap      m_publicMap            = new GametableMap(true);
    private GametableMap      m_privateMap           = new GametableMap(false);

    // this points to whichever map is presently active
    private GametableMap      m_activeMap;

    // some cursors
    Cursor                    m_handCursor;
    Cursor                    m_penCursor;
    Cursor                    m_emptyCursor;
    Cursor                    m_eraserCursor;
    Image[]                   m_pointCursorImages    = new Image[NUM_POINT_CURSORS];

    // the frame
    private GametableFrame    m_gametableFrame;

    // zoom and top-left state

    // This is the number of screen pixels that are
    // used per model pixel. It's never less than 1
    public int                m_zoom                 = 1;

    // the size of a square at the current zoom level
    public int                m_squareSize           = getSquareSizeForZoom(m_zoom);

    // hand tool
    int                       m_handToolStartX;
    int                       m_handToolStartY;
    int                       m_scrollStartX;
    int                       m_scrollStartY;

    // misc flags
    boolean                   m_bSpaceKeyDown;
    boolean                   m_bShiftKeyDown;
    boolean                   m_bControlKeyDown;
    boolean                   m_bAltKeyDown;

    // drag stuff
    // where they first clicked
    int                       m_clickX;
    int                       m_clickY;

    // where the mouse is now
    int                       m_dragX;
    int                       m_dragY;

    // hand tool
    int                       m_preClickScrollX;
    int                       m_preClickScrollY;

    // Pen tool
    PenAsset                  m_penAsset;

    boolean                   m_bLDragging;
    boolean                   m_bRDragging;
    boolean                   m_bNeedToDrawPointCursor;
    int                       m_toolMode;

    Image                     m_offscreen;
    int                       m_offscreenX;
    int                       m_offscreenY;

    boolean                   m_bPogBeingDragged;
    Pog                       m_pogBeingDragged;
    int                       m_pogDragInsetX;
    int                       m_pogDragInsetY;

    private Point             m_mouseModelAnchor;
    private Point             m_mouseModelFloat;
    boolean                   m_bMouseOnView;

    Pog                       m_pogMouseOver;

    SquareGridMode            m_squareGridMode       = new SquareGridMode();
    HexGridMode               m_hexGridMode          = new HexGridMode();
    GridMode                  m_noGridMode           = new GridMode();
    GridMode                  m_gridMode;

    private int               m_activeToolId         = -1;
    private static final Tool NULL_TOOL              = new NullTool();

    public boolean            m_bRightClicking;                                      // true if
    // the
    // current
    // mouse
    // action
    // was
    // initiated
    // with a
    // right-click
    public int                m_preRightClickToolID;                                 // the id of

    // the tool
    // that we
    // switched
    // out of to
    // go to
    // hand tool
    // for a
    // right-click

    // this is the color used to overlay on top of the public layer
    // when the user is on the private layer. It's white with 50% alpha
    public Color              OVERLAY_COLOR          = new Color(255, 255, 255, 128);

    public GametableCanvas()
    {
        setFocusable(true);
        setRequestFocusEnabled(true);
        requestFocus();

        addMouseListener(this);
        addMouseMotionListener(this);
        addFocusListener(new FocusListener()
        {
            /*
             * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
             */
            public void focusGained(FocusEvent e)
            {
                JPanel panel = (JPanel)getParent();
                panel.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), LineBorder
                    .createBlackLineBorder()));
            }

            /*
             * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
             */
            public void focusLost(FocusEvent e)
            {
                JPanel panel = (JPanel)getParent();
                panel.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 1, 1, 1)));
            }

        });

        initializeKeys();

        m_activeMap = m_publicMap;
    }

    /**
     * Initializes all the keys for the canvas.
     */
    private void initializeKeys()
    {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed SPACE"), "startPointing");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released SPACE"), "stopPointing");

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftDown");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released SHIFT"), "shiftUp");

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control pressed CONTROL"), "controlDown");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released CONTROL"), "controlUp");

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt pressed ALT"), "altDown");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released ALT"), "altUp");

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed SUBTRACT"), "zoomIn");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed MINUS"), "zoomIn");

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed ADD"), "zoomOut");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed PLUS"), "zoomOut");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed EQUALS"), "zoomOut");

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed UP"), "scrollUp");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed KP_UP"), "scrollUp");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed DOWN"), "scrollDown");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed KP_DOWN"), "scrollDown");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed LEFT"), "scrollLeft");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed KP_LEFT"), "scrollLeft");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed RIGHT"), "scrollRight");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed KP_RIGHT"), "scrollRight");

        getActionMap().put("startPointing", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (m_gametableFrame.getFocusOwner() instanceof JTextField)
                {
                    return;
                }

                if (!m_bMouseOnView || getActiveTool().isBeingUsed())
                {
                    // no pointing if the mouse is outside the view area, or the active tool is
                    // being used.
                    return;
                }

                // we're only interested in doing this if they aren't already
                // holding the space key.
                if (m_bSpaceKeyDown == false)
                {
                    m_bSpaceKeyDown = true;

                    pointAt(m_mouseModelFloat);
                }
            }
        });

        getActionMap().put("stopPointing", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                m_bSpaceKeyDown = false;
                pointAt(null);
            }
        });

        getActionMap().put("shiftDown", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                m_bShiftKeyDown = true;
                repaint();
            }
        });

        getActionMap().put("shiftUp", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                m_bShiftKeyDown = false;
                repaint();
            }
        });

        getActionMap().put("controlDown", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                m_bControlKeyDown = true;
                repaint();
            }
        });

        getActionMap().put("controlUp", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                m_bControlKeyDown = false;
                repaint();
            }
        });

        getActionMap().put("altDown", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                m_bAltKeyDown = true;
            }
        });

        getActionMap().put("altUp", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                m_bAltKeyDown = false;
            }
        });

        getActionMap().put("zoomIn", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (m_gametableFrame.getFocusOwner() instanceof JTextField)
                {
                    return;
                }

                centerZoom(1);
            }
        });

        getActionMap().put("zoomOut", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (m_gametableFrame.getFocusOwner() instanceof JTextField)
                {
                    return;
                }

                centerZoom(-1);
            }
        });

        getActionMap().put("scrollUp", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (m_gametableFrame.getFocusOwner() instanceof JTextField)
                {
                    return;
                }

                GametableMap map = getActiveMap();
                Point p = drawToModel(map.getScrollX(), map.getScrollY());
                scrollMapTo(p.x, p.y - KEYBOARD_SCROLL_AMOUNT);
            }
        });

        getActionMap().put("scrollDown", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (m_gametableFrame.getFocusOwner() instanceof JTextField)
                {
                    return;
                }

                GametableMap map = getActiveMap();
                Point p = drawToModel(map.getScrollX(), map.getScrollY());
                scrollMapTo(p.x, p.y + KEYBOARD_SCROLL_AMOUNT);
            }
        });

        getActionMap().put("scrollLeft", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (m_gametableFrame.getFocusOwner() instanceof JTextField)
                {
                    return;
                }

                GametableMap map = getActiveMap();
                Point p = drawToModel(map.getScrollX(), map.getScrollY());
                scrollMapTo(p.x - KEYBOARD_SCROLL_AMOUNT, p.y);
            }
        });

        getActionMap().put("scrollRight", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                if (m_gametableFrame.getFocusOwner() instanceof JTextField)
                {
                    return;
                }

                GametableMap map = getActiveMap();
                Point p = drawToModel(map.getScrollX(), map.getScrollY());
                scrollMapTo(p.x + KEYBOARD_SCROLL_AMOUNT, p.y);
            }
        });
    }

    public void init(GametableFrame frame)
    {
        m_gametableFrame = frame;
        m_mapBk = UtilityFunctions.getImage("assets/mapbk.png");

        m_pointCursorImages[0] = UtilityFunctions.getImage("assets/whiteHand.png");
        m_pointCursorImages[1] = UtilityFunctions.getImage("assets/brownHand.png");
        m_pointCursorImages[2] = UtilityFunctions.getImage("assets/purpleHand.png");
        m_pointCursorImages[3] = UtilityFunctions.getImage("assets/blueHand.png");
        m_pointCursorImages[4] = UtilityFunctions.getImage("assets/redHand.png");
        m_pointCursorImages[5] = UtilityFunctions.getImage("assets/greenHand.png");
        m_pointCursorImages[6] = UtilityFunctions.getImage("assets/greyHand.png");
        m_pointCursorImages[7] = UtilityFunctions.getImage("assets/yellowHand.png");

        setPrimaryScroll(m_publicMap, 0, 0);

        // set up the grid modes
        m_squareGridMode.init(this);
        m_hexGridMode.init(this);
        m_gridMode = m_squareGridMode;

        addMouseWheelListener(this);
        setZoom(0);
        setActiveTool(0);
    }

    public int getModifierFlags()
    {
        return ((m_bControlKeyDown ? Tool.MODIFIER_CTRL : 0) | (m_bSpaceKeyDown ? Tool.MODIFIER_SPACE : 0) | (m_bShiftKeyDown ? Tool.MODIFIER_SHIFT
            : 0));
    }

    /**
     * Sets the mouse cursor to be the cursor at the specified index for the currently active tool.
     * 
     * @param index The cursor of the given index for this tool. A negative number means no cursor.
     */
    public void setToolCursor(int index)
    {
        if (index < 0)
        {
            setCursor(m_emptyCursor);
        }
        else
        {
            setCursor(m_gametableFrame.getToolManager().getToolInfo(m_activeToolId).getCursor(index));
        }
    }

    public void setActiveTool(int index)
    {
        Tool oldTool = getActiveTool();
        oldTool.deactivate();

        m_activeToolId = index;

        Tool tool = getActiveTool();
        tool.activate(this);
        setToolCursor(0);
        m_gametableFrame.setToolSelected(m_activeToolId);
    }

    public Tool getActiveTool()
    {
        if (m_activeToolId < 0)
        {
            return NULL_TOOL;
        }
        return m_gametableFrame.getToolManager().getToolInfo(m_activeToolId).getTool();
    }

    public GametableMap getPublicMap()
    {
        return m_publicMap;
    }

    public GametableMap getPrivateMap()
    {
        return m_privateMap;
    }

    public GametableMap getActiveMap()
    {
        // if we're processing a packet, we want it to go to the
        // public layer, even if they're presently on the private layer.
        // HOWEVER, if we're in the process of opening a file, then that
        // trumps net packet processing, and we want to return whatever
        // map they're on.

        if (PacketSourceState.isFileLoading())
        {
            return m_activeMap;
        }
        if (PacketSourceState.isNetPacketProcessing())
        {
            return m_publicMap;
        }
        return m_activeMap;
    }

    public boolean isPublicMap()
    {
        return (getActiveMap() == m_publicMap);
    }

    public void setActiveMap(GametableMap map)
    {
        m_activeMap = map;
    }

    public void setGridModeByID(int id)
    {
        switch (id)
        {
            case GRID_MODE_NONE:
            {
                m_gridMode = m_noGridMode;
            }
            break;

            case GRID_MODE_SQUARES:
            {
                m_gridMode = m_squareGridMode;
            }
            break;

            case GRID_MODE_HEX:
            {
                m_gridMode = m_hexGridMode;
            }
            break;
        }
    }

    public int getGridModeID()
    {
        if (m_gridMode == m_squareGridMode)
        {
            return GRID_MODE_SQUARES;
        }
        if (m_gridMode == m_hexGridMode)
        {
            return GRID_MODE_HEX;
        }
        return GRID_MODE_NONE;
    }

    public PogPanel getPogPanel()
    {
        return m_gametableFrame.getPogPanel();
    }

    public static int getSquareSizeForZoom(int level)
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
        if (zoomLevel < 0)
        {
            zoomLevel = 0;
        }

        if (zoomLevel >= NUM_ZOOM_LEVELS)
        {
            zoomLevel = NUM_ZOOM_LEVELS - 1;
        }

        if (m_zoom != zoomLevel)
        {
            m_zoom = zoomLevel;
            m_squareSize = getSquareSizeForZoom(m_zoom);
            repaint();
        }
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

    public double modelToSquares(double m)
    {
        return (m / BASE_SQUARE_SIZE);
    }

    /** *********************************************************** */
    // MouseListener/MouseMotionListener overrides:
    /** *********************************************************** */
    public void mouseDragged(MouseEvent e)
    {
        // We handle dragging ourselves - don't tread on me, Java!
        mouseMoved(e);
    }

    public void mouseMoved(MouseEvent e)
    {
        m_mouseModelFloat = viewToModel(e.getX(), e.getY());
        if (isPointing())
        {
            return;
        }
        m_gametableFrame.getToolManager().mouseMoved(m_mouseModelFloat.x, m_mouseModelFloat.y, getModifierFlags());
        Pog prevPog = m_pogMouseOver;
        if (prevPog != m_pogMouseOver)
        {
            repaint();
        }
    }

    public void mouseClicked(MouseEvent e)
    {
        // Ignore this because java has sucky mouse clicking
    }

    public void mousePressed(MouseEvent e)
    {
        requestFocus();
        m_mouseModelAnchor = viewToModel(e.getX(), e.getY());
        m_mouseModelFloat = m_mouseModelAnchor;
        if (isPointing())
        {
            return;
        }

        // this code deals with making a right click automatically be the hand tool
        if (e.getButton() == MouseEvent.BUTTON3)
        {
            m_bRightClicking = true;
            m_preRightClickToolID = m_activeToolId;
            setActiveTool(1); // HACK -- To hand tool
            m_gametableFrame.getToolManager().mouseButtonPressed(m_mouseModelAnchor.x, m_mouseModelAnchor.y,
                getModifierFlags());
        }
        else
        {
            m_bRightClicking = false;
        }

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            m_gametableFrame.getToolManager().mouseButtonPressed(m_mouseModelAnchor.x, m_mouseModelAnchor.y,
                getModifierFlags());
        }

    }

    public void mouseReleased(MouseEvent e)
    {
        m_mouseModelFloat = viewToModel(e.getX(), e.getY());
        m_mouseModelAnchor = null;
        if (isPointing())
        {
            return;
        }
        m_gametableFrame.getToolManager().mouseButtonReleased(m_mouseModelFloat.x, m_mouseModelFloat.y,
            getModifierFlags());

        if (m_bRightClicking)
        {
            // return to arrow too
            setActiveTool(m_preRightClickToolID);
            m_bRightClicking = false;
        }
    }

    public void mouseEntered(MouseEvent e)
    {
        m_bMouseOnView = true;
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

    public Pog getPogByID(int id)
    {
        for (int i = 0; i < getActiveMap().getNumPogs(); i++)
        {
            Pog pog = getActiveMap().getPogAt(i);
            if (pog.getId() == id)
            {
                return pog;
            }
        }
        return null;
    }

    public void clearUndoStacks()
    {
        // we only clear the public stack. No need to mess with the private one.
        m_publicMap.clearUndos();
    }

    public void redo()
    {
        // first, see if we even can redo
        if (!getActiveMap().canRedo())
        {
            // we can't redo.
            return;
        }

        // we can redo.
        getActiveMap().redoNextRecent();

        repaint();
    }

    public void doRedo(int stateID)
    {
        // the active map should be the public map
        getActiveMap().redo(stateID);
    }

    public void undo()
    {
        // first, see if we even can undo
        if (!getActiveMap().canUndo())
        {
            // we can't undo.
            return;
        }

        // we can undo. Undo the most recent action
        getActiveMap().undoMostRecent();

        repaint();
    }

    public void doUndo(int stateID)
    {
        // the active map should be the public map
        getActiveMap().undo(stateID);
    }

    public void recenterView(int modelCenterX, int modelCenterY, int zoomLevel)
    {
        m_gametableFrame.send(PacketManager.makeRecenterPacket(modelCenterX, modelCenterY, zoomLevel));

        if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
        {
            doRecenterView(modelCenterX, modelCenterY, zoomLevel);
        }
    }

    /*
     * This function will set the scroll for all maps, keeping their relative offsets preserved. The x,y values sent in
     * will become the scroll values for the desired map. All others maps will preserve offsets from that.
     */
    public void setPrimaryScroll(GametableMap mapToSet, int x, int y)
    {
        m_publicMap.setScroll(x, y);
        m_privateMap.setScroll(x, y);
        /*
         * int dx = x - mapToSet.getScrollX(); int dy = y - mapToSet.getScrollY(); m_publicMap.setScroll(dx +
         * mapToSet.getScrollX(), dy + mapToSet.getScrollY()); m_privateMap.setScroll(dx + mapToSet.getScrollX(), dy +
         * mapToSet.getScrollY());
         */
    }

    public void doRecenterView(int modelCenterX, int modelCenterY, int zoomLevel)
    {
        // if you recenter for any reason, your tool action is cancelled
        m_gametableFrame.getToolManager().cancelToolAction();

        // make the sent in x and y our center, ad the sent in zoom.
        // So start with the zoom
        setZoom(zoomLevel);

        // find the view coordinate for the model center
        setPrimaryScroll(m_publicMap, 0, 0);

        // we need to get the coords for the shared map, even if we're not on that at the moment
        // so we cheezily set to the shared map, then return it to normal after the
        // call to modelToView
        GametableMap storedMap = m_activeMap;
        m_activeMap = m_publicMap;
        Point viewCenter = modelToView(modelCenterX, modelCenterY);
        m_activeMap = storedMap;

        // find where the top left would have to be, based on our size
        int tlX = viewCenter.x - getWidth() / 2;
        int tlY = viewCenter.y - getHeight() / 2;

        // that is our new scroll position
        setPrimaryScroll(m_publicMap, tlX, tlY);

        repaint();
    }

    public void setPogData(int id, String s, Map toAdd, Set toDelete)
    {
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makePogDataPacket(id, s, toAdd, toDelete));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doSetPogData(id, s, toAdd, toDelete);
            }
        }
        else
        {
            doSetPogData(id, s, toAdd, toDelete);
        }
    }

    public void doSetPogData(int id, String s, Map toAdd, Set toDelete)
    {
        Pog pog = getPogByID(id);
        if (pog == null)
        {
            return;
        }

        if (s != null)
        {
            pog.setText(s);
        }

        if (toDelete != null)
        {
            for (Iterator iterator = toDelete.iterator(); iterator.hasNext();)
            {
                String key = (String)iterator.next();
                pog.removeAttribute(key);
            }
        }

        if (toAdd != null)
        {
            for (Iterator iterator = toAdd.entrySet().iterator(); iterator.hasNext();)
            {
                Map.Entry entry = (Map.Entry)iterator.next();
                pog.setAttribute((String)entry.getKey(), (String)entry.getValue());
            }
        }

        repaint();
    }

    public void movePog(int id, int newX, int newY)
    {
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makeMovePogPacket(id, newX, newY));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doMovePog(id, newX, newY);
            }
        }
        else
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
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makeRemovePogsPacket(ids));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doRemovePogs(ids);
            }
        }
        else
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
        if (m_bLDragging && m_pogBeingDragged != null && m_pogBeingDragged.getId() == id)
        {
            m_pogBeingDragged = null;
            m_gametableFrame.logSystemMessage("The pog you were holding disappears out of your hands!");
        }
        repaint();
    }

    public void addPog(Pog toAdd)
    {
        toAdd.assignUniqueId();
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makeAddPogPacket(toAdd));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doAddPog(toAdd);
            }
        }
        else
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
        if (isPublicMap())
        {
            // if we're the host, push it to everyone and add the lines.
            // if we're a joiner, just push it to the host
            // stateID is irrelevant if we're a joiner
            int stateID = -1;
            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                stateID = m_gametableFrame.getNewStateId();
            }
            m_gametableFrame.send(PacketManager.makeLinesPacket(lines, m_gametableFrame.getMyPlayerId(), stateID));

            // if we're the host or if we're offline, go ahead and add them now
            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doAddLineSegments(lines, m_gametableFrame.getMyPlayerId(), stateID);
            }
        }
        else
        {
            // state ids are irrelevant on the private layer
            doAddLineSegments(lines, m_gametableFrame.getMyPlayerId(), 0);
        }
    }

    public void doAddLineSegments(LineSegment[] lines, int authorID, int stateID)
    {
        getActiveMap().beginUndoableAction();
        if (lines != null)
        {
            for (int i = 0; i < lines.length; i++)
            {
                getActiveMap().addLine(lines[i]);
            }
        }
        getActiveMap().endUndoableAction(authorID, stateID);
        repaint();
    }

    public void erase(Rectangle r, boolean bColorSpecific, int color)
    {
        if (isPublicMap())
        {
            // if we're the host, push it to everyone and add the lines.
            // if we're a joiner, just push it to the host
            // stateID is irrelevant if we're a joiner
            int stateID = -1;
            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                stateID = m_gametableFrame.getNewStateId();
            }
            m_gametableFrame.send(PacketManager.makeErasePacket(r, bColorSpecific, color, m_gametableFrame
                .getMyPlayerId(), stateID));
            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doErase(r, bColorSpecific, color, m_gametableFrame.getMyPlayerId(), stateID);
            }
        }
        else
        {
            // stateID is irrelevant for the private layer
            doErase(r, bColorSpecific, color, m_gametableFrame.getMyPlayerId(), 0);
        }
    }

    public void doErase(Rectangle r, boolean bColorSpecific, int color, int authorID, int stateID)
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

        getActiveMap().beginUndoableAction();
        // now we have just the survivors
        // replace all the lines with this list
        getActiveMap().clearLines();
        for (int i = 0; i < survivingLines.size(); i++)
        {
            getActiveMap().addLine((LineSegment)survivingLines.get(i));
        }
        getActiveMap().endUndoableAction(authorID, stateID);
        repaint();
    }

    // called by the pogs area when a pog is being dragged
    public void pogDrag()
    {
        m_bPogBeingDragged = true;
        updatePogDropLoc();

        repaint();
    }

    private Point getPogDragMousePosition()
    {
        Point screenMousePoint = getPogPanel().getGrabPosition();
        Point canvasView = UtilityFunctions.getComponentCoordinates(this, screenMousePoint);

        return viewToModel(canvasView);
    }

    public void pogDrop()
    {
        m_bPogBeingDragged = false;
        updatePogDropLoc();

        Pog pog = getPogPanel().getGrabbedPog();
        if (pog != null)
        {
            // only add the pog if it's in the viewport
            if (isPointVisible(getPogDragMousePosition()))
            {
                // add this pog to the list
                addPog(pog);
            }
        }

        // make the arrow the current tool
        setActiveTool(0);
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
        PogPanel panel = getPogPanel();
        Point screenMousePoint = panel.getGrabPosition();
        Point pogGrabOffset = panel.getGrabOffset();

        // convert to our coordinates
        Point canvasView = UtilityFunctions.getComponentCoordinates(this, screenMousePoint);

        // now convert to model coordinates
        Point canvasModel = viewToModel(canvasView);
        panel.getGrabbedPog().setPosition(canvasModel.x - pogGrabOffset.x, canvasModel.y - pogGrabOffset.y);

        // now, snap to grid if they don't have the control key down
        if (!m_bControlKeyDown)
        {
            snapPogToGrid(panel.getGrabbedPog());
        }
    }

    public void snapPogToGrid(Pog pog)
    {
        m_gridMode.snapPogToGrid(pog);
    }

    public void scrollMapTo(int modelX, int modelY)
    {
        Point target = modelToDraw(modelX, modelY);
        setPrimaryScroll(getActiveMap(), target.x, target.y);
        repaint();
    }

    public Point snapViewPoint(Point viewPoint)
    {
        Point modelPoint = viewToModel(viewPoint);
        Point modelSnap = m_gridMode.snapPoint(modelPoint);
        Point viewSnap = modelToView(modelSnap);
        return viewSnap;
    }

    public Point snapPoint(Point modelPoint)
    {
        return m_gridMode.snapPoint(modelPoint);
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

    private boolean isPointing()
    {
        Player me = m_gametableFrame.getMyPlayer();
        return me.isPointing();
    }

    private void pointAt(Point pointLocation)
    {
        Player me = m_gametableFrame.getMyPlayer();

        if (pointLocation == null)
        {
            me.setPointing(false);
            m_gametableFrame.send(PacketManager.makePointPacket(m_gametableFrame.getMyPlayerIndex(), 0, 0, false));
            repaint();
            return;
        }

        me.setPointing(true);
        me.setPoint(pointLocation);

        m_gametableFrame.send(PacketManager.makePointPacket(m_gametableFrame.getMyPlayerIndex(), me.getPoint().x, me
            .getPoint().y, true));

        setToolCursor(-1);

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
        g.setFont(Font.decode("sans-12"));
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
            HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ((Graphics2D)g).addRenderingHints(HINTS);
        }

        // if they're on the public layer, we draw it first, then the private layer
        // on top of it at half alpha.
        // if they're on the priavet layer, we draw the public layer on white at half alpha,
        // then the private layer at full alpha

        if (isPublicMap())
        {
            // they are on the public map. Draw the public map as normal,
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            paintMap(g, m_publicMap);
        }
        else
        {
            // they're on the private map. First, draw the public map as normal.
            // Then draw a 50% alpha sheet over it. then draw the private map
            paintMap(g, getPublicMap());

            g.setColor(OVERLAY_COLOR); // OVERLAY_COLOR is white with 50% alpha
            g.fillRect(0, 0, getWidth(), getHeight());

            /*
             * Graphics2D g2 = (Graphics2D)g.create();
             * g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); g2.setColor(Color.WHITE);
             * g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
             */

            // now draw the private layer
            paintMap(g, m_privateMap);
        }
    }

    public void paintMap(Graphics g, GametableMap mapToDraw)
    {
        g.translate(-mapToDraw.getScrollX(), -mapToDraw.getScrollY());

        // we don't draw the matte if we're on the private map)
        if (mapToDraw != m_privateMap)
        {
            drawMatte(g, mapToDraw.getScrollX(), mapToDraw.getScrollY(), getWidth(), getHeight());
        }

        // draw all the underlays here
        for (int i = 0; i < mapToDraw.getNumPogs(); i++)
        {
            Pog pog = mapToDraw.getPogAt(i);
            if (pog.isUnderlay())
            {
                pog.drawToCanvas(g);
            }
        }

        // we don't draw the underlay being dragged if we're not
        // drawing the current map
        if (mapToDraw == getActiveMap())
        {
            // if they're dragging an underlay, draw it here
            // there could be a pog drag in progress
            if (m_bPogBeingDragged)
            {
                if (isPointVisible(getPogDragMousePosition()))
                {
                    Pog pog = getPogPanel().getGrabbedPog();

                    if (pog.isUnderlay())
                    {
                        pog.drawGhostlyToCanvas(g);
                    }
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
        }

        // we don't draw the grid if we're on the private map)
        if (mapToDraw != m_privateMap)
        {
            m_gridMode.drawLines(g, mapToDraw.getScrollX(), mapToDraw.getScrollY(), getWidth(), getHeight());
        }

        // lines
        for (int i = 0; i < mapToDraw.getNumLines(); i++)
        {
            LineSegment ls = mapToDraw.getLineAt(i);

            // LineSegments police themselves, performance wise. If they won't touch the current
            // viewport, they don't draw
            ls.draw(g, this);
        }

        // pogs
        for (int i = 0; i < mapToDraw.getNumPogs(); i++)
        {
            Pog pog = mapToDraw.getPogAt(i);
            if (!pog.isUnderlay())
            {
                pog.drawToCanvas(g);
            }
        }

        // we don't draw the pog being dragged if we're not
        // drawing the current map
        if (mapToDraw == getActiveMap())
        {
            // there could be a pog drag in progress
            if (m_bPogBeingDragged)
            {
                if (isPointVisible(getPogDragMousePosition()))
                {

                    Pog pog = getPogPanel().getGrabbedPog();

                    if (!pog.isUnderlay())
                    {
                        pog.drawGhostlyToCanvas(g);
                    }
                }
            }
        }

        // draw the cursor overlays
        g.setFont(Font.decode("system-bold-12"));
        List players = m_gametableFrame.getPlayers();
        for (int i = 0; i < players.size(); i++)
        {
            Player plr = (Player)players.get(i);
            if (plr.isPointing())
            {
                // draw this player's point cursor
                Point pointingAt = modelToDraw(plr.getPoint().x, plr.getPoint().y);
                int idx = i % m_pointCursorImages.length;
                Image hand = m_pointCursorImages[idx];

                // 5px offset to align with mouse pointer
                int drawX = pointingAt.x;
                int drawY = pointingAt.y - 5;
                g.drawImage(hand, drawX, drawY, null);
                FontMetrics fm = g.getFontMetrics();
                drawY -= fm.getHeight() + 2;
                Rectangle r = fm.getStringBounds(plr.getCharacterName(), g).getBounds();
                r.height -= fm.getLeading();
                r.width -= 1;
                final int padding = 3;
                r.grow(padding, 0);
                g.setColor(new Color(192, 192, 192, 128));
                g.fillRect(drawX - padding, drawY, r.width, r.height);
                g.setColor(Color.BLACK);
                g.drawRect(drawX - padding, drawY, r.width - 1, r.height - 1);
                g.drawString(plr.getCharacterName(), drawX, drawY + fm.getAscent() - fm.getLeading());
            }
        }

        // mousing around
        Pog mouseOverPog = null;
        if (m_bMouseOnView)
        {
            mouseOverPog = mapToDraw.getPogAt(m_mouseModelFloat);
            if (m_bShiftKeyDown)
            {
                // this shift key is down. Show all pog data
                for (int i = 0; i < mapToDraw.getNumPogs(); i++)
                {
                    Pog pog = mapToDraw.getPogAt(i);
                    if (pog != mouseOverPog)
                    {
                        pog.drawTextToCanvas(g, false);
                    }
                }
            }

            if (mouseOverPog != null)
            {
                mouseOverPog.drawTextToCanvas(g, true);
            }
        }

        // most prevalent of the pog text is any recently changed pog text
        for (int i = 0; i < mapToDraw.getNumPogs(); i++)
        {
            Pog pog = mapToDraw.getPogAt(i);
            if (pog != mouseOverPog)
            {
                pog.drawChangedTextToCanvas(g);
            }
        }

        if (mapToDraw == getActiveMap())
        {
            getActiveTool().paint(g);
        }

        g.translate(mapToDraw.getScrollX(), mapToDraw.getScrollY());
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
}
