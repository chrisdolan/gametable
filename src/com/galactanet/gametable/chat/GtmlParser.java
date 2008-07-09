/*
 * GtmlParser.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.chat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.galactanet.gametable.UtilityFunctions;



/**
 * TODO: comment
 */
public class GtmlParser
{
    private static class Tag
    {
        private final String     name;
        private final Map        attributes;
        private final SpanSource source;

        public Tag(final String tagName, final Map tagAttributes, final SpanSource spanSource)
        {
            name = tagName;
            attributes = tagAttributes;
            source = spanSource;
        }

        /**
         * @return Returns the attributes.
         */
        public Map getAttributes()
        {
            return attributes;
        }

        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return Returns the source.
         */
        public SpanSource getSource()
        {
            return source;
        }
    }

    private static final int STATE_TEXT                                   = 0;
    private static final int STATE_ENTITY                                 = 1;
    private static final int STATE_TAG_OPEN_NAME                          = 2;
    private static final int STATE_TAG_CLOSE_NAME                         = 3;
    private static final int STATE_TAG_OPEN_ATTRIBUTE_NAME                = 4;
    private static final int STATE_TAG_OPEN_ATTRIBUTE_VALUE               = 5;
    private static final int STATE_TAG_OPEN_ATTRIBUTE_VALUE_ENTITY        = 6;
    private static final int STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE        = 7;
    private static final int STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE_ENTITY = 8;
    private static final int STATE_TAG_OPEN_CLOSING                       = 9;

    private static final Map ENTITY_MAP                                   = getEntityMap();

    private static void clear(final StringBuffer stringBuffer)
    {
        stringBuffer.delete(0, stringBuffer.length());
    }

    private static Map getEntityMap()
    {
        final HashMap map = new HashMap();
        map.put("amp", "&");
        map.put("quot", "\"");
        map.put("apos", "'");
        map.put("lt", "<");
        map.put("gt", ">");
        map.put("nbsp", String.valueOf((char)160));
        return Collections.unmodifiableMap(map);
    }

    private static Color parseColor(final String spec)
    {
        if (spec == null || spec.length() == 0)
        {
            return null;
        }

        if (spec.charAt(0) != '#')
        {
            return null;
        }

        final int len = spec.length();

        // Characters per component
        int cLen;

        switch (len)
        {
            case 4:
            case 5:
                cLen = 1;
            break;
            case 7:
            case 9:
                cLen = 2;
            break;
            default:
                return null;
        }

        final int[] tempColor = new int[] {
            0, 0, 0, 255
        };

        // number of specified components: 3 or 4
        final int cNum = (len - 1) / cLen;
        for (int i = 0; i < cNum; ++i)
        {
            String component = "";
            final int index = (i * cLen) + 1;
            for (int c = 0; c < cLen; ++c)
            {
                component += spec.charAt(index + c);
            }

            if (cLen == 1)
            {
                component += component;
            }

            try
            {
                tempColor[i] = Integer.parseInt(component, 16);
            }
            catch (final NumberFormatException nfe)
            {
                return null;
            }
        }

        return new Color(tempColor[0], tempColor[1], tempColor[2], tempColor[3]);
    }

    private static String parseEntity(String entityName)
    {
        entityName = entityName.trim().toLowerCase();
        if (entityName.length() == 0)
        {
            return "";
        }

        if (entityName.charAt(0) == '#')
        {
            try
            {
                final int i = Integer.parseInt(entityName.substring(1));
                return String.valueOf((char)i);
            }
            catch (final NumberFormatException e)
            {
                return "";
            }
        }

        final String value = (String)ENTITY_MAP.get(entityName);
        if (value == null)
        {
            return "";
        }

        return value;
    }

    private static float parseFloat(final String string, final float defaultValue)
    {
        if (string == null)
        {
            return defaultValue;
        }

        try
        {
            return Float.parseFloat(string);
        }
        catch (final NumberFormatException e)
        {
            return defaultValue;
        }
    }

    private final StringBuffer textBuffer   = new StringBuffer();
    private final StringBuffer entityBuffer = new StringBuffer();
    private final StringBuffer nameBuffer   = new StringBuffer();
    private final Map          attributes   = new HashMap();
    private final List         tagStack     = new ArrayList();
    private final List         spans        = new ArrayList();
    private final List         breaks       = new ArrayList();
    private String             currentTag;
    private String             currentAttribute;
    private int                state;
    private int                spanStart;

    public GtmlParser()
    {
    }

    public List getSpans()
    {
        return spans;
    }

    public int[] getBreaks()
    {
        int[] retVal = new int[breaks.size()];
        for (int i = 0, size = breaks.size(); i < size; ++i)
        {
            Integer position = (Integer)breaks.get(i);
            retVal[i] = position.intValue();
        }

        return retVal;
    }

    public String getText()
    {
        return textBuffer.toString();
    }

    public static final String getStateString(int state)
    {
        switch (state)
        {
            case STATE_ENTITY:
                return "ENTITY";
            case STATE_TAG_CLOSE_NAME:
                return "TAG_CLOSE_NAME";
            case STATE_TAG_OPEN_ATTRIBUTE_NAME:
                return "TAG_OPEN_ATTRIBUTE_NAME";
            case STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE:
                return "TAG_OPEN_ATTRIBUTE_QUOTED_VALUE";
            case STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE_ENTITY:
                return "TAG_OPEN_ATTRIBUTE_QUOTED_VALUE_ENTITY";
            case STATE_TAG_OPEN_ATTRIBUTE_VALUE:
                return "TAG_OPEN_ATTRIBUTE_VALUE";
            case STATE_TAG_OPEN_ATTRIBUTE_VALUE_ENTITY:
                return "TAG_OPEN_ATTRIBUTE_VALUE_ENTITY";
            case STATE_TAG_OPEN_CLOSING:
                return "TAG_OPEN_ATTRIBUTE_OPEN_CLOSING";
            case STATE_TAG_OPEN_NAME:
                return "TAG_OPEN_NAME";
            case STATE_TEXT:
                return "TEXT";
            default:
                return "UNKNOWN(" + state + ")";
        }
    }

    public void parse(final String text)
    {
        reset();
        boolean wasWhitespace = true;
        char quoteCharacter = '"';
        for (int i = 0, len = text.length(); i < len; ++i)
        {
            final char c = text.charAt(i);
            // System.out.println("GtmlParser: " + getStateString(state) + " '" + c + "' - text='" +
            // textBuffer.toString()
            // + "' name='" + nameBuffer.toString() + "' entity='" + entityBuffer + "'");

            switch (state)
            {
                case STATE_TEXT:
                {
                    if (c == '&')
                    {
                        state = STATE_ENTITY;
                        break;
                    }

                    if (c == '<')
                    {
                        state = STATE_TAG_OPEN_NAME;
                        break;
                    }

                    if (Character.isWhitespace(c))
                    {
                        if (!wasWhitespace)
                        {
                            wasWhitespace = true;
                            textBuffer.append(' ');
                        }
                    }
                    else
                    {
                        wasWhitespace = false;
                        textBuffer.append(c);
                    }
                }
                break;

                case STATE_ENTITY:
                {
                    if (c == ';')
                    {
                        textBuffer.append(getEntity());
                        state = STATE_TEXT;
                        break;
                    }

                    entityBuffer.append(c);
                }
                break;

                case STATE_TAG_OPEN_NAME:
                {
                    if (c == ' ')
                    {
                        if (nameBuffer.length() == 0)
                        {
                            break;
                        }

                        currentTag = getName().trim().toLowerCase();
                        state = STATE_TAG_OPEN_ATTRIBUTE_NAME;
                        break;
                    }

                    if (c == '/')
                    {
                        if (nameBuffer.length() == 0)
                        {
                            state = STATE_TAG_CLOSE_NAME;
                            break;
                        }

                        state = STATE_TAG_OPEN_CLOSING;
                        break;
                    }

                    if (c == '>')
                    {
                        if (nameBuffer.length() == 0)
                        {
                            // ignore unnamed tag
                            state = STATE_TEXT;
                            break;
                        }

                        currentTag = getName().trim().toLowerCase();
                        state = STATE_TEXT;
                        openTag(currentTag, attributes);
                        attributes.clear();
                        break;
                    }

                    nameBuffer.append(c);
                }
                break;

                case STATE_TAG_CLOSE_NAME:
                {
                    if (c == '>')
                    {
                        state = STATE_TEXT;
                        closeTag(getName().trim().toLowerCase());
                        break;
                    }

                    nameBuffer.append(c);
                }
                break;

                case STATE_TAG_OPEN_ATTRIBUTE_NAME:
                {
                    if (c == '=')
                    {
                        currentAttribute = getName().trim().toLowerCase();
                        state = STATE_TAG_OPEN_ATTRIBUTE_VALUE;
                        break;
                    }

                    if (c == '>')
                    {
                        state = STATE_TEXT;
                        openTag(currentTag, attributes);
                        attributes.clear();
                        break;
                    }

                    nameBuffer.append(c);
                }
                break;

                case STATE_TAG_OPEN_ATTRIBUTE_VALUE:
                {
                    if (c == '"' || c == '\'')
                    {
                        quoteCharacter = c;
                        if (nameBuffer.length() == 0)
                        {
                            state = STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE;
                            break;
                        }
                    }
                    else if ((c == ' ') || (c == '\n'))
                    {
                        if (nameBuffer.length() == 0)
                        {
                            break;
                        }

                        attributes.put(currentAttribute, getName());
                        state = STATE_TAG_OPEN_ATTRIBUTE_NAME;
                        break;
                    }

                    if (c == '&')
                    {
                        state = STATE_TAG_OPEN_ATTRIBUTE_VALUE_ENTITY;
                        break;
                    }

                    if (c == '/')
                    {
                        if (nameBuffer.length() > 0)
                        {
                            attributes.put(currentAttribute, getName());
                        }

                        state = STATE_TAG_OPEN_CLOSING;
                        break;
                    }

                    nameBuffer.append(c);
                }
                break;

                case STATE_TAG_OPEN_ATTRIBUTE_VALUE_ENTITY:
                case STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE_ENTITY:
                {
                    if (c == ';')
                    {
                        nameBuffer.append(getEntity());
                        if (state == STATE_TAG_OPEN_ATTRIBUTE_VALUE_ENTITY)
                        {
                            state = STATE_TAG_OPEN_ATTRIBUTE_VALUE;
                        }
                        else
                        {
                            state = STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE;
                        }
                        break;
                    }

                    entityBuffer.append(c);
                }
                break;

                case STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE:
                {
                    if (c == quoteCharacter)
                    {
                        if (nameBuffer.length() == 0)
                        {
                            state = STATE_TAG_OPEN_ATTRIBUTE_NAME;
                            break;
                        }

                        attributes.put(currentAttribute, getName());
                        state = STATE_TAG_OPEN_ATTRIBUTE_NAME;
                        break;
                    }

                    if (c == '&')
                    {
                        state = STATE_TAG_OPEN_ATTRIBUTE_QUOTED_VALUE_ENTITY;
                        break;
                    }

                    nameBuffer.append(c);
                }
                break;

                case STATE_TAG_OPEN_CLOSING:
                {
                    if (c == '>')
                    {
                        state = STATE_TEXT;
                        openTag(currentTag, attributes);
                        attributes.clear();
                        closeTag(currentTag);
                        break;
                    }
                }
                break;
            }
        }

        // System.out.println("GtmlParser: raw = " + text);
        // System.out.println("GtmlParser: text = " + getText());
        // System.out.println("GtmlParser: spans = " + mSpans);
    }

    private void closeTag(final String tagName)
    {
        // System.out.println("GtmlParser: close(" + tagName + ")");

        final Tag tag = (Tag)tagStack.get(tagStack.size() - 1);
        final int lastSpanStart = spanStart;
        spanStart = textBuffer.length();
        if ((tag != null) && (lastSpanStart != spanStart))
        {
            spans.add(new Span(tag.getSource(), lastSpanStart, spanStart));
        }

        tagStack.remove(tagStack.size() - 1);
    }

    private String getEntity()
    {
        final String value = parseEntity(entityBuffer.toString());
        entityBuffer.delete(0, entityBuffer.length());
        return value;
    }

    private String getName()
    {
        final String name = nameBuffer.toString();
        nameBuffer.delete(0, nameBuffer.length());
        return name;
    }

    private void openTag(final String tagName, final Map tagAttributes)
    {
        // System.out.println("GtmlParser: open(" + tagName + ", " + tagAttributes + ")");

        Tag lastTag;
        SpanSource source;
        if (tagStack.size() == 0)
        {
            lastTag = null;
            source = new SpanSource();
        }
        else
        {
            lastTag = (Tag)tagStack.get(tagStack.size() - 1);
            source = new SpanSource(lastTag.getSource());
        }

        if (tagName.equals("font"))
        {
            final Color color = parseColor((String)tagAttributes.get("color"));
            if (color != null)
            {
                source.setTextColor(color);
            }

            final Color bgcolor = parseColor((String)tagAttributes.get("bgcolor"));
            if (bgcolor != null)
            {
                source.setBackgroundColor(bgcolor);
            }

            final float size = parseFloat((String)tagAttributes.get("size"), 0f);
            if (size > 0)
            {
                source.setFontSize(size);
            }
        }
        else if (tagName.equals("br"))
        {
            breaks.add(new Integer(textBuffer.length()));
        }
        else if (tagName.equals("b"))
        {
            source.setBold(true);
        }
        else if (tagName.equals("i"))
        {
            source.setItalicized(true);
        }
        else if (tagName.equals("u"))
        {
            source.setUnderlined(true);
        }
        else if (tagName.equals("a"))
        {
            final String href = (String)tagAttributes.get("href");
            final String className = (String)tagAttributes.get("class");
            if (className == null)
            {
                source.setUnderlined(true);
                source.setTextColor(parseColor("#03F"));
            }
            if (href != null)
            {
                if (href.startsWith("http"))
                {
                    source.setClickAction(new Runnable()
                    {
                        public void run()
                        {
                            UtilityFunctions.launchBrowser(href);
                        }
                    });
                }
            }
        }

        final int lastSpanStart = spanStart;
        spanStart = textBuffer.length();
        if ((lastTag != null) && (lastSpanStart != spanStart))
        {
            spans.add(new Span(lastTag.getSource(), lastSpanStart, spanStart));
        }

        final Tag tag = new Tag(tagName, tagAttributes, source);
        tagStack.add(tag);
    }

    private void reset()
    {
        // System.out.println("GtmlParser: reset()");
        currentTag = null;
        currentAttribute = null;
        state = STATE_TEXT;
        spanStart = 0;
        tagStack.clear();
        spans.clear();
        breaks.clear();
        attributes.clear();
        clear(textBuffer);
        clear(entityBuffer);
        clear(nameBuffer);
    }

}
