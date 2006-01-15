/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;



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
                                                       + "</style></head><body id=\"bodycontent\">";
    public static final String DEFAULT_TEXT_FOOTER = "</body></html>";
    public static final String DEFAULT_TEXT        = DEFAULT_TEXT_HEADER + DEFAULT_TEXT_FOOTER;

    // --- Members ---------------------------------------------------------------------------------------------------

    private JScrollPane        m_scrollPane;
    private List               entries             = new ArrayList();

    // --- Constructors ----------------------------------------------------------------------------------------------

    /**
     * Default Constructor;
     */
    public ChatLogPane()
    {
        super("text/html", DEFAULT_TEXT);
        setEditable(false);
        setFocusable(true);

        // clear all default keystrokes
        InputMap map = new InputMap();
        setInputMap(WHEN_FOCUSED, map);
    }

    // --- Methods ---------------------------------------------------------------------------------------------------

    /**
     * @return the pane to add to UIs
     */
    public Component getComponentToAdd()
    {
        if (m_scrollPane == null)
        {
            m_scrollPane = new JScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        return m_scrollPane;
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
        int length = in.length();
        while (true)
        {
            int nextPosition = in.indexOf(HTTP_INTRO, position);
            if (nextPosition == -1)
            {
                out.append(in.substring(position));
                break;
            }

            out.append(in.substring(position, nextPosition));
            position = nextPosition;
            for (nextPosition = position + HTTP_INTRO.length(); nextPosition < length; ++nextPosition)
            {
                char c = in.charAt(nextPosition);
                if (Character.isJavaIdentifierPart(c))
                {
                    continue;
                }

                if (Character.isWhitespace(c))
                {
                    break;
                }

                boolean foundEnd = false;
                switch (c)
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
            out.append("<a href=\"");
            out.append(url);
            out.append("\">");
            out.append(url);
            out.append("</a>");
            position = nextPosition;
        }

        return out.toString();
    }
}
