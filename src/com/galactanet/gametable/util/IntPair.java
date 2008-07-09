/*
 * IntPair.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.util;

/**
 * An immutable point class.
 */
public final class IntPair
{
    public final static IntPair ORIGIN = new IntPair(0, 0);

    private final int           x;

    private final int           y;

    /**
     * Constructor.
     */
    public IntPair(final int ix, final int iy)
    {
        x = ix;
        y = iy;
    }

    /**
     * Calculates the dot product of these two vectors.
     */
    public int dotProduct(final IntPair vector)
    {
        return x * vector.x + y * vector.y;
    }

    public boolean equals(final Object o)
    {
        final IntPair p = (IntPair)o;
        return p.x == x && p.y == y;
    }

    /**
     * Gets the first element of the pair.
     */
    public int getX()
    {
        return x;
    }

    /**
     * Gets the second element of the pair.
     */
    public int getY()
    {
        return y;
    }

    public int hashCode()
    {
        return x ^ y;
    }

    /**
     * Retuns an IntPair where each component has the maximum between this and the other IntPair.
     */
    public IntPair max(final IntPair other)
    {
        return new IntPair(Math.max(x, other.x), Math.max(y, other.y));
    }

    /**
     * Retuns an IntPair where each component has the minimum between this and the other IntPair.
     */
    public IntPair min(final IntPair other)
    {
        return new IntPair(Math.min(x, other.x), Math.min(y, other.y));
    }

    /**
     * Calculates the perpendicular product of these two vectors.
     */
    public int perpProduct(final IntPair vector)
    {
        return x * vector.y - y * vector.x;
    }

    /**
     * Gets the scaled version of this IntPair.
     */
    public IntPair scale(final double scalar)
    {
        return new IntPair((int)Math.round(x * scalar), (int)Math.round(y * scalar));
    }

    /**
     * Gets the scaled version of this IntPair.
     */
    public IntPair scale(final float scalar)
    {
        return new IntPair(Math.round(x * scalar), Math.round(y * scalar));
    }

    /**
     * Subtracts the given vector from this vector.
     */
    public IntPair subtract(final IntPair vector)
    {
        return translate(-vector.x, -vector.y);
    }

    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Returns a new point translated by the specified amount.
     */
    public IntPair translate(final int dx, final int dy)
    {
        return new IntPair(x + dx, y + dy);
    }

    /**
     * Returns a new point translated by the specified amount.
     */
    public IntPair translate(final IntPair vector)
    {
        return translate(vector.x, vector.y);
    }
}
