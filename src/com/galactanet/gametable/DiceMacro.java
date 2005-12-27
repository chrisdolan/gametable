/*
 * DiceMacro.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Encapsulates a dice macro.
 * 
 * @author sephalon
 */
public class DiceMacro
{
    // --- Types ---

    /**
     * A class to hold the results of a roll.
     * 
     * @author iffy
     */
    public static class Result
    {
        public int    value;
        public String roll;
        public String result;

        public Result(int val, String rollText, String resultText)
        {
            value = val;
            roll = rollText;
            result = resultText;
        }
    }

    /**
     * An encapsulation of one term of the DiceMacro.
     * 
     * @author sephalon
     */
    private static class Term
    {
        // note, a "1" sided die is a bonus
        // and qty can be negative
        // m_keep is number of highest rolls to keep. If negative, keep
        // lowest rolls. A value of 0 means to keep all.
        int m_die;
        int m_qty;
        int m_keep;

        public Term(int[] numbers)
        {
            m_qty = numbers[0];
            m_die = numbers[1];
            m_keep = numbers[2];
        }

        public boolean equals(Term comp)
        {
            return m_die == comp.m_die && m_qty == comp.m_qty && m_keep == comp.m_keep;
        }

        public String getDieName()
        {
            if (m_die < 2)
            {
                return "";
            }
            String name = "d" + m_die;
            return name;
        }

        public String toString()
        {
            StringBuffer result = new StringBuffer();

            if (m_die > 1)
            {
                if (m_qty < -1 || m_qty > 1)
                {
                    result.append(m_qty);
                }
                else if (m_qty == -1)
                {
                    result.append('-');
                }

                result.append('d');
                result.append(m_die);
                if (m_keep < 0)
                {
                    result.append('l');
                    result.append(0 - m_keep);
                }
                else if (m_keep > 0)
                {
                    result.append('h');
                    result.append(m_keep);
                }
            }
            else
            {
                result.append(m_qty);
            }

            return result.toString();
        }
    }

    List   m_dieTypes = new ArrayList(); // full of DiceMacro_RollType instances
    String m_name     = null;
    String m_macro    = "0";

    public DiceMacro()
    {
    }

    public DiceMacro(String macro, String name)
    {
        if (!init(macro, name))
        {
            m_name = null;
            m_macro = "0";
        }
    }

    public String toString()
    {
        if (m_name == null || isSameMacroString(m_name, m_macro))
        {
            return m_macro;
        }

        return m_name + " (" + m_macro + ")";
    }

    public String doMacro()
    {
        if ("0".equals(m_macro))
        {
            return "";
        }

        Player me = GametableFrame.getGametableFrame().getMePlayer();
        String name = me.getCharacterName();

        Result result = roll();
        String ret = name + " rolls " + result.roll + ": [" + result.result + "] = " + result.value;
        return ret;
    }

    public Result roll()
    {
        if ("0".equals(m_macro))
        {
            return null;
        }

        StringBuffer ret = new StringBuffer();
        int total = 0;

        for (int i = 0; i < m_dieTypes.size(); i++)
        {
            // get the die type
            Term dieType = (Term)m_dieTypes.get(i);
            boolean bFirstAdd = (i == 0);

            int subtotal = 0;
            // special case: straight bonus
            if (dieType.m_die < 2)
            {
                if (dieType.m_qty > 0)
                {
                    if (!bFirstAdd)
                    {
                        ret.append(" + ");
                    }
                    ret.append(dieType.m_qty);
                    subtotal += dieType.m_qty;
                }
                else if (dieType.m_qty < 0)
                {
                    if (!bFirstAdd)
                    {
                        ret.append(' ');
                    }
                    ret.append("- ");
                    ret.append(-dieType.m_qty);
                    subtotal += dieType.m_qty;
                }
            }
            else if (dieType.m_keep == 0)
            {
                // normal case: die rollin!
                // Straight addition
                int bound = Math.abs(dieType.m_qty);
                boolean negative = (dieType.m_qty < 0);
                if (bound > 0)
                {
                    // single quantity - no parentheticals
                    if (bound == 1)
                    {
                        int value = rollDie(dieType.m_die);
                        if (bFirstAdd)
                        {
                            if (negative)
                            {
                                ret.append("- ");
                            }
                        }
                        else
                        {
                            ret.append(negative ? " - " : " + ");
                        }

                        ret.append(value);
                        if (negative)
                        {
                            subtotal -= value;
                        }
                        else
                        {
                            subtotal += value;
                        }
                    }
                    else
                    {
                        // multiple quantity - parentheticals
                        if (bFirstAdd)
                        {
                            if (negative)
                            {
                                ret.append("- ");
                            }
                        }
                        else
                        {
                            ret.append(negative ? " - " : " + ");
                        }

                        ret.append('(');
                        for (int j = 0; j < bound; j++)
                        {
                            int value = rollDie(dieType.m_die);
                            if (j > 0)
                            {
                                ret.append(" + ");
                            }
                            ret.append(value);
                            if (negative)
                            {
                                subtotal -= value;
                            }
                            else
                            {
                                subtotal += value;
                            }
                        }
                        ret.append(')');
                    }
                }
            }
            else
            {
                // Keep some rolls.
                int number = Math.abs(dieType.m_qty);
                if (!bFirstAdd)
                {
                    ret.append((dieType.m_qty < 0) ? " - " : " + ");
                }
                ret.append('(');
                int[] rolls = new int[number];
                for (int index = 0; index < number; index++)
                {
                    rolls[index] = rollDie(dieType.m_die);
                    if (index < 1)
                    {
                        ret.append(rolls[index]);
                    }
                    else
                    {
                        ret.append(',');
                        ret.append(rolls[index]);
                    }
                }
                Arrays.sort(rolls);
                if (dieType.m_keep > 0)
                {
                    // Keep highest
                    for (int index = dieType.m_qty - dieType.m_keep; index < dieType.m_qty; index++)
                    {
                        if (dieType.m_qty < 0)
                        {
                            subtotal -= rolls[index];
                        }
                        else
                        {
                            subtotal += rolls[index];
                        }
                    }
                    ret.append(")h");
                    ret.append(dieType.m_keep);
                }
                else
                {
                    // Keep lowest
                    for (int index = 0; index < -dieType.m_keep; index++)
                    {
                        if (dieType.m_qty < 0)
                        {
                            subtotal -= rolls[index];
                        }
                        else
                        {
                            subtotal += rolls[index];
                        }
                    }
                    ret.append(")l");
                    ret.append(-dieType.m_keep);
                }

            }
            total += subtotal;
            //System.out.println(dieType + ": " + subtotal + " (" + total + ")");
        }

        return new Result(total, getRollString(), ret.toString());
    }

    public int rollDie(int sides)
    {
        return UtilityFunctions.getRandom(sides) + 1;
    }

    public String getRollString()
    {
        if ("0".equals(m_macro))
        {
            return "";
        }

        if (!isSameMacroString(m_name, m_macro) && m_name != null && m_name.length() > 0)
        {
            return m_name + " (" + m_macro + ")";
        }
        return m_macro;
    }

    public String getMacroString()
    {
        if ("0".equals(m_macro))
        {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        boolean bIsFirst = true;

        for (int i = 0; i < m_dieTypes.size(); i++)
        {
            String dice = m_dieTypes.get(i).toString();
            if (!bIsFirst)
            {
                buffer.append(' ');
            }

            boolean negate = (dice.charAt(0) == '-');
            if (negate)
            {
                buffer.append("- ");
                buffer.append(dice.substring(1).trim());
            }
            else
            {
                if (!bIsFirst)
                {
                    buffer.append("+ ");
                }

                buffer.append(dice);
            }
            bIsFirst = false;
        }

        return buffer.toString();
    }

    private static boolean isSameMacroString(String a, String b)
    {
        if (a == b)
        {
            return true;
        }
        
        if (a == null || b == null)
        {
            return false;
        }
        
        return normalizeMacroString(a).equals(normalizeMacroString(b));
    }
    
    private static String normalizeMacroString(String in)
    {
        // Remove spaces.
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < in.length(); index++)
        {
            if (!Character.isWhitespace(in.charAt(index)))
            {
                buffer.append(Character.toLowerCase(in.charAt(index)));
            }
        }

        return buffer.toString();
    }

    public boolean init(String macro, String name)
    {
        try
        {
            m_name = name;
            m_macro = normalizeMacroString(macro);

            // Parse the macro string. It will be something like
            // "3d6 + 4" or "2d4 + 3d6h2 + 8"

            if ("0".equals(m_macro))
            {
                return true;
            }

            // Grab individual dice rolls.
            boolean isNegative = false;
            // #dice, type, #keep
            int[] numbers = {
                1, 1, 0
            };

            // Corresponds with index of numbers array
            int phase = 0;
            int startOfCurrentNumber = 0;
            int length = m_macro.length();
            for (int index = 0; index < length; ++index)
            {
                char c = m_macro.charAt(index);
                boolean isLast = (index == (length - 1));
                if (!Character.isDigit(c) || isLast)
                {
                    // End of this number.
                    if (startOfCurrentNumber != index || isLast)
                    {
                        // end is position after number.
                        int end = (isLast ? length : index);
                        String numberStr = m_macro.substring(startOfCurrentNumber, end);
                        int number = Integer.parseInt(numberStr);
                        numbers[phase] = isNegative ? (-number) : number;
                        isNegative = false;
                    }
                    startOfCurrentNumber = index + 1;

                    // Check for end of dice roll.
                    if (c == '+' || c == '-' || isLast)
                    {
                        if (index > 0 || isLast)
                        {
                            m_dieTypes.add(new Term(numbers));
                        }
                        isNegative = (c == '-');
                        numbers[0] = 1;
                        numbers[1] = 1;
                        numbers[2] = 0;
                        phase = 0;
                    }
                    else if (c == 'd')
                    {
                        numbers[0] = isNegative ? (-numbers[0]) : numbers[0];
                        isNegative = false;
                        phase = 1;
                    }
                    else if (c == 'h')
                    {
                        isNegative = false;
                        phase = 2;
                    }
                    else if (c == 'l')
                    {
                        isNegative = true;
                        phase = 2;
                    }
                    else
                    {
                        m_name = null;
                        m_macro = "0";
                        return false;
                    }
                }
            }

            m_macro = getMacroString();
            if (m_name != null && m_name.length() < 1)
            {
                m_name = m_macro;
            }
            return true;
        }
        catch (Throwable ex)
        {
            Log.log(Log.SYS, "parse error: \"" + m_macro + "\" (\"" + macro + "\")");
            Log.log(Log.SYS, ex);
            m_name = null;
            m_macro = "0";
            return false;
        }
    }

    public void writeToStream(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(m_name);
        dos.writeUTF(m_macro);
    }

    public void initFromStream(DataInputStream dis) throws IOException
    {
        String name = dis.readUTF();
        String macro = dis.readUTF();
        init(macro, name);
    }
}
