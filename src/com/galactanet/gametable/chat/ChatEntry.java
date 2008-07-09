/*
 * ChatEntry.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.chat;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.galactanet.gametable.util.FontStringSizeEvaluator;
import com.galactanet.gametable.util.IntPair;
import com.galactanet.gametable.util.StringSizeEvaluator;



/**
 * A single entry in the chat log.
 */
public class ChatEntry
{
    private class Sizer
    {
        private int        maxWidth   = 0;
        private int        lineWidth  = 0;
        private int        lineHeight = 0;
        private int        height     = 0;
        private int        current    = 0;
        private final int  textLength = mText.length();
        private final Font defaultFont;
//        private final int  wrapWidth;

        public Sizer(final Font font, final int width)
        {
            defaultFont = font;
//            wrapWidth = width;
        }

        public IntPair getSize()
        {
            final StringSizeEvaluator defaultSizeEvaluator = new FontStringSizeEvaluator(defaultFont);
            final Iterator spanIterator = iterator();
            while (spanIterator.hasNext())
            {
                final Span span = (Span)spanIterator.next();

                // calculate inter-span text
                calculateUpTo(span.getStartPosition(), defaultSizeEvaluator);

                // calculate span text
                calculateUpTo(span.getEndPosition(), new FontStringSizeEvaluator(span.getSource().getFont(defaultFont)));
            }

            // calculate final text
            calculateUpTo(textLength, defaultSizeEvaluator);

            // calculate last line
            height += lineHeight;
            if (lineWidth > maxWidth)
            {
                maxWidth = lineWidth;
            }

            return new IntPair(maxWidth, height);
        }

        private void calculateUpTo(final int end, final StringSizeEvaluator se)
        {
            while (current < end)
            {
                boolean newline = false;
                final int nextBreak = getIndexOfBreak(current);
                int next = end;
                if (nextBreak <= next)
                {
                    next = nextBreak;
                    newline = true;
                }

                final IntPair size = se.getStringSize(mText, current, next - current);
                // System.out.println("'" + mText.substring(current, next) + "' " + size);
                lineWidth += size.getX();
                if (lineHeight < size.getY())
                {
                    lineHeight = size.getY();
                }

                current = next;
                if (newline)
                {
                    height += lineHeight;
                    if (lineWidth > maxWidth)
                    {
                        maxWidth = lineWidth;
                    }

                    lineHeight = 0;
                    lineWidth = 0;
                }
            }
        }
    }

    public static int getNext(final int[] array, final int minValue, final int defaultValue)
    {
        for (int i = 0, hardCount = array.length; i < hardCount; ++i)
        {
            final int value = array[i];
            if (value > minValue)
            {
                return value;
            }
        }

        return defaultValue;
    }

    private final Date   mTimestamp;
    private final String mText;
    private final int    mIndex;
    private final List   mSpans      = new ArrayList();
    private final int[]  mHardBreaks;
    private int[]        mSoftBreaks = new int[0];
    private int          mTop;
    private int          mWidth;

    private int          mHeight;

    public ChatEntry(final int index, final String text)
    {
        mTimestamp = new Date();
        mIndex = index;
        final GtmlParser parser = new GtmlParser();
        parser.parse(text);
        mText = parser.getText();
        mSpans.addAll(parser.getSpans());
        mHardBreaks = parser.getBreaks();
    }

    public void addSpan(final SpanSource source, final int start, final int end)
    {
        final List spans = new ArrayList(mSpans);
        boolean added = false;
        final Span toAdd = new Span(source, start, end);
        for (final Iterator iter = spans.iterator(); iter.hasNext();)
        {
            final Span span = (Span)iter.next();
            if (start < span.getStartPosition())
            {
                // entirely too early
                if (end < span.getStartPosition())
                {
                    continue;
                }

                // overlaps start but not end
                if (end < span.getEndPosition())
                {
                    span.setStartPosition(end);
                    mSpans.add(toAdd);
                    added = true;
                    break;
                }

                // overlaps start and coincides at the end
                if (end == span.getEndPosition())
                {
                    mSpans.remove(span);
                    mSpans.add(toAdd);
                    added = true;
                    break;
                }

                // overlaps start and end
                if (end > span.getEndPosition())
                {
                    mSpans.remove(span);
                    // add later
                    continue;
                }

                System.err.println("addSpan - MISSED CASE 1");
            }
            else if (start == span.getStartPosition())
            {
                // we definitely coincide at the beginning

                // we stop short of the end of this span.
                if (end < span.getEndPosition())
                {
                    span.setStartPosition(end);
                    mSpans.add(toAdd);
                    added = true;
                    break;
                }

                // we entirely coincide with this span
                if (end == span.getEndPosition())
                {
                    mSpans.remove(span);
                    mSpans.add(toAdd);
                    added = true;
                    break;
                }

                // we completely consume this span.
                if (end > span.getEndPosition())
                {
                    mSpans.remove(span);
                    // add later
                    continue;
                }

                System.err.println("addSpan - MISSED CASE 2");
            }
            else if (start > span.getStartPosition())
            {
                // fun case: we are entirely inside this span, we have to split
                if (end < span.getEndPosition())
                {
                    mSpans.add(new Span(span.getSource(), end, span.getEndPosition()));
                    span.setEndPosition(start);
                    mSpans.add(toAdd);
                    added = true;
                    break;
                }

                // start in the middle of this span, and coincide with the end
                if (end == span.getEndPosition())
                {
                    span.setEndPosition(start);
                    mSpans.add(toAdd);
                    added = true;
                    break;
                }

                // we end after this span
                if (end > span.getEndPosition())
                {
                    // start in the middle of this span, and overlap the end
                    if (start < span.getEndPosition())
                    {
                        span.setEndPosition(start);
                        // add later
                        continue;
                    }

                    // start completely after this span
                    if (start > span.getEndPosition())
                    {
                        continue;
                    }
                }

                System.err.println("addSpan - MISSED CASE 3");
            }
            System.err.println("addSpan - MISSED CASE 4");
        }

        if (!added)
        {
            mSpans.add(toAdd);
        }

        // sort by start position
        Collections.sort(mSpans, new Comparator()
        {
            public int compare(final Object o1, final Object o2)
            {
                if (o1 == o2)
                {
                    return 0;
                }

                final Span s1 = (Span)o1;
                final Span s2 = (Span)o2;

                return s1.getStartPosition() - s2.getEndPosition();
            }
        });
    }

    public int getCharacterIndexForXPosition(final int xPosition, final StringSizeEvaluator defaultSizeEvaluator)
    {
        if (xPosition <= 0)
        {
            return 0;
        }

        final String text = getText();
        if (xPosition >= getWidth())
        {
            return text.length();
        }

        // TODO: this could be pricey, also could be a binary search
        // Could be constant time if we know we are a monospaced font!
        final int length = text.length();
        for (int i = 0; i < length; ++i)
        {
            final int width = defaultSizeEvaluator.getStringWidth(text, 0, i);
            if (width > xPosition)
            {
                return i - 1;
            }
        }

        return length;
    }

    /**
     * @return the total height of this entry.
     */
    public int getHeight()
    {
        return mHeight;
    }

    /**
     * @return Returns the index.
     */
    public int getIndex()
    {
        return mIndex;
    }

    public int getIndexOfBreak()
    {
        return getIndexOfBreak(0);
    }

    public int getIndexOfBreak(final int start)
    {
        final int length = mText.length();
        final int hard = getNext(mHardBreaks, start + 1, length);
        final int soft = getNext(mSoftBreaks, start + 1, length);

        return Math.min(hard, soft);
    }

    /**
     * @return The text of this entry.
     */
    public String getText()
    {
        return mText;
    }

    /**
     * @return Time this log was created.
     */
    public Date getTimestamp()
    {
        return mTimestamp;
    }

    /**
     * @return Returns the top pixel position of this entry.
     */
    public int getTop()
    {
        return mTop;
    }

    /**
     * @return the maximum pixel width of this entry.
     */
    public int getWidth()
    {
        return mWidth;
    }

    public int getXPositionForCharacterIndex(final int index)
    {
        // TODO: iterate spans and use span SSEs
        if (index == 0)
        {
            return 0;
        }

        final String text = getText();
        final int length = text.length();
        if (index >= length)
        {
            return getWidth();
        }

        return getWidth() * index / length;
    }

    public Iterator iterator()
    {
        return mSpans.iterator();
    }

    /**
     * calculates the size of this entry.
     * 
     * @param se
     */
    public void setSize(final Font defaultFont, final int wrapWidth)
    {
        if (defaultFont == null)
        {
            mWidth = getText().length();
            mHeight = 1;
            return;
        }

        if ((mText == null) || (mText.length() < 1))
        {
            final StringSizeEvaluator defaultSizeEvaluator = new FontStringSizeEvaluator(defaultFont);
            mHeight = defaultSizeEvaluator.getStringHeight(" ", 0, 1);
            mWidth = 1;
            return;
        }

        final Sizer sizer = new Sizer(defaultFont, wrapWidth);
        final IntPair size = sizer.getSize();

        mWidth = size.getX();
        mHeight = size.getY();
    }

    /**
     * Sets the top position of this entry.
     * 
     * @param top The top to set.
     */
    public void setTop(final int top)
    {
        mTop = top;
    }

    public String toString()
    {
        return "[LogEntry " + getIndex() + "]";
    }
}
