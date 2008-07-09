/*
 * ChatModel.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.chat;

import java.awt.Font;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.galactanet.gametable.util.Strings;



public class ChatModel
{
    public interface Listener
    {
        /**
         * Notifies the listener that entries were added to the model.
         */
        void notifyEntriesAdded();
    }

    private class Recorder
    {
        private String         mName = "";
        private BufferedWriter mOut  = null;

        /**
         * Constructor.
         */
        public Recorder(final String name, final OutputStream out)
        {
            mName = name;
            mOut = new BufferedWriter(new OutputStreamWriter(out));
        }

        public void flush()
        {
            try
            {
                mOut.flush();
            }
            catch (final IOException ioe)
            {
                ioe.printStackTrace();
            }
        }

        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return mName;
        }

        public void recordLine(final String line)
        {
            try
            {
                mOut.write(line);
                mOut.write('\n');
            }
            catch (final IOException ioe)
            {
                ioe.printStackTrace();
            }
        }

    }

    private final List entries         = new ArrayList();
    private final Map  listeners       = new WeakHashMap();
    private Font       defaultFont     = null;
    private int        wrapWidth       = Integer.MAX_VALUE;
    private final int  firstEntryIndex = 0;
    private Recorder   mRecorder       = null;

    public ChatModel()
    {
    }

    /**
     * @param listener
     */
    public void addListener(final Listener listener)
    {
        listeners.put(listener, Boolean.TRUE);
    }

    public void clear()
    {
        entries.clear();
        fireEntriesAdded();
    }

    /**
     * @return Returns the defaultFont.
     */
    public Font getDefaultFont()
    {
        return defaultFont;
    }

    /**
     * @param index
     * @return
     */
    public ChatEntry getEntry(final int index)
    {
        if (index >= entries.size())
        {
            return null;
        }

        return (ChatEntry)entries.get(index);
    }

    /**
     * @return
     */
    public int getEntryCount()
    {
        return entries.size();
    }

    /**
     * @return
     */
    public int getHeight()
    {
        int height = 0;

        for (int i = 0, len = entries.size(); i < len; ++i)
        {
            final ChatEntry entry = (ChatEntry)entries.get(i);
            height += entry.getHeight();
        }

        return height;
    }

    /**
     * Gets the log entry index for the given y position.
     * 
     * @param y Y position to get the entry for.
     * @return entry index, or -1 if out of bounds.
     */
    public int getIndexAtYPosition(final int y)
    {
        // System.out.println("getIndexAtYPosition(" + y + ")");
        if (y < 0)
        {
            System.out.println("getIndexAtYPosition(): less than zero");
            return 0;
        }

        if (y >= getHeight())
        {
            // System.out.println("getIndexAtYPosition(): greater than log
            // height ("
            // + getHeight() + ")");
            return entries.size();
        }

        int min = 0;
        int max = entries.size() - 1;
        int index = min + (max - min) / 2;
        int count = 0;
        while (min < max)
        {
            final ChatEntry entry = (ChatEntry)entries.get(index);
            count++;
            if (count > 20)
            {
                System.out.println("!!!!!!!!!!!!!!!!!!");
                System.out.println("!!!!!!!!!!!!!!!!!!");
                System.out.println("!!!!!!!!!!!!!!!!!!");
                System.out.println("!!!!!!!!!!!!!!!!!!");
                System.out.println("!!!!!!!!!!!!!!!!!!");
                System.out.println("!!!!!!!!!!!!!!!!!!");
                return -1;
            }

            // System.out.println("[" + min + ", " + index + ", " + max
            // + "] - top=" + entry.getTop() + ", height="
            // + entry.getHeight() + ", diff=" + (y - entry.getTop()));
            final int h = entry.getHeight();
            final int diff = y - entry.getTop();
            if ((diff >= 0) && (diff < h))
            {
                return index;
            }

            if (diff < 0)
            {
                // check lower half, reduce max
                max = index;
                if (max - min == 1)
                {
                    index = min;
                    continue;
                }
            }
            else if (diff >= h)
            {
                // check upper half, reduce min
                min = index;
                if (max - min == 1)
                {
                    index = max;
                    continue;
                }
            }

            index = min + (max - min) / 2;
        }

        return index + firstEntryIndex;
    }

    /**
     * Gets an iterator for over the entries that are visible in the given view.
     * 
     * @param yOffset top Y for view
     * @param height height of view
     * @return Iterator over entries.
     */
    public Iterator getIteratorForView(final int yOffset, final int height)
    {
        int yPosition = Integer.MIN_VALUE;
        final int startLine = getIndexAtYPosition(yOffset);
        int endLine = -1;
        for (int i = startLine, len = entries.size(); i < len; ++i)
        {
            final ChatEntry entry = (ChatEntry)entries.get(i);
            if (yPosition == Integer.MIN_VALUE) {
                yPosition = entry.getTop() + entry.getHeight();
            } else { 
                yPosition += entry.getHeight();
            }

            if (yPosition >= yOffset + height)
            {
                endLine = Math.min(i + 2, entries.size());
                break;
            }

        }

        if (startLine < 0)
        {
            return entries.subList(entries.size(), entries.size()).iterator();
        }

        if (endLine < 0)
        {
            return entries.subList(startLine, entries.size()).iterator();
        }

        return entries.subList(startLine, endLine).iterator();
    }

    public String getRecordingName()
    {
        if (mRecorder == null)
        {
            return null;
        }

        return mRecorder.getName();
    }

    public String getSelectionText(final int startLine, final int startCharacter, final int endLine,
        final int endCharacter)
    {
        final StringBuffer buffer = new StringBuffer();
        for (int i = startLine; i <= endLine; ++i)
        {
            final ChatEntry entry = getEntry(i);
            if (entry == null)
            {
                break;
            }

            if (startLine == endLine)
            {
                buffer.append(entry.getText().substring(startCharacter, endCharacter));
                break;
            }

            if (i == startLine)
            {
                buffer.append(entry.getText().substring(startCharacter));
                buffer.append('\n');
            }
            else if (i == endLine)
            {
                buffer.append(entry.getText().substring(0, endCharacter));
            }
            else
            {
                buffer.append(entry.getText());
                buffer.append('\n');
            }
        }
        return buffer.toString();
    }

    /**
     * @return
     */
    public int getWidth()
    {
        int width = 0;
        for (int i = 0, len = entries.size(); i < len; ++i)
        {
            final ChatEntry entry = (ChatEntry)entries.get(i);
            final int w = entry.getWidth();
            if (w > width)
            {
                width = w;
            }
        }

        return width;
    }

    /**
     * @param line
     */
    public void receiveLine(final String line)
    {
        try
        {
            final ChatEntry entry = new ChatEntry(entries.size() + firstEntryIndex, line);
            entry.setSize(defaultFont, wrapWidth);
            int position = 0;
            if (entries.size() > 0)
            {
                final ChatEntry last = (ChatEntry)entries.get(entries.size() - 1);
                position = last.getTop() + last.getHeight();
            }

            entry.setTop(position);
            position += entry.getHeight();

            entries.add(entry);

            if (mRecorder != null)
            {
                mRecorder.recordLine(entry.getText());
            }
            fireEntriesAdded();
        }
        catch (final RuntimeException re)
        {
            System.out.println("exception on line: \"" + Strings.escape(line) + "\"");
            re.printStackTrace();
        }
    }

    /**
     * @param listener
     */
    public void removeListener(final Listener listener)
    {
        listeners.remove(listener);
    }

    public void save(final OutputStream stream)
    {
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(stream));
            final Iterator iterator = entries.iterator();
            while (iterator.hasNext())
            {
                final ChatEntry entry = (ChatEntry)iterator.next();
                out.write(entry.getText());
            }
            out.flush();
        }
        catch (final IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (final IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }

        }
    }

    public void setDefaultFont(final Font font)
    {
        defaultFont = font;
        recalculateSize();
    }

    public void setWrapWidth(final int width)
    {
        if (width == wrapWidth)
        {
            return;
        }

        wrapWidth = width;
        recalculateSize();
    }

    public void startRecordingTo(final String filename)
    {
        try
        {
            mRecorder = new Recorder(filename, new FileOutputStream(filename));
        }
        catch (final FileNotFoundException e)
        {
            e.printStackTrace();
            mRecorder = null;
        }
    }

    public void stopRecording()
    {
        mRecorder = null;
    }

    private void fireEntriesAdded()
    {
        final Iterator iterator = listeners.keySet().iterator();
        while (iterator.hasNext())
        {
            final Listener listener = (Listener)iterator.next();
            listener.notifyEntriesAdded();
        }
    }

    private void recalculateSize()
    {
        int position = 0;
        for (int i = 0, len = entries.size(); i < len; ++i)
        {
            final ChatEntry entry = (ChatEntry)entries.get(i);
            entry.setSize(defaultFont, wrapWidth);
            entry.setTop(position);
            position += entry.getHeight();
        }
    }
}
