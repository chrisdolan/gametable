/*
 * ToolManager.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.tools;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.*;
import java.util.List;

import com.galactanet.gametable.GametableFrame;
import com.galactanet.gametable.Log;
import com.galactanet.gametable.util.UtilityFunctions;



/**
 * A class to load and configure Tools.
 * 
 * @author iffy
 */
public class ToolManager
{
    /**
     * Class that encapsulates all the information for a specific tool.
     * 
     * @author iffy
     */
    public static class Info
    {
        private final List cursors = new ArrayList();
        private Image      icon;
        private int        id;
        private String     name;
        private String     quickKey;
        private Tool       tool;

        /**
         * Constructor.
         * 
         * @param i The index of this tool.
         * @param n The name of this tool.
         * @param className The name of the class that implements this tool.
         * @param iconName The name of the icon to represent this tool.
         * @throws IllegalArgumentException If unable to instantiate the implementation.
         */
        public Info(final int i, final String n, final String className, final String iconName, final String qkname)
            throws IllegalArgumentException
        {
            try
            {
                id = i;
                name = n;

                // Instantiate the tool given the name.
                tool = (Tool)Class.forName(className).newInstance();

                icon = UtilityFunctions.getImage(iconName);

            }
            catch (final Exception e)
            {
                throw new IllegalArgumentException("Unable to create Tool info object.", e);
            }

            String quickKeyName = qkname.toUpperCase();
            String keyName = quickKeyName;
            if (!quickKeyName.startsWith("VK_"))
            {
                keyName = "VK_" + quickKeyName;
            }
            else
            {
                quickKeyName = quickKeyName.substring(3);
            }

            try
            {
                KeyEvent.class.getField(keyName).getInt(null);
            }
            catch (final Exception e)
            {
                quickKey = null;
                Log.log(Log.SYS, "Unable to set quickKey to " + quickKeyName + " (" + keyName + ")");
                Log.log(Log.SYS, e);
            }
            quickKey = quickKeyName;
        }

        /* package */
        /**
         * Adds a cursor to the info object.
         * 
         * @param c Cursor to add.
         */
        void addCursor(final Cursor c)
        {
            cursors.add(c);
        }

        /**
         * @param index The index of the cursor to get.
         * @return The cursor at the specified index.
         */
        public Cursor getCursor(final int index)
        {
            return (Cursor)cursors.get(index);
        }

        /**
         * @return Returns the icon for this tool.
         */
        public Image getIcon()
        {
            return icon;
        }

        /**
         * @return Returns the id.
         */
        public int getId()
        {
            return id;
        }

        /**
         * @return Returns the name of this tool.
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return Number of cursors for this tool (should be at least 1).
         */
        public int getNumCursors()
        {
            return cursors.size();
        }

        /**
         * @return Returns the quickKey.
         */
        public String getQuickKey()
        {
            return quickKey;
        }

        /**
         * @return Returns the tool instance.
         */
        public Tool getTool()
        {
            return tool;
        }
    }

    private static final String CURSOR_FIELD_PREFIX      = "Cursor.";
    private static final String DEFAULT_TOOLS_PROPERTIES = "tools.properties";
    private static final String PROPERTY_CLASS           = ".class";
    private static final String PROPERTY_CURSORS         = ".cursors.";
    private static final String PROPERTY_ICON            = ".icon";
    private static final String PROPERTY_NAME            = ".name";
    private static final String PROPERTY_PREFIX          = "com.galactanet.gametable.tools.";

    private static final String PROPERTY_QUICK_KEY       = ".quickKey";

    /**
     * @param spec Specifier string for the cursor.
     * @return A cursor object derived from the specifier string.
     */
    private static Cursor getCursor(final String spec)
    {
        if (spec.startsWith(CURSOR_FIELD_PREFIX))
        {
            // It's a predefined cursor, so we'll use reflection to get at it.
            try
            {
                final String fieldName = spec.substring(CURSOR_FIELD_PREFIX.length());
                final Field field = Cursor.class.getField(fieldName);
                final int cursorId = ((Integer)field.get(null)).intValue();
                return Cursor.getPredefinedCursor(cursorId);
            }
            catch (final Exception e)
            {
                Log.log(Log.SYS, e);
            }
        }
        else
        {
            // Parse it as a "filename,hotspotX,hotspotY" string.
            try
            {
                // Get the filename.
                int start = 0;
                int end = spec.indexOf(',');
                if (end == -1)
                {
                    throw new ParseException("No comma found in cursor spec: \"" + spec + "\"", 0);
                }
                final String imageName = spec.substring(start, end);

                // Get the hotspot X coordinate.
                start = end + 1;
                if (start >= spec.length())
                {
                    throw new ParseException("Invalid cursor spec: \"" + spec + "\"", start - 1);
                }
                end = spec.indexOf(',', start);
                if (end == -1)
                {
                    throw new ParseException("Invalid cursor spec: \"" + spec + "\"", start);
                }
                final String hotSpotXStr = spec.substring(start, end);

                // Get the hotspot Y coordinate.
                start = end + 1;
                if (start >= spec.length())
                {
                    throw new ParseException("Invalid cursor spec: \"" + spec + "\"", start - 1);
                }
                final String hotSpotYStr = spec.substring(start);

                // Create the cursor based on the parsed data.
                final Image target = UtilityFunctions.createDrawableImage(32, 32);
                {
                    final Image image = UtilityFunctions.getImage(imageName);
                    final Graphics g = target.getGraphics();
                    g.drawImage(image, 0, 0, null);
                    g.dispose();
                }
                final int hotSpotX = Integer.parseInt(hotSpotXStr);
                final int hotSpotY = Integer.parseInt(hotSpotYStr);
                return Toolkit.getDefaultToolkit().createCustomCursor(target, new Point(hotSpotX, hotSpotY), imageName);
            }
            catch (final ParseException pe)
            {
                Log.log(Log.SYS, pe);
            }
            catch (final RuntimeException re)
            {
                Log.log(Log.SYS, re);
            }
        }

        // We'll return a "null" cursor for the error condition.
        final BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, 0);
        return Toolkit.getDefaultToolkit().createCustomCursor(bi, new Point(0, 0), "empty");
    }

    private final List infoList           = new ArrayList();

    // when this is true, no mouse events will be dispatched.
    // it is set to false when a mousePressed event is received.
    private boolean    m_bActionCancelled = false;

    private final Map  nameInfoMap        = new HashMap();

    /**
     * Constructor.
     */
    public ToolManager()
    {
    }

    public void cancelToolAction()
    {
        // whatever they were up to is halted immediately.
        // no further mouse stuff will be dispatched until the
        // next mouseButtonPressed is called
        getActiveTool().endAction();
        m_bActionCancelled = true;
    }

    // shortcut function to get the active tool
    public Tool getActiveTool()
    {
        return GametableFrame.getGametableFrame().getGametableCanvas().getActiveTool();
    }

    /**
     * @return The largest dimension of all the icons in the tool manager.
     */
    public int getMaxIconSize()
    {
        int size = 0;
        final int numTools = getNumTools();
        for (int toolId = 0; toolId < numTools; toolId++)
        {
            final Info info = getToolInfo(toolId);
            final Image image = info.getIcon();
            if (image.getWidth(null) > size)
            {
                size = image.getWidth(null);
            }

            if (image.getHeight(null) > size)
            {
                size = image.getHeight(null);
            }
        }

        return size;
    }

    /**
     * @return Number of tools available.
     */
    public int getNumTools()
    {
        return infoList.size();
    }

    /**
     * @param index Index of the tool to get.
     * @return Info for that tool.
     */
    public Info getToolInfo(final int index)
    {
        return (Info)infoList.get(index);
    }

    /**
     * @param name Name of the tool to get.
     * @return Info for that tool, or null if not found.
     */
    public Info getToolInfo(final String name)
    {
        return (Info)nameInfoMap.get(name);
    }

    /**
     * Initializes the tool manager with the default tool properties file.
     * 
     * @throws IOException If unable to access the file somehow.
     */
    public void initialize() throws IOException
    {
        initialize(DEFAULT_TOOLS_PROPERTIES);
    }

    /**
     * Initializes the tool manager from the given properties stream.
     * 
     * @param stream Stream of properties data.
     * @throws IOException If there is a problem accessing the stream.
     */
    public void initialize(final InputStream stream) throws IOException
    {
        // load up the properties
        final Properties props = new Properties();
        props.load(stream);
        initialize(props);
    }

    /**
     * Initializes the tool manager from the given properties object.
     * 
     * @param props Properties object.
     * @throws IOException If there is a problem accessing the stream.
     */
    public void initialize(final Properties props) throws IOException
    {
        // find all specified tools
        int toolId = 0;
        while (true)
        {
            final String prefix = PROPERTY_PREFIX + toolId;

            // if no valid name property for this ID, then we are done.
            final String name = props.getProperty(prefix + PROPERTY_NAME);
            if (name == null)
            {
                break;
            }

            try
            {
                // Initialize the tool info from the properties for this ID.
                final String className = props.getProperty(prefix + PROPERTY_CLASS);
                final String iconName = props.getProperty(prefix + PROPERTY_ICON);
                final String quickKeyName = props.getProperty(prefix + PROPERTY_QUICK_KEY);
                final Info info = new Info(toolId, name, className, iconName, quickKeyName);

                // Load cursors for this ID.
                int cursorId = 0;
                while (true)
                {
                    final String cursorPrefix = prefix + PROPERTY_CURSORS;
                    final String cursorValue = props.getProperty(cursorPrefix + cursorId);
                    if ((cursorValue == null) || (cursorValue.length() < 1))
                    {
                        break;
                    }

                    final Cursor cursor = getCursor(cursorValue);
                    info.addCursor(cursor);

                    cursorId++;
                }

                // Add to registry.
                infoList.add(info);
                nameInfoMap.put(info.getName(), info);
            }
            catch (final Exception e)
            {
                Log.log(Log.SYS, e);
            }

            toolId++;
        }
    }

    /**
     * Initializes the tool manager with the specified tool properties file.
     * 
     * @param fileName Name of the tools.properties file to load.
     * @throws IOException If unable to access the file somehow.
     */
    public void initialize(final String fileName) throws IOException
    {
        final Properties props = new Properties();
        final String fileName2 = "assets/" + fileName;
        final InputStream is = getClass().getResourceAsStream("/" + fileName2);
        if (is != null)
        {
            props.load(is);
        }

        File file = new File(fileName);
        if (!file.exists())
        {
            file = new File(fileName2);
            if (file.exists())
            {
                props.load(new FileInputStream(file));
            }
        }
        else
        {
            props.load(new FileInputStream(file));
        }
        initialize(props);
    }

    // mouse event functions.
    public void mouseButtonPressed(final int x, final int y, final int flags)
    {
        m_bActionCancelled = false;
        getActiveTool().mouseButtonPressed(x, y, flags);
    }

    public void mouseButtonReleased(final int x, final int y, final int flags)
    {
        if (m_bActionCancelled)
        {
            // this action has been cancelled.
            return;
        }
        getActiveTool().mouseButtonReleased(x, y, flags);
    }

    public void mouseMoved(final int x, final int y, final int flags)
    {
        // we call this even if the action has been cancelled.
        // Some tools set their cursor and do other things while no mouse button is down.
        getActiveTool().mouseMoved(x, y, flags);
    }
}
