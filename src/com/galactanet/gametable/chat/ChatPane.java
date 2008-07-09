/*
 * ChatPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.chat;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.ViewportLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.galactanet.gametable.util.FontStringSizeEvaluator;
import com.galactanet.gametable.util.IntPair;
import com.galactanet.gametable.util.StringSizeEvaluator;
import com.galactanet.gametable.util.Strings;



/**
 * Brand New Custom Chat Pane
 */
public class ChatPane extends JComponent implements ChatModel.Listener, Scrollable
{
    /**
     * My own viewport layout.
     */
    public class Layout extends ViewportLayout
    {
        private static final long serialVersionUID = -7068851549433208296L;

        /*
         * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
         */
        public void layoutContainer(final Container parent)
        {
            final JViewport viewport = (JViewport)parent;
            final Component view = viewport.getView();

            final Dimension viewSize = new Dimension(view.getPreferredSize());
            final Dimension viewportSize = viewport.getSize();
            final Point viewPosition = viewport.getViewPosition();

            if (viewSize.width <= viewportSize.width)
            {
                viewPosition.x = 0;
            }

            if (viewSize.height <= viewportSize.height)
            {
                viewPosition.y = 0;
                jumpToBottom = true;
            }
            else if (jumpToBottom)
            {
                viewPosition.y = viewSize.height - viewportSize.height;
            }

            if ((viewPosition.x == 0) && (viewportSize.width > viewSize.width))
            {
                viewSize.width = viewportSize.width;
            }

            if ((viewPosition.y == 0) && (viewportSize.height > viewSize.height))
            {
                viewSize.height = viewportSize.height;
            }

            viewport.setViewPosition(viewPosition);
            viewport.setViewSize(viewSize);
        }
    }

    private static class Painter
    {
        private final Graphics2D          gcImage;
        private final ChatModel           model;
        private final Rectangle           viewRect;
        private final StringSizeEvaluator defaultSizeEvaluator = new FontStringSizeEvaluator(defaultFont);
        private final int                 lineX;
        private ChatEntry                 entry;
        private int                       x;
        private int                       y;
        private int                       lineHeight           = 0;
        private int                       drawnUpTo;
        private String                    text;
        private int                       textLength;

        public Painter(final Graphics2D g, final Rectangle view, final ChatModel chatModel)
        {
            gcImage = g;
            model = chatModel;
            viewRect = view;
            lineX = -viewRect.x;
        }

        public void paint()
        {
            final Iterator entryIterator = model.getIteratorForView(viewRect.y, viewRect.height);
            while (entryIterator.hasNext())
            {
                entry = (ChatEntry)entryIterator.next();
                try
                {
                    x = lineX;
                    y = entry.getTop() - viewRect.y;
                    drawnUpTo = 0;
                    text = entry.getText();
                    textLength = text.length();
                    // System.out.println("(" + x + ", " + y + ") - " + entry);

                    final Iterator spanIterator = entry.iterator();
                    while (spanIterator.hasNext())
                    {
                        final Span span = (Span)spanIterator.next();
                        paintUpTo(span.getStartPosition(), null);
                        paintUpTo(span.getEndPosition(), span);
                    }
                    paintUpTo(textLength, null);
                }
                catch (final RuntimeException re)
                {
                    System.err.println("error drawing line \"" + Strings.escape(entry.getText()) + "\"");
                    re.printStackTrace();
                }
            }
        }

        private void paintUpTo(final int end, final Span span)
        {
            while (drawnUpTo < end)
            {
                boolean newline = false;
                final int nextBreak = entry.getIndexOfBreak(drawnUpTo);
                int next = end;
                if (nextBreak <= next)
                {
                    next = nextBreak;
                    newline = true;
                }

                StringSizeEvaluator se;
                final String sub = text.substring(drawnUpTo, end);
                if (span != null)
                {
                    final Font spanFont = span.getSource().getFont(defaultFont);
                    se = new FontStringSizeEvaluator(spanFont);
                    gcImage.setFont(spanFont);

                    final Color c = span.getSource().getTextColor();
                    if (c != null)
                    {
                        gcImage.setColor(c);
                    }
                    else
                    {
                        gcImage.setColor(defaultTextColor);
                    }
                }
                else
                {
                    gcImage.setColor(defaultTextColor);
                    gcImage.setFont(defaultFont);
                    se = defaultSizeEvaluator;
                }

                gcImage.drawString(sub, x, y + gcImage.getFontMetrics().getMaxAscent());
                final IntPair size = se.getStringSize(sub, 0, sub.length());
                if (size.getY() > lineHeight)
                {
                    lineHeight = size.getY();
                }
                x += size.getX();

                drawnUpTo = next;
                if (newline)
                {
                    x = lineX;
                    y += lineHeight;
                    lineHeight = 0;
                }
            }
        }
    }

    /**
     * Inner class that represents a selection.
     */
    class SelectionInfo
    {
        /**
         * The mark is the place in the log where the selection started. For example, when drag-selecting with the
         * mouse, the mark is the position where the mouse was pressed down to start the selection.
         */
        public Point   mark;

        /**
         * The point is the place in the log where the selections ends. For example, when drag-selecting with the mouse,
         * the point is the position that the mouse is currently hovering over.
         */
        public Point   point;

        public int     startLine;
        public int     startCharacter;
        public int     endLine;
        public int     endCharacter;

        /**
         * Says whether this selection is still open (i.e. user is still selecting).
         */
        public boolean open = true;

        public Point getBottom()
        {
            if (mark.y > point.y)
            {
                return mark;
            }

            return point;
        }

        public Point getTop()
        {
            if (mark.y < point.y)
            {
                return mark;
            }

            return point;
        }
    }

    private static final long serialVersionUID                = 6254696693659141495L;
    private static Color      defaultBackgroundColor          = null;
    private static Color      defaultSelectionBackgroundColor = null;
    private static Color      defaultTextColor                = null;
    private static Font       defaultFont                     = null;
    private Image             backBuffer                      = null;
    // private Point mousePosition = new Point(0, 0);
    private ChatModel         model                           = new ChatModel();
    private JScrollPane       scrollPane;
    private boolean           jumpToBottom;

    // private SelectionInfo mSelection;

    public ChatPane()
    {
        if (defaultBackgroundColor == null)
        {
            defaultBackgroundColor = new Color(0xFF, 0xFF, 0xFF);
        }

        if (defaultSelectionBackgroundColor == null)
        {
            defaultSelectionBackgroundColor = new Color(0x00, 0x60, 0x00);
        }

        if (defaultTextColor == null)
        {
            defaultTextColor = new Color(0x00, 0x00, 0x00);
        }

        if (defaultFont == null)
        {
            defaultFont = new Font(Font.SERIF, Font.PLAIN, 14);
        }

        setBackground(defaultBackgroundColor);
        setForeground(defaultTextColor);
        model.addListener(this);
        model.setDefaultFont(defaultFont);
        initializeEventHandlers();
    }

    /**
     * @return the pane to add to UIs
     */
    public Component getComponentToAdd()
    {
        return getScrollPane();
    }

    /**
     * @return Returns the log.
     */
    public ChatModel getModel()
    {
        return model;
    }

    /*
     * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }

    /*
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        return new Dimension(model.getWidth(), model.getHeight());
    }

    /*
     * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction)
    {
        if (orientation == SwingConstants.HORIZONTAL)
        {
            return (getWidth() / 4);
        }

        final StringSizeEvaluator defaultSizeEvaluator = new FontStringSizeEvaluator(model.getDefaultFont());
        final int lineHeight = defaultSizeEvaluator.getStringHeight(" ", 0, 1);
        final int distance = visibleRect.height - lineHeight;
        int targetY;
        if (direction < 0)
        {
            targetY = visibleRect.y - distance;
        }
        else
        {
            targetY = visibleRect.y + distance;
        }

        final int maxY = getHeight() - visibleRect.height;
        if (targetY < 0)
        {
            targetY = 0;
        }

        final int index = model.getIndexAtYPosition(targetY);
        targetY = model.getEntry(index).getTop();

        if (targetY > maxY)
        {
            targetY = maxY;
        }

        final int result = targetY - visibleRect.y;
        return Math.abs(result);
    }

    /*
     * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
     */
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }

    /*
     * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }

    /*
     * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction)
    {
        final StringSizeEvaluator defaultSizeEvaluator = new FontStringSizeEvaluator(model.getDefaultFont());
        if (orientation == SwingConstants.HORIZONTAL)
        {
            return defaultSizeEvaluator.getStringWidth(" ", 0, 1);
        }

        final int distance = defaultSizeEvaluator.getStringHeight(" ", 0, 1);
        int targetY;
        if (direction < 0)
        {
            targetY = visibleRect.y - distance;
        }
        else
        {
            targetY = visibleRect.y + distance;
        }

        final int maxY = getHeight() - visibleRect.height;
        if (targetY < 0)
        {
            targetY = 0;
        }

        targetY -= targetY % distance;

        if (targetY > maxY)
        {
            targetY = maxY;
        }

        return Math.abs(targetY - visibleRect.y);
    }

    /*
     * @see com.tivo.tools.tvnav.logviewer.LogModel.Listener#notifyEntriesAdded()
     */
    public void notifyEntriesAdded()
    {
        setSize(getPreferredSize());
        revalidate();
        repaint();
    }

    /*
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(final Graphics g)
    {
        final Rectangle viewRect = g.getClipBounds();
        // System.out.println("viewRect: " + viewRect);

        ensureBackBuffer(viewRect.width, viewRect.height);
        final Graphics2D gcImage = (Graphics2D)backBuffer.getGraphics();

        gcImage.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        gcImage.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        gcImage.setBackground(defaultBackgroundColor);
        gcImage.setColor(defaultTextColor);
        gcImage.setFont(defaultFont);
        gcImage.clearRect(0, 0, viewRect.width, viewRect.height);

        new Painter(gcImage, viewRect, model).paint();

        gcImage.dispose();
        g.drawImage(backBuffer, viewRect.x, viewRect.y, null);
    }

    // private void endSelection()
    // {
    // if (mSelection == null)
    // {
    // return;
    // }
    //
    // mSelection.open = false;
    // // TODO: kill selection if it contains no characters.
    // }

    private void ensureBackBuffer(final int width, final int height)
    {
        if ((backBuffer == null) || (backBuffer.getWidth(null) != width) || (backBuffer.getHeight(null) != height))
        {
            backBuffer = createImage(width, height);
        }
    }

    private JScrollPane getScrollPane()
    {
        if (scrollPane == null)
        {
            scrollPane = new JScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getViewport().setLayout(new Layout());
            scrollPane.getViewport().addChangeListener(new ChangeListener()
            {
                public void stateChanged(final ChangeEvent e)
                {
                    final Rectangle viewRect = scrollPane.getViewport().getViewRect();
                    model.setWrapWidth(viewRect.width);
                    if (scrollPane.getVerticalScrollBar().getModel().getValueIsAdjusting())
                    {
                        if (viewRect.y + viewRect.height < model.getHeight())
                        {
                            jumpToBottom = false;
                        }
                        else
                        {
                            jumpToBottom = true;
                        }
                    }
                    // updateSelection();
                }
            });

            scrollPane.addFocusListener(new FocusListener()
            {
                /*
                 * @see java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent)
                 */
                public void focusGained(final FocusEvent e)
                {
                    scrollPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), LineBorder
                        .createBlackLineBorder()));
                }

                /*
                 * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
                 */
                public void focusLost(final FocusEvent e)
                {
                    scrollPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 1,
                        1, 1)));
                }
            });

            scrollPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 1, 1, 1)));
        }

        return scrollPane;
    }

    /**
     * Function that initializes all the event handlers.
     */
    private void initializeEventHandlers()
    {
        addMouseListener(new MouseAdapter()
        {
            public void mousePressed(final MouseEvent e)
            {
                getScrollPane().requestFocusInWindow();
                // startSelection();
            }

            public void mouseReleased(final MouseEvent e)
            {
                // endSelection();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(final MouseEvent e)
            {
                mouseMoved(e);
            }

            public void mouseMoved(final MouseEvent e)
            {
                // mousePosition = new Point(e.getX(), e.getY());
                // updateSelection();
            }
        });
    }

    // private void startSelection()
    // {
    // mSelection = new SelectionInfo();
    // mSelection.mark = mousePosition;
    // mSelection.point = mousePosition;
    // updateSelection();
    // }

    // private void updateSelection()
    // {
    // if (mSelection == null)
    // {
    // return;
    // }
    //
    // if (!mSelection.open)
    // {
    // return;
    // }
    //
    // final StringSizeEvaluator evaluator = new FontStringSizeEvaluator(model.getDefaultFont());
    // mSelection.point = mousePosition;
    // mSelection.startLine = model.getIndexAtYPosition(mSelection.getTop().y);
    // mSelection.endLine = model.getIndexAtYPosition(mSelection.getBottom().y);
    // if (mSelection.startLine == mSelection.endLine)
    // {
    // int startX;
    // int endX;
    //
    // if (mSelection.getTop().x < mSelection.getBottom().x)
    // {
    // startX = mSelection.getTop().x;
    // endX = mSelection.getBottom().x;
    // }
    // else
    // {
    // startX = mSelection.getBottom().x;
    // endX = mSelection.getTop().x;
    // }
    // final ChatEntry startEntry = model.getEntry(mSelection.startLine);
    // if (startEntry == null)
    // {
    // mSelection.startCharacter = 0;
    // }
    // else
    // {
    // mSelection.startCharacter = startEntry.getCharacterIndexForXPosition(startX, evaluator);
    // }
    // final ChatEntry entry = model.getEntry(mSelection.endLine);
    // if (entry == null)
    // {
    // mSelection.endCharacter = 0;
    // }
    // else
    // {
    // mSelection.endCharacter = entry.getCharacterIndexForXPosition(endX, evaluator);
    // }
    // }
    // else
    // {
    // mSelection.startCharacter = model.getEntry(mSelection.startLine).getCharacterIndexForXPosition(
    // mSelection.getTop().x, evaluator);
    // final ChatEntry entry = model.getEntry(mSelection.endLine);
    // if (entry == null)
    // {
    // mSelection.endCharacter = 0;
    // }
    // else
    // {
    // mSelection.endCharacter = entry.getCharacterIndexForXPosition(mSelection.getBottom().x, evaluator);
    // }
    // }
    //
    // // System.out.println("selection points: top: " + mSelection.getTop() + ", bottom: " + mSelection.getBottom());
    // // System.out.println("selection lines: [" + mSelection.startLine + ", " + mSelection.startCharacter + "] - "
    // // + " [" + mSelection.endLine + ", " + mSelection.endCharacter + "]");
    //
    // repaint();
    // }
}
