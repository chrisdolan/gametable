/*
 * ColorComboCellRenderer.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import java.awt.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;



/**
 * TODO: comment
 * 
 * @author sephalon
 */
public class ColorComboCellRenderer extends JLabel implements ListCellRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = 4720853422440583368L;
    private final ImageIcon[] m_icons;
    private final ImageIcon[] m_selectedIcons;

    public ColorComboCellRenderer()
    {
        final int width = 79;
        final int height = 17;

        m_icons = new ImageIcon[GametableFrame.COLORS.length];
        m_selectedIcons = new ImageIcon[GametableFrame.COLORS.length];

        for (int i = 0; i < m_icons.length; i++)
        {
            final Color col = new Color(GametableFrame.COLORS[i].intValue());
            Image img = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration().createCompatibleImage(width, height, Transparency.BITMASK);
            createImage(width, height);
            Graphics g = img.getGraphics();
            g.setColor(col);
            g.fillRect(2, 2, img.getWidth(null) - 4, img.getHeight(null) - 4);
            m_icons[i] = new ImageIcon(img);

            img = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height, Transparency.BITMASK);
            g = img.getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
            g.setColor(col);
            g.fillRect(2, 2, img.getWidth(null) - 4, img.getHeight(null) - 4);
            m_selectedIcons[i] = new ImageIcon(img);
        }
    }

    /**
     * This is the only method defined by ListCellRenderer. We just reconfigure the JLabel each time we're called.
     * 
     * @param value Value to display.
     * @param index Cell Index.
     * @param isSelected Is the cell selected.
     * @param cellHasFocus The list and cell have the focus.
     */
    public Component getListCellRendererComponent(final JList list, final Object value, final int index,
        final boolean isSelected, final boolean cellHasFocus)
    {

        setText(value.toString());

        // find the color needed
        final int col = ((Integer)value).intValue();
        int idx = -1;
        for (int i = 0; i < GametableFrame.COLORS.length; i++)
        {
            if (GametableFrame.COLORS[i].intValue() == col)
            {
                idx = i;
            }
        }

        if (idx == -1)
        {
            return this;
        }

        setText("");

        if (isSelected)
        {
            setIcon(m_selectedIcons[idx]);
        }
        else
        {
            setIcon(m_icons[idx]);
        }
        return this;
    }
}
