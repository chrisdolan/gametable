/*
 * ChatLogPane.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.Component;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;



public class ChatLogEntryPane extends JEditorPane
{
    // --- Types -----------------------------------------------------------------------------------------------------

    private class StyleAction extends AbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6659861238593013643L;
        String                    style;

        StyleAction(final String styleName)
        {
            style = styleName;
        }

        /*
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e)
        {
            toggleStyle(style);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3333422308857068208L;

    // --- Members ---------------------------------------------------------------------------------------------------

    private static void applyStyle(final HTMLDocument doc, final int start, final int end, final AttributeSet styleOn,
        final AttributeSet styleOff)
    {
        boolean allSet = true;

        final List elements = getElementsIn(doc, start, end);
        for (int i = 0, size = elements.size(); i < size; ++i)
        {
            final AbstractDocument.LeafElement element = (AbstractDocument.LeafElement)elements.get(i);
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

    private static String getAttributeString(final AttributeSet attributes)
    {
        final AttributeSet clean = getCleanStyle(attributes);
        final StringBuffer buffer = new StringBuffer();
        buffer.append('{');
        for (final Enumeration e = clean.getAttributeNames(); e.hasMoreElements();)
        {
            final Object key = e.nextElement();
            buffer.append(' ');
            buffer.append(key.toString());
            if (!(key instanceof String))
            {
                buffer.append(" (");
                buffer.append(key.getClass().getName());
                buffer.append(')');
            }
            buffer.append(" => ");
            final Object value = clean.getAttribute(key);
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

    private static AttributeSet getCleanStyle(final AttributeSet attributes)
    {
        final SimpleAttributeSet retVal = new SimpleAttributeSet(attributes);
        retVal.removeAttribute(StyleConstants.NameAttribute);
        return retVal;
    }

    private static AttributeSet getCleanStyle(final HTMLDocument doc, final String name)
    {
        return getCleanStyle(doc.getStyle(name));
    }

    private static List getElementsIn(final HTMLDocument doc, final int start, final int end)
    {
        final List retVal = new ArrayList();
        int pos = start;
        while (true)
        {
            final AbstractDocument.LeafElement elem = (AbstractDocument.LeafElement)doc.getCharacterElement(pos);
            retVal.add(elem);
            if (elem.getEndOffset() >= end)
            {
                break;
            }
            pos = elem.getEndOffset();
        }

        return retVal;
    }

    private final GametableFrame frame;
    /**
     * List of sent items.
     */
    private final List           history         = new ArrayList();
    private int                  historyPosition = 0;

    // --- Constructors ----------------------------------------------------------------------------------------------

    private boolean              ignoreCaret     = false;

    private boolean              lastTypedSent   = false;

    private boolean              spaceTyped      = false;

    private MutableAttributeSet  styleOverride   = null;

    private StyledEntryToolbar   toolbar         = null;

    public ChatLogEntryPane(final GametableFrame parentFrame)
    {
        super("text/html", ChatLogPane.DEFAULT_TEXT);
        frame = parentFrame;
        initialize();
        clear();
    }

    /**
     * Clears this text pane.
     */
    public void clear()
    {
        setText("");
    }

    /**
     * @return the component to add to UIs
     */
    public Component getComponentToAdd()
    {
        return this;
    }

    private AttributeSet getCurrentStyle()
    {
        if (styleOverride != null)
        {
            return styleOverride;
        }

        final HTMLDocument doc = (HTMLDocument)getDocument();
        int pos = getCaretPosition();
        if (pos != doc.getStartPosition().getOffset() + 1)
        {
            --pos;
        }
        return doc.getCharacterElement(pos).getAttributes();
    }

    public String getPlainText()
    {
        final HTMLDocument doc = (HTMLDocument)getDocument();
        try
        {
            return doc.getText(doc.getStartPosition().getOffset(), doc.getLength());
        }
        catch (final Exception e)
        {
            Log.log(Log.SYS, e);
            return "";
        }
    }

    /**
     * @return the useful part of the text of this component.
     */
    public String getText()
    {
        return UtilityFunctions.getBodyContent(super.getText());
    }

    /**
     * Initializes this object.
     */
    private void initialize()
    {
        setEditable(true);
        setFocusable(true);
        setRequestFocusEnabled(true);
        setBorder(new BevelBorder(BevelBorder.LOWERED));

        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed UP"), "historyBack");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed DOWN"), "historyForward");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed ENTER"), "enter");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed B"), "bold");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed I"), "italics");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed U"), "underline");
        addKeyListener(new KeyAdapter()
        {
            /*
             * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
             */
            public void keyPressed(final KeyEvent e)
            {
                spaceTyped = false;
            }

            /*
             * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
             */
            public void keyTyped(final KeyEvent e)
            {
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                {
                    return;
                }

                if (e.getKeyChar() == ' ')
                {
                    spaceTyped = true;
                    return;
                }

                if ((e.getKeyChar() == '\n') || (e.getKeyChar() == (char)8))
                {
                    return;
                }

                final String charStr = String.valueOf(e.getKeyChar());
                final HTMLDocument doc = (HTMLDocument)getDocument();
                final int dotPos = getCaret().getDot();
                if (styleOverride != null)
                {
                    try
                    {
                        ignoreCaret = true;
                        doc.insertAfterEnd(doc.getCharacterElement(dotPos), charStr);
                        doc.setCharacterAttributes(dotPos, charStr.length(), styleOverride, false);

                        // Hack to force the carat style to be what we just typed
                        setCaretPosition(dotPos);
                        setCaretPosition(dotPos + charStr.length());
                    }
                    catch (final Exception ex)
                    {
                        Log.log(Log.SYS, ex);
                    }
                    finally
                    {
                        ignoreCaret = false;
                    }

                    // Hack to get around weird first-character bug in edit pane
                    if (doc.getLength() > 2)
                    {
                        styleOverride = null;
                    }
                    e.consume();
                }
            }
        });

        addMouseListener(new MouseAdapter()
        {
            /*
             * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
             */
            public void mousePressed(final MouseEvent e)
            {
                spaceTyped = false;
            }
        });

        addCaretListener(new CaretListener()
        {
            /*
             * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
             */
            public void caretUpdate(final CaretEvent e)
            {
                // System.out.println("caretUpdate(" + e + ")");
                if (!ignoreCaret && !spaceTyped)
                {
                    styleOverride = null;
                    toolbar.updateStyles();
                }
                spaceTyped = false;
                // Send a typing packet. Caret position anywhere other than 1
                // means we're typing.
                // TODO: Find a better way. This sucks.
                if (lastTypedSent != (e.getDot() != 1))
                {
                    frame.send(PacketManager.makeTypingPacket(frame.getMyPlayer().getPlayerName(), e.getDot() != 1));
                    lastTypedSent = (e.getDot() != 1);
                }
            }
        });

        getActionMap().put("bold", new StyleAction("bold"));
        getActionMap().put("italics", new StyleAction("italics"));
        getActionMap().put("underline", new StyleAction("underline"));

        getActionMap().put("historyBack", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = -8619495333157141200L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                historyPosition--;
                if (historyPosition < 0)
                {
                    historyPosition = 0;
                }
                else
                {
                    setText((String)history.get(historyPosition));
                }
                toolbar.updateStyles();
            }
        });

        getActionMap().put("historyForward", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = -1252509345269856189L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                historyPosition++;

                if (historyPosition > history.size())
                {
                    historyPosition = history.size();
                }
                else
                {
                    if (historyPosition == history.size())
                    {
                        clear();
                    }
                    else
                    {
                        setText((String)history.get(historyPosition));
                    }
                }
                toolbar.updateStyles();
            }
        });

        getActionMap().put("enter", new AbstractAction()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 415264524729572508L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(final ActionEvent e)
            {
                // they hit return on the text bar
                final String entered = getText().trim();
                if (entered.length() == 0)
                {
                    // useless string.
                    // return focus to the map
                    frame.getGametableCanvas().requestFocus();
                    return;
                }

                history.add(entered);
                historyPosition = history.size();

                // parse for commands
                final String plain = getPlainText().trim();
                if ((plain.length() > 0) && (plain.charAt(0) == '/'))
                {
                    frame.parseSlashCommand(plain);
                }
                else
                {
                    frame.say(entered);
                }

                if (styleOverride == null)
                {
                    styleOverride = new SimpleAttributeSet(getCurrentStyle());
                }

                clear();
                toolbar.updateStyles();
            }
        });
    }

    public boolean isCurrentStyle(final String style)
    {
        final AttributeSet current = getCurrentStyle();
        final HTMLDocument doc = (HTMLDocument)getDocument();
        final AttributeSet styleOn = getCleanStyle(doc, "." + style);

        return current.containsAttributes(styleOn);
    }

    public void setCurrentStyle(final String style, final boolean status)
    {
        final HTMLDocument doc = (HTMLDocument)getDocument();
        final AttributeSet styleOn = getCleanStyle(doc, "." + style);
        final AttributeSet styleOff = getCleanStyle(doc, ".no-" + style);

        final AttributeSet current = getCurrentStyle();
        if (styleOverride == null)
        {
            styleOverride = new SimpleAttributeSet(current);
        }

        if (current.containsAttributes(styleOn))
        {
            styleOverride.removeAttributes(styleOn);
            styleOverride.addAttributes(styleOff);
        }
        else
        {
            styleOverride.addAttributes(styleOn);
            styleOverride.removeAttributes(styleOff);
        }
    }

    public void setText(final String text)
    {
        super.setText(ChatLogPane.DEFAULT_TEXT_HEADER + text + ChatLogPane.DEFAULT_TEXT_FOOTER);
    }

    public void setToolbar(final StyledEntryToolbar bar)
    {
        toolbar = bar;
    }

    public void toggleStyle(final String style)
    {
        final HTMLDocument doc = (HTMLDocument)getDocument();
        final Caret c = getCaret();
        final int start = Math.min(c.getMark(), c.getDot());
        final int end = Math.max(c.getMark(), c.getDot());

        if (start == end)
        {
            setCurrentStyle(style, !isCurrentStyle(style));

            getAttributeString(styleOverride);
            toolbar.updateStyles();
            return;
        }

        final AttributeSet styleOn = getCleanStyle(doc, "." + style);
        final AttributeSet styleOff = getCleanStyle(doc, ".no-" + style);
        applyStyle(doc, start, end, styleOn, styleOff);

        toolbar.updateStyles();
    }
}
