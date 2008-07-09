/*
 * SpanSource.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.chat;

import java.awt.Color;
import java.awt.Font;



/**
 * Class representing data about one or many text spans, including color, whether it is a link, has hover text, or
 * whatever.
 */
public class SpanSource
{
    private Color    textColor;
    private Color    backgroundColor;
    private Font     font;
    private Runnable clickAction;
    private String   hoverText;
    private boolean  hidden;
    private Boolean  bold;
    private Boolean  italicized;
    private Boolean  underlined;
    private float    fontSize;

    public SpanSource()
    {
    }

    public SpanSource(final SpanSource toCopy)
    {
        textColor = toCopy.getTextColor();
        backgroundColor = toCopy.getBackgroundColor();
        font = toCopy.font;
        clickAction = toCopy.getClickAction();
        hoverText = toCopy.getHoverText();
        hidden = toCopy.isHidden();
        bold = toCopy.bold;
        italicized = toCopy.italicized;
        underlined = toCopy.underlined;
        fontSize = toCopy.getFontSize();
    }

    public void clearBold()
    {
        bold = null;
    }

    public void clearItalicized()
    {
        italicized = null;
    }

    public void clearUnderlined()
    {
        underlined = null;
    }

    /**
     * @return Returns the backgroundColor.
     */
    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    /**
     * @return Returns the bold.
     */
    public Boolean getBold()
    {
        return bold;
    }

    /**
     * @return Returns the clickAction.
     */
    public Runnable getClickAction()
    {
        return clickAction;
    }

    public Font getFont(final Font defaultFont)
    {
        Font f = font;
        if (f == null)
        {
            f = defaultFont;
        }
        int style = f.getStyle();
        float size = f.getSize2D();

        if (fontSize > 0)
        {
            size = fontSize;
        }

        if (bold != null)
        {
            if (bold.booleanValue())
            {
                style |= Font.BOLD;
            }
            else
            {
                style &= ~Font.BOLD;
            }
        }

        if (italicized != null)
        {
            if (italicized.booleanValue())
            {
                style |= Font.ITALIC;
            }
            else
            {
                style &= ~Font.ITALIC;
            }
        }

        return f.deriveFont(style, size);
    }

    /**
     * @return Returns the fontSize.
     */
    public float getFontSize()
    {
        return fontSize;
    }

    /**
     * @return Returns the hoverText.
     */
    public String getHoverText()
    {
        return hoverText;
    }

    /**
     * @return Returns the italicized.
     */
    public Boolean getItalicized()
    {
        return italicized;
    }

    /**
     * @return Returns the color.
     */
    public Color getTextColor()
    {
        return textColor;
    }

    /**
     * @return Returns the underlined.
     */
    public Boolean getUnderlined()
    {
        return underlined;
    }

    /**
     * @return Returns the hidden.
     */
    public boolean isHidden()
    {
        return hidden;
    }

    /**
     * @param backgroundColor The backgroundColor to set.
     */
    public void setBackgroundColor(final Color c)
    {
        backgroundColor = c;
    }

    /**
     * @param b The bold to set.
     */
    public void setBold(final boolean b)
    {
        if (b)
        {
            bold = Boolean.TRUE;
        }
        else
        {
            bold = Boolean.FALSE;
        }
    }

    /**
     * @param action The clickAction to set.
     */
    public void setClickAction(final Runnable action)
    {
        clickAction = action;
    }

    /**
     * @param fn The font to set.
     */
    public void setFont(final Font fn)
    {
        font = fn;
    }

    /**
     * @param fontSize The fontSize to set.
     */
    public void setFontSize(final float size)
    {
        fontSize = size;
    }

    /**
     * @param hidden The hidden to set.
     */
    public void setHidden(final boolean h)
    {
        hidden = h;
    }

    /**
     * @param text The hoverText to set.
     */
    public void setHoverText(final String text)
    {
        hoverText = text;
    }

    /**
     * @param it The italicized to set.
     */
    public void setItalicized(final boolean it)
    {
        if (it)
        {
            italicized = Boolean.TRUE;
        }
        else
        {
            italicized = Boolean.FALSE;
        }
    }

    /**
     * @param c The color to set.
     */
    public void setTextColor(final Color c)
    {
        textColor = c;
    }

    /**
     * @param un The underlined to set.
     */
    public void setUnderlined(final boolean un)
    {
        if (un)
        {
            underlined = Boolean.TRUE;
        }
        else
        {
            underlined = Boolean.FALSE;
        }
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[SpanSource" + (bold != null ? " B:" + bold : "") + (italicized != null ? " I:" + italicized : "")
            + (underlined != null ? " U:" + underlined : "") + ']';
    }
}
