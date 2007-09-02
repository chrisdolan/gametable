/*
 * Preferences.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.prefs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



/**
 * Class that encapsulates all of the persistable preference state for GameTable.
 * 
 * @author iffy
 */
public class Preferences
{
    // --- Members ---

    /**
     * The map of custom preference descriptors.
     */
    private final Map preferenceDescriptors = new HashMap();

    /**
     * Map of the custom preference values.
     */
    private final Map preferenceValues      = new HashMap();

    // --- Constructors ---

    /**
     * Default Constructor.
     */
    public Preferences()
    {
    }

    // --- Public Functions ---

    public void addPreference(final PreferenceDescriptor pref)
    {
        preferenceDescriptors.put(pref.getName(), pref);
        preferenceValues.put(pref.getName(), pref.getDefaultValue());
    }

    public boolean getBooleanValue(final String name)
    {
        final Object o = preferenceValues.get(name);
        if (o == null)
        {
            return getDefaultBooleanValue(name);
        }

        return ("true".equalsIgnoreCase(o.toString()));
    }

    private boolean getDefaultBooleanValue(final String name)
    {
        final PreferenceDescriptor pref = getPreference(name);
        if ((pref == null) || (pref.getDefaultValue() == null))
        {
            return false;
        }

        return ("true".equalsIgnoreCase(pref.getDefaultValue().toString()));
    }

    private int getDefaultIntegerValue(final String name)
    {
        final PreferenceDescriptor pref = getPreference(name);
        if ((pref == null) || (pref.getDefaultValue() == null))
        {
            return Integer.MIN_VALUE;
        }

        try
        {
            return Integer.parseInt(pref.getDefaultValue().toString());
        }
        catch (final NumberFormatException nfe)
        {
            return Integer.MIN_VALUE;
        }
    }

    private String getDefaultStringValue(final String name)
    {
        final PreferenceDescriptor pref = getPreference(name);
        if ((pref == null) || (pref.getDefaultValue() == null))
        {
            return null;
        }

        return pref.getDefaultValue().toString();
    }

    private Object getDefaultValue(final String name)
    {
        final PreferenceDescriptor pref = getPreference(name);
        if (pref == null)
        {
            return null;
        }

        return pref.getDefaultValue();
    }

    public int getIntegerValue(final String name)
    {
        final Object o = preferenceValues.get(name);
        if (o == null)
        {
            return getDefaultIntegerValue(name);
        }

        try
        {
            return Integer.parseInt(o.toString());
        }
        catch (final NumberFormatException nfe)
        {
            return getDefaultIntegerValue(name);
        }
    }

    // --- Private Functions ---

    public PreferenceDescriptor getPreference(final String name)
    {
        return (PreferenceDescriptor)preferenceDescriptors.get(name);
    }

    public Set getPreferenceNames()
    {
        return Collections.unmodifiableSet(preferenceDescriptors.entrySet());
    }

    public String getStringValue(final String name)
    {
        final Object o = preferenceValues.get(name);
        if (o == null)
        {
            return getDefaultStringValue(name);
        }

        return o.toString();
    }

    public Object getValue(final String name)
    {
        final Object o = preferenceValues.get(name);
        if (o == null)
        {
            return getDefaultValue(name);
        }

        return o;
    }
}
