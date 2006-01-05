/*
 * PogLibrary.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.io.File;
import java.io.IOException;
import java.util.*;



/**
 * A recursively-defined representation of a set of pogs.
 * 
 * @author iffy
 */
public class PogLibrary
{
    public static final int LIBRARY_TYPE_POG      = 0;
    public static final int LIBRARY_TYPE_UNDERLAY = 1;

    /**
     * The short name of this library. Unique within the parent library.
     */
    private String          name                  = null;

    /**
     * The filesystem path to this set of pogs.
     */
    private File            location              = null;

    /**
     * Whether this is a pog or underlay library.
     */
    private int             libraryType           = LIBRARY_TYPE_POG;

    /**
     * A list of child libraries, sorted by name.
     */
    private List            childLibraries        = new ArrayList();

    /**
     * The list of pogs in this library.
     */
    private List            pogs                  = new ArrayList();

    /**
     * Set of acquired pog names.
     */
    private Set             acquiredPogs          = new HashSet();

    /**
     * The parent library.
     */
    private PogLibrary      parent                = null;

    /**
     * Constructor
     */
    public PogLibrary(PogLibrary mommy, String directory, int type) throws IOException
    {
        parent = mommy;
        libraryType = type;
        location = new File(directory).getCanonicalFile();
        if (!location.canRead() || !location.isDirectory())
        {
            throw new IOException("cannot read from " + directory);
        }
        name = getNameFromDirectory(location);

        acquirePogs();
    }

    public PogLibrary() throws IOException
    {
        location = new File(".").getCanonicalFile();
        name = getNameFromDirectory(location);
        PogLibrary child = new PogLibrary(this, "pogs", LIBRARY_TYPE_POG);
        childLibraries.add(child);
        child = new PogLibrary(this, "underlays", LIBRARY_TYPE_UNDERLAY);
        childLibraries.add(child);
    }

    /**
     * @return Returns the parent library.
     */
    public PogLibrary getParent()
    {
        return parent;
    }

    /**
     * @return Returns the root library.
     */
    public PogLibrary getRoot()
    {
        if (parent == null)
        {
            return this;
        }

        return parent.getRoot();
    }

    /**
     * @return Returns the child libraries of this library.
     */
    public List getChildLibraries()
    {
        return Collections.unmodifiableList(childLibraries);
    }

    /**
     * @return Returns the location of this library.
     */
    public File getLocation()
    {
        return location;
    }

    /**
     * @return Gets the canonical path of this library.
     */
    public String getPath()
    {
        try
        {
            return location.getCanonicalPath();
        }
        catch (IOException ioe)
        {
            return location.getAbsolutePath();
        }
    }

    /**
     * @return Returns the name of this library.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Returns the pogs in this library.
     */
    public List getPogs()
    {
        return Collections.unmodifiableList(pogs);
    }

    /**
     * @return Returns the pogs in this library.
     */
    public List getAllPogs()
    {
        int size = childLibraries.size();
        if (size < 1)
        {
            return getPogs();
        }

        List accum = new ArrayList(pogs);
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)childLibraries.get(i);
            accum.addAll(child.getAllPogs());
        }

        return Collections.unmodifiableList(accum);
    }

    /**
     * Gets the child library of the given name.
     * 
     * @param libraryName Name of library to get.
     * @return Library found or null.
     */
    public PogLibrary getChild(String libraryName)
    {
        PogLibrary child = getChildExact(libraryName);
        if (child != null)
        {
            return child;
        }

        String newName = location.getPath() + File.separator + libraryName;
        try
        {
            newName = new File(newName).getCanonicalFile().getPath();
        }
        catch (IOException ioe)
        {
            newName = new File(newName).getAbsolutePath();
        }

        return getChildExact(newName);
    }

    /**
     * Gets the pog with the given name.
     * 
     * @param pogName name of pog to fetch
     * @return Pog found or null.
     */
    public PogType getPog(String pogName)
    {
        int size = pogs.size();
        for (int i = 0; i < size; ++i)
        {
            PogType pogType = (PogType)pogs.get(i);
            if (pogName.equals(pogType.getFilename()))
            {
                return pogType;
            }
        }

        size = childLibraries.size();
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)childLibraries.get(i);
            PogType pog = child.getPog(pogName);
            if (pog != null)
            {
                return pog;
            }
        }

        return null;
    }

    public PogType createPlaceholder(String filename, int face)
    {
        File f = new File(filename);
        File p = f.getParentFile();
        if (!p.getAbsoluteFile().equals(getLocation()))
        {
            PogLibrary child = getChild(p.getPath());
            if (child != null)
            {
                return child.createPlaceholder(filename, face);
            }

            // TODO: handle library creation
            return null;
        }

        Log.log(Log.SYS, this + ".createPlaceholder(" + filename + ", " + face + ")");
        PogType retVal = null;
        try
        {
            retVal = new PogType(filename, face, (libraryType == LIBRARY_TYPE_UNDERLAY));
            pogs.add(retVal);
            acquiredPogs.add(filename);
        }
        catch (Exception ex)
        {
            // any exceptions thrown in this process cancel
            // the addition of that one pog.
            Log.log(Log.SYS, ex);
        }

        return retVal;
    }

    /**
     * Ensures that this panel has all the available pogs loaded.
     */
    public boolean acquirePogs()
    {
        if (!location.exists())
        {
            return false;
        }

        boolean retVal = false;
        // We don't want to scour the root directory for pogs
        if (getParent() != null)
        {
            String[] files = location.list();

            String rootPath = getRoot().getPath() + File.separator;
            String path = getPath() + File.separator;
            path = path.substring(rootPath.length());

            int len = files.length;
            for (int i = 0; i < len; ++i)
            {
                String filename = path + files[i];
                if (acquiredPogs.contains(filename))
                {
                    continue;
                }

                File test = new File(filename);

                if (test.isFile() && test.canRead())
                {
                    try
                    {
                        PogType pog = new PogType(filename, 1, (libraryType == LIBRARY_TYPE_UNDERLAY));
                        if (!pog.isUnknown())
                        {
                            pogs.add(pog);
                        }
                        acquiredPogs.add(filename);
                    }
                    catch (Exception ex)
                    {
                        // any exceptions thrown in this process cancel
                        // the addition of that one pog.
                        Log.log(Log.SYS, ex);
                    }
                    retVal = true;
                }
                else if (test.isDirectory() && test.canRead())
                {
                    try
                    {
                        PogLibrary child = new PogLibrary(this, test.getAbsolutePath(), libraryType);
                        childLibraries.add(child);
                        acquiredPogs.add(filename);
                    }
                    catch (Exception ex)
                    {
                        // any exceptions thrown in this process cancel
                        // the addition of that one directory.
                        Log.log(Log.SYS, ex);
                    }
                }
            }
        }

        int numPogs = pogs.size();
        for (int i = 0; i < numPogs; ++i)
        {
            PogType pog = (PogType)pogs.get(i);
            if (pog.isUnknown())
            {
                pog.load();
            }
        }

        int size = childLibraries.size();
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)childLibraries.get(i);
            if (child.acquirePogs())
            {
                retVal = true;
            }
        }

        return retVal;
    }

    // --- Private Methods ---

    /**
     * Gets a child by the exact name.
     * 
     * @param libraryName Exact name of child to find.
     * @return Child library with the given name, or null if not found.
     */
    private PogLibrary getChildExact(String libraryName)
    {
        File f = new File(libraryName).getAbsoluteFile();
        try
        {
            f = f.getCanonicalFile();
        }
        catch (IOException ioe)
        {
            // use the old one.
        }

        int size = childLibraries.size();
        for (int i = 0; i < size; ++i)
        {
            PogLibrary child = (PogLibrary)childLibraries.get(i);
            if (f.equals(child.getLocation()))
            {
                return child;
            }
        }

        return null;
    }

    private static String getNameFromDirectory(File file)
    {
        String temp = file.getAbsolutePath();
        int start = temp.lastIndexOf(File.separatorChar) + 1;
        if (start == temp.length())
        {
            start = temp.lastIndexOf(File.separatorChar, start - 1) + 1;
        }
        int end = temp.indexOf(File.separatorChar, start);
        if (end < 0)
        {
            end = temp.length();
        }
        return new String(temp.substring(start, end));
    }
}
