/*
 * Span.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.chat;

/**
 * An assignment of properties to a
 */
public class Span
{
    private final SpanSource source;
    private int              startPosition;
    private int              endPosition;

    /**
     * @param color Color of this span.
     * @param startPosition Start of this span (inclusive).
     * @param endPosition End of this span (exclusive).
     */
    public Span(final SpanSource spanSource, final int start, final int end)
    {
        source = spanSource;
        startPosition = start;
        endPosition = end;
    }

    /**
     * @return Returns the endPosition.
     */
    public int getEndPosition()
    {
        return endPosition;
    }

    /**
     * @return Returns the source.
     */
    public SpanSource getSource()
    {
        return source;
    }

    /**
     * @return Returns the startPosition.
     */
    public int getStartPosition()
    {
        return startPosition;
    }

    /**
     * @param endPosition The endPosition to set.
     */
    public void setEndPosition(final int end)
    {
        endPosition = end;
    }

    /**
     * @param startPosition The startPosition to set.
     */
    public void setStartPosition(final int start)
    {
        startPosition = start;
    }

    public String toString()
    {
        return "[Span " + startPosition + ", " + endPosition + "]";
    }
}
