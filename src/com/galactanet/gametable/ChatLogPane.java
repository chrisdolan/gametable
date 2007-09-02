/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument.AbstractElement;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.html.HTMLDocument;



/**
 * TODO: comment
 */
public class ChatLogPane extends JEditorPane
{
    // --- Constants -------------------------------------------------------------------------------------------------

    /**
     * My own viewport layout.
     */
    public class Layout extends ViewportLayout
    {
        /**
         * 
         */
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

    private static final Color COLOR_ROLLOVER      = new Color(0xFF, 0xFF, 0x7F, 0xAF);
    public static final String DEFAULT_TEXT_FOOTER = "</body></html>";

    public static final String DEFAULT_TEXT_HEADER = "<html><head><style type=\'text/css\'>"
                                                       + "body { font-family: sans-serif; font-size: 12pt; }"
                                                       + ".bold { font-weight: bold; }"
                                                       + ".no-bold { font-weight: regular; }"
                                                       + ".italics { font-style: italic; }"
                                                       + ".no-italics { font-style: normal; }"
                                                       + ".underline { text-decoration: underline; }"
                                                       + ".no-underline { text-decoration: none; }"
                                                       + "a.user { color:black; font-weight:bold; text-decoration: none; }"
                                                       + "a.dice { color:black; text-decoration: none; }"
                                                       + "</style></head><body id=\"bodycontent\">";
    public static final String DEFAULT_TEXT        = DEFAULT_TEXT_HEADER + DEFAULT_TEXT_FOOTER;
    private static final Font  FONT_ROLLOVER       = Font.decode("sans-12");

    private static final int   MAX_ENTRIES         = 1000;

    // --- Types -----------------------------------------------------------------------------------------------------

    /**
     * 
     */
    private static final long  serialVersionUID    = 1727146777739049291L;

    // --- Members ---------------------------------------------------------------------------------------------------

    private static String highlightUrls(final String in)
    {
        final String HTTP_INTRO = "http://";

        final StringBuffer out = new StringBuffer();
        int position = 0;
        int nextPosition = position;
        final int length = in.length();
        boolean inTag = false;
        while (true)
        {
            if (nextPosition == length)
            {
                break;
            }

            final char c = in.charAt(nextPosition);
            // System.out.println("char " + c + " at " + nextPosition + (inTag ? " inTag" : ""));
            if (inTag)
            {
                out.append(c);
                if (c == '>')
                {
                    inTag = false;
                }
                ++nextPosition;
                continue;
            }

            if (c == '<')
            {
                out.append(c);
                inTag = true;
                ++nextPosition;
                continue;
            }

            if (in.substring(nextPosition).startsWith(HTTP_INTRO))
            {
                position = nextPosition;
                for (nextPosition = position + HTTP_INTRO.length(); nextPosition < length; ++nextPosition)
                {
                    final char c2 = in.charAt(nextPosition);
                    if (Character.isJavaIdentifierPart(c2))
                    {
                        continue;
                    }

                    if (Character.isWhitespace(c2))
                    {
                        break;
                    }

                    boolean foundEnd = false;
                    switch (c2)
                    {
                        case '&':
                        {
                            final StringBuffer accum = new StringBuffer();
                            while (true)
                            {
                                final char c3 = in.charAt(++nextPosition);
                                if (c3 == ';')
                                {
                                    break;
                                }
                                accum.append(c3);
                            }
                            final String entity = accum.toString();
                            if (!entity.equals("amp"))
                            {
                                foundEnd = true;
                            }
                        }
                        break;
                        case '.':
                        case '%':
                        case '/':
                        case '#':
                        case '?':
                        case '+':
                        case '-':
                        case '=':
                        break;
                        default:
                            foundEnd = true;
                        break;
                    }

                    if (foundEnd)
                    {
                        break;
                    }
                }

                final String url = in.substring(position, nextPosition);
                out.append("<a ");
                if (UtilityFunctions.getRandom(2) > 0)
                {
                    out.append("class=\"test\" ");
                }
                out.append(" href=\"");
                out.append(url);
                out.append("\">");
                out.append(url);
                out.append("</a>");
                position = nextPosition;
            }
            else
            {
                out.append(c);
                ++nextPosition;
            }
        }

        return out.toString();
    }

    private int         endIndex         = 0;
    private List        entries          = new ArrayList();
    private boolean     jumpToBottom     = true;
    private final Point mousePosition    = new Point();
    private Point       rolloverPosition = null;
    private String      rolloverText     = null;

    // --- Constructors ----------------------------------------------------------------------------------------------

    private JScrollPane scrollPane;

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * Default Constructor;
     */
    public ChatLogPane()
    {
        super("text/html", DEFAULT_TEXT);
        setEditable(false);
        setFocusable(true);
        setLayout(null);

        // clear all default keystrokes
        final InputMap map = new InputMap();
        final InputMap oldMap = getInputMap(WHEN_FOCUSED);
        final String action = "copy-to-clipboard";
        final KeyStroke keys[] = oldMap.allKeys();
        for (int i = 0, size = keys.length; i < size; ++i)
        {
            final Object a = oldMap.get(keys[i]);
            if (action.equals(a))
            {
                map.put(keys[i], action);
            }
        }

        setInputMap(WHEN_FOCUSED, map);

        addHyperlinkListener(new HyperlinkListener()
        {
            public void hyperlinkUpdate(final HyperlinkEvent e)
            {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                {
                    if (e.getURL().getProtocol().equals("gtuser"))
                    {
                        final String username = UtilityFunctions.urlDecode(e.getURL().getHost());
                        GametableFrame.getGametableFrame().startTellTo(username);
                        return;
                    }

                    UtilityFunctions.launchBrowser(e.getURL().toString());
                }
                else if (e.getEventType().equals(HyperlinkEvent.EventType.ENTERED))
                {
                    if (e.getURL().getProtocol().equals("gtuser"))
                    {
                        final String username = UtilityFunctions.urlDecode(e.getURL().getHost());
                        setRolloverText("Send a tell to " + username + ".", new Point(mousePosition));
                        return;
                    }

                    setRolloverText(e.getURL().toString(), new Point(mousePosition));
                }
                else if (e.getEventType().equals(HyperlinkEvent.EventType.EXITED))
                {
                    clearRollover();
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener()
        {
            /*
             * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
             */
            public void mouseDragged(final MouseEvent e)
            {
                mouseMoved(e);
            }

            /*
             * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
             */
            public void mouseMoved(final MouseEvent e)
            {
                mousePosition.x = e.getX();
                mousePosition.y = e.getY();
            }
        });

        addMouseListener(new MouseAdapter()
        {
            /*
             * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
             */
            public void mouseExited(final MouseEvent e)
            {
                clearRollover();
            }
        });

        addFocusListener(new FocusAdapter()
        {
            /*
             * @see java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent)
             */
            public void focusGained(final FocusEvent e)
            {
                getScrollPane().setBorder(
                    new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new MatteBorder(1, 1, 1, 1, Color.BLACK)));
            }

            /*
             * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
             */
            public void focusLost(final FocusEvent e)
            {
                getScrollPane().setBorder(
                    new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 1, 1, 1)));
            }
        });

        addText("<br>Welcome to <a href=\"http://gametable.galactanet.com/\">" + GametableApp.VERSION + "</a>.");
    }

    public void addText(final String text)
    {
        final String entryStr = highlightUrls(text);
        final String entryArray[] = entryStr.split("<br[\\s]?>");
        for (int i = 0, size = entryArray.length; i < size; ++i)
        {
            entries.add(entryArray[i]);
            Log.log(Log.PLAY, entryArray[i] + "<br>");
        }

        boolean set = false;
        int visibleLines = (entries.size() - endIndex);
        if (visibleLines > MAX_ENTRIES)
        {
            // System.out.println("shrinking from: " + visibleLines + " ( " + endIndex + " )");
            endIndex += visibleLines / 2;
            visibleLines = (entries.size() - endIndex);
            // System.out.println(" to: " + visibleLines + " ( " + endIndex + " )");
            set = true;
        }

        final HTMLDocument doc = (HTMLDocument)getDocument();
        if (set || (entries.size() < 2))
        {
            final StringBuffer bodyContent = new StringBuffer();
            bodyContent.append(DEFAULT_TEXT_HEADER);
            for (int i = endIndex, size = entries.size(); i < size; ++i)
            {
                final String entry = (String)entries.get(i);
                bodyContent.append(entry);
                bodyContent.append("<br>\n");
            }
            bodyContent.append(DEFAULT_TEXT_FOOTER);
            setText(bodyContent.toString());
        }
        else
        {
            final BranchElement body = (BranchElement)doc.getElement("bodycontent");
            final AbstractElement elem = (AbstractElement)body.getChildAt(body.getChildCount() - 1);
            try
            {
                doc.insertBeforeEnd(elem, entryStr + "<br>");
            }
            catch (final Exception e)
            {
                Log.log(Log.SYS, e);
            }
        }
    }

    /**
     * Clears any rollover text on the panel.
     */
    private void clearRollover()
    {
        if (rolloverText != null)
        {
            rolloverText = null;
            rolloverPosition = null;
            repaint();
        }
    }

    public void clearText()
    {
        setText(DEFAULT_TEXT);
        entries = new ArrayList();
        addText("Welcome to <a href=\"http://gametable.galactanet.com/\">" + GametableApp.VERSION + "</a>.");
    }

    /**
     * @return the pane to add to UIs
     */
    public Component getComponentToAdd()
    {
        return getScrollPane();
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
                /*
                 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
                 */
                public void stateChanged(final ChangeEvent e)
                {
                    final Rectangle viewRect = scrollPane.getViewport().getViewRect();
                    if (viewRect.y + viewRect.height < getHeight() - 10)
                    {
                        jumpToBottom = false;
                    }
                    else
                    {
                        jumpToBottom = true;
                    }
                }
            });

            scrollPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 1, 1, 1)));
        }

        return scrollPane;
    }

    /*
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(final Graphics g)
    {
        super.paintComponent(g);
        if ((rolloverText != null) && (rolloverPosition != null))
        {
            final Graphics2D g2 = (Graphics2D)g.create();
            g2.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
            g2.setFont(FONT_ROLLOVER);
            final Rectangle rect = g2.getFontMetrics().getStringBounds(rolloverText, g2).getBounds();

            final int CURSOR_WIDTH = 16;
            final int CURSOR_HEIGHT = 24;

            final int fontAscent = -rect.y;
            final int fontDescent = rect.height - fontAscent;

            int drawX = rolloverPosition.x;
            int drawY = rolloverPosition.y + fontAscent + CURSOR_HEIGHT;
            rect.x += drawX;
            rect.y += drawY;
            rect.grow(2, 2);

            final Rectangle bounds = getBounds();
            bounds.x = 0;
            bounds.y = 0;
            bounds.grow(-5, -5);

            if (rect.x < bounds.x)
            {
                final int dx = bounds.x - rect.x;
                drawX += dx;
                rect.x += dx;
            }

            if (rect.y < bounds.y)
            {
                final int dy = bounds.y - rect.y;
                final int dx = CURSOR_WIDTH;
                drawY += dy;
                rect.y += dy;
                drawX += dx;
                rect.x += dx;
            }

            if (rect.x + rect.width > bounds.x + bounds.width)
            {
                final int dx = (bounds.x + bounds.width) - (rect.x + rect.width);
                drawX += dx;
                rect.x += dx;
            }

            if (rect.y + rect.height > (bounds.y + bounds.height))
            {
                final int targetY = rolloverPosition.y - fontDescent - 2;
                final int dy = targetY - drawY;
                drawY += dy;
                rect.y += dy;
            }

            g2.setColor(COLOR_ROLLOVER);
            g2.fill(rect);
            g2.setColor(Color.BLACK);
            g2.draw(rect);
            g2.drawString(rolloverText, drawX, drawY);
            g2.dispose();
        }
    }

    private void setRolloverText(final String text, final Point location)
    {
        rolloverText = text;
        rolloverPosition = location;
        repaint();
    }
}
