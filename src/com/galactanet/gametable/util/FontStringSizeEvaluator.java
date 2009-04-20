/*
 * FontStringSizeEvaluator.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.util;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;



/**
 * A StringSizeEvaluator for an AWT font.
 */
public class FontStringSizeEvaluator implements StringSizeEvaluator
{
    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, false);

    private final Font                     font;

    /**
     * 
     */
    public FontStringSizeEvaluator(final Font f)
    {
        font = f;
    }

    /*
     * @see com.tivo.tools.tvnav.logviewer.LogEntry.SizeEvaluator#getHeight(com.tivo.tools.tvnav.logviewer.LogEntry)
     */
    public int getStringHeight(final String string, int start, int length)
    {
        final IntPair size = getStringSize(string, start, length);
        return size.getY();
    }

    /*
     * @see com.tivo.tools.tvnav.logviewer.util.StringSizeEvaluator#getStringSize(java.lang.String)
     */
    public IntPair getStringSize(final String string, int start, int length)
    {
        final Rectangle rect = font.getStringBounds(string, start, start + length, FONT_RENDER_CONTEXT).getBounds();
        return new IntPair(rect.width, rect.height);
    }

    /*
     * @see com.tivo.tools.tvnav.logviewer.LogEntry.SizeEvaluator#getWidth(com.tivo.tools.tvnav.logviewer.LogEntry)
     */
    public int getStringWidth(final String s, int start, int length)
    {
        final IntPair size = getStringSize(s, start, length);
        return size.getX();
    }
}
