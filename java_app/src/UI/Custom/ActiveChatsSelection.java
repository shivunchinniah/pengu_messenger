package UI.Custom;

import java.awt.BorderLayout;
import java.awt.Color;
import static java.awt.Component.TOP_ALIGNMENT;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import pengu.messenger.PenguMessengerClient;
import pengu.messenger.PenguMessengerClient.ActiveChatItem;

/**
 * This class creates an active chat selection panel.
 *
 * @author Shivun Chinniah
 */
public class ActiveChatsSelection extends javax.swing.JPanel {

    private JPanel list;
    ModernScrollPane jsp;
    PenguMessengerClient pmc = new PenguMessengerClient();
    ActiveChatItem[] items = new ActiveChatItem[0];
    Rectangle scroll = null;
    private int deleteChatTarget = -1;
    private int selectedChatID = -1;
    private String selectedUsername = "";

    //updates the items array.
    private void fetchChats() {
        items = pmc.getListOfActiveChats();
    }

    /**
     * Parameterized constructor method.
     *
     * @param pmc The PenguMessengerClient to use.
     */
    public ActiveChatsSelection(PenguMessengerClient pmc) {
        this.pmc = pmc;
        fetchChats();
        reloadFrame();
    }

    /**
     * Marks a specified username as selected.
     *
     * @param selectedUsername The username to mark as selected.
     */
    public void setSelected(String selectedUsername) {

        markSelectedUsername(selectedUsername);
    }

    /**
     * Refreshes the Panel
     */
    public void reload() {
        reloadFrame();
    }
    
    public void reloadPreserveScroll(){
        scroll = jsp.getViewport().getViewRect();
        reloadFrame();
    }

    // reloads all the items and makes the components
    private void reloadFrame() {
        fetchChats();

        this.removeAll();

        this.setLayout(new BorderLayout());

        GridBagLayout gbl = new GridBagLayout();

        gbl.columnWidths = new int[]{0, 0, 0, 0};
        gbl.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};

        list = new JPanel(gbl);
        //list.setBackground(Color.WHITE);
        list.setAlignmentY(TOP_ALIGNMENT);

        GridBagConstraints gbc2 = new GridBagConstraints();
        //gbc2.anchor = GridBagConstraints.ABOVE_BASELINE;
        gbc2.gridwidth = GridBagConstraints.REMAINDER;
        gbc2.ipady = 0;
        gbc2.weightx = 1;
        gbc2.insets = new Insets(1, 10, 1, 10);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        int addition = 0;
        for (int i = 0; i < items.length; i++) {
            if (i == 0) {
                gbc2.insets = new Insets(5, 10, 10, 15);
                addition += 10;
            } else if (i == items.length - 1) {
                gbc2.insets = new Insets(0, 10, 10, 15);
                addition += 10;
            } else {
                gbc2.insets = new Insets(0, 10, 10, 15);
                addition += 10;
            }

            if (items[i].getUsername().equals(selectedUsername)) {
                selectedChatID = items[i].getChatID();

                list.add(new MessageDisplay(true, items[i].getUsername(), items[i].getDisplayName(), items[i].getMessagePreview(), items[i].getMessageStatus(), items[i].getContactGroup(), items[i].getContactColor(), items[i].getChatID(), items[i].getLastMessageSeen()) {
                    @Override
                    public void messageDisplayClicked() {
                        markSelectedUsername(getUsername());
                    }

                    @Override
                    public void actionDeleteChat() {
                        handleDeleteChat(getChatID());
                    }

                }, gbc2, list.getComponentCount());
            } else {
                list.add(new MessageDisplay(items[i].getUsername(), items[i].getDisplayName(), items[i].getMessagePreview(), items[i].getMessageStatus(), items[i].getContactGroup(), items[i].getContactColor(), items[i].getChatID(), items[i].getLastMessageSeen()) {
                    @Override
                    public void messageDisplayClicked() {
                        markSelectedUsername(getUsername());
                    }

                    @Override
                    public void actionDeleteChat() {
                        handleDeleteChat(getChatID());
                    }

                }, gbc2, list.getComponentCount());
            }

        }
        JPanel spacer = new JPanel();
        spacer.setSize(0, addition);
        spacer.setBackground(new Color(150, 150, 150));

        list.add(spacer, gbc2, list.getComponentCount());

        jsp = new ModernScrollPane(list, Color.black);
        list.setBackground(new Color(150, 150, 150));
        jsp.setAlignmentY(TOP_ALIGNMENT);

        jsp.setBackground(Color.WHITE);

        //jsp.getVerticalScrollBar().setUI(new PenguMessengerScrollBarUI());
        jsp.getVerticalScrollBar().setUnitIncrement(20);
        //this.Settings.add(jsp);

        if (scroll != null) {
            jsp.getViewport().scrollRectToVisible(scroll);
        }
        scroll = null; //reset scroll

        if (items.length == 0) {
            JLabel temp = new JLabel("<html>No chats have been started.<br>Go to contacts to start a new chat.</html>", SwingConstants.CENTER);
            temp.setPreferredSize(new Dimension(300, 70));

            JButton contacts = new JButton("Go to Contacts");
            contacts.addActionListener((ActionEvent e) -> {
                // display/center the jdialog when the button is pressed
                goToContacts();
            });

            list.add(temp, gbc2, list.getComponentCount());
            list.add(contacts, gbc2, list.getComponentCount());
        }

        this.add(jsp);

        this.revalidate();

    }

    // method is called when chat is requested to be deleted.
    private void handleDeleteChat(int chatID) {
        System.out.println("Delete Chat: " + chatID);
        pmc.deleteActiveChat(chatID);
        reloadFrame();
        deleteChatTarget = chatID;
        respondToDeleteChat();
    }

    /**
     * Accessor method for the deleteChatTarget field.
     *
     * @return Returns the value of the deleteChatTarget field.
     */
    public int getDeleteChatTarget() {
        return deleteChatTarget;
    }

    /**
     * Un-marks any username that has been marked as selected.
     */
    public void resetSelectionVars() {
        selectedChatID = -1;
        selectedUsername = "";
    }

    /**
     * Override this to choose what happens when a chat is deleted.
     */
    public void respondToDeleteChat() {

    }

    /**
     * Override this to chose what happens when the go to contacts button is
     * pressed.
     */
    public void goToContacts() {

    }

    //Helper method to mark a username as selected and refresh the items array.
    private void markSelectedUsername(String selectedUsername) {
        scroll = jsp.getViewport().getViewRect();

        if (this.selectedUsername.equals(selectedUsername)) {
            // allready selected
        } else {
            this.selectedUsername = selectedUsername;
            reloadFrame();
            selectedAction();
        }
    }

    /**
     * Accessor method for the selectedChatID field.
     * 
     * @return Returns the value of the selectedChatID field.
     */
    public int getSelectedChatID() {
        return selectedChatID;
    }
    
    /**
     * Accessor method for the selectedUsername field.
     * 
     * @return Returns the value of the selectedUsername field.
     */
    public String getSelectedUsername() {
        return selectedUsername;
    }

    /**
     * Override this to choose what happens when Message View Box is selected
     */
    public void selectedAction() {
    }

}
