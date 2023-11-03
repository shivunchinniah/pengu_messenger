/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI.Custom;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.AbstractBorder;

/**
 * This class creates a border with rounded corners.
 * @author Shivun Chinniah
 */
public class RoundedCornerBorder extends AbstractBorder {

    private final Color ALPHA_ZERO;
    

    /**
     * Parameterized constructor method.
     * @param trimCol The Color of the the panel's container to create the round border effect.
     * @param rad The radius of the corners.
     */
    public RoundedCornerBorder(Color trimCol, int rad) {
        ALPHA_ZERO = trimCol;
        this.rad = rad;

    }

    /**
     * Default constructor method.
     */
    public RoundedCornerBorder() {
        ALPHA_ZERO = new Color(69, 162, 158);
        this.rad = 15;

    }

    
   

    private final int rad;

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape border = getBorderShape(x, y, width - 1, height - 1);
        g2.setPaint(ALPHA_ZERO);
        Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
        corner.subtract(new Area(border));
        g2.fill(corner);
        g2.setPaint(Color.WHITE);
        g2.draw(border);
        g2.dispose();
    }
    
    /**
     * Calculates the shape of the border.
     * 
     * 
     * @param x the X coordinate of the newly constructed RoundRectangle2D 
     * @param y the Y coordinate of the newly constructed RoundRectangle2D 
     * @param w the width to which to set the newly constructed RoundRectangle2D 
     * @param h the height to which to set the newly constructed RoundRectangle2D
     * @return The calculated Shape of the border.
     */
    public Shape getBorderShape(int x, int y, int w, int h) {
        int r = rad; //h / 2;

        return new RoundRectangle2D.Double(x, y, w, h, r, r);

    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(4, 16, 4, 16);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(4, 16, 4, 16);
        return insets;
    }
}
