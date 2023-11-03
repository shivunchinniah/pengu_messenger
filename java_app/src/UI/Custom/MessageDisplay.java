package UI.Custom;

import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * This class creates a message display box.
 * @author Shivun Chinniah
 */
public class MessageDisplay extends javax.swing.JPanel {

    private final JPopupMenu options = new JPopupMenu();
    private final JMenuItem deleteChat = new JMenuItem("Delete Chat");
    private final int chatID;
    private final String username;
    
    private void setMessageDisplayComponentPopupMenu(JPopupMenu popup){
        setComponentPopupMenu(popup);
    }
    
    /**
     * Parameterized constructor
     * @param username The username of the chat.
     * @param displayName The display/friendly name of the chat.
     * @param ChatPreview The preview of the chat's last message.
     * @param chatStatus The status of the chat's last message.
     * @param contactGroup The contact group name of the user.
     * @param contactColor The contact group Color of the user.
     * @param chatID The chat ID of the chat.
     * @param lastMessageSeen TRUE if the chat's last message was seen, ELSE false.
     */
    public MessageDisplay(String username, String displayName, String ChatPreview, String chatStatus, String contactGroup, Color contactColor, int chatID, boolean lastMessageSeen) {
        initComponents();
        ChatPreview = shortenMessage(ChatPreview);
        
        if(ChatPreview.equals("")){
            ChatPreview = " ";
        }
 
        options.add(deleteChat);
        setMessageDisplayComponentPopupMenu(options);
        this.username = username;
        this.chatID = chatID;
        Name.setText(username);
        FriendlyName.setText(displayName);
        Message.setText(ChatPreview);
        Date.setText(chatStatus);
        pnlContactType.setBackground(contactColor);
        pnlContactType.setToolTipText(contactGroup + " Contact");
        if (lastMessageSeen) {
            Unread.setVisible(false);
        }
        deleteChat.addActionListener((ActionEvent ev) -> {
            actionDeleteChat();
        });
        

    }
    
    /**
     * Override this 
     */
    public void actionDeleteChat(){
        
    }
    
    // Shortens the message if it is too long to be displayed
    private static String shortenMessage(String message){
        if(message.length() >40){
            return message.substring(0,37)+"...";
        }else{
            return message;
        }
    }
    
    private void setComponentBackground(Color bg){
        setBackground(bg);
    }
    
     /**
     * Parameterized constructor
     * 
     * @param selected IF TRUE the chat icon will be a different shade from the others.
     * @param username The username of the chat.
     * @param displayName The display/friendly name of the chat.
     * @param ChatPreview The preview of the chat's last message.
     * @param chatStatus The status of the chat's last message.
     * @param contactGroup The contact group name of the user.
     * @param contactColor The contact group Color of the user.
     * @param chatID The chat ID of the chat.
     * @param lastMessageSeen TRUE if the chat's last message was seen, ELSE false.
     */
    public MessageDisplay(boolean selected, String username, String displayName, String ChatPreview, String chatStatus, String contactGroup, Color contactColor, int chatID, boolean lastMessageSeen) {
        initComponents();
        options.add(deleteChat);
        
        ChatPreview = shortenMessage(ChatPreview);
        
         if(ChatPreview.equals("")){
            ChatPreview = " ";
         }
        
        if (selected) {
            setComponentBackground(new Color(214, 214, 214));
        }
        
        this.chatID = chatID;
        Name.setText(username);
        FriendlyName.setText(displayName);
        Message.setText(ChatPreview);
        Date.setText(chatStatus);
        this.username = username;

        pnlContactType.setBackground(contactColor);
        pnlContactType.setToolTipText(contactGroup + " Contact");
        if (lastMessageSeen) {
            Unread.setVisible(false);
        }
        setMessageDisplayComponentPopupMenu(options);
        
        deleteChat.addActionListener((ActionEvent ev) -> {
            actionDeleteChat();
        });

    }

    /**
     * Override This method to choose what happens when Message Display Box is
     * clicked
     */
    public void messageDisplayClicked() {

    }

    /**
     * Accessor method or the chatID field.
     * @return Returns the value of the chatID field.
     */
    public int getChatID() {
        return chatID;
    }
    
    /**
     * Accessor method or the username field.
     * @return Returns the value of the username field.
     */
    public String getUsername() {
        return username;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        Name = new javax.swing.JLabel();
        Date = new javax.swing.JLabel();
        Message = new javax.swing.JLabel();
        Unread = new javax.swing.JLabel();
        FriendlyName = new javax.swing.JLabel();
        pnlContactType = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setBackground(new java.awt.Color(248, 248, 248));
        setForeground(new java.awt.Color(237, 237, 237));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        Name.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        Name.setForeground(new java.awt.Color(51, 51, 51));
        Name.setText("Name");

        Date.setForeground(new java.awt.Color(51, 51, 51));
        Date.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        Date.setText("10/10/10 11:11");

        Message.setForeground(new java.awt.Color(102, 102, 102));
        Message.setText("Helo world this is sample message. This is the...");

        Unread.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/unread symbol.png"))); // NOI18N
        Unread.setToolTipText("Unread");

        FriendlyName.setFont(new java.awt.Font("Arial", 2, 14)); // NOI18N
        FriendlyName.setForeground(new java.awt.Color(153, 153, 153));
        FriendlyName.setText("Friendly Name");

        pnlContactType.setBackground(new java.awt.Color(51, 255, 153));
        pnlContactType.setForeground(new java.awt.Color(51, 255, 51));

        javax.swing.GroupLayout pnlContactTypeLayout = new javax.swing.GroupLayout(pnlContactType);
        pnlContactType.setLayout(pnlContactTypeLayout);
        pnlContactTypeLayout.setHorizontalGroup(
            pnlContactTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        pnlContactTypeLayout.setVerticalGroup(
            pnlContactTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jSeparator1.setForeground(new java.awt.Color(160, 160, 161));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(pnlContactType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(Name, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Unread))
                            .addComponent(jSeparator1)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 3, Short.MAX_VALUE)
                                .addComponent(Message, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Date, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(FriendlyName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Name)
                    .addComponent(Unread))
                .addGap(2, 2, 2)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(FriendlyName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Message)
                    .addComponent(Date))
                .addGap(8, 8, 8))
            .addComponent(pnlContactType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        // TODO add your handling code here:
        messageDisplayClicked();
    }//GEN-LAST:event_formMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Date;
    private javax.swing.JLabel FriendlyName;
    private javax.swing.JLabel Message;
    private javax.swing.JLabel Name;
    private javax.swing.JLabel Unread;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel pnlContactType;
    // End of variables declaration//GEN-END:variables

}