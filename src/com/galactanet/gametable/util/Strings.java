/*
 * Strings.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.util;




/**
 * A nasty util class for high-level <tt>String</tt> operations.
 */
public abstract class Strings
{
    /**
     * Default implementation of StringSizeEvaluator.
     */
    private static final class DefaultStringSizeEvaluator implements StringSizeEvaluator
    {
        /**
         * Width of a tab, in spaces.
         */
        private static final int TAB_WIDTH = 8;

        /*
         * @see com.tivo.tools.tvnav.logviewer.util.StringSizeEvaluator#getStringHeight(java.lang.String)
         */
        public int getStringHeight(final String s, int start, int length)
        {
            return Strings.getLineCount(s.substring(start, start + length));
        }

        /*
         * @see com.tivo.tools.tvnav.logviewer.util.StringSizeEvaluator#getStringSize(java.lang.String)
         */
        public IntPair getStringSize(final String s, int start, int length)
        {
            return new IntPair(getStringWidth(s, start, length), getStringHeight(s, start, length));
        }

        /*
         * @see com.tivo.tools.tvnav.logviewer.util.StringSizeEvaluator#getStringWidth(java.lang.String)
         */
        public int getStringWidth(final String text, int start, int len)
        {
            int maxTally = 0;
            int tally = 0;
            for (int i = start; i < len; ++i)
            {
                final char c = text.charAt(i);
                switch (c)
                {
                    case '\t':
                        tally += TAB_WIDTH - tally % TAB_WIDTH;
                    break;
                    case '\n':
                    case '\r':
                        if (tally > maxTally)
                        {
                            maxTally = tally;
                        }
                        tally = 0;
                    break;
                    default:
                        tally++;
                    break;
                }
            }

            return tally > maxTally ? tally : maxTally;
        }
    }

    static final StringSizeEvaluator DEFAULT_STRING_SIZE_EVALUATOR = new DefaultStringSizeEvaluator();

    /**
     * Given a filename and a new extension, returns a new String that is the filename with the extension replaced.
     * 
     * @param fileName The filename to change the extension of.
     * @param ext The new extension.
     * @return fileName with the extensions changed to ext.
     */
    public static String changeExtension(final String fileName, final String ext)
    {
        final int index = fileName.lastIndexOf('.');
        return fileName.substring(0, index) + '.' + ext;
    }

    /**
     * Changes the filename portion of a path.
     * 
     * @param path The path to replace the filename portion.
     * @param fileName The filename to be set.
     * @return A new path string with the given filename.
     */
    public static String changeFileName(final String path, final String fileName)
    {
        final StringBuffer out = new StringBuffer();
        final int start = path.lastIndexOf('/');
        if (start >= 0)
        {
            out.append(path.substring(0, start + 1));
        }
        out.append(fileName);
        return out.toString();
    }

    /**
     * Wraps a string to the specified number of columns, assuming that one character fits in one column. Does not
     * attempt to break on words.
     */
    public static final String charWrap(final String in, final int cols)
    {
        return charWrap(in, cols, DEFAULT_STRING_SIZE_EVALUATOR);
    }

    /**
     * Wraps a string to the specified maximum width per line, by using the specified StringSizeEvaluator to determine
     * the width of a given string. Does not attempt to break on words.
     */
    public static final String charWrap(final String in, final int maxSize, final StringSizeEvaluator we)
    {
        final int inLen = in.length();
        final StringBuffer out = new StringBuffer(inLen * 2);
        int lineSize = 0;
        for (int index = 0; index < inLen; ++index)
        {
            final String c = in.substring(index, index + 1);
            final char ch = c.charAt(0);
            if (ch == '\n' || ch == '\r')
            {
                out.append(c);
                lineSize = 0;
                continue;
            }

            if (lineSize + we.getStringWidth(c, 0, c.length()) > maxSize)
            {
                out.append('\n');
                lineSize = 0;
            }

            out.append(c);
            lineSize += we.getStringWidth(c, 0, c.length());
        }

        return out.toString();
    }

    /**
     * Escapes the given string, java-style. Not fully implemented.
     * 
     * @param in String to escape.
     * @return Java-escaped version of <code>in</code>.
     */
    public static final String escape(final String in)
    {
        if (in == null)
        {
            return "<null>";
        }

        final int inLen = in.length();
        final StringBuffer out = new StringBuffer(inLen * 2);
        for (int i = 0; i < inLen; ++i)
        {
            final char c = in.charAt(i);
            switch (c)
            {
                case '\n':
                    out.append("\\n");
                break;
                case '\r':
                    out.append("\\r");
                break;
                case '\t':
                    out.append("\\t");
                break;
                case '\f':
                    out.append("\\f");
                break;
                case '\b':
                    out.append("\\b");
                break;
                case '\\':
                case '\'':
                case '\"':
                    out.append('\\');
                default:
                    out.append(c);
                break;
            }
        }
        return out.toString();
    }

    /**
     * Gets the sub-line at the given line number.
     */
    public static final String getLine(final String in, final int lineNumber)
    {
        return getLines(in, lineNumber, lineNumber + 1);
    }

    public static final int getLineCount(final String in)
    {
        return getLineCount(in, 0, in.length());
    }

    /**
     * Returns the number of lines in the given string.
     * 
     * @param in String to count the number of lines in.
     * @return Number of lines in the given string.
     */
    public static final int getLineCount(final String in, int start, int length)
    {
        if (length == 0)
        {
            return 0;
        }

        int tally = 0;
        for (int i = start; i < length; i++)
        {
            if (in.charAt(i) == '\n')
            {
                tally++;
            }
        }

        if (in.charAt(length - 1) != '\n')
        {
            tally++;
        }

        return tally;
    }

    /**
     * Gets the line number at the given position in the given string.
     */
    public static final int getLineNumberAt(final String in, final int position)
    {
        if (position == 0)
        {
            return 0;
        }

        final int inLen = in.length();
        if (position > inLen)
        {
            throw new IndexOutOfBoundsException("position " + position + " not in [0, " + inLen + "]");
        }

        int tally = 0;
        for (int i = 0; i < position; i++)
        {
            if (in.charAt(i) == '\n')
            {
                tally++;
            }
        }

        return tally;
    }

    /**
     * Gets the sub-lines in the given string between start, inclusive, and end, exclusive.
     */
    public static final String getLines(final String in, final int start, final int end)
    {
        final int inLen = in.length();

        int current = 0;
        int index = 0;
        for (; index < inLen && current < start; index++)
        {
            if (in.charAt(index) == '\n')
            {
                current++;
            }
        }

        if (index == inLen)
        {
            throw new IndexOutOfBoundsException("There is no line " + start);
        }

        final int startPos = index;
        for (; index < inLen; index++)
        {
            if (in.charAt(index) == '\n')
            {
                current++;
            }

            if (current == end)
            {
                break;
            }
        }

        final int endPos = index;

        return in.substring(startPos, endPos);
    }

    public static final int getStringWidth(final String in)
    {
        return getStringWidth(in, 0, in.length());
    }

    /**
     * Gets the string width, given a 1 width for all characters.
     * 
     * @param in String to get the width of.
     * @return Width of given string.
     */
    public static final int getStringWidth(final String in, int start, int length)
    {
        return DEFAULT_STRING_SIZE_EVALUATOR.getStringWidth(in, start, length);
    }

    /**
     * Returns the given string, padded out to the specified size by adding spaces to the left.
     * 
     * @param in String to left-pad.
     * @return Left-padded version of in.
     */
    public static final String leftPad(final String in, final int size)
    {
        final int inLen = in.length();
        if (inLen >= size)
        {
            return in;
        }

        final StringBuffer out = new StringBuffer(size);
        final int max = size - inLen;
        for (int i = 0; i < max; ++i)
        {
            out.append(' ');
        }

        out.append(in);

        return out.toString();
    }

    /**
     * Returns the given string, padded out to the specified size by adding spaces to the right.
     * 
     * @param in String to right-pad.
     * @return Right-padded version of in.
     */
    public static final String rightPad(final String in, final int size)
    {
        final int inLen = in.length();
        if (inLen >= size)
        {
            return in;
        }

        final StringBuffer out = new StringBuffer(size);
        final int max = size - inLen;
        out.append(in);
        for (int i = 0; i < max; ++i)
        {
            out.append(' ');
        }

        return out.toString();
    }

    /**
     * Given a path, strips the extension off of it, returning the new String.
     * 
     * @param path The path to strip the extension from.
     * @return A new string that has the extension stripped from it.
     */
    public static String stripExtension(final String path)
    {
        final int start = path.lastIndexOf('/') + 1;
        final int end = path.lastIndexOf('.');
        return path.substring(start, end);
    }

    /**
     * Returns the largest string <code>S</code> such that <code>S + R
     * = in</code> where the
     * <code>width(S) <= maxSize</code>. For this to really work, <code>in</code> can't have any newlines.
     */
    public static final String truncate(final String in, final int maxSize, final StringSizeEvaluator eval)
    {
        // could this easily be some kinda binary search? Maybe just
        // for long strings?
        final int len = in.length();
        for (int i = len; i > 0; i--)
        {
            final int w = eval.getStringWidth(in, 0, i);
            if (w <= maxSize)
            {
                return in.substring(0, i);
            }
        }

        return "";
    }

    /**
     * Prevents instantiation by others.
     */
    private Strings()
    {
    }
}
