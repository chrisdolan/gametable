/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class ChatLogPane extends JEditorPane
{
    // --- Constants -------------------------------------------------------------------------------------------------

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
    public static final String DEFAULT_TEXT_FOOTER = "</body></html>";
    public static final String DEFAULT_TEXT        = DEFAULT_TEXT_HEADER + DEFAULT_TEXT_FOOTER;

    private static final Font  FONT_ROLLOVER       = Font.decode("sans-12");
    private static final Color COLOR_ROLLOVER      = new Color(0xFF, 0xFF, 0x7F, 0xAF);

    // --- Members ---------------------------------------------------------------------------------------------------

    private JScrollPane        scrollPane;
    private List               entries             = new ArrayList();
    private String             rolloverText        = null;
    private Point              rolloverPosition    = null;
    private Point              mousePosition       = new Point();

    // --- Constructors ----------------------------------------------------------------------------------------------

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
        InputMap map = new InputMap();
        setInputMap(WHEN_FOCUSED, map);

        addHyperlinkListener(new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                {
                    if (e.getURL().getProtocol().equals("gtuser"))
                    {
                        String username = UtilityFunctions.urlDecode(e.getURL().getHost());
                        GametableFrame.getGametableFrame().startTellTo(username);
                        return;
                    }

                    UtilityFunctions.launchBrowser(e.getURL().toString());
                }
                else if (e.getEventType().equals(HyperlinkEvent.EventType.ENTERED))
                {
                    if (e.getURL().getProtocol().equals("gtuser"))
                    {
                        String username = UtilityFunctions.urlDecode(e.getURL().getHost());
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
            public void mouseDragged(MouseEvent e)
            {
                mouseMoved(e);
            }

            /*
             * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
             */
            public void mouseMoved(MouseEvent e)
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
            public void mouseExited(MouseEvent e)
            {
                clearRollover();
            }
        });

        addText("Welcome to <a href=\"http://gametable.galactanet.com/\">" + GametableApp.VERSION + "</a>.");
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * @return the pane to add to UIs
     */
    public Component getComponentToAdd()
    {
        if (scrollPane == null)
        {
            scrollPane = new JScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        return scrollPane;
    }

    public void addText(String text)
    {
        entries.add(highlightUrls(text));
        System.out.println("text: " + text);

        StringBuffer bodyContent = new StringBuffer();
        bodyContent.append(DEFAULT_TEXT_HEADER);
        for (int i = 0, size = entries.size(); i < size; ++i)
        {
            String entry = (String)entries.get(i);
            bodyContent.append(entry);
            bodyContent.append("<br>\n");
        }
        bodyContent.append(DEFAULT_TEXT_FOOTER);
        setText(bodyContent.toString());
    }

    private static String highlightUrls(String in)
    {
        final String HTTP_INTRO = "http://";

        StringBuffer out = new StringBuffer();
        int position = 0;
        int nextPosition = position;
        int length = in.length();
        boolean inTag = false;
        while (true)
        {
            if (nextPosition == length)
            {
                break;
            }

            char c = in.charAt(nextPosition);
            //System.out.println("char " + c + " at " + nextPosition + (inTag ? " inTag" : ""));
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
                    char c2 = in.charAt(nextPosition);
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

                String url = in.substring(position, nextPosition);
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

    /*
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (rolloverText != null && rolloverPosition != null)
        {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.addRenderingHints(UtilityFunctions.STANDARD_RENDERING_HINTS);
            g2.setFont(FONT_ROLLOVER);
            Rectangle rect = g2.getFontMetrics().getStringBounds(rolloverText, g2).getBounds();
            int drawX = rolloverPosition.x;
            int drawY = rolloverPosition.y - rect.y + 24;
            rect.x += drawX;
            rect.y += drawY;
            rect.grow(2, 2);
            g2.setColor(COLOR_ROLLOVER);
            g2.fill(rect);
            g2.setColor(Color.BLACK);
            g2.draw(rect);
            g2.drawString(rolloverText, drawX, drawY);
            g2.dispose();
        }
    }

    private void setRolloverText(String text, Point location)
    {
        rolloverText = text;
        rolloverPosition = location;
        repaint();
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
}
