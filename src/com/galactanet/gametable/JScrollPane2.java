/*
 * JScrollPane2.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;


/**
 * TODO: comment
 *
 * @author sephalon
 */
public class JScrollPane2 extends JScrollPane
{
    private boolean m_bDisregardNextPaint;
    Image           m_currentImage;



    public void paint(Graphics g)
    {
        ImageObserver observer = this;
        if (m_bDisregardNextPaint)
        {
            m_bDisregardNextPaint = false;
            if (m_currentImage != null)
            {
                g.drawImage(m_currentImage, 0, 0, observer);
            }
            return;
        }

        if (m_currentImage == null || m_currentImage.getWidth(observer) != getWidth()
            || m_currentImage.getHeight(observer) != getHeight())
        {
            m_currentImage = createImage(getWidth(), getHeight());
        }

        Graphics offscreenG = m_currentImage.getGraphics();
        super.paint(offscreenG);
        g.drawImage(m_currentImage, 0, 0, observer);
    }

    public void disregardNextPaint()
    {
        m_bDisregardNextPaint = true;
    }
}
