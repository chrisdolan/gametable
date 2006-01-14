/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;



public class ChatLogEntryPane extends JEditorPane
{
    // --- Types -----------------------------------------------------------------------------------------------------

    private class StyleAction extends AbstractAction
    {
        String style;

        StyleAction(String styleName)
        {
            style = styleName;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            HTMLDocument doc = (HTMLDocument)getDocument();
            Caret c = getCaret();
            int start = Math.min(c.getMark(), c.getDot());
            int end = Math.max(c.getMark(), c.getDot());

            AttributeSet styleOn = getCleanStyle(doc, "." + style);
            AttributeSet styleOff = getCleanStyle(doc, ".no-" + style);

            if (start == end)
            {
                AttributeSet current = getCurrentStyle();
//                System.out.println("styleOn: " + getAttributeString(styleOn));
//                System.out.println("current: " + getAttributeString(current));

                if (currentStyle == null)
                {
                    currentStyle = new SimpleAttributeSet(current);
                }

                if (current.containsAttributes(styleOn))
                {
//                    System.out.println("contains");
                    currentStyle.removeAttributes(styleOn);
                    currentStyle.addAttributes(styleOff);
                }
                else
                {
//                    System.out.println("does not contain");
                    currentStyle.addAttributes(styleOn);
                    currentStyle.removeAttributes(styleOff);
                }

                getAttributeString(currentStyle);
                //                System.out.println("currentStyle: " + getAttributeString(currentStyle));
                return;
            }

            applyStyle(doc, start, end, styleOn, styleOff);
            //System.out.println("entryBox: " + UtilityFunctions.getBodyContent(getText()));
        }
    }

    // --- Members ---------------------------------------------------------------------------------------------------

    private MutableAttributeSet currentStyle = null;
    private GametableFrame      frame;

    // --- Constructors ----------------------------------------------------------------------------------------------

    public ChatLogEntryPane(GametableFrame parentFrame)
    {
        super("text/html", ChatLogPane.DEFAULT_TEXT);
        frame = parentFrame;
        initialize();
        clear();
    }

    /**
     * @return the component to add to UIs
     */
    public Component getComponentToAdd()
    {
        return this;
    }

    /**
     * Clears this text pane.
     */
    public void clear()
    {
        setText(ChatLogPane.DEFAULT_TEXT);
    }

    /**
     * @return the useful part of the text of this component.
     */
    public String getUserText()
    {
        return UtilityFunctions.getBodyContent(getText());
    }

    /**
     * Initializes this object.
     */
    private void initialize()
    {
        setEditable(true);
        setFocusable(true);
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed ENTER"), "enter");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed B"), "bold");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed I"), "italics");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed U"), "underline");
        addKeyListener(new KeyAdapter()
        {
            /*
             * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
             */
            public void keyTyped(KeyEvent e)
            {
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                {
                    return;
                }

                HTMLDocument doc = (HTMLDocument)getDocument();
                String charStr = String.valueOf(e.getKeyChar());
                if (currentStyle != null)
                {
                    try
                    {
                        int dotPos = getCaret().getDot();
                        doc.insertAfterEnd(doc.getCharacterElement(dotPos), charStr);
                        doc.setCharacterAttributes(dotPos, charStr.length(), currentStyle, false);
                        setCaretPosition(dotPos);
                        setCaretPosition(dotPos + charStr.length());
                    }
                    catch (Exception ex)
                    {
                        Log.log(Log.SYS, ex);
                    }
                    currentStyle = null;
                    e.consume();
                }
            }
        });

        getActionMap().put("bold", new StyleAction("bold"));
        getActionMap().put("italics", new StyleAction("italics"));
        getActionMap().put("underline", new StyleAction("underline"));

        getActionMap().put("enter", new AbstractAction()
        {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e)
            {
                frame.submitEntryText();
            }
        });
    }

    private AttributeSet getCurrentStyle()
    {
        if (currentStyle != null)
        {
            return currentStyle;
        }

        HTMLDocument doc = (HTMLDocument)getDocument();
        int pos = getCaretPosition();
        if (pos != doc.getStartPosition().getOffset() + 1)
        {
            --pos;
        }
        return doc.getCharacterElement(pos).getAttributes();
    }

    private static AttributeSet getCleanStyle(HTMLDocument doc, String name)
    {
        return getCleanStyle(doc.getStyle(name));
    }

    private static AttributeSet getCleanStyle(AttributeSet attributes)
    {
        SimpleAttributeSet retVal = new SimpleAttributeSet(attributes);
        retVal.removeAttribute(StyleConstants.NameAttribute);
        return retVal;
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
            if (styleOff != null)
            {
                doc.setCharacterAttributes(start, end - start, styleOff, false);
            }
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

    private static String getAttributeString(AttributeSet attributes)
    {
        AttributeSet clean = getCleanStyle(attributes);
        StringBuffer buffer = new StringBuffer();
        buffer.append('{');
        for (Enumeration e = clean.getAttributeNames(); e.hasMoreElements();)
        {
            Object key = e.nextElement();
            buffer.append(' '); 
            buffer.append(key.toString());
            if (!(key instanceof String))
            {
                buffer.append(" (");
                buffer.append(key.getClass().getName());
                buffer.append(')');
            }
            buffer.append(" => ");
            Object value = clean.getAttribute(key);
            buffer.append(value.toString());
            if (!(value instanceof String))
            {
                buffer.append(" (");
                buffer.append(value.getClass().getName());
                buffer.append(')');
            }
        }
        buffer.append(" }");

        return buffer.toString();
    }
}
