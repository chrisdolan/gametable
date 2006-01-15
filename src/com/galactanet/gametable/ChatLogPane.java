/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;



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
                                                       + ".serif { font-family: serif; }"
                                                       + ".no-serif { font-family: sans-serif; }"
                                                       + ".big { font-size: 14pt; }" + ".no-big { font-size: 12pt; }"
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
        // System.out.println("text: " + text);
        entries.add(text);

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
}
