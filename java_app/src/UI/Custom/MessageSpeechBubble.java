package UI.Custom;

import com.sun.prism.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;

/**
 * This class creates a message bubble.
 * @author Shivun Chinniah
 */
public class MessageSpeechBubble extends JPanel {

    private JLabel text = new JLabel();
    private JPanel bubble = new JPanel();

    private static final Color BUBBLE_COL_RIGHT = new Color(15, 188, 186);

    private static final Color BUBBLE_COL_LEFT = new Color(89, 89, 89);
    private static final Color TEXT_COL = Color.white;
    private static final AbstractBorder BORDER_LIFT = new MessageSpeechBubble.TextBubbleBorder(0, 16, 8);
    private static final AbstractBorder BORDER_RIGHT = new MessageSpeechBubble.TextBubbleBorder(0, 16, 8, false);

    private JPopupMenu options = new JPopupMenu();
    private JMenuItem deleteMessage = new JMenuItem("Delete Message");
    //private JMenuItem forwardMessage = new JMenuItem("Forward Message"); // to be added in future
    private int messageID;
    
    private boolean isReceived; 
    
    
    
    /**
     * Accessor method or the isReceived field.
     * @return Returns the value of the isReceived field.
     */
    public boolean isReceived(){
        return isReceived;
    }
    
    /**
     * Accessor method or the messageID field.
     * @return Returns the value of the messageID field.
     */
    public int getMessageID() {
        return messageID;
    }
    
    private void setComponentLayout(LayoutManager lmg){
        setLayout(lmg);
    }
    
    /**
     * Parameterized constructor method.
     * 
     * @param in The input String for the message.
     * @param left IF true the message will be on the left-hand side of the message view box.
     * @param date The date String of the message.
     * @param status The Status String of the message.
     * @param messageID  The ID of the message.
     */
    public MessageSpeechBubble(String in, boolean left, String date, String status, int messageID) {
        isReceived = left;
        this.messageID = messageID;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("/UI/resources/OpenSans-Regular.ttf")));

        } catch (IOException | FontFormatException e) {
            //Handle exception

        }

        String message = wrap(escapeHTML(in), 45, "<br>");
        text.setText("<html><body>" + message + "</body></html>");

        text.setFont(new Font("Open Sans", Font.PLAIN, 12));

        GridBagConstraints c = new GridBagConstraints();
        JPanel spacer = new JPanel();
        
       
        if (left) {
            spacer.setPreferredSize(new Dimension(620, 0));
            bubble.setBorder(BORDER_LIFT);
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 5, 0, 5);
            bubble.setBackground(BUBBLE_COL_LEFT);
        } else {
            spacer.setPreferredSize(new Dimension(620, 0));
            bubble.setBorder(BORDER_RIGHT);
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(0, 0, 0, 10);
            bubble.setBackground(BUBBLE_COL_RIGHT);
        }

        this.setComponentLayout(new GridBagLayout());
        text.setForeground(TEXT_COL);

        //text.setOpaque(true);
        bubble.setLayout(new GridBagLayout());

        GridBagConstraints gbc4 = new GridBagConstraints();

        gbc4.gridx = 0;
        gbc4.gridy = 0;
        gbc4.gridwidth = 3;
        gbc4.fill = GridBagConstraints.HORIZONTAL;

        bubble.add(text, gbc4);

        gbc4.gridx = 0;
        gbc4.gridy = 2;
        gbc4.gridwidth = 1;
        gbc4.fill = GridBagConstraints.HORIZONTAL;
        gbc4.anchor = GridBagConstraints.LAST_LINE_END;
        gbc4.insets = new Insets(5, 0, 0, 0);
        gbc4.weightx = 1.0;

        JLabel dateL = new JLabel(date + " " + status, SwingConstants.RIGHT);
        dateL.setForeground(new Color(230, 230, 230));
        dateL.setFont(new Font("Open Sans", Font.PLAIN, 10));
        dateL.setToolTipText("dd/mm/yy 24hour:min");

        bubble.add(dateL, gbc4);

        c.gridy = 0;
        c.gridx = 0;
        //c.insets = new Insets(0, 5, 0, 5);

        addComponent(bubble, c);
        
        setComponentBackground(Color.WHITE);
        c.gridy = 1;

        addComponent(spacer, c);

        options.add(deleteMessage);
        
        deleteMessage.addActionListener((ActionEvent ev) -> {
            actionDeleteMessage();
        });
        
        //options.add(forwardMessage);

        bubble.setComponentPopupMenu(options);

    }
    
    private void setComponentBackground(Color bg){
        setBackground(bg);
    }
    
    private void addComponent(Component component, Object constraints){
        add(component, constraints);
    }
    
    /**
     * Override this to choose what happens when message is deleted.
     */
    public void actionDeleteMessage() {
        
    }

    private static String wrap(String in, int len, String newlineSym) {
        in = in.trim();
        in = in.replace("\n", newlineSym);

        if (in.length() < len) {
            return in;
        }
        if (in.substring(0, len).contains("\n") && false) {
            return in.substring(0, in.indexOf("\n")).trim() + "\n\n" + wrap(in.substring(in.indexOf("\n") + 1), len, newlineSym);
        }
        int place = Math.max(Math.max(in.lastIndexOf(" ", len), in.lastIndexOf("\t", len)), in.lastIndexOf("-", len));
        if (place == -1) {
            return in.substring(0, len) + newlineSym + wrap(in.substring(len), len, newlineSym);
        } else {

            return in.substring(0, place).trim() + newlineSym + wrap(in.substring(place), len, newlineSym);
        }

    }

    private static class TextBubbleBorder extends AbstractBorder {

        //private Color color;
        private int thickness = 0;
        private int radii = 8;
        private int pointerSize = 7;
        private Insets insets = null;
        private BasicStroke stroke = null;
        private int strokePad;
        private final int pointerPad = 4;
        private boolean left = true;
        RenderingHints hints;

        TextBubbleBorder() {
            this(4, 8, 7);
         
        }

        TextBubbleBorder(int thickness, int radii, int pointerSize) {
            this.thickness = thickness;
            this.radii = radii;
            this.pointerSize = pointerSize;

            stroke = new BasicStroke(1, 1, 1, 1);
            strokePad = thickness / 2;

            hints = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = radii + strokePad;
            int bottomPad = pad + pointerSize + strokePad;
            insets = new Insets(pad - 10, pad - 6, bottomPad - 10, pad - 6);
           
        }

        TextBubbleBorder(
                int thickness, int radii, int pointerSize, boolean left) {
            this(thickness, radii, pointerSize);
            this.left = left;
            
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return insets;
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            return getBorderInsets(c);
        }

        @Override
        public void paintBorder(
                Component c,
                Graphics g,
                int x, int y,
                int width, int height) {

            super.paintBorder(c, g, x, y, width, height);

            Graphics2D g2 = (Graphics2D) g;

            int bottomLineY = height - thickness - pointerSize;

            RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(
                    0 + strokePad,
                    0 + strokePad,
                    width - thickness - 5,
                    bottomLineY - 5,
                    radii,
                    radii);

            Polygon pointer = new Polygon();

            if (left) {
                // left point
                pointer.addPoint(
                        strokePad + radii + pointerPad,
                        bottomLineY);
                // right point
                pointer.addPoint(
                        strokePad + radii + pointerPad + pointerSize,
                        bottomLineY);
                // bottom point
                pointer.addPoint(
                        strokePad + radii + pointerPad + (pointerSize / 2),
                        height - strokePad);
            } else {
                // left point
                pointer.addPoint(
                        width - (strokePad + radii + pointerPad),
                        bottomLineY);
                // right point
                pointer.addPoint(
                        width - (strokePad + radii + pointerPad + pointerSize),
                        bottomLineY);
                // bottom point
                pointer.addPoint(
                        width - (strokePad + radii + pointerPad + (pointerSize / 2)),
                        height - strokePad);
            }

        }

    }

    private static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

}
