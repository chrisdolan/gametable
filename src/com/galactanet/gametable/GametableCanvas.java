/*
 * GametableCanvas.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import com.galactanet.gametable.tools.NullTool;
import com.galactanet.gametable.tools.Tool;
import com.galactanet.gametable.ui.PogPanel;
import com.galactanet.gametable.util.UtilityFunctions;



/**
 * The main map view of Gametable.
 */
public class GametableCanvas extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener
{
    // grid modes
    public final static int    GRID_MODE_NONE         = 0;
    public final static int    GRID_MODE_SQUARES      = 1;
    public final static int    GRID_MODE_HEX          = 2;

    // the size of a square at max zoom level (0)
    public final static int    BASE_SQUARE_SIZE       = 64;

    public final static int    NUM_ZOOM_LEVELS        = 5;

    private static final float KEYBOARD_SCROLL_FACTOR = 0.5f;
    private static final int   KEYBOARD_SCROLL_TIME   = 300;

    private static final Font  MAIN_FONT              = Font.decode("sans-12");
    /**
     * A singleton instance of the NULL tool.
     */
    private static final Tool  NULL_TOOL              = new NullTool();

    /**
     * This is the color used to overlay on top of the public layer when the user is on the private layer. It's white
     * with 50% alpha
     */
    private static final Color OVERLAY_COLOR          = new Color(255, 255, 255, 128);

    /**
     *
     */
    private static final long  serialVersionUID       = 6250860728974514790L;

    private Image              m_mapBackground;

    // this is the map (or layer) that all players share
    private final GametableMap m_publicMap            = new GametableMap(true);
    // this is the map (or layer) that is private to a specific player
    private final GametableMap m_privateMap           = new GametableMap(false);
    // this points to whichever map is presently active
    private GametableMap       m_activeMap;

    private int                m_activeToolId         = -1;

    private boolean            m_bAltKeyDown;
    private boolean            m_bControlKeyDown;
    private boolean            m_bMouseOnView;
    private boolean            m_bShiftKeyDown;

    // misc flags
    private boolean            m_bSpaceKeyDown;
    private Point              m_deltaScroll;
    // some cursors
    private Cursor             m_emptyCursor;
    // the frame
    private GametableFrame     m_gametableFrame;

    GridMode                   m_gridMode;
    SquareGridMode             m_squareGridMode       = new SquareGridMode();
    HexGridMode                m_hexGridMode          = new HexGridMode();
    GridMode                   m_noGridMode           = new GridMode();

    private Point              m_mouseModelFloat;

    private boolean            m_newPogIsBeingDragged;
    private Pog                m_pogMouseOver;
    private Image              m_pointingImage;
    /**
     * the id of the tool that we switched out of to go to hand tool for a right-click
     */
    private int                m_previousToolId;

    /**
     * true if the current mouse action was initiated with a right-click
     */
    private boolean            m_rightClicking;
    private Point              m_startScroll;
    private boolean            m_scrolling;
    private long               m_scrollTime;
    private long               m_scrollTimeTotal;

    /**
     * This is the number of screen pixels that are used per model pixel. It's never less than 1
     */
    public int                 m_zoom                 = 1;

    // the size of a square at the current zoom level
    public int                 m_squareSize           = getSquareSizeForZoom(m_zoom);
    
    /**
     * Constructor.
     */
    public GametableCanvas()
    {
        setFocusable(true);
        setRequestFocusEnabled(true);

        addMouseListener(this);
        addMouseMotionListener(this);
        addFocusListener(new FocusListener()
        {
            /*
             * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
             */
            public void focusGained(final FocusEvent e)
            {
                final JPanel panel = (JPanel)getParent();
                panel.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), LineBorder
                    .createBlackLineBorder()));
            }

            /*
             * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
             */
            public void focusLost(final FocusEvent e)
            {
                final JPanel panel = (JPanel)getParent();
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
            /**
             * 
             */
            private static final long serialVersionUID = -1053248611112843772L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (isTextFieldFocused())
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
            /**
             * 
             */
            private static final long serialVersionUID = -8422918377090083512L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_bSpaceKeyDown = false;
                pointAt(null);
            }
        });

        getActionMap().put("shiftDown", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 3881440237209743033L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (isTextFieldFocused())
                {
                    return;
                }

                m_bShiftKeyDown = true;
                repaint();
            }
        });

        getActionMap().put("shiftUp", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 4458628987043121905L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_bShiftKeyDown = false;
                repaint();
            }
        });

        getActionMap().put("controlDown", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 7483132144245136048L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_bControlKeyDown = true;
                repaint();
            }
        });

        getActionMap().put("controlUp", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = -3685986269044575610L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_bControlKeyDown = false;
                repaint();
            }
        });

        getActionMap().put("altDown", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1008551504896354075L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                m_bAltKeyDown = true;
                repaint();
            }
        });

        getActionMap().put("altUp", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = -5789160422348881793L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (m_bAltKeyDown)
                {
                    m_bAltKeyDown = false;
                    repaint();
                }
            }
        });

        getActionMap().put("zoomIn", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = -6378089523552259896L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (isTextFieldFocused())
                {
                    return;
                }

                centerZoom(1);
            }
        });

        getActionMap().put("zoomOut", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 3489902228064051594L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (isTextFieldFocused())
                {
                    return;
                }

                centerZoom(-1);
            }
        });

        getActionMap().put("scrollUp", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 3255081196222471923L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (isTextFieldFocused())
                {
                    return;
                }

                if (m_scrolling)
                {
                    return;
                }

                final GametableMap map = getActiveMap();
                final Point p = drawToModel(map.getScrollX(), map.getScrollY()
                    - Math.round(getHeight() * KEYBOARD_SCROLL_FACTOR));
                smoothScrollTo(p.x, p.y);
            }
        });

        getActionMap().put("scrollDown", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 2041156257507421225L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (isTextFieldFocused())
                {
                    return;
                }

                if (m_scrolling)
                {
                    return;
                }

                final GametableMap map = getActiveMap();
                final Point p = drawToModel(map.getScrollX(), map.getScrollY()
                    + Math.round(getHeight() * KEYBOARD_SCROLL_FACTOR));
                smoothScrollTo(p.x, p.y);
            }
        });

        getActionMap().put("scrollLeft", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = -2772860909080008403L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (isTextFieldFocused())
                {
                    return;
                }

                if (m_scrolling)
                {
                    return;
                }

                final GametableMap map = getActiveMap();
                final Point p = drawToModel(map.getScrollX() - Math.round(getWidth() * KEYBOARD_SCROLL_FACTOR), map
                    .getScrollY());
                smoothScrollTo(p.x, p.y);
            }
        });

        getActionMap().put("scrollRight", new AbstractAction()
        {
            /**
             *
             */
            private static final long serialVersionUID = -4782758632637647018L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                if (isTextFieldFocused())
                {
                    return;
                }

                if (m_scrolling)
                {
                    return;
                }

                final GametableMap map = getActiveMap();
                final Point p = drawToModel(map.getScrollX() + Math.round(getWidth() * KEYBOARD_SCROLL_FACTOR), map
                    .getScrollY());
                smoothScrollTo(p.x, p.y);
            }
        });
    }

    public void addCardPog(final Pog toAdd)
    {
        toAdd.assignUniqueId();
        m_privateMap.addPog(toAdd);
        m_gametableFrame.refreshActivePogList();
        repaint();
    }

    public void addLineSegments(final LineSegment[] lines)
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

    public void addPog(final Pog toAdd)
    {
        toAdd.assignUniqueId();
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makeAddPogPacket(toAdd));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doAddPog(toAdd, true);
            }
        }
        else
        {
            doAddPog(toAdd, false);
        }
    }

    public void centerZoom(final int delta)
    {
        // can't do this at all if we're dragging
        if (m_newPogIsBeingDragged)
        {
            return;
        }
        // note the model location of the center
        final Point modelCenter = viewToModel(getWidth() / 2, getHeight() / 2);

        // do the zoom
        setZoom(m_zoom + delta);

        // note the view location of the model center
        final Point viewCenter = modelToView(modelCenter.x, modelCenter.y);

        // note the present actual center
        final int presentCenterX = getWidth() / 2;
        final int presentCenterY = getHeight() / 2;

        // set up the scroll to enforce the center being where it's supposed to be
        final int scrX = getActiveMap().getScrollX() - (presentCenterX - viewCenter.x);
        final int scrY = getActiveMap().getScrollY() - (presentCenterY - viewCenter.y);
        setPrimaryScroll(getActiveMap(), scrX, scrY);
    }

    public void clearUndoStacks()
    {
        // we only clear the public stack. No need to mess with the private one.
        m_publicMap.clearUndos();
    }

    public void doAddLineSegments(final LineSegment[] lines, final int authorID, final int stateID)
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

    public void doAddPog(final Pog toAdd, final boolean bPublicLayerPog)
    {
        GametableMap map = m_privateMap;
        if (bPublicLayerPog)
        {
            map = m_publicMap;
        }
        map.addPog(toAdd);
        m_gametableFrame.refreshActivePogList();
        repaint();
    }

    public void doErase(final Rectangle r, boolean bColorSpecific, final int color, final int authorID,
        final int stateID)
    {
        final Point modelStart = new Point(r.x, r.y);
        final Point modelEnd = new Point(r.x + r.width, r.y + r.height);

        final ArrayList survivingLines = new ArrayList();
        for (int i = 0; i < getActiveMap().getNumLines(); i++)
        {
            final LineSegment ls = getActiveMap().getLineAt(i);

            if (!bColorSpecific || (ls.getColor().getRGB() == color))
            {
                // we are the color being erased, or we're in erase all
                // mode
                final LineSegment[] result = ls.crop(modelStart, modelEnd);

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

    public void doLockPog(final int id, final boolean newLock)
    {
        final Pog toLock = getActiveMap().getPogByID(id);
        if (toLock == null)
        {
            return;
        }

        toLock.setLocked(newLock);

        // this pog moves to the end of the array
        getActiveMap().removePog(toLock);
        getActiveMap().addPog(toLock);
    }

    public void doMovePog(final int id, final int newX, final int newY)
    {
        final Pog toMove = getActiveMap().getPogByID(id);
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

    public void doPogReorder(final Map changes)
    {
        getActiveMap().reorderPogs(changes);
        m_gametableFrame.refreshActivePogList();
    }

    public void doRecenterView(final int modelCenterX, final int modelCenterY, final int zoomLevel)
    {
        // if you recenter for any reason, your tool action is cancelled
        m_gametableFrame.getToolManager().cancelToolAction();

        // make the sent in x and y our center, ad the sent in zoom.
        // So start with the zoom
        setZoom(zoomLevel);

        final Point viewCenter = modelToView(modelCenterX, modelCenterY);

        // find where the top left would have to be, based on our size
        final int tlX = viewCenter.x - getWidth() / 2;
        final int tlY = viewCenter.y - getHeight() / 2;

        // that is our new scroll position
        final Point newModelPoint = viewToModel(tlX, tlY);
        if (PacketSourceState.isHostDumping())
        {
            scrollMapTo(newModelPoint.x, newModelPoint.y);
        }
        else
        {
            smoothScrollTo(newModelPoint.x, newModelPoint.y);
        }
    }

    public void doRedo(final int stateID)
    {
        // the active map should be the public map
        getActiveMap().redo(stateID);
    }

    public void doRemovePog(final int id)
    {
        final Pog toRemove = getActiveMap().getPogByID(id);
        if (toRemove != null)
        {
            getActiveMap().removePog(toRemove);
        }
        m_gametableFrame.refreshActivePogList();
        repaint();
    }

    public void doRemovePogs(final int ids[], final boolean bDiscardCards)
    {
        // make a list of all the pogs that are cards
        final List cardsList = new ArrayList();

        if (bDiscardCards)
        {
            for (int i = 0; i < ids.length; i++)
            {
                final Pog toRemove = getActiveMap().getPogByID(ids[i]);
                if (toRemove.isCardPog())
                {
                    final DeckData.Card card = toRemove.getCard();
                    cardsList.add(card);
                }
            }
        }

        // remove all the offending pogs
        for (int i = 0; i < ids.length; i++)
        {
            doRemovePog(ids[i]);
        }

        if (bDiscardCards)
        {
            // now remove the offending cards
            if (cardsList.size() > 0)
            {
                final DeckData.Card cards[] = new DeckData.Card[cardsList.size()];
                for (int i = 0; i < cards.length; i++)
                {
                    cards[i] = (DeckData.Card)cardsList.get(i);
                }
                m_gametableFrame.discardCards(cards);
            }
        }
    }

    public void doRotatePog(final int id, final double newAngle)
    {
        final Pog toRotate = getActiveMap().getPogByID(id);
        if (toRotate == null)
        {
            return;
        }

        toRotate.setAngle(newAngle);

        // this pog moves to the end of the array
        getActiveMap().removePog(toRotate);
        getActiveMap().addPog(toRotate);

        repaint();
    }

    public void doFlipPog(final int id, final int flipH, final int flipV)
    {
        final Pog toFlip = getActiveMap().getPogByID(id);
        if (toFlip == null)
        {
            return;
        }

        toFlip.setFlip(flipH, flipV);

        // this pog moves to the end of the array
        getActiveMap().removePog(toFlip);
        getActiveMap().addPog(toFlip);

        repaint();
    }

    public void doSetPogData(final int id, final String s, final Map toAdd, final Set toDelete)
    {
        final Pog pog = getActiveMap().getPogByID(id);
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
            for (final Iterator iterator = toDelete.iterator(); iterator.hasNext();)
            {
                final String key = (String)iterator.next();
                pog.removeAttribute(key);
            }
        }

        if (toAdd != null)
        {
            for (final Iterator iterator = toAdd.entrySet().iterator(); iterator.hasNext();)
            {
                final Map.Entry entry = (Map.Entry)iterator.next();
                pog.setAttribute((String)entry.getKey(), (String)entry.getValue());
            }
        }

        m_gametableFrame.refreshActivePogList();
        repaint();
    }

    public void doSetPogSize(final int id, final float size)
    {
        final Pog pog = getActiveMap().getPogByID(id);
        if (pog == null)
        {
            return;
        }

        pog.setFaceSize(size);
        snapPogToGrid(pog);
        repaint();
    }

    public void doUndo(final int stateID)
    {
        // the active map should be the public map
        getActiveMap().undo(stateID);
    }

    // topLeftX and topLeftY are the coordinates of where the
    // top left of the map area is in whatever coordinate system g is set up to be
    public void drawMatte(final Graphics g, final int topLeftX, final int topLeftY, final int width, final int height)
    {
        // background image
        int qx = Math.abs(topLeftX) / m_mapBackground.getWidth(null);
        if (topLeftX < 0)
        {
            qx++;
            qx = -qx;
        }

        int qy = Math.abs(topLeftY) / m_mapBackground.getHeight(null);
        if (topLeftY < 0)
        {
            qy++;
            qy = -qy;
        }

        final int linesXOffset = qx * m_mapBackground.getWidth(null);
        final int linesYOffset = qy * m_mapBackground.getHeight(null);
        final int vLines = width / m_mapBackground.getWidth(null) + 2;
        final int hLines = height / m_mapBackground.getHeight(null) + 2;

        for (int i = 0; i < vLines; i++)
        {
            for (int j = 0; j < hLines; j++)
            {
                g.drawImage(m_mapBackground, i * m_mapBackground.getWidth(null) + linesXOffset, j
                    * m_mapBackground.getHeight(null) + linesYOffset, null);
            }
        }
    }

    public Point drawToModel(final int modelX, final int modelY)
    {
        return drawToModel(new Point(modelX, modelY));
    }

    public Point drawToModel(final Point drawPoint)
    {
        final double squaresX = (double)(drawPoint.x) / (double)m_squareSize;
        final double squaresY = (double)(drawPoint.y) / (double)m_squareSize;

        final int modelX = (int)(squaresX * BASE_SQUARE_SIZE);
        final int modelY = (int)(squaresY * BASE_SQUARE_SIZE);

        return new Point(modelX, modelY);
    }

    public void erase(final Rectangle r, final boolean bColorSpecific, final int color)
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

    public Tool getActiveTool()
    {
        if (m_activeToolId < 0)
        {
            return NULL_TOOL;
        }
        return m_gametableFrame.getToolManager().getToolInfo(m_activeToolId).getTool();
    }

    public GridMode getGridMode()
    {
        return m_gridMode;
    }

    public int getGridModeId()
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

    public int getModifierFlags()
    {
        return ((m_bControlKeyDown ? Tool.MODIFIER_CTRL : 0) | (m_bSpaceKeyDown ? Tool.MODIFIER_SPACE : 0) | (m_bShiftKeyDown ? Tool.MODIFIER_SHIFT : 0) | (m_bShiftKeyDown ? Tool.MODIFIER_ALT : 0));
    }

    private Point getPogDragMousePosition()
    {
        final Point screenMousePoint = getPogPanel().getGrabPosition();
        final Point canvasView = UtilityFunctions.getComponentCoordinates(this, screenMousePoint);

        return viewToModel(canvasView);
    }

    private PogPanel getPogPanel()
    {
        return m_gametableFrame.getPogPanel();
    }

    public Rectangle getVisibleCanvasRect(final int level)
    {
        final Point topLeft = viewToModel(0, 0);

        int canvasW = 0;
        int canvasH = 0;
        
        switch (level)
        {
            case 0:
            {
                canvasW = getWidth();
                canvasH = getHeight();
            }
            break;

            case 1:
            {
                canvasW = (getWidth() * 4) / 3;
                canvasH = (getHeight() * 4) / 3;
            }
            break;

            case 2:
            {
                canvasW = getWidth() * 2;
                canvasH = getHeight() * 2;
            }
            break;

            case 3:
            {
                canvasW = getWidth() * 4;
                canvasH = getHeight() * 4;
            }
            break;

            case 4:
            {
                canvasW = getWidth() * 8;
                canvasH = getHeight() * 8;
            }
            break;
        }

        //final Point bottomRight = m_canvas.viewToModel(bottomRightX, bottomRightY);
        final Rectangle visbleCanvas = new Rectangle(topLeft.x, topLeft.y, canvasW, canvasH);
        
        //System.out.println(topLeft.x + " " + topLeft.y);
        //System.out.println(bottomRight.x + " " + bottomRight.y);

        return visbleCanvas;        
    }

    public static int getSquareSizeForZoom(final int level)
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

    public GametableMap getPrivateMap()
    {
        return m_privateMap;
    }

    public GametableMap getPublicMap()
    {
        return m_publicMap;
    }

    public void init(final GametableFrame frame)
    {
        m_gametableFrame = frame;
        m_mapBackground = UtilityFunctions.getImage("assets/mapbk.png");

        m_pointingImage = UtilityFunctions.getImage("assets/whiteHand.png");

        setPrimaryScroll(m_publicMap, 0, 0);

        // set up the grid modes
        m_squareGridMode.init(this);
        m_hexGridMode.init(this);
        m_noGridMode.init(this);
        m_gridMode = m_squareGridMode;

        addMouseWheelListener(this);
        setZoom(0);
        setActiveTool(0);
    }

    private boolean isPointing()
    {
        final Player me = m_gametableFrame.getMyPlayer();
        return me.isPointing();
    }

    public boolean isPointVisible(final Point modelPoint)
    {
        final Point portalTL = viewToModel(0, 0);
        final Point portalBR = viewToModel(getWidth(), getHeight());
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

    public boolean isPublicMap()
    {
        return (getActiveMap() == m_publicMap);
    }

    /**
     * @return
     */
    public boolean isTextFieldFocused()
    {
        final Component focused = m_gametableFrame.getFocusOwner();
        if (focused instanceof JTextComponent)
        {
            final JTextComponent textComponent = (JTextComponent)focused;
            return textComponent.isEditable();
        }

        return false;
    }

    public void lockPog(final int id, final boolean newLock)
    {
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makeLockPogPacket(id, newLock));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doLockPog(id, newLock);
            }
        }
        else
        {
            doLockPog(id, newLock);
        }
    }

    public Point modelToDraw(final int modelX, final int modelY)
    {
        return modelToDraw(new Point(modelX, modelY));
    }

    // conversion from model to view coordinates
    public Point modelToDraw(final Point modelPoint)
    {
        final double squaresX = (double)modelPoint.x / (double)BASE_SQUARE_SIZE;
        final double squaresY = (double)modelPoint.y / (double)BASE_SQUARE_SIZE;

        final int viewX = (int)Math.round(squaresX * m_squareSize);
        final int viewY = (int)Math.round(squaresY * m_squareSize);

        return new Point(viewX, viewY);
    }

    /*
     * Modified to accomodate grid distance factor
     */
    public double modelToSquares(final double m)
    {
        return (m_gametableFrame.grid_multiplier * m / BASE_SQUARE_SIZE);
    }

    public Point modelToView(final int modelX, final int modelY)
    {
        return modelToView(new Point(modelX, modelY));
    }

    public Point modelToView(final Point modelPoint)
    {
        final double squaresX = (double)modelPoint.x / (double)BASE_SQUARE_SIZE;
        final double squaresY = (double)modelPoint.y / (double)BASE_SQUARE_SIZE;

        int viewX = (int)Math.round(squaresX * m_squareSize);
        int viewY = (int)Math.round(squaresY * m_squareSize);

        viewX -= getActiveMap().getScrollX();
        viewY -= getActiveMap().getScrollY();

        return new Point(viewX, viewY);
    }

    public void mouseClicked(final MouseEvent e)
    {
        // Ignore this because java has sucky mouse clicking
    }

    /** *********************************************************** */
    // MouseListener/MouseMotionListener overrides:
    /** *********************************************************** */
    public void mouseDragged(final MouseEvent e)
    {
        // We handle dragging ourselves - don't tread on me, Java!
        mouseMoved(e);
    }

    public void mouseEntered(final MouseEvent e)
    {
        m_bMouseOnView = true;
    }

    public void mouseExited(final MouseEvent e)
    {
        m_bMouseOnView = false;
    }

    public void mouseMoved(final MouseEvent e)
    {
        m_mouseModelFloat = viewToModel(e.getX(), e.getY());
        if (isPointing())
        {
            return;
        }
        m_gametableFrame.getToolManager().mouseMoved(m_mouseModelFloat.x, m_mouseModelFloat.y, getModifierFlags());
        final Pog prevPog = m_pogMouseOver;
        if (prevPog != m_pogMouseOver)
        {
            repaint();
        }
    }

    public void mousePressed(final MouseEvent e)
    {
        requestFocus();
        m_mouseModelFloat = viewToModel(e.getX(), e.getY());
        if (isPointing())
        {
            return;
        }

        // this code deals with making a right click automatically be the hand tool
        if (e.getButton() == MouseEvent.BUTTON3)
        {
            m_rightClicking = true;
            m_previousToolId = m_activeToolId;
            setActiveTool(1); // HACK -- To hand tool
            m_gametableFrame.getToolManager().mouseButtonPressed(m_mouseModelFloat.x, m_mouseModelFloat.y,
                getModifierFlags());
        }
        else
        {
            m_rightClicking = false;
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                m_gametableFrame.getToolManager().mouseButtonPressed(m_mouseModelFloat.x, m_mouseModelFloat.y,
                    getModifierFlags());
            }
        }

    }

    public void mouseReleased(final MouseEvent e)
    {
        m_mouseModelFloat = viewToModel(e.getX(), e.getY());
        if (isPointing())
        {
            return;
        }
        m_gametableFrame.getToolManager().mouseButtonReleased(m_mouseModelFloat.x, m_mouseModelFloat.y,
            getModifierFlags());

        if (m_rightClicking)
        {
            // return to arrow too
            setActiveTool(m_previousToolId);
            m_rightClicking = false;
        }
    }

    public void mouseWheelMoved(final MouseWheelEvent e)
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

    public void movePog(final int id, final int newX, final int newY)
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

    public void paintComponent(final Graphics graphics)
    {
        paintComponent(graphics, getWidth(), getHeight());
    }
    
    /**
     * Paint the component to the specified graphics, without limiting to the component's size
     * @param graphics
     * @param width
     * @param height
     */
    private void paintComponent(final Graphics graphics, int width, int height)
    {
        final Graphics2D g = (Graphics2D)graphics.create();
        g.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
        g.setFont(MAIN_FONT);

        // if they're on the public layer, we draw it first, then the private layer
        // on top of it at half alpha.
        // if they're on the priavet layer, we draw the public layer on white at half alpha,
        // then the private layer at full alpha

        if (isPublicMap())
        {
            // they are on the public map. Draw the public map as normal,
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            paintMap(g, m_publicMap, width, height);
        }
        else
        {
            // they're on the private map. First, draw the public map as normal.
            // Then draw a 50% alpha sheet over it. then draw the private map
            paintMap(g, getPublicMap(), width, height);

            g.setColor(OVERLAY_COLOR); // OVERLAY_COLOR is white with 50% alpha
            g.fillRect(0, 0, width, height);

            /*
             * Graphics2D g2 = (Graphics2D)g.create();
             * g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); g2.setColor(Color.WHITE);
             * g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
             */

            // now draw the private layer
            paintMap(g, m_privateMap, width, height);
        }
        g.dispose();
    }
    
    /**
     * export the map to a jpeg image
     * @param mapToExport instance of the map that should be exported.  If null will use the active map
     * @param outputFile file where to save the result
     * @throws IOException if file saving causes an error
     */
    public void exportMap(GametableMap mapToExport, File outputFile) throws IOException
    {
        if (mapToExport == null)
            mapToExport = getActiveMap();
        
        Rectangle mapBounds = getMapBounds(mapToExport);

        int squareSize = GametableCanvas.getSquareSizeForZoom(m_zoom);
        mapBounds.grow(squareSize, squareSize);
        
        BufferedImage image = new BufferedImage(mapBounds.width, mapBounds.height, BufferedImage.TYPE_INT_RGB);        
        Graphics g = image.getGraphics();
        
        int sx = mapToExport.getScrollX();
        int sy = mapToExport.getScrollY();
        
        mapToExport.setScroll(mapBounds.x, mapBounds.y);
        
        paintComponent(g, mapBounds.width, mapBounds.height);
        
        mapToExport.setScroll(sx, sy);
   
        ImageIO.write(image, "jpg", outputFile);
    }
    
    /**
     * Calculate the bounds used by the specified map
     * @param map map to calculate
     * @return coordinates of the space used by the map
     */
    public Rectangle getMapBounds(final GametableMap map)
    {
        Rectangle bounds = null;
        
        // lines
        for (int i = 0; i < map.getNumLines(); i++)
        {
            final LineSegment ls = map.getLineAt(i);
            Rectangle r = ls.getBounds(this);
            
            if (bounds == null)
                bounds = r;
            else
                bounds.add(r);
        }
    
        // pogs
        for (int i = 0; i < map.getNumPogs(); i++)
        {
            final Pog pog = map.getPog(i);
            Rectangle r = pog.getBounds(this);
            
            if (bounds == null)
                bounds = r;
            else
                bounds.add(r);
        }
        
        if (bounds == null)
            bounds = new Rectangle(0, 0, 1, 1);
        
        return bounds;
    }

    public void paintMap(final Graphics g, final GametableMap mapToDraw, int width, int height)
    {
        g.translate(-mapToDraw.getScrollX(), -mapToDraw.getScrollY());

        // we don't draw the matte if we're on the private map)
        if (mapToDraw != m_privateMap)
        {
            drawMatte(g, mapToDraw.getScrollX(), mapToDraw.getScrollY(), width, height);
        }

        // draw all the underlays here
        for (int i = 0; i < mapToDraw.getNumPogs(); i++)
        {
            final Pog pog = mapToDraw.getPog(i);
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
            if (m_newPogIsBeingDragged)
            {
                if (isPointVisible(getPogDragMousePosition()))
                {
                    final Pog pog = getPogPanel().getGrabbedPog();

                    if (pog.isUnderlay())
                    {
                        pog.drawGhostlyToCanvas(g);
                    }
                }
            }
        }

        // we don't draw the grid if we're on the private map)
        if (mapToDraw != m_privateMap)
        {
            m_gridMode.drawLines(g, mapToDraw.getScrollX(), mapToDraw.getScrollY(), width, height);
        }

        // lines
        for (int i = 0; i < mapToDraw.getNumLines(); i++)
        {
            final LineSegment ls = mapToDraw.getLineAt(i);

            // LineSegments police themselves, performance wise. If they won't touch the current
            // viewport, they don't draw
            ls.draw(g, this);
        }

        // pogs
        for (int i = 0; i < mapToDraw.getNumPogs(); i++)
        {
            final Pog pog = mapToDraw.getPog(i);
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
            if (m_newPogIsBeingDragged)
            {
                if (isPointVisible(getPogDragMousePosition()))
                {
                    final Pog pog = getPogPanel().getGrabbedPog();

                    if (!pog.isUnderlay())
                    {
                        pog.drawGhostlyToCanvas(g);
                    }
                }
            }
        }

        // draw the cursor overlays
        final List players = m_gametableFrame.getPlayers();
        for (int i = 0; i < players.size(); i++)
        {
            final Player plr = (Player)players.get(i);
            if (plr.isPointing())
            {
                // draw this player's point cursor
                final Point pointingAt = modelToDraw(plr.getPoint().x, plr.getPoint().y);

                // 5px offset to align with mouse pointer
                final int drawX = pointingAt.x;
                int drawY = pointingAt.y - 5;
                g.drawImage(m_pointingImage, drawX, drawY, null);
                final FontMetrics fm = g.getFontMetrics();
                drawY -= fm.getHeight() + 2;
                final Rectangle r = fm.getStringBounds(plr.getCharacterName(), g).getBounds();
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
        if (m_bMouseOnView || m_gametableFrame.shouldShowNames())
        {
            mouseOverPog = mapToDraw.getPogAt(m_mouseModelFloat);
            if (m_bShiftKeyDown || m_gametableFrame.shouldShowNames())
            {
                // this shift key is down. Show all pog data
                for (int i = 0; i < mapToDraw.getNumPogs(); i++)
                {
                    final Pog pog = mapToDraw.getPog(i);
                    if (pog != mouseOverPog)
                    {
                        pog.drawTextToCanvas(g, false, false);
                    }
                }
            }

            if (mouseOverPog != null)
            {
                mouseOverPog.drawTextToCanvas(g, true, true);
            }
        }

        // most prevalent of the pog text is any recently changed pog text
        for (int i = 0; i < mapToDraw.getNumPogs(); i++)
        {
            final Pog pog = mapToDraw.getPog(i);
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

    // called by the pogs area when a pog is being dragged
    public void pogDrag()
    {
        m_newPogIsBeingDragged = true;
        updatePogDropLoc();

        repaint();
    }

    public void pogDrop()
    {
        m_newPogIsBeingDragged = false;
        updatePogDropLoc();

        final Pog pog = getPogPanel().getGrabbedPog();
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

    public boolean pogInViewport(final Pog pog)
    {
        // only add the pog if they dropped it in the visible area
        final int width = pog.getFaceSize() * BASE_SQUARE_SIZE;

        // get the model coords of the viewable area
        final Point portalTL = viewToModel(0, 0);
        final Point portalBR = viewToModel(getWidth(), getHeight());

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

    private void pointAt(final Point pointLocation)
    {
        final Player me = m_gametableFrame.getMyPlayer();

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

    public void recenterView(final int modelCenterX, final int modelCenterY, final int zoomLevel)
    {
        m_gametableFrame.send(PacketManager.makeRecenterPacket(modelCenterX, modelCenterY, zoomLevel));

        if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
        {
            doRecenterView(modelCenterX, modelCenterY, zoomLevel);
        }
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

    public void removeCardPogsForCards(final DeckData.Card discards[])
    {
        // distribute this to each layer
        m_privateMap.removeCardPogsForCards(discards);
        m_publicMap.removeCardPogsForCards(discards);

        m_gametableFrame.refreshActivePogList();
        repaint();
    }

    public void removePog(final int id)
    {
        removePog(id, true);
    }

    public void removePog(final int id, final boolean bDiscardCards)
    {
        final int removeArray[] = new int[1];
        removeArray[0] = id;
        removePogs(removeArray, bDiscardCards);
    }

    /*
    * Pass the ability to check NetStatus up the chain of object calls
    */
    public int getNetStatus ( )
    {
        return m_gametableFrame.getNetStatus();
    }

    public void removePogs(final int ids[], final boolean bDiscardCards)
    {
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makeRemovePogsPacket(ids));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doRemovePogs(ids, bDiscardCards);
            }
        }
        else
        {
            doRemovePogs(ids, bDiscardCards);
        }
    }

    public void reorderPogs(final Map changes)
    {
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makePogReorderPacket(changes));
            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doPogReorder(changes);
            }
        }
        else
        {
            doPogReorder(changes);
        }
    }

    public void rotatePog(final int id, final double newAngle)
    {
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makeRotatePogPacket(id, newAngle));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doRotatePog(id, newAngle);
            }
        }
        else
        {
            doRotatePog(id, newAngle);
        }
    }

    public void flipPog(final int id, final int flipH, final int flipV)
    {
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makeFlipPogPacket(id, flipH, flipV));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doFlipPog(id, flipH, flipV);
            }
        }
        else
        {
            doFlipPog(id, flipH, flipV);
        }
    }
    public void scrollMapTo(final int modelX, final int modelY)
    {
        final Point target = modelToDraw(modelX, modelY);
        setPrimaryScroll(getActiveMap(), target.x, target.y);
        repaint();
    }

    public void scrollToPog(final Pog pog)
    {
        Point pogModel = new Point(pog.getX() + (pog.getWidth() / 2), pog.getY() + (pog.getHeight() / 2));
        final Point pogView = modelToView(pogModel);
        pogView.x -= (getWidth() / 2);
        pogView.y -= (getHeight() / 2);
        pogModel = viewToModel(pogView);
        smoothScrollTo(pogModel.x, pogModel.y);
    }

    public void setActiveMap(final GametableMap map)
    {
        m_activeMap = map;
    }

    public void setActiveTool(final int index)
    {
        final Tool oldTool = getActiveTool();
        oldTool.deactivate();

        m_activeToolId = index;

        final Tool tool = getActiveTool();
        tool.activate(this);
        setToolCursor(0);
        m_gametableFrame.setToolSelected(m_activeToolId);
    }

    public void setGridModeByID(final int id)
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

    public void setPogData(final int id, final String s, final Map toAdd, final Set toDelete)
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

    public void setPogSize(final int id, final float size)
    {
        if (isPublicMap())
        {
            m_gametableFrame.send(PacketManager.makePogSizePacket(id, size));

            if (m_gametableFrame.getNetStatus() != GametableFrame.NETSTATE_JOINED)
            {
                doSetPogSize(id, size);
            }
        }
        else
        {
            doSetPogSize(id, size);
        }
    }

    /*
     * This function will set the scroll for all maps, keeping their relative offsets preserved. The x,y values sent in
     * will become the scroll values for the desired map. All others maps will preserve offsets from that.
     */
    public void setPrimaryScroll(final GametableMap mapToSet, final int x, final int y)
    {
        m_publicMap.setScroll(x, y);
        m_privateMap.setScroll(x, y);
        /*
         * int dx = x - mapToSet.getScrollX(); int dy = y - mapToSet.getScrollY(); m_publicMap.setScroll(dx +
         * mapToSet.getScrollX(), dy + mapToSet.getScrollY()); m_privateMap.setScroll(dx + mapToSet.getScrollX(), dy +
         * mapToSet.getScrollY());
         */
    }

    /**
     * Sets the mouse cursor to be the cursor at the specified index for the currently active tool.
     * 
     * @param index The cursor of the given index for this tool. A negative number means no cursor.
     */
    public void setToolCursor(final int index)
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

    public void setZoom(final int zl)
    {
        int zoomLevel = zl;
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

    public void smoothScrollTo(final int modelX, final int modelY)
    {
        final GametableMap map = getActiveMap();
        m_startScroll = drawToModel(map.getScrollX(), map.getScrollY());
        m_deltaScroll = new Point(modelX - m_startScroll.x, modelY - m_startScroll.y);
        m_scrollTime = 0;
        m_scrollTimeTotal = KEYBOARD_SCROLL_TIME;
        m_scrolling = true;
    }

    public void snapPogToGrid(final Pog pog)
    {
        m_gridMode.snapPogToGrid(pog);
    }

    public Point snapPoint(final Point modelPoint)
    {
        return m_gridMode.snapPoint(modelPoint);
    }

    // --- Drawing ---

    public Point snapViewPoint(final Point viewPoint)
    {
        final Point modelPoint = viewToModel(viewPoint);
        final Point modelSnap = m_gridMode.snapPoint(modelPoint);
        final Point viewSnap = modelToView(modelSnap);
        return viewSnap;
    }

    public void tick(final long ms)
    {
        if (m_scrolling)
        {
            m_scrollTime += ms;
            float pos = m_scrollTime / (float)m_scrollTimeTotal;
            if (pos >= 1f)
            {
                scrollMapTo(m_startScroll.x + m_deltaScroll.x, m_startScroll.y + m_deltaScroll.y);
                m_scrolling = false;
            }
            else
            {
                pos = (float)(Math.sin((pos * Math.PI) - (Math.PI / 2)) + 1) / 2;
                final int x = m_startScroll.x + Math.round(m_deltaScroll.x * pos);
                final int y = m_startScroll.y + Math.round(m_deltaScroll.y * pos);
                scrollMapTo(x, y);
            }
        }
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

    public void updatePogDropLoc()
    {
        final PogPanel panel = getPogPanel();
        final Point screenMousePoint = panel.getGrabPosition();
        final Point pogGrabOffset = panel.getGrabOffset();

        // convert to our coordinates
        final Point canvasView = UtilityFunctions.getComponentCoordinates(this, screenMousePoint);

        // now convert to model coordinates
        final Point canvasModel = viewToModel(canvasView);
        final Pog grabbedPog = panel.getGrabbedPog();

        // now, snap to grid if they don't have the control key down
        if (!m_bControlKeyDown)
        {
// Removed the adjustment part, because it was actually making the dragging worse
//            final Point adjustment = grabbedPog.getSnapDragAdjustment();
//            grabbedPog.setPosition(canvasModel.x - pogGrabOffset.x + adjustment.x, canvasModel.y - pogGrabOffset.y
//                + adjustment.y);
            grabbedPog.setPosition(canvasModel.x - pogGrabOffset.x, canvasModel.y - pogGrabOffset.y);
            snapPogToGrid(grabbedPog);
        }
        else
        {
            grabbedPog.setPosition(canvasModel.x - pogGrabOffset.x, canvasModel.y - pogGrabOffset.y);
        }
    }

    public Point viewToModel(final int viewX, final int viewY)
    {
        return viewToModel(new Point(viewX, viewY));
    }

    public Point viewToModel(final Point viewPoint)
    {
        final double squaresX = (double)(viewPoint.x + getActiveMap().getScrollX()) / (double)m_squareSize;
        final double squaresY = (double)(viewPoint.y + getActiveMap().getScrollY()) / (double)m_squareSize;

        final int modelX = (int)(squaresX * BASE_SQUARE_SIZE);
        final int modelY = (int)(squaresY * BASE_SQUARE_SIZE);

        return new Point(modelX, modelY);
    }

    public static void drawDottedRect(final Graphics g, final int ix, final int iy, final int iWidth, final int iHeight)
    {
        final Graphics2D g2d = (Graphics2D)g;
        final Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
            2f
        }, 0f));

        int x = ix;
        int y = iy;
        int width = iWidth;
        int height = iHeight;
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

    public static void drawDottedLine(final Graphics g, final int x, final int y, final int x2, final int y2)
    {
        final Graphics2D g2d = (Graphics2D)g;
        final Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
            2f
        }, 0f));
        g.drawLine(x, y, x2, y2);
        g2d.setStroke(oldStroke);
    }
}
