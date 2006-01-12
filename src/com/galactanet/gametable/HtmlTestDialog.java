/*
 * HtmlTestDialog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;



/**
 * TODO: comment
 * 
 * @author iffy
 */
public class HtmlTestDialog extends JDialog
{

    private JEditorPane         htmlPane            = null;
    private JScrollPane         scrollPane          = null;
    private JPanel              contentPanel        = null;
    private JEditorPane         entryBox            = null;
    private JPanel              bottomPanel         = null;
    private JPanel              buttonPanel         = null;
    private JButton             okButton            = null;
    private JButton             cancelButton        = null;
    private JScrollPane         entryScrollPane     = null;
    private List                entries             = new ArrayList();

    private static final String DEFAULT_TEXT_HEADER = "<html><head><style type=\'text/css\'>"
                                                        + "body { font-family: sans-serif; font-size: 12pt; }"
                                                        + ".bold { font-weight: bold; }"
                                                        + ".no-bold { font-weight: regular; }"
                                                        + ".italics { font-style: italic; }"
                                                        + ".no-italics { font-style: normal; }"
                                                        + ".underline { text-decoration: underline; }"
                                                        + ".no-underline { text-decoration: none; }"
                                                        + ".serif { font-family: serif; }"
                                                        + ".no-serif { font-family: sans-serif; }"
                                                        + "</style></head><body>";
    private static final String DEFAULT_TEXT_FOOTER = "</body></html>";
    private static final String DEFAULT_TEXT        = DEFAULT_TEXT_HEADER + DEFAULT_TEXT_FOOTER;

    /**
     * This is the default constructor
     */
    public HtmlTestDialog()
    {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setSize(300, 500);
        this.setPreferredSize(new Dimension(300, 500));
        this.setMinimumSize(new Dimension(300, 150));
        this.setContentPane(getContentPanel());
        this.setTitle("Html Test");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);
    }

    /**
     * This method initializes htmlPane
     * 
     * @return javax.swing.JEditorPane
     */
    private JEditorPane getHtmlPane()
    {
        if (htmlPane == null)
        {
            htmlPane = new JEditorPane("text/html", DEFAULT_TEXT);
            htmlPane.setEditable(false);
            // htmlPane.setFocusable(false);
        }
        return htmlPane;
    }

    /**
     * This method initializes scrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getScrollPane()
    {
        if (scrollPane == null)
        {
            scrollPane = new JScrollPane();
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setViewportView(getHtmlPane());
        }
        return scrollPane;
    }

    /**
     * This method initializes contentPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getContentPanel()
    {
        if (contentPanel == null)
        {
            BorderLayout borderLayout = new BorderLayout();
            borderLayout.setHgap(5);
            borderLayout.setVgap(5);
            contentPanel = new JPanel();
            contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
            contentPanel.setLayout(borderLayout);
            contentPanel.add(getBottomPanel(), BorderLayout.SOUTH);
            contentPanel.add(getScrollPane(), BorderLayout.CENTER);
        }
        return contentPanel;
    }

    /**
     * This method initializes entryBox
     * 
     * @return javax.swing.JEditorPane
     */
    private JEditorPane getEntryBox()
    {
        if (entryBox == null)
        {
            entryBox = new JEditorPane("text/html", DEFAULT_TEXT);
            entryBox.setPreferredSize(new java.awt.Dimension(278, 30));
            entryBox.setContentType("text/html");
            entryBox.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed ENTER"), "enter");
            entryBox.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed B"), "bold");
            entryBox.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed I"), "italicize");
            entryBox.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed U"), "underline");
            entryBox.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed S"), "serif");

            entryBox.getActionMap().put("bold", new AbstractAction()
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    HTMLDocument doc = (HTMLDocument)entryBox.getDocument();
                    Caret c = entryBox.getCaret();
                    int start = Math.min(c.getMark(), c.getDot());
                    int end = Math.max(c.getMark(), c.getDot());

                    AttributeSet styleOn = getCleanStyle(doc, ".bold");
                    AttributeSet styleOff = getCleanStyle(doc, ".no-bold");
                    applyStyle(doc, start, end, styleOn, styleOff);
                    System.out.println("entryBox: " + getBodyContent(entryBox.getText()));
                }
            });

            entryBox.getActionMap().put("italicize", new AbstractAction()
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    HTMLDocument doc = (HTMLDocument)entryBox.getDocument();
                    Caret c = entryBox.getCaret();
                    int start = Math.min(c.getMark(), c.getDot());
                    int end = Math.max(c.getMark(), c.getDot());

                    AttributeSet styleOn = getCleanStyle(doc, ".italics");
                    AttributeSet styleOff = getCleanStyle(doc, ".no-italics");
                    applyStyle(doc, start, end, styleOn, styleOff);
                    System.out.println("entryBox: " + getBodyContent(entryBox.getText()));
                }
            });

            entryBox.getActionMap().put("underline", new AbstractAction()
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    HTMLDocument doc = (HTMLDocument)entryBox.getDocument();
                    Caret c = entryBox.getCaret();
                    int start = Math.min(c.getMark(), c.getDot());
                    int end = Math.max(c.getMark(), c.getDot());

                    AttributeSet styleOn = getCleanStyle(doc, ".underline");
                    AttributeSet styleOff = getCleanStyle(doc, ".no-underline");
                    applyStyle(doc, start, end, styleOn, styleOff);
                    System.out.println("entryBox: " + getBodyContent(entryBox.getText()));
                }
            });

            entryBox.getActionMap().put("serif", new AbstractAction()
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    HTMLDocument doc = (HTMLDocument)entryBox.getDocument();
                    Caret c = entryBox.getCaret();
                    int start = Math.min(c.getMark(), c.getDot());
                    int end = Math.max(c.getMark(), c.getDot());

                    AttributeSet styleOn = getCleanStyle(doc, ".serif");
                    AttributeSet styleOff = getCleanStyle(doc, ".no-serif");
                    applyStyle(doc, start, end, styleOn, styleOff);
                    System.out.println("entryBox: " + getBodyContent(entryBox.getText()));
                }
            });

            entryBox.getActionMap().put("enter", new AbstractAction()
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    entries.add(getBodyContent(entryBox.getText()));
                    entryBox.setText(DEFAULT_TEXT);

                    StringBuffer text = new StringBuffer();
                    // text.append("<html><head></head><body
                    // style=\"font-family:Arial,Helvetica,sans;font-size:12pt;\">");
                    text.append(DEFAULT_TEXT_HEADER);
                    for (int i = 0, size = entries.size(); i < size; ++i)
                    {
                        String entry = (String)entries.get(i);
                        text.append("<table border=\"0\" cellspaing=\"0\" cellpadding=\"0\"><tr><td valign=\"top\"><b>Iffy&gt;</b></td><td>");
                        text.append(entry);
                        text.append("</td></tr></table>");
                    }
                    text.append(DEFAULT_TEXT_FOOTER);
                    htmlPane.setText(text.toString());
                    System.out.println("htmlPane:\n" + htmlPane.getText());
                }
            });
        }
        return entryBox;
    }

//    private static void dumpAttributes(AttributeSet set)
//    {
//        System.out.println("Set = " + set + " (" + set.getClass() + ")");
//        for (Enumeration e = set.getAttributeNames(); e.hasMoreElements();)
//        {
//            Object name = e.nextElement();
//            Object value = set.getAttribute(name);
//            System.out.println("\t" + name + " (" + name.getClass() + ") => " + value + "(" + value.getClass() + ")");
//        }
//    }

    private static AttributeSet getCleanStyle(HTMLDocument doc, String name)
    {
        return doc.getStyleSheet().removeAttribute(doc.getStyle(name), StyleConstants.NameAttribute);
    }

    private static void applyStyle(HTMLDocument doc, int start, int end, AttributeSet styleOn, AttributeSet styleOff)
    {
        boolean allSet = true;

        List elements = getElementsIn(doc, start, end);
        for (int i = 0, size = elements.size(); i < size; ++i)
        {
            AbstractDocument.LeafElement element = (AbstractDocument.LeafElement)elements.get(i);
            if (!element.containsAttributes(styleOn))
            {
                allSet = false;
                break;
            }
        }

        if (allSet)
        {
            doc.setCharacterAttributes(start, end - start, styleOff, false);
            return;
        }

        doc.setCharacterAttributes(start, end - start, styleOn, false);
    }

    private static List getElementsIn(HTMLDocument doc, int start, int end)
    {
        List retVal = new ArrayList();
        int pos = start;
        while (true)
        {
            AbstractDocument.LeafElement elem = (AbstractDocument.LeafElement)doc.getCharacterElement(pos);
            retVal.add(elem);
            if (elem.getEndOffset() >= end)
            {
                break;
            }
            pos = elem.getEndOffset();
        }

        return retVal;
    }

    private static String getBodyContent(String html)
    {
        int end = html.lastIndexOf("</body>");
        int start = html.indexOf("<body") + "<body".length();
        start = html.indexOf('>', start) + 1;
        return html.substring(start, end).trim();
    }

    /**
     * This method initializes bottomPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getBottomPanel()
    {
        if (bottomPanel == null)
        {
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(getButtonPanel(), BorderLayout.CENTER);
            bottomPanel.add(getEntryScrollPane(), BorderLayout.NORTH);
        }
        return bottomPanel;
    }

    /**
     * This method initializes buttonPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPanel()
    {
        if (buttonPanel == null)
        {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getOkButton(), null);
            buttonPanel.add(getCancelButton(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes okButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getOkButton()
    {
        if (okButton == null)
        {
            okButton = new JButton();
            okButton.setAction(new AbstractAction("Ok")
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    dispose();
                }
            });
        }
        return okButton;
    }

    /**
     * This method initializes cancelButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton()
    {
        if (cancelButton == null)
        {
            cancelButton = new JButton();
            cancelButton.setAction(new AbstractAction("Cancel")
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    dispose();
                }
            });
        }
        return cancelButton;
    }

    /**
     * This method initializes entryScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getEntryScrollPane()
    {
        if (entryScrollPane == null)
        {
            entryScrollPane = new JScrollPane();
            entryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            entryScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            entryScrollPane.setPreferredSize(new Dimension(297, 75));
            entryScrollPane.setViewportView(getEntryBox());
        }
        return entryScrollPane;
    }

}
