

package com.galactanet.gametable.util;

/**
 * An interface for determining the dimensions of a string. A common usage will be to determine the pixel dimensions of
 * a string for a given font.
 */
public interface StringSizeEvaluator
{
    /**
     * Returns the height of the given string.
     */
    int getStringHeight(String s, int start, int length);

    /**
     * Returns the total size of the string.
     * 
     * @param s String to measure.
     * @return Size of the string.
     */
    IntPair getStringSize(String s, int start, int length);

    /**
     * Returns the width of the given string. This is defined as the width of the single longest line using whatever
     * metrics it's supposed to use. Tabs stops are defined at every 8 space-widths.
     */
    int getStringWidth(String s, int start, int length);
}
