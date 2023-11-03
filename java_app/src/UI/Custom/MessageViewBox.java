package UI.Custom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import javax.swing.JPanel;
import pengu.messenger.PenguMessengerClient;
import pengu.messenger.PenguMessengerClient.Message;
import pengu.messenger.PenguMessengerClient.SentMessage;

/**
 * This class creates a Message View Box for displaying messages.
 * @author Shivun Chinniah
 */
public class MessageViewBox extends javax.swing.JPanel {

    private JPanel list;
    private PenguMessengerClient pmc = new PenguMessengerClient();
    private ModernScrollPane jsp;
    private Rectangle scroll = null;
    private double heightOffset = 0;
    private final int chatID;
    
    private final GridBagLayout gbl;

    /**
     * Parameterized constructor method.
     * 
     * @param chatID The ID of the chat to display.
     * @param height The height of the panel.
     * @param pmc The PenguMessengerClient to use.
     */
    public MessageViewBox(int chatID, int height, PenguMessengerClient pmc) {
        this.pmc = pmc;
        this.chatID = chatID;
        
       

        gbl = new GridBagLayout();

        gbl.columnWidths = new int[]{0, 0, 0, 0};
        gbl.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        
        list = new JPanel(gbl);
        list.setBackground(Color.white);
        list.setAlignmentX(TOP_ALIGNMENT);

        reloadComponents();

    }

    /**
     * Reloads the message view box but preserves the scroll.
     */
    public void reloadPreserveScroll() {
        scroll = jsp.getViewport().getViewRect();
        reloadComponents();
    }
    
    
    /**
     * Reloads the message view box.
     */
    public void reload(){
        reloadComponents();
    }
    
    // draws the componets of the panel.
    private void reloadComponents() {
        this.removeAll();
        
        list.removeAll();
        this.setLayout(new BorderLayout());
        
        list = new JPanel(gbl);
        list.setBackground(Color.white);
        list.setAlignmentX(TOP_ALIGNMENT);
        
        Message[] messages = this.pmc.getMessagesForChat(chatID);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridwidth = GridBagConstraints.REMAINDER;
        gbc2.ipady = 20;
        gbc2.weightx = 1;
        gbc2.insets = new Insets(1, 0, 1, 0);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        for (Message message : messages) {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm");
            if (message instanceof SentMessage) {
                String stat;
                SentMessage temp = (SentMessage) message;
                if (temp.getDeliveredStatus()) {
                    if (temp.getReadStatus()) {
                        stat = "R";
                    } else {
                        stat = "D";
                    }
                } else {
                    stat = "";// Default: Nothing
                }
                list.add(new MessageSpeechBubble(temp.getMessageContent(), false, df.format(message.getMessageTransactionDate()), stat, message.getMessageID()) {
                    @Override
                    public void actionDeleteMessage() {
                        deleteMessage(getMessageID(), isReceived());
                        heightOffset = getHeight();
                    }
                }, gbc2, list.getComponentCount());
            } else {

                list.add(new MessageSpeechBubble(message.getMessageContent(), true, df.format(message.getMessageTransactionDate()), " ", message.getMessageID()) {
                    @Override
                    public void actionDeleteMessage() {
                        deleteMessage(getMessageID(), isReceived());
                        heightOffset = getHeight();
                    }
                }, gbc2, list.getComponentCount());
            }
        }

        jsp = new ModernScrollPane(list, Color.gray);

        jsp.setAlignmentY(TOP_ALIGNMENT);
        jsp.setBackground(Color.white);
        jsp.getVerticalScrollBar().setUnitIncrement(10);

        

        if (scroll != null) {
            jsp.getViewport().scrollRectToVisible(new Rectangle((int)scroll.getX(), (int) ((int) scroll.getY()-heightOffset), (int)scroll.getWidth(), (int)scroll.getHeight()));
            
        }else{
            jsp.getViewport().scrollRectToVisible(new Rectangle(1, 1000000000, 1, 1));
        }

        this.add(jsp);
        this.revalidate();
        scroll = null;
    }

    
    //Deletes a message
    private void deleteMessage(int messageID, boolean isReceived) {
        if (isReceived) {
            pmc.deleteReceivedMessage(messageID);
        } else {//is Sent message
            pmc.deleteSentMessage(messageID);
        }
       
        reloadPreserveScroll();
      

    }

    

}
