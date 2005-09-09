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
 * TODO: comment
 * 
 * @author sephalon
 */
public class DiceMacro
{
    List   m_dieTypes = new ArrayList(); // full of DiceMacro_RollType instances
    String m_name;
    String m_macro;



    public DiceMacro()
    {
    }

    public String toString()
    {
        return m_name;
    }

    public String doMacro()
    {
        Player me = GametableFrame.getGametableFrame().getMePlayer();
        String name = me.getCharacterName();

        String ret = name + " rolls " + getRollString() + ": " + roll();
        return ret;
    }

    public String roll()
    {
        String ret = "[";
        boolean bFirstAdd = true;
        int total = 0;

        for (int i = 0; i < m_dieTypes.size(); i++)
        {
            // get the die type
            DiceMacro_RollType dieType = (DiceMacro_RollType)m_dieTypes.get(i);

            // special case: straight bonus
            if (dieType.m_die < 2)
            {
                total += dieType.m_qty;
                if (dieType.m_qty > 0)
                {
                    ret += "+" + dieType.m_qty;
                }
                else if (dieType.m_qty < 0)
                {
                    ret += "" + dieType.m_qty;
                }
            }
            else
            {
                if (dieType.m_keep == 0)
                {
                    // normal case: die rollin!
                    // Straight addition
                    for (int j = 0, bound = Math.abs(dieType.m_qty); j < bound; j++)
                    {
                        int value = rollDie(dieType.m_die);
                        if (bFirstAdd)
                        {
                            ret += "" + value;
                            bFirstAdd = false;
                        }
                        else
                        {
                            ret += ((dieType.m_qty < 0) ? "-" : "+") + value;
                        }
                        if (dieType.m_qty < 0)
                            total -= value;
                        else
                            total += value;
                    }
                }
                else
                {
                    // Keep some rolls.
                    int number = Math.abs(dieType.m_qty);
                    ret += (bFirstAdd ? "" : ((dieType.m_qty < 0) ? "-" : "+")) + "(";
                    bFirstAdd = true;
                    int[] rolls = new int[number];
                    for (int index = 0; index < number; index++)
                    {
                        rolls[index] = rollDie(dieType.m_die);
                        if (bFirstAdd)
                        {
                            ret += "" + rolls[index];
                            bFirstAdd = false;
                        }
                        else
                        {
                            ret += "," + rolls[index];
                        }
                    }
                    Arrays.sort(rolls);
                    if (dieType.m_keep > 0)
                    {
                        // Keep highest
                        for (int index = dieType.m_qty - dieType.m_keep; index < dieType.m_qty; index++)
                            if (dieType.m_qty < 0)
                                total -= rolls[index];
                            else
                                total += rolls[index];
                        ret += ")h" + dieType.m_keep;
                    }
                    else
                    {
                        // Keep lowest
                        for (int index = 0; index < -dieType.m_keep; index++)
                            if (dieType.m_qty < 0)
                                total -= rolls[index];
                            else
                                total += rolls[index];
                        ret += ")l" + (-dieType.m_keep);
                    }
                }
            }
        }

        ret += "] = ";
        ret += "" + total;
        return ret;
    }

    public int rollDie(int sides)
    {
        double random = Math.random();
        double result = random * sides;
        int ret = (int)result;
        ret++;
        return ret;
    }

    public String getRollString()
    {
        String ret = getMacroString();

        if (!ret.equals(m_name))
        {
            return m_name + " (" + ret + ")";
        }
        return ret;
    }

    public String getMacroString()
    {
        StringBuffer buffer = new StringBuffer();
        boolean bIsFirst = true;

        for (int i = 0; i < m_dieTypes.size(); i++)
        {
            String dice = m_dieTypes.get(i).toString();
            if (!(bIsFirst || dice.charAt(0) == '-'))
                buffer.append('+');

            buffer.append(dice);
            bIsFirst = false;
        }

        return buffer.toString();
    }

    public boolean init(String macro, String name)
    {
        try
        {
            m_name = name;
            m_macro = macro;

            // Parse the macro string. It will be something like
            // "3d6 + 4" or "2d4 + 3d6h2 + 8"

            // Remove spaces.
            StringBuffer buffer = new StringBuffer();
            for (int index = 0; index < m_macro.length(); index++)
                if (m_macro.charAt(index) != ' ')
                    buffer.append(m_macro.charAt(index));

            // Grab individual dice rolls.
            boolean isNegative = false;
            // #dice, type, #keep
            int[] numbers = {
                1, 1, 0
            };
            // Corresponds with index of numbers array
            int phase = 0;
            int startOfCurrentNumber = 0;
            for (int index = 0; index < buffer.length(); index++)
            {
                if (buffer.charAt(index) < '0' || buffer.charAt(index) > '9' || index == buffer.length() - 1)
                {
                    // End of this number.

                    if (startOfCurrentNumber != index || index == buffer.length() - 1)
                    {
                        // end is position after number.
                        int end = (index == buffer.length() - 1) ? buffer.length() : index;
                        int number = Integer.parseInt(buffer.substring(startOfCurrentNumber, end));
                        numbers[phase] = isNegative ? (-number) : number;
                    }
                    startOfCurrentNumber = index + 1;

                    // Check for end of dice roll.
                    if (buffer.charAt(index) == '+' || buffer.charAt(index) == '-' || index == buffer.length() - 1)
                    {
                        m_dieTypes.add(new DiceMacro_RollType(numbers));
                        isNegative = buffer.charAt(index) == '-';
                        numbers[0] = 1;
                        numbers[1] = 1;
                        numbers[2] = 0;
                        phase = 0;
                    }
                    else if (buffer.charAt(index) == 'd')
                    {
                        isNegative = false;
                        phase = 1;
                    }
                    else if (buffer.charAt(index) == 'h')
                    {
                        isNegative = false;
                        phase = 2;
                    }
                    else if (buffer.charAt(index) == 'l')
                    {
                        isNegative = true;
                        phase = 2;
                    }
                    else
                        return false;
                }
            }
            return true;
        }
        catch (Exception ex)
        {
            Log.log(Log.SYS, ex);
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


class DiceMacro_RollType
{
    // note, a "1" sided die is a bonus
    // and qty can be negative
    // m_keep is number of highest rolls to keep. If negative, keep
    // lowest rolls. A value of 0 means to keep all.
    int m_die;
    int m_qty;
    int m_keep;



    public DiceMacro_RollType()
    {
    }

    public DiceMacro_RollType(int[] numbers)
    {
        m_qty = numbers[0];
        m_die = numbers[1];
        m_keep = numbers[2];
    }

    public boolean equals(DiceMacro_RollType comp)
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

        result.append(m_qty);

        if (m_die > 1)
        {
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

        return result.toString();
    }
}
