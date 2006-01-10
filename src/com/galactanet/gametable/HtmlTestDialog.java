/*
 * HtmlTestDialog.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;



/**
 * TODO: comment
 * 
 * @author iffy
 */
public class HtmlTestDialog extends JDialog
{

    private JEditorPane htmlPane        = null;
    private JScrollPane scrollPane      = null;
    private JPanel      contentPanel    = null;
    private JEditorPane entryBox        = null;
    private JPanel      bottomPanel     = null;
    private JPanel      buttonPanel     = null;
    private JButton     okButton        = null;
    private JButton     cancelButton    = null;
    private JScrollPane entryScrollPane = null;
    private List        entries         = new ArrayList();

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
            htmlPane = new JEditorPane(
                "text/html",
                "<html><head><style type='text/css'>font-family:Arial,helvetica,sans;font-size:12pt;</style></head><body></body></html>");
            htmlPane.setEditable(false);
            htmlPane.setFocusable(false);
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
            entryBox = new JEditorPane("text/html",
                "<html><head></head><body style=\"font-family:Arial,Helvetica,sans;font-size:12pt;\"></body></html>");
            entryBox.setPreferredSize(new java.awt.Dimension(278, 30));
            entryBox.setContentType("text/html");
            entryBox.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed ENTER"), "enter");
            entryBox.getActionMap().put("enter", new AbstractAction()
            {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    String rawText = entryBox.getText();
                    int end = rawText.lastIndexOf("</body>");
                    int start = rawText.lastIndexOf('>', end) + 1;
                    String cutText = rawText.substring(start, end).trim();

                    int halfLen = cutText.length() / 2;
                    int fontSize = (UtilityFunctions.getRandom(8) - 4);
                    if (fontSize == 0)
                    {
                        fontSize++;
                    }
                    String temp = "<font size=\"" + (fontSize > 0 ? "+" : "") + fontSize + "\">";
                    temp += cutText.substring(0, halfLen);
                    temp += "</font>";
                    temp += cutText.substring(halfLen);

                    entries.add(temp);
                    entryBox.setText("");

                    StringBuffer text = new StringBuffer();
                    text.append("<html><head></head><body style=\"font-family:Arial,Helvetica,sans;font-size:12pt;\">");
                    for (int i = 0, size = entries.size(); i < size; ++i)
                    {
                        String entry = (String)entries.get(i);
                        text.append(entry);
                        text.append("<br>");
                    }
                    text.append("</body></html>");
                    htmlPane.setText(text.toString());
                    System.out.println("htmlPane:\n" + htmlPane.getText());
                }
            });
        }
        return entryBox;
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
